import { Container, Graphics, Sprite, Text, Texture } from "pixi.js";

import {
  CRAZY_ROOSTER_LAYOUT,
  CRAZY_ROOSTER_SYMBOL_FRAME_KEYS,
  CRAZY_ROOSTER_SYMBOL_LABELS,
} from "../config/CrazyRoosterGameConfig";
import {
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../app/assets/providerPackRegistry";

export class CrazyRoosterSymbol extends Container {
  private readonly shadow = new Graphics();
  private readonly backing = new Graphics();
  private readonly highlight = new Graphics();
  private readonly sprite = new Sprite();
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
  private readonly showDebugLabels =
    new URLSearchParams(window.location.search).get("debugSymbolLabels") === "1";
  private textureRequestToken = 0;

  public symbolId = -1;

  constructor() {
    super();

    this.sprite.width = CRAZY_ROOSTER_LAYOUT.symbolWidth - 20;
    this.sprite.height = CRAZY_ROOSTER_LAYOUT.symbolHeight - 20;
    this.sprite.x = 10;
    this.sprite.y = 10;
    this.sprite.alpha = 0.98;
    this.addChild(this.shadow);
    this.addChild(this.backing);
    this.addChild(this.sprite);
    this.addChild(this.highlight);

    this.labelText.anchor.set(0.5);
    this.labelText.x = CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5;
    this.labelText.y = CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5;
    this.labelText.visible = this.showDebugLabels;
    this.addChild(this.labelText);
  }

  public setSymbol(id: number): void {
    const normalized =
      ((id % CRAZY_ROOSTER_LAYOUT.symbolCount) + CRAZY_ROOSTER_LAYOUT.symbolCount) %
      CRAZY_ROOSTER_LAYOUT.symbolCount;

    this.symbolId = normalized;
    const palette = this.resolvePalette(normalized);
    this.shadow.clear();
    this.shadow.roundRect(
      6,
      8,
      CRAZY_ROOSTER_LAYOUT.symbolWidth - 12,
      CRAZY_ROOSTER_LAYOUT.symbolHeight - 10,
      22,
    );
    this.shadow.fill({ color: 0x000000, alpha: 0.18 });
    this.backing.clear();
    this.backing.roundRect(
      0,
      0,
      CRAZY_ROOSTER_LAYOUT.symbolWidth,
      CRAZY_ROOSTER_LAYOUT.symbolHeight,
      22,
    );
    this.backing.fill({ color: palette.frame, alpha: 0.95 });
    this.backing.stroke({ color: palette.border, width: 4 });
    this.highlight.clear();
    this.highlight.roundRect(
      8,
      8,
      CRAZY_ROOSTER_LAYOUT.symbolWidth - 16,
      34,
      18,
    );
    this.highlight.fill({ color: 0xffffff, alpha: 0.12 });
    this.sprite.texture = Texture.WHITE;
    this.sprite.tint = palette.fill;
    this.labelText.text = CRAZY_ROOSTER_SYMBOL_LABELS[normalized] ?? String(normalized);
    this.labelText.style.fill = palette.text;
    void this.applyResolvedTexture(normalized, palette.fill);
  }

  private resolvePalette(symbolId: number): {
    frame: number;
    fill: number;
    border: number;
    text: number;
  } {
    const isNanobanana = getProviderPackStatus().effectiveProvider === "nanobanana";
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
      frame: isNanobanana ? 0x0f0a08 : 0x15080a,
      fill,
      border: isNanobanana ? 0xf6dfaa : 0xc7141a,
      text: 0xffffff,
    };
  }

  private async applyResolvedTexture(symbolId: number, fallbackTint: number): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const frameKey = CRAZY_ROOSTER_SYMBOL_FRAME_KEYS[symbolId];
    const resolved = frameKey
      ? await resolveProviderFrameTexture("symbolAtlas", frameKey)
      : { texture: null };

    if (requestToken !== this.textureRequestToken || this.symbolId !== symbolId) {
      return;
    }

    if (resolved.texture) {
      this.sprite.texture = resolved.texture;
      this.sprite.tint = 0xffffff;
      return;
    }

    this.sprite.texture = Texture.WHITE;
    this.sprite.tint = fallbackTint;
  }
}
