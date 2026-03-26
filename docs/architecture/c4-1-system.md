# C4-1: System Context Diagram — Контекст системы

## Уровень 1: System Context Diagram

```mermaid
C4Context
    Person(user_seller, "Seller", "Industrial components supplier: motor manufacturers, electronics distributors, specialized vendors")
    Person(user_buyer, "Buyer", "Manufacturing company, integrator, repair service, small production")
    
    System_Boundary(marketplace, "OK Marketplace System") {
        System(marketplace_app, "Marketplace", "B2B platform for industrial components trading", "Enables sellers to create OFFER, buyers to create REQUEST, performs intelligent matching")
    }
    
    System_Ext(casdoor, "Casdoor", "Authentication and authorization provider", "OAuth 2.0 / OIDC, user management, organizations, JWT tokens")
    System_Ext(email_provider, "Email Provider", "Email service", "Email delivery for account, OTP, deal notifications (NOT for matching - in-card display)")
    System_Ext(sms_provider, "SMS Provider", "SMS gateway", "OTP confirmation, critical notifications")
    
    Rel(user_seller, marketplace_app, "Registers, creates OFFER, receives matches, chats, confirms deals")
    Rel(user_buyer, marketplace_app, "Registers, creates REQUEST, selects OFFER, chats, pays")
    
    Rel(marketplace_app, casdoor, "Authentication: OAuth 2.0, JWT token validation", "HTTPS")
    Rel(marketplace_app, email_provider, "Email notifications", "SMTP/API")
    Rel(marketplace_app, sms_provider, "SMS notifications, OTP", "API")
    
    UpdateRelStyle(user_seller, marketplace_app, $offsetX="-80", $offsetY="-20")
    UpdateRelStyle(user_buyer, marketplace_app, $offsetX="80", $offsetY="-20")
```

## Data Flows

### Flow 1: Registration and Authentication

```mermaid
sequenceDiagram
    participant User as Seller/Buyer
    participant Marketplace as Marketplace
    participant Casdoor as Casdoor
    participant DB as PostgreSQL
    
    User->>Marketplace: Open application
    Marketplace->>Casdoor: Redirect for auth
    Casdoor->>User: Login form
    User->>Casdoor: Credentials (email/password, social, SSO)
    Casdoor->>DB: Validate credentials
    DB-->>Casdoor: User authenticated
    Casdoor-->>Marketplace: JWT token
    Marketplace-->>User: Access granted
```

### Flow 2: Create OFFER (Seller)

```mermaid
sequenceDiagram
    participant Seller
    participant Marketplace
    participant Casdoor as Casdoor
    participant DB as PostgreSQL
    
    Seller->>Marketplace: Fill product card
    Marketplace->>Casdoor: Validate JWT token
    Casdoor-->>Marketplace: Token valid
    Marketplace->>DB: Save OFFER to PostgreSQL
    DB-->>Marketplace: OFFER created
    Marketplace->>Marketplace: Trigger matching algorithm
```

### Flow 3: Create REQUEST (Buyer)

```mermaid
sequenceDiagram
    participant Buyer
    participant Marketplace
    participant Casdoor as Casdoor
    participant Matcher as Matching Algorithm
    
    Buyer->>Marketplace: Create product request
    Marketplace->>Casdoor: Validate token
    Casdoor-->>Marketplace: Token valid
    Marketplace->>Matcher: Run matching algorithm
    Matcher-->>Marketplace: Relevant OFFERs found
    Marketplace-->>Buyer: Display matching offers
```

### Flow 4: Notifications

```mermaid
sequenceDiagram
    participant Marketplace
    participant Email as Email Provider
    participant SMS as SMS Provider
    participant User as User
    
    Marketplace->>Email: New match notification
    Email-->>User: Email sent
    Marketplace->>SMS: OTP confirmation
    SMS-->>User: SMS sent
    Marketplace->>Email: Deal status change
    Email-->>User: Email sent
```

## System Boundaries

```mermaid
graph TB
    subgraph "Inside Boundaries (Team Responsibility)"
        Frontend["Frontend<br/>Compose Multiplatform"]
        AdService["Ad Service"]
        ChatService["Chat Service"]
        NotifService["Notification Service"]
        AnalyticsService["Analytics Service"]
        DB["PostgreSQL<br/>5 Logical DBs"]
    end
    
    subgraph "Outside Boundaries (External Dependencies)"
        Casdoor["Casdoor<br/>Auth Service"]
        EmailProvider["Email Provider"]
        SMSProvider["SMS Provider"]
    end
    
    Frontend --> AdService
    Frontend --> ChatService
    AdService --> Casdoor
    ChatService --> Casdoor
    NotifService --> EmailProvider
    NotifService --> SMSProvider
```

## Technology Stack at System Level

```mermaid
graph LR
    subgraph "Frontend"
        FE["Kotlin<br/>Compose Multiplatform Web"]
    end
    
    subgraph "API Gateway"
        GW["Envoy Gateway<br/>Y.Cloud"]
    end
    
    subgraph "Backend"
        BS["Kotlin<br/>Ktor Microservices"]
    end
    
    subgraph "SgAuth"
        Auth["Casdoor<br/>Go, OAuth2/OIDC"]
    end
    
    subgraph "Database"
        DB["PostgreSQL 15+<br/>5 Logical DBs"]
    end
    
    subgraph "Authorization"
        Perm["Casbin<br/>RBAC/ABAC"]
    end
    
    FE --> GW
    GW --> BS
    BS --> Auth
    BS --> DB
    BS --> Perm
```

## Security Architecture

```mermaid
graph TB
    subgraph "Client"
        Browser["Browser<br/>HTTPS"]
    end
    
    subgraph "Security Layer"
        TLS["TLS 1.3"]
        JWT["JWT Token<br/>Short lifetime"]
    end
    
    subgraph "Authentication"
        Casdoor["Casdoor<br/>OAuth 2.0 / OIDC"]
    end
    
    subgraph "Authorization"
        Casbin["Casbin<br/>RBAC"]
    end
    
    subgraph "Data Isolation"
        OrgIso["Organization<br/>Isolation"]
    end
    
    Browser --> TLS
    TLS --> JWT
    JWT --> Casdoor
    Casdoor --> Casbin
    Casbin --> OrgIso
```

---

*Document Version: 1.0*
*Created: 2026-03-24*
*Status: Ready for review*
