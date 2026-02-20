const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/multiplayer-service-store.json';

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function blank() {
  return {
    sessions: {},
    operations: {}
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
      operations: parsed.operations || {}
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
  for (const field of fields) {
    if (!field.value) {
      return { ok: false, code: 400, message: `${field.name} is required` };
    }
  }
  return { ok: true };
}

function upsertSession(input) {
  const required = ensureRequired([
    { name: 'bankId', value: input.bankId },
    { name: 'sessionId', value: input.sessionId },
    { name: 'playerId', value: input.playerId },
    { name: 'operationType', value: input.operationType }
  ]);
  if (!required.ok) {
    return required;
  }

  const state = loadState();
  const operationId = String(input.operationId || `${input.operationType}:${input.sessionId}`);
  const opKey = `${input.operationType}:${operationId}`;
  if (state.operations[opKey]) {
    return { ok: true, code: 200, session: state.operations[opKey], idempotent: true };
  }

  const key = `${input.bankId}:${input.sessionId}`;
  const current = state.sessions[key] || {
    bankId: String(input.bankId),
    sessionId: String(input.sessionId),
    playerId: String(input.playerId),
    lobbyId: null,
    roomId: null,
    status: 'INIT',
    updatedAt: nowIso()
  };

  if (input.lobbyId !== undefined) {
    current.lobbyId = input.lobbyId;
  }
  if (input.roomId !== undefined) {
    current.roomId = input.roomId;
  }
  if (input.status) {
    current.status = input.status;
  }
  current.updatedAt = nowIso();

  state.sessions[key] = current;
  state.operations[opKey] = current;
  saveState(state);

  return { ok: true, code: 200, session: current, idempotent: false };
}

function listSessions(bankId, sessionId) {
  const state = loadState();
  let out = Object.values(state.sessions);
  if (bankId) {
    out = out.filter((item) => item.bankId === String(bankId));
  }
  if (sessionId) {
    out = out.filter((item) => item.sessionId === String(sessionId));
  }
  return { sessions: out.sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1)) };
}

module.exports = {
  upsertSession,
  listSessions
};
