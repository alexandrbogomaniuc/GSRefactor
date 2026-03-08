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
