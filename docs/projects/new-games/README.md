# New Games Project

This folder is the single source of truth for new game development planning and decisions.

## Scope
- Build new games while keeping existing GS as the core platform.
- Integrate new game runtime with GS session and wallet flows.
- Start with Plinko as the first production candidate.

## Documents
- `00-product-decisions.md`: confirmed product/architecture constraints.
- `01-gs-capability-map.md`: initial GS code capability map with Java source references.
- `02-target-architecture.md`: target architecture for GS + New Games stack.
- `03-milestones.md`: phased delivery plan and acceptance targets.
- `04-kickoff-readiness.md`: readiness checklist and day-1 execution plan.
- `05-gs-internal-api-contract-v1.md`: frozen NGS <-> GS internal API v1 contract.
- `06-routing-and-cutover-strategy.md`: legacy/new route selection and rollout safety.
- `07-brand-foundation.md`: temporary BETONLINE-inspired brand tokens/assets.
- `08-testing-and-perf-baseline.md`: current automated test coverage and local smoke performance evidence.
- `09-runtime-ops-handoff.md`: runtime packaging/deploy/e2e/status runbook for operations handoff.
- `10-m4-proof-pack.md`: one-command M4 evidence generation (`runtime:e2e + runtime:status + perf SLO checks`).
- `changelog.md`: separate ongoing changelog for this project only.

## Working Rule
- Update `changelog.md` for every meaningful architecture, API, delivery, or scope decision.
