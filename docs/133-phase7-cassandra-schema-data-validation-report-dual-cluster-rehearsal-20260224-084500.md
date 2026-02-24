# Phase 7 - Cassandra Schema/Data Validation Report (Dual-Cluster Rehearsal) (2026-02-24)

## Scope
Schema/data validation for Phase 7 Cassandra rehearsal using:
- Source cluster: legacy `gp3-c1-1`
- Target cluster: refactor `refactor-c1-1`

This is a real container-executed validation run (CQL over live containers), not a synthetic report.

## Assumptions (explicit)
- This closes the Phase 7 **rehearsal + validation deliverable** with a tested result (`No-Go`) and blocker list.
- Actual target-version upgrade execution (for example 4.x/5.x cutover candidate) remains a follow-up infra execution wave after blockers are cleared.

## Runtime commands executed (container PTY mode)
- Source preflight / schema / counts / query-smoke were executed in `gp3-c1-1` via `docker exec ... cqlsh`.
- Target preflight / schema / counts / query-smoke were executed in `refactor-c1-1` via `docker exec ... cqlsh`.
- In this sandbox, `docker exec` only works reliably as plain PTY commands (no host-side stdout capture/redirection).
- For refactor target artifacts, outputs were written inside the container to `/var/log/cassandra/*` and copied from bind-mounted host path into `docs/phase7/cassandra/`.

## 1) Schema validation
### Source/target keyspace scope used for schema hash parity
- `rcasinoscks`
- `rcasinoks`

### Schema hash parity result
- Source (`gp3-c1-1`, in-container `/tmp/phase7-source-schema.cql`)
  - `sha256=233b48345527987a917640a23894b4d2e90ae67c92188e3ddb6e22feba58cac0`
  - `lines=2283`
- Target (`refactor-c1-1`, in-container `/tmp/phase7-target-schema.cql`)
  - `sha256=233b48345527987a917640a23894b4d2e90ae67c92188e3ddb6e22feba58cac0`
  - `lines=2283`

Result:
- `PASS` for schema parity of GS/MP keyspaces (`rcasinoscks`, `rcasinoks`) by exact hash match.

### Keyspace inventory (preflight)
- Source `gp3-c1-1` keyspaces:
  - `rcasinoks`, `system`, `mpmain`, `rcasinoscks`, `system_traces`, `mpmqb2`
- Target `refactor-c1-1` keyspaces:
  - `rcasinoscks`, `system`, `system_traces`, `rcasinoks`

Result:
- `FAIL` for full-cluster keyspace parity (source contains `mpmain`, `mpmqb2` absent on target).
- This is a rehearsal blocker for MP-related cutover if those keyspaces are still required.

## 2) Critical table count parity (corrected table set)
### Corrected critical table set (validated against live schema)
- `rcasinoscks.accountcf`
- `rcasinoscks.accountcf_ext`
- `rcasinoscks.frbonuscf`
- `rcasinoscks.paymenttransactioncf2`
- `rcasinoks.gamesessioncf`

Reason for correction:
- Previous template/list entries `rcasinoscks.gamesessioncf` and `rcasinoscks.commonwalletcf` are not valid in the refactor dataset schema.

### Count results
| Keyspace | Table | Source Count (`gp3-c1-1`) | Target Count (`refactor-c1-1`) | Delta | Pass/Fail | Notes |
|---|---|---:|---:|---:|---|---|
| rcasinoscks | accountcf | 1 | 4 | +3 target | FAIL | target contains additional local/refactor accounts |
| rcasinoscks | accountcf_ext | 1 | 4 | +3 target | FAIL | target contains additional local/refactor ext IDs |
| rcasinoscks | frbonuscf | 4 | 0 | -4 target | FAIL | source FRB rows absent on target |
| rcasinoscks | paymenttransactioncf2 | 0 | 0 | 0 | PASS | both empty |
| rcasinoks | gamesessioncf | 68 | 0 | -68 target | FAIL | source gameplay sessions absent on target |

Result:
- `FAIL` data parity for corrected critical table set.
- Target cluster is not restored/parity-aligned with source legacy dataset.

## 3) Query smoke (runtime table access)
### Source `gp3-c1-1` query smoke (direct CQL)
- `rcasinoscks.accountcf LIMIT 1` -> `PASS` (1 row returned)
- `rcasinoscks.accountcf_ext LIMIT 1` -> `PASS` (1 row returned)
- `rcasinoscks.frbonuscf LIMIT 1` -> `PASS` (1 row returned)
- `rcasinoscks.paymenttransactioncf2 LIMIT 1` -> `PASS` (0 rows, query valid)
- `rcasinoks.gamesessioncf LIMIT 1` -> `PASS` (1 row returned)

### Target `refactor-c1-1` query smoke (artifact file)
- `rcasinoscks.accountcf LIMIT 1` -> `PASS` (1 row returned)
- `rcasinoscks.accountcf_ext LIMIT 1` -> `PASS` (1 row returned)
- `rcasinoscks.frbonuscf LIMIT 1` -> `PASS` (0 rows, query valid)
- `rcasinoscks.paymenttransactioncf2 LIMIT 1` -> `PASS` (0 rows, query valid)
- `rcasinoks.gamesessioncf LIMIT 1` -> `PASS` (0 rows, query valid)

Result:
- `PASS` query compatibility/runtime access for corrected critical tables on both clusters.
- `FAIL` sample-row parity due dataset divergence (row presence/content differs).

## 4) Evidence files
### Target (refactor) captured files
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-preflight-refactor-c1-1-20260224-082227.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-refactor-c1-1-20260224-082227.cql`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-table-counts-refactor-c1-1-20260224-082227.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-query-smoke-refactor-c1-1-20260224-082227.log`

### Source (legacy) live command outputs captured during rehearsal
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-preflight-legacy-gp3-c1-1-20260224-082227.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-legacy-gp3-c1-1-20260224-082227.cql`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-table-counts-legacy-gp3-c1-1-20260224-082227.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-query-smoke-legacy-gp3-c1-1-20260224-082227.log`
- Schema diff (source vs target): `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-schema-diff-legacy-gp3-vs-refactor-c1-20260224-082227.patch` (0 lines, exact schema match)

## 5) Validation decision
- Schema parity (GS/MP keyspaces): `PASS`
- Full-cluster keyspace parity: `FAIL`
- Critical-table count parity: `FAIL`
- Query compatibility/runtime access: `PASS`
- Overall schema/data validation status: `FAIL (No-Go until target restore/parity alignment)`

## 6) Next required fixes before canary cutover
1. Restore/seed target cluster from source snapshot for required keyspaces (including MP keyspaces if still in scope).
2. Re-run corrected count parity and sample-row parity checks.
3. Re-run GS/MP runtime smoke flows against target-aligned dataset.
