package com.dgphoenix.casino.common.cache.data.bank;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BankInfoAliasCompatibilityTest {

    @Test
    public void shouldUseAbsAliasForMqFrbDefChipsWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_FRB_DEF_CHIPS", "11");

        assertEquals("11", bankInfo.getMQFrbDefChips());
    }

    @Test
    public void shouldPreferLegacyMqFrbDefChipsOverAlias() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty(BankInfo.MQ_KEY_FRB_DEF_CHIPS, "7");
        bankInfo.setProperty("ABS_FRB_DEF_CHIPS", "11");

        assertEquals("7", bankInfo.getMQFrbDefChips());
    }

    @Test
    public void shouldUseAbsAliasForMqClientLogLevelWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_CLIENT_LOG_LEVEL", MaxQuestClientLogLevel.TRACE.name());

        assertEquals(MaxQuestClientLogLevel.TRACE, bankInfo.getMQClientLogLevel());
    }

    @Test
    public void shouldUseAbsAliasForMqWeaponModeWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_WEAPONS_MODE", MaxQuestWeaponMode.PAID_SHOTS.name());

        assertEquals(MaxQuestWeaponMode.PAID_SHOTS, bankInfo.getMaxQuestWeaponMode());
    }

    @Test
    public void shouldUseAbsAliasForMqBackgroundLoadingFlagWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("DISABLE_ABS_BACKGROUND_LOADING", "true");

        assertTrue(bankInfo.isMQBackgroundLoadingDisabled());
    }

    @Test
    public void shouldUseAbsAliasForMqTournamentRealModeUrlWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_TOURNAMENT_REAL_MODE_URL", "http://localhost:19000/tournament");

        assertEquals("http://localhost:19000/tournament", bankInfo.getMQTournamentRealModeUrl());
    }

    @Test
    public void shouldUseAbsAliasForMqRoomsSortOrderWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_ROOMS_SORT_ORDER", "DESC");

        assertEquals("DESC", bankInfo.getMQRoomsSortOrder());
    }

    @Test
    public void shouldUseAbsAliasForMqStartBonusDisabledFlagWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_PLAYER_START_BONUS_DISABLED", "true");

        assertTrue(bankInfo.isMqStartBonusDisabled());
    }

    @Test
    public void shouldUseAbsAliasForWpmClassWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_WPM_CLASS", "com.abs.wallet.AbsWalletManager");

        assertEquals("com.abs.wallet.AbsWalletManager", bankInfo.getWPMClass());
    }

    @Test
    public void shouldPreferLegacyWpmClassOverAlias() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty(BankInfo.KEY_WPM_CLASS, "com.dgphoenix.wallet.LegacyWalletManager");
        bankInfo.setProperty("ABS_WPM_CLASS", "com.abs.wallet.AbsWalletManager");

        assertEquals("com.dgphoenix.wallet.LegacyWalletManager", bankInfo.getWPMClass());
    }

    @Test
    public void shouldUseAbsAliasForStartGameProcessorWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_START_GAME_PROCESSOR", "com.abs.gs.StartGameProcessor");

        assertEquals("com.abs.gs.StartGameProcessor", bankInfo.getStartGameProcessorClass());
    }

    @Test
    public void shouldPreferLegacyStartGameProcessorOverAlias() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty(BankInfo.KEY_START_GAME_PROCESSOR, "com.dgphoenix.gs.LegacyStartGameProcessor");
        bankInfo.setProperty("ABS_START_GAME_PROCESSOR", "com.abs.gs.StartGameProcessor");

        assertEquals("com.dgphoenix.gs.LegacyStartGameProcessor", bankInfo.getStartGameProcessorClass());
    }

    @Test
    public void shouldUseAbsAliasForCloseGameProcessorWhenLegacyMissing() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty("ABS_CLOSE_GAME_PROCESSOR", "com.abs.gs.CloseGameProcessor");

        assertEquals("com.abs.gs.CloseGameProcessor", bankInfo.getCloseGameProcessorClass());
    }

    @Test
    public void shouldPreferLegacyCloseGameProcessorOverAlias() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setProperty(BankInfo.KEY_CLOSE_GAME_PROCESSOR, "com.dgphoenix.gs.LegacyCloseGameProcessor");
        bankInfo.setProperty("ABS_CLOSE_GAME_PROCESSOR", "com.abs.gs.CloseGameProcessor");

        assertEquals("com.dgphoenix.gs.LegacyCloseGameProcessor", bankInfo.getCloseGameProcessorClass());
    }

    @Test
    public void shouldKeepExistingDefaultWhenBothLegacyAndAliasMissing() {
        BankInfo bankInfo = new BankInfo();

        assertEquals("5", bankInfo.getMQFrbDefChips());
        assertEquals("ASC", bankInfo.getMQRoomsSortOrder());
        assertFalse(bankInfo.isMqStartBonusDisabled());
    }
}
