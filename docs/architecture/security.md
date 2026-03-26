# Архитектура безопасности — OK Marketplace

## 1. Модель угроз (Threat Model)

| Угроза | Описание | Уровень риска | Митигация |
|--------|----------|---------------|-----------|
| **Кража токенов** | Перехват JWT через XSS или MITM | Высокий | Short-lived access tokens; HttpOnly cookies; TLS |
| **Подмена ID пользователя** | Манипуляция с user_id в JWT claims | Средний | Криптографическая верификация JWT на Gateway |
| **Брутфорс** | Подбор пароля на странице входа | Средний | Rate limiting; lockout policy; captcha |
| **CSRF** | Подмена запросов от имени пользователя | Средний | CSRF tokens; SameSite cookies |
| **SQL Injection** | Инъекция в запросы к БД | Высокий | Parameterized queries; ORM |
| **SSRF** | Атака через URL в пользовательском вводе | Средний | Валидация URL; whitelist |
| **DDoS** | Отказ в обслуживании | Высокий | Rate limiting; CDN; WAF |

---

## 2. Аутентификация (AuthN)

### 2.1 OIDC Flow

| Параметр | Значение |
|----------|----------|
| **Flow** | Authorization Code Flow with PKCE |
| **Token Endpoint** | POST /oidc/token (IAM Casdoor) |
| **Grant Type** | authorization_code + pkce |
| **Code Verifier** | SHA-256, 43-128 символов |
| **Code Challenge** | BASE64URL(SHA256(code_verifier)) |

### 2.2 Token Management

| Token | Время жизни | Хранение | Использование |
|-------|-------------|----------|----------------|
| **Access Token** | 15 минут | In-memory / Memory-safe | Авторизация API запросов |
| **Refresh Token** | 30 дней | HttpOnly Secure Cookie | Обновление access token |
| **ID Token** | 15 минут | In-memory | Профиль пользователя |

### 2.3 Authentication Flow

```mermaid
sequenceDiagram
    autonumber
    participant User as Пользователь
    participant Shell as Shell Web App
    participant IAM as IAM (Casdoor)
    participant GW as API Gateway

    User->>Shell: Открыть приложение
    Shell->>Shell: Токен отсутствует?

    Shell->>IAM: Редирект /oidc/authorize?client_id=...&redirect_uri=...&response_type=code&scope=openid%20profile&code_challenge=...&code_challenge_method=S256
    
    User->>IAM: Ввод логина/пароля
    IAM->>IAM: Аутентификация
    
    IAM-->>Shell: 302 Redirect /callback?code=AUTH_CODE
    
    Shell->>IAM: POST /oidc/token (code=AUTH_CODE&code_verifier=...)
    IAM-->>Shell: { access_token, refresh_token, id_token }
    
    Shell->>GW: Запросы с Bearer {access_token}
```

---

## 3. Авторизация (AuthZ)

### 3.1 Двухуровневая модель

```mermaid
flowchart LR
    subgraph L1["Level 1: Gateway (Stateless JWT Verification)"]
        direction TB
        A1["Проверка подписи (RSA/EdDSA)"]
        A2["Проверка срока действия"]
        A3["Проверка issuer и audience"]
        A4["Извлечение user_id и roles"]
    end

    subgraph L2["Level 2: Microservice (Domain Authorization - Casbin)"]
        direction TB
        B1["Проверка ролей (RBAC)"]
        B2["Проверка прав на ресурс (ABAC)"]
        B3["Владелец ресурса?"]
    end

    L1 -->|user_id + roles| L2
```

### 3.2 Role-Based Access Control (RBAC)

| Роль | Описание | Permitted Actions |
|------|----------|-------------------|
| **guest** | Неавторизованный | Просмотр объявлений |
| **user** | Авторизованный | Создание объявлений, чат |
| **seller** | Продавец | CRUD своих объявлений |
| **moderator** | Модератор | Удаление любых объявлений |
| **admin** | Администратор | Полный доступ |

### 3.3 ABAC Policy Example

```yaml
# Casbin Policy
p, seller, ad, read, owner, equal
p, seller, ad, update, owner, equal
p, seller, ad, delete, owner, equal
p, moderator, ad, delete, *, *
p, admin, *, *, *, *
```

---

## 4. Безопасность данных (Data Security)

### 4.1 Encryption at Rest

| Данные | Шифрование | Ключ |
|--------|-------------|------|
| PostgreSQL (все таблицы) | AES-256-GCM | Yandex Lockbox / KMS |
| S3 (изображения) | Server-side encryption | AWS S3 SSE-KMS |
| Secrets (пароли, ключи) | Encrypted vault | HashiCorp Vault / Yandex Lockbox |

### 4.2 Encryption in Transit

| Компонент | Протокол | Версия |
|-----------|----------|--------|
| Client → Gateway | TLS | 1.2 (min), 1.3 (preferred) |
| Gateway → Service | mTLS (будущее) | 1.3 |
| Service → DB | TLS | 1.2+ |
| Service → External API | TLS | 1.2+ |

### 4.3 Secrets Management

```mermaid
flowchart TB
    subgraph Vault["Secrets Storage (Yandex Lockbox / Vault)"]
        direction TB
        V1["API keys"]
        V2["Database passwords"]
        V3["JWT signing keys"]
        V4["OIDC client secrets"]
    end

    subgraph Inject["Injection"]
        I1["Environment / Vault Agent"]
    end

    Vault -->|Injection| Inject
```

### 4.4 Data Classification

| Категория | Примеры | Защита |
|-----------|---------|--------|
| **Public** | Объявления, названия товаров | Нет ограничений |
| **Private** | Email, phone, имя пользователя | Encryption at rest; Access logging |
| **Sensitive** | Payment info, documents | Encryption; Strict access; Audit |

---

## 5. Сетевая безопасность (Network Security)

### 5.1 Network Isolation

```mermaid
flowchart TB
    subgraph VPC["VPC / Cloud Network"]
        direction LR
        
        subgraph Public["Public Subnet"]
            P1["Gateway"]
            P2["CDN"]
            P3["WAF"]
        end
        
        subgraph Private["Private Subnet"]
            Pr1["AD Service"]
            Pr2["Chat Service"]
            Pr3["Database"]
        end
    end
```

### 5.2 Database Access

| Правило | Описание |
|---------|----------|
| Security Group | Разрешён входящий трафик только с подсети бэкенда |
| Port | Только 5432 (PostgreSQL) |
| Authentication | IAM-based; пароли с ротацией |

### 5.3 CORS Policy

```
Allowed Origins:
  - https://okmarketplace.ru
  - https://www.okmarketplace.ru
  - https://dev.okmarketplace.ru (Development)

Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Authorization, Content-Type, X-Request-ID
Credentials: true
Max Age: 86400 (24 hours)
```

### 5.4 Rate Limiting

| Эндпоинт | Лимит | Окно |
|----------|-------|------|
| /api/auth/* | 10 req/min | Per IP |
| /api/ad/v1 (write) | 30 req/min | Per user |
| /api/ad/v1 (read) | 100 req/min | Per user |
| /api/chat/v1 | 60 req/min | Per user |

---

## 6. Security Flow

```mermaid
sequenceDiagram
    autonumber
    participant U as User
    participant G as Envoy Gateway
    participant IAM as IAM (Casdoor)
    participant Svc as AD Service (Casbin)
    participant DB as PostgreSQL

    Note over U, G: Request with JWT
    U->>G: GET /api/ad/v1/123 (Header: Bearer eyJhbGci...)
    
    Note over G: L1 - JWT Verification
    G->>G: Extract JWT claims
    G->>G: Verify signature (RS256)
    G->>G: Check exp, iss, aud
    alt Token Invalid/Expired
        G-->>U: 401 Unauthorized (Token expired)
    else Token Valid
        Note over G: Extract user_id, roles
        G->>Svc: Proxy request + user_id in header
    end

    Note over Svc: L2 - Domain Authorization
    Svc->>Svc: Extract resource ID from path
    Svc->>Svc: Casbin: enforce(user_id, "ad", "read", "123")
    
    alt Access Denied
        Svc-->>U: 403 Forbidden (No permission)
    else Access Granted
        Svc->>DB: SELECT * FROM ads WHERE id = 123
        DB-->>Svc: Ad record
        Svc-->>G: 200 OK + Ad data
        G-->>U: 200 OK + JSON
    end

    Note over G, Svc: Audit Logging
    G->>Logs: Access log: method, path, user_id, status, latency
    Svc->>Logs: AuthZ log: user, resource, action, result
```

---

## Appendix: Security Checklist

- [x] TLS 1.2+/1.3 everywhere
- [x] JWT with RS256/EdDSA
- [x] Short-lived access tokens (15 min)
- [x] HttpOnly Secure cookies for refresh token
- [x] CORS whitelist
- [x] Rate limiting on Gateway
- [x] DB encryption at rest
- [x] Secrets in Vault/Lockbox
- [x] RBAC + ABAC (Casbin)
- [x] Audit logging for auth events
- [x] WAF for OWASP Top 10