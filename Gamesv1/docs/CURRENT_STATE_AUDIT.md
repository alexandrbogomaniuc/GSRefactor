# CURRENT_STATE_AUDIT

Audit snapshot date: 2026-03-03
Repo root: `E:\Dev\GSRefactor\Gamesv1`

## Scope

This run produced export/proof artifacts only (no new product features).

## Canonical mirror proof

- Strict upstream check against included GS pack artifact: PASS
- Command: `pnpm run verify:gs-contract-pack -- --strict-upstream --upstream E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gs_pack\gs --repo E:\Dev\GSRefactor\Gamesv1\docs\gs`

## Export proof

- Clean export zip: `E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gamesv1\Gamesv1_export_20260303T112426Z.zip`
- SHA-256: `b43d90f901b3bddcd1350c36763960fdbff1c1b60cef07ad54ce1408888de6ac`
- Exclusion check: PASS

## Audit bundle

- Bundle: `E:\Dev\GSRefactor\exports\AUDIT_BUNDLE_20260303T112544Z.zip`
- SHA-256: `dd2c4659006f9f229b9131e97333e77f12e5fe58168b4c2513fa779cade8b6aa`

## Proof commands

- `pnpm run test:contract`: PASS
- `pnpm run test`: PASS
- `pnpm run build`: PASS
- `pnpm run release:pack -- --game premium-slot`: PASS
- `pnpm run create-game --dry-run`: PASS

Raw outputs are in `proof/command_outputs.txt` and `proof/verify_gs_contract_pack_output.txt` inside the audit bundle.
