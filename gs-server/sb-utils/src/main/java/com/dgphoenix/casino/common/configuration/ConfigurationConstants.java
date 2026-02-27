package com.abs.casino.common.configuration;

public interface ConfigurationConstants {
    public static final String DEFAULT_ARRAY_DELIMITER = " ";

    public static final String KEY_DOMAIN = "domain";
    public static final String KEY_GS_DOMAIN = "gsDomain";

    public static final String KEY_SERVER_SESSION_TIMEOUT = "server.session.timeout";

    public static final String KEY_REQUEST_TIMEOUT = "server.http.request.timeout";

    public static final String KEY_ID_GENERATOR_START_VALUE = "ID_GENERATOR_START_VALUE";

    public static final String KEY_SERVER_UPDATE_TIMEOUT = "SERVER_UPDATE_TIMEOUT";

    public static final String KEY_CASINO_SYSTEM_TYPE = "CASINO_SYSTEM_TYPE";
    public static final String KEY_TRUST_ALL_SSL_FOR_HTTP_CLIENT = "TRUST_ALL_SSL_FOR_HTTP_CLIENT";
    public final static String KEY_USE_CASSANDRA_STORAGE_FOR_TRANSFER_ACCOUNTS =
            "USE_CASSANDRA_STORAGE_FOR_TRANSFER_ACCOUNTS";
    public final static String KEY_USE_STRONG_CONSISTENCY_CASSANDRA_STORAGE = "USE_STRONG_CONSISTENCY_CASSANDRA_STORAGE";
    public final static String KEY_USE_CASSANDRA_FOR_STORE_WALLET_OPS = "USE_CASSANDRA_FOR_STORE_WALLET_OPS";
    public final static String KEY_ENABLE_CN_TOP_PLAYERS_UPDATER = "ENABLE_CN_TOP_PLAYERS_UPDATER";

    public static final String KEY_SESSION_TRACKER_MAX_SLEEP_TIME = "session.tracker.max.sleep.time";

    public final static String KEY_SOURCE_VERSION = "SOURCE_VERSION";
    public final static String KEY_USE_STATIC_VERSIONING = "USE_STATIC_VERSIONING";

    public final static String KEY_TRUSTED_IP = "trusted.ip";
    public final static String KEY_HTTP_PROXY_HOST = "http.proxy.host";
    public final static String KEY_HTTP_PROXY_PORT = "http.proxy.port";
    public final static String KEY_TESTSTAND_ENABLED = "teststand.enabled";
}
