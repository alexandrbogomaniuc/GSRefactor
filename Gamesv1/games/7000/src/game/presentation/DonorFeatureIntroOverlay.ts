import { Assets, Container, Graphics, Sprite, Text, Texture } from "pixi.js";

import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../app/assets/providerPackRegistry";
import { resolveMappedSourceTexture } from "../../app/assets/mappedSourceTextureResolver";
import { CRAZY_ROOSTER_SYMBOL_FRAME_KEYS } from "../config/CrazyRoosterGameConfig";

export type DonorFeatureIntroVariant = "blitz" | "power" | "ultimate";

type IntroPreviewOptions = {
  holdMs?: number;
  symbolGrid?: number[][] | null;
};

type LoadedOverlayAssets = {
  titles: Record<DonorFeatureIntroVariant, Texture | null>;
  plates: Record<DonorFeatureIntroVariant, Texture | null>;
  board: Texture | null;
  icons: Record<
    | "superStrike"
    | "strike"
    | "bonus"
    | "major"
    | "minor"
    | "mini"
    | "grand"
    | "x2"
    | "x3"
    | "x5"
    | "x10"
    | "x15"
    | "cherry"
    | "watermelon"
    | "lemon"
    | "orange"
    | "bell"
    | "bar",
    Texture | null
  >;
};

type MotionSpriteConfig = {
  texture: Texture | null;
  startX: number;
  startY: number;
  endX: number;
  endY: number;
  scale: number;
  rotation: number;
  delayMs?: number;
};

type VariantLayout = {
  heading: string;
  captionTop: string;
  captionBottom: string;
  iconRow: Array<Texture | null>;
  movers: MotionSpriteConfig[];
};

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

const clamp01 = (value: number): number => Math.max(0, Math.min(1, value));

const easeOutCubic = (value: number): number => 1 - Math.pow(1 - clamp01(value), 3);

export class DonorFeatureIntroOverlay extends Container {
  private static assetsPromise: Promise<LoadedOverlayAssets> | null = null;
  private static previewTexturePromises = new Map<number, Promise<Texture | null>>();
  private static composedCoinPromises = new Map<string, Promise<Texture | null>>();

  private readonly scrim = new Graphics();
  private readonly bottomShade = new Graphics();
  private readonly topGlow = new Graphics();
  private readonly boardGlow = new Graphics();
  private readonly boardFrame = new Graphics();
  private readonly heroGlow = new Sprite(Texture.WHITE);
  private readonly heroCoin = new Sprite(Texture.EMPTY);
  private readonly titleSprite = new Sprite(Texture.EMPTY);
  private readonly plateSprite = new Sprite(Texture.EMPTY);
  private readonly previewBoard = new Sprite(Texture.EMPTY);
  private readonly previewSymbols = Array.from({ length: 12 }, () => {
    const sprite = new Sprite(Texture.EMPTY);
    sprite.anchor.set(0.5);
    return sprite;
  });
  private readonly titleFallback = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 40,
      fontWeight: "900",
      fill: 0xffd64f,
      stroke: { color: 0x4d090d, width: 8 },
      align: "center",
      lineHeight: 40,
      letterSpacing: 0.6,
    },
  });
  private readonly captionTop = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 20,
      fontWeight: "700",
      fill: 0xfff2c8,
      stroke: { color: 0x4e080d, width: 5 },
      align: "center",
      lineHeight: 24,
    },
  });
  private readonly captionBottom = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 20,
      fontWeight: "800",
      fill: 0xfff2c8,
      stroke: { color: 0x4e080d, width: 5 },
      align: "center",
      lineHeight: 24,
    },
  });
  private readonly continueText = new Text({
    text: "CLICK TO CONTINUE",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 22,
      fontWeight: "900",
      fill: 0xe3d2cf,
      stroke: { color: 0x1a0406, width: 5 },
      align: "center",
      letterSpacing: 1.1,
    },
  });
  private readonly rowIcons = Array.from({ length: 6 }, () => {
    const sprite = new Sprite(Texture.EMPTY);
    sprite.anchor.set(0.5);
    return sprite;
  });
  private readonly movingIcons = Array.from({ length: 4 }, () => {
    const sprite = new Sprite(Texture.EMPTY);
    sprite.anchor.set(0.5);
    return sprite;
  });
  private readonly movingGlow = Array.from({ length: 4 }, () => {
    const sprite = new Sprite(Texture.WHITE);
    sprite.anchor.set(0.5);
    sprite.tint = 0xffd36b;
    sprite.blendMode = "add";
    return sprite;
  });
  private viewportWidth = 0;
  private viewportHeight = 0;
  private holdMs = 0;
  private elapsedMs = 0;
  private activeVariant: DonorFeatureIntroVariant | null = null;
  private layout: VariantLayout | null = null;
  private boardPreviewActive = false;
  private requestToken = 0;

  constructor() {
    super();
    this.visible = false;
    this.eventMode = "none";
    this.alpha = 0;

    this.heroGlow.anchor.set(0.5);
    this.heroGlow.tint = 0xffd36b;
    this.heroGlow.blendMode = "add";
    this.heroGlow.visible = false;
    this.heroCoin.anchor.set(0.5);
    this.heroCoin.visible = false;
    this.titleSprite.anchor.set(0.5, 0);
    this.plateSprite.anchor.set(0.5);
    this.previewBoard.anchor.set(0.5);
    this.titleFallback.anchor.set(0.5, 0);
    this.titleFallback.visible = false;
    this.captionTop.anchor.set(0.5, 0);
    this.captionBottom.anchor.set(0.5, 0);
    this.continueText.anchor.set(0.5, 0.5);

    this.addChild(this.scrim, this.bottomShade, this.topGlow, this.boardGlow, this.boardFrame);
    this.addChild(this.titleSprite, this.titleFallback, this.plateSprite, this.previewBoard);
    this.previewSymbols.forEach((sprite) => this.addChild(sprite));
    this.addChild(this.heroGlow, this.heroCoin);
    this.rowIcons.forEach((sprite) => this.addChild(sprite));
    this.movingGlow.forEach((sprite) => this.addChild(sprite));
    this.movingIcons.forEach((sprite) => this.addChild(sprite));
    this.addChild(this.captionTop, this.captionBottom, this.continueText);

  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    super.destroy(options);
  }

  public resize(width: number, height: number): void {
    this.viewportWidth = width;
    this.viewportHeight = height;
    this.layoutScene();
  }

  public clear(): void {
    // Invalidate in-flight async `play` operations so stale completions
    // cannot re-show the overlay over a newer scene owner.
    this.requestToken += 1;
    this.activeVariant = null;
    this.layout = null;
    this.boardPreviewActive = false;
    this.visible = false;
    this.alpha = 0;
    this.heroCoin.visible = false;
    this.heroGlow.visible = false;
    this.titleSprite.visible = false;
    this.plateSprite.visible = false;
    this.previewBoard.visible = false;
    this.titleFallback.visible = false;
    this.captionTop.text = "";
    this.captionBottom.text = "";
    this.continueText.visible = false;
    this.previewSymbols.forEach((sprite) => {
      sprite.visible = false;
      sprite.alpha = 0;
    });
    this.rowIcons.forEach((sprite) => {
      sprite.visible = false;
      sprite.alpha = 0;
    });
    this.movingIcons.forEach((sprite) => {
      sprite.visible = false;
      sprite.alpha = 0;
    });
    this.movingGlow.forEach((sprite) => {
      sprite.visible = false;
      sprite.alpha = 0;
    });
  }

  public update(deltaMs: number): void {
    this.tick(deltaMs);
  }

  public async play(
    variant: DonorFeatureIntroVariant,
    options: IntroPreviewOptions = {},
  ): Promise<void> {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      this.clear();
      return;
    }

    const requestToken = ++this.requestToken;
    const assets = await DonorFeatureIntroOverlay.loadAssets();
    const previewTextures = await this.resolvePreviewTextures(
      options.symbolGrid ?? null,
      variant,
      assets,
    );
    if (requestToken !== this.requestToken) {
      return;
    }

    this.activeVariant = variant;
    this.layout = this.buildVariantLayout(variant, assets);
    this.holdMs =
      options.holdMs ??
      (variant === "ultimate" ? 1960 : variant === "power" ? 1440 : 1220);
    this.elapsedMs = 0;
    this.visible = true;
    this.alpha = 0;

    this.titleSprite.texture = assets.titles[variant] ?? Texture.EMPTY;
    this.titleSprite.visible = variant !== "power" && this.titleSprite.texture !== Texture.EMPTY;
    this.titleFallback.visible = !this.titleSprite.visible && variant !== "power";
    this.titleFallback.text = this.layout.heading;
    this.plateSprite.texture = assets.plates[variant] ?? Texture.EMPTY;
    this.plateSprite.visible = variant !== "power" && this.plateSprite.texture !== Texture.EMPTY;
    this.previewBoard.texture = assets.board ?? Texture.EMPTY;
    this.previewBoard.visible = variant === "power" && this.previewBoard.texture !== Texture.EMPTY;
    this.captionTop.text = this.layout.captionTop;
    this.captionTop.visible = variant !== "power" && this.layout.captionTop.length > 0;
    this.captionBottom.text = this.layout.captionBottom;
    this.captionBottom.visible = variant !== "power" && this.layout.captionBottom.length > 0;
    this.continueText.visible = variant !== "power";
    this.boardPreviewActive = previewTextures.some((texture) => texture !== null);
    this.heroCoin.texture =
      variant === "power"
        ? assets.icons.strike ?? assets.icons.superStrike ?? Texture.EMPTY
        : Texture.EMPTY;
    this.heroCoin.visible = variant === "power" && this.heroCoin.texture !== Texture.EMPTY;
    this.heroGlow.visible = this.heroCoin.visible;
    this.previewSymbols.forEach((sprite, index) => {
      const texture = previewTextures[index] ?? null;
      sprite.texture = texture ?? Texture.EMPTY;
      sprite.visible = texture !== null;
      sprite.alpha = texture ? 1 : 0;
    });

    this.rowIcons.forEach((sprite, index) => {
      const texture = this.layout?.iconRow[index] ?? null;
      sprite.texture = texture ?? Texture.EMPTY;
      sprite.visible = variant !== "power" && texture !== null;
    });

    this.movingIcons.forEach((sprite, index) => {
      const config = this.layout?.movers[index];
      sprite.texture = config?.texture ?? Texture.EMPTY;
      sprite.visible =
        variant !== "power" && !this.boardPreviewActive && config?.texture !== null;
    });

    this.layoutScene();
  }

  private tick(deltaMs: number): void {
    if (!this.visible || !this.activeVariant || !this.layout) {
      return;
    }

    const stepMs = Math.min(deltaMs, 96);
    this.elapsedMs += stepMs;
    const fadeIn = clamp01(this.elapsedMs / 180);
    const fadeOut =
      this.elapsedMs > this.holdMs - 260
        ? 1 - clamp01((this.elapsedMs - (this.holdMs - 260)) / 260)
        : 1;
    this.alpha = Math.min(fadeIn, fadeOut);

    if (this.elapsedMs >= this.holdMs) {
      this.clear();
      return;
    }

    const centerX = this.viewportWidth * 0.5;
    const iconRowY = Math.max(162, this.viewportHeight * 0.19);
    const iconSpacing = this.boardPreviewActive ? 72 : 78;
    this.rowIcons.forEach((sprite, index) => {
      if (!sprite.visible) {
        return;
      }
      const pulse = Math.sin(this.elapsedMs / 220 + index * 0.72);
      sprite.x = centerX + (index - (this.rowIcons.length - 1) * 0.5) * iconSpacing;
      sprite.y = iconRowY + pulse * 4;
      const size = this.boardPreviewActive ? (index === 0 ? 78 : 54) : index === 0 ? 86 : 62;
      sprite.width = size;
      sprite.height = size;
      sprite.alpha = 0.88 + pulse * 0.06;
      sprite.rotation = pulse * 0.04;
    });

    this.previewSymbols.forEach((sprite, index) => {
      if (!sprite.visible) {
        return;
      }
      const pulse = 0.96 + Math.sin(this.elapsedMs / 260 + index * 0.44) * 0.04;
      sprite.alpha = 0.94 + Math.sin(this.elapsedMs / 220 + index * 0.52) * 0.06;
      sprite.scale.set(pulse);
    });

    const boardCenterY = this.viewportHeight * 0.53;
    const isPower = this.activeVariant === "power";
    this.topGlow.clear();
    if (isPower) {
      this.topGlow.ellipse(centerX, boardCenterY - 46, 312, 124);
      this.topGlow.fill({ color: 0xffd56a, alpha: 0.18 + fadeIn * 0.2 });
    } else {
      this.topGlow.ellipse(centerX, iconRowY + 10, 314, 82);
      this.topGlow.fill({ color: 0xffba42, alpha: 0.08 + fadeIn * 0.12 });
    }

    this.boardGlow.clear();
    if (isPower) {
      this.boardGlow.roundRect(
        centerX - this.previewBoard.width * 0.58,
        boardCenterY - this.previewBoard.height * 0.58,
        this.previewBoard.width * 1.16,
        this.previewBoard.height * 1.14,
        36,
      );
      this.boardGlow.fill({ color: 0xffd86e, alpha: 0.16 + fadeIn * 0.16 });
    } else {
      this.boardGlow.ellipse(centerX, boardCenterY + 176, 236, 74);
      this.boardGlow.fill({ color: 0xffbc48, alpha: 0.12 + fadeIn * 0.08 });
    }

    this.movingIcons.forEach((sprite, index) => {
      const config = this.layout?.movers[index];
      const glow = this.movingGlow[index];
      if (!config || !sprite.visible) {
        sprite.visible = false;
        glow.visible = false;
        return;
      }

      const introProgress = easeOutCubic((this.elapsedMs - (config.delayMs ?? 0)) / 760);
      const hover = Math.sin(this.elapsedMs / 210 + index * 0.8) * 5;
      const x = config.startX + (config.endX - config.startX) * introProgress;
      const y = config.startY + (config.endY - config.startY) * introProgress + hover;
      const scale = config.scale * (0.82 + introProgress * 0.18 + Math.sin(this.elapsedMs / 260 + index) * 0.03);

      sprite.x = x;
      sprite.y = y;
      sprite.width = 104 * scale;
      sprite.height = 104 * scale;
      sprite.rotation = config.rotation * (1 - introProgress * 0.25) + Math.sin(this.elapsedMs / 420 + index) * 0.04;
      sprite.alpha = 0.84 + introProgress * 0.16;

      glow.visible = true;
      glow.x = x;
      glow.y = y;
      glow.width = sprite.width * 1.16;
      glow.height = sprite.height * 1.16;
      glow.alpha = 0.08 + introProgress * 0.12;
    });

    if (this.heroCoin.visible) {
      const pulse = 1 + Math.sin(this.elapsedMs / 150) * 0.04;
      this.heroCoin.scale.set(pulse);
      this.heroCoin.alpha = 0.96;
      this.heroGlow.alpha = 0.16 + fadeIn * 0.22 + Math.sin(this.elapsedMs / 110) * 0.04;
    }

    this.continueText.alpha =
      0.28 +
      clamp01((this.elapsedMs - Math.max(0, this.holdMs - 520)) / 360) * 0.72 *
        (0.88 + Math.sin(this.elapsedMs / 140) * 0.12);
  }

  private layoutScene(): void {
    if (this.viewportWidth <= 0 || this.viewportHeight <= 0) {
      return;
    }

    const isPower = this.activeVariant === "power";

    this.scrim.clear();
    this.scrim.rect(0, 0, this.viewportWidth, this.viewportHeight);
    this.scrim.fill({ color: 0x140305, alpha: isPower ? 0.52 : 0.84 });

    this.bottomShade.clear();
    this.bottomShade.rect(0, this.viewportHeight * 0.78, this.viewportWidth, this.viewportHeight * 0.22);
    this.bottomShade.fill({ color: 0x050102, alpha: isPower ? 0.42 : 0.66 });

    const centerX = this.viewportWidth * 0.5;
    const titleY = Math.max(20, this.viewportHeight * 0.028);
    const iconRowY = isPower ? Math.max(118, this.viewportHeight * 0.142) : Math.max(132, this.viewportHeight * 0.165);
    const captionTopY = Math.max(204, this.viewportHeight * 0.218);
    const boardY = isPower ? this.viewportHeight * 0.49 : this.viewportHeight * 0.47;

    this.titleSprite.x = centerX;
    this.titleSprite.y = titleY;
    this.titleSprite.scale.set(0.95);

    this.titleFallback.x = centerX;
    this.titleFallback.y = titleY;

    this.plateSprite.x = centerX;
    this.plateSprite.y = iconRowY;
    this.plateSprite.width = isPower ? 0 : 520;
    this.plateSprite.height = isPower ? 0 : 182;
    this.plateSprite.alpha = isPower ? 0 : 0.54;

    this.previewBoard.x = centerX;
    this.previewBoard.y = boardY;
    this.previewBoard.width = isPower
      ? Math.min(492, this.viewportWidth * 0.48)
      : Math.min(410, this.viewportWidth * 0.39);
    this.previewBoard.height = isPower
      ? Math.min(532, this.viewportHeight * 0.69)
      : Math.min(420, this.viewportHeight * 0.33);
    this.previewBoard.alpha = isPower ? 0.56 : 0.28;

    this.captionTop.x = centerX;
    this.captionTop.y = captionTopY;
    this.captionBottom.x = centerX;
    this.captionBottom.y = boardY + this.previewBoard.height * 0.5 + 22;
    this.continueText.x = centerX;
    this.continueText.y = this.viewportHeight - 46;

    const boardWidth = this.previewBoard.width;
    const boardHeight = this.previewBoard.height;
    const boardLeft = centerX - boardWidth * 0.5;
    const boardTop = boardY - boardHeight * 0.5;
    const cornerRadius = 14;

    this.boardFrame.clear();
    this.boardFrame.roundRect(boardLeft, boardTop, boardWidth, boardHeight, cornerRadius);
    this.boardFrame.fill({ color: 0x24106c, alpha: isPower ? 0.9 : 0.98 });
    this.boardFrame.stroke({ color: 0xffe56c, width: isPower ? 7 : 5, alpha: 1 });
    this.boardFrame.moveTo(boardLeft + boardWidth / 3, boardTop + 10);
    this.boardFrame.lineTo(boardLeft + boardWidth / 3, boardTop + boardHeight - 10);
    this.boardFrame.moveTo(boardLeft + (boardWidth * 2) / 3, boardTop + 10);
    this.boardFrame.lineTo(boardLeft + (boardWidth * 2) / 3, boardTop + boardHeight - 10);
    this.boardFrame.stroke({ color: 0xe7ce67, width: isPower ? 3 : 2, alpha: isPower ? 0.42 : 0.34 });

    const cellWidth = boardWidth / 3;
    const cellHeight = boardHeight / 4;
    this.previewSymbols.forEach((sprite, index) => {
      const column = index % 3;
      const row = Math.floor(index / 3);
      sprite.x = boardLeft + cellWidth * (column + 0.5);
      sprite.y = boardTop + cellHeight * (row + 0.5);
      const maxWidth = cellWidth * (isPower ? 0.68 : 0.58);
      const maxHeight = cellHeight * (isPower ? 0.62 : 0.54);
      const sourceWidth = sprite.texture.orig.width;
      const sourceHeight = sprite.texture.orig.height;
      const fitScale = Math.min(maxWidth / Math.max(1, sourceWidth), maxHeight / Math.max(1, sourceHeight));
      sprite.width = sourceWidth * fitScale;
      sprite.height = sourceHeight * fitScale;
    });

    this.heroCoin.x = centerX;
    this.heroCoin.y = boardTop + boardHeight - Math.min(72, boardHeight * 0.12);
    this.heroCoin.width = isPower ? 116 : 0;
    this.heroCoin.height = isPower ? 116 : 0;
    this.heroGlow.x = this.heroCoin.x;
    this.heroGlow.y = this.heroCoin.y + 2;
    this.heroGlow.width = isPower ? 214 : 0;
    this.heroGlow.height = isPower ? 214 : 0;
    if (!isPower) {
      this.heroCoin.visible = false;
      this.heroGlow.visible = false;
    }

    if (this.layout) {
      this.layout.movers.forEach((config, index) => {
        const relative = this.resolveVariantBoardPosition(index, this.layout!.movers.length);
        config.startX = centerX + (index - (this.layout!.movers.length - 1) * 0.5) * 70;
        config.startY = iconRowY - 4 - (index % 2) * 12;
        config.endX = boardLeft + relative.x * this.previewBoard.width;
        config.endY = boardTop + relative.y * this.previewBoard.height;
      });
    }
  }

  private resolveVariantBoardPosition(index: number, total: number): { x: number; y: number } {
    if (total <= 1) {
      return { x: 0.5, y: 0.5 };
    }
    if (total === 2) {
      return index === 0 ? { x: 0.5, y: 0.34 } : { x: 0.5, y: 0.66 };
    }
    if (total === 3) {
      return [
        { x: 0.5, y: 0.28 },
        { x: 0.5, y: 0.52 },
        { x: 0.5, y: 0.76 },
      ][index] ?? { x: 0.5, y: 0.5 };
    }
    return [
      { x: 0.32, y: 0.28 },
      { x: 0.68, y: 0.28 },
      { x: 0.32, y: 0.68 },
      { x: 0.68, y: 0.68 },
    ][index] ?? { x: 0.5, y: 0.5 };
  }

  private buildVariantLayout(
    variant: DonorFeatureIntroVariant,
    assets: LoadedOverlayAssets,
  ): VariantLayout {
    if (variant === "blitz") {
      return {
        heading: "BLITZ STRIKE\nBONUS GAME",
        captionTop: "Start fast with 1+ Chicken Coins on the reels.",
        captionBottom: "Blitz entry keeps the donor feature handoff short and direct.",
        iconRow: [
          assets.icons.strike,
          assets.icons.bonus,
          assets.icons.x5,
          assets.icons.x10,
          assets.icons.minor,
          assets.icons.major,
        ],
        movers: [
          {
            texture: assets.icons.strike,
            startX: 0,
            startY: 0,
            endX: 0,
            endY: 0,
            scale: 1.02,
            rotation: -0.08,
            delayMs: 60,
          },
          {
            texture: assets.icons.x10,
            startX: 0,
            startY: 0,
            endX: 0,
            endY: 0,
            scale: 0.88,
            rotation: 0.05,
            delayMs: 150,
          },
          {
            texture: assets.icons.major,
            startX: 0,
            startY: 0,
            endX: 0,
            endY: 0,
            scale: 0.82,
            rotation: 0.04,
            delayMs: 240,
          },
        ],
      };
    }

    if (variant === "power") {
      return {
        heading: "POWER STRIKE\nBONUS GAME",
        captionTop: "Power Strike Coins guarantee stronger collector hits.",
        captionBottom: "The Chicken Coin collects Coins that land on the screen.",
        iconRow: [
          assets.icons.strike,
          assets.icons.x10,
          assets.icons.x15,
          assets.icons.major,
          assets.icons.grand,
          assets.icons.superStrike,
        ],
        movers: [
          {
            texture: assets.icons.x15,
            startX: 0,
            startY: 0,
            endX: 0,
            endY: 0,
            scale: 0.92,
            rotation: 0.08,
            delayMs: 60,
          },
          {
            texture: assets.icons.strike,
            startX: 0,
            startY: 0,
            endX: 0,
            endY: 0,
            scale: 1.06,
            rotation: -0.06,
            delayMs: 140,
          },
          {
            texture: assets.icons.major,
            startX: 0,
            startY: 0,
            endX: 0,
            endY: 0,
            scale: 0.84,
            rotation: 0.04,
            delayMs: 220,
          },
        ],
      };
    }

    return {
      heading: "ULTIMATE STRIKE\nBONUS GAME",
      captionTop: "All Super Chicken Coins, guaranteed Boost.",
      captionBottom:
        "The Super Chicken Coin collects all Coins on\nthe screen, including Chicken Coins",
      iconRow: [
        assets.icons.superStrike,
        assets.icons.grand,
        assets.icons.major,
        assets.icons.x15,
        assets.icons.x10,
        assets.icons.bonus,
      ],
      movers: [
        {
          texture: assets.icons.x10,
          startX: 0,
          startY: 0,
          endX: 0,
          endY: 0,
          scale: 0.92,
          rotation: 0.04,
          delayMs: 40,
        },
        {
          texture: assets.icons.superStrike,
          startX: 0,
          startY: 0,
          endX: 0,
          endY: 0,
          scale: 1.18,
          rotation: -0.08,
          delayMs: 140,
        },
        {
          texture: assets.icons.major,
          startX: 0,
          startY: 0,
          endX: 0,
          endY: 0,
          scale: 0.86,
          rotation: 0.08,
          delayMs: 260,
        },
      ],
    };
  }

  private static async loadAssets(): Promise<LoadedOverlayAssets> {
    if (!DonorFeatureIntroOverlay.assetsPromise) {
      DonorFeatureIntroOverlay.assetsPromise = (async () => {
        const manifestUrl = resolveDonorManifestUrl();
        const resolveAtlas = (source: string, cachePrefix: string) =>
          resolveMappedSourceTexture({
            source,
            baseUrl: manifestUrl,
            cachePrefix,
          });
        const resolveDirect = async (source: string): Promise<Texture | null> => {
          const resolved = new URL(source, manifestUrl).toString();
          await Assets.load(resolved);
          return Texture.from(resolved);
        };

        const [
          textBlitz,
          textPower,
          textUltimate,
          bgBlitz,
          bgPower,
          bgUltimate,
          superStrike,
          strike,
          bonus,
          major,
          minor,
          mini,
          grand,
          x2,
          x3,
          x5,
          x10,
          x15,
          cherry,
          watermelon,
          lemon,
          orange,
          bell,
          bar,
          board,
        ] = await Promise.all([
          resolveAtlas("../anims_v5/slot_render.atlas#slot/blitz_strike", "donor-intro:title-blitz"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/text_power", "donor-intro:title-power"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/text_ultimate", "donor-intro:title-ultimate"),
          resolveAtlas("../anims_v5/slot_render.atlas#slot/background_red2", "donor-intro:bg-blitz"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/bg_power", "donor-intro:bg-power"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/bg_ultimate", "donor-intro:bg-ultimate"),
          resolveDirect("../image/img_super_strike_coin.d11daf84.png"),
          resolveDirect("../image/img_strike_coin.9ea8c29f.png"),
          resolveDirect("../image/img_bonus_coin.2529fcb8.png"),
          resolveDirect("../image/img_major_coin.b2d60eb9.png"),
          resolveDirect("../image/img_minor_coin.fea1c83b.png"),
          resolveDirect("../image/img_mini_coin.e12ee50e.png"),
          resolveDirect("../image/img_grand_coin.68e52468.png"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/ico2", "donor-intro:ico2"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/ico3", "donor-intro:ico3"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/ico5", "donor-intro:ico5"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/ico10", "donor-intro:ico10"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/ico15", "donor-intro:ico15"),
          resolveDirect("../image/chery.7c79c57e.png"),
          resolveDirect("../image/watermelon.cc2cfaf0.png"),
          resolveDirect("../image/lemon.5f70c041.png"),
          resolveDirect("../image/orange.a5ac7051.png"),
          resolveDirect("../image/bell.d508a0aa.png"),
          resolveDirect("../image/bar.2f5353fa.png"),
          this.loadBoardCrop(manifestUrl),
        ]);

        return {
          titles: {
            blitz: textBlitz,
            power: textPower,
            ultimate: textUltimate,
          },
          plates: {
            blitz: bgBlitz ?? bgPower,
            power: bgPower,
            ultimate: bgUltimate,
          },
          board,
          icons: {
            superStrike,
            strike,
            bonus,
            major,
            minor,
            mini,
            grand,
            x2,
            x3,
            x5,
            x10,
            x15,
            cherry,
            watermelon,
            lemon,
            orange,
            bell,
            bar,
          },
        };
      })();
    }

    return DonorFeatureIntroOverlay.assetsPromise;
  }

  private static async loadBoardCrop(manifestUrl: string): Promise<Texture | null> {
    const boardUrl = new URL("../image/bonus.fb5969d4.png", manifestUrl).toString();
    const image = await this.loadImage(boardUrl);
    if (!image) {
      return null;
    }

    const crop = {
      x: 992,
      y: 0,
      width: 1039,
      height: 586,
    };
    const canvas = document.createElement("canvas");
    canvas.width = crop.width;
    canvas.height = crop.height;
    const context = canvas.getContext("2d");
    if (!context) {
      return null;
    }
    context.drawImage(
      image,
      crop.x,
      crop.y,
      crop.width,
      crop.height,
      0,
      0,
      crop.width,
      crop.height,
    );
    return Texture.from(canvas);
  }

  private static async loadImage(url: string): Promise<HTMLImageElement | null> {
    return await new Promise((resolve) => {
      const image = new Image();
      image.crossOrigin = "anonymous";
      image.onload = () => resolve(image);
      image.onerror = () => resolve(null);
      image.src = url;
    });
  }

  private async resolvePreviewTextures(
    symbolGrid: number[][] | null,
    variant: DonorFeatureIntroVariant,
    assets: LoadedOverlayAssets,
  ): Promise<Array<Texture | null>> {
    if (variant === "power") {
      return await this.resolveDonorPowerPreviewTextures(assets);
    }

    if (!symbolGrid) {
      return Array.from({ length: 12 }, () => null);
    }

    const flatIds = symbolGrid.flatMap((row) => row).slice(0, 12);
    const textures = await Promise.all(
      flatIds.map((symbolId) => this.resolvePreviewTexture(symbolId, variant, assets)),
    );
    while (textures.length < 12) {
      textures.push(null);
    }
    return textures;
  }

  private async resolvePreviewTexture(
    symbolId: number,
    variant: DonorFeatureIntroVariant,
    assets: LoadedOverlayAssets,
  ): Promise<Texture | null> {
    if (symbolId <= 6) {
      return null;
    }
    if (symbolId === 7) {
      return assets.icons.bonus ?? null;
    }
    if (symbolId === 8) {
      return variant === "ultimate"
        ? assets.icons.superStrike ?? assets.icons.strike ?? null
        : assets.icons.strike ?? null;
    }
    if (symbolId === 9) {
      return variant === "blitz"
        ? assets.icons.major ?? assets.icons.bonus ?? null
        : assets.icons.superStrike ?? assets.icons.major ?? null;
    }

    const normalized =
      ((symbolId % CRAZY_ROOSTER_SYMBOL_FRAME_KEYS.length) + CRAZY_ROOSTER_SYMBOL_FRAME_KEYS.length) %
      CRAZY_ROOSTER_SYMBOL_FRAME_KEYS.length;
    const cached = DonorFeatureIntroOverlay.previewTexturePromises.get(normalized);
    if (cached) {
      return await cached;
    }

    const promise = (async () => {
      try {
        const frameKey = CRAZY_ROOSTER_SYMBOL_FRAME_KEYS[normalized];
        const resolved = await resolveProviderFrameTexture("symbolAtlas", frameKey);
        return resolved.texture;
      } catch {
        return null;
      }
    })();
    DonorFeatureIntroOverlay.previewTexturePromises.set(normalized, promise);
    return await promise;
  }

  private async resolveDonorPowerPreviewTextures(
    assets: LoadedOverlayAssets,
  ): Promise<Array<Texture | null>> {
    const x5 = await DonorFeatureIntroOverlay.resolveDonorCoinPreviewTexture("slot/num5");
    const x7 = await DonorFeatureIntroOverlay.resolveDonorCoinPreviewTexture("slot/num7");
    return [
      assets.icons.cherry ?? null,
      assets.icons.watermelon ?? assets.icons.orange ?? null,
      x7,
      assets.icons.cherry ?? null,
      assets.icons.watermelon ?? assets.icons.orange ?? null,
      x5,
      x5,
      assets.icons.watermelon ?? assets.icons.orange ?? null,
      x5,
      assets.icons.lemon ?? null,
      null,
      assets.icons.bar ?? null,
    ];
  }

  private static async resolveDonorCoinPreviewTexture(
    numFrameKey: "slot/num5" | "slot/num7",
  ): Promise<Texture | null> {
    const cacheKey = `donor-coin-preview:${numFrameKey}`;
    const cached = DonorFeatureIntroOverlay.composedCoinPromises.get(cacheKey);
    if (cached) {
      return await cached;
    }

    const loading = (async () => {
      const manifestUrl = resolveDonorManifestUrl();
      const [glowTexture, coinTexture, blueTexture, numTexture] = await Promise.all([
        resolveMappedSourceTexture({
          source: "../anims_v5/coins_render.atlas#slot/sweet_light/sweetlight_0012",
          baseUrl: manifestUrl,
          cachePrefix: `${cacheKey}:glow`,
          atlasOrigin: "top-left",
          rasterizeFrame: true,
        }),
        resolveMappedSourceTexture({
          source: "../anims_v5/coins_render.atlas#slot/coin1",
          baseUrl: manifestUrl,
          cachePrefix: `${cacheKey}:coin`,
          atlasOrigin: "top-left",
          rasterizeFrame: true,
        }),
        numFrameKey === "slot/num7"
          ? resolveMappedSourceTexture({
              source: "../anims_v5/coins_render.atlas#slot/coin_blue",
              baseUrl: manifestUrl,
              cachePrefix: `${cacheKey}:coin-blue`,
              atlasOrigin: "top-left",
              rasterizeFrame: true,
            })
          : Promise.resolve(null),
        resolveMappedSourceTexture({
          source: `../anims_v5/coins_render.atlas#${numFrameKey}`,
          baseUrl: manifestUrl,
          cachePrefix: `${cacheKey}:num`,
          atlasOrigin: "top-left",
          rasterizeFrame: true,
        }),
      ]);
      const glowImage = glowTexture?.source.resource as CanvasImageSource | undefined;
      const coinImage = coinTexture?.source.resource as CanvasImageSource | undefined;
      const blueImage = blueTexture?.source.resource as CanvasImageSource | undefined;
      const numImage = numTexture?.source.resource as CanvasImageSource | undefined;
      if (!coinImage || !numImage) {
        return numTexture ?? coinTexture ?? glowTexture ?? null;
      }

      const canvasSize = 180;
      const canvas = document.createElement("canvas");
      canvas.width = canvasSize;
      canvas.height = canvasSize;
      const ctx = canvas.getContext("2d");
      if (!ctx) {
        return numTexture ?? coinTexture ?? glowTexture ?? null;
      }
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      if (glowImage) {
        const glowSize = canvas.width * 1.16;
        ctx.globalAlpha = 0.82;
        ctx.drawImage(
          glowImage,
          (canvas.width - glowSize) * 0.5,
          (canvas.height - glowSize) * 0.5,
          glowSize,
          glowSize,
        );
      }

      if (blueImage) {
        const blueSize = canvas.width * 0.98;
        ctx.globalAlpha = 0.38;
        ctx.drawImage(
          blueImage,
          (canvas.width - blueSize) * 0.5,
          (canvas.height - blueSize) * 0.5,
          blueSize,
          blueSize,
        );
      }

      ctx.globalAlpha = 1;
      const coinAspect = (coinTexture?.orig.width ?? 173) / Math.max(coinTexture?.orig.height ?? 171, 1);
      const coinHeight = canvas.height * 0.96;
      const coinWidth = coinHeight * coinAspect;
      ctx.drawImage(
        coinImage,
        (canvas.width - coinWidth) * 0.5,
        (canvas.height - coinHeight) * 0.5,
        coinWidth,
        coinHeight,
      );

      const numScale = numFrameKey === "slot/num7" ? 1.06 : 1.02;
      const numWidth = canvas.width * numScale;
      const numHeight = canvas.height * numScale;
      ctx.drawImage(
        numImage,
        (canvas.width - numWidth) * 0.5,
        (canvas.height - numHeight) * 0.5,
        numWidth,
        numHeight,
      );
      return Texture.from(canvas);
    })().catch(() => null);

    DonorFeatureIntroOverlay.composedCoinPromises.set(cacheKey, loading);
    return await loading;
  }
}
