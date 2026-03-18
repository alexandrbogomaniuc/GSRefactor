import { Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../assets/providerPackRegistry.ts";

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

type ChromeModes = {
  buyLabel: string;
  autoplayActive: boolean;
  turboSelected: boolean;
  soundEnabled: boolean;
  statusText: string;
};

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

export class Beta3VisualChrome extends Container {
  private static donorBuyBonusTexturesPromise: Promise<{
    button: Texture | null;
    icon: Texture | null;
  }> | null = null;

  public static readonly padding = {
    left: 78,
    right: 78,
    top: 70,
    bottom: 44,
  } as const;

  private readonly cabinetAura = new Graphics();
  private readonly cabinetFrameHint = new Graphics();
  private readonly boostStreak = new Graphics();
  private readonly buyPanel = new Container();
  private readonly buyPanelGlow = new Graphics();
  private readonly buyPanelSkin = new Sprite(Texture.WHITE);
  private readonly buyPanelBackground = new Graphics();
  private readonly buyIcon = new Sprite(Texture.WHITE);
  private readonly buyTitle = new Text({
    text: "BUY BONUS",
    style: {
      fontFamily: "Arial Black, Trebuchet MS, Arial, sans-serif",
      fontSize: 24,
      fontWeight: "900",
      fill: 0x7f1719,
      stroke: { color: 0xffe39a, width: 3 },
      align: "center",
    },
  });
  private readonly buyValue = new Text({
    text: "75",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 28,
      fontWeight: "900",
      fill: 0x5b120f,
      stroke: { color: 0xffe3a7, width: 3 },
      align: "center",
    },
  });
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);

  private machineWidth = 0;
  private machineHeight = 0;
  private viewportOrientation: "portrait" | "landscape" = "landscape";
  private ambientTime = 0;
  private boostFlash = 0;
  private buyPanelHover = 0;
  private textureRequestToken = 0;
  private modes: ChromeModes = {
    buyLabel: "BONUS 75",
    autoplayActive: false,
    turboSelected: false,
    soundEnabled: true,
    statusText: "READY",
  };

  public onBuyFeatureRequest: (() => void) | null = null;

  constructor() {
    super();
    this.boostStreak.blendMode = "add";
    this.addChild(this.cabinetAura, this.cabinetFrameHint, this.boostStreak);

    this.buyPanel.eventMode = "static";
    this.buyPanel.cursor = "pointer";
    this.buyPanel.on("pointertap", () => this.onBuyFeatureRequest?.());
    this.buyPanel.on("pointerover", () => {
      this.buyPanelHover = 1;
    });
    this.buyPanel.on("pointerout", () => {
      this.buyPanelHover = 0;
    });
    this.buyPanelSkin.anchor.set(0.5);
    this.buyIcon.anchor.set(0.5);
    this.buyTitle.anchor.set(0.5);
    this.buyValue.anchor.set(0.5);
    this.buyPanel.addChild(
      this.buyPanelGlow,
      this.buyPanelSkin,
      this.buyPanelBackground,
      this.buyIcon,
      this.buyTitle,
      this.buyValue,
    );
    this.addChild(this.buyPanel);

    this.redrawChrome();
    void this.refreshTextures();
    Ticker.shared.add(this.motionTicker);
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public setModeState(input: Partial<ChromeModes>): void {
    this.modes = { ...this.modes, ...input };
    this.buyValue.text = this.modes.buyLabel.replace("BONUS ", "");
  }

  public resize(machineWidth: number, machineHeight: number): void {
    this.machineWidth = machineWidth;
    this.machineHeight = machineHeight;
    this.redrawChrome();
  }

  public setViewportOrientation(orientation: "portrait" | "landscape"): void {
    if (this.viewportOrientation === orientation) {
      return;
    }
    this.viewportOrientation = orientation;
    this.redrawChrome();
  }

  public triggerBoostPulse(): void {
    this.boostFlash = 1;
  }

  public beginSpinCycle(): void {
    this.boostFlash = Math.max(this.boostFlash, 0.12);
  }

  public triggerPresentationCue(_input?: unknown): void {
    this.boostFlash = Math.max(this.boostFlash, 0.18);
  }

  public clearPresentationCue(): void {
    this.boostFlash = 0;
  }

  private tick(deltaMs: number): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }

    const frameMs = Math.min(deltaMs, 50);
    this.ambientTime += frameMs / 1000;
    this.boostFlash = Math.max(0, this.boostFlash - frameMs / 760);

    this.cabinetAura.alpha = 0.14 + Math.sin(this.ambientTime * 1.6) * 0.03 + this.boostFlash * 0.2;
    this.buyPanel.scale.set(1 + this.buyPanelHover * 0.035 + Math.sin(this.ambientTime * 2) * 0.008);
    this.buyPanelGlow.alpha = 0.26 + this.boostFlash * 0.34;
    this.redrawBoostStreak();
  }

  private redrawChrome(): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const isPortrait = this.viewportOrientation === "portrait";

    this.cabinetAura.clear();
    if (!isDonorlocal) {
      this.cabinetAura.roundRect(
        -56,
        -72,
        this.machineWidth + 112,
        this.machineHeight + 118,
        52,
      );
      this.cabinetAura.fill({ color: 0x6e0912, alpha: 0.08 });
    }

    this.cabinetFrameHint.clear();
    this.cabinetFrameHint.roundRect(
      isDonorlocal ? -6 : -12,
      isDonorlocal ? -6 : -12,
      this.machineWidth + (isDonorlocal ? 12 : 24),
      this.machineHeight + (isDonorlocal ? 12 : 24),
      isDonorlocal ? 24 : 30,
    );
    this.cabinetFrameHint.stroke({
      color: 0xf9d88d,
      width: isDonorlocal ? 1.2 : 2,
      alpha: isDonorlocal ? 0.06 : 0.18,
    });

    const buyX = isDonorlocal
      ? isPortrait
        ? this.machineWidth * 0.5
        : -142
      : -72;
    const buyY = isDonorlocal
      ? isPortrait
        ? this.machineHeight + 64
        : this.machineHeight * 0.485
      : this.machineHeight * 0.52;
    this.buyPanel.position.set(buyX, buyY);

    this.buyPanelGlow.clear();
    if (isDonorlocal) {
      this.buyPanelGlow.roundRect(
        isPortrait ? -170 : -88,
        isPortrait ? -52 : -42,
        isPortrait ? 340 : 176,
        isPortrait ? 104 : 84,
        28,
      );
      this.buyPanelGlow.stroke({
        color: 0xffd768,
        width: 6,
        alpha: 0.18,
      });
    } else {
      this.buyPanelGlow.roundRect(-96, -90, 192, 168, 24);
      this.buyPanelGlow.stroke({
        color: 0xc7141a,
        width: 10,
        alpha: 0.26,
      });
    }

    this.buyPanelBackground.clear();
    if (isDonorlocal) {
      this.buyPanelSkin.visible = true;
      this.buyPanelBackground.visible = true;
      const panelWidth = isPortrait ? 332 : 164;
      const panelHeight = isPortrait ? 100 : 72;
      this.buyPanelBackground.roundRect(-panelWidth * 0.5, -panelHeight * 0.5, panelWidth, panelHeight, 24);
      this.buyPanelBackground.fill({ color: 0xf5ce4f, alpha: 0.94 });
      this.buyPanelBackground.stroke({ color: 0xfff0bf, width: 3, alpha: 0.9 });
      this.buyPanelBackground.roundRect(
        -(isPortrait ? 304 : 148) * 0.5,
        -(isPortrait ? 82 : 56) * 0.5,
        isPortrait ? 304 : 148,
        isPortrait ? 22 : 18,
        12,
      );
      this.buyPanelBackground.fill({ color: 0xffffff, alpha: 0.12 });
    } else {
      this.buyPanelSkin.visible = false;
      this.buyPanelBackground.visible = true;
      this.buyPanelBackground.roundRect(-82, -78, 164, 144, 18);
      this.buyPanelBackground.fill({ color: 0xf5d35c, alpha: 0.96 });
      this.buyPanelBackground.stroke({ color: 0xfff0bf, width: 3, alpha: 0.94 });
    }

    this.buyPanelSkin.position.set(0, 0);
    this.buyPanelSkin.width = isDonorlocal ? (isPortrait ? 344 : 176) : 0;
    this.buyPanelSkin.height = isDonorlocal ? (isPortrait ? 108 : 78) : 0;

    this.buyIcon.position.set(
      isDonorlocal ? (isPortrait ? -118 : 0) : 0,
      isDonorlocal ? (isPortrait ? -1 : -12) : -20,
    );
    this.buyIcon.width = isDonorlocal ? (isPortrait ? 36 : 28) : 76;
    this.buyIcon.height = isDonorlocal ? (isPortrait ? 36 : 28) : 76;
    this.buyTitle.visible = isDonorlocal ? true : !isDonorlocal;
    this.buyValue.visible = !isDonorlocal;
    this.buyTitle.style.fontSize = isDonorlocal ? (isPortrait ? 36 : 17) : 24;
    this.buyTitle.style.letterSpacing = isDonorlocal ? (isPortrait ? 0.9 : 0.4) : 0;
    this.buyTitle.position.set(
      isDonorlocal ? (isPortrait ? 36 : 0) : 0,
      isDonorlocal ? (isPortrait ? 5 : 14) : 18,
    );
    this.buyValue.position.set(0, isDonorlocal ? 38 : 50);
  }

  private redrawBoostStreak(): void {
    this.boostStreak.clear();
    if (this.machineWidth <= 0 || this.machineHeight <= 0 || this.boostFlash < 0.12) {
      return;
    }

    const centerX = this.machineWidth * 0.5;
    const alpha = Math.min(1, this.boostFlash * 1.15);
    const zig = 18 + Math.sin(this.ambientTime * 10) * 6;

    this.boostStreak.moveTo(centerX - zig, 14);
    this.boostStreak.lineTo(centerX + zig * 0.5, this.machineHeight * 0.26);
    this.boostStreak.lineTo(centerX - zig * 0.36, this.machineHeight * 0.52);
    this.boostStreak.lineTo(centerX + zig * 0.74, this.machineHeight * 0.8);
    this.boostStreak.lineTo(centerX - zig * 0.16, this.machineHeight - 10);
    this.boostStreak.stroke({ color: 0xfff7dd, width: 12, alpha: alpha * 0.72 });

    this.boostStreak.moveTo(centerX + zig * 0.56, 42);
    this.boostStreak.lineTo(centerX + zig * 1.2, this.machineHeight * 0.34);
    this.boostStreak.lineTo(centerX + zig * 0.64, this.machineHeight * 0.58);
    this.boostStreak.lineTo(centerX + zig * 1.14, this.machineHeight * 0.9);
    this.boostStreak.stroke({ color: 0x7ce3ff, width: 6, alpha: alpha * 0.66 });

    this.boostStreak.roundRect(centerX - 12, 30, 24, this.machineHeight - 52, 12);
    this.boostStreak.fill({ color: 0xffffff, alpha: alpha * 0.14 });
  }

  private async refreshTextures(): Promise<void> {
    if (getProviderPackStatus().effectiveProvider === "donorlocal") {
      try {
        const donorTextures = await this.resolveDonorBuyBonusTextures();
        this.buyPanelSkin.texture = donorTextures.button ?? Texture.WHITE;
        this.buyPanelSkin.tint = donorTextures.button ? 0xffffff : 0xf5ce4f;
        this.buyIcon.texture = donorTextures.icon ?? Texture.WHITE;
        this.buyIcon.tint = 0xffffff;
        return;
      } catch {
        // Fall through to provider atlas lookup.
      }
    }

    const requestToken = ++this.textureRequestToken;
    const resolved = await resolveProviderFrameTexture("uiAtlas", "button-buybonus");
    if (requestToken !== this.textureRequestToken) {
      return;
    }

    this.buyIcon.texture = resolved.texture ?? Texture.WHITE;
    this.buyIcon.tint = resolved.texture ? 0xffffff : 0x8a1b1f;
  }

  private async resolveDonorBuyBonusTextures(): Promise<{
    button: Texture | null;
    icon: Texture | null;
  }> {
    if (!Beta3VisualChrome.donorBuyBonusTexturesPromise) {
      Beta3VisualChrome.donorBuyBonusTexturesPromise = (async () => {
        const manifestUrl = resolveDonorManifestUrl();
        const atlasUrl = new URL("../anims_v5/buy_bonus_render.atlas", manifestUrl).toString();
        const atlasImageUrl = new URL("../anims_v5/buy_bonus_render.png", manifestUrl).toString();
        const fallbackButtonUrl = new URL("../image/bonus.fb5969d4.png", manifestUrl).toString();

        try {
          const response = await fetch(atlasUrl);
          if (!response.ok) {
            return {
              button: await this.resolveFallbackDonorBuyButton(fallbackButtonUrl),
              icon: null,
            };
          }

          const atlasText = await response.text();
          const atlasImage = await Beta3VisualChrome.loadAtlasImage(atlasImageUrl);
          const frames = Beta3VisualChrome.parseAtlasFrames(atlasText);

          return {
            button:
              Beta3VisualChrome.createAtlasTexture(frames.get("buy_bonus/btn_buy_bonus"), atlasImage) ??
              (await this.resolveFallbackDonorBuyButton(fallbackButtonUrl)),
            icon: Beta3VisualChrome.createAtlasTexture(frames.get("buy_bonus/ico_def"), atlasImage),
          };
        } catch {
          return {
            button: await this.resolveFallbackDonorBuyButton(fallbackButtonUrl),
            icon: null,
          };
        }
      })();
    }

    return Beta3VisualChrome.donorBuyBonusTexturesPromise;
  }

  private async resolveFallbackDonorBuyButton(url: string): Promise<Texture | null> {
    const image = await Beta3VisualChrome.loadAtlasImage(url);
    const canvas = document.createElement("canvas");
    canvas.width = 182;
    canvas.height = 198;
    const context = canvas.getContext("2d");
    if (!context) {
      return null;
    }

    context.drawImage(
      image,
      18,
      698,
      182,
      198,
      0,
      0,
      182,
      198,
    );
    return Texture.from(canvas);
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
}
