import { Assets, Container, Graphics, Sprite, Text, Texture } from "pixi.js";

import { engine } from "@gamesv1/pixi-engine";
import {
  createAudioCueRegistry,
  resolveAudioCueActions,
  WowPreloader,
  type AudioCueRegistry,
  type ShellThemeTokens,
} from "@gamesv1/ui-kit";

import roosterLogoUrl from "../../../raw-assets/preload{m}/rooster-logo.png?url";
import betonlineLogoUrl from "../../../raw-assets/preload{m}/betonline-logo.svg?url";
import { userSettings } from "../utils/userSettings";

export class LoadScreen extends Container {
  public static assetBundles = ["preload"];
  private static themeTokens: ShellThemeTokens | null = null;
  private static audioRegistry: AudioCueRegistry = createAudioCueRegistry();

  private readonly preloader: WowPreloader;
  private readonly backdrop = new Graphics();
  private readonly backdropGlow = new Graphics();
  private readonly backdropFlares = new Graphics();
  private readonly floorGlow = new Graphics();
  private readonly lockupPlateShadow = new Graphics();
  private readonly lockupPlate = new Graphics();
  private readonly lockupInset = new Graphics();
  private readonly roosterLogo = new Sprite(Texture.EMPTY);
  private readonly roosterFallbackText = new Text({
    text: "CRAZY ROOSTER\nHOLD AND WIN",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 34,
      fontWeight: "900",
      fill: 0xffd86f,
      stroke: { color: 0x7c140d, width: 4 },
      align: "center",
      lineHeight: 34,
      letterSpacing: 1,
    },
  });
  private readonly betonlineLogo = new Sprite(Texture.EMPTY);
  private readonly betonlineFallbackText = new Text({
    text: "BETONLINE",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 92,
      fontWeight: "900",
      fill: 0xffffff,
      stroke: { color: 0x1a1c20, width: 4 },
      align: "center",
      letterSpacing: 2,
    },
  });
  private readonly statusText: Text;
  private readonly logoShine = new Graphics();
  private readonly completionStar = new Graphics();
  private readonly statusPlate = new Graphics();
  private readonly loadingGlow = new Graphics();
  private readonly loadingFrame = new Graphics();
  private readonly loadingTrack = new Graphics();
  private readonly loadingFill = new Graphics();
  private readonly loadingBorderSpark = new Graphics();
  private readonly emberLayer = new Graphics();
  private readonly footerText: Text;
  private readonly proofHoldMs = Math.max(
    0,
    Number(new URLSearchParams(window.location.search).get("preloaderHoldMs") ?? "0") || 0,
  );
  private readonly reducedMotion =
    new URLSearchParams(window.location.search).get("motion") === "minimal" ||
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  private audioStingerPlayed = false;
  private shownAtMs = 0;
  private progress = 0;
  private statusLabel = "BOOTSTRAPPING";
  private ambientTime = 0;
  private shineProgress = -1;
  private starLife = 0;
  private roosterReady = false;
  private betonlineReady = false;
  private roosterBaseWidth = 0;
  private roosterBaseHeight = 0;
  private lockupRect = { x: 0, y: 0, width: 0, height: 0 };
  private trackRect = { x: 0, y: 0, width: 0, height: 0, skew: 0 };
  private betonlineRect = { x: 0, y: 0, width: 0, height: 0 };

  public static configure(input: {
    themeTokens: ShellThemeTokens;
    audioRegistry?: AudioCueRegistry;
  }): void {
    LoadScreen.themeTokens = input.themeTokens;
    LoadScreen.audioRegistry = input.audioRegistry ?? createAudioCueRegistry();
  }

  constructor() {
    super();

    this.eventMode = "static";
    this.preloader = new WowPreloader(LoadScreen.themeTokens ?? undefined, {
      reducedMotion: this.reducedMotion,
    });
    this.preloader.alpha = 0;
    this.preloader.eventMode = "none";

    this.roosterLogo.anchor.set(0.5);
    this.roosterLogo.visible = false;
    this.roosterFallbackText.anchor.set(0.5);
    this.betonlineLogo.anchor.set(0.5);
    this.betonlineLogo.visible = false;
    this.betonlineFallbackText.anchor.set(0.5);

    this.statusText = new Text({
      text: this.statusLabel,
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 19,
        fontWeight: "900",
        fill: 0xf4f4f4,
        stroke: { color: 0x24272b, width: 4 },
        align: "center",
        letterSpacing: 2,
      },
    });
    this.statusText.anchor.set(0.5);
    this.logoShine.visible = false;
    this.completionStar.visible = false;
    this.addChild(
      this.backdrop,
      this.backdropGlow,
      this.backdropFlares,
      this.floorGlow,
      this.lockupPlateShadow,
      this.lockupPlate,
      this.lockupInset,
      this.preloader,
      this.roosterLogo,
      this.roosterFallbackText,
      this.betonlineLogo,
      this.betonlineFallbackText,
      this.logoShine,
      this.completionStar,
      this.statusPlate,
      this.statusText,
      this.loadingGlow,
      this.loadingFrame,
      this.loadingTrack,
      this.loadingFill,
      this.loadingBorderSpark,
      this.emberLayer,
    );

    this.footerText = new Text({
      text: "Powered by BetOnline Studios®",
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 14,
        fontWeight: "700",
        fill: 0xdbdbdb,
        align: "center",
      },
    });
    this.footerText.anchor.set(0.5);
    this.addChild(this.footerText);

    void this.loadBrandTextures();
  }

  public setBootPhase(label: string, progress: number): void {
    this.preloader.setStatus(label);
    this.preloader.setProgress(progress);
    this.statusLabel = label.toUpperCase();
    this.progress = Math.max(this.progress, progress);
    this.redrawOverlay();
  }

  public beginAssetLoadPhase(): void {
    this.preloader.setStatus("LOADING ASSETS");
    this.statusLabel = "LOADING ASSETS";
    this.redrawOverlay();
  }

  public onLoad(progress: number): void {
    const weightedProgress = 20 + progress * 0.8;
    this.preloader.setStatus(progress >= 100 ? "READY" : "LOADING ASSETS");
    this.preloader.setProgress(weightedProgress);
    this.statusLabel = progress >= 100 ? "READY TO PLAY" : "LOADING ASSETS";
    this.progress = weightedProgress;
    if (weightedProgress >= 100 && this.shineProgress < 0) {
      this.shineProgress = 0;
    }
    this.redrawOverlay();
  }

  public resize(width: number, height: number): void {
    this.preloader.resize(width, height);
    const isPortrait = height > width;
    const stackCenterX = width * 0.5 + (isPortrait ? 12 : 0);

    const roosterWidthBase = Math.min(width * 0.29, 402);
    const roosterWidth = Math.min(
      roosterWidthBase * (isPortrait ? 2.05 : 1),
      isPortrait ? 660 : 402,
    );
    const roosterHeight = roosterWidth * (1024 / 1536);
    const roosterY = height * (isPortrait ? 0.305 : 0.275);
    this.roosterLogo.x = stackCenterX;
    this.roosterLogo.y = roosterY;
    this.roosterBaseWidth = roosterWidth;
    this.roosterBaseHeight = roosterHeight;
    this.roosterLogo.width = this.roosterBaseWidth;
    this.roosterLogo.height = this.roosterBaseHeight;
    this.roosterFallbackText.x = stackCenterX;
    this.roosterFallbackText.y = roosterY;
    this.roosterFallbackText.visible = !this.roosterReady;

    const betonlineBaseWidth = Math.min(width * 0.62, 760);
    const betonlineHeight = betonlineBaseWidth * (289 / 735);
    const betonlineWidth = betonlineBaseWidth * 1.08;
    const betonlineY = height * (isPortrait ? 0.468 : 0.462);
    this.betonlineLogo.x = stackCenterX;
    this.betonlineLogo.y = betonlineY;
    this.betonlineLogo.width = betonlineWidth;
    this.betonlineLogo.height = betonlineHeight;
    this.betonlineFallbackText.x = stackCenterX;
    this.betonlineFallbackText.y = betonlineY;
    this.betonlineFallbackText.visible = !this.betonlineReady;
    this.betonlineRect = {
      x: this.betonlineLogo.x - this.betonlineLogo.width * 0.5,
      y: this.betonlineLogo.y - this.betonlineLogo.height * 0.5,
      width: this.betonlineLogo.width,
      height: this.betonlineLogo.height,
    };

    this.lockupRect = {
      x: stackCenterX - Math.min(width * 0.39, 430),
      y: roosterY - roosterHeight * 0.72,
      width: Math.min(width * 0.78, 860),
      height: (betonlineY + betonlineHeight * 0.78) - (roosterY - roosterHeight * 0.72),
    };

    const trackWidth = Math.min(width * 0.38, 460);
    const trackHeight = Math.max(22, Math.min(30, height * 0.036));
    const trackY = betonlineY + betonlineHeight * 0.9;
    this.trackRect = {
      x: stackCenterX - trackWidth * 0.5,
      y: trackY,
      width: trackWidth,
      height: trackHeight,
      skew: Math.max(14, trackHeight * 0.8),
    };

    this.statusText.x = stackCenterX;
    this.statusText.y = trackY - 62;
    this.footerText.x = width * 0.5;
    this.footerText.y = height - 26;

    this.redrawOverlay();
  }

  public update(ticker: { deltaMS: number }): void {
    this.ambientTime += ticker.deltaMS / 1000;
    this.preloader.tick(ticker.deltaMS);
    this.redrawOverlay(ticker.deltaMS / 1000);
  }

  public async show(): Promise<void> {
    this.alpha = 1;
    this.shownAtMs = performance.now();
    this.preloader.setProgress(6);
    this.preloader.setStatus("BOOTSTRAPPING");
    this.progress = 6;
    this.statusLabel = "BOOTSTRAPPING";
    this.shineProgress = -1;
    this.starLife = 0;
    this.redrawOverlay();
    window.addEventListener("pointerdown", this.tryPlayAudioStinger, { once: true });
  }

  public async hide(): Promise<void> {
    const elapsedMs = performance.now() - this.shownAtMs;
    const remainingHoldMs = this.proofHoldMs - elapsedMs;
    if (remainingHoldMs > 0) {
      await new Promise((resolve) => {
        window.setTimeout(resolve, remainingHoldMs);
      });
    }

    this.alpha = 0;
    window.removeEventListener("pointerdown", this.tryPlayAudioStinger);
  }

  private redrawOverlay(deltaSeconds = 0): void {
    const width = this.preloader.width || window.innerWidth || 1280;
    const height = this.preloader.height || window.innerHeight || 720;
    this.backdrop.clear();
    this.backdrop.rect(0, 0, width, height);
    this.backdrop.fill({ color: 0x1d1f24, alpha: 1 });

    this.backdropGlow.clear();
    this.backdropGlow.ellipse(width * 0.5, height * 0.46, width * 0.38, height * 0.34);
    this.backdropGlow.fill({ color: 0x7b0d14, alpha: 0.26 });
    this.backdropGlow.ellipse(width * 0.5, height * 0.4, width * 0.24, height * 0.18);
    this.backdropGlow.fill({ color: 0xe3aa45, alpha: 0.1 });

    this.backdropFlares.clear();
    this.backdropFlares.moveTo(width * 0.16, height * 0.12);
    this.backdropFlares.lineTo(width * 0.26, height * 0.18);
    this.backdropFlares.lineTo(width * 0.2, height * 0.72);
    this.backdropFlares.lineTo(width * 0.08, height * 0.62);
    this.backdropFlares.closePath();
    this.backdropFlares.fill({ color: 0x5a0b11, alpha: 0.16 });
    this.backdropFlares.moveTo(width * 0.84, height * 0.12);
    this.backdropFlares.lineTo(width * 0.92, height * 0.62);
    this.backdropFlares.lineTo(width * 0.8, height * 0.72);
    this.backdropFlares.lineTo(width * 0.74, height * 0.18);
    this.backdropFlares.closePath();
    this.backdropFlares.fill({ color: 0x5a0b11, alpha: 0.16 });

    this.floorGlow.clear();
    this.floorGlow.ellipse(width * 0.5, height * 0.86, width * 0.38, height * 0.08);
    this.floorGlow.fill({ color: 0xffb046, alpha: 0.08 });

    this.lockupPlateShadow.clear();
    this.lockupPlateShadow.roundRect(
      this.lockupRect.x + 18,
      this.lockupRect.y + 22,
      this.lockupRect.width,
      this.lockupRect.height + 26,
      42,
    );
    this.lockupPlateShadow.fill({ color: 0x050102, alpha: 0.36 });

    this.lockupPlate.clear();
    this.lockupPlate.roundRect(
      this.lockupRect.x,
      this.lockupRect.y,
      this.lockupRect.width,
      this.lockupRect.height + 18,
      42,
    );
    this.lockupPlate.fill({ color: 0x150306, alpha: 0.92 });
    this.lockupPlate.stroke({ color: 0xf0c877, width: 4, alpha: 0.88 });

    this.lockupInset.clear();
    this.lockupInset.roundRect(
      this.lockupRect.x + 16,
      this.lockupRect.y + 18,
      this.lockupRect.width - 32,
      this.lockupRect.height - 10,
      34,
    );
    this.lockupInset.fill({ color: 0x2a070a, alpha: 0.74 });
    this.lockupInset.stroke({ color: 0x5d0c11, width: 2, alpha: 0.72 });

    this.statusText.text = this.statusLabel;

    const roosterPulse = this.reducedMotion
      ? 1
      : 0.982 + Math.sin(this.ambientTime * 0.8) * 0.018;
    this.roosterLogo.width = this.roosterBaseWidth * roosterPulse;
    this.roosterLogo.height = this.roosterBaseHeight * roosterPulse;
    this.roosterFallbackText.scale.set(roosterPulse);

    const clampedProgress = Math.max(0, Math.min(100, this.progress));
    const innerPadding = 4;
    const fillRatio = clampedProgress / 100;
    const fillWidth = Math.max(0, (this.trackRect.width - innerPadding * 2) * fillRatio);
    const fillHeight = this.trackRect.height - innerPadding * 2;

    this.statusPlate.clear();
    this.statusPlate.roundRect(
      this.statusText.x - Math.max(118, this.statusText.width * 0.5 + 20),
      this.statusText.y - 18,
      Math.max(236, this.statusText.width + 40),
      36,
      16,
    );
    this.statusPlate.fill({ color: 0x36080c, alpha: 0.88 });
    this.statusPlate.stroke({ color: 0xffd78a, width: 2, alpha: 0.78 });

    this.loadingGlow.clear();
    this.drawSkewRect(
      this.loadingGlow,
      this.trackRect.x - 18,
      this.trackRect.y - 16,
      this.trackRect.width + 36,
      this.trackRect.height + 30,
      this.trackRect.skew + 4,
      0x6f0c12,
      0.18,
    );

    this.loadingFrame.clear();
    this.drawSkewRect(
      this.loadingFrame,
      this.trackRect.x - 10,
      this.trackRect.y - 10,
      this.trackRect.width + 20,
      this.trackRect.height + 20,
      this.trackRect.skew,
      0xc7141a,
      0.22,
    );
    this.loadingFrame.stroke({ color: 0xffd78a, width: 2, alpha: 0.34 });

    this.loadingTrack.clear();
    this.drawSkewRect(
      this.loadingTrack,
      this.trackRect.x,
      this.trackRect.y,
      this.trackRect.width,
      this.trackRect.height,
      this.trackRect.skew,
      0x121418,
      0.95,
    );
    this.loadingTrack.stroke({ color: 0x5f6772, width: 2, alpha: 0.96 });

    this.loadingFill.clear();
    if (fillWidth > 0) {
      const fillX = this.trackRect.x + innerPadding;
      const fillY = this.trackRect.y + innerPadding;
      const redWidth = Math.min(fillWidth, (this.trackRect.width - innerPadding * 2) * 0.3);
      if (redWidth > 0) {
        this.drawSkewRect(
          this.loadingFill,
          fillX,
          fillY,
          redWidth,
          fillHeight,
          Math.max(8, this.trackRect.skew - 6),
          0xc7141a,
          0.98,
        );
      }
      const whiteWidth = Math.max(0, fillWidth - redWidth);
      if (whiteWidth > 0) {
        this.drawSkewRect(
          this.loadingFill,
          fillX + redWidth,
          fillY,
          whiteWidth,
          fillHeight,
          Math.max(8, this.trackRect.skew - 6),
          0xffffff,
          0.95,
        );
      }
      this.drawSkewRect(
        this.loadingFill,
        fillX + Math.max(0, fillWidth - 24),
        fillY,
        Math.min(24, fillWidth),
        fillHeight,
        Math.max(8, this.trackRect.skew - 8),
        0xfff4d4,
        0.72,
      );
    }

    const sparkPhase = (this.ambientTime * 0.9) % 1;
    const sparkX = this.trackRect.x + sparkPhase * this.trackRect.width;
    const sparkY = this.trackRect.y - 2;
    const sparkAlpha = fillRatio > 0 ? 0.3 + Math.sin(this.ambientTime * 3.4) * 0.1 : 0;
    this.loadingBorderSpark.clear();
    this.loadingBorderSpark.moveTo(sparkX, sparkY - 4);
    this.loadingBorderSpark.lineTo(sparkX + 4, sparkY);
    this.loadingBorderSpark.lineTo(sparkX, sparkY + 4);
    this.loadingBorderSpark.lineTo(sparkX - 4, sparkY);
    this.loadingBorderSpark.closePath();
    this.loadingBorderSpark.fill({ color: 0xfff4d4, alpha: Math.max(0, sparkAlpha) });

    this.emberLayer.clear();
    for (let index = 0; index < 6; index += 1) {
      const phase = this.ambientTime * 0.7 + index * 0.8;
      const emberX = this.lockupRect.x + 40 + ((index + 1) / 7) * (this.lockupRect.width - 80);
      const emberY = this.trackRect.y + 44 + Math.sin(phase) * 8;
      this.emberLayer.circle(emberX, emberY, 3 + (index % 2));
      this.emberLayer.fill({ color: 0xffc76f, alpha: 0.12 + Math.sin(phase + 0.8) * 0.04 });
    }

    if (!this.reducedMotion && this.shineProgress >= 0 && this.shineProgress <= 1) {
      this.shineProgress += deltaSeconds / 0.7;
    }
    if (this.reducedMotion && this.shineProgress >= 0) {
      this.shineProgress = 1.05;
    }

    this.logoShine.clear();
    this.logoShine.visible =
      !this.reducedMotion && this.shineProgress >= 0 && this.shineProgress <= 1;
    if (this.logoShine.visible) {
      const startX = this.betonlineRect.x - 40;
      const endX = this.betonlineRect.x + this.betonlineRect.width + 40;
      const lineX = startX + (endX - startX) * this.shineProgress;
      this.logoShine.moveTo(lineX - 3, this.betonlineRect.y + this.betonlineRect.height + 16);
      this.logoShine.lineTo(lineX + 7, this.betonlineRect.y - 16);
      this.logoShine.lineTo(lineX + 16, this.betonlineRect.y - 16);
      this.logoShine.lineTo(lineX + 6, this.betonlineRect.y + this.betonlineRect.height + 16);
      this.logoShine.closePath();
      this.logoShine.fill({ color: 0xffffff, alpha: 0.74 });
      this.logoShine.stroke({ color: 0xffffff, width: 1, alpha: 0.95 });
    }

    if (!this.reducedMotion && this.shineProgress > 1 && this.starLife <= 0) {
      this.starLife = 1;
    }
    this.starLife = Math.max(0, this.starLife - deltaSeconds / 0.55);
    const starPulse = Math.sin(this.ambientTime * 11) * 0.12;
    this.completionStar.clear();
    this.completionStar.visible = this.starLife > 0;
    if (this.completionStar.visible) {
      const starSize = 10 + starPulse * 4;
      const starX = this.betonlineRect.x + this.betonlineRect.width + 12;
      const starY = this.betonlineRect.y - 6;
      this.completionStar.moveTo(starX, starY - starSize);
      this.completionStar.lineTo(starX + starSize * 0.35, starY - starSize * 0.35);
      this.completionStar.lineTo(starX + starSize, starY);
      this.completionStar.lineTo(starX + starSize * 0.35, starY + starSize * 0.35);
      this.completionStar.lineTo(starX, starY + starSize);
      this.completionStar.lineTo(starX - starSize * 0.35, starY + starSize * 0.35);
      this.completionStar.lineTo(starX - starSize, starY);
      this.completionStar.lineTo(starX - starSize * 0.35, starY - starSize * 0.35);
      this.completionStar.closePath();
      this.completionStar.fill({ color: 0xffffff, alpha: this.starLife * 0.9 });
    }
  }

  private readonly tryPlayAudioStinger = (): void => {
    if (this.audioStingerPlayed) {
      return;
    }

    const cue = LoadScreen.themeTokens?.preloader.audioStingerCue;
    if (!cue || userSettings.getMasterVolume() <= 0) {
      return;
    }

    for (const action of resolveAudioCueActions(cue, LoadScreen.audioRegistry)) {
      if (action.type !== "sfx") {
        continue;
      }
      if (action.respectSoundEnabled && userSettings.getMasterVolume() <= 0) {
        continue;
      }
      try {
        engine().audio.sfx.play(action.assetKey, {
          volume: action.volume ?? 1,
        });
      } catch {
        // Preloader stingers are optional in the QA slice.
      }
      this.audioStingerPlayed = true;
    }
  };

  private drawSkewRect(
    target: Graphics,
    x: number,
    y: number,
    width: number,
    height: number,
    skew: number,
    color: number,
    alpha: number,
  ): void {
    const safeSkew = Math.min(Math.max(4, skew), width * 0.45);
    target.moveTo(x, y + height);
    target.lineTo(x + safeSkew, y);
    target.lineTo(x + width, y);
    target.lineTo(x + width - safeSkew, y + height);
    target.closePath();
    target.fill({ color, alpha });
  }

  private async loadBrandTextures(): Promise<void> {
    try {
      await Assets.load(roosterLogoUrl);
      this.roosterLogo.texture = Texture.from(roosterLogoUrl);
      this.roosterLogo.tint = 0xffffff;
      this.roosterReady = true;
      this.roosterLogo.visible = true;
    } catch {
      this.roosterReady = false;
      this.roosterLogo.visible = false;
    }

    try {
      await Assets.load(betonlineLogoUrl);
      this.betonlineLogo.texture = Texture.from(betonlineLogoUrl);
      this.betonlineLogo.tint = 0xffffff;
      this.betonlineReady = true;
      this.betonlineLogo.visible = true;
    } catch {
      this.betonlineReady = false;
      this.betonlineLogo.visible = false;
    }

    this.roosterFallbackText.visible = !this.roosterReady;
    this.betonlineFallbackText.visible = !this.betonlineReady;
    this.resize(this.preloader.width || window.innerWidth, this.preloader.height || window.innerHeight);
  }
}
