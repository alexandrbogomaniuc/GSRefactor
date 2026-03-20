package com.abs.casino.services.mp;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraCurrentPlayerSessionStatePersister;
import com.abs.casino.cassandra.persist.CassandraPlayerSessionState;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.client.canex.request.privateroom.Player;
import com.abs.casino.common.client.canex.request.privateroom.PrivateRoom;
import com.abs.casino.common.client.canex.request.privateroom.Status;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.gs.GameServer;
import com.abs.casino.gs.persistance.remotecall.KafkaRequestMultiPlayer;
import com.abs.casino.gs.socket.mq.BattlegroundService;
import com.abs.casino.kafka.dto.GetMQDataRequest;
import com.abs.casino.kafka.dto.SitOutRequest2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MPGameSessionService {
    private static final Logger LOG = LogManager.getLogger(MPGameSessionService.class);
    private final BankInfoCache bankInfoCache;
    private volatile GameServer gameServer;
    private final SessionHelper sessionHelper;
    private final KafkaRequestMultiPlayer kafkaRequestMultiPlayer;
    private final BattlegroundService battlegroundService;
    private final CassandraCurrentPlayerSessionStatePersister playerSessionStatePersister;

    public MPGameSessionService(BankInfoCache bankInfoCache,
                                SessionHelper sessionHelper,
                                KafkaRequestMultiPlayer kafkaRequestMultiPlayer,
                                BattlegroundService battlegroundService,
                                CassandraPersistenceManager cpm) {
        this.bankInfoCache = bankInfoCache;
        this.sessionHelper = sessionHelper;
        this.kafkaRequestMultiPlayer = kafkaRequestMultiPlayer;
        this.battlegroundService = battlegroundService;
        this.playerSessionStatePersister = cpm.getPersister(CassandraCurrentPlayerSessionStatePersister.class);
    }

    public CassandraPlayerSessionState getPlayerSessionWithUnfinishedSid(String extId) {
        return playerSessionStatePersister.getPlayerSessionWithUnfinishedSid(extId);
    }

    public void savePlayerSessionState(String sid, String extId, String privateRoomId, boolean isFinishGameSession, long dateTime) {
        playerSessionStatePersister.persist(sid, extId, privateRoomId, isFinishGameSession, dateTime);
    }

    public CassandraPlayerSessionState getBySid(String sid) {
        return playerSessionStatePersister.getBySid(sid);
    }

    private void updatePlayersStatusInPrivateRoom(AccountInfo account, String privateRoomId) {

        LOG.debug("updatePlayersStatusInPrivateRoom: privateRoomId:{}, AccountInfo:{}", privateRoomId, account);

        try {
            if (StringUtils.isTrimmedEmpty(privateRoomId)) {
                LOG.debug("updatePlayersStatusInPrivateRoom: privateRoomId is empty, skip function for {}", account);
                return;
            }

            if (account == null) {
                LOG.error("updatePlayersStatusInPrivateRoom: account is null, privateRoomId={}", privateRoomId);
                return;
            }

            if (battlegroundService == null) {
                LOG.error("updatePlayersStatusInPrivateRoom: battlegroundService is null, privateRoomId={}, account={}",
                        privateRoomId, account);
                return;
            }

            if (battlegroundService.getPrivateRoomSettings(privateRoomId) == null) {
                LOG.error("updatePlayersStatusInPrivateRoom: no PrivateRoomSettings object exists for " +
                                "privateRoomId={}, account={}", privateRoomId, account);
                return;
            }

            Player player = new Player(account.getNickName(), account.getExternalId(), Status.ACCEPTED);
            List<Player> players = new ArrayList();
            players.add(player);

            PrivateRoom privateRoom = new PrivateRoom();
            privateRoom.setPrivateRoomId(privateRoomId);
            privateRoom.setBankId((long) account.getBankId());
            privateRoom.setPlayers(players);

            LOG.debug("updatePlayersStatusInPrivateRoom: PrivateRoom:{}", privateRoom);

            battlegroundService.updatePlayersStatusInPrivateRoom(privateRoom, false);

        }
        catch(Exception e) {
            LOG.error("updatePlayersStatusInPrivateRoom: Exception Error {}", e.getMessage(), e);
        }
    }

    public Pair<GameSession, Boolean> finishGameSessionAndMakeSitOut(String sid, String privateRoomId) throws CommonException {
        boolean isLocked = false;
        String extId = null;
        try {
            long fiveMillis = TimeUnit.SECONDS.toMillis(5);
            sessionHelper.lock(sid, fiveMillis);
            isLocked = true;
            sessionHelper.openSession();
            ITransactionData transactionData = sessionHelper.getTransactionData();
            if (transactionData == null) {
                LOG.warn("finishGameSessionAndMakeSitOut: TransactionData is null, sid {}", sid);
                throw new CommonException("TransactionData is null");
            }

            GameSession gameSession = transactionData.getGameSession();

            AccountInfo account = transactionData.getAccount();

            LOG.debug("finishGameSessionAndMakeSitOut: find game session for account: {}, gameSession: {}",
                    account, gameSession);

            if (account == null) {
                LOG.warn("finishGameSessionAndMakeSitOut: Unable to find account from transactionData, sid {}", sid);
                throw new CommonException("Unable find account from transactionData");
            }

            extId = account.getExternalId();

            BankInfo bankInfo = bankInfoCache.getBankInfo(account.getBankId());
            if (bankInfo == null) {
                LOG.warn("finishGameSessionAndMakeSitOut: BankInfo is null for account {}", account);
                throw new CommonException("BankInfo is null for account: " + account);
            }

            Pair<GameSession, Boolean> result;
            if (getGameServer().needCloseMultiplayerGame(gameSession, bankInfo, -1)) {
                LOG.debug("finishGameSessionAndMakeSitOut: needCloseMultiplayerGame and SitOut account: {}",
                    account);
                sessionHelper.commitTransaction();
                sessionHelper.markTransactionCompleted();
                sessionHelper.clearWithUnlock();
                isLocked = false;
                kafkaRequestMultiPlayer.sitOut(new SitOutRequest2(account.getId(), gameSession.getId()));

                result = new Pair<>(gameSession, true);
            } else {
                LOG.debug("finishGameSessionAndMakeSitOut: no needCloseMultiplayerGame for account: {}",
                        account);
                sessionHelper.commitTransaction();
                sessionHelper.markTransactionCompleted();
                result =  new Pair<>(gameSession, false);
            }

            if(!StringUtils.isTrimmedEmpty(privateRoomId)) {
                LOG.debug("finishGameSessionAndMakeSitOut: updatePlayersStatusInPrivateRoom for " +
                        "privateRoomId:{} and account {}", privateRoomId, account);
                this.updatePlayersStatusInPrivateRoom(account, privateRoomId);
            }

            return result;
        } finally {
            if (isLocked) {
                sessionHelper.getTransactionData().setAppliedAutoFinishLogic(false);
                sessionHelper.clearWithUnlock();
            }

            try {
                LOG.debug("finishGameSessionAndMakeSitOut: savePlayerSessionState sid:{}, extId={}, privateRoomId={}, " +
                        "isFinishGameSession=true", sid, extId, privateRoomId);
                this.savePlayerSessionState(sid, extId, privateRoomId, true, System.currentTimeMillis());
            } catch (Exception e) {
                LOG.warn("finishGameSessionAndMakeSitOut: Exception:{} ", e.getMessage(), e);
            }
        }
    }

    private GameServer getGameServer() {
        GameServer local = gameServer;
        if (local == null) {
            synchronized (this) {
                local = gameServer;
                if (local == null) {
                    local = GameServer.getInstance();
                    gameServer = local;
                }
            }
        }
        return local;
    }
}
