# Phase 5 Bonus/FRB Runtime Evidence (20260225-191548 UTC)

- bankId: 6275
- transport: host
- bonusBaseUrl: http://127.0.0.1:18076
- readiness_check: PASS
- bonus_frb_canary_probe: PASS

## Readiness Output
```text
Phase 5 Bonus/FRB Runtime Readiness
  bonus-frb-service: 127.0.0.1:18076
  gs:                127.0.0.1:18081
PASS bonus-frb-service endpoint reachable
PASS gs endpoint reachable
PASS docker socket accessible
READY: runtime checks passed
```

## Canary Output
```text
Bonus/FRB canary probe summary
  bankId: 6275
  accountId: canary-account-1772046948
  frbId: canary-frb-1772046948
  decision: routeToBonusFrbService=true
PASS: bonus-frb canary check/consume/release flow verified.
```
