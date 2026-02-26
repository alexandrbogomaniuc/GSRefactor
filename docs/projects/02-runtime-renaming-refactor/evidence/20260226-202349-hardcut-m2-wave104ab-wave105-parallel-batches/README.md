# Wave 104A/104B + 105 Evidence (Parallel Batch Integration)

Scope:
- W104A: migrate 10 low-risk API/form declaration packages to `com.abs`.
- W104B: migrate 10 low-risk support/cache/web declaration packages to `com.abs`.
- W105 integration: merged validation/evidence after parallel execution with bounded rewires in `log4j2.xml` and `getSessionError.jsp`.

Validation:
- Fast gate: PASS (`web-gs package`, `refactor smoke`) after command-path correction rerun.
- Full matrix: PASS (`9/9`).
