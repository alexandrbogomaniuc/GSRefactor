# EXPORT_PROOF

Artifact-truth export proof for Gamesv1.

## 1) Exact repo root used for export

- Export source root: `E:\Dev\GSRefactor\Gamesv1`
- Git toplevel: `E:/Dev/GSRefactor`

## 2) Was this the same root used for command proofs?

- Yes.
- All proof commands in this audit run used working directory `E:\Dev\GSRefactor\Gamesv1`.

## 3) Archive produced

- Archive file: `E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gamesv1\Gamesv1_export_20260303T112426Z.zip`

## 4) Archive hash

- SHA-256: `b43d90f901b3bddcd1350c36763960fdbff1c1b60cef07ad54ce1408888de6ac`

## 5) Export timestamp (UTC)

- Archive last write: `2026-03-03 11:24:48`

## 6) Export cleanliness (generated/binary junk exclusions)

Excluded from archive build:
- `node_modules`
- `dist`
- `build`
- `.cache`
- `release-packs`
- `~$*.docx`

Archive exclusion check:
- Result: `ZIP_EXCLUSION_CHECK=PASS`

## 7) Repo/export mismatch truth

- This archive was exported from the same repo root used for command proofs.
- Required file truth is recorded in `docs/EXPORT_FILE_CHECKLIST.md`.

## 8) Canonical contract mirror check

Strict upstream verify command used the included GS pack artifact:
- upstream source zip: `E:\Dev\GSRefactor\docs\gs_pack_upload.zip`
- strict verify upstream dir: `E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gs_pack\gs`
- command: `corepack pnpm run verify:gs-contract-pack -- --strict-upstream --upstream E:\Dev\GSRefactor\exports\audit_20260303T111938Z\gs_pack\gs --repo E:\Dev\GSRefactor\Gamesv1\docs\gs`
- Result: `PASS`

## 9) Audit bundle artifact

- Audit bundle: `E:\Dev\GSRefactor\exports\AUDIT_BUNDLE_20260303T112544Z.zip`
- Audit bundle SHA-256: `dd2c4659006f9f229b9131e97333e77f12e5fe58168b4c2513fa779cade8b6aa`
