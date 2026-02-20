const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18077);
const SERVICE_NAME = process.env.SERVICE_NAME || 'history-service';

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: SERVICE_NAME, now: new Date().toISOString() });
});

app.post('/api/v1/history/records', (req, res) => {
  const result = store.appendRecord({
    bankId: req.body.bankId,
    sessionId: req.body.sessionId,
    operationId: req.body.operationId,
    eventType: req.body.eventType,
    payload: req.body.payload || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ record: result.record, idempotent: result.idempotent });
});

app.get('/api/v1/history/records', (req, res) => {
  const result = store.listRecords(req.query.bankId, req.query.sessionId, req.query.eventType);
  res.json(result);
});

app.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`${SERVICE_NAME} listening on port ${PORT}`);
});
