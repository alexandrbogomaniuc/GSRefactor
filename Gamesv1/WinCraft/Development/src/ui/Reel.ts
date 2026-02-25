import * as PIXI from 'pixi.js';

export class Reel {
    public container: PIXI.Container;
    private symbolHeight: number = 130;
    private symbolWidth: number = 130;
    private symbols: PIXI.Container[] = [];
    private visibleSymbols: number = 5;
    private isSpinning: boolean = false;

    // Abstracting visual state
    private targetResult: number[] = [];

    constructor(xOffset: number) {
        this.container = new PIXI.Container();
        this.container.x = xOffset;
        this.container.y = 150; // Offset down to leave room for TopInventory

        // Build placeholder/sprite symbols
        for (let i = 0; i < this.visibleSymbols + 2; i++) {
            const sym = this.createSymbol(Math.floor(Math.random() * 5) + 1);
            sym.y = i * this.symbolHeight;
            this.symbols.push(sym);
            this.container.addChild(sym);
        }

        // Add a mask to hide symbols scrolling off-screen
        const mask = new PIXI.Graphics();
        mask.beginFill(0xffffff);
        mask.drawRect(xOffset, 150, this.symbolWidth, this.symbolHeight * this.visibleSymbols);
        mask.endFill();
        this.container.mask = mask;
    }

    private createSymbol(id: number): PIXI.Container {
        const cont = new PIXI.Container();

        let sprite: PIXI.Sprite | PIXI.Graphics;

        try {
            // Try to load loaded assets
            if (id === 1) sprite = PIXI.Sprite.from('/assets/dirt.png');
            else if (id === 2) sprite = PIXI.Sprite.from('/assets/stone.png');
            else {
                // Fallback
                sprite = new PIXI.Graphics();
                (sprite as PIXI.Graphics).beginFill(id * 0x333333 + 0x228811);
                (sprite as PIXI.Graphics).drawRoundedRect(-60, -60, 120, 120, 10);
                (sprite as PIXI.Graphics).endFill();
            }
        } catch {
            sprite = new PIXI.Graphics();
            (sprite as PIXI.Graphics).beginFill(id * 0x333333 + 0x228811);
            (sprite as PIXI.Graphics).drawRoundedRect(-60, -60, 120, 120, 10);
            (sprite as PIXI.Graphics).endFill();
        }

        // Add text label for clarity during development
        const text = new PIXI.Text(`Sym ${id}`, {
            fontSize: 24,
            fill: 0xffffff,
            align: 'center',
            fontWeight: 'bold',
        });
        text.anchor.set(0.5);

        // If it's a sprite, we need to anchor it center
        if (sprite instanceof PIXI.Sprite) {
            sprite.anchor.set(0.5);
            // Scale to fill 120x120 roughly
            sprite.width = 120;
            sprite.height = 120;
        }

        cont.addChild(sprite);
        cont.addChild(text);
        cont.x = this.symbolWidth / 2;
        return cont;
    }

    public startSpin() {
        this.isSpinning = true;
        this.targetResult = [];
    }

    public stopSpin(resultColumn: number[]) {
        this.targetResult = resultColumn;
        // In a cascading game, we wouldn't just snap. But for Phase 1 logic integration, snap it.
        setTimeout(() => {
            this.isSpinning = false;
            for (let i = 0; i < this.visibleSymbols; i++) {
                // We recreate or update the visual
                const newSym = this.createSymbol(this.targetResult[i]);
                newSym.y = i * this.symbolHeight;

                this.container.removeChild(this.symbols[i]);
                this.symbols[i] = newSym;
                this.container.addChild(newSym);
            }
        }, 800);
    }

    public update(_delta: number) {
        if (!this.isSpinning) return;

        // Simple blur/scroll downwards to simulate falling blocks
        const speed = 35;
        this.symbols.forEach((sym, index) => {
            sym.y += speed;
            if (sym.y >= this.symbolHeight * this.visibleSymbols) {
                sym.y -= this.symbolHeight * (this.visibleSymbols + 2);
                // Randomize during scroll
                const newId = Math.floor(Math.random() * 5) + 1;
                const newSym = this.createSymbol(newId);
                newSym.y = sym.y;

                this.container.removeChild(sym);
                this.symbols[index] = newSym;
                this.container.addChild(newSym);
            }
        });
    }
}
