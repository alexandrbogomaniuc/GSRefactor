# Error Taxonomy v1

Last updated: 2026-02-20 UTC
Scope: GS modernization compatibility facade and extracted services.

## 1) Categories
1. `validation`: request/contract parameter issues.
2. `auth`: token/session identity failures.
3. `state`: invalid state transitions, session ownership mismatch.
4. `dependency`: wallet/cassandra/kafka/mp/upstream connectivity or timeout.
5. `rate_limit`: request throttling and protection controls.
6. `internal`: unhandled server errors.

## 2) Severity Mapping
1. `SEV-1`: financial inconsistency risk or production outage.
2. `SEV-2`: major feature unavailable for one or more banks.
3. `SEV-3`: degraded behavior with workaround.
4. `SEV-4`: minor/no user-impact issue.

## 3) Canonical Error Envelope
```json
{
  "code": "<stable_code>",
  "category": "validation|auth|state|dependency|rate_limit|internal",
  "message": "<safe message>",
  "retryable": false,
  "traceId": "<trace-id>",
  "operationId": "<optional-op-id>",
  "details": {}
}
```

## 4) Initial Stable Codes
- `VAL-001`: missing required parameter.
- `VAL-002`: invalid parameter format/range.
- `AUTH-001`: invalid or expired token.
- `AUTH-002`: bank/session ownership mismatch.
- `STATE-001`: invalid gameplay/session transition.
- `STATE-002`: duplicate operation replay detected (idempotent return path).
- `DEP-001`: wallet timeout/unavailable.
- `DEP-002`: datastore timeout/unavailable.
- `DEP-003`: message bus unavailable.
- `INT-001`: unexpected server exception.

## 5) Routing Rules
1. Never expose stack traces or secrets in error payload.
2. Keep `code` stable; `message` may be localized.
3. Emit `traceId` on every error for correlation.
4. Set `retryable=true` only for dependency/transient classes with safe retry semantics.

## 6) Runbook Linkage (Initial)
- `validation/auth`: request correction path + client contract checks.
- `state`: session replay/reconnect diagnostic flow.
- `dependency`: dependency health checks and failover/rollback gate.
- `internal`: incident capture and temporary bank rollback if impact threshold is crossed.
