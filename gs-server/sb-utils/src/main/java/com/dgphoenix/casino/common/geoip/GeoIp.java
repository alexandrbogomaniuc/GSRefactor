package com.abs.casino.common.geoip;

import com.dgphoenix.casino.common.util.IGeoIp;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.record.Country;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class GeoIp implements IGeoIp {
    private static final Logger LOG = Logger.getLogger(GeoIp.class);
    private static final String DATABASE_FILE = "geoip-data-base/GeoLite2-Country.mmdb";
    private static final String COUNTRY_NOT_RECOGNIZED = "N/A";
    private static final String CODE_NOT_RECOGNIZED = "--";
    private static final String UNABLE_TO_GET_BY_IP_MESSAGE = "Unable to get country by IP: ";
    private static final GeoIp instance = new GeoIp();

    private DatabaseReader reader = null;

    public static GeoIp getInstance() {
        return instance;
    }

    private GeoIp() {
        InputStream dataBaseInputStream = null;
        try {
            dataBaseInputStream = getClass().getClassLoader().getResourceAsStream(DATABASE_FILE);
            reader = new DatabaseReader.Builder(dataBaseInputStream).withCache(new CHMCache()).build();
        } catch (Exception e) {
            LOG.error("Failed to initialize GeoIP library", e);
        } finally {
            try {
                if (dataBaseInputStream != null) {
                    dataBaseInputStream.close();
                }
            } catch (IOException e) {
                LOG.error("Failed to close GeoIp data base input stream", e);
            }
        }
    }

    public String getCountryName(String ip) {
        if (reader == null) {
            LOG.error("getCountryName(" + ip + ") reader == null");
            return COUNTRY_NOT_RECOGNIZED;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            Country country = reader.country(ipAddress).getCountry();
            if (country != null && country.getName() != null) {
                LOG.debug("getCountryName(" + ip + ") " + country.getName());
                return country.getName();
            }
        } catch (GeoIp2Exception e) {
            LOG.error(UNABLE_TO_GET_BY_IP_MESSAGE + ip, e);
        } catch (IOException e) {
            LOG.error(UNABLE_TO_GET_BY_IP_MESSAGE + ip, e);
        }
        return COUNTRY_NOT_RECOGNIZED;
    }

    public String getCountryCode(String ip) {
        if (reader == null) {
            LOG.error("getCountryCode(" + ip + ") cl == null");
            return CODE_NOT_RECOGNIZED;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            Country country = reader.country(ipAddress).getCountry();
            if (country != null && country.getIsoCode() != null) {
                LOG.debug("getCountryCode(" + ip + ") " + country.getIsoCode());
                return country.getIsoCode();
            }
        } catch (GeoIp2Exception e) {
            LOG.error(UNABLE_TO_GET_BY_IP_MESSAGE + ip, e);
        } catch (IOException e) {
            LOG.error(UNABLE_TO_GET_BY_IP_MESSAGE + ip, e);
        }
        return CODE_NOT_RECOGNIZED;
    }
}