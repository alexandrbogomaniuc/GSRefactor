const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18076);
const SERVICE_NAME = process.env.SERVICE_NAME || 'bonus-frb-service';

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: SERVICE_NAME, now: new Date().toISOString() });
});

app.get('/api/v1/bonus/frb/check', (req, res) => {
  const result = store.checkFrb({
    bankId: req.query.bankId,
    accountId: req.query.accountId,
    frbId: req.query.frbId
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ frb: result.frb });
});

app.post('/api/v1/bonus/frb/consume', (req, res) => {
  const result = store.consumeFrb({
    bankId: req.body.bankId,
    accountId: req.body.accountId,
    frbId: req.body.frbId,
    operationId: req.body.operationId
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ frb: result.frb, idempotent: result.idempotent });
});

app.post('/api/v1/bonus/frb/release', (req, res) => {
  const result = store.releaseFrb({
    bankId: req.body.bankId,
    accountId: req.body.accountId,
    frbId: req.body.frbId,
    operationId: req.body.operationId
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ frb: result.frb, idempotent: result.idempotent });
});

app.get('/api/v1/bonus/frb', (req, res) => {
  const result = store.listFrb(req.query.bankId, req.query.accountId);
  res.json(result);
});

app.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`${SERVICE_NAME} listening on port ${PORT}`);
});
