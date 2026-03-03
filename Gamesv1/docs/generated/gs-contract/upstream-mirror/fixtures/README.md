# GS Contract Fixtures

Canonical example payload fixtures for `slot-browser-v1` exact wire contract.

Use these fixtures for:
- transport contract tests
- schema validation tests
- release registration artifact validation

Source-of-truth remains:
- `docs/gs/README.md`
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/release-registration-contract.md`

Wire shape rules:
- Canonical request field names are not normalized at boundary.
- Sequencing/idempotency fields are exact wire names (`requestCounter`, `currentStateVersion`, `idempotencyKey`, `clientOperationId`).
- `selectedBet` uses exact fields: `coinValueMinor`, `lines`, `multiplier`, `totalBetMinor`.
- Read-only endpoints (`bootstrap`, `gethistory`) do not require idempotency/client operation headers.

Fixture wrapper format (canonical):
- `fixtureVersion`
- `name`
- `endpoint`
- `requestHeaders`
- `requestBody`
- `response.httpStatus`
- `response.body`
