#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import { fileURLToPath } from "node:url";

import {
  ProvisionalMathSource,
  type ProvisionalMathMode,
  type ProvisionalMathPreset,
} from "../src/app/runtime/provisionalMathSource.ts";

type GeneratorTraceSpin = {
  spinIndex: number;
  mode: ProvisionalMathMode;
  preset: ProvisionalMathPreset;
  requestedPreset: ProvisionalMathPreset | null;
  effectivePreset: ProvisionalMathPreset | null;
  seedStateBefore: number;
  seedStateAfter: number;
  sourcePathUsed: "weighted-reel-rng" | "canned-preset-override";
  rawGeneratedMatrix: number[][];
  finalPresentedMatrix: number[][];
  visibleMatrix: number[][];
  boardHash: string;
  repeatedBoard: boolean;
  lineWins: unknown[];
  totalWin: number;
  collectTriggered: boolean;
  boostTriggered: boolean;
  bonusTriggered: boolean;
  jackpotTriggered: boolean;
  postGenerationRewrite: boolean;
};

type RuntimeSanitySpin = {
  spinIndex: number;
  visibleMatrix: number[][];
  lineWins: unknown[];
  totalWin: number;
  collectTriggered: boolean;
  boostTriggered: boolean;
  bonusTriggered: boolean;
  jackpotTriggered: boolean;
  repeatedBoard: boolean;
};

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const gameRoot = path.resolve(__dirname, "..");
const docsRoot = path.resolve(gameRoot, "docs");

const args = parseArgs(process.argv.slice(2));
const spins = Math.max(1, args.spins);
const seed = args.seed;
const mode: ProvisionalMathMode = args.mode;
const requestedPreset = args.preset;

const source = new ProvisionalMathSource(seed);
const seenBoardHashes = new Set<string>();
const traceRows: GeneratorTraceSpin[] = [];
const sanityRows: RuntimeSanitySpin[] = [];

let winCount = 0;
let repeatedBoardCount = 0;
let collectCount = 0;
let boostCount = 0;
let bonusCount = 0;
let jackpotCount = 0;

for (let spinIndex = 1; spinIndex <= spins; spinIndex += 1) {
  const outcome = source.nextOutcome({
    totalBetMinor: args.totalBetMinor,
    mode,
    requestedPreset,
  });

  const boardHash = outcome.generatorTrace.boardHash;
  const repeatedBoard = seenBoardHashes.has(boardHash);
  if (repeatedBoard) {
    repeatedBoardCount += 1;
  }
  seenBoardHashes.add(boardHash);

  const hasWin = outcome.totalWinAmountMinor > 0 || outcome.lineWins.length > 0;
  if (hasWin) {
    winCount += 1;
  }
  if (outcome.triggers.collect) {
    collectCount += 1;
  }
  if (outcome.triggers.boost) {
    boostCount += 1;
  }
  if (outcome.triggers.bonus) {
    bonusCount += 1;
  }
  if (outcome.triggers.jackpot) {
    jackpotCount += 1;
  }

  traceRows.push({
    spinIndex,
    mode: outcome.mode,
    preset: outcome.preset,
    requestedPreset: outcome.generatorTrace.requestedPreset,
    effectivePreset: outcome.generatorTrace.effectivePreset,
    seedStateBefore: outcome.generatorTrace.seedStateBefore,
    seedStateAfter: outcome.generatorTrace.seedStateAfter,
    sourcePathUsed: outcome.generatorTrace.sourcePath,
    rawGeneratedMatrix: cloneMatrix(outcome.generatorTrace.rawGeneratedMatrix),
    finalPresentedMatrix: cloneMatrix(outcome.generatorTrace.finalPresentedMatrix),
    visibleMatrix: cloneMatrix(outcome.reelStops),
    boardHash,
    repeatedBoard,
    lineWins: cloneJson(outcome.lineWins),
    totalWin: outcome.totalWinAmountMinor,
    collectTriggered: outcome.triggers.collect,
    boostTriggered: outcome.triggers.boost,
    bonusTriggered: outcome.triggers.bonus,
    jackpotTriggered: outcome.triggers.jackpot,
    postGenerationRewrite: outcome.generatorTrace.postGenerationRewrite,
  });

  sanityRows.push({
    spinIndex,
    visibleMatrix: cloneMatrix(outcome.reelStops),
    lineWins: cloneJson(outcome.lineWins),
    totalWin: outcome.totalWinAmountMinor,
    collectTriggered: outcome.triggers.collect,
    boostTriggered: outcome.triggers.boost,
    bonusTriggered: outcome.triggers.bonus,
    jackpotTriggered: outcome.triggers.jackpot,
    repeatedBoard,
  });
}

const tracePath = path.resolve(
  docsRoot,
  args.traceOut ?? "PROVISIONAL_GENERATOR_TRACE_100_SPINS.json",
);
const sanityPath = path.resolve(
  docsRoot,
  args.sanityOut ?? "RUNTIME_SANITY_100_SPINS_AFTER_GENERATOR_FIX.json",
);

fs.mkdirSync(path.dirname(tracePath), { recursive: true });
fs.mkdirSync(path.dirname(sanityPath), { recursive: true });
fs.writeFileSync(tracePath, `${JSON.stringify(traceRows, null, 2)}\n`, "utf8");
fs.writeFileSync(sanityPath, `${JSON.stringify(sanityRows, null, 2)}\n`, "utf8");

const summary = {
  spins,
  seed,
  mode,
  requestedPreset: requestedPreset ?? "none",
  totalBetMinor: args.totalBetMinor,
  winCount,
  winRatePercent: Number(((winCount / spins) * 100).toFixed(2)),
  repeatedBoardCount,
  uniqueBoardCount: seenBoardHashes.size,
  collectCount,
  boostCount,
  bonusCount,
  jackpotCount,
  tracePath,
  sanityPath,
};

process.stdout.write(`${JSON.stringify(summary, null, 2)}\n`);

function parseArgs(raw: string[]): {
  spins: number;
  seed: number;
  mode: ProvisionalMathMode;
  preset: ProvisionalMathPreset | null;
  totalBetMinor: number;
  traceOut: string | null;
  sanityOut: string | null;
} {
  const parsed = {
    spins: 100,
    seed: 700020260316,
    mode: "base" as ProvisionalMathMode,
    preset: null as ProvisionalMathPreset | null,
    totalBetMinor: 1,
    traceOut: null as string | null,
    sanityOut: null as string | null,
  };

  for (let index = 0; index < raw.length; index += 1) {
    const token = raw[index];
    const next = raw[index + 1];

    if (token === "--spins" && next) {
      parsed.spins = Number(next);
      index += 1;
      continue;
    }
    if (token === "--seed" && next) {
      parsed.seed = Number(next);
      index += 1;
      continue;
    }
    if (token === "--mode" && next) {
      parsed.mode = next as ProvisionalMathMode;
      index += 1;
      continue;
    }
    if (token === "--preset" && next) {
      parsed.preset = next as ProvisionalMathPreset;
      index += 1;
      continue;
    }
    if (token === "--total-bet-minor" && next) {
      parsed.totalBetMinor = Number(next);
      index += 1;
      continue;
    }
    if (token === "--trace-out" && next) {
      parsed.traceOut = next;
      index += 1;
      continue;
    }
    if (token === "--sanity-out" && next) {
      parsed.sanityOut = next;
      index += 1;
      continue;
    }
  }

  if (!Number.isFinite(parsed.spins) || parsed.spins <= 0) {
    parsed.spins = 100;
  }
  if (!Number.isFinite(parsed.seed)) {
    parsed.seed = 700020260316;
  }
  if (!Number.isFinite(parsed.totalBetMinor) || parsed.totalBetMinor <= 0) {
    parsed.totalBetMinor = 1;
  }
  if (
    parsed.mode !== "base" &&
    parsed.mode !== "buy75" &&
    parsed.mode !== "buy200" &&
    parsed.mode !== "buy300"
  ) {
    parsed.mode = "base";
  }
  return parsed;
}

function cloneMatrix(matrix: number[][]): number[][] {
  return matrix.map((row) => [...row]);
}

function cloneJson<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}
