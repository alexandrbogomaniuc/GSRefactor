# Video Action/Reaction Map (REC-20260318101534)

Source video:
- `/Users/alexb/Movies/TapRecord/Video/REC-20260318101534.mp4`

Analysis artifacts:
- `/tmp/rec_20260318101534_analysis/contact_every2s.png`
- `/tmp/rec_20260318101534_analysis/seq_early.png`
- `/tmp/rec_20260318101534_analysis/seq_collect_transition.png`
- `/tmp/rec_20260318101534_analysis/seq_linewin.png`
- `/tmp/rec_20260318101534_analysis/seq_multiline.png`
- `/tmp/rec_20260318101534_analysis/seq_special_intro.png`
- `/tmp/rec_20260318101534_analysis/seq_big_lightning.png`
- `/tmp/rec_20260318101534_analysis/seq_jackpot_fx.png`
- `/tmp/rec_20260318101534_analysis/seq_feature_intro2.png`
- `/tmp/rec_20260318101534_analysis/seq_final_fx.png`

## Observed timeline (directly from video)

- `00.00s–02.00s`
  - Idle board, no major overlays.
- `02.00s–03.00s`
  - Spin starts and settles.
- `03.00s–04.25s`
  - Static result board, then next spin starts.
- `08.50s–10.25s`
  - Spin resolves with a single `1x` coin visible on lower left.
- `10.50s–12.50s`
  - Board dims and countdown appears (`0 → 1 → 2 → 3`).
- `19.75s–21.25s`
  - Coin-fly trails trigger from reel area to topper.
  - Topper shows green `+5`.
- `26.00s–27.50s`
  - Multi-coin result (`5x`, `1x`) with multiple fly-trails and topper increment.
- `33.00s–34.25s`
  - Dimmed board with top-row grape emphasis and repeated `24` markers.
- `46.00s–48.00s`
  - Strong full-bed red glow + lightning burst.
  - Center coin emblem highlighted.
  - Top labels cycle rapidly (`MINOR`, `x10`, `MINI`, `x10`, `MAJOR`).
- `57.25s–59.00s`
  - Another multiplier coin-fly burst with topper increment.
- `74.50s–75.00s`
  - Second dim/countdown sequence (`0 → 1 → 2` observed in sampled frames).
- `85.50s–87.00s`
  - Left-column stacked multipliers (`5x`, `3x`, `2x`) then multi-trail fly burst and topper increment.

## Findings

1. Coin-fly assets in this recording appear mostly uncropped (major prior crop issue looks improved).
2. Feature moments still overlap visually: lightning, topper increment, and jackpot label cycling stack together in the same short window.
3. Jackpot plaque behavior reads as rapid label swapping at top center, not a single clear plaque-driven reveal.
4. Countdown/dim transitions are present but feel abrupt; dim layer keeps some symbol ghosts and bottom strip visibility.
5. Collect/boost/jackpot moments are not clearly distinct enough in visual identity; many use the same coin-fly + topper `+5` pattern.

## Next adjustments to implement

1. Add stricter state ownership for post-win FX:
   - During jackpot/boost scenes, suppress non-owner overlays and generic cue lane effects.
2. Re-time burst choreography into phases:
   - phase A: settle,
   - phase B: anticipation,
   - phase C: single primary effect,
   - phase D: topper/plaque reaction,
   - phase E: release.
3. Jackpot presentation:
   - lock to one winning plaque reaction per burst (no rapid multi-label swaps in one beat).
4. Countdown transition polish:
   - tighten dim mask and reduce ghosted symbol leakage during countdown states.
5. Feature differentiation:
   - collect: coin-fly + topper only,
   - boost: stronger reel-bed pulse + selective lightning,
   - jackpot: plaque-first callout + controlled lightning follow-through.
