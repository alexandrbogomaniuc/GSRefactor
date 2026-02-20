const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/history-service-store.json';

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function blank() {
  return {
    records: {},
    opIndex: {}
  };
}

function loadState() {
  ensureDir(STORE_FILE);
  if (!fs.existsSync(STORE_FILE)) {
    const state = blank();
    saveState(state);
    return state;
  }
  try {
    const parsed = JSON.parse(fs.readFileSync(STORE_FILE, 'utf8'));
    return {
      records: parsed.records || {},
      opIndex: parsed.opIndex || {}
    };
  } catch (_) {
    return blank();
  }
}

function saveState(state) {
  ensureDir(STORE_FILE);
  const tmp = `${STORE_FILE}.tmp`;
  fs.writeFileSync(tmp, JSON.stringify(state, null, 2));
  fs.renameSync(tmp, STORE_FILE);
}

function ensureRequired(fields) {
  for (const key of fields) {
    if (!key.value) {
      return { ok: false, code: 400, message: `${key.name} is required` };
    }
  }
  return { ok: true };
}

function appendRecord(input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'sessionId', value: input.sessionId },
    { name: 'operationId', value: input.operationId },
    { name: 'eventType', value: input.eventType }
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const opKey = `append:${String(input.operationId)}`;
  const existing = state.opIndex[opKey];
  if (existing) {
    const [bankId, sessionId, recordId] = existing.split(':');
    const row = state.records[`${bankId}:${sessionId}`] || [];
    const rec = row.find((item) => item.recordId === recordId);
    if (rec) {
      return { ok: true, code: 200, record: rec, idempotent: true };
    }
  }

  const key = `${input.bankId}:${input.sessionId}`;
  const row = state.records[key] || [];
  const recordId = `${Date.now()}-${Math.floor(Math.random() * 1000000)}`;
  const record = {
    recordId,
    bankId: String(input.bankId),
    sessionId: String(input.sessionId),
    operationId: String(input.operationId),
    eventType: String(input.eventType),
    payload: input.payload || {},
    createdAt: nowIso()
  };

  row.push(record);
  state.records[key] = row;
  state.opIndex[opKey] = `${input.bankId}:${input.sessionId}:${recordId}`;
  saveState(state);
  return { ok: true, code: 200, record, idempotent: false };
}

function listRecords(bankId, sessionId, eventType) {
  const state = loadState();
  let out = [];

  Object.keys(state.records).forEach((key) => {
    const [recBankId, recSessionId] = key.split(':');
    if (bankId && recBankId !== String(bankId)) {
      return;
    }
    if (sessionId && recSessionId !== String(sessionId)) {
      return;
    }
    out = out.concat(state.records[key]);
  });

  out = out
    .filter((item) => (!eventType || item.eventType === String(eventType)))
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));

  return { records: out };
}

module.exports = {
  appendRecord,
  listRecords
};
