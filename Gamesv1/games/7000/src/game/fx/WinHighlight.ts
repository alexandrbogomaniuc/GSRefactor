import { Container, Graphics, Ticker } from "pixi.js";

import { CRAZY_ROOSTER_LAYOUT } from "../config/CrazyRoosterGameConfig";

type SymbolLike = {
  getGlobalPosition: () => { x: number; y: number };
};

export class WinHighlight extends Container {
  private overlays: Graphics[] = [];
  private pulseActive = false;
  private pulseTime = 0;

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
    });
  }

  public showWin(symbols: SymbolLike[], styleHook?: string): void {
    this.clear();
    if (!this.parent) {
      return;
    }

    this.pulseActive = true;
    this.pulseTime = 0;
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
    this.pulseActive = false;
    this.overlays.forEach((overlay) => overlay.destroy());
    this.overlays = [];
    this.removeChildren();
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
