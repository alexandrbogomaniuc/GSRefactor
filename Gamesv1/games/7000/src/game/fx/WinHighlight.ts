import { Container, Graphics, Ticker } from "pixi.js";

import { CRAZY_ROOSTER_LAYOUT } from "../config/CrazyRoosterGameConfig";

type SymbolLike = {
  getGlobalPosition: () => { x: number; y: number };
};

type MotionTargetLike = {
  x: number;
  y: number;
  scale: {
    x: number;
    y: number;
    set: (x: number, y?: number) => void;
  };
};

type ActiveSymbolMotion = {
  target: MotionTargetLike;
  baseX: number;
  baseY: number;
  baseScaleX: number;
  baseScaleY: number;
  phase: number;
};

export type WinHighlightMotionProfile = {
  amplitudePx: number;
  liftPx: number;
  scalePulse: number;
  frequencyHz: number;
  phaseStep: number;
};

export class WinHighlight extends Container {
  private overlays: Graphics[] = [];
  private pulseActive = false;
  private pulseTime = 0;
  private motionProfile: WinHighlightMotionProfile | null = null;
  private symbolMotions: ActiveSymbolMotion[] = [];

  constructor() {
    super();

    Ticker.shared.add((time) => {
      if (!this.pulseActive) {
        return;
      }
      this.pulseTime += time.deltaMS / 1000;
      const alpha = 0.48 + Math.sin(this.pulseTime * 9) * 0.24;
      this.overlays.forEach((overlay) => {
        overlay.alpha = alpha;
      });
      this.tickSymbolMotion();
    });
  }

  public showWin(
    symbols: SymbolLike[],
    styleHook?: string,
    motionProfile?: WinHighlightMotionProfile | null,
  ): void {
    this.clear();
    if (!this.parent) {
      return;
    }

    this.pulseActive = true;
    this.pulseTime = 0;
    this.motionProfile = motionProfile ?? null;
    this.symbolMotions = this.captureSymbolMotions(symbols);
    const style = this.resolveStyle(styleHook);

    symbols.forEach((symbol) => {
      const point = symbol.getGlobalPosition();
      const localPoint = this.parent!.toLocal(point);
      const overlay = new Graphics();
      overlay.roundRect(
        0,
        0,
        CRAZY_ROOSTER_LAYOUT.symbolWidth,
        CRAZY_ROOSTER_LAYOUT.symbolHeight,
        18,
      );
      overlay.stroke({ width: 7, color: style.stroke, alignment: 0.5 });
      overlay.fill({ color: style.fill, alpha: style.alpha });
      overlay.x = localPoint.x;
      overlay.y = localPoint.y;
      this.addChild(overlay);
      this.overlays.push(overlay);
    });
  }

  public clear(): void {
    this.restoreSymbolMotions();
    this.pulseActive = false;
    this.motionProfile = null;
    this.overlays.forEach((overlay) => overlay.destroy());
    this.overlays = [];
    this.removeChildren();
  }

  private tickSymbolMotion(): void {
    if (!this.motionProfile || this.symbolMotions.length === 0) {
      return;
    }

    const angularFrequency = this.motionProfile.frequencyHz * Math.PI * 2;
    this.symbolMotions.forEach((motion) => {
      const phaseTime = this.pulseTime * angularFrequency + motion.phase;
      const wave = Math.sin(phaseTime);
      const liftWave = Math.max(0, Math.sin(phaseTime * 0.9 + 0.7));
      motion.target.x = motion.baseX + wave * this.motionProfile!.amplitudePx;
      motion.target.y = motion.baseY - liftWave * this.motionProfile!.liftPx;
      const scaleOffset = wave * this.motionProfile!.scalePulse;
      motion.target.scale.set(
        motion.baseScaleX + scaleOffset,
        motion.baseScaleY + scaleOffset,
      );
    });
  }

  private captureSymbolMotions(symbols: SymbolLike[]): ActiveSymbolMotion[] {
    if (!this.motionProfile) {
      return [];
    }

    return symbols
      .map((symbol, index) => {
        const target = this.resolveMotionTarget(symbol);
        if (!target) {
          return null;
        }

        return {
          target,
          baseX: target.x,
          baseY: target.y,
          baseScaleX: target.scale.x,
          baseScaleY: target.scale.y,
          phase: index * this.motionProfile!.phaseStep,
        };
      })
      .filter((entry): entry is ActiveSymbolMotion => Boolean(entry));
  }

  private restoreSymbolMotions(): void {
    this.symbolMotions.forEach((motion) => {
      motion.target.x = motion.baseX;
      motion.target.y = motion.baseY;
      motion.target.scale.set(motion.baseScaleX, motion.baseScaleY);
    });
    this.symbolMotions = [];
  }

  private resolveMotionTarget(symbol: SymbolLike): MotionTargetLike | null {
    const candidate = symbol as unknown as Partial<MotionTargetLike>;
    if (typeof candidate.x !== "number" || typeof candidate.y !== "number") {
      return null;
    }
    if (
      !candidate.scale ||
      typeof candidate.scale.x !== "number" ||
      typeof candidate.scale.y !== "number" ||
      typeof candidate.scale.set !== "function"
    ) {
      return null;
    }
    return candidate as MotionTargetLike;
  }

  private resolveStyle(styleHook?: string): {
    stroke: number;
    fill: number;
    alpha: number;
  } {
    switch (styleHook) {
      case "subtle":
        return { stroke: 0xffffff, fill: 0x6a1118, alpha: 0.24 };
      case "neon":
        return { stroke: 0xfff1a8, fill: 0xc7141a, alpha: 0.34 };
      case "intense":
        return { stroke: 0xffffff, fill: 0xc7141a, alpha: 0.44 };
      default:
        return { stroke: 0xffffff, fill: 0x9d1017, alpha: 0.3 };
    }
  }
}
