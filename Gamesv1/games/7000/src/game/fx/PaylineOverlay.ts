import { Container, Graphics, Text, Ticker } from "pixi.js";

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

export class PaylineOverlay extends Container {
  private static readonly CALLOUT_WIDTH = 304;
  private static readonly CALLOUT_HEIGHT = 74;

  private readonly glowPath = new Graphics();
  private readonly tracePath = new Graphics();
  private readonly nodes = new Graphics();
  private readonly callout = new Container();
  private readonly calloutBase = new Graphics();
  private readonly calloutAccent = new Graphics();
  private readonly calloutTitle = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 19,
      fontWeight: "900",
      fill: 0xfff1a8,
      stroke: { color: 0x1a0406, width: 5 },
      letterSpacing: 1,
      align: "center",
    },
  });
  private readonly calloutValue = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 26,
      fontWeight: "900",
      fill: 0xffffff,
      stroke: { color: 0x220307, width: 5 },
      letterSpacing: 0.5,
      align: "center",
    },
  });

  private activeLine: PaylineOverlayLine | null = null;
  private traceProgress = 0;
  private pulseTime = 0;
  private traceDurationMs = 260;
  private currentStyleHook = "";

  constructor() {
    super();

    this.calloutTitle.anchor.set(0.5);
    this.calloutValue.anchor.set(0.5);
    this.callout.addChild(this.calloutBase, this.calloutAccent, this.calloutTitle, this.calloutValue);
    this.callout.visible = false;

    this.addChild(this.glowPath, this.tracePath, this.nodes, this.callout);

    Ticker.shared.add((ticker) => {
      if (!this.activeLine) {
        return;
      }

      this.traceProgress = Math.min(1, this.traceProgress + ticker.deltaMS / this.traceDurationMs);
      this.pulseTime += ticker.deltaMS / 1000;
      this.redraw();
    });
  }

  public showLine(
    line: PaylineOverlayLine,
    styleHook?: string,
    durationMs = 760,
  ): void {
    this.activeLine = line;
    this.currentStyleHook = styleHook ?? "";
    this.traceProgress = 0;
    this.pulseTime = 0;
    this.traceDurationMs = Math.max(180, Math.min(420, durationMs * 0.36));
    this.callout.visible = true;
    this.redraw();
  }

  public clear(): void {
    this.activeLine = null;
    this.traceProgress = 0;
    this.pulseTime = 0;
    this.glowPath.clear();
    this.tracePath.clear();
    this.nodes.clear();
    this.callout.visible = false;
  }

  private redraw(): void {
    if (!this.activeLine) {
      this.clear();
      return;
    }

    const style = this.resolveStyle(this.currentStyleHook);
    const points = this.activeLine.points;
    const pulseAlpha = 0.78 + Math.sin(this.pulseTime * 8) * 0.14;

    this.glowPath.clear();
    this.drawPolyline(this.glowPath, points, 18, style.glow, 0.28 + pulseAlpha * 0.22, 1);

    this.tracePath.clear();
    this.drawPolyline(this.tracePath, points, 8, style.core, 0.92, this.traceProgress);

    this.nodes.clear();
    points.forEach((point, index) => {
      const radius = index === 1 ? 16 : 13;
      this.nodes.circle(point.x, point.y, radius);
      this.nodes.fill({ color: style.nodeFill, alpha: 0.2 + pulseAlpha * 0.2 });
      this.nodes.circle(point.x, point.y, radius - 5);
      this.nodes.fill({ color: style.core, alpha: 0.9 });
    });

    this.redrawCallout(style);
  }

  private redrawCallout(style: ReturnType<PaylineOverlay["resolveStyle"]>): void {
    if (!this.activeLine) {
      return;
    }

    const points = this.activeLine.points;
    const centerPoint = points[Math.floor(points.length * 0.5)] ?? points[0];
    const minY = Math.min(...points.map((point) => point.y));
    const maxX = Math.max(...points.map((point) => point.x));
    const minX = Math.min(...points.map((point) => point.x));
    const halfWidth = PaylineOverlay.CALLOUT_WIDTH * 0.5;
    const preferredX = Math.max(minX + halfWidth, Math.min(maxX - halfWidth, centerPoint.x));
    const preferredY = Math.max(44, minY - 68);
    const symbolKey = SYMBOL_KEY_BY_ID.get(this.activeLine.symbolId) ?? `SYMBOL ${this.activeLine.symbolId}`;
    const paylineName = PAYLINE_NAME_BY_ID.get(this.activeLine.lineId);

    this.calloutBase.clear();
    this.calloutBase.roundRect(
      -halfWidth,
      -(PaylineOverlay.CALLOUT_HEIGHT * 0.5),
      PaylineOverlay.CALLOUT_WIDTH,
      PaylineOverlay.CALLOUT_HEIGHT,
      26,
    );
    this.calloutBase.fill({ color: style.panelFill, alpha: 0.94 });
    this.calloutBase.stroke({ color: style.panelStroke, width: 4, alpha: 0.96 });

    this.calloutAccent.clear();
    this.calloutAccent.roundRect(
      -(PaylineOverlay.CALLOUT_WIDTH * 0.5) + 6,
      -(PaylineOverlay.CALLOUT_HEIGHT * 0.5) + 6,
      PaylineOverlay.CALLOUT_WIDTH - 12,
      24,
      18,
    );
    this.calloutAccent.fill({ color: style.accentFill, alpha: 0.92 });

    this.callout.x = preferredX;
    this.callout.y = preferredY;
    this.callout.alpha = 0.92 + Math.sin(this.pulseTime * 7.5) * 0.08;
    this.callout.scale.set(0.98 + Math.sin(this.pulseTime * 6) * 0.02);

    this.calloutTitle.text = paylineName
      ? `LINE ${this.activeLine.lineId}  ${paylineName}`
      : `LINE ${this.activeLine.lineId}  ${symbolKey}`;
    this.calloutValue.text = `${symbolKey}  x${formatMultiplier(this.activeLine.multiplier)}  PAY ${formatCurrencyMinor(
      this.activeLine.amountMinor,
    )}`;
    this.calloutTitle.y = -15;
    this.calloutValue.y = 15;
  }

  private drawPolyline(
    graphics: Graphics,
    points: PaylineOverlayPoint[],
    width: number,
    color: number,
    alpha: number,
    progress: number,
  ): void {
    if (points.length === 0 || progress <= 0) {
      return;
    }

    const clampedProgress = Math.max(0, Math.min(1, progress));
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

  private resolveStyle(styleHook?: string): {
    glow: number;
    core: number;
    nodeFill: number;
    panelFill: number;
    panelStroke: number;
    accentFill: number;
  } {
    switch (styleHook) {
      case "subtle":
        return {
          glow: 0xa9151c,
          core: 0xfff6d1,
          nodeFill: 0xf9ca57,
          panelFill: 0x31090d,
          panelStroke: 0xf7d87c,
          accentFill: 0x6c1117,
        };
      case "intense":
        return {
          glow: 0xff3b57,
          core: 0xffffff,
          nodeFill: 0xffca63,
          panelFill: 0x46050d,
          panelStroke: 0xfff0a6,
          accentFill: 0xb01522,
        };
      case "neon":
      default:
        return {
          glow: 0xc7141a,
          core: 0xfff1a8,
          nodeFill: 0xffcb57,
          panelFill: 0x35070b,
          panelStroke: 0xfde09b,
          accentFill: 0x8e1116,
        };
    }
  }
}

const distance = (a: PaylineOverlayPoint, b: PaylineOverlayPoint): number =>
  Math.hypot(b.x - a.x, b.y - a.y);

const computePolylineLength = (points: PaylineOverlayPoint[]): number =>
  points.slice(1).reduce((total, point, index) => total + distance(points[index], point), 0);

const formatMultiplier = (value: number): string => {
  const rounded = Math.round(value * 100) / 100;
  return Number.isInteger(rounded) ? `${rounded}` : rounded.toFixed(2).replace(/0+$/, "").replace(/\.$/, "");
};

const formatCurrencyMinor = (valueMinor: number): string => `$${(valueMinor / 100).toFixed(2)}`;
