const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

let redisLib = null;
try {
  // Optional dependency at runtime; service degrades to file state if unavailable.
  redisLib = require('redis');
} catch (_) {
  redisLib = null;
}

const STORE_FILE = process.env.STORE_FILE || '/data/gameplay-orchestrator-store.json';
const MAX_EVENTS = 8000;
const STATE_CACHE_BACKEND = String(process.env.GAMEPLAY_STATE_CACHE_BACKEND || 'file').toLowerCase();
const STATE_BLOB_TTL_SECONDS = Number(process.env.GAMEPLAY_STATE_BLOB_TTL_SECONDS || 900);
const REDIS_URL = String(process.env.REDIS_URL || '').trim();
const REDIS_HOST = String(process.env.REDIS_HOST || 'redis').trim();
const REDIS_PORT = Number(process.env.REDIS_PORT || 6379);
const REDIS_DB = Number(process.env.REDIS_DB || 0);
const REDIS_KEY_PREFIX = String(process.env.GAMEPLAY_STATE_REDIS_KEY_PREFIX || 'abs:gameplay').trim();

let redisClient = null;
let redisUnavailableReason = null;

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function normalizedBackend() {
  return STATE_CACHE_BACKEND === 'redis' ? 'redis' : 'file';
}

function blank() {
  return {
    intents: {},
    opIndex: {},
    outbox: [],
    events: [],
    stateBlobs: {}
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

function normalizeStateBlob(blob) {
  if (!blob || typeof blob !== 'object') {
    return null;
  }
  return {
    stateKey: String(blob.stateKey || ''),
    seed: blob.seed,
    state: blob.state,
    context: blob.context || {},
    fingerprint: String(blob.fingerprint || ''),
    updatedAt: blob.updatedAt || nowIso(),
    ttlSeconds: Number(blob.ttlSeconds || STATE_BLOB_TTL_SECONDS),
    expiresAt: blob.expiresAt || null
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
    const rawStateBlobs = parsed.stateBlobs || {};
    const stateBlobs = {};
    for (const key of Object.keys(rawStateBlobs)) {
      const normalized = normalizeStateBlob(rawStateBlobs[key]);
      if (normalized) {
        stateBlobs[key] = normalized;
      }
    }
    return {
      intents: parsed.intents || {},
      opIndex: parsed.opIndex || {},
      outbox: (parsed.outbox || []).map((item) => normalizeOutboxItem(item)),
      events: parsed.events || [],
      stateBlobs
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

function stableSerialize(input) {
  if (input === null || input === undefined) {
    return JSON.stringify(input);
  }
  if (Array.isArray(input)) {
    return `[${input.map((item) => stableSerialize(item)).join(',')}]`;
  }
  if (typeof input === 'object') {
    const keys = Object.keys(input).sort();
    const body = keys.map((key) => `${JSON.stringify(key)}:${stableSerialize(input[key])}`).join(',');
    return `{${body}}`;
  }
  return JSON.stringify(input);
}

function buildFingerprint(seed, state, context) {
  const payload = stableSerialize({ seed, state, context: context || {} });
  return crypto.createHash('sha256').update(payload).digest('hex');
}

function sanitizeTtl(raw) {
  const ttl = Number(raw);
  if (!Number.isFinite(ttl) || ttl <= 0) {
    return STATE_BLOB_TTL_SECONDS;
  }
  return Math.max(30, Math.floor(ttl));
}

function buildStateBlob(input) {
  const ttlSeconds = sanitizeTtl(input.ttlSeconds);
  const now = Date.now();
  return {
    stateKey: input.stateKey,
    seed: input.seed,
    state: input.state,
    context: input.context || {},
    fingerprint: buildFingerprint(input.seed, input.state, input.context || {}),
    updatedAt: new Date(now).toISOString(),
    ttlSeconds,
    expiresAt: new Date(now + ttlSeconds * 1000).toISOString()
  };
}

function stateBlobExpired(blob) {
  if (!blob || !blob.expiresAt) {
    return false;
  }
  return new Date(blob.expiresAt).getTime() <= Date.now();
}

function redisStateBlobKey(stateKey) {
  return `${REDIS_KEY_PREFIX}:stateblob:${stateKey}`;
}

function redisConfigured() {
  return normalizedBackend() === 'redis';
}

function redisConnectionOptions() {
  if (REDIS_URL.length > 0) {
    return { url: REDIS_URL };
  }
  return {
    socket: {
      host: REDIS_HOST,
      port: REDIS_PORT
    },
    database: REDIS_DB
  };
}

async function getRedisClient() {
  if (!redisConfigured()) {
    return null;
  }
  if (!redisLib) {
    redisUnavailableReason = 'redis_package_missing';
    return null;
  }
  if (redisClient && redisClient.isOpen) {
    return redisClient;
  }
  try {
    redisClient = redisLib.createClient(redisConnectionOptions());
    redisClient.on('error', (err) => {
      redisUnavailableReason = err && err.message ? err.message : 'redis_error';
    });
    await redisClient.connect();
    redisUnavailableReason = null;
    return redisClient;
  } catch (err) {
    redisUnavailableReason = err && err.message ? err.message : 'redis_connect_failed';
    return null;
  }
}

function getStateCacheStatus() {
  return {
    configuredBackend: normalizedBackend(),
    redisHost: REDIS_HOST,
    redisPort: REDIS_PORT,
    redisDb: REDIS_DB,
    redisUnavailableReason
  };
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

async function putStateBlob(input) {
  const stateKey = String(input.stateKey || '').trim();
  if (!stateKey) {
    return { ok: false, code: 400, message: 'stateKey is required' };
  }
  if (input.seed === undefined || input.seed === null) {
    return { ok: false, code: 400, message: 'seed is required' };
  }
  if (input.state === undefined) {
    return { ok: false, code: 400, message: 'state is required' };
  }

  const stateBlob = buildStateBlob({
    stateKey,
    seed: input.seed,
    state: input.state,
    context: input.context || {},
    ttlSeconds: input.ttlSeconds
  });

  let degradedFromRedis = false;
  if (redisConfigured()) {
    const client = await getRedisClient();
    if (client) {
      try {
        await client.set(
          redisStateBlobKey(stateKey),
          JSON.stringify(stateBlob),
          { EX: stateBlob.ttlSeconds }
        );
        return { ok: true, code: 200, stateBlob, cacheBackend: 'redis', degradedFromRedis: false };
      } catch (err) {
        redisUnavailableReason = err && err.message ? err.message : 'redis_set_failed';
        degradedFromRedis = true;
      }
    } else {
      degradedFromRedis = true;
    }
  }

  const state = loadState();
  state.stateBlobs[stateKey] = stateBlob;
  saveState(state);
  return { ok: true, code: 200, stateBlob, cacheBackend: 'file', degradedFromRedis };
}

async function getStateBlob(stateKeyInput) {
  const stateKey = String(stateKeyInput || '').trim();
  if (!stateKey) {
    return { ok: false, code: 400, message: 'stateKey is required' };
  }

  let degradedFromRedis = false;
  if (redisConfigured()) {
    const client = await getRedisClient();
    if (client) {
      try {
        const raw = await client.get(redisStateBlobKey(stateKey));
        if (raw) {
          const parsed = normalizeStateBlob(JSON.parse(raw));
          if (parsed) {
            return { ok: true, code: 200, stateBlob: parsed, cacheBackend: 'redis', degradedFromRedis: false };
          }
        }
      } catch (err) {
        redisUnavailableReason = err && err.message ? err.message : 'redis_get_failed';
        degradedFromRedis = true;
      }
    } else {
      degradedFromRedis = true;
    }
  }

  const state = loadState();
  const blob = normalizeStateBlob(state.stateBlobs[stateKey]);
  if (!blob) {
    return { ok: false, code: 404, message: 'state blob not found', degradedFromRedis };
  }
  if (stateBlobExpired(blob)) {
    delete state.stateBlobs[stateKey];
    saveState(state);
    return { ok: false, code: 404, message: 'state blob expired', degradedFromRedis };
  }
  return { ok: true, code: 200, stateBlob: blob, cacheBackend: 'file', degradedFromRedis };
}

module.exports = {
  createLaunchIntent,
  createWagerIntent,
  createSettleIntent,
  listIntents,
  listOutbox,
  ackOutbox,
  putStateBlob,
  getStateBlob,
  getStateCacheStatus
};
