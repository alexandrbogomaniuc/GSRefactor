package com.dgphoenix.casino.common.cache.data.payment.bonus;


import com.dgphoenix.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.frb.IFRBonusWinOperation;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.validation.constraints.PositiveOrZero;

public class FRBWinOperation implements IFRBonusWinOperation, KryoSerializable {
    private static final byte VERSION = 2;
    @PositiveOrZero
    private long id;
    @PositiveOrZero
    private long accountId;
    @PositiveOrZero
    private Long gameSessionId;
    @PositiveOrZero
    private Long roundId;
    @PositiveOrZero
    private long amount;
    @PositiveOrZero
    private long startTime;
    @PositiveOrZero
    private Long endTime;
    private FRBWinOperationStatus internalStatus;
    private FRBWinOperationStatus externalStatus;
    private String description;
    private String externalSessionId;
    private String externalTransactionId;
    private String externalRoundId;
    @PositiveOrZero
    private Long bonusId;
    private ClientType clientType;
    @PositiveOrZero
    private long betAmount;

    public FRBWinOperation() {}

    public FRBWinOperation(long frbWinOperationId, long accountId, long gameSessionId,
                           Long roundId, long amount, long betAmount, FRBWinOperationStatus externalStatus,
                           FRBWinOperationStatus internalStatus, String description, long startTime,
                           Long endTime, String externalSessionId, String externalTransactionId, String externalRoundId,
                           Long bonusId) {
        this.id = frbWinOperationId;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.amount = amount;
        this.betAmount = betAmount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.externalStatus = externalStatus;
        this.internalStatus = internalStatus;
        this.description = description;
        this.externalSessionId = externalSessionId;
        this.externalTransactionId = externalTransactionId;
        this.externalRoundId = externalRoundId;
        this.bonusId = bonusId;
    }

    public FRBWinOperation(long id, long accountId, long gameSessionId, Long roundId,
                           long amount, long betAmount, String description, FRBWinOperationStatus externalStatus,
                           FRBWinOperationStatus internalStatus, String extSessionId, Long bonusId) {
        this.id = id;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.amount = amount;
        this.betAmount = betAmount;
        this.description = description;
        this.externalStatus = externalStatus;
        this.internalStatus = internalStatus;
        this.startTime = System.currentTimeMillis();
        this.externalSessionId = extSessionId;
        this.bonusId = bonusId;
    }

    public FRBWinOperation copy() {
        return new FRBWinOperation(id, accountId, gameSessionId, roundId, amount, betAmount, externalStatus, internalStatus,
                description, startTime, endTime, externalSessionId, externalTransactionId,
                externalRoundId, bonusId);
    }

    public void update(FRBWinOperationStatus internal, Long endTime) {
        this.internalStatus = internal;
        this.endTime = endTime;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(long betAmount) {
        this.betAmount = betAmount;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public FRBWinOperationStatus getInternalStatus() {
        return internalStatus;
    }

    @Override
    public boolean isOverdue(long time) {
        return System.currentTimeMillis() - startTime > time;
    }

    public void setInternalStatus(FRBWinOperationStatus internalStatus) {
        this.internalStatus = internalStatus;
    }

    @Override
    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    @Override
    public String getExternalRoundId() {
        return externalRoundId;
    }

    public void setExternalRoundId(String externalRoundId) {
        this.externalRoundId = externalRoundId;
    }

    @Override
    public FRBWinOperationStatus getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(FRBWinOperationStatus externalStatus) {
        this.externalStatus = externalStatus;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    @Override
    public String toString() {
        return "FRBWinOperation [" +
                "id=" + id +
                ", accountId=" + accountId +
                ", gameSessionId=" + gameSessionId +
                ", roundId=" + roundId +
                ", amount=" + amount +
                ", betAmount=" + betAmount +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", internalStatus=" + internalStatus +
                ", externalStatus=" + externalStatus +
                ", description='" + description + '\'' +
                ", externalSessionId='" + externalSessionId + '\'' +
                ", externalTransactionId='" + externalTransactionId + '\'' +
                ", externalRoundId='" + externalRoundId + '\'' +
                ", bonusId='" + bonusId + '\'' +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(accountId, true);
        output.writeLong(gameSessionId == null ? -1 : gameSessionId);
        output.writeLong(roundId == null ? -1 : roundId);
        output.writeLong(amount, true);
        output.writeLong(startTime, true);
        output.writeLong(endTime == null ? -1 : endTime);
        //migration bad data fix
        output.writeString(internalStatus == null ? FRBWinOperationStatus.STARTED.name() : internalStatus.name());
        output.writeString(externalStatus == null ? FRBWinOperationStatus.STARTED.name() : externalStatus.name());
        output.writeString(description);
        output.writeString(externalSessionId);
        output.writeString(externalTransactionId);
        output.writeString(externalRoundId);
        output.writeLong(bonusId == null ? -1 : bonusId);
        output.writeLong(clientType == null ? -1 : clientType.getId());
        output.writeLong(betAmount, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        accountId = input.readLong(true);

        long l = input.readLong();
        gameSessionId = l < 0 ? null : l;

        l = input.readLong();
        roundId = l < 0 ? null : l;

        amount = input.readLong(true);
        startTime = input.readLong(true);

        l = input.readLong();
        endTime = l < 0 ? null : l;

        String s = input.readString();
        internalStatus = StringUtils.isTrimmedEmpty(s) ? FRBWinOperationStatus.STARTED :
                FRBWinOperationStatus.valueOf(s);

        s = input.readString();
        externalStatus = StringUtils.isTrimmedEmpty(s) ? FRBWinOperationStatus.STARTED :
                FRBWinOperationStatus.valueOf(s);

        description = input.readString();
        externalSessionId = input.readString();
        externalTransactionId = input.readString();
        externalRoundId = input.readString();

        l = input.readLong();
        bonusId = l < 0 ? null : l;

        if (ver > 0) {
            long id = input.readLong();
            clientType = id == -1 ? null : ClientType.getById(id);
        }
        if (ver > 1) {
            betAmount = input.readLong(true);
        }
    }
}
