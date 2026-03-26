# C4-2: Диаграмма контейнеров

**Уровень:** Container (C4-2)  
**Система:** OK Marketplace  
**Версия:** 5.0  
**Дата:** 2026-03-26  
**Статус:** Готово к review

---

## 1. Обзор

Данный документ описывает архитектуру контейнеров системы OK Marketplace — полная архитектура микросервисов.

**Состав системы:**

### MVP (реализуется сейчас)

| № | Микросервис      | Тип        | Назначение                                 | Статус |
|---|------------------|------------|--------------------------------------------|--------|
| 1 | AD Service       | Microservice | Управление объявлениями (CRUD, поиск)      | MVP    |

### Roadmap (будущие сервисы)

| № | Микросервис            | Тип          | Назначение                                   | Статус   |
|---|------------------------|--------------|----------------------------------------------|----------|
| 2 | Profile Service       | Microservice | Управление профилями пользователей           | Roadmap  |
| 3 | Chat Service          | Microservice | Мессенджер между продавцом и покупателем    | Roadmap  |
| 4 | Notification Service  | Microservice | Отправка push/email/SMS уведомлений          | Roadmap  |
| 5 | Monetization Service  | Microservice | Платежи, подписки, комиссии                  | Roadmap  |
| 6 | Verification Service  | Microservice | Верификация документов, рейтинг продавцов    | Roadmap  |

---

## 2. Контейнеры

### 2.1 API Gateway (Envoy)

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | API Gateway                                                                          |
| **Тип**              | Контейнер (шлюз)                                                                    |
| **Технология**       | Envoy Proxy                                                                          |
| **Назначение**       | Единая точка входа; маршрутизация запросов; балансировка нагрузки; валидация JWT    |
| **Порт**             | 443 (HTTPS/HTTP2)                                                                    |
| **Протоколы**       | HTTP/2, WebSocket                                                                    |
| **Ответственность**  | - Аутентификация запросов<br>- Маршрутизация к сервисам<br>- Rate limiting<br>- Логирование |

### 2.2 AD Service

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | AD Service                                                                           |
| **Тип**              | Микросервис (Kotlin, Ktor)                                                          |
| **Назначение**       | Управление объявлениями (ads): создание, чтение, обновление, удаление, поиск        |
| **Порт**             | 8080                                                                                 |
| **Статус**           | **MVP** — реализуется сейчас                                                         |
| **Ответственность**  | - CRUD-операции с объявлениями (ads)<br>- Валидация данных<br>- Полнотекстовый поиск<br>- Фильтрация и сортировка |

**API Endpoints:**

```
AD Service
└── /api/ad/v1
```

### 2.3 Shell Web App

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Shell Web App                                                                        |
| **Тип**              | Контейнер (хост-приложение)                                                          |
| **Технология**       | Compose Multiplatform (Web/JS)                                                       |
| **Назначение**       | Оболочка приложения: навигация, layout, lazy-loading микрофронтендов, мост авторизации |
| **Статус**           | **MVP**                                                                              |
| **Ответственность**  | - Единый layout (header, sidebar, footer)<br>- Shell Router: навигация между MFE<br>- Auth-bridge: JWT handling, сессия<br>- Lazy-loading модулей по маршруту |

### 2.4 Auth Microfrontend

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Auth Microfrontend                                                                   |
| **Тип**              | Контейнер (микрофронтенд)                                                            |
| **Технология**       | Compose Multiplatform Module                                                         |
| **Назначение**       | UI-модуль аутентификации: логин, регистрация, восстановление пароля, OIDC          |
| **Статус**           | **MVP**                                                                              |
| **Ответственность**  | - Экраны входа и регистрации<br>- Social login (Google, GitHub)<br>- Валидация форм<br>- Передача OIDC flow в IAM |

**Связи:**
- Shell Web App → lazy-load → Auth Microfrontend
- Auth Microfrontend → API Gateway → IAM (Casdoor) — OIDC flow

### 2.5 Ad Microfrontend

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Ad Microfrontend                                                                     |
| **Тип**              | Контейнер (микрофронтенд)                                                            |
| **Технология**       | Compose Multiplatform Module                                                         |
| **Назначение**       | UI-модуль объявлений: CRUD, поиск, каталог, фильтры                                |
| **Статус**           | **MVP**                                                                              |
| **Ответственность**  | - Создание и редактирование объявлений<br>- Список и карточка объявления<br>- Поиск и фильтрация<br>- Категории и каталог |

**Связи:**
- Shell Web App → lazy-load → Ad Microfrontend
- Ad Microfrontend → API Gateway → AD Service — REST API

### 2.6 Profile Microfrontend

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Profile Microfrontend                                                                |
| **Тип**              | Контейнер (микрофронтенд)                                                            |
| **Технология**       | Compose Multiplatform Module                                                         |
| **Назначение**       | UI-модуль профиля: данные пользователя, избранное, история просмотров                |
| **Статус**           | **Roadmap**                                                                          |
| **Ответственность**  | - Редактирование профиля (имя, фото, bio)<br>- Список избранного<br>- История просмотров<br>- Настройки уведомлений |

**Связи:**
- Shell Web App → lazy-load → Profile Microfrontend
- Profile Microfrontend → API Gateway → Profile Service — REST API

### 2.7 Chat Microfrontend

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Chat Microfrontend                                                                   |
| **Тип**              | Контейнер (микрофронтенд)                                                            |
| **Технология**       | Compose Multiplatform Module                                                         |
| **Назначение**       | UI-модуль чата: окно диалога, список чатов, real-time сообщения                     |
| **Статус**           | **Roadmap**                                                                          |
| **Ответственность**  | - Окно чата между продавцом и покупателем<br>- Список диалогов<br>- WebSocket для real-time<br>- Отправка текста и фото |

**Связи:**
- Shell Web App → lazy-load → Chat Microfrontend
- Chat Microfrontend → API Gateway → Chat Service — REST + WebSocket

---

### 2.8 Profile Service

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Profile Service                                                                      |
| **Тип**              | Микросервис                                                                          |
| **Технология**       | Kotlin, Ktor                                                                         |
| **Назначение**       | Управление профилями пользователей, аватарки, история просмотров, избранное          |
| **Порт**             | 8081                                                                                 |
| **Статус**           | **Roadmap**                                                                          |
| **Ответственность**  | - Профиль пользователя (имя, фото, bio)<br>- История просмотров<br>- Избранные объявления<br>- Настройки уведомлений |

**API Endpoints:**

```
Profile Service
└── /api/profile/v1
```

### 2.9 Chat Service

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Chat Service                                                                         |
| **Тип**              | Микросервис                                                                          |
| **Технология**       | Kotlin, Ktor + WebSocket                                                             |
| **Назначение**       | Мессенджер для коммуникации между продавцом и покупателем                           |
| **Порт**             | 8082                                                                                 |
| **Статус**           | **Roadmap**                                                                          |
| **Ответственность**  | - Чат-комнаты между пользователями<br>- Сообщения (текст, фото)<br>- WebSocket для real-time<br>- Уведомления о новых сообщениях |

**API Endpoints:**

```
Chat Service
├── /api/chat/v1          (CRUD чатов)
└── /ws/chat/v1/messages  (WebSocket)
```

### 2.10 Notification Service

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Notification Service                                                                |
| **Тип**              | Микросервис                                                                          |
| **Технология**       | Kotlin, Ktor                                                                         |
| **Назначение**       | Централизованная отправка уведомлений различных типов                                |
| **Порт**             | 8083                                                                                 |
| **Статус**           | **Roadmap**                                                                          |
| **Ответственность**  | - Email уведомления<br>- Push-уведомления (FCM/APNs)<br>- SMS<br>- Шаблонизация<br>- Очередь отправки |

**API Endpoints:**

```
Notification Service
└── /api/notification/v1
```

### 2.11 Monetization Service

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Monetization Service                                                                 |
| **Тип**              | Микросервис                                                                          |
| **Технология**       | Kotlin, Ktor                                                                         |
| **Назначение**       | Платежи, подписки, комиссии с транзакций, финансовая аналитика                       |
| **Порт**             | 8084                                                                                 |
| **Статус**           | **Roadmap**                                                                          |
| **Ответственность**  | - Обработка платежей (Stripe/ЮKassa)<br>- Подписки (Premium)<br>- Комиссии с продаж<br>- Кошельки пользователей<br>- Вывод средств |

**API Endpoints:**

```
Monetization Service
├── /api/monetization/v1/payments
├── /api/monetization/v1/subscriptions
└── /api/monetization/v1/wallets
```

### 2.12 Verification Service

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | Verification Service                                                                |
| **Тип**              | Микросервис                                                                          |
| **Технология**       | Kotlin, Ktor                                                                         |
| **Назначение**       | Верификация документов продавцов, рейтинг trust score                                |
| **Порт**             | 8085                                                                                 |
| **Статус**           | **Roadmap**                                                                          |
| **Ответственность**  | - Верификация паспорта/ID<br>- Проверка телефона<br>- Рейтинг продавцов (trust score)<br>- Статус "Проверен"<br>- Споры и арбитраж |

**API Endpoints:**

```
Verification Service
├── /api/verification/v1/verification
├── /api/verification/v1/trust-scores
└── /api/verification/v1/disputes
```

### 2.13 IAM

| Атрибут              | Описание                                                                                                      |
|----------------------|---------------------------------------------------------------------------------------------------------------|
| **Название**         | IAM                                                                                                           |
| **Тип**              | Контейнер (внешний сервис)                                                                                    |
| **Технология**       | Casdoor                                                                                                       |
| **Назначение**       | Провайдер аутентификации; управление пользователями; выпуск JWT                                               |
| **Порт**             | 8000 (внутренний)                                                                                             |
| **Протоколы**       | OIDC                                                                                              |
| **Ответственность**  | - Регистрация и вход пользователей<br>- Управление организациями<br>- Выпуск и валидация JWT-токенов<br>- SSO |

### 2.14 PostgreSQL (AD Service DB)

| Атрибут              | Описание                                                                              |
|----------------------|--------------------------------------------------------------------------------------|
| **Название**         | PostgreSQL — База данных AD Service                                                 |
| **Тип**              | Контейнер (управляемая СУБД)                                                         |
| **Технология**       | PostgreSQL 15+                                                                        |
| **Назначение**       | Персистентное хранение объявлений                                                      |
| **Схема данных**    | ads, companies                                                                       |

### 2.15 PostgreSQL (IAM DB)

| Атрибут              | Описание                                                 |
|----------------------|----------------------------------------------------------|
| **Название**         | PostgreSQL — База данных IAM                             |
| **Тип**              | Контейнер (управляемая СУБД)                             |
| **Технология**       | PostgreSQL 15+                                           |
| **Назначение**       | Хранение пользователей, организаций, сессий, токенов IAM |

---

## 3. Внешние системы

### 3.1 Email Provider

| Атрибут        | Описание                                                           |
|----------------|--------------------------------------------------------------------|
| **Название**   | Email Provider                                                     |
| **Тип**        | Внешний сервис                                                     |
| **Назначение** | Отправка email-уведомлений                                         |
| **Протокол**   | SMTP / API                                                         |
| **Сценарии**   | Регистрация, смена пароля, уведомления о новых объявлениях         |

---

## 4. Диаграмма Container

```mermaid
C4Container
    title [L2] Диаграмма контейнеров — OK Marketplace (Micro-frontends)

    Person(user, "Пользователь", "Продавец / Покупатель")

    System_Ext(api_gw, "API Gateway (Envoy)", "Edge Proxy", "Маршрутизация трафика (UI + API)")
    System_Ext(iam, "IAM (Casdoor)", "OIDC", "Auth & Identity")

    Container_Boundary(marketplace, "OK Marketplace") {

        Container(shell, "Shell Web App", "Compose/JS", "Хост-приложение (Layout, Навигация, Auth-bridge, Lazy-loading)")

        Container_Boundary(mfe_layer, "Micro-frontends") {
            Container(auth_mfe, "Auth Microfrontend", "Compose Module", "UI: Login, Register, OIDC")
            Container(ad_mfe, "Ad Microfrontend", "Compose Module", "UI: Поиск, карточки, подача")
            Container(profile_mfe, "Profile Microfrontend", "Compose Module", "UI: Профиль, избранное (Roadmap)")
            Container(chat_mfe, "Chat Microfrontend", "Compose Module", "UI: Окно чата, диалоги (Roadmap)")
        }

        Container_Boundary(ad_domain, "Domain: Ads (MVP)") {
            Container(ad_service, "AD Service", "Kotlin, Ktor", "Backend: CRUD, Поиск")
            ContainerDb(postgres_ad, "PostgreSQL (AD)", "DB")
        }

        Container_Boundary(chat_domain, "Domain: Chats (Roadmap)") {
            Container(chat_service, "Chat Service", "Kotlin, Ktor", "Backend: WS, История сообщений")
        }
    }

%% Маршрутизация трафика
    Rel_Down(user, api_gw, "Вход в систему", "HTTPS")

%% Шлюз раздает и фронт, и данные
    Rel_Down(api_gw, shell, "Загрузка оболочки", "Static")
    Rel_Down(api_gw, auth_mfe, "Загрузка Auth MFE", "Static")
    Rel_Down(api_gw, ad_mfe, "Загрузка Ads MFE", "Static")
    Rel_Down(api_gw, profile_mfe, "Загрузка Profile MFE", "Static")
    Rel_Down(api_gw, chat_mfe, "Загрузка Chat MFE", "Static")

    Rel_Down(api_gw, ad_service, "API: /api/ad/v1/*", "HTTP2")
    Rel_Down(api_gw, chat_service, "API: /api/chat/v1/*", "HTTP2/WS")
    Rel_Down(api_gw, iam, "OIDC", "HTTPS")

%% Внутренние связи
    Rel_Right(shell, auth_mfe, "Lazy-load")
    Rel_Right(shell, ad_mfe, "Lazy-load")
    Rel_Right(shell, profile_mfe, "Lazy-load")
    Rel_Right(shell, chat_mfe, "Lazy-load")

%% MFE → API Gateway
    Rel_Up(auth_mfe, api_gw, "OIDC flow", "HTTPS")
    Rel_Up(ad_mfe, api_gw, "Запросы данных", "JSON/JWT")
    Rel_Up(profile_mfe, api_gw, "Запросы данных", "JSON/JWT")
    Rel_Up(chat_mfe, api_gw, "Запросы данных", "JSON/JWT")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

---

## 5. Потоки данных

### 5.1 Регистрация и аутентификация

```mermaid
sequenceDiagram
    autonumber
    participant User as Пользователь
    participant Web as Compose Web
    participant IAM as IAM (Casdoor)
    participant GW as API Gateway (Envoy)
    participant AD as AD Service

    Note over User, Web: 1. Проверка сессии
    User->>Web: Открыть маркетплейс
    Web->>Web: Токен отсутствует?

    Note over User, IAM: 2. Аутентификация (OIDC Flow)
    Web->>IAM: Редирект на Login Page
    User->>IAM: Ввод логина/пароля
    IAM->>IAM: Проверка учетных данных
    IAM-->>Web: Authorization Code (через Redirect URI)

    Note over Web, IAM: 3. Получение JWT
    Web->>IAM: Обмен Code на Tokens (POST /token)
    IAM-->>Web: Access Token (JWT) + ID Token

    Note over User, AD: 4. Выполнение запроса
    Web->>GW: GET /api/ad/v1 (Header: Bearer JWT)
    Note over GW: Валидация подписи JWT (Local)
    GW->>AD: Проксирование запроса
    AD-->>GW: Список объявлений
    GW-->>Web: 200 OK + Data
    Web-->>User: Отображение контента
```

### 5.2 Создание объявления (AD)

```mermaid
sequenceDiagram
    autonumber
    participant User as Продавец
    participant Web as Compose Web
    participant GW as API Gateway (Envoy)
    participant AD as AD Service
    participant DB as PostgreSQL (AD)

    User->>Web: Заполнить форму объявления
    Web->>GW: POST /api/ad/v1 (Header: Bearer JWT)

    Note over GW: Stateless-валидация JWT (Public Key)

    GW->>AD: Proxy POST /ad/v1 (Header: Bearer JWT)

    Note over AD: Извлечение user_id из JWT Claims

    AD->>DB: INSERT INTO ads (user_id, content, ...)
    DB-->>AD: Row Created (ID: 777)

    AD-->>GW: 201 Created (JSON)
    GW-->>Web: 201 Created
    Web-->>User: "Объявление успешно опубликовано"
```

### 5.3 Чат между продавцом и покупателем (Chat Service — Roadmap)

```mermaid
sequenceDiagram
    autonumber
    participant Buyer as Покупатель
    participant Seller as Продавец
    participant Web as Compose Web
    participant GW as API Gateway (Envoy)
    participant Chat as Chat Service

    Note over Buyer, Web: Начало чата
    Buyer->>Web: "Хочу купить товар"
    Web->>GW: POST /api/chat/v1 (JWT)

    GW->>Chat: Создать чат ( buyer_id, seller_id )
    Chat-->>GW: chat_id created

    GW-->>Web: 201 Created (chat_id)
    Web-->>Buyer: Чат открыт

    Note over Buyer, Seller: WebSocket для real-time
    Buyer->>WS: WS /ws/chat/v1/messages
    Seller->>WS: WS /ws/chat/v1/messages

    Buyer->>WS: "Готов купить за 5000"
    WS->>Seller: "Готов купить за 5000"
```

---

## 6. Маршрутизация API Gateway

```mermaid
graph LR
    User([Пользователь]) --> GW{API Gateway <br/> Envoy}

    subgraph "MVP"
        AD[AD Service]
    end

    subgraph "Roadmap"
        Profile[Profile Service]
        Chat[Chat Service]
        Notif[Notification Service]
        Monet[Monetization Service]
        Verif[Verification Service]
    end

    subgraph "External"
        IAM[IAM / Casdoor]
    end

    GW -- "/api/auth/*" --> IAM

    GW -- "/api/ad/v1/*" --> AD

    GW -- "/api/profile/v1/*" --> Profile
    GW -- "/api/chat/v1/*" --> Chat
    GW -- "/api/notification/v1/*" --> Notif
    GW -- "/api/monetization/v1/*" --> Monet
    GW -- "/api/verification/v1/*" --> Verif
```

---

## 7. Границы масштабирования

### 7.1 Единые точки отказа (SPOF)

| Компонент              | Риск SPOF                     | Митигация                                                   |
|------------------------|-------------------------------|-------------------------------------------------------------|
| API Gateway            | Шлюз недоступен              | Горизонтальное масштабирование (несколько инстансов)       |
| Shell Web App          | Оболочка недоступна          | CDN; fallback на полный reload                              |
| Auth Microfrontend     | Модуль недоступен            | Fallback на прямой редирект на IAM                         |
| Ad Microfrontend       | Модуль недоступен            | CDN cache; SSR fallback                                     |
| AD Service             | Сервис недоступен            | Горизонтальное масштабирование; health checks              |
| Profile Service        | Сервис недоступен            | Горизонтальное масштабирование                              |
| Chat Service           | Сервис недоступен            | Горизонтальное масштабирование; WebSocket sticky sessions  |
| PostgreSQL (AD)        | База данных недоступна       | Репликация (master-slave или patroni)                      |
| IAM                    | Сервис недоступен            | Кэширование JWT на Gateway; несколько инстансов           |

### 7.2 Узкие места

| Компонент           | Узкое место              | Решение                                                    |
|---------------------|--------------------------|------------------------------------------------------------|
| PostgreSQL          | Запись/чтение при нагрузке | Индексы; read replicas; шардирование (будущее)             |
| Chat Service        | WebSocket соединения    | Connection pooling; Redis для pub/sub                     |
| Notification Queue  | Очередь уведомлений     | Kafka/RabbitMQ для асинхронной обработки                  |
| Monetization Service| PCI DSS compliance      | Вынос платежей в внешний PCI DSS хранилище                |

---

## 8. Ключевые допущения

1. **AD Service — единственный MVP-сервис** — на текущий момент реализуется только AD Service. Остальные сервисы в статусе Roadmap.

2. **Внешний IAM** — IAM на базе Casdoor рассматривается как внешний сервис, развёртываемый отдельно.

3. **Email Provider как внешняя зависимость** — Отправка email делегируется внешнему провайдеру через SMTP/API.

4. **Горизонтальное масштабирование** — Каждый микросервис масштабируется независимо (несколько инстансов за балансировщиком).

5. **Синхронные коммуникации для MVP** — AD Service взаимодействует с БД синхронно. В будущем асинхронные операции (Chat, Notifications) будут вынесены в очереди.

6. **Общая база для MVP** — AD Service имеет собственную PostgreSQL. Прочие сервисы в будущем получат собственные БД или перейдут на sharded/shared решение.

---

## 9. Версионирование API

| Endpoint                  | Сервис              | Версия |
|---------------------------|---------------------|--------|
| /api/ad/v1                | AD Service          | v1     |
| /api/profile/v1           | Profile Service     | v1     |
| /api/chat/v1              | Chat Service        | v1     |
| /api/notification/v1      | Notification Service| v1     |
| /api/monetization/v1      | Monetization Service| v1     |
| /api/verification/v1      | Verification Service| v1     |

---

*Document Version: 5.0*  
*Created: 2026-03-26*  
*Status: Готово к review*