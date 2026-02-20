import { performance } from "node:perf_hooks";

type Metric = {
  ms: number;
};

type PerfConfig = {
  baseUrl: string;
  sessions: number;
  roundsPerSession: number;
  concurrency: number;
  betAmount: number;
};

type SessionCursor = {
  sessionId: string;
  counter: number;
};

const config: PerfConfig = {
  baseUrl: process.env.NGS_BASE_URL?.replace(/\/$/, "") ?? "http://127.0.0.1:6400",
  sessions: Number(process.env.NGS_SESSIONS ?? "200"),
  roundsPerSession: Number(process.env.NGS_ROUNDS_PER_SESSION ?? "2"),
  concurrency: Number(process.env.NGS_CONCURRENCY ?? "40"),
  betAmount: Number(process.env.NGS_BET_AMOUNT ?? "100")
};

function percentile(values: number[], p: number): number {
  if (!values.length) return 0;
  const sorted = [...values].sort((a, b) => a - b);
  const idx = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
  return sorted[idx] ?? 0;
}

async function postJson<T>(url: string, payload: unknown): Promise<{ data: T; ms: number; status: number }> {
  const started = performance.now();
  const response = await fetch(url, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  });
  const ms = performance.now() - started;
  const data = (await response.json()) as T;
  return { data, ms, status: response.status };
}

async function openSessions(): Promise<SessionCursor[]> {
  const cursors: SessionCursor[] = [];
  for (let i = 0; i < config.sessions; i += 1) {
    const sessionId = `perf-${Date.now()}-${i}`;
    const result = await postJson<{ requestCounter: number }>(`${config.baseUrl}/v1/opengame`, {
      sessionId,
      gameId: 10,
      playerId: sessionId
    });
    if (result.status !== 200) {
      throw new Error(`opengame failed status=${result.status} session=${sessionId}`);
    }
    cursors.push({ sessionId, counter: result.data.requestCounter });
  }
  return cursors;
}

async function runSessionRound(cursor: SessionCursor, betMetrics: Metric[], collectMetrics: Metric[]): Promise<void> {
  cursor.counter += 1;
  const place = await postJson<{ roundId: string }>(`${config.baseUrl}/v1/placebet`, {
    sessionId: cursor.sessionId,
    requestCounter: cursor.counter,
    bets: [{ betType: "MEDIUM:16", betAmount: config.betAmount }]
  });

  if (place.status !== 200) {
    throw new Error(`placebet failed status=${place.status} session=${cursor.sessionId}`);
  }
  betMetrics.push({ ms: place.ms });

  cursor.counter += 1;
  const collect = await postJson<{ roundId: string }>(`${config.baseUrl}/v1/collect`, {
    sessionId: cursor.sessionId,
    requestCounter: cursor.counter,
    roundId: place.data.roundId
  });

  if (collect.status !== 200) {
    throw new Error(`collect failed status=${collect.status} session=${cursor.sessionId}`);
  }
  collectMetrics.push({ ms: collect.ms });
}

async function runPool<T>(items: T[], worker: (item: T) => Promise<void>, concurrency: number): Promise<void> {
  let index = 0;
  const runners = new Array(Math.max(1, concurrency)).fill(0).map(async () => {
    while (true) {
      const local = index;
      index += 1;
      if (local >= items.length) {
        return;
      }
      await worker(items[local] as T);
    }
  });

  await Promise.all(runners);
}

async function main(): Promise<void> {
  const health = await fetch(`${config.baseUrl}/healthz`);
  if (!health.ok) {
    throw new Error(`NGS is not healthy at ${config.baseUrl}`);
  }

  const cursors = await openSessions();

  const betMetrics: Metric[] = [];
  const collectMetrics: Metric[] = [];

  const started = performance.now();
  await runPool(
    cursors,
    async (cursor) => {
      for (let i = 0; i < config.roundsPerSession; i += 1) {
        await runSessionRound(cursor, betMetrics, collectMetrics);
      }
    },
    config.concurrency
  );
  const totalSec = (performance.now() - started) / 1000;

  const totalBets = betMetrics.length;
  const betsPerSec = totalBets / Math.max(totalSec, 0.001);

  const betLatencies = betMetrics.map((m) => m.ms);
  const collectLatencies = collectMetrics.map((m) => m.ms);

  console.log(JSON.stringify({
    target: {
      players: 200,
      betsPerSecond: 100
    },
    config,
    totals: {
      rounds: totalBets,
      durationSec: Number(totalSec.toFixed(2)),
      achievedBetsPerSec: Number(betsPerSec.toFixed(2))
    },
    placebet: {
      p50: Number(percentile(betLatencies, 50).toFixed(2)),
      p95: Number(percentile(betLatencies, 95).toFixed(2)),
      p99: Number(percentile(betLatencies, 99).toFixed(2))
    },
    collect: {
      p50: Number(percentile(collectLatencies, 50).toFixed(2)),
      p95: Number(percentile(collectLatencies, 95).toFixed(2)),
      p99: Number(percentile(collectLatencies, 99).toFixed(2))
    }
  }, null, 2));
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
