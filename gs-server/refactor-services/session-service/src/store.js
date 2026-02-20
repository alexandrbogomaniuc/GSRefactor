const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/session-service-store.json';
const MAX_EVENTS = 5000;
const MAX_ERROR_LENGTH = 512;
const MAX_BACKOFF_MS = 60000;

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function blank() {
  return {
    sessions: {},
    opIndex: {},
    outbox: [],
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
      sessions: parsed.sessions || {},
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
    updatedAt: nowIso(),
    attempts: 0,
    lastError: null,
    nextAttemptAt: null,
    dlqAt: null,
    replayCount: 0,
    lastReplayReason: null,
    lastReplayAt: null
  });
  return id;
}

function normalizeOutboxItem(item) {
  const normalizedAttempts = Number.isFinite(Number(item.attempts)) ? Number(item.attempts) : 0;
  const normalizedStatus = item.status || 'NEW';
  return {
    eventId: item.eventId,
    eventType: item.eventType,
    status: normalizedStatus,
    payload: item.payload || {},
    createdAt: item.createdAt || nowIso(),
    updatedAt: item.updatedAt || nowIso(),
    attempts: Math.max(0, normalizedAttempts),
    lastError: item.lastError || null,
    nextAttemptAt: item.nextAttemptAt || null,
    dlqAt: item.dlqAt || null,
    replayCount: Number.isFinite(Number(item.replayCount)) ? Math.max(0, Number(item.replayCount)) : 0,
    lastReplayReason: item.lastReplayReason || null,
    lastReplayAt: item.lastReplayAt || null
  };
}

function ensureRequired(fields) {
  for (const key of fields) {
    if (!key.value) {
      return { ok: false, code: 400, message: `${key.name} is required` };
    }
  }
  return { ok: true };
}

function buildSessionKey(bankId, sessionId) {
  return `${bankId}:${sessionId}`;
}

function createSession(input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'sessionId', value: input.sessionId },
    { name: 'userId', value: input.userId },
    { name: 'gameId', value: input.gameId },
    { name: 'operationId', value: input.operationId }
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const opKey = `create:${input.operationId}`;
  const existingSessionKey = state.opIndex[opKey];
  if (existingSessionKey && state.sessions[existingSessionKey]) {
    return { ok: true, code: 200, session: state.sessions[existingSessionKey], idempotent: true };
  }

  const key = buildSessionKey(String(input.bankId), String(input.sessionId));
  if (state.sessions[key] && state.sessions[key].status !== 'CLOSED') {
    return { ok: false, code: 409, message: 'session already exists and active' };
  }

  const session = {
    bankId: String(input.bankId),
    sessionId: String(input.sessionId),
    userId: String(input.userId),
    gameId: String(input.gameId),
    status: 'OPEN',
    createdAt: nowIso(),
    updatedAt: nowIso(),
    lastTouchedAt: nowIso(),
    closeReason: null,
    metadata: input.metadata || {},
    history: [
      { action: 'CREATE', at: nowIso(), operationId: String(input.operationId) }
    ]
  };

  state.sessions[key] = session;
  state.opIndex[opKey] = key;

  const outboxId = appendOutbox(state, 'session.created', {
    bankId: session.bankId,
    sessionId: session.sessionId,
    userId: session.userId,
    gameId: session.gameId
  });

  pushEvent(state, {
    type: 'SESSION_CREATE',
    bankId: session.bankId,
    sessionId: session.sessionId,
    at: nowIso(),
    operationId: String(input.operationId),
    outboxEventId: outboxId
  });

  saveState(state);
  return { ok: true, code: 200, session, idempotent: false };
}

function touchSession(input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'sessionId', value: input.sessionId },
    { name: 'operationId', value: input.operationId }
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const opKey = `touch:${input.operationId}`;
  const existingKey = state.opIndex[opKey];
  if (existingKey && state.sessions[existingKey]) {
    return { ok: true, code: 200, session: state.sessions[existingKey], idempotent: true };
  }

  const key = buildSessionKey(String(input.bankId), String(input.sessionId));
  const session = state.sessions[key];
  if (!session) {
    return { ok: false, code: 404, message: 'session not found' };
  }
  if (session.status === 'CLOSED') {
    return { ok: false, code: 409, message: 'session already closed' };
  }

  session.lastTouchedAt = nowIso();
  session.updatedAt = nowIso();
  session.history.push({ action: 'TOUCH', at: nowIso(), operationId: String(input.operationId) });
  state.opIndex[opKey] = key;

  const outboxId = appendOutbox(state, 'session.touched', {
    bankId: session.bankId,
    sessionId: session.sessionId,
    lastTouchedAt: session.lastTouchedAt
  });

  pushEvent(state, {
    type: 'SESSION_TOUCH',
    bankId: session.bankId,
    sessionId: session.sessionId,
    at: nowIso(),
    operationId: String(input.operationId),
    outboxEventId: outboxId
  });

  saveState(state);
  return { ok: true, code: 200, session, idempotent: false };
}

function closeSession(input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'sessionId', value: input.sessionId },
    { name: 'operationId', value: input.operationId }
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const opKey = `close:${input.operationId}`;
  const existingKey = state.opIndex[opKey];
  if (existingKey && state.sessions[existingKey]) {
    return { ok: true, code: 200, session: state.sessions[existingKey], idempotent: true };
  }

  const key = buildSessionKey(String(input.bankId), String(input.sessionId));
  const session = state.sessions[key];
  if (!session) {
    return { ok: false, code: 404, message: 'session not found' };
  }

  session.status = 'CLOSED';
  session.closeReason = input.reason || 'UNSPECIFIED';
  session.updatedAt = nowIso();
  session.history.push({
    action: 'CLOSE',
    at: nowIso(),
    operationId: String(input.operationId),
    reason: session.closeReason
  });
  state.opIndex[opKey] = key;

  const outboxId = appendOutbox(state, 'session.closed', {
    bankId: session.bankId,
    sessionId: session.sessionId,
    reason: session.closeReason
  });

  pushEvent(state, {
    type: 'SESSION_CLOSE',
    bankId: session.bankId,
    sessionId: session.sessionId,
    at: nowIso(),
    operationId: String(input.operationId),
    outboxEventId: outboxId
  });

  saveState(state);
  return { ok: true, code: 200, session, idempotent: false };
}

function getSession(bankId, sessionId) {
  const key = buildSessionKey(String(bankId), String(sessionId));
  const state = loadState();
  const session = state.sessions[key];
  if (!session) {
    return { ok: false, code: 404, message: 'session not found' };
  }
  return { ok: true, code: 200, session };
}

function listSessions(bankId, status) {
  const state = loadState();
  const sessions = Object.values(state.sessions)
    .filter((session) => (!bankId || String(session.bankId) === String(bankId)))
    .filter((session) => (!status || String(session.status) === String(status)))
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1));
  return { sessions };
}

function listOutbox(status) {
  const state = loadState();
  const outbox = state.outbox
    .filter((item) => !status || item.status === status)
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
  return { outbox };
}

function claimOutboxForDelivery(limit) {
  const state = loadState();
  const nowMs = Date.now();
  const batchLimit = Math.max(1, Number(limit || 100));
  const outbox = state.outbox
    .filter((item) => item.status === 'NEW' || item.status === 'RETRY')
    .filter((item) => {
      if (!item.nextAttemptAt) {
        return true;
      }
      const nextAttemptMs = Date.parse(item.nextAttemptAt);
      return Number.isFinite(nextAttemptMs) && nextAttemptMs <= nowMs;
    })
    .sort((a, b) => (a.createdAt > b.createdAt ? 1 : -1))
    .slice(0, batchLimit);
  return { outbox };
}

function ackOutbox(eventId) {
  const state = loadState();
  const item = state.outbox.find((event) => event.eventId === eventId);
  if (!item) {
    return { ok: false, code: 404, message: 'outbox event not found' };
  }

  item.status = 'SENT';
  item.nextAttemptAt = null;
  item.updatedAt = nowIso();
  saveState(state);
  return { ok: true, code: 200, outboxEvent: item };
}

function failOutboxDelivery(eventId, errorMessage, maxAttempts, retryBaseMs) {
  const state = loadState();
  const item = state.outbox.find((event) => event.eventId === eventId);
  if (!item) {
    return { ok: false, code: 404, message: 'outbox event not found' };
  }

  const attemptsLimit = Math.max(1, Number(maxAttempts || 5));
  const retryBase = Math.max(100, Number(retryBaseMs || 1000));

  item.attempts = Number(item.attempts || 0) + 1;
  item.lastError = String(errorMessage || 'unknown relay error').slice(0, MAX_ERROR_LENGTH);
  item.updatedAt = nowIso();

  if (item.attempts >= attemptsLimit) {
    item.status = 'DLQ';
    item.dlqAt = nowIso();
    item.nextAttemptAt = null;
  } else {
    const backoffMs = Math.min(retryBase * Math.pow(2, item.attempts - 1), MAX_BACKOFF_MS);
    item.status = 'RETRY';
    item.nextAttemptAt = new Date(Date.now() + backoffMs).toISOString();
  }

  saveState(state);
  return { ok: true, code: 200, outboxEvent: item, movedToDlq: item.status === 'DLQ' };
}

function requeueOutbox(eventId, replayReason, maxReplayCount, replayWindowSeconds) {
  const state = loadState();
  const item = state.outbox.find((event) => event.eventId === eventId);
  if (!item) {
    return { ok: false, code: 404, message: 'outbox event not found' };
  }
  if (item.status !== 'DLQ') {
    return { ok: false, code: 409, message: 'only DLQ events can be requeued' };
  }
  const replayLimit = Math.max(1, Number(maxReplayCount || 5));
  if (Number(item.replayCount || 0) >= replayLimit) {
    return { ok: false, code: 409, message: `replay limit reached (${replayLimit})` };
  }
  const minReplayWindowSec = Math.max(0, Number(replayWindowSeconds || 0));
  if (item.lastReplayAt && minReplayWindowSec > 0) {
    const elapsedMs = Date.now() - Date.parse(item.lastReplayAt);
    if (Number.isFinite(elapsedMs) && elapsedMs < minReplayWindowSec * 1000) {
      const remainingSec = Math.ceil((minReplayWindowSec * 1000 - elapsedMs) / 1000);
      return { ok: false, code: 429, message: `replay window active (${remainingSec}s remaining)` };
    }
  }

  item.status = 'NEW';
  item.nextAttemptAt = null;
  item.dlqAt = null;
  item.lastError = null;
  item.replayCount = Number(item.replayCount || 0) + 1;
  item.lastReplayReason = String(replayReason || 'manual-replay').slice(0, MAX_ERROR_LENGTH);
  item.lastReplayAt = nowIso();
  item.updatedAt = nowIso();

  pushEvent(state, {
    type: 'OUTBOX_REQUEUE',
    eventId: item.eventId,
    eventType: item.eventType,
    at: nowIso(),
    replayCount: item.replayCount,
    reason: item.lastReplayReason
  });

  saveState(state);
  return { ok: true, code: 200, outboxEvent: item };
}

function outboxStatusCounts(outbox) {
  const counts = {
    NEW: 0,
    RETRY: 0,
    DLQ: 0,
    SENT: 0,
    OTHER: 0
  };
  for (const item of outbox) {
    if (counts[item.status] !== undefined) {
      counts[item.status] += 1;
    } else {
      counts.OTHER += 1;
    }
  }
  counts.total = outbox.length;
  return counts;
}

function toReplayRow(item) {
  return {
    eventId: item.eventId,
    eventType: item.eventType,
    status: item.status,
    attempts: item.attempts,
    replayCount: item.replayCount,
    lastReplayAt: item.lastReplayAt,
    lastReplayReason: item.lastReplayReason,
    lastError: item.lastError,
    dlqAt: item.dlqAt,
    updatedAt: item.updatedAt,
    payload: item.payload || {}
  };
}

function getReplayReport(limit) {
  const state = loadState();
  const outbox = state.outbox;
  const reportLimit = Math.max(1, Number(limit || 20));
  const counts = outboxStatusCounts(outbox);
  const replayed = outbox
    .filter((item) => Number(item.replayCount || 0) > 0)
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1))
    .slice(0, reportLimit)
    .map((item) => toReplayRow(item));
  const dlq = outbox
    .filter((item) => item.status === 'DLQ')
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1))
    .slice(0, reportLimit)
    .map((item) => toReplayRow(item));

  return {
    generatedAt: nowIso(),
    counts,
    replayedEvents: replayed,
    dlqEvents: dlq
  };
}

module.exports = {
  createSession,
  touchSession,
  closeSession,
  getSession,
  listSessions,
  listOutbox,
  getReplayReport,
  claimOutboxForDelivery,
  ackOutbox,
  failOutboxDelivery,
  requeueOutbox
};
