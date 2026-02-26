package com.dgphoenix.casino.common.config;

import com.abs.casino.common.config.ClusterType;
import com.dgphoenix.casino.common.cache.ServerConfigsCache;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 18.01.2022
 */
@RunWith(MockitoJUnitRunner.class)
public class HostConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    ;
    @Mock
    private ServerConfigsCache configsCache;
    @Mock
    private GameServerConfig config;
    @Mock
    private GameServerConfigTemplate template;


    @Before
    public void setUp() throws Exception {
        when(configsCache.getServerConfig(1)).thenReturn(config);
        when(config.getTemplate()).thenReturn(template);
    }

    @Test
    public void createWithoutGSId() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Game server id must be not null");

        new HostConfiguration(null, null);
    }

    @Test
    public void createWithoutConfig() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Couldn't find GameServerConfig");

        ServerConfigsCache configsCache = mock(ServerConfigsCache.class);

        new HostConfiguration(1, configsCache);
    }

    @Test
    public void createWithoutGsDomain() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("GsDomain could not be empty");

        new HostConfiguration(1, configsCache);
    }

    @Test
    public void createSuccessfully() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1.domain.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        assertNotNull(hostConfiguration);
    }

    @Test
    public void betsoftProductionCluster() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1.betsoftgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        ClusterType clusterType = hostConfiguration.getClusterType();

        assertEquals(ClusterType.PRODUCTION, clusterType);
    }

    @Test
    public void nucleusProductionCluster() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1.nucleusgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        ClusterType clusterType = hostConfiguration.getClusterType();

        assertEquals(ClusterType.PRODUCTION, clusterType);
    }

    @Test
    public void nucleusStagingCluster() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1-ng-copy.nucleusgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        ClusterType clusterType = hostConfiguration.getClusterType();

        assertEquals(ClusterType.STAGING, clusterType);
    }

    @Test
    public void stagingCluster() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1-beta.discreetgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        ClusterType clusterType = hostConfiguration.getClusterType();

        assertEquals(ClusterType.STAGING, clusterType);
    }

    @Test
    public void developmentCluster() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1-gp3.dgphoenix.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        ClusterType clusterType = hostConfiguration.getClusterType();

        assertEquals(ClusterType.DEVELOPMENT, clusterType);
    }

    @Test
    public void clusterName() {
        when(config.getTemplate().getGsDomain()).thenReturn("-beta.discreetgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        String clusterName = hostConfiguration.getClusterName();

        assertEquals("beta", clusterName);
    }

    @Test
    public void mqbClusterName() {
        when(config.getTemplate().getGsDomain()).thenReturn("-mqb.maxquest.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        String clusterName = hostConfiguration.getClusterName();

        assertEquals("mqb", clusterName);
    }

    @Test
    public void ngClusterName() {
        when(config.getTemplate().getGsDomain()).thenReturn("-ng-copy.nucleusgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        String clusterName = hostConfiguration.getClusterName();

        assertEquals("ng", clusterName);
    }

    @Test
    public void getSecondLevelDomain() {
        when(config.getTemplate().getGsDomain()).thenReturn("-beta.discreetgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        String domainName = hostConfiguration.getSecondLevelDomainName();

        assertEquals("discreetgaming.com", domainName);
    }

    @Test
    public void nonProductionCuracaoHost() {
        when(config.getTemplate().getGsDomain()).thenReturn("-beta.discreetgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        String curacaoHost = hostConfiguration.getCuracaoHost("a.curacao.com");

        assertEquals("gs.curacao.com", curacaoHost);
    }

    @Test
    public void curacaoHostOnProduction() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1.betsoftgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        String curacaoHost = hostConfiguration.getCuracaoHost("play-sb.betsoftgaming.com");

        assertEquals("play-cur.betsoftgaming.com", curacaoHost);
    }

    @Test
    public void curacaoHostOnProductionWithSubDomain() {
        when(config.getTemplate().getGsDomain()).thenReturn("gs1.betsoftgaming.com");
        HostConfiguration hostConfiguration = new HostConfiguration(1, configsCache);

        String curacaoHost = hostConfiguration.getCuracaoHost("play.sb.betsoftgaming.com");

        assertEquals("play.sb.cur.betsoftgaming.com", curacaoHost);
    }
}
