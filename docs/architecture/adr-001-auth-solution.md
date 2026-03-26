# ADR-001: Выбор решения аутентификации для B2B Маркетплейса

## Context

Для MVP B2B маркетплейса требуется решение identity and access management (IAM), обеспечивающее:
- Multi-tenancy: изоляция организаций (продавцов) с собственными пользователями и политиками
- OAuth 2.0 / OIDC: стандартные протоколы для интеграции с фронтендом и внешними системами
- RBAC: разграничение прав (Админ организации, Менеджер, Пользователь)
- SSO: вход для сотрудников компаний-партнёров через корпоративные IdP
- Serverless-ready: совместимость с Y.Cloud Serverless (Cloud Functions)
- Kotlin SDK: для backend на Kotlin/JVM

## Comparison

Сравнение четырёх кандидатов по ключевым критериям:

| Критерий | Keycloak | SuperTokens | Casdoor + Casbin | Zitadel |
|----------|:--------:|:------------:|:----------------:|:-------:|
| **Сложность развёртывания в Y.Cloud Serverless** | 🔴 Сложно (JVM, 512MB+ RAM, требует Kubernetes) | 🟢 Просто (Go binary, <100MB) | 🟢 Просто (Go, Docker-ready, есть examples для AWS Lambda) | 🟡 Средне (Go, требует PostgreSQL, официальный Knative guide) |
| **B2B применимость** | 🟢 Отлично (Realm-based isolation, LDAP/AD sync) | 🟡 Ограниченно (Focus на B2C, базовый tenant management) | 🟢 Отлично (Organizations, LDAP sync, SAML) | 🟢 Отлично (Organizations, Instance-based isolation) |
| **Multi-tenancy** | 🟢 Realms = отдельные изолированные окружения | 🟡 Limited (App-level tenants, не полная изоляция) | 🟢 Organizations с изолированными пользователями/приложениями | 🟢 Instances + Organizations (двойная изоляция) |
| **Kotlin SDK** | ⚠️ Community-only (keycloak-spring-boot-starter) | ⚠️ Community (supertokens-kt, Ktor plugin) | ⚠️ Community (casdoor-java-sdk с Kotlin extensions) | ⚠️ Community (нет официального, только Java gRPC client) |
| **RBAC** | 🟢 Full (Client roles, realm roles, composite roles) | ⚠️ Basic (только user roles) | 🟢 Full (Casbin с Model + Policy, ABAC support) | 🟢 Full (Project roles, organizational roles) |
| **MFA** | 🟢 TOTP, WebAuthn, SMS, Email | 🟢 TOTP, WebAuthn, Email | 🟢 TOTP, WebAuthn, SMS | 🟢 TOTP, WebAuthn |
| **Social Login** | 🟢 20+ провайдеров | 🟢 30+ провайдеров | 🟢 50+ провайдеров | 🟢 20+ провайдеров |
| **SAML 2.0** | 🟢 Да | ⚠️ Enterprise only | 🟢 Да | 🟢 Да |
| **LDAP/AD** | 🟢 Native | ❌ Нет | 🟢 Да | 🟢 Да |
| **UI Admin Console** | 🟡 Средне | ⚠️ Basic | 🟢 Отлично (React UI) | 🟡 Средне |
| **License** | Apache 2 | Apache 2 | Apache 2 | Apache 2 |
| **GitHub Stars** | 25K+ | 15K+ | 13K+ | 10K+ |
| **Активность разработки** | 🟢 Высокая | 🟢 Высокая | 🟢 Высокая | 🟢 Высокая |

## Decision

**Выбрано:** Casdoor + Casbin

## Rationale

Обоснование выбора для B2B маркетплейса:

1. **Лёгкость развёртывания в Y.Cloud Serverless:**
   - Go binary минимального размера (~80MB)
   - Официальные examples для AWS Lambda / serverless environments
   - Stateless operation с external PostgreSQL
   - Можно упаковать в Docker container для Y.Cloud Cloud Container Registry

2. **B2B-готовность:**
   - Organizations модель для tenant isolation
   - SAML 2.0 для корпоративных клиентов (критично для B2B)
   - LDAP sync для интеграции с AD компаний-партнёров
   - UI-first админка для самостоятельного управления организациями

3. **Multi-tenancy:**
   - Organizations = tenants с изолированными:
     - Пользователями
     - Приложениями (OAuth clients)
     - Ролями и правами
     - Провайдерами аутентификации
   - Organization hierarchy для сложных структур

4. **RBAC через Casbin:**
   - Нативная интеграция с Casbin для authorization
   - Гибкие модели: RBAC, ABAC, ACL
   - Policy stored in DB, versionable
   - Perfect fit для сложных B2B permission scenarios

5. **Kotlin интеграция:**
   - HTTP REST API + Java SDK = работает с Kotlin
   - Можно обернуть в Kotlin-специфичные extension functions
   - OkHttp client для type-safe интеграции

6. **Developer Experience:**
   - React-based admin UI — быстрая настройка без кода
   - 50+ social providers (Google, GitHub, Azure AD)
   - Comprehensive documentation + active Discord community

## Consequences

### Positive
- Быстрая настройка через UI админки
- Единое решение для auth + authorization (Casbin integration)
- Легковесный Go-based сервис (низкое потребление ресурсов)
- Активное сообщество (13K+ GitHub stars)
- Гибкие permission модели для B2B сценариев

### Negative
- Менее зрелый, чем Keycloak (меньше enterprise-grade features)
- Kotlin SDK community-driven, не official
- Документация менее полная, чем у Keycloak
- Меньше готовых интеграций с enterprise systems

### Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-------------|
| Kotlin SDK community-only | Medium | Medium | Использовать HTTP API + OkHttp + Kotlin extensions |
| Документация неполная | Medium | Low | Использовать source code + Discord community |
| Масштабирование под high load | Low | Medium | Stateless design + горизонтальное масштабирование + Redis sessions |
| B2B edge cases (complex org hierarchy) | Low | Medium | Casbin permission model покроет большинство сценариев |

### Alternatives Considered

Если в будущем понадобится более enterprise-grade решение:
- **Keycloak**: для полной enterprise интеграции (AD sync, complex federation)
- **Zitadel**: для более гибкой multi-instance архитектуры