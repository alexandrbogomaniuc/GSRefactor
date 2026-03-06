import type { BGM, SFX } from "./audio/audio";
import type { ResponsiveLayoutManager } from "./layout/ResponsiveLayoutManager";
import type { Navigation } from "./navigation/navigation";
import type {
  CreationResizePluginOptions,
  DeepRequired,
} from "./resize/ResizePlugin";

declare global {
  namespace PixiMixins {
    interface Application extends DeepRequired<CreationResizePluginOptions> {
      audio: {
        bgm: BGM;
        sfx: SFX;
        getMasterVolume: () => number;
        setMasterVolume: (volume: number) => void;
      };
      navigation: Navigation;
      layout: ResponsiveLayoutManager;
    }

    interface ApplicationOptions extends CreationResizePluginOptions {}
  }
}

export {};
