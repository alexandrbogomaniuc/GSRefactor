import fs from "node:fs";
import path from "node:path";

const repoRoot = process.cwd();
const gameSrc = path.join(repoRoot, "games", "premium-slot", "src");

const blockedCodePatterns = [
  { name: "window.postMessage", regex: /window\.postMessage/g },
  { name: "new WebSocket", regex: /new\s+WebSocket\s*\(/g },
  { name: "WebSocket global", regex: /\bWebSocket\b/g },
];

const assetLiteralRegex = /["'`]([^"'`]+\.(png|mp3|wav|svg))["'`]/g;
const allowAssetLiteralFiles = new Set([
  path.join("app", "assets", "assetKeys.ts"),
  "manifest.json",
]);

const failures: string[] = [];

function walk(dir: string) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });

  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(fullPath);
      continue;
    }

    if (!entry.name.endsWith(".ts") && !entry.name.endsWith(".tsx")) {
      continue;
    }

    const relPath = path.relative(gameSrc, fullPath).replace(/\\/g, "/");
    const source = fs.readFileSync(fullPath, "utf8");

    for (const pattern of blockedCodePatterns) {
      const matches = source.match(pattern.regex);
      if (!matches) continue;

      if (pattern.name === "WebSocket global") {
        const filtered = matches.filter(
          () => !source.includes("new WebSocket(") && !source.includes("window.postMessage"),
        );
        if (filtered.length === 0) continue;
      }

      failures.push(`${relPath}: forbidden usage '${pattern.name}'`);
    }

    const allowAssetLiterals = Array.from(allowAssetLiteralFiles).some((allowed) =>
      relPath.endsWith(allowed),
    );

    if (!allowAssetLiterals) {
      const matches = source.match(assetLiteralRegex);
      if (matches?.length) {
        failures.push(`${relPath}: hardcoded asset literal(s) ${matches.join(", ")}`);
      }
    }
  }
}

walk(gameSrc);

if (failures.length) {
  console.error("Boundary check failed:\n" + failures.map((v) => `- ${v}`).join("\n"));
  process.exit(1);
}

console.log("Boundary check passed for games/premium-slot.");
