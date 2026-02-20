const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18075);
const SERVICE_NAME = process.env.SERVICE_NAME || 'wallet-adapter';
const ROUTE_ENABLED = String(process.env.WALLET_ADAPTER_ROUTE_ENABLED || 'false').toLowerCase() === 'true';
const CANARY_BANKS = String(process.env.WALLET_ADAPTER_CANARY_BANKS || '')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({
    status: 'ok',
    service: SERVICE_NAME,
    now: new Date().toISOString(),
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS
  });
});

app.get('/api/v1/wallet/routing/decision', (req, res) => {
  const bankId = String(req.query.bankId || '').trim();
  const bankCanary = bankId.length > 0 && CANARY_BANKS.includes(bankId);
  const routeToWalletAdapter = ROUTE_ENABLED && bankCanary;
  const reason = !ROUTE_ENABLED ? 'route_disabled' : (bankCanary ? 'eligible' : 'bank_not_in_canary');

  res.json({
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankId,
    routeToWalletAdapter,
    reason
  });
});

app.post('/api/v1/wallet/reserve', (req, res) => {
  const result = store.reserve({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    roundId: req.body.roundId,
    accountId: req.body.accountId,
    operationId: req.body.operationId,
    currency: req.body.currency,
    amount: req.body.amount,
    metadata: req.body.metadata || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ operation: result.operation, idempotent: result.idempotent });
});

app.post('/api/v1/wallet/settle', (req, res) => {
  const result = store.settle({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    roundId: req.body.roundId,
    accountId: req.body.accountId,
    operationId: req.body.operationId,
    currency: req.body.currency,
    amount: req.body.amount,
    metadata: req.body.metadata || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ operation: result.operation, idempotent: result.idempotent });
});

app.post('/api/v1/wallet/refund', (req, res) => {
  const result = store.refund({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    roundId: req.body.roundId,
    accountId: req.body.accountId,
    operationId: req.body.operationId,
    currency: req.body.currency,
    amount: req.body.amount,
    metadata: req.body.metadata || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ operation: result.operation, idempotent: result.idempotent });
});

app.get('/api/v1/wallet/operations', (req, res) => {
  const result = store.listOperations(req.query.bankId, req.query.type, req.query.status);
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
