# Phase 9 Manifest Wave Path Profiles and Full-vs-Profile Diff Report (2026-02-24 10:30 UTC)

## What was done
- Added manifest-declared `pathProfiles` and linked each Phase 9 wave to a profile (`pathProfile`) in:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json`
- Upgraded candidate scanner to resolve the effective path profile from the manifest (with backward-compatible `--safe-targets-only` and optional `--path-profile`).
- Added full-vs-profile diff tool and smoke test:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-diff.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-diff-smoke.sh`
- Integrated new Phase 9 diff checks into the shared verification suite.

## Real GS W0 evidence
- Full scan report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-scan-20260224-093902.md`
- Wave-profile scan report (`w0_safe_targets`): `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-scan-20260224-093904.md`
- Diff report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-diff-W0-20260224-093902.md`

## Result highlights (W0)
- Profile used: `w0_safe_targets`
- Full hits: `1658`
- Profile hits: `632`
- Filtered-out hits: `1026`
- `mq` remains review-only and visible in profile scan, preserving the auto-apply safety gate.

## Why this matters
- Path-profile behavior is now data-driven in the manifest (not hardcoded scanner defaults).
- Operators can compare full vs wave-safe candidate sets before approving any rename automation.
