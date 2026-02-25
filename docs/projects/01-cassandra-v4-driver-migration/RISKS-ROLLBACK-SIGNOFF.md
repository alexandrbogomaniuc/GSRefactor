# CASS-V4 Risks, Rollback, And Sign-Off Gates

Last updated: 2026-02-25 UTC

## Top risks
| Risk | Impact | Likelihood | Mitigation | Owner |
|---|---|---|---|---|
| Hidden driver API incompatibility | runtime failures | medium | phased migration + module-level tests + wrappers | Tech lead |
| Data mismatch after copy | financial inconsistency | medium | critical-table parity checks + targeted verification | Data owner |
| Runtime performance regression | degraded player experience | medium | baseline vs target perf tests + config tuning | Perf owner |
| Rollback path not usable | extended outage risk | low/medium | rollback rehearsal before sign-off | Ops owner |
| Mixed old/new configuration drift | unstable behavior | medium | config freeze + explicit env manifests | Release owner |

## Rollback policy
1. Keep pre-migration snapshots and schema exports immutable.
2. Maintain known-good runtime config bundle.
3. If any sign-off gate fails, stop rollout and execute rollback runbook.
4. Post-rollback, run minimal health validation before retry planning.

## Sign-off gates
| Gate | Requirement | Status values |
|---|---|---|
| SG1 | Driver migration complete and reviewed | PASS/FAIL |
| SG2 | Schema parity validated | PASS/FAIL |
| SG3 | Data parity validated for critical tables | PASS/FAIL |
| SG4 | Runtime flow regression suite passes | PASS/FAIL |
| SG5 | Performance within threshold | PASS/FAIL |
| SG6 | Rollback drill successful | PASS/FAIL |
| SG7 | Documentation and evidence complete | PASS/FAIL |

## Final decision rule
- `SIGN_OFF_READY` only if all SG1-SG7 are `PASS`.
- Any `FAIL` means `NO_GO` until fixed and retested.
