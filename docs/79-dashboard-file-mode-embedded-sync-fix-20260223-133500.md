# Dashboard File-Mode Embedded Sync Fix (2026-02-23)

## What was done
- Added reusable script `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh` to sync the dashboard's embedded checklist/outbox JSON snapshots from the current source JSON files.
- Ran the sync so `modernizationProgress.html` embedded data now matches:
  - `support/data/modernization-checklist.json`
  - `support/data/session-outbox-health.json`
- Added a footer note in `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html` explaining `file://` behavior and the sync command.

## Why this mattered
- The dashboard opened via `file://` intentionally reads embedded JSON (browser `fetch` restrictions on local files).
- The embedded snapshot had drifted (`20/35`) vs current checklist JSON (`26/41`), so the dashboard looked stale even though the checklist JSON was newer.

## Validation
- `bash -n` and `--help` passed for the sync script.
- Node verification confirmed embedded checklist/outbox now exactly match source JSON.
- Browser file-mode check confirmed dashboard renders and shows progress:
  - `26/41 completed (63%)`
  - source: `embedded-checklist`
