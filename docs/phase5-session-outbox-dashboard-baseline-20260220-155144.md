# Phase 5 Session Outbox Dashboard + Canary Baseline Tightening (2026-02-20 15:51:44 UTC)

## Goal
Expose session outbox safety state directly in modernization dashboard and tighten canary alert gates for NEW/RETRY/DLQ trends.

## Delivered
1. Dashboard visibility for outbox canary safety
- Added outbox health card and JSON data source:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/session-outbox-health.json`
- Card shows NEW/RETRY/DLQ/replay-429 values, thresholds, trend labels, update timestamp, and source.
- Added architecture checklist item:
  - `ar-outbox-canary-gate` in both embedded and external checklist JSON.

2. Outbox alert script baseline tightening + trend gate
- Updated script defaults:
  - `MAX_NEW=10`, `MAX_RETRY=2`, `MAX_DLQ=0`
- Added optional trend sampling:
  - `--sample-count N`
  - `--sample-interval-sec N`
- Added strict increasing trend detection for NEW/RETRY samples (fails fast).
- File:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-outbox-alert-check.sh`

3. Canary governance docs aligned to enforceable command
- Updated policy threshold command to 15-minute window sampling:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/26-bank-canary-policy-v1.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/35-session-service-canary-routing-policy.md`

4. Runtime file sync for support UI assets
- Copied updated files into runtime-mounted support directory:
  - `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/support/modernizationProgress.html`
  - `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/support/data/modernization-checklist.json`
  - `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/support/data/session-outbox-health.json`

## Verification
1. Static checks
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-outbox-alert-check.sh
node -e "const fs=require('fs');JSON.parse(fs.readFileSync('/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/session-outbox-health.json','utf8'));JSON.parse(fs.readFileSync('/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json','utf8'));console.log('json-ok')"
```

2. Presence checks
```bash
rg -n "ar-outbox-canary-gate|Session Outbox Canary Health|OUTBOX_HEALTH_URL|sample-count" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-outbox-alert-check.sh \
  /Users/alexb/Documents/Dev/Dev_new/docs/26-bank-canary-policy-v1.md \
  /Users/alexb/Documents/Dev/Dev_new/docs/35-session-service-canary-routing-policy.md -S
```

## Notes
- Runtime HTTP check to `http://localhost/support/...` was not available in current sandbox (no listener on port 80).
- Docker-backed runtime execution remains blocked in this sandbox by docker socket permission denial.
