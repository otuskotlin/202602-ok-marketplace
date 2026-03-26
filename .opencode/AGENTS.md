# Структура агентов OpenCode

## Общая архитектура

```
┌─────────────────────────────────────────────────────────────────┐
│                     HUMAN (USER)                                 │
│                  Decision Maker / Approver                       │
└─────────────────────────────────────────────────────────────────┘
                              ↑↓
                              ↓↑
                    ┌─────────────────┐
                    │   ORCHESTRATOR   │
                    │   (Координатор)   │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ↓                   ↓                   ↓
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    PO       │    │  EXECUTOR   │    │  REVIEWER   │
│ (Бизнес)    │    │ (Разработка) │    │ (Качество)  │
└─────────────┘    └─────────────┘    └─────────────┘
         │                                       │
         ↓                                       ↓
┌─────────────┐                         ┌─────────────┐
│ ARCHITECT   │                         │RELEASE-AGENT│
│ (Техника)   │                         │  (DevOps)   │
└─────────────┘                         └─────────────┘
```

---

## Агенты

### 1. Orchestrator
**Роль:** Координатор потока
**Инструменты:** task, read, glob, grep
**Ограничения:** НЕ пишет код

### 2. Product Owner
**Роль:** Бизнес-анализ
**Инструменты:** read, write, edit, websearch, codesearch
**Выход:** Vision, ЦА, CJM, монетизация
**Запреты:** ❌ Код, ❌ Технологии, ❌ БД

### 3. Architect
**Роль:** Техническое проектирование
**Инструменты:** read, write, edit, websearch, codesearch
**Вход:** Выход Product Owner
**Выход:** C4, ERD, ADR, API контракты
**Запреты:** ❌ Исполняемый код

### 4. Executor
**Роль:** Разработка (TDD)
**Инструменты:** read, write, edit, bash
**Вход:** Gate 1 (утверждённый план)
**Выход:** Код + Тесты
**Цикл:** RED → GREEN → REFACTOR

### 5. Reviewer
**Роль:** Quality Gate
**Инструменты:** read, write, edit, bash, websearch
**Вход:** Выход Executor
**Выход:** Quality Report, Verdict

### 6. Release Agent
**Роль:** DevOps
**Инструменты:** read, write, edit, bash
**Вход:** Gate 3 approval
**Выход:** CI/CD, Deploy

---

## Gate'ы

| Gate | Когда | Что видит пользователь | Действие |
|------|-------|----------------------|----------|
| Gate 1 | После PO + Architect | Vision, C4, ERD, ADR | Approve/Reject |
| Gate 2 | После Executor | Green Build, Coverage, Change Log | Approve/Reject |
| Gate 3 | После Reviewer | Quality Report | Approve → Deploy |

---

## Rollback

```
Gate 2 Reject:
├─ "Бизнес-план" → product-owner
├─ "Техника" → architect
└─ "Реализация" → executor

Gate 3 Reject:
└─ К нужному этапу
```

---

## Запрещённые действия

| Агент | Нельзя |
|-------|--------|
| Orchestrator | Писать код |
| Product Owner | Технологии, БД, код |
| Architect | Исполняемый код |
| Executor | Фичи вне тестов до RED |
| Release Agent | Деплоить до Gate 3 |

---

## Правила

1. **PO → Architect → Executor** — порядок обязателен
2. **Gate 1 до Executor** — без аппрува не начинаем
3. **Blocking Gates** — жди решения
4. **Mermaid Only** — диаграммы
