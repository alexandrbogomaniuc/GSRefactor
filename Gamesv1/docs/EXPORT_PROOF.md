# EXPORT_PROOF

Artifact-truth export proof for Gamesv1.

## 1) Exact repo root used for export

- Export source root: `E:\Dev\GSRefactor\Gamesv1`
- Git toplevel: `E:/Dev/GSRefactor`

## 2) Same root used for command proofs

- Yes.
- All proof commands in this final run used working directory `E:\Dev\GSRefactor\Gamesv1`.

## 3) Final clean Gamesv1 export

- Archive file: `E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\gamesv1\Gamesv1_export_20260303T120703Z.zip`
- SHA-256: `eb896ea6e50797278bddca5dd8200010586f2bf78c3d2feee9e7fe7c06ab53b5`
- Timestamp (UTC): `2026-03-03 12:07:21`

## 4) Exclusion rules used

Excluded from export staging:
- `node_modules`
- `dist`
- `build`
- `.cache`
- `release-packs`
- `~$*.docx`

Archive exclusion check result:
- `ZIP_EXCLUSION_CHECK=PASS`

## 5) Strict upstream mirror proof

- Included GS pack artifact: `E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\gs_pack\gs_pack_upload.zip`
- Extracted strict upstream path: `E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\gs_pack\gs`
- Command:
  - `corepack pnpm run verify:gs-contract-pack -- --strict-upstream --upstream E:\Dev\GSRefactor\exports\audit_final_20260303T120234Z\gs_pack\gs --repo E:\Dev\GSRefactor\Gamesv1\docs\gs`
- Result: `PASS`

## 6) Final audit bundle

- Bundle file: `E:\Dev\GSRefactor\exports\AUDIT_BUNDLE_FINAL_20260303T120816Z.zip`
- SHA-256: `ff8e894ab2f7dc79869ba82c12bc15c6b9b5592a8d5caf7310517571a9f4ed8c`
- Contains:
  - `/gs_pack/gs_pack_upload.zip`
  - `/gamesv1/Gamesv1_export_20260303T120703Z.zip`
  - `/proof/SHA256SUMS.txt`
  - `/proof/command_outputs.txt`
  - `/proof/verify_gs_contract_pack_output.txt`
