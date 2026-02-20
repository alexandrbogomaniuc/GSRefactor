# M4 Proof Pack

Last updated: 2026-02-13

This document defines the repeatable command to generate M4 evidence for:
- load target check (`>=100 bets/sec`),
- latency SLO checks (`placebet p95 <= 250ms`, `collect p95 <= 300ms`),
- live runtime chain check (`runtime:e2e`),
- runtime health snapshot (`runtime:status`).

## Command
```bash
cd /Users/alexb/Documents/Dev/new-games-server
npm run runtime:proof-pack
```

## Output Artifacts
- Markdown report:
  - `/Users/alexb/Documents/Dev/docs/New games Project/evidence/m4-proof-pack-<UTC>.md`
- Raw outputs:
  - `/Users/alexb/Documents/Dev/new-games-server/artifacts/perf/perf-proof-<UTC>.json`
  - `/Users/alexb/Documents/Dev/new-games-server/artifacts/perf/perf-proof-<UTC>.stderr.txt`
  - `/Users/alexb/Documents/Dev/new-games-server/artifacts/perf/e2e-proof-<UTC>.txt`
  - `/Users/alexb/Documents/Dev/new-games-server/artifacts/perf/status-proof-<UTC>.txt`

## Threshold Overrides
- `TARGET_BETS_PER_SEC` (default `100`)
- `TARGET_PLACEBET_P95_MS` (default `250`)
- `TARGET_COLLECT_P95_MS` (default `300`)

Example:
```bash
TARGET_BETS_PER_SEC=120 npm run runtime:proof-pack
```

## Notes
- `runtime:status` is executed in non-mutating mode by default (`PROBE_LAUNCH=0`) to avoid session invalidation side-effects.
- If NGS is not running and `AUTO_START_NGS=1`, proof-pack starts it automatically.
