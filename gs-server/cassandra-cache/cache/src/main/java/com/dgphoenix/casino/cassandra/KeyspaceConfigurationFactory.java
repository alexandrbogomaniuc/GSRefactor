package com.abs.casino.cassandra;

import com.abs.casino.cassandra.config.ClusterConfig;
import com.abs.casino.common.configuration.ConfigHelper;
import com.dgphoenix.casino.common.util.NtpTimeProvider;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.09.16
 */
public class KeyspaceConfigurationFactory {

    private final ConfigHelper configHelper;
    private final NtpTimeProvider timeProvider;

    public KeyspaceConfigurationFactory(ConfigHelper configHelper, NtpTimeProvider timeProvider) {
        this.configHelper = configHelper;
        this.timeProvider = timeProvider;
        configHelper.registerAlias(ClusterConfig.class);
    }

    public KeyspaceConfiguration create(String configFilename) {
        KeyspaceConfiguration configuration = new KeyspaceConfiguration(configFilename, configHelper, timeProvider);
        configuration.load();
        return configuration;
    }
}
