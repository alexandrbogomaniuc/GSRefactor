import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';
import { Camera } from '../Camera';
import { GameConfig } from '../../game/GameConfig';

export class VFXManager {
    private container: PIXI.Container;
    private camera: Camera;

    constructor(container: PIXI.Container, camera: Camera) {
        this.container = container;
        this.camera = camera;
    }

    public playImpactShake() {
        this.camera.shake(
            GameConfig.Feel.ImpactShakeIntensityPX,
            GameConfig.Feel.ImpactShakeDurationMS
        );
    }

    public playBlockBreak(x: number, y: number, vfxType: string) {
        if (vfxType === 'none') return;

        let colorArray = [0x7c5b45, 0x9a7558]; // Dirt default

        if (vfxType === 'rock_chips') {
            colorArray = [0x666666, 0x888888];
        } else if (vfxType === 'sparkles_gold') {
            colorArray = [0xffd700, 0xffaa00, 0xffffff];
        } else if (vfxType === 'sparkles_blue') {
            colorArray = [0x00ffff, 0x0088ff, 0xffffff];
        }

        const particleCount = 12;

        for (let i = 0; i < particleCount; i++) {
            const p = new PIXI.Sprite(PIXI.Texture.WHITE);
            // Size from 4px to 10px
            const size = 4 + Math.random() * 6;
            p.width = size;
            p.height = size;
            p.anchor.set(0.5);

            p.tint = colorArray[Math.floor(Math.random() * colorArray.length)];

            p.x = x + (Math.random() * 20 - 10);
            p.y = y + (Math.random() * 20 - 10);

            this.container.addChild(p);

            // Random radial burst trajectory
            const angle = Math.random() * Math.PI * 2;
            const distance = 30 + Math.random() * 50;
            const destX = p.x + Math.cos(angle) * distance;
            const destY = p.y + Math.sin(angle) * distance;

            gsap.to(p, {
                x: destX,
                y: destY,
                alpha: 0,
                rotation: Math.random() * Math.PI * 4,
                duration: 0.3 + Math.random() * 0.3,
                ease: "power2.out",
                onComplete: () => {
                    if (p.parent) p.parent.removeChild(p);
                    p.destroy();
                }
            });
        }
    }

    public update(_deltaMS: number) {
        // No longer needed for GSAP managed tweens
    }
}
