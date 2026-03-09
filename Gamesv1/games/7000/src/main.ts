import { LoadScreen } from "./app/screens/LoadScreen";
import { MainScreen } from "./app/screens/main/MainScreen";
import { userSettings } from "./app/utils/userSettings";
import { ConfigManager } from "./app/utils/ConfigManager";
import { gsRuntimeClient } from "./app/runtime";
import { ResolvedRuntimeConfigStore } from "./app/stores";
import { CreationEngine, setEngine } from "@gamesv1/pixi-engine";
import { i18n } from "@gamesv1/i18n";
import { createAudioCueRegistry } from "@gamesv1/ui-kit";
import manifest from "./manifest.json";
import { initializeProviderPackStatus } from "./app/assets/providerPackRegistry";
import { resolveCrazyRoosterBrandKit } from "./app/theme/brandKit";
import { applyCrazyRoosterSharedGameConfig } from "./game/config/CrazyRoosterGameConfig";

import "@pixi/sound";

applyCrazyRoosterSharedGameConfig();

const appEngine = new CreationEngine();
setEngine(appEngine);

(async () => {
  await appEngine.init({
    background: "#0d0d0d",
    resizeOptions: { minWidth: 768, minHeight: 1024, letterbox: false },
    assetManifest: manifest,
    assetBasePath: "assets",
  });

  const urlParams = new URLSearchParams(window.location.search);
  const providerPackStatus = await initializeProviderPackStatus(urlParams);
  const brandTheme = resolveCrazyRoosterBrandKit(
    urlParams.get("brandName") ?? urlParams.get("brand"),
    undefined,
    urlParams,
  );
  LoadScreen.configure({
    themeTokens: brandTheme,
    audioRegistry: createAudioCueRegistry(),
  });

  userSettings.init();
  await appEngine.navigation.showScreen(LoadScreen);
  const loadScreen = appEngine.navigation.currentScreen as LoadScreen | undefined;
  loadScreen?.setBootPhase(
    providerPackStatus.safePlaceholder ? "VALIDATING ART PACK" : "STARTING SESSION",
    8,
  );

  const explicitDevFallback =
    urlParams.get("devConfig") === "1" ||
    urlParams.get("allowDevFallback") === "1" ||
    urlParams.has("proofState") ||
    (import.meta.env.DEV && import.meta.env.VITE_ALLOW_DEV_CONFIG_FALLBACK === "1");

  let bootstrapFlow: Awaited<ReturnType<typeof gsRuntimeClient.bootstrap>> | null = null;
  try {
    bootstrapFlow = await gsRuntimeClient.bootstrap();
    loadScreen?.setBootPhase(
      gsRuntimeClient.isDemoRuntime() ? "LOADING DEMO CONFIG" : "SYNCING CONFIG",
      14,
    );
  } catch (error) {
    if (!explicitDevFallback) {
      throw error;
    }
    console.warn("[GS Runtime Bootstrap] Falling back to demo runtime:", error);
    loadScreen?.setBootPhase("LOADING DEMO CONFIG", 14);
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

  loadScreen?.setBootPhase("LOCALIZING SHELL", 18);

  const language =
    urlParams.get("lang") ||
    ResolvedRuntimeConfigStore.localization.defaultLang ||
    "en";
  await i18n.init({
    language,
    fallbackLanguage: "en",
    localesPath: ResolvedRuntimeConfigStore.localization.contentPath,
    namespaces: ["common", "paytable", "rules"],
    brandOverrides: {
      BRAND_NAME: brandTheme.brand.displayName,
    },
  });

  loadScreen?.beginAssetLoadPhase();
  await appEngine.navigation.showScreen(MainScreen);
  const mainScreen = appEngine.navigation.currentScreen as MainScreen | undefined;
  mainScreen?.installTestingHooks();

  (window as Window & { __game7000ProviderPack?: unknown }).__game7000ProviderPack =
    providerPackStatus;
})();
