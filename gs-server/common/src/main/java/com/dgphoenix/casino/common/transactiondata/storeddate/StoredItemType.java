package com.dgphoenix.casino.common.transactiondata.storeddate;

/**
 * User: Grien
 * Date: 22.12.2014 12:20
 */
public enum StoredItemType implements Comparable<StoredItemType> {
    //order of values is IMPORTANT. Order of values determines the order of processing.
    TRANSFER_PLAYER_BET,
    GAME_SESSION,
    ACCOUNT,
    PLAYER_BET,
    LASTHAND,
    PAYMENT_TRANSACTION,
    PROMO_TOURNAMENT_RANKS,
    SHORT_BET_INFO,
    PROMO_MEMBERS
}
