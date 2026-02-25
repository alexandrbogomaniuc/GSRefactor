import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';
import { GameConfig } from '../game/GameConfig';

export class MiningGrid {
    public container: PIXI.Container;
    private backgroundContainer: PIXI.Container;
    private blocksContainer: PIXI.Container;
    private blocks: (PIXI.Sprite | null)[][] = [];
    private blockHps: number[][] = [];
    private chests: PIXI.Sprite[] = [];

    constructor(startX: number, startY: number) {
        this.container = new PIXI.Container();
        this.container.x = startX;
        this.container.y = startY;

        this.backgroundContainer = new PIXI.Container();
        this.blocksContainer = new PIXI.Container();

        this.container.addChild(this.backgroundContainer);
        this.container.addChild(this.blocksContainer);

        this.buildStaticDecorations();
        this.initEmptyGrid();
    }

    private buildStaticDecorations() {
        const gridBottomY = GameConfig.MiningGrid.rows * GameConfig.MiningGrid.blockSize;

        // Gap below the regular mining blocks (row 5) exposing the background
        const floorY = gridBottomY + 12;

        // 1. Chests (Row N + Gap)
        for (let c = 0; c < GameConfig.MiningGrid.cols; c++) {
            let tex = PIXI.Texture.WHITE;
            try { tex = PIXI.Texture.from('/assets/chest-closed.png'); } catch { }

            const chest = new PIXI.Sprite(tex);
            chest.width = GameConfig.MiningGrid.blockSize;
            chest.height = GameConfig.MiningGrid.blockSize;
            chest.x = c * GameConfig.MiningGrid.blockSize;
            chest.y = floorY;

            if (tex === PIXI.Texture.WHITE) chest.tint = 0x8B4513; // Brown for fallback
            this.chests.push(chest);
            this.backgroundContainer.addChild(chest);
        }

        // 2. Grass block headers (Row N + 1 + Gap)
        for (let c = 0; c < GameConfig.MiningGrid.cols; c++) {
            let tex = PIXI.Texture.WHITE;
            try { tex = PIXI.Texture.from('/assets/Grass.png'); } catch { }

            const grass = new PIXI.Sprite(tex);
            grass.width = GameConfig.MiningGrid.blockSize;
            grass.height = GameConfig.MiningGrid.blockSize;
            grass.x = c * GameConfig.MiningGrid.blockSize;
            grass.y = floorY + GameConfig.MiningGrid.blockSize;

            if (tex === PIXI.Texture.WHITE) grass.tint = 0x5E9E3B; // Green for fallback
            this.backgroundContainer.addChild(grass);
        }

        // 3. Deep dirt filler below grass
        const dirtFill = new PIXI.Graphics();
        dirtFill.beginFill(0x7c5b45);
        dirtFill.drawRect(0, floorY + GameConfig.MiningGrid.blockSize * 2, GameConfig.MiningGrid.cols * GameConfig.MiningGrid.blockSize, 500);
        dirtFill.endFill();
        this.backgroundContainer.addChild(dirtFill);
    }

    private initEmptyGrid() {
        // Initialize an empty 2D array representation
        for (let col = 0; col < GameConfig.MiningGrid.cols; col++) {
            this.blocks[col] = [];
            this.blockHps[col] = [];
            for (let row = 0; row < GameConfig.MiningGrid.rows; row++) {
                this.blocks[col][row] = null;
                this.blockHps[col][row] = 0;
            }
        }

        // Setup initial default visuals so the grid isn't empty before the first spin
        for (let col = 0; col < GameConfig.MiningGrid.cols; col++) {
            for (let row = 0; row < GameConfig.MiningGrid.rows; row++) {
                let id = 1;
                if (row === 0) id = 6; // Grass
                else if (row === 1) id = 1; // Dirt
                else if (row === 2) id = 2; // Stone
                else if (row === 3) id = 7; // Redstone
                else if (row === 4) id = 3; // Gold
                else if (row === 5) {
                    id = col % 2 === 0 ? 5 : 4; // Obsidian, Diamond alternating
                }
                this.spawnBlock(col, row, id);
            }
        }
    }

    public populateGrid(initialGridIds: number[][]) {
        // Clear previous
        this.blocksContainer.removeChildren();

        for (let col = 0; col < GameConfig.MiningGrid.cols; col++) {
            for (let row = 0; row < GameConfig.MiningGrid.rows; row++) {
                const typeId = initialGridIds[row][col];
                if (typeId > 0) {
                    this.spawnBlock(col, row, typeId);
                }
            }
        }
    }

    private spawnBlock(col: number, row: number, typeId: number) {
        let sprite: PIXI.Sprite;
        let tex = PIXI.Texture.WHITE;

        // Try load assets or fallback
        try {
            const blockDef = Object.values(GameConfig.Blocks).find((b: any) => b.id === typeId);
            if (blockDef && blockDef.texture) {
                tex = PIXI.Texture.from(`/assets/${blockDef.texture}`);
            }
        } catch {
            // fallback to white
        }

        if (tex === PIXI.Texture.WHITE) {
            // Placeholder rectangle if no texture
            const gfx = new PIXI.Graphics();
            gfx.beginFill(0x555555);
            gfx.lineStyle(2, 0x000000);
            gfx.drawRect(0, 0, GameConfig.MiningGrid.blockSize, GameConfig.MiningGrid.blockSize);
            gfx.endFill();
            sprite = new PIXI.Sprite(PIXI.Texture.WHITE);
            sprite.tint = 0x555555 * typeId;
        } else {
            sprite = new PIXI.Sprite(tex);
        }

        sprite.width = GameConfig.MiningGrid.blockSize;
        sprite.height = GameConfig.MiningGrid.blockSize;

        // Final position calculation
        sprite.x = col * GameConfig.MiningGrid.blockSize;
        sprite.y = row * GameConfig.MiningGrid.blockSize;

        // Add 2.5D pseudo-depth sorting by Y axis if needed eventually
        this.blocksContainer.addChild(sprite);
        this.blocks[col][row] = sprite;

        const blockDef = Object.values(GameConfig.Blocks).find((b: any) => b.id === typeId);
        this.blockHps[col][row] = blockDef ? blockDef.maxHp : 1;
    }

    /**
     * Re-evaluates gravity for a specific column.
     * Starts from the bottom up.
     * Returns a Promise that resolves when all drop animations for this column are complete.
     */
    public async resolveGravity(col: number): Promise<void> {
        const promises: Promise<void>[] = [];

        // Scan bottom to top
        for (let row = GameConfig.MiningGrid.rows - 1; row >= 0; row--) {
            const block = this.blocks[col][row];
            if (block !== null) {
                // Find how far down it can fall
                let lowestEmptyRow = -1;
                for (let searchRow = GameConfig.MiningGrid.rows - 1; searchRow > row; searchRow--) {
                    if (this.blocks[col][searchRow] === null) {
                        lowestEmptyRow = searchRow;
                        break;
                    }
                }

                if (lowestEmptyRow !== -1) {
                    // Update the logic grid immediately
                    this.blocks[col][lowestEmptyRow] = block;
                    this.blocks[col][row] = null;

                    this.blockHps[col][lowestEmptyRow] = this.blockHps[col][row];
                    this.blockHps[col][row] = 0;

                    // Animate the block falling
                    const targetY = lowestEmptyRow * GameConfig.MiningGrid.blockSize;

                    const p = new Promise<void>((resolve) => {
                        gsap.to(block, {
                            y: targetY,
                            duration: GameConfig.MiningGrid.gravitySpeed / 1000,
                            ease: "bounce.out",
                            onComplete: () => {
                                resolve();
                            }
                        });
                    });
                    promises.push(p);
                }
            }
        }

        await Promise.all(promises);
    }

    /**
     * Gets the current topmost block in a column.
     * Starts from row 0 (top) down to max rows.
     */
    public getTopBlock(col: number): { row: number, x: number, y: number } | null {
        for (let r = 0; r < GameConfig.MiningGrid.rows; r++) {
            if (this.blocks[col][r] !== null) {
                return {
                    row: r,
                    x: this.container.x + (col * GameConfig.MiningGrid.blockSize) + (GameConfig.MiningGrid.blockSize / 2),
                    y: this.container.y + (r * GameConfig.MiningGrid.blockSize) + (GameConfig.MiningGrid.blockSize / 2)
                };
            }
        }
        return null; // Column is empty
    }

    /**
     * Applies damage to a block. If HP reaches 0, destroys it.
     * Returns true if the block was destroyed, false if it survived.
     */
    public damageBlock(col: number, row: number, damage: number): boolean {
        if (this.blocks[col][row] === null) return false;

        this.blockHps[col][row] -= damage;

        const block = this.blocks[col][row]!;

        if (this.blockHps[col][row] <= 0) {
            // Destroy visual - Explosive blast out
            gsap.to(block.scale, {
                x: 1.5,
                y: 1.5,
                duration: 0.25,
                ease: "back.in(2)"
            });
            gsap.to(block, {
                alpha: 0,
                rotation: (Math.random() - 0.5) * Math.PI,
                duration: 0.25,
                ease: "back.in(2)",
                onComplete: () => {
                    if (block.parent) block.parent.removeChild(block);
                    block.destroy();
                }
            });

            // Destroy logic
            this.blocks[col][row] = null;
            this.blockHps[col][row] = 0;
            return true;
        } else {
            // Just flash/squash if it survives
            gsap.fromTo(block,
                { tint: 0xff0000, y: block.y + 5 },
                { tint: 0xffffff, y: block.y - 5, duration: 0.1, yoyo: true, repeat: 1 }
            );
            return false;
        }
    }

    public openChest(col: number) {
        if (!this.chests[col]) return;
        const chest = this.chests[col];

        try {
            chest.texture = PIXI.Texture.from('/assets/chest-open.png');
        } catch { }

        // Give it a bouncy celebratory pop
        chest.scale.set(1.2, 0.8);
        gsap.to(chest.scale, {
            x: 1,
            y: 1,
            duration: 0.5,
            ease: "elastic.out(1, 0.3)"
        });
    }
}
