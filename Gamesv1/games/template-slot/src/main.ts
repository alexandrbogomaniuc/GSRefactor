import { LoadScreen } from "./app/screens/LoadScreen";
import { MainScreen } from "./app/screens/main/MainScreen";
import { userSettings } from "./app/utils/userSettings";
import { ConfigManager } from "./app/utils/ConfigManager";
import { setEngine, CreationEngine } from "@gs/slot-shell";

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

  // 3. Show screens
  await engine.navigation.showScreen(LoadScreen);
  await engine.navigation.showScreen(MainScreen);
})();
