# Technology Stack — MVP

> **Status**: MVP-Optimized (2 Services)

## Overview

```mermaid
graph TB
    subgraph "Client Layer"
        Frontend["Kotlin<br/>Compose Multiplatform Web"]
    end
    
    subgraph "API Gateway Layer"
        Gateway["Envoy Gateway<br/>Y.Cloud Managed"]
    end
    
    subgraph "Backend Layer (MVP)"
        MS["Marketplace Service<br/>Kotlin/Ktor"]
        CD["IAM<br/>Casdoor/OIDC"]
    end
    
    subgraph "Data Layer (MVP)"
        DB1["PostgreSQL<br/>Marketplace DB"]
        DB2["PostgreSQL<br/>Casdoor DB"]
    end
    
    subgraph "External"
        Email["Email Provider"]
    end
    
    Frontend --> Gateway
    Gateway --> MS
    Gateway --> CD
    MS --> DB1
    CD --> DB2
    MS --> Email
```

## Technology Stack Summary (MVP)

```mermaid
graph LR
    subgraph "Layers"
        Client["Client/Frontend"]
        Gateway["API Gateway"]
        Backend["Backend Services"]
        Data["Database"]
        Auth["Auth/Authorization"]
    end
    
    subgraph "Technologies (MVP)"
        FE["Kotlin<br/>Compose Multiplatform"]
        GW["Envoy Gateway<br/>Y.Cloud"]
%%        MS["Marketplace Service<br/>Kotlin/Ktor"]
        subgraph MS ["Marketplace Service (Kotlin/Ktor)"]
            Logic["Бизнес-логика"]
            CB["Casbin (Policy Engine)"]
            Logic --> CB
        end
        PG["PostgreSQL 15+"]
        CD["Casdoor<br/>/OIDC"]
        CB["Casbin<br/>RBAC/ABAC"]
    end
    
    Client --> FE
    Gateway --> GW
    Backend --> MS
    Data --> PG
    Auth --> CD
    Auth --> CB
    
    FE --> GW
    GW --> MS
    MS --> PG
    CD --> PG
    MS --> CB
```

## Frontend

```mermaid
graph TB
    subgraph "Frontend Stack"
        Framework["Kotlin<br/>Compose Multiplatform Web"]
        State["ViewModel<br/>UI State"]
        HTTP["Custom HTTP Client<br/>JWT handling"]
        Realtime["WebSocket<br/>via HTTP/2"]
    end
```

## Backend Services (MVP)

```mermaid
graph TB
    subgraph "AD Service"
        MS["Ktor<br/>Exposed ORM<br/>Casbin RBAC<br/>Ads Engine<br/>Email Notifications<br/>Kotlinx.Serialization"]
    end
    
    subgraph "IAM"
        CD["Casdoor/OIDC<br/>JWT<br/>User Management"]
    end
```

## API Gateway

```mermaid
graph LR
    subgraph Features ["Envoy Gateway Features"]
        HTTP2["HTTP/2<br/>Multiplexing"]
        JWTv["JWT Validation"]
        Rate["Rate Limiting"]
        LB["Load Balancing"]
        TLS["TLS Termination"]
    end

    subgraph Endpoints ["Endpoints & Routing"]
        API["/api/{service}/v1/*"]
        Auth["/auth/*"]
    end

    subgraph Backends ["Upstream Services"]
        MS["Ad Service"]
        CD["Casdoor (IAM)"]
    end

%% Применяем фичи к основному API
    HTTP2 --> API
    JWTv --> API
    Rate --> API
    TLS --> API
    LB --> API

%% Маршрутизация к конкретным сервисам
    API --> MS
    Auth --> CD

%% Фичи для Auth (обычно только TLS и HTTP2)
    TLS --> Auth
    HTTP2 --> Auth
```

## Database (MVP)

```mermaid
graph TB
    subgraph "PostgreSQL 15+ (Managed)"
        DB_AD["ads_db<br/>Объявления, компании (MVP)"]
%%        DB_COMM["comments_db"]
%%        DB_CHATS["chats_db"]
        DB_CD["casdoor_db<br/>Пользователи, сессии, токены"]
    end
```

## Authentication & Authorization

```mermaid
graph LR
    subgraph "Authentication"
        OAuth["OIDC"]
        JWT["JWT Tokens"]
    end
    
    subgraph "Authorization"
        RBAC["RBAC"]
        ABAC["ABAC"]
    end
    
    OAuth --> JWT
    JWT --> RBAC
    JWT --> ABAC
```

## Deployment

```mermaid
graph TD
    User([Пользователь]) --> ManagedGW

    subgraph "Y.Cloud Platform"
        ManagedGW["Y.Cloud Gateway<br/>(Envoy Managed)"]

        Containers["Serverless Containers<br/>(Ktor, Casdoor)"]
        ManagedDB[("Managed PostgreSQL<br/>(ads_db, casdoor_db)")]
        S3["Object Storage / CDN<br/>(Static Assets)"]
    end

%% Потоки трафика
    ManagedGW -->|API Requests| Containers
    ManagedGW -->|UI Assets| S3

%% Внутренние связи
    Containers --> ManagedDB
```

## External Services (MVP)

```mermaid
graph LR
    subgraph "External Providers"
        Email["Email Provider<br/>SMTP/API"]
    end
```

## Version Information (MVP)

```mermaid
graph LR
    subgraph "Versions"
        Kotlin["Kotlin (latest)"]
        Ktor["Ktor (latest)"]
        Compose["Compose Multiplatform (latest)"]
        Postgres["PostgreSQL (15+)"]
        Envoy["Envoy (Latest)"]
        Casdoor["Casdoor (Latest)"]
    end
```

---

*Document Version: 2.0 (MVP)*
*Created: 2026-03-26*
*Status: Ready for review*
