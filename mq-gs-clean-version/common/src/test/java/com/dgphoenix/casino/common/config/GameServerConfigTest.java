package com.dgphoenix.casino.common.config;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameServerConfigTest {

    private static final List<String> TRUSTED_IP = Arrays.asList("85.17.87.132/29", "94.102.151.130/29", null);

    private GameServerConfigTemplate config;

    @Before
    public void setUp() throws Exception {
        config = new GameServerConfigTemplate();
    }

    @Test
    public void ipTrusted() {
        config.setTrustedIp(TRUSTED_IP);

        boolean trusted = config.isIpTrusted("85.17.87.134");

        assertTrue("IP from trusted subnet. Should return true", trusted);
    }

    @Test
    public void ipUntrusted() {
        config.setTrustedIp(TRUSTED_IP);

        boolean untrusted = config.isIpTrusted("85.17.87.136");

        assertFalse("IP not from trusted subnet. Should return false", untrusted);
    }

    @Test
    public void trustedListIsEmpty() {
        boolean trusted = config.isIpTrusted("85.17.87.136");

        assertTrue("Trusted list is empty. Should return true", trusted);
    }

    @Test
    public void setIllegalCIDRString() {
        config.setTrustedIp(Collections.singletonList("127.0.0.1"));

        boolean untrusted = config.isIpTrusted("127.0.0.1");

        assertFalse("Illegal CIDR sting is set. Should return false", untrusted);
    }
}