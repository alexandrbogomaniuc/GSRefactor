import { Container, Graphics, GraphicsContext, Ticker } from "pixi.js";

interface Particle {
  sprite: Graphics;
  vx: number;
  vy: number;
  life: number;
  maxLife: number;
}

export class ParticleBurst extends Container {
  private particles: Particle[] = [];
  private pool: Graphics[] = [];
  private sharedContext: GraphicsContext;
  private isActive: boolean = false;

  constructor() {
    super();

    // Create one shared piece of geometry on the GPU
    this.sharedContext = new GraphicsContext();
    this.sharedContext.circle(0, 0, 5);
    this.sharedContext.fill({ color: 0xffffff, alpha: 1 });

    Ticker.shared.add((time) => this.tick(time.deltaMS / 1000));
  }

  public play(x: number, y: number) {
    this.isActive = true;

    // Spawn 20 particles
    for (let i = 0; i < 20; i++) {
      let p: Graphics;
      if (this.pool.length > 0) {
        p = this.pool.pop()!;
      } else {
        p = new Graphics(this.sharedContext); // Bind shared geometry
      }

      // Randomize per instance cheaply using transforms + tint
      p.scale.set(Math.random() * 0.8 + 0.6);
      p.tint = 0xffd700; // Gold coin color mock

      p.x = x;
      p.y = y;
      this.addChild(p);

      const angle = Math.random() * Math.PI * 2;
      const speed = Math.random() * 300 + 100;

      this.particles.push({
        sprite: p,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed - 200, // Pop upwards mostly
        life: 1.0 + Math.random(),
        maxLife: 2.0,
      });
    }
  }

  public clear(): void {
    for (const particle of this.particles) {
      if (particle.sprite.parent === this) {
        this.removeChild(particle.sprite);
        this.pool.push(particle.sprite);
      }
    }
    this.particles = [];
    this.isActive = false;
  }

  private tick(dt: number) {
    if (!this.isActive) return;

    let living = 0;
    this.particles.forEach((p) => {
      if (p.life > 0) {
        p.sprite.x += p.vx * dt;
        p.sprite.y += p.vy * dt;
        p.vy += 800 * dt; // Gravity

        p.life -= dt;

        // Fade out
        const alpha = Math.max(0, p.life / p.maxLife);
        p.sprite.alpha = alpha;

        living++;
      } else if (p.sprite.parent) {
        this.removeChild(p.sprite);
        this.pool.push(p.sprite);
      }
    });

    if (living === 0) {
      this.isActive = false;
    }
  }
}
