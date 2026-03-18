import { Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import { CRAZY_ROOSTER_JACKPOTS } from "../config/CrazyRoosterGameConfig";
import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
} from "../../app/assets/providerPackRegistry";

type JackpotLevel = "mini" | "minor" | "major" | "grand";
type AtlasFrame = {
  x: number;
  y: number;
  width: number;
  height: number;
  offsetX: number;
  offsetY: number;
  originalWidth: number;
  originalHeight: number;
  rotated: boolean;
};

const PLAQUES: Array<{
  level: JackpotLevel;
  value: number;
}> = [
  { level: "grand", value: CRAZY_ROOSTER_JACKPOTS.grandMultiplier },
  { level: "major", value: CRAZY_ROOSTER_JACKPOTS.majorMultiplier },
  { level: "minor", value: CRAZY_ROOSTER_JACKPOTS.minorMultiplier },
  { level: "mini", value: CRAZY_ROOSTER_JACKPOTS.miniMultiplier },
];

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

export class JackpotPlaqueController extends Container {
  private static donorTitleTexturesPromise: Promise<Map<JackpotLevel, Texture | null>> | null = null;
  private static donorTitleTextures: Map<JackpotLevel, Texture | null> | null = null;

  private readonly plates = PLAQUES.map((entry) => ({
    ...entry,
    plaque: new Graphics(),
    glow: new Graphics(),
    titleSprite: new Sprite(),
    label: new Text({
      text: entry.level.toUpperCase(),
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 17,
        fontWeight: "900",
        fill: 0xffcf43,
        stroke: { color: 0x3b0a0f, width: 5 },
        letterSpacing: 1.4,
      },
    }),
    value: new Text({
      text: `${entry.value}`,
      style: {
        fontFamily: "Arial Black, Trebuchet MS, Arial, sans-serif",
        fontSize: 32,
        fontWeight: "900",
        fill: [0xfff7b8, 0xffeb72, 0xffd139],
        stroke: { color: 0x6b2400, width: 4, join: "round" },
        dropShadow: {
          alpha: 0.45,
          angle: Math.PI / 2,
          blur: 1,
          color: 0x7a2a00,
          distance: 2,
        },
        letterSpacing: 0.2,
        padding: 6,
      },
    }),
  }));
  private readonly tickerHandler = (ticker: Ticker) => this.tick(ticker.deltaMS);

  private activeLevel: JackpotLevel | null = null;
  private time = 0;
  private machineWidth = 0;

  constructor() {
    super();
    this.plates.forEach((plate) => {
      plate.titleSprite.anchor.set(0.5);
      plate.titleSprite.visible = false;
      plate.label.anchor.set(0.5);
      plate.value.anchor.set(0.5);
      plate.value.resolution = Math.max(2, Math.ceil(window.devicePixelRatio || 1));
      this.addChild(plate.glow, plate.plaque, plate.titleSprite, plate.label, plate.value);
    });

    Ticker.shared.add(this.tickerHandler);
    this.layout();
    void this.refreshDonorTitleTextures();
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.tickerHandler);
    super.destroy(options);
  }

  public resize(machineWidth: number): void {
    this.machineWidth = machineWidth;
    this.layout();
  }

  public celebrate(level: JackpotLevel | null): void {
    this.activeLevel = level;
  }

  public clear(): void {
    this.activeLevel = null;
  }

  public getPlaqueCenter(level: "mini" | "minor" | "major" | "grand"): { x: number; y: number } {
    return this.resolvePlaqueCenter(level);
  }

  private async refreshDonorTitleTextures(): Promise<void> {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return;
    }

    const textures = await JackpotPlaqueController.loadDonorTitleTextures();
    JackpotPlaqueController.donorTitleTextures = textures;
    this.layout();
  }

  private layout(): void {
    if (this.machineWidth <= 0) {
      return;
    }

    const centerX = this.machineWidth * 0.5;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    if (
      isDonorlocal &&
      !JackpotPlaqueController.donorTitleTextures &&
      !JackpotPlaqueController.donorTitleTexturesPromise
    ) {
      void this.refreshDonorTitleTextures();
    }
    const placements: Record<JackpotLevel, { x: number; y: number }> = {
      grand: { x: centerX - (isDonorlocal ? 244 : 176), y: isDonorlocal ? -104 : -126 },
      major: { x: centerX + (isDonorlocal ? 244 : 176), y: isDonorlocal ? -104 : -126 },
      minor: { x: centerX - (isDonorlocal ? 232 : 166), y: isDonorlocal ? -34 : -54 },
      mini: { x: centerX + (isDonorlocal ? 232 : 166), y: isDonorlocal ? -34 : -54 },
    };

    this.plates.forEach((plate) => {
      const pos = placements[plate.level];
      const { bg, border, label } = this.resolvePlaquePalette(plate.level);
      const isTopPlaque = plate.level === "grand" || plate.level === "major";
      const donorTitleTexture = isDonorlocal
        ? JackpotPlaqueController.donorTitleTextures?.get(plate.level) ?? null
        : null;
      const displayValue = isDonorlocal
        ? plate.level === "grand"
          ? 300
          : plate.level === "major"
            ? 450
            : plate.level === "minor"
              ? 150
              : 75
        : plate.value;
      const { width, height } = this.resolvePlaqueMetrics(plate.level, isDonorlocal);
      const donorLabelY = pos.y - height * 0.54;
      const donorValueY = pos.y + (isTopPlaque ? 5 : 4);

      plate.plaque.clear();
      plate.plaque.roundRect(pos.x - width * 0.5, pos.y - height * 0.5, width, height, 16);
      plate.plaque.fill({ color: bg, alpha: 0.98 });
      plate.plaque.stroke({ color: 0x6c2c0f, width: 1.5, alpha: 0.55 });

      plate.plaque.roundRect(
        pos.x - width * 0.5 + 2,
        pos.y - height * 0.5 + 2,
        width - 4,
        height - 4,
        14,
      );
      plate.plaque.stroke({ color: border, width: 4, alpha: 0.98 });

      plate.plaque.roundRect(
        pos.x - width * 0.5 + 6,
        pos.y - height * 0.5 + 7,
        width - 12,
        height * 0.38,
        12,
      );
      plate.plaque.fill({ color: 0xffffff, alpha: 0.08 });

      plate.label.style.fontSize = isDonorlocal ? (isTopPlaque ? 18 : 17) : isTopPlaque ? 20 : 18;
      plate.value.style.fontSize = isDonorlocal ? (isTopPlaque ? 33 : 31) : isTopPlaque ? 34 : 31;
      plate.label.style.fill = label;
      plate.value.text = `${displayValue}`;
      plate.label.x = Math.round(pos.x);
      plate.label.y = isDonorlocal ? donorLabelY : pos.y - (isTopPlaque ? 11 : 9);
      plate.titleSprite.texture = donorTitleTexture ?? Texture.EMPTY;
      plate.titleSprite.visible = Boolean(donorTitleTexture);
      plate.titleSprite.x = Math.round(pos.x);
      plate.titleSprite.y = pos.y - height * (isTopPlaque ? 0.53 : 0.52);
      plate.titleSprite.scale.set(isTopPlaque ? 0.78 : 0.75);
      plate.value.x = Math.round(pos.x);
      plate.value.y = Math.round(isDonorlocal ? donorValueY + 2 : pos.y + (isTopPlaque ? 12 : 10));
      plate.label.visible = !plate.titleSprite.visible;
    });
  }

  private tick(deltaMs: number): void {
    this.time += deltaMs / 1000;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    this.plates.forEach((plate, index) => {
      const isActive = this.activeLevel === plate.level;
      const pulse =
        (isActive ? 0.22 : 0.08) + Math.sin(this.time * (isActive ? 7 : 2.4) + index * 0.6) * 0.06;
      const scale = isDonorlocal
        ? isActive
          ? 1.02 + pulse * 0.02
          : 1
        : isActive
          ? 1.06 + pulse * 0.05
          : 1 + pulse * 0.02;

      plate.glow.clear();
      const { width: plaqueWidth, height: plaqueHeight } = this.resolvePlaqueMetrics(
        plate.level,
        isDonorlocal,
      );
      const width = isDonorlocal ? plaqueWidth + 8 : plaqueWidth + 24;
      const height = isDonorlocal ? plaqueHeight + 8 : plaqueHeight + 14;
      const center = this.resolvePlaqueCenter(plate.level);
      if (isDonorlocal && isActive) {
        const flareWidth = width + 22 + pulse * 10;
        const flareHeight = height + 18 + pulse * 8;
        plate.glow.roundRect(
          center.x - flareWidth * 0.5,
          center.y - flareHeight * 0.5,
          flareWidth,
          flareHeight,
          22,
        );
        plate.glow.fill({
          color: 0xffce63,
          alpha: 0.09 + pulse * 0.18,
        });
        plate.glow.roundRect(
          center.x - (flareWidth + 12) * 0.5,
          center.y - (flareHeight + 10) * 0.5,
          flareWidth + 12,
          flareHeight + 10,
          24,
        );
        plate.glow.stroke({
          color: 0xfff6c5,
          width: 8,
          alpha: 0.24 + pulse * 0.2,
        });
        plate.glow.ellipse(
          center.x,
          center.y,
          flareWidth * 0.54,
          flareHeight * 0.62,
        );
        plate.glow.fill({
          color: 0xffde7a,
          alpha: 0.08 + pulse * 0.12,
        });
        plate.glow.ellipse(
          center.x,
          center.y - flareHeight * 0.26,
          flareWidth * 0.28,
          10 + pulse * 8,
        );
        plate.glow.fill({
          color: 0xfff3c1,
          alpha: 0.12 + pulse * 0.12,
        });
      }
      plate.glow.roundRect(center.x - width * 0.5, center.y - height * 0.5, width, height, 18);
      plate.glow.stroke({
        color: isActive ? 0xfff4be : 0xc7141a,
        width: isDonorlocal ? (isActive ? 6 : 2.5) : isActive ? 6 : 3.5,
        alpha: isDonorlocal ? (isActive ? 0.82 : 0.14) : isActive ? 0.72 : 0.22,
      });
      plate.glow.scale.set(scale);
      plate.glow.pivot.set(center.x, center.y);
      plate.glow.position.set(center.x, center.y);

      plate.label.alpha = isActive ? 1 : 0.9;
      plate.value.alpha = isActive ? 1 : 0.92;
      plate.titleSprite.alpha = isActive ? 1 : 0.94;
      plate.label.scale.set(isActive ? (isDonorlocal ? 1.08 + pulse * 0.03 : 1.05 + pulse * 0.02) : 1);
      plate.value.scale.set(isActive ? (isDonorlocal ? 1.11 + pulse * 0.04 : 1.08 + pulse * 0.03) : 1);
      plate.titleSprite.scale.set(
        (plate.level === "grand" || plate.level === "major" ? 0.78 : 0.75) *
          (isActive ? (isDonorlocal ? 1.12 + pulse * 0.04 : 1.08 + pulse * 0.03) : 1),
      );
    });
  }

  private static async loadDonorTitleTextures(): Promise<Map<JackpotLevel, Texture | null>> {
    if (!JackpotPlaqueController.donorTitleTexturesPromise) {
      JackpotPlaqueController.donorTitleTexturesPromise = (async () => {
        const manifestUrl = resolveDonorManifestUrl();
        const atlasUrl = new URL("../anims_v5/coins_render.atlas", manifestUrl).toString();
        const atlasImageUrl = new URL("../anims_v5/coins_render.png", manifestUrl).toString();

        const result = new Map<JackpotLevel, Texture | null>([
          ["grand", null],
          ["major", null],
          ["minor", null],
          ["mini", null],
        ]);

        try {
          const response = await fetch(atlasUrl);
          if (!response.ok) {
            return result;
          }

          const atlasText = await response.text();
          const atlasImage = await JackpotPlaqueController.loadAtlasImage(atlasImageUrl);
          const frames = JackpotPlaqueController.parseAtlasFrames(atlasText);

          result.set(
            "grand",
            JackpotPlaqueController.createAtlasTexture(frames.get("slot/num_grand"), atlasImage),
          );
          result.set(
            "major",
            JackpotPlaqueController.createAtlasTexture(frames.get("slot/num_major"), atlasImage),
          );
          result.set(
            "minor",
            JackpotPlaqueController.createAtlasTexture(frames.get("slot/num_minor"), atlasImage),
          );
          result.set(
            "mini",
            JackpotPlaqueController.createAtlasTexture(frames.get("slot/num_mini"), atlasImage),
          );
        } catch {
          return result;
        }

        return result;
      })();
    }

    return JackpotPlaqueController.donorTitleTexturesPromise;
  }

  private static parseAtlasFrames(atlasText: string): Map<string, AtlasFrame> {
    const frames = new Map<string, AtlasFrame>();
    const lines = atlasText.split(/\r?\n/);
    let index = 0;

    while (index < lines.length) {
      const line = lines[index]?.trim() ?? "";
      index += 1;
      if (!line || line.includes(".png") || line.includes(":")) {
        continue;
      }

      const frameName = line;
      let frame: AtlasFrame | null = null;

      while (index < lines.length) {
        const row = lines[index]?.trim() ?? "";
        if (!row || !row.includes(":")) {
          break;
        }

        if (row.startsWith("bounds:")) {
          const [x, y, width, height] = row
            .replace("bounds:", "")
            .split(",")
            .map((value) => Number.parseInt(value.trim(), 10));
          if ([x, y, width, height].every((value) => Number.isFinite(value))) {
            frame = {
              x,
              y,
              width,
              height,
              offsetX: 0,
              offsetY: 0,
              originalWidth: width,
              originalHeight: height,
              rotated: false,
            };
          }
        } else if (row.startsWith("offsets:") && frame) {
          const [offsetX, offsetY, originalWidth, originalHeight] = row
            .replace("offsets:", "")
            .split(",")
            .map((value) => Number.parseInt(value.trim(), 10));
          if ([offsetX, offsetY, originalWidth, originalHeight].every((value) => Number.isFinite(value))) {
            frame.offsetX = offsetX;
            frame.offsetY = offsetY;
            frame.originalWidth = originalWidth;
            frame.originalHeight = originalHeight;
          }
        } else if (row.startsWith("rotate:") && frame) {
          frame.rotated = row.includes("90") || row.endsWith("true");
        }

        index += 1;
      }

      if (frame) {
        frames.set(frameName, frame);
      }
    }

    return frames;
  }

  private static createAtlasTexture(
    frame: AtlasFrame | undefined,
    sourceImage: CanvasImageSource,
  ): Texture | null {
    if (!frame) {
      return null;
    }

    const canvas = document.createElement("canvas");
    canvas.width = Math.max(1, frame.width);
    canvas.height = Math.max(1, frame.height);
    const context = canvas.getContext("2d");
    if (!context) {
      return null;
    }

    if (!frame.rotated) {
      context.drawImage(
        sourceImage,
        frame.x,
        frame.y,
        frame.width,
        frame.height,
        0,
        0,
        frame.width,
        frame.height,
      );
    } else {
      context.save();
      context.translate(0, frame.width);
      context.rotate(-Math.PI / 2);
      context.drawImage(
        sourceImage,
        frame.x,
        frame.y,
        frame.width,
        frame.height,
        0,
        0,
        frame.width,
        frame.height,
      );
      context.restore();
    }

    return Texture.from(canvas);
  }

  private static async loadAtlasImage(url: string): Promise<HTMLImageElement> {
    return await new Promise((resolve, reject) => {
      const image = new Image();
      image.crossOrigin = "anonymous";
      image.onload = () => resolve(image);
      image.onerror = () => reject(new Error(`Failed to load donor atlas image: ${url}`));
      image.src = url;
    });
  }

  private resolvePlaqueCenter(level: JackpotLevel): { x: number; y: number } {
    const centerX = this.machineWidth * 0.5;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    switch (level) {
      case "grand":
        return { x: centerX - (isDonorlocal ? 244 : 176), y: isDonorlocal ? -104 : -126 };
      case "major":
        return { x: centerX + (isDonorlocal ? 244 : 176), y: isDonorlocal ? -104 : -126 };
      case "minor":
        return { x: centerX - (isDonorlocal ? 232 : 166), y: isDonorlocal ? -34 : -54 };
      case "mini":
      default:
        return { x: centerX + (isDonorlocal ? 232 : 166), y: isDonorlocal ? -34 : -54 };
    }
  }

  private resolvePlaquePalette(level: JackpotLevel): {
    bg: number;
    border: number;
    label: number;
  } {
    switch (level) {
      case "grand":
        return { bg: 0x972125, border: 0xeec86f, label: 0xff3c3c };
      case "major":
        return { bg: 0x5a2488, border: 0xe4c868, label: 0xdc55ff };
      case "minor":
        return { bg: 0x4f2ca0, border: 0xe9cb74, label: 0x6b7cff };
      case "mini":
      default:
        return { bg: 0x3f7f35, border: 0xf0d181, label: 0x61d741 };
    }
  }

  private resolvePlaqueMetrics(
    level: JackpotLevel,
    isDonorlocal: boolean,
  ): { width: number; height: number } {
    const isTopPlaque = level === "grand" || level === "major";
    if (isDonorlocal) {
      return {
        width: isTopPlaque ? 184 : 166,
        height: isTopPlaque ? 54 : 50,
      };
    }
    return {
      width: isTopPlaque ? 206 : 176,
      height: isTopPlaque ? 60 : 54,
    };
  }
}
