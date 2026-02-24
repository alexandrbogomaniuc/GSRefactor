# Phase 5/6 Service Extraction Status Report

- Verification suite source: /Users/alexb/Documents/Dev/dev_new/docs/quality/local-verification/phase5-6-local-verification-20260224-142510.md
- verification pass/fail/skip: 82/0/0
- overall_status: NO_GO_RUNTIME_FAILURE
- decision: No-Go (runtime failures present)

## Service Status

| Service | Runtime Evidence | Readiness | Probe Statuses | Service Status |
|---|---|---|---|---|
| gameplay_orchestrator | `/Users/alexb/Documents/Dev/dev_new/docs/phase5/gameplay/phase5-gameplay-runtime-evidence-20260224-163436.md` | PASS | gameplay_canary_probe=FAIL | NO_GO_RUNTIME_FAILURE |
| wallet_adapter | `/Users/alexb/Documents/Dev/dev_new/docs/phase5/wallet/phase5-wallet-runtime-evidence-20260224-163436.md` | PASS | wallet_canary_probe=FAIL | NO_GO_RUNTIME_FAILURE |
| bonus_frb_service | `/Users/alexb/Documents/Dev/dev_new/docs/phase5/bonus-frb/phase5-bonus-frb-runtime-evidence-20260224-163041.md` | PASS | bonus_frb_canary_probe=FAIL | NO_GO_RUNTIME_FAILURE |
| history_service | `/Users/alexb/Documents/Dev/dev_new/docs/phase5/history/phase5-history-runtime-evidence-20260224-163045.md` | PASS | history_canary_probe=FAIL | NO_GO_RUNTIME_FAILURE |
| multiplayer_service | `/Users/alexb/Documents/Dev/dev_new/docs/phase6/multiplayer/phase6-multiplayer-runtime-evidence-20260224-163048.md` | PASS | multiplayer_routing_policy_probe=PASS; multiplayer_canary_probe=SKIPPED | TESTED_GO_RUNTIME_READY |

## Interpretation

- Review service rows above and resolve failing runtime or verification checks before proceeding.

## Checklist Mapping

- se-gameplay-orchestrator
- se-wallet-adapter
- se-bonus-service
- se-history-service
- se-mp-service
