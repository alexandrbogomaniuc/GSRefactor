# RENAME-FINAL Risks, Rollback, And Sign-Off Gates

Last updated: 2026-02-25 UTC

## Top risks
| Risk | Impact | Likelihood | Mitigation | Owner |
|---|---|---|---|---|
| Blind rename of runtime-sensitive token | startup/runtime failure | medium/high | compatibility-first waves + review-only guardrails | Tech lead |
| Reflection/class-load mismatch | game launch or processing failure | medium | targeted class-loader compatibility tests | Runtime owner |
| GS-MP payload key mismatch | multiplayer breakage | medium | dual-read/dual-write transition + contract tests | Integration owner |
| Config/template drift | hidden runtime regressions | medium | bank/template validation checks | Config owner |
| Premature fallback removal | unexpected production regression | medium | residual scans + staged decommission | Release owner |

## Rollback policy
1. Keep wave-level patch and revert instructions for every apply wave.
2. Do not combine fallback removal with first migration write.
3. If runtime regression appears, rollback immediately to prior wave baseline.
4. After rollback, run startup + launch + wallet + multiplayer health checks.

## Sign-off gates
| Gate | Requirement | Status values |
|---|---|---|
| SG1 | Inventory and mapping complete | PASS/FAIL |
| SG2 | Runtime class-string migration validated | PASS/FAIL |
| SG3 | Contract/template migration validated | PASS/FAIL |
| SG4 | End-to-end runtime tests pass | PASS/FAIL |
| SG5 | Residual legacy scan accepted | PASS/FAIL |
| SG6 | Rollback drills successful | PASS/FAIL |
| SG7 | Decommission wave validated | PASS/FAIL |
| SG8 | Documentation and evidence complete | PASS/FAIL |

## Final decision rule
- `SIGN_OFF_READY` only if all SG1-SG8 are `PASS`.
- Any `FAIL` means `NO_GO` until fixed and retested.
