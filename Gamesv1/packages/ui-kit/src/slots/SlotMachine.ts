import { Container, Graphics, Ticker } from "pixi.js";
import { GameConfig } from "../config/GameConfig.ts";
import { Reel } from "./Reel.ts";

export interface SpinOptions {
  minSpinDurationMs?: number;
  spinStaggerMs?: number;
  speedMultiplier?: number;
  reelStopColumns: number[][];
}

export class SlotMachine extends Container {
  private reels: Reel[] = [];
  private isSpinning: boolean = false;
  private ticker: Ticker;

  // Callbacks
  public onSpinComplete: () => void = () => {};

  constructor() {
    super();

    for (let i = 0; i < GameConfig.numReels; i++) {
      const reel = new Reel(i);
      reel.x = i * (GameConfig.symbolWidth + GameConfig.reelSpacing);
      this.addChild(reel);
      this.reels.push(reel);
    }

    // ONE mask for the entire reel matrix (Massive mobile performance save over 5 masks)
    const boundsMask = new Graphics();
    const totalWidth =
      GameConfig.numReels * GameConfig.symbolWidth +
      (GameConfig.numReels - 1) * GameConfig.reelSpacing;
    const totalHeight =
      GameConfig.numRows * GameConfig.symbolHeight +
      (GameConfig.numRows - 1) * GameConfig.rowSpacing;

    boundsMask.rect(0, 0, totalWidth, totalHeight);
    boundsMask.fill(0xffffff);
    this.addChild(boundsMask);
    this.mask = boundsMask;

    this.ticker = new Ticker();
    this.ticker.add((time) => this.tick(time.deltaTime, time.deltaMS / 1000));
    this.ticker.start();
  }

  public spin(options: SpinOptions) {
    if (this.isSpinning) return;
    if (
      !options.reelStopColumns ||
      options.reelStopColumns.length !== this.reels.length
    ) {
      throw new Error(
        "SlotMachine.spin requires reelStopColumns from server presentation payload.",
      );
    }
    for (let index = 0; index < options.reelStopColumns.length; index += 1) {
      const column = options.reelStopColumns[index];
      if (!column || column.length < GameConfig.numRows) {
        throw new Error(
          `Invalid reel stop column at index ${index}. Expected at least ${GameConfig.numRows} symbols.`,
        );
      }
    }
    this.isSpinning = true;

    const minSpinDurationMs =
      options.minSpinDurationMs ??
      Math.round(GameConfig.minSpinDuration * 1000);
    const speedMultiplier = options.speedMultiplier ?? 1;

    this.reels.forEach((reel) => reel.spin(speedMultiplier));

    // Mock a server wait time then stop
    setTimeout(() => this.stop(options), minSpinDurationMs);
  }

  public stop(options: SpinOptions) {
    const spinStaggerMs =
      options.spinStaggerMs ?? Math.round(GameConfig.spinStagger * 1000);

    // Send stop commands with stagger
    this.reels.forEach((reel, index) => {
      setTimeout(() => {
        const resolvedColumn = options.reelStopColumns[index];
        reel.stop(resolvedColumn.slice(0, GameConfig.numRows));
      }, index * spinStaggerMs);
    });

    // Calculate total time until last reel stops (roughly)
    const totalStopDelay = (this.reels.length - 1) * spinStaggerMs + 500;
    setTimeout(() => {
      this.isSpinning = false;
      this.onSpinComplete();
    }, totalStopDelay);
  }

  private tick(deltaTime: number, deltaSeconds: number) {
    if (!this.isSpinning) return;
    this.reels.forEach((reel) => reel.tick(deltaTime, deltaSeconds));
  }

  public getReels(): Reel[] {
    return this.reels;
  }
}
