package com.dgphoenix.casino.common.cache.data.payment.bonus;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWin;
import com.dgphoenix.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FRBonusWin implements IDistributedCacheEntry, IFRBonusWin, KryoSerializable {
    private static final byte VERSION = 0;
    private static final Logger LOG = LogManager.getLogger(FRBonusWin.class);
    private Map<String, CommonFRBonusWin> frbonusWinsMap = new HashMap<>();
    private long serverBalance;
    private long accountId;

    public FRBonusWin() {}

    public FRBonusWin(long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public FRBonusWin copy(IFRBonusWin source) {
        FRBonusWin copy = new FRBonusWin(source.getAccountId());
        for (Map.Entry<String, CommonFRBonusWin> entry : ((FRBonusWin) source).getFrBonusWinMap().entrySet()) {
            CommonFRBonusWin commonFRBonusWin = entry.getValue();
            CommonFRBonusWin commonFRBonusWinCopy = copy.createFRBonusWin(commonFRBonusWin.getGameId(),
                    commonFRBonusWin.getGameSessionId() == null ? 0 : commonFRBonusWin.getGameSessionId());
            commonFRBonusWinCopy.setRoundId(commonFRBonusWin.getRoundId());
            commonFRBonusWinCopy.setWinAmount(commonFRBonusWin.getWinAmount());
            commonFRBonusWinCopy.setRoundFinished(commonFRBonusWin.isRoundFinished());
            if (commonFRBonusWin.getOperation() != null) {
                commonFRBonusWinCopy.setOperation(commonFRBonusWin.getOperation().copy());
            }
        }
        return copy;
    }


    @Override
    public boolean isHasAnyFRBonusWinWithAnyAmount() {
        for (Map.Entry<String, CommonFRBonusWin> entry : getFrBonusWinMap().entrySet()) {
            CommonFRBonusWin commonFRBonusWin = entry.getValue();
            if (commonFRBonusWin.getWinAmount() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FRBWinOperation getCurrentFRBonusWinOperation(Long gameId) {
        return getGameFRBWinOperation(gameId);
    }

    @Override
    public boolean isAnyFRBWinOperationExist() {
        for (CommonFRBonusWin commonFRBonusWin : getFrBonusWinMap().values()) {
            if (commonFRBonusWin.getOperation() != null) {
                return true;
            }
        }
        return false;
    }

    public void updateGameFRBonusWin(long gameId, long winAmount, Long roundId) {
        CommonFRBonusWin frBonusWin = getFRBWin(gameId);
        frBonusWin.update(winAmount, roundId);
    }

    public void updateGameFRBonusWin(long gameId, long winAmount) {
        CommonFRBonusWin frBonusWin = getFRBWin(gameId);
        frBonusWin.update(winAmount);
    }

    public FRBWinOperation getGameFRBWinOperation(long gameId) {
        CommonFRBonusWin commonFRBonusWin = getFRBWin(gameId);
        if (commonFRBonusWin != null) {
            return commonFRBonusWin.getOperation();
        }
        return null;
    }

    public Long getFRBonusWinGameSessionId(long gameId) {
        CommonFRBonusWin frbWin = getFRBWin(gameId);
        return frbWin == null ? null : frbWin.getGameSessionId();
    }

    private Map<String, CommonFRBonusWin> getFrBonusWinMap() {
        return frbonusWinsMap;
    }

    public void increaseWinAmount(long gameId, long winAmount) throws FRBException {
        CommonFRBonusWin frBonusWin = getFRBWinWithCheck(gameId);
        frBonusWin.increaseWinAmount(winAmount);
    }

    public boolean isGameFRBWinRoundFinished(long gameId) {
        return true;
    }

    public boolean isNewRound(long gameId) throws FRBException {
        return getFRBWinWithCheck(gameId).isNewRound();
    }

    public boolean isRoundFinished(long gameId) throws FRBException {
        return getFRBWinWithCheck(gameId).isRoundFinished();
    }

    public void setNewRound(long gameId, boolean newRound) throws FRBException {
        getFRBWinWithCheck(gameId).setNewRound(newRound);
    }

    public void setGameFRBWinRoundFinished(long gameId, boolean flag) throws FRBException {
        CommonFRBonusWin frBonusWin = getFRBWinWithCheck(gameId);
        frBonusWin.setRoundFinished(flag);
    }

    public Long getGameFRBWinRoundId(long gameId) throws FRBException {
        CommonFRBonusWin commonFRBonusWin = getFRBWinWithCheck(gameId);
        return commonFRBonusWin.getRoundId();
    }

    public long getGameFRBonusWinWinAmount(long gameId) throws FRBException {
        CommonFRBonusWin commonFRBonusWin = getFRBWinWithCheck(gameId);
        return commonFRBonusWin.getWinAmount();
    }

    public CommonFRBonusWin createFRBonusWin(long gameId, long gameSessionId) {
        CommonFRBonusWin commonFRBonusWin = new CommonFRBonusWin(gameId, gameSessionId);
        getFrBonusWinMap().put(String.valueOf(gameId), commonFRBonusWin);
        return commonFRBonusWin;
    }

    public synchronized FRBWinOperation createFRBWinOperation(long id, long accountId, long gameSessionId, Long roundId,
                                                              long amount, long betAmount, String description,
                                                              FRBWinOperationStatus externalStatus,
                                                              FRBWinOperationStatus internalStatus, long gameId,
                                                              String extSessionId, Long bonusId) throws FRBException {
        CommonFRBonusWin commonFRBonusWin = getFRBWinWithCheck(gameId);
        return commonFRBonusWin.createCommonFRBonusWin(id, accountId, gameSessionId, roundId, amount, betAmount, description,
                externalStatus, internalStatus, extSessionId, bonusId);
    }

    public void setFRBonusWinOperation(long gameId, FRBWinOperation operation)
            throws FRBException {
        CommonFRBonusWin commonFRBonusWin = getFRBWinWithCheck(gameId);
        commonFRBonusWin.setOperation(operation);
    }

    private CommonFRBonusWin getFRBWinWithCheck(long gameId) throws FRBException {
        CommonFRBonusWin frBonusWin = getFRBWin(gameId);
        if (frBonusWin == null) {
            throw new FRBException("game frbWin is null");
        }
        return frBonusWin;
    }

    public void setGameFRBonusWinRoundId(long gameId, Long roundId) throws FRBException {
        CommonFRBonusWin commonFRBonusWin = getFRBWinWithCheck(gameId);
        commonFRBonusWin.setRoundId(roundId);
    }

    public void removeFRBonusWinSafely(long gameId) {
        CommonFRBonusWin commonFRBonusWin = getFRBWin(gameId);
        if (commonFRBonusWin != null && commonFRBonusWin.getRoundId() == null && commonFRBonusWin.getOperation() == null) {
            removeFRBonusWin(gameId);
        }
    }

    public boolean hasGameFRBonusWin() {
        return !CollectionUtils.isEmpty(getFrBonusWinMap());
    }

    public boolean hasAnyOperation() {
        for (Map.Entry<String, CommonFRBonusWin> entry : frbonusWinsMap.entrySet()) {
            CommonFRBonusWin commonFRBonusWin = entry.getValue();
            if (commonFRBonusWin != null && commonFRBonusWin.getOperation() != null) {
                return true;
            }
        }
        return false;
    }

    public void addFRBonusWin(CommonFRBonusWin сommonFRBonusWin) {
        getFrBonusWinMap().put(String.valueOf(сommonFRBonusWin.getGameId()), сommonFRBonusWin);
    }

    public void removeFRBonusWin(long gameId) {
        getFrBonusWinMap().remove(String.valueOf(gameId));
        LOG.debug("removeFRBonusWin gameId:" + gameId + " removed");
    }


    public long getServerBalance() {
        return serverBalance;
    }

    public long getServerBalanceDirty() {
        return serverBalance;
    }

    public boolean isGameFRBWinExist(long gameId) {
        return getFrBonusWinMap().containsKey(String.valueOf(gameId));
    }

    public void setServerBalance(long serverBalance) {
        this.serverBalance = serverBalance;
    }

    public Map<String, CommonFRBonusWin> getFRBonusWins() {
        return frbonusWinsMap == null ? null : new HashMap<>(frbonusWinsMap);
    }

    public FRBWinOperation getFRBonusWinOperation(long gameId) {
        CommonFRBonusWin commonFRBWin = getFRBWin(gameId);
        if (commonFRBWin != null) {
            return commonFRBWin.getOperation();
        }

        return null;
    }

    public CommonFRBonusWin getFRBWin(long gameId) {
        return getFrBonusWinMap().get(String.valueOf(gameId));
    }

    public void setFRBonusWinGameSessionId(long gameId, Long gameSessionId) {
        CommonFRBonusWin commonFRBonusWin = getFRBWin(gameId);
        if (commonFRBonusWin != null) {
            commonFRBonusWin.setGameSessionId(gameSessionId);
        }
    }

    private String printFRBWins(Map<String, CommonFRBonusWin> commonFRBonusWinMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, CommonFRBonusWin> entry : commonFRBonusWinMap.entrySet()) {
            builder.append(entry.getValue()).append("  ");
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FRBonusWin");
        sb.append("[accountId=").append(accountId);
        sb.append(", serverBalance=").append(serverBalance);
        sb.append(", frbWins=").append(printFRBWins(frbonusWinsMap));
        sb.append(']');
        return sb.toString();
    }


    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeLong(serverBalance, true);
        kryo.writeObject(output, frbonusWinsMap);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        accountId = input.readLong(true);
        serverBalance = input.readLong(true);
        frbonusWinsMap = kryo.readObject(input, HashMap.class);
    }
}
