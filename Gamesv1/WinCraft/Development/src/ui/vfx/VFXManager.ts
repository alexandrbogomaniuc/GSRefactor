import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';
import { Camera } from '../Camera';
import { GameConfig } from '../../game/GameConfig';

/**
 * VFXManager — Visual Effects Engine
 * 
 * Handles all particle effects, screen feedback, and visual "juice":
 * - Block break particles (block-type specific colors and shapes)
 * - Impact screen shake
 * - Flash overlay on big hits
 * - Floating damage/reward numbers
 * - Sparkle trail for precious blocks
 * - Win celebration particles
 */
export class VFXManager {
    private container: PIXI.Container;
    private camera: Camera;
    private flashOverlay: PIXI.Graphics;

    constructor(container: PIXI.Container, camera: Camera) {
        this.container = container;
        this.camera = camera;

        // Pre-create a full-screen flash overlay for impact effects
        this.flashOverlay = new PIXI.Graphics();
        this.flashOverlay.beginFill(0xFFFFFF);
        this.flashOverlay.drawRect(0, 0, 1920, 1080);
        this.flashOverlay.endFill();
        this.flashOverlay.alpha = 0;
        this.flashOverlay.zIndex = 9999;
        this.container.addChild(this.flashOverlay);
    }

    public playImpactShake() {
        this.camera.shake(
            GameConfig.Feel.ImpactShakeIntensityPX,
            GameConfig.Feel.ImpactShakeDurationMS
        );
    }

    /**
     * Enhanced block break with block-type specific debris
     */
    public playBlockBreak(x: number, y: number, vfxType: string) {
        if (vfxType === 'none') return;

        let colorArray = [0x7c5b45, 0x9a7558]; // Dirt default
        let particleCount = 14;
        let particleSize = { min: 4, max: 10 };
        let burstDistance = { min: 30, max: 60 };
        let addSparkle = false;

        switch (vfxType) {
            case 'dirt_crumble':
                colorArray = [0x7c5b45, 0x9a7558, 0x654321];
                particleSize = { min: 5, max: 12 };
                break;
            case 'grass_tear':
                colorArray = [0x4caf50, 0x66bb6a, 0x388e3c, 0x7c5b45];
                particleCount = 16;
                break;
            case 'rock_chips':
                colorArray = [0x666666, 0x888888, 0x555555, 0x999999];
                particleSize = { min: 3, max: 8 };
                burstDistance = { min: 20, max: 45 };
                break;
            case 'redstone_sparks':
                colorArray = [0xff0000, 0xcc0000, 0xff3333, 0xff6666];
                particleCount = 18;
                addSparkle = true;
                break;
            case 'sparkles_gold':
                colorArray = [0xffd700, 0xffaa00, 0xffffff, 0xffe066];
                particleCount = 20;
                particleSize = { min: 3, max: 7 };
                burstDistance = { min: 40, max: 80 };
                addSparkle = true;
                break;
            case 'sparkles_blue':
                colorArray = [0x00ffff, 0x0088ff, 0xffffff, 0x66ccff];
                particleCount = 22;
                burstDistance = { min: 50, max: 90 };
                addSparkle = true;
                break;
            case 'obsidian_shatter':
                colorArray = [0x1a1a2e, 0x2d2d44, 0x4a0080, 0x800080];
                particleCount = 24;
                particleSize = { min: 3, max: 6 };
                burstDistance = { min: 50, max: 100 };
                addSparkle = true;
                break;
            case 'dust':
                colorArray = [0xcccccc, 0xaaaaaa, 0x888888];
                particleCount = 8;
                particleSize = { min: 2, max: 5 };
                burstDistance = { min: 15, max: 30 };
                break;
        }

        // Spawn debris particles (simple radial burst, biased to fly sideways)
        for (let i = 0; i < particleCount; i++) {
            const p = new PIXI.Sprite(PIXI.Texture.WHITE);
            const size = particleSize.min + Math.random() * (particleSize.max - particleSize.min) + 3; // slightly larger chunks
            p.width = size;
            p.height = size;
            p.anchor.set(0.5);
            p.tint = colorArray[Math.floor(Math.random() * colorArray.length)];
            p.x = x;
            p.y = y;

            this.container.addChild(p);

            // Favor horizontal angles (sides: near 0 or PI)
            let angle = Math.random() * Math.PI * 2;
            const distance = burstDistance.min + Math.random() * (burstDistance.max - burstDistance.min);
            const destX = p.x + Math.cos(angle) * distance * 1.5; // push wider horizontally
            const destY = p.y + Math.sin(angle) * distance * 0.8; // less vertical spread

            gsap.to(p, {
                x: destX,
                y: destY,
                alpha: 0,
                rotation: (Math.random() - 0.5) * Math.PI * 4,
                duration: 0.2 + Math.random() * 0.15, // faster
                ease: "power2.out",
                onComplete: () => {
                    if (p.parent) p.parent.removeChild(p);
                    p.destroy();
                }
            });
        }

        // Small quick flash circle at center
        const flash = new PIXI.Graphics();
        flash.beginFill(0xFFFFAA, 0.8);
        flash.drawCircle(0, 0, 10);
        flash.endFill();
        flash.x = x;
        flash.y = y;
        this.container.addChild(flash);

        gsap.to(flash, {
            alpha: 0,
            width: 40,
            height: 40,
            duration: 0.15,
            ease: "power1.out",
            onComplete: () => {
                if (flash.parent) flash.parent.removeChild(flash);
                flash.destroy();
            }
        });

        // Sparkle trail for precious blocks
        if (addSparkle) {
            this.playSparkleTrail(x, y, colorArray[0]);
        }
    }

    /**
     * Glowing sparkle trail that lingers — used for gold, diamond, redstone blocks
     */
    private playSparkleTrail(x: number, y: number, color: number) {
        for (let i = 0; i < 8; i++) {
            const sparkle = new PIXI.Sprite(PIXI.Texture.WHITE);
            sparkle.width = 3;
            sparkle.height = 3;
            sparkle.anchor.set(0.5);
            sparkle.tint = color;
            sparkle.x = x + (Math.random() * 40 - 20);
            sparkle.y = y + (Math.random() * 40 - 20);
            sparkle.alpha = 0;

            this.container.addChild(sparkle);

            gsap.to(sparkle, {
                alpha: 1,
                duration: 0.1,
                delay: i * 0.05,
                onComplete: () => {
                    gsap.to(sparkle, {
                        y: sparkle.y - 30 - Math.random() * 20,
                        alpha: 0,
                        width: 0,
                        height: 0,
                        duration: 0.5 + Math.random() * 0.3,
                        ease: "power1.out",
                        onComplete: () => {
                            if (sparkle.parent) sparkle.parent.removeChild(sparkle);
                            sparkle.destroy();
                        }
                    });
                }
            });
        }
    }

    /**
     * Brief white flash overlay — sells the impact of a big hit
     */
    public playImpactFlash(intensity: number = 0.3) {
        this.flashOverlay.alpha = intensity;
        gsap.to(this.flashOverlay, {
            alpha: 0,
            duration: 0.15,
            ease: "power2.out"
        });
    }

    /**
     * Floating damage/reward number that rises and fades
     */
    public showFloatingText(x: number, y: number, text: string, color: number = 0xFFFFFF, fontSize: number = 24) {
        const style = new PIXI.TextStyle({
            fontFamily: 'Arial, sans-serif',
            fontSize: fontSize,
            fontWeight: 'bold',
            fill: color,
            stroke: { width: 3, color: 0x000000 },
            dropShadow: {
                color: 0x000000,
                blur: 2,
                angle: Math.PI / 4,
                distance: 2,
            }
        });

        const text_ = new PIXI.Text({ text, style });
        text_.anchor.set(0.5);
        text_.x = x;
        text_.y = y;
        text_.alpha = 0;

        this.container.addChild(text_);

        // Pop-in then float up and fade out
        gsap.timeline()
            .to(text_, {
                alpha: 1,
                y: y - 10,
                duration: 0.15,
                ease: "back.out(2)"
            })
            .to(text_, {
                y: y - 60,
                alpha: 0,
                duration: 0.8,
                ease: "power1.out",
                onComplete: () => {
                    if (text_.parent) text_.parent.removeChild(text_);
                    text_.destroy();
                }
            });
    }

    /**
     * Win celebration — burst of golden particles from center
     */
    public playWinBurst(x: number, y: number, magnitude: number = 1) {
        const count = Math.floor(20 * magnitude);
        const colors = [0xffd700, 0xffaa00, 0xffffff, 0xff6600, 0xffee44];

        for (let i = 0; i < count; i++) {
            const p = new PIXI.Sprite(PIXI.Texture.WHITE);
            const size = 3 + Math.random() * 5;
            p.width = size;
            p.height = size;
            p.anchor.set(0.5);
            p.tint = colors[Math.floor(Math.random() * colors.length)];
            p.x = x;
            p.y = y;

            this.container.addChild(p);

            const angle = Math.random() * Math.PI * 2;
            const distance = 80 + Math.random() * 120 * magnitude;
            const destX = x + Math.cos(angle) * distance;
            const destY = y + Math.sin(angle) * distance;

            gsap.to(p, {
                x: destX,
                y: destY + 40, // Gravity
                alpha: 0,
                rotation: Math.random() * Math.PI * 6,
                width: 0,
                height: 0,
                duration: 0.6 + Math.random() * 0.6,
                ease: "power2.out",
                delay: Math.random() * 0.2,
                onComplete: () => {
                    if (p.parent) p.parent.removeChild(p);
                    p.destroy();
                }
            });
        }
    }

    public update(_deltaMS: number) {
        // GSAP manages all tweens
    }
}
