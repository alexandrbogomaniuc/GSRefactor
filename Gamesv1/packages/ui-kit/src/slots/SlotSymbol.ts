import { Container, Sprite, Texture, Text } from "pixi.js";
import { GameConfig } from "../config/GameConfig";

export class SlotSymbol extends Container {
  private sprite: Sprite;
  private symbolText: Text;
  public symbolId: number = -1;

  constructor() {
    super();

    // Generic placeholder texture (e.g., from a shared atlas)
    this.sprite = new Sprite(Texture.EMPTY);
    this.sprite.width = GameConfig.symbolWidth;
    this.sprite.height = GameConfig.symbolHeight;
    this.sprite.anchor.set(0.5);
    this.sprite.x = GameConfig.symbolWidth / 2;
    this.sprite.y = GameConfig.symbolHeight / 2;
    this.addChild(this.sprite);

    this.symbolText = new Text({
      text: "",
      style: {
        fontFamily: "Arial",
        fontSize: 36,
        fill: 0xffffff,
        fontWeight: "bold",
        stroke: { color: 0x000000, width: 2 }
      },
    });
    this.symbolText.anchor.set(0.5);
    this.symbolText.x = GameConfig.symbolWidth / 2;
    this.symbolText.y = GameConfig.symbolHeight / 2;
    this.addChild(this.symbolText);
  }

  public setSymbol(id: number) {
    this.symbolId = id;

    // Try to get texture from atlas, fallback to empty
    try {
      const textureName = `symbol_${id}`;
      this.sprite.texture = Texture.from(textureName);
    } catch {
      // Placeholder behavior if texture doesn't exist
      this.sprite.texture = Texture.WHITE;
      this.sprite.tint = 0x3d3d3d;
    }

    this.symbolText.text = `${id}`;
    this.scale.set(1);
  }

  public get height(): number {
    return GameConfig.symbolHeight;
  }
}

