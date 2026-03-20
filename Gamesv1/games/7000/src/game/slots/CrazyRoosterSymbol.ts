import { Assets, Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";
import { Spine } from "@esotericsoftware/spine-pixi-v8";
import type { Slot } from "@esotericsoftware/spine-pixi-v8";

import {
  CRAZY_ROOSTER_LAYOUT,
  CRAZY_ROOSTER_SYMBOL_FRAME_KEYS,
  CRAZY_ROOSTER_SYMBOL_LABELS,
} from "../config/CrazyRoosterGameConfig";
import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../app/assets/providerPackRegistry";

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

type DonorRoosterCoinFx = {
  coinBase: Texture;
  head: Texture;
  lightFrames: Texture[];
  headGlowFrames: Texture[];
};

export type DonorMultiplierVariantKey =
  | "x5"
  | "x7"
  | "x10"
  | "x15"
  | "major"
  | "minor"
  | "mini"
  | "grand";

const DEFAULT_SYMBOL_MOTION = {
  cycleDuration: 2.1167,
  headScaleBase: 0.985,
  headScaleAmplitude: 0.026,
  headRotationAmplitude: 0.046,
  headBobAmplitude: 1.4,
  headBobOffsetY: 1.8,
  lightAlphaBase: 0.22,
  lightAlphaAmplitude: 0.32,
} as const;

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

export class CrazyRoosterSymbol extends Container {
  private static donorRoosterCoinFxPromise: Promise<DonorRoosterCoinFx | null> | null = null;
  private static donorMultiplierAssetsPromise: Promise<void> | null = null;

  private readonly coinGlow = new Graphics();
  private readonly shadow = new Graphics();
  private readonly backing = new Graphics();
  private readonly highlight = new Graphics();
  private readonly sprite = new Sprite();
  private readonly roosterCoinLayer = new Container();
  private readonly roosterCoinLight = new Sprite(Texture.EMPTY);
  private readonly roosterCoinHeadGlow = new Sprite(Texture.EMPTY);
  private readonly roosterCoinHead = new Sprite(Texture.EMPTY);
  private readonly roosterCoinSweep = new Graphics();
  private readonly roosterCoinSweepMask = new Graphics();
  private donorMultiplierSpine: Spine | null = null;
  private donorMultiplierVariant: DonorMultiplierVariantKey | null = null;
  private donorMultiplierSuppressedSlots: Slot[] = [];
  private readonly labelText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 30,
      fontWeight: "800",
      fill: 0xffffff,
      stroke: { color: 0x100f13, width: 4 },
      align: "center",
    },
  });
  private readonly showDebugLabels =
    new URLSearchParams(window.location.search).get("debugSymbolLabels") === "1";
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);
  private textureRequestToken = 0;
  private readonly coinVariantSeed = Math.floor(Math.random() * 997);
  private glowTime = Math.random() * Math.PI * 2;
  private roosterCoinAnimTime = Math.random() * 2.1167;
  private multiplierGlowActive = false;
  private coinGlowBaseAlpha = 0;
  private roosterCoinFxActive = false;
  private roosterCoinLightFrames: Texture[] = [];
  private roosterCoinHeadGlowFrames: Texture[] = [];
  private donorVariantOverride: DonorMultiplierVariantKey | null = null;

  public symbolId = -1;

  constructor() {
    super();

    this.sprite.anchor.set(0.5);
    this.sprite.x = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
    this.sprite.y = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
    this.layoutSpriteForTexture(Texture.WHITE, false);
    this.sprite.alpha = 1;

    const centerX = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
    const centerY = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
    this.roosterCoinLight.anchor.set(0.5);
    this.roosterCoinLight.position.set(centerX, centerY);
    this.roosterCoinLight.width = CRAZY_ROOSTER_LAYOUT.symbolWidth * 1.02;
    this.roosterCoinLight.height = CRAZY_ROOSTER_LAYOUT.symbolHeight * 1.02;
    this.roosterCoinLight.alpha = 0.32;
    this.roosterCoinLight.tint = 0xffefb0;
    this.roosterCoinLight.blendMode = "add";
    this.roosterCoinHeadGlow.anchor.set(0.5);
    this.roosterCoinHeadGlow.position.set(centerX, centerY + 2);
    this.roosterCoinHeadGlow.width = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.68;
    this.roosterCoinHeadGlow.height = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.68;
    this.roosterCoinHeadGlow.alpha = 0;
    this.roosterCoinHeadGlow.blendMode = "add";
    this.roosterCoinHead.anchor.set(0.5);
    this.roosterCoinHead.position.set(centerX, centerY + 1.5);
    this.roosterCoinHead.width = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.73;
    this.roosterCoinHead.height = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.75;
    this.roosterCoinSweep.blendMode = "add";
    this.roosterCoinSweepMask.circle(centerX, centerY, CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.35);
    this.roosterCoinSweepMask.fill({ color: 0xffffff, alpha: 1 });
    this.roosterCoinSweepMask.renderable = false;
    this.roosterCoinSweep.mask = this.roosterCoinSweepMask;
    this.roosterCoinLayer.visible = false;
    this.roosterCoinLayer.addChild(
      this.roosterCoinLight,
      this.roosterCoinSweep,
      this.roosterCoinHeadGlow,
      this.roosterCoinHead,
      this.roosterCoinSweepMask,
    );

    this.addChild(this.coinGlow);
    this.coinGlow.blendMode = "add";
    this.coinGlow.pivot.set(centerX, centerY);
    this.coinGlow.position.set(centerX, centerY);
    this.addChild(this.shadow);
    this.addChild(this.backing);
    this.addChild(this.sprite);
    this.addChild(this.roosterCoinLayer);
    this.addChild(this.highlight);

    this.labelText.anchor.set(0.5);
    this.labelText.x = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
    this.labelText.y = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
    this.labelText.visible = this.showDebugLabels;
    this.addChild(this.labelText);
    Ticker.shared.add(this.motionTicker);
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public setSymbol(id: number): void {
    const normalized =
      ((id % CRAZY_ROOSTER_LAYOUT.symbolCount) + CRAZY_ROOSTER_LAYOUT.symbolCount) %
      CRAZY_ROOSTER_LAYOUT.symbolCount;

    this.symbolId = normalized;
    this.disableDonorMultiplierFx();
    this.disableRoosterCoinFx();
    const palette = this.resolvePalette(normalized);
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const isMultiplierCoin = normalized === 7 || normalized === 8;
    const usesDonorMultiplierSpine =
      isDonorlocal && (normalized === 7 || normalized === 8);
    this.multiplierGlowActive = isMultiplierCoin && !usesDonorMultiplierSpine;
    const donorCoinGlowBaseAlpha =
      normalized === 7
        ? 0.5
        : normalized === 8
          ? 0.22
          : 0.16;
    this.coinGlowBaseAlpha = isDonorlocal ? donorCoinGlowBaseAlpha : 0.24;
    this.coinGlow.clear();
    if (isMultiplierCoin) {
      const glowAlpha = this.coinGlowBaseAlpha;
      const centerX = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
      const centerY = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
      const outerRadiusX = CRAZY_ROOSTER_LAYOUT.symbolWidth * (isDonorlocal ? 0.5 : 0.46);
      const outerRadiusY = CRAZY_ROOSTER_LAYOUT.symbolHeight * (isDonorlocal ? 0.5 : 0.46);
      const midRadiusX = CRAZY_ROOSTER_LAYOUT.symbolWidth * (isDonorlocal ? 0.42 : 0.4);
      const midRadiusY = CRAZY_ROOSTER_LAYOUT.symbolHeight * (isDonorlocal ? 0.42 : 0.4);

      const isStrikeCoin = normalized === 8;
      this.coinGlow.ellipse(centerX, centerY, outerRadiusX, outerRadiusY);
      this.coinGlow.fill({
        color: isDonorlocal
          ? isStrikeCoin
            ? 0xffb22f
            : 0xffca42
          : 0xffd457,
        alpha: glowAlpha * (isDonorlocal ? (isStrikeCoin ? 0.14 : 0.32) : 0.58),
      });
      this.coinGlow.ellipse(centerX, centerY, midRadiusX, midRadiusY);
      this.coinGlow.fill({
        color: isDonorlocal ? 0xffedb4 : 0xfff3b4,
        alpha: glowAlpha * (isDonorlocal ? (isStrikeCoin ? 0.05 : 0.12) : 0.34),
      });
      if (!isDonorlocal) {
        this.coinGlow.ellipse(centerX, centerY, midRadiusX * 0.92, midRadiusY * 0.92);
        this.coinGlow.stroke({
          color: 0xffefad,
          width: 2.8,
          alpha: 0.38,
        });
      }
      if (isDonorlocal) {
        if (!isStrikeCoin) {
          const rayCount = 8;
          const innerRadius = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.38;
          const outerRadius = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
          for (let index = 0; index < rayCount; index += 1) {
            const angle = (index / rayCount) * Math.PI * 2;
            this.coinGlow.moveTo(
              centerX + Math.cos(angle) * innerRadius,
              centerY + Math.sin(angle) * innerRadius,
            );
            this.coinGlow.lineTo(
              centerX + Math.cos(angle) * outerRadius,
              centerY + Math.sin(angle) * outerRadius,
            );
          }
          this.coinGlow.stroke({
            color: 0xffd56a,
            width: 2.4,
            alpha: glowAlpha * 0.08,
            cap: "round",
          });
        }
      } else {
        const rayCount = 8;
        const innerRadius = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.3;
        const outerRadius = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.44;
        for (let index = 0; index < rayCount; index += 1) {
          const angle = (index / rayCount) * Math.PI * 2;
          this.coinGlow.moveTo(
            centerX + Math.cos(angle) * innerRadius,
            centerY + Math.sin(angle) * innerRadius,
          );
          this.coinGlow.lineTo(
            centerX + Math.cos(angle) * outerRadius,
            centerY + Math.sin(angle) * outerRadius,
          );
        }
        this.coinGlow.stroke({
          color: 0xffd259,
          width: 2.2,
          alpha: 0.16,
        });
      }
      this.coinGlow.alpha = glowAlpha;
      this.coinGlow.scale.set(1);
      this.coinGlow.rotation = 0;
    } else {
      this.coinGlow.alpha = 0;
      this.coinGlow.scale.set(1);
      this.coinGlow.rotation = 0;
    }

    this.shadow.clear();
    this.shadow.roundRect(
      6,
      8,
      CRAZY_ROOSTER_LAYOUT.symbolWidth - 12,
      CRAZY_ROOSTER_LAYOUT.symbolHeight - 10,
      22,
    );
    this.shadow.fill({ color: 0x000000, alpha: 0.1 });
    this.backing.clear();
    this.backing.roundRect(
      0,
      0,
      CRAZY_ROOSTER_LAYOUT.symbolWidth,
      CRAZY_ROOSTER_LAYOUT.symbolHeight,
      22,
    );
    this.backing.fill({ color: palette.frame, alpha: 0.72 });
    this.backing.stroke({ color: palette.border, width: 3, alpha: 0.6 });
    this.highlight.clear();
    this.highlight.roundRect(
      8,
      8,
      CRAZY_ROOSTER_LAYOUT.symbolWidth - 16,
      34,
      18,
    );
    this.highlight.fill({ color: 0xffffff, alpha: 0.06 });
    this.setSpriteTexture(Texture.WHITE, palette.fill, false);
    this.labelText.text =
      isDonorlocal && normalized === 4
        ? "MELON"
        : CRAZY_ROOSTER_SYMBOL_LABELS[normalized] ?? String(normalized);
    this.labelText.style.fill = palette.text;
    if (isDonorlocal) {
      this.shadow.visible = false;
      this.backing.visible = false;
      this.highlight.visible = false;
    }

    if (
      isDonorlocal &&
      normalized === 9 &&
      (this.donorVariantOverride === "major" ||
        this.donorVariantOverride === "minor" ||
        this.donorVariantOverride === "mini" ||
        this.donorVariantOverride === "grand")
    ) {
      this.setSpriteTexture(Texture.EMPTY);
      this.shadow.visible = false;
      this.backing.visible = false;
      this.highlight.visible = false;
      void this.applyDonorJackpotTexture(normalized, this.donorVariantOverride);
      return;
    }

    if (isDonorlocal && normalized === 9) {
      this.setSpriteTexture(Texture.EMPTY);
      this.shadow.visible = false;
      this.backing.visible = false;
      this.highlight.visible = false;
      void this.enableRoosterCoinFx(normalized);
      return;
    }

    void this.applyResolvedTexture(normalized, palette.fill);
    if (usesDonorMultiplierSpine) {
      void this.enableDonorMultiplierFx(normalized);
    }
  }

  public setDonorVariantOverride(variant: DonorMultiplierVariantKey | null): void {
    this.donorVariantOverride = variant;
  }

  public applyDonorVariantOverride(variant: DonorMultiplierVariantKey | null): void {
    if (this.donorVariantOverride === variant) {
      return;
    }
    this.donorVariantOverride = variant;
    if (this.symbolId === 7 || this.symbolId === 8 || this.symbolId === 9) {
      this.setSymbol(this.symbolId);
    }
  }

  private resolvePalette(symbolId: number): {
    frame: number;
    fill: number;
    border: number;
    text: number;
  } {
    const donorFallbackPalette = [
      0xa1171f,
      0xd36c11,
      0xdb8b1c,
      0x7451c6,
      0x2d8f84,
      0x2f6ad8,
      0x4b4b4b,
      0xc2a03d,
      0x8e0d13,
      0xf3d24e,
    ];
    const fill = donorFallbackPalette[symbolId] ?? 0x404040;

    return {
      frame: 0x15080a,
      fill,
      border: 0xc7141a,
      text: 0xffffff,
    };
  }

  private async applyResolvedTexture(symbolId: number, fallbackTint: number): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const frameKey = CRAZY_ROOSTER_SYMBOL_FRAME_KEYS[symbolId];
    const provider = getProviderPackStatus().effectiveProvider;
    let resolved: Awaited<ReturnType<typeof resolveProviderFrameTexture>> | { texture: Texture | null };

    if (provider === "donorlocal" && symbolId <= 6) {
      if (symbolId === 0) {
        const donorManifestUrl = resolveDonorManifestUrl();
        const donorBellUrl = new URL("../image/bell.d508a0aa.png", donorManifestUrl).toString();
        try {
          await Assets.load(donorBellUrl);
          resolved = {
            texture: Texture.from(donorBellUrl),
            resolvedProvider: "donorlocal",
            fallbackUsed: false,
          };
        } catch {
          resolved = { texture: null };
        }
      } else {
        const donorManifestUrl = resolveDonorManifestUrl();
        const donorSymbolImages: Partial<Record<number, string>> = {
          1: "../image/chery.7c79c57e.png",
          2: "../image/lemon.5f70c041.png",
          3: "../image/orange.a5ac7051.png",
          4: "../image/watermelon.cc2cfaf0.png",
          5: "../image/bar.2f5353fa.png",
          6: "../image/777.26770b3f.png",
        };
        const donorStaticImage = donorSymbolImages[symbolId];
        if (donorStaticImage) {
          const donorStaticUrl = new URL(donorStaticImage, donorManifestUrl).toString();
          try {
            await Assets.load(donorStaticUrl);
            resolved = {
              texture: Texture.from(donorStaticUrl),
              resolvedProvider: "donorlocal",
              fallbackUsed: false,
            };
          } catch {
            resolved = { texture: null };
          }
        } else {
          resolved = { texture: null };
        }
      }
    } else {
      resolved = { texture: null };
    }

    if (provider === "donorlocal" && (symbolId === 7 || symbolId === 8)) {
      if (requestToken !== this.textureRequestToken || this.symbolId !== symbolId) {
        return;
      }
      this.setSpriteTexture(Texture.EMPTY);
      this.shadow.visible = false;
      this.backing.visible = false;
      this.highlight.visible = false;
      return;
    }
    if (!resolved.texture) {
      resolved = frameKey
        ? await resolveProviderFrameTexture("symbolAtlas", frameKey)
        : { texture: null };
    }
    if (provider === "donorlocal" && (symbolId === 7 || symbolId === 8)) {
      const donorCoinImages = [
        "../image/img_bonus_coin.2529fcb8.png",
        "../image/img_strike_coin.9ea8c29f.png",
        "../image/img_super_strike_coin.d11daf84.png",
        "../image/img_major_coin.b2d60eb9.png",
        "../image/img_minor_coin.fea1c83b.png",
        "../image/img_mini_coin.e12ee50e.png",
      ] as const;
      const donorManifestUrl = resolveDonorManifestUrl();
      const pinnedCoinById: Partial<Record<number, string>> = {
        8: "../image/img_strike_coin.9ea8c29f.png",
      };
      const pinned = pinnedCoinById[symbolId];
      if (pinned) {
        const pinnedUrl = new URL(pinned, donorManifestUrl).toString();
        try {
          await Assets.load(pinnedUrl);
          resolved = {
            texture: Texture.from(pinnedUrl),
            resolvedProvider: "donorlocal",
            fallbackUsed: false,
          };
        } catch {
          resolved = { texture: null };
        }
      }

      if (!resolved.texture) {
        const startIndex = (this.coinVariantSeed + symbolId) % donorCoinImages.length;
        for (let offset = 0; offset < donorCoinImages.length; offset += 1) {
          const candidateUrl = new URL(
            donorCoinImages[(startIndex + offset) % donorCoinImages.length],
            donorManifestUrl,
          ).toString();
          try {
            await Assets.load(candidateUrl);
            resolved = {
              texture: Texture.from(candidateUrl),
              resolvedProvider: "donorlocal",
              fallbackUsed: false,
            };
            break;
          } catch {
            // Try the next donor coin candidate.
          }
        }
      }
    }

    if (requestToken !== this.textureRequestToken || this.symbolId !== symbolId) {
      return;
    }

    if (resolved.texture) {
      const stretchToCell =
        provider === "donorlocal" &&
        symbolId >= 0 &&
        symbolId <= 6;
      this.setSpriteTexture(resolved.texture, 0xffffff, !stretchToCell);
      this.shadow.visible = false;
      this.backing.visible = false;
      this.highlight.visible = false;
      return;
    }

    this.setSpriteTexture(Texture.WHITE, fallbackTint, false);
    const keepGenericBacking = provider !== "donorlocal" || !resolved.texture;
    this.shadow.visible = keepGenericBacking;
    this.backing.visible = keepGenericBacking;
    this.highlight.visible = keepGenericBacking;
  }

  private async applyDonorJackpotTexture(
    symbolId: number,
    variant: Extract<DonorMultiplierVariantKey, "mini" | "minor" | "major" | "grand">,
  ): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const donorManifestUrl = resolveDonorManifestUrl();
    const donorJackpotImages: Record<typeof variant, string> = {
      mini: "../image/img_mini_coin.e12ee50e.png",
      minor: "../image/img_minor_coin.fea1c83b.png",
      major: "../image/img_major_coin.b2d60eb9.png",
      grand: "../image/img_grand_coin.68e52468.png",
    };
    const donorUrl = new URL(donorJackpotImages[variant], donorManifestUrl).toString();

    try {
      await Assets.load(donorUrl);
    } catch {
      return;
    }

    if (requestToken !== this.textureRequestToken || this.symbolId !== symbolId) {
      return;
    }

    this.disableRoosterCoinFx();
    this.setSpriteTexture(Texture.from(donorUrl));
    this.shadow.visible = false;
    this.backing.visible = false;
    this.highlight.visible = false;
  }

  private async enableRoosterCoinFx(symbolId: number): Promise<void> {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return;
    }

    const fx = await CrazyRoosterSymbol.loadDonorRoosterCoinFx();
    if (!fx) {
      if (this.symbolId !== symbolId || symbolId !== 9) {
        return;
      }
      this.roosterCoinFxActive = false;
      this.roosterCoinLayer.visible = false;
      this.setSpriteTexture(Texture.EMPTY);
      this.shadow.visible = false;
      this.backing.visible = false;
      this.highlight.visible = false;
      return;
    }

    if (this.symbolId !== symbolId || symbolId !== 9) {
      return;
    }

    this.roosterCoinFxActive = true;
    this.roosterCoinLightFrames = fx.lightFrames;
    this.roosterCoinHeadGlowFrames = fx.headGlowFrames;
    this.setSpriteTexture(fx.coinBase);
    this.roosterCoinHead.texture = fx.head;
    this.roosterCoinLayer.visible = true;
    this.shadow.visible = false;
    this.backing.visible = false;
    this.highlight.visible = false;
  }

  private async enableDonorMultiplierFx(symbolId: number): Promise<void> {
    const variant = this.resolveDonorMultiplierVariant(symbolId);
    const spine = await this.ensureDonorMultiplierSpine(variant);
    if (!spine || this.symbolId !== symbolId) {
      return;
    }

    this.roosterCoinFxActive = false;
    this.roosterCoinLayer.visible = false;
    this.setSpriteTexture(Texture.EMPTY);
    this.shadow.visible = false;
    this.backing.visible = false;
    this.highlight.visible = false;
    this.coinGlow.alpha = 0;

    spine.visible = true;
    spine.x = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
    spine.y = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5 + 1;
    const scale =
      variant === "x15" ? 0.328 : variant === "x10" ? 0.322 : 0.316;
    spine.scale.set(scale);
    spine.state.setAnimation(0, "start", false);
    spine.state.addAnimation(0, "idle", true, 0);
  }

  private disableRoosterCoinFx(): void {
    this.roosterCoinFxActive = false;
    this.roosterCoinLayer.visible = false;
    this.roosterCoinHead.rotation = 0;
    this.roosterCoinHead.scale.set(1);
    this.roosterCoinHeadGlow.alpha = 0;
    this.roosterCoinSweep.clear();
  }

  private disableDonorMultiplierFx(): void {
    if (this.donorMultiplierSpine) {
      this.donorMultiplierSpine.visible = false;
      this.hideDonorMultiplierSuppressedSlots();
    }
  }

  private setSpriteTexture(
    texture: Texture,
    tint = 0xffffff,
    preserveAspect = true,
  ): void {
    this.sprite.texture = texture;
    this.sprite.tint = tint;
    this.layoutSpriteForTexture(texture, preserveAspect);
  }

  private layoutSpriteForTexture(texture: Texture, preserveAspect: boolean): void {
    const inset = 4;
    const maxWidth = CRAZY_ROOSTER_LAYOUT.symbolWidth - inset * 2;
    const maxHeight = CRAZY_ROOSTER_LAYOUT.symbolHeight - inset * 2;
    this.sprite.x = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
    this.sprite.y = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;

    if (texture === Texture.EMPTY) {
      this.sprite.scale.set(0);
      return;
    }

    const nativeWidth = texture.frame.width > 0 ? texture.frame.width : 1;
    const nativeHeight = texture.frame.height > 0 ? texture.frame.height : 1;

    if (!preserveAspect) {
      this.sprite.scale.set(maxWidth / nativeWidth, maxHeight / nativeHeight);
      return;
    }

    const fitScale = Math.min(maxWidth / nativeWidth, maxHeight / nativeHeight);
    this.sprite.scale.set(fitScale);
  }

  private resolveDonorMultiplierVariant(symbolId: number): DonorMultiplierVariantKey {
    if (this.donorVariantOverride) {
      return this.donorVariantOverride;
    }
    if (symbolId === 8) {
      return "x10";
    }
    if (symbolId === 9) {
      return "x15";
    }
    return this.coinVariantSeed % 2 === 0 ? "x5" : "x7";
  }

  private async ensureDonorMultiplierSpine(
    variant: DonorMultiplierVariantKey,
  ): Promise<Spine | null> {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return null;
    }

    const donorManifestUrl = resolveDonorManifestUrl();
    const atlasUrl = new URL("../anims_v5/coins_render.atlas", donorManifestUrl).toString();
    const skeletonFile =
      variant === "major" || variant === "minor" || variant === "mini" || variant === "grand"
        ? `../anims_v5/icon_coin_${variant}.json`
        : `../anims_v5/icon_coin_${variant}.json`;
    const skeletonUrl = new URL(skeletonFile, donorManifestUrl).toString();

    if (!CrazyRoosterSymbol.donorMultiplierAssetsPromise) {
      CrazyRoosterSymbol.donorMultiplierAssetsPromise = Assets.load([atlasUrl]).then(() => undefined);
    }

    try {
      await CrazyRoosterSymbol.donorMultiplierAssetsPromise;
      await Assets.load([skeletonUrl]);
    } catch {
      return null;
    }

    if (this.donorMultiplierSpine && this.donorMultiplierVariant === variant) {
      return this.donorMultiplierSpine;
    }

    if (this.donorMultiplierSpine) {
      if (this.donorMultiplierSpine.parent === this) {
        this.removeChild(this.donorMultiplierSpine);
      }
      this.donorMultiplierSpine.destroy();
      this.donorMultiplierSpine = null;
    }

    try {
      const spine = Spine.from({
        skeleton: skeletonUrl,
        atlas: atlasUrl,
        autoUpdate: true,
      });
      spine.visible = false;
      this.addChildAt(spine, this.getChildIndex(this.sprite) + 1);
      this.donorMultiplierSpine = spine;
      this.donorMultiplierVariant = variant;
      this.captureDonorMultiplierSuppressedSlots(variant);
      return spine;
    } catch {
      return null;
    }
  }

  private captureDonorMultiplierSuppressedSlots(variant: DonorMultiplierVariantKey): void {
    if (!this.donorMultiplierSpine) {
      this.donorMultiplierSuppressedSlots = [];
      return;
    }

    const suppressedNames =
      variant === "x7" || variant === "x10" || variant === "x15"
        ? new Set(["wave", "light2", "blick_coin", "blick_num"])
        : variant === "major"
          ? new Set(["wave", "light", "blick", "blick_num2"])
          : new Set<string>();

    this.donorMultiplierSuppressedSlots = this.donorMultiplierSpine.skeleton.slots.filter((slot) =>
      suppressedNames.has(slot.data.name),
    );
  }

  private hideDonorMultiplierSuppressedSlots(): void {
    for (const slot of this.donorMultiplierSuppressedSlots) {
      slot.color.a = 0;
      slot.setAttachment(null);
    }
  }

  private static async loadDonorRoosterCoinFx(): Promise<DonorRoosterCoinFx | null> {
    if (!CrazyRoosterSymbol.donorRoosterCoinFxPromise) {
      CrazyRoosterSymbol.donorRoosterCoinFxPromise = (async () => {
        const donorManifestUrl = resolveDonorManifestUrl();
        const atlasUrl = new URL("../anims_v5/coins_render.atlas", donorManifestUrl).toString();
        const atlasImageUrl = new URL("../anims_v5/coins_render.png", donorManifestUrl).toString();

        try {
          const response = await fetch(atlasUrl);
          if (!response.ok) {
            return null;
          }
          const atlasText = await response.text();
          const atlasImage = await CrazyRoosterSymbol.loadImageSource(atlasImageUrl);
          const frames = CrazyRoosterSymbol.parseAtlasFrames(atlasText);

          const coinBase = CrazyRoosterSymbol.createAtlasTexture(frames.get("slot/coin1"), atlasImage);
          const head = CrazyRoosterSymbol.createAtlasTexture(
            frames.get("slot/ico_chicken_head"),
            atlasImage,
          );

          if (!coinBase || !head) {
            return null;
          }

          const lightFrames: Texture[] = [];
          for (let index = 1; index <= 26; index += 1) {
            const frameName = `slot/sweet_light/sweetlight_${String(index).padStart(4, "0")}`;
            const texture = CrazyRoosterSymbol.createAtlasTexture(frames.get(frameName), atlasImage);
            if (texture) {
              lightFrames.push(texture);
            }
          }

          const headGlowFrames: Texture[] = [];
          for (let index = 5; index <= 26; index += 1) {
            const frameName = `slot/glow_head/glow_head_${String(index).padStart(4, "0")}`;
            const texture = CrazyRoosterSymbol.createAtlasTexture(frames.get(frameName), atlasImage);
            if (texture) {
              headGlowFrames.push(texture);
            }
          }

          return {
            coinBase,
            head,
            lightFrames,
            headGlowFrames,
          };
        } catch {
          return null;
        }
      })();
    }

    return CrazyRoosterSymbol.donorRoosterCoinFxPromise;
  }

  private static async loadImageSource(url: string): Promise<HTMLImageElement> {
    return await new Promise<HTMLImageElement>((resolve, reject) => {
      const image = new Image();
      image.decoding = "async";
      image.onload = () => resolve(image);
      image.onerror = () => reject(new Error(`Failed to load donor image: ${url}`));
      image.src = url;
    });
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
        if (!row) {
          index += 1;
          break;
        }
        if (!row.includes(":")) {
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
    atlasImage: CanvasImageSource,
  ): Texture | null {
    if (!frame) {
      return null;
    }

    const canvas = document.createElement("canvas");
    canvas.width = Math.max(1, frame.originalWidth);
    canvas.height = Math.max(1, frame.originalHeight);
    const context = canvas.getContext("2d");
    if (!context) {
      return null;
    }

    if (!frame.rotated) {
      context.drawImage(
        atlasImage,
        frame.x,
        frame.y,
        frame.width,
        frame.height,
        frame.offsetX,
        frame.offsetY,
        frame.width,
        frame.height,
      );
    } else {
      context.save();
      context.translate(frame.offsetX, frame.offsetY + frame.width);
      context.rotate(-Math.PI / 2);
      context.drawImage(
        atlasImage,
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

  private tick(deltaMs: number): void {
    const symbolMotionPreset = DEFAULT_SYMBOL_MOTION;
    if (this.roosterCoinFxActive) {
      this.roosterCoinAnimTime += deltaMs / 1000;
      const cycleDuration = Math.max(0.001, symbolMotionPreset.cycleDuration);
      const phase = (this.roosterCoinAnimTime % cycleDuration) / cycleDuration;
      const phaseRad = phase * Math.PI * 2;

      if (this.roosterCoinLightFrames.length > 0) {
        const lightIndex = Math.floor(phase * this.roosterCoinLightFrames.length) %
          this.roosterCoinLightFrames.length;
        this.roosterCoinLight.texture = this.roosterCoinLightFrames[lightIndex];
      }
      this.roosterCoinLight.alpha =
        symbolMotionPreset.lightAlphaBase +
        (Math.sin(phaseRad) * 0.5 + 0.5) * symbolMotionPreset.lightAlphaAmplitude;

      const headScale =
        symbolMotionPreset.headScaleBase +
        Math.sin(phaseRad) * symbolMotionPreset.headScaleAmplitude;
      this.roosterCoinHead.scale.set(headScale);
      this.roosterCoinHead.rotation =
        Math.sin(phaseRad - Math.PI * 0.34) * symbolMotionPreset.headRotationAmplitude;
      this.roosterCoinHead.y =
        CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5 +
        symbolMotionPreset.headBobOffsetY +
        Math.sin(phaseRad) * symbolMotionPreset.headBobAmplitude;

      if (this.roosterCoinHeadGlowFrames.length > 0) {
        const glowWindowStart = 0.47;
        const glowWindowEnd = 0.93;
        if (phase >= glowWindowStart && phase <= glowWindowEnd) {
          const glowProgress = (phase - glowWindowStart) / (glowWindowEnd - glowWindowStart);
          const glowIndex = Math.min(
            this.roosterCoinHeadGlowFrames.length - 1,
            Math.floor(glowProgress * this.roosterCoinHeadGlowFrames.length),
          );
          this.roosterCoinHeadGlow.texture = this.roosterCoinHeadGlowFrames[glowIndex];
          this.roosterCoinHeadGlow.alpha = 0.08 + Math.sin(glowProgress * Math.PI) * 0.36;
        } else {
          this.roosterCoinHeadGlow.alpha = 0;
        }
      }

      const sweepWindowStart = 0.24;
      const sweepWindowEnd = 0.74;
      this.roosterCoinSweep.clear();
      if (phase >= sweepWindowStart && phase <= sweepWindowEnd) {
        const sweepProgress = (phase - sweepWindowStart) / (sweepWindowEnd - sweepWindowStart);
        const centerX = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
        const centerY = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
        const sweepHeight = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.76;
        const sweepWidth = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.12;
        const sweepX = centerX - CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.52 +
          sweepProgress * CRAZY_ROOSTER_LAYOUT.symbolWidth * 1.04;
        const sweepAlpha = Math.sin(sweepProgress * Math.PI) * 0.35;
        this.roosterCoinSweep.moveTo(sweepX - sweepWidth * 0.92, centerY - sweepHeight * 0.5);
        this.roosterCoinSweep.lineTo(sweepX + sweepWidth * 0.26, centerY - sweepHeight * 0.5);
        this.roosterCoinSweep.lineTo(sweepX + sweepWidth * 1.05, centerY + sweepHeight * 0.5);
        this.roosterCoinSweep.lineTo(sweepX - sweepWidth * 0.12, centerY + sweepHeight * 0.5);
        this.roosterCoinSweep.closePath();
        this.roosterCoinSweep.fill({ color: 0xffffff, alpha: sweepAlpha });
        this.roosterCoinSweep.moveTo(sweepX - sweepWidth * 0.58, centerY - sweepHeight * 0.5);
        this.roosterCoinSweep.lineTo(sweepX + sweepWidth * 0.01, centerY - sweepHeight * 0.5);
        this.roosterCoinSweep.lineTo(sweepX + sweepWidth * 0.74, centerY + sweepHeight * 0.5);
        this.roosterCoinSweep.lineTo(sweepX + sweepWidth * 0.13, centerY + sweepHeight * 0.5);
        this.roosterCoinSweep.closePath();
        this.roosterCoinSweep.fill({ color: 0xfff0c4, alpha: sweepAlpha * 0.8 });
      }
    }

    if (this.multiplierGlowActive) {
      this.glowTime += deltaMs / 1000;
      const pulse = 0.72 + Math.sin(this.glowTime * 4.6 + this.coinVariantSeed * 0.01) * 0.28;
      const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
      const isStrikeCoin = this.symbolId === 8;
      this.coinGlow.alpha = this.coinGlowBaseAlpha * (
        0.72 + pulse * (isDonorlocal ? (isStrikeCoin ? 0.42 : 0.76) : 0.44)
      );
      this.coinGlow.scale.set(
        isDonorlocal
          ? isStrikeCoin
            ? 1 + pulse * 0.028
            : 1 + pulse * 0.06
          : 0.98 + pulse * 0.05,
      );
      this.coinGlow.rotation = 0;
    }

    if (this.donorMultiplierSpine?.visible) {
      this.hideDonorMultiplierSuppressedSlots();
    }
  }
}
