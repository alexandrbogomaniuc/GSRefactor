import { Container, Text, Ticker } from "pixi.js";

export class WinCounter extends Container {
  private countText: Text;
  private targetValue: number = 0;
  private currentValue: number = 0;
  private isCounting: boolean = false;
  private countDuration: number = 2; // seconds

  constructor() {
    super();
    this.countText = new Text({
      text: "",
      style: {
        fontFamily: "Arial",
        fontSize: 64,
        fill: 0xffd700,
        stroke: { color: 0x000, width: 6 },
        fontWeight: "900",
      },
    });
    this.countText.anchor.set(0.5);
    this.addChild(this.countText);

    Ticker.shared.add((time) => this.tick(time.deltaMS / 1000));

    this.visible = false;
  }

  public showWin(amount: number) {
    this.targetValue = amount;
    this.currentValue = 0;
    this.isCounting = true;
    this.visible = true;

    this.countText.text = "MEGA WIN\n0";

    // Simple scale pop
    this.scale.set(0.1);

    // Very basic mock tween directly in the ticker since we aren't pulling in GSAP logic here
  }

  private tick(dt: number) {
    if (!this.isCounting) return;

    // Pop in scale
    if (this.scale.x < 1) {
      this.scale.x += 5 * dt;
      this.scale.y += 5 * dt;
      if (this.scale.x > 1) this.scale.set(1);
    }

    // Count up
    this.currentValue += (this.targetValue / this.countDuration) * dt;

    if (this.currentValue >= this.targetValue) {
      this.currentValue = this.targetValue;
      this.isCounting = false;

      // Auto hide after 2 seconds
      setTimeout(() => {
        this.visible = false;
      }, 2000);
    }

    this.countText.text = `MEGA WIN\n$Math.floor(this.currentValue)`;
  }
}
