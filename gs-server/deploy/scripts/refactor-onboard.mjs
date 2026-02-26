#!/usr/bin/env node

import { spawnSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import http from 'node:http';
import https from 'node:https';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const SCRIPT_DIR = path.dirname(__filename);
const DEPLOY_DIR = path.resolve(SCRIPT_DIR, '..');
const DEV_NEW_ROOT = path.resolve(DEPLOY_DIR, '..', '..');
const REPO_ROOT = DEV_NEW_ROOT;

const START_SCRIPT = path.join(SCRIPT_DIR, 'refactor-start.sh');
const STOP_SCRIPT = path.join(SCRIPT_DIR, 'refactor-stop.sh');
const SYNC_CLUSTER_HOSTS = path.join(SCRIPT_DIR, 'sync-cluster-hosts.sh');
const BOOTSTRAP_SCRIPT = path.join(SCRIPT_DIR, 'refactor-bootstrap-runtime.sh');
const COMPOSE_FILE = path.join(DEPLOY_DIR, 'docker', 'refactor', 'docker-compose.yml');
const DEFAULT_LAUNCH_BASE_URL = process.env.LAUNCH_BASE_URL ?? 'http://127.0.0.1:18080/startgame';
const DEFAULT_LAUNCH_BANK_ID = process.env.LAUNCH_BANK_ID ?? '6275';
const DEFAULT_LAUNCH_SUBCASINO_ID = process.env.LAUNCH_SUBCASINO_ID ?? '507';
const DEFAULT_LAUNCH_GAME_ID = process.env.LAUNCH_GAME_ID ?? '838';
const DEFAULT_LAUNCH_MODE = process.env.LAUNCH_MODE ?? 'real';
const DEFAULT_LAUNCH_TOKEN = process.env.LAUNCH_TOKEN ?? 'bav_game_session_001';
const DEFAULT_LAUNCH_LANG = process.env.LAUNCH_LANG ?? 'en';

function log(msg) {
  process.stdout.write(`[refactor-onboard] ${msg}\n`);
}

function fail(msg, code = 1) {
  process.stderr.write(`[refactor-onboard] ERROR: ${msg}\n`);
  process.exit(code);
}

function usage() {
  process.stdout.write(`Usage: node gs-server/deploy/scripts/refactor-onboard.mjs <command>\n\nCommands:\n  preflight  Check tools and local files, then run refactor preflight\n  up         Start the refactor-only Docker environment (includes preflight)\n  down       Stop the refactor-only Docker environment\n  smoke      Run simple HTTP checks against a running refactor environment\n`);
}

function run(bin, args, opts = {}) {
  const res = spawnSync(bin, args, {
    cwd: opts.cwd ?? REPO_ROOT,
    env: { ...process.env, ...(opts.env ?? {}) },
    stdio: opts.stdio ?? 'inherit',
    shell: false,
  });
  if (res.error) {
    fail(`Failed to run ${bin}: ${res.error.message}`);
  }
  if ((res.status ?? 1) !== 0) {
    if (opts.allowFailure) return res;
    fail(`${bin} ${args.join(' ')} exited with code ${res.status}`);
  }
  return res;
}

function hasCommand(cmd) {
  const checker = process.platform === 'win32' ? 'where' : 'command';
  const checkerArgs = process.platform === 'win32' ? [cmd] : ['-v', cmd];
  const res = spawnSync(checker, checkerArgs, { stdio: 'ignore', shell: process.platform !== 'win32' });
  return res.status === 0;
}

function resolveBash() {
  if (process.env.BASH_BIN && existsSync(process.env.BASH_BIN)) {
    return process.env.BASH_BIN;
  }
  if (hasCommand('bash')) {
    return 'bash';
  }
  if (process.platform === 'win32') {
    const candidates = [
      'C:/Program Files/Git/bin/bash.exe',
      'C:/Program Files/Git/usr/bin/bash.exe',
    ];
    for (const candidate of candidates) {
      if (existsSync(candidate)) return candidate;
    }
  }
  fail('bash was not found. On Windows, install Git for Windows (Git Bash) and ensure bash is in PATH, or set BASH_BIN.');
}

function checkPath(filePath, label) {
  if (!existsSync(filePath)) {
    fail(`${label} is missing: ${filePath}`);
  }
}

function checkRequiredCommands() {
  const required = [
    ['docker', ['--version']],
    ['java', ['-version']],
    ['mvn', ['-v']],
    ['node', ['--version']],
    ['npm', ['--version']],
    ['curl', ['--version']],
    ['unzip', ['-v']],
  ];

  for (const [cmd, args] of required) {
    if (!hasCommand(cmd)) {
      fail(`Missing required command: ${cmd}`);
    }
    run(cmd, args, { stdio: 'ignore' });
    log(`Found ${cmd}`);
  }

  if (hasCommand('rsync')) {
    log('Found rsync (faster file sync path will be used)');
  } else {
    log('rsync not found (okay): bootstrap will use a slower copy fallback');
  }

  run('docker', ['compose', 'version'], { stdio: 'ignore' });
  log('Docker Compose plugin is available');
}

function checkLocalFiles() {
  checkPath(START_SCRIPT, 'Start script');
  checkPath(STOP_SCRIPT, 'Stop script');
  checkPath(SYNC_CLUSTER_HOSTS, 'Cluster host sync script');
  checkPath(BOOTSTRAP_SCRIPT, 'Bootstrap runtime script');
  checkPath(COMPOSE_FILE, 'Refactor compose file');
  const clusterHosts = path.join(DEPLOY_DIR, 'config', 'cluster-hosts.properties');
  checkPath(clusterHosts, 'Cluster host config');
  log('Required repo files are present');
}

function buildLaunchUrl({
  bankId = DEFAULT_LAUNCH_BANK_ID,
  subCasinoId = DEFAULT_LAUNCH_SUBCASINO_ID,
  gameId = DEFAULT_LAUNCH_GAME_ID,
  mode = DEFAULT_LAUNCH_MODE,
  token = DEFAULT_LAUNCH_TOKEN,
  lang = DEFAULT_LAUNCH_LANG,
  baseUrl = DEFAULT_LAUNCH_BASE_URL,
} = {}) {
  const search = new URLSearchParams({
    bankId: String(bankId),
    subCasinoId: String(subCasinoId),
    gameId: String(gameId),
    mode: String(mode),
    token: String(token),
    lang: String(lang),
  });
  return `${baseUrl}?${search.toString()}`;
}

function runBashScript(scriptPath, scriptArgs = []) {
  const bashBin = resolveBash();
  return run(bashBin, [scriptPath, ...scriptArgs]);
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function httpCheck(url, { okStatuses = [200], timeoutMs = 5000, followRedirects = true } = {}, redirects = 0) {
  const parsed = new URL(url);
  const client = parsed.protocol === 'https:' ? https : http;

  return await new Promise((resolve, reject) => {
    const req = client.request(
      parsed,
      { method: 'GET', timeout: timeoutMs, headers: { 'User-Agent': 'refactor-onboard-smoke' } },
      (res) => {
        const status = res.statusCode ?? 0;
        const location = res.headers.location;
        res.resume();

        if (followRedirects && location && status >= 300 && status < 400 && redirects < 5) {
          const nextUrl = new URL(location, parsed).toString();
          resolve(httpCheck(nextUrl, { okStatuses, timeoutMs, followRedirects }, redirects + 1));
          return;
        }

        if (okStatuses.includes(status)) {
          resolve({ ok: true, status, finalUrl: url });
          return;
        }

        resolve({ ok: false, status, finalUrl: url });
      }
    );

    req.on('timeout', () => {
      req.destroy(new Error('timeout'));
    });
    req.on('error', (err) => reject(err));
    req.end();
  });
}

async function retryingHttpCheck(
  check,
  { attempts = 10, delayMs = 3000, timeoutMs = 8000 } = {}
) {
  let lastResult = null;
  let lastError = null;

  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    try {
      const result = await httpCheck(check.url, {
        okStatuses: check.okStatuses,
        timeoutMs,
        followRedirects: true,
      });
      if (result.ok) {
        return { ok: true, result, attempt };
      }
      lastResult = result;
    } catch (err) {
      lastError = err;
    }

    if (attempt < attempts) {
      await sleep(delayMs);
    }
  }

  return { ok: false, result: lastResult, error: lastError, attempt: attempts };
}

async function smokeChecks() {
  const primaryLaunchUrl = buildLaunchUrl();
  const checks = [];

  checks.push(
    // Use a concrete static game asset instead of "/" to avoid startup-time proxy false negatives.
    { label: 'Static asset route', url: 'http://127.0.0.1:18080/html5pc/actiongames/dragonstone/lobby/version.json', okStatuses: [200] },
    // Diagnostic-only signal: can flap during startup even when launch path is healthy.
    { label: 'GS support route (diagnostic)', url: `http://127.0.0.1:18081/support/bankSelectAction.do?bankId=${encodeURIComponent(DEFAULT_LAUNCH_BANK_ID)}`, okStatuses: [200], required: false },
    { label: 'Config service health', url: 'http://127.0.0.1:18072/health', okStatuses: [200] },
    {
      label: 'Launch alias (startgame)',
      url: primaryLaunchUrl,
      okStatuses: [200],
    },
  );

  const secondaryBankId = process.env.SECONDARY_LAUNCH_BANK_ID;
  const secondarySubCasinoId = process.env.SECONDARY_LAUNCH_SUBCASINO_ID;
  if (secondaryBankId && secondarySubCasinoId) {
    const secondaryLaunchUrl = buildLaunchUrl({
      bankId: secondaryBankId,
      subCasinoId: secondarySubCasinoId,
      gameId: process.env.SECONDARY_LAUNCH_GAME_ID ?? DEFAULT_LAUNCH_GAME_ID,
      mode: process.env.SECONDARY_LAUNCH_MODE ?? DEFAULT_LAUNCH_MODE,
      token: process.env.SECONDARY_LAUNCH_TOKEN ?? DEFAULT_LAUNCH_TOKEN,
      lang: process.env.SECONDARY_LAUNCH_LANG ?? DEFAULT_LAUNCH_LANG,
    });
    checks.push({
      label: 'Launch alias (secondary)',
      url: secondaryLaunchUrl,
      okStatuses: [200],
      required: false,
    });
  }

  const maxAttempts = Math.max(1, Number(process.env.REFACTOR_SMOKE_RETRIES ?? '10'));
  const retryDelayMs = Math.max(250, Number(process.env.REFACTOR_SMOKE_DELAY_MS ?? '3000'));

  let failures = 0;
  for (const check of checks) {
    const outcome = await retryingHttpCheck(check, {
      attempts: maxAttempts,
      delayMs: retryDelayMs,
      timeoutMs: 8000,
    });
    const required = check.required !== false;
    if (outcome.ok) {
      const suffix = outcome.attempt > 1 ? ` (attempt ${outcome.attempt}/${maxAttempts})` : '';
      log(`PASS: ${check.label} (${check.url}) -> HTTP ${outcome.result.status}${suffix}`);
    } else {
      if (required) {
        failures += 1;
      }
      const prefix = required ? 'FAIL' : 'WARN';
      if (outcome.result) {
        log(`${prefix}: ${check.label} (${check.url}) -> HTTP ${outcome.result.status} after ${maxAttempts} attempts`);
      } else {
        log(`${prefix}: ${check.label} (${check.url}) -> ${outcome.error?.message ?? 'unknown error'} after ${maxAttempts} attempts`);
      }
    }
  }

  if (failures > 0) {
    fail(`Smoke checks failed (${failures} failure(s)). See lines above.`, 2);
  }

  log('Smoke checks passed. The refactor launch alias is reachable.');
}

async function main() {
  const command = process.argv[2];
  if (!command || command === 'help' || command === '--help' || command === '-h') {
    usage();
    return;
  }

  switch (command) {
    case 'preflight':
      log(`Repo root detected: ${REPO_ROOT}`);
      checkLocalFiles();
      checkRequiredCommands();
      runBashScript(START_SCRIPT, ['preflight']);
      log('Preflight finished. You can now run the up command.');
      return;
    case 'up':
      log(`Repo root detected: ${REPO_ROOT}`);
      checkLocalFiles();
      checkRequiredCommands();
      runBashScript(START_SCRIPT, ['up']);
      log('Refactor environment start command finished. Run smoke if you want a quick check.');
      return;
    case 'down':
      log(`Repo root detected: ${REPO_ROOT}`);
      checkPath(STOP_SCRIPT, 'Stop script');
      if (!hasCommand('docker')) {
        fail('Missing required command: docker');
      }
      runBashScript(STOP_SCRIPT);
      log('Refactor environment stop command finished.');
      return;
    case 'smoke':
      await smokeChecks();
      return;
    default:
      usage();
      fail(`Unknown command: ${command}`);
  }
}

main().catch((err) => fail(err?.message ?? String(err)));
