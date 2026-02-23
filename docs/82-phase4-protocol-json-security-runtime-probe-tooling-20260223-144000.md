# Phase 4 Protocol JSON Security Runtime Probe Tooling (2026-02-23)

## What was done
- Added a Phase 4 runtime JSON security canary probe script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-json-security-canary-probe.sh`
- The probe validates protocol-adapter HTTP behavior (direct `/api/v1/protocol/requests/normalize`) for:
  - JSON hash verification (POST + GET hash-rule)
  - exempt endpoint behavior
  - replay nonce reuse behavior
- Probe safely patches/restores bank settings for the tested bank and supports:
  - `--hmac-secret` for non-prod runtime validation when a test secret is available
  - `--require-secret false` default (graceful `SKIP` if runtime secret is not configured)
- Updated `phase4-protocol-runtime-evidence-pack.sh` with optional security probe execution:
  - `--run-security-probe true|false` (default `false`)
  - `--security-require-secret true|false` (default `false`)
- Added a local verification suite help check for the new runtime canary script.

## Why this matters (high level)
- This prepares runtime validation for JSON protocol security without forcing immediate secret injection/cutover.
- It keeps current migration safe by making the new runtime probe optional while local logic smoke remains the mandatory behavior gate.

## Validation
- `bash -n` passed for the new probe and updated Phase 4 evidence pack.
- `--help` passed for:
  - `phase4-protocol-json-security-canary-probe.sh`
  - `phase4-protocol-runtime-evidence-pack.sh`
- `phase5-6-local-verification-suite.sh` passed with the new help check.
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-143849.md`
  - summary: PASS=21, FAIL=0, SKIP=0

## Scope note
- Full runtime hash/replay validation still depends on a non-prod HMAC secret being available to the running protocol-adapter service (or passed consistently via env). The probe is implemented and ready for that step.
