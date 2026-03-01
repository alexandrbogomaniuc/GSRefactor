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
// [OUT] POST /v1/opengame { sessionId: "...", ... }
// [IN]  200 /v1/opengame { sessionId: "...", balance: ..., requestCounter: ..., runtimeConfig: {...} }
// [OUT] POST /v1/placebet { clientOperationId: "...", requestCounter: 17, ... }
// [IN]  200 /v1/placebet { roundId: "...", math: {...}, ... }
// [OUT] POST /v1/collect { roundId: "...", requestCounter: 18, ... }
// [IN]  200 /v1/collect { balance: ..., winAmount: ..., ... }
```

### Console Output
```text
// Paste relevant JS/runtime errors
```
