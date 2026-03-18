import { Assets, Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import type { PremiumHudControlId } from "@gamesv1/ui-kit";

import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../assets/providerPackRegistry.ts";

type HudButtonLike = {
  x: number;
  y: number;
  width: number;
  height: number;
  alpha: number;
  visible: boolean;
};

type HudButtons = Record<PremiumHudControlId, HudButtonLike>;

type HudChromeState = {
  autoplayActive: boolean;
  turboSelected: boolean;
  soundEnabled: boolean;
  buyLabel: string;
  betLabel: string;
  balanceLabel: string;
  winLabel: string;
  holdTurboRequested: boolean;
  orientation: "portrait" | "landscape";
};

type SecondaryControlVisual = {
  controlId: Exclude<PremiumHudControlId, "spin">;
  frameKey: string;
  container: Container;
  glow: Graphics;
  fallbackPlate: Graphics;
  art: Sprite;
  badge: Text;
};

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

const secondarySpecs: Array<{
  controlId: Exclude<PremiumHudControlId, "spin">;
  frameKey: string;
  fallbackTint: number;
}> = [
  { controlId: "turbo", frameKey: "button-turbo", fallbackTint: 0x37a860 },
  { controlId: "autoplay", frameKey: "button-autoplay", fallbackTint: 0x2f2f2f },
  { controlId: "buyFeature", frameKey: "button-buybonus", fallbackTint: 0xffbc52 },
  { controlId: "sound", frameKey: "button-sound", fallbackTint: 0x2f2f2f },
  { controlId: "settings", frameKey: "button-settings", fallbackTint: 0x2f2f2f },
  { controlId: "history", frameKey: "button-history", fallbackTint: 0x2f2f2f },
];

export class HeroHudChrome extends Container {
  private readonly leftPanel = new Graphics();
  private readonly centerPanel = new Graphics();
  private readonly rightPanel = new Graphics();
  private readonly balancePanel = new Graphics();
  private readonly betPanel = new Graphics();
  private readonly balancePlateSprite = new Sprite(Texture.WHITE);
  private readonly betPlateSprite = new Sprite(Texture.WHITE);
  private readonly winPlateSprite = new Sprite(Texture.WHITE);
  private readonly centerText = new Text({
    text: "PLACE YOUR BET",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 20,
      fontWeight: "900",
      fill: 0xfff2d5,
      stroke: { color: 0x240507, width: 5 },
      letterSpacing: 1,
    },
  });
  private readonly winText = new Text({
    text: "WIN: 0",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 18,
      fontWeight: "900",
      fill: 0xfff4db,
      stroke: { color: 0x210508, width: 4 },
      letterSpacing: 0.8,
    },
  });
  private readonly spinGlow = new Graphics();
  private readonly spinPlate = new Graphics();
  private readonly spinFace = new Graphics();
  private readonly spinArt = new Sprite(Texture.WHITE);
  private readonly spinText = new Text({
    text: "GO",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 25,
      fontWeight: "900",
      fill: 0xfff5e6,
      stroke: { color: 0x200407, width: 5 },
      letterSpacing: 1,
    },
  });
  private readonly spinSubtext = new Text({
    text: "SPIN",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 11,
      fontWeight: "900",
      fill: 0xffd9a0,
      stroke: { color: 0x170306, width: 3 },
      letterSpacing: 1,
    },
  });
  private readonly betText = new Text({
    text: "Bet: 1",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 20,
      fontWeight: "900",
      fill: 0xf5d589,
      stroke: { color: 0x1f0508, width: 4 },
      letterSpacing: 1,
    },
  });
  private readonly balanceText = new Text({
    text: "Balance: 0",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 18,
      fontWeight: "800",
      fill: 0xfff4d4,
      stroke: { color: 0x210509, width: 4 },
      letterSpacing: 0.8,
    },
  });

  private readonly secondaryVisuals: SecondaryControlVisual[];
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);
  private readonly donorTurboArt = new Sprite(Texture.WHITE);
  private donorTurboTexture: Texture | null = null;
  private spinTexture: Texture | null = null;
  private balancePlateTexture: Texture | null = null;
  private betPlateTexture: Texture | null = null;
  private winPlateTexture: Texture | null = null;

  private buttons: HudButtons | null = null;
  private ambientTime = 0;
  private state: HudChromeState = {
    autoplayActive: false,
    turboSelected: false,
    soundEnabled: true,
    buyLabel: "BONUS 75",
    betLabel: "1",
    balanceLabel: "0",
    winLabel: "0",
    holdTurboRequested: false,
    orientation: "landscape",
  };

  constructor() {
    super();

    this.visible = false;
    this.centerText.anchor.set(0.5);
    this.betText.anchor.set(0.5);
    this.balanceText.anchor.set(0.5);
    this.winText.anchor.set(0.5);
    this.balancePlateSprite.anchor.set(0.5);
    this.betPlateSprite.anchor.set(0.5);
    this.winPlateSprite.anchor.set(0.5);
    this.balancePlateSprite.visible = false;
    this.betPlateSprite.visible = false;
    this.winPlateSprite.visible = false;
    this.spinArt.anchor.set(0.5);
    this.spinArt.visible = false;
    this.spinText.anchor.set(0.5);
    this.spinSubtext.anchor.set(0.5);
    this.addChild(
      this.leftPanel,
      this.centerPanel,
      this.rightPanel,
      this.balancePanel,
      this.balancePlateSprite,
      this.betPlateSprite,
      this.winPlateSprite,
      this.centerText,
      this.betPanel,
      this.betText,
      this.balanceText,
      this.winText,
      this.spinGlow,
      this.spinPlate,
      this.spinFace,
      this.spinArt,
      this.spinText,
      this.spinSubtext,
    );

    this.secondaryVisuals = secondarySpecs.map((spec) => {
      const container = new Container();
      const glow = new Graphics();
      const fallbackPlate = new Graphics();
      const art = new Sprite(Texture.WHITE);
      const badge = new Text({
        text: "",
        style: {
          fontFamily: "Trebuchet MS, Arial, sans-serif",
          fontSize: 10,
          fontWeight: "900",
          fill: 0xfff7ec,
          stroke: { color: 0x1b0408, width: 3 },
          letterSpacing: 1,
        },
      });

      art.anchor.set(0.5);
      badge.anchor.set(0.5);
      container.addChild(glow, fallbackPlate, art, badge);
      this.addChild(container);

      return {
        controlId: spec.controlId,
        frameKey: spec.frameKey,
        container,
        glow,
        fallbackPlate,
        art,
        badge,
      };
    });

    this.donorTurboArt.anchor.set(0.5);
    this.donorTurboArt.visible = false;
    this.addChild(this.donorTurboArt);

    this.refreshBadges();
    void this.refreshTextures();
    Ticker.shared.add(this.motionTicker);
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public attachButtons(buttons: HudButtons): void {
    this.buttons = buttons;
    this.visible = true;
    this.layout();
  }

  public setState(next: Partial<HudChromeState>): void {
    this.state = { ...this.state, ...next };
    this.refreshBadges();
    this.layout();
  }

  private tick(deltaMs: number): void {
    if (!this.visible || !this.buttons) {
      return;
    }

    this.ambientTime += deltaMs / 1000;
    this.spinGlow.alpha = 0.26 + Math.sin(this.ambientTime * 2.4) * 0.08;
    this.spinFace.scale.set(1 + Math.sin(this.ambientTime * 2) * 0.012);
    this.centerPanel.alpha = 0.96 + Math.sin(this.ambientTime * 1.2) * 0.03;

    this.secondaryVisuals.forEach((visual, index) => {
      if (!visual.container.visible) {
        return;
      }
      visual.glow.alpha = 0.12 + Math.sin(this.ambientTime * 2 + index * 0.3) * 0.05;
      if (getProviderPackStatus().effectiveProvider === "donorlocal") {
        visual.glow.alpha += 0.08;
      }
    });
  }

  private refreshBadges(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const isPortrait = this.state.orientation === "portrait";
    for (const visual of this.secondaryVisuals) {
      switch (visual.controlId) {
        case "turbo":
          visual.badge.text = isDonorlocal && isPortrait ? "HOLD FOR\nTURBO" : "";
          break;
        case "autoplay":
          visual.badge.text = isDonorlocal
            ? isPortrait
              ? "A"
              : "AUTO"
            : this.state.autoplayActive
              ? "STOP"
              : "AUTO";
          break;
        case "buyFeature":
          visual.badge.text = this.state.buyLabel.replace("BONUS ", "");
          break;
        case "sound":
          visual.badge.text = isDonorlocal ? (isPortrait ? "" : "−") : "";
          break;
        case "settings":
          visual.badge.text = isDonorlocal ? (isPortrait ? "⚙" : "⚙") : "";
          break;
        case "history":
          visual.badge.text = isDonorlocal ? (isPortrait ? "" : "+") : "";
          break;
      }
    }

    this.spinSubtext.text = this.state.holdTurboRequested ? "TURBO" : "SPIN";
    if (isDonorlocal && isPortrait) {
      this.betText.text = `BET: $${this.state.betLabel}`;
      this.balanceText.text = `$${this.state.balanceLabel}`;
    } else {
      this.betText.text = `Bet: ${this.state.betLabel}`;
      this.balanceText.text = `Balance: ${this.state.balanceLabel}`;
    }
    this.centerText.text = "PLACE YOUR BET";
    this.centerText.style.fontSize = isDonorlocal && isPortrait ? 22 : 20;
    this.winText.text = `WIN: ${this.state.winLabel}`;
    this.winText.visible = isDonorlocal && isPortrait;
  }

  private layout(): void {
    if (!this.buttons) {
      return;
    }

    const spin = this.buttons.spin;
    const turbo = this.buttons.turbo;
    const autoplay = this.buttons.autoplay;
    const settings = this.buttons.settings;
    const sound = this.buttons.sound;
    const history = this.buttons.history;

    this.visible = spin.visible || turbo.visible || autoplay.visible;

    const leftX = settings.x - 44;
    const leftY = settings.y - 42;
    const leftW = history.x - settings.x + 88;
    const leftH = 84;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const isNarrow = spin.x - settings.x < 540;
    const isPortrait = this.state.orientation === "portrait";
    const portraitDonorRow = isDonorlocal && isPortrait;

    this.leftPanel.clear();
    if (!isDonorlocal) {
      this.leftPanel.roundRect(leftX, leftY, leftW, leftH, 20);
      this.leftPanel.fill({ color: 0x1a0608, alpha: 0.92 });
      this.leftPanel.stroke({ color: 0xeecb86, width: 3, alpha: 0.9 });
    }
    this.balancePanel.clear();
    this.balancePlateSprite.visible = false;
    if (isDonorlocal && isPortrait) {
      const portraitBalanceX = settings.x + 90;
      const portraitPlateY = spin.y + 3;
      this.balancePanel.roundRect(portraitBalanceX - 66, portraitPlateY - 19, 132, 38, 14);
      this.balancePanel.fill({ color: 0x240709, alpha: 0.88 });
      this.balancePanel.stroke({ color: 0xe7c57d, width: 2.5, alpha: 0.8 });
      if (this.balancePlateTexture) {
        this.balancePlateSprite.texture = this.balancePlateTexture;
        this.balancePlateSprite.width = 132;
        this.balancePlateSprite.height = 38;
        this.balancePlateSprite.position.set(portraitBalanceX, portraitPlateY);
        this.balancePlateSprite.alpha = 0.42;
        this.balancePlateSprite.visible = true;
      }
      this.balanceText.x = portraitBalanceX;
      this.balanceText.y = portraitPlateY + 1;
      this.balanceText.style.fontSize = 18;
      this.balanceText.style.align = "center";
      this.balanceText.visible = true;
    } else if (isDonorlocal) {
      const panelX = settings.x + 44;
      const panelY = settings.y - 33;
      const panelW = isNarrow ? 184 : 214;
      const panelH = 60;
      if (this.balancePlateTexture) {
        this.balancePlateSprite.texture = this.balancePlateTexture;
        this.balancePlateSprite.width = panelW;
        this.balancePlateSprite.height = panelH;
        this.balancePlateSprite.position.set(panelX + panelW * 0.5, panelY + panelH * 0.5);
        this.balancePlateSprite.alpha = 0.96;
        this.balancePlateSprite.visible = true;
      } else {
        this.balancePanel.roundRect(panelX, panelY, panelW, panelH, 14);
        this.balancePanel.fill({ color: 0x1a0608, alpha: 0.92 });
        this.balancePanel.stroke({ color: 0xeec982, width: 3, alpha: 0.9 });
      }
      this.balanceText.x = panelX + panelW * 0.5;
      this.balanceText.y = panelY + panelH * 0.5;
      this.balanceText.style.fontSize = 18;
      this.balanceText.style.align = "center";
      this.balanceText.visible = true;
    } else {
      this.balanceText.style.align = "center";
      this.balanceText.visible = false;
    }

    const centerX = (settings.x + spin.x) * 0.5;
    const centerY = spin.y - 22;
    this.centerPanel.clear();
    this.winPlateSprite.visible = false;
    if (!isDonorlocal) {
      this.centerPanel.roundRect(centerX - 142, centerY - 18, 284, 48, 16);
      this.centerPanel.fill({ color: 0x21070a, alpha: 0.92 });
      this.centerPanel.stroke({ color: 0xf0d191, width: 3, alpha: 0.9 });
    } else if (!isPortrait) {
      const stripLeft = settings.x + 74;
      const stripRight = spin.x - 132;
      const stripTop = spin.y - 30;
      const stripWidth = Math.max(0, stripRight - stripLeft);
      if (stripWidth > 0) {
        this.centerPanel.roundRect(stripLeft, stripTop + 8, stripWidth, 42, 14);
        this.centerPanel.fill({ color: 0x1b0609, alpha: 0.18 });
        this.centerPanel.stroke({ color: 0xf0d191, width: 1.5, alpha: 0.08 });
      }
    } else {
      const stripLeft = settings.x + 28;
      const stripRight = turbo.x + 22;
      const stripWidth = Math.max(0, stripRight - stripLeft);
      const stripCenterX = stripLeft + stripWidth * 0.5;
      const stripY = spin.y - 118;
      if (this.winPlateTexture) {
        this.winPlateSprite.texture = this.winPlateTexture;
        this.winPlateSprite.width = stripWidth;
        this.winPlateSprite.height = 22;
        this.winPlateSprite.position.set(stripCenterX, stripY);
        this.winPlateSprite.alpha = 0.48;
        this.winPlateSprite.visible = true;
      } else {
        this.centerPanel.roundRect(stripLeft, stripY - 11, stripWidth, 22, 10);
        this.centerPanel.fill({ color: 0x180608, alpha: 0.56 });
        this.centerPanel.stroke({ color: 0xf0d191, width: 2, alpha: 0.22 });
      }
      this.centerPanel.roundRect(stripLeft, stripY - 11, stripWidth, 22, 10);
      this.centerPanel.fill({ color: 0x160608, alpha: 0.54 });
      this.centerPanel.stroke({ color: 0xf0d191, width: 1.6, alpha: 0.24 });
      this.winText.x = stripCenterX;
      this.winText.y = stripY + 1;
      this.winText.style.fontSize = 14;
    }
    this.centerText.x = isDonorlocal && isPortrait ? turbo.x - 58 : centerX;
    this.centerText.y = isDonorlocal
      ? isPortrait
        ? spin.y - 148
        : centerY + (isNarrow ? -2 : 8)
      : centerY + 6;

    const rightX = turbo.x - 24;
    const rightY = autoplay.y - 34;
    const rightW = spin.x - turbo.x + 98;
    const rightH = spin.y - autoplay.y + 102;
    this.rightPanel.clear();
    if (!isDonorlocal) {
      this.rightPanel.roundRect(rightX, rightY, rightW, rightH, 24);
      this.rightPanel.fill({ color: 0x170507, alpha: 0.9 });
      this.rightPanel.stroke({ color: 0xf1ce8f, width: 3, alpha: 0.86 });
    }

    this.spinText.x = spin.x;
    this.spinText.y = spin.y - 2;
    this.spinSubtext.x = spin.x;
    this.spinSubtext.y = spin.y + spin.height * 0.31;

    this.betPlateSprite.visible = false;
    if (isDonorlocal && isPortrait) {
      const portraitBetX = turbo.x - 76;
      const portraitPlateY = spin.y + 3;
      this.betPanel.clear();
      this.betPanel.roundRect(portraitBetX - 66, portraitPlateY - 19, 132, 38, 14);
      this.betPanel.fill({ color: 0x240709, alpha: 0.88 });
      this.betPanel.stroke({ color: 0xe7c57d, width: 2.5, alpha: 0.8 });
      if (this.betPlateTexture) {
        this.betPlateSprite.texture = this.betPlateTexture;
        this.betPlateSprite.width = 132;
        this.betPlateSprite.height = 38;
        this.betPlateSprite.position.set(portraitBetX, portraitPlateY);
        this.betPlateSprite.alpha = 0.42;
        this.betPlateSprite.visible = true;
      }
      this.betText.x = portraitBetX;
      this.betText.y = portraitPlateY + 1;
      this.betText.style.fontSize = 18;
      this.betText.style.align = "center";
      this.betText.visible = true;
    } else if (isDonorlocal) {
      const panelWidth = isNarrow ? 134 : 148;
      const panelHeight = 58;
      const panelLeft = turbo.x - (isNarrow ? 66 : 72) - panelWidth;
      const panelTop = spin.y - 29;
      this.betPanel.clear();
      if (this.betPlateTexture) {
        this.betPlateSprite.texture = this.betPlateTexture;
        this.betPlateSprite.width = panelWidth;
        this.betPlateSprite.height = panelHeight;
        this.betPlateSprite.position.set(panelLeft + panelWidth * 0.5, panelTop + panelHeight * 0.5);
        this.betPlateSprite.alpha = 0.96;
        this.betPlateSprite.visible = true;
      } else {
        this.betPanel.roundRect(panelLeft, panelTop, panelWidth, panelHeight, 14);
        this.betPanel.fill({ color: 0x1a0608, alpha: 0.92 });
        this.betPanel.stroke({ color: 0xeec982, width: 3, alpha: 0.9 });
      }
      this.betText.x = panelLeft + panelWidth * 0.5;
      this.betText.y = panelTop + panelHeight * 0.5;
      this.betText.style.fontSize = isNarrow ? 18 : 20;
      this.betText.visible = true;
    } else if (this.buttons.sound.visible && this.buttons.history.visible) {
      const panelLeft = this.buttons.sound.x - 54;
      const panelRight = this.buttons.history.x + 54;
      const panelTop = this.buttons.sound.y - 30;
      const panelHeight = 60;
      this.betPanel.clear();
      this.betPanel.roundRect(panelLeft, panelTop, panelRight - panelLeft, panelHeight, 14);
      this.betPanel.fill({ color: 0x1a0608, alpha: 0.92 });
      this.betPanel.stroke({ color: 0xeec982, width: 3, alpha: 0.9 });
      this.betText.x = (this.buttons.sound.x + this.buttons.history.x) * 0.5;
      this.betText.y = this.buttons.sound.y - 2;
      this.betText.style.fontSize = isNarrow ? 18 : 20;
      this.betText.visible = true;
    } else {
      this.betPanel.clear();
      this.betText.x = centerX + 160;
      this.betText.y = centerY + 6;
      this.betText.visible = true;
    }

    this.spinGlow.clear();
    this.spinGlow.circle(
      spin.x,
      spin.y,
      spin.width * (isDonorlocal ? (portraitDonorRow ? 0.52 : 0.44) : 0.65),
    );
    this.spinGlow.fill({ color: isDonorlocal ? 0xffdd91 : 0xc7141a, alpha: isDonorlocal ? 0.16 : 0.24 });

    const useSpinArt = false && this.spinTexture !== null;
    this.spinArt.visible = useSpinArt;
    if (useSpinArt) {
      this.spinArt.texture = this.spinTexture!;
      const spinArtSize = Math.max(spin.width, spin.height) * 1.04;
      this.spinArt.width = spinArtSize;
      this.spinArt.height = spinArtSize;
      this.spinArt.position.set(spin.x, spin.y + 2);
      this.spinPlate.clear();
      this.spinFace.clear();
      this.spinText.visible = false;
      this.spinSubtext.visible = false;
    } else {
      this.spinPlate.clear();
      this.spinFace.clear();
      if (isDonorlocal) {
        const boxW = portraitDonorRow ? 96 : 54;
        const boxH = portraitDonorRow ? 96 : 54;
        this.spinPlate.roundRect(spin.x - boxW * 0.5, spin.y - boxH * 0.5, boxW, boxH, 14);
        this.spinPlate.fill({ color: 0x160608, alpha: 0.96 });
        this.spinPlate.stroke({ color: 0xf2ca7e, width: 3, alpha: 0.92 });
        const arrowW = portraitDonorRow ? 17 : 8;
        const arrowH = portraitDonorRow ? 23 : 11;
        this.spinFace.moveTo(spin.x - arrowW, spin.y - arrowH);
        this.spinFace.lineTo(spin.x - arrowW, spin.y + arrowH);
        this.spinFace.lineTo(spin.x + arrowH, spin.y);
        this.spinFace.closePath();
        this.spinFace.fill({ color: 0xfff2d7, alpha: 0.98 });
        this.spinText.visible = false;
        this.spinSubtext.visible = false;
      } else {
        this.spinPlate.circle(spin.x, spin.y, spin.width * 0.54);
        this.spinPlate.fill({ color: 0x1f0609, alpha: 0.96 });
        this.spinPlate.stroke({
          color: 0xf2ca7e,
          width: 3,
          alpha: 0.92,
        });

        this.spinFace.roundRect(
          spin.x - spin.width * 0.33,
          spin.y - spin.height * 0.18,
          spin.width * 0.66,
          spin.height * 0.36,
          14,
        );
        this.spinFace.fill({ color: 0xc7141a, alpha: 0.96 });
        this.spinFace.stroke({ color: 0xffefcf, width: 3, alpha: 0.95 });
        this.spinText.visible = true;
        this.spinSubtext.visible = true;
      }
    }

    for (const visual of this.secondaryVisuals) {
      const button = this.buttons[visual.controlId];
      let centerXPos = button.x;
      let centerYPos = button.y;
      const artSize =
        visual.controlId === "turbo"
          ? portraitDonorRow
            ? 152
            : 124
          : visual.controlId === "autoplay"
            ? portraitDonorRow
              ? 120
              : 62
            : visual.controlId === "buyFeature"
              ? 0
              : portraitDonorRow
                ? 104
                : 52;

      const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
      if (isDonorlocal) {
        if (visual.controlId === "turbo") {
          centerXPos = portraitDonorRow ? turbo.x : spin.x - 90;
          centerYPos = portraitDonorRow ? turbo.y : spin.y - 6;
        } else if (visual.controlId === "autoplay") {
          centerXPos = portraitDonorRow ? button.x : spin.x + 2;
          centerYPos = portraitDonorRow ? button.y : spin.y - 70;
        } else if (visual.controlId === "sound") {
          centerXPos = portraitDonorRow ? sound.x : spin.x + 88;
          centerYPos = portraitDonorRow ? sound.y : spin.y - 76;
        } else if (portraitDonorRow && visual.controlId === "history") {
          centerXPos = history.x;
          centerYPos = history.y;
        } else if (portraitDonorRow && visual.controlId === "settings") {
          centerXPos = settings.x;
          centerYPos = settings.y;
        }
      }
      visual.container.visible = button.visible && artSize > 0;
      if (!visual.container.visible) {
        continue;
      }
      visual.container.position.set(centerXPos, centerYPos);
      visual.glow.clear();
      visual.glow.circle(0, 0, artSize * 0.5);
      visual.glow.fill({ color: 0xc7141a, alpha: 0.16 });

      const spec = secondarySpecs.find((entry) => entry.controlId === visual.controlId);
      visual.fallbackPlate.clear();
      visual.fallbackPlate.roundRect(-artSize * 0.45, -artSize * 0.45, artSize * 0.9, artSize * 0.9, 14);
      visual.fallbackPlate.fill({
        color: visual.controlId === "turbo" && isDonorlocal
          ? 0x31a95b
          : visual.controlId === "autoplay"
            ? 0x111217
            : spec?.fallbackTint ?? 0x1b0508,
        alpha: visual.controlId === "autoplay" ? 0.96 : 0.95,
      });
      visual.fallbackPlate.stroke({
        color: visual.controlId === "turbo" && isDonorlocal ? 0xd7ffe2 : 0xeec982,
        width: visual.controlId === "turbo" && isDonorlocal ? 4 : 3,
        alpha: 0.9,
      });

      visual.art.width = artSize;
      visual.art.height = artSize;
      if ((visual.controlId === "sound" || visual.controlId === "history") && !portraitDonorRow) {
        visual.art.alpha = 0;
        visual.badge.style.fontSize = 28;
        visual.badge.style.fill = 0xfff4d6;
        visual.badge.style.stroke = { color: 0x230508, width: 4 };
        visual.badge.y = 0;
      } else if (visual.controlId === "settings" && !portraitDonorRow) {
        visual.art.alpha = 0;
        visual.badge.style.fontSize = 24;
        visual.badge.style.fill = 0xfff4d6;
        visual.badge.style.stroke = { color: 0x230508, width: 4 };
        visual.badge.y = 0;
      } else if (
        portraitDonorRow &&
        (visual.controlId === "sound" || visual.controlId === "history" || visual.controlId === "settings")
      ) {
        visual.art.alpha = 0;
        visual.badge.style.fontSize = visual.controlId === "settings" ? 28 : 32;
        visual.badge.style.fill = 0xfff4d6;
        visual.badge.style.stroke = { color: 0x230508, width: 4 };
        visual.badge.y = 0;
      } else {
        const autoplayDonor = visual.controlId === "autoplay" && isDonorlocal;
        const turboDonor = visual.controlId === "turbo" && isDonorlocal;
        const portraitTurbo = turboDonor && portraitDonorRow;
        if (autoplayDonor || turboDonor) {
          visual.fallbackPlate.visible = true;
        }
        visual.art.alpha = autoplayDonor || turboDonor ? 0 : 1;
        visual.badge.style.fontSize = portraitTurbo
          ? 18
          : turboDonor
            ? 12
          : autoplayDonor
            ? portraitDonorRow
              ? 26
              : 12
            : visual.controlId === "turbo"
              ? 16
              : 10;
        visual.badge.style.fill = portraitTurbo
          ? 0xf2fff6
          : turboDonor
          ? 0xf3fff7
          : autoplayDonor
            ? portraitDonorRow
              ? 0xf9e5ad
              : 0xd6d8de
            : 0xfff7ec;
        visual.badge.style.stroke = portraitTurbo
          ? { color: 0x0c2c18, width: 5 }
          : turboDonor
          ? { color: 0x0d2918, width: 5 }
          : autoplayDonor
            ? portraitDonorRow
              ? { color: 0x2d1107, width: 4 }
              : { color: 0x090a0d, width: 4 }
            : { color: 0x1b0408, width: 3 };
        visual.badge.style.align = "center";
        visual.badge.y = portraitTurbo
          ? -4
          : turboDonor
            ? 0
            : autoplayDonor
              ? portraitDonorRow
                ? 1
                : 0
              : visual.controlId === "turbo"
                ? 2
                : artSize * 0.4;
      }
    }

    const turboButton = this.buttons.turbo;
    this.donorTurboArt.visible = false;
    if (isDonorlocal && turboButton.visible && this.donorTurboTexture !== null && !portraitDonorRow) {
      this.donorTurboArt.texture = this.donorTurboTexture!;
      this.donorTurboArt.width = 132;
      this.donorTurboArt.height = 56;
      this.donorTurboArt.position.set(spin.x - 90, spin.y - 6);
      this.donorTurboArt.alpha = this.state.turboSelected ? 1 : 0.92;
      this.donorTurboArt.visible = true;
    }

  }

  private async refreshTextures(): Promise<void> {
    const textures = await Promise.all(
      this.secondaryVisuals.map((visual) =>
        this.resolveControlTexture(visual.frameKey),
      ),
    );

    textures.forEach((resolved, index) => {
      const visual = this.secondaryVisuals[index];
      const texture = resolved.texture;
      visual.art.texture = texture ?? Texture.WHITE;
      visual.art.tint = texture ? 0xffffff : secondarySpecs[index].fallbackTint;
      visual.fallbackPlate.visible = !texture;
    });

    const provider = getProviderPackStatus().effectiveProvider;
    const spinResolved = await resolveProviderFrameTexture("uiAtlas", "button-spin");
    this.spinTexture = spinResolved.texture;
    const [balancePlate, betPlate, winPlate] = await Promise.all([
      resolveProviderFrameTexture("uiAtlas", "hud-balance-plate"),
      resolveProviderFrameTexture("uiAtlas", "hud-bet-plate"),
      resolveProviderFrameTexture("uiAtlas", "hud-win-plate"),
    ]);
    this.balancePlateTexture = provider === "donorlocal" ? balancePlate.texture : null;
    this.betPlateTexture = provider === "donorlocal" ? betPlate.texture : null;
    this.winPlateTexture = provider === "donorlocal" ? winPlate.texture : null;

    if (provider === "donorlocal") {
      try {
        const donorTurboUrl = new URL("../image/hold_for_turbo.9418c1be.png", resolveDonorManifestUrl()).toString();
        await Assets.load(donorTurboUrl);
        this.donorTurboTexture = Texture.from(donorTurboUrl);
      } catch {
        this.donorTurboTexture = null;
      }
    } else {
      this.donorTurboTexture = null;
    }

    this.layout();
  }

  private async resolveControlTexture(frameKey: string): Promise<{
    texture: Texture | null;
  }> {
    const heroResolved = await resolveProviderFrameTexture("heroUiAtlas", frameKey);
    if (heroResolved.texture) {
      return { texture: heroResolved.texture };
    }

    const uiResolved = await resolveProviderFrameTexture("uiAtlas", frameKey);
    return { texture: uiResolved.texture ?? null };
  }
}
