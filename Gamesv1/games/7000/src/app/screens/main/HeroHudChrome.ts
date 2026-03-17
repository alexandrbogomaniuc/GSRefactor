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

type DonorHudTextures = Partial<
  Record<Exclude<PremiumHudControlId, "spin">, Texture | null>
>;

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
  private static donorHudTexturesPromise: Promise<DonorHudTextures> | null = null;

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
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    this.railGlow.alpha = (isDonorlocal ? 0.18 : 0.26) + Math.sin(this.ambientTime * 1.7) * 0.06;
    this.spinGlow.alpha = (isDonorlocal ? 0.24 : 0.36) + Math.sin(this.ambientTime * 2.8) * 0.08;
    this.spinFace.scale.set(1 + Math.sin(this.ambientTime * 2.4) * 0.01);
    this.secondaryVisuals.forEach((visual, index) => {
      visual.container.scale.set(0.98 + Math.sin(this.ambientTime * 2.1 + index * 0.5) * 0.015);
      visual.glow.alpha = pulse * (isDonorlocal ? 0.22 : 0.32);
    });
  }

  private refreshBadges(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    for (const visual of this.secondaryVisuals) {
      switch (visual.controlId) {
        case "turbo":
          visual.badge.text = isDonorlocal
            ? this.state.turboSelected
              ? "ARMED"
              : ""
            : this.state.turboSelected
              ? "ARMED"
              : "HOLD";
          break;
        case "autoplay":
          visual.badge.text = this.state.autoplayActive ? "STOP" : isDonorlocal ? "AUTO" : "START";
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
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";

    this.railGlow.clear();
    this.railGlow.roundRect(
      minLeft - (isDonorlocal ? 22 : 34),
      minTop - (isDonorlocal ? 18 : 34),
      maxRight - minLeft + (isDonorlocal ? 44 : 68),
      maxBottom - minTop + (isDonorlocal ? 38 : 68),
      isDonorlocal ? 36 : 48,
    );
    this.railGlow.fill({ color: isDonorlocal ? 0x2d0904 : 0x53090e, alpha: isDonorlocal ? 0.1 : 0.16 });
    this.railGlow.stroke({ color: isDonorlocal ? 0xf1c56c : 0xc7141a, width: isDonorlocal ? 10 : 16, alpha: isDonorlocal ? 0.18 : 0.24 });

    this.railPlate.clear();
    this.railPlate.roundRect(
      minLeft - (isDonorlocal ? 16 : 22),
      minTop - (isDonorlocal ? 12 : 22),
      maxRight - minLeft + (isDonorlocal ? 32 : 44),
      maxBottom - minTop + (isDonorlocal ? 26 : 42),
      isDonorlocal ? 30 : 42,
    );
    this.railPlate.fill({ color: isDonorlocal ? 0x0b0204 : 0x090305, alpha: isDonorlocal ? 0.9 : 0.78 });
    this.railPlate.stroke({ color: 0xf3c575, width: isDonorlocal ? 3 : 4, alpha: isDonorlocal ? 0.76 : 0.82 });

    this.railInset.clear();
    this.railInset.roundRect(
      minLeft - (isDonorlocal ? 10 : 12),
      minTop - (isDonorlocal ? 8 : 12),
      maxRight - minLeft + (isDonorlocal ? 20 : 24),
      maxBottom - minTop + (isDonorlocal ? 10 : 18),
      isDonorlocal ? 24 : 34,
    );
    this.railInset.fill({ color: isDonorlocal ? 0x160406 : 0x1c0408, alpha: isDonorlocal ? 0.86 : 0.76 });
    this.railInset.stroke({ color: 0x5d0c11, width: 2, alpha: isDonorlocal ? 0.52 : 0.74 });

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
      spinButton.x - spinButton.width * (isDonorlocal ? 0.48 : 0.53),
      spinButton.y - spinButton.height * (isDonorlocal ? 0.54 : 0.58),
      spinButton.width * (isDonorlocal ? 0.96 : 1.06),
      spinButton.height * (isDonorlocal ? 1.04 : 1.16),
      isDonorlocal ? 30 : 38,
    );
    this.spinGlow.stroke({ color: isDonorlocal ? 0xf4c86d : 0xc7141a, width: isDonorlocal ? 8 : 14, alpha: isDonorlocal ? 0.22 : 0.28 });

    this.spinPlate.clear();
    this.spinPlate.roundRect(
      spinButton.x - spinButton.width * 0.5,
      spinButton.y - spinButton.height * 0.5,
      spinButton.width,
      spinButton.height,
      34,
    );
    this.spinPlate.fill({ color: isDonorlocal ? 0x140406 : 0x1c0408, alpha: 0.92 });
    this.spinPlate.stroke({ color: 0xf4c97d, width: 3, alpha: isDonorlocal ? 0.82 : 0.94 });

    this.spinFace.clear();
    this.spinFace.roundRect(
      spinButton.x - spinButton.width * 0.44,
      spinButton.y - spinButton.height * 0.34,
      spinButton.width * 0.88,
      spinButton.height * 0.58,
      26,
    );
    this.spinFace.fill({ color: isDonorlocal ? 0xa81312 : 0xc7141a, alpha: 0.94 });
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
      const centerY = button.y + (isDonorlocal ? 2 : -2);
      const artWidth =
        isDonorlocal && visual.controlId === "buyFeature"
          ? 164
          : isDonorlocal && visual.controlId === "turbo"
            ? 124
            : isDonorlocal && visual.controlId === "autoplay"
              ? 62
              : visual.controlId === "buyFeature"
                ? 118
                : 104;
      const artHeight =
        isDonorlocal && visual.controlId === "buyFeature"
          ? 44
          : isDonorlocal && visual.controlId === "turbo"
            ? 28
            : artWidth;

      visual.container.visible = button.visible;
      visual.container.position.set(centerX, centerY);

      visual.glow.clear();
      if (isDonorlocal && visual.controlId !== "sound" && visual.controlId !== "settings" && visual.controlId !== "history") {
        visual.glow.roundRect(-artWidth * 0.56, -artHeight * 0.66, artWidth * 1.12, artHeight * 1.32, 18);
        visual.glow.fill({ color: 0x5e1406, alpha: 0.08 });
        visual.glow.stroke({ color: 0xffd986, width: 3, alpha: 0.18 });
      } else {
        const artSize = Math.max(artWidth, artHeight);
        visual.glow.circle(0, 0, artSize * 0.58);
        visual.glow.fill({ color: 0xc7141a, alpha: 0.18 });
        visual.glow.stroke({ color: 0xffc96e, width: 3, alpha: 0.14 });
      }

      visual.fallbackPlate.clear();
      if (isDonorlocal && visual.controlId === "buyFeature") {
        visual.fallbackPlate.roundRect(-84, -24, 168, 48, 18);
        visual.fallbackPlate.fill({ color: 0x1c0608, alpha: 0.94 });
        visual.fallbackPlate.stroke({ color: 0xf2cb76, width: 3, alpha: 0.88 });
      } else if (isDonorlocal && visual.controlId === "turbo") {
        visual.fallbackPlate.roundRect(-72, -20, 144, 40, 18);
        visual.fallbackPlate.fill({ color: 0x160406, alpha: 0.94 });
        visual.fallbackPlate.stroke({ color: 0xf2cb76, width: 2.5, alpha: 0.78 });
      } else {
        const plateSize = Math.max(artWidth, artHeight);
        visual.fallbackPlate.circle(0, 0, plateSize * 0.48);
        visual.fallbackPlate.fill({ color: 0x180406, alpha: 0.96 });
        visual.fallbackPlate.stroke({ color: 0xf3c575, width: 4, alpha: 0.9 });
      }

      visual.art.width = artWidth;
      visual.art.height = artHeight;
      visual.badgePlate.clear();
      if (isDonorlocal && visual.controlId === "buyFeature") {
        visual.badgePlate.roundRect(36, -14, 46, 28, 12);
        visual.badgePlate.fill({ color: 0x4b1203, alpha: 0.9 });
        visual.badgePlate.stroke({ color: 0xfff0b6, width: 2, alpha: 0.88 });
        visual.badge.x = 59;
        visual.badge.y = 0;
      } else if (isDonorlocal && visual.controlId === "turbo") {
        visual.badgePlate.roundRect(-28, 18, 56, 18, 9);
        visual.badgePlate.fill({ color: 0x3b080b, alpha: this.state.turboSelected ? 0.84 : 0 });
        visual.badgePlate.stroke({ color: 0xffd78a, width: 1.5, alpha: this.state.turboSelected ? 0.72 : 0 });
        visual.badge.x = 0;
        visual.badge.y = 27;
      } else {
        const badgeY = Math.max(artHeight, artWidth) * 0.48;
        visual.badgePlate.roundRect(-30, badgeY - 10, 60, 20, 10);
        visual.badgePlate.fill({ color: 0x3b080b, alpha: 0.9 });
        visual.badgePlate.stroke({ color: 0xffd78a, width: 2, alpha: 0.82 });
        visual.badge.x = 0;
        visual.badge.y = badgeY;
      }
      visual.badge.visible = !(isDonorlocal && visual.controlId === "turbo" && !this.state.turboSelected);
    }
  }

  private async refreshTextures(): Promise<void> {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const donorTextures = isDonorlocal ? await HeroHudChrome.resolveDonorHudTextures() : {};
    const textures = await Promise.all(
      this.secondaryVisuals.map(async (visual) => {
        const directTexture = donorTextures[visual.controlId];
        if (directTexture) {
          return { texture: directTexture, donorDirect: true };
        }

        const hero = await resolveProviderFrameTexture("heroUiAtlas", visual.frameKey);
        if (hero.texture) {
          return { texture: hero.texture, donorDirect: false };
        }

        const ui = await resolveProviderFrameTexture("uiAtlas", visual.frameKey);
        return { texture: ui.texture, donorDirect: false };
      }),
    );

    textures.forEach((resolved, index) => {
      const visual = this.secondaryVisuals[index];
      const texture = resolved.texture;
      visual.art.texture = texture ?? Texture.WHITE;
      visual.art.tint = texture ? 0xffffff : secondarySpecs[index].fallbackTint;
      visual.art.visible = Boolean(texture);
      visual.fallbackPlate.visible = !texture || (isDonorlocal && resolved.donorDirect);
    });

    this.layout();
  }

  private static async resolveDonorHudTextures(): Promise<DonorHudTextures> {
    if (!HeroHudChrome.donorHudTexturesPromise) {
      HeroHudChrome.donorHudTexturesPromise = (async () => {
        const manifestUrl = new URL(getDonorLocalManifestUrl(), window.location.origin).toString();
        const entries: Array<[Exclude<PremiumHudControlId, "spin">, string]> = [
          ["turbo", new URL("../image/hold_for_turbo.9418c1be.png", manifestUrl).toString()],
          ["autoplay", new URL("../image/arrows.44cbe5f3.png", manifestUrl).toString()],
          ["buyFeature", new URL("../image/btn_buy_bonus.1d85b9e0.png", manifestUrl).toString()],
        ];

        const output: DonorHudTextures = {};
        await Promise.all(
          entries.map(async ([key, url]) => {
            try {
              await Assets.load(url);
              output[key] = Texture.from(url);
            } catch {
              output[key] = null;
            }
          }),
        );
        return output;
      })();
    }

    return HeroHudChrome.donorHudTexturesPromise;
  }
}
