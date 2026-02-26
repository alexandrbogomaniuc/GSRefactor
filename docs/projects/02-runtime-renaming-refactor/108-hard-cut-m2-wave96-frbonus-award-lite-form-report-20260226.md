# Hard-Cut M2 Wave 96 Report (FRB award lite form)

Date (UTC): 2026-02-26
Wave: 96
Scope: migrate `AwardFRBLiteForm` to `com.abs` with bounded dependent rewires.

## Changed files
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBLiteForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBLiteAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## What changed
- Package declaration moved from `com.dgphoenix.casino.actions.api.frbonus` to `com.abs.casino.actions.api.frbonus` for `AwardFRBLiteForm`.
- Added import update in `AwardFRBLiteAction` to reference `com.abs...AwardFRBLiteForm`.
- Updated Struts form-bean `FRBAwardLiteForm` type to `com.abs...AwardFRBLiteForm`.

## Validation evidence
- Passing run (9/9):
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-183626-hardcut-m2-wave96-frbonus-award-lite-form/`

## Final outcome
- Scoped legacy refs: `1 -> 0`
- Scoped `com.abs` refs: `2`
- Global tracked declarations/files remaining: `2105` (`2277` baseline, `172` reduced)
