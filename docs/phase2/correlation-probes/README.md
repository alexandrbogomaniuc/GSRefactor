# Phase 2 Correlation Probe Runbook

## Command
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase2-correlation-probe.sh --base-url http://127.0.0.1:18080
```

## Output
- Report: `correlation-probe-<timestamp>.md`
- Raw headers: `correlation-headers-<timestamp>.txt`
- Body: `correlation-body-<timestamp>.txt`

## Current baseline
- Latest known baseline: `correlation-probe-20260220-100539.md`
- Status: request path is reachable, correlation echo headers are not present yet (pre-deploy baseline).
