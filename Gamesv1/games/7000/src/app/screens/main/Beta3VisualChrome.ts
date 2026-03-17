import { Assets, Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import {
  CRAZY_ROOSTER_JACKPOTS,
} from "../../../game/config/CrazyRoosterGameConfig.ts";
import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../assets/providerPackRegistry.ts";

type ChromeModes = {
  buyLabel: string;
  autoplayActive: boolean;
  turboSelected: boolean;
  soundEnabled: boolean;
  statusText: string;
};

type ChromeReactionTone = "standard" | "collect" | "boost" | "bonus" | "jackpot";

type ChromeReactionInput = {
  tone: ChromeReactionTone;
  title: string;
  caption?: string;
  emphasis?: number;
  holdMs?: number;
  jackpotTier?: string | null;
  plaqueIndexes?: number[];
};

type JackpotCoinVisual = {
  halo: Graphics;
  sprite: Sprite;
  label: Text;
  value: Text;
  baseX: number;
  baseY: number;
  phase: number;
  heroTextureActive: boolean;
};

type DonorChromeTextures = {
  topperBackdrop: Texture | null;
  buyButton: Texture | null;
  jackpotCoins: Array<Texture | null>;
};

const readDebugProviderFlag = (): boolean =>
  new URLSearchParams(window.location.search).get("providerDebug") === "1";

const DEFAULT_TOPPER_TITLE = "JACKPOT RUN";
const DEFAULT_TOPPER_CAPTION = "ROOSTER RUSH";

export class Beta3VisualChrome extends Container {
  private static donorChromeTexturesPromise: Promise<DonorChromeTextures> | null = null;

  public static readonly padding = {
    left: 190,
    right: 190,
    top: 196,
    bottom: 96,
  } as const;

  private readonly cabinetShadow = new Graphics();
  private readonly stageAura = new Graphics();
  private readonly cabinetBackplate = new Graphics();
  private readonly cabinetGlow = new Graphics();
  private readonly topperBackdrop = new Sprite(Texture.EMPTY);
  private readonly topBar = new Graphics();
  private readonly mascotHalo = new Graphics();
  private readonly heroGlowSprite = new Sprite(Texture.WHITE);
  private readonly heroPulseSprite = new Sprite(Texture.WHITE);
  private readonly mascotSprite = new Sprite(Texture.WHITE);
  private readonly mascotCaption = new Text({
    text: "ROOSTER RUSH",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 24,
      fontWeight: "900",
      fill: 0xfff4cf,
      stroke: { color: 0x24070a, width: 4 },
      letterSpacing: 1,
      align: "center",
    },
  });
  private readonly jackpotTitle = new Text({
    text: "JACKPOT RUN",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 26,
      fontWeight: "900",
      fill: 0xffefb0,
      stroke: { color: 0x280508, width: 4 },
      letterSpacing: 2,
      align: "center",
    },
  });
  private readonly buyPanel = new Container();
  private readonly buyPanelBackground = new Graphics();
  private readonly buyPanelGlow = new Graphics();
  private readonly buyPanelSkin = new Sprite(Texture.EMPTY);
  private readonly buyIcon = new Sprite(Texture.WHITE);
  private readonly buyTitle = new Text({
    text: "BUY BONUS",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 20,
      fontWeight: "900",
      fill: 0xffedc6,
      stroke: { color: 0x1a0608, width: 4 },
      letterSpacing: 1,
      align: "center",
    },
  });
  private readonly buyValue = new Text({
    text: "BONUS 75",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 24,
      fontWeight: "900",
      fill: 0xffffff,
      stroke: { color: 0x24070a, width: 4 },
      align: "center",
    },
  });
  private readonly buyCaption = new Text({
    text: "LAUNCH HOLD & WIN",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 13,
      fontWeight: "800",
      fill: 0xffe3c2,
      stroke: { color: 0x170406, width: 2 },
      align: "center",
      letterSpacing: 0.5,
      wordWrap: true,
      wordWrapWidth: 126,
      lineHeight: 14,
    },
  });
  private readonly actionPanel = new Container();
  private readonly actionPanelBackground = new Graphics();
  private readonly actionPanelGlow = new Graphics();
  private readonly actionTitle = new Text({
    text: "ACTION STACK",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 18,
      fontWeight: "900",
      fill: 0xffefb0,
      stroke: { color: 0x1c0608, width: 4 },
      letterSpacing: 1,
      align: "center",
    },
  });
  private readonly autoState = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 16,
      fontWeight: "800",
      fill: 0xfafafa,
      stroke: { color: 0x120305, width: 3 },
    },
  });
  private readonly turboState = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 16,
      fontWeight: "800",
      fill: 0xfafafa,
      stroke: { color: 0x120305, width: 3 },
    },
  });
  private readonly soundState = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 16,
      fontWeight: "800",
      fill: 0xfafafa,
      stroke: { color: 0x120305, width: 3 },
    },
  });
  private readonly statusChip = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 13,
      fontWeight: "800",
      fill: 0xffd8d0,
      align: "center",
      wordWrap: true,
      wordWrapWidth: 148,
    },
  });
  private readonly providerBadge = new Container();
  private readonly providerBadgeBackground = new Graphics();
  private readonly providerBadgeText = new Text({
    text: "",
    style: {
      fontFamily: "monospace",
      fontSize: 12,
      fill: 0xf6f6f6,
      stroke: { color: 0x110708, width: 2 },
      wordWrap: true,
      wordWrapWidth: 200,
    },
  });
  private readonly jackpotCoins: JackpotCoinVisual[] = [];
  private readonly showProviderDebug = readDebugProviderFlag();
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);

  private machineWidth = 0;
  private machineHeight = 0;
  private ambientTime = 0;
  private boostFlash = 0;
  private buyPanelHover = 0;
  private textureRequestToken = 0;
  private modes: ChromeModes = {
    buyLabel: "BONUS 75",
    autoplayActive: false,
    turboSelected: false,
    soundEnabled: true,
    statusText: "BETONLINE READY",
  };
  private reactionTone: ChromeReactionTone = "standard";
  private reactionPulse = 0;
  private reactionHoldMs = 0;
  private plaquePulse = 0;
  private activePlaqueIndexes: number[] = [];
  private titleOverride: string | null = null;
  private captionOverride: string | null = null;

  public onBuyFeatureRequest: (() => void) | null = null;

  constructor() {
    super();

    this.mascotSprite.anchor.set(0.5, 1);
    this.mascotSprite.alpha = 0.96;
    this.topperBackdrop.anchor.set(0.5);
    this.topperBackdrop.alpha = 0.98;
    this.topperBackdrop.visible = false;
    this.heroGlowSprite.anchor.set(0.5);
    this.heroGlowSprite.alpha = 0;
    this.heroPulseSprite.anchor.set(0.5);
    this.heroPulseSprite.alpha = 0;
    this.mascotCaption.anchor.set(0.5, 0.5);
    this.jackpotTitle.anchor.set(0.5);
    this.buyPanelSkin.anchor.set(0.5);
    this.buyPanelSkin.visible = false;
    this.buyTitle.anchor.set(0.5);
    this.buyValue.anchor.set(0.5);
    this.buyCaption.anchor.set(0.5);
    this.actionTitle.anchor.set(0.5);
    this.statusChip.anchor.set(0.5, 0);

    this.addChild(
      this.stageAura,
      this.cabinetShadow,
      this.cabinetBackplate,
      this.cabinetGlow,
      this.topperBackdrop,
      this.topBar,
      this.mascotHalo,
      this.heroGlowSprite,
      this.heroPulseSprite,
      this.mascotSprite,
      this.mascotCaption,
      this.jackpotTitle,
    );

    const jackpotSpecs = [
      {
        frameKey: "coin-multiplier-2x",
        label: "MINI",
        value: `${CRAZY_ROOSTER_JACKPOTS.miniMultiplier}x`,
        phase: 0,
      },
      {
        frameKey: "coin-multiplier-3x",
        label: "MINOR",
        value: `${CRAZY_ROOSTER_JACKPOTS.minorMultiplier}x`,
        phase: Math.PI * 0.35,
      },
      {
        frameKey: "coin-multiplier-5x",
        label: "MAJOR",
        value: `${CRAZY_ROOSTER_JACKPOTS.majorMultiplier}x`,
        phase: Math.PI * 0.7,
      },
      {
        frameKey: "coin-multiplier-10x",
        label: "GRAND",
        value: `${CRAZY_ROOSTER_JACKPOTS.grandMultiplier}x`,
        phase: Math.PI,
      },
    ] as const;

    for (const spec of jackpotSpecs) {
      const halo = new Graphics();
      const sprite = new Sprite(Texture.WHITE);
      const label = new Text({
        text: spec.label,
        style: {
          fontFamily: "Trebuchet MS, Arial, sans-serif",
          fontSize: 14,
          fontWeight: "900",
          fill: 0xfff2c6,
          stroke: { color: 0x220709, width: 3 },
          align: "center",
        },
      });
      const value = new Text({
        text: spec.value,
        style: {
          fontFamily: "Trebuchet MS, Arial, sans-serif",
          fontSize: 12,
          fontWeight: "900",
          fill: 0xffffff,
          stroke: { color: 0x2a0608, width: 3 },
          align: "center",
        },
      });

      sprite.anchor.set(0.5);
      sprite.visible = false;
      label.anchor.set(0.5);
      value.anchor.set(0.5);
      this.addChild(halo, sprite, label, value);

      this.jackpotCoins.push({
        halo,
        sprite,
        label,
        value,
        baseX: 0,
        baseY: 0,
        phase: spec.phase,
        heroTextureActive: false,
      });
    }

    this.buyPanel.eventMode = "static";
    this.buyPanel.cursor = "pointer";
    this.buyPanel.on("pointertap", () => {
      this.onBuyFeatureRequest?.();
    });
    this.buyPanel.on("pointerover", () => {
      this.buyPanelHover = 1;
    });
    this.buyPanel.on("pointerout", () => {
      this.buyPanelHover = 0;
    });
    this.buyPanel.addChild(
      this.buyPanelGlow,
      this.buyPanelSkin,
      this.buyPanelBackground,
      this.buyIcon,
      this.buyTitle,
      this.buyValue,
      this.buyCaption,
    );
    this.addChild(this.buyPanel);

    this.actionPanel.addChild(
      this.actionPanelGlow,
      this.actionPanelBackground,
      this.actionTitle,
      this.autoState,
      this.turboState,
      this.soundState,
      this.statusChip,
    );
    this.addChild(this.actionPanel);

    if (this.showProviderDebug) {
      this.providerBadge.addChild(this.providerBadgeBackground, this.providerBadgeText);
      this.addChild(this.providerBadge);
    }

    this.setModeState(this.modes);
    this.redrawChrome();
    this.refreshTextures();
    Ticker.shared.add(this.motionTicker);
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public setModeState(input: Partial<ChromeModes>): void {
    this.modes = { ...this.modes, ...input };
    this.buyValue.text =
      getProviderPackStatus().effectiveProvider === "donorlocal"
        ? this.modes.buyLabel.replace(/^BONUS\s*/i, "")
        : this.modes.buyLabel;
    this.autoState.text = `AUTO  ${this.modes.autoplayActive ? "ENGAGED" : "READY"}`;
    this.turboState.text = `TURBO  ${this.modes.turboSelected ? "ARMED" : "HOLD TO FIRE"}`;
    this.soundState.text = `AUDIO  ${this.modes.soundEnabled ? "LIVE" : "MUTED"}`;
    this.statusChip.text = this.modes.statusText || "BETONLINE READY";
    this.updateProviderDebug();
    this.redrawPanels();
  }

  public resize(machineWidth: number, machineHeight: number): void {
    this.machineWidth = machineWidth;
    this.machineHeight = machineHeight;
    this.redrawChrome();
  }

  public triggerBoostPulse(): void {
    this.boostFlash = 1;
  }

  public beginSpinCycle(): void {
    this.boostFlash = Math.max(this.boostFlash, 0.18);
    this.reactionPulse = Math.max(this.reactionPulse, 0.2);
    this.reactionHoldMs = Math.max(this.reactionHoldMs, 220);
    this.plaquePulse = Math.max(this.plaquePulse, 0.16);
    this.activePlaqueIndexes = [];
    this.titleOverride = null;
    this.captionOverride = null;
    this.reactionTone = "standard";
    this.updateReactionText();
  }

  public triggerPresentationCue(input: ChromeReactionInput): void {
    this.reactionTone = input.tone;
    this.reactionPulse = Math.max(
      this.reactionPulse,
      input.emphasis ?? this.resolveReactionEmphasis(input.tone),
    );
    this.reactionHoldMs = Math.max(this.reactionHoldMs, input.holdMs ?? 780);
    this.plaquePulse = Math.max(
      this.plaquePulse,
      input.tone === "jackpot" ? 1 : input.tone === "boost" ? 0.8 : input.tone === "collect" ? 0.64 : 0.52,
    );
    this.activePlaqueIndexes =
      input.plaqueIndexes ?? this.resolvePlaqueIndexes(input.tone, input.jackpotTier);
    this.titleOverride = input.title;
    this.captionOverride = input.caption ?? DEFAULT_TOPPER_CAPTION;
    this.boostFlash = Math.max(this.boostFlash, this.reactionPulse * 0.72);
    this.updateReactionText();
  }

  public clearPresentationCue(): void {
    this.boostFlash = 0;
    this.reactionPulse = 0;
    this.reactionHoldMs = 0;
    this.plaquePulse = 0;
    this.activePlaqueIndexes = [];
    this.titleOverride = null;
    this.captionOverride = null;
    this.reactionTone = "standard";
    this.updateReactionText();
  }

  private tick(deltaMs: number): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }

    this.ambientTime += deltaMs / 1000;
    this.boostFlash = Math.max(0, this.boostFlash - deltaMs / 520);
    this.reactionPulse = Math.max(0, this.reactionPulse - deltaMs / 920);
    this.plaquePulse = Math.max(0, this.plaquePulse - deltaMs / 1120);
    this.reactionHoldMs = Math.max(0, this.reactionHoldMs - deltaMs);
    if (
      this.reactionHoldMs <= 0 &&
      this.reactionPulse <= 0.08 &&
      (this.titleOverride || this.captionOverride || this.activePlaqueIndexes.length > 0)
    ) {
      this.titleOverride = null;
      this.captionOverride = null;
      this.activePlaqueIndexes = [];
      this.reactionTone = "standard";
      this.updateReactionText();
    }

    const heroFloat = Math.sin(this.ambientTime * 1.35) * 7;
    const reactionPalette = this.resolveReactionPalette();
    const heroPulse =
      1 +
      Math.sin(this.ambientTime * 1.7) * 0.035 +
      this.boostFlash * 0.06 +
      this.reactionPulse * 0.04;
    this.mascotSprite.y = -72 + heroFloat;
    this.mascotSprite.scale.set(heroPulse);
    this.heroGlowSprite.y = -70 + heroFloat * 0.18;
    this.heroGlowSprite.scale.set(
      1.02 +
        Math.sin(this.ambientTime * 1.9) * 0.04 +
        this.boostFlash * 0.08 +
        this.reactionPulse * 0.05,
    );
    this.heroGlowSprite.alpha = 0.22 + this.boostFlash * 0.22 + this.reactionPulse * 0.12;
    this.heroGlowSprite.tint = reactionPalette.glow;
    this.heroPulseSprite.y = -80 + heroFloat * 0.12;
    this.heroPulseSprite.scale.set(
      0.98 +
        Math.sin(this.ambientTime * 2.2 + 0.4) * 0.05 +
        this.boostFlash * 0.1 +
        this.reactionPulse * 0.06,
    );
    this.heroPulseSprite.alpha = 0.14 + this.boostFlash * 0.18 + this.reactionPulse * 0.14;
    this.heroPulseSprite.tint = reactionPalette.accent;

    this.mascotHalo.clear();
    this.mascotHalo.ellipse(
      this.machineWidth * 0.5,
      -56 + heroFloat * 0.2,
      98 + this.boostFlash * 18 + this.reactionPulse * 18,
      28 + this.boostFlash * 8 + this.reactionPulse * 7,
    );
    this.mascotHalo.fill({
      color: reactionPalette.glow,
      alpha: 0.2 + this.boostFlash * 0.15 + this.reactionPulse * 0.12,
    });

    this.cabinetGlow.alpha =
      0.52 +
      Math.sin(this.ambientTime * 2.1) * 0.14 +
      this.boostFlash * 0.18 +
      this.reactionPulse * 0.12;
    this.buyPanel.scale.set(1 + this.buyPanelHover * 0.03 + Math.sin(this.ambientTime * 2.4) * 0.008);
    this.actionPanel.scale.set(1 + Math.sin(this.ambientTime * 1.8 + 0.5) * 0.01);

    this.redrawTopBar();
    this.jackpotTitle.style.fill = reactionPalette.titleFill;
    this.mascotCaption.style.fill = reactionPalette.captionFill;

    this.jackpotCoins.forEach((coin, index) => {
      const plaqueHot = this.activePlaqueIndexes.includes(index) ? 1 : 0;
      const drift = Math.sin(this.ambientTime * 1.8 + coin.phase) * 5;
      const pulse =
        1 +
        Math.sin(this.ambientTime * 2.3 + coin.phase) * 0.05 +
        this.boostFlash * 0.03 +
        plaqueHot * this.plaquePulse * 0.08;
      coin.sprite.x = coin.baseX;
      coin.sprite.y = coin.baseY + drift;
      coin.sprite.scale.set(pulse);
      coin.sprite.tint = coin.heroTextureActive ? 0xffffff : reactionPalette.coinTint;
      coin.halo.clear();
      if (coin.heroTextureActive) {
        coin.halo.circle(
          coin.baseX,
          coin.baseY + drift,
          46 + index * 2 + this.boostFlash * 6 + plaqueHot * this.plaquePulse * 14,
        );
        coin.halo.fill({
          color: reactionPalette.accent,
          alpha: 0.16 + this.boostFlash * 0.08 + plaqueHot * this.plaquePulse * 0.18,
        });
      } else {
        coin.halo.circle(
          coin.baseX,
          coin.baseY + drift,
          33 + index * 2 + this.boostFlash * 4 + plaqueHot * this.plaquePulse * 10,
        );
        coin.halo.fill({
          color: reactionPalette.coinTint,
          alpha: 0.8 + plaqueHot * this.plaquePulse * 0.24,
        });
        coin.halo.stroke({
          color: reactionPalette.accent,
          width: 4,
          alpha: 0.92 + plaqueHot * this.plaquePulse * 0.08,
        });
        coin.halo.circle(
          coin.baseX,
          coin.baseY + drift,
          24 + index * 2 + this.boostFlash * 2 + plaqueHot * this.plaquePulse * 6,
        );
        coin.halo.fill({ color: reactionPalette.shadow, alpha: 0.28 + plaqueHot * this.plaquePulse * 0.12 });
      }
      coin.label.x = coin.baseX;
      coin.label.y = coin.baseY + 10 + drift * 0.1;
      coin.label.style.fill = reactionPalette.captionFill;
      coin.label.scale.set(1 + plaqueHot * this.plaquePulse * 0.04);
      coin.value.x = coin.baseX;
      coin.value.y = coin.baseY + 28 + drift * 0.1;
      coin.value.style.fill = reactionPalette.titleFill;
      coin.value.scale.set(1 + plaqueHot * this.plaquePulse * 0.05);
    });
  }

  private redrawChrome(): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";

    const left = -32;
    const top = -22;
    const width = this.machineWidth + 64;
    const height = this.machineHeight + 48;

    this.stageAura.clear();
    this.stageAura.roundRect(-132, -184, this.machineWidth + 264, this.machineHeight + 242, 82);
    this.stageAura.fill({ color: isDonorlocal ? 0x3b070b : 0x4a0408, alpha: isDonorlocal ? 0.28 : 0.36 });
    this.stageAura.stroke({ color: isDonorlocal ? 0xf0c974 : 0xc7141a, width: isDonorlocal ? 3 : 4, alpha: isDonorlocal ? 0.16 : 0.24 });
    this.stageAura.roundRect(-82, -146, this.machineWidth + 164, this.machineHeight + 182, 62);
    this.stageAura.fill({ color: isDonorlocal ? 0x160305 : 0x200306, alpha: isDonorlocal ? 0.2 : 0.22 });

    this.cabinetShadow.clear();
    this.cabinetShadow.roundRect(left + 16, top + 28, width + 10, height + 8, 46);
    this.cabinetShadow.fill({ color: 0x060202, alpha: 0.42 });

    this.cabinetBackplate.clear();
    this.cabinetBackplate.roundRect(left, top, width, height, 42);
    this.cabinetBackplate.fill({ color: isDonorlocal ? 0x120204 : 0x180406, alpha: 0.95 });
    this.cabinetBackplate.stroke({ color: 0xe1b465, width: isDonorlocal ? 4 : 5, alpha: isDonorlocal ? 0.78 : 0.9 });
    this.cabinetBackplate.roundRect(left + 16, top + 18, width - 32, height - 44, 34);
    this.cabinetBackplate.fill({ color: isDonorlocal ? 0x1d0407 : 0x260609, alpha: isDonorlocal ? 0.72 : 0.78 });
    this.cabinetBackplate.stroke({ color: 0x5c0c11, width: 3, alpha: isDonorlocal ? 0.66 : 0.82 });
    this.cabinetBackplate.roundRect(left + 28, top + this.machineHeight + 8, width - 56, 40, 20);
    this.cabinetBackplate.fill({ color: 0x100204, alpha: isDonorlocal ? 0.8 : 0.88 });
    this.cabinetBackplate.stroke({ color: 0xffd88a, width: 2, alpha: isDonorlocal ? 0.28 : 0.42 });

    this.cabinetGlow.clear();
    this.cabinetGlow.roundRect(left - 10, top - 8, width + 20, height + 16, 48);
    this.cabinetGlow.stroke({ color: isDonorlocal ? 0xe4a545 : 0xc7141a, width: isDonorlocal ? 8 : 10, alpha: isDonorlocal ? 0.28 : 0.42 });
    this.cabinetGlow.roundRect(left + 14, top + 14, width - 28, height - 32, 36);
    this.cabinetGlow.stroke({ color: 0xffd48a, width: 3, alpha: isDonorlocal ? 0.12 : 0.16 });

    this.redrawTopBar();

    this.topperBackdrop.visible = isDonorlocal && this.topperBackdrop.texture !== Texture.EMPTY;
    this.topperBackdrop.x = this.machineWidth * 0.5;
    this.topperBackdrop.y = -92;
    this.topperBackdrop.width = 566;
    this.topperBackdrop.height = 274;
    this.mascotSprite.x = this.machineWidth * 0.5;
    this.mascotSprite.y = -72;
    this.mascotSprite.width = 128;
    this.mascotSprite.height = 128;
    this.heroGlowSprite.x = this.machineWidth * 0.5;
    this.heroGlowSprite.width = isDonorlocal ? 300 : 220;
    this.heroGlowSprite.height = isDonorlocal ? 300 : 220;
    this.heroPulseSprite.x = this.machineWidth * 0.5;
    this.heroPulseSprite.width = isDonorlocal ? 244 : 168;
    this.heroPulseSprite.height = isDonorlocal ? 244 : 168;
    this.mascotCaption.x = this.machineWidth * 0.5;
    this.mascotCaption.y = isDonorlocal ? 114 : 106;
    this.jackpotTitle.x = this.machineWidth * 0.5;
    this.jackpotTitle.y = isDonorlocal ? -196 : -186;

    const coinStartX = isDonorlocal ? this.machineWidth * 0.5 - 252 : this.machineWidth * 0.5 - 144;
    this.jackpotCoins.forEach((coin, index) => {
      if (isDonorlocal) {
        coin.baseX = coinStartX + (index % 2) * 504;
        coin.baseY = index < 2 ? -134 : -26;
      } else {
        coin.baseX = coinStartX + index * 96;
        coin.baseY = -112 + (index % 2 === 0 ? -6 : 4);
      }
      coin.sprite.width = coin.heroTextureActive ? (isDonorlocal ? 126 : 100) : isDonorlocal ? 92 : 64;
      coin.sprite.height = coin.heroTextureActive ? (isDonorlocal ? 126 : 100) : isDonorlocal ? 92 : 64;
    });

    if (isDonorlocal) {
      this.buyPanel.position.set(-158, this.machineHeight * 0.665);
      this.buyPanelSkin.position.set(0, 0);
      this.buyPanelSkin.width = 356;
      this.buyPanelSkin.height = 82;
      this.buyIcon.visible = false;
      this.buyTitle.visible = false;
      this.buyValue.visible = true;
      this.buyValue.x = 104;
      this.buyValue.y = 1;
      this.buyValue.style.fontSize = 28;
      this.buyCaption.x = 108;
      this.buyCaption.y = 22;
      this.buyCaption.style.wordWrapWidth = 132;
    } else {
      this.buyPanel.position.set(-184, this.machineHeight * 0.435 - 82);
      this.buyPanelSkin.width = 0;
      this.buyPanelSkin.height = 0;
      this.buyIcon.visible = true;
      this.buyTitle.visible = true;
      this.buyValue.visible = true;
      this.buyIcon.x = 86;
      this.buyIcon.y = 82;
      this.buyIcon.width = 88;
      this.buyIcon.height = 88;
      this.buyTitle.x = 86;
      this.buyTitle.y = 28;
      this.buyValue.x = 86;
      this.buyValue.y = 148;
      this.buyCaption.x = 86;
      this.buyCaption.y = 188;
      this.buyCaption.style.wordWrapWidth = 126;
    }

    this.actionPanel.position.set(this.machineWidth + 32, this.machineHeight * 0.392 - 80);
    this.actionTitle.x = 93;
    this.actionTitle.y = 26;
    this.autoState.x = 22;
    this.autoState.y = 72;
    this.turboState.x = 22;
    this.turboState.y = 106;
    this.soundState.x = 22;
    this.soundState.y = 140;
    this.statusChip.x = 93;
    this.statusChip.y = 172;

    if (this.showProviderDebug) {
      this.providerBadge.x = this.machineWidth + 26;
      this.providerBadge.y = -116;
      this.updateProviderDebug();
    }

    this.redrawPanels();
  }

  private redrawPanels(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const buyAccent = this.buyPanelHover > 0 ? 0xffe39f : 0xe5b35e;
    this.buyPanelGlow.clear();
    if (isDonorlocal) {
      this.buyPanelGlow.roundRect(-182, -48, 364, 96, 30);
      this.buyPanelGlow.fill({ color: 0x4f1204, alpha: 0.1 });
      this.buyPanelGlow.stroke({ color: 0xffd768, width: 6, alpha: 0.22 });
    } else {
      this.buyPanelGlow.roundRect(0, 0, 172, 218, 32);
      this.buyPanelGlow.fill({ color: 0x49070b, alpha: 0.14 });
      this.buyPanelGlow.stroke({ color: 0xc7141a, width: 9, alpha: 0.3 });
    }

    this.buyPanelBackground.clear();
    if (isDonorlocal) {
      this.buyPanelBackground.roundRect(48, -22, 126, 44, 18);
      this.buyPanelBackground.fill({ color: 0x4d1203, alpha: 0.84 });
      this.buyPanelBackground.stroke({ color: buyAccent, width: 2.5, alpha: 0.86 });
      this.buyPanelBackground.roundRect(-172, -40, 344, 18, 9);
      this.buyPanelBackground.fill({ color: 0xffffff, alpha: 0.08 });
    } else {
      this.buyPanelBackground.roundRect(0, 0, 172, 218, 32);
      this.buyPanelBackground.fill({ color: 0x160406, alpha: 0.95 });
      this.buyPanelBackground.stroke({ color: buyAccent, width: 3, alpha: 0.92 });
      this.buyPanelBackground.roundRect(12, 12, 148, 68, 22);
      this.buyPanelBackground.fill({ color: 0x2a070a, alpha: 0.7 });
      this.buyPanelBackground.roundRect(18, 144, 136, 34, 16);
      this.buyPanelBackground.fill({ color: 0x3a090d, alpha: 0.84 });
    }

    this.actionPanelGlow.clear();
    this.actionPanelGlow.roundRect(0, 0, 186, 216, 30);
    this.actionPanelGlow.fill({ color: 0x46070b, alpha: 0.12 });
    this.actionPanelGlow.stroke({ color: 0xc7141a, width: 8, alpha: 0.28 });

    this.actionPanelBackground.clear();
    this.actionPanelBackground.roundRect(0, 0, 186, 216, 30);
    this.actionPanelBackground.fill({ color: 0x130406, alpha: 0.93 });
    this.actionPanelBackground.stroke({ color: 0xffd88a, width: 3, alpha: 0.85 });
    this.actionPanelBackground.roundRect(14, 14, 158, 54, 20);
    this.actionPanelBackground.fill({ color: 0x29070a, alpha: 0.68 });
    this.actionPanelBackground.roundRect(18, 166, 150, 30, 14);
    this.actionPanelBackground.fill({ color: 0x39090d, alpha: 0.82 });
  }

  private redrawTopBar(): void {
    if (this.machineWidth <= 0) {
      return;
    }

    const reactionPalette = this.resolveReactionPalette();
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    this.topBar.clear();
    this.topBar.roundRect(
      this.machineWidth * 0.5 - (isDonorlocal ? 154 : 196),
      isDonorlocal ? -214 : -186,
      isDonorlocal ? 308 : 392,
      isDonorlocal ? 42 : 54,
      isDonorlocal ? 20 : 26,
    );
    this.topBar.fill({
      color: reactionPalette.shadow,
      alpha: isDonorlocal ? 0.72 + this.reactionPulse * 0.12 : 0.88 + this.reactionPulse * 0.12,
    });
    this.topBar.stroke({
      color: reactionPalette.accent,
      width: (isDonorlocal ? 1.5 : 2) + this.reactionPulse * 2,
      alpha: isDonorlocal ? 0.72 + this.reactionPulse * 0.12 : 0.86 + this.reactionPulse * 0.12,
    });
    this.topBar.roundRect(
      this.machineWidth * 0.5 - (isDonorlocal ? 132 : 174),
      isDonorlocal ? -204 : -174,
      isDonorlocal ? 264 : 348,
      isDonorlocal ? 20 : 30,
      16,
    );
    this.topBar.fill({ color: 0x4a0b0f, alpha: isDonorlocal ? 0.42 + this.reactionPulse * 0.08 : 0.54 + this.reactionPulse * 0.08 });
    this.topBar.stroke({ color: 0xfff0c4, width: 1.5, alpha: isDonorlocal ? 0.26 + this.reactionPulse * 0.06 : 0.4 + this.reactionPulse * 0.06 });
  }

  private resolveReactionEmphasis(tone: ChromeReactionTone): number {
    switch (tone) {
      case "jackpot":
        return 1;
      case "boost":
        return 0.86;
      case "collect":
        return 0.72;
      case "bonus":
        return 0.78;
      case "standard":
      default:
        return 0.56;
    }
  }

  private resolvePlaqueIndexes(
    tone: ChromeReactionTone,
    jackpotTier: string | null | undefined,
  ): number[] {
    if (tone === "jackpot") {
      switch ((jackpotTier ?? "").toLowerCase()) {
        case "mini":
          return [0];
        case "minor":
          return [1];
        case "major":
          return [2];
        case "grand":
          return [3];
        default:
          return [0, 1, 2, 3];
      }
    }

    if (tone === "boost") {
      return [2, 3];
    }

    if (tone === "collect") {
      return [0, 1];
    }

    if (tone === "bonus") {
      return [1, 2];
    }

    return [1];
  }

  private updateReactionText(): void {
    this.jackpotTitle.text = this.titleOverride ?? DEFAULT_TOPPER_TITLE;
    this.mascotCaption.text = this.captionOverride ?? DEFAULT_TOPPER_CAPTION;
  }

  private resolveReactionPalette(): {
    glow: number;
    accent: number;
    titleFill: number;
    captionFill: number;
    coinTint: number;
    shadow: number;
  } {
    switch (this.reactionTone) {
      case "collect":
        return {
          glow: 0xe39524,
          accent: 0xffe7a1,
          titleFill: 0xfff0c5,
          captionFill: 0xffe4b5,
          coinTint: 0xf0b74e,
          shadow: 0x2d1204,
        };
      case "boost":
        return {
          glow: 0xc7141a,
          accent: 0xffd49f,
          titleFill: 0xfff3dc,
          captionFill: 0xffdcc8,
          coinTint: 0xff8d47,
          shadow: 0x330608,
        };
      case "bonus":
        return {
          glow: 0xb66a15,
          accent: 0xffd68a,
          titleFill: 0xfff0d4,
          captionFill: 0xffe1bf,
          coinTint: 0xf0b15e,
          shadow: 0x341206,
        };
      case "jackpot":
        return {
          glow: 0xe0ad2f,
          accent: 0xfff0c4,
          titleFill: 0xfff5dc,
          captionFill: 0xffecc0,
          coinTint: 0xf6ce63,
          shadow: 0x3d1605,
        };
      case "standard":
      default:
        return {
          glow: 0xc7141a,
          accent: 0xffd782,
          titleFill: 0xffefb0,
          captionFill: 0xfff4cf,
          coinTint: 0xe5b357,
          shadow: 0x100204,
        };
    }
  }

  private updateProviderDebug(): void {
    if (!this.showProviderDebug) {
      return;
    }

    const status = getProviderPackStatus();
    const lines = [
      `req: ${status.requestedProvider}`,
      `eff: ${status.effectiveProvider}`,
      `safe: ${status.safePlaceholder ? "yes" : "no"}`,
      `missing: ${status.missingKeys.length}`,
    ];
    if (status.fallbackReason) {
      lines.push(status.fallbackReason);
    }

    this.providerBadgeText.text = lines.join("\n");
    this.providerBadgeText.x = 10;
    this.providerBadgeText.y = 8;

    this.providerBadgeBackground.clear();
    this.providerBadgeBackground.roundRect(
      0,
      0,
      this.providerBadgeText.width + 20,
      this.providerBadgeText.height + 16,
      14,
    );
    this.providerBadgeBackground.fill({ color: 0x090204, alpha: 0.82 });
    this.providerBadgeBackground.stroke({ color: 0xffd88a, width: 2, alpha: 0.92 });
  }

  private async refreshTextures(): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const donorTextures = isDonorlocal
      ? await Beta3VisualChrome.resolveDonorChromeTextures()
      : null;
    const buy = isDonorlocal
      ? { texture: donorTextures?.buyButton ?? null }
      : await resolveProviderFrameTexture("heroUiAtlas", "button-buybonus");
    const buyFallback = buy.texture
      ? buy
      : await resolveProviderFrameTexture("symbolAtlas", "collector-symbol");
    const heroGlow = isDonorlocal
      ? await resolveProviderFrameTexture("vfxAtlas", "collector-ring")
      : await resolveProviderFrameTexture("heroVfxAtlas", "vfx-hero-glow");
    const heroPulse = isDonorlocal
      ? await resolveProviderFrameTexture("vfxAtlas", "spark-burst-01")
      : await resolveProviderFrameTexture("heroVfxAtlas", "vfx-hero-pulse");
    const jackpotFrames = isDonorlocal
      ? donorTextures?.jackpotCoins.map((texture) => ({ texture })) ?? []
      : await Promise.all([
          resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-mini"),
          resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-minor"),
          resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-major"),
          resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-grand"),
        ]);

    if (requestToken !== this.textureRequestToken) {
      return;
    }

    this.mascotSprite.visible = false;
    this.topperBackdrop.texture = donorTextures?.topperBackdrop ?? Texture.EMPTY;
    this.topperBackdrop.visible = Boolean(donorTextures?.topperBackdrop);
    this.buyPanelSkin.texture = buy.texture ?? Texture.EMPTY;
    this.buyPanelSkin.visible = Boolean(buy.texture);
    this.applyTexture(this.buyIcon, isDonorlocal ? null : buyFallback.texture, 0xf1b458);
    this.buyIcon.visible = !isDonorlocal;
    this.applyTexture(this.heroGlowSprite, heroGlow.texture, 0xc7141a);
    this.applyTexture(this.heroPulseSprite, heroPulse.texture, 0xf4c869);
    this.heroGlowSprite.visible = Boolean(heroGlow.texture);
    this.heroPulseSprite.visible = Boolean(heroPulse.texture);
    this.jackpotCoins.forEach((coin, index) => {
      const resolved = jackpotFrames[index];
      coin.heroTextureActive = Boolean(resolved?.texture);
      this.applyTexture(coin.sprite, resolved?.texture ?? null, 0xf2b545);
      coin.label.visible = !coin.heroTextureActive;
      coin.value.visible = !coin.heroTextureActive;
    });
    this.mascotCaption.visible = true;
    this.redrawChrome();
  }

  private static async resolveDonorChromeTextures(): Promise<DonorChromeTextures> {
    if (!Beta3VisualChrome.donorChromeTexturesPromise) {
      Beta3VisualChrome.donorChromeTexturesPromise = (async () => {
        const manifestUrl = new URL(getDonorLocalManifestUrl(), window.location.origin).toString();
        const urls = {
          topperBackdrop: new URL("../image/startScreen2.d0b00680.png", manifestUrl).toString(),
          buyButton: new URL("../image/btn_buy_bonus.1d85b9e0.png", manifestUrl).toString(),
          jackpotCoins: [
            new URL("../image/img_mini_coin.e12ee50e.png", manifestUrl).toString(),
            new URL("../image/img_minor_coin.fea1c83b.png", manifestUrl).toString(),
            new URL("../image/img_major_coin.b2d60eb9.png", manifestUrl).toString(),
            new URL("../image/img_grand_coin.68e52468.png", manifestUrl).toString(),
          ],
        };

        const loadTexture = async (url: string): Promise<Texture | null> => {
          try {
            await Assets.load(url);
            return Texture.from(url);
          } catch {
            return null;
          }
        };

        return {
          topperBackdrop: await loadTexture(urls.topperBackdrop),
          buyButton: await loadTexture(urls.buyButton),
          jackpotCoins: await Promise.all(urls.jackpotCoins.map((url) => loadTexture(url))),
        };
      })();
    }

    return Beta3VisualChrome.donorChromeTexturesPromise;
  }

  private applyTexture(sprite: Sprite, texture: Texture | null, fallbackTint: number): void {
    sprite.texture = texture ?? Texture.WHITE;
    sprite.tint = texture ? 0xffffff : fallbackTint;
  }
}
