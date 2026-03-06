# Preloader WOW Proof

## Reproduction

- Brand A: `?brand=A&preloaderHoldMs=5000`
- Brand B: `?brand=B&preloaderHoldMs=5000`
- Minimal motion: `?brand=A&motion=minimal&preloaderHoldMs=5000`

## Proof-Only Delay

- `preloaderHoldMs` is proof-only.
- It artificially keeps the preloader visible long enough for screenshots and GIF capture.
- It does not change GS bootstrap/runtime behavior, loading order, or release behavior.

## Canonical Source Files

- Token resolver: `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`
- Brand presets: `games/premium-slot/src/app/theme/brandKit.ts`
