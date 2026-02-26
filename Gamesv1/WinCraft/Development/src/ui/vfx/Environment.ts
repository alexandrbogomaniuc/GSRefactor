import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';
import { GameConfig } from '../../game/GameConfig';

export class Environment {
    public container: PIXI.Container;

    // HD Background Layers
    private staticBg!: PIXI.Sprite;
    private sunRayLayer!: PIXI.Graphics; // Keep simple volumetric sun ray rendering

    private cloudsContainer!: PIXI.Container;
    private floraContainer!: PIXI.Container;

    // Ambient Mobs
    private spiritDeer!: PIXI.Sprite;
    private dragonSilhouette!: PIXI.Sprite;

    constructor(parentContainer: PIXI.Container) {
        this.container = new PIXI.Container();
        parentContainer.addChildAt(this.container, 0); // Always at the absolute back

        this.buildHDLandscape();
        this.buildHDClouds();
        this.buildHDFlora();
        this.spawnWowMobs();

        // Final subtle sun rays over everything
        this.addSunRays();
    }

    private buildHDLandscape() {
        try {
            const tex = PIXI.Texture.from('/assets/bg_epic_landscape.png');
            this.staticBg = new PIXI.Sprite(tex);

            // The image is high res (e.g. 1024x1024 base). Scale it to cover 1920x1080
            // preserving aspect ratio or stretching slightly as needed.
            // Using a "cover" calculation logic:
            const scaleX = 1920 / tex.width;
            const scaleY = 1080 / tex.height;
            const scale = Math.max(scaleX, scaleY); // Ensure no black bars

            this.staticBg.scale.set(scale);

            // Center it
            this.staticBg.anchor.set(0.5);
            this.staticBg.x = 1920 / 2;
            this.staticBg.y = 1080 / 2;

            this.container.addChild(this.staticBg);

            // Add subtle breathing parallax effect to the base layer
            gsap.to(this.staticBg.scale, {
                x: scale * 1.05,
                y: scale * 1.05,
                duration: 20,
                ease: "sine.inOut",
                yoyo: true,
                repeat: -1
            });

            gsap.to(this.staticBg, {
                y: this.staticBg.y - 15,
                duration: 15,
                ease: "sine.inOut",
                yoyo: true,
                repeat: -1
            });

        } catch (e) {
            console.warn("[Environment] Could not load bg_epic_landscape.png, falling back to blue sky.");
            this.staticBg = new PIXI.Sprite(PIXI.Texture.WHITE);
            this.staticBg.tint = 0x87CEEB;
            this.staticBg.width = 1920;
            this.staticBg.height = 1080;
            this.container.addChild(this.staticBg);
        }
    }

    private buildHDClouds() {
        this.cloudsContainer = new PIXI.Container();
        this.container.addChild(this.cloudsContainer);

        try {
            const tex = PIXI.Texture.from('/assets/bg_epic_clouds.png');

            // Spawn multiple clouds from the single sprite with SCREEN blend mode
            // Since the generated asset has a pure black background, SCREEN will make
            // the black invisible and keep the bright fluffy clouds.
            for (let i = 0; i < 4; i++) {
                const cloud = new PIXI.Sprite(tex);
                cloud.blendMode = 'screen';

                // Randomize sizes and horizontal start
                const baseScale = 0.5 + Math.random() * 0.8;
                cloud.scale.set(baseScale);
                cloud.alpha = 0.6 + Math.random() * 0.4;

                cloud.x = (Math.random() * 1920 * 2) - 1920; // Start across a wide field
                cloud.y = -200 + Math.random() * 300; // Keep towards the top part of the screen

                this.cloudsContainer.addChild(cloud);
                this.animateCloud(cloud, baseScale);
            }
        } catch {
            console.warn("[Environment] Missing bg_epic_clouds.png");
        }
    }

    private animateCloud(cloud: PIXI.Sprite, scale: number) {
        // Clouds further back (smaller) move slower
        const speed = 150 * (2.0 - scale); // 100-300 seconds to cross large distance

        gsap.to(cloud, {
            x: 1920 + 500,
            duration: speed,
            ease: "none",
            onComplete: () => {
                cloud.x = -cloud.width;
                cloud.y = -200 + Math.random() * 300;
                this.animateCloud(cloud, scale);
            }
        });
    }

    private buildHDFlora() {
        this.floraContainer = new PIXI.Container();
        this.container.addChild(this.floraContainer);

        try {
            const tex = PIXI.Texture.from('/assets/bg_epic_flowers.png');

            // Spawn magical flowers around the base UI areas
            // The asset has a solid black background, SCREEN mode makes it glow on top of landscape

            const positions = [
                { x: 100, y: 800, scale: 0.8 },
                { x: 350, y: 950, scale: 1.2 },
                { x: 1500, y: 850, scale: 0.9 },
                { x: 1750, y: 900, scale: 1.1 },
            ];

            positions.forEach((pos, idx) => {
                const flower = new PIXI.Sprite(tex);
                flower.blendMode = 'screen';
                flower.anchor.set(0.5, 1); // Anchor bottom
                flower.scale.set(pos.scale);
                flower.x = pos.x;
                flower.y = pos.y;

                // Slight neon pulse
                flower.alpha = 0.8;

                this.floraContainer.addChild(flower);

                // Gently sway them
                gsap.to(flower.scale, {
                    x: pos.scale * 1.05,
                    y: pos.scale * 0.95,
                    duration: 2 + (idx * 0.5),
                    ease: "sine.inOut",
                    yoyo: true,
                    repeat: -1
                });

                gsap.to(flower, {
                    alpha: 1,
                    duration: 3 + (idx * 0.3),
                    ease: "sine.inOut",
                    yoyo: true,
                    repeat: -1
                });
            });

        } catch {
            console.warn("[Environment] Missing bg_epic_flowers.png");
        }
    }

    private spawnWowMobs() {
        // SPIRIT DEER (SCREEN MODE)
        try {
            const deerTex = PIXI.Texture.from('/assets/bg_spirit_deer.png');
            this.spiritDeer = new PIXI.Sprite(deerTex);
            this.spiritDeer.blendMode = 'screen'; // Erase black bg, keep glowing deer
            this.spiritDeer.anchor.set(0.5, 1);

            const floorY = 550 + (GameConfig.MiningGrid.rows * GameConfig.MiningGrid.blockSize) - 50;

            this.spiritDeer.x = -300;
            this.spiritDeer.y = floorY;
            this.spiritDeer.scale.set(0.6);

            this.container.addChild(this.spiritDeer);

            // Start roaming
            this.roamDeer();

        } catch {
            console.warn("[Environment] Missing bg_spirit_deer.png");
        }

        // FLYING DRAGON (MULTIPLY MODE)
        try {
            const dragonTex = PIXI.Texture.from('/assets/bg_flying_dragon.png');
            this.dragonSilhouette = new PIXI.Sprite(dragonTex);
            // Dragon has white background, multiply will remove white and leave black silhouette
            this.dragonSilhouette.blendMode = 'multiply';
            this.dragonSilhouette.anchor.set(0.5);
            this.dragonSilhouette.scale.set(0.3); // High altitude
            this.dragonSilhouette.alpha = 0.5; // Atmospheric perspective fading

            this.container.addChild(this.dragonSilhouette);

            this.flyDragon();

        } catch {
            console.warn("[Environment] Missing bg_flying_dragon.png");
        }
    }

    private roamDeer() {
        if (!this.spiritDeer) return;

        // Pulse the spirit glow
        gsap.to(this.spiritDeer, {
            alpha: 0.6,
            duration: 2,
            yoyo: true,
            ease: "sine.inOut",
            repeat: -1
        });

        const targetX = 200 + Math.random() * 1500;
        const speed = 15 + Math.random() * 15; // Slow ethereal walk

        // Face direction (flip if walking left)
        if (targetX < this.spiritDeer.x) {
            this.spiritDeer.scale.x = -Math.abs(this.spiritDeer.scale.x);
        } else {
            this.spiritDeer.scale.x = Math.abs(this.spiritDeer.scale.x);
        }

        gsap.to(this.spiritDeer, {
            x: targetX,
            duration: speed,
            ease: "sine.inOut",
            onComplete: () => {
                // Wait and graze
                setTimeout(() => this.roamDeer(), 4000 + Math.random() * 8000);
            }
        });

        // Gentle bob
        gsap.to(this.spiritDeer, {
            y: this.spiritDeer.y - 10,
            duration: 1,
            yoyo: true,
            repeat: speed,
            ease: "sine.inOut"
        });
    }

    private flyDragon() {
        if (!this.dragonSilhouette) return;

        this.dragonSilhouette.x = -200;
        this.dragonSilhouette.y = 150 + Math.random() * 200;

        // Randomly flip x to occasionally fly Right-to-Left
        const rtl = Math.random() > 0.5;
        if (rtl) {
            this.dragonSilhouette.x = 2100;
            this.dragonSilhouette.scale.x = -Math.abs(this.dragonSilhouette.scale.x);
        } else {
            this.dragonSilhouette.x = -200;
            this.dragonSilhouette.scale.x = Math.abs(this.dragonSilhouette.scale.x);
        }

        const speed = 40 + Math.random() * 30; // Very slow and majestic
        const endX = rtl ? -200 : 2100;

        gsap.to(this.dragonSilhouette, {
            x: endX,
            y: this.dragonSilhouette.y - 50 + Math.random() * 100, // drift up/down
            duration: speed,
            ease: "none", // Fixed string
            onComplete: () => {
                // Long delay between sightings
                setTimeout(() => this.flyDragon(), 15000 + Math.random() * 30000);
            }
        });
    }

    private addSunRays() {
        this.sunRayLayer = new PIXI.Graphics();

        // Draw some angled god rays
        this.sunRayLayer.beginFill(0xFFFFFF, 0.05); // Very faint white
        this.sunRayLayer.drawPolygon([
            -200, -100,
            300, -100,
            800, 1200,
            -600, 1200
        ]);

        this.sunRayLayer.beginFill(0xFFFFAA, 0.03); // Faint yellow
        this.sunRayLayer.drawPolygon([
            500, -100,
            800, -100,
            1600, 1200,
            900, 1200
        ]);
        this.sunRayLayer.endFill();

        this.sunRayLayer.blendMode = 'add';
        this.container.addChild(this.sunRayLayer);

        gsap.to(this.sunRayLayer, {
            alpha: 0.4,
            duration: 5,
            yoyo: true,
            repeat: -1,
            ease: "sine.inOut"
        });
    }

    public update(_deltaTime: number) {
        // Handled by GSAP
    }
}
