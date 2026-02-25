# Phase 4 Protocol Runtime Evidence (20260225-120340 UTC)

- bankId: 6275
- transport: host
- protocolBaseUrl: http://127.0.0.1:18078
- gsBaseUrl: http://127.0.0.1:18081
- subCasinoId: 507
- token: bav_game_session_001
- allowMissingRuntime: false
- runtime_readiness: PASS
- parity_check: PASS
- wallet_shadow_probe: PASS
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
Protocol wallet shadow canary summary
  bankId: 6275
  sessionId: 1_184bcd3d8f9f94aee50f0000019ca33a_CxlDBwQB
  roundId: 699ee51d00001d8e
  reserve_http: 200
  settle_http: 200
  walletOperationId: ngs-1772021021646219
  protocol_events_before: 30
  protocol_events_after: 32
PASS: protocol-adapter wallet reserve/settle shadow observed (30 -> 32).
```

## JSON Security Probe Output
```text
Security probe not executed because --run-security-probe=false (default safe mode).
```
