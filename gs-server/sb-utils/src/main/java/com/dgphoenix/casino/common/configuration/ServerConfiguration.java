package com.abs.casino.common.configuration;

import com.abs.casino.common.configuration.resource.FileObserveFactory;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.string.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ANGeL Date: Sep 16, 2008 Time: 5:47:18 PM
 */
public class ServerConfiguration extends Configuration implements ConfigurationConstants {
    private static final Logger LOG = Logger.getLogger(ServerConfiguration.class);
    protected final static ServerConfiguration instance =
            new ServerConfiguration(FileObserveFactory.CASINO_CONFIGURATION_PROPERTIES);
    private final String domain;
    private final long requestTimeout;
    private String httpProxyHost;
    private int httpProxyPort = 8080;
    private final long serverSessionTimeout;
    private CasinoSystemType casinoSystemType = CasinoSystemType.MULTIBANK;
    private boolean trustAllSslForHttpClientConnections = false;
    private boolean useStrongConsistencyCassandraStorage;
    private boolean useCassandraStorageForTransferAccounts;
    private boolean useCassandraStorageForStoreWalletOperations;
    private final long sessionTrackerMaxSleepTime;

    private boolean useStaticVersioning;
    private String sourceVersion;

    private List<String> trustedIp;
    private List<SubnetUtils> subnetUtils = new ArrayList<SubnetUtils>();

    public static ServerConfiguration getInstance() {
        return instance;
    }

    protected ServerConfiguration(String bundleName) {
        super(bundleName);
        try {
            requestTimeout = getLongProperty(KEY_REQUEST_TIMEOUT);
            domain = getStringProperty(KEY_DOMAIN);
            serverSessionTimeout = getLongProperty(KEY_SERVER_SESSION_TIMEOUT);
            sessionTrackerMaxSleepTime = getLongProperty(KEY_SESSION_TRACKER_MAX_SLEEP_TIME);
            try {
                String casinoType = getStringProperty(KEY_CASINO_SYSTEM_TYPE);
                if(!StringUtils.isTrimmedEmpty(casinoType)) {
                    casinoSystemType = CasinoSystemType.valueOf(casinoType);
                }
            } catch (Exception e) {
                LOG.error("Cannot load casinoSystemType", e);
            }
            try {
                trustAllSslForHttpClientConnections = getBooleanProperty(KEY_TRUST_ALL_SSL_FOR_HTTP_CLIENT);
            } catch (Exception e) {
                LOG.error("Cannot load trustAllSslForHttpClientConnections", e);
            }
            try {
                httpProxyHost = getStringPropertySilent(KEY_HTTP_PROXY_HOST);
                if(!StringUtils.isTrimmedEmpty(httpProxyHost)) {
                    httpProxyPort = getIntProperty(KEY_HTTP_PROXY_PORT);
                }
            } catch(Exception e) {
                LOG.error("Cannot load httpProxy info", e);
            }
            useCassandraStorageForTransferAccounts = getBooleanProperty(KEY_USE_CASSANDRA_STORAGE_FOR_TRANSFER_ACCOUNTS);
            useStrongConsistencyCassandraStorage = getBooleanProperty(KEY_USE_STRONG_CONSISTENCY_CASSANDRA_STORAGE);
            useCassandraStorageForStoreWalletOperations = getBooleanProperty(KEY_USE_CASSANDRA_FOR_STORE_WALLET_OPS);
            trustedIp = getStringsList(KEY_TRUSTED_IP, DEFAULT_ARRAY_DELIMITER);
            if (!CollectionUtils.isEmpty(trustedIp)) {
                for (String ip: trustedIp) {
                    SubnetUtils utils;
                    try {
                        utils = new SubnetUtils(ip);
                    } catch (Exception e) {
                        utils = null;
                        LOG.debug("SubnetUtils constructor error", e);
                    }
                    subnetUtils.add(utils);
                }
                subnetUtils = Collections.unmodifiableList(subnetUtils);
            }
            try{
                useStaticVersioning = getBooleanProperty(KEY_USE_STATIC_VERSIONING);
                sourceVersion = getStringProperty(KEY_SOURCE_VERSION);
            }catch (Exception e){
            }
        } catch (CommonException e) {
            throw new RuntimeException("Init error", e);
        }
    }

    public boolean isTrustAllSslForHttpClientConnections() {
        return trustAllSslForHttpClientConnections;
    }

    public CasinoSystemType getCasinoSystemType() {
        return casinoSystemType;
    }

    public String getDomain() {
        return domain;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public boolean isUserLevelLog() {
        return false;
    }

    public long getServerSessionTimeout() {
        return serverSessionTimeout;
    }

    public long getSessionTrackerMaxSleepTime() {
        return sessionTrackerMaxSleepTime;
    }

    public boolean isUseCassandraStorageForTransferAccounts() {
        return useStrongConsistencyCassandraStorage && useCassandraStorageForTransferAccounts;
    }


    public boolean isUseCassandraStorageForStoreWalletOperations() {
        return useCassandraStorageForStoreWalletOperations;
    }


    public boolean isUseStrongConsistencyCassandraStorage() {
        return useStrongConsistencyCassandraStorage;
    }

    public boolean isUseStaticVersioning() {
        return useStaticVersioning;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public List<String> getTrustedIp() {
        return trustedIp;
    }

    public boolean isIpTrusted(String ip) {
        if (CollectionUtils.isEmpty(trustedIp)) {
            LOG.debug("trustedIp list is empty");
            return true;
        }
        for (SubnetUtils utils : subnetUtils) {
            if (utils == null) {
                continue;
            }
            if (utils.getInfo().isInRange(ip)) {
                return true;
            }
        }
        return false;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ServerConfiguration");
        sb.append("[domain='").append(domain).append('\'');
        sb.append(", requestTimeout=").append(requestTimeout);
        sb.append(", casinoSystemType=").append(casinoSystemType);
        sb.append(", serverSessionTimeout=").append(serverSessionTimeout);
        sb.append(", trustAllSslForHttpClientConnections=").append(trustAllSslForHttpClientConnections);
        sb.append(", useCassandraStorageForTransferAccounts=").append(isUseCassandraStorageForTransferAccounts());
        sb.append(", useStrongConsistencyCassandraStorage=").append(isUseStrongConsistencyCassandraStorage());
        sb.append(", sessionTrackerMaxSleepTime=").append(sessionTrackerMaxSleepTime);
        sb.append(", useStaticVersioning=").append(useStaticVersioning);
        sb.append(", sourceVersion=").append(sourceVersion);
        sb.append(", ").append(KEY_TRUSTED_IP).append("=").append(trustedIp);
        sb.append(", httpProxyHost=").append(httpProxyHost);
        sb.append(", httpProxyPort=").append(httpProxyPort);
        sb.append(']');
        return sb.toString();
    }
}