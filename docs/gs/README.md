# GS Slot Contracts Canonical Pack

- Status: Canonical for Phase 1
- Last updated: 2026-03-02

## 1) Canonical Contract Versions

- `slot-bootstrap-v1`
- `slot-browser-v1`
- `slot-runtime-v1`
- `slot-release-registration-v1`

## 2) Canonical Docs by Audience

Browser-facing (authoritative for Gamesv1/browser integration):
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/browser-error-codes.md`
- `docs/gs/browser-runtime-sequence-diagrams.md`

Release/Ops-facing:
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`

Internal-only (not browser-facing transport):
- `docs/gs/internal-slot-runtime-contract.md`
- `docs/gs/math-package-spec.md`
- `docs/gs/rng-ownership-decision.md`

Canonical markdown files hashed in `contract-lock.json` for Gamesv1 export:
- `README.md`
- `bootstrap-config-contract.md`
- `browser-runtime-api-contract.md`
- `browser-error-codes.md`
- `browser-runtime-sequence-diagrams.md`
- `release-registration-contract.md`
- `enable-disable-canary-rollback.md`

## 3) Canonical Fixtures and Schemas

Canonical fixture folder:
- `docs/gs/fixtures/`

These files are normative wire examples for `slot-browser-v1`.

Canonical schema folder:
- `docs/gs/schemas/`

These files are machine-readable JSON Schemas for request/response payload validation.

Required canonical fixture set:
- `bootstrap.request.json`
- `bootstrap.response.json`
- `opengame.request.json`
- `opengame.response.json`
- `playround.request.json`
- `playround.response.json`
- `playround.duplicate.response.json`
- `featureaction.request.json`
- `featureaction.response.json`
- `resumegame.request.json`
- `resumegame.response.json`
- `closegame.request.json`
- `closegame.response.json`
- `gethistory.request.json`
- `gethistory.response.json`
- `error.INVALID_REQUEST_COUNTER.json`
- `error.STATE_VERSION_MISMATCH.json`
- `error.BOOTSTRAP_CONFIG_MISMATCH.json`
- `error.IDEMPOTENCY_KEY_REUSE.json`
- `release-registration.sample.json`

## 4) Locked Browser Decisions

1. Bootstrap endpoint is `POST /slot/v1/bootstrap`.
2. Bootstrap is read-only, does not advance `requestCounter`, and does not require `idempotencyKey`.
3. Bootstrap is not an `openGame` substitute.
4. History endpoint is `POST /slot/v1/gethistory`.
5. `gethistory` is read-only and must use current accepted `requestCounter` for request/session correlation.
6. Browser endpoint header requirements are fixed in:
   - `docs/gs/browser-runtime-api-contract.md` section `3.4 Endpoint Header Matrix (Canonical)`.
7. Outcome hash serialization is fixed to RFC 8785 JCS + SHA-256 with `sha256:<lowercase-hex>`.
8. Config key mapping is fixed:
   - `USE_JP_NOTIFICATION` controls server notifications enablement.
   - `CUSTOMER_SETTINGS_URL` maps to `localizationPolicy.contentPath`.
   - `content_path` is a legacy alias accepted only during GS config resolution.

## 5) Obsolete / Archived

Obsolete content is tracked under:
- `docs/gs/obsolete/`

Obsolete naming and variants:
- Any weaker bootstrap draft or pre-canonical fixture variant is obsolete once moved under `obsolete/`.

## 6) Gamesv1 Verification

Verification source:
- `docs/gs/contract-lock.json`

How to verify:
1. From `docs/gs`, compute `sha256` for each file listed in `contract-lock.json`.
2. Confirm all computed hashes match the lock file values.
3. Reject pack consumption if any canonical file hash differs.

Example verification command (macOS):
```bash
cd docs/gs
shasum -a 256 README.md bootstrap-config-contract.md browser-runtime-api-contract.md \
  browser-error-codes.md browser-runtime-sequence-diagrams.md \
  release-registration-contract.md enable-disable-canary-rollback.md
```
