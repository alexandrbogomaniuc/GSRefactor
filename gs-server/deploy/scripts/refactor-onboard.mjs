#!/usr/bin/env node

import { spawnSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
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
const CLUSTER_HOSTS_FILE = path.join(DEPLOY_DIR, 'config', 'cluster-hosts.properties');

function parsePropertiesFile(filePath) {
  if (!existsSync(filePath)) {
    return {};
  }
  const out = {};
  const raw = readFileSync(filePath, 'utf8');
  for (const line of raw.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eq = trimmed.indexOf('=');
    if (eq <= 0) continue;
    const key = trimmed.slice(0, eq).trim();
    const value = trimmed.slice(eq + 1).trim();
    out[key] = value;
  }
  return out;
}

const CLUSTER_HOSTS = parsePropertiesFile(CLUSTER_HOSTS_FILE);
const cfg = (key, fallback) => process.env[key] ?? CLUSTER_HOSTS[key] ?? fallback;

const DEFAULT_LAUNCH_BASE_URL = cfg('LAUNCH_BASE_URL', 'http://127.0.0.1:18080/startgame');
const DEFAULT_LAUNCH_BANK_ID = cfg('LAUNCH_BANK_ID', '6275');
const DEFAULT_LAUNCH_SUBCASINO_ID = cfg('LAUNCH_SUBCASINO_ID', '507');
const DEFAULT_LAUNCH_GAME_ID = cfg('LAUNCH_GAME_ID', '838');
const DEFAULT_LAUNCH_MODE = cfg('LAUNCH_MODE', 'real');
const DEFAULT_LAUNCH_TOKEN = cfg('LAUNCH_TOKEN', 'bav_game_session_001');
const DEFAULT_LAUNCH_LANG = cfg('LAUNCH_LANG', 'en');
const DEFAULT_SECONDARY_LAUNCH_BANK_ID = cfg('SECONDARY_LAUNCH_BANK_ID', '');
const DEFAULT_SECONDARY_LAUNCH_SUBCASINO_ID = cfg('SECONDARY_LAUNCH_SUBCASINO_ID', '');
const DEFAULT_SECONDARY_LAUNCH_GAME_ID = cfg('SECONDARY_LAUNCH_GAME_ID', DEFAULT_LAUNCH_GAME_ID);
const DEFAULT_SECONDARY_LAUNCH_MODE = cfg('SECONDARY_LAUNCH_MODE', DEFAULT_LAUNCH_MODE);
const DEFAULT_SECONDARY_LAUNCH_TOKEN = cfg('SECONDARY_LAUNCH_TOKEN', DEFAULT_LAUNCH_TOKEN);
const DEFAULT_SECONDARY_LAUNCH_LANG = cfg('SECONDARY_LAUNCH_LANG', DEFAULT_LAUNCH_LANG);
const DEFAULT_GS_DIRECT_LAUNCH_BASE_URL = cfg(
  'GS_DIRECT_LAUNCH_BASE_URL',
  `http://${cfg('GS_EXTERNAL_HOST', '127.0.0.1')}:${cfg('GS_EXTERNAL_PORT', '18081')}/cwstartgamev2.do`
);
const SESSION_SERVICE_HEALTH_URL = `http://${cfg('SESSION_SERVICE_EXTERNAL_HOST', '127.0.0.1')}:${cfg('SESSION_SERVICE_EXTERNAL_PORT', '18073')}/health`;
const GAMEPLAY_SERVICE_HEALTH_URL = `http://${cfg('GAMEPLAY_ORCHESTRATOR_EXTERNAL_HOST', '127.0.0.1')}:${cfg('GAMEPLAY_ORCHESTRATOR_EXTERNAL_PORT', '18074')}/health`;
const WALLET_SERVICE_HEALTH_URL = `http://${cfg('WALLET_ADAPTER_EXTERNAL_HOST', '127.0.0.1')}:${cfg('WALLET_ADAPTER_EXTERNAL_PORT', '18075')}/health`;
const PROTOCOL_SERVICE_HEALTH_URL = `http://${cfg('PROTOCOL_ADAPTER_EXTERNAL_HOST', '127.0.0.1')}:${cfg('PROTOCOL_ADAPTER_EXTERNAL_PORT', '18078')}/health`;
const DEFAULT_NGINX_ERROR_LOG_PATH = cfg(
  'NGINX_ERROR_LOG_PATH',
  path.join(DEV_NEW_ROOT, 'Doker', 'runtime-gs', 'logs', 'nginx', 'error.log')
);
const REFACTOR_COMPOSE_PROJECT = 'refactor';
const CORE_INFRA_SERVICES = ['c1-refactor', 'zookeeper', 'kafka', 'mp', 'gs', 'static'];
const RECOVERY_ALWAYS_INCLUDE = ['gs', 'static'];
const SMOKE_RECOVERY_WAIT_MS = 3000;
const CORE_RECOVERY_WAIT_MS = 10000;
const RECENT_RESTART_WINDOW_SEC = 120;
const DEFAULT_SOAK_RUNS = 5;
const DEFAULT_SOAK_GAP_MS = 2000;
const DEFAULT_SOAK_ARTIFACT_ROOT = path.join(DEPLOY_DIR, 'artifacts', 'refactor-soak');

function log(msg) {
  process.stdout.write(`[refactor-onboard] ${msg}\n`);
}

function logErr(msg) {
  process.stderr.write(`[refactor-onboard] ${msg}\n`);
}

function fail(msg, code = 1) {
  process.stderr.write(`[refactor-onboard] ERROR: ${msg}\n`);
  process.exit(code);
}

function usage() {
  process.stdout.write(`Usage: node gs-server/deploy/scripts/refactor-onboard.mjs <command>\n\nCommands:\n  preflight  Check tools and local files, then run refactor preflight\n  up         Start the refactor-only Docker environment (includes preflight)\n  down       Stop the refactor-only Docker environment\n  smoke      Run simple HTTP checks against a running refactor environment\n  soak       Run repeated smoke checks and write soak summary artifacts\n`);
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

function toText(output) {
  if (output == null) return '';
  if (Buffer.isBuffer(output)) return output.toString('utf8');
  return String(output);
}

function runCapture(bin, args, opts = {}) {
  const res = spawnSync(bin, args, {
    cwd: opts.cwd ?? REPO_ROOT,
    env: { ...process.env, ...(opts.env ?? {}) },
    stdio: 'pipe',
    shell: false,
  });

  if (res.error) {
    return {
      ok: false,
      status: res.status ?? 1,
      stdout: toText(res.stdout),
      stderr: toText(res.stderr),
      error: res.error,
    };
  }

  const status = res.status ?? 1;
  return {
    ok: status === 0,
    status,
    stdout: toText(res.stdout),
    stderr: toText(res.stderr),
  };
}

function parseBooleanEnv(name, fallback) {
  const raw = process.env[name];
  if (raw == null || raw.trim() === '') return fallback;
  const normalized = raw.trim().toLowerCase();
  if (['1', 'true', 'yes', 'on'].includes(normalized)) return true;
  if (['0', 'false', 'no', 'off'].includes(normalized)) return false;
  return fallback;
}

function parsePositiveIntEnv(name, fallback) {
  const raw = process.env[name];
  if (raw == null || raw.trim() === '') return fallback;
  const parsed = Number.parseInt(raw, 10);
  if (!Number.isFinite(parsed) || parsed < 0) return fallback;
  return parsed;
}

function composeArgs(extraArgs = []) {
  return ['compose', '-p', REFACTOR_COMPOSE_PROJECT, '-f', COMPOSE_FILE, ...extraArgs];
}

function logOutputLines(prefix, text, maxLines = 40, writer = log) {
  if (!text || text.trim() === '') return;
  const lines = text
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);
  const shown = lines.slice(0, maxLines);
  for (const line of shown) {
    writer(`${prefix}${line}`);
  }
  if (lines.length > shown.length) {
    writer(`${prefix}... (${lines.length - shown.length} more line(s) omitted)`);
  }
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

function buildGsDirectLaunchUrl({
  bankId = DEFAULT_LAUNCH_BANK_ID,
  gameId = DEFAULT_LAUNCH_GAME_ID,
  mode = DEFAULT_LAUNCH_MODE,
  token = DEFAULT_LAUNCH_TOKEN,
  lang = DEFAULT_LAUNCH_LANG,
  baseUrl = DEFAULT_GS_DIRECT_LAUNCH_BASE_URL,
} = {}) {
  const search = new URLSearchParams({
    bankId: String(bankId),
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

function emitNginxUpstreamHints() {
  try {
    if (!existsSync(DEFAULT_NGINX_ERROR_LOG_PATH)) {
      return;
    }
    const raw = readFileSync(DEFAULT_NGINX_ERROR_LOG_PATH, 'utf8');
    const lines = raw.trimEnd().split(/\r?\n/);
    const lastLines = lines.slice(-300);
    const hintRegex = /could not be resolved|connect\(\) failed/i;
    const matches = lastLines.filter((line) => hintRegex.test(line));
    if (matches.length === 0) {
      return;
    }
    log(`NGINX-HINT: upstream errors seen in ${DEFAULT_NGINX_ERROR_LOG_PATH} (tail excerpt):`);
    for (const line of matches.slice(-8)) {
      log(`NGINX-HINT: ${line}`);
    }
  } catch (err) {
    log(`WARN: Unable to read nginx error hints: ${err?.message ?? 'unknown error'}`);
  }
}

function normalizeCoreServiceStatus(rawStatus) {
  const status = String(rawStatus ?? '').trim().toLowerCase();
  if (!status) return 'not-running';
  if (status === 'running' || status === 'restarting' || status === 'exited') {
    return status;
  }
  if (status === 'created' || status === 'dead' || status === 'removing' || status === 'paused') {
    return 'not-running';
  }
  return status;
}

function parseStartedAtToAgeSeconds(startedAt) {
  const raw = String(startedAt ?? '').trim();
  if (!raw || raw.startsWith('0001-01-01')) {
    return null;
  }
  const ts = Date.parse(raw);
  if (!Number.isFinite(ts)) {
    return null;
  }
  const ageSeconds = Math.floor((Date.now() - ts) / 1000);
  return ageSeconds >= 0 ? ageSeconds : 0;
}

function isUnhealthyCoreStatus(status) {
  return status === 'restarting' || status === 'exited' || status === 'not-running';
}

function isRecentRestartState(state) {
  return (
    state.status === 'running' &&
    Number.isFinite(state.restartCount) &&
    state.restartCount > 0 &&
    Number.isFinite(state.uptimeSeconds) &&
    state.uptimeSeconds <= RECENT_RESTART_WINDOW_SEC
  );
}

function isUnhealthyCoreState(state) {
  return isUnhealthyCoreStatus(state.status) || isRecentRestartState(state);
}

function inspectCoreInfraServices() {
  if (!hasCommand('docker')) {
    return {
      dockerAvailable: false,
      serviceStates: [],
    };
  }

  const serviceStates = [];
  for (const service of CORE_INFRA_SERVICES) {
    const idsRes = runCapture('docker', composeArgs(['ps', '-q', service]));
    if (!idsRes.ok) {
      serviceStates.push({
        service,
        status: 'not-running',
        restartCount: 'n/a',
        containerId: null,
        inspectError: `compose ps -q failed (exit ${idsRes.status})`,
      });
      continue;
    }

    const ids = idsRes.stdout
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter(Boolean);

    if (ids.length === 0) {
      serviceStates.push({
        service,
        status: 'not-running',
        restartCount: 'n/a',
        containerId: null,
      });
      continue;
    }

    let aggregatedStatus = 'running';
    let maxRestartCount = 0;
    let hasRestartCount = false;
    let selectedContainerId = ids[0];
    let selectedExitDetails = null;
    let selectedInspectError = null;
    let latestUptimeSeconds = null;

    for (const id of ids) {
      const inspectRes = runCapture('docker', ['inspect', id]);
      if (!inspectRes.ok) {
        aggregatedStatus = 'not-running';
        selectedInspectError = `docker inspect failed for ${id.slice(0, 12)} (exit ${inspectRes.status})`;
        selectedContainerId = id;
        continue;
      }

      let inspectObj;
      try {
        const parsed = JSON.parse(inspectRes.stdout);
        inspectObj = Array.isArray(parsed) ? parsed[0] : parsed;
      } catch {
        aggregatedStatus = 'not-running';
        selectedInspectError = `could not parse docker inspect output for ${id.slice(0, 12)}`;
        selectedContainerId = id;
        continue;
      }

      const status = normalizeCoreServiceStatus(inspectObj?.State?.Status);
      const uptimeSeconds = parseStartedAtToAgeSeconds(inspectObj?.State?.StartedAt);
      if (Number.isFinite(uptimeSeconds)) {
        latestUptimeSeconds = uptimeSeconds;
      }
      const parsedRestartCount = Number(inspectObj?.RestartCount);
      if (Number.isFinite(parsedRestartCount)) {
        hasRestartCount = true;
        maxRestartCount = Math.max(maxRestartCount, parsedRestartCount);
      }

      if (status === 'restarting') {
        aggregatedStatus = 'restarting';
        selectedContainerId = id;
      } else if (status === 'exited' && aggregatedStatus !== 'restarting') {
        aggregatedStatus = 'exited';
        selectedContainerId = id;
        selectedExitDetails = {
          oomKilled: Boolean(inspectObj?.State?.OOMKilled),
          exitCode: inspectObj?.State?.ExitCode ?? 'n/a',
          finishedAt: inspectObj?.State?.FinishedAt ?? 'n/a',
          stateError: inspectObj?.State?.Error ? String(inspectObj.State.Error) : '',
        };
      } else if (status !== 'running' && aggregatedStatus === 'running') {
        aggregatedStatus = 'not-running';
        selectedContainerId = id;
      }
    }

    serviceStates.push({
      service,
      status: aggregatedStatus,
      restartCount: hasRestartCount ? maxRestartCount : 'n/a',
      containerId: selectedContainerId,
      exitDetails: selectedExitDetails,
      inspectError: selectedInspectError,
      uptimeSeconds: latestUptimeSeconds,
    });
  }

  return {
    dockerAvailable: true,
    serviceStates,
  };
}

function collectInfraSignals(outcome, coreInfra = null) {
  const signals = new Set(outcome.downDependencies ?? []);
  if (outcome.gsDirectFailed) {
    signals.add('GS direct launch probe');
  }
  if (outcome.gsSupportFailed) {
    signals.add('GS support route probe');
  }
  for (const state of coreInfra?.serviceStates ?? []) {
    if (state.status === 'restarting') {
      signals.add(`core service restarting: ${state.service}`);
    } else if (state.status === 'exited') {
      signals.add(`core service exited: ${state.service}`);
    } else if (state.status === 'not-running') {
      signals.add(`core service not-running: ${state.service}`);
    } else if (isRecentRestartState(state)) {
      signals.add(`core service recently restarted: ${state.service}`);
    }
  }
  return [...signals];
}

function emitInfraDiagnostics(coreInfra = inspectCoreInfraServices()) {
  if (!coreInfra.dockerAvailable) {
    logErr('INFRA-DIAG: docker command not found; cannot emit compose diagnostics.');
    return coreInfra;
  }

  logErr(`INFRA-DIAG: docker compose ps summary (core: ${CORE_INFRA_SERVICES.join(', ')})`);
  const ps = runCapture('docker', composeArgs(['ps', ...CORE_INFRA_SERVICES]));
  if (ps.ok) {
    logOutputLines('INFRA-DIAG: ', ps.stdout, 80, logErr);
  } else {
    logErr(`INFRA-DIAG: docker compose ps failed (exit ${ps.status}).`);
    logOutputLines('INFRA-DIAG: ', ps.stderr || ps.stdout, 40, logErr);
  }

  let exitedInspected = 0;
  for (const state of coreInfra.serviceStates) {
    const unhealthy = isUnhealthyCoreState(state);
    const containerLabel = state.containerId ? ` container=${state.containerId.slice(0, 12)}` : '';
    const uptimeLabel = Number.isFinite(state.uptimeSeconds) ? ` uptimeSeconds=${state.uptimeSeconds}` : '';
    logErr(
      `INFRA-DIAG: service ${state.service} status=${state.status} restartCount=${state.restartCount}${uptimeLabel}${containerLabel}${
        unhealthy ? ' unhealthy=true' : ' unhealthy=false'
      }`
    );
    if (state.status === 'restarting') {
      logErr(`INFRA-DIAG: unhealthy signal -> ${state.service} is restarting (restartCount=${state.restartCount}).`);
    } else if (isRecentRestartState(state)) {
      logErr(
        `INFRA-DIAG: unhealthy signal -> ${state.service} recently restarted (uptimeSeconds=${state.uptimeSeconds}, restartCount=${state.restartCount}).`
      );
    }
    if (state.inspectError) {
      logErr(`INFRA-DIAG: ${state.service} inspect warning: ${state.inspectError}`);
    }
    if (state.status !== 'exited' || !state.exitDetails) {
      continue;
    }
    exitedInspected += 1;
    const { oomKilled, exitCode, finishedAt, stateError } = state.exitDetails;
    logErr(
      `INFRA-DIAG: exited ${state.service}${containerLabel} OOMKilled=${oomKilled} exitCode=${exitCode} finishedAt=${finishedAt}${
        stateError ? ` error=${stateError}` : ''
      }`
    );
  }
  if (exitedInspected === 0) {
    logErr('INFRA-DIAG: no exited core containers found for OOMKilled inspection.');
  }
  return coreInfra;
}

function buildSmokeRecoveryTargets(coreInfra = null) {
  const targets = new Set(RECOVERY_ALWAYS_INCLUDE);
  for (const state of coreInfra?.serviceStates ?? []) {
    if (isUnhealthyCoreState(state)) {
      targets.add(state.service);
    }
  }
  return CORE_INFRA_SERVICES.filter((service) => targets.has(service));
}

function getSmokeRecoveryWaitMs(targets) {
  const hasCoreTargets = targets.some((service) => !RECOVERY_ALWAYS_INCLUDE.includes(service));
  return hasCoreTargets ? CORE_RECOVERY_WAIT_MS : SMOKE_RECOVERY_WAIT_MS;
}

function runSmokeAutoRecovery(attempt, totalAttempts, targets) {
  const services = targets.length > 0 ? targets : RECOVERY_ALWAYS_INCLUDE;
  logErr(
    `SMOKE-RECOVERY: attempt ${attempt}/${totalAttempts} running docker compose up -d ${services.join(' ')}`
  );
  const upRes = runCapture('docker', composeArgs(['up', '-d', ...services]));
  if (!upRes.ok) {
    logErr(`SMOKE-RECOVERY: compose up failed (exit ${upRes.status}).`);
    logOutputLines('SMOKE-RECOVERY: ', upRes.stderr || upRes.stdout, 40, logErr);
    return false;
  }
  logOutputLines('SMOKE-RECOVERY: ', upRes.stdout, 20, logErr);
  return true;
}

async function runSmokePass(checks, { maxAttempts, retryDelayMs }) {
  let failures = 0;
  let launchAliasFailed = false;
  let gsSupportFailed = false;
  let gsDirectFailed = false;
  const downDependencies = [];

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
      if (check.isLaunchAlias) {
        launchAliasFailed = true;
      }
      if (check.dependency) {
        downDependencies.push(check.label);
      }
      if (check.supportProbe) {
        gsSupportFailed = true;
      }
      if (check.gsDirectProbe) {
        gsDirectFailed = true;
      }
      const prefix = required ? 'FAIL' : 'WARN';
      if (outcome.result) {
        log(`${prefix}: ${check.label} (${check.url}) -> HTTP ${outcome.result.status} after ${maxAttempts} attempts`);
      } else {
        log(`${prefix}: ${check.label} (${check.url}) -> ${outcome.error?.message ?? 'unknown error'} after ${maxAttempts} attempts`);
      }
    }
  }

  return {
    failures,
    launchAliasFailed,
    gsSupportFailed,
    gsDirectFailed,
    downDependencies,
  };
}

function buildSmokeStabilityChecks(checks) {
  const stabilityChecks = checks.filter((check) => check.stabilityCritical === true);
  return stabilityChecks.length > 0 ? stabilityChecks : checks.filter((check) => check.required !== false);
}

async function runSmokeStabilityPasses(
  checks,
  { totalPasses, gapMs, maxAttempts, retryDelayMs, isInfraBlockedFailure, emitFailureHints }
) {
  if (totalPasses <= 1) {
    return;
  }
  const stabilityChecks = buildSmokeStabilityChecks(checks);
  if (stabilityChecks.length === 0) {
    log('STABILITY-PASS: no stability checks resolved; skipping additional passes.');
    return;
  }
  for (let pass = 2; pass <= totalPasses; pass += 1) {
    if (gapMs > 0) {
      await sleep(gapMs);
    }
    log(
      `STABILITY-PASS: running pass ${pass}/${totalPasses} on ${stabilityChecks.length} critical checks (retries=${maxAttempts}, delayMs=${retryDelayMs}).`
    );
    const outcome = await runSmokePass(stabilityChecks, { maxAttempts, retryDelayMs });
    if (outcome.failures > 0) {
      if (emitFailureHints) {
        emitFailureHints(outcome);
      }
      if (isInfraBlockedFailure(outcome)) {
        fail(
          `Smoke stability pass ${pass}/${totalPasses} failed with infrastructure signals. See diagnostics above.`,
          3
        );
      }
      fail(
        `Smoke stability pass ${pass}/${totalPasses} failed (${outcome.failures} failure(s)). See lines above.`,
        2
      );
    }
    log(`STABILITY-PASS: pass ${pass}/${totalPasses} succeeded.`);
  }
}

async function smokeChecks() {
  const primaryLaunchUrl = buildLaunchUrl();
  const gsDirectLaunchUrl = buildGsDirectLaunchUrl();
  const checks = [];

  checks.push(
    // Use a concrete static game asset instead of "/" to avoid startup-time proxy false negatives.
    {
      label: 'Static asset route',
      url: 'http://127.0.0.1:18080/html5pc/actiongames/dragonstone/lobby/version.json',
      okStatuses: [200],
      stabilityCritical: true,
    },
    // Diagnostic-only signal: can flap during startup even when launch path is healthy.
    {
      label: 'GS support route (diagnostic)',
      url: `http://127.0.0.1:18081/support/bankSelectAction.do?bankId=${encodeURIComponent(DEFAULT_LAUNCH_BANK_ID)}`,
      okStatuses: [200],
      required: false,
      supportProbe: true,
    },
    {
      label: 'GS direct launch (cwstartgamev2)',
      url: gsDirectLaunchUrl,
      okStatuses: [200],
      required: false,
      gsDirectProbe: true,
      stabilityCritical: true,
    },
    {
      label: 'Config service health',
      url: 'http://127.0.0.1:18072/health',
      okStatuses: [200],
      stabilityCritical: true,
    },
    { label: 'Dependency health: session-service', url: SESSION_SERVICE_HEALTH_URL, okStatuses: [200], required: false, dependency: true },
    { label: 'Dependency health: gameplay-orchestrator', url: GAMEPLAY_SERVICE_HEALTH_URL, okStatuses: [200], required: false, dependency: true },
    { label: 'Dependency health: wallet-adapter', url: WALLET_SERVICE_HEALTH_URL, okStatuses: [200], required: false, dependency: true },
    { label: 'Dependency health: protocol-adapter', url: PROTOCOL_SERVICE_HEALTH_URL, okStatuses: [200], required: false, dependency: true },
    {
      label: 'Launch alias (startgame)',
      url: primaryLaunchUrl,
      okStatuses: [200],
      isLaunchAlias: true,
      stabilityCritical: true,
    },
  );

  const secondaryBankId = process.env.SECONDARY_LAUNCH_BANK_ID;
  const secondarySubCasinoId = process.env.SECONDARY_LAUNCH_SUBCASINO_ID;
  const effectiveSecondaryBankId = secondaryBankId ?? DEFAULT_SECONDARY_LAUNCH_BANK_ID;
  const effectiveSecondarySubCasinoId = secondarySubCasinoId ?? DEFAULT_SECONDARY_LAUNCH_SUBCASINO_ID;
  if (effectiveSecondaryBankId && effectiveSecondarySubCasinoId) {
    const secondaryLaunchUrl = buildLaunchUrl({
      bankId: effectiveSecondaryBankId,
      subCasinoId: effectiveSecondarySubCasinoId,
      gameId: process.env.SECONDARY_LAUNCH_GAME_ID ?? DEFAULT_SECONDARY_LAUNCH_GAME_ID,
      mode: process.env.SECONDARY_LAUNCH_MODE ?? DEFAULT_SECONDARY_LAUNCH_MODE,
      token: process.env.SECONDARY_LAUNCH_TOKEN ?? DEFAULT_SECONDARY_LAUNCH_TOKEN,
      lang: process.env.SECONDARY_LAUNCH_LANG ?? DEFAULT_SECONDARY_LAUNCH_LANG,
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
  const stabilityPasses = Math.max(1, parsePositiveIntEnv('REFACTOR_SMOKE_STABILITY_PASSES', 2));
  const stabilityGapMs = Math.max(0, Number(process.env.REFACTOR_SMOKE_STABILITY_GAP_MS ?? '1500'));
  const stabilityRetries = Math.max(1, parsePositiveIntEnv('REFACTOR_SMOKE_STABILITY_RETRIES', 2));
  const stabilityDelayMs = Math.max(250, Number(process.env.REFACTOR_SMOKE_STABILITY_DELAY_MS ?? '1000'));

  const autoRecoverEnabled = parseBooleanEnv('REFACTOR_SMOKE_AUTORECOVER', true);
  const maxRecoveryAttempts = parsePositiveIntEnv('REFACTOR_SMOKE_RECOVERY_ATTEMPTS', 2);

  let outcome = await runSmokePass(checks, { maxAttempts, retryDelayMs });
  if (outcome.launchAliasFailed) {
    let coreInfra = inspectCoreInfraServices();
    let infraSignals = collectInfraSignals(outcome, coreInfra);
    if (infraSignals.length > 0) {
      emitNginxUpstreamHints();
      coreInfra = emitInfraDiagnostics(coreInfra);
      if (autoRecoverEnabled && maxRecoveryAttempts > 0) {
        let recoveryTargets = buildSmokeRecoveryTargets(coreInfra);
        for (let attempt = 1; attempt <= maxRecoveryAttempts; attempt += 1) {
          const upOk = runSmokeAutoRecovery(attempt, maxRecoveryAttempts, recoveryTargets);
          if (!upOk) {
            continue;
          }
          const recoveryWaitMs = getSmokeRecoveryWaitMs(recoveryTargets);
          log(`SMOKE-RECOVERY: waiting ${recoveryWaitMs}ms before re-probing smoke checks.`);
          await sleep(recoveryWaitMs);
          outcome = await runSmokePass(checks, { maxAttempts, retryDelayMs });
          if (outcome.failures === 0) {
            log(`SMOKE-RECOVERY: recovery succeeded on attempt ${attempt}/${maxRecoveryAttempts}.`);
            log('Smoke checks passed. The refactor launch alias is reachable.');
            return;
          }
          coreInfra = inspectCoreInfraServices();
          infraSignals = collectInfraSignals(outcome, coreInfra);
          emitNginxUpstreamHints();
          coreInfra = emitInfraDiagnostics(coreInfra);
          recoveryTargets = buildSmokeRecoveryTargets(coreInfra);
        }
      } else if (!autoRecoverEnabled) {
        log('SMOKE-RECOVERY: disabled via REFACTOR_SMOKE_AUTORECOVER=0.');
      }
      const attemptLabel = autoRecoverEnabled ? maxRecoveryAttempts : 0;
      const signalLabel = infraSignals.length > 0 ? infraSignals.join(', ') : 'none observed after recovery attempt(s)';
      log(
        `INFRA-BLOCKED: Launch alias failed with infrastructure signals after ${attemptLabel} recovery attempt(s) (${signalLabel}).`
      );
      fail('Smoke checks infra-blocked by upstream/runtime outage. See diagnostics above.', 3);
    }
    log('WARN: Launch alias failed while GS direct/support probes remained healthy; keeping functional classification (rc=2).');
  }

  if (outcome.failures > 0) {
    if (outcome.launchAliasFailed) {
      emitNginxUpstreamHints();
    }
    fail(`Smoke checks failed (${outcome.failures} failure(s)). See lines above.`, 2);
  }

  await runSmokeStabilityPasses(checks, {
    totalPasses: stabilityPasses,
    gapMs: stabilityGapMs,
    maxAttempts: stabilityRetries,
    retryDelayMs: stabilityDelayMs,
    isInfraBlockedFailure: (passOutcome) => passOutcome.launchAliasFailed && collectInfraSignals(passOutcome).length > 0,
    emitFailureHints: (passOutcome) => {
      if (!passOutcome.launchAliasFailed) {
        return;
      }
      const infraSignals = collectInfraSignals(passOutcome);
      if (infraSignals.length > 0) {
        emitNginxUpstreamHints();
        emitInfraDiagnostics();
        log(
          `INFRA-BLOCKED: Stability pass launch alias failed with infrastructure signals (${infraSignals.join(', ')}).`
        );
      }
    },
  });

  log('Smoke checks passed. The refactor launch alias is reachable.');
}

function detectGitSha() {
  const res = runCapture('git', ['rev-parse', 'HEAD'], { cwd: REPO_ROOT });
  if (!res.ok) return 'unknown';
  const sha = res.stdout.trim();
  return sha === '' ? 'unknown' : sha;
}

function classifySmokeRc(rawRc) {
  if (rawRc === 0) {
    return { rc: 0, outcome: 'pass' };
  }
  if (rawRc === 2) {
    return { rc: 2, outcome: 'functional_fail' };
  }
  if (rawRc === 3) {
    return { rc: 3, outcome: 'infra_blocked_fail' };
  }
  return { rc: 3, outcome: 'infra_blocked_fail' };
}

function buildSoakArtifactDir() {
  const configured = (process.env.REFACTOR_SOAK_ARTIFACT_DIR ?? '').trim();
  const root = configured === '' ? DEFAULT_SOAK_ARTIFACT_ROOT : path.resolve(configured);
  const stamp = new Date().toISOString().replace(/[:.]/g, '-');
  const dir = path.join(root, `soak-${stamp}`);
  mkdirSync(dir, { recursive: true });
  return dir;
}

function writeSoakSummaryArtifacts(summary, artifactDir) {
  const jsonPath = path.join(artifactDir, 'soak-summary.json');
  const txtPath = path.join(artifactDir, 'soak-summary.txt');

  writeFileSync(jsonPath, `${JSON.stringify(summary, null, 2)}\n`, 'utf8');

  const lines = [
    'Refactor Soak Summary',
    `started_at=${summary.startedAt}`,
    `ended_at=${summary.endedAt}`,
    `git_sha=${summary.gitSha}`,
    `total_runs=${summary.totalRuns}`,
    `pass_count=${summary.passCount}`,
    `functional_fail_count=${summary.functionalFailCount}`,
    `infra_blocked_fail_count=${summary.infraBlockedFailCount}`,
    `final_rc=${summary.finalRc}`,
    `gap_ms=${summary.gapMs}`,
    `artifact_dir=${summary.artifactDir}`,
    '',
    'Runs:',
  ];

  for (const run of summary.runs) {
    lines.push(
      `run=${run.runIndex} started_at=${run.startedAt} ended_at=${run.endedAt} duration_ms=${run.durationMs} raw_rc=${run.rawRc} rc=${run.rc} outcome=${run.outcome}`
    );
  }

  writeFileSync(txtPath, `${lines.join('\n')}\n`, 'utf8');

  return { jsonPath, txtPath };
}

async function soakChecks() {
  const totalRuns = Math.max(1, parsePositiveIntEnv('REFACTOR_SOAK_RUNS', DEFAULT_SOAK_RUNS));
  const gapMs = Math.max(0, parsePositiveIntEnv('REFACTOR_SOAK_GAP_MS', DEFAULT_SOAK_GAP_MS));
  const artifactDir = buildSoakArtifactDir();
  const gitSha = detectGitSha();
  const startedAt = new Date().toISOString();

  const runs = [];
  let passCount = 0;
  let functionalFailCount = 0;
  let infraBlockedFailCount = 0;

  log(`SOAK: starting ${totalRuns} run(s), gap=${gapMs}ms, artifactDir=${artifactDir}`);

  for (let runIndex = 1; runIndex <= totalRuns; runIndex += 1) {
    const runStartedAt = new Date().toISOString();
    const runStartedMs = Date.now();
    const smokeRes = runCapture(process.execPath, [__filename, 'smoke'], {
      cwd: REPO_ROOT,
    });
    const runEndedAt = new Date().toISOString();
    const durationMs = Date.now() - runStartedMs;
    const rawRc = Number.isInteger(smokeRes.status) ? smokeRes.status : 1;
    const classified = classifySmokeRc(rawRc);

    if (classified.outcome === 'pass') {
      passCount += 1;
    } else if (classified.outcome === 'functional_fail') {
      functionalFailCount += 1;
    } else {
      infraBlockedFailCount += 1;
    }

    runs.push({
      runIndex,
      startedAt: runStartedAt,
      endedAt: runEndedAt,
      durationMs,
      rawRc,
      rc: classified.rc,
      outcome: classified.outcome,
    });

    log(
      `SOAK-RUN: ${runIndex}/${totalRuns} outcome=${classified.outcome} rc=${classified.rc} rawRc=${rawRc} durationMs=${durationMs}`
    );

    if (runIndex < totalRuns && gapMs > 0) {
      await sleep(gapMs);
    }
  }

  const finalRc = infraBlockedFailCount > 0 ? 3 : functionalFailCount > 0 ? 2 : 0;
  const endedAt = new Date().toISOString();
  const summary = {
    startedAt,
    endedAt,
    gitSha,
    totalRuns,
    gapMs,
    artifactDir,
    passCount,
    functionalFailCount,
    infraBlockedFailCount,
    finalRc,
    runs,
  };

  const outputs = writeSoakSummaryArtifacts(summary, artifactDir);
  log(`SOAK: wrote summary artifacts: ${outputs.jsonPath}, ${outputs.txtPath}`);

  if (finalRc === 0) {
    log('SOAK: all runs passed.');
    return;
  }

  fail(
    `Soak finished with failures (functional=${functionalFailCount}, infra-blocked=${infraBlockedFailCount}). See ${outputs.txtPath}`,
    finalRc
  );
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
    case 'soak':
      await soakChecks();
      return;
    default:
      usage();
      fail(`Unknown command: ${command}`);
  }
}

main().catch((err) => fail(err?.message ?? String(err)));
