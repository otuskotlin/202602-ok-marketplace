FROM swaggerapi/swagger-ui:latest

# 1. Создаем папку для спек внутри контейнера
USER root
RUN mkdir -p /usr/share/nginx/html/specs && apk add --no-cache jq

# 2. Копируем все ваши файлы .yaml или .json в контейнер
# Предполагаем, что они лежат в локальной папке ./my-specs
COPY ./specs-*.yaml /usr/share/nginx/html/specs

# Удаляем вредителя
RUN rm /docker-entrypoint.d/40-swagger-ui.sh

COPY generate-config.sh /docker-entrypoint.d/35-generate.sh
RUN chmod +x /docker-entrypoint.d/35-generate.sh

EXPOSE 8080