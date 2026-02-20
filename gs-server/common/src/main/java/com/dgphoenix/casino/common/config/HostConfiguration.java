package com.dgphoenix.casino.common.config;

import com.dgphoenix.casino.common.cache.ServerConfigsCache;
import com.dgphoenix.casino.common.util.string.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 18.01.2022
 */
public class HostConfiguration {

    private final String gsDomain;
    private final ClusterType clusterType;
    private final String clusterName;
    private final int serverId;

    public HostConfiguration(Integer gameServerId, ServerConfigsCache configsCache) {
        this(null, gameServerId, configsCache);
    }

    public HostConfiguration(String clusterTypeAsString, Integer gameServerId, ServerConfigsCache configsCache) {
        if (gameServerId == null) {
            throw new IllegalStateException("Game server id must be not null");
        }

        GameServerConfig config = configsCache.getServerConfig(gameServerId);
        checkNotNull(config, "Couldn't find GameServerConfig");
        gsDomain = initGsDomainName(config.getTemplate());
        clusterType = initClusterType(clusterTypeAsString);
        clusterName = initClusterName();
        serverId = config.getServerId();
    }

    private String initGsDomainName(GameServerConfigTemplate config) {
        String domainName = config.getGsDomain();
        checkState(isNotEmpty(domainName), "GsDomain could not be empty");
        return domainName;
    }

    private ClusterType initClusterType(String clusterTypeAsString) {
        if (StringUtils.isTrimmedEmpty(clusterTypeAsString)) {

            if (gsDomain.endsWith("softgaming.com") || ( gsDomain.endsWith("eusgaming.com") && !gsDomain.contains("copy") )) {
                return ClusterType.PRODUCTION;
            }

            if(gsDomain.endsWith("dgphoenix.com")) {
                return ClusterType.DEVELOPMENT;
            }
        } else {
            try {
                return ClusterType.fromString(clusterTypeAsString);
            } catch (Exception exception) {

            }
        }

        return ClusterType.STAGING;
    }

    private String initClusterName() {
        return gsDomain.substring(1, gsDomain.indexOf('.', 1)).split("-")[0];
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public boolean isProductionCluster() {
        return ClusterType.PRODUCTION.equals(clusterType);
    }

    public String getSecondLevelDomainName() {
        return gsDomain.substring(gsDomain.indexOf('.') + 1);
    }

    public String getCuracaoHost(String url) {
        String curacaoHost = url;
        if (isProductionCluster()) {
            if (url.contains(".sb")) {
                String secondLevelDomainName = this.getSecondLevelDomainName();
                curacaoHost = url.substring(0, url.indexOf('.' + secondLevelDomainName)) + ".cur." + secondLevelDomainName;
            } else {
                curacaoHost = curacaoHost.replace("-sb", "-cur");
            }
        } else {
            curacaoHost = "gs" + url.substring(1);
        }
        return curacaoHost;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getGsDomain() {
        return gsDomain;
    }

    public int getServerId() {
        return serverId;
    }
}
