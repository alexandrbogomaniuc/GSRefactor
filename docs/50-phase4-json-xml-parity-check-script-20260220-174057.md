# Phase 4 JSON/XML Parity Check Script (2026-02-20 17:40:57 UTC)

## Goal
Add executable parity assertion for protocol-adapter canonical normalization between `XML` and `JSON` bank modes.

## Delivered
- Script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh`
- Behavior:
  - snapshots current bank protocol settings,
  - runs normalize request in `XML` mode,
  - runs same normalize request in `JSON` mode,
  - asserts canonical payload parity (excluding `protocolMode` and timestamp),
  - restores original bank settings on exit.

## Verification commands
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh --help
```

## Usage example
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh \
  --bank-id 6275 \
  --base-url http://127.0.0.1:18078 \
  --endpoint /wallet/reserve \
  --method POST
```
