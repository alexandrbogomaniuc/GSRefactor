# BUILD KICKOFF PROMPT

Use this prompt to start implementation of a new original slot game in Gamesv1.

## REQUIRED INPUT BLOCK
- `[NEW_GAME_ID]`:
- `[NEW_GAME_NAME]`:
- `[NEW_THEME]`:
- `[ASSET_STATUS]`: (concept / WIP / production-ready)
- `[TARGET_VOLATILITY]`: (low / medium / high)
- `[TARGET_RTP_POLICY]`: (describe target policy constraints)
- `[FEATURE_SET]`: (collect/boost/bonus/buy-bonus variants)
- `[BUY_BONUS_TIERS]`: (bet multipliers and labels)
- `[SYMBOL_SET]`: (original symbol taxonomy)
- `[LOCALES]`: (must include `en` + at least 2 more)
- `[MOBILE_PRIORITY]`: (default yes)
- `[AUDIO_DIRECTION]`:
- `[PROMO_REQUIRED]`: (yes/no)

## PROMPT BODY
Build a new original game module in `Gamesv1/games/[NEW_GAME_ID]/` using the canonical Gamesv1 architecture.

Hard constraints:
1. No GS contract changes.
2. Browser is presentation-only for wallet/session truth.
3. Use shell token system and shared HUD architecture.
4. Follow existing repo architecture (`games/premium-slot` + `packages/ui-kit` patterns).
5. Use `@gamesv1/core-protocol` for runtime calls.
6. Do not introduce canonical direct WebSocket runtime path.

Implementation requirements:
1. Scaffold game package from canonical structure (settings/theme/locales/math/gs/src/public/raw-assets/docs).
2. Implement runtime mapper for presentation payload only.
3. Implement `RoundActionBuilder` and `BetSelectionBuilder` as explicit app-layer builders.
4. Wire builders into spin/autoplay/buy-feature control handling.
5. Keep feature definitions config-driven (not hardcoded in rendering code).
6. Reuse `PremiumTemplateHud` and capability-driven visibility.
7. Add test coverage for:
   - bet selection boundaries
   - round action payload formation
   - buy bonus dispatch
   - autoplay stop/resume behavior
   - restore/resume flow compliance

Evidence-informed mechanic baseline to preserve (without copying art/style):
- Collect + Boost + Bonus + Buy Bonus layered model.
- Buy bonus three-tier model.
- Jackpot classes and bonus-mode reset concept.

Deliverables:
1. Working game module compiling in workspace.
2. Contract-safe runtime integration.
3. Updated docs for architecture and release handoff.
4. Smoke-test checklist and known gaps list.
