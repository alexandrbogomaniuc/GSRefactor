import { Container, Graphics, Ticker } from "pixi.js";
import { GameConfig, SlotSymbol } from "@gamesv1/ui-kit";

export class WinHighlight extends Container {
  private overlays: Graphics[] = [];

  private pulseActive = false;
  private pulseTime = 0;

  private resolveStyle(styleHook?: string): {
    stroke: number;
    fill: number;
    alpha: number;
  } {
    switch (styleHook) {
      case "subtle":
        return {
          stroke: 0xffffff,
          fill: 0x90a4c6,
          alpha: 0.28,
        };
      case "neon":
        return {
          stroke: 0x35f7ff,
          fill: 0x00b7c2,
          alpha: 0.36,
        };
      case "intense":
        return {
          stroke: 0xff5fb2,
          fill: 0xff2f92,
          alpha: 0.42,
        };
      default:
        return {
          stroke: 0xffffff,
          fill: 0xffd700,
          alpha: 0.4,
        };
    }
  }

  constructor() {
    super();
    Ticker.shared.add((time) => {
      if (!this.pulseActive) return;
      this.pulseTime += time.deltaMS / 1000;
      const alpha = 0.5 + Math.sin(this.pulseTime * 10) * 0.3;
      this.overlays.forEach((g) => (g.alpha = alpha));
    });
  }

  public showWin(symbols: SlotSymbol[], styleHook?: string) {
    this.clear();
    if (!this.parent) return;

    this.pulseActive = true;
    this.pulseTime = 0;
    const style = this.resolveStyle(styleHook);

    symbols.forEach((sym) => {
      const glow = new Graphics();
      glow.roundRect(0, 0, GameConfig.symbolWidth, GameConfig.symbolHeight, 16);

      glow.stroke({ width: 8, color: style.stroke, alignment: 0.5 });
      glow.fill({ color: style.fill, alpha: style.alpha });

      const point = sym.getGlobalPosition();
      const localPoint = this.parent!.toLocal(point);

      glow.x = localPoint.x;
      glow.y = localPoint.y;

      this.addChild(glow);
      this.overlays.push(glow);
    });
  }

  public clear() {
    this.pulseActive = false;
    this.overlays.forEach((g) => g.destroy());
    this.overlays = [];
    this.removeChildren();
  }
}
