import { Sprite, Texture } from "pixi.js";

import { randomBool, randomFloat, randomInt } from "@gamesv1/pixi-engine";

import { AppAssetKeys } from "../../assets/assetKeys";

export enum DIRECTION {
  NE,
  NW,
  SE,
  SW,
}

export class Logo extends Sprite {
  public direction!: DIRECTION;
  public speed!: number;

  get left() {
    return -this.width * 0.5;
  }

  get right() {
    return this.width * 0.5;
  }

  get top() {
    return -this.height * 0.5;
  }

  get bottom() {
    return this.height * 0.5;
  }

  constructor() {
    const textureAlias = randomBool()
      ? AppAssetKeys.LOGO_PRELOAD
      : AppAssetKeys.LOGO_ALT;

    super({ texture: Texture.from(textureAlias), anchor: 0.5, scale: 0.25 });
    this.direction = randomInt(0, 3);
    this.speed = randomFloat(1, 6);
  }
}
