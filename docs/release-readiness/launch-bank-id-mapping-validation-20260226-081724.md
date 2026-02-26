# Launch Bank-ID Mapping Validation (Refactor Runtime)

Date (UTC): 2026-02-26 08:17 UTC
Workspace: `/Users/alexb/Documents/Dev/Dev_new`

## What was validated
1. Startup/smoke scripts now use configurable launch parameters (environment variables) instead of fixed embedded values.
2. Secondary launch smoke check can be enabled for additional bank/subcasino pairs.
3. Current Betonline mapping behavior is explicit and reproducible:
   - Internal bank: `6276` (subcasino `508`)
   - Current external launch bank id: `6274`

## Runtime proof
- Smoke checks (primary + secondary) both pass:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-081724/refactor-onboard-smoke.log`
- Direct launch using `bankId=6276` (subcasino `508`) returns error page (`Bank is incorrect`):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-081724/startgame-6276-headers.txt`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-081724/startgame-6276-body.html`
- External launch id path for Betonline (`bankId=6274&subCasinoId=508`) returns launch page (`HTTP 200`):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-081724/startgame-6274-508-headers.txt`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-081724/startgame-6274-508-body.html`

## Outcome
- Refactor startup/onboarding behavior is now clearer and configurable.
- Launch URL confusion for bank `6276` is documented with concrete pass/fail proof.
