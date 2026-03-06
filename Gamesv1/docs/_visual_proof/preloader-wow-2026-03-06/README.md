# Preloader WOW Proof

## Reproduction Queries

- Brand A proof: `?brand=A&preloaderHoldMs=5000`
- Brand B proof: `?brand=B&preloaderHoldMs=5000`
- Force minimal motion on the WOW preloader: `?brand=A&motion=minimal&preloaderHoldMs=5000`

## Why `preloaderHoldMs` Is Proof-Only

- `preloaderHoldMs` artificially delays preloader dismissal so screenshots and GIF capture can complete.
- It does not change GS bootstrap/runtime behavior, asset loading order, or release artifacts.
- Production flows should not rely on it; it exists only to make visual audit capture deterministic.
