# 01 Executive Summary

## High-confidence product picture
- The donor opens with a gate screen, then enters a single-screen slot HUD with balance/bet/win and action controls. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/desktop_001_intro.png`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/desktop_002_main_idle.png`)
- Menu overlays include `Game rules` and `How to play?`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/desktop_003_menu_open.png`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_004_rules_open.txt`)
- Reconciled truth confirms Buy Bonus modal presence and options. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/assets/screenshots/screenshot_04_buy_bonus_probe.png`)
- Reconciled truth confirms autoplay capture in replacement clip. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/AUTOPLAY_RECONCILED.md`)

## Core mechanics (factual)
- The How-to text defines `Collect Feature`, `Chicken Boost Feature`, `Bonus Game`, and jackpot classes. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- How-to states fixed 8 active paylines. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- Jackpot table was directly captured at three bet levels in Phase 1A: bet `0.2/0.3/0.4` with corresponding mini/minor/major/grand values. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/JACKPOT_BET_TABLE.csv`)

## What changed after reconciliation
- Several Phase 1C clips were reclassified; the final source of truth is the Phase 1D reconciled index/audit, not raw Phase 1C labels. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/ARTIFACT_AUDIT.md`)
- The old `video_11_twenty_spins_continuous.mp4` claim was replaced by `video_11_twenty_spins_continuous_FIXED.mp4`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/TWENTY_SPIN_RECONCILED.md`)

## Key implementation takeaway
- Treat this donor as mechanic reference only (collect + boost + bonus-buy layering), and use GSRefactor shared shell/runtime contracts for execution. (Evidence for donor mechanics: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`; Evidence for GS shell/runtime conventions: `Gamesv1/games/premium-slot/docs/ARCHITECTURE_MAP.md`)
