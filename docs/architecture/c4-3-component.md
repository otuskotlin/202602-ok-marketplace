# C4-3: Component Diagram — MVP Architecture

## Level 3: Component Diagrams (MVP)

> **Note on Architecture**: This is MVP-Optimized. Single AD Service for ads management.

### 3.0 Micro-frontends Architecture (MVP)

```mermaid
graph TB
    subgraph "Frontend Composition Layer"
        SH[("Shell App<br/>Compose Multiplatform<br/>Navigation, Layout")]
    end
    
    subgraph "Micro-frontends (MVP - Lazy Loaded)"
        MF1[("Auth Microfrontend<br/>Login, Register")]
        MF2[("Ad Microfrontend<br/>Ads CRUD, Search")]
    end
    
    subgraph "Backend Services"
        MS["AD Service"]
        CD["Casdoor"]
    end
    
    SH -->|Lazy Load| MF1
    SH -->|Lazy Load| MF2
    
    MF1 -->|OAuth| CD
    MF2 -->|REST API| MS
    
    style SH fill:#DDA0DD
    style MF1 fill:#90EE90
    style MF2 fill:#90EE90
    style MS fill:#87CEEB
    style CD fill:#FFB6C1
```

### Micro-frontends Mapping (MVP)

```mermaid
graph LR
    subgraph "Micro-frontends"
        AuthMFE["Auth MFE<br/>Login, Register, Password Recovery"]
        AdMFE["Ad MFE<br/>Ad CRUD, Search, Filters"]
    end
    
    subgraph "Backend Services"
        CD["Casdoor"]
        MS["AD Service"]
    end
    
    AuthMFE --> CD
    AdMFE --> MS
```

---

### 3.1 Frontend (Shell + Micro-frontends)

```mermaid
C4Component
    ContainerDb(backend, "Backend Services", "Ktor", "API")
    
    Container_Boundary(shell, "Shell App") {
        Component(shell_router, "Shell Router", "Kotlin", "Navigation, lazy loading")
        Component(shell_layout, "Shell Layout", "Kotlin", "Header, sidebar, footer")
        Component(shell_auth, "Auth Integration", "Kotlin", "JWT handling, session")
    }
    
    Container_Boundary(mf_auth, "Auth Microfrontend") {
        Component(auth_theme, "Auth Theme", "Kotlin", "Auth theming")
        Component(auth_screens, "Auth Screens", "Kotlin", "Login, Register")
        Component(auth_oauth, "OAuth Handler", "Kotlin", "Social login")
    }
    
    Container_Boundary(mf_ad, "Ad Microfrontend (MVP)") {
        Component(ad_theme, "Ad Theme", "Kotlin", "Ad theming")
        Component(ad_screens, "Ad Screens", "Kotlin", "Create, List, Detail")
        Component(ad_catalog, "Catalog & Search", "Kotlin", "Search, Filters, Categories")
    }
    
    Container_Boundary(shared, "Shared Libraries") {
        Component(common_api, "API Client", "Kotlin", "HTTP client, JWT")
        Component(common_state, "State Management", "Kotlin", "ViewModel, UI State")
        Component(common_validation, "Form Validation", "Kotlin", "Input validation")
        Component(common_ui, "UI Components", "Kotlin", "Buttons, Inputs, Cards")
    }
    
    Rel(shell_router, shell_layout, "Layout")
    Rel(shell_router, shell_auth, "Auth")
    Rel(shell_auth, common_api, "API calls")
    
    Rel(shell_router, auth_screens, "Route")
    Rel(auth_screens, common_api, "API calls")
    Rel(auth_screens, common_state, "State")
    
    Rel(shell_router, ad_screens, "Route")
    Rel(shell_router, ad_catalog, "Route")
    Rel(ad_screens, common_api, "API calls")
    Rel(ad_screens, common_state, "State")
    
    Rel(common_api, backend, "REST")
```

---

### 3.2 AD Service (MVP)

```mermaid
C4Component
    ContainerDb(db_ad, "PostgreSQL - AD DB", "JDBC", "SQL")
    Container(ext_auth, "Casdoor", "HTTP", "Auth")
    Container(ext_email, "Email Provider", "SMTP", "Email")
    
    %% Controllers
    Component(controller_ad, "Ad Controller", "Kotlin/Ktor", "REST: CRUD ads")
    Component(controller_category, "Category Controller", "Kotlin/Ktor", "Categories")
    Component(controller_company, "Company Controller", "Kotlin/Ktor", "Company management")
    
    %% Services
    Component(service_ad, "Ad Service", "Kotlin", "Business logic: ads")
    Component(service_company, "Company Service", "Kotlin", "Company management")
    Component(service_notification, "Notification Service", "Kotlin", "Email dispatch")
    
    %% Repositories
    Component(repository_ad, "Ad Repository", "Kotlin/Exposed", "Data: ads")
    Component(repository_company, "Company Repository", "Kotlin/Exposed", "Data: companies")
    Component(repository_category, "Category Repository", "Kotlin/Exposed", "Data: categories")
    
    %% Infrastructure
    Component(casbin_enforcer, "Casbin Enforcer", "Kotlin/Casbin", "Authorization")
    Component(jwt_provider, "JWT Provider", "Kotlin", "Token parsing")
    Component(email_sender, "Email Sender", "Kotlin", "SMTP sending")
    
    %% Controller -> Service
    Rel(controller_ad, service_ad, "Uses")
    Rel(controller_company, service_company, "Uses")
    Rel(controller_category, repository_category, "Uses")
    
    %% Service -> Repository
    Rel(service_ad, repository_ad, "Data access")
    Rel(service_company, repository_company, "Data access")
    
    %% Service -> Auth
    Rel(service_ad, casbin_enforcer, "Check permissions")
    Rel(service_company, casbin_enforcer, "Check permissions")
    
    %% JWT
    Rel(service_ad, jwt_provider, "Parse JWT")
    
    %% Database
    Rel(repository_ad, db_ad, "SQL")
    Rel(repository_company, db_ad, "SQL")
    Rel(repository_category, db_ad, "SQL")
    
    %% External
    Rel(email_sender, ext_email, "SMTP")
```

---

### 3.3 Casdoor (Unchanged)

```mermaid
C4Component
    ContainerDb(db_casdoor, "PostgreSQL - Casdoor DB", "JDBC", "casdoor DB")
    
    Component(handler_oauth, "OAuth Handler", "Go", "OAuth 2.0 / OIDC")
    Component(handler_login, "Login Handler", "Go", "Password, MFA")
    Component(handler_token, "Token Handler", "Go", "JWT generation")
    Component(handler_org, "Organization Handler", "Go", "Multi-tenant")
    Component(handler_user, "User Handler", "Go", "User CRUD")
    Component(handler_session, "Session Handler", "Go", "Session")
    
    Component(provider_password, "Password Provider", "Go", "Email/password")
    Component(provider_social, "Social Providers", "Go", "Google, GitHub")
    
    Component(store_user, "User Store", "Go", "User data")
    Component(store_session, "Session Store", "Go", "Session data")
    Component(store_token, "Token Store", "Go", "Token data")
    
    Rel(handler_oauth, handler_login, "Redirects")
    Rel(handler_oauth, handler_token, "Issues tokens")
    Rel(handler_login, provider_password, "Authenticates")
    Rel(handler_login, provider_social, "Social login")
    
    Rel(handler_token, store_token, "Store/retrieve")
    Rel(handler_user, store_user, "User CRUD")
    
    Rel(store_user, db_casdoor, "SQL")
    Rel(store_session, db_casdoor, "SQL")
    Rel(store_token, db_casdoor, "SQL")
```

---

## Authorization Flow (Unchanged)

```mermaid
sequenceDiagram
    participant Client as Frontend
    participant Gateway as Envoy
    participant Controller as Ad Controller
    participant Service as Ad Service
    participant Casbin as Casbin Enforcer
    participant Repo as Ad Repository
    participant DB as PostgreSQL
    
    Client->>Gateway: POST /api/v1/ads (JWT)
    Gateway->>Controller: Forward + user_id from JWT
    Controller->>Service: createAd(dto, user_id)
    Service->>Casbin: enforcer.enforce(user_id, "ad", "write")
    Casbin-->>Service: true/false
    alt Разрешено
        Service->>Repo: insert(ad)
        Repo->>DB: INSERT INTO ads
        DB-->>Repo: ID
        Repo-->>Service: ad
        Service-->>Controller: created
        Controller-->>Gateway: 201 Created
        Gateway-->>Client: OK
    else Запрещено
        Service-->>Controller: Forbidden
        Controller-->>Gateway: 403 Forbidden
        Gateway-->>Client: 403
    end
```

## Database Schema (MVP)

```mermaid
erDiagram
    company ||--o{ user : "employees"
    company ||--o{ ad : "publishes"
    
    category ||--o{ ad : "has"
    
    company {
        uuid id PK
        string name
        string inn
        string address
        string email
        float rating
        uuid user_id FK
        timestamp created_at
        timestamp updated_at
    }
    
    user {
        uuid id PK
        uuid company_id FK
        string email
        string name
        string role
        timestamp created_at
    }
    
    category {
        uuid id PK
        uuid parent_id FK
        string name
        string slug
        int sort_order
        timestamp created_at
    }
    
    ad {
        uuid id PK
        uuid company_id FK
        uuid category_id FK
        string title
        string description
        string article
        decimal price
        string unit
        int quantity
        jsonb specifications
        string status
        timestamp created_at
        timestamp updated_at
    }
```

---

## What's NOT in MVP

| Component | Full Architecture | MVP | Notes |
|-----------|------------------|-----|-------|
| Chat Service | ✅ | ❌ | Chat not in MVP |
| Chat MFE | ✅ | ❌ | Chat UI not in MVP |
| Analytics Service | ✅ | ❌ | Stats not in MVP |
| Analytics MFE | ✅ | ❌ | Stats UI not in MVP |
| Notification Service | Separate | Integrated | Merged into AD Service |
| WebSocket Handler | ✅ | ❌ | Real-time not needed |

---

*Document Version: 3.0 (MVP)*
*Created: 2026-03-26*
*Status: Ready for review*
*Changes: Убраны Offer, Request, Match контроллеры/сервисы/репозитории. Оставлен только Ad.*