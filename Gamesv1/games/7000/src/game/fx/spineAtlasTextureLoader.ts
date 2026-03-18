import { Assets, Rectangle, Texture } from "pixi.js";

type AtlasFrame = {
  x: number;
  y: number;
  width: number;
  height: number;
};

const parseAtlasFrames = (atlasText: string): Map<string, AtlasFrame> => {
  const lines = atlasText.split(/\r?\n/);
  const frames = new Map<string, AtlasFrame>();

  let currentName: string | null = null;
  let currentX = 0;
  let currentY = 0;
  let currentW = 0;
  let currentH = 0;

  const commitCurrent = (): void => {
    if (!currentName || currentW <= 0 || currentH <= 0) {
      return;
    }
    frames.set(currentName, { x: currentX, y: currentY, width: currentW, height: currentH });
  };

  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line) {
      continue;
    }
    if (!line.includes(":") && !line.endsWith(".png")) {
      commitCurrent();
      currentName = line;
      currentX = 0;
      currentY = 0;
      currentW = 0;
      currentH = 0;
      continue;
    }
    if (line.startsWith("xy:")) {
      const [x, y] = line
        .replace("xy:", "")
        .split(",")
        .map((v) => Number(v.trim()));
      currentX = Number.isFinite(x) ? x : 0;
      currentY = Number.isFinite(y) ? y : 0;
      continue;
    }
    if (line.startsWith("size:")) {
      const [w, h] = line
        .replace("size:", "")
        .split(",")
        .map((v) => Number(v.trim()));
      currentW = Number.isFinite(w) ? w : 0;
      currentH = Number.isFinite(h) ? h : 0;
      continue;
    }
  }

  commitCurrent();
  return frames;
};

export const loadSpineAtlasTextures = async (
  atlasUrl: string,
  imageUrl: string,
): Promise<Map<string, Texture>> => {
  const response = await fetch(atlasUrl, { cache: "no-store" });
  if (!response.ok) {
    throw new Error(`Failed to load atlas text: ${response.status} ${response.statusText}`);
  }
  const atlasText = await response.text();
  await Assets.load(imageUrl);
  const base = Texture.from(imageUrl).source;
  if (!base) {
    throw new Error(`Atlas image resource unavailable for ${imageUrl}`);
  }
  const parsed = parseAtlasFrames(atlasText);
  const textures = new Map<string, Texture>();
  for (const [name, frame] of parsed) {
    textures.set(
      name,
      new Texture({
        source: base,
        frame: new Rectangle(frame.x, frame.y, frame.width, frame.height),
      }),
    );
  }
  return textures;
};
