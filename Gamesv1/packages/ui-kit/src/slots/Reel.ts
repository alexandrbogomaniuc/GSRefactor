import { Container } from "pixi.js";
import { GameConfig } from "../config/GameConfig.ts";
import { SlotSymbol } from "./SlotSymbol.ts";

export class Reel extends Container {
  public id: number;
  private symbolList: SlotSymbol[] = [];
  private reelContainer = new Container();

  // State
  public isSpinning: boolean = false;
  private positionY: number = 0;
  private speed: number = 0;
  private targetResult: number[] | null = null;
  private nextGeneratedSymbolId: number;

  // Timers
  private baseEaseInSpeed: number = 3000; // pixels per sec
  private currentMaxSpeed: number = 3000;

  constructor(id: number) {
    super();
    this.id = id;
    this.nextGeneratedSymbolId = id % GameConfig.symbolCount;
    this.addChild(this.reelContainer);

    // Initialize symbols (grid rows + extra buffers for wrap-around)
    const totalSymbols = GameConfig.numRows + GameConfig.extraSymbols;
    for (let i = 0; i < totalSymbols; i++) {
      const sym = new SlotSymbol();
      sym.setSymbol(this.consumeGeneratedSymbolId());
      sym.y = this.getSymbolY(i);
      this.symbolList.push(sym);
      this.reelContainer.addChild(sym);
    }
  }

  private getSymbolY(index: number): number {
    return index * (GameConfig.symbolHeight + GameConfig.rowSpacing);
  }

  public spin(speedMultiplier = 1) {
    this.isSpinning = true;
    this.speed = 0;
    this.targetResult = null;
    this.nextGeneratedSymbolId = (this.id + 1) % GameConfig.symbolCount;
    this.currentMaxSpeed =
      this.baseEaseInSpeed * Math.max(0.5, speedMultiplier);
  }

  public stop(result: number[]) {
    // Queue the result. Logic picks it up on wrap around.
    this.targetResult = result;
  }

  public tick(_dt: number, timeSeconds: number) {
    if (!this.isSpinning) return;

    // Accelerate
    if (this.speed < this.currentMaxSpeed && this.targetResult === null) {
      this.speed += 5000 * timeSeconds;
      if (this.speed > this.currentMaxSpeed) this.speed = this.currentMaxSpeed;
    }

    // Move symbols down
    this.positionY += this.speed * timeSeconds;

    const symbolTotalHeight = GameConfig.symbolHeight + GameConfig.rowSpacing;

    // Wrap around logic
    if (this.positionY >= symbolTotalHeight) {
      this.positionY -= symbolTotalHeight;

      // Move bottom-most symbol to top
      const bottomSym = this.symbolList.pop()!;

      // If we have a target result and we are about to fill the visible rows!
      if (this.targetResult && this.targetResult.length > 0) {
        // Populate the top with the final results
        const nextId = this.targetResult.pop()!;
        bottomSym.setSymbol(nextId);
      } else {
        bottomSym.setSymbol(this.consumeGeneratedSymbolId());
      }

      this.symbolList.unshift(bottomSym);

      // Check if we finished placing target results
      if (this.targetResult && this.targetResult.length === 0) {
        // Snap and bounce!
        this.isSpinning = false;
        this.positionY = 0; // Perfect align // In a real system, we'd trigger a bounce tween
        this.updateSymbolPositions();
        return;
      }
    }

    this.updateSymbolPositions();
  }

  private consumeGeneratedSymbolId(): number {
    const value = this.nextGeneratedSymbolId;
    this.nextGeneratedSymbolId =
      (this.nextGeneratedSymbolId + 1) % GameConfig.symbolCount;
    return value;
  }

  private updateSymbolPositions() {
    const symbolTotalHeight = GameConfig.symbolHeight + GameConfig.rowSpacing;
    // The first symbol (index 0) is logically 'above' the view, so -1.
    const startIndex = -1;

    for (let i = 0; i < this.symbolList.length; i++) {
      const sym = this.symbolList[i];
      sym.y = (startIndex + i) * symbolTotalHeight + this.positionY;
    }
  }

  // Give access to the visible symbols for FX overlays
  public getVisibleSymbols(): SlotSymbol[] {
    // Index 0 is above screen, 1...numRows are visible.
    return this.symbolList.slice(1, GameConfig.numRows + 1);
  }
}
