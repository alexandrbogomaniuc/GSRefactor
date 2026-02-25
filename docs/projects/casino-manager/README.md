# Casino Manager Project

This folder is the single source of truth for the Casino Manager reverse-mapping and clone implementation plan.

## Scope
- Crawl and document the provider CM structure in read-only mode.
- Map CM pages/reports/tools to our current DB schema.
- Define a safe data-sync strategy (live -> copy DB) for our CM.
- Deliver a dedicated CM module/container plan.

## Documents
- `00-project-charter.md`: goals, constraints, and success criteria.
- `01-cm-crawl-inventory.md`: CM menu/routes crawl coverage and page logic inventory.
- `02-db-schema-baseline.md`: Cassandra schema baseline used for mapping (SQL deferred).
- `03-cm-to-db-mapping.md`: functional mapping between CM pages and our tables.
- `04-sync-copy-strategy.md`: hourly cron sync baseline and rollout path.
- `05-container-module-plan.md`: dedicated container/module deployment plan.
- `06-open-questions.md`: best-practice decision closure for phase-1.
- `07-auth-logic.md`: authentication and authorization logic (default `root`/`root` bootstrap).
- `08-phase1-report-query-matrix.md`: detailed filter/column contracts and Cassandra query strategy for phase-1 report pack.
- `09-cassandra-read-model-v1.cql`: initial Cassandra read-model and sync metadata schema.
- `10-hourly-sync-runbook.md`: cron schedule, job steps, and failure handling runbook.
- `11-auth-implementation-checklist.md`: concrete delivery checklist for auth/RBAC implementation.
- `12-runnable-prototype.md`: how to run and test the current CM prototype module.
- `13-provider-url-map.md`: full 94-endpoint provider menu URL map captured from authenticated read-only crawl.
- `14-functionality-map.md`: detailed functionality contract and step-by-step implementation plan for player-first parity.
- `changelog.md`: ongoing changelog for this project only.

## Working Rule
- Update `changelog.md` for every meaningful architecture, schema, security, or scope decision.
- Keep provider credentials and secrets out of docs and commits.
- Record evidence for crawls/mappings in `evidence/`.
- Keep implementation templates in `templates/`.
