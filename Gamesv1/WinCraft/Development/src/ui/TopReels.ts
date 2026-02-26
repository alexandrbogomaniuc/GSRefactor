import * as PIXI from 'pixi.js';
import { GameConfig } from '../game/GameConfig';

export class TopReels {
    public container: PIXI.Container;
    private reelsContainer: PIXI.Container;
    private columns: PIXI.Container[] = [];
    private symbols: PIXI.Container[][] = []; // [col][row]
    private isSpinning: boolean = false;
    private isSpinningCol: boolean[] = [];

    constructor(startX: number, startY: number) {
        this.container = new PIXI.Container();
        this.container.x = startX;
        this.container.y = startY;

        this.reelsContainer = new PIXI.Container();
        this.buildFrame();
        this.buildColumns();
    }

    private buildColumns() {
        for (let c = 0; c < GameConfig.TopSlotGrid.cols; c++) {
            const colContainer = new PIXI.Container();
            // Columns are positioned with exact padding
            colContainer.x = GameConfig.TopSlotGrid.paddingX + c * GameConfig.TopSlotGrid.symbolWidth;
            this.reelsContainer.addChild(colContainer);
            this.columns.push(colContainer);
            this.symbols[c] = [];

            // Pre-fill with random visuals for IDLE state
            for (let r = 0; r < GameConfig.TopSlotGrid.rows + 1; r++) {
                const randomToolId = Math.floor(Math.random() * 13) + 101;
                const sym = this.createSymbolSprite(randomToolId);
                sym.y = (r - 1) * GameConfig.TopSlotGrid.symbolHeight; // Local symbol offset
                colContainer.addChild(sym);
                this.symbols[c].push(sym);
            }
        }
    }

    private buildFrame() {
        const paddingX = GameConfig.TopSlotGrid.paddingX;
        const paddingY = GameConfig.TopSlotGrid.paddingY;
        const sw = GameConfig.TopSlotGrid.slotSize;
        const sh = GameConfig.TopSlotGrid.slotSize;

        // Exact 500x300 panel dimensions to match Mining Grid at 320 seamlessly
        const panelWidth = 500;
        const panelHeight = 300;

        const outerPanel = new PIXI.Graphics();

        // Outer UI black box
        outerPanel.beginFill(0x000000);
        outerPanel.drawRect(0, 0, panelWidth, panelHeight);
        outerPanel.endFill();

        // White border inner outline
        outerPanel.beginFill(0xFFFFFF);
        outerPanel.drawRect(2, 2, panelWidth - 4, panelHeight - 4);
        outerPanel.endFill();

        outerPanel.beginFill(0x555555);
        outerPanel.drawRect(6, 6, panelWidth - 8, panelHeight - 8);
        outerPanel.endFill();

        outerPanel.beginFill(0xC6C6C6);
        outerPanel.drawRect(6, 6, panelWidth - 12, panelHeight - 12);
        outerPanel.endFill();

        // Background dark slots
        outerPanel.beginFill(0x8B8B8B);
        for (let c = 0; c < GameConfig.TopSlotGrid.cols; c++) {
            for (let r = 0; r < GameConfig.TopSlotGrid.rows; r++) {
                const x = paddingX + c * GameConfig.TopSlotGrid.symbolWidth;
                const y = paddingY + r * GameConfig.TopSlotGrid.symbolHeight;
                outerPanel.drawRect(x, y, sw, sh);
            }
        }
        outerPanel.endFill();

        this.container.addChild(outerPanel);

        // --- Reel Mask and Container Offset --- 
        this.reelsContainer.x = 0;
        this.reelsContainer.y = paddingY;

        const mask = new PIXI.Graphics();
        mask.beginFill(0xFFFFFF);
        for (let c = 0; c < GameConfig.TopSlotGrid.cols; c++) {
            for (let r = 0; r < GameConfig.TopSlotGrid.rows; r++) {
                const x = paddingX + c * GameConfig.TopSlotGrid.symbolWidth;
                const y = r * GameConfig.TopSlotGrid.symbolHeight; // relative to reelsContainer
                mask.drawRect(x, y, sw, sh);
            }
        }
        mask.endFill();

        this.container.addChild(mask);
        this.reelsContainer.mask = mask;
        this.container.addChild(this.reelsContainer);

        // --- Draw Precise Grid Cell Borders ON TOP ---
        const fg = new PIXI.Graphics();

        for (let c = 0; c < GameConfig.TopSlotGrid.cols; c++) {
            for (let r = 0; r < GameConfig.TopSlotGrid.rows; r++) {
                const x = paddingX + c * GameConfig.TopSlotGrid.symbolWidth;
                const y = paddingY + r * GameConfig.TopSlotGrid.symbolHeight;

                // Dark top/left inner shadow (the Minecraft deep shadow volume)
                fg.beginFill(0x373737);
                fg.drawRect(x, y, sw, 4); // Top edge
                fg.drawRect(x, y + 4, 4, sh - 4); // Left edge, below top
                fg.endFill();

                // Bright bottom/right inner highlight (the Minecraft light volume)
                fg.beginFill(0xFFFFFF);
                fg.drawRect(x + 4, y + sh - 4, sw - 4, 4); // Bottom edge, right of left edge
                fg.drawRect(x + sw - 4, y + 4, 4, sh - 8); // Right edge, between top and bottom
                fg.endFill();
            }
        }
        this.container.addChild(fg);
    }

    private createSymbolSprite(id: number): PIXI.Container {
        const cell = new PIXI.Container();
        let tex = PIXI.Texture.EMPTY;

        try {
            const tool = Object.values(GameConfig.Tools).find((t: any) => t.id === id);
            if (tool && tool.texture) {
                tex = PIXI.Texture.from(`/assets/${tool.texture}`);
            }
        } catch { }

        const sprite = new PIXI.Sprite(tex);
        const internalPadding = Math.floor((GameConfig.TopSlotGrid.slotSize - 64) / 2);
        sprite.width = 64;
        sprite.height = 64;
        sprite.x = internalPadding;
        sprite.y = internalPadding;

        cell.addChild(sprite);
        return cell;
    }

    public startSpin() {
        this.isSpinning = true;
        this.isSpinningCol = Array(GameConfig.TopSlotGrid.cols).fill(true);
    }

    /**
     * Snap the reels to the server-provided deterministic result grid with staggered timing.
     */
    public stopSpin(slotResultIds: number[][]): Promise<void> {
        return new Promise((resolve) => {
            const staggerMs = 300; // Time gap between each reel stopping

            for (let c = 0; c < GameConfig.TopSlotGrid.cols; c++) {
                setTimeout(() => {
                    this.isSpinningCol[c] = false;

                    for (let r = 0; r < GameConfig.TopSlotGrid.rows + 1; r++) {
                        const targetSym = this.symbols[c][r];
                        targetSym.y = (r - 1) * GameConfig.TopSlotGrid.symbolHeight; // Snap y: -100, 0, 100, 200

                        if (r > 0) { // Only assign IDs to the 3 visible rows
                            const id = slotResultIds[r - 1][c];
                            const sprite = targetSym.children[0] as PIXI.Sprite;

                            try {
                                const tool = Object.values(GameConfig.Tools).find((t: any) => t.id === id);
                                if (tool && tool.texture) {
                                    sprite.texture = PIXI.Texture.from(`/assets/${tool.texture}`);
                                    sprite.tint = 0xFFFFFF; // Clear placeholder tint
                                } else {
                                    sprite.texture = PIXI.Texture.EMPTY;
                                }
                            } catch {
                                sprite.texture = PIXI.Texture.EMPTY;
                            }
                        }
                    }

                    // Resolve promise after the last column stops
                    if (c === GameConfig.TopSlotGrid.cols - 1) {
                        this.isSpinning = false;
                        resolve();
                    }
                }, c * staggerMs);
            }
        });
    }

    /**
     * Called by ticker. Creates fake rolling blur effect if spinning per column.
     */
    public update(deltaMS: number) {
        if (!this.isSpinning) return;

        const speed = 2; // px per ms
        const step = speed * deltaMS;

        for (let c = 0; c < GameConfig.TopSlotGrid.cols; c++) {
            if (!this.isSpinningCol[c]) continue; // Skip stopped columns

            const colSyms = this.symbols[c];
            for (let i = 0; i < colSyms.length; i++) {
                colSyms[i].y += step;
                if (colSyms[i].y > GameConfig.TopSlotGrid.rows * GameConfig.TopSlotGrid.symbolHeight) {
                    colSyms[i].y -= (GameConfig.TopSlotGrid.rows + 1) * GameConfig.TopSlotGrid.symbolHeight;
                }
            }
        }
    }
}
