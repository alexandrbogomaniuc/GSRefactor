import { Assets, Container, Graphics, Rectangle, Sprite, Texture, Ticker } from "pixi.js";
import { Spine } from "@esotericsoftware/spine-pixi-v8";

import { CRAZY_ROOSTER_LAYOUT, CRAZY_ROOSTER_PAYLINES } from "../config/CrazyRoosterGameConfig";
import { CrazyRoosterReel } from "./CrazyRoosterReel";
import type { DonorMultiplierVariantKey } from "./CrazyRoosterSymbol";
import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../app/assets/providerPackRegistry";

const DEFAULT_CABINET_FILTERS = {
  cabinetGlowBase: 0.44,
  cabinetGlowAmplitude: 0.14,
  separatorGlowBase: 0.55,
  separatorGlowAmplitude: 0.1,
} as const;

const DONOR_SLOT_SOURCE_WIDTH = 1886;
const DONOR_SLOT_SOURCE_HEIGHT = 1255;

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

export interface CrazyRoosterSpinOptions {
  minSpinDurationMs?: number;
  spinStaggerMs?: number;
  speedMultiplier?: number;
  finalReelHoldMs?: number;
  reelStopColumns: number[][];
  reelStopVariants?: Array<Array<DonorMultiplierVariantKey | null>>;
}

export type PresentationVariantMap = Partial<
  Record<`${number}-${number}`, DonorMultiplierVariantKey>
>;

export class CrazyRoosterSlotMachine extends Container {
  private readonly reels: CrazyRoosterReel[] = [];
  private readonly ticker = new Ticker();
  private readonly reelViewportMask = new Graphics();
  private readonly cabinetBase = new Graphics();
  private readonly cabinetInner = new Graphics();
  private readonly cabinetGlow = new Graphics();
  private readonly separatorLines = new Graphics();
  private readonly paylineGuide = new Graphics();
  private readonly boostOverlay = new Graphics();
  private readonly reelBed = new Graphics();
  private readonly reelBedSprite = new Sprite(Texture.WHITE);
  private readonly reelFrameSprite = new Sprite(Texture.WHITE);
  private donorSlotSpine: Spine | null = null;
  private donorSlotHideTimeout: number | null = null;
  private readonly spinTimeouts: number[] = [];
  private paylineFlashTimeout: number | null = null;
  private paylineFlashRemainingMs = 0;
  private readonly showPaylineGuide: boolean;
  private proofPaylineGuideVisible = false;
  private isSpinning = false;
  private elapsed = 0;
  private settledReels = 0;

  public onSpinStart: () => void = () => {};
  public onReelStop: (reelIndex: number) => void = () => {};
  public onSpinComplete: () => void = () => {};

  constructor(assetRoot: string) {
    super();
    const paylineParam = new URLSearchParams(window.location.search).get("showPaylines");
    this.showPaylineGuide = paylineParam === "1";

    const machineWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const machineHeight =
      CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
      (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing;

    this.redrawCabinet(machineWidth, machineHeight);
    this.reelFrameSprite.anchor.set(0.5);
    this.reelFrameSprite.x = machineWidth * 0.5;
    this.reelFrameSprite.y = machineHeight * 0.5;
    this.reelFrameSprite.width = machineWidth + 14;
    this.reelFrameSprite.height = machineHeight + 14;
    this.reelFrameSprite.alpha = 0.95;
    this.reelBedSprite.anchor.set(0.5);
    this.reelBedSprite.x = machineWidth * 0.5;
    this.reelBedSprite.y = machineHeight * 0.5;
    this.reelBedSprite.width = machineWidth + 2;
    this.reelBedSprite.height = machineHeight + 2;
    this.reelBedSprite.alpha = 1;
    this.addChild(
      this.cabinetBase,
      this.cabinetInner,
      this.reelBed,
      this.reelBedSprite,
      this.reelFrameSprite,
      this.cabinetGlow,
      this.separatorLines,
      this.paylineGuide,
    );
    this.paylineGuide.blendMode = "normal";
    this.paylineGuide.visible = this.showPaylineGuide;

    for (let index = 0; index < CRAZY_ROOSTER_LAYOUT.reelCount; index += 1) {
      const reel = new CrazyRoosterReel(index, assetRoot);
      reel.x = index * (CRAZY_ROOSTER_LAYOUT.symbolWidth + CRAZY_ROOSTER_LAYOUT.reelSpacing);
      reel.onStopSettled = (reelId) => this.handleReelSettled(reelId);
      this.reels.push(reel);
      this.addChild(reel);
    }
    this.addChild(this.boostOverlay);
    this.addChild(this.paylineGuide);

    this.reelViewportMask.roundRect(0, 0, machineWidth, machineHeight, 18);
    this.reelViewportMask.fill(0xffffff);
    this.addChild(this.reelViewportMask);
    for (const reel of this.reels) {
      reel.mask = this.reelViewportMask;
    }
    this.boostOverlay.mask = this.reelViewportMask;
    this.paylineGuide.mask = this.reelViewportMask;
    void this.applyReelFrameTexture();
    this.redrawCabinet(machineWidth, machineHeight);

    this.ticker.add((ticker) => {
      const frameMs = Math.min(ticker.deltaMS, 50);
      this.elapsed += frameMs / 1000;
      this.cabinetGlow.alpha =
        DEFAULT_CABINET_FILTERS.cabinetGlowBase +
        Math.sin(this.elapsed * 2.1) * DEFAULT_CABINET_FILTERS.cabinetGlowAmplitude;
      this.separatorLines.alpha =
        DEFAULT_CABINET_FILTERS.separatorGlowBase +
        Math.sin(this.elapsed * 2.4) * DEFAULT_CABINET_FILTERS.separatorGlowAmplitude;
      this.paylineFlashRemainingMs = Math.max(0, this.paylineFlashRemainingMs - frameMs);
      const flashStrength = Math.min(1, this.paylineFlashRemainingMs / 980);
      const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
      this.boostOverlay.visible = !isDonorlocal && flashStrength > 0.02;
      if (this.boostOverlay.visible) {
        this.boostOverlay.alpha = Math.max(
          0.08,
          0.06 + flashStrength * 0.36 + Math.sin(this.elapsed * 12) * 0.05,
        );
      }
      this.paylineGuide.visible =
        this.showPaylineGuide || this.proofPaylineGuideVisible || this.paylineFlashRemainingMs > 0;
      if (this.paylineGuide.visible) {
        if (this.proofPaylineGuideVisible) {
          const proofBase = 0.7 + Math.sin(this.elapsed * 2.8) * 0.06;
          this.paylineGuide.alpha =
            this.paylineFlashRemainingMs > 0
              ? Math.max(proofBase, 0.9 + Math.sin(this.elapsed * 7) * 0.08)
              : proofBase;
        } else {
          this.paylineGuide.alpha =
            this.paylineFlashRemainingMs > 0
              ? 0.9 + Math.sin(this.elapsed * 7) * 0.1
              : (this.showPaylineGuide ? 0.24 : 0.16) + Math.sin(this.elapsed * 3.2) * 0.08;
        }
      }
      if (!this.isSpinning) {
        return;
      }
      const deltaSeconds = frameMs / 1000;
      this.reels.forEach((reel) => reel.tick(deltaSeconds));
    });
    this.ticker.start();
  }

  public setPresentationColumns(
    columns: number[][],
    variants: PresentationVariantMap = {},
  ): void {
    if (this.donorSlotSpine) {
      this.donorSlotSpine.alpha = 0;
      this.donorSlotSpine.visible = false;
    }
    this.reels.forEach((reel, index) => {
      const reelVariants = Array.from({ length: CRAZY_ROOSTER_LAYOUT.rowCount }, (_, rowIndex) =>
        variants[`${index}-${rowIndex}`] ?? null,
      );
      reel.applyColumnWithVariants(columns[index] ?? [], reelVariants);
    });
  }

  public spin(options: CrazyRoosterSpinOptions): void {
    if (this.isSpinning) {
      return;
    }

    if (options.reelStopColumns.length !== CRAZY_ROOSTER_LAYOUT.reelCount) {
      throw new Error("CrazyRoosterSlotMachine.spin requires a stop column for every reel.");
    }
    if (
      options.reelStopVariants &&
      options.reelStopVariants.length !== CRAZY_ROOSTER_LAYOUT.reelCount
    ) {
      throw new Error(
        "CrazyRoosterSlotMachine.spin requires stop variants for every reel when provided.",
      );
    }

    this.isSpinning = true;
    this.settledReels = 0;
    this.paylineGuide.visible = this.showPaylineGuide;
    const speedMultiplier = options.speedMultiplier ?? 1;
    const minSpinDurationMs = options.minSpinDurationMs ?? CRAZY_ROOSTER_LAYOUT.minSpinMs;
    const spinStaggerMs = options.spinStaggerMs ?? CRAZY_ROOSTER_LAYOUT.spinStaggerMs;
    const finalReelHoldMs = options.finalReelHoldMs ?? 0;

    this.clearSpinTimeouts();
    this.clearDonorSlotHideTimeout();
    this.reels.forEach((reel) => reel.spin(speedMultiplier));
    if (getProviderPackStatus().effectiveProvider === "donorlocal" && this.donorSlotSpine) {
      this.donorSlotSpine.visible = true;
      this.donorSlotSpine.alpha = 0.46;
      this.donorSlotSpine.state.setAnimation(0, "spin_speed1_start", false);
      this.donorSlotSpine.state.addAnimation(0, "spin_speed1_idle", true, 0);
    }
    this.onSpinStart();

    const startTimeout = window.setTimeout(() => {
      this.reels.forEach((reel, index) => {
        const extraDelay =
          index === this.reels.length - 1 ? Math.max(0, finalReelHoldMs) : 0;
        const stopTimeout = window.setTimeout(() => {
          reel.stop(
            options.reelStopColumns[index] ?? [],
            options.reelStopVariants?.[index] ?? [],
          );
        }, index * spinStaggerMs + extraDelay);
        this.spinTimeouts.push(stopTimeout);
      });
    }, minSpinDurationMs);
    this.spinTimeouts.push(startTimeout);
  }

  public applyPresentationVariants(variants: PresentationVariantMap): void {
    this.reels.forEach((reel, reelIndex) => {
      const visibleSymbols = reel.getVisibleSymbols();
      for (let rowIndex = 0; rowIndex < visibleSymbols.length; rowIndex += 1) {
        visibleSymbols[rowIndex]?.applyDonorVariantOverride(
          variants[`${reelIndex}-${rowIndex}`] ?? null,
        );
      }
    });
  }

  public getReels(): CrazyRoosterReel[] {
    return this.reels;
  }

  public flashPaylines(durationMs = 950): void {
    this.paylineGuide.visible = true;
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      this.boostOverlay.visible = true;
      this.boostOverlay.alpha = 0.44;
    } else {
      this.boostOverlay.visible = false;
      this.boostOverlay.alpha = 0;
    }
    this.paylineFlashRemainingMs = Math.max(this.paylineFlashRemainingMs, durationMs);
    this.paylineGuide.alpha = 0.9;
    if (this.showPaylineGuide) {
      return;
    }
    if (this.paylineFlashTimeout !== null) {
      window.clearTimeout(this.paylineFlashTimeout);
    }
    this.paylineFlashTimeout = window.setTimeout(() => {
      this.paylineFlashTimeout = null;
      if (!this.isSpinning) {
        this.paylineGuide.visible = false;
      }
    }, Math.max(120, durationMs));
  }

  public setProofPaylineGuideVisible(visible: boolean): void {
    this.proofPaylineGuideVisible = visible;
    this.paylineGuide.visible =
      this.showPaylineGuide || this.proofPaylineGuideVisible || this.paylineFlashRemainingMs > 0;
  }

  public clearPaylineGuide(): void {
    if (this.paylineFlashTimeout !== null) {
      window.clearTimeout(this.paylineFlashTimeout);
      this.paylineFlashTimeout = null;
    }
    this.paylineFlashRemainingMs = 0;
    this.proofPaylineGuideVisible = false;
    this.paylineGuide.visible = this.showPaylineGuide;
    this.paylineGuide.alpha = this.showPaylineGuide ? 0.24 : 0;
  }

  private async applyReelFrameTexture(): Promise<void> {
    const status = getProviderPackStatus();
    if (status.effectiveProvider === "donorlocal") {
      await this.applyDonorlocalCabinetTextures();
      return;
    }

    this.reelBedSprite.visible = false;
    this.reelFrameSprite.visible = true;
    const preferredProvider = status.effectiveProvider;
    const resolved = await resolveProviderFrameTexture(
      "uiAtlas",
      "reel-frame-panel",
      preferredProvider,
    );
    const resolvedTexture = resolved.texture;
    if (resolvedTexture) {
      this.reelFrameSprite.texture = resolvedTexture;
      this.reelFrameSprite.tint = 0xffffff;
      this.reelFrameSprite.alpha = 0.95;
      return;
    }

    this.reelFrameSprite.texture = Texture.WHITE;
    this.reelFrameSprite.tint = 0x2b0810;
    this.reelFrameSprite.alpha = 0.95;
  }

  private async applyDonorlocalCabinetTextures(): Promise<void> {
    const manifestUrl = resolveDonorManifestUrl();
    const donorBoardCropUrl = new URL("./slot_board_crop.local.png", manifestUrl).toString();
    const donorSlotUrl = new URL("../image/slot.d8edf336.png", manifestUrl).toString();

    try {
      let bedTexture: Texture | null = null;
      try {
        await Assets.load(donorBoardCropUrl);
        bedTexture = Texture.from(donorBoardCropUrl);
      } catch {
        const slotTexture = await Assets.load<Texture>(donorSlotUrl);
        bedTexture = this.createDonorCropTexture(slotTexture, 1032, 24, 828, 650);
      }
      this.reelBedSprite.texture = bedTexture ?? Texture.WHITE;
      this.reelBedSprite.tint = bedTexture ? 0xffffff : 0x20135c;
      this.reelBedSprite.visible = true;
      this.reelFrameSprite.visible = false;
      if (this.donorSlotSpine) {
        this.donorSlotSpine.alpha = 0;
        this.donorSlotSpine.visible = false;
      }
      return;
    } catch (error) {
      console.warn("[CrazyRoosterSlotMachine] Failed to load donorlocal cabinet textures", error);
      this.reelBedSprite.visible = false;
      this.reelFrameSprite.visible = false;
    }
  }

  private createDonorCropTexture(
    atlasTexture: Texture,
    x: number,
    y: number,
    width: number,
    height: number,
    applyGreenKey = false,
  ): Texture | null {
    const source = atlasTexture.source;
    if (!source) {
      return null;
    }

    const sourceImage =
      (source.resource as { source?: CanvasImageSource } | null)?.source ??
      ((source as unknown as { source?: CanvasImageSource }).source ?? null);
    if (sourceImage) {
      const sourceWidth =
        (source as unknown as { pixelWidth?: number; width?: number }).pixelWidth ??
        (source as unknown as { width?: number }).width ??
        ("naturalWidth" in sourceImage ? sourceImage.naturalWidth : 0) ??
        ("videoWidth" in sourceImage ? sourceImage.videoWidth : 0) ??
        ("width" in sourceImage ? sourceImage.width : 0);
      const sourceHeight =
        (source as unknown as { pixelHeight?: number; height?: number }).pixelHeight ??
        (source as unknown as { height?: number }).height ??
        ("naturalHeight" in sourceImage ? sourceImage.naturalHeight : 0) ??
        ("videoHeight" in sourceImage ? sourceImage.videoHeight : 0) ??
        ("height" in sourceImage ? sourceImage.height : 0);
      const scaleX =
        sourceWidth > 0 ? sourceWidth / DONOR_SLOT_SOURCE_WIDTH : 1;
      const scaleY =
        sourceHeight > 0 ? sourceHeight / DONOR_SLOT_SOURCE_HEIGHT : 1;
      const cropX = Math.round(x * scaleX);
      const cropY = Math.round(y * scaleY);
      const cropWidth = Math.round(width * scaleX);
      const cropHeight = Math.round(height * scaleY);
      const canvas = document.createElement("canvas");
      canvas.width = width;
      canvas.height = height;
      const context = canvas.getContext("2d");
      if (context) {
        context.drawImage(
          sourceImage,
          cropX,
          cropY,
          cropWidth,
          cropHeight,
          0,
          0,
          width,
          height,
        );
        if (applyGreenKey) {
          const imageData = context.getImageData(0, 0, width, height);
          const pixels = imageData.data;
          for (let index = 0; index < pixels.length; index += 4) {
            const red = pixels[index];
            const green = pixels[index + 1];
            const blue = pixels[index + 2];
            if (green > 90 && green > red + 18 && green > blue + 12) {
              pixels[index + 3] = 0;
            }
          }
          context.putImageData(imageData, 0, 0);
        }
        return Texture.from(canvas);
      }
    }

    return new Texture({
      source,
      frame: new Rectangle(x, y, width, height),
      orig: new Rectangle(0, 0, width, height),
      trim: new Rectangle(0, 0, width, height),
    });
  }

  private isProofLightningScene(): boolean {
    const params = new URLSearchParams(window.location.search);
    return (
      getProviderPackStatus().effectiveProvider === "donorlocal" &&
      params.get("proofState")?.trim().toLowerCase() === "lightning"
    );
  }

  private redrawCabinet(machineWidth: number, machineHeight: number): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const proofLightningScene = this.isProofLightningScene();
    const donorBoardWidth = machineWidth + 68;
    const donorBoardHeight = machineHeight + 2;
    const donorBoardX = (machineWidth - donorBoardWidth) * 0.5;
    const donorBoardY = (machineHeight - donorBoardHeight) * 0.5;

    this.reelFrameSprite.x = machineWidth * 0.5;
    this.reelFrameSprite.y = machineHeight * 0.5;
    this.reelBedSprite.x = machineWidth * 0.5;
    this.reelBedSprite.y = machineHeight * 0.5;
    this.reelFrameSprite.width = isDonorlocal ? donorBoardWidth + 6 : machineWidth + 14;
    this.reelFrameSprite.height = isDonorlocal ? donorBoardHeight + 6 : machineHeight + 14;
    this.reelBedSprite.width = isDonorlocal ? donorBoardWidth : machineWidth + 2;
    this.reelBedSprite.height = isDonorlocal ? donorBoardHeight : machineHeight + 2;
    if (this.donorSlotSpine) {
      const donorSlotEffectVisible =
        isDonorlocal &&
        !proofLightningScene &&
        (this.isSpinning || this.donorSlotHideTimeout !== null) &&
        this.donorSlotSpine.alpha > 0.01;
      this.donorSlotSpine.visible = donorSlotEffectVisible;
      this.donorSlotSpine.x = machineWidth * 0.5;
      this.donorSlotSpine.y = machineHeight * 0.5;
      const donorScale = (machineWidth / 1440) * 0.99;
      this.donorSlotSpine.scale.set(donorScale);
      if (!donorSlotEffectVisible) {
        this.donorSlotSpine.alpha = 0;
      }
    }

    this.cabinetBase.clear();
    this.cabinetBase.roundRect(
      isDonorlocal ? donorBoardX - 3 : -10,
      isDonorlocal ? donorBoardY - 3 : -10,
      isDonorlocal ? donorBoardWidth + 6 : machineWidth + 20,
      isDonorlocal ? donorBoardHeight + 6 : machineHeight + 20,
      isDonorlocal ? 22 : 26,
    );
    this.cabinetBase.fill({
      color: isDonorlocal ? 0x21104f : 0x140306,
      alpha: isDonorlocal ? 0.028 : 0.98,
    });
    this.cabinetBase.stroke({
      color: isDonorlocal ? 0xfff0a6 : 0xf5c96a,
      width: isDonorlocal ? 4.2 : 5,
      alpha: isDonorlocal ? 0.88 : 0.96,
    });

    this.cabinetInner.clear();
    this.cabinetInner.roundRect(
      isDonorlocal ? donorBoardX - 0.75 : -1,
      isDonorlocal ? donorBoardY - 0.75 : -1,
      isDonorlocal ? donorBoardWidth + 1.5 : machineWidth + 2,
      isDonorlocal ? donorBoardHeight + 1.5 : machineHeight + 2,
      isDonorlocal ? 19 : 20,
    );
    this.cabinetInner.fill({
      color: isDonorlocal ? 0x1a1257 : 0x1b0432,
      alpha: isDonorlocal ? 0.018 : 0.82,
    });
    this.cabinetInner.stroke({
      color: isDonorlocal ? 0xeabf61 : 0x40215d,
      width: isDonorlocal ? 1.2 : 2.5,
      alpha: isDonorlocal ? 0.18 : 0.5,
    });

    this.reelBed.clear();
    if (!isDonorlocal) {
      this.reelBed.roundRect(0, 0, machineWidth, machineHeight, 18);
      this.reelBed.fill({
        color: 0x241658,
        alpha: 0.86,
      });
    }

    this.cabinetGlow.clear();
    this.cabinetGlow.roundRect(
      isDonorlocal ? donorBoardX - 4 : -20,
      isDonorlocal ? donorBoardY - 4 : -18,
      isDonorlocal ? donorBoardWidth + 8 : machineWidth + 40,
      isDonorlocal ? donorBoardHeight + 8 : machineHeight + 36,
      isDonorlocal ? 24 : 38,
    );
    this.cabinetGlow.stroke({
      color: isDonorlocal ? 0xffd96d : 0xc7141a,
      width: isDonorlocal ? 2.6 : 9,
      alpha: isDonorlocal ? 0.08 : 0.44,
    });

    this.separatorLines.clear();
    for (let reelIndex = 1; reelIndex < CRAZY_ROOSTER_LAYOUT.reelCount; reelIndex += 1) {
      const x =
        reelIndex * CRAZY_ROOSTER_LAYOUT.symbolWidth +
        (reelIndex - 0.5) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
      this.separatorLines.moveTo(x, 10);
      this.separatorLines.lineTo(x, machineHeight - 10);
    }
    this.separatorLines.stroke({
      color: 0xf9d98e,
      width: isDonorlocal ? 1.8 : 2,
      alpha: isDonorlocal ? 0.18 : 0.35,
    });

    const centerPoints = CRAZY_ROOSTER_PAYLINES.map((line) =>
      line.map((rowIndex, reelIndex) => {
        const x =
          reelIndex * (CRAZY_ROOSTER_LAYOUT.symbolWidth + CRAZY_ROOSTER_LAYOUT.reelSpacing) +
          CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
        const y =
          rowIndex * (CRAZY_ROOSTER_LAYOUT.symbolHeight + CRAZY_ROOSTER_LAYOUT.rowSpacing) +
          CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
        return { x, y };
      }),
    );

    this.paylineGuide.clear();
    this.paylineGuide.visible =
      this.showPaylineGuide || this.proofPaylineGuideVisible || this.paylineFlashRemainingMs > 0;
    centerPoints.forEach((points, lineIndex) => {
      const [first, second, third] = points;
      this.paylineGuide.moveTo(first.x, first.y);
      this.paylineGuide.lineTo(second.x, second.y);
      this.paylineGuide.lineTo(third.x, third.y);
      this.paylineGuide.stroke({
        color: 0xffd24a,
        width: isDonorlocal ? 3 : 4.4,
        alpha: isDonorlocal ? (lineIndex < 4 ? 0.28 : 0.22) : (lineIndex < 4 ? 0.34 : 0.28),
      });
      this.paylineGuide.moveTo(first.x, first.y);
      this.paylineGuide.lineTo(second.x, second.y);
      this.paylineGuide.lineTo(third.x, third.y);
      this.paylineGuide.stroke({
        color: 0xfff8d6,
        width: isDonorlocal ? 1.2 : 1.8,
        alpha: isDonorlocal ? (lineIndex < 4 ? 0.52 : 0.44) : (lineIndex < 4 ? 0.58 : 0.5),
      });
    });

    this.boostOverlay.clear();
    if (getProviderPackStatus().effectiveProvider === "donorlocal") {
      this.boostOverlay.visible = false;
      this.boostOverlay.alpha = 0;
      return;
    }
    const highlightedLineIndexes = [0, 1, 2, 4, 6];
    highlightedLineIndexes.forEach((lineIndex) => {
      const points = centerPoints[lineIndex];
      if (!points) {
        return;
      }
      const [first, second, third] = points;
      this.boostOverlay.moveTo(first.x, first.y);
      this.boostOverlay.lineTo(second.x, second.y);
      this.boostOverlay.lineTo(third.x, third.y);
      this.boostOverlay.stroke({
        color: 0xffd347,
        width: 8.4,
        alpha: 0.3,
      });
      this.boostOverlay.moveTo(first.x, first.y);
      this.boostOverlay.lineTo(second.x, second.y);
      this.boostOverlay.lineTo(third.x, third.y);
      this.boostOverlay.stroke({
        color: 0xfff6c8,
        width: 3.2,
        alpha: 0.82,
      });

      for (const point of points) {
        this.boostOverlay.circle(point.x, point.y, 8);
        this.boostOverlay.fill({ color: 0xffefb2, alpha: 0.22 });
      }
    });
    this.boostOverlay.blendMode = "add";
    this.boostOverlay.visible = false;
  }

  private handleReelSettled(reelId: number): void {
    if (!this.isSpinning) {
      return;
    }

    this.settledReels += 1;
    this.onReelStop(reelId);

    if (this.settledReels >= this.reels.length) {
      if (getProviderPackStatus().effectiveProvider === "donorlocal" && this.donorSlotSpine) {
        this.clearDonorSlotHideTimeout();
        this.donorSlotSpine.visible = true;
        this.donorSlotSpine.alpha = 0.38;
        this.donorSlotSpine.state.setAnimation(0, "spin_speed1_finish", false);
        this.donorSlotSpine.state.addAnimation(0, "idle_stop", true, 0);
        this.donorSlotHideTimeout = window.setTimeout(() => {
          if (!this.donorSlotSpine) {
            return;
          }
          this.donorSlotSpine.alpha = 0;
          this.donorSlotSpine.visible = false;
        }, 460);
      }
      this.isSpinning = false;
      this.paylineGuide.visible = this.showPaylineGuide;
      this.clearSpinTimeouts();
      this.onSpinComplete();
    }
  }

  private clearSpinTimeouts(): void {
    while (this.spinTimeouts.length > 0) {
      const timeout = this.spinTimeouts.pop();
      if (timeout !== undefined) {
        window.clearTimeout(timeout);
      }
    }
    if (this.paylineFlashTimeout !== null) {
      window.clearTimeout(this.paylineFlashTimeout);
      this.paylineFlashTimeout = null;
    }
    this.paylineFlashRemainingMs = 0;
  }

  private clearDonorSlotHideTimeout(): void {
    if (this.donorSlotHideTimeout !== null) {
      window.clearTimeout(this.donorSlotHideTimeout);
      this.donorSlotHideTimeout = null;
    }
  }
}
