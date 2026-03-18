import sharedWordmarkUrl from "../../../raw-assets/preload{m}/logo.svg?url";
import { Assets, Spritesheet, Texture, type SpritesheetData } from "pixi.js";

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
import nanobananaVfxAtlasUrl from "../../../assets/providers/nanobanana/runtime/atlas_vfx.png?url";
import nanobananaVfxAtlasData from "../../../assets/providers/nanobanana/runtime/atlas_vfx.json";
import nanobananaHeroUiAtlasUrl from "../../../assets/providers/nanobanana/runtime/atlas_hero_ui.png?url";
import nanobananaHeroUiAtlasData from "../../../assets/providers/nanobanana/runtime/atlas_hero_ui.json";
import nanobananaHeroVfxAtlasUrl from "../../../assets/providers/nanobanana/runtime/atlas_hero_vfx.png?url";
import nanobananaHeroVfxAtlasData from "../../../assets/providers/nanobanana/runtime/atlas_hero_vfx.json";

import {
  CRAZY_ROOSTER_BRAND,
  resolveExplicitAssetProvider,
  type CrazyRoosterAssetProvider,
} from "../../game/config/CrazyRoosterGameConfig";

declare const __DONORLOCAL_MANIFEST_FS_PATH__: string;

type CommittedProvider = Exclude<CrazyRoosterAssetProvider, "donorlocal">;
type AtlasFrames = Record<string, unknown>;
type AtlasData = {
  frames?: AtlasFrames;
  meta?: unknown;
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
  heroUiAtlas?: ProviderAtlas;
  heroVfxAtlas?: ProviderAtlas;
  manifestUrl?: string;
};

type ProviderManifestAtlas = {
  imageUrl?: string;
  dataUrl?: string;
  data?: AtlasData;
};

type ProviderManifest = {
  wordmarkUrl?: string;
  backgrounds?: ProviderBackgrounds;
  symbolAtlas?: ProviderManifestAtlas;
  uiAtlas?: ProviderManifestAtlas;
  vfxAtlas?: ProviderManifestAtlas;
};

export type ProviderPackStatus = {
  requestedProvider: CrazyRoosterAssetProvider;
  effectiveProvider: CrazyRoosterAssetProvider;
  safePlaceholder: boolean;
  missingKeys: string[];
  wordmarkUrl: string;
  backgrounds: ProviderBackgrounds;
  manifestUrl?: string;
  fallbackReason?: string;
};

export type ProviderAtlasKind =
  | "symbolAtlas"
  | "uiAtlas"
  | "vfxAtlas"
  | "heroUiAtlas"
  | "heroVfxAtlas";

export type ProviderResolvedTexture = {
  texture: Texture | null;
  resolvedProvider: CrazyRoosterAssetProvider | null;
  fallbackUsed: boolean;
};

const DONORLOCAL_MANIFEST_URL = `/@fs${__DONORLOCAL_MANIFEST_FS_PATH__}`;

const hasFrame = (atlas: AtlasData | undefined, frameKey: string): boolean =>
  Boolean(atlas?.frames && typeof atlas.frames === "object" && frameKey in atlas.frames);

const isSpritesheetAtlasData = (atlas: AtlasData | undefined): atlas is AtlasData & {
  meta: Record<string, unknown>;
} => Boolean(atlas?.meta && typeof atlas.meta === "object");

const normalizeKeyMappedSourceUrl = (
  source: string | undefined,
  atlasUrl: string,
): string | undefined => {
  if (!source?.trim()) {
    return undefined;
  }

  const resolved = new URL(source, atlasUrl).toString();
  const sourceLower = source.toLowerCase();
  if (/\.(png|jpg|jpeg|webp|svg)(\?|$)/.test(sourceLower)) {
    return resolved;
  }

  return undefined;
};

const loadKeyMappedTextures = async (
  atlas: ProviderAtlas,
): Promise<Record<string, Texture>> => {
  const textures: Record<string, Texture> = {};
  const frames = atlas.data?.frames;
  if (!atlas.url || !frames || typeof frames !== "object") {
    return textures;
  }

  for (const [frameKey, frameValue] of Object.entries(frames)) {
    if (!frameValue || typeof frameValue !== "object") {
      continue;
    }

    const sourceUrl = normalizeKeyMappedSourceUrl(
      (frameValue as { source?: string }).source,
      atlas.url,
    );
    if (!sourceUrl) {
      continue;
    }

    try {
      await Assets.load(sourceUrl);
      textures[frameKey] = Texture.from(sourceUrl);
    } catch (error) {
      console.warn(
        `[ProviderPack] Failed to load mapped texture for ${frameKey} from ${sourceUrl}:`,
        error,
      );
    }
  }

  return textures;
};

const PROVIDER_PACKS: Record<CommittedProvider, ProviderPackDefinition> = {
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
    vfxAtlas: {
      url: nanobananaVfxAtlasUrl,
      data: nanobananaVfxAtlasData as AtlasData,
    },
    heroUiAtlas: {
      url: nanobananaHeroUiAtlasUrl,
      data: nanobananaHeroUiAtlasData as AtlasData,
    },
    heroVfxAtlas: {
      url: nanobananaHeroVfxAtlasUrl,
      data: nanobananaHeroVfxAtlasData as AtlasData,
    },
  },
};

let cachedProviderPackStatus: ProviderPackStatus | null = null;
let cachedDonorLocalPack: ProviderPackDefinition | null = null;
const atlasTextureCache = new Map<string, Promise<Record<string, Texture>>>();

const getFallbackPack = (): ProviderPackDefinition => PROVIDER_PACKS.openai;

const getPackForProvider = (
  provider: CrazyRoosterAssetProvider,
): ProviderPackDefinition | undefined => {
  if (provider === "donorlocal") {
    return cachedDonorLocalPack ?? undefined;
  }

  return PROVIDER_PACKS[provider];
};

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

const buildStatus = (
  requestedProvider: CrazyRoosterAssetProvider,
  effectiveProvider: CrazyRoosterAssetProvider,
  pack: ProviderPackDefinition,
  missingKeys: string[],
  fallbackReason?: string,
  safePlaceholder = missingKeys.length > 0,
): ProviderPackStatus => ({
  requestedProvider,
  effectiveProvider,
  safePlaceholder,
  missingKeys,
  wordmarkUrl: pack.wordmarkUrl,
  backgrounds: pack.backgrounds,
  manifestUrl: pack.manifestUrl,
  fallbackReason,
});

const logStatus = (status: ProviderPackStatus): void => {
  if (status.fallbackReason) {
    console.warn(
      `[ProviderPack] Requested "${status.requestedProvider}" fell back to "${status.effectiveProvider}": ${status.fallbackReason}`,
    );
  }

  if (status.safePlaceholder && status.missingKeys.length > 0) {
    console.error(
      `[ProviderPack] Provider "${status.effectiveProvider}" is missing required assets/keys: ${status.missingKeys.join(
        ", ",
      )}. Falling back to safe placeholder rendering for unavailable art.`,
    );
    return;
  }

  console.info(
    `[ProviderPack] Provider "${status.effectiveProvider}" validated${
      status.requestedProvider !== status.effectiveProvider
        ? ` after request for "${status.requestedProvider}"`
        : ""
    }.`,
  );
};

const buildDonorLocalFallbackReason = (
  fallbackReason: string | undefined,
  usedDevBenchmarkDefault: boolean,
): string => {
  const normalized =
    fallbackReason ??
    "local donor manifest is incomplete and did not satisfy the provider contract.";

  if (!usedDevBenchmarkDefault) {
    return normalized;
  }

  return `DEV benchmark mode defaulted to donorlocal because no explicit provider was set, but ${normalized}`;
};

const resolveRelativeUrl = (value: string | undefined, baseUrl: string): string | undefined => {
  if (!value?.trim()) {
    return undefined;
  }
  return new URL(value, baseUrl).toString();
};

const readRemoteAtlasData = async (
  dataUrl: string | undefined,
  label: string,
): Promise<AtlasData | undefined> => {
  if (!dataUrl) {
    return undefined;
  }

  try {
    const response = await fetch(dataUrl, { cache: "no-store" });
    if (!response.ok) {
      console.warn(`[ProviderPack] Failed to load ${label} atlas JSON: ${response.status}`);
      return undefined;
    }

    return (await response.json()) as AtlasData;
  } catch (error) {
    console.warn(`[ProviderPack] Failed to parse ${label} atlas JSON:`, error);
    return undefined;
  }
};

const normalizeManifestAtlas = async (
  manifestAtlas: ProviderManifestAtlas | undefined,
  manifestUrl: string,
  label: string,
): Promise<ProviderAtlas | undefined> => {
  if (!manifestAtlas) {
    return undefined;
  }

  const url = resolveRelativeUrl(manifestAtlas.imageUrl, manifestUrl);
  const data =
    manifestAtlas.data ??
    (await readRemoteAtlasData(resolveRelativeUrl(manifestAtlas.dataUrl, manifestUrl), label));

  return { url, data };
};

const loadDonorLocalPack = async (): Promise<{
  pack?: ProviderPackDefinition;
  fallbackReason?: string;
  missingKeys: string[];
}> => {
  if (!import.meta.env.DEV) {
    return {
      missingKeys: ["manifest.file"],
      fallbackReason: "donorlocal is DEV-only and is disabled in non-dev builds.",
    };
  }

  try {
    const response = await fetch(DONORLOCAL_MANIFEST_URL, { cache: "no-store" });
    if (!response.ok) {
      return {
        missingKeys: ["manifest.file"],
        fallbackReason:
          `local donor manifest was not found at ${__DONORLOCAL_MANIFEST_FS_PATH__}.`,
      };
    }

    const contentType = response.headers.get("content-type") ?? "";
    if (!contentType.includes("json")) {
      return {
        missingKeys: ["manifest.file"],
        fallbackReason:
          `local donor manifest at ${__DONORLOCAL_MANIFEST_FS_PATH__} did not return JSON.`,
      };
    }

    const manifest = (await response.json()) as ProviderManifest;
    const manifestUrl = response.url || DONORLOCAL_MANIFEST_URL;
    const pack: ProviderPackDefinition = {
      provider: "donorlocal",
      wordmarkUrl:
        resolveRelativeUrl(manifest.wordmarkUrl, manifestUrl) ?? sharedWordmarkUrl,
      backgrounds: {
        desktop: resolveRelativeUrl(manifest.backgrounds?.desktop, manifestUrl),
        landscape: resolveRelativeUrl(manifest.backgrounds?.landscape, manifestUrl),
        portrait: resolveRelativeUrl(manifest.backgrounds?.portrait, manifestUrl),
      },
      symbolAtlas: await normalizeManifestAtlas(
        manifest.symbolAtlas,
        manifestUrl,
        "donorlocal.symbolAtlas",
      ),
      uiAtlas: await normalizeManifestAtlas(
        manifest.uiAtlas,
        manifestUrl,
        "donorlocal.uiAtlas",
      ),
      vfxAtlas: await normalizeManifestAtlas(
        manifest.vfxAtlas,
        manifestUrl,
        "donorlocal.vfxAtlas",
      ),
      manifestUrl,
    };

    return {
      pack,
      missingKeys: validatePack(pack),
    };
  } catch (error) {
    return {
      missingKeys: ["manifest.file"],
      fallbackReason:
        `local donor manifest could not be loaded from ${__DONORLOCAL_MANIFEST_FS_PATH__}: ${String(
          error,
        )}`,
    };
  }
};

export const initializeProviderPackStatus = async (
  queryParams = new URLSearchParams(window.location.search),
): Promise<ProviderPackStatus> => {
  const explicitProvider = resolveExplicitAssetProvider(queryParams);
  const usedDevBenchmarkDefault = import.meta.env.DEV && !explicitProvider;
  const requestedProvider =
    explicitProvider ??
    (usedDevBenchmarkDefault ? "donorlocal" : CRAZY_ROOSTER_BRAND.defaultProvider);

  let status: ProviderPackStatus;
  if (requestedProvider === "donorlocal") {
    const donorLocalPack = await loadDonorLocalPack();
    if (!donorLocalPack.pack || donorLocalPack.missingKeys.length > 0) {
      cachedDonorLocalPack = donorLocalPack.pack ?? null;
      const fallbackPack = getFallbackPack();
      const fallbackMissingKeys = validatePack(fallbackPack);
      status = buildStatus(
        requestedProvider,
        "openai",
        fallbackPack,
        donorLocalPack.missingKeys,
        buildDonorLocalFallbackReason(
          donorLocalPack.fallbackReason,
          usedDevBenchmarkDefault,
        ),
        fallbackMissingKeys.length > 0,
      );
      status.manifestUrl = donorLocalPack.pack?.manifestUrl ?? DONORLOCAL_MANIFEST_URL;
    } else {
      status = buildStatus(
        requestedProvider,
        requestedProvider,
        donorLocalPack.pack,
        donorLocalPack.missingKeys,
      );
      cachedDonorLocalPack = donorLocalPack.pack;
    }
  } else {
    cachedDonorLocalPack = null;
    const pack = PROVIDER_PACKS[requestedProvider];
    status = buildStatus(
      requestedProvider,
      requestedProvider,
      pack,
      validatePack(pack),
    );
  }

  logStatus(status);
  cachedProviderPackStatus = status;
  return status;
};

export const getProviderPackStatus = (): ProviderPackStatus =>
  cachedProviderPackStatus ??
  buildStatus(
    CRAZY_ROOSTER_BRAND.defaultProvider,
    CRAZY_ROOSTER_BRAND.defaultProvider,
    getFallbackPack(),
    validatePack(getFallbackPack()),
  );

export const resolveProviderWordmarkUrl = (): string => getProviderPackStatus().wordmarkUrl;

export const resolveProviderBackgroundUrl = (
  width: number,
  height: number,
): string | undefined => {
  const backgrounds = getProviderPackStatus().backgrounds;
  const aspectRatio = width / Math.max(1, height);

  if (aspectRatio < 0.8) {
    return backgrounds.portrait ?? backgrounds.desktop;
  }

  if (aspectRatio > 1.35) {
    return backgrounds.landscape ?? backgrounds.desktop;
  }

  return backgrounds.desktop ?? backgrounds.landscape ?? backgrounds.portrait;
};

export const getDonorLocalManifestUrl = (): string => DONORLOCAL_MANIFEST_URL;

const loadAtlasTextures = async (
  provider: CrazyRoosterAssetProvider,
  atlasKind: ProviderAtlasKind,
): Promise<Record<string, Texture>> => {
  const pack = getPackForProvider(provider);
  const atlas = pack?.[atlasKind];

  if (!atlas?.url || !atlas.data) {
    return {};
  }

  const cacheKey = `${provider}:${atlasKind}:${atlas.url}`;
  const cached = atlasTextureCache.get(cacheKey);
  if (cached) {
    return cached;
  }

  const loading = (async () => {
    if (!isSpritesheetAtlasData(atlas.data)) {
      return loadKeyMappedTextures(atlas);
    }

    await Assets.load(atlas.url!);
    const spritesheet = new Spritesheet({
      texture: Texture.from(atlas.url!),
      data: atlas.data as SpritesheetData,
      cachePrefix: `${provider}:${atlasKind}:`,
    });

    return spritesheet.parseSync();
  })().catch((error) => {
    console.warn(
      `[ProviderPack] Failed to build spritesheet for ${provider}.${atlasKind}:`,
      error,
    );
    atlasTextureCache.delete(cacheKey);
    return {};
  });

  atlasTextureCache.set(cacheKey, loading);
  return loading;
};

export const resolveProviderFrameTexture = async (
  atlasKind: ProviderAtlasKind,
  frameKey: string,
  preferredProvider = getProviderPackStatus().effectiveProvider,
): Promise<ProviderResolvedTexture> => {
  const providerChain = Array.from(
    new Set<CrazyRoosterAssetProvider>([preferredProvider, "openai"]),
  );

  for (const provider of providerChain) {
    const textures = await loadAtlasTextures(provider, atlasKind);
    const texture = textures[frameKey];
    if (texture) {
      return {
        texture,
        resolvedProvider: provider,
        fallbackUsed: provider !== preferredProvider,
      };
    }
  }

  return {
    texture: null,
    resolvedProvider: null,
    fallbackUsed: preferredProvider !== "openai",
  };
};

export const getProviderDebugLine = (): string => {
  const status = getProviderPackStatus();
  return `provider req=${status.requestedProvider} eff=${status.effectiveProvider} safe=${status.safePlaceholder ? 1 : 0} missing=${status.missingKeys.length}`;
};
