const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18074);
const SERVICE_NAME = process.env.SERVICE_NAME || 'gameplay-orchestrator';
const ROUTE_ENABLED = String(process.env.GAMEPLAY_ORCHESTRATOR_ROUTE_ENABLED || 'false').toLowerCase() === 'true';
const CANARY_BANKS = String(process.env.GAMEPLAY_ORCHESTRATOR_CANARY_BANKS || '')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: SERVICE_NAME, now: new Date().toISOString() });
});

app.get('/api/v1/gameplay/routing/decision', (req, res) => {
  const bankId = String(req.query.bankId || '').trim();
  const isMultiplayer = String(req.query.isMultiplayer || 'false').toLowerCase() === 'true';
  const bankCanary = bankId.length > 0 && CANARY_BANKS.includes(bankId);
  const routeToGameplayService = ROUTE_ENABLED && bankCanary && !isMultiplayer;

  res.json({
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankId,
    isMultiplayer,
    routeToGameplayService,
    reason: isMultiplayer ? 'multiplayer_routed_elsewhere' : 'eligible'
  });
});

app.post('/api/v1/gameplay/launch-intents', (req, res) => {
  const result = store.createLaunchIntent({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    gameId: req.body.gameId,
    operationId: req.body.operationId,
    metadata: req.body.metadata || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ intent: result.intent, idempotent: result.idempotent });
});

app.post('/api/v1/gameplay/wager-intents', (req, res) => {
  const result = store.createWagerIntent({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    gameId: req.body.gameId,
    operationId: req.body.operationId,
    roundId: req.body.roundId,
    currency: req.body.currency,
    amount: req.body.amount,
    metadata: req.body.metadata || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ intent: result.intent, idempotent: result.idempotent });
});

app.post('/api/v1/gameplay/settle-intents', (req, res) => {
  const result = store.createSettleIntent({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    gameId: req.body.gameId,
    operationId: req.body.operationId,
    roundId: req.body.roundId,
    currency: req.body.currency,
    amount: req.body.amount,
    metadata: req.body.metadata || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ intent: result.intent, idempotent: result.idempotent });
});

app.get('/api/v1/gameplay/intents', (req, res) => {
  const result = store.listIntents(req.query.bankId, req.query.type, req.query.status);
  res.json(result);
});

app.get('/api/v1/outbox', (req, res) => {
  const result = store.listOutbox(req.query.status);
  res.json(result);
});

app.post('/api/v1/outbox/:eventId/ack', (req, res) => {
  const result = store.ackOutbox(req.params.eventId);
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ outboxEvent: result.outboxEvent });
});

app.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`${SERVICE_NAME} listening on port ${PORT}`);
});
