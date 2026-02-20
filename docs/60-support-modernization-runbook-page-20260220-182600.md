# Support Modernization Runbook Page (2026-02-20 18:26 UTC)

## What was done
- Added support page `/support/modernizationRunbook.jsp` with executable command sequences for:
  - Phase 4 protocol adapter readiness + evidence-pack,
  - Phase 5 gameplay/Redis readiness + evidence-pack.
- Wired new runbook page into support home (`/support/index.jsp`).
- Linked runbook page from modernization docs index (`/support/modernizationDocs.jsp`).

## Files changed
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationRunbook.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/index.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`

## Result
- Operators now have a single runbook page in the GS support UI for repeatable phase execution and evidence generation.
