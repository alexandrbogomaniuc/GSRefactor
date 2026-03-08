import sharedWordmarkUrl from "../../../raw-assets/preload{m}/logo.svg?url";

import openaiBackgroundDesktopUrl from "../../../assets/providers/openai/runtime/background-desktop-1920x1080.png?url";
import openaiBackgroundLandscapeUrl from "../../../assets/providers/openai/runtime/background-landscape-safe-1600x900.png?url";
import openaiBackgroundPortraitUrl from "../../../assets/providers/openai/runtime/background-portrait-1080x1920.png?url";
import openaiSymbolsAtlasUrl from "../../../assets/providers/openai/runtime/atlas_symbols.png?url";
import openaiSymbolsAtlasData from "../../../assets/providers/openai/runtime/atlas_symbols.json";
import openaiUiAtlasUrl from "../../../assets/providers/openai/runtime/atlas_ui.png?url";
import openaiUiAtlasData from "../../../assets/providers/openai/runtime/atlas_ui.json";
import openaiVfxAtlasUrl from "../../../assets/providers/openai/runtime/atlas_vfx.png?url";
import openaiVfxAtlasData from "../../../assets/providers/openai/runtime/atlas_vfx.json";

import nanobananaBackgroundDesktopUrl from "../../../assets/providers/nanobanana/runtime/background-desktop-1920x1080.png?url";
import nanobananaBackgroundLandscapeUrl from "../../../assets/providers/nanobanana/runtime/background-landscape-safe-1600x900.png?url";
import nanobananaBackgroundPortraitUrl from "../../../assets/providers/nanobanana/runtime/background-portrait-1080x1920.png?url";
import nanobananaSymbolsAtlasUrl from "../../../assets/providers/nanobanana/runtime/atlas_symbols.png?url";
import nanobananaSymbolsAtlasData from "../../../assets/providers/nanobanana/runtime/atlas_symbols.json";
import nanobananaUiAtlasUrl from "../../../assets/providers/nanobanana/runtime/atlas_ui.png?url";
import nanobananaUiAtlasData from "../../../assets/providers/nanobanana/runtime/atlas_ui.json";

import {
  resolveAssetProvider,
  type CrazyRoosterAssetProvider,
} from "../../game/config/CrazyRoosterGameConfig";

type AtlasFrames = Record<string, unknown>;
type AtlasData = {
  frames?: AtlasFrames;
};

type ProviderAtlas = {
  url?: string;
  data?: AtlasData;
};

type ProviderBackgrounds = {
  desktop?: string;
  landscape?: string;
  portrait?: string;
};

type ProviderPackDefinition = {
  provider: CrazyRoosterAssetProvider;
  wordmarkUrl: string;
  backgrounds: ProviderBackgrounds;
  symbolAtlas?: ProviderAtlas;
  uiAtlas?: ProviderAtlas;
  vfxAtlas?: ProviderAtlas;
};

export type ProviderPackStatus = {
  requestedProvider: CrazyRoosterAssetProvider;
  safePlaceholder: boolean;
  missingKeys: string[];
  wordmarkUrl: string;
  backgrounds: ProviderBackgrounds;
};

const hasFrame = (atlas: AtlasData | undefined, frameKey: string): boolean =>
  Boolean(atlas?.frames && typeof atlas.frames === "object" && frameKey in atlas.frames);

const PROVIDER_PACKS: Record<CrazyRoosterAssetProvider, ProviderPackDefinition> = {
  openai: {
    provider: "openai",
    wordmarkUrl: sharedWordmarkUrl,
    backgrounds: {
      desktop: openaiBackgroundDesktopUrl,
      landscape: openaiBackgroundLandscapeUrl,
      portrait: openaiBackgroundPortraitUrl,
    },
    symbolAtlas: {
      url: openaiSymbolsAtlasUrl,
      data: openaiSymbolsAtlasData as AtlasData,
    },
    uiAtlas: {
      url: openaiUiAtlasUrl,
      data: openaiUiAtlasData as AtlasData,
    },
    vfxAtlas: {
      url: openaiVfxAtlasUrl,
      data: openaiVfxAtlasData as AtlasData,
    },
  },
  nanobanana: {
    provider: "nanobanana",
    wordmarkUrl: sharedWordmarkUrl,
    backgrounds: {
      desktop: nanobananaBackgroundDesktopUrl,
      landscape: nanobananaBackgroundLandscapeUrl,
      portrait: nanobananaBackgroundPortraitUrl,
    },
    symbolAtlas: {
      url: nanobananaSymbolsAtlasUrl,
      data: nanobananaSymbolsAtlasData as AtlasData,
    },
    uiAtlas: {
      url: nanobananaUiAtlasUrl,
      data: nanobananaUiAtlasData as AtlasData,
    },
  },
};

let cachedProviderPackStatus: ProviderPackStatus | null = null;

const validatePack = (pack: ProviderPackDefinition): string[] => {
  const missing: string[] = [];

  if (!pack.wordmarkUrl) {
    missing.push("logo/preloader.wordmark");
  }

  if (!pack.backgrounds.desktop) {
    missing.push("preloader.background.desktop");
  }
  if (!pack.backgrounds.landscape) {
    missing.push("preloader.background.landscape");
  }
  if (!pack.backgrounds.portrait) {
    missing.push("preloader.background.portrait");
  }

  if (!pack.uiAtlas?.url || !pack.uiAtlas.data) {
    missing.push("uiAtlas.file");
  } else if (!hasFrame(pack.uiAtlas.data, "reel-frame-panel")) {
    missing.push("uiAtlas.reel-frame-panel");
  }

  if (!pack.symbolAtlas?.url || !pack.symbolAtlas.data) {
    missing.push("symbolAtlas.file");
  } else {
    for (const frameKey of ["symbol-0-egg", "symbol-9-rooster", "collector-symbol"]) {
      if (!hasFrame(pack.symbolAtlas.data, frameKey)) {
        missing.push(`symbolAtlas.${frameKey}`);
      }
    }
  }

  if (!pack.vfxAtlas?.url || !pack.vfxAtlas.data) {
    missing.push("vfxAtlas.file");
  } else if (!hasFrame(pack.vfxAtlas.data, "lightning-arc-01")) {
    missing.push("vfxAtlas.lightning-arc-01");
  }

  return missing;
};

export const initializeProviderPackStatus = (
  queryParams = new URLSearchParams(window.location.search),
): ProviderPackStatus => {
  const requestedProvider = resolveAssetProvider(queryParams);
  const pack = PROVIDER_PACKS[requestedProvider];
  const missingKeys = validatePack(pack);

  const status: ProviderPackStatus = {
    requestedProvider,
    safePlaceholder: missingKeys.length > 0,
    missingKeys,
    wordmarkUrl: pack.wordmarkUrl,
    backgrounds: pack.backgrounds,
  };

  if (missingKeys.length > 0) {
    console.error(
      `[ProviderPack] Provider "${requestedProvider}" is missing required assets/keys: ${missingKeys.join(
        ", ",
      )}. Falling back to safe placeholder rendering for unavailable art.`,
    );
  } else {
    console.info(`[ProviderPack] Provider "${requestedProvider}" validated for QA beta.`);
  }

  cachedProviderPackStatus = status;
  return status;
};

export const getProviderPackStatus = (): ProviderPackStatus =>
  cachedProviderPackStatus ?? initializeProviderPackStatus();

export const resolveProviderWordmarkUrl = (
  queryParams = new URLSearchParams(window.location.search),
): string =>
  cachedProviderPackStatus?.wordmarkUrl ?? initializeProviderPackStatus(queryParams).wordmarkUrl;

export const resolveProviderBackgroundUrl = (
  width: number,
  height: number,
  queryParams = new URLSearchParams(window.location.search),
): string | undefined => {
  const backgrounds =
    cachedProviderPackStatus?.backgrounds ?? initializeProviderPackStatus(queryParams).backgrounds;
  const aspectRatio = width / Math.max(1, height);

  if (aspectRatio < 0.8) {
    return backgrounds.portrait ?? backgrounds.desktop;
  }

  if (aspectRatio > 1.35) {
    return backgrounds.landscape ?? backgrounds.desktop;
  }

  return backgrounds.desktop ?? backgrounds.landscape ?? backgrounds.portrait;
};
