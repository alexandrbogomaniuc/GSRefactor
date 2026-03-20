import sharedWordmarkUrl from "../../../raw-assets/preload{m}/logo.svg?url";
import { Assets, Spritesheet, Texture, type SpritesheetData } from "pixi.js";

import {
  CRAZY_ROOSTER_BRAND,
  resolveExplicitAssetProvider,
  type CrazyRoosterAssetProvider,
} from "../../game/config/CrazyRoosterGameConfig";
import { resolveMappedSourceTexture } from "./mappedSourceTextureResolver";

declare const __DONORLOCAL_MANIFEST_FS_PATH__: string;

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

const pickMappedSource = (source: unknown): string | undefined => {
  if (typeof source === "string" && source.trim()) {
    return source;
  }
  if (Array.isArray(source)) {
    const first = source.find((entry) => typeof entry === "string" && entry.trim());
    return typeof first === "string" ? first : undefined;
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

    const source = pickMappedSource(
      (frameValue as { source?: string | string[] }).source,
    );
    if (!source) {
      continue;
    }

    const texture = await resolveMappedSourceTexture({
      source,
      baseUrl: atlas.url,
      cachePrefix: `provider-pack:${atlas.url}:${frameKey}`,
      atlasOrigin: "top-left",
      rasterizeFrame: true,
    });
    if (texture) {
      textures[frameKey] = texture;
    } else {
      console.warn(
        `[ProviderPack] Failed to load mapped texture for ${frameKey} from ${source}.`,
      );
    }
  }

  return textures;
};

let cachedProviderPackStatus: ProviderPackStatus | null = null;
let cachedDonorLocalPack: ProviderPackDefinition | null = null;
const atlasTextureCache = new Map<string, Promise<Record<string, Texture>>>();

const getPackForProvider = (
  provider: CrazyRoosterAssetProvider,
): ProviderPackDefinition | undefined => {
  if (provider === "donorlocal") {
    return cachedDonorLocalPack ?? undefined;
  }
  return undefined;
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
  const donorLocalStrictMode = queryParams.get("donorlocalStrict") !== "0";
  const explicitProvider = resolveExplicitAssetProvider(queryParams);
  const requestedProvider = explicitProvider ?? CRAZY_ROOSTER_BRAND.defaultProvider;
  const donorLocalPack = await loadDonorLocalPack();
  if (!donorLocalPack.pack || donorLocalPack.missingKeys.length > 0) {
    cachedDonorLocalPack = donorLocalPack.pack ?? null;
    const donorLocalFallbackReason = buildDonorLocalFallbackReason(
      donorLocalPack.fallbackReason,
      false,
    );
    const detail = donorLocalPack.missingKeys.length
      ? `missing keys: ${donorLocalPack.missingKeys.join(", ")}`
      : "missing keys: none reported";
    if (donorLocalStrictMode) {
      throw new Error(
        `[7000] donorlocal requested but unavailable. ${donorLocalFallbackReason} (${detail})`,
      );
    }
    throw new Error(`[7000] donorlocal-only mode: ${donorLocalFallbackReason} (${detail})`);
  }
  const status = buildStatus(
    requestedProvider,
    "donorlocal",
    donorLocalPack.pack,
    donorLocalPack.missingKeys,
  );
  cachedDonorLocalPack = donorLocalPack.pack;

  logStatus(status);
  cachedProviderPackStatus = status;
  return status;
};

export const getProviderPackStatus = (): ProviderPackStatus =>
  cachedProviderPackStatus ??
  buildStatus(
    "donorlocal",
    "donorlocal",
    {
      provider: "donorlocal",
      wordmarkUrl: sharedWordmarkUrl,
      backgrounds: {},
      manifestUrl: DONORLOCAL_MANIFEST_URL,
    },
    ["manifest.file"],
    "donorlocal pack is not initialized yet.",
    true,
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
  const textures = await loadAtlasTextures(preferredProvider, atlasKind);
  const texture = textures[frameKey];
  if (texture) {
    return {
      texture,
      resolvedProvider: preferredProvider,
      fallbackUsed: false,
    };
  }

  return {
    texture: null,
    resolvedProvider: null,
    fallbackUsed: false,
  };
};

export const getProviderDebugLine = (): string => {
  const status = getProviderPackStatus();
  return `provider req=${status.requestedProvider} eff=${status.effectiveProvider} safe=${status.safePlaceholder ? 1 : 0} missing=${status.missingKeys.length}`;
};
