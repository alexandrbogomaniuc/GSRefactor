const express = require('express');
const store = require('./store');

const PORT = Number(process.env.PORT || 18070);
const SERVICE_NAME = process.env.SERVICE_NAME || 'config-service';

const app = express();
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.json({
    status: 'ok',
    service: SERVICE_NAME,
    now: new Date().toISOString()
  });
});

app.get('/api/v1/config/drafts', (req, res) => {
  const result = store.listDrafts(req.query.bankId);
  res.json(result);
});

app.get('/api/v1/config/drafts/:draftVersion', (req, res) => {
  const result = store.getDraft(req.params.draftVersion);
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ draft: result.draft });
});

app.post('/api/v1/config/drafts', (req, res) => {
  const result = store.putDraft({
    draftVersion: req.body.draftVersion,
    bankId: req.body.bankId,
    performedBy: req.body.performedBy || 'system',
    changeReason: req.body.changeReason || '',
    payload: req.body.payload || {}
  });
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ draft: result.draft });
});

app.post('/api/v1/config/workflow/:action', (req, res) => {
  const result = store.workflow(
    req.body.draftVersion,
    req.params.action,
    req.body.performedBy || 'system',
    req.body.note || ''
  );
  if (!result.ok) {
    return res.status(result.code).json({ error: result.message });
  }
  return res.json({ draft: result.draft });
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
