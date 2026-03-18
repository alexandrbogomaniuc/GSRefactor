import { BlurFilter, Container, Graphics, Text } from "pixi.js";

import { engine } from "@gamesv1/pixi-engine";

import buyBonusTablesData from "../../../../math/buy-bonus-tables.json";
import featureTablesData from "../../../../math/feature-tables.json";
import jackpotsData from "../../../../math/jackpots.json";
import paytableData from "../../../../math/paytable.json";

type RuleSection = {
  title: string;
  body: string;
};

const asRecord = (value: unknown): Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : {};

const asNumber = (value: unknown, fallback = 0): number =>
  typeof value === "number" && Number.isFinite(value) ? value : fallback;

const asString = (value: unknown, fallback = ""): string =>
  typeof value === "string" ? value : fallback;

const donorPaytable = asRecord(asRecord(paytableData).donorReferencePaytable);
const lineWinRule = asRecord(asRecord(paytableData).lineWinRule);
const baseGameRules = asRecord(asRecord(featureTablesData).baseGame);
const collectRules = asRecord(baseGameRules.collectRules);
const boostRules = asRecord(baseGameRules.boostRules);
const holdAndWinRules = asRecord(asRecord(featureTablesData).holdAndWinBonus);
const jackpotLevels = asRecord(asRecord(jackpotsData).levels);
const buyBonusTiers = Array.isArray(asRecord(buyBonusTablesData).tiers)
  ? (asRecord(buyBonusTablesData).tiers as Array<Record<string, unknown>>)
  : [];

const buildSections = (): RuleSection[] => {
  const paylines = Math.max(1, Math.round(asNumber(lineWinRule.stackedPaylines, 8)));
  const leftToRightRule = asString(lineWinRule.kind, "left_to_right_highest_per_line");
  const reelCount = Math.max(1, Math.round(asNumber(lineWinRule.reelCount, 3)));

  const buyTiers = buyBonusTiers
    .map((tier) => `${Math.round(asNumber(tier.tier, 0))}x`)
    .filter(Boolean)
    .join(" / ");

  return [
    {
      title: "SYMBOLS",
      body: [
        `777 ${asNumber(donorPaytable.SEVEN_777, 30)}x • Bell ${asNumber(donorPaytable.BELL, 20)}x • BAR ${asNumber(donorPaytable.BAR, 15)}x`,
        `Watermelon ${asNumber(donorPaytable.WATERMELON, 8)}x • Grapes ${asNumber(donorPaytable.GRAPES, 8)}x`,
        `Orange ${asNumber(donorPaytable.ORANGE, 2)}x • Lemon ${asNumber(donorPaytable.LEMON, 2)}x • Plum ${asNumber(donorPaytable.PLUM, 2)}x • Cherries ${asNumber(donorPaytable.CHERRIES, 1)}x`,
      ].join("\n"),
    },
    {
      title: "PAYLINES",
      body: [
        `Board: ${reelCount} reels x 4 rows`,
        `${paylines} fixed paylines, pays left-to-right, highest win per line.`,
        `Rule ID: ${leftToRightRule}.`,
      ].join("\n"),
    },
    {
      title: "COLLECT FEATURE",
      body: [
        "Chicken Coin and Super Chicken trigger Collect in base and bonus modes.",
        `Collect set: ${String(collectRules.collectsSymbolIds ?? "[7,8,9]")}`,
      ].join("\n"),
    },
    {
      title: "CHICKEN BOOST",
      body: [
        "Super Chicken can trigger Boost, attach jackpots, and add extra bonus coins.",
        `Boost multipliers: ${String(boostRules.multiplierOptions ?? "[2,3,5,7,10]")}`,
      ].join("\n"),
    },
    {
      title: "BONUS GAME",
      body: [
        "Trigger: Bonus Coin on reels 1 and 3 plus Chicken or Super Chicken on reel 2.",
        "Starts with 3 spins and resets to 3 when Bonus/Chicken/Super Chicken lands.",
        `Allowed bonus symbols: ${String(holdAndWinRules.allowedSymbolIds ?? "[7,8,9]")}`,
      ].join("\n"),
    },
    {
      title: "JACKPOTS",
      body: [
        `Mini ${asNumber(jackpotLevels.mini, 25)}x • Minor ${asNumber(jackpotLevels.minor, 50)}x`,
        `Major ${asNumber(jackpotLevels.major, 150)}x • Grand ${asNumber(jackpotLevels.grand, 1000)}x`,
      ].join("\n"),
    },
    {
      title: "BUY BONUS GAME",
      body: [
        `Tiers: ${buyTiers || "75x / 200x / 300x"}`,
        "Higher tiers force stronger bonus starts and higher Super Chicken density in provisional math.",
      ].join("\n"),
    },
  ];
};

export class RulesHelpPopup extends Container {
  private readonly backdrop = new Graphics();
  private readonly panel = new Container();
  private readonly panelBase = new Graphics();
  private readonly title = new Text({
    text: "RULES & HELP",
    style: {
      fill: 0xf7d98a,
      fontSize: 44,
      fontWeight: "900",
      letterSpacing: 1,
      fontFamily: "Trebuchet MS, Arial, sans-serif",
    },
  });
  private readonly subtitle = new Text({
    text: "Donor-rule parity view for internal benchmark",
    style: {
      fill: 0xd4d8e6,
      fontSize: 18,
      fontWeight: "600",
      fontFamily: "Trebuchet MS, Arial, sans-serif",
    },
  });
  private readonly body = new Container();
  private readonly closeButton = new Container();
  private readonly closeButtonBg = new Graphics();
  private readonly closeButtonLabel = new Text({
    text: "CLOSE",
    style: {
      fill: 0xffffff,
      fontSize: 24,
      fontWeight: "800",
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      letterSpacing: 1,
    },
  });

  constructor() {
    super();

    this.backdrop.eventMode = "static";
    this.backdrop.cursor = "pointer";
    this.backdrop.on("pointertap", () => {
      void engine().navigation.dismissPopup();
    });
    this.addChild(this.backdrop);

    this.addChild(this.panel);
    this.panel.addChild(this.panelBase);

    this.title.anchor.set(0.5, 0);
    this.subtitle.anchor.set(0.5, 0);
    this.panel.addChild(this.title, this.subtitle);

    this.panel.addChild(this.body);
    this.populateSections();

    this.closeButton.eventMode = "static";
    this.closeButton.cursor = "pointer";
    this.closeButton.on("pointertap", () => {
      void engine().navigation.dismissPopup();
    });
    this.closeButton.addChild(this.closeButtonBg, this.closeButtonLabel);
    this.closeButtonLabel.anchor.set(0.5);
    this.panel.addChild(this.closeButton);
  }

  public resize(width: number, height: number): void {
    this.backdrop.clear();
    this.backdrop.rect(0, 0, width, height);
    this.backdrop.fill({ color: 0x06080f, alpha: 0.78 });

    const panelWidth = Math.min(980, Math.max(820, width * 0.84));
    const panelHeight = Math.min(1120, Math.max(760, height * 0.9));
    this.panel.x = width * 0.5;
    this.panel.y = height * 0.5;

    this.panelBase.clear();
    this.panelBase.roundRect(-panelWidth * 0.5, -panelHeight * 0.5, panelWidth, panelHeight, 28);
    this.panelBase.fill({ color: 0x101625, alpha: 0.96 });
    this.panelBase.stroke({ color: 0xf3d07a, width: 3, alpha: 0.76 });

    this.title.y = -panelHeight * 0.5 + 28;
    this.subtitle.y = this.title.y + 58;

    this.body.x = -panelWidth * 0.5 + 40;
    this.body.y = -panelHeight * 0.5 + 120;

    this.closeButton.x = 0;
    this.closeButton.y = panelHeight * 0.5 - 54;
    this.closeButtonBg.clear();
    this.closeButtonBg.roundRect(-120, -28, 240, 56, 18);
    this.closeButtonBg.fill({ color: 0xb51f2b, alpha: 0.95 });
    this.closeButtonBg.stroke({ color: 0xffdf9a, width: 3, alpha: 0.95 });
    this.closeButtonLabel.position.set(0, 0);
  }

  public prepare(): void {
    // no-op
  }

  public async show(): Promise<void> {
    const currentEngine = engine();
    if (currentEngine.navigation.currentScreen) {
      currentEngine.navigation.currentScreen.filters = [new BlurFilter({ strength: 4 })];
    }
  }

  public async hide(): Promise<void> {
    const currentEngine = engine();
    if (currentEngine.navigation.currentScreen) {
      currentEngine.navigation.currentScreen.filters = [];
    }
  }

  private populateSections(): void {
    this.body.removeChildren();

    let cursorY = 0;
    const sections = buildSections();

    for (const section of sections) {
      const header = new Text({
        text: section.title,
        style: {
          fill: 0xf4d17d,
          fontSize: 24,
          fontWeight: "900",
          fontFamily: "Trebuchet MS, Arial, sans-serif",
          letterSpacing: 0.8,
        },
      });
      header.position.set(0, cursorY);
      this.body.addChild(header);
      cursorY += 30;

      const content = new Text({
        text: section.body,
        style: {
          fill: 0xf2f4ff,
          fontSize: 19,
          lineHeight: 28,
          wordWrap: true,
          wordWrapWidth: 860,
          fontFamily: "Trebuchet MS, Arial, sans-serif",
        },
      });
      content.position.set(0, cursorY);
      this.body.addChild(content);
      cursorY += content.height + 18;
    }
  }
}
