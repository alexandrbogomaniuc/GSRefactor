# Phase 7 Cassandra Rehearsal Checklist v1

Last updated: 2026-02-20 UTC

## A) Pre-rehearsal
- [ ] Capture current cluster release/version and topology.
- [ ] Export keyspace/table metadata (including compaction/compression settings).
- [ ] Capture row count/sample checks for critical tables.
- [ ] Freeze schema change window.
- [ ] Confirm backup location and restore permissions.

## B) Snapshot + restore rehearsal
- [ ] Take snapshot for all target keyspaces.
- [ ] Restore into target Cassandra cluster.
- [ ] Run `nodetool repair` / validation commands as applicable.
- [ ] Verify all keyspaces/tables exist post-restore.

## C) Parity validation
- [ ] Schema parity diff: no unexpected changes.
- [ ] Row count parity for critical tables within agreed tolerance.
- [ ] Sample row hash checks for high-risk tables (accounts/sessions/wallet/bonus/history).
- [ ] Query smoke on critical tables passes (`phase7-cassandra-query-smoke.sh`).
- [ ] GS launch/wager/settle/history/reconnect smoke tests pass.
- [ ] MP reconnect/lobby smoke tests pass for MP banks.

## D) Performance/reliability checks
- [ ] p95/p99 latency baseline compared with pre-upgrade.
- [ ] Query timeout and retry behavior validated.
- [ ] GC and compaction behavior reviewed.
- [ ] No sustained error spikes in logs/metrics.

## E) Sign-off outputs
- [ ] Rehearsal report with pass/fail per item.
- [ ] Open issues and mitigation list.
- [ ] Go/No-Go recommendation for canary cutover.

## Evidence bundle location
- `docs/phase7/cassandra/`.
