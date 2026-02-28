#!/usr/bin/env node

import { createHash } from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { execSync } from "node:child_process";

type CliArgs = {
  gameId: string;
  version?: string;
  staticOrigin: string;
  skipBuild: boolean;
  markKnownGood: boolean;
};

type FileDigest = {
  path: string;
  bytes: number;
  sha256: string;
};

type ReleaseIndexEntry = {
  releaseId: string;
  gameId: string;
  version: string;
  gitSha: string;
  createdAt: string;
  status: "candidate" | "known-good";
  outputDir: string;
};

const parseArgs = (argv: string[]): CliArgs => {
  let gameId = "";
  let version: string | undefined;
  let staticOrigin = "https://cdn.example.invalid/games";
  let skipBuild = false;
  let markKnownGood = false;

  for (let i = 2; i < argv.length; i += 1) {
    const token = argv[i];

    if (token === "--game" && argv[i + 1]) {
      gameId = argv[i + 1];
      i += 1;
      continue;
    }

    if (token === "--version" && argv[i + 1]) {
      version = argv[i + 1];
      i += 1;
      continue;
    }

    if (token === "--static-origin" && argv[i + 1]) {
      staticOrigin = argv[i + 1];
      i += 1;
      continue;
    }

    if (token === "--skip-build") {
      skipBuild = true;
      continue;
    }

    if (token === "--mark-known-good") {
      markKnownGood = true;
      continue;
    }
  }

  if (!gameId) {
    throw new Error("Missing --game <gameId>. Example: --game premium-slot");
  }

  return { gameId, version, staticOrigin, skipBuild, markKnownGood };
};

const toPosix = (value: string): string => value.split(path.sep).join("/");

const sha256 = (filePath: string): string => {
  const content = fs.readFileSync(filePath);
  return createHash("sha256").update(content).digest("hex");
};

const listFilesRecursive = (rootDir: string): string[] => {
  if (!fs.existsSync(rootDir)) return [];

  const out: string[] = [];
  const visit = (dir: string) => {
    const entries = fs.readdirSync(dir, { withFileTypes: true });
    entries.sort((a, b) => a.name.localeCompare(b.name));

    for (const entry of entries) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        visit(full);
      } else if (entry.isFile()) {
        out.push(full);
      }
    }
  };

  visit(rootDir);
  return out;
};

const collectDigests = (rootDir: string): FileDigest[] =>
  listFilesRecursive(rootDir).map((filePath) => {
    const stats = fs.statSync(filePath);
    return {
      path: toPosix(path.relative(rootDir, filePath)),
      bytes: stats.size,
      sha256: sha256(filePath),
    };
  });

const stableSortObject = (input: unknown): unknown => {
  if (Array.isArray(input)) {
    return input.map((item) => stableSortObject(item));
  }

  if (input && typeof input === "object") {
    const record = input as Record<string, unknown>;
    const sortedKeys = Object.keys(record).sort((a, b) => a.localeCompare(b));
    const out: Record<string, unknown> = {};

    for (const key of sortedKeys) {
      out[key] = stableSortObject(record[key]);
    }

    return out;
  }

  return input;
};

const writeStableJson = (target: string, data: unknown): void => {
  const stable = stableSortObject(data);
  fs.writeFileSync(target, `${JSON.stringify(stable, null, 2)}\n`);
};

const ensureDir = (dir: string): void => {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
};

const readJson = <T>(filePath: string): T =>
  JSON.parse(fs.readFileSync(filePath, "utf8")) as T;

const safeReadJson = <T>(filePath: string): T | null => {
  if (!fs.existsSync(filePath)) return null;
  return readJson<T>(filePath);
};

const run = (command: string, cwd: string): void => {
  execSync(command, { cwd, stdio: "inherit" });
};

const getGitInfo = (repoRoot: string): { gitSha: string; commitDateIso: string } => {
  const gitSha = execSync("git rev-parse HEAD", { cwd: repoRoot }).toString().trim();
  const commitDateIso = execSync("git show -s --format=%cI HEAD", { cwd: repoRoot })
    .toString()
    .trim();
  return { gitSha, commitDateIso };
};

const deterministicTimestamp = (fallbackIso: string): string => {
  const sourceDateEpoch = process.env.SOURCE_DATE_EPOCH;
  if (!sourceDateEpoch) return fallbackIso;

  const epoch = Number(sourceDateEpoch);
  if (!Number.isFinite(epoch)) return fallbackIso;
  return new Date(epoch * 1000).toISOString();
};

const collectPackageVersions = (
  repoRoot: string,
  gameId: string,
): Record<string, { version: string; source: string }> => {
  const versions: Record<string, { version: string; source: string }> = {};

  const rootPkg = readJson<{ name: string; version: string }>(path.join(repoRoot, "package.json"));
  versions[rootPkg.name] = { version: rootPkg.version, source: "repo-root" };

  const gamePkgPath = path.join(repoRoot, "games", gameId, "package.json");
  const gamePkg = readJson<{ name: string; version: string }>(gamePkgPath);
  versions[gamePkg.name] = { version: gamePkg.version, source: toPosix(path.relative(repoRoot, gamePkgPath)) };

  const packagesRoot = path.join(repoRoot, "packages");
  if (fs.existsSync(packagesRoot)) {
    for (const packageDirName of fs.readdirSync(packagesRoot)) {
      const pkgPath = path.join(packagesRoot, packageDirName, "package.json");
      if (!fs.existsSync(pkgPath)) continue;
      const pkg = readJson<{ name: string; version: string }>(pkgPath);
      versions[pkg.name] = {
        version: pkg.version,
        source: toPosix(path.relative(repoRoot, pkgPath)),
      };
    }
  }

  return versions;
};

const buildLocalizationManifest = (gameDir: string) => {
  const localesRoot = path.join(gameDir, "locales");
  const languages = fs.existsSync(localesRoot)
    ? fs
        .readdirSync(localesRoot, { withFileTypes: true })
        .filter((entry) => entry.isDirectory())
        .map((entry) => entry.name)
        .sort((a, b) => a.localeCompare(b))
    : [];

  const files = languages.map((language) => {
    const languageDir = path.join(localesRoot, language);
    const digests = collectDigests(languageDir);
    return {
      language,
      files: digests,
    };
  });

  return {
    sourceRoot: "locales",
    languages,
    files,
  };
};

const buildAssetManifest = (gameDir: string, repoRoot: string) => {
  const sourceManifestPath = path.join(gameDir, "src", "manifest.json");
  const sourceManifest = safeReadJson<Record<string, unknown>>(sourceManifestPath);

  const publicAssetsRoot = path.join(gameDir, "public", "assets");
  const distAssetsRoot = path.join(gameDir, "dist", "assets");

  const staticAssets = fs.existsSync(distAssetsRoot)
    ? collectDigests(distAssetsRoot)
    : collectDigests(publicAssetsRoot);

  return {
    source: sourceManifestPath && fs.existsSync(sourceManifestPath)
      ? {
          path: toPosix(path.relative(repoRoot, sourceManifestPath)),
          sha256: sha256(sourceManifestPath),
        }
      : null,
    runtimeAssetRoot: fs.existsSync(distAssetsRoot) ? "dist/assets" : "public/assets",
    assetCount: staticAssets.length,
    bundles: sourceManifest,
    files: staticAssets,
  };
};

const buildClientBundleManifest = (gameDir: string) => {
  const distRoot = path.join(gameDir, "dist");
  if (!fs.existsSync(distRoot)) {
    throw new Error(`Missing build output at ${distRoot}. Run release command without --skip-build.`);
  }

  const files = collectDigests(distRoot);
  const indexPath = path.join(distRoot, "index.html");

  return {
    distRoot: "dist",
    entrypoint: fs.existsSync(indexPath) ? "index.html" : null,
    fileCount: files.length,
    files,
  };
};

const buildMathManifestReference = (gameDir: string, repoRoot: string) => {
  const explicitMathPack = path.join(gameDir, "math", "math-pack.json");
  const gsTemplateParams = path.join(gameDir, "gs", "template-params.json");

  if (fs.existsSync(explicitMathPack)) {
    return {
      strategy: "static-math-pack",
      path: toPosix(path.relative(repoRoot, explicitMathPack)),
      sha256: sha256(explicitMathPack),
      launchTimeInjection: false,
    };
  }

  if (fs.existsSync(gsTemplateParams)) {
    return {
      strategy: "gs-template-params-reference",
      path: toPosix(path.relative(repoRoot, gsTemplateParams)),
      sha256: sha256(gsTemplateParams),
      launchTimeInjection: false,
    };
  }

  return {
    strategy: "missing",
    path: null,
    sha256: null,
    launchTimeInjection: false,
    warning: "No math-pack.json or gs/template-params.json found.",
  };
};

const writeMarkdown = (target: string, lines: string[]): void => {
  fs.writeFileSync(target, `${lines.join("\n")}\n`);
};

const main = () => {
  const args = parseArgs(process.argv);
  const repoRoot = process.cwd();
  const gameDir = path.join(repoRoot, "games", args.gameId);

  if (!fs.existsSync(gameDir)) {
    throw new Error(`Game not found: games/${args.gameId}`);
  }

  const gameSettingsPath = path.join(gameDir, "game.settings.json");
  if (!fs.existsSync(gameSettingsPath)) {
    throw new Error(`Missing game.settings.json in games/${args.gameId}`);
  }

  const gameSettings = readJson<{ gameId: string; gameName: string; version?: string; features?: Record<string, boolean> }>(
    gameSettingsPath,
  );

  const releaseVersion = args.version ?? gameSettings.version ?? "0.0.0";
  const { gitSha, commitDateIso } = getGitInfo(repoRoot);
  const createdAt = deterministicTimestamp(commitDateIso);
  const releaseId = `${releaseVersion}+${gitSha.slice(0, 8)}`;

  if (!args.skipBuild) {
    run("node --experimental-strip-types tools/config-gen/src/index.ts", repoRoot);
    run(`corepack pnpm --filter @games/${args.gameId} build`, repoRoot);
    run(`corepack pnpm --filter @games/${args.gameId} exec vite build`, repoRoot);
  }

  const bundleManifest = buildClientBundleManifest(gameDir);
  const assetManifest = buildAssetManifest(gameDir, repoRoot);
  const localizationManifest = buildLocalizationManifest(gameDir);
  const mathPackageManifestReference = buildMathManifestReference(gameDir, repoRoot);
  const packageVersions = collectPackageVersions(repoRoot, args.gameId);

  const staticOrigin = args.staticOrigin.replace(/\/$/, "");
  const gsCompatibility = {
    runtimeTarget: "gs-http-runtime",
    websocketSupport: "legacy-experimental",
    launchTimeMathInjectionAllowed: false,
    staticAssetsFromCdn: true,
    requiredCapabilities: [
      "idempotency",
      "requestCounter",
      "restore",
      "wallet-authoritative-server",
    ],
    clientEntrypointUrl: `${staticOrigin}/${args.gameId}/${releaseVersion}/index.html`,
    assetManifestUrl: `${staticOrigin}/${args.gameId}/${releaseVersion}/manifest.json`,
    localizationBaseUrl: `${staticOrigin}/${args.gameId}/${releaseVersion}/locales/`,
    mathPackageReferencePath: mathPackageManifestReference.path,
  };

  const releaseMetadata = {
    releaseId,
    gameId: args.gameId,
    gameName: gameSettings.gameName,
    version: releaseVersion,
    gitSha,
    createdAt,
    deterministicSource: process.env.SOURCE_DATE_EPOCH ? "SOURCE_DATE_EPOCH" : "git-commit-date",
    packagingRules: {
      launchTimeMathInjectionAllowed: false,
      staticAndMathArtifactsSeparated: true,
      noSecretsInArtifacts: true,
    },
  };

  const outputBase = path.join(gameDir, "release-packs");
  const outputDir = path.join(outputBase, releaseId);
  if (fs.existsSync(outputDir)) {
    fs.rmSync(outputDir, { recursive: true, force: true });
  }
  ensureDir(outputDir);

  const artifactsDir = path.join(outputDir, "artifacts");
  ensureDir(artifactsDir);

  const clientBundleManifestPath = path.join(artifactsDir, "client-bundle.manifest.json");
  const assetManifestPath = path.join(artifactsDir, "asset.manifest.json");
  const localizationManifestPath = path.join(artifactsDir, "localization.manifest.json");
  const mathRefPath = path.join(artifactsDir, "math-package.manifest-reference.json");
  const packageVersionsPath = path.join(artifactsDir, "package-versions.json");
  const releaseMetadataPath = path.join(artifactsDir, "release-metadata.json");
  const compatibilityPath = path.join(artifactsDir, "gs-compatibility.json");

  writeStableJson(clientBundleManifestPath, bundleManifest);
  writeStableJson(assetManifestPath, assetManifest);
  writeStableJson(localizationManifestPath, localizationManifest);
  writeStableJson(mathRefPath, mathPackageManifestReference);
  writeStableJson(packageVersionsPath, packageVersions);
  writeStableJson(releaseMetadataPath, releaseMetadata);
  writeStableJson(compatibilityPath, gsCompatibility);

  const allArtifactFiles = collectDigests(artifactsDir);
  const checksumsPath = path.join(outputDir, "checksums.sha256.json");
  writeStableJson(checksumsPath, {
    releaseId,
    files: allArtifactFiles,
  });

  const indexPath = path.join(outputBase, "index.json");
  const existingIndex = safeReadJson<{ releases: ReleaseIndexEntry[] }>(indexPath) ?? { releases: [] };
  const previousKnownGood =
    existingIndex.releases.find((entry) => entry.status === "known-good") ?? existingIndex.releases[0] ?? null;

  const newEntry: ReleaseIndexEntry = {
    releaseId,
    gameId: args.gameId,
    version: releaseVersion,
    gitSha,
    createdAt,
    status: args.markKnownGood ? "known-good" : "candidate",
    outputDir: toPosix(path.relative(repoRoot, outputDir)),
  };

  const mergedEntries = [
    newEntry,
    ...existingIndex.releases.filter((entry) => entry.releaseId !== releaseId),
  ];

  writeStableJson(indexPath, { releases: mergedEntries });

  const registrationPackPath = path.join(outputDir, "GS_REGISTRATION_PACK.md");
  writeMarkdown(registrationPackPath, [
    "# GS Registration Pack",
    "",
    `- Release ID: \`${releaseId}\``,
    `- Game: \`${args.gameId}\` (${gameSettings.gameName})`,
    `- Version: \`${releaseVersion}\``,
    `- Git SHA: \`${gitSha}\``,
    `- Created At: \`${createdAt}\``,
    "",
    "## Artifact Set",
    "",
    "- client bundle manifest",
    "- asset manifest",
    "- localization manifest",
    "- math package manifest reference",
    "- package versions",
    "- release metadata",
    "- GS compatibility metadata",
    "- checksums",
    "",
    "## GS Ops Actions",
    "",
    "1. Verify checksums from `checksums.sha256.json`.",
    "2. Upload client/static assets to the versioned CDN path.",
    "3. Register release metadata and compatibility payload in GS registration workflow.",
    "4. Enable for canary environment first.",
    "5. Promote after smoke checklist passes.",
    "",
    "## No-Secret Guarantee",
    "",
    "Generated artifacts intentionally exclude credentials/tokens/secrets.",
  ]);

  const rollbackPackPath = path.join(outputDir, "ROLLBACK_PACK.md");
  writeMarkdown(rollbackPackPath, [
    "# Rollback Pack",
    "",
    `- Current candidate: \`${releaseId}\``,
    previousKnownGood
      ? `- Previous known-good: \`${previousKnownGood.releaseId}\``
      : "- Previous known-good: _none recorded_",
    "",
    "## Rollback Procedure",
    "",
    "1. Disable current release in GS registration.",
    previousKnownGood
      ? `2. Re-enable previous known-good release \`${previousKnownGood.releaseId}\`.`
      : "2. Re-enable last validated release recorded by GS Ops.",
    "3. Verify launch URL and one normal round transaction.",
    "4. Verify reconnect restore path and wallet consistency.",
    "",
    "## Rollback Validation",
    "",
    "- Launch success",
    "- Normal round request/response",
    "- Reconnect restore",
    "- Balance consistency",
  ]);

  const canaryChecklistPath = path.join(outputDir, "CANARY_CHECKLIST.md");
  writeMarkdown(canaryChecklistPath, [
    "# Canary Checklist",
    "",
    "- [ ] GS registration created for candidate release",
    "- [ ] CDN/static assets reachable for candidate version",
    "- [ ] Guest launch URL opens and initializes",
    "- [ ] Free launch URL opens and initializes",
    "- [ ] Real launch URL opens and initializes",
    "- [ ] One normal transaction path validated",
    "- [ ] Error logs monitored for first canary window",
    "- [ ] Rollback path prepared and tested",
  ]);

  const smokeChecklistPath = path.join(outputDir, "SMOKE_TEST_CHECKLIST.md");
  writeMarkdown(smokeChecklistPath, [
    "# Smoke Test Checklist",
    "",
    "- [ ] launch",
    "- [ ] normal round",
    "- [ ] reconnect",
    "- [ ] turbo",
    "- [ ] history",
    "- [ ] localization override",
    "- [ ] trunc-cents",
    "- [ ] delayed wallet messages",
    "- [ ] free spins",
    "- [ ] buy feature",
    "- [ ] rollback",
  ]);

  const summaryPath = path.join(outputDir, "RELEASE_PACK_SUMMARY.json");
  writeStableJson(summaryPath, {
    releaseId,
    outputDir: toPosix(path.relative(repoRoot, outputDir)),
    artifacts: {
      clientBundleManifest: toPosix(path.relative(outputDir, clientBundleManifestPath)),
      assetManifest: toPosix(path.relative(outputDir, assetManifestPath)),
      localizationManifest: toPosix(path.relative(outputDir, localizationManifestPath)),
      mathPackageManifestReference: toPosix(path.relative(outputDir, mathRefPath)),
      packageVersions: toPosix(path.relative(outputDir, packageVersionsPath)),
      releaseMetadata: toPosix(path.relative(outputDir, releaseMetadataPath)),
      gsCompatibility: toPosix(path.relative(outputDir, compatibilityPath)),
      checksums: toPosix(path.relative(outputDir, checksumsPath)),
      registrationPack: toPosix(path.relative(outputDir, registrationPackPath)),
      rollbackPack: toPosix(path.relative(outputDir, rollbackPackPath)),
      canaryChecklist: toPosix(path.relative(outputDir, canaryChecklistPath)),
      smokeChecklist: toPosix(path.relative(outputDir, smokeChecklistPath)),
    },
  });

  console.log(`\nRelease pack created: ${toPosix(path.relative(repoRoot, outputDir))}`);
  console.log(`Summary: ${toPosix(path.relative(repoRoot, summaryPath))}`);
};

main();
