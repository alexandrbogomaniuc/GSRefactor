# Project 02 Hard-Cut Live Batch D Report (2026-03-01)

## Scope
- Batch ID: `20260301-083058-hardcut-live-batchD-bankmini-immutable2`
- Declarations migrated (`com.dgphoenix -> com.abs`):
  - `ImmutableBaseGameInfoWrapper`
  - `BankMiniGameInfo`
- Bounded rewires:
  - `CurrencySelectAction` import update for moved `ImmutableBaseGameInfoWrapper`.
  - `BankMiniGameInfo` internal import update for moved `ImmutableBaseGameInfoWrapper`.

## Validation
Fast-gate module compiles:
1. `gs-server/sb-utils` -> `BUILD SUCCESS`
2. `gs-server/common` -> `BUILD SUCCESS`
3. `gs-server/game-server/common-gs` -> `BUILD SUCCESS` (with `-Dcluster.properties=local/local-machine.properties`)
4. `gs-server/game-server/web-gs` -> `BUILD SUCCESS` (with `-Dcluster.properties=local/local-machine.properties`)

## Metrics
- Baseline tracked declarations/files: `2277`
- Reduced: `1974`
- Remaining: `303`
- Burndown: `86.693017%`

Weighted completion:
- Project 01: `100.000000%`
- Project 02: `50.429824%`
- Core total (01+02): `75.214912%`
- Entire portfolio: `87.607456%`

ETA model refresh:
- `~12.3h` (`~1.53 workdays`)

## Evidence
- `docs/projects/02-runtime-renaming-refactor/evidence/20260301-083058-hardcut-live-batchD-bankmini-immutable2/pre-count.txt`
- `docs/projects/02-runtime-renaming-refactor/evidence/20260301-083058-hardcut-live-batchD-bankmini-immutable2/pre-targets.txt`
- `docs/projects/02-runtime-renaming-refactor/evidence/20260301-083058-hardcut-live-batchD-bankmini-immutable2/diff-batch.patch`
- `docs/projects/02-runtime-renaming-refactor/evidence/20260301-083058-hardcut-live-batchD-bankmini-immutable2/fast-gate-summary.txt`
- `docs/projects/02-runtime-renaming-refactor/evidence/20260301-083058-hardcut-live-batchD-bankmini-immutable2/post-scan-legacy-packages.txt`
- `docs/projects/02-runtime-renaming-refactor/evidence/20260301-083058-hardcut-live-batchD-bankmini-immutable2/post-git-status.txt`
