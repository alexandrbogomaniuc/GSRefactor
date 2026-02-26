package com.abs.casino.common.cache.data.bank;

/**
 * User: flsh
 * Date: 9/14/12
 */
public enum PlayerGameSettingsType {
    /* NONE - use from BaseGameInfo,
       ACCOUNT - from AccountInfo,
       DEDICATED - from CassandraPlayerGameSettingsPersister/Transactiondata
    */
    NONE, ACCOUNT, DEDICATED
}
