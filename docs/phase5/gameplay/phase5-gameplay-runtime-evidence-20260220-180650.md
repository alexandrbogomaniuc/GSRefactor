# Phase 5 Gameplay Runtime Evidence (20260220-180650 UTC)

- bankId: 6275
- gameId: 838
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- gameplayBaseUrl: http://127.0.0.1:18074
- requireRedisHit: false
- readiness_check: FAIL
- gameplay_canary_probe: SKIPPED

## Readiness Output
```text
Phase 5 Runtime Readiness
  gameplay: 127.0.0.1:18074
  gs:       127.0.0.1:18081
  redis:    127.0.0.1:16379
FAIL gameplay endpoint unreachable (127.0.0.1:18074)
FAIL gs endpoint unreachable (127.0.0.1:18081)
FAIL redis endpoint unreachable (127.0.0.1:16379)
FAIL docker socket not accessible
NOT_READY: fix failed checks before running phase5 gameplay canary probe
```

## Canary Output
```text
Canary probe not executed because readiness check failed.
```
