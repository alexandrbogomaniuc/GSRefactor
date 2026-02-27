package com.abs.casino.gs.managers.dblink;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraFRBonusWinPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWin;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.persistance.LasthandPersister;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.abs.casino.gs.singlegames.tools.cbservtools.CBGameException;

import java.util.HashMap;
import java.util.Map;

public class FRBonusDBLink extends DBLink {

    private Long bonusId;
    private final Long frbCoinValue;

    public FRBonusDBLink(long accountId, String nickName, long bankId, long gameId, String gameName,
                         boolean isJackpotGame, GameSession gameSession, Currency currency, Long bonusId) throws CommonException {
        super(accountId, nickName, bankId, gameId, gameName, isJackpotGame, gameSession, currency);
        this.bonusId = bonusId;
        setRoundId(getRoundIdFromLastHand());
        try {
            FRBonus frBonus = FRBonusManager.getInstance().getById(bonusId);
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            if (frBonus == null) {
                FRBonus archivedFRBonus = FRBonusManager.getInstance().getArchivedFRBonusById(bonusId);
                if (archivedFRBonus == null) {
                    throw new CommonException("FRBonus not found: id=" + bonusId);
                }
                if (BonusStatus.ACTIVE.equals(archivedFRBonus.getStatus())) {
                    logError("FRBonusDBLink: Strange error, active FRBonus in archive=" + archivedFRBonus);
                    throw new CommonException("Strange error, active FRBonus in archive, " +
                            "FRBonus not found: id=" + bonusId);
                }
                frBonus = archivedFRBonus;
            } else {
                transactionData.setFrBonus(frBonus);
            }

            this.frbCoinValue = frBonus.getCoinValue();

            CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            CassandraFRBonusWinPersister frBonusWinPersister = persistenceManager.getPersister(CassandraFRBonusWinPersister.class);

            FRBonusWin frbWin = transactionData.getFrbWin();
            if (frbWin == null) {
                frbWin = frBonusWinPersister.get(accountId);
                logDebug("loaded frbWin=" + frbWin);
                transactionData.setFrbWin(frbWin);
            }
            gameSession.setFrbonusStatus(getBonus().getStatus());
        } catch (Exception e) {
            logError("FRBonusDBLink init error:", e);
            throw new CommonException(e);
        }
    }

    public FRBonusDBLink(AccountInfo accountInfo, long gameId, Long gameSessionId, Long bonusId, SessionInfo sessionInfo,
                         IBaseGameInfo<?, ?> gameInfo, String lang) throws CommonException {
        super(accountInfo, gameId, gameSessionId, sessionInfo, gameInfo, lang);
        this.bonusId = bonusId;
        setRoundId(getRoundIdFromLastHand());
        try {
            FRBonus frBonus = FRBonusManager.getInstance().getById(bonusId);
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            if (frBonus == null) {
                FRBonus archivedFRBonus = FRBonusManager.getInstance().getArchivedFRBonusById(bonusId);
                if (archivedFRBonus == null) {
                    throw new CommonException("FRBonus not found: id=" + bonusId);
                }
                if (BonusStatus.ACTIVE.equals(archivedFRBonus.getStatus())) {
                    logError("FRBonusDBLink: Strange error, active FRBonus in archive=" + archivedFRBonus);
                    throw new CommonException("Strange error, active FRBonus in archive, " +
                            "FRBonus not found: id=" + bonusId);
                }
                frBonus = archivedFRBonus;
            } else {
                transactionData.setFrBonus(frBonus);
            }

            this.frbCoinValue = frBonus.getCoinValue();

            CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            CassandraFRBonusWinPersister frBonusWinPersister = persistenceManager.getPersister(CassandraFRBonusWinPersister.class);

            FRBonusWin frbWin = transactionData.getFrbWin();
            if (frbWin == null) {
                frbWin = frBonusWinPersister.get(accountId);
                logDebug("loaded frbWin=" + frbWin);
                transactionData.setFrbWin(frbWin);
            }
            GameSession gameSession = getGameSession();
            gameSession.setFrbonusId(bonusId);
            gameSession.setFrbonusStatus(getBonus().getStatus());
        } catch (Exception e) {
            logError("FRBonusDBLink init error:", e);
            throw new CommonException(e);
        }

    }

    public boolean isBetRangeLimitedServlet() {
        return false;
    }

    Long getFrbChips() throws CBGameException {
        Map lasthandPublic = new HashMap(), lasthandPrivate = new HashMap(), l3 = new HashMap(), l4 = new HashMap();

        getLastHand((int) getGameId(), lasthandPublic, lasthandPrivate, l3, l4);
        String strFrbChips = (String) lasthandPrivate.get("FRBCHIPS");

        if (strFrbChips != null) {
            return Long.parseLong(strFrbChips);
        } else {
            FRBonus frBonus = FRBonusManager.getInstance().getById(getBonusId());
            // if FRB bonus not finished new balance should be equal to initial chips
            return frBonus.getFrbTableRoundChips();
        }
    }

    public long getBalanceLong() {
        try {
            if (isBetRangeLimitedServlet()) {
                LasthandInfo lasthandInfo = getLasthandInfo(getGameId());
                boolean isLasthand = lasthandInfo != null && !StringUtils.isTrimmedEmpty(lasthandInfo.getLasthandData());
                boolean isEndOfFRBTableGames = !isLasthand && (getRoundsLeft() == 0); // && getGameController().isRoundFinished(); // isRoundFinished works weird
                long newBalance = 0;
                if (!isEndOfFRBTableGames) { // if has lasthand or FRB not finished
                    newBalance = getFrbChips();
                } else {
                    long frbTableRoundChips = getBonus().getFrbTableRoundChips(); // Initial chips for FRB round

                    // FRB bonus is finished, we should show current balance, but not initial chips or real balance.
                    PlayerBetPersistenceManager playerBetPersistenceManager = ApplicationContextHelper.getApplicationContext()
                            .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
                    PlayerBet currentBet = playerBetPersistenceManager.getCurrentBet(getGameSession()); // We need full round bet
                    long betAmountInFRBTable = currentBet.getBet(); // Get full round bet in cents
                    newBalance = frbTableRoundChips - betAmountInFRBTable;
                }
                return newBalance;
            }
        } catch (Exception ex) {
            logError("ERROR::getBalanceLong()", ex);
        }

        return super.getBalanceLong();
    }

    @Override
    protected void saveLastHandOnClose(LasthandInfo lasthand) {
        LasthandPersister.getInstance().saveOnClose(accountId, gameId, bonusId, BonusSystemType.FRB_SYSTEM, lasthand);
    }

    public FRBonus getBonus() {
        return SessionHelper.getInstance().getTransactionData().getFrBonus();
    }

    public long getRoundsLeft() throws BonusException {
        FRBonus frBonus = getBonus();
        if (frBonus == null) {
            throw new BonusException("No FRBonus !!!");
        } else {
            return frBonus.getRoundsLeft();
        }
    }

    public long getRounds() throws BonusException {
        FRBonus frBonus = getBonus();
        if (frBonus == null) {
            throw new BonusException("No FRBonus !!!");
        } else {
            return frBonus.getRounds();
        }
    }

    public IFRBonusWin getFrbonusWin() {
        return SessionHelper.getInstance().getTransactionData().getFrbWin();
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    public GameMode getMode() {
        return GameMode.REAL;
    }

    /**
     * SETBETS
     */
    @Override
    public void incrementBalance(long bet, long win) throws CommonException {
        FRBonus bonus = getBonus();
        if (bet < 0) {
            bonus.incrementBetSum(-bet);
        }
        bonus.incrementWinSum(win);
        AccountInfo accountInfo = getAccount();
        accountInfo.incrementBalance(0, win, false);
    }

    @Override
    public void setRoundFinished() throws CommonException {
        super.setRoundFinished();
    }

    @Override
    public String getLogPrefix() {
        return "GS:FRBonusDBLink [accountId=" + accountId + ", nickName=" + nickName + ", gameId=" + gameId
                + ", gameSessionId=" + gameSessionId + ", bonusId=" + bonusId + "] ";
    }

    @Override
    public boolean isFRBGame() {
        return true;
    }

    @Override
    public String getFRBCoin() {
        if (frbCoinValue == null) {
            return super.getFRBCoin();
        } else {
            return String.valueOf(frbCoinValue);
        }
    }

    @Override
    public String getCurrentDefaultBetPerLine() {
        String bpl = getFRBDefaultBetPerLine();
        return !StringUtils.isTrimmedEmpty(bpl) ? bpl : super.getCurrentDefaultBetPerLine();
    }

    @Override
    public String getCurrentDefaultNumLines() {
        String numLines = getFRBDefaultNumLines();
        return !StringUtils.isTrimmedEmpty(numLines) ? numLines : super.getCurrentDefaultNumLines();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FRBonusDBLink");
        sb.append("{accountId=").append(accountId);
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", bonusId=").append(bonusId);
        sb.append(", bankId=").append(getBankId());
        sb.append(", gameId=").append(gameId);
        sb.append(", gameName='").append(gameName).append('\'');
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", minBet=").append(getMinBet());
        sb.append(", maxBet=").append(getMaxBet());
        sb.append(", COINSEQ=").append(getCOINSEQ() == null ? "null" : "");
        for (int i = 0; getCOINSEQ() != null && i < getCOINSEQ().length; ++i)
            sb.append(i == 0 ? "" : ", ").append(getCOINSEQ()[i]);
        sb.append(", limitsChanged=").append(isLimitsChanged());
        sb.append(", roundId=").append(getRoundId());
        sb.append(", winAmount=").append(getWinAmount());
        sb.append('}');
        return sb.toString();
    }
}
