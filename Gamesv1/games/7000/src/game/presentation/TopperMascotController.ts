import {
  Assets,
  Container,
  Graphics,
  Rectangle,
  Sprite,
  Text,
  Texture,
  Ticker,
} from "pixi.js";
import { Spine } from "@esotericsoftware/spine-pixi-v8";
import type { Slot } from "@esotericsoftware/spine-pixi-v8";

import {
  getDonorLocalManifestUrl,
  getProviderPackStatus,
  resolveProviderFrameTexture,
} from "../../app/assets/providerPackRegistry";

export type MascotReactionState =
  | "idle"
  | "react_collect"
  | "react_boost_start"
  | "react_boost_loop"
  | "react_boost_finish"
  | "react_jackpot"
  | "react_bigwin";

const STATE_HOLDS_MS: Record<Exclude<MascotReactionState, "react_boost_loop">, number> = {
  idle: 0,
  react_collect: 560,
  react_boost_start: 320,
  react_boost_finish: 380,
  react_jackpot: 980,
  react_bigwin: 1100,
};

const readStatusDebugFlag = (): boolean => {
  const params = new URLSearchParams(window.location.search);
  return params.get("providerDebug") === "1" || params.get("uiStatus") === "1";
};

const resolveDonorManifestUrl = (): string =>
  new URL(getDonorLocalManifestUrl(), window.location.origin).toString();

export class TopperMascotController extends Container {
  private readonly aura = new Graphics();
  private readonly coinPile = new Container();
  private readonly donorChickenMask = new Graphics();
  private readonly mascotSprite = new Sprite(Texture.WHITE);
  private readonly statusText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 12,
      fontWeight: "700",
      fill: 0xffe2c8,
      stroke: { color: 0x180507, width: 3 },
      letterSpacing: 1,
      align: "center",
    },
  });
  private readonly motionTicker = (ticker: Ticker) => this.tick(ticker.deltaMS);

  private machineWidth = 0;
  private anchorX = 0;
  private anchorY = 12;
  private ambientTime = 0;
  private stateElapsedMs = 0;
  private reelImpact = 0;
  private currentState: MascotReactionState = "idle";
  private textureRequestToken = 0;
  private baseScaleX = 1;
  private baseScaleY = 1;
  private providerSnapshot = "";
  private coinTexture: Texture | null = null;
  private donorCoinTextures: Texture[] = [];
  private fallbackTexture: Texture | null = null;
  private donorSpine: Spine | null = null;
  private donorCoinBedSpine: Spine | null = null;
  private donorCoinSlots: Slot[] = [];
  private donorSuppressedSlots: Slot[] = [];
  private donorDollarPupilSlots: Slot[] = [];
  private donorSuppressedSlotNames = new Set<string>();
  private donorBaseScale = 0.228;
  private readonly showDebugState = readStatusDebugFlag();

  constructor() {
    super();

    this.mascotSprite.anchor.set(0.5, 0.86);
    // Keep the donor chicken mask renderable with near-zero alpha so Pixi can
    // compute stable mask bounds while still hiding the masked shape.
    this.donorChickenMask.alpha = 0.001;
    this.statusText.anchor.set(0.5);
    this.statusText.visible = this.showDebugState;

    this.addChild(this.aura, this.coinPile, this.donorChickenMask, this.mascotSprite, this.statusText);
    this.buildCoinPile();
    void this.refreshTexture();
    Ticker.shared.add(this.motionTicker);
  }

  public override destroy(options?: Parameters<Container["destroy"]>[0]): void {
    Ticker.shared.remove(this.motionTicker);
    super.destroy(options);
  }

  public resize(machineWidth: number, machineHeight: number): void {
    this.machineWidth = machineWidth;
    this.anchorX = machineWidth * 0.5;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    this.anchorY = isDonorlocal
      ? -24
      : Math.max(12, Math.min(20, machineHeight * 0.003 + 10));
    this.redrawBase();
  }

  public setState(state: MascotReactionState): void {
    this.currentState = state;
    this.stateElapsedMs = 0;
    this.reelImpact = Math.max(this.reelImpact, state === "idle" ? 0.06 : 0.46);
    this.statusText.text = state.replace(/_/g, " ").toUpperCase();
    this.applyDonorAnimation(state);
    void this.refreshTexture();
  }

  public pulseForReelStop(index: number): void {
    this.reelImpact = Math.max(this.reelImpact, 0.18 + (2 - index) * 0.07);
  }

  public getFocusPoint(): { x: number; y: number } {
    return { x: this.anchorX, y: this.anchorY - 54 };
  }

  private redrawBase(): void {
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const hasDonorSpine = isDonorlocal && this.donorSpine !== null;
    if (hasDonorSpine) {
      this.mascotSprite.visible = false;
      this.coinPile.visible = false;
      this.donorSpine!.visible = true;
      if (this.donorCoinBedSpine) {
        this.donorCoinBedSpine.visible = true;
        this.donorCoinBedSpine.x = this.anchorX;
        this.donorCoinBedSpine.y = this.anchorY + 16;
        this.donorCoinBedSpine.scale.set(this.donorBaseScale);
      }
      this.donorSpine!.x = this.anchorX;
      this.donorSpine!.y = this.anchorY + 16;
      this.donorSpine!.scale.set(this.donorBaseScale);
      this.updateDonorChickenMask();
      this.statusText.x = this.anchorX;
      this.statusText.y = this.anchorY + 84;
      return;
    }
    if (this.donorSpine) {
      this.donorSpine.visible = false;
    }
    if (this.donorCoinBedSpine) {
      this.donorCoinBedSpine.visible = false;
    }
    this.donorChickenMask.clear();
    const targetWidth = isDonorlocal ? 208 : 172;
    const targetHeight = isDonorlocal ? 198 : 172;
    const textureWidth = Math.max(1, this.mascotSprite.texture.width || targetWidth);
    const textureHeight = Math.max(1, this.mascotSprite.texture.height || targetHeight);
    const uniformScale = Math.min(targetWidth / textureWidth, targetHeight / textureHeight);
    this.baseScaleX = uniformScale;
    this.baseScaleY = uniformScale;

    this.mascotSprite.x = this.anchorX;
    this.mascotSprite.y = this.anchorY;
    this.mascotSprite.scale.set(this.baseScaleX, this.baseScaleY);
    this.coinPile.x = this.anchorX;
    this.coinPile.y = this.anchorY + (isDonorlocal ? 42 : 4);
    this.coinPile.visible = true;
    this.coinPile.alpha = isDonorlocal ? 0.5 : 0.9;
    this.coinPile.scale.set(isDonorlocal ? 1.12 : 1);
    this.coinPile.children.forEach((child, index) => {
      const coin = child as Sprite;
      const row = index < 8 ? 0 : 1;
      const rowIndex = row === 0 ? index : index - 8;
      if (isDonorlocal) {
        coin.position.set(
          row === 0 ? -84 + rowIndex * 23 : -68 + rowIndex * 23,
          row === 0 ? 40 : 57,
        );
        coin.width = row === 0 ? 28 + (index % 2) * 2 : 25;
        coin.height = coin.width;
        coin.alpha = row === 0 ? 0.44 : 0.36;
      } else {
        coin.position.set(row === 0 ? -92 + rowIndex * 24 : -76 + rowIndex * 24, row === 0 ? 14 : 28);
        coin.width = row === 0 ? 34 + (index % 3) * 4 : 30 + (index % 2) * 4;
        coin.height = coin.width;
        coin.alpha = row === 0 ? 0.88 : 0.8;
      }
    });
    this.statusText.x = this.anchorX;
    this.statusText.y = this.anchorY + 112;
  }

  private tick(deltaMs: number): void {
    if (this.machineWidth <= 0) {
      return;
    }

    const frameMs = Math.min(deltaMs, 50);
    this.ambientTime += frameMs / 1000;
    this.stateElapsedMs += frameMs;
    this.reelImpact = Math.max(0, this.reelImpact - frameMs / 350);

    const providerStatus = getProviderPackStatus();
    if (providerStatus.effectiveProvider !== this.providerSnapshot) {
      this.providerSnapshot = providerStatus.effectiveProvider;
      void this.refreshTexture();
    }

    if (
      this.currentState !== "idle" &&
      this.currentState !== "react_boost_loop" &&
      this.stateElapsedMs >=
        STATE_HOLDS_MS[this.currentState as Exclude<MascotReactionState, "react_boost_loop">]
    ) {
      this.setState("idle");
      return;
    }

    const pulseStrength = this.resolvePulseStrength();
    const motionBoost = this.resolveMotionBoost();
    const stateKick =
      this.currentState === "react_boost_start"
        ? 0.24
        : this.currentState === "react_boost_loop"
          ? 0.22
          : this.currentState === "react_jackpot"
            ? 0.2
            : this.currentState === "react_bigwin"
              ? 0.18
              : 0;
    const boostLoopMultiplier = this.currentState === "react_boost_loop" ? 1.4 : 1;
    const idlePeck =
      this.currentState === "idle"
        ? Math.max(0, Math.sin(this.ambientTime * 1.1 + 0.5)) * 0.18
        : 0;
    const floatY =
      Math.sin(this.ambientTime * (1.4 + motionBoost * 1.1)) *
        (6 + motionBoost * 3.8) *
        boostLoopMultiplier -
      this.reelImpact * (8 + motionBoost * 3.2) -
      stateKick * 8;
    const swayX = Math.sin(this.ambientTime * (1.2 + motionBoost * 1.1)) * (3 + motionBoost * 4.2);
    const scale =
      1 +
      pulseStrength * (0.13 + motionBoost * 0.05) +
      Math.sin(this.ambientTime * (1.8 + motionBoost)) * 0.028 +
      idlePeck * 0.1 +
      stateKick * 0.12;

    const auraActive = this.currentState !== "idle" || this.reelImpact > 0.12;
    this.aura.clear();
    if (auraActive) {
      this.aura.ellipse(
        this.anchorX,
        this.anchorY - 18 + floatY * 0.1,
        74 + pulseStrength * 36,
        28 + pulseStrength * 12,
      );
      this.aura.fill({ color: 0xffc96a, alpha: 0.05 + pulseStrength * 0.18 });
      this.aura.stroke({
        color: 0xe59f36,
        width: 1.4 + pulseStrength * 2.8,
        alpha: 0.1 + pulseStrength * 0.22,
      });
    }

    this.mascotSprite.x = this.anchorX + swayX;
    this.mascotSprite.y = this.anchorY + floatY + idlePeck * 7;
    this.mascotSprite.scale.set(
      this.baseScaleX * scale,
      this.baseScaleY * scale * (getProviderPackStatus().effectiveProvider === "donorlocal" ? 0.985 : 0.98),
    );
    this.mascotSprite.rotation =
      Math.sin(this.ambientTime * (2.2 + motionBoost * 0.8)) * (0.012 + motionBoost * 0.022) +
      idlePeck * 0.04;
    if (this.donorSpine && getProviderPackStatus().effectiveProvider === "donorlocal") {
      this.mascotSprite.visible = false;
      this.coinPile.visible = false;
      const donorScale = this.donorBaseScale * (1 + pulseStrength * 0.07 + stateKick * 0.09 + idlePeck * 0.03);
      if (this.donorCoinBedSpine) {
        this.donorCoinBedSpine.visible = true;
        this.donorCoinBedSpine.scale.set(this.donorBaseScale);
        this.donorCoinBedSpine.x = this.anchorX;
        this.donorCoinBedSpine.y = this.anchorY + 16;
      }
      this.donorSpine.visible = true;
      this.donorSpine.scale.set(donorScale);
      this.donorSpine.x = this.anchorX + swayX * 0.45;
      this.donorSpine.y = this.anchorY + 16 + floatY * 0.18 + idlePeck * 2.6;
      this.updateDonorChickenMask();
      return;
    }
    const donorLocalCoinBaseY = getProviderPackStatus().effectiveProvider === "donorlocal" ? 38 : -2;
    this.coinPile.y = this.anchorY + donorLocalCoinBaseY + floatY * 0.06;
    this.coinPile.x = this.anchorX + Math.sin(this.ambientTime * 1.4) * 2.1;
    const donorLocalCoinAlphaBase =
      getProviderPackStatus().effectiveProvider === "donorlocal" ? 0.42 : 0.68;
    this.coinPile.alpha = donorLocalCoinAlphaBase + pulseStrength * 0.2;
    this.coinPile.children.forEach((child, index) => {
      const coin = child as Sprite;
      const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
      const row = index < 8 ? 0 : 1;
      const baseY = isDonorlocal ? (row === 0 ? 34 : 50) : row === 0 ? 14 : 28;
      const shimmer = 0.7 + Math.sin(this.ambientTime * 3.8 + index * 0.9) * 0.3;
      coin.alpha = (isDonorlocal ? 0.28 : 0.52) + shimmer * (isDonorlocal ? 0.2 : 0.34);
      coin.y = baseY + Math.sin(index * 0.8 + this.ambientTime * 1.1) * (isDonorlocal ? 2.8 : 4);
    });
  }

  private resolvePulseStrength(): number {
    switch (this.currentState) {
      case "react_collect":
        return 0.42;
      case "react_boost_start":
      case "react_boost_loop":
      case "react_boost_finish":
        return 0.72;
      case "react_jackpot":
      case "react_bigwin":
        return 0.8;
      case "idle":
      default:
        return 0.2;
    }
  }

  private resolveMotionBoost(): number {
    switch (this.currentState) {
      case "react_boost_start":
      case "react_boost_loop":
        return 1.24;
      case "react_boost_finish":
        return 1.02;
      case "react_jackpot":
      case "react_bigwin":
        return 0.9;
      case "react_collect":
        return 0.45;
      case "idle":
      default:
        return 0.34;
    }
  }

  private async refreshTexture(): Promise<void> {
    const requestToken = ++this.textureRequestToken;
    const resolved = await resolveProviderFrameTexture("symbolAtlas", "symbol-9-rooster");
    if (requestToken !== this.textureRequestToken) {
      return;
    }
    this.fallbackTexture = resolved.texture;

    const status = getProviderPackStatus();
    let texture: Texture | null = this.fallbackTexture;
    this.coinTexture = null;

    if (status.effectiveProvider === "donorlocal") {
      const donorManifestUrl = resolveDonorManifestUrl();
      const donorTopperBodyUrl = new URL("./topper_body_crop.local.png", donorManifestUrl).toString();
      const donorSkeletonUrl = new URL("../anims_v5/gold_chicken.json", donorManifestUrl).toString();
      const donorAtlasUrl = new URL("../anims_v5/gold_chicken.atlas", donorManifestUrl).toString();
      const donorAtlasImageUrl = new URL("../anims_v5/gold_chicken.png", donorManifestUrl).toString();
      try {
        await Assets.load([
          donorTopperBodyUrl,
          donorSkeletonUrl,
          donorAtlasUrl,
          donorAtlasImageUrl,
        ]);
        if (this.donorSpine === null) {
          this.donorSpine = Spine.from({
            skeleton: donorSkeletonUrl,
            atlas: donorAtlasUrl,
            autoUpdate: true,
          });
          this.donorSpine.afterUpdateWorldTransforms = () => {
            this.hideDonorCoinSlots();
            this.hideSuppressedDonorSlots();
          };
          this.donorSpine.mask = this.donorChickenMask;
          this.captureDonorCoinSlots();
          this.captureSuppressedDonorSlots();
          this.donorSpine.state.timeScale = 1;
          this.addChildAt(this.donorSpine, this.getChildIndex(this.coinPile) + 1);
        }
        if (this.donorCoinBedSpine === null) {
          this.donorCoinBedSpine = Spine.from({
            skeleton: donorSkeletonUrl,
            atlas: donorAtlasUrl,
            autoUpdate: true,
          });
          this.donorCoinBedSpine.afterUpdateWorldTransforms = () => {
            this.hideNonDonorCoinSlots(this.donorCoinBedSpine);
          };
          this.donorCoinBedSpine.state.clearTracks();
          this.donorCoinBedSpine.state.timeScale = 0;
          this.donorCoinBedSpine.skeleton.setToSetupPose();
          this.donorCoinBedSpine.update(0);
          this.addChildAt(this.donorCoinBedSpine, this.getChildIndex(this.donorChickenMask));
        }
        if (this.donorCoinTextures.length === 0) {
          const atlasTexture = await Assets.load<Texture>(donorAtlasImageUrl);
          this.donorCoinTextures = [
            this.createDonorCoinTexture(atlasTexture, 1966, 324, 65, 77),
            this.createDonorCoinTexture(atlasTexture, 1403, 8, 80, 68),
            this.createDonorCoinTexture(atlasTexture, 1547, 27, 76, 72),
          ].filter((texture): texture is Texture => texture !== null);
        }
        this.applyDonorAnimation(this.currentState);
        texture = Texture.from(donorTopperBodyUrl);
        this.coinTexture = null;
      } catch {
        if (this.donorCoinBedSpine) {
          this.removeChild(this.donorCoinBedSpine);
          this.donorCoinBedSpine.destroy({ children: true });
          this.donorCoinBedSpine = null;
        }
        if (this.donorSpine) {
          this.removeChild(this.donorSpine);
          this.donorSpine.destroy({ children: true });
          this.donorSpine = null;
        }
        this.donorCoinSlots = [];
        this.donorSuppressedSlots = [];
        this.donorDollarPupilSlots = [];
        this.donorSuppressedSlotNames.clear();
        texture = this.fallbackTexture;
        this.coinTexture = null;
      }
    } else if (this.donorSpine) {
      this.donorSpine.visible = false;
      if (this.donorCoinBedSpine) {
        this.donorCoinBedSpine.visible = false;
      }
    }

    this.rebuildCoinPileTextures();

    const finalTexture = texture ?? Texture.WHITE;
    this.mascotSprite.texture = finalTexture;
    this.mascotSprite.tint = texture ? 0xffffff : 0xffd26c;
    this.providerSnapshot = status.effectiveProvider;
    this.redrawBase();
  }

  private applyDonorAnimation(state: MascotReactionState): void {
    if (!this.donorSpine) {
      return;
    }

    switch (state) {
      case "idle":
        this.donorSpine.state.setAnimation(0, "idle", true);
        break;
      case "react_collect":
        this.donorSpine.state.setAnimation(0, "action1", false);
        this.donorSpine.state.addAnimation(0, "idle", true, 0);
        break;
      case "react_boost_start":
        this.donorSpine.state.setAnimation(0, "action3_start", false);
        this.donorSpine.state.addAnimation(0, "action3_loop", true, 0);
        break;
      case "react_boost_loop":
        this.donorSpine.state.setAnimation(0, "action3_loop", true);
        break;
      case "react_boost_finish":
        this.donorSpine.state.setAnimation(0, "action3_finish", false);
        this.donorSpine.state.addAnimation(0, "idle", true, 0);
        break;
      case "react_jackpot":
      case "react_bigwin":
        this.donorSpine.state.setAnimation(0, "action2", false);
        this.donorSpine.state.addAnimation(0, "idle", true, 0);
        break;
      default:
        this.donorSpine.state.setAnimation(0, "idle", true);
    }
  }

  private buildCoinPile(): void {
    const total = 14;
    for (let index = 0; index < total; index += 1) {
      const coin = new Sprite(Texture.WHITE);
      coin.anchor.set(0.5);
      const row = index < 8 ? 0 : 1;
      const rowIndex = row === 0 ? index : index - 8;
      const x = row === 0 ? -92 + rowIndex * 24 : -76 + rowIndex * 24;
      const y = row === 0 ? 14 : 28;
      coin.position.set(x, y);
      coin.width = row === 0 ? 34 + (index % 3) * 4 : 30 + (index % 2) * 4;
      coin.height = coin.width;
      coin.alpha = row === 0 ? 0.88 : 0.8;
      this.coinPile.addChild(coin);
    }
    this.rebuildCoinPileTextures();
  }

  private rebuildCoinPileTextures(): void {
    this.coinPile.children.forEach((child, index) => {
      const coin = child as Sprite;
      const donorCoinTexture =
        this.donorCoinTextures.length > 0
          ? this.donorCoinTextures[index % this.donorCoinTextures.length]
          : null;
      coin.texture = donorCoinTexture ?? this.coinTexture ?? Texture.WHITE;
      coin.tint = donorCoinTexture
        ? 0xffffff
        : this.coinTexture
          ? index % 3 === 0
            ? 0xffefcc
            : 0xffffff
          : index % 2 === 0
            ? 0xefbf52
            : 0xf2d889;
      coin.visible = true;
    });
  }

  private captureDonorCoinSlots(): void {
    if (!this.donorSpine) {
      this.donorCoinSlots = [];
      return;
    }
    this.donorCoinSlots = this.donorSpine.skeleton.slots.filter((slot) => {
      const slotName = slot.data.name;
      const boneName = slot.bone.data.name;
      return slotName.includes("coin") || boneName === "coins" || boneName.startsWith("coin");
    });
  }

  private captureSuppressedDonorSlots(): void {
    if (!this.donorSpine) {
      this.donorSuppressedSlots = [];
      this.donorDollarPupilSlots = [];
      this.donorSuppressedSlotNames.clear();
      return;
    }
    this.donorDollarPupilSlots = this.donorSpine.skeleton.slots.filter((slot) => {
      const slotName = slot.data.name;
      const attachmentName = slot.getAttachment()?.name ?? slot.attachment?.name ?? "";
      return (
        slotName === "pupil_R2" ||
        slotName === "pupil_L2" ||
        attachmentName.includes("pupil_dollar")
      );
    });
    this.donorSuppressedSlots = this.donorSpine.skeleton.slots.filter((slot) => {
      const slotName = slot.data.name;
      return (
        slotName === "light_yellow" ||
        slotName === "fire" ||
        slotName === "fire2" ||
        slotName === "lighting_1" ||
        slotName === "lighting_2" ||
        slotName.startsWith("lighting_back")
      );
    });
    this.donorSuppressedSlotNames = new Set(this.donorSuppressedSlots.map((slot) => slot.data.name));
  }

  private hideDonorCoinSlots(): void {
    for (const slot of this.donorCoinSlots) {
      slot.color.a = 0;
    }
  }

  private hideSuppressedDonorSlots(): void {
    for (const slot of this.donorSuppressedSlots) {
      slot.color.a = 0;
      slot.setAttachment(null);
    }
    const showDollarPupils = this.currentState === "react_bigwin";
    for (const slot of this.donorDollarPupilSlots) {
      if (showDollarPupils) {
        continue;
      }
      slot.color.a = 0;
      slot.setAttachment(null);
    }
  }

  private hideNonDonorCoinSlots(spine: Spine | null): void {
    if (!spine) {
      return;
    }

    for (const slot of spine.skeleton.slots) {
      if (this.donorSuppressedSlotNames.has(slot.data.name)) {
        slot.color.a = 0;
        slot.setAttachment(null);
        continue;
      }
      if (this.isDonorCoinSlot(slot)) {
        slot.color.a = 1;
      } else {
        slot.color.a = 0;
      }
    }
  }

  private isDonorCoinSlot(slot: Slot): boolean {
    const slotName = slot.data.name;
    const boneName = slot.bone.data.name;
    return slotName.includes("coin") || boneName === "coins" || boneName.startsWith("coin");
  }

  private updateDonorChickenMask(): void {
    this.donorChickenMask.clear();
    if (!this.donorSpine || getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return;
    }

    // Show only the animated chicken body/head area; the coin bed below it is rendered
    // by the static donor coin pile layer so it stays locked in place.
    this.donorChickenMask.roundRect(
      this.anchorX - 206,
      this.anchorY - 352,
      412,
      382,
      40,
    );
    this.donorChickenMask.fill(0xffffff);
  }

  private createDonorCoinTexture(
    atlasTexture: Texture,
    x: number,
    y: number,
    width: number,
    height: number,
  ): Texture | null {
    const source = atlasTexture.source;
    if (!source) {
      return null;
    }

    return new Texture({
      source,
      frame: new Rectangle(x, y, width, height),
      orig: new Rectangle(0, 0, width, height),
      trim: new Rectangle(0, 0, width, height),
    });
  }

}
