import { Container, Graphics, Sprite, Texture, Ticker } from "pixi.js";

import { CRAZY_ROOSTER_LAYOUT } from "../config/CrazyRoosterGameConfig";
import { resolveProviderFrameTexture } from "../../app/assets/providerPackRegistry";

type LightningPlayOptions = {
  sourceX?: number;
  sourceY?: number;
  targetX?: number;
  targetY?: number;
  durationMs?: number;
};

export class LightningArcFx extends Container {
  private readonly glow = new Graphics();
  private readonly sprite = new Sprite(Texture.WHITE);
  private readonly fallback = new Graphics();
  private elapsedMs = 0;
  private durationMs = 420;
  private active = false;
  private requestToken = 0;
  private localStart = { x: 0, y: 0 };
  private localEnd = { x: 0, y: 0 };

  constructor() {
    super();

    this.glow.visible = false;
    this.addChild(this.glow);

    this.sprite.anchor.set(0.5);
    this.sprite.visible = false;
    this.sprite.alpha = 0;
    this.addChild(this.sprite);

    this.fallback.visible = false;
    this.addChild(this.fallback);

    Ticker.shared.add((ticker) => this.tick(ticker.deltaMS));
    void this.prewarm();
  }

  public async play(
    width: number,
    height: number,
    options: LightningPlayOptions = {},
  ): Promise<void> {
    const requestToken = ++this.requestToken;
    const resolved = await resolveProviderFrameTexture("vfxAtlas", "lightning-arc-01");
    if (requestToken !== this.requestToken) {
      return;
    }

    const sourceX = options.sourceX ?? width * 0.18;
    const sourceY = options.sourceY ?? -height * 0.18;
    const targetX = options.targetX ?? width * 0.82;
    const targetY = options.targetY ?? height * 0.18;
    const dx = targetX - sourceX;
    const dy = targetY - sourceY;
    const distance = Math.max(1, Math.hypot(dx, dy));
    const midX = (sourceX + targetX) * 0.5;
    const midY = (sourceY + targetY) * 0.5;
    const angle = Math.atan2(dy, dx);

    this.elapsedMs = 0;
    this.durationMs = Math.max(360, options.durationMs ?? 520);
    this.active = true;
    this.visible = true;
    this.position.set(midX, midY);
    this.rotation = angle;
    this.localStart = { x: -distance * 0.5, y: 0 };
    this.localEnd = { x: distance * 0.5, y: 0 };
    this.glow.clear();
    this.glow.ellipse(0, 0, distance * 0.56, Math.max(48, distance * 0.18));
    this.glow.fill({ color: 0xfff0a8, alpha: 0.38 });
    this.glow.visible = true;
    this.glow.alpha = 1;
    this.glow.scale.set(1);
    this.fallback.clear();
    this.fallback.moveTo(this.localStart.x, this.localStart.y);
    this.fallback.lineTo(this.localStart.x + distance * 0.24, -distance * 0.12);
    this.fallback.lineTo(this.localStart.x + distance * 0.42, distance * 0.06);
    this.fallback.lineTo(0, -distance * 0.08);
    this.fallback.lineTo(this.localEnd.x - distance * 0.2, distance * 0.1);
    this.fallback.lineTo(this.localEnd.x - distance * 0.06, -distance * 0.04);
    this.fallback.lineTo(this.localEnd.x, this.localEnd.y);

    if (resolved.texture) {
      this.fallback.stroke({ width: 8, color: 0xfff8c8, alpha: 0.76 });
      this.fallback.visible = true;
      this.sprite.visible = true;
      this.sprite.texture = resolved.texture;
      this.sprite.tint = 0xffffff;
      this.sprite.width = Math.max(distance * 1.2, CRAZY_ROOSTER_LAYOUT.symbolWidth * 1.95);
      this.sprite.height = Math.max(
        CRAZY_ROOSTER_LAYOUT.symbolHeight * 1.95,
        Math.min(height * 0.64, 124 + distance * 0.2),
      );
      this.sprite.alpha = 1;
      this.sprite.scale.set(1);
      return;
    }

    this.sprite.visible = false;
    this.fallback.stroke({ width: 14, color: 0xfff3a0, alpha: 0.96 });
    this.fallback.visible = true;
    this.fallback.alpha = 1;
  }

  public clear(): void {
    this.active = false;
    this.visible = false;
    this.rotation = 0;
    this.glow.visible = false;
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
      this.sprite.scale.set(1 + progress * 0.18);
    }

    if (this.glow.visible) {
      this.glow.alpha = alpha * 0.96;
      this.glow.scale.set(1 + progress * 0.28);
    }

    if (this.fallback.visible) {
      this.fallback.alpha = alpha;
      this.fallback.scale.set(1 + progress * 0.14);
    }

    if (progress >= 1) {
      this.clear();
    }
  }

  private async prewarm(): Promise<void> {
    await resolveProviderFrameTexture("vfxAtlas", "lightning-arc-01");
  }
}
