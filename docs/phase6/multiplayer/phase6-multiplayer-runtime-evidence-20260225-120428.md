# Phase 6 Multiplayer Runtime Evidence (20260225-120428 UTC)

- bankId: 6275
- gameId: 838
- transport: host
- multiplayerBaseUrl: http://127.0.0.1:18079
- runSyncCanary: false
- readiness_check: PASS
- multiplayer_routing_policy_probe: PASS
- multiplayer_canary_probe: SKIPPED

## Readiness Output
```text
Phase 6 Multiplayer Runtime Readiness
  multiplayer-service: 127.0.0.1:18079
  gs:                 127.0.0.1:18081
PASS multiplayer-service endpoint reachable
PASS gs endpoint reachable
PASS docker socket accessible
READY: runtime checks passed
```

## Routing Policy Probe Output
```text
Multiplayer routing policy probe summary
  bankId: 6275
  gameId: 838
  sessionId: mp-policy-session-1772021068
  non-mp: route=false reason=non_multiplayer_game
  mp:     route=false reason=bank_multiplayer_disabled
PASS: multiplayer routing policy (isMultiplayer bypass + bank capability gate) verified.
```

## Canary Output
```text
Canary probe not executed because --run-sync-canary=false (default safe mode).
```
