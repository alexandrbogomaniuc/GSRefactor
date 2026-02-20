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
