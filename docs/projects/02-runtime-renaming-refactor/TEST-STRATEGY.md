# RENAME-FINAL Test Strategy

Last updated: 2026-02-25 UTC

## Testing principles
1. No blind replacement.
2. Validate runtime behavior after each rename wave.
3. Keep backward compatibility until migration proof is complete.

## Test matrix
| ID | Area | What to validate | Method | Pass condition | Evidence path |
|---|---|---|---|---|---|
| RN-T001 | Inventory | latest legacy naming inventory is complete | inventory script | current list generated without errors | `evidence/inventory-*.md` |
| RN-T002 | Mapping | compatibility mapping covers all high-risk items | map validator + review | no uncovered high-risk item | `evidence/mapping-validation-*.md` |
| RN-T003 | Build | renamed modules compile | Maven build | build success | `evidence/build-*.txt` |
| RN-T004 | Unit | reflection helper compatibility tests pass | unit tests | all compatibility tests pass | `evidence/unit-compat-*.txt` |
| RN-T005 | Config | class-string key aliases read old and new names | targeted tests | both key forms resolve correctly | `evidence/config-alias-*.md` |
| RN-T006 | Startup | GS and MP start after each wave | startup smoke | no class loading failures | `evidence/startup-smoke-*.md` |
| RN-T007 | Launch | `/startgame` works after each wave | runtime smoke | HTTP 200 + page load | `evidence/launch-*.md` |
| RN-T008 | Wallet | wager/refund/settle remains correct | runtime smoke + logs | lifecycle complete | `evidence/wallet-*.md` |
| RN-T009 | Multiplayer | GS-MP contract works with migrated keys | runtime smoke + logs | no contract breakage | `evidence/mp-contract-*.md` |
| RN-T010 | Template | template and bank configs handle new naming | support/config checks | values load and save correctly | `evidence/template-config-*.md` |
| RN-T011 | Regression | full local verification suite remains green | `phase5-6-local-verification-suite.sh` | pass/fail/skip within policy | `docs/quality/local-verification/phase5-6-local-verification-*.md` |
| RN-T012 | Legacy scan | no unexpected high-risk legacy tokens remain | scan + manual review | only approved residuals remain | `evidence/residual-scan-*.md` |
| RN-T013 | Rollback | each wave can roll back safely | patch rollback drill | rollback success | `evidence/rollback-*.md` |
| RN-T014 | Decommission | removal of fallback does not break runtime | integrated regression | no regression in critical flows | `evidence/decommission-*.md` |

## Mandatory script set (existing)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-runtime-naming-inventory.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-compatibility-map-validate.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-patch-plan-export.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`

## Test execution order
1. RN-T001 to RN-T005
2. RN-T006 to RN-T010
3. RN-T011 to RN-T014

## Stop criteria
- Any failure in RN-T006 to RN-T014 blocks sign-off.
- Any unresolved high-risk legacy runtime token blocks decommission.
