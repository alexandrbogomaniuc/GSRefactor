import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';
import { GameConfig } from '../../game/GameConfig';

export class Environment {
    public container: PIXI.Container;
    private sky!: PIXI.Graphics;
    private mtns!: PIXI.Graphics;
    private clouds: PIXI.Graphics[] = [];
    private ghast!: PIXI.Sprite;
    private chicken!: PIXI.Sprite;
    private groundContainer!: PIXI.Container;

    constructor(parentContainer: PIXI.Container) {
        this.container = new PIXI.Container();
        parentContainer.addChildAt(this.container, 0); // Always at the absolute back

        this.buildSky();
        this.buildMountains();
        this.buildClouds();
        this.buildGround();
        this.spawnMobs();
        this.add25DEffects();
    }

    private buildSky() {
        this.sky = new PIXI.Graphics();
        // Simple blue sky background
        this.sky.beginFill(0x87CEEB);
        this.sky.drawRect(0, 0, 1920, 1080);
        this.sky.endFill();
        this.container.addChild(this.sky);

        // A simple "sun"
        const sun = new PIXI.Graphics();
        sun.beginFill(0xFFFFFF);
        sun.drawRect(1400, 100, 80, 80);
        sun.endFill();
        this.container.addChild(sun);
    }

    private buildMountains() {
        // Far back mountains
        this.mtns = new PIXI.Graphics();
        this.mtns.beginFill(0xA9C2C2); // Light pixelated mountain color

        // Draw some blocky mountains stretching across 1920
        this.mtns.drawRect(100, 700, 200, 150);
        this.mtns.drawRect(200, 600, 150, 250);
        this.mtns.drawRect(600, 650, 180, 200);
        this.mtns.drawRect(1400, 500, 300, 350);
        this.mtns.drawRect(1600, 600, 200, 250);

        this.mtns.endFill();
        this.container.addChild(this.mtns);
    }

    private buildClouds() {
        for (let i = 0; i < 10; i++) {
            const cloud = new PIXI.Graphics();
            cloud.beginFill(0xFFFFFF, 0.8);
            const w = 150 + Math.random() * 200;
            const h = 50 + Math.random() * 40;
            cloud.drawRect(0, 0, w, h);
            cloud.endFill();

            cloud.x = Math.random() * 1920;
            cloud.y = 50 + Math.random() * 300;
            this.clouds.push(cloud);
            this.container.addChild(cloud);

            this.animateCloud(cloud);
        }
    }

    private animateCloud(cloud: PIXI.Graphics) {
        const speed = 20 + Math.random() * 40; // Seconds to cross screen
        gsap.to(cloud, {
            x: 1920 + cloud.width,
            duration: speed,
            ease: "none",
            onComplete: () => {
                cloud.x = -cloud.width;
                cloud.y = 50 + Math.random() * 300;
                this.animateCloud(cloud);
            }
        });
    }

    private buildGround() {
        this.groundContainer = new PIXI.Container();
        this.container.addChild(this.groundContainer);

        // A deep strip across the bottom corresponding to the floor under the grid
        const floorY = 550 + (GameConfig.MiningGrid.rows * GameConfig.MiningGrid.blockSize); // Under the grid

        // Draw a green upper lip
        const grassTop = new PIXI.Graphics();
        grassTop.beginFill(0x5E9E3B);
        grassTop.drawRect(0, floorY, 1920, 40);
        grassTop.endFill();
        this.groundContainer.addChild(grassTop);

        // Draw dirt below it
        const dirtFill = new PIXI.Graphics();
        dirtFill.beginFill(0x7c5b45);
        dirtFill.drawRect(0, floorY + 40, 1920, 1080 - (floorY + 40));
        dirtFill.endFill();
        this.groundContainer.addChild(dirtFill);
    }

    private spawnMobs() {
        try {
            this.ghast = new PIXI.Sprite(PIXI.Texture.from('/assets/ghast.png'));
            this.ghast.anchor.set(0.5);
            this.ghast.x = 1920 + 200;
            this.ghast.y = 200;
            this.ghast.scale.set(0.8);
            this.ghast.alpha = 0.8;
            this.container.addChild(this.ghast);

            this.flyGhast();

            this.chicken = new PIXI.Sprite(PIXI.Texture.from('/assets/chicken.png'));
            this.chicken.anchor.set(0.5, 1); // Anchor bottom center
            const floorY = 550 + (GameConfig.MiningGrid.rows * GameConfig.MiningGrid.blockSize);
            this.chicken.x = 1500;
            this.chicken.y = floorY + 10;
            this.chicken.scale.set(0.5); // Might be huge asset originally
            this.container.addChild(this.chicken);

            this.wanderChicken();
        } catch {
            console.warn("[Environment] Could not spawn ambient mobs. Assets missing.");
        }

        this.buildBirds();
    }

    private buildBirds() {
        // Draw 5 simple distant birds (two angled lines like a "V")
        for (let i = 0; i < 5; i++) {
            const bird = new PIXI.Graphics();
            bird.lineStyle(2, 0x222222, 0.8);
            bird.moveTo(0, 0);
            bird.lineTo(10, 5);
            bird.lineTo(20, 0);

            // Random start position
            bird.x = -50 - Math.random() * 500;
            bird.y = 100 + Math.random() * 200;
            bird.scale.set(0.5 + Math.random() * 0.5); // Distant size variation
            this.container.addChild(bird);

            this.animateBird(bird);
        }
    }

    private animateBird(bird: PIXI.Graphics) {
        const speed = 10 + Math.random() * 15; // 10-25s to cross screen

        // Simple bobbing flight path
        gsap.to(bird, {
            y: bird.y - 20,
            duration: 1,
            yoyo: true,
            repeat: -1,
            ease: "sine.inOut"
        });

        gsap.to(bird, {
            x: 1950,
            duration: speed,
            ease: "none",
            onComplete: () => {
                bird.x = -50;
                bird.y = 100 + Math.random() * 200;
                this.animateBird(bird);
            }
        });
    }

    private flyGhast() {
        if (!this.ghast) return;

        // Start offscreen right
        this.ghast.x = 1920 + 300;
        this.ghast.y = 100 + Math.random() * 200;

        const speed = 30 + Math.random() * 20; // slow floating

        gsap.to(this.ghast, {
            x: -300,
            duration: speed,
            ease: "none",
            onComplete: () => {
                // Wait a bit, then fly again
                setTimeout(() => this.flyGhast(), 5000 + Math.random() * 10000);
            }
        });

        // Add a secondary bobbing tween that repeats
        gsap.to(this.ghast, {
            y: this.ghast.y + 60,
            duration: 3 + Math.random(),
            ease: "sine.inOut",
            yoyo: true,
            repeat: Math.floor(speed / 3) + 1
        });
    }

    private wanderChicken() {
        if (!this.chicken) return;

        const moveTime = 1 + Math.random() * 2;
        const targetX = 1200 + Math.random() * 600;

        // Face direction
        this.chicken.scale.x = targetX > this.chicken.x ? 0.5 : -0.5;

        // Hopping motion
        const tl = gsap.timeline({
            onComplete: () => {
                setTimeout(() => this.wanderChicken(), 2000 + Math.random() * 3000);
            }
        });

        tl.to(this.chicken, {
            x: targetX,
            duration: moveTime,
            ease: "none"
        }, 0);

        // Small hops
        const hops = Math.floor(moveTime * 4);
        for (let i = 0; i < hops; i++) {
            tl.to(this.chicken, {
                y: this.chicken.y - 15,
                duration: moveTime / hops / 2,
                yoyo: true,
                repeat: 1
            }, i * (moveTime / hops));
        }
    }

    private add25DEffects() {
        // Subtle slow breathing pan for the sky
        gsap.to(this.sky, {
            x: -20,
            y: -10,
            scaleX: 1.05,
            scaleY: 1.05,
            duration: 15,
            ease: "sine.inOut",
            yoyo: true,
            repeat: -1
        });

        // Faster parallax pan for the mountains
        gsap.to(this.mtns, {
            x: -35,
            duration: 12,
            ease: "sine.inOut",
            yoyo: true,
            repeat: -1
        });
    }

    public update(_deltaTime: number) {
        // Any continuous tick updates if not handled by GSAP
    }
}
