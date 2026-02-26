package com.abs.casino.actions.enter.game.routing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * GS compatibility bridge for multiplayer-service canary shadowing.
 * Behavior is fail-open to legacy flow: any config/read/network issue disables routing/shadow calls.
 */
public final class MultiplayerServiceRoutingBridge {
    private static final Logger LOG = LogManager.getLogger(MultiplayerServiceRoutingBridge.class);
    private static final MultiplayerServiceRoutingBridge INSTANCE = new MultiplayerServiceRoutingBridge();

    public static final String REQUEST_ROUTE_ATTRIBUTE = "route.multiplayerService";

    private static final String RESOURCE_CLUSTER_HOSTS = "cluster-hosts.properties";
    private static final String KEY_HOST = "MULTIPLAYER_SERVICE_HOST";
    private static final String KEY_PORT = "MULTIPLAYER_SERVICE_PORT";
    private static final String KEY_ROUTE_ENABLED = "MULTIPLAYER_SERVICE_ROUTE_ENABLED";
    private static final String KEY_CANARY_BANKS = "MULTIPLAYER_SERVICE_CANARY_BANKS";
    private static final String KEY_BANK_FLAGS = "MULTIPLAYER_SERVICE_BANK_FLAGS";

    private static final int CONNECT_TIMEOUT_MS = 400;
    private static final int READ_TIMEOUT_MS = 700;
    private static final int MAX_BODY_BYTES = 4096;

    private MultiplayerServiceRoutingBridge() {
    }

    public static MultiplayerServiceRoutingBridge getInstance() {
        return INSTANCE;
    }

    public RouteDecision decide(long bankId, int gameId, String sessionId, boolean isMultiplayerPath) {
        Config config = loadConfig();
        String bankIdValue = String.valueOf(bankId);

        if (!isMultiplayerPath) {
            return RouteDecision.disabled("non_multiplayer_game", config.baseUrl(), true);
        }
        if (!config.valid) {
            return RouteDecision.disabled("missing_config", config.baseUrl(), true);
        }
        if (!isBankMultiplayerEnabled(config, bankIdValue)) {
            return RouteDecision.disabled("bank_multiplayer_disabled", config.baseUrl(), false);
        }
        if (!config.routeEnabled) {
            return RouteDecision.disabled("route_disabled", config.baseUrl(), true);
        }
        if (!config.canaryBanks.contains(bankIdValue)) {
            return RouteDecision.disabled("bank_not_in_canary", config.baseUrl(), true);
        }

        Boolean remoteDecision = fetchRemoteDecision(config, bankIdValue, gameId, sessionId);
        if (remoteDecision == null) {
            return RouteDecision.disabled("remote_unavailable", config.baseUrl(), true);
        }
        return remoteDecision
                ? RouteDecision.enabled("canary_enabled", config.baseUrl(), true)
                : RouteDecision.disabled("remote_disabled", config.baseUrl(), true);
    }

    public void shadowSessionSync(RouteDecision decision,
                                  long bankId,
                                  String sessionId,
                                  String playerId,
                                  int gameId,
                                  String roomId,
                                  String status) {
        if (decision == null || !decision.routeToMultiplayerService) {
            return;
        }
        if (isBlank(sessionId) || isBlank(playerId)) {
            LOG.debug("multiplayer-service shadow sync skipped: bankId={}, sessionId={}, playerId={}",
                    bankId, sessionId, playerId);
            return;
        }

        Config config = loadConfig();
        if (!config.valid) {
            LOG.debug("multiplayer-service shadow sync skipped: config unavailable");
            return;
        }

        String operationId = "launch-sync-" + sessionId + "-" + gameId;
        String payload = "{"
                + "\"bankId\":\"" + escapeJson(String.valueOf(bankId)) + "\","
                + "\"sessionId\":\"" + escapeJson(sessionId) + "\","
                + "\"playerId\":\"" + escapeJson(playerId) + "\","
                + "\"roomId\":\"" + escapeJson(nullToEmpty(roomId)) + "\","
                + "\"operationId\":\"" + escapeJson(operationId) + "\","
                + "\"status\":\"" + escapeJson(isBlank(status) ? "SYNCED" : status) + "\""
                + "}";

        HttpResult httpResult = postJson(config.baseUrl() + "/api/v1/multiplayer/session/sync", payload);
        if (httpResult.status >= 200 && httpResult.status < 300) {
            LOG.debug("multiplayer-service shadow sync ok: bankId={}, sessionId={}, gameId={}, status={}, body={}",
                    bankId, sessionId, gameId, httpResult.status, httpResult.body);
            return;
        }

        LOG.warn("multiplayer-service shadow sync failed, fallback to legacy only: bankId={}, sessionId={}, gameId={}, status={}, body={}",
                bankId, sessionId, gameId, httpResult.status, httpResult.body);
    }

    private boolean isBankMultiplayerEnabled(Config config, String bankIdValue) {
        Boolean flag = config.bankFlags.get(bankIdValue);
        return flag == null || flag.booleanValue();
    }

    private Boolean fetchRemoteDecision(Config config, String bankIdValue, int gameId, String sessionId) {
        HttpURLConnection conn = null;
        try {
            String endpoint = config.baseUrl() + "/api/v1/multiplayer/routing/decision?bankId="
                    + URLEncoder.encode(bankIdValue, StandardCharsets.UTF_8.name())
                    + "&gameId=" + URLEncoder.encode(String.valueOf(gameId), StandardCharsets.UTF_8.name())
                    + "&sessionId=" + URLEncoder.encode(nullToEmpty(sessionId), StandardCharsets.UTF_8.name())
                    + "&isMultiplayer=true";
            conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            int status = conn.getResponseCode();
            String body = readBody(conn);
            if (status < 200 || status > 299) {
                LOG.warn("multiplayer-service decision call failed: status={}, body={}", status, body);
                return null;
            }
            if (body.contains("\"routeToMultiplayerService\":true")) {
                return Boolean.TRUE;
            }
            if (body.contains("\"routeToMultiplayerService\":false")) {
                return Boolean.FALSE;
            }
            LOG.warn("multiplayer-service decision response missing routeToMultiplayerService flag: {}", body);
            return null;
        } catch (Exception e) {
            LOG.warn("multiplayer-service decision unavailable: {}", e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private HttpResult postJson(String endpoint, String payload) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }
            int status = conn.getResponseCode();
            return new HttpResult(status, readBody(conn));
        } catch (Exception e) {
            return new HttpResult(-1, e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readBody(HttpURLConnection conn) {
        InputStream is = null;
        try {
            is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) {
                return "";
            }
            return readText(is, MAX_BODY_BYTES);
        } catch (Exception ignored) {
            return "";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                    // no-op
                }
            }
        }
    }

    private String readText(InputStream is, int maxBytes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int total = 0;
        int read;
        while ((read = is.read(buffer)) != -1) {
            if (total + read > maxBytes) {
                bos.write(buffer, 0, maxBytes - total);
                break;
            }
            bos.write(buffer, 0, read);
            total += read;
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private Config loadConfig() {
        Properties props = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_CLUSTER_HOSTS)) {
            if (is == null) {
                return Config.invalid();
            }
            props.load(is);
        } catch (Exception e) {
            LOG.warn("Failed to load {}: {}", RESOURCE_CLUSTER_HOSTS, e.getMessage());
            return Config.invalid();
        }

        String host = trim(props.getProperty(KEY_HOST));
        String port = trim(props.getProperty(KEY_PORT));
        boolean routeEnabled = "true".equalsIgnoreCase(trim(props.getProperty(KEY_ROUTE_ENABLED)));
        Set<String> canaryBanks = parseCsv(trim(props.getProperty(KEY_CANARY_BANKS)));
        Map<String, Boolean> bankFlags = parseBankFlags(trim(props.getProperty(KEY_BANK_FLAGS)));

        if (isBlank(host) || isBlank(port)) {
            return Config.invalid();
        }
        return new Config(true, host, port, routeEnabled, canaryBanks, bankFlags);
    }

    private Set<String> parseCsv(String value) {
        if (isBlank(value)) {
            return Collections.emptySet();
        }
        Set<String> out = new HashSet<>();
        String[] chunks = value.split(",");
        for (String chunk : chunks) {
            if (chunk == null) {
                continue;
            }
            String normalized = chunk.trim();
            if (!normalized.isEmpty()) {
                out.add(normalized);
            }
        }
        return out;
    }

    private Map<String, Boolean> parseBankFlags(String value) {
        if (isBlank(value)) {
            return Collections.emptyMap();
        }
        Map<String, Boolean> out = new HashMap<>();
        String[] pairs = value.split(",");
        for (String pair : pairs) {
            if (pair == null) {
                continue;
            }
            String[] chunks = pair.split(":", 2);
            if (chunks.length != 2) {
                continue;
            }
            String bankId = chunks[0] == null ? "" : chunks[0].trim();
            String rawFlag = chunks[1] == null ? "" : chunks[1].trim();
            if (bankId.isEmpty()) {
                continue;
            }
            out.put(bankId, "true".equalsIgnoreCase(rawFlag)
                    || "1".equals(rawFlag)
                    || "yes".equalsIgnoreCase(rawFlag));
        }
        return out;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String escapeJson(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class Config {
        final boolean valid;
        final String host;
        final String port;
        final boolean routeEnabled;
        final Set<String> canaryBanks;
        final Map<String, Boolean> bankFlags;

        Config(boolean valid,
               String host,
               String port,
               boolean routeEnabled,
               Set<String> canaryBanks,
               Map<String, Boolean> bankFlags) {
            this.valid = valid;
            this.host = host;
            this.port = port;
            this.routeEnabled = routeEnabled;
            this.canaryBanks = canaryBanks;
            this.bankFlags = bankFlags;
        }

        static Config invalid() {
            return new Config(false, "", "", false, Collections.emptySet(), Collections.emptyMap());
        }

        String baseUrl() {
            if (!valid) {
                return "";
            }
            return "http://" + host + ":" + port;
        }
    }

    private static final class HttpResult {
        final int status;
        final String body;

        HttpResult(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    public static final class RouteDecision {
        private final boolean routeToMultiplayerService;
        private final String reason;
        private final String endpoint;
        private final boolean bankMultiplayerEnabled;

        private RouteDecision(boolean routeToMultiplayerService,
                              String reason,
                              String endpoint,
                              boolean bankMultiplayerEnabled) {
            this.routeToMultiplayerService = routeToMultiplayerService;
            this.reason = reason;
            this.endpoint = endpoint;
            this.bankMultiplayerEnabled = bankMultiplayerEnabled;
        }

        static RouteDecision enabled(String reason, String endpoint, boolean bankMultiplayerEnabled) {
            return new RouteDecision(true, reason, endpoint, bankMultiplayerEnabled);
        }

        static RouteDecision disabled(String reason, String endpoint, boolean bankMultiplayerEnabled) {
            return new RouteDecision(false, reason, endpoint, bankMultiplayerEnabled);
        }

        public boolean isRouteToMultiplayerService() {
            return routeToMultiplayerService;
        }

        public String getReason() {
            return reason;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public boolean isBankMultiplayerEnabled() {
            return bankMultiplayerEnabled;
        }
    }
}
