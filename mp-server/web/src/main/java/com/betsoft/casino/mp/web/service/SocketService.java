package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.common.AbstractGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.kafka.KafkaMessageService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.IBgPlace;
import com.betsoft.casino.mp.model.friends.Friend;
import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.IQuestAmount;
import com.betsoft.casino.mp.model.quests.ITreasureProgress;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.payment.AddWinPendingOperation;
import com.betsoft.casino.mp.payment.BuyInPendingOperation;
import com.betsoft.casino.mp.payment.SitOutPendingOperation;
import com.betsoft.casino.mp.service.ISocketService;
import com.betsoft.casino.mp.service.PendingOperationService;
import com.betsoft.casino.mp.transport.CrashGameSetting;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.utils.BGFStatusUtil;
import com.betsoft.casino.mp.utils.BGOStatusUtil;
import com.betsoft.casino.mp.utils.BGStatusUtil;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.teststand.TestStandError;
import com.betsoft.casino.teststand.TestStandLocal;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.mp.*;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.kafka.dto.AddBatchWinRequestDto;
import com.abs.casino.kafka.dto.AddBatchWinResponseDto;
import com.abs.casino.kafka.dto.AddMQReservedNicknamesRequest;
import com.abs.casino.kafka.dto.AddWinRequestDto;
import com.abs.casino.kafka.dto.AddWinResultDto;
import com.abs.casino.kafka.dto.AddWinWithSitOutRequest;
import com.abs.casino.kafka.dto.BGFriendDto;
import com.abs.casino.kafka.dto.BGOnlinePlayerDto;
import com.abs.casino.kafka.dto.BGPlayerDto;
import com.abs.casino.kafka.dto.BGUpdatePrivateRoomRequest;
import com.abs.casino.kafka.dto.BGUpdateRoomResultDto;
import com.abs.casino.kafka.dto.BattlegroundRoundInfoDto;
import com.abs.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.BuyInRequest;
import com.abs.casino.kafka.dto.BuyInResultDto;
import com.abs.casino.kafka.dto.CashBonusDto;
import com.abs.casino.kafka.dto.CheckBuyInRequest;
import com.abs.casino.kafka.dto.CloseFRBonusAndSessionRequest;
import com.abs.casino.kafka.dto.CloseFRBonusResultDto;
import com.abs.casino.kafka.dto.CloseGameSessionRequest;
import com.abs.casino.kafka.dto.CrashGameSettingDto;
import com.abs.casino.kafka.dto.CrashGameSettingsResponseDto;
import com.abs.casino.kafka.dto.CurrencyRateDto;
import com.abs.casino.kafka.dto.DetailedPlayerInfo2Dto;
import com.abs.casino.kafka.dto.FinishGameSessionAndMakeSitOutRequest;
import com.abs.casino.kafka.dto.GetBalanceRequest;
import com.abs.casino.kafka.dto.GetBatchAddWinStatusRequest;
import com.abs.casino.kafka.dto.GetCrashGamesSettingsRequest;
import com.abs.casino.kafka.dto.GetDetailedPlayerInfo2Request;
import com.abs.casino.kafka.dto.GetExternalAccountIdsRequest;
import com.abs.casino.kafka.dto.GetExternalAccountIdsResponseDto;
import com.abs.casino.kafka.dto.GetFriendsRequest;
import com.abs.casino.kafka.dto.GetFriendsResponseDto;
import com.abs.casino.kafka.dto.GetMQDataRequest;
import com.abs.casino.kafka.dto.GetOnlineStatusRequest;
import com.abs.casino.kafka.dto.GetOnlineStatusResponseDto;
import com.abs.casino.kafka.dto.GetPaymentOperationStatus2Request;
import com.abs.casino.kafka.dto.GetServerRunningRoomsRequest;
import com.abs.casino.kafka.dto.GetServerRunningRoomsResponse;
import com.abs.casino.kafka.dto.InvitePlayersToPrivateRoomRequest;
import com.abs.casino.kafka.dto.KafkaHandlerException;
import com.abs.casino.kafka.dto.KafkaResponse;
import com.abs.casino.kafka.dto.LeaveMultiPlayerLobbyRequest;
import com.abs.casino.kafka.dto.LongResponseDto;
import com.abs.casino.kafka.dto.MQDataDto;
import com.abs.casino.kafka.dto.MQDataWrapperDto;
import com.abs.casino.kafka.dto.MQQuestAmountDto;
import com.abs.casino.kafka.dto.MQQuestDataDto;
import com.abs.casino.kafka.dto.MQQuestPrizeDto;
import com.abs.casino.kafka.dto.MQTreasureQuestProgressDto;
import com.abs.casino.kafka.dto.NotifyPrivateRoomWasDeactivatedRequest;
import com.abs.casino.kafka.dto.PlaceDto;
import com.abs.casino.kafka.dto.PushOnlineRoomsPlayersRequest;
import com.abs.casino.kafka.dto.RMSPlayerDto;
import com.abs.casino.kafka.dto.RMSRoomDto;
import com.abs.casino.kafka.dto.RefundBuyInRequest;
import com.abs.casino.kafka.dto.RemoveMQReservedNicknamesRequest;
import com.abs.casino.kafka.dto.RoundInfoResultDto;
import com.abs.casino.kafka.dto.RoundPlayerDto;
import com.abs.casino.kafka.dto.SaveCashBonusRoundResultRequest;
import com.abs.casino.kafka.dto.SavePlayerBetForFRBRequest;
import com.abs.casino.kafka.dto.SaveTournamentRoundResultRequest;
import com.abs.casino.kafka.dto.SessionTouchRequest;
import com.abs.casino.kafka.dto.SitInRequest;
import com.abs.casino.kafka.dto.SitInResponseDto;
import com.abs.casino.kafka.dto.SitOutCashBonusSessionRequest;
import com.abs.casino.kafka.dto.SitOutCashBonusSessionResultDto;
import com.abs.casino.kafka.dto.SitOutRequest;
import com.abs.casino.kafka.dto.SitOutResultDto;
import com.abs.casino.kafka.dto.SitOutTournamentSessionRequest;
import com.abs.casino.kafka.dto.SitOutTournamentSessionResultDto;
import com.abs.casino.kafka.dto.StartNewRoundForManyPlayersRequest;
import com.abs.casino.kafka.dto.StartNewRoundForManyPlayersResponseDto;
import com.abs.casino.kafka.dto.StartNewRoundRequest;
import com.abs.casino.kafka.dto.StartNewRoundResponseDto;
import com.abs.casino.kafka.dto.StringResponseDto;
import com.abs.casino.kafka.dto.TournamentInfoDto;
import com.abs.casino.kafka.dto.UpdateCurrencyRatesRequestResponse;
import com.abs.casino.kafka.dto.UpdatePlayersStatusInPrivateRoomRequest;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.utils.KafkaResponseConverterUtil.convertToType;


@Service
/**
 * Main class for communication between mq and gs side via thrift/kafka calls
 */
public class SocketService implements ISocketService {
    private static final Logger LOG = LogManager.getLogger();
    private final PendingOperationService pendingOperationService;
    private final KafkaMessageService kafkaMessageService;
    private static final Set<String> FAKE_CURRENCIES = new HashSet<>(Arrays.asList("FUN", "MBT","TKN"));

    private final ServerConfigService serverConfigService;

    public SocketService(PendingOperationService pendingOperationService,
                         KafkaMessageService kafkaMessageService,
                         ServerConfigService serverConfigService) {
        this.pendingOperationService = pendingOperationService;
        this.kafkaMessageService = kafkaMessageService;
        this.serverConfigService = serverConfigService;
    }

    private Exception getKExceptionIfPossible(Exception e) {
        if (e instanceof KafkaHandlerException) {
            return (KafkaHandlerException) e.getCause();
        }
        return e;
    }

    public DetailedPlayerInfo2Dto getDetailedPlayerInfo(String sessionId,
                                                        long gameIdForFrbCheck,
                                                        String mode,
                                                        Long bonusId,
                                                        Long tournamentId)
            throws CommonException {
        GetDetailedPlayerInfo2Request getDetailedPlayerInfo2Request =
                new GetDetailedPlayerInfo2Request(sessionId, gameIdForFrbCheck, mode,
                        bonusId == null ? -1 : bonusId, tournamentId == null ? -1 : tournamentId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(getDetailedPlayerInfo2Request);
        try {
            return convertToType(response, (r) -> new DetailedPlayerInfo2Dto(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
    }

    public List<GetServerRunningRoomsResponse> getAllRemoteServersRunningRooms(Long gameId) throws CommonException {
        List<GetServerRunningRoomsResponse> responsesFromServers = Collections.synchronizedList(new ArrayList<>());

        Set<Integer> serverIds = serverConfigService.getConfigsMap().keySet();
        int localServerId = serverConfigService.getServerId();

        if (serverIds == null || serverIds.isEmpty()) {
            LOG.debug("getAllRemoteServersRunningRooms: serverIds is null or empty for gameId={}", gameId);
            return responsesFromServers;
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(serverIds.size(), 10)); // limit threads

        List<CompletableFuture<Void>> futures = serverIds.stream()
                .filter(serverId -> serverId != null && serverId != localServerId && serverId != IRoomInfo.NOT_ASSIGNED_ID)
                .map(serverId ->
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return getRemoteServerRunningRooms(serverId, gameId);
                            } catch (Exception exception) {
                                LOG.error("getAllRemoteServersRunningRooms: exception getting rooms for serverId={}, gameId={}", serverId, gameId, exception);
                                return null;
                            }
                        }, executor).thenAccept(response -> {
                            if (response != null) {
                                responsesFromServers.add(response);
                            }
                        })
                )
                .collect(Collectors.toList());

        // Wait for all to complete
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error waiting for remote server responses serverIds={}, gameId={}", serverIds, gameId, e);
            throw new CommonException("Error waiting for remote server responses", e);
        }

        executor.shutdown();

        responsesFromServers.sort(
                Comparator.comparing(GetServerRunningRoomsResponse::getServerId)
        );

        return responsesFromServers;
    }

    public GetServerRunningRoomsResponse getRemoteServerRunningRooms(int serverId, Long gameId) throws CommonException {

        LOG.debug("Error waiting for remote server responses serverId={}, gameId={}", serverId, gameId);

        GetServerRunningRoomsRequest getServerRunningRoomsRequest = new GetServerRunningRoomsRequest(gameId);
        Mono<KafkaResponse> getServerRunningRoomsResponse = kafkaMessageService.syncRequestToSpecificMP(getServerRunningRoomsRequest, serverId);

        try {

            return convertToType(getServerRunningRoomsResponse, (r) -> new GetServerRunningRoomsResponse(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));

        } catch (Exception e) {
            LOG.error("getRemoteServerRunningRooms: Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
    }

    /**
     * Start gameSession and close old if required
     *
     * @param sessionId player session identifier
     * @param gameId game identifier
     * @param mode game play mode, free|real|frb|toournament
     * @param lang game client language
     * @param bonusId bonus identifier for bonus session
     * @param oldGameSessionId old gameSessionId if player restart game
     * @param oldRoundId old roundId if player restart game
     * @param roomId MQ room identifier
     * @param betNumber MQ room bet serial number
     * @param tournamentId tournament identifier for tournament mode
     * @param nickname MQ side player nickname
     * @return {@code SitInResultDto} call result
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public ISitInResult sitIn(String sessionId, long gameId, String mode, String lang, Long bonusId,
                              long oldGameSessionId, long oldRoundId, long roomId, int betNumber, Long tournamentId,
                              String nickname) throws CommonException {
        final long now = System.currentTimeMillis();
        LOG.debug("sitIn request: sessionId={}, gameId={}, mode={}, lang={}, bonusId={}, oldGameSessionId={}, " +
                        "oldRoundId={}, roomId={}, betNumber={}, tournamentId, tournamentId: {}", sessionId, gameId,
                mode, lang, bonusId, oldGameSessionId, oldRoundId, roomId, betNumber, tournamentId);
        MutableObject<ISitInResult> mutable = new MutableObject<>();
        try {
            SitInRequest request = new SitInRequest(sessionId, gameId, mode, lang,
                    bonusId == null ? -1 : bonusId, oldGameSessionId, oldRoundId, roomId, betNumber,
                    tournamentId == null ? -1 : tournamentId, nickname);

            Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomGs(request);
            try {
                SitInResponseDto result = convertToType(response, (r) -> new SitInResponseDto(r.isSuccess(), r.getStatusCode(), r.getReasonPhrases()));

                SitInResult sitInResult = new SitInResult(result.getGameSessionId(), result.getBalance(),
                        result.getAmount(), result.getPlayerRoundId());
                LOG.debug("sitIn: sessionId={}, sitInResult={}", sessionId, sitInResult);
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: sitIn", System.currentTimeMillis() - now, sessionId);
                mutable.setValue(sitInResult);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("sitIn error, sessionId={}", sessionId, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    "SocketService: sitIn [error]", System.currentTimeMillis() - now, sessionId);
            throw new CommonException("Kafka call error", e);
        }
        return mutable.getValue();
    }

    private BattlegroundRoundInfoDto convert(IBattlegroundRoundInfo bgRoundInfo) {
        if (bgRoundInfo == null) {
            return null;
        }
        return new BattlegroundRoundInfoDto(bgRoundInfo.getBuyIn(), bgRoundInfo.getWinAmount(),
                bgRoundInfo.getBetsSum(), bgRoundInfo.getWinSum(), convertIBgPlace(bgRoundInfo.getPlaces()),
                bgRoundInfo.getStatus(), bgRoundInfo.getPlayersNumber(), bgRoundInfo.getWinnerName(),
                bgRoundInfo.getRoundId(), bgRoundInfo.getRoundStartDate(), bgRoundInfo.getPrivateRoomId());
    }

    private BGUpdatePrivateRoomRequest convert(IBGUpdatePrivateRoom bgUpdatePrivateRoom) {
        if (bgUpdatePrivateRoom == null) {
            return null;
        }
        return new BGUpdatePrivateRoomRequest(bgUpdatePrivateRoom.getPrivateRoomId(),
                convertIBGPlayer(bgUpdatePrivateRoom.getPlayers()),
                bgUpdatePrivateRoom.getBankId());
    }

    private List<BGPlayerDto> convertIBGPlayer(List<IBGPlayer> players) {
        List<BGPlayerDto> result = new ArrayList<>();
        if (players != null) {
            for (IBGPlayer player : players) {
                result.add(new BGPlayerDto(player.getNickname(), player.getAccountId(),
                        player.getExternalId(), BGStatusUtil.toBGStatus(player.getStatus())));
            }
        }
        return result;
    }

    private List<PlaceDto> convertIBgPlace(List<IBgPlace> places) {
        List<PlaceDto> result = new ArrayList<>();
        if (places != null) {
            for (IBgPlace place : places) {
                result.add(new PlaceDto(place.getAccountId(), place.getWin(), place.getRank(), place.getBetsSum(),
                        place.getWinSum(), place.getGameSessionId(), place.getGameScore() / 100, place.getEjectPoint()));
            }
        }
        return result;
    }

    private List<RMSRoomDto> convertRMSRooms(List<IRMSRoom> rooms) {
        List<RMSRoomDto> result = new ArrayList<>();
        if (rooms != null) {
            for (IRMSRoom room : rooms) {
                result.add(new RMSRoomDto(room.getRoomId(), room.getServerId(), room.isIsActive(),
                        room.isIsBattleground(), room.isIsPrivate(), room.getBuyInStake(),
                        room.getCurrency(), room.getGameId(), room.getGameName(),
                        convertRMSPlayer(room.getPlayers())));
            }
        }
        return result;

    }

    private List<RMSPlayerDto> convertRMSPlayer(List<IRMSPlayer> players) {
        List<RMSPlayerDto> result = new ArrayList<>();
        if (players != null) {
            for (IRMSPlayer player : players) {
                result.add(new RMSPlayerDto(player.getServerId(), player.getNickname(),
                        player.isIsOwner(), player.getSessionId(), player.getSeatNr()));
            }
        }
        return result;
    }

    private IBGUpdateRoomResult convert(BGUpdateRoomResultDto bgUpdateRoomResult) {
        if (bgUpdateRoomResult == null) {
            return null;
        }
        return new BGUpdateRoomResult(bgUpdateRoomResult.getCode(), bgUpdateRoomResult.getMessage(),
                bgUpdateRoomResult.getPrivateRoomId(),
                convertBGPlayerDto(bgUpdateRoomResult.getPlayers()));
    }

    private List<IBGPlayer> convertBGPlayerDto(List<BGPlayerDto> players) {
        List<IBGPlayer> result = new ArrayList<>();
        if (players != null) {
            for (BGPlayerDto player : players) {
                result.add(new BGPlayer(player.getNickname(), player.getAccountId(),
                        player.getExternalId(), BGStatusUtil.fromBGStatus(player.getStatus())));
            }
        }
        return result;
    }

    private BGFriendDto convert(Friend friend) {
        if (friend == null) {
            return null;
        }
        return new BGFriendDto(friend.getNickname(), friend.getExternalId(), BGFStatusUtil.toBGFStatus(friend.getStatus()));
    }

    private List<Friend> convertBGFriendDto(List<BGFriendDto> friends) {
        List<Friend> result = new ArrayList<>();
        if (friends != null) {
            for (BGFriendDto friend : friends) {
                result.add(new Friend(friend.getNickname(), friend.getExternalId(), BGFStatusUtil.fromBGFStatus(friend.getStatus())));
            }
        }
        return result;
    }

    public List<BGOnlinePlayerDto> convertFriendsToBGOnlinePlayersDto(Collection<Friend> friends) {

        LOG.debug("convertFriendsToTBGOnlinePlayers: friends:{}", friends);

        if (friends == null || friends.size() == 0) {
            LOG.error("convertFriendsToTBGOnlinePlayers: friends is empty");
            return null;
        }

        List<BGOnlinePlayerDto> tbgOnlinePlayers = new ArrayList<>();

        for (Friend friend : friends) {

            BGOnlinePlayerDto tbgOnlinePlayer = new BGOnlinePlayerDto(
                    friend.getNickname(),
                    friend.getExternalId(),
                    null
            );

            tbgOnlinePlayers.add(tbgOnlinePlayer);
        }

        LOG.debug("convertFriendsToTBGOnlinePlayers: tbgOnlinePlayers:{}", tbgOnlinePlayers);

        return tbgOnlinePlayers;
    }

    public List<OnlinePlayer> convertTBGOnlinePlayersTo(Collection<BGOnlinePlayerDto> tbgOnlinePlayers) {

        LOG.debug("convertTBGOnlinePlayersTo: tbgOnlinePlayers:{}", tbgOnlinePlayers);

        if (tbgOnlinePlayers == null || tbgOnlinePlayers.size() == 0) {
            LOG.error("convertTBGOnlinePlayersTo: tbgOnlinePlayers is empty");
            return null;
        }

        List<OnlinePlayer> onlinePlayers = new ArrayList<>();

        for (BGOnlinePlayerDto tbgOnlinePlayer : tbgOnlinePlayers) {

            OnlinePlayer onlinePlayer = new OnlinePlayer(
                    tbgOnlinePlayer.getNickname(),
                    tbgOnlinePlayer.getExternalId(),
                    BGOStatusUtil.fromBGOStatus(tbgOnlinePlayer.getStatus())
            );

            onlinePlayers.add(onlinePlayer);
        }

        LOG.debug("convertTBGOnlinePlayersTo: onlinePlayers:{}", onlinePlayers);

        return onlinePlayers;
    }

    @Override
    public IAddWinResult addWinSync(int serverId, String sessionId, long gameSessionId,
                                    Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                                    IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) throws CommonException {
        final long now = System.currentTimeMillis();
        LOG.debug("addWinSync: sessionId={}, gameSessionId={}, winAmount={}, returnedBet={}, roundId={}, roomId={}, " +
                        "playerBet={}, bgRoundInfo={}", sessionId, gameSessionId, winAmount.toCents(),
                returnedBet.toCents(), roundId, roomId, playerBet, bgRoundInfo);
        MutableObject<IAddWinResult> mutable = new MutableObject<>();
        try {
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), convert(bgRoundInfo),
                    playerBet.getStartRoundTime());
            AddWinRequestDto request =
                    new AddWinRequestDto(sessionId, gameSessionId, winAmount.toCents(),
                            returnedBet.toCents(), accountId, roundInfoResult, Collections.emptyMap(), roundId, roomId, false);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                AddWinResultDto result = convertToType(response, (r) -> new AddWinResultDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: addWinSync", System.currentTimeMillis() - now, sessionId);
                AddWinResult addWinResult = new AddWinResult(result.isPlayerOffline(), result.getBalance(),
                        result.isSuccess(), result.getStatusCode(), result.getReasonPhrases());
                LOG.debug("addWinSync: sessionId={}, addWinResult={}", sessionId, addWinResult);
                mutable.setValue(addWinResult);
                
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("addWinSync: onError, sessionId={}", sessionId, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    "SocketService: addWinSync [error]", System.currentTimeMillis() - now, sessionId);
            throw new CommonException(e);
        }
        return mutable.getValue();
    }

    @Override
    public IAddWinResult addWinWithSitOutSync(int serverId, String sessionId, long gameSessionId, Money winAmount, Money returnedBet,
                                              long roundId, long roomId, long accountId, IPlayerBet playerBet,
                                              IBattlegroundRoundInfo bgRoundInfo, boolean sitOut) throws CommonException {
        final long now = System.currentTimeMillis();
        LOG.debug("addWinWithSitOutSync: sessionId={}, gameSessionId={}, winAmount={}, returnedBet={}, roundId={}, roomId={}, " +
                        "playerBet={}, bgRoundInfo={}, sitOut={}", sessionId, gameSessionId, winAmount.toCents(),
                returnedBet.toCents(), roundId, roomId, playerBet, bgRoundInfo, sitOut);
        MutableObject<IAddWinResult> mutable = new MutableObject<>();
        try {
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), convert(bgRoundInfo),
                    playerBet.getStartRoundTime());
            AddWinWithSitOutRequest request =
                    new AddWinWithSitOutRequest(sessionId, gameSessionId, winAmount.toCents(),
                            returnedBet.toCents(), roundId, roomId, accountId, roundInfoResult, Collections.emptyMap(), sitOut);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                AddWinResultDto result = convertToType(response, (r) -> new AddWinResultDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: addWinWithSitOutSync", System.currentTimeMillis() - now, sessionId);
                AddWinResult addWinResult = new AddWinResult(result.isPlayerOffline(), result.getBalance(),
                        result.isSuccess(), result.getStatusCode(), result.getReasonPhrases());
                LOG.debug("addWinWithSitOutSync: sessionId={}, addWinResult={}", sessionId, addWinResult);
                mutable.setValue(addWinResult);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("addWinWithSitOutSync: onError, sessionId={}", sessionId, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    "SocketService: addWinWithSitOutSync [error]", System.currentTimeMillis() - now, sessionId);
            throw new CommonException(e);
        }
        return mutable.getValue();
    }

    /**
     * Process win operation and sitOut player
     * @param serverId serverId
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param winAmount win amount in cents
     * @param returnedBet not used (returned) bet in cents
     * @param roundId MQ side round identifier
     * @param roomId MQ side room identifier
     * @param accountId GS side account identifier
     * @param playerBet player bet
     * @param bgRoundInfo battleground info
     * @param gameId gameId
     * @param bankId bankId
     * @param sitOut true if required sitOut
     * @return {@code Mono<IAddWinResult>} add win results from gs side.
     */
    @Override
    public Mono<IAddWinResult> addWinWithSitOut(int serverId, String sessionId, long gameSessionId,
                                                Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                                                IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo, long gameId, long bankId,
                                                boolean sitOut) {
        final long now = System.currentTimeMillis();
        LOG.debug("addWinWithSitOut: sessionId={}, gameSessionId={}, winAmount={}, returnedBet={}, roundId={}, roomId={}, " +
                        "playerBet={}, bgRoundInfo={}, sitOut={}", sessionId, gameSessionId, winAmount.toCents(),
                returnedBet.toCents(), roundId, roomId, playerBet, bgRoundInfo, sitOut);
        return Mono.create(sink -> {
            try {
                RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                        playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), convert(bgRoundInfo),
                        playerBet.getStartRoundTime());
                AddWinWithSitOutRequest request =
                        new AddWinWithSitOutRequest(sessionId, gameSessionId, winAmount.toCents(),
                                returnedBet.toCents(), roundId, roomId, accountId, roundInfoResult, Collections.emptyMap(), sitOut);
                Mono<KafkaResponse> response =
                        kafkaMessageService.syncRequestToRandomGs(request);
                try {
                    AddWinResultDto result = convertToType(response, (r) -> new AddWinResultDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));
                    StatisticsManager.getInstance().updateRequestStatistics(
                            "SocketService: addWinWithSitOut", System.currentTimeMillis() - now, sessionId);
                    AddWinResult addWinResult = new AddWinResult(result.isPlayerOffline(), result.getBalance(),
                            result.isSuccess(), result.getStatusCode(), result.getReasonPhrases());
                    LOG.debug("addWinWithSitOut: sessionId={}, addWinResult={}", sessionId, addWinResult);
                    sink.success(addWinResult);
                } catch (Exception e) {
                    LOG.error("Error getting response: ", e);
                    throw new CommonException("Error getting response", e);
                }
            } catch (Exception e) {
                LOG.error("addWinWithSitOut: onError, sessionId={}", sessionId, e);
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: addWinWithSitOut [error]", System.currentTimeMillis() - now, sessionId);
                AddWinPendingOperation operation = new AddWinPendingOperation(accountId, sessionId, gameSessionId,
                        roomId, winAmount.toCents(), returnedBet.toCents(), roundId, playerBet, bgRoundInfo, gameId, bankId);
                pendingOperationService.create(operation);
                sink.error(getKExceptionIfPossible(e));
            }
        });
    }


    /**
     *  Process win and prepare for next round
     * @param serverId serverId
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param winAmount win amount in cents
     * @param returnedBet not used (returned) bet in cents
     * @param roundId MQ side round identifier
     * @param roomId MQ side room identifier
     * @param accountId GS side account identifier
     * @param playerBet player bet
     * @param bgRoundInfo battleground info
     * @return {@code Mono<IAddWinResult>} add win results from gs side.
     */
    @Override
    public Mono<IAddWinResult> addWin(int serverId, String sessionId, long gameSessionId,
                                      Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                                      IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
        final long now = System.currentTimeMillis();
        LOG.debug("addWin: sessionId={}, gameSessionId={}, winAmount={}, returnedBet={}, roundId={}, roomId={}, " +
                        "playerBet={}, bgRoundInfo={}", sessionId, gameSessionId, winAmount.toCents(),
                returnedBet.toCents(), roundId, roomId, playerBet, bgRoundInfo);
        RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), convert(bgRoundInfo),
                playerBet.getStartRoundTime());
        return Mono.create(sink -> {
            try {
                TestStandLocal testStandLocal = TestStandLocal.getInstance();
                boolean needTransportExceptionOnAddWin = testStandLocal.needTransportExceptionOnAddWin(sessionId,
                        TestStandError.TRANSPORT_ADD_WIN__EXCEPTION);
                if (needTransportExceptionOnAddWin) {
                    testStandLocal.removeTransportExceptionForSid(sessionId);
                    sink.error(new KafkaHandlerException(4, "TTransportException from teststand"));
                } else {
                    AddWinRequestDto request =
                            new AddWinRequestDto(sessionId, gameSessionId, winAmount.toCents(),
                                    returnedBet.toCents(), accountId, roundInfoResult, Collections.emptyMap(), roundId, roomId, false);
                    Mono<KafkaResponse> response =
                            kafkaMessageService.syncRequestToRandomGs(request);
                    try {
                        AddWinResultDto result = convertToType(response, (r) -> new AddWinResultDto(r.isSuccess(),
                                r.getStatusCode(), r.getReasonPhrases()));
                        StatisticsManager.getInstance().updateRequestStatistics(
                                "SocketService: addWin", System.currentTimeMillis() - now, sessionId);
                        AddWinResult addWinResult = new AddWinResult(result.isPlayerOffline(), result.getBalance(),
                                result.isSuccess(), result.getStatusCode(), result.getReasonPhrases());
                        LOG.debug("addWin: sessionId={}, addWinResult={}", sessionId, addWinResult);
                        sink.success(addWinResult);
                    } catch (Exception e) {
                        LOG.error("Error getting response: ", e);
                        throw new CommonException("Error getting response", e);
                    }
                }
            } catch (Exception e) {
                LOG.error("addWin: onError, sessionId={}", sessionId, e);
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: addWin [error]", System.currentTimeMillis() - now, sessionId);
                sink.error(getKExceptionIfPossible(e));
            }
        });
    }

    /**
     * Add batch win operations for processing
     * @param roomId MQ room identifier
     * @param roundId MQ round identifier
     * @param gameId game identifier
     * @param addWinRequests set of win requests
     * @param bankId bankId
     * @param timeoutInMillis max time for wait result. If timeout reached returned only finished results
     * @return {@code Map} with call results
     */
    @Override
    public Map<Long, IAddWinResult> addBatchWin(long roomId, long roundId, long gameId, Set<IAddWinRequest> addWinRequests, long bankId,
                                                long timeoutInMillis) {
        LOG.debug("addBatchWin: roomId={}, roundId={}, gameId={}, timeoutInMillis={}", roomId, roundId, gameId, timeoutInMillis);
        MutableObject<Map<Long, IAddWinResult>> result = new MutableObject<>();
        Set<AddWinRequestDto> tWinRequests = new HashSet<>(addWinRequests.size());
        for (IAddWinRequest winRequest : addWinRequests) {
            IPlayerBet playerBet = winRequest.getPlayerBet();
            IBattlegroundRoundInfo bgRoundInfo = winRequest.getBgRoundInfo();
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), convert(bgRoundInfo),
                    playerBet.getStartRoundTime());
            tWinRequests.add(new AddWinRequestDto(winRequest.getSessionId(), winRequest.getGameSessionId(), winRequest.getWinAmount(),
                    winRequest.getReturnedBet(), winRequest.getAccountId(), roundInfoResult, Collections.emptyMap(), winRequest.getGsRoundId(), -1,
                    winRequest.isSitOut()));
        }
        try {
            AddBatchWinRequestDto request = new AddBatchWinRequestDto(roomId, roundId, gameId, tWinRequests, timeoutInMillis);
            Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomGs(request);
            try {
                AddBatchWinResponseDto responseDto = convertToType(response, (r) -> new AddBatchWinResponseDto(r.isSuccess(), r.getStatusCode(), r.getReasonPhrases()));
                Map<Long,AddWinResultDto> callResult = CollectionUtils.isEmpty(responseDto.getWinResults()) ? new HashMap<Long, AddWinResultDto>() : responseDto.getWinResults();

                Map<Long, IAddWinResult> winResultMap = new HashMap<>(callResult.size());
                for (Map.Entry<Long, AddWinResultDto> entry : callResult.entrySet()) {
                    Long accountId = entry.getKey();
                    AddWinResultDto tWinResult = entry.getValue();
                    AddWinResult addWinResult = new AddWinResult(tWinResult.isPlayerOffline(), tWinResult.getBalance(),
                            tWinResult.isSuccess(), tWinResult.getStatusCode(), tWinResult.getReasonPhrases());
                    LOG.debug("addBatchWin: tWinResult={} for roomId={}, roundId={}, timeoutInMillis={}, accountId={}, addWinResult={}", tWinResult,
                            roomId, roundId, timeoutInMillis, accountId, addWinResult);
                    winResultMap.put(accountId, addWinResult);
                }
                Set<Long> failedWins = winResultMap.entrySet().stream()
                        .filter(entry -> !entry.getValue().isSuccess())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
                if (winResultMap.size() != addWinRequests.size() || !failedWins.isEmpty()) {
                    addPendingWinForTracking(addWinRequests, winResultMap, failedWins, roomId, gameId, bankId);
                }
                result.setValue(winResultMap);
                LOG.debug("addBatchWin end: roomId={}, roundId={}, gameId={}, timeoutInMillis={}", roomId, roundId, gameId, timeoutInMillis);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("addBatchWin: failed for roomId={}, roundId={}", roomId, roundId, e);
        }

        return result.getValue();
    }

    private void addPendingWinForTracking(Set<IAddWinRequest> addWinRequest, Map<Long, IAddWinResult> winResultMap, Set<Long> failedWins, long roomId,
                                          long gameId, long bankId) {
        for (IAddWinRequest request: addWinRequest) {
            long accountId = request.getAccountId();
            if(!winResultMap.containsKey(accountId) || failedWins.contains(accountId)) {
                String sessionId = request.getSessionId();
                long gameSessionId = request.getGameSessionId();
                IBattlegroundRoundInfo bgRoundInfo = request.getBgRoundInfo();
                AddWinPendingOperation operation = pendingOperationService.createWinPendingOperation(accountId, sessionId, gameSessionId, roomId, gameId, bankId, request.getWinAmount(), request.getReturnedBet(), request.getGsRoundId(), request.getPlayerBet(), bgRoundInfo);
                pendingOperationService.create(operation);
            }
        }
    }

    /**
     * Return status batch send win operation
     * @param roomId MQ room identifier
     * @param roundId MQ round identifier
     * @return {@code BatchOperationStatus} operation status
     */
    @Override
    public BatchOperationStatus getBatchAddWinStatus(long roomId, long roundId) {
        MutableObject<BatchOperationStatus> status = new MutableObject<>();

        GetBatchAddWinStatusRequest request = new GetBatchAddWinStatusRequest(roomId, roundId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            try {
                StringResponseDto responseDto = convertToType(response, (r) -> new StringResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                String result = responseDto.getValue();
                if (!StringUtils.isTrimmedEmpty(result)) {
                    status.setValue(BatchOperationStatus.valueOf(result));
                }
                LOG.debug("getBatchAddWinStatus: result={} for roomId={}, roundId={}", result, roomId, roundId);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("getBatchAddWinStatus: failed for roomId={}, roundId={}", roomId, roundId, e);
        }
        return status.getValue();
    }

    /**
     * Return status for bet/win operation
     * @param accountId account identifier
     * @param roomId MQ room identifier
     * @param roundId MQ round identifier
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param gameId game identifier
     * @param bankId bank identifier
     * @param isBet true for bet, false for win
     * @param betNumber MQ side bet serial number
     * @return {@code PaymentTransactionStatus} operation status
     */
    @Override
    public PaymentTransactionStatus getPaymentOperationStatus(long accountId, long roomId, long roundId, String sessionId,
                                                              long gameSessionId, long gameId, long bankId, Boolean isBet, int betNumber) {
        MutableObject<PaymentTransactionStatus> status = new MutableObject<>();

        try {
            GetPaymentOperationStatus2Request request =
                    new GetPaymentOperationStatus2Request(accountId, roomId, roundId, sessionId, gameSessionId, gameId, bankId, isBet, betNumber);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                StringResponseDto responseDto = convertToType(response, (r) -> new StringResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                String result = responseDto.getValue();
                if (!StringUtils.isTrimmedEmpty(result)) {
                    status.setValue(PaymentTransactionStatus.valueOf(result));
                }
                LOG.debug("getPaymentOperationStatus: result={} for roomId={}, roundId={}, accountId={}, sessionId={}", result,
                        roomId, roundId, accountId, sessionId);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("getPaymentOperationStatus: failed for roomId={}, roundId={}, accountId={}, sessionId={}", roomId, roundId, accountId,
                    sessionId, e);
        }
        return status.getValue();
    }

    /**
     * Process win and close game session
     * @param serverId serverId
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param winAmount win amount in cents
     * @param returnedBet not used bet in cents
     * @param roundId MQ side round identifier
     * @param roomId MQ side room identifier
     * @param accountId account identifier
     * @param playerBet playerBet
     * @param bgRoundInfo battleground info
     * @return {@code ISitOutResult} sitOut result
     * @throws Exception if any unexpected error occur
     */
    @Override
    public ISitOutResult sitOut(int serverId, String sessionId, long gameSessionId,
                                Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                                IPlayerBet playerBet,
                                IBattlegroundRoundInfo bgRoundInfo) throws Exception {
        final long now = System.currentTimeMillis();
        LOG.debug("sitOut request: sessionId={}, gameSessionId={}, accountId={}, roundId={}, roomId={}, winAmount={}, " +
                        "returnedBet={}, playerBet={}", sessionId, gameSessionId, accountId,
                roundId, roomId, winAmount.toCents(), returnedBet.toCents(), playerBet);
        MutableObject<SitOutResult> mutable = new MutableObject<>();
        RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), convert(bgRoundInfo),
                playerBet.getDateTime());
        SitOutRequest request =
                new SitOutRequest(sessionId, gameSessionId, winAmount.toCents(), returnedBet.toCents(),
                        roundId, roomId, accountId, roundInfoResult, Collections.emptyMap());
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            // TODO: remove contributions (emptyMap)
            SitOutResultDto tSitOutResult = convertToType(response, (r) -> new SitOutResultDto(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));

            SitOutResult sitOutResult = new SitOutResult(tSitOutResult.isSuccess(),
                    tSitOutResult.getStatusCode(), tSitOutResult.getReasonPhrases());
            LOG.debug("sitOut: success, result={}", sitOutResult);
            StatisticsManager.getInstance().updateRequestStatistics(
                    "SocketService: sitOut", System.currentTimeMillis() - now, sessionId);
            mutable.setValue(sitOutResult);
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }

        return mutable.getValue();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void sendMQDataSync(int serverId, ISeat seat, IActiveFrbSession frbSession, IPlayerProfile profile,
                               long gameId, Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons) throws Exception {
        IRoomPlayerInfo info = seat.getPlayerInfo();
        IPlayerStats stats = info.getStats();
        MQDataDto data = new MQDataDto(seat.getAccountId(),
                gameId,
                seat.getNickname(),
                seat.getTotalScore().getAmount(),
                stats.getRounds(),
                stats.getKills(),
                stats.getTreasures(),
                profile.getBorder(),
                profile.getHero(),
                profile.getBackground(),
                profile.getBorders(),
                profile.getHeroes(),
                profile.getBackgrounds(),
                profile.isDisableTooltips(),
                convertQuests(allQuests),
                weapons
        );
        LOG.debug("sendMQDataSync={}", data);

        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(data);
        try {
            convertToType(response, (r) -> new VoidKafkaResponse(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
    }

    private Set<MQQuestDataDto> convertQuests(Set<IQuest> source) {
        Set<MQQuestDataDto> quests = new HashSet<>();
        for (IQuest quest : source) {
            List<MQTreasureQuestProgressDto> treasures = new ArrayList<>();
            @SuppressWarnings("rawtypes")
            List treasuresList = quest.getProgress().getTreasures();
            for (Object trObject : treasuresList) {
                ITreasureProgress treasure = (ITreasureProgress) trObject;
                treasures.add(new MQTreasureQuestProgressDto(treasure.getTreasureId(), treasure.getCollect(),
                        treasure.getGoal()));
            }
            IQuestAmount amount = quest.getQuestPrize().getAmount();
            MQQuestAmountDto tmQuestAmount = new MQQuestAmountDto(amount.getFrom(), amount.getTo());
            MQQuestPrizeDto tmQuestPrize = new MQQuestPrizeDto(tmQuestAmount, quest.getQuestPrize().getSpecialWeaponId());
            quests.add(new MQQuestDataDto(quest.getId(), quest.getType(), quest.getRoomCoin(),
                    quest.isNeedReset(), quest.getCollectedAmount(), quest.getName(), tmQuestPrize,
                    treasures));
        }
        return quests;
    }

    @Override
    public Mono<Boolean> touchSession(String sessionId)  {
        SessionTouchRequest sessionTouchRequest = new SessionTouchRequest(sessionId);
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomGs(sessionTouchRequest);
        try {
            return response
                    .map(m -> { 
                        if (m instanceof BooleanResponseDto) {
                            return ((BooleanResponseDto) m).isBool();
                        }
                        throw new RuntimeException(m.getReasonPhrases());
                    });
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            return Mono.error(e);
        }
    }

    @Override
    public BuyInResult buyIn(int serverId, long accountId, String sessionId, Money amount, long gameSessionId,
                             long roomId, int betNumber, Long tournamentId, IBuyInPostProcessor buyInPostProcessor) throws BuyInFailedException {
        return buyIn(serverId, accountId, sessionId, amount, gameSessionId, roomId, betNumber, tournamentId, null, buyInPostProcessor);
    }

    public Mono<IBuyInResult> buyInParallel(int serverId, long accountId, String sessionId, Money amount, long gameSessionId,
                                            long roomId, int betNumber, Long tournamentId) {
        final long now = System.currentTimeMillis();
        LOG.debug("buyIn request: serverId={}, accountId={}, sessionId={}, amount={}, gameSessionId={}, roomId={}, " +
                        "betNumber={}", serverId, accountId, sessionId, amount.toCents(), gameSessionId,
                roomId, betNumber);
        MutableObject<BuyInResult> mutable = new MutableObject<>();
        return Mono.create(sink -> {
            try {
                if (amount.smallerOrEqualsTo(Money.ZERO) && tournamentId == null) {
                    sink.error(new BuyInFailedException("Buy in amount should be positive", true));
                    return;
                }
                    BuyInResultDto result;
                    try {
                        BuyInRequest request = new BuyInRequest(sessionId, amount.toCents(), gameSessionId, roomId, betNumber,
                                tournamentId == null ? -1 : tournamentId, 0, 0);
                        Mono<KafkaResponse> response =
                                kafkaMessageService.syncRequestToRandomGs(request);
                        result = convertToType(response, (r) -> new BuyInResultDto(r.isSuccess(),
                                    r.getStatusCode(), r.getReasonPhrases()));
                    } catch (Exception e) {
                        LOG.error("buyIn failed: serverId={}, sessionId={}, amount={}, gameSessionId={}, roomId={}, " +
                                        "betNumber={}", serverId, sessionId, amount.toCents(), gameSessionId,
                                roomId, betNumber, e);
                        throw e;
                    }
                    if (result != null) {
                        BuyInResult buyInResult = new BuyInResult(result.getAmount(), result.getBalance(),
                                result.getPlayerRoundId(), result.getGameSessionId(), result.isSuccess(),
                                result.getStatusCode() > 0, result.getReasonPhrases(), result.getStatusCode());
                        LOG.debug("buyIn: sessionId={}, buyInResult={}, serverId={}", sessionId, buyInResult, serverId);
                        StatisticsManager.getInstance().updateRequestStatistics(
                                "SocketService: buyIn", System.currentTimeMillis() - now, sessionId);
                        mutable.setValue(buyInResult);
                    }
                    sink.success(mutable.getValue());
            } catch (Exception e) {
                BuyInFailedException exception = getBuyInException(e);
                if (exception != null) {
                    sink.error(exception);
                } else {
                    LOG.error("buyIn failed: unexpected error", e);
                }
            }
            try {
                checkBuyIn(serverId, accountId, sessionId, amount, gameSessionId, roomId, betNumber, now, mutable, tournamentId, null);
            } catch (BuyInFailedException e) {
                sink.error(e);
            }
        });
    }

    /**
     * Make bet
     * @param serverId server id
     * @param accountId account identifier
     * @param sessionId GS side session identifier
     * @param amount bet amount in cents
     * @param gameSessionId  GS side game session identifier
     * @param roomId MQ side room identifier
     * @param betNumber bet number
     * @param tournamentId tournamentId
     * @param currentBalance current balance of player
     * @param buyInPostProcessor processor for post process of result
     * @return {@code BuyInResult} result of bet
     * @throws BuyInFailedException if any unexpected error occur
     */
    @Override
    public BuyInResult buyIn(int serverId, long accountId, String sessionId, Money amount, long gameSessionId,
                             long roomId, int betNumber, Long tournamentId, Long currentBalance, IBuyInPostProcessor buyInPostProcessor)
            throws BuyInFailedException {
        final long now = System.currentTimeMillis();
        LOG.debug("buyIn request: serverId={}, accountId={}, sessionId={}, amount={}, gameSessionId={}, roomId={}, " +
                        "betNumber={}", serverId, accountId, sessionId, amount.toCents(), gameSessionId,
                roomId, betNumber);
        if (amount.smallerOrEqualsTo(Money.ZERO) && tournamentId == null) {
            throw new BuyInFailedException("Buy in amount should be positive", true);
        }
        MutableObject<BuyInResult> mutable = new MutableObject<>();
        try {
            BuyInResultDto result;
            try {
                long roundId = 0;
                if (buyInPostProcessor instanceof AbstractGameRoom) {
                    IRoomInfo roomInfo = ((AbstractGameRoom<?, ?, ?, ?, ?, ?, ?, ?>) buyInPostProcessor).getRoomInfo();
                    if (roomInfo != null) {
                        roundId = roomInfo.getRoundId();
                    }
                }
                final long finalRoundId = roundId;

                BuyInRequest request =
                        new BuyInRequest(sessionId, amount.toCents(), gameSessionId, roomId, betNumber,
                                tournamentId == null ? -1 : tournamentId, currentBalance == null ? 0 : currentBalance, finalRoundId);
                Mono<KafkaResponse> response =
                        kafkaMessageService.syncRequestToRandomGs(request);
                try {
                    result = convertToType(response, (r) -> new BuyInResultDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));
                } catch (Exception e) {
                    LOG.error("Error getting response: ", e);
                    throw new KafkaHandlerException(-1, "Error getting response", e);
                }
            } catch (KafkaHandlerException e) {
                LOG.error("buyIn failed: serverId={}, sessionId={}, amount={}, gameSessionId={}, roomId={}, " +
                                "betNumber={}", serverId, sessionId, amount.toCents(), gameSessionId,
                        roomId, betNumber, e);
                throw e;
            }
            if (result != null) {
                BuyInResult buyInResult = new BuyInResult(result.getAmount(), result.getBalance(),
                        result.getPlayerRoundId(), result.getGameSessionId(), result.isSuccess(),
                        result.getStatusCode() > 0, result.getReasonPhrases(), result.getStatusCode());
                LOG.debug("buyIn: sessionId={}, buyInResult={}, serverId={}", sessionId, buyInResult, serverId);
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: buyIn", System.currentTimeMillis() - now, sessionId);
                mutable.setValue(buyInResult);
            }
        } catch (Exception e) {
            processBuyInException(e);
        }
        checkBuyIn(serverId, accountId, sessionId, amount, gameSessionId, roomId, betNumber, now, mutable, tournamentId, currentBalance);
        if (buyInPostProcessor != null) {
            buyInPostProcessor.buyInPostProcess(serverId, accountId, sessionId, amount, gameSessionId, roomId,
                    betNumber, tournamentId, 0L, mutable.getValue());
        }
        return mutable.getValue();
    }

    /**
     * Return buyIn if player for some reason did not participate in the round
     * @param serverId serverId
     * @param sessionId GS side session identifier
     * @param cents return bet in cents
     * @param accountId player account identifier
     * @param gameSessionId GS side game session identifier
     * @param roomId MQ side room identifier
     * @param betNumber MQ side bet serial number
     * @return true if success
     */
    @Override
    public boolean refundBuyIn(int serverId, String sessionId, long cents, long accountId, long gameSessionId, long roomId, int betNumber) {
        LOG.debug("refundBuyIn: sessionId={}, cents={}, accountId={}, gameSessionId={}, roomId={}, betNumber={}", sessionId, cents, accountId,
                gameSessionId, roomId, betNumber);
        MutableObject<VoidKafkaResponse> mutable = new MutableObject<>();
        try {
            RefundBuyInRequest request =
                    new RefundBuyInRequest(sessionId, cents, accountId, gameSessionId, roomId, betNumber);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                VoidKafkaResponse result = convertToType(response, (r) -> new VoidKafkaResponse(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                LOG.debug("refundBuyIn: sessionId={}, result={}, serverId={}", sessionId, result, serverId);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new KafkaHandlerException(-1, "Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("refundBuyIn failed", e);
        }
        return mutable.getValue() != null && mutable.getValue().isSuccess();
    }

    /**
     * updates Players Status In PrivateRoom
     * @param request Update Room Request
     * @return TBGUpdateRoomResult if success or null
     */
    @Override
    public IBGUpdateRoomResult updatePlayersStatusInPrivateRoom(int serverId, IBGUpdatePrivateRoom request) {
        LOG.debug("updatePlayersStatusInPrivateRoom: serverId:{}, TBGUpdateRoomRequest={}", serverId, request);
        MutableObject<BGUpdateRoomResultDto> mutable = new MutableObject<>();
        try {
            UpdatePlayersStatusInPrivateRoomRequest requestToKfk = new UpdatePlayersStatusInPrivateRoomRequest(convert(request));

            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(requestToKfk);
            try {
                BGUpdateRoomResultDto result = convertToType(response, (r) -> new BGUpdateRoomResultDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                LOG.debug("updatePlayersStatusInPrivateRoom: serverId:{}, TBGUpdateRoomResult={}", serverId, result);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("updatePlayersStatusInPrivateRoom: Exception {}", e.getMessage(), e);
        }

        if (mutable.getValue() != null) {
            return convert(mutable.getValue());
        }

        return null;
    }

    /**
     * invite Players To Private Room
     * @param players To invite to Private Room Request
     * @return boolean if success true or false
     */
    @Override
    public boolean invitePlayersToPrivateRoom(int serverId, List<IBGPlayer> players, String privateRoomId) {
        LOG.debug("invitePlayersToPrivateRoom: serverId:{}, List<IBGPlayer>:{}, privateRoomId:{}",
                serverId, players, privateRoomId);
        MutableObject<Boolean> mutable = new MutableObject<>();
        try {
            InvitePlayersToPrivateRoomRequest request =
                    new InvitePlayersToPrivateRoomRequest(convertIBGPlayer(players), privateRoomId);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                BooleanResponseDto responseDto = convertToType(response, (r) -> new BooleanResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                boolean result = responseDto.isBool();
                LOG.debug("invitePlayersToPrivateRoom: serverId:{}, result={}", serverId, result);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("invitePlayersToPrivateRoom: Exception {}", e.getMessage(), e);
        }

        if(mutable != null && mutable.getValue() != null) {
            return mutable.getValue();
        }

        return false;
    }

    /**
     * get Friends for specified friend
     * @param friend specified friend
     * @return List of friends if success or null
     */
    @Override
    public List<Friend> getFriends(int serverId, Friend friend) {
        LOG.debug("getFriends: serverId:{}, TBGFriend={}", serverId, friend);
        MutableObject<List<BGFriendDto>> mutable = new MutableObject<>();
        try {
            GetFriendsRequest request =
                    new GetFriendsRequest(convert(friend));
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                GetFriendsResponseDto responseDto = convertToType(response, (r) -> new GetFriendsResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                List<BGFriendDto> result = responseDto.getFriends();
                LOG.debug("getFriends: serverId:{}, result={}", serverId, result);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("getFriends: Exception {}", e.getMessage(), e);
        }

        if (mutable.getValue() != null) {
            return convertBGFriendDto(mutable.getValue());
        }
        return null;
    }

    /**
     * get Online Status for the specified players in the list
     * @param onlinePlayers specified players in the list
     * @return Updated list of players with Online Status if success or null
     */
    @Override
    public List<OnlinePlayer> getOnlineStatus(int serverId, Collection<Friend> onlinePlayers) {
        LOG.debug("getOnlineStatus: serverId:{}, List<TBGOnlinePlayer>={}", serverId, onlinePlayers);
        MutableObject<List<BGOnlinePlayerDto>> mutable = new MutableObject<>();
        try {
            GetOnlineStatusRequest request =
                    new GetOnlineStatusRequest(convertFriendsToBGOnlinePlayersDto(onlinePlayers));
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                GetOnlineStatusResponseDto responseDto = convertToType(response, (r) -> new GetOnlineStatusResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                List<BGOnlinePlayerDto> result = responseDto.getPlayers();
                LOG.debug("getOnlineStatus: serverId:{}, result={}", serverId, result);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("getOnlineStatus: Exception {}", e.getMessage(), e);
        }

        if (mutable.getValue() != null) {
            return convertTBGOnlinePlayersTo(mutable.getValue());
        }

        return null;
    }

    /**
     * push to Canex Online Rooms Players
     * @param rmsRooms Room with players data
     * @return boolean if success true or false
     */
    @Override
    public boolean pushOnlineRoomsPlayers(List<IRMSRoom> rmsRooms) {
        LOG.debug("pushOnlineRoomsPlayers: rmsRooms:{}", rmsRooms);

        MutableObject<Boolean> mutable = new MutableObject<>();

        try {
            List<RMSRoomDto> trmsRooms = convertRMSRooms(rmsRooms);
            PushOnlineRoomsPlayersRequest request =
                    new PushOnlineRoomsPlayersRequest(trmsRooms);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                BooleanResponseDto responseDto = convertToType(response, (r) -> new BooleanResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                boolean result = responseDto.isBool();
                LOG.debug("pushOnlineRoomsPlayers: result={}", result);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("pushOnlineRoomsPlayers: Exception {}", e.getMessage(), e);
        }

        if (mutable != null && mutable.getValue() != null) {
            return mutable.getValue();
        }

        return false;
    }

    /**
     * Finish Game Session And Make SitOut using MPGameSessionService
     * @param sid player's SID
     * @param privateRoomId private room Id if exists
     * @return true if success or false
     */
    @Override
    public boolean finishGameSessionAndMakeSitOut(int serverId, String sid, String privateRoomId) {
        LOG.debug("finishGameSessionAndMakeSitOut: serverId:{} sid:{}, privateRoomId:{}",
                serverId, sid, privateRoomId);

        MutableObject<Boolean> mutable = new MutableObject<>();

        try {
            FinishGameSessionAndMakeSitOutRequest request =
                    new FinishGameSessionAndMakeSitOutRequest(sid, privateRoomId);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                BooleanResponseDto responseDto = convertToType(response, (r) -> new BooleanResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                boolean result = responseDto.isBool();
                LOG.debug("finishGameSessionAndMakeSitOut: serverId:{}, result={}", serverId, result);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("finishGameSessionAndMakeSitOut: Exception {}", e.getMessage(), e);
        }

        if(mutable != null && mutable.getValue() != null) {
            return mutable.getValue();
        }

        return false;
    }

    /**
     * Just close gameSession without any payment operation
     * @param serverId serverId
     * @param sessionId GS session identifier
     * @param accountId player account identifier
     * @param gameSessionId GS side game session identifier
     * @param roomId roomId
     * @param gameId gameId
     * @param bankId bankId
     * @param buyIn room buyIn amount
     * @return true if success
     */
    @Override
    public boolean closeGameSession(int serverId, String sessionId, long accountId, long gameSessionId, long roomId, long gameId, long bankId, long buyIn) {
        LOG.debug("closeGameSession: sessionId={}, accountId={}, gameSessionId={}, roomId={}, buyIn={}", sessionId, accountId, gameSessionId, roomId, buyIn);
        MutableObject<Boolean> mutable = new MutableObject<>();
        try {
            CloseGameSessionRequest getDetailedPlayerInfo2Request =
                    new CloseGameSessionRequest(sessionId, accountId, gameSessionId, buyIn);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(getDetailedPlayerInfo2Request);
            try {
                BooleanResponseDto responseDto = convertToType(response, (r) -> new BooleanResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                boolean result = responseDto.isBool();
                LOG.debug("closeGameSession: sessionId={}, result={}, serverId={}", sessionId, result, serverId);
                mutable.setValue(result);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("closeGameSession failed", e);
            mutable.setValue(false);
        }
        if (!mutable.getValue()) {
            pendingOperationService.create(new SitOutPendingOperation(accountId, sessionId, gameSessionId, roomId, gameId, bankId));
        }
        return mutable.getValue();
    }

    /**
     * Check buyIn result, this method called only if buyIn failed by unknown reason
     * @param serverId serverId
     * @param accountId account identifier
     * @param sessionId GS side session identifier
     * @param amount bet amount in cents
     * @param gameSessionId GS side session identifier
     * @param roomId MQ side room identifier
     * @param betNumber MQ side bet number
     * @throws BuyInFailedException if any unexpected error occur
     */
    private void checkBuyIn(int serverId, long accountId, String sessionId, Money amount, long gameSessionId,
                            long roomId, int betNumber, long now, MutableObject<BuyInResult> mutable, Long tournamentId, Long currentBalance)
            throws BuyInFailedException {
        byte maxRetry = 10;
        while (mutable.getValue() == null && maxRetry-- > 0) {
            try {
                CheckBuyInRequest request =
                        new CheckBuyInRequest(sessionId, amount.toCents(), accountId,
                                gameSessionId, roomId, betNumber);
                Mono<KafkaResponse> response =
                        kafkaMessageService.syncRequestToRandomGs(request);
                try {
                    BuyInResultDto result = convertToType(response, (r) -> new BuyInResultDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));
                    if (result != null) {
                        BuyInResult buyInResult = new BuyInResult(result.getAmount(), result.getBalance(),
                                result.getPlayerRoundId(), result.getGameSessionId(), result.isSuccess(),
                                result.getStatusCode() > 0, result.getReasonPhrases(), result.getStatusCode());
                        LOG.debug("buyIn [checkBuyIn]: sessionId={}, buyInResult={}, serverId={}", sessionId,
                                buyInResult, serverId);
                        StatisticsManager.getInstance().updateRequestStatistics(
                                "SocketService: checkBuyIn", System.currentTimeMillis() - now, sessionId);
                        mutable.setValue(buyInResult);
                    }
                } catch (Exception e) {
                    LOG.error("Error getting response: ", e);
                    throw new CommonException("Error getting response", e);
                }
            } catch (Exception e) {
                LOG.error("checkBuyIn failed", e);
            }
            if (mutable.getValue() == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    //nop
                    Thread.currentThread().interrupt();
                }
            }
        }
        BuyInResult buyInResult = mutable.getValue();
        if (buyInResult == null) {
            pendingOperationService.create(new BuyInPendingOperation(accountId, sessionId, gameSessionId, roomId,
                    amount.toCents(), betNumber, tournamentId, currentBalance, 0, 0));
            throw new BuyInFailedException("Unknown error", true, 0);
        } else if (buyInResult.getAmount() <= 0 || !buyInResult.isSuccess()) {
            LOG.debug("BuyIn failed, BuyInResult={}", mutable.getValue());
            throw new BuyInFailedException(buyInResult.getErrorDescription(), buyInResult.isFatalError(),
                    buyInResult.getErrorCode());
        } else {
            //buyIn success, need remove from tracking
            pendingOperationService.remove(accountId);
        }
    }

    public void processBuyInException(Exception e) throws BuyInFailedException {
        if (e instanceof KafkaHandlerException) {
            //nop, already logged\
            BuyInFailedException exception = getBuyInException(e);
            if (exception != null) {
                throw exception;
            }
        } else {
            LOG.error("buyIn failed: unexpected error", e);
        }
    }

    private BuyInFailedException getBuyInException(Exception e) {
        if (e instanceof KafkaHandlerException) {
            int code = ((KafkaHandlerException) e).getCode();
            if (code == ErrorCodes.NOT_ENOUGH_MONEY || code == ErrorCodes.BUYIN_NOT_ALLOWED) {
                return new BuyInFailedException(e.getMessage(), true);
            }
            if (code > 0) {
                return new BuyInFailedException(e.getMessage(), e, true, code);
            }
        }
        return null;
    }

    /**
     * Prepare for start new round
     * @param serverId serverId
     * @param sessionId GS session identifier
     * @param accountId player account identifier
     * @param gameSessionId game session identifier
     * @param roomId MQ side room identifier
     * @param roomRoundId MQ side round identifier
     * @param roundStartDate round start date
     * @param battlegroundRoom true if room is battleground
     * @param stakeOrBuyInAmount battleground buyIn amount in cents
     * @return {@code StartNewRoundResult} operation result
     * @throws Exception if any unexpected error occur
     */
    @Override
    public StartNewRoundResult startNewRound(int serverId, long accountId, String sessionId, long gameSessionId,
                                             long roomId, long roomRoundId, long roundStartDate,
                                             boolean battlegroundRoom, long stakeOrBuyInAmount) throws Exception {
        final long now = System.currentTimeMillis();
        LOG.debug("startNewRound request: serverId={}, accountId={}, sessionId={}, gameSessionId={}, roomId={}, " +
                        "roomRoundId={}, roundStartDate={}, battlegroundRoom={}, stakeOrBuyInAmount={}",
                serverId, accountId, sessionId, gameSessionId, roomId, roomRoundId, roundStartDate, battlegroundRoom,
                stakeOrBuyInAmount);
        MutableObject<StartNewRoundResult> mutable = new MutableObject<>();
        StartNewRoundRequest request =
                new StartNewRoundRequest(sessionId, accountId, gameSessionId, roomId,
                        roomRoundId, roundStartDate, battlegroundRoom, stakeOrBuyInAmount);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            StartNewRoundResponseDto result = convertToType(response, (r) -> new StartNewRoundResponseDto(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));
            if (result != null) {
                StartNewRoundResult startResult = new StartNewRoundResult(result.getPlayerRoundId(),
                        result.getGameSessionId(), result.isSuccess(), result.getStatusCode() > 0, result.getReasonPhrases(), -1L);
                LOG.debug("startNewRound: sessionId={}, startResult={}, serverId={}", sessionId,
                        startResult, serverId);
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: startNewRound", System.currentTimeMillis() - now, sessionId);
                mutable.setValue(startResult);
            }
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return mutable.getValue();
    }

    /**
     * Batch prepare start new round for many players
     * @param seats list round players
     * @param roomId MQ side room identifier
     * @param roomRoundId MQ side round identifier
     * @param roundStartDate round start date
     * @param battlegroundRoom  true if battleground room
     * @param stakeOrBuyInAmount battleground buyIn amount in cents
     * @return {@code <T extends IStartNewRoundResult>} list IStartNewRoundResult
     * @param <T> template
     * @throws Exception  if any unexpected error occur
     */
    @Override
    public <T extends IStartNewRoundResult> List<T> startNewRoundForManyPlayers(List<ISeat> seats,
                                                                                long roomId, long roomRoundId, long roundStartDate,
                                                                                boolean battlegroundRoom, long stakeOrBuyInAmount) throws Exception {
        final long now = System.currentTimeMillis();
        List<RoundPlayerDto> roundPlayers = convertFromSeatsToTransport(seats);
        int serverId = extractFirstServerId(seats);
        LOG.debug("startNewRound request: roundPlayers={}, roomId={}, " +
                        "roomRoundId={}, roundStartDate={}, battlegroundRoom={}, stakeOrBuyInAmount={}",
                roundPlayers, roomId, roomRoundId, roundStartDate, battlegroundRoom,
                stakeOrBuyInAmount);
        MutableObject<List<IStartNewRoundResult>> mutable = new MutableObject<>();
        StartNewRoundForManyPlayersRequest getDetailedPlayerInfo2Request =
                new StartNewRoundForManyPlayersRequest(roundPlayers, roomId, roomRoundId, roundStartDate, battlegroundRoom, stakeOrBuyInAmount);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(getDetailedPlayerInfo2Request);
        try {
            StartNewRoundForManyPlayersResponseDto responseDto = convertToType(response, (r) -> new StartNewRoundForManyPlayersResponseDto(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));
            Map<Long, StartNewRoundResponseDto> resultMap = responseDto.getResults();
            if (resultMap != null) {
                List<Long> keysOfMap = new ArrayList<>(resultMap.keySet());
                List<IStartNewRoundResult> startResult = keysOfMap.stream()
                        .map(accountId -> convertFromTransport(accountId, resultMap.get(accountId)))
                        .collect(Collectors.toList());
                LOG.debug("startNewRoundForManyPlayers: startResult={}, serverId={}",
                        startResult, serverId);
                StatisticsManager.getInstance().updateRequestStatistics("ServiceHandler startNewRoundForManyPlayers",
                        System.currentTimeMillis() - now, "roomId: " + roomId + ", roomRoundId: " + roomRoundId + ", size: " + roundPlayers.size());
                mutable.setValue(startResult);
            }
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return (List<T>) mutable.getValue();
    }

    private int extractFirstServerId(List<ISeat> seats) {
        int serverId = 1;
        for (ISeat seat : seats) {
            ISocketClient client = seat.getSocketClient();
            IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            if (playerInfo == null) {
                LOG.error("sendStartNewRoundToAllPlayers: playerInfo is null for seat={}", seat);
                continue;
            }
            serverId = client != null ? client.getServerId() : IRoom.extractServerId(playerInfo.getSessionId());
            break;
        }
        return serverId;
    }

    private IStartNewRoundResult convertFromTransport(long accountId, StartNewRoundResponseDto result) {
        return new StartNewRoundResult(result.getPlayerRoundId(),
                result.getGameSessionId(), result.isSuccess(), result.getStatusCode() > 0, result.getReasonPhrases(), accountId);
    }

    private List<RoundPlayerDto> convertFromSeatsToTransport(List<ISeat> seats) {
        List<RoundPlayerDto> result = new ArrayList<>();
        for (ISeat seat : seats) {
            IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            if (playerInfo == null) {
                LOG.error("sendStartNewRoundToAllPlayers: playerInfo is null for seat={}", seat);
                continue;
            }
            result.add(new RoundPlayerDto(seat.getAccountId(), playerInfo.getSessionId(), playerInfo.getGameSessionId()));
        }
        return result;
    }

    /**
     * Logout player
     * @param serverId serverId
     * @param sessionId GS player session identifier
     * @return {@code Mono<Boolean> } true if logout success.
     */
    @Override
    public Mono<Boolean> leaveMultiPlayerLobby(int serverId, String sessionId) {
        final long now = System.currentTimeMillis();
        LOG.debug("Leave lobby request: serverId={}, sessionId={}", serverId, sessionId);
        return Mono.create(sink -> {
            try {
                LeaveMultiPlayerLobbyRequest request =
                        new LeaveMultiPlayerLobbyRequest(sessionId);
                Mono<KafkaResponse> response =
                        kafkaMessageService.syncRequestToRandomGs(request);
                try {
                    convertToType(response, (r) -> new VoidKafkaResponse(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));
                    StatisticsManager.getInstance().updateRequestStatistics(
                            "SocketService: leaveMultiPlayerLobby", System.currentTimeMillis() - now,
                            sessionId);
                    sink.success();
                } catch (Exception e) {
                    LOG.error("Error getting response: ", e);
                    throw new CommonException("Error getting response", e);
                }
            } catch (Exception e) {
                LOG.error("leaveMultiPlayerLobby sessionId={}", sessionId, e);
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: leaveMultiPlayerLobby [error]",
                        System.currentTimeMillis() - now, sessionId);
                sink.error(getKExceptionIfPossible(e));
            }
        });
    }

    /**
     * Return current player balance
     *
     * @param serverId serverId
     * @param sessionId GS player session identifier
     * @param mode game mode free|real|frb
     * @return current balance in cents
     */
    @Override
    public Mono<Long> getBalance(int serverId, String sessionId, String mode) {
        final long now = System.currentTimeMillis();
        LOG.debug("getBalance: serverId={}, sessionId={}, mode={}", serverId, sessionId, mode);
        return Mono.create(sink -> {
            try {
                GetBalanceRequest request =
                        new GetBalanceRequest(sessionId, mode);
                Mono<KafkaResponse> response =
                        kafkaMessageService.syncRequestToRandomGs(request);
                try {
                    LongResponseDto requestDto = convertToType(response, (r) -> new LongResponseDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));
                    Long balance = requestDto.getValue();
                    StatisticsManager.getInstance().updateRequestStatistics(
                            "SocketService: getBalance",
                            System.currentTimeMillis() - now, sessionId);
                    sink.success(balance);
                } catch (Exception e) {
                    LOG.error("Error getting response: ", e);
                    throw new CommonException("Error getting response", e);
                }
            } catch (Exception e) {
                LOG.error("getBalance={}", e.getMessage());
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: getBalance [error]",
                        System.currentTimeMillis() - now, sessionId);
                sink.error(getKExceptionIfPossible(e));
            }
        });
    }

    public long getBalanceSync(int serverId, String sessionId, String mode) throws Exception {
        final long now = System.currentTimeMillis();
        LOG.debug("getBalanceSync: serverId={}, sessionId={}, mode={}", serverId, sessionId, mode);
        MutableObject<Long> mutable = new MutableObject<>();
        GetBalanceRequest request =
                new GetBalanceRequest(sessionId, mode);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            LongResponseDto requestDto =
                    convertToType(response, (r) -> new LongResponseDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));
            Long balance = requestDto.getValue();
            StatisticsManager.getInstance().updateRequestStatistics(
                    "SocketService: getBalanceSync",
                    System.currentTimeMillis() - now, sessionId);
            mutable.setValue(balance);
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return mutable.getValue();
    }

    /**
     * Return current currency rate
     * @param unknownRate  {@code CurrencyRate} currency rate for load
     * @return {@code CurrencyRate} current currency rate
     * @throws Exception if any unexpected error occur
     */
    @Override
    public CurrencyRate getCurrencyRatesSync(CurrencyRate unknownRate) throws Exception {
        final long now = System.currentTimeMillis();

        if (FAKE_CURRENCIES.contains(unknownRate.getSourceCurrency())
                || FAKE_CURRENCIES.contains(unknownRate.getDestinationCurrency())) {
            CurrencyRate currencyFakeRate = new CurrencyRate(
                    unknownRate.getSourceCurrency(), unknownRate.getDestinationCurrency(), 1, System.currentTimeMillis());
            LOG.debug("getCurrencyRatesSync: found fake currency, return rate 1.0 unknownRate: {}, currencyFakeRate: {}",
                    unknownRate, currencyFakeRate);
            return currencyFakeRate;
        }

        LOG.debug("getCurrencyRatesSync: unknownRate: {}", unknownRate);
        Set<CurrencyRateDto> rates = new HashSet<>();
        rates.add(new CurrencyRateDto(unknownRate.getSourceCurrency(), unknownRate.getDestinationCurrency(),
                unknownRate.getRate(), unknownRate.getUpdateDate()));
        MutableObject<CurrencyRate> mutable = new MutableObject<>();
        UpdateCurrencyRatesRequestResponse request =
                new UpdateCurrencyRatesRequestResponse(rates);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            UpdateCurrencyRatesRequestResponse responseDto = convertToType(response, (r) -> new UpdateCurrencyRatesRequestResponse(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));
            Set<CurrencyRateDto> tCurrencyRates = responseDto.getRates();
            StatisticsManager.getInstance().updateRequestStatistics(
                    "SocketService: getCurrencyRatesSync", System.currentTimeMillis() - now);
            Set<CurrencyRate> result = new HashSet<>(tCurrencyRates.size());
            for (CurrencyRateDto tRate : tCurrencyRates) {
                result.add(new CurrencyRate(tRate.getSourceCurrency(), tRate.getDestinationCurrency(),
                        tRate.getRate(), tRate.getUpdateDate()));
            }
            CurrencyRate resultRate = result.iterator().next();
            LOG.debug("getCurrencyRatesSync resultRate={}", resultRate);
            mutable.setValue(resultRate);
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return mutable.getValue();
    }

    /**
     * Return external account identifiers
     *
     * @param accountIds account identifiers set
     * @return {@code Map<Long, String>} external account identifiers
     */
    public Map<Long, String> getExternalAccountIds(List<Long> accountIds) {
        try {
            MutableObject<Map<Long, String>> mutable = new MutableObject<>();
            
            GetExternalAccountIdsRequest request =
                    new GetExternalAccountIdsRequest(accountIds);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                GetExternalAccountIdsResponseDto responseDto 
                    = convertToType(response, (r) -> new GetExternalAccountIdsResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                mutable.setValue(responseDto.getExternalAccountIds());
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            } 
            return mutable.getValue();
        } catch (Exception e) {
            LOG.error("Failed to get external account ids", e);
            return new HashMap<>();
        }
    }

    @Override
    public Boolean savePlayerBetForFRB(int serverId, String sessionId, long gameSessionId, long roundId, long accountId,
                                       IPlayerBet playerBet) {
        final long now = System.currentTimeMillis();
        LOG.debug("savePlayerBetForFRB: sessionId={}, gameSessionId={}, roundId={}, playerBet={}", sessionId,
                gameSessionId, roundId, playerBet);
        MutableObject<Boolean> mutable = new MutableObject<>();
        try {
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), null,
                    playerBet.getDateTime());
            SavePlayerBetForFRBRequest request =
                    new SavePlayerBetForFRBRequest(sessionId, gameSessionId, roundId, accountId, roundInfoResult);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                convertToType(response, (r) -> new BooleanResponseDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                StatisticsManager.getInstance().updateRequestStatistics(
                        "SocketService: savePlayerBetForFRB", System.currentTimeMillis() - now, sessionId);
                LOG.debug("savePlayerBetForFRB success for accountId={}, playerBet={}", accountId, playerBet);
                mutable.setValue(true);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("savePlayerBetForFRB", e);
            mutable.setValue(false);
        }
        return mutable.getValue();
    }

    @SuppressWarnings("rawtypes")
    public IActiveCashBonusSession saveCashBonusRoundResult(long gameId, ISeat seat, IActiveCashBonusSession bonus,
                                                            IPlayerProfile profile, Set<IQuest> allQuests,
                                                            Map<Long, Map<Integer, Integer>> weapons, IPlayerBet playerBet,
                                                            long roundId)
            throws CommonException {
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        LOG.debug("saveCashBonusRoundResult: accountId={}, sessionId={}, gameSessionId={}, bonus={}, playerBet={}, " +
                        "roundId={}", seat.getAccountId(), playerInfo.getSessionId(), playerInfo.getGameSessionId(),
                bonus, playerBet, roundId);
        MutableObject<IActiveCashBonusSession> mutable = new MutableObject<>();
        try {
            MQDataDto mqData = prepareMQData(seat, gameId, playerInfo, profile, allQuests, weapons);
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), null, playerBet.getDateTime());

            SaveCashBonusRoundResultRequest request =
                    new SaveCashBonusRoundResultRequest(playerInfo.getId(), playerInfo.getSessionId(),
                            playerInfo.getGameSessionId(), bonus.getId(), bonus.getBalance(), bonus.getBetSum(), mqData,
                            roundInfoResult, roundId);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                CashBonusDto result = convertToType(response, (r) -> new CashBonusDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                LOG.debug("saveCashBonusRoundResult: result={}", result);
                String status = result.getStatus();
                //small fix for prevent excption on client side, RELEASING is unknown
                if ("RELEASING".equalsIgnoreCase(status)) {
                    status = "RELEASED";
                }
                ActiveCashBonusSession resultBonus = new ActiveCashBonusSession(result.getBonusId(),
                        bonus.getAccountId(), result.getAwardDate(), result.getExpirationDate(), result.getBalance(),
                        result.getAmount(), result.getBetSum(), result.getRolloverMultiplier(), status,
                        result.getMaxWinLimit());
                mutable.setValue(resultBonus);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("saveCashBonusRoundResult failed, bonusId={}", bonus.getId(), e);
            throw new com.dgphoenix.casino.common.exception.CommonException("saveCashBonusRoundResult call failed", e);
        }
        return mutable.getValue();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ITournamentSession saveTournamentRoundResult(long gameId, ISeat seat, ITournamentSession tournament,
                                                        IPlayerProfile profile, Set<IQuest> allQuests,
                                                        Map<Long, Map<Integer, Integer>> weapons, IPlayerBet playerBet,
                                                        long roundId) throws CommonException {
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        LOG.debug("saveTournamentResult: accountId={}, sessionId={}, gameSessionId={}, tournament={}",
                seat.getAccountId(), playerInfo.getSessionId(), playerInfo.getGameSessionId(), tournament);
        MutableObject<ITournamentSession> mutable = new MutableObject<>();
        try {
            MQDataDto data = prepareMQData(seat, gameId, playerInfo, profile, allQuests, weapons);
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), null,
                    playerBet.getDateTime());
            SaveTournamentRoundResultRequest request =
                    new SaveTournamentRoundResultRequest(playerInfo.getId(), playerInfo.getSessionId(),
                            playerInfo.getGameSessionId(), tournament.getTournamentId(), tournament.getBalance(), data,
                            roundInfoResult, roundId);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                TournamentInfoDto result = convertToType(response, (r) -> new TournamentInfoDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                LOG.debug("saveTournamentRoundResult: result={}", result);
                TournamentSession resultSession = new TournamentSession(seat.getAccountId(), result.getTournamentId(),
                        result.getName(), result.getState(), result.getStartDate(), result.getEndDate(), result.getBalance(), result.getBuyInPrice(),
                        result.getBuyInAmount(), result.isReBuyAllowed(), result.getReBuyPrice(), result.getReBuyAmount(),
                        result.getReBuyCount(), result.getReBuyLimit(), result.isResetBalanceAfterRebuy());
                mutable.setValue(resultSession);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("saveTournamentRoundResult failed, tournament={}", tournament, e);
            throw new CommonException("saveTournamentRoundResult call failed", e);
        }
        return mutable.getValue();
    }

    @SuppressWarnings("rawtypes")
    private MQDataDto prepareMQData(ISeat seat, long gameId, IRoomPlayerInfo playerInfo, IPlayerProfile profile,
                                  Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons) {
        return new MQDataDto(seat.getAccountId(),
                gameId,
                seat.getNickname(),
                seat.getTotalScore().getAmount(),
                playerInfo.getStats().getRounds(),
                playerInfo.getStats().getKills(),
                playerInfo.getStats().getTreasures(),
                profile.getBorder(),
                profile.getHero(),
                profile.getBackground(),
                profile.getBorders(),
                profile.getHeroes(),
                profile.getBackgrounds(),
                profile.isDisableTooltips(),
                convertQuests(allQuests),
                weapons);
    }

    private MQDataDto prepareMQData(long accountId, String nickName, long gameId, double experience, IPlayerStats playerStats,
                                  IPlayerProfile profile, Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons) {
        return new MQDataDto(accountId,
                gameId,
                nickName,
                experience,
                playerStats.getRounds(),
                playerStats.getKills(),
                playerStats.getTreasures(),
                profile.getBorder(),
                profile.getHero(),
                profile.getBackground(),
                profile.getBorders(),
                profile.getHeroes(),
                profile.getBackgrounds(),
                profile.isDisableTooltips(),
                convertQuests(allQuests),
                weapons);
    }

    /**
     *  Save current round and close gameSession for cache bonus game
     * @param accountId  player account identifier
     * @param nickName nickname
     * @param sessionId GS player session identifier
     * @param gameSessionId GS game session identifier
     * @param gameId gameId
     * @param experience experience
     * @param bonus {@code IActiveCashBonusSession} cash bonus session
     * @param playerStats  {@code IPlayerStats} player stats
     * @param profile {@code IPlayerProfile} player profile
     * @param allQuests  {@code Set<IQuest>} set quests
     * @param weapons {@code Map<Integer, Integer>>} map of weapons
     * @param playerBet {@code IPlayerBet} player bet data
     * @param roundId roundId
     * @return {@code ISitOutCashBonusSessionResult} result of sitout cash bonus session
     * @throws CommonException if any unexpected error occur
     */
    @SuppressWarnings("rawtypes")
    public ISitOutCashBonusSessionResult sitOutCashBonusSession(long accountId, String nickName, String sessionId, long gameSessionId, long gameId,
                                                                double experience, IActiveCashBonusSession bonus, IPlayerStats playerStats,
                                                                IPlayerProfile profile, Set<IQuest> allQuests,
                                                                Map<Long, Map<Integer, Integer>> weapons,
                                                                IPlayerBet playerBet, long roundId)
            throws CommonException {
        LOG.debug("sitOutCashBonusSession: accountId={}, sessionId={}, gameSessionId={}, bonus={}, playerBet={}, " +
                "roundId={}", accountId, sessionId, gameSessionId, bonus, playerBet, roundId);
        MutableObject<ISitOutCashBonusSessionResult> mutable = new MutableObject<>();
        try {
            MQDataDto mqData = prepareMQData(accountId, nickName, gameId, experience, playerStats, profile, allQuests, weapons);
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), null,
                    playerBet.getDateTime());
            SitOutCashBonusSessionRequest request =
                    new SitOutCashBonusSessionRequest(accountId,
                            sessionId, gameSessionId, bonus.getId(), bonus.getBalance(),
                            bonus.getBetSum(), mqData, roundInfoResult, roundId);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                SitOutCashBonusSessionResultDto result = convertToType(response, (r) -> new SitOutCashBonusSessionResultDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                LOG.debug("sitOutCashBonusSession: result={}", result);
                CashBonusDto resultCashBonus = result.getCashBonus();
                if (resultCashBonus != null) {
                    ActiveCashBonusSession resultBonus = new ActiveCashBonusSession(resultCashBonus.getBonusId(),
                            bonus.getAccountId(), resultCashBonus.getAwardDate(), resultCashBonus.getExpirationDate(),
                            resultCashBonus.getBalance(), resultCashBonus.getAmount(), resultCashBonus.getBetSum(),
                            resultCashBonus.getRolloverMultiplier(), resultCashBonus.getStatus(),
                            resultCashBonus.getMaxWinLimit());
                    mutable.setValue(new SitOutCashBonusSessionResult(result.isSuccess(), result.getStatusCode(),
                            result.getReasonPhrases(), resultBonus,
                            result.getActiveFRBonusId() < 0 ? null : result.getActiveFRBonusId()));
                }
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("sitOutCashBonusSession failed, bonusId={}", bonus.getId(), e);
            throw new com.dgphoenix.casino.common.exception.CommonException("saveCashBonusRoundResult call failed", e);
        }
        ISitOutCashBonusSessionResult value = mutable.getValue();
        LOG.debug("sitOutCashBonusSession result: {}", value);
        return value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ISitOutTournamentSessionResult sitOutTournamentSession(long accountId, String nickName, String sessionId, long gameSessionId, long gameId,
                                                                  double experience, ITournamentSession tournament, IPlayerStats playerStats,
                                                                  IPlayerProfile profile, Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons,
                                                                  IPlayerBet playerBet, long roundId)
            throws CommonException {
        //IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        LOG.debug("sitOutTournamentSession: accountId={}, sessionId={}, gameSessionId={}, tournament={}, " +
                "playerBet={}, roundId={}", accountId, sessionId, gameSessionId, tournament, playerBet, roundId);
        MutableObject<ISitOutTournamentSessionResult> mutable = new MutableObject<>();
        try {
            //TMQData mqData = prepareMQData(seat, gameId, playerInfo, profile, allQuests, weapons);
            MQDataDto mqData = prepareMQData(accountId, nickName, gameId, experience, playerStats, profile, allQuests, weapons);
            RoundInfoResultDto roundInfoResult = new RoundInfoResultDto(playerBet.getAccountId(), playerBet.getDateTime(),
                    playerBet.getBet() / 100, playerBet.getWin() / 100, playerBet.getData(), null,
                    playerBet.getDateTime());
            SitOutTournamentSessionRequest request =
                    new SitOutTournamentSessionRequest(playerBet.getAccountId(),
                            sessionId, gameSessionId, tournament.getTournamentId(),
                            tournament.getBalance(), mqData, roundInfoResult, roundId);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                SitOutTournamentSessionResultDto result = convertToType(response, (r) -> new SitOutTournamentSessionResultDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
                TournamentInfoDto tTournamentInfo = result.getTournamentSession();
                TournamentSession tournamentSession = new TournamentSession(accountId,
                        tTournamentInfo.getTournamentId(), tTournamentInfo.getName(), tTournamentInfo.getState(),
                        tTournamentInfo.getStartDate(), tTournamentInfo.getEndDate(), tTournamentInfo.getBalance(),
                        tTournamentInfo.getBuyInPrice(), tTournamentInfo.getBuyInAmount(), tTournamentInfo.isReBuyAllowed(),
                        tTournamentInfo.getReBuyPrice(), tTournamentInfo.getReBuyAmount(),
                        tTournamentInfo.getReBuyCount(), tTournamentInfo.getReBuyLimit(), tTournamentInfo.isResetBalanceAfterRebuy());
                SitOutTournamentSessionResult sitOutResult = new SitOutTournamentSessionResult(result.isSuccess(),
                        result.getStatusCode(), result.getReasonPhrases(), tournamentSession,
                        result.getActiveFRBonusId() < 0 ? null : result.getActiveFRBonusId());
                LOG.debug("sitOutTournamentSession result: result={}", result);
                mutable.setValue(sitOutResult);
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("sitOutCashBonusSession failed, tournamentId={}", tournament.getTournamentId(), e);
            throw new CommonException("sitOutTournamentSession call failed", e);
        }
        ISitOutTournamentSessionResult value = mutable.getValue();
        LOG.debug("sitOutTournamentSession result: {}", value);
        return value;
    }

    /**
     * Add reserved or censored nickname
     *
     * @param region region/country identifier
     * @param owner owner identifier
     * @param nicknames reserved nickname
     */
    @Override
    public void addMQReservedNicknames(String region, long owner, Set<String> nicknames) throws
            com.dgphoenix.casino.common.exception.CommonException {
        try {
            AddMQReservedNicknamesRequest request =
                    new AddMQReservedNicknamesRequest(region, owner, nicknames);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                convertToType(response, (r) -> new VoidKafkaResponse(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("Failed call addMQReservedNicknames", e);
            throw new com.dgphoenix.casino.common.exception.CommonException("call failed", e);
        }
    }

    /**
     * Remove reserved or censored nickname
     *
     * @param region region/country identifier
     * @param owner owner identifier
     * @param nicknames reserved nickname
     */
    @Override
    public void removeMQReservedNicknames(String region, long owner, Set<String> nicknames) throws
            com.dgphoenix.casino.common.exception.CommonException {
        try {
            RemoveMQReservedNicknamesRequest request =
                    new RemoveMQReservedNicknamesRequest(region, owner, nicknames);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                convertToType(response, (r) -> new VoidKafkaResponse(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases()));
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("Failed call removeMQReservedNicknames", e);
            throw new com.dgphoenix.casino.common.exception.CommonException("call failed", e);
        }
    }

    /**
     * Load saved mq data from gs side
     * @param accountId accountId
     * @param gameId gameId
     * @param serverId serverId
     * @return {@code MQData} data from gs side
     * @throws Exception
     */
    public MQData loadMQDataSync(long accountId, long gameId, int serverId) throws Exception {
        MutableObject<MQDataWrapperDto> mutable = new MutableObject<>();
        try {
            GetMQDataRequest request =
                    new GetMQDataRequest(accountId, gameId);
            Mono<KafkaResponse> response =
                    kafkaMessageService.syncRequestToRandomGs(request);
            try {
                mutable.setValue(convertToType(response, (r) -> new MQDataWrapperDto(r.isSuccess(),
                        r.getStatusCode(), r.getReasonPhrases())));
            } catch (Exception e) {
                LOG.error("Error getting response: ", e);
                throw new CommonException("Error getting response", e);
            }
        } catch (Exception e) {
            LOG.error("Failed to load MQ Data");
            throw e;
        }
        return convertMQData((mutable.getValue()).getData());
    }

    /**
     * Return Crash game settings, this method used for periodically refresh cache values
     *
     * @param bankIds bank identifiers
     * @param gameId game identifier
     * @return {@code Set<ICrashGameSetting>} crash game settings
     * @throws Exception if any unexpected error occur
     */
    @Override
    public Set<ICrashGameSetting> getCrashGameSetting(Set<Long> bankIds, int gameId) throws Exception {
        MutableObject<Set<CrashGameSettingDto>> mutable = new MutableObject<>();

        GetCrashGamesSettingsRequest request =
                new GetCrashGamesSettingsRequest(bankIds, gameId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            CrashGameSettingsResponseDto responseDto = convertToType(response, (r) -> new CrashGameSettingsResponseDto(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));
            mutable.setValue(responseDto.getSettings());
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        Set<CrashGameSettingDto> t = mutable.getValue();
        if (CollectionUtils.isEmpty(t)) {
            return Collections.emptySet();
        }
        Set<ICrashGameSetting> result = new HashSet<>(t.size());
        for (CrashGameSettingDto s : t) {
            CrashGameSetting crashGameSetting = getCrashGameSetting(s);
            LOG.debug("getCrashGameSetting: convert TCrashGameSetting={} to CrashGameSetting={}", s, crashGameSetting);
            result.add(crashGameSetting);
        }

        return result;
    }

    private static CrashGameSetting getCrashGameSetting(CrashGameSettingDto s) {

        double maxMultiplier = BigDecimal.valueOf(s.getMaxMultiplier() / 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        CrashGameSetting crashGameSetting = new CrashGameSetting(
                s.getBankId(),
                s.getCurrencyCode(),
                s.getMaxRoomPlayers(),
                maxMultiplier,
                s.getMaxPlayerProfitInRound(),
                s.getTotalPlayersProfitInRound(),
                s.getMinStake(),
                s.getMaxStake(),
                s.isSendRealBetWin()
        );

        return crashGameSetting;
    }

    @Override
    public void roomWasDeactivated(String privateRoomId, String reason, long bankId) throws Exception {
        LOG.debug("roomWasDeactivated: roomId: {}", privateRoomId);

        NotifyPrivateRoomWasDeactivatedRequest request =
                new NotifyPrivateRoomWasDeactivatedRequest(privateRoomId, reason, bankId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            convertToType(response, 
                    (r) -> new VoidKafkaResponse(r.isSuccess(), r.getStatusCode(), r.getReasonPhrases()));
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
    }

    /**
     * Close game session for FRB mode
     * @param serverId serverId
     * @param accountId player account identifier
     * @param sessionId GS player session identifier
     * @param gameSessionId GS game session identifier
     * @param gameId game identifier
     * @param bonusId bonus identifier
     * @param winSum win amount in cents
     * @return {@code FrbCloseResult} operation result
     * @throws Exception if any unexpected error occur
     */
    @Override
    public FrbCloseResult closeFRBonusAndSession(int serverId, long accountId, String sessionId,
                                                 long gameSessionId, long gameId, long bonusId, long winSum)
            throws Exception {
        final long now = System.currentTimeMillis();
        LOG.debug("closeFRBonusAndSession: accountId={}, sessionId={}, gameSessionId={}, gameId={}, bonusId={}, " +
                "winSum={}", accountId, sessionId, gameSessionId, gameId, bonusId, winSum);
        org.apache.commons.lang.mutable.MutableObject mutable = new org.apache.commons.lang.mutable.MutableObject();
        CloseFRBonusAndSessionRequest request =
                new CloseFRBonusAndSessionRequest(accountId, sessionId,
                        gameSessionId, gameId, bonusId, winSum);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToRandomGs(request);
        try {
            CloseFRBonusResultDto tCloseFRBonusResult = convertToType(response, (r) -> new CloseFRBonusResultDto(r.isSuccess(),
                    r.getStatusCode(), r.getReasonPhrases()));
            StatisticsManager.getInstance().updateRequestStatistics(
                    "SocketService: closeFRBonusAndSession", System.currentTimeMillis() - now, sessionId);
            FrbCloseResult result = new FrbCloseResult(
                    tCloseFRBonusResult.getNextFRBonusId() > 0,
                    tCloseFRBonusResult.getNextFRBonusId(),
                    tCloseFRBonusResult.getReasonPhrases(),
                    tCloseFRBonusResult.getStatusCode(),
                    tCloseFRBonusResult.getBalance(),
                    tCloseFRBonusResult.getRealWinSum()
            );
            LOG.debug("closeFRBonusAndSession success, result={}", result);
            mutable.setValue(result);
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return (FrbCloseResult) mutable.getValue();
    }

    private MQData convertMQData(MQDataDto source) {
        if (source == null) {
            return null;
        }
        MQData data = new MQData();
        data.setAccountId(source.getAccountId());
        data.setGameId(source.getGameId());
        data.setNickname(source.getNickname());
        data.setExperience(source.getExperience());
        data.setRounds(source.getRounds());
        data.setKills(source.getKills());
        data.setTreasures(source.getTreasures());
        data.setBorderStyle(source.getBorderStyle());
        data.setHero(source.getHero());
        data.setBackground(source.getBackground());
        data.setBorders(source.getBorders());
        data.setHeroes(source.getHeroes());
        data.setBackgrounds(source.getBackgrounds());
        data.setDisableTooltips(source.isDisableTooltips());
        Set<MQQuestData> quests = new HashSet<>();
        for (MQQuestDataDto questSource : source.getQuests()) {
            MQQuestData quest = new MQQuestData();
            quest.setId(questSource.getId());
            quest.setType(questSource.getType());
            List<MQTreasureQuestProgress> treasures = new ArrayList<>();
            for (MQTreasureQuestProgressDto progress : questSource.getTreasures()) {
                treasures.add(new MQTreasureQuestProgress(progress.getTreasureId(), progress.getCollect(),
                        progress.getGoal()));
            }
            quest.setTreasures(treasures);
            quest.setCollectedAmount(questSource.getCollectedAmount());
            quest.setName(questSource.getName());
            quest.setRoomCoin(questSource.getRoomCoin());
            quest.setNeedReset(questSource.isNeedReset());
            MQQuestPrizeDto questPrize = questSource.getQuestPrize();
            MQuestAmount mQuestAmount = new MQuestAmount(questPrize.getAmount().getFromAmount(),
                    questPrize.getAmount().getToAmount());
            quest.setQuestPrize(new MQuestPrize(mQuestAmount, questPrize.getSpecialWeaponId()));
            quests.add(quest);
        }
        data.setQuests(quests);
        data.setWeapons(source.getWeapons());
        return data;
    }

}
