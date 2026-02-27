import * as PIXI from 'pixi.js';

export class Reel {
    public container: PIXI.Container;
    private symbolHeight: number = 100;
    private symbols: PIXI.Text[] = [];
    private visibleSymbols: number = 3;
    private isSpinning: boolean = false;

    // Abstracting visual state
    private targetResult: number[] = [];

    constructor(xOffset: number) {
        this.container = new PIXI.Container();
        this.container.x = xOffset;
        this.container.y = 50;

        // Build generic placeholder symbols
        for (let i = 0; i < this.visibleSymbols + 1; i++) {
            const sym = this.createSymbol(Math.floor(Math.random() * 5) + 1);
            sym.y = i * this.symbolHeight;
            this.symbols.push(sym);
            this.container.addChild(sym);
        }

        // Add a mask to hide symbols scrolling off-screen
        const mask = new PIXI.Graphics();
        mask.beginFill(0xffffff);
        mask.drawRect(xOffset, 50, 100, this.symbolHeight * this.visibleSymbols);
        mask.endFill();
        this.container.mask = mask;
    }

    private createSymbol(id: number): PIXI.Text {
        const text = new PIXI.Text(`Sym ${id}`, {
            fontSize: 24,
            fill: 0xffffff,
            align: 'center',
            fontWeight: 'bold',
        });
        text.anchor.set(0.5);
        text.x = 50;
        return text;
    }

    public startSpin() {
        this.isSpinning = true;
        this.targetResult = [];
    }

    public stopSpin(resultColumn: number[]) {
        this.targetResult = resultColumn;
        // In a real game, this would trigger an easing function.
        // For the template, we just snap the symbols to the target result to prove logic integration.
        setTimeout(() => {
            this.isSpinning = false;
            for (let i = 0; i < this.visibleSymbols; i++) {
                this.symbols[i].text = `Sym ${this.targetResult[i]}`;
            }
        }, 500); // Artificial visual delay
    }

    public update(_delta: number) {
        if (!this.isSpinning) return;

        // Simple blur/scroll effect
        const speed = 25;
        this.symbols.forEach(sym => {
            sym.y += speed;
            if (sym.y >= this.symbolHeight * this.visibleSymbols) {
                sym.y -= this.symbolHeight * (this.visibleSymbols + 1);
                sym.text = `Sym ${Math.floor(Math.random() * 5) + 1}`; // Randomize during blur
            }
        });
    }
}
