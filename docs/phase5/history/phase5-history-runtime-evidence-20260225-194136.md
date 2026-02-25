# Phase 5 History Runtime Evidence (20260225-194136 UTC)

- bankId: 6275
- transport: host
- historyBaseUrl: http://127.0.0.1:18077
- readiness_check: PASS
- history_canary_probe: PASS

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
History canary probe summary
  bankId: 6275
  sessionId: canary-session-1772048496
  eventType: round_settle
  decision: routeToHistoryService=true
PASS: history canary append/query flow verified.
```
