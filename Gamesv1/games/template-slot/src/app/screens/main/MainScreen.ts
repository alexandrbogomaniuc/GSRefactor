import { FancyButton } from "@pixi/ui";
import { Container } from "pixi.js";

import { engine } from "../../getEngine";
import { PausePopup } from "../../popups/PausePopup";
import { SettingsPopup } from "../../popups/SettingsPopup";
import { Button, SlotMachine, GameConfig } from "@gs/slot-shell";
import { WinHighlight } from "../../../game/fx/WinHighlight";
import { ParticleBurst } from "../../../game/fx/ParticleBurst";
import { WinCounter } from "../../../game/ui/WinCounter";
// import { GameConfig } from "../../../game/config/GameConfig";
import { DebugOverlay } from "./DebugOverlay";

/** The screen that holds the app */
export class MainScreen extends Container {
  public static assetBundles = ["main"];

  private reelsLayer: Container;
  private fxLayer: Container;
  private uiLayer: Container;

  private slotMachine: SlotMachine;
  private winHighlight: WinHighlight;
  private particleBurst: ParticleBurst;
  private winCounter: WinCounter;
  private debugOverlay: DebugOverlay;

  private spinButton: Button;
  private pauseButton: FancyButton;
  private settingsButton: FancyButton;

  private paused = false;
  private isSpinning = false;

  constructor() {
    super();

    // 1) Setup Layered Rendering Order
    this.reelsLayer = new Container();
    this.fxLayer = new Container();
    this.uiLayer = new Container();

    this.addChild(this.reelsLayer);
    this.addChild(this.fxLayer);
    this.addChild(this.uiLayer);

    // 2) Instantiation
    this.slotMachine = new SlotMachine();
    this.reelsLayer.addChild(this.slotMachine);

    this.winHighlight = new WinHighlight();
    this.fxLayer.addChild(this.winHighlight);

    this.particleBurst = new ParticleBurst();
    this.fxLayer.addChild(this.particleBurst);

    this.winCounter = new WinCounter();
    this.uiLayer.addChild(this.winCounter);

    this.debugOverlay = new DebugOverlay();
    this.addChild(this.debugOverlay); // Attach direct to screen to avoid scaling

    // 3) UI Elements
    this.spinButton = new Button({ text: "SPIN", width: 200, height: 100 });
    this.spinButton.onPress.connect(() => this.handleSpin());
    this.uiLayer.addChild(this.spinButton);

    const buttonAnimation = {
      hover: { props: { scale: { x: 1.1, y: 1.1 } }, duration: 100 },
      pressed: { props: { scale: { x: 0.9, y: 0.9 } }, duration: 100 },
    };

    this.pauseButton = new FancyButton({
      defaultView: "icon-pause.png",
      anchor: 0.5,
      animations: buttonAnimation,
    });
    this.pauseButton.onPress.connect(() =>
      engine().navigation.presentPopup(PausePopup),
    );
    this.uiLayer.addChild(this.pauseButton);

    this.settingsButton = new FancyButton({
      defaultView: "icon-settings.png",
      anchor: 0.5,
      animations: buttonAnimation,
    });
    this.settingsButton.onPress.connect(() =>
      engine().navigation.presentPopup(SettingsPopup),
    );
    this.uiLayer.addChild(this.settingsButton);

    // Wire up events
    this.slotMachine.onSpinComplete = () => this.handleSpinComplete();
  }

  private handleSpin() {
    if (this.isSpinning) return;
    this.isSpinning = true;
    this.winHighlight.clear();

    // MOCK Sound
    console.log("SOUND: Play Reel Spin...");

    this.slotMachine.spin();
  }

  private handleSpinComplete() {
    // Mock evaluating a win
    const reels = this.slotMachine.getReels();
    // Select the middle visible symbols to mock a payline win
    const winSymbols = [
      reels[0].getVisibleSymbols()[1],
      reels[1].getVisibleSymbols()[1],
      reels[2].getVisibleSymbols()[1],
    ];

    console.log("SOUND: Play Win Fanfare...");
    this.winHighlight.showWin(winSymbols);

    // Spawn particles above the center
    this.particleBurst.play(0, -100);

    // Show massive counter
    this.winCounter.y = -200;
    this.winCounter.showWin(5000);

    setTimeout(() => {
      this.isSpinning = false; // Reset to allow spin again
    }, 2000);
  }

  public prepare() { }

  public update() {
    if (this.paused) return;
  }

  public async pause() {
    this.uiLayer.interactiveChildren = false;
    this.paused = true;
  }

  public async resume() {
    this.uiLayer.interactiveChildren = true;
    this.paused = false;
  }

  public reset() { }

  public resize(width: number, height: number) {
    const centerX = width * 0.5;
    const centerY = height * 0.5;

    // Center the layers
    const machineWidth =
      GameConfig.numReels * GameConfig.symbolWidth +
      (GameConfig.numReels - 1) * GameConfig.reelSpacing;
    const machineHeight =
      GameConfig.numRows * GameConfig.symbolHeight +
      (GameConfig.numRows - 1) * GameConfig.rowSpacing;

    const scale = Math.min(
      width / (machineWidth + 100),
      height / (machineHeight + 300),
    );

    // Scale layers uniformly
    this.reelsLayer.scale.set(scale);
    this.fxLayer.scale.set(scale);

    // Position reels layer
    this.reelsLayer.x = centerX - (machineWidth * scale) / 2;
    this.reelsLayer.y = centerY - (machineHeight * scale) / 2;

    // FX layer matches reels layer perfectly for correct local transforms mapping
    this.fxLayer.x = this.reelsLayer.x;
    this.fxLayer.y = this.reelsLayer.y;

    // UI Layer positioning
    this.uiLayer.x = centerX;
    this.uiLayer.y = centerY;

    this.spinButton.x = 0;
    this.spinButton.y = height / 2 - 100; // Bottom center

    this.pauseButton.x = -centerX + 50;
    this.pauseButton.y = -centerY + 50;
    this.settingsButton.x = centerX - 50;
    this.settingsButton.y = -centerY + 50;

    this.debugOverlay.resize(width, height);
  }

  public async show(): Promise<void> {
    engine().audio.bgm.play("main/sounds/bgm-main.mp3", { volume: 0.5 });
  }

  public async hide() { }

  public blur() {
    if (!engine().navigation.currentPopup) {
      engine().navigation.presentPopup(PausePopup);
    }
  }
}
