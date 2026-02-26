import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';

/**
 * Environment — Dynamic Background Layer
 * 
 * Renders the "alive" world behind the slot game:
 * - Game logo (top-left corner)
 * - Floating ghasts with bobbing/wing animation
 * - Animated chicken with idle bobbing  
 * - Procedural Minecraft-style pixel clouds (white blocks, no image)
 */

interface FloatingEntity {
    sprite: PIXI.Container;
    baseY: number;
    speedX: number;
    bobAmplitude: number;
    bobSpeed: number;
    phase: number;
}

export class Environment {
    public container: PIXI.Container;

    // Sub-layers (back to front)
    private cloudsLayer: PIXI.Container;
    private creaturesLayer: PIXI.Container;
    private foregroundLayer: PIXI.Container;

    // Animated entities
    private ghasts: FloatingEntity[] = [];
    private clouds: FloatingEntity[] = [];
    private chicken: PIXI.Sprite | null = null;
    private chickenBaseY: number = 0;

    // Logo is now rendered text, no sprite needed

    // Canvas dimensions
    private readonly CANVAS_W = 1920;

    constructor(parentContainer: PIXI.Container) {
        this.container = new PIXI.Container();
        parentContainer.addChildAt(this.container, 0);

        this.cloudsLayer = new PIXI.Container();
        this.creaturesLayer = new PIXI.Container();
        this.foregroundLayer = new PIXI.Container();

        this.container.addChild(this.cloudsLayer);
        this.container.addChild(this.creaturesLayer);
        this.container.addChild(this.foregroundLayer);

        this.buildLogo();
        this.buildProceduralClouds();
        this.buildGhasts();
        this.buildChicken();
    }

    // ─────────────────────────────────────────────
    // LOGO (top-left corner)
    // ─────────────────────────────────────────────
    private buildLogo() {
        const logoContainer = new PIXI.Container();

        // Minecraft-style white text with heavy shadows — pyramid shape
        const makeStyle = (size: number): PIXI.TextStyle => new PIXI.TextStyle({
            fontFamily: '"Impact", "Arial Black", sans-serif',
            fontSize: size,
            fontWeight: 'bold',
            fill: '#FFFFFF',
            stroke: { width: 8, color: '#222222' },
            letterSpacing: 8,
            dropShadow: {
                color: '#000000',
                blur: 0,
                angle: Math.PI / 4,
                distance: 6,
            }
        });

        // "WIN" — smaller on top (pyramid top)
        const winText = new PIXI.Text({ text: 'WIN', style: makeStyle(70) });
        logoContainer.addChild(winText);

        // "CRAFT" — bigger on bottom (pyramid base)
        const craftText = new PIXI.Text({ text: 'CRAFT', style: makeStyle(90) });
        logoContainer.addChild(craftText);

        // Center-align both lines horizontally — creating the pyramid alignment
        // We position after render tick so we can measure text width
        winText.anchor.set(0.5, 0);
        craftText.anchor.set(0.5, 0);
        winText.x = 0;
        winText.y = 0;
        craftText.x = 0;
        craftText.y = 68;

        // Center the logo on the left portion of the screen
        logoContainer.x = 200;
        logoContainer.y = 15;

        this.foregroundLayer.addChild(logoContainer);
    }

    // ─────────────────────────────────────────────
    // PROCEDURAL MINECRAFT CLOUDS (pixel-art white blocks)
    // ─────────────────────────────────────────────
    private buildProceduralClouds() {
        // Cloud patterns — each cloud is a grid of blocks (1=block, 0=empty)
        const cloudPatterns = [
            // Large fluffy cloud
            [
                [0, 1, 1, 1, 1, 0],
                [1, 1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1, 1],
                [0, 1, 1, 1, 0, 0],
            ],
            // Medium cloud
            [
                [0, 1, 1, 1, 0],
                [1, 1, 1, 1, 1],
                [0, 1, 1, 1, 0],
            ],
            // Long cloud
            [
                [0, 1, 1, 1, 1, 1, 1, 0],
                [1, 1, 1, 1, 1, 1, 1, 1],
                [0, 0, 1, 1, 1, 1, 0, 0],
            ],
            // Small puffy cloud
            [
                [1, 1, 1],
                [1, 1, 1],
            ],
            // Wispy cloud
            [
                [0, 0, 1, 1, 0],
                [0, 1, 1, 1, 1],
                [1, 1, 1, 1, 0],
                [0, 1, 0, 0, 0],
            ],
        ];

        const blockSize = 16; // Each pixel block in the cloud

        const cloudConfigs = [
            { pattern: 0, x: -100, y: 30, speed: 0.25, alpha: 0.85, scale: 2.4 },
            { pattern: 1, x: 400, y: 80, speed: 0.18, alpha: 0.7, scale: 2.0 },
            { pattern: 2, x: 900, y: 20, speed: 0.30, alpha: 0.9, scale: 2.6 },
            { pattern: 3, x: 1400, y: 100, speed: 0.15, alpha: 0.6, scale: 1.8 },
            { pattern: 4, x: 1700, y: 50, speed: 0.22, alpha: 0.75, scale: 2.2 },
            { pattern: 1, x: 200, y: 130, speed: 0.12, alpha: 0.5, scale: 1.6 },
        ];

        for (const cfg of cloudConfigs) {
            const cloudContainer = new PIXI.Container();
            const pattern = cloudPatterns[cfg.pattern];

            for (let row = 0; row < pattern.length; row++) {
                for (let col = 0; col < pattern[row].length; col++) {
                    if (pattern[row][col] === 1) {
                        const block = new PIXI.Graphics();
                        // Main white block
                        block.beginFill(0xFFFFFF);
                        block.drawRect(0, 0, blockSize, blockSize);
                        block.endFill();

                        // Top highlight (lighter)
                        block.beginFill(0xFFFFFF, 0.3);
                        block.drawRect(0, 0, blockSize, 3);
                        block.endFill();

                        // Bottom shadow (darker)
                        block.beginFill(0xD0D0D0);
                        block.drawRect(0, blockSize - 3, blockSize, 3);
                        block.endFill();

                        // Left highlight
                        block.beginFill(0xFFFFFF, 0.2);
                        block.drawRect(0, 3, 2, blockSize - 6);
                        block.endFill();

                        block.x = col * blockSize;
                        block.y = row * blockSize;
                        cloudContainer.addChild(block);
                    }
                }
            }

            cloudContainer.x = cfg.x;
            cloudContainer.y = cfg.y;
            cloudContainer.alpha = cfg.alpha;
            cloudContainer.scale.set(cfg.scale);

            this.cloudsLayer.addChild(cloudContainer);
            this.clouds.push({
                sprite: cloudContainer,
                baseY: cfg.y,
                speedX: cfg.speed,
                bobAmplitude: 3 + Math.random() * 4,
                bobSpeed: 0.0004 + Math.random() * 0.0002,
                phase: Math.random() * Math.PI * 2,
            });
        }
    }

    // ─────────────────────────────────────────────
    // GHASTS (floating mobs with bobbing animation)
    // ─────────────────────────────────────────────
    private buildGhasts() {
        try {
            const tex = PIXI.Assets.get('/assets/ghast.png');
            if (!tex) return;

            const ghastConfigs = [
                { x: 280, y: 60, scale: 1.4, speed: 0.15, flipX: false },
                { x: 1450, y: 120, scale: 1.1, speed: -0.12, flipX: true },
            ];

            for (const cfg of ghastConfigs) {
                const ghast = new PIXI.Sprite(tex);
                ghast.anchor.set(0.5);
                ghast.scale.set(cfg.flipX ? -cfg.scale : cfg.scale, cfg.scale);
                ghast.x = cfg.x;
                ghast.y = cfg.y;
                ghast.alpha = 0.85;

                this.creaturesLayer.addChild(ghast);
                this.ghasts.push({
                    sprite: ghast,
                    baseY: cfg.y,
                    speedX: cfg.speed,
                    bobAmplitude: 8 + Math.random() * 6,
                    bobSpeed: 0.001 + Math.random() * 0.0005,
                    phase: Math.random() * Math.PI * 2,
                });

                // Wing-flap animation
                gsap.to(ghast.scale, {
                    y: cfg.scale * 1.08,
                    duration: 0.8 + Math.random() * 0.4,
                    yoyo: true,
                    repeat: -1,
                    ease: "sine.inOut",
                });
            }
        } catch (e) {
            console.warn("[Environment] Ghast assets not available:", e);
        }
    }

    // ─────────────────────────────────────────────
    // CHICKEN (standing on left side, facing left)
    // ─────────────────────────────────────────────
    private buildChicken() {
        try {
            const tex = PIXI.Assets.get('/assets/chicken.png');
            if (!tex) return;

            this.chicken = new PIXI.Sprite(tex);
            this.chicken.anchor.set(0.5, 1.0);
            this.chicken.scale.set(-0.9, 0.9); // Flipped to face left, smaller
            // Left side of grid, at chest row level
            // Mining grid at x=710, so chicken at about x=600 (left side)
            this.chicken.x = 600;
            this.chicken.y = 1040;
            this.chickenBaseY = 1040;

            this.creaturesLayer.addChild(this.chicken);

            // Realistic chicken idle: quick peck bob
            const peckLoop = () => {
                if (!this.chicken) return;
                const delay = 2 + Math.random() * 3;
                gsap.delayedCall(delay, () => {
                    if (!this.chicken) return;
                    gsap.to(this.chicken, {
                        rotation: -0.12,
                        y: this.chickenBaseY + 3,
                        duration: 0.1,
                        ease: "power2.in",
                        onComplete: () => {
                            if (!this.chicken) return;
                            gsap.to(this.chicken, {
                                rotation: 0,
                                y: this.chickenBaseY,
                                duration: 0.12,
                                ease: "power2.out",
                                onComplete: peckLoop
                            });
                        }
                    });
                });
            };
            peckLoop();

            // Small shuffling steps
            const shuffleLoop = () => {
                if (!this.chicken) return;
                const targetX = 580 + Math.random() * 30;
                gsap.to(this.chicken, {
                    x: targetX,
                    duration: 2 + Math.random() * 2,
                    ease: "steps(3)",
                    onComplete: shuffleLoop
                });
            };
            shuffleLoop();

        } catch (e) {
            console.warn("[Environment] Chicken asset not available:", e);
        }
    }

    // ─────────────────────────────────────────────
    // PUBLIC: Win reaction
    // ─────────────────────────────────────────────
    public onWin() {
        if (!this.chicken) return;

        gsap.to(this.chicken, {
            y: this.chickenBaseY - 40,
            duration: 0.15,
            yoyo: true,
            repeat: 3,
            ease: "power2.out",
        });

        gsap.to(this.chicken, {
            rotation: Math.PI * 2,
            duration: 0.4,
            ease: "power1.out",
            onComplete: () => {
                if (this.chicken) this.chicken.rotation = 0;
            }
        });
    }

    // ─────────────────────────────────────────────
    // FRAME UPDATE
    // ─────────────────────────────────────────────
    public update(deltaTimeMS: number) {
        const time = performance.now();

        // Animate clouds
        for (const cloud of this.clouds) {
            cloud.sprite.x += cloud.speedX * (deltaTimeMS / 16.67);
            cloud.sprite.y = cloud.baseY + Math.sin(time * cloud.bobSpeed + cloud.phase) * cloud.bobAmplitude;

            const cloudWidth = (cloud.sprite as PIXI.Container).width || 100;
            if (cloud.speedX > 0 && cloud.sprite.x > this.CANVAS_W + 50) {
                cloud.sprite.x = -cloudWidth - 50;
            } else if (cloud.speedX < 0 && cloud.sprite.x < -cloudWidth - 50) {
                cloud.sprite.x = this.CANVAS_W + 50;
            }
        }

        // Animate ghasts
        for (const ghast of this.ghasts) {
            ghast.sprite.x += ghast.speedX * (deltaTimeMS / 16.67);
            ghast.sprite.y = ghast.baseY + Math.sin(time * ghast.bobSpeed + ghast.phase) * ghast.bobAmplitude;

            if (ghast.speedX > 0 && ghast.sprite.x > this.CANVAS_W + 100) {
                ghast.sprite.x = -100;
            } else if (ghast.speedX < 0 && ghast.sprite.x < -100) {
                ghast.sprite.x = this.CANVAS_W + 100;
            }
        }

        // Chicken movement is now handled entirely by GSAP peck/shuffle loops
        // No frame-level bobbing needed
    }
}
