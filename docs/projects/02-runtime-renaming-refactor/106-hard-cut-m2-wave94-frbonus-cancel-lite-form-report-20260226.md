# Hard-Cut M2 Wave 94 Report (FRB cancel lite form)

Date (UTC): 2026-02-26
Wave: 94
Scope: migrate `CancelFRBLiteForm` to `com.abs` with bounded dependent rewires.

## Changed files
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBLiteForm.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBLiteAction.java`
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## What changed
- Package declaration moved from `com.dgphoenix.casino.actions.api.frbonus` to `com.abs.casino.actions.api.frbonus` for `CancelFRBLiteForm`.
- Added import update in `CancelFRBLiteAction` to reference `com.abs...CancelFRBLiteForm`.
- Updated Struts form-bean `CancelFRBLiteForm` type to `com.abs...CancelFRBLiteForm`.

## Validation evidence
- Initial failed attempt (reactor path mismatch):
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182126-hardcut-m2-wave94-frbonus-cancel-lite-form/`
- Rerun failed attempt (missing cluster properties in game-server modules):
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182240-hardcut-m2-wave94-frbonus-cancel-lite-form-rerun/`
- Partial rerun (7/9 PASS):
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182406-hardcut-m2-wave94-frbonus-cancel-lite-form-rerun2/`
- Final passing run (9/9 PASS):
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182613-hardcut-m2-wave94-frbonus-cancel-lite-form-rerun3/`

## Final outcome
- Scoped legacy refs: `1 -> 0`
- Scoped `com.abs` refs: `2`
- Global tracked declarations/files remaining: `2107` (`2277` baseline, `170` reduced)
