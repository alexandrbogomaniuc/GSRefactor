package com.dgphoenix.casino.gs.singlegames.tools.cbservtools;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.dblink.*;
import com.abs.casino.gs.managers.game.engine.AbstractSPGameEngine;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created Date: 23.03.2009 Time: 18:30:56
 */
public class SPGameProcessor extends AbstractSPGameEngine {
    private static final Logger LOG = LogManager.getLogger(SPGameProcessor.class);

    public SPGameProcessor(long gameId) {
        super(gameId);
    }

    protected IDBLink createDBLink(AccountInfo accountInfo, GameMode mode, Long gameSessionId, Long bonusId,
                                   SessionInfo sessionInfo, IBaseGameInfo gameInfo, String lang)
            throws CommonException {

        if (BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId()).isStubMode()) {
            if (mode != GameMode.FREE) {
                RemoteClientStubHelper.getInstance().getExtAccountInfo(accountInfo.getExternalId())
                        .setBalance(accountInfo.getBalance());
            }
        }

        switch (mode) {
            case REAL: {
                if (bonusId != null) {
                    FRBonus frBonus = FRBonusManager.getInstance().getById(bonusId);
                    if (frBonus != null) {
                        if (frBonus.getAccountId() != accountInfo.getId()) {
                            throw new CommonException("Inconsistent state, frBonusId=" + bonusId +
                                    ", frBonus=" + frBonus + ", accountId=" + accountInfo.getId());
                        }
                        return new FRBonusDBLink(accountInfo, getGameId(), gameSessionId, bonusId, sessionInfo,
                                gameInfo, lang);
                    }
                }
                return new DBLink(accountInfo, getGameId(), gameSessionId, sessionInfo, gameInfo, lang);
            }
            case FREE: {
                return new DBLinkFree(accountInfo, getGameId(), gameSessionId, sessionInfo, gameInfo, lang);
            }
            case BONUS: {
                return new BonusDBLink(accountInfo, getGameId(), gameSessionId, bonusId, sessionInfo, gameInfo, lang);
            }
            default: {
                throw new CommonException("game mode is not supported:" + mode);
            }
        }
    }

    @Override
    public IDBLink recreateDBLink(GameSession gameSession, AccountInfo accountInfo, boolean putToCache)
            throws CommonException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SPGameProcessor::recreateDBLink gameSessionId:" + gameSession.getId());
        }

        long accountId = gameSession.getAccountId();
        long bankId = accountInfo.getBankId();
        long gameId = gameSession.getGameId();
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId,
                accountInfo.getCurrency());

        if (BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId()).isStubMode()) {
            RemoteClientStubHelper.getInstance().getExtAccountInfo(accountInfo.getExternalId()).setBalance(accountInfo.getBalance());
        }

        IDBLink dblink;
        if (gameSession.isFRBonusGameSession()) {
            dblink = new FRBonusDBLink(accountId, accountInfo.getNickName(), bankId, gameId, gameInfo.getName(),
                    false, gameSession, accountInfo.getCurrency(),
                    gameSession.getFrbonusId());
        } else if (gameSession.isBonusGameSession()) {
            dblink = new BonusDBLink(accountId, accountInfo.getNickName(), bankId, gameId, gameInfo.getName(),
                    false, gameSession, accountInfo.getCurrency(),
                    gameSession.getBonusId());
        } else if (gameSession.isRealMoney()) {
            dblink = new DBLink(accountId, accountInfo.getNickName(), bankId, gameId, gameInfo.getName(),
                    false, gameSession, accountInfo.getCurrency());
        } else {
            dblink = new DBLinkFree(accountId, accountInfo.getNickName(), bankId, gameId, gameInfo.getName(),
                    false, gameSession, accountInfo.getCurrency());
        }
        if (putToCache) {
            return DBLinkCache.getInstance().putAndGetSilent(dblink);
        }
        return dblink;
    }

}
