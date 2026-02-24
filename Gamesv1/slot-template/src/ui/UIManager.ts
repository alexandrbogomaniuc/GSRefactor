import * as PIXI from 'pixi.js';
import { SlotEngine } from '../game/SlotEngine';
import { Reel } from './Reel';

export class UIManager {
    private app: PIXI.Application;
    private engine: SlotEngine;
    private reels: Reel[] = [];

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
            width: 800,
            height: 600,
            backgroundColor: 0x1099bb,
            resolution: window.devicePixelRatio || 1,
            autoDensity: true
        });

        // Append the new canvas object to the DOM (replaces deprecated app.view)
        const container = document.getElementById('pixi-container');
        if (container) {
            container.appendChild(this.app.canvas);
        }

        this.buildReels(3); // Default to a 3-reel slot layout initially

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
            this.lblWin.innerText = "Win: --";
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

    private buildReels(cols: number = 3) {
        // Calculate offset to center the dynamic number of reels perfectly
        const reelWidth = 120;
        const totalWidth = cols * reelWidth;
        const startX = (800 - totalWidth) / 2 + (reelWidth / 2);

        for (let i = 0; i < cols; i++) {
            const reel = new Reel(startX + (i * reelWidth));
            this.reels.push(reel);
            this.app.stage.addChild(reel.container);
        }
    }

    private bindEngine() {
        this.engine.onStateChange = (state) => {
            this.lblStatus.innerText = `Engine State: ${state}`;
            this.btnSpin.disabled = (state !== 'READY');

            if (state === 'RESERVED') {
                // GS Orchestrator has received Bet, reels are spinning waiting for result
                this.btnSpin.classList.add('spinning');
                this.reels.forEach(r => r.startSpin());
            } else {
                this.btnSpin.classList.remove('spinning');
            }
        };

        this.engine.onSpinResult = (winAmount, grid) => {
            // GS sent deterministic evaluation payload. 
            // Engine is locked in EVALUATING until visuals finish.
            console.log(`[UIManager] Received GS Grid:`, grid);

            // Map the dynamically sized 2D array (rows x cols) to specific reel columns.
            const rows = grid.length;
            const cols = grid[0].length;

            for (let i = 0; i < Math.min(this.reels.length, cols); i++) {
                const column = [];
                for (let r = 0; r < rows; r++) {
                    column.push(grid[r][i]);
                }
                this.reels[i].stopSpin(column);
            }

            // Simulate the visual presentation time taking 2 seconds
            // In a real game, listen to a PIXI 'onComplete' animation event.
            // *Turbo overrides this to finish quickly.*
            const spinDuration = this.btnTurbo.classList.contains('active') ? 500 : 2000;

            setTimeout(() => {
                this.lblWin.innerText = `Win: $${winAmount.toFixed(2)}`;
                console.log("[UIManager] Reeling complete. Telling Engine to SETTLE.");

                const ratio = winAmount / this.currentBetAmount;
                if (ratio >= 15) {
                    this.showWinCelebration(winAmount, ratio);
                } else {
                    this.engine.animationsComplete();
                }
            }, spinDuration);
        };

        this.engine.onBalanceUpdate = (balance) => {
            this.lblBalance.innerText = `Balance: $${balance.toFixed(2)}`;
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
            // Evaluates Autoplay after the session is formally settled and ready again
            if (this.btnAuto.classList.contains('active')) {
                // Short breather between spins
                setTimeout(() => {
                    const currentBalance = parseFloat(this.lblBalance.innerText.replace(/[^0-9.-]+/g, ""));
                    if (currentBalance < 1.00) {
                        console.warn("[UIManager] Insufficient balance for Autoplay. Stopping.");
                        this.btnAuto.classList.remove('active');
                        return;
                    }
                    if (this.btnAuto.classList.contains('active') && this.engine.state === 'READY') {
                        this.btnSpin.click();
                    }
                }, 800);
            }
        };
    }

    private update(delta: number) {
        this.reels.forEach(r => r.update(delta));
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
            this.engine.animationsComplete();
        }, this.btnTurbo.classList.contains('active') ? 1500 : 4000);
    }
}
