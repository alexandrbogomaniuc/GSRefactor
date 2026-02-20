const http = require("http");
const { URL } = require("url");
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const { spawnSync } = require("child_process");

const PORT = Number(process.env.CM_PORT || 7070);
const CASSANDRA_CONTAINER = process.env.CM_CASSANDRA_CONTAINER || "gp3-c1-1";
const GS_CONTAINER = process.env.CM_GS_CONTAINER || "gp3-gs-1";

const LEGACY_USERS_FILE = path.resolve(__dirname, "..", "data", "users.json");
const CORE_FILE =
  process.env.CM_CORE_FILE ||
  process.env.CM_USERS_FILE ||
  path.resolve(__dirname, "..", "data", "cm-core.json");
const MIRROR_FILE =
  process.env.CM_MIRROR_FILE || path.resolve(__dirname, "..", "data", "cm-mirror.json");

const PUBLIC_DIR = path.resolve(__dirname, "..", "public");
const MENU_FILE = path.resolve(PUBLIC_DIR, "cm-menu.json");

const ACCESS_TTL_MS = 15 * 60 * 1000;
const REFRESH_TTL_MS = 8 * 60 * 60 * 1000;
const LOCK_WINDOW_MS = 15 * 60 * 1000;
const LOCK_THRESHOLD = 5;

const USER_LOGIN_RE = /^[\w\-.@+]+$/;
const USER_EMAIL_RE = /^[\w\-.+]+@[\w\-.]+$/;

const PERM = Object.freeze({
  VIEW_USER_LIST: 1,
  CREATE_USER: 2,
  VIEW_ROLE_LIST: 3,
  CREATE_ROLE: 4,
  EDIT_ROLE: 5,
  VIEW_PLAYER_SEARCH: 6,
  VIEW_BANK_LIST: 7,
  VIEW_TRANSACTIONS: 8,
  VIEW_GAME_SESSION: 9,
  VIEW_WALLET_ALERTS: 10,
  MANAGE_2FA: 11,
  VIEW_OBJECT_HISTORY: 12,
  VIEW_USER_SESSIONS: 13,
  VIEW_USER_IPS: 14,
  RESET_USER_PASSWORD: 15,
  EDIT_USER: 16,
  FLUSH_USER_IPS: 17,
  LOCK_UNLOCK_USER: 18,
  DELETE_USER: 19,
});

const MANAGE_PERMISSIONS = [
  PERM.CREATE_USER,
  PERM.CREATE_ROLE,
  PERM.EDIT_ROLE,
  PERM.MANAGE_2FA,
  PERM.RESET_USER_PASSWORD,
  PERM.EDIT_USER,
  PERM.FLUSH_USER_IPS,
  PERM.LOCK_UNLOCK_USER,
  PERM.DELETE_USER,
];

const REPORT_PERMISSION_BY_ID = Object.freeze({
  userList: PERM.VIEW_USER_LIST,
  roleList: PERM.VIEW_ROLE_LIST,
  playerSearch: PERM.VIEW_PLAYER_SEARCH,
  bankList: PERM.VIEW_BANK_LIST,
  transactions: PERM.VIEW_TRANSACTIONS,
  gameSessionSearch: PERM.VIEW_GAME_SESSION,
  walletOperationAlerts: PERM.VIEW_WALLET_ALERTS,
});

const REPORT_DEFS = [
  {
    id: "playerSearch",
    title: "Player Search",
    path: "/cm/reports/playerSearch",
    fields: [
      { id: "clusterId", label: "Cluster ID", defaultValue: "1" },
      { id: "subcasinoList", label: "Subcasino IDs (csv)", defaultValue: "" },
      { id: "bankList", label: "Bank IDs (csv)", defaultValue: "6274" },
      { id: "nickName", label: "Nickname", defaultValue: "" },
      { id: "accountId", label: "Account ID", defaultValue: "" },
      { id: "extId", label: "Ext. ID", defaultValue: "" },
      { id: "fuzzySearch", label: "Fuzzy Search (true/false)", defaultValue: "false" },
      { id: "regAfterTime", label: "Registered After (iso/ms)", defaultValue: "" },
      { id: "regBeforeTime", label: "Registered Before (iso/ms)", defaultValue: "" },
      { id: "accountStatus", label: "Locked Filter (true/false/-1)", defaultValue: "-1" },
      { id: "mainPerPage", label: "Rows Per Page", defaultValue: "20" },
    ],
  },
  {
    id: "bankList",
    title: "Bank List",
    path: "/cm/reports/bankList",
    fields: [{ id: "limit", label: "Limit", defaultValue: "50" }],
  },
  {
    id: "transactions",
    title: "Transactions",
    path: "/cm/reports/transactions",
    fields: [
      { id: "extId", label: "Ext. ID", defaultValue: "bav_game_session_001" },
      { id: "limit", label: "Limit", defaultValue: "20" },
    ],
  },
  {
    id: "gameSessionSearch",
    title: "Game Session Search",
    path: "/cm/reports/gameSessionSearch",
    fields: [{ id: "gameSessionId", label: "Game Session ID", defaultValue: "" }],
  },
  {
    id: "walletOperationAlerts",
    title: "Wallet Operation Alerts",
    path: "/cm/reports/walletOperationAlerts",
    fields: [{ id: "limit", label: "Limit", defaultValue: "20" }],
  },
];

const DEFAULT_CATALOG = {
  clusters: [{ id: 1, title: "gp3" }],
  subcasinos: [
    { id: 507, title: "localhost", clusterId: 1 },
    { id: 59, title: "gp3-main", clusterId: 1 },
  ],
  banks: [
    { id: 6274, title: "bank-6274", clusterId: 1, subcasinoId: 507 },
    { id: 6275, title: "bank-6275", clusterId: 1, subcasinoId: 507 },
  ],
};

const DEFAULT_PERMISSIONS = [
  { id: PERM.VIEW_USER_LIST, title: "View User List", category: "Management" },
  { id: PERM.CREATE_USER, title: "Create User", category: "Management" },
  { id: PERM.VIEW_ROLE_LIST, title: "View Role List", category: "Management" },
  { id: PERM.CREATE_ROLE, title: "Create Role", category: "Management" },
  { id: PERM.EDIT_ROLE, title: "Edit Role", category: "Management" },
  { id: PERM.VIEW_PLAYER_SEARCH, title: "View Player Search", category: "Reports" },
  { id: PERM.VIEW_BANK_LIST, title: "View Bank List", category: "Reports" },
  { id: PERM.VIEW_TRANSACTIONS, title: "View Transactions", category: "Reports" },
  { id: PERM.VIEW_GAME_SESSION, title: "View Game Session Search", category: "Reports" },
  { id: PERM.VIEW_WALLET_ALERTS, title: "View Wallet Operation Alerts", category: "Alerts" },
  { id: PERM.MANAGE_2FA, title: "Manage 2FA", category: "Management" },
  { id: PERM.VIEW_OBJECT_HISTORY, title: "View Object Change History", category: "Management" },
  { id: PERM.VIEW_USER_SESSIONS, title: "View User Sessions", category: "Management" },
  { id: PERM.VIEW_USER_IPS, title: "View User IPs", category: "Management" },
  { id: PERM.RESET_USER_PASSWORD, title: "Reset User Password", category: "Management" },
  { id: PERM.EDIT_USER, title: "Edit User", category: "Management" },
  { id: PERM.FLUSH_USER_IPS, title: "Flush User IPs", category: "Management" },
  { id: PERM.LOCK_UNLOCK_USER, title: "Lock/Unlock User", category: "Management" },
  { id: PERM.DELETE_USER, title: "Delete User", category: "Management" },
];

const SUPPORT_PERMISSION_IDS = Object.freeze([
  PERM.VIEW_USER_LIST,
  PERM.VIEW_ROLE_LIST,
  PERM.VIEW_PLAYER_SEARCH,
  PERM.VIEW_BANK_LIST,
  PERM.VIEW_TRANSACTIONS,
  PERM.VIEW_GAME_SESSION,
  PERM.VIEW_WALLET_ALERTS,
  PERM.VIEW_OBJECT_HISTORY,
  PERM.VIEW_USER_SESSIONS,
  PERM.VIEW_USER_IPS,
]);

const USER_MANAGER_PERMISSION_IDS = Object.freeze([
  PERM.VIEW_USER_LIST,
  PERM.CREATE_USER,
  PERM.VIEW_ROLE_LIST,
  PERM.CREATE_ROLE,
  PERM.EDIT_ROLE,
  PERM.MANAGE_2FA,
  PERM.VIEW_OBJECT_HISTORY,
  PERM.VIEW_USER_SESSIONS,
  PERM.VIEW_USER_IPS,
  PERM.RESET_USER_PASSWORD,
  PERM.EDIT_USER,
  PERM.FLUSH_USER_IPS,
  PERM.LOCK_UNLOCK_USER,
]);

const accessTokens = new Map(); // token -> { username, expiresAt, sessionId }
const refreshTokens = new Map(); // hash -> { username, expiresAt, revoked }

function nowIso() {
  return new Date().toISOString();
}

function ensureDir(p) {
  fs.mkdirSync(path.dirname(p), { recursive: true });
}

function uniqIntArray(values) {
  const input = Array.isArray(values) ? values : [];
  const out = [];
  const seen = new Set();
  for (const v of input) {
    const n = Number(v);
    if (!Number.isInteger(n)) continue;
    if (seen.has(n)) continue;
    seen.add(n);
    out.push(n);
  }
  return out;
}

function formatDisplayDate(value) {
  if (!value) return "";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "";
  const months = [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
  ];
  const dd = String(d.getUTCDate()).padStart(2, "0");
  const mon = months[d.getUTCMonth()];
  const yyyy = d.getUTCFullYear();
  const hh = String(d.getUTCHours()).padStart(2, "0");
  const mm = String(d.getUTCMinutes()).padStart(2, "0");
  const ss = String(d.getUTCSeconds()).padStart(2, "0");
  return `${dd} ${mon} ${yyyy} ${hh}:${mm}:${ss}`;
}

function formatDurationMs(ms) {
  const n = Number(ms);
  if (!Number.isFinite(n) || n <= 0) return "00:00:00.000";
  const total = Math.floor(n);
  const hh = String(Math.floor(total / 3600000)).padStart(2, "0");
  const mm = String(Math.floor((total % 3600000) / 60000)).padStart(2, "0");
  const ss = String(Math.floor((total % 60000) / 1000)).padStart(2, "0");
  const mss = String(total % 1000).padStart(3, "0");
  return `${hh}:${mm}:${ss}.${mss}`;
}

function humanSince(iso) {
  if (!iso) return "";
  const ts = new Date(iso).getTime();
  if (!Number.isFinite(ts)) return "";
  const sec = Math.max(0, Math.floor((Date.now() - ts) / 1000));
  if (sec < 60) return `${sec}s`;
  const min = Math.floor(sec / 60);
  if (min < 60) return `${min}m`;
  const hour = Math.floor(min / 60);
  if (hour < 48) return `${hour}h ${min % 60}m`;
  const day = Math.floor(hour / 24);
  return `${day}d ${hour % 24}h`;
}

function extractClientIp(req) {
  const xff = String(req.headers["x-forwarded-for"] || "").trim();
  if (xff) return xff.split(",")[0].trim();
  return req.socket && req.socket.remoteAddress
    ? String(req.socket.remoteAddress)
    : "unknown";
}

function loadCoreDbRaw() {
  if (fs.existsSync(CORE_FILE)) {
    try {
      return JSON.parse(fs.readFileSync(CORE_FILE, "utf8"));
    } catch (_err) {
      return { users: [] };
    }
  }

  if (fs.existsSync(LEGACY_USERS_FILE)) {
    try {
      return JSON.parse(fs.readFileSync(LEGACY_USERS_FILE, "utf8"));
    } catch (_err) {
      return { users: [] };
    }
  }

  return { users: [] };
}

function saveCoreDb(db) {
  const toSave = Object.assign({}, db, { version: 3, updatedAt: nowIso() });
  ensureDir(CORE_FILE);
  const tmp = `${CORE_FILE}.tmp`;
  fs.writeFileSync(tmp, JSON.stringify(toSave, null, 2), "utf8");
  fs.renameSync(tmp, CORE_FILE);
}

function loadMirrorDb() {
  if (!fs.existsSync(MIRROR_FILE)) {
    return { version: 1, lastUpdate: null, reports: {} };
  }
  try {
    const parsed = JSON.parse(fs.readFileSync(MIRROR_FILE, "utf8"));
    return parsed && typeof parsed === "object"
      ? parsed
      : { version: 1, lastUpdate: null, reports: {} };
  } catch (_err) {
    return { version: 1, lastUpdate: null, reports: {} };
  }
}

function saveMirrorDb(db) {
  const toSave = Object.assign({}, db, { version: 1 });
  ensureDir(MIRROR_FILE);
  const tmp = `${MIRROR_FILE}.tmp`;
  fs.writeFileSync(tmp, JSON.stringify(toSave, null, 2), "utf8");
  fs.renameSync(tmp, MIRROR_FILE);
}

function writeMirrorSnapshot(reportId, sourceTable, filters, rows) {
  try {
    const db = loadMirrorDb();
    const safeRows = Array.isArray(rows) ? rows.slice(0, 200) : [];
    db.lastUpdate = nowIso();
    db.reports = db.reports || {};
    db.reports[reportId] = {
      updatedAt: nowIso(),
      sourceTable,
      filters,
      count: Array.isArray(rows) ? rows.length : 0,
      rows: safeRows,
    };
    saveMirrorDb(db);
  } catch (_err) {
    // mirror is best effort
  }
}

function hashPasswordScrypt(password) {
  const salt = crypto.randomBytes(16).toString("hex");
  const digest = crypto.scryptSync(password, salt, 64).toString("hex");
  return `scrypt$${salt}$${digest}`;
}

function verifyPasswordScrypt(stored, candidate) {
  const parts = String(stored || "").split("$");
  if (parts.length !== 3 || parts[0] !== "scrypt") return false;
  const [, salt, digest] = parts;
  const candDigest = crypto.scryptSync(candidate, salt, 64).toString("hex");
  const a = Buffer.from(digest, "hex");
  const b = Buffer.from(candDigest, "hex");
  if (a.length !== b.length) return false;
  return crypto.timingSafeEqual(a, b);
}

function hashToken(token) {
  return crypto.createHash("sha256").update(token).digest("hex");
}

function newToken(size = 32) {
  return crypto.randomBytes(size).toString("base64url");
}

function generateStrongPassword() {
  return `A${newToken(10)}#9z`;
}

function json(res, statusCode, payload) {
  const body = JSON.stringify(payload);
  res.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "Content-Length": Buffer.byteLength(body),
  });
  res.end(body);
}

function text(res, statusCode, body, contentType) {
  res.writeHead(statusCode, {
    "Content-Type": contentType || "text/plain; charset=utf-8",
    "Content-Length": Buffer.byteLength(body),
  });
  res.end(body);
}

function contentTypeFor(filePath) {
  if (filePath.endsWith(".html")) return "text/html; charset=utf-8";
  if (filePath.endsWith(".css")) return "text/css; charset=utf-8";
  if (filePath.endsWith(".js")) return "application/javascript; charset=utf-8";
  if (filePath.endsWith(".json")) return "application/json; charset=utf-8";
  if (filePath.endsWith(".svg")) return "image/svg+xml";
  if (filePath.endsWith(".png")) return "image/png";
  return "application/octet-stream";
}

function safeStaticPath(urlPath) {
  const rel = urlPath === "/" ? "index.html" : String(urlPath || "").replace(/^\/+/, "");
  const clean = path.normalize(rel).replace(/^((\.\.)[\\/])+/g, "");
  const target = path.join(PUBLIC_DIR, clean);
  if (!target.startsWith(PUBLIC_DIR)) return null;
  return target;
}

function serveStatic(req, res, urlPath) {
  if (req.method !== "GET") return false;
  const filePath = safeStaticPath(urlPath);
  if (!filePath) return false;
  if (!fs.existsSync(filePath)) return false;
  const stat = fs.statSync(filePath);
  if (!stat.isFile()) return false;
  const data = fs.readFileSync(filePath);
  res.writeHead(200, {
    "Content-Type": contentTypeFor(filePath),
    "Content-Length": data.length,
    "Cache-Control": "no-store",
  });
  res.end(data);
  return true;
}

function loadMenuItems() {
  try {
    const raw = fs.readFileSync(MENU_FILE, "utf8");
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch (_err) {
    return [];
  }
}

function parseJsonBody(req) {
  return new Promise((resolve, reject) => {
    let data = "";
    req.on("data", (chunk) => {
      data += chunk;
      if (data.length > 2_000_000) {
        reject(new Error("payload too large"));
        req.destroy();
      }
    });
    req.on("end", () => {
      if (!data) {
        resolve({});
        return;
      }
      try {
        resolve(JSON.parse(data));
      } catch (err) {
        reject(err);
      }
    });
    req.on("error", reject);
  });
}

function normalizeCatalog(input) {
  const catalog = input && typeof input === "object" ? input : {};
  const clusters = Array.isArray(catalog.clusters)
    ? catalog.clusters
        .map((c) => ({ id: Number(c.id), title: String(c.title || `cluster-${c.id}`) }))
        .filter((c) => Number.isInteger(c.id))
    : [];
  const subcasinos = Array.isArray(catalog.subcasinos)
    ? catalog.subcasinos
        .map((s) => ({
          id: Number(s.id),
          title: String(s.title || `subcasino-${s.id}`),
          clusterId: Number(s.clusterId),
        }))
        .filter((s) => Number.isInteger(s.id) && Number.isInteger(s.clusterId))
    : [];
  const banks = Array.isArray(catalog.banks)
    ? catalog.banks
        .map((b) => ({
          id: Number(b.id),
          title: String(b.title || `bank-${b.id}`),
          clusterId: Number(b.clusterId),
          subcasinoId: Number(b.subcasinoId),
        }))
        .filter(
          (b) =>
            Number.isInteger(b.id) &&
            Number.isInteger(b.clusterId) &&
            Number.isInteger(b.subcasinoId)
        )
    : [];

  if (!clusters.length) return DEFAULT_CATALOG;
  return {
    clusters,
    subcasinos: subcasinos.length ? subcasinos : DEFAULT_CATALOG.subcasinos,
    banks: banks.length ? banks : DEFAULT_CATALOG.banks,
  };
}

function buildDefaultRoles() {
  const now = nowIso();
  return [
    {
      id: 1,
      roleName: "SUPER_ADMIN",
      description: "Full access role",
      isSystem: true,
      editable: false,
      isNonRestricted: true,
      permissions: DEFAULT_PERMISSIONS.map((p) => p.id),
      clusterIds: [],
      subcasinoIds: [],
      bankIds: [],
      creationDate: now,
      lastChangeDate: now,
    },
    {
      id: 2,
      roleName: "SUPPORT",
      description: "Read-only support role",
      isSystem: false,
      editable: true,
      isNonRestricted: true,
      permissions: SUPPORT_PERMISSION_IDS.slice(),
      clusterIds: [],
      subcasinoIds: [],
      bankIds: [],
      creationDate: now,
      lastChangeDate: now,
    },
    {
      id: 3,
      roleName: "USER_MANAGER",
      description: "User and role administration",
      isSystem: false,
      editable: true,
      isNonRestricted: true,
      permissions: USER_MANAGER_PERMISSION_IDS.slice(),
      clusterIds: [],
      subcasinoIds: [],
      bankIds: [],
      creationDate: now,
      lastChangeDate: now,
    },
  ];
}

function expectedPermissionIdsForRole(roleName) {
  const upper = String(roleName || "").trim().toUpperCase();
  if (upper === "SUPER_ADMIN") {
    return DEFAULT_PERMISSIONS.map((p) => p.id);
  }
  if (upper === "SUPPORT") {
    return SUPPORT_PERMISSION_IDS.slice();
  }
  if (upper === "USER_MANAGER") {
    return USER_MANAGER_PERMISSION_IDS.slice();
  }
  return null;
}

function roleIdByName(roles, roleName) {
  const upper = String(roleName || "").trim().toUpperCase();
  const found = (roles || []).find(
    (r) => String(r.roleName || "").trim().toUpperCase() === upper
  );
  return found ? found.id : null;
}

function normalizeRoles(input) {
  const roles = Array.isArray(input) ? input : [];
  if (!roles.length) return buildDefaultRoles();
  return roles
    .map((r) => ({
      id: Number(r.id),
      roleName: String(r.roleName || "").trim(),
      description: r.description ? String(r.description) : "",
      isSystem: !!r.isSystem,
      editable: r.editable !== false,
      isNonRestricted: r.isNonRestricted !== false,
      permissions: uniqIntArray(r.permissions),
      clusterIds: uniqIntArray(r.clusterIds),
      subcasinoIds: uniqIntArray(r.subcasinoIds),
      bankIds: uniqIntArray(r.bankIds),
      creationDate: r.creationDate || nowIso(),
      lastChangeDate: r.lastChangeDate || nowIso(),
    }))
    .map((r) => {
      const expected = expectedPermissionIdsForRole(r.roleName);
      if (!expected) return r;
      const out = Object.assign({}, r, {
        permissions: expected,
      });
      if (String(r.roleName).toUpperCase() === "SUPER_ADMIN") {
        out.isSystem = true;
        out.editable = false;
      }
      return out;
    })
    .filter((r) => Number.isInteger(r.id) && r.roleName.length > 0);
}

function roleNameById(roles) {
  const out = new Map();
  for (const r of roles) out.set(r.id, r.roleName);
  return out;
}

function normalizeUserRecord(u, roles) {
  const roleMap = roleNameById(roles);
  const username = String(u.username || u.login || "").trim();
  const roleIds = uniqIntArray(u.roleIds);
  const roleSet = Array.isArray(u.roleSet) ? u.roleSet.map(String) : [];
  let normalizedRoleIds = roleIds;

  if (!normalizedRoleIds.length && roleSet.length) {
    const byName = new Map();
    for (const r of roles) byName.set(String(r.roleName).toLowerCase(), r.id);
    normalizedRoleIds = roleSet
      .map((n) => byName.get(String(n).toLowerCase()))
      .filter((id) => Number.isInteger(id));
  }
  if (!normalizedRoleIds.length) {
    if (username.toLowerCase() === "root") {
      const superAdminRoleId = roleIdByName(roles, "SUPER_ADMIN");
      normalizedRoleIds = superAdminRoleId ? [superAdminRoleId] : [];
    } else {
      const supportRoleId = roleIdByName(roles, "SUPPORT");
      normalizedRoleIds = supportRoleId ? [supportRoleId] : [];
    }
  }

  const lastLoginAt = u.lastLoginAt || null;
  const status = String(u.status || "ACTIVE").toUpperCase();
  const userLevel = String(u.userLevel || (u.isGeneral ? "GENERAL" : "SPECIFIC")).toUpperCase();

  return {
    username,
    email: String(u.email || ""),
    comment: String(u.comment || ""),
    passwordHash: String(u.passwordHash || ""),
    passwordHistory: Array.isArray(u.passwordHistory) ? u.passwordHistory.map(String) : [],
    roleIds: normalizedRoleIds,
    roleSet: normalizedRoleIds.map((id) => roleMap.get(id) || `ROLE_${id}`),
    status: status === "DISABLED" ? "DISABLED" : "ACTIVE",
    userLevel: userLevel === "GENERAL" ? "GENERAL" : "SPECIFIC",
    includeFutureBanks: !!u.includeFutureBanks,
    clusterIds: uniqIntArray(u.clusterIds),
    subcasinoIds: uniqIntArray(u.subcasinoIds),
    bankIds: uniqIntArray(u.bankIds),
    twoFactorEnabled: !!u.twoFactorEnabled,
    twoFactorStatus: u.twoFactorStatus ? String(u.twoFactorStatus) : null,
    mustChangePassword: !!u.mustChangePassword,
    failedAttempts: Number.isInteger(u.failedAttempts) ? u.failedAttempts : 0,
    failedAt: Array.isArray(u.failedAt) ? u.failedAt.map(String) : [],
    lockedUntil: u.lockedUntil || null,
    createdAt: u.createdAt || nowIso(),
    updatedAt: u.updatedAt || nowIso(),
    lastLoginAt,
    lastLoginIp: u.lastLoginIp || null,
    loginSessions: Array.isArray(u.loginSessions) ? u.loginSessions : [],
    loginIpHistory: Array.isArray(u.loginIpHistory) ? u.loginIpHistory : [],
    deletedAt: u.deletedAt || null,
  };
}

function normalizeCoreDb(raw) {
  const src = raw && typeof raw === "object" ? raw : { users: [] };
  const roles = normalizeRoles(src.roles);
  const users = (Array.isArray(src.users) ? src.users : [])
    .map((u) => normalizeUserRecord(u, roles))
    .filter((u) => u.username.length > 0)
    .filter((u) => !u.deletedAt);
  const catalog = normalizeCatalog(src.catalog);
  const audit = Array.isArray(src.audit) ? src.audit : [];
  return {
    version: 3,
    users,
    roles,
    permissions: DEFAULT_PERMISSIONS,
    catalog,
    audit,
    createdAt: src.createdAt || nowIso(),
  };
}

function loadCoreDb() {
  return normalizeCoreDb(loadCoreDbRaw());
}

function appendAudit(db, entry) {
  const row = {
    id: newToken(8),
    at: nowIso(),
    actor: entry.actor || "system",
    action: entry.action || "UNKNOWN",
    objectType: entry.objectType || "Unknown",
    objectId: entry.objectId || null,
    objectName: entry.objectName || null,
    details: entry.details || {},
  };
  db.audit.unshift(row);
  db.audit = db.audit.slice(0, 1000);
}

function getUserByUsername(db, username) {
  return db.users.find((u) => u.username === username) || null;
}

function getUserByUsernameInsensitive(db, username) {
  const target = String(username || "").toLowerCase();
  return db.users.find((u) => u.username.toLowerCase() === target) || null;
}

function findRoleById(db, roleId) {
  const id = Number(roleId);
  return db.roles.find((r) => r.id === id) || null;
}

function findRoleByName(db, roleName) {
  const target = String(roleName || "").trim().toLowerCase();
  return db.roles.find((r) => r.roleName.toLowerCase() === target) || null;
}

function roleNamesForUser(db, user) {
  return user.roleIds
    .map((id) => {
      const role = findRoleById(db, id);
      return role ? role.roleName : null;
    })
    .filter(Boolean);
}

function isRootLogin(value) {
  return String(value || "").trim().toLowerCase() === "root";
}

function permissionSetForUser(db, user) {
  const out = new Set();
  for (const roleId of user.roleIds || []) {
    const role = findRoleById(db, roleId);
    if (!role) continue;
    for (const p of role.permissions || []) {
      out.add(Number(p));
    }
  }
  return out;
}

function hasActiveSession(user) {
  const sessions = Array.isArray(user.loginSessions) ? user.loginSessions : [];
  const now = Date.now();
  return sessions.some((s) => s.active && new Date(s.expiresAt).getTime() > now);
}

function addIpHistory(user, ip) {
  const now = nowIso();
  const history = Array.isArray(user.loginIpHistory) ? user.loginIpHistory : [];
  const found = history.find((x) => x.ip === ip);
  if (found) {
    found.lastSeenAt = now;
    found.count = Number(found.count || 0) + 1;
  } else {
    history.unshift({ ip, lastSeenAt: now, count: 1 });
  }
  user.loginIpHistory = history.slice(0, 30);
}

function deactivateAllSessionsForUser(user) {
  const sessions = Array.isArray(user.loginSessions) ? user.loginSessions : [];
  for (const session of sessions) {
    session.active = false;
  }
  user.loginSessions = sessions;
}

function revokeTokenMapsForUser(username) {
  for (const [token, session] of accessTokens.entries()) {
    if (session.username === username) accessTokens.delete(token);
  }
  for (const [hash, session] of refreshTokens.entries()) {
    if (session.username === username) {
      session.revoked = true;
      refreshTokens.set(hash, session);
    }
  }
}

function revokeAllTokensForUser(username) {
  revokeTokenMapsForUser(username);
  const db = loadCoreDb();
  const user = getUserByUsername(db, username);
  if (user) {
    deactivateAllSessionsForUser(user);
    user.updatedAt = nowIso();
    saveCoreDb(db);
  }
}

function checkPasswordPolicy(newPassword) {
  const errors = [];
  if (typeof newPassword !== "string") {
    errors.push("password must be string");
    return errors;
  }
  if (newPassword.length < 12) errors.push("minimum length is 12");
  if (!/[A-Z]/.test(newPassword)) errors.push("missing uppercase letter");
  if (!/[a-z]/.test(newPassword)) errors.push("missing lowercase letter");
  if (!/[0-9]/.test(newPassword)) errors.push("missing number");
  if (!/[^A-Za-z0-9]/.test(newPassword)) errors.push("missing special character");
  return errors;
}

function authenticateAccess(req) {
  const header = req.headers.authorization || "";
  if (!header.startsWith("Bearer ")) return null;
  const token = header.slice("Bearer ".length).trim();
  const session = accessTokens.get(token);
  if (!session) return null;
  if (session.expiresAt <= Date.now()) {
    accessTokens.delete(token);
    return null;
  }
  return { token, username: session.username, sessionId: session.sessionId || null };
}

function parseCqlTable(text) {
  const lines = String(text || "")
    .split("\n")
    .map((l) => l.trim())
    .filter(Boolean);
  const tableLines = [];
  for (const line of lines) {
    if (line.startsWith("(") && line.endsWith("rows)")) continue;
    if (line.includes("|")) tableLines.push(line);
  }
  if (tableLines.length < 2) return [];
  const headers = tableLines[0].split("|").map((x) => x.trim());
  const rows = [];
  for (let i = 1; i < tableLines.length; i += 1) {
    const line = tableLines[i];
    if (/^[\-+\s]+$/.test(line)) continue;
    const values = line.split("|").map((x) => x.trim());
    if (values.length !== headers.length) continue;
    const row = {};
    for (let c = 0; c < headers.length; c += 1) {
      row[headers[c]] = values[c];
    }
    rows.push(row);
  }
  return rows;
}

function runCqlRaw(query) {
  const args = ["exec", "-i", CASSANDRA_CONTAINER, "cqlsh", "-e", query];
  const result = spawnSync("docker", args, {
    encoding: "utf8",
    timeout: 20_000,
    maxBuffer: 10 * 1024 * 1024,
  });
  if (result.error) {
    throw new Error(`cqlsh spawn error: ${result.error.message}`);
  }
  if (result.status !== 0) {
    throw new Error(result.stderr || result.stdout || "cqlsh failed");
  }
  return String(result.stdout || "");
}

function runCql(query) {
  return parseCqlTable(runCqlRaw(query));
}

function runGsCurl(pathname, params) {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params || {})) {
    if (value == null || value === "") continue;
    query.set(key, String(value));
  }

  const url = `http://localhost:8080${pathname}?${query.toString()}`;
  const args = ["exec", "-i", GS_CONTAINER, "curl", "-sS", "--max-time", "20", url];
  const result = spawnSync("docker", args, {
    encoding: "utf8",
    timeout: 25_000,
    maxBuffer: 10 * 1024 * 1024,
  });
  if (result.error) {
    throw new Error(`gs curl spawn error: ${result.error.message}`);
  }
  if (result.status !== 0) {
    throw new Error(result.stderr || result.stdout || "gs curl failed");
  }
  return String(result.stdout || "").trim();
}

function cqlQuote(value) {
  return String(value || "").replace(/'/g, "''");
}

function md5Hex(value) {
  return crypto.createHash("md5").update(String(value || ""), "utf8").digest("hex");
}

function parseJsonObject(value) {
  if (typeof value !== "string" || !value.trim()) return {};
  try {
    const parsed = JSON.parse(value);
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch (_err) {
    return {};
  }
}

function parseBooleanText(value, fallback) {
  if (typeof value === "boolean") return value;
  const v = String(value || "")
    .trim()
    .toLowerCase();
  if (["true", "1", "yes", "y"].includes(v)) return true;
  if (["false", "0", "no", "n"].includes(v)) return false;
  return fallback;
}

function parseTimeParamMs(value) {
  if (value == null || value === "") return null;
  const asNum = Number(value);
  if (Number.isFinite(asNum) && asNum > 0) return asNum;
  const asDate = Date.parse(String(value));
  return Number.isFinite(asDate) ? asDate : null;
}

function displayFromEpochMs(value) {
  const n = Number(value);
  if (!Number.isFinite(n) || n <= 0) return "";
  return formatDisplayDate(new Date(n).toISOString());
}

function centsToAmount(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return 0;
  return Number((n / 100).toFixed(2));
}

function accountStatusLabel(locked) {
  return locked ? "Locked by Admin" : "Active";
}

function platformIdByName(platformName) {
  const v = String(platformName || "")
    .trim()
    .toUpperCase();
  if (v === "FLASH") return 1;
  if (v === "WIN32" || v === "WIN 32") return 2;
  if (v === "AIR") return 3;
  if (v === "MOBILE") return 4;
  if (v === "ANDROID") return 5;
  if (v === "IOS") return 6;
  if (v === "WINDOWS PHONE") return 7;
  if (v === "VAULT") return 8;
  return -1;
}

function parseNumber(value, fallback) {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

function formatBonusApiDate(epochMs) {
  const d = new Date(Number(epochMs || 0));
  if (!Number.isFinite(d.getTime())) return "";
  const dd = String(d.getUTCDate()).padStart(2, "0");
  const mm = String(d.getUTCMonth() + 1).padStart(2, "0");
  const yyyy = d.getUTCFullYear();
  return `${dd}.${mm}.${yyyy}`;
}

function formatBonusApiDateTime(epochMs) {
  const d = new Date(Number(epochMs || 0));
  if (!Number.isFinite(d.getTime())) return "";
  const dd = String(d.getUTCDate()).padStart(2, "0");
  const mm = String(d.getUTCMonth() + 1).padStart(2, "0");
  const yyyy = d.getUTCFullYear();
  const hh = String(d.getUTCHours()).padStart(2, "0");
  const min = String(d.getUTCMinutes()).padStart(2, "0");
  const sec = String(d.getUTCSeconds()).padStart(2, "0");
  return `${dd}.${mm}.${yyyy} ${hh}:${min}:${sec}`;
}

function parseBonusApiXml(xmlText) {
  const xml = String(xmlText || "");
  const responseBodyMatch = xml.match(/<RESPONSE>\s*([\s\S]*?)\s*<\/RESPONSE>/i);
  const responseBody = responseBodyMatch ? String(responseBodyMatch[1] || "") : xml;
  const pick = (tag) => {
    const m = responseBody.match(new RegExp(`<${tag}>\\s*([\\s\\S]*?)\\s*<\\/${tag}>`, "i"));
    return m ? String(m[1] || "").trim() : "";
  };
  return {
    raw: xml,
    result: pick("RESULT").toUpperCase(),
    bonusId: pick("BONUSID"),
    code: pick("CODE"),
    description: pick("DESCRIPTION"),
  };
}

function boolToYesNo(value) {
  return value ? "YES" : "NO";
}

function dayBucketUtc(ms) {
  const d = new Date(ms);
  const yyyy = d.getUTCFullYear();
  const mm = String(d.getUTCMonth() + 1).padStart(2, "0");
  const dd = String(d.getUTCDate()).padStart(2, "0");
  return Number(`${yyyy}${mm}${dd}`);
}

function bonusStatusFromRow(row) {
  const statusText = String((row && row.status) || "").toUpperCase();
  if (statusText) return statusText;
  const statusId = Number(row && row.statusId);
  if (statusId === 2) return "RELEASED";
  if (statusId === 3) return "CANCELLED";
  if (statusId === 4) return "EXPIRED";
  if (statusId === 5) return "LOST";
  return "ACTIVE";
}

function bonusTypeTitle(id) {
  const n = Number(id);
  if (n === 0) return "Deposits";
  if (n === 1) return "Slots deposits";
  if (n === 2) return "Loss";
  if (n === 3) return "Prize";
  if (n === 4) return "Promo";
  if (n === 5) return "Special";
  return "Unknown";
}

function frbBetTypeTitle(id) {
  return Number(id) === 2 ? "Fixed Bet for Single Game" : "Default Bet";
}

function safeParseJsonCell(value, fallback) {
  const parsed = parseJsonObject(value);
  return parsed && typeof parsed === "object" ? parsed : fallback;
}

function readAccountInfo(accountId, cache) {
  const key = Number(accountId);
  if (!Number.isInteger(key) || key <= 0) return null;
  if (cache.has(key)) return cache.get(key);
  const rows = runCql(`PAGING OFF; SELECT key, jcn FROM rcasinoscks.accountcf WHERE key=${key};`);
  if (!rows.length) {
    cache.set(key, null);
    return null;
  }
  const info = parseJsonObject(rows[0].jcn);
  cache.set(key, info);
  return info;
}

function readBankInfo(bankId, cache) {
  const key = Number(bankId);
  if (!Number.isInteger(key) || key <= 0) return null;
  if (cache.has(key)) return cache.get(key);

  const fallback = {
    id: key,
    subCasinoId: null,
    title: `bank-${key}`,
    externalBankId: String(key),
    bonusPassKey: "",
    bonusHashEnabled: false,
  };
  const raw = runCqlRaw(`PAGING OFF; SELECT key, jcn FROM rcasinoscks.bankinfocf WHERE key=${key};`);
  const rowLine = String(raw)
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.includes("|"))
    .find((line) => {
      if (/^[\-+\s]+$/.test(line)) return false;
      const idx = line.indexOf("|");
      if (idx <= 0) return false;
      const lhs = line.slice(0, idx).trim();
      return Number(lhs) === key;
    });
  if (!rowLine) {
    cache.set(key, fallback);
    return fallback;
  }

  const separatorIdx = rowLine.indexOf("|");
  const jcnText = separatorIdx >= 0 ? rowLine.slice(separatorIdx + 1).trim() : "";
  const jcn = parseJsonObject(jcnText);
  const properties = jcn && typeof jcn.properties === "object" ? jcn.properties : {};
  const bonusPassKey =
    String(properties.BONUS_PASS_KEY || properties.bonusPassKey || "").trim() ||
    String(jcn.bonusPassKey || "").trim();
  const hashRaw = String(
    properties.BONUS_IS_HASH_VALUE != null ? properties.BONUS_IS_HASH_VALUE : jcn.isHashValueEnable
  )
    .trim()
    .toLowerCase();
  const bonusHashEnabled = ["true", "1", "yes", "y"].includes(hashRaw);
  const out = {
    id: key,
    subCasinoId: Number.isInteger(Number(jcn.subCasinoId)) ? Number(jcn.subCasinoId) : null,
    externalBankId: String(jcn.externalBankId || "").trim() || String(key),
    bonusPassKey,
    bonusHashEnabled,
    title:
      String(jcn.externalBankIdDescription || "").trim() ||
      String(jcn.externalBankId || "").trim() ||
      `bank-${key}`,
  };
  cache.set(key, out);
  return out;
}

function readSubcasinoInfo(subcasinoId, cache) {
  const key = Number(subcasinoId);
  if (!Number.isInteger(key) || key <= 0) return null;
  if (cache.has(key)) return cache.get(key);

  const fallback = { id: key, title: `subcasino-${key}` };
  const rows = runCql(`PAGING OFF; SELECT key, jcn FROM rcasinoscks.subcasinocf WHERE key=${key};`);
  if (!rows.length) {
    cache.set(key, fallback);
    return fallback;
  }

  const jcn = parseJsonObject(rows[0].jcn);
  const out = {
    id: key,
    title: String(jcn.name || "").trim() || fallback.title,
  };
  cache.set(key, out);
  return out;
}

function readGameSessionsByExtId(extId) {
  if (!validateExtId(extId)) return [];
  const rows = runCql(
    `PAGING OFF; SELECT gsid, accid, extid, lbid, st, et, gameid, bets, income, payout, bonusbet, bonuswin, unjsw, contribjp_json, nb, stbalance, enbbalance, cltype, curr FROM rcasinoks.gamesessioncf WHERE extid='${cqlQuote(
      extId
    )}' LIMIT 2000;`
  );

  return rows.map((row) => ({
    gsid: Number(row.gsid),
    accountId: Number(row.accid),
    extId: String(row.extid || ""),
    bankId: Number(row.lbid),
    startTimeMs: Number(row.st),
    endTimeMs: Number(row.et),
    gameId: Number(row.gameid),
    betsCount: Number(row.bets),
    income: Number(row.income),
    payout: Number(row.payout),
    bonusBet: Number(row.bonusbet),
    bonusWin: Number(row.bonuswin),
    externalUnjWin: Number(row.unjsw),
    jackpotContributions: Number(
      safeParseJsonCell(row.contribjp_json, {}).totalContribution || 0
    ),
    negativeBet: Number(row.nb),
    startBalance: Number(row.stbalance),
    endBalance: Number(row.enbbalance),
    platform: String(row.cltype || "").trim() || "",
    currencyCode: String(row.curr || "").trim() || "",
  }));
}

function aggregateSessionStats(sessions) {
  const safe = Array.isArray(sessions) ? sessions : [];
  const sessionCount = safe.length;
  const betsCount = safe.reduce((sum, s) => sum + (Number.isFinite(s.betsCount) ? s.betsCount : 0), 0);
  const incomeMinor = safe.reduce((sum, s) => sum + (Number.isFinite(s.income) ? s.income : 0), 0);
  const payoutMinor = safe.reduce((sum, s) => sum + (Number.isFinite(s.payout) ? s.payout : 0), 0);
  const bonusBetMinor = safe.reduce(
    (sum, s) => sum + (Number.isFinite(s.bonusBet) ? s.bonusBet : 0),
    0
  );
  const bonusWinMinor = safe.reduce(
    (sum, s) => sum + (Number.isFinite(s.bonusWin) ? s.bonusWin : 0),
    0
  );

  let last = null;
  for (const s of safe) {
    const ts = Number(s.endTimeMs);
    if (!Number.isFinite(ts) || ts <= 0) continue;
    if (!last || ts > last.endTimeMs) last = s;
  }

  return {
    sessionCount,
    betsCount,
    incomeMinor,
    payoutMinor,
    bonusBetMinor,
    bonusWinMinor,
    income: centsToAmount(incomeMinor),
    payout: centsToAmount(payoutMinor),
    bonusBet: centsToAmount(bonusBetMinor),
    bonusWin: centsToAmount(bonusWinMinor),
    gameRevenue: centsToAmount(incomeMinor - payoutMinor),
    roundsCount: betsCount,
    lastSession: last,
  };
}

function gameInfoByBank(bankId) {
  const rows = runCql(
    `PAGING OFF; SELECT key, jcn FROM rcasinoscks.gameinfocf WHERE bankidx='${cqlQuote(
      String(bankId)
    )}' LIMIT 10000;`
  );
  const byId = new Map();
  for (const row of rows) {
    const info = safeParseJsonCell(row.jcn, null);
    if (!info) continue;
    const id = Number(info.id);
    if (!Number.isInteger(id) || id <= 0) continue;
    byId.set(id, {
      title: String(info.name || `Game ${id}`),
      gameTypeId: Number(info.gameTypeId),
      groupId: Number(info.groupId),
      isJackpot: parseBooleanText(info.hasJackpot, false),
    });
  }
  return byId;
}

function frbAllGamesByBank(bankId) {
  const byId = gameInfoByBank(bankId);
  return Array.from(byId.keys())
    .map((v) => Number(v))
    .filter((v) => Number.isInteger(v) && v > 0)
    .sort((a, b) => a - b);
}

function readBonusHistoryRows(accountId) {
  const acc = Number(accountId);
  if (!Number.isInteger(acc) || acc <= 0) return [];
  const rows = runCql(
    `PAGING OFF; SELECT accid, awardtime, bonusid, extbonusid, jcn, statusid FROM rcasinoks.bonusarchcf WHERE accid=${acc} LIMIT 500;`
  );
  return rows
    .map((row) => {
      const jcn = safeParseJsonCell(row.jcn, {});
      const awardTimeMs = Number(row.awardtime || jcn.awardTimeMs || 0);
      const expirationMs = Number(jcn.expirationTimeMs || jcn.expirationDate || 0);
      const status = bonusStatusFromRow({
        status: jcn.status,
        statusId: Number(row.statusid),
      });
      return {
        bonusId: Number(row.bonusid || jcn.bonusId || 0),
        externalBonusId: String(row.extbonusid || jcn.externalBonusId || ""),
        type: bonusTypeTitle(jcn.bonusType),
        bonusType: Number(jcn.bonusType),
        amount: parseNumber(jcn.amount, centsToAmount(jcn.amountMinor)),
        rolloverMultiplier: parseNumber(jcn.rolloverMultiplier, 0),
        rollover: parseNumber(jcn.rollover, 0),
        maxWinCapMultiplier: parseNumber(jcn.maxWinCapMultiplier, 0),
        maxWinCap: parseNumber(jcn.maxWinCap, 0),
        releasedType: parseBooleanText(jcn.releasedType, true) ? "AUTO" : "MANUAL",
        gameLimitType: Number.isInteger(Number(jcn.gameLimitType)) ? Number(jcn.gameLimitType) : 0,
        gameList: Array.isArray(jcn.gameList) ? jcn.gameList : [],
        description: String(jcn.description || ""),
        status,
        awardedAtMs: awardTimeMs,
        awardedAt: displayFromEpochMs(awardTimeMs),
        startDateMs: Number(jcn.startTimeMs || 0),
        startDate: displayFromEpochMs(Number(jcn.startTimeMs || 0)),
        expirationDateMs: expirationMs,
        expirationDate: displayFromEpochMs(expirationMs),
        createdBy: String(jcn.awardedBy || ""),
      };
    })
    .sort((a, b) => b.awardedAtMs - a.awardedAtMs);
}

function readFrbonusHistoryRows(accountId) {
  const acc = Number(accountId);
  if (!Number.isInteger(acc) || acc <= 0) return [];
  const archRows = runCql(
    `PAGING OFF; SELECT accid, awardtime, frbonusid, extfrbonusid, jcn, statusid FROM rcasinoks.frbonusarchcf WHERE accid=${acc} LIMIT 500;`
  );
  const out = [];
  const seen = new Set();

  const mapArchRow = (row) => {
    const jcn = safeParseJsonCell(row.jcn, {});
    const awardTimeMs = Number(row.awardtime || jcn.awardTimeMs || 0);
    const expirationMs = Number(jcn.expirationTimeMs || 0);
    const rounds = parseNumber(jcn.rounds, 0);
    const roundsLeft = parseNumber(jcn.roundsLeft, rounds);
    const status = bonusStatusFromRow({
      status: jcn.status,
      statusId: Number(row.statusid),
    });
    return {
      frbonusId: Number(row.frbonusid || jcn.frbonusId || 0),
      externalFrbonusId: String(row.extfrbonusid || jcn.externalFrbonusId || ""),
      rounds,
      roundsLeft,
      roundsPlayed: Math.max(0, rounds - roundsLeft),
      frChips: parseNumber(jcn.frChips, 0),
      frbBetType: Number(jcn.frbBetType || 1),
      frbBetTypeTitle: frbBetTypeTitle(jcn.frbBetType),
      gameLimitType: Number.isInteger(Number(jcn.gameLimitType)) ? Number(jcn.gameLimitType) : 0,
      gameList: Array.isArray(jcn.gameList) ? jcn.gameList : [],
      gameId: Number(jcn.gameId || 0),
      maxWinCap: parseNumber(jcn.maxWinCap, 0),
      awardDurationDays: parseNumber(jcn.awardDurationDays, 0),
      description: String(jcn.description || ""),
      status,
      awardedAtMs: awardTimeMs,
      awardedAt: displayFromEpochMs(awardTimeMs),
      startDateMs: Number(jcn.startTimeMs || 0),
      startDate: displayFromEpochMs(Number(jcn.startTimeMs || 0)),
      expirationDateMs: expirationMs,
      expirationDate: displayFromEpochMs(expirationMs),
      createdBy: String(jcn.awardedBy || ""),
    };
  };

  const parseGameIds = (raw) => {
    return uniqIntArray(
      String(raw || "")
        .split(/[|,]/)
        .map((x) => Number(x.trim()))
        .filter((n) => Number.isInteger(n) && n > 0)
    );
  };

  const mapLiveRow = (frbonusRow) => {
    const jcn = safeParseJsonCell(frbonusRow.jcn, {});
    const frbonusId = Number(frbonusRow.frbonusid || jcn.id || jcn.frbonusId || 0);
    const rounds = parseNumber(jcn.rounds, 0);
    const roundsLeft = parseNumber(jcn.roundsLeft, rounds);
    const gameList = Array.isArray(jcn.gameList)
      ? uniqIntArray(jcn.gameList)
      : parseGameIds(jcn.gameIds);
    const gameId = Number(jcn.gameId || gameList[0] || 0);
    const awardTimeMs = Number(jcn.timeAwarded || jcn.awardTimeMs || jcn.lastUpdateDate || 0);
    const startDateMs = Number(jcn.startDate || jcn.startTimeMs || 0);
    const expirationMs = Number(jcn.expirationDate || jcn.expirationTimeMs || frbonusRow.expdate || 0);
    return {
      frbonusId,
      externalFrbonusId: String(frbonusRow.extfrbonusid || jcn.externalFrbonusId || `${jcn.bankId || ""}+${jcn.extId || ""}`),
      rounds,
      roundsLeft,
      roundsPlayed: Math.max(0, rounds - roundsLeft),
      frChips: parseNumber(jcn.frChips, parseNumber(jcn.frbTableRoundChips, parseNumber(jcn.coinValue, 0))),
      frbBetType: Number(jcn.frbBetType || 1),
      frbBetTypeTitle: frbBetTypeTitle(jcn.frbBetType),
      gameLimitType: Number.isInteger(Number(jcn.gameLimitType)) ? Number(jcn.gameLimitType) : 0,
      gameList,
      gameId,
      maxWinCap: parseNumber(jcn.maxWinCap, parseNumber(jcn.maxWinLimit, 0)),
      awardDurationDays: parseNumber(jcn.awardDurationDays, parseNumber(jcn.freeRoundValidity, 0)),
      description: String(jcn.description || ""),
      status: String(jcn.status || "ACTIVE").toUpperCase(),
      awardedAtMs: awardTimeMs,
      awardedAt: displayFromEpochMs(awardTimeMs),
      startDateMs,
      startDate: displayFromEpochMs(startDateMs),
      expirationDateMs: expirationMs,
      expirationDate: displayFromEpochMs(expirationMs),
      createdBy: String(jcn.awardedBy || ""),
    };
  };

  for (const row of archRows) {
    const mapped = mapArchRow(row);
    if (!Number.isInteger(mapped.frbonusId) || mapped.frbonusId <= 0) continue;
    if (seen.has(mapped.frbonusId)) continue;
    seen.add(mapped.frbonusId);
    out.push(mapped);
  }

  const liveIdsRows = runCql(
    `PAGING OFF; SELECT accid, frbonusid FROM rcasinoscks.frbonuscf_acc WHERE accid=${acc} LIMIT 1000;`
  );
  for (const idRow of liveIdsRows) {
    const frbonusId = Number(idRow.frbonusid);
    if (!Number.isInteger(frbonusId) || frbonusId <= 0) continue;
    if (seen.has(frbonusId)) continue;
    const frbonusRows = runCql(
      `PAGING OFF; SELECT frbonusid, expdate, extfrbonusid, jcn FROM rcasinoscks.frbonuscf WHERE frbonusid=${frbonusId};`
    );
    if (!frbonusRows.length) continue;
    const mapped = mapLiveRow(frbonusRows[0]);
    if (!Number.isInteger(mapped.frbonusId) || mapped.frbonusId <= 0) continue;
    if (seen.has(mapped.frbonusId)) continue;
    seen.add(mapped.frbonusId);
    out.push(mapped);
  }

  return out
    .map((row) => {
      const fallbackNow = Date.now();
      const safeAwardedAtMs = Number.isFinite(row.awardedAtMs) && row.awardedAtMs > 0
        ? row.awardedAtMs
        : fallbackNow;
      return Object.assign({}, row, {
        awardedAtMs: safeAwardedAtMs,
        awardedAt: displayFromEpochMs(safeAwardedAtMs),
      });
    })
    .sort((a, b) => b.awardedAtMs - a.awardedAtMs);
}

function summarizeBonusRows(rows) {
  const safe = Array.isArray(rows) ? rows : [];
  const now = Date.now();
  let awardedAmount = 0;
  let activeAmount = 0;
  let releasedAmount = 0;
  let activeCount = 0;
  let releasedCount = 0;
  let cancelledCount = 0;
  let expiredCount = 0;
  let lostCount = 0;
  for (const row of safe) {
    const amount = parseNumber(row.amount, 0);
    awardedAmount += amount;
    const status = String(row.status || "").toUpperCase();
    if (status === "RELEASED") {
      releasedCount += 1;
      releasedAmount += amount;
      continue;
    }
    if (status === "CANCELLED") {
      cancelledCount += 1;
      continue;
    }
    if (status === "EXPIRED") {
      expiredCount += 1;
      continue;
    }
    if (status === "LOST") {
      lostCount += 1;
      continue;
    }
    if (row.expirationDateMs > 0 && row.expirationDateMs < now) {
      expiredCount += 1;
      continue;
    }
    activeCount += 1;
    activeAmount += amount;
  }
  return {
    awardedCount: safe.length,
    awardedAmount: Number(awardedAmount.toFixed(2)),
    activeCount,
    activeAmount: Number(activeAmount.toFixed(2)),
    releasedCount,
    releasedAmount: Number(releasedAmount.toFixed(2)),
    cancelledCount,
    expiredCount,
    lostCount,
  };
}

function summarizeFrbonusRows(rows) {
  const safe = Array.isArray(rows) ? rows : [];
  let activeCount = 0;
  let finishedCount = 0;
  let cancelledCount = 0;
  let roundsAwarded = 0;
  let roundsLeft = 0;
  let roundsPlayed = 0;
  let bets = 0;
  let wins = 0;
  for (const row of safe) {
    const status = String(row.status || "").toUpperCase();
    if (status === "CANCELLED") cancelledCount += 1;
    else if (status === "FINISHED" || status === "RELEASED") finishedCount += 1;
    else activeCount += 1;
    roundsAwarded += parseNumber(row.rounds, 0);
    roundsLeft += parseNumber(row.roundsLeft, 0);
    roundsPlayed += parseNumber(row.roundsPlayed, 0);
    bets += parseNumber(row.totalBet, 0);
    wins += parseNumber(row.totalWin, 0);
  }
  return {
    awardedCount: safe.length,
    activeCount,
    finishedCount,
    cancelledCount,
    roundsAwarded,
    roundsLeft,
    roundsPlayed,
    bets: Number(bets.toFixed(2)),
    wins: Number(wins.toFixed(2)),
  };
}

function buildPlayerGameInfoRows(payload, filters) {
  const sessions = Array.isArray(payload.gameRows) ? payload.gameRows.slice() : [];
  const gameMap = gameInfoByBank(payload.bankId);

  const dateFromMs = parseTimeParamMs(filters.dateFrom) || null;
  const dateToMs = parseTimeParamMs(filters.dateTo) || null;
  const platformFilter = toSafeInt(filters.platform, -1, -1, 99);
  const playerMode = toSafeInt(filters.playerMode, 0, 0, 3);
  const gameTypeFilter = toSafeInt(filters.gameType, -1, -1, 99);
  const isJackpotFilter = String(filters.isJackpot || "-1");
  const showBySessions = parseBooleanText(filters.showBySessions, false);

  const filtered = sessions.filter((s) => {
    const endMs = Number(s.endTimeMs || 0);
    if (dateFromMs && endMs < dateFromMs) return false;
    if (dateToMs && endMs > dateToMs) return false;
    if (platformFilter >= 0 && Number(s.platformId) !== platformFilter) return false;
    if (playerMode === 1 && Number(s.bonusBet || 0) <= 0 && Number(s.bonusWin || 0) <= 0) return false;
    if (playerMode === 2) return false;
    if (playerMode === 3) return false;
    if (gameTypeFilter >= 0) {
      const gi = gameMap.get(Number(s.gameId));
      if (!gi || Number(gi.gameTypeId) !== gameTypeFilter) return false;
    }
    if (isJackpotFilter !== "-1") {
      const want = parseBooleanText(isJackpotFilter, false);
      const gi = gameMap.get(Number(s.gameId));
      if (!!(gi && gi.isJackpot) !== want) return false;
    }
    return true;
  });

  const withMeta = filtered.map((s) => {
    const gi = gameMap.get(Number(s.gameId));
    return Object.assign({}, s, {
      gameTitle: gi ? gi.title : `Game ${s.gameId || "-"}`,
      groupName: gi ? `Group ${gi.groupId}` : "Group -",
    });
  });

  if (showBySessions) {
    const rows = withMeta
      .slice()
      .sort((a, b) => Number(b.endTimeMs || 0) - Number(a.endTimeMs || 0))
      .map((s, idx) => ({
        rowNumber: idx + 1,
        bankId: payload.bankId,
        gameId: s.gameId,
        gameTitle: s.gameTitle,
        startTime: displayFromEpochMs(s.startTimeMs),
        endTime: displayFromEpochMs(s.endTimeMs),
        income: centsToAmount(s.income),
        payout: centsToAmount(s.payout),
        gainLoss: centsToAmount(Number(s.income || 0) - Number(s.payout || 0)),
        jackpotWins: centsToAmount(0),
        externalUnjWin: centsToAmount(s.externalUnjWin),
        jackpotContributions: centsToAmount(s.jackpotContributions),
        unjContributions: centsToAmount(0),
        roundsCount: Number(s.betsCount || 0),
        negativeBet: centsToAmount(s.negativeBet),
        startBalance: centsToAmount(s.startBalance),
        endBalance: centsToAmount(s.endBalance),
        gameSessionId: s.sessionId,
        totalRecords: withMeta.length,
      }));
    return {
      mode: "sessions",
      rows,
    };
  }

  const grouped = new Map();
  for (const s of withMeta) {
    const key = `${s.gameId}:${s.gameTitle}`;
    const curr = grouped.get(key) || {
      gameId: s.gameId,
      gameTitle: s.gameTitle,
      groupName: s.groupName,
      sessionsCount: 0,
      totalIncomeMinor: 0,
      totalPayoutMinor: 0,
      betsCount: 0,
      jackpotContribMinor: 0,
      externalUnjWinMinor: 0,
      negativeBetMinor: 0,
      bonusBetMinor: 0,
      bonusWinMinor: 0,
    };
    curr.sessionsCount += 1;
    curr.totalIncomeMinor += Number(s.income || 0);
    curr.totalPayoutMinor += Number(s.payout || 0);
    curr.betsCount += Number(s.betsCount || 0);
    curr.jackpotContribMinor += Number(s.jackpotContributions || 0);
    curr.externalUnjWinMinor += Number(s.externalUnjWin || 0);
    curr.negativeBetMinor += Number(s.negativeBet || 0);
    curr.bonusBetMinor += Number(s.bonusBet || 0);
    curr.bonusWinMinor += Number(s.bonusWin || 0);
    grouped.set(key, curr);
  }

  const totalSessions = withMeta.length || 1;
  const totalIncomeMinor = Array.from(grouped.values()).reduce((sum, x) => sum + x.totalIncomeMinor, 0);
  const rows = Array.from(grouped.values())
    .sort((a, b) => b.sessionsCount - a.sessionsCount || a.gameTitle.localeCompare(b.gameTitle))
    .map((g) => ({
      gameId: g.gameId,
      gameTitle: g.gameTitle,
      groupName: g.groupName,
      sessionsCount: g.sessionsCount,
      sessionsCountPercent: Number(((g.sessionsCount / totalSessions) * 100).toFixed(2)),
      totalIncome: centsToAmount(g.totalIncomeMinor),
      avgBet: g.betsCount > 0 ? Number((centsToAmount(g.totalIncomeMinor) / g.betsCount).toFixed(2)) : 0,
      incomePercent: totalIncomeMinor > 0 ? Number(((g.totalIncomeMinor / totalIncomeMinor) * 100).toFixed(2)) : 0,
      jackpotContributions: centsToAmount(g.jackpotContribMinor),
      unjContributions: 0,
      totalPayout: centsToAmount(g.totalPayoutMinor),
      jackpotWins: 0,
      externalUnjWin: centsToAmount(g.externalUnjWinMinor),
      gainLoss: centsToAmount(g.totalIncomeMinor - g.totalPayoutMinor),
      gainLossPercent:
        g.totalIncomeMinor > 0
          ? Number((((g.totalIncomeMinor - g.totalPayoutMinor) / g.totalIncomeMinor) * 100).toFixed(2))
          : 0,
      negativeBet: centsToAmount(g.negativeBetMinor),
      bonusBet: centsToAmount(g.bonusBetMinor),
      bonusWin: centsToAmount(g.bonusWinMinor),
    }));
  return {
    mode: "games",
    rows,
  };
}

function resolvePlayerSearchBankIds(urlObj, db) {
  const fromList = parseCsvInts(urlObj.searchParams.get("bankList"));
  const single = toSafeInt(urlObj.searchParams.get("bankId"), -1, 1, 10_000_000);
  if (Number.isInteger(single) && single > 0 && !fromList.includes(single)) {
    fromList.push(single);
  }
  if (fromList.length) return fromList;
  const fromCatalog = uniqIntArray((db.catalog && db.catalog.banks || []).map((b) => b.id));
  return fromCatalog.length ? fromCatalog : [6274];
}

function parseLockedFilterValue(raw) {
  const value = String(raw || "").trim().toLowerCase();
  if (!value || value === "-1" || value === "all") return null;
  if (["true", "1", "locked"].includes(value)) return true;
  if (["false", "0", "active"].includes(value)) return false;
  return null;
}

function queryPlayerSearch(access, urlObj) {
  const db = access.db;
  const clusterId = toSafeInt(urlObj.searchParams.get("clusterId"), -1, 1, 10_000_000);
  const subcasinoFilter = parseCsvInts(urlObj.searchParams.get("subcasinoList"));
  const bankIds = resolvePlayerSearchBankIds(urlObj, db);
  const nickname = String(urlObj.searchParams.get("nickName") || "").trim();
  const accountIdFilter = toSafeInt(urlObj.searchParams.get("accountId"), -1, 1, Number.MAX_SAFE_INTEGER);
  const extId = String(urlObj.searchParams.get("extId") || "").trim();
  const fuzzySearch = parseBooleanText(urlObj.searchParams.get("fuzzySearch"), false);
  const regAfterMs = parseTimeParamMs(urlObj.searchParams.get("regAfterTime"));
  const regBeforeMs = parseTimeParamMs(urlObj.searchParams.get("regBeforeTime"));
  const accountLockedFilter = parseLockedFilterValue(urlObj.searchParams.get("accountStatus"));
  const mainPerPage = toSafeInt(
    urlObj.searchParams.get("mainPerPage") || urlObj.searchParams.get("limit"),
    20,
    1,
    200
  );

  const bankCatalogMap = new Map();
  for (const bank of (db.catalog && db.catalog.banks) || []) {
    bankCatalogMap.set(Number(bank.id), bank);
  }

  const accountCache = new Map();
  const bankCache = new Map();
  const subcasinoCache = new Map();

  const players = [];
  for (const bankId of bankIds) {
    const catalogBank = bankCatalogMap.get(Number(bankId)) || null;
    if (clusterId > 0 && catalogBank && Number(catalogBank.clusterId) !== clusterId) {
      continue;
    }
    if (
      subcasinoFilter.length &&
      catalogBank &&
      !subcasinoFilter.includes(Number(catalogBank.subcasinoId))
    ) {
      continue;
    }

    const extRows = runCql(
      `PAGING OFF; SELECT bankid, extid, accountid FROM rcasinoscks.accountcf_ext WHERE bankid=${Number(
        bankId
      )} LIMIT ${Math.max(mainPerPage * 6, 60)};`
    );

    for (const extRow of extRows) {
      const accountId = Number(extRow.accountid);
      if (!Number.isInteger(accountId) || accountId <= 0) continue;
      if (accountIdFilter > 0 && accountId !== accountIdFilter) continue;

      const externalId = String(extRow.extid || "").trim();
      if (extId) {
        if (fuzzySearch) {
          if (!externalId.toLowerCase().includes(extId.toLowerCase())) continue;
        } else if (externalId !== extId) {
          continue;
        }
      }

      const account = readAccountInfo(accountId, accountCache);
      if (!account || typeof account !== "object") continue;

      const accountNickname = String(account.nickName || account.externalId || externalId || "").trim();
      if (nickname) {
        if (fuzzySearch) {
          if (!accountNickname.toLowerCase().includes(nickname.toLowerCase())) continue;
        } else if (accountNickname !== nickname) {
          continue;
        }
      }

      const accountBankId = Number.isInteger(Number(account.bankId))
        ? Number(account.bankId)
        : Number(bankId);
      const bankInfo = readBankInfo(accountBankId, bankCache);
      const subcasinoId = Number.isInteger(Number(account.subCasinoId))
        ? Number(account.subCasinoId)
        : bankInfo && Number.isInteger(Number(bankInfo.subCasinoId))
          ? Number(bankInfo.subCasinoId)
          : catalogBank && Number.isInteger(Number(catalogBank.subcasinoId))
            ? Number(catalogBank.subcasinoId)
            : null;

      if (subcasinoFilter.length && subcasinoId && !subcasinoFilter.includes(subcasinoId)) continue;
      const subcasinoInfo = subcasinoId ? readSubcasinoInfo(subcasinoId, subcasinoCache) : null;

      const isLocked = !!account.locked;
      if (accountLockedFilter !== null && isLocked !== accountLockedFilter) continue;

      const registerTimeMs = Number(account.registerTime);
      if (regAfterMs && (!Number.isFinite(registerTimeMs) || registerTimeMs < regAfterMs)) continue;
      if (regBeforeMs && (!Number.isFinite(registerTimeMs) || registerTimeMs > regBeforeMs)) continue;

      const balanceAmount = centsToAmount(Number(account.balance));
      const currencyCode =
        account.currency && account.currency.code ? String(account.currency.code) : "";

      players.push({
        accountId,
        accountName: accountNickname,
        accountNickname,
        externalId: externalId || String(account.externalId || ""),
        bankId: accountBankId,
        bankName: bankInfo ? bankInfo.title : `bank-${accountBankId}`,
        subcasinoId: subcasinoId || null,
        subcasinoName: subcasinoInfo ? subcasinoInfo.title : "",
        accountStatus: accountStatusLabel(isLocked),
        balance: balanceAmount,
        registrationTimeMs: Number.isFinite(registerTimeMs) ? registerTimeMs : 0,
        registrationTime: displayFromEpochMs(registerTimeMs),
        lastLoginTimeMs: Number(account.lastLoginTime) || 0,
        lastLoginTime: displayFromEpochMs(account.lastLoginTime),
        currencyCode: currencyCode || "",
        currencyId: null,
        isAccountTester: !!account.testUser,
        locked: isLocked,
      });
    }
  }

  players.sort((a, b) => b.registrationTimeMs - a.registrationTimeMs || a.accountId - b.accountId);
  const totalRecords = players.length;
  const rows = players.slice(0, mainPerPage).map((player, idx) => {
    const sessions = readGameSessionsByExtId(player.externalId);
    const metrics = aggregateSessionStats(sessions);
    const lastSessionPlatform = metrics.lastSession ? String(metrics.lastSession.platform || "") : "";
    const lastSessionTimeMs = metrics.lastSession ? Number(metrics.lastSession.endTimeMs || 0) : 0;
    const lastLoginTimeMs = Math.max(Number(player.lastLoginTimeMs || 0), lastSessionTimeMs);

    const platformText = lastSessionPlatform || "Flash";

    return {
      rowNumber: idx + 1,
      accountId: player.accountId,
      accountName: player.accountName,
      accountNickname: player.accountNickname,
      externalId: player.externalId,
      subcasinoId: player.subcasinoId,
      subcasinoName: player.subcasinoName,
      bankId: player.bankId,
      bankName: player.bankName,
      accountStatus: player.accountStatus,
      totalAccountSessionCount: metrics.sessionCount,
      totalBetsCount: metrics.betsCount,
      totalIncome: metrics.income,
      gameRevenue: metrics.gameRevenue,
      balance: player.balance,
      lastPlatform: platformText,
      lastPlatformId: platformIdByName(platformText),
      lastLoginTime: displayFromEpochMs(lastLoginTimeMs),
      registrationTime: player.registrationTime,
      currencyId: player.currencyId,
      currencyCode: player.currencyCode,
      totalRecords,
    };
  });

  return {
    reportId: "playerSearch",
    sourceTable: "rcasinoscks.accountcf_ext + rcasinoscks.accountcf + rcasinoks.gamesessioncf",
    filters: {
      clusterId: clusterId > 0 ? clusterId : null,
      subcasinoList: subcasinoFilter,
      bankList: bankIds,
      nickName: nickname || null,
      accountId: accountIdFilter > 0 ? accountIdFilter : null,
      extId: extId || null,
      fuzzySearch,
      regAfterTime: regAfterMs,
      regBeforeTime: regBeforeMs,
      accountStatus: accountLockedFilter,
      mainPerPage,
    },
    count: rows.length,
    rows,
    links: {
      playerSummaryInfo:
        "/cm/players/{bankId}/{accountId}/summary?accountName={accountName}",
      playerGameInfo: "/cm/players/{bankId}/{accountId}/game-info",
      playerPaymentDetail: "/cm/players/{bankId}/{accountId}/payment-detail",
      playerBonusReport: "/cm/players/{bankId}/{accountId}/bonus-detail",
      playerFRBonusReport: "/cm/players/{bankId}/{accountId}/frbonus-detail",
      objectChangeHistory: "/cm/players/{bankId}/{accountId}/change-history",
    },
  };
}

function readPlayerHistoryRows(db, accountId) {
  return (db.audit || [])
    .filter(
      (a) =>
        String(a.objectType || "").toLowerCase() === "player" &&
        String(a.objectId || "") === String(accountId)
    )
    .slice(0, 200)
    .map((row) => ({
      at: formatDisplayDate(row.at),
      actor: row.actor,
      action: row.action,
      objectType: row.objectType,
      objectId: row.objectId,
      objectName: row.objectName,
      details: row.details || {},
    }));
}

function buildPlayerSummaryPayload(db, bankId, accountId) {
  const account = readAccountInfo(accountId, new Map());
  if (!account) return null;

  const resolvedBankId = Number.isInteger(Number(account.bankId)) ? Number(account.bankId) : Number(bankId);
  const bankInfo = readBankInfo(resolvedBankId, new Map()) || { title: `bank-${resolvedBankId}` };
  const subcasinoId = Number(account.subCasinoId || bankInfo.subCasinoId || 0) || null;
  const subcasinoInfo = subcasinoId ? readSubcasinoInfo(subcasinoId, new Map()) : null;
  const externalId = String(account.externalId || account.nickName || "");
  const sessions = readGameSessionsByExtId(externalId);
  const stats = aggregateSessionStats(sessions);

  const accountName = String(account.nickName || externalId || `account-${accountId}`);
  const statusLabel = accountStatusLabel(!!account.locked);
  const registrationPlatform = stats.lastSession && stats.lastSession.platform
    ? String(stats.lastSession.platform)
    : "Flash";
  const currencyCode = account.currency && account.currency.code ? String(account.currency.code) : "";
  const historyRows = readPlayerHistoryRows(db, accountId);
  const bonusRows = readBonusHistoryRows(accountId);
  const frbonusRows = readFrbonusHistoryRows(accountId);
  const bonusStats = summarizeBonusRows(bonusRows);
  const frbonusStats = summarizeFrbonusRows(frbonusRows);
  const gameMap = gameInfoByBank(resolvedBankId);

  return {
    reportId: "playerSummaryInfo",
    accountId: Number(account.id || accountId),
    accountName,
    externalId,
    bankId: resolvedBankId,
    bankName: bankInfo.title,
    subcasinoId,
    subcasinoName: subcasinoInfo ? subcasinoInfo.title : "",
    status: statusLabel,
    locked: !!account.locked,
    tester: !!account.testUser,
    currencyCode,
    menu: {
      actions: [
        { id: "lockAccount", title: account.locked ? "Unlock Account" : "Lock Account" },
        { id: "awardBonus", title: "Award Bonus" },
        { id: "awardFRBonus", title: "Award FRBonus" },
        { id: "makeTester", title: account.testUser ? "Unset tester" : "Set tester" },
      ],
      view: [
        { id: "frbonus", title: "View fr bonus report info" },
        { id: "summary", title: "View summary info" },
        { id: "game", title: "View game info" },
        { id: "payments", title: "View payments detail" },
        { id: "bonus", title: "View bonus report info" },
        { id: "history", title: "Object change history" },
      ],
    },
    shortInfo: {
      accountUsername: accountName,
      accountExternalId: externalId,
      cluster: "gp3",
      subcasinoName: subcasinoInfo ? subcasinoInfo.title : "",
      bankName: bankInfo.title,
      isAccountTester: account.testUser ? "YES" : "NO",
      balance: centsToAmount(Number(account.balance)),
      totalBets: stats.income,
      totalPayout: stats.payout,
      payoutIncludingJp: stats.payout,
      locked: account.locked ? "YES" : "NO",
      gameRevenue: `${stats.gameRevenue} / 0.0%`,
      deposits: 0,
      withdrawals: 0,
      currencyCode: currencyCode || "",
      streamer: "NO",
    },
    playerInfo: {
      id: Number(account.id || accountId),
      registrationTime: displayFromEpochMs(account.registerTime),
      registrationPlatform,
    },
    activity: {
      loginTime: displayFromEpochMs(account.lastLoginTime || (stats.lastSession && stats.lastSession.endTimeMs)),
      roundsCount: stats.roundsCount,
      platform: registrationPlatform,
      bonusIncome: stats.bonusBet,
      bonusPayout: stats.bonusWin,
      totalSessionsPlayed: stats.sessionCount,
    },
    bonuses: bonusStats,
    frb: frbonusStats,
    bonusRows,
    frbonusRows,
    gameRows: sessions
      .slice()
      .sort((a, b) => b.endTimeMs - a.endTimeMs)
      .map((s) => ({
        sessionId: s.gsid,
        gameId: Number(s.gameId || 0),
        gameTitle: gameMap.get(Number(s.gameId || 0))
          ? String(gameMap.get(Number(s.gameId || 0)).title)
          : `Game ${Number(s.gameId || 0)}`,
        platformId: platformIdByName(s.platform || ""),
        startTimeMs: Number(s.startTimeMs || 0),
        endTime: displayFromEpochMs(s.endTimeMs),
        endTimeMs: Number(s.endTimeMs || 0),
        platform: s.platform || "-",
        betsCount: s.betsCount,
        income: Number(s.income || 0),
        payout: Number(s.payout || 0),
        bonusBet: Number(s.bonusBet || 0),
        bonusWin: Number(s.bonusWin || 0),
        externalUnjWin: Number(s.externalUnjWin || 0),
        jackpotContributions: Number(s.jackpotContributions || 0),
        negativeBet: Number(s.negativeBet || 0),
        startBalance: Number(s.startBalance || 0),
        endBalance: Number(s.endBalance || 0),
        totalBets: centsToAmount(s.income),
        totalPayout: centsToAmount(s.payout),
        gameRevenue: centsToAmount(Number(s.income || 0) - Number(s.payout || 0)),
        currencyCode: s.currencyCode || currencyCode || "",
      })),
    paymentRows: [],
    historyRows,
  };
}

function saveAccountObject(accountId, accountObj) {
  const payload = cqlQuote(JSON.stringify(accountObj));
  runCql(`UPDATE rcasinoscks.accountcf SET jcn='${payload}' WHERE key=${Number(accountId)};`);
}

function bonusTypeApiName(bonusTypeId) {
  const n = Number(bonusTypeId);
  if (n === 0) return "DEPOSIT";
  if (n === 1) return "SLOTS";
  if (n === 2) return "LOSS";
  if (n === 3) return "PRIZE";
  if (n === 4) return "PROMO";
  if (n === 5) return "SPECIAL";
  return "";
}

function normalizeGameMode(gameLimitType) {
  const n = Number(gameLimitType);
  if (n === 1) return "EXCEPT";
  if (n === 2) return "ONLY";
  return "ALL";
}

function buildHashWithBankKey(bankInfo, orderedParams) {
  const values = [];
  for (const value of orderedParams || []) {
    if (value == null || value === "") continue;
    values.push(String(value));
  }
  if (bankInfo && bankInfo.bonusHashEnabled) {
    values.push(String(bankInfo.bonusPassKey || ""));
  }
  return md5Hex(values.join(""));
}

function upsertBonusAward(bankId, accountId, account, actionBody, actor) {
  const amount = parseNumber(actionBody.amount, NaN);
  const rolloverMultiplier = parseNumber(actionBody.rolloverMultiplier, NaN);
  const multiplierCap = parseNumber(actionBody.multiplierCap, 0);
  const explicitMaxWinCap = parseNumber(actionBody.maxWinCap, 0);
  const bonusType = Number.isInteger(Number(actionBody.bonusType)) ? Number(actionBody.bonusType) : 0;
  const description = String(actionBody.description || "").trim().slice(0, 500);
  const releasedType = parseBooleanText(actionBody.releasedType, true);
  const gameLimitType = Number.isInteger(Number(actionBody.gameLimitType))
    ? Number(actionBody.gameLimitType)
    : 0;
  const gameList = uniqIntArray(actionBody.gameList).slice(0, 500);
  const startTimeMs = parseTimeParamMs(actionBody.startTime) || Date.now();
  const expirationTimeMs = parseTimeParamMs(actionBody.expirationTime) || 0;

  if (!Number.isFinite(amount) || amount <= 0) {
    return { error: "INVALID_AMOUNT" };
  }
  if (!Number.isFinite(rolloverMultiplier) || rolloverMultiplier <= 0) {
    return { error: "INVALID_ROLLOVER_MULTIPLIER" };
  }
  if (!Number.isFinite(expirationTimeMs) || expirationTimeMs <= startTimeMs) {
    return { error: "INVALID_EXPIRATION_TIME" };
  }

  const bankInfo = readBankInfo(Number(bankId), new Map());
  if (!bankInfo) return { error: "BANK_NOT_FOUND" };
  if (bankInfo.bonusHashEnabled && !String(bankInfo.bonusPassKey || "").trim()) {
    return { error: "BANK_BONUS_PASSKEY_MISSING" };
  }
  const extUserId = String(account.externalId || account.nickName || "").trim();
  if (!extUserId) return { error: "PLAYER_EXT_ID_MISSING" };

  const type = bonusTypeApiName(bonusType);
  if (!type) return { error: "INVALID_BONUS_TYPE" };
  const gamesMode = normalizeGameMode(gameLimitType);
  const gameIds = uniqIntArray(gameList).filter((v) => v > 0);
  const gameIdsStr = gameIds.join("|");
  if (gamesMode !== "ALL" && !gameIdsStr) {
    return { error: "INVALID_GAMES_ID" };
  }

  const amountMinor = Math.round(amount * 100);
  if (!Number.isFinite(amountMinor) || amountMinor < 1) {
    return { error: "INVALID_AMOUNT" };
  }
  const effectiveBankId = String(bankInfo.externalBankId || bankId);
  const subCasinoId = Number(account.subCasinoId || bankInfo.subCasinoId || 0);
  const extBonusId = `CM-BONUS-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
  const expDate = formatBonusApiDate(expirationTimeMs);
  const startTime = formatBonusApiDateTime(startTimeMs);
  const comment = String(actionBody.comment || "").trim().slice(0, 500);
  let maxWinMultiplier = parseNumber(actionBody.maxWinMultiplier, NaN);
  if (!Number.isFinite(maxWinMultiplier) || maxWinMultiplier <= 0) {
    if (multiplierCap > 0) {
      maxWinMultiplier = multiplierCap;
    } else if (explicitMaxWinCap > 0 && amount > 0) {
      maxWinMultiplier = explicitMaxWinCap / amount;
    } else {
      maxWinMultiplier = NaN;
    }
  }
  const maxWinMultiplierStr = Number.isFinite(maxWinMultiplier) && maxWinMultiplier > 0
    ? Number(maxWinMultiplier.toFixed(4)).toString()
    : "";
  const autoRelease = releasedType ? "true" : "false";

  const hash = buildHashWithBankKey(bankInfo, [
    extUserId,
    effectiveBankId,
    type,
    String(amountMinor),
    Number(rolloverMultiplier.toFixed(4)).toString(),
    maxWinMultiplierStr,
    gamesMode,
    gameIdsStr,
    expDate,
    comment,
    description,
    extBonusId,
    autoRelease,
  ]);
  const xml = runGsCurl("/bsaward.do", {
    subCasinoId: Number.isInteger(subCasinoId) && subCasinoId > 0 ? subCasinoId : "",
    bankId: effectiveBankId,
    userId: extUserId,
    type,
    amount: amountMinor,
    multiplier: Number(rolloverMultiplier.toFixed(4)).toString(),
    games: gamesMode,
    gameIds: gameIdsStr,
    expDate,
    comment,
    description,
    extBonusId,
    hash,
    autoRelease,
    startTime,
    maxWinMultiplier: maxWinMultiplierStr,
    timeZone: Number.isFinite(Number(actionBody.timeZone)) ? Number(actionBody.timeZone) : 0,
  });
  const parsed = parseBonusApiXml(xml);
  if (parsed.result !== "OK") {
    return {
      error: "AWARD_BONUS_API_ERROR",
      code: parsed.code || "",
      message: parsed.description || "Bonus API returned error",
    };
  }
  const bonusId = Number(parsed.bonusId || 0);
  const rollover = Number((amount * rolloverMultiplier).toFixed(2));
  return {
    ok: true,
    bonusId,
    externalBonusId: extBonusId,
    amount: Number(amount.toFixed(2)),
    rollover,
  };
}

function upsertFrbonusAward(bankId, accountId, account, actionBody, actor) {
  const rounds = toSafeInt(actionBody.rounds || actionBody.freeRoundsCount, -1, 1, 1_000_000);
  const frChips = parseNumber(actionBody.frChips, NaN);
  const frbBetType = toSafeInt(actionBody.frbBetType, 1, 1, 2);
  const gameLimitType = Number.isInteger(Number(actionBody.gameLimitType))
    ? Number(actionBody.gameLimitType)
    : 0;
  const gameList = uniqIntArray(actionBody.gameList).slice(0, 500);
  const gameId = Number.isInteger(Number(actionBody.gameId || actionBody.singleGame))
    ? Number(actionBody.gameId || actionBody.singleGame)
    : null;
  const awardDurationDays = toSafeInt(actionBody.awardDurationDays, 0, 0, 10_000);
  const maxWinCap = parseNumber(actionBody.maxWinCap, 0);
  const description = String(actionBody.description || "").trim().slice(0, 500);
  const startTimeMs = parseTimeParamMs(actionBody.startTime) || Date.now();
  const expirationTimeMs = parseTimeParamMs(actionBody.expirationTime) || 0;

  if (!Number.isInteger(rounds) || rounds <= 0) {
    return { error: "INVALID_ROUNDS" };
  }
  if (!Number.isFinite(frChips) || frChips <= 0) {
    return { error: "INVALID_FR_CHIPS" };
  }
  if (!Number.isFinite(expirationTimeMs) || expirationTimeMs <= startTimeMs) {
    return { error: "INVALID_EXPIRATION_TIME" };
  }
  if (frbBetType === 2 && (!Number.isInteger(gameId) || gameId <= 0)) {
    return { error: "GAME_ID_REQUIRED_FOR_FIXED_BET" };
  }

  const bankInfo = readBankInfo(Number(bankId), new Map());
  if (!bankInfo) return { error: "BANK_NOT_FOUND" };
  if (bankInfo.bonusHashEnabled && !String(bankInfo.bonusPassKey || "").trim()) {
    return { error: "BANK_BONUS_PASSKEY_MISSING" };
  }
  const extUserId = String(account.externalId || account.nickName || "").trim();
  if (!extUserId) return { error: "PLAYER_EXT_ID_MISSING" };
  const effectiveBankId = String(bankInfo.externalBankId || bankId);
  const subCasinoId = Number(account.subCasinoId || bankInfo.subCasinoId || 0);
  const extFrbonusId = `CM-FRB-${Date.now()}-${Math.floor(Math.random() * 100000)}`;

  let selectedGameIds =
    frbBetType === 2 && Number.isInteger(gameId) && gameId > 0
      ? [gameId]
      : uniqIntArray(gameList).filter((v) => v > 0);
  if (!selectedGameIds.length && frbBetType !== 2) {
    if (gameLimitType === 0) {
      selectedGameIds = frbAllGamesByBank(Number(bankId));
    } else {
      return { error: "INVALID_GAME_LIST" };
    }
  }
  if (!selectedGameIds.length) {
    return { error: "FRB_GAMES_NOT_FOUND" };
  }
  const games = selectedGameIds.join("|");
  const comment = String(actionBody.comment || "").trim().slice(0, 500);
  const maxWinLimitMinor = maxWinCap > 0 ? Math.round(maxWinCap * 100) : 0;
  const hash = buildHashWithBankKey(bankInfo, [
    extUserId,
    effectiveBankId,
    String(rounds),
    games,
    comment,
    description,
    maxWinLimitMinor > 0 ? String(maxWinLimitMinor) : "",
    extFrbonusId,
  ]);
  const xml = runGsCurl("/frbaward.do", {
    subCasinoId: Number.isInteger(subCasinoId) && subCasinoId > 0 ? subCasinoId : "",
    bankId: effectiveBankId,
    userId: extUserId,
    rounds,
    games,
    extBonusId: extFrbonusId,
    hash,
    startTime: formatBonusApiDateTime(startTimeMs),
    expirationTime: formatBonusApiDateTime(expirationTimeMs),
    duration: awardDurationDays > 0 ? awardDurationDays : "",
    frbTableRoundChips: Math.round(frChips),
    comment,
    description,
    maxWinLimit: maxWinLimitMinor > 0 ? maxWinLimitMinor : "",
    timeZone: Number.isFinite(Number(actionBody.timeZone)) ? Number(actionBody.timeZone) : 0,
  });
  const parsed = parseBonusApiXml(xml);
  if (parsed.result !== "OK") {
    return {
      error: "AWARD_FRBONUS_API_ERROR",
      code: parsed.code || "",
      message: parsed.description || "FR bonus API returned error",
    };
  }
  const frbonusId = Number(parsed.bonusId || 0);
  return {
    ok: true,
    frbonusId,
    externalFrbonusId: extFrbonusId,
    rounds,
    frChips: Number(frChips.toFixed(2)),
  };
}

function toSafeInt(value, fallback, min, max) {
  const n = Number(value);
  if (!Number.isInteger(n)) return fallback;
  if (n < min) return fallback;
  if (n > max) return max;
  return n;
}

function validateExtId(v) {
  return /^[A-Za-z0-9._:\-\[\]]{1,128}$/.test(v || "");
}

function parseCsvInts(value) {
  if (!value) return [];
  return uniqIntArray(
    String(value)
      .split(",")
      .map((x) => x.trim())
      .filter(Boolean)
  );
}

function hasPermission(access, permissionId) {
  return access.isSuperAdmin || access.permissionSet.has(permissionId);
}

function hasAnyPermission(access, permissionIds) {
  return permissionIds.some((p) => hasPermission(access, p));
}

function requirePermission(access, res, permissionId) {
  if (!hasPermission(access, permissionId)) {
    json(res, 403, { error: "FORBIDDEN", permissionId });
    return false;
  }
  return true;
}

function buildCapabilities(access) {
  return {
    canViewUserList: hasPermission(access, PERM.VIEW_USER_LIST),
    canCreateUser: hasPermission(access, PERM.CREATE_USER),
    canViewRoleList: hasPermission(access, PERM.VIEW_ROLE_LIST),
    canCreateRole: hasPermission(access, PERM.CREATE_ROLE),
    canEditUser: hasPermission(access, PERM.EDIT_USER),
    canSwitch2fa: hasPermission(access, PERM.MANAGE_2FA),
    canViewHistory: hasPermission(access, PERM.VIEW_OBJECT_HISTORY),
    canViewSessionInfo: hasPermission(access, PERM.VIEW_USER_SESSIONS),
    canViewIpsInfo: hasPermission(access, PERM.VIEW_USER_IPS),
    canResetPassword: hasPermission(access, PERM.RESET_USER_PASSWORD),
    canFlushIps: hasPermission(access, PERM.FLUSH_USER_IPS),
    canLockUnlock: hasPermission(access, PERM.LOCK_UNLOCK_USER),
    canDeleteUser: hasPermission(access, PERM.DELETE_USER),
    canManageAny: hasAnyPermission(access, MANAGE_PERMISSIONS),
  };
}

function permissionFromMenuHref(href, action) {
  const link = String(href || "");

  if (/\/api\/reports\/userList\//.test(link)) return PERM.VIEW_USER_LIST;
  if (/\/api\/reports\/roleList\//.test(link)) return PERM.VIEW_ROLE_LIST;
  if (/\/api\/action\/createUser$/.test(link)) return PERM.CREATE_USER;
  if (/\/api\/action\/createRole$/.test(link)) return PERM.CREATE_ROLE;

  if (/\/api\/reports\/playerSearch\//.test(link)) return PERM.VIEW_PLAYER_SEARCH;
  if (/\/api\/reports\/bankList\//.test(link)) return PERM.VIEW_BANK_LIST;
  if (/\/api\/reports\/transactions\//.test(link)) return PERM.VIEW_TRANSACTIONS;
  if (/\/api\/reports\/gameSessionSearch\//.test(link)) return PERM.VIEW_GAME_SESSION;
  if (/\/api\/reports\/walletOperationAlerts\//.test(link)) return PERM.VIEW_WALLET_ALERTS;

  if (String(action || "").toLowerCase() === "dialog" || /\/api\/action\//.test(link)) {
    return "MANAGE_ANY";
  }

  return null;
}

function filterMenuTree(items, access) {
  const out = [];
  for (const original of items || []) {
    const node = JSON.parse(JSON.stringify(original));
    const children = Array.isArray(node.children) ? filterMenuTree(node.children, access) : [];
    if (children.length) {
      node.children = children;
      out.push(node);
      continue;
    }

    const href =
      Array.isArray(node.links) && node.links[0] && node.links[0].href
        ? String(node.links[0].href)
        : "";
    const required = permissionFromMenuHref(href, node.action);
    if (required === "MANAGE_ANY" && !buildCapabilities(access).canManageAny) {
      continue;
    }
    if (Number.isInteger(required) && !hasPermission(access, required)) {
      continue;
    }
    out.push(node);
  }
  return out;
}

function filteredReportDefs(access) {
  return REPORT_DEFS.filter((r) => {
    const required = REPORT_PERMISSION_BY_ID[r.id];
    if (!required) return true;
    return hasPermission(access, required);
  });
}

function bootstrapDefaultRoot() {
  const db = loadCoreDb();
  const superAdminRoleId = roleIdByName(db.roles, "SUPER_ADMIN") || 1;
  const existing = getUserByUsername(db, "root");
  if (!existing) {
    const hash = hashPasswordScrypt("root");
    db.users.push({
      username: "root",
      email: "root@localhost",
      comment: "Bootstrap super admin",
      passwordHash: hash,
      passwordHistory: [hash],
      roleIds: [superAdminRoleId],
      roleSet: ["SUPER_ADMIN"],
      status: "ACTIVE",
      userLevel: "GENERAL",
      includeFutureBanks: true,
      clusterIds: [],
      subcasinoIds: [],
      bankIds: [],
      twoFactorEnabled: false,
      twoFactorStatus: "Disabled",
      mustChangePassword: true,
      failedAttempts: 0,
      failedAt: [],
      lockedUntil: null,
      createdAt: nowIso(),
      updatedAt: nowIso(),
      lastLoginAt: null,
      lastLoginIp: null,
      loginSessions: [],
      loginIpHistory: [],
      deletedAt: null,
    });
    appendAudit(db, {
      actor: "system",
      action: "CREATE_USER",
      objectType: "User",
      objectId: "root",
      objectName: "root",
      details: { source: "bootstrap" },
    });
  } else if (!existing.roleIds || !existing.roleIds.includes(superAdminRoleId)) {
    existing.roleIds = [superAdminRoleId];
    existing.roleSet = ["SUPER_ADMIN"];
    existing.updatedAt = nowIso();
    appendAudit(db, {
      actor: "system",
      action: "FIX_ROOT_ROLE",
      objectType: "User",
      objectId: "root",
      objectName: "root",
      details: { roleId: superAdminRoleId },
    });
  }
  if (existing && isRootLogin(existing.username)) {
    let changed = false;
    if (existing.status !== "ACTIVE") {
      existing.status = "ACTIVE";
      changed = true;
    }
    if (existing.lockedUntil) {
      existing.lockedUntil = null;
      changed = true;
    }
    if (Number(existing.failedAttempts || 0) !== 0) {
      existing.failedAttempts = 0;
      changed = true;
    }
    if (Array.isArray(existing.failedAt) && existing.failedAt.length) {
      existing.failedAt = [];
      changed = true;
    }
    if (changed) {
      existing.updatedAt = nowIso();
      appendAudit(db, {
        actor: "system",
        action: "UNLOCK_ROOT_GUARD",
        objectType: "User",
        objectId: "root",
        objectName: "root",
        details: {},
      });
    }
  }
  saveCoreDb(db);
}

async function handleLogin(req, res) {
  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const username = String(body.username || "").trim();
  const password = String(body.password || "");
  if (!username || !password) {
    json(res, 400, { error: "MISSING_CREDENTIALS" });
    return;
  }

  const db = loadCoreDb();
  const user = getUserByUsername(db, username);
  if (!user) {
    json(res, 401, { error: "INVALID_CREDENTIALS" });
    return;
  }
  const isRoot = isRootLogin(user.username);
  if (isRoot) {
    let changed = false;
    if (user.status !== "ACTIVE") {
      user.status = "ACTIVE";
      changed = true;
    }
    if (user.lockedUntil) {
      user.lockedUntil = null;
      changed = true;
    }
    if (Number(user.failedAttempts || 0) !== 0) {
      user.failedAttempts = 0;
      changed = true;
    }
    if (Array.isArray(user.failedAt) && user.failedAt.length) {
      user.failedAt = [];
      changed = true;
    }
    if (changed) {
      user.updatedAt = nowIso();
      saveCoreDb(db);
    }
  } else if (user.status !== "ACTIVE") {
    json(res, 401, { error: "INVALID_CREDENTIALS" });
    return;
  }

  const now = Date.now();
  if (!isRoot && user.lockedUntil && new Date(user.lockedUntil).getTime() > now) {
    json(res, 423, {
      error: "ACCOUNT_LOCKED",
      lockedUntil: user.lockedUntil,
    });
    return;
  }

  const ok = verifyPasswordScrypt(user.passwordHash, password);
  if (!ok) {
    if (isRoot) {
      json(res, 401, { error: "INVALID_CREDENTIALS" });
      return;
    }
    const recent = (user.failedAt || []).filter(
      (t) => now - new Date(t).getTime() <= LOCK_WINDOW_MS
    );
    recent.push(new Date(now).toISOString());
    user.failedAt = recent;
    user.failedAttempts = recent.length;
    if (recent.length >= LOCK_THRESHOLD) {
      user.lockedUntil = new Date(now + LOCK_WINDOW_MS).toISOString();
    }
    user.updatedAt = nowIso();
    saveCoreDb(db);
    json(res, user.lockedUntil ? 423 : 401, {
      error: user.lockedUntil ? "ACCOUNT_LOCKED" : "INVALID_CREDENTIALS",
      remainingBeforeLock: Math.max(0, LOCK_THRESHOLD - recent.length),
      lockedUntil: user.lockedUntil || null,
    });
    return;
  }

  user.failedAt = [];
  user.failedAttempts = 0;
  user.lockedUntil = null;
  user.lastLoginAt = nowIso();
  user.lastLoginIp = extractClientIp(req);
  user.updatedAt = nowIso();

  const accessToken = newToken(32);
  const refreshToken = newToken(48);
  const sessionId = newToken(10);
  const accessExpiresAt = now + ACCESS_TTL_MS;
  const refreshExpiresAt = now + REFRESH_TTL_MS;

  accessTokens.set(accessToken, {
    username: user.username,
    expiresAt: accessExpiresAt,
    sessionId,
  });
  refreshTokens.set(hashToken(refreshToken), {
    username: user.username,
    expiresAt: refreshExpiresAt,
    revoked: false,
  });

  const newSession = {
    sessionId,
    issuedAt: nowIso(),
    expiresAt: new Date(accessExpiresAt).toISOString(),
    ip: extractClientIp(req),
    userAgent: String(req.headers["user-agent"] || "unknown"),
    active: true,
    lastSeenAt: nowIso(),
  };
  user.loginSessions = [newSession, ...(Array.isArray(user.loginSessions) ? user.loginSessions : [])]
    .slice(0, 20)
    .map((s, idx) => (idx === 0 ? s : Object.assign({}, s, { active: false })));
  addIpHistory(user, newSession.ip);

  appendAudit(db, {
    actor: user.username,
    action: "LOGIN",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: { ip: newSession.ip },
  });

  saveCoreDb(db);

  const permissionSet = permissionSetForUser(db, user);
  const access = {
    user,
    db,
    permissionSet,
    isSuperAdmin: roleNamesForUser(db, user).includes("SUPER_ADMIN"),
  };

  json(res, 200, {
    accessToken,
    accessExpiresAt: new Date(accessExpiresAt).toISOString(),
    refreshToken,
    refreshExpiresAt: new Date(refreshExpiresAt).toISOString(),
    mustChangePassword: !!user.mustChangePassword,
    roles: roleNamesForUser(db, user),
    permissions: Array.from(permissionSet.values()).sort((a, b) => a - b),
    capabilities: buildCapabilities(access),
  });
}

async function handleChangePassword(req, res, auth) {
  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const oldPassword = String(body.oldPassword || "");
  const newPassword = String(body.newPassword || "");
  if (!oldPassword || !newPassword) {
    json(res, 400, { error: "MISSING_PASSWORDS" });
    return;
  }
  if (oldPassword === newPassword) {
    json(res, 400, { error: "NEW_PASSWORD_MUST_DIFFER" });
    return;
  }

  const policyErrors = checkPasswordPolicy(newPassword);
  if (policyErrors.length) {
    json(res, 400, { error: "PASSWORD_POLICY_FAILED", details: policyErrors });
    return;
  }

  const db = loadCoreDb();
  const user = getUserByUsername(db, auth.username);
  if (!user) {
    json(res, 404, { error: "USER_NOT_FOUND" });
    return;
  }
  if (!verifyPasswordScrypt(user.passwordHash, oldPassword)) {
    json(res, 401, { error: "INVALID_CREDENTIALS" });
    return;
  }

  const history = Array.isArray(user.passwordHistory) ? user.passwordHistory : [];
  for (const h of history.slice(0, 5)) {
    if (verifyPasswordScrypt(h, newPassword)) {
      json(res, 400, { error: "PASSWORD_REUSE_NOT_ALLOWED" });
      return;
    }
  }

  const newHash = hashPasswordScrypt(newPassword);
  user.passwordHash = newHash;
  user.passwordHistory = [newHash, ...history].slice(0, 5);
  user.mustChangePassword = false;
  user.updatedAt = nowIso();

  appendAudit(db, {
    actor: user.username,
    action: "CHANGE_PASSWORD",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: {},
  });

  saveCoreDb(db);
  revokeAllTokensForUser(user.username);

  json(res, 200, {
    ok: true,
    message: "Password changed. Please login again.",
  });
}

function requireAuth(req, res) {
  const auth = authenticateAccess(req);
  if (!auth) {
    json(res, 401, { error: "UNAUTHORIZED" });
    return null;
  }
  return auth;
}

function requireReportAccess(req, res) {
  const auth = requireAuth(req, res);
  if (!auth) return null;

  const db = loadCoreDb();
  const user = getUserByUsername(db, auth.username);
  if (!user || user.status !== "ACTIVE") {
    json(res, 401, { error: "UNAUTHORIZED" });
    return null;
  }
  if (user.mustChangePassword) {
    json(res, 428, { error: "PASSWORD_CHANGE_REQUIRED" });
    return null;
  }

  const permissionSet = permissionSetForUser(db, user);
  const access = {
    auth,
    user,
    db,
    permissionSet,
    isSuperAdmin: roleNamesForUser(db, user).includes("SUPER_ADMIN"),
  };
  access.capabilities = buildCapabilities(access);
  return access;
}

function buildUserManagementMeta(db, access) {
  const mirror = loadMirrorDb();
  return {
    clusters: db.catalog.clusters,
    subcasinos: db.catalog.subcasinos,
    banks: db.catalog.banks,
    roles: db.roles.map((r) => ({
      id: r.id,
      title: r.roleName,
      description: r.description,
      isSystem: r.isSystem,
    })),
    permissions: db.permissions,
    capabilities: access.capabilities,
    dbInfo: {
      core: { kind: "cm-core", file: CORE_FILE },
      mirror: {
        kind: "cm-mirror",
        file: MIRROR_FILE,
        lastUpdate: mirror.lastUpdate || null,
      },
    },
    userLevels: [
      { id: "GENERAL", title: "General" },
      { id: "SPECIFIC", title: "Specific" },
    ],
    statuses: [
      { id: "ACTIVE", title: "Active" },
      { id: "DISABLED", title: "Disabled" },
    ],
  };
}

function queryUserList(db, urlObj) {
  const login = String(urlObj.searchParams.get("login") || "").trim().toLowerCase();
  const email = String(urlObj.searchParams.get("email") || "").trim().toLowerCase();
  const status = String(urlObj.searchParams.get("status") || "").trim().toUpperCase();
  const userLevel = String(urlObj.searchParams.get("userLevel") || "").trim().toUpperCase();
  const clusterId = toSafeInt(urlObj.searchParams.get("clusterId"), -1, 1, 1_000_000);
  const subcasinoId = toSafeInt(urlObj.searchParams.get("subcasinoId"), -1, 1, 1_000_000);
  const bankFilter = parseCsvInts(urlObj.searchParams.get("bankList"));

  const rows = db.users
    .filter((u) => {
      if (login && !u.username.toLowerCase().includes(login)) return false;
      if (email && !u.email.toLowerCase().includes(email)) return false;
      if (status && u.status !== status) return false;
      if (userLevel && u.userLevel !== userLevel) return false;
      if (clusterId > 0 && !u.clusterIds.includes(clusterId)) return false;
      if (subcasinoId > 0 && !u.subcasinoIds.includes(subcasinoId)) return false;
      if (bankFilter.length && !bankFilter.some((id) => u.bankIds.includes(id))) return false;
      return true;
    })
    .sort((a, b) => a.username.localeCompare(b.username))
    .map((u, idx) => ({
      rowNumber: idx + 1,
      login: u.username,
      email: u.email,
      roles: roleNamesForUser(db, u).join(", "),
      lastUpdate: formatDisplayDate(u.updatedAt),
      lastLoggedIn: formatDisplayDate(u.lastLoginAt),
      status: u.status,
      loggedIn: hasActiveSession(u),
      timeSinceLogin: humanSince(u.lastLoginAt),
      userLevel: u.userLevel === "GENERAL" ? "General" : "Specific",
      enable2fa: u.twoFactorEnabled,
      status2fa: u.twoFactorEnabled ? "Enabled" : "Disabled",
    }));

  return {
    reportId: "userList",
    sourceTable: "cm_core.users",
    filters: {
      login: login || null,
      email: email || null,
      status: status || null,
      userLevel: userLevel || null,
      clusterId: clusterId > 0 ? clusterId : null,
      subcasinoId: subcasinoId > 0 ? subcasinoId : null,
      bankList: bankFilter,
    },
    count: rows.length,
    rows,
  };
}

function queryRoleList(db) {
  const rows = db.roles
    .slice()
    .sort((a, b) => a.roleName.localeCompare(b.roleName))
    .map((r, idx) => ({
      rowNumber: idx + 1,
      roleId: r.id,
      editable: r.editable,
      roleName: r.roleName,
      description: r.description,
      isSystem: r.isSystem,
      creationDate: formatDisplayDate(r.creationDate),
      lastChangeDate: formatDisplayDate(r.lastChangeDate),
    }));
  return {
    reportId: "roleList",
    sourceTable: "cm_core.roles",
    count: rows.length,
    rows,
  };
}

function normalizeScopes(body, isGeneral) {
  const clusterIds = uniqIntArray(body.clusterIds);
  const subcasinoIds = uniqIntArray(body.subcasinoIds);
  const bankIds = uniqIntArray(body.bankIds);
  if (isGeneral) {
    return { clusterIds: [], subcasinoIds: [], bankIds: [] };
  }
  return { clusterIds, subcasinoIds, bankIds };
}

function ensureRoleIdsExist(db, roleIds) {
  for (const id of roleIds) {
    if (!findRoleById(db, id)) {
      return id;
    }
  }
  return null;
}

async function handleCreateUser(req, res, access) {
  if (!requirePermission(access, res, PERM.CREATE_USER)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const db = access.db;
  const login = String(body.login || "").trim();
  const email = String(body.email || "").trim();
  const comment = String(body.comment || "").trim();
  const isGeneral = !!body.isGeneral;
  const includeFutureBanks = !!body.includeFutureBanks;
  const roleIds = uniqIntArray(body.roleIds || body.roles);
  const scopes = normalizeScopes(body, isGeneral);

  if (!login || !USER_LOGIN_RE.test(login)) {
    json(res, 400, { error: "INVALID_LOGIN" });
    return;
  }
  if (!email || !USER_EMAIL_RE.test(email)) {
    json(res, 400, { error: "INVALID_EMAIL" });
    return;
  }
  if (getUserByUsernameInsensitive(db, login)) {
    json(res, 409, { error: "LOGIN_ALREADY_EXISTS" });
    return;
  }
  if (db.users.some((u) => u.email.toLowerCase() === email.toLowerCase())) {
    json(res, 409, { error: "EMAIL_ALREADY_EXISTS" });
    return;
  }
  if (!roleIds.length) {
    json(res, 400, { error: "ROLE_REQUIRED" });
    return;
  }

  const unknownRole = ensureRoleIdsExist(db, roleIds);
  if (unknownRole) {
    json(res, 400, { error: "UNKNOWN_ROLE", roleId: unknownRole });
    return;
  }

  if (
    !isGeneral &&
    !scopes.clusterIds.length &&
    !scopes.subcasinoIds.length &&
    !scopes.bankIds.length
  ) {
    json(res, 400, {
      error: "SCOPE_REQUIRED",
      details: "At least one cluster, subcasino, or bank must be selected for non-general users.",
    });
    return;
  }

  const customPassword = String(body.initialPassword || "").trim();
  let effectivePassword = customPassword;
  if (effectivePassword) {
    const policyErrors = checkPasswordPolicy(effectivePassword);
    if (policyErrors.length) {
      json(res, 400, { error: "PASSWORD_POLICY_FAILED", details: policyErrors });
      return;
    }
  } else {
    effectivePassword = generateStrongPassword();
  }

  const hash = hashPasswordScrypt(effectivePassword);
  const now = nowIso();
  const user = {
    username: login,
    email,
    comment,
    passwordHash: hash,
    passwordHistory: [hash],
    roleIds,
    roleSet: roleIds.map((id) => findRoleById(db, id).roleName),
    status: "ACTIVE",
    userLevel: isGeneral ? "GENERAL" : "SPECIFIC",
    includeFutureBanks,
    clusterIds: scopes.clusterIds,
    subcasinoIds: scopes.subcasinoIds,
    bankIds: scopes.bankIds,
    twoFactorEnabled: false,
    twoFactorStatus: "Disabled",
    mustChangePassword: true,
    failedAttempts: 0,
    failedAt: [],
    lockedUntil: null,
    createdAt: now,
    updatedAt: now,
    lastLoginAt: null,
    lastLoginIp: null,
    loginSessions: [],
    loginIpHistory: [],
    deletedAt: null,
  };

  db.users.push(user);

  appendAudit(db, {
    actor: access.auth.username,
    action: "CREATE_USER",
    objectType: "User",
    objectId: login,
    objectName: login,
    details: {
      roles: user.roleSet,
      userLevel: user.userLevel,
      includeFutureBanks,
      scope: scopes,
    },
  });

  saveCoreDb(db);

  json(res, 201, {
    ok: true,
    user: {
      login: user.username,
      email: user.email,
      roles: user.roleSet,
      status: user.status,
      userLevel: user.userLevel,
      mustChangePassword: user.mustChangePassword,
    },
    generatedPassword: customPassword ? null : effectivePassword,
  });
}

async function handleEditUser(req, res, access) {
  if (!requirePermission(access, res, PERM.EDIT_USER)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const originalLogin = String(body.originalLogin || "").trim();
  if (!originalLogin) {
    json(res, 400, { error: "ORIGINAL_LOGIN_REQUIRED" });
    return;
  }

  const db = access.db;
  const user = getUserByUsernameInsensitive(db, originalLogin);
  if (!user) {
    json(res, 404, { error: "USER_NOT_FOUND" });
    return;
  }

  const newLogin = String(body.login || user.username).trim();
  const newEmail = String(body.email || user.email).trim();
  const newComment = String(body.comment || "").trim();
  const isGeneral =
    typeof body.isGeneral === "boolean"
      ? body.isGeneral
      : String(body.userLevel || user.userLevel).toUpperCase() === "GENERAL";
  const includeFutureBanks =
    typeof body.includeFutureBanks === "boolean"
      ? body.includeFutureBanks
      : !!user.includeFutureBanks;
  const roleIds = uniqIntArray(body.roleIds && body.roleIds.length ? body.roleIds : user.roleIds);
  const scopes = normalizeScopes(body, isGeneral);

  if (!newLogin || !USER_LOGIN_RE.test(newLogin)) {
    json(res, 400, { error: "INVALID_LOGIN" });
    return;
  }
  if (!newEmail || !USER_EMAIL_RE.test(newEmail)) {
    json(res, 400, { error: "INVALID_EMAIL" });
    return;
  }

  const duplicateLogin = db.users.find(
    (u) => u.username.toLowerCase() === newLogin.toLowerCase() && u.username !== user.username
  );
  if (duplicateLogin) {
    json(res, 409, { error: "LOGIN_ALREADY_EXISTS" });
    return;
  }

  const duplicateEmail = db.users.find(
    (u) => u.email.toLowerCase() === newEmail.toLowerCase() && u.username !== user.username
  );
  if (duplicateEmail) {
    json(res, 409, { error: "EMAIL_ALREADY_EXISTS" });
    return;
  }

  if (!roleIds.length) {
    json(res, 400, { error: "ROLE_REQUIRED" });
    return;
  }
  const unknownRole = ensureRoleIdsExist(db, roleIds);
  if (unknownRole) {
    json(res, 400, { error: "UNKNOWN_ROLE", roleId: unknownRole });
    return;
  }

  if (
    !isGeneral &&
    !scopes.clusterIds.length &&
    !scopes.subcasinoIds.length &&
    !scopes.bankIds.length
  ) {
    json(res, 400, {
      error: "SCOPE_REQUIRED",
      details: "At least one cluster, subcasino, or bank must be selected for non-general users.",
    });
    return;
  }

  const oldLogin = user.username;
  user.username = newLogin;
  user.email = newEmail;
  user.comment = newComment;
  user.includeFutureBanks = includeFutureBanks;
  user.userLevel = isGeneral ? "GENERAL" : "SPECIFIC";
  user.roleIds = roleIds;
  user.roleSet = roleIds.map((id) => findRoleById(db, id).roleName);
  user.clusterIds = scopes.clusterIds;
  user.subcasinoIds = scopes.subcasinoIds;
  user.bankIds = scopes.bankIds;
  user.updatedAt = nowIso();

  if (oldLogin !== newLogin) {
    deactivateAllSessionsForUser(user);
    revokeTokenMapsForUser(oldLogin);
  }

  appendAudit(db, {
    actor: access.auth.username,
    action: "EDIT_USER",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: {
      previousLogin: oldLogin,
      roles: user.roleSet,
      userLevel: user.userLevel,
      includeFutureBanks,
      scope: scopes,
    },
  });

  saveCoreDb(db);

  json(res, 200, {
    ok: true,
    user: {
      login: user.username,
      email: user.email,
      comment: user.comment,
      roles: user.roleSet,
      userLevel: user.userLevel,
      includeFutureBanks: user.includeFutureBanks,
    },
  });
}

async function handleCreateRole(req, res, access) {
  if (!requirePermission(access, res, PERM.CREATE_ROLE)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const db = access.db;
  const roleName = String(body.roleName || "").trim();
  const description = String(body.description || "").trim();
  const isSystem = !!body.isSystem;
  const isNonRestricted = body.isNonRestricted !== false;
  const permissions = uniqIntArray(body.permissions);
  const clusterIds = uniqIntArray(body.clusterIds);
  const subcasinoIds = uniqIntArray(body.subcasinoIds);
  const bankIds = uniqIntArray(body.bankIds);

  if (!roleName || roleName.length < 3) {
    json(res, 400, { error: "INVALID_ROLE_NAME" });
    return;
  }
  if (findRoleByName(db, roleName)) {
    json(res, 409, { error: "ROLE_ALREADY_EXISTS" });
    return;
  }
  if (!permissions.length) {
    json(res, 400, { error: "PERMISSIONS_REQUIRED" });
    return;
  }

  const knownPermissionIds = new Set(db.permissions.map((p) => p.id));
  for (const p of permissions) {
    if (!knownPermissionIds.has(p)) {
      json(res, 400, { error: "UNKNOWN_PERMISSION", permissionId: p });
      return;
    }
  }

  if (!isNonRestricted && !clusterIds.length && !subcasinoIds.length && !bankIds.length) {
    json(res, 400, {
      error: "SCOPE_REQUIRED",
      details: "At least one cluster, subcasino, or bank must be selected for restricted role.",
    });
    return;
  }

  const maxId = db.roles.reduce((m, r) => Math.max(m, r.id), 0);
  const now = nowIso();
  const role = {
    id: maxId + 1,
    roleName,
    description,
    isSystem,
    editable: !isSystem,
    isNonRestricted,
    permissions,
    clusterIds: isNonRestricted ? [] : clusterIds,
    subcasinoIds: isNonRestricted ? [] : subcasinoIds,
    bankIds: isNonRestricted ? [] : bankIds,
    creationDate: now,
    lastChangeDate: now,
  };

  db.roles.push(role);

  appendAudit(db, {
    actor: access.auth.username,
    action: "CREATE_ROLE",
    objectType: "Role",
    objectId: String(role.id),
    objectName: role.roleName,
    details: {
      permissions,
      isSystem,
      isNonRestricted,
      scope: {
        clusterIds: role.clusterIds,
        subcasinoIds: role.subcasinoIds,
        bankIds: role.bankIds,
      },
    },
  });

  saveCoreDb(db);

  json(res, 201, {
    ok: true,
    role: {
      roleId: role.id,
      roleName: role.roleName,
      description: role.description,
      isSystem: role.isSystem,
      creationDate: formatDisplayDate(role.creationDate),
      lastChangeDate: formatDisplayDate(role.lastChangeDate),
    },
  });
}

async function handleSwitch2fa(req, res, access) {
  if (!requirePermission(access, res, PERM.MANAGE_2FA)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const login = String(body.login || "").trim();
  if (!login) {
    json(res, 400, { error: "LOGIN_REQUIRED" });
    return;
  }

  const db = access.db;
  const user = getUserByUsernameInsensitive(db, login);
  if (!user) {
    json(res, 404, { error: "USER_NOT_FOUND" });
    return;
  }

  const explicit = body.enabled;
  user.twoFactorEnabled =
    typeof explicit === "boolean" ? explicit : !user.twoFactorEnabled;
  user.twoFactorStatus = user.twoFactorEnabled ? "Enabled" : "Disabled";
  user.updatedAt = nowIso();

  appendAudit(db, {
    actor: access.auth.username,
    action: "SWITCH_2FA",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: { enabled: user.twoFactorEnabled },
  });

  saveCoreDb(db);
  json(res, 200, {
    ok: true,
    login: user.username,
    status2fa: user.twoFactorStatus,
  });
}

async function handleResetUserPassword(req, res, access) {
  if (!requirePermission(access, res, PERM.RESET_USER_PASSWORD)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const login = String(body.login || "").trim();
  if (!login) {
    json(res, 400, { error: "LOGIN_REQUIRED" });
    return;
  }

  const db = access.db;
  const user = getUserByUsernameInsensitive(db, login);
  if (!user) {
    json(res, 404, { error: "USER_NOT_FOUND" });
    return;
  }

  let password = String(body.newPassword || "").trim();
  if (password) {
    const errors = checkPasswordPolicy(password);
    if (errors.length) {
      json(res, 400, { error: "PASSWORD_POLICY_FAILED", details: errors });
      return;
    }
  } else {
    password = generateStrongPassword();
  }

  const newHash = hashPasswordScrypt(password);
  user.passwordHash = newHash;
  user.passwordHistory = [newHash, ...(user.passwordHistory || [])].slice(0, 5);
  user.mustChangePassword = true;
  user.failedAttempts = 0;
  user.failedAt = [];
  user.lockedUntil = null;
  user.updatedAt = nowIso();

  deactivateAllSessionsForUser(user);
  revokeTokenMapsForUser(user.username);

  appendAudit(db, {
    actor: access.auth.username,
    action: "RESET_PASSWORD",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: {},
  });

  saveCoreDb(db);

  json(res, 200, {
    ok: true,
    login: user.username,
    generatedPassword: body.newPassword ? null : password,
  });
}

async function handleFlushUserIps(req, res, access) {
  if (!requirePermission(access, res, PERM.FLUSH_USER_IPS)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const login = String(body.login || "").trim();
  if (!login) {
    json(res, 400, { error: "LOGIN_REQUIRED" });
    return;
  }

  const db = access.db;
  const user = getUserByUsernameInsensitive(db, login);
  if (!user) {
    json(res, 404, { error: "USER_NOT_FOUND" });
    return;
  }

  user.loginIpHistory = [];
  user.updatedAt = nowIso();

  appendAudit(db, {
    actor: access.auth.username,
    action: "FLUSH_IPS",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: {},
  });

  saveCoreDb(db);
  json(res, 200, { ok: true, login: user.username });
}

async function handleToggleUserStatus(req, res, access) {
  if (!requirePermission(access, res, PERM.LOCK_UNLOCK_USER)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const login = String(body.login || "").trim();
  if (!login) {
    json(res, 400, { error: "LOGIN_REQUIRED" });
    return;
  }

  const db = access.db;
  const user = getUserByUsernameInsensitive(db, login);
  if (!user) {
    json(res, 404, { error: "USER_NOT_FOUND" });
    return;
  }

  const requested = String(body.status || "").toUpperCase();
  const nextStatus = requested === "ACTIVE" || requested === "DISABLED"
    ? requested
    : user.status === "ACTIVE"
      ? "DISABLED"
      : "ACTIVE";

  if (user.username === "root" && nextStatus === "DISABLED") {
    json(res, 400, { error: "ROOT_CANNOT_BE_DISABLED" });
    return;
  }

  user.status = nextStatus;
  user.updatedAt = nowIso();

  if (nextStatus === "DISABLED") {
    deactivateAllSessionsForUser(user);
    revokeTokenMapsForUser(user.username);
  }

  appendAudit(db, {
    actor: access.auth.username,
    action: nextStatus === "DISABLED" ? "LOCK_USER" : "UNLOCK_USER",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: { status: nextStatus },
  });

  saveCoreDb(db);
  json(res, 200, { ok: true, login: user.username, status: user.status });
}

async function handleDeleteUser(req, res, access) {
  if (!requirePermission(access, res, PERM.DELETE_USER)) return;

  let body;
  try {
    body = await parseJsonBody(req);
  } catch (_err) {
    json(res, 400, { error: "INVALID_JSON" });
    return;
  }

  const login = String(body.login || "").trim();
  if (!login) {
    json(res, 400, { error: "LOGIN_REQUIRED" });
    return;
  }

  if (login.toLowerCase() === "root") {
    json(res, 400, { error: "ROOT_CANNOT_BE_DELETED" });
    return;
  }

  if (login.toLowerCase() === access.auth.username.toLowerCase()) {
    json(res, 400, { error: "SELF_DELETE_NOT_ALLOWED" });
    return;
  }

  const db = access.db;
  const idx = db.users.findIndex((u) => u.username.toLowerCase() === login.toLowerCase());
  if (idx < 0) {
    json(res, 404, { error: "USER_NOT_FOUND" });
    return;
  }

  const [user] = db.users.splice(idx, 1);
  revokeTokenMapsForUser(user.username);

  appendAudit(db, {
    actor: access.auth.username,
    action: "DELETE_USER",
    objectType: "User",
    objectId: user.username,
    objectName: user.username,
    details: {},
  });

  saveCoreDb(db);
  json(res, 200, { ok: true, login: user.username });
}

function sessionDurationMs(session) {
  const start = new Date(session.issuedAt || 0).getTime();
  const endCandidate = session.lastSeenAt || session.expiresAt;
  const end = new Date(endCandidate || 0).getTime();
  if (!Number.isFinite(start) || !Number.isFinite(end)) return 0;
  return Math.max(0, end - start);
}

function findLastModifiedBy(db, login) {
  const entry = db.audit.find(
    (a) =>
      String(a.objectType || "").toLowerCase() === "user" &&
      String(a.objectName || "").toLowerCase() === String(login || "").toLowerCase() &&
      String(a.action || "").toUpperCase() !== "LOGIN"
  );
  return entry ? entry.actor : "system";
}

function readUserDetails(db, login) {
  const user = getUserByUsernameInsensitive(db, login);
  if (!user) return null;

  const roleDetails = user.roleIds
    .map((id) => findRoleById(db, id))
    .filter(Boolean)
    .map((r) => ({ id: r.id, roleName: r.roleName, isSystem: r.isSystem }));

  const sessions = Array.isArray(user.loginSessions) ? user.loginSessions : [];
  const durations = sessions.map(sessionDurationMs);
  const totalMs = durations.reduce((a, b) => a + b, 0);
  const avgMs = durations.length ? Math.floor(totalMs / durations.length) : 0;
  const lastSession = sessions[0] || null;

  const common = {
    login: user.username,
    email: user.email,
    comment: user.comment,
    status: user.status,
    userLevel: user.userLevel,
    includeFutureBanks: user.includeFutureBanks,
    roles: roleDetails,
    scope: {
      clusterIds: user.clusterIds,
      subcasinoIds: user.subcasinoIds,
      bankIds: user.bankIds,
    },
    createdAt: formatDisplayDate(user.createdAt),
    lastUpdate: formatDisplayDate(user.updatedAt),
    lastLoginAt: formatDisplayDate(user.lastLoginAt),
    status2fa: user.twoFactorEnabled ? "Enabled" : "Disabled",
    userInfo: {
      roles: roleDetails.map((r) => r.roleName).join(", "),
      email: user.email,
      comment: user.comment,
      lastModifiedBy: findLastModifiedBy(db, user.username),
    },
    activity: {
      numberOfSessions: sessions.length,
      lastLogDate: formatDisplayDate(user.lastLoginAt),
      lastSessionLength: formatDurationMs(lastSession ? sessionDurationMs(lastSession) : 0),
      ipHostname: user.lastLoginIp || (lastSession ? lastSession.ip : ""),
      totalTime: formatDurationMs(totalMs),
      averageLength: formatDurationMs(avgMs),
    },
  };

  const sessionInfo = {
    login: user.username,
    sessions: sessions.map((s) => ({
      sessionId: s.sessionId,
      active: !!s.active,
      issuedAt: formatDisplayDate(s.issuedAt),
      expiresAt: formatDisplayDate(s.expiresAt),
      ip: s.ip,
      userAgent: s.userAgent,
      lastSeenAt: formatDisplayDate(s.lastSeenAt),
      length: formatDurationMs(sessionDurationMs(s)),
    })),
  };

  const ipsInfo = {
    login: user.username,
    ips: (user.loginIpHistory || []).map((x) => ({
      ip: x.ip,
      lastSeenAt: formatDisplayDate(x.lastSeenAt),
      count: Number(x.count || 0),
    })),
  };

  return { user, common, sessionInfo, ipsInfo };
}

function routeReports(req, res, urlObj, access) {
  if (req.method === "GET" && urlObj.pathname === "/cm/reports/userList") {
    if (!requirePermission(access, res, PERM.VIEW_USER_LIST)) return;
    const payload = queryUserList(access.db, urlObj);
    writeMirrorSnapshot("userList", payload.sourceTable, payload.filters, payload.rows);
    json(res, 200, payload);
    return;
  }

  if (req.method === "GET" && urlObj.pathname === "/cm/reports/roleList") {
    if (!requirePermission(access, res, PERM.VIEW_ROLE_LIST)) return;
    const payload = queryRoleList(access.db);
    writeMirrorSnapshot("roleList", payload.sourceTable, {}, payload.rows);
    json(res, 200, payload);
    return;
  }

  try {
    if (req.method === "GET" && urlObj.pathname === "/cm/reports/playerSearch") {
      if (!requirePermission(access, res, PERM.VIEW_PLAYER_SEARCH)) return;
      const payload = queryPlayerSearch(access, urlObj);
      writeMirrorSnapshot("playerSearch", payload.sourceTable, payload.filters, payload.rows);
      json(res, 200, payload);
      return;
    }

    if (req.method === "GET" && urlObj.pathname === "/cm/reports/bankList") {
      if (!requirePermission(access, res, PERM.VIEW_BANK_LIST)) return;
      const limit = toSafeInt(urlObj.searchParams.get("limit"), 50, 1, 200);
      const rows = runCql(
        `PAGING OFF; SELECT key FROM rcasinoscks.bankinfocf LIMIT ${limit};`
      ).map((r) => ({ bankId: r.key }));
      const payload = {
        reportId: "bankList",
        sourceTable: "rcasinoscks.bankinfocf",
        filters: { limit },
        count: rows.length,
        rows,
      };
      writeMirrorSnapshot("bankList", payload.sourceTable, payload.filters, rows);
      json(res, 200, payload);
      return;
    }

    if (req.method === "GET" && urlObj.pathname === "/cm/reports/transactions") {
      if (!requirePermission(access, res, PERM.VIEW_TRANSACTIONS)) return;
      const extId = String(urlObj.searchParams.get("extId") || "").trim();
      if (!validateExtId(extId)) {
        json(res, 400, { error: "INVALID_EXT_ID" });
        return;
      }
      const limit = toSafeInt(urlObj.searchParams.get("limit"), 20, 1, 200);
      const rows = runCql(
        `PAGING OFF; SELECT bucket, startdate, key, transactionid, extid FROM rcasinoscks.paymenttransactioncf2 WHERE extid='${extId}' LIMIT ${limit};`
      );
      const payload = {
        reportId: "transactions",
        sourceTable: "rcasinoscks.paymenttransactioncf2",
        filters: { extId, limit },
        count: rows.length,
        rows,
      };
      writeMirrorSnapshot("transactions", payload.sourceTable, payload.filters, rows);
      json(res, 200, payload);
      return;
    }

    if (req.method === "GET" && urlObj.pathname === "/cm/reports/gameSessionSearch") {
      if (!requirePermission(access, res, PERM.VIEW_GAME_SESSION)) return;
      const gameSessionId = toSafeInt(
        urlObj.searchParams.get("gameSessionId"),
        -1,
        1,
        Number.MAX_SAFE_INTEGER
      );
      if (gameSessionId < 0) {
        json(res, 400, { error: "INVALID_GAME_SESSION_ID" });
        return;
      }
      const rows = runCql(
        `PAGING OFF; SELECT key FROM rcasinoks.gamesessioncf WHERE key=${gameSessionId};`
      );
      const payload = {
        reportId: "gameSessionSearch",
        sourceTable: "rcasinoks.gamesessioncf",
        filters: { gameSessionId },
        count: rows.length,
        rows,
      };
      writeMirrorSnapshot("gameSessionSearch", payload.sourceTable, payload.filters, rows);
      json(res, 200, payload);
      return;
    }

    if (req.method === "GET" && urlObj.pathname === "/cm/reports/walletOperationAlerts") {
      if (!requirePermission(access, res, PERM.VIEW_WALLET_ALERTS)) return;
      const limit = toSafeInt(urlObj.searchParams.get("limit"), 20, 1, 100);
      const rows = runCql(`PAGING OFF; SELECT sid FROM rcasinoks.wopcf LIMIT ${limit};`);
      const payload = {
        reportId: "walletOperationAlerts",
        sourceTable: "rcasinoks.wopcf",
        filters: { limit },
        count: rows.length,
        rows,
      };
      writeMirrorSnapshot("walletOperationAlerts", payload.sourceTable, payload.filters, rows);
      json(res, 200, payload);
      return;
    }
  } catch (err) {
    json(res, 500, { error: "REPORT_QUERY_FAILED", details: String(err.message || err) });
    return;
  }

  json(res, 404, { error: "NOT_FOUND" });
}

bootstrapDefaultRoot();

const server = http.createServer(async (req, res) => {
  const urlObj = new URL(req.url, `http://${req.headers.host || "localhost"}`);

  if (req.method === "GET" && urlObj.pathname === "/health") {
    const mirror = loadMirrorDb();
    json(res, 200, {
      ok: true,
      service: "cm-module",
      time: nowIso(),
      cassandraContainer: CASSANDRA_CONTAINER,
      coreDbFile: CORE_FILE,
      mirrorDbFile: MIRROR_FILE,
      mirrorLastUpdate: mirror.lastUpdate || null,
    });
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm-auth/login") {
    await handleLogin(req, res);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm-auth/change-password") {
    const auth = requireAuth(req, res);
    if (!auth) return;
    await handleChangePassword(req, res, auth);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm-auth/logout") {
    const auth = requireAuth(req, res);
    if (!auth) return;
    revokeAllTokensForUser(auth.username);
    json(res, 200, { ok: true });
    return;
  }

  if (req.method === "GET" && urlObj.pathname === "/cm/meta/reports") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    json(res, 200, { items: filteredReportDefs(access) });
    return;
  }

  if (req.method === "GET" && urlObj.pathname === "/cm/meta/menu") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    const items = filterMenuTree(loadMenuItems(), access);
    json(res, 200, { items });
    return;
  }

  if (req.method === "GET" && urlObj.pathname === "/cm/meta/user-management") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    json(res, 200, buildUserManagementMeta(access.db, access));
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/createUser") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleCreateUser(req, res, access);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/editUser") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleEditUser(req, res, access);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/createRole") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleCreateRole(req, res, access);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/switch2fa") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleSwitch2fa(req, res, access);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/resetUserPassword") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleResetUserPassword(req, res, access);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/flushUserIps") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleFlushUserIps(req, res, access);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/toggleUserStatus") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleToggleUserStatus(req, res, access);
    return;
  }

  if (req.method === "POST" && urlObj.pathname === "/cm/actions/deleteUser") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    await handleDeleteUser(req, res, access);
    return;
  }

  const userCommonMatch = urlObj.pathname.match(/^\/cm\/users\/([^/]+)\/common-info$/);
  const userSessionMatch = urlObj.pathname.match(/^\/cm\/users\/([^/]+)\/session-info$/);
  const userIpsMatch = urlObj.pathname.match(/^\/cm\/users\/([^/]+)\/ips-info$/);
  const playerSummaryMatch = urlObj.pathname.match(/^\/cm\/players\/(\d+)\/(\d+)\/summary$/);
  const playerGameInfoMatch = urlObj.pathname.match(/^\/cm\/players\/(\d+)\/(\d+)\/game-info$/);
  const playerPaymentMatch = urlObj.pathname.match(/^\/cm\/players\/(\d+)\/(\d+)\/payment-detail$/);
  const playerBonusMatch = urlObj.pathname.match(/^\/cm\/players\/(\d+)\/(\d+)\/bonus-detail$/);
  const playerFrbonusMatch = urlObj.pathname.match(/^\/cm\/players\/(\d+)\/(\d+)\/frbonus-detail$/);
  const playerHistoryMatch = urlObj.pathname.match(/^\/cm\/players\/(\d+)\/(\d+)\/change-history$/);
  const playerActionMatch = urlObj.pathname.match(/^\/cm\/players\/(\d+)\/(\d+)\/actions\/([^/]+)$/);

  if (req.method === "GET" && (userCommonMatch || userSessionMatch || userIpsMatch)) {
    const access = requireReportAccess(req, res);
    if (!access) return;

    if (userCommonMatch && !requirePermission(access, res, PERM.VIEW_USER_LIST)) return;
    if (userSessionMatch && !requirePermission(access, res, PERM.VIEW_USER_SESSIONS)) return;
    if (userIpsMatch && !requirePermission(access, res, PERM.VIEW_USER_IPS)) return;

    const encodedLogin =
      (userCommonMatch && userCommonMatch[1]) ||
      (userSessionMatch && userSessionMatch[1]) ||
      (userIpsMatch && userIpsMatch[1]);

    const login = decodeURIComponent(encodedLogin);
    const details = readUserDetails(access.db, login);
    if (!details) {
      json(res, 404, { error: "USER_NOT_FOUND" });
      return;
    }

    if (userCommonMatch) {
      json(res, 200, details.common);
      return;
    }

    if (userSessionMatch) {
      json(res, 200, details.sessionInfo);
      return;
    }

    json(res, 200, details.ipsInfo);
    return;
  }

  if (
    req.method === "GET" &&
    (playerSummaryMatch ||
      playerGameInfoMatch ||
      playerPaymentMatch ||
      playerBonusMatch ||
      playerFrbonusMatch ||
      playerHistoryMatch)
  ) {
    const access = requireReportAccess(req, res);
    if (!access) return;
    if (!requirePermission(access, res, PERM.VIEW_PLAYER_SEARCH)) return;

    const match =
      playerSummaryMatch ||
      playerGameInfoMatch ||
      playerPaymentMatch ||
      playerBonusMatch ||
      playerFrbonusMatch ||
      playerHistoryMatch;

    const bankId = Number(match[1]);
    const accountId = Number(match[2]);
    const payload = buildPlayerSummaryPayload(access.db, bankId, accountId);
    if (!payload) {
      json(res, 404, { error: "PLAYER_NOT_FOUND" });
      return;
    }

    if (playerSummaryMatch) {
      json(res, 200, payload);
      return;
    }
    if (playerGameInfoMatch) {
      const gameInfo = buildPlayerGameInfoRows(payload, {
        dateFrom: urlObj.searchParams.get("dateFrom"),
        dateTo: urlObj.searchParams.get("dateTo"),
        playerMode: urlObj.searchParams.get("playerMode"),
        platform: urlObj.searchParams.get("platform"),
        gameType: urlObj.searchParams.get("gameType"),
        isJackpot: urlObj.searchParams.get("isJackpot"),
        showBySessions: urlObj.searchParams.get("showBySessions"),
      });
      json(res, 200, {
        reportId: "playerGameInfoDetail",
        bankId,
        accountId,
        mode: gameInfo.mode,
        count: gameInfo.rows.length,
        rows: gameInfo.rows,
      });
      return;
    }
    if (playerPaymentMatch) {
      json(res, 200, {
        reportId: "playerPaymentDetail",
        bankId,
        accountId,
        count: payload.paymentRows.length,
        rows: payload.paymentRows,
      });
      return;
    }
    if (playerBonusMatch) {
      json(res, 200, {
        reportId: "playerBonusDetail",
        bankId,
        accountId,
        count: payload.bonusRows.length,
        rows: payload.bonusRows,
      });
      return;
    }
    if (playerFrbonusMatch) {
      json(res, 200, {
        reportId: "playerFrbonusDetail",
        bankId,
        accountId,
        count: payload.frbonusRows.length,
        rows: payload.frbonusRows,
      });
      return;
    }

    json(res, 200, {
      reportId: "objectChangeHistory",
      bankId,
      accountId,
      count: payload.historyRows.length,
      rows: payload.historyRows,
    });
    return;
  }

  if (req.method === "POST" && playerActionMatch) {
    const access = requireReportAccess(req, res);
    if (!access) return;
    if (!requirePermission(access, res, PERM.VIEW_PLAYER_SEARCH)) return;
    if (!access.capabilities || !access.capabilities.canManageAny) {
      json(res, 403, { error: "FORBIDDEN" });
      return;
    }

    const bankId = Number(playerActionMatch[1]);
    const accountId = Number(playerActionMatch[2]);
    const actionId = String(playerActionMatch[3] || "").trim();
    let actionBody = {};
    try {
      actionBody = await parseJsonBody(req);
    } catch (_err) {
      json(res, 400, { error: "INVALID_JSON" });
      return;
    }

    const account = readAccountInfo(accountId, new Map());
    if (!account) {
      json(res, 404, { error: "PLAYER_NOT_FOUND" });
      return;
    }
    if (Number(account.bankId || bankId) !== bankId) {
      json(res, 400, { error: "BANK_ACCOUNT_MISMATCH" });
      return;
    }

    if (actionId === "lockAccount") {
      account.locked = !account.locked;
      saveAccountObject(accountId, account);
      appendAudit(access.db, {
        actor: access.auth.username,
        action: account.locked ? "LOCK_PLAYER" : "UNLOCK_PLAYER",
        objectType: "Player",
        objectId: String(accountId),
        objectName: String(account.externalId || account.nickName || accountId),
        details: { bankId },
      });
      saveCoreDb(access.db);
      json(res, 200, { ok: true, action: actionId, locked: !!account.locked });
      return;
    }

    if (actionId === "makeTester") {
      account.testUser = !account.testUser;
      saveAccountObject(accountId, account);
      appendAudit(access.db, {
        actor: access.auth.username,
        action: account.testUser ? "SET_TESTER" : "UNSET_TESTER",
        objectType: "Player",
        objectId: String(accountId),
        objectName: String(account.externalId || account.nickName || accountId),
        details: { bankId },
      });
      saveCoreDb(access.db);
      json(res, 200, { ok: true, action: actionId, tester: !!account.testUser });
      return;
    }

    if (actionId === "awardBonus") {
      try {
        const result = upsertBonusAward(
          bankId,
          accountId,
          account,
          actionBody,
          access.auth.username
        );
        if (result.error) {
          json(res, 400, result);
          return;
        }
        appendAudit(access.db, {
          actor: access.auth.username,
          action: "AWARD_BONUS",
          objectType: "Player",
          objectId: String(accountId),
          objectName: String(account.externalId || account.nickName || accountId),
          details: {
            bankId,
            bonusId: result.bonusId,
            amount: result.amount,
            rollover: result.rollover,
          },
        });
        saveCoreDb(access.db);
        json(res, 200, Object.assign({ action: actionId }, result));
        return;
      } catch (err) {
        json(res, 500, { error: "AWARD_BONUS_FAILED", details: String(err.message || err) });
        return;
      }
    }

    if (actionId === "awardFRBonus") {
      try {
        const result = upsertFrbonusAward(
          bankId,
          accountId,
          account,
          actionBody,
          access.auth.username
        );
        if (result.error) {
          json(res, 400, result);
          return;
        }
        appendAudit(access.db, {
          actor: access.auth.username,
          action: "AWARD_FRBONUS",
          objectType: "Player",
          objectId: String(accountId),
          objectName: String(account.externalId || account.nickName || accountId),
          details: {
            bankId,
            frbonusId: result.frbonusId,
            rounds: result.rounds,
            frChips: result.frChips,
          },
        });
        saveCoreDb(access.db);
        json(res, 200, Object.assign({ action: actionId }, result));
        return;
      } catch (err) {
        json(res, 500, { error: "AWARD_FRBONUS_FAILED", details: String(err.message || err) });
        return;
      }
    }

    json(res, 400, { error: "UNSUPPORTED_PLAYER_ACTION", actionId });
    return;
  }

  if (req.method === "GET" && urlObj.pathname === "/cm/history/object-change") {
    const access = requireReportAccess(req, res);
    if (!access) return;
    if (!requirePermission(access, res, PERM.VIEW_OBJECT_HISTORY)) return;

    const objectType = String(urlObj.searchParams.get("objectType") || "").toLowerCase();
    const objectId = String(urlObj.searchParams.get("objectId") || "").trim();
    const objectName = String(urlObj.searchParams.get("objectName") || "").trim().toLowerCase();

    const rows = access.db.audit.filter((a) => {
      if (objectType && String(a.objectType || "").toLowerCase() !== objectType) return false;
      if (objectId && String(a.objectId || "") !== objectId) return false;
      if (objectName && !String(a.objectName || "").toLowerCase().includes(objectName)) return false;
      return true;
    });

    json(res, 200, {
      count: rows.length,
      rows: rows.slice(0, 200).map((r) => ({
        at: formatDisplayDate(r.at),
        actor: r.actor,
        action: r.action,
        objectType: r.objectType,
        objectId: r.objectId,
        objectName: r.objectName,
        details: r.details,
      })),
    });
    return;
  }

  if (urlObj.pathname.startsWith("/cm/reports/")) {
    const access = requireReportAccess(req, res);
    if (!access) return;
    routeReports(req, res, urlObj, access);
    return;
  }

  if (serveStatic(req, res, urlObj.pathname)) return;
  text(res, 404, "Not Found", "text/plain; charset=utf-8");
});

server.listen(PORT, () => {
  process.stdout.write(
    `[cm-module] listening on :${PORT}, core=${CORE_FILE}, mirror=${MIRROR_FILE}, cassandra=${CASSANDRA_CONTAINER}\n`
  );
});
