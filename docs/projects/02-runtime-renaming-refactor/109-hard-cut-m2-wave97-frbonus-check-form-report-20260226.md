# Hard-Cut M2 Wave 97 Report (FRB check form)

Date (UTC): 2026-02-26
Wave: 97
Scope: migrate `CheckFRBForm` to `com.abs` with bounded dependent rewires.

## Changed files
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CheckFRBForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CheckFRBAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## What changed
- Package declaration moved from `com.dgphoenix.casino.actions.api.frbonus` to `com.abs.casino.actions.api.frbonus` for `CheckFRBForm`.
- Added import update in `CheckFRBAction` to reference `com.abs...CheckFRBForm`.
- Updated Struts form-bean `FRBCheckForm` type to `com.abs...CheckFRBForm`.

## Validation evidence
- Passing run (9/9):
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-184111-hardcut-m2-wave97-frbonus-check-form/`

## Final outcome
- Scoped legacy refs: `1 -> 0`
- Scoped `com.abs` refs: `2`
- Global tracked declarations/files remaining: `2104` (`2277` baseline, `173` reduced)
