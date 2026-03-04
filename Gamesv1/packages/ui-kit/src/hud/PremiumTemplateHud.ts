import { Container, Text } from "pixi.js";

import { flowLayout } from "@pixi/layout";

import {
  ResponsiveHudLayoutController,
  type HudLayoutViewport,
} from "../layout/HudLayout";
import { Button } from "../ui/Button";

export type PremiumHudControlId =
  | "spin"
  | "turbo"
  | "autoplay"
  | "buyFeature"
  | "sound"
  | "settings"
  | "history";

export interface PremiumHudVisibility {
  controls: Record<PremiumHudControlId, boolean>;
  metrics: {
    balance: boolean;
    bet: boolean;
    win: boolean;
  };
}

export interface PremiumHudVisibilityPatch {
  controls?: Partial<Record<PremiumHudControlId, boolean>>;
  metrics?: Partial<PremiumHudVisibility["metrics"]>;
}

export interface PremiumHudThemeTokens {
  visualStyle?: string;
  panelAlpha?: number;
  metricAccentColor?: number;
  controlSkinHooks?: Partial<Record<PremiumHudControlId, string>>;
}

export interface PremiumHudState {
  balance: number;
  bet: number;
  win: number;
  turboSelected: boolean;
  soundEnabled: boolean;
}

export interface PremiumHudCallbacks {
  onControlPress?: (controlId: PremiumHudControlId) => void;
}

const defaultVisibility: PremiumHudVisibility = {
  controls: {
    spin: true,
    turbo: true,
    autoplay: true,
    buyFeature: true,
    sound: true,
    settings: true,
    history: true,
  },
  metrics: {
    balance: true,
    bet: true,
    win: true,
  },
};

const formatMoney = (value: number): string =>
  Number.isFinite(value) ? Math.max(0, value).toFixed(2) : "0.00";

const createMetricText = (): Text =>
  new Text({
    text: "",
    style: {
      fontFamily: "Arial",
      fontSize: 24,
      fontWeight: "700",
      fill: 0xf7f2da,
      stroke: { color: 0x121212, width: 3 },
    },
  });

const metricRegionHeight = 76;

const defaultTheme: Required<PremiumHudThemeTokens> = {
  visualStyle: "premium-default",
  panelAlpha: 1,
  metricAccentColor: 0xf7f2da,
  controlSkinHooks: {},
};

export class PremiumTemplateHud extends Container {
  private readonly controlsLayer = new Container();
  private readonly metricsLayer = new Container();

  private readonly buttons: Record<PremiumHudControlId, Button>;
  private readonly metricTexts = {
    balance: createMetricText(),
    bet: createMetricText(),
    win: createMetricText(),
  };

  private readonly controlLayout: ResponsiveHudLayoutController;
  private visibility: PremiumHudVisibility = structuredClone(defaultVisibility);
  private theme: Required<PremiumHudThemeTokens> = { ...defaultTheme };
  private callbacks: PremiumHudCallbacks = {};

  constructor() {
    super();

    this.addChild(this.metricsLayer);
    this.addChild(this.controlsLayer);

    this.buttons = {
      spin: this.createControlButton("SPIN", "spin", 216, 100),
      turbo: this.createControlButton("TURBO OFF", "turbo"),
      autoplay: this.createControlButton("AUTO", "autoplay"),
      buyFeature: this.createControlButton("BUY", "buyFeature"),
      sound: this.createControlButton("SOUND ON", "sound"),
      settings: this.createControlButton("SETTINGS", "settings"),
      history: this.createControlButton("HISTORY", "history"),
    };

    for (const metricText of Object.values(this.metricTexts)) {
      metricText.anchor.set(0.5);
      this.metricsLayer.addChild(metricText);
    }

    this.controlLayout = new ResponsiveHudLayoutController([
      { id: "spin", object: this.buttons.spin, width: 216, height: 100, visible: true },
      { id: "turbo", object: this.buttons.turbo, width: 186, height: 84, visible: true },
      {
        id: "autoplay",
        object: this.buttons.autoplay,
        width: 186,
        height: 84,
        visible: true,
      },
      {
        id: "buyFeature",
        object: this.buttons.buyFeature,
        width: 186,
        height: 84,
        visible: true,
      },
      { id: "sound", object: this.buttons.sound, width: 186, height: 84, visible: true },
      {
        id: "settings",
        object: this.buttons.settings,
        width: 186,
        height: 84,
        visible: true,
      },
      {
        id: "history",
        object: this.buttons.history,
        width: 186,
        height: 84,
        visible: true,
      },
    ]);

    this.applyVisibility(defaultVisibility);
    this.applyTheme(defaultTheme);
    this.setState({
      balance: 0,
      bet: 0,
      win: 0,
      turboSelected: false,
      soundEnabled: true,
    });
  }

  public setCallbacks(callbacks: PremiumHudCallbacks): void {
    this.callbacks = callbacks;
  }

  public applyTheme(next: PremiumHudThemeTokens): void {
    this.theme = {
      visualStyle: next.visualStyle ?? this.theme.visualStyle,
      panelAlpha:
        typeof next.panelAlpha === "number" && Number.isFinite(next.panelAlpha)
          ? Math.min(1, Math.max(0, next.panelAlpha))
          : this.theme.panelAlpha,
      metricAccentColor:
        typeof next.metricAccentColor === "number" && Number.isFinite(next.metricAccentColor)
          ? next.metricAccentColor
          : this.theme.metricAccentColor,
      controlSkinHooks: {
        ...this.theme.controlSkinHooks,
        ...(next.controlSkinHooks ?? {}),
      },
    };

    this.controlsLayer.alpha = this.theme.panelAlpha;
    this.metricsLayer.alpha = this.theme.visualStyle === "minimal" ? 0.9 : 1;

    for (const metricText of Object.values(this.metricTexts)) {
      metricText.style.fill = this.theme.metricAccentColor;
    }

    for (const [controlId, button] of Object.entries(this.buttons) as Array<
      [PremiumHudControlId, Button]
    >) {
      const hook = this.theme.controlSkinHooks[controlId];
      switch (hook) {
        case "muted":
          button.alpha = 0.7;
          break;
        case "emphasis":
          button.alpha = 1;
          break;
        default:
          button.alpha = 0.95;
          break;
      }
    }
  }

  public applyVisibility(next: PremiumHudVisibilityPatch): void {
    this.visibility = {
      controls: {
        ...this.visibility.controls,
        ...(next.controls ?? {}),
      },
      metrics: {
        ...this.visibility.metrics,
        ...(next.metrics ?? {}),
      },
    };

    for (const [id, visible] of Object.entries(this.visibility.controls)) {
      this.controlLayout.setFeatureVisible(id, visible);
    }

    this.metricTexts.balance.visible = this.visibility.metrics.balance;
    this.metricTexts.bet.visible = this.visibility.metrics.bet;
    this.metricTexts.win.visible = this.visibility.metrics.win;
  }

  public setState(state: PremiumHudState): void {
    this.metricTexts.balance.text = `BAL ${formatMoney(state.balance)}`;
    this.metricTexts.bet.text = `BET ${formatMoney(state.bet)}`;
    this.metricTexts.win.text = `WIN ${formatMoney(state.win)}`;

    this.buttons.turbo.text = state.turboSelected ? "TURBO ON" : "TURBO OFF";
    this.buttons.sound.text = state.soundEnabled ? "SOUND ON" : "SOUND OFF";
  }

  public resize(viewport: HudLayoutViewport): void {
    this.controlLayout.apply(viewport);

    const metricItems = [
      {
        id: "balance",
        width: this.metricTexts.balance.width + 32,
        height: this.metricTexts.balance.height,
        visible: this.visibility.metrics.balance,
      },
      {
        id: "bet",
        width: this.metricTexts.bet.width + 32,
        height: this.metricTexts.bet.height,
        visible: this.visibility.metrics.bet,
      },
      {
        id: "win",
        width: this.metricTexts.win.width + 32,
        height: this.metricTexts.win.height,
        visible: this.visibility.metrics.win,
      },
    ];

    const metricLayout = flowLayout(metricItems, {
      width: viewport.width,
      height: metricRegionHeight,
      orientation: viewport.orientation,
      safeArea: viewport.safeArea,
      padding: {
        top: 10,
        left: 16,
        right: 16,
        bottom: 10,
      },
      gap: 12,
      rowGap: 8,
    });

    const metricPosition = new Map(metricLayout.items.map((item) => [item.id, item]));

    for (const [key, text] of Object.entries(this.metricTexts) as Array<
      [keyof typeof this.metricTexts, Text]
    >) {
      const positioned = metricPosition.get(key);
      if (!positioned) {
        text.visible = false;
        continue;
      }
      text.visible = true;
      text.x = positioned.x + positioned.width * 0.5;
      text.y = positioned.y + positioned.height * 0.5;
    }
  }

  private createControlButton(
    label: string,
    controlId: PremiumHudControlId,
    width = 186,
    height = 84,
  ): Button {
    const button = new Button({ text: label, width, height, fontSize: 22 });
    button.onPress.connect(() => {
      this.callbacks.onControlPress?.(controlId);
    });
    this.controlsLayer.addChild(button);
    return button;
  }
}
