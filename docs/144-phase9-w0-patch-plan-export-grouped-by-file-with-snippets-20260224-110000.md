# Phase 9 W0 Patch-Plan Export Grouped By File With Snippets (2026-02-24 11:00 UTC)

## What was done
- Added Phase 9 review-only per-file grouped patch-plan export generator:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-patch-plan-export.sh`
- Added smoke test and verification-suite coverage:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-patch-plan-export-smoke.sh`
- Export groups W0 auto-candidate mappings by file and adds snippet previews for operator review before any text-only replacement wave.

## Real GS W0 evidence
- Source profile scan report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-scan-20260224-093904.md`
- Generated patch-plan export:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-patch-plan-W0-20260224-094711.md`
- Summary:
  - `auto_candidate_mappings=5`
  - `grouped_files=19`
  - `review_only_hits=1` (`mq` excluded from file queue)

## Why this matters
- Converts mapping-centric planning into a file review queue, which is the practical unit for safe W0 edits.
- Preserves rename safety by keeping review-only tokens (`mq`) visible but excluded from executable patch planning.
- Provides snippet previews so operators can quickly detect risky runtime/config values before replacement work starts.
