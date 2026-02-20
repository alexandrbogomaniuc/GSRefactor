# Phase 6 Multiplayer Runtime Evidence (20260220-190732 UTC)

- bankId: 6275
- gameId: 838
- transport: host
- multiplayerBaseUrl: http://127.0.0.1:18079
- readiness_check: FAIL
- multiplayer_canary_probe: SKIPPED

## Readiness Output
```text
Phase 6 Multiplayer Runtime Readiness
  multiplayer-service: 127.0.0.1:18079
  gs:                 127.0.0.1:18081
FAIL multiplayer-service endpoint unreachable (127.0.0.1:18079)
FAIL gs endpoint unreachable (127.0.0.1:18081)
FAIL docker socket not accessible
NOT_READY: fix failed checks before running phase6 multiplayer canary probe
```

## Canary Output
```text
Canary probe not executed because readiness check failed.
```
