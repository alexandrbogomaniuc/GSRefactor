import { Container } from "pixi.js";

import { CRAZY_ROOSTER_LAYOUT } from "../config/CrazyRoosterGameConfig";
import { CrazyRoosterSymbol } from "./CrazyRoosterSymbol";

const symbolStep =
  CRAZY_ROOSTER_LAYOUT.symbolHeight + CRAZY_ROOSTER_LAYOUT.rowSpacing;

export class CrazyRoosterReel extends Container {
  private readonly reelContainer = new Container();
  private readonly symbols: CrazyRoosterSymbol[] = [];
  private speed = 0;
  private positionY = 0;
  private pendingResult: number[] | null = null;
  private nextGeneratedSymbolId = 0;

  public isSpinning = false;
  public onStopComplete: (() => void) | null = null;

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
    this.speed = 0;
    this.positionY = 0;
    this.pendingResult = null;
    this.nextGeneratedSymbolId = (this.id + 1) % CRAZY_ROOSTER_LAYOUT.symbolCount;
    this.maxSpeed = 3200 * Math.max(0.65, speedMultiplier);
  }

  public stop(column: number[]): void {
    this.pendingResult = [...column];
  }

  public tick(deltaSeconds: number): void {
    if (!this.isSpinning) {
      return;
    }

    if (this.speed < this.maxSpeed && this.pendingResult === null) {
      this.speed += 5200 * deltaSeconds;
      if (this.speed > this.maxSpeed) {
        this.speed = this.maxSpeed;
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
        recycled.setSymbol(this.pendingResult.pop() ?? 0);
      } else {
        recycled.setSymbol(this.consumeGeneratedSymbolId());
      }

      this.symbols.unshift(recycled);

      if (this.pendingResult && this.pendingResult.length === 0) {
        this.isSpinning = false;
        this.positionY = 0;
        this.updateSymbolPositions();
        this.onStopComplete?.();
        return;
      }
    }

    this.updateSymbolPositions();
  }

  public applyColumn(column: number[]): void {
    const safeColumn = Array.from({ length: CRAZY_ROOSTER_LAYOUT.rowCount }, (_, index) =>
      column[index] ?? 0,
    );
    const extended = [
      safeColumn[safeColumn.length - 1] ?? 0,
      ...safeColumn,
      safeColumn[0] ?? 0,
    ];

    for (let index = 0; index < this.symbols.length; index += 1) {
      this.symbols[index].setSymbol(extended[index] ?? 0);
    }

    this.positionY = 0;
    this.pendingResult = null;
    this.isSpinning = false;
    this.updateSymbolPositions();
  }

  public getVisibleSymbols(): CrazyRoosterSymbol[] {
    return this.symbols.slice(1, CRAZY_ROOSTER_LAYOUT.rowCount + 1);
  }

  private maxSpeed = 3200;

  private consumeGeneratedSymbolId(): number {
    const current = this.nextGeneratedSymbolId;
    this.nextGeneratedSymbolId =
      (this.nextGeneratedSymbolId + 1) % CRAZY_ROOSTER_LAYOUT.symbolCount;
    return current;
  }

  private updateSymbolPositions(): void {
    for (let index = 0; index < this.symbols.length; index += 1) {
      this.symbols[index].y = (index - 1) * symbolStep + this.positionY;
    }
  }
}
