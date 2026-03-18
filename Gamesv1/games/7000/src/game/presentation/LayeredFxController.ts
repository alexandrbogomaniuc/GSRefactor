import { Container, Graphics, Point, Sprite, Texture } from "pixi.js";

import {
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../app/assets/providerPackRegistry";
import { resolveMappedSourceTexture } from "../../app/assets/mappedSourceTextureResolver";
import { LightningArcFx } from "../fx/LightningArcFx";

type FlyingCoin = {
  sprite: Sprite;
  glow: Sprite | null;
  startX: number;
  startY: number;
  controlX: number;
  controlY: number;
  endX: number;
  endY: number;
  lifeMs: number;
  elapsedMs: number;
  spin: number;
  frames: Texture[];
  frameOffset: number;
};

type FxPoint = {
  x: number;
  y: number;
};

type JackpotTier = "mini" | "minor" | "major" | "grand";

type CoinBurstOptions = {
  targetX?: number;
  targetY?: number;
  durationMs?: number;
  countPerOrigin?: number;
  waveCount?: number;
  waveGapMs?: number;
  startSpreadX?: number;
  startSpreadY?: number;
  endSpreadX?: number;
  endSpreadY?: number;
  controlLift?: number;
};

export class LayeredFxController extends Container {
  private readonly fireBack = new Graphics();
  private readonly stageGlow = new Graphics();
  private readonly topperAura = new Graphics();
  private readonly jackpotOverlayHost = new Container();
  private readonly jackpotTargetHalo = new Graphics();
  private readonly boostEdgeAura = new Graphics();
  private readonly boostSeamHost = new Container();
  private readonly boostSeamOverlay = new Graphics();
  private boostSeamSprite = new Sprite(Texture.EMPTY);
  private boostSeamSparkSprite = new Sprite(Texture.EMPTY);
  private readonly collectRingSprite = new Sprite(Texture.EMPTY);
  private readonly boostBurstSprite = new Sprite(Texture.EMPTY);
  private readonly jackpotBurstSprite = new Sprite(Texture.EMPTY);
  private readonly fireFront = new Graphics();
  private readonly coinLayer = new Container();
  private readonly lightningFxPool = [
    new LightningArcFx(),
    new LightningArcFx(),
    new LightningArcFx(),
    new LightningArcFx(),
    new LightningArcFx(),
    new LightningArcFx(),
  ];
  private readonly coinPool: Sprite[] = [];
  private readonly coinGlowPool: Sprite[] = [];
  private seamOverlayLayer: Container | null = null;

  private machineWidth = 0;
  private machineHeight = 0;
  private topperAnchor = { x: 0, y: -80 };
  private collectAnchor = { x: 0, y: -80 };
  private boostAnchor = { x: 0, y: -80 };
  private jackpotAnchor = { x: 0, y: -80 };
  private jackpotBurstAnchor = { x: 0, y: -80 };
  private fireAnchor = { x: 0, y: -80 };
  private boostEdgeTarget: FxPoint | null = null;
  private boostEdgeSide: -1 | 1 = 1;
  private ambientTime = 0;
  private collectPulse = 0;
  private boostPulse = 0;
  private boostLoop = 0;
  private boostSeamHoldMs = 0;
  private jackpotPulse = 0;
  private jackpotBurstHoldMs = 0;
  private jackpotTier: JackpotTier | null = null;
  private winPulse = 0;
  private coinTexture: Texture | null = null;
  private defaultJackpotBurstTexture: Texture = Texture.EMPTY;
  private donorCoinTextures: Texture[] = [];
  private donorCoinGlowTexture: Texture | null = null;
  private donorBoostSeamTextures: Texture[] = [];
  private donorBoostSparkTextures: Texture[] = [];
  private donorJackpotTextures: Partial<Record<JackpotTier, Texture>> = {};
  private coinFlights: FlyingCoin[] = [];
  private textureRequestToken = 0;
  private lightningCursor = 0;
  private scheduledEffectTimeouts: number[] = [];

  constructor() {
    super();
    this.collectRingSprite.anchor.set(0.5);
    this.collectRingSprite.visible = false;
    this.collectRingSprite.blendMode = "add";
    this.boostBurstSprite.anchor.set(0.5);
    this.boostBurstSprite.visible = false;
    this.boostBurstSprite.blendMode = "add";
    this.jackpotBurstSprite.anchor.set(0.5);
    this.jackpotBurstSprite.visible = false;
    this.jackpotBurstSprite.blendMode = "add";
    this.jackpotOverlayHost.eventMode = "none";
    this.jackpotOverlayHost.renderable = true;
    this.jackpotOverlayHost.visible = true;
    this.jackpotTargetHalo.blendMode = "add";
    this.boostEdgeAura.blendMode = "add";
    this.boostSeamHost.eventMode = "none";
    this.boostSeamHost.renderable = true;
    this.boostSeamHost.visible = false;
    this.boostSeamOverlay.blendMode = "add";
    this.configureBoostSeamSprite(this.boostSeamSprite);
    this.configureBoostSeamSprite(this.boostSeamSparkSprite);
    this.boostSeamHost.addChild(
      this.boostSeamOverlay,
      this.boostSeamSprite,
      this.boostSeamSparkSprite,
    );
    this.jackpotOverlayHost.addChild(this.jackpotTargetHalo, this.jackpotBurstSprite);
    this.addChild(
      this.fireBack,
      this.stageGlow,
      this.topperAura,
      this.jackpotOverlayHost,
      this.boostEdgeAura,
      this.boostSeamHost,
      this.collectRingSprite,
      this.boostBurstSprite,
      this.coinLayer,
      ...this.lightningFxPool,
      this.fireFront,
    );
    void this.refreshTextures();
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    this.clearScheduledEffects();
    this.jackpotOverlayHost.parent?.removeChild(this.jackpotOverlayHost);
    this.boostSeamHost.parent?.removeChild(this.boostSeamHost);
    this.jackpotOverlayHost.destroy();
    this.boostSeamSprite.destroy();
    this.boostSeamSparkSprite.destroy();
    this.boostSeamOverlay.destroy();
    this.boostSeamHost.destroy();
    super.destroy(options);
  }

  public setOverlayLayer(layer: Container | null): void {
    if (this.seamOverlayLayer === layer) {
      return;
    }
    this.seamOverlayLayer = layer;
    this.reparentSeamHost();
    this.syncOverlayHostTransforms();
  }

  public resize(machineWidth: number, machineHeight: number): void {
    this.machineWidth = machineWidth;
    this.machineHeight = machineHeight;
  }

  public setTopperAnchor(x: number, y: number): void {
    this.topperAnchor = { x, y };
    this.collectAnchor = { x, y: y - 24 };
    this.boostAnchor = { x, y: y - 40 };
    this.jackpotAnchor = { x, y: y - 54 };
    this.jackpotBurstAnchor = { x, y: y - 10 };
    this.fireAnchor = { x, y: y - 32 };
    this.boostEdgeTarget = null;
    this.boostEdgeSide = 1;
  }

  public beginSpinCycle(): void {
    this.clearScheduledEffects();
    this.collectPulse = 0;
    this.jackpotPulse = 0;
    this.winPulse = 0.08;
    this.boostPulse = 0.22;
    this.boostLoop = 0;
    this.boostSeamHoldMs = 0;
    this.collectAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 24 };
    this.boostAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 40 };
    this.jackpotAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 54 };
    this.jackpotBurstAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 10 };
    this.fireAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 32 };
    this.boostEdgeTarget = null;
    this.boostEdgeSide = 1;
    this.jackpotBurstHoldMs = 0;
    this.jackpotTier = null;
    this.jackpotBurstSprite.texture = this.defaultJackpotBurstTexture;
    this.jackpotBurstSprite.blendMode = "add";
    this.lightningFxPool.forEach((fx) => fx.clear());
    this.clearBoostSeamCue();
  }

  private isDonorlocalProvider(): boolean {
    return getProviderPackStatus().effectiveProvider === "donorlocal";
  }

  public async playBoostStart(): Promise<void> {
    this.boostPulse = 1;
    this.boostLoop = Math.max(this.boostLoop, 0.65);
    if (this.machineWidth > 0 && this.machineHeight > 0) {
      const centerOrigin = { x: this.machineWidth * 0.5, y: this.machineHeight * 0.46 };
      this.playLightningBurst([centerOrigin], {
        targetX: this.topperAnchor.x,
        targetY: this.topperAnchor.y - 42,
        durationMs: 620,
      });
    }
  }

  public playBoostLoop(): void {
    this.boostLoop = 1;
    this.boostSeamHoldMs = Math.max(
      this.boostSeamHoldMs,
      this.isDonorlocalProvider() ? 3200 : 1400,
    );
  }

  public playBoostFinish(): void {
    this.boostLoop = Math.max(0.2, this.boostLoop * 0.4);
    this.boostPulse = Math.max(this.boostPulse, 0.5);
    this.boostSeamHoldMs = Math.max(
      this.boostSeamHoldMs,
      this.isDonorlocalProvider() ? 2400 : 900,
    );
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
      const centerOrigin = { x: this.machineWidth * 0.5, y: this.machineHeight * 0.3 };
      this.playLightningBurst([centerOrigin], {
        targetX: this.topperAnchor.x,
        targetY: this.topperAnchor.y - 52,
        durationMs: 500,
      });
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

  public playCollectSweepBurst(origins: FxPoint[], durationMs = 880): void {
    const isDonorlocal = this.isDonorlocalProvider();
    const resolvedOrigins = this.resolveOrigins(origins, {
      x: this.machineWidth * 0.5,
      y: this.machineHeight * 0.44,
    });
    this.collectAnchor = this.resolveEffectAnchor(
      resolvedOrigins,
      this.topperAnchor.x,
      this.topperAnchor.y - 24,
      0.18,
    );
    this.collectPulse = Math.max(this.collectPulse, 1);
    this.winPulse = Math.max(this.winPulse, 0.34);
    this.playCoinFlyBurst(resolvedOrigins, {
      targetX: this.topperAnchor.x,
      targetY: this.topperAnchor.y - 24,
      durationMs,
      countPerOrigin: isDonorlocal ? (resolvedOrigins.length > 2 ? 3 : 4) : resolvedOrigins.length > 2 ? 2 : 3,
      waveCount: isDonorlocal ? 4 : 3,
      waveGapMs: isDonorlocal ? 150 : 180,
      startSpreadX: 18,
      startSpreadY: 12,
      endSpreadX: 22,
      endSpreadY: 12,
      controlLift: isDonorlocal ? 138 : 124,
    });
  }

  public playBoostStrike(
    origins: FxPoint[],
    options: {
      targetX?: number;
      targetY?: number;
      durationMs?: number;
      spawnCoinBurst?: boolean;
    } = {},
  ): void {
    const isDonorlocal = this.isDonorlocalProvider();
    const resolvedOrigins = this.resolveOrigins(origins, {
      x: this.machineWidth * 0.5,
      y: this.machineHeight * 0.38,
    });
    this.boostAnchor = this.resolveEffectAnchor(
      resolvedOrigins,
      options.targetX ?? this.topperAnchor.x,
      options.targetY ?? this.topperAnchor.y - 40,
      0.58,
    );
    const edgeReferenceX =
      options.targetX ??
      resolvedOrigins.reduce((sum, origin) => sum + origin.x, 0) / resolvedOrigins.length;
    this.boostEdgeSide = edgeReferenceX >= this.machineWidth * 0.5 ? 1 : -1;
    const boostEdgeX = this.resolveBoostEdgeX();
    this.boostEdgeTarget = {
      x: boostEdgeX,
      y: Math.max(26, Math.min(this.machineHeight - 26, this.boostAnchor.y)),
    };
    this.fireAnchor = {
      x: this.boostAnchor.x * 0.56 + boostEdgeX * 0.44,
      y: this.boostAnchor.y + 12,
    };
    this.boostPulse = Math.max(this.boostPulse, 1);
    this.boostLoop = Math.max(this.boostLoop, 0.85);
    this.boostSeamHoldMs = Math.max(
      this.boostSeamHoldMs,
      isDonorlocal ? 4600 : 1800,
    );
    this.presentBoostSeamCue();
    const lightningOrigins = isDonorlocal
      ? [
          {
            x: this.machineWidth * (this.boostEdgeSide > 0 ? 0.62 : 0.38),
            y: 26,
          },
          {
            x: this.machineWidth * (this.boostEdgeSide > 0 ? 0.68 : 0.32),
            y: 44,
          },
        ]
      : resolvedOrigins;
    this.playLightningBurst(lightningOrigins, {
      targetX: options.targetX ?? this.topperAnchor.x,
      targetY: options.targetY ?? this.topperAnchor.y - 40,
      durationMs: options.durationMs
        ? Math.min(820, Math.max(560, options.durationMs))
        : 640,
      staggerMs: isDonorlocal ? 74 : 96,
      repeatCount: isDonorlocal ? 2 : 2,
      repeatGapMs: isDonorlocal ? 92 : 120,
    });
    if (options.spawnCoinBurst ?? true) {
      this.playCoinFlyBurst(resolvedOrigins, {
        targetX: options.targetX ?? this.topperAnchor.x,
        targetY: (options.targetY ?? this.topperAnchor.y - 24) + 12,
        durationMs: options.durationMs ?? 820,
        countPerOrigin: isDonorlocal ? 3 : 2,
        waveCount: isDonorlocal ? 3 : 2,
        waveGapMs: isDonorlocal ? 144 : 160,
        startSpreadX: 14,
        startSpreadY: 10,
        endSpreadX: 18,
        endSpreadY: 12,
        controlLift: isDonorlocal ? 104 : 92,
      });
    }
  }

  public playJackpotStrike(
    level: number,
    origins: FxPoint[],
    target: FxPoint | null,
    durationMs = 980,
    tier: string | null = null,
  ): void {
    const isDonorlocal = this.isDonorlocalProvider();
    const resolvedTarget = target ?? {
      x: this.topperAnchor.x,
      y: this.topperAnchor.y - 54,
    };
    const resolvedOrigins = this.resolveOrigins(origins, {
      x: this.machineWidth * 0.5,
      y: this.machineHeight * 0.3,
    });
    const plaqueSide = resolvedTarget.x >= this.machineWidth * 0.5 ? 1 : -1;
    const plaqueEdgeX = this.machineWidth * (plaqueSide > 0 ? 0.78 : 0.22);
    const boardSideOrigin = resolvedOrigins.reduce(
      (best, origin) =>
        plaqueSide > 0 ? (origin.x > best.x ? origin : best) : origin.x < best.x ? origin : best,
      resolvedOrigins[0] ?? {
        x: this.machineWidth * (plaqueSide > 0 ? 0.74 : 0.26),
        y: this.machineHeight * 0.42,
      },
    );
    this.jackpotAnchor = {
      x: resolvedTarget.x,
      y: resolvedTarget.y,
    };
    this.jackpotBurstAnchor = isDonorlocal
      ? {
          x:
            boardSideOrigin.x * 0.86 +
            this.machineWidth * (plaqueSide > 0 ? 0.74 : 0.26) * 0.14,
          y: Math.max(
            this.machineHeight * 0.24,
            Math.min(this.machineHeight * 0.72, boardSideOrigin.y),
          ),
        }
      : { x: resolvedTarget.x, y: resolvedTarget.y };
    this.fireAnchor = isDonorlocal
      ? {
          x: resolvedTarget.x * 0.74 + plaqueEdgeX * 0.26,
          y: Math.max(52, this.machineHeight * 0.18),
        }
      : {
          x: resolvedTarget.x,
          y: resolvedTarget.y + 20,
      };
    this.jackpotTier = this.resolveJackpotTier(tier);
    const donorJackpotTexture =
      isDonorlocal && this.jackpotTier
        ? this.donorJackpotTextures[this.jackpotTier] ?? null
        : null;
    this.jackpotBurstSprite.texture = donorJackpotTexture ?? this.defaultJackpotBurstTexture;
    this.jackpotBurstSprite.blendMode = donorJackpotTexture ? "normal" : "add";
    this.jackpotPulse = Math.max(
      this.jackpotPulse,
      isDonorlocal ? 0.9 + level * 0.16 : 0.74 + level * 0.12,
    );
    this.jackpotBurstHoldMs = Math.max(
      this.jackpotBurstHoldMs,
      isDonorlocal ? 2200 + level * 260 : 900 + level * 140,
    );
    if (isDonorlocal) {
      this.boostPulse = Math.max(this.boostPulse * 0.18, 0.08 + level * 0.02);
      this.boostLoop = Math.max(this.boostLoop * 0.12, 0.04 + level * 0.02);
      this.boostSeamHoldMs = 0;
      this.boostEdgeTarget = null;
      this.clearBoostSeamCue();
    } else {
      this.boostPulse = Math.max(this.boostPulse, 0.62);
      this.boostLoop = Math.max(this.boostLoop, 0.24);
    }
    const lightningOrigins = isDonorlocal
      ? [
          {
            x: this.machineWidth * (plaqueSide > 0 ? 0.78 : 0.22),
            y: this.machineHeight * 0.28,
          },
          {
            x: this.machineWidth * (plaqueSide > 0 ? 0.74 : 0.26),
            y: this.machineHeight * 0.5,
          },
        ]
      : resolvedOrigins;
    this.playLightningBurst(lightningOrigins, {
      targetX: resolvedTarget.x,
      targetY: resolvedTarget.y,
      durationMs: Math.min(980, 620 + level * 70),
      staggerMs: isDonorlocal ? 80 : 92,
      repeatCount: isDonorlocal ? 2 + Math.min(1, level) : 2 + Math.min(1, level),
      repeatGapMs: isDonorlocal ? 124 : 126,
    });
    this.playCoinFlyBurst(resolvedOrigins, {
      targetX: resolvedTarget.x,
      targetY: resolvedTarget.y + 8,
      durationMs,
      countPerOrigin: isDonorlocal ? 1 : 2,
      waveCount: isDonorlocal ? 1 : 2,
      waveGapMs: isDonorlocal ? 0 : 170,
      startSpreadX: isDonorlocal ? 10 : 16,
      startSpreadY: isDonorlocal ? 8 : 10,
      endSpreadX: isDonorlocal ? 8 : 12,
      endSpreadY: isDonorlocal ? 6 : 8,
      controlLift: isDonorlocal ? 96 : 96,
    });
    if (!isDonorlocal) {
      this.playCoinFlyBurst([resolvedTarget], {
        targetX: this.topperAnchor.x,
        targetY: this.topperAnchor.y - 24,
        durationMs: durationMs + 120,
        countPerOrigin: 3 + Math.min(2, level),
        waveCount: 2,
        waveGapMs: 140,
        startSpreadX: 20,
        startSpreadY: 10,
        endSpreadX: 20,
        endSpreadY: 10,
        controlLift: 84,
      });
    }
  }

  public playCoinFlyBurst(origins: FxPoint[], options: CoinBurstOptions = {}): void {
    const resolvedOrigins = this.resolveOrigins(origins, {
      x: this.machineWidth * 0.5,
      y: this.machineHeight * 0.4,
    });
    const targetX = options.targetX ?? this.topperAnchor.x;
    const targetY = options.targetY ?? this.topperAnchor.y - 18;
    const countPerOrigin = Math.max(1, options.countPerOrigin ?? 2);
    const waveCount = Math.max(1, options.waveCount ?? 1);
    const waveGapMs = Math.max(0, options.waveGapMs ?? 0);

    const spawnWave = (waveIndex: number) => {
      for (const origin of resolvedOrigins) {
        this.spawnCoinFlight(origin.x, origin.y, targetX, targetY, countPerOrigin, {
          ...options,
          durationMs:
            options.durationMs === undefined
              ? undefined
              : Math.max(420, options.durationMs - waveIndex * 70),
        });
      }
    };

    spawnWave(0);
    for (let waveIndex = 1; waveIndex < waveCount; waveIndex += 1) {
      const timeout = window.setTimeout(() => {
        spawnWave(waveIndex);
      }, waveGapMs * waveIndex);
      this.scheduledEffectTimeouts.push(timeout);
    }
  }

  public clearPresentation(): void {
    this.clearScheduledEffects();
    this.collectPulse = 0;
    this.boostPulse = 0;
    this.boostLoop = 0;
    this.boostSeamHoldMs = 0;
    this.jackpotPulse = 0;
    this.jackpotBurstHoldMs = 0;
    this.winPulse = 0;
    this.collectAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 24 };
    this.boostAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 40 };
    this.jackpotAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 54 };
    this.jackpotBurstAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 10 };
    this.fireAnchor = { x: this.topperAnchor.x, y: this.topperAnchor.y - 32 };
    this.boostEdgeTarget = null;
    this.boostEdgeSide = 1;
    this.jackpotTier = null;
    this.jackpotBurstSprite.texture = this.defaultJackpotBurstTexture;
    this.jackpotBurstSprite.blendMode = "add";
    this.lightningFxPool.forEach((fx) => fx.clear());
    this.coinFlights.forEach((coin) => this.recycleCoin(coin.sprite, coin.glow));
    this.coinFlights = [];
    this.clearBoostSeamCue();
  }

  public update(deltaMs: number): void {
    this.tick(deltaMs);
  }

  private tick(deltaMs: number): void {
    if (this.machineWidth <= 0 || this.machineHeight <= 0) {
      return;
    }

    const stepMs = Math.min(deltaMs, 96);
    const isDonorlocal = this.isDonorlocalProvider();
    this.ambientTime += stepMs / 1000;
    this.collectPulse = Math.max(0, this.collectPulse - stepMs / (isDonorlocal ? 1080 : 940));
    this.boostPulse = Math.max(0, this.boostPulse - stepMs / (isDonorlocal ? 1260 : 1040));
    this.boostLoop = Math.max(0, this.boostLoop - stepMs / (isDonorlocal ? 4300 : 3400));
    this.boostSeamHoldMs = Math.max(
      0,
      this.boostSeamHoldMs - stepMs * (isDonorlocal ? 0.48 : 1),
    );
    this.jackpotPulse = Math.max(0, this.jackpotPulse - stepMs / (isDonorlocal ? 2760 : 1360));
    this.jackpotBurstHoldMs = Math.max(
      0,
      this.jackpotBurstHoldMs - stepMs * (isDonorlocal ? 0.78 : 1),
    );
    this.winPulse = Math.max(0, this.winPulse - stepMs / 1100);
    this.syncOverlayHostTransforms();

    this.redrawFireBack();
    this.redrawStageGlow();
    this.redrawTopperAura();
    this.redrawFeatureBursts();
    this.redrawFireFront();
    this.tickCoinFlights(stepMs);
  }

  private redrawFireBack(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const donorJackpotOnly =
      isDonorlocal &&
      this.jackpotPulse > 0.04 &&
      this.boostLoop < 0.08 &&
      this.boostPulse < 0.08;
    const intensity = isDonorlocal
      ? this.collectPulse * 0.12 +
        this.boostLoop * 0.45 +
        this.jackpotPulse * (donorJackpotOnly ? 0.1 : 0.2) +
        this.winPulse * 0.1
      : 0.28 + this.boostLoop * 0.45 + this.jackpotPulse * 0.2;
    const baseY = this.machineHeight - 10;
    this.fireBack.clear();
    if (isDonorlocal && intensity < 0.03) {
      return;
    }

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

    const featureFire = this.boostLoop * 0.72 + this.jackpotPulse * (donorJackpotOnly ? 0.42 : 0.9);
    if (featureFire > 0.04) {
      if (isDonorlocal) {
        const inset = 8;
        const boostFocusedEdge = this.boostEdgeTarget && this.boostLoop > 0.08;
        const plaqueSide = this.jackpotAnchor.x >= this.machineWidth * 0.5 ? 1 : -1;
        this.fireBack.roundRect(
          inset,
          inset,
          this.machineWidth - inset * 2,
          this.machineHeight - inset * 2,
          24,
        );
        this.fireBack.stroke({
          color: 0xff9a2f,
          width: 8 + featureFire * 12,
          alpha: donorJackpotOnly ? 0.02 + featureFire * 0.05 : 0.08 + featureFire * 0.12,
        });
        const activeEdgeX = this.resolveBoostEdgeX();
        const activeEdgeTargetY = this.boostEdgeTarget?.y ?? this.machineHeight * 0.5;
        const edgePoints = boostFocusedEdge
          ? [
              { x: this.machineWidth * 0.5, y: 18, width: 58, height: 20 },
              {
                x: this.machineWidth * (this.boostEdgeSide > 0 ? 0.74 : 0.26),
                y: 20,
                width: 52,
                height: 18,
              },
              {
                x: activeEdgeX - this.boostEdgeSide * 14,
                y: Math.max(34, activeEdgeTargetY * 0.32),
                width: 18,
                height: 58,
              },
              {
                x: activeEdgeX - this.boostEdgeSide * 12,
                y: activeEdgeTargetY,
                width: 20,
                height: 66,
              },
            ]
          : donorJackpotOnly
            ? [
                {
                  x: this.machineWidth * (plaqueSide > 0 ? 0.72 : 0.28),
                  y: 20,
                  width: 36,
                  height: 14,
                },
                {
                  x: this.fireAnchor.x + plaqueSide * -10,
                  y: Math.max(32, this.fireAnchor.y - 8),
                  width: 18,
                  height: 44,
                },
                {
                  x: this.jackpotBurstAnchor.x + plaqueSide * -8,
                  y: this.jackpotBurstAnchor.y + 10,
                  width: 22,
                  height: 52,
                },
              ]
          : [
              { x: this.machineWidth * 0.18, y: 20, width: 44, height: 18 },
              { x: this.machineWidth * 0.5, y: 18, width: 54, height: 20 },
              { x: this.machineWidth * 0.82, y: 20, width: 44, height: 18 },
            ];
        edgePoints.forEach((edge, index) => {
          const phase = this.ambientTime * 4 + index * 0.7;
          this.fireBack.ellipse(
            edge.x,
            edge.y,
            edge.width + Math.sin(phase) * 4 + featureFire * (donorJackpotOnly ? 8 : 18),
            edge.height + Math.cos(phase) * 3 + featureFire * (donorJackpotOnly ? 8 : 14),
          );
          this.fireBack.fill({
            color: 0xff8c28,
            alpha: donorJackpotOnly ? 0.03 + featureFire * 0.07 : 0.06 + featureFire * 0.12,
          });
        });
        if (this.boostEdgeTarget && this.boostLoop > 0.08) {
          const edgeX = this.resolveBoostEdgeX();
          const targetY = this.boostEdgeTarget.y;
          const cornerX = edgeX + (this.boostEdgeSide > 0 ? -22 : 22);
          const ridgeWidth = 20 + featureFire * 18;
          const ridgeHeight = 56 + featureFire * 44;
          this.fireBack.ellipse(edgeX, targetY - 10, ridgeWidth, ridgeHeight);
          this.fireBack.fill({
            color: 0xff9128,
            alpha: 0.1 + featureFire * 0.18,
          });
          this.fireBack.ellipse(
            edgeX - this.boostEdgeSide * 14,
            targetY - 8,
            12 + featureFire * 10,
            82 + featureFire * 54,
          );
          this.fireBack.fill({
            color: 0xffa53d,
            alpha: 0.12 + featureFire * 0.2,
          });
          this.fireBack.ellipse(
            edgeX - this.boostEdgeSide * 18,
            Math.max(30, targetY * 0.42),
            18 + featureFire * 12,
            54 + featureFire * 34,
          );
          this.fireBack.fill({
            color: 0xffbf5f,
            alpha: 0.08 + featureFire * 0.16,
          });
          this.fireBack.ellipse(cornerX, 22, 34 + featureFire * 22, 18 + featureFire * 10);
          this.fireBack.fill({
            color: 0xffa33a,
            alpha: 0.08 + featureFire * 0.14,
          });
        }
      }
      const anchorX = this.fireAnchor.x;
      const anchorY = this.fireAnchor.y;
      for (let index = 0; index < 3; index += 1) {
        const phase = this.ambientTime * 3.2 + index * 0.7;
        const x = anchorX + (index - 1) * (28 + featureFire * 10);
        const y = anchorY + Math.sin(phase) * 8;
        const width =
          36 + featureFire * (donorJackpotOnly ? 16 : 34) - index * (donorJackpotOnly ? 6 : 4);
        const height =
          44 +
          featureFire * (donorJackpotOnly ? 20 : 42) +
          Math.cos(phase * 1.2) * (donorJackpotOnly ? 4 : 6);
        this.fireBack.ellipse(x, y, width, height);
        this.fireBack.fill({
          color: 0xa10c10,
          alpha: donorJackpotOnly ? 0.06 + featureFire * 0.08 : 0.14 + featureFire * 0.16,
        });
        this.fireBack.ellipse(x, y - 4, width * 0.54, height * 0.56);
        this.fireBack.fill({
          color: 0xffa233,
          alpha: donorJackpotOnly ? 0.05 + featureFire * 0.08 : 0.1 + featureFire * 0.14,
        });
      }
    }
  }

  private redrawStageGlow(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const intensity = isDonorlocal
      ? this.collectPulse * 0.08 + this.boostPulse * 0.18 + this.jackpotPulse * 0.12 + this.winPulse * 0.2
      : 0.2 +
        this.collectPulse * 0.08 +
        this.boostPulse * 0.28 +
        this.jackpotPulse * 0.12 +
        this.winPulse * 0.22;
    this.stageGlow.clear();
    if (isDonorlocal && intensity < 0.03) {
      return;
    }
    this.stageGlow.roundRect(-72, -88, this.machineWidth + 144, this.machineHeight + 136, 64);
    this.stageGlow.fill({
      color: isDonorlocal ? 0x4e0814 : 0x6a0811,
      alpha: isDonorlocal ? intensity * 0.42 : 0.08 + intensity,
    });
    this.stageGlow.stroke({
      color: 0xffd48a,
      width: 3,
      alpha: isDonorlocal ? 0.04 + intensity * 0.34 : 0.12 + intensity * 0.55,
    });
  }

  private redrawTopperAura(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const intensity = isDonorlocal
      ? this.collectPulse * 0.16 +
        this.boostLoop * 0.24 +
        this.jackpotPulse * 0.28 +
        this.winPulse * 0.14
      : 0.14 +
        this.collectPulse * 0.18 +
        this.boostLoop * 0.28 +
        this.jackpotPulse * 0.34 +
        this.winPulse * 0.18;
    const pulse = Math.sin(this.ambientTime * 2.8) * 8;
    this.topperAura.clear();
    if (isDonorlocal && intensity < 0.03) {
      return;
    }
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
    this.topperAura.stroke({
      color: isDonorlocal ? 0xffd774 : 0xc7141a,
      width: 2 + intensity * 5,
      alpha: isDonorlocal ? intensity * 0.22 : 0.16 + intensity * 0.5,
    });
  }

  private redrawFeatureBursts(): void {
    const isDonorlocal = this.isDonorlocalProvider();
    this.jackpotTargetHalo.clear();
    this.boostEdgeAura.clear();
    const seamLevel = Math.max(
      0,
      Math.min(1, this.boostSeamHoldMs / (isDonorlocal ? 2400 : 1600)),
    );
    if (this.boostPulse <= 0.02 && this.boostLoop <= 0.08 && seamLevel <= 0.02) {
      this.clearBoostSeamCue();
    }
    this.collectRingSprite.visible = this.collectPulse > 0.02 && this.collectRingSprite.texture !== Texture.EMPTY;
    if (this.collectRingSprite.visible) {
      this.collectRingSprite.position.set(this.collectAnchor.x, this.collectAnchor.y);
      this.collectRingSprite.scale.set(0.9 + this.collectPulse * 0.8 + Math.sin(this.ambientTime * 5) * 0.04);
      this.collectRingSprite.alpha = 0.16 + this.collectPulse * 0.42;
      this.collectRingSprite.tint = 0xffd36f;
      this.collectRingSprite.rotation = this.ambientTime * 0.6;
    }

    this.boostBurstSprite.visible =
      (this.boostPulse > 0.02 || this.boostLoop > 0.08 || seamLevel > 0.08) &&
      this.boostBurstSprite.texture !== Texture.EMPTY;
    if (this.boostBurstSprite.visible) {
      const boostLevel = Math.max(this.boostPulse * 0.9, this.boostLoop * 0.7, seamLevel * 0.76);
      this.boostBurstSprite.position.set(this.boostAnchor.x, this.boostAnchor.y);
      this.boostBurstSprite.scale.set(
        (isDonorlocal ? 0.78 : 0.92) +
          boostLevel * (isDonorlocal ? 0.5 : 0.74) +
          Math.sin(this.ambientTime * 8) * 0.05,
      );
      this.boostBurstSprite.alpha = isDonorlocal
        ? 0.08 + boostLevel * 0.24
        : 0.14 + boostLevel * 0.36;
      this.boostBurstSprite.tint = 0xfff0cf;
        this.boostBurstSprite.rotation = -this.ambientTime * 1.6;
      if (isDonorlocal && this.boostEdgeTarget) {
        const boostEdgeX = this.resolveBoostEdgeX();
        const seamY = 26;
        const targetY = this.boostEdgeTarget.y;
        const bendX = this.boostAnchor.x * 0.54 + boostEdgeX * 0.46;
        const bendY = Math.max(seamY + 24, targetY - (76 + boostLevel * 28));
        this.boostEdgeAura.moveTo(this.machineWidth * 0.5, seamY);
        this.boostEdgeAura.bezierCurveTo(
          this.machineWidth * (this.boostEdgeSide > 0 ? 0.66 : 0.34),
          seamY - 10,
          bendX,
          bendY,
          boostEdgeX,
          targetY - 8,
        );
        this.boostEdgeAura.stroke({
          color: 0xffc44f,
          width: 7 + boostLevel * 10,
          alpha: 0.28 + boostLevel * 0.36,
        });
        this.boostEdgeAura.moveTo(this.machineWidth * 0.5, seamY + 6);
        this.boostEdgeAura.bezierCurveTo(
          this.machineWidth * (this.boostEdgeSide > 0 ? 0.64 : 0.36),
          seamY + 4,
          bendX,
          bendY + 16,
          boostEdgeX - this.boostEdgeSide * 8,
          targetY + 16,
        );
        this.boostEdgeAura.stroke({
          color: 0xfff0c1,
          width: 3 + boostLevel * 4,
          alpha: 0.28 + boostLevel * 0.34,
        });
        this.boostEdgeAura.ellipse(
          boostEdgeX,
          targetY - 8,
          20 + boostLevel * 22,
          36 + boostLevel * 24,
        );
        this.boostEdgeAura.fill({
          color: 0xffa72d,
          alpha: 0.22 + boostLevel * 0.28,
        });
        this.boostEdgeAura.circle(
          boostEdgeX - this.boostEdgeSide * 10,
          targetY - 18,
          18 + boostLevel * 16,
        );
        this.boostEdgeAura.stroke({
          color: 0xfff4cf,
          width: 4 + boostLevel * 4,
          alpha: 0.24 + boostLevel * 0.34,
        });
        this.boostEdgeAura.ellipse(
          boostEdgeX - this.boostEdgeSide * 18,
          28,
          24 + boostLevel * 16,
          12 + boostLevel * 8,
        );
        this.boostEdgeAura.fill({
          color: 0xffd269,
          alpha: 0.18 + boostLevel * 0.22,
        });
        if (this.boostSeamSprite.visible) {
          this.boostSeamSprite.alpha = 0.8 + boostLevel * 0.24 + seamLevel * 0.2;
        }
        if (this.boostSeamSparkSprite.visible) {
          this.boostSeamSparkSprite.alpha = 0.84 + boostLevel * 0.24 + seamLevel * 0.22;
          this.boostSeamSparkSprite.rotation = -this.ambientTime * 1.8 * this.boostEdgeSide;
        }
        this.redrawBoostSeamOverlay(boostLevel);
      }
    }

    const burstHoldLevel = Math.max(
      0,
      Math.min(1, this.jackpotBurstHoldMs / (isDonorlocal ? 2200 : 1200)),
    );
    const jackpotBurstLevel = Math.max(this.jackpotPulse, burstHoldLevel * 0.96);
    this.jackpotBurstSprite.visible =
      jackpotBurstLevel > 0.02 && this.jackpotBurstSprite.texture !== Texture.EMPTY;
    if (this.jackpotBurstSprite.visible) {
      const donorJackpotOnly =
        isDonorlocal &&
        this.jackpotPulse > 0.04 &&
        this.boostLoop < 0.08 &&
        this.boostPulse < 0.08;
      const haloPulse = Math.sin(this.ambientTime * 9.4) * 0.5 + 0.5;
      const haloWidth =
        (isDonorlocal ? 56 : 62) +
        jackpotBurstLevel * (isDonorlocal ? 14 : 42) +
        haloPulse * (isDonorlocal ? 4 : 10);
      const haloHeight =
        (isDonorlocal ? 28 : 34) +
        jackpotBurstLevel * (isDonorlocal ? 8 : 18) +
        haloPulse * (isDonorlocal ? 3 : 6);
      const burstCenter = isDonorlocal
        ? {
            x: Math.max(
              48,
              Math.min(this.machineWidth - 48, this.jackpotBurstAnchor.x + (this.jackpotAnchor.x >= this.machineWidth * 0.5 ? 1 : -1) * 18),
            ),
            y: Math.max(
              this.machineHeight * 0.22,
              Math.min(this.machineHeight * 0.74, this.jackpotBurstAnchor.y),
            ),
          }
        : this.jackpotAnchor;
      if (isDonorlocal) {
        const plaqueSide = this.jackpotAnchor.x >= this.machineWidth * 0.5 ? 1 : -1;
        const seamX = this.machineWidth * (plaqueSide > 0 ? 0.78 : 0.22);
        const seamY = this.machineHeight * 0.46;
        const bendX = burstCenter.x + plaqueSide * -34;
        const bendY = this.machineHeight * 0.22;
        this.jackpotTargetHalo.moveTo(seamX, seamY);
        this.jackpotTargetHalo.bezierCurveTo(
          seamX + plaqueSide * -18,
          seamY - 80,
          bendX,
          bendY,
          burstCenter.x,
          burstCenter.y + 6,
        );
        this.jackpotTargetHalo.stroke({
          color: 0xffc656,
          width: donorJackpotOnly ? 3 + jackpotBurstLevel * 3 : 5 + jackpotBurstLevel * 6,
          alpha: donorJackpotOnly
            ? 0.08 + jackpotBurstLevel * 0.1
            : 0.14 + jackpotBurstLevel * 0.2,
        });
        this.jackpotTargetHalo.moveTo(seamX + plaqueSide * -8, seamY + 14);
        this.jackpotTargetHalo.bezierCurveTo(
          seamX + plaqueSide * -24,
          seamY - 40,
          bendX,
          bendY + 28,
          burstCenter.x + plaqueSide * -10,
          burstCenter.y + 22,
        );
        this.jackpotTargetHalo.stroke({
          color: 0xfff2c6,
          width: donorJackpotOnly ? 1.5 + jackpotBurstLevel * 1.8 : 2 + jackpotBurstLevel * 3,
          alpha: donorJackpotOnly
            ? 0.08 + jackpotBurstLevel * 0.08
            : 0.16 + jackpotBurstLevel * 0.16,
        });
        if (!donorJackpotOnly) {
          this.jackpotTargetHalo.ellipse(
            seamX,
            seamY,
            22 + jackpotBurstLevel * 16,
            40 + jackpotBurstLevel * 18,
          );
          this.jackpotTargetHalo.fill({
            color: 0xff9d2e,
            alpha: 0.06 + jackpotBurstLevel * 0.12,
          });
        }
      }
      this.jackpotTargetHalo.ellipse(
        burstCenter.x,
        burstCenter.y,
        haloWidth,
        haloHeight,
      );
      this.jackpotTargetHalo.fill({
        color: 0xffd773,
        alpha: isDonorlocal
          ? (donorJackpotOnly ? 0.04 + jackpotBurstLevel * 0.08 : 0.06 + jackpotBurstLevel * 0.12)
          : 0.07 + jackpotBurstLevel * 0.16,
      });
      this.jackpotTargetHalo.ellipse(
        burstCenter.x,
        burstCenter.y,
        haloWidth + (isDonorlocal ? 8 : 14),
        haloHeight + (isDonorlocal ? 4 : 8),
      );
      this.jackpotTargetHalo.stroke({
        color: 0xfff5c2,
        width: (isDonorlocal ? 2.5 : 4) + jackpotBurstLevel * (isDonorlocal ? 1.8 : 3),
        alpha: isDonorlocal
          ? (donorJackpotOnly ? 0.06 + jackpotBurstLevel * 0.1 : 0.1 + jackpotBurstLevel * 0.16)
          : 0.12 + jackpotBurstLevel * 0.24,
      });
      this.jackpotTargetHalo.ellipse(
        burstCenter.x,
        burstCenter.y,
        haloWidth + (isDonorlocal ? 14 : 26),
        haloHeight + (isDonorlocal ? 8 : 14),
      );
      this.jackpotTargetHalo.stroke({
        color: 0xffc84b,
        width: (isDonorlocal ? 1.5 : 2.5) + haloPulse * (isDonorlocal ? 0.9 : 1.5),
        alpha: isDonorlocal
          ? (donorJackpotOnly ? 0.04 + jackpotBurstLevel * 0.08 : 0.08 + jackpotBurstLevel * 0.12)
          : 0.08 + jackpotBurstLevel * 0.18,
      });
      if (isDonorlocal) {
        if (!donorJackpotOnly) {
          this.jackpotTargetHalo.ellipse(
            this.fireAnchor.x,
            this.fireAnchor.y,
            16 + jackpotBurstLevel * 10,
            28 + jackpotBurstLevel * 14,
          );
          this.jackpotTargetHalo.fill({
            color: 0xffa133,
            alpha: 0.03 + jackpotBurstLevel * 0.08,
          });
        }
      }
      this.jackpotBurstSprite.position.set(burstCenter.x, burstCenter.y);
      const burstSize =
        (isDonorlocal ? 68 : 156) +
        jackpotBurstLevel * (isDonorlocal ? 28 : 148) +
        Math.sin(this.ambientTime * 10) * (isDonorlocal ? 4 : 10);
      this.jackpotBurstSprite.width = burstSize;
      this.jackpotBurstSprite.height = burstSize;
      this.jackpotBurstSprite.alpha = isDonorlocal
        ? (donorJackpotOnly ? 0.05 + jackpotBurstLevel * 0.1 : 0.08 + jackpotBurstLevel * 0.14)
        : 0.18 + jackpotBurstLevel * 0.46;
      this.jackpotBurstSprite.tint = 0xffdf94;
      this.jackpotBurstSprite.rotation = this.ambientTime * 2.1;
    }
  }

  private reparentSeamHost(): void {
    const targetParent = this.seamOverlayLayer ?? this;
    const seamIndex = 5;
    const jackpotIndex = 3;

    if (this.boostSeamHost.parent !== targetParent) {
      this.boostSeamHost.parent?.removeChild(this.boostSeamHost);
      if (targetParent === this) {
        this.addChildAt(this.boostSeamHost, seamIndex);
      } else {
        targetParent.addChild(this.boostSeamHost);
      }
    }
    if (this.jackpotOverlayHost.parent !== targetParent) {
      this.jackpotOverlayHost.parent?.removeChild(this.jackpotOverlayHost);
      if (targetParent === this) {
        this.addChildAt(this.jackpotOverlayHost, jackpotIndex);
      } else {
        targetParent.addChild(this.jackpotOverlayHost);
      }
    }
  }

  private syncOverlayHostTransforms(): void {
    if (!this.seamOverlayLayer) {
      this.boostSeamHost.position.set(0, 0);
      this.boostSeamHost.scale.set(1, 1);
      this.boostSeamHost.rotation = 0;
      this.jackpotOverlayHost.position.set(0, 0);
      this.jackpotOverlayHost.scale.set(1, 1);
      this.jackpotOverlayHost.rotation = 0;
      return;
    }
    const origin = this.seamOverlayLayer.toLocal(this.toGlobal(new Point(0, 0)));
    const axisX = this.seamOverlayLayer.toLocal(this.toGlobal(new Point(1, 0)));
    const axisY = this.seamOverlayLayer.toLocal(this.toGlobal(new Point(0, 1)));
    const deltaX = { x: axisX.x - origin.x, y: axisX.y - origin.y };
    const deltaY = { x: axisY.x - origin.x, y: axisY.y - origin.y };
    const rotation = Math.atan2(deltaX.y, deltaX.x);
    const scaleX = Math.hypot(deltaX.x, deltaX.y) || 1;
    const scaleY = Math.hypot(deltaY.x, deltaY.y) || 1;
    this.boostSeamHost.position.set(origin.x, origin.y);
    this.boostSeamHost.scale.set(scaleX, scaleY);
    this.boostSeamHost.rotation = rotation;
    this.jackpotOverlayHost.position.set(origin.x, origin.y);
    this.jackpotOverlayHost.scale.set(scaleX, scaleY);
    this.jackpotOverlayHost.rotation = rotation;
  }

  private configureBoostSeamSprite(sprite: Sprite): void {
    sprite.anchor.set(0.5);
    sprite.visible = false;
    sprite.renderable = true;
    sprite.eventMode = "none";
    sprite.blendMode = "normal";
  }

  private recreateBoostSeamSprite(current: Sprite, texture: Texture, childIndex: number): Sprite {
    current.parent?.removeChild(current);
    current.destroy();

    const replacement = new Sprite(texture);
    this.configureBoostSeamSprite(replacement);
    replacement.visible = true;
    replacement.alpha = 1;
    this.boostSeamHost.addChildAt(replacement, Math.min(childIndex, this.boostSeamHost.children.length));

    return replacement;
  }

  private presentBoostSeamCue(): void {
    if (!this.isDonorlocalProvider() || !this.boostEdgeTarget) {
      return;
    }

    const boostLevel = Math.max(this.boostPulse * 0.9, this.boostLoop * 0.7, 0.72);
    const seamTexture = this.resolveBoostSeamTexture();
    const sparkTexture = this.resolveBoostSparkTexture();
    const boostEdgeX = this.resolveBoostEdgeX();
    const targetY = this.boostEdgeTarget.y;
    this.boostSeamHost.visible = true;
    this.boostSeamOverlay.visible = true;

    if (seamTexture) {
      this.boostSeamSprite = this.recreateBoostSeamSprite(this.boostSeamSprite, seamTexture, 0);
      this.applyOverlaySpriteLayout(
        this.boostSeamSprite,
        this.machineWidth * (this.boostEdgeSide > 0 ? 0.72 : 0.28),
        Math.max(48, targetY - 74),
        220 + boostLevel * 140,
        42 + boostLevel * 20,
        this.boostEdgeSide > 0 ? -0.04 : 0.04,
        1,
      );
      this.boostSeamSprite.alpha = 0.88 + boostLevel * 0.24;
      this.boostSeamSprite.tint = 0xfff8ea;
    }

    if (sparkTexture) {
      this.boostSeamSparkSprite = this.recreateBoostSeamSprite(
        this.boostSeamSparkSprite,
        sparkTexture,
        1,
      );
      const sparkSize = 78 + boostLevel * 64;
      this.applyOverlaySpriteLayout(
        this.boostSeamSparkSprite,
        boostEdgeX - this.boostEdgeSide * 8,
        targetY - 10,
        sparkSize,
        sparkSize,
        -this.ambientTime * 1.8 * this.boostEdgeSide,
      );
      this.boostSeamSparkSprite.alpha = 0.94 + boostLevel * 0.2;
      this.boostSeamSparkSprite.tint = 0xfff8ea;
    }
  }

  private clearBoostSeamCue(): void {
    this.boostSeamHost.visible = false;
    this.boostSeamOverlay.clear();
    this.boostSeamOverlay.visible = false;
    this.boostSeamSprite.visible = false;
    this.boostSeamSparkSprite.visible = false;
  }

  private redrawBoostSeamOverlay(boostLevel: number): void {
    if (!this.isDonorlocalProvider() || !this.boostEdgeTarget) {
      this.boostSeamOverlay.clear();
      this.boostSeamOverlay.visible = false;
      return;
    }

    const seamY = 26;
    const boostEdgeX = this.resolveBoostEdgeX();
    const targetY = this.boostEdgeTarget.y;
    const bendX = this.boostAnchor.x * 0.54 + boostEdgeX * 0.46;
    const bendY = Math.max(seamY + 24, targetY - (76 + boostLevel * 28));
    const start = this.resolveOverlayPoint(this.machineWidth * 0.5, seamY);
    const startLower = this.resolveOverlayPoint(this.machineWidth * 0.5, seamY + 6);
    const curveA = this.resolveOverlayPoint(
      this.machineWidth * (this.boostEdgeSide > 0 ? 0.66 : 0.34),
      seamY - 10,
    );
    const curveALower = this.resolveOverlayPoint(
      this.machineWidth * (this.boostEdgeSide > 0 ? 0.64 : 0.36),
      seamY + 4,
    );
    const curveB = this.resolveOverlayPoint(bendX, bendY);
    const curveBLower = this.resolveOverlayPoint(bendX, bendY + 16);
    const edge = this.resolveOverlayPoint(boostEdgeX, targetY - 8);
    const edgeLower = this.resolveOverlayPoint(
      boostEdgeX - this.boostEdgeSide * 8,
      targetY + 16,
    );
    const target = this.resolveOverlayPoint(boostEdgeX, targetY - 8);
    const crown = this.resolveOverlayPoint(
      boostEdgeX - this.boostEdgeSide * 18,
      28,
    );
    const strokeScale = this.resolveOverlayStrokeScale();

    this.boostSeamOverlay.clear();
    this.boostSeamOverlay.visible = true;
    this.boostSeamOverlay.moveTo(start.x, start.y);
    this.boostSeamOverlay.bezierCurveTo(
      curveA.x,
      curveA.y,
      curveB.x,
      curveB.y,
      edge.x,
      edge.y,
    );
    this.boostSeamOverlay.stroke({
      color: 0xffd058,
      width: (10 + boostLevel * 14) * strokeScale,
      alpha: 0.48 + boostLevel * 0.42,
      cap: "round",
      join: "round",
    });
    this.boostSeamOverlay.moveTo(startLower.x, startLower.y);
    this.boostSeamOverlay.bezierCurveTo(
      curveALower.x,
      curveALower.y,
      curveBLower.x,
      curveBLower.y,
      edgeLower.x,
      edgeLower.y,
    );
    this.boostSeamOverlay.stroke({
      color: 0xfff5d1,
      width: (5 + boostLevel * 6) * strokeScale,
      alpha: 0.4 + boostLevel * 0.34,
      cap: "round",
      join: "round",
    });
    this.boostSeamOverlay.ellipse(
      target.x,
      target.y,
      (24 + boostLevel * 24) * strokeScale,
      (40 + boostLevel * 28) * strokeScale,
    );
    this.boostSeamOverlay.fill({
      color: 0xffa62d,
      alpha: 0.3 + boostLevel * 0.32,
    });
    this.boostSeamOverlay.circle(
      target.x - this.boostEdgeSide * 10 * strokeScale,
      target.y - 12 * strokeScale,
      (18 + boostLevel * 16) * strokeScale,
    );
    this.boostSeamOverlay.stroke({
      color: 0xfff7df,
      width: (5 + boostLevel * 6) * strokeScale,
      alpha: 0.38 + boostLevel * 0.36,
    });
    this.boostSeamOverlay.ellipse(
      crown.x,
      crown.y,
      (24 + boostLevel * 16) * strokeScale,
      (12 + boostLevel * 8) * strokeScale,
    );
    this.boostSeamOverlay.fill({
      color: 0xffd975,
      alpha: 0.28 + boostLevel * 0.24,
    });
  }

  private resolveOverlayPoint(localX: number, localY: number): FxPoint {
    if (!this.seamOverlayLayer || this.boostSeamHost.parent === this) {
      return { x: localX, y: localY };
    }
    const globalPoint = this.toGlobal({ x: localX, y: localY });
    const overlayPoint = this.boostSeamHost.toLocal(globalPoint);
    return { x: overlayPoint.x, y: overlayPoint.y };
  }

  private resolveOverlayStrokeScale(): number {
    if (!this.seamOverlayLayer || this.boostSeamHost.parent === this) {
      return 1;
    }
    const controllerScale = this.getContainerScale(this);
    const overlayScale = this.getContainerScale(this.boostSeamHost);
    return ((controllerScale.x / overlayScale.x) + (controllerScale.y / overlayScale.y)) * 0.5;
  }

  private getContainerScale(target: Container): { x: number; y: number } {
    const matrix = target.worldTransform;
    return {
      x: Math.hypot(matrix.a, matrix.b) || 1,
      y: Math.hypot(matrix.c, matrix.d) || 1,
    };
  }

  private applyOverlaySpriteLayout(
    sprite: Sprite,
    localX: number,
    localY: number,
    width: number,
    height: number,
    rotation = 0,
    flipX: -1 | 1 = 1,
  ): void {
    if (!this.seamOverlayLayer || sprite.parent === this) {
      sprite.position.set(localX, localY);
      sprite.width = width;
      sprite.height = height;
      sprite.rotation = rotation;
      sprite.scale.x = flipX * Math.abs(sprite.scale.x || 1);
      return;
    }

    const globalPoint = this.toGlobal({ x: localX, y: localY });
    const overlayPoint = this.seamOverlayLayer.toLocal(globalPoint);
    const controllerScale = this.getContainerScale(this);
    const overlayScale = this.getContainerScale(this.seamOverlayLayer);
    const widthScale = controllerScale.x / overlayScale.x;
    const heightScale = controllerScale.y / overlayScale.y;

    sprite.position.set(overlayPoint.x, overlayPoint.y);
    sprite.width = width * widthScale;
    sprite.height = height * heightScale;
    sprite.rotation = rotation;
    sprite.scale.x = flipX * Math.abs(sprite.scale.x || 1);
  }

  private redrawFireFront(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const donorJackpotOnly =
      isDonorlocal &&
      this.jackpotPulse > 0.04 &&
      this.boostLoop < 0.08 &&
      this.boostPulse < 0.08;
    const intensity = isDonorlocal
      ? this.boostPulse * 0.18 +
        this.jackpotPulse * (donorJackpotOnly ? 0.06 : 0.12) +
        this.winPulse * 0.14
      : 0.16 + this.boostPulse * 0.18 + this.jackpotPulse * 0.12 + this.winPulse * 0.14;
    const topY = this.machineHeight * 0.08;
    this.fireFront.clear();
    if (isDonorlocal && intensity < 0.03) {
      return;
    }

    for (let index = 0; index < 4; index += 1) {
      const phase = this.ambientTime * 3 + index * 1.2;
      const x = this.machineWidth * (0.22 + index * 0.18);
      this.fireFront.ellipse(x, topY + Math.sin(phase) * 6, 30 + intensity * 14, 12 + intensity * 9);
      this.fireFront.fill({ color: 0xffd877, alpha: 0.06 + intensity * 0.14 });
    }

    const featureFire =
      this.boostPulse * 0.9 + this.boostLoop * 0.64 + this.jackpotPulse * (donorJackpotOnly ? 0.44 : 1.05);
    if (featureFire > 0.05) {
      if (isDonorlocal) {
        const inset = 12;
        const boostFocusedEdge = this.boostEdgeTarget && this.boostLoop > 0.08;
        const plaqueSide = this.jackpotAnchor.x >= this.machineWidth * 0.5 ? 1 : -1;
        this.fireFront.roundRect(
          inset,
          inset,
          this.machineWidth - inset * 2,
          this.machineHeight - inset * 2,
          20,
        );
        this.fireFront.stroke({
          color: 0xffefb3,
          width: 2 + featureFire * 3.2,
          alpha: donorJackpotOnly ? 0.03 + featureFire * 0.06 : 0.08 + featureFire * 0.14,
        });
        const activeEdgeX = this.resolveBoostEdgeX();
        const activeEdgeTargetY = this.boostEdgeTarget?.y ?? this.machineHeight * 0.5;
        const perimeterFlares = boostFocusedEdge
          ? [
              { x: this.machineWidth * 0.5, y: 18, width: 40, height: 13 },
              {
                x: this.machineWidth * (this.boostEdgeSide > 0 ? 0.76 : 0.24),
                y: 18,
                width: 34,
                height: 12,
              },
              {
                x: activeEdgeX - this.boostEdgeSide * 10,
                y: Math.max(30, activeEdgeTargetY * 0.34),
                width: 14,
                height: 36,
              },
              {
                x: activeEdgeX - this.boostEdgeSide * 8,
                y: activeEdgeTargetY - 8,
                width: 16,
                height: 40,
              },
            ]
          : donorJackpotOnly
            ? [
                {
                  x: this.machineWidth * (plaqueSide > 0 ? 0.74 : 0.26),
                  y: 18,
                  width: 24,
                  height: 10,
                },
                {
                  x: this.fireAnchor.x + plaqueSide * -10,
                  y: Math.max(28, this.fireAnchor.y - 16),
                  width: 10,
                  height: 24,
                },
                {
                  x: this.jackpotBurstAnchor.x + plaqueSide * -6,
                  y: this.jackpotBurstAnchor.y + 6,
                  width: 12,
                  height: 28,
                },
              ]
          : [
              { x: this.machineWidth * 0.16, y: 18, width: 28, height: 12 },
              { x: this.machineWidth * 0.5, y: 18, width: 36, height: 13 },
              { x: this.machineWidth * 0.84, y: 18, width: 28, height: 12 },
            ];
        perimeterFlares.forEach((flare, index) => {
          const phase = this.ambientTime * 5 + index * 0.9;
          this.fireFront.ellipse(
            flare.x,
            flare.y,
            flare.width + Math.sin(phase) * 2 + featureFire * (donorJackpotOnly ? 4 : 10),
            flare.height + Math.cos(phase) * 2 + featureFire * (donorJackpotOnly ? 4 : 8),
          );
          this.fireFront.fill({
            color: 0xfff1c7,
            alpha: donorJackpotOnly ? 0.02 + featureFire * 0.05 : 0.05 + featureFire * 0.12,
          });
        });
        if (this.boostEdgeTarget && this.boostLoop > 0.08) {
          const edgeX = this.resolveBoostEdgeX();
          const targetY = this.boostEdgeTarget.y;
          this.fireFront.ellipse(
            edgeX,
            targetY - 18,
            10 + featureFire * 8,
            34 + featureFire * 24,
          );
          this.fireFront.fill({
            color: 0xffefbf,
            alpha: 0.08 + featureFire * 0.16,
          });
          this.fireFront.ellipse(
            edgeX - this.boostEdgeSide * 12,
            targetY - 4,
            8 + featureFire * 7,
            62 + featureFire * 38,
          );
          this.fireFront.fill({
            color: 0xfff2c6,
            alpha: 0.08 + featureFire * 0.18,
          });
          this.fireFront.ellipse(
            edgeX - this.boostEdgeSide * 14,
            26,
            20 + featureFire * 10,
            10 + featureFire * 6,
          );
          this.fireFront.fill({
            color: 0xfff4d0,
            alpha: 0.06 + featureFire * 0.14,
          });
        }
      }
      const anchorX = this.fireAnchor.x;
      const anchorY = this.fireAnchor.y;
      for (let index = 0; index < 2; index += 1) {
        const phase = this.ambientTime * 4.4 + index;
        const x = anchorX + (index === 0 ? -16 : 16);
        const y = anchorY - 10 + Math.sin(phase) * 7;
        this.fireFront.ellipse(
          x,
          y,
          28 + featureFire * (donorJackpotOnly ? 8 : 16),
          18 + featureFire * (donorJackpotOnly ? 6 : 12),
        );
        this.fireFront.fill({
          color: 0xffdf82,
          alpha: donorJackpotOnly ? 0.03 + featureFire * 0.08 : 0.08 + featureFire * 0.18,
        });
      }
      this.fireFront.ellipse(
        anchorX,
        anchorY - 22,
        44 + featureFire * (donorJackpotOnly ? 12 : 28),
        20 + featureFire * (donorJackpotOnly ? 4 : 10),
      );
      this.fireFront.fill({
        color: 0xfff0bf,
        alpha: donorJackpotOnly ? 0.02 + featureFire * 0.06 : 0.05 + featureFire * 0.12,
      });
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
      const donorCoin = coin.frames.length > 1;
      coin.sprite.scale.set(
        donorCoin ? 0.74 + inv * 0.24 : 0.52 + inv * 0.18,
      );
      if (coin.frames.length > 0) {
        const frameIndex =
          Math.floor(coin.elapsedMs / 44 + coin.frameOffset) % coin.frames.length;
        const frameTexture = coin.frames[frameIndex] ?? coin.frames[0];
        coin.sprite.texture = frameTexture;
        if (donorCoin && frameTexture) {
          coin.sprite.width = Math.max(56, frameTexture.width || 64);
          coin.sprite.height = Math.max(56, frameTexture.height || 64);
        }
      }
      if (coin.glow) {
        coin.glow.x = x;
        coin.glow.y = y;
        coin.glow.alpha = donorCoin ? 0.2 + inv * 0.26 : 0.12 + inv * 0.12;
        coin.glow.scale.set(donorCoin ? 1.05 + inv * 0.3 : 0.86 + inv * 0.16);
      }

      if (progress >= 1) {
        this.recycleCoin(coin.sprite, coin.glow);
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
    options: CoinBurstOptions = {},
  ): void {
    for (let index = 0; index < count; index += 1) {
      const sprite = this.obtainCoinSprite();
      const glow = this.obtainCoinGlowSprite();
      const startSpreadX = options.startSpreadX ?? 54;
      const startSpreadY = options.startSpreadY ?? 32;
      const endSpreadX = options.endSpreadX ?? 42;
      const endSpreadY = options.endSpreadY ?? 18;
      const controlLift = options.controlLift ?? 80;
      const startX = fromX + (Math.random() - 0.5) * startSpreadX;
      const startY = fromY + (Math.random() - 0.5) * startSpreadY;
      const endX = toX + (Math.random() - 0.5) * endSpreadX;
      const endY = toY + (Math.random() - 0.5) * endSpreadY;
      const controlX = (startX + endX) * 0.5 + (Math.random() - 0.5) * 96;
      const controlY = Math.min(startY, endY) - controlLift - Math.random() * 70;

      sprite.x = startX;
      sprite.y = startY;
      sprite.alpha = 1;
      sprite.rotation = Math.random() * Math.PI * 2;
      sprite.scale.set(0.62 + Math.random() * 0.18);
      if (glow) {
        glow.x = startX;
        glow.y = startY;
        glow.alpha = 0.34;
        this.coinLayer.addChild(glow);
      }
      this.coinLayer.addChild(sprite);

      this.coinFlights.push({
        sprite,
        glow,
        startX,
        startY,
        controlX,
        controlY,
        endX,
        endY,
        lifeMs: Math.max(280, (options.durationMs ?? 560) + Math.random() * 180),
        elapsedMs: 0,
        spin: (Math.random() - 0.5) * 7,
        frames: this.resolveCoinFlightFrames(),
        frameOffset: Math.floor(Math.random() * 9),
      });
    }
  }

  private clearScheduledEffects(): void {
    while (this.scheduledEffectTimeouts.length > 0) {
      const timeout = this.scheduledEffectTimeouts.pop();
      if (timeout !== undefined) {
        window.clearTimeout(timeout);
      }
    }
  }

  private resolveOrigins(origins: FxPoint[], fallback: FxPoint): FxPoint[] {
    if (origins.length === 0) {
      return [fallback];
    }

    const deduped = new Map<string, FxPoint>();
    origins.forEach((origin) => {
      const key = `${Math.round(origin.x)}:${Math.round(origin.y)}`;
      if (!deduped.has(key)) {
        deduped.set(key, origin);
      }
    });
    return [...deduped.values()];
  }

  private resolveEffectAnchor(
    origins: FxPoint[],
    targetX: number,
    targetY: number,
    targetWeight: number,
  ): FxPoint {
    if (origins.length === 0) {
      return { x: targetX, y: targetY };
    }
    const sum = origins.reduce(
      (acc, point) => ({ x: acc.x + point.x, y: acc.y + point.y }),
      { x: 0, y: 0 },
    );
    const averageX = sum.x / origins.length;
    const averageY = sum.y / origins.length;
    const clampedWeight = Math.max(0, Math.min(1, targetWeight));
    return {
      x: averageX * (1 - clampedWeight) + targetX * clampedWeight,
      y: averageY * (1 - clampedWeight) + targetY * clampedWeight,
    };
  }

  private resolveBoostEdgeX(): number {
    return this.machineWidth * (this.boostEdgeSide > 0 ? 0.96 : 0.04);
  }

  private playLightningBurst(
    origins: FxPoint[],
    options: {
      targetX: number;
      targetY: number;
      durationMs?: number;
      staggerMs?: number;
      repeatCount?: number;
      repeatGapMs?: number;
    },
  ): void {
    const staggerMs = options.staggerMs ?? 72;
    const repeatCount = Math.max(1, options.repeatCount ?? 1);
    const repeatGapMs = Math.max(0, options.repeatGapMs ?? 0);
    const sampledOrigins =
      origins.length <= this.lightningFxPool.length
        ? origins
        : [
            origins[0],
            origins[Math.floor(origins.length * 0.5)],
            origins[origins.length - 1],
          ];

    for (let repeatIndex = 0; repeatIndex < repeatCount; repeatIndex += 1) {
      sampledOrigins.forEach((origin, index) => {
        const fx = this.lightningFxPool[this.lightningCursor % this.lightningFxPool.length];
        this.lightningCursor += 1;
        const timeout = window.setTimeout(() => {
          void fx.play(this.machineWidth, this.machineHeight, {
            sourceX: origin.x,
            sourceY: origin.y,
            targetX: options.targetX,
            targetY: options.targetY,
            durationMs:
              options.durationMs === undefined
                ? undefined
                : Math.max(360, options.durationMs - repeatIndex * 80),
          });
        }, index * staggerMs + repeatIndex * repeatGapMs);
        this.scheduledEffectTimeouts.push(timeout);
      });
    }
  }

  private obtainCoinSprite(): Sprite {
    const sprite = this.coinPool.pop() ?? new Sprite(Texture.WHITE);
    sprite.anchor.set(0.5);
    const donorFrames = this.resolveCoinFlightFrames();
    if (donorFrames.length > 0) {
      sprite.texture = donorFrames[0];
      sprite.tint = 0xffffff;
      sprite.width = Math.max(56, donorFrames[0].width || 64);
      sprite.height = Math.max(56, donorFrames[0].height || 64);
      return sprite;
    }

    sprite.texture = this.coinTexture ?? Texture.WHITE;
    sprite.tint = this.coinTexture ? 0xffffff : 0xffce54;
    sprite.width = 34;
    sprite.height = 34;
    return sprite;
  }

  private obtainCoinGlowSprite(): Sprite | null {
    if (this.donorCoinGlowTexture === null) {
      return null;
    }
    const glow = this.coinGlowPool.pop() ?? new Sprite(this.donorCoinGlowTexture);
    glow.anchor.set(0.5);
    glow.texture = this.donorCoinGlowTexture;
    glow.tint = 0xfff3ba;
    glow.blendMode = "add";
    glow.width = Math.max(84, this.donorCoinGlowTexture.width || 84);
    glow.height = Math.max(84, this.donorCoinGlowTexture.height || 84);
    return glow;
  }

  private recycleCoin(sprite: Sprite, glow: Sprite | null = null): void {
    if (sprite.parent === this.coinLayer) {
      this.coinLayer.removeChild(sprite);
    }
    if (glow?.parent === this.coinLayer) {
      this.coinLayer.removeChild(glow);
    }
    this.coinPool.push(sprite);
    if (glow) {
      this.coinGlowPool.push(glow);
    }
  }

  private async refreshTextures(): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const [
      coin,
      collectRing,
      boostBurst,
      jackpotBurst,
      jackpotBurstHero,
      donorCoinGlow,
      donorBoostSeamTextures,
      donorBoostSparkTextures,
      donorJackpotTextures,
    ] = await Promise.all([
      resolveProviderFrameTexture("symbolAtlas", "coin-multiplier-2x"),
      resolveProviderFrameTexture("vfxAtlas", "collector-ring"),
      resolveProviderFrameTexture("vfxAtlas", "spark-burst-01"),
      resolveProviderFrameTexture("vfxAtlas", "spark-burst-03"),
      resolveProviderFrameTexture("heroVfxAtlas", "jackpot-burst"),
      this.loadDonorCoinGlowTexture(),
      this.loadDonorBoostSeamTextures(),
      this.loadDonorBoostSparkTextures(),
      this.loadDonorJackpotTextures(),
    ]);
    if (requestToken !== this.textureRequestToken) {
      return;
    }
    this.coinTexture = coin.texture;
    this.collectRingSprite.texture = collectRing.texture ?? Texture.EMPTY;
    this.boostBurstSprite.texture = boostBurst.texture ?? Texture.EMPTY;
    this.defaultJackpotBurstTexture =
      jackpotBurstHero.texture ?? jackpotBurst.texture ?? Texture.EMPTY;
    this.jackpotBurstSprite.texture =
      (this.jackpotTier ? donorJackpotTextures[this.jackpotTier] : null) ??
      this.defaultJackpotBurstTexture;
    this.donorCoinGlowTexture = donorCoinGlow;
    this.donorCoinTextures = await this.loadDonorCoinFlightTextures();
    this.donorBoostSeamTextures = donorBoostSeamTextures;
    this.donorBoostSparkTextures = donorBoostSparkTextures;
    this.donorJackpotTextures = donorJackpotTextures;
  }

  private resolveJackpotTier(tier: string | null): JackpotTier | null {
    switch ((tier ?? "").toLowerCase()) {
      case "mini":
      case "minor":
      case "major":
      case "grand":
        return (tier ?? "").toLowerCase() as JackpotTier;
      default:
        return null;
    }
  }

  private resolveCoinFlightFrames(): Texture[] {
    return getProviderPackStatus().effectiveProvider === "donorlocal" &&
      this.donorCoinTextures.length > 0
      ? this.donorCoinTextures
      : this.coinTexture
        ? [this.coinTexture]
        : [];
  }

  private async loadDonorCoinFlightTextures(): Promise<Texture[]> {
    const packStatus = getProviderPackStatus();
    if (packStatus.effectiveProvider !== "donorlocal" || !packStatus.manifestUrl) {
      return [];
    }

    const frameSources = Array.from({ length: 9 }, (_, index) => {
      const frame = String(index + 1).padStart(4, "0");
      return `../anims_v5/coin_fly.atlas#coin/coin_real${frame}`;
    });

    const resolved = await Promise.all(
      frameSources.map((source, index) =>
        resolveMappedSourceTexture({
          source,
          baseUrl: packStatus.manifestUrl!,
          cachePrefix: `donorlocal-coin-fly:${index + 1}`,
          atlasOrigin: "top-left",
          rasterizeFrame: true,
        }),
      ),
    );

    return resolved.filter((texture): texture is Texture => texture !== null);
  }

  private async loadDonorCoinGlowTexture(): Promise<Texture | null> {
    const packStatus = getProviderPackStatus();
    if (packStatus.effectiveProvider !== "donorlocal" || !packStatus.manifestUrl) {
      return null;
    }

    return await resolveMappedSourceTexture({
      source: "../anims_v5/coin_fly.atlas#coin/coin_glow",
      baseUrl: packStatus.manifestUrl,
      cachePrefix: "donorlocal-coin-fly-glow",
      atlasOrigin: "top-left",
      rasterizeFrame: true,
    });
  }

  private async loadDonorJackpotTextures(): Promise<Partial<Record<JackpotTier, Texture>>> {
    const packStatus = getProviderPackStatus();
    if (packStatus.effectiveProvider !== "donorlocal" || !packStatus.manifestUrl) {
      return {};
    }

    const baseUrl = packStatus.manifestUrl;
    const entries: Array<[JackpotTier, string]> = [
      ["mini", "../image/img_mini_coin.e12ee50e.png"],
      ["minor", "../image/img_minor_coin.fea1c83b.png"],
      ["major", "../image/img_major_coin.b2d60eb9.png"],
      ["grand", "../image/img_grand_coin.68e52468.png"],
    ];
    const loaded = await Promise.all(
      entries.map(async ([tierKey, source]) => [
        tierKey,
        await resolveMappedSourceTexture({
          source,
          baseUrl,
          cachePrefix: `donor-jackpot:${tierKey}`,
        }),
      ] as const),
    );
    return Object.fromEntries(
      loaded.filter((entry): entry is readonly [JackpotTier, Texture] => Boolean(entry[1])),
    );
  }

  private resolveBoostSeamTexture(): Texture | null {
    if (this.donorBoostSeamTextures.length === 0) {
      return null;
    }
    const index = Math.floor(this.ambientTime * 20) % this.donorBoostSeamTextures.length;
    return this.donorBoostSeamTextures[index] ?? this.donorBoostSeamTextures[0] ?? null;
  }

  private resolveBoostSparkTexture(): Texture | null {
    if (this.donorBoostSparkTextures.length === 0) {
      return null;
    }
    const index = Math.floor(this.ambientTime * 26) % this.donorBoostSparkTextures.length;
    return this.donorBoostSparkTextures[index] ?? this.donorBoostSparkTextures[0] ?? null;
  }

  private async loadDonorBoostSeamTextures(): Promise<Texture[]> {
    const packStatus = getProviderPackStatus();
    if (packStatus.effectiveProvider !== "donorlocal" || !packStatus.manifestUrl) {
      return [];
    }

    const frameSources = [
      "0003",
      "0005",
      "0007",
      "0009",
      "0011",
      "0012",
    ].map(
      (frame, index) =>
        resolveMappedSourceTexture({
          source: `../anims_v5/lightning_path2.atlas#lightning_path/lighting_shot_${frame}`,
          baseUrl: packStatus.manifestUrl!,
          cachePrefix: `donorlocal-boost-seam:${index + 1}`,
          atlasOrigin: "top-left",
          rasterizeFrame: true,
        }),
    );

    const resolved = await Promise.all(frameSources);
    return resolved.filter((texture): texture is Texture => texture !== null);
  }

  private async loadDonorBoostSparkTextures(): Promise<Texture[]> {
    const packStatus = getProviderPackStatus();
    if (packStatus.effectiveProvider !== "donorlocal" || !packStatus.manifestUrl) {
      return [];
    }

    const frameSources = ["0000", "0001", "0002", "0003", "0004"].map(
      (frame, index) =>
        resolveMappedSourceTexture({
          source: `../anims_v5/lightning_path2.atlas#lightning_path/sparks${frame}`,
          baseUrl: packStatus.manifestUrl!,
          cachePrefix: `donorlocal-boost-spark:${index + 1}`,
          atlasOrigin: "top-left",
          rasterizeFrame: true,
        }),
    );

    const resolved = await Promise.all(frameSources);
    return resolved.filter((texture): texture is Texture => texture !== null);
  }
}
