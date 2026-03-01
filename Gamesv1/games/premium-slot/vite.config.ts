import path from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig } from "vite";

import { assetpackPlugin } from "./scripts/assetpack-vite-plugin";

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const pkg = (name) => path.resolve(rootDir, "../../packages", name, "src");

export default defineConfig({
  plugins: [assetpackPlugin()],
  server: {
    port: 8080,
    open: true,
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
  },
});
