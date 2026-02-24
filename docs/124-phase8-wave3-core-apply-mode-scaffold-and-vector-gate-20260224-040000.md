# Phase 8 Wave 3 - Core Apply-Mode Scaffold and Vector Gate (2026-02-24)

## Scope
Core GS settings / coin-rule precision modernization scaffold (backend code + offline vector tests).
No default runtime behavior change: legacy scale=2 remains active unless explicitly enabled via system properties.

## What Changed
- Added disabled-by-default precision apply-mode scaffolding to GS settings/coin-rule code paths:
  - `/game/settings/DynamicCoinManager.java`
  - `/game/settings/GamesLevelHelper.java`
- New system properties (Wave 3 opt-in activation prep):
  - `abs.gs.phase8.precision.scaleReady.apply` (default `false`)
  - `abs.gs.phase8.precision.scaleReady.minorUnitScale` (default/fallback `2`)
- `DynamicCoinManager`:
  - introduced `getConfiguredBaseBetInCurrencyMinorUnits(...)`
  - `getDynamicDefaultCoin(...)` now uses configured base-bet minor-unit scale when apply-mode is enabled
  - preserved existing dual-calc parity hooks and legacy behavior when apply-mode is disabled
- `GamesLevelHelper`:
  - introduced `getConfiguredTemplateMaxBet(...)`
  - `getGLMaxBet(...)` now routes through configured scale-aware template max calculation only when apply-mode is enabled
  - preserved existing dual-calc parity hooks and legacy behavior when apply-mode is disabled
- Added deterministic offline vector gate:
  - `phase8-precision-wave3-applymode-vector-smoke.sh`
  - validates disabled-by-default behavior, scale parsing/fallbacks, and scale3 apply-mode examples (`0.001`, line-based normalization)
- Added apply-mode smoke help + execution to shared local verification suite (`phase5-6-local-verification-suite.sh`).

## Validation Performed
- Targeted new smoke
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave3-applymode-vector-smoke.sh`
  - result: `summary pass=12 fail=0`
- Script syntax checks
  - `bash -n phase8-precision-wave3-applymode-vector-smoke.sh`
  - `bash -n phase5-6-local-verification-suite.sh`
- Shared verification suite (with new apply-mode checks included)
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260224-044932.md`
  - summary: `pass=42 fail=0 skip=0`
- Dashboard embedded data sync
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`
  - checklist remains `26/41` (tooling/evidence increment only)
  - embedded fingerprint: `fp=b2f6cf80d59b`
- Evidence propagation checks
  - `modernization-checklist.json`, `modernizationDocs.jsp`, and synced `modernizationProgress.html` now point `pu-precision-audit` evidence to this doc (`doc 124`)
- Git whitespace check
  - `git -C /Users/alexb/Documents/Dev/Dev_new diff --check` clean before commit

## Compatibility / Rollback
- Backward compatible by default (`apply=false`).
- New behavior is opt-in only and intended for non-prod validation/canary prep.
- Rollback: revert this commit; no schema/data impact.

## Phase 8 Impact (Status)
- This does not complete Phase 8 yet.
- It advances Phase 8 from comparison-only scaffolding toward controlled precision activation in core GS settings/coin-rule calculations.
- Remaining work for full Phase 8 completion is still required (bank/currency policy mapping, 0.001 enablement validation matrix across GS/wallet/reporting paths, and controlled rollout evidence).
