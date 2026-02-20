package com.dgphoenix.casino.common.cache.data.payment.bonus;

import com.dgphoenix.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.dgphoenix.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.PositiveOrZero;

public class CommonFRBonusWin implements KryoSerializable {
    private static final byte VERSION = 1;
    private static final Logger LOG = LogManager.getLogger(CommonFRBonusWin.class);
    private static final String PROPERTY_SEPARATOR = ";";
    private static final String VALUE_SEPARATOR = "=";

    private long gameId;
    @PositiveOrZero
    private Long roundId;
    @PositiveOrZero
    private Long gameSessionId;
    private long winAmount;
    private boolean newRound = true;
    private boolean roundFinished;
    private FRBWinOperation operation;
    private String additionalRoundInfo;

    public CommonFRBonusWin() {}

    public CommonFRBonusWin(long gameId, long gameSessionId) {
        this.gameId = gameId;
        this.gameSessionId = gameSessionId;
        this.winAmount = 0L;
    }

    public CommonFRBonusWin copy() {
        CommonFRBonusWin copy = new CommonFRBonusWin(gameId, gameSessionId == null ? 0 : gameSessionId);
        copy.setRoundId(roundId);
        copy.setWinAmount(winAmount);
        copy.setRoundFinished(roundFinished);
        if (operation != null) {
            copy.setOperation(operation.copy());
        }
        copy.setAdditionalRoundInfo(additionalRoundInfo);
        return copy;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public FRBWinOperation getOperation() {
        if (operation != null && operation.getId() > 0) {
            return operation;
        }
        return null;
    }

    public FRBWinOperation createCommonFRBonusWin(long id, long accountId, long gameSessionId, Long roundId, long amount, long betAmount,
                                                  String description, FRBWinOperationStatus externalStatus, FRBWinOperationStatus internalStatus,
                                                  String extSessionId, Long bonusId) throws FRBException {

        if (operation != null && operation.getId() > 0) {
            LOG.debug("previous frbWin operation:" + operation + " new operation:" + id);
            throw new FRBException("frbWin has incomplete operation");
        }
        operation = new FRBWinOperation(id, accountId, gameSessionId, roundId, amount, betAmount, description, externalStatus,
                internalStatus, extSessionId, bonusId);
        return operation;
    }

    public void setOperation(FRBWinOperation operation) {
        this.operation = operation;
    }

    public long getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    public void increaseWinAmount(long amount) {
        this.winAmount += amount;
    }

    public boolean isRoundFinished() {
        return roundFinished;
    }

    public void setRoundFinished(boolean roundFinished) {
        this.roundFinished = roundFinished;
    }

    public boolean isNewRound() {
        return newRound;
    }

    public void setNewRound(boolean newRound) {
        this.newRound = newRound;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void update(long winAmount, Long roundId) {
        this.winAmount = winAmount;
        this.roundId = roundId;
    }

    public void update(long winAmount) {
        this.winAmount = winAmount;
    }

    public String getAdditionalRoundInfo() {
        return additionalRoundInfo;
    }

    public void setAdditionalRoundInfo(String additionalRoundInfo) {
        this.additionalRoundInfo = additionalRoundInfo;
    }

    public void addAdditionalRoundInfoProperty(String name, String value) {
        additionalRoundInfo = CollectionUtils
                .modifyStringProperty(additionalRoundInfo, PROPERTY_SEPARATOR, VALUE_SEPARATOR)
                .add(name, value)
                .getString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommonFRBonusWin");
        sb.append("[gameId=").append(gameId);
        sb.append(", roundId=").append(roundId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", winAmount=").append(winAmount);
        sb.append(", newRound=").append(newRound);
        sb.append(", roundFinished=").append(roundFinished);
        sb.append(", operation=").append(operation);
        sb.append(", additionalRoundInfo=").append(additionalRoundInfo);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(gameId, true);
        output.writeLong(roundId == null ? -1L : roundId);
        output.writeLong(gameSessionId == null ? -1L : gameSessionId);
        output.writeLong(winAmount, true);
        output.writeBoolean(newRound);
        output.writeBoolean(roundFinished);
        kryo.writeObjectOrNull(output, operation, FRBWinOperation.class);
        output.writeString(additionalRoundInfo);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        gameId = input.readLong(true);
        long l = input.readLong();
        roundId = l < 0 ? null : l;
        l = input.readLong();
        gameSessionId = l < 0 ? null : l;
        winAmount = input.readLong(true);
        newRound = input.readBoolean();
        roundFinished = input.readBoolean();
        operation = kryo.readObjectOrNull(input, FRBWinOperation.class);
        if (ver > 0) {
            additionalRoundInfo = input.readString();
        }
    }
}


