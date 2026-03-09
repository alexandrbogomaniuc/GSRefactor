import { Container, Graphics, Sprite, Texture, Ticker } from "pixi.js";

import { CRAZY_ROOSTER_LAYOUT } from "../config/CrazyRoosterGameConfig";
import { CrazyRoosterReel } from "./CrazyRoosterReel";
import { resolveProviderFrameTexture } from "../../app/assets/providerPackRegistry";

export interface CrazyRoosterSpinOptions {
  minSpinDurationMs?: number;
  spinStaggerMs?: number;
  speedMultiplier?: number;
  reelStopColumns: number[][];
}

export class CrazyRoosterSlotMachine extends Container {
  private readonly reels: CrazyRoosterReel[] = [];
  private readonly ticker = new Ticker();
  private readonly reelFrame = new Sprite(Texture.WHITE);
  private isSpinning = false;

  public onSpinComplete: () => void = () => {};

  constructor(assetRoot: string) {
    super();

    const machineWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const machineHeight =
      CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
      (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing;

    this.reelFrame.width = machineWidth;
    this.reelFrame.height = machineHeight;
    this.reelFrame.alpha = 0.9;
    this.reelFrame.tint = 0x3f0a10;
    this.addChild(this.reelFrame);

    for (let index = 0; index < CRAZY_ROOSTER_LAYOUT.reelCount; index += 1) {
      const reel = new CrazyRoosterReel(index, assetRoot);
      reel.x = index * (CRAZY_ROOSTER_LAYOUT.symbolWidth + CRAZY_ROOSTER_LAYOUT.reelSpacing);
      this.reels.push(reel);
      this.addChild(reel);
    }

    const mask = new Graphics();
    mask.roundRect(
      0,
      0,
      machineWidth,
      machineHeight,
      18,
    );
    mask.fill(0xffffff);
    this.addChild(mask);
    this.mask = mask;
    void this.applyReelFrameTexture();

    this.ticker.add((ticker) => {
      if (!this.isSpinning) {
        return;
      }
      const deltaSeconds = ticker.deltaMS / 1000;
      this.reels.forEach((reel) => reel.tick(deltaSeconds));
    });
    this.ticker.start();
  }

  public setPresentationColumns(columns: number[][]): void {
    this.reels.forEach((reel, index) => {
      reel.applyColumn(columns[index] ?? []);
    });
  }

  public spin(options: CrazyRoosterSpinOptions): void {
    if (this.isSpinning) {
      return;
    }

    if (options.reelStopColumns.length !== CRAZY_ROOSTER_LAYOUT.reelCount) {
      throw new Error("CrazyRoosterSlotMachine.spin requires a stop column for every reel.");
    }

    this.isSpinning = true;
    const speedMultiplier = options.speedMultiplier ?? 1;
    const minSpinDurationMs = options.minSpinDurationMs ?? CRAZY_ROOSTER_LAYOUT.minSpinMs;
    const spinStaggerMs = options.spinStaggerMs ?? CRAZY_ROOSTER_LAYOUT.spinStaggerMs;

    this.reels.forEach((reel) => reel.spin(speedMultiplier));

    window.setTimeout(() => {
      this.reels.forEach((reel, index) => {
        window.setTimeout(() => {
          reel.stop(options.reelStopColumns[index] ?? []);
        }, index * spinStaggerMs);
      });

      window.setTimeout(() => {
        this.isSpinning = false;
        this.onSpinComplete();
      }, (this.reels.length - 1) * spinStaggerMs + CRAZY_ROOSTER_LAYOUT.stopDelayMs);
    }, minSpinDurationMs);
  }

  public getReels(): CrazyRoosterReel[] {
    return this.reels;
  }

  private async applyReelFrameTexture(): Promise<void> {
    const resolved = await resolveProviderFrameTexture("uiAtlas", "reel-frame-panel");
    if (resolved.texture) {
      this.reelFrame.texture = resolved.texture;
      this.reelFrame.tint = 0xffffff;
      return;
    }

    this.reelFrame.texture = Texture.WHITE;
    this.reelFrame.tint = 0x3f0a10;
  }
}
