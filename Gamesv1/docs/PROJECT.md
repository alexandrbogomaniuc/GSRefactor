# PROJECT

High-level architecture charter for Gamesv1.

## 1. Mission

Build and maintain the reference client shell and packaging stack for GS slot games.

Outcome target:
- New slots ship faster with consistent runtime behavior and release quality.
- Financial and state correctness stays server-owned by GS.

## 2. Architecture Alignment

### Runtime ownership
- GS owns session, wallet, DB state, restore path, requestCounter, and idempotency.
- Browser client renders and orchestrates UX only.

### Transport
- Primary: GS HTTP runtime flow (`Enter` + `processTransaction` style contract).
- Legacy: abs.gs.v1 WebSocket only for experiments/backward compatibility.

### Assets
- Client assets are served from CDN/static origin.
- Client code uses manifest aliases/bundles, not hardcoded paths.

### Release packaging
- Every release is versioned and emits GS registration artifacts and deployment metadata.

## 3. Scope

In scope:
- single-player slot client shell
- runtime compliance behaviors
- release packaging and registration artifacts

Out of scope:
- Pariplay/operator-specific messaging integration
- multiplayer

## 4. Core Layers

1. Protocol layer (`packages/core-protocol`): normalized transport/events, HTTP runtime contract.
2. Compliance layer (`packages/core-compliance`): resolved config, limits, timing policy.
3. Engine layer (`packages/pixi-engine`): rendering loop, layout, asset bootstrap.
4. UI layer (`packages/ui-kit`): reusable controls/HUD/slot visuals.
5. Game layer (`games/premium-slot`): feature composition and content.

## 5. Non-Negotiable Constraints

1. Client does not authoritatively mutate wallet or DB state.
2. Game code does not open raw WebSocket or direct HTTP financial calls outside protocol package.
3. Runtime path must be compatible with GS HTTP handshake and recovery behavior.
4. Release outputs must be versioned and traceable to git SHA.
