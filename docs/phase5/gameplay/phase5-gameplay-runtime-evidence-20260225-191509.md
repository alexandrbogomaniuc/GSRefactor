# Phase 5 Gameplay Runtime Evidence (20260225-191509 UTC)

- bankId: 6275
- gameId: 838
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- gameplayBaseUrl: http://127.0.0.1:18074
- subCasinoId: 507
- requireRedisHit: false
- readiness_check: PASS
- gameplay_canary_probe: PASS

## Readiness Output
```text
Phase 5 Runtime Readiness
  gameplay: 127.0.0.1:18074
  gs:       127.0.0.1:18081
  redis:    127.0.0.1:16379
PASS gameplay endpoint reachable
PASS gs endpoint reachable
PASS redis endpoint reachable
PASS docker socket accessible
READY: runtime checks passed
```

## Canary Output
```text
Gameplay canary probe summary
  transport: host
  bankId: 6275
  decision: routeToGameplayService=true
  sessionId: 1_19418660c00c94aee5250000019c9eb9_CxlDBwQB
  launch_http: 302
  launch_intents_before: 14
  launch_intents_after: 15
  financial_check: ENABLED
  reserve_http: 200
  settle_http: 200
  walletOperationId: ngs-1772046910476007
  wager_intents_before: 8
  wager_intents_after: 9
  settle_intents_before: 7
  settle_intents_after: 8
  state_cache_configured_backend: redis
  state_blob_cache_backend: redis
  state_blob_degraded_from_redis: false
  state_blob_fingerprint: 341b1ca70dde463a016c252ff41be16ae6f23350a036edff037395104a426ac2
PASS: gameplay-orchestrator launch, financial, and state-blob shadow flows verified.
```
