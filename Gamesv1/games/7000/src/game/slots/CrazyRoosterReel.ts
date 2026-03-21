import { Container } from "pixi.js";

import { CRAZY_ROOSTER_LAYOUT } from "../config/CrazyRoosterGameConfig";
import { CrazyRoosterSymbol, type DonorMultiplierVariantKey } from "./CrazyRoosterSymbol";

const symbolStep =
  CRAZY_ROOSTER_LAYOUT.symbolHeight + CRAZY_ROOSTER_LAYOUT.rowSpacing;

const DEFAULT_REEL_MOTION = {
  startSpeed: 520,
  maxSpeed: 3560,
  settleSpeed: 820,
  acceleration: 6800,
  deceleration: 7600,
  settleBounceDurationMs: 320,
  settleBounceAmplitude: 34,
  settleBounceFrequency: 1.45,
  symbolSwayAmplitude: 0,
  symbolSwayFrequency: 5.2,
} as const;

export class CrazyRoosterReel extends Container {
  private readonly reelContainer = new Container();
  private readonly symbols: CrazyRoosterSymbol[] = [];
  private speed = 0;
  private positionY = 0;
  private settleBounceMs = 0;
  private settleBounceDurationMs = DEFAULT_REEL_MOTION.settleBounceDurationMs;
  private pendingResult: number[] | null = null;
  private pendingVariants: Array<DonorMultiplierVariantKey | null> | null = null;
  private nextGeneratedSymbolId = 0;
  private reelTimeSeconds = 0;

  public isSpinning = false;
  public onStopSettled: (reelId: number) => void = () => {};

  constructor(
    public readonly id: number,
    _assetRoot?: string,
  ) {
    super();

    this.addChild(this.reelContainer);
    this.nextGeneratedSymbolId = id % CRAZY_ROOSTER_LAYOUT.symbolCount;

    const totalSymbols =
      CRAZY_ROOSTER_LAYOUT.rowCount + CRAZY_ROOSTER_LAYOUT.extraSymbols;
    for (let index = 0; index < totalSymbols; index += 1) {
      const symbol = new CrazyRoosterSymbol();
      symbol.setSymbol(this.consumeGeneratedSymbolId());
      symbol.y = (index - 1) * symbolStep;
      this.symbols.push(symbol);
      this.reelContainer.addChild(symbol);
    }
  }

  public spin(speedMultiplier = 1): void {
    this.isSpinning = true;
    this.speed = DEFAULT_REEL_MOTION.startSpeed;
    this.positionY = 0;
    this.pendingResult = null;
    this.pendingVariants = null;
    this.nextGeneratedSymbolId = (this.id + 1) % CRAZY_ROOSTER_LAYOUT.symbolCount;
    this.maxSpeed = DEFAULT_REEL_MOTION.maxSpeed * Math.max(0.74, speedMultiplier);
    this.settleBounceDurationMs = DEFAULT_REEL_MOTION.settleBounceDurationMs;
    this.reelTimeSeconds = 0;
    for (const symbol of this.symbols) {
      symbol.setDonorVariantOverride(null);
    }
  }

  public stop(column: number[], variants: Array<DonorMultiplierVariantKey | null> = []): void {
    this.pendingResult = [...column];
    this.pendingVariants = [...variants];
  }

  public tick(deltaSeconds: number): void {
    this.reelTimeSeconds += deltaSeconds;
    if (!this.isSpinning) {
      if (this.settleBounceMs > 0) {
        this.settleBounceMs = Math.max(0, this.settleBounceMs - deltaSeconds * 1000);
        this.updateSymbolPositions();
      }
      return;
    }

    if (this.speed < this.maxSpeed && this.pendingResult === null) {
      this.speed += DEFAULT_REEL_MOTION.acceleration * deltaSeconds;
      if (this.speed > this.maxSpeed) {
        this.speed = this.maxSpeed;
      }
    }

    if (this.pendingResult !== null) {
      const settleSpeed = DEFAULT_REEL_MOTION.settleSpeed;
      if (this.speed > settleSpeed) {
        this.speed -= DEFAULT_REEL_MOTION.deceleration * deltaSeconds;
        if (this.speed < settleSpeed) {
          this.speed = settleSpeed;
        }
      }
    }

    this.positionY += this.speed * deltaSeconds;

    if (this.positionY >= symbolStep) {
      this.positionY -= symbolStep;
      const recycled = this.symbols.pop();
      if (!recycled) {
        return;
      }

      if (this.pendingResult && this.pendingResult.length > 0) {
        const variant =
          this.pendingVariants && this.pendingVariants.length > 0
            ? this.pendingVariants.pop() ?? null
            : null;
        recycled.setDonorVariantOverride(variant);
        recycled.setSymbol(this.pendingResult.pop() ?? 0);
      } else {
        recycled.setDonorVariantOverride(null);
        recycled.setSymbol(this.consumeGeneratedSymbolId());
      }

      this.symbols.unshift(recycled);

      if (this.pendingResult && this.pendingResult.length === 0) {
        this.isSpinning = false;
        this.positionY = 0;
        this.settleBounceMs = this.settleBounceDurationMs;
        this.pendingVariants = null;
        this.updateSymbolPositions();
        this.onStopSettled(this.id);
        return;
      }
    }

    this.updateSymbolPositions();
  }

  public applyColumn(column: number[]): void {
    this.applyColumnWithVariants(column, []);
  }

  public applyColumnWithVariants(
    column: number[],
    variants: Array<DonorMultiplierVariantKey | null>,
  ): void {
    const safeColumn = Array.from({ length: CRAZY_ROOSTER_LAYOUT.rowCount }, (_, index) =>
      column[index] ?? 0,
    );
    const extended = [
      safeColumn[safeColumn.length - 1] ?? 0,
      ...safeColumn,
      safeColumn[0] ?? 0,
    ];
    const safeVariants = Array.from({ length: CRAZY_ROOSTER_LAYOUT.rowCount }, (_, index) =>
      variants[index] ?? null,
    );
    const extendedVariants = [
      safeVariants[safeVariants.length - 1] ?? null,
      ...safeVariants,
      safeVariants[0] ?? null,
    ];

    for (let index = 0; index < this.symbols.length; index += 1) {
      this.symbols[index].setDonorVariantOverride(extendedVariants[index] ?? null);
      this.symbols[index].setSymbol(extended[index] ?? 0);
    }

    this.positionY = 0;
    this.settleBounceMs = 0;
    this.pendingResult = null;
    this.pendingVariants = null;
    this.isSpinning = false;
    this.updateSymbolPositions();
  }

  public getVisibleSymbols(): CrazyRoosterSymbol[] {
    return this.symbols.slice(1, CRAZY_ROOSTER_LAYOUT.rowCount + 1);
  }

  private maxSpeed: number = DEFAULT_REEL_MOTION.maxSpeed;

  private consumeGeneratedSymbolId(): number {
    const current = this.nextGeneratedSymbolId;
    this.nextGeneratedSymbolId =
      (this.nextGeneratedSymbolId + 1) % CRAZY_ROOSTER_LAYOUT.symbolCount;
    return current;
  }

  private updateSymbolPositions(): void {
    let bounceOffset = 0;
    if (this.settleBounceMs > 0) {
      const progress = 1 - this.settleBounceMs / this.settleBounceDurationMs;
      bounceOffset =
        Math.sin(progress * Math.PI * DEFAULT_REEL_MOTION.settleBounceFrequency) *
        (1 - progress) *
        DEFAULT_REEL_MOTION.settleBounceAmplitude;
    }
    for (let index = 0; index < this.symbols.length; index += 1) {
      const sway =
        DEFAULT_REEL_MOTION.symbolSwayAmplitude > 0
          ? Math.sin(
              this.reelTimeSeconds * DEFAULT_REEL_MOTION.symbolSwayFrequency +
                index * 0.7 +
                this.id * 0.2,
            ) * DEFAULT_REEL_MOTION.symbolSwayAmplitude
          : 0;
      this.symbols[index].x = sway;
      this.symbols[index].y = (index - 1) * symbolStep + this.positionY + bounceOffset;
    }
  }
}
