import { Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import {
  CRAZY_ROOSTER_JACKPOTS,
} from "../../../game/config/CrazyRoosterGameConfig.ts";
import {
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

const readDebugProviderFlag = (): boolean =>
  new URLSearchParams(window.location.search).get("providerDebug") === "1";

export class Beta3VisualChrome extends Container {
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
    text: "Tap to launch Hold&Win",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 14,
      fontWeight: "700",
      fill: 0xffd8d0,
      align: "center",
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
      wordWrapWidth: 180,
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

  public onBuyFeatureRequest: (() => void) | null = null;

  constructor() {
    super();

    this.mascotSprite.anchor.set(0.5, 1);
    this.mascotSprite.alpha = 0.96;
    this.heroGlowSprite.anchor.set(0.5);
    this.heroGlowSprite.alpha = 0;
    this.heroPulseSprite.anchor.set(0.5);
    this.heroPulseSprite.alpha = 0;
    this.mascotCaption.anchor.set(0.5, 0.5);
    this.jackpotTitle.anchor.set(0.5);
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
    this.buyValue.text = this.modes.buyLabel;
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
    this.boostFlash = Math.max(this.boostFlash, 0.35);
  }

  public triggerPresentationCue(
    cue:
      | string
      | {
          tone?: string;
          title?: string;
          caption?: string;
          holdMs?: number;
          jackpotTier?: string | null;
          plaqueIndexes?: number[];
        },
  ): void {
    if (!cue) {
      return;
    }
    const cueText = typeof cue === "string"
      ? cue
      : [cue.tone, cue.title, cue.caption, cue.jackpotTier].filter(Boolean).join(" ").toLowerCase();
    if (cueText.includes("boost") || cueText.includes("jackpot") || cueText.includes("collect")) {
      this.boostFlash = 1;
    }
    if (cueText.includes("line")) {
      this.boostFlash = Math.max(this.boostFlash, 0.55);
    }
  }

  public clearPresentationCue(_cue?: string): void {
    this.boostFlash = Math.max(0, this.boostFlash * 0.45);
  }

  private tick(deltaMs: number): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }

    this.ambientTime += deltaMs / 1000;
    this.boostFlash = Math.max(0, this.boostFlash - deltaMs / 520);

    const heroFloat = Math.sin(this.ambientTime * 1.35) * 7;
    const heroPulse = 1 + Math.sin(this.ambientTime * 1.7) * 0.035 + this.boostFlash * 0.06;
    this.mascotSprite.y = -72 + heroFloat;
    this.mascotSprite.scale.set(heroPulse);
    this.heroGlowSprite.y = -70 + heroFloat * 0.18;
    this.heroGlowSprite.scale.set(1.02 + Math.sin(this.ambientTime * 1.9) * 0.04 + this.boostFlash * 0.08);
    this.heroGlowSprite.alpha = 0.22 + this.boostFlash * 0.22;
    this.heroPulseSprite.y = -80 + heroFloat * 0.12;
    this.heroPulseSprite.scale.set(0.98 + Math.sin(this.ambientTime * 2.2 + 0.4) * 0.05 + this.boostFlash * 0.1);
    this.heroPulseSprite.alpha = 0.14 + this.boostFlash * 0.18;

    this.mascotHalo.clear();
    this.mascotHalo.ellipse(
      this.machineWidth * 0.5,
      -56 + heroFloat * 0.2,
      98 + this.boostFlash * 18,
      28 + this.boostFlash * 8,
    );
    this.mascotHalo.fill({ color: 0xc7141a, alpha: 0.22 + this.boostFlash * 0.15 });

    this.cabinetGlow.alpha = 0.52 + Math.sin(this.ambientTime * 2.1) * 0.14 + this.boostFlash * 0.18;
    this.buyPanel.scale.set(1 + this.buyPanelHover * 0.03 + Math.sin(this.ambientTime * 2.4) * 0.008);
    this.actionPanel.scale.set(1 + Math.sin(this.ambientTime * 1.8 + 0.5) * 0.01);

    this.jackpotCoins.forEach((coin, index) => {
      const drift = Math.sin(this.ambientTime * 1.8 + coin.phase) * 5;
      const pulse = 1 + Math.sin(this.ambientTime * 2.3 + coin.phase) * 0.05 + this.boostFlash * 0.03;
      coin.sprite.x = coin.baseX;
      coin.sprite.y = coin.baseY + drift;
      coin.sprite.scale.set(pulse);
      coin.halo.clear();
      if (coin.heroTextureActive) {
        coin.halo.circle(coin.baseX, coin.baseY + drift, 46 + index * 2 + this.boostFlash * 6);
        coin.halo.fill({ color: 0xffcf73, alpha: 0.18 + this.boostFlash * 0.08 });
      } else {
        coin.halo.circle(coin.baseX, coin.baseY + drift, 33 + index * 2 + this.boostFlash * 4);
        coin.halo.fill({ color: 0xe5b357, alpha: 0.98 });
        coin.halo.stroke({ color: 0xfff0c1, width: 4, alpha: 0.95 });
        coin.halo.circle(coin.baseX, coin.baseY + drift, 24 + index * 2 + this.boostFlash * 2);
        coin.halo.fill({ color: 0x9f5519, alpha: 0.28 });
      }
      coin.label.x = coin.baseX;
      coin.label.y = coin.baseY + 10 + drift * 0.1;
      coin.value.x = coin.baseX;
      coin.value.y = coin.baseY + 28 + drift * 0.1;
    });
  }

  private redrawChrome(): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }

    const left = -32;
    const top = -22;
    const width = this.machineWidth + 64;
    const height = this.machineHeight + 48;

    this.stageAura.clear();
    this.stageAura.roundRect(-118, -166, this.machineWidth + 236, this.machineHeight + 210, 76);
    this.stageAura.fill({ color: 0x5e0408, alpha: 0.34 });
    this.stageAura.stroke({ color: 0xc7141a, width: 4, alpha: 0.22 });

    this.cabinetShadow.clear();
    this.cabinetShadow.roundRect(left + 12, top + 24, width, height, 42);
    this.cabinetShadow.fill({ color: 0x060202, alpha: 0.42 });

    this.cabinetBackplate.clear();
    this.cabinetBackplate.roundRect(left, top, width, height, 42);
    this.cabinetBackplate.fill({ color: 0x180406, alpha: 0.95 });
    this.cabinetBackplate.stroke({ color: 0xe1b465, width: 5, alpha: 0.9 });

    this.cabinetGlow.clear();
    this.cabinetGlow.roundRect(left - 10, top - 8, width + 20, height + 16, 48);
    this.cabinetGlow.stroke({ color: 0xc7141a, width: 10, alpha: 0.42 });

    this.topBar.clear();
    this.topBar.roundRect(this.machineWidth * 0.5 - 176, -178, 352, 40, 20);
    this.topBar.fill({ color: 0x100204, alpha: 0.9 });
    this.topBar.stroke({ color: 0xffd782, width: 2, alpha: 0.9 });

    this.mascotSprite.x = this.machineWidth * 0.5;
    this.mascotSprite.y = -72;
    this.mascotSprite.width = 128;
    this.mascotSprite.height = 128;
    this.heroGlowSprite.x = this.machineWidth * 0.5;
    this.heroGlowSprite.width = 220;
    this.heroGlowSprite.height = 220;
    this.heroPulseSprite.x = this.machineWidth * 0.5;
    this.heroPulseSprite.width = 168;
    this.heroPulseSprite.height = 168;
    this.mascotCaption.x = this.machineWidth * 0.5;
    this.mascotCaption.y = 108;
    this.jackpotTitle.x = this.machineWidth * 0.5;
    this.jackpotTitle.y = -158;

    const coinStartX = this.machineWidth * 0.5 - 132;
    this.jackpotCoins.forEach((coin, index) => {
      coin.baseX = coinStartX + index * 88;
      coin.baseY = -116 + (index % 2 === 0 ? -5 : 5);
      coin.sprite.width = coin.heroTextureActive ? 92 : 58;
      coin.sprite.height = coin.heroTextureActive ? 92 : 58;
    });

    this.buyPanel.position.set(-162, this.machineHeight * 0.44 - 84);
    this.buyIcon.x = 76;
    this.buyIcon.y = 76;
    this.buyIcon.width = 84;
    this.buyIcon.height = 84;
    this.buyTitle.x = 76;
    this.buyTitle.y = 24;
    this.buyValue.x = 76;
    this.buyValue.y = 142;
    this.buyCaption.x = 76;
    this.buyCaption.y = 174;

    this.actionPanel.position.set(this.machineWidth + 22, this.machineHeight * 0.4 - 80);
    this.actionTitle.x = 86;
    this.actionTitle.y = 24;
    this.autoState.x = 18;
    this.autoState.y = 66;
    this.turboState.x = 18;
    this.turboState.y = 98;
    this.soundState.x = 18;
    this.soundState.y = 130;
    this.statusChip.x = 86;
    this.statusChip.y = 164;

    if (this.showProviderDebug) {
      this.providerBadge.x = this.machineWidth + 26;
      this.providerBadge.y = -116;
      this.updateProviderDebug();
    }

    this.redrawPanels();
  }

  private redrawPanels(): void {
    const buyAccent = this.buyPanelHover > 0 ? 0xffe39f : 0xe5b35e;
    this.buyPanelGlow.clear();
    this.buyPanelGlow.roundRect(0, 0, 152, 196, 30);
    this.buyPanelGlow.stroke({ color: 0xc7141a, width: 8, alpha: 0.34 });

    this.buyPanelBackground.clear();
    this.buyPanelBackground.roundRect(0, 0, 152, 196, 30);
    this.buyPanelBackground.fill({ color: 0x170406, alpha: 0.94 });
    this.buyPanelBackground.stroke({ color: buyAccent, width: 3, alpha: 0.92 });

    this.actionPanelGlow.clear();
    this.actionPanelGlow.roundRect(0, 0, 172, 206, 28);
    this.actionPanelGlow.stroke({ color: 0xc7141a, width: 8, alpha: 0.28 });

    this.actionPanelBackground.clear();
    this.actionPanelBackground.roundRect(0, 0, 172, 206, 28);
    this.actionPanelBackground.fill({ color: 0x130406, alpha: 0.92 });
    this.actionPanelBackground.stroke({ color: 0xffd88a, width: 3, alpha: 0.85 });
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
    const buy = await resolveProviderFrameTexture("heroUiAtlas", "button-buybonus");
    const buyFallback = buy.texture
      ? buy
      : await resolveProviderFrameTexture("symbolAtlas", "collector-symbol");
    const heroGlow = await resolveProviderFrameTexture("heroVfxAtlas", "vfx-hero-glow");
    const heroPulse = await resolveProviderFrameTexture("heroVfxAtlas", "vfx-hero-pulse");
    const jackpotFrames = await Promise.all([
      resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-mini"),
      resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-minor"),
      resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-major"),
      resolveProviderFrameTexture("heroUiAtlas", "jackpot-plaque-grand"),
    ]);

    if (requestToken !== this.textureRequestToken) {
      return;
    }

    this.mascotSprite.visible = false;
    this.applyTexture(this.buyIcon, buyFallback.texture, 0xf1b458);
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
    this.mascotCaption.visible = false;
  }

  private applyTexture(sprite: Sprite, texture: Texture | null, fallbackTint: number): void {
    sprite.texture = texture ?? Texture.WHITE;
    sprite.tint = texture ? 0xffffff : fallbackTint;
  }
}
