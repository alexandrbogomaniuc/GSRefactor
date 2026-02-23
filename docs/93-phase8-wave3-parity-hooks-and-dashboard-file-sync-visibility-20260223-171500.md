# Phase 8 Wave 3 Parity Hooks + Dashboard File Sync Visibility (2026-02-23)

## Scope
- Main project priority: continue Phase 8 precision modernization (GS-only) with Wave 3 runtime scaffold, no behavior switch.
- Secondary fix: make `file://` dashboard refresh visibly show embedded snapshot updates even when completed-count does not change.

## Phase 8 Wave 3 (GS-only, compatibility preserved)
### What changed
- Added disabled-by-default parity hook scaffolding in GS settings/coin-rule paths:
  - `DynamicCoinManager`
    - `verifyLegacyBaseBetParityForScale2IfEnabled(...)`
    - `getScaleReadyBaseBetInCurrencyMinorUnits(...)`
    - property flag gate: `abs.gs.phase8.precision.dualCalc.compare`
  - `GamesLevelHelper`
    - `verifyLegacyTemplateMaxBetParityForScale2IfEnabled(...)`
    - `getScaleReadyTemplateMaxBet(...)`
    - property flag gate: `abs.gs.phase8.precision.dualCalc.compare`
- Hooks are invoked on existing runtime paths but are disabled by default and only assert parity when explicitly enabled.

### Why
- This creates a safe runtime comparison entry point before any precision behavior switch, keeping legacy scale=2 behavior intact while preparing for discrepancy evidence collection.

## Dashboard `file://` visibility fix
### Root cause
- Progress headline mostly reflects status counts (`done/in_progress/planned`).
- Many recent iterations changed evidence paths/docs only, so completed-count remained unchanged.
- `checklist.updatedAt` is date-only (`YYYY-MM-DD`), so meta text also looked unchanged.

### Fix implemented
- `sync-modernization-dashboard-embedded-data.sh` now injects embedded snapshot metadata into the HTML JSON blocks:
  - `__embeddedSyncedAtUtc`
  - `__embeddedFingerprint`
- `modernizationProgress.html` now displays (for embedded/file mode):
  - snapshot sync timestamp
  - fingerprint
  - HTML file modified time
- Footer note explains that evidence-only updates may not change the completed-count.

## Test Evidence
- Dashboard sync script:
  - `bash -n gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` ✅
  - `gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh --help` ✅
  - `gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` ✅
  - embedded fields present in HTML (`__embeddedSyncedAtUtc`, `__embeddedFingerprint`) ✅
- Browser file-mode verification:
  - `file:///.../modernizationProgress.html` shows meta line with `snapshot synced`, `fp`, `html mtime` ✅
- Phase 8 Wave 3 and regression gates:
  - `gs-server/deploy/scripts/phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh` ✅ (`summary pass=12 fail=0`)
  - `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` ✅
    - report: `docs/quality/local-verification/phase5-6-local-verification-20260223-155349.md`
    - summary: `PASS=34, FAIL=0, SKIP=0`

## Compatibility / Rollback
- GS runtime behavior unchanged (Wave 3 hooks disabled by default).
- Rollback options:
  - remove parity hook calls/methods,
  - revert dashboard embedded metadata display + sync script enrichment.

## Next Step
- Continue Phase 8 Wave 3 with discrepancy evidence collection scaffolding (disabled-by-default counters/log snapshots) in GS settings/coin-rule paths, still without changing financial behavior.
