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
        CD["Casdoor<br/>Go/OAuth2/OIDC"]
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
        MS["Marketplace Service<br/>Kotlin/Ktor"]
        PG["PostgreSQL 15+"]
        CD["Casdoor<br/>OAuth 2.0/OIDC"]
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
    MS --> CD
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
    subgraph "Marketplace Service"
        MS["Ktor<br/>Exposed ORM<br/>Casbin RBAC<br/>Matching Engine<br/>Email Notifications"]
    end
    
    subgraph "Casdoor"
        CD["Go<br/>OAuth 2.0/OIDC<br/>JWT<br/>User Management"]
    end
```

## API Gateway

```mermaid
graph LR
    subgraph "Envoy Gateway Features"
        HTTP2["HTTP/2<br/>Multiplexing"]
        JWTv["JWT Validation"]
        Rate["Rate Limiting"]
        LB["Load Balancing"]
        TLS["TLS Termination"]
    end
    
    subgraph "Endpoints"
        API["/api/v1/* → Services"]
        Auth["/auth/* → Casdoor"]
    end
    
    HTTP2 --> API
    JWTv --> API
    Rate --> API
    TLS --> API
    Auth --> Auth
```

## Database (MVP)

```mermaid
graph TB
    subgraph "PostgreSQL 15+ (Managed)"
        DB1["marketplace_db<br/>offers, requests, matches, companies, notifications"]
        DB2["casdoor_db<br/>users, organizations, sessions, tokens"]
    end
```

## Authentication & Authorization

```mermaid
graph LR
    subgraph "Authentication"
        OAuth["OAuth 2.0 / OIDC"]
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
graph TB
    subgraph "Y.Cloud Platform"
        CDN["CDN<br/>Static Assets"]
        Containers["Serverless Containers<br/>Auto-scaling"]
        ManagedGW["Y.Cloud Gateway<br/>HTTP/2, JWT"]
        ManagedDB["Managed PostgreSQL"]
    end
    
    CDN --> ManagedGW
    Containers --> ManagedGW
    Containers --> ManagedDB
```

## External Services (MVP)

```mermaid
graph LR
    subgraph "External Providers"
        Email["Email Provider<br/>SMTP/API"]
        IdP["Casdoor (managed)"]
    end
```

## Version Information (MVP)

```mermaid
graph LR
    subgraph "Versions"
        Kotlin["Kotlin 1.9.x"]
        Ktor["Ktor 2.3.x"]
        Compose["Compose Multiplatform 1.5.x"]
        Postgres["PostgreSQL 15+"]
        Envoy["Envoy (Latest)"]
        Casdoor["Casdoor (Latest)"]
    end
```

---

*Document Version: 2.0 (MVP)*
*Created: 2026-03-26*
*Status: Ready for review*
