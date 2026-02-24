# Phase 4 Runtime Evidence Pack: Degraded Readiness Classification

Date: 2026-02-24
Status: Implemented and tested

## Goal
- Avoid false Phase 4 runtime parity failures when the protocol-adapter runtime is not deployed/reachable yet.
- Produce accurate blocked/degraded evidence while preserving strict behavior.

## Delivered
- Updated `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh`
  - adds runtime readiness section (calls `phase4-runtime-readiness-check.sh`)
  - adds `--allow-missing-runtime true|false`
  - skips runtime probes as `SKIP_RUNTIME_NOT_READY` when readiness fails in allow-missing mode
  - classifies connectivity/container-unavailable probe failures as `SKIP_RUNTIME_UNAVAILABLE` in allow-missing mode
  - keeps strict failure behavior when `--allow-missing-runtime=false`
- Added smoke test:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack-degraded-smoke.sh`
- Added verification-suite coverage:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`

## Real Runtime Evidence (current environment)
Executed:

```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --transport host \
  --allow-missing-runtime true
```

Observed report output:
- `runtime_readiness: FAIL`
- `parity_check: SKIP_RUNTIME_NOT_READY`
- `wallet_shadow_probe: SKIP_RUNTIME_NOT_READY`
- `json_security_probe: SKIPPED`

Evidence report:
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol/phase4-protocol-runtime-evidence-20260224-091020.md`

## Validation
- `bash -n` on updated/new scripts
- degraded smoke test passes
- full local verification suite passes after integration

## Next Step
- Start `protocol-adapter` in the refactor container group and rerun the same evidence pack in strict mode to collect real parity + runtime canary evidence.
