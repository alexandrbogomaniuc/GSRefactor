# Phase 5 Wallet Adapter Runtime Evidence (20260220-184505 UTC)

- bankId: 6275
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- walletBaseUrl: http://127.0.0.1:18075
- readiness_check: FAIL
- wallet_canary_probe: SKIPPED

## Readiness Output
```text
Phase 5 Wallet Runtime Readiness
  wallet-adapter: 127.0.0.1:18075
  gs:             127.0.0.1:18081
FAIL wallet-adapter endpoint unreachable (127.0.0.1:18075)
FAIL gs endpoint unreachable (127.0.0.1:18081)
FAIL docker socket not accessible
NOT_READY: fix failed checks before running phase5 wallet canary probe
```

## Canary Output
```text
Canary probe not executed because readiness check failed.
```
