import {
  Application,
  BlurFilter,
  ColorMatrixFilter,
  Container,
  Graphics,
  Text
} from "pixi.js";
import "./style.css";

type OpenGameResponse = {
  sessionId: string;
  balance: number;
  requestCounter: number;
  minBet: number;
  maxBet: number;
};

type PlaceBetResponse = {
  roundId: string;
  balance: number;
  math: {
    bets?: Array<{ betAmount: number; winAmount: number }>;
    totalWinAmount: number;
    details: {
      ballInfo: Array<{ slot: number }>;
      config?: {
        lines?: number;
        risk?: string;
        multipliers?: number[];
      };
    };
  };
};

type CollectResponse = {
  roundId: string;
  balance: number;
  winAmount: number;
};

type ReadHistoryEntry = {
  created: string;
  roundUuid: string;
  sumBetAmount: number;
  sumWinAmount: number;
  betType: string;
  collected: boolean;
};

type ReadHistoryResponse = {
  requestCounter: number;
  history: ReadHistoryEntry[];
};

type ErrorEnvelope = {
  error?: {
    code: string;
    message: string;
  };
};

type OpenGameOptions = {
  sessionId: string;
  bankId?: number;
  playerId?: string;
  gsInternalBaseUrl?: string;
  gameId?: number;
};

type HistoryRow = {
  created: string;
  roundId: string;
  betAmount: number;
  winAmount: number;
  betType: string;
  collected: boolean;
};

type RiskLevel = "low" | "medium" | "high";

const DEFAULT_ROWS = 7;
const DEFAULT_RISK: RiskLevel = "medium";
const ALLOWED_ROWS = [5, 6, 7, 8] as const;
const PRELOADER_MIN_VISIBLE_MS = 3400;
const PIN_VISUAL_SCALE = 1.28;
const BALL_RADIUS = 10;
const PEG_COLLISION_RADIUS = 7.6;

const PAYOUT_TABLES: Record<number, Record<RiskLevel, number[]>> = {
  5: {
    low: [2.1, 1.2, 0.8, 1.2, 2.1, 3.4],
    medium: [9, 2.3, 0.9, 0.9, 2.3, 9],
    high: [20, 3.9, 0.5, 0.5, 3.9, 20]
  },
  6: {
    low: [2.4, 1.4, 1, 0.8, 1, 1.4, 2.4],
    medium: [11.5, 3, 1.1, 0.7, 1.1, 3, 11.5],
    high: [26, 5.2, 1.1, 0.4, 1.1, 5.2, 26]
  },
  7: {
    low: [3, 1.8, 1.2, 1, 1, 1.2, 1.8, 3],
    medium: [14, 4, 1.6, 0.7, 0.7, 1.6, 4, 14],
    high: [30, 7, 2, 0.5, 0.5, 2, 7, 30]
  },
  8: {
    low: [2.5, 2.1, 1.2, 1, 0.4, 1, 1.2, 2.1, 2.5],
    medium: [12.2, 3, 1.2, 0.7, 0.4, 0.7, 1.2, 3, 12.2],
    high: [28, 4, 1.4, 0.3, 0.2, 0.3, 1.4, 4, 28]
  },
  9: {
    low: [3.5, 2.5, 1.5, 1, 0.65, 0.65, 1, 1.5, 2.5, 3.5],
    medium: [13.5, 4.1, 1.7, 1, 0.4, 0.4, 1, 1.7, 4.1, 13.5],
    high: [43, 6.95, 1.8, 0.6, 0.2, 0.2, 0.6, 1.8, 6.95, 43]
  },
  10: {
    low: [6.5, 3, 1.4, 1.1, 1, 0.4, 1, 1.1, 1.4, 3, 6.5],
    medium: [20, 4.65, 2, 1.5, 0.5, 0.4, 0.5, 1.5, 2, 4.65, 20],
    high: [72, 10, 2.75, 0.9, 0.3, 0.2, 0.3, 0.9, 2.75, 10, 72]
  },
  11: {
    low: [8.5, 3.4, 2.1, 1.3, 1, 0.6, 0.6, 1, 1.3, 2.1, 3.4, 8.5],
    medium: [24, 6.2, 3, 1.8, 0.6, 0.5, 0.5, 0.6, 1.8, 3, 6.2, 24],
    high: [108, 13.2, 5, 1.4, 0.4, 0.2, 0.2, 0.4, 1.4, 5, 13.2, 108]
  },
  12: {
    low: [9, 3.4, 2, 1.2, 1.1, 1, 0.4, 1, 1.1, 1.2, 2, 3.4, 9],
    medium: [30, 11, 4, 2.1, 1.1, 0.5, 0.3, 0.5, 1.1, 2.1, 4, 11, 30],
    high: [150, 21, 8, 2, 0.7, 0.2, 0.2, 0.2, 0.7, 2, 8, 21, 150]
  },
  13: {
    low: [9, 4.5, 3.2, 2, 1.2, 0.9, 0.6, 0.6, 0.9, 1.2, 2, 3.2, 4.5, 9],
    medium: [42, 12, 6.3, 3, 1.3, 0.6, 0.4, 0.4, 0.6, 1.3, 3, 6.3, 12, 42],
    high: [240, 35, 10, 4, 1, 0.2, 0.2, 0.2, 0.2, 1, 4, 10, 35, 240]
  },
  14: {
    low: [8, 4, 2, 1.45, 1.2, 1.1, 1, 0.4, 1, 1.1, 1.2, 1.45, 2, 4, 8],
    medium: [58, 15.5, 7.2, 4.1, 1.6, 1, 0.5, 0.2, 0.5, 1, 1.6, 4.1, 7.2, 15.5, 58],
    high: [400, 55, 16, 5.2, 1.8, 0.3, 0.2, 0.2, 0.2, 0.3, 1.8, 5.2, 16, 55, 400]
  },
  15: {
    low: [15, 8, 3.2, 2, 1.6, 1.1, 1, 0.6, 0.6, 1, 1.1, 1.6, 2, 3.2, 8, 15],
    medium: [88, 18, 11.2, 5.2, 3, 1.1, 0.5, 0.3, 0.3, 0.5, 1.1, 3, 5.2, 11.2, 18, 88],
    high: [620, 80, 25, 7.5, 3, 0.5, 0.2, 0.2, 0.2, 0.2, 0.5, 3, 7.5, 25, 80, 620]
  },
  16: {
    low: [16, 8.5, 2.2, 1.45, 1.2, 1.2, 1.1, 1, 0.4, 1, 1.1, 1.2, 1.2, 1.45, 2.2, 8.5, 16],
    medium: [108, 38, 9.2, 5.1, 3, 1.3, 1, 0.5, 0.3, 0.5, 1, 1.3, 3, 5.1, 9.2, 38, 108],
    high: [888, 112, 26, 8, 4, 2, 0.2, 0.2, 0.2, 0.2, 0.2, 2, 4, 8, 26, 112, 888]
  }
};

type BoardGeometry = {
  centerX: number;
  startY: number;
  pegSpacingX: number;
  pegSpacingY: number;
  rowPegY: number[];
  rowPegXs: number[][];
  laneX: number;
  laneY: number;
  laneWidth: number;
  laneHeight: number;
  pocketTopY: number;
  binsY: number;
  binsBottomY: number;
  binsStartX: number;
  binWidth: number;
  binGap: number;
  binCenters: number[];
  dividerXs: number[];
  multipliers: number[];
  slotGlows: Graphics[];
  pegPoints: Array<{ x: number; y: number }>;
  pegActors: Array<{ shadow: Graphics; player: Container }>;
};

type SimulatedPath = {
  points: Array<{ x: number; y: number; rotation: number }>;
  pegHits: Array<{ frame: number; x: number; y: number; pegIndex: number }>;
};

type PhysicsTuning = {
  bounce: number;
  rowImpulse: number;
  bucketPull: number;
  pegNudge: number;
};


class NgsApi {
  sessionId = "";
  requestCounter = 0;
  private readonly apiBase: string;
  private operationSequence = 0;

  constructor(apiBase: string) {
    this.apiBase = apiBase.replace(/\/+$/, "");
  }

  private nextOperationId(prefix: string): string {
    this.operationSequence += 1;
    return `${prefix}-${Date.now()}-${this.operationSequence}`;
  }

  private async post<TReq extends Record<string, unknown>, TRes>(
    path: string,
    body: TReq
  ): Promise<TRes> {
    const response = await fetch(`${this.apiBase}${path}`, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(body)
    });

    const data = ((await response.json()) ?? {}) as TRes & ErrorEnvelope;
    if (!response.ok) {
      throw new Error(data.error?.message ?? `Request failed: ${response.status}`);
    }

    return data as TRes;
  }

  async openGame(options: OpenGameOptions): Promise<OpenGameResponse> {
    const response = await this.post<OpenGameOptions, OpenGameResponse>("/v1/opengame", {
      sessionId: options.sessionId,
      bankId: options.bankId,
      playerId: options.playerId,
      gsInternalBaseUrl: options.gsInternalBaseUrl,
      gameId: options.gameId
    });
    this.sessionId = response.sessionId;
    this.requestCounter = response.requestCounter;
    return response;
  }

  async placeBet(betAmount: number, betType: string): Promise<PlaceBetResponse> {
    const requestCounter = this.requestCounter + 1;
    const response = await this.post<
      {
        sessionId: string;
        requestCounter: number;
        clientOperationId: string;
        bets: Array<{ betType: string; betAmount: number }>;
      },
      PlaceBetResponse
    >("/v1/placebet", {
      sessionId: this.sessionId,
      requestCounter,
      clientOperationId: this.nextOperationId("place"),
      bets: [{ betType, betAmount }]
    });
    this.requestCounter = requestCounter;
    return response;
  }

  async collect(roundId: string): Promise<CollectResponse> {
    const requestCounter = this.requestCounter + 1;
    const response = await this.post<
      {
        sessionId: string;
        requestCounter: number;
        roundId: string;
        clientOperationId: string;
      },
      CollectResponse
    >("/v1/collect", {
      sessionId: this.sessionId,
      requestCounter,
      roundId,
      clientOperationId: this.nextOperationId("collect")
    });
    this.requestCounter = requestCounter;
    return response;
  }

  async readHistory(pageNumber = 0): Promise<ReadHistoryResponse> {
    if (!this.sessionId) {
      throw new Error("Open game before syncing history");
    }

    const requestCounter = this.requestCounter + 1;
    const response = await this.post<
      {
        sessionId: string;
        requestCounter: number;
        pageNumber: number;
      },
      ReadHistoryResponse
    >("/v1/readhistory", {
      sessionId: this.sessionId,
      requestCounter,
      pageNumber
    });

    this.requestCounter = requestCounter;
    return response;
  }
}

function hashSeed(input: string): number {
  let value = 2166136261;
  for (let i = 0; i < input.length; i += 1) {
    value ^= input.charCodeAt(i);
    value += (value << 1) + (value << 4) + (value << 7) + (value << 8) + (value << 24);
  }
  return value >>> 0;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value));
}

function createSeededRandom(seed: string): () => number {
  let state = hashSeed(seed);
  return () => {
    state = (state * 1664525 + 1013904223) >>> 0;
    return state / 0x100000000;
  };
}

function buildGuidedDecisions(
  rows: number,
  targetSlot: number,
  rng: () => number,
  steeringBias: number
): number[] {
  const safeRows = Math.max(1, rows);
  const safeTarget = clamp(Math.round(targetSlot), 0, safeRows);
  const decisions: number[] = [];
  let rightsUsed = 0;

  for (let step = 0; step < safeRows; step += 1) {
    const remaining = safeRows - step;
    const rightsLeft = safeTarget - rightsUsed;
    let goRight = false;
    if (rightsLeft <= 0) {
      goRight = false;
    } else if (rightsLeft >= remaining) {
      goRight = true;
    } else {
      const idealRatio = rightsLeft / remaining;
      const noisyRatio = clamp(idealRatio + (rng() - 0.5) * 0.18, 0.04, 0.96);
      const threshold = clamp(noisyRatio * steeringBias + idealRatio * (1 - steeringBias), 0.04, 0.96);
      goRight = rng() < threshold;
    }

    if (goRight) {
      rightsUsed += 1;
      decisions.push(1);
    } else {
      decisions.push(-1);
    }
  }

  return decisions;
}

function toCurrency(value: number): string {
  return `$${(value / 100).toFixed(2)}`;
}

function toClock(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "--:--:--";
  }
  return date.toLocaleTimeString([], { hour12: false });
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

function formatClientError(error: unknown): string {
  const message = String(error);
  if (message.includes("Session is stale or expired")) {
    return "Session expired or was replaced by a newer launch. Reload the launch URL and try again.";
  }
  return message;
}

function normalizeRisk(value: string): RiskLevel {
  const normalized = value.trim().toLowerCase();
  if (normalized === "low" || normalized === "medium" || normalized === "high") {
    return normalized;
  }
  return DEFAULT_RISK;
}

function normalizeRows(value: number): number {
  if (Number.isInteger(value) && ALLOWED_ROWS.includes(value as (typeof ALLOWED_ROWS)[number])) {
    return value;
  }
  return DEFAULT_ROWS;
}

function formatMultiplier(value: number): string {
  return Number(value.toFixed(2)).toString();
}

function findPaidSlot(
  betAmount: number,
  winAmount: number,
  multipliers: number[],
  preferredSlot: number
): number {
  if (!Number.isFinite(betAmount) || betAmount <= 0 || multipliers.length === 0) {
    return clamp(preferredSlot, 0, Math.max(0, multipliers.length - 1));
  }

  let bestSlot = clamp(preferredSlot, 0, multipliers.length - 1);
  let bestDiff = Number.POSITIVE_INFINITY;
  for (let i = 0; i < multipliers.length; i += 1) {
    const projected = Math.round(betAmount * (multipliers[i] ?? 0));
    const diff = Math.abs(projected - winAmount);
    if (diff < bestDiff) {
      bestDiff = diff;
      bestSlot = i;
    } else if (diff === bestDiff && Math.abs(i - preferredSlot) < Math.abs(bestSlot - preferredSlot)) {
      bestSlot = i;
    }
  }

  return bestSlot;
}

function resolveMultipliers(risk: string, rows: number): number[] {
  const safeRisk = normalizeRisk(risk);
  const safeRows = normalizeRows(rows);
  return PAYOUT_TABLES[safeRows]?.[safeRisk] ?? PAYOUT_TABLES[DEFAULT_ROWS][DEFAULT_RISK];
}

function slotColor(slot: number, slotCount: number): number {
  const center = (slotCount - 1) * 0.5;
  const edgeDistance = Math.abs(slot - center) / Math.max(center, 1);
  if (edgeDistance >= 0.82) {
    return 0xbe1f2f;
  }
  if (edgeDistance >= 0.62) {
    return 0xd84c2f;
  }
  if (edgeDistance >= 0.36) {
    return 0xea8121;
  }
  if (edgeDistance >= 0.16) {
    return 0xf0ad24;
  }
  return 0xf4ce34;
}

async function bootstrap() {
  const launchParams = new URLSearchParams(window.location.search);
  const getLaunchParam = (keys: string[]): string | null => {
    for (const key of keys) {
      const value = launchParams.get(key);
      if (value && value.trim().length > 0) {
        return value;
      }
    }
    return null;
  };

  const launchSessionId = getLaunchParam(["sid", "SID", "sessionId", "SESSIONID"]);
  const launchBankIdValue = getLaunchParam(["bankId", "BANKID"]);
  const launchBankId = launchBankIdValue ? Number(launchBankIdValue) : undefined;
  const launchGameIdValue = getLaunchParam(["gameIdNumeric", "GAMEIDNUMERIC", "gameId", "GAMEID"]);
  const launchGameId = launchGameIdValue && !Number.isNaN(Number(launchGameIdValue))
    ? Number(launchGameIdValue)
    : undefined;
  const launchNgsApiUrl =
    getLaunchParam(["ngsApiUrl", "NGSAPIURL"]) ?? import.meta.env.VITE_NGS_API_BASE ?? "http://localhost:6400";
  const launchGsInternalBaseUrl = getLaunchParam(["gsInternalBaseUrl", "GSINTERNALBASEURL"]) ?? undefined;

  const appShell = document.querySelector<HTMLDivElement>("#app-shell")!;
  const preloader = document.querySelector<HTMLDivElement>("#preloader");
  const preloaderText = document.querySelector<HTMLElement>("#preloader-text");
  const preloaderProgress = document.querySelector<HTMLElement>(".preloader-progress-bar");
  const topMeta = document.querySelector<HTMLElement>("#top-meta")!;
  const stageClock = document.querySelector<HTMLElement>("#stage-clock")!;
  const stageWrap = document.querySelector<HTMLDivElement>("#stage-wrap")!;
  const status = document.querySelector<HTMLPreElement>("#status")!;
  const sessionInput = document.querySelector<HTMLInputElement>("#session-id")!;
  const betInput = document.querySelector<HTMLInputElement>("#bet-amount")!;
  const riskInput = document.querySelector<HTMLSelectElement>("#risk")!;
  const rowsInput = document.querySelector<HTMLSelectElement>("#rows")!;
  const animationSpeedInput = document.querySelector<HTMLSelectElement>("#animation-speed")!;
  const soundEnabledInput = document.querySelector<HTMLInputElement>("#sound-enabled")!;
  const autoRoundsInput = document.querySelector<HTMLInputElement>("#auto-rounds")!;
  const autoIntervalInput = document.querySelector<HTMLInputElement>("#auto-interval")!;
  const autoSkipAnimationInput = document.querySelector<HTMLInputElement>("#auto-skip-animation")!;
  const tuneBounceInput = document.querySelector<HTMLInputElement>("#tune-bounce")!;
  const tuneRowImpulseInput = document.querySelector<HTMLInputElement>("#tune-row-impulse")!;
  const tuneBucketPullInput = document.querySelector<HTMLInputElement>("#tune-bucket-pull")!;
  const tunePegNudgeInput = document.querySelector<HTMLInputElement>("#tune-peg-nudge")!;
  const tuneBounceValue = document.querySelector<HTMLElement>("#tune-bounce-value")!;
  const tuneRowImpulseValue = document.querySelector<HTMLElement>("#tune-row-impulse-value")!;
  const tuneBucketPullValue = document.querySelector<HTMLElement>("#tune-bucket-pull-value")!;
  const tunePegNudgeValue = document.querySelector<HTMLElement>("#tune-peg-nudge-value")!;
  const spinButton = document.querySelector<HTMLButtonElement>("#spin")!;
  const autoStartButton = document.querySelector<HTMLButtonElement>("#autobet-start")!;
  const autoStopButton = document.querySelector<HTMLButtonElement>("#autobet-stop")!;
  const autoProgress = document.querySelector<HTMLElement>("#autobet-progress")!;
  const historyRefreshButton = document.querySelector<HTMLButtonElement>("#history-refresh")!;
  const historyList = document.querySelector<HTMLDivElement>("#history-list")!;
  const paytablePreview = document.querySelector<HTMLElement>("#paytable-preview")!;
  const balanceValue = document.querySelector<HTMLElement>("#balance")!;
  const requestCounterValue = document.querySelector<HTMLElement>("#request-counter")!;
  const roundIdValue = document.querySelector<HTMLElement>("#round-id")!;
  const lastWinValue = document.querySelector<HTMLElement>("#last-win")!;
  const totalWinValue = document.querySelector<HTMLElement>("#total-win")!;
  const preloaderStartedAt = Date.now();

  function setPreloaderMessage(message: string, progress?: number): void {
    if (preloaderText) {
      preloaderText.textContent = message;
    }
    if (preloaderProgress && progress !== undefined) {
      preloaderProgress.style.width = `${clamp(progress, 0, 100)}%`;
    }
  }

  async function hidePreloader(): Promise<void> {
    const elapsed = Date.now() - preloaderStartedAt;
    const remaining = PRELOADER_MIN_VISIBLE_MS - elapsed;
    if (remaining > 0) {
      await sleep(remaining);
    }

    setPreloaderMessage("Ready to play.", 100);
    await sleep(180);
    appShell.classList.remove("app-loading");
    if (preloader) {
      preloader.classList.add("is-hidden");
      window.setTimeout(() => preloader.remove(), 360);
    }
  }

  function updateStageClock(): void {
    const now = new Date();
    stageClock.textContent = now.toLocaleString([], { hour12: false });
  }

  const topMetaParts = [`API ${launchNgsApiUrl.replace(/^https?:\/\//, "")}`];
  if (launchBankId !== undefined) {
    topMetaParts.push(`Bank ${launchBankId}`);
  }
  if (launchGameId !== undefined) {
    topMetaParts.push(`Game ${launchGameId}`);
  }
  topMeta.textContent = topMetaParts.join(" • ");

  updateStageClock();
  window.setInterval(updateStageClock, 1000);
  setPreloaderMessage("Initializing game runtime...", 12);

  if (launchSessionId) {
    sessionInput.value = launchSessionId;
  }
  rowsInput.value = String(normalizeRows(Number(rowsInput.value)));
  riskInput.value = normalizeRisk(riskInput.value);

  const api = new NgsApi(launchNgsApiUrl);
  const historyRows: HistoryRow[] = [];
  const statusLines: string[] = [];
  let currentSessionId = "";
  let totalWinAmount = 0;
  let busy = false;
  let autoRunning = false;

  const openGameOptions = (): OpenGameOptions => ({
    sessionId: sessionInput.value.trim(),
    bankId: launchBankId,
    gsInternalBaseUrl: launchGsInternalBaseUrl,
    gameId: launchGameId
  });

  function setStatus(line: string): void {
    statusLines.unshift(`[${new Date().toLocaleTimeString()}] ${line}`);
    while (statusLines.length > 8) {
      statusLines.pop();
    }
    status.textContent = statusLines.join("\n");
  }

  function syncCounters(): void {
    requestCounterValue.textContent = String(api.requestCounter);
  }

  function updateTotalWin(value: number): void {
    totalWinAmount = Math.max(0, Math.round(value));
    totalWinValue.textContent = toCurrency(totalWinAmount);
  }

  function resetHistory(): void {
    historyRows.length = 0;
    updateTotalWin(0);
    renderHistory();
  }

  function renderHistory(): void {
    if (historyRows.length === 0) {
      historyList.innerHTML = '<div class="history-empty">No rounds yet.</div>';
      return;
    }

    historyList.innerHTML = historyRows
      .map((item) => {
        const outcomeClass = item.winAmount > 0 ? "win" : "loss";
        const shortRoundId = item.roundId.slice(0, 8);
        return `<div class="history-row"><strong>${toClock(item.created)}</strong><span>${shortRoundId}</span><span>${toCurrency(item.betAmount)}</span><span class="pill ${outcomeClass}">${toCurrency(item.winAmount)}</span></div>`;
      })
      .join("");
  }

  function pushHistory(row: HistoryRow): void {
    const existingIndex = historyRows.findIndex((entry) => entry.roundId === row.roundId);
    if (existingIndex >= 0) {
      historyRows.splice(existingIndex, 1);
    }

    historyRows.unshift(row);
    while (historyRows.length > 20) {
      historyRows.pop();
    }
    renderHistory();
  }

  function syncHistoryFromServer(entries: ReadHistoryEntry[]): void {
    historyRows.length = 0;
    let syncedTotalWin = 0;
    for (const entry of entries) {
      historyRows.push({
        created: entry.created,
        roundId: entry.roundUuid,
        betAmount: entry.sumBetAmount,
        winAmount: entry.sumWinAmount,
        betType: entry.betType,
        collected: entry.collected
      });
      syncedTotalWin += entry.sumWinAmount;
    }
    updateTotalWin(syncedTotalWin);
    renderHistory();
  }

  function selectedRows(): number {
    return normalizeRows(Number(rowsInput.value));
  }

  function selectedRisk(): RiskLevel {
    return normalizeRisk(riskInput.value);
  }

  function selectedMultipliers(): number[] {
    return resolveMultipliers(selectedRisk(), selectedRows());
  }

  function updatePaytablePreview(): void {
    const rows = selectedRows();
    const risk = selectedRisk();
    const multipliers = selectedMultipliers();
    paytablePreview.textContent = `Risk ${risk.toUpperCase()} • Lines ${rows} • Buckets: ${multipliers
      .map((value) => `${formatMultiplier(value)}x`)
      .join(" · ")}`;
  }

  function ballSpeedMultiplier(): number {
    const value = Number(animationSpeedInput.value);
    if (!Number.isFinite(value) || value <= 0) {
      return 1;
    }
    return clamp(value, 0.6, 2);
  }

  function readPhysicsTuning(): PhysicsTuning {
    return {
      bounce: clamp(Number(tuneBounceInput.value) || 0.46, 0.1, 0.6),
      rowImpulse: clamp(Number(tuneRowImpulseInput.value) || 18, 4, 28),
      bucketPull: clamp(Number(tuneBucketPullInput.value) || 0.0012, 0, 0.003),
      pegNudge: clamp(Number(tunePegNudgeInput.value) || 22, 0, 24)
    };
  }

  function syncTuningLabels(): void {
    const t = readPhysicsTuning();
    tuneBounceValue.textContent = t.bounce.toFixed(2);
    tuneRowImpulseValue.textContent = t.rowImpulse.toFixed(0);
    tuneBucketPullValue.textContent = t.bucketPull.toFixed(4);
    tunePegNudgeValue.textContent = t.pegNudge.toFixed(0);
  }

  function updateControls(): void {
    const locked = busy || autoRunning;
    spinButton.disabled = locked;
    autoStartButton.disabled = locked;
    historyRefreshButton.disabled = locked;
    autoStopButton.disabled = !autoRunning;
    sessionInput.disabled = locked;
    betInput.disabled = locked;
    riskInput.disabled = locked;
    rowsInput.disabled = locked;
    animationSpeedInput.disabled = locked;
  }

  function drawBackground(): void {
    bgLayer.removeChildren();

    const stageWidth = stageWrap.clientWidth;
    const stageHeight = stageWrap.clientHeight;

    const frame = new Graphics();
    frame.rect(0, 0, stageWidth, stageHeight).fill({ color: 0x080b11 });
    bgLayer.addChild(frame);

    const ambientA = new Graphics();
    ambientA.circle(stageWidth * 0.2, stageHeight * 0.14, stageWidth * 0.35).fill({
      color: 0xbb1824,
      alpha: 0.2
    });
    ambientA.filters = [new BlurFilter({ strength: 66 })];
    bgLayer.addChild(ambientA);

    const ambientB = new Graphics();
    ambientB.circle(stageWidth * 0.8, stageHeight * 0.84, stageWidth * 0.3).fill({
      color: 0x153968,
      alpha: 0.14
    });
    ambientB.filters = [new BlurFilter({ strength: 52 })];
    bgLayer.addChild(ambientB);

    const topShadow = new Graphics();
    topShadow.roundRect(stageWidth * 0.12, 16, stageWidth * 0.76, 46, 14).fill({
      color: 0x03060a,
      alpha: 0.62
    });
    bgLayer.addChild(topShadow);
  }

  function drawBoard(rows: number, multipliers: number[]): BoardGeometry {
    boardLayer.removeChildren();

    const stageWidth = stageWrap.clientWidth;
    const stageHeight = stageWrap.clientHeight;
    const availableHeight = Math.max(320, stageHeight - 10);
    const rowScale = clamp((rows - 5) / 3, 0, 1);
    const boardHeight = clamp(availableHeight * (0.9 + rowScale * 0.06), 420, availableHeight);
    const boardWidth = clamp(stageWidth * (0.95 + rowScale * 0.03), 560, stageWidth - 8);
    const boardX = (stageWidth - boardWidth) / 2;
    const boardY = Math.max(4, (stageHeight - boardHeight) * 0.5);

    const laneInset = Math.max(12, boardWidth * 0.022);
    const laneX = boardX + laneInset;
    const laneY = boardY + laneInset;
    const laneWidth = boardWidth - laneInset * 2;
    const laneHeight = boardHeight - laneInset * 2;

    const centerX = laneX + laneWidth * 0.5;
    const binsY = laneY + laneHeight - 54;
    const binsBottomY = binsY + 44;
    const startY = laneY + 54;
    const pegBottomY = binsY - 52;
    const pegSpacingX = Math.max(34, Math.min(66, (laneWidth - 76) / Math.max(rows + 0.15, 1)));
    const pegSpacingY = Math.max(34, (pegBottomY - startY) / Math.max(rows - 1, 1));
    const pegPoints: Array<{ x: number; y: number }> = [];
    const pegActors: Array<{ shadow: Graphics; player: Container }> = [];
    const rowPegY: number[] = [];
    const rowPegXs: number[][] = [];
    const slotCount = Math.max(3, multipliers.length);

    const rail = new Graphics();
    rail
      .roundRect(boardX, boardY, boardWidth, boardHeight, 26)
      .fill({ color: 0x6d3c1d })
      .stroke({ color: 0xa26933, width: 2, alpha: 0.8 });
    boardLayer.addChild(rail);

    const felt = new Graphics();
    felt
      .roundRect(laneX, laneY, laneWidth, laneHeight, 20)
      .fill({ color: 0x0d642f })
      .stroke({ color: 0xb9e2b4, width: 2, alpha: 0.38 });
    boardLayer.addChild(felt);

    for (let stripe = 0; stripe < 6; stripe += 1) {
      const stripeY = laneY + stripe * (laneHeight / 6);
      const stripeShape = new Graphics();
      stripeShape.roundRect(laneX + 8, stripeY, laneWidth - 16, laneHeight / 12, 8).fill({
        color: 0x104f27,
        alpha: stripe % 2 === 0 ? 0.3 : 0.18
      });
      boardLayer.addChild(stripeShape);
    }

    const markings = new Graphics();
    markings.stroke({ color: 0xd6ebd4, width: 3, alpha: 0.4 });
    markings.circle(centerX, laneY + laneHeight * 0.5, laneWidth * 0.12);
    markings.moveTo(laneX + 14, laneY + laneHeight * 0.5);
    markings.lineTo(laneX + laneWidth - 14, laneY + laneHeight * 0.5);
    markings.roundRect(laneX + 34, laneY + laneHeight * 0.08, laneWidth - 68, laneHeight - 108, 16);
    boardLayer.addChild(markings);

    const goalFrame = new Graphics();
    goalFrame
      .roundRect(centerX - 110, laneY - 28, 220, 34, 8)
      .stroke({ color: 0xe6e8f0, width: 3, alpha: 0.8 });
    for (let i = 0; i < 8; i += 1) {
      const gx = centerX - 108 + i * 30;
      goalFrame.moveTo(gx, laneY - 27);
      goalFrame.lineTo(gx, laneY + 4);
    }
    boardLayer.addChild(goalFrame);

    const topPerspectiveScale = 0.84;
    const pinScale = PIN_VISUAL_SCALE;
    const sx = (value: number): number => value * pinScale;
    for (let row = 0; row < rows; row += 1) {
      // Plinko board starts from a 2-pin row (no single apex pin).
      const pegCount = row + 2;
      const rowDepth = row / Math.max(rows - 1, 1);
      const perspectiveScale = topPerspectiveScale + (1 - topPerspectiveScale) * rowDepth;
      const rowWidth = (pegCount - 1) * pegSpacingX * perspectiveScale;
      const rowStartX = centerX - rowWidth / 2;
      const y = startY + row * pegSpacingY;
      rowPegY.push(y);
      const rowXs: number[] = [];

      for (let peg = 0; peg < pegCount; peg += 1) {
        const x =
          pegCount <= 1
            ? centerX
            : rowStartX + (peg * rowWidth) / Math.max(pegCount - 1, 1);
        pegPoints.push({ x, y });
        rowXs.push(x);

        const pinShadow = new Graphics();
        pinShadow.ellipse(0, 0, sx(10.6), sx(4)).fill({ color: 0x0a2d1b, alpha: 0.24 });
        pinShadow.filters = [new BlurFilter({ strength: 2.2 })];
        pinShadow.x = x + sx(2.7);
        pinShadow.y = y + sx(7.8);
        boardLayer.addChild(pinShadow);

        const jerseyColor = (row + peg) % 2 === 0 ? 0x1b63b1 : 0x178443;
        const sockColor = 0x0f1319;
        const skinColor = 0xf2c58c;

        const player = new Container();
        player.x = x;
        player.y = y;
        boardLayer.addChild(player);

        const bootLeft = new Graphics();
        bootLeft.ellipse(-sx(2.25), sx(8.5), sx(1.8), sx(0.9)).fill({ color: 0x090c11, alpha: 0.95 });
        player.addChild(bootLeft);

        const bootRight = new Graphics();
        bootRight.ellipse(sx(2.25), sx(8.5), sx(1.8), sx(0.9)).fill({ color: 0x090c11, alpha: 0.95 });
        player.addChild(bootRight);

        const playerBody = new Graphics();
        playerBody.ellipse(0, sx(1.7), sx(6.7), sx(7.4)).fill({ color: jerseyColor, alpha: 0.98 });
        playerBody.stroke({ color: 0x0d1a28, width: sx(1.3), alpha: 0.88 });
        player.addChild(playerBody);

        const shoulder = new Graphics();
        shoulder.roundRect(-sx(4.35), -sx(1.45), sx(8.7), sx(2.7), sx(1.1)).fill({
          color: 0xe5f2ff,
          alpha: 0.67
        });
        player.addChild(shoulder);

        const jerseyStripe = new Graphics();
        jerseyStripe.roundRect(-sx(1), -sx(1.4), sx(2), sx(6.8), sx(0.6)).fill({
          color: 0xffffff,
          alpha: 0.7
        });
        player.addChild(jerseyStripe);

        const leftArm = new Graphics();
        leftArm.ellipse(-sx(5.3), sx(0.9), sx(1.25), sx(2.2)).fill({ color: skinColor, alpha: 0.95 });
        player.addChild(leftArm);

        const rightArm = new Graphics();
        rightArm.ellipse(sx(5.3), sx(0.9), sx(1.25), sx(2.2)).fill({ color: skinColor, alpha: 0.95 });
        player.addChild(rightArm);

        const leftLeg = new Graphics();
        leftLeg.roundRect(-sx(2.45), sx(6.2), sx(1.9), sx(2.85), sx(0.75)).fill({
          color: sockColor,
          alpha: 0.92
        });
        player.addChild(leftLeg);

        const rightLeg = new Graphics();
        rightLeg.roundRect(sx(0.55), sx(6.2), sx(1.9), sx(2.85), sx(0.75)).fill({
          color: sockColor,
          alpha: 0.92
        });
        player.addChild(rightLeg);

        const playerHead = new Graphics();
        playerHead.circle(-sx(0.15), -sx(4.55), sx(3.2)).fill({ color: skinColor, alpha: 0.99 });
        playerHead.stroke({ color: 0x6e4c27, width: sx(0.95), alpha: 0.78 });
        player.addChild(playerHead);

        const hair = new Graphics();
        hair.ellipse(-sx(0.25), -sx(5.45), sx(2.45), sx(1.22)).fill({ color: 0x2b1f14, alpha: 0.9 });
        player.addChild(hair);

        const faceShade = new Graphics();
        faceShade.ellipse(-sx(0.3), -sx(3.6), sx(1.6), sx(1)).fill({ color: 0xd49c6b, alpha: 0.28 });
        player.addChild(faceShade);

        const eyeLeft = new Graphics();
        eyeLeft.circle(-sx(0.95), -sx(4.55), sx(0.18)).fill({ color: 0x1a1a1a, alpha: 0.8 });
        player.addChild(eyeLeft);

        const eyeRight = new Graphics();
        eyeRight.circle(sx(0.7), -sx(4.55), sx(0.18)).fill({ color: 0x1a1a1a, alpha: 0.8 });
        player.addChild(eyeRight);

        pegActors.push({ shadow: pinShadow, player });
      }

      rowPegXs.push(rowXs);
    }

    const binGap = slotCount >= 13 ? 4 : 6;
    const bottomRowXs = rowPegXs[rowPegXs.length - 1] ?? [];
    const pyramidBaseWidth =
      bottomRowXs.length > 1
        ? bottomRowXs[bottomRowXs.length - 1]! - bottomRowXs[0]!
        : rows * pegSpacingX;
    const minBinsWidth = slotCount * 22 + (slotCount - 1) * binGap;
    const targetBinsWidth = pyramidBaseWidth + pegSpacingX * 0.95;
    const totalBinsWidth = clamp(targetBinsWidth, minBinsWidth, laneWidth - 18);
    const binWidth = (totalBinsWidth - binGap * (slotCount - 1)) / slotCount;
    const binsStartX = centerX - totalBinsWidth / 2;
    const binCenters: number[] = [];
    const slotGlows: Graphics[] = [];
    const pocketTopY = binsY - 34;
    const dividerXs: number[] = [];
    const labelFontSize = slotCount >= 15 ? 10 : slotCount >= 12 ? 11 : 13;

    for (let divider = 0; divider <= slotCount; divider += 1) {
      const dividerX = binsStartX + divider * (binWidth + binGap) - binGap * 0.5;
      dividerXs.push(dividerX);
      const pocketWall = new Graphics();
      pocketWall
        .roundRect(dividerX - 2.1, pocketTopY, 4.2, 36, 2)
        .fill({ color: 0x102920, alpha: 0.95 })
        .stroke({ color: 0x6eb06a, width: 1, alpha: 0.55 });
      boardLayer.addChild(pocketWall);
    }

    for (let slot = 0; slot < slotCount; slot += 1) {
      const x = binsStartX + slot * (binWidth + binGap);
      const slotRect = new Graphics();
      slotRect
        .roundRect(x, binsY, binWidth, 44, 8)
        .fill({ color: slotColor(slot, slotCount), alpha: 0.97 })
        .stroke({ color: 0x351a06, width: 1.5, alpha: 0.85 });
      boardLayer.addChild(slotRect);

      const slotGlow = new Graphics();
      slotGlow.roundRect(x, binsY, binWidth, 44, 8).fill({ color: 0xfff4bf, alpha: 0 });
      boardLayer.addChild(slotGlow);
      slotGlows.push(slotGlow);

      binCenters.push(x + binWidth * 0.5);

      const slotText = new Text({
        text: `${formatMultiplier(multipliers[slot] ?? 0)}x`,
        style: {
          fill: slot >= Math.floor((slotCount - 1) / 2) - 1 && slot <= Math.ceil((slotCount - 1) / 2) + 1
            ? 0x2f2106
            : 0xffffff,
          fontFamily: "Teko, Rajdhani, sans-serif",
          fontSize: labelFontSize,
          fontWeight: "700"
        }
      });
      slotText.x = x + Math.max(4, binWidth * 0.12);
      slotText.y = binsY + (slotCount >= 13 ? 13 : 12);
      boardLayer.addChild(slotText);
    }

    return {
      centerX,
      startY,
      pegSpacingX,
      pegSpacingY,
      laneX,
      laneY,
      laneWidth,
      laneHeight,
      pocketTopY,
      binsY,
      binsBottomY,
      binsStartX,
      binWidth,
      binGap,
      binCenters,
      dividerXs,
      multipliers,
      slotGlows,
      rowPegY,
      rowPegXs,
      pegPoints,
      pegActors
    };
  }

function simulateBallFrames(
  roundId: string,
  slot: number,
  rows: number,
  geometry: BoardGeometry,
  speedMultiplier: number,
  tuning: PhysicsTuning
): SimulatedPath {
  const safeSlot = clamp(slot, 0, geometry.binCenters.length - 1);
  const targetX = geometry.binCenters[safeSlot] ?? geometry.centerX;
  const rng = createSeededRandom(`${roundId}:${safeSlot}:${rows}:stable-physics`);
  const dtBase = 1 / 144;
  const substeps = 3;
  const dt = dtBase / substeps;
  const gravity = 1240 * speedMultiplier;
  const wallRestitution = clamp(0.22 + tuning.bounce * 0.46, 0.2, 0.58);
  const pegRestitution = clamp(0.26 + tuning.bounce * 0.58, 0.24, 0.72);
  const airDrag = 0.0019;
  const baseFriction = clamp(0.03 + (0.36 - tuning.bounce) * 0.07, 0.018, 0.055);
  const targetNudge = tuning.pegNudge * 0.9;
  const ballRadius = BALL_RADIUS;
  const pegRadius = PEG_COLLISION_RADIUS;
  const laneLeft = geometry.laneX + 13 + ballRadius;
  const laneRight = geometry.laneX + geometry.laneWidth - 13 - ballRadius;
  const finishY = geometry.binsBottomY + 4;
  const targetSlotLeft = geometry.binsStartX + safeSlot * (geometry.binWidth + geometry.binGap);
  const targetSlotRight = targetSlotLeft + geometry.binWidth;
  const prePocketHeight = Math.max(geometry.pocketTopY - geometry.startY, 1);
  const minDownwardPocketVy = 40 * speedMultiplier;
  const maxUpwardSpeed = 24 * speedMultiplier;
  const rowSteeringBias = clamp(0.42 + tuning.bucketPull * 220, 0.42, 0.9);
  const rowDecisions = buildGuidedDecisions(rows, safeSlot, rng, rowSteeringBias);

  let x = geometry.centerX + (rng() - 0.5) * 5;
  let y = geometry.startY - 54;
  let vx = (rng() - 0.5) * 10;
  let vy = 0;
  let rotation = 0;
  let frames = 0;
  let progressBaselineY = y;
  let noProgressFrames = 0;
  let stalledFrames = 0;
  let lastStableX = x;
  let lastStableY = y;
  let rowImpulseIndex = 0;

  const points: Array<{ x: number; y: number; rotation: number }> = [{ x, y, rotation: 0 }];
  const pegHits: Array<{ frame: number; x: number; y: number; pegIndex: number }> = [];
  const hitCooldownByPeg = new Int32Array(geometry.pegPoints.length);
  hitCooldownByPeg.fill(-1000);

  while (frames < 1400 && y < finishY) {
    for (let step = 0; step < substeps; step += 1) {
      vy += gravity * dt;
      vx *= 1 - airDrag;
      vy *= 1 - airDrag * 0.55;

      x += vx * dt;
      y += vy * dt;

      while (rowImpulseIndex < rows && y >= (geometry.rowPegY[rowImpulseIndex] ?? geometry.pocketTopY) - geometry.pegSpacingY * 0.18) {
        const dir = rowDecisions[rowImpulseIndex] ?? (Math.sign(targetX - x) || (rng() < 0.5 ? -1 : 1));
        const depth = (rowImpulseIndex + 1) / Math.max(rows, 1);
        const impulse = tuning.rowImpulse * (0.78 + depth * 0.52) * speedMultiplier;
        vx += dir * impulse;
        vy = Math.max(vy, (58 + depth * 28) * speedMultiplier);
        rowImpulseIndex += 1;
      }

      if (x < laneLeft) {
        x = laneLeft;
        vx = Math.abs(vx) * wallRestitution;
      } else if (x > laneRight) {
        x = laneRight;
        vx = -Math.abs(vx) * wallRestitution;
      }

      const rowGuess = clamp(Math.floor((y - geometry.startY) / Math.max(geometry.pegSpacingY, 1)), 0, rows - 1);
      const rowDirection = rowDecisions[rowGuess] ?? 0;

      for (let pegIndex = 0; pegIndex < geometry.pegPoints.length; pegIndex += 1) {
        const peg = geometry.pegPoints[pegIndex]!;
        const dx = x - peg.x;
        const dy = y - peg.y;
        if (Math.abs(dy) > geometry.pegSpacingY * 0.88) {
          continue;
        }

        const minDistance = ballRadius + pegRadius;
        const distSq = dx * dx + dy * dy;
        if (distSq >= minDistance * minDistance || distSq <= 0.00001) {
          continue;
        }

        const distance = Math.sqrt(distSq);
        let nx = dx / distance;
        let ny = dy / distance;

        if (dy < 0 && Math.abs(dx) < pegRadius * 0.56) {
          const side = rowDirection || Math.sign(targetX - x) || (rng() < 0.5 ? -1 : 1);
          nx += side * 0.26;
          const inv = 1 / Math.hypot(nx, ny);
          nx *= inv;
          ny *= inv;
        }

        const overlap = minDistance - distance;
        x += nx * (overlap + 0.08);
        y += ny * (overlap + 0.08);

        const vn = vx * nx + vy * ny;
        if (vn < 0) {
          vx -= (1 + pegRestitution) * vn * nx;
          vy -= (1 + pegRestitution) * vn * ny;

          const tangentX = -ny;
          const tangentY = nx;
          const vt = vx * tangentX + vy * tangentY;
          vx -= vt * tangentX * baseFriction;
          vy -= vt * tangentY * baseFriction;

          const direction = rowDirection || Math.sign(targetX - x);
          if (direction !== 0 && y < geometry.pocketTopY - 6) {
            const progressNorm = clamp((y - geometry.startY) / prePocketHeight, 0, 1);
            vx += direction * targetNudge * (0.3 + progressNorm * 0.9) * speedMultiplier;
          }
        }

        vx = clamp(vx, -360 * speedMultiplier, 360 * speedMultiplier);
        vy = clamp(vy, -maxUpwardSpeed, 620 * speedMultiplier);
        if (dy < 0 && Math.abs(dx) < pegRadius * 0.62 && Math.abs(vy) < 12 * speedMultiplier) {
          vy = Math.max(vy, 30 * speedMultiplier);
          vx += (rng() - 0.5) * 8 * speedMultiplier;
        }

        if (frames - hitCooldownByPeg[pegIndex]! >= 6) {
          pegHits.push({ frame: frames, x: peg.x, y: peg.y, pegIndex });
          hitCooldownByPeg[pegIndex] = frames;
        }
      }

      if (y >= geometry.pocketTopY) {
        const slotMinX = targetSlotLeft + ballRadius * 0.46;
        const slotMaxX = targetSlotRight - ballRadius * 0.46;
        const slotCenterX = (slotMinX + slotMaxX) * 0.5;
        const approach = clamp((y - geometry.pocketTopY) / Math.max(geometry.binsY - geometry.pocketTopY, 1), 0, 1);
        vx += (slotCenterX - x) * (0.09 + approach * 0.14);

        if (x < slotMinX) {
          x = slotMinX;
          vx = Math.abs(vx) * 0.24 + 8 * speedMultiplier;
        } else if (x > slotMaxX) {
          x = slotMaxX;
          vx = -Math.abs(vx) * 0.24 - 8 * speedMultiplier;
        }

        vy = Math.max(vy, minDownwardPocketVy);
      }

      if (y > progressBaselineY + 1.2) {
        progressBaselineY = y;
        noProgressFrames = 0;
      } else if (y < geometry.pocketTopY - 8) {
        noProgressFrames += 1;
        if (noProgressFrames >= 42) {
          const pushDir = Math.sign(targetX - x) || (rng() < 0.5 ? -1 : 1);
          vx += pushDir * (34 + targetNudge * 1.2) * speedMultiplier;
          vy = Math.max(vy, 64 * speedMultiplier);
          y += 1.4;
          noProgressFrames = 0;
          progressBaselineY = y;
        }
      } else {
        noProgressFrames = 0;
        progressBaselineY = y;
      }

      const moved = Math.hypot(x - lastStableX, y - lastStableY);
      if (moved < 0.16 && y < geometry.pocketTopY - 8) {
        stalledFrames += 1;
        if (stalledFrames >= 14) {
          const pushDir = rowDecisions[clamp(rowGuess, 0, rowDecisions.length - 1)] ?? (Math.sign(targetX - x) || (rng() < 0.5 ? -1 : 1));
          vx += pushDir * (18 + tuning.rowImpulse * 0.7) * speedMultiplier;
          vy = Math.max(vy, 84 * speedMultiplier);
          y += 1.6;
          stalledFrames = 0;
        }
      } else {
        stalledFrames = 0;
        lastStableX = x;
        lastStableY = y;
      }
    }

    frames += 1;
    rotation += clamp(vx * 0.0009, -0.065, 0.065);
    if (frames % 1 === 0) {
      points.push({ x, y, rotation });
    }

    const insideTargetSlot = x > targetSlotLeft + ballRadius * 0.42 && x < targetSlotRight - ballRadius * 0.42;
    if (
      y >= geometry.binsBottomY - 2 &&
      insideTargetSlot &&
      Math.abs(vx) < 20 * speedMultiplier &&
      vy > 0
    ) {
      break;
    }
  }

  const settleY = geometry.binsY + 9;
  const settleSteps = 6;
  const fromX = x;
  const fromY = Math.min(y, settleY + 4);
  const fromRotation = points[points.length - 1]?.rotation ?? 0;
  const settleX = clamp(targetX, targetSlotLeft + ballRadius * 0.6, targetSlotRight - ballRadius * 0.6);
  for (let i = 1; i <= settleSteps; i += 1) {
    const t = i / settleSteps;
    const ease = t * t * (3 - 2 * t);
    points.push({
      x: fromX + (settleX - fromX) * ease,
      y: fromY + (settleY - fromY) * ease,
      rotation: fromRotation + t * 0.2
    });
  }

  return { points, pegHits };
}

  const pegKickLocks = new Set<number>();

  function spawnPegImpact(x: number, y: number): void {
    const container = new Container();
    fxLayer.addChild(container);
    container.x = x;
    container.y = y;

    const ring = new Graphics();
    ring.circle(0, 0, 2).stroke({ color: 0xf7f3d1, width: 1.8, alpha: 0.9 });
    container.addChild(ring);

    const sparks: Array<{ shape: Graphics; angle: number; speed: number }> = [];
    for (let i = 0; i < 6; i += 1) {
      const spark = new Graphics();
      spark.circle(0, 0, 1.2).fill({ color: i % 2 === 0 ? 0xffdc86 : 0xe7f4ff, alpha: 0.95 });
      container.addChild(spark);
      sparks.push({ shape: spark, angle: (Math.PI * 2 * i) / 6, speed: 10 + i * 1.6 });
    }

    const ttlMs = 170;
    let elapsedMs = 0;
    const tick = () => {
      elapsedMs += app.ticker.deltaMS;
      const t = Math.min(elapsedMs / ttlMs, 1);
      ring.scale.set(1 + t * 2.3);
      ring.alpha = 1 - t;
      for (const spark of sparks) {
        const distance = spark.speed * t;
        spark.shape.x = Math.cos(spark.angle) * distance;
        spark.shape.y = Math.sin(spark.angle) * distance;
        spark.shape.alpha = 1 - t;
      }
      if (t >= 1) {
        app.ticker.remove(tick);
        container.destroy({ children: true });
      }
    };
    app.ticker.add(tick);
  }

  function animatePegKick(geometry: BoardGeometry, pegIndex: number, hitX: number): void {
    const actor = geometry.pegActors[pegIndex];
    if (!actor || pegKickLocks.has(pegIndex)) {
      return;
    }

    pegKickLocks.add(pegIndex);
    const { player, shadow } = actor;
    const fromY = player.y;
    const fromX = player.x;
    const fromRotation = player.rotation;
    const fromShadowAlpha = shadow.alpha;
    const fromShadowScaleX = shadow.scale.x;
    const fromShadowScaleY = shadow.scale.y;
    const fromShadowY = shadow.y;
    const direction = Math.sign(hitX - fromX) || 1;
    const ttlMs = 210;
    let elapsedMs = 0;

    const tick = () => {
      elapsedMs += app.ticker.deltaMS;
      const t = Math.min(elapsedMs / ttlMs, 1);
      const up = Math.sin(t * Math.PI);
      player.y = fromY - up * 4.6;
      player.x = fromX + direction * up * 1.2;
      player.rotation = fromRotation + direction * up * 0.085;

      shadow.alpha = fromShadowAlpha + up * 0.08;
      shadow.scale.x = fromShadowScaleX + up * 0.12;
      shadow.scale.y = fromShadowScaleY - up * 0.14;
      shadow.y = fromShadowY + up * 0.5;

      if (t >= 1) {
        player.y = fromY;
        player.x = fromX;
        player.rotation = fromRotation;
        shadow.alpha = fromShadowAlpha;
        shadow.scale.x = fromShadowScaleX;
        shadow.scale.y = fromShadowScaleY;
        shadow.y = fromShadowY;
        pegKickLocks.delete(pegIndex);
        app.ticker.remove(tick);
      }
    };

    app.ticker.add(tick);
  }

  function flashBucket(geometry: BoardGeometry, slot: number): void {
    const glow = geometry.slotGlows[slot];
    if (!glow) {
      return;
    }

    const ttlMs = 260;
    let elapsedMs = 0;
    const tick = () => {
      elapsedMs += app.ticker.deltaMS;
      const t = Math.min(elapsedMs / ttlMs, 1);
      const pulse = Math.sin(t * Math.PI);
      glow.alpha = pulse * 0.62;
      if (t >= 1) {
        glow.alpha = 0;
        app.ticker.remove(tick);
      }
    };
    app.ticker.add(tick);
  }

  function spawnImpactExplosion(x: number, y: number): void {
    const container = new Container();
    fxLayer.addChild(container);

    const ring = new Graphics();
    ring.circle(0, 0, 3).stroke({ color: 0xffcd61, width: 2, alpha: 0.95 });
    container.addChild(ring);

    const particles: Array<{ shape: Graphics; angle: number; speed: number }> = [];
    for (let i = 0; i < 8; i += 1) {
      const angle = (Math.PI * 2 * i) / 8;
      const particle = new Graphics();
      particle.circle(0, 0, 1.8).fill({ color: i % 2 === 0 ? 0xffdd8c : 0xff7e52, alpha: 1 });
      container.addChild(particle);
      particles.push({ shape: particle, angle, speed: 24 + i * 1.8 });
    }

    container.x = x;
    container.y = y;

    const ttlMs = 260;
    let elapsedMs = 0;
    const tick = () => {
      elapsedMs += app.ticker.deltaMS;
      const t = Math.min(elapsedMs / ttlMs, 1);

      ring.scale.set(1 + t * 6.2);
      ring.alpha = 1 - t;

      for (const particle of particles) {
        const travel = particle.speed * t;
        particle.shape.x = Math.cos(particle.angle) * travel;
        particle.shape.y = Math.sin(particle.angle) * travel;
        particle.shape.alpha = 1 - t;
      }

      if (t >= 1) {
        app.ticker.remove(tick);
        container.destroy({ children: true });
      }
    };

    app.ticker.add(tick);
  }

  async function animateBall(
    roundId: string,
    slot: number,
    rows: number,
    geometry: BoardGeometry,
    speedMultiplier: number,
    tuning: PhysicsTuning
  ): Promise<void> {
    fxLayer.removeChildren();
    const ballRadius = BALL_RADIUS;

    const trailGlow = new Graphics();
    trailGlow.circle(0, 0, ballRadius * 1.75).fill({ color: 0xffffff, alpha: 0.2 });
    trailGlow.filters = [new BlurFilter({ strength: 8 })];

    const ballShadow = new Graphics();
    ballShadow
      .ellipse(ballRadius * 0.28, ballRadius * 0.92, ballRadius * 0.92, ballRadius * 0.34)
      .fill({ color: 0x15181d, alpha: 0.32 });

    const ballOuter = new Graphics();
    ballOuter.circle(0, 0, ballRadius).fill({ color: 0xf5f7fa });
    ballOuter.stroke({ color: 0x1a1e24, width: ballRadius * 0.15, alpha: 0.9 });

    const patchA = new Graphics();
    patchA.circle(0, -ballRadius * 0.16, ballRadius * 0.25).fill({ color: 0x1a1f27, alpha: 0.96 });
    const patchB = new Graphics();
    patchB.circle(-ballRadius * 0.28, ballRadius * 0.14, ballRadius * 0.18).fill({ color: 0x1a1f27, alpha: 0.92 });
    const patchC = new Graphics();
    patchC.circle(ballRadius * 0.26, ballRadius * 0.2, ballRadius * 0.17).fill({ color: 0x1a1f27, alpha: 0.92 });
    const ballHighlight = new Graphics();
    ballHighlight
      .circle(-ballRadius * 0.3, -ballRadius * 0.32, ballRadius * 0.24)
      .fill({ color: 0xffffff, alpha: 0.88 });

    const ballSprite = new Container();
    ballSprite.addChild(trailGlow, ballShadow, ballOuter, patchA, patchB, patchC, ballHighlight);
    fxLayer.addChild(ballSprite);

  const simulation = simulateBallFrames(roundId, slot, rows, geometry, speedMultiplier, tuning);
    const rawFrames = simulation.points;
    const stride = Math.max(1, Math.ceil(rawFrames.length / 520));
    const frames = rawFrames.filter((_, index) => index % stride === 0);
    if (frames[frames.length - 1] !== rawFrames[rawFrames.length - 1]) {
      frames.push(rawFrames[rawFrames.length - 1]!);
    }
    if (frames.length < 2) {
      return;
    }

    await new Promise<void>((resolve) => {
      let frameIndexFloat = 0;
      const playback = clamp(0.42 + speedMultiplier * 0.18, 0.46, 0.82);
      let hitIndex = 0;
      let elapsedMs = 0;
      const maxAnimationMs = 7000;

      const tick = () => {
        elapsedMs += app.ticker.deltaMS;
        frameIndexFloat += (app.ticker.deltaMS / (1000 / 120)) * playback;
        const baseIndex = Math.floor(frameIndexFloat);
        const localT = frameIndexFloat - baseIndex;
        const clampedIndex = Math.min(baseIndex, frames.length - 2);

        const current = frames[clampedIndex];
        const next = frames[clampedIndex + 1];

        ballSprite.x = current.x + (next.x - current.x) * localT;
        ballSprite.y = current.y + (next.y - current.y) * localT;
        ballSprite.rotation = current.rotation + (next.rotation - current.rotation) * localT;

        while (hitIndex < simulation.pegHits.length && simulation.pegHits[hitIndex]!.frame <= clampedIndex) {
          const hit = simulation.pegHits[hitIndex]!;
          spawnPegImpact(hit.x, hit.y);
          animatePegKick(geometry, hit.pegIndex, ballSprite.x);
          hitIndex += 1;
        }

        if (baseIndex >= frames.length - 1 || elapsedMs >= maxAnimationMs) {
          app.ticker.remove(tick);
          resolve();
        }
      };

      app.ticker.add(tick);
    });

    const last = frames[frames.length - 1];
    ballSprite.destroy({ children: true });
    flashBucket(geometry, clamp(slot, 0, geometry.binCenters.length - 1));
    spawnImpactExplosion(last.x, last.y);
  }

  async function ensureOpenGame(forceReopen = false): Promise<void> {
    const desiredSessionId = sessionInput.value.trim();
    if (!forceReopen && api.sessionId && api.sessionId === desiredSessionId) {
      return;
    }

    const options = openGameOptions();
    if (!options.sessionId) {
      throw new Error("Session Id is required");
    }

    const response = await api.openGame(options);
    if (sessionInput.value.trim() !== response.sessionId) {
      sessionInput.value = response.sessionId;
    }
    balanceValue.textContent = toCurrency(response.balance);
    syncCounters();

    if (currentSessionId !== response.sessionId) {
      currentSessionId = response.sessionId;
      resetHistory();
    }

    setStatus(`Game session opened for ${response.sessionId}.`);
  }

  async function runSingleRound(
    animate: boolean,
    source: "manual" | "auto",
    autoFastVisual = false
  ): Promise<void> {
    const betAmount = Number(betInput.value);
    const rows = selectedRows();
    const risk = selectedRisk();
    const multipliers = selectedMultipliers();

    if (!Number.isFinite(betAmount) || betAmount <= 0) {
      throw new Error("Bet amount must be greater than zero");
    }

    await ensureOpenGame();

    const place = await api.placeBet(betAmount, `${risk.toUpperCase()}:${rows}`);
    balanceValue.textContent = toCurrency(place.balance);
    roundIdValue.textContent = place.roundId;
    syncCounters();

    const collected = await api.collect(place.roundId);
    balanceValue.textContent = toCurrency(collected.balance);
    syncCounters();

    const serverMultipliers = place.math.details.config?.multipliers;
    const activeMultipliers =
      Array.isArray(serverMultipliers) && serverMultipliers.length > 0 ? serverMultipliers : multipliers;
    const rawSlot = place.math.details.ballInfo[0]?.slot ?? Math.floor(activeMultipliers.length * 0.5);
    const slotFromBallInfo = clamp(rawSlot, 0, activeMultipliers.length - 1);
    const paidWinAmount = Number.isFinite(collected.winAmount) ? collected.winAmount : place.math.totalWinAmount;
    const safeSlot = findPaidSlot(betAmount, paidWinAmount, activeMultipliers, slotFromBallInfo);
    const landedMultiplier = activeMultipliers[safeSlot] ?? 0;
    const tuning = readPhysicsTuning();
    if (safeSlot !== slotFromBallInfo) {
      setStatus(
        `Outcome alignment: slot ${slotFromBallInfo} adjusted to ${safeSlot} to match ${toCurrency(paidWinAmount)}.`
      );
    }

    setStatus(`Round ${place.roundId.slice(0, 8)} settled at slot ${safeSlot} (${formatMultiplier(landedMultiplier)}x).`);
    if (animate) {
      const speed = autoFastVisual ? Math.max(1.35, ballSpeedMultiplier() * 1.25) : ballSpeedMultiplier();
      await animateBall(place.roundId, safeSlot, rows, currentBoard, speed, tuning);
    }

    lastWinValue.textContent = toCurrency(collected.winAmount);
    updateTotalWin(totalWinAmount + collected.winAmount);

    pushHistory({
      created: new Date().toISOString(),
      roundId: collected.roundId,
      betAmount,
      winAmount: collected.winAmount,
      betType: `${risk.toUpperCase()}:${rows}`,
      collected: true
    });

    const sourceText = source === "auto" ? "Auto" : "Manual";
    const soundText = soundEnabledInput.checked ? " sound:on" : " sound:off";
    setStatus(`${sourceText} round settled. Win ${toCurrency(collected.winAmount)}.${soundText}`);
  }

  async function runAutoBet(): Promise<void> {
    const totalRounds = Math.max(1, Number(autoRoundsInput.value) || 1);
    const intervalMs = Math.max(0, Number(autoIntervalInput.value) || 0);
    const autoFastVisual = autoSkipAnimationInput.checked;

    autoRunning = true;
    updateControls();
    autoProgress.textContent = `0 / ${totalRounds}`;
    setStatus(`Auto bet started (${totalRounds} rounds).`);

    let completed = 0;

    while (autoRunning && completed < totalRounds) {
      busy = true;
      updateControls();
      try {
        await runSingleRound(true, "auto", autoFastVisual);
      } catch (error) {
        setStatus(`Auto bet failed: ${String(error)}`);
        autoRunning = false;
      } finally {
        busy = false;
        updateControls();
      }

      if (!autoRunning) {
        break;
      }

      completed += 1;
      autoProgress.textContent = `${completed} / ${totalRounds}`;

      if (completed < totalRounds && intervalMs > 0) {
        await sleep(intervalMs);
      }
    }

    if (!autoRunning && completed < totalRounds) {
      setStatus(`Auto bet stopped at ${completed} / ${totalRounds}.`);
    } else if (completed >= totalRounds) {
      setStatus(`Auto bet finished (${completed} / ${totalRounds}).`);
    }

    autoRunning = false;
    updateControls();
    autoProgress.textContent = "Idle";
  }

  const app = new Application();
  setPreloaderMessage("Preparing game canvas...", 28);
  await app.init({
    antialias: true,
    backgroundAlpha: 0,
    resizeTo: stageWrap
  });
  stageWrap.appendChild(app.canvas);

  const bgLayer = new Container();
  const boardLayer = new Container();
  const fxLayer = new Container();
  app.stage.addChild(bgLayer, boardLayer, fxLayer);

  const boardColor = new ColorMatrixFilter();
  boardColor.brightness(1.08, false);
  boardColor.contrast(1.03, false);
  boardLayer.filters = [boardColor];

  function refreshBoardFromControls(): void {
    const rows = selectedRows();
    const multipliers = selectedMultipliers();
    currentBoard = drawBoard(rows, multipliers);
    updatePaytablePreview();
  }

  setPreloaderMessage("Drawing playfield...", 72);
  let currentBoard = drawBoard(selectedRows(), selectedMultipliers());
  drawBackground();
  updatePaytablePreview();
  syncTuningLabels();
  updateTotalWin(0);
  renderHistory();
  updateControls();
  setPreloaderMessage("Loading shaders and effects...", 88);
  await hidePreloader();

  window.addEventListener("resize", () => {
    drawBackground();
    refreshBoardFromControls();
  });

  rowsInput.addEventListener("change", () => {
    const rows = selectedRows();
    rowsInput.value = String(rows);
    refreshBoardFromControls();
    setStatus(`Lines set to ${rows}. Payout and probabilities updated.`);
  });

  riskInput.addEventListener("change", () => {
    const risk = selectedRisk();
    riskInput.value = risk;
    refreshBoardFromControls();
    setStatus(`Risk set to ${risk.toUpperCase()}. Payout and probabilities updated.`);
  });

  animationSpeedInput.addEventListener("change", () => {
    setStatus(`Ball speed set to ${animationSpeedInput.options[animationSpeedInput.selectedIndex]?.text ?? "Normal"}.`);
  });

  const onTuningChange = () => {
    syncTuningLabels();
  };
  tuneBounceInput.addEventListener("input", onTuningChange);
  tuneRowImpulseInput.addEventListener("input", onTuningChange);
  tuneBucketPullInput.addEventListener("input", onTuningChange);
  tunePegNudgeInput.addEventListener("input", onTuningChange);

  soundEnabledInput.addEventListener("change", () => {
    setStatus(`Sound effects ${soundEnabledInput.checked ? "enabled" : "disabled"}.`);
  });

  spinButton.addEventListener("click", async () => {
    if (busy || autoRunning) {
      return;
    }

    busy = true;
    updateControls();
    try {
      await runSingleRound(true, "manual");
    } catch (error) {
      setStatus(`Spin failed: ${formatClientError(error)}`);
    } finally {
      busy = false;
      updateControls();
    }
  });

    autoStartButton.addEventListener("click", async () => {
      if (busy || autoRunning) {
        return;
      }
      try {
        await runAutoBet();
      } catch (error) {
        autoRunning = false;
        busy = false;
        updateControls();
        autoProgress.textContent = "Idle";
        setStatus(`Auto bet failed: ${formatClientError(error)}`);
      }
  });

  autoStopButton.addEventListener("click", () => {
    autoRunning = false;
    autoProgress.textContent = "Stopping...";
  });

  historyRefreshButton.addEventListener("click", async () => {
    if (busy || autoRunning) {
      return;
    }

    busy = true;
    updateControls();
    try {
      await ensureOpenGame();
      const history = await api.readHistory(0);
      syncCounters();
      syncHistoryFromServer(history.history);
      setStatus(`History synced (${history.history.length} rounds).`);
    } catch (error) {
      setStatus(`History sync failed: ${formatClientError(error)}`);
    } finally {
      busy = false;
      updateControls();
    }
  });

  setStatus(`Client ready. API: ${launchNgsApiUrl}`);
  setStatus("Physics profile: RealBounce-v3 (strong peg rebound + slot-lock).");
}

void bootstrap().catch((error) => {
  const preloaderText = document.querySelector<HTMLElement>("#preloader-text");
  if (preloaderText) {
    preloaderText.textContent = `Startup failed: ${String(error)}`;
  }
  console.error(error);
});
