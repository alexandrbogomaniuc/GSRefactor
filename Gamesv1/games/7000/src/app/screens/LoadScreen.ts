import { Container, Graphics, Sprite, Text, Texture } from "pixi.js";

import { engine } from "@gamesv1/pixi-engine";
import {
  createAudioCueRegistry,
  resolveAudioCueActions,
  WowPreloader,
  type AudioCueRegistry,
  type ShellThemeTokens,
} from "@gamesv1/ui-kit";

import { CRAZY_ROOSTER_DISPLAY_NAME, CRAZY_ROOSTER_FOOTER } from "../../game/config/CrazyRoosterGameConfig";
import { userSettings } from "../utils/userSettings";

export class LoadScreen extends Container {
  public static assetBundles = ["preload"];
  private static themeTokens: ShellThemeTokens | null = null;
  private static audioRegistry: AudioCueRegistry = createAudioCueRegistry();

  private readonly preloader: WowPreloader;
  private readonly wordmarkGlow = new Graphics();
  private readonly wordmarkPlate = new Graphics();
  private readonly wordmarkSprite = new Sprite(Texture.EMPTY);
  private readonly wordmarkText: Text;
  private readonly titleText: Text;
  private readonly subtitleText: Text;
  private readonly statusPill = new Graphics();
  private readonly statusText: Text;
  private readonly loadingTrack = new Graphics();
  private readonly loadingFill = new Graphics();
  private readonly loadingSheen = new Graphics();
  private readonly footerText: Text;
  private readonly proofHoldMs = Math.max(
    0,
    Number(new URLSearchParams(window.location.search).get("preloaderHoldMs") ?? "0") || 0,
  );
  private readonly reducedMotion =
    new URLSearchParams(window.location.search).get("motion") === "minimal" ||
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  private audioStingerPlayed = false;
  private shownAtMs = 0;
  private progress = 0;
  private statusLabel = "BOOTSTRAPPING";

  public static configure(input: {
    themeTokens: ShellThemeTokens;
    audioRegistry?: AudioCueRegistry;
  }): void {
    LoadScreen.themeTokens = input.themeTokens;
    LoadScreen.audioRegistry = input.audioRegistry ?? createAudioCueRegistry();
  }

  constructor() {
    super();

    this.eventMode = "static";
    this.preloader = new WowPreloader(LoadScreen.themeTokens ?? undefined, {
      reducedMotion: this.reducedMotion,
    });
    this.addChild(this.preloader);

    this.wordmarkSprite.anchor.set(0.5);
    this.wordmarkSprite.visible = false;

    this.addChild(this.wordmarkGlow);
    this.addChild(this.wordmarkPlate);
    this.addChild(this.wordmarkSprite);

    this.wordmarkText = new Text({
      text: CRAZY_ROOSTER_DISPLAY_NAME,
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 112,
        fontWeight: "900",
        fill: 0xfff4ee,
        stroke: { color: 0x140204, width: 8 },
        align: "center",
        letterSpacing: -2,
      },
    });
    this.wordmarkText.anchor.set(0.5);
    this.addChild(this.wordmarkText);

    this.titleText = new Text({
      text: "BETONLINE STUDIOS",
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 20,
        fontWeight: "800",
        fill: 0xffd98b,
        stroke: { color: 0x220408, width: 4 },
        align: "center",
        letterSpacing: 4,
      },
    });
    this.titleText.anchor.set(0.5);
    this.addChild(this.titleText);

    this.subtitleText = new Text({
      text: "CRAZY ROOSTER HOLD&WIN",
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 28,
        fontWeight: "900",
        fill: 0xffffff,
        stroke: { color: 0x220408, width: 5 },
        align: "center",
        letterSpacing: 1,
      },
    });
    this.subtitleText.anchor.set(0.5);
    this.addChild(this.subtitleText);

    this.statusText = new Text({
      text: this.statusLabel,
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 16,
        fontWeight: "900",
        fill: 0xffefe5,
        align: "center",
        letterSpacing: 2,
      },
    });
    this.statusText.anchor.set(0.5);
    this.addChild(this.statusPill, this.statusText, this.loadingTrack, this.loadingFill, this.loadingSheen);

    this.footerText = new Text({
      text: CRAZY_ROOSTER_FOOTER,
      style: {
        fontFamily: "Trebuchet MS, Arial, sans-serif",
        fontSize: 16,
        fontWeight: "700",
        fill: 0xf1d7d9,
        align: "center",
      },
    });
    this.footerText.anchor.set(0.5);
    this.addChild(this.footerText);
  }

  public setBootPhase(label: string, progress: number): void {
    this.preloader.setStatus(label);
    this.preloader.setProgress(progress);
    this.statusLabel = label.toUpperCase();
    this.progress = Math.max(this.progress, progress);
    this.redrawOverlay();
  }

  public beginAssetLoadPhase(): void {
    this.preloader.setStatus("LOADING ASSETS");
    this.statusLabel = "LOADING ASSETS";
    this.redrawOverlay();
  }

  public onLoad(progress: number): void {
    const weightedProgress = 20 + progress * 0.8;
    this.preloader.setStatus(progress >= 100 ? "READY" : "LOADING ASSETS");
    this.preloader.setProgress(weightedProgress);
    this.statusLabel = progress >= 100 ? "READY TO PLAY" : "LOADING ASSETS";
    this.progress = weightedProgress;
    this.redrawOverlay();
  }

  public resize(width: number, height: number): void {
    this.preloader.resize(width, height);
    const plateWidth = Math.min(width * 0.84, 980);
    const plateHeight = Math.min(height * 0.34, 290);
    const plateX = width * 0.5;
    const plateY = height * 0.4;

    this.wordmarkGlow.clear();
    this.wordmarkGlow.roundRect(
      plateX - plateWidth * 0.48,
      plateY - plateHeight * 0.42,
      plateWidth * 0.96,
      plateHeight * 0.84,
      42,
    );
    this.wordmarkGlow.stroke({ color: 0xc7141a, width: 12, alpha: 0.26 });

    this.wordmarkPlate.clear();
    this.wordmarkPlate.roundRect(
      plateX - plateWidth * 0.5,
      plateY - plateHeight * 0.5,
      plateWidth,
      plateHeight,
      42,
    );
    this.wordmarkPlate.fill({ color: 0x040409, alpha: 0.96 });
    this.wordmarkPlate.stroke({ color: 0xffd98b, width: 3, alpha: 0.85 });

    this.wordmarkText.x = plateX;
    this.wordmarkText.y = plateY - 8;
    const scale = Math.min(1, plateWidth / Math.max(1, this.wordmarkText.width + 72));
    this.wordmarkText.scale.set(scale);
    this.wordmarkSprite.x = plateX;
    this.wordmarkSprite.y = plateY - 8;
    this.wordmarkSprite.visible = false;
    this.wordmarkText.visible = true;

    this.titleText.x = width * 0.5;
    this.titleText.y = height * 0.17;
    this.subtitleText.x = width * 0.5;
    this.subtitleText.y = plateY + plateHeight * 0.38;
    this.footerText.x = width * 0.5;
    this.footerText.y = height - 38;
    this.statusText.x = width * 0.5;
    this.statusText.y = plateY + plateHeight * 0.62;

    this.redrawOverlay();
  }

  public update(ticker: { deltaMS: number }): void {
    this.preloader.tick(ticker.deltaMS);
    this.redrawOverlay(ticker.deltaMS / 1000);
  }

  public async show(): Promise<void> {
    this.alpha = 1;
    this.shownAtMs = performance.now();
    this.preloader.setProgress(6);
    this.preloader.setStatus("BOOTSTRAPPING");
    this.progress = 6;
    this.statusLabel = "BOOTSTRAPPING";
    this.redrawOverlay();
    window.addEventListener("pointerdown", this.tryPlayAudioStinger, { once: true });
  }

  public async hide(): Promise<void> {
    const elapsedMs = performance.now() - this.shownAtMs;
    const remainingHoldMs = this.proofHoldMs - elapsedMs;
    if (remainingHoldMs > 0) {
      await new Promise((resolve) => {
        window.setTimeout(resolve, remainingHoldMs);
      });
    }

    this.alpha = 0;
    window.removeEventListener("pointerdown", this.tryPlayAudioStinger);
  }

  private redrawOverlay(deltaSeconds = 0): void {
    const width = this.preloader.width || window.innerWidth || 1280;
    const height = this.preloader.height || window.innerHeight || 720;
    const trackWidth = Math.min(width * 0.5, 520);
    const trackHeight = 24;
    const trackX = width * 0.5 - trackWidth * 0.5;
    const trackY = height * 0.72;
    const fillWidth = Math.max(trackHeight, (trackWidth - 8) * (Math.max(0, Math.min(100, this.progress)) / 100));

    this.statusText.text = this.statusLabel;

    this.statusPill.clear();
    this.statusPill.roundRect(width * 0.5 - 132, height * 0.63 - 18, 264, 36, 18);
    this.statusPill.fill({ color: 0x130305, alpha: 0.92 });
    this.statusPill.stroke({ color: 0xffd98b, width: 2, alpha: 0.86 });

    this.loadingTrack.clear();
    this.loadingTrack.roundRect(trackX, trackY, trackWidth, trackHeight, 12);
    this.loadingTrack.fill({ color: 0x0d0608, alpha: 0.94 });
    this.loadingTrack.stroke({ color: 0xffd98b, width: 2, alpha: 0.78 });

    this.loadingFill.clear();
    this.loadingFill.roundRect(trackX + 4, trackY + 4, fillWidth, trackHeight - 8, 10);
    this.loadingFill.fill({ color: 0xc7141a, alpha: 0.96 });

    const shimmerX = (performance.now() / 4 + deltaSeconds * 1000) % Math.max(60, fillWidth + 60);
    this.loadingSheen.clear();
    this.loadingSheen.roundRect(trackX + 4 + Math.max(0, shimmerX - 28), trackY + 4, 36, trackHeight - 8, 10);
    this.loadingSheen.fill({ color: 0xffe8b8, alpha: fillWidth > 18 ? 0.22 : 0 });
  }

  private readonly tryPlayAudioStinger = (): void => {
    if (this.audioStingerPlayed) {
      return;
    }

    const cue = LoadScreen.themeTokens?.preloader.audioStingerCue;
    if (!cue || userSettings.getMasterVolume() <= 0) {
      return;
    }

    for (const action of resolveAudioCueActions(cue, LoadScreen.audioRegistry)) {
      if (action.type !== "sfx") {
        continue;
      }
      if (action.respectSoundEnabled && userSettings.getMasterVolume() <= 0) {
        continue;
      }
      try {
        engine().audio.sfx.play(action.assetKey, {
          volume: action.volume ?? 1,
        });
      } catch {
        // Preloader stingers are optional in the QA slice.
      }
      this.audioStingerPlayed = true;
    }
  };
}
