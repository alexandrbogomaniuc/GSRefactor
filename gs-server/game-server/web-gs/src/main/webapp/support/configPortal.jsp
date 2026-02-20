<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.property.BooleanProperty" %>
<%@ page import="com.dgphoenix.casino.common.util.property.EnumProperty" %>
<%@ page import="com.dgphoenix.casino.common.util.property.MandatoryProperty" %>
<%@ page import="com.dgphoenix.casino.common.util.property.NumericProperty" %>
<%@ page import="com.dgphoenix.casino.common.util.property.StringProperty" %>
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

    Map<Long, BankInfo> allBanksMap = BankInfoCache.getInstance().getAllObjects();
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
    </style>
</head>
<body>
<div class="container col-xs-12 col-sm-12 col-md-10 col-md-offset-1">
    <h2>Game Server Configuration Portal</h2>
    <p>
        Unified operator view for all configuration levels used by GS.<br/>
        <span class="small-note">Level 1: cluster hosts; Level 2: bank settings catalog; Level 3: effective bank values.</span>
    </p>

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
            <button class="btn btn-success" type="submit" name="workflowAction" value="publish">Publish</button>
            <button class="btn btn-warning" type="submit" name="workflowAction" value="rollback">Rollback</button>
        </form>

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
            </tr>
            </thead>
            <tbody>
            <% if (recentDrafts.isEmpty()) { %>
            <tr>
                <td colspan="9"><em>No drafts in current session yet.</em></td>
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
            </tr>
            <% } %>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
