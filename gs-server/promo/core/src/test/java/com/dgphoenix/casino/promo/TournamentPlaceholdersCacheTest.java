package com.abs.casino.promo;

import com.google.common.cache.Cache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TournamentPlaceholdersCacheTest {
    private TournamentPlaceholdersCache placeholdersCache;

    @Before
    public void init() {
        placeholdersCache = new TournamentPlaceholdersCache();
    }

    @Test
    public void invalidateByTournamentId_shouldChooseAllKeysWithGivenId() {
        placeholdersCache.put(TournamentPlaceholdersCache.WELCOME_MSG_CACHE_KEY+"_1_eng_usd", "val");
        placeholdersCache.put(TournamentPlaceholdersCache.RULES_CACHE_KEY+"_1_eng_rub", "val");
        placeholdersCache.put(TournamentPlaceholdersCache.WELCOME_MSG_CACHE_KEY+"_2_eng_usd", "val");
        placeholdersCache.put(TournamentPlaceholdersCache.RULES_CACHE_KEY+"_3_rus_usd", "val");

        placeholdersCache.invalidateByTournamentId(1);
        assertNull(placeholdersCache.getIfPresent("welcomeMsg_1_eng_usd"));
        assertNull(placeholdersCache.getIfPresent("rules_1_eng_rub"));
        assertNotNull(placeholdersCache.getIfPresent("welcomeMsg_2_eng_usd"));
        assertNotNull(placeholdersCache.getIfPresent("rules_3_rus_usd"));
    }

    @Test
    public void composeKey_shouldConcatenateGivenParams() {
        String expected = "prefix_1_eng_usd";
        assertEquals(expected, placeholdersCache.composeKey("prefix", 1, "eng", "usd"));
    }
}