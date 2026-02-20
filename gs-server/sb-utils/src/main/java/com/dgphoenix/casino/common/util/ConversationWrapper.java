package com.dgphoenix.casino.common.util;

/**
 * User: plastical
 * Date: 28.07.2010
 */
public class ConversationWrapper<T> {
    private Long accountId;
    private String externalId;
    private Long bankId;
    private Long gameId;
    private Long gameSessionId;
    private Long roundId;
    private T value;

    public ConversationWrapper() {
    }

    public ConversationWrapper(String externalId, Long bankId, T value) {
        this.externalId = externalId;
        this.bankId = bankId;
        this.value = value;
    }

    public ConversationWrapper(Long bankId, T value) {
        this.bankId = bankId;
        this.value = value;
    }

    public ConversationWrapper(Long accountId, String externalId, Long bankId, Long gameId, Long gameSessionId,
                                 Long roundId, T value) {
        this.accountId = accountId;
        this.externalId = externalId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.value = value;
    }

    public ConversationWrapper(Long accountId, Long bankId, Long gameId, Long gameSessionId, Long roundId, T value) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.value = value;
    }

    public ConversationWrapper(Long accountId, Long bankId, Long gameId, Long gameSessionId, T value) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.gameSessionId = gameSessionId;
        this.value = value;
    }

    public ConversationWrapper(Long accountId, Long bankId, Long gameId, T value) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.value = value;
    }

    public ConversationWrapper(Long accountId, Long bankId, T value) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.value = value;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ConversationWrapper");
        sb.append("{accountId=").append(accountId);
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(", gameId=").append(gameId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", roundId=").append(roundId);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
