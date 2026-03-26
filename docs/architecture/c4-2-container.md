# C4-2: Container Diagram — MVP Architecture

## Level 2: Container Diagram (MVP)

> **Status**: MVP-Optimized (5 Requirements)
> - REQ-001: Registration/Authentication
> - REQ-002: Create OFFER
> - REQ-003: Create REQUEST
> - REQ-004: Matching (On-Demand UI)
> - REQ-005: Catalog & Search

```mermaid
C4Container
    Person(user_seller, Seller, "Industrial components supplier")
    Person(user_buyer, Buyer, "Manufacturing company")
    
    Container_Boundary(frontend, "Frontend") {
        Container(compose_web, "Compose Web App", "Kotlin, Compose Multiplatform", "Web app: UI, Forms, Catalog")
    }
    
    Container_Boundary(gateway, "API Gateway") {
        Container(envoy, "Envoy Gateway", "Envoy Proxy", "Routing, JWT validation, HTTP/2")
    }
    
    Container_Boundary(services, "Backend Services (MVP)") {
        Container(marketplace_service, "Marketplace Service", "Kotlin, Ktor", "OFFER/REQUEST CRUD, matching")
        Container(casdoor, "Casdoor", "Go, OAuth2/OIDC", "Authentication, authorization, JWT")
    }
    
    Container_Boundary(data, "Data Layer (MVP)") {
        ContainerDb(postgres_marketplace, "PostgreSQL", "PostgreSQL 15+", "Single DB: users, offers, requests, matches")
        ContainerDb(postgres_casdoor, "PostgreSQL - Casdoor", "PostgreSQL 15+", "Users, organizations, sessions, tokens")
    }
    
    System_Ext(email_svc, "Email Provider", "SMTP/API")
    
    Rel(user_seller, compose_web, "Uses", "HTTPS")
    Rel(user_buyer, compose_web, "Uses", "HTTPS")
    
    Rel(compose_web, envoy, "API requests", "HTTPS/JWT, HTTP/2")
    Rel(compose_web, casdoor, "OAuth flow", "HTTPS")
    
    Rel(envoy, marketplace_service, "Proxies requests", "HTTP/2, JWT")
    Rel(envoy, casdoor, "JWT validation", "HTTP/2")
    
    Rel(marketplace_service, casdoor, "Authorization check (Casbin)", "HTTP")
    Rel(marketplace_service, email_svc, "Email notifications", "SMTP/API")
    
    Rel(marketplace_service, postgres_marketplace, "SQL queries", "JDBC")
    Rel(casdoor, postgres_casdoor, "SQL queries", "JDBC")
    
    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="2")
```

> **Note on HTTP/2**: Using HTTP/2 on Envoy Gateway enables multiplexing, header compression. WebSocket upgrades work over HTTP/2.

## Container Description

### Frontend Responsibilities (MVP)

```mermaid
graph TB
    subgraph "Frontend Container"
        UI["Compose Web App<br/>Kotlin, Compose Multiplatform"]
    end
    
    subgraph "Responsibilities"
        Forms["OFFER/REQUEST<br/>Forms"]
        Catalog["Product Catalog<br/>Search"]
        Profile["Company Profile<br/>Management"]
    end
    
    UI --> Forms
    UI --> Catalog
    UI --> Profile
```

### API Gateway Routes (MVP)

```mermaid
graph LR
    subgraph "Envoy Gateway"
        Entry["/api/v1/*"]
    end
    
    subgraph "Backend Services"
        MS["Marketplace Service<br/>/offers, /requests, /matches, /notifications"]
    end
    
    Entry --> MS
```

### Backend Services Responsibilities (MVP)

```mermaid
graph TB
    subgraph "Marketplace Service"
        MS["OFFER/REQUEST CRUD<br/>Matching Algorithm<br/>Email Notifications"]
    end
    
    subgraph "Casdoor"
        CD["Authentication<br/>JWT Tokens<br/>Organization<br/>User Management"]
    end
```

### Data Layer - Single Database (MVP)

```mermaid
graph TB
    subgraph "PostgreSQL 15+"
        DB1["marketplace_db<br/>offers, requests, matches, companies, notifications"]
    end
```

## Key Changes from Full Architecture

| Component | Full Architecture | MVP | Rationale |
|-----------|------------------|-----|------------|
| Services | 5 microservices | 2 services | Chat, Analytics not in MVP |
| Notification | Separate service | Integrated into Marketplace | On-demand UI matching (no email) |
| Databases | 5 logical DBs | 2 DBs (1 app + 1 casdoor) | Cost reduction |
| Chat | Included | Excluded | REQ not in MVP |
| Analytics | Included | Excluded | REQ not in MVP |

## OIDC Authentication Flow (Unchanged)

```mermaid
sequenceDiagram
    participant User as User
    participant Frontend as Compose Web
    participant Gateway as Envoy Gateway
    participant Casdoor as Casdoor (OIDC Provider)
    participant DB as PostgreSQL (Casdoor DB)
    
    Note over User, Gateway: 1. Initial Request (no token)
    User->>Frontend: 2. Open application
    Frontend->>Gateway: 3. Request /api/v1/offers
    Gateway->>Casdoor: 4. Validate JWT (introspect)
    Casdoor->>DB: 5. Check token validity
    DB-->>Casdoor: 6. Token not found/invalid
    Casdoor-->>Gateway: 7. 401 Unauthorized
    Gateway-->>Frontend: 8. 401 + redirect URI
    
    Note over User, Casdoor: 2. OIDC Authorization Code Flow
    Frontend->>Casdoor: 9. Authorization Request (redirect)
    Note over Casdoor: 10. Login form
    User->>Casdoor: 11. Credentials
    Casdoor->>DB: 12. Validate credentials
    DB-->>Casdoor: 13. User authenticated
    Casdoor-->>Frontend: 14. Authorization Code
    
    Note over Frontend, Gateway: 3. Token Exchange
    Frontend->>Casdoor: 15. Token Request
    Casdoor-->>Frontend: 16. Access Token + ID Token
    
    Note over User, Gateway: 4. API Access with Token
    Frontend->>Gateway: 17. Request /api/v1/offers + Access Token
    Gateway->>Casdoor: 18. Validate Token
    Casdoor-->>Gateway: 19. Token valid
    Gateway->>Frontend: 20. 200 OK + Data
```

## Sequence: Create OFFER with Matching

```mermaid
sequenceDiagram
    participant User as Seller
    participant Frontend as Compose Web
    participant Gateway as Envoy Gateway
    participant MS as Marketplace Service
    participant Casdoor as Casdoor
    participant DB as PostgreSQL
    participant Email as Email Provider
    
    User->>Frontend: 1. Fill OFFER form
    Frontend->>Gateway: 2. POST /api/v1/offers + JWT
    Gateway->>Casdoor: 3. Validate JWT
    Casdoor-->>Gateway: 4. OK, user_id=123
    Gateway->>MS: 5. POST /offers + user_id
    MS->>DB: 6. INSERT INTO offers
    DB-->>MS: 7. OFFER created
    MS->>MS: 8. Store OFFER (matching runs on-demand)
    MS-->>Gateway: 9. 201 Created
    Gateway-->>Frontend: 10. Response
    Frontend-->>User: 11. OFFER created

    Note over User, Frontend: Later: User views "My Listings" → matching runs
    Frontend->>Gateway: GET /api/v1/matching
    Gateway->>MS: Get matches for user
    MS->>DB: SELECT matching OFFERs/REQUESTs
    DB-->>MS: Matches
    MS-->>Gateway: 200 OK + matches
    Gateway-->>Frontend: Response
    Frontend-->>User: Show matches in listing card
```

## Deployment Diagram (Y.Cloud Serverless Containers)

```mermaid
graph TB
    subgraph "Y.Cloud Infrastructure"
        subgraph "Serverless Containers"
            F[("Frontend<br/>Compose Web<br/>Static Assets")]
            MS["Marketplace Service<br/>Kotlin/Ktor<br/>Serverless Container"]
            CD["Casdoor<br/>Go<br/>Serverless Container"]
        end
        
        subgraph "Managed Services"
            EG["(Envoy Gateway<br/>Y.Cloud Gateway<br/>HTTP/2, JWT")"]
            DB1["(PostgreSQL<br/>Marketplace DB)"]
            DB2["(PostgreSQL<br/>Casdoor DB)"]
        end
        
        subgraph "External Services"
            EP["Email Provider<br/>SMTP/API"]
        end
    end
    
    Client(("User<br/>Browser")) -->|HTTPS| F
    F -->|HTTP/2, JWT| EG
    EG -->|HTTP/2| MS
    EG -->|HTTP/2| CD
    
    MS -->|JDBC| DB1
    CD -->|JDBC| DB2
    
    MS -->|SMTP| EP
    
    style F fill:#90EE90
    style MS fill:#87CEEB
    style CD fill:#FFB6C1
    style EG fill:#DDA0DD
    style DB1 fill:#F0E68C
    style DB2 fill:#F0E68C
```

**Y.Cloud Serverless Deployment Notes (MVP):**
- Frontend: CDN + Static Hosting
- Marketplace Service: Serverless Container with auto-scaling
- Envoy Gateway: Managed Y.Cloud Gateway
- PostgreSQL: 2 managed DBs (marketplace + casdoor)

## Scalability (MVP)

```mermaid
graph LR
    subgraph "Frontend"
        CDN["CDN<br/>Static Assets"]
    end
    
    subgraph "Gateway"
        GW["Envoy Gateway<br/>Auto-scaling"]
    end
    
    subgraph "Services"
        MS["Marketplace Service<br/>Serverless"]
        CD["Casdoor<br/>Serverless"]
    end
    
    subgraph "Database"
        PG["PostgreSQL<br/>Managed"]
    end
    
    subgraph "External"
        Ext["Email<br/>Auto-scaling"]
    end
    
    CDN --> GW
    GW --> MS
    GW --> CD
    MS --> PG
    MS --> Ext
```

---

*Document Version: 2.0 (MVP)*
*Created: 2026-03-26*
*Status: Ready for review*
