# Protocol: GS HTTP Runtime (Compatibility Pointer)

This file is a compatibility pointer only.
Canonical runtime contract now lives in:

- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-sequence-diagrams.md`

## Canonical endpoint semantics

- `/v1/bootstrap`
- `/v1/opengame`
- `/v1/playround`
- `/v1/featureaction`
- `/v1/resumegame`
- `/v1/closegame`
- `/v1/gethistory`

## Deprecated semantics

The older browser-facing `/v1/placebet` + `/v1/collect` split is deprecated in canonical scope.
Any references to those endpoints are legacy/experimental only.
