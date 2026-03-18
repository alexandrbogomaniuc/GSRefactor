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
  private readonly spinGlow = new Graphics();
  private readonly spinPlate = new Graphics();
  private readonly spinFace = new Graphics();
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
    this.addChild(this.railGlow, this.railPlate, this.spinGlow, this.spinPlate, this.spinFace);
    this.addChild(this.spinText, this.spinSubtext);

    this.secondaryVisuals = secondarySpecs.map((spec) => {
      const container = new Container();
      const glow = new Graphics();
      const fallbackPlate = new Graphics();
      const art = new Sprite(Texture.WHITE);
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
    this.railGlow.roundRect(minLeft - 26, minTop - 28, maxRight - minLeft + 52, maxBottom - minTop + 54, 42);
    this.railGlow.stroke({ color: 0xc7141a, width: 14, alpha: 0.28 });

    this.railPlate.clear();
    this.railPlate.roundRect(minLeft - 18, minTop - 18, maxRight - minLeft + 36, maxBottom - minTop + 34, 38);
    this.railPlate.fill({ color: 0x070305, alpha: 0.5 });
    this.railPlate.stroke({ color: 0xf3c575, width: 3, alpha: 0.74 });

    const spinButton = this.buttons.spin;
    this.spinText.x = spinButton.x;
    this.spinText.y = spinButton.y - 6;
    this.spinSubtext.x = spinButton.x;
    this.spinSubtext.y = spinButton.y + spinButton.height * 0.36;

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

    for (const visual of this.secondaryVisuals) {
      const button = this.buttons[visual.controlId];
      const centerX = button.x;
      const centerY = button.y;
      const artSize = visual.controlId === "buyFeature" ? 112 : 102;

      visual.container.visible = button.visible;
      visual.container.position.set(centerX, centerY - 2);

      visual.glow.clear();
      visual.glow.circle(0, 0, artSize * 0.56);
      visual.glow.fill({ color: 0xc7141a, alpha: 0.22 });

      visual.fallbackPlate.clear();
      visual.fallbackPlate.circle(0, 0, artSize * 0.46);
      visual.fallbackPlate.fill({ color: 0x180406, alpha: 0.96 });
      visual.fallbackPlate.stroke({ color: 0xf3c575, width: 4, alpha: 0.9 });

      visual.art.width = artSize;
      visual.art.height = artSize;
      visual.badge.y = artSize * 0.44;
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
