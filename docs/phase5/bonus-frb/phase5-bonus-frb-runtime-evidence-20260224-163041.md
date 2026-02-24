# Phase 5 Bonus/FRB Runtime Evidence (20260224-163041 UTC)

- bankId: 6275
- transport: host
- bonusBaseUrl: http://127.0.0.1:18076
- readiness_check: PASS
- bonus_frb_canary_probe: FAIL

## Readiness Output
```text
Phase 5 Bonus/FRB Runtime Readiness
  bonus-frb-service: 127.0.0.1:18076
  gs:                127.0.0.1:18081
PASS bonus-frb-service endpoint reachable
PASS gs endpoint reachable
READY: runtime checks passed
```

## Canary Output
```text
FAIL: bonus-frb-service canary route decision is not enabled for bank 6275
Decision payload: {"routeEnabled":false,"canaryBanks":["6275"],"bankId":"6275","routeToBonusFrbService":false,"reason":"route_disabled"}
```
