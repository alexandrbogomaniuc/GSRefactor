package com.abs.casino.cassandra;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.09.16
 */
public class KeyspaceManagerFactory {

    private final KeyspaceConfigurationFactory configurationFactory;

    public KeyspaceManagerFactory(KeyspaceConfigurationFactory configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    public IKeyspaceManager create(String configFilename, String schemaScriptFilename, PersisterDependencyInjector persisterDependencyInjector) {
        KeyspaceConfiguration configuration = configurationFactory.create(configFilename);
        return new KeyspaceManagerImpl(configuration, new PersistersFactory(persisterDependencyInjector), schemaScriptFilename);
    }
}
