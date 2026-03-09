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
  private readonly cabinetBase = new Graphics();
  private readonly cabinetGlow = new Graphics();
  private readonly reelPanels: Sprite[] = [];
  private isSpinning = false;
  private elapsed = 0;

  public onSpinComplete: () => void = () => {};

  constructor(assetRoot: string) {
    super();

    const machineWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const machineHeight =
      CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
      (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing;

    this.redrawCabinet(machineWidth, machineHeight);
    this.addChild(this.cabinetBase, this.cabinetGlow);

    for (let index = 0; index < CRAZY_ROOSTER_LAYOUT.reelCount; index += 1) {
      const panel = new Sprite(Texture.WHITE);
      panel.width = CRAZY_ROOSTER_LAYOUT.symbolWidth;
      panel.height = machineHeight;
      panel.alpha = 0.96;
      panel.x = index * (CRAZY_ROOSTER_LAYOUT.symbolWidth + CRAZY_ROOSTER_LAYOUT.reelSpacing);
      this.reelPanels.push(panel);
      this.addChild(panel);

      const reel = new CrazyRoosterReel(index, assetRoot);
      reel.x = index * (CRAZY_ROOSTER_LAYOUT.symbolWidth + CRAZY_ROOSTER_LAYOUT.reelSpacing);
      this.reels.push(reel);
      this.addChild(reel);
    }

    const mask = new Graphics();
    mask.roundRect(
      -8,
      -8,
      machineWidth + 16,
      machineHeight + 16,
      26,
    );
    mask.fill(0xffffff);
    this.addChild(mask);
    this.mask = mask;
    void this.applyReelFrameTexture();

    this.ticker.add((ticker) => {
      this.elapsed += ticker.deltaMS / 1000;
      this.cabinetGlow.alpha = 0.48 + Math.sin(this.elapsed * 2.1) * 0.12;
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
    const resolvedTexture = resolved.texture;
    if (resolvedTexture) {
      this.reelPanels.forEach((panel) => {
        panel.texture = resolvedTexture;
        panel.tint = 0xffffff;
      });
      return;
    }

    this.reelPanels.forEach((panel) => {
      panel.texture = Texture.WHITE;
      panel.tint = 0x3f0a10;
    });
  }

  private redrawCabinet(machineWidth: number, machineHeight: number): void {
    this.cabinetBase.clear();
    this.cabinetBase.roundRect(-10, -10, machineWidth + 20, machineHeight + 20, 28);
    this.cabinetBase.fill({ color: 0x0d0406, alpha: 0.94 });
    this.cabinetBase.stroke({ color: 0xffd88a, width: 4, alpha: 0.82 });

    this.cabinetGlow.clear();
    this.cabinetGlow.roundRect(-16, -14, machineWidth + 32, machineHeight + 28, 34);
    this.cabinetGlow.stroke({ color: 0xc7141a, width: 8, alpha: 0.44 });
  }
}
