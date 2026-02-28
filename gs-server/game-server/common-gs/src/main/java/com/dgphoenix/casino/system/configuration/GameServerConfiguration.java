package com.dgphoenix.casino.system.configuration;

import com.abs.casino.GeoIp;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraBlockedCountriesPersister;
import com.dgphoenix.casino.common.cache.ServerConfigsCache;
import com.abs.casino.common.cache.data.server.ServerCoordinatorInfoProvider;
import com.abs.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.config.GameServerConfig;
import com.dgphoenix.casino.common.config.GameServerConfigTemplate;
import com.abs.casino.common.config.MountMonitoringEntry;
import com.abs.casino.common.configuration.CasinoSystemType;
import com.abs.casino.common.configuration.IGameServerConfiguration;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.util.PropertyMessageResources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class GameServerConfiguration implements IGameServerConfiguration {
    private static final String MESSAGE_BUNDLE = "com.dgphoenix.casino.gs.web.messages.ApplicationMessage";
    private static final Logger LOG = LogManager.getLogger(GameServerConfiguration.class);

    private final GameServerConfig config;
    private final CassandraBlockedCountriesPersister blockedCountriesPersister;
    private final GeoIp geoIp;

    public GameServerConfiguration(ServerConfigsCache configsCache, GeoIp geoIp,
                                   CassandraPersistenceManager persistenceManager, ServerCoordinatorInfoProvider serverIdProvider) {
        Integer gameServerId = serverIdProvider.getServerId();
        if (gameServerId == null) {
            LOG.fatal("Couldn't find serverId. Seems ServerIdProvider is not initialized.");
            config = null;
        } else {
            config = configsCache.getServerConfig(gameServerId);
        }
        checkNotNull(config, "Couldn't find GameServerConfig");
        LOG.info("Initialized: {}", config);
        this.geoIp = geoIp;
        blockedCountriesPersister = persistenceManager.getPersister(CassandraBlockedCountriesPersister.class);
    }

    /**
     * @deprecated Should be used only for backward compatibility.
     */
    public static GameServerConfiguration getInstance() {
        return ApplicationContextHelper.getApplicationContext()
                .getBean("gameServerConfiguration", GameServerConfiguration.class);
    }

    public ServerInfo composeServerInfo(long currentTime) {
        int serverId = getServerId();
        String host = getHost();
        int maxLoad = getMaxLoadValue();
        String label = getServerLabel();
        return new ServerInfo(serverId, label, host, maxLoad, currentTime);
    }

    public String getApplicationMessage(String key) {
        return PropertyMessageResources.getMessageResources(MESSAGE_BUNDLE).getMessage(key);
    }

    public String getBrandName() {
        return getApplicationMessage("title.brandName");
    }

    public String getBrandNameLowCase() {
        return getApplicationMessage("title.brandName.lowCase");
    }

    public String getBrandLiveDomainSuffix() {
        return getApplicationMessage("brand.liveDomainSuffix");
    }

    public String getBrandApiRootTagName() {
        return getApplicationMessage("brand.api.rootTagName");
    }

    public String getBrandApiRootShortTagName() {
        return getApplicationMessage("brand.api.rootShortTagName");
    }

    @Override
    public String getStringPropertySilent(String propertyName) {
        return config.getTemplate().getProperty(propertyName);
    }

    public boolean isTrustAllSslForHttpClientConnections() {
        return config.getTemplate().isTrustAllSslForHttpClientConnections();
    }

    @Override
    public CasinoSystemType getCasinoSystemType() {
        return config.getTemplate().getCasinoSystemType();
    }

    @Override
    public String getDomain() {
        return config.getTemplate().getDomain();
    }

    public long getRequestTimeout() {
        return config.getTemplate().getRequestTimeout();
    }

    public long getServerSessionTimeout() {
        return config.getTemplate().getServerSessionTimeout();
    }

    public long getSessionTrackerMaxSleepTime() {
        return config.getTemplate().getSessionTrackerMaxSleepTime();
    }

    public boolean isUseStaticVersioning() {
        return config.getTemplate().isUseStaticVersioning();
    }

    public String getSourceVersion() {
        return config.getTemplate().getSourceVersion();
    }

    public List<String> getTrustedIp() {
        return config.getTemplate().getTrustedIp();
    }

    @Override
    public boolean isIpTrusted(String ip) {
        return config.getTemplate().isIpTrusted(ip);
    }

    @Override
    public boolean isCountryTrusted(String ip) {
        boolean trusted = true;
        if (config.getTemplate().isEnabledCountryCheck()) {
            long now = System.currentTimeMillis();
            try {
                trusted = blockedCountriesPersister.check(ip, geoIp);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            StatisticsManager.getInstance().updateRequestStatistics(
                    "GameServerConfiguration: CheckBlockedCountries", System.currentTimeMillis() - now
            );
        }
        return trusted;
    }

    public String getHttpProxyHost() {
        return config.getTemplate().getHttpProxyHost();
    }

    public int getHttpProxyPort() {
        return config.getTemplate().getHttpProxyPort();
    }

    @Override
    public String getHost() {
        return config.getTemplate().getHost();
    }

    @Override
    public String getGsDomain() {
        return config.getTemplate().getGsDomain();
    }

    @Override
    public long getGameServerId() {
        return getServerId();
    }

    public int getServerId() {
        return config.getServerId();
    }

    @Override
    public long getSystemId() {
        return config.getTemplate().getSystemId();
    }

    public String getExportCachePath() {
        return config.getTemplate().getExportCachePath();
    }

    public String getCloseGameProcessorClassName() {
        return config.getTemplate().getCloseGameProcessorClassName();
    }

    public long getLastHandExpirationPeriod() {
        return config.getTemplate().getLastHandExpirationPeriod();
    }

    public long getLastHandExpirationCheckPeriod() {
        return config.getTemplate().getLastHandExpirationCheckPeriod();
    }

    public long getFreeBalance() {
        return config.getTemplate().getFreeBalance();
    }

    public Integer getFreeBalanceMultiplier() {
        return config.getTemplate().getFreeBalanceMultiplier();
    }

    public long getJackpotBaseBankStartValue() {
        return config.getTemplate().getJackpotBaseBankStartValue();
    }

    public boolean isDropFreeBalance() {
        return config.getTemplate().isDropFreeBalance();
    }

    public long getJackpotUpdaterInterval() {
        return config.getTemplate().getJackpotUpdaterInterval();
    }

    public int getMaxLoadValue() {
        return config.getTemplate().getMaxLoadValue();
    }

    @Override
    public String getServerLabel() {
        return config.getName();
    }

    public long getLogoutTrackerSleepTimeout() {
        return config.getTemplate().getLogoutTrackerSleepTimeout();
    }

    public int getLogoutTrackerThreadPoolSize() {
        return config.getTemplate().getLogoutTrackerThreadPoolSize();
    }

    public long getWalletTrackerSleepTimeout() {
        return config.getTemplate().getWalletTrackerSleepTimeout();
    }

    public long getFrbonusWinTrackerSleepTimeout() {
        return config.getTemplate().getFrbonusWinTrackerSleepTimeout();
    }

    public int getWalletTrackerThreadPoolSize() {
        return config.getTemplate().getWalletTrackerThreadPoolSize();
    }

    public int getFrbonusWinTrackerThreadPoolSize() {
        return config.getTemplate().getFrbonusWinTrackerThreadPoolSize();
    }

    public long getBonusTrackerSleepTimeout() {
        return config.getTemplate().getBonusTrackerSleepTimeout();
    }

    public int getBonusTrackerThreadPoolSize() {
        return config.getTemplate().getBonusTrackerThreadPoolSize();
    }

    public boolean isLastHandExpirationServer() {
        return config.getTemplate().isLastHandExpirationServer();
    }

    public long getServerUpdaterUpdateInterval() {
        return config.getTemplate().getServerUpdaterUpdateInterval();
    }

    public int getAsyncPersistenceThreadPoolSize() {
        return config.getTemplate().getAsyncPersistenceThreadPoolSize();
    }

    public long getAsyncPersistenceTaskTimeout() {
        return config.getTemplate().getAsyncPersistenceTaskTimeout();
    }

    public String getWinnerFeedPath() {
        return config.getTemplate().getWinnerFeedPath();
    }

    public long getWinnerFeedUpdateInterval() {
        return config.getTemplate().getWinnerFeedUpdateInterval();
    }

    public List<Long> getWinnerFeedBanks() {
        return config.getTemplate().getWinnerFeedBanks();
    }

    public String getActionNameAfterGameClose() {
        return config.getTemplate().getActionNameAfterGameClose();
    }

    @Override
    public boolean isStressTestMode() {
        return config.getTemplate().isStressTestMode();
    }

    public String getReportsOutputPath() {
        return config.getTemplate().getReportsOutputPath();
    }

    public String getReportsUploadUrl() {
        return config.getTemplate().getReportsUploadUrl();
    }

    public String getSmtpServer() {
        return config.getTemplate().getSmtpServer();
    }

    public String getFromSupportEmail() {
        return config.getTemplate().getFromSupportEmail();
    }

    public boolean isIgnoreCloseGameAction() {
        return config.getTemplate().isIgnoreCloseGameAction();
    }

    public boolean isUseDistributedLocks() {
        return config.getTemplate().isUseDistributedLocks();
    }

    public String getNtpServer() {
        return config.getTemplate().getNtpServer();
    }

    public long getMaxInnactivityAccountPeriodInHours() {
        return config.getTemplate().getMaxInnactivityAccountPeriodInHours();
    }

    public String getDailyWalletOperationPath() {
        return config.getTemplate().getDailyWalletOperationPath();
    }

    public String getSshStaticLobbyHost() {
        return config.getTemplate().getSshStaticLobbyHost();
    }

    public int getSshStaticLobbyPort() {
        return config.getTemplate().getSshStaticLobbyPort();
    }

    public String getSshStaticLobbyUser() {
        return config.getTemplate().getSshStaticLobbyUser();
    }

    public String getSshStaticLobbyPass() {
        return config.getTemplate().getSshStaticLobbyPass();
    }

    public int getCommonExecutorServiceThreadPoolSize() {
        return config.getTemplate().getCommonExecutorServiceThreadPoolSize();
    }

    public Set<String> getSshJackpotsUploadHosts() {
        return config.getTemplate().getSshJackpotsUploadHosts();
    }

    public int getSshJackpotsUploadPort() {
        return config.getTemplate().getSshJackpotsUploadPort();
    }

    public String getSshStatisticUpdaterUser() {
        return config.getTemplate().getSshStatisticUpdaterUser();
    }

    public String getSshStatisticUpdaterPass() {
        return config.getTemplate().getSshStatisticUpdaterPass();
    }

    public boolean isShowMessageInTestMode() {
        return config.getTemplate().isShowMessageInTestMode();
    }

    public long getFavoriteGamesUpdateInterval() {
        return config.getTemplate().getFavoriteGamesUpdateInterval();
    }

    public int getBaseGameCacheMaxSize() {
        return config.getTemplate().getBaseGameCacheMaxSize();
    }

    public boolean isHttpClientStatisticsEnabled() {
        return config.getTemplate().isHttpClientStatisticsEnabled();
    }

    public Set<MountMonitoringEntry> getDiskFreeSpaceMonitoringSettings() {
        return config.getTemplate().getDiskFreeSpaceMonitoringSettings();
    }

    @Override
    public boolean isTestSystem() {
        return config.getTemplate().isTestSystem();
    }

    public int getOnlineConcurrentMailNotificationLimit() {
        return config.getTemplate().getOnlineConcurrentMailNotificationLimit();
    }

    public long getOnlineConcurrentMailNotificationDelay() {
        return config.getTemplate().getOnlineConcurrentMailNotificationDelay();
    }

    public String getNotificationMail() {
        return config.getTemplate().getNotificationMail();
    }

    public String getDocToolsDefaultRemoteURL() {
        return config.getTemplate().getDocToolsDefaultRemoteURL();
    }

    public String getJava8ProxyUrl() {
        return config.getTemplate().getJava8ProxyUrl();
    }

    public boolean isEmulationFilterEnabled() {
        return config.getTemplate().isEmulationFilterEnabled();
    }

    public String getRulesPath() {
        return config.getTemplate().getRulesPath();
    }

    public String getMaintenancePage() {
        String configValue = getStringPropertySilent(GameServerConfigTemplate.KEY_MAINTENANCE_PAGE);
        if (!StringUtils.isTrimmedEmpty(configValue)) {
            return configValue;
        } else {
            return "/error_pages/game_in_maintenance_mode.jsp";
        }
    }

    public boolean isDemoCluster() {
        return config.getTemplate().isDemoCluster();
    }

    public String getDemoClusterDomain() {
        return config.getTemplate().getDemoClusterDomain();
    }

    public String getPromoFeedsRootPath() {
        return config.getTemplate().getPromoFeedsRootPath();
    }


    public long getRoundWinsQueuePersistenceInterval() {
        return config.getTemplate().getRoundWinsQueuePersistenceInterval();
    }

    public boolean isTicketedDrawProcessorEnabled() {
        return config.getTemplate().isTicketedDrawProcessorEnabled();
    }

    public long getLeaderboardTrackerInterval() {
        return config.getTemplate().getLeaderboardTrackerInterval();
    }

    public long getLeaderboardTrackerExpireTime() {
        return config.getTemplate().getLeaderboardTrackerExpireTime();
    }

    public boolean discontinuesSameTypeErrorsDiagnosticEnabled() {
        return config.getTemplate().discontinuesSameTypeErrorsDiagnosticEnabled();
    }

    public boolean isNeedDebugSerialize() {
        return config.getTemplate().isNeedDebugSerialize();
    }

    public boolean isBigStorageCassandraClusterEnabled() {
        return config.getTemplate().isBigStorageCassandraClusterEnabled();
    }

    public String getSshSharedHost() {
        return config.getTemplate().getSshSharedHost();
    }

    public Integer getSshSharedPort() {
        return config.getTemplate().getSshSharedPort();
    }

    public String getSshSharedUser() {
        return config.getTemplate().getSshSharedUser();
    }

    public String getSshSharedPass() {
        return config.getTemplate().getSshSharedPass();
    }

    public Integer getPromoMotivationMessagePeriod() {
        return config.getTemplate().getPromoMotivationMessagePeriod();
    }

    @Override
    public boolean isLive() {
        throw new IllegalStateException("Use HostConfiguration.isProduction");
    }

    @Override
    public String getClusterName() {
        throw new IllegalStateException("Use HostConfiguration.getClusterName");
    }

    public boolean isEnableXssSanitizer() {
        return config.getTemplate().isEnableXssSanitizer();
    }

    public String getDisabledXssSanitizingDomains() {
        return config.getTemplate().getDisabledXssSanitizingDomains();
    }

    public String getDisableXssSanitizingUrls() {
        return config.getTemplate().getDisabledXssSanitizingUrls();
    }

    public Long getMaxCallsCWApiTestPerDay() {
        return config.getTemplate().getMaxCallsCWApiTestPerDay();
    }

    public String getEmailHashsumFile() {
        return config.getTemplate().getEmailHashsumFile();
    }

    public Map<Long, String> getHashSumFileSettings() {
        return config.getTemplate().getHashSumFileSettings();
    }

    @Override
    public int getClusterId() {
        return config.getTemplate().getClusterId();
    }

    public long getTournamentClustersCount() {
        return config.getTemplate().getTournamentClustersCount();
    }

    public Map<Long, String> getCrossClusterApiEndpoints() {
        return config.getTemplate().getCrossClusterApiEndpoints();
    }

    public String getGameCertificateDirectory() {
        return config.getTemplate().getGameCertificateDirectory();
    }

    public boolean isDisableJackpotHistoryUpdater() {
        return config.getTemplate().isDisableJackpotHistoryUpdater();
    }

    public boolean isJackpotTickerDisabled() {
        return config.getTemplate().isJackpotTickerDisabled();
    }

    public String getCmHost() {
        return config.getTemplate().getCmHost();
    }
    @Override
    public String toString() {
        return "GameServerConfiguration[" +
                "config=" + config +
                ']';
    }
}
