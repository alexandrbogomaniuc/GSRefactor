# CASS-V4 Test Strategy

Last updated: 2026-02-25 UTC

## Testing principles
1. Prove correctness first, then performance.
2. Cover both technical migration and business-critical flows.
3. Keep test evidence reproducible from scripts and logs.

## Test matrix
| ID | Area | What to validate | Method | Pass condition | Evidence path |
|---|---|---|---|---|---|
| C4-T001 | Dependency | All Java Cassandra consumers use approved 4.x driver | dependency inventory script | no 3.x driver in active runtime modules | `evidence/dependency-inventory-*.md` |
| C4-T002 | Build | All migrated modules compile | Maven build | build success, no errors | `evidence/build-*.txt` |
| C4-T003 | Unit | DAO/repository logic under new APIs | unit tests | tests pass | `evidence/unit-*.txt` |
| C4-T004 | Session config | driver 4 session settings load correctly | integration test | connection stable, no startup errors | `evidence/session-config-*.md` |
| C4-T005 | Query compatibility | critical queries run on Cassandra 4 | query smoke script | zero failed critical queries | `docs/phase7/cassandra/phase7-cassandra-query-smoke-*.log` |
| C4-T006 | Schema parity | source vs target schema differences controlled | schema export + diff | only expected diffs | `docs/phase7/cassandra/phase7-cassandra-schema-diff-*.patch` |
| C4-T007 | Table parity | row-count parity for required tables | table-counts script | required tables match | `docs/phase7/cassandra/phase7-cassandra-table-counts-*.txt` |
| C4-T008 | Data semantics | financial key tables preserve required fields | targeted validation queries | no critical mismatch | `evidence/financial-parity-*.md` |
| C4-T009 | Launch flow | `/startgame` works with Cassandra 4 target | runtime smoke | HTTP 200 and playable load | `evidence/launch-smoke-*.md` |
| C4-T010 | Wallet flow | auth/balance/bet/settle/refund complete | runtime smoke + logs | full lifecycle success | `evidence/wallet-flow-*.md` |
| C4-T011 | Multiplayer flow | GS-MP communication works | runtime smoke + logs | no protocol breakage | `evidence/mp-flow-*.md` |
| C4-T012 | Reconnect/history | reconnect and history read paths work | runtime tests | no regressions | `evidence/reconnect-history-*.md` |
| C4-T013 | Local full suite | existing regression suite remains green | `phase5-6-local-verification-suite.sh` | pass/fail/skip within policy | `docs/quality/local-verification/phase5-6-local-verification-*.md` |
| C4-T014 | Performance | p95 latency and error budget acceptable | controlled load test | within agreed threshold | `evidence/performance-*.md` |
| C4-T015 | Resilience | retry and failover behavior | failure-injection tests | graceful handling, no data corruption | `evidence/resilience-*.md` |
| C4-T016 | Rollback | fallback path restores safe state | rollback drill | rollback success + validation pass | `evidence/rollback-*.md` |

## Mandatory script set (existing)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-preflight.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-driver-inventory.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-table-counts.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-evidence-pack.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-rehearsal-report-generate.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`

## Test execution order
1. C4-T001 to C4-T004
2. C4-T005 to C4-T008
3. C4-T009 to C4-T013
4. C4-T014 to C4-T016

## Stop criteria
- Any failed test in C4-T009 to C4-T016 blocks sign-off.
- Any unresolved data mismatch in financial-critical tables blocks sign-off.
