# Phase 9 W0 Approval Digest Binding For Exact Patch-Plan Verification (2026-02-24 11:45 UTC)

## What was done
- Extended W0 approval artifact generation to include `patchPlanSha256` (SHA-256 of the reviewed patch-plan content):
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-approval-artifact-generate.sh`
- Extended W0 apply executor to verify the approval artifact hash against the exact patch-plan content before apply execution:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh`
- Updated smokes to cover digest presence and digest-mismatch blocking.

## Real GS W0 digest-binding evidence
- Real digest-bound approval artifact:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-apply-approval-20260224-100928.json`
  - contains `patchPlanSha256=e584ec6b0e983d88e2d4f3d4cb60ec3a43bbf5c254b9fa90e4ad8620154ea830`
- Tampered-hash artifact (for guard validation):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-apply-approval-20260224-100928-badhash.json`
- Real apply attempt blocked on digest mismatch (no file changes):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-text-replace-apply-blocked-digest-mismatch-20260224-100930.log`
  - error starts with: `FAIL: approval artifact patchPlanSha256 mismatch...`

## Why this matters
- Closes a safety gap where basename/path + allowlist matched but patch-plan content could be changed after review.
- Apply mode is now bound to the exact reviewed patch-plan content, not just the file name.
- This is the last major guard needed before attempting a tiny real W0 apply wave on a minimal approved subset.
