# Phase 5 Gameplay Runtime Evidence (20260225-114233 UTC)

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
PASS docker socket accessible
READY: runtime checks passed
```

## Canary Output
```text
FAIL: reserve returned HTTP 400
Reserve body: {"error":{"retryable":false,"traceId":"f3e92c85-285b-46ad-bb00-80ebf5e3e6dd","code":"BAD_REQUEST","details":{},"message":"Mismatch sessionId. (received:1_ee9751ff9be7948cc3c20000019cb2a5_CxlDBwQB; expected:1_4f01a36e9322948cc3c30000019c8d07_CxlDBwQB)"}}
```
