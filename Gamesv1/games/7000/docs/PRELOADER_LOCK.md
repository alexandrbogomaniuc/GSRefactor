# Game 7000 Preloader Lock (Protected Baseline)

## Status
This preloader is now a protected baseline for Game 7000 and must not be changed by later asset/polish passes unless explicitly requested by product review.

## Source of truth
- Runtime screen logic: `Gamesv1/games/7000/src/app/screens/LoadScreen.ts`
- Locked logo assets:
  - `Gamesv1/games/7000/raw-assets/preload{m}/rooster-logo.png`
  - `Gamesv1/games/7000/raw-assets/preload{m}/betonline-logo.svg`

## Locked behavior/layout rules
- Keep the stacked composition:
  - rooster logo top,
  - BetOnline logo middle,
  - status phrase and skewed progress bar below,
  - `Powered by BetOnline Studios®` footer at bottom.
- BetOnline logo must preserve original SVG aspect ratio (no vertical stretch).
- Rooster logo keeps visible slow pulse.
- Portrait mode keeps enlarged rooster treatment and aligned center axis.

## Locked proof set
- `Gamesv1/games/7000/docs/_visual_proof/preloader-lock-2026-03-10/desktop-v5-aligned.png`
- `Gamesv1/games/7000/docs/_visual_proof/preloader-lock-2026-03-10/portrait-v5-aligned.png`

## Archive snapshot (restore-safe)
- `Gamesv1/games/7000/docs/_archive/preloader-lock-2026-03-10/LoadScreen.snapshot.ts`
- `Gamesv1/games/7000/docs/_archive/preloader-lock-2026-03-10/README.md`

## Restore procedure
1. Restore `LoadScreen.ts` from `LoadScreen.snapshot.ts`.
2. Ensure both locked logo assets above exist unchanged.
3. Run `corepack pnpm -C Gamesv1/games/7000 build`.
4. Validate against the locked proof screenshots.
