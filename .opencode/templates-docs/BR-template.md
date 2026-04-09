# TARGET DIRECTORY: docs/business/BR/
# TARGET FILENAME: BR-[NNN]-[short_feature_name].md (e.g., BR-001-create-ad.md)
# NEXT NUMBER: Use sequential NNN from glob(\"docs/business/BR/BR-*.md\" | wc -l +1)

# BR-[NNN]: [Название фичи]

## Business Value
[Одна строка: какую проблему решает, какую выгоду приносит]

## User Scenarios (BDD Style, multiple for each BR for each role)

### BR-[NNN]-U[MMM]
* **As:** {{Role}}
* **I want:** {{objective}}
* **In order to:** {{reason}}

#### Acceptance Criteria

* BR-[NNN]-U[MMM]-C[KKK] - {{comment}}
  * **Given:** [Контекст/начальное состояние]
  * **When:** [Действие пользователя или системы]
  * **Then:** [Ожидаемый результат]

## System Constraints

| Constraint | Value | Notes |
|-----------|-------|-------|
| Latency | < N ms | |
| Throughput | N req/sec | |
| Availability | N% | |

## Out of Scope
- [Что НЕ входит в это требование]
