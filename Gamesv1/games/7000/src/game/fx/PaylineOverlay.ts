import { Container, Graphics, Sprite, Text, Texture, Ticker } from "pixi.js";

import { resolveProviderFrameTexture } from "../../app/assets/providerPackRegistry";
import paylinesData from "../../../math/paylines.json";
import paytableData from "../../../math/paytable.json";

export type PaylineOverlayPoint = {
  x: number;
  y: number;
};

export type PaylineOverlayLine = {
  lineId: number;
  symbolId: number;
  multiplier: number;
  amountMinor: number;
  points: PaylineOverlayPoint[];
};

export type PaylineOverlayTone =
  | "standard"
  | "collect"
  | "boost"
  | "bonus"
  | "jackpot";

export type PaylineOverlayOptions = {
  durationMs?: number;
  sequenceCount?: number;
  sequenceIndex?: number;
  styleHook?: string;
  tone?: PaylineOverlayTone;
};

const PAYLINE_NAME_BY_ID = new Map<number, string>(
  (Array.isArray((paylinesData as { paylines?: unknown[] }).paylines)
    ? (paylinesData as { paylines: Array<{ lineId?: number; name?: string }> }).paylines
    : []
  )
    .map((entry) => ({
      lineId: typeof entry.lineId === "number" ? entry.lineId : -1,
      name: typeof entry.name === "string" ? entry.name.replace(/_/g, " ") : "",
    }))
    .filter((entry) => entry.lineId > 0 && entry.name.length > 0)
    .map((entry) => [entry.lineId, entry.name]),
);

const SYMBOL_KEY_BY_ID = new Map<number, string>(
  Object.entries(
    (paytableData as { linePayouts?: Record<string, { symbolKey?: string }> }).linePayouts ?? {},
  )
    .map(([symbolIdRaw, entry]) => ({
      symbolId: Number(symbolIdRaw),
      symbolKey: typeof entry.symbolKey === "string" ? entry.symbolKey : "",
    }))
    .filter((entry) => Number.isFinite(entry.symbolId) && entry.symbolKey.length > 0)
    .map((entry) => [entry.symbolId, entry.symbolKey]),
);

type PaylineOverlayStyle = {
  featureLabel: string;
  glow: number;
  support: number;
  core: number;
  spark: number;
  nodeFill: number;
  panelFill: number;
  panelStroke: number;
  accentFill: number;
  badgeFill: number;
  badgeStroke: number;
  sequenceFill: number;
  multiplierFill: number;
  payoutFill: number;
  detailFill: number;
};

type PaylineOverlayTextureState = {
  plate: Texture | null;
  accent: Texture | null;
  sequence: Texture | null;
  badge: Texture | null;
  multiplier: Texture | null;
};

export class PaylineOverlay extends Container {
  private static readonly CALLOUT_WIDTH = 348;
  private static readonly CALLOUT_HEIGHT = 94;

  private readonly supportPath = new Graphics();
  private readonly glowPath = new Graphics();
  private readonly tracePath = new Graphics();
  private readonly sparkTrail = new Graphics();
  private readonly nodes = new Graphics();
  private readonly callout = new Container();
  private readonly calloutShadow = new Graphics();
  private readonly plateSprite = new Sprite(Texture.WHITE);
  private readonly calloutBase = new Graphics();
  private readonly accentSprite = new Sprite(Texture.WHITE);
  private readonly calloutAccent = new Graphics();
  private readonly badgeSprite = new Sprite(Texture.WHITE);
  private readonly lineBadge = new Graphics();
  private readonly sequenceSprite = new Sprite(Texture.WHITE);
  private readonly sequenceBadge = new Graphics();
  private readonly multiplierSprite = new Sprite(Texture.WHITE);
  private readonly featureTag = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 14,
      fontWeight: "900",
      fill: 0xfff5d2,
      stroke: { color: 0x1a0406, width: 4 },
      letterSpacing: 1.4,
      align: "center",
    },
  });
  private readonly lineBadgeText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 22,
      fontWeight: "900",
      fill: 0xffffff,
      stroke: { color: 0x1a0406, width: 5 },
      letterSpacing: 0.6,
      align: "center",
    },
  });
  private readonly sequenceText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 15,
      fontWeight: "900",
      fill: 0xfff5d8,
      stroke: { color: 0x1a0406, width: 4 },
      letterSpacing: 0.8,
      align: "center",
    },
  });
  private readonly multiplierText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 34,
      fontWeight: "900",
      fill: 0xfff8e3,
      stroke: { color: 0x220307, width: 6 },
      letterSpacing: 0.4,
      align: "center",
    },
  });
  private readonly payoutText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 21,
      fontWeight: "900",
      fill: 0xfff0bd,
      stroke: { color: 0x220307, width: 5 },
      letterSpacing: 0.4,
      align: "right",
    },
  });
  private readonly detailText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 13,
      fontWeight: "800",
      fill: 0xffedd2,
      stroke: { color: 0x180306, width: 4 },
      letterSpacing: 0.6,
      align: "center",
    },
  });

  private activeLine: PaylineOverlayLine | null = null;
  private activeTone: PaylineOverlayTone = "standard";
  private currentStyleHook = "";
  private traceProgress = 0;
  private pulseTime = 0;
  private traceDurationMs = 260;
  private displayDurationMs = 760;
  private displayElapsedMs = 0;
  private sequenceIndex = 0;
  private sequenceCount = 1;
  private textureRequestToken = 0;
  private textureState: PaylineOverlayTextureState = {
    plate: null,
    accent: null,
    sequence: null,
    badge: null,
    multiplier: null,
  };

  constructor() {
    super();

    this.plateSprite.anchor.set(0.5);
    this.plateSprite.visible = false;
    this.accentSprite.anchor.set(0.5);
    this.accentSprite.visible = false;
    this.badgeSprite.anchor.set(0.5);
    this.badgeSprite.visible = false;
    this.sequenceSprite.anchor.set(0.5);
    this.sequenceSprite.visible = false;
    this.multiplierSprite.anchor.set(0.5);
    this.multiplierSprite.visible = false;

    this.featureTag.anchor.set(0.5);
    this.lineBadgeText.anchor.set(0.5);
    this.sequenceText.anchor.set(0.5);
    this.multiplierText.anchor.set(0.5);
    this.payoutText.anchor.set(1, 0.5);
    this.detailText.anchor.set(0.5, 0);

    this.callout.addChild(
      this.calloutShadow,
      this.plateSprite,
      this.calloutBase,
      this.accentSprite,
      this.calloutAccent,
      this.badgeSprite,
      this.lineBadge,
      this.sequenceSprite,
      this.sequenceBadge,
      this.multiplierSprite,
      this.featureTag,
      this.lineBadgeText,
      this.sequenceText,
      this.multiplierText,
      this.payoutText,
      this.detailText,
    );
    this.callout.visible = false;

    this.addChild(
      this.supportPath,
      this.glowPath,
      this.tracePath,
      this.sparkTrail,
      this.nodes,
      this.callout,
    );

    Ticker.shared.add((ticker) => {
      if (!this.activeLine) {
        return;
      }

      this.displayElapsedMs = Math.min(
        this.displayDurationMs,
        this.displayElapsedMs + ticker.deltaMS,
      );
      this.traceProgress = Math.min(1, this.displayElapsedMs / this.traceDurationMs);
      this.pulseTime += ticker.deltaMS / 1000;
      this.redraw();
    });
  }

  public showLine(line: PaylineOverlayLine, options: PaylineOverlayOptions = {}): void {
    this.activeLine = line;
    this.activeTone = options.tone ?? "standard";
    this.currentStyleHook = options.styleHook ?? "";
    this.traceDurationMs = Math.max(
      220,
      Math.min(480, (options.durationMs ?? 760) * 0.32),
    );
    this.displayDurationMs = Math.max(420, options.durationMs ?? 760);
    this.displayElapsedMs = 0;
    this.traceProgress = 0;
    this.pulseTime = 0;
    this.sequenceIndex = Math.max(0, options.sequenceIndex ?? 0);
    this.sequenceCount = Math.max(1, options.sequenceCount ?? 1);
    this.callout.visible = true;
    void this.refreshTextures();
    this.redraw();
  }

  public clear(): void {
    this.activeLine = null;
    this.traceProgress = 0;
    this.pulseTime = 0;
    this.displayElapsedMs = 0;
    this.supportPath.clear();
    this.glowPath.clear();
    this.tracePath.clear();
    this.sparkTrail.clear();
    this.nodes.clear();
    this.callout.visible = false;
    this.textureRequestToken += 1;
  }

  private redraw(): void {
    if (!this.activeLine) {
      this.clear();
      return;
    }

    const style = this.resolveStyle(this.currentStyleHook, this.activeTone);
    const points = this.activeLine.points;
    const visibility = this.resolveVisibilityAlpha();
    const pulseAlpha = 0.78 + Math.sin(this.pulseTime * 7.6) * 0.14;

    this.supportPath.clear();
    this.drawPolyline(
      this.supportPath,
      points,
      28,
      style.support,
      visibility * 0.16,
      1,
    );

    this.glowPath.clear();
    this.drawPolyline(
      this.glowPath,
      points,
      17,
      style.glow,
      visibility * (0.26 + pulseAlpha * 0.18),
      1,
    );

    this.tracePath.clear();
    this.drawPolyline(
      this.tracePath,
      points,
      7,
      style.core,
      visibility * 0.96,
      this.traceProgress,
    );

    this.nodes.clear();
    points.forEach((point, index) => {
      const outerRadius = index === 1 ? 18 : 15;
      const innerRadius = index === 1 ? 8 : 7;
      this.nodes.circle(point.x, point.y, outerRadius);
      this.nodes.fill({ color: style.nodeFill, alpha: visibility * (0.16 + pulseAlpha * 0.18) });
      this.nodes.circle(point.x, point.y, innerRadius + Math.sin(this.pulseTime * 8 + index) * 1.1);
      this.nodes.fill({ color: style.core, alpha: visibility * 0.94 });
    });

    this.redrawSpark(points, style, visibility);
    this.redrawCallout(style, visibility);
  }

  private redrawSpark(
    points: PaylineOverlayPoint[],
    style: PaylineOverlayStyle,
    visibility: number,
  ): void {
    const sparkProgress =
      this.traceProgress < 1
        ? this.traceProgress
        : 0.25 + ((Math.sin(this.pulseTime * 4.4) + 1) * 0.5) * 0.5;
    const sparkPoint = pointAlongPolyline(points, sparkProgress);

    this.sparkTrail.clear();
    if (!sparkPoint) {
      return;
    }

    const auraRadius = 24 + Math.sin(this.pulseTime * 8.2) * 3.2;
    this.sparkTrail.circle(sparkPoint.x, sparkPoint.y, auraRadius);
    this.sparkTrail.fill({ color: style.spark, alpha: visibility * 0.12 });
    this.sparkTrail.circle(sparkPoint.x, sparkPoint.y, 10);
    this.sparkTrail.fill({ color: style.spark, alpha: visibility * 0.92 });
    this.sparkTrail.circle(sparkPoint.x, sparkPoint.y, 4);
    this.sparkTrail.fill({ color: style.core, alpha: visibility * 0.98 });
  }

  private redrawCallout(style: PaylineOverlayStyle, visibility: number): void {
    if (!this.activeLine) {
      return;
    }

    const points = this.activeLine.points;
    const centerPoint = points[Math.floor(points.length * 0.5)] ?? points[0];
    const minY = Math.min(...points.map((point) => point.y));
    const maxX = Math.max(...points.map((point) => point.x));
    const minX = Math.min(...points.map((point) => point.x));
    const halfWidth = PaylineOverlay.CALLOUT_WIDTH * 0.5;
    const minCenter = minX + halfWidth - 24;
    const maxCenter = maxX - halfWidth + 24;
    const preferredX =
      minCenter <= maxCenter
        ? Math.max(minCenter, Math.min(maxCenter, centerPoint.x))
        : centerPoint.x;
    const preferredY = Math.max(60, minY - 80);
    const symbolKey =
      SYMBOL_KEY_BY_ID.get(this.activeLine.symbolId) ?? `SYMBOL ${this.activeLine.symbolId}`;
    const paylineName =
      PAYLINE_NAME_BY_ID.get(this.activeLine.lineId) ?? `LINE ${this.activeLine.lineId}`;
    const hasPlateTexture = this.plateSprite.visible;
    const hasBadgeTexture = this.badgeSprite.visible;
    const hasSequenceTexture = this.sequenceSprite.visible && this.sequenceCount > 1;
    const hasMultiplierTexture = this.multiplierSprite.visible;

    this.calloutShadow.clear();
    this.calloutShadow.roundRect(
      -halfWidth + 4,
      -(PaylineOverlay.CALLOUT_HEIGHT * 0.5) + 6,
      PaylineOverlay.CALLOUT_WIDTH,
      PaylineOverlay.CALLOUT_HEIGHT,
      26,
    );
    this.calloutShadow.fill({ color: 0x070102, alpha: visibility * 0.38 });

    this.calloutBase.clear();
    this.calloutBase.roundRect(
      -halfWidth,
      -(PaylineOverlay.CALLOUT_HEIGHT * 0.5),
      PaylineOverlay.CALLOUT_WIDTH,
      PaylineOverlay.CALLOUT_HEIGHT,
      26,
    );
    this.calloutBase.fill({
      color: style.panelFill,
      alpha: visibility * (hasPlateTexture ? 0.54 : 0.96),
    });
    this.calloutBase.stroke({
      color: style.panelStroke,
      width: 4,
      alpha: visibility * (hasPlateTexture ? 0.48 : 0.98),
    });

    this.calloutAccent.clear();
    this.calloutAccent.roundRect(
      -halfWidth + 8,
      -(PaylineOverlay.CALLOUT_HEIGHT * 0.5) + 8,
      PaylineOverlay.CALLOUT_WIDTH - 16,
      22,
      18,
    );
    this.calloutAccent.fill({ color: style.accentFill, alpha: visibility * 0.9 });

    this.lineBadge.clear();
    this.lineBadge.circle(-halfWidth + 36, 6, 24);
    this.lineBadge.fill({
      color: style.badgeFill,
      alpha: visibility * (hasBadgeTexture ? 0.42 : 0.96),
    });
    this.lineBadge.stroke({
      color: style.badgeStroke,
      width: 3,
      alpha: visibility * (hasBadgeTexture ? 0.5 : 0.98),
    });

    this.sequenceBadge.clear();
    if (this.sequenceCount > 1) {
      this.sequenceBadge.roundRect(halfWidth - 76, -36, 58, 26, 14);
      this.sequenceBadge.fill({
        color: style.sequenceFill,
        alpha: visibility * (hasSequenceTexture ? 0.4 : 0.94),
      });
      this.sequenceBadge.stroke({
        color: style.badgeStroke,
        width: 2,
        alpha: visibility * (hasSequenceTexture ? 0.45 : 0.98),
      });
    }

    this.callout.x = preferredX;
    this.callout.y = preferredY;
    this.callout.alpha = visibility;
    this.callout.scale.set(0.96 + easeOutCubic(clamp01(this.displayElapsedMs / 180)) * 0.04);

    this.layoutTexturedSprites(style, visibility, halfWidth);

    this.featureTag.style.fill = style.detailFill;
    this.lineBadgeText.style.fill = style.multiplierFill;
    this.sequenceText.style.fill = style.detailFill;
    this.multiplierText.style.fill = style.multiplierFill;
    this.payoutText.style.fill = style.payoutFill;
    this.detailText.style.fill = style.detailFill;

    this.featureTag.text = style.featureLabel;
    this.featureTag.y = -35;

    this.lineBadgeText.text = `L${this.activeLine.lineId}`;
    this.lineBadgeText.x = -halfWidth + 36;
    this.lineBadgeText.y = 6;

    this.sequenceText.visible = this.sequenceCount > 1;
    this.sequenceText.text = `${this.sequenceIndex + 1}/${this.sequenceCount}`;
    this.sequenceText.x = halfWidth - 47;
    this.sequenceText.y = -23;

    this.multiplierText.text = `x${formatMultiplier(this.activeLine.multiplier)}`;
    this.multiplierText.x = hasMultiplierTexture ? -4 : -18;
    this.multiplierText.y = 4;

    this.payoutText.text = `PAY ${formatCurrencyMinor(this.activeLine.amountMinor)}`;
    this.payoutText.x = halfWidth - 22;
    this.payoutText.y = 4;

    this.detailText.text = `${symbolKey}  •  ${paylineName}`;
    this.detailText.x = 12;
    this.detailText.y = 22;
  }

  private layoutTexturedSprites(
    style: PaylineOverlayStyle,
    visibility: number,
    halfWidth: number,
  ): void {
    this.plateSprite.alpha = visibility * 0.84;
    this.plateSprite.width =
      PaylineOverlay.CALLOUT_WIDTH + (this.activeTone === "boost" || this.activeTone === "jackpot" ? 22 : 0);
    this.plateSprite.height = PaylineOverlay.CALLOUT_HEIGHT + 10;
    this.plateSprite.x = 0;
    this.plateSprite.y = 0;
    this.plateSprite.tint = 0xffffff;

    this.accentSprite.visible = Boolean(this.textureState.accent);
    this.accentSprite.alpha = visibility * 0.94;
    this.accentSprite.width = PaylineOverlay.CALLOUT_WIDTH - 52;
    this.accentSprite.height =
      this.activeTone === "boost" ? 34 : this.activeTone === "jackpot" ? 28 : 24;
    this.accentSprite.x = 0;
    this.accentSprite.y = this.activeTone === "boost" ? -18 : -30;
    this.accentSprite.tint = 0xffffff;

    this.badgeSprite.alpha = visibility * 0.98;
    this.badgeSprite.width = 64;
    this.badgeSprite.height = 42;
    this.badgeSprite.x = -halfWidth + 36;
    this.badgeSprite.y = 6;
    this.badgeSprite.tint = 0xffffff;

    this.sequenceSprite.alpha = visibility * 0.88;
    this.sequenceSprite.width = 66;
    this.sequenceSprite.height = 28;
    this.sequenceSprite.x = halfWidth - 47;
    this.sequenceSprite.y = -23;
    this.sequenceSprite.tint = style.sequenceFill;

    this.multiplierSprite.alpha = visibility * 0.96;
    this.multiplierSprite.width = 50;
    this.multiplierSprite.height = 50;
    this.multiplierSprite.x = -84;
    this.multiplierSprite.y = 3;
    this.multiplierSprite.tint = 0xffffff;
  }

  private resolveVisibilityAlpha(): number {
    const intro = easeOutCubic(clamp01(this.displayElapsedMs / 170));
    const outroStart = this.displayDurationMs * 0.8;
    const outro =
      this.displayElapsedMs <= outroStart
        ? 1
        : 1 - clamp01((this.displayElapsedMs - outroStart) / Math.max(1, this.displayDurationMs - outroStart));
    return intro * outro;
  }

  private drawPolyline(
    graphics: Graphics,
    points: PaylineOverlayPoint[],
    width: number,
    color: number,
    alpha: number,
    progress: number,
  ): void {
    if (points.length === 0 || progress <= 0 || alpha <= 0) {
      return;
    }

    const clampedProgress = clamp01(progress);
    const totalLength = computePolylineLength(points);
    const targetLength = totalLength * clampedProgress;
    let traversed = 0;

    graphics.moveTo(points[0].x, points[0].y);
    for (let index = 1; index < points.length; index += 1) {
      const start = points[index - 1];
      const end = points[index];
      const segmentLength = distance(start, end);
      if (targetLength >= traversed + segmentLength) {
        graphics.lineTo(end.x, end.y);
        traversed += segmentLength;
        continue;
      }

      const remaining = Math.max(0, targetLength - traversed);
      const ratio = segmentLength === 0 ? 0 : remaining / segmentLength;
      graphics.lineTo(
        start.x + (end.x - start.x) * ratio,
        start.y + (end.y - start.y) * ratio,
      );
      break;
    }

    graphics.stroke({
      width,
      color,
      alpha,
      cap: "round",
      join: "round",
      alignment: 0.5,
    });
  }

  private async refreshTextures(): Promise<void> {
    if (!this.activeLine) {
      return;
    }

    const requestToken = ++this.textureRequestToken;
    const line = this.activeLine;
    const tone = this.activeTone;
    const accentPromise =
      tone === "boost"
        ? resolveProviderFrameTexture("heroUiAtlas", "boost-banner")
        : Promise.resolve({ texture: null, resolvedProvider: null, fallbackUsed: false });
    const multiplierKey = resolveMultiplierCoinKey(line.multiplier);
    const [plate, accent, sequence, badge, multiplier] = await Promise.all([
      resolveProviderFrameTexture("uiAtlas", "payline-plate"),
      accentPromise,
      resolveProviderFrameTexture("uiAtlas", "payline-sequence-chip"),
      resolveProviderFrameTexture("uiAtlas", "payline-badge"),
      resolveProviderFrameTexture("symbolAtlas", multiplierKey),
    ]);

    if (requestToken !== this.textureRequestToken || !this.activeLine) {
      return;
    }

    this.textureState = {
      plate: plate.texture,
      accent: accent.texture ?? null,
      sequence: sequence.texture,
      badge: badge.texture,
      multiplier: multiplier.texture,
    };
    this.applyTexture(this.plateSprite, this.textureState.plate);
    this.applyTexture(this.accentSprite, this.textureState.accent);
    this.applyTexture(this.sequenceSprite, this.textureState.sequence);
    this.applyTexture(this.badgeSprite, this.textureState.badge);
    this.applyTexture(this.multiplierSprite, this.textureState.multiplier);
    this.redraw();
  }

  private applyTexture(sprite: Sprite, texture: Texture | null): void {
    sprite.texture = texture ?? Texture.WHITE;
    sprite.visible = Boolean(texture);
  }

  private resolveStyle(
    styleHook: string,
    tone: PaylineOverlayTone,
  ): PaylineOverlayStyle {
    let style: PaylineOverlayStyle;

    switch (tone) {
      case "collect":
        style = {
          featureLabel: "COLLECT PAY",
          glow: 0xefaa1f,
          support: 0x5f2405,
          core: 0xfff5d4,
          spark: 0xfff4c8,
          nodeFill: 0xffd75d,
          panelFill: 0x382006,
          panelStroke: 0xffdd8e,
          accentFill: 0x9b5a12,
          badgeFill: 0x67310d,
          badgeStroke: 0xffd88f,
          sequenceFill: 0x7a4f12,
          multiplierFill: 0xfff4d0,
          payoutFill: 0xfff8da,
          detailFill: 0xffefcb,
        };
        break;
      case "boost":
        style = {
          featureLabel: "BOOST STRIKE",
          glow: 0xff5634,
          support: 0x4b0d09,
          core: 0xfff6e0,
          spark: 0xffffff,
          nodeFill: 0xffc05a,
          panelFill: 0x3b0908,
          panelStroke: 0xffd6a2,
          accentFill: 0xb61f14,
          badgeFill: 0x6e1510,
          badgeStroke: 0xffd2a1,
          sequenceFill: 0x8e2b19,
          multiplierFill: 0xfff5e0,
          payoutFill: 0xffe8b2,
          detailFill: 0xffedd0,
        };
        break;
      case "bonus":
        style = {
          featureLabel: "BONUS ENTRY",
          glow: 0xff9a31,
          support: 0x4d1708,
          core: 0xfff2d0,
          spark: 0xfff8e0,
          nodeFill: 0xf3c768,
          panelFill: 0x3a1507,
          panelStroke: 0xffd08a,
          accentFill: 0x8c3f14,
          badgeFill: 0x6a2d0e,
          badgeStroke: 0xffd18d,
          sequenceFill: 0x7f4315,
          multiplierFill: 0xfff0d2,
          payoutFill: 0xfff7d2,
          detailFill: 0xffedcd,
        };
        break;
      case "jackpot":
        style = {
          featureLabel: "JACKPOT RUN",
          glow: 0xffd24f,
          support: 0x5c2206,
          core: 0xfffbef,
          spark: 0xffffff,
          nodeFill: 0xffe282,
          panelFill: 0x3c1506,
          panelStroke: 0xffefba,
          accentFill: 0xaf7a19,
          badgeFill: 0x734a0a,
          badgeStroke: 0xffebb5,
          sequenceFill: 0x95640f,
          multiplierFill: 0xfff8ea,
          payoutFill: 0xffffff,
          detailFill: 0xfff0c8,
        };
        break;
      case "standard":
      default:
        style = {
          featureLabel: "WINNING LINE",
          glow: 0xc7141a,
          support: 0x42090d,
          core: 0xfff1b8,
          spark: 0xfff8d6,
          nodeFill: 0xffcb57,
          panelFill: 0x33070b,
          panelStroke: 0xf7da9b,
          accentFill: 0x891117,
          badgeFill: 0x5d1014,
          badgeStroke: 0xf1d38e,
          sequenceFill: 0x6f161b,
          multiplierFill: 0xfff3d1,
          payoutFill: 0xffefbc,
          detailFill: 0xffeacc,
        };
        break;
    }

    switch (styleHook) {
      case "intense":
        style.glow = 0xff7f55;
        style.core = 0xffffff;
        style.spark = 0xffffff;
        style.panelStroke = 0xfff2c9;
        break;
      case "neon":
        style.glow = tone === "jackpot" ? 0xffde7d : style.glow;
        style.core = tone === "boost" ? 0xfff9ea : style.core;
        break;
      case "subtle":
      default:
        break;
    }

    return style;
  }
}

const clamp01 = (value: number): number => Math.max(0, Math.min(1, value));

const easeOutCubic = (value: number): number => 1 - Math.pow(1 - clamp01(value), 3);

const distance = (a: PaylineOverlayPoint, b: PaylineOverlayPoint): number =>
  Math.hypot(b.x - a.x, b.y - a.y);

const computePolylineLength = (points: PaylineOverlayPoint[]): number =>
  points.slice(1).reduce((total, point, index) => total + distance(points[index], point), 0);

const pointAlongPolyline = (
  points: PaylineOverlayPoint[],
  progress: number,
): PaylineOverlayPoint | null => {
  if (points.length === 0) {
    return null;
  }
  if (points.length === 1) {
    return points[0];
  }

  const clampedProgress = clamp01(progress);
  const totalLength = computePolylineLength(points);
  const targetLength = totalLength * clampedProgress;
  let traversed = 0;

  for (let index = 1; index < points.length; index += 1) {
    const start = points[index - 1];
    const end = points[index];
    const segmentLength = distance(start, end);
    if (targetLength <= traversed + segmentLength) {
      const remaining = Math.max(0, targetLength - traversed);
      const ratio = segmentLength === 0 ? 0 : remaining / segmentLength;
      return {
        x: start.x + (end.x - start.x) * ratio,
        y: start.y + (end.y - start.y) * ratio,
      };
    }
    traversed += segmentLength;
  }

  return points[points.length - 1] ?? null;
};

const formatMultiplier = (value: number): string => {
  const rounded = Math.round(value * 100) / 100;
  return Number.isInteger(rounded)
    ? `${rounded}`
    : rounded.toFixed(2).replace(/0+$/, "").replace(/\.$/, "");
};

const formatCurrencyMinor = (valueMinor: number): string => `$${(valueMinor / 100).toFixed(2)}`;

const resolveMultiplierCoinKey = (multiplier: number): string => {
  if (multiplier >= 10) {
    return "coin-multiplier-10x";
  }
  if (multiplier >= 5) {
    return "coin-multiplier-5x";
  }
  if (multiplier >= 3) {
    return "coin-multiplier-3x";
  }
  return "coin-multiplier-2x";
};
