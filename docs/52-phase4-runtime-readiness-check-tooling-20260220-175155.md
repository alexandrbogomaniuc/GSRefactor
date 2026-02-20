# Phase 4 Runtime Readiness Check Tooling (2026-02-20 17:51:55 UTC)

## Goal
Provide a fast preflight check before running Phase 4 protocol evidence scripts.

## Delivered
- Script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-runtime-readiness-check.sh`
- Checks:
  1. protocol-adapter host/port reachable
  2. GS host/port reachable
  3. optional Docker socket accessibility

## Verification
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-runtime-readiness-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-runtime-readiness-check.sh --help
```

## Usage
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-runtime-readiness-check.sh \
  --protocol-host 127.0.0.1 --protocol-port 18078 \
  --gs-host 127.0.0.1 --gs-port 18081
```
