# Phase 5 Wallet Adapter Runtime Evidence (20260225-194123 UTC)

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
  sessionId: 1_d9eb8471f0af94aee52d0000019c97a1_CxlDBwQB
  roundId: 699f506300001e7d
  reserve_http: 200
  settle_http: 200
  walletOperationId: ngs-1772048483948748
  reserve_ops_before: 12
  reserve_ops_after: 13
  settle_ops_before: 11
  settle_ops_after: 12
PASS: wallet-adapter reserve/settle shadow observed.
```
