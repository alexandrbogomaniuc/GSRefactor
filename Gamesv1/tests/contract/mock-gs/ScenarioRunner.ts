import { GsMockServer } from './GsMockServer.ts';
import WebSocket, { type RawData } from 'ws';
import * as fs from 'fs';
import * as path from 'path';
import { z } from 'zod';

const EnvelopeSchema = z.object({
    version: z.string(),
    type: z.string(),
    operationId: z.string().optional(),
    payload: z.any()
});

class ScenarioRunner {
    private static readonly STEP_TIMEOUT_MS = 8000;

    private readonly server: GsMockServer;
    private ws: WebSocket | null = null;
    private capturedFrames: any[] = [];

    constructor() {
        this.server = new GsMockServer(6001);
    }

    public async runScenario(filePath: string) {
        const scenario = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
        console.log(`\nRunning Scenario: ${scenario.name}`);

        for (const step of scenario.steps) {
            await this.executeStep(step);
        }

        // Reset transport state between scenarios to avoid listener leakage.
        await this.disconnect();
        console.log(`OK Scenario ${scenario.name} completed.`);
    }

    private async executeStep(step: { action: string; params?: any; expect?: any }) {
        console.log(`  Action: ${step.action}`);

        switch (step.action) {
            case 'connect':
                await this.connect(step.params.baseUrl);
                break;
            case 'spin':
                await this.sendSpin(step.params);
                break;
            case 'settle':
                await this.sendSettle(step.params);
                break;
            case 'disconnect':
                await this.disconnect();
                break;
            case 'sync':
                await this.sendSync(step.params);
                break;
            default:
                throw new Error(`Unsupported scenario action: ${step.action}`);
        }
    }

    private async connect(url: string): Promise<void> {
        await this.disconnect();

        await this.withTimeout(
            new Promise<void>((resolve, reject) => {
                const ws = new WebSocket(url);

                const onOpen = () => {
                    this.ws = ws;
                    this.sendRaw({ type: 'GAME_READY', payload: { token: 'test' } });
                };

                const onMessage = (data: RawData) => {
                    try {
                        const msg = this.parseMessage(data);
                        this.capturedFrames.push(msg);
                        if (msg.type === 'SESSION_ACCEPTED') {
                            cleanup();
                            resolve();
                        }
                    } catch (error) {
                        cleanup();
                        reject(error);
                    }
                };

                const onError = (error: Error) => {
                    cleanup();
                    reject(error);
                };

                const cleanup = () => {
                    ws.off('open', onOpen);
                    ws.off('message', onMessage);
                    ws.off('error', onError);
                };

                ws.on('open', onOpen);
                ws.on('message', onMessage);
                ws.on('error', onError);
            }),
            `connect timeout for ${url}`,
        );
    }

    private async sendSpin(params: any): Promise<void> {
        await this.withTimeout(
            new Promise<void>((resolve, reject) => {
                this.sendRaw({
                    type: 'BET_REQUEST',
                    operationId: params.operationId,
                    payload: { betAmount: params.betAmount }
                });

                const listener = (data: RawData) => {
                    try {
                        const msg = this.parseMessage(data);
                        if (msg.type === 'BET_ACCEPTED' || msg.type === 'BET_REJECTED') {
                            this.ws?.off('message', listener);
                            resolve();
                        }
                    } catch (error) {
                        this.ws?.off('message', listener);
                        reject(error);
                    }
                };

                this.ws?.on('message', listener);
            }),
            `spin timeout for operationId=${params?.operationId ?? 'unknown'}`,
        );
    }

    private async sendSettle(params: any): Promise<void> {
        await this.withTimeout(
            new Promise<void>((resolve, reject) => {
                this.sendRaw({
                    type: 'SETTLE_REQUEST',
                    operationId: params.operationId,
                    payload: {}
                });

                const listener = (data: RawData) => {
                    try {
                        const msg = this.parseMessage(data);
                        if (msg.type === 'SETTLE_ACCEPTED') {
                            this.ws?.off('message', listener);
                            resolve();
                        }
                    } catch (error) {
                        this.ws?.off('message', listener);
                        reject(error);
                    }
                };

                this.ws?.on('message', listener);
            }),
            `settle timeout for operationId=${params?.operationId ?? 'unknown'}`,
        );
    }

    private async sendSync(params: any): Promise<void> {
        await this.withTimeout(
            new Promise<void>((resolve, reject) => {
                this.sendRaw({
                    type: 'SYNC_REQUEST',
                    payload: params ?? {}
                });

                const listener = (data: RawData) => {
                    try {
                        const msg = this.parseMessage(data);
                        if (msg.type === 'SESSION_SYNC') {
                            this.ws?.off('message', listener);
                            resolve();
                        }
                    } catch (error) {
                        this.ws?.off('message', listener);
                        reject(error);
                    }
                };

                this.ws?.on('message', listener);
            }),
            'sync timeout',
        );
    }

    private parseMessage(data: RawData): any {
        const raw = typeof data === 'string' ? data : data.toString();
        const msg = JSON.parse(raw);
        const result = EnvelopeSchema.safeParse(msg);

        if (!result.success) {
            throw new Error(`Protocol Validation Failed: ${JSON.stringify(result.error.format())}`);
        }

        return msg;
    }

    private sendRaw(envelopePatch: any) {
        const env = {
            version: '1.0',
            traceId: 'trace-123',
            sessionId: 'session-123',
            bankId: 'bank-123',
            gameId: 'game-123',
            timestamp: new Date().toISOString(),
            seq: 1,
            ...envelopePatch
        };

        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            throw new Error('WebSocket is not open');
        }

        this.ws.send(JSON.stringify(env));
    }

    private async disconnect(): Promise<void> {
        if (!this.ws) return;

        const ws = this.ws;
        this.ws = null;

        if (ws.readyState === WebSocket.CLOSED) {
            return;
        }

        await new Promise<void>((resolve) => {
            const done = () => resolve();
            ws.once('close', done);
            ws.close();

            // Guard if close event is not delivered.
            setTimeout(done, 500);
        });
    }

    private async withTimeout<T>(promise: Promise<T>, message: string): Promise<T> {
        return new Promise<T>((resolve, reject) => {
            const timer = setTimeout(() => reject(new Error(message)), ScenarioRunner.STEP_TIMEOUT_MS);

            promise
                .then((value) => {
                    clearTimeout(timer);
                    resolve(value);
                })
                .catch((error) => {
                    clearTimeout(timer);
                    reject(error);
                });
        });
    }

    public async stop() {
        await this.disconnect();
        this.server.close();
    }
}

async function main() {
    const runner = new ScenarioRunner();
    const scenarioDir = path.join(process.cwd(), 'tests', 'contract', 'mock-gs', 'scenarios');
    const files = fs.readdirSync(scenarioDir).filter((f) => f.endsWith('.json')).sort();

    try {
        for (const file of files) {
            await runner.runScenario(path.join(scenarioDir, file));
        }
        console.log('\nAll Scenarios passed.');
    } catch (e) {
        console.error('\nScenario Runner failed:', e);
        process.exit(1);
    } finally {
        await runner.stop();
    }
}

main();
