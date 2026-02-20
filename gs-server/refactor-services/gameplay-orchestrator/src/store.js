const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/gameplay-orchestrator-store.json';
const MAX_EVENTS = 8000;

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function blank() {
  return {
    intents: {},
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
      intents: parsed.intents || {},
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

function upsertIntent(intentType, input, extraRequiredFields, payloadBuilder) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'sessionId', value: input.sessionId },
    { name: 'gameId', value: input.gameId },
    { name: 'operationId', value: input.operationId },
    ...extraRequiredFields
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const opKey = `${intentType}:${String(input.operationId)}`;
  const existingIntentId = state.opIndex[opKey];
  if (existingIntentId && state.intents[existingIntentId]) {
    return { ok: true, code: 200, intent: state.intents[existingIntentId], idempotent: true };
  }

  const intentId = `${intentType}:${input.bankId}:${input.sessionId}:${input.operationId}`;
  const intent = payloadBuilder(intentId);

  state.intents[intentId] = intent;
  state.opIndex[opKey] = intentId;

  const outboxId = appendOutbox(state, `gameplay.${intentType}.requested`, {
    bankId: intent.bankId,
    sessionId: intent.sessionId,
    gameId: intent.gameId,
    operationId: intent.operationId,
    intentId: intent.intentId,
    type: intent.type,
    metadata: intent.metadata || {}
  });

  pushEvent(state, {
    type: 'GAMEPLAY_INTENT_ACCEPTED',
    intentType,
    intentId,
    bankId: intent.bankId,
    sessionId: intent.sessionId,
    operationId: intent.operationId,
    at: nowIso(),
    outboxEventId: outboxId
  });

  saveState(state);
  return { ok: true, code: 200, intent, idempotent: false };
}

function createLaunchIntent(input) {
  return upsertIntent('launch', input, [], (intentId) => ({
    intentId,
    type: 'launch',
    bankId: String(input.bankId),
    sessionId: String(input.sessionId),
    gameId: String(input.gameId),
    operationId: String(input.operationId),
    status: 'ACCEPTED',
    createdAt: nowIso(),
    updatedAt: nowIso(),
    metadata: input.metadata || {}
  }));
}

function createWagerIntent(input) {
  const amountCheck = toPositiveAmount(input.amount);
  if (!amountCheck.ok) {
    return amountCheck;
  }

  return upsertIntent(
    'wager',
    input,
    [
      { name: 'roundId', value: input.roundId },
      { name: 'currency', value: input.currency }
    ],
    (intentId) => ({
      intentId,
      type: 'wager',
      bankId: String(input.bankId),
      sessionId: String(input.sessionId),
      gameId: String(input.gameId),
      operationId: String(input.operationId),
      roundId: String(input.roundId),
      currency: String(input.currency),
      amount: amountCheck.amount,
      status: 'ACCEPTED',
      createdAt: nowIso(),
      updatedAt: nowIso(),
      metadata: input.metadata || {}
    })
  );
}

function createSettleIntent(input) {
  const amountCheck = toPositiveAmount(input.amount);
  if (!amountCheck.ok) {
    return amountCheck;
  }

  return upsertIntent(
    'settle',
    input,
    [
      { name: 'roundId', value: input.roundId },
      { name: 'currency', value: input.currency }
    ],
    (intentId) => ({
      intentId,
      type: 'settle',
      bankId: String(input.bankId),
      sessionId: String(input.sessionId),
      gameId: String(input.gameId),
      operationId: String(input.operationId),
      roundId: String(input.roundId),
      currency: String(input.currency),
      amount: amountCheck.amount,
      status: 'ACCEPTED',
      createdAt: nowIso(),
      updatedAt: nowIso(),
      metadata: input.metadata || {}
    })
  );
}

function listIntents(bankId, type, status) {
  const state = loadState();
  const intents = Object.values(state.intents)
    .filter((item) => (!bankId || item.bankId === String(bankId)))
    .filter((item) => (!type || item.type === String(type)))
    .filter((item) => (!status || item.status === String(status)))
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1));
  return { intents };
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
  createLaunchIntent,
  createWagerIntent,
  createSettleIntent,
  listIntents,
  listOutbox,
  ackOutbox
};
