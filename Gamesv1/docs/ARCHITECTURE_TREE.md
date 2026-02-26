## Production-Grade Slot Game Structure

When building a new slot game using this repository template, code MUST be partitioned into the following exact directories. This prevents "Spaghetti Game Code" where networking logic leaks directly into visual UI elements.

### `/docs/`
- `/docs/protocol/` - Markdown files verifying the server's `abs.gs.v1` and `ExtGame` JSON schemas.
- `/docs/compliance/` - Checklists ensuring all regulatory requirements (timings, turboplay, logging) are implemented.
- `/docs/operator-integrations/` - Guides detailing inner-iframe PostMessage handshakes for custom casinos.

### `/src/`
- `/src/net/` - Pure stateless layer. Translates raw WebSocket frames / HTTP Fetch calls into normalized Game Events. (No PixiJS code goes here).
- `/src/compliance/` - Mandatory timing enforcement modules, profile loggers, and Reality Check modals.
- `/src/game/` - The definitive State Machine and Core Logic loop. Holds grid definitions, win evaluators, and balance projections.
- `/src/ux/` - The View Layer. All visual PixiJS containers, Spine animations, Particles, and interaction handlers.
- `/src/debug/` - Development-only scripts: FPS Overlays, Cheat Panels (forcing specific wins), and VRAM profilers.

### `/tests/`
- `/tests/contract/` - Automated tests that hit the MCP Server verifying that `/src/net/` correctly understands the JSON payloads sent by the C#/Go Backend.
- `/tests/e2e/` - Headless Playwright tests ensuring an entire 100-spin session evaluates without unhandled exceptions or memory leaks.

