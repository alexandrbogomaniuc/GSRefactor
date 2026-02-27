# PROJECT: GS Gamesv1 Universal Slot Template

Welcome to the GS Monorepo. This project provides a high-quality baseline for creating compliance-ready slot games.

## 🔗 Core Navigation
- **[Master Context & Source of Truth](file:///.agent/context.md)**: The canonical project state (Vision, Status, Tech Stack).
- **[Template Integration Guide](file:///docs/TemplateIntegrationGuide.md)**: How to scaffold and build new games.
- **[Configuration System](file:///docs/CONFIG_SYSTEM.md)**: Deep dive into design-time and runtime config layers.
- **[Localization System](file:///docs/LOCALIZATION.md)**: Rules for multi-language support.

## 🏗 Repository Structure
- `games/`: Ready-to-run slot implementations (e.g., `template-slot`).
- `packages/`: Shared logic (Protocol, Shell, Config, i18n).
- `tools/`: Development utilities (Scaffolder, Config Generator, Contract Mock).
- `docs/`: Technical guides and architecture deep-dives.
- `docs/archive/`: Legacy research and early project milestones.

## ✅ Docs Sanity Checklist
Before claiming a documentation task is complete, ensure:
- [ ] Every referenced file path exists in the repository.
- [ ] Every command is runnable from the project root.
- [ ] No secrets or passwords appear (use `[REDACTED]` or `{{SECRET_NAME}}`).
- [ ] Links between documents use absolute or relative-to-root paths correctly.

---
> [!NOTE]
> This repository uses **pnpm workspaces**. Ensure you run `pnpm install` from the root to link all local packages correctly.
