# Browser Runtime API Contract (Compatibility Pointer)

> [!WARNING]
> Compatibility pointer only. Do not treat this file as contract authority.
> Canonical runtime and release contracts live in `docs/gs/*`.

This file is kept for backward link compatibility.
The canonical contract is:

- `docs/gs/browser-runtime-api-contract.md`

Bootstrap/config canon:
- `docs/gs/bootstrap-config-contract.md`

Error canon:
- `docs/gs/browser-error-codes.md`

Sequence canon:
- `docs/gs/browser-runtime-sequence-diagrams.md`

## Deprecation

Legacy `/v1/placebet` + `/v1/collect` assumptions are deprecated for canonical Gamesv1 runtime.
Legacy `/v1/readhistory` naming is deprecated and replaced by `/slot/v1/gethistory`.
Use `/slot/v1/playround` per `docs/gs/browser-runtime-api-contract.md`.
