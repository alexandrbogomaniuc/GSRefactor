# Phase 9 W0 Apply Approval Artifact And File Allowlist Guard (2026-02-24 11:30 UTC)

## What was done
- Extended the Phase 9 W0 text replacement executor to require an explicit approval artifact for `--mode apply`:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh`
- Added approval artifact generator (JSON, versioned, file allowlist, patch-plan link):
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-approval-artifact-generate.sh`
- Added end-to-end smoke coverage for approval generation + guarded apply:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-approval-artifact-generate-smoke.sh`
- Updated existing W0 text-replace smoke to use the approval artifact flow.

## Real GS W0 guard evidence
- Real apply attempt without approval (blocked, no file changes):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-text-replace-apply-blocked-no-approval-20260224-095400.log`
  - error: `Missing --approval-file for --mode apply`
- Real generated approval artifact from W0 dry-run report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-apply-approval-20260224-100221.json`
  - summary: `allowed_files=19`, `approvalId=phase9-w0-review-001`
- Source dry-run report used to generate approval:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-text-replace-dry-run-20260224-095054.md`

## Why this matters
- Prevents accidental W0 file modification runs without an explicit approval step.
- Locks apply execution to a reviewed patch-plan and explicit file allowlist.
- Preserves review-only token safety (`mq`) and adds a practical operator gate before any real rename wave execution.
