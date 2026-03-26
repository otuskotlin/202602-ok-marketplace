# Mermaid Diagrams

Стандартные диаграммы для архитектурной документации.

## Flowchart — компоненты и связи

```mermaid
flowchart LR
    A[Client] --> B[API Gateway]
    B --> C[Service]
    C --> D[(Database)]
```

## Sequence — последовательность операций

```mermaid
sequenceDiagram
    Client->>API: POST /order
    API->>Service: Process
    Service->>DB: Save
    DB-->>Service: OK
    Service-->>API: 201 Created
    API-->>Client: Response
```

## C4 Context — система в целом

```mermaid
C4Context
    Person(user, "Пользователь")
    System(app, "Приложение")
    SystemDb(db, "База данных")
    
    Rel(user, app, "Использует")
    Rel(app, db, "Хранит данные")
```

## Deployment — инфраструктура

```mermaid
deployment-beta
    docker-bridge network
    container "api" as api["API Service"]
    container "worker" as worker["Worker"]
    database "postgres" as db[(PostgreSQL)]
    
    api --> worker
    worker --> db
```

## Architecture — компоненты

```mermaid
architecture-beta
    group api[API Layer]
        service apiService[api-service] [[API Service]]
        service gateway[gateway] [[Gateway]]
    end
    
    group core[Core]
        service business[business-logic] [[Business Logic]]
        service repository[repository] [[Repository]]
    end
    
    group storage[Storage]
        database db[(Database)]
    end
    
    apiService --> business
    gateway --> apiService
    business --> repository
    repository --> db
```

## Чеклист

- [ ] Все связи подписаны
- [ ] Нет пересечений линий
- [ ] Компоненты одного уровня на одном уровне
- [ ] Цвета однотипных компонентов одинаковые