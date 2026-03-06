# VIDEO DESCRIPTIONS

## preflight_test.mp4
- Visible: live donor UI, reel/win-state motion.
- Trigger: initial pre-flight recording while interacting with controls.
- Effect/state change: UI transitions and spin/win animation frames are visible.
- Category: pre-flight validation evidence.
- Limitation: not part of required `video_01..video_11` set.

## video_01_idle_to_spin.mp4
- Visible: idle HUD, then spin initiation.
- Trigger: spin command from idle state.
- Effect/state change: idle -> active spin start.
- Category: spin start.
- Limitation: short clip focused on transition only.

## video_02_losing_spin.mp4
- Visible: full spin sequence ending at zero-win state.
- Trigger: manual spin command.
- Effect/state change: spin start -> settled losing outcome.
- Category: loss.
- Limitation: clip includes one non-target spin before the zero-win settle in this take.

## video_03_nonzero_win.mp4
- Visible: full spin ending with non-zero win text.
- Trigger: manual spin command.
- Effect/state change: spin start -> settled non-zero outcome.
- Category: win.
- Limitation: no payout math inferred beyond visible win text.

## video_04_menu_overlay.mp4
- Visible: burger/menu panel open then close.
- Trigger: burger button click twice.
- Effect/state change: menu overlay toggled.
- Category: menu UI.
- Limitation: only open/close flow captured.

## video_05_rules_or_howto.mp4
- Visible: rules view opened from menu and then closed.
- Trigger: burger menu open -> rules selection -> close action.
- Effect/state change: rules overlay visible then dismissed.
- Category: rules/how-to UI.
- Limitation: no deep scroll/readthrough in this clip.

## video_06_bet_change_and_jackpots.mp4
- Visible: repeated bet up/down actions with jackpot values changing.
- Trigger: bet plus/minus controls.
- Effect/state change: at least three bet levels shown with updated jackpot numbers.
- Category: bet/jackpot.
- Limitation: no extrapolation of jackpot scaling logic.

## video_07_hold_for_turbo_probe.mp4
- Visible: hold input applied on the turbo-labeled control during active spin.
- Trigger: spin start then sustained hold interaction.
- Effect/state change: hold-for-turbo control receives held input during gameplay.
- Category: turbo probe.
- Limitation: no hidden turbo internals inferred.

## video_08_autoplay_probe.mp4
- Visible: autoplay probe control click followed by manual spin evidence.
- Trigger: probe control interaction and subsequent spin command.
- Effect/state change: no sustained autonomous spin loop observed after probe click in this run.
- Category: autoplay probe.
- Limitation: absence is based on observed clip behavior only.

## video_09_buy_bonus_probe.mp4
- Visible: repeated click attempts on BUY BONUS canvas area.
- Trigger: direct clicks at BUY BONUS screen location.
- Effect/state change: no buy-bonus modal/options became visible in this clip.
- Category: buy bonus probe.
- Limitation: BUY BONUS is canvas-rendered; probe is based on visible response only.

## video_10_special_effect_or_collect_state.mp4
- Visible: special coin/multiplier-style win-state visuals during spin sequence.
- Trigger: consecutive spins.
- Effect/state change: special-style visual moments and non-zero win overlays observed.
- Category: special effect / collect-like state.
- Limitation: no hidden mechanic attribution beyond visible effects.

## video_11_twenty_spins_continuous.mp4
- Visible: continuous 20-spin sequence without clip break.
- Trigger: repeated spin commands in one uninterrupted recording.
- Effect/state change: 20 sequential outcomes; timestamped log recorded in `SPIN_RUN_LOG.csv`.
- Category: continuous run.
- Limitation: win text was sampled from visible overlay; no internal data access.
