# Phase 9 W0 Safe-Target Path Filter Profile for Candidate Scan (2026-02-24 10:15 UTC)

## What was done
- Extended `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh` with `--safe-targets-only true|false`.
- Added W0-oriented low-risk path filtering in scanner output (docs/config/templates and similar text config paths) while excluding Java code paths from early rename candidate reports.
- Updated smoke test to verify safe-path inclusion and Java-path exclusion.

## Why
- W0 is intended for low-risk/non-runtime string cleanup. A raw candidate scan over-reports code-path hits and makes safe execution planning noisy.
- This narrows W0 candidate outputs before any rename automation is considered.

## Real GS W0 safe-target evidence
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-scan-20260224-093307.md`
- Result highlights:
  - `Safe targets only: true`
  - `Total line hits: 632` (down from full-scan 1658)
  - `Filtered-out line hits (path profile): 1026`
  - `Auto-apply status: BLOCKED`
  - `Block reason: BLOCKED_REVIEW_ONLY:mq`

## Outcome
- W0 candidate reporting is now significantly cleaner while preserving the review-only block on `mq`.
- Next refinement: move path profiles into the Phase 9 manifest so wave filters are data-driven instead of scanner defaults.
