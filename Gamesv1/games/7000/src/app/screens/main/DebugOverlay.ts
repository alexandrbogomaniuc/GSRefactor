import { Container, Graphics, Text, Ticker } from "pixi.js";

type MathBridgeSummary = {
  lineIds: number[];
  lineMultipliers: number[];
  totalWinMultiplier: number | null;
};

const readParams = (): URLSearchParams => new URLSearchParams(window.location.search);

const showFpsOverlay = (): boolean => readParams().get("debugOverlay") === "1";

const showMathOverlay = (): boolean => {
  const params = readParams();
  if (params.get("mathOverlay") === "0") {
    return false;
  }
  if (params.get("mathOverlay") === "1") {
    return true;
  }
  return params.get("mathSource") === "provisional" || params.has("mathPreset");
};

export class DebugOverlay extends Container {
  private readonly fpsText = new Text({
    text: "FPS: --",
    style: {
      fontFamily: "monospace",
      fontSize: 16,
      fill: 0x00ff66,
      stroke: { color: 0x000000, width: 3 },
    },
  });
  private readonly mathPanel = new Graphics();
  private readonly mathText = new Text({
    text: "",
    style: {
      fontFamily: "monospace",
      fontSize: 14,
      fill: 0xffefb8,
      stroke: { color: 0x130204, width: 3 },
      letterSpacing: 0.4,
      lineHeight: 18,
    },
  });
  private readonly showFps = showFpsOverlay();
  private readonly showMath = showMathOverlay();
  private timeSum = 0;
  private frames = 0;

  constructor() {
    super();

    this.visible = this.showFps || this.showMath;
    this.fpsText.visible = this.showFps;
    this.mathPanel.visible = this.showMath;
    this.mathText.visible = this.showMath;

    this.addChild(this.mathPanel, this.mathText, this.fpsText);
    this.setMathBridgeSummary(null);

    if (this.showFps) {
      Ticker.shared.add(this.update, this);
    }
  }

  public setMathBridgeSummary(summary: MathBridgeSummary | null): void {
    if (!this.showMath) {
      return;
    }

    const lineIds = summary?.lineIds.length ? summary.lineIds.join(", ") : "-";
    const multipliers = summary?.lineMultipliers.length
      ? summary.lineMultipliers.map((value) => `x${formatNumber(value)}`).join(", ")
      : "-";
    const totalWinMultiplier =
      summary?.totalWinMultiplier !== null && summary?.totalWinMultiplier !== undefined
        ? `x${formatNumber(summary.totalWinMultiplier)}`
        : "-";

    this.mathText.text =
      `MATH BRIDGE\n` +
      `LINES: ${lineIds}\n` +
      `LINE MULTS: ${multipliers}\n` +
      `TOTAL MULT: ${totalWinMultiplier}`;

    this.redrawMathPanel();
  }

  private redrawMathPanel(): void {
    this.mathPanel.clear();
    if (!this.showMath) {
      return;
    }

    this.mathPanel.roundRect(0, 0, this.mathText.width + 20, this.mathText.height + 18, 16);
    this.mathPanel.fill({ color: 0x1a0406, alpha: 0.86 });
    this.mathPanel.stroke({ color: 0xc7141a, width: 2, alpha: 0.9 });
    this.mathText.x = this.mathPanel.x + 10;
    this.mathText.y = this.mathPanel.y + 9;
  }

  private update(ticker: Ticker): void {
    this.frames += 1;
    this.timeSum += ticker.deltaMS;

    if (this.timeSum >= 1000) {
      const fps = Math.round((this.frames * 1000) / this.timeSum);
      this.fpsText.text =
        `FPS: ${fps}\n` +
        `Objects: -- (Not available in v8 natively)\n` +
        `Scale: ${window.devicePixelRatio}`;

      this.frames = 0;
      this.timeSum = 0;
    }
  }

  public resize(width: number, _height: number): void {
    this.fpsText.x = width - this.fpsText.width - 20;
    this.fpsText.y = 20;

    if (this.showMath) {
      this.mathPanel.x = 20;
      this.mathPanel.y = 20;
      this.mathText.x = this.mathPanel.x + 10;
      this.mathText.y = this.mathPanel.y + 9;
    }
  }
}

const formatNumber = (value: number): string => {
  const rounded = Math.round(value * 100) / 100;
  return Number.isInteger(rounded)
    ? `${rounded}`
    : rounded.toFixed(2).replace(/0+$/, "").replace(/\.$/, "");
};
