# Phase 7 Cassandra Upgrade Plan v1

Last updated: 2026-02-20 UTC
Scope: upgrade Cassandra with zero schema/table loss and bank-safe rollback.

## Baseline
- Current refactor runtime Cassandra image: `cassandra:2.1.20`.
- Existing schema/tables are source-of-truth and must be preserved.
- No production cutover without parity tests and canary sign-off.

## Target strategy
1. Build dual-cluster upgrade path (old + new).
2. Rehearse snapshot/restore + validation on production-like dataset.
3. Validate GS/MP/service driver compatibility against target cluster.
4. Cut over by canary banks first; global only after stability window.

## Upgrade phases
1. Inventory + freeze window
- Capture keyspaces/tables/indexes, table options, UDT/functions, and row counts.
- Freeze destructive DDL during rehearsal/cutover windows.

2. Rehearsal (non-production)
- Take full snapshot from current cluster.
- Restore into target Cassandra cluster.
- Run schema + data parity checks.
- Run launch/wager/settle/history/reconnect smoke suite.

3. Driver/protocol validation
- Verify Java drivers used by GS/MP and extracted services.
- Confirm protocol negotiation and query compatibility.
- Validate latency and timeout profiles under load.

4. Canary cutover
- Enable reads/writes on target for selected bank canary set.
- Monitor errors, p95 latency, and reconciliation counters.
- Rollback immediately on financial mismatch or protocol regression.

5. Global rollout
- Expand bank waves after canary stability.
- Keep rollback runbook and snapshots ready until post-window close.

## Non-negotiable gates
- Schema diff: zero unexpected differences.
- Data parity checks for critical tables: pass.
- Financial operation idempotency checks: pass.
- Reconnect and history parity: pass.
- Canary metrics within policy thresholds.

## Deliverables mapped
- Upgrade plan: this document.
- Rehearsal checklist/report: `docs/41-phase7-cassandra-rehearsal-checklist-v1.md`.
- Driver compatibility matrix: `docs/43-phase7-cassandra-driver-compatibility-matrix-v1.md`.
- Cutover/rollback runbook: `docs/42-phase7-cassandra-cutover-rollback-runbook-v1.md`.
- Schema/data parity template: `docs/44-phase7-cassandra-schema-data-parity-template-v1.md`.

## Automation helpers
- Preflight: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-preflight.sh`
- Driver inventory: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-driver-inventory.sh`
- Schema export: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh`
- Critical table counts: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-table-counts.sh`
- Query smoke: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh`
- Schema diff: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh`
- Evidence pack orchestrator: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-evidence-pack.sh`
- Rehearsal report generator: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-rehearsal-report-generate.sh`
