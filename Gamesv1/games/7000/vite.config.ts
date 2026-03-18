import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig } from "vite";

import { assetpackPlugin } from "./scripts/assetpack-vite-plugin";

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const pkg = (name) => path.resolve(rootDir, "../../packages", name, "src");
const gamesv1Root = path.resolve(rootDir, "../..");
const devWorkspaceRoot = path.resolve(rootDir, "../../../../..");

const parseEnvManifestOverride = (): string | null => {
  const raw = process.env.VITE_DONORLOCAL_MANIFEST_FS_PATH?.trim();
  if (!raw) {
    return null;
  }
  return path.isAbsolute(raw) ? raw : path.resolve(rootDir, raw);
};

const envManifestOverride = parseEnvManifestOverride();
const preferredDonorLocalManifestFsPath = path.resolve(
  rootDir,
  "../../GameseDonors/ChickenGame/assets/_donor_raw_local/runtime/manifest.json",
);
const donorLocalManifestFsPath =
  envManifestOverride && fs.existsSync(envManifestOverride)
    ? envManifestOverride
    : preferredDonorLocalManifestFsPath;
const donorLocalFsRoot = path.dirname(donorLocalManifestFsPath);

export default defineConfig({
  plugins: [assetpackPlugin()],
  server: {
    port: 8080,
    open: true,
    fs: {
      allow: [gamesv1Root, devWorkspaceRoot, donorLocalFsRoot],
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          const normalized = id.replaceAll("\\", "/");

          if (
            normalized.includes("/node_modules/pixi.js/") ||
            normalized.includes("/node_modules/@pixi/")
          ) {
            return "vendor-pixi";
          }

          if (normalized.includes("/node_modules/motion/")) {
            return "vendor-motion";
          }

          if (
            normalized.includes("/packages/core-protocol/") ||
            normalized.includes("/packages/core-compliance/")
          ) {
            return "runtime-core";
          }

          if (
            normalized.includes("/packages/ui-kit/") ||
            normalized.includes("/packages/pixi-engine/")
          ) {
            return "ui-engine";
          }

          return undefined;
        },
      },
    },
  },
  resolve: {
    alias: {
      "@gamesv1/core-protocol": pkg("core-protocol"),
      "@gamesv1/core-compliance": pkg("core-compliance"),
      "@gamesv1/ui-kit": pkg("ui-kit"),
      "@gamesv1/pixi-engine": pkg("pixi-engine"),
      "@gamesv1/i18n": pkg("i18n"),
    },
  },
  define: {
    APP_VERSION: JSON.stringify(process.env.npm_package_version),
    __DONORLOCAL_MANIFEST_FS_PATH__: JSON.stringify(donorLocalManifestFsPath),
  },
});
