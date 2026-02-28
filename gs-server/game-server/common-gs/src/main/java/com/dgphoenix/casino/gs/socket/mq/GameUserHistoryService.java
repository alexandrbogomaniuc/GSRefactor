package com.abs.casino.gs.socket.mq;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.abs.casino.mqb.GameUserHistory;
import com.abs.casino.mqb.GameUserHistoryInfo;
import com.abs.casino.services.LoginService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GameUserHistoryService {
    private final LoginService loginService;
    private final CassandraGameSessionPersister gameSessionPersister;
    private final AccountManager accountManager;
    private final BaseGameInfoTemplateCache gameInfoManager;

    public GameUserHistoryService(LoginService loginService, CassandraPersistenceManager cpm, AccountManager accountManager, BaseGameInfoTemplateCache gameInfoManager) {
        this.loginService = loginService;
        this.gameSessionPersister = cpm.getPersister(CassandraGameSessionPersister.class);
        this.accountManager = accountManager;
        this.gameInfoManager = gameInfoManager;
    }

    public GameUserHistoryInfo getUserGameHistoryInfo(Long mmcBankId, String mmcToken, Long mqcBankId, String mqcToken, Long startTime, Long endTime, ClientType clientType) throws CommonException {
        if (startTime == null || startTime > System.currentTimeMillis()) {
            startTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
        }
        Date startDate = new Date(startTime);
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        Date endDate = new Date(endTime);

        CommonWalletAuthResult mmcUserCWInfo = loginService.getUserCWInfo(mmcToken, mmcBankId, clientType);
        if (mmcUserCWInfo == null) {
            throw new CommonException("User not found with token: " + mmcToken + ", bankId: " + mmcBankId + ", clientType: " + clientType);
        }
        CommonWalletAuthResult mqcUserCWInfo = loginService.getUserCWInfo(mqcToken, mqcBankId, clientType);
        if (mqcUserCWInfo == null) {
            throw new CommonException("User not found with token: " + mqcToken + ", bankId: " + mqcBankId + ", clientType: " + clientType);
        }
        List<GameUserHistory> mmcHistory = loadGameUserHistories(mmcBankId, mmcUserCWInfo.getUserId(), startDate, endDate);
        List<GameUserHistory> mqcHistory = loadGameUserHistories(mqcBankId, mqcUserCWInfo.getUserId(), startDate, endDate);
        return new GameUserHistoryInfo(mmcHistory, mqcHistory);
    }

    private List<GameUserHistory> loadGameUserHistories(Long bankId, String externalId, Date startDate, Date endDate) throws CommonException {
        AccountInfo accountInfo = accountManager.getByCompositeKey(bankId, externalId);
        if (accountInfo == null) {
            return new ArrayList<>();
        }
        List<GameSession> gameSession = gameSessionPersister.getAccountGameSessionList(accountInfo.getId(), startDate, endDate);
        return gameSession.stream().map(this::buildGameHistory).collect(Collectors.toList());
    }

    private GameUserHistory buildGameHistory(GameSession gameSession) {
        return new GameUserHistory(gameSession.getAccountId(), gameSession.getGameId(), getNameByGameId(gameSession.getGameId()),
                gameSession.getIncome(), gameSession.getPayout(), gameSession.getCurrency().getCode(), gameSession.getStartTime());
    }

    private String getNameByGameId(long gameId) {
        BaseGameInfoTemplate template = gameInfoManager.getBaseGameInfoTemplateById(gameId);
        return template.getTitle();
    }
}
