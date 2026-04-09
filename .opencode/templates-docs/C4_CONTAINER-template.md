# TARGET DIRECTORY: docs/architecture/
# TARGET FILENAME: C4_CONTAINER.md (single file, edit if exists)

# C4 Container Diagram - Level 2

## System: {{system_name}}

```mermaid
graph TB
    subgraph \"{{system_name}}\"
        UI[Web UI]
        API[API App]
        DB[(Database)]
        MQ[(Message Queue)]
    end
    ExternalUser --> UI
    UI --> API
    API --> DB
    API --> MQ
```
{{additional_containers}}