# CURRENT_STATE_AUDIT

Audit snapshot date: 2026-03-03
Repo root: `E:\Dev\GSRefactor\Gamesv1`

## Scope

Final proof-reconciliation run only (no product feature work).

## Verification model (resolved contradiction)

Option A applied:
- Default `verify:gs-contract-pack` is now reproducibly repo-local (uses `Gamesv1/docs/gs` by default).
- Strict upstream equality remains explicit and stronger via `--strict-upstream --upstream <path>`.

Result:
- default verify: PASS
- strict verify against included GS pack: PASS

## Final proof command status

Commands and results from final state:
1. `pnpm run verify:gs-contract-pack` -> PASS
2. `pnpm run verify:gs-contract-pack -- --strict-upstream --upstream E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\gs_pack\gs --repo E:\Dev\GSRefactor\Gamesv1\docs\gs` -> PASS
3. `pnpm run test:contract` -> PASS
4. `pnpm run test` -> PASS
5. `pnpm run build` -> PASS
6. `pnpm run release:pack -- --game premium-slot` -> PASS
7. `pnpm run create-game -- --dry-run --gameId audit-proof-slot --name "Audit Proof Slot" --themeId audit` -> PASS

## Final export and bundle

- Clean export: `E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\gamesv1\Gamesv1_export_20260303T120703Z.zip`
- Export SHA-256: `eb896ea6e50797278bddca5dd8200010586f2bf78c3d2feee9e7fe7c06ab53b5`
- Included GS pack: `E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\gs_pack\gs_pack_upload.zip`
- GS pack SHA-256: `adda98196cec7f0f34ac41623fd8cfe9a3bc0299ac266000c90afa941eaeadd1`

- Final upload bundle: `E:\Dev\GSRefactor\exports\AUDIT_BUNDLE_FINAL_20260303T120816Z.zip`
- Bundle SHA-256: `ff8e894ab2f7dc79869ba82c12bc15c6b9b5592a8d5caf7310517571a9f4ed8c`

## Raw proof output location

- `E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\proof\command_outputs.txt`
- `E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\proof\verify_gs_contract_pack_output.txt`
