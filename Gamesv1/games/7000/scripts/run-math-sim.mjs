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

const symbolById = new Map(symbols.symbols.map((s) => [s.id, s]));
const linePayouts = new Map(
  Object.entries(paytable.linePayouts).map(([id, entry]) => [Number(id), Number(entry.pays["3"] ?? 0)]),
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
  packageVersion: "0.3.0-provisional",
  certified: false,
  startedAt,
  seed,
  roundsPerMode: args.rounds,
  sourceOfTruthNote: "Simulation only. GS/fixture runtime remains authoritative for production outcomes.",
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
  const entries = Object.entries(weightMap);
  const total = entries.reduce((sum, [, weight]) => sum + Number(weight), 0);
  let roll = randomFn() * total;
  for (const [key, weightRaw] of entries) {
    const weight = Number(weightRaw);
    roll -= weight;
    if (roll <= 0) {
      return key;
    }
  }
  return entries[entries.length - 1]?.[0];
}

function randIntInclusive(min, max, randomFn) {
  return min + Math.floor(randomFn() * (max - min + 1));
}

function spinBoard(randomFn) {
  const reels = reelModel.reels.map((reel) => reel.symbolWeights);
  const board = Array.from({ length: reels.length }, () => Array.from({ length: 4 }, () => 0));
  for (let reelIndex = 0; reelIndex < reels.length; reelIndex += 1) {
    for (let rowIndex = 0; rowIndex < 4; rowIndex += 1) {
      board[reelIndex][rowIndex] = Number(weightedPick(reels[reelIndex], randomFn));
    }
  }
  return board;
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
  const wildId = 6;
  const allWild = values.every((id) => id === wildId);
  if (allWild) {
    return linePayouts.get(wildId) ?? 0;
  }

  let bestPay = 0;
  for (let symbolId = 0; symbolId <= 5; symbolId += 1) {
    const matched = values.every((id) => id === symbolId || id === wildId);
    if (!matched) {
      continue;
    }
    const pay = linePayouts.get(symbolId) ?? 0;
    if (pay > bestPay) {
      bestPay = pay;
    }
  }
  return bestPay;
}

function collectBonusFamilyCount(board) {
  let count = 0;
  for (const reel of board) {
    for (const symbolId of reel) {
      if (symbolId === 7 || symbolId === 8 || symbolId === 9) {
        count += 1;
      }
    }
  }
  return count;
}

function runModeSimulation(mode, rounds, randomFn) {
  const thresholds = winThresholds.thresholds;
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
  let jackpotRoundCount = 0;
  let maxWin = 0;

  const buyConfig =
    mode === "base" ? null : buyTables.tiers.find((entry) => entry.modeKey === mode) ?? null;
  const costPerRound = buyConfig ? Number(buyConfig.costMultiplier) : 1;

  for (let spin = 0; spin < rounds; spin += 1) {
    let lineWin = 0;
    let bonusTriggered = false;
    let jackpotHit = false;
    let totalWin = 0;

    if (buyConfig) {
      bonusTriggered = true;
      const bonusResult = simulateHoldAndWin({ randomFn, buyConfig, board: null });
      totalWin = bonusResult.totalWin;
      jackpotHit = bonusResult.jackpotHit;
      for (const [tier, count] of Object.entries(bonusResult.jackpotTierHits)) {
        jackpotTierHits[tier] += count;
      }
    } else {
      const board = spinBoard(randomFn);
      lineWin = evaluateLineWin(board);
      const bonusFamilyCount = collectBonusFamilyCount(board);
      const triggerThreshold = 6;
      if (bonusFamilyCount >= triggerThreshold) {
        bonusTriggered = true;
        const bonusResult = simulateHoldAndWin({ randomFn, buyConfig: null, board });
        totalWin = lineWin + bonusResult.totalWin;
        jackpotHit = bonusResult.jackpotHit;
        for (const [tier, count] of Object.entries(bonusResult.jackpotTierHits)) {
          jackpotTierHits[tier] += count;
        }
      } else {
        totalWin = lineWin;
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
    if (jackpotHit) {
      jackpotRoundCount += 1;
    }

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
    rtpPercent: toPercent(payoutTotal / wagerTotal),
    hitFrequencyPercent: toPercent(hitCount / rounds),
    bonusFrequencyPercent: toPercent(bonusCount / rounds),
    jackpotFrequencyPercent: toPercent(jackpotRoundCount / rounds),
    averagePayoutMultiplier: round3(payoutTotal / rounds),
    averageWinOnHitMultiplier: round3(hitCount > 0 ? payoutTotal / hitCount : 0),
    maxObservedWinMultiplier: round3(maxWin),
    tierDistribution: tierCounts,
    jackpotTierHits,
  };
}

function simulateHoldAndWin({ randomFn, buyConfig, board }) {
  const bonusConfig = featureTables.holdAndWinBonus;
  const jackpotLogic = featureTables.jackpotTriggerLogic;
  const locked = [];
  const jackpotTierHits = {
    mini: 0,
    minor: 0,
    major: 0,
    grand: 0,
  };

  if (buyConfig) {
    const [minStart, maxStart] = buyConfig.startingLockedSymbolRange;
    const initialCount = randIntInclusive(minStart, maxStart, randomFn);
    for (let i = 0; i < initialCount; i += 1) {
      const type = weightedPick(buyConfig.startingTypeWeights, randomFn);
      locked.push(resolveBonusSymbol(type, buyConfig, jackpotLogic, randomFn, jackpotTierHits));
    }
  } else if (board) {
    for (const reel of board) {
      for (const symbolId of reel) {
        if (symbolId === 7) {
          locked.push(resolveBonusSymbol("coin", null, jackpotLogic, randomFn, jackpotTierHits));
        } else if (symbolId === 8) {
          locked.push(resolveBonusSymbol("collector", null, jackpotLogic, randomFn, jackpotTierHits));
        } else if (symbolId === 9) {
          locked.push(resolveBonusSymbol("jackpot", null, jackpotLogic, randomFn, jackpotTierHits));
        }
      }
    }
  }

  let spinsLeft = Number(bonusConfig.entrySpins);
  while (spinsLeft > 0 && locked.length < bonusConfig.maxGridSymbols) {
    const stageIndex = Math.max(0, Math.min(2, bonusConfig.entrySpins - spinsLeft));
    const stageChance = Number(
      (buyConfig?.bonusLandingChanceByRespinIndex ?? bonusConfig.newSymbolChanceByRespinIndex)[stageIndex],
    );
    const empties = bonusConfig.maxGridSymbols - locked.length;
    let landedThisSpin = 0;
    const perSlotChance = stageChance / 4;

    for (let i = 0; i < empties; i += 1) {
      if (randomFn() < perSlotChance) {
        const type = weightedPick(bonusConfig.newBonusSymbolWeights, randomFn);
        locked.push(resolveBonusSymbol(type, buyConfig, jackpotLogic, randomFn, jackpotTierHits));
        landedThisSpin += 1;
      }
    }

    if (landedThisSpin > 0) {
      spinsLeft = Number(bonusConfig.entrySpins);
    } else {
      spinsLeft -= 1;
    }
  }

  const coinSum = locked.filter((item) => item.type === "coin").reduce((sum, item) => sum + item.value, 0);
  const collectorCount = locked.filter((item) => item.type === "collector").length;
  const jackpotSum = locked
    .filter((item) => item.type === "jackpot")
    .reduce((sum, item) => sum + item.value, 0);
  const collectWin = round3(coinSum * collectorCount * 0.24);
  const totalWin = round3(coinSum + collectWin + jackpotSum);

  return {
    totalWin,
    jackpotHit: jackpotSum > 0,
    jackpotTierHits,
  };
}

function resolveBonusSymbol(typeRaw, buyConfig, jackpotLogic, randomFn, jackpotTierHits) {
  const type = String(typeRaw);
  if (type === "coin") {
    const pool = featureTables.holdAndWinBonus.coinValueMultipliers;
    const idx = Math.floor(randomFn() * pool.length);
    return {
      type: "coin",
      value: Number(pool[idx]),
    };
  }
  if (type === "collector") {
    return {
      type: "collector",
      value: 0,
    };
  }

  const weights = buyConfig?.jackpotTierWeightOverride ?? jackpotLogic.tierSelectionWeights;
  const tier = weightedPick(weights, randomFn);
  const value = Number(jackpots.levels[tier] ?? 0);
  jackpotTierHits[tier] += 1;
  return {
    type: "jackpot",
    tier,
    value,
  };
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
        `jackpot=${modeResult.jackpotFrequencyPercent}%`,
        `avgWin=${modeResult.averagePayoutMultiplier}x`,
      ].join(" "),
    );
  }
  console.log(`report=${outPathValue}`);
}
