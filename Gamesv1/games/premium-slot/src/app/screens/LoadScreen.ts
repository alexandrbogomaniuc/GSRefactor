import { Container } from "pixi.js";

import { engine } from "@gamesv1/pixi-engine";
import {
  createAudioCueRegistry,
  resolveAudioCueActions,
  WowPreloader,
  type AudioCueRegistry,
  type ShellThemeTokens,
} from "@gamesv1/ui-kit";

import { userSettings } from "../utils/userSettings";

export class LoadScreen extends Container {
  public static assetBundles = ["preload"];
  private static themeTokens: ShellThemeTokens | null = null;
  private static audioRegistry: AudioCueRegistry = createAudioCueRegistry();

  private readonly preloader: WowPreloader;
  private readonly proofHoldMs = Math.max(
    0,
    Number(new URLSearchParams(window.location.search).get("preloaderHoldMs") ?? "0") || 0,
  );
  private audioStingerPlayed = false;
  private shownAtMs = 0;
  private readonly reducedMotion =
    new URLSearchParams(window.location.search).get("motion") === "minimal" ||
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

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
  }

  public setBootPhase(label: string, progress: number): void {
    this.preloader.setStatus(label);
    this.preloader.setProgress(progress);
  }

  public beginAssetLoadPhase(): void {
    this.preloader.setStatus("LOADING ASSETS");
  }

  public onLoad(progress: number) {
    const weightedProgress = 20 + progress * 0.8;
    this.preloader.setStatus(progress >= 100 ? "READY" : "LOADING ASSETS");
    this.preloader.setProgress(weightedProgress);
  }

  public resize(width: number, height: number) {
    this.preloader.resize(width, height);
  }

  public update(ticker: { deltaMS: number }): void {
    this.preloader.tick(ticker.deltaMS);
  }

  public async show() {
    this.alpha = 1;
    this.shownAtMs = performance.now();
    this.preloader.setProgress(6);
    this.preloader.setStatus("CONNECTING TO GS");
    window.addEventListener("pointerdown", this.tryPlayAudioStinger, { once: true });
  }

  public async hide() {
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

  private readonly tryPlayAudioStinger = () => {
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
      engine().audio.sfx.play(action.assetKey, {
        volume: action.volume ?? 1,
      });
      this.audioStingerPlayed = true;
    }
  }
}
