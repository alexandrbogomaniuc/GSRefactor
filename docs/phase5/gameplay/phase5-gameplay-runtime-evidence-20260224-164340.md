# Phase 5 Gameplay Runtime Evidence (20260224-164340 UTC)

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
FAIL: reserve returned HTTP 502
Reserve body: {"error":{"retryable":true,"traceId":"ec70394d-b19c-4edd-b760-3a6562695915","code":"WALLET_GATEWAY_ERROR","details":{},"message":"TransportException[message=Invalid response code: 500, Internal Server Error, statusCode=500, reasonPhrase='Internal Server Error', messageBody='Internal Server Error']"}}
```
