# Phase 4 Protocol Runtime Evidence (20260224-164243 UTC)

- bankId: 6275
- transport: host
- protocolBaseUrl: http://127.0.0.1:18078
- gsBaseUrl: http://127.0.0.1:18081
- subCasinoId: 507
- token: bav_game_session_001
- allowMissingRuntime: false
- runtime_readiness: PASS
- parity_check: PASS
- wallet_shadow_probe: FAIL
- json_security_probe: SKIPPED

## Runtime Readiness Output
```text
Phase 4 Runtime Readiness
  transport: host
  protocol: 127.0.0.1:18078
  gs:       127.0.0.1:18081
PASS protocol endpoint reachable
PASS gs endpoint reachable
READY: runtime checks passed
```

## Parity Check Output
```text
PARITY_OK bankId=6275 endpoint=/wallet/reserve
JSON/XML parity check passed for bank 6275 (POST /wallet/reserve) transport=host
```

## Wallet Shadow Probe Output
```text
FAIL: reserve returned HTTP 502
Reserve body: {"error":{"retryable":true,"traceId":"fcf68e8c-ad89-42fd-9484-8c5fbdcc3947","code":"WALLET_GATEWAY_ERROR","details":{},"message":"TransportException[message=Invalid response code: 500, Internal Server Error, statusCode=500, reasonPhrase='Internal Server Error', messageBody='Internal Server Error']"}}
```

## JSON Security Probe Output
```text
Security probe not executed because --run-security-probe=false (default safe mode).
```
