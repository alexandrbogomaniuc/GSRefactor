import { Container, Graphics, Ticker } from "pixi.js";
import { GameConfig, SlotSymbol } from "@gamesv1/ui-kit";

export class WinHighlight extends Container {
  private overlays: Graphics[] = [];

  private pulseActive = false;
  private pulseTime = 0;

  constructor() {
    super();
    Ticker.shared.add((time) => {
      if (!this.pulseActive) return;
      this.pulseTime += time.deltaMS / 1000;
      const alpha = 0.5 + Math.sin(this.pulseTime * 10) * 0.3;
      this.overlays.forEach((g) => (g.alpha = alpha));
    });
  }

  public showWin(symbols: SlotSymbol[]) {
    this.clear();
    if (!this.parent) return;

    this.pulseActive = true;
    this.pulseTime = 0;

    symbols.forEach((sym) => {
      const glow = new Graphics();
      glow.roundRect(0, 0, GameConfig.symbolWidth, GameConfig.symbolHeight, 16);

      glow.stroke({ width: 8, color: 0xffffff, alignment: 0.5 });
      glow.fill({ color: 0xffd700, alpha: 0.4 });

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
