# Non-Functional Requirements (NFR): Gamesv1 Template

## 1. Performance & Scale
- **Load Time:** The initial `index.html` loading screen must render within 2 seconds on a 3G mobile connection. Key asset bundles (CSS/JS) must be under 500KB gzipped.
- **FPS (Frames Per Second):** 
  - Desktop target: Locked at 60 FPS using PixiJS.
  - Mobile Low-Power Mode target: Maintained at 30 FPS to reduce battery consumption.
- **Latency Targets:**
  - `BET_REQUEST` to `BET_ACCEPTED` parsing loop must execute in under <10ms client-side overhead (ignoring network latency).
  - Animations must not stutter while awaiting network payload execution.

## 2. Infrastructure & Hosting
- **Stateless Architecture:** No internal player state should be persisted across page reloads outside of explicit URL context and session tokens derived from the GS orchestrator.
- **CDN Deployment:** Assets (images, sounds, scripts) must be hosted behind a CDN. Reference paths inside the application must use `assets/` relative pathing or a global `BASE_URL`.
- **Browser Compatibility:**
  - Modern WebKit (Safari iOS 14+).
  - Chromium engines (Chrome 88+, Edge).
  - Firefox 85+.

## 3. Resilience & Recoverability
- **Idempotency & Sequence:** The client must *never* reuse an `operationId`. A UUIDv4 must be generated per independent financial action. All WebSocket messages must include a monotonic `seq` to detect reordering and replays.
- **Session Sync:** The client must be able to resume a `RESERVED` spin state using a `SESSION_SYNC` payload containing the server-authoritative state blob.
- **Retry Strategy:** Network disconnects during an active spin must trigger a silent reconnect with exponential backoff (e.g., 200ms, 400ms, 800ms) up to a maximum of 5 attempts before surfacing a fatal "Network Disconnected" UI.

## 4. Security & Compliance
- **Subprotocol Targeting:** WebSocket connections must strictly enforce the `abs.gs.v1` subprotocol in headers.
- **Authentication:** `AuthToken` must be passed in the `GAME_READY` payload; WebSockets connection must immediately terminate if initialization fails.
- **No PII:** The client should never receive or log Personally Identifiable Information (PII) of the user. Only the abstract `sessionId` and generalized balances should be parsed.
- **TLS Only:** Data in transit must use `WSS/HTTPS` protocols; unencrypted `WS` is only permitted for local debugging hooks (`env=dev`).

## 5. Observability
- **Logging Strategy:** 
  - Standard `console.log` statements must be stripped in production builds.
  - Fatal errors (unrecoverable network, missing critical assets) must trigger a unique standard error code structure: `{ code, category, message, retryable }`.
- **Telemetry Injection:** All outbound WebSocket JSON envelopes *must* contain the exact canonical fields: `version`, `type`, `traceId`, `sessionId`, `bankId`, `gameId`, `operationId`, `timestamp`, `seq`, and `payload`.
