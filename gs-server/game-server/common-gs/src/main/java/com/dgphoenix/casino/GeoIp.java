package com.dgphoenix.casino;

import com.dgphoenix.casino.common.util.IGeoIp;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.record.Country;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class GeoIp implements IGeoIp {
    private static final Logger LOG = LogManager.getLogger(GeoIp.class);
    private static final String DATABASE_FILE = "geoip-data-base/GeoLite2-Country.mmdb";
    private static final String COUNTRY_NOT_RECOGNIZED = "N/A";
    public static final String CODE_NOT_RECOGNIZED = "--";

    private DatabaseReader reader = null;

    public GeoIp() {
        try (InputStream dataBaseInputStream = new ClassPathResource(DATABASE_FILE).getInputStream()) {
            reader = new DatabaseReader
                    .Builder(dataBaseInputStream)
                    .withCache(new CHMCache())
                    .build();
        } catch (IOException e) {
            LOG.error("Failed to initialize GeoIP library", e);
        }
    }

    @Override
    public String getCountryName(String ip) {
        if (reader == null) {
            return COUNTRY_NOT_RECOGNIZED;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            Country country = reader.country(ipAddress).getCountry();
            if (country != null && country.getName() != null) {
                return country.getName();
            }
        } catch (GeoIp2Exception | IOException e) {
            LOG.error("Unable get country by IP: {}, error={}", ip, e.getMessage());
        }
        return COUNTRY_NOT_RECOGNIZED;
    }

    @Override
    public String getCountryCode(String ip) {
        if (reader == null) {
            return CODE_NOT_RECOGNIZED;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            Country country = reader.country(ipAddress).getCountry();
            if (country != null && country.getIsoCode() != null) {
                return country.getIsoCode();
            }
        } catch (GeoIp2Exception | IOException e) {
            LOG.error("Unable get country by IP: {}, error={}", ip, e.getMessage());
        }
        return CODE_NOT_RECOGNIZED;
    }
}