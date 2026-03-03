# Protocol: GS HTTP Runtime (Compatibility Pointer)

> [!WARNING]
> Compatibility pointer only. Do not treat this file as contract authority.
> Canonical runtime and release contracts live in `docs/gs/*`.

This file is a compatibility pointer only.
Canonical runtime contract now lives in:

- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-sequence-diagrams.md`

## Canonical endpoint semantics

- `/slot/v1/bootstrap`
- `/slot/v1/opengame`
- `/slot/v1/playround`
- `/slot/v1/featureaction`
- `/slot/v1/resumegame`
- `/slot/v1/closegame`
- `/slot/v1/gethistory`

## Deprecated semantics

The older browser-facing `/v1/placebet` + `/v1/collect` split is deprecated in canonical scope.
`/v1/readhistory` naming is deprecated in canonical scope and replaced by `/slot/v1/gethistory`.
Any references to those endpoints are legacy/experimental only.
