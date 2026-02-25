# RENAME-FINAL Documentation And Evidence Checklist

Last updated: 2026-02-25 UTC

## Required documents
1. Updated runtime naming inventory report.
2. Compatibility mapping update log.
3. Wave-by-wave change summary and verification proof.
4. GS-MP contract migration report.
5. Residual legacy naming report.
6. Decommission report for removed compatibility layers.
7. Final sign-off summary.

## Existing references to update
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/runtime-naming-cleanup/README.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/runtime-naming-cleanup/05-runtime-class-string-inventory.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/runtime-naming-cleanup/06-runtime-config-template-script-inventory.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/runtime-naming-cleanup/07-safe-rename-execution-plan-with-compatibility-mapping.md`

## Evidence file naming standard
- `rn-<wave>-<area>-<YYYYMMDD-HHMMSS>.md`
- `rn-<wave>-<area>-<YYYYMMDD-HHMMSS>.log`
- `rn-<wave>-<area>-<YYYYMMDD-HHMMSS>.txt`

## Minimum evidence set before sign-off
- Fresh inventory proving current state.
- Mapping validation output.
- Runtime proof for launch/wallet/multiplayer after latest wave.
- Full local verification suite report.
- Residual scan showing only approved leftovers (or none).
- Rollback and decommission evidence.

## Review checklist
- Every wave has pre-check and post-check evidence.
- Every risky rename has rollback steps.
- Any retained legacy token has a clear reason and target removal plan.

## Storage location
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence`
