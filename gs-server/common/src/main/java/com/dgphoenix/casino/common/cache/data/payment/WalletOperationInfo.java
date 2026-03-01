package com.abs.casino.common.cache.data.payment;

import com.abs.casino.common.cache.data.payment.WalletOperationStatus;
import com.abs.casino.common.cache.data.payment.WalletOperationType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Date;

/**
 * User: flsh
 * Date: 18.07.13
 */
public class WalletOperationInfo implements KryoSerializable {
    private static final byte VERSION = 1;
    private long id;
    private long accountId;
    private long bankId;
    private long subCasinoId;
    private long gameId;
    private long gameSessionId;
    private long roundId;
    private long amount;
    private long startTime;
    private long endTime;
    private WalletOperationType type;
    private WalletOperationStatus externalStatus;
    private WalletOperationStatus internalStatus;
    private String externalTransactionId;
    private String externalRoundId;
    private String externalSessionId;
    private String description;
    private long negativeBet;
    private long balance;
    private boolean hasRefund;

    public WalletOperationInfo() {
    }

    public WalletOperationInfo(long id, long accountId, long bankId, long subCasinoId, long gameSessionId, long roundId,
                               long amount, long startTime, long endTime,
                               WalletOperationType type,
                               WalletOperationStatus externalStatus,
                               WalletOperationStatus internalStatus, String externalTransactionId,
                               String externalRoundId, String externalSessionId, String description,
                               long negativeBet) {
        this.id = id;
        this.accountId = accountId;
        this.bankId = bankId;
        this.subCasinoId = subCasinoId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.amount = amount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.externalStatus = externalStatus;
        this.internalStatus = internalStatus;
        this.externalTransactionId = externalTransactionId;
        this.externalRoundId = externalRoundId;
        this.externalSessionId = externalSessionId;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Date getUpdateDate() {
        return new Date(endTime);
    }

    public void setUpdateDate(Date updateDate) {
        //nop
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(long subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public WalletOperationType getType() {
        return type;
    }

    public void setType(WalletOperationType type) {
        this.type = type;
    }

    public WalletOperationStatus getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(WalletOperationStatus externalStatus) {
        this.externalStatus = externalStatus;
    }

    public WalletOperationStatus getInternalStatus() {
        return internalStatus;
    }

    public void setInternalStatus(WalletOperationStatus internalStatus) {
        this.internalStatus = internalStatus;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getExternalRoundId() {
        return externalRoundId;
    }

    public void setExternalRoundId(String externalRoundId) {
        this.externalRoundId = externalRoundId;
    }

    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getNegativeBet() {
        return negativeBet;
    }

    public void setNegativeBet(long negativeBet) {
        this.negativeBet = negativeBet;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public boolean hasRefund() {
        return hasRefund;
    }

    public void setHasRefund(boolean hasRefund) {
        this.hasRefund = hasRefund;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(accountId);
        output.writeLong(bankId);
        output.writeLong(subCasinoId);
        output.writeLong(gameSessionId);
        output.writeLong(roundId);
        output.writeLong(amount);
        output.writeLong(startTime);
        output.writeLong(endTime);
        output.writeInt(type.ordinal(), true);
        output.writeInt(externalStatus.ordinal(), true);
        output.writeInt(internalStatus.ordinal(), true);
        output.writeString(externalTransactionId);
        output.writeString(externalRoundId);
        output.writeString(externalSessionId);
        output.writeString(description);
        output.writeLong(negativeBet);
        output.writeLong(gameId);
        output.writeLong(balance);
        output.writeBoolean(hasRefund);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        accountId = input.readLong();
        bankId = input.readLong();
        subCasinoId = input.readLong();
        gameSessionId = input.readLong();
        roundId = input.readLong();
        amount = input.readLong();
        startTime = input.readLong();
        endTime = input.readLong();
        type = WalletOperationType.values()[input.readInt(true)];
        externalStatus = WalletOperationStatus.values()[input.readInt(true)];
        internalStatus = WalletOperationStatus.values()[input.readInt(true)];
        externalTransactionId = input.readString();
        externalRoundId = input.readString();
        externalSessionId = input.readString();
        description = input.readString();
        negativeBet = input.readLong();
        if (ver > 0) {
            gameId = input.readLong();
            balance = input.readLong();
            hasRefund = input.readBoolean();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WalletOperationInfo that = (WalletOperationInfo) o;

        if (id != that.id) return false;
        if (accountId != that.accountId) return false;
        if (bankId != that.bankId) return false;
        if (gameSessionId != that.gameSessionId) return false;
        if (roundId != that.roundId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WalletOperationInfo [");
        sb.append("id=").append(id);
        sb.append(", accountId=").append(accountId);
        sb.append(", bankId=").append(bankId);
        sb.append(", subCasinoId=").append(subCasinoId);
        sb.append(", gameId=").append(gameId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", roundId=").append(roundId);
        sb.append(", amount=").append(amount);
        sb.append(", negativeBet=").append(negativeBet);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", type=").append(type);
        sb.append(", externalStatus=").append(externalStatus);
        sb.append(", internalStatus=").append(internalStatus);
        sb.append(", externalTransactionId='").append(externalTransactionId).append('\'');
        sb.append(", externalRoundId='").append(externalRoundId).append('\'');
        sb.append(", externalSessionId='").append(externalSessionId).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", balance='").append(balance).append('\'');
        sb.append(", hasRefund='").append(hasRefund).append('\'');
        sb.append(']');
        return sb.toString();
    }
}

