# Phase 2 Correlation Probe Runbook

## Command
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase2-correlation-probe.sh --base-url http://127.0.0.1:18080
```

Optional:
- `--wait-ready-sec 60` to wait for non-`502` static readiness before probing.

## Output
- Report: `correlation-probe-<timestamp>.md`
- Raw headers: `correlation-headers-<timestamp>.txt`
- Body: `correlation-body-<timestamp>.txt`

## Current baseline
- Latest known validation: `correlation-probe-20260220-104035.md`
- Status: request path reachable and correlation echo headers are `PASS`.
