import { sound } from "@pixi/sound";
import type {
  ApplicationOptions,
  DestroyOptions,
  RendererDestroyOptions,
} from "pixi.js";
import { Application, Assets, extensions, ResizePlugin } from "pixi.js";
import "pixi.js/app";

import { CreationAudioPlugin } from "./audio/AudioPlugin";
import { CreationNavigationPlugin } from "./navigation/NavigationPlugin";
import { CreationResizePlugin } from "./resize/ResizePlugin";
import { getResolution } from "./utils/getResolution";

extensions.remove(ResizePlugin);
extensions.add(CreationResizePlugin);
extensions.add(CreationAudioPlugin);
extensions.add(CreationNavigationPlugin);

export interface EngineInitOptions extends Partial<ApplicationOptions> {
  assetManifest?: unknown;
  assetBasePath?: string;
  preloadBundle?: string;
}

export class CreationEngine extends Application {
  public async init(opts: EngineInitOptions): Promise<void> {
    const {
      assetManifest,
      assetBasePath = "assets",
      preloadBundle = "preload",
      ...applicationOptions
    } = opts;

    applicationOptions.resizeTo ??= window;
    applicationOptions.resolution ??= getResolution();

    await super.init(applicationOptions);

    document.getElementById("pixi-container")!.appendChild(this.canvas);
    document.addEventListener("visibilitychange", this.visibilityChange);

    if (assetManifest) {
      await Assets.init({ manifest: assetManifest as never, basePath: assetBasePath });

      if (preloadBundle) {
        await Assets.loadBundle(preloadBundle);
      }

      const allBundles = (assetManifest as { bundles?: { name: string }[] }).bundles?.map(
        (item) => item.name,
      );

      if (allBundles?.length) {
        Assets.backgroundLoadBundle(allBundles);
      }
    }
  }

  public override destroy(
    rendererDestroyOptions: RendererDestroyOptions = false,
    options: DestroyOptions = false,
  ): void {
    document.removeEventListener("visibilitychange", this.visibilityChange);
    super.destroy(rendererDestroyOptions, options);
  }

  protected visibilityChange = () => {
    if (document.hidden) {
      sound.pauseAll();
      this.navigation.blur();
    } else {
      sound.resumeAll();
      this.navigation.focus();
    }
  };
}
