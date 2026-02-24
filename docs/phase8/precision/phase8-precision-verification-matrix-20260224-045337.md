# Phase 8 Precision Verification Matrix (2026-02-24T04:53:38.654Z UTC)

- source-policy: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase8-precision-policy.json`
- schemaVersion: 1
- defaultMinorUnitScale: 2
- allowedMinorUnitScales: 2, 3
- currencyPolicies: 6
- verificationCategories: 5
- blockingCategories: 3
- phase8ReadyToClose: no

## Currency Matrix

| Currency | Scale | Phase8 Status | Canary | Notes |
|---|---:|---|---|---|
| USD | 2 | legacy_default | yes | Keep legacy scale=2 default while scale-ready compare/apply remains disabled in production. |
| EUR | 2 | legacy_default | yes | Baseline legacy scale=2 currency for parity and regression validation. |
| GBP | 2 | legacy_default | yes | Baseline legacy scale=2 currency for parity and regression validation. |
| KWD | 3 | candidate_scale3 | yes | Scale=3 candidate for 0.001 validation under non-prod canary after wallet/reporting contract confirmation. |
| BHD | 3 | candidate_scale3 | yes | Scale=3 candidate for non-prod compare/apply validation with audit evidence. |
| JOD | 3 | candidate_scale3 | no | Hold until downstream wallet/reporting precision compatibility is proven. |

## Verification Categories

| Category | Status | Blocking | Evidence |
|---|---|---|---|
| gs_settings_coin_rule | vector_gated_apply_mode_scaffold | no | docs/124-phase8-wave3-core-apply-mode-scaffold-and-vector-gate-20260224-040000.md |
| gs_reporting_display | wave1_remediated_vector_gated | no | docs/89-phase8-wave1-closure-and-wave2-coinrule-vectors-20260223-161500.md |
| wallet_contract_and_rounding | pending_external_validation | yes | docs/66-phase5-wallet-runtime-evidence-pack-tooling-20260220-185700.md |
| history_reporting_exports | pending_matrix_execution | yes | docs/70-phase5-history-runtime-evidence-pack-tooling-20260220-191100.md |
| nonprod_canary_runtime | pending | yes | docs/26-bank-canary-policy-v1.md |

## Summary Counts

### By Minor Unit Scale
- scale 2: 3
- scale 3: 3

### By Currency Status
- candidate_scale3: 3
- legacy_default: 3

## Next Actions (Generated)
- wallet_contract_and_rounding: pending_external_validation -> resolve before Phase 8 closure
- history_reporting_exports: pending_matrix_execution -> resolve before Phase 8 closure
- nonprod_canary_runtime: pending -> resolve before Phase 8 closure

## Notes
- This matrix is a GS-side planning/verification artifact and does not activate precision behavior by itself.
- Runtime activation remains gated by Wave 3 apply-mode properties and canary validation.
