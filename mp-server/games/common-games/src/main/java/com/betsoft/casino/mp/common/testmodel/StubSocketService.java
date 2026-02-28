package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.friends.Friend;
import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.service.ISocketService;
import com.abs.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.exception.CommonException;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongConsumer;

/**
 * User: flsh
 * Date: 20.02.2020.
 */
public abstract class StubSocketService implements ISocketService {
    @Override
    public Mono<Boolean> touchSession(String sessionId) {
        return null;
    }

    @Override
    public ISitOutResult sitOut(int serverId, String sessionId, long gameSessionId, Money winAmount,
                                Money returnedBet, long roundId, long roomId, long accountId,
                                IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo)
            throws Exception {
        return new ISitOutResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public int getErrorCode() {
                return 0;
            }

            @Override
            public String getErrorDetails() {
                return "null";
            }
        };
    }

    @Override
    public ISitInResult sitIn(String sessionId, long gameId, String mode,
                              String lang, Long bonusId, long oldGameSessionId, long oldRoundId,
                              long roomId, int betNumber, Long tournamentId, String nickname)
            throws CommonException {
        return null;
    }

    @Override
    public IBuyInResult buyIn(int serverId, long accountId, String sessionId, Money amount,
                              long gameSessionId, long roomId, int betNumber, Long tournamentId, Long currentBalance, IBuyInPostProcessor buyInPostProcessor) {
        return new IBuyInResult() {
            int playerRoundId = 1;

            @Override
            public long getAmount() {
                return amount.toCents();
            }

            @Override
            public void setAmount(long amount) {
            }

            @Override
            public long getBalance() {
                return amount.toCents() * 10;
            }

            @Override
            public void setBalance(long balance) {
            }

            @Override
            public long getPlayerRoundId() {
                return playerRoundId++;
            }

            @Override
            public void setPlayerRoundId(long playerRoundId) {
            }

            @Override
            public long getGameSessionId() {
                return gameSessionId;
            }

            @Override
            public void setGameSessionId(long gameSessionId) {
            }

            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public void setSuccess(boolean success) {
            }

            @Override
            public boolean isFatalError() {
                return false;
            }

            @Override
            public void setFatalError(boolean fatalError) {
            }

            @Override
            public String getErrorDescription() {
                return null;
            }

            @Override
            public void setErrorDescription(String errorDescription) {
            }

            @Override
            public int getErrorCode() {
                return 0;
            }

            @Override
            public void setErrorCode(int errorCode) {
            }
        };
    }

    @Override
    public Mono<IAddWinResult> addWinWithSitOut(int serverId, String sessionId, long gameSessionId, Money winAmount, Money returnedBet,
                                                long roundId, long roomId, long accountId, IPlayerBet playerBet,
                                                IBattlegroundRoundInfo bgRoundInfo, long gameId, long bankId, boolean sitOut) {
        return addWin(serverId, sessionId, gameSessionId, winAmount, returnedBet, roundId, roomId, accountId, playerBet, bgRoundInfo);
    }

    @Override
    public IAddWinResult addWinWithSitOutSync(int serverId, String sessionId, long gameSessionId, Money winAmount, Money returnedBet,
                                              long roundId, long roomId, long accountId, IPlayerBet playerBet,
                                              IBattlegroundRoundInfo bgRoundInfo, boolean sitOut) {
        return addWin(serverId, sessionId, gameSessionId, winAmount, returnedBet, roundId, roomId, accountId, playerBet, bgRoundInfo).block();
    }

    @Override
    public IBGUpdateRoomResult updatePlayersStatusInPrivateRoom(int serverId, IBGUpdatePrivateRoom request) {
        return null;
    }

    @Override
    public boolean invitePlayersToPrivateRoom(int serverId, List<IBGPlayer> players, String privateRoomId) {
        return false;
    }

    @Override
    public List<Friend> getFriends(int serverId, Friend friend) {
        return null;
    }

    @Override
    public List<OnlinePlayer> getOnlineStatus(int serverId, Collection<Friend> onlinePlayers) {
        return null;
    }

    @Override
    public boolean pushOnlineRoomsPlayers(List<IRMSRoom> trmsRooms) {
        return false;
    }

    @Override
    public boolean finishGameSessionAndMakeSitOut(int serverId, String sid, String privateRoomId) {
        return false;
    }

    @Override
    public boolean closeGameSession(int serverId, String sessionId, long accountId, long gameSessionId, long roomId, long gameId, long bankId, long buyIn) {
        return true;
    }

    @Override
    public boolean refundBuyIn(int serverId, String sessionId, long cents, long accountId, long gameSessionId, long roomId, int betNumber) {
        return true;
    }

    @Override
    public Mono<IBuyInResult> buyInParallel(int serverId, long accountId, String sessionId, Money amount,
                              long gameSessionId, long roomId, int betNumber, Long tournamentId) {
        return null;
    }

    @Override
    public IBuyInResult buyIn(int serverId, long accountId, String sessionId, Money amount, long gameSessionId,
                              long roomId, int betNumber, Long tournamentId, IBuyInPostProcessor buyInPostProcessor) throws BuyInFailedException {
        return buyIn(serverId, accountId, sessionId, amount, gameSessionId, roomId, betNumber, tournamentId, null, buyInPostProcessor);
    }

    @Override
    public IAddWinResult addWinSync(int serverId, String sessionId, long gameSessionId, Money winAmount, Money returnedBet,
                                    long roundId, long roomId, long accountId, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
        return addWin(serverId, sessionId, gameSessionId, winAmount, returnedBet, roundId, roomId, accountId, playerBet, bgRoundInfo).block();
    }

    @Override
    public Mono<IAddWinResult> addWin(int serverId, String sessionId, long gameSessionId, Money winAmount,
                                      Money returnedBet, long roundId, long roomId, long accountId, IPlayerBet playerBet,
                                      IBattlegroundRoundInfo bgRoundInfo) {
        return null;
    }

    @Override
    public Map<Long, IAddWinResult> addBatchWin(long roomId, long roundId, long gameId, Set<IAddWinRequest> addWinRequest, long bankId,
                                                long timeoutInMillis) {
        return Collections.emptyMap();
    }

    @Override
    public BatchOperationStatus getBatchAddWinStatus(long roomId, long roundId) {
        return BatchOperationStatus.FINISHED;
    }

    @Override
    public PaymentTransactionStatus getPaymentOperationStatus(long accountId, long roomId, long roundId, String sessionId, long gameSessionId,
                                                              long gameId, long bankId, Boolean isBet, int betNumber) {
        return PaymentTransactionStatus.APPROVED;
    }

    @Override
    public IStartNewRoundResult startNewRound(int serverId, long accountId, String sessionId, long gameSessionId,
                                              long roomId, long roomRoundId, long roundStartDate,
                                              boolean battlegroundRoom, long stakeOrBuyInAmount) {
        return null;
    }

    @Override
    public <T extends IStartNewRoundResult> List<T> startNewRoundForManyPlayers(List<ISeat> seats, long roomId, long roomRoundId, long roundStartDate, boolean battlegroundRoom, long stakeOrBuyInAmount) throws Exception {
        return null;
    }

    @Override
    public Mono<Boolean> leaveMultiPlayerLobby(int serverId, String sessionId) {
        return null;
    }

    @Override
    public IFrbCloseResult closeFRBonusAndSession(int serverId, long accountId, String sessionId,
                                                  long gameSessionId, long gameId, long bonusId,
                                                  long winSum) {
        return null;
    }

    @Override
    public void sendMQDataSync(int serverId, ISeat seat, IActiveFrbSession frbSession,
                               IPlayerProfile profile, long gameId, Set<IQuest> quests,
                               Map<Long, Map<Integer, Integer>> weapons) {
    }

    @Override
    public Mono<Long> getBalance(int serverId, String sessionId, String mode) {
        return null;
    }

    @Override
    public long getBalanceSync(int serverId, String sessionId, String mode) throws Exception {
        return 0;
    }

    @Override
    public CurrencyRate getCurrencyRatesSync(CurrencyRate unknownRate) {
        return null;
    }

    @Override
    public Map<Long, String> getExternalAccountIds(List<Long> accountIds) {
        return Collections.emptyMap();
    }

    @Override
    public Boolean savePlayerBetForFRB(int serverId, String sessionId, long gameSessionId, long roundId,
                                       long accountId, IPlayerBet playerBet) {
        return Boolean.FALSE;
    }

    @Override
    public IActiveCashBonusSession saveCashBonusRoundResult(long gameId, ISeat seat,
                                                            IActiveCashBonusSession bonus,
                                                            IPlayerProfile profile, Set<IQuest> allQuests,
                                                            Map<Long, Map<Integer, Integer>> weapons,
                                                            IPlayerBet playerBet, long roundid) {
        return null;
    }

    @Override
    public ISitOutCashBonusSessionResult sitOutCashBonusSession(long accountId, String nickName, String sessionId, long gameSessionId, long gameId,
                                                                double experience, IActiveCashBonusSession bonus, IPlayerStats playerStats,
                                                                IPlayerProfile profile, Set<IQuest> allQuests,
                                                                Map<Long, Map<Integer, Integer>> weapons,
                                                                IPlayerBet playerBet, long roundId) {
        return null;
    }

    @Override
    public void addMQReservedNicknames(String region, long owner, Set<String> nicknames) {
    }

    @Override
    public void removeMQReservedNicknames(String region, long owner, Set<String> nicknames) {
    }

    @Override
    public ITournamentSession saveTournamentRoundResult(long gameId, ISeat seat, ITournamentSession tournament, IPlayerProfile profile,
                                                        Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons, IPlayerBet playerBet,
                                                        long roundId) {
        return null;
    }

    @Override
    public ISitOutTournamentSessionResult sitOutTournamentSession(long accountId, String nickName, String sessionId, long gameSessionId, long gameId,
                                                                  double experience, ITournamentSession tournament, IPlayerStats playerStats,
                                                                  IPlayerProfile profile, Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons,
                                                                  IPlayerBet playerBet, long roundId) {
        return null;
    }


    @Override
    public void roomWasDeactivated(String privateRoomId, String reason, long bankId) throws Exception {

    }


    @Override
    public Set<ICrashGameSetting> getCrashGameSetting(Set<Long> bankIds, int gameId) {
        return Collections.emptySet();
    }

    public static FluxSink<WebSocketMessage> getConnection() {
        return new FluxSink<WebSocketMessage>() {
            @Override
            public void complete() {
            }

            @Override
            public Context currentContext() {
                return null;
            }

            @Override
            public void error(Throwable e) {
            }

            @Override
            public FluxSink<WebSocketMessage> next(WebSocketMessage webSocketMessage) {
                return null;
            }

            @Override
            public long requestedFromDownstream() {
                return 0;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public FluxSink<WebSocketMessage> onRequest(LongConsumer consumer) {
                return null;
            }

            @Override
            public FluxSink<WebSocketMessage> onCancel(Disposable d) {
                return null;
            }

            @Override
            public FluxSink<WebSocketMessage> onDispose(Disposable d) {
                return null;
            }
        };
    }
}
