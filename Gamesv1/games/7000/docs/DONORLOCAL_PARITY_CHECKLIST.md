# Donorlocal Parity Checklist

Base branch for this sprint:

- `codex/qa/7000-donorlocal-benchmark-mode-20260317-1117`

Working branch:

- `codex/qa/7000-beta6-donorlocal-parity-pass-20260317-1210`

Benchmark URL:

- `http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional`

This checklist tracks the donorlocal-first parity pass completed in Beta 6.

| Area | Donorlocal benchmark quality | Current game quality | Top issue | Action taken in this sprint |
| --- | --- | --- | --- | --- |
| Preloader | Strong centered lockup with premium plate, clear brand stack, and richer perceived loading motion. | Materially closer. The lockup now reads as a composed branded screen instead of a default loader. | Still lacks authored hero motion and richer end-stinger polish. | Strengthened the lockup plate, background flares, floor glow, status chip, progress bar treatment, and ember motion in `LoadScreen.ts`, while preserving the approved logo/bar composition. |
| Top area | Donorlocal wants a clear topper hero with readable jackpot framing and less debug text competition. | Improved. The topper now owns the top-center space and the benchmark chrome is no longer fighting stacked header text. | Jackpot plaques still read more like framed runtime elements than fully authored premium plaques. | Removed the redundant top-center benchmark header, lifted/tightened the topper plate, strengthened aura/plate scale, and retuned mascot/title spacing in `MainScreen.ts` and `TopperMascotController.ts`. |
| Reel frame/bed | Donorlocal favors a heavier cabinet with depth, glow, and a premium stage boundary around the reel bed. | Improved. The cabinet now feels more intentional and better separated from the background. | Still short on bespoke art texture and authored material variation. | Expanded backplate depth, stage aura, inner framing, lower pedestal, and marquee treatment in `Beta3VisualChrome.ts`. |
| Controls | Donorlocal favors a confident bottom rail with clear button hierarchy and less placeholder-shell energy. | Improved. The bottom control bar now reads like a cabinet component instead of floating generic buttons. | Secondary controls still rely on fallback geometry where authored art is unavailable. | Strengthened rail glow/inset, spin pedestal, secondary badge plates, and ambient motion in `HeroHudChrome.ts`. |
| Buy bonus | Donorlocal expects the buy tile to look intentional, compact, and readable at a glance. | Improved. The tile reads cleaner and the copy no longer wraps like a debug stub. | It still lacks bespoke buy art states and a more premium icon plate. | Reworked buy-tile spacing, shortened the copy to `LAUNCH HOLD & WIN`, widened the caption wrap, and retuned the tile framing in `Beta3VisualChrome.ts`. |
| Feature moments | Donorlocal expects collect, boost, and jackpot to feel staged and distinct rather than sharing one generic reaction path. | Improved. Collect/boost/jackpot now diverge more clearly in topper text, FX, lightning, coin spread, and plaque emphasis. | Bonus and jackpot are still lighter than a fully authored donor sequence with dedicated art and sound layers. | Increased collect/boost/jackpot intensity in `LayeredFxController.ts`, upgraded line/feature titles in `MainScreen.ts`, and kept the exact math-driven payline system intact. |
| Idle liveness | Donorlocal keeps the cabinet feeling alive even when nothing is happening. | Improved. The scene now has more ambient motion and less static dead space. | Some areas still rely on subtle pulses rather than authored idle loops. | Added more motion to cabinet glow, topper float, control breathing, preloader embers, and stage aura across the chrome controllers. |
| Typography / hierarchy | Donorlocal keeps a clearer hierarchy between hero title, state messaging, plaques, and support labels. | Improved. The main hierarchy is cleaner and less debug-forward than the benchmark-mode branch. | Some runtime labels still use fallback fonts and simple text rendering rather than authored numerals/plates. | Suppressed the redundant legacy top header, tightened topper text spacing, and improved the buy/action panel readability without changing GS or provider architecture. |

## Proof set

Proof screenshots for this pass live under:

- `Gamesv1/games/7000/docs/_visual_proof/beta6-2026-03-17/`

Included files:

- `preloader.png`
- `idle.png`
- `top-area.png`
- `control-cluster.png`
- `collect.png`
- `boost.png`
- `jackpot.png`
