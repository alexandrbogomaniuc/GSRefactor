package com.dgphoenix.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.promo.PromoWinInfo;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * User: flsh
 * Date: 6/28/12
 */
public class CommonGameWallet implements KryoSerializable {
    private static final Logger LOG = LogManager.getLogger(CommonGameWallet.class);

    public static final String PROP_ORIGINAL_WIN = "originalWin";
    public static final String PROP_DOUBLE_UP_COUNT = "doubleUpCount";

    //version 4 must be skipped, next version = 5
    private static final byte VERSION = 3;
    private static final String PROPERTY_SEPARATOR = ";";
    private static final String VALUE_SEPARATOR = "=";

    private int gameId;
    private Long roundId;
    private Long gameSessionId;
    private long winAmount;
    private long betAmount;
    private Long lastNegativeBet;
    private long negativeBet;
    private boolean newRound = true;
    private boolean roundFinished;
    private String tempToken;
    private ClientType clientType;
    private CommonWalletOperation winOperation;
    private CommonWalletOperation betOperation;
    private String additionalRoundInfo;
    private double jpContribution;
    private long jpWin;

    public CommonGameWallet() {
    }

    public CommonGameWallet(CommonGameWallet other) {
        this.gameId = other.gameId;
        this.roundId = other.roundId;
        this.gameSessionId = other.gameSessionId;
        this.winAmount = other.winAmount;
        this.betAmount = other.betAmount;
        this.lastNegativeBet = other.lastNegativeBet;
        this.negativeBet = other.negativeBet;
        this.newRound = other.newRound;
        this.roundFinished = other.roundFinished;
        this.tempToken = other.tempToken;
        this.clientType = other.clientType;
        if (other.winOperation != null) {
            this.winOperation = other.winOperation.copy();
        }
        if (other.betOperation != null) {
            this.betOperation = other.betOperation.copy();
        }
        this.additionalRoundInfo = other.additionalRoundInfo;
        this.jpContribution = other.jpContribution;
        this.jpWin = other.jpWin;
    }

    public CommonGameWallet(int gameId, long gameSessionId) {
        this.gameId = gameId;
        this.gameSessionId = gameSessionId;
        this.winAmount = 0L;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public CommonWalletOperation getBetOperation() {
        if (betOperation != null && betOperation.getId() != -1) {
            return betOperation;
        }
        return null;
    }

    public CommonWalletOperation getWinOperation() {
        if (winOperation != null && winOperation.getId() != -1) {
            return winOperation;
        }
        return null;
    }

    public void setWinOperation(CommonWalletOperation winOperation) {
        this.winOperation = winOperation;
    }

    public void setBetOperation(CommonWalletOperation betOperation) {
        this.betOperation = betOperation;
    }

    public CommonWalletOperation createCommonWalletOperation(long id, long accountId,
                                                             long gameSessionId, Long roundId,
                                                             long amount,
                                                             WalletOperationType type,
                                                             String description,
                                                             WalletOperationStatus externalStatus,
                                                             WalletOperationStatus internalStatus,
                                                             long negativeBet,
                                                             String externalSessionId)
            throws WalletException {
        CommonWalletOperation operation = WalletOperationType.DEBIT.equals(type) ? getBetOperation() : getWinOperation();
        if (operation != null && operation.getId() != -1) {
            LOG.debug("previous wallet operation:" + operation + " new operation:" + id);
            throw new WalletException("wallet has incomplete operation");
        }
        operation = new CommonWalletOperation(id, accountId, gameSessionId, roundId, amount,
                type, description, externalStatus, internalStatus, negativeBet);
        operation.setExternalSessionId(externalSessionId);
        setOperation(operation);
        return operation;
    }

    public void setOperation(CommonWalletOperation operation) {
        if (WalletOperationType.DEBIT.equals(operation.getType())) {
            betOperation = operation;
        } else {
            winOperation = operation;
        }
    }

    public void resetOperation(CommonWalletOperation operation) {
        if (WalletOperationType.DEBIT.equals(operation.getType())) {
            betOperation = null;
        } else {
            winOperation = null;
        }
    }

    public void resetBetAndWinOperations() {
        betOperation = null;
        winOperation = null;
    }

    public void resetBetOperation() {
        betOperation = null;
    }

    public void resetWinOperation() {
        winOperation = null;
    }

    public long getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    public long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(long betAmount) {
        this.betAmount = betAmount;
    }

    public void increaseWinAmount(long amount) {
        this.winAmount += amount;
    }

    public void increaseBetAmount(long amount) {
        this.betAmount += amount;
    }

    public Long getLastNegativeBet() {
        return lastNegativeBet;
    }

    public void setLastNegativeBet(Long lastNegativeBet) {
        this.lastNegativeBet = lastNegativeBet;
    }

    public long getNegativeBet() {
        return negativeBet;
    }

    public void setNegativeBet(long negativeBet) {
        this.negativeBet = negativeBet;
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

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public void update(long winAmount, long betAmount, Long roundId) {
        this.winAmount = winAmount;
        this.betAmount = betAmount;
        this.roundId = roundId;
    }

    public void update(long winAmount, long betAmount) {
        this.winAmount = winAmount;
        this.betAmount = betAmount;
    }

    public String getTempToken() {
        return tempToken;
    }

    public void setTempToken(String tempToken) {
        this.tempToken = tempToken;
    }

    public String getAdditionalRoundInfo() {
        return additionalRoundInfo;
    }

    public String getAdditionalRoundInfo(final String propertyName) {
        if (isBlank(additionalRoundInfo)) {
            return null;
        }
        checkNotNull(propertyName, "Parameter name of additional round info must be not null");
        Map<String, String> properties = CollectionUtils.stringToMap(additionalRoundInfo);
        return properties.get(propertyName);
    }

    public void clearAdditionalRoundInfo() {
        additionalRoundInfo = "";
    }

    public void addAdditionalRoundInfoProperty(String name, String value) {
        additionalRoundInfo = CollectionUtils
                .modifyStringProperty(additionalRoundInfo, PROPERTY_SEPARATOR, VALUE_SEPARATOR)
                .add(name, value)
                .getString();
    }

    public void removeAdditionalRoundInfoProperty(String name) {
        additionalRoundInfo = CollectionUtils
                .modifyStringProperty(additionalRoundInfo, PROPERTY_SEPARATOR, VALUE_SEPARATOR)
                .remove(name)
                .getString();
    }

    public void setAdditionalRoundInfo(String additionalRoundInfo) {
        this.additionalRoundInfo = additionalRoundInfo;
    }

    public double getJpContribution() {
        return jpContribution;
    }

    public void setJpContribution(double jpContribution) {
        this.jpContribution = jpContribution;
    }

    public void increaseJpContribution(double contribution) {
        this.jpContribution += contribution;
    }

    public void decreaseJpContribution(double contribution) {
        this.jpContribution -= contribution;
    }

    public long getJpWin() {
        return jpWin;
    }

    public void setJpWin(long jpWin) {
        this.jpWin = jpWin;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommonGameWallet");
        sb.append("[gameId=").append(gameId);
        sb.append(", roundId=").append(roundId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", winAmount=").append(winAmount);
        sb.append(", betAmount=").append(betAmount);
        sb.append(", lastNegativeBet=").append(lastNegativeBet);
        sb.append(", negativeBet=").append(negativeBet);
        sb.append(", newRound=").append(newRound);
        sb.append(", roundFinished=").append(roundFinished);
        sb.append(", tempToken=").append(tempToken);
        sb.append(", clientType=").append(clientType);
        sb.append(", betOperation=").append(betOperation);
        sb.append(", winOperation=").append(winOperation);
        sb.append(", additionalRoundInfo=").append(additionalRoundInfo);
        sb.append(", jpContribution=").append(jpContribution);
        sb.append(", jpWin=").append(jpWin);
        //sb.append(", promoWinInfo=").append(promoWinInfo);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(gameId, true);
        kryo.writeObjectOrNull(output, roundId, Long.class);
        kryo.writeObjectOrNull(output, gameSessionId, Long.class);
        output.writeLong(winAmount);
        output.writeLong(betAmount);
        kryo.writeObjectOrNull(output, lastNegativeBet, Long.class);
        output.writeLong(negativeBet);
        output.writeBoolean(newRound);
        output.writeBoolean(roundFinished);
        kryo.writeObjectOrNull(output, clientType, ClientType.class);
        kryo.writeObjectOrNull(output, winOperation, CommonWalletOperation.class);
        kryo.writeObjectOrNull(output, betOperation, CommonWalletOperation.class);
        kryo.writeObjectOrNull(output, tempToken, String.class);
        output.writeString(additionalRoundInfo);
        output.writeDouble(jpContribution);
        output.writeLong(jpWin, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        final byte ver = input.readByte();
        gameId = input.readInt(true);
        roundId = kryo.readObjectOrNull(input, Long.class);
        gameSessionId = kryo.readObjectOrNull(input, Long.class);
        winAmount = input.readLong();
        betAmount = input.readLong();
        lastNegativeBet = kryo.readObjectOrNull(input, Long.class);
        negativeBet = input.readLong();
        newRound = input.readBoolean();
        roundFinished = input.readBoolean();
        clientType = kryo.readObjectOrNull(input, ClientType.class);
        winOperation = kryo.readObjectOrNull(input, CommonWalletOperation.class);
        betOperation = kryo.readObjectOrNull(input, CommonWalletOperation.class);
        if (ver >= 1) {
            tempToken = kryo.readObjectOrNull(input, String.class);
        }
        if (ver >= 2) {
            additionalRoundInfo = input.readString();
        }
        if (ver >= 3) {
            jpContribution = input.readDouble();
            jpWin = input.readLong(true);
        }
        if (ver == 4) { //temporary deserialization fix
            kryo.readObjectOrNull(input, PromoWinInfo.class);
        }
    }
}


