# ADR-002: Выбор базы данных для MVP B2B маркетплейса

## Context

Implements [REQ-NNN](../requirements/REQ-NNN.md).

Для MVP B2B маркетплейса требуется база данных, обеспечивающая:

- **ACID**: транзакционная целостность критична для финансовых операций (orders, payments)
- **Гибкие схемы**: Ads имеют varying specifications (атрибуты товаров различаются по категориям)
- **Serverless совместимость**: развёртывание в Y.Cloud Serverless (Cloud Functions / Container)
- **Kotlin совместимость**: драйверы, ORM, reactive support
- **Совместимость с Casdoor**: B2B маркетплейс требует авторизации через Casdoor, который хранит свои данные
- **Стоимость**: минимазация OPEX для MVP

### Требования Casdoor к БД

Casdoor поддерживает следующие СУБД для хранения своих данных:
- **MySQL** (5.7+) — рекомендуется
- **PostgreSQL** (10+)

Casdoor НЕ поддерживает:
- YDB
- CockroachDB
- SQLite

Это критичный критерий: выбранная БД должна быть совместима с Casdoor.

## Comparison

Comparing candidates against B2B критериям:

| Критерий | PostgreSQL | MySQL | CockroachDB | YDB |
|----------|:----------:|:-----:|:-----------:|:---:|
| **ACID (транзакции)** | ✅ Полная поддержка | ✅ ACID (InnoDB) | ✅ Distributed ACID | ✅ Distributed ACID |
| **Гибкие схемы** | ⚠️ ALTER TABLE / JSON | ⚠️ Limited JSON | ⚠️ Schema changes | ✅ JSON, YQL |
| **Serverless (Y.Cloud)** | 🟡 Managed PostgreSQL | 🟡 Managed MySQL | ❌ Не support | ✅ Serverless native |
| **Kotlin драйверы** | ✅ Exposed, Ktor, Spring | ⚠️ MySQL Connector/J | ⚠️ pgJDBC (experimental) | ⚠️ YDB SDK (limited Kotlin) |
| **Совместимость с Casdoor** | ✅ Поддерживается | ✅ Поддерживается | ❌ Не поддерживается | ❌ Не поддерживается |
| **Стоимость (MVP)** | 🟡 ~$50-100/мес | 🟡 ~$30-80/мес | 🟡 ~$100+/мес | 🟢 Pay-per-request |

### Оценка по сценариям B2B

| Сценарий | PostgreSQL | MySQL | CockroachDB | YDB |
|----------|:----------:|:-----:|:-----------:|:---:|
| **Financial transactions** | ✅ | ✅ | ✅ | ✅ |
| **Product catalog (varying attrs)** | ✅ | ⚠️ | ⚠️ | 🟡 |
| **Search & filtering** | ✅ | ⚠️ | ⚠️ | 🟡 |
| **Serverless deployment** | ⚠️ | ⚠️ | ❌ | ✅ |
| **Kotlin developer experience** | ✅ | ⚠️ | ⚠️ | ⚠️ |
| **Casdoor compatibility** | ✅ | ✅ | ❌ | ❌ |
| **Cost optimization MVP** | 🟡 | 🟡 | 🟡 | ✅ |

## Decision

**Выбрано:** PostgreSQL

## Rationale

Почему выбран этот вариант. Ссылка на сценарий из REQ:

1. **ACID для B2B транзакций:**
   - MVCC обеспечивает изоляцию транзакций без блокировок reads
   - Критично для financial operations (orders, payments, invoices)
   - Доказуемая consistency для аудита

2. **Совместимость с Casdoor:**
   - PostgreSQL официально поддерживается Casdoor
   - Это required criteria для B2B authentication
   - MySQL также поддерживается, но PostgreSQL предпочтительнее для Kotlin экосистемы

3. **Гибкие схемы для Ads specs:**
   - JSON-поля позволяют хранить varying specifications без ALTER TABLE
   - Индексы обеспечивают эффективные запросы по атрибутам
   - 80/20 pattern: frequently queried fields → columns, rest → JSON

4. **Kotlin Ecosystem:**
   - Exposed (Kotlin-first ORM от JetBrains) — типобезопасный DSL
   - Ktor SQL plugin для reactive queries
   - Spring Data R2DBC для reactive stack
   - Лучший developer experience для Kotlin/JVM

5. **Y.Cloud Compatible:**
   - Managed PostgreSQL доступен в Y.Cloud
   - Stateful-сервис с container-based deployment
   - Для MVP: минимальный cost при стабильном performance
   - Future: возможность миграции на YDB при scale

6. **Cost/Performance Balance:**
   - Для MVP: ~$50-70/мес (managed PostgreSQL)
   - Индексы обеспечивают производительность без extra infrastructure
   - Современные версии PostgreSQL имеют улучшения для JSON

7. **Alternative consideration:**
   - YDB был бы идеален для pure serverless, но НЕ совместим с Casdoor
   - CockroachDB также НЕ совместим с Casdoor
   - MySQL совместим, но PostgreSQL предлагает лучший Kotlin developer experience
   - PostgreSQL обеспечивает баланс гибкости, ACID, Casdoor-совместимости и developer experience

## Consequences

### Positive
- Casdoor-совместимость из коробки (required для B2B auth)
- Типобезопасный Kotlin-опыт (Exposed DSL)
- Гибкость схемы без миграций для evolving specs
- Сильная экосистема инструментов (pgAdmin, DBeaver, DataGrip)
- Проверенная технология (30+ лет в production)
- ACID compliance для финансовых операций
- Отличная документация и community

### Negative
- Managed PostgreSQL в Y.Cloud не true serverless (нужен always-on инстанс)
- JSON-поля требуют понимания стратегий индексации
- Стоимость выше чем YDB (но приемлемо для MVP)

### Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Cold start в serverless контейнере | Medium | Medium | Keep-alive strategies, connection pooling |
| JSON query performance без индексов | Medium | High | Создавать индексы на этапе разработки |
| Schema drift (JSON fields uncontrolled) | Low | Medium | JSON Schema validation + documentation |
| Cost при scale (100k+ requests/day) | Low | Medium | YDB миграция когда понадобится |

### Alternatives Considered

Если в будущем понадобится полный serverless:
- **YDB**: для Serverless-native deployment с pay-per-request (но Casdoor требует отдельный PostgreSQL/MySQL)
- **CockroachDB**: для distributed ACID (но Casdoor не поддерживает)

Для B2B с Casdoor-авторизацией, PostgreSQL — единственный выбор, обеспечивающий совместимость с Casdoor и лучший Kotlin developer experience.

(End of file - total 210 lines)
