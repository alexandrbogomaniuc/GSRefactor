import { Container, Graphics, Ticker } from "pixi.js";
import { GameConfig } from "../config/GameConfig";
import { Symbol } from "../slots/Symbol";

export class WinHighlight extends Container {
  private overlays: Graphics[] = [];

  // Animate glowing
  private pulseActive: boolean = false;
  private pulseTime: number = 0;

  constructor() {
    super();
    Ticker.shared.add((time) => {
      if (!this.pulseActive) return;
      this.pulseTime += time.deltaMS / 1000;
      const alpha = 0.5 + Math.sin(this.pulseTime * 10) * 0.3; // Rapid pulse
      this.overlays.forEach((g) => (g.alpha = alpha));
    });
  }

  public showWin(symbols: Symbol[]) {
    this.clear();
    if (!this.parent) return;

    this.pulseActive = true;
    this.pulseTime = 0;

    symbols.forEach((sym) => {
      const glow = new Graphics();
      glow.roundRect(0, 0, GameConfig.symbolWidth, GameConfig.symbolHeight, 16);

      // "Premium" glow effect mimicking CSS box shadow
      glow.stroke({ width: 8, color: 0xffffff, alignment: 0.5 });
      glow.fill({ color: 0xffd700, alpha: 0.4 });

      // Transform directly to the symbol's global position relative to the slot machine.
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
