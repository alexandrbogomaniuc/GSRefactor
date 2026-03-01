# Orchestrator Helpful Outputs

Generated: 2026-03-01 08:52:56+00:00

## pnpm build output
```text

> gamesv1-monorepo@1.0.0 build E:\Dev\GSRefactor\Gamesv1
> corepack pnpm --filter @games/premium-slot build


> @games/premium-slot@0.0.0 build E:\Dev\GSRefactor\Gamesv1\games\premium-slot
> tsc


```

## pnpm test output
```text
 ERR_PNPM_NO_SCRIPT  Missing script: test

Command "test" not found.

```

## canonical test scripts output

### pnpm run test:config
```text

> gamesv1-monorepo@1.0.0 test:config E:\Dev\GSRefactor\Gamesv1
> node --experimental-strip-types tests/compliance/config.test.ts

corepack.cmd : (node:41444) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/tests/compliance/config.test.ts is not specified and it doesn't parse as CommonJS.
At line:7 char:15
+ $configOut = (& corepack pnpm run test:config 2>&1 | Out-String)
+               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : NotSpecified: ((node:41444) [M...se as CommonJS.:String) [], RemoteException
    + FullyQualifiedErrorId : NativeCommandError
 
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\package.json.
(Use `node --trace-warnings ...` to show where the warning was created)
(node:41444) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
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

```

### pnpm run test:animation-policy
```text

> gamesv1-monorepo@1.0.0 test:animation-policy E:\Dev\GSRefactor\Gamesv1
> node --experimental-strip-types tests/compliance/animation-policy.test.ts

corepack.cmd : (node:51308) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/tests/compliance/animation-policy.test.ts is not specified and it doesn't parse as 
CommonJS.
At line:8 char:18
+ ... nimationOut = (& corepack pnpm run test:animation-policy 2>&1 | Out-S ...
+                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : NotSpecified: ((node:51308) [M...se as CommonJS.:String) [], RemoteException
    + FullyQualifiedErrorId : NativeCommandError
 
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\package.json.
(Use `node --trace-warnings ...` to show where the warning was created)
(node:51308) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
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

```

### pnpm run test:contract
```text

> gamesv1-monorepo@1.0.0 test:contract E:\Dev\GSRefactor\Gamesv1
> node --experimental-strip-types tests/contract/mock-gs/ScenarioRunner.ts

corepack.cmd : (node:17120) [MODULE_TYPELESS_PACKAGE_JSON] Warning: Module type of 
file:///E:/Dev/GSRefactor/Gamesv1/tests/contract/mock-gs/ScenarioRunner.ts is not specified and it doesn't parse as 
CommonJS.
At line:9 char:17
+ $contractOut = (& corepack pnpm run test:contract 2>&1 | Out-String)
+                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : NotSpecified: ((node:17120) [M...se as CommonJS.:String) [], RemoteException
    + FullyQualifiedErrorId : NativeCommandError
 
Reparsing as ES module because module syntax was detected. This incurs a performance overhead.
To eliminate this warning, add "type": "module" to E:\Dev\GSRefactor\Gamesv1\package.json.
(Use `node --trace-warnings ...` to show where the warning was created)
🤖 [GS Mock Server] Listening on ws://localhost:6001

Running Scenario: Buy Feature
  Action: connect
🔌 Client connected to Mock GS
  Action: spin
OK Scenario Buy Feature completed.

Running Scenario: Delayed Wallet Message
  Action: connect
🔌 Client disconnected
🔌 Client connected to Mock GS
  Action: spin
OK Scenario Delayed Wallet Message completed.

Running Scenario: Duplicate operationId Retry
  Action: connect
🔌 Client disconnected
🔌 Client connected to Mock GS
  Action: spin
  Action: spin
♻️  [Idempotency] Resending cached result for opId: op-dup-test
OK Scenario Duplicate operationId Retry completed.

Running Scenario: Free Spins Entry
  Action: connect
🔌 Client disconnected
🔌 Client connected to Mock GS
  Action: spin
OK Scenario Free Spins Entry completed.

Running Scenario: Insufficient Funds
  Action: connect
🔌 Client disconnected
🔌 Client connected to Mock GS
  Action: spin
OK Scenario Insufficient Funds completed.

Running Scenario: Normal Spin Win
  Action: connect
🔌 Client disconnected
🔌 Client connected to Mock GS
  Action: spin
  Action: settle
OK Scenario Normal Spin Win completed.

Running Scenario: Reconnect mid-spin
  Action: connect
🔌 Client disconnected
🔌 Client connected to Mock GS
  Action: spin
  Action: disconnect
  Action: connect
🔌 Client disconnected
🔌 Client connected to Mock GS
  Action: sync
OK Scenario Reconnect mid-spin completed.

All Scenarios passed.
🔌 Client disconnected

```

## Simple repo tree from root
```text
Gamesv1/
├─ .agent/
│  ├─ context.md
│  ├─ rules/
│  ├─ skills/
│  ├─ workflows/
├─ .gitignore
├─ AGENTS.md
├─ context.md
├─ docs/
│  ├─ _archive/
│  ├─ ART_AND_PROMO_PIPELINE.md
│  ├─ ASSET_MANIFEST_SPEC.md
│  ├─ CAPABILITY_MATRIX.md
│  ├─ compliance/
│  ├─ CONFIG_SYSTEM.md
│  ├─ CURRENT_STATE_AUDIT.md
│  ├─ DOCS_MAP.md
│  ├─ game/
│  ├─ GAME_CLIENT_REQUIREMENTS_MAIN.md
│  ├─ GS_REGISTRATION_ARTIFACTS.md
│  ├─ LOCALIZATION.md
│  ├─ MasterContext.md
│  ├─ ORCHESTRATOR_OUTPUTS.md
│  ├─ PROJECT.md
│  ├─ protocol/
│  ├─ qa/
│  ├─ refs/
│  ├─ RELEASE_ARTIFACTS.md
│  ├─ RELEASE_PROCESS.md
├─ games/
│  ├─ _archive/
│  ├─ premium-slot/
├─ node_modules/
│  ├─ .bin/
│  ├─ .modules.yaml
│  ├─ .pnpm/
│  ├─ .pnpm-workspace-state-v1.json
│  ├─ @esbuild/
│  ├─ @types/
│  ├─ tsx/
│  ├─ typescript/
│  ├─ uuid/
│  ├─ ws/
│  ├─ zod/
├─ package.json
├─ packages/
│  ├─ core-compliance/
│  ├─ core-protocol/
│  ├─ i18n/
│  ├─ operator-pariplay/
│  ├─ pixi-engine/
│  ├─ pixi-layout/
│  ├─ ui-kit/
├─ pnpm-lock.yaml
├─ pnpm-workspace.yaml
├─ progress.md
├─ README.md
├─ tests/
│  ├─ compliance/
│  ├─ contract/
│  ├─ game/
│  ├─ layout/
│  ├─ net/
│  ├─ operator/
│  ├─ tsconfig.json
├─ tools/
│  ├─ check-boundaries/
│  ├─ config-gen/
│  ├─ create-game/
│  ├─ create-game.ts
│  ├─ i18n-check/
│  ├─ mcp/
│  ├─ release-pack/
├─ tsconfig.json
```
