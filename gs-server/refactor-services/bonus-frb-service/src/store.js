const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/bonus-frb-service-store.json';

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function blank() {
  return {
    frbRecords: {},
    opIndex: {},
    events: []
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
      frbRecords: parsed.frbRecords || {},
      opIndex: parsed.opIndex || {},
      events: parsed.events || []
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

function pushEvent(state, eventType, payload) {
  state.events.push({ eventType, payload, at: nowIso() });
  if (state.events.length > 3000) {
    state.events = state.events.slice(state.events.length - 3000);
  }
}

function key(bankId, accountId, frbId) {
  return `${bankId}:${accountId}:${frbId}`;
}

function checkFrb(input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'accountId', value: input.accountId },
    { name: 'frbId', value: input.frbId }
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const record = state.frbRecords[key(input.bankId, input.accountId, input.frbId)] || {
    bankId: String(input.bankId),
    accountId: String(input.accountId),
    frbId: String(input.frbId),
    status: 'AVAILABLE',
    consumedCount: 0,
    updatedAt: nowIso()
  };
  return { ok: true, code: 200, frb: record };
}

function applyAction(action, input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'accountId', value: input.accountId },
    { name: 'frbId', value: input.frbId },
    { name: 'operationId', value: input.operationId }
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const opKey = `${action}:${String(input.operationId)}`;
  const existing = state.opIndex[opKey];
  if (existing && state.frbRecords[existing]) {
    return { ok: true, code: 200, frb: state.frbRecords[existing], idempotent: true };
  }

  const recordKey = key(input.bankId, input.accountId, input.frbId);
  const record = state.frbRecords[recordKey] || {
    bankId: String(input.bankId),
    accountId: String(input.accountId),
    frbId: String(input.frbId),
    status: 'AVAILABLE',
    consumedCount: 0,
    updatedAt: nowIso()
  };

  if (action === 'consume') {
    record.status = 'CONSUMED';
    record.consumedCount += 1;
  } else if (action === 'release') {
    record.status = 'AVAILABLE';
  }
  record.updatedAt = nowIso();

  state.frbRecords[recordKey] = record;
  state.opIndex[opKey] = recordKey;
  pushEvent(state, `frb.${action}`, {
    bankId: record.bankId,
    accountId: record.accountId,
    frbId: record.frbId,
    operationId: String(input.operationId)
  });
  saveState(state);

  return { ok: true, code: 200, frb: record, idempotent: false };
}

function consumeFrb(input) {
  return applyAction('consume', input);
}

function releaseFrb(input) {
  return applyAction('release', input);
}

function listFrb(bankId, accountId) {
  const state = loadState();
  const records = Object.values(state.frbRecords)
    .filter((item) => (!bankId || item.bankId === String(bankId)))
    .filter((item) => (!accountId || item.accountId === String(accountId)))
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1));
  return { frbRecords: records };
}

module.exports = {
  checkFrb,
  consumeFrb,
  releaseFrb,
  listFrb
};
