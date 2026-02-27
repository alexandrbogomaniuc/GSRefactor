import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';

export class Camera {
    public container: PIXI.Container;
    private shakeTimeline: gsap.core.Timeline | null = null;
    private baseX: number = 0;
    private baseY: number = 0;

    constructor() {
        this.container = new PIXI.Container();
        // The camera container holds everything and moves to simulate camera action
    }

    public setBasePosition(x: number, y: number) {
        this.baseX = x;
        this.baseY = y;
        this.container.x = x;
        this.container.y = y;
    }

    /**
     * Vicious screen shake for heavy pickaxe impacts or explosions
     * @param intensity pixels to shift
     * @param durationMS time in milliseconds
     */
    public shake(intensity: number = 5, durationMS: number = 150) {
        if (this.shakeTimeline) {
            this.shakeTimeline.kill();
        }

        const durationS = durationMS / 1000;
        const shakes = Math.floor(durationMS / 30); // Shake every ~30ms

        this.shakeTimeline = gsap.timeline({
            onComplete: () => {
                // Return to true center
                gsap.to(this.container, { x: this.baseX, y: this.baseY, duration: 0.1 });
            }
        });

        for (let i = 0; i < shakes; i++) {
            const offsetX = (Math.random() - 0.5) * intensity * 2;
            const offsetY = (Math.random() - 0.5) * intensity * 2;

            this.shakeTimeline.to(this.container, {
                x: this.baseX + offsetX,
                y: this.baseY + offsetY,
                duration: durationS / shakes,
                ease: "rough({ template: none.out, strength: 1, points: 20, taper: none, randomize: true, clamp: false })"
            });
        }
    }

    /**
     * Cinematic Zoom into a specific point (e.g., a massive chest win)
     */
    public zoomTo(scale: number, durationMS: number = 500) {
        gsap.to(this.container.scale, {
            x: scale,
            y: scale,
            duration: durationMS / 1000,
            ease: "power2.out" // Smooth cinematic ease
        });
    }

    /**
     * Reset camera to default 1x scale
     */
    public resetZoom(durationMS: number = 500) {
        gsap.to(this.container.scale, {
            x: 1,
            y: 1,
            duration: durationMS / 1000,
            ease: "power2.inOut"
        });
    }
}
