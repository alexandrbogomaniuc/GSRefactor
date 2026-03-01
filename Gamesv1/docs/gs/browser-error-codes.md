# GS Browser Error Codes

Status: canonical browser-facing error code map for slot-browser-v1.

## Categories

1. `GS_AUTH_*`
- authentication/session/token errors

2. `GS_SEQUENCE_*`
- invalid `requestCounter`, stale `currentStateVersion`, sequencing violations

3. `GS_IDEMPOTENCY_*`
- duplicate/invalid idempotency handling

4. `GS_LIMITS_*`
- bet range, exposure, feature eligibility

5. `GS_FEATURE_*`
- invalid feature action or disabled feature policy

6. `GS_RESTORE_*`
- resume/restore payload invalid or unavailable

7. `GS_HISTORY_*`
- history policy/availability failures

8. `GS_RUNTIME_*`
- generic runtime processing failures

## Browser handling policy

1. Always surface `code` + `message` in dev mode.
2. Never infer wallet/session corrections locally after an error.
3. Use recoverable UX for retryable classes (`GS_SEQUENCE_*`, transient runtime).
4. Use terminal UX for non-recoverable classes (`GS_AUTH_*`, invalid session).
