# Support Portal Runtime Sync Validation (2026-02-26 08:01 UTC)

## Why this check was needed
The support modernization page returned 404 in runtime even though source files existed. This could mislead stakeholders into thinking Milestone 3 assets were missing.

## Root cause (plain English)
The runtime `ROOT/support` folder was seeded earlier and not refreshed with latest source support files on later startups.

## What was changed
- Script updated: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-start.sh`
- New startup sync step now copies these source files into runtime on every `up`:
  - `support/modernizationProgress.html`
  - `support/modernizationRunbook.jsp`
  - `support/modernizationDocs.jsp`
  - `support/phase8DiscrepancyViewer.html`
  - `support/data/modernization-checklist.json`
  - `support/data/session-outbox-health.json`
  - `support/data/audit-requirements-status.json`
  - `support/data/audit-scope-summary.json`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080102/support-modernizationProgress-head.txt`
  - Shows: `HTTP/1.1 200 OK`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080102/startgame-head.txt`
  - Shows: `HTTP/1.1 200 OK`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080102/timestamp.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080102/refactor-onboard-smoke.log`
  - Shows all smoke checks passing (`HTTP 200`), including launch alias and support route.

## Conclusion
Support portal runtime delivery is restored and aligned with source assets during normal refactor startup.
