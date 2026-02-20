package com.dgphoenix.casino.actions.enter.game.routing;

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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * GS compatibility bridge for history-service canary shadowing.
 * Behavior is fail-open to legacy flow: any config/read/network issue disables routing/shadow calls.
 */
public final class HistoryServiceRoutingBridge {
    private static final Logger LOG = LogManager.getLogger(HistoryServiceRoutingBridge.class);
    private static final HistoryServiceRoutingBridge INSTANCE = new HistoryServiceRoutingBridge();

    private static final String RESOURCE_CLUSTER_HOSTS = "cluster-hosts.properties";
    private static final String KEY_HOST = "HISTORY_SERVICE_HOST";
    private static final String KEY_PORT = "HISTORY_SERVICE_PORT";
    private static final String KEY_ROUTE_ENABLED = "HISTORY_SERVICE_ROUTE_ENABLED";
    private static final String KEY_CANARY_BANKS = "HISTORY_SERVICE_CANARY_BANKS";

    private static final int CONNECT_TIMEOUT_MS = 400;
    private static final int READ_TIMEOUT_MS = 700;
    private static final int MAX_BODY_BYTES = 4096;

    private HistoryServiceRoutingBridge() {
    }

    public static HistoryServiceRoutingBridge getInstance() {
        return INSTANCE;
    }

    public RouteDecision decide(long bankId) {
        Config config = loadConfig();
        String bankIdValue = String.valueOf(bankId);

        if (!config.valid) {
            return RouteDecision.disabled("missing_config", config.baseUrl());
        }
        if (!config.routeEnabled) {
            return RouteDecision.disabled("route_disabled", config.baseUrl());
        }
        if (!config.canaryBanks.contains(bankIdValue)) {
            return RouteDecision.disabled("bank_not_in_canary", config.baseUrl());
        }

        Boolean remoteDecision = fetchRemoteDecision(config, bankIdValue);
        if (remoteDecision == null) {
            return RouteDecision.disabled("remote_unavailable", config.baseUrl());
        }
        return remoteDecision
                ? RouteDecision.enabled("canary_enabled", config.baseUrl())
                : RouteDecision.disabled("remote_disabled", config.baseUrl());
    }

    public void shadowAppendRecord(RouteDecision decision,
                                   long bankId,
                                   String sessionId,
                                   String roundId,
                                   String eventType,
                                   String operationId,
                                   String traceId) {
        if (decision == null || !decision.routeToHistoryService) {
            return;
        }
        if (isBlank(sessionId) || isBlank(eventType) || isBlank(operationId)) {
            LOG.debug("history-service shadow skipped: bankId={}, sessionId={}, eventType={}, operationId={}",
                    bankId, sessionId, eventType, operationId);
            return;
        }

        Config config = loadConfig();
        if (!config.valid) {
            LOG.debug("history-service shadow skipped: config unavailable");
            return;
        }

        String payload = "{"
                + "\"bankId\":\"" + escapeJson(String.valueOf(bankId)) + "\","
                + "\"sessionId\":\"" + escapeJson(sessionId) + "\","
                + "\"operationId\":\"" + escapeJson(operationId) + "\","
                + "\"eventType\":\"" + escapeJson(eventType) + "\","
                + "\"payload\":{"
                + "\"source\":\"gs-ngs-history-shadow\","
                + "\"roundId\":\"" + escapeJson(nullToEmpty(roundId)) + "\","
                + "\"traceId\":\"" + escapeJson(nullToEmpty(traceId)) + "\""
                + "}"
                + "}";

        HttpResult httpResult = postJson(config.baseUrl() + "/api/v1/history/records", payload);
        if (httpResult.status >= 200 && httpResult.status < 300) {
            LOG.debug("history-service shadow append ok: bankId={}, sessionId={}, operationId={}, status={}, body={}",
                    bankId, sessionId, operationId, httpResult.status, httpResult.body);
            return;
        }

        LOG.warn("history-service shadow append failed, fallback to legacy only: bankId={}, sessionId={}, operationId={}, status={}, body={}",
                bankId, sessionId, operationId, httpResult.status, httpResult.body);
    }

    private Boolean fetchRemoteDecision(Config config, String bankIdValue) {
        HttpURLConnection conn = null;
        try {
            String endpoint = config.baseUrl() + "/api/v1/history/routing/decision?bankId="
                    + URLEncoder.encode(bankIdValue, StandardCharsets.UTF_8.name());
            conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            int status = conn.getResponseCode();
            String body = readBody(conn);
            if (status < 200 || status > 299) {
                LOG.warn("history-service decision call failed: status={}, body={}", status, body);
                return null;
            }
            if (body.contains("\"routeToHistoryService\":true")) {
                return Boolean.TRUE;
            }
            if (body.contains("\"routeToHistoryService\":false")) {
                return Boolean.FALSE;
            }
            LOG.warn("history-service decision response missing routeToHistoryService flag: {}", body);
            return null;
        } catch (Exception e) {
            LOG.warn("history-service decision unavailable: {}", e.getMessage());
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

        if (isBlank(host) || isBlank(port)) {
            return Config.invalid();
        }
        return new Config(true, host, port, routeEnabled, canaryBanks);
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

        Config(boolean valid, String host, String port, boolean routeEnabled, Set<String> canaryBanks) {
            this.valid = valid;
            this.host = host;
            this.port = port;
            this.routeEnabled = routeEnabled;
            this.canaryBanks = canaryBanks;
        }

        static Config invalid() {
            return new Config(false, "", "", false, Collections.emptySet());
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
        private final boolean routeToHistoryService;
        private final String reason;
        private final String endpoint;

        private RouteDecision(boolean routeToHistoryService, String reason, String endpoint) {
            this.routeToHistoryService = routeToHistoryService;
            this.reason = reason;
            this.endpoint = endpoint;
        }

        static RouteDecision enabled(String reason, String endpoint) {
            return new RouteDecision(true, reason, endpoint);
        }

        static RouteDecision disabled(String reason, String endpoint) {
            return new RouteDecision(false, reason, endpoint);
        }

        public boolean isRouteToHistoryService() {
            return routeToHistoryService;
        }

        public String getReason() {
            return reason;
        }

        public String getEndpoint() {
            return endpoint;
        }

        @Override
        public String toString() {
            return "RouteDecision{"
                    + "routeToHistoryService=" + routeToHistoryService
                    + ", reason='" + reason + '\''
                    + ", endpoint='" + endpoint + '\''
                    + '}';
        }
    }
}
