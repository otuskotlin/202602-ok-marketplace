# TARGET DIRECTORY: docs/architecture/proposals/
# TARGET FILENAME: OPT-[X]-[tech_name].md (e.g., OPT-A-pg-repo.md)
# X: A, B, C... sequential

# Option [X]: [Технология]

## Architecture

```mermaid
flowchart LR
    A[Client] --> B[Service]
    B --> C[(Storage)]
```

## Implementation

[Описание как встроить эту технологию в систему]

## Parameters (for ADR)

| Parameter | Value | Source |
|-----------|-------|--------|
| Latency | N ms | Benchmark |
| Throughput | N msg/sec | Benchmark |
| Cost | $N/month | Cloud estimate |

## Pros
- [Плюс 1]
- [Плюс 2]

## Cons
- [Минус 1]
- [Минус 2]
