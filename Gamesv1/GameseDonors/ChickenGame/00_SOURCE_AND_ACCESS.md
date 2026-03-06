# 00 Source And Access

## Donor URL (redacted)
- Source launch pattern used in evidence:
  - `https://api.inout.games/api/launch?gameMode=chicken-coin&operatorId=ee2013ed-e1f0-4d6e-97d2-f36619e2eb52&currency=USD&lang=en`
- In captured runtime URLs, auth query value is redacted. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt`)

## Redirect/auth observations
- A runtime request is captured to `https://chicken-coin.inout.games/api/modes/game?...&authToken=[REDACTED]...` with HTTP 200. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt`)
- A `POST https://api.inout.games/api/auth` request is captured with HTTP 201. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt`)

## Tooling used in evidence runs
- Chrome DevTools MCP screenshots/snapshots/network/console/performance were used in Phase 1A. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/SESSION_LOG.md`)
- DevTools screencast recording was used in Phase 1C, then reconciled in Phase 1D. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/VIDEO_VALIDATION.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/ARTIFACT_AUDIT.md`)

## Sanitization steps for canonical path
- Scope scanned: `Gamesv1/GameseDonors/ChickenGame/`
- Patterns scanned: `authToken=`, `token=`, `session=`, `cookie`
- Result: see bottom section "Scan Result" after command output refresh in this run.

## Limitations
- This Phase 2 run did not replay or re-capture donor gameplay; it is synthesis-only from committed Phase 1A/1C/1D evidence. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/README.md`)
- Many later Phase 1A spin rows are marked unreadable due capture timing, so symbol-level certainty is strongest for the first sampled rows and reconciled clips. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/OBSERVED_SPINS.csv`)

## Scan Result
- Completed on 2026-03-06 in this Phase 2 run.
- Pattern matches exist in evidence files, but auth/session values are stored as redacted placeholders where present.
- Raw-like secret scan (`authToken=`, `token=`, `session=`, `cookie`) returned `0` non-redacted credential-like hits in `Gamesv1/GameseDonors/ChickenGame/`.
