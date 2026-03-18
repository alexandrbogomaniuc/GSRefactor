# Beta 5C Review

## What Improved Vs Beta 5B

- Normal provisional QA now has a real multi-line sequence instead of only single-line proof boards.
- Winning lines no longer read like debug plaques:
  - the callout is now a product-style donor-inspired banner,
  - line id is isolated in its own badge,
  - multiplier and payout have stronger hierarchy,
  - multi-line presentation exposes a sequence chip (`1/2`, `2/2`).
- Line sequencing is more staged:
  - trace-in,
  - hold/pulse,
  - fade-out,
  - next line.
- Feature-linked line presentation is now visibly distinct:
  - `collect` uses a warm payout banner,
  - `boost` uses a hotter strike treatment,
  - `bonus` uses its own entry label/state,
  - `jackpot` uses the strongest premium/gold treatment.
- Feature timing now respects the line sequence more cleanly, so collect/boost/jackpot/bonus do not all pile onto the first visible line at once.
- Cue priority is cleaner:
  - boost no longer falls back to collect messaging,
  - bonus entry no longer inherits boost banner text,
  - jackpot keeps jackpot-specific messaging.

## What Still Feels Generic

- The line renderer is still vector-based original code, not a donor-authored atlas/Spine package.
- The callout is donor-inspired, but not yet backed by bespoke art or donor-like counting numerals.
- Win-counter choreography is still driven by the generic shared `WowVfxOrchestrator` / `WinCounter` path.
- Jackpot/bonus/boost states are more distinct now, but the mascot/plaque/board choreography is still lighter than true donor-grade authored sequencing.

## Remaining Work

### a) Animation / choreography

- Add authored enter/loop/finish states around line banners instead of a single pulse/fade treatment.
- Sequence mascot/plaque/fire/lightning reactions with more donor-like phase separation.
- Add a short captured clip for the multi-line normal sequence so QA can review timing, not just stills.

### b) Art

- Replace vector banners/badges with bespoke original art plates.
- Add dedicated line-win iconography, better number styling, and more premium board-local highlight dressing.
- Improve provider-specific polish for the line banner so donorlocal benchmarking uses richer art language.

### c) Layout / polish

- Fine-tune banner placement per payline shape so steep diagonals and top-row wins never feel crowded.
- Reconcile HUD/status copy with the richer line presentation so the shell reads as one choreography layer.
- Improve payout readability at low-dev bets where minor-unit wins are necessarily small.

### d) Math / certification

- Replace provisional preset boards with certified/backend-authored QA envelopes once available.
- Confirm production line-win ordering, pacing, and payout formatting against GS-authoritative outcome payloads.
- Keep browser math non-authoritative; this remains a DEV-only choreography bridge.
