# ChickenGame Bet Ladder

Scope:
- Exact USD bet preset/dropdown values produced by the donor client for ChickenGame.

Evidence:
- `assets/_research_runs/ChickenGame/codex-phase1f-paylines-bets-20260307-1608/assets/snapshots/usd_bets_config_live.txt`
- `assets/_research_runs/ChickenGame/codex-phase1f-paylines-bets-20260307-1608/assets/snapshots/usd_bet_ladder_logic.txt`
- `assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/assets/snapshots/snapshot_06_autoplay_probe.txt`

Live USD bounds observed from donor config:
- `minBetAmount: 0.10`
- `maxBetAmount: 20000.00`
- `betPresets: []`

ChickenGame client-side preset/dropdown ladder:
- `0.1`
- `0.2`
- `0.3`
- `0.4`
- `0.5`
- `0.6`
- `0.7`
- `0.8`
- `0.9`
- `1`
- `2`
- `3`
- `4`
- `5`
- `6`
- `7`
- `8`
- `9`
- `10`
- `15`
- `20`
- `25`
- `50`
- `75`
- `100`
- `150`
- `200`

Implementation note backed by the extracted client logic:
- ChickenGame generates the visible preset/dropdown list from the live min/max bounds and the threshold-step table in `usd_bet_ladder_logic.txt`.
- For ChickenGame, the bet UI passes `maxPresetDivider: 100`, so the preset/dropdown list is generated up to `lt(maxBetAmount, 100) = 200`.
