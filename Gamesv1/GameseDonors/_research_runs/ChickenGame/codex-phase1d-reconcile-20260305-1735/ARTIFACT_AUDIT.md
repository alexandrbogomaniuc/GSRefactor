# ARTIFACT_AUDIT

Phase 1C video artifacts were audited against committed media frames and contact sheets under:
- `assets/video-thumbs/*`
- `assets/screenshots/*_contact.jpg`

| filename | audit_status | actual_visible_content | old_claim | action_taken | replacement |
|---|---|---|---|---|---|
| preflight_test.mp4 | MATCHES_DESCRIPTION | Live gameplay viewport visible; not black. | preflight validity check | kept as valid preflight evidence | n/a |
| video_01_idle_to_spin.mp4 | MISDESCRIBED | Clip contains active spin/result states and does not cleanly start from stable idle. | idle -> spin start | relabeled in reconciled index/description | n/a |
| video_02_losing_spin.mp4 | MISDESCRIBED | Contains non-zero win state (`0.4`) in sampled frames. | full losing spin | relabeled as mixed-result spin capture | n/a |
| video_03_nonzero_win.mp4 | MATCHES_DESCRIPTION | Non-zero win state is visible and settles on win/result content. | non-zero win | kept | n/a |
| video_04_menu_overlay.mp4 | MATCHES_DESCRIPTION | Menu overlay open/close behavior visible. | menu overlay | kept | n/a |
| video_05_rules_or_howto.mp4 | WRONG_PURPOSE | Shows menu overlay state; no clear rules/how-to panel in audited frames. | rules/how-to open/close | relabeled as menu-state capture, not rules proof | n/a |
| video_06_bet_change_and_jackpots.mp4 | WRONG_PURPOSE | Contains bonus/instruction-like content, not a clean 3-level bet+jackpot change probe. | bet/jackpot change proof | relabeled as non-conforming for claimed purpose | n/a |
| video_07_hold_for_turbo_probe.mp4 | MISDESCRIBED | Mixed spin/menu states; hold-for-turbo behavior is not isolated clearly. | hold-for-turbo probe | relabeled as inconclusive for hold behavior | n/a |
| video_08_autoplay_probe.mp4 | NEEDS_REPLACEMENT | Old clip does not cleanly prove autonomous repeated spins. | autoplay probe | replaced with factual clip | video_08_autoplay_probe_FIXED.mp4 |
| video_09_buy_bonus_probe.mp4 | MATCHES_DESCRIPTION | Buy bonus modal/options are visible in committed media (see sampled p75 frame and Phase 1C screenshots). | buy bonus probe | kept and used as primary buy-bonus truth source | n/a |
| video_10_special_effect_or_collect_state.mp4 | MATCHES_DESCRIPTION | Special/highlighted win-effect states are visible. | special effect / collect-like state | kept | n/a |
| video_11_twenty_spins_continuous.mp4 | NEEDS_REPLACEMENT | Original clip includes non-target flow content and does not meet strict 20-spin proof quality. | 20-spin continuous run | replaced | video_11_twenty_spins_continuous_FIXED.mp4 |

Additional Phase 1D capture note:
- `video_09_buy_bonus_probe_FIXED.mp4` exists but is classified as WRONG_PURPOSE (does not provide clean buy-bonus modal proof) and is not used for final buy-bonus conclusion.
