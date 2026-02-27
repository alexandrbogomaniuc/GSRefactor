package com.abs.casino;

import com.dgphoenix.casino.GeoIp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GeoIpTest {
    private GeoIp geoIp;
    private String australianIp;

    @Before
    public void initGeoIp() {
        geoIp = new GeoIp();
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
