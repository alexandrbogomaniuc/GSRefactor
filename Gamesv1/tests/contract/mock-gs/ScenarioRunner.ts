import { GsMockServer } from './GsMockServer';
import WebSocket from 'ws';
import * as fs from 'fs';
import * as path from 'path';

class ScenarioRunner {
    private server: GsMockServer;
    private ws: WebSocket | null = null;
    private capturedFrames: any[] = [];

    constructor() {
        this.server = new GsMockServer(6001);
    }

    public async runScenario(filePath: string) {
        const scenario = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
        console.log(`\n🎬 Running Scenario: ${scenario.name}`);

        for (const step of scenario.steps) {
            await this.executeStep(step);
        }

        console.log(`✅ Scenario ${scenario.name} completed.`);
    }

    private async executeStep(step: any) {
        console.log(`  ➡️ Action: ${step.action}`);

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
                this.ws?.close();
                break;
            case 'sync':
                await this.sendSync(step.params);
                break;
        }
    }

    private connect(url: string): Promise<void> {
        return new Promise((resolve) => {
            this.ws = new WebSocket(url);
            this.ws.on('open', () => {
                this.sendRaw({ type: 'GAME_READY', payload: { token: 'test' } });
            });
            this.ws.on('message', (data) => {
                const msg = JSON.parse(data.toString());
                this.capturedFrames.push(msg);
                if (msg.type === 'SESSION_ACCEPTED') resolve();
            });
        });
    }

    private sendSpin(params: any): Promise<void> {
        return new Promise((resolve) => {
            this.sendRaw({
                type: 'BET_REQUEST',
                operationId: params.operationId,
                payload: { betAmount: params.betAmount }
            });

            const listener = (data: any) => {
                const msg = JSON.parse(data.toString());
                if (msg.type === 'BET_ACCEPTED' || msg.type === 'BET_REJECTED') {
                    this.ws?.off('message', listener);
                    resolve();
                }
            };
            this.ws?.on('message', listener);
        });
    }

    private sendSettle(params: any): Promise<void> {
        return new Promise((resolve) => {
            this.sendRaw({
                type: 'SETTLE_REQUEST',
                operationId: params.operationId,
                payload: {}
            });

            const listener = (data: any) => {
                const msg = JSON.parse(data.toString());
                if (msg.type === 'SETTLE_ACCEPTED') {
                    this.ws?.off('message', listener);
                    resolve();
                }
            };
            this.ws?.on('message', listener);
        });
    }

    private sendSync(params: any): Promise<void> {
        return new Promise((resolve) => {
            this.sendRaw({
                type: 'SYNC_REQUEST',
                payload: {}
            });

            const listener = (data: any) => {
                const msg = JSON.parse(data.toString());
                if (msg.type === 'SESSION_SYNC') {
                    this.ws?.off('message', listener);
                    resolve();
                }
            };
            this.ws?.on('message', listener);
        });
    }

    private sendRaw(envelopePatch: any) {
        const env = {
            version: "1.0",
            traceId: "trace-123",
            sessionId: "session-123",
            bankId: "bank-123",
            gameId: "game-123",
            timestamp: new Date().toISOString(),
            seq: 1,
            ...envelopePatch
        };
        this.ws?.send(JSON.stringify(env));
    }

    public stop() {
        this.server.close();
        this.ws?.close();
    }
}

async function main() {
    const runner = new ScenarioRunner();
    const scenarioDir = path.join(__dirname, 'scenarios');
    const files = fs.readdirSync(scenarioDir).filter(f => f.endsWith('.json'));

    try {
        for (const file of files) {
            await runner.runScenario(path.join(scenarioDir, file));
        }
        console.log("\n✨ All Scenarios passed!");
    } catch (e) {
        console.error("\n❌ Scenario Runner failed:", e);
        process.exit(1);
    } finally {
        runner.stop();
    }
}

main();
