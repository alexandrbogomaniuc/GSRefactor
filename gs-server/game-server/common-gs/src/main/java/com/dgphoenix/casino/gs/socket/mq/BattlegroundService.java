package com.dgphoenix.casino.gs.socket.mq;

import com.abs.casino.kafka.dto.GetPrivateRoomInfoRequest;

import com.dgphoenix.casino.account.AccountManager;
import com.abs.casino.battleground.messages.*;
import com.abs.casino.battleground.messages.*;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.abs.casino.cassandra.persist.mp.*;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.client.canex.request.friends.Friend;
import com.abs.casino.common.client.canex.request.friends.UpdateFriendsRequest;
import com.abs.casino.common.client.canex.request.friends.UpdateFriendsResponse;
import com.abs.casino.common.client.canex.request.onlineplayer.OnlinePlayer;
import com.abs.casino.common.client.canex.request.onlineplayer.UpdateOnlinePlayersRequest;
import com.abs.casino.common.client.canex.request.onlineplayer.UpdateOnlinePlayersResponse;
import com.abs.casino.common.client.canex.request.privateroom.Player;
import com.abs.casino.common.client.canex.request.privateroom.PrivateRoom;
import com.abs.casino.common.client.canex.request.privateroom.Status;
import com.abs.casino.common.client.canex.request.privateroom.UpdateRoomResponse;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.promo.battleground.BattlegroundConfig;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.abs.casino.gs.persistance.remotecall.KafkaRequestMultiPlayer;
import com.abs.casino.kafka.dto.*;
import com.abs.casino.kafka.dto.privateroom.request.*;
import com.abs.casino.kafka.dto.privateroom.response.*;
import com.abs.casino.promo.persisters.CassandraBattlegroundConfigPersister;
import com.abs.casino.services.LoginService;
import com.abs.casino.util.BGFStatusUtil;
import com.abs.casino.util.BGOStatusUtil;
import com.abs.casino.util.BGStatusUtil;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import com.abs.casino.battleground.messages.BattlegroundInfo;
import com.abs.casino.battleground.messages.BattlegroundRoundHistoryInfo;
import com.abs.casino.kafka.dto.StringResponseDto;
import com.abs.casino.kafka.dto.privateroom.response.DeactivateRoomResultDto;
import com.abs.casino.kafka.dto.privateroom.response.PrivateRoomIdResultDto;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BattlegroundService {
    private static final Logger LOG = LogManager.getLogger(BattlegroundService.class);

    private static final Integer WIN_PRIZE_POSITION = 1;

    private final CassandraBattlegroundConfigPersister cassandraBattlegroundConfigPersister;
    private final BankInfoCache bankInfoCache;
    private final BaseGameInfoTemplateCache baseGameInfoTemplateCache;
    private final LoadingCache<Long, List<BattlegroundInfo>> configsCache;
    private final BattlegroundHistoryPersister battlegroundHistoryPersister;
    private final LoginService loginService;
    private final AccountManager accountManager;
    private final BattlegroundPrivateRoomSettingsPersister battlegroundPrivateRoomSettingsPersister;
    private final CassandraAccountInfoPersister cassandraAccountInfoPersister;
    private final MQServiceHandler mqServiceHandler;
    private final KafkaRequestMultiPlayer kafkaRequestMultiPlayer;

    public BattlegroundService(CassandraPersistenceManager cpm, BankInfoCache bankInfoCache, BaseGameInfoTemplateCache baseGameInfoTemplateCache,
                               LoginService loginService, AccountManager accountManager, CommonExecutorService executorService,
                               MQServiceHandler mqServiceHandler, KafkaRequestMultiPlayer kafkaRequestMultiPlayer) {
        this.cassandraBattlegroundConfigPersister = cpm.getPersister(CassandraBattlegroundConfigPersister.class);
        this.bankInfoCache = bankInfoCache;
        this.baseGameInfoTemplateCache = baseGameInfoTemplateCache;
        this.battlegroundHistoryPersister = cpm.getPersister(BattlegroundHistoryPersister.class);
        this.accountManager = accountManager;
        this.loginService = loginService;
        this.configsCache = configureConfigsCache();
        this.battlegroundPrivateRoomSettingsPersister = cpm.getPersister(BattlegroundPrivateRoomSettingsPersister.class);
        this.cassandraAccountInfoPersister = cpm.getPersister(CassandraAccountInfoPersister.class);
        this.mqServiceHandler = mqServiceHandler;
        this.kafkaRequestMultiPlayer = kafkaRequestMultiPlayer;
    }

    public BattlegroundRoundHistoryInfo getPlayerBattlegroundHistory(Long mmcBankId, String mmcToken, Long mqcBankId, String mqcToken, Long startTime, Long endTime, ClientType clientType) throws CommonException {
        if (startTime == null || startTime > System.currentTimeMillis()) {
            startTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        CommonWalletAuthResult mmcUserCWInfo = loginService.getUserCWInfo(mmcToken, mmcBankId, clientType);
        if (mmcUserCWInfo == null) {
            throw new CommonException("User not found with token: " + mmcToken + ", bankId: " + mmcBankId + ", clientType: " + clientType);
        }
        LOG.debug("Auth result for token:{}, bankId:{}, clientType:{}, walletAuthResult:{}", mmcBankId, mmcToken, clientType, mmcUserCWInfo);
        CommonWalletAuthResult mqcUserCWInfo = loginService.getUserCWInfo(mqcToken, mqcBankId, clientType);
        if (mqcUserCWInfo == null) {
            throw new CommonException("User not found with token: " + mqcToken + ", bankId: " + mqcBankId + ", clientType: " + clientType);
        }
        LOG.debug("Auth result for token:{}, bankId:{}, clientType:{}, walletAuthResult:{}", mqcBankId, mqcBankId, clientType, mqcUserCWInfo);
        List<BattlegroundRoundHistory> mmcRounds = loadBattlegroundRounds(mmcBankId, mmcUserCWInfo.getUserId(), startTime, endTime);
        List<BattlegroundRoundHistory> mqcRounds = loadBattlegroundRounds(mqcBankId, mqcUserCWInfo.getUserId(), startTime, endTime);
        return new BattlegroundRoundHistoryInfo(mmcRounds, mqcRounds);
    }

    private List<BattlegroundRoundHistory> loadBattlegroundRounds(Long bankId, String externalId, Long startTime, Long endTime) throws CommonException {
        AccountInfo accountInfo = accountManager.getByCompositeKey(bankId, externalId);
        if (accountInfo == null) {
            return new ArrayList<>();
        }
        LOG.debug("loadBattlegroundRounds find accountInfo. ExternalId:{}, AccountInfo:{}", externalId, accountInfo);
        List<BattlegroundRound> roundHistories = battlegroundHistoryPersister.getBattlegroundHistoryByAccountIdAndPeriod(accountInfo.getId(), startTime, endTime);
        return roundHistories.stream()
                .map(history -> buildBattlegroundRoundHistory(history, accountInfo))
                .collect(Collectors.toList());
    }

    private BattlegroundRoundHistory buildBattlegroundRoundHistory(BattlegroundRound battlegroundRound, AccountInfo accountInfo) {
        return new BattlegroundRoundHistory(battlegroundRound.getGameId(), battlegroundRound.getGameName(), battlegroundRound.getBuyIn(),
                WIN_PRIZE_POSITION.equals(battlegroundRound.getFinalRank()) ? battlegroundRound.getWinnerPot() : 0L,
                accountInfo.getCurrency().getCode(), battlegroundRound.getFinalRank(), battlegroundRound.getDateTime(), accountInfo.getId(), battlegroundRound.getRoundId());
    }

    public List<BattlegroundInfo> getGamesByBankId(long bankId) {
        return configsCache.getUnchecked(bankId);
    }

    private String getNameByGameId(long gameId) {
        BaseGameInfo defaultGameInfo = baseGameInfoTemplateCache.getDefaultGameInfo(gameId);
        return defaultGameInfo.getName();
    }

    public boolean isBattlegroundGame(long gameId) {
        BaseGameInfoTemplate template = baseGameInfoTemplateCache.getBaseGameInfoTemplateById(gameId);
        return template != null && template.isBattleGroundsMultiplayerGame();
    }

    private LoadingCache<Long, List<BattlegroundInfo>> configureConfigsCache() {
        return CacheBuilder
                .newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build(new CacheLoader<Long, List<BattlegroundInfo>>() {
                    @Override
                    public List<BattlegroundInfo> load(Long bankId) throws Exception {
                        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
                        if (bankInfo == null) {
                            throw new CommonException("Incorrect bankId=" + bankId);
                        }
                        Set<BattlegroundConfig> configs = cassandraBattlegroundConfigPersister.getConfigs(bankInfo.getId());
                        if (configs == null || configs.isEmpty()) {
                            throw new CommonException(String.format("For bank=%d configurations do not exist", bankId));
                        }
                        return configs.stream()
                                .map(battlegroundConfig -> new BattlegroundInfo(battlegroundConfig.getGameId(),
                                        getNameByGameId(battlegroundConfig.getGameId()),
                                        battlegroundConfig.getBuyInsForDefaultCurrency()
                                )).collect(Collectors.toList());
                    }
                });
    }

    private AccountInfo getAccountInfo(String token, CommonWalletAuthResult authResult, long bankId, ClientType clientType) throws CommonException {
        AccountInfo accountInfo = accountManager.getByCompositeKey(bankId, authResult.getUserId());
        if (accountInfo == null) {
            SessionHelper.getInstance().lock((int) bankId, authResult.getUserId());
            try {
                SessionHelper.getInstance().openSession();

                BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
                String nickName = !StringUtils.isTrimmedEmpty(authResult.getUserName()) ?
                        authResult.getUserName()  :
                        token;

                accountInfo = accountManager.saveAccount(null, authResult.getUserId(), bankInfo, nickName, false, false, null,
                        clientType, null, null, authResult.getCurrency(), authResult.getCountryCode(), true);

                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();

                LOG.debug("getAccountInfo: Create new account: {}", accountInfo);
            } catch (Exception e) {
                LOG.error("getAccountInfo: Unable to create account", e);
                return null;
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }

        return accountInfo;
    }

    public StringResponseDto getPrivateRoomURL(String token, int gameId, long bankId,
                                               long buyIn, String domainUrl, ClientType clientType) throws CommonException {
        validateInputParams(token, gameId, bankId, buyIn, domainUrl);
        CommonWalletAuthResult authResult = loginService.getUserCWInfo(token, bankId, clientType);
        validateAuthResult(authResult, token, bankId, clientType);

        AccountInfo accountInfo = getAccountInfo(token, authResult, bankId, clientType);

        if(accountInfo == null) {
            LOG.error("getPrivateRoomURL: Unable to identify account");
            return null;
        }

        String ownerExternalId = accountInfo.getExternalId();
        LOG.debug("getPrivateRoomURL: ownerExternalId = : {}", ownerExternalId);

        PrivateRoomURLDto createRoomRequest = new PrivateRoomURLDto(
                authResult.getUserName(),
                gameId,
                bankId,
                buyIn,
                authResult.getCurrency(),
                domainUrl,
                ownerExternalId);
        return kafkaRequestMultiPlayer.getPrivateRoomURL(createRoomRequest);
    }

    public PrivateRoomIdResultDto getPrivateRoomId(String token, int gameId, long bankId,
                                                   long buyIn, ClientType clientType) throws CommonException {
        validateInputParams(token, gameId, bankId, buyIn, null);
        CommonWalletAuthResult authResult = loginService.getUserCWInfo(token, bankId, clientType);
        validateAuthResult(authResult, token, bankId, clientType);

        AccountInfo accountInfo = getAccountInfo(token, authResult, bankId, clientType);

        if(accountInfo == null) {
            LOG.error("getPrivateRoomId: Unable to identify account");
            return new PrivateRoomIdResultDto(false, 400, "internal error", null, null);
        }

        long ownerAccountId = accountInfo.getId();
        String ownerExternalId = accountInfo.getExternalId();

        LOG.debug("getPrivateRoomId: ownerAccountId = {}, ownerExternalId = : {}", ownerAccountId, ownerExternalId);

        PrivateRoomIdDto createRoomRequest = new PrivateRoomIdDto(
                authResult.getUserName(),
                gameId,
                bankId,
                buyIn,
                authResult.getCurrency(),
                ownerAccountId,
                ownerExternalId);

        return kafkaRequestMultiPlayer.getPrivateRoomId(createRoomRequest);
    }

    public DeactivateRoomResultDto deactivate(String token, String roomId, long bankId, ClientType clientType) throws CommonException {
        if (StringUtils.isTrimmedEmpty(token)) {
            throw new CommonException("token not presented");
        }
        CommonWalletAuthResult authResult = loginService.getUserCWInfo(token, bankId, clientType);
        validateAuthResult(authResult, token, bankId, clientType);

        AccountInfo accountInfo = accountManager.getByCompositeKey(bankId, authResult.getUserId());

        if (accountInfo == null) {
            LOG.error("deactivate: Unable deactivate private room, account info is null");
            return new DeactivateRoomResultDto(false, 400, "internal error");
        }

        long ownerAccountId = accountInfo.getId();
        String ownerExternalId = accountInfo.getExternalId();

        LOG.debug("deactivate: ownerAccountId = {}, ownerExternalId = : {}", ownerAccountId, ownerExternalId);
        DeactivateRoomDto request = new DeactivateRoomDto(authResult.getUserName(), ownerAccountId, roomId, ownerExternalId);
        return kafkaRequestMultiPlayer.deactivate(request);
    }

    public DeactivateRoomResultDto deactivate(List<String> roomIds) throws CommonException {
        LOG.debug("deactivate: roomIds = {}", roomIds);

        if (roomIds == null) {
            LOG.error("deactivate: roomIds is null");
            return new DeactivateRoomResultDto(false, 400,"roomIds is null");
        }

        DeactivateRoomResultDto fullResult = new DeactivateRoomResultDto(true, 200,"");

        for(String roomId : roomIds) {

            DeactivateRoomResultDto result = this.deactivate(roomId);

            StringBuilder sb = new StringBuilder(fullResult.getReasonPhrases());
            sb.append(roomId);

            if(result == null) {
                fullResult.setStatusCode(400);
                sb.append("-error:result is null;");
            } else {

                if(!StringUtils.isTrimmedEmpty(result.getReasonPhrases())) {
                    sb.append(":");
                    sb.append(result.getReasonPhrases());
                }

                if(result.getStatusCode() != 200) {
                    fullResult.setStatusCode(400);
                    sb.append("-error:");
                    sb.append(result.getStatusCode());
                    sb.append(";");
                } else {
                    sb.append("-success;");
                }
            }

            fullResult.setReasonPhrases(sb.toString());
        }

        return fullResult;
    }

    public DeactivateRoomResultDto deactivate(String roomId) throws CommonException {
        LOG.debug("deactivate: roomId = {}", roomId);

        if(StringUtils.isTrimmedEmpty(roomId)) {
            LOG.error("deactivate: roomId is null");
            return new DeactivateRoomResultDto(false, 400, "roomId is null");
        }

        DeactivateRoomDto request = new DeactivateRoomDto(null, 0, roomId, null);
        return kafkaRequestMultiPlayer.deactivate(request);
    }

    public static Status fromTBGStatus(BGStatus tbgStatus) {
        switch (tbgStatus) {
            case accepted:
                return Status.ACCEPTED;
            case rejected:
                return Status.REJECTED;
            case kicked:
                return Status.KICKED;
            case loading:
                return Status.LOADING;
            case ready:
                return Status.READY;
            case waiting:
                return Status.WAITING;
            case playing:
                return Status.PLAYING;
            default:
                return Status.INVITED;
        }
    }

    public static BGStatus toBGStatus(Status status) {
        switch (status) {
            case ACCEPTED:
                return BGStatus.accepted;
            case REJECTED:
                return BGStatus.rejected;
            case KICKED:
                return BGStatus.kicked;
            case LOADING:
                return BGStatus.loading;
            case READY:
                return BGStatus.ready;
            case WAITING:
                return BGStatus.waiting;
            case PLAYING:
                return BGStatus.playing;
            default:
                return BGStatus.invited;
        }
    }

    public List<BGOnlinePlayerDto> convertTBGFriendsToTBGOnlinePlayers(Collection<BGFriendDto> tbgFriends) {

        LOG.debug("convertTBGFriendsToTBGOnlinePlayers: tbgFriends:{}", tbgFriends);

        if(tbgFriends == null || tbgFriends.isEmpty()) {
            LOG.error("convertTBGFriendsToTBGOnlinePlayers: tbgFriends is empty");
            return null;
        }

        List<BGOnlinePlayerDto> tbgOnlinePlayers = new ArrayList<>();

        for(BGFriendDto tbgFriend : tbgFriends) {

            BGOnlinePlayerDto tbgOnlinePlayer = new BGOnlinePlayerDto(
                    tbgFriend.getNickname(),
                    tbgFriend.getExternalId(),
                    null
            );

            tbgOnlinePlayers.add(tbgOnlinePlayer);
        }

        LOG.debug("convertTBGFriendsToTBGOnlinePlayers: tbgOnlinePlayers:{}", tbgOnlinePlayers);

        return tbgOnlinePlayers;
    }

    public void getFriendsWithOnlineStatus(AccountInfo accountInfo) throws CommonException {

        //TO DO: check if accountInfo is Private Room Owner

        LOG.debug("getFriendsWithOnlineStatus: accountInfo:{}", accountInfo);

        if(accountInfo == null) {
            LOG.error("getFriendsWithOnlineStatus: accountInfo is null ");
            return;
        }

        if(StringUtils.isTrimmedEmpty(accountInfo.getExternalId())) {
            LOG.error("getFriendsWithOnlineStatus: accountInfo.getExternalId() is empty ");
            return;
        }

        if(accountInfo.getBankId() == 0) {
            LOG.error("getFriendsWithOnlineStatus: accountInfo.getBankId() is 0 ");
            return;
        }

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());

        if(bankInfo == null) {
            LOG.error("getFriendsWithOnlineStatus: bankInfo is null for bankId:{}", accountInfo.getBankId());
            return;
        }

        if(!bankInfo.isAllowUpdatePlayersStatusInPrivateRoom()) {
            LOG.debug("getFriendsWithOnlineStatus: Update Players Status In PrivateRoom is not allowed skip " +
                    "getFriendsWithOnlineStatus, check BankInfo configuration: {}", bankInfo);
            return;
        }

        BGFriendDto tbgFriend = new BGFriendDto(accountInfo.getNickName(), accountInfo.getExternalId(), null);

        LOG.debug("getFriendsWithOnlineStatuses: tbgFriend:{} converted from accountInfo:{}", tbgFriend, accountInfo);

        List<BGFriendDto> tbgFriends = null;

        try {
            tbgFriends = mqServiceHandler.getFriends(tbgFriend, (long) accountInfo.getBankId());
        } catch (Exception e) {
            LOG.error("getFriendsWithOnlineStatus: exception to get tbgFriends for accountInfo:{}", accountInfo);
        }

        LOG.debug("getFriendsWithOnlineStatus: tbgFriends:{}", tbgFriends);

        if(tbgFriends == null) {
            LOG.debug("getFriendsWithOnlineStatus: tbgFriends is null skip for accountInfo:{}", accountInfo);
            return;
        }

        if(tbgFriends.isEmpty()) {
            LOG.debug("getFriendsWithOnlineStatus: tbgFriends is empty skip for accountInfo:{}", accountInfo);
            return;
        }

        UpdateFriendsDto tbgUpdateFriendsRequest = new UpdateFriendsDto(
                tbgFriend.getNickname(),
                tbgFriend.getExternalId(),
                tbgFriends
        );

        LOG.debug("getFriendsWithOnlineStatus: tbgUpdateFriendsRequest:{}", tbgUpdateFriendsRequest);

        UpdateFriendsResultDto tbgUpdateFriendsResult  = kafkaRequestMultiPlayer.updateFriends(tbgUpdateFriendsRequest);

        LOG.debug("getFriendsWithOnlineStatus: tbgUpdateFriendsResult:{}", tbgUpdateFriendsResult);

        if(tbgUpdateFriendsResult == null || tbgUpdateFriendsResult.getStatusCode() != 200) {

            LOG.error("getFriendsWithOnlineStatus: bad tbgUpdateFriendsResult for " +
                    "tbgUpdateFriendsRequest:{}", tbgUpdateFriendsRequest);
        }

        List<BGOnlinePlayerDto> tbgOnlinePlayers = convertTBGFriendsToTBGOnlinePlayers(tbgFriends);

        LOG.debug("getFriendsWithOnlineStatus: tbgOnlinePlayers:{}", tbgOnlinePlayers);

        if(tbgOnlinePlayers == null || tbgOnlinePlayers.isEmpty()) {
            LOG.error("getFriendsWithOnlineStatus: tbgOnlinePlayers is empty: {}, skip getOnlineStatus", tbgOnlinePlayers);
            return;
        }

        try {
            tbgOnlinePlayers = mqServiceHandler.getOnlineStatus(tbgOnlinePlayers, (long) accountInfo.getBankId());
        } catch (Exception e) {
            LOG.error("getFriendsWithOnlineStatus: exception to get getOnlineStatus for accountInfo:{}", accountInfo);
        }

        UpdateOnlinePlayersDto tbgUpdateOnlinePlayersRequest = new UpdateOnlinePlayersDto(tbgOnlinePlayers);

        LOG.debug("getFriendsWithOnlineStatus: after getOnlineStatus tbgUpdateOnlinePlayersRequest:{}",
                tbgUpdateOnlinePlayersRequest);

        UpdateOnlinePlayersResultDto tbgUpdateOnlinePlayersResult  = kafkaRequestMultiPlayer
                .updateOnlinePlayers(tbgUpdateOnlinePlayersRequest);

        LOG.debug("getFriendsWithOnlineStatus: tbgUpdateOnlinePlayersResult:{}", tbgUpdateOnlinePlayersResult);

        if(tbgUpdateOnlinePlayersResult == null || tbgUpdateOnlinePlayersResult.getStatusCode() != 200) {

            LOG.error("getFriendsWithOnlineStatus: bad tbgUpdateOnlinePlayersResult for " +
                    "tbgUpdateOnlinePlayersRequest:{}", tbgUpdateOnlinePlayersResult);
        }
    }

    public UpdateRoomResponse updatePlayersStatusInPrivateRoomToLoading(AccountInfo accountInfo, String privateRoomId) throws CommonException {

        LOG.debug("updatePlayersStatusInPrivateRoomToLoading: privateRoomId:{}, accountInfo:{}", privateRoomId, accountInfo);

        if(accountInfo == null) {
            LOG.error("updatePlayersStatusInPrivateRoomToLoading: accountInfo is null");
            return null;
        }

        if(StringUtils.isTrimmedEmpty(privateRoomId)) {
            LOG.error("updatePlayersStatusInPrivateRoomToLoading: privateRoomId is empty");
            return null;
        }

        Player player = new  Player(accountInfo.getNickName(),
                accountInfo.getExternalId(),
                Status.LOADING);

        List<Player> players = new ArrayList<Player>();
        players.add(player);

        PrivateRoom privateRoom = new PrivateRoom();
        privateRoom.setPrivateRoomId(privateRoomId);
        privateRoom.setBankId((long)accountInfo.getBankId());
        privateRoom.setPlayers(players);

        LOG.debug("updatePlayersStatusInPrivateRoomToLoading: PrivateRoom:{}", privateRoom);

        return this.updatePlayersStatusInPrivateRoom(privateRoom, false);
    }

    public UpdateRoomResponse updatePlayersStatusInPrivateRoom(PrivateRoom privateRoom, boolean isTransitionLimited) throws CommonException {
        LOG.debug("updatePlayersStatusInPrivateRoom: isTransitionLimited={}, privateRoom={}", isTransitionLimited, privateRoom);

        if(privateRoom == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: privateRoom is null");
            return new UpdateRoomResponse(400, "privateRoom is null", null, null);
        }

        Long bankId = privateRoom.getBankId();

        if( bankId == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: bankId is null");
            return new UpdateRoomResponse(400, "BankId is null", privateRoom.getPrivateRoomId(), null);
        }

        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        if (bankInfo == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: BankInfo is null for bankId: {}", bankId);
            return new UpdateRoomResponse(400, "BankInfo not found", privateRoom.getPrivateRoomId(), null);
        }

        if(bankInfo.isAllowUpdatePlayersStatusInPrivateRoom()) {

            UpdateRoomDto request = createUpdateRoomRequest(privateRoom, isTransitionLimited);

            if (request == null) {
                LOG.error("updatePlayersStatusInPrivateRoom: request is null for privateRoom: {}", privateRoom);
                return new UpdateRoomResponse(400, "Not Enough Info", privateRoom.getPrivateRoomId(), null);
            } else {
                LOG.debug("updatePlayersStatusInPrivateRoom: isTransitionLimited:{}, TBGUpdateRoomRequest:{}", isTransitionLimited, request);
            }

            UpdateRoomResultDto updateRoomResultDto = kafkaRequestMultiPlayer.updatePlayersStatusInPrivateRoom(request);

            if (updateRoomResultDto == null) {
                LOG.error("updatePlayersStatusInPrivateRoom: tbgUpdateRoomResult is null for TBGUpdateRoomRequest: {}", request);
                return new UpdateRoomResponse(400, "No Result", privateRoom.getPrivateRoomId(), null);
            } else {
                LOG.debug("updatePlayersStatusInPrivateRoom: {}", updateRoomResultDto.getPrivateRoomId());
            }

            List<Player> players = new ArrayList<>();

            if(updateRoomResultDto.getPlayers() == null) {
                LOG.debug("updatePlayersStatusInPrivateRoom: tbgUpdateRoomResult.getPlayers() is null");
            } else {
                for (BGPlayerDto playerDto : updateRoomResultDto.getPlayers()) {
                    Player player = new Player(
                            playerDto.getNickname(),
                            playerDto.getExternalId(),
                            BGStatusUtil.fromBGStatus(playerDto.getStatus()));
                    players.add(player);
                }
            }

            return new UpdateRoomResponse(
                    updateRoomResultDto.getStatusCode(),
                    updateRoomResultDto.getReasonPhrases(),
                    updateRoomResultDto.getPrivateRoomId(),
                    players
            );


        } else {
            LOG.debug("updatePlayersStatusInPrivateRoom: Update Players Status In PrivateRoom is not allowed, check BankInfo configuration: {}", bankInfo);
            return new UpdateRoomResponse(400, "Update Players Status In PrivateRoom is not allowed, check BankInfo configuration",
                    privateRoom.getPrivateRoomId(), null);
        }
    }

    public UpdateFriendsResponse updateFriends(UpdateFriendsRequest updateFriendsRequest) throws CommonException {
        LOG.debug("updateFriends: updateFriendsRequest:{}", updateFriendsRequest);

        if (updateFriendsRequest == null) {
            LOG.error("updateFriends: updateFriendsRequest is null");
            return new UpdateFriendsResponse(400, "updateFriendsRequest is null", null, null, null);
        }

        UpdateFriendsDto request = createUpdateFriendsRequest(updateFriendsRequest);

        if (request == null) {
            LOG.error("updateFriends: UpdateFriendsRequest is null for updateFriendsRequest: {}", updateFriendsRequest);
            return new UpdateFriendsResponse(400, "Not Enough Info",
                    updateFriendsRequest.getNickname(), updateFriendsRequest.getExternalId(), null);
        } else {
            LOG.debug("updateFriends: UpdateFriendsRequest:{}", request);
        }

        UpdateFriendsResultDto result = kafkaRequestMultiPlayer.updateFriends(request);

        if (result == null) {
            LOG.error("updateFriends: updateFriendsResult is null for updateFriendsRequest: {}", updateFriendsRequest);
            return new UpdateFriendsResponse(400, "No Result",
                    updateFriendsRequest.getNickname(), updateFriendsRequest.getExternalId(), null);
        } else {
            LOG.debug("updateFriends: updateFriendsResult:{}", result);
        }

        List<Friend> friends = new ArrayList<>();
        for(BGFriendDto playerDto:  result.getPlayers()){
            Friend friend = new Friend(
                    playerDto.getNickname(),
                    playerDto.getExternalId(),
                    BGFStatusUtil.fromTBGFStatus(playerDto.getStatus())
            );
            friends.add(friend);
        }

        return new UpdateFriendsResponse(
                result.getStatusCode(),
                result.getReasonPhrases(),
                result.getNickname(),
                result.getExternalId(),
                friends
        );
    }

    public UpdateOnlinePlayersResponse updateOnlinePlayers(UpdateOnlinePlayersRequest updateOnlinePlayersRequest) throws CommonException {
        LOG.debug("updateOnlinePlayers: updateOnlinePlayersRequest:{}", updateOnlinePlayersRequest);

        if (updateOnlinePlayersRequest == null) {
            LOG.error("updateOnlinePlayers: updateOnlinePlayersRequest is null");
            return new UpdateOnlinePlayersResponse(400, "updateOnlinePlayersRequest is null", null);
        }

        UpdateOnlinePlayersDto request = createUpdateOnlinePlayersRequest(updateOnlinePlayersRequest);

        if (request == null) {
            LOG.error("updateOnlinePlayers: tgbUpdateOnlinePlayersRequest is null for updateOnlinePlayersRequest: {}", updateOnlinePlayersRequest);
            return new UpdateOnlinePlayersResponse(400, "Not Enough Info", null);
        } else {
            LOG.debug("updateOnlinePlayers: tgbUpdateOnlinePlayersRequest:{}", request);
        }

        UpdateOnlinePlayersResultDto result = kafkaRequestMultiPlayer.updateOnlinePlayers(request);

        if (result == null) {
            LOG.error("updateOnlinePlayers is null: {}", updateOnlinePlayersRequest);
            return new UpdateOnlinePlayersResponse(400, "No Result", null);
        } else {
            LOG.debug("updateOnlinePlayers: {}", result);
        }

        List<OnlinePlayer> onlinePlayers = new ArrayList<>();
        for(BGOnlinePlayerDto playerDtolayer : result.getOnlinePlayers()) {
            OnlinePlayer onlinePlayer = new OnlinePlayer(
                playerDtolayer.getNickname(),
                playerDtolayer.getExternalId(),
                playerDtolayer.getStatus() == BGOStatus.online);
            onlinePlayers.add(onlinePlayer);
        }

        return new UpdateOnlinePlayersResponse(
                result.getStatusCode(),
                result.getReasonPhrases(),
                onlinePlayers
        );
    }

    private void validateInputParams(String token, int gameId, long bankId,
                                     long buyIn, String domainUrl) throws CommonException {
        if (StringUtils.isTrimmedEmpty(token)) {
            throw new CommonException("token not presented");
        }
        BattlegroundConfig config = cassandraBattlegroundConfigPersister.getConfig(bankId, gameId);
        if (config == null || !config.isEnabled()) {
            throw new CommonException("wrong gameId");
        }
        if (config.getBuyInsForDefaultCurrency() == null || !config.getBuyInsForDefaultCurrency().contains(buyIn)) {
            throw new CommonException("non-existent buy-in");
        }
        if (domainUrl != null) {
            validateDomainURL(domainUrl);
        }
    }

    private void validateDomainURL(String domainUrl) throws CommonException{
        if (StringUtils.isTrimmedEmpty(domainUrl)) {
            throw new CommonException("domainUrl not presented");
        }
    }

    private void validateAuthResult(CommonWalletAuthResult authResult, String token, long bankId, ClientType clientType) throws CommonException {
        if (authResult == null) {
            throw new CommonException(String.format("User not found. token: %s, bankId: %d, clientType: %s", token, bankId, clientType));
        }
        if (!authResult.isSuccess() || StringUtils.isTrimmedEmpty(authResult.getUserName(), authResult.getCurrency())) {
            throw new CommonException("unsuccessful authentication");
        }
    }

    private UpdateRoomDto createUpdateRoomRequest(PrivateRoom privateRoom, boolean isTransitionLimited) {

        if(privateRoom == null) {
            LOG.error("createUpdateRoomRequest: privateRoom is null");
            return null;
        }

        if(privateRoom.getBankId() == null) {
            LOG.error("createUpdateRoomRequest: privateRoom.getBankId() is null, for privateRoom:{}", privateRoom);
            return null;
        }

        if(StringUtils.isTrimmedEmpty(privateRoom.getPrivateRoomId())) {
            LOG.error("createUpdateRoomRequest: privateRoom.getPrivateRoomId() is empty, for privateRoom:{}", privateRoom);
            return null;
        }

        if(privateRoom.getPlayers() == null) {
            LOG.error("createUpdateRoomRequest: privateRoom.getPlayers() is null, for privateRoom:{}", privateRoom);
            return null;
        }

        List<BGPlayerDto> players = new ArrayList<>();
        for(Player player : privateRoom.getPlayers()) {
            AccountInfo accountInfo = null;
            try {
                accountInfo = accountManager.getByCompositeKey(privateRoom.getBankId(), player.getExternalId());
            } catch (Exception e) {
                LOG.error("createUpdateRoomRequest: exception to get accountInfo for bankId: {}, ExternalId:{}, {} ",
                        privateRoom.getBankId(), player.getExternalId(), e.getMessage(), e);
            }

            long accountId = accountInfo != null ? accountInfo.getId(): 0;
            BGPlayerDto playerDto = new BGPlayerDto(
                player.getNickname(),
                accountId,
                player.getExternalId(),
                toBGStatus(player.getStatus())
            );
            players.add(playerDto);
        }

        int bankId = privateRoom.getBankId() == null ? 0 : privateRoom.getBankId().intValue();
        return new UpdateRoomDto(privateRoom.getPrivateRoomId(), players,  bankId, isTransitionLimited);
    }

    private UpdateFriendsDto createUpdateFriendsRequest(UpdateFriendsRequest updateFriendsRequest) {
        if(updateFriendsRequest == null) {
            LOG.error("createUpdateFriendsRequest: updateFriendsRequest is null");
            return null;
        }

        if(StringUtils.isTrimmedEmpty(updateFriendsRequest.getExternalId())) {
            LOG.error("createUpdateFriendsRequest: updateFriendsRequest.getExternalId() is empty in:{}", updateFriendsRequest);
            return null;
        }

        if(StringUtils.isTrimmedEmpty(updateFriendsRequest.getNickname())) {
            LOG.error("createUpdateFriendsRequest: updateFriendsRequest.getNickname() is empty in:{}", updateFriendsRequest);
            return null;
        }

        if(updateFriendsRequest.getFriends() == null || updateFriendsRequest.getFriends().size() == 0) {
            LOG.error("createUpdateFriendsRequest: updateFriendsRequest.getFriends() is empty in:{}", updateFriendsRequest);
            return null;
        }

        List<BGFriendDto> friends = new ArrayList<>();
        for(Friend friend : updateFriendsRequest.getFriends()) {
            BGFriendDto playerDto = new BGFriendDto(
                    friend.getNickname(),
                    friend.getExternalId(),
                    BGFStatusUtil.toTBGFStatus(friend.getStatus()));
            friends.add(playerDto);
        }

        return new UpdateFriendsDto(
                updateFriendsRequest.getNickname(),
                updateFriendsRequest.getExternalId(),
                friends);
    }

    private UpdateOnlinePlayersDto createUpdateOnlinePlayersRequest(UpdateOnlinePlayersRequest updateOnlinePlayersRequest) {
        if(updateOnlinePlayersRequest == null) {
            LOG.error("createUpdateOnlinePlayersRequest: updateOnlinePlayersRequest is null");
            return null;
        }

        if(updateOnlinePlayersRequest.getOnlinePlayers() == null || updateOnlinePlayersRequest.getOnlinePlayers().isEmpty()) {
            LOG.error("createUpdateOnlinePlayersRequest: updateOnlinePlayersRequest.getOnlinePlayers() is empty in:{}", updateOnlinePlayersRequest);
            return null;
        }
        List<BGOnlinePlayerDto> players = new ArrayList<>();
        for(OnlinePlayer onlinePlayer : updateOnlinePlayersRequest.getOnlinePlayers()) {
            BGOnlinePlayerDto playerDto = new BGOnlinePlayerDto(onlinePlayer.getNickname(),
                                                onlinePlayer.getExternalId(),
                                               BGOStatusUtil.toBGOStatus(StatusOnlinePlayer.findByValue(onlinePlayer.isOnline())));
            players.add(playerDto);
        }

        return new UpdateOnlinePlayersDto(players);
    }

    public BattlegroundPrivateRoomSetting getPrivateRoomSettingsWithoutCreation(String privateRoomId) {
        if(StringUtils.isTrimmedEmpty(privateRoomId)) {
            return null;
        }

        return battlegroundPrivateRoomSettingsPersister.load(privateRoomId);
    }

    public BattlegroundPrivateRoomSetting createPrivateRoomSettings(String privateRoomId) {
        if(StringUtils.isTrimmedEmpty(privateRoomId)) {
            return null;
        }
        try {
            PrivateRoomInfoResultDto roomInfo = kafkaRequestMultiPlayer.getPrivateRoomInfo(new GetPrivateRoomInfoRequest(privateRoomId));
            BattlegroundPrivateRoomSetting setting = new BattlegroundPrivateRoomSetting(
                    privateRoomId,
                    roomInfo.getCurrency(),
                    roomInfo.getGameId(),
                    roomInfo.getBankId(),
                    roomInfo.getBuyIn(),
                    roomInfo.getServerId());

            battlegroundPrivateRoomSettingsPersister.create(setting.getPrivateRoomId(), setting);

            return setting;
        } catch (CommonException e) {
            return null;
        }
    }

    public BattlegroundPrivateRoomSetting getPrivateRoomSettings(String privateRoomId) {
        if(StringUtils.isTrimmedEmpty(privateRoomId)) {
            return null;
        }

        BattlegroundPrivateRoomSetting setting = getPrivateRoomSettingsWithoutCreation(privateRoomId);

        if (setting == null) {
            setting = createPrivateRoomSettings(privateRoomId);
        }

        return setting;
    }

    public Set<String> getParticipationNicknamesBySessionId(String sid, GameSession gameSession) {
        Set<Long> participantsByGameSessionId = battlegroundHistoryPersister.getParticipantsBySID(sid);
        participantsByGameSessionId.addAll(loadParticipantAccountIdsInActiveGameSession(gameSession));
        return convertAccountsToExtId(participantsByGameSessionId);
    }

    public Set<Long> loadParticipantAccountIdsInActiveGameSession(GameSession gameSession) {
        if (gameSession == null) {
            return Collections.emptySet();
        }
        if (!isBattlegroundGame(gameSession.getGameId())) {
            return Collections.emptySet();
        }

        try {
            CollectionResponseDto data = kafkaRequestMultiPlayer
                    .getParticipantAccountIdsInRound(new GetParticipantAccountIdsInRoundDto(gameSession.getAccountId(), gameSession.getId()));
            return data.getValue();
        } catch (CommonException e) {
            return Collections.emptySet();
        }
    }

    public MPGameSessionCloseInfo getParticipationNicknamesByGameSession(GameSession gameSession) {
        List<BattlegroundRoundParticipant> rounds = battlegroundHistoryPersister.getBattlegroundRoundParticipantByGameSessionId(gameSession.getId());

        List<GameRoundEntry> gamesEntry = rounds.stream()
                .map(round -> new GameRoundEntry(round.getRoundId(), convertAccountsToExtId(round.getAccountIds()), round.getStartTime(), round.getEndTime()))
                .collect(Collectors.toList());
        return convert(gameSession, gamesEntry, getSidFromRounds(rounds), getPrivateRoomIdFromRounds(rounds));
    }

    private String getSidFromRounds(List<BattlegroundRoundParticipant> rounds) {
        return rounds != null && rounds.iterator().hasNext() ? rounds.iterator().next().getSid() : null;
    }

    private String getPrivateRoomIdFromRounds(List<BattlegroundRoundParticipant> rounds) {
        return rounds != null && rounds.iterator().hasNext() ? rounds.iterator().next().getPrivateRoomId() : null;
    }

    private MPGameSessionCloseInfo convert(GameSession gameSession, List<GameRoundEntry> gamesEntry, String sid, String privateRoomId) {
        return new MPGameSessionCloseInfo.MPGameSessionCloseInfoBuilder(gameSession.getGameId(), gameSession.getId(), gamesEntry, accountManager.getExtId(gameSession.getAccountId()))
                .setStartTime(gameSession.getStartTime())
                .setEndTime(gameSession.getEndTime())
                .setSid(sid)
                .setPrivateRoomId(privateRoomId)
                .build();
    }

    private Set<String> convertAccountsToExtId(Collection<Long> ids) {
        return cassandraAccountInfoPersister.getByIds(ids).values().stream()
                .map(AccountInfo::getExternalId)
                .collect(Collectors.toSet());
    }

    public String getBattlegroundPrivateRoomIdIfExist(GameSession gameSession) {
        try {
            RoomInfoResultDto data = kafkaRequestMultiPlayer
                    .loadCurrentBattlegroundRoomInfoForPlayer(new ParticipantGameSessionDto(gameSession.getAccountId(), gameSession.getId()));
            return data != null && data.isPrivateRoom() ? data.getPrivateRoomId() : null;
        } catch (CommonException e) {
            return null;
        }
    }

}
