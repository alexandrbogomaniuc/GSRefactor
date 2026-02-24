# Phase 5 Wallet Adapter Runtime Evidence (20260224-163436 UTC)

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
FAIL: wallet-adapter canary route decision is not enabled for bank 6275
Decision payload: {"routeEnabled":false,"canaryBanks":["6275"],"bankId":"6275","routeToWalletAdapter":false,"reason":"route_disabled"}
```
