import { Container, Graphics, Sprite, Texture, Ticker } from "pixi.js";

import { resolveProviderFrameTexture } from "../../app/assets/providerPackRegistry";
import { LightningArcFx } from "../fx/LightningArcFx";

type FlyingCoin = {
  sprite: Sprite;
  startX: number;
  startY: number;
  controlX: number;
  controlY: number;
  endX: number;
  endY: number;
  lifeMs: number;
  elapsedMs: number;
  spin: number;
};

export class LayeredFxController extends Container {
  private readonly fireBack = new Graphics();
  private readonly stageGlow = new Graphics();
  private readonly topperAura = new Graphics();
  private readonly fireFront = new Graphics();
  private readonly coinLayer = new Container();
  private readonly lightningFx = new LightningArcFx();
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);
  private readonly coinPool: Sprite[] = [];

  private machineWidth = 0;
  private machineHeight = 0;
  private topperAnchor = { x: 0, y: -80 };
  private ambientTime = 0;
  private collectPulse = 0;
  private boostPulse = 0;
  private boostLoop = 0;
  private jackpotPulse = 0;
  private winPulse = 0;
  private coinTexture: Texture | null = null;
  private coinFlights: FlyingCoin[] = [];
  private textureRequestToken = 0;

  constructor() {
    super();
    this.addChild(this.fireBack, this.stageGlow, this.topperAura, this.coinLayer, this.lightningFx, this.fireFront);
    Ticker.shared.add(this.motionTicker);
    void this.refreshTextures();
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public resize(machineWidth: number, machineHeight: number): void {
    this.machineWidth = machineWidth;
    this.machineHeight = machineHeight;
  }

  public setTopperAnchor(x: number, y: number): void {
    this.topperAnchor = { x, y };
  }

  public beginSpinCycle(): void {
    this.collectPulse = 0;
    this.jackpotPulse = 0;
    this.winPulse = 0.08;
    this.boostPulse = 0.22;
    this.boostLoop = 0;
    this.lightningFx.clear();
  }

  public async playBoostStart(): Promise<void> {
    this.boostPulse = 1;
    this.boostLoop = Math.max(this.boostLoop, 0.65);
    if (this.machineWidth > 0 && this.machineHeight > 0) {
      await this.lightningFx.play(this.machineWidth, this.machineHeight);
    }
  }

  public playBoostLoop(): void {
    this.boostLoop = 1;
  }

  public playBoostFinish(): void {
    this.boostLoop = Math.max(0.2, this.boostLoop * 0.4);
    this.boostPulse = Math.max(this.boostPulse, 0.5);
  }

  public playCollectSweep(fromX: number, fromY: number): void {
    this.collectPulse = Math.max(this.collectPulse, 1);
    this.winPulse = Math.max(this.winPulse, 0.28);
    this.spawnCoinFlight(fromX, fromY, this.topperAnchor.x, this.topperAnchor.y - 24, 10);
  }

  public playJackpotHit(level = 1): void {
    this.jackpotPulse = Math.max(this.jackpotPulse, 0.68 + level * 0.1);
    this.boostPulse = Math.max(this.boostPulse, 0.54);
    if (this.machineWidth > 0 && this.machineHeight > 0) {
      void this.lightningFx.play(this.machineWidth, this.machineHeight);
    }
    this.spawnCoinFlight(
      this.machineWidth * 0.5,
      this.machineHeight * 0.28,
      this.topperAnchor.x,
      this.topperAnchor.y - 32,
      12 + level * 2,
    );
  }

  public playWinPulse(intensity: "big" | "huge" | "mega" | "none"): void {
    if (intensity === "none") {
      return;
    }
    this.winPulse = intensity === "mega" ? 1 : intensity === "huge" ? 0.8 : 0.64;
    this.spawnCoinFlight(this.machineWidth * 0.5, this.machineHeight * 0.5, this.topperAnchor.x, this.topperAnchor.y - 18, intensity === "mega" ? 10 : 6);
  }

  public playCoinFly(originX: number, originY: number, count = 6): void {
    this.spawnCoinFlight(originX, originY, this.topperAnchor.x, this.topperAnchor.y - 18, count);
  }

  public clearPresentation(): void {
    this.collectPulse = 0;
    this.boostPulse = 0;
    this.boostLoop = 0;
    this.jackpotPulse = 0;
    this.winPulse = 0;
    this.lightningFx.clear();
    this.coinFlights.forEach((coin) => this.recycleCoin(coin.sprite));
    this.coinFlights = [];
  }

  private tick(deltaMs: number): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }

    this.ambientTime += deltaMs / 1000;
    this.collectPulse = Math.max(0, this.collectPulse - deltaMs / 620);
    this.boostPulse = Math.max(0, this.boostPulse - deltaMs / 720);
    this.boostLoop = Math.max(0, this.boostLoop - deltaMs / 3400);
    this.jackpotPulse = Math.max(0, this.jackpotPulse - deltaMs / 960);
    this.winPulse = Math.max(0, this.winPulse - deltaMs / 1100);

    this.redrawFireBack();
    this.redrawStageGlow();
    this.redrawTopperAura();
    this.redrawFireFront();
    this.tickCoinFlights(deltaMs);
  }

  private redrawFireBack(): void {
    const intensity = 0.28 + this.boostLoop * 0.45 + this.jackpotPulse * 0.2;
    const baseY = this.machineHeight - 10;
    this.fireBack.clear();

    for (let index = 0; index < 6; index += 1) {
      const phase = this.ambientTime * 2.2 + index * 0.8;
      const x = this.machineWidth * (0.12 + index * 0.16);
      const width = 44 + Math.sin(phase) * 10 + intensity * 18;
      const height = 36 + Math.cos(phase * 1.4) * 8 + intensity * 20;
      this.fireBack.ellipse(x, baseY - 8 - Math.sin(phase) * 6, width, height);
      this.fireBack.fill({ color: 0xa90f14, alpha: 0.12 + intensity * 0.18 });
      this.fireBack.ellipse(x, baseY - 10 - Math.sin(phase + 0.4) * 8, width * 0.58, height * 0.62);
      this.fireBack.fill({ color: 0xff8b2d, alpha: 0.08 + intensity * 0.14 });
    }
  }

  private redrawStageGlow(): void {
    const intensity =
      0.2 +
      this.collectPulse * 0.08 +
      this.boostPulse * 0.28 +
      this.jackpotPulse * 0.12 +
      this.winPulse * 0.22;
    this.stageGlow.clear();
    this.stageGlow.roundRect(-72, -88, this.machineWidth + 144, this.machineHeight + 136, 64);
    this.stageGlow.fill({ color: 0x6a0811, alpha: 0.08 + intensity });
    this.stageGlow.stroke({ color: 0xffd48a, width: 3, alpha: 0.12 + intensity * 0.55 });
  }

  private redrawTopperAura(): void {
    const intensity =
      0.14 +
      this.collectPulse * 0.18 +
      this.boostLoop * 0.28 +
      this.jackpotPulse * 0.34 +
      this.winPulse * 0.18;
    const pulse = Math.sin(this.ambientTime * 2.8) * 8;
    this.topperAura.clear();
    this.topperAura.ellipse(
      this.topperAnchor.x,
      this.topperAnchor.y - 24,
      92 + intensity * 56 + pulse,
      34 + intensity * 18 + pulse * 0.2,
    );
    this.topperAura.fill({ color: 0xffcf6f, alpha: 0.08 + intensity * 0.32 });
    this.topperAura.ellipse(
      this.topperAnchor.x,
      this.topperAnchor.y - 38,
      62 + intensity * 28,
      18 + intensity * 8,
    );
    this.topperAura.fill({ color: 0xfff0bf, alpha: 0.04 + intensity * 0.14 });
    this.topperAura.circle(this.topperAnchor.x, this.topperAnchor.y - 86, 74 + intensity * 26);
    this.topperAura.stroke({ color: 0xc7141a, width: 2 + intensity * 5, alpha: 0.16 + intensity * 0.5 });
  }

  private redrawFireFront(): void {
    const intensity = 0.16 + this.boostPulse * 0.18 + this.jackpotPulse * 0.12 + this.winPulse * 0.14;
    const topY = this.machineHeight * 0.08;
    this.fireFront.clear();

    for (let index = 0; index < 4; index += 1) {
      const phase = this.ambientTime * 3 + index * 1.2;
      const x = this.machineWidth * (0.22 + index * 0.18);
      this.fireFront.ellipse(x, topY + Math.sin(phase) * 6, 30 + intensity * 14, 12 + intensity * 9);
      this.fireFront.fill({ color: 0xffd877, alpha: 0.06 + intensity * 0.14 });
    }
  }

  private tickCoinFlights(deltaMs: number): void {
    if (this.coinFlights.length === 0) {
      return;
    }

    const survivors: FlyingCoin[] = [];
    for (const coin of this.coinFlights) {
      coin.elapsedMs += deltaMs;
      const progress = Math.min(1, coin.elapsedMs / coin.lifeMs);
      const inv = 1 - progress;
      const x =
        inv * inv * coin.startX +
        2 * inv * progress * coin.controlX +
        progress * progress * coin.endX;
      const y =
        inv * inv * coin.startY +
        2 * inv * progress * coin.controlY +
        progress * progress * coin.endY;

      coin.sprite.x = x;
      coin.sprite.y = y;
      coin.sprite.rotation += coin.spin * (deltaMs / 1000);
      coin.sprite.alpha = 1 - progress * 0.18;
      coin.sprite.scale.set(0.52 + inv * 0.18);

      if (progress >= 1) {
        this.recycleCoin(coin.sprite);
        continue;
      }

      survivors.push(coin);
    }

    this.coinFlights = survivors;
  }

  private spawnCoinFlight(
    fromX: number,
    fromY: number,
    toX: number,
    toY: number,
    count: number,
  ): void {
    for (let index = 0; index < count; index += 1) {
      const sprite = this.obtainCoinSprite();
      const startX = fromX + (Math.random() - 0.5) * 54;
      const startY = fromY + (Math.random() - 0.5) * 32;
      const endX = toX + (Math.random() - 0.5) * 42;
      const endY = toY + (Math.random() - 0.5) * 18;
      const controlX = (startX + endX) * 0.5 + (Math.random() - 0.5) * 96;
      const controlY = Math.min(startY, endY) - 80 - Math.random() * 70;

      sprite.x = startX;
      sprite.y = startY;
      sprite.alpha = 1;
      sprite.rotation = Math.random() * Math.PI * 2;
      sprite.scale.set(0.62 + Math.random() * 0.18);
      this.coinLayer.addChild(sprite);

      this.coinFlights.push({
        sprite,
        startX,
        startY,
        controlX,
        controlY,
        endX,
        endY,
        lifeMs: 560 + Math.random() * 180,
        elapsedMs: 0,
        spin: (Math.random() - 0.5) * 7,
      });
    }
  }

  private obtainCoinSprite(): Sprite {
    const sprite = this.coinPool.pop() ?? new Sprite(Texture.WHITE);
    sprite.anchor.set(0.5);
    sprite.texture = this.coinTexture ?? Texture.WHITE;
    sprite.tint = this.coinTexture ? 0xffffff : 0xffce54;
    sprite.width = 34;
    sprite.height = 34;
    return sprite;
  }

  private recycleCoin(sprite: Sprite): void {
    if (sprite.parent === this.coinLayer) {
      this.coinLayer.removeChild(sprite);
    }
    this.coinPool.push(sprite);
  }

  private async refreshTextures(): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const coin = await resolveProviderFrameTexture("symbolAtlas", "coin-multiplier-2x");
    if (requestToken !== this.textureRequestToken) {
      return;
    }
    this.coinTexture = coin.texture;
  }
}
