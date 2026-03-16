import paylinesData from "../../../math/paylines.json";
import paytableData from "../../../math/paytable.json";
import reelModelData from "../../../math/reel-strips-or-weights.json";
import featureTablesData from "../../../math/feature-tables.json";
import buyBonusTablesData from "../../../math/buy-bonus-tables.json";
import jackpotsData from "../../../math/jackpots.json";
import winThresholdsData from "../../../math/win-thresholds.json";

import { buildGridFromColumns } from "../../game/config/CrazyRoosterGameConfig";

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
  jackpotTierWeightOverride?: WeightedNumberMap;
};

type HoldAndWinConfig = {
  entrySpins: number;
  maxGridSymbols: number;
  newSymbolChanceByRespinIndex: number[];
  newBonusSymbolWeights: WeightedNumberMap;
  coinValueMultipliers: number[];
};

type JackpotLogicConfig = {
  tierSelectionWeights: WeightedNumberMap;
};

type BonusSymbolType = "coin" | "collector" | "jackpot";

type BonusLockedSymbol = {
  type: BonusSymbolType;
  value: number;
  tier?: ProvisionalJackpotTier;
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
  mathBridge: MathBridgePresentationHints & {
    lineWinMultiplier: number;
    bonusWinMultiplier: number;
    totalWinMultiplier: number;
  };
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
      const range = asNumberArray(parsed.startingLockedSymbolRange);
      return {
        modeKey: String(parsed.modeKey ?? "base") as ProvisionalMathMode,
        costMultiplier: asNumber(parsed.costMultiplier, 0),
        startingLockedSymbolRange: [
          asNumber(range[0], 0),
          asNumber(range[1], 0),
        ] as [number, number],
        startingTypeWeights: asWeightedNumberMap(parsed.startingTypeWeights),
        bonusLandingChanceByRespinIndex: asNumberArray(parsed.bonusLandingChanceByRespinIndex),
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
    const payout3 = asNumber(payouts["3"]);
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
  newBonusSymbolWeights: asWeightedNumberMap(
    asRecord(asRecord(featureTablesData).holdAndWinBonus).newBonusSymbolWeights,
  ),
  coinValueMultipliers: asNumberArray(
    asRecord(asRecord(featureTablesData).holdAndWinBonus).coinValueMultipliers,
  ),
};

const JACKPOT_LOGIC: JackpotLogicConfig = {
  tierSelectionWeights: asWeightedNumberMap(
    asRecord(asRecord(featureTablesData).jackpotTriggerLogic).tierSelectionWeights,
  ),
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

const WILD_SYMBOL_ID = 6;
const COIN_SYMBOL_ID = 7;
const COLLECTOR_SYMBOL_ID = 8;
const JACKPOT_SYMBOL_ID = 9;
const BASE_BONUS_TRIGGER_COUNT = 6;

const DONOR_REEL_STOP_NORMAL: [number, number, number] = [483, 567, 650];
const DONOR_REEL_STOP_BONUS_HOLD: [number, number, number] = [483, 567, 5000];

const PRESET_OVERRIDES: Record<Exclude<ProvisionalMathPreset, "normal">, PresetOverride> = {
  collect: {
    columns: [
      [8, 1, 2, 3],
      [7, 5, 2, 4],
      [0, 7, 3, 1],
    ],
    forceCollect: true,
  },
  boost: {
    columns: [
      [8, 7, 2, 3],
      [8, 1, 7, 4],
      [2, 8, 3, 0],
    ],
    forceCollect: true,
    forceBoost: true,
  },
  bonus: {
    columns: [
      [8, 7, 9, 2],
      [7, 8, 9, 1],
      [7, 8, 2, 3],
    ],
    forceCollect: true,
    forceBoost: true,
    forceBonus: true,
  },
  jackpot: {
    columns: [
      [9, 7, 8, 2],
      [8, 9, 7, 1],
      [7, 8, 9, 3],
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

const normalizeColumns = (columns: number[][]): number[][] =>
  columns.map((column) => [...column].slice(0, 4).map((value) => Number(value) || 0));

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

const createMulberry32 = (seedInput: number): (() => number) => {
  let state = seedInput >>> 0;
  return () => {
    state += 0x6d2b79f5;
    let value = Math.imul(state ^ (state >>> 15), state | 1);
    value ^= value + Math.imul(value ^ (value >>> 7), value | 61);
    return ((value ^ (value >>> 14)) >>> 0) / 4294967296;
  };
};

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
};

const resolveBonusSymbol = (
  typeRaw: BonusSymbolType,
  randomFn: () => number,
  jackpotWeights: WeightedNumberMap,
  forcedJackpotTier: ProvisionalJackpotTier | undefined,
  jackpotTierHits: Record<ProvisionalJackpotTier, number>,
): BonusLockedSymbol => {
  if (typeRaw === "coin") {
    const pool = HOLD_AND_WIN_CONFIG.coinValueMultipliers;
    const index = Math.floor(randomFn() * Math.max(1, pool.length));
    return {
      type: "coin",
      value: asNumber(pool[index], 1),
    };
  }

  if (typeRaw === "collector") {
    return {
      type: "collector",
      value: 0,
    };
  }

  const resolvedTier =
    forcedJackpotTier && jackpotTierHits[forcedJackpotTier] === 0
      ? forcedJackpotTier
      : (weightedPick(jackpotWeights, randomFn) as ProvisionalJackpotTier);
  const value = asNumber(JACKPOT_LEVELS[resolvedTier], 0);
  jackpotTierHits[resolvedTier] += 1;
  return {
    type: "jackpot",
    tier: resolvedTier,
    value,
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

  if (buyTier) {
    const [minStart, maxStart] = buyTier.startingLockedSymbolRange;
    const initialCount = randomIntInclusive(minStart, maxStart, randomFn);
    for (let index = 0; index < initialCount; index += 1) {
      const type = weightedPick(
        buyTier.startingTypeWeights,
        randomFn,
      ) as BonusSymbolType;
      locked.push(
        resolveBonusSymbol(
          type,
          randomFn,
          jackpotWeights,
          forcedJackpotTier,
          jackpotTierHits,
        ),
      );
    }
  } else if (board) {
    for (const column of board) {
      for (const symbolId of column) {
        if (symbolId === COIN_SYMBOL_ID) {
          locked.push(
            resolveBonusSymbol(
              "coin",
              randomFn,
              jackpotWeights,
              forcedJackpotTier,
              jackpotTierHits,
            ),
          );
        } else if (symbolId === COLLECTOR_SYMBOL_ID) {
          locked.push(
            resolveBonusSymbol(
              "collector",
              randomFn,
              jackpotWeights,
              forcedJackpotTier,
              jackpotTierHits,
            ),
          );
        } else if (symbolId === JACKPOT_SYMBOL_ID) {
          locked.push(
            resolveBonusSymbol(
              "jackpot",
              randomFn,
              jackpotWeights,
              forcedJackpotTier,
              jackpotTierHits,
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
        locked.push(
          resolveBonusSymbol(
            type,
            randomFn,
            jackpotWeights,
            forcedJackpotTier,
            jackpotTierHits,
          ),
        );
        landedThisSpin += 1;
      }
    }

    if (landedThisSpin > 0) {
      spinsLeft = HOLD_AND_WIN_CONFIG.entrySpins;
    } else {
      spinsLeft -= 1;
    }
  }

  const coinSum = locked
    .filter((entry) => entry.type === "coin")
    .reduce((sum, entry) => sum + entry.value, 0);
  const collectorCount = locked.filter((entry) => entry.type === "collector").length;
  const jackpotSum = locked
    .filter((entry) => entry.type === "jackpot")
    .reduce((sum, entry) => sum + entry.value, 0);

  const collectWin = round3(coinSum * collectorCount * 0.24);
  const bonusWinMultiplier = round3(coinSum + collectWin + jackpotSum);
  return {
    bonusWinMultiplier,
    jackpotTierHits,
    holdAndWinRemaining: Math.max(0, spinsLeft),
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
  private readonly random: () => number;

  constructor(seedInput: number) {
    this.random = createMulberry32(seedInput);
  }

  private spinRandomColumns(): number[][] {
    return REEL_WEIGHTS.map((weightsBySymbol) =>
      Array.from({ length: 4 }, () =>
        Number(weightedPick(weightsBySymbol, this.random)) || 0,
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
    const preset = input.requestedPreset ?? "normal";
    const presetOverride =
      preset === "normal" ? null : PRESET_OVERRIDES[preset];
    const mode = presetOverride?.modeOverride ?? input.mode;
    const totalBetMinor = Math.max(1, Math.round(input.totalBetMinor));
    const columns = normalizeColumns(
      presetOverride?.columns ?? this.spinRandomColumns(),
    );
    const symbolGrid = buildGridFromColumns(columns);
    const counts = columns.flat().reduce(
      (acc, symbolId) => {
        if (symbolId === COIN_SYMBOL_ID) acc.coin += 1;
        if (symbolId === COLLECTOR_SYMBOL_ID) acc.collector += 1;
        if (symbolId === JACKPOT_SYMBOL_ID) acc.jackpot += 1;
        if (
          symbolId === COIN_SYMBOL_ID ||
          symbolId === COLLECTOR_SYMBOL_ID ||
          symbolId === JACKPOT_SYMBOL_ID
        ) {
          acc.bonusFamily += 1;
        }
        return acc;
      },
      { coin: 0, collector: 0, jackpot: 0, bonusFamily: 0 },
    );

    const { lineWins, lineWinMultiplier, lineWinAmountMinor } =
      this.evaluateLineWins(columns, totalBetMinor);

    const bonusTriggered =
      presetOverride?.forceBonus === true ||
      mode !== "base" ||
      counts.bonusFamily >= BASE_BONUS_TRIGGER_COUNT;
    const collectTriggered =
      presetOverride?.forceCollect === true ||
      (counts.collector > 0 && counts.coin > 0);
    const boostTriggered =
      presetOverride?.forceBoost === true ||
      counts.collector >= 2 ||
      (counts.collector > 0 && counts.jackpot > 0);

    const holdAndWin = bonusTriggered
      ? simulateHoldAndWin({
          mode,
          board: mode === "base" ? columns : null,
          randomFn: this.random,
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
        };

    let bonusWinMultiplier = holdAndWin.bonusWinMultiplier;
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
    const jackpotTier = resolveHighestJackpotTier(holdAndWin.jackpotTierHits);
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
    if (collectTriggered) {
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
    if (boostTriggered) {
      animationCues.push("focus-status-banner");
    }
    if (collectTriggered) {
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
    if (collectTriggered) {
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
      state: preset === "normal" ? "provisional" : `provisional-${preset}`,
      mathSource: "provisional",
      mathPreset: preset,
      collectFeatureActive: toBoolLabel(collectTriggered),
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
      counters.holdAndWinRemaining = Math.max(0, holdAndWin.holdAndWinRemaining);
    }
    if (jackpotLevel !== null) {
      counters.jackpotLevel = jackpotLevel;
    }

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
        collect: collectTriggered,
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
