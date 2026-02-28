> [!WARNING]
> This document is archived and may be outdated.
> Do not use it as source of truth.
> Canonical entrypoint: `docs/MasterContext.md` and `docs/DOCS_MAP.md`.
# Project Architecture (Monorepo)

This repository is organized as a workspace monorepo to ensure clean separation between core protocol logic, shared tools, and individual game implementations.

## Directory Structure

### `/packages/`
Contains reusable core libraries shared across games and tools.
- `gs-protocol`: Pure stateless layer. Translates raw WebSocket frames into normalized Game Events.
- `gs-compliance`: Regulatory requirement implementations (timing, logging, Reality Checks).
- `gs-operator-bridge`: Inner-iframe PostMessage handshakes and operator integrations.
- `gs-core`: Shared game state machines, grid evaluators, and UX utilities.

### `/games/`
Standalone game projects using the core packages.
- `template-slot`: The canonical, production-grade PixiJS v8 / Vite template. **START HERE** for new games.
- `wincraft`: Implementation of the WinCraft slot game.

### `/tools/`
Development and testing infrastructure.
- `mock-gs`: WebSocket mock server for contract testing.
- `mock-slot-template`: Legacy mock server and math tuner UI.

### `/docs/`
System-wide documentation and standards.
- `/docs/protocol/`: `abs.gs.v1` and `ExtGame` JSON schemas.
- `/docs/compliance/`: Regulatory checklists and requirements.

### `/tests/`
Global test suites.
- `/tests/contract/`: Automated unit/contract tests for the protocol layer.

### `/.agent/`
Project-specific rules and context for AI coding assistants.

