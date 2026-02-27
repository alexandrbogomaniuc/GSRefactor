# Template Integration Guide (Igaming Monorepo)

This guide explains how to use the canonical template and the scaffolding tool to create new slot games.

## 🛠 Using the Scaffolder

The `create-game` tool automates the process of setting up a new game project in the `/games` directory.

### Command
Run the following command from the root of the repository:

```bash
pnpm create-game --name "My Awesome Slot" --id 5002 --slug awesome-slot
```

### Options
- `--name`: The display name of the game.
- `--id`: The unique Game ID for the back-end.
- `--slug`: The directory name and package slug.

---

## 🏗 Game Architecture

Each game created via the scaffolder follows a clean separation of concerns.

### `game.settings.json`
The central configuration file for the game. Defines:
- Game ID & Name
- Reel layout (Rows/Cols)
- Enabled feature modules (Free Spins, Buy Feature, etc.)

### `/src/game/features/`
This is where game-specific feature modules reside. Modules are designed to be "pluggable" and can be enabled or disabled via configuration.

### Shared Logic (@gs/slot-shell)
Games do not copy core logic. Instead, they depend on `@gs/slot-shell`. This package provides:
- **Reels Framework**: `SlotMachine`, `Reel`, and `Symbol` components.
- **UI Shell**: Settings menus, volume controls, and buttons.
- **Engine**: The core PixiJS v8 bootstrap and screen management.

---

## 🎨 Asset Management

### Placeholder Atlas
The template uses a generic asset atlas for symbols. To brand your game:
1. Replace the assets in `games/[slug]/assets/`.
2. Update the `manifest.json` to point to your new graphics.

---

## 🧪 Development Workflow

```bash
# 1. Scaffold game
pnpm create-game --name MyGame --id 1234 --slug my-game

# 2. Install shared dependencies
pnpm install

# 3. Start development server
cd games/my-game
pnpm dev
```

## ✅ Best Practices
- **Do not modify `@gs/slot-shell`** unless the change applies to ALL games.
- **Isolate feature logic**: Keep specific mechanics (like "Expanding Wilds") inside a feature module.
- **Use the protocol package**: All network communication must pass through `@gs/protocol`.
