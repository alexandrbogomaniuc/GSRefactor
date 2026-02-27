# Gamesv1 Monorepo

Universal, high-performance slot game development platform using PixiJS v8 and the `@gs` protocol stack.

## 🏗 Repository Structure

- **`games/`**: Slot implementations.
  - **`premium-slot-client/`**: 🌟 **The Canonical Template.** Use this as the base for all new slot games. Features PixiJS v8, `@pixi/ui` components, and advanced animations.
  - **`wincraft/`**: Reference implementation of the WinCraft game.
  - **`_archive/`**: Retired templates and legacy math/mock tools.
- **`packages/`**: Shared core libraries.
  - `@gs/protocol`: abs.gs.v1 WebSocket protocol layer.
  - `@gs/slot-shell`: Common UI components, reel framework, and engine bootstrap.
  - `@gs/config`: Tiered configuration system.
  - `@gs/i18n`: Multi-language support.
- **`tools/`**: Development utilities.
  - `create-game`: Scaffolding tool for new projects.
  - `config-gen`: GS registration file generator.
  - `i18n-check`: Translation validation tool.
- **`tests/`**: Global contract and unit tests.

---

## 🚀 Getting Started

### 1. Installation
Ensure you have `pnpm` installed globally.
```bash
pnpm install
```

### 2. Scaffold a New Game
```bash
npm run create-game -- --name "My New Game" --id 5001 --slug my-new-game
```

### 3. Local Development
```bash
cd games/my-new-game
pnpm dev
```

### 4. Production Build
```bash
cd games/my-new-game
pnpm build
```

---

## 🧪 Testing

### Contract Tests (abs.gs.v1)
Validates that the client-server communication follows the strict financial protocol.
```bash
npm run test:contract
```

### i18n Validation
Checks for missing translations across all games.
```bash
npm run i18n:check
```

---

## 📐 Standards & Guidelines

- **Architecture**: Always prefer shared logic in `@gs/slot-shell` over copying code.
- **Art**: Place high-res assets in `raw-assets/` for automated optimization via AssetPack.
- **Compliance**: Adhere to the checklists in `docs/compliance/`.
- **Source of Truth**: Refer to [.agent/context.md](.agent/context.md) for current project status.
