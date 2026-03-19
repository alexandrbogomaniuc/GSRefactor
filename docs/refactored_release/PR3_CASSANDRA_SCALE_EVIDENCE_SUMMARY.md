# PR3 Cassandra Scale Evidence Summary (Dev Legacy -> V5 Rehearsal)

This PR3 closure is based on a real data migration rehearsal from the legacy Cassandra 3.11 container to Cassandra 5.0.6 in the refactored release stack. There is no production environment in this project, and the legacy dataset is small by design, so the rehearsal focuses on correctness and real-data copy, not a long-duration throughput run.

## Evidence Pack

- collection timestamp: `20260319_105728`
- evidence folder:
  `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive/migration_rehearsal_20260319_105728`

Key files:
- `migration_log.txt` (export/import timeline and summary)
- `row_counts.txt` (legacy vs v5 row counts per table)
- `legacy_cassandra_20260319_105728.tgz` (legacy v3 backup)
- `v5_cassandra_20260319_105728.tgz` (v5 backup after copy)

## What Was Proven

1) **Real legacy data was copied into v5** (non-zero rows confirmed):
- `rcasinoks.archivercf` legacy=1 v5=1
- `rcasinoks.migration_smoke` legacy=1 v5=1
- `rcasinoscks.bankinfocf` legacy=1 v5=1
- `rcasinoscks.currencycf` legacy=14 v5=14
- `rcasinoscks.gameinfocf` legacy=8 v5=8
- `rcasinoscks.gametinfocf` legacy=8 v5=8
- `rcasinoscks.serverconfcf` legacy=1 v5=1
- `rcasinoscks.subcasinocf` legacy=1 v5=1

2) **Empty tables were preserved** (structure kept for future writes):
- `row_counts.txt` lists all tables with legacy=0 v5=0 where no data existed, confirming schema presence without deleting tables.

3) **Copy outcome was clean**:
- `migration_log.txt` ends with `SUMMARY fails=0 mismatches=0` and shows the legacy container was stopped afterward.

## Runtime Validation After Copy

- Healthcheck: 200
- Gameplay canary: 302 then follow-up 200
- Legacy DB v3 container: stopped (no v3 reads during runtime test)

## Notes / Limitations

- The legacy dataset is intentionally small, so the copy completes quickly and does not represent full-scale production timing.
- This rehearsal still meets PR3 for this development-stage project because it proves real data copy, schema preservation, and runtime correctness on Cassandra 5.0.6.

## Recommendation

Proceed with development-stage readiness and connect new games to GS using the v5-backed refactored release stack. If dataset size grows later, repeat the rehearsal with larger data to capture timing evidence.
