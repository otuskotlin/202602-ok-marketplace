# ERD: Entity Relationship Diagram — MVP Database

## Context

Database schema для MVP B2B маркетплейса (сервис объявлений).

### MVP Tables

| Table | Purpose |
|-------|---------|
| **companies** | Компании (продавцы/покупатели) |
| **users** | Пользователи (сотрудники компаний) |
| **categories** | Категории товаров |
| **ads** | Объявления |

> **Note**: Casdoor хранит своих users, organizations в отдельной БД. Здесь — только marketplace-сущности.

---

## ERD (Mermaid)

```mermaid
erDiagram
    %% Companies and Users
    company ||--o{ user : "employees"
    company ||--o{ ad : "publishes"
    
    %% Categories
    category ||--o{ ad : "has"
    category ||--o{ category : "parent"
    
    %% Company relationships
    company {
        uuid id PK
        string name
        string inn "INN/ЕГРПОУ"
        string address
        string email "Contact email"
        float rating
        uuid user_id FK "Owner user"
        timestamp created_at
        timestamp updated_at
    }
    
    user {
        uuid id PK
        uuid company_id FK
        string email "Casdoor user email"
        string name "Full name"
        string role "admin, manager, user"
        timestamp created_at
    }
    
    category {
        uuid id PK
        uuid parent_id FK "Self-reference for hierarchy"
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
        string article "Vendor/Manufacturer article"
        decimal price "Price per unit"
        string unit "pcs, kg, meter, etc."
        int quantity "Available quantity"
        jsonb specifications "Dynamic attrs: size, weight, voltage"
        string status "draft, active, inactive, sold"
        timestamp created_at
        timestamp updated_at
    }
```

---

## Table Definitions

### companies

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| name | VARCHAR(255) | NOT NULL |
| inn | VARCHAR(50) | UNIQUE, NOT NULL |
| address | TEXT | |
| email | VARCHAR(255) | NOT NULL |
| rating | DECIMAL(3,2) | DEFAULT 0.00 |
| user_id | UUID | REFERENCES users(id) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**Indexes:** idx_companies_inn, idx_companies_email

### users

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| company_id | UUID | REFERENCES companies(id) ON DELETE CASCADE |
| email | VARCHAR(255) | NOT NULL |
| name | VARCHAR(255) | |
| role | VARCHAR(50) | DEFAULT 'user' |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**Indexes:** idx_users_email (UNIQUE), idx_users_company

### categories

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| parent_id | UUID | REFERENCES categories(id) |
| name | VARCHAR(255) | NOT NULL |
| slug | VARCHAR(100) | UNIQUE, NOT NULL |
| sort_order | INT | DEFAULT 0 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**Indexes:** idx_categories_parent, idx_categories_slug

### ads

| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| company_id | UUID | REFERENCES companies(id) ON DELETE CASCADE |
| category_id | UUID | REFERENCES categories(id) |
| title | VARCHAR(500) | NOT NULL |
| description | TEXT | |
| article | VARCHAR(100) | |
| price | DECIMAL(15,2) | NOT NULL |
| unit | VARCHAR(20) | DEFAULT 'pcs' |
| quantity | INT | DEFAULT 0 |
| specifications | JSONB | DEFAULT '{}' |
| status | VARCHAR(20) | DEFAULT 'draft' |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**Indexes:** idx_ads_company, idx_ads_category, idx_ads_status

**Special indexes:**
- Full-text search: GIN(to_tsvector('russian', title || ' ' || description))
- JSONB: GIN(specifications)

---

## Relationships

```mermaid
graph TB
    subgraph "MVP Data Flow"
        U[users<br/>- id<br/>- company_id<br/>- email] --> C[companies<br/>- id<br/>- user_id<br/>- name<br/>- inn]
        C --> A[ads<br/>- company_id<br/>- category_id<br/>- price]
    end
```

---

## Sample Data

### Categories (Seed)

**Electronics:**
- Electronics (slug: electronics, sort: 1)
- Sensors (slug: sensors, sort: 2)
- Motors (slug: motors, sort: 3)
- Automation (slug: automation, sort: 4)

**Industrial:**
- Industrial Equipment (slug: industrial, sort: 10)
- Tools (slug: tools, sort: 11)

---

## Evolution Path

| Phase | Tables | Notes |
|-------|--------|-------|
| MVP | 4 tables | Current |
| v1.1 | +ratings | Add ratings/reviews |
| v1.2 | +messages | Add chat (NOT in MVP) |
| v1.3 | +analytics_events | Add analytics (NOT in MVP) |

---

*Document Version: 2.0*
*Created: 2026-03-26*
*Status: Ready for review*
*Changes: Убраны таблицы requests, matches. Таблица offers переименована в ads.*