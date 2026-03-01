# GAME_CLIENT_REQUIREMENTS_MAIN

Canonical client capability and behavior requirements.

## A. Runtime Contract

1. Client uses GS HTTP runtime flow as canonical transport path.
2. Client treats GS responses as source of truth for wallet/session/state.
3. Client never invents or mutates authoritative financial state locally.
4. Retries must preserve GS idempotency keys and sequencing metadata.
5. Browser transport scope is `browser -> GS` only.
6. Internal slot-engine/RNG/audit paths are server-side/private and out of browser state ownership.
7. Client runtime transport must support: `bootstrap/openGame`, `playRound`, `featureAction`, `resumeGame`, `closeGame`, and browser-facing history access.

## B. Financial/State Safety

1. Monetary requests carry stable idempotency keys.
2. Request ordering follows GS requestCounter/session rules.
3. Recovery path must resume from GS-provided restore data.
4. Client must tolerate retries/timeouts without duplicate settlement.

## C. UX and Compliance

1. Enforce configured min spin timing.
2. Enforce configured turbo/autoplay capability flags.
3. Apply currency formatting/truncation from resolved runtime config.
4. Keep animation behavior compliant with timing contracts.

## D. Asset and Runtime Packaging

1. Assets are loaded from CDN/static origin.
2. Runtime uses manifest aliases/bundles only.
3. No raw asset path strings in gameplay logic.

## E. Release Readiness

1. Build output is versioned and reproducible.
2. Release artifact set includes manifest + GS registration artifacts.
3. Guest/free/real launch URLs validate runtime boot and round flow.

## F. Explicit Non-Goals

1. Operator-specific iframe messaging is not required by canonical path.
2. Multiplayer support is not included.
