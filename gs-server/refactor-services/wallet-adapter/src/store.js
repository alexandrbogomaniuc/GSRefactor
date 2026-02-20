const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/wallet-adapter-store.json';
const MAX_EVENTS = 8000;

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function blank() {
  return {
    operations: {},
    opIndex: {},
    outbox: [],
    events: []
  };
}

function normalizeOutboxItem(item) {
  return {
    eventId: item.eventId,
    eventType: item.eventType,
    status: item.status || 'NEW',
    payload: item.payload || {},
    createdAt: item.createdAt || nowIso(),
    updatedAt: item.updatedAt || nowIso()
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
      operations: parsed.operations || {},
      opIndex: parsed.opIndex || {},
      outbox: (parsed.outbox || []).map((item) => normalizeOutboxItem(item)),
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

function pushEvent(state, event) {
  state.events.push(event);
  if (state.events.length > MAX_EVENTS) {
    state.events = state.events.slice(state.events.length - MAX_EVENTS);
  }
}

function appendOutbox(state, eventType, payload) {
  const id = `${Date.now()}-${Math.floor(Math.random() * 1000000)}`;
  state.outbox.push({
    eventId: id,
    eventType,
    status: 'NEW',
    payload,
    createdAt: nowIso(),
    updatedAt: nowIso()
  });
  return id;
}

function ensureRequired(fields) {
  for (const key of fields) {
    if (!key.value) {
      return { ok: false, code: 400, message: `${key.name} is required` };
    }
  }
  return { ok: true };
}

function toPositiveAmount(rawAmount) {
  const amount = Number(rawAmount);
  if (!Number.isFinite(amount) || amount <= 0) {
    return { ok: false, code: 400, message: 'amount must be a positive number' };
  }
  return { ok: true, amount: Number(amount.toFixed(6)) };
}

function upsertOperation(operationType, input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'sessionId', value: input.sessionId },
    { name: 'roundId', value: input.roundId },
    { name: 'operationId', value: input.operationId },
    { name: 'currency', value: input.currency },
    { name: 'accountId', value: input.accountId }
  ]);
  if (!required.ok) {
    return required;
  }

  const amountCheck = toPositiveAmount(input.amount);
  if (!amountCheck.ok) {
    return amountCheck;
  }

  const state = loadState();
  const opKey = `${operationType}:${String(input.operationId)}`;
  const existingOperationId = state.opIndex[opKey];
  if (existingOperationId && state.operations[existingOperationId]) {
    return { ok: true, code: 200, operation: state.operations[existingOperationId], idempotent: true };
  }

  const operationRecordId = `${operationType}:${input.bankId}:${input.operationId}`;
  const operation = {
    operationRecordId,
    type: operationType,
    bankId: String(input.bankId),
    sessionId: String(input.sessionId),
    roundId: String(input.roundId),
    accountId: String(input.accountId),
    operationId: String(input.operationId),
    currency: String(input.currency),
    amount: amountCheck.amount,
    status: 'ACCEPTED',
    createdAt: nowIso(),
    updatedAt: nowIso(),
    metadata: input.metadata || {}
  };

  state.operations[operationRecordId] = operation;
  state.opIndex[opKey] = operationRecordId;

  const outboxId = appendOutbox(state, `wallet.${operationType}.requested`, {
    bankId: operation.bankId,
    sessionId: operation.sessionId,
    roundId: operation.roundId,
    accountId: operation.accountId,
    operationId: operation.operationId,
    currency: operation.currency,
    amount: operation.amount,
    type: operation.type,
    metadata: operation.metadata
  });

  pushEvent(state, {
    type: 'WALLET_OPERATION_ACCEPTED',
    operationType,
    operationRecordId,
    operationId: operation.operationId,
    bankId: operation.bankId,
    at: nowIso(),
    outboxEventId: outboxId
  });

  saveState(state);
  return { ok: true, code: 200, operation, idempotent: false };
}

function reserve(input) {
  return upsertOperation('reserve', input);
}

function settle(input) {
  return upsertOperation('settle', input);
}

function refund(input) {
  return upsertOperation('refund', input);
}

function listOperations(bankId, type, status) {
  const state = loadState();
  const operations = Object.values(state.operations)
    .filter((item) => (!bankId || item.bankId === String(bankId)))
    .filter((item) => (!type || item.type === String(type)))
    .filter((item) => (!status || item.status === String(status)))
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1));
  return { operations };
}

function listOutbox(status) {
  const state = loadState();
  const outbox = state.outbox
    .filter((item) => (!status || item.status === status))
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
  return { outbox };
}

function ackOutbox(eventId) {
  const state = loadState();
  const item = state.outbox.find((entry) => entry.eventId === String(eventId));
  if (!item) {
    return { ok: false, code: 404, message: 'event not found' };
  }
  item.status = 'SENT';
  item.updatedAt = nowIso();
  saveState(state);
  return { ok: true, code: 200, outboxEvent: item };
}

module.exports = {
  reserve,
  settle,
  refund,
  listOperations,
  listOutbox,
  ackOutbox
};
