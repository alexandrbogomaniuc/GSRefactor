import * as PIXI from 'pixi.js';
import { SlotEngine } from '../game/SlotEngine';
import { Camera } from './Camera';
import { GlowFilter } from '@pixi/filter-glow';
import { TopReels } from './TopReels';
import { MiningGrid } from './MiningGrid';
import { GameConfig } from '../game/GameConfig';
import { PickaxeEntity } from './physics/PickaxeEntity';
import { VFXManager } from './vfx/VFXManager';

export class UIManager {
    private app: PIXI.Application;
    private engine: SlotEngine;

    // Layout Containers
    private camera!: Camera;
    private topReels!: TopReels;
    private miningGrid!: MiningGrid;
    private vfxManager!: VFXManager;
    private currentMiningScript: any;

    // UI Elements
    private btnSpin!: HTMLButtonElement;
    private btnPaytable!: HTMLButtonElement;
    private btnAuto!: HTMLButtonElement;
    private btnTurbo!: HTMLButtonElement;

    // Status & Modals
    private lblStatus!: HTMLParagraphElement;
    private lblBalance!: HTMLParagraphElement;
    private lblWin!: HTMLParagraphElement;
    private modalPaytable!: HTMLDivElement;
    private btnClosePaytable!: HTMLButtonElement;

    // Bets
    private currentBetAmount: number = 1.00;
    private availableBets: number[] = [0.10, 0.20, 0.50, 1.00, 2.00, 5.00, 10.00, 20.00, 50.00];

    constructor(engine: SlotEngine) {
        this.engine = engine;

        // Initialize an empty PixiJS Application
        this.app = new PIXI.Application();

        this.initHTMLControls();
        this.bindEngine();
    }

    /**
     * PixiJS V8 requires an asynchronous boot sequence to initialize WebGL contexts
     * before we can append the canvas to the screen or draw graphics.
     */
    public async initPixi() {
        await this.app.init({
            width: 1920,
            height: 1080,
            backgroundAlpha: 0, // Transparent to show CSS background
            resolution: window.devicePixelRatio || 1,
            autoDensity: true
        });

        // Append the new canvas object to the DOM
        const container = document.getElementById('pixi-container');
        if (container) {
            container.appendChild(this.app.canvas);
        }

        // Setup the Camera
        this.camera = new Camera();
        this.app.stage.addChild(this.camera.container);

        // Example: Add a slight global glow for premium feel
        const glowFilter = new GlowFilter({ distance: 15, outerStrength: 0.1, innerStrength: 0, color: 0xffffff, quality: 0.1 });
        this.camera.container.filters = [glowFilter as any];

        this.vfxManager = new VFXManager(this.camera.container, this.camera);

        // Preload assets before building grids
        // Force Nearest Neighbor Scaling for crisp pixel art in PixiJS v8
        if ((PIXI as any).TextureStyle) {
            (PIXI as any).TextureStyle.defaultOptions.scaleMode = 'nearest';
        } else if ((PIXI as any).BaseTexture) {
            (PIXI as any).BaseTexture.defaultOptions.scaleMode = 1; // 1 = NEAREST
        }

        try {
            const urls = [
                '/assets/dirt.png', '/assets/stone.png', '/assets/gold.png', '/assets/crystal.png', '/assets/obsidian.png',
                '/assets/PickWood.png', '/assets/PickStone.png', '/assets/PickGold.png', '/assets/PickDiamond.png', '/assets/redstone.png',
                '/assets/chest-closed.png', '/assets/chest-open.png', '/assets/Grass.png', '/assets/Enchantment.png',
                '/assets/bg_epic_landscape.png', '/assets/bg_epic_clouds.png', '/assets/bg_spirit_deer.png', '/assets/bg_epic_flowers.png', '/assets/bg_flying_dragon.png'
            ];
            await Promise.allSettled(urls.map(u => PIXI.Assets.load(u).catch(e => console.warn("Failed to load:", u))));
            console.log("[UIManager] Assets loaded phase completed.");
        } catch (e) {
            console.warn("[UIManager] Error during asset loading.", e);
        }

        // 1. Top Inventory (Y: 20) - Obsolete: Integrated into TopReels
        // this.inventory = new TopInventory(740, 20);
        // this.camera.container.addChild(this.inventory.container);

        // 2. Top Spin Reels (Y: 20)
        // (5 * 96) + 20 padding = 500 width. Center is (1920-500)/2 = 710
        // Top Reels height is exactly 300px
        this.topReels = new TopReels(710, 20);
        this.camera.container.addChild(this.topReels.container);

        // 3. Bottom Mining Grid (Y: 332)
        // Starts exactly 12 pixels below the Top Reels to form a clear sky gap
        this.miningGrid = new MiningGrid(710, 332);
        this.camera.container.addChild(this.miningGrid.container);

        // Start Render Loop
        this.app.ticker.add((ticker) => {
            this.update(ticker.deltaTime);
        });
    }

    private initHTMLControls() {
        this.btnSpin = document.getElementById('btn-spin') as HTMLButtonElement;
        this.btnPaytable = document.getElementById('btn-paytable') as HTMLButtonElement;
        this.btnAuto = document.getElementById('btn-auto') as HTMLButtonElement;
        this.btnTurbo = document.getElementById('btn-turbo') as HTMLButtonElement;

        this.lblStatus = document.getElementById('engine-status') as HTMLParagraphElement;
        this.lblBalance = document.getElementById('lbl-balance') as HTMLParagraphElement;
        this.lblWin = document.getElementById('lbl-win') as HTMLParagraphElement;

        this.modalPaytable = document.getElementById('modal-paytable') as HTMLDivElement;
        this.btnClosePaytable = document.getElementById('btn-close-paytable') as HTMLButtonElement;

        const btnBetUp = document.getElementById('btn-bet-up') as HTMLButtonElement;
        const btnBetDown = document.getElementById('btn-bet-down') as HTMLButtonElement;
        const lblBet = document.getElementById('lbl-bet') as HTMLSpanElement;

        const btnHome = document.getElementById('btn-home') as HTMLButtonElement | null;
        if (btnHome) {
            btnHome.addEventListener('click', () => {
                console.log("[Interop] Sending HOME event to Casino Wrapper parent.");
                window.parent.postMessage({ action: 'HOME' }, '*');
            });
        }

        const updateBetUI = () => {
            lblBet.innerText = `$${this.currentBetAmount.toFixed(2)}`;
        };

        btnBetUp.addEventListener('click', () => {
            let idx = this.availableBets.indexOf(this.currentBetAmount);
            if (idx < this.availableBets.length - 1) {
                this.currentBetAmount = this.availableBets[idx + 1];
                updateBetUI();
            }
        });

        btnBetDown.addEventListener('click', () => {
            let idx = this.availableBets.indexOf(this.currentBetAmount);
            if (idx > 0) {
                this.currentBetAmount = this.availableBets[idx - 1];
                updateBetUI();
            }
        });

        this.btnSpin.addEventListener('click', () => {
            this.lblWin.innerText = "--";
            this.engine.play({ betAmount: this.currentBetAmount });
        });

        this.btnPaytable.addEventListener('click', () => {
            this.modalPaytable.style.display = 'flex';
        });

        this.btnClosePaytable.addEventListener('click', () => {
            this.modalPaytable.style.display = 'none';
        });

        this.btnAuto.addEventListener('click', () => {
            this.btnAuto.classList.toggle('active');
            console.log("[UIManager] Autoplay toggled:", this.btnAuto.classList.contains('active'));
        });

        this.btnTurbo.addEventListener('click', () => {
            this.btnTurbo.classList.toggle('active');
            console.log("[UIManager] Turbo toggled:", this.btnTurbo.classList.contains('active'));
        });
    }

    // Removed buildReels method.

    private bindEngine() {
        this.engine.onStateChange = (state) => {
            this.lblStatus.innerText = `Engine State: ${state}`;

            // Only enable spin when IDLE
            this.btnSpin.disabled = (state !== 'IDLE');

            if (state === 'SPINNING') {
                this.btnSpin.classList.add('spinning');
                this.topReels.startSpin();
            } else {
                this.btnSpin.classList.remove('spinning');
            }

            // Activate the physical event sequence
            if (state === 'MINING_PHASE') {
                this.runMiningPhaseSequence();
            }
        };

        this.engine.onSpinResult = (winAmount, slotResult, miningScript) => {
            console.log(`[UIManager] Round payload received.`, slotResult);

            // Populate the Initial Block field provided by the GS server script
            if (miningScript && miningScript.initialMiningGrid) {
                this.miningGrid.populateGrid(miningScript.initialMiningGrid);
                this.currentMiningScript = miningScript;
            }

            // A typical reel stop visual delay
            const spinDuration = this.btnTurbo.classList.contains('active') ? 500 : 1500;
            setTimeout(async () => {
                await this.topReels.stopSpin(slotResult);
                this.lblWin.innerText = `$${winAmount.toFixed(2)}`;
                this.engine.reelsResolved();
            }, spinDuration);
        };

        this.engine.onBalanceUpdate = (balance) => {
            this.lblBalance.innerText = `$${balance.toFixed(2)}`;
        };

        this.engine.onFreeRoundsUpdate = (remaining: number) => {
            const lblFrb = document.getElementById('lbl-frb') as HTMLSpanElement;
            if (lblFrb) {
                if (remaining > 0) {
                    lblFrb.style.display = 'inline';
                    lblFrb.innerText = `(FRB: ${remaining})`;
                } else {
                    lblFrb.style.display = 'none';
                }
            }
        };

        this.engine.onRoundComplete = () => {
            if (this.btnAuto.classList.contains('active')) {
                setTimeout(() => {
                    const currentBalance = parseFloat(this.lblBalance.innerText.replace(/[^0-9.-]+/g, ""));
                    if (currentBalance < 1.00) {
                        console.warn("[UIManager] Insufficient balance for Autoplay. Stopping.");
                        this.btnAuto.classList.remove('active');
                        return;
                    }
                    if (this.btnAuto.classList.contains('active') && this.engine.state === 'IDLE') {
                        this.btnSpin.click();
                    }
                }, 800);
            }
        };
    }

    /**
     * Executes the heavy physical Mining Stage (Pickaxe Drops -> Squashes -> Gravity Collapses).
     */
    private async runMiningPhaseSequence() {
        console.log("[UIManager] Running Full Mining Sequence...");

        if (this.currentMiningScript && this.currentMiningScript.pickaxeDrops) {
            for (const drop of this.currentMiningScript.pickaxeDrops) {
                // Determine tool ID from string matching
                let toolId = 101;
                if (drop.type === 'StonePickaxe') toolId = 102;
                else if (drop.type === 'IronPickaxe' || drop.type === 'GoldPickaxe') toolId = 103;
                else if (drop.type === 'DiamondPickaxe') toolId = 104;

                const pickaxe = new PickaxeEntity(toolId);
                // Important: Set initial position before adding to container to avoid flash
                const startX = this.topReels.container.x + (drop.col * GameConfig.TopSlotGrid.symbolWidth) + (GameConfig.TopSlotGrid.symbolWidth / 2);
                const startY = this.topReels.container.y + (GameConfig.TopSlotGrid.rows * GameConfig.TopSlotGrid.symbolHeight);
                pickaxe.sprite.x = startX;
                pickaxe.sprite.y = startY;

                this.camera.container.addChild(pickaxe.sprite);

                let isToolBroken = false;
                let isFalling = true; // Indicates the first arc drop

                // Tool drops towards whatever the current top block in the column is
                while (!isToolBroken) {
                    const targetBlock = this.miningGrid.getTopBlock(drop.col);

                    if (!targetBlock) {
                        // Col is empty, tool breaks on the floor
                        const floorY = this.miningGrid.container.y + (GameConfig.MiningGrid.rows * GameConfig.MiningGrid.blockSize);
                        const floorX = this.miningGrid.container.x + (drop.col * GameConfig.MiningGrid.blockSize) + (GameConfig.MiningGrid.blockSize / 2);
                        await pickaxe.playDropAnimation(pickaxe.sprite.x, pickaxe.sprite.y, floorX, floorY);
                        this.vfxManager.playImpactShake();
                        this.vfxManager.playBlockBreak(floorX, floorY, 'dust');
                        this.miningGrid.openChest(drop.col);
                        break; // Exit while loop naturally
                    }

                    // Arc down to the block or chop it if we are already there
                    if (isFalling) {
                        await pickaxe.playDropAnimation(pickaxe.sprite.x, pickaxe.sprite.y, targetBlock.x, targetBlock.y);
                        isFalling = false;
                    } else {
                        await pickaxe.playChopAnimation(targetBlock.x, targetBlock.y);
                    }

                    this.vfxManager.playImpactShake();
                    this.vfxManager.playBlockBreak(targetBlock.x, targetBlock.y, 'rock_chips');

                    // Damage logic
                    const destroyed = this.miningGrid.damageBlock(drop.col, targetBlock.row, pickaxe.getDamage());
                    isToolBroken = pickaxe.applyHit();

                    if (destroyed) {
                        // resolve gravity down before next chop
                        await this.miningGrid.resolveGravity(drop.col);
                        isFalling = true; // Needs to fall to the next block
                    }
                }

                pickaxe.destroy();
            }

            // Wait a slight moment after all drops resolve before processing chests
            await new Promise(resolve => setTimeout(resolve, GameConfig.Feel.BlockCrumbleDelayMS));

        } else {
            // Fallback if no script
            const gravityPromises = [];
            for (let i = 0; i < GameConfig.MiningGrid.cols; i++) {
                gravityPromises.push(this.miningGrid.resolveGravity(i));
            }
            await Promise.all(gravityPromises);
        }

        // Tell engine visual scripts are complete. If there was a win, show it.
        const winAmountStr = this.lblWin.innerText.replace(/[^0-9.-]+/g, "");
        const winAmount = parseFloat(winAmountStr) || 0;

        if (winAmount > 0) {
            const ratio = winAmount / this.currentBetAmount;
            if (ratio >= 15) {
                this.showWinCelebration(winAmount, ratio);
                return;
            }
        }

        this.engine.miningComplete();
    }

    private update(_delta: number) {
        if (this.topReels) {
            this.topReels.update(this.app.ticker.elapsedMS);
        }
        if (this.vfxManager) {
            this.vfxManager.update(this.app.ticker.elapsedMS);
        }
    }

    private showWinCelebration(amount: number, ratio: number) {
        const container = document.getElementById('win-celebration') as HTMLDivElement;
        const typeText = document.getElementById('win-type-text') as HTMLHeadingElement;
        const amtText = document.getElementById('win-amount-text') as HTMLHeadingElement;

        container.className = 'win-celebration';
        if (ratio >= 50) {
            container.classList.add('mega');
            typeText.innerText = "MEGA WIN!";
        } else if (ratio >= 30) {
            container.classList.add('huge');
            typeText.innerText = "HUGE WIN!";
        } else {
            container.classList.add('big');
            typeText.innerText = "BIG WIN!";
        }

        amtText.innerText = `$${amount.toFixed(2)}`;
        container.style.display = 'flex';

        setTimeout(() => {
            container.style.display = 'none';
            this.engine.miningComplete();
        }, this.btnTurbo.classList.contains('active') ? 1500 : 4000);
    }
}
