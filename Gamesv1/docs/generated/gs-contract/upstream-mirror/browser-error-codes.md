# GS Browser Error Codes

Canonical browser-facing error categories:

- `GS_SEQUENCE_INVALID`: non-monotonic `requestCounter` or stale state sequencing.
- `GS_IDEMPOTENCY_INVALID`: missing/invalid idempotency contract on mutating endpoints.
- `GS_SESSION_INVALID`: session is unknown, expired, or mismatched.
- `GS_STATE_CONFLICT`: provided `currentStateVersion` conflicts with GS truth.
- `GS_VALIDATION_FAILED`: request payload shape/type mismatch.
- `GS_RETRYABLE`: temporary retryable failure with retry metadata.
- `GS_INTERNAL`: server-side error.
