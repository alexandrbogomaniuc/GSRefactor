# Phase 4 Protocol JSON Security Logic Smoke + Suite Gate (2026-02-23)

## What was done
- Added executable local smoke test for protocol-adapter JSON security behavior:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-security-logic-smoke.sh`
- Coverage includes:
  - POST HMAC-SHA256 hash verification using raw JSON body
  - GET hash rule concatenation (`bankId+userId+timeZone`)
  - hash-exempt endpoint handling
  - ENFORCE mode missing hash block (`401`)
  - replay nonce reuse block (`409`) in ENFORCE mode
- Integrated this smoke test into the default local verification suite (`phase5-6-local-verification-suite.sh`) with:
  - CLI help check
  - executable logic smoke check

## Why this matters (high level)
- This validates the JSON protocol security behavior (Casino-side hash + replay semantics) as executable tests, not just documentation/config.
- It reduces risk while keeping rollout backward-compatible because the tests exercise shadow/enforce paths locally without touching the legacy runtime.

## Validation
- `bash -n` passed for the new smoke script and updated verification suite.
- `phase4-protocol-security-logic-smoke.sh --help` passed.
- `phase4-protocol-security-logic-smoke.sh` executed successfully (all PASS).
- `phase5-6-local-verification-suite.sh` passed with expanded checks.
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-134556.md`
  - summary: PASS=20, FAIL=0, SKIP=0

## Scope note
- This is a local logic gate for the protocol-adapter service internals. Runtime canary verification remains separate (`phase4-protocol-runtime-evidence-pack.sh`) and still depends on refactor stack readiness.
