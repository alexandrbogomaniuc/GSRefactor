# 04 Gameplay And Mechanics

## FACTS

### Board and line model (as documented/observed)

- How-to text states fixed active lines: `8`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- Idle and after-stop captures show a visible board of `3` reels (columns) by `4` rows. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/desktop_002_main_idle.png`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/spin_001_after_stop.png`)

### Symbol families observed in evidence

- Fruit symbols observed in readable spin rows: cherries, lemons, oranges, plums, watermelons, grapes. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/OBSERVED_SPINS.csv`)
- `BAR` appears in readable spin rows. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/OBSERVED_SPINS.csv`)
- `777` symbol asset is present in captured network loads. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt`)
- Coin-related symbols appear in evidence: `CHICKEN_COIN`, `SUPER_CHICKEN_COIN`, `COIN_1X`; How-to also lists multiplier outcomes up to `x10`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/OBSERVED_SPINS.csv`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)

### Collect + Boost (How-to statements)

- Collect Feature trigger and payout behavior are explicitly described in the How-to modal text. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- Chicken Boost Feature is explicitly described with three random bonus categories: multiplier boost, jackpot trigger, extra coins. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)

### Bonus game + jackpots (How-to statements)

- Bonus Game trigger condition is documented as Bonus Coins on reels 1 and 3 plus Chicken/Super Chicken on reel 2. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- Jackpot classes and bet multipliers are documented as MINI 25x, MINOR 50x, MAJOR 150x, GRAND 1000x. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- Phase 1A captured concrete jackpot values at bet `0.2`, `0.3`, and `0.4`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/JACKPOT_BET_TABLE.csv`)

### Buy Bonus tiers and prices

- How-to text documents 3 Buy Bonus tiers: `75 bets`, `200 bets`, `300 bets`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`)
- Reconciled evidence reports visible modal prices at bet `0.6`: `45`, `120`, `180`. (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/assets/screenshots/screenshot_04_buy_bonus_probe.png`)

## INFERENCE

- INFERENCE: Buy Bonus modal prices at bet `0.6` are consistent with `75x/200x/300x` tier multipliers from How-to (`0.6*75=45`, `0.6*200=120`, `0.6*300=180`). (Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`)

## UNOBSERVED

- UNOBSERVED: exact payout table for every symbol in every count from high-confidence readable captures.
- UNOBSERVED: exact feature probability weights and exact jackpot selection weights.
