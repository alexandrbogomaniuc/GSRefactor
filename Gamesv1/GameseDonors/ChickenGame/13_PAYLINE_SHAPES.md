# ChickenGame Payline Shapes

Scope:
- Exact PAY LINES shapes for the donor's fixed 8-line layout.

Board convention:
- 3 reels x 4 rows.
- `rowIndex` is 0-based from top to bottom.

Evidence:
- `assets/_research_runs/ChickenGame/codex-phase1f-paylines-bets-20260307-1608/assets/screenshots/pay_lines_table_mobile_c0a70cb8.png`
- `assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt`
- `assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`

Deterministic mappings:
- `line_01: [0, 0, 0]`
- `line_02: [1, 1, 1]`
- `line_03: [2, 2, 2]`
- `line_04: [3, 3, 3]`
- `line_05: [0, 1, 2]`
- `line_06: [1, 2, 3]`
- `line_07: [2, 1, 0]`
- `line_08: [3, 2, 1]`
