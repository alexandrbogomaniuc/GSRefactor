import { Container, Text, Ticker } from "pixi.js";

export class DebugOverlay extends Container {
    private fpsText: Text;
    private timeSum: number = 0;
    private frames: number = 0;

    constructor() {
        super();

        this.fpsText = new Text({
            text: "FPS: --",
            style: {
                fontFamily: "monospace",
                fontSize: 16,
                fill: 0x00ff00,
                stroke: { color: 0x000000, width: 3 },
            },
        });
        this.addChild(this.fpsText);

        Ticker.shared.add(this.update, this);
    }

    private update(ticker: Ticker) {
        this.frames++;
        this.timeSum += ticker.deltaMS;

        if (this.timeSum >= 1000) {
            const fps = Math.round((this.frames * 1000) / this.timeSum);
            this.fpsText.text = `FPS: ${fps}\nObjects: -- (Not available in v8 natively)\nScale: ${window.devicePixelRatio}`;

            this.frames = 0;
            this.timeSum = 0;
        }
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public resize(width: number, _height: number) {
        this.x = width - this.width - 20; // Top right
        this.y = 20;
    }
}
