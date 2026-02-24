# Phase 5 Wallet Adapter Runtime Evidence (20260224-164808 UTC)

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
Wallet-adapter shadow canary summary
  bankId: 6275
  sessionId: 2_d6a79fe9873f908818bf0000019c95c6_CxlDBwQB
  roundId: 699dd64800001bca
  reserve_http: 200
  settle_http: 200
  walletOperationId: ngs-1771951688600017
  reserve_ops_before: 0
  reserve_ops_after: 0
  settle_ops_before: 0
  settle_ops_after: 0
FAIL: no new reserve operation observed in wallet-adapter
```
