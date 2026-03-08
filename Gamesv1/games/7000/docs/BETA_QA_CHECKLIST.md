# Game 7000 QA Beta 1 Checklist

Base branch for this beta: `origin/eng/7000-crazy-rooster-20260307-1346`

QA beta branch intent:

- provider packs assembled under `assets/providers/{openai,nanobanana}`,
- query override supported via `?assetProvider=openai|nanobanana`,
- shared BetOnline typographic wordmark used by the WOW preloader,
- demo fallback runnable with `?allowDevFallback=1`,
- incomplete provider packs should log a clear validation error and continue with safe placeholder rendering.

## Core Smoke

- [ ] Open `http://127.0.0.1:8080/?allowDevFallback=1`
- [ ] Confirm the preloader completes
- [ ] Confirm the main HUD renders idle
- [ ] Click `SPIN` once and confirm reels complete and status updates
- [ ] Use bet `-` and `+` and confirm ladder changes
- [ ] Hold `SPIN` for turbo and confirm the HUD/status reflects turbo behavior
- [ ] Start and stop autoplay
- [ ] Open buy bonus and confirm the stub/demo result resolves without a crash

## Provider QA

- [ ] Open `http://127.0.0.1:8080/?allowDevFallback=1&assetProvider=openai`
- [ ] Confirm OpenAI backgrounds/provider skin load without provider validation errors
- [ ] Open `http://127.0.0.1:8080/?allowDevFallback=1&assetProvider=nanobanana`
- [ ] Confirm NanoBanana selection does not crash the game
- [ ] Confirm NanoBanana logs the expected missing-pack warning and falls back safely for unavailable art

## Deterministic Feature Moments

Use `proofState` so QA does not wait on RNG:

- collect: `http://127.0.0.1:8080/?allowDevFallback=1&proofState=collect`
- lightning / boost: `http://127.0.0.1:8080/?allowDevFallback=1&proofState=boost`
- bonus: `http://127.0.0.1:8080/?allowDevFallback=1&proofState=bonus`
- big win: `http://127.0.0.1:8080/?allowDevFallback=1&proofState=big`
- mega win: `http://127.0.0.1:8080/?allowDevFallback=1&proofState=mega`
- buy tiers: `buy75`, `buy200`, `buy300`

## Expected Debug Hooks

Browser console / automation helpers:

- `window.__game7000.spin()`
- `window.__game7000.buy()`
- `window.__game7000.autoplay()`
- `window.__game7000.turbo()`
- `window.__game7000ProviderPack`
