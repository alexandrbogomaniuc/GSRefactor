const express = require('express');
const store = require('./store');
const policy = require('./policy');

const PORT = Number(process.env.PORT || 18079);
const SERVICE_NAME = process.env.SERVICE_NAME || 'multiplayer-service';
const ROUTE_ENABLED = String(process.env.MULTIPLAYER_SERVICE_ROUTE_ENABLED || 'false').toLowerCase() === 'true';
const CANARY_BANKS = String(process.env.MULTIPLAYER_SERVICE_CANARY_BANKS || '')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);
const BANK_FLAGS = policy.parseBankFlags(process.env.MULTIPLAYER_SERVICE_BANK_FLAGS || '');

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({
    status: 'ok',
    service: SERVICE_NAME,
    now: new Date().toISOString(),
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankFlags: BANK_FLAGS
  });
});

app.get('/api/v1/multiplayer/routing/decision', (req, res) => {
  const bankId = String(req.query.bankId || '').trim();
  const gameId = String(req.query.gameId || '').trim();
  const sessionId = String(req.query.sessionId || '').trim();
  const isMultiplayer = String(req.query.isMultiplayer || '').trim().toLowerCase() === 'true';
  const decision = policy.routeDecision({
    bankId,
    isMultiplayer,
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankFlags: BANK_FLAGS
  });

  res.json({
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankId,
    gameId,
    sessionId,
    ...decision
  });
});

app.post('/api/v1/multiplayer/lobby/join', (req, res) => {
  const result = store.upsertSession({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    playerId: req.body.playerId,
    lobbyId: req.body.lobbyId || null,
    roomId: req.body.roomId || null,
    operationId: req.body.operationId,
    operationType: 'lobby_join',
    status: 'LOBBY_JOINED'
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session, idempotent: result.idempotent });
});

app.post('/api/v1/multiplayer/room/sit-in', (req, res) => {
  const result = store.upsertSession({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    playerId: req.body.playerId,
    roomId: req.body.roomId,
    operationId: req.body.operationId,
    operationType: 'room_sit_in',
    status: 'SIT_IN'
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session, idempotent: result.idempotent });
});

app.post('/api/v1/multiplayer/room/sit-out', (req, res) => {
  const result = store.upsertSession({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    playerId: req.body.playerId,
    roomId: req.body.roomId || null,
    operationId: req.body.operationId,
    operationType: 'room_sit_out',
    status: 'SIT_OUT'
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session, idempotent: result.idempotent });
});

app.post('/api/v1/multiplayer/session/sync', (req, res) => {
  const result = store.upsertSession({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    playerId: req.body.playerId,
    lobbyId: req.body.lobbyId,
    roomId: req.body.roomId,
    operationId: req.body.operationId,
    operationType: 'session_sync',
    status: req.body.status || 'SYNCED'
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ session: result.session, idempotent: result.idempotent });
});

app.get('/api/v1/multiplayer/sessions', (req, res) => {
  const result = store.listSessions(req.query.bankId, req.query.sessionId);
  return res.json(result);
});

app.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`${SERVICE_NAME} listening on port ${PORT}`);
});
