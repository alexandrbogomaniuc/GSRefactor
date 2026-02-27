import { LoadScreen } from "./app/screens/LoadScreen";
import { MainScreen } from "./app/screens/main/MainScreen";
import { userSettings } from "./app/utils/userSettings";
import { ConfigManager } from "./app/utils/ConfigManager";
import { setEngine, CreationEngine } from "@gs/slot-shell";
import { i18n } from "@gs/i18n";

/**
 * Importing these modules will automatically register there plugins with the engine.
 */
import "@pixi/sound";
// import "@esotericsoftware/spine-pixi-v8";

// Create a new creation engine instance
const engine = new CreationEngine();
setEngine(engine);

(async () => {
  // Initialize the creation engine instance
  await engine.init({
    background: "#1E1E1E",
    resizeOptions: { minWidth: 768, minHeight: 1024, letterbox: false },
  });

  // 1. Initialize Configuration (Loads settings & server data)
  await ConfigManager.init();

  // 2. Initialize the user settings
  userSettings.init();

  // 3. Initialize i18n
  //    Dev: ?lang=es overrides language
  //    Prod: language comes from server launch params (LANGUAGE field)
  const urlParams = new URLSearchParams(window.location.search);
  const devLang = urlParams.get("lang");
  const brandName = urlParams.get("brand") || "Casino";
  const language = devLang || "en"; // In prod, replace "en" with server-provided language

  await i18n.init({
    language,
    fallbackLanguage: "en",
    localesPath: "./locales",
    namespaces: ["common", "paytable", "rules"],
    brandOverrides: {
      BRAND_NAME: brandName,
    },
  });

  // 4. Show screens
  await engine.navigation.showScreen(LoadScreen);
  await engine.navigation.showScreen(MainScreen);
})();

