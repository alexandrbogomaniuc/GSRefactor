const express = require('express');
const store = require('./store');
const { startOutboxRelay } = require('./outboxRelay');

const PORT = Number(process.env.PORT || 18073);
const SERVICE_NAME = process.env.SERVICE_NAME || 'session-service';
const ROUTE_ENABLED = String(process.env.SESSION_SERVICE_ROUTE_ENABLED || 'false').toLowerCase() === 'true';
const CANARY_BANKS = String(process.env.SESSION_SERVICE_CANARY_BANKS || '')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);
const OUTBOX_REPLAY_MAX_COUNT = Number(process.env.SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT || 5);
const OUTBOX_REPLAY_WINDOW_SECONDS = Number(process.env.SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS || 0);

const app = express();
app.use(express.json({ limit: '2mb' }));
let stopOutboxRelay = async () => {};

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: SERVICE_NAME, now: new Date().toISOString() });
});

app.get('/api/v1/routing/decision', (req, res) => {
  const bankId = String(req.query.bankId || '').trim();
  const bankCanary = bankId.length > 0 && CANARY_BANKS.includes(bankId);
  const routeToSessionService = ROUTE_ENABLED && bankCanary;
  res.json({
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankId,
    routeToSessionService
  });
});

app.get('/api/v1/sessions', (req, res) => {
  const result = store.listSessions(req.query.bankId, req.query.status);
  res.json(result);
});

app.get('/api/v1/sessions/:bankId/:sessionId', (req, res) => {
  const result = store.getSession(req.params.bankId, req.params.sessionId);
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session });
});

app.post('/api/v1/sessions/create', (req, res) => {
  const result = store.createSession({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    userId: req.body.userId,
    gameId: req.body.gameId,
    operationId: req.body.operationId,
    metadata: req.body.metadata || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session, idempotent: result.idempotent });
});

app.post('/api/v1/sessions/touch', (req, res) => {
  const result = store.touchSession({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    operationId: req.body.operationId
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session, idempotent: result.idempotent });
});

app.post('/api/v1/sessions/close', (req, res) => {
  const result = store.closeSession({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    operationId: req.body.operationId,
    reason: req.body.reason
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session, idempotent: result.idempotent });
});

app.get('/api/v1/outbox', (req, res) => {
  const result = store.listOutbox(req.query.status);
  res.json(result);
});

app.get('/api/v1/outbox/replay-report', (req, res) => {
  const result = store.getReplayReport(req.query.limit);
  res.json(result);
});

app.post('/api/v1/outbox/:eventId/ack', (req, res) => {
  const result = store.ackOutbox(req.params.eventId);
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ outboxEvent: result.outboxEvent });
});

app.post('/api/v1/outbox/:eventId/requeue', (req, res) => {
  const replayReason = (req.query.reason || req.body.reason || '').toString();
  const result = store.requeueOutbox(
    req.params.eventId,
    replayReason,
    OUTBOX_REPLAY_MAX_COUNT,
    OUTBOX_REPLAY_WINDOW_SECONDS
  );
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ outboxEvent: result.outboxEvent });
});

const server = app.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`${SERVICE_NAME} listening on port ${PORT}`);
  startOutboxRelay(store)
    .then((stopFn) => {
      stopOutboxRelay = stopFn;
    })
    .catch((error) => {
      // eslint-disable-next-line no-console
      console.error(`failed to start outbox relay: ${error.message}`);
    });
});

async function shutdown(signal) {
  // eslint-disable-next-line no-console
  console.log(`${SERVICE_NAME} received ${signal}, shutting down`);
  await stopOutboxRelay();
  server.close(() => {
    process.exit(0);
  });
}

process.on('SIGTERM', () => {
  shutdown('SIGTERM');
});

process.on('SIGINT', () => {
  shutdown('SIGINT');
});
