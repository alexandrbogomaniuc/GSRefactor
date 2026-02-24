# Phase 9 ABS Rename Candidate Scan and Review-Only Block Gate (2026-02-24 10:00 UTC)

## What was done
- Added manifest-driven Phase 9 GS-scope candidate scanner:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh`
- Added smoke test that verifies review-only blocking for `mq`:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-scan-smoke.sh`
- Integrated scanner help/smoke into the shared local verification suite.

## Key behavior
- Produces wave-specific candidate reports from `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json`
- Supports `--enforce-auto-apply true` to block unsafe wave execution when review-only mappings have hits
- `mq` is blocked from auto-apply by design (`BLOCKED_REVIEW_ONLY:mq`)

## Real GS-scope evidence (W0)
- Report (blocked auto-apply run):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-scan-20260224-093041.md`
- Result summary:
  - `Auto-apply status: BLOCKED`
  - `Block reason: BLOCKED_REVIEW_ONLY:mq`
  - `Review-only mappings with hits: 1`
  - `mq` hits remain high and require context-filtered/manual treatment before any rename automation.

## Why this matters
- Prevents accidental broad replacements of short/high-collision tokens while still surfacing safe W0 rename candidates.
- Moves Phase 9 from inventory + plan to executable wave gating.
