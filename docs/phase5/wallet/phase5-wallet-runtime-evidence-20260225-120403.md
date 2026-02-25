# Phase 5 Wallet Adapter Runtime Evidence (20260225-120403 UTC)

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
  sessionId: 1_f3b7f0319beb94aee5100000019ca941_CxlDBwQB
  roundId: 699ee53300006d3a
  reserve_http: 200
  settle_http: 200
  walletOperationId: ngs-1772021043372102
  reserve_ops_before: 2
  reserve_ops_after: 3
  settle_ops_before: 1
  settle_ops_after: 2
PASS: wallet-adapter reserve/settle shadow observed.
```
