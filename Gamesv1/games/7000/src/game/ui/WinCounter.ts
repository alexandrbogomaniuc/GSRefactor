import { Container, Text, Ticker } from "pixi.js";

export class WinCounter extends Container {
  private countText: Text;
  private targetValue: number = 0;
  private currentValue: number = 0;
  private reportedTotalValue: number = 0;
  private isCounting: boolean = false;
  private countDuration: number = 1.2; // seconds
  private title: string = "WIN";
  private currentStyleHook = "";

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

  private applyStyleHook(styleHook?: string) {
    this.currentStyleHook = styleHook ?? "";

    const style = this.countText.style;
    style.fill = 0xffd700;
    style.stroke = { color: 0x000000, width: 6 };

    switch (this.currentStyleHook) {
      case "subtle":
        style.fill = 0xf7f2da;
        style.stroke = { color: 0x2b2b2b, width: 4 };
        break;
      case "neon":
        style.fill = 0x35f7ff;
        style.stroke = { color: 0x041018, width: 7 };
        break;
      case "intense":
        style.fill = 0xff5fb2;
        style.stroke = { color: 0x240010, width: 8 };
        break;
      default:
        break;
    }
  }

  public showWin(amount: number, title = "WIN", styleHook?: string) {
    this.title = title;
    this.reportWin(amount);
    this.currentValue = 0;
    this.isCounting = true;
    this.visible = true;
    this.applyStyleHook(styleHook);

    this.countText.text = `${this.title}\n$0`;

    // Simple scale pop
    this.scale.set(0.1);
  }

  public reportWin(amount: number) {
    const normalized = Math.max(0, Math.round(amount));
    this.reportedTotalValue = normalized;
    this.targetValue = normalized;
    if (!this.isCounting) {
      this.currentValue = normalized;
    }
  }

  public getReportedTotal(): number {
    return this.reportedTotalValue;
  }

  public hideNow() {
    this.isCounting = false;
    this.visible = false;
    this.currentValue = this.reportedTotalValue;
    this.targetValue = this.reportedTotalValue;
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
    }

    this.countText.text = `${this.title}\n$${Math.floor(this.currentValue)}`;
  }
}
