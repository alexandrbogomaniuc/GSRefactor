# COMMAND_PROOF_2026-03-03

Audit-run command proof for export/exactness only.

## Repo root used

- `E:\Dev\GSRefactor\Gamesv1`

## Raw output artifacts

- `E:\Dev\GSRefactor\exports\audit_20260303T111938Z\proof\command_outputs.txt`
- `E:\Dev\GSRefactor\exports\audit_20260303T111938Z\proof\verify_gs_contract_pack_output.txt`

These raw logs are included inside `AUDIT_BUNDLE_20260303T112544Z.zip` under `proof/`.

## Commands executed

1. `pnpm run verify:gs-contract-pack -- --strict-upstream --upstream E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gs_pack\gs --repo E:\Dev\GSRefactor\Gamesv1\docs\gs`
2. `pnpm run test:contract`
3. `pnpm run test` (with `GS_CONTRACT_UPSTREAM_PATH=E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gs_pack\gs`)
4. `pnpm run build`
5. `pnpm run release:pack -- --game premium-slot`
6. `pnpm run create-game -- --dry-run --gameId audit-proof-slot --name "Audit Proof Slot" --themeId audit`

## Result summary

- strict verify: PASS
- test:contract: PASS
- test: PASS
- build: PASS
- release:pack: PASS
- create-game --dry-run: PASS
