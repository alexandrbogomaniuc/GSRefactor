# Phase 5 Wallet Adapter Runtime Evidence (20260225-191524 UTC)

- bankId: 6275
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- walletBaseUrl: http://127.0.0.1:18075
- subCasinoId: 507
- token: bav_game_session_001
- readiness_check: PASS
- wallet_canary_probe: PASS

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
Wallet-adapter shadow canary summary
  bankId: 6275
  sessionId: 1_ed4b10996d8194aee5260000019cb20d_CxlDBwQB
  roundId: 699f4a4c0000063f
  reserve_http: 200
  settle_http: 200
  walletOperationId: ngs-1772046924435072
  reserve_ops_before: 9
  reserve_ops_after: 10
  settle_ops_before: 8
  settle_ops_after: 9
PASS: wallet-adapter reserve/settle shadow observed.
```
