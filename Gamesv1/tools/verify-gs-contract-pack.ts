import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

type LockV2 = {
  generatedAtUtc: string;
  hashAlgorithm: string;
  contractVersions: Record<string, unknown>;
  canonical: unknown;
};

type LockV1 = {
  algorithm: string;
  generatedAt: string;
  files: Record<string, string>;
};

type NormalizedLock = {
  generatedAtUtc: string;
  hashAlgorithm: string;
  contractVersions: Record<string, unknown>;
  canonicalMap: Record<string, string>;
};

type CliOptions = {
  upstreamPath: string;
  repoPath: string;
  strictUpstream: boolean;
};

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const ROOT_DIR = path.resolve(__dirname, "..");

const DEFAULT_UPSTREAM_PATH =
  process.env.GS_CONTRACT_UPSTREAM_PATH?.trim() ||
  path.resolve(ROOT_DIR, "../docs/gs");
const DEFAULT_REPO_PATH =
  process.env.GS_CONTRACT_REPO_PATH?.trim() ||
  path.resolve(ROOT_DIR, "docs/gs");

const toPosix = (value: string): string => value.replace(/\\/g, "/");
const normalizeHash = (value: string): string => value.trim().toLowerCase();

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const parseArgs = (argv: string[]): CliOptions => {
  let upstreamPath = DEFAULT_UPSTREAM_PATH;
  let repoPath = DEFAULT_REPO_PATH;
  let strictUpstream = false;

  for (let i = 2; i < argv.length; i += 1) {
    const token = argv[i];
    if (token === "--strict-upstream") {
      strictUpstream = true;
      continue;
    }
    if (token === "--upstream" && argv[i + 1]) {
      upstreamPath = argv[i + 1];
      i += 1;
      continue;
    }
    if (token === "--repo" && argv[i + 1]) {
      repoPath = argv[i + 1];
      i += 1;
      continue;
    }
  }

  return { upstreamPath, repoPath, strictUpstream };
};

const resolvePathCandidates = (rawPath: string): string[] => {
  const candidates = new Set<string>();
  const trimmed = rawPath.trim().replace(/[\\/]+$/, "");
  if (trimmed.length === 0) return [];

  candidates.add(path.resolve(trimmed));

  if (/^\/[A-Za-z0-9_\-./]+$/.test(trimmed)) {
    const windowsSuffix = trimmed.replace(/\//g, "\\");
    candidates.add(path.resolve(`C:${windowsSuffix}`));
    candidates.add(path.resolve(`E:${windowsSuffix}`));

    const usersPath = trimmed.match(/^\/Users\/(.+)$/);
    if (usersPath) {
      candidates.add(path.resolve(`C:\\Users\\${usersPath[1].replace(/\//g, "\\")}`));
      candidates.add(path.resolve(`E:\\Users\\${usersPath[1].replace(/\//g, "\\")}`));
    }

    candidates.add(`\\\\wsl.localhost\\Ubuntu${windowsSuffix}`);
    candidates.add(`\\\\wsl.localhost\\Ubuntu-24.04${windowsSuffix}`);
    candidates.add(`\\\\wsl.localhost\\Debian${windowsSuffix}`);
  }

  return [...candidates];
};

const resolveExistingPath = (rawPath: string): string | null => {
  const candidates = resolvePathCandidates(rawPath);
  for (const candidate of candidates) {
    if (fs.existsSync(candidate)) {
      return path.resolve(candidate);
    }
  }
  return null;
};

const candidateList = (rawPath: string): string =>
  resolvePathCandidates(rawPath)
    .map((candidate) => `- ${candidate}`)
    .join("\n");

const walkFiles = (dir: string): string[] => {
  const out: string[] = [];

  const walk = (current: string): void => {
    const entries = fs.readdirSync(current, { withFileTypes: true });
    entries.sort((a, b) => a.name.localeCompare(b.name));

    for (const entry of entries) {
      const fullPath = path.join(current, entry.name);
      if (entry.isDirectory()) {
        walk(fullPath);
      } else if (entry.isFile()) {
        out.push(fullPath);
      }
    }
  };

  walk(dir);
  return out;
};

const TEXT_EXTENSIONS = new Set([".md", ".json", ".txt", ".sha256"]);

const shouldNormalizeText = (filePath: string): boolean =>
  TEXT_EXTENSIONS.has(path.extname(filePath).toLowerCase());

const sha256 = (filePath: string, normalizeText = false): string => {
  const hash = crypto.createHash("sha256");
  if (!normalizeText || !shouldNormalizeText(filePath)) {
    hash.update(fs.readFileSync(filePath));
    return normalizeHash(hash.digest("hex"));
  }

  const normalized = fs
    .readFileSync(filePath, "utf8")
    .replace(/^\uFEFF/, "")
    .replace(/\r\n/g, "\n");
  hash.update(normalized, "utf8");
  return normalizeHash(hash.digest("hex"));
};

const collectHashes = (
  rootDir: string,
  options: { normalizeText: boolean },
): Record<string, string> => {
  const result: Record<string, string> = {};
  const files = walkFiles(rootDir);

  for (const absolutePath of files) {
    const relativePath = toPosix(path.relative(rootDir, absolutePath));
    result[relativePath] = sha256(absolutePath, options.normalizeText);
  }

  return result;
};

const readJson = (filePath: string): unknown =>
  JSON.parse(fs.readFileSync(filePath, "utf8").replace(/^\uFEFF/, ""));

const isStringRecord = (value: unknown): value is Record<string, string> =>
  isRecord(value) && Object.values(value).every((entry) => typeof entry === "string");

const toCanonicalPath = (
  section: "markdown" | "fixtures" | "schemas",
  entryPath: string,
): string => {
  const normalized = toPosix(entryPath);
  if (section === "markdown") {
    return normalized;
  }
  return normalized.startsWith(`${section}/`)
    ? normalized
    : `${section}/${normalized}`;
};

const parseCanonicalMap = (canonical: unknown): Record<string, string> => {
  const out: Record<string, string> = {};

  // Current upstream lock structure:
  // canonical: { markdown: {...}, fixtures: {...}, schemas: {...} }
  if (isRecord(canonical)) {
    const markdown = canonical.markdown;
    const fixtures = canonical.fixtures;
    const schemas = canonical.schemas;

    if (
      isStringRecord(markdown) &&
      isStringRecord(fixtures) &&
      isStringRecord(schemas)
    ) {
      for (const [entryPath, hash] of Object.entries(markdown)) {
        out[toCanonicalPath("markdown", entryPath)] = normalizeHash(hash);
      }
      for (const [entryPath, hash] of Object.entries(fixtures)) {
        out[toCanonicalPath("fixtures", entryPath)] = normalizeHash(hash);
      }
      for (const [entryPath, hash] of Object.entries(schemas)) {
        out[toCanonicalPath("schemas", entryPath)] = normalizeHash(hash);
      }
      return out;
    }
  }

  // Backward-compatible parsing for older lock shapes.
  if (Array.isArray(canonical)) {
    for (const entry of canonical) {
      if (!isRecord(entry)) {
        throw new Error("Invalid upstream lock: canonical array item must be an object.");
      }
      const filePath =
        typeof entry.path === "string"
          ? entry.path
          : typeof entry.file === "string"
            ? entry.file
            : typeof entry.relativePath === "string"
              ? entry.relativePath
              : null;
      const hashValue =
        typeof entry.sha256 === "string"
          ? entry.sha256
          : typeof entry.hash === "string"
            ? entry.hash
            : typeof entry.digest === "string"
              ? entry.digest
              : null;

      if (!filePath || !hashValue) {
        throw new Error(
          "Invalid upstream lock: canonical array item must include path and hash.",
        );
      }

      out[toPosix(filePath)] = normalizeHash(hashValue);
    }
    return out;
  }

  if (!isRecord(canonical)) {
    throw new Error(
      "Invalid upstream lock: canonical must be an object map or array.",
    );
  }

  for (const [rawPath, rawValue] of Object.entries(canonical)) {
    if (typeof rawValue === "string") {
      out[toPosix(rawPath)] = normalizeHash(rawValue);
      continue;
    }
    if (isRecord(rawValue)) {
      const hashValue =
        typeof rawValue.sha256 === "string"
          ? rawValue.sha256
          : typeof rawValue.hash === "string"
            ? rawValue.hash
            : typeof rawValue.digest === "string"
              ? rawValue.digest
              : null;
      if (!hashValue) {
        throw new Error(
          `Invalid upstream lock: canonical entry "${rawPath}" missing hash.`,
        );
      }
      out[toPosix(rawPath)] = normalizeHash(hashValue);
      continue;
    }
    throw new Error(
      `Invalid upstream lock: canonical entry "${rawPath}" has unsupported shape.`,
    );
  }

  return out;
};

const isLockV2 = (value: unknown): value is LockV2 =>
  isRecord(value) &&
  typeof value.generatedAtUtc === "string" &&
  typeof value.hashAlgorithm === "string" &&
  isRecord(value.contractVersions) &&
  "canonical" in value;

const isLockV1 = (value: unknown): value is LockV1 =>
  isRecord(value) &&
  typeof value.algorithm === "string" &&
  typeof value.generatedAt === "string" &&
  isRecord(value.files) &&
  Object.values(value.files).every((entry) => typeof entry === "string");

const normalizeLock = (parsed: unknown): NormalizedLock => {
  if (isLockV2(parsed)) {
    const canonicalMap = parseCanonicalMap(parsed.canonical);
    return {
      generatedAtUtc: parsed.generatedAtUtc,
      hashAlgorithm: parsed.hashAlgorithm,
      contractVersions: parsed.contractVersions,
      canonicalMap,
    };
  }

  if (isLockV1(parsed)) {
    const canonicalMap: Record<string, string> = {};
    for (const [rawPath, rawHash] of Object.entries(parsed.files)) {
      canonicalMap[toPosix(rawPath)] = normalizeHash(rawHash);
    }

    return {
      generatedAtUtc: parsed.generatedAt,
      hashAlgorithm: parsed.algorithm,
      contractVersions: { legacyLockFormat: "v1" },
      canonicalMap,
    };
  }

  throw new Error(
    "Invalid upstream contract-lock.json format. Expected v2 keys (generatedAtUtc, hashAlgorithm, contractVersions, canonical) or legacy v1 keys (algorithm, generatedAt, files).",
  );
};

const readUpstreamLock = (
  upstreamDir: string,
): { lock: NormalizedLock; canonicalMap: Record<string, string> } => {
  const lockPath = path.join(upstreamDir, "contract-lock.json");
  if (!fs.existsSync(lockPath)) {
    throw new Error(`Missing upstream contract-lock.json at ${lockPath}`);
  }

  const parsed = readJson(lockPath);
  const lock = normalizeLock(parsed);

  if (lock.hashAlgorithm.toLowerCase() !== "sha256") {
    throw new Error(
      `Unsupported upstream hashAlgorithm "${lock.hashAlgorithm}". Expected sha256.`,
    );
  }

  const canonicalMap = lock.canonicalMap;
  if (Object.keys(canonicalMap).length === 0) {
    throw new Error("Invalid upstream lock: canonical map is empty.");
  }

  return { lock, canonicalMap };
};

const sorted = (values: Iterable<string>): string[] =>
  [...values].sort((a, b) => a.localeCompare(b));

const printList = (title: string, values: string[]): void => {
  if (values.length === 0) return;
  console.error(`${title}:`);
  for (const value of values) {
    console.error(`  - ${value}`);
  }
};

const verifyUpstreamLockSemantics = (
  upstreamHashes: Record<string, string>,
  canonicalMap: Record<string, string>,
): number => {
  const lockPaths = new Set(Object.keys(canonicalMap));
  const upstreamPaths = new Set(Object.keys(upstreamHashes));

  const missingInUpstream = sorted(
    [...lockPaths].filter((entry) => !upstreamPaths.has(entry)),
  );

  const hashMismatches = sorted(
    [...lockPaths].filter(
      (entry) => upstreamHashes[entry] && upstreamHashes[entry] !== canonicalMap[entry],
    ),
  );

  const hasErrors = missingInUpstream.length > 0 || hashMismatches.length > 0;
  if (!hasErrors) {
    console.log("Upstream lock semantics verification passed.");
    console.log(`- canonical entries: ${lockPaths.size}`);
    return 0;
  }

  console.error("Upstream lock semantics verification failed.");
  printList("Missing files in upstream pack", missingInUpstream);
  printList("Upstream hash mismatches vs lock canonical map", hashMismatches);
  return 1;
};

const verifyRepoMirror = (
  upstreamHashes: Record<string, string>,
  repoHashes: Record<string, string>,
): number => {
  const upstreamPaths = new Set(Object.keys(upstreamHashes));
  const repoPaths = new Set(Object.keys(repoHashes));

  const missingInRepo = sorted(
    [...upstreamPaths].filter((entry) => !repoPaths.has(entry)),
  );
  const extraInRepo = sorted(
    [...repoPaths].filter((entry) => !upstreamPaths.has(entry)),
  );
  const hashMismatches = sorted(
    [...upstreamPaths].filter(
      (entry) => repoHashes[entry] && repoHashes[entry] !== upstreamHashes[entry],
    ),
  );

  const hasErrors =
    missingInRepo.length > 0 ||
    extraInRepo.length > 0 ||
    hashMismatches.length > 0;
  if (!hasErrors) {
    console.log("Repo mirror verification passed.");
    console.log(`- files compared: ${upstreamPaths.size}`);
    return 0;
  }

  console.error("Repo mirror verification failed.");
  printList("Missing files in repo mirror", missingInRepo);
  printList("Extra files in repo canonical scope", extraInRepo);
  printList("Repo byte/hash mismatches vs upstream", hashMismatches);
  return 1;
};

try {
  const options = parseArgs(process.argv);

  const repoDir = resolveExistingPath(options.repoPath);
  if (!repoDir) {
    throw new Error(
      `REPO path is not accessible. Checked:\n${candidateList(options.repoPath)}\nProvide --repo <path> or set GS_CONTRACT_REPO_PATH.`,
    );
  }

  const resolvedUpstream = resolveExistingPath(options.upstreamPath);
  if (!resolvedUpstream && options.strictUpstream) {
    throw new Error(
      `UPSTREAM path is not accessible. Checked:\n${candidateList(options.upstreamPath)}\nProvide --upstream <path> or set GS_CONTRACT_UPSTREAM_PATH.`,
    );
  }

  const upstreamDir = resolvedUpstream ?? repoDir;
  if (!resolvedUpstream) {
    console.warn(
      `UPSTREAM path not accessible; falling back to repo mirror for local integrity verification: ${repoDir}`,
    );
  }

  if (
    options.strictUpstream &&
    path.resolve(upstreamDir).toLowerCase() === path.resolve(repoDir).toLowerCase()
  ) {
    throw new Error(
      "Strict upstream mode requires distinct upstream and repo paths; they currently resolve to the same location.",
    );
  }

  const { lock, canonicalMap } = readUpstreamLock(upstreamDir);
  const upstreamCanonicalHashes = collectHashes(upstreamDir, { normalizeText: true });
  const upstreamByteHashes = collectHashes(upstreamDir, { normalizeText: false });
  const repoByteHashes = collectHashes(repoDir, { normalizeText: false });

  const lockStatus = verifyUpstreamLockSemantics(upstreamCanonicalHashes, canonicalMap);
  const mirrorStatus = verifyRepoMirror(upstreamByteHashes, repoByteHashes);

  if (lockStatus === 0 && mirrorStatus === 0) {
    console.log("GS contract pack verification passed.");
    console.log(`- upstream: ${upstreamDir}`);
    console.log(`- repo: ${repoDir}`);
    console.log(`- lock generatedAtUtc: ${lock.generatedAtUtc}`);
    process.exit(0);
  }

  process.exit(1);
} catch (error) {
  console.error((error as Error).message);
  process.exit(1);
}
