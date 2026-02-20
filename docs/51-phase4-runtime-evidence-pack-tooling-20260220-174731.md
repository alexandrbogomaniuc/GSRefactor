# Phase 4 Runtime Evidence Pack Tooling (2026-02-20 17:47:31 UTC)

## Goal
Provide one command to run Phase 4 protocol runtime checks and capture operator-ready evidence.

## Delivered
- Script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh`
- Checks executed by the script:
  1. JSON/XML parity check
     - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh`
  2. Wallet reserve/settle shadow probe
     - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh`
- Output:
  - report markdown in `/Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol/phase4-protocol-runtime-evidence-<timestamp>.md`

## Verification
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh --help
```

## Usage
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --base-url http://127.0.0.1:18078
```

## Latest execution evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol/phase4-protocol-runtime-evidence-20260220-175051.md`
- Observed blockers in this environment:
  - protocol-adapter endpoint not reachable on `127.0.0.1:18078`,
  - Docker socket permission denied (`unix:///Users/alexb/.docker/run/docker.sock`) for wallet probe in Docker mode.
- Follow-up implemented:
  - wallet probe now supports host transport mode to bypass Docker socket access.
