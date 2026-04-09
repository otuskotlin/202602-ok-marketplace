# TARGET DIRECTORY: docs/architecture/ADR/
# TARGET FILENAME: ADR-[NNN]-[decision_title].md (e.g., ADR-001-use-pg-repo.md)
# NEXT NUMBER: Sequential from glob(\"docs/architecture/ADR/ADR-*.md\" | wc -l +1)

# ADR-[NNN]: [Тема решения]

## Context

Implements [BR-NNN](../business/BR-NNN.md).
[Краткое описание проблемы для решения]

## Comparison

Comparing candidates against [BR-NNN](../business/BR-NNN.md):

| Criteria | [Option A](../proposals/OPT-A-name.md) | [Option B](../proposals/OPT-B-name.md) |
|----------|:---:|:---:|
| **Scenario: Name** | ✅/⚠️/❌ | ✅/⚠️/❌ |
| Latency | Nms | Nms |
| Throughput | N | N |

## Decision

**Chosen:** Option [X]

## Rationale

[Почему выбран этот вариант. Ссылка на сценарий из BR]

## Consequences

**Positive:**
- [Плюс 1]

**Negative:**
- [Минус 1]

**Risks:**
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| ... | ... | ... | ... |
