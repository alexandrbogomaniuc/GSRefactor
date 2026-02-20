#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const inputFile = process.argv[2];
if (!inputFile) {
  // eslint-disable-next-line no-console
  console.error('Usage: validate-session-event-stream.js <jsonl-file>');
  process.exit(1);
}

function isIsoDate(value) {
  if (typeof value !== 'string') {
    return false;
  }
  const parsed = Date.parse(value);
  return Number.isFinite(parsed);
}

function validateBase(event) {
  const errors = [];

  if (!event || typeof event !== 'object') {
    return ['event is not an object'];
  }

  if (typeof event.eventId !== 'string' || event.eventId.trim().length === 0) {
    errors.push('eventId must be non-empty string');
  }

  const allowedTypes = ['session.created', 'session.touched', 'session.closed'];
  if (!allowedTypes.includes(event.eventType)) {
    errors.push(`eventType must be one of ${allowedTypes.join(', ')}`);
  }

  if (!isIsoDate(event.createdAt)) {
    errors.push('createdAt must be ISO date-time');
  }

  if (!event.payload || typeof event.payload !== 'object') {
    errors.push('payload must be object');
    return errors;
  }

  if (typeof event.payload.bankId !== 'string' || event.payload.bankId.trim().length === 0) {
    errors.push('payload.bankId must be non-empty string');
  }

  if (typeof event.payload.sessionId !== 'string' || event.payload.sessionId.trim().length === 0) {
    errors.push('payload.sessionId must be non-empty string');
  }

  return errors;
}

function validateTypeSpecific(event) {
  const errors = [];
  const payload = event.payload || {};

  if (event.eventType === 'session.created') {
    if (typeof payload.userId !== 'string' || payload.userId.trim().length === 0) {
      errors.push('payload.userId must be non-empty string for session.created');
    }
    if (typeof payload.gameId !== 'string' || payload.gameId.trim().length === 0) {
      errors.push('payload.gameId must be non-empty string for session.created');
    }
  }

  if (event.eventType === 'session.touched') {
    if (!isIsoDate(payload.lastTouchedAt)) {
      errors.push('payload.lastTouchedAt must be ISO date-time for session.touched');
    }
  }

  if (event.eventType === 'session.closed') {
    if (typeof payload.reason !== 'string' || payload.reason.trim().length === 0) {
      errors.push('payload.reason must be non-empty string for session.closed');
    }
  }

  return errors;
}

const content = fs.readFileSync(path.resolve(inputFile), 'utf8');
const lines = content
  .split(/\r?\n/)
  .map((line) => line.trim())
  .filter((line) => line.length > 0);

if (lines.length === 0) {
  // eslint-disable-next-line no-console
  console.error('No JSON events to validate');
  process.exit(2);
}

let validCount = 0;
let invalidCount = 0;

lines.forEach((line, index) => {
  let event;
  try {
    event = JSON.parse(line);
  } catch (error) {
    invalidCount += 1;
    // eslint-disable-next-line no-console
    console.error(`line ${index + 1}: invalid JSON (${error.message})`);
    return;
  }

  const errors = [...validateBase(event), ...validateTypeSpecific(event)];
  if (errors.length > 0) {
    invalidCount += 1;
    // eslint-disable-next-line no-console
    console.error(`line ${index + 1}: ${errors.join('; ')}`);
    return;
  }

  validCount += 1;
});

// eslint-disable-next-line no-console
console.log(`validated=${validCount} invalid=${invalidCount} total=${lines.length}`);

if (invalidCount > 0) {
  process.exit(3);
}
