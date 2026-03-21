# Runtime Bugfix Report

Branch: `codex/qa/7000-runtime-bugfix-clean-20260321-0805`  
Base commit: `a48b449afb6472226cc61ef73a14553fefcabbbd`  
Benchmark path used: `http://127.0.0.1:8092/?allowDevFallback=1&assetProvider=donorlocal&mathSource=provisional`

## A) Root Cause

### Confirmed mismatch
- `lineWins` carried non-zero `amountMinor` values.
- Runtime win counter state could remain `0`.

### Why it happened
1. In [`MainScreen.ts`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/screens/main/MainScreen.ts), the `showWinCounter` callback was gated by `shouldShowWinCounter(...)`.  
   For donorlocal small wins, the gate can suppress `showWin(...)`, so the counter state was not reliably updated for every settled winning round.
2. In [`WinCounter.ts`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/game/ui/WinCounter.ts), `hideNow()` previously zeroed internal values, so hidden-counter flows could lose reported totals.
3. In round settlement ([`MainScreen.ts`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/screens/main/MainScreen.ts)), `settledPresentation.winAmount` could lag bridge line-win totals for donorlocal provisional flow, creating a display/runtime mismatch path.

### Fix applied
1. `WinCounter` now has explicit `reportWin(amount)` state propagation (`reportedTotalValue`) independent of visibility.
2. `hideNow()` now preserves the reported total instead of forcing internal values to `0`.
3. `MainScreen` now:
   - always calls `winCounter.reportWin(amountMinor)` inside the win-counter callback before visibility gating,
   - computes `hintedLineWinAmountMinor` from `mathBridgeHints.lineWins`,
   - uses `max(settledPresentation.winAmount, hintedLineWinAmountMinor)` for settlement reporting.

## B) Before/After Comparison

### Before (reproduced on this clean branch before patching)
- Sample winning round:
  - `lineWinAmountMinor = 4`
  - `winCounter.targetValue = 0`
  - visible runtime total could stay zero despite non-zero line wins.

### After (post-fix runtime probe)
- 10-spin post-fix probe:
  - each spin: `lineWinAmountMinor = 4`
  - each spin: `winCounter.targetValue = 4`
  - each spin: `winCounter.reportedTotalValue = 4`
  - mismatch count: `0/10`

### Conclusion
- Runtime-reported win total now tracks line-win minor units correctly in the donorlocal provisional path.

## C) Remaining Suspicion Check

### Repeated boards
- Confirmed: repeated boards are excessive in no-preset provisional run.
- 100-spin sample:
  - repeated-board flagged spins: `97`
  - unique visible boards: `3`

### Win frequency
- 100-spin sample win frequency: `100/100` (`100%`).

### Why this still happens
- This is not the same bug as the win-total propagation issue.
- It is caused by provisional source topology:
  - [`provisionalMathSource.ts`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/provisionalMathSource.ts) sets `preset = requestedPreset ?? "normal"` in `nextOutcome(...)`.
  - `PRESET_OVERRIDES.normal` is a fixed column matrix, so “no preset” still uses deterministic normal layout.

### Answer to “display bug vs deeper runtime issue”
- Display bug: **fixed** (counter now matches line-win totals).
- Runtime suspicion (repetition / 100% wins): **still real**, but it is a provisional-math topology issue, not a win-counter propagation issue.

## D) Evidence Files

- 100-spin data (after fix):  
  [`RUNTIME_SANITY_100_SPINS_AFTER_FIX.json`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/RUNTIME_SANITY_100_SPINS_AFTER_FIX.json)
- Proof folder:  
  [`runtime-bugfix-2026-03-21`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/_visual_proof/runtime-bugfix-2026-03-21)
- Winning sample screenshot:  
  [`donorlocal-winning-spin-with-total.png`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/_visual_proof/runtime-bugfix-2026-03-21/donorlocal-winning-spin-with-total.png)
- Repeated-board sample screenshot:  
  [`donorlocal-repeated-board-example.png`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/_visual_proof/runtime-bugfix-2026-03-21/donorlocal-repeated-board-example.png)
- Losing-spin note screenshot (no true losing spin observed in this 100-spin no-preset run):  
  [`donorlocal-losing-spin-not-observed.png`](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/_visual_proof/runtime-bugfix-2026-03-21/donorlocal-losing-spin-not-observed.png)
