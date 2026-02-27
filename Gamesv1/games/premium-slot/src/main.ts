import { LoadScreen } from "./app/screens/LoadScreen";
import { MainScreen } from "./app/screens/main/MainScreen";
import { userSettings } from "./app/utils/userSettings";
import { ConfigManager } from "./app/utils/ConfigManager";
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

  await ConfigManager.init();
  userSettings.init();

  const urlParams = new URLSearchParams(window.location.search);
  const devLang = urlParams.get("lang");
  const brandName = urlParams.get("brand") || "Casino";
  const language = devLang || "en";

  await i18n.init({
    language,
    fallbackLanguage: "en",
    localesPath: "./locales",
    namespaces: ["common", "paytable", "rules"],
    brandOverrides: {
      BRAND_NAME: brandName,
    },
  });

  await appEngine.navigation.showScreen(LoadScreen);
  await appEngine.navigation.showScreen(MainScreen);
})();
