# Phase 8 Precision Remediation Wave Plan (Bucketed) (2026-02-23)

## Purpose
Convert the raw Phase 8 precision/min-bet audit into a safe execution sequence for `0.001` support preparation, keeping GS behavior backward-compatible during migration.

## Delivered
- Bucket planner script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh`
- Verification suite integration (help + executable run):
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`
- Support portal/runbook references updated:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`

## Generated Wave-Plan Report
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260223-145326.md`

## Bucket Counts (current matcher)
- `wave1_reporting_stats`: `0`
- `wave2_settings_coin_rules`: `2`
- `wave3_config_templates`: `10`
- `wave4_core_financial_paths`: `42`

## Interpretation
- Wave 4 currently dominates the matched hotspots and confirms why direct financial-path refactors must stay late and heavily gated.
- Wave 2 and Wave 3 already expose concrete GS-first candidates (`DynamicCoinManager`, `GamesLevelHelper`, bank config coin/min values) for controlled pre-financial remediation work.
- Wave 1 matcher is intentionally conservative and currently returns zero hits; raw Phase 8 audit evidence still shows cent-based reporting/statistics patterns (for example `MQServiceHandler` `Math.round(... * 100)` call sites). We will widen/split Wave 1 matching in the next increment rather than mixing risky code edits now.

## Safety / Testing Notes
- Fixed bucket-script reliability issues during implementation:
  - `rg` regex typo detection now fails the script instead of being hidden by `|| true`
  - shell glob expansion (`*.java`, `*.xml`) is disabled inside the bucket scan helper to preserve intended `rg -g` filters
- This prevents false PASS results in the shared verification suite.

## Validation
- `bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh --help` ✅
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh` ✅
  - output: `/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260223-145326.md`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-145326.md`
  - summary: `PASS=26`, `FAIL=0`, `SKIP=0`

## Next Step
- Improve Wave 1 bucketing (split reporting/statistics vs operational session update paths), then add a Wave 1-specific deterministic vector smoke before any code changes in cent-based display/reporting conversions.
