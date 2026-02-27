package com.abs.casino.common.geoip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GeoIpTest {
    private GeoIp geoIp;
    private String australianIp;

    @Before
    public void initGeoIp() {
        geoIp = GeoIp.getInstance();
        australianIp = "1.1.1.1";
    }

    @Test
    public void australianCountryCodeByIpIsValid() {
        String australianCountryCode = "AU";
        Assert.assertEquals(australianCountryCode, geoIp.getCountryCode(australianIp));
    }

    @Test
    public void australianCountryNameByIpIsValid() {
        String australianCountryName = "Australia";
        Assert.assertEquals(australianCountryName, geoIp.getCountryName(australianIp));
    }
}