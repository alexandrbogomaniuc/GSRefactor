import { Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import type { PremiumHudControlId } from "@gamesv1/ui-kit";

import { resolveProviderFrameTexture } from "../../assets/providerPackRegistry.ts";

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
  holdTurboRequested: boolean;
};

type SecondaryControlVisual = {
  controlId: Exclude<PremiumHudControlId, "spin">;
  frameKey: string;
  container: Container;
  glow: Graphics;
  fallbackPlate: Graphics;
  art: Sprite;
  badgePlate: Graphics;
  badge: Text;
};

const secondarySpecs: Array<{
  controlId: Exclude<PremiumHudControlId, "spin">;
  frameKey: string;
  fallbackTint: number;
}> = [
  { controlId: "turbo", frameKey: "button-turbo", fallbackTint: 0x3db7ff },
  { controlId: "autoplay", frameKey: "button-autoplay", fallbackTint: 0xdb42ff },
  { controlId: "buyFeature", frameKey: "button-buybonus", fallbackTint: 0xffbc52 },
  { controlId: "sound", frameKey: "button-sound", fallbackTint: 0xff9c2d },
  { controlId: "settings", frameKey: "button-settings", fallbackTint: 0xff8e35 },
  { controlId: "history", frameKey: "button-history", fallbackTint: 0xc89153 },
];

export class HeroHudChrome extends Container {
  private readonly railGlow = new Graphics();
  private readonly railPlate = new Graphics();
  private readonly railInset = new Graphics();
  private readonly spinShadow = new Graphics();
  private readonly spinGlow = new Graphics();
  private readonly spinPlate = new Graphics();
  private readonly spinFace = new Graphics();
  private readonly spinBadgePlate = new Graphics();
  private readonly spinText = new Text({
    text: "SPIN",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 32,
      fontWeight: "900",
      fill: 0xfffbf2,
      stroke: { color: 0x1d0408, width: 5 },
      letterSpacing: 1,
    },
  });
  private readonly spinSubtext = new Text({
    text: "HOLD FOR TURBO",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 11,
      fontWeight: "900",
      fill: 0xffd899,
      stroke: { color: 0x160406, width: 3 },
      letterSpacing: 1,
    },
  });
  private readonly secondaryVisuals: SecondaryControlVisual[];
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);

  private buttons: HudButtons | null = null;
  private ambientTime = 0;
  private state: HudChromeState = {
    autoplayActive: false,
    turboSelected: false,
    soundEnabled: true,
    buyLabel: "BONUS 75",
    holdTurboRequested: false,
  };

  constructor() {
    super();

    this.visible = false;
    this.spinText.anchor.set(0.5);
    this.spinSubtext.anchor.set(0.5);
    this.addChild(
      this.railGlow,
      this.railPlate,
      this.railInset,
      this.spinShadow,
      this.spinGlow,
      this.spinPlate,
      this.spinFace,
      this.spinBadgePlate,
    );
    this.addChild(this.spinText, this.spinSubtext);

    this.secondaryVisuals = secondarySpecs.map((spec) => {
      const container = new Container();
      const glow = new Graphics();
      const fallbackPlate = new Graphics();
      const art = new Sprite(Texture.WHITE);
      const badgePlate = new Graphics();
      const badge = new Text({
        text: "",
        style: {
          fontFamily: "Trebuchet MS, Arial, sans-serif",
          fontSize: 12,
          fontWeight: "900",
          fill: 0xfff6ec,
          stroke: { color: 0x190408, width: 3 },
          letterSpacing: 1,
        },
      });

      art.anchor.set(0.5);
      badge.anchor.set(0.5);
      container.addChild(glow, fallbackPlate, art, badgePlate, badge);
      this.addChild(container);

      return {
        controlId: spec.controlId,
        frameKey: spec.frameKey,
        container,
        glow,
        fallbackPlate,
        art,
        badgePlate,
        badge,
      };
    });

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
    const pulse = 0.86 + Math.sin(this.ambientTime * 2) * 0.08;
    this.railGlow.alpha = 0.26 + Math.sin(this.ambientTime * 1.7) * 0.06;
    this.spinGlow.alpha = 0.36 + Math.sin(this.ambientTime * 2.8) * 0.08;
    this.spinFace.scale.set(1 + Math.sin(this.ambientTime * 2.4) * 0.01);
    this.secondaryVisuals.forEach((visual, index) => {
      visual.container.scale.set(0.98 + Math.sin(this.ambientTime * 2.1 + index * 0.5) * 0.015);
      visual.glow.alpha = pulse * 0.32;
    });
  }

  private refreshBadges(): void {
    for (const visual of this.secondaryVisuals) {
      switch (visual.controlId) {
        case "turbo":
          visual.badge.text = this.state.turboSelected ? "ARMED" : "HOLD";
          break;
        case "autoplay":
          visual.badge.text = this.state.autoplayActive ? "STOP" : "START";
          break;
        case "buyFeature":
          visual.badge.text = this.state.buyLabel.replace("BONUS ", "");
          break;
        case "sound":
          visual.badge.text = this.state.soundEnabled ? "LIVE" : "OFF";
          break;
        case "settings":
          visual.badge.text = "MENU";
          break;
        case "history":
          visual.badge.text = "LOG";
          break;
      }
    }

    this.spinSubtext.text = this.state.holdTurboRequested ? "TURBO READY" : "HOLD FOR TURBO";
  }

  private layout(): void {
    if (!this.buttons) {
      return;
    }

    const visibleButtons = Object.values(this.buttons).filter((button) => button.visible);
    if (visibleButtons.length === 0) {
      this.visible = false;
      return;
    }

    this.visible = true;
    let minLeft = Number.POSITIVE_INFINITY;
    let maxRight = Number.NEGATIVE_INFINITY;
    let minTop = Number.POSITIVE_INFINITY;
    let maxBottom = Number.NEGATIVE_INFINITY;

    for (const button of visibleButtons) {
      minLeft = Math.min(minLeft, button.x - button.width * 0.5);
      maxRight = Math.max(maxRight, button.x + button.width * 0.5);
      minTop = Math.min(minTop, button.y - button.height * 0.5);
      maxBottom = Math.max(maxBottom, button.y + button.height * 0.5);
    }

    this.railGlow.clear();
    this.railGlow.roundRect(
      minLeft - 34,
      minTop - 34,
      maxRight - minLeft + 68,
      maxBottom - minTop + 68,
      48,
    );
    this.railGlow.fill({ color: 0x53090e, alpha: 0.16 });
    this.railGlow.stroke({ color: 0xc7141a, width: 16, alpha: 0.24 });

    this.railPlate.clear();
    this.railPlate.roundRect(
      minLeft - 22,
      minTop - 22,
      maxRight - minLeft + 44,
      maxBottom - minTop + 42,
      42,
    );
    this.railPlate.fill({ color: 0x090305, alpha: 0.78 });
    this.railPlate.stroke({ color: 0xf3c575, width: 4, alpha: 0.82 });

    this.railInset.clear();
    this.railInset.roundRect(
      minLeft - 12,
      minTop - 12,
      maxRight - minLeft + 24,
      maxBottom - minTop + 18,
      34,
    );
    this.railInset.fill({ color: 0x1c0408, alpha: 0.76 });
    this.railInset.stroke({ color: 0x5d0c11, width: 2, alpha: 0.74 });

    const spinButton = this.buttons.spin;
    this.spinText.x = spinButton.x;
    this.spinText.y = spinButton.y - 10;
    this.spinSubtext.x = spinButton.x;
    this.spinSubtext.y = spinButton.y + spinButton.height * 0.36;

    this.spinShadow.clear();
    this.spinShadow.roundRect(
      spinButton.x - spinButton.width * 0.58,
      spinButton.y - spinButton.height * 0.5 + 10,
      spinButton.width * 1.16,
      spinButton.height * 1.08,
      42,
    );
    this.spinShadow.fill({ color: 0x050102, alpha: 0.4 });

    this.spinGlow.clear();
    this.spinGlow.roundRect(
      spinButton.x - spinButton.width * 0.53,
      spinButton.y - spinButton.height * 0.58,
      spinButton.width * 1.06,
      spinButton.height * 1.16,
      38,
    );
    this.spinGlow.stroke({ color: 0xc7141a, width: 14, alpha: 0.28 });

    this.spinPlate.clear();
    this.spinPlate.roundRect(
      spinButton.x - spinButton.width * 0.5,
      spinButton.y - spinButton.height * 0.5,
      spinButton.width,
      spinButton.height,
      34,
    );
    this.spinPlate.fill({ color: 0x1c0408, alpha: 0.92 });
    this.spinPlate.stroke({ color: 0xf4c97d, width: 3, alpha: 0.94 });

    this.spinFace.clear();
    this.spinFace.roundRect(
      spinButton.x - spinButton.width * 0.44,
      spinButton.y - spinButton.height * 0.34,
      spinButton.width * 0.88,
      spinButton.height * 0.58,
      26,
    );
    this.spinFace.fill({ color: 0xc7141a, alpha: 0.94 });
    this.spinFace.stroke({ color: 0xfff2d2, width: 3, alpha: 0.95 });

    this.spinBadgePlate.clear();
    this.spinBadgePlate.roundRect(
      spinButton.x - spinButton.width * 0.26,
      spinButton.y + spinButton.height * 0.22,
      spinButton.width * 0.52,
      24,
      12,
    );
    this.spinBadgePlate.fill({ color: 0x3b080b, alpha: 0.9 });
    this.spinBadgePlate.stroke({ color: 0xffd78a, width: 2, alpha: 0.82 });

    for (const visual of this.secondaryVisuals) {
      const button = this.buttons[visual.controlId];
      const centerX = button.x;
      const centerY = button.y;
      const artSize = visual.controlId === "buyFeature" ? 118 : 104;

      visual.container.visible = button.visible;
      visual.container.position.set(centerX, centerY - 2);

      visual.glow.clear();
      visual.glow.circle(0, 0, artSize * 0.58);
      visual.glow.fill({ color: 0xc7141a, alpha: 0.18 });
      visual.glow.stroke({ color: 0xffc96e, width: 3, alpha: 0.14 });

      visual.fallbackPlate.clear();
      visual.fallbackPlate.circle(0, 0, artSize * 0.48);
      visual.fallbackPlate.fill({ color: 0x180406, alpha: 0.96 });
      visual.fallbackPlate.stroke({ color: 0xf3c575, width: 4, alpha: 0.9 });

      visual.art.width = artSize;
      visual.art.height = artSize;
      visual.badgePlate.clear();
      visual.badgePlate.roundRect(-30, artSize * 0.38, 60, 20, 10);
      visual.badgePlate.fill({ color: 0x3b080b, alpha: 0.9 });
      visual.badgePlate.stroke({ color: 0xffd78a, width: 2, alpha: 0.82 });
      visual.badge.y = artSize * 0.48;
    }
  }

  private async refreshTextures(): Promise<void> {
    const textures = await Promise.all(
      this.secondaryVisuals.map((visual) =>
        resolveProviderFrameTexture("heroUiAtlas", visual.frameKey),
      ),
    );

    textures.forEach((resolved, index) => {
      const visual = this.secondaryVisuals[index];
      const texture = resolved.texture;
      visual.art.texture = texture ?? Texture.WHITE;
      visual.art.tint = texture ? 0xffffff : secondarySpecs[index].fallbackTint;
      visual.fallbackPlate.visible = !texture;
    });

    this.layout();
  }
}
