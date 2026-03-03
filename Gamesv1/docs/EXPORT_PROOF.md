# EXPORT_PROOF

Artifact-truth export proof for Gamesv1.

## 1) Exact repo root used for export

- Export source root: `E:\Dev\GSRefactor\Gamesv1`
- Git toplevel: `E:/Dev/GSRefactor`

## 2) Was this the same root used for command proofs?

- Yes.
- All proof commands were run with working directory `E:\Dev\GSRefactor\Gamesv1`.

## 3) Archive produced

- Archive file: `E:\Dev\GSRefactor\exports\Gamesv1_export_20260303T091017Z.zip`

## 4) Archive hash

- SHA-256: `0e87b0d703853926c378206e47af977a31bce16cb52231ded0bb95a0a8bb5920`

## 5) Export timestamp (UTC)

- Archive last write: `2026-03-03 09:10:45`

## 6) Export cleanliness (generated/binary junk exclusions)

Excluded from archive build:
- `node_modules`
- `dist`
- `build`
- `.cache`
- `release-packs`
- `~$*.docx` and `*.docx`

Archive exclusion check:
- Result: `ZIP_EXCLUSION_CHECK=PASS`

## 7) Repo/export mismatch truth

- This archive was exported from the same repo root used for all proof commands.
- Required file truth is recorded in `docs/EXPORT_FILE_CHECKLIST.md`.

## 8) Canonical contract mirror check

Strict upstream verify command used the real upstream pack path:
- `corepack pnpm run verify:gs-contract-pack -- --strict-upstream --upstream e:\Dev\GSRefactor\docs\gs --repo e:\Dev\GSRefactor\Gamesv1\docs\gs`
- Result: `PASS`
