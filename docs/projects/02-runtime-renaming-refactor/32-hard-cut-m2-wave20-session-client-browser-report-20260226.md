# Hard-Cut M2 Wave 20 Report (Session Browser + Client Info)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W20-session-client-browser`
Status: `COMPLETE`

## Scope
Migrated namespaces:
- `com.dgphoenix.casino.common.cache.data.session.BrowserInfo` -> `com.abs.casino.common.cache.data.session.BrowserInfo`
- `com.dgphoenix.casino.common.cache.data.session.GameClientInfo` -> `com.abs.casino.common.cache.data.session.GameClientInfo`

Wave touched 3 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103510-hardcut-m2-wave20-session-client-browser`

## Key migration result
- `BrowserInfo`: legacy refs `0`, abs refs `1`
- `GameClientInfo`: legacy refs `0`, abs refs `1`

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
- Runtime logic risk: low.
- Coherent two-model migration with shared persister import updates.

## Next wave proposal
- M2 Wave 21: continue next low-fanout family outside `common-gs` compile boundary.
