import { Assets, Container, Graphics, Sprite, Text, Texture } from "pixi.js";

import {
  DefaultShellThemeTokens,
  getBrandMonogram,
  type ShellThemeTokens,
} from "../theme/ShellThemeTokens";

export interface WowPreloaderOptions {
  reducedMotion?: boolean;
}

type ParticleState = {
  dot: Graphics;
  orbitRadius: number;
  angle: number;
  speed: number;
  alpha: number;
};

const clamp = (value: number, min: number, max: number): number =>
  Math.min(max, Math.max(min, value));

const parseHexColor = (value: string): number => Number.parseInt(value.slice(1, 7), 16);

const mixColors = (a: number, b: number, amount: number): number => {
  const ratio = clamp(amount, 0, 1);
  const ar = (a >> 16) & 0xff;
  const ag = (a >> 8) & 0xff;
  const ab = a & 0xff;
  const br = (b >> 16) & 0xff;
  const bg = (b >> 8) & 0xff;
  const bb = b & 0xff;
  const rr = Math.round(ar + (br - ar) * ratio);
  const rg = Math.round(ag + (bg - ag) * ratio);
  const rb = Math.round(ab + (bb - ab) * ratio);
  return (rr << 16) | (rg << 8) | rb;
};

export class WowPreloader extends Container {
  private readonly theme: ShellThemeTokens;
  private readonly background = new Graphics();
  private readonly glow = new Graphics();
  private readonly particleLayer = new Container();
  private readonly heroLayer = new Container();
  private readonly heroFx = new Graphics();
  private readonly logoFrame = new Graphics();
  private readonly logoSprite = new Sprite(Texture.EMPTY);
  private readonly logoFallback = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS",
      fontSize: 54,
      fontWeight: "800",
      fill: 0xffffff,
      align: "center",
      letterSpacing: 3,
    },
  });
  private readonly brandLabel = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS",
      fontSize: 30,
      fontWeight: "800",
      fill: 0xffffff,
      align: "center",
      letterSpacing: 2,
    },
  });
  private readonly phaseLabel = new Text({
    text: "PREPARING SESSION",
    style: {
      fontFamily: "Trebuchet MS",
      fontSize: 14,
      fontWeight: "700",
      fill: 0xf7f4e8,
      align: "center",
      letterSpacing: 4,
    },
  });
  private readonly progressTrack = new Graphics();
  private readonly progressFill = new Graphics();
  private readonly progressGlint = new Graphics();
  private readonly progressLabel = new Text({
    text: "0%",
    style: {
      fontFamily: "Trebuchet MS",
      fontSize: 18,
      fontWeight: "700",
      fill: 0xffffff,
      align: "center",
      letterSpacing: 2,
    },
  });

  private readonly particles: ParticleState[] = [];
  private readonly primaryColor: number;
  private readonly accentColor: number;

  private elapsed = 0;
  private progress = 0;
  private reducedMotion = false;
  private viewport = { width: 1280, height: 720 };

  constructor(
    theme: ShellThemeTokens = DefaultShellThemeTokens,
    options: WowPreloaderOptions = {},
  ) {
    super();

    this.theme = theme;
    this.reducedMotion = options.reducedMotion ?? false;
    this.primaryColor = parseHexColor(theme.brand.primaryColor);
    this.accentColor = parseHexColor(theme.brand.accentColor);

    this.logoSprite.anchor.set(0.5);
    this.logoSprite.visible = false;
    this.logoSprite.scale.set(0.52);

    this.logoFallback.anchor.set(0.5);
    this.logoFallback.text = getBrandMonogram(this.theme.brand.displayName);

    this.brandLabel.anchor.set(0.5, 0);
    this.brandLabel.text = this.theme.brand.displayName.toUpperCase();

    this.phaseLabel.anchor.set(0.5, 0.5);
    this.progressLabel.anchor.set(0.5);

    this.heroLayer.addChild(this.heroFx, this.logoFrame, this.logoSprite, this.logoFallback);
    this.addChild(
      this.background,
      this.glow,
      this.particleLayer,
      this.heroLayer,
      this.brandLabel,
      this.phaseLabel,
      this.progressTrack,
      this.progressFill,
      this.progressGlint,
      this.progressLabel,
    );

    this.rebuildParticles();
    this.redrawStatic();
    void this.resolveBrandLogo();
  }

  public setProgress(progress: number): void {
    this.progress = clamp(progress, 0, 100);
    this.progressLabel.text = `${Math.round(this.progress)}%`;
    this.redrawProgress();
  }

  public setStatus(status: string): void {
    this.phaseLabel.text = status.toUpperCase();
  }

  public setReducedMotion(reducedMotion: boolean): void {
    this.reducedMotion = reducedMotion;
    this.rebuildParticles();
    this.redrawStatic();
  }

  public resize(width: number, height: number): void {
    this.viewport.width = width;
    this.viewport.height = height;
    this.redrawStatic();
  }

  public tick(deltaMs: number): void {
    this.elapsed += deltaMs / 1000;
    this.animateParticles();
    this.animateHero();
    this.redrawProgress();
  }

  private rebuildParticles(): void {
    this.particleLayer.removeChildren();
    this.particles.length = 0;

    if (this.theme.preloader.style === "minimal" || this.reducedMotion) {
      return;
    }

    const count = Math.round(10 + this.theme.preloader.vfxIntensity * 12);
    for (let index = 0; index < count; index += 1) {
      const dot = new Graphics()
        .circle(0, 0, 4 + (index % 3))
        .fill({
          color: index % 2 === 0 ? this.accentColor : this.primaryColor,
          alpha: 0.45,
        });
      this.particleLayer.addChild(dot);
      this.particles.push({
        dot,
        orbitRadius: 110 + index * 9,
        angle: (Math.PI * 2 * index) / count,
        speed: 0.22 + index * 0.01,
        alpha: 0.25 + (index % 4) * 0.12,
      });
    }
  }

  private animateParticles(): void {
    const centerX = this.viewport.width * 0.5;
    const centerY = this.viewport.height * 0.42;
    const intensity =
      this.theme.preloader.style === "minimal" ? 0.08 : this.theme.preloader.vfxIntensity;

    for (const particle of this.particles) {
      particle.angle += particle.speed * 0.012 * (0.55 + intensity);
      const pulse = 1 + Math.sin(this.elapsed * 2.1 + particle.angle) * 0.12;
      particle.dot.x = centerX + Math.cos(particle.angle) * particle.orbitRadius * pulse;
      particle.dot.y =
        centerY + Math.sin(particle.angle * 1.18) * particle.orbitRadius * 0.55;
      particle.dot.alpha =
        particle.alpha * (0.8 + Math.sin(this.elapsed * 1.6 + particle.angle) * 0.2);
      particle.dot.scale.set(pulse);
    }
  }

  private animateHero(): void {
    this.heroFx.clear();

    const ringRadius = Math.min(this.viewport.width, this.viewport.height) * 0.12;
    const accentGlow = mixColors(this.primaryColor, this.accentColor, 0.6);
    const intensity = this.reducedMotion ? 0.18 : this.theme.preloader.vfxIntensity;
    const pulse = 1 + Math.sin(this.elapsed * 2.4) * 0.06;

    this.heroLayer.position.set(this.viewport.width * 0.5, this.viewport.height * 0.42);
    this.heroFx.rotation = 0;

    if (this.theme.preloader.style === "minimal" || this.reducedMotion) {
      this.heroFx
        .circle(0, 0, ringRadius * 1.18)
        .stroke({ width: 3, color: accentGlow, alpha: 0.25 + intensity * 0.2 });
      return;
    }

    if (this.theme.preloader.heroFx === "energyRing") {
      this.heroFx
        .circle(0, 0, ringRadius * 1.18 * pulse)
        .stroke({ width: 5, color: accentGlow, alpha: 0.42 + intensity * 0.2 })
        .circle(0, 0, ringRadius * 1.46)
        .stroke({ width: 2, color: this.primaryColor, alpha: 0.28 });

      for (let index = 0; index < 3; index += 1) {
        const angle = this.elapsed * (0.9 + intensity) + index * ((Math.PI * 2) / 3);
        const x = Math.cos(angle) * ringRadius * 1.18;
        const y = Math.sin(angle) * ringRadius * 1.18;
        this.heroFx.circle(x, y, 6 + intensity * 3).fill({
          color: index % 2 === 0 ? this.accentColor : this.primaryColor,
          alpha: 0.8,
        });
      }
      return;
    }

    if (this.theme.preloader.heroFx === "coinVortex") {
      for (let index = 0; index < 10; index += 1) {
        const angle = this.elapsed * (1.2 + intensity * 0.8) + index * 0.62;
        const radius = ringRadius * (0.35 + index * 0.09);
        const x = Math.cos(angle) * radius;
        const y = Math.sin(angle * 1.15) * radius * 0.72;
        this.heroFx
          .circle(x, y, 5 + (index % 2))
          .fill({ color: index % 2 === 0 ? this.primaryColor : this.accentColor, alpha: 0.76 });
      }
      this.heroFx
        .circle(0, 0, ringRadius * 1.34)
        .stroke({ width: 2, color: accentGlow, alpha: 0.24 });
      return;
    }

    this.heroFx.rotation = -0.28;
    const sweepShift = Math.sin(this.elapsed * 2.2) * ringRadius * 0.5;
    this.heroFx
      .roundRect(-ringRadius * 1.8 + sweepShift, -ringRadius * 0.66, ringRadius * 2.5, 22, 11)
      .fill({ color: this.accentColor, alpha: 0.28 + intensity * 0.2 })
      .roundRect(-ringRadius * 1.3 - sweepShift * 0.6, -2, ringRadius * 2.8, 18, 9)
      .fill({ color: accentGlow, alpha: 0.22 + intensity * 0.14 })
      .roundRect(-ringRadius * 1.6 + sweepShift * 0.4, ringRadius * 0.52, ringRadius * 2.4, 16, 8)
      .fill({ color: this.primaryColor, alpha: 0.18 + intensity * 0.12 });
  }

  private redrawStatic(): void {
    const { width, height } = this.viewport;
    const isPortrait = height >= width;
    const baseColor = mixColors(0x090a10, this.primaryColor, 0.16);
    const accentTint = mixColors(0x10131a, this.accentColor, 0.24);
    const ringRadius = Math.min(width, height) * 0.12;
    const brandWidth = Math.min(460, width - 72);
    const trackWidth = Math.min(440, width - 80);
    const trackHeight = 18;
    const progressY = height * 0.74;
    const heroY = height * (isPortrait ? 0.36 : 0.42);

    this.background.clear();
    this.background.rect(0, 0, width, height).fill({ color: baseColor });
    this.background.circle(width * 0.18, height * 0.22, Math.min(width, height) * 0.22).fill({
      color: accentTint,
      alpha: 0.12,
    });
    this.background.circle(width * 0.78, height * 0.74, Math.min(width, height) * 0.26).fill({
      color: this.primaryColor,
      alpha: 0.08,
    });

    this.glow.clear();
    this.glow
      .roundRect(width * 0.14, height * 0.14, width * 0.72, height * 0.58, 42)
      .stroke({ width: 1.5, color: this.accentColor, alpha: 0.14 })
      .fill({ color: accentTint, alpha: 0.05 });

    this.heroLayer.position.set(width * 0.5, heroY);

    this.logoFrame.clear();
    this.logoFrame
      .circle(0, 0, ringRadius * 0.86)
      .fill({ color: mixColors(0x11131a, this.accentColor, 0.12), alpha: 0.94 })
      .circle(0, 0, ringRadius * 0.86)
      .stroke({ width: 3, color: this.primaryColor, alpha: 0.5 });

    this.logoSprite.scale.set(ringRadius / 150);
    this.logoFallback.style.fill = this.primaryColor;
    this.logoFallback.style.fontSize = Math.round(clamp(ringRadius * 0.6, 40, 68));

    this.brandLabel.text = this.theme.brand.displayName.toUpperCase();
    this.brandLabel.style.fill = this.primaryColor;
    this.brandLabel.style.fontSize = isPortrait ? 30 : 28;
    this.brandLabel.x = width * 0.5;
    this.brandLabel.y = heroY + ringRadius + 42;
    this.brandLabel.style.wordWrap = true;
    this.brandLabel.style.wordWrapWidth = brandWidth;

    this.phaseLabel.x = width * 0.5;
    this.phaseLabel.y = progressY - 34;

    this.progressTrack.clear();
    this.progressTrack
      .roundRect(width * 0.5 - trackWidth / 2, progressY, trackWidth, trackHeight, 9)
      .fill({ color: 0xffffff, alpha: 0.12 });

    this.progressLabel.x = width * 0.5;
    this.progressLabel.y = progressY + 44;

    this.redrawProgress();
    this.animateHero();
  }

  private redrawProgress(): void {
    const { width, height } = this.viewport;
    const trackWidth = Math.min(440, width - 80);
    const trackHeight = 18;
    const progressY = height * 0.74;
    const fillWidth = Math.max(12, (trackWidth - 6) * (this.progress / 100));
    const glintTravel = ((this.elapsed * 180) % (fillWidth + 48)) - 24;

    this.progressFill.clear();
    this.progressFill
      .roundRect(width * 0.5 - trackWidth / 2 + 3, progressY + 3, fillWidth, trackHeight - 6, 7)
      .fill({ color: this.primaryColor, alpha: 0.96 });

    this.progressGlint.clear();
    this.progressGlint
      .roundRect(
        width * 0.5 - trackWidth / 2 + 3 + glintTravel,
        progressY + 4,
        46,
        trackHeight - 8,
        6,
      )
      .fill({
        color: this.accentColor,
        alpha: this.theme.preloader.style === "minimal" ? 0.18 : 0.34,
      });
  }

  private async resolveBrandLogo(): Promise<void> {
    const source = this.theme.brand.logoAssetKey ?? this.theme.brand.logoUrl;
    if (!source) {
      this.logoFallback.visible = true;
      this.logoSprite.visible = false;
      return;
    }

    try {
      if (this.theme.brand.logoUrl) {
        await Assets.load(source);
      }
      this.logoSprite.texture = Texture.from(source);
      this.logoSprite.visible = true;
      this.logoFallback.visible = false;
    } catch {
      this.logoFallback.visible = true;
      this.logoSprite.visible = false;
    }
  }
}
