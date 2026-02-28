package com.abs.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusGameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.gs.managers.payment.bonus.CreationBonusHelper;
import com.abs.casino.services.bonus.ForbiddenGamesForBonusProvider;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 28.06.2021
 */
@RunWith(MockitoJUnitRunner.class)
public class CreationBonusHelperTest {

    @Mock
    private BankInfoCache bankInfoCache;
    @Mock
    private BaseGameCache baseGameCache;
    @Mock
    private BaseGameInfoTemplateCache baseGameInfoTemplateCache;
    @Mock
    private ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider;
    @Mock
    private BankInfo bankInfo;
    @Mock
    @SuppressWarnings("rawtypes")
    private IBaseGameInfo baseGameInfo;

    private CreationBonusHelper creationBonusHelper;

    @Before
    public void setUp() throws Exception {
        creationBonusHelper = new CreationBonusHelper(bankInfoCache, baseGameCache, baseGameInfoTemplateCache, forbiddenGamesForBonusProvider);
    }

    @Test
    public void testValidGameIds() {
        List<Long> gameIds = Arrays.asList(200L, 201L, 202L, 203L);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(baseGameCache.getAllGamesSet(anyLong(), any())).thenReturn(Sets.newHashSet(200L, 201L, 202L));
        List<Long> expected = Arrays.asList(200L, 201L, 202L);

        List<Long> actual = creationBonusHelper.getValidGameIds(271, gameIds);

        assertEquals(expected, actual);
    }

    @Test
    public void testFilteringNonActionBonusGamesWithModeOnly() {
        List<Long> gameIds = Arrays.asList(200L, 201L, 202L, 203L, 207L);
        prepareBonusGames();
        List<Long> expected = Arrays.asList(200L, 201L, 202L, 203L);

        List<Long> actual = creationBonusHelper.filterBonusGames(gameIds, 10L, false, BonusGameMode.ONLY);

        assertEquals(expected, actual);
    }

    @Test
    public void testFilteringActionBonusGamesWithModeOnly() {
        List<Long> gameIds = Arrays.asList(300L, 301L);
        prepareBonusGames();
        List<Long> expected = Arrays.asList(300L, 301L);

        List<Long> actual = creationBonusHelper.filterBonusGames(gameIds, 10L, true, BonusGameMode.ONLY);

        assertEquals(expected, actual);
    }

    @Test
    public void testFilteringNonActionBonusGamesWithModeExcept() {
        List<Long> gameIds = Arrays.asList(200L, 201L);
        prepareBonusGames();
        List<Long> expected = Arrays.asList(202L, 203L, 204L, 205L);

        List<Long> actual = creationBonusHelper.filterBonusGames(gameIds, 10L, false, BonusGameMode.EXCEPT);

        assertEquals(expected, actual);
    }

    @Test
    public void testFilteringNonActionBonusGamesWithModeAll() {
        List<Long> gameIds = Collections.emptyList();
        prepareBonusGames();
        List<Long> expected = Arrays.asList(200L, 201L, 202L, 203L, 204L, 205L);

        List<Long> actual = creationBonusHelper.filterBonusGames(gameIds, 10L, false, BonusGameMode.ALL);

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMixingNonActionAndActionGames() {
        List<Long> gameIds = Arrays.asList(200L, 300L);
        prepareBonusGames();
        creationBonusHelper.filterBonusGames(gameIds, 10L, false, BonusGameMode.ONLY);
    }

    @Test
    public void testFilteringFrbonusGamesWith() {
        List<Long> gameIds = Arrays.asList(200L, 201L, 202L, 203L, 207L);
        prepareFrbonusGames();
        List<Long> expected = Arrays.asList(200L, 201L, 202L, 203L);

        List<Long> actual = creationBonusHelper.filterFRBonusGames(gameIds, 10L, false, BonusGameMode.ONLY);

        assertEquals(expected, actual);
    }

    @Test
    public void testGettingBonusGamesWithoutSingleGameIdForAllDevices() {
        List<Long> expected = Arrays.asList(200L, 201L, 202L, 203L);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isUseSingleGameIdForAllDevices()).thenReturn(false);
        when(baseGameCache.getAllGamesSet(anyLong(), any())).thenReturn(Sets.newHashSet(expected));

        List<Long> actual = creationBonusHelper.getBonusGames(10L);

        assertEquals(expected, actual);
    }

    @Test
    public void testGettingBonusGamesWithSingleGameIdForAllDevices() {
        List<Long> allGameIds = Arrays.asList(200L, 201L, 202L, 203L, 204L);
        List<Boolean> areMobileGames = Arrays.asList(false, true, false, true, true);
        List<Long> mobileGameIds = Arrays.asList(201L, 203L, 204L);
        List<Long> expected = Arrays.asList(200L, 202L);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isUseSingleGameIdForAllDevices()).thenReturn(true);
        when(baseGameCache.getAllGamesSet(anyLong(), any())).thenReturn(Sets.newHashSet(allGameIds));
        when(baseGameCache.getGameInfoShared(anyLong(), anyLong(), any())).thenReturn(baseGameInfo);
        when(baseGameInfo.isMobile()).thenAnswer(AdditionalAnswers.returnsElementsOf(areMobileGames));
        when(baseGameInfo.getId()).thenAnswer(AdditionalAnswers.returnsElementsOf(mobileGameIds));

        List<Long> actual = creationBonusHelper.getBonusGames(10L);

        assertEquals(expected, actual);
    }

    private void prepareBonusGames() {
        List<Long> allGameIds = Arrays.asList(200L, 201L, 202L, 203L, 204L, 205L, 300L, 301L);
        Set<Long> allActionGames = Sets.newHashSet(300L, 301L);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(baseGameCache.getAllGamesSet(anyLong(), any())).thenReturn(new HashSet<>(allGameIds));
        when(baseGameCache.getGameInfoById(anyLong(), anyLong(), any())).thenReturn(baseGameInfo);
        when(baseGameInfo.isEnabled()).thenReturn(true);
        when(baseGameInfo.getId()).thenAnswer(AdditionalAnswers.returnsElementsOf(allGameIds));
        when(baseGameInfoTemplateCache.getMultiplayerGames()).thenReturn(allActionGames);
    }

    private void prepareFrbonusGames() {
        List<Long> allGameIds = Arrays.asList(200L, 201L, 202L, 203L, 204L, 205L, 300L, 301L);
        Set<Long> allActionGames = Sets.newHashSet(300L, 301L);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfoCache.getFrbGames(any())).thenReturn(Sets.newHashSet(allGameIds));
        when(baseGameCache.getGameInfoById(anyLong(), anyLong(), any())).thenReturn(baseGameInfo);
        when(baseGameInfo.isEnabled()).thenReturn(true);
        when(baseGameInfo.getId()).thenAnswer(AdditionalAnswers.returnsElementsOf(allGameIds));
        when(baseGameInfoTemplateCache.getMultiplayerGames()).thenReturn(allActionGames);
    }
}
