# Orchestrator Helpful Outputs

Generated: 2026-02-28 21:05:31+00:00

## pnpm build output
```text

> gamesv1-monorepo@1.0.0 build E:\Dev\GSRefactor\Gamesv1
> corepack pnpm --filter @games/premium-slot build


> @games/premium-slot@0.0.0 build E:\Dev\GSRefactor\Gamesv1\games\premium-slot
> tsc
```

## pnpm test output
```text
corepack.cmd : 'test' is not recognized as an internal or external command,
At line:6 char:13
+ $testOut = (& corepack pnpm test 2>&1 | Out-String)
+             ~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : NotSpecified: ('test' is not r...ternal command,:String) [], RemoteException
    + FullyQualifiedErrorId : NativeCommandError
 
operable program or batch file.
undefined
 ERR_PNPM_RECURSIVE_EXEC_FIRST_FAIL  Command "test" not found

Did you mean "pnpm tsc"?
```

## Simple repo tree from root
```text
Gamesv1/
├─ .agent/
│  ├─ rules/
│  ├─ skills/
│  ├─ workflows/
│  └─ context.md
├─ docs/
│  ├─ _archive/
│  ├─ compliance/
│  ├─ game/
│  ├─ protocol/
│  ├─ qa/
│  ├─ refs/
│  ├─ ART_AND_PROMO_PIPELINE.md
│  ├─ ASSET_MANIFEST_SPEC.md
│  ├─ CAPABILITY_MATRIX.md
│  ├─ CONFIG_SYSTEM.md
│  ├─ DOCS_MAP.md
│  ├─ GAME_CLIENT_REQUIREMENTS_MAIN.md
│  ├─ GS_REGISTRATION_ARTIFACTS.md
│  ├─ LOCALIZATION.md
│  ├─ MasterContext.md
│  ├─ ORCHESTRATOR_OUTPUTS.md
│  ├─ PROJECT.md
│  ├─ RELEASE_ARTIFACTS.md
│  └─ RELEASE_PROCESS.md
├─ games/
│  ├─ _archive/
│  └─ premium-slot/
├─ packages/
│  ├─ core-compliance/
│  ├─ core-protocol/
│  ├─ i18n/
│  ├─ operator-pariplay/
│  ├─ pixi-engine/
│  ├─ pixi-layout/
│  └─ ui-kit/
├─ tests/
│  ├─ compliance/
│  ├─ contract/
│  ├─ game/
│  ├─ layout/
│  ├─ net/
│  ├─ operator/
│  └─ tsconfig.json
├─ tools/
│  ├─ check-boundaries/
│  ├─ config-gen/
│  ├─ create-game/
│  ├─ i18n-check/
│  ├─ mcp/
│  ├─ release-pack/
│  └─ create-game.ts
├─ .gitignore
├─ AGENTS.md
├─ context.md
├─ package.json
├─ pnpm-lock.yaml
├─ pnpm-workspace.yaml
├─ progress.md
├─ README.md
└─ tsconfig.json
```
