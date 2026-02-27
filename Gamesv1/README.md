# Gamesv1 - Igaming Monorepo

A high-performance, compliance-ready monorepo for HTML5 slot games using PixiJS v8 and the `abs.gs.v1` protocol.

## 🚀 Getting Started

This project uses `pnpm` workspaces for dependency management.

```bash
# Install dependencies (from root)
pnpm install

# Run the canonical template in dev mode
cd games/template-slot
npm run dev
```

## 📂 Repository Structure

- **`/games`**: Game implementations. See [template-slot](games/template-slot) for a starting point.
- **`/packages`**: Core libraries (@gs/protocol, @gs/compliance).
- **`/tools`**: Development tools including mock servers and config UIs.
- **`/docs`**: Architecture and compliance documentation.

For a detailed view of the architecture, see [docs/ARCHITECTURE_TREE.md](docs/ARCHITECTURE_TREE.md).

## 🛠 Tech Stack

- **Graphics**: PixiJS v8
- **Build Tool**: Vite
- **Language**: TypeScript
- **Protocol**: WebSocket (`abs.gs.v1`)
- **Package Manager**: pnpm

## ⚖️ License
Internal Use Only.
