# ADR-003: Выбор UI технологии для MVP B2B Маркетплейса

## Status: ACCEPTED

## Context

Для MVP B2B маркетплейса требуется Frontend решение, обеспечивающее:
- Единый язык с backend (Kotlin) — возможность шаринга кода и unified DSL
- Сложность разработки для MVP — минимальный time-to-market
- Serverless совместимость — развёртывание в Y.Cloud Serverless
- Доступность разработчиков на рынке — найм в 2026
- B2B компоненты — Forms, Tables, CRUD, Dashboards

---

## Полный анализ: Compose vs React

### 1. Аргументы ПРОТИВ Compose Multiplatform

Несмотря на привлекательность единого стека, существуют объективные минусы:

| Минус | Детали |
|-------|--------|
| **Web в Beta** | Compose Multiplatform for Web официально находится в статусе Beta (не Stable). Это означает возможные API breaking changes, меньшую стабильность в production и риски для долгосрочной поддержки. |
| **Ограниченная экосистема enterprise компонентов** | В отличие от React, экосистема Compose значительно меньше. Для B2B часто требуются: сложные Data Grids (AG Grid, Handsontable), продвинутые Charts (Recharts, Nivo), сложные Forms (React Hook Form + Yup). Нативных Kotlin-альтернатив меньше. |
| **Сложность найма Kotlin/Web разработчиков** | Разработчиков с опытом Kotlin + Compose Web на рынке значительно меньше, чем React-специалистов. Это увеличивает cost hiring и время закрытия позиций. |
| **Нет нативной Serverless поддержки** | Хотя Ktor работает в serverless, это требует дополнительной настройки (GraalVM native image, custom runtime). React/Next.js имеет out-of-the-box support для Vercel, Y.Cloud и других serverless платформ. |

---

### 2. Аргументы ЗА React (технически лучше для проекта)

React является лидером рынка и предлагает объективные преимущества:

| Плюс | Детали |
|------|--------|
| **Лидер рынка — 42.6%** | По данным State of JS 2025, React занимает доминирующую позицию с 42.6% использования в production. Это означает: огромный пул кандидатов, легкий найм, большое community. |
| **Богатая экосистема B2B компонентов** | Ant Design, MUI (Material UI), Radix UI, shadcn/ui, AG Grid — десятки enterprise-ready библиотек для сложных интерфейсов. B2B компоненты (Data Tables, Forms, Dashboards) уже готовы. |
| **Serverless готовность** | Next.js + Vercel — стандарт де-факто для serverless. Легкое развёртывание в Y.Cloud Functions, AWS Lambda, Vercel, Netlify. SSR/SSG из коробки. |
| **Готовые enterprise решения** | Enterprise паттерны: State Management (Redux Toolkit, Zustand), Testing (Jest, Playwright), Forms (React Hook Form), Auth (NextAuth), — всё mature и проверено. |

---

### 3. Аргументы ЗА Compose (стратегически лучше для курса)

Несмотря на технические минусы, Compose выбран по стратегическим причинам образовательной программы:

| Плюс | Детали |
|------|--------|
| **Kotlin курс — единый стек everywhere** | Backend (Ktor) + Frontend (Compose Web) + Mobile (KMP) = один язык. Студенты изучают Kotlin everywhere — это ключевая ценность курса "Kotlin — от Junior до Middle". |
| **Backend Ktor + Frontend + Mobile KMP — один язык** | Единый Kotlin для всех компонентов: <br> • Backend: Ktor + Kotlin <br> • Web: Compose Multiplatform <br> • Mobile: KMP (Android + iOS) <br> • Shared: KMP models, validation, API client |
| **Mobile — просто добавить targets** | В отличие от React (где нужен React Native), в Compose добавление mobile — это просто расширение конфигурации. 80-90% кода шарится между Web, Android, iOS, Desktop. |
| **KMP — first-class citizen в Compose** | Compose Multiplatform — это KMP-first. Всё, что работает в Kotlin (models, business logic, validation), работает в Compose без дополнительных abstraction layers. |

---

### 4. Итоговое сравнение

| Критерий | React + TypeScript | Compose Multiplatform |
|----------|:------------------:|:---------------------:|
| **Лидер рынка** | ✅ 42.6% | ❌ <5% |
| **Enterprise экосистема** | ✅ Ant Design, MUI, AG Grid | ⚠️ Ограниченная |
| **Serverless** | ✅ Next.js + Vercel/Y.Cloud | ⚠️ Требует настройки |
| **Найм** | ✅ Легко | ❌ Сложно |
| **Единый язык с Backend** | ❌ TypeScript | ✅ Kotlin |
| **KMP шаринг кода** | ❌ Сложно | ✅ Нативно |
| **Mobile (из коробки)** | ❌ React Native | ✅ KMP |
| **Образовательная ценность** | ❌ Двойной стек | ✅ Единый Kotlin |

---

## Decision

**Выбрано:** Compose Multiplatform (Kotlin/Compose)

---

## Rationale

### Итог: Compose — стратегический выбор для курса

**Почему Compose, несмотря на технические минусы:**

1. **Для курса "Kotlin — от Junior до Middle"** — это правильный выбор:
   - Студенты изучают единый Kotlin стек
   - Backend + Frontend + Mobile = полный full-stack
   - KMP — must-have навык 2026

2. **Технические минусы компенсируемы:**
   - Beta → скоро Stable (JetBrains активно развивают)
   - B2B компоненты → Material 3 + custom components
   - Serverless → Ktor + GraalVM native image
   - Найм → обучение внутри команды / hiring Kotlin-девелоперов

3. **Стратегический выигрыш:**
   - Единый код для Web + Android + iOS + Desktop
   - Kotlin everywhere — конкурентное преимущество на рынке
   - No React Native = no double stack

---

## Consequences

### Positive

- **Единый Kotlin стек** — backend, web, mobile, desktop
- **KMP из коробки** — 80-90% кода шарится
- **Mobile — просто расширение** — не отдельная технология
- **Material 3 + Compose** — enterprise-ready компоненты
- **Serverless ready** — Ktor/Netty/GraalVM для Y.Cloud
- **Образовательная ценность** — полный Kotlin курс
- **Конкурентное преимущество** — Kotlin full-stack разработчики востребованы

### Negative

- Меньше B2B UI компонентов чем React экосистема
- Менее массовый рынок разработчиков
- Compose Web в Beta (временно)
- Сложнее найти готовых Kotlin/Web разработчиков

### Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-------------|
| Меньше enterprise UI компонентов | Medium | Low | Material 3 + custom components |
| Kotlin/Web разработчиков меньше чем React | Medium | Medium | Обучение внутри команды |
| Compose Web Beta | Low | Medium | Следить за стабильностью, мигрировать к Stable |
| Serverless сложнее чем Next.js | Medium | Low | Ktor + GraalVM native image |

### Alternatives Considered

**React + TypeScript:**
- ✅ Технически лучше для проекта
- ✅ Лидер рынка, легкий найм
- ✅ Богатая экосистема B2B
- ❌ Двойной стек (Kotlin backend + TS frontend)
- ❌ Mobile = React Native (отдельная технология)
- ❌ Не подходит для курса "Kotlin everywhere"

**Решение:** Выбираем Compose по стратегическим причинам курса.

---

*Decision пересматривается если:*
- *Compose Multiplatform Web перестанет развиваться*
- *Появится острая необходимость в React-специфичных B2B компонентах*
- *Курс изменит фокус на найм готовых разработчиков*
