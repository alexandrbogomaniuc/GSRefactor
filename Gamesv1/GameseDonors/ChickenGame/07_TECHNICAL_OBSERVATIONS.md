# 07 Technical Observations

## Console findings (captured)

- Console captured CORB issues and accessibility/form warnings.
- Console captured browser autoplay/audio restriction warning (`AudioContext was not allowed to start until user gesture`).
- Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/console/console_capture.txt`

## Network findings (captured)

- One capture listed 181 network requests including JS bundles, CSS, animation JSON/atlas/png assets, and many audio cues.
- Runtime requests include `api/modes/game` and `POST /api/auth` with redacted auth query values in stored notes.
- Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt`

## Performance findings (captured)

- Load trace note reports `LCP: 137 ms`, `CLS: 0.00` (lab), no CPU/network throttling.
- Active-spin trace note reports `INP: 82 ms`, `CLS: 0.00` (lab), no CPU/network throttling.
- Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/perf/perf_load_note.txt`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/perf/perf_spin_note.txt`

## What is intentionally not claimed

- UNOBSERVED: internal renderer/framework implementation details beyond what file requests and captures explicitly show.
- UNOBSERVED: internal math engine, RTP internals, and private back-end outcome calculation logic.
