package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.common.AbstractActionSeat;
import com.betsoft.casino.mp.common.AbstractBattlegroundGameRoom;
import com.betsoft.casino.mp.common.AchievementHelper;
import com.betsoft.casino.mp.data.persister.ActiveFrbSessionPersister;
import com.betsoft.casino.mp.data.persister.PlayerNicknamePersister;
import com.betsoft.casino.mp.data.persister.PlayerQuestsPersister;
import com.betsoft.casino.mp.data.persister.PlayerStatsPersister;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.playerinfo.BattlegroundRoomPlayerInfo;
import com.betsoft.casino.mp.model.playerinfo.CrashGameBGRoomPlayerInfo;
import com.betsoft.casino.mp.model.playerinfo.CrashGameRoomPlayerInfo;
import com.betsoft.casino.mp.model.playerinfo.DefaultRoomPlayerInfo;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.lobby.EnterLobbyHandler;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SitOutTask;
import com.betsoft.casino.mp.web.service.SocketService;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.common.currency.CurrencyRate;
import com.abs.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

/**
 * User: flsh
 * Date: 03.11.17.
 */
@SuppressWarnings("rawtypes")
@Component
public class SitInHandler extends AbstractRoomHandler<SitIn, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(SitInHandler.class);

    protected final LobbySessionService lobbySessionService;
    private final SocketService socketService;
    private final PlayerStatsPersister playerStatsPersister;
    private final ActiveFrbSessionPersister activeFrbSessionPersister;
    private final PlayerQuestsPersister playerQuestsPersister;
    private final PlayerNicknamePersister nicknamePersister;
    private final CurrencyRateService currencyRateService;
    protected final CrashGameSettingsService crashGameSettingsService;
    private final BGPrivateRoomInfoService bgPrivateRoomInfoService;
    private final MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService;
    private final BotConfigInfoService botConfigInfoService;
    protected final IPendingOperationService pendingOperationService;

    public SitInHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                        MultiNodeRoomInfoService multiNodeRoomInfoService,
                        RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                        LobbySessionService lobbySessionService, SocketService socketService,
                        ServerConfigService serverConfigService, CassandraPersistenceManager cpm,
                        CurrencyRateService currencyRateService, CrashGameSettingsService crashGameSettingsService,
                        BGPrivateRoomInfoService bgPrivateRoomInfoService, MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService,
                        BotConfigInfoService botConfigInfoService, IPendingOperationService pendingOperationService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        this.socketService = socketService;
        this.activeFrbSessionPersister = cpm.getPersister(ActiveFrbSessionPersister.class);
        this.playerStatsPersister = cpm.getPersister(PlayerStatsPersister.class);
        this.playerQuestsPersister = cpm.getPersister(PlayerQuestsPersister.class);
        this.nicknamePersister = cpm.getPersister(PlayerNicknamePersister.class);
        this.currencyRateService = currencyRateService;
        this.crashGameSettingsService = crashGameSettingsService;
        this.bgPrivateRoomInfoService = bgPrivateRoomInfoService;
        this.multiNodePrivateRoomInfoService = multiNodePrivateRoomInfoService;
        this.botConfigInfoService = botConfigInfoService;
        this.pendingOperationService = pendingOperationService;
    }

    @Override
    public void handle(WebSocketSession session, SitIn message, IGameSocketClient client) {
        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                if (room.isNotAllowPlayWithAnyPendingPlayers() && hasPlayersWithPendingOperation(room.getId())) {
                    getLog().warn("Cannot open room, room has players with pending operations");
                    sendErrorMessage(client, FOUND_PENDING_OPERATION, "Found room with " +
                            "pending operation", message.getRid());
                    return;
                }
                IRoomPlayerInfo thisRoomPlayer = getThisRoomPlayerInfo(client);
                if (thisRoomPlayer == null) {
                    LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
                    if (lobbySession == null) {
                        sendErrorMessage(client, INVALID_SESSION, "Session not found", message.getRid());
                        return;
                    }
                    if (lobbySession.getMoneyType() != room.getRoomInfo().getMoneyType()) {
                        sendErrorMessage(client, INTERNAL_ERROR, "Bad room", message.getRid());
                        return;
                    }

                    if (room.isBattlegroundMode()) {
                        if (!lobbySession.isBattlegroundAllowed()) {
                            sendErrorMessage(client, BUYIN_NOT_ALLOWED, "Battleground mode not allowed",
                                    message.getRid());
                            return;
                        } else if (!lobbySession.isConfirmBattlegroundBuyIn() && !room.getGameType().isCrashGame()) {
                            getLog().warn("Buy in is not confirmed, lobbySession: {}", lobbySession);
                            sendErrorMessage(client, INTERNAL_ERROR, "Buy in is not confirmed",
                                    message.getRid());
                            return;
                        } else //noinspection unchecked
                            if (!room.isBuyInAllowed(null)) {
                                sendErrorMessage(client, BUYIN_NOT_ALLOWED,
                                        "SitIn not allowed at current game state", message.getRid());
                                return;
                            }
                    }

                    long playerStake = message.getStake();
                    List<Long> stakes = lobbySession.getStakes();
                    //todo: check stake by compare selected room.template and RoomTemplateService.getMostSuitable()
                    boolean isBattlegroundRoom = room.getRoomInfo().isBattlegroundMode();
                    boolean isRoomsWithSpecialLogic = room.getRoomInfo().getMoneyType() == MoneyType.FRB ||
                            isBattlegroundRoom;
                    if (!isMaxCrashGame(room.getGameType())) {
                        if (!isRoomsWithSpecialLogic && (playerStake <= 0 || !stakes.contains(playerStake))) {
                            //todo: fastfix need convert stakes to player currency
                            getLog().error("handle: Cannot sitIn, found bad stake={}, stakes={}", playerStake, stakes);
                            sendErrorMessage(client, BAD_STAKE, "Illegal stake value",
                                    message.getRid());
                            return;
                        }

                        long roomStake = room.getRoomInfo().getStake().toCents();
                        if (playerStake != roomStake) {
                            getLog().error("handle: Cannot sitIn, found bad stake={}, stakes={}, roomStake: {}",
                                    playerStake, stakes, roomStake);
                            sendErrorMessage(client, BAD_STAKE, "Illegal stake value",
                                    message.getRid());
                            return;
                        }
                    }
                    IRoomPlayerInfo playerInfo = createRoomPlayerInfo(
                            client.getSessionId(),
                            room.getId(),
                            lobbySession,
                            room.getRoomInfo().getMoneyType(),
                            playerStake);
                    sitIn(message, client, room, playerInfo, true);
                } else if (hasPendingOperation(thisRoomPlayer)) {
                    getLog().warn("handle: Cannot sitIn, player has pending operation");
                    sendErrorMessage(client, FOUND_PENDING_OPERATION, "Found pending operation", message.getRid());
                } else {
                    if (!room.getGameState().isReconnectAllowed()) {
                        sendErrorMessage(client, BUYIN_NOT_ALLOWED,
                                "SitIn (reconnect) not allowed at current game state", message.getRid());
                        return;
                    }
                    if (thisRoomPlayer instanceof IBattlegroundRoomPlayerInfo) {
                        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
                        ((IBattlegroundRoomPlayerInfo) thisRoomPlayer).setBattlegroundRake(lobbySession.getBattlegroundRakePercent());
                    }
                    sitIn(message, client, room, thisRoomPlayer, false);
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private boolean isMaxCrashGame(GameType gameType) {
        return gameType != null && gameType.isCrashGame();
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private IRoomPlayerInfo createRoomPlayerInfo(String sessionId, long roomId, LobbySession lobbySession,
                                                 MoneyType mode, long playerStake) {
        if (lobbySession.isBattlegroundAllowed()) {
            return createBattlegroundRoomPlayerInfo(sessionId, roomId, lobbySession, playerStake);
        } else {
            Long bonusOrTournamentId = EnterLobbyHandler.getBonusOrTournamentId(lobbySession.getTournamentSession(),
                    lobbySession.getActiveFrbSession(), lobbySession.getActiveCashBonusSession());
            if (bonusOrTournamentId == null) {
                return createRegularRoomPlayerInfo(sessionId, roomId, lobbySession, mode, playerStake);
            } else {
                return createSpecialModeRoomPlayerInfo(bonusOrTournamentId, sessionId, roomId, lobbySession, playerStake,
                        mode);
            }
        }
    }

    private IRoomPlayerInfo createRegularRoomPlayerInfo(String sessionId, long roomId, LobbySession lobbySession,
                                                        MoneyType mode, long playerStake) {
        long gameId = lobbySession.getGameId();
        PlayerStats playerStats = playerStatsPersister.load(lobbySession.getBankId(), gameId, lobbySession.getAccountId());

        PlayerQuests playerQuests = playerQuestsPersister.load(lobbySession.getBankId(), gameId,
                lobbySession.getAccountId(), Money.fromCents(playerStake), mode.ordinal());

        getLog().debug("createRoomPlayerInfo: playerQuests={}", playerQuests);

        GameType gameType = GameType.getByGameId((int) gameId);
        if (gameType != null && gameType.isCrashGame()) {
            return new CrashGameRoomPlayerInfo(
                    lobbySession.getAccountId(),
                    lobbySession.getBankId(),
                    roomId,
                    -1,
                    sessionId,
                    0, //unknown at the moment, set later
                    lobbySession.getNickname(),
                    lobbySession.getAvatar(),
                    System.currentTimeMillis(),
                    lobbySession.getCurrency(),
                    playerStats,
                    lobbySession.isShowRefreshButton(),
                    playerQuests,
                    playerStake,
                    lobbySession.getStakesReserve()
            );
        } else {
            return new DefaultRoomPlayerInfo(
                    lobbySession.getAccountId(),
                    lobbySession.getBankId(),
                    roomId,
                    -1,
                    sessionId,
                    0, //unknown at the moment, set later
                    lobbySession.getNickname(),
                    lobbySession.getAvatar(),
                    System.currentTimeMillis(),
                    lobbySession.getCurrency(),
                    playerStats,
                    lobbySession.isShowRefreshButton(),
                    null,
                    playerQuests,
                    playerStake,
                    lobbySession.getStakesReserve(),
                    lobbySession.getWeaponMode(),
                    lobbySession.isAllowWeaponSaveInAllGames());
        }
    }

    private IRoomPlayerInfo createSpecialModeRoomPlayerInfo(long tournamentOrBonusId, String sessionId, long roomId, LobbySession lobbySession,
                                                            long playerStake, MoneyType mode) {
        IActiveFrbSession activeFrbSession = lobbySession.getActiveFrbSession();
        long gameId = lobbySession.getGameId();
        PlayerStats playerStats;
        if (lobbySession.getTournamentSession() != null) {
            long tournamentId = lobbySession.getTournamentSession().getTournamentId();
            playerStats = playerStatsPersister.loadTournamentStats(tournamentId,
                    lobbySession.getBankId(), gameId, lobbySession.getAccountId());
        } else {
            playerStats = new PlayerStats();
        }
        PlayerQuests playerQuests = playerQuestsPersister.loadSpecialModeQuests(tournamentOrBonusId,
                lobbySession.getBankId(), gameId, lobbySession.getAccountId(),
                Money.fromCents(activeFrbSession == null ? playerStake : activeFrbSession.getStake()), mode.ordinal());

        getLog().debug("createSpecialModeRoomPlayerInfo: accountId={}, playerQuests={}", lobbySession.getAccountId(),
                playerQuests);

        IRoomPlayerInfo roomPlayerInfo = new DefaultRoomPlayerInfo(
                lobbySession.getAccountId(),
                lobbySession.getBankId(),
                roomId,
                -1,
                sessionId,
                0,
                lobbySession.getNickname(),
                (Avatar) lobbySession.getAvatar(),
                System.currentTimeMillis(),
                (Currency) lobbySession.getCurrency(),
                playerStats,
                lobbySession.isShowRefreshButton(),
                null,
                playerQuests,
                activeFrbSession == null ? playerStake : activeFrbSession.getStake(),
                lobbySession.getStakesReserve(),
                lobbySession.getWeaponMode(),
                true);
        roomPlayerInfo.setTournamentSession(lobbySession.getTournamentSession());
        roomPlayerInfo.setActiveFrbSession(lobbySession.getActiveFrbSession());
        roomPlayerInfo.setActiveCashBonusSession(lobbySession.getActiveCashBonusSession());
        return roomPlayerInfo;
    }

    private IBattlegroundRoomPlayerInfo createBattlegroundRoomPlayerInfo(String sessionId, long roomId, LobbySession lobbySession, long playerStake) {
        long gameId = lobbySession.getGameId();
        PlayerStats playerStats = playerStatsPersister.load(lobbySession.getBankId(), gameId, lobbySession.getAccountId());

        if (lobbySession.getGameId() == GameType.BG_MAXCRASHGAME.getGameId()) {
            return new CrashGameBGRoomPlayerInfo(lobbySession.getAccountId(),
                    lobbySession.getBankId(),
                    roomId,
                    -1,
                    sessionId,
                    0,
                    lobbySession.getNickname(),
                    lobbySession.getAvatar(),
                    System.currentTimeMillis(),
                    lobbySession.getCurrency(),
                    playerStats,
                    lobbySession.isShowRefreshButton(),
                    null,
                    playerStake,
                    lobbySession.getStakesReserve(),
                    lobbySession.getWeaponMode(),
                    true,
                    lobbySession.getBattlegroundRakePercent());
        } else {
            return new BattlegroundRoomPlayerInfo(
                    lobbySession.getAccountId(),
                    lobbySession.getBankId(),
                    roomId,
                    -1,
                    sessionId,
                    0, //unknown at the moment, set later
                    lobbySession.getNickname(),
                    lobbySession.getAvatar(),
                    System.currentTimeMillis(),
                    lobbySession.getCurrency(),
                    playerStats,
                    lobbySession.isShowRefreshButton(),
                    playerStake,
                    lobbySession.getStakesReserve(),
                    lobbySession.getWeaponMode(),
                    lobbySession.isAllowWeaponSaveInAllGames(),
                    lobbySession.getBattlegroundRakePercent(),
                    lobbySession.isPrivateRoom(),
                    lobbySession.isOwner());
        }
    }

    private IRoomPlayerInfo getThisRoomPlayerInfo(IGameSocketClient client) {
        return playerInfoService.get(client.getAccountId());
    }

    @SuppressWarnings("unchecked")
    private void sitIn(SitIn message, IGameSocketClient client, IRoom room, IRoomPlayerInfo roomPlayer, boolean isNewPlayerInfo) {
        IRoomInfo roomInfo = room.getRoomInfo();
        GameType gameType = roomInfo.getGameType();
        MoneyType moneyType = roomInfo.getMoneyType();

        try {
            boolean needWaitSitOut = sitOutPlayerFromOldRooms(client.getAccountId(), roomInfo, client);

            if (needWaitSitOut) {

                IRoomPlayerInfo playerInfo = null;

                //wait up to 10 sec for sitOut process end
                try {
                    int count = 100;
                    while (count-- > 0) {
                        Thread.sleep(100);
                        playerInfo = playerInfoService.get(client.getAccountId());
                        if (playerInfo == null) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    getLog().error("sitIn: Check roomPlayerInfo after sitOut failed", e);
                    sendErrorMessage(client, ErrorCodes.NEED_SITOUT, "Sitout failed", message.getRid());
                    return;
                }

                getLog().debug("sitIn: sitOut, found roomPlayerInfo={}", playerInfo);

                if (playerInfo != null) {
                    if (hasPendingOperation(playerInfo)) {
                        sendErrorMessage(client, FOUND_PENDING_OPERATION,
                                "Found pending payment operation", message.getRid());
                    } else {
                        sendErrorMessage(client, NEED_SITOUT,
                                "SitOut in progress, try sitIn later", message.getRid());
                    }
                    return;
                }
            }

            if (roomInfo.isBattlegroundMode()) {
                if (message.getStake() != roomInfo.getBattlegroundBuyIn() && !roomInfo.getGameType().isCrashGame()) {
                    sendErrorMessage(client, INTERNAL_ERROR,
                            "Try launch wrong stake for battle mode of room", message.getRid());
                    return;
                }
                if (!roomInfo.getMoneyType().equals(MoneyType.REAL)) {
                    sendErrorMessage(client, INTERNAL_ERROR,
                            "Battle allowed only in real mode", message.getRid());
                    return;
                }
            }

            String baseCurrency = roomInfo.getCurrency();
            String playerCode = roomPlayer.getCurrency().getCode();
            double rate = currencyRateService.get(playerCode, baseCurrency).getRate();
            getLog().debug("rate: {}, playerCode: {}, baseCurrency: {}", rate, playerCode, baseCurrency);
            if (rate < 0) {
                if (playerCode.equals(baseCurrency)) {
                    rate = 1.0;
                } else {
                    CurrencyRate currencyRate = new CurrencyRate(playerCode, baseCurrency, -2, System.currentTimeMillis());
                    CurrencyRate updatedRate = socketService.getCurrencyRatesSync(currencyRate);

                    getLog().debug("Success update rate: {}", updatedRate);
                    currencyRateService.updateOneCurrencyToCache(updatedRate);
                    rate = updatedRate.getRate();

                    if (rate < 0) {
                        sendErrorMessage(client, INTERNAL_ERROR, "SitIn: Currency rate is wrong",
                                message.getRid());
                        getLog().error("SitIn: Currency rate is wrong");
                        return;
                    }
                }
            }

            ISeat seat = room.createSeat(roomPlayer, client, rate);
            ISeat oldSeat;
            //lock required for prevent duplicate request handling
            playerInfoService.lock(seat.getAccountId());
            try {
                oldSeat = room.tryReconnect(seat);
                if (oldSeat == null) {
                    int sitInResult = -1;

                    if (roomInfo.isBattlegroundMode() && !roomInfo.getGameType().isCrashGame()) {
                        boolean isBot = botConfigInfoService.getByMqNickName(seat.getNickname()) != null;
                        if (isBot) {
                            short realCount = 0;
                            List<ISeat> seats = room.getSeats();
                            for (ISeat seatRoom : seats) {
                                if (seatRoom != null) {
                                    if (botConfigInfoService.getByMqNickName(seatRoom.getNickname()) == null) {
                                        getLog().debug("MQB bot sitIn found realPlayer: {}", seatRoom.getAccountId());
                                        realCount++;
                                    }
                                }
                            }
                            if (realCount > 1) {
                                getLog().debug("MQB bot sitIn is not allowed, accountId: {}, realCount: {}", seat.getAccountId(), realCount);
                                sitInResult = NOT_ALLOWED_SIT_IN_FOR_BOT;
                            }
                        }
                    }

                    if (sitInResult == -1) {
                        sitInResult = room.processSitIn(seat, message);
                    }

                    if (sitInResult == OK) {
                        getLog().debug("sitIn HS lock: {}", seat.getAccountId());
                        try {

                            this.processSitInWithBuyIn(message, client, room, seat, roomPlayer, isNewPlayerInfo);

                        } catch (Exception e) {
                            getLog().error("Cannot sitIn, roomPlayer={}", roomPlayer, e);
                            room.rollbackSitIn(seat);
                            if (!(e instanceof CommonException) && !(e instanceof com.dgphoenix.casino.common.exception.CommonException)) { //error message already sended
                                throw e;
                            }
                        }
                    } else if (sitInResult == TOO_MANY_PLAYER) {
                        sendErrorMessage(client, TOO_MANY_PLAYER, "Too many players", message.getRid());
                    } else if (sitInResult == NOT_ALLOWED_SITIN) {
                        sendErrorMessage(client, NOT_ALLOWED_SITIN, "Not allowed SitIn", message.getRid());
                    } else if (sitInResult == CANNOT_OBTAIN_LOCK) {
                        sendErrorMessage(client, CANNOT_OBTAIN_LOCK, "Cannot obtain room lock", message.getRid());
                    } else if (sitInResult == NOT_ALLOWED_SIT_IN_FOR_BOT) {
                        Error errorMessage = createErrorMessage(NOT_ALLOWED_SIT_IN_FOR_BOT, "Not allowed SitIn for Bot", message.getRid());
                        getLog().warn("error, message=  {}, client={}", errorMessage, client);
                        client.sendMessage(errorMessage);
                    } else {
                        sendErrorMessage(client, sitInResult, "Unknown error", message.getRid());
                    }
                }
            } finally {
                unlock(seat.getAccountId());
            }

            if (oldSeat != null) {
                getLog().debug("sitIn: Old seat found: {}", oldSeat);
                String gsGameMode = getMode(room, client);
                if (oldSeat.isSitOutStarted()) {
                    sendErrorMessage(client, FOUND_PENDING_OPERATION,
                            "Found pending operation", message.getRid());
                    return;
                }
                IActiveFrbSession activeFrbSession;
                IActiveCashBonusSession activeCashBonusSession;
                ITournamentSession tournamentSession;
                Long bonusId = null;
                Long tournamentId = null;
                if (moneyType == MoneyType.FRB) {
                    activeFrbSession = roomPlayer.getActiveFrbSession();
                    if (activeFrbSession == null) {
                        sendErrorMessage(client, INTERNAL_ERROR,
                                "Active FRB session not found", message.getRid());
                        return;
                    }
                    bonusId = activeFrbSession.getBonusId();
                } else if (moneyType == MoneyType.CASHBONUS) {
                    activeCashBonusSession = roomPlayer.getActiveCashBonusSession();
                    if (activeCashBonusSession == null) {
                        sendErrorMessage(client, INTERNAL_ERROR,
                                "Active CashBonus session not found", message.getRid());
                        return;
                    }
                    bonusId = activeCashBonusSession.getId();
                } else if (moneyType == MoneyType.TOURNAMENT) {
                    tournamentSession = roomPlayer.getTournamentSession();
                    if (tournamentSession == null) {
                        sendErrorMessage(client, INTERNAL_ERROR,
                                "Tournament session not found", message.getRid());
                        return;
                    }
                    tournamentId = tournamentSession.getTournamentId();
                }
                long accountId = oldSeat.getAccountId();
                String nickname = nicknamePersister.getNickname(oldSeat.getBankId(), accountId);
                try {
                    processSitInWithoutBuyIn(message, client, room, roomPlayer, roomInfo, gameType, moneyType, seat,
                            oldSeat, gsGameMode, bonusId, tournamentId, accountId, nickname);
                } catch (Exception e) {
                    getLog().error("SitIn: Failed to perform buy in", e);
                    sendErrorMessage(client, INTERNAL_ERROR, "SitIn failed, try again",
                            message.getRid());
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private void unlock(long accountId) {
        if (playerInfoService.isLocked(accountId)) {
            playerInfoService.unlock(accountId);
            getLog().debug("sitIn HS unlock: {}", accountId);
        } else {
            getLog().debug("sitIn HS cannot unlock. not locked: {}", accountId);
        }
    }

    private ISitInResult sitInWithSidRecovery(SitIn message, IGameSocketClient client, IRoomPlayerInfo roomPlayer,
                                              IRoom room, GameType gameType, String mode, Long bonusId,
                                              Long tournamentId, String nickname) throws CommonException {
        String currentSid = client.getSessionId();
        try {
            return socketService.sitIn(currentSid, gameType.getGameId(), mode, message.getLang(), bonusId,
                    roomPlayer.getGameSessionId(), roomPlayer.getExternalRoundId(), room.getId(),
                    roomPlayer.getBuyInCount(), tournamentId, nickname);
        } catch (CommonException e) {
            String expectedSid = extractExpectedSidFromException(e);
            if (!StringUtils.isTrimmedEmpty(expectedSid) && !expectedSid.equals(currentSid)) {
                getLog().warn("Detected stale SID on SitIn, retry with expected SID. receivedSid={}, expectedSid={}",
                        currentSid, expectedSid);
                migrateSessionId(client, roomPlayer, currentSid, expectedSid);
                return socketService.sitIn(expectedSid, gameType.getGameId(), mode, message.getLang(), bonusId,
                        roomPlayer.getGameSessionId(), roomPlayer.getExternalRoundId(), room.getId(),
                        roomPlayer.getBuyInCount(), tournamentId, nickname);
            }
            throw e;
        }
    }

    private void migrateSessionId(IGameSocketClient client, IRoomPlayerInfo roomPlayer, String oldSid, String newSid) {
        client.setSessionId(newSid);
        roomPlayer.setSessionId(newSid);

        LobbySession lobbySession = lobbySessionService.get(oldSid);
        if (lobbySession == null) {
            return;
        }

        lobbySessionService.remove(oldSid);
        lobbySession.setSessionId(newSid);
        lobbySession.setSocketClient(client);
        lobbySessionService.add(lobbySession);
    }

    private String extractExpectedSidFromException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            String expectedSid = extractExpectedSidFromMessage(message);
            if (!StringUtils.isTrimmedEmpty(expectedSid)) {
                return expectedSid;
            }
            current = current.getCause();
        }
        return null;
    }

    private String extractExpectedSidFromMessage(String message) {
        if (StringUtils.isTrimmedEmpty(message) || !message.contains("Mismatch sessionId")) {
            return null;
        }
        int expectedIdx = message.indexOf("expected:");
        if (expectedIdx < 0) {
            return null;
        }
        int start = expectedIdx + "expected:".length();
        int end = message.indexOf(')', start);
        if (end < 0) {
            end = message.length();
        }
        String sid = message.substring(start, end).trim();
        if (sid.endsWith(";")) {
            sid = sid.substring(0, sid.length() - 1).trim();
        }
        return StringUtils.isTrimmedEmpty(sid) ? null : sid;
    }

    private void setLimits(SitInResponse response, IRoom room) {
        if (room instanceof com.betsoft.casino.mp.maxcrashgame.model.AbstractCrashGameRoom) {
            IRoomInfo roomInfo = room.getRoomInfo();
            ICrashGameSetting settings = crashGameSettingsService.getSettings(roomInfo.getBankId(), roomInfo.getGameType().getGameId(),
                    roomInfo.getCurrency());
            if (settings != null) {
                response.setMaxMultiplier(settings.getMaxMultiplier());
                response.setMaxPlayerProfitInRound(settings.getMaxPlayerProfitInRound());
                response.setTotalPlayersProfitInRound(settings.getTotalPlayersProfitInRound());
            }
        }
    }

    private void processSitInWithoutBuyIn(SitIn message, IGameSocketClient client, IRoom room, IRoomPlayerInfo roomPlayer,
                                          IRoomInfo roomInfo, GameType gameType, MoneyType moneyType, ISeat seat,
                                          ISeat oldSeat, String gsGameMode, Long bonusId, Long tournamentId,
                                          long accountId, String nickname) throws CommonException {
        if (hasPendingOperation(roomPlayer)) {
            getLog().warn("processSitInWithOutBuyIn: Cannot sitIn, player has pending operation");
            sendErrorMessage(client, FOUND_PENDING_OPERATION,
                    "Found pending operation", message.getRid());
            throw new CommonException("Cannot sitIn, player has pending operation");
        }
        ISitInResult sitInResult = sitInWithSidRecovery(message, client, roomPlayer, room, gameType,
                gsGameMode, bonusId, tournamentId, nickname);
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (moneyType == MoneyType.REAL || moneyType == MoneyType.FREE || moneyType == MoneyType.FRB) {
            lobbySession.setBalance(sitInResult.getBalance());
            lobbySessionService.add(lobbySession);
        }

        List<Weapon> weapons = new ArrayList<>();
        playerInfoService.lock(accountId);
        ISeat actualSeat = room.getSeatByAccountId(accountId);
        boolean isBattleActionGame = gameType.isBattleGroundGame() && !gameType.isCrashGame();
        getLog().debug("processSitInWithoutBuyIn HS lock: {}", accountId);
        IRoomPlayerInfo roomPlayerInfo;
        Money seatBalance = Money.fromCents(lobbySession.getBalance());
        try {
            roomPlayerInfo = playerInfoService.get(accountId);
            //it's normal, after sitOut player removed, just use copy and put playerInfo back
            if (roomPlayerInfo == null) {
                roomPlayerInfo = roomPlayer;
                roomPlayerInfo.setPendingOperation(false);
                roomPlayerInfo.setEnterDate(System.currentTimeMillis());
                roomPlayerInfo.setRoundBuyInAmount(0);
            } else if (hasPendingOperation(roomPlayerInfo)) {
                getLog().error("Impossible error, after sitIn found pending operation, please fix, " +
                        "playerInfo={}", roomPlayerInfo);
            }

            long buyInAmount = sitInResult.getBuyInAmount();

            if(isBattleActionGame && lobbySession.isConfirmBattlegroundBuyIn() && roomPlayerInfo.getRoundBuyInAmount() == 0){
                Money amount = roomInfo.isBattlegroundMode() ? Money.fromCents(roomInfo.getBattlegroundBuyIn()) : Money.ZERO;
                getLog().debug("Found confirmed battle case without buyIn amount, need buyIn lobbySession: {}, playerInfo: {}, amount: {}",
                        lobbySession, roomPlayerInfo, amount );
                seat.getCurrentPlayerRoundInfo().setRoundStartBalance(seatBalance);
                BuyInResult buyInResult;
                try {
                    buyInResult = socketService.buyIn(client.getServerId(), roomPlayer.getId(),
                            client.getSessionId(), amount, sitInResult.getGameSessionId(), room.getId(),
                            roomPlayer.getBuyInCount(), tournamentId, 0L, room);
                } catch (BuyInFailedException e) {
                    throw new CommonException("BuyIn failed", e);
                }
                buyInAmount = buyInResult.getAmount();
                if (actualSeat.getPlayerInfo() != null) {
                    actualSeat.getPlayerInfo().setRoundBuyInAmount(buyInAmount);
                }
                getLog().debug("buyInAmount after battle buyIn: {}", buyInAmount);
            }

            roomPlayerInfo.setGameSessionId(sitInResult.getGameSessionId());
            roomPlayerInfo.makeBuyIn(sitInResult.getPlayerRoundId(), buyInAmount);
            roomPlayerInfo.setRoomId(room.getId());
            roomPlayerInfo.setSessionId(client.getSessionId());
            if (buyInAmount > 0) {
                roomPlayerInfo.incrementBuyInCount();
            }
            playerInfoService.put(roomPlayerInfo);
            getLog().debug("Success reSitIn: seat={}", actualSeat);
            actualSeat.updatePlayerRoundInfo(sitInResult.getPlayerRoundId());
            if (actualSeat.getPlayerInfo() != null) {
                actualSeat.getPlayerInfo().setGameSessionId(sitInResult.getGameSessionId());
                actualSeat.getPlayerInfo().setSessionId(client.getSessionId());
            }
            if (actualSeat instanceof IActionGameSeat) {
                weapons = convertWeapons((AbstractActionSeat) actualSeat);
            }
            if (actualSeat instanceof IMultiNodeSeat) {
                //need save seat with actual playerInfo
                //noinspection unchecked
                room.saveSeat(getSeatNumber(actualSeat), actualSeat);
            }
        } finally {
            unlock(accountId);
        }
        if (!hasPendingOperation(roomPlayerInfo)) {
            int level = AchievementHelper.getPlayerLevel(roomPlayerInfo.getStats().getScore());

            SitInResponse sitInResponse = new SitInResponse(getCurrentTime(), message.getRid(), getSeatNumber(oldSeat),
                    oldSeat.getNickname(), oldSeat.getJoinDate(), getAmmoAmount(oldSeat),
                    roomInfo.isBattlegroundMode() && !gameType.isCrashGame() ? 0 : lobbySession.getBalance(),
                    (Avatar) oldSeat.getAvatar(), weapons,
                    getConvertedLootboxPrices(room, oldSeat), roomPlayer.isShowRefreshButton(),
                    level, moneyType == MoneyType.FRB,
                    roomPlayerInfo.getActiveFrbSession() != null ? roomPlayerInfo.getActiveFrbSession().getWinSum() : 0,
                    moneyType.name(), lobbySession.getBattlegroundRakePercent()
            );

            setLimits(sitInResponse, room);

            seat.sendMessage(sitInResponse, message);

            if(roomInfo.isPrivateRoom()) {
                if (room instanceof AbstractBattlegroundGameRoom) {
                    ((AbstractBattlegroundGameRoom) room)
                            .updatePlayersStatusAndSendToOwner(
                                    Arrays.asList(seat.getSocketClient()), Status.READY);
                } else if (room instanceof BattleAbstractCrashGameRoom) {
                    ((BattleAbstractCrashGameRoom) room)
                            .updatePlayersStatusAndSendToOwner(
                                    Arrays.asList(seat.getSocketClient()), Status.WAITING);
                }
            }

            IRoomTemplate roomTemplate;
            long templateId = roomInfo.getTemplateId();
            IRoomInfoService roomInfoService = getRoomInfoService(client);
            roomTemplate = roomInfoService.getTemplate(templateId);

            roomInfoService.checkAndCreateForTemplate(roomPlayer.getBankId(), roomTemplate,
                    roomInfo.getStake(), roomInfo.getCurrency());

            makeTournamentBuyInAndSendWeapons(room, seat, moneyType);
        }
    }

    private boolean hasPendingOperation(IRoomPlayerInfo roomPlayer) {
        if (roomPlayer.isPendingOperation()) {
            getLog().debug("hasPendingOperation: roomPlayer.isPendingOperation()={}", roomPlayer.isPendingOperation());
            return true;
        }
        IPendingOperation operation = pendingOperationService.get(roomPlayer.getId());
        if (operation != null) {
            getLog().debug("hasPendingOperation: found pending operation={}", operation);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<Weapon> convertWeapons(IActionGameSeat seat) {
        List<Weapon> weapons = new ArrayList<>();
        Map<SpecialWeaponType, IWeapon> weaponsMap = seat.getWeapons();
        for (Map.Entry<SpecialWeaponType, IWeapon> entry : weaponsMap.entrySet()) {
            weapons.add(new Weapon(entry.getKey().getId(), entry.getValue().getShots()));
        }
        return weapons;
    }
    private void makeBuyInForTournament(IRoom gameRoom, ISeat seat) {
        if (seat != null && seat.getPlayerInfo() != null) {
            IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            if (playerInfo.getTournamentSession() != null) {
                //noinspection unchecked
                gameRoom.makeBuyInForTournament(seat);
            }
        }
    }

    private void sendWeaponsInfo(IRoom gameRoom, ISeat seat) {
        if (seat instanceof IActionGameSeat) {
            boolean frb = gameRoom.getRoomInfo().getMoneyType() == MoneyType.FRB;
            ITransportObjectsFactoryService toFactoryService = gameRoom.getTOFactoryService();
            IActionGameSeat actionGameSeat = (IActionGameSeat) seat;
            IWeapons weapons = toFactoryService.createWeapons(System.currentTimeMillis(), SERVER_RID,
                    actionGameSeat.getAmmoAmount(), frb, getSeatWeapons(actionGameSeat, toFactoryService));
            seat.sendMessage(weapons);
        }
    }

    private List<ITransportWeapon> getSeatWeapons(IActionGameSeat seat, ITransportObjectsFactoryService toFactoryService) {
        List<ITransportWeapon> weapons = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<SpecialWeaponType, IWeapon> seatWeapons = seat.getWeapons();
        seatWeapons.forEach((specialWeaponType, weapon) -> weapons.add(toFactoryService.createWeapon(specialWeaponType.getId(), weapon.getShots())));
        return weapons;
    }

    //return true if sitOut from other room found
    private boolean sitOutPlayerFromOldRooms(long accountId, IRoomInfo currentSitInRoom, IGameSocketClient client) throws CommonException {
        IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
        if (playerInfo == null) {
            getLog().debug("sitOutPlayerFromOldRooms: player not found in any room, accountId={}", accountId);
            return false;
        }
        if (playerInfo.getRoomId() == currentSitInRoom.getId()) {
            getLog().debug("sitOutPlayerFromOldRooms: player found in currentSitInRoom, accountId={}", accountId);
            return false;
        }
        IRoomInfoService roomInfoService = getRoomInfoService(client);
        IRoomInfo roomInfo = roomInfoService.getRoom(playerInfo.getRoomId());
        IRoom room = roomServiceFactory.getRoomAbs(currentSitInRoom.getGameType(), playerInfo.getRoomId());
        if (roomInfo.isBattlegroundMode() && room != null) {
            RoomState roomState = room.getGameState().getRoomState();
            if (roomState.equals(RoomState.PLAY) || roomState.equals(RoomState.QUALIFY)) {
                throw new CommonException("Cannot sitOut player from battleground room");
            }
        }
        int seatNumber = roomInfo instanceof ISingleNodeRoomInfo ? playerInfo.getSeatNumber() : 0;
        if (room != null) {
            //noinspection unchecked
            room.processSitOut(null, null, seatNumber, playerInfo.getId(), true);
        } else {
            getLog().debug("sitOutPlayerFromOldRooms: Cannot sitOut player, room not found or placed on other server, " +
                    "roomInfo={}, playerInfo={}", roomInfo, playerInfo);

            SitOutTask sitOutTask = new SitOutTask(playerInfo.getRoomId(), accountId, seatNumber);

            playerInfoService
                    .getNotifyService()
                    .executeOnAllMembers(sitOutTask);
        }

        return true;
    }

    private void processSitInWithBuyIn(SitIn inboundMessage, IGameSocketClient client, IRoom room, ISeat seat,
                                       IRoomPlayerInfo roomPlayer, boolean isNewPlayerInfo) throws CommonException {
        if (hasPendingOperation(roomPlayer)) {
            getLog().warn("processSitInWithBuyIn: Cannot sitIn, player has pending operation");
            sendErrorMessage(client, FOUND_PENDING_OPERATION,
                    "Found pending operation", inboundMessage.getRid());
            throw new CommonException("Cannot sitIn, player has pending operation");
        }
        roomPlayer.setPendingOperation(true, "SitIn");
        playerInfoService.put(roomPlayer);
        String mode = getMode(room, client);
        IRoomInfoService roomInfoService = getRoomInfoService(client);
        MoneyType moneyType = roomInfoService.getRoom(room.getId()).getMoneyType();
        IRoomInfo roomInfo = room.getRoomInfo();
        GameType gameType = roomInfo.getGameType();
        IActiveFrbSession activeFrbSession;
        IActiveCashBonusSession activeCashBonusSession;
        ITournamentSession tournamentSession;
        Long bonusId = null;
        Long tournamentId = null;

        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if(lobbySession == null){
            sendErrorMessage(client, INTERNAL_ERROR,"processSitInWithBuyIn lobby session is not found, sit in not allow", inboundMessage.getRid());
            return;
        }

        if (moneyType == MoneyType.FRB) {
            activeFrbSession = roomPlayer.getActiveFrbSession();
            if (activeFrbSession == null) {
                sendErrorMessage(client, INTERNAL_ERROR,
                        "Active FRB session not found", inboundMessage.getRid());
                return;
            }
            bonusId = activeFrbSession.getBonusId();
        } else if (moneyType == MoneyType.CASHBONUS) {
            activeCashBonusSession = roomPlayer.getActiveCashBonusSession();
            if (activeCashBonusSession == null || !activeCashBonusSession.isActive()) {
                sendErrorMessage(client, INTERNAL_ERROR,
                        "Active CashBonus session not found", inboundMessage.getRid());
                return;
            }
            bonusId = activeCashBonusSession.getId();
        } else if (moneyType == MoneyType.TOURNAMENT) {
            tournamentSession = roomPlayer.getTournamentSession();
            if (tournamentSession == null) {
                sendErrorMessage(client, INTERNAL_ERROR,
                        "Tournament session not found", inboundMessage.getRid());
                return;
            }
            tournamentId = tournamentSession.getTournamentId();
        }

        Money amount = roomInfo.isBattlegroundMode() ? Money.fromCents(roomInfo.getBattlegroundBuyIn()) : Money.ZERO;
        Money seatBalance = Money.fromCents(lobbySession.getBalance());

        String nickname = nicknamePersister.getNickname(roomPlayer.getBankId(), roomPlayer.getId());

        try {
            ISitInResult sitInResult = sitInWithSidRecovery(inboundMessage, client, roomPlayer, room, gameType,
                    mode, bonusId, tournamentId, nickname);
            BuyInResult buyInResult = null;
            if (amount.greaterThan(Money.ZERO)) {
                if (gameType.isBattleGroundGame() && !gameType.isCrashGame() && lobbySession.isConfirmBattlegroundBuyIn()
                        && roomPlayer.getRoundBuyInAmount() > 0) {
                    getLog().debug("processSitInWithBuyIn: isBattleActionGame processSitInWithBuyIn: " +
                                    "player has buyIn. not need BuyIn new seat: {}, roomPlayer: {}", seat, roomPlayer);
                } else {
                    seat.getCurrentPlayerRoundInfo().setRoundStartBalance(seatBalance);
                    buyInResult = socketService.buyIn(client.getServerId(), roomPlayer.getId(),
                            client.getSessionId(), amount, sitInResult.getGameSessionId(), room.getId(),
                            roomPlayer.getBuyInCount(), tournamentId, 0L, room);
                }
            }

            lobbySession = lobbySessionService.get(client.getSessionId());
            if (moneyType == MoneyType.REAL || moneyType == MoneyType.FREE || moneyType == MoneyType.FRB) {
                lobbySession.setBalance(sitInResult.getBalance());
                lobbySessionService.add(lobbySession);
            }
            seat.updatePlayerRoundInfo(sitInResult.getPlayerRoundId());
            List<Weapon> weapons = new ArrayList<>();
            if (seat instanceof IActionGameSeat) {
                @SuppressWarnings("unchecked")
                Map<SpecialWeaponType, IWeapon> weaponsMap = ((IActionGameSeat) seat).getWeapons();
                weaponsMap.forEach((type, weapon) -> weapons.add(new Weapon(type.getId(), weapon.getShots())));
            }
            int level = AchievementHelper.getPlayerLevel(roomPlayer.getStats().getScore());
            getLog().debug("processSitInWithBuyIn: New seat: {}", seat);

            SitInResponse allMessage = new SitInResponse(getCurrentTime(), SERVER_RID, getSeatNumber(seat), seat.getNickname(),
                    getCurrentTime(), 0, 0, (Avatar) seat.getAvatar(), weapons,
                    getConvertedLootboxPrices(room, seat), false, level, moneyType == MoneyType.FRB, 0,
                    moneyType.name(), lobbySession.getBattlegroundRakePercent());

            setLimits(allMessage, room);

            SitInResponse seatMessage = new SitInResponse(getCurrentTime(), inboundMessage.getRid(), getSeatNumber(seat), seat.getNickname(),
                    getCurrentTime(), getAmmoAmount(seat),
                    (roomInfo.isBattlegroundMode() && !gameType.isCrashGame()) ? 0 : lobbySession.getBalance(),
                    (Avatar) seat.getAvatar(),
                    weapons, getConvertedLootboxPrices(room, seat),
                    roomPlayer.isShowRefreshButton(), level, moneyType == MoneyType.FRB,
                    roomPlayer.getActiveFrbSession() != null ? roomPlayer.getActiveFrbSession().getWinSum() : 0,
                    moneyType.name(), lobbySession.getBattlegroundRakePercent());

            setLimits(seatMessage, room);

            room.sendChanges(allMessage, seatMessage, seat.getAccountId(), inboundMessage);

            if(roomInfo.isPrivateRoom() && room instanceof AbstractBattlegroundGameRoom) {
                try {
                    ((AbstractBattlegroundGameRoom) room)
                            .updatePlayersStatusAndSendToOwner(Arrays.asList(seat.getSocketClient()), Status.READY);
                } catch (Exception e) {
                    getLog().error("processSitInWithBuyIn: Exception to updatePlayersStatusAndSentToOwner, " +
                            "{}", e.getMessage(), e);
                }
            }

            IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            playerInfo.setGameSessionId(sitInResult.getGameSessionId());
            if (seat instanceof ISingleNodeSeat) {
                playerInfo.setSeatNumber(getSeatNumber(seat));
            }
            if (roomPlayer.getActiveFrbSession() != null) {
                setAmmoAmount(seat, roomPlayer.getActiveFrbSession().getCurrentAmmoAmount());
                playerInfo.makeBuyIn(sitInResult.getPlayerRoundId(), 0);
            } else if (roomPlayer.getActiveCashBonusSession() != null) {
                playerInfo.makeBuyIn(sitInResult.getPlayerRoundId(),
                        roomPlayer.getActiveCashBonusSession().getBalance());
            } else if (roomPlayer.getTournamentSession() != null) {
                playerInfo.makeBuyIn(sitInResult.getPlayerRoundId(), 0);
            } else if (room.isBattlegroundMode()) {
                if (buyInResult != null) {
                    int battlegroundAmmoAmount = room.getRoomInfo().getBattlegroundAmmoAmount();
                    setAmmoAmount(seat, battlegroundAmmoAmount);
                    playerInfo.makeBuyIn(buyInResult.getPlayerRoundId(), buyInResult.getAmount());
                } else {
                    getLog().error("Impossible error, battleground mode and null buyInResult");
                }
            } else {
                playerInfo.makeBuyIn(sitInResult.getPlayerRoundId(), sitInResult.getBuyInAmount());
            }
            playerInfo.setRoomId(roomInfo.getId());
            if (sitInResult.getBuyInAmount() > 0 || buyInResult != null) {
                playerInfo.incrementBuyInCount();
            }
            playerInfo.setPendingOperation(false);
            playerInfo.setEnterDate(System.currentTimeMillis());
            playerInfoService.put(playerInfo);
            if (seat instanceof IMultiNodeSeat) {
                //need save seat with actual playerInfo
                //noinspection unchecked
                room.saveSeat(getSeatNumber(seat), seat);
            }
            getLog().debug("processSitInWithBuyIn: success seat={}", seat);
            lobbySession = lobbySessionService.get(client.getSessionId());
            if (lobbySession != null) {
                lobbySession.setRoomId(room.getId());
                lobbySessionService.add(lobbySession);
                getLog().debug("processSitInWithBuyIn: Found lobbySession={}", lobbySession);
            } else {
                getLog().warn("processSitInWithBuyIn: not found lobbySession for sid={}", client.getSessionId());
            }
            IRoomTemplate roomTemplate;
            long templateId = roomInfo.getTemplateId();
            roomTemplate = roomInfoService.getTemplate(templateId);
            roomInfoService.checkAndCreateForTemplate(roomPlayer.getBankId(), roomTemplate,
                    roomInfo.getStake(), roomInfo.getCurrency());
            makeTournamentBuyInAndSendWeapons(room, seat, moneyType);
        } catch (Exception e) {
            getLog().error("processSitInWithBuyIn: failed to perform processSitInWithBuyIn", e);
            if (e instanceof BuyInFailedException) {
                BuyInFailedException bfExc = (BuyInFailedException) e;
                if (bfExc.getErrorCode() > 0) {
                    sendErrorMessage(client,
                            translateGameServerErrorCode(bfExc.getErrorCode()),
                            "BuyIn failed, reason: " + e.getMessage(), inboundMessage.getRid());
                } else {
                    sendErrorMessage(client,
                            bfExc.isFatal() ? BAD_BUYIN : NOT_FATAL_BAD_BUYIN,
                            "ReBuy failed, reason: " + e.getMessage(), inboundMessage.getRid());
                }
                if (!bfExc.isPlayerAlreadySitOut()) {
                    //noinspection unchecked
                    room.rollbackSitIn(seat);
                }
                if (isNewPlayerInfo) {
                    playerInfoService.remove(roomInfoService, roomPlayer.getRoomId(), roomPlayer.getId());
                }
            } else {
                //noinspection unchecked
                room.rollbackSitIn(seat);
                sendErrorMessage(client, INTERNAL_ERROR, "SitIn in failed, try again", inboundMessage.getRid());
            }
        }
    }

    private void makeTournamentBuyInAndSendWeapons(IRoom room, ISeat seat, MoneyType moneyType) {
        if (moneyType == MoneyType.TOURNAMENT && getAmmoAmount(seat) <= 0) {
            makeBuyInForTournament(room, seat);
            sendWeaponsInfo(room, seat);
        }
    }

    private int getAmmoAmount(ISeat seat) {
        return seat instanceof IActionGameSeat ? ((IActionGameSeat) seat).getAmmoAmount() : 0;
    }

    private void setAmmoAmount(ISeat seat, int ammoAmount) {
        if (seat instanceof IActionGameSeat) {
            ((IActionGameSeat) seat).setAmmoAmount(ammoAmount);
        }
    }
    private String getMode(IRoom room, IGameSocketClient client) {
        IRoomInfoService roomInfoService = getRoomInfoService(client);
        return roomInfoService.getRoom(room.getId()).getMoneyType().toString().toLowerCase();
    }

    @SuppressWarnings("unchecked")
    protected List<Double> getConvertedLootboxPrices(IRoom room, ISeat seat) {
        List<Double> prices = new ArrayList<>();
        if (room instanceof AbstractActionGameRoom) {
            List<Money> weaponLootBoxPrices = ((AbstractActionGameRoom) room).getWeaponLootBoxPrices((IActionGameSeat) seat);
            for (Money lootBoxPrice : weaponLootBoxPrices) {
                prices.add(lootBoxPrice.toDoubleCents());
            }
        }
        return prices;
    }

    protected int getSeatNumber(ISeat seat) {
        if (seat instanceof ISingleNodeSeat) {
            return ((ISingleNodeSeat) seat).getNumber();
        } else {
            return 0;
        }
    }

    @Override
    public IRoomInfoService getRoomInfoService(IGameSocketClient client) {
        if (client.isPrivateRoom()) {
            return client.getGameType().isSingleNodeRoomGame() ? bgPrivateRoomInfoService : multiNodePrivateRoomInfoService;
        }
        return super.getRoomInfoService(client);
    }
}
