# ADR-004: Matching Algorithm — On-Demand Matching for MVP

## Context

Для MVP B2B маркетплейса требуется простой механизм мэтчинга между OFFER (продавец предлагает товар) и REQUEST (покупатель ищет товар).

### MVP Requirements

- **REQ-004**: On-demand Matching
  - Matching выполняется **при просмотре пользователем своих объявлений** (on-demand)
  - При открытии раздела "Мои объявления" система ищет релевантные совпадения
  - Результаты отображаются **прямо в карточке объявления** как релевантные матчинги
  - **Email-мэтчинг НЕ используется** (риск спама и бана)
  - Простой алгоритм: совпадение по категории + цена в диапазоне

### Non-Functional Requirements

- **Simple**: MVP — минимум complexity
- **On-demand**: запуск только при явном запросе пользователя
- **In-card display**: отображение матчей в карточке объявления, не в уведомлениях

## Comparison

| Критерий | On-Demand UI (MVP) | Email-Matching | Advanced Matching |
|----------|:------------------:|:--------------:|:-----------------:|
| **Сложность** | ✅ Простой | ⚠️ Средне | ❌ Сложно |
| **Время разработки** | ✅ 1-2 дня | ⚠️ 1-2 дня | ❌ Месяцы |
| **Спам-риск** | ✅ Нет | ❌ Высокий | ✅ Нет |
| **Точность** | ⚠️ Базовая | ⚠️ Базовая | 🟡 Высокая |
| **Ресурсы** | ✅ Минимум | ⚠️ Email infra | ❌ ML infrastructure |
| **MVP применимость** | ✅ Идеально | ❌ Бан/спам | ❌ Избыточно |

### On-Demand Matching Algorithm Details

**Вариант A: On-Demand (выбрано)**
- Запуск matching при просмотре пользователем раздела "Мои объявления"
- Результаты отображаются непосредственно в карточке объявления

**Вариант B: Event-Driven (альтернатива)**
- При создании OFFER публикуется событие
- Асинхронный matching с кешированием результатов

**Вариант C: Background Job (для масштаба)**
- Фоновая обработка через job scheduler
- Предварительное вычисление и хранение результатов

## Decision

**Выбрано:** Вариант A — On-demand matching при просмотре "Мои объявления"

## Rationale

1. **No spam risk**: Матчи отображаются в карточке объявления, email НЕ используется — никакого спама
2. **MVP-simplicity**: самый простой код, минимум сущностей
3. **Быстрая разработка**: 1-2 дня для реализации
4. **User control**: пользователь сам решает когда смотреть совпадения
5. **Database performance**: Базы данных обеспечат достаточную производительность для MVP нагрузки

### Почему НЕ email-matching

- **Спам-риск**: автоматические email = бан от почтовых провайдеров
- **Anti-spam compliance**: требует double opt-in, сложная compliance
- **User experience**: B2B пользователи ценят контроль над коммуникацией

### Почему НЕ AI/ML

- Избыточно для MVP
- Требует training data, infrastructure
- Базовый matching по категории+цене достаточен для B2B

## Consequences

### Positive

- **No spam**: Матчи отображаются в карточке объявления, email НЕ используется для matching
- **Simple**: минимум кода, легко поддерживать
- **Fast**: on-demand — только когда пользователь смотрит свои объявления
- **User control**: пользователь сам решает когда видеть совпадения
- **Cost-effective**: без email-инфраструктуры и anti-spam compliance

### Negative

- **Passive**: пользователь должен сам зайти в "Мои объявления"
- **Basic matching**: только category + price, без продвинутых критериев

### Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Slow matching with 10k+ records | Medium | Medium | Add pagination, cache results |
| Users don't discover feature | High | Medium | Prominent UI placement, onboarding |

### Scalability Path

| Phase | Approach |
|-------|----------|
| MVP (0-10k ads) | On-demand UI, single query, in-card display |
| Growth (10k-100k) | Background jobs + cache, batch processing, push notifications |
| Scale (100k+) | Async matching, sharding by category, push + email (opt-in) |

---

*Document Version: 1.0*
*Created: 2026-03-26*
*Status: Ready for review*
