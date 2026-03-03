# Third-Party Main Document Notes (Supplemental, Non-Canonical)

## Status

- This file is **NON-CANONICAL**.
- It is additional useful information only.
- It does **not** override or redefine the canonical GS browser/runtime contract pack.

## Canonical Source of Truth

Canonical GS pack location:
- `docs/gs/`

Canonical contract behavior is defined by:
- `bootstrap-config-contract.md`
- `browser-runtime-api-contract.md`
- `browser-error-codes.md`
- `browser-runtime-sequence-diagrams.md`
- `contract-lock.json`
- canonical fixtures and schemas in `fixtures/` and `schemas/`

## Allowed Use of Third-Party Material

Third-party material may be used for:
- terminology cross-checks,
- implementation sanity checks,
- future modernization ideas outside Phase 1 scope.

Third-party material must **not** be used to:
- change endpoint names or HTTP methods,
- alter ownership boundaries,
- alter request/response envelope fields,
- alter idempotency/requestCounter semantics,
- alter the canonical hash-locked pack without explicit GS contract update.

## Practical Cross-Checks (Optional)

When reviewing third-party guidance, verify it does not conflict with locked Phase-1 rules:
1. Browser endpoints remain `/slot/v1/*` only.
2. Bootstrap remains `POST /slot/v1/bootstrap`, read-only.
3. History remains `POST /slot/v1/gethistory`, read-only with current accepted `requestCounter`.
4. Mutating endpoints require idempotency semantics.
5. Numeric JSON integer fields remain integer-typed (`requestCounter`, `stateVersion`, `currentStateVersion`, minor-unit money fields).
6. Server-only diagnostics (`serverAudit`, `rngTraceRef`, internal diagnostics) remain excluded from browser payloads.

## Future Modernization Ideas (Non-Binding)

- Add CI schema conformance checks against `schemas/*.schema.json`.
- Add pack-signature verification in Gamesv1 sync pipeline.
- Add changelog automation tied to `contract-lock.json` hash diff.
