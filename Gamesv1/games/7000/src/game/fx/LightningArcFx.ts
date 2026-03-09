import { Container, Graphics, Sprite, Texture, Ticker } from "pixi.js";

import { CRAZY_ROOSTER_LAYOUT } from "../config/CrazyRoosterGameConfig";
import { resolveProviderFrameTexture } from "../../app/assets/providerPackRegistry";

export class LightningArcFx extends Container {
  private readonly sprite = new Sprite(Texture.WHITE);
  private readonly fallback = new Graphics();
  private elapsedMs = 0;
  private durationMs = 420;
  private active = false;
  private requestToken = 0;

  constructor() {
    super();

    this.sprite.anchor.set(0.5);
    this.sprite.visible = false;
    this.sprite.alpha = 0;
    this.addChild(this.sprite);

    this.fallback.visible = false;
    this.addChild(this.fallback);

    Ticker.shared.add((ticker) => this.tick(ticker.deltaMS));
    void this.prewarm();
  }

  public async play(width: number, height: number): Promise<void> {
    const requestToken = ++this.requestToken;
    const resolved = await resolveProviderFrameTexture("vfxAtlas", "lightning-arc-01");
    if (requestToken !== this.requestToken) {
      return;
    }

    this.elapsedMs = 0;
    this.active = true;
    this.visible = true;
    this.x = width * 0.5;
    this.y = height * 0.5;

    if (resolved.texture) {
      this.fallback.visible = false;
      this.sprite.visible = true;
      this.sprite.texture = resolved.texture;
      this.sprite.tint = 0xffffff;
      this.sprite.width = width * 0.96;
      this.sprite.height = Math.max(height * 0.9, CRAZY_ROOSTER_LAYOUT.symbolHeight * 4.2);
      this.sprite.alpha = 1;
      this.sprite.scale.set(0.94);
      return;
    }

    this.sprite.visible = false;
    this.fallback.clear();
    this.fallback.moveTo(-width * 0.45, -height * 0.45);
    this.fallback.lineTo(-width * 0.15, -height * 0.12);
    this.fallback.lineTo(0, -height * 0.2);
    this.fallback.lineTo(width * 0.16, height * 0.04);
    this.fallback.lineTo(width * 0.04, height * 0.04);
    this.fallback.lineTo(width * 0.25, height * 0.42);
    this.fallback.stroke({ width: 10, color: 0xfff3a0, alpha: 0.92 });
    this.fallback.visible = true;
    this.fallback.alpha = 1;
  }

  public clear(): void {
    this.active = false;
    this.visible = false;
    this.sprite.visible = false;
    this.fallback.visible = false;
    this.elapsedMs = 0;
  }

  private tick(deltaMs: number): void {
    if (!this.active) {
      return;
    }

    this.elapsedMs += deltaMs;
    const progress = Math.min(1, this.elapsedMs / this.durationMs);
    const alpha = 1 - progress;

    if (this.sprite.visible) {
      this.sprite.alpha = alpha;
      this.sprite.scale.set(0.94 + progress * 0.16);
    }

    if (this.fallback.visible) {
      this.fallback.alpha = alpha;
      this.fallback.scale.set(1 + progress * 0.08);
    }

    if (progress >= 1) {
      this.clear();
    }
  }

  private async prewarm(): Promise<void> {
    await resolveProviderFrameTexture("vfxAtlas", "lightning-arc-01");
  }
}
