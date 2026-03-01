# ORCHESTRATOR_OUTPUTS

Generated: 2026-03-01 15:37:57 +00:00

## Command: corepack pnpm run build

```text

> gamesv1-monorepo@1.0.0 build E:\Dev\GSRefactor\Gamesv1
> corepack pnpm --filter @games/premium-slot build


> @games/premium-slot@0.0.0 build E:\Dev\GSRefactor\Gamesv1\games\premium-slot
> tsc
```

## Command: corepack pnpm run test

```text

> gamesv1-monorepo@1.0.0 test E:\Dev\GSRefactor\Gamesv1
> corepack pnpm run test:config && corepack pnpm run test:animation-policy && corepack pnpm run test:contract


> gamesv1-monorepo@1.0.0 test:config E:\Dev\GSRefactor\Gamesv1
> node --experimental-strip-types tests/compliance/config.test.ts

corepack : (node:55292) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/tests/compliance/config.test.ts is not specified and it doesn't parse as CommonJS.
At line:5 char:15
+ $testOutput = corepack pnpm run test 2>&1 | Out-String
+               ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : NotSpecified: ((node:55292) [M...se as CommonJS.:String) [], RemoteException
    + FullyQualifiedErrorId : NativeCommandError
 
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\package.json.
(Use `node --trace-warnings ...` to show where the warning was created)
(node:55292) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/packages/core-compliance/src/config/ConfigResolver.ts is not specified and it 
doesn't parse as CommonJS.
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\packages\core-compliance\package.json.
PASS applies precedence template < bank < game < currency < launch
PASS falls back to template bet ladder when currency override is missing
PASS logs diff entries per overriding layer
PASS applies legacy GL_DEFAULT_BET fallback when launch defaultBet is absent
PASS surfaces unsupported config keys in warnings
PASS throws on invalid constraints
PASS throws when maxBet exceeds maxExposure
PASS throws when history URL uses javascript scheme

ConfigResolver tests: 8 passed, 0 failed.

> gamesv1-monorepo@1.0.0 test:animation-policy E:\Dev\GSRefactor\Gamesv1
> node --experimental-strip-types tests/compliance/animation-policy.test.ts

(node:18296) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/tests/compliance/animation-policy.test.ts is not specified and it doesn't parse as 
CommonJS.
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\package.json.
(Use `node --trace-warnings ...` to show where the warning was created)
(node:18296) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/packages/core-compliance/src/animation/AnimationPolicy.ts is not specified and it 
doesn't parse as CommonJS.
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\packages\core-compliance\package.json.
PASS resolves normal spin timing without turbo
PASS resolves turbo timing with speed profile
PASS classifies win tiers by multiplier thresholds
PASS forced skip yields zero presentation duration
PASS low performance mode disables heavy win fx
PASS autoplay delay respects timing contract

AnimationPolicy tests: 6 passed, 0 failed.

> gamesv1-monorepo@1.0.0 test:contract E:\Dev\GSRefactor\Gamesv1
> node --experimental-strip-types tests/contract/browser-runtime.contract.test.ts

(node:20220) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/tests/contract/browser-runtime.contract.test.ts is not specified and it doesn't 
parse as CommonJS.
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\package.json.
(Use `node --trace-warnings ...` to show where the warning was created)
(node:20220) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/packages/core-protocol/src/http/GsHttpRuntimeTransport.ts is not specified and it 
doesn't parse as CommonJS.
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\packages\core-protocol\package.json.
PASS bootstrap returns runtime/session/wallet truth
PASS opengame updates sequencing snapshot
PASS playround consumes requestCounter/idempotency and returns payload
PASS idempotency duplicate returns same round result
PASS featureaction updates requestCounter/currentStateVersion
PASS resumegame returns restore payload
PASS gethistory returns entries and sequencing
PASS closegame succeeds

Browser runtime contract tests: 8 passed, 0 failed.
```

## Repo Tree (simple, filtered)

```text
.agent\context.md
.agent\rules\00_project_quality.md
.agent\rules\01_rules_protocol.md
.agent\rules\10_stack_versions.md
.agent\rules\20_asset_pipeline.md
.agent\rules\30_vfx_animation.md
.agent\rules\40_mobile_perf_debug.md
.agent\rules\agent_rules.md
.agent\skills\assetpack-compression-engineer\SKILL.md
.agent\skills\performance-auditor\SKILL.md
.agent\skills\pixi-v8-api-guardian\SKILL.md
.agent\skills\premium-slot-architect\SKILL.md
.agent\skills\spine-pipeline\SKILL.md
.agent\skills\vfx-wow-recipes\SKILL.md
.agent\workflows\add_assetpack.md
.agent\workflows\add_spine.md
.agent\workflows\kickoff.md
.agent\workflows\new_game.md
.agent\workflows\perf_audit.md
.agent\workflows\release_game.md
.agent\workflows\scaffold_pixi.md
.agent\workflows\vertical_slice_slots.md
.gitignore
AGENTS.md
context.md
docs\_archive\ActivityLog.md
docs\_archive\ARCHITECTURE_TREE.md
docs\_archive\ASSET_PIPELINE.md
docs\_archive\GSDatabaseRegistrationGuide.md
docs\_archive\Independent_Development_Guide.md
docs\_archive\MasterContext.md
docs\_archive\Milestones.md
docs\_archive\NFR.md
docs\_archive\PERF_BUDGET.md
docs\_archive\PixiJS_Initialization_Guide.md
docs\_archive\PRD.md
docs\_archive\PROJECT.md
docs\_archive\SprintPlan.md
docs\_archive\TemplateIntegrationGuide.md
docs\_archive\VFX_LIBRARY.md
docs\_archive\WowFxRecommendations.md
docs\ART_AND_PROMO_PIPELINE.md
docs\ASSET_MANIFEST_SPEC.md
docs\CAPABILITY_MATRIX.md
docs\compliance\client-requirements-checklist.md
docs\compliance\config-resolution.md
docs\CONFIG_SYSTEM.md
docs\CURRENT_STATE_AUDIT.md
docs\DOCS_MAP.md
docs\game\round-lifecycle.md
docs\GAME_CLIENT_REQUIREMENTS_MAIN.md
docs\generated\ORCHESTRATOR_OUTPUTS.md
docs\generated\README.md
docs\gs\bootstrap-config-contract.md
docs\gs\browser-error-codes.md
docs\gs\browser-runtime-api-contract.md
docs\gs\browser-runtime-sequence-diagrams.md
docs\gs\enable-disable-canary-rollback.md
docs\gs\release-registration-contract.md
docs\GS_REGISTRATION_ARTIFACTS.md
docs\LOCALIZATION.md
docs\MasterContext.md
docs\PROJECT.md
docs\protocol\abs-gs-v1.md
docs\protocol\browser-runtime-api-contract.md
docs\protocol\gs-http-runtime.md
docs\protocol\spin-profiling.md
docs\qa\AAA_quality_gate.md
docs\qa\bug-template.md
docs\refs\pixijs\llms.txt
docs\refs\pixijs\llms-full.txt
docs\refs\pixijs\llms-medium.txt
docs\refs\pixijs\v8-known-issues.md
docs\RELEASE_ARTIFACTS.md
docs\RELEASE_PROCESS.md
games\_archive\slot-template\.gitignore
games\_archive\slot-template\config-ui.html
games\_archive\slot-template\index.html
games\_archive\slot-template\master-game-config.json
games\_archive\slot-template\mock-server.js
games\_archive\slot-template\package.json
games\_archive\slot-template\tsconfig.json
games\_archive\wincraft\ActivityLog.md
games\_archive\wincraft\context.md
games\_archive\wincraft\vault-memory.md
games\premium-slot\.assetpack\619b30c15febfabaf6b3344709986d3907776746.json
games\premium-slot\.assetpack\924bc44baee610e8c40a94ad002f9b62cecc0b0d.json
games\premium-slot\.gitignore
games\premium-slot\docs\asset-manifest.sample.json
games\premium-slot\docs\VERTICAL_SLICE_PLAN.md
games\premium-slot\eslint.config.mjs
games\premium-slot\game.settings.json
games\premium-slot\gs\registration.md
games\premium-slot\gs\template-params.json
games\premium-slot\gs\template-params.properties
games\premium-slot\index.html
games\premium-slot\package.json
games\premium-slot\public\favicon.png
games\premium-slot\public\style.css
games\premium-slot\scripts\assetpack-vite-plugin.ts
games\premium-slot\src\main.ts
games\premium-slot\src\manifest.json
games\premium-slot\src\pixi-mixins.d.ts
games\premium-slot\src\vite-env.d.ts
games\premium-slot\tsconfig.json
games\premium-slot\vite.config.ts
package.json
packages\core-compliance\package.json
packages\core-compliance\src\CapabilityMatrix.ts
packages\core-compliance\src\ComplianceConfig.ts
packages\core-compliance\src\ConfigResolver.ts
packages\core-compliance\src\FeatureFlags.ts
packages\core-compliance\src\index.ts
packages\core-compliance\src\ResolvedRuntimeConfig.ts
packages\core-compliance\src\RuntimeConfig.ts
packages\core-protocol\package.json
packages\core-protocol\src\IGameTransport.ts
packages\core-protocol\src\index.ts
packages\core-protocol\src\schemas.ts
packages\core-protocol\src\SpinProfiling.ts
packages\i18n\package.json
packages\i18n\src\index.ts
packages\operator-pariplay\package.json
packages\operator-pariplay\src\index.ts
packages\operator-pariplay\src\PariplayBridge.ts
packages\pixi-engine\package.json
packages\pixi-engine\src\engine.ts
packages\pixi-engine\src\index.ts
packages\pixi-engine\src\pixi-mixins.d.ts
packages\pixi-engine\src\postmessage-simulator.html
packages\pixi-engine\src\runtimeEngine.ts
packages\pixi-layout\package.json
packages\pixi-layout\src\index.ts
packages\ui-kit\package.json
packages\ui-kit\src\assets.ts
packages\ui-kit\src\index.ts
packages\ui-kit\tsconfig.json
pnpm-lock.yaml
pnpm-workspace.yaml
progress.md
README.md
tests\_archive\contract\transport.contract.test.ts
tests\_archive\operator\pariplay.test.ts
tests\_archive\README.md
tests\compliance\animation-policy.test.ts
tests\compliance\config.test.ts
tests\contract\browser-runtime.contract.test.ts
tests\game\round-state.test.ts
tests\layout\layout-matrix.test.ts
tests\net\spin-profiling.test.ts
tests\tsconfig.json
tools\check-boundaries\index.mjs
tools\check-boundaries\src\index.ts
tools\config-gen\package.json
tools\config-gen\src\index.ts
tools\config-gen\tsconfig.json
tools\create-game.ts
tools\i18n-check\package.json
tools\i18n-check\src\index.ts
tools\i18n-check\tsconfig.json
tools\mcp\gs-protocol-mcp\package.json
tools\mcp\gs-protocol-mcp\tsconfig.json
tools\release-pack\create-release.ts
tsconfig.json
```

Note: tree is intentionally filtered/excerpted to keep this report reviewable (excludes node_modules/dist/build/.cache and limits to first 250 paths).
