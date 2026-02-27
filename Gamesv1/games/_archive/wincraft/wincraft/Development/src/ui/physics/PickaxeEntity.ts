import * as PIXI from 'pixi.js';
import { gsap } from 'gsap';
import { GameConfig } from '../../game/GameConfig';

export class PickaxeEntity {
    public sprite: PIXI.Sprite;
    public toolId: number;
    private damage: number;
    private durability: number;

    constructor(toolId: number) {
        this.toolId = toolId;

        let tex = PIXI.Texture.WHITE;
        try {
            const tool = Object.values(GameConfig.Tools).find((t: any) => t.id === toolId);
            if (tool && tool.texture) {
                tex = PIXI.Texture.from(`/assets/${tool.texture}`);
                this.damage = tool.damage;
                this.durability = tool.durability;
            } else {
                this.damage = 1;
                this.durability = 1;
            }
        } catch {
            this.damage = 1;
            this.durability = 1;
        }

        this.sprite = new PIXI.Sprite(tex);
        this.sprite.anchor.set(0.5); // Center anchor for rotation

        // Match size with grid blocks
        this.sprite.width = GameConfig.MiningGrid.blockSize * 0.8;
        this.sprite.height = GameConfig.MiningGrid.blockSize * 0.8;
    }

    /**
     * Executes the heavy arc drop from the top reel down to the mining block.
     * @param startX X coordinate of the top reel column
     * @param startY Y coordinate (usually just below the top reel)
     * @param targetX X coordinate of the target block
     * @param targetY Y coordinate of the target block
     * @returns A promise that resolves when the pickaxe hits the target
     */
    public async playDropAnimation(startX: number, startY: number, targetX: number, targetY: number): Promise<void> {
        this.sprite.x = startX;
        this.sprite.y = startY;
        this.sprite.alpha = 1;
        this.sprite.rotation = 0;

        return new Promise<void>((resolve) => {
            // A nice spinning arc down
            gsap.to(this.sprite, {
                x: targetX,
                y: targetY,
                rotation: Math.PI * 4, // 2 full spins
                duration: GameConfig.Feel.PickaxeDropTimeMS / 1000,
                ease: "power2.in",
                onComplete: () => {
                    resolve();
                }
            });
        });
    }

    /**
     * Executes a smaller, snappier "chop" animation when hacking a block
     */
    public async playChopAnimation(targetX: number, targetY: number): Promise<void> {
        // Move back and up slightly, then slam down
        return new Promise<void>((resolve) => {
            const tl = gsap.timeline({
                onComplete: () => resolve()
            });

            tl.to(this.sprite, {
                y: targetY - 20,
                x: targetX + 10,
                rotation: -Math.PI / 8, // Draw back
                duration: 0.15,
                ease: "power1.out"
            })
                .to(this.sprite, {
                    y: targetY,
                    x: targetX,
                    rotation: Math.PI / 4, // Slam forward
                    duration: 0.1,
                    ease: "power2.in"
                });
        });
    }

    /**
     * Applies damage to a block logic structure. Returns true if the pickaxe broke.
     */
    public applyHit(): boolean {
        this.durability -= 1;
        return this.durability <= 0;
    }

    public getDamage(): number {
        return this.damage;
    }

    public destroy() {
        gsap.killTweensOf(this.sprite);
        if (this.sprite && this.sprite.parent) {
            this.sprite.parent.removeChild(this.sprite);
        }
        this.sprite.destroy();
    }
}
