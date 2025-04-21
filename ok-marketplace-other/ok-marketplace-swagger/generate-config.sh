#!/bin/sh

# 1. Собираем список для выпадающего меню
FILES=$(ls /usr/share/nginx/html/specs)
URLS=$(echo "$FILES" | jq -R -s -c 'split("\n") | map(select(length > 0) | {url: ("specs/" + .), name: .})')

# 2. Подменяем сервер во всех файлах спецификаций
# Предположим, мы передаем новый URL через переменную API_SERVER_URL
if [ -n "$API_SERVER_URL" ]; then
  echo "Patching servers to $API_SERVER_URL"
  # Ищем блок servers: и заменяем url под ним.
  # Это сработает для простого случая как в твоем примере.
  find /usr/share/nginx/html/specs -type f \( -name "*.yaml" -o -name "*.yml" \) -exec sed -i "/servers:/,/url:/ s|url:.*|url: $API_SERVER_URL|g" {} +
  # Для JSON файлов:
  find /usr/share/nginx/html/specs -type f -name "*.json" -exec sed -i "s|\"url\": *\"[^\"]*\"|\"url\": \"$API_SERVER_URL\"|g" {} +
fi

# 3. Генерируем конфиг UI (без маркеров, т.к. 40-й скрипт мы удалим)
cat <<EOF > /usr/share/nginx/html/swagger-initializer.js
window.onload = function() {
  window.ui = SwaggerUIBundle({
    urls: $URLS,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
    plugins: [SwaggerUIBundle.plugins.DownloadUrl],
    layout: 'StandaloneLayout'
  });
};
EOF
