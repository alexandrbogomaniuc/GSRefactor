package com.abs.casino.services.bonus;

import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.abs.casino.services.bonus.ForbiddenGamesForBonusProvider;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 07.06.2021
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ForbiddenGamesForBonusProviderTest {

    @Mock
    private SubCasinoCache subCasinoCache;

    private ForbiddenGamesForBonusProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new ForbiddenGamesForBonusProvider(subCasinoCache);
    }

    @Test
    public void testCommonGames() throws IllegalAccessException {
        Set<Long> expectedGames = (Set<Long>) FieldUtils.readField(provider, "COMMON_GAMES", true);

        Set<Long> actualGames = provider.getGames(10);

        assertEquals(expectedGames, actualGames);
    }

    @Test
    public void testRoyaalGames() throws IllegalAccessException {
        long royaalCasinoBankId = (long) FieldUtils.readField(provider, "ROYAAL_CASINO_BANK_ID", true);
        Set<Long> expectedGames = (Set<Long>) FieldUtils.readField(provider, "ROYAAL_GAMES", true);

        Set<Long> actualGames = provider.getGames(royaalCasinoBankId);

        assertEquals(expectedGames, actualGames);
    }

    @Test
    public void testBetOnlineNgGames() throws IllegalAccessException {
        long betOnlineSubcasinoId = (long) FieldUtils.readField(provider, "BETONLINE_NG_SUBCASINO_ID", true);
        Set<Long> expectedGames = (Set<Long>) FieldUtils.readField(provider, "BETONLINE_NG_GAMES", true);
        when(subCasinoCache.getBankIds(betOnlineSubcasinoId)).thenReturn(Arrays.asList(120L, 121L));

        Set<Long> actualGames = provider.getGames(120);

        assertEquals(expectedGames, actualGames);
    }
}
