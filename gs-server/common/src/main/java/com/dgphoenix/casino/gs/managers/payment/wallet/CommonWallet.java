package com.abs.casino.gs.managers.payment.wallet;

import com.abs.casino.common.cache.data.payment.AbstractWallet;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CommonWallet extends AbstractWallet implements IWallet, KryoSerializable {
    private static final byte VERSION = 0;
    private static final Logger LOG = LogManager.getLogger(CommonWallet.class);
    private Map<Integer, CommonGameWallet> gameWalletsMap = new HashMap<>(2);
    private long serverBalance;

    public CommonWallet() {
    }

    public CommonWallet(long accountId) {
        super(accountId);
    }

    @Override
    public boolean isHasAnyGameWalletWithAnyAmount() {
        for (Map.Entry<Integer, CommonGameWallet> entry : getGameWalletsMap().entrySet()) {
            CommonGameWallet commonGameWallet = entry.getValue();
            if (commonGameWallet != null && (commonGameWallet.getBetAmount() > 0 || commonGameWallet.getWinAmount() > 0)) {
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean isHasAnyGameWalletWithNotEmptyRoundId() {
        for (Map.Entry<Integer, CommonGameWallet> entry : getGameWalletsMap().entrySet()) {
            CommonGameWallet commonGameWallet = entry.getValue();
            if (commonGameWallet != null && commonGameWallet.getRoundId() != null) {
                return true;
            }

        }
        return false;
    }

    //gameId:roundId
    @Override
    public Map<Integer, Long> getUnfinishedGames() {
        Map<Integer, Long> result = new HashMap<>();
        for (Map.Entry<Integer, CommonGameWallet> entry : getGameWalletsMap().entrySet()) {
            CommonGameWallet commonGameWallet = entry.getValue();
            Long roundId = commonGameWallet == null ? null : commonGameWallet.getRoundId();
            if (roundId != null && !commonGameWallet.isRoundFinished()) {
                result.put(entry.getKey(), roundId);
            }

        }
        return result;
    }

    @Override
    public int getGameWalletsSize() {
        return getGameWalletsMap().size();
    }

    private Map<Integer, CommonGameWallet> getGameWalletsMap() {
        return gameWalletsMap;
    }

    @Override
    public long getServerBalance() {
        return serverBalance;
    }

    @Override
    public synchronized void setServerBalance(long serverBalance) {
        this.serverBalance = serverBalance;
    }

    public long getGameWalletNegativeBet(int gameId) throws WalletException {
        CommonGameWallet wallet = getGameWalletWithCheck(gameId);
        return wallet.getNegativeBet();
    }

    public void setGameWalletNegativeBet(int gameId, long betAmount) throws WalletException {
        CommonGameWallet wallet = getGameWalletWithCheck(gameId);
        wallet.setNegativeBet(betAmount);
    }

    @Override
    public Set<Integer> getWalletGamesIds() {
        return gameWalletsMap.keySet();
    }

    @Override
    public CommonWalletOperation getCurrentWalletOperation(int gameId) {
        return getCurrentWalletOperation(getGameWallet(gameId));
    }

    @Override
    public CommonWalletOperation getCurrentWalletOperation(CommonGameWallet commonGameWallet) {
        if (commonGameWallet == null) {
            LOG.warn("getCurrentWalletOperation: commonGameWallet is null");
            return null;
        }

        if (commonGameWallet.getBetOperation() != null) {
            LOG.debug("getCurrentWalletOperation: AccountId={} BetOperation={}",
                    commonGameWallet.getBetOperation().getAccountId(), commonGameWallet.getBetOperation());
            return commonGameWallet.getBetOperation();
        }

        if (commonGameWallet.getWinOperation() != null) {
            LOG.debug("getCurrentWalletOperation: AccountId={} WinOperation={}",
                    commonGameWallet.getWinOperation().getAccountId(), commonGameWallet.getWinOperation());
            return commonGameWallet.getWinOperation();
        }

        LOG.warn("getCurrentWalletOperation: commonGameWallet is both BetOperation and WinOperation are null" +
                ",in GameId={}, RoundId={} GameSessionId={}",
                commonGameWallet.getGameId(), commonGameWallet.getRoundId(), commonGameWallet.getGameSessionId());
        return null;
    }

    @Override
    public boolean isAnyWalletOperationExist() {
        for (CommonGameWallet wallet : getGameWalletsMap().values()) {
            if (wallet != null && (wallet.getBetOperation() != null || wallet.getWinOperation() != null)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGameWalletExist(int gameId) {
        return getGameWalletsMap().containsKey(gameId);
    }

    public void addGameWallet(CommonGameWallet gameWallet) {
        getGameWalletsMap().put(gameWallet.getGameId(), gameWallet);
    }

    public CommonGameWallet createGameWallet(int gameId, long gameSessionId) {
        CommonGameWallet gameWallet = new CommonGameWallet(gameId, gameSessionId);
        getGameWalletsMap().put(gameId, gameWallet);
        return gameWallet;
    }

    public CommonGameWallet createGameWallet(int gameId, long gameSessionId, ClientType clientType) {
        CommonGameWallet gameWallet = new CommonGameWallet(gameId, gameSessionId);
        gameWallet.setClientType(clientType);
        getGameWalletsMap().put(gameId, gameWallet);
        return gameWallet;
    }


    public CommonWalletOperation createCommonWalletOperation(long id, long accountId, long gameSessionId,
                                                             Long roundId, long amount,
                                                             WalletOperationType type, String description,
                                                             WalletOperationStatus externalStatus,
                                                             WalletOperationStatus internalStatus,
                                                             int gameId, long negativeBet,
                                                             String externalSessionId)
            throws WalletException {

        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        return gameWallet.createCommonWalletOperation(id, accountId, gameSessionId, roundId, amount, type, description,
                externalStatus, internalStatus, negativeBet, externalSessionId);
    }

    public CommonGameWallet getGameWallet(int gameId) {
        return getGameWalletsMap().get(gameId);
    }

    @Override
    public void removeGameWallet(int gameId) {
        getGameWalletsMap().remove(gameId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeGameWallet gameId:" + gameId + " removed");
        }
    }


    public CommonWalletOperation getGameWalletBetOperation(int gameId) {
        CommonGameWallet gameWallet = getGameWallet(gameId);
        if (gameWallet != null) {
            return gameWallet.getBetOperation();
        }
        return null;
    }

    public CommonWalletOperation getGameWalletWinOperation(int gameId) {
        CommonGameWallet gameWallet = getGameWallet(gameId);
        if (gameWallet != null) {
            return gameWallet.getWinOperation();
        }
        return null;
    }

    public CommonGameWallet getGameWalletWithCheck(int gameId) throws WalletException {
        CommonGameWallet gameWallet = getGameWallet(gameId);
        if (gameWallet == null) {
            throw new WalletException("CommonGameWallet is null: accountId=" + accountId + ", gameId=" + gameId);
        }
        return gameWallet;
    }

    @Override
    public void increaseWinAmount(int gameId, long winAmount) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.increaseWinAmount(winAmount);
    }

    public void increaseBetAmount(int gameId, long betAmount) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.increaseBetAmount(betAmount);
    }

    public void increaseJpContribution(int gameId, double jpContribution) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.increaseJpContribution(jpContribution);
    }

    public void decreaseJpContribution(int gameId, double jpContribution) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.decreaseJpContribution(jpContribution);
    }

    public long getGameWalletWinAmount(int gameId) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        return gameWallet.getWinAmount();
    }

    public long getGameWalletBetAmount(int gameId) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        return gameWallet.getBetAmount();
    }

    public void setGameWalletWinAmount(int gameId, long winAmount) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.setWinAmount(winAmount);
    }

    public void setGameWalletJpWin(int gameId, long jpWin) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        if (gameWallet.getJpWin() == 0 && jpWin != 0) {
            gameWallet.setJpWin(jpWin);
        }
    }

    public void clearGameWalletJpWin(int gameId) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.setJpWin(0);
    }

    public void setGameWalletBetAmount(int gameId, long betAmount) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.setBetAmount(betAmount);
    }

    public void setGameWalletJpContribution(int gameId, long jpContribution) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.setJpContribution(jpContribution);
    }

    public Long getGameWalletRoundId(int gameId) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        return gameWallet.getRoundId();
    }

    public void setGameWalletRoundId(int gameId, Long roundId) throws WalletException {
        CommonGameWallet gameWallet = getGameWalletWithCheck(gameId);
        gameWallet.setRoundId(roundId);
    }

    public void updateGameWallet(int gameId, long winAmount, long betAmount, Long roundId) {
        CommonGameWallet gameWallet = getGameWallet(gameId);
        gameWallet.update(winAmount, betAmount, roundId);
    }

    public void updateGameWallet(int gameId, long winAmount, long betAmount) {
        CommonGameWallet gameWallet = getGameWallet(gameId);
        gameWallet.update(winAmount, betAmount);
    }

    public Map<Integer, CommonGameWallet> getGameWallets() {
        return Collections.unmodifiableMap(gameWalletsMap);
    }

    public Collection<CommonGameWallet> getCommonGameWallets() {
        return gameWalletsMap.values();
    }

    @Override
    public boolean removeGameWalletSafely(int gameId) {
        CommonGameWallet gameWallet = getGameWallet(gameId);
        if (gameWallet != null && gameWallet.getRoundId() == null && gameWallet.getBetOperation() == null &&
                gameWallet.getWinOperation() == null) {
            removeGameWallet(gameId);
            return true;
        }
        return false;
    }

    public boolean hasGameWallets() {
        return !CollectionUtils.isEmpty(getGameWalletsMap());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommonWallet");
        sb.append("[super=").append(super.toString());
        sb.append(", serverBalance=").append(serverBalance);
        sb.append(", gameWallets=").append(printGameWallets(getGameWalletsMap()));
        sb.append(']');
        return sb.toString();
    }

    private String printGameWallets(Map<Integer, CommonGameWallet> gameWallets) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, CommonGameWallet> entry : gameWallets.entrySet()) {
            sb.append(entry.getValue()).append(" ");
        }
        return sb.toString();
    }

    public void setGameWalletLastNegativeBetAmount(int gameId, Long betAmount) throws WalletException {
        CommonGameWallet wallet = getGameWalletWithCheck(gameId);
        wallet.setLastNegativeBet(betAmount);
    }

    public Long getGameWalletLastNegativeBetAmount(int gameId) {
        CommonGameWallet wallet = getGameWallet(gameId);
        return wallet == null ? null : wallet.getLastNegativeBet();
    }

    public boolean isNewRound(int gameId) throws WalletException {
        return getGameWalletWithCheck(gameId).isNewRound();
    }

    public void setNewRound(int gameId, boolean flag) throws WalletException {
        getGameWalletWithCheck(gameId).setNewRound(flag);
    }

    public boolean isGameWalletRoundFinished(int gameId) throws WalletException {
        return getGameWalletWithCheck(gameId).isRoundFinished();
    }

    public void setGameWalletRoundFinished(int gameId, boolean flag) throws WalletException {
        final CommonGameWallet commonGameWallet = getGameWalletWithCheck(gameId);
        commonGameWallet.setRoundFinished(flag);
        if (flag) {
            //commonGameWallet.setNewRound(true);
        }
    }

    public Long getGameWalletGameSessionId(int gameId) {
        CommonGameWallet wallet = getGameWallet(gameId);
        return wallet == null ? null : wallet.getGameSessionId();
    }

    public void setGameWalletGameSessionId(int gameId, Long gameSessionId) {
        CommonGameWallet wallet = getGameWallet(gameId);
        if (wallet != null) {
            wallet.setGameSessionId(gameSessionId);
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeLong(serverBalance, true);
        kryo.writeClassAndObject(output, gameWalletsMap);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        final byte ver = input.readByte();
        accountId = input.readLong(true);
        serverBalance = input.readLong(true);
        //ugly fast serialization fix
        Map<Object, Object> tmpGameWalletsMap = (Map) kryo.readClassAndObject(input);
        for (Map.Entry<Object, Object> entry : tmpGameWalletsMap.entrySet()) {
            try {
                Object gameIdKey = entry.getKey();
                Object commonGameWalletValue = entry.getValue();
                //good case
                if (gameIdKey instanceof Integer && commonGameWalletValue instanceof CommonGameWallet) {
                    CommonGameWallet gameWallet = (CommonGameWallet) commonGameWalletValue;
                    gameWalletsMap.put(gameWallet.getGameId(), gameWallet);
                    continue;
                } else {
                    LOG.error("read: bad gameWalletsMap entry, key={}, value={}", gameIdKey, commonGameWalletValue);
                }
                if (gameIdKey instanceof CommonGameWallet) {
                    CommonGameWallet cgw = (CommonGameWallet) gameIdKey;
                    gameWalletsMap.put(cgw.getGameId(), cgw);
                } else if (commonGameWalletValue instanceof CommonGameWallet) {
                    CommonGameWallet cgw = (CommonGameWallet) commonGameWalletValue;
                    gameWalletsMap.put(cgw.getGameId(), cgw);
                }
            } catch (Exception e) {
                LOG.error("read: error", e);
            }
        }

    }
}
