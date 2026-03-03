# PRODUCTIZATION_GATE

Gate rule for this audit sprint:
- Gate is **GREEN** only if strict upstream verification passes and the clean export is clean.

## Current status (2026-03-03)

- **GREEN**

## Required evidence

1. Strict upstream mirror check
- Command: `pnpm run verify:gs-contract-pack -- --strict-upstream --upstream E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gs_pack\gs --repo E:\Dev\GSRefactor\Gamesv1\docs\gs`
- Result: PASS

2. Clean export check
- Export: `E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gamesv1\Gamesv1_export_20260303T112426Z.zip`
- SHA-256: `b43d90f901b3bddcd1350c36763960fdbff1c1b60cef07ad54ce1408888de6ac`
- Exclusion check: PASS (`node_modules`, `dist`, `build`, `.cache`, `release-packs`, `~$*.docx` excluded)

3. Proof command run set
- `pnpm run test:contract`: PASS
- `pnpm run test`: PASS
- `pnpm run build`: PASS
- `pnpm run release:pack -- --game premium-slot`: PASS
- `pnpm run create-game --dry-run`: PASS

Raw outputs are included in the audit bundle under `proof/`.
