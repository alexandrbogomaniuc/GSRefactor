# Hard-Cut M2 Wave 8 Report (Canex Request Root)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W8-canex-request-root`
Status: `COMPLETE`

## Scope
Migrated namespace usage for root request classes:
- from `com.dgphoenix.casino.common.client.canex.request`
- to `com.abs.casino.common.client.canex.request`
for:
- `CanexRequest`
- `CanexJsonRequest`
- `RequestType`

Wave touched 20 files across common, common-wallet, web-gs, and canex subpackage request models.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-095313-hardcut-m2-wave8-canex-request-root`

## Key migration result
- Remaining legacy refs for this wave scope: `0`
- New `com.abs` refs for this scope: `23`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: medium.
- This wave has broader import fan-out than previous waves but remained green in full matrix and runtime smoke.

## Next wave proposal
- M2 Wave 9: continue with another low-fanout namespace family while keeping common-gs boundary guardrail in place.
