# Wave 100A/100B + 101 Evidence (Parallel Batch Integration)

Scope:
- W100A: migrate 15 bonus/frbonus API actions to `com.abs` + Struts action-type rewires.
- W100B: migrate 12 routing/request DTO classes to `com.abs` + bounded dependent import rewires.
- W101 integration: merged validation and evidence capture after parallel execution.

Validation:
- Fast gate: PASS (`web-gs package`, `refactor smoke`)
- Full matrix: PASS (`9/9`)
