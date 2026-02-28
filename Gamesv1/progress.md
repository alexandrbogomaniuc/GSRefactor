Original prompt: TASK: Implement responsive layout using @pixi/layout in packages/pixi-engine and ui-kit.

- Verified existing responsive layout implementation in pixi-engine/ui-kit and premium-slot integration.
- Next: run build + layout matrix tests and fix any issues.
- Build check: corepack pnpm run build (pass).
- Layout matrix: 6/6 scenarios passing via workspace execution.
- Added script: npm run test:layout.
- Fixed compile blocker in RuntimeConfigSchema: z.record now uses explicit key schema.
- Added docs/ART_AND_PROMO_PIPELINE.md with required deliverables, export specs, Spine/AE pipeline, quality gates, and dual checklists.
- Added docs/ASSET_MANIFEST_SPEC.md defining per-game manifest schema and validation rules.
- Added games/premium-slot/docs/asset-manifest.sample.json placeholder sample aligned to spec.
- Validated sample manifest JSON parses correctly.
- Docs dedup pass: created docs/MasterContext.md as canonical context and docs/DOCS_MAP.md as index.
- Moved outdated docs to docs/_archive and prepended warning headers.
- Updated .agent/context.md, .agent/workflows/kickoff.md, and .agent/rules to remove stale references.
