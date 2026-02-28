package com.abs.casino.services.geoip;

import com.dgphoenix.casino.GeoIp;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraCountryRestrictionPersister;
import com.abs.casino.common.exception.BadArgumentException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.geoip.CountryRestrictionList;
import com.abs.casino.common.geoip.RestrictionType;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountryRestrictionServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private GeoIp geoIp;
    @Mock
    private CassandraPersistenceManager persistenceManager;
    @Mock
    private CassandraCountryRestrictionPersister restrictionPersister;
    private CountryRestrictionService restrictionService;

    @Before
    public void before() throws BadArgumentException {
        when(geoIp.getCountryCode("1.1.1.0")).thenReturn(GeoIp.CODE_NOT_RECOGNIZED);
        when(geoIp.getCountryCode("1.1.1.1")).thenReturn("US");
        when(geoIp.getCountryCode("1.1.1.2")).thenReturn("CA");
        when(geoIp.getCountryCode("1.1.1.3")).thenReturn("FR");
        when(persistenceManager.getPersister(CassandraCountryRestrictionPersister.class)).thenReturn(restrictionPersister);
        when(restrictionPersister.get(2, RestrictionType.PROMO))
                .thenReturn(new CountryRestrictionList(true, Sets.newHashSet("CA")));
        when(restrictionPersister.get(3, RestrictionType.PROMO))
                .thenReturn(new CountryRestrictionList(false, Sets.newHashSet("FR")));
        restrictionService = new CountryRestrictionService(geoIp, persistenceManager);
    }

    @Test
    public void countryCodeNotRecognized() throws CommonException {
        thrown.expect(BadArgumentException.class);
        restrictionService.isAllowed("1.1.1.0", 2L, RestrictionType.PROMO);
    }

    @Test
    public void isAllowedAny() throws CommonException {
        Assert.assertTrue(restrictionService.isAllowed("1.1.1.1", 1L, RestrictionType.PROMO));
        Assert.assertTrue(restrictionService.isAllowed("1.1.1.2", 1L, RestrictionType.PROMO));
        Assert.assertTrue(restrictionService.isAllowed("1.1.1.3", 1L, RestrictionType.PROMO));
    }

    @Test
    public void isAllowedWhite() throws CommonException {
        Assert.assertFalse(restrictionService.isAllowed("1.1.1.1", 2L, RestrictionType.PROMO));
        Assert.assertTrue(restrictionService.isAllowed("1.1.1.2", 2L, RestrictionType.PROMO));
        Assert.assertFalse(restrictionService.isAllowed("1.1.1.3", 2L, RestrictionType.PROMO));
    }

    @Test
    public void isAllowedBlack() throws CommonException {
        Assert.assertTrue(restrictionService.isAllowed("1.1.1.1", 3L, RestrictionType.PROMO));
        Assert.assertTrue(restrictionService.isAllowed("1.1.1.2", 3L, RestrictionType.PROMO));
        Assert.assertFalse(restrictionService.isAllowed("1.1.1.3", 3L, RestrictionType.PROMO));
    }

    @Test
    public void wrongCountryCode() throws BadArgumentException {
        thrown.expectMessage("Wrong country code");
        restrictionService.save(1L, RestrictionType.PROMO, new CountryRestrictionList(true, Sets.newHashSet("USA")));
    }

    @Test
    public void unsupportedCountryCode() throws BadArgumentException {
        thrown.expectMessage("Unsupported country code");
        restrictionService.save(1L, RestrictionType.PROMO, new CountryRestrictionList(true, Sets.newHashSet("AA")));
    }
}
