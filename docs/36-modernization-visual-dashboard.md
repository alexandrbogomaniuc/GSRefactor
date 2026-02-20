# Temporary Visual Progress Dashboard

## Purpose
Provide a visual control board for full GS modernization progress:
- legacy functionality coverage,
- milestones by phase,
- overall completion tracking.

## Files
- HTML page:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- Checklist source:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`

Runtime copies:
- `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/support/modernizationProgress.html`
- `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/support/data/modernization-checklist.json`

## Access
- Support index link:
  - `/support/modernizationProgress.html`

## Behavior
- Section-level and overall progress bars.
- Checkbox tracking per item.
- Filter modes: all/open/done.
- Reset local overrides.

## Important note
- Default statuses come from `modernization-checklist.json` (source of truth for baseline).
- Checkbox toggles are local/browser-side temporary overrides (localStorage), intended for visual tracking.
