import {
  createTransport,
  type GameInitConfig,
  type IGameTransport,
  type OpenGameResponse,
  type PlayRoundResponse,
} from "@gamesv1/core-protocol";

import { SessionRuntimeStore } from "../stores/SessionRuntimeStore";

const DEFAULT_API_BASE = "http://127.0.0.1:6400";
const DEFAULT_GAME_ID = 10;

const asNumber = (value: string | null): number | undefined => {
  if (!value) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
};

const readLaunchParams = () => {
  const params = new URLSearchParams(window.location.search);
  const sessionId =
    params.get("sid") ??
    params.get("SID") ??
    params.get("sessionId") ??
    params.get("SESSIONID") ??
    "";
  const bankId = asNumber(params.get("bankId") ?? params.get("BANKID"));
  const gameId =
    asNumber(
      params.get("gameIdNumeric") ??
        params.get("GAMEIDNUMERIC") ??
        params.get("gameId") ??
        params.get("GAMEID"),
    ) ?? DEFAULT_GAME_ID;
  const apiBase =
    params.get("ngsApiUrl") ??
    params.get("NGSAPIURL") ??
    import.meta.env.VITE_NGS_API_BASE ??
    DEFAULT_API_BASE;
  const gsInternalBaseUrl =
    params.get("gsInternalBaseUrl") ?? params.get("GSINTERNALBASEURL") ?? undefined;
  const token = params.get("token") ?? params.get("TOKEN") ?? undefined;
  const playerId = params.get("playerId") ?? params.get("PLAYERID") ?? undefined;
  const language = params.get("lang") ?? undefined;

  return {
    sessionId,
    bankId,
    gameId,
    apiBase,
    gsInternalBaseUrl,
    token,
    playerId,
    language,
  };
};

export class GsRuntimeClient {
  private transport: IGameTransport | null = null;
  private config: GameInitConfig | null = null;

  public async bootstrap(): Promise<OpenGameResponse> {
    const launch = readLaunchParams();
    if (!launch.sessionId) {
      throw new Error("Missing sessionId launch parameter. Cannot open GS runtime session.");
    }

    const transport = createTransport({
      mode: "GS_HTTP_RUNTIME",
      baseUrl: launch.apiBase,
      token: launch.token,
      bankId: launch.bankId,
      playerId: launch.playerId,
      gameId: launch.gameId,
      sessionId: launch.sessionId,
      gsInternalBaseUrl: launch.gsInternalBaseUrl,
      language: launch.language,
    });

    this.transport = transport;
    this.config = {
      mode: "GS_HTTP_RUNTIME",
      baseUrl: launch.apiBase,
      token: launch.token,
      bankId: launch.bankId,
      playerId: launch.playerId,
      gameId: launch.gameId,
      sessionId: launch.sessionId,
      gsInternalBaseUrl: launch.gsInternalBaseUrl,
      language: launch.language,
    };

    const opened = await transport.bootstrap(this.config);
    this.applyOpenSnapshot(opened);
    return opened;
  }

  public async resumeGame(): Promise<OpenGameResponse> {
    const transport = this.assertTransport();
    const opened = await transport.resumeGame();
    this.applyOpenSnapshot(opened);
    return opened;
  }

  public async playRound(betAmount: number, betType: string): Promise<PlayRoundResponse> {
    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.get();
    const nextCounter = snapshot.requestCounter + 1;
    const operationKey = `round-${nextCounter}`;

    const result = await transport.playRound({
      betAmount,
      betType,
      metadata: {
        requestCounter: nextCounter,
        clientOperationId: operationKey,
        idempotencyKey: operationKey,
        currentStateVersion: snapshot.currentStateVersion,
      },
    });

    SessionRuntimeStore.patch({
      balance: result.balance,
      requestCounter: result.requestCounter,
      currentStateVersion: result.currentStateVersion,
      lastRoundId: result.roundId,
    });
    return result;
  }

  public async readHistory(pageNumber = 0): Promise<Array<Record<string, unknown>>> {
    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.get();

    const response = await transport.readHistory({
      pageNumber,
      metadata: {
        requestCounter: snapshot.requestCounter + 1,
        currentStateVersion: snapshot.currentStateVersion,
      },
    });

    SessionRuntimeStore.patch({
      requestCounter: response.requestCounter,
      currentStateVersion: response.currentStateVersion,
    });

    return response.history;
  }

  public async close(reason = "client-close"): Promise<void> {
    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.getSnapshot();
    await transport.closeGame({
      reason,
      metadata: {
        requestCounter: (snapshot?.requestCounter ?? 0) + 1,
        currentStateVersion: snapshot?.currentStateVersion,
      },
    });
    SessionRuntimeStore.clear();
  }

  public getTransport(): IGameTransport {
    return this.assertTransport();
  }

  private applyOpenSnapshot(opened: OpenGameResponse): void {
    SessionRuntimeStore.set({
      sessionId: opened.sessionId,
      balance: opened.balance,
      requestCounter: opened.requestCounter,
      currentStateVersion: opened.currentStateVersion,
      unfinishedRound: opened.unresolvedRoundState,
      lastRoundId: undefined,
    });
  }

  private assertTransport(): IGameTransport {
    if (!this.transport || !this.config) {
      throw new Error("GsRuntimeClient not bootstrapped.");
    }
    return this.transport;
  }
}

export const gsRuntimeClient = new GsRuntimeClient();
