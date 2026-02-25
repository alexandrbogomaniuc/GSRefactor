# Phase 5 Gameplay Runtime Evidence (20260225-194115 UTC)

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
  sessionId: 1_ddb4a6226a2994aee52c0000019caa23_CxlDBwQB
  launch_http: 302
  launch_intents_before: 21
  launch_intents_after: 22
  financial_check: ENABLED
  reserve_http: 200
  settle_http: 200
  walletOperationId: ngs-1772048476736727
  wager_intents_before: 11
  wager_intents_after: 12
  settle_intents_before: 10
  settle_intents_after: 11
  state_cache_configured_backend: redis
  state_blob_cache_backend: redis
  state_blob_degraded_from_redis: false
  state_blob_fingerprint: 341b1ca70dde463a016c252ff41be16ae6f23350a036edff037395104a426ac2
PASS: gameplay-orchestrator launch, financial, and state-blob shadow flows verified.
```
