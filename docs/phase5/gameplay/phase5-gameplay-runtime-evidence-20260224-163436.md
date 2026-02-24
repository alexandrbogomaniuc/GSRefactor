# Phase 5 Gameplay Runtime Evidence (20260224-163436 UTC)

- bankId: 6275
- gameId: 838
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- gameplayBaseUrl: http://127.0.0.1:18074
- subCasinoId: 507
- requireRedisHit: false
- readiness_check: PASS
- gameplay_canary_probe: FAIL

## Readiness Output
```text
Phase 5 Runtime Readiness
  gameplay: 127.0.0.1:18074
  gs:       127.0.0.1:18081
  redis:    127.0.0.1:16379
PASS gameplay endpoint reachable
PASS gs endpoint reachable
PASS redis endpoint reachable
READY: runtime checks passed
```

## Canary Output
```text
FAIL: gameplay canary route decision is not enabled for bank 6275
Decision payload: {"routeEnabled":false,"canaryBanks":["6275"],"bankId":"6275","isMultiplayer":false,"routeToGameplayService":false,"reason":"eligible"}
```
