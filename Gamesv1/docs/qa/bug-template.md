# Bug Report Template

> CRITICAL: Do not submit truncated logs. Attach sanitized HTTP runtime logs and console traces.

## Overview
- Summary:
- Severity: `S1 - Crash` | `S2 - Math/Compliance` | `S3 - Visual/UX`

## Environment
- Build URL:
- Device:
- OS Version:
- Browser:
- Launch mode: `guest` | `free` | `real`

## Reproduction Steps
1.
2.
3.

## Observation
- Expected Result:
- Actual Result:

## Evidence
### Media
- Screenshots / Video:

### Technical Logs (Sanitized)
```json
// Attach GS HTTP runtime requests/responses here (tokens redacted)
// Example:
// [OUT] POST /v1/bootstrap { sessionId: "...", ... }
// [IN]  200 /v1/bootstrap { session: {...}, wallet: {...}, runtimeConfig: {...} }
// [OUT] POST /v1/opengame { sessionId: "...", ... }
// [IN]  200 /v1/opengame { sessionId: "...", balance: ..., requestCounter: ... }
// [OUT] POST /v1/playround { clientOperationId: "...", idempotencyKey: "...", requestCounter: 17, ... }
// [IN]  200 /v1/playround { roundId: "...", balance: ..., winAmount: ..., presentationPayload: {...} }
```

### Console Output
```text
// Paste relevant JS/runtime errors
```
