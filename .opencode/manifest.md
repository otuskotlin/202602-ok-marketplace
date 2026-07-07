# Архитектура сервиса

## Общие правила

- **Сервис = сборщик сущностей**
- **Сущность = полная инфраструктура** (common, biz, api, repo-*)
- **Репозитории — ВНУТРИ сущности** (не бывает общего repo на уровне сервиса)

## Структура сервиса

```
{имя-сервиса}/
├── .opencode/               # Скиллы (технические "рельсы")
│   └── skills/              # repo-pg.md, cor-biz.md, api-mappers.md, app-spring.md
├── app-ktor/               # Точки входа (на выбор)
├── app-spring/
├── app-kafka/
├── app-rabbit/
└── entities/
    ├── {сущность-1}/
    │   ├── .opencode/       # Контекст (бизнес-"мясо")
    │   ├── common/          # Domain: модели, Context, repo interface
    │   ├── biz/             # Application: COR процессор
    │   ├── api/             # API: мапперы
    │   ├── repo-pgsqlx4k/      # Data: реализация ТОЛЬКО для этой сущности
    │   ├── repo-inmemory/   # Data: реализация ТОЛЬКО для этой сущности
    │   └── app/             # Infrastructure: бины для этой сущности
    └── {сущность-2}/
```

## Правила для AI

**ПРЕЖДЕ ЧЕМ ПРЕДЛАГАТЬ ПРАВКИ:**
1. Определи тип файла (biz, repo, api, app)
2. Найди ближайший `.opencode/skills/` с соответствующим скиллом
3. Найди ближайший `.opencode/` на уровне сущности для бизнес-контекста
4. Скомбинируй: скилл + контекст

## Поддерживаемые варианты

- **app-ktor, app-spring, app-kafka, app-rabbit** — точки входа
- **repo-inmemory, repo-pgsqlx4k, repo-cassandra, repo-gremlin** — БД
- **api-v2-kmp, api-v1-jackson** — API