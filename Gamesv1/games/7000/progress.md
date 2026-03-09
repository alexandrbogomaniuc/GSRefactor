Original prompt: GAME ENGINEERING -- GAME #7000 "Crazy Rooster Hold&Win" (FIRST REAL DELIVERY) from origin/beta/gsrefactor-beta-20260306 with donor parity to Gamesv1/GameseDonors/ChickenGame, 3x4 board, 8 fixed paylines, collect/lightning/bonus/buy/autoplay/hold-for-turbo, BetOnline brand shell, WOW preloader, provider-switched placeholder assets, and final test/build/push audit report.

- 2026-03-07: Bootstrapped the authoritative repo at /Users/alexb/Documents/Dev/GSRefactor, checked out beta/gsrefactor-beta-20260306, and created branch audit/game-7000-engineering-20260307-1155.
- 2026-03-07: Installed workspace dependencies with pnpm and added a temporary local node shim in /tmp/node-shim/bin/node so repo scripts that use `node --experimental-strip-types` can run on the host Node version via tsx.
- 2026-03-07: Verified donor facts from ChickenGame pack:
  - 3 reels x 4 rows.
  - 8 fixed L->R paylines.
  - payline shapes from donor payline image:
    1: [0,0,0]
    2: [1,1,1]
    3: [2,2,2]
    4: [3,3,3]
    5: [0,1,2]
    6: [1,2,3]
    7: [2,1,0]
    8: [3,2,1]
  - bonus trigger uses coins on reels 1 and 3 plus chicken/super chicken on reel 2.
  - jackpots: 25x/50x/150x/1000x.
  - buy bonus tiers: 75x/200x/300x.
- 2026-03-07: Planned implementation scope:
  - keep almost all edits inside Gamesv1/games/7000,
  - only minimal shared/root script routing if required so `corepack pnpm -C Gamesv1 run build` and `test:layout` target @games/7000.
- 2026-03-08: Assembled QA beta branch `qa/7000-beta1-20260308-0935` from `eng/7000-crazy-rooster-20260307-1346` and imported both provider packs for QA review.
  - checked out the committed OpenAI runtime pack from `origin/codex/assets/7000-openai-polish-20260308-0914`.
  - the requested NanoBanana runtime pack was not present on `origin/assets/7000-nanobanana-crazy-rooster-phase1-20260307-1358`; only `raw-assets/providers/nanobanana` existed, so a partial QA runtime pack was assembled locally from those committed files and documented as incomplete.
  - added provider selection by query param/config, startup asset-pack validation, safe placeholder fallback behavior, and a branded preloader lockup that uses the BetOnline wordmark instead of the Pixi placeholder.
  - fixed demo fallback startup for `?allowDevFallback=1` and resolved ladder-mode config validation by clamping the runtime max bet to the configured preset range.
  - produced QA proof artifacts under `docs/_visual_proof/beta1-2026-03-08/` and added `docs/BETA_QA_CHECKLIST.md` with deterministic feature URLs for smoke testing.
- 2026-03-09: Started QA beta 2 provider-matrix pass on branch `qa/7000-beta2-provider-matrix-20260309-0754`.
  - expanded provider selection precedence to query param > `VITE_ASSET_PROVIDER` > config default.
  - added `donorlocal` as a DEV-only provider that loads from a runtime manifest under `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/runtime/manifest.json`.
  - donorlocal never uses static imports; missing or incomplete local manifests now warn and fall back to `openai` without crashing.
  - added provider-matrix docs and convenience dev scripts for one-port and three-port QA workflows.
- 2026-03-09: Validation and smoke results for QA beta 2.
  - `corepack pnpm -C Gamesv1 run test`, `test:layout`, `build`, and direct `corepack pnpm -C Gamesv1/games/7000 build` all passed under Node 22.
  - browser smoke on `http://127.0.0.1:8081/?allowDevFallback=1` confirmed:
    - `openai` resolves cleanly with `effectiveProvider=openai`.
    - `nanobanana` keeps `effectiveProvider=nanobanana` and remains `safePlaceholder=true` because the committed pack is still missing `vfxAtlas.file`.
    - `donorlocal` falls back to `effectiveProvider=openai` with a warning when the local manifest path is absent or returns non-JSON.
- 2026-03-09: Completed QA beta 2B real-provider rendering pass on branch `qa/7000-beta2b-real-provider-rendering-20260309-0956`.
  - imported the committed NanoBanana runtime atlases from `origin/assets/7000-nanobanana-runtime-20260309-0756` under `assets/providers/nanobanana/runtime/`.
  - replaced reel text tiles with atlas-driven symbol rendering using the fallback chain `selected provider -> openai -> placeholder`, with `debugSymbolLabels=1` as the only opt-in label mode.
  - applied provider atlas usage to reel chrome and lightning VFX, added a prewarmed `LightningArcFx`, and surfaced `requestedProvider`, `effectiveProvider`, `safePlaceholder`, and `missingKeys` on-screen for QA.
  - captured proof artifacts under `docs/_visual_proof/beta2b-2026-03-09/`, including openai/nanobanana/donorlocal idle states plus openai/nanobanana lightning frames and provider state JSON.
  - donorlocal is active in this local checkout because the ignored manifest under `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/runtime/manifest.json` now satisfies the provider contract.
