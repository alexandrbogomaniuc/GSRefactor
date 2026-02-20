const fs = require('fs');
const path = require('path');

const STORE_FILE = process.env.STORE_FILE || '/data/config-workflow-store.json';
const MAX_EVENTS = 5000;

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(filePath) {
  const dir = path.dirname(filePath);
  fs.mkdirSync(dir, { recursive: true });
}

function blankState() {
  return {
    drafts: {},
    events: [],
    outbox: []
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
    const raw = fs.readFileSync(STORE_FILE, 'utf8');
    const parsed = JSON.parse(raw);
    if (!parsed || typeof parsed !== 'object') {
      return blankState();
    }
    parsed.drafts = parsed.drafts || {};
    parsed.events = parsed.events || [];
    parsed.outbox = parsed.outbox || [];
    return parsed;
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

function requireDraft(draftVersion, bankId, performedBy, changeReason) {
  if (!draftVersion) {
    return { ok: false, code: 400, message: 'draftVersion is required' };
  }
  if (!bankId) {
    return { ok: false, code: 400, message: 'bankId is required' };
  }
  if (!performedBy) {
    return { ok: false, code: 400, message: 'performedBy is required' };
  }
  return {
    ok: true,
    draftVersion,
    bankId: String(bankId),
    performedBy,
    changeReason: changeReason || ''
  };
}

function putDraft(input) {
  const valid = requireDraft(input.draftVersion, input.bankId, input.performedBy || 'system', input.changeReason);
  if (!valid.ok) {
    return valid;
  }

  const state = loadState();
  const existing = state.drafts[valid.draftVersion] || {
    draftVersion: valid.draftVersion,
    bankId: valid.bankId,
    status: 'DRAFT',
    createdAt: nowIso(),
    updatedAt: nowIso(),
    updatedBy: valid.performedBy,
    changeReason: '',
    configVersion: null,
    payload: {},
    history: []
  };

  existing.bankId = valid.bankId;
  existing.updatedBy = valid.performedBy;
  existing.updatedAt = nowIso();
  if (input.changeReason !== undefined) {
    existing.changeReason = String(input.changeReason || '');
  }
  if (input.payload !== undefined) {
    existing.payload = input.payload;
  }

  existing.history.push({
    action: 'DRAFT',
    at: nowIso(),
    by: valid.performedBy,
    note: existing.changeReason
  });

  state.drafts[valid.draftVersion] = existing;
  const outboxId = appendOutbox(state, 'config.draft.saved', {
    draftVersion: existing.draftVersion,
    bankId: existing.bankId,
    status: existing.status,
    updatedBy: existing.updatedBy
  });

  pushEvent(state, {
    type: 'DRAFT_SAVED',
    draftVersion: existing.draftVersion,
    bankId: existing.bankId,
    by: existing.updatedBy,
    at: nowIso(),
    outboxEventId: outboxId
  });

  saveState(state);
  return { ok: true, code: 200, draft: existing };
}

function listDrafts(filterBankId) {
  const state = loadState();
  const drafts = Object.values(state.drafts)
    .filter((draft) => !filterBankId || String(draft.bankId) === String(filterBankId))
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1));
  return { drafts };
}

function getDraft(draftVersion) {
  const state = loadState();
  const draft = state.drafts[draftVersion];
  if (!draft) {
    return { ok: false, code: 404, message: 'draft not found' };
  }
  return { ok: true, code: 200, draft };
}

function canTransition(status, action) {
  if (action === 'validate') {
    return ['DRAFT', 'VALIDATION_FAILED', 'ROLLED_BACK'].includes(status);
  }
  if (action === 'approve') {
    return ['VALIDATED'].includes(status);
  }
  if (action === 'publish') {
    return ['APPROVED'].includes(status);
  }
  if (action === 'rollback') {
    return ['VALIDATED', 'APPROVED', 'PUBLISHED'].includes(status);
  }
  return false;
}

function workflow(draftVersion, action, performedBy, note) {
  const state = loadState();
  const draft = state.drafts[draftVersion];
  if (!draft) {
    return { ok: false, code: 404, message: 'draft not found' };
  }

  const normalizedAction = String(action || '').toLowerCase();
  if (!['validate', 'approve', 'publish', 'rollback'].includes(normalizedAction)) {
    return { ok: false, code: 400, message: 'unsupported workflow action' };
  }

  if (!canTransition(draft.status, normalizedAction)) {
    return {
      ok: false,
      code: 409,
      message: `invalid transition: ${draft.status} -> ${normalizedAction}`
    };
  }

  let nextStatus = draft.status;
  if (normalizedAction === 'validate') {
    nextStatus = draft.bankId ? 'VALIDATED' : 'VALIDATION_FAILED';
  } else if (normalizedAction === 'approve') {
    nextStatus = 'APPROVED';
  } else if (normalizedAction === 'publish') {
    nextStatus = 'PUBLISHED';
    draft.configVersion = `cfg-${Date.now()}`;
  } else if (normalizedAction === 'rollback') {
    nextStatus = 'ROLLED_BACK';
  }

  draft.status = nextStatus;
  draft.updatedAt = nowIso();
  draft.updatedBy = performedBy || 'system';
  draft.history.push({
    action: normalizedAction.toUpperCase(),
    at: nowIso(),
    by: draft.updatedBy,
    note: note || ''
  });

  const outboxType = `config.workflow.${normalizedAction}`;
  const outboxId = appendOutbox(state, outboxType, {
    draftVersion: draft.draftVersion,
    bankId: draft.bankId,
    status: draft.status,
    configVersion: draft.configVersion,
    by: draft.updatedBy
  });

  pushEvent(state, {
    type: `WORKFLOW_${normalizedAction.toUpperCase()}`,
    draftVersion: draft.draftVersion,
    bankId: draft.bankId,
    by: draft.updatedBy,
    at: nowIso(),
    outboxEventId: outboxId
  });

  state.drafts[draftVersion] = draft;
  saveState(state);
  return { ok: true, code: 200, draft };
}

function listOutbox(status) {
  const state = loadState();
  const outbox = state.outbox
    .filter((item) => !status || item.status === status)
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
  return { outbox };
}

function ackOutbox(eventId) {
  const state = loadState();
  const item = state.outbox.find((event) => event.eventId === eventId);
  if (!item) {
    return { ok: false, code: 404, message: 'outbox event not found' };
  }

  item.status = 'SENT';
  item.updatedAt = nowIso();
  saveState(state);
  return { ok: true, code: 200, outboxEvent: item };
}

module.exports = {
  putDraft,
  listDrafts,
  getDraft,
  workflow,
  listOutbox,
  ackOutbox
};
