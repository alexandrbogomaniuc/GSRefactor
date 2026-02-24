# Phase 5 History Runtime Evidence (20260224-130303 UTC)

- bankId: 6275
- transport: host
- historyBaseUrl: http://127.0.0.1:18077
- readiness_check: PASS
- history_canary_probe: FAIL

## Readiness Output
```text
Phase 5 History Runtime Readiness
  history-service: 127.0.0.1:18077
  gs:              127.0.0.1:18081
PASS history-service endpoint reachable
PASS gs endpoint reachable
PASS docker socket accessible
READY: runtime checks passed
```

## Canary Output
```text
FAIL: history-service canary route decision is not enabled for bank 6275
Decision payload: {"routeEnabled":false,"canaryBanks":["6275"],"bankId":"6275","routeToHistoryService":false,"reason":"route_disabled"}
```
