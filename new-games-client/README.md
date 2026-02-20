# New Games Client

PixiJS frontend for new games, starting with Plinko.

## Stack
- Vite + TypeScript
- PixiJS (`latest`)

## Run
```bash
npm install
npm run dev
```

## Environment
Create `.env` if needed:

```bash
VITE_NGS_API_BASE=http://localhost:6400
```

## Current Scope
- `opengame -> placebet -> collect` flow.
- Deterministic slot animation using server-provided round + slot.
- BetOnline-themed game shell inspired by reference Plinko layout (table board, top chrome, bottom action strip).
- Branded startup preloader with BetOnline logo and animated progress bar.
- Physics-style ball simulation with gravity, peg collisions, rebound damping, and slot-target steering.
- Autobet always renders one visible ball per round (no hidden/skip mode), with optional fast visual pacing.
- Board geometry scales both up and down when rows change (`10..13`), and payout buckets stay aligned to the pyramid base.
- Landing funnel/divider collision guards prevent balls from finishing between payout buckets.
- Enhanced visuals:
  - metallic pegs with layered highlights/shadows,
  - per-pin touch spark effects,
  - symmetric pocket walls and payout pots,
  - slot flash + impact explosion when a ball is collected by a slot.
- Rows/risk model aligned with Betsoft-style Plinko behavior:
  - line counts `10..13`,
  - low/medium/high risk payout tables,
  - center-heavy (binomial) chance profile with row-dependent slot count.
- Client parity UX modules:
  - settings (`ball speed`, `sound toggle`),
  - autobet (`rounds`, `interval`, `start/stop`, progress),
  - history panel (local round feed + server `readhistory` sync),
  - rules panel,
  - bottom KPI strip including `Total Win` accumulator.
- HUD layout is constrained to prevent left-panel updates from shrinking/hiding stage actions.
- Desktop layout is pinned to viewport height (`100dvh`) so history/log growth does not push the bottom action bar off-screen.
- Reads GS launch params from URL when available:
  - `sid/sessionId`, `bankId`, `ngsApiUrl`, `gsInternalBaseUrl`.
- Supports GS redirect parameter casing variants:
  - `SID`/`sid`, `BANKID`/`bankId`, and related uppercase/lowercase launch keys.
- If GS rotates to a newer SID, client syncs to the returned session and shows a clear stale-session message when relaunch is required.
- Auto Bet start now has guarded error handling and reports failures in the status panel without leaving UI controls in a stuck state.
- `Open Game` button was removed; session open is performed automatically on first spin/auto/history action.
- Ball path decisions are now generated with a near-shortest probabilistic planner:
  - each row still uses chance-based left/right choice,
  - choices are constrained by remaining steps/rights so trajectory stays close to the shortest feasible route to the math-selected bucket.
- Pin rebound tuning is reduced (lower restitution, stronger damping/drag) so paths look less “hyper-bouncy” and more gravity-led.
- Collision response now includes:
  - tangential friction damping,
  - lightweight spin/angular coupling,
  - tiny collision-normal jitter to mimic board/ball micro-imperfections.
- Bottom-bucket behavior tuning:
  - drastically reduced terminal steering,
  - divider push softened,
  - final horizontal settle correction capped to a very small value to avoid visible “fly into bucket” artifacts.
- Result-lock trajectory flow:
  - animation is now aligned to settled payout (`collect.winAmount`) so rendered bucket and paid multiplier match.
- Collision-time guidance:
  - each peg impact applies a subtle target-direction nudge (configurable) so path changes happen on realistic bounce events, not only near the bottom.
- Physics Tuning now includes:
  - `Peg Nudge` (collision steering strength toward the settled target bucket).
- Geometry and scale updates:
  - board now starts from a 2-pin top row (no single apex pin),
  - pin/player figures are scaled up,
  - ball visual/physics radius is doubled for stronger readability.
