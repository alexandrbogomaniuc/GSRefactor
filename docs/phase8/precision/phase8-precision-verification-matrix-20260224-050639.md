# Phase 8 Precision Verification Matrix (2026-02-24T05:06:39.477Z UTC)

- source-policy: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase8-precision-policy.json`
- schemaVersion: 1
- defaultMinorUnitScale: 2
- allowedMinorUnitScales: 2, 3
- currencyPolicies: 6
- verificationCategories: 5
- blockingCategories: 1
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
| wallet_contract_and_rounding | offline_contract_vector_gated_pending_partner_runtime_confirmation | no | docs/127-phase8-wallet-contract-precision-vector-gate-20260224-060000.md |
| history_reporting_exports | offline_vector_gated_pending_runtime_confirmation | no | docs/126-phase8-history-reporting-precision-vector-gate-20260224-053000.md |
| nonprod_canary_runtime | execution_ready_pending_jvm_flags_and_run | yes | docs/128-phase8-nonprod-canary-readiness-and-evidence-pack-20260224-063000.md |

## Summary Counts

### By Minor Unit Scale
- scale 2: 3
- scale 3: 3

### By Currency Status
- candidate_scale3: 3
- legacy_default: 3

## Next Actions (Generated)
- nonprod_canary_runtime: execution_ready_pending_jvm_flags_and_run -> resolve before Phase 8 closure

## Notes
- This matrix is a GS-side planning/verification artifact and does not activate precision behavior by itself.
- Runtime activation remains gated by Wave 3 apply-mode properties and canary validation.
