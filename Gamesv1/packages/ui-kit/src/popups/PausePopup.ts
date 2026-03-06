import { BlurFilter, Container, Sprite, Texture } from "pixi.js";

import { engine } from "@gamesv1/pixi-engine";

import { Button } from "../ui/Button.ts";
import { Label } from "../ui/Label.ts";
import { RoundedBox } from "../ui/RoundedBox.ts";

export class PausePopup extends Container {
  private bg: Sprite;
  private panel: Container;
  private title: Label;
  private doneButton: Button;
  private panelBase: RoundedBox;

  constructor() {
    super();

    this.bg = new Sprite(Texture.WHITE);
    this.bg.tint = 0x0;
    this.bg.interactive = true;
    this.addChild(this.bg);

    this.panel = new Container();
    this.addChild(this.panel);

    this.panelBase = new RoundedBox({ height: 300 });
    this.panel.addChild(this.panelBase);

    this.title = new Label({
      text: "Paused",
      style: { fill: 0xec1561, fontSize: 50 },
    });
    this.title.y = -80;
    this.panel.addChild(this.title);

    this.doneButton = new Button({ text: "Resume" });
    this.doneButton.y = 70;
    this.doneButton.onPress.connect(() => engine().navigation.dismissPopup());
    this.panel.addChild(this.doneButton);
  }

  public resize(width: number, height: number) {
    this.bg.width = width;
    this.bg.height = height;
    this.panel.x = width * 0.5;
    this.panel.y = height * 0.5;
  }

  public async show() {
    const currentEngine = engine();
    if (currentEngine.navigation.currentScreen) {
      currentEngine.navigation.currentScreen.filters = [
        new BlurFilter({ strength: 5 }),
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
