package com.betsoft.casino.mp.web.handlers.lobby;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.betsoft.casino.mp.amazon.model.math.TreasureQuests;
import com.betsoft.casino.mp.common.AbstractGameRoom;
import com.betsoft.casino.mp.common.AchievementHelper;
import com.betsoft.casino.mp.common.math.Paytable;
import com.betsoft.casino.mp.data.persister.*;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.ErrorProcessedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientInfo;
import com.betsoft.casino.mp.model.playerinfo.AbstractBattlegroundRoomPlayerInfo;
import com.betsoft.casino.mp.model.quests.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Currency;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.*;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.abs.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.mp.MQData;
import com.abs.casino.common.mp.MQQuestData;
import com.abs.casino.common.mp.MQTreasureQuestProgress;
import com.abs.casino.common.mp.MQuestPrize;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.kafka.dto.BattlegroundInfoDto;
import com.abs.casino.kafka.dto.CashBonusDto;
import com.abs.casino.kafka.dto.DetailedPlayerInfo2Dto;
import com.abs.casino.kafka.dto.FRBonusDto;
import com.abs.casino.kafka.dto.TournamentInfoDto;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Enter lobby handler. Handle EnterLobby message from client.
 */
@Component
public class EnterLobbyHandler extends MessageHandler<EnterLobby, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(EnterLobbyHandler.class);
    private static final long NO_ROOM = 0;

    private static final String PLAYER_NICKNAME = "MP_USE_NICKNAME_IF_PROVIDED";
    private static final String NICKNAME_ALLOWED_SYMBOLS = "MP_NICKNAME_ALLOWED_SYMBOLS";
    private static final String SEND_REAL_BET_WIN = "CW_SEND_REAL_BET_WIN";
    private static final List<Long> MQB_BANKS = Arrays.asList(6274L, 6275L, 271L, 9128L, 9129L);
    private static final Long MQC_BANK = 6275L;
    private static final String MQC_NICKNAME_SUFFIX = "_MQC";
    private static final List<GameType> ALLOWED_PENDING = Arrays.asList(GameType.BG_DRAGONSTONE ,GameType.BG_MISSION_AMAZON, GameType.DRAGONSTONE, GameType.MISSION_AMAZON, GameType.SECTOR_X, GameType.BG_SECTOR_X, GameType.MAXCRASHGAME, GameType.BG_MAXCRASHGAME);
    private static final Map<String, Integer> NEW_PLAYER_BULLETS_BONUS = ImmutableMap.of(
            "grenade", 3,
            "laser", 1,
            "plasmagun", 2
    );

    private final SocketService socketService;
    private final NicknameValidator validator;
    private final SingleNodeRoomInfoService singleNodeRoomInfoService;
    protected final MultiNodeRoomInfoService multiNodeRoomInfoService;
    private final BGPrivateRoomInfoService bgPrivateRoomInfoService;
    private final MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService;
    private final RoomPlayerInfoService roomPlayerInfoService;
    private final PlayerStatsPersister statsPersister;
    private final PlayerProfilePersister playerProfilePersister;
    private final NicknameService nicknameService;
    private final ActiveFrbSessionPersister activeFrbSessionPersister;
    private final ActiveCashBonusSessionPersister activeCashBonusSessionPersister;
    private final RoundResultNotificationPersister roundResultNotificationPersister;
    private final PlayerQuestsPersister playerQuestsPersister;
    private final CurrencyRateService currencyRateService;
    private final GameConfigPersister gameConfigPersister;
    private final WeaponsPersister weaponService;
    private final TournamentSessionPersister tournamentSessionPersister;
    protected final RoomServiceFactory roomServiceFactory;
    private final ServerConfigService serverConfigService;
    private final CrashGameSettingsService crashGameSettingsService;
    private final RoomPlayersMonitorService roomPlayersMonitorService;

    @SuppressWarnings("rawtypes")
    public EnterLobbyHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                             NicknameValidator validator, SocketService socketService,
                             SingleNodeRoomInfoService singleNodeRoomInfoService, MultiNodeRoomInfoService multiNodeRoomInfoService,
                             BGPrivateRoomInfoService bgPrivateRoomInfoService, MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService,
                             RoomPlayerInfoService roomPlayerInfoService,
                             CassandraPersistenceManager cpm,
                             NicknameService nicknameService, CurrencyRateService currencyRateService,
                             RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                             CrashGameSettingsService crashGameSettingsService,
                             RoomPlayersMonitorService roomPlayersMonitorService) {
        super(gson, lobbySessionService, lobbyManager);
        this.validator = validator;
        this.socketService = socketService;
        this.singleNodeRoomInfoService = singleNodeRoomInfoService;
        this.multiNodeRoomInfoService = multiNodeRoomInfoService;
        this.bgPrivateRoomInfoService = bgPrivateRoomInfoService;
        this.multiNodePrivateRoomInfoService = multiNodePrivateRoomInfoService;
        this.roomPlayerInfoService = roomPlayerInfoService;
        this.statsPersister = cpm.getPersister(PlayerStatsPersister.class);
        this.playerProfilePersister = cpm.getPersister(PlayerProfilePersister.class);
        this.nicknameService = nicknameService;
        this.activeFrbSessionPersister = cpm.getPersister(ActiveFrbSessionPersister.class);
        this.activeCashBonusSessionPersister = cpm.getPersister(ActiveCashBonusSessionPersister.class);
        this.roundResultNotificationPersister = cpm.getPersister(RoundResultNotificationPersister.class);
        this.playerQuestsPersister = cpm.getPersister(PlayerQuestsPersister.class);
        this.currencyRateService = currencyRateService;
        this.gameConfigPersister = cpm.getPersister(GameConfigPersister.class);
        this.weaponService = cpm.getPersister(WeaponsPersister.class);
        this.tournamentSessionPersister = cpm.getPersister(TournamentSessionPersister.class);
        this.roomServiceFactory = roomServiceFactory;
        this.serverConfigService = serverConfigService;
        this.crashGameSettingsService = crashGameSettingsService;
        this.roomPlayersMonitorService = roomPlayersMonitorService;
    }

    @Override
    public void handle(WebSocketSession session, EnterLobby message, ILobbySocketClient client) {
        try {
            innerHandle(session, message, client);
        } catch (ErrorProcessedException e) {
            //nop, error already processed, just return
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    /**
     * Handle EnterLobby message from client. Init/reinit start data for player. (lobbySession, PlayerInfo, ...)
     * @param session web socket session
     * @param message EnterLobby message from client
     * @param client lobby web socket client
     * @throws ErrorProcessedException if any unexpected error occur
     * @throws CommonException if any unexpected error occur
     */
    private void innerHandle(WebSocketSession session, EnterLobby message, ILobbySocketClient client)
            throws ErrorProcessedException, CommonException {

        this.validateServerId(message, client);
        client.setServerId(message.getServerId());

        GameType messageGameType = this.getGameType(message, client);

        boolean isBattleGroundGame = messageGameType.isBattleGroundGame();
        boolean isCrashGame = messageGameType.isCrashGame();
        boolean isCrashPVPGame = isBattleGroundGame && isCrashGame;

        MoneyType modeMoneyType = this.getMoneyType(message, client);

        DetailedPlayerInfo2Dto tPlayerInfo = this.getDetailedPlayerInfoWithSidRecovery(
                message, messageGameType, modeMoneyType, client);

        this.checkPlayerInfo(tPlayerInfo, client, message);

        getLog().debug("innerHandle: accountId={}, tPlayerInfo: {} ", tPlayerInfo.getAccountId(), tPlayerInfo);

        MoneyType messageMoneyType = this.getMoneyType(message, tPlayerInfo);

        Map<String, String> gameSettings = tPlayerInfo.getGameSettings();

        String nickname = MQB_BANKS.contains(tPlayerInfo.getBankId()) ?
                getNicknameWithoutValidation(tPlayerInfo, gameSettings, client) :
                getNicknameWithValidation(tPlayerInfo, gameSettings, client);

        if (MQC_BANK.equals(tPlayerInfo.getBankId()) && nickname.endsWith(MQC_NICKNAME_SUFFIX)) {
            nickname = nickname.substring(0, nickname.length() - MQC_NICKNAME_SUFFIX.length());
        }

        boolean nicknameEditable = !MQB_BANKS.contains(tPlayerInfo.getBankId()) && isNicknameEditable(gameSettings);

        IRoomPlayerInfo roomPlayerInfo = roomPlayerInfoService.get(tPlayerInfo.getAccountId());

        getLog().debug("innerHandle: accountId={}, roomPlayerInfo: {} ", tPlayerInfo.getAccountId(), roomPlayerInfo);

        this.checkPendingOperation(roomPlayerInfo, message, client, messageGameType);

        this.validateAndSitOutPlayer(roomPlayerInfo, messageMoneyType, messageGameType, modeMoneyType, tPlayerInfo, message, client);

        long lockTime = System.currentTimeMillis();
        boolean locked = false;
        try {
            locked = roomPlayerInfoService.tryLock(tPlayerInfo.getAccountId(), 10, TimeUnit.SECONDS);
            getLog().debug("innerHandle: account {} locked {}", tPlayerInfo.getAccountId(), locked);
        } catch (InterruptedException e) {
            getLog().error("innerHandle: Cannot lock roomPlayerInfoService for accountId:{}, interrupted",
                    tPlayerInfo.getAccountId(), e);
        }
        if (!locked) {
            throw new CommonException("Cannot obtain lock by accountId");
        }

        getLog().debug("innerHandle: Lock account, accountId={}, time={}ms",
                tPlayerInfo.getAccountId(), System.currentTimeMillis() - lockTime);

        try {
            //need double-check for right gameType after possible sitOut
            roomPlayerInfo = roomPlayerInfoService.get(tPlayerInfo.getAccountId());

            getLog().debug("innerHandle: Second time accountId={}, messageMoneyType: {}, roomPlayerInfo: {}",
                    tPlayerInfo.getAccountId(), messageMoneyType, roomPlayerInfo);

            this.checkRoomPlayerInfo(roomPlayerInfo, messageGameType, messageMoneyType, message);

            IActiveFrbSession activeFrbSession = this.getActiveFrbSession(tPlayerInfo, message, client, roomPlayerInfo, messageMoneyType);
            getLog().debug("innerHandle: activeFrbSession={}, tPlayerInf={}", activeFrbSession, tPlayerInfo);

            ActiveCashBonusSession activeCashBonusSession = this.getCashBonusSession(tPlayerInfo);
            getLog().debug("innerHandle: activeCashBonusSession={}, tPlayerInfo={}",
                    activeCashBonusSession, tPlayerInfo);

            TournamentSession tournamentSession = this.getTournamentSession(tPlayerInfo);
            getLog().debug("innerHandle: tournamentSession={}, tPlayerInfo={}",
                    tournamentSession, tPlayerInfo);

            List<BattlegroundInfoDto> playerInfoBattlegrounds = tPlayerInfo.getBattlegrounds();
            boolean battlegroundAllowed = false;

            getLog().debug("innerHandle: playerInfoBattlegrounds={}, tPlayerInfo={}",
                    playerInfoBattlegrounds, tPlayerInfo);

            if (playerInfoBattlegrounds != null && !playerInfoBattlegrounds.isEmpty()) {
                battlegroundAllowed = playerInfoBattlegrounds.stream()
                        .anyMatch(tBattlegroundInfo -> tBattlegroundInfo.getGameId() == messageGameType.getGameId());
            }

            getLog().debug("innerHandle: battlegroundAllowed: {}, playerInfoBattlegrounds: {}, messageGameType: {}",
                    battlegroundAllowed,
                    playerInfoBattlegrounds != null ? playerInfoBattlegrounds.size() : null,
                    messageGameType);

            if (battlegroundAllowed && (!isBattleGroundGame || !messageMoneyType.equals(MoneyType.REAL))) {
                getLog().error("innerHandle: Cannot enter lobby, found wrong BG mode," +
                                " battlegroundAllowed={}, isBattleGroundGame={}, moneyType={}",
                        battlegroundAllowed, isBattleGroundGame, messageMoneyType);
                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Cannot enter lobby: found wrong BG mode", message.getRid());
                return;
            }

            if (isBattleGroundGame && !battlegroundAllowed) {
                getLog().error("innerHandle: Cannot launch battleground game in non BG mode," +
                                " battlegroundAllowed={}, isBattleGroundGame={}", battlegroundAllowed, isBattleGroundGame);
                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Cannot launch battleground game in non BG mode", message.getRid());
                return;
            }

            if (isCrashPVPGame && message.getBattlegroundBuyIn() == 0) {
                getLog().error("innerHandle: Cannot launch battleground game getBattlegroundBuyIn is zero," +
                        " isCrashPVPGame={}, message.getBattlegroundBuyIn()={}", isCrashPVPGame, message.getBattlegroundBuyIn());
                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Cannot launch battleground game getBattlegroundBuyIn is zero", message.getRid());
                return;
            }

            PlayerInfo playerInfo = new PlayerInfo(
                    tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(), tPlayerInfo.getExternalId(),
                    tPlayerInfo.getUserName(),
                    new Currency(tPlayerInfo.getCurrency(), tPlayerInfo.getCurrencySymbol()),
                    tPlayerInfo.isGuest(), tPlayerInfo.isShowRefreshBalanceButton());

            client.setPlayerInfo(playerInfo);
            client.setSessionId(message.getSid());
            client.setLang(message.getLang());
            client.setMoneyType(messageMoneyType);
            client.setPrivateRoom(message.isPrivateRoom());

            this.loadStats(playerInfo, messageGameType, client.getMoneyType());

            getLog().debug("innerHandle: playerInfo={}", playerInfo);

            long roomId = -1;

            LobbySession lobbySession = this.getLobbySession(tPlayerInfo, messageMoneyType, message, client);
            getLog().debug("innerHandle: lobbySession={}, tPlayerInfo={}", lobbySession, tPlayerInfo);

            boolean allowWeaponSaveInAllGames = this.isAllowWeaponSaveInAllGames(messageMoneyType, gameSettings);
            getLog().debug("innerHandle: allowWeaponSaveInAllGames={}", allowWeaponSaveInAllGames);

            this.restoreLobbySession(client, tPlayerInfo, messageMoneyType, activeCashBonusSession, roomId, lobbySession, allowWeaponSaveInAllGames);

            PlayerProfile playerProfile = playerProfilePersister.load(playerInfo.getBankId(), playerInfo.getAccountId());
            getLog().debug("innerHandle: bankId={}, accountId={}, playerProfile={}",
                    playerInfo.getBankId(), playerInfo.getAccountId(), playerProfile);

            boolean needPlayerStartBonus = this.processStartBonus(messageGameType, messageMoneyType, gameSettings, playerProfile, tPlayerInfo, playerInfo);

            playerProfile = this.createNewPlayerProfileIfNeed(playerProfile, tPlayerInfo);

            getLog().debug("innerHandle: playerProfile={}", playerProfile);

            Avatar avatar = new Avatar(playerProfile.getBorder(), playerProfile.getHero(), playerProfile.getBackground());
            float alreadySitInStake = 0;
            Long actualBattlegroundBuyIn = message.getBattlegroundBuyIn();

            roomPlayerInfo = roomPlayerInfoService.get(playerInfo.getAccountId());

            getLog().debug("innerHandle: accountId={}, avatar={}, roomPlayerInfo={}",
                    playerInfo.getAccountId(), avatar, roomPlayerInfo);

            if (roomPlayerInfo != null) {

                roomId = roomPlayerInfo.getRoomId();
                IRoomInfo roomInfo = this.getRoomInfo(roomId);

                getLog().debug("innerHandle: roomId={}, roomInfo={}", roomId, roomInfo);

                if (isCrashPVPGame && !message.isContinueIncompleteRound()
                        && roomInfo != null && roomInfo.getStake().toCents() != message.getBattlegroundBuyIn()) {

                    getLog().debug("innerHandle: Cannot launch battleground buyins are differ, old: {}, new: {}",
                            roomInfo.getStake(), message.getBattlegroundBuyIn());

                    this.sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Cannot launch battleground buyins are differ",
                            message.getRid());

                } else if (isBattleGroundGame && roomInfo != null && message.isContinueIncompleteRound()) {

                    actualBattlegroundBuyIn = roomInfo.getStake().toCents();
                    getLog().debug("innerHandle: Find incomplete btg, change battlegroundBuyIn: {}", actualBattlegroundBuyIn);

                }

                if (roomInfo != null) {
                    client.setPrivateRoom(roomInfo.isPrivateRoom());
                }

                alreadySitInStake = roomPlayerInfo.getStake();
            }

            if (message.isContinueIncompleteRound() && actualBattlegroundBuyIn != null && actualBattlegroundBuyIn == 1) {
                getLog().error("innerHandle: BG round already finished: isContinueIncompleteRound={}, actualBattlegroundBuyIn={}",
                        message.isContinueIncompleteRound(), actualBattlegroundBuyIn);
                this.sendErrorMessage(client, ErrorCodes.BG_ROUND_ALREADY_FINISHED, "BG round already finished", message.getRid());
                return;
            }

            if (lobbySession != null) {

                getLog().debug("innerHandle: Need replace old socketClient in lobbySession: {}", lobbySession);

                //need replace old socketClient
                lobbySession.setSocketClient(client);
                lobbySession.setWebsocketSessionId(session.getId());
                lobbySession.setRoomId(roomId);
                lobbySession.setSendRealBetWin(this.isSendRealBetWin(gameSettings, messageGameType));

                getLog().debug("innerHandle: After replace old socketClient in lobbySession: {}", lobbySession);

                this.processLobbySessionIfFRBMode(messageMoneyType, activeFrbSession, lobbySession, tPlayerInfo.getBalance());

                getLog().debug("innerHandle: 1 registerPlayer with nickname: {}, playerInfo: {}, client: {}",
                        nickname, playerInfo, client);

                lobbyManager.registerPlayer(playerInfo, nickname, client);

            } else {

                getLog().debug("innerHandle: 2 registerPlayer with nickname: {}, playerInfo: {}, client: {}",
                        nickname, playerInfo, client);

                lobbyManager.registerPlayer(playerInfo, nickname, client);

                List<Long> stakes = this.getStakes(tPlayerInfo);
                getLog().debug("innerHandle: stakes after removing: {}", stakes);

                this.checkCurrencyRateForEUR(tPlayerInfo);

                boolean activeBGRound = isBattleGroundRoom(tPlayerInfo);

                lobbySession = new LobbySession(message.getSid(), playerInfo.getAccountId(),
                        playerInfo.getBankId(),
                        client.getNickname(),
                        avatar,
                        System.currentTimeMillis(),
                        tPlayerInfo.getBalance(),
                        roomId,
                        client,
                        new Currency(tPlayerInfo.getCurrency(), tPlayerInfo.getCurrencySymbol()),
                        tPlayerInfo.isShowRefreshBalanceButton(),
                        stakes,
                        this.getStakesReserve(messageGameType, gameSettings),
                        this.getStakesLimit(messageGameType, gameSettings),
                        tPlayerInfo.getLbContribution(),
                        messageGameType.getGameId(),
                        this.getMaxQuestWeaponMode(messageGameType, gameSettings),
                        allowWeaponSaveInAllGames,
                        messageMoneyType,
                        tournamentSession == null && activeCashBonusSession == null && !activeBGRound ? activeFrbSession : null,
                        activeCashBonusSession,
                        tournamentSession
                );

                lobbySession.setNicknameEditable(nicknameEditable);
                lobbySession.setWebsocketSessionId(session.getId());
                lobbySession.setSendRealBetWin(this.isSendRealBetWin(gameSettings, messageGameType));

                getLog().debug("innerHandle: New lobbySession: {}", lobbySession);

            }

            getLog().debug("innerHandle: roomId={}", roomId);
            if (roomId > NO_ROOM) {
                this.checkCurrentRoomAndFixInconsistencies(roomId, messageMoneyType, message, lobbySession, client, roomPlayerInfo);
            }

            lobbySession.setBattlegroundAllowed(battlegroundAllowed);

            BattlegroundInfoDto currentTBattlegroundInfo =
                    this.getCurrentTBattlegroundInfo(playerInfoBattlegrounds, lobbySession.getGameId());

            getLog().debug("innerHandle: battlegroundAllowed={}, isCrashPVPGame={}, currentTBattlegroundInfo={}",
                    battlegroundAllowed, isCrashPVPGame, currentTBattlegroundInfo);

            if (battlegroundAllowed && currentTBattlegroundInfo != null) {

                if (isCrashPVPGame) {

                    if (!currentTBattlegroundInfo.getBuyIns().contains(actualBattlegroundBuyIn)) {

                        getLog().error("innerHandle: Cannot launch battleground game getBattlegroundBuyIn is wrong," +
                                "actualBattlegroundBuyIn={}, currentTBattlegroundInfo.getBuyIns()={}",
                                actualBattlegroundBuyIn, currentTBattlegroundInfo.getBuyIns());
                        this.sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Cannot launch battleground game getBattlegroundBuyIn " +
                                        "is wrong", message.getRid());
                        return;
                    }

                    List<Long> battlegroundBuyIns = Collections.singletonList(actualBattlegroundBuyIn);
                    lobbySession.setBattlegroundBuyIns(battlegroundBuyIns);

                } else {
                    lobbySession.setBattlegroundBuyIns(currentTBattlegroundInfo.getBuyIns());
                }

                lobbySession.setBattlegroundRakePercent(currentTBattlegroundInfo.getRake());
                lobbySession.setPrivateRoom(message.isPrivateRoom());
            }

            lobbySession.setExternalId(tPlayerInfo.getExternalId());

            getLog().debug("innerHandle: Add lobbySession={}", lobbySession);
            lobbySessionService.add(lobbySession);

            client.setGameType(messageGameType);
            client.startTouchSession(socketService, client.getServerId(), message.getSid());
            client.startBalanceUpdater(socketService, client.getServerId(), message.getSid(), lobbySessionService);

            this.initPlayerQuests(lobbySession.getStakes(), messageGameType, messageMoneyType, client.getBankId(),
                    client.getAccountId(), tournamentSession, activeFrbSession, activeCashBonusSession);

            this.saveSocketClientInfo(client, lobbySession.getCurrency(), actualBattlegroundBuyIn);

            this.sendResponse(
                    session,
                    message,
                    client,
                    playerInfo,
                    roomId,
                    avatar,
                    playerProfile,
                    getPaytable(messageGameType),
                    lobbySession,
                    alreadySitInStake,
                    needPlayerStartBonus,
                    getNicknameGlyphs(gameSettings),
                    playerInfoBattlegrounds
            );

        } catch (Exception e) {
            getLog().error("innerHandle: Cannot enter lobby", e);
            sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Cannot enter lobby: " + e.getMessage(),
                    message.getRid());
        } finally {
            roomPlayerInfoService.unlock(tPlayerInfo.getAccountId());
            getLog().debug("innerHandle: accountId {} unlocked", tPlayerInfo.getAccountId());
        }
    }

    private void saveSocketClientInfo(ILobbySocketClient client,  ICurrency currency, long buyInStack) {

        if(client == null) {
            getLog().error("saveSocketClientInfo: client is null");
            return;
        }

        if(StringUtils.isTrimmedEmpty(client.getWebSocketSessionId())) {
            getLog().error("saveSocketClientInfo: client.getWebSocketSessionId() is empty: {}", client);
            return;
        }

        GameType gameType = client.getGameType();

        if(gameType == null) {
            getLog().error("saveSocketClientInfo: gameType is null in ILobbySocketClient: {}", client);
            return;
        }

        try {
            String playerExternalId = null;
            IPlayerInfo playerInfo = client.getPlayerInfo();
            if (playerInfo != null) {
                playerExternalId = playerInfo.getExternalId();
            }

            String currencyCode = currency != null ? currency.getCode() : "";

            SocketClientInfo socketClientInfo = roomPlayersMonitorService.convert(client, buyInStack, -1,
                    playerExternalId, false, -1, false, currencyCode);

            roomPlayersMonitorService.upsertSocketClientInfo(socketClientInfo);

        } catch (Exception exception) {
            getLog().error("saveSocketClientInfo: exception: {}", exception.getMessage(), exception);
        }
    }

    /**
     * Checks current room data and if data incorrect or old fix it.
     * @param roomId roomId
     * @param moneyType moneyType
     * @param message EnterLobby message
     * @param lobbySession lobby session
     * @param client lobby socket client
     * @param roomPlayerInfo room player info
     */
    private void checkCurrentRoomAndFixInconsistencies(long roomId, MoneyType moneyType, EnterLobby message,
                                                       LobbySession lobbySession, ILobbySocketClient client,
                                                       IRoomPlayerInfo roomPlayerInfo) {
        IRoomInfo roomInfo = this.getRoomInfo(roomId);

        getLog().debug("checkCurrentRoomAndFixInconsistencies: roomInfo: {}", roomInfo);

        if (roomInfo != null) {

            if (roomPlayerInfo != null && lobbySession != null && !roomPlayerInfo.getNickname().equals(lobbySession.getNickname())) {
                getLog().warn("checkCurrentRoomAndFixInconsistencies: found nickName inconsistency, roomPlayerInfo.nickName={}, " +
                                "lobbySession.nickname={}, set as roomPlayerInfo",
                        roomPlayerInfo.getNickname(), lobbySession.getNickname());
                lobbySession.setNickname(roomPlayerInfo.getNickname());
            }

            int buyInCount;
            long roundBuyInAmount;

            if (roomPlayerInfo != null && roomInfo.isBattlegroundMode()) {

                buyInCount = roomPlayerInfo.getBuyInCount();
                roundBuyInAmount = roomPlayerInfo.getRoundBuyInAmount();

                if (buyInCount > 0 && roundBuyInAmount > 0) {
                    lobbySession.setConfirmBattlegroundBuyIn(true);
                    getLog().debug("checkCurrentRoomAndFixInconsistencies account:{} " +
                                    "restore state setConfirmBattlegroundBuyIn, roomId:{}",
                            roomPlayerInfo.getId(), roomId);
                }

                if (roomPlayerInfo instanceof AbstractBattlegroundRoomPlayerInfo) {
                    AbstractBattlegroundRoomPlayerInfo castedRoomInfo = (AbstractBattlegroundRoomPlayerInfo) roomPlayerInfo;
                    lobbySession.setPrivateRoom(castedRoomInfo.isPrivateRoom());
                    lobbySession.setOwner(castedRoomInfo.isOwner());
                }

            }

            if (roomInfo.getMoneyType().name().equalsIgnoreCase(message.getMode())
                    || (moneyType.equals(MoneyType.TOURNAMENT) && isMessageRealMode(message))
                    || (moneyType.equals(MoneyType.CASHBONUS) && isMessageRealMode(message))
                    || (roomInfo.isBattlegroundMode() && !isMessageRealMode(message))) {

                client.setMoneyType(roomInfo.getMoneyType());
                getLog().debug("Seater found in roomId={}, return roomId for reconnect", roomId);

            } else {

                if (getLog().isDebugEnabled()) {

                    getLog().debug("Seater found in roomId={}, but found mode mismatch, roomInfo={}, " +
                                    "EnterLobby.mode={}. Not reconnect", roomId,
                            roomInfo.getMoneyType().name(), message.getMode());

                }

                roomId = -1;
                lobbySession.setRoomId(roomId);
            }
        } else {

            roomId = -1;
            lobbySession.setRoomId(roomId);

        }
    }

    private boolean isMessageRealMode(EnterLobby message) {
        return "real".equalsIgnoreCase(message.getMode());
    }

    private void processLobbySessionIfFRBMode(MoneyType moneyType, IActiveFrbSession activeFrbSession,
                                              LobbySession lobbySession, long balance) {

        getLog().debug("processLobbySessionIfFRBMode: moneyType={}, activeFrbSession={}, lobbySession={}, balance={}",
                moneyType, activeFrbSession, lobbySession, balance);

        if (moneyType.equals(MoneyType.FRB) && activeFrbSession != null &&
                lobbySession.getActiveFrbSession() == null) {

            getLog().debug("processLobbySessionIfFRBMode: found new active FRB, update lobbySession, activeFrbSession: {}",
                    activeFrbSession);
            lobbySession.setActiveFrbSession(activeFrbSession);
            MoneyType sessionMoneyType = lobbySession.getMoneyType();

            if (!sessionMoneyType.equals(moneyType)) {
                getLog().debug("processLobbySessionIfFRBMode: update lobbySession sessionMoneyType: {}, moneyType: {}",
                        sessionMoneyType, moneyType);

                lobbySession.setMoneyType(moneyType);
                lobbySession.setBalance(balance);
            }

            if (lobbySession.getActiveCashBonusSession() != null &&
                    !lobbySession.getActiveCashBonusSession().isActive()) {
                getLog().debug("processLobbySessionIfFRBMode: setActiveCashBonusSession to null int " +
                        "lobbySession: {}", lobbySession);
                lobbySession.setActiveCashBonusSession(null);
            }

            lobbySession.setTournamentSession(null);
        }

    }

    private void checkCurrencyRateForEUR(DetailedPlayerInfo2Dto tPlayerInfo) {
        double rateForEUR = tPlayerInfo.getCurrencyRateForEUR();
        CurrencyRate currencyRateEUR = currencyRateService.get(tPlayerInfo.getCurrency(), "EUR");
        if (rateForEUR > 0 && currencyRateEUR.getRate() < 0) {
            CurrencyRate rate = new CurrencyRate(
                    tPlayerInfo.getCurrency(),
                    "EUR",
                    rateForEUR,
                    System.currentTimeMillis());
            roomPlayerInfoService.getNotifyService().executeOnAllMembers(
                    new UpdateCurrencyTask(Collections.singleton(rate)
                    ));
            getLog().debug("update rate {}", rate);
        }
    }

    /**
     * Return amount stake reserve for start of buyIn
     * @param gameType gameType
     * @param gameSettings game settings from gs side.
     * @return amount stake reserve
     */
    private int getStakesReserve(GameType gameType, Map<String, String> gameSettings) {
        int stakesReserve = GameType.getStakesReserve(gameType);
        if (gameSettings != null && !gameSettings.isEmpty()) {
            String reserve = getGameSettingValue(gameSettings, BaseGameConstants.KEY_MQ_STAKES_RESERVE,
                    BaseGameConstants.KEY_ABS_STAKES_RESERVE);
            if (!StringUtils.isTrimmedEmpty(reserve)) {
                stakesReserve = Integer.parseInt(reserve);
            }
        }
        return stakesReserve;
    }

    /**
     * Return amount stake limit for start of buyIn
     * @param gameType gameType
     * @param gameSettings game settings from gs side.
     * @return amount stake limit
     */
    private int getStakesLimit(GameType gameType, Map<String, String> gameSettings) {
        int stakesLimit = GameType.getStakesLimit(gameType);

        if (gameSettings != null && !gameSettings.isEmpty()) {
            String limit = getGameSettingValue(gameSettings, BaseGameConstants.KEY_MQ_STAKES_LIMIT,
                    BaseGameConstants.KEY_ABS_STAKES_LIMIT);
            if (!StringUtils.isTrimmedEmpty(limit)) {
                stakesLimit = Integer.parseInt(limit);
            }
        }
        return stakesLimit;
    }

    private MaxQuestWeaponMode getMaxQuestWeaponMode(GameType gameType, Map<String, String> gameSettings) {
        return MaxQuestWeaponMode.LOOT_BOX;
    }

    /**
     * Checks and create player profile if it needed.
     * @param currentProfile currentProfile
     * @param tPlayerInfo tPlayerInfo from gs side
     * @return actual player profile
     */
    private PlayerProfile createNewPlayerProfileIfNeed(PlayerProfile currentProfile, DetailedPlayerInfo2Dto tPlayerInfo) {

        getLog().debug("createNewPlayerProfileIfNeed: currentProfile={}", currentProfile);

        if (currentProfile != null) {
            return currentProfile;
        }

        PlayerProfile newProfile = new PlayerProfile(new HashSet<>(), new HashSet<>(), new HashSet<>(),
                AvatarParts.BORDER.getDefaultPartId(), AvatarParts.HERO.getDefaultPartId(),
                AvatarParts.BACKGROUND.getDefaultPartId(), false);

        playerProfilePersister.save(tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(), newProfile);

        getLog().debug("createNewPlayerProfileIfNeed: newProfile={}", newProfile);

        return newProfile;
    }

    /**
     * Updates and restore data of old lobby sessions from actual data
     * @param client lobby socket client
     * @param tPlayerInfo actual player info from gs
     * @param moneyType MoneyType
     * @param activeCashBonusSession cash bonus session
     * @param roomId roomId
     * @param lobbySession lobby session of player
     * @param allowWeaponSaveInAllGames  true if needed to save special weapons between rounds and after sitout. (for old games)
     */
    private void restoreLobbySession(ILobbySocketClient client, DetailedPlayerInfo2Dto tPlayerInfo, MoneyType moneyType,
                                     ActiveCashBonusSession activeCashBonusSession, long roomId,
                                     LobbySession lobbySession, boolean allowWeaponSaveInAllGames) {

        getLog().debug("restoreLobbySession: first lobbySession={}", lobbySession);

        if (lobbySession != null) {
            if (moneyType.equals(MoneyType.REAL)) {

                getLog().debug(" restoreLobbySession: switch to real mode lobbySession: {},  moneyType: {}",
                        lobbySession, moneyType);

                lobbySession.setActiveCashBonusSession(null);
                lobbySession.setActiveFrbSession(null);
                lobbySession.setMoneyType(moneyType);
                lobbySession.setBalance(tPlayerInfo.getBalance());
                lobbySession.setStakes(getStakes(tPlayerInfo));
                lobbySession.setAllowWeaponSaveInAllGames(allowWeaponSaveInAllGames);
                lobbySession.setStakes(getStakes(tPlayerInfo));
                client.stopBalanceUpdater();

            } else if (moneyType.equals(MoneyType.CASHBONUS) && activeCashBonusSession != null) {

                if (lobbySession.getActiveCashBonusSession() != null &&
                        lobbySession.getActiveCashBonusSession().getId() != activeCashBonusSession.getId()) {

                    getLog().warn("Start new cashBonus session but found other unfinished cashBonus " +
                                    "session, lobbySession={}, new bonus={}", lobbySession,
                            activeCashBonusSession);
                    lobbySession.setActiveCashBonusSession(activeCashBonusSession);
                    lobbySession.setRoomId(roomId);
                    lobbySession.setAllowWeaponSaveInAllGames(true);

                } else {
                    getLog().debug("Start new cashBonus session, active bonus.id is equals with " +
                            "lobby.activeCashBonusSession");
                }
            }
        }

        getLog().debug("restoreLobbySession: second lobbySession={}", lobbySession);
    }

    private boolean processStartBonus(GameType gameType, MoneyType moneyType, Map<String, String> gameSettings,
                                      PlayerProfile playerProfile, DetailedPlayerInfo2Dto tPlayerInfo,
                                      PlayerInfo playerInfo) {
        boolean needPlayerStartBonus = false;

        getLog().debug("processStartBonus: gameType={}, accountId={}", gameType, playerInfo.getAccountId());

        if (GameType.AMAZON.equals(gameType) || GameType.PIRATES.equals(gameType)) {

            getLog().debug("processStartBonus: playerProfile={}, gameSettings={}, accountId={}",
                    playerProfile, gameSettings, playerInfo.getAccountId());

            String startBonusSetting = getGameSettingValue(gameSettings, BaseGameConstants.KEY_MQ_AWARD_PLAYER_START_BONUS,
                    BaseGameConstants.KEY_ABS_AWARD_PLAYER_START_BONUS);
            if (playerProfile == null && !StringUtils.isTrimmedEmpty(startBonusSetting)) {
                if (Boolean.parseBoolean(startBonusSetting)) {

                    initPlayerStartBonusWeapons(tPlayerInfo, gameType.getGameId());
                    needPlayerStartBonus = isNeedStartBonus(playerInfo) && moneyType == MoneyType.REAL;

                }
            } else if (moneyType == MoneyType.REAL) {
                needPlayerStartBonus = isNeedStartBonus(playerInfo);
            }
        }
        return needPlayerStartBonus;
    }

    private String getGameSettingValue(Map<String, String> gameSettings, String key, String aliasKey) {
        if (gameSettings == null || gameSettings.isEmpty()) {
            return null;
        }
        String value = gameSettings.get(key);
        if (!StringUtils.isTrimmedEmpty(value)) {
            return value;
        }
        return gameSettings.get(aliasKey);
    }

    private boolean isAllowWeaponSaveInAllGames(MoneyType moneyType, Map<String, String> gameSettings) {

        getLog().debug("isAllowWeaponSaveInAllGames: moneyType={}, gameSettings={}", moneyType, gameSettings);

        return (moneyType != MoneyType.REAL && moneyType != MoneyType.FREE)
                || (gameSettings != null && gameSettings.get(BankInfo.KEY_ROUND_WINS_WITHOUT_BETS_ALLOWED)
                .equalsIgnoreCase(Boolean.TRUE.toString()));
    }

    private String getNicknameGlyphs(Map<String, String> gameSettings) {
        String nicknameGlyphs = validator.getDefaultAllowedSymbols();
        if (gameSettings != null && gameSettings.containsKey(NICKNAME_ALLOWED_SYMBOLS)) {
            nicknameGlyphs += gameSettings.get(NICKNAME_ALLOWED_SYMBOLS);
        }
        return nicknameGlyphs;
    }

    /**
     * Checks lobby session for player and return error if session data is not compatible with player info data and  message from client
     * @param tPlayerInfo player info from gs
     * @param moneyType moneyType
     * @param message message from client
     * @param client lobby socket client
     * @return actual lobby session
     * @throws ErrorProcessedException error if lobby session incompatible with input data
     */
    private LobbySession getLobbySession(DetailedPlayerInfo2Dto tPlayerInfo, MoneyType moneyType,
                                         EnterLobby message, ILobbySocketClient client) throws ErrorProcessedException {
        LobbySession lobbySession = lobbySessionService.get(message.getSid());

        getLog().debug("getLobbySession: SID={}, lobbySession={}", message.getSid(), lobbySession);

        if (!isExistingLobbySessionCompatibleWithNew(message, tPlayerInfo, moneyType)) {
            getLog().error("getLobbySession: Found unclosed lobbySession, exit. lobbySession={}", lobbySession);
            sendErrorMessage(client, ErrorCodes.FOUND_OPEN_LOBBY_SESSION, "Cannot enter lobby: " +
                    "found conflicting lobby session", message.getRid());
            throw new ErrorProcessedException();
        }

        return lobbySession;
    }

    /**
     * Checks roomPlayerInfo and return error if  data is not correct
     * @param roomPlayerInfo room player info
     * @param messageGameType gameType
     * @param messageMoneyType moneyType
     * @throws CommonException error if room player info incompatible with room
     */
    private void checkRoomPlayerInfo(IRoomPlayerInfo roomPlayerInfo, GameType messageGameType, MoneyType messageMoneyType, EnterLobby message)
            throws CommonException {

        getLog().debug("checkRoomPlayerInfo: roomPlayerInfo={}, messageGameType: {}, messageMoneyType: {}",
                roomPlayerInfo, messageGameType, messageMoneyType);

        if (roomPlayerInfo != null) {

            getLog().debug("checkRoomPlayerInfo: found roomPlayerInfo: {}", roomPlayerInfo);
            IRoomInfo roomInfo = getRoomInfo(roomPlayerInfo.getRoomId());

            getLog().debug("checkRoomPlayerInfo: roomId={}, roomInfo={}", roomPlayerInfo.getRoomId(), roomInfo);

            if (roomInfo == null) {
                getLog().error("checkRoomPlayerInfo: Found roomPlayerInfo with non-existent roomId: {}",
                        roomPlayerInfo.getRoomId());
                throw new CommonException("SitOut failed");
            }

            getLog().debug("checkRoomPlayerInfo: messageGameType={}, roomInfo.getGameType()={}, messageMoneyType={}, " +
                            "roomInfo.getMoneyType()={}", messageGameType, roomInfo.getGameType(), messageMoneyType,
                    roomInfo.getMoneyType());

            boolean isBattlegroundRoom = isBattlegroundRoom(roomInfo);

            long roomStake = roomInfo.getStake().toCents();
            long messageBuyIn = message.getBattlegroundBuyIn();

            if ( !messageGameType.equals(roomInfo.getGameType())
                    || !messageMoneyType.equals(roomInfo.getMoneyType())
                    || (isBattlegroundRoom && !message.isContinueIncompleteRound() && roomStake != messageBuyIn)
                    || (roomInfo.isPrivateRoom() && roomInfo.isDeactivated())
            ) {
                getLog().error("checkRoomPlayerInfo: SitOut failed messageGameType={}, roomInfo.getGameType()={}," +
                        " messageMoneyType={}, roomInfo.getMoneyType()={}, message.isContinueIncompleteRound()={}," +
                        " roomStake={}, messageBuyIn={}",
                        messageGameType, roomInfo.getGameType(), messageMoneyType, roomInfo.getMoneyType(),
                        message.isContinueIncompleteRound(), roomStake, messageBuyIn);
                throw new CommonException("SitOut failed");
            }
        }
    }

    private boolean isBattlegroundRoom(IRoomInfo roomInfo) {
        return roomInfo != null && roomInfo.isBattlegroundMode();
    }

    private boolean isBattleGroundRoom(DetailedPlayerInfo2Dto tPlayerInfo) {
        if (tPlayerInfo == null) {
            return false;
        }
        IRoomPlayerInfo roomPlayerInfo = roomPlayerInfoService.get(tPlayerInfo.getAccountId());
        if (roomPlayerInfo != null) {
            IRoomInfo room = getRoomInfo(roomPlayerInfo.getRoomId());
            return isBattlegroundRoom(room);
        }
        return false;
    }

    /**
     * Returns paytable data for clients (part if EnterLobbyResponse)
     * @param gameType gameType
     * @return actual paytable for game
     */
    private Paytable getPaytable(GameType gameType) {
        switch (gameType) {
            case PIRATES:
                return com.betsoft.casino.mp.pirates.model.math.PayTableInst.getTable();
            case PIRATES_POV:
            case DMC_PIRATES:
                return com.betsoft.casino.mp.piratescommon.model.math.PayTableInst.getTable();
            case AMAZON:
                return com.betsoft.casino.mp.amazon.model.math.PayTableInst.getTable();
            case REVENGE_OF_RA:
                return com.betsoft.casino.mp.revengeofra.model.math.PayTableInst.getTable();
            case DRAGONSTONE:
                return com.betsoft.casino.mp.dragonstone.model.math.PayTableInst.getTable();
            case BG_DRAGONSTONE:
                return com.betsoft.casino.mp.bgdragonstone.model.math.PayTableInst.getTable();
            case CLASH_OF_THE_GODS:
                return com.betsoft.casino.mp.clashofthegods.model.math.PayTableInst.getTable();
            case MISSION_AMAZON:
                return com.betsoft.casino.mp.missionamazon.model.math.PayTableInst.getTable();
            case BG_MISSION_AMAZON:
                return com.betsoft.casino.mp.bgmissionamazon.model.math.PayTableInst.getTable();
            case SECTOR_X:
                return com.betsoft.casino.mp.sectorx.model.math.PayTableInst.getTable();
            case BG_SECTOR_X:
                return com.betsoft.casino.mp.bgsectorx.model.math.PayTableInst.getTable();
            default:
                return null;
        }
    }

    private void closeOldSocketConnectionsIfExist(Collection<LobbySession> playerLobbySessions, DetailedPlayerInfo2Dto tPlayerInfo,
                                            EnterLobby message, ILobbySocketClient client) throws ErrorProcessedException, CommonException {
        if (
                (playerLobbySessions!= null) &&
                        (
                                playerLobbySessions.size() > 1 ||
                                (
                                        playerLobbySessions.size() == 1 &&
                                        !playerLobbySessions.iterator().next().getSessionId().equals(message.getSid())))
        ) {

            getLog().debug("closeOldSocketConnectionsIfExist: Found unclosed old lobbySessions for accountId: {} " +
                    "sessions: {}", tPlayerInfo.getAccountId(), playerLobbySessions.size());

            for (LobbySession lobbySession : playerLobbySessions) {

                getLog().debug("closeOldSocketConnectionsIfExist: Unclosed old lobbySession for accountId: {} " +
                        "session: {}", tPlayerInfo.getAccountId(), lobbySession);

                String sessionId = lobbySession.getSessionId();

                if (!sessionId.equals(message.getSid())) {

                    ILobbySocketClient oldClient = (ILobbySocketClient)
                            lobbySessionService.get(sessionId).getSocketClient();

                    if (oldClient != null) {
                        sendErrorMessage(oldClient, ErrorCodes.INVALID_SESSION, "New Lobby session is opening", message.getRid());
                    }
                }
            }

            CloseConnectionTask closeConnectionTask =
                    new CloseConnectionTask(tPlayerInfo.getAccountId(), message.getSid());

            roomPlayerInfoService
                    .getNotifyService()
                    .executeOnAllMembers(closeConnectionTask);

            //wait up to 10 sec for closeConnection process end
            getLog().debug("closeOldSocketConnectionsIfExist: Wait up to 10 sec for closeConnection process end, closeConnectionTask:{}", closeConnectionTask);

            Collection<LobbySession> lobbySessions = null;

            try {

                int count = 100;

                while (count-- > 0) {

                    Thread.sleep(100);

                    lobbySessions = lobbySessionService.getByAccountId(tPlayerInfo.getAccountId());

                    getLog().debug("closeOldSocketConnectionsIfExist: waiting for closeConnection, " +
                            "lobbySessions={}", lobbySessions.size());

                    if (lobbySessions.isEmpty() || (lobbySessions.size() == 1 &&
                            lobbySessions.iterator().next().getSessionId().equals(message.getSid()))) {
                        break;
                    }
                }

            } catch (InterruptedException e) {
                getLog().error("closeOldSocketConnectionsIfExist: Check connections after close connection failed", e);
                sendErrorMessage(client, ErrorCodes.NEED_SITOUT, "Close connection failed", message.getRid());
                Thread.currentThread().interrupt();
                throw new ErrorProcessedException();
            }

            getLog().debug("closeOldSocketConnectionsIfExist: waiting closeConnection is finished, lobbySessions={}", lobbySessions);
        }
    }

    private void removeRoomPlayerInfo(IRoomPlayerInfo roomPlayerInfo, IRoomInfo roomInfo) {
        IRoomInfoService roomInfoService = this.getRoomInfoService(roomInfo);

        getLog().debug("removeRoomPlayerInfo: roomInfoService={} identified for roomInfo={}",
                roomInfoService, roomInfo);

        if(roomInfoService != null) {
            roomPlayerInfoService.remove(roomInfoService, roomPlayerInfo.getRoomId(), roomPlayerInfo.getId());
            getLog().debug("removeRoomPlayerInfo: roomPlayerInfoService has removed " +
                    "roomPlayerInfo={}", roomPlayerInfo);
        }
    }

    private void removeRoomPlayerInfoIfNoSeatFound(IRoomPlayerInfo roomPlayerInfo, IRoomInfo roomInfo) throws CommonException {

        if(roomPlayerInfo == null) {
            getLog().error("removeRoomPlayerInfoIfNoSeatFound: roomPlayerInfo is null");
            return;
        }

        if(roomInfo == null) {
            getLog().error("removeRoomPlayerInfoIfNoSeatFound: roomInfo is null");
            return;
        }

        IRoom room = roomServiceFactory.getRoomWithoutCreation(roomInfo.getGameType(), roomInfo.getId());

        getLog().debug("removeRoomPlayerInfoIfNoSeatFound: the room found for roomId={} is room={}",
                roomInfo.getId(), room);

        if( room == null) {
            getLog().warn("removeRoomPlayerInfoIfNoSeatFound: remove roomPlayerInfo from roomPlayerInfoService, " +
                    "because there is a no room for roomPlayerInfo={}", roomPlayerInfo);

            if(roomInfo instanceof ISingleNodeRoomInfo) {
                getLog().debug("removeRoomPlayerInfoIfNoSeatFound: roomInfo is instance of ISingleNodeRoomInfo," +
                                " it is safe to remove roomPlayerInfo");
                removeRoomPlayerInfo(roomPlayerInfo, roomInfo);
            }

        } else {

            ISeat seat = room.getSeatByAccountId(roomPlayerInfo.getId());

            getLog().debug("removeRoomPlayerInfoIfNoSeatFound: the seat found for accountId={} is seat={}",
                    roomPlayerInfo.getId(), seat);

            if(seat == null) {
                getLog().warn("removeRoomPlayerInfoIfNoSeatFound: remove roomPlayerInfo from roomPlayerInfoService, " +
                        "because there is a no seat allocated for roomPlayerInfo={}", roomPlayerInfo);

                removeRoomPlayerInfo(roomPlayerInfo, roomInfo);
            }
        }
    }

    /**
     * Validates player and makes sitout if needed.
     * @param roomPlayerInfo current room player info
     * @param messageMoneyType moneyType
     * @param messageGameType gameType
     * @param modeMoneyType modeMoneyType
     * @param tPlayerInfo player info from gs side
     * @param message EnterLobby message from client
     * @param client lobby socket client
     * @throws ErrorProcessedException if any unexpected error occur
     * @throws CommonException if any unexpected error occur
     */
    private void validateAndSitOutPlayer(IRoomPlayerInfo roomPlayerInfo, MoneyType messageMoneyType, GameType messageGameType,
                                         MoneyType modeMoneyType, DetailedPlayerInfo2Dto tPlayerInfo,
                                         EnterLobby message, ILobbySocketClient client) throws ErrorProcessedException, CommonException {

        long accountId = tPlayerInfo.getAccountId();
        Collection<LobbySession> playerLobbySessions = lobbySessionService.getByAccountId(accountId);

        getLog().debug("validateAndSitOutPlayer: accountId={}, playerLobbySessions: {} ", accountId, playerLobbySessions);

        //Notify BG non-Crash old games that new lobby session is opening
        if (messageGameType.isBattleGroundGame() && !messageGameType.isCrashGame() && playerLobbySessions.size() == 1
                && playerLobbySessions.iterator().next().getSessionId().equals(message.getSid())) {

            ILobbySocketClient oldClient = (ILobbySocketClient)
                    lobbySessionService.get(message.getSid()).getSocketClient();

            getLog().debug("validateAndSitOutPlayer: Rare case. Found session with current sid. " +
                    "Need sent invalid message to client: {}", oldClient);

            if (oldClient != null) {
                sendErrorMessage(oldClient, ErrorCodes.INVALID_SESSION, "New Lobby session is opening",
                        message.getRid());
            }
        }

        this.closeOldSocketConnectionsIfExist(playerLobbySessions, tPlayerInfo, message, client);

        if (roomPlayerInfo == null) {
            return;
        }

        IRoomInfo roomInfo = this.getRoomInfo(roomPlayerInfo.getRoomId());

        getLog().debug("validateAndSitOutPlayer: roomInfo={}", roomInfo);

        if (roomInfo != null) {

            boolean isBattlegroundRoom = isBattlegroundRoom(roomInfo);

            long roomStake = roomInfo.getStake().toCents();
            long messageBuyIn = message.getBattlegroundBuyIn();

            getLog().debug("validateAndSitOutPlayer: found roomInfo, id:{}, isBattlegroundRoom:{} " +
                            "roomInfo.getState():{}, message.isContinueIncompleteRound():{} " +
                            "roomInfo.getMoneyType:{}, required messageMoneyType:{}; " +
                            "roomInfo.gameType:{}, required messageGameType:{}",
                    roomInfo.getId(), isBattlegroundRoom,
                    roomInfo.getState(), message.isContinueIncompleteRound(),
                    roomInfo.getMoneyType(), messageMoneyType,
                    roomInfo.getGameType(), messageGameType);

            if ( !messageGameType.equals(roomInfo.getGameType())
              || !messageMoneyType.equals(roomInfo.getMoneyType())
              || (isBattlegroundRoom && roomInfo.getState().equals(RoomState.WAIT) && !message.isContinueIncompleteRound() && roomStake != messageBuyIn)
              || (roomInfo.isPrivateRoom() && roomInfo.isDeactivated())
            ) {

                getLog().debug("validateAndSitOutPlayer: Found roomPlayerInfo for other roomInfo " +
                        "roomInfo.gameType={}, current gameType={}, roomInfo.moneyType={}, modeMoneyType={} " +
                        "roomInfo.isPrivateRoom()=true, roomInfo.isDeactivated()=true " +
                        "make sitOut", roomInfo.getGameType(), messageGameType, roomInfo.getMoneyType(), modeMoneyType);

                int seatNumber = roomInfo instanceof ISingleNodeRoomInfo ? roomPlayerInfo.getSeatNumber() : 0;

                SitOutTask sitOutTask = new SitOutTask(roomPlayerInfo.getRoomId(), roomPlayerInfo.getId(), seatNumber);

                roomPlayerInfoService
                        .getNotifyService()
                        .executeOnAllMembers(sitOutTask);

                //wait up to 10 sec for sitOut process end
                getLog().debug("validateAndSitOutPlayer: Wait up to 10 sec for sitOut process end, sitOutTask:{}", sitOutTask);

                IRoomPlayerInfo playerInfo = null;

                try {
                    int count = 100;
                    while (count-- > 0) {

                        Thread.sleep(100);

                        playerInfo = roomPlayerInfoService.get(accountId);

                        if (playerInfo == null) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    getLog().error("validateAndSitOutPlayer: Check roomPlayerInfo after sitOut failed", e);
                    sendErrorMessage(client, ErrorCodes.NEED_SITOUT, "Sitout failed", message.getRid());
                    Thread.currentThread().interrupt();
                    throw new ErrorProcessedException();
                }

                getLog().debug("validateAndSitOutPlayer: waiting sitOut finished, playerInfo={}", playerInfo);

                if(playerInfo != null) {
                    getLog().warn("validateAndSitOutPlayer: there is an issue with sitOut for playerInfo={}", playerInfo);
                    this.removeRoomPlayerInfoIfNoSeatFound(playerInfo, roomInfo);
                }
            }
        } else {
            throw new CommonException("Room not found, id=" + roomPlayerInfo.getRoomId());
        }
    }

    /**
     * Checks pending operation for player. If player has pending operation the error will be generated.
     * @param roomPlayerInfo room player info
     * @param message EnterLobby message from client
     * @param client lobby socket client
     * @param gameType gameType
     * @throws ErrorProcessedException error if player has pending operation
     */
    private void checkPendingOperation(IRoomPlayerInfo roomPlayerInfo, EnterLobby message, ILobbySocketClient client, GameType gameType)
            throws ErrorProcessedException {

        getLog().debug("checkPendingOperation: message={}, gameType={}, roomPlayerInfo: {} ", message, gameType, roomPlayerInfo);

        boolean isAllowedPending = ALLOWED_PENDING.contains(gameType);

        if (roomPlayerInfo != null && roomPlayerInfo.isPendingOperation() && !isAllowedPending) {
            getLog().error("checkPendingOperation: cannot enter lobby, found failed payment operation, roomPlayerInfo={}",
                    roomPlayerInfo);
            sendErrorMessage(client, ErrorCodes.FOUND_PENDING_OPERATION, "Found pending operation", message.getRid());
            throw new ErrorProcessedException();
        }
    }

    /**
     * Get nickname without validation
     * @param tPlayerInfo player info from gs
     * @param gameSettings game settings from gs
     * @param client lobby socket client
     * @return String nickname
     * @throws CommonException error generation of nickname
     */
    private String getNicknameWithoutValidation(DetailedPlayerInfo2Dto tPlayerInfo, Map<String, String> gameSettings,
                                                ILobbySocketClient client) throws CommonException {
        String nickname;
        if (gameSettings != null) {
            nickname = gameSettings.get(PLAYER_NICKNAME);
            if (!StringUtils.isTrimmedEmpty(nickname)) {
                return nickname;
            }
        }
        getLog().warn("Nickname for MQB account dont presented");
        try {
            nickname = getPlayerNickname(tPlayerInfo, gameSettings, client);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return !StringUtils.isTrimmedEmpty(nickname) ? nickname : nicknameService.generateRandomNickname(tPlayerInfo.isGuest(),
                tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(), null);
    }

    /**
     * Get nickname with validation
     * @param tPlayerInfo player info from gs
     * @param gameSettings game settings from gs
     * @param client lobby socket client
     * @return String nickname
     * @throws CommonException error generation of nickname
     */
    private String getNicknameWithValidation(DetailedPlayerInfo2Dto tPlayerInfo, Map<String, String> gameSettings,
                                             ILobbySocketClient client) throws CommonException {
        String nickname;
        if (gameSettings != null && gameSettings.containsKey(BaseGameConstants.KEY_TOURNAMENT_PLAYER_ALIAS)) {
            nickname = gameSettings.get(BaseGameConstants.KEY_TOURNAMENT_PLAYER_ALIAS);
        } else {
            try {
                nickname = getPlayerNickname(tPlayerInfo, gameSettings, client);
            } catch (Exception e) {
                throw new CommonException(e);
            }
        }
        if (StringUtils.isTrimmedEmpty(nickname)) {
            //no special code for this error
            throw new CommonException("Nick name is null");
        }
        return nickname;
    }

    private boolean isNicknameEditable(Map<String, String> gameSettings) {
        return gameSettings != null && !gameSettings.containsKey(BaseGameConstants.KEY_TOURNAMENT_PLAYER_ALIAS);
    }

    private boolean isSendRealBetWin(Map<String, String> gameSettings, GameType gameType) {
        if (gameType == GameType.TRIPLE_MAX_BLAST) {
            return true;
        }
        return gameSettings != null && Boolean.parseBoolean(gameSettings.get(SEND_REAL_BET_WIN));
    }

    private MoneyType getMoneyType(EnterLobby message, ILobbySocketClient client) throws ErrorProcessedException {
        try {
            String mode = StringUtils.isTrimmedEmpty(message.getMode()) ? MoneyType.REAL.name() : message.getMode();
            return MoneyType.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLog().error("Illegal MoneyType: {}", message.getMode());
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Illegal mode: " + message.getMode(), message.getRid());
            throw new ErrorProcessedException();
        }

    }

    private void validateServerId(EnterLobby message, ILobbySocketClient client) throws ErrorProcessedException {

        getLog().debug("validateServerId: serverID={}, message={}", message.getServerId(), message);

        if (message.getServerId() <= 0) {
            getLog().error("validateServerId: Bad serverId={}, message={}, client={}", message.getServerId(), message, client);
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Bad serverId: " + message.getServerId(),
                    message.getRid());
            throw new ErrorProcessedException();
        }
    }

    private GameType getGameType(EnterLobby message, ILobbySocketClient client) throws ErrorProcessedException {
        GameType gameType = GameType.getByGameId(message.getGameId());
        if (gameType == null) {
            getLog().error("Unknown GameType: gameId={}", message.getGameId());
            sendErrorMessage(client, ErrorCodes.UNKNOWN_GAME_ID, "Unknown gameId: " + message.getGameId(), message.getRid());
            throw new ErrorProcessedException();
        }
        return gameType;
    }

    private boolean isExistingLobbySessionCompatibleWithNew(EnterLobby message,
                                                            DetailedPlayerInfo2Dto tPlayerInfo, MoneyType moneyType) {
        Collection<LobbySession> sessions = lobbySessionService.getByAccountId(tPlayerInfo.getAccountId());

        getLog().debug("isExistingLobbySessionCompatibleWithNew: accountId={}, sessions={}",
                tPlayerInfo.getAccountId(), sessions);

        if (sessions.isEmpty()) {
            return true;
        }

        if (sessions.size() > 1) {
            getLog().error("isExistingLobbySessionCompatibleWithNew: found more than one LobbySession, stp enter. " +
                    "sessions={}", sessions);
            return false;
        }

        LobbySession session = sessions.iterator().next();
        getLog().debug("isExistingLobbySessionCompatibleWithNew: first session from list for accountId:{}, " +
                        "session={}", tPlayerInfo.getAccountId(), session);

        getLog().debug("isExistingLobbySessionCompatibleWithNew: accountId:{}, " +
                "session.getMoneyType()={}, moneyType={}, session.getSessionId()={}, message.getSid()={}",
                tPlayerInfo.getAccountId(), session.getMoneyType(), moneyType, session.getSessionId(), message.getSid());

        if (session.getMoneyType().equals(moneyType) || session.getSessionId().equals(message.getSid())) {
            return true;
        } else {
            getLog().error("isExistingLobbySessionCompatibleWithNew: Found existing lobby with conflicting" +
                            " moneyType. session={}, new moneyType={}", session, moneyType);
            return false;
        }
    }

    private String getPlayerNickname(DetailedPlayerInfo2Dto tPlayerInfo, Map<String, String> gameSettings,
                                     ILobbySocketClient client) throws Exception {
        if (tPlayerInfo.isGuest()) {
            IRoomPlayerInfo roomPlayerInfo = roomPlayerInfoService.get(tPlayerInfo.getAccountId());
            if (roomPlayerInfo != null && !StringUtils.isTrimmedEmpty(roomPlayerInfo.getNickname())) {
                return roomPlayerInfo.getNickname();
            }
            return nicknameService.generateRandomNickname(true, tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(), null);
        }
        String nickname = nicknameService.getNickname(tPlayerInfo.getBankId(), tPlayerInfo.getAccountId());
        if (StringUtils.isTrimmedEmpty(nickname)) {
            nickname = loadMQDataIfAvailableAndReturnNickname(tPlayerInfo.getAccountId(),
                    tPlayerInfo.getBankId(), client.getServerId());
            if (StringUtils.isTrimmedEmpty(nickname) && gameSettings != null &&
                    gameSettings.containsKey(PLAYER_NICKNAME)) {
                nickname = gameSettings.get(PLAYER_NICKNAME);
                if (isNicknamePassable(gameSettings, nickname)) {
                    nickname = generateNickNameBySuffixIncrement(nickname, tPlayerInfo);
                    if (validator.isTooLong(nickname)) {
                        nickname = nicknameService.generateRandomNickname(tPlayerInfo.isGuest(),
                                tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(), null);
                    } else {
                        String oldNickname = nicknameService.getNickname(tPlayerInfo.getBankId(),
                                tPlayerInfo.getAccountId());
                        nicknameService.changeNickname(tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(),
                                oldNickname, nickname);
                    }
                } else {
                    nickname = nicknameService.generateRandomNickname(tPlayerInfo.isGuest(),
                            tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(), null);
                }
            }
        }
        if (!isNicknamePassable(gameSettings, nickname)) {
            nickname = nicknameService.generateRandomNickname(tPlayerInfo.isGuest(),
                    tPlayerInfo.getBankId(), tPlayerInfo.getAccountId(), nickname);
        }
        return nickname;
    }

    private String generateNickNameBySuffixIncrement(String originalNickname, DetailedPlayerInfo2Dto tPlayerInfo) {
        int suffix = 1;
        String nickname = originalNickname;
        while (!nicknameService.isNicknameAvailable(nickname,
                tPlayerInfo.getBankId(), tPlayerInfo.getAccountId())) {
            nickname = originalNickname + suffix++;
        }
        return nickname;
    }

    private boolean isNeedStartBonus(PlayerInfo playerInfo) {
        PlayerStats stats = playerInfo.getStats();
        return stats == null || (stats.getKillsCount() <= 0);
    }

    private boolean isNicknamePassable(Map<String, String> gameSettings, String nickname) {
        return gameSettings != null
                && validator.isPassableNickname(nickname, gameSettings.get(NICKNAME_ALLOWED_SYMBOLS))
                && !validator.isTooLong(nickname);
    }

    /**
     * Creates or gets actual frb session
     * @param tPlayerInfo player info from gs
     * @param message EnterLobby message
     * @param client lobby socket client
     * @param roomPlayerInfo room player info
     * @param moneyType moneyType
     * @return {@code IActiveFrbSession} actual frb session
     *
     */
    private IActiveFrbSession getActiveFrbSession(DetailedPlayerInfo2Dto tPlayerInfo, EnterLobby message, ILobbySocketClient client,
                                                  IRoomPlayerInfo roomPlayerInfo, MoneyType moneyType) {
        IActiveFrbSession activeFrbSession = null;
        FRBonusDto activeFrb = tPlayerInfo.getActiveFrb();
        if (activeFrb != null && !message.isNoFRB() && moneyType != MoneyType.FREE) {
            if (roomPlayerInfo != null && roomPlayerInfo.getActiveFrbSession() != null &&
                    activeFrb.getBonusId() != roomPlayerInfo.getActiveFrbSession().getBonusId()) {
                getLog().warn("Found new FRB but other frb in progress, roomPlayerInfo={}, new FRB.id={}",
                        roomPlayerInfo, activeFrb.getBonusId());
                sendErrorMessage(client, ErrorCodes.CLOSE_FRB_IN_PROGRESS,
                        "Close FRBonus session in progress, try later", message.getRid());
                return roomPlayerInfo.getActiveFrbSession();
            }
            if (roomPlayerInfo != null && roomPlayerInfo.getActiveFrbSession() == null) {
                getLog().warn("Found unclosed not FRB session and active FRBonus, not start new FRB. " +
                        "roomPlayerInfo={}, new FRB.id={}", roomPlayerInfo, activeFrb.getBonusId());
            } else {
                activeFrbSession = activeFrbSessionPersister.get(activeFrb.getBonusId());
                if (activeFrbSession == null) {
                    activeFrbSession = new ActiveFrbSession(
                            activeFrb.getBonusId(),
                            tPlayerInfo.getAccountId(),
                            activeFrb.getAwardDate(),
                            activeFrb.getStartDate(),
                            activeFrb.getExpirationDate() < 0 ? null : activeFrb.getExpirationDate(),
                            (int) activeFrb.getRounds(),
                            (int) activeFrb.getRounds(),
                            activeFrb.getWinSum(),
                            activeFrb.getSpinCost(), "ACTIVE", activeFrb.getMaxWinLimit());
                    activeFrbSessionPersister.persist(activeFrbSession);
                    getLog().debug("Found new FRB, create new activeFrbSession={}", activeFrbSession);
                } else {
                    if (roomPlayerInfo != null) {
                        try {
                            @SuppressWarnings("rawtypes")
                            AbstractGameRoom room = (AbstractGameRoom) roomServiceFactory.
                                    getRoomWithoutCreationById(roomPlayerInfo.getRoomId());
                            if (room != null) {
                                long accountId = roomPlayerInfo.getId();
                                getLog().debug("found old FRB room for player {}, ", accountId);
                                @SuppressWarnings("rawtypes")
                                ISeat seatByAccountId = room.getSeatByAccountId(accountId);
                                getLog().debug("seatByAccountId: {} for accountId: {}", seatByAccountId, accountId);
                                if (seatByAccountId != null) {
                                    IActiveFrbSession frbSession = seatByAccountId.getPlayerInfo().getActiveFrbSession();
                                    if (frbSession != null) {
                                        activeFrbSession = (ActiveFrbSession) frbSession;
                                        activeFrbSessionPersister.persist(activeFrbSession);
                                        LobbySession lobbySession = lobbySessionService.get(accountId);
                                        if (lobbySession != null) {
                                            lobbySession.setActiveFrbSession(activeFrbSession);
                                            lobbySessionService.add(lobbySession);
                                        }
                                        getLog().debug("found seater in room with frbSession {}, ", activeFrbSession);
                                    }
                                }
                            } else {
                                getLog().debug(" active FRB room not found for playerinfo: {}", roomPlayerInfo);
                            }
                        } catch (Exception e) {
                            getLog().warn("roomPlayerInfo.getId(): {} not found, error: {}",
                                    roomPlayerInfo.getId(), e.getMessage());
                        }
                    }
                    getLog().debug("Found uncompleted activeFrbSession={}", activeFrbSession);
                }
            }
        } else if (roomPlayerInfo != null && roomPlayerInfo.getActiveFrbSession() != null &&
                moneyType != MoneyType.FREE) {
            getLog().warn("Active FRB not found but current frb in progress, roomPlayerInfo={}", roomPlayerInfo);
            activeFrbSession = roomPlayerInfo.getActiveFrbSession();
        }

        if (activeFrb != null) {
            finalyzeExpiredFRB(activeFrb, tPlayerInfo.getAccountId());
        }

        return activeFrbSession;
    }

    /**
     * Generates start  weapon bonus. Not used in actual games.
     * @param playerInfo player info from gs
     * @param gameId gameId
     */
    private void initPlayerStartBonusWeapons(DetailedPlayerInfo2Dto playerInfo, long gameId) {
        List<Long> stakes = getStakes(playerInfo);
        Map<Integer, Integer> bonusWeapons = new HashMap<>(2);
        for (Map.Entry<String, Integer> nameAndBullets : NEW_PLAYER_BULLETS_BONUS.entrySet()) {
            String name = nameAndBullets.getKey();
            Integer bullets = nameAndBullets.getValue();
            SpecialWeaponType weaponType = SpecialWeaponType.getByMathTitle(name);
            if (weaponType != null && weaponType.getAvailableGameIds().contains((int) gameId)) {
                bonusWeapons.put(weaponType.getId(), bullets);
            }
        }

        weaponService.saveWeapons(playerInfo.getBankId(), playerInfo.getAccountId(), MoneyType.REAL.ordinal(),
                getMinimalStake(stakes), bonusWeapons, gameId);
    }

    boolean isFakeCurrency(String playerCurrency) {
        return "MMC".equalsIgnoreCase(playerCurrency) || "MQC".equalsIgnoreCase(playerCurrency);
    }

    /**
     * Gets stakes with check
     * @param playerInfo player info from gs
     * @return list of allowed stakes. first two stakes less 0.02 EUR will be removed. (by request from managers)
     */
    private List<Long> getStakes(DetailedPlayerInfo2Dto playerInfo) {
        ArrayList<Long> stakes = new ArrayList<>(playerInfo.getStakes());
        double rateForEUR = playerInfo.getCurrencyRateForEUR();
        boolean isFakeCurrency = isFakeCurrency(playerInfo.getCurrency());

        getLog().debug("stakes: {}, isFakeCurrency: {}, rateForEUR: {} ", stakes, isFakeCurrency, rateForEUR);
        Collections.sort(stakes);

        if (isFakeCurrency) {
            return stakes;
        }

        int size = stakes.size();
        if (size >= 3) {
            removeStake(stakes, rateForEUR);
            if (size > 3) {
                removeStake(stakes, rateForEUR);
            }
        }

        return stakes;
    }

    private Money getMinimalStake(List<Long> stakes) {
        long minStakeValue = 0;
        for (Long stake : stakes) {
            if (minStakeValue == 0 || stake < minStakeValue) {
                minStakeValue = stake;
            }
        }

        return Money.fromCents(minStakeValue);
    }

    /**
     * Init player quests for old games. Not used in new games.
     */
    private void initPlayerQuests(List<Long> stakes, GameType gameType, MoneyType moneyType, long bankId, long accountId,
                                  TournamentSession tournamentSession, IActiveFrbSession activeFrbSession,
                                  ActiveCashBonusSession activeCashBonusSession) {

        getLog().debug("initPlayerQuests: gameType.isSupportPlayerQuests()={}", gameType.isSupportPlayerQuests());

        if (!gameType.isSupportPlayerQuests()) {
            return;
        }

        long gameId = gameType.getGameId();
        int mode = moneyType.ordinal();

        for (Long stake : stakes) {

            PlayerQuests playerQuests = loadQuests(bankId, gameId, accountId, stake, mode, tournamentSession,
                    activeFrbSession, activeCashBonusSession);

            getLog().debug("initPlayerQuests: get data from cassandra for stake: playerQuests for stake {}", stake);

            if (playerQuests == null || playerQuests.getQuests().isEmpty()) {

                PlayerQuests playerQuestsForStake = new PlayerQuests(new HashSet<>());
                int idxQuestForCoin = 0;

                getLog().debug("initPlayerQuests: need generate new quests from config for stake {}", +stake);

                if (gameType.equals(GameType.PIRATES)) {

                    int[] wins = {20, 50, 150};

                    for (int idx = 1; idx <= 3; idx++) {
                        List<ITreasureProgress> treasureProgresses = new ArrayList<>();
                        treasureProgresses.add(new TreasureProgress(idx, 0, 3));
                        QuestPrize questPrize = new QuestPrize(new QuestAmount(wins[idx - 1], wins[idx - 1]), -1);
                        String name = com.betsoft.casino.mp.pirates.model.math.Treasure.getById(idx).name();
                        Quest newQuest = new Quest(idxQuestForCoin++, 1, stake, false, 0,
                                new QuestProgress(treasureProgresses), questPrize, name);
                        getLog().debug("new Quest: {}", newQuest);
                        playerQuestsForStake.getQuests().add(newQuest);
                    }

                } else if (gameType.equals(GameType.PIRATES_POV) || gameType.equals(GameType.DMC_PIRATES)) {

                    List<ITreasureProgress> treasureProgresses = new ArrayList<>();
                    treasureProgresses.add(new TreasureProgress(1, 0, 3));
                    QuestPrize questPrize = new QuestPrize(new QuestAmount(100, 500), -1);
                    String name = "Key";
                    Quest newQuest = new Quest(idxQuestForCoin++, 1, stake, false, 0,
                            new QuestProgress(treasureProgresses), questPrize, name);
                    getLog().debug("initPlayerQuests: new Quest: {}", newQuest);
                    playerQuestsForStake.getQuests().add(newQuest);

                } else if (gameType.equals(GameType.AMAZON) || gameType.equals(GameType.MISSION_AMAZON)) {

                    List<ITreasureProgress> treasureProgresses;
                    int idx = 1;
                    for (TreasureQuests treasureQuest : TreasureQuests.values()) {
                        treasureProgresses = new ArrayList<>();
                        for (ITreasure treasure : treasureQuest.getTreasures()) {
                            treasureProgresses.add(new TreasureProgress(treasure.getId(), 0, 1));
                        }
                        QuestPrize questPrize = new QuestPrize(new QuestAmount(treasureQuest.getWin(), treasureQuest.getWin()), -1);
                        String name = com.betsoft.casino.mp.amazon.model.math.Treasure.getById(idx).name();
                        Quest newQuest = new Quest(idxQuestForCoin++, 1, stake, false, 0,
                                new QuestProgress(treasureProgresses), questPrize, name);
                        getLog().debug("initPlayerQuests: new Quest: {}", newQuest);
                        playerQuestsForStake.getQuests().add(newQuest);
                    }
                }

                updateQuests(bankId, gameId, accountId, playerQuestsForStake.getQuests(),
                        Money.fromCents(stake), mode, tournamentSession, activeFrbSession, activeCashBonusSession);
            }
        }
    }

    /**
     * Load player quests for old games. Not used in new games.
     */
    private PlayerQuests loadQuests(long bankId, long gameId, long accountId, long stake, int mode,
                                    TournamentSession tournamentSession, IActiveFrbSession activeFrbSession,
                                    ActiveCashBonusSession activeCashBonusSession) {
        Long bonusOrTournamentId = getBonusOrTournamentId(tournamentSession, activeFrbSession, activeCashBonusSession);

        if (bonusOrTournamentId == null) {
            return playerQuestsPersister.load(bankId, gameId, accountId, Money.fromCents(stake), mode);
        } else {
            return playerQuestsPersister.loadSpecialModeQuests(bonusOrTournamentId, bankId, gameId, accountId,
                    Money.fromCents(stake), mode);
        }
    }

    /**
     * Update player quests for old games. Not used in new games.
     */
    private void updateQuests(long bankId, long gameId, long accountId, Set<IQuest> quests, Money stake,
                              int mode, TournamentSession tournamentSession, IActiveFrbSession activeFrbSession,
                              ActiveCashBonusSession activeCashBonusSession) {
        Long bonusOrTournamentId = getBonusOrTournamentId(tournamentSession, activeFrbSession, activeCashBonusSession);
        if (bonusOrTournamentId == null) {
            playerQuestsPersister.updateQuests(bankId, gameId, accountId, quests, stake, mode);
        } else {
            playerQuestsPersister.updateSpecialModeQuests(bonusOrTournamentId, bankId, gameId,
                    accountId, quests, stake, mode);
        }
    }

    public static Long getBonusOrTournamentId(TournamentSession tournamentSession, IActiveFrbSession activeFrbSession,
                                              ActiveCashBonusSession activeCashBonusSession) {
        if (tournamentSession != null) {
            return tournamentSession.getTournamentId();
        } else if (activeCashBonusSession != null) {
            return activeCashBonusSession.getId();
        } else if (activeFrbSession != null) {
            return activeFrbSession.getBonusId();
        }
        return null;
    }

    /**
     * Remove stakes  < 0.02 EUR
     * @param stakes list stakes
     * @param rateForEUR rate for EUR
     */
    private void removeStake(ArrayList<Long> stakes, double rateForEUR) {
        String mpStress = System.getProperty("mp_stress");
        boolean stressMode = !StringUtils.isTrimmedEmpty(mpStress) &&
                mpStress.equalsIgnoreCase(Boolean.TRUE.toString());

        double minStake = stakes.get(0);
        double stakeInEUR = (minStake / 100) * rateForEUR;
        getLog().debug("removeStake, stressMode: {}, minStake: {}, rateForEUR: {}, stakeInEUR: {} ", stressMode, minStake, rateForEUR, stakeInEUR);

        if (stressMode) {
            return;
        }
        if (stakeInEUR <= 0.02) {
            getLog().debug("{} {} {} was removed", rateForEUR, stakes.get(0), stakeInEUR);
            stakes.remove(0);
        }
    }

    private void loadStats(PlayerInfo playerInfo, GameType gameType, MoneyType moneyType) {
        boolean needRealStats = MoneyType.REAL == moneyType;
        PlayerStats playerStats;

        if(needRealStats) {
            playerStats = statsPersister.load(
                    playerInfo.getBankId(),
                    gameType.getGameId(),
                    playerInfo.getAccountId());
        } else {
            playerStats = new PlayerStats();
        }

        playerInfo.setStats(playerStats);
    }

    void finalyzeExpiredFRB(FRBonusDto activeFrb, long accountId) {
        List<IActiveFrbSession> frbSessions = activeFrbSessionPersister.getByAccountId(accountId);
        for (IActiveFrbSession frbSession : frbSessions) {
            if (activeFrb != null && activeFrb.getBonusId() == frbSession.getBonusId()) { //skip current
                continue;
            }
            if (frbSession.getExpirationDate() != null && frbSession.getExpirationDate() <= System.currentTimeMillis()) {
                activeFrbSessionPersister.remove(frbSession.getBonusId());
                getLog().debug("Remove expired activeFrbSession={}", frbSession);
            }
        }
    }

    /**
     * Converts stakes for client
     * @param sessionStakes stakes
     * @param activeFRBonus active frb session
     * @return {@code List<Float>} list stakes for client
     */
    private List<Float> convertStakes(List<Long> sessionStakes, IActiveFrbSession activeFRBonus) {
        List<Float> stakes = new ArrayList<>();
        if (activeFRBonus == null) {
            for (Long playerStake : sessionStakes) {
                float baseStake = Money.fromCents(playerStake).toFloatCents();
                stakes.add(baseStake);
            }
        } else {
            stakes.add(Money.fromCents(activeFRBonus.getStake()).toFloatCents());
        }
        return stakes;
    }

    /**
     * Send EnterLobbyResponse to client
     * @throws CommonException if any unexpected error occur
     */
    private void sendResponse(WebSocketSession session, EnterLobby message, ILobbySocketClient client,
                              PlayerInfo playerInfo, long roomId, Avatar avatar,
                              PlayerProfile profile, Paytable paytable, LobbySession lobbySession,
                              float alreadySitInStake, boolean needStartBonus, String nicknameGlyphs,
                              List<BattlegroundInfoDto> tBattlegroundInfoList) throws CommonException {

        getLog().debug("sendResponse: message={}, playerInfo={}, lobbySession={}", message, playerInfo, lobbySession);

        ArrayList<Integer> borders = new ArrayList<>(AvatarParts.BORDER.getFreeParts());
        ArrayList<Integer> heroes = new ArrayList<>(AvatarParts.HERO.getFreeParts());
        ArrayList<Integer> backgrounds = new ArrayList<>(AvatarParts.BACKGROUND.getFreeParts());

        if (!CollectionUtils.isEmpty(profile.getBorders())) {
            borders.addAll(profile.getBorders());
        }

        if (!CollectionUtils.isEmpty(profile.getHeroes())) {
            heroes.addAll(profile.getHeroes());
        }

        if (!CollectionUtils.isEmpty(profile.getBackgrounds())) {
            backgrounds.addAll(profile.getBackgrounds());
        }

        IActiveFrbSession activeFRBonus = lobbySession.getActiveFrbSession();
        PlayerStats stats = playerInfo.getStats();
        int level = AchievementHelper.getPlayerLevel(stats.getScore());
        List<Float> stakes = convertStakes(lobbySession.getStakes(), activeFRBonus);

        ITournamentSession tournamentSession = lobbySession.getTournamentSession();

        GameType gameType = GameType.getByGameId((int) lobbySession.getGameId());
        short maxBulletsOnMap = gameType != null ? gameType.getMaxBulletsOnMap() : 0;
        boolean isDisabledBgInfo = (lobbySession.getTournamentSession() != null || lobbySession.getActiveFrbSession() != null ||
                lobbySession.getActiveCashBonusSession() != null || !MoneyType.REAL.equals(lobbySession.getMoneyType())) &&
                (!isBattlegroundRoom(getRoomInfo(roomId)));

        EnterLobbyResponse response = new EnterLobbyResponse(System.currentTimeMillis(),
                lobbyManager.getActivePlayersCount(playerInfo.getBankId()),
                client.getNickname(),
                lobbySession.getBalance(),
                message.getRid(),
                playerInfo.getCurrency(),
                stats.getScore().getLongAmount(),
                roomId,
                level,
                avatar, stakes, borders, heroes, backgrounds, paytable, null,
                playerInfo.isShowRefreshBalanceButton(),
                stats.getKillsCount(),
                stats.getTreasuresCount(),
                stats.getRounds(),
                stats.getScore().getLongAmount(),
                AchievementHelper.getXP(level),
                AchievementHelper.getXP(level + 1L), profile.isDisableTooltips(),
                lobbySession.getStakesReserve(), lobbySession.getStakesLimit(),
                roomId <= 0 ? 0 : alreadySitInStake,
                needStartBonus,
                nicknameGlyphs, lobbySession.getWeaponMode().name(),
                maxBulletsOnMap,
                isDisabledBgInfo ? null : getBattlegroundInfo(session, client, tBattlegroundInfoList, lobbySession, roomId));
        response.setNicknameEditable(lobbySession.isNicknameEditable());
        boolean isFree = message.getMode().equalsIgnoreCase("free");
        //dirty fix for find error reason
        if (lobbySession.getActiveCashBonusSession() != null && !lobbySession.getActiveCashBonusSession().isActive()) {
            getLog().warn("sendResponse: Found not active cashBonus in session, please fix reason, " +
                    "lobbySession={}", lobbySession);
            lobbySession.setActiveCashBonusSession(null);
            lobbySessionService.add(lobbySession);
        }
        if (!isFree) {
            ActiveCashBonusSession activeCashBonusSession = lobbySession.getActiveCashBonusSession();
            if (activeCashBonusSession != null) {
                response.setCashBonusInfo(new CashBonusInfo(
                        activeCashBonusSession.getId(),
                        activeCashBonusSession.getAwardDate(),
                        activeCashBonusSession.getExpirationDate(),
                        activeCashBonusSession.getBalance(),
                        activeCashBonusSession.getAmount(),
                        activeCashBonusSession.getAmountToRelease(),
                        activeCashBonusSession.getStatus())
                );
            } else {
                IActiveFrbSession activeFrbSession = lobbySession.getActiveFrbSession();
                if (tournamentSession != null) {
                    response.setTournamentInfo(new TournamentInfo(
                            tournamentSession.getTournamentId(),
                            tournamentSession.getName(),
                            tournamentSession.getState(),
                            tournamentSession.getStartDate(),
                            tournamentSession.getEndDate(),
                            tournamentSession.getBalance(),
                            tournamentSession.getBuyInPrice(),
                            tournamentSession.getBuyInAmount(),
                            tournamentSession.isReBuyAllowed(),
                            tournamentSession.getReBuyPrice(),
                            tournamentSession.getReBuyAmount(),
                            tournamentSession.getReBuyCount(),
                            tournamentSession.getReBuyLimit(),
                            tournamentSession.isResetBalanceAfterRebuy()));
                } else if (activeFrbSession != null) {
                    response.setFrBonusInfo(new FRBonusInfo(
                            activeFrbSession.getBonusId(),
                            activeFrbSession.getAwardDate(),
                            activeFrbSession.getExpirationDate() == null ? -1 : activeFrbSession.getExpirationDate(),
                            activeFrbSession.getStartAmmoAmount(),
                            activeFrbSession.getCurrentAmmoAmount(),
                            activeFrbSession.getWinSum(),
                            activeFrbSession.getStake())
                    );
                }
            }
        }
        if (gameType != null && gameType.isCrashGame()) {
            ICrashGameSetting settings = crashGameSettingsService.getSettings(playerInfo.getBankId(), lobbySession.getGameId(),
                    playerInfo.getCurrency().getCode());
            response.setMinStake(settings.getMinStake());
            response.setMaxStake(settings.getMaxStake());
        }

        getLog().debug("sendResponse: response={}", response);

        client.sendMessage(response, message);

        sendNotifications(client);
    }

    /**
     * Gets BattlegroundInfo from player info from gs for sending to client
     * @param session web socket session
     * @param client lobby socket client
     * @param tBattlegroundInfoList {@code List<TBattlegroundInfo> } list of battleground infos from gs side.
     * @param lobbySession lobby session of player
     * @param roomId roomId
     * @return {@code EnterLobbyBattlegroundInfo} battle fround info for client
     * @throws CommonException if any unexpected error occur
     */
    private EnterLobbyBattlegroundInfo getBattlegroundInfo(WebSocketSession session, ILobbySocketClient client,
                                                           List<BattlegroundInfoDto> tBattlegroundInfoList,
                                                           LobbySession lobbySession, long roomId) throws CommonException {
        BattlegroundInfoDto currentTBattlegroundInfo = getCurrentTBattlegroundInfo(tBattlegroundInfoList,
                lobbySession.getGameId());
        if (currentTBattlegroundInfo == null) {
            return null;
        }
        IRoomInfo room = getRoomInfo(roomId);
        Long alreadySeatRoomId = null;
        String startGameUrl = null;
        RoomState roomState = null;
        if (isBattlegroundRoom(room)) {
            alreadySeatRoomId = roomId;
            int serverId = serverConfigService.getServerId();
            if (room instanceof ISingleNodeRoomInfo) {
                serverId = ((ISingleNodeRoomInfo) room).getGameServerId();
            }
            roomState = room.getState();
            IServerConfig config = serverConfigService.getConfig(serverId);
            if (config == null) {
                getLog().warn("ServerConfig not found for serverId=" + serverId + ". Seems that server is down. Migrating down games.");
                roomServiceFactory.repairRoomsOnDownServer(serverId);
                serverId = serverConfigService.getServerId();
                config = serverConfigService.getConfig();
            }
            startGameUrl = AbstractStartGameUrlHandler.getRoomUrl(session, roomId, config, client,
                    room.getStake().toCents(), room.getGameType());
        }

        return new EnterLobbyBattlegroundInfo(currentTBattlegroundInfo.getBuyIns(), alreadySeatRoomId, startGameUrl,
                roomState == null ? null : roomState.name(), lobbySession.isConfirmBattlegroundBuyIn());
    }

    private BattlegroundInfoDto getCurrentTBattlegroundInfo(List<BattlegroundInfoDto> tBattlegroundInfoList, long gameId) {
        if (tBattlegroundInfoList == null) {
            return null;
        }
        for (BattlegroundInfoDto tBattlegroundInfo : tBattlegroundInfoList) {
            if (tBattlegroundInfo.getGameId() == gameId) {
                return tBattlegroundInfo;
            }
        }
        return null;
    }

    private void checkPlayerInfo(DetailedPlayerInfo2Dto tPlayerInfo, ILobbySocketClient client, EnterLobby message)
            throws ErrorProcessedException {

        getLog().debug("checkPlayerInfo: message={}, tPlayerInfo: {} ", message, tPlayerInfo);

        if (tPlayerInfo == null || !tPlayerInfo.isSuccess()) {
            getLog().error("checkPlayerInfo: bad tPlayerInfo: {} ", tPlayerInfo);
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            throw new ErrorProcessedException();
        }
    }

    private DetailedPlayerInfo2Dto getDetailedPlayerInfoWithSidRecovery(EnterLobby message, GameType messageGameType,
                                                                         MoneyType modeMoneyType, ILobbySocketClient client)
            throws CommonException {
        DetailedPlayerInfo2Dto tPlayerInfo = socketService.getDetailedPlayerInfo(
                message.getSid(), messageGameType.getGameId(), modeMoneyType.name(), message.getBonusId(),
                message.getTournamentId());
        String expectedSid = extractExpectedSidFromMismatch(tPlayerInfo);
        if (!StringUtils.isTrimmedEmpty(expectedSid) && !expectedSid.equals(message.getSid())) {
            getLog().warn("Detected stale SID on EnterLobby, retry with expected SID. receivedSid={}, expectedSid={}",
                    message.getSid(), expectedSid);
            message.setSid(expectedSid);
            client.setSessionId(expectedSid);
            tPlayerInfo = socketService.getDetailedPlayerInfo(
                    expectedSid, messageGameType.getGameId(), modeMoneyType.name(), message.getBonusId(),
                    message.getTournamentId());
        }
        return tPlayerInfo;
    }

    private String extractExpectedSidFromMismatch(DetailedPlayerInfo2Dto tPlayerInfo) {
        if (tPlayerInfo == null || tPlayerInfo.isSuccess()) {
            return null;
        }
        String reason = tPlayerInfo.getReasonPhrases();
        if (StringUtils.isTrimmedEmpty(reason) || !reason.contains("Mismatch sessionId")) {
            return null;
        }
        int expectedIdx = reason.indexOf("expected:");
        if (expectedIdx < 0) {
            return null;
        }
        int start = expectedIdx + "expected:".length();
        int end = reason.indexOf(')', start);
        if (end < 0) {
            end = reason.length();
        }
        String sid = reason.substring(start, end).trim();
        if (sid.endsWith(";")) {
            sid = sid.substring(0, sid.length() - 1).trim();
        }
        return StringUtils.isTrimmedEmpty(sid) ? null : sid;
    }

    /**
     * Get correct money type.
     * @param message EnterLobby message
     * @param tPlayerInfo player info from gs
     * @return {@code MoneyType} moneyType
     */
    private MoneyType getMoneyType(EnterLobby message, DetailedPlayerInfo2Dto tPlayerInfo) {
        boolean realMessageMode = MoneyType.REAL.name().equalsIgnoreCase(message.getMode());
        if (isBattleGroundRoom(tPlayerInfo)) {
            return MoneyType.REAL;
        } else if (realMessageMode && (message.getBattlegroundBuyIn() != null && message.getBattlegroundBuyIn() > 0)) {
            return tPlayerInfo.getActiveFrb() == null ? MoneyType.REAL : MoneyType.FRB;
        } else if (message.getTournamentId() != null && message.getTournamentId() > 0) {
            return MoneyType.TOURNAMENT;
        } else if (message.getBonusId() != null && message.getBonusId() > 0) {
            return MoneyType.CASHBONUS;
        } else if (realMessageMode) {
            return tPlayerInfo.getActiveFrb() != null && !message.isNoFRB() ? MoneyType.FRB : MoneyType.REAL;
        } else {
            return MoneyType.FREE;
        }
    }

    /**
     * Gets cash bonus session from player info.
     * @param tPlayerInfo player info from gs side.
     * @return {@code ActiveCashBonusSession} cash bonus session
     */
    private ActiveCashBonusSession getCashBonusSession(DetailedPlayerInfo2Dto tPlayerInfo) {
        CashBonusDto bonus = tPlayerInfo.getCashBonus();
        if (bonus == null) {
            return null;
        }
        ActiveCashBonusSession session = activeCashBonusSessionPersister.get(bonus.getBonusId());
        if (session == null) {
            session = new ActiveCashBonusSession(bonus.getBonusId(), tPlayerInfo.getAccountId(), bonus.getAwardDate(),
                    bonus.getExpirationDate(), bonus.getBalance(), bonus.getAmount(), bonus.getBetSum(), bonus.getRolloverMultiplier(),
                    bonus.getStatus(), bonus.getMaxWinLimit());
            activeCashBonusSessionPersister.persist(session);
        }
        return session;
    }

    /**
     * Gets Tournament session from player info.
     * @param tPlayerInfo player info from gs side.
     * @return {@code TournamentSession} Tournament session
     */
    private TournamentSession getTournamentSession(DetailedPlayerInfo2Dto tPlayerInfo) {
        TournamentInfoDto tournamentInfo = tPlayerInfo.getTournamentInfo();
        if (tournamentInfo == null) {
            return null;
        }
        TournamentSession session = tournamentSessionPersister.get(tournamentInfo.getTournamentId(), tPlayerInfo.getAccountId());
        if (session == null) {
            session = new TournamentSession(tPlayerInfo.getAccountId(), tournamentInfo.getTournamentId(), tournamentInfo.getName(), tournamentInfo.getState(),
                    tournamentInfo.getStartDate(), tournamentInfo.getEndDate(), tournamentInfo.getBalance(),
                    tournamentInfo.getBuyInPrice(), tournamentInfo.getBuyInAmount(), tournamentInfo.isReBuyAllowed(),
                    tournamentInfo.getReBuyPrice(), tournamentInfo.getReBuyAmount(),
                    tournamentInfo.getReBuyCount(), tournamentInfo.getReBuyLimit(), tournamentInfo.isResetBalanceAfterRebuy());
            tournamentSessionPersister.persist(session);
        }
        return session;
    }

    private void sendNotifications(ILobbySocketClient client) {

        long gameId = client.getGameType().getGameId();

        roundResultNotificationPersister
                .getNotifications(client.getAccountId(), gameId)
                .forEach(client::sendMessage);
    }

    /**
     * Tries load old MQData from gs side.
     * @param accountId accountId of player
     * @param bankId bankId
     * @param serverId serverId
     * @return String nickname of player
     * @throws Exception if any unexpected error occur
     */
    private String loadMQDataIfAvailableAndReturnNickname(long accountId, long bankId, int serverId) throws Exception {
        String nickname = null;
        for (GameType gameType : GameType.values()) {
            long gameId = gameType.getGameId();
            MQData mqData = socketService.loadMQDataSync(accountId, gameId, serverId);
            if (mqData != null) {
                getLog().debug("Received MQ Data for accountId={} : {}", accountId, mqData);
                nickname = mqData.getNickname();
                if (nicknameService.isNicknameAvailable(nickname, bankId, accountId)) {
                    nicknameService.changeNickname(bankId, accountId, null, nickname);
                } else {
                    nickname = nicknameService.generateRandomNickname(false, bankId, accountId, nickname);
                }

                PlayerStats stats = new PlayerStats();
                stats.addScore(mqData.getExperience());
                stats.setRounds(mqData.getRounds());
                stats.setKills(mqData.getKills());
                stats.setTreasures(mqData.getTreasures());
                statsPersister.addStats(bankId, gameId, accountId, stats);

                PlayerProfile profile = new PlayerProfile(mqData.getBorders(),
                        mqData.getHeroes(),
                        mqData.getBackgrounds(),
                        mqData.getBorderStyle(),
                        mqData.getHero(),
                        mqData.getBackground(),
                        mqData.isDisableTooltips());
                playerProfilePersister.save(bankId, accountId, profile);

                Map<Long, Set<IQuest>> allQuestsByCoins = new ConcurrentHashMap<>();
                for (MQQuestData quest : mqData.getQuests()) {

                    long roomCoin = quest.getRoomCoin();
                    Set<IQuest> questsCoin = allQuestsByCoins.computeIfAbsent(roomCoin, k -> new HashSet<>());

                    List<ITreasureProgress> treasures = new ArrayList<>();
                    for (MQTreasureQuestProgress treasureProgress : quest.getTreasures()) {
                        treasures.add(new TreasureProgress(
                                treasureProgress.getTreasureId(),
                                treasureProgress.getCollect(),
                                treasureProgress.getGoal()));
                    }
                    QuestProgress progress = new QuestProgress(treasures);
                    MQuestPrize questPrize = quest.getQuestPrize();
                    QuestAmount questAmount = new QuestAmount(questPrize.getAmount().getFrom(), questPrize.getAmount().getTo());

                    questsCoin.add(new Quest(quest.getId(),
                            quest.getType(),
                            quest.getRoomCoin(),
                            quest.isNeedReset(),
                            quest.getCollectedAmount(),
                            progress,
                            new QuestPrize(questAmount, questPrize.getSpecialWeaponId()),
                            quest.getName()));
                }
                allQuestsByCoins.forEach((coinCents, quests) ->
                        playerQuestsPersister.updateQuests(bankId, gameId, accountId, quests,
                                Money.fromCents(coinCents), MoneyType.REAL.ordinal()));

                Map<Long, Map<Integer, Integer>> weapons = mqData.getWeapons();
                if (weapons != null) {
                    weapons.forEach((coinCents, weaponsForCoin) ->
                            weaponService.saveWeapons(bankId, accountId, MoneyType.REAL.ordinal(),
                                    Money.fromCents(coinCents), weaponsForCoin, gameId));
                }
            }
        }
        return nickname;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private IRoomInfo getRoomInfo(long roomId) {
        return roomServiceFactory.getRoomInfo(roomId);
    }

    private IRoomInfoService getRoomInfoService(IRoomInfo roomInfo) {
        if (roomInfo.isPrivateRoom()) {
            return roomInfo.getGameType().isSingleNodeRoomGame() ? bgPrivateRoomInfoService : multiNodePrivateRoomInfoService;
        }
        return roomInfo.getGameType().isSingleNodeRoomGame() ? singleNodeRoomInfoService : multiNodeRoomInfoService;
    }
}
