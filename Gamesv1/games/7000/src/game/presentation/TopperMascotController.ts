import { Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import { resolveProviderFrameTexture } from "../../app/assets/providerPackRegistry";

export type MascotReactionState =
  | "idle"
  | "react_collect"
  | "react_boost_start"
  | "react_boost_loop"
  | "react_boost_finish"
  | "react_jackpot"
  | "react_bigwin";

const STATE_LABELS: Record<MascotReactionState, string> = {
  idle: "ROOSTER READY",
  react_collect: "COLLECT SWEEP",
  react_boost_start: "BOOST IGNITE",
  react_boost_loop: "BOOST LIVE",
  react_boost_finish: "BOOST LOCK",
  react_jackpot: "JACKPOT CALL",
  react_bigwin: "BIG WIN CALL",
};

const STATE_HOLDS_MS: Record<Exclude<MascotReactionState, "react_boost_loop">, number> = {
  idle: 0,
  react_collect: 520,
  react_boost_start: 340,
  react_boost_finish: 420,
  react_jackpot: 880,
  react_bigwin: 960,
};

type MascotPalette = {
  auraColor: number;
  auraAlpha: number;
  accentColor: number;
  captionColor: number;
};

export class TopperMascotController extends Container {
  private readonly plateGlow = new Graphics();
  private readonly plateShadow = new Graphics();
  private readonly plate = new Graphics();
  private readonly plateInset = new Graphics();
  private readonly aura = new Graphics();
  private readonly energyRing = new Graphics();
  private readonly mascotSprite = new Sprite(Texture.WHITE);
  private readonly accentSprite = new Sprite(Texture.WHITE);
  private readonly titleText = new Text({
    text: "CRAZY ROOSTER",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 28,
      fontWeight: "900",
      fill: 0xfff3cf,
      stroke: { color: 0x1f0507, width: 5 },
      letterSpacing: 1.5,
      align: "center",
    },
  });
  private readonly stateText = new Text({
    text: STATE_LABELS.idle,
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 14,
      fontWeight: "800",
      fill: 0xffd8d0,
      stroke: { color: 0x180406, width: 3 },
      letterSpacing: 1,
      align: "center",
    },
  });
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);

  private machineWidth = 0;
  private anchorX = 0;
  private anchorY = -58;
  private ambientTime = 0;
  private currentState: MascotReactionState = "idle";
  private stateElapsedMs = 0;
  private reelImpact = 0;
  private textureRequestToken = 0;

  constructor() {
    super();

    this.mascotSprite.anchor.set(0.5, 1);
    this.accentSprite.anchor.set(0.5);
    this.accentSprite.visible = false;
    this.titleText.anchor.set(0.5);
    this.stateText.anchor.set(0.5);

    this.addChild(
      this.plateGlow,
      this.plateShadow,
      this.plate,
      this.plateInset,
      this.aura,
      this.energyRing,
      this.accentSprite,
      this.mascotSprite,
      this.titleText,
      this.stateText,
    );

    Ticker.shared.add(this.motionTicker);
    void this.refreshTextures();
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public resize(machineWidth: number): void {
    this.machineWidth = machineWidth;
    this.anchorX = machineWidth * 0.5;
    this.redrawBase();
  }

  public setState(state: MascotReactionState): void {
    this.currentState = state;
    this.stateElapsedMs = 0;
    this.stateText.text = STATE_LABELS[state];
    this.reelImpact = Math.max(this.reelImpact, state === "idle" ? 0.1 : 0.6);
    void this.refreshTextures();
  }

  public pulseForReelStop(index: number): void {
    this.reelImpact = Math.max(this.reelImpact, 0.35 + (2 - index) * 0.08);
  }

  public getFocusPoint(): { x: number; y: number } {
    return { x: this.anchorX, y: this.anchorY - 80 };
  }

  private redrawBase(): void {
    const plateWidth = 304;
    const plateHeight = 170;

    this.plateGlow.clear();
    this.plateGlow.roundRect(
      this.anchorX - plateWidth * 0.5 - 14,
      this.anchorY - 174,
      plateWidth + 28,
      plateHeight + 26,
      44,
    );
    this.plateGlow.fill({ color: 0x701017, alpha: 0.28 });
    this.plateGlow.stroke({ color: 0xc7141a, width: 10, alpha: 0.26 });

    this.plateShadow.clear();
    this.plateShadow.roundRect(
      this.anchorX - plateWidth * 0.5 + 12,
      this.anchorY - 150,
      plateWidth,
      plateHeight,
      42,
    );
    this.plateShadow.fill({ color: 0x050102, alpha: 0.38 });

    this.plate.clear();
    this.plate.roundRect(
      this.anchorX - plateWidth * 0.5,
      this.anchorY - 160,
      plateWidth,
      plateHeight,
      42,
    );
    this.plate.fill({ color: 0x150305, alpha: 0.9 });
    this.plate.stroke({ color: 0xe8bb74, width: 3, alpha: 0.88 });

    this.plateInset.clear();
    this.plateInset.roundRect(
      this.anchorX - plateWidth * 0.5 + 12,
      this.anchorY - 148,
      plateWidth - 24,
      plateHeight - 36,
      34,
    );
    this.plateInset.fill({ color: 0x2a070a, alpha: 0.72 });
    this.plateInset.stroke({ color: 0xffe0a2, width: 2, alpha: 0.45 });

    this.mascotSprite.x = this.anchorX;
    this.mascotSprite.y = this.anchorY;
    this.mascotSprite.width = 154;
    this.mascotSprite.height = 154;

    this.accentSprite.x = this.anchorX + 102;
    this.accentSprite.y = this.anchorY - 122;
    this.accentSprite.width = 50;
    this.accentSprite.height = 50;

    this.titleText.x = this.anchorX;
    this.titleText.y = this.anchorY - 150;
    this.titleText.style.fontSize = 28;
    this.stateText.x = this.anchorX;
    this.stateText.y = this.anchorY + 4;
    this.stateText.style.fontSize = 14;
  }

  private tick(deltaMs: number): void {
    if (this.machineWidth <= 0) {
      return;
    }

    this.ambientTime += deltaMs / 1000;
    this.stateElapsedMs += deltaMs;
    this.reelImpact = Math.max(0, this.reelImpact - deltaMs / 260);

    if (
      this.currentState !== "idle" &&
      this.currentState !== "react_boost_loop" &&
      this.stateElapsedMs >= STATE_HOLDS_MS[this.currentState as Exclude<MascotReactionState, "react_boost_loop">]
    ) {
      this.setState("idle");
      return;
    }

    const palette = this.resolvePalette();
    const statePulse = this.resolveStatePulse();
    const floatY = Math.sin(this.ambientTime * 1.45) * 6 + this.reelImpact * -8;
    const scale = 1 + statePulse.scaleBoost + Math.sin(this.ambientTime * 1.8) * 0.03;

    this.mascotSprite.y = this.anchorY + floatY;
    this.mascotSprite.scale.set(scale);
    this.mascotSprite.rotation = statePulse.rotation;

    this.accentSprite.visible = statePulse.showAccent;
    this.accentSprite.alpha = statePulse.accentAlpha;
    this.accentSprite.scale.set(0.95 + statePulse.scaleBoost * 0.6);
    this.accentSprite.rotation = -statePulse.rotation * 0.7;

    this.aura.clear();
    this.aura.ellipse(
      this.anchorX,
      this.anchorY - 42 + floatY * 0.2,
      132 + statePulse.glowRadius,
      40 + statePulse.glowRadius * 0.2,
    );
    this.aura.fill({
      color: palette.auraColor,
      alpha: palette.auraAlpha + statePulse.alphaBoost,
    });

    this.energyRing.clear();
    this.energyRing.circle(
      this.anchorX,
      this.anchorY - 66 + floatY * 0.12,
      76 + statePulse.glowRadius * 0.44,
    );
    this.energyRing.stroke({
      color: palette.accentColor,
      width: 3 + statePulse.scaleBoost * 20,
      alpha: 0.38 + statePulse.alphaBoost * 0.65,
    });

    this.titleText.style.fill = palette.captionColor;
    this.stateText.style.fill = palette.captionColor;
  }

  private resolvePalette(): MascotPalette {
    switch (this.currentState) {
      case "react_collect":
        return { auraColor: 0xffd86b, auraAlpha: 0.2, accentColor: 0xffefb0, captionColor: 0xfff0c9 };
      case "react_boost_start":
      case "react_boost_loop":
      case "react_boost_finish":
        return { auraColor: 0xc7141a, auraAlpha: 0.24, accentColor: 0xfff0b4, captionColor: 0xffe2cc };
      case "react_jackpot":
        return { auraColor: 0xffb648, auraAlpha: 0.28, accentColor: 0xfff5c4, captionColor: 0xfff0be };
      case "react_bigwin":
        return { auraColor: 0xff4f95, auraAlpha: 0.25, accentColor: 0xffffff, captionColor: 0xfff7d7 };
      case "idle":
      default:
        return { auraColor: 0xc7141a, auraAlpha: 0.16, accentColor: 0xffd98b, captionColor: 0xffefce };
    }
  }

  private resolveStatePulse(): {
    scaleBoost: number;
    alphaBoost: number;
    glowRadius: number;
    rotation: number;
    showAccent: boolean;
    accentAlpha: number;
  } {
    switch (this.currentState) {
      case "react_collect":
        return {
          scaleBoost: 0.06,
          alphaBoost: 0.08,
          glowRadius: 20,
          rotation: Math.sin(this.ambientTime * 4) * 0.03,
          showAccent: true,
          accentAlpha: 0.92,
        };
      case "react_boost_start":
        return {
          scaleBoost: 0.12,
          alphaBoost: 0.16,
          glowRadius: 34,
          rotation: Math.sin(this.ambientTime * 7) * 0.05,
          showAccent: true,
          accentAlpha: 1,
        };
      case "react_boost_loop":
        return {
          scaleBoost: 0.1,
          alphaBoost: 0.14,
          glowRadius: 28 + Math.sin(this.ambientTime * 5.2) * 6,
          rotation: Math.sin(this.ambientTime * 5.8) * 0.035,
          showAccent: true,
          accentAlpha: 0.94,
        };
      case "react_boost_finish":
        return {
          scaleBoost: 0.08,
          alphaBoost: 0.12,
          glowRadius: 24,
          rotation: Math.sin(this.ambientTime * 4) * 0.02,
          showAccent: true,
          accentAlpha: 0.82,
        };
      case "react_jackpot":
        return {
          scaleBoost: 0.14,
          alphaBoost: 0.18,
          glowRadius: 40,
          rotation: Math.sin(this.ambientTime * 6.5) * 0.04,
          showAccent: true,
          accentAlpha: 1,
        };
      case "react_bigwin":
        return {
          scaleBoost: 0.11,
          alphaBoost: 0.16,
          glowRadius: 32,
          rotation: Math.sin(this.ambientTime * 4.5) * 0.025,
          showAccent: true,
          accentAlpha: 0.94,
        };
      case "idle":
      default:
        return {
          scaleBoost: 0.02,
          alphaBoost: 0,
          glowRadius: 10,
          rotation: 0,
          showAccent: false,
          accentAlpha: 0,
        };
    }
  }

  private async refreshTextures(): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const [mascot, collect, boost, jackpot] = await Promise.all([
      resolveProviderFrameTexture("symbolAtlas", "symbol-9-rooster"),
      resolveProviderFrameTexture("symbolAtlas", "collector-symbol"),
      resolveProviderFrameTexture("symbolAtlas", "symbol-8-bolt"),
      resolveProviderFrameTexture("symbolAtlas", "coin-multiplier-10x"),
    ]);

    if (requestToken !== this.textureRequestToken) {
      return;
    }

    this.mascotSprite.texture = mascot.texture ?? Texture.WHITE;
    this.mascotSprite.tint = mascot.texture ? 0xffffff : 0xd14d30;

    let accentTexture: Texture | null = null;
    let accentTint = 0xffefb0;
    if (this.currentState === "react_collect") {
      accentTexture = collect.texture;
      accentTint = 0xffdb71;
    } else if (
      this.currentState === "react_boost_start" ||
      this.currentState === "react_boost_loop" ||
      this.currentState === "react_boost_finish"
    ) {
      accentTexture = boost.texture;
      accentTint = 0xffd97a;
    } else if (this.currentState === "react_jackpot" || this.currentState === "react_bigwin") {
      accentTexture = jackpot.texture;
      accentTint = 0xffcf5a;
    }

    this.accentSprite.texture = accentTexture ?? Texture.WHITE;
    this.accentSprite.tint = accentTexture ? 0xffffff : accentTint;
  }
}
