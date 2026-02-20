package com.dgphoenix.casino.common.util.support;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 24.01.2020
 */
public enum AdditionalInfoAttribute {

    ACCOUNT_ID("accountId"),
    BANK_ID("bankId"),
    BET_NUMBER("betNumber"),
    BONUS_ID("bonusId"),
    CENTS("cents"),
    ERROR_MESSAGES("errorMessages"),
    EXTERNAL_ID("externalId"),
    GAME_ID("gameId"),
    GAME_SESSION_ID("gameSessionId"),
    LANG("lang"),
    MODE("mode"),
    REQUEST_TIMEOUT_IN_SECONDS("requestTimeoutInSeconds"),
    RETURNED_BET("returnedBet"),
    ROOM_ID("roomId"),
    ROUND_ID("roundId"),
    ROUND_INFO_RESULT("roundInfoResult"),
    SESSION_ID("sessionId"),
    SUPPORT_TICKET_ID("supportTicketId"),
    TIMESTAMP("timestamp"),
    TOKEN("token"),
    WALLET_STATE("walletState"),
    TRANSACTION_ID("transactionId");

    private final String attributeName;

    AdditionalInfoAttribute(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
