# CURRENT_STATE_AUDIT

Audit regenerated from actual commands on 2026-03-03 from `E:\Dev\GSRefactor\Gamesv1`.

Raw command outputs are captured in `docs/generated/COMMAND_PROOF_2026-03-03.md`.

## Source-of-truth split

- Client capability spec: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- Runtime/release contract spec: `docs/gs/*`
- Export truth docs: `docs/EXPORT_PROOF.md`, `docs/EXPORT_FILE_CHECKLIST.md`

## Command proof (actual runs)

1. `corepack pnpm run verify:gs-contract-pack -- --strict-upstream --upstream e:\Dev\GSRefactor\docs\gs --repo e:\Dev\GSRefactor\Gamesv1\docs\gs`
- PASS
- Upstream lock semantics verification passed
- Canonical entries: 43
- Repo files compared: 60

2. `corepack pnpm run test:contract`
- PASS (10 passed, 0 failed)

3. `corepack pnpm run test`
- PASS
- Includes: verify + config + animation-policy + contract + template tests

4. `corepack pnpm run build`
- PASS (`@games/premium-slot` build succeeded)

5. `corepack pnpm run release:pack -- --game premium-slot`
- PASS
- Output: `games/premium-slot/release-packs/1.0.0+7ec5ebb1`

6. `corepack pnpm run create-game -- --dry-run --gameId dryrun-slot --name "Dry Run Slot" --themeId neon`
- PASS

## Export proof

- Archive: `E:\Dev\GSRefactor\exports\Gamesv1_export_20260303T091017Z.zip`
- SHA-256: `0e87b0d703853926c378206e47af977a31bce16cb52231ded0bb95a0a8bb5920`
- Exclusion check: PASS
- Required-file checklist: PASS (17/17 present)

## Workspace tree summary (root)

- `.agent/`
- `docs/`
- `games/`
- `packages/`
- `tests/`
- `tools/`
- `README.md`, `package.json`, `pnpm-workspace.yaml`, `tsconfig.json`

## Hygiene checks

Tracked-source ban check (`node_modules`, `dist`, `build`, `.cache`, `release-packs`, `~$*.docx`) against git index:
- PASS (no tracked banned paths)

## Exactness state

- `upstream-exact`: PASS (strict upstream mirror verify passed)
- `runtime-exact`: PASS (contract tests green)
- `release/scaffold-exact`: PASS (`release:pack` and `create-game --dry-run` green)
