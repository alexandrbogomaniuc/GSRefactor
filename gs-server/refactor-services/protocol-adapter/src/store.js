const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/protocol-adapter-store.json';
const MAX_EVENTS = 8000;
const nonceCache = new Map();

const DEFAULT_PROTOCOL_MODE = normalizeProtocolMode(process.env.PROTOCOL_ADAPTER_DEFAULT_MODE || 'XML');
const DEFAULT_HASH_ENABLED = asBool(process.env.PROTOCOL_ADAPTER_JSON_HASH_ENABLED, false);
const DEFAULT_HASH_HEADER = String(process.env.PROTOCOL_ADAPTER_JSON_HASH_HEADER || 'Hash');
const DEFAULT_HASH_ENFORCEMENT = normalizeEnforcement(process.env.PROTOCOL_ADAPTER_JSON_HASH_ENFORCEMENT_MODE || 'OFF');
const DEFAULT_HASH_EXEMPT_ENDPOINTS = parseCsv(process.env.PROTOCOL_ADAPTER_JSON_HASH_EXEMPT_ENDPOINTS || '');
const DEFAULT_GET_HASH_RULES = parseHashRuleMap(process.env.PROTOCOL_ADAPTER_JSON_GET_HASH_RULES || '');
const DEFAULT_REPLAY_ENABLED = asBool(process.env.PROTOCOL_ADAPTER_JSON_REPLAY_ENABLED, false);
const DEFAULT_REPLAY_WINDOW_SECONDS = asPositiveInt(process.env.PROTOCOL_ADAPTER_JSON_REPLAY_WINDOW_SECONDS, 300);
const DEFAULT_NONCE_TTL_SECONDS = asPositiveInt(process.env.PROTOCOL_ADAPTER_JSON_NONCE_TTL_SECONDS, 300);

const BANK_MODE_OVERRIDES = parseKeyValueMap(process.env.PROTOCOL_ADAPTER_BANK_MODES || '');
const BANK_SECRET_REFS = parseKeyValueMap(process.env.PROTOCOL_ADAPTER_JSON_SECRET_REFS || '');
const BANK_HMAC_SECRETS = parseKeyValueMap(process.env.PROTOCOL_ADAPTER_JSON_HMAC_SECRETS || '');

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function blankState() {
  return {
    bankSettings: {},
    events: []
  };
}

function loadState() {
  ensureDir(STORE_FILE);
  if (!fs.existsSync(STORE_FILE)) {
    const state = blankState();
    saveState(state);
    return state;
  }

  try {
    const parsed = JSON.parse(fs.readFileSync(STORE_FILE, 'utf8'));
    return {
      bankSettings: parsed.bankSettings || {},
      events: parsed.events || []
    };
  } catch (_) {
    return blankState();
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

function asBool(value, defaultValue) {
  if (value === undefined || value === null || value === '') {
    return defaultValue;
  }
  return String(value).trim().toLowerCase() === 'true';
}

function asPositiveInt(value, defaultValue) {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return defaultValue;
  }
  return parsed;
}

function normalizeProtocolMode(value) {
  const mode = String(value || '').trim().toUpperCase();
  return mode === 'JSON' ? 'JSON' : 'XML';
}

function normalizeEnforcement(value) {
  const mode = String(value || '').trim().toUpperCase();
  if (mode === 'SHADOW') {
    return 'SHADOW';
  }
  if (mode === 'ENFORCE') {
    return 'ENFORCE';
  }
  return 'OFF';
}

function parseKeyValueMap(input) {
  const map = {};
  if (!input) {
    return map;
  }

  String(input)
    .split(',')
    .map((segment) => segment.trim())
    .filter((segment) => segment.length > 0)
    .forEach((segment) => {
      const separatorIndex = segment.indexOf(':');
      if (separatorIndex <= 0) {
        return;
      }
      const key = segment.slice(0, separatorIndex).trim();
      const value = segment.slice(separatorIndex + 1).trim();
      if (!key || !value) {
        return;
      }
      map[key] = value;
    });

  return map;
}

function parseCsv(input) {
  return String(input || '')
    .split(',')
    .map((segment) => segment.trim())
    .filter((segment) => segment.length > 0);
}

function parseHashRuleMap(input) {
  const out = {};
  if (!input) {
    return out;
  }

  String(input)
    .split(',')
    .map((segment) => segment.trim())
    .filter((segment) => segment.length > 0)
    .forEach((segment) => {
      const separatorIndex = segment.indexOf(':');
      if (separatorIndex <= 0) {
        return;
      }
      const endpoint = normalizeEndpoint(segment.slice(0, separatorIndex));
      const fieldList = segment.slice(separatorIndex + 1).trim();
      if (!endpoint || !fieldList) {
        return;
      }
      const fields = fieldList
        .split('+')
        .map((field) => field.trim())
        .filter((field) => field.length > 0);
      if (fields.length > 0) {
        out[endpoint] = fields;
      }
    });

  return out;
}

function normalizeEndpoint(endpoint) {
  const normalized = String(endpoint || '').trim();
  if (!normalized) {
    return '';
  }
  return normalized.startsWith('/') ? normalized : `/${normalized}`;
}

function stableJsonStringify(value) {
  if (value === null || value === undefined) {
    return 'null';
  }
  if (typeof value !== 'object') {
    return JSON.stringify(value);
  }
  if (Array.isArray(value)) {
    return `[${value.map((item) => stableJsonStringify(item)).join(',')}]`;
  }

  const keys = Object.keys(value).sort();
  const parts = keys
    .filter((key) => value[key] !== undefined)
    .map((key) => `${JSON.stringify(key)}:${stableJsonStringify(value[key])}`);
  return `{${parts.join(',')}}`;
}

function secureCompareHex(leftHex, rightHex) {
  try {
    const left = Buffer.from(String(leftHex || '').trim().toLowerCase(), 'hex');
    const right = Buffer.from(String(rightHex || '').trim().toLowerCase(), 'hex');
    if (left.length === 0 || right.length === 0 || left.length !== right.length) {
      return false;
    }
    return crypto.timingSafeEqual(left, right);
  } catch (_) {
    return false;
  }
}

function findHeaderValue(headers, targetHeaderName) {
  if (!headers || typeof headers !== 'object') {
    return '';
  }
  const target = String(targetHeaderName || '').toLowerCase();
  const foundKey = Object.keys(headers).find((key) => String(key).toLowerCase() === target);
  if (!foundKey) {
    return '';
  }
  const value = headers[foundKey];
  return value === undefined || value === null ? '' : String(value);
}

function getHashInput(rawBody, payload) {
  if (typeof rawBody === 'string' && rawBody.length > 0) {
    return rawBody;
  }
  return stableJsonStringify(payload || {});
}

function buildGetHashInput(fields, query) {
  return fields
    .map((field) => {
      const value = query[field];
      if (value === undefined || value === null) {
        return '';
      }
      return String(value);
    })
    .join('');
}

function isHashExemptEndpoint(settings, endpoint) {
  const exemptions = settings && settings.jsonSecurity && settings.jsonSecurity.hash
    ? settings.jsonSecurity.hash.exemptEndpoints || []
    : [];
  return Array.isArray(exemptions) && exemptions.includes(endpoint);
}

function getBaseSettings(bankId) {
  const envMode = BANK_MODE_OVERRIDES[String(bankId)] || DEFAULT_PROTOCOL_MODE;
  return {
    bankId: String(bankId),
    protocolMode: normalizeProtocolMode(envMode),
    jsonSecurity: {
      hash: {
        enabled: DEFAULT_HASH_ENABLED,
        headerName: DEFAULT_HASH_HEADER,
        algorithm: 'HMAC-SHA256',
        enforcementMode: DEFAULT_HASH_ENFORCEMENT,
        secretRef: BANK_SECRET_REFS[String(bankId)] || null,
        exemptEndpoints: DEFAULT_HASH_EXEMPT_ENDPOINTS.map((endpoint) => normalizeEndpoint(endpoint)),
        getHashRules: DEFAULT_GET_HASH_RULES
      },
      replay: {
        enabled: DEFAULT_REPLAY_ENABLED,
        windowSeconds: DEFAULT_REPLAY_WINDOW_SECONDS,
        nonceTtlSeconds: DEFAULT_NONCE_TTL_SECONDS
      }
    },
    updatedAt: null,
    updatedBy: 'system-env'
  };
}

function mergeSettings(base, override) {
  if (!override) {
    return base;
  }

  const merged = JSON.parse(JSON.stringify(base));
  if (override.protocolMode) {
    merged.protocolMode = normalizeProtocolMode(override.protocolMode);
  }
  if (override.jsonSecurity && typeof override.jsonSecurity === 'object') {
    if (override.jsonSecurity.hash && typeof override.jsonSecurity.hash === 'object') {
      const hash = override.jsonSecurity.hash;
      if (hash.enabled !== undefined) {
        merged.jsonSecurity.hash.enabled = asBool(hash.enabled, merged.jsonSecurity.hash.enabled);
      }
      if (hash.headerName) {
        merged.jsonSecurity.hash.headerName = String(hash.headerName);
      }
      if (hash.enforcementMode) {
        merged.jsonSecurity.hash.enforcementMode = normalizeEnforcement(hash.enforcementMode);
      }
      if (hash.secretRef !== undefined) {
        merged.jsonSecurity.hash.secretRef = hash.secretRef ? String(hash.secretRef) : null;
      }
      if (hash.exemptEndpoints !== undefined) {
        const list = Array.isArray(hash.exemptEndpoints) ? hash.exemptEndpoints : parseCsv(hash.exemptEndpoints);
        merged.jsonSecurity.hash.exemptEndpoints = list.map((endpoint) => normalizeEndpoint(endpoint));
      }
      if (hash.getHashRules !== undefined && hash.getHashRules && typeof hash.getHashRules === 'object') {
        const rules = {};
        Object.keys(hash.getHashRules).forEach((endpoint) => {
          const normalizedEndpoint = normalizeEndpoint(endpoint);
          const fields = Array.isArray(hash.getHashRules[endpoint])
            ? hash.getHashRules[endpoint]
            : String(hash.getHashRules[endpoint] || '').split('+');
          const normalizedFields = fields.map((field) => String(field).trim()).filter((field) => field.length > 0);
          if (normalizedEndpoint && normalizedFields.length > 0) {
            rules[normalizedEndpoint] = normalizedFields;
          }
        });
        merged.jsonSecurity.hash.getHashRules = rules;
      }
    }
    if (override.jsonSecurity.replay && typeof override.jsonSecurity.replay === 'object') {
      const replay = override.jsonSecurity.replay;
      if (replay.enabled !== undefined) {
        merged.jsonSecurity.replay.enabled = asBool(replay.enabled, merged.jsonSecurity.replay.enabled);
      }
      if (replay.windowSeconds !== undefined) {
        merged.jsonSecurity.replay.windowSeconds = asPositiveInt(
          replay.windowSeconds,
          merged.jsonSecurity.replay.windowSeconds
        );
      }
      if (replay.nonceTtlSeconds !== undefined) {
        merged.jsonSecurity.replay.nonceTtlSeconds = asPositiveInt(
          replay.nonceTtlSeconds,
          merged.jsonSecurity.replay.nonceTtlSeconds
        );
      }
    }
  }
  if (override.updatedAt) {
    merged.updatedAt = override.updatedAt;
  }
  if (override.updatedBy) {
    merged.updatedBy = override.updatedBy;
  }
  return merged;
}

function resolveBankSettings(bankId) {
  if (!bankId) {
    return { ok: false, code: 400, message: 'bankId is required' };
  }

  const state = loadState();
  const key = String(bankId);
  const merged = mergeSettings(getBaseSettings(key), state.bankSettings[key]);
  return { ok: true, code: 200, settings: merged };
}

function listBankSettings(filterBankId) {
  const state = loadState();
  const bankIds = new Set();

  Object.keys(BANK_MODE_OVERRIDES).forEach((bankId) => bankIds.add(bankId));
  Object.keys(BANK_SECRET_REFS).forEach((bankId) => bankIds.add(bankId));
  Object.keys(state.bankSettings).forEach((bankId) => bankIds.add(bankId));

  if (filterBankId) {
    bankIds.add(String(filterBankId));
  }

  const settings = Array.from(bankIds)
    .filter((bankId) => !filterBankId || bankId === String(filterBankId))
    .sort((a, b) => a.localeCompare(b))
    .map((bankId) => resolveBankSettings(bankId).settings);

  return { settings };
}

function upsertBankSettings(bankId, patch, performedBy) {
  if (!bankId) {
    return { ok: false, code: 400, message: 'bankId is required' };
  }

  const current = resolveBankSettings(bankId);
  if (!current.ok) {
    return current;
  }

  const next = mergeSettings(current.settings, {
    protocolMode: patch && patch.protocolMode,
    jsonSecurity: patch && patch.jsonSecurity,
    updatedAt: nowIso(),
    updatedBy: performedBy || 'system'
  });

  const state = loadState();
  state.bankSettings[String(bankId)] = next;

  pushEvent(state, {
    type: 'BANK_SETTINGS_UPDATED',
    bankId: String(bankId),
    at: nowIso(),
    by: next.updatedBy,
    protocolMode: next.protocolMode,
    hashEnabled: next.jsonSecurity.hash.enabled,
    hashEnforcementMode: next.jsonSecurity.hash.enforcementMode,
    replayEnabled: next.jsonSecurity.replay.enabled
  });

  saveState(state);
  return { ok: true, code: 200, settings: next };
}

function checkReplayProtection(bankId, settings, headers) {
  if (!settings.jsonSecurity.replay.enabled) {
    return {
      ok: true,
      checked: false,
      replayProtected: false,
      reason: 'replay_check_disabled'
    };
  }

  const tsRaw = findHeaderValue(headers, 'X-Timestamp');
  const nonce = findHeaderValue(headers, 'X-Nonce');
  if (!tsRaw) {
    return {
      ok: false,
      checked: true,
      replayProtected: true,
      reason: 'missing_timestamp'
    };
  }
  if (!nonce) {
    return {
      ok: false,
      checked: true,
      replayProtected: true,
      reason: 'missing_nonce'
    };
  }

  const tsNumber = Number(tsRaw);
  if (!Number.isFinite(tsNumber)) {
    return {
      ok: false,
      checked: true,
      replayProtected: true,
      reason: 'invalid_timestamp'
    };
  }

  // Accept either seconds or milliseconds input.
  const requestTsSec = tsNumber > 1000000000000 ? Math.floor(tsNumber / 1000) : Math.floor(tsNumber);
  const nowSec = Math.floor(Date.now() / 1000);
  const skew = Math.abs(nowSec - requestTsSec);

  if (skew > settings.jsonSecurity.replay.windowSeconds) {
    return {
      ok: false,
      checked: true,
      replayProtected: true,
      reason: 'timestamp_out_of_window'
    };
  }

  const cacheKey = `${bankId}:${nonce}`;
  const expiresAt = nonceCache.get(cacheKey);
  if (expiresAt && expiresAt > Date.now()) {
    return {
      ok: false,
      checked: true,
      replayProtected: true,
      reason: 'nonce_reused'
    };
  }

  nonceCache.set(cacheKey, Date.now() + settings.jsonSecurity.replay.nonceTtlSeconds * 1000);
  return {
    ok: true,
    checked: true,
    replayProtected: true,
    reason: 'ok'
  };
}

function evaluateHash(bankId, settings, method, endpoint, headers, query, rawBody, payload) {
  const result = {
    checked: false,
    required: false,
    verified: null,
    reason: 'hash_not_required',
    headerName: settings.jsonSecurity.hash.headerName,
    enforcementMode: settings.jsonSecurity.hash.enforcementMode,
    hashInput: null,
    expectedHash: null,
    providedHash: null
  };

  if (settings.protocolMode !== 'JSON' || !settings.jsonSecurity.hash.enabled) {
    return result;
  }

  result.checked = true;

  const normalizedEndpoint = normalizeEndpoint(endpoint);
  if (isHashExemptEndpoint(settings, normalizedEndpoint)) {
    result.required = false;
    result.verified = true;
    result.reason = 'hash_exempt_endpoint';
    return result;
  }

  result.required = true;
  const normalizedMethod = String(method || '').toUpperCase();

  const headerName = settings.jsonSecurity.hash.headerName || 'Hash';
  const providedHash = findHeaderValue(headers, headerName);
  result.providedHash = providedHash || null;

  const secret = BANK_HMAC_SECRETS[String(bankId)];
  if (!secret) {
    result.verified = false;
    result.reason = 'secret_not_available';
    return result;
  }

  let hashInput = null;
  if (normalizedMethod === 'POST') {
    hashInput = getHashInput(rawBody, payload);
  } else if (normalizedMethod === 'GET') {
    const rules = settings.jsonSecurity.hash.getHashRules || {};
    const fields = rules[normalizedEndpoint];
    if (!Array.isArray(fields) || fields.length === 0) {
      result.verified = false;
      result.reason = 'get_hash_rule_missing';
      return result;
    }
    hashInput = buildGetHashInput(fields, query || {});
  } else {
    result.verified = false;
    result.reason = 'hash_unsupported_method';
    return result;
  }
  result.hashInput = hashInput;

  const expectedHash = crypto
    .createHmac('sha256', secret)
    .update(hashInput, 'utf8')
    .digest('hex');

  result.expectedHash = expectedHash;

  if (!providedHash) {
    result.verified = false;
    result.reason = 'missing_hash_header';
    return result;
  }

  const verified = secureCompareHex(expectedHash, providedHash);
  result.verified = verified;
  result.reason = verified ? 'ok' : 'hash_mismatch';
  return result;
}

function evaluateRequest(input) {
  if (!input || typeof input !== 'object') {
    return { ok: false, code: 400, message: 'request payload is required' };
  }

  const bankId = String(input.bankId || '').trim();
  const method = String(input.method || '').trim().toUpperCase();
  const endpoint = String(input.endpoint || '').trim();

  if (!bankId) {
    return { ok: false, code: 400, message: 'bankId is required' };
  }
  if (!method) {
    return { ok: false, code: 400, message: 'method is required' };
  }

  const settingsResult = resolveBankSettings(bankId);
  if (!settingsResult.ok) {
    return settingsResult;
  }

  const settings = settingsResult.settings;
  const headers = input.headers && typeof input.headers === 'object' ? input.headers : {};
  const query = input.query && typeof input.query === 'object' ? input.query : {};
  const payload = input.payload && typeof input.payload === 'object' ? input.payload : {};
  const rawBody = typeof input.rawBody === 'string' ? input.rawBody : '';

  const hashResult = evaluateHash(bankId, settings, method, endpoint, headers, query, rawBody, payload);
  const replayResult = hashResult.required
    ? checkReplayProtection(bankId, settings, headers)
    : { ok: true, checked: false, replayProtected: false, reason: 'replay_not_required' };

  let allowed = true;
  let httpStatus = 200;
  const enforcement = settings.jsonSecurity.hash.enforcementMode;
  if (enforcement === 'ENFORCE') {
    if (hashResult.required && hashResult.verified !== true) {
      allowed = false;
      httpStatus = hashResult.reason === 'missing_hash_header' ? 401 : 403;
    }
    if (allowed && replayResult.ok !== true) {
      allowed = false;
      httpStatus = 409;
    }
  }

  const canonicalRequest = {
    bankId,
    protocolMode: settings.protocolMode,
    method,
    endpoint,
    query,
    payload,
    receivedAt: nowIso(),
    traceId: String(input.traceId || ''),
    sessionId: String(input.sessionId || ''),
    operationId: String(input.operationId || '')
  };

  const state = loadState();
  pushEvent(state, {
    type: 'PROTOCOL_REQUEST_EVALUATED',
    bankId,
    at: nowIso(),
    endpoint,
    method,
    protocolMode: settings.protocolMode,
    allowed,
    hashChecked: hashResult.checked,
    hashReason: hashResult.reason,
    replayReason: replayResult.reason
  });
  saveState(state);

  return {
    ok: true,
    code: httpStatus,
    result: {
      allowed,
      httpStatus,
      settings,
      security: {
        hash: {
          checked: hashResult.checked,
          required: hashResult.required,
          verified: hashResult.verified,
          reason: hashResult.reason,
          headerName: hashResult.headerName,
          enforcementMode: hashResult.enforcementMode,
          hashInput: hashResult.hashInput,
          providedHash: hashResult.providedHash,
          expectedHash: hashResult.expectedHash ? '[computed]' : null
        },
        replay: replayResult
      },
      canonicalRequest
    }
  };
}

function listEvents(limit) {
  const state = loadState();
  const max = asPositiveInt(limit, 200);
  const events = state.events.slice(-max).reverse();
  return { events };
}

module.exports = {
  resolveBankSettings,
  listBankSettings,
  upsertBankSettings,
  evaluateRequest,
  listEvents
};
