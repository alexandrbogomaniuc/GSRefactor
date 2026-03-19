import { Assets, Spritesheet, Texture, type SpritesheetData } from "pixi.js";

type ParsedFrame = {
  frame: { x: number; y: number; w: number; h: number };
  rotated: boolean;
  trimmed: boolean;
  spriteSourceSize: { x: number; y: number; w: number; h: number };
  sourceSize: { w: number; h: number };
};

type ParsedAtlas = {
  image: string;
  size: { w: number; h: number } | null;
  frames: Record<string, ParsedFrame>;
};

const atlasTextureCache = new Map<string, Promise<Record<string, Texture>>>();
const rasterizedAtlasTextureCache = new Map<string, Promise<Texture | null>>();

const parseTuple = (value: string, expectedLength: number): number[] => {
  const parts = value
    .split(",")
    .map((segment) => Number.parseInt(segment.trim(), 10))
    .filter((entry) => Number.isFinite(entry));
  if (parts.length < expectedLength) {
    return [];
  }
  return parts.slice(0, expectedLength);
};

const parseSpineAtlas = (
  atlasText: string,
  atlasOrigin: "top-left" | "bottom-left" = "bottom-left",
): ParsedAtlas => {
  const lines = atlasText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0);

  if (lines.length === 0) {
    return { image: "", size: null, frames: {} };
  }

  const image = lines[0];
  let index = 1;
  let size: { w: number; h: number } | null = null;

  while (index < lines.length && lines[index].includes(":")) {
    const [rawKey, rawValue = ""] = lines[index].split(":");
    const key = rawKey.trim().toLowerCase();
    const value = rawValue.trim();
    if (key === "size") {
      const parsed = parseTuple(value, 2);
      if (parsed.length === 2) {
        size = { w: parsed[0], h: parsed[1] };
      }
    }
    index += 1;
  }

  const frames: Record<string, ParsedFrame> = {};

  while (index < lines.length) {
    const name = lines[index];
    index += 1;
    if (!name || name.includes(":")) {
      continue;
    }

    let bounds: number[] = [];
    let offsets: number[] = [];
    let rotated = false;

    while (index < lines.length && lines[index].includes(":")) {
      const [rawKey, rawValue = ""] = lines[index].split(":");
      const key = rawKey.trim().toLowerCase();
      const value = rawValue.trim();

      if (key === "bounds") {
        bounds = parseTuple(value, 4);
      } else if (key === "offsets") {
        offsets = parseTuple(value, 4);
      } else if (key === "rotate") {
        const normalized = value.toLowerCase();
        rotated = normalized === "90" || normalized === "true";
      }

      index += 1;
    }

    if (bounds.length !== 4) {
      continue;
    }

    const [x, y, w, h] = bounds;
    const frameWidth = rotated ? h : w;
    const frameHeight = rotated ? w : h;
    const frameY =
      atlasOrigin === "bottom-left" && size ? Math.max(0, size.h - y - h) : y;
    const hasOffsets = offsets.length === 4;
    const [offsetX, offsetY, sourceW, sourceH] = hasOffsets
      ? offsets
      : [0, 0, frameWidth, frameHeight];
    frames[name] = {
      frame: { x, y: frameY, w, h },
      rotated,
      trimmed: hasOffsets,
      spriteSourceSize: { x: offsetX, y: offsetY, w: frameWidth, h: frameHeight },
      sourceSize: { w: sourceW, h: sourceH },
    };
  }

  return { image, size, frames };
};

const loadSpineAtlasTextures = async ({
  atlasUrl,
  cachePrefix,
  atlasOrigin,
}: {
  atlasUrl: string;
  cachePrefix: string;
  atlasOrigin?: "top-left" | "bottom-left";
}): Promise<Record<string, Texture>> => {
  const resolvedOrigin = atlasOrigin ?? "bottom-left";
  const cacheKey = `${atlasUrl}|${cachePrefix}|${resolvedOrigin}`;
  const cached = atlasTextureCache.get(cacheKey);
  if (cached) {
    return cached;
  }

  const loading = (async () => {
    const response = await fetch(atlasUrl, { cache: "no-store" });
    if (!response.ok) {
      return {};
    }
    const atlasText = await response.text();
    const parsed = parseSpineAtlas(atlasText, resolvedOrigin);
    if (Object.keys(parsed.frames).length === 0) {
      return {};
    }

    const imageUrl = new URL(
      parsed.image || atlasUrl.replace(/\.atlas(\?.*)?$/i, ".png$1"),
      atlasUrl,
    ).toString();
    await Assets.load(imageUrl);
    const texture = Texture.from(imageUrl);
    const spritesheetData: SpritesheetData = {
      frames: parsed.frames,
      meta: {
        image: parsed.image,
        scale: "1",
        size: parsed.size ?? {
          w: Math.round(texture.width),
          h: Math.round(texture.height),
        },
      },
      animations: {},
    };

    const spritesheet = new Spritesheet({
      texture,
      data: spritesheetData,
      cachePrefix,
    });
    return spritesheet.parseSync();
  })().catch((error) => {
    console.warn("[ProviderPack] Failed to load mapped spine atlas texture source:", error);
    atlasTextureCache.delete(cacheKey);
    return {};
  });

  atlasTextureCache.set(cacheKey, loading);
  return loading;
};

const directImagePattern = /\.(png|jpg|jpeg|webp|svg)(?:\?|$)/i;
const atlasSourcePattern = /^(.*?\.atlas)(?:#(.+))?$/i;

export const resolveMappedSourceTexture = async ({
  source,
  baseUrl,
  cachePrefix,
  atlasOrigin,
  rasterizeFrame,
}: {
  source: string | undefined;
  baseUrl: string;
  cachePrefix: string;
  atlasOrigin?: "top-left" | "bottom-left";
  rasterizeFrame?: boolean;
}): Promise<Texture | null> => {
  if (!source?.trim()) {
    return null;
  }

  const resolved = new URL(source, baseUrl).toString();
  if (directImagePattern.test(resolved)) {
    try {
      await Assets.load(resolved);
      return Texture.from(resolved);
    } catch (error) {
      console.warn(`[ProviderPack] Failed to load mapped direct texture ${resolved}:`, error);
      return null;
    }
  }

  const atlasMatch = resolved.match(atlasSourcePattern);
  if (!atlasMatch?.[1] || !atlasMatch[2]) {
    return null;
  }

  const atlasUrl = atlasMatch[1];
  const frameKey = atlasMatch[2];
  if (rasterizeFrame) {
    const resolvedOrigin = atlasOrigin ?? "bottom-left";
    const cacheKey = `${cachePrefix}:${atlasUrl}:${frameKey}:raster:${resolvedOrigin}`;
    const cached = rasterizedAtlasTextureCache.get(cacheKey);
    if (cached) {
      return cached;
    }

    const loading = (async () => {
      const response = await fetch(atlasUrl, { cache: "no-store" });
      if (!response.ok) {
        return null;
      }
      const atlasText = await response.text();
      const parsed = parseSpineAtlas(atlasText, resolvedOrigin);
      const frame = parsed.frames[frameKey];
      if (!frame) {
        return null;
      }
      const imageUrl = new URL(
        parsed.image || atlasUrl.replace(/\.atlas(\?.*)?$/i, ".png$1"),
        atlasUrl,
      ).toString();
      await Assets.load(imageUrl);
      const baseTexture = Texture.from(imageUrl);
      const sourceImage = baseTexture.source.resource as CanvasImageSource | undefined;
      if (!sourceImage) {
        return null;
      }
      const canvas = document.createElement("canvas");
      canvas.width = frame.sourceSize.w;
      canvas.height = frame.sourceSize.h;
      const ctx = canvas.getContext("2d");
      if (!ctx) {
        return null;
      }
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      if (!frame.rotated) {
        ctx.drawImage(
          sourceImage,
          frame.frame.x,
          frame.frame.y,
          frame.frame.w,
          frame.frame.h,
          frame.spriteSourceSize.x,
          frame.spriteSourceSize.y,
          frame.frame.w,
          frame.frame.h,
        );
      } else {
        // Spine atlas 90-degree packed regions must be unrotated when rasterized,
        // otherwise frames appear cropped/misaligned in runtime particle flights.
        ctx.save();
        ctx.translate(
          frame.spriteSourceSize.x,
          frame.spriteSourceSize.y + frame.frame.w,
        );
        ctx.rotate(-Math.PI / 2);
        ctx.drawImage(
          sourceImage,
          frame.frame.x,
          frame.frame.y,
          frame.frame.w,
          frame.frame.h,
          0,
          0,
          frame.frame.w,
          frame.frame.h,
        );
        ctx.restore();
      }
      return Texture.from(canvas);
    })().catch((error) => {
      console.warn("[ProviderPack] Failed to rasterize mapped atlas texture source:", error);
      rasterizedAtlasTextureCache.delete(cacheKey);
      return null;
    });

    rasterizedAtlasTextureCache.set(cacheKey, loading);
    return loading;
  }

  const textures = await loadSpineAtlasTextures({
    atlasUrl,
    cachePrefix: `${cachePrefix}:${atlasUrl}`,
    atlasOrigin,
  });
  return textures[frameKey] ?? null;
};
