# Hard-Cut M2 Wave 95 Report (FRB award form)

Date (UTC): 2026-02-26
Wave: 95
Scope: migrate `AwardFRBForm` to `com.abs` with bounded dependent rewires.

## Changed files
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## What changed
- Package declaration moved from `com.dgphoenix.casino.actions.api.frbonus` to `com.abs.casino.actions.api.frbonus` for `AwardFRBForm`.
- Added import update in `AwardFRBAction` to reference `com.abs...AwardFRBForm`.
- Updated Struts form-bean `FRBAwardForm` type to `com.abs...AwardFRBForm`.

## Validation evidence
- Passing run (9/9):
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-183115-hardcut-m2-wave95-frbonus-award-form/`

## Final outcome
- Scoped legacy refs: `1 -> 0`
- Scoped `com.abs` refs: `2`
- Global tracked declarations/files remaining: `2106` (`2277` baseline, `171` reduced)
