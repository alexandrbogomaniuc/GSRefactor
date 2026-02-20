# Phase 5 History Runtime Evidence (20260220-190016 UTC)

- bankId: 6275
- transport: host
- historyBaseUrl: http://127.0.0.1:18077
- readiness_check: FAIL
- history_canary_probe: SKIPPED

## Readiness Output
```text
Phase 5 History Runtime Readiness
  history-service: 127.0.0.1:18077
  gs:              127.0.0.1:18081
FAIL history-service endpoint unreachable (127.0.0.1:18077)
FAIL gs endpoint unreachable (127.0.0.1:18081)
FAIL docker socket not accessible
NOT_READY: fix failed checks before running phase5 history canary probe
```

## Canary Output
```text
Canary probe not executed because readiness check failed.
```
