import {
  Assets,
  Container,
  FederatedPointerEvent,
  Graphics,
  Sprite,
  Text,
  Texture,
} from "pixi.js";

import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
} from "../../app/assets/providerPackRegistry";
import { resolveMappedSourceTexture } from "../../app/assets/mappedSourceTextureResolver";

export type DonorBuyBonusVariant = "blitz" | "power" | "ultimate";

export type DonorBuyBonusOption = {
  id: string;
  variant: DonorBuyBonusVariant;
  priceMinor: number;
  priceMultiplier: number;
};

type LoadedModalAssets = {
  backgrounds: Record<DonorBuyBonusVariant, Texture | null>;
  titles: Record<DonorBuyBonusVariant, Texture | null>;
  button: Texture | null;
  fire: Texture | null;
  glare: Texture | null;
  icons: Record<
    | "strike"
    | "superStrike"
    | "mini"
    | "minor"
    | "major"
    | "grand"
    | "x2"
    | "x3"
    | "x5"
    | "x10"
    | "x15",
    Texture | null
  >;
};

type CardView = {
  root: Container;
  frame: Graphics;
  background: Sprite;
  glare: Sprite;
  badge: Graphics;
  badgeText: Text;
  title: Sprite;
  titleFallback: Text;
  description: Text;
  priceButton: Sprite;
  priceLabel: Text;
  flames: Sprite[];
  icons: Sprite[];
};

type CardLayout = {
  width: number;
  height: number;
  centerX: number;
  centerY: number;
};

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

const CARD_VARIANT_META: Record<
  DonorBuyBonusVariant,
  {
    fallbackTitle: string;
    description: string;
    volatility: number;
    iconKeys: Array<keyof LoadedModalAssets["icons"]>;
  }
> = {
  blitz: {
    fallbackTitle: "BLITZ STRIKE",
    description: "Start fast with 1+ Chicken Coins on the reels",
    volatility: 2,
    iconKeys: ["strike", "strike"],
  },
  power: {
    fallbackTitle: "POWER STRIKE",
    description: "One or more Chicken Coins. Only high-value Coins",
    volatility: 3,
    iconKeys: ["x5", "x10", "x15", "strike", "strike", "mini", "major", "minor", "grand"],
  },
  ultimate: {
    fallbackTitle: "ULTIMATE STRIKE",
    description: "All Super Chicken Coins, guaranteed Boost.",
    volatility: 4,
    iconKeys: ["x10", "x15", "x3", "x2", "superStrike", "mini", "major", "minor", "grand"],
  },
};

export class DonorBuyBonusModal extends Container {
  private static assetsPromise: Promise<LoadedModalAssets> | null = null;
  private static composedCoinPromises = new Map<string, Promise<Texture | null>>();

  private readonly scrim = new Graphics();
  private readonly titleText = new Text({
    text: "Buy bonus:",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 34,
      fontWeight: "900",
      fill: 0xffffff,
      align: "center",
    },
  });
  private readonly closeText = new Text({
    text: "×",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 44,
      fontWeight: "700",
      fill: 0xffffff,
      align: "center",
    },
  });
  private readonly cards = Array.from({ length: 3 }, () => this.createCard());
  private viewportWidth = 0;
  private viewportHeight = 0;
  private elapsedMs = 0;
  private options: DonorBuyBonusOption[] = [];
  private resolveSelection: ((optionId: string | null) => void) | null = null;

  constructor() {
    super();
    this.visible = false;
    this.alpha = 0;
    this.eventMode = "none";

    this.scrim.eventMode = "static";
    this.scrim.on("pointertap", () => this.finish(null));

    this.titleText.anchor.set(0.5, 0);
    this.closeText.anchor.set(0.5);
    this.closeText.eventMode = "static";
    this.closeText.cursor = "pointer";
    this.closeText.on("pointertap", (event: FederatedPointerEvent) => {
      event.stopPropagation();
      this.finish(null);
    });

    this.addChild(this.scrim, this.titleText, this.closeText);
    this.cards.forEach((card) => this.addChild(card.root));
  }

  public resize(width: number, height: number): void {
    this.viewportWidth = width;
    this.viewportHeight = height;
    this.layout();
  }

  public update(deltaMs: number): void {
    if (!this.visible) {
      return;
    }

    this.elapsedMs += Math.min(deltaMs, 48);
    this.alpha = 1;
    this.cards.forEach((card, index) => {
      if (!card.root.visible) {
        return;
      }
      const pulse = Math.sin(this.elapsedMs / 220 + index * 0.7);
      card.glare.alpha = 0.12 + (pulse + 1) * 0.12;
      card.priceButton.alpha = 0.88 + (pulse + 1) * 0.04;
      card.icons.forEach((icon, iconIndex) => {
        if (!icon.visible) {
          return;
        }
        const wobble = Math.sin(this.elapsedMs / 260 + index * 0.8 + iconIndex * 0.55);
        icon.alpha = 0.94 + wobble * 0.04;
      });
    });
  }

  public clear(): void {
    if (this.resolveSelection) {
      this.resolveSelection(null);
      this.resolveSelection = null;
    }
    this.options = [];
    this.visible = false;
    this.eventMode = "none";
    this.alpha = 0;
  }

  public async open(options: DonorBuyBonusOption[]): Promise<string | null> {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return null;
    }

    this.clear();
    this.options = options.slice(0, 3);
    if (this.options.length === 0) {
      return null;
    }

    const assets = await DonorBuyBonusModal.loadAssets();
    this.elapsedMs = 0;
    this.visible = true;
    this.eventMode = "static";

    this.cards.forEach((card, index) => {
      const option = this.options[index];
      if (!option) {
        card.root.visible = false;
        return;
      }
      card.root.visible = true;
      this.applyOptionToCard(card, option, assets);
    });

    this.layout();

    return await new Promise<string | null>((resolve) => {
      this.resolveSelection = resolve;
    });
  }

  private finish(optionId: string | null): void {
    const resolve = this.resolveSelection;
    this.resolveSelection = null;
    this.visible = false;
    this.eventMode = "none";
    this.alpha = 0;
    resolve?.(optionId);
  }

  private createCard(): CardView {
    const root = new Container();
    root.eventMode = "static";
    root.cursor = "pointer";

    const frame = new Graphics();
    const background = new Sprite(Texture.EMPTY);
    background.anchor.set(0.5);

    const glare = new Sprite(Texture.WHITE);
    glare.anchor.set(0.5);
    glare.tint = 0xffd465;
    glare.blendMode = "add";

    const badge = new Graphics();

    const badgeText = new Text({
      text: "VOLATILITY",
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 11,
        fontWeight: "900",
        fill: 0xffffff,
        align: "center",
        letterSpacing: 0.5,
      },
    });
    badgeText.anchor.set(0.5);

    const title = new Sprite(Texture.EMPTY);
    title.anchor.set(0.5, 0);

    const titleFallback = new Text({
      text: "",
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 34,
        fontWeight: "900",
        fill: 0xffd95d,
        stroke: { color: 0x53101d, width: 8 },
        align: "center",
      },
    });
    titleFallback.anchor.set(0.5, 0);

    const description = new Text({
      text: "",
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 17,
        fontWeight: "800",
        fill: 0xfff7df,
        stroke: { color: 0x4a0b0d, width: 4 },
        align: "center",
        wordWrap: true,
        wordWrapWidth: 360,
        lineHeight: 18,
      },
    });
    description.anchor.set(0.5, 0.5);

    const priceButton = new Sprite(Texture.WHITE);
    priceButton.anchor.set(0.5);
    priceButton.tint = 0xffca44;
    priceButton.alpha = 0.96;

    const priceLabel = new Text({
      text: "",
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 28,
        fontWeight: "900",
        fill: 0x8f200f,
        align: "center",
      },
    });
    priceLabel.anchor.set(0.5);

    const flames = Array.from({ length: 4 }, () => {
      const sprite = new Sprite(Texture.EMPTY);
      sprite.anchor.set(0.5);
      return sprite;
    });

    const icons = Array.from({ length: 9 }, () => {
      const sprite = new Sprite(Texture.EMPTY);
      sprite.anchor.set(0.5);
      return sprite;
    });

    root.addChild(
      frame,
      background,
      glare,
      badge,
      badgeText,
      title,
      titleFallback,
      description,
      priceButton,
      priceLabel,
    );
    flames.forEach((sprite) => root.addChild(sprite));
    icons.forEach((sprite) => root.addChild(sprite));

    return {
      root,
      frame,
      background,
      glare,
      badge,
      badgeText,
      title,
      titleFallback,
      description,
      priceButton,
      priceLabel,
      flames,
      icons,
    };
  }

  private layout(): void {
    if (this.viewportWidth <= 0 || this.viewportHeight <= 0) {
      return;
    }

    this.scrim.clear();
    this.scrim.rect(0, 0, this.viewportWidth, this.viewportHeight);
    this.scrim.fill({ color: 0x140408, alpha: 0.72 });

    const centerX = this.viewportWidth * 0.5;
    const titleY = 22;
    this.titleText.x = centerX;
    this.titleText.y = titleY;
    this.closeText.x = this.viewportWidth - 56;
    this.closeText.y = 36;

    const cardWidth = Math.min(530, this.viewportWidth * 0.46);
    const topCardHeight = 124;
    const middleCardHeight = 138;
    const bottomCardHeight = 154;
    const gap = 18;
    const startY = 150;

    const layouts: CardLayout[] = [
      { width: cardWidth, height: topCardHeight, centerX, centerY: startY },
      { width: cardWidth, height: middleCardHeight, centerX, centerY: startY + topCardHeight + gap },
      {
        width: cardWidth,
        height: bottomCardHeight,
        centerX,
        centerY: startY + topCardHeight + middleCardHeight + gap * 2 + 10,
      },
    ];

    this.cards.forEach((card, index) => {
      if (!card.root.visible) {
        return;
      }
      this.layoutCard(card, layouts[index], this.options[index]);
    });
  }

  private layoutCard(
    card: CardView,
    layout: CardLayout,
    option: DonorBuyBonusOption | undefined,
  ): void {
    if (!option) {
      card.root.visible = false;
      return;
    }

    const { width, height, centerX, centerY } = layout;
    const top = centerY;
    const left = centerX - width * 0.5;
    const hasBackgroundArt =
      card.background.texture !== Texture.EMPTY && card.background.texture !== Texture.WHITE;
    const hasButtonArt =
      card.priceButton.texture !== Texture.EMPTY && card.priceButton.texture !== Texture.WHITE;
    const hasTitleArt = card.title.texture !== Texture.EMPTY;

    card.root.x = 0;
    card.root.y = 0;
    card.root.hitArea = undefined;

    card.background.x = centerX;
    card.background.y = top + height * 0.5;
    card.background.width = width;
    card.background.height = height;

    card.glare.x = centerX;
    card.glare.y = top + (option.variant === "ultimate" ? 60 : 52);
    card.glare.width =
      card.glare.texture !== Texture.WHITE ? 42 : width * 0.78;
    card.glare.height =
      card.glare.texture !== Texture.WHITE ? 108 : height * 0.24;

    const badgeWidth = 86;
    const badgeHeight = 60;
    const badgeLeft = left + 12;
    const badgeTop = top + 18;
    card.badge.clear();
    card.badge.roundRect(badgeLeft, badgeTop, badgeWidth, badgeHeight, 12);
    card.badge.fill({ color: 0x261a27, alpha: 0.95 });
    card.badge.stroke({ color: 0xffc94d, width: 2, alpha: 0.72 });
    card.badgeText.x = badgeLeft + badgeWidth * 0.5;
    card.badgeText.y = badgeTop + badgeHeight - 12;

    card.frame.clear();
    if (!hasBackgroundArt) {
      card.frame.roundRect(left, top, width, height, 18);
      card.frame.stroke({ color: 0xffc94d, width: 4, alpha: 0.88 });
    }
    if (!hasButtonArt) {
      card.frame.roundRect(centerX - 118, top + height - 34, 236, 42, 12);
      card.frame.fill({ color: 0xffcf42, alpha: 0.92 });
      card.frame.stroke({ color: 0xfff3b0, width: 2, alpha: 0.78 });
    }

    card.title.x = centerX;
    card.title.y = top + (option.variant === "ultimate" ? 4 : 8);
    card.title.width = hasTitleArt
      ? Math.min(width * 0.7, option.variant === "ultimate" ? 388 : 322)
      : Math.min(width * 0.62, option.variant === "ultimate" ? 344 : 286);
    card.title.height = hasTitleArt
      ? option.variant === "ultimate"
        ? 62
        : 42
      : option.variant === "ultimate"
        ? 54
        : 34;
    card.titleFallback.x = centerX;
    card.titleFallback.y = top + 6;

    const flamesVisible = CARD_VARIANT_META[option.variant].volatility;
    card.flames.forEach((sprite, index) => {
      sprite.visible = index < flamesVisible;
      if (!sprite.visible) {
        return;
      }
      sprite.x = badgeLeft + 18 + index * 18;
      sprite.y = badgeTop + 18;
      sprite.width = 22;
      sprite.height = 22;
      sprite.alpha = 0.96;
    });

    const iconKeys = CARD_VARIANT_META[option.variant].iconKeys;
    card.icons.forEach((sprite, index) => {
      const key = iconKeys[index];
      sprite.visible = Boolean(key && sprite.texture !== Texture.EMPTY);
      if (!key || !sprite.visible) {
        sprite.visible = false;
        return;
      }
      const placement = this.resolveIconPlacement(option.variant, index, centerX, top);
      const size = placement.size;
      sprite.x = placement.x;
      sprite.y = placement.y;
      sprite.width = size;
      sprite.height = size;
      sprite.rotation = key === "strike" || key === "superStrike" ? 0 : 0;
    });

    card.description.x = centerX;
    card.description.y = top + height - (option.variant === "ultimate" ? 38 : 34);
    card.description.style.wordWrapWidth = width - 56;

    card.priceButton.x = centerX;
    card.priceButton.y = top + height - 4;
    card.priceButton.width = hasButtonArt ? 224 : 230;
    card.priceButton.height = hasButtonArt ? 48 : 38;
    card.priceLabel.x = centerX;
    card.priceLabel.y = top + height - (hasButtonArt ? 10 : 6);

    card.root.off("pointertap");
    card.root.on("pointertap", (event: FederatedPointerEvent) => {
      event.stopPropagation();
      this.finish(option.id);
    });
  }

  private resolveIconPlacement(
    variant: DonorBuyBonusVariant,
    index: number,
    centerX: number,
    top: number,
  ): { x: number; y: number; size: number } {
    if (variant === "blitz") {
      const placements = [
        { x: centerX - 34, y: top + 48, size: 58 },
        { x: centerX + 34, y: top + 48, size: 58 },
      ];
      return placements[index] ?? { x: centerX, y: top + 48, size: 40 };
    }

    if (variant === "power") {
      const placements = [
        { x: centerX - 148, y: top + 62, size: 34 },
        { x: centerX - 114, y: top + 52, size: 38 },
        { x: centerX - 76, y: top + 62, size: 42 },
        { x: centerX - 4, y: top + 58, size: 52 },
        { x: centerX + 46, y: top + 58, size: 52 },
        { x: centerX + 146, y: top + 40, size: 36 },
        { x: centerX + 186, y: top + 40, size: 36 },
        { x: centerX + 146, y: top + 78, size: 36 },
        { x: centerX + 186, y: top + 78, size: 36 },
      ];
      return placements[index] ?? { x: centerX, y: top + 52, size: 34 };
    }

    const placements = [
      { x: centerX - 154, y: top + 64, size: 38 },
      { x: centerX - 118, y: top + 52, size: 42 },
      { x: centerX - 82, y: top + 70, size: 34 },
      { x: centerX - 46, y: top + 84, size: 34 },
      { x: centerX + 10, y: top + 62, size: 66 },
      { x: centerX + 142, y: top + 40, size: 36 },
      { x: centerX + 184, y: top + 40, size: 36 },
      { x: centerX + 142, y: top + 80, size: 36 },
      { x: centerX + 184, y: top + 80, size: 36 },
    ];
    return placements[index] ?? { x: centerX, y: top + 58, size: 34 };
  }

  private applyOptionToCard(
    card: CardView,
    option: DonorBuyBonusOption,
    assets: LoadedModalAssets,
  ): void {
    const meta = CARD_VARIANT_META[option.variant];

    card.background.texture = assets.backgrounds[option.variant] ?? Texture.WHITE;
    card.background.tint =
      card.background.texture === Texture.WHITE
        ? option.variant === "ultimate"
          ? 0x8b2a12
          : option.variant === "power"
            ? 0x5a2dc0
            : 0x4f2ab2
        : 0xffffff;
    card.background.alpha = card.background.texture === Texture.WHITE
      ? option.variant === "ultimate"
        ? 0.94
        : 0.9
      : 1;
    card.title.texture = assets.titles[option.variant] ?? Texture.EMPTY;
    card.title.visible = card.title.texture !== Texture.EMPTY;
    card.titleFallback.visible = !card.title.visible;
    card.titleFallback.text = meta.fallbackTitle;
    card.titleFallback.style = {
      ...card.titleFallback.style,
      fill: option.variant === "ultimate" ? 0xffd65a : 0xe4d6ff,
      stroke: { color: option.variant === "ultimate" ? 0x7d130a : 0x46207f, width: 8 },
    };
    card.description.text = meta.description;
    card.priceButton.texture = assets.button ?? Texture.WHITE;
    card.priceButton.tint = card.priceButton.texture === Texture.WHITE ? 0xffca44 : 0xffffff;
    card.priceButton.alpha = card.priceButton.texture === Texture.WHITE ? 0.96 : 1;
    card.priceLabel.text = Math.max(0, option.priceMinor).toLocaleString("en-US");
    card.glare.texture = assets.glare ?? Texture.WHITE;
    card.glare.tint =
      card.glare.texture === Texture.WHITE
        ? option.variant === "ultimate"
          ? 0xff8b33
          : 0xffd465
        : 0xffffff;
    card.glare.alpha = card.glare.texture === Texture.WHITE
      ? option.variant === "ultimate"
        ? 0.14
        : 0.1
      : 0.3;

    card.flames.forEach((sprite) => {
      sprite.texture = assets.fire ?? Texture.EMPTY;
    });

    meta.iconKeys.forEach((key, index) => {
      const sprite = card.icons[index];
      sprite.texture = assets.icons[key] ?? Texture.EMPTY;
      sprite.visible = assets.icons[key] !== null;
    });
    card.icons.slice(meta.iconKeys.length).forEach((sprite) => {
      sprite.texture = Texture.EMPTY;
      sprite.visible = false;
    });
  }

  private static async loadAssets(): Promise<LoadedModalAssets> {
    if (!DonorBuyBonusModal.assetsPromise) {
      DonorBuyBonusModal.assetsPromise = (async () => {
        const manifestUrl = resolveDonorManifestUrl();
        const resolveAtlas = (source: string, cachePrefix: string) =>
          resolveMappedSourceTexture({
            source,
            baseUrl: manifestUrl,
            cachePrefix,
            atlasOrigin: "top-left",
          });
        const resolveDirect = async (source: string): Promise<Texture | null> => {
          const resolved = new URL(source, manifestUrl).toString();
          await Assets.load(resolved);
          return Texture.from(resolved);
        };

        const [
          bgBlitz,
          bgPower,
          bgUltimate,
          textBlitz,
          textPower,
          textUltimate,
          button,
          fire,
          glare,
          strike,
          superStrike,
          mini,
          minor,
          major,
          grand,
          x2,
          x3,
          x5,
          x10,
          x15,
        ] = await Promise.all([
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/bg_blitz", "donor-buy:bg-blitz"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/bg_power", "donor-buy:bg-power"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/bg_ultimate", "donor-buy:bg-ultimate"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/text_blitz", "donor-buy:text-blitz"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/text_power", "donor-buy:text-power"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/text_ultimate", "donor-buy:text-ultimate"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/btn_buy_bonus", "donor-buy:button"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/fire", "donor-buy:fire"),
          resolveAtlas("../anims_v5/buy_bonus_render.atlas#buy_bonus/blick", "donor-buy:glare"),
          resolveDirect("../image/img_strike_coin.9ea8c29f.png"),
          resolveDirect("../image/img_super_strike_coin.d11daf84.png"),
          resolveDirect("../image/img_mini_coin.e12ee50e.png"),
          resolveDirect("../image/img_minor_coin.fea1c83b.png"),
          resolveDirect("../image/img_major_coin.b2d60eb9.png"),
          resolveDirect("../image/img_grand_coin.68e52468.png"),
          this.resolveRenderedMultiplierCoin("slot/num2"),
          this.resolveRenderedMultiplierCoin("slot/num3"),
          this.resolveRenderedMultiplierCoin("slot/num5"),
          this.resolveRenderedMultiplierCoin("slot/num10"),
          this.resolveRenderedMultiplierCoin("slot/num15"),
        ]);

        return {
          backgrounds: {
            blitz: bgBlitz,
            power: bgPower,
            ultimate: bgUltimate,
          },
          titles: {
            blitz: textBlitz,
            power: textPower,
            ultimate: textUltimate,
          },
          button,
          fire,
          glare,
          icons: {
            strike,
            superStrike,
            mini,
            minor,
            major,
            grand,
            x2,
            x3,
            x5,
            x10,
            x15,
          },
        };
      })();
    }
    return DonorBuyBonusModal.assetsPromise;
  }

  private static async resolveRenderedMultiplierCoin(
    numFrameKey: "slot/num2" | "slot/num3" | "slot/num5" | "slot/num10" | "slot/num15",
  ): Promise<Texture | null> {
    const cacheKey = `donor-buy-rendered:${numFrameKey}`;
    const cached = DonorBuyBonusModal.composedCoinPromises.get(cacheKey);
    if (cached) {
      return await cached;
    }

    const loading = (async () => {
      const manifestUrl = resolveDonorManifestUrl();
      const [glowTexture, coinTexture, numTexture] = await Promise.all([
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

      if (glowImage) {
        const glowSize = canvas.width * 1.14;
        ctx.globalAlpha = 0.84;
        ctx.drawImage(
          glowImage,
          (canvas.width - glowSize) * 0.5,
          (canvas.height - glowSize) * 0.5,
          glowSize,
          glowSize,
        );
      }

      ctx.globalAlpha = 1;
      const coinAspect = (coinTexture?.orig.width ?? 173) / Math.max(coinTexture?.orig.height ?? 171, 1);
      const coinHeight = canvas.height * 0.95;
      const coinWidth = coinHeight * coinAspect;
      ctx.drawImage(
        coinImage,
        (canvas.width - coinWidth) * 0.5,
        (canvas.height - coinHeight) * 0.5,
        coinWidth,
        coinHeight,
      );

      const numWidth = canvas.width * 1.02;
      const numHeight = canvas.height * 1.02;
      ctx.drawImage(
        numImage,
        (canvas.width - numWidth) * 0.5,
        (canvas.height - numHeight) * 0.5,
        numWidth,
        numHeight,
      );

      return Texture.from(canvas);
    })().catch(() => null);

    DonorBuyBonusModal.composedCoinPromises.set(cacheKey, loading);
    return await loading;
  }
}
