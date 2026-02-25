#!/usr/bin/env node

import fs from 'node:fs';
import http from 'node:http';
import https from 'node:https';
import path from 'node:path';

const DEFAULT_BASE_URL = 'http://127.0.0.1:18081';
const DEFAULT_ALLOWED_HOSTS = new Set(['localhost', '127.0.0.1', 'host.docker.internal']);
const DEFAULT_ALLOWED_DOMAIN_TOKENS = new Set(['localhost', '127.0.0.1']);

const SINGLEPLAYER_REQUIRED_FALSE = [
  'ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM',
  'USE_WINNER_FEED',
  'NEEDS_JACKPOT3_FEED',
];

const SINGLEPLAYER_REQUIRED_EMPTY = [
  'UPDATE_PLAYER_STATUS_IN_PRIVATE_ROOM_URL',
  'UPDATE_PLAYERS_ROOMS_NUMBER_URL',
  'GET_FRIENDS_URL',
  'INVATE_PLAYERS_TO_PRIVATE_ROOM_URL',
  'GET_PLAYERS_ONLINE_STATUS_URL',
  'NOTIFICATION_CLOSE_GAME_PROCESSOR_URL',
];

function usage(code = 0) {
  const msg = `Usage:
  node gs-server/deploy/scripts/bank-template-audit.mjs --bank-id <id>[,<id>...] [options]

Options:
  --bank-id <ids>       Required. One ID or comma-separated bank IDs.
  --base-url <url>      GS base URL (default: ${DEFAULT_BASE_URL})
  --mode <mode>         Template mode to validate: singleplayer|multiplayer (default: singleplayer)
  --json-out <file>     Write JSON report to file
  --help                Show this help

What it checks:
  - Third-party http(s) URLs in bank support properties (non-local hosts)
  - Third-party tokens in ALLOWED_ORIGIN / ALLOWED_DOMAINS
  - Singleplayer template cleanup requirements (disable/clear selected fields)
`;
  (code === 0 ? process.stdout : process.stderr).write(msg);
  process.exit(code);
}

function parseArgs(argv) {
  const out = { baseUrl: DEFAULT_BASE_URL, mode: 'singleplayer', bankIds: [], jsonOut: null };
  for (let i = 2; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === '--help' || arg === '-h') usage(0);
    if (arg === '--bank-id') {
      const value = argv[++i];
      if (!value) usage(1);
      out.bankIds.push(...value.split(',').map((s) => s.trim()).filter(Boolean));
      continue;
    }
    if (arg === '--base-url') {
      out.baseUrl = argv[++i];
      if (!out.baseUrl) usage(1);
      continue;
    }
    if (arg === '--mode') {
      out.mode = (argv[++i] || '').trim();
      if (!out.mode) usage(1);
      continue;
    }
    if (arg === '--json-out') {
      out.jsonOut = argv[++i];
      if (!out.jsonOut) usage(1);
      continue;
    }
    process.stderr.write(`Unknown argument: ${arg}\n`);
    usage(1);
  }
  if (!out.bankIds.length) {
    process.stderr.write('Missing required --bank-id\n');
    usage(1);
  }
  if (!['singleplayer', 'multiplayer'].includes(out.mode)) {
    process.stderr.write(`Invalid --mode: ${out.mode}\n`);
    usage(1);
  }
  return out;
}

function httpGet(url) {
  const parsed = new URL(url);
  const client = parsed.protocol === 'https:' ? https : http;
  return new Promise((resolve, reject) => {
    const req = client.request(parsed, { method: 'GET', timeout: 10000 }, (res) => {
      let body = '';
      res.setEncoding('utf8');
      res.on('data', (chunk) => {
        body += chunk;
      });
      res.on('end', () => {
        resolve({ statusCode: res.statusCode || 0, body });
      });
    });
    req.on('timeout', () => req.destroy(new Error('timeout')));
    req.on('error', reject);
    req.end();
  });
}

function decodeHtml(str) {
  return str
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>');
}

function parseSupportInputs(html) {
  const controls = new Map();

  // Parse <input ... name="value(KEY)" ...>
  const inputRe = /<input\b([^>]*?)>/gi;
  let m;
  while ((m = inputRe.exec(html)) !== null) {
    const attrs = m[1];
    const nameMatch = attrs.match(/\bname\s*=\s*"([^"]+)"/i);
    if (!nameMatch) continue;
    const name = nameMatch[1];
    const keyMatch = name.match(/^value\((.+)\)$/);
    if (!keyMatch) continue;
    const key = keyMatch[1];
    const typeMatch = attrs.match(/\btype\s*=\s*"([^"]+)"/i);
    const type = (typeMatch?.[1] || 'text').toLowerCase();
    const checked = /\bchecked\b/i.test(attrs);
    const valueMatch = attrs.match(/\bvalue\s*=\s*"([^"]*)"/i);
    const value = valueMatch ? decodeHtml(valueMatch[1]) : '';
    controls.set(key, { type, value: type === 'checkbox' ? String(checked) : value });
  }

  // Parse <textarea name="value(KEY)">...</textarea> (if any relevant fields exist)
  const textareaRe = /<textarea\b([^>]*?)>([\s\S]*?)<\/textarea>/gi;
  while ((m = textareaRe.exec(html)) !== null) {
    const attrs = m[1];
    const nameMatch = attrs.match(/\bname\s*=\s*"([^"]+)"/i);
    if (!nameMatch) continue;
    const name = nameMatch[1];
    const keyMatch = name.match(/^value\((.+)\)$/);
    if (!keyMatch) continue;
    const key = keyMatch[1];
    controls.set(key, { type: 'textarea', value: decodeHtml(m[2].trim()) });
  }

  return controls;
}

function findThirdPartyUrls(controls, allowedHosts) {
  const findings = [];
  for (const [key, control] of controls.entries()) {
    if (key === 'ALLOWED_ORIGIN' || key === 'ALLOWED_DOMAINS') continue;
    const value = String(control.value || '').trim();
    if (!/^https?:\/\//i.test(value)) continue;
    try {
      const host = new URL(value).hostname.toLowerCase();
      if (!allowedHosts.has(host)) {
        findings.push({ key, value, host });
      }
    } catch {
      findings.push({ key, value, host: '(invalid-url)' });
    }
  }
  return findings;
}

function parseCsvTokens(value) {
  return String(value || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
}

function findThirdPartyAllowListTokens(controls) {
  const findings = [];
  const allowedOrigin = controls.get('ALLOWED_ORIGIN')?.value || '';
  const allowedDomains = controls.get('ALLOWED_DOMAINS')?.value || '';

  for (const token of parseCsvTokens(allowedOrigin)) {
    if (!token) continue;
    if (/^https?:\/\//i.test(token)) {
      try {
        const host = new URL(token).hostname.toLowerCase();
        if (!DEFAULT_ALLOWED_HOSTS.has(host)) {
          findings.push({ key: 'ALLOWED_ORIGIN', value: token, host });
        }
      } catch {
        findings.push({ key: 'ALLOWED_ORIGIN', value: token, host: '(invalid-url)' });
      }
      continue;
    }
    if (!DEFAULT_ALLOWED_DOMAIN_TOKENS.has(token.toLowerCase())) {
      findings.push({ key: 'ALLOWED_ORIGIN', value: token, host: '(non-url-token)' });
    }
  }

  for (const token of parseCsvTokens(allowedDomains)) {
    if (!DEFAULT_ALLOWED_DOMAIN_TOKENS.has(token.toLowerCase())) {
      findings.push({ key: 'ALLOWED_DOMAINS', value: token, host: token.toLowerCase() });
    }
  }
  return findings;
}

function checkSingleplayerTemplate(controls) {
  const violations = [];

  for (const key of SINGLEPLAYER_REQUIRED_FALSE) {
    const value = (controls.get(key)?.value ?? '').toString().toLowerCase();
    if (value !== 'false') {
      violations.push({ key, expected: 'false', actual: controls.get(key)?.value ?? '(missing)' });
    }
  }

  for (const key of SINGLEPLAYER_REQUIRED_EMPTY) {
    const value = String(controls.get(key)?.value ?? '');
    if (value.trim() !== '') {
      violations.push({ key, expected: 'empty', actual: value });
    }
  }

  // Allow only local tokens in these two fields for internal test environments.
  const ao = parseCsvTokens(controls.get('ALLOWED_ORIGIN')?.value || '');
  const ad = parseCsvTokens(controls.get('ALLOWED_DOMAINS')?.value || '');

  for (const token of ao) {
    if (!/^https?:\/\//i.test(token)) {
      violations.push({ key: 'ALLOWED_ORIGIN', expected: 'URL token', actual: token });
      continue;
    }
    try {
      const host = new URL(token).hostname.toLowerCase();
      if (!DEFAULT_ALLOWED_HOSTS.has(host)) {
        violations.push({ key: 'ALLOWED_ORIGIN', expected: 'localhost/127.0.0.1/host.docker.internal only', actual: token });
      }
    } catch {
      violations.push({ key: 'ALLOWED_ORIGIN', expected: 'valid URL', actual: token });
    }
  }

  for (const token of ad) {
    if (!DEFAULT_ALLOWED_DOMAIN_TOKENS.has(token.toLowerCase())) {
      violations.push({ key: 'ALLOWED_DOMAINS', expected: 'localhost/127.0.0.1 only', actual: token });
    }
  }

  return violations;
}

function summarizeKeyFields(controls) {
  const pick = (key) => controls.get(key)?.value ?? '(missing)';
  return {
    WPM_CLASS: pick('WPM_CLASS'),
    START_GAME_PROCESSOR: pick('START_GAME_PROCESSOR'),
    CLOSE_GAME_PROCESSOR: pick('CLOSE_GAME_PROCESSOR'),
    MP_LOBBY_WS_URL: pick('MP_LOBBY_WS_URL'),
    COMMON_WALLET_AUTH_URL: pick('COMMON_WALLET_AUTH_URL'),
    COMMON_WALLET_BALANCE_URL: pick('COMMON_WALLET_BALANCE_URL'),
    COMMON_WALLET_WAGER_URL: pick('COMMON_WALLET_WAGER_URL'),
    COMMON_WALLET_REFUND_URL: pick('COMMON_WALLET_REFUND_URL'),
    ALLOWED_ORIGIN: pick('ALLOWED_ORIGIN'),
    ALLOWED_DOMAINS: pick('ALLOWED_DOMAINS'),
    ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM: pick('ALLOW_UPDATE_PLAYERS_STATUS_IN_PRIVATE_ROOM'),
    USE_WINNER_FEED: pick('USE_WINNER_FEED'),
    NEEDS_JACKPOT3_FEED: pick('NEEDS_JACKPOT3_FEED'),
  };
}

async function auditBank({ baseUrl, bankId, mode }) {
  const url = `${baseUrl.replace(/\/$/, '')}/support/bankSelectAction.do?bankId=${encodeURIComponent(bankId)}`;
  const resp = await httpGet(url);
  if (resp.statusCode !== 200) {
    return { bankId, url, ok: false, fetchError: `HTTP ${resp.statusCode}` };
  }
  const controls = parseSupportInputs(resp.body);
  const thirdPartyUrls = findThirdPartyUrls(controls, DEFAULT_ALLOWED_HOSTS);
  const thirdPartyAllowListTokens = findThirdPartyAllowListTokens(controls);
  const templateViolations = mode === 'singleplayer' ? checkSingleplayerTemplate(controls) : [];

  const ok =
    thirdPartyUrls.length === 0 &&
    thirdPartyAllowListTokens.length === 0 &&
    templateViolations.length === 0;

  return {
    bankId,
    url,
    ok,
    mode,
    fetchStatus: resp.statusCode,
    thirdPartyUrls,
    thirdPartyAllowListTokens,
    templateViolations,
    keyFields: summarizeKeyFields(controls),
  };
}

function printHumanReport(report) {
  process.stdout.write(`Bank template audit (${report.mode})\n`);
  process.stdout.write(`Base URL: ${report.baseUrl}\n`);
  process.stdout.write(`Banks checked: ${report.results.map((r) => r.bankId).join(', ')}\n\n`);

  for (const r of report.results) {
    process.stdout.write(`- Bank ${r.bankId}: ${r.ok ? 'PASS' : 'FAIL'}\n`);
    if (r.fetchError) {
      process.stdout.write(`  Fetch error: ${r.fetchError}\n`);
      continue;
    }
    process.stdout.write(`  Support page: ${r.url}\n`);
    process.stdout.write(`  Third-party URLs: ${r.thirdPartyUrls.length}\n`);
    process.stdout.write(`  Third-party allow-list tokens: ${r.thirdPartyAllowListTokens.length}\n`);
    if (r.mode === 'singleplayer') {
      process.stdout.write(`  Singleplayer template violations: ${r.templateViolations.length}\n`);
    }
    if (r.thirdPartyUrls.length) {
      for (const f of r.thirdPartyUrls) {
        process.stdout.write(`    URL: ${f.key} -> ${f.value}\n`);
      }
    }
    if (r.thirdPartyAllowListTokens.length) {
      for (const f of r.thirdPartyAllowListTokens) {
        process.stdout.write(`    Allow-list: ${f.key} -> ${f.value}\n`);
      }
    }
    if (r.templateViolations.length) {
      for (const v of r.templateViolations) {
        process.stdout.write(`    Template: ${v.key} expected ${v.expected}, actual ${v.actual}\n`);
      }
    }
  }

  process.stdout.write(`\nOverall: ${report.ok ? 'PASS' : 'FAIL'}\n`);
}

async function main() {
  const args = parseArgs(process.argv);
  const results = [];
  for (const bankId of args.bankIds) {
    // eslint-disable-next-line no-await-in-loop
    results.push(await auditBank({ baseUrl: args.baseUrl, bankId, mode: args.mode }));
  }
  const report = {
    generatedAt: new Date().toISOString(),
    baseUrl: args.baseUrl,
    mode: args.mode,
    ok: results.every((r) => r.ok),
    results,
  };

  if (args.jsonOut) {
    const outPath = path.resolve(args.jsonOut);
    fs.mkdirSync(path.dirname(outPath), { recursive: true });
    fs.writeFileSync(outPath, `${JSON.stringify(report, null, 2)}\n`, 'utf8');
  }

  printHumanReport(report);
  process.exit(report.ok ? 0 : 2);
}

main().catch((err) => {
  process.stderr.write(`ERROR: ${err?.message || err}\n`);
  process.exit(1);
});
