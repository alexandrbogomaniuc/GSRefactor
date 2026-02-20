# Phase 5 Bonus/FRB Runtime Evidence (20260220-185313 UTC)

- bankId: 6275
- transport: host
- bonusBaseUrl: http://127.0.0.1:18076
- readiness_check: FAIL
- bonus_frb_canary_probe: SKIPPED

## Readiness Output
```text
Phase 5 Bonus/FRB Runtime Readiness
  bonus-frb-service: 127.0.0.1:18076
  gs:                127.0.0.1:18081
FAIL bonus-frb-service endpoint unreachable (127.0.0.1:18076)
FAIL gs endpoint unreachable (127.0.0.1:18081)
FAIL docker socket not accessible
NOT_READY: fix failed checks before running phase5 bonus/frb canary probe
```

## Canary Output
```text
Canary probe not executed because readiness check failed.
```
