# Phase 5 Gameplay Runtime Evidence (20260224-164243 UTC)

- bankId: 6275
- gameId: 838
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- gameplayBaseUrl: http://127.0.0.1:18074
- subCasinoId: 507
- requireRedisHit: false
- readiness_check: PASS
- gameplay_canary_probe: FAIL

## Readiness Output
```text
Phase 5 Runtime Readiness
  gameplay: 127.0.0.1:18074
  gs:       127.0.0.1:18081
  redis:    127.0.0.1:16379
PASS gameplay endpoint reachable
PASS gs endpoint reachable
PASS redis endpoint reachable
READY: runtime checks passed
```

## Canary Output
```text
FAIL: reserve returned HTTP 400
Reserve body: {"error":{"retryable":false,"traceId":"fb8995e0-5f7d-4e64-885e-a3c988325570","code":"BAD_REQUEST","details":{},"message":"Mismatch sessionId. (received:2_7f29a683c631908818b70000019cbd33_CxlDBwQB; expected:2_98a999d30dab908818b80000019c8e42_CxlDBwQB)"}}
```
