import { Container, Graphics, Sprite, Text, Texture } from "pixi.js";

import {
  CRAZY_ROOSTER_LAYOUT,
  CRAZY_ROOSTER_SYMBOL_LABELS,
} from "../config/CrazyRoosterGameConfig";

export class CrazyRoosterSymbol extends Container {
  private readonly backing = new Graphics();
  private readonly sprite = new Sprite(Texture.WHITE);
  private readonly labelText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 30,
      fontWeight: "800",
      fill: 0xffffff,
      stroke: { color: 0x100f13, width: 4 },
      align: "center",
    },
  });

  public symbolId = -1;

  constructor(private readonly assetRoot: string) {
    super();

    this.sprite.width = CRAZY_ROOSTER_LAYOUT.symbolWidth - 14;
    this.sprite.height = CRAZY_ROOSTER_LAYOUT.symbolHeight - 14;
    this.sprite.x = 7;
    this.sprite.y = 7;
    this.sprite.alpha = 0.92;
    this.addChild(this.sprite);

    this.labelText.anchor.set(0.5);
    this.labelText.x = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
    this.labelText.y = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
    this.addChild(this.labelText);
  }

  public setSymbol(id: number): void {
    const normalized =
      ((id % CRAZY_ROOSTER_LAYOUT.symbolCount) + CRAZY_ROOSTER_LAYOUT.symbolCount) %
      CRAZY_ROOSTER_LAYOUT.symbolCount;

    this.symbolId = normalized;
    const palette = this.resolvePalette(normalized);
    this.backing.clear();
    this.backing.roundRect(
      0,
      0,
      CRAZY_ROOSTER_LAYOUT.symbolWidth,
      CRAZY_ROOSTER_LAYOUT.symbolHeight,
      22,
    );
    this.backing.fill(palette.frame);
    this.backing.stroke({ color: palette.border, width: 4 });
    if (!this.backing.parent) {
      this.addChildAt(this.backing, 0);
    }
    this.sprite.texture = Texture.WHITE;
    this.sprite.tint = palette.fill;
    this.labelText.text = CRAZY_ROOSTER_SYMBOL_LABELS[normalized] ?? String(normalized);
    this.labelText.style.fill = palette.text;
  }

  private resolvePalette(symbolId: number): {
    frame: number;
    fill: number;
    border: number;
    text: number;
  } {
    const isNanobanana = this.assetRoot.includes("nanobanana");
    const openAiPalette = [
      0xa1171f,
      0xd36c11,
      0xdb8b1c,
      0x7451c6,
      0x2d8f84,
      0x2f6ad8,
      0x4b4b4b,
      0xc2a03d,
      0x8e0d13,
      0xf3d24e,
    ];
    const nanoPalette = [
      0x93181c,
      0xe07d19,
      0x3e7d2d,
      0x7f49b7,
      0x16887a,
      0x335dd2,
      0x666666,
      0xd24f2b,
      0xb4171d,
      0xf0cf5c,
    ];
    const fill = (isNanobanana ? nanoPalette : openAiPalette)[symbolId] ?? 0x404040;

    return {
      frame: isNanobanana ? 0x131313 : 0x1a1212,
      fill,
      border: isNanobanana ? 0xffffff : 0xc7141a,
      text: 0xffffff,
    };
  }
}
