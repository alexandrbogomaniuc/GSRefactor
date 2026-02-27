# Template Integration Guide (Igaming Monorepo)

This guide explains how to use the canonical template and the scaffolding tool to create new slot games.

## 🛠 Using the Scaffolder

The `create-game` tool automates the process of setting up a new game project in the `/games` directory.

### Command
Run the following command from the root of the repository:

```bash
npm run create-game -- --name "My Awesome Slot" --id 5002 --slug awesome-slot
```
*(Note: Be sure to include the double dash `--` to pass arguments through to the underlying script.)*

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
- `gs` Platform Registration data.

### `/src/game/features/`
This is where game-specific feature modules reside. Modules are designed to be "pluggable" and can be enabled or disabled via configuration.

### Shared Logic (@gs/slot-shell)
Games do not copy core logic. Instead, they depend on `@gs/slot-shell`. This package provides:
- **Reels Framework**: `SlotMachine`, `Reel`, and `SlotSymbol` components.
- **UI Shell**: Settings menus, volume controls, and buttons.
- **Engine**: The core PixiJS v8 bootstrap and screen management.

---

## 🎨 Asset Management

### Raw Assets (AssetPack)
The template uses **AssetPack** for optimized asset delivery.
1. Place your source graphics in `games/[slug]/assets/`.
2. Run the build/dev command to compress and package them.
3. Update `manifest.json` if you add new spritesheets or fonts.

---

## 🧪 Development Workflow

```bash
# 1. Scaffold game
npm run create-game -- --name MyGame --id 1234 --slug my-game

# 2. Start development server
cd games/my-game
pnpm dev
```

## ✅ Best Practices
- **Do not modify `@gs/slot-shell`** unless the change applies to ALL games.
- **Isolate feature logic**: Keep specific mechanics inside a feature module.
- **Use the protocol package**: All network communication must pass through `@gs/protocol`.
- **Sync i18n**: Run `npm run i18n:check` regularly to ensure all translations are complete.

