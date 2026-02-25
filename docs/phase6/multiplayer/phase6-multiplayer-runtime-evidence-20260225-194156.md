# Phase 6 Multiplayer Runtime Evidence (20260225-194156 UTC)

- bankId: 6274
- gameId: 838
- transport: host
- multiplayerBaseUrl: http://127.0.0.1:18079
- runSyncCanary: true
- policyExpectBankMpEnabled: true
- policyExpectNonMpRoute: false
- policyExpectNonMpReason: non_multiplayer_game
- policyExpectMpRoute: true
- policyExpectMpReason: eligible
- readiness_check: PASS
- multiplayer_routing_policy_probe: PASS
- multiplayer_canary_probe: PASS

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
  bankId: 6274
  gameId: 838
  sessionId: mp-policy-session-1772048516
  non-mp: route=false reason=non_multiplayer_game
  mp:     route=true reason=eligible
PASS: multiplayer routing policy (isMultiplayer bypass + bank capability gate) verified.
```

## Canary Output
```text
Multiplayer canary probe summary
  bankId: 6274
  gameId: 838
  sessionId: mp-canary-session-1772048516
  decision: routeToMultiplayerService=true
PASS: multiplayer routing+sync flow verified.
```
