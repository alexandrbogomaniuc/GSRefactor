package com.abs.casino.services.geoip;

import com.abs.casino.GeoIp;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraCountryRestrictionPersister;
import com.abs.casino.common.exception.BadArgumentException;
import com.abs.casino.common.geoip.CountryRestrictionList;
import com.abs.casino.common.geoip.RestrictionType;
import com.dgphoenix.casino.common.promo.IPromoCountryRestrictionService;
import com.dgphoenix.casino.common.util.Pair;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CountryRestrictionService implements IPromoCountryRestrictionService {
    private static final Set<String> KNOWN_COUNTRIES = new HashSet<>(Arrays.asList(Locale.getISOCountries()));

    private final GeoIp geoIp;
    private final CassandraCountryRestrictionPersister restrictionPersister;
    private final CountryRestrictionList noRestrictions = new CountryRestrictionList(true, null);

    private final LoadingCache<Pair<Long, RestrictionType>, CountryRestrictionList> restrictionsCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .concurrencyLevel(8)
            .build(new CacheLoader<Pair<Long, RestrictionType>, CountryRestrictionList>() {
                @Override
                public CountryRestrictionList load(@Nonnull Pair<Long, RestrictionType> key) {
                    CountryRestrictionList restrictions = restrictionPersister.get(key.getKey(), key.getValue());
                    return restrictions == null ? noRestrictions : restrictions;
                }
            });

    public CountryRestrictionService(GeoIp geoIp, CassandraPersistenceManager persistenceManager) {
        this.geoIp = geoIp;
        this.restrictionPersister = persistenceManager.getPersister(CassandraCountryRestrictionPersister.class);
    }

    public boolean isAllowed(String ip, long objectId, RestrictionType type) throws BadArgumentException {
        CountryRestrictionList restrictionList = restrictionsCache.getUnchecked(new Pair<>(objectId, type));
        if (restrictionList == noRestrictions) {
            return true;
        }
        String countryCode = geoIp.getCountryCode(ip);
        if (countryCode.equals(GeoIp.CODE_NOT_RECOGNIZED)) {
            throw new BadArgumentException("Unable get country by IP: " + ip + ", objectId = " + objectId + ", type = " + type);
        }
        return restrictionList.isAllowed(countryCode);
    }

    public void save(long objectId, RestrictionType type, CountryRestrictionList countryRestrictions) throws BadArgumentException {
        for (String countryCode : countryRestrictions.getCountries()) {
            if (countryCode.length() != 2) {
                throw new BadArgumentException("Wrong country code = " + countryCode);
            }
            if (!KNOWN_COUNTRIES.contains(countryCode)) {
                throw new BadArgumentException("Unsupported country code = " + countryCode);
            }
        }
        restrictionPersister.persist(objectId, type, countryRestrictions);
    }

    public void remove(long objectId, RestrictionType type) {
        restrictionPersister.delete(objectId, type);
    }

    @Override
    public boolean isCountryAllowed(String ip, long promoId) {
        try {
            return isAllowed(ip, promoId, RestrictionType.PROMO);
        } catch (BadArgumentException ignored) {
            return true;
        }
    }
}
