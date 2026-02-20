const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18076);
const SERVICE_NAME = process.env.SERVICE_NAME || 'bonus-frb-service';
const ROUTE_ENABLED = String(process.env.BONUS_FRB_SERVICE_ROUTE_ENABLED || 'false').toLowerCase() === 'true';
const CANARY_BANKS = String(process.env.BONUS_FRB_SERVICE_CANARY_BANKS || '')
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

app.get('/api/v1/bonus/frb/routing/decision', (req, res) => {
  const bankId = String(req.query.bankId || '').trim();
  const bankCanary = bankId.length > 0 && CANARY_BANKS.includes(bankId);
  const routeToBonusFrbService = ROUTE_ENABLED && bankCanary;
  const reason = !ROUTE_ENABLED ? 'route_disabled' : (bankCanary ? 'eligible' : 'bank_not_in_canary');

  res.json({
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankId,
    routeToBonusFrbService,
    reason
  });
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
