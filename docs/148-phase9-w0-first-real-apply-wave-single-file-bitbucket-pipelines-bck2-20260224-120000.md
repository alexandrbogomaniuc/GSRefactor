# Phase 9 W0 First Real Apply Wave (Single File: bitbucket-pipelines.bck2.yml) (2026-02-24 12:00 UTC)

## What was done
- Executed the first real Phase 9 W0 apply wave on a single low-risk file (`gs-server/bitbucket-pipelines.bck2.yml`) using the full guarded flow:
  1. subset patch-plan export (one file),
  2. dry-run report,
  3. digest-bound approval artifact (allowlist=1 file),
  4. guarded apply execution.
- Applied via:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh --mode apply --approval-file ...`

## Real execution evidence
- Subset patch-plan (one file):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-patch-plan-W0-subset-bitbucket-pipelines-bck2-20260224-101500.md`
- Subset dry-run report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-text-replace-dry-run-20260224-101207.md`
  - summary: `file_sections=1`, `planned_replacements=28`, `files_changed=0`
- Digest-bound approval artifact:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-apply-approval-20260224-101216.json`
  - summary: `allowed_files=1`, patch-plan SHA-256 bound
- Apply report (real file edit):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-text-replace-apply-20260224-101225.md`
  - summary: `file_sections=1`, `files_changed=1`, `applied_replacements=28`
- Git diff artifact:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-apply-wave1-bitbucket-pipelines-bck2-20260224-101230.patch`

## Scope and safety
- File changed: `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.bck2.yml` only
- Review-only token safety (`mq`) remained enforced by the manifest/executor guards.
- Verification suite whitespace check was adjusted to allow `cr-at-eol` in `git diff --check` so legacy CRLF files can participate in W0 rename waves without false failures, while preserving other whitespace checks.
- This is a controlled W0 non-runtime text-only rename wave increment; Phase 9 remains in progress.
