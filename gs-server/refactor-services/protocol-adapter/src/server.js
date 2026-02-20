const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18078);
const SERVICE_NAME = process.env.SERVICE_NAME || 'protocol-adapter';
const ROUTE_ENABLED = String(process.env.PROTOCOL_ADAPTER_ROUTE_ENABLED || 'false').toLowerCase() === 'true';
const CANARY_BANKS = String(process.env.PROTOCOL_ADAPTER_CANARY_BANKS || '')
  .split(',')
  .map((value) => value.trim())
  .filter((value) => value.length > 0);

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: SERVICE_NAME, now: new Date().toISOString() });
});

app.get('/api/v1/protocol/routing/decision', (req, res) => {
  const bankId = String(req.query.bankId || '').trim();
  const isCanaryBank = bankId.length > 0 && CANARY_BANKS.includes(bankId);
  const routeToProtocolAdapter = ROUTE_ENABLED && isCanaryBank;

  const settingsResult = bankId ? store.resolveBankSettings(bankId) : null;
  const settings = settingsResult && settingsResult.ok ? settingsResult.settings : null;

  res.json({
    routeEnabled: ROUTE_ENABLED,
    canaryBanks: CANARY_BANKS,
    bankId,
    routeToProtocolAdapter,
    failOpenLegacyPath: true,
    protocolMode: settings ? settings.protocolMode : null,
    reason: routeToProtocolAdapter ? 'canary_match' : 'legacy_fallback'
  });
});

app.get('/api/v1/protocol/banks/settings', (req, res) => {
  const result = store.listBankSettings(req.query.bankId);
  res.json(result);
});

app.get('/api/v1/protocol/banks/:bankId/settings', (req, res) => {
  const result = store.resolveBankSettings(req.params.bankId);
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ settings: result.settings });
});

app.post('/api/v1/protocol/banks/:bankId/settings', (req, res) => {
  const result = store.upsertBankSettings(req.params.bankId, req.body, req.body.performedBy || 'system');
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ settings: result.settings });
});

app.post('/api/v1/protocol/requests/normalize', (req, res) => {
  const result = store.evaluateRequest(req.body);
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.status(result.code).json(result.result);
});

app.get('/api/v1/protocol/events', (req, res) => {
  const result = store.listEvents(req.query.limit);
  res.json(result);
});

app.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`${SERVICE_NAME} listening on port ${PORT}`);
});
