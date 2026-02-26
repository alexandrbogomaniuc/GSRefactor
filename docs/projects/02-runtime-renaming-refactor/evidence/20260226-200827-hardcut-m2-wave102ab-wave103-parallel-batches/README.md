# Wave 102A/102B + 103 Evidence (Parallel Batch Integration)

Scope:
- W102A: migrate FRB transport + MQB response declarations to `com.abs`.
- W102B: migrate low-risk entity/lobby/web/cache/error declarations to `com.abs` with bounded import rewires.
- W103 integration: merged validation/evidence after parallel execution, with compatibility correction for game-history list typing.

Validation:
- Fast gate: PASS (`web-gs package`, `refactor smoke`)
- Full matrix: PASS (`9/9`)
