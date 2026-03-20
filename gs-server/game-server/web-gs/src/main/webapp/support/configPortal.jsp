<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.util.property.BooleanProperty" %>
<%@ page import="com.abs.casino.common.util.property.EnumProperty" %>
<%@ page import="com.abs.casino.common.util.property.MandatoryProperty" %>
<%@ page import="com.abs.casino.common.util.property.NumericProperty" %>
<%@ page import="com.abs.casino.common.util.property.StringProperty" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="java.lang.reflect.Modifier" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    private String esc(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value;
        escaped = escaped.replace("&", "&amp;");
        escaped = escaped.replace("<", "&lt;");
        escaped = escaped.replace(">", "&gt;");
        escaped = escaped.replace("\"", "&quot;");
        return escaped;
    }

    private String categoryFor(String key) {
        if (key == null) {
            return "General";
        }
        if (key.startsWith("COMMON_WALLET_") || key.startsWith("CW_") || key.startsWith("WALLET_")) {
            return "Wallet";
        }
        if (key.startsWith("FRB_") || key.startsWith("BONUS_") || key.contains("BONUS")) {
            return "Bonus/FRB";
        }
        if (key.startsWith("MP_") || key.contains("MULTIPLAYER")) {
            return "Multiplayer";
        }
        if (key.endsWith("_URL") || key.contains("_URL_")) {
            return "Integration URL";
        }
        if (key.startsWith("MQ_") || key.contains("MAXQUEST")) {
            return "Legacy MQ";
        }
        if (key.startsWith("GL_")) {
            return "Game Limits";
        }
        return "General";
    }

    private boolean matches(String queryLower, String value) {
        if (queryLower == null || queryLower.length() == 0) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(queryLower);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value;
        escaped = escaped.replace("\\", "\\\\");
        escaped = escaped.replace("\"", "\\\"");
        escaped = escaped.replace("\n", "\\n");
        escaped = escaped.replace("\r", "\\r");
        return escaped;
    }

    private String extractJsonString(String json, String fieldName) {
        if (isBlank(json) || isBlank(fieldName)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Map<String, String> postJson(String targetUrl, String payload) {
        Map<String, String> result = new HashMap<String, String>();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(4000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] bytes = payload.getBytes("UTF-8");
            connection.setFixedLengthStreamingMode(bytes.length);
            OutputStream os = connection.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();

            int code = connection.getResponseCode();
            result.put("code", String.valueOf(code));

            InputStream responseStream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
            StringBuilder body = new StringBuilder();
            if (responseStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                reader.close();
            }
            result.put("body", body.toString());
        } catch (Exception e) {
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private String buildBaseUrl(javax.servlet.http.HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        if (host != null && host.indexOf(':') > -1 && !(host.startsWith("[") && host.endsWith("]"))) {
            int split = host.lastIndexOf(':');
            if (split > -1 && split < host.length() - 1) {
                String maybePort = host.substring(split + 1);
                boolean numericPort = true;
                for (int i = 0; i < maybePort.length(); i++) {
                    if (!Character.isDigit(maybePort.charAt(i))) {
                        numericPort = false;
                        break;
                    }
                }
                if (numericPort) {
                    host = host.substring(0, split);
                }
            }
        }
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port) + (contextPath == null ? "" : contextPath);
    }

    private String absoluteUrl(javax.servlet.http.HttpServletRequest request, String pathAndQuery) {
        if (isBlank(pathAndQuery)) {
            return buildBaseUrl(request);
        }
        if (pathAndQuery.startsWith("http://") || pathAndQuery.startsWith("https://")) {
            return pathAndQuery;
        }
        return buildBaseUrl(request) + (pathAndQuery.startsWith("/") ? pathAndQuery : "/" + pathAndQuery);
    }
%>
<%
    String query = request.getParameter("q");
    if (query == null) {
        query = "";
    }
    query = query.trim();
    String queryLower = query.toLowerCase(Locale.ENGLISH);

    Properties clusterProps = new Properties();
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cluster-hosts.properties")) {
        if (is != null) {
            clusterProps.load(is);
        }
    }
    List<String> clusterKeys = new ArrayList<String>(clusterProps.stringPropertyNames());
    Collections.sort(clusterKeys);

    Map<Long, BankInfo> allBanksMap = Collections.<Long, BankInfo>emptyMap();
    String bankCacheWarning = "";
    try {
        Map<Long, BankInfo> loadedBanks = BankInfoCache.getInstance().getAllObjects();
        if (loadedBanks != null) {
            allBanksMap = loadedBanks;
        } else {
            bankCacheWarning = "Bank cache is not initialized yet (null map). Retry after GS startup completes.";
        }
    } catch (Exception e) {
        bankCacheWarning = "Bank cache unavailable during startup: " + e.getClass().getSimpleName()
                + (e.getMessage() == null ? "" : (": " + e.getMessage()));
    }
    List<Long> bankIds = new ArrayList<Long>(allBanksMap.keySet());
    Collections.sort(bankIds);

    Long selectedBankId = null;
    String selectedBankParam = request.getParameter("bankId");
    if (selectedBankParam != null && selectedBankParam.trim().length() > 0) {
        try {
            long parsed = Long.parseLong(selectedBankParam.trim());
            if (allBanksMap.containsKey(parsed)) {
                selectedBankId = parsed;
            }
        } catch (NumberFormatException ignore) {
        }
    }
    if (selectedBankId == null && !bankIds.isEmpty()) {
        selectedBankId = bankIds.get(0);
    }
    BankInfo selectedBank = selectedBankId == null ? null : allBanksMap.get(selectedBankId.longValue());
    String guestBankId = "271";
    String guestGameId = "838";
    String guestLang = "en";
    String realBankId = (selectedBankId == null || selectedBankId.longValue() <= 0L) ? "6275" : String.valueOf(selectedBankId.longValue());
    String realSubCasinoId = (selectedBank == null || selectedBank.getSubCasinoId() <= 0L) ? "507" : String.valueOf(selectedBank.getSubCasinoId());
    String realGameId = "838";
    String realToken = "bav_game_session_001";
    String sampleAccountId = "40962";

    List<Map<String, String>> propertyDefs = new ArrayList<Map<String, String>>();
    Map<String, Map<String, String>> propertyDefsByKey = new HashMap<String, Map<String, String>>();
    Set<String> seenKeys = new HashSet<String>();

    Field[] fields = BankInfo.class.getDeclaredFields();
    for (Field field : fields) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
            continue;
        }
        if (!(field.isAnnotationPresent(BooleanProperty.class)
                || field.isAnnotationPresent(StringProperty.class)
                || field.isAnnotationPresent(NumericProperty.class)
                || field.isAnnotationPresent(EnumProperty.class))) {
            continue;
        }
        Object rawKey = field.get(null);
        if (!(rawKey instanceof String)) {
            continue;
        }

        String key = (String) rawKey;
        if (!seenKeys.add(key)) {
            continue;
        }

        String type = "String";
        String description = "";
        if (field.isAnnotationPresent(BooleanProperty.class)) {
            type = "Boolean";
            description = field.getAnnotation(BooleanProperty.class).description();
        } else if (field.isAnnotationPresent(NumericProperty.class)) {
            type = "Numeric";
            description = field.getAnnotation(NumericProperty.class).description();
        } else if (field.isAnnotationPresent(EnumProperty.class)) {
            type = "Enum";
            description = field.getAnnotation(EnumProperty.class).description();
        } else if (field.isAnnotationPresent(StringProperty.class)) {
            type = "String";
            description = field.getAnnotation(StringProperty.class).description();
        }

        Map<String, String> row = new LinkedHashMap<String, String>();
        row.put("key", key);
        row.put("type", type);
        row.put("description", description == null ? "" : description);
        row.put("mandatory", field.isAnnotationPresent(MandatoryProperty.class) ? "yes" : "no");
        row.put("category", categoryFor(key));

        propertyDefs.add(row);
        propertyDefsByKey.put(key, row);
    }
    Collections.sort(propertyDefs, new Comparator<Map<String, String>>() {
        @Override
        public int compare(Map<String, String> a, Map<String, String> b) {
            return a.get("key").compareTo(b.get("key"));
        }
    });

    Map<String, String> selectedProps = selectedBank == null ? Collections.<String, String>emptyMap() : selectedBank.getProperties();
    List<String> selectedPropKeys = new ArrayList<String>(selectedProps.keySet());
    Collections.sort(selectedPropKeys);

    List<String> missingMandatoryKeys = new ArrayList<String>();
    for (Map<String, String> row : propertyDefs) {
        if (!"yes".equals(row.get("mandatory"))) {
            continue;
        }
        String key = row.get("key");
        String value = selectedProps.get(key);
        if (isBlank(value)) {
            missingMandatoryKeys.add(key);
        }
    }
    Collections.sort(missingMandatoryKeys);

    String workflowActionParam = request.getParameter("workflowAction");
    boolean hasWorkflowAction = !isBlank(workflowActionParam);
    String workflowAction = hasWorkflowAction ? workflowActionParam.trim().toLowerCase(Locale.ENGLISH) : "draft";
    String draftVersion = request.getParameter("draftVersion");
    if (isBlank(draftVersion)) {
        draftVersion = "draft-" + System.currentTimeMillis();
    }
    String changeReason = request.getParameter("changeReason");
    if (changeReason == null) {
        changeReason = "";
    }
    String performedBy = request.getParameter("performedBy");
    if (isBlank(performedBy)) {
        performedBy = request.getRemoteUser();
    }
    if (isBlank(performedBy)) {
        performedBy = "portal-operator";
    }

    boolean validationPassed = selectedBank != null && !clusterKeys.isEmpty() && missingMandatoryKeys.isEmpty();
    String workflowStatus = "DRAFT";
    if (hasWorkflowAction) {
        if ("validate".equals(workflowAction)) {
            workflowStatus = validationPassed ? "VALIDATED" : "VALIDATION_FAILED";
        } else if ("approve".equals(workflowAction)) {
            workflowStatus = validationPassed ? "APPROVED" : "APPROVAL_BLOCKED";
        } else if ("publish".equals(workflowAction)) {
            workflowStatus = validationPassed ? "PUBLISHED" : "PUBLISH_BLOCKED";
        } else if ("rollback".equals(workflowAction)) {
            workflowStatus = "ROLLED_BACK";
        }
    }

    boolean configServiceEnabled = "true".equalsIgnoreCase(clusterProps.getProperty("CONFIG_PORTAL_USE_CONFIG_SERVICE", "false"));
    String configServiceHost = clusterProps.getProperty("CONFIG_SERVICE_HOST", "");
    String configServicePort = clusterProps.getProperty("CONFIG_SERVICE_PORT", "");
    String configServiceBaseUrl = isBlank(configServiceHost) || isBlank(configServicePort) ? "" : "http://" + configServiceHost + ":" + configServicePort;
    String configExecutionMode = "local-scaffold";
    String configSyncStatus = hasWorkflowAction ? "LOCAL_ONLY" : "IDLE";
    String configSyncMessage = "";

    if (hasWorkflowAction && configServiceEnabled) {
        if (selectedBankId == null) {
            configSyncStatus = "SKIPPED";
            configSyncMessage = "No bank selected.";
        } else if (isBlank(configServiceBaseUrl)) {
            configSyncStatus = "SKIPPED";
            configSyncMessage = "Config service host/port is not set.";
        } else {
            String payload;
            String targetUrl;
            if ("draft".equals(workflowAction)) {
                targetUrl = configServiceBaseUrl + "/api/v1/config/drafts";
                payload = "{\"draftVersion\":\"" + jsonEscape(draftVersion) + "\","
                        + "\"bankId\":\"" + jsonEscape(String.valueOf(selectedBankId)) + "\","
                        + "\"performedBy\":\"" + jsonEscape(performedBy) + "\","
                        + "\"changeReason\":\"" + jsonEscape(changeReason) + "\","
                        + "\"payload\":{}}";
            } else {
                targetUrl = configServiceBaseUrl + "/api/v1/config/workflow/" + jsonEscape(workflowAction);
                payload = "{\"draftVersion\":\"" + jsonEscape(draftVersion) + "\","
                        + "\"performedBy\":\"" + jsonEscape(performedBy) + "\","
                        + "\"note\":\"" + jsonEscape(changeReason) + "\"}";
            }

            Map<String, String> remote = postJson(targetUrl, payload);
            String remoteError = remote.get("error");
            if (!isBlank(remoteError)) {
                configSyncStatus = "ERROR";
                configSyncMessage = remoteError;
            } else {
                int httpCode = Integer.parseInt(remote.get("code"));
                String remoteBody = remote.get("body");
                if (httpCode >= 200 && httpCode < 300) {
                    configExecutionMode = "config-service";
                    configSyncStatus = "SYNCED";
                    configSyncMessage = "HTTP " + httpCode;
                    String remoteStatus = extractJsonString(remoteBody, "status");
                    if (!isBlank(remoteStatus)) {
                        workflowStatus = remoteStatus;
                    }
                } else {
                    configSyncStatus = "ERROR";
                    configSyncMessage = "HTTP " + httpCode + " " + (remoteBody == null ? "" : remoteBody);
                }
            }
        }
    }

    if (workflowStatus.contains("FAILED") || workflowStatus.contains("BLOCKED")) {
        validationPassed = false;
    } else if ("VALIDATED".equals(workflowStatus) || "APPROVED".equals(workflowStatus) || "PUBLISHED".equals(workflowStatus)) {
        validationPassed = true;
    }

    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> draftStore =
            (Map<String, Map<String, String>>) session.getAttribute("configPortalDraftStore");
    if (draftStore == null) {
        draftStore = new LinkedHashMap<String, Map<String, String>>();
    }

    if (hasWorkflowAction) {
        String updatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Map<String, String> draftMeta = draftStore.get(draftVersion);
        if (draftMeta == null) {
            draftMeta = new LinkedHashMap<String, String>();
        }
        draftMeta.put("draftVersion", draftVersion);
        draftMeta.put("status", workflowStatus);
        draftMeta.put("bankId", selectedBankId == null ? "" : String.valueOf(selectedBankId));
        draftMeta.put("changeReason", changeReason);
        draftMeta.put("validationPassed", String.valueOf(validationPassed));
        draftMeta.put("updatedAt", updatedAt);
        draftMeta.put("executionMode", configExecutionMode);
        draftMeta.put("syncStatus", configSyncStatus);
        draftMeta.put("performedBy", performedBy);
        draftStore.put(draftVersion, draftMeta);

        while (draftStore.size() > 20) {
            Iterator<String> iterator = draftStore.keySet().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            } else {
                break;
            }
        }
        session.setAttribute("configPortalDraftStore", draftStore);
    }

    List<Map<String, String>> recentDrafts = new ArrayList<Map<String, String>>(draftStore.values());
    Collections.reverse(recentDrafts);
%>
<!DOCTYPE html>
<html>
<head>
    <title>GS Configuration Portal</title>
    <link rel="stylesheet" href="/support/css/bootstrap.min.css"/>
    <style>
        body {
            padding-bottom: 40px;
        }
        .section {
            margin-top: 18px;
            padding: 14px;
            border: 1px solid #ddd;
            border-radius: 4px;
            background: #fff;
        }
        .small-note {
            color: #666;
            font-size: 12px;
        }
        .mandatory {
            color: #0b6a29;
            font-weight: bold;
        }
        .badge-soft {
            background: #f2f2f2;
            color: #333;
            border-radius: 3px;
            padding: 2px 6px;
            font-size: 11px;
        }
        code {
            white-space: pre-wrap;
            word-break: break-word;
        }
        .approval-progress {
            height: 10px;
            background: #eceff3;
            border-radius: 8px;
            overflow: hidden;
            margin: 6px 0 10px 0;
        }
        .approval-progress-bar {
            height: 100%;
            width: 0;
            background: linear-gradient(90deg, #5bc0de, #5cb85c);
        }
        .approval-card {
            border: 1px solid #d7dde5;
            border-radius: 6px;
            padding: 10px 12px;
            background: #fbfcfe;
            margin-bottom: 10px;
        }
        .approval-card h5 {
            margin-top: 0;
            margin-bottom: 6px;
        }
        .approval-tools textarea {
            width: 100%;
            min-height: 90px;
            font-family: monospace;
            font-size: 12px;
        }
        .approval-mini-btn {
            margin-right: 6px;
            margin-bottom: 4px;
        }
        .approval-status-pill {
            display: inline-block;
            padding: 2px 6px;
            border-radius: 10px;
            font-size: 11px;
            font-weight: 600;
            background: #eef2f7;
        }
        .approval-status-pill.pending { background: #f8f1d5; color: #8a6d3b; }
        .approval-status-pill.approved { background: #dff0d8; color: #3c763d; }
        .approval-status-pill.rejected { background: #f2dede; color: #a94442; }
        .approval-status-pill.published { background: #d9edf7; color: #31708f; }
        .approval-status-pill.rolled_back { background: #fcf8e3; color: #8a6d3b; }
        .guardrail-card {
            border: 1px solid #d9e2ef;
            border-radius: 6px;
            background: #fafcff;
            padding: 10px 12px;
            margin-top: 12px;
        }
        .guardrail-pill {
            display: inline-block;
            padding: 2px 7px;
            border-radius: 10px;
            font-size: 11px;
            font-weight: 600;
            background: #eef2f7;
            margin-right: 6px;
        }
        .guardrail-pill.pass { background: #dff0d8; color: #3c763d; }
        .guardrail-pill.warn { background: #fcf8e3; color: #8a6d3b; }
        .guardrail-pill.fail { background: #f2dede; color: #a94442; }
        .guardrail-pill.info { background: #d9edf7; color: #31708f; }
        .guardrail-checklist label {
            display: block;
            font-weight: normal;
            margin-bottom: 4px;
        }
    </style>
</head>
<body>
<div class="container col-xs-12 col-sm-12 col-md-10 col-md-offset-1">
    <h2>Game Server Configuration Portal</h2>
    <p>
        Unified operator view for all configuration levels used by GS.<br/>
        <span class="small-note">Level 1: cluster hosts; Level 2: bank settings catalog; Level 3: effective bank values.</span>
    </p>
    <p class="small-note">
        Quick links:
        <a href="/support/index.jsp">Legacy Support Index</a> |
        <a href="/support/modernizationProgress.html">Progress</a> |
        <a href="/support/modernizationDocs.jsp">Docs Index</a> |
        <a href="/support/modernizationRunbook.jsp">Runbook</a> |
        <a href="/support/clusterHosts.jsp">Cluster Hosts</a>
    </p>
    <div class="section">
        <h4>GS Tool Directory</h4>
        <p class="small-note">
            This page is the practical entry point for GS operations. Use it as the one-page directory for launch endpoints,
            logs, diagnostics, API test pages, bank/subcasino editors, and configuration tools. Tools ending with
            <code>.do</code> are action endpoints; tools ending with <code>.jsp</code> are direct support pages.
            Some pages require a bank, subcasino, game, or account id to be useful.
        </p>

        <table class="table table-bordered table-striped table-condensed">
            <thead>
            <tr>
                <th style="width: 220px;">Area</th>
                <th style="width: 240px;">Endpoint</th>
                <th>Logic</th>
                <th>How to use</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Legacy full catalog</td>
                <td><a href="<%=absoluteUrl(request, "/support/index.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/index.jsp"))%></code></a></td>
                <td>Full legacy support-tools inventory shipped with GS.</td>
                <td>Open this if you need the widest possible list. Use this portal for the cleaner day-to-day shortlist.</td>
            </tr>
            <tr>
                <td>Config portal</td>
                <td><a href="<%=absoluteUrl(request, "/support/configPortal.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/configPortal.jsp"))%></code></a></td>
                <td>Unified view of cluster settings, bank-property catalog, and effective bank values.</td>
                <td>Start here to inspect configuration quickly, then jump to the detailed editor pages below.</td>
            </tr>
            <tr>
                <td>Cluster hosts</td>
                <td><a href="<%=absoluteUrl(request, "/support/clusterHosts.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/clusterHosts.jsp"))%></code></a></td>
                <td>Shows host, port, and cluster-level runtime wiring values.</td>
                <td>Use this to confirm service hostnames, ports, and deployment-level connection targets.</td>
            </tr>
            <tr>
                <td>Health check</td>
                <td><a href="<%=absoluteUrl(request, "/support/health/check.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/health/check.jsp"))%></code></a></td>
                <td>Fast liveness/readiness probe for the GS runtime.</td>
                <td>Open directly in a browser or curl it. Expected result in the current environment is HTTP 200.</td>
            </tr>
            <tr>
                <td>Guest launch</td>
                <td><a href="<%=absoluteUrl(request, "/startGuestgame?bankId=" + guestBankId + "&gameId=" + guestGameId + "&lang=" + guestLang)%>"><code><%=esc(absoluteUrl(request, "/startGuestgame?bankId=" + guestBankId + "&gameId=" + guestGameId + "&lang=" + guestLang))%></code></a></td>
                <td>Starts guest mode without casino-side auth; injects missing subCasinoId when possible.</td>
                <td>Use for game smoke checks where you want GS + MP + static only, without real wallet traffic.</td>
            </tr>
            <tr>
                <td>Real launch</td>
                <td><a href="<%=absoluteUrl(request, "/startgame?bankId=" + realBankId + "&subCasinoId=" + realSubCasinoId + "&gameId=" + realGameId + "&mode=real&token=" + realToken + "&lang=" + guestLang)%>"><code><%=esc(absoluteUrl(request, "/startgame?bankId=" + realBankId + "&subCasinoId=" + realSubCasinoId + "&gameId=" + realGameId + "&mode=real&token=" + realToken + "&lang=" + guestLang))%></code></a></td>
                <td>Starts real-money flow, validates token, and continues via wallet/casino integration.</td>
                <td>Use for real integration checks. In localhost-style setups, pass <code>subCasinoId</code> explicitly.</td>
            </tr>
            <tr>
                <td>Server configuration</td>
                <td><a href="<%=absoluteUrl(request, "/support/serverConfiguration.do")%>"><code><%=esc(absoluteUrl(request, "/support/serverConfiguration.do"))%></code></a></td>
                <td>Shows the server-configuration form backed by <code>ServerConfigurationAction</code>.</td>
                <td>Use when you need the GS-level server config page rather than bank-specific properties.</td>
            </tr>
            <tr>
                <td>Cache viewer</td>
                <td><a href="<%=absoluteUrl(request, "/support/cacheviewer.do")%>"><code><%=esc(absoluteUrl(request, "/support/cacheviewer.do"))%></code></a></td>
                <td>Reads and displays selected runtime cache contents.</td>
                <td>Use to inspect loaded cache objects without going directly to Cassandra or log files.</td>
            </tr>
            <tr>
                <td>Log viewer</td>
                <td><a href="<%=absoluteUrl(request, "/support/logviewer.do")%>"><code><%=esc(absoluteUrl(request, "/support/logviewer.do"))%></code></a></td>
                <td>Browser-side reader for GS support logs and captured HTTP call details.</td>
                <td>Use when you want a support UI for logs instead of tailing files or container output.</td>
            </tr>
            <tr>
                <td>Server statistics</td>
                <td><a href="<%=absoluteUrl(request, "/support/stat")%>"><code><%=esc(absoluteUrl(request, "/support/stat"))%></code></a></td>
                <td>Shows server-side request/query statistics.</td>
                <td>Use for quick traffic/performance snapshots. <code>?getter=true</code> can be slower but more detailed.</td>
            </tr>
            <tr>
                <td>System diagnosis</td>
                <td><a href="<%=absoluteUrl(request, "/systemdiagnosis.servlet")%>"><code><%=esc(absoluteUrl(request, "/systemdiagnosis.servlet"))%></code></a></td>
                <td>Runs a broader health/diagnostic servlet for the GS runtime.</td>
                <td>Use when you need a larger system-level diagnosis than the simple health check.</td>
            </tr>
            <tr>
                <td>API issues</td>
                <td><a href="<%=absoluteUrl(request, "/support/showAPIIssues.do")%>"><code><%=esc(absoluteUrl(request, "/support/showAPIIssues.do"))%></code></a></td>
                <td>Lists API issues captured by the support/API-issues action.</td>
                <td>Use this when partner or wallet integration errors need a support-side review page.</td>
            </tr>
            <tr>
                <td>Wallet info</td>
                <td><a href="<%=absoluteUrl(request, "/support/walletinfo.do")%>"><code><%=esc(absoluteUrl(request, "/support/walletinfo.do"))%></code></a></td>
                <td>Support form for wallet/account change tracing.</td>
                <td>Use when you need to inspect wallet/account transitions or support account-level balance questions.</td>
            </tr>
            <tr>
                <td>Wallet list</td>
                <td><a href="<%=absoluteUrl(request, "/support/viewWallets.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/viewWallets.jsp"))%></code></a></td>
                <td>Support-side wallet overview page.</td>
                <td>Use for a quick wallet inventory view before drilling into a specific account or bank.</td>
            </tr>
            <tr>
                <td>Subcasino / bank landing</td>
                <td><a href="<%=absoluteUrl(request, "/support/cache/bank/common/subcasinoSelect.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/cache/bank/common/subcasinoSelect.jsp"))%></code></a></td>
                <td>Main UI entry for bank/subcasino configuration work.</td>
                <td>Use this first when you want to add a bank, edit a bank, inspect a subcasino, or navigate to game configuration.</td>
            </tr>
            <tr>
                <td>Bank + subcasino control</td>
                <td><a href="<%=absoluteUrl(request, "/support/BankNSubCasinoControl.do")%>"><code><%=esc(absoluteUrl(request, "/support/BankNSubCasinoControl.do"))%></code></a></td>
                <td>Management entry for the combined banks/subcasinos control flow.</td>
                <td>Use when you want the consolidated control page rather than opening a single bank directly.</td>
            </tr>
            <tr>
                <td>Subcasino editor</td>
                <td><a href="<%=absoluteUrl(request, "/support/subCasino.do?subcasinoId=" + realSubCasinoId)%>"><code><%=esc(absoluteUrl(request, "/support/subCasino.do?subcasinoId=" + realSubCasinoId))%></code></a></td>
                <td>Shows and edits one subcasino, including linked banks and domain names.</td>
                <td>Open with a concrete <code>subcasinoId</code> to review domain mapping, bank membership, and related settings.</td>
            </tr>
            <tr>
                <td>Bank editor</td>
                <td><a href="<%=absoluteUrl(request, "/support/bankInfo.do?bankId=" + realBankId)%>"><code><%=esc(absoluteUrl(request, "/support/bankInfo.do?bankId=" + realBankId))%></code></a></td>
                <td>Shows and edits one bank’s effective configuration and game assignments.</td>
                <td>Use this to inspect or change one bank’s core settings after selecting it from the subcasino landing page.</td>
            </tr>
            <tr>
                <td>Bank properties editor</td>
                <td>
                    <a href="<%=absoluteUrl(request, "/support/bankSupport.do")%>"><code><%=esc(absoluteUrl(request, "/support/bankSupport.do"))%></code></a>
                    <br/>
                    <a href="<%=absoluteUrl(request, "/support/bankSelectAction.do?bankId=" + realBankId)%>"><code><%=esc(absoluteUrl(request, "/support/bankSelectAction.do?bankId=" + realBankId))%></code></a>
                </td>
                <td>Edits the property-level bank configuration set.</td>
                <td>Use <code>bankSupport.do</code> to choose a bank, then open <code>bankSelectAction.do</code> for direct property editing.</td>
            </tr>
            <tr>
                <td>Game editor</td>
                <td><a href="<%=absoluteUrl(request, "/support/loadgameinfo.do?bankId=" + realBankId + "&gameId=" + realGameId)%>"><code><%=esc(absoluteUrl(request, "/support/loadgameinfo.do?bankId=" + realBankId + "&gameId=" + realGameId))%></code></a></td>
                <td>Loads a bank/game pair into the support editor.</td>
                <td>Use this from bank info pages when you need game-specific limits, properties, or routing values.</td>
            </tr>
            <tr>
                <td>Domain whitelist</td>
                <td><a href="<%=absoluteUrl(request, "/support/domainwl.do")%>"><code><%=esc(absoluteUrl(request, "/support/domainwl.do"))%></code></a></td>
                <td>Domain whitelist / domain-management support page.</td>
                <td>Use this when validating which hostnames are allowed to launch under GS.</td>
            </tr>
            <tr>
                <td>API service tool</td>
                <td><a href="<%=absoluteUrl(request, "/tools/api/service.jsp")%>"><code><%=esc(absoluteUrl(request, "/tools/api/service.jsp"))%></code></a></td>
                <td>Support-side API helper page for testing/checking external service integration by bank.</td>
                <td>Use when you want to inspect bank-linked API wiring or test API-related support flows.</td>
            </tr>
            <tr>
                <td>Bank endpoints report</td>
                <td><a href="<%=absoluteUrl(request, "/tools/bankEndpoints.jsp")%>"><code><%=esc(absoluteUrl(request, "/tools/bankEndpoints.jsp"))%></code></a></td>
                <td>Shows endpoint-level bank release/report information.</td>
                <td>Use to review endpoint wiring and release-facing bank endpoint summaries.</td>
            </tr>
            <tr>
                <td>Wallets manager tool</td>
                <td><a href="<%=absoluteUrl(request, "/tools/walletsManager.jsp")%>"><code><%=esc(absoluteUrl(request, "/tools/walletsManager.jsp"))%></code></a></td>
                <td>Support tool for wallet/account operations with bank and subcasino context.</td>
                <td>Use when investigating wallet behavior beyond the simpler wallet-info page.</td>
            </tr>
            <tr>
                <td>Game history</td>
                <td><a href="<%=absoluteUrl(request, "/support/gamehistory.do?accountId=" + sampleAccountId)%>"><code><%=esc(absoluteUrl(request, "/support/gamehistory.do?accountId=" + sampleAccountId))%></code></a></td>
                <td>Support action for account-level game history lookup.</td>
                <td>Use when player support requires looking up a specific account’s round/session history.</td>
            </tr>
            <tr>
                <td>Compare banks</td>
                <td><a href="<%=absoluteUrl(request, "/support/compare/banks.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/compare/banks.jsp"))%></code></a></td>
                <td>Diffs bank-level configuration between two banks.</td>
                <td>Use before copying or aligning settings from one bank to another.</td>
            </tr>
            <tr>
                <td>Compare templates</td>
                <td><a href="<%=absoluteUrl(request, "/support/compare/gameTemplates.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/compare/gameTemplates.jsp"))%></code></a></td>
                <td>Diffs game-template configuration.</td>
                <td>Use when comparing template-level setup rather than bank-level values.</td>
            </tr>
            <tr>
                <td>Game config by banks</td>
                <td><a href="<%=absoluteUrl(request, "/support/GameConfig/getGamesConfigByBanks.jsp?banks=" + realBankId + "&editmode=true&mode=coins,frb,limit,defcoin")%>"><code><%=esc(absoluteUrl(request, "/support/GameConfig/getGamesConfigByBanks.jsp?banks=" + realBankId + "&editmode=true&mode=coins,frb,limit,defcoin"))%></code></a></td>
                <td>Bulk game-config inspection page across one or more banks.</td>
                <td>Use when you need a wider grid-style review of coins, FRB, limits, and default-coin setup.</td>
            </tr>
            <tr>
                <td>Cassandra account view</td>
                <td><a href="<%=absoluteUrl(request, "/support/showcassandra_account.jsp?accountId=" + sampleAccountId)%>"><code><%=esc(absoluteUrl(request, "/support/showcassandra_account.jsp?accountId=" + sampleAccountId))%></code></a></td>
                <td>Direct support-side Cassandra-backed account inspection page.</td>
                <td>Use for fast account debugging when you already know the account id and want storage-side details.</td>
            </tr>
            <tr>
                <td>Modernization docs</td>
                <td>
                    <a href="<%=absoluteUrl(request, "/support/modernizationDocs.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/modernizationDocs.jsp"))%></code></a>
                    <br/>
                    <a href="<%=absoluteUrl(request, "/support/modernizationRunbook.jsp")%>"><code><%=esc(absoluteUrl(request, "/support/modernizationRunbook.jsp"))%></code></a>
                </td>
                <td>Project-specific support docs, runbook, and migration notes.</td>
                <td>Use for modernization/rehearsal context rather than day-to-day bank operations.</td>
            </tr>
            </tbody>
        </table>
        <p class="small-note">
            Practical operator path for bank editing: <code>subcasinoSelect.jsp</code> -> <code>subCasino.do</code> -> <code>bankInfo.do</code> ->
            <code>loadgameinfo.do</code> or <code>bankSelectAction.do</code>. Practical operator path for diagnostics: <code>health/check.jsp</code> ->
            <code>logviewer.do</code> -> <code>cacheviewer.do</code> -> <code>showAPIIssues.do</code>.
        </p>
    </div>
    <% if (!isBlank(bankCacheWarning)) { %>
    <div class="alert alert-warning" style="margin-top: 10px;">
        <strong>Startup cache warning:</strong> <%=bankCacheWarning%>
    </div>
    <% } %>

    <form class="form-inline" method="get" action="/support/configPortal.jsp" style="margin-bottom: 10px;">
        <div class="form-group" style="margin-right: 10px;">
            <label for="bankId">Bank</label>
            <select class="form-control" id="bankId" name="bankId">
                <% for (Long bankId : bankIds) {
                    BankInfo bank = allBanksMap.get(bankId);
                    String extId = bank == null ? "" : bank.getExternalBankId();
                    String extDesc = bank == null ? "" : bank.getExternalBankIdDescription();
                    boolean isSelected = selectedBankId != null && selectedBankId.equals(bankId);
                %>
                <option value="<%=bankId%>" <%=isSelected ? "selected=\"selected\"" : ""%>>
                    <%=bankId%> | <%=esc(extId)%> | <%=esc(extDesc)%>
                </option>
                <% } %>
            </select>
        </div>
        <div class="form-group" style="margin-right: 10px;">
            <label for="q">Search</label>
            <input class="form-control" id="q" name="q" value="<%=esc(query)%>" placeholder="key, description, category"/>
        </div>
        <button type="submit" class="btn btn-primary">Apply</button>
        <a class="btn btn-default" href="/support/configPortal.jsp">Reset</a>
        <% if (selectedBankId != null) { %>
        <a class="btn btn-info" href="/support/bankSelectAction.do?bankId=<%=selectedBankId%>">Open Bank Editor</a>
        <% } %>
    </form>

    <div class="section">
        <h4>Level 1: Cluster Hosts</h4>
        <p class="small-note">
            Source: <code>cluster-hosts.properties</code> on classpath.
            Detailed key descriptions: <a href="/support/clusterHosts.jsp">/support/clusterHosts.jsp</a>
        </p>
        <% if (clusterKeys.isEmpty()) { %>
        <div class="alert alert-danger">cluster-hosts.properties is missing or empty.</div>
        <% } else { %>
        <table class="table table-bordered table-striped table-condensed">
            <thead>
            <tr>
                <th>Key</th>
                <th>Value</th>
            </tr>
            </thead>
            <tbody>
            <% for (String key : clusterKeys) {
                String value = clusterProps.getProperty(key);
                if (!matches(queryLower, key) && !matches(queryLower, value)) {
                    continue;
                }
            %>
            <tr>
                <td><code><%=esc(key)%></code></td>
                <td><code><%=esc(value)%></code></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </div>

    <div class="section">
        <h4>Level 1b: Session Outbox Safety Controls</h4>
        <p class="small-note">
            These keys control Kafka outbox retry, DLQ behavior, and manual replay safety for canary banks.
        </p>
        <table class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th>Key</th>
                <th>Current Value</th>
                <th>Purpose</th>
            </tr>
            </thead>
            <tbody>
            <%
                String[][] outboxControlRows = new String[][]{
                        {"SESSION_SERVICE_OUTBOX_RELAY_ENABLED", "Enable or disable Kafka outbox relay worker."},
                        {"SESSION_SERVICE_OUTBOX_TOPIC", "Main topic for session lifecycle events."},
                        {"SESSION_SERVICE_OUTBOX_DLQ_TOPIC", "DLQ topic for events that exceed retry attempts."},
                        {"SESSION_SERVICE_OUTBOX_MAX_ATTEMPTS", "Max publish attempts before moving event to DLQ."},
                        {"SESSION_SERVICE_OUTBOX_RETRY_BASE_MS", "Base delay in ms for exponential retry backoff."},
                        {"SESSION_SERVICE_OUTBOX_BATCH_LIMIT", "Max outbox events processed per relay cycle."},
                        {"SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT", "Max allowed manual replays per DLQ event."},
                        {"SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS", "Minimum seconds between replays for same event."}
                };
                for (String[] row : outboxControlRows) {
                    String key = row[0];
                    String desc = row[1];
                    String value = clusterProps.getProperty(key, "");
                    boolean missing = isBlank(value);
            %>
            <tr>
                <td><code><%=esc(key)%></code></td>
                <td>
                    <% if (missing) { %>
                    <span style="color:#a94442;font-weight:bold;">MISSING</span>
                    <% } else { %>
                    <code><%=esc(value)%></code>
                    <% } %>
                </td>
                <td><%=esc(desc)%></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <p class="small-note">
            Runtime endpoints: <code>/api/v1/outbox?status=NEW|RETRY|DLQ</code>,
            <code>/api/v1/outbox/replay-report</code>,
            <code>POST /api/v1/outbox/:eventId/requeue?reason=...</code>.
        </p>
    </div>

    <div class="section">
        <h4>Level 2: Bank Settings Catalog</h4>
        <p class="small-note">Auto-generated from <code>BankInfo</code> annotated keys and descriptions.</p>
        <table class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th style="width:220px;">Category</th>
                <th>Meaning</th>
            </tr>
            </thead>
            <tbody>
            <tr><td><code>Wallet</code></td><td>Balance/auth/settlement and financial routing controls.</td></tr>
            <tr><td><code>Bonus/FRB</code></td><td>Free rounds, bonus wallet, and bonus validation controls.</td></tr>
            <tr><td><code>Multiplayer</code></td><td>Lobby, room, and websocket multiplayer behavior.</td></tr>
            <tr><td><code>Integration URL</code></td><td>Outbound endpoint addresses used for partner integrations.</td></tr>
            <tr><td><code>Game Limits</code></td><td>Bet/min/max operational boundaries per bank/game.</td></tr>
            <tr><td><code>Legacy MQ</code></td><td>Legacy-era keys still supported for compatibility mapping.</td></tr>
            <tr><td><code>General</code></td><td>Cross-cutting settings not specific to one domain.</td></tr>
            </tbody>
        </table>
        <table class="table table-bordered table-striped table-condensed">
            <thead>
            <tr>
                <th>Key</th>
                <th>Type</th>
                <th>Category</th>
                <th>Mandatory</th>
                <th>Description</th>
            </tr>
            </thead>
            <tbody>
            <%
                int catalogCount = 0;
                for (Map<String, String> row : propertyDefs) {
                    String key = row.get("key");
                    String type = row.get("type");
                    String category = row.get("category");
                    String mandatory = row.get("mandatory");
                    String description = row.get("description");
                    if (!matches(queryLower, key) && !matches(queryLower, type) && !matches(queryLower, category) && !matches(queryLower, description)) {
                        continue;
                    }
                    catalogCount++;
            %>
            <tr>
                <td><code><%=esc(key)%></code></td>
                <td><span class="badge-soft"><%=esc(type)%></span></td>
                <td><%=esc(category)%></td>
                <td><%="yes".equals(mandatory) ? "<span class=\"mandatory\">yes</span>" : "no"%></td>
                <td><%=esc(description)%></td>
            </tr>
            <% } %>
            <% if (catalogCount == 0) { %>
            <tr>
                <td colspan="5"><em>No catalog rows match current filter.</em></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>

    <div class="section">
        <h4>Level 3: Effective Values (Bank <%=selectedBankId == null ? "N/A" : selectedBankId%>)</h4>
        <% if (selectedBank == null) { %>
        <div class="alert alert-warning">No bank loaded in cache.</div>
        <% } else { %>
        <table class="table table-bordered table-condensed">
            <tr>
                <th style="width: 200px;">Bank Id</th>
                <td><code><%=selectedBank.getId()%></code></td>
            </tr>
            <tr>
                <th>External Bank Id</th>
                <td><code><%=esc(selectedBank.getExternalBankId())%></code></td>
            </tr>
            <tr>
                <th>External Description</th>
                <td><%=esc(selectedBank.getExternalBankIdDescription())%></td>
            </tr>
            <tr>
                <th>SubCasino Id</th>
                <td><code><%=selectedBank.getSubCasinoId()%></code></td>
            </tr>
            <tr>
                <th>Default Currency</th>
                <td><code><%=selectedBank.getDefaultCurrency() == null ? "" : esc(selectedBank.getDefaultCurrency().getCode())%></code></td>
            </tr>
            <tr>
                <th>Configured Property Count</th>
                <td><code><%=selectedProps.size()%></code></td>
            </tr>
        </table>

        <table class="table table-bordered table-striped table-condensed">
            <thead>
            <tr>
                <th>Key</th>
                <th>Value</th>
                <th>Type</th>
                <th>Category</th>
                <th>Description</th>
            </tr>
            </thead>
            <tbody>
            <%
                int valueCount = 0;
                for (String key : selectedPropKeys) {
                    String value = selectedProps.get(key);
                    Map<String, String> meta = propertyDefsByKey.get(key);
                    String type = meta == null ? "Custom" : meta.get("type");
                    String category = meta == null ? "Custom" : meta.get("category");
                    String description = meta == null ? "Defined outside BankInfo annotations" : meta.get("description");
                    if (!matches(queryLower, key) && !matches(queryLower, value) && !matches(queryLower, type) && !matches(queryLower, category) && !matches(queryLower, description)) {
                        continue;
                    }
                    valueCount++;
            %>
            <tr>
                <td><code><%=esc(key)%></code></td>
                <td><code><%=esc(value)%></code></td>
                <td><span class="badge-soft"><%=esc(type)%></span></td>
                <td><%=esc(category)%></td>
                <td><%=esc(description)%></td>
            </tr>
            <% } %>
            <% if (valueCount == 0) { %>
            <tr>
                <td colspan="5"><em>No effective values match current filter.</em></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </div>

    <div class="section">
        <h4>Level 4: Config Workflow (Draft -> Validate -> Approve -> Publish -> Rollback)</h4>
        <p class="small-note">
            Workflow bridge mode: uses config-service when feature flag is enabled, otherwise falls back to local scaffold.
        </p>
        <table class="table table-bordered table-condensed">
            <tr>
                <th style="width: 220px;">Workflow Status</th>
                <td><code><%=esc(workflowStatus)%></code></td>
            </tr>
            <tr>
                <th>Draft Version</th>
                <td><code><%=esc(draftVersion)%></code></td>
            </tr>
            <tr>
                <th>Selected Bank</th>
                <td><code><%=selectedBankId == null ? "N/A" : selectedBankId%></code></td>
            </tr>
            <tr>
                <th>Execution Mode</th>
                <td><code><%=esc(configExecutionMode)%></code></td>
            </tr>
            <tr>
                <th>Sync Status</th>
                <td><code><%=esc(configSyncStatus)%></code></td>
            </tr>
            <tr>
                <th>Sync Message</th>
                <td><code><%=esc(configSyncMessage)%></code></td>
            </tr>
            <tr>
                <th>Validation Result</th>
                <td><%=validationPassed ? "<span class=\"mandatory\">PASS</span>" : "<span style=\"color:#a94442;font-weight:bold;\">FAIL</span>"%></td>
            </tr>
            <tr>
                <th>Validation Details</th>
                <td>
                    <% if (selectedBank == null) { %>
                    <div>No bank selected in cache.</div>
                    <% } %>
                    <% if (clusterKeys.isEmpty()) { %>
                    <div>cluster-hosts.properties is empty.</div>
                    <% } %>
                    <% if (!missingMandatoryKeys.isEmpty()) { %>
                    <div>Missing mandatory keys: <code><%=esc(String.join(", ", missingMandatoryKeys))%></code></div>
                    <% } %>
                    <% if (selectedBank != null && !clusterKeys.isEmpty() && missingMandatoryKeys.isEmpty()) { %>
                    <div>All baseline checks passed for scaffold workflow.</div>
                    <% } %>
                </td>
            </tr>
        </table>

        <form method="get" action="/support/configPortal.jsp" class="form-inline">
            <input type="hidden" name="bankId" value="<%=selectedBankId == null ? "" : selectedBankId%>"/>
            <input type="hidden" name="q" value="<%=esc(query)%>"/>
            <input type="hidden" name="performedBy" value="<%=esc(performedBy)%>"/>

            <div class="form-group" style="margin-right: 10px;">
                <label for="draftVersion">Draft Version</label>
                <input id="draftVersion" name="draftVersion" class="form-control" value="<%=esc(draftVersion)%>"/>
            </div>
            <div class="form-group" style="margin-right: 10px;">
                <label for="changeReason">Change Reason</label>
                <input id="changeReason" name="changeReason" class="form-control" style="min-width: 280px;" value="<%=esc(changeReason)%>" placeholder="operator note"/>
            </div>

            <button class="btn btn-default" type="submit" name="workflowAction" value="draft">Save Draft</button>
            <button class="btn btn-info" type="submit" name="workflowAction" value="validate">Validate</button>
            <button class="btn btn-primary" type="submit" name="workflowAction" value="approve">Approve</button>
            <button class="btn btn-success" id="workflowPublishBtn" type="submit" name="workflowAction" value="publish">Publish</button>
            <button class="btn btn-warning" id="workflowRollbackBtn" type="submit" name="workflowAction" value="rollback">Rollback</button>
        </form>

        <div class="guardrail-card" id="configWorkflowGuardrails"
             data-workflow-status="<%=esc(workflowStatus)%>"
             data-validation-passed="<%=String.valueOf(validationPassed)%>"
             data-sync-status="<%=esc(configSyncStatus)%>"
             data-execution-mode="<%=esc(configExecutionMode)%>"
             data-selected-bank-id="<%=selectedBankId == null ? "" : String.valueOf(selectedBankId)%>"
             data-draft-version="<%=esc(draftVersion)%>"
             data-canary-banks="<%=esc(clusterProps.getProperty("SESSION_SERVICE_CANARY_BANKS", ""))%>">
            <h5 style="margin-top:0;">Level 4c: Publish/Rollback Guardrails + Canary Controls (Browser Local)</h5>
            <p class="small-note">
                Visual guardrails for operators before publish/rollback. Checks below are stored in your browser for this bank+draft and do not change backend workflow behavior.
            </p>
            <div id="guardrailSummaryPills" style="margin-bottom:8px;">Loading guardrails...</div>
            <div id="guardrailSummaryText" class="small-note" style="margin-bottom:8px;"></div>
            <div class="row">
                <div class="col-sm-7">
                    <table class="table table-bordered table-condensed table-striped" style="margin-bottom:8px;">
                        <thead>
                        <tr>
                            <th>Rule</th>
                            <th>Status</th>
                            <th>Detail</th>
                        </tr>
                        </thead>
                        <tbody id="guardrailRulesBody">
                        <tr><td colspan="3"><em>Loading...</em></td></tr>
                        </tbody>
                    </table>
                </div>
                <div class="col-sm-5">
                    <div class="guardrail-checklist">
                        <label><input type="checkbox" id="guardCheckCanary"/> Canary smoke executed for selected bank/profile</label>
                        <label><input type="checkbox" id="guardCheckRollbackPlan"/> Rollback plan reviewed and ready</label>
                        <label><input type="checkbox" id="guardCheckPeerReview"/> Peer review / operator approval captured</label>
                        <label><input type="checkbox" id="guardCheckIncidentComms"/> Incident/comms channel prepared (for publish/rollback)</label>
                    </div>
                    <div style="margin-top:8px;">
                        <button type="button" class="btn btn-default btn-xs" id="guardRefreshBtn">Refresh Guardrails</button>
                        <button type="button" class="btn btn-warning btn-xs" id="guardResetChecksBtn">Reset Guardrail Checks</button>
                    </div>
                    <div id="guardrailLocalStatus" class="small-note" style="margin-top:6px;">No local guardrail action yet.</div>
                </div>
            </div>
        </div>

        <h5 style="margin-top:16px;">Session Draft Registry (latest 20)</h5>
        <table class="table table-bordered table-striped table-condensed">
            <thead>
            <tr>
                <th>Draft Version</th>
                <th>Status</th>
                <th>Bank</th>
                <th>Validation</th>
                <th>Mode</th>
                <th>Sync</th>
                <th>Updated At</th>
                <th>By</th>
                <th>Change Reason</th>
                <th>Approval Queue</th>
            </tr>
            </thead>
            <tbody>
            <% if (recentDrafts.isEmpty()) { %>
            <tr>
                <td colspan="10"><em>No drafts in current session yet.</em></td>
            </tr>
            <% } else { %>
            <% for (Map<String, String> row : recentDrafts) { %>
            <tr>
                <td><code><%=esc(row.get("draftVersion"))%></code></td>
                <td><code><%=esc(row.get("status"))%></code></td>
                <td><code><%=esc(row.get("bankId"))%></code></td>
                <td><code><%=esc(row.get("validationPassed"))%></code></td>
                <td><code><%=esc(row.get("executionMode"))%></code></td>
                <td><code><%=esc(row.get("syncStatus"))%></code></td>
                <td><code><%=esc(row.get("updatedAt"))%></code></td>
                <td><code><%=esc(row.get("performedBy"))%></code></td>
                <td><%=esc(row.get("changeReason"))%></td>
                <td>
                    <button type="button"
                            class="btn btn-xs btn-default approval-queue-btn"
                            data-draft-version="<%=esc(row.get("draftVersion"))%>"
                            data-status="<%=esc(row.get("status"))%>"
                            data-bank-id="<%=esc(row.get("bankId"))%>"
                            data-validation-passed="<%=esc(row.get("validationPassed"))%>"
                            data-updated-at="<%=esc(row.get("updatedAt"))%>"
                            data-performed-by="<%=esc(row.get("performedBy"))%>"
                            data-change-reason="<%=esc(row.get("changeReason"))%>">
                        Queue
                    </button>
                </td>
            </tr>
            <% } %>
            <% } %>
            </tbody>
        </table>

        <h5 style="margin-top:16px;">Level 4b: Persistent Approval Queue (Browser Local)</h5>
        <p class="small-note">
            Local persistent approval tracking for operator workflow review. Data is stored in your browser (<code>localStorage</code>) and can be exported/imported as JSON bundle.
        </p>
        <div class="approval-card">
            <h5>Approval Overview</h5>
            <div id="approvalOverviewCounts" class="small-note">Loading...</div>
            <div class="approval-progress"><div id="approvalProgressBar" class="approval-progress-bar"></div></div>
            <div id="approvalOverviewMeta" class="small-note"></div>
        </div>

        <div class="row">
            <div class="col-sm-6">
                <div class="approval-card">
                    <h5>Queued Draft Approvals</h5>
                    <div class="small-note" id="approvalQueueHint">Use <code>Queue</code> from the session registry to add drafts.</div>
                    <table class="table table-bordered table-condensed table-striped">
                        <thead>
                        <tr>
                            <th>Draft</th>
                            <th>Bank</th>
                            <th>Status</th>
                            <th>Validation</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody id="approvalQueueBody">
                        <tr><td colspan="5"><em>No queued approvals yet.</em></td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="approval-card">
                    <h5>Approval Decision History</h5>
                    <div class="small-note">Latest 20 local approval decisions (approve/reject/publish/rollback/remove).</div>
                    <table class="table table-bordered table-condensed table-striped">
                        <thead>
                        <tr>
                            <th>Time</th>
                            <th>Draft</th>
                            <th>Decision</th>
                            <th>By</th>
                        </tr>
                        </thead>
                        <tbody id="approvalHistoryBody">
                        <tr><td colspan="4"><em>No local approval history yet.</em></td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="approval-card approval-tools">
            <h5>Approval Queue Bundle (Export / Import)</h5>
            <div style="margin-bottom:8px;">
                <button type="button" class="btn btn-default btn-sm approval-mini-btn" id="approvalExportBtn">Export Bundle JSON</button>
                <button type="button" class="btn btn-default btn-sm approval-mini-btn" id="approvalImportBtn">Import Bundle JSON</button>
                <button type="button" class="btn btn-warning btn-sm approval-mini-btn" id="approvalResetBtn">Reset Local Approval Queue</button>
                <button type="button" class="btn btn-info btn-sm approval-mini-btn" id="approvalRefreshBtn">Refresh View</button>
            </div>
            <textarea id="approvalBundleJson" placeholder='{"type":"config-portal-approval-bundle","version":1,...}'></textarea>
            <div id="approvalBundleStatus" class="small-note" style="margin-top:6px;">No bundle action yet.</div>
        </div>
    </div>
</div>
<script type="text/javascript">
(function () {
    var QUEUE_KEY = 'configPortalApprovalQueue.v1';
    var HISTORY_KEY = 'configPortalApprovalHistory.v1';
    var GUARD_KEY_PREFIX = 'configPortalWorkflowGuardrails.v1.';
    var MAX_HISTORY = 20;
    function safeParse(text, fallback) { try { return JSON.parse(text); } catch (e) { return fallback; } }
    function loadArray(key) {
        if (!window.localStorage) { return []; }
        var raw = window.localStorage.getItem(key);
        if (!raw) { return []; }
        var parsed = safeParse(raw, []);
        return parsed && typeof parsed.length === 'number' ? parsed : [];
    }
    function saveArray(key, arr) { if (window.localStorage) { window.localStorage.setItem(key, JSON.stringify(arr)); } }
    function loadQueue() { return loadArray(QUEUE_KEY); }
    function saveQueue(q) { saveArray(QUEUE_KEY, q); }
    function loadHistory() { return loadArray(HISTORY_KEY); }
    function saveHistory(h) { saveArray(HISTORY_KEY, h.slice(0, MAX_HISTORY)); }
    function nowIso() { return new Date().toISOString(); }
    function escHtml(v) {
        if (v === null || v === undefined) { return ''; }
        return String(v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');
    }
    function code(v) { return '<code>' + escHtml(v) + '</code>'; }
    function setBundleStatus(msg) {
        var el = document.getElementById('approvalBundleStatus');
        if (el) { el.innerHTML = escHtml(msg); }
    }
    function pushHistory(entry) {
        var h = loadHistory();
        h.unshift(entry);
        saveHistory(h);
    }
    function normalizeValidation(v) { return String(v || '').toLowerCase() === 'true' ? 'true' : 'false'; }
    function queueFromButton(btn) {
        var item = {
            draftVersion: btn.getAttribute('data-draft-version') || '',
            bankId: btn.getAttribute('data-bank-id') || '',
            workflowStatus: btn.getAttribute('data-status') || '',
            validationPassed: normalizeValidation(btn.getAttribute('data-validation-passed')),
            updatedAt: btn.getAttribute('data-updated-at') || '',
            performedBy: btn.getAttribute('data-performed-by') || 'portal-operator',
            changeReason: btn.getAttribute('data-change-reason') || '',
            localApprovalState: 'pending',
            queuedAt: nowIso(),
            localDecisionAt: ''
        };
        if (!item.draftVersion) { setBundleStatus('ERROR: missing draftVersion.'); return; }
        var q = loadQueue();
        var found = false;
        for (var i = 0; i < q.length; i++) {
            if (q[i].draftVersion === item.draftVersion) {
                item.localApprovalState = q[i].localApprovalState || 'pending';
                item.localDecisionAt = q[i].localDecisionAt || '';
                q[i] = item;
                found = true;
                break;
            }
        }
        if (!found) { q.unshift(item); }
        saveQueue(q);
        pushHistory({ at: nowIso(), draftVersion: item.draftVersion, decision: found ? 'requeue_update' : 'queued', performedBy: item.performedBy });
        setBundleStatus((found ? 'Updated queued draft: ' : 'Queued draft: ') + item.draftVersion);
        renderApprovals();
    }
    function setDecision(draftVersion, decision) {
        var q = loadQueue();
        for (var i = 0; i < q.length; i++) {
            if (q[i].draftVersion === draftVersion) {
                q[i].localApprovalState = decision;
                q[i].localDecisionAt = nowIso();
                saveQueue(q);
                pushHistory({ at: q[i].localDecisionAt, draftVersion: draftVersion, decision: decision, performedBy: q[i].performedBy || 'portal-operator' });
                setBundleStatus('Decision saved: ' + draftVersion + ' -> ' + decision);
                renderApprovals();
                return;
            }
        }
        setBundleStatus('ERROR: queued draft not found: ' + draftVersion);
    }
    function removeQueued(draftVersion) {
        var q = loadQueue();
        var next = [];
        var removed = false;
        for (var i = 0; i < q.length; i++) {
            if (q[i].draftVersion === draftVersion) {
                removed = true;
                pushHistory({ at: nowIso(), draftVersion: draftVersion, decision: 'removed', performedBy: q[i].performedBy || 'portal-operator' });
            } else {
                next.push(q[i]);
            }
        }
        if (!removed) { setBundleStatus('ERROR: queued draft not found: ' + draftVersion); return; }
        saveQueue(next);
        setBundleStatus('Removed queued draft: ' + draftVersion);
        renderApprovals();
    }
    function renderApprovals() {
        var q = loadQueue();
        var h = loadHistory();
        var counts = { total: q.length, pending:0, approved:0, rejected:0, published:0, rolled_back:0 };
        for (var i = 0; i < q.length; i++) {
            var st = q[i].localApprovalState || 'pending';
            counts[st] = (counts[st] || 0) + 1;
        }
        var resolved = (counts.approved || 0) + (counts.published || 0) + (counts.rolled_back || 0);
        var pct = counts.total ? Math.round((resolved / counts.total) * 100) : 0;
        var countsEl = document.getElementById('approvalOverviewCounts');
        var metaEl = document.getElementById('approvalOverviewMeta');
        var barEl = document.getElementById('approvalProgressBar');
        var queueBody = document.getElementById('approvalQueueBody');
        var historyBody = document.getElementById('approvalHistoryBody');
        var hintEl = document.getElementById('approvalQueueHint');
        if (countsEl) {
            countsEl.innerHTML = 'Queued <b>' + counts.total + '</b> | Pending <b>' + (counts.pending||0) + '</b> | Approved <b>' + (counts.approved||0) + '</b> | Rejected <b>' + (counts.rejected||0) + '</b> | Published <b>' + (counts.published||0) + '</b> | Rolled Back <b>' + (counts.rolled_back||0) + '</b>';
        }
        if (metaEl) { metaEl.innerHTML = 'Local approval progress: <b>' + pct + '%</b> resolved from queued drafts.'; }
        if (barEl) { barEl.style.width = pct + '%'; }
        if (hintEl) { hintEl.innerHTML = counts.total ? 'Queue is persisted in your browser and survives page refresh.' : 'Use <code>Queue</code> from the session registry to add drafts.'; }
        if (queueBody) {
            if (!q.length) {
                queueBody.innerHTML = '<tr><td colspan=\"5\"><em>No queued approvals yet.</em></td></tr>';
            } else {
                var out = [];
                for (var j = 0; j < q.length; j++) {
                    var row = q[j];
                    var st2 = row.localApprovalState || 'pending';
                    out.push('<tr><td>' + code(row.draftVersion) + '<div class=\"small-note\">' + escHtml(row.updatedAt || row.queuedAt) + '</div></td>' +
                        '<td>' + code(row.bankId) + '</td>' +
                        '<td><span class=\"approval-status-pill ' + escHtml(st2) + '\">' + escHtml(st2) + '</span></td>' +
                        '<td>' + code(row.validationPassed) + '</td>' +
                        '<td>' +
                        '<button type=\"button\" class=\"btn btn-xs btn-success approval-mini-btn\" data-approve-draft=\"' + escHtml(row.draftVersion) + '\">Approve</button>' +
                        '<button type=\"button\" class=\"btn btn-xs btn-warning approval-mini-btn\" data-publish-draft=\"' + escHtml(row.draftVersion) + '\">Publish</button>' +
                        '<button type=\"button\" class=\"btn btn-xs btn-danger approval-mini-btn\" data-reject-draft=\"' + escHtml(row.draftVersion) + '\">Reject</button>' +
                        '<button type=\"button\" class=\"btn btn-xs btn-default approval-mini-btn\" data-rollback-draft=\"' + escHtml(row.draftVersion) + '\">Rollback</button>' +
                        '<button type=\"button\" class=\"btn btn-xs btn-link approval-mini-btn\" data-remove-draft=\"' + escHtml(row.draftVersion) + '\">Remove</button>' +
                        '</td></tr>');
                }
                queueBody.innerHTML = out.join('');
            }
        }
        if (historyBody) {
            if (!h.length) {
                historyBody.innerHTML = '<tr><td colspan=\"4\"><em>No local approval history yet.</em></td></tr>';
            } else {
                var hOut = [];
                for (var k = 0; k < h.length && k < MAX_HISTORY; k++) {
                    var hh = h[k];
                    hOut.push('<tr><td>' + code(hh.at) + '</td><td>' + code(hh.draftVersion) + '</td><td><span class=\"approval-status-pill ' + escHtml(hh.decision) + '\">' + escHtml(hh.decision) + '</span></td><td>' + code(hh.performedBy) + '</td></tr>');
                }
                historyBody.innerHTML = hOut.join('');
            }
        }
    }
    function exportBundle() {
        var bundle = { type: 'config-portal-approval-bundle', version: 1, exportedAt: nowIso(), queue: loadQueue(), history: loadHistory() };
        document.getElementById('approvalBundleJson').value = JSON.stringify(bundle, null, 2);
        setBundleStatus('Exported bundle JSON (' + bundle.queue.length + ' queue, ' + bundle.history.length + ' history).');
    }
    function importBundle() {
        var txt = document.getElementById('approvalBundleJson').value || '';
        if (!txt.replace(/\\s+/g, '').length) { setBundleStatus('ERROR: bundle JSON is empty.'); return; }
        var bundle = safeParse(txt, null);
        if (!bundle) { setBundleStatus('ERROR: invalid JSON.'); return; }
        if (!bundle.queue || typeof bundle.queue.length !== 'number' || !bundle.history || typeof bundle.history.length !== 'number') {
            setBundleStatus('ERROR: bundle must contain queue[] and history[].'); return;
        }
        saveQueue(bundle.queue);
        saveHistory(bundle.history);
        setBundleStatus('Imported bundle JSON (' + bundle.queue.length + ' queue, ' + bundle.history.length + ' history).');
        renderApprovals();
    }
    function resetLocal() {
        if (!window.confirm('Reset local approval queue and history in this browser?')) { setBundleStatus('Reset cancelled.'); return; }
        if (window.localStorage) {
            window.localStorage.removeItem(QUEUE_KEY);
            window.localStorage.removeItem(HISTORY_KEY);
        }
        document.getElementById('approvalBundleJson').value = '';
        setBundleStatus('Local approval queue/history reset.');
        renderApprovals();
    }
    function guardPanel() { return document.getElementById('configWorkflowGuardrails'); }
    function guardContext() {
        var p = guardPanel();
        if (!p) { return null; }
        return {
            workflowStatus: p.getAttribute('data-workflow-status') || '',
            validationPassed: normalizeValidation(p.getAttribute('data-validation-passed')) === 'true',
            syncStatus: p.getAttribute('data-sync-status') || '',
            executionMode: p.getAttribute('data-execution-mode') || '',
            selectedBankId: p.getAttribute('data-selected-bank-id') || '',
            draftVersion: p.getAttribute('data-draft-version') || '',
            canaryBanksCsv: p.getAttribute('data-canary-banks') || ''
        };
    }
    function guardStorageKey(ctx) {
        return GUARD_KEY_PREFIX + (ctx.selectedBankId || 'none') + '.' + (ctx.draftVersion || 'draft');
    }
    function defaultGuardChecks() {
        return { canary:false, rollbackPlan:false, peerReview:false, incidentComms:false, updatedAt:'' };
    }
    function loadGuardChecks(ctx) {
        var d = defaultGuardChecks();
        if (!ctx || !window.localStorage) { return d; }
        var parsed = safeParse(window.localStorage.getItem(guardStorageKey(ctx)) || '', null);
        if (!parsed) { return d; }
        d.canary = !!parsed.canary;
        d.rollbackPlan = !!parsed.rollbackPlan;
        d.peerReview = !!parsed.peerReview;
        d.incidentComms = !!parsed.incidentComms;
        d.updatedAt = parsed.updatedAt || '';
        return d;
    }
    function saveGuardChecks(ctx, checks) {
        if (!ctx || !window.localStorage) { return; }
        checks.updatedAt = nowIso();
        window.localStorage.setItem(guardStorageKey(ctx), JSON.stringify(checks));
    }
    function setGuardLocalStatus(msg) {
        var el = document.getElementById('guardrailLocalStatus');
        if (el) { el.innerHTML = escHtml(msg); }
    }
    function csvContains(csv, val) {
        if (!csv || !val) { return false; }
        var parts = String(csv).split(',');
        for (var i = 0; i < parts.length; i++) {
            if (String(parts[i]).trim() === String(val)) { return true; }
        }
        return false;
    }
    function guardRule(name, ok, detail, level) {
        return { name:name, ok:!!ok, detail:detail || '', level: level || (ok ? 'pass' : 'fail') };
    }
    function readGuardChecksFromInputs() {
        return {
            canary: !!(document.getElementById('guardCheckCanary') || {}).checked,
            rollbackPlan: !!(document.getElementById('guardCheckRollbackPlan') || {}).checked,
            peerReview: !!(document.getElementById('guardCheckPeerReview') || {}).checked,
            incidentComms: !!(document.getElementById('guardCheckIncidentComms') || {}).checked,
            updatedAt: nowIso()
        };
    }
    function writeGuardChecksToInputs(checks) {
        var m = {
            guardCheckCanary: 'canary',
            guardCheckRollbackPlan: 'rollbackPlan',
            guardCheckPeerReview: 'peerReview',
            guardCheckIncidentComms: 'incidentComms'
        };
        for (var id in m) {
            if (!m.hasOwnProperty(id)) { continue; }
            var el = document.getElementById(id);
            if (el) { el.checked = !!checks[m[id]]; }
        }
    }
    function renderGuardrails() {
        var ctx = guardContext();
        if (!ctx) { return; }
        var checks = loadGuardChecks(ctx);
        writeGuardChecksToInputs(checks);

        var isCanaryBank = csvContains(ctx.canaryBanksCsv, ctx.selectedBankId);
        var rules = [];
        rules.push(guardRule('Validation passed', ctx.validationPassed, ctx.validationPassed ? 'Workflow validation is PASS.' : 'Validation failed/blocked.', ctx.validationPassed ? 'pass' : 'fail'));
        rules.push(guardRule('Config sync state', ctx.syncStatus === 'SYNCED' || ctx.syncStatus === 'LOCAL_ONLY' || ctx.syncStatus === 'IDLE', 'syncStatus=' + (ctx.syncStatus || 'n/a'), (ctx.syncStatus === 'ERROR') ? 'fail' : 'info'));
        rules.push(guardRule('Selected bank canary coverage', isCanaryBank, isCanaryBank ? 'Bank is in SESSION_SERVICE_CANARY_BANKS.' : 'Bank not in SESSION_SERVICE_CANARY_BANKS.', isCanaryBank ? 'pass' : 'warn'));
        rules.push(guardRule('Local: canary smoke executed', checks.canary, checks.canary ? 'Operator marked canary smoke complete.' : 'Operator has not marked canary smoke.', checks.canary ? 'pass' : 'warn'));
        rules.push(guardRule('Local: rollback plan ready', checks.rollbackPlan, checks.rollbackPlan ? 'Rollback plan confirmed.' : 'Rollback plan not confirmed.', checks.rollbackPlan ? 'pass' : 'warn'));
        rules.push(guardRule('Local: peer review', checks.peerReview, checks.peerReview ? 'Peer review captured.' : 'Peer review not marked.', checks.peerReview ? 'pass' : 'warn'));
        rules.push(guardRule('Local: incident/comms ready', checks.incidentComms, checks.incidentComms ? 'Comms channel prepared.' : 'Comms readiness not marked.', checks.incidentComms ? 'pass' : 'warn'));

        var rulesBody = document.getElementById('guardrailRulesBody');
        if (rulesBody) {
            var rows = [];
            for (var i = 0; i < rules.length; i++) {
                var r = rules[i];
                var label = r.ok ? 'PASS' : (r.level === 'warn' ? 'WARN' : 'FAIL');
                rows.push('<tr><td>' + escHtml(r.name) + '</td><td><span class=\"guardrail-pill ' + escHtml(r.level) + '\">' + label + '</span></td><td>' + escHtml(r.detail) + '</td></tr>');
            }
            rulesBody.innerHTML = rows.join('');
        }

        var missing = [];
        if (!checks.canary) { missing.push('canary smoke'); }
        if (!checks.rollbackPlan) { missing.push('rollback plan'); }
        if (!checks.peerReview) { missing.push('peer review'); }
        if (!checks.incidentComms) { missing.push('incident/comms'); }
        var summaryPills = document.getElementById('guardrailSummaryPills');
        if (summaryPills) {
            var pillHtml = [];
            pillHtml.push('<span class=\"guardrail-pill ' + (ctx.validationPassed ? 'pass' : 'fail') + '\">validation ' + (ctx.validationPassed ? 'PASS' : 'FAIL') + '</span>');
            pillHtml.push('<span class=\"guardrail-pill ' + (isCanaryBank ? 'pass' : 'warn') + '\">canary bank ' + (isCanaryBank ? 'YES' : 'NO') + '</span>');
            pillHtml.push('<span class=\"guardrail-pill ' + (missing.length ? 'warn' : 'pass') + '\">local checks ' + (missing.length ? 'INCOMPLETE' : 'READY') + '</span>');
            pillHtml.push('<span class=\"guardrail-pill info\">workflow ' + escHtml(ctx.workflowStatus || 'DRAFT') + '</span>');
            summaryPills.innerHTML = pillHtml.join('');
        }
        var summaryText = document.getElementById('guardrailSummaryText');
        if (summaryText) {
            var msg = missing.length ? ('Missing local checks: ' + missing.join(', ') + '.') : 'All local guardrail checks are marked.';
            if (checks.updatedAt) { msg += ' Last updated: ' + checks.updatedAt; }
            summaryText.innerHTML = escHtml(msg);
        }
    }
    function persistGuardChecksFromInputs() {
        var ctx = guardContext();
        if (!ctx) { return; }
        saveGuardChecks(ctx, readGuardChecksFromInputs());
        setGuardLocalStatus('Guardrail checks saved for bank ' + (ctx.selectedBankId || 'N/A') + ' / draft ' + (ctx.draftVersion || 'draft') + '.');
        renderGuardrails();
    }
    function resetGuardChecks() {
        var ctx = guardContext();
        if (!ctx) { return; }
        if (!window.confirm('Reset local guardrail checks for this bank/draft?')) { setGuardLocalStatus('Guardrail reset cancelled.'); return; }
        if (window.localStorage) { window.localStorage.removeItem(guardStorageKey(ctx)); }
        setGuardLocalStatus('Guardrail checks reset.');
        renderGuardrails();
    }
    function workflowGuardConfirm(actionName) {
        var ctx = guardContext();
        if (!ctx) { return true; }
        var checks = loadGuardChecks(ctx);
        var missing = [];
        if (!checks.rollbackPlan) { missing.push('rollback plan'); }
        if (!checks.peerReview) { missing.push('peer review'); }
        if (!checks.incidentComms) { missing.push('incident/comms'); }
        if ('publish' === actionName && !checks.canary) { missing.unshift('canary smoke'); }
        if (!missing.length) { return true; }
        return window.confirm('Guardrail warning for ' + actionName + ': missing ' + missing.join(', ') + '. Continue anyway?');
    }
    document.addEventListener('click', function (evt) {
        var t = evt.target;
        if (!t) { return; }
        if (String(t.className || '').indexOf('approval-queue-btn') >= 0) { queueFromButton(t); return; }
        if (t.getAttribute('data-approve-draft')) { setDecision(t.getAttribute('data-approve-draft'), 'approved'); return; }
        if (t.getAttribute('data-publish-draft')) { setDecision(t.getAttribute('data-publish-draft'), 'published'); return; }
        if (t.getAttribute('data-reject-draft')) { setDecision(t.getAttribute('data-reject-draft'), 'rejected'); return; }
        if (t.getAttribute('data-rollback-draft')) { setDecision(t.getAttribute('data-rollback-draft'), 'rolled_back'); return; }
        if (t.getAttribute('data-remove-draft')) { removeQueued(t.getAttribute('data-remove-draft')); return; }
    });
    var ex = document.getElementById('approvalExportBtn');
    var im = document.getElementById('approvalImportBtn');
    var re = document.getElementById('approvalResetBtn');
    var rf = document.getElementById('approvalRefreshBtn');
    if (ex) { ex.onclick = exportBundle; }
    if (im) { im.onclick = importBundle; }
    if (re) { re.onclick = resetLocal; }
    if (rf) { rf.onclick = renderApprovals; }
    var gc = ['guardCheckCanary', 'guardCheckRollbackPlan', 'guardCheckPeerReview', 'guardCheckIncidentComms'];
    for (var gi = 0; gi < gc.length; gi++) {
        var gEl = document.getElementById(gc[gi]);
        if (gEl) { gEl.onchange = persistGuardChecksFromInputs; }
    }
    var gRefresh = document.getElementById('guardRefreshBtn');
    var gReset = document.getElementById('guardResetChecksBtn');
    if (gRefresh) { gRefresh.onclick = function () { renderGuardrails(); setGuardLocalStatus('Guardrails refreshed.'); }; }
    if (gReset) { gReset.onclick = resetGuardChecks; }
    var pubBtn = document.getElementById('workflowPublishBtn');
    var rbBtn = document.getElementById('workflowRollbackBtn');
    if (pubBtn) {
        pubBtn.addEventListener('click', function (evt) {
            if (!workflowGuardConfirm('publish')) { evt.preventDefault(); setGuardLocalStatus('Publish cancelled by guardrail confirmation.'); }
        });
    }
    if (rbBtn) {
        rbBtn.addEventListener('click', function (evt) {
            if (!workflowGuardConfirm('rollback')) { evt.preventDefault(); setGuardLocalStatus('Rollback cancelled by guardrail confirmation.'); }
        });
    }
    renderApprovals();
    renderGuardrails();
})();
</script>
</body>
</html>
