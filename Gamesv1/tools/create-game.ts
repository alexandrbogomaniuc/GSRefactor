#!/usr/bin/env tsx

import fs from "node:fs/promises";
import path from "node:path";

type CliOptions = {
  gameId: string;
  name: string;
  themeId: string;
  languages: string[];
  dryRun: boolean;
  force: boolean;
};

type FileEntry = {
  relativePath: string;
  content: string;
};

const usage = `
Usage:
  corepack pnpm run create-game -- --gameId <game-id> --name "<Game Name>" --themeId <theme-id> [--languages en,es,de] [--dry-run] [--force]

Required:
  --gameId     Lowercase kebab-case identifier (example: dragon-flare)
  --name       Display name (example: Dragon Flare)
  --themeId    Theme identifier (example: fantasy-dragon)

Optional:
  --languages  Comma-separated locales. Must include en + at least two additional locales.
  --dry-run    Print planned files without writing.
  --force      Overwrite existing files in an existing game directory.
`;

const fail = (message: string): never => {
  console.error(`[create-game] ${message}`);
  process.exit(1);
};

const parseArgs = (argv: string[]): CliOptions => {
  const argMap = new Map<string, string | boolean>();

  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (!arg.startsWith("--")) continue;

    const next = argv[i + 1];
    const hasValue = next && !next.startsWith("--");
    argMap.set(arg, hasValue ? next : true);
    if (hasValue) i += 1;
  }

  if (argv.includes("-h") || argMap.has("--help")) {
    console.log(usage.trim());
    process.exit(0);
  }

  const gameId = (argMap.get("--gameId") ?? argMap.get("--game-id")) as string | undefined;
  const name = argMap.get("--name") as string | undefined;
  const themeId = (argMap.get("--themeId") ?? argMap.get("--theme-id")) as string | undefined;
  const languagesInput = (argMap.get("--languages") ?? "en,es,de") as string;

  if (!gameId) fail("Missing required --gameId");
  if (!name) fail("Missing required --name");
  if (!themeId) fail("Missing required --themeId");

  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(gameId)) {
    fail("--gameId must be lowercase kebab-case (a-z, 0-9, -)");
  }

  const languages = Array.from(
    new Set(
      languagesInput
        .split(",")
        .map((v) => v.trim().toLowerCase())
        .filter(Boolean),
    ),
  );

  if (!languages.includes("en")) {
    fail("--languages must include 'en'");
  }

  if (languages.length < 3) {
    fail("--languages must include at least 3 languages (en + 2 others)");
  }

  return {
    gameId,
    name,
    themeId,
    languages,
    dryRun: Boolean(argMap.get("--dry-run")),
    force: Boolean(argMap.get("--force")),
  };
};

const toPrettyJson = (value: unknown): string => `${JSON.stringify(value, null, 2)}\n`;

const buildFiles = (options: CliOptions): FileEntry[] => {
  const i18nEntries = Object.fromEntries(
    options.languages.map((lang) => {
      const titleSuffix = lang === "en" ? options.name : `${options.name} (${lang.toUpperCase()})`;
      return [
        `${lang}.json`,
        toPrettyJson({
          "game.title": titleSuffix,
          "hud.spin": lang === "es" ? "GIRAR" : lang === "de" ? "DREHEN" : "SPIN",
          "hud.balance": lang === "es" ? "SALDO" : lang === "de" ? "GUTHABEN" : "BALANCE",
          "hud.win": lang === "es" ? "GANANCIA" : lang === "de" ? "GEWINN" : "WIN",
          "hud.bet": lang === "es" ? "APUESTA" : lang === "de" ? "EINSATZ" : "BET",
          "status.ready":
            lang === "es"
              ? "Juego listo"
              : lang === "de"
                ? "Spiel bereit"
                : "Game ready",
        }),
      ];
    }),
  );

  const gameJson = {
    gameId: options.gameId,
    name: options.name,
    themeId: options.themeId,
    version: "1.0.0",
    entrypoint: "src/main.ts",
    mathPackPath: "math/math-pack.json",
    themeConfigPath: "theme/theme.json",
    i18n: {
      defaultLanguage: "en",
      supportedLanguages: options.languages,
    },
  };

  const themeJson = {
    themeId: options.themeId,
    gameId: options.gameId,
    palette: {
      primary: "#14213D",
      accent: "#FCA311",
      background: "#0B132B",
      text: "#E5E5E5",
    },
    assets: {
      bundle: "main",
      loadingScreen: "preload",
    },
  };

  const mathPackJson = {
    id: `${options.gameId}-default-math`,
    rtp: 96.0,
    volatility: "medium",
    maxWinMultiplier: 5000,
    reels: {
      count: 5,
      rows: 3,
    },
    notes: "Stub math pack. Replace with certified model before release.",
  };

  const packageJson = {
    name: `@games/${options.gameId}`,
    version: "0.1.0",
    private: true,
    type: "module",
    scripts: {
      dev: "vite",
      build: "tsc -p tsconfig.json && vite build",
      "smoke:test": "npx tsx tests/smoke/run-smoke.ts",
    },
    dependencies: {
      "@gamesv1/core-compliance": "workspace:*",
      "@gamesv1/i18n": "workspace:*",
      "@gamesv1/pixi-engine": "workspace:*",
      "@gamesv1/ui-kit": "workspace:*",
      "pixi.js": "^8.8.1",
    },
    devDependencies: {
      typescript: "^5.9.3",
      vite: "^6.2.0",
    },
  };

  const tsconfigJson = {
    compilerOptions: {
      target: "ES2022",
      module: "ESNext",
      moduleResolution: "Bundler",
      strict: true,
      resolveJsonModule: true,
      esModuleInterop: true,
      skipLibCheck: true,
      noEmit: true,
      types: ["node"],
    },
    include: ["src", "tests", "vite.config.ts"],
  };

  const viteConfigTs = `import { defineConfig } from "vite";

export default defineConfig({
  server: {
    host: true,
    port: 5173,
  },
});
`;

  const mainTs = `import { bootstrapGame } from "./bootstrap";

void bootstrapGame();
`;

  const bootstrapTs = `import { DefaultFeatureFlags, type FeatureFlags } from "@gamesv1/core-compliance";
import { DefaultHudSchema, mergeHudSchema, type HudSchema } from "@gamesv1/ui-kit";
import { I18N_KEYS, type TranslationKey } from "./i18n/keys";
import { outcomeMapperStub } from "./outcome/OutcomeMapper";
import { gameFeatureFlags } from "./config/featureFlags";
import { gameHudSchema } from "./config/hud";

export interface BootSnapshot {
  titleKey: TranslationKey;
  featureFlags: FeatureFlags;
  hudSchema: HudSchema;
  sampleOutcome: ReturnType<typeof outcomeMapperStub>;
}

const t = (key: TranslationKey): string => key;

export const bootstrapGame = (): BootSnapshot => {
  const featureFlags: FeatureFlags = {
    ...DefaultFeatureFlags,
    ...gameFeatureFlags,
  };

  const hudSchema = mergeHudSchema(gameHudSchema);
  const sampleOutcome = outcomeMapperStub({});
  const snapshot: BootSnapshot = {
    titleKey: I18N_KEYS.GAME_TITLE,
    featureFlags,
    hudSchema,
    sampleOutcome,
  };

  console.info("[bootstrapGame]", {
    title: t(snapshot.titleKey),
    featureFlags: snapshot.featureFlags,
    hudSchema: snapshot.hudSchema,
  });

  return snapshot;
};

void DefaultHudSchema;
`;

  const indexTs = `export * from "./bootstrap";
export * from "./outcome/OutcomeMapper";
`;

  const i18nKeysTs = `export const I18N_KEYS = {
  GAME_TITLE: "game.title",
  HUD_SPIN: "hud.spin",
  HUD_BALANCE: "hud.balance",
  HUD_WIN: "hud.win",
  HUD_BET: "hud.bet",
  STATUS_READY: "status.ready",
} as const;

export type TranslationKey = (typeof I18N_KEYS)[keyof typeof I18N_KEYS];
`;

  const outcomeMapperTs = `export interface ProviderOutcome {
  roundId?: string;
  transactionId?: string;
  totalWinCents?: number;
  symbols?: number[][];
  meta?: Record<string, unknown>;
}

export interface NormalizedOutcome {
  roundId: string;
  transactionId: string;
  totalWinCents: number;
  symbols: number[][];
  isWin: boolean;
  raw: ProviderOutcome;
}

export const outcomeMapperStub = (payload: ProviderOutcome): NormalizedOutcome => {
  return {
    roundId: payload.roundId ?? "TODO_ROUND_ID",
    transactionId: payload.transactionId ?? "TODO_TX_ID",
    totalWinCents: payload.totalWinCents ?? 0,
    symbols: payload.symbols ?? [],
    isWin: (payload.totalWinCents ?? 0) > 0,
    raw: payload,
  };
};
`;

  const featureFlagsTs = `import type { FeatureFlags } from "@gamesv1/core-compliance";

export const gameFeatureFlags: Partial<FeatureFlags> = {
  autoplayAllowed: true,
  turboplayAllowed: true,
  spinProfilingEnabled: false,
};
`;

  const hudTs = `import type { HudSchema } from "@gamesv1/ui-kit";

export const gameHudSchema: Partial<HudSchema> = {
  controls: {
    autoplay: true,
    turbo: true,
  },
};
`;

  const smokeRunnerTs = `import assert from "node:assert/strict";
import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

import { outcomeMapperStub } from "../../src/outcome/OutcomeMapper";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const configs = ["guest", "free", "real"] as const;

for (const configName of configs) {
  const configPath = path.join(__dirname, "configs", configName + ".json");
  const raw = await fs.readFile(configPath, "utf8");
  const parsed = JSON.parse(raw) as { mode: string; fakeOutcome?: { totalWinCents?: number } };

  assert.equal(parsed.mode, configName);

  const normalized = outcomeMapperStub({
    totalWinCents: parsed.fakeOutcome?.totalWinCents ?? 0,
  });

  assert.ok(typeof normalized.totalWinCents === "number");
}

console.log("Smoke tests passed for guest/free/real configs.");
`;

  const releaseChecklist = `# Release Checklist (${options.gameId})

- [ ] Theme assets finalized and packed.
- [ ] Math pack replaced with certified payload.
- [ ] Localization reviewed for all supported languages.
- [ ] Guest/free/real smoke tests passed.
- [ ] Compliance review completed (feature flags + min spin rules).
- [ ] QA sign-off attached.
- [ ] Rollback plan documented.
`;

  const prSummary = `# PR Summary (${options.gameId})

## Scope
- Scaffolded new game package: games/${options.gameId}
- Added config, i18n, math, entrypoints, outcome mapper, and smoke tests

## Risks
- Math pack and outcome mapper are stubs and must be replaced before production

## Validation
- [ ] Smoke test: corepack pnpm --filter @games/${options.gameId} run smoke:test
- [ ] Build: corepack pnpm --filter @games/${options.gameId} run build

## Rollback
- Revert this game folder and remove workspace references if added elsewhere.
`;

  const smokeGuest = toPrettyJson({
    mode: "guest",
    currency: "FUN",
    fakeOutcome: { totalWinCents: 0 },
  });

  const smokeFree = toPrettyJson({
    mode: "free",
    currency: "FREE",
    fakeOutcome: { totalWinCents: 150 },
  });

  const smokeReal = toPrettyJson({
    mode: "real",
    currency: "USD",
    fakeOutcome: { totalWinCents: 200 },
  });

  const files: FileEntry[] = [
    { relativePath: "game.json", content: toPrettyJson(gameJson) },
    { relativePath: "package.json", content: toPrettyJson(packageJson) },
    { relativePath: "tsconfig.json", content: toPrettyJson(tsconfigJson) },
    { relativePath: "vite.config.ts", content: viteConfigTs },
    { relativePath: "theme/theme.json", content: toPrettyJson(themeJson) },
    { relativePath: "math/math-pack.json", content: toPrettyJson(mathPackJson) },
    { relativePath: "src/main.ts", content: mainTs },
    { relativePath: "src/bootstrap.ts", content: bootstrapTs },
    { relativePath: "src/index.ts", content: indexTs },
    { relativePath: "src/i18n/keys.ts", content: i18nKeysTs },
    { relativePath: "src/outcome/OutcomeMapper.ts", content: outcomeMapperTs },
    { relativePath: "src/config/featureFlags.ts", content: featureFlagsTs },
    { relativePath: "src/config/hud.ts", content: hudTs },
    { relativePath: "tests/smoke/run-smoke.ts", content: smokeRunnerTs },
    { relativePath: "tests/smoke/configs/guest.json", content: smokeGuest },
    { relativePath: "tests/smoke/configs/free.json", content: smokeFree },
    { relativePath: "tests/smoke/configs/real.json", content: smokeReal },
    { relativePath: "docs/release-checklist.md", content: releaseChecklist },
    { relativePath: "docs/pr-summary.md", content: prSummary },
  ];

  for (const [fileName, fileContent] of Object.entries(i18nEntries)) {
    files.push({
      relativePath: `i18n/${fileName}`,
      content: fileContent,
    });
  }

  return files;
};

const createGame = async (): Promise<void> => {
  const options = parseArgs(process.argv.slice(2));
  const repoRoot = process.cwd();
  const targetDir = path.join(repoRoot, "games", options.gameId);

  const targetExists = await fs
    .access(targetDir)
    .then(() => true)
    .catch(() => false);

  if (targetExists && !options.force) {
    fail(`Target already exists: games/${options.gameId}. Use --force to overwrite files.`);
  }

  const files = buildFiles(options);

  if (options.dryRun) {
    console.log(`[create-game] Dry run for games/${options.gameId}`);
    for (const file of files) {
      console.log(`  - ${file.relativePath}`);
    }
    return;
  }

  const dirs = [
    "theme",
    "i18n",
    "math",
    "src",
    "src/config",
    "src/i18n",
    "src/outcome",
    "tests",
    "tests/smoke",
    "tests/smoke/configs",
    "docs",
  ];

  await fs.mkdir(targetDir, { recursive: true });

  for (const dir of dirs) {
    await fs.mkdir(path.join(targetDir, dir), { recursive: true });
  }

  for (const file of files) {
    const absolutePath = path.join(targetDir, file.relativePath);
    await fs.writeFile(absolutePath, file.content, "utf8");
  }

  console.log(`[create-game] Scaffold complete: games/${options.gameId}`);
  console.log(
    `[create-game] Run smoke tests: corepack pnpm --filter @games/${options.gameId} run smoke:test`,
  );
};

void createGame();
