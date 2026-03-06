# 06 VFX Audio Timings

## Method note

- Timings below are approximate and derived from reconciled clip durations plus sampled frame positions (`p00/p50/p95` and fixed first/mid/last thumbs).
- This is observational timing only; no internal timeline traces were captured for these specific effects.

## Reconciled clips used

- Special/collect-like: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/assets/video/video_10_special_effect_or_collect_state.mp4`
- Buy bonus probe (primary truth source): `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/assets/video/video_09_buy_bonus_probe.mp4`
- Autoplay fixed: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video/video_08_autoplay_probe_FIXED.mp4`
- 20-spin fixed: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video/video_11_twenty_spins_continuous_FIXED.mp4`

## Timing observations

- Special/highlighted result state is clearly represented from mid to late section of `video_10_special_effect_or_collect_state.mp4`, approx `00:10.88-00:20.67` (p50->p95 window).
  - Evidence thumbs: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_10_special_effect_or_collect_state_p50.jpg`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_10_special_effect_or_collect_state_p95.jpg`

- Buy Bonus modal/options are visible in the later part of `video_09_buy_bonus_probe.mp4`, approx around `00:23.40` (p75 sample), with close/control still visible by late segment.
  - Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_09_buy_bonus_probe_p75.jpg`

- Autoplay run-state continuity is visible across `video_08_autoplay_probe_FIXED.mp4` from start through mid and late (`00:00.00`, `00:41.38`, `01:18.62` approx checkpoints), indicating repeated autonomous progression between outcomes.
  - Evidence thumbs: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_08_autoplay_probe_FIXED_first.jpg`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_08_autoplay_probe_FIXED_mid.jpg`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_08_autoplay_probe_FIXED_last.jpg`

- 20-spin replacement continuity is represented across `video_11_twenty_spins_continuous_FIXED.mp4` checkpoints (`00:00.00`, `02:23.50`, `04:32.65` approx), supporting long uninterrupted gameplay progression.
  - Evidence thumbs: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_11_twenty_spins_continuous_FIXED_first.jpg`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_11_twenty_spins_continuous_FIXED_mid.jpg`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video-thumbs/video_11_twenty_spins_continuous_FIXED_last.jpg`

## Limitations

- UNOBSERVED: frame-accurate milliseconds for specific sub-effects (for example exact lightning onset after press) because those events were not annotated frame-by-frame during Phase 1 capture runs.
