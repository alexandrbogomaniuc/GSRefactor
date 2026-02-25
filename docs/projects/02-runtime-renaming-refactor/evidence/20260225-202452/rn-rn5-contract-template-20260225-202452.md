# RN5 Contract/Template Migration Evidence

Date (UTC): 2026-02-25 20:24:52
Scope: Runtime naming cleanup project (`RENAME-FINAL`) - RN5 compatibility completion wave.

## What changed
1. GS -> MP payload now writes both legacy and alias weapon-mode keys:
- `MQ_WEAPONS_MODE`
- `ABS_WEAPONS_MODE`

2. Multiplayer launch templates (real/free) now publish dual alias keys for the remaining runtime settings:
- `ABS_HELP_PATH`
- `ABS_TIMER_OFFSET`
- `ABS_TIMER_FREQ`
- `ABS_WEAPONS_SAVING_ALLOWED`
- `DISABLE_ABS_AUTOFIRING`
- `ABS_ROOMS_SORT_ORDER`
- `ABS_CLIENT_LOG_LEVEL`

3. Support template-property editor now includes alias options for migrated game keys:
- `ABS_STAKES_RESERVE`
- `ABS_STAKES_LIMIT`
- `ABS_AWARD_PLAYER_START_BONUS`

## Test results
- RN-T004 unit compatibility:
  - `BankInfoAliasCompatibilityTest`: PASS (15/15)
  - `ReflectionUtilsCompatibilityTest`: PASS (3/3)
- RN-T003 build:
  - `common-gs`: BUILD SUCCESS
  - `web-gs`: BUILD SUCCESS
- Runtime smoke:
  - `/startgame` returned HTTP 200 for bank `6275`, subcasino `507`, game `838`.
- Template packaging proof:
  - Built `ROOT.war` contains all newly added `ABS_*` template keys for both real and free MP templates.

## Raw evidence files
- Fresh inventory snapshots:
  - `../../../../phase9/runtime-naming-cleanup/evidence/20260225-202457-class_refs.txt`
  - `../../../../phase9/runtime-naming-cleanup/evidence/20260225-202457-mq_refs.txt`
  - `../../../../phase9/runtime-naming-cleanup/evidence/20260225-202457-phase9_map_refs.txt`
- `rn-rn5-unit-bankinfo-20260225-202452.txt`
- `rn-rn5-unit-reflection-20260225-202452.txt`
- `rn-rn5-build-common-gs-20260225-202452.txt`
- `rn-rn5-build-web-gs-20260225-202452.txt`
- `rn-rn5-launch-http-20260225-202452.txt`
- `rn-rn5-launch-body-20260225-202452.html`
- `rn-rn5-template-alias-real-war-20260225-202452.txt`
- `rn-rn5-template-alias-free-war-20260225-202452.txt`
- `rn-rn5-source-alias-coverage-20260225-202452.txt`

## Current decision
RN5 is marked COMPLETE for the planned dual-key compatibility wave. Legacy `MQ_*` keys remain intentionally supported to avoid breaking existing clients until a separate decommission wave is approved and tested.
