import { List } from "@pixi/ui";
import type { Text } from "pixi.js";
import { BlurFilter, Container, Sprite, Texture } from "pixi.js";

import { engine } from "@gamesv1/pixi-engine";

import { Button } from "../ui/Button.ts";
import { Label } from "../ui/Label.ts";
import { RoundedBox } from "../ui/RoundedBox.ts";
import { VolumeSlider } from "../ui/VolumeSlider.ts";

export class SettingsPopup extends Container {
  private bg: Sprite;
  private panel: Container;
  private title: Text;
  private doneButton: Button;
  private panelBase: RoundedBox;
  private versionLabel: Text;
  private layout: List;
  private masterSlider: VolumeSlider;
  private bgmSlider: VolumeSlider;
  private sfxSlider: VolumeSlider;

  constructor() {
    super();

    this.bg = new Sprite(Texture.WHITE);
    this.bg.tint = 0x0;
    this.bg.interactive = true;
    this.addChild(this.bg);

    this.panel = new Container();
    this.addChild(this.panel);

    this.panelBase = new RoundedBox({ height: 425 });
    this.panel.addChild(this.panelBase);

    this.title = new Label({
      text: "Settings",
      style: {
        fill: 0xec1561,
        fontSize: 50,
      },
    });
    this.title.y = -this.panelBase.boxHeight * 0.5 + 60;
    this.panel.addChild(this.title);

    this.doneButton = new Button({ text: "OK" });
    this.doneButton.y = this.panelBase.boxHeight * 0.5 - 78;
    this.doneButton.onPress.connect(() => engine().navigation.dismissPopup());
    this.panel.addChild(this.doneButton);

    this.versionLabel = new Label({
      text: `Version ${APP_VERSION}`,
      style: {
        fill: 0xffffff,
        fontSize: 12,
      },
    });
    this.versionLabel.alpha = 0.5;
    this.versionLabel.y = this.panelBase.boxHeight * 0.5 - 15;
    this.panel.addChild(this.versionLabel);

    this.layout = new List({ type: "vertical", elementsMargin: 4 });
    this.layout.x = -140;
    this.layout.y = -80;
    this.panel.addChild(this.layout);

    this.masterSlider = new VolumeSlider("Master Volume");
    this.masterSlider.onUpdate.connect((v: number) => {
      engine().audio.setMasterVolume(v / 100);
    });
    this.layout.addChild(this.masterSlider);

    this.bgmSlider = new VolumeSlider("BGM Volume");
    this.bgmSlider.onUpdate.connect((v: number) => {
      engine().audio.bgm.setVolume(v / 100);
    });
    this.layout.addChild(this.bgmSlider);

    this.sfxSlider = new VolumeSlider("SFX Volume");
    this.sfxSlider.onUpdate.connect((v: number) => {
      engine().audio.sfx.setVolume(v / 100);
    });
    this.layout.addChild(this.sfxSlider);
  }

  public resize(width: number, height: number) {
    this.bg.width = width;
    this.bg.height = height;
    this.panel.x = width * 0.5;
    this.panel.y = height * 0.5;
  }

  public prepare() {
    this.masterSlider.value = engine().audio.getMasterVolume() * 100;
    this.bgmSlider.value = engine().audio.bgm.getVolume() * 100;
    this.sfxSlider.value = engine().audio.sfx.getVolume() * 100;
  }

  public async show() {
    const currentEngine = engine();
    if (currentEngine.navigation.currentScreen) {
      currentEngine.navigation.currentScreen.filters = [
        new BlurFilter({ strength: 4 }),
      ];
    }

    this.bg.alpha = 0.8;
    this.panel.pivot.y = 0;
  }

  public async hide() {
    const currentEngine = engine();
    if (currentEngine.navigation.currentScreen) {
      currentEngine.navigation.currentScreen.filters = [];
    }

    this.bg.alpha = 0;
    this.panel.pivot.y = -500;
  }
}
