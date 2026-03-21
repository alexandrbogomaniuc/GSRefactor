import paylinesData from "../../../math/paylines.json";
import paytableData from "../../../math/paytable.json";
import reelModelData from "../../../math/reel-strips-or-weights.json";
import featureTablesData from "../../../math/feature-tables.json";
import buyBonusTablesData from "../../../math/buy-bonus-tables.json";
import jackpotsData from "../../../math/jackpots.json";
import winThresholdsData from "../../../math/win-thresholds.json";

const PROVISIONAL_REEL_COUNT = 3;
const PROVISIONAL_ROW_COUNT = 4;

const buildGridFromColumns = (columns: number[][]): number[][] =>
  Array.from({ length: PROVISIONAL_ROW_COUNT }, (_, rowIndex) =>
    Array.from({ length: PROVISIONAL_REEL_COUNT }, (_, reelIndex) => {
      return columns[reelIndex]?.[rowIndex] ?? 0;
    }),
  );

export type ProvisionalMathMode = "base" | "buy75" | "buy200" | "buy300";
export type ProvisionalMathPreset =
  | "normal"
  | "collect"
  | "boost"
  | "bonus"
  | "jackpot"
  | "mega";
export type ProvisionalWinTier = "none" | "big" | "huge" | "mega";
export type ProvisionalJackpotTier = "mini" | "minor" | "major" | "grand";

type WeightedNumberMap = Record<string, number>;

type PaylineDefinition = {
  lineId: number;
  rowsByReel: number[];
};

type BuyTierConfig = {
  modeKey: ProvisionalMathMode;
  costMultiplier: number;
  startingLockedSymbolRange: [number, number];
  startingTypeWeights: WeightedNumberMap;
  bonusLandingChanceByRespinIndex: number[];
  guaranteedChickenRange: [number, number];
  guaranteedSuperChickenCount: number;
  autoBoostOnEntry: boolean;
  jackpotTierWeightOverride?: WeightedNumberMap;
};

type HoldAndWinConfig = {
  entrySpins: number;
  maxGridSymbols: number;
  newSymbolChanceByRespinIndex: number[];
  newBonusSymbolWeights: WeightedNumberMap;
  coinValueMultipliers: number[];
  jackpotAttachChanceOnBonusCoin: number;
};

type JackpotLogicConfig = {
  tierSelectionWeights: WeightedNumberMap;
};

type BoostRulesConfig = {
  outcomeWeights: WeightedNumberMap;
  multiplierOptions: number[];
  extraCoinCountOptions: number[];
};

type BonusSymbolType = "bonus" | "chicken" | "superChicken";

type BonusLockedSymbol = {
  type: BonusSymbolType;
  value: number;
  tier?: ProvisionalJackpotTier;
  extraBonusCount?: number;
  boostApplied?: boolean;
  jackpotAttached?: boolean;
};

type PresetOverride = {
  columns: number[][];
  forceCollect?: boolean;
  forceBoost?: boolean;
  forceBonus?: boolean;
  forcedJackpotTier?: ProvisionalJackpotTier;
  minimumTotalMultiplier?: number;
  modeOverride?: ProvisionalMathMode;
};

export type ProvisionalMathLineWin = {
  lineId: number;
  rowsByReel: number[];
  symbolId: number;
  multiplier: number;
  amountMinor: number;
  positions: Array<{ reelIndex: number; rowIndex: number }>;
};

export type ProvisionalMathTimingHints = {
  reelStopDelaysMs: [number, number, number];
  lineHighlightDelayMs: number;
  lineHighlightDurationMs: number;
  featureStartDelayMs: number;
  featureLoopDurationMs: number;
  featureFinishDelayMs: number;
  coinFlyDurationMs: number;
};

export type ProvisionalMathOutcome = {
  mode: ProvisionalMathMode;
  preset: ProvisionalMathPreset;
  reelStops: number[][];
  symbolGrid: number[][];
  lineWins: ProvisionalMathLineWin[];
  lineWinMultiplier: number;
  lineWinAmountMinor: number;
  bonusWinMultiplier: number;
  totalWinMultiplier: number;
  totalWinAmountMinor: number;
  triggers: {
    collect: boolean;
    boost: boolean;
    bonus: boolean;
    jackpot: boolean;
  };
  jackpotTier: ProvisionalJackpotTier | null;
  jackpotLevel: number | null;
  winTier: ProvisionalWinTier;
  eventTriggers: string[];
  timingHints: ProvisionalMathTimingHints;
  counters: {
    buyFeatureAvailable: boolean;
    holdAndWinRemaining?: number;
    jackpotLevel?: number;
  };
  labels: Record<string, string>;
  messages: string[];
  soundCues: string[];
  animationCues: string[];
  generatorTrace: ProvisionalGeneratorTrace;
};

export type ProvisionalGeneratorTrace = {
  mode: ProvisionalMathMode;
  requestedPreset: ProvisionalMathPreset | null;
  effectivePreset: ProvisionalMathPreset | null;
  sourcePath: "weighted-reel-rng" | "canned-preset-override";
  seedStateBefore: number;
  seedStateAfter: number;
  rawGeneratedMatrix: number[][];
  finalPresentedMatrix: number[][];
  boardHash: string;
  postGenerationRewrite: boolean;
};

export type MathBridgePresentationHints = {
  source: "provisional";
  mode: ProvisionalMathMode;
  preset: ProvisionalMathPreset;
  winTier: ProvisionalWinTier;
  triggers: ProvisionalMathOutcome["triggers"];
  lineWins: ProvisionalMathLineWin[];
  eventTriggers: string[];
  timingHints: ProvisionalMathTimingHints;
  jackpotTier: ProvisionalJackpotTier | null;
  lineWinMultiplier: number;
  bonusWinMultiplier: number;
  totalWinMultiplier: number;
  generatorTrace: ProvisionalGeneratorTrace;
};

export type ProvisionalMathAdaptedResult = {
  winAmountMinor: number;
  reelStops: number[][];
  symbolGrid: number[][];
  uiMessages: string[];
  audioCues: string[];
  animationCues: string[];
  counters: Record<string, unknown>;
  labels: Record<string, string>;
  mathBridge: MathBridgePresentationHints;
};

type NextOutcomeInput = {
  totalBetMinor: number;
  mode: ProvisionalMathMode;
  requestedPreset?: ProvisionalMathPreset | null;
};

const asRecord = (value: unknown): Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : {};

const asNumber = (value: unknown, fallback = 0): number =>
  typeof value === "number" && Number.isFinite(value) ? value : fallback;

const asNumberArray = (value: unknown): number[] =>
  Array.isArray(value) ? value.map((entry) => asNumber(entry)).filter(Number.isFinite) : [];

const asWeightedNumberMap = (value: unknown): WeightedNumberMap => {
  const parsed = asRecord(value);
  const output: WeightedNumberMap = {};
  for (const [key, raw] of Object.entries(parsed)) {
    const normalized = asNumber(raw);
    if (normalized > 0) {
      output[key] = normalized;
    }
  }
  return output;
};

const asFeatureCoinWeights = (value: unknown): WeightedNumberMap => {
  const parsed = asWeightedNumberMap(value);
  return {
    bonus: asNumber(parsed.bonus, asNumber(parsed.coin, 0)),
    chicken: asNumber(parsed.chicken, asNumber(parsed.collector, 0)),
    superChicken: asNumber(parsed.superChicken, asNumber(parsed.jackpot, 0)),
  };
};

const asRange = (value: unknown, fallback: [number, number]): [number, number] => {
  const parsed = asNumberArray(value);
  if (parsed.length < 2) {
    return fallback;
  }
  const min = Math.max(0, Math.round(asNumber(parsed[0], fallback[0])));
  const max = Math.max(min, Math.round(asNumber(parsed[1], fallback[1])));
  return [min, max];
};

const asPaylines = (value: unknown): PaylineDefinition[] => {
  if (!Array.isArray(value)) {
    return [];
  }
  return value
    .map((entry) => {
      const parsed = asRecord(entry);
      return {
        lineId: asNumber(parsed.lineId),
        rowsByReel: asNumberArray(parsed.rowsByReel),
      };
    })
    .filter((line) => line.lineId > 0 && line.rowsByReel.length === 3);
};

const asBuyTiers = (value: unknown): BuyTierConfig[] => {
  if (!Array.isArray(value)) {
    return [];
  }
  return value
    .map((entry) => {
      const parsed = asRecord(entry);
      return {
        modeKey: String(parsed.modeKey ?? "base") as ProvisionalMathMode,
        costMultiplier: asNumber(parsed.costMultiplier, 0),
        startingLockedSymbolRange: asRange(parsed.startingLockedSymbolRange, [4, 6]),
        startingTypeWeights: asFeatureCoinWeights(parsed.startingTypeWeights),
        bonusLandingChanceByRespinIndex: asNumberArray(parsed.bonusLandingChanceByRespinIndex),
        guaranteedChickenRange: asRange(parsed.guaranteedChickenRange, [0, 0]),
        guaranteedSuperChickenCount: Math.max(0, Math.round(asNumber(parsed.guaranteedSuperChickenCount, 0))),
        autoBoostOnEntry: Boolean(parsed.autoBoostOnEntry),
        jackpotTierWeightOverride: asWeightedNumberMap(parsed.jackpotTierWeightOverride),
      };
    })
    .filter(
      (tier) =>
        tier.modeKey !== "base" &&
        tier.costMultiplier > 0 &&
        tier.startingLockedSymbolRange[0] > 0 &&
        tier.startingLockedSymbolRange[1] >= tier.startingLockedSymbolRange[0],
    );
};

const asReelWeights = (value: unknown): Array<Record<string, number>> => {
  if (!Array.isArray(value)) {
    return [];
  }
  return value
    .map((entry) => asWeightedNumberMap(asRecord(entry).symbolWeights))
    .filter((weights) => Object.keys(weights).length > 0);
};

const REEL_WEIGHTS = asReelWeights(asRecord(reelModelData).reels);
const PAYLINES = asPaylines(asRecord(paylinesData).paylines);
const ACTIVE_PAYLINES = Math.max(
  1,
  asNumber(asRecord(asRecord(paytableData).lineWinRule).stackedPaylines, 8),
);
const LINE_PAYOUTS = Object.entries(
  asRecord(asRecord(paytableData).linePayouts),
).reduce(
  (map, [symbolIdRaw, lineEntry]) => {
    const symbolId = Number(symbolIdRaw);
    if (!Number.isFinite(symbolId)) {
      return map;
    }
    const entry = asRecord(lineEntry);
    const payouts = asRecord(entry.pays);
    const payout3 = asNumber(payouts["3"]) / ACTIVE_PAYLINES;
    if (payout3 > 0) {
      map.set(symbolId, payout3);
    }
    return map;
  },
  new Map<number, number>(),
);

const HOLD_AND_WIN_CONFIG: HoldAndWinConfig = {
  entrySpins: asNumber(asRecord(asRecord(featureTablesData).holdAndWinBonus).entrySpins, 3),
  maxGridSymbols: asNumber(asRecord(asRecord(featureTablesData).holdAndWinBonus).maxGridSymbols, 12),
  newSymbolChanceByRespinIndex: asNumberArray(
    asRecord(asRecord(featureTablesData).holdAndWinBonus).newSymbolChanceByRespinIndex,
  ),
  newBonusSymbolWeights: asFeatureCoinWeights(
    asRecord(asRecord(featureTablesData).holdAndWinBonus).newBonusSymbolWeights,
  ),
  coinValueMultipliers: asNumberArray(
    asRecord(asRecord(featureTablesData).holdAndWinBonus).coinValueMultipliers,
  ),
  jackpotAttachChanceOnBonusCoin: asNumber(
    asRecord(asRecord(featureTablesData).holdAndWinBonus).jackpotAttachChanceOnBonusCoin,
    0,
  ),
};

const JACKPOT_LOGIC: JackpotLogicConfig = {
  tierSelectionWeights: asWeightedNumberMap(
    asRecord(asRecord(featureTablesData).jackpotTriggerLogic).tierSelectionWeights,
  ),
};

const BASE_BOOST_RULES = asRecord(asRecord(asRecord(featureTablesData).baseGame).boostRules);
const BOOST_RULES: BoostRulesConfig = {
  outcomeWeights: asWeightedNumberMap(BASE_BOOST_RULES.outcomeWeights),
  multiplierOptions: asNumberArray(BASE_BOOST_RULES.multiplierOptions),
  extraCoinCountOptions: asNumberArray(BASE_BOOST_RULES.extraCoinCountOptions),
};

const BUY_TIERS = asBuyTiers(asRecord(buyBonusTablesData).tiers);
const BUY_TIER_BY_MODE = new Map<ProvisionalMathMode, BuyTierConfig>(
  BUY_TIERS.map((entry) => [entry.modeKey, entry]),
);

const JACKPOT_LEVELS = asRecord(jackpotsData).levels as Record<ProvisionalJackpotTier, number>;
const WIN_THRESHOLDS = asRecord(winThresholdsData).thresholds as {
  big: number;
  huge: number;
  mega: number;
};

const BASE_SYMBOL_IDS = asRecord(asRecord(asRecord(featureTablesData).baseGame).symbolIds);
const WILD_SYMBOL_ID = 6;
const BONUS_COIN_SYMBOL_ID = asNumber(BASE_SYMBOL_IDS.bonusCoin, 7);
const CHICKEN_COIN_SYMBOL_ID = asNumber(BASE_SYMBOL_IDS.chickenCoin, 8);
const SUPER_CHICKEN_COIN_SYMBOL_ID = asNumber(BASE_SYMBOL_IDS.superChickenCoin, 9);
const BONUS_TRIGGER_RULE = asRecord(asRecord(asRecord(featureTablesData).baseGame).bonusTrigger);
const BASE_BONUS_TRIGGER_REELS = asNumberArray(BONUS_TRIGGER_RULE.bonusCoinReelIndexes);
const BASE_BONUS_CENTER_REEL_INDEX = asNumber(BONUS_TRIGGER_RULE.chickenTriggerReelIndex, 1);
const BASE_BONUS_CENTER_SYMBOL_IDS = asNumberArray(BONUS_TRIGGER_RULE.chickenTriggerSymbolIds);

const DONOR_REEL_STOP_NORMAL: [number, number, number] = [483, 567, 650];
const DONOR_REEL_STOP_BONUS_HOLD: [number, number, number] = [483, 567, 5000];

const PRESET_OVERRIDES: Partial<Record<ProvisionalMathPreset, PresetOverride>> = {
  normal: {
    columns: [
      [4, 6, 5, 1],
      [0, 4, 6, 5],
      [2, 3, 4, 6],
    ],
  },
  collect: {
    columns: [
      [7, 3, 5, 8],
      [0, 8, 4, 2],
      [5, 1, 7, 3],
    ],
    forceCollect: true,
  },
  boost: {
    columns: [
      [7, 6, 3, 9],
      [0, 9, 6, 8],
      [7, 1, 4, 6],
    ],
    forceCollect: true,
    forceBoost: true,
  },
  bonus: {
    columns: [
      [7, 8, 3, 1],
      [0, 9, 8, 2],
      [7, 3, 6, 8],
    ],
    forceCollect: true,
    forceBoost: true,
    forceBonus: true,
  },
  jackpot: {
    columns: [
      [7, 9, 3, 6],
      [8, 2, 9, 7],
      [7, 6, 9, 4],
    ],
    forceCollect: true,
    forceBoost: true,
    forceBonus: true,
    forcedJackpotTier: "major",
  },
  mega: {
    columns: [
      [6, 6, 6, 6],
      [6, 6, 6, 6],
      [6, 6, 6, 6],
    ],
    forceBonus: true,
    forceBoost: true,
    forcedJackpotTier: "grand",
    minimumTotalMultiplier: asNumber(WIN_THRESHOLDS.mega, 50) + 30,
    modeOverride: "buy300",
  },
};

const round3 = (value: number): number => Number(value.toFixed(3));

const toBoolLabel = (value: boolean): string => (value ? "true" : "false");

const formatCurrencyMinor = (valueMinor: number): string => `$${(valueMinor / 100).toFixed(2)}`;

const resolveJackpotLevel = (
  tier: ProvisionalJackpotTier | null,
): number | null => {
  if (!tier) {
    return null;
  }
  if (tier === "mini") return 1;
  if (tier === "minor") return 2;
  if (tier === "major") return 3;
  return 4;
};

const parsePreset = (value: string | null | undefined): ProvisionalMathPreset | null => {
  if (!value) {
    return null;
  }
  const normalized = value.trim().toLowerCase();
  if (
    normalized === "normal" ||
    normalized === "collect" ||
    normalized === "boost" ||
    normalized === "bonus" ||
    normalized === "jackpot" ||
    normalized === "mega"
  ) {
    return normalized;
  }
  return null;
};

const parseMode = (value: string | null | undefined): ProvisionalMathMode | null => {
  if (!value) {
    return null;
  }
  const normalized = value.trim().toLowerCase();
  if (
    normalized === "base" ||
    normalized === "buy75" ||
    normalized === "buy200" ||
    normalized === "buy300"
  ) {
    return normalized;
  }
  return null;
};

const weightedPick = (
  weights: WeightedNumberMap,
  randomFn: () => number,
): string => {
  const entries = Object.entries(weights);
  const total = entries.reduce((sum, [, weight]) => sum + weight, 0);
  if (entries.length === 0 || total <= 0) {
    return "";
  }
  let roll = randomFn() * total;
  for (const [key, weight] of entries) {
    roll -= weight;
    if (roll <= 0) {
      return key;
    }
  }
  return entries[entries.length - 1]?.[0] ?? "";
};

const randomIntInclusive = (
  min: number,
  max: number,
  randomFn: () => number,
): number => {
  if (max <= min) {
    return min;
  }
  return min + Math.floor(randomFn() * (max - min + 1));
};

const pickFromArray = (
  values: number[],
  randomFn: () => number,
  fallback: number,
): number => {
  if (values.length === 0) {
    return fallback;
  }
  const index = Math.max(
    0,
    Math.min(values.length - 1, Math.floor(randomFn() * values.length)),
  );
  return asNumber(values[index], fallback);
};

const normalizeColumns = (columns: number[][]): number[][] =>
  columns.map((column) => [...column].slice(0, 4).map((value) => Number(value) || 0));

const isBaseBonusTrigger = (columns: number[][]): boolean => {
  const [leftReelIndex, rightReelIndex] = BASE_BONUS_TRIGGER_REELS.length >= 2
    ? [BASE_BONUS_TRIGGER_REELS[0], BASE_BONUS_TRIGGER_REELS[1]]
    : [0, 2];
  const centerSymbols =
    BASE_BONUS_CENTER_SYMBOL_IDS.length > 0
      ? BASE_BONUS_CENTER_SYMBOL_IDS
      : [CHICKEN_COIN_SYMBOL_ID, SUPER_CHICKEN_COIN_SYMBOL_ID];

  const leftHasBonus = columns[leftReelIndex]?.some((symbolId) => symbolId === BONUS_COIN_SYMBOL_ID) ?? false;
  const rightHasBonus = columns[rightReelIndex]?.some((symbolId) => symbolId === BONUS_COIN_SYMBOL_ID) ?? false;
  const centerHasChicken =
    columns[BASE_BONUS_CENTER_REEL_INDEX]?.some((symbolId) => centerSymbols.includes(symbolId)) ?? false;

  return leftHasBonus && rightHasBonus && centerHasChicken;
};

const evaluateThreeSymbolLine = (values: number[]): { symbolId: number; multiplier: number } | null => {
  if (values.length < 3) {
    return null;
  }

  const allWild = values.every((symbolId) => symbolId === WILD_SYMBOL_ID);
  if (allWild) {
    return {
      symbolId: WILD_SYMBOL_ID,
      multiplier: LINE_PAYOUTS.get(WILD_SYMBOL_ID) ?? 0,
    };
  }

  let bestSymbol: number | null = null;
  let bestMultiplier = 0;
  for (const [symbolId, multiplier] of LINE_PAYOUTS.entries()) {
    const matched = values.every(
      (value) => value === symbolId || value === WILD_SYMBOL_ID,
    );
    if (!matched) {
      continue;
    }
    if (multiplier > bestMultiplier) {
      bestMultiplier = multiplier;
      bestSymbol = symbolId;
    }
  }

  if (bestSymbol === null || bestMultiplier <= 0) {
    return null;
  }
  return { symbolId: bestSymbol, multiplier: bestMultiplier };
};

const classifyWinTier = (multiplier: number): ProvisionalWinTier => {
  if (multiplier >= WIN_THRESHOLDS.mega) {
    return "mega";
  }
  if (multiplier >= WIN_THRESHOLDS.huge) {
    return "huge";
  }
  if (multiplier >= WIN_THRESHOLDS.big) {
    return "big";
  }
  return "none";
};

const createDefaultTimingHints = (bonusTriggered: boolean): ProvisionalMathTimingHints => ({
  reelStopDelaysMs: bonusTriggered ? DONOR_REEL_STOP_BONUS_HOLD : DONOR_REEL_STOP_NORMAL,
  lineHighlightDelayMs: 90,
  lineHighlightDurationMs: 760,
  featureStartDelayMs: bonusTriggered ? 220 : 140,
  featureLoopDurationMs: 1150,
  featureFinishDelayMs: 480,
  coinFlyDurationMs: 880,
});

const cloneColumns = (columns: number[][]): number[][] =>
  columns.map((column) => [...column]);

const sameColumns = (left: number[][], right: number[][]): boolean => {
  if (left.length !== right.length) {
    return false;
  }
  for (let columnIndex = 0; columnIndex < left.length; columnIndex += 1) {
    const leftColumn = left[columnIndex] ?? [];
    const rightColumn = right[columnIndex] ?? [];
    if (leftColumn.length !== rightColumn.length) {
      return false;
    }
    for (let rowIndex = 0; rowIndex < leftColumn.length; rowIndex += 1) {
      if (leftColumn[rowIndex] !== rightColumn[rowIndex]) {
        return false;
      }
    }
  }
  return true;
};

const createBoardHash = (columns: number[][]): string =>
  columns
    .map((column) => column.map((symbolId) => String(symbolId)).join(","))
    .join("|");

type HoldAndWinSimulationInput = {
  mode: ProvisionalMathMode;
  board: number[][] | null;
  randomFn: () => number;
  forcedJackpotTier?: ProvisionalJackpotTier;
};

type HoldAndWinSimulationResult = {
  bonusWinMultiplier: number;
  jackpotTierHits: Record<ProvisionalJackpotTier, number>;
  holdAndWinRemaining: number;
  collectTriggered: boolean;
  boostTriggered: boolean;
};

const resolveBonusSymbol = (
  typeRaw: BonusSymbolType,
  randomFn: () => number,
  jackpotWeights: WeightedNumberMap,
  forcedJackpotTier: ProvisionalJackpotTier | undefined,
  jackpotTierHits: Record<ProvisionalJackpotTier, number>,
  allowBoost = true,
): BonusLockedSymbol => {
  let value = pickFromArray(HOLD_AND_WIN_CONFIG.coinValueMultipliers, randomFn, 1);
  let tier: ProvisionalJackpotTier | undefined;
  let boostApplied = false;
  let jackpotAttached = false;
  let extraBonusCount = 0;

  if (typeRaw === "superChicken" && allowBoost) {
    const boostOutcome = weightedPick(
      Object.keys(BOOST_RULES.outcomeWeights).length > 0
        ? BOOST_RULES.outcomeWeights
        : { multiplierBoost: 62, jackpotAttach: 23, extraCoins: 15 },
      randomFn,
    );
    if (boostOutcome === "multiplierBoost") {
      const multiplier = pickFromArray(BOOST_RULES.multiplierOptions, randomFn, 2);
      value = round3(value * Math.max(1, multiplier));
      boostApplied = true;
    } else if (boostOutcome === "jackpotAttach") {
      tier =
        forcedJackpotTier && jackpotTierHits[forcedJackpotTier] === 0
          ? forcedJackpotTier
          : (weightedPick(jackpotWeights, randomFn) as ProvisionalJackpotTier);
      if (tier) {
        const jackpotValue = asNumber(JACKPOT_LEVELS[tier], 0);
        if (jackpotValue > 0) {
          value = round3(value + jackpotValue);
          jackpotTierHits[tier] += 1;
          jackpotAttached = true;
        }
      }
      boostApplied = true;
    } else if (boostOutcome === "extraCoins") {
      extraBonusCount = Math.max(
        0,
        Math.round(pickFromArray(BOOST_RULES.extraCoinCountOptions, randomFn, 2)),
      );
      boostApplied = extraBonusCount > 0;
    }
  } else if (
    typeRaw === "bonus" &&
    HOLD_AND_WIN_CONFIG.jackpotAttachChanceOnBonusCoin > 0 &&
    randomFn() < HOLD_AND_WIN_CONFIG.jackpotAttachChanceOnBonusCoin
  ) {
    tier =
      forcedJackpotTier && jackpotTierHits[forcedJackpotTier] === 0
        ? forcedJackpotTier
        : (weightedPick(jackpotWeights, randomFn) as ProvisionalJackpotTier);
    if (tier) {
      const jackpotValue = asNumber(JACKPOT_LEVELS[tier], 0);
      if (jackpotValue > 0) {
        value = round3(value + jackpotValue);
        jackpotTierHits[tier] += 1;
        jackpotAttached = true;
      }
    }
  }

  return {
    type: typeRaw,
    value,
    tier,
    boostApplied,
    jackpotAttached,
    extraBonusCount,
  };
};

const resolveFeatureTypeFromSymbolId = (symbolId: number): BonusSymbolType | null => {
  if (symbolId === BONUS_COIN_SYMBOL_ID) {
    return "bonus";
  }
  if (symbolId === CHICKEN_COIN_SYMBOL_ID) {
    return "chicken";
  }
  if (symbolId === SUPER_CHICKEN_COIN_SYMBOL_ID) {
    return "superChicken";
  }
  return null;
};

const simulateBaseCollectFromBoard = (
  board: number[][],
  randomFn: () => number,
  forcedJackpotTier?: ProvisionalJackpotTier,
): Pick<HoldAndWinSimulationResult, "bonusWinMultiplier" | "jackpotTierHits" | "collectTriggered" | "boostTriggered"> => {
  const jackpotTierHits: Record<ProvisionalJackpotTier, number> = {
    mini: 0,
    minor: 0,
    major: 0,
    grand: 0,
  };
  const jackpotWeights = JACKPOT_LOGIC.tierSelectionWeights;
  const locked: BonusLockedSymbol[] = [];
  let boostTriggered = false;

  for (const column of board) {
    for (const symbolId of column) {
      const type = resolveFeatureTypeFromSymbolId(symbolId);
      if (!type) {
        continue;
      }
      const resolved = resolveBonusSymbol(
        type,
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
        true,
      );
      boostTriggered ||= Boolean(resolved.boostApplied || resolved.jackpotAttached);
      locked.push(resolved);

      const extraCount = Math.max(0, Math.round(asNumber(resolved.extraBonusCount, 0)));
      for (let index = 0; index < extraCount; index += 1) {
        locked.push(
          resolveBonusSymbol(
            "bonus",
            randomFn,
            jackpotWeights,
            forcedJackpotTier,
            jackpotTierHits,
            false,
          ),
        );
      }
    }
  }

  const collectTriggered = locked.some(
    (entry) => entry.type === "chicken" || entry.type === "superChicken",
  );
  const totalValue = locked.reduce((sum, entry) => sum + entry.value, 0);

  return {
    bonusWinMultiplier: collectTriggered ? round3(totalValue) : 0,
    jackpotTierHits,
    collectTriggered,
    boostTriggered,
  };
};

const simulateHoldAndWin = ({
  mode,
  board,
  randomFn,
  forcedJackpotTier,
}: HoldAndWinSimulationInput): HoldAndWinSimulationResult => {
  const locked: BonusLockedSymbol[] = [];
  const jackpotTierHits: Record<ProvisionalJackpotTier, number> = {
    mini: 0,
    minor: 0,
    major: 0,
    grand: 0,
  };
  const buyTier = BUY_TIER_BY_MODE.get(mode);
  const jackpotWeights =
    buyTier?.jackpotTierWeightOverride &&
    Object.keys(buyTier.jackpotTierWeightOverride).length > 0
      ? buyTier.jackpotTierWeightOverride
      : JACKPOT_LOGIC.tierSelectionWeights;
  let boostTriggered = Boolean(buyTier?.autoBoostOnEntry);

  if (buyTier) {
    const [minStart, maxStart] = buyTier.startingLockedSymbolRange;
    let initialCount = randomIntInclusive(minStart, maxStart, randomFn);
    const [minChicken, maxChicken] = buyTier.guaranteedChickenRange;
    const guaranteedChicken = randomIntInclusive(minChicken, maxChicken, randomFn);
    const guaranteedSuper = Math.max(0, buyTier.guaranteedSuperChickenCount);

    for (let index = 0; index < guaranteedSuper; index += 1) {
      const resolved = resolveBonusSymbol(
        "superChicken",
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
      );
      boostTriggered ||= Boolean(resolved.boostApplied || resolved.jackpotAttached);
      locked.push(resolved);
    }
    for (let index = 0; index < guaranteedChicken; index += 1) {
      locked.push(
        resolveBonusSymbol(
          "chicken",
          randomFn,
          jackpotWeights,
          forcedJackpotTier,
          jackpotTierHits,
        ),
      );
    }

    initialCount = Math.max(initialCount, locked.length);
    const fillCount = Math.max(0, initialCount - locked.length);
    for (let index = 0; index < fillCount; index += 1) {
      const type = weightedPick(
        buyTier.startingTypeWeights,
        randomFn,
      ) as BonusSymbolType;
      const resolved = resolveBonusSymbol(
        type || "bonus",
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
      );
      boostTriggered ||= Boolean(resolved.boostApplied || resolved.jackpotAttached);
      locked.push(resolved);
    }
  } else if (board) {
    for (const column of board) {
      for (const symbolId of column) {
        const type = resolveFeatureTypeFromSymbolId(symbolId);
        if (!type) {
          continue;
        }
        const resolved = resolveBonusSymbol(
          type,
          randomFn,
          jackpotWeights,
          forcedJackpotTier,
          jackpotTierHits,
        );
        boostTriggered ||= Boolean(resolved.boostApplied || resolved.jackpotAttached);
        locked.push(resolved);

        const extraCount = Math.max(0, Math.round(asNumber(resolved.extraBonusCount, 0)));
        for (let extraIndex = 0; extraIndex < extraCount; extraIndex += 1) {
          locked.push(
            resolveBonusSymbol(
              "bonus",
              randomFn,
              jackpotWeights,
              forcedJackpotTier,
              jackpotTierHits,
              false,
            ),
          );
        }
      }
    }
  }

  let spinsLeft = HOLD_AND_WIN_CONFIG.entrySpins;
  while (spinsLeft > 0 && locked.length < HOLD_AND_WIN_CONFIG.maxGridSymbols) {
    const stageIndex = Math.max(
      0,
      Math.min(
        HOLD_AND_WIN_CONFIG.newSymbolChanceByRespinIndex.length - 1,
        HOLD_AND_WIN_CONFIG.entrySpins - spinsLeft,
      ),
    );
    const stageChance = buyTier
      ? asNumber(
          buyTier.bonusLandingChanceByRespinIndex[stageIndex],
          HOLD_AND_WIN_CONFIG.newSymbolChanceByRespinIndex[stageIndex] ?? 0.18,
        )
      : asNumber(HOLD_AND_WIN_CONFIG.newSymbolChanceByRespinIndex[stageIndex], 0.18);
    const emptySlots = HOLD_AND_WIN_CONFIG.maxGridSymbols - locked.length;
    let landedThisSpin = 0;
    const perSlotChance = Math.max(0, stageChance / 4);

    for (let index = 0; index < emptySlots; index += 1) {
      if (randomFn() < perSlotChance) {
        const type = weightedPick(
          HOLD_AND_WIN_CONFIG.newBonusSymbolWeights,
          randomFn,
        ) as BonusSymbolType;
        const resolved = resolveBonusSymbol(
          type || "bonus",
          randomFn,
          jackpotWeights,
          forcedJackpotTier,
          jackpotTierHits,
        );
        boostTriggered ||= Boolean(resolved.boostApplied || resolved.jackpotAttached);
        locked.push(resolved);
        landedThisSpin += 1;

        const extraCount = Math.max(0, Math.round(asNumber(resolved.extraBonusCount, 0)));
        for (
          let extraIndex = 0;
          extraIndex < extraCount && locked.length < HOLD_AND_WIN_CONFIG.maxGridSymbols;
          extraIndex += 1
        ) {
          locked.push(
            resolveBonusSymbol(
              "bonus",
              randomFn,
              jackpotWeights,
              forcedJackpotTier,
              jackpotTierHits,
              false,
            ),
          );
          landedThisSpin += 1;
        }
      }
    }

    if (landedThisSpin > 0) {
      spinsLeft = HOLD_AND_WIN_CONFIG.entrySpins;
    } else {
      spinsLeft -= 1;
    }
  }

  const bonusSum = locked
    .filter((entry) => entry.type === "bonus")
    .reduce((sum, entry) => sum + entry.value, 0);
  const chickenSum = locked
    .filter((entry) => entry.type === "chicken")
    .reduce((sum, entry) => sum + entry.value, 0);
  const superChickenSum = locked
    .filter((entry) => entry.type === "superChicken")
    .reduce((sum, entry) => sum + entry.value, 0);

  const collectTriggered = chickenSum > 0 || superChickenSum > 0;
  const collectWin = collectTriggered ? round3(chickenSum + superChickenSum) : 0;
  const bonusWinMultiplier = round3(bonusSum + collectWin);
  return {
    bonusWinMultiplier,
    jackpotTierHits,
    holdAndWinRemaining: Math.max(0, spinsLeft),
    collectTriggered,
    boostTriggered,
  };
};

const resolveHighestJackpotTier = (
  hits: Record<ProvisionalJackpotTier, number>,
): ProvisionalJackpotTier | null => {
  if (hits.grand > 0) return "grand";
  if (hits.major > 0) return "major";
  if (hits.minor > 0) return "minor";
  if (hits.mini > 0) return "mini";
  return null;
};

export class ProvisionalMathSource {
  private rngState: number;

  constructor(seedInput: number) {
    this.rngState = seedInput >>> 0;
  }

  private nextRandom = (): number => {
    this.rngState = (this.rngState + 0x6d2b79f5) >>> 0;
    let value = Math.imul(this.rngState ^ (this.rngState >>> 15), this.rngState | 1);
    value ^= value + Math.imul(value ^ (value >>> 7), value | 61);
    return ((value ^ (value >>> 14)) >>> 0) / 4294967296;
  };

  private getRngState(): number {
    return this.rngState >>> 0;
  }

  private spinRandomColumns(): number[][] {
    return REEL_WEIGHTS.map((weightsBySymbol) =>
      Array.from({ length: 4 }, () =>
        Number(weightedPick(weightsBySymbol, this.nextRandom)) || 0,
      ),
    );
  }

  private evaluateLineWins(
    columns: number[][],
    totalBetMinor: number,
  ): {
    lineWins: ProvisionalMathLineWin[];
    lineWinMultiplier: number;
    lineWinAmountMinor: number;
  } {
    const lineWins: ProvisionalMathLineWin[] = [];
    let lineWinMultiplier = 0;

    for (const line of PAYLINES) {
      const values = line.rowsByReel.map(
        (rowIndex, reelIndex) => columns[reelIndex]?.[rowIndex] ?? 0,
      );
      const evaluated = evaluateThreeSymbolLine(values);
      if (!evaluated || evaluated.multiplier <= 0) {
        continue;
      }
      lineWinMultiplier += evaluated.multiplier;
      lineWins.push({
        lineId: line.lineId,
        rowsByReel: [...line.rowsByReel],
        symbolId: evaluated.symbolId,
        multiplier: round3(evaluated.multiplier),
        amountMinor: Math.max(
          0,
          Math.round(evaluated.multiplier * Math.max(1, totalBetMinor)),
        ),
        positions: line.rowsByReel.map((rowIndex, reelIndex) => ({
          reelIndex,
          rowIndex,
        })),
      });
    }

    return {
      lineWins,
      lineWinMultiplier: round3(lineWinMultiplier),
      lineWinAmountMinor: Math.max(
        0,
        Math.round(lineWinMultiplier * Math.max(1, totalBetMinor)),
      ),
    };
  }

  public nextOutcome(input: NextOutcomeInput): ProvisionalMathOutcome {
    const requestedPreset = input.requestedPreset ?? null;
    const preset = requestedPreset ?? "normal";
    const presetOverride = requestedPreset ? PRESET_OVERRIDES[requestedPreset] ?? null : null;
    const sourcePath: ProvisionalGeneratorTrace["sourcePath"] = presetOverride
      ? "canned-preset-override"
      : "weighted-reel-rng";
    const seedStateBefore = this.getRngState();
    const mode = presetOverride?.modeOverride ?? input.mode;
    const totalBetMinor = Math.max(1, Math.round(input.totalBetMinor));
    const rawGeneratedMatrix = cloneColumns(presetOverride?.columns ?? this.spinRandomColumns());
    const columns = normalizeColumns(
      cloneColumns(rawGeneratedMatrix),
    );
    const finalPresentedMatrix = cloneColumns(columns);
    const postGenerationRewrite =
      sourcePath === "canned-preset-override" || !sameColumns(rawGeneratedMatrix, finalPresentedMatrix);
    const boardHash = createBoardHash(finalPresentedMatrix);
    const symbolGrid = buildGridFromColumns(columns);
    const counts = columns.flat().reduce(
      (acc, symbolId) => {
        if (symbolId === BONUS_COIN_SYMBOL_ID) acc.bonus += 1;
        if (symbolId === CHICKEN_COIN_SYMBOL_ID) acc.chicken += 1;
        if (symbolId === SUPER_CHICKEN_COIN_SYMBOL_ID) acc.superChicken += 1;
        if (
          symbolId === BONUS_COIN_SYMBOL_ID ||
          symbolId === CHICKEN_COIN_SYMBOL_ID ||
          symbolId === SUPER_CHICKEN_COIN_SYMBOL_ID
        ) {
          acc.featureFamily += 1;
        }
        return acc;
      },
      { bonus: 0, chicken: 0, superChicken: 0, featureFamily: 0 },
    );

    const { lineWins, lineWinMultiplier, lineWinAmountMinor } =
      this.evaluateLineWins(columns, totalBetMinor);

    const bonusTriggered =
      presetOverride?.forceBonus === true ||
      mode !== "base" ||
      isBaseBonusTrigger(columns);
    const collectTriggered =
      presetOverride?.forceCollect === true ||
      counts.chicken > 0 ||
      counts.superChicken > 0;

    const holdAndWinResult = bonusTriggered
      ? simulateHoldAndWin({
          mode,
          board: mode === "base" ? columns : null,
          randomFn: this.nextRandom,
          forcedJackpotTier: presetOverride?.forcedJackpotTier,
        })
      : {
          bonusWinMultiplier: 0,
          jackpotTierHits: {
            mini: 0,
            minor: 0,
            major: 0,
            grand: 0,
          },
          holdAndWinRemaining: 0,
          collectTriggered: false,
          boostTriggered: false,
        };

    const baseCollectResult =
      !bonusTriggered && collectTriggered
        ? simulateBaseCollectFromBoard(
            columns,
            this.nextRandom,
            presetOverride?.forcedJackpotTier,
          )
      : {
          bonusWinMultiplier: 0,
          jackpotTierHits: {
            mini: 0,
            minor: 0,
            major: 0,
            grand: 0,
          },
          collectTriggered: false,
          boostTriggered: false,
        };

    const mergedJackpotTierHits: Record<ProvisionalJackpotTier, number> = {
      mini: holdAndWinResult.jackpotTierHits.mini + baseCollectResult.jackpotTierHits.mini,
      minor: holdAndWinResult.jackpotTierHits.minor + baseCollectResult.jackpotTierHits.minor,
      major: holdAndWinResult.jackpotTierHits.major + baseCollectResult.jackpotTierHits.major,
      grand: holdAndWinResult.jackpotTierHits.grand + baseCollectResult.jackpotTierHits.grand,
    };

    const boostTriggered =
      presetOverride?.forceBoost === true ||
      holdAndWinResult.boostTriggered ||
      baseCollectResult.boostTriggered;
    const effectiveCollectTriggered =
      collectTriggered ||
      holdAndWinResult.collectTriggered ||
      baseCollectResult.collectTriggered;

    let bonusWinMultiplier = round3(
      holdAndWinResult.bonusWinMultiplier + baseCollectResult.bonusWinMultiplier,
    );
    let totalWinMultiplier = round3(lineWinMultiplier + bonusWinMultiplier);
    if (
      presetOverride?.minimumTotalMultiplier &&
      totalWinMultiplier < presetOverride.minimumTotalMultiplier
    ) {
      bonusWinMultiplier = round3(
        bonusWinMultiplier + (presetOverride.minimumTotalMultiplier - totalWinMultiplier),
      );
      totalWinMultiplier = round3(lineWinMultiplier + bonusWinMultiplier);
    }

    const totalWinAmountMinor = Math.max(
      0,
      Math.round(totalWinMultiplier * totalBetMinor),
    );
    const jackpotTier = resolveHighestJackpotTier(mergedJackpotTierHits);
    const jackpotLevel = resolveJackpotLevel(jackpotTier);
    const jackpotTriggered = jackpotTier !== null;
    const winTier = classifyWinTier(totalWinMultiplier);
    const timingHints = createDefaultTimingHints(bonusTriggered);

    const eventTriggers: string[] = [
      "round.spin.start",
      "round.reel.stop.1",
      "round.reel.stop.2",
      bonusTriggered ? "round.reel.stop.3.bonusHold" : "round.reel.stop.3",
    ];

    if (lineWins.length > 0) {
      eventTriggers.push("overlay.totalSummary.update");
    }
    if (effectiveCollectTriggered) {
      eventTriggers.push("feature.collect.triggered");
    }
    if (boostTriggered) {
      eventTriggers.push("feature.boost.triggered");
    }
    if (bonusTriggered) {
      eventTriggers.push("feature.bonus.enter");
    }
    if (jackpotTriggered) {
      eventTriggers.push("feature.jackpot.attached");
    }
    if (winTier !== "none") {
      eventTriggers.push("overlay.winTier.enter");
    }

    const animationCues = [...eventTriggers];
    if (boostTriggered && !bonusTriggered && !jackpotTriggered) {
      animationCues.push("focus-status-banner");
    }
    if (effectiveCollectTriggered) {
      animationCues.push("collect-sweep");
      animationCues.push("coin-fly");
    }
    if (bonusTriggered) {
      animationCues.push("hold-and-win-frame");
    }
    if (jackpotTriggered) {
      animationCues.push("jackpot-overlay");
    }

    const soundCues: string[] = [];
    if (winTier === "mega") {
      soundCues.push("win-tier-mega");
    } else if (winTier === "huge") {
      soundCues.push("win-tier-huge");
    } else if (winTier === "big") {
      soundCues.push("win-tier-big");
    }
    if (jackpotTriggered) {
      soundCues.push("jackpot-stinger");
    }

    const messages: string[] = [];
    if (lineWins.length > 0) {
      messages.push(`LINE WINS ${lineWins.length}`);
    } else {
      messages.push("NO LINE WIN");
    }
    if (effectiveCollectTriggered) {
      messages.push("COLLECT TRIGGERED");
    }
    if (boostTriggered) {
      messages.push("BOOST TRIGGERED");
    }
    if (bonusTriggered) {
      messages.push("BONUS TRIGGERED");
    }
    if (jackpotTier) {
      messages.push(`JACKPOT ${jackpotTier.toUpperCase()}`);
    }
    if (winTier !== "none") {
      messages.push(`${winTier.toUpperCase()} WIN ${round3(totalWinMultiplier)}x`);
    } else {
      messages.push(`WIN ${round3(totalWinMultiplier)}x`);
    }
    messages.push(`TOTAL ${formatCurrencyMinor(totalWinAmountMinor)}`);

    const labels: Record<string, string> = {
      state:
        requestedPreset === null
          ? "provisional-random"
          : preset === "normal"
            ? "provisional"
            : `provisional-${preset}`,
      mathSource: "provisional",
      mathPreset: requestedPreset ?? "none",
      collectFeatureActive: toBoolLabel(effectiveCollectTriggered),
      boostFeatureActive: toBoolLabel(boostTriggered),
      bonusGameActive: toBoolLabel(bonusTriggered),
      holdAndWinActive: toBoolLabel(bonusTriggered),
      jackpotTriggered: toBoolLabel(jackpotTriggered),
      buyFeatureAvailable: toBoolLabel(!bonusTriggered),
      winTier,
    };

    if (jackpotTier) {
      labels.jackpotTier = jackpotTier;
    }

    const counters: ProvisionalMathOutcome["counters"] = {
      buyFeatureAvailable: !bonusTriggered,
    };
    if (bonusTriggered) {
      counters.holdAndWinRemaining = Math.max(0, holdAndWinResult.holdAndWinRemaining);
    }
    if (jackpotLevel !== null) {
      counters.jackpotLevel = jackpotLevel;
    }

    const seedStateAfter = this.getRngState();

    return {
      mode,
      preset,
      reelStops: columns,
      symbolGrid,
      lineWins,
      lineWinMultiplier,
      lineWinAmountMinor,
      bonusWinMultiplier: round3(bonusWinMultiplier),
      totalWinMultiplier,
      totalWinAmountMinor,
      triggers: {
        collect: effectiveCollectTriggered,
        boost: boostTriggered,
        bonus: bonusTriggered,
        jackpot: jackpotTriggered,
      },
      jackpotTier,
      jackpotLevel,
      winTier,
      eventTriggers,
      timingHints,
      counters,
      labels,
      messages,
      soundCues,
      animationCues,
      generatorTrace: {
        mode,
        requestedPreset,
        effectivePreset: requestedPreset,
        sourcePath,
        seedStateBefore,
        seedStateAfter,
        rawGeneratedMatrix,
        finalPresentedMatrix,
        boardHash,
        postGenerationRewrite,
      },
    };
  }
}

export const adaptProvisionalMathOutcome = (
  outcome: ProvisionalMathOutcome,
): ProvisionalMathAdaptedResult => ({
  winAmountMinor: outcome.totalWinAmountMinor,
  reelStops: outcome.reelStops.map((column) => [...column]),
  symbolGrid: outcome.symbolGrid.map((row) => [...row]),
  uiMessages: [...outcome.messages],
  audioCues: [...outcome.soundCues],
  animationCues: [...outcome.animationCues],
  counters: { ...outcome.counters },
  labels: { ...outcome.labels },
  mathBridge: {
    source: "provisional",
    mode: outcome.mode,
    preset: outcome.preset,
    winTier: outcome.winTier,
    triggers: { ...outcome.triggers },
    lineWins: outcome.lineWins.map((entry) => ({
      ...entry,
      rowsByReel: [...entry.rowsByReel],
      positions: entry.positions.map((position) => ({ ...position })),
    })),
    eventTriggers: [...outcome.eventTriggers],
    timingHints: { ...outcome.timingHints },
    jackpotTier: outcome.jackpotTier,
    lineWinMultiplier: outcome.lineWinMultiplier,
    bonusWinMultiplier: outcome.bonusWinMultiplier,
    totalWinMultiplier: outcome.totalWinMultiplier,
    generatorTrace: {
      ...outcome.generatorTrace,
      rawGeneratedMatrix: outcome.generatorTrace.rawGeneratedMatrix.map((row) => [...row]),
      finalPresentedMatrix: outcome.generatorTrace.finalPresentedMatrix.map((row) => [...row]),
    },
  },
});

export const readMathSourceParam = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
): string | null => {
  const raw = params.get("mathSource");
  if (!raw) {
    return null;
  }
  const normalized = raw.trim().toLowerCase();
  return normalized.length > 0 ? normalized : null;
};

export const isProvisionalMathSourceRequested = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
): boolean => readMathSourceParam(params) === "provisional";

export const readMathPresetParam = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
): ProvisionalMathPreset | null => parsePreset(params.get("mathPreset"));

export const readMathModeParam = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
): ProvisionalMathMode | null => parseMode(params.get("mathMode"));

export const readMathSeedParam = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
): number => {
  const raw = params.get("mathSeed");
  const parsed = raw ? Number(raw) : NaN;
  if (Number.isFinite(parsed)) {
    return parsed;
  }
  return 7000_2026_0316;
};
