# Phase 5 Wallet Adapter Runtime Evidence (20260224-164350 UTC)

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
FAIL: reserve returned HTTP 502
Reserve body: {"error":{"retryable":true,"traceId":"e3c96faa-b07f-4942-a616-44d6f58a55c8","code":"WALLET_GATEWAY_ERROR","details":{},"message":"TransportException[message=Invalid response code: 500, Internal Server Error, statusCode=500, reasonPhrase='Internal Server Error', messageBody='Internal Server Error']"}}
```
