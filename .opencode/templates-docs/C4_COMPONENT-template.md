# TARGET DIRECTORY: docs/architecture/
# TARGET FILENAME: C4_COMPONENT.md (single file, edit if exists)

# C4 Component Diagram - Level 3

## Container: {{container_name}}

```mermaid
graph TB
    subgraph \"{{container_name}}\"
        COMP1[{{component1}}]
        COMP2[{{component2}}]
        COMP3[{{component3}}]
    end
    COMP1 --> COMP2
    COMP2 --> COMP3
```
{{interactions}}