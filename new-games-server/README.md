# New Games Server

Dedicated New Games runtime service that integrates with legacy GS through internal APIs.

## Stack
- Node.js 20+
- TypeScript
- Fastify

## API
- `POST /v1/opengame`
- `POST /v1/placebet`
- `POST /v1/collect`
- `POST /v1/readhistory`

## Environment
See `.env.example`.

## Run
```bash
npm install
npm run dev
```

Default port: `6400`.

## Test
```bash
npm test
```

## Performance Smoke
Requires a running NGS instance (default: `http://127.0.0.1:6400`).

```bash
npm run perf:smoke
```

Optional env overrides:
- `NGS_BASE_URL`
- `NGS_SESSIONS` (default `200`)
- `NGS_ROUNDS_PER_SESSION` (default `2`)
- `NGS_CONCURRENCY` (default `40`)
- `NGS_BET_AMOUNT` (default `100`)

## RTP
- Current Plinko model follows Betsoft-style configuration by `risk + lines`.
- Supported lines: `10..13`; risk levels: `low`, `medium`, `high`.
- Slot probability is derived from Pascal/binomial row for the selected line count (center-heavy).
- Multipliers come from per-line/per-risk payout tables.
- Theoretical RTP stays around `~96.0%` across supported configurations.
- See `parseBetType`, `pascalWeights`, and `deterministicOutcome` in `/Users/alexb/Documents/Dev/new-games-server/src/index.ts`.

## Upstream Timeout
- `GS_INTERNAL_TIMEOUT_MS` (default `3000`) controls timeout for NGS -> GS internal API calls.

## Runtime Automation
GS runtime deploy/hot-swap + health checks:

```bash
npm run runtime:deploy-gs
```

Build deployable GS class bundle artifact:

```bash
npm run runtime:build-bundle
```

Full local route+API E2E (auto-starts NGS if needed):

```bash
npm run runtime:e2e
```

Runtime status snapshot for handoff/ops:

```bash
npm run runtime:status
```

Full M4 proof pack (runtime e2e + status + perf report with SLO verdict):

```bash
npm run runtime:proof-pack
```

Proof-pack artifacts:
- report: `/Users/alexb/Documents/Dev/docs/New games Project/evidence/m4-proof-pack-<UTC>.md`
- perf JSON/stderr + e2e/status logs: `/Users/alexb/Documents/Dev/new-games-server/artifacts/perf/`

Useful overrides:
- `RESTART_GS=0` (skip GS container restart in deploy script)
- `CLASS_BUNDLE=/absolute/path/to/newgames-gs-runtime-*.tar.gz` (deploy prebuilt bundle instead of local compile)
- `GS_ENDPOINT_BASE`, `GS_CONTAINER`, `LAUNCH_URL`
- `AUTO_START_NGS=0`, `NGS_BASE_URL`, `BET_AMOUNT`, `BET_TYPE`
- `PROBE_LAUNCH=1` (enable launch route probe in `runtime:status`; default is non-mutating mode)
- `TARGET_BETS_PER_SEC`, `TARGET_PLACEBET_P95_MS`, `TARGET_COLLECT_P95_MS` (proof-pack SLO thresholds)

## Notes
- If `GS_INTERNAL_BASE_URL` is not set, the service uses local mock GS-internal behavior.
- Contract version for GS-internal integration is `v1`.
- `opengame` accepts optional `gsInternalBaseUrl` to override GS internal base URL per session
  (used for bank-level routing from legacy GS).
- `opengame` auto-recovers stale SID mismatches by retrying GS session validation with the expected SID when GS reports `Mismatch sessionId`.
