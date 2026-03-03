import { LoadScreen } from "./app/screens/LoadScreen";
import { MainScreen } from "./app/screens/main/MainScreen";
import { userSettings } from "./app/utils/userSettings";
import { ConfigManager } from "./app/utils/ConfigManager";
import { gsRuntimeClient } from "./app/runtime";
import { ResolvedRuntimeConfigStore } from "./app/stores";
import { CreationEngine, setEngine } from "@gamesv1/pixi-engine";
import { i18n } from "@gamesv1/i18n";
import manifest from "./manifest.json";

import "@pixi/sound";

const appEngine = new CreationEngine();
setEngine(appEngine);

(async () => {
  await appEngine.init({
    background: "#1E1E1E",
    resizeOptions: { minWidth: 768, minHeight: 1024, letterbox: false },
    assetManifest: manifest,
    assetBasePath: "assets",
  });

  const urlParams = new URLSearchParams(window.location.search);
  const explicitDevFallback =
    urlParams.get("devConfig") === "1" ||
    urlParams.get("allowDevFallback") === "1" ||
    (import.meta.env.DEV && import.meta.env.VITE_ALLOW_DEV_CONFIG_FALLBACK === "1");

  let bootstrapFlow: Awaited<ReturnType<typeof gsRuntimeClient.bootstrap>> | null = null;
  try {
    bootstrapFlow = await gsRuntimeClient.bootstrap();
  } catch (error) {
    if (!explicitDevFallback) {
      throw error;
    }
    console.warn("[GS Runtime Bootstrap] Failed; explicit dev fallback enabled:", error);
  }
  await ConfigManager.init({
    runtimeConfigFromGs:
      (bootstrapFlow?.bootstrap.runtime as Record<string, unknown> | null)?.runtimeConfig as
        | Record<string, unknown>
        | undefined,
    capabilitiesFromGs:
      (bootstrapFlow?.bootstrap.policies as Record<string, unknown> | null)?.capabilities as
        | Record<string, unknown>
        | undefined,
    currencyCodeFromGs:
      ((bootstrapFlow?.bootstrap.runtime as Record<string, unknown> | null)?.wallet as
        | Record<string, unknown>
        | null)?.currencyCode as string | undefined,
    allowDevFallback: explicitDevFallback,
  });
  userSettings.init();

  const devLang = urlParams.get("lang");
  const brandName = urlParams.get("brand") || "Casino";
  const language = devLang || ResolvedRuntimeConfigStore.localization.defaultLang || "en";
  const localesPath = ResolvedRuntimeConfigStore.localization.contentPath;

  await i18n.init({
    language,
    fallbackLanguage: "en",
    localesPath,
    namespaces: ["common", "paytable", "rules"],
    brandOverrides: {
      BRAND_NAME: brandName,
    },
  });

  await appEngine.navigation.showScreen(LoadScreen);
  await appEngine.navigation.showScreen(MainScreen);
})();
