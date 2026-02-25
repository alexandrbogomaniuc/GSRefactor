# Phase 5 Wallet Adapter Runtime Evidence (20260225-114233 UTC)

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
PASS docker socket accessible
READY: runtime checks passed
```

## Canary Output
```text
FAIL: settle returned HTTP 400
Settle body: {"error":{"retryable":false,"traceId":"ff9be35b-1b06-4479-bb18-72a72a9b9807","code":"BAD_REQUEST","details":{},"message":"Mismatch sessionId. (received:1_1f07c8e40dad948cc3c10000019cb364_CxlDBwQB; expected:1_4f01a36e9322948cc3c30000019c8d07_CxlDBwQB)"}}
```
