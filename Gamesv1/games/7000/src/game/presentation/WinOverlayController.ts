import { Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import { resolveProviderFrameTexture } from "../../app/assets/providerPackRegistry";

type OverlayKind = "big" | "mega" | "total";

type OverlayStep = {
  kind: OverlayKind;
  title: string;
  subtitle: string;
  accent: number;
  durationMs: number;
  frameKey: string;
};

type OverlaySequenceInput = {
  tier: "none" | "big" | "huge" | "mega";
  amountMinor: number;
  messages: string[];
};

export class WinOverlayController extends Container {
  private readonly backdrop = new Graphics();
  private readonly plateGlow = new Graphics();
  private readonly plate = new Graphics();
  private readonly accentBar = new Graphics();
  private readonly emblem = new Sprite(Texture.WHITE);
  private readonly titleText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 40,
      fontWeight: "900",
      fill: 0xfff0cf,
      stroke: { color: 0x180406, width: 5 },
      letterSpacing: 2,
      align: "center",
    },
  });
  private readonly amountText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 54,
      fontWeight: "900",
      fill: 0xffffff,
      stroke: { color: 0x180406, width: 6 },
      align: "center",
    },
  });
  private readonly subtitleText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 16,
      fontWeight: "700",
      fill: 0xffead2,
      align: "center",
      wordWrap: true,
      wordWrapWidth: 400,
    },
  });
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);

  private viewport = { width: 1280, height: 720, safeTop: 0 };
  private steps: OverlayStep[] = [];
  private currentIndex = -1;
  private stepElapsedMs = 0;
  private textureRequestToken = 0;
  private sequenceAmountMinor = 0;
  private pulsePhase = 0;
  private activeAccent = 0xffcf6c;

  constructor() {
    super();
    this.visible = false;
    this.emblem.anchor.set(0.5);
    this.titleText.anchor.set(0.5);
    this.amountText.anchor.set(0.5);
    this.subtitleText.anchor.set(0.5);
    this.addChild(
      this.backdrop,
      this.plateGlow,
      this.plate,
      this.accentBar,
      this.emblem,
      this.titleText,
      this.amountText,
      this.subtitleText,
    );
    Ticker.shared.add(this.motionTicker);
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public resize(width: number, height: number, safeTop = 0): void {
    this.viewport = { width, height, safeTop };
    this.layout();
  }

  public playSequence(input: OverlaySequenceInput): number {
    this.sequenceAmountMinor = input.amountMinor;
    this.steps = this.buildSteps(input);
    this.currentIndex = -1;
    this.stepElapsedMs = 0;

    if (this.steps.length === 0) {
      this.clear();
      return 0;
    }

    this.visible = true;
    this.alpha = 1;
    this.scale.set(1);
    this.activateStep(0);
    return this.steps.reduce((sum, step) => sum + step.durationMs, 0);
  }

  public clear(): void {
    this.steps = [];
    this.currentIndex = -1;
    this.stepElapsedMs = 0;
    this.visible = false;
  }

  private tick(deltaMs: number): void {
    if (!this.visible || this.currentIndex < 0 || this.currentIndex >= this.steps.length) {
      return;
    }

    this.stepElapsedMs += deltaMs;
    this.pulsePhase += deltaMs / 1000;
    const step = this.steps[this.currentIndex];
    const progress = Math.min(1, this.stepElapsedMs / step.durationMs);
    const entrance = Math.min(1, progress / 0.18);
    const exit = progress > 0.82 ? (1 - progress) / 0.18 : 1;
    const alpha = Math.max(0, Math.min(1, entrance, exit));

    this.alpha = alpha;
    const scale = 0.94 + entrance * 0.08 + Math.sin(this.pulsePhase * 4.4) * 0.012;
    this.scale.set(scale);

    this.plateGlow.clear();
    this.plateGlow.roundRect(
      this.viewport.width * 0.5 - 248,
      this.viewport.safeTop + 128,
      496,
      214,
      48,
    );
    this.plateGlow.stroke({ color: this.activeAccent, width: 8, alpha: 0.2 + alpha * 0.55 });

    if (this.stepElapsedMs >= step.durationMs) {
      const nextIndex = this.currentIndex + 1;
      if (nextIndex >= this.steps.length) {
        this.clear();
        return;
      }
      this.activateStep(nextIndex);
    }
  }

  private buildSteps(input: OverlaySequenceInput): OverlayStep[] {
    if (input.amountMinor <= 0) {
      return [];
    }

    const headline = input.messages[0] ?? "ROOSTER RUSH";
    const totalStep: OverlayStep = {
      kind: "total",
      title: "TOTAL WIN",
      subtitle: headline,
      accent: 0xffd278,
      durationMs: 920,
      frameKey: "symbol-7-coin",
    };

    if (input.tier === "mega") {
      return [
        {
          kind: "mega",
          title: "MEGA WIN",
          subtitle: headline,
          accent: 0xff5fb2,
          durationMs: 1080,
          frameKey: "coin-multiplier-10x",
        },
        totalStep,
      ];
    }

    if (input.tier === "big" || input.tier === "huge") {
      return [
        {
          kind: "big",
          title: input.tier === "huge" ? "HUGE WIN" : "BIG WIN",
          subtitle: headline,
          accent: input.tier === "huge" ? 0x35f7ff : 0xffd278,
          durationMs: 980,
          frameKey: input.tier === "huge" ? "coin-multiplier-10x" : "coin-multiplier-5x",
        },
        totalStep,
      ];
    }

    return [totalStep];
  }

  private activateStep(index: number): void {
    this.currentIndex = index;
    this.stepElapsedMs = 0;
    const step = this.steps[index];
    this.activeAccent = step.accent;
    this.titleText.text = step.title;
    this.amountText.text = `$${Math.max(0, Math.round(this.sequenceAmountMinor))}`;
    this.subtitleText.text = step.subtitle;
    this.layout();
    void this.refreshEmblem(step.frameKey, step.accent);
  }

  private layout(): void {
    const centerX = this.viewport.width * 0.5;
    const top = this.viewport.safeTop + 128;

    this.backdrop.clear();
    this.backdrop.rect(0, 0, this.viewport.width, this.viewport.height);
    this.backdrop.fill({ color: 0x050102, alpha: this.visible ? 0.36 : 0 });

    this.plate.clear();
    this.plate.roundRect(centerX - 236, top + 12, 472, 190, 42);
    this.plate.fill({ color: 0x190507, alpha: 0.92 });
    this.plate.stroke({ color: 0xffedd2, width: 3, alpha: 0.82 });

    this.accentBar.clear();
    this.accentBar.roundRect(centerX - 182, top + 26, 364, 12, 6);
    this.accentBar.fill({ color: this.activeAccent, alpha: 0.78 });

    this.emblem.x = centerX;
    this.emblem.y = top + 72;
    this.emblem.width = 64;
    this.emblem.height = 64;

    this.titleText.x = centerX;
    this.titleText.y = top + 62;
    this.amountText.x = centerX;
    this.amountText.y = top + 128;
    this.subtitleText.x = centerX;
    this.subtitleText.y = top + 176;
  }

  private async refreshEmblem(frameKey: string, fallbackTint: number): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const resolved = await resolveProviderFrameTexture("symbolAtlas", frameKey);
    if (requestToken !== this.textureRequestToken) {
      return;
    }

    this.emblem.texture = resolved.texture ?? Texture.WHITE;
    this.emblem.tint = resolved.texture ? 0xffffff : fallbackTint;
  }
}
