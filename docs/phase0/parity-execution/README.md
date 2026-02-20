# Phase 0 Parity Harness Runbook

## Dry-run (safe default)
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase0-parity-harness.sh \
  --mode dry-run \
  --base-url http://localhost:18080 \
  --fixture-file docs/phase0/parity-fixture.env.example
```

## Run mode (requires full wallet fixture)
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase0-parity-harness.sh \
  --mode run \
  --base-url http://localhost:18080 \
  --fixture-file docs/phase0/parity-fixture.env
```

## Fixture bootstrap helper
Generate `docs/phase0/parity-fixture.env` with deterministic `BONUS_HASH`:
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase0-fixture-bootstrap.sh \
  --bank-id 6274 \
  --game-id 838 \
  --token bav_game_session_001 \
  --external-bank-id 6274 \
  --ext-bonus-id 1 \
  --bonus-pass-key <BANK_BONUS_PASS_KEY>
```

Includes:
- `BSCHECK_HASH` for `bscheck.do`
- `BSAWARD_HASH` for full `bsaward.do` contract
- full `bsaward` contract fixture fields (`BSAWARD_*`)

## Output
- Reports: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/phase0-parity-<timestamp>.md`
- HTTP response snippets (run mode): `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/P0-*.body.txt`
- Reconnect facade fallback report: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/phase0-reconnect-facade-fallback-20260220-141948.md`

## Notes
- Harness now validates response body contracts (`PASS_CONTRACT` / `FAIL_CONTRACT`), not HTTP status alone.
- Harness now includes `P0-LA-03` to validate clean launch alias `/startgame` in parity runs.
- Refactor isolated baseline: `BANK_ID=271` is currently launch-positive (`P0-LA-01`, `P0-LA-03`).
- `P0-WA-01` now validates `bscheck.do` contract with centralized `BSCHECK_HASH` fixture.
- `P0-SE-01` now validates full `bsaward.do` contract with `BSAWARD_*` fixture fields.
- `P0-SE-01` accepts idempotent duplicate-award response (`CODE=641` / `already exists`) as pass-contract.
- Deterministic negative probes (`P0-LA-02`, `P0-WA-00`, `P0-SE-00`) run without wallet-positive fixture data and provide a stable baseline.
- Launch alias `/startgame` is implemented at refactor static proxy layer (`/cwstartgamev2.do` internal proxy, no `Location` redirect header).
- Reconnect compatibility bridge is active at refactor static proxy:
  - `/restartgame.do` redirects are followed internally,
  - legacy `/cwstartgame.do` is mapped to `/cwstartgamev2.do` with `sessionId -> token`,
  - temporary fallback avoids raw `500` exposure during reconnect invalid-bank probes.

## Hash helper (bonus endpoints)
Use GS-aligned hash composition rules to generate fixture hash values:
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/phase0-bonus-hash-helper.sh \
  --mode check \
  --ext-bonus-id 1 \
  --external-bank-id 6274 \
  --bonus-pass-key <BANK_BONUS_PASS_KEY>
```
