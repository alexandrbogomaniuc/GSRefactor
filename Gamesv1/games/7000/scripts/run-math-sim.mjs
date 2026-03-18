#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const gameRoot = path.resolve(__dirname, "..");
const mathRoot = path.join(gameRoot, "math");

const args = parseArgs(process.argv.slice(2));
const seed = Number.isFinite(args.seed) ? Number(args.seed) : Date.now();
const rng = mulberry32(seed);

const symbols = readJson("symbols.json");
const paylines = readJson("paylines.json");
const paytable = readJson("paytable.json");
const reelModel = readJson("reel-strips-or-weights.json");
const featureTables = readJson("feature-tables.json");
const buyTables = readJson("buy-bonus-tables.json");
const jackpots = readJson("jackpots.json");
const winThresholds = readJson("win-thresholds.json");

const wildSymbolId =
  symbols?.symbols?.find?.((entry) => String(entry?.class ?? "").toLowerCase() === "wild")?.id ?? 6;
const activePaylines = Math.max(1, Number(paytable?.lineWinRule?.stackedPaylines ?? 8));
const linePayouts = new Map(
  Object.entries(paytable.linePayouts).map(([id, entry]) => [
    Number(id),
    Number(entry.pays?.["3"] ?? 0) / activePaylines,
  ]),
);
const reels = reelModel.reels.map((reel) => reel.symbolWeights);
const jackpotLevels = jackpots.levels;
const thresholds = winThresholds.thresholds;

const baseSymbolIds = featureTables?.baseGame?.symbolIds ?? {};
const bonusCoinSymbolId = Number(baseSymbolIds.bonusCoin ?? 7);
const chickenCoinSymbolId = Number(baseSymbolIds.chickenCoin ?? 8);
const superChickenCoinSymbolId = Number(baseSymbolIds.superChickenCoin ?? 9);
const triggerReelIndexes = featureTables?.baseGame?.bonusTrigger?.bonusCoinReelIndexes ?? [0, 2];
const triggerCenterReelIndex = Number(featureTables?.baseGame?.bonusTrigger?.chickenTriggerReelIndex ?? 1);
const triggerCenterSymbolIds = featureTables?.baseGame?.bonusTrigger?.chickenTriggerSymbolIds ?? [
  chickenCoinSymbolId,
  superChickenCoinSymbolId,
];

const boostOutcomeWeights = sanitizeWeights(
  featureTables?.baseGame?.boostRules?.outcomeWeights ?? {
    multiplierBoost: 62,
    jackpotAttach: 23,
    extraCoins: 15,
  },
);
const boostMultipliers = sanitizeNumberList(featureTables?.baseGame?.boostRules?.multiplierOptions, [2, 3, 5, 7, 10]);
const boostExtraCoinCounts = sanitizeNumberList(
  featureTables?.baseGame?.boostRules?.extraCoinCountOptions,
  [2, 3, 5],
);

const holdAndWinConfig = {
  entrySpins: Number(featureTables?.holdAndWinBonus?.entrySpins ?? 3),
  maxGridSymbols: Number(featureTables?.holdAndWinBonus?.maxGridSymbols ?? 12),
  newSymbolChanceByRespinIndex: sanitizeNumberList(
    featureTables?.holdAndWinBonus?.newSymbolChanceByRespinIndex,
    [0.24, 0.2, 0.17],
  ),
  newBonusSymbolWeights: normalizeFeatureCoinWeights(featureTables?.holdAndWinBonus?.newBonusSymbolWeights),
  coinValueMultipliers: sanitizeNumberList(featureTables?.holdAndWinBonus?.coinValueMultipliers, [1, 2, 3, 5, 7, 10]),
  jackpotAttachChanceOnBonusCoin: Number(featureTables?.holdAndWinBonus?.jackpotAttachChanceOnBonusCoin ?? 0),
};
const jackpotLogic = {
  tierSelectionWeights: sanitizeWeights(featureTables?.jackpotTriggerLogic?.tierSelectionWeights),
};

const buyTierByMode = new Map(
  (buyTables.tiers ?? []).map((entry) => [
    String(entry.modeKey),
    {
      modeKey: String(entry.modeKey),
      costMultiplier: Number(entry.costMultiplier ?? 0),
      startingLockedSymbolRange: sanitizeRange(entry.startingLockedSymbolRange, [4, 6]),
      guaranteedChickenRange: sanitizeRange(entry.guaranteedChickenRange, [0, 0]),
      guaranteedSuperChickenCount: Number(entry.guaranteedSuperChickenCount ?? 0),
      autoBoostOnEntry: Boolean(entry.autoBoostOnEntry),
      startingTypeWeights: normalizeFeatureCoinWeights(entry.startingTypeWeights),
      bonusLandingChanceByRespinIndex: sanitizeNumberList(
        entry.bonusLandingChanceByRespinIndex,
        holdAndWinConfig.newSymbolChanceByRespinIndex,
      ),
      jackpotTierWeightOverride: sanitizeWeights(entry.jackpotTierWeightOverride),
    },
  ]),
);

const validModes = new Set(["all", "base", "buy75", "buy200", "buy300"]);
if (!validModes.has(args.mode)) {
  console.error(`Invalid --mode '${args.mode}'. Use one of: ${Array.from(validModes).join(", ")}`);
  process.exit(1);
}

const modeKeys = args.mode === "all" ? ["base", "buy75", "buy200", "buy300"] : [args.mode];
const startedAt = new Date().toISOString();
const report = {
  gameId: 7000,
  packageVersion: String(paytable.specVersion ?? "0.4.0-donor-rules-lock"),
  certified: false,
  startedAt,
  seed,
  roundsPerMode: args.rounds,
  sourceOfTruthNote: "Simulation only. GS/fixture runtime remains authoritative for production outcomes.",
  donorRulesLocked: {
    board: "3x4",
    paylines: 8,
    leftToRightHighestPerLine: true,
    bonusTrigger: "bonusCoin@reels1+3 + chickenOrSuper@reel2",
    bonusSymbolSet: ["bonusCoin", "chickenCoin", "superChickenCoin"],
    boostOutcomes: {
      multipliers: [...boostMultipliers],
      extraBonusCoins: [...boostExtraCoinCounts],
      jackpotTiers: { ...jackpotLevels },
    },
  },
  modes: {},
};

for (const mode of modeKeys) {
  report.modes[mode] = runModeSimulation(mode, args.rounds, rng);
}

const finishedAt = new Date().toISOString();
report.finishedAt = finishedAt;
const outPath = path.resolve(gameRoot, args.out);
fs.mkdirSync(path.dirname(outPath), { recursive: true });
fs.writeFileSync(outPath, `${JSON.stringify(report, null, 2)}\n`, "utf8");

printSummary(report, outPath);

function parseArgs(raw) {
  const defaults = {
    rounds: 100000,
    mode: "all",
    seed: Date.now(),
    out: "math/reports/math-sim-latest.json",
  };
  for (let i = 0; i < raw.length; i += 1) {
    const token = raw[i];
    if (token === "--rounds" && raw[i + 1]) {
      defaults.rounds = Math.max(1, Number(raw[i + 1]));
      i += 1;
      continue;
    }
    if (token === "--mode" && raw[i + 1]) {
      defaults.mode = String(raw[i + 1]);
      i += 1;
      continue;
    }
    if (token === "--seed" && raw[i + 1]) {
      defaults.seed = Number(raw[i + 1]);
      i += 1;
      continue;
    }
    if (token === "--out" && raw[i + 1]) {
      defaults.out = String(raw[i + 1]);
      i += 1;
      continue;
    }
  }
  return defaults;
}

function readJson(fileName) {
  const fullPath = path.join(mathRoot, fileName);
  return JSON.parse(fs.readFileSync(fullPath, "utf8"));
}

function mulberry32(seedValue) {
  let t = seedValue >>> 0;
  return () => {
    t += 0x6d2b79f5;
    let x = Math.imul(t ^ (t >>> 15), t | 1);
    x ^= x + Math.imul(x ^ (x >>> 7), x | 61);
    return ((x ^ (x >>> 14)) >>> 0) / 4294967296;
  };
}

function weightedPick(weightMap, randomFn) {
  const entries = Object.entries(weightMap).filter(([, value]) => Number(value) > 0);
  const total = entries.reduce((sum, [, weight]) => sum + Number(weight), 0);
  if (entries.length === 0 || total <= 0) {
    return "";
  }

  let roll = randomFn() * total;
  for (const [key, weightRaw] of entries) {
    const weight = Number(weightRaw);
    roll -= weight;
    if (roll <= 0) {
      return key;
    }
  }
  return entries[entries.length - 1]?.[0] ?? "";
}

function randIntInclusive(min, max, randomFn) {
  const lower = Number(min);
  const upper = Number(max);
  if (!Number.isFinite(lower) || !Number.isFinite(upper) || upper <= lower) {
    return Math.max(0, Math.round(lower));
  }
  return lower + Math.floor(randomFn() * (upper - lower + 1));
}

function sanitizeWeights(value) {
  if (!value || typeof value !== "object") {
    return {};
  }
  const output = {};
  for (const [key, raw] of Object.entries(value)) {
    const numeric = Number(raw);
    if (Number.isFinite(numeric) && numeric > 0) {
      output[key] = numeric;
    }
  }
  return output;
}

function sanitizeNumberList(value, fallback = []) {
  if (!Array.isArray(value)) {
    return [...fallback];
  }
  const output = value.map((entry) => Number(entry)).filter((entry) => Number.isFinite(entry) && entry > 0);
  return output.length > 0 ? output : [...fallback];
}

function sanitizeRange(value, fallback) {
  const values = Array.isArray(value) ? value.map((entry) => Number(entry)) : [];
  if (values.length < 2 || !Number.isFinite(values[0]) || !Number.isFinite(values[1])) {
    return [...fallback];
  }
  const lower = Math.max(0, Math.round(values[0]));
  const upper = Math.max(lower, Math.round(values[1]));
  return [lower, upper];
}

function normalizeFeatureCoinWeights(value) {
  const weights = sanitizeWeights(value);
  return {
    bonus: Number(weights.bonus ?? weights.coin ?? 0),
    chicken: Number(weights.chicken ?? weights.collector ?? 0),
    superChicken: Number(weights.superChicken ?? weights.jackpot ?? 0),
  };
}

function pickArrayValue(values, randomFn, fallback = 1) {
  if (!Array.isArray(values) || values.length === 0) {
    return fallback;
  }
  const index = Math.max(0, Math.min(values.length - 1, Math.floor(randomFn() * values.length)));
  return Number(values[index] ?? fallback) || fallback;
}

function spinBoard(randomFn) {
  const board = Array.from({ length: reels.length }, () => Array.from({ length: 4 }, () => 0));
  for (let reelIndex = 0; reelIndex < reels.length; reelIndex += 1) {
    for (let rowIndex = 0; rowIndex < 4; rowIndex += 1) {
      board[reelIndex][rowIndex] = Number(weightedPick(reels[reelIndex], randomFn));
    }
  }
  return board;
}

function matchesBonusTrigger(board) {
  const [leftReel, rightReel] = triggerReelIndexes;
  const leftHasBonus = board[leftReel]?.some((symbolId) => symbolId === bonusCoinSymbolId) ?? false;
  const rightHasBonus = board[rightReel]?.some((symbolId) => symbolId === bonusCoinSymbolId) ?? false;
  const centerHasChicken =
    board[triggerCenterReelIndex]?.some((symbolId) => triggerCenterSymbolIds.includes(symbolId)) ?? false;
  return leftHasBonus && rightHasBonus && centerHasChicken;
}

function evaluateLineWin(board) {
  let lineWin = 0;
  for (const line of paylines.paylines) {
    const values = line.rowsByReel.map((rowIndex, reelIndex) => board[reelIndex][rowIndex]);
    const linePay = evaluateThreeSymbolLine(values);
    lineWin += linePay;
  }
  return lineWin;
}

function evaluateThreeSymbolLine(values) {
  const allWild = values.every((id) => id === wildSymbolId);
  if (allWild) {
    return linePayouts.get(wildSymbolId) ?? 0;
  }

  let bestPay = 0;
  for (const [symbolId, payout] of linePayouts.entries()) {
    const matched = values.every((id) => id === symbolId || id === wildSymbolId);
    if (!matched) {
      continue;
    }
    if (payout > bestPay) {
      bestPay = payout;
    }
  }
  return bestPay;
}

function createFeatureCoin({
  type,
  randomFn,
  jackpotWeights,
  forcedJackpotTier,
  jackpotTierHits,
  allowBoost,
}) {
  let value = pickArrayValue(holdAndWinConfig.coinValueMultipliers, randomFn, 1);
  let jackpotAttached = false;
  let boostApplied = false;
  let extraBonusCount = 0;

  if (type === "superChicken" && allowBoost) {
    const boostOutcome = weightedPick(boostOutcomeWeights, randomFn);
    if (boostOutcome === "multiplierBoost") {
      const multiplier = pickArrayValue(boostMultipliers, randomFn, 2);
      value *= multiplier;
      boostApplied = true;
    } else if (boostOutcome === "jackpotAttach") {
      const tier =
        forcedJackpotTier && jackpotTierHits[forcedJackpotTier] === 0
          ? forcedJackpotTier
          : weightedPick(jackpotWeights, randomFn);
      const jackpotValue = Number(jackpotLevels[tier] ?? 0);
      value += jackpotValue;
      jackpotTierHits[tier] += 1;
      jackpotAttached = jackpotValue > 0;
      boostApplied = true;
    } else if (boostOutcome === "extraCoins") {
      extraBonusCount = Math.max(0, Math.round(pickArrayValue(boostExtraCoinCounts, randomFn, 2)));
      boostApplied = extraBonusCount > 0;
    }
  } else if (type === "bonus" && holdAndWinConfig.jackpotAttachChanceOnBonusCoin > 0) {
    if (randomFn() < holdAndWinConfig.jackpotAttachChanceOnBonusCoin) {
      const tier = weightedPick(jackpotWeights, randomFn);
      const jackpotValue = Number(jackpotLevels[tier] ?? 0);
      value += jackpotValue;
      jackpotTierHits[tier] += 1;
      jackpotAttached = jackpotValue > 0;
    }
  }

  return {
    type,
    value: Number(value.toFixed(3)),
    jackpotAttached,
    boostApplied,
    extraBonusCount,
  };
}

function extractFeatureTypesFromBoard(board) {
  const featureTypes = [];
  for (const column of board) {
    for (const symbolId of column) {
      if (symbolId === bonusCoinSymbolId) {
        featureTypes.push("bonus");
      } else if (symbolId === chickenCoinSymbolId) {
        featureTypes.push("chicken");
      } else if (symbolId === superChickenCoinSymbolId) {
        featureTypes.push("superChicken");
      }
    }
  }
  return featureTypes;
}

function simulateBaseFeatureRound({
  board,
  randomFn,
  forcedJackpotTier,
}) {
  const jackpotTierHits = {
    mini: 0,
    minor: 0,
    major: 0,
    grand: 0,
  };
  const featureTypes = extractFeatureTypesFromBoard(board);
  const jackpotWeights = jackpotLogic.tierSelectionWeights;
  const locked = [];
  let boostTriggered = false;

  for (const type of featureTypes) {
    const coin = createFeatureCoin({
      type,
      randomFn,
      jackpotWeights,
      forcedJackpotTier,
      jackpotTierHits,
      allowBoost: true,
    });
    locked.push(coin);
    boostTriggered ||= coin.boostApplied || coin.jackpotAttached || coin.extraBonusCount > 0;
    if (coin.extraBonusCount > 0) {
      for (let index = 0; index < coin.extraBonusCount; index += 1) {
        locked.push(
          createFeatureCoin({
            type: "bonus",
            randomFn,
            jackpotWeights,
            forcedJackpotTier,
            jackpotTierHits,
            allowBoost: false,
          }),
        );
      }
    }
  }

  const hasChicken = locked.some((entry) => entry.type === "chicken" || entry.type === "superChicken");
  const featureValueSum = locked.reduce((sum, entry) => sum + entry.value, 0);

  const collectTriggered = hasChicken;
  const totalWin = collectTriggered ? featureValueSum : 0;

  return {
    totalWin: round3(totalWin),
    collectTriggered,
    boostTriggered,
    jackpotTierHits,
  };
}

function simulateHoldAndWin({ randomFn, buyConfig, board, forcedJackpotTier }) {
  const jackpotWeights =
    buyConfig?.jackpotTierWeightOverride &&
    Object.keys(buyConfig.jackpotTierWeightOverride).length > 0
      ? buyConfig.jackpotTierWeightOverride
      : jackpotLogic.tierSelectionWeights;

  const jackpotTierHits = {
    mini: 0,
    minor: 0,
    major: 0,
    grand: 0,
  };
  const locked = [];
  let boostTriggered = Boolean(buyConfig?.autoBoostOnEntry);

  if (buyConfig) {
    const [minStart, maxStart] = buyConfig.startingLockedSymbolRange;
    let initialCount = randIntInclusive(minStart, maxStart, randomFn);

    const guaranteedSuper = Math.max(0, Number(buyConfig.guaranteedSuperChickenCount ?? 0));
    for (let index = 0; index < guaranteedSuper; index += 1) {
      const coin = createFeatureCoin({
        type: "superChicken",
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
        allowBoost: true,
      });
      boostTriggered ||= coin.boostApplied || coin.jackpotAttached || coin.extraBonusCount > 0;
      locked.push(coin);
    }

    const [minChicken, maxChicken] = buyConfig.guaranteedChickenRange ?? [0, 0];
    const guaranteedChicken = randIntInclusive(minChicken, maxChicken, randomFn);
    for (let index = 0; index < guaranteedChicken; index += 1) {
      const coin = createFeatureCoin({
        type: "chicken",
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
        allowBoost: true,
      });
      boostTriggered ||= coin.boostApplied || coin.jackpotAttached || coin.extraBonusCount > 0;
      locked.push(coin);
    }

    initialCount = Math.max(initialCount, locked.length);
    const fillCount = Math.max(0, initialCount - locked.length);
    for (let index = 0; index < fillCount; index += 1) {
      const type = weightedPick(buyConfig.startingTypeWeights, randomFn);
      const coin = createFeatureCoin({
        type: type || "bonus",
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
        allowBoost: true,
      });
      boostTriggered ||= coin.boostApplied || coin.jackpotAttached || coin.extraBonusCount > 0;
      locked.push(coin);
    }
  } else if (board) {
    const featureTypes = extractFeatureTypesFromBoard(board);
    for (const type of featureTypes) {
      const coin = createFeatureCoin({
        type,
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
        allowBoost: true,
      });
      boostTriggered ||= coin.boostApplied || coin.jackpotAttached || coin.extraBonusCount > 0;
      locked.push(coin);
    }
  }

  let spinsLeft = Number(holdAndWinConfig.entrySpins);
  while (spinsLeft > 0 && locked.length < holdAndWinConfig.maxGridSymbols) {
    const stageIndex = Math.max(0, Math.min(holdAndWinConfig.entrySpins - spinsLeft, holdAndWinConfig.newSymbolChanceByRespinIndex.length - 1));
    const stageChance = Number(
      (buyConfig?.bonusLandingChanceByRespinIndex ?? holdAndWinConfig.newSymbolChanceByRespinIndex)[stageIndex] ??
        holdAndWinConfig.newSymbolChanceByRespinIndex[stageIndex] ??
        0.18,
    );
    const emptySlots = holdAndWinConfig.maxGridSymbols - locked.length;
    const perSlotChance = Math.max(0, stageChance / 4);
    let landedThisSpin = 0;

    for (let index = 0; index < emptySlots; index += 1) {
      if (randomFn() >= perSlotChance) {
        continue;
      }
      const type = weightedPick(holdAndWinConfig.newBonusSymbolWeights, randomFn) || "bonus";
      const coin = createFeatureCoin({
        type,
        randomFn,
        jackpotWeights,
        forcedJackpotTier,
        jackpotTierHits,
        allowBoost: true,
      });
      locked.push(coin);
      landedThisSpin += 1;
      boostTriggered ||= coin.boostApplied || coin.jackpotAttached || coin.extraBonusCount > 0;

      if (coin.extraBonusCount > 0) {
        for (
          let extraIndex = 0;
          extraIndex < coin.extraBonusCount && locked.length < holdAndWinConfig.maxGridSymbols;
          extraIndex += 1
        ) {
          locked.push(
            createFeatureCoin({
              type: "bonus",
              randomFn,
              jackpotWeights,
              forcedJackpotTier,
              jackpotTierHits,
              allowBoost: false,
            }),
          );
          landedThisSpin += 1;
        }
      }
    }

    if (landedThisSpin > 0) {
      spinsLeft = Number(holdAndWinConfig.entrySpins);
    } else {
      spinsLeft -= 1;
    }
  }

  const bonusValueSum = locked.filter((entry) => entry.type === "bonus").reduce((sum, entry) => sum + entry.value, 0);
  const chickenValueSum = locked.filter((entry) => entry.type === "chicken").reduce((sum, entry) => sum + entry.value, 0);
  const superValueSum = locked
    .filter((entry) => entry.type === "superChicken")
    .reduce((sum, entry) => sum + entry.value, 0);

  const collectTriggered = chickenValueSum > 0 || superValueSum > 0;
  const collectExtension = collectTriggered ? chickenValueSum + superValueSum : 0;
  const totalWin = round3(bonusValueSum + collectExtension);

  return {
    totalWin,
    collectTriggered,
    boostTriggered,
    jackpotHit: Object.values(jackpotTierHits).some((count) => count > 0),
    jackpotTierHits,
    holdAndWinRemaining: Math.max(0, spinsLeft),
  };
}

function runModeSimulation(mode, rounds, randomFn) {
  const tierCounts = {
    none: 0,
    small: 0,
    big: 0,
    huge: 0,
    mega: 0,
  };
  const jackpotTierHits = {
    mini: 0,
    minor: 0,
    major: 0,
    grand: 0,
  };

  let wagerTotal = 0;
  let payoutTotal = 0;
  let hitCount = 0;
  let bonusCount = 0;
  let collectCount = 0;
  let boostCount = 0;
  let jackpotRoundCount = 0;
  let maxWin = 0;

  const buyConfig = mode === "base" ? null : buyTierByMode.get(mode) ?? null;
  const costPerRound = buyConfig ? Number(buyConfig.costMultiplier) : 1;

  for (let spin = 0; spin < rounds; spin += 1) {
    let totalWin = 0;
    let bonusTriggered = false;
    let collectTriggered = false;
    let boostTriggered = false;
    let jackpotHit = false;
    const perRoundJackpotHits = {
      mini: 0,
      minor: 0,
      major: 0,
      grand: 0,
    };

    if (buyConfig) {
      bonusTriggered = true;
      const bonusResult = simulateHoldAndWin({
        randomFn,
        buyConfig,
        board: null,
      });
      totalWin = bonusResult.totalWin;
      collectTriggered = bonusResult.collectTriggered;
      boostTriggered = bonusResult.boostTriggered;
      jackpotHit = bonusResult.jackpotHit;
      mergeCounts(perRoundJackpotHits, bonusResult.jackpotTierHits);
    } else {
      const board = spinBoard(randomFn);
      const lineWin = evaluateLineWin(board);
      bonusTriggered = matchesBonusTrigger(board);

      if (bonusTriggered) {
        const bonusResult = simulateHoldAndWin({
          randomFn,
          buyConfig: null,
          board,
        });
        totalWin = round3(lineWin + bonusResult.totalWin);
        collectTriggered = bonusResult.collectTriggered;
        boostTriggered = bonusResult.boostTriggered;
        jackpotHit = bonusResult.jackpotHit;
        mergeCounts(perRoundJackpotHits, bonusResult.jackpotTierHits);
      } else {
        const baseFeatureResult = simulateBaseFeatureRound({
          board,
          randomFn,
        });
        totalWin = round3(lineWin + baseFeatureResult.totalWin);
        collectTriggered = baseFeatureResult.collectTriggered;
        boostTriggered = baseFeatureResult.boostTriggered;
        jackpotHit = Object.values(baseFeatureResult.jackpotTierHits).some((count) => count > 0);
        mergeCounts(perRoundJackpotHits, baseFeatureResult.jackpotTierHits);
      }
    }

    wagerTotal += costPerRound;
    payoutTotal += totalWin;
    maxWin = Math.max(maxWin, totalWin);

    if (totalWin > 0) {
      hitCount += 1;
    }
    if (bonusTriggered) {
      bonusCount += 1;
    }
    if (collectTriggered) {
      collectCount += 1;
    }
    if (boostTriggered) {
      boostCount += 1;
    }
    if (jackpotHit) {
      jackpotRoundCount += 1;
    }
    mergeCounts(jackpotTierHits, perRoundJackpotHits);

    if (totalWin <= 0) {
      tierCounts.none += 1;
    } else if (totalWin < thresholds.big) {
      tierCounts.small += 1;
    } else if (totalWin < thresholds.huge) {
      tierCounts.big += 1;
    } else if (totalWin < thresholds.mega) {
      tierCounts.huge += 1;
    } else {
      tierCounts.mega += 1;
    }
  }

  return {
    mode,
    rounds,
    costPerRoundMultiplier: costPerRound,
    rtpPercent: toPercent(payoutTotal / Math.max(1, wagerTotal)),
    hitFrequencyPercent: toPercent(hitCount / rounds),
    bonusFrequencyPercent: toPercent(bonusCount / rounds),
    collectFrequencyPercent: toPercent(collectCount / rounds),
    boostFrequencyPercent: toPercent(boostCount / rounds),
    jackpotFrequencyPercent: toPercent(jackpotRoundCount / rounds),
    averagePayoutMultiplier: round3(payoutTotal / rounds),
    averageWinOnHitMultiplier: round3(hitCount > 0 ? payoutTotal / hitCount : 0),
    maxObservedWinMultiplier: round3(maxWin),
    tierDistribution: tierCounts,
    jackpotTierHits,
  };
}

function mergeCounts(target, source) {
  for (const key of Object.keys(target)) {
    target[key] += Number(source[key] ?? 0);
  }
}

function toPercent(value) {
  return round3(value * 100);
}

function round3(value) {
  return Number(value.toFixed(3));
}

function printSummary(result, outPathValue) {
  console.log("Game 7000 math simulation complete");
  console.log(`seed=${result.seed} roundsPerMode=${result.roundsPerMode}`);
  for (const [mode, modeResult] of Object.entries(result.modes)) {
    console.log(
      [
        `[${mode}]`,
        `rtp=${modeResult.rtpPercent}%`,
        `hit=${modeResult.hitFrequencyPercent}%`,
        `bonus=${modeResult.bonusFrequencyPercent}%`,
        `collect=${modeResult.collectFrequencyPercent}%`,
        `boost=${modeResult.boostFrequencyPercent}%`,
        `jackpot=${modeResult.jackpotFrequencyPercent}%`,
        `avgWin=${modeResult.averagePayoutMultiplier}x`,
      ].join(" "),
    );
  }
  console.log(`report=${outPathValue}`);
}
