# Phase 5 Wallet Adapter Runtime Evidence (20260224-164243 UTC)

- bankId: 6275
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- walletBaseUrl: http://127.0.0.1:18075
- subCasinoId: 507
- token: bav_game_session_001
- readiness_check: PASS
- wallet_canary_probe: FAIL

## Readiness Output
```text
Phase 5 Wallet Runtime Readiness
  wallet-adapter: 127.0.0.1:18075
  gs:             127.0.0.1:18081
PASS wallet-adapter endpoint reachable
PASS gs endpoint reachable
READY: runtime checks passed
```

## Canary Output
```text
FAIL: reserve returned HTTP 400
Reserve body: {"error":{"retryable":false,"traceId":"aa210b8d-7c92-4f5d-9eb4-ad7dcad5157e","code":"BAD_REQUEST","details":{},"message":"Mismatch sessionId. (received:2_3c0379c9a078908818b60000019cb995_CxlDBwQB; expected:2_7f29a683c631908818b70000019cbd33_CxlDBwQB)"}}
```
