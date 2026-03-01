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

const localeLabel = (lang: string, en: string, es: string, de: string): string =>
  lang === "es" ? es : lang === "de" ? de : en;

const buildLocaleFile = (options: CliOptions, lang: string, namespace: "common" | "paytable" | "rules") => {
  if (namespace === "common") {
    return {
      "game.title": lang === "en" ? options.name : `${options.name} (${lang.toUpperCase()})`,
      "hud.spin": localeLabel(lang, "SPIN", "GIRAR", "DREHEN"),
      "hud.balance": localeLabel(lang, "BALANCE", "SALDO", "GUTHABEN"),
      "hud.win": localeLabel(lang, "WIN", "GANANCIA", "GEWINN"),
      "hud.bet": localeLabel(lang, "BET", "APUESTA", "EINSATZ"),
      "status.ready": localeLabel(lang, "Game ready", "Juego listo", "Spiel bereit"),
    };
  }

  if (namespace === "paytable") {
    return {
      "paytable.title": localeLabel(lang, "Paytable", "Tabla de Pagos", "Gewinntabelle"),
      "paytable.stub": localeLabel(
        lang,
        "Replace with certified payouts before release.",
        "Reemplazar con pagos certificados antes del lanzamiento.",
        "Vor Release mit zertifizierten Auszahlungen ersetzen.",
      ),
    };
  }

  return {
    "rules.title": localeLabel(lang, "Rules", "Reglas", "Regeln"),
    "rules.stub": localeLabel(
      lang,
      "Replace with final legal/compliance rules before release.",
      "Reemplazar con reglas legales/finales antes del lanzamiento.",
      "Vor Release mit finalen Compliance-Regeln ersetzen.",
    ),
  };
};

const buildFiles = (options: CliOptions): FileEntry[] => {
  const gameSettings = {
    gameId: options.gameId,
    gameName: options.name,
    version: "0.1.0",
    themeId: options.themeId,
    reels: {
      rows: 3,
      cols: 5,
    },
    features: {
      freeSpins: true,
      buyFeature: false,
      autoplay: true,
    },
    gs: {
      runtimeTarget: "slot-browser-v1",
      historyPolicy: {
        enabled: true,
        openInSameWindow: true,
      },
      mathManifestPath: "math/math-pack.manifest.json",
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
  };

  const mathManifest = {
    id: `${options.gameId}-math-default`,
    modelVersion: "0.0.1-stub",
    rtp: 96,
    volatility: "medium",
    certified: false,
    note: "Stub manifest. Replace with certified math package before release.",
  };

  const registrationTemplate = {
    gameId: options.gameId,
    version: "0.1.0",
    runtimeTarget: "slot-browser-v1",
    releaseArtifact: "generated by tools/release-pack/create-release.ts",
  };

  const assetManifestSample = {
    manifestVersion: "1.0.0",
    gameId: options.gameId,
    gameName: options.name,
    themeId: options.themeId,
    updatedAt: "2026-03-01T00:00:00Z",
    owner: "art-team-placeholder",
    deliverables: {
      icons: [
        {
          id: "icon_512",
          status: "todo",
          sourcePath: "raw-assets/promo/icons/icon_512.png",
          runtimePath: "assets/promo/icons/icon_512.png",
          format: "png",
          width: 512,
          height: 512,
          fps: 0,
          codec: "none",
          compression: "lossless",
          bundle: "promo",
          notes: "store icon",
        },
      ],
      banners: [
        {
          id: "banner_landscape_1920x1080",
          status: "todo",
          sourcePath: "raw-assets/promo/banners/banner_landscape_1920x1080.jpg",
          runtimePath: "assets/promo/banners/banner_landscape_1920x1080.jpg",
          format: "jpg",
          width: 1920,
          height: 1080,
          fps: 0,
          codec: "none",
          compression: "jpg-q85",
          bundle: "promo",
          notes: "marketing banner",
        },
      ],
      promoVideos: [
        {
          id: "promo_teaser_15s",
          status: "todo",
          sourcePath: "raw-assets/promo/videos/promo_teaser_15s.mp4",
          runtimePath: "assets/promo/videos/promo_teaser_15s.mp4",
          format: "mp4",
          width: 1920,
          height: 1080,
          fps: 30,
          codec: "h264",
          compression: "8mbps",
          bundle: "promo",
          notes: "teaser clip",
        },
      ],
      preloaders: [
        {
          id: "preloader_static_1024",
          status: "todo",
          sourcePath: "raw-assets/preload/preloader_static_1024x1024.png",
          runtimePath: "assets/preload/preloader_static_1024x1024.png",
          format: "png",
          width: 1024,
          height: 1024,
          fps: 0,
          codec: "none",
          compression: "lossless",
          bundle: "preload",
          notes: "startup card",
        },
      ],
      bigWinVideos: [
        {
          id: "big_win",
          status: "todo",
          sourcePath: "raw-assets/main/win-videos/big_win.mp4",
          runtimePath: "assets/main/win-videos/big_win.mp4",
          format: "mp4",
          width: 1920,
          height: 1080,
          fps: 30,
          codec: "h264",
          compression: "8mbps",
          bundle: "main",
          notes: "big tier",
        },
      ],
      symbolAnimations: [
        {
          id: "symbol_wild_spine",
          symbolId: "wild",
          status: "todo",
          sourcePath: "raw-assets/main/spine/sym_wild/",
          runtimePath: "assets/main/spine/sym_wild/",
          format: "spine",
          width: 512,
          height: 512,
          fps: 30,
          codec: "none",
          compression: "atlas-webp",
          bundle: "main",
          spineSkeleton: "sym_wild.json",
          spineAtlas: "sym_wild.atlas",
          staticFirstFrame: "raw-assets/main/symbols/symbol_wild_static.png",
          hasSpritesheetFallback: true,
          notes: "frame0 must match static symbol",
        },
      ],
      uiKit: [
        {
          id: "hud_icons_set",
          status: "todo",
          sourcePath: "raw-assets/main/ui-kit/hud_icons/",
          runtimePath: "assets/main/ui-kit/hud_icons/",
          format: "png",
          width: 1024,
          height: 1024,
          fps: 0,
          codec: "none",
          compression: "atlas-webp",
          bundle: "main",
          notes: "spin/turbo/autoplay/buy/settings/history/sound",
        },
      ],
    },
    qualityBudgets: {
      maxAtlasSizeMobile: 2048,
      maxAtlasSizeDesktop: 4096,
      maxStartupTextureMBMobile: 64,
      maxStartupTextureMBDesktop: 128,
      maxInitialGpuUploadMB: 40,
      maxEventGpuUploadMB: 8,
    },
    bundleMap: {
      preload: ["preloaders"],
      main: ["symbolAnimations", "bigWinVideos", "uiKit"],
      promo: ["icons", "banners", "promoVideos"],
    },
  };

  const packageJson = {
    name: `@games/${options.gameId}`,
    version: "0.1.0",
    private: true,
    type: "module",
    scripts: {
      dev: "vite",
      build: "tsc -p tsconfig.json && vite build",
      "smoke:test": "node --experimental-strip-types tests/smoke/run-smoke.ts",
    },
    dependencies: {
      "@gamesv1/core-compliance": "workspace:*",
      "@gamesv1/core-protocol": "workspace:*",
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
import { mergeHudSchema, type HudSchema } from "@gamesv1/ui-kit";
import { I18N_KEYS, type TranslationKey } from "./i18n/keys";
import { outcomeMapperStub } from "./runtime/OutcomeMapper";
import { gameFeatureFlags } from "./config/featureFlags";
import { gameHudSchema } from "./config/hud";

export interface BootSnapshot {
  titleKey: TranslationKey;
  featureFlags: FeatureFlags;
  hudSchema: HudSchema;
  sampleOutcome: ReturnType<typeof outcomeMapperStub>;
}

export const bootstrapGame = (): BootSnapshot => {
  const featureFlags: FeatureFlags = {
    ...DefaultFeatureFlags,
    ...gameFeatureFlags,
  };

  const hudSchema = mergeHudSchema(gameHudSchema);
  const sampleOutcome = outcomeMapperStub({
    presentationPayload: {
      reelStopColumns: [
        [0, 1, 2],
        [1, 2, 3],
        [2, 3, 4],
        [3, 4, 5],
        [4, 5, 6],
      ],
      winAmount: 0,
    },
  });

  return {
    titleKey: I18N_KEYS.GAME_TITLE,
    featureFlags,
    hudSchema,
    sampleOutcome,
  };
};
`;

  const indexTs = `export * from "./bootstrap";
export * from "./runtime/OutcomeMapper";
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

  const outcomeMapperTs = `export interface RuntimeRoundPayload {
  roundId?: string;
  presentationPayload?: {
    reelStopColumns?: number[][];
    winAmount?: number;
  };
}

export interface NormalizedOutcome {
  roundId: string;
  reelStopColumns: number[][];
  totalWinCents: number;
  raw: RuntimeRoundPayload;
}

export const outcomeMapperStub = (payload: RuntimeRoundPayload): NormalizedOutcome => {
  const reelStopColumns = payload.presentationPayload?.reelStopColumns;
  if (!Array.isArray(reelStopColumns)) {
    throw new Error("Runtime presentationPayload.reelStopColumns is required.");
  }

  return {
    roundId: payload.roundId ?? "TODO_ROUND_ID",
    reelStopColumns,
    totalWinCents: Math.max(0, Math.round(payload.presentationPayload?.winAmount ?? 0)),
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
    buyFeature: false,
    sound: true,
    settings: true,
    history: true,
  },
};
`;

  const smokeRunnerTs = `import assert from "node:assert/strict";
import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

import { outcomeMapperStub } from "../../src/runtime/OutcomeMapper";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const configs = ["guest", "free", "real"] as const;

for (const configName of configs) {
  const configPath = path.join(__dirname, "configs", `${configName}.json`);
  const raw = await fs.readFile(configPath, "utf8");
  const parsed = JSON.parse(raw) as { mode: string };

  assert.equal(parsed.mode, configName);

  const normalized = outcomeMapperStub({
    roundId: `${configName}-round`,
    presentationPayload: {
      reelStopColumns: [
        [0, 1, 2],
        [1, 2, 3],
        [2, 3, 4],
        [3, 4, 5],
        [4, 5, 6],
      ],
      winAmount: configName === "real" ? 220 : 0,
    },
  });

  assert.ok(typeof normalized.totalWinCents === "number");
}

console.log("Smoke tests passed for guest/free/real configs.");
`;

  const releaseChecklist = `# Release Checklist (${options.gameId})

- [ ] Theme assets finalized and packed.
- [ ] Math manifest replaced with certified package reference.
- [ ] Localization reviewed for all supported languages.
- [ ] Guest/free/real smoke tests passed.
- [ ] Capability/config compliance reviewed.
- [ ] QA sign-off attached.
- [ ] Rollback plan documented.
`;

  const prSummary = `# PR Summary (${options.gameId})

## Scope
- Scaffolded canonical game package: games/${options.gameId}
- Added game.settings/locales/math manifest/runtime entrypoints/smoke tests

## Risks
- Runtime outcome mapper and math manifest are stubs; replace before production

## Validation
- [ ] Smoke test: corepack pnpm --filter @games/${options.gameId} run smoke:test
- [ ] Build: corepack pnpm --filter @games/${options.gameId} run build

## Rollback
- Remove this game folder and workspace references.
`;

  const smokeGuest = toPrettyJson({ mode: "guest", currency: "FUN" });
  const smokeFree = toPrettyJson({ mode: "free", currency: "FREE" });
  const smokeReal = toPrettyJson({ mode: "real", currency: "USD" });

  const files: FileEntry[] = [
    { relativePath: "game.settings.json", content: toPrettyJson(gameSettings) },
    { relativePath: "package.json", content: toPrettyJson(packageJson) },
    { relativePath: "tsconfig.json", content: toPrettyJson(tsconfigJson) },
    { relativePath: "vite.config.ts", content: viteConfigTs },
    {
      relativePath: "index.html",
      content: `<!doctype html>\n<html><body><div id=\"app\"></div><script type=\"module\" src=\"/src/main.ts\"></script></body></html>\n`,
    },
    { relativePath: "theme/theme.json", content: toPrettyJson(themeJson) },
    { relativePath: "math/math-pack.manifest.json", content: toPrettyJson(mathManifest) },
    { relativePath: "gs/registration.template.json", content: toPrettyJson(registrationTemplate) },
    { relativePath: "docs/asset-manifest.sample.json", content: toPrettyJson(assetManifestSample) },
    { relativePath: "raw-assets/main/.gitkeep", content: "" },
    { relativePath: "raw-assets/preload/.gitkeep", content: "" },
    { relativePath: "raw-assets/promo/.gitkeep", content: "" },
    { relativePath: "src/main.ts", content: mainTs },
    { relativePath: "src/bootstrap.ts", content: bootstrapTs },
    { relativePath: "src/index.ts", content: indexTs },
    { relativePath: "src/i18n/keys.ts", content: i18nKeysTs },
    { relativePath: "src/runtime/OutcomeMapper.ts", content: outcomeMapperTs },
    { relativePath: "src/config/featureFlags.ts", content: featureFlagsTs },
    { relativePath: "src/config/hud.ts", content: hudTs },
    { relativePath: "tests/smoke/run-smoke.ts", content: smokeRunnerTs },
    { relativePath: "tests/smoke/configs/guest.json", content: smokeGuest },
    { relativePath: "tests/smoke/configs/free.json", content: smokeFree },
    { relativePath: "tests/smoke/configs/real.json", content: smokeReal },
    { relativePath: "docs/release-checklist.md", content: releaseChecklist },
    { relativePath: "docs/pr-summary.md", content: prSummary },
  ];

  for (const language of options.languages) {
    files.push({
      relativePath: `locales/${language}/common.json`,
      content: toPrettyJson(buildLocaleFile(options, language, "common")),
    });
    files.push({
      relativePath: `locales/${language}/paytable.json`,
      content: toPrettyJson(buildLocaleFile(options, language, "paytable")),
    });
    files.push({
      relativePath: `locales/${language}/rules.json`,
      content: toPrettyJson(buildLocaleFile(options, language, "rules")),
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

  await fs.mkdir(targetDir, { recursive: true });

  for (const file of files) {
    const absolutePath = path.join(targetDir, file.relativePath);
    await fs.mkdir(path.dirname(absolutePath), { recursive: true });
    await fs.writeFile(absolutePath, file.content, "utf8");
  }

  console.log(`[create-game] Scaffold complete: games/${options.gameId}`);
  console.log(
    `[create-game] Run smoke tests: corepack pnpm --filter @games/${options.gameId} run smoke:test`,
  );
};

void createGame();
