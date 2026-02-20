const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18075);
const SERVICE_NAME = process.env.SERVICE_NAME || 'wallet-adapter';

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: SERVICE_NAME, now: new Date().toISOString() });
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
