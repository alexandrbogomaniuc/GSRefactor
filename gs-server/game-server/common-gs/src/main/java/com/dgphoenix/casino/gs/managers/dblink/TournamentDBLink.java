package com.abs.casino.gs.managers.dblink;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.account.IPlayerGameSettings;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.abs.casino.common.cache.data.bank.ICoin;
import com.abs.casino.common.cache.data.bank.ILimit;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.abs.casino.unj.api.AbstractSharedGameState;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TournamentDBLink implements IDBLink {
    private final GameSession gameSession;
    private final AccountInfo accountInfo;
    private final long bankId;
    private long lastActivity;
    private long currentBet;
    private long currentWin;
    private long winAmount;
    private Long roundId;
    private final boolean walletBank;

    public TournamentDBLink(GameSession gameSession, AccountInfo accountInfo) {
        this.gameSession = gameSession;
        this.accountInfo = accountInfo;
        this.bankId = accountInfo.getBankId();
        this.roundId = generateRoundId();
        this.walletBank = WalletProtocolFactory.getInstance().isWalletBank(accountInfo.getBankId());
    }

    @Override
    public AccountInfo getAccount() {
        return accountInfo;
    }

    @Override
    public void markEnterGame(int gameID, String gameName, int gamestate) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public void setLastSavedBet(PlayerBet bet) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public String getLogPrefix() {
        return null;
    }

    @Override
    public void setLastPaymentOperationId(Long lastPaymentOperationId) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public Currency getCurrency() {
        return null;
    }

    @Override
    public double adjustMoneyValue(double originalValue) {
        return originalValue;
    }

    @Override
    public GameSession getGameSession() {
        return gameSession;
    }

    @Override
    public GameMode getMode() {
        return GameMode.REAL;
    }

    @Override
    public void resetCurrentBetWin() {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public void updateCurrentBetWin(long betAmount, long winAmount) {
        this.currentBet += betAmount;
        this.currentWin += winAmount;
    }

    @Override
    public GameSession finishGameSession(GameSession gameSession, SessionInfo sessionInfo) {
        return null;
    }

    @Override
    public void refreshGameSettings() {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public IBaseGameInfo getGameSettings() {
        return null;
    }

    @Override
    public double getDBBonus() {
        return 0;
    }

    @Override
    public long getBalanceLong() {
        return 0;
    }

    @Override
    public void saveLasthand(String data) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public LasthandInfo getLasthandInfo(long gameID) {
        return null;
    }

    @Override
    public boolean isAuthState() {
        return false;
    }

    @Override
    public void setAuthState(boolean authState) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public IWallet getWallet() {
        if (!isWalletBank()) {
            return null;
        }
        return SessionHelper.getInstance().getTransactionData().getWallet();
    }

    @Override
    public void saveWinAmount(long winAmount) {
        setWinAmount(getWinAmount() + winAmount);
    }

    @Override
    public void interceptBet(long bet, long win) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public void interceptRoundFinished() {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public SessionInfo getSessionInfo() {
        return null;
    }

    @Override
    public long getLastActivity() {
        return lastActivity;
    }

    @Override
    public void updateLastActivity() {
        lastActivity = System.currentTimeMillis();
    }

    @Override
    public boolean isNeedUpdateLastActivity() {
        return false;
    }

    @Override
    public boolean isSendRoundId() {
        return false;
    }

    @Override
    public boolean isSendExternalWalletMessages() {
        return false;
    }

    @Override
    public boolean isCloseOldGameAfterRoundFinished() {
        return false;
    }

    @Override
    public int getTimeZoneOffset() {
        return 0;
    }

    @Override
    public boolean isSaveGameSidByRound() {
        return false;
    }

    @Override
    public boolean isSaveShortBetInfo() {
        return false;
    }

    @Override
    public Long getLastCheckTime() {
        return null;
    }

    @Override
    public void setLastCheckTime(Long lastCheckTime) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public boolean isNotFixAnyChanges() {
        return false;
    }

    @Override
    public long generateRoundId() {
        return IdGenerator.getInstance().getNext(IWallet.class);
    }

    @Override
    public void saveGameSessionRealityCheckParams() {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public boolean isBonusInstantLostOnThreshold() {
        return false;
    }

    @Override
    public long getAccountId() {
        return 0;
    }

    @Override
    public int getActiveGameID() {
        return 0;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public boolean isUsePlayerGameSettings() {
        return false;
    }

    @Override
    public IPlayerGameSettings getPlayerGameSettings() {
        return null;
    }

    @Override
    public long getGameId() {
        return 1;
    }

    @Override
    public String getGameName() {
        return null;
    }

    @Override
    public Long getRoomId() {
        return null;
    }

    @Override
    public ICurrency getActiveCurrency() {
        return null;
    }

    @Override
    public boolean isWalletBank() {
        return walletBank;
    }

    @Override
    public long getGameSessionId() {
        return gameSession.getId();
    }

    @Override
    public String getNickName() {
        return null;
    }

    @Override
    public Long getRoundId() {
        return roundId;
    }

    @Override
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    @Override
    public long getWinAmount() {
        return winAmount;
    }

    @Override
    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    @Override
    public long getCurrentBet() {
        return currentBet;
    }

    @Override
    public long getCurrentWin() {
        return currentWin;
    }

    @Override
    public boolean isForReal() {
        return false;
    }

    @Override
    public double getBalance() {
        return 0;
    }

    @Override
    public boolean isLimitsChanged() {
        return false;
    }

    @Override
    public void setLimitsChanged(boolean limitsChanged) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public Map<String, String> getLastHandLimits() {
        return Collections.emptyMap();
    }

    @Override
    public void setLastHandLimit(String key, String value) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public void resetLasthandLimits() {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public void setLasthand(String data) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public String getLasthand() {
        return null;
    }

    @Override
    public boolean getLastHand(Map publicLastHand, Map privateLastHand, Map autoPublic, Map autoPrivate) {
        return false;
    }

    @Override
    public boolean getLastHand(int gameID, Map publicLastHand, Map privateLastHand, Map autoPublic, Map autoPrivate) {
        return false;
    }

    @Override
    public String getLasthand(long gameID) {
        return null;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public void setRoundFinished() {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public boolean isFRBGame() {
        return false;
    }

    @Override
    public void incrementBalance(long bet, long win) throws CommonException {
        getAccount().incrementBalance(bet, win, false);
    }

    @Override
    public Double getMinBet() {
        return null;
    }

    @Override
    public Double getConvertedMinBet() {
        return null;
    }

    @Override
    public void setMinBet(Double minBet) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public Double getMaxBet() {
        return null;
    }

    @Override
    public Double getConvertedMaxBet() {
        return null;
    }

    @Override
    public double getPayoutPercent() {
        return 0;
    }

    @Override
    public void setMaxBet(Double maxBet) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public double[] getCOINSEQ() {
        return new double[0];
    }

    @Override
    public double[] getConvertedCOINSEQ() {
        return new double[0];
    }

    @Override
    public void setCOINSEQ(double[] COINSEQ) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public boolean isBonusGameSession() {
        return false;
    }

    @Override
    public String getDefaultBetPerLineNotFRB() {
        return null;
    }

    @Override
    public String getDefaultNumLinesNotFRB() {
        return null;
    }

    @Override
    public String getCurrentDefaultBetPerLine() {
        return null;
    }

    @Override
    public String getCurrentDefaultNumLines() {
        return null;
    }

    @Override
    public String getFRBDefaultBetPerLine() {
        return null;
    }

    @Override
    public String getFRBDefaultNumLines() {
        return null;
    }

    @Override
    public String getFRBCoin() {
        return null;
    }

    @Override
    public Integer getDefaultCoin() {
        return null;
    }

    @Override
    public String getChipValues() {
        return null;
    }

    @Override
    public List<? extends ICoin> getCoins() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends ICoin> getCoins(boolean withCache) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUseDynamicLevels() {
        return false;
    }

    @Override
    public ILimit getLimit() {
        return null;
    }

    @Override
    public String getGameSettingsProperty(String key) {
        return null;
    }

    @Override
    public int getSubCasinoId() {
        return 0;
    }

    @Override
    public boolean isMaxBet(long betsCount) {
        return false;
    }

    @Override
    public AbstractSharedGameState getSharedGameState(String unjExtraId) {
        return null;
    }

    @Override
    public void updateSharedGameState(String unjExtraId, AbstractSharedGameState state) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public boolean isGameLogEnabled() {
        return false;
    }

    @Override
    public boolean isLogoutOnError() {
        return false;
    }

    @Override
    public Long getWalletTransactionId() {
        return null;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return Collections.emptyMap();
    }

    @Override
    public void setRequestParameters(Map<String, String[]> parameters) {
        //nop, because this TournamentDBLink is fake
    }

    @Override
    public String getRequestParameterValue(String parameterName) {
        return null;
    }

    @Override
    public String getLasthandParameter(String parameterName) {
        return null;
    }
}
