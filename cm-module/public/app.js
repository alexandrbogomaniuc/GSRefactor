(function () {
  const state = {
    token: null,
    username: null,
    reports: [],
    reportMap: {},
    menu: [],
    umMeta: null,
    capabilities: {},
    tabs: [],
    activeTabId: null,
    userCommonCache: {},
    userCommonViewByLogin: {},
    lastUserRows: [],
    editOriginalLogin: null,
    lastPlayerRows: [],
    playerSummaryCache: {},
    playerSummaryViewByKey: {},
    playerGameFilterByKey: {},
  };

  const els = {
    loginScreen: document.getElementById("login-screen"),
    passwordScreen: document.getElementById("password-screen"),
    appScreen: document.getElementById("app-screen"),

    loginForm: document.getElementById("login-form"),
    loginUsername: document.getElementById("login-username"),
    loginPassword: document.getElementById("login-password"),
    loginError: document.getElementById("login-error"),

    passwordForm: document.getElementById("password-form"),
    oldPassword: document.getElementById("old-password"),
    newPassword: document.getElementById("new-password"),
    passwordError: document.getElementById("password-error"),
    passwordSuccess: document.getElementById("password-success"),

    helloUser: document.getElementById("hello-user"),
    logoutBtn: document.getElementById("logout-btn"),

    statusService: document.getElementById("status-service"),
    statusCassandra: document.getElementById("status-cassandra"),
    statusUser: document.getElementById("status-user"),
    statusAuth: document.getElementById("status-auth"),
    statusCoreDb: document.getElementById("status-core-db"),
    statusMirrorDb: document.getElementById("status-mirror-db"),

    menuTree: document.getElementById("menu-tree"),
    workspaceTabs: document.getElementById("workspace-tabs"),
    workspaceSubtitle: document.getElementById("workspace-subtitle"),

    panelUserList: document.getElementById("panel-user-list"),
    panelUserCommon: document.getElementById("panel-user-common"),
    panelCreateUser: document.getElementById("panel-create-user"),
    panelRoleList: document.getElementById("panel-role-list"),
    panelCreateRole: document.getElementById("panel-create-role"),
    panelPlayerSearch: document.getElementById("panel-player-search"),
    panelPlayerSummary: document.getElementById("panel-player-summary"),
    panelReports: document.getElementById("panel-reports"),

    // user list
    ufLevel: document.getElementById("uf-level"),
    userSearchBtn: document.getElementById("user-search-btn"),
    userExportBtn: document.getElementById("user-export-btn"),
    userListMeta: document.getElementById("user-list-meta"),
    userListError: document.getElementById("user-list-error"),
    userListTable: document.getElementById("user-list-table"),

    // user common
    ucViewBtn: document.getElementById("uc-view-btn"),
    ucViewMenu: document.getElementById("uc-view-menu"),
    ucActionsBtn: document.getElementById("uc-actions-btn"),
    ucActionsMenu: document.getElementById("uc-actions-menu"),
    ucActionsDropdown: document.getElementById("uc-actions-dropdown"),
    ucMessage: document.getElementById("uc-message"),
    ucError: document.getElementById("uc-error"),
    ucCommonView: document.getElementById("uc-common-view"),
    ucTableView: document.getElementById("uc-table-view"),
    ucDataTable: document.getElementById("uc-data-table"),
    ucLogin: document.getElementById("uc-login"),
    ucStatus: document.getElementById("uc-status"),
    ucRoles: document.getElementById("uc-roles"),
    ucEmail: document.getElementById("uc-email"),
    ucComment: document.getElementById("uc-comment"),
    ucLastModified: document.getElementById("uc-last-modified"),
    ucNumberSessions: document.getElementById("uc-number-sessions"),
    ucLastLogDate: document.getElementById("uc-last-log-date"),
    ucLastSessionLength: document.getElementById("uc-last-session-length"),
    ucIpHostname: document.getElementById("uc-ip-hostname"),
    ucTotalTime: document.getElementById("uc-total-time"),
    ucAverageLength: document.getElementById("uc-average-length"),
    ucEditBtn: document.getElementById("uc-edit-btn"),

    // create user
    cuLogin: document.getElementById("cu-login"),
    cuEmail: document.getElementById("cu-email"),
    cuComment: document.getElementById("cu-comment"),
    cuGeneral: document.getElementById("cu-general"),
    cuIncludeFuture: document.getElementById("cu-include-future"),
    cuPassword: document.getElementById("cu-password"),
    cuRoles: document.getElementById("cu-roles"),
    cuScope: document.getElementById("cu-scope"),
    cuClusters: document.getElementById("cu-clusters"),
    cuSubcasinos: document.getElementById("cu-subcasinos"),
    cuBanks: document.getElementById("cu-banks"),
    createUserBtn: document.getElementById("create-user-btn"),
    createUserError: document.getElementById("create-user-error"),
    createUserSuccess: document.getElementById("create-user-success"),

    // role list
    roleRefreshBtn: document.getElementById("role-refresh-btn"),
    roleListMeta: document.getElementById("role-list-meta"),
    roleListError: document.getElementById("role-list-error"),
    roleListTable: document.getElementById("role-list-table"),

    // create role
    crName: document.getElementById("cr-name"),
    crDescription: document.getElementById("cr-description"),
    crNonrestricted: document.getElementById("cr-nonrestricted"),
    crScope: document.getElementById("cr-scope"),
    crClusters: document.getElementById("cr-clusters"),
    crSubcasinos: document.getElementById("cr-subcasinos"),
    crBanks: document.getElementById("cr-banks"),
    crPermissions: document.getElementById("cr-permissions"),
    createRoleBtn: document.getElementById("create-role-btn"),
    createRoleError: document.getElementById("create-role-error"),
    createRoleSuccess: document.getElementById("create-role-success"),

    // reports
    reportSelect: document.getElementById("report-select"),
    reportForm: document.getElementById("report-form"),
    runReportBtn: document.getElementById("run-report-btn"),
    reportError: document.getElementById("report-error"),
    reportMeta: document.getElementById("report-meta"),
    reportTable: document.getElementById("report-table"),

    // player search
    psCluster: document.getElementById("ps-cluster"),
    psSubcasinoList: document.getElementById("ps-subcasino-list"),
    psBankList: document.getElementById("ps-bank-list"),
    psNickname: document.getElementById("ps-nickname"),
    psExtid: document.getElementById("ps-extid"),
    psAccountId: document.getElementById("ps-account-id"),
    psRegAfter: document.getElementById("ps-reg-after"),
    psRegBefore: document.getElementById("ps-reg-before"),
    psAccountStatus: document.getElementById("ps-account-status"),
    psMainPerPage: document.getElementById("ps-main-per-page"),
    psFuzzySearch: document.getElementById("ps-fuzzy-search"),
    playerSearchBtn: document.getElementById("player-search-btn"),
    playerExportBtn: document.getElementById("player-export-btn"),
    playerSearchMeta: document.getElementById("player-search-meta"),
    playerSearchError: document.getElementById("player-search-error"),
    playerSearchTable: document.getElementById("player-search-table"),

    // player summary
    psmActionsBtn: document.getElementById("psm-actions-btn"),
    psmActionsMenu: document.getElementById("psm-actions-menu"),
    psmViewBtn: document.getElementById("psm-view-btn"),
    psmViewMenu: document.getElementById("psm-view-menu"),
    psmMessage: document.getElementById("psm-message"),
    psmError: document.getElementById("psm-error"),
    psmSummaryView: document.getElementById("psm-summary-view"),
    psmGameFilterWrap: document.getElementById("psm-game-filter-wrap"),
    psmGameFilterForm: document.getElementById("psm-game-filter-form"),
    psmDateFrom: document.getElementById("psm-date-from"),
    psmDateTo: document.getElementById("psm-date-to"),
    psmPlayerMode: document.getElementById("psm-player-mode"),
    psmPlatform: document.getElementById("psm-platform"),
    psmGameType: document.getElementById("psm-game-type"),
    psmIsJackpot: document.getElementById("psm-is-jackpot"),
    psmShowBySessions: document.getElementById("psm-show-by-sessions"),
    psmGameSearchBtn: document.getElementById("psm-game-search-btn"),
    psmTableView: document.getElementById("psm-table-view"),
    psmDataTable: document.getElementById("psm-data-table"),
    psmLogin: document.getElementById("psm-login"),
    psmStatus: document.getElementById("psm-status"),
    psmPlayerName: document.getElementById("psm-player-name"),
    psmExtid: document.getElementById("psm-extid"),
    psmCluster: document.getElementById("psm-cluster"),
    psmSubcasino: document.getElementById("psm-subcasino"),
    psmBank: document.getElementById("psm-bank"),
    psmTester: document.getElementById("psm-tester"),
    psmBalance: document.getElementById("psm-balance"),
    psmTotalBets: document.getElementById("psm-total-bets"),
    psmTotalPayout: document.getElementById("psm-total-payout"),
    psmGameRevenue: document.getElementById("psm-game-revenue"),
    psmCurrency: document.getElementById("psm-currency"),
    psmAccountId: document.getElementById("psm-account-id"),
    psmRegDate: document.getElementById("psm-reg-date"),
    psmType: document.getElementById("psm-type"),
    psmLastLog: document.getElementById("psm-last-log"),
    psmRounds: document.getElementById("psm-rounds"),
    psmGameSessions: document.getElementById("psm-game-sessions"),

    // award bonus modal
    awardBonusModal: document.getElementById("award-bonus-modal"),
    awardBonusOverlay: document.getElementById("award-bonus-overlay"),
    awardBonusTitle: document.getElementById("award-bonus-title"),
    awardBonusSubmitBtn: document.getElementById("award-bonus-submit-btn"),
    awardBonusCancelBtn: document.getElementById("award-bonus-cancel-btn"),
    abType: document.getElementById("ab-type"),
    abAmount: document.getElementById("ab-amount"),
    abRolloverMultiplier: document.getElementById("ab-rollover-multiplier"),
    abRollover: document.getElementById("ab-rollover"),
    abCapMultiplier: document.getElementById("ab-cap-multiplier"),
    abMaxWinCap: document.getElementById("ab-max-win-cap"),
    abStartTime: document.getElementById("ab-start-time"),
    abExpirationTime: document.getElementById("ab-expiration-time"),
    abGameLimitType: document.getElementById("ab-game-limit-type"),
    abGameList: document.getElementById("ab-game-list"),
    abDescription: document.getElementById("ab-description"),
    abReleasedType: document.getElementById("ab-released-type"),

    // award frbonus modal
    awardFrbonusModal: document.getElementById("award-frbonus-modal"),
    awardFrbonusOverlay: document.getElementById("award-frbonus-overlay"),
    awardFrbonusTitle: document.getElementById("award-frbonus-title"),
    awardFrbonusSubmitBtn: document.getElementById("award-frbonus-submit-btn"),
    awardFrbonusCancelBtn: document.getElementById("award-frbonus-cancel-btn"),
    afbRounds: document.getElementById("afb-rounds"),
    afbBetType: document.getElementById("afb-bet-type"),
    afbGameLimitType: document.getElementById("afb-game-limit-type"),
    afbGameList: document.getElementById("afb-game-list"),
    afbSingleGame: document.getElementById("afb-single-game"),
    afbStartTime: document.getElementById("afb-start-time"),
    afbExpirationTime: document.getElementById("afb-expiration-time"),
    afbAwardDurationDays: document.getElementById("afb-award-duration-days"),
    afbFrChips: document.getElementById("afb-fr-chips"),
    afbMaxWinCap: document.getElementById("afb-max-win-cap"),
    afbDescription: document.getElementById("afb-description"),

    // edit modal
    editModal: document.getElementById("edit-user-modal"),
    editCancelOverlay: document.getElementById("edit-user-cancel-overlay"),
    editSaveBtn: document.getElementById("edit-user-save-btn"),
    editCancelBtn: document.getElementById("edit-user-cancel-btn"),
    eumLogin: document.getElementById("eum-login"),
    eumEmail: document.getElementById("eum-email"),
    eumComment: document.getElementById("eum-comment"),
    eumGeneral: document.getElementById("eum-general"),
    eumIncludeFuture: document.getElementById("eum-include-future"),
    eumRoles: document.getElementById("eum-roles"),
    eumScope: document.getElementById("eum-scope"),
    eumClusters: document.getElementById("eum-clusters"),
    eumSubcasinos: document.getElementById("eum-subcasinos"),
    eumBanks: document.getElementById("eum-banks"),
  };

  const panelIds = [
    "panel-user-list",
    "panel-user-common",
    "panel-create-user",
    "panel-role-list",
    "panel-create-role",
    "panel-player-search",
    "panel-player-summary",
    "panel-reports",
  ];

  const VIEW_MENU_CONFIG = [
    { id: "common", label: "Common Info", capability: null },
    { id: "sessions", label: "Session Info", capability: "canViewSessionInfo" },
    { id: "ips", label: "IPs Info", capability: "canViewIpsInfo" },
    { id: "history", label: "Object change history", capability: "canViewHistory" },
  ];

  const PLAYER_SUMMARY_VIEW_CONFIG = [
    { id: "summary", label: "View summary info" },
    { id: "game", label: "View game info" },
    { id: "payments", label: "View payments detail" },
    { id: "bonus", label: "View bonus report info" },
    { id: "frbonus", label: "View fr bonus report info" },
    { id: "history", label: "Object change history" },
  ];

  function showScreen(name) {
    els.loginScreen.classList.remove("active");
    els.passwordScreen.classList.remove("active");
    els.appScreen.classList.remove("active");
    if (name === "login") els.loginScreen.classList.add("active");
    if (name === "password") els.passwordScreen.classList.add("active");
    if (name === "app") els.appScreen.classList.add("active");
  }

  async function api(path, options) {
    const opts = Object.assign(
      { headers: { "Content-Type": "application/json" } },
      options || {}
    );
    if (state.token) {
      opts.headers = Object.assign({}, opts.headers, {
        Authorization: `Bearer ${state.token}`,
      });
    }

    const res = await fetch(path, opts);
    const txt = await res.text();
    let body = {};
    try {
      body = txt ? JSON.parse(txt) : {};
    } catch (_err) {
      body = { raw: txt };
    }

    if (!res.ok) {
      const message = body.error || `HTTP_${res.status}`;
      const err = new Error(message);
      err.status = res.status;
      err.body = body;
      throw err;
    }
    return body;
  }

  function errorText(err) {
    if (!err) return "Unknown error";
    if (err.body) {
      try {
        return JSON.stringify(err.body);
      } catch (_err) {
        return String(err.message || err);
      }
    }
    return String(err.message || err);
  }

  function userStatusLabel(status) {
    return String(status || "").toUpperCase() === "ACTIVE" ? "ENABLED" : "DISABLED";
  }

  function clearMessages() {
    [
      els.loginError,
      els.passwordError,
      els.passwordSuccess,
      els.userListError,
      els.createUserError,
      els.createUserSuccess,
      els.roleListError,
      els.createRoleError,
      els.createRoleSuccess,
      els.reportError,
      els.ucError,
      els.ucMessage,
      els.playerSearchError,
      els.psmError,
      els.psmMessage,
    ].forEach((el) => {
      if (el) el.textContent = "";
    });
  }

  function option(label, value) {
    const o = document.createElement("option");
    o.value = String(value);
    o.textContent = String(label);
    return o;
  }

  function fillSelect(selectEl, items, opts) {
    const options = Object.assign({ includeAll: false, allLabel: "All", allValue: "" }, opts || {});
    selectEl.innerHTML = "";
    if (options.includeAll) {
      selectEl.appendChild(option(options.allLabel, options.allValue));
    }
    (items || []).forEach((item) => {
      selectEl.appendChild(option(item.title, item.id));
    });
  }

  function selectedInts(selectEl) {
    return Array.from(selectEl.selectedOptions)
      .map((o) => Number(o.value))
      .filter((n) => Number.isInteger(n));
  }

  function parseCsvIntsText(value) {
    return String(value || "")
      .split(",")
      .map((x) => Number(x.trim()))
      .filter((n) => Number.isInteger(n) && n > 0);
  }

  function toDateTimeLocal(ms) {
    let raw = Number(ms);
    if (!Number.isFinite(raw)) {
      const parsed = Date.parse(String(ms || ""));
      raw = Number.isFinite(parsed) ? parsed : Date.now();
    }
    const d = new Date(raw || Date.now());
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mm = String(d.getMinutes()).padStart(2, "0");
    return `${y}-${m}-${day}T${hh}:${mm}`;
  }

  function defaultGameFilterState() {
    const now = new Date();
    const start = new Date(now);
    start.setHours(0, 0, 0, 0);
    const end = new Date(now);
    end.setHours(23, 59, 59, 999);
    return {
      dateFrom: start.toISOString(),
      dateTo: end.toISOString(),
      playerMode: "0",
      platform: "-1",
      gameType: "-1",
      isJackpot: "-1",
      showBySessions: false,
    };
  }

  function setPanelsHidden() {
    panelIds.forEach((id) => {
      const el = document.getElementById(id);
      if (el) el.classList.add("hidden");
    });
  }

  function setActivePanel(panelId) {
    setPanelsHidden();
    const panel = document.getElementById(panelId);
    if (panel) panel.classList.remove("hidden");
  }

  function activeTab() {
    return state.tabs.find((t) => t.id === state.activeTabId) || null;
  }

  function renderTabs() {
    els.workspaceTabs.innerHTML = "";

    state.tabs.forEach((tab) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "workspace-tab";
      if (tab.id === state.activeTabId) btn.classList.add("active");
      btn.textContent = tab.title;
      btn.addEventListener("click", () => {
        state.activeTabId = tab.id;
        renderTabs();
        showActiveTab();
      });

      if (tab.closable) {
        const close = document.createElement("span");
        close.className = "workspace-tab-close";
        close.textContent = "×";
        close.addEventListener("click", (e) => {
          e.stopPropagation();
          closeTab(tab.id);
        });
        btn.appendChild(close);
      }

      els.workspaceTabs.appendChild(btn);
    });
  }

  function openTab(next) {
    const existing = state.tabs.find((t) => t.id === next.id);
    if (!existing) {
      state.tabs.push(next);
    }
    state.activeTabId = next.id;
    renderTabs();
    showActiveTab();
  }

  function closeTab(id) {
    const idx = state.tabs.findIndex((t) => t.id === id);
    if (idx < 0) return;
    state.tabs.splice(idx, 1);
    if (!state.tabs.length) {
      return;
    }
    if (state.activeTabId === id) {
      state.activeTabId = state.tabs[Math.max(0, idx - 1)].id;
    }
    renderTabs();
    showActiveTab();
  }

  async function showActiveTab() {
    clearMessages();
    const tab = activeTab();
    if (!tab) return;
    setActivePanel(tab.panelId);
    els.workspaceSubtitle.textContent = tab.subtitle || "";

    if (tab.type === "user-list") {
      await loadUserList();
      return;
    }

    if (tab.type === "user-common") {
      const login = tab.payload.login;
      if (!state.userCommonViewByLogin[login]) {
        state.userCommonViewByLogin[login] = "common";
      }
      await loadUserCommon(login, state.userCommonViewByLogin[login]);
      return;
    }

    if (tab.type === "create-user") {
      toggleCreateUserScope();
      return;
    }

    if (tab.type === "role-list") {
      await loadRoleList();
      return;
    }

    if (tab.type === "player-search") {
      await loadPlayerSearch();
      return;
    }

    if (tab.type === "player-summary") {
      const bankId = tab.payload.bankId;
      const accountId = tab.payload.accountId;
      const key = `${bankId}:${accountId}`;
      if (!state.playerSummaryViewByKey[key]) {
        state.playerSummaryViewByKey[key] = "summary";
      }
      await loadPlayerSummary(bankId, accountId, state.playerSummaryViewByKey[key]);
      return;
    }

    if (tab.type === "create-role") {
      toggleCreateRoleScope();
      return;
    }

    if (tab.type === "report") {
      if (tab.payload.reportId && state.reportMap[tab.payload.reportId]) {
        els.reportSelect.value = tab.payload.reportId;
        renderReportForm();
        await runReport();
      }
    }
  }

  function resetStateToLogin() {
    state.token = null;
    state.username = null;
    localStorage.removeItem("cm_token");
    localStorage.removeItem("cm_username");
    showScreen("login");
  }

  function assignTarget(node) {
    const href =
      Array.isArray(node.links) && node.links[0] && node.links[0].href
        ? String(node.links[0].href)
        : "";

    if (/\/api\/reports\/userList\/layout$/.test(href)) {
      node.target = { type: "user-list" };
    } else if (/\/api\/action\/createUser$/.test(href)) {
      node.target = { type: "create-user" };
    } else if (/\/api\/reports\/roleList\/complete$/.test(href)) {
      node.target = { type: "role-list" };
    } else if (/\/api\/action\/createRole$/.test(href)) {
      node.target = { type: "create-role" };
    } else if (/\/api\/reports\/playerSearch\/layout$/.test(href)) {
      node.target = { type: "player-search" };
    } else {
      const m = href.match(/\/api\/reports\/([^/]+)\//);
      if (m) {
        node.target = { type: "report", reportId: m[1] };
      }
    }

    if (Array.isArray(node.children)) {
      node.children.forEach(assignTarget);
    }
  }

  function renderMenuNode(node, depth) {
    const wrap = document.createElement("div");
    wrap.className = "menu-node";
    wrap.style.paddingLeft = `${depth * 12}px`;

    const titleBtn = document.createElement("button");
    titleBtn.type = "button";
    titleBtn.className = "menu-item";
    titleBtn.textContent = node.caption || "Untitled";
    wrap.appendChild(titleBtn);

    const children = Array.isArray(node.children) ? node.children : [];
    if (children.length) {
      titleBtn.classList.add("group");
      const childWrap = document.createElement("div");
      childWrap.className = "menu-children collapsed";
      children.forEach((c) => childWrap.appendChild(renderMenuNode(c, depth + 1)));
      wrap.appendChild(childWrap);
      titleBtn.addEventListener("click", () => {
        childWrap.classList.toggle("collapsed");
      });
      return wrap;
    }

    titleBtn.classList.add("leaf");
    if (node.target) {
      titleBtn.addEventListener("click", () => {
        openTarget(node.target, node.caption || "");
      });
    }
    return wrap;
  }

  function renderMenu() {
    els.menuTree.innerHTML = "";
    state.menu.forEach(assignTarget);
    state.menu.forEach((node) => {
      els.menuTree.appendChild(renderMenuNode(node, 0));
    });
  }

  function openTarget(target, caption) {
    if (!target) return;

    if (target.type === "user-list") {
      openTab({
        id: "tab-user-list",
        title: "User List",
        type: "user-list",
        panelId: "panel-user-list",
        subtitle: "",
        payload: {},
        closable: false,
      });
      return;
    }

    if (target.type === "create-user") {
      openTab({
        id: "tab-create-user",
        title: "Create User",
        type: "create-user",
        panelId: "panel-create-user",
        subtitle: "",
        payload: {},
        closable: false,
      });
      return;
    }

    if (target.type === "role-list") {
      openTab({
        id: "tab-role-list",
        title: "Role List",
        type: "role-list",
        panelId: "panel-role-list",
        subtitle: "",
        payload: {},
        closable: false,
      });
      return;
    }

    if (target.type === "create-role") {
      openTab({
        id: "tab-create-role",
        title: "Create Role",
        type: "create-role",
        panelId: "panel-create-role",
        subtitle: "",
        payload: {},
        closable: false,
      });
      return;
    }

    if (target.type === "player-search") {
      openTab({
        id: "tab-player-search",
        title: "Player Search",
        type: "player-search",
        panelId: "panel-player-search",
        subtitle: "",
        payload: {},
        closable: false,
      });
      return;
    }

    if (target.type === "report") {
      openTab({
        id: `tab-report-${target.reportId}`,
        title: caption || "Report",
        type: "report",
        panelId: "panel-reports",
        subtitle: "",
        payload: { reportId: target.reportId },
        closable: false,
      });
    }
  }

  function openUserCommonTab(login) {
    openTab({
      id: `tab-user-common-${login.toLowerCase()}`,
      title: "User Common Info",
      type: "user-common",
      panelId: "panel-user-common",
      subtitle: "",
      payload: { login },
      closable: true,
    });
  }

  function openPlayerSummaryTab(row) {
    const bankId = Number(row.bankId);
    const accountId = Number(row.accountId);
    if (!Number.isInteger(bankId) || !Number.isInteger(accountId)) return;
    openTab({
      id: `tab-player-summary-${bankId}-${accountId}`,
      title: "Summary Info",
      type: "player-summary",
      panelId: "panel-player-summary",
      subtitle: "",
      payload: {
        bankId,
        accountId,
        accountName: row.accountName || row.accountNickname || row.externalId || String(accountId),
      },
      closable: true,
    });
  }

  function renderSimpleTable(tableEl, rows, columns) {
    tableEl.innerHTML = "";
    if (!Array.isArray(rows) || !rows.length) {
      const tr = document.createElement("tr");
      const td = document.createElement("td");
      td.textContent = "No rows";
      tr.appendChild(td);
      tableEl.appendChild(tr);
      return;
    }

    const headers = columns && columns.length ? columns : Object.keys(rows[0]);

    const thead = document.createElement("thead");
    const htr = document.createElement("tr");
    headers.forEach((k) => {
      const th = document.createElement("th");
      th.textContent = k;
      htr.appendChild(th);
    });
    thead.appendChild(htr);
    tableEl.appendChild(thead);

    const tbody = document.createElement("tbody");
    rows.forEach((row) => {
      const tr = document.createElement("tr");
      headers.forEach((k) => {
        const td = document.createElement("td");
        td.textContent = row[k] == null ? "" : String(row[k]);
        tr.appendChild(td);
      });
      tbody.appendChild(tr);
    });
    tableEl.appendChild(tbody);
  }

  function renderUserListTable(rows) {
    els.userListTable.innerHTML = "";
    if (!rows.length) {
      renderSimpleTable(els.userListTable, [], ["#", "Login", "E-mail", "Roles"]);
      return;
    }

    const columns = [
      "#",
      "Login",
      "",
      "E-mail",
      "Roles",
      "Last update time",
      "Last login time",
      "Status",
      "Logged in",
      "Time since login",
      "Level",
      "2FA",
    ];

    const thead = document.createElement("thead");
    const htr = document.createElement("tr");
    columns.forEach((name) => {
      const th = document.createElement("th");
      th.textContent = name;
      htr.appendChild(th);
    });
    thead.appendChild(htr);
    els.userListTable.appendChild(thead);

    const tbody = document.createElement("tbody");
    rows.forEach((row) => {
      const tr = document.createElement("tr");

      const values = [
        row.rowNumber,
        row.login,
        "...",
        row.email,
        row.roles,
        row.lastUpdate,
        row.lastLoggedIn,
        userStatusLabel(row.status),
        row.loggedIn ? "Yes" : "No",
        row.timeSinceLogin,
        row.userLevel,
        String(row.status2fa || ""),
      ];

      values.forEach((value, idx) => {
        const td = document.createElement("td");
        if (idx === 1) {
          const link = document.createElement("button");
          link.type = "button";
          link.className = "link-cell";
          link.textContent = String(value);
          link.addEventListener("click", () => openUserCommonTab(row.login));
          td.appendChild(link);
        } else if (idx === 2) {
          const dots = document.createElement("button");
          dots.type = "button";
          dots.className = "dots-btn";
          dots.textContent = "⋮";
          dots.addEventListener("click", () => openUserCommonTab(row.login));
          td.appendChild(dots);
          td.classList.add("dots-cell");
        } else {
          td.textContent = value == null ? "" : String(value);
        }

        if (idx === 7) {
          td.classList.add(String(row.status).toUpperCase() === "ACTIVE" ? "status-enabled" : "status-disabled");
        }
        if (idx === 11) {
          td.classList.add(String(row.status2fa).toLowerCase() === "enabled" ? "status-enabled" : "status-disabled");
        }

        tr.appendChild(td);
      });

      tbody.appendChild(tr);
    });

    els.userListTable.appendChild(tbody);
  }

  async function loadUserList() {
    clearMessages();
    const params = new URLSearchParams();
    if (els.ufLevel.value) params.set("userLevel", els.ufLevel.value);

    try {
      const payload = await api(`/cm/reports/userList?${params.toString()}`, { method: "GET" });
      state.lastUserRows = payload.rows || [];
      renderUserListTable(state.lastUserRows);
      const ts = new Date();
      els.userListMeta.textContent = `Report generated at ${ts.toLocaleString("en-GB", { hour12: false })}`;
    } catch (err) {
      els.userListError.textContent = errorText(err);
    }
  }

  function exportUserListCsv() {
    const rows = state.lastUserRows || [];
    if (!rows.length) return;

    const headers = [
      "rowNumber",
      "login",
      "email",
      "roles",
      "lastUpdate",
      "lastLoggedIn",
      "status",
      "loggedIn",
      "timeSinceLogin",
      "userLevel",
      "status2fa",
    ];

    const lines = [headers.join(",")];
    rows.forEach((row) => {
      const line = headers
        .map((h) => {
          const v = row[h] == null ? "" : String(row[h]);
          return `"${v.replace(/"/g, '""')}"`;
        })
        .join(",");
      lines.push(line);
    });

    const blob = new Blob([lines.join("\n")], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `cm-user-list-${Date.now()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  function hideDropdowns() {
    els.ucViewMenu.classList.add("hidden");
    els.ucActionsMenu.classList.add("hidden");
  }

  function activeUserLogin() {
    const tab = activeTab();
    if (!tab || tab.type !== "user-common") return null;
    return tab.payload.login;
  }

  function renderUserCommon(payload) {
    els.ucLogin.textContent = payload.login || "-";
    els.ucStatus.textContent = userStatusLabel(payload.status || "");
    els.ucRoles.textContent = (payload.userInfo && payload.userInfo.roles) || "-";
    els.ucEmail.textContent = (payload.userInfo && payload.userInfo.email) || "-";
    els.ucComment.textContent = (payload.userInfo && payload.userInfo.comment) || "-";
    els.ucLastModified.textContent = (payload.userInfo && payload.userInfo.lastModifiedBy) || "-";

    const activity = payload.activity || {};
    els.ucNumberSessions.textContent = String(activity.numberOfSessions || 0);
    els.ucLastLogDate.textContent = activity.lastLogDate || "-";
    els.ucLastSessionLength.textContent = activity.lastSessionLength || "-";
    els.ucIpHostname.textContent = activity.ipHostname || "-";
    els.ucTotalTime.textContent = activity.totalTime || "-";
    els.ucAverageLength.textContent = activity.averageLength || "-";

    els.ucCommonView.classList.remove("hidden");
    els.ucTableView.classList.add("hidden");
  }

  function renderUserCommonTable(view, payload) {
    let rows = [];
    let cols = [];

    if (view === "sessions") {
      rows = payload.sessions || [];
      cols = ["sessionId", "active", "issuedAt", "expiresAt", "ip", "lastSeenAt", "length"];
    } else if (view === "ips") {
      rows = payload.ips || [];
      cols = ["ip", "lastSeenAt", "count"];
    } else if (view === "history") {
      rows = payload.rows || [];
      cols = ["at", "actor", "action", "objectType", "objectName", "details"];
      rows = rows.map((r) => Object.assign({}, r, { details: JSON.stringify(r.details || {}) }));
    }

    renderSimpleTable(els.ucDataTable, rows, cols);
    els.ucCommonView.classList.add("hidden");
    els.ucTableView.classList.remove("hidden");
  }

  function renderViewMenu() {
    els.ucViewMenu.innerHTML = "";
    VIEW_MENU_CONFIG.forEach((item) => {
      if (item.capability && !state.capabilities[item.capability]) return;
      const btn = document.createElement("button");
      btn.type = "button";
      btn.textContent = item.label;
      btn.addEventListener("click", async () => {
        const login = activeUserLogin();
        if (!login) return;
        state.userCommonViewByLogin[login] = item.id;
        hideDropdowns();
        await loadUserCommon(login, item.id);
      });
      els.ucViewMenu.appendChild(btn);
    });
  }

  function renderActionsMenu(commonPayload) {
    els.ucActionsMenu.innerHTML = "";

    const actions = [];
    if (state.capabilities.canResetPassword) {
      actions.push({ id: "resetPassword", label: "Reset Password" });
    }
    if (state.capabilities.canFlushIps) {
      actions.push({ id: "flushIps", label: "Flush IPs" });
    }
    if (state.capabilities.canLockUnlock) {
      const label = String(commonPayload.status || "").toUpperCase() === "ACTIVE"
        ? "Lock/Unlock User"
        : "Unlock User";
      actions.push({ id: "toggleStatus", label });
    }
    if (state.capabilities.canDeleteUser) {
      actions.push({ id: "deleteUser", label: "Delete User" });
    }

    if (!actions.length) {
      els.ucActionsDropdown.classList.add("hidden");
      return;
    }

    els.ucActionsDropdown.classList.remove("hidden");
    actions.forEach((action) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.textContent = action.label;
      btn.addEventListener("click", async () => {
        hideDropdowns();
        await runUserAction(action.id);
      });
      els.ucActionsMenu.appendChild(btn);
    });
  }

  async function runUserAction(actionId) {
    const login = activeUserLogin();
    if (!login) return;

    try {
      if (actionId === "resetPassword") {
        const payload = await api("/cm/actions/resetUserPassword", {
          method: "POST",
          body: JSON.stringify({ login }),
        });
        const msg = payload.generatedPassword
          ? `Password reset. Generated password: ${payload.generatedPassword}`
          : "Password reset.";
        els.ucMessage.textContent = msg;
      }

      if (actionId === "flushIps") {
        await api("/cm/actions/flushUserIps", {
          method: "POST",
          body: JSON.stringify({ login }),
        });
        els.ucMessage.textContent = "IP history flushed.";
      }

      if (actionId === "toggleStatus") {
        await api("/cm/actions/toggleUserStatus", {
          method: "POST",
          body: JSON.stringify({ login }),
        });
        els.ucMessage.textContent = "User status updated.";
      }

      if (actionId === "deleteUser") {
        if (!confirm(`Delete user ${login}?`)) return;
        await api("/cm/actions/deleteUser", {
          method: "POST",
          body: JSON.stringify({ login }),
        });
        els.ucMessage.textContent = "User deleted.";
        closeTab(state.activeTabId);
      }

      await loadUserList();
      const stillActiveLogin = activeUserLogin();
      if (stillActiveLogin) {
        await loadUserCommon(stillActiveLogin, state.userCommonViewByLogin[stillActiveLogin] || "common");
      }
    } catch (err) {
      els.ucError.textContent = errorText(err);
    }
  }

  async function loadUserCommon(login, view) {
    clearMessages();
    renderViewMenu();

    try {
      if (view === "common") {
        const payload = await api(`/cm/users/${encodeURIComponent(login)}/common-info`, { method: "GET" });
        state.userCommonCache[login] = payload;
        renderUserCommon(payload);
        renderActionsMenu(payload);
      }

      if (view === "sessions") {
        const payload = await api(`/cm/users/${encodeURIComponent(login)}/session-info`, { method: "GET" });
        renderUserCommonTable("sessions", payload);
      }

      if (view === "ips") {
        const payload = await api(`/cm/users/${encodeURIComponent(login)}/ips-info`, { method: "GET" });
        renderUserCommonTable("ips", payload);
      }

      if (view === "history") {
        const payload = await api(
          `/cm/history/object-change?objectType=User&objectName=${encodeURIComponent(login)}`,
          { method: "GET" }
        );
        renderUserCommonTable("history", payload);
      }

      const label = VIEW_MENU_CONFIG.find((x) => x.id === view);
      els.ucViewBtn.textContent = `${label ? label.label : "View"} ▾`;

      els.ucEditBtn.classList.toggle("hidden", !state.capabilities.canEditUser);
      if (!state.capabilities.canEditUser) {
        els.ucEditBtn.disabled = true;
      } else {
        els.ucEditBtn.disabled = false;
      }
    } catch (err) {
      els.ucError.textContent = errorText(err);
    }
  }

  function playerSearchParams() {
    const qs = new URLSearchParams();
    const cluster = String(els.psCluster.value || "").trim();
    const subcasinoList = String(els.psSubcasinoList.value || "").trim();
    const bankList = String(els.psBankList.value || "").trim();
    const nickname = String(els.psNickname.value || "").trim();
    const extid = String(els.psExtid.value || "").trim();
    const accountId = String(els.psAccountId.value || "").trim();
    const regAfter = String(els.psRegAfter.value || "").trim();
    const regBefore = String(els.psRegBefore.value || "").trim();
    const accountStatus = String(els.psAccountStatus.value || "").trim();
    const perPage = String(els.psMainPerPage.value || "").trim();

    if (cluster) qs.set("clusterId", cluster);
    if (subcasinoList) qs.set("subcasinoList", subcasinoList);
    if (bankList) qs.set("bankList", bankList);
    if (nickname) qs.set("nickName", nickname);
    if (extid) qs.set("extId", extid);
    if (accountId) qs.set("accountId", accountId);
    if (regAfter) qs.set("regAfterTime", regAfter);
    if (regBefore) qs.set("regBeforeTime", regBefore);
    if (accountStatus) qs.set("accountStatus", accountStatus);
    if (perPage) qs.set("mainPerPage", perPage);
    qs.set("fuzzySearch", els.psFuzzySearch.checked ? "true" : "false");
    return qs;
  }

  function renderPlayerSearchTable(rows) {
    els.playerSearchTable.innerHTML = "";
    if (!Array.isArray(rows) || !rows.length) {
      renderSimpleTable(els.playerSearchTable, [], [
        "#",
        "Player Name",
        "Ext. ID",
        "SubCasino Name",
        "Bank Name",
        "Status",
        "Logon Count",
        "Bets Count",
        "Total Bets",
        "Game Revenue",
        "Balance",
        "Last Log Platform",
        "Last Log Date",
        "Reg Time",
        "Currency Code",
      ]);
      return;
    }

    const columns = [
      "#",
      "Player Name",
      "",
      "Ext. ID",
      "SubCasino Name",
      "Bank Name",
      "Status",
      "Logon Count",
      "Bets Count",
      "Total Bets",
      "Game Revenue",
      "Balance",
      "Last Log Platform",
      "Last Log Date",
      "Reg Time",
      "Currency Code",
    ];

    const thead = document.createElement("thead");
    const htr = document.createElement("tr");
    columns.forEach((name) => {
      const th = document.createElement("th");
      th.textContent = name;
      htr.appendChild(th);
    });
    thead.appendChild(htr);
    els.playerSearchTable.appendChild(thead);

    const tbody = document.createElement("tbody");
    rows.forEach((row) => {
      const tr = document.createElement("tr");
      const values = [
        row.rowNumber,
        row.accountNickname || row.accountName,
        "...",
        row.externalId,
        row.subcasinoName,
        row.bankName,
        row.accountStatus,
        row.totalAccountSessionCount,
        row.totalBetsCount,
        row.totalIncome,
        row.gameRevenue,
        row.balance,
        row.lastPlatform,
        row.lastLoginTime,
        row.registrationTime,
        row.currencyCode,
      ];

      values.forEach((value, idx) => {
        const td = document.createElement("td");
        if (idx === 1) {
          const link = document.createElement("button");
          link.type = "button";
          link.className = "link-cell";
          link.textContent = value == null ? "" : String(value);
          link.addEventListener("click", () => openPlayerSummaryTab(row));
          td.appendChild(link);
        } else if (idx === 2) {
          const dots = document.createElement("button");
          dots.type = "button";
          dots.className = "dots-btn";
          dots.textContent = "⋮";
          dots.addEventListener("click", () => openPlayerSummaryTab(row));
          td.classList.add("dots-cell");
          td.appendChild(dots);
        } else {
          td.textContent = value == null ? "" : String(value);
        }
        if (idx === 6) {
          td.classList.add(String(row.accountStatus || "").toLowerCase().includes("active")
            ? "status-enabled"
            : "status-disabled");
        }
        tr.appendChild(td);
      });
      tbody.appendChild(tr);
    });
    els.playerSearchTable.appendChild(tbody);
  }

  async function loadPlayerSearch() {
    clearMessages();
    try {
      const payload = await api(`/cm/reports/playerSearch?${playerSearchParams().toString()}`, {
        method: "GET",
      });
      state.lastPlayerRows = payload.rows || [];
      renderPlayerSearchTable(state.lastPlayerRows);
      const ts = new Date();
      els.playerSearchMeta.textContent = `Report generated at ${ts.toLocaleString("en-GB", {
        hour12: false,
      })}, total records: ${state.lastPlayerRows.length}`;
    } catch (err) {
      els.playerSearchError.textContent = errorText(err);
    }
  }

  function exportPlayerSearchCsv() {
    const rows = state.lastPlayerRows || [];
    if (!rows.length) return;
    const headers = [
      "rowNumber",
      "accountNickname",
      "externalId",
      "subcasinoName",
      "bankName",
      "accountStatus",
      "totalAccountSessionCount",
      "totalBetsCount",
      "totalIncome",
      "gameRevenue",
      "balance",
      "lastPlatform",
      "lastLoginTime",
      "registrationTime",
      "currencyCode",
      "bankId",
      "accountId",
    ];

    const lines = [headers.join(",")];
    rows.forEach((row) => {
      const line = headers
        .map((h) => {
          const v = row[h] == null ? "" : String(row[h]);
          return `"${v.replace(/"/g, '""')}"`;
        })
        .join(",");
      lines.push(line);
    });

    const blob = new Blob([lines.join("\n")], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `cm-player-search-${Date.now()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  function activePlayerSummaryKey() {
    const tab = activeTab();
    if (!tab || tab.type !== "player-summary") return null;
    return `${tab.payload.bankId}:${tab.payload.accountId}`;
  }

  function activePlayerSummaryTab() {
    const tab = activeTab();
    if (!tab || tab.type !== "player-summary") return null;
    return tab;
  }

  function currentPlayerGameFilter(key) {
    if (!state.playerGameFilterByKey[key]) {
      state.playerGameFilterByKey[key] = defaultGameFilterState();
    }
    return state.playerGameFilterByKey[key];
  }

  function applyPlayerGameFilterInputs(key) {
    const filter = currentPlayerGameFilter(key);
    els.psmDateFrom.value = filter.dateFrom ? toDateTimeLocal(filter.dateFrom) : "";
    els.psmDateTo.value = filter.dateTo ? toDateTimeLocal(filter.dateTo) : "";
    els.psmPlayerMode.value = String(filter.playerMode ?? "0");
    els.psmPlatform.value = String(filter.platform ?? "-1");
    els.psmGameType.value = String(filter.gameType ?? "-1");
    els.psmIsJackpot.value = String(filter.isJackpot ?? "-1");
    els.psmShowBySessions.checked = !!filter.showBySessions;
  }

  function collectPlayerGameFilterInputs(key) {
    const next = {
      dateFrom: els.psmDateFrom.value ? new Date(els.psmDateFrom.value).toISOString() : "",
      dateTo: els.psmDateTo.value ? new Date(els.psmDateTo.value).toISOString() : "",
      playerMode: String(els.psmPlayerMode.value || "0"),
      platform: String(els.psmPlatform.value || "-1"),
      gameType: String(els.psmGameType.value || "-1"),
      isJackpot: String(els.psmIsJackpot.value || "-1"),
      showBySessions: !!els.psmShowBySessions.checked,
    };
    state.playerGameFilterByKey[key] = next;
    return next;
  }

  function fillAwardDefaultTimes(startInput, expirationInput) {
    const now = new Date();
    const exp = new Date(now);
    exp.setDate(exp.getDate() + 30);
    exp.setHours(23, 59, 0, 0);
    startInput.value = toDateTimeLocal(now.toISOString());
    expirationInput.value = toDateTimeLocal(exp.toISOString());
  }

  function closeAwardBonusModal() {
    els.awardBonusModal.classList.add("hidden");
  }

  function closeAwardFrbonusModal() {
    els.awardFrbonusModal.classList.add("hidden");
  }

  function refreshAwardBonusDerived() {
    const amount = Number(els.abAmount.value || 0);
    const mult = Number(els.abRolloverMultiplier.value || 0);
    const capMult = Number(els.abCapMultiplier.value || 0);
    const rollover = Number.isFinite(amount) && Number.isFinite(mult) ? amount * mult : 0;
    const cap = Number.isFinite(amount) && Number.isFinite(capMult) && capMult > 0 ? amount * capMult : 0;
    els.abRollover.value = rollover.toFixed(2);
    if (!Number(els.abMaxWinCap.value || 0)) {
      els.abMaxWinCap.value = cap.toFixed(2);
    }
  }

  function openAwardBonusModal() {
    const tab = activePlayerSummaryTab();
    if (!tab) return;
    const accountName = tab.payload.accountName || tab.payload.accountId;
    els.awardBonusTitle.textContent = `Award Bonus to ${accountName}`;
    els.abType.value = "0";
    els.abAmount.value = "10";
    els.abRolloverMultiplier.value = "2";
    els.abCapMultiplier.value = "0";
    els.abMaxWinCap.value = "0";
    els.abGameLimitType.value = "0";
    els.abGameList.value = "";
    els.abDescription.value = "";
    els.abReleasedType.value = "true";
    fillAwardDefaultTimes(els.abStartTime, els.abExpirationTime);
    refreshAwardBonusDerived();
    els.awardBonusModal.classList.remove("hidden");
  }

  function openAwardFrbonusModal() {
    const tab = activePlayerSummaryTab();
    if (!tab) return;
    const accountName = tab.payload.accountName || tab.payload.accountId;
    els.awardFrbonusTitle.textContent = `Award Free Round Bonus for ${accountName}`;
    els.afbRounds.value = "5";
    els.afbBetType.value = "1";
    els.afbGameLimitType.value = "0";
    els.afbGameList.value = "";
    els.afbSingleGame.value = "";
    els.afbAwardDurationDays.value = "";
    els.afbFrChips.value = "10";
    els.afbMaxWinCap.value = "0";
    els.afbDescription.value = "";
    fillAwardDefaultTimes(els.afbStartTime, els.afbExpirationTime);
    els.awardFrbonusModal.classList.remove("hidden");
  }

  function closePlayerDropdowns() {
    els.psmActionsMenu.classList.add("hidden");
    els.psmViewMenu.classList.add("hidden");
  }

  function renderPlayerSummaryCard(payload) {
    const shortInfo = payload.shortInfo || {};
    const playerInfo = payload.playerInfo || {};
    const activity = payload.activity || {};

    els.psmLogin.textContent = payload.accountName || "-";
    els.psmStatus.textContent = payload.status || "-";
    els.psmPlayerName.textContent = shortInfo.accountUsername || "-";
    els.psmExtid.textContent = shortInfo.accountExternalId || "-";
    els.psmCluster.textContent = shortInfo.cluster || "-";
    els.psmSubcasino.textContent = shortInfo.subcasinoName || "-";
    els.psmBank.textContent = shortInfo.bankName || "-";
    els.psmTester.textContent = shortInfo.isAccountTester || "NO";
    els.psmBalance.textContent = String(shortInfo.balance ?? "-");
    els.psmTotalBets.textContent = String(shortInfo.totalBets ?? "-");
    els.psmTotalPayout.textContent = String(shortInfo.totalPayout ?? "-");
    els.psmGameRevenue.textContent = String(shortInfo.gameRevenue ?? "-");
    els.psmCurrency.textContent = shortInfo.currencyCode || "-";
    els.psmAccountId.textContent = String(playerInfo.id ?? payload.accountId ?? "-");
    els.psmRegDate.textContent = playerInfo.registrationTime || "-";
    els.psmType.textContent = playerInfo.registrationPlatform || "-";
    els.psmLastLog.textContent = activity.loginTime || "-";
    els.psmRounds.textContent = String(activity.roundsCount ?? 0);
    els.psmGameSessions.textContent = String(activity.totalSessionsPlayed ?? 0);

    els.psmSummaryView.classList.remove("hidden");
    els.psmGameFilterWrap.classList.add("hidden");
    els.psmTableView.classList.add("hidden");
  }

  function renderPlayerSummaryTable(viewId, payload) {
    let rows = [];
    let columns = [];

    if (viewId === "game") {
      rows = payload.rows || [];
      columns = payload.mode === "sessions"
        ? [
            "rowNumber",
            "gameTitle",
            "startTime",
            "endTime",
            "income",
            "payout",
            "gainLoss",
            "roundsCount",
            "gameSessionId",
          ]
        : [
            "gameTitle",
            "sessionsCount",
            "sessionsCountPercent",
            "totalIncome",
            "avgBet",
            "incomePercent",
            "totalPayout",
            "gainLoss",
            "gainLossPercent",
            "bonusBet",
            "bonusWin",
          ];
    } else if (viewId === "payments") {
      rows = payload.rows || [];
      columns = Object.keys(rows[0] || {});
    } else if (viewId === "bonus") {
      rows = payload.rows || [];
      columns = Object.keys(rows[0] || {});
    } else if (viewId === "frbonus") {
      rows = payload.rows || [];
      columns = Object.keys(rows[0] || {});
    } else if (viewId === "history") {
      rows = (payload.rows || []).map((row) =>
        Object.assign({}, row, { details: JSON.stringify(row.details || {}) })
      );
      columns = ["at", "actor", "action", "objectType", "objectId", "objectName", "details"];
    }

    renderSimpleTable(els.psmDataTable, rows, columns);
    els.psmSummaryView.classList.add("hidden");
    els.psmGameFilterWrap.classList.toggle("hidden", viewId !== "game");
    els.psmTableView.classList.remove("hidden");
  }

  function renderPlayerMenus(summaryPayload) {
    els.psmActionsMenu.innerHTML = "";
    els.psmViewMenu.innerHTML = "";

    const actions = (summaryPayload.menu && summaryPayload.menu.actions) || [];
    if (!state.capabilities.canManageAny || !actions.length) {
      els.psmActionsBtn.disabled = true;
    } else {
      els.psmActionsBtn.disabled = false;
      actions.forEach((action) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.textContent = action.title || action.id;
        btn.addEventListener("click", async () => {
          closePlayerDropdowns();
          await runPlayerAction(action.id);
        });
        els.psmActionsMenu.appendChild(btn);
      });
    }

    const views = (summaryPayload.menu && summaryPayload.menu.view) || PLAYER_SUMMARY_VIEW_CONFIG;
    views.forEach((view) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.textContent = view.title || view.label || view.id;
      btn.addEventListener("click", async () => {
        const tab = activePlayerSummaryTab();
        if (!tab) return;
        const key = `${tab.payload.bankId}:${tab.payload.accountId}`;
        state.playerSummaryViewByKey[key] = view.id;
        closePlayerDropdowns();
        await loadPlayerSummary(tab.payload.bankId, tab.payload.accountId, view.id);
      });
      els.psmViewMenu.appendChild(btn);
    });
  }

  async function runPlayerAction(actionId) {
    const tab = activePlayerSummaryTab();
    if (!tab) return;
    const { bankId, accountId } = tab.payload;
    if (actionId === "awardBonus") {
      openAwardBonusModal();
      return;
    }
    if (actionId === "awardFRBonus") {
      openAwardFrbonusModal();
      return;
    }
    try {
      await api(`/cm/players/${bankId}/${accountId}/actions/${actionId}`, {
        method: "POST",
        body: "{}",
      });
      els.psmMessage.textContent = "Player updated.";
      await loadPlayerSummary(bankId, accountId, "summary");
      await loadPlayerSearch();
    } catch (err) {
      els.psmError.textContent = errorText(err);
    }
  }

  async function loadPlayerSummary(bankId, accountId, viewId) {
    clearMessages();
    try {
      const summary = await api(`/cm/players/${bankId}/${accountId}/summary`, { method: "GET" });
      const key = `${bankId}:${accountId}`;
      state.playerSummaryCache[key] = summary;
      renderPlayerMenus(summary);

      if (!viewId || viewId === "summary") {
        renderPlayerSummaryCard(summary);
      } else {
        let endpoint = "";
        let endpointQs = "";
        if (viewId === "game") endpoint = "game-info";
        if (viewId === "payments") endpoint = "payment-detail";
        if (viewId === "bonus") endpoint = "bonus-detail";
        if (viewId === "frbonus") endpoint = "frbonus-detail";
        if (viewId === "history") endpoint = "change-history";
        if (viewId === "game") {
          const key = `${bankId}:${accountId}`;
          applyPlayerGameFilterInputs(key);
          const filter = collectPlayerGameFilterInputs(key);
          const qs = new URLSearchParams();
          if (filter.dateFrom) qs.set("dateFrom", filter.dateFrom);
          if (filter.dateTo) qs.set("dateTo", filter.dateTo);
          qs.set("playerMode", filter.playerMode);
          qs.set("platform", filter.platform);
          qs.set("gameType", filter.gameType);
          qs.set("isJackpot", filter.isJackpot);
          qs.set("showBySessions", filter.showBySessions ? "true" : "false");
          endpointQs = `?${qs.toString()}`;
        }

        if (!endpoint) {
          renderPlayerSummaryCard(summary);
        } else {
          const payload = await api(`/cm/players/${bankId}/${accountId}/${endpoint}${endpointQs}`, {
            method: "GET",
          });
          renderPlayerSummaryTable(viewId, payload);
        }
      }

      const label = PLAYER_SUMMARY_VIEW_CONFIG.find((x) => x.id === (viewId || "summary"));
      els.psmViewBtn.textContent = `${label ? label.label : "View"} ▾`;
    } catch (err) {
      els.psmError.textContent = errorText(err);
    }
  }

  async function submitAwardBonus() {
    const tab = activePlayerSummaryTab();
    if (!tab) return;
    const { bankId, accountId } = tab.payload;
    try {
      const payload = {
        bonusType: Number(els.abType.value),
        amount: Number(els.abAmount.value),
        rolloverMultiplier: Number(els.abRolloverMultiplier.value),
        multiplierCap: Number(els.abCapMultiplier.value || 0),
        maxWinCap: Number(els.abMaxWinCap.value || 0),
        startTime: els.abStartTime.value ? new Date(els.abStartTime.value).toISOString() : "",
        expirationTime: els.abExpirationTime.value ? new Date(els.abExpirationTime.value).toISOString() : "",
        gameLimitType: Number(els.abGameLimitType.value),
        gameList: parseCsvIntsText(els.abGameList.value),
        description: els.abDescription.value.trim(),
        releasedType: els.abReleasedType.value === "true",
      };
      await api(`/cm/players/${bankId}/${accountId}/actions/awardBonus`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      closeAwardBonusModal();
      els.psmMessage.textContent = "Bonus awarded.";
      await loadPlayerSummary(bankId, accountId, "summary");
      await loadPlayerSearch();
    } catch (err) {
      els.psmError.textContent = errorText(err);
    }
  }

  async function submitAwardFrbonus() {
    const tab = activePlayerSummaryTab();
    if (!tab) return;
    const { bankId, accountId } = tab.payload;
    try {
      const payload = {
        rounds: Number(els.afbRounds.value),
        frbBetType: Number(els.afbBetType.value),
        gameLimitType: Number(els.afbGameLimitType.value),
        gameList: parseCsvIntsText(els.afbGameList.value),
        gameId: els.afbSingleGame.value ? Number(els.afbSingleGame.value) : null,
        startTime: els.afbStartTime.value ? new Date(els.afbStartTime.value).toISOString() : "",
        expirationTime: els.afbExpirationTime.value ? new Date(els.afbExpirationTime.value).toISOString() : "",
        awardDurationDays: els.afbAwardDurationDays.value ? Number(els.afbAwardDurationDays.value) : 0,
        frChips: Number(els.afbFrChips.value),
        maxWinCap: Number(els.afbMaxWinCap.value || 0),
        description: els.afbDescription.value.trim(),
      };
      await api(`/cm/players/${bankId}/${accountId}/actions/awardFRBonus`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      closeAwardFrbonusModal();
      els.psmMessage.textContent = "FR bonus awarded.";
      await loadPlayerSummary(bankId, accountId, "summary");
      await loadPlayerSearch();
    } catch (err) {
      els.psmError.textContent = errorText(err);
    }
  }

  function renderUserManagementMeta(meta) {
    state.umMeta = meta;
    state.capabilities = meta.capabilities || {};

    fillSelect(els.ufLevel, meta.userLevels || [], {
      includeAll: true,
      allLabel: "All",
      allValue: "",
    });

    fillSelect(els.cuRoles, meta.roles || []);
    fillSelect(els.cuClusters, meta.clusters || []);
    fillSelect(els.cuSubcasinos, meta.subcasinos || []);
    fillSelect(els.cuBanks, meta.banks || []);

    fillSelect(els.crClusters, meta.clusters || []);
    fillSelect(els.crSubcasinos, meta.subcasinos || []);
    fillSelect(els.crBanks, meta.banks || []);

    fillSelect(els.eumRoles, meta.roles || []);
    fillSelect(els.eumClusters, meta.clusters || []);
    fillSelect(els.eumSubcasinos, meta.subcasinos || []);
    fillSelect(els.eumBanks, meta.banks || []);

    renderPermissions(meta.permissions || []);

    if (!state.capabilities.canCreateUser) {
      els.createUserBtn.disabled = true;
    }
    if (!state.capabilities.canCreateRole) {
      els.createRoleBtn.disabled = true;
    }
  }

  function renderPermissions(permissions) {
    els.crPermissions.innerHTML = "";
    permissions.forEach((perm) => {
      const label = document.createElement("label");
      label.className = "perm-item";

      const cb = document.createElement("input");
      cb.type = "checkbox";
      cb.value = String(perm.id);
      cb.dataset.role = "permission";
      label.appendChild(cb);

      const text = document.createElement("span");
      text.textContent = `${perm.title} (${perm.category})`;
      label.appendChild(text);
      els.crPermissions.appendChild(label);
    });
  }

  function toggleCreateUserScope() {
    const hide = !!els.cuGeneral.checked;
    els.cuScope.classList.toggle("hidden", hide);
  }

  function toggleCreateRoleScope() {
    els.crScope.classList.toggle("hidden", !!els.crNonrestricted.checked);
  }

  function toggleEditScope() {
    els.eumScope.classList.toggle("hidden", !!els.eumGeneral.checked);
  }

  async function submitCreateUser() {
    clearMessages();
    try {
      const body = {
        login: els.cuLogin.value.trim(),
        email: els.cuEmail.value.trim(),
        comment: els.cuComment.value.trim(),
        isGeneral: !!els.cuGeneral.checked,
        includeFutureBanks: !!els.cuIncludeFuture.checked,
        initialPassword: els.cuPassword.value.trim() || undefined,
        roleIds: selectedInts(els.cuRoles),
        clusterIds: selectedInts(els.cuClusters),
        subcasinoIds: selectedInts(els.cuSubcasinos),
        bankIds: selectedInts(els.cuBanks),
      };

      const payload = await api("/cm/actions/createUser", {
        method: "POST",
        body: JSON.stringify(body),
      });

      const generated = payload.generatedPassword
        ? ` Generated password: ${payload.generatedPassword}`
        : "";
      els.createUserSuccess.textContent = `User ${payload.user.login} created.${generated}`;
      await loadUserList();
    } catch (err) {
      els.createUserError.textContent = errorText(err);
    }
  }

  async function loadRoleList() {
    clearMessages();
    try {
      const payload = await api("/cm/reports/roleList", { method: "GET" });
      renderSimpleTable(els.roleListTable, payload.rows || [], [
        "rowNumber",
        "roleId",
        "roleName",
        "description",
        "isSystem",
        "creationDate",
        "lastChangeDate",
      ]);
      els.roleListMeta.textContent = `rows=${payload.count}`;
    } catch (err) {
      els.roleListError.textContent = errorText(err);
    }
  }

  function selectedPermissionIds() {
    return Array.from(els.crPermissions.querySelectorAll('input[data-role="permission"]:checked'))
      .map((x) => Number(x.value))
      .filter((n) => Number.isInteger(n));
  }

  async function submitCreateRole() {
    clearMessages();
    try {
      const body = {
        roleName: els.crName.value.trim(),
        description: els.crDescription.value.trim(),
        isNonRestricted: !!els.crNonrestricted.checked,
        permissions: selectedPermissionIds(),
        clusterIds: selectedInts(els.crClusters),
        subcasinoIds: selectedInts(els.crSubcasinos),
        bankIds: selectedInts(els.crBanks),
      };

      const payload = await api("/cm/actions/createRole", {
        method: "POST",
        body: JSON.stringify(body),
      });

      els.createRoleSuccess.textContent = `Role ${payload.role.roleName} created (id=${payload.role.roleId}).`;

      const meta = await api("/cm/meta/user-management", { method: "GET" });
      renderUserManagementMeta(meta);
      await loadRoleList();
    } catch (err) {
      els.createRoleError.textContent = errorText(err);
    }
  }

  function renderReportSelect() {
    els.reportSelect.innerHTML = "";
    state.reports.forEach((r) => {
      els.reportSelect.appendChild(option(`${r.title} (${r.id})`, r.id));
    });
  }

  function inputForField(field) {
    const wrap = document.createElement("div");
    wrap.className = "field";
    const label = document.createElement("label");
    label.textContent = field.label;
    label.htmlFor = `rf-${field.id}`;
    wrap.appendChild(label);

    const input = document.createElement("input");
    input.id = `rf-${field.id}`;
    input.name = field.id;
    input.value = field.defaultValue || "";
    input.type = "text";
    wrap.appendChild(input);
    return wrap;
  }

  function currentReport() {
    const id = els.reportSelect.value;
    return state.reportMap[id];
  }

  function renderReportForm() {
    els.reportForm.innerHTML = "";
    const report = currentReport();
    if (!report) return;
    report.fields.forEach((f) => {
      els.reportForm.appendChild(inputForField(f));
    });
  }

  async function runReport() {
    clearMessages();
    const report = currentReport();
    if (!report) return;

    const qs = new URLSearchParams();
    report.fields.forEach((f) => {
      const input = document.getElementById(`rf-${f.id}`);
      if (!input) return;
      const v = String(input.value || "").trim();
      if (v.length) qs.set(f.id, v);
    });

    try {
      const payload = await api(`${report.path}?${qs.toString()}`, { method: "GET" });
      renderSimpleTable(els.reportTable, payload.rows || []);
      els.reportMeta.textContent = `reportId=${payload.reportId}, source=${payload.sourceTable}, rows=${payload.count}`;
    } catch (err) {
      els.reportError.textContent = errorText(err);
    }
  }

  function openEditModal() {
    const login = activeUserLogin();
    if (!login) return;
    const payload = state.userCommonCache[login];
    if (!payload) return;

    state.editOriginalLogin = login;

    els.eumLogin.value = payload.login || "";
    els.eumEmail.value = payload.email || "";
    els.eumComment.value = payload.comment || "";
    els.eumIncludeFuture.checked = !!payload.includeFutureBanks;
    els.eumGeneral.checked = String(payload.userLevel || "").toUpperCase() === "GENERAL";

    const roleIdSet = new Set((payload.roles || []).map((r) => Number(r.id)));
    Array.from(els.eumRoles.options).forEach((o) => {
      o.selected = roleIdSet.has(Number(o.value));
    });

    const scope = payload.scope || {};
    const clusterSet = new Set((scope.clusterIds || []).map(Number));
    const subcasinoSet = new Set((scope.subcasinoIds || []).map(Number));
    const bankSet = new Set((scope.bankIds || []).map(Number));

    Array.from(els.eumClusters.options).forEach((o) => {
      o.selected = clusterSet.has(Number(o.value));
    });
    Array.from(els.eumSubcasinos.options).forEach((o) => {
      o.selected = subcasinoSet.has(Number(o.value));
    });
    Array.from(els.eumBanks.options).forEach((o) => {
      o.selected = bankSet.has(Number(o.value));
    });

    toggleEditScope();
    els.editModal.classList.remove("hidden");
  }

  function closeEditModal() {
    els.editModal.classList.add("hidden");
    state.editOriginalLogin = null;
  }

  async function saveEditUser() {
    if (!state.editOriginalLogin) return;

    try {
      const body = {
        originalLogin: state.editOriginalLogin,
        login: els.eumLogin.value.trim(),
        email: els.eumEmail.value.trim(),
        comment: els.eumComment.value.trim(),
        isGeneral: !!els.eumGeneral.checked,
        includeFutureBanks: !!els.eumIncludeFuture.checked,
        roleIds: selectedInts(els.eumRoles),
        clusterIds: selectedInts(els.eumClusters),
        subcasinoIds: selectedInts(els.eumSubcasinos),
        bankIds: selectedInts(els.eumBanks),
      };

      const payload = await api("/cm/actions/editUser", {
        method: "POST",
        body: JSON.stringify(body),
      });

      closeEditModal();
      await loadUserList();

      const newLogin = payload.user.login;
      const currentTab = activeTab();
      if (currentTab && currentTab.type === "user-common") {
        currentTab.payload.login = newLogin;
        currentTab.id = `tab-user-common-${newLogin.toLowerCase()}`;
        state.activeTabId = currentTab.id;
      }
      renderTabs();
      await loadUserCommon(newLogin, "common");
      els.ucMessage.textContent = "User updated.";
    } catch (err) {
      els.ucError.textContent = errorText(err);
    }
  }

  async function initApp() {
    clearMessages();

    const [health, menu, reports, userMeta] = await Promise.all([
      api("/health", { method: "GET", headers: {} }),
      api("/cm/meta/menu", { method: "GET" }),
      api("/cm/meta/reports", { method: "GET" }),
      api("/cm/meta/user-management", { method: "GET" }),
    ]);

    els.statusService.textContent = health.service;
    els.statusCassandra.textContent = health.cassandraContainer;
    els.statusUser.textContent = state.username || "-";
    els.statusAuth.textContent = "Authenticated";
    els.statusCoreDb.textContent = userMeta.dbInfo && userMeta.dbInfo.core
      ? userMeta.dbInfo.core.file
      : "-";
    els.statusMirrorDb.textContent = userMeta.dbInfo && userMeta.dbInfo.mirror
      ? `${userMeta.dbInfo.mirror.file}`
      : "-";

    state.menu = menu.items || [];
    state.reports = reports.items || [];
    state.reportMap = {};
    state.reports.forEach((r) => {
      state.reportMap[r.id] = r;
    });

    renderUserManagementMeta(userMeta);
    renderMenu();
    renderReportSelect();
    renderReportForm();

    state.tabs = [];
    state.activeTabId = null;

    openTab({
      id: "tab-user-list",
      title: "User List",
      type: "user-list",
      panelId: "panel-user-list",
      subtitle: "",
      payload: {},
      closable: false,
    });
  }

  els.loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearMessages();
    try {
      const payload = await api("/cm-auth/login", {
        method: "POST",
        body: JSON.stringify({
          username: els.loginUsername.value.trim(),
          password: els.loginPassword.value,
        }),
      });
      state.token = payload.accessToken;
      state.username = els.loginUsername.value.trim();
      localStorage.setItem("cm_token", state.token);
      localStorage.setItem("cm_username", state.username);
      els.helloUser.textContent = `Hello ${state.username}`;

      if (payload.mustChangePassword) {
        showScreen("password");
      } else {
        showScreen("app");
        await initApp();
      }
    } catch (err) {
      els.loginError.textContent = errorText(err);
    }
  });

  els.passwordForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearMessages();
    try {
      const payload = await api("/cm-auth/change-password", {
        method: "POST",
        body: JSON.stringify({
          oldPassword: els.oldPassword.value,
          newPassword: els.newPassword.value,
        }),
      });
      els.passwordSuccess.textContent = payload.message || "Password changed.";
      state.token = null;
      localStorage.removeItem("cm_token");
      setTimeout(() => {
        showScreen("login");
      }, 700);
    } catch (err) {
      els.passwordError.textContent = errorText(err);
    }
  });

  els.logoutBtn.addEventListener("click", async () => {
    try {
      await api("/cm-auth/logout", { method: "POST", body: "{}" });
    } catch (_err) {
      // best effort
    }
    resetStateToLogin();
  });

  els.userSearchBtn.addEventListener("click", loadUserList);
  els.userExportBtn.addEventListener("click", exportUserListCsv);

  els.cuGeneral.addEventListener("change", toggleCreateUserScope);
  els.createUserBtn.addEventListener("click", submitCreateUser);

  els.roleRefreshBtn.addEventListener("click", loadRoleList);

  els.crNonrestricted.addEventListener("change", toggleCreateRoleScope);
  els.createRoleBtn.addEventListener("click", submitCreateRole);

  els.playerSearchBtn.addEventListener("click", loadPlayerSearch);
  els.playerExportBtn.addEventListener("click", exportPlayerSearchCsv);

  els.reportSelect.addEventListener("change", renderReportForm);
  els.runReportBtn.addEventListener("click", runReport);

  els.ucViewBtn.addEventListener("click", () => {
    els.ucViewMenu.classList.toggle("hidden");
    els.ucActionsMenu.classList.add("hidden");
  });
  els.ucActionsBtn.addEventListener("click", () => {
    els.ucActionsMenu.classList.toggle("hidden");
    els.ucViewMenu.classList.add("hidden");
  });

  els.psmActionsBtn.addEventListener("click", () => {
    els.psmActionsMenu.classList.toggle("hidden");
    els.psmViewMenu.classList.add("hidden");
  });
  els.psmViewBtn.addEventListener("click", () => {
    els.psmViewMenu.classList.toggle("hidden");
    els.psmActionsMenu.classList.add("hidden");
  });
  els.psmGameSearchBtn.addEventListener("click", async () => {
    const tab = activePlayerSummaryTab();
    if (!tab) return;
    await loadPlayerSummary(tab.payload.bankId, tab.payload.accountId, "game");
  });

  els.abAmount.addEventListener("input", refreshAwardBonusDerived);
  els.abRolloverMultiplier.addEventListener("input", refreshAwardBonusDerived);
  els.abCapMultiplier.addEventListener("input", refreshAwardBonusDerived);
  els.awardBonusOverlay.addEventListener("click", closeAwardBonusModal);
  els.awardBonusCancelBtn.addEventListener("click", closeAwardBonusModal);
  els.awardBonusSubmitBtn.addEventListener("click", submitAwardBonus);

  els.awardFrbonusOverlay.addEventListener("click", closeAwardFrbonusModal);
  els.awardFrbonusCancelBtn.addEventListener("click", closeAwardFrbonusModal);
  els.awardFrbonusSubmitBtn.addEventListener("click", submitAwardFrbonus);

  els.ucEditBtn.addEventListener("click", openEditModal);

  els.eumGeneral.addEventListener("change", toggleEditScope);
  els.editCancelOverlay.addEventListener("click", closeEditModal);
  els.editCancelBtn.addEventListener("click", closeEditModal);
  els.editSaveBtn.addEventListener("click", saveEditUser);

  document.addEventListener("click", (e) => {
    const target = e.target;
    const inView = els.ucViewBtn.contains(target) || els.ucViewMenu.contains(target);
    const inActions = els.ucActionsBtn.contains(target) || els.ucActionsMenu.contains(target);
    const inPlayerView = els.psmViewBtn.contains(target) || els.psmViewMenu.contains(target);
    const inPlayerActions = els.psmActionsBtn.contains(target) || els.psmActionsMenu.contains(target);
    if (!inView) els.ucViewMenu.classList.add("hidden");
    if (!inActions) els.ucActionsMenu.classList.add("hidden");
    if (!inPlayerView) els.psmViewMenu.classList.add("hidden");
    if (!inPlayerActions) els.psmActionsMenu.classList.add("hidden");
  });

  const savedToken = localStorage.getItem("cm_token");
  const savedUser = localStorage.getItem("cm_username");
  if (savedToken && savedUser) {
    state.token = savedToken;
    state.username = savedUser;
    els.helloUser.textContent = `Hello ${savedUser}`;
    showScreen("app");
    initApp().catch(() => resetStateToLogin());
  } else {
    showScreen("login");
  }
})();
