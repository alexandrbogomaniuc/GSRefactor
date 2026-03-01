package com.abs.casino.common.cache.data.payment;

import com.abs.casino.gs.managers.payment.wallet.IWalletOperation;
import com.abs.casino.common.cache.data.payment.WalletOperationType;
import com.abs.casino.common.cache.data.payment.WalletOperationStatus;

import java.util.Date;

/**
 * User: plastical
 * Date: 02.03.2010
 */
public abstract class AbstractWalletOperation implements IWalletOperation {
    protected long id;
    protected long accountId;
    protected Long gameSessionId;
    protected Long roundId;
    protected long amount;
    protected long startTime;
    protected Long endTime;
    protected WalletOperationType type;
    protected WalletOperationStatus externalStatus;
    protected WalletOperationStatus internalStatus;
    protected String description;
    protected String externalSessionId;
    protected long negativeBet;
    protected String additionalProperties;

    protected AbstractWalletOperation() {
    }

    public AbstractWalletOperation(long walletOperationId, long accountId, long gameSessionId,
                                   long roundId, long amount, WalletOperationType type,
                                   WalletOperationStatus externalStatus,
                                   WalletOperationStatus internalStatus, String description, long startTime,
                                   Long endTime, long negativeBet) {
        this.id = walletOperationId;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.amount = amount;
        this.type = type;
        this.externalStatus = externalStatus;
        this.internalStatus = internalStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.negativeBet = negativeBet;
    }

    public AbstractWalletOperation(long id, long accountId, Long gameSessionId, Long roundId,
                                   long amount,
                                   WalletOperationType type, String description, WalletOperationStatus externalStatus,
                                   WalletOperationStatus internalStatus, long negativeBet) {
        this.id = id;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.externalStatus = externalStatus;
        this.internalStatus = internalStatus;
        this.startTime = System.currentTimeMillis();
        this.negativeBet = negativeBet;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setType(WalletOperationType type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public long getAmount() {
        return amount;
    }

    public WalletOperationType getType() {
        return type;
    }

    public WalletOperationStatus getExternalStatus() {
        return externalStatus;
    }

    public WalletOperationStatus getInternalStatus() {
        return internalStatus;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime == null ? 0 : endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getExternalRoundId() {
        return null;
    }

    public void setExternalRoundId(String roundId) {
        //nop
    }

    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalStatus(WalletOperationStatus externalStatus) {
        this.externalStatus = externalStatus;
    }

    public void setInternalStatus(WalletOperationStatus internalStatus) {
        this.internalStatus = internalStatus;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public void update(WalletOperationStatus internal, Long endTime) {
        this.internalStatus = internal;
        this.endTime = endTime;
    }

    public long getNegativeBet() {
        return negativeBet;
    }

    public void setNegativeBet(long negativeBet) {
        this.negativeBet = negativeBet;
    }

    public boolean isOverdue(long time) {
        return System.currentTimeMillis() - startTime > time;
    }

    @Override
    public String getAdditionalProperties() {
        return additionalProperties;
    }

    @Override
    public synchronized void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AbstractWalletOperation");
        sb.append("[description='").append(description).append('\'');
        sb.append(", id=").append(id);
        sb.append(", accountId=").append(accountId);
        sb.append(", internalStatus=").append(internalStatus == null ? "null" : internalStatus);
        sb.append(", externalStatus=").append(externalStatus == null ? "null" : externalStatus);
        sb.append(", type=").append(type == null ? "null" : type);
        sb.append(", endTime=").append(endTime == null ? "null" : new Date(endTime));
        sb.append(", startTime=").append(new Date(startTime));
        sb.append(", amount=").append(amount);
        sb.append(", negativeBet=").append(negativeBet);
        sb.append(", roundId=").append(roundId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", additionalProperties=").append(additionalProperties);
        sb.append(']');
        return sb.toString();
    }
}
