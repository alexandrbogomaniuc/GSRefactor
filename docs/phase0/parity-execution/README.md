# Phase 0 Parity Harness Runbook

## Dry-run (safe default)
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase0-parity-harness.sh \
  --mode dry-run \
  --base-url http://localhost:18080 \
  --fixture-file docs/phase0/parity-fixture.env.example
```

## Run mode (requires full wallet fixture)
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase0-parity-harness.sh \
  --mode run \
  --base-url http://localhost:18080 \
  --fixture-file docs/phase0/parity-fixture.env
```

## Output
- Reports: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/phase0-parity-<timestamp>.md`
- HTTP response snippets (run mode): `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/P0-*.body.txt`

## Notes
- Harness now validates response body contracts (`PASS_CONTRACT` / `FAIL_CONTRACT`), not HTTP status alone.
- Deterministic negative probes (`P0-LA-02`, `P0-WA-00`, `P0-SE-00`) run without wallet-positive fixture data and provide a stable baseline.

## Hash helper (bonus endpoints)
Use GS-aligned hash composition rules to generate fixture hash values:
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase0-bonus-hash-helper.sh \
  --mode check \
  --ext-bonus-id 1 \
  --external-bank-id 6274 \
  --bonus-pass-key <BANK_BONUS_PASS_KEY>
```
