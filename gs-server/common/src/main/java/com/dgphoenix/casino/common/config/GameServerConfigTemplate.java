package com.abs.casino.common.config;

import com.abs.casino.common.config.FreeSpaceThresholdType;
import com.abs.casino.common.config.MountMonitoringEntry;
import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.data.IDistributedConfigEntry;
import com.abs.casino.common.configuration.CasinoSystemType;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.property.BooleanProperty;
import com.abs.casino.common.util.property.NumericProperty;
import com.abs.casino.common.util.property.PropertyUtils;
import com.abs.casino.common.util.property.StringProperty;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * User: flsh
 * Date: 10.06.14.
 */
public class GameServerConfigTemplate implements IDistributedConfigEntry, Identifiable, KryoSerializable {
    public static final int TEMPLATE_ID = 1;
    private static final short VERSION = 2;
    private static final int DEFAULT_THRIFT_PORT = 6000;
    private static final int DEFAULT_CM_THRIFT_PORT = 6001;
    private static final Logger LOG = LogManager.getLogger(GameServerConfigTemplate.class);
    private long systemId = 1;
    private String host;
    private String gsDomain;
    private long delayExecutorTimeout = 50000;
    private boolean enableMassBonusCreator = true;
    private boolean dropFreeBalance = true;
    private boolean lastHandExpirationServer = false;
    private boolean stressTestMode = false;
    private String closeGameProcessorClassName = "com.abs.casino.gs.managers.payment.transfer.processor.DefaultCloseGameProcessor";
    private long lastHandExpirationPeriod = 60;
    private long lastHandExpirationCheckPeriod = 1440;
    private long freeBalance = DEFAULT_FREE_BALANCE;
    private long jackpotBaseBankStartValue = 0;
    private long gameTotalsUpdaterInterval = 5000;
    private long jackpotUpdaterInterval = 2000;
    private int maxLoadValue = 4000;
    private long logoutTrackerSleepTimeout = 5000;
    private int logoutTrackerThreadPoolSize = 50;
    private long walletTrackerSleepTimeout = 30000;
    private long frbonusWinTrackerSleepTimeout = 60000;
    private int walletTrackerThreadPoolSize = 50;
    private int frbonusWinTrackerThreadPoolSize = 5;
    private long bonusTrackerSleepTimeout = 30000;
    private int bonusTrackerThreadPoolSize = 20;
    private long serverUpdaterUpdateInterval = 10000;
    private int asyncPersistenceThreadPoolSize = 50;
    private long asyncPersistenceTaskTimeout = 20000;
    private String winnerFeedPath;
    private long winnerFeedUpdateInterval = 30000;
    private String actionNameAfterGameClose = "logout";
    private String reportsOutputPath;
    private String reportsUploadUrl;
    private String exportCachePath;
    private String smtpServer;
    private String fromSupportEmail;
    private boolean ignoreCloseGameAction = false;
    private boolean useDistributedLocks = false;
    private boolean casinonoTournamentsEnabled = false;
    private String ntpServer;
    private long maxInnactivityAccountPeriodInHours = 1;
    private boolean usePostToSendAlerts = true;
    private String dailyWalletOperationPath;
    private String sshStaticLobbyHost;
    private int sshStaticLobbyPort;
    private String sshStaticLobbyUser;
    private String sshStaticLobbyPass;

    //from ServerConfiguration
    private List<Long> winnerFeedBanks;
    private String domain;
    private long requestTimeout = 60000;
    private String httpProxyHost;
    private int httpProxyPort = 8080;
    private long serverSessionTimeout = 600000;
    private CasinoSystemType casinoSystemType = CasinoSystemType.MULTIBANK;
    private boolean trustAllSslForHttpClientConnections = false;
    private long sessionTrackerMaxSleepTime = 60000;

    private boolean useStaticVersioning = false;
    private String sourceVersion = "1";

    private boolean enableXssSanitizer = false;
    private String disabledXssSanitizingDomains;
    private String disabledXssSanitizingUrls;

    private List<String> trustedIp;
    private transient List<SubnetUtils> trustedSubnetList = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();

    public static final String KEY_IS_ENABLED_COUNTRY_CHECK = "IS_ENABLED_COUNTRY_CHECK";
    public static final String KEY_GEO_IP_DATABASE_FILE = "GEO_IP_DATABASE_FILE";
    public static final String KEY_SHOW_MESSAGE_IN_TEST_MODE = "SHOW_MESSAGE_IN_TEST_MODE";
    public static final String KEY_RULES_PATH = "RULES_PATH";
    public static final String KEY_FAVORITE_GAMES_UPDATE_INTERVAL = "FAVORITE_GAMES_UPDATE_INTERVAL";
    public static final Long DEFAULT_FAVORITE_GAMES_UPDATE_INTERVAL = 300L;
    public static final String KEY_GAME_TOTALS_CACHE_MAX_SIZE = "GAME_TOTALS_CACHE_MAX_SIZE";
    public static final String KEY_BASE_GAME_CACHE_MAX_SIZE = "BASE_GAME_CACHE_MAX_SIZE";
    public static final int DEFAULT_GAME_TOTALS_CACHE_MAX_SIZE = 1000;
    public static final int DEFAULT_BASE_GAME_CACHE_MAX_SIZE = 1000;
    public static final String IS_HTTP_CLIENT_STATISTICS_ENABLED = "IS_HTTP_CLIENT_STATISTICS_ENABLED";
    public static final String KEY_FREE_SPACE_MONITORING_SETTINGS = "FREE_SPACE_MONITORING_SETTINGS";
    public static final String KEY_IS_TEST_SYSTEM = "IS_TEST_SYSTEM";
    public static final String KEY_ONLINE_CONCURRENT_MAIL_NOTIFICATION_LIMIT = "ONLINE_CONCURRENT_MAIL_NOTIFICATION_LIMIT";
    private static final int DEFAULT_ONLINE_CONCURRENT_NOTIFICATION_LIMIT = 5000;
    public static final String KEY_ONLINE_CONCURRENT_MAIL_NOTIFICATION_DELAY = "ONLINE_CONCURRENT_MAIL_NOTIFICATION_DELAY";
    private static final int DEFAULT_ONLINE_CONCURRENT_NOTIFICATION_DELAY = 60000;
    public static final String KEY_NOTIFICATION_MAIL = "NOTIFICATION_MAIL";
    public static final String KEY_DOC_TOOLS_DEFAULT_REMOTE_URL = "DOC_TOOLS_DEFAULT_REMOTE_URL";

    public static final String KEY_SSH_JACKPOTS_UPLOAD_HOSTS = "SSH_JACKPOTS_UPLOAD_HOST";
    private static final String SSH_JACKPOTS_UPLOAD_HOSTS_DELIMITER = ";";
    public static final String KEY_SSH_JACKPOTS_UPLOAD_PORT = "SSH_JACKPOTS_UPLOAD_PORT";
    public static final Integer DEFAULT_SSH_JACKPOTS_UPLOAD_PORT = 3000;

    public static final String KEY_SSH_STATISTIC_UPDATER_USER = "SSH_STATISTIC_UPDATER_USER";
    public static final String KEY_SSH_STATISTIC_UPDATER_PASS = "SSH_STATISTIC_UPDATER_PASS";
    public static final long DEFAULT_FREE_BALANCE = 100000;

    public static final String KEY_JAVA8_PROXY_URL = "JAVA8_PROXY_URL";
    public static final String KEY_IS_EMULATION_FILTER_ENABLED = "EMULATION_FILTER_ENABLED";
    private static final String KEY_ENABLE_SSL_CERTIFICATES_CHECKING = "ENABLE_SSL_CERTIFICATES_CHECKING";

    @BooleanProperty(description = "Send big win alerts to MAIL_LIST if win more than MIN_TO_SEND_BIG_WIN")
    public static final String KEY_IS_SEND_BIG_WIN = "IS_SEND_BIG_WIN";
    @NumericProperty(description = "Minimal amount in euro cents to consider when win is BIG")
    public static final String KEY_MIN_TO_SEND_BIG_WIN = "MIN_TO_SEND_BIG_WIN";
    @StringProperty(description = "Emails separated by ;")
    public static final String KEY_MAIL_LIST = "MAIL_LIST";

    @StringProperty(description = "WebSocket host for MP")
    public static final String KEY_MP_LOBBY_WS_HOST = "MP_LOBBY_WS_HOST";

    public static final String KEY_IS_DEMO_CLUSTER = "IS_DEMO_CLUSTER";
    public static final String KEY_DEMO_CLUSTER_DOMAIN = "DEMO_CLUSTER_DOMAIN";

    private static final String KEY_PROMO_FEEDS_ROOT_PATH = "PROMO_FEEDS_ROOT_PATH";
    private static final String PROMO_FEEDS_ROOT_DEFAULT_PATH = "/www/html/stat/info/promo";

    public static final String KEY_BUCKET_CLOSE_TIMEOUT = "BUCKET_CLOSE_TIMEOUT";
    public static final long DEFAULT_BUCKET_CLOSE_TIMEOUT = TimeUnit.MINUTES.toMillis(1);

    @NumericProperty
    public static final String KEY_ROUND_WINS_QUEUE_PERSISTENCE_INTERVAL = "ROUND_WINS_QUEUE_PERSISTENCE_INTERVAL";
    private static final int DEFAULT_ROUND_WINS_QUEUE_PERSISTENCE_INTERVAL = 5000;

    @StringProperty
    public static final String KEY_MAINTENANCE_PAGE = "MAINTENANCE_PAGE";

    @BooleanProperty
    public static final String KEY_TICKETED_DRAW_PROCESSOR_ENABLED = "TICKETED_DRAW_PROCESSOR_ENABLED";

    @NumericProperty
    public static final String KEY_LEADERBOARD_TRACKER_INTERVAL = "LEADERBOARD_TRACKER_INTERVAL";
    private static final long DEFAULT_LEADERBOARD_TRACKER_INTERVAL = 1800000;

    @NumericProperty
    public static final String KEY_LEADERBOARD_TRACKER_EXPIRE_TIME = "LEADERBOARD_TRACKER_EXPIRE_TIME";
    private static final long DEFAULT_LEADERBOARD_TRACKER_EXPIRE_TIME = 172800000;

    @StringProperty(description = "Subcasino + file name(247=checksumfilename.csv;270=checksumfilename2.csv)")
    public static final String KEY_HASHSUM_FILE_SETTINGS = "HASHSUM_FILE_SETTINGS";

    @StringProperty(description = "Email for support HASHSUM CSV file")
    public static final String KEY_EMAIL_HASHSUM_FILE_SUPPORT = "EMAIL_HASHSUM_FILE_SUPPORT";

    @NumericProperty(description = "Maximum calls for Common Wallet API Test per day")
    public static final String KEY_MAX_CW_API_TEST_CALLS_PER_DAY = "MAX_CW_API_TEST_CALLS_PER_DAY";

    @NumericProperty(description = "Cluster id")
    public static final String KEY_CLUSTER_ID = "CLUSTER_ID";

    @NumericProperty(description = "Total clusters count available for MQ tournaments")
    public static final String KEY_TOURNAMENT_CLUSTERS_COUNT = "TOURNAMENT_CLUSTERS_COUNT";

    @BooleanProperty(description = "Checking common errors for hour")
    public static final String KEY_DISCONTINUES_SAME_TYPE_ERRORS_DIAGNOSTIC_ENABLED = "DISCONTINUES_SAME_ERRORS_DIAGNOSTIC_ENABLED";

    @BooleanProperty(description = "Enable serialization/deserialization before persist bankInfo in CassandraBankInfoPersister")
    public static final String KEY_IS_NEED_DEBUG_SERIALIZE = "IS_NEED_DEBUG_SERIALIZE";

    @NumericProperty(description = "Multiplier for the free balance forming")
    public static final String KEY_FREE_BALANCE_MULTIPLIER = "FREEBALANCE_MULTIPLIER";

    @NumericProperty(description = "Pool thread size for CommonExecutorService")
    public static final String KEY_COMMON_EXECUTOR_SERVICE_POOL_SIZE = "COMMON_EXECUTOR_SERVICE_POOL_SIZE";
    private static final int DEFAULT_COMMON_EXECUTOR_SERVICE_POOL_SIZE = 10;

    @BooleanProperty(description = "Enable big storage cassandra cluster for some CF")
    public static final String KEY_ENABLE_BIG_STORAGE_CASSANDRA_CLUSTER = "ENABLE_BIG_STORAGE_CASSANDRA_CLUSTER";

    @StringProperty(description = "CrossCluster API endpoints (clusterId1=endpointUrl;clusterId2=endpointUrl2)")
    public static final String KEY_CROSS_CLUSTER_API_ENDPOINTS = "CROSS_CLUSTER_API_ENDPOINTS";

    @StringProperty(description = "ssh host for shared resources")
    public static final String KEY_SSH_SHARED_HOST = "SSH_SHARED_HOST";

    @StringProperty(description = "ssh port for shared resources")
    public static final String KEY_SSH_SHARED_PORT = "SSH_SHARED_PORT";

    @StringProperty(description = "ssh user for shared resources")
    public static final String KEY_SSH_SHARED_USER = "SSH_SHARED_USER";

    @StringProperty(description = "ssh password for shared resources")
    public static final String KEY_SSH_SHARED_PASSWORD = "SSH_SHARED_PASSWORD";

    @NumericProperty(description = "promo motivation message period in seconds")
    public static final String KEY_PROMO_MOTIVATION_MESSAGE_PERIOD = "PROMO_MOTIVATION_MESSAGE_PERIOD";

    @StringProperty(description = "Path to directory with game certificates")
    public static final String KEY_GAME_CERTIFICATE_DIRECTORY = "GAME_CERTIFICATE_DIRECTORY";

    @BooleanProperty(description = "Disable Jackpot History Updater")
    public static final String KEY_DISABLE_JP_HISTORY_UPDATER = "DISABLE_JP_HISTORY_UPDATER";

    @BooleanProperty(description = "Enable jackpot ticker API")
    public static final String KEY_JACKPOT_TICKER_ENABLED = "JACKPOT_TICKER_ENABLED";
    @StringProperty(description = "CM host")
    public static final String KEY_CM_HOST = "CM_HOST";


    public GameServerConfigTemplate() {
    }

    public long getSystemId() {
        return systemId;
    }

    public synchronized void setSystemId(long systemId) {
        this.systemId = systemId;
    }

    public String getHost() {
        return host;
    }

    public synchronized void setHost(String host) {
        this.host = host;
    }

    public String getGsDomain() {
        return gsDomain;
    }

    public synchronized void setGsDomain(String gsDomain) {
        this.gsDomain = gsDomain;
    }

    public long getDelayExecutorTimeout() {
        return delayExecutorTimeout;
    }

    public List<Long> getWinnerFeedBanks() {
        return winnerFeedBanks;
    }

    public void setWinnerFeedBanks(List<Long> winnerFeedBanks) {
        this.winnerFeedBanks = winnerFeedBanks;
    }

    public synchronized void setDelayExecutorTimeout(long delayExecutorTimeout) {
        this.delayExecutorTimeout = delayExecutorTimeout;
    }


    public boolean isEnableMassBonusCreator() {
        return enableMassBonusCreator;
    }

    public synchronized void setEnableMassBonusCreator(boolean enableMassBonusCreator) {
        this.enableMassBonusCreator = enableMassBonusCreator;
    }

    public boolean isDropFreeBalance() {
        return dropFreeBalance;
    }

    public synchronized void setDropFreeBalance(boolean dropFreeBalance) {
        this.dropFreeBalance = dropFreeBalance;
    }

    public boolean isLastHandExpirationServer() {
        return lastHandExpirationServer;
    }

    public synchronized void setLastHandExpirationServer(boolean lastHandExpirationServer) {
        this.lastHandExpirationServer = lastHandExpirationServer;
    }

    public boolean isStressTestMode() {
        return stressTestMode;
    }

    public synchronized void setStressTestMode(boolean stressTestMode) {
        this.stressTestMode = stressTestMode;
    }

    public String getCloseGameProcessorClassName() {
        return closeGameProcessorClassName;
    }

    public String getExportCachePath() {
        return exportCachePath;
    }

    public void setExportCachePath(String exportCachePath) {
        this.exportCachePath = exportCachePath;
    }

    public synchronized void setCloseGameProcessorClassName(String closeGameProcessorClassName) {
        this.closeGameProcessorClassName = closeGameProcessorClassName;
    }

    public long getLastHandExpirationPeriod() {
        return lastHandExpirationPeriod;
    }

    public synchronized void setLastHandExpirationPeriod(long lastHandExpirationPeriod) {
        this.lastHandExpirationPeriod = lastHandExpirationPeriod;
    }

    public long getLastHandExpirationCheckPeriod() {
        return lastHandExpirationCheckPeriod;
    }

    public synchronized void setLastHandExpirationCheckPeriod(long lastHandExpirationCheckPeriod) {
        this.lastHandExpirationCheckPeriod = lastHandExpirationCheckPeriod;
    }

    public long getFreeBalance() {
        return freeBalance;
    }

    public synchronized void setFreeBalance(long freeBalance) {
        this.freeBalance = freeBalance;
    }

    public Integer getFreeBalanceMultiplier() {
        return PropertyUtils.getIntProperty(properties, KEY_FREE_BALANCE_MULTIPLIER);
    }

    public long getJackpotBaseBankStartValue() {
        return jackpotBaseBankStartValue;
    }

    public synchronized void setJackpotBaseBankStartValue(long jackpotBaseBankStartValue) {
        this.jackpotBaseBankStartValue = jackpotBaseBankStartValue;
    }

    public long getJackpotUpdaterInterval() {
        return jackpotUpdaterInterval;
    }

    public synchronized void setJackpotUpdaterInterval(long jackpotUpdaterInterval) {
        this.jackpotUpdaterInterval = jackpotUpdaterInterval;
    }

    public int getMaxLoadValue() {
        return maxLoadValue;
    }

    public synchronized void setMaxLoadValue(int maxLoadValue) {
        this.maxLoadValue = maxLoadValue;
    }

    public long getLogoutTrackerSleepTimeout() {
        return logoutTrackerSleepTimeout;
    }

    public synchronized void setLogoutTrackerSleepTimeout(long logoutTrackerSleepTimeout) {
        this.logoutTrackerSleepTimeout = logoutTrackerSleepTimeout;
    }

    public int getLogoutTrackerThreadPoolSize() {
        return logoutTrackerThreadPoolSize;
    }

    public synchronized void setLogoutTrackerThreadPoolSize(int logoutTrackerThreadPoolSize) {
        this.logoutTrackerThreadPoolSize = logoutTrackerThreadPoolSize;
    }

    public long getWalletTrackerSleepTimeout() {
        return walletTrackerSleepTimeout;
    }

    public synchronized void setWalletTrackerSleepTimeout(long walletTrackerSleepTimeout) {
        this.walletTrackerSleepTimeout = walletTrackerSleepTimeout;
    }

    public long getFrbonusWinTrackerSleepTimeout() {
        return frbonusWinTrackerSleepTimeout;
    }

    public synchronized void setFrbonusWinTrackerSleepTimeout(long frbonusWinTrackerSleepTimeout) {
        this.frbonusWinTrackerSleepTimeout = frbonusWinTrackerSleepTimeout;
    }

    public int getWalletTrackerThreadPoolSize() {
        return walletTrackerThreadPoolSize;
    }

    public synchronized void setWalletTrackerThreadPoolSize(int walletTrackerThreadPoolSize) {
        this.walletTrackerThreadPoolSize = walletTrackerThreadPoolSize;
    }

    public int getFrbonusWinTrackerThreadPoolSize() {
        return frbonusWinTrackerThreadPoolSize;
    }

    public synchronized void setFrbonusWinTrackerThreadPoolSize(int frbonusWinTrackerThreadPoolSize) {
        this.frbonusWinTrackerThreadPoolSize = frbonusWinTrackerThreadPoolSize;
    }

    public long getBonusTrackerSleepTimeout() {
        return bonusTrackerSleepTimeout;
    }

    public synchronized void setBonusTrackerSleepTimeout(long bonusTrackerSleepTimeout) {
        this.bonusTrackerSleepTimeout = bonusTrackerSleepTimeout;
    }

    public int getBonusTrackerThreadPoolSize() {
        return bonusTrackerThreadPoolSize;
    }

    public synchronized void setBonusTrackerThreadPoolSize(int bonusTrackerThreadPoolSize) {
        this.bonusTrackerThreadPoolSize = bonusTrackerThreadPoolSize;
    }

    public long getServerUpdaterUpdateInterval() {
        return serverUpdaterUpdateInterval;
    }

    public synchronized void setServerUpdaterUpdateInterval(long serverUpdaterUpdateInterval) {
        this.serverUpdaterUpdateInterval = serverUpdaterUpdateInterval;
    }

    public int getAsyncPersistenceThreadPoolSize() {
        return asyncPersistenceThreadPoolSize;
    }

    public synchronized void setAsyncPersistenceThreadPoolSize(int asyncPersistenceThreadPoolSize) {
        this.asyncPersistenceThreadPoolSize = asyncPersistenceThreadPoolSize;
    }

    public long getAsyncPersistenceTaskTimeout() {
        return asyncPersistenceTaskTimeout;
    }

    public synchronized void setAsyncPersistenceTaskTimeout(long asyncPersistenceTaskTimeout) {
        this.asyncPersistenceTaskTimeout = asyncPersistenceTaskTimeout;
    }

    public String getWinnerFeedPath() {
        return winnerFeedPath;
    }

    public synchronized void setWinnerFeedPath(String winnerFeedPath) {
        this.winnerFeedPath = winnerFeedPath;
    }

    public long getWinnerFeedUpdateInterval() {
        return winnerFeedUpdateInterval;
    }

    public synchronized void setWinnerFeedUpdateInterval(long winnerFeedUpdateInterval) {
        this.winnerFeedUpdateInterval = winnerFeedUpdateInterval;
    }

    public String getActionNameAfterGameClose() {
        return actionNameAfterGameClose;
    }

    public synchronized void setActionNameAfterGameClose(String actionNameAfterGameClose) {
        this.actionNameAfterGameClose = actionNameAfterGameClose;
    }

    public String getReportsOutputPath() {
        return reportsOutputPath;
    }

    public synchronized void setReportsOutputPath(String reportsOutputPath) {
        this.reportsOutputPath = reportsOutputPath;
    }

    public String getReportsUploadUrl() {
        return reportsUploadUrl;
    }

    public synchronized void setReportsUploadUrl(String reportsUploadUrl) {
        this.reportsUploadUrl = reportsUploadUrl;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public synchronized void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public String getFromSupportEmail() {
        return fromSupportEmail;
    }

    public synchronized void setFromSupportEmail(String fromSupportEmail) {
        this.fromSupportEmail = fromSupportEmail;
    }

    public boolean isIgnoreCloseGameAction() {
        return ignoreCloseGameAction;
    }

    public synchronized void setIgnoreCloseGameAction(boolean ignoreCloseGameAction) {
        this.ignoreCloseGameAction = ignoreCloseGameAction;
    }

    public boolean isUseDistributedLocks() {
        return useDistributedLocks;
    }

    public void setUseDistributedLocks(boolean useDistributedLocks) {
        this.useDistributedLocks = useDistributedLocks;
    }

    public boolean isCasinonoTournamentsEnabled() {
        return casinonoTournamentsEnabled;
    }

    public synchronized void setCasinonoTournamentsEnabled(boolean casinonoTournamentsEnabled) {
        this.casinonoTournamentsEnabled = casinonoTournamentsEnabled;
    }

    public String getNtpServer() {
        return ntpServer;
    }

    public synchronized void setNtpServer(String ntpServer) {
        this.ntpServer = ntpServer;
    }

    public long getMaxInnactivityAccountPeriodInHours() {
        return maxInnactivityAccountPeriodInHours;
    }

    public synchronized void setMaxInnactivityAccountPeriodInHours(long maxInnactivityAccountPeriodInHours) {
        this.maxInnactivityAccountPeriodInHours = maxInnactivityAccountPeriodInHours;
    }

    public boolean isUsePostToSendAlerts() {
        return usePostToSendAlerts;
    }

    public synchronized void setUsePostToSendAlerts(boolean usePostToSendAlerts) {
        this.usePostToSendAlerts = usePostToSendAlerts;
    }

    public String getDailyWalletOperationPath() {
        return dailyWalletOperationPath;
    }

    public synchronized void setDailyWalletOperationPath(String dailyWalletOperationPath) {
        this.dailyWalletOperationPath = dailyWalletOperationPath;
    }

    public String getSshStaticLobbyHost() {
        return sshStaticLobbyHost;
    }

    public synchronized void setSshStaticLobbyHost(String sshStaticLobbyHost) {
        this.sshStaticLobbyHost = sshStaticLobbyHost;
    }

    public int getSshStaticLobbyPort() {
        return sshStaticLobbyPort;
    }

    public synchronized void setSshStaticLobbyPort(int sshStaticLobbyPort) {
        this.sshStaticLobbyPort = sshStaticLobbyPort;
    }

    public String getSshStaticLobbyUser() {
        return sshStaticLobbyUser;
    }

    public synchronized void setSshStaticLobbyUser(String sshStaticLobbyUser) {
        this.sshStaticLobbyUser = sshStaticLobbyUser;
    }

    public String getSshStaticLobbyPass() {
        return sshStaticLobbyPass;
    }

    public synchronized void setSshStaticLobbyPass(String sshStaticLobbyPass) {
        this.sshStaticLobbyPass = sshStaticLobbyPass;
    }

    public int getCommonExecutorServiceThreadPoolSize() {
        Integer poolSize = PropertyUtils.getIntProperty(properties, KEY_COMMON_EXECUTOR_SERVICE_POOL_SIZE);
        return poolSize != null ? poolSize : DEFAULT_COMMON_EXECUTOR_SERVICE_POOL_SIZE;
    }

    public Set<String> getSshJackpotsUploadHosts() {
        String hosts = properties.get(KEY_SSH_JACKPOTS_UPLOAD_HOSTS);
        if (isTrimmedEmpty(hosts)) {
            return Collections.emptySet();
        }
        return Sets.newHashSet(Splitter.on(SSH_JACKPOTS_UPLOAD_HOSTS_DELIMITER).split(hosts));
    }

    public int getSshJackpotsUploadPort() {
        Integer uploadPort = PropertyUtils.getIntProperty(properties, KEY_SSH_JACKPOTS_UPLOAD_PORT);
        if (uploadPort == null) {
            uploadPort = DEFAULT_SSH_JACKPOTS_UPLOAD_PORT;
        }
        return uploadPort;
    }


    public String getSshStatisticUpdaterUser() {
        return properties.get(KEY_SSH_STATISTIC_UPDATER_USER);
    }

    public String getSshStatisticUpdaterPass() {
        return properties.get(KEY_SSH_STATISTIC_UPDATER_PASS);
    }

    public String getDomain() {
        return domain;
    }

    public synchronized void setDomain(String domain) {
        this.domain = domain;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public synchronized void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public synchronized void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    public synchronized void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    public long getServerSessionTimeout() {
        return serverSessionTimeout;
    }

    public synchronized void setServerSessionTimeout(long serverSessionTimeout) {
        this.serverSessionTimeout = serverSessionTimeout;
    }

    public CasinoSystemType getCasinoSystemType() {
        return casinoSystemType;
    }

    public synchronized void setCasinoSystemType(CasinoSystemType casinoSystemType) {
        this.casinoSystemType = casinoSystemType;
    }

    public boolean isTrustAllSslForHttpClientConnections() {
        return trustAllSslForHttpClientConnections;
    }

    public synchronized void setTrustAllSslForHttpClientConnections(boolean trustAllSslForHttpClientConnections) {
        this.trustAllSslForHttpClientConnections = trustAllSslForHttpClientConnections;
    }

    public long getSessionTrackerMaxSleepTime() {
        return sessionTrackerMaxSleepTime;
    }

    public synchronized void setSessionTrackerMaxSleepTime(long sessionTrackerMaxSleepTime) {
        this.sessionTrackerMaxSleepTime = sessionTrackerMaxSleepTime;
    }

    public boolean isUseStaticVersioning() {
        return useStaticVersioning;
    }

    public synchronized void setUseStaticVersioning(boolean useStaticVersioning) {
        this.useStaticVersioning = useStaticVersioning;
    }

    public boolean isSendBigWinMail() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_SEND_BIG_WIN);
    }

    public long getMinToSendBigWin() {
        Long value = PropertyUtils.getLongProperty(properties, KEY_MIN_TO_SEND_BIG_WIN);
        return value == null ? 0 : value;
    }

    public String getMailAlertList() {
        return PropertyUtils.getStringProperty(properties, KEY_MAIL_LIST);
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public Set<MountMonitoringEntry> getDiskFreeSpaceMonitoringSettings() {
        String monitoringSettingsAsString = PropertyUtils
                .getStringProperty(properties, KEY_FREE_SPACE_MONITORING_SETTINGS);
        Set<MountMonitoringEntry> result = new HashSet<>();
        if (isNotBlank(monitoringSettingsAsString)) {
            Map<String, String> mountPointsWithThresholds = Splitter.on(";").withKeyValueSeparator("=")
                    .split(monitoringSettingsAsString);
            for (Map.Entry<String, String> mountPointAndThreshold : mountPointsWithThresholds.entrySet()) {
                String mountPath = mountPointAndThreshold.getKey();
                if (isBlank(mountPath)) {
                    LOG.warn("Invalid mount path: {}", mountPointAndThreshold);
                    continue;
                }

                String threshold = mountPointAndThreshold.getValue();
                FreeSpaceThresholdType thresholdType = threshold.contains("%")
                        ? FreeSpaceThresholdType.PERCENTAGE
                        : FreeSpaceThresholdType.MEGABYTES;
                String pureThresholdAmountAsString = threshold.replace("%", "");

                try {
                    long thresholdAmount = Long.parseLong(pureThresholdAmountAsString);
                    if (thresholdType == FreeSpaceThresholdType.PERCENTAGE) {
                        checkArgument(thresholdAmount >= 0 && thresholdAmount < 100,
                                "invalid percentage: %s", thresholdAmount);
                    }
                    result.add(new MountMonitoringEntry(mountPath, thresholdType, thresholdAmount));
                } catch (Exception e) {
                    LOG.error("Invalid threshold. mountPath: {}, threshold: {}", mountPath, threshold, e);
                }
            }
        }
        return result;
    }

    public synchronized void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public List<String> getTrustedIp() {
        return trustedIp;
    }

    public boolean isIpTrusted(String ip) {
        if (CollectionUtils.isEmpty(trustedIp) || trustedSubnetList == null) {
            LOG.debug("trustedIp list is empty");
            return true;
        }
        return trustedSubnetList.stream()
                .map(SubnetUtils::getInfo)
                .anyMatch(info -> info.isInRange(ip));
    }

    public synchronized void setTrustedIp(@Nonnull List<String> trustedIp) {
        if (!CollectionUtils.isEmpty(trustedIp)) {
            this.trustedIp = trustedIp;
            initTrustedSubnets();
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    public synchronized void setProperty(String key, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        if (value == null) {
            this.properties.remove(key);
        } else {
            this.properties.put(key, value);
        }
    }

    public synchronized void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean isEnabledCountryCheck() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_ENABLED_COUNTRY_CHECK);
    }

    public boolean isShowMessageInTestMode() {
        return PropertyUtils.getBooleanProperty(properties, KEY_SHOW_MESSAGE_IN_TEST_MODE);
    }

    public long getFavoriteGamesUpdateInterval() {
        Long updateInterval;
        try {
            updateInterval = PropertyUtils.getLongProperty(properties, KEY_FAVORITE_GAMES_UPDATE_INTERVAL);
            if (updateInterval == null) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ignored) {
            updateInterval = DEFAULT_FAVORITE_GAMES_UPDATE_INTERVAL;
        }
        return updateInterval;
    }

    public String getRulesPath() {
        return PropertyUtils.getStringProperty(properties, KEY_RULES_PATH);
    }

    public String getJava8ProxyUrl() {
        return PropertyUtils.getStringProperty(properties, KEY_JAVA8_PROXY_URL);
    }

    public boolean isEmulationFilterEnabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_EMULATION_FILTER_ENABLED);
    }

    public long getBucketCloseTimeout() {
        Long value = PropertyUtils.getLongProperty(properties, KEY_BUCKET_CLOSE_TIMEOUT);
        return value != null ? value : DEFAULT_BUCKET_CLOSE_TIMEOUT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GameServerConfigTemplate");
        sb.append("[systemId=").append(systemId);
        sb.append(", clusterId=").append(getClusterId());
        sb.append(", host='").append(host).append('\'');
        sb.append(", gsDomain='").append(gsDomain).append('\'');
        sb.append(", delayExecutorTimeout=").append(delayExecutorTimeout);
        sb.append(", enableMassBonusCreator=").append(enableMassBonusCreator);
        sb.append(", dropFreeBalance=").append(dropFreeBalance);
        sb.append(", lastHandExpirationServer=").append(lastHandExpirationServer);
        sb.append(", stressTestMode=").append(stressTestMode);
        sb.append(", closeGameProcessorClassName='").append(closeGameProcessorClassName).append('\'');
        sb.append(", lastHandExpirationPeriod=").append(lastHandExpirationPeriod);
        sb.append(", lastHandExpirationCheckPeriod=").append(lastHandExpirationCheckPeriod);
        sb.append(", freeBalance=").append(freeBalance);
        sb.append(", jackpotBaseBankStartValue=").append(jackpotBaseBankStartValue);
        sb.append(", gameTotalsUpdaterInterval=").append(gameTotalsUpdaterInterval);
        sb.append(", jackpotUpdaterInterval=").append(jackpotUpdaterInterval);
        sb.append(", maxLoadValue=").append(maxLoadValue);
        sb.append(", logoutTrackerSleepTimeout=").append(logoutTrackerSleepTimeout);
        sb.append(", logoutTrackerThreadPoolSize=").append(logoutTrackerThreadPoolSize);
        sb.append(", walletTrackerSleepTimeout=").append(walletTrackerSleepTimeout);
        sb.append(", frbonusWinTrackerSleepTimeout=").append(frbonusWinTrackerSleepTimeout);
        sb.append(", walletTrackerThreadPoolSize=").append(walletTrackerThreadPoolSize);
        sb.append(", frbonusWinTrackerThreadPoolSize=").append(frbonusWinTrackerThreadPoolSize);
        sb.append(", bonusTrackerSleepTimeout=").append(bonusTrackerSleepTimeout);
        sb.append(", bonusTrackerThreadPoolSize=").append(bonusTrackerThreadPoolSize);
        sb.append(", serverUpdaterUpdateInterval=").append(serverUpdaterUpdateInterval);
        sb.append(", asyncPersistenceThreadPoolSize=").append(asyncPersistenceThreadPoolSize);
        sb.append(", asyncPersistenceTaskTimeout=").append(asyncPersistenceTaskTimeout);
        sb.append(", winnerFeedPath='").append(winnerFeedPath).append('\'');
        sb.append(", winnerFeedUpdateInterval=").append(winnerFeedUpdateInterval);
        sb.append(", actionNameAfterGameClose='").append(actionNameAfterGameClose).append('\'');
        sb.append(", reportsOutputPath='").append(reportsOutputPath).append('\'');
        sb.append(", reportsUploadUrl='").append(reportsUploadUrl).append('\'');
        sb.append(", exportCachePath='").append(exportCachePath).append('\'');
        sb.append(", smtpServer='").append(smtpServer).append('\'');
        sb.append(", fromSupportEmail='").append(fromSupportEmail).append('\'');
        sb.append(", ignoreCloseGameAction=").append(ignoreCloseGameAction);
        sb.append(", useDistributedLocks=").append(useDistributedLocks);
        sb.append(", casinonoTournamentsEnabled=").append(casinonoTournamentsEnabled);
        sb.append(", ntpServer='").append(ntpServer).append('\'');
        sb.append(", maxInnactivityAccountPeriodInHours=").append(maxInnactivityAccountPeriodInHours);
        sb.append(", usePostToSendAlerts=").append(usePostToSendAlerts);
        sb.append(", dailyWalletOperationPath='").append(dailyWalletOperationPath).append('\'');
        sb.append(", sshStaticLobbyHost='").append(sshStaticLobbyHost).append('\'');
        sb.append(", sshStaticLobbyPort=").append(sshStaticLobbyPort);
        sb.append(", sshStaticLobbyUser='").append(sshStaticLobbyUser).append('\'');
        sb.append(", sshStaticLobbyPass='").append(sshStaticLobbyPass).append('\'');
        sb.append(", winnerFeedBanks=").append(winnerFeedBanks);
        sb.append(", domain='").append(domain).append('\'');
        sb.append(", requestTimeout=").append(requestTimeout);
        sb.append(", httpProxyHost='").append(httpProxyHost).append('\'');
        sb.append(", httpProxyPort=").append(httpProxyPort);
        sb.append(", serverSessionTimeout=").append(serverSessionTimeout);
        sb.append(", casinoSystemType=").append(casinoSystemType);
        sb.append(", trustAllSslForHttpClientConnections=").append(trustAllSslForHttpClientConnections);
        sb.append(", sessionTrackerMaxSleepTime=").append(sessionTrackerMaxSleepTime);
        sb.append(", useStaticVersioning=").append(useStaticVersioning);
        sb.append(", sourceVersion='").append(sourceVersion).append('\'');
        sb.append(", trustedIp=").append(trustedIp);
        sb.append(", enableXssSanitizer=").append(enableXssSanitizer);
        sb.append(", disabledXssSanitizingDomains").append(disabledXssSanitizingDomains);
        sb.append(", disabledXssSanitizingUrls").append(disabledXssSanitizingUrls);
        sb.append(", properties=").append(properties);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public long getId() {
        // hardcode as template can be only one and generic
        return TEMPLATE_ID;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeShort(VERSION);

        output.writeInt(1, true);
        output.writeLong(systemId, true);
        output.writeString(host);
        output.writeString(gsDomain);
        output.writeString("TEMPLATE");
        output.writeLong(delayExecutorTimeout, true);
        output.writeBoolean(enableMassBonusCreator);
        output.writeBoolean(dropFreeBalance);
        output.writeBoolean(lastHandExpirationServer);
        output.writeBoolean(stressTestMode);
        output.writeString(closeGameProcessorClassName);
        output.writeLong(lastHandExpirationPeriod, true);
        output.writeLong(lastHandExpirationCheckPeriod, true);
        output.writeLong(freeBalance, true);
        output.writeLong(jackpotBaseBankStartValue, true);
        output.writeLong(gameTotalsUpdaterInterval, true);
        output.writeLong(jackpotUpdaterInterval, true);
        output.writeInt(maxLoadValue, true);
        output.writeLong(logoutTrackerSleepTimeout, true);
        output.writeInt(logoutTrackerThreadPoolSize, true);
        output.writeLong(walletTrackerSleepTimeout, true);
        output.writeLong(frbonusWinTrackerSleepTimeout, true);
        output.writeInt(walletTrackerThreadPoolSize, true);
        output.writeInt(frbonusWinTrackerThreadPoolSize, true);
        output.writeLong(bonusTrackerSleepTimeout, true);
        output.writeInt(bonusTrackerThreadPoolSize, true);
        output.writeLong(serverUpdaterUpdateInterval, true);
        output.writeInt(asyncPersistenceThreadPoolSize, true);
        output.writeLong(asyncPersistenceTaskTimeout, true);
        output.writeString(winnerFeedPath);
        output.writeLong(winnerFeedUpdateInterval, true);
        output.writeString(actionNameAfterGameClose);
        output.writeString(reportsOutputPath);
        output.writeString(reportsUploadUrl);
        output.writeString(exportCachePath);
        output.writeString(smtpServer);
        output.writeString(fromSupportEmail);
        output.writeBoolean(ignoreCloseGameAction);
        output.writeBoolean(useDistributedLocks);
        output.writeBoolean(casinonoTournamentsEnabled);
        output.writeString(ntpServer);
        output.writeLong(maxInnactivityAccountPeriodInHours, true);
        output.writeBoolean(usePostToSendAlerts);
        output.writeString(dailyWalletOperationPath);
        output.writeString(sshStaticLobbyHost);
        output.writeInt(sshStaticLobbyPort, true);
        output.writeString(sshStaticLobbyUser);
        output.writeString(sshStaticLobbyPass);

        //from ServerConfiguration
        kryo.writeObjectOrNull(output, winnerFeedBanks, ArrayList.class);
        output.writeString(domain);
        output.writeLong(requestTimeout, true);
        output.writeString(httpProxyHost);
        output.writeInt(httpProxyPort, true);
        output.writeInt(DEFAULT_THRIFT_PORT, true);
        output.writeString("unknown");
        output.writeLong(serverSessionTimeout, true);
        kryo.writeObject(output, casinoSystemType);
        output.writeBoolean(trustAllSslForHttpClientConnections);
        output.writeLong(sessionTrackerMaxSleepTime, true);

        output.writeBoolean(useStaticVersioning);
        output.writeString(sourceVersion);

        kryo.writeObjectOrNull(output, trustedIp, ArrayList.class);

        kryo.writeObjectOrNull(output, properties, HashMap.class);

        output.writeBoolean(enableXssSanitizer);
        output.writeString(disabledXssSanitizingDomains);
        output.writeString(disabledXssSanitizingUrls);

        output.writeInt(DEFAULT_CM_THRIFT_PORT, true);
        output.writeString("unknown");
    }

    @Override
    public void read(Kryo kryo, Input input) {
        short version = input.readShort();

        input.readInt(true); // serverId
        systemId = input.readLong(true);
        host = input.readString();
        gsDomain = input.readString();
        input.readString(); // server name
        delayExecutorTimeout = input.readLong(true);
        enableMassBonusCreator = input.readBoolean();
        dropFreeBalance = input.readBoolean();
        lastHandExpirationServer = input.readBoolean();
        stressTestMode = input.readBoolean();
        closeGameProcessorClassName = input.readString();
        lastHandExpirationPeriod = input.readLong(true);
        lastHandExpirationCheckPeriod = input.readLong(true);
        freeBalance = input.readLong(true);
        jackpotBaseBankStartValue = input.readLong(true);
        gameTotalsUpdaterInterval = input.readLong(true);
        jackpotUpdaterInterval = input.readLong(true);
        maxLoadValue = input.readInt(true);
        logoutTrackerSleepTimeout = input.readLong(true);
        logoutTrackerThreadPoolSize = input.readInt(true);
        walletTrackerSleepTimeout = input.readLong(true);
        frbonusWinTrackerSleepTimeout = input.readLong(true);
        walletTrackerThreadPoolSize = input.readInt(true);
        frbonusWinTrackerThreadPoolSize = input.readInt(true);
        bonusTrackerSleepTimeout = input.readLong(true);
        bonusTrackerThreadPoolSize = input.readInt(true);
        serverUpdaterUpdateInterval = input.readLong(true);
        asyncPersistenceThreadPoolSize = input.readInt(true);
        asyncPersistenceTaskTimeout = input.readLong(true);
        winnerFeedPath = input.readString();
        winnerFeedUpdateInterval = input.readLong(true);
        actionNameAfterGameClose = input.readString();
        reportsOutputPath = input.readString();
        reportsUploadUrl = input.readString();
        exportCachePath = input.readString();
        smtpServer = input.readString();
        fromSupportEmail = input.readString();
        ignoreCloseGameAction = input.readBoolean();
        useDistributedLocks = input.readBoolean();
        casinonoTournamentsEnabled = input.readBoolean();
        ntpServer = input.readString();
        maxInnactivityAccountPeriodInHours = input.readLong(true);
        usePostToSendAlerts = input.readBoolean();
        dailyWalletOperationPath = input.readString();
        sshStaticLobbyHost = input.readString();
        sshStaticLobbyPort = input.readInt(true);
        sshStaticLobbyUser = input.readString();
        sshStaticLobbyPass = input.readString();

        //from ServerConfiguration
        winnerFeedBanks = kryo.readObjectOrNull(input, ArrayList.class);
        domain = input.readString();
        requestTimeout = input.readLong(true);
        httpProxyHost = input.readString();
        httpProxyPort = input.readInt(true);
        input.readInt(true); // thriftPort
        input.readString(); // thriftHost
        serverSessionTimeout = input.readLong(true);
        casinoSystemType = kryo.readObject(input, CasinoSystemType.class);
        trustAllSslForHttpClientConnections = input.readBoolean();
        sessionTrackerMaxSleepTime = input.readLong(true);

        useStaticVersioning = input.readBoolean();
        sourceVersion = input.readString();

        trustedIp = kryo.readObjectOrNull(input, ArrayList.class);
        initTrustedSubnets();

        properties = kryo.readObjectOrNull(input, HashMap.class);

        if (version > 0) {
            enableXssSanitizer = input.readBoolean();
            disabledXssSanitizingDomains = input.readString();
            disabledXssSanitizingUrls = input.readString();
        }
        if (version > 1) {
            input.readInt(true); // thriftCMPort
            input.readString(); // thriftCMHost
        }
    }

    private void initTrustedSubnets() {
        trustedSubnetList = trustedIp.stream()
                .filter(Objects::nonNull)
                .map(parseSubnetString())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Function<String, SubnetUtils> parseSubnetString() {
        return ip -> {
            try {
                return new SubnetUtils(ip);
            } catch (IllegalArgumentException e) {
                LOG.debug("SubnetUtils constructor error", e);
                return null;
            }
        };
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        GameServerConfigTemplate from = (GameServerConfigTemplate) entry;
        systemId = from.systemId;
        host = from.host;
        gsDomain = from.gsDomain;
        delayExecutorTimeout = from.delayExecutorTimeout;
        enableMassBonusCreator = from.enableMassBonusCreator;
        dropFreeBalance = from.dropFreeBalance;
        lastHandExpirationServer = from.lastHandExpirationServer;
        stressTestMode = from.stressTestMode;
        closeGameProcessorClassName = from.closeGameProcessorClassName;
        lastHandExpirationPeriod = from.lastHandExpirationPeriod;
        lastHandExpirationCheckPeriod = from.lastHandExpirationCheckPeriod;
        freeBalance = from.freeBalance;
        jackpotBaseBankStartValue = from.jackpotBaseBankStartValue;
        gameTotalsUpdaterInterval = from.gameTotalsUpdaterInterval;
        jackpotUpdaterInterval = from.jackpotUpdaterInterval;
        maxLoadValue = from.maxLoadValue;
        logoutTrackerSleepTimeout = from.logoutTrackerSleepTimeout;
        logoutTrackerThreadPoolSize = from.logoutTrackerThreadPoolSize;
        walletTrackerSleepTimeout = from.walletTrackerSleepTimeout;
        frbonusWinTrackerSleepTimeout = from.frbonusWinTrackerSleepTimeout;
        walletTrackerThreadPoolSize = from.walletTrackerThreadPoolSize;
        frbonusWinTrackerThreadPoolSize = from.frbonusWinTrackerThreadPoolSize;
        bonusTrackerSleepTimeout = from.bonusTrackerSleepTimeout;
        bonusTrackerThreadPoolSize = from.bonusTrackerThreadPoolSize;
        serverUpdaterUpdateInterval = from.serverUpdaterUpdateInterval;
        asyncPersistenceThreadPoolSize = from.asyncPersistenceThreadPoolSize;
        asyncPersistenceTaskTimeout = from.asyncPersistenceTaskTimeout;
        winnerFeedPath = from.winnerFeedPath;
        winnerFeedUpdateInterval = from.winnerFeedUpdateInterval;
        actionNameAfterGameClose = from.actionNameAfterGameClose;
        reportsOutputPath = from.reportsOutputPath;
        reportsUploadUrl = from.reportsUploadUrl;
        exportCachePath = from.exportCachePath;
        smtpServer = from.smtpServer;
        fromSupportEmail = from.fromSupportEmail;
        ignoreCloseGameAction = from.ignoreCloseGameAction;
        useDistributedLocks = from.useDistributedLocks;
        casinonoTournamentsEnabled = from.casinonoTournamentsEnabled;
        ntpServer = from.ntpServer;
        maxInnactivityAccountPeriodInHours = from.maxInnactivityAccountPeriodInHours;
        usePostToSendAlerts = from.usePostToSendAlerts;
        dailyWalletOperationPath = from.dailyWalletOperationPath;
        sshStaticLobbyHost = from.sshStaticLobbyHost;
        sshStaticLobbyPort = from.sshStaticLobbyPort;
        sshStaticLobbyUser = from.sshStaticLobbyUser;
        sshStaticLobbyPass = from.sshStaticLobbyPass;

        //from ServerConfiguration
        winnerFeedBanks = from.winnerFeedBanks == null ? null : new ArrayList<>(from.winnerFeedBanks);
        domain = from.domain;
        requestTimeout = from.requestTimeout;
        httpProxyHost = from.httpProxyHost;
        httpProxyPort = from.httpProxyPort;
        serverSessionTimeout = from.serverSessionTimeout;
        casinoSystemType = from.casinoSystemType;
        trustAllSslForHttpClientConnections = from.trustAllSslForHttpClientConnections;
        sessionTrackerMaxSleepTime = from.sessionTrackerMaxSleepTime;

        useStaticVersioning = from.useStaticVersioning;
        sourceVersion = from.sourceVersion;

        trustedIp = from.trustedIp == null ? null : new ArrayList<>(from.trustedIp);
        initTrustedSubnets();
        properties = new HashMap<>(from.properties);

        enableXssSanitizer = from.enableXssSanitizer;
        disabledXssSanitizingDomains = from.disabledXssSanitizingDomains;
        disabledXssSanitizingUrls = from.disabledXssSanitizingUrls;
    }

    public int getBaseGameCacheMaxSize() {
        int maxSize;

        String maxSizeStr = getProperty(KEY_BASE_GAME_CACHE_MAX_SIZE);
        try {
            maxSize = Integer.valueOf(maxSizeStr);
        } catch (NumberFormatException e) {
            maxSize = DEFAULT_BASE_GAME_CACHE_MAX_SIZE;
        }
        return maxSize;
    }

    public boolean isHttpClientStatisticsEnabled() {
        return PropertyUtils.getBooleanProperty(properties, IS_HTTP_CLIENT_STATISTICS_ENABLED);
    }

    public boolean isTestSystem() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_TEST_SYSTEM);
    }

    public int getOnlineConcurrentMailNotificationLimit() {
        Integer limit = PropertyUtils.getIntProperty(properties, KEY_ONLINE_CONCURRENT_MAIL_NOTIFICATION_LIMIT);
        return limit == null ? DEFAULT_ONLINE_CONCURRENT_NOTIFICATION_LIMIT : limit;
    }

    public long getOnlineConcurrentMailNotificationDelay() {
        Long delay = PropertyUtils.getLongProperty(properties, KEY_ONLINE_CONCURRENT_MAIL_NOTIFICATION_DELAY);
        return delay == null ? DEFAULT_ONLINE_CONCURRENT_NOTIFICATION_DELAY : delay;
    }

    public String getNotificationMail() {
        return PropertyUtils.getStringProperty(properties, KEY_NOTIFICATION_MAIL);
    }

    public String getDocToolsDefaultRemoteURL() {
        return PropertyUtils.getStringProperty(properties, KEY_DOC_TOOLS_DEFAULT_REMOTE_URL);
    }

    public boolean isSSLCertificatesCheckingEnabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ENABLE_SSL_CERTIFICATES_CHECKING);
    }

    public boolean isDemoCluster() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_DEMO_CLUSTER);
    }

    public String getDemoClusterDomain() {
        return PropertyUtils.getStringProperty(properties, KEY_DEMO_CLUSTER_DOMAIN);
    }

    public String getPromoFeedsRootPath() {
        String promoFeedsRootPath = PropertyUtils.getStringProperty(properties, KEY_PROMO_FEEDS_ROOT_PATH);
        return promoFeedsRootPath != null ? promoFeedsRootPath : PROMO_FEEDS_ROOT_DEFAULT_PATH;
    }

    public long getRoundWinsQueuePersistenceInterval() {
        Long interval = PropertyUtils.getLongProperty(properties, KEY_ROUND_WINS_QUEUE_PERSISTENCE_INTERVAL);
        return interval == null ? DEFAULT_ROUND_WINS_QUEUE_PERSISTENCE_INTERVAL : interval;
    }

    public boolean isTicketedDrawProcessorEnabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_TICKETED_DRAW_PROCESSOR_ENABLED);
    }

    public long getLeaderboardTrackerInterval() {
        Long interval = PropertyUtils.getLongProperty(properties, KEY_LEADERBOARD_TRACKER_INTERVAL);
        return interval == null ? DEFAULT_LEADERBOARD_TRACKER_INTERVAL : interval;
    }

    public long getLeaderboardTrackerExpireTime() {
        Long expireTime = PropertyUtils.getLongProperty(properties, KEY_LEADERBOARD_TRACKER_EXPIRE_TIME);
        return expireTime == null ? DEFAULT_LEADERBOARD_TRACKER_EXPIRE_TIME : expireTime;
    }

    public boolean isEnableXssSanitizer() {
        return enableXssSanitizer;
    }

    public synchronized void setEnableXssSanitizer(boolean enableXssSanitizer) {
        this.enableXssSanitizer = enableXssSanitizer;
    }

    public String getDisabledXssSanitizingDomains() {
        return disabledXssSanitizingDomains;
    }

    public synchronized void setDisabledXssSanitizingDomains(String disabledXssSanitizingDomains) {
        this.disabledXssSanitizingDomains = disabledXssSanitizingDomains;
    }

    public String getDisabledXssSanitizingUrls() {
        return disabledXssSanitizingUrls;
    }

    public synchronized void setDisabledXssSanitizingUrls(String disabledXssSanitizingUrls) {
        this.disabledXssSanitizingUrls = disabledXssSanitizingUrls;
    }

    public boolean discontinuesSameTypeErrorsDiagnosticEnabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_DISCONTINUES_SAME_TYPE_ERRORS_DIAGNOSTIC_ENABLED);
    }

    public boolean isNeedDebugSerialize() {
        return PropertyUtils.getBooleanProperty(properties, KEY_IS_NEED_DEBUG_SERIALIZE);
    }

    public String getEmailHashsumFile() {
        return PropertyUtils.getStringProperty(properties, KEY_EMAIL_HASHSUM_FILE_SUPPORT);
    }

    public Map<Long, String> getHashSumFileSettings() {
        String settings = PropertyUtils.getStringProperty(properties, KEY_HASHSUM_FILE_SETTINGS);
        if (settings == null) {
            return Collections.emptyMap();
        }

        Map<Long, String> hashSumSettings = new HashMap<>();
        for (String hashStr : settings.split(";")) {
            String[] subcasinoIdAndFileName = hashStr.split("=");
            if (subcasinoIdAndFileName.length != 2) {
                return Collections.emptyMap();
            }
            long subcasinoId;
            try {
                subcasinoId = Long.parseLong(subcasinoIdAndFileName[0]);
            } catch (NumberFormatException e) {
                return Collections.emptyMap();
            }
            hashSumSettings.put(subcasinoId, subcasinoIdAndFileName[1]);
        }
        return hashSumSettings;
    }

    //key=clusterId, value=endPointURL (http://domain:port)
    public Map<Long, String> getCrossClusterApiEndpoints() {
        String s = PropertyUtils.getStringProperty(properties, KEY_CROSS_CLUSTER_API_ENDPOINTS);
        if (StringUtils.isTrimmedEmpty(s)) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new HashMap<>();
        try {
            Map<String, String> stringMap = CollectionUtils.stringToMap(s);
            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                result.put(Long.parseLong(entry.getKey()), entry.getValue());
            }
        } catch (Exception e) {
            LOG.error("Cannot parse: CROSS_CLUSTER_API_ENDPOINTS='{}'", s, e);
        }
        return result;
    }

    public Long getMaxCallsCWApiTestPerDay() {
        Long maxCalls = PropertyUtils.getLongProperty(properties, KEY_MAX_CW_API_TEST_CALLS_PER_DAY);
        if (maxCalls == null) {
            maxCalls = 100L;
        }
        return maxCalls;
    }

    public int getClusterId() {
        Integer clusterId = PropertyUtils.getIntProperty(properties, KEY_CLUSTER_ID);
        return clusterId == null ? 0 : clusterId;
    }

    public long getTournamentClustersCount() {
        Long tournamentClustersCount = PropertyUtils.getLongProperty(properties, KEY_TOURNAMENT_CLUSTERS_COUNT);
        if (tournamentClustersCount == null) {
            tournamentClustersCount = 6L;
        }
        return tournamentClustersCount;
    }

    public boolean isBigStorageCassandraClusterEnabled() {
        return PropertyUtils.getBooleanProperty(properties, KEY_ENABLE_BIG_STORAGE_CASSANDRA_CLUSTER);
    }

    public String getSshSharedHost() {
        return PropertyUtils.getStringProperty(properties, KEY_SSH_SHARED_HOST);
    }

    public Integer getSshSharedPort() {
        return PropertyUtils.getIntProperty(properties, KEY_SSH_SHARED_PORT);
    }

    public String getSshSharedUser() {
        return PropertyUtils.getStringProperty(properties, KEY_SSH_SHARED_USER);
    }

    public String getSshSharedPass() {
        return PropertyUtils.getStringProperty(properties, KEY_SSH_SHARED_PASSWORD);
    }

    public String getGameCertificateDirectory() {
        return PropertyUtils.getStringProperty(properties, KEY_GAME_CERTIFICATE_DIRECTORY);
    }

    public Integer getPromoMotivationMessagePeriod() {
        return PropertyUtils.getIntProperty(properties, KEY_PROMO_MOTIVATION_MESSAGE_PERIOD);
    }

    public boolean isDisableJackpotHistoryUpdater() {
        return PropertyUtils.getBooleanProperty(properties, KEY_DISABLE_JP_HISTORY_UPDATER);
    }

    public boolean isJackpotTickerDisabled() {
        return !PropertyUtils.getBooleanProperty(properties, KEY_JACKPOT_TICKER_ENABLED);
    }

    public String getCmHost() {
        return PropertyUtils.getStringProperty(properties, KEY_CM_HOST);
    }

    private <T extends Enum> T getEnumProperty(String key, T defaultValue) {
        try {
            String stringProperty = PropertyUtils.getStringProperty(properties, key);
            if (isTrimmedEmpty(stringProperty)) {
                return defaultValue;
            }

            Enum anEnum = Enum.valueOf(defaultValue.getClass(), stringProperty);
            if (anEnum != null) {
                return (T) anEnum;
            } else {
                return defaultValue;
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
