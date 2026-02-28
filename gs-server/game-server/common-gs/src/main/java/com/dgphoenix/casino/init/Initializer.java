package com.abs.casino.init;

import com.abs.casino.cache.PingSessionCache;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.DistributedLockManager;
import com.abs.casino.cassandra.IRemoteUnlocker;
import com.abs.casino.cassandra.persist.CassandraExtendedAccountInfoPersister;
import com.dgphoenix.casino.cassandra.persist.*;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.abs.casino.common.cache.data.server.ServerInfo;
import com.abs.casino.common.config.UtilsApplicationContextHelper;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.abs.casino.common.exception.FatalException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.abs.casino.common.util.SynchroTimeProvider;
import com.abs.casino.common.util.hardware.HardwareConfigurationManager;
import com.abs.casino.common.util.hardware.data.HardwareInfo;
import com.abs.casino.common.util.logkit.LoggingUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.web.IdValueBean;
import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.abs.casino.common.web.statistics.StatisticsBuilder;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.dblink.DBLinkCache;
import com.abs.casino.gs.managers.payment.bonus.tracker.ExpiredBonusTracker;
import com.abs.casino.gs.managers.payment.bonus.tracker.ExpiredFRBonusTracker;
import com.abs.casino.gs.managers.payment.wallet.WalletPersister;
import com.abs.casino.init.ApplicationScopeNames;
import com.abs.casino.init.GsInitThread;
import com.abs.casino.statistics.http.HttpClientCallbackHandler;
import com.abs.casino.system.MetricsManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.dgphoenix.casino.common.configuration.messages.MessageManager.GAME_NAME_PREFIX;

public class Initializer implements ServletContextListener {

    private GsInitThread initializer;
    public static final int PROJECT_START_YEAR = 2014;
    private static final Logger LOG = LogManager.getLogger(Initializer.class);
    private GameServerConfiguration config;
    private NtpTimeProvider ntpTimeProvider;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        setUncaughtExceptionHandler();
        LoggingUtils.initializeGameLog();
        LOG.info("Initializing resources");
        config = ApplicationContextHelper.getApplicationContext()
                .getBean("gameServerConfiguration", GameServerConfiguration.class);
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        int serverId = config.getServerId();
        try {
            IRemoteUnlocker remoteUnlocker = ApplicationContextHelper.getApplicationContext()
                    .getBean("remoteUnlocker", IRemoteUnlocker.class);
            DistributedLockManager distributedLockManager = persistenceManager.getPersister(DistributedLockManager.class);
            distributedLockManager.setLoadBalancer(LoadBalancerCache.getInstance());
            distributedLockManager.setRemoteUnlocker(remoteUnlocker);
            distributedLockManager.setServerId(serverId);

            CassandraCommonGameWalletPersister commonGameWalletPersister =
                    persistenceManager.getPersister(CassandraCommonGameWalletPersister.class);
            WalletPersister.getInstance().init(commonGameWalletPersister);

            LOG.info("Starting server ID:" + config.getServerId());
            LOG.info("Starting server Host: " + config.getHost());


            HttpClientConnection.setProperties(config.getRequestTimeout(), config.getHttpProxyHost(),
                    config.getHttpProxyPort(), config.isTrustAllSslForHttpClientConnections());
            {
                Collection<BankInfo> banks = BankInfoCache.getInstance().getAllObjects().values();
                Set<String> trustedHosts = new HashSet<>();
                for (BankInfo bank : banks) {
                    Set<String> hosts = bank.getTrustedHttpsHostsAsSet();
                    if (hosts != null && !hosts.isEmpty()) {
                        trustedHosts.addAll(hosts);
                    }
                }
                if (!trustedHosts.isEmpty()) {
                    HttpClientConnection.setupTrustedHostnameVerifier(trustedHosts);
                }
            }
            String proxyUrl = config.getJava8ProxyUrl();
            HttpClientConnection.setupJava8ProxyUrl(proxyUrl);
            boolean isClientStatisticsEnabled = config.isHttpClientStatisticsEnabled();
            if (isClientStatisticsEnabled) {
                CassandraCallStatisticsPersister callStatisticsPersister =
                        persistenceManager.getPersister(CassandraCallStatisticsPersister.class);
                HttpClientCallbackHandler.getInstance().init(callStatisticsPersister);
            }
            CassandraTrackingInfoPersister trackingInfoPersister = persistenceManager.getPersister(CassandraTrackingInfoPersister.class);
            trackingInfoPersister.setGameServerId(serverId);
            initHardwareInfo(serverId);
            GameServer.getInstance().dsoStarted();
            DBLinkCache.getInstance().registerTDInvalidationListener();
            //GameAdditionalDataCacheInitializer.assertInitialized();
            //Default locale
            ServletContext sc = event.getServletContext();
            sc.setAttribute(org.apache.struts.Globals.LOCALE_KEY, Locale.ENGLISH);
            LOG.info("Initializing resources completed");
        } catch (Exception e) {
            LOG.fatal("Initializer error:", e);
            throw new RuntimeException("Initializer failed", e);
        }
        //set offline status may be need if global cluster crash occured
        ServerInfo serverInfo = LoadBalancerCache.getInstance().getServerInfoById(serverId);
        if (serverInfo != null && serverInfo.isServerOnline()) {
            LOG.warn("ServerInfo status isOnline, global crash ?");
            //serverInfo.setOnline(false);
        }
        event.getServletContext().setAttribute(ApplicationScopeNames.DAYS_IN_MONTH_VALUES, getDaysInMonth());
        event.getServletContext().setAttribute(ApplicationScopeNames.YEARS_VALUES, getYears());
        event.getServletContext().setAttribute(ApplicationScopeNames.MONTH_VALUES, getMonth());
        event.getServletContext().setAttribute(ApplicationScopeNames.HOURS_VALUES, getHours());
        event.getServletContext().setAttribute(ApplicationScopeNames.MINUTES_VALUES, getMinutes());
        event.getServletContext().setAttribute(ApplicationScopeNames.SECONDS_VALUES, getSeconds());
        event.getServletContext().setAttribute(ApplicationScopeNames.GAME_NAMES, getGameNames());
        ntpTimeProvider = UtilsApplicationContextHelper.getApplicationContext()
                .getBean("timeProvider", NtpTimeProvider.class);
        String ntpServerHost = config.getNtpServer();
        try {
            try {
                ntpTimeProvider.start(ntpServerHost);
            } catch (Exception e) {
                LOG.warn("timeProvider: " + ntpServerHost + " variable not found, use 'metadata.google.internal' as we run on GCP");
                ntpServerHost = "metadata.google.internal";
                try {
                    InetAddress.getByName(ntpServerHost);
                } catch (UnknownHostException e2) {
                    LOG.warn(ntpServerHost + " resolve failed, use 'pool.ntp.org' as we run on GCP");
                    ntpServerHost = "pool.ntp.org";
                }
                ntpTimeProvider.start(ntpServerHost);
            }
            LOG.debug("timeProvider: started with, ntpServerHost={}", ntpServerHost);
            SynchroTimeProvider.getInstance().setWrappedProvider(ntpTimeProvider);
        } catch (RuntimeException e) {
            LOG.error("DANGER!!!: Cannot start SynchroTimeProvider, time synchronization disabled: ntpServer={}", config.getNtpServer(),
                    e);
        }
        initializer = new GsInitThread();
        try {
            initializer.startup();
        } catch (FatalException e) {
            LOG.fatal("GsInitThread error", e);
            throw new RuntimeException("GsInitThread error: " + e.getMessage());
        }
        StatisticsManager.getInstance().setId(String.valueOf(GameServer.getInstance().getServerId()));
        registerStatisticsGetter();
        ExpiredFRBonusTracker.getInstance().init();
        ExpiredBonusTracker.getInstance().init();
        CassandraExtendedAccountInfoPersister extendedAccountInfoPersister =
                persistenceManager.getPersister(CassandraExtendedAccountInfoPersister.class);
        ExtendedAccountInfoPersisterInstanceHolder.setPersister(extendedAccountInfoPersister);
        MetricsManager.getInstance().startup();
        PingSessionCache.getInstance().init();

        CassandraBankInfoPersister bankInfoPersister = persistenceManager.getPersister(CassandraBankInfoPersister.class);
        bankInfoPersister.addBankInfoUpdateListener((bankId, bankInfo) -> BankInfoCache.getInstance().invalidateFrbGamesForBank(bankId));
        bankInfoPersister.setNeedDebugSerialize(config.isNeedDebugSerialize());

        LOG.error("\nInitializer: ALL INITIALIZED\n");
    }

    private void setUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultUncaughtExceptionHandler != null) {
            LOG.info("Default uncaught exception handler: {}",
                    defaultUncaughtExceptionHandler.getClass().getCanonicalName());
        }
        Thread.setDefaultUncaughtExceptionHandler(((thread, exception) -> {
            LOG.error("Found uncaught exception, thread: {}", thread, exception);
            if (defaultUncaughtExceptionHandler != null) {
                defaultUncaughtExceptionHandler.uncaughtException(thread, exception);
            }
        }));
    }

    private void initHardwareInfo(int serverId) {
        ServerInfo serverInfo = LoadBalancerCache.getInstance().getServerInfoById(serverId);
        HardwareInfo hardwareInfo = null;
        try {
            //prevent java.lang.UnsatisfiedLinkError: org.hyperic.sigar.Sigar.getCpuListNative()[Lorg/hyperic/sigar/Cpu;
            hardwareInfo = HardwareConfigurationManager.getInstance().getHardwareInfo();
        } catch (Throwable e) {
            LOG.warn("initServerInfo: hardwareInfo is not initialized", e);
        }
        serverInfo.setHardwareInfo(hardwareInfo);
    }

    public void registerStatisticsGetter() {
        StatisticsManager.getInstance().registerStatisticsGetter("GameServer", new IStatisticsGetter() {
            final long startTime = System.currentTimeMillis();

            @Override
            public String getStatistics() {
                int serverId = config.getServerId();
                ServerInfo serverInfo = LoadBalancerCache.getInstance().getServerInfoById(serverId);
                if (serverInfo == null) {
                    return "serverInfo is null";
                }
                return "started=" + new Date(startTime) + ", serverId=" + serverInfo.getServerId() +
                        ", name=" + serverInfo.getLabel() + ", upTime=" + (System.currentTimeMillis() - serverInfo.getUptime());
            }
        });
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOG.debug("Shutdown statistics dump: {}\n", StatisticsBuilder.getInstance().buildRequestStatistics());

        MetricsManager.getInstance().shutdown();
        HttpClientCallbackHandler.getInstance().shutdown();
        ExpiredFRBonusTracker.getInstance().shutdown();
        ExpiredBonusTracker.getInstance().shutdown();
        if (initializer != null) {
            initializer.terminate();
        }
        GameServer.getInstance().getIdGenerator().shutdownBlockAllocators();
        GameServer.getInstance().getIntegerIdGenerator().shutdownBlockAllocators();
        HttpClientConnection.shutdown();
        PingSessionCache.getInstance().shutdown();
        ntpTimeProvider.shutdown();

        LOG.error("\nInitializer: ALL DESTROYED\n");
    }

    private List<IdValueBean> getDaysInMonth() {
        List<IdValueBean> result = new ArrayList<>(31);
        for (long i = 1; i <= 31; i++) {
            result.add(new IdValueBean(i, String.valueOf(i)));
        }
        return result;
    }

    private List<IdValueBean> getYears() {
        List<IdValueBean> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, 2); // Add 2 weeks in case of new year
        int endYear = calendar.get(Calendar.YEAR);
        for (long i = PROJECT_START_YEAR; i <= endYear; i++) {
            result.add(new IdValueBean(i, String.valueOf(i)));
        }
        return result;
    }

    private List<IdValueBean> getMonth() {
        List<IdValueBean> result = new ArrayList<>(12);
        result.add(new IdValueBean(1, "January"));
        result.add(new IdValueBean(2, "February"));
        result.add(new IdValueBean(3, "March"));
        result.add(new IdValueBean(4, "April"));
        result.add(new IdValueBean(5, "May"));
        result.add(new IdValueBean(6, "June"));
        result.add(new IdValueBean(7, "July"));
        result.add(new IdValueBean(8, "August"));
        result.add(new IdValueBean(9, "September"));
        result.add(new IdValueBean(10, "October"));
        result.add(new IdValueBean(11, "November"));
        result.add(new IdValueBean(12, "December"));
        return result;
    }

    private List<IdValueBean> getHours() {
        List<IdValueBean> result = new ArrayList<>(24);
        for (long i = 0; i < TimeUnit.DAYS.toHours(1); i++) {
            result.add(new IdValueBean(i, String.valueOf(i)));
        }
        return result;
    }

    private List<IdValueBean> getMinutes() {
        List<IdValueBean> result = new ArrayList<>(60);
        for (long i = 0; i < TimeUnit.HOURS.toMinutes(1); i++) {
            result.add(new IdValueBean(i, String.valueOf(i)));
        }
        return result;
    }

    private List<IdValueBean> getSeconds() {
        List<IdValueBean> result = new ArrayList<>(60);
        for (long i = 0; i < TimeUnit.MINUTES.toSeconds(1); i++) {
            result.add(new IdValueBean(i, String.valueOf(i)));
        }
        return result;
    }

    public List<IdValueBean> getGameNames() {

        List<IdValueBean> result = new ArrayList<>();
        for (BaseGameInfoTemplate baseGameInfoTemplate : BaseGameInfoTemplateCache.getInstance().getAllObjects().values()) {
            long gameId = baseGameInfoTemplate.getGameId();
            String gameNaturalName = baseGameInfoTemplate.getGameName();
            String gameName = getMessage(GAME_NAME_PREFIX + gameNaturalName);
            result.add(new IdValueBean(gameId, StringUtils.isTrimmedEmpty(gameName) ?
                    gameNaturalName : gameName));

        }
        Collections.sort(result);
        return result;
    }

    protected String getMessage(String key) {
        return MessageManager.getInstance().getApplicationMessage(key);
    }
}
