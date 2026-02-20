import { spawn, type ChildProcess } from "node:child_process";
import path from "node:path";
import { createServer, type IncomingMessage, type ServerResponse } from "node:http";
import { setTimeout as delay } from "node:timers/promises";

const cwd = "/Users/alexb/Documents/Dev/new-games-server";

export type JsonResponse<T = unknown> = {
  status: number;
  data: T;
};

export type StartedNgsServer = {
  baseUrl: string;
  port: number;
  logs: () => string;
  stop: () => Promise<void>;
};

function randomPort(base: number): number {
  return base + Math.floor(Math.random() * 400);
}

async function waitForHealth(baseUrl: string, server: ChildProcess, logs: () => string, timeoutMs = 15000): Promise<void> {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    if (server.exitCode !== null && server.exitCode !== undefined) {
      throw new Error(`Server exited with code ${server.exitCode}. Logs:\n${logs()}`);
    }
    try {
      const response = await fetch(`${baseUrl}/healthz`);
      if (response.ok) {
        return;
      }
    } catch {
      // keep waiting
    }
    await delay(150);
  }
  throw new Error(`Server did not become healthy within ${timeoutMs}ms. Logs:\n${logs()}`);
}

export async function startNgsServer(envOverrides?: Record<string, string>): Promise<StartedNgsServer> {
  const port = randomPort(6510);
  const baseUrl = `http://127.0.0.1:${port}`;
  let serverLogs = "";

  const tsxPath = path.join(cwd, "node_modules", "tsx", "dist", "cli.mjs");
  const server = spawn(process.execPath, [tsxPath, "src/index.ts"], {
    cwd,
    env: {
      ...process.env,
      PORT: String(port),
      NODE_ENV: "test",
      ...(envOverrides ?? {})
    },
    stdio: ["ignore", "pipe", "pipe"]
  });

  server.stdout?.on("data", (...args: unknown[]) => {
    serverLogs += String(args[0] ?? "");
  });
  server.stderr?.on("data", (...args: unknown[]) => {
    serverLogs += String(args[0] ?? "");
  });

  await waitForHealth(baseUrl, server, () => serverLogs);

  return {
    baseUrl,
    port,
    logs: () => serverLogs,
    stop: async () => {
      if (server.killed || server.exitCode !== null) {
        return;
      }
      server.kill("SIGTERM");
      await delay(300);
      if (!server.killed && server.exitCode === null) {
        server.kill("SIGKILL");
      }
    }
  };
}

export async function postJson<T>(baseUrl: string, endpoint: string, payload: unknown): Promise<JsonResponse<T>> {
  const response = await fetch(`${baseUrl}${endpoint}`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  });

  return {
    status: response.status,
    data: (await response.json()) as T
  };
}

type DelayMap = {
  validateDelayMs?: number;
  validateExpectedSessionId?: string;
  reserveDelayMs?: number;
  settleDelayMs?: number;
  historyDelayMs?: number;
};

export type StartedGsStub = {
  baseUrl: string;
  stop: () => Promise<void>;
};

function resolveDelay(pathname: string, delayMap: DelayMap): number {
  if (pathname.endsWith("/session/validate")) return delayMap.validateDelayMs ?? 0;
  if (pathname.endsWith("/wallet/reserve")) return delayMap.reserveDelayMs ?? 0;
  if (pathname.endsWith("/wallet/settle")) return delayMap.settleDelayMs ?? 0;
  if (pathname.endsWith("/history/write")) return delayMap.historyDelayMs ?? 0;
  return 0;
}

function json(res: ServerResponse, statusCode: number, payload: Record<string, unknown>) {
  res.statusCode = statusCode;
  res.setHeader("content-type", "application/json");
  res.end(JSON.stringify(payload));
}

async function parseBody(req: IncomingMessage): Promise<Record<string, unknown>> {
  const chunks: Buffer[] = [];
  for await (const chunk of req) {
    chunks.push(Buffer.from(chunk));
  }
  if (!chunks.length) {
    return {};
  }

  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8")) as Record<string, unknown>;
  } catch {
    return {};
  }
}

export async function startGsStub(delayMap: DelayMap): Promise<StartedGsStub> {
  const port = randomPort(7010);

  const server = createServer(async (req, res) => {
    const pathname = req.url ?? "";
    const requestBody = await parseBody(req);
    const delayMs = resolveDelay(pathname, delayMap);
    if (delayMs > 0) {
      await delay(delayMs);
    }

    if (pathname.endsWith("/session/validate")) {
      const expectedSessionId = delayMap.validateExpectedSessionId;
      const receivedSessionId = String(requestBody.sessionId ?? "");
      if (expectedSessionId && receivedSessionId !== expectedSessionId) {
        json(res, 400, {
          error: {
            retryable: false,
            traceId: `${receivedSessionId}:sv:test`,
            code: "BAD_REQUEST",
            details: {},
            message: `Mismatch sessionId. (received:${receivedSessionId}; expected:${expectedSessionId})`
          }
        });
        return;
      }
      json(res, 200, {
        ok: true,
        playerId: requestBody.playerId ?? "stub-player",
        bankId: requestBody.bankId ?? 6274,
        balance: 100000,
        currency: {
          code: "USD",
          prefix: "$",
          suffix: "",
          grouping: ",",
          decimal: ".",
          precision: 1,
          denomination: 1
        }
      });
      return;
    }

    if (pathname.endsWith("/wallet/reserve")) {
      json(res, 200, {
        ok: true,
        walletOperationId: `stub-${String(requestBody.roundId ?? "round")}`,
        balance: 99900
      });
      return;
    }

    if (pathname.endsWith("/wallet/settle")) {
      json(res, 200, {
        ok: true,
        balance: 100050
      });
      return;
    }

    if (pathname.endsWith("/history/write")) {
      json(res, 200, { ok: true });
      return;
    }

    json(res, 404, { ok: false, error: "Not found" });
  });

  await new Promise<void>((resolve, reject) => {
    server.once("error", reject);
    server.listen(port, "127.0.0.1", () => resolve());
  });

  return {
    baseUrl: `http://127.0.0.1:${port}`,
    stop: async () => {
      await new Promise<void>((resolve, reject) => {
        server.close((error) => {
          if (error) {
            reject(error);
            return;
          }
          resolve();
        });
      });
    }
  };
}
