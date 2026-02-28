package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.friends.Friend;
import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.abs.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.exception.CommonException;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISocketService {

    Mono<Boolean> touchSession(String sessionId);

    IAddWinResult addWinSync(int serverId, String sessionId, long gameSessionId,
                             Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                             IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) throws CommonException;

    Mono<IAddWinResult> addWin(int serverId, String sessionId, long gameSessionId, Money winAmount,
                               Money returnedBet, long roundId, long roomId, long accountId, IPlayerBet playerBet,
                               IBattlegroundRoundInfo bgRoundInfo);

    Mono<IAddWinResult> addWinWithSitOut(int serverId, String sessionId, long gameSessionId,
                                         Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                                         IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo, long gameId, long bankId, boolean sitOut);

    IAddWinResult addWinWithSitOutSync(int serverId, String sessionId, long gameSessionId,
                                       Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                                       IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo, boolean sitOut) throws CommonException;
    Map<Long, IAddWinResult> addBatchWin(long roomId, long roundId, long gameId, Set<IAddWinRequest> addWinRequest, long bankId,
                                         long timeoutInMillis);
    BatchOperationStatus getBatchAddWinStatus(long roomId, long roundId);
    PaymentTransactionStatus getPaymentOperationStatus(long accountId, long roomId, long roundId, String sessionId,
                                                       long gameSessionId, long gameId, long bankId, Boolean isBet, int betNumber);
    ISitOutResult sitOut(int serverId, String sessionId, long gameSessionId,
                         Money winAmount, Money returnedBet, long roundId, long roomId, long accountId,
                         IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) throws Exception;
    ISitInResult sitIn(String sessionId, long gameId, String mode, String lang, Long bonusId,
                       long oldGameSessionId, long oldRoundId, long roomId, int betNumber, Long tournamentId,
                       String nickname) throws CommonException;
    IBuyInResult buyIn(int serverId, long accountId, String sessionId, Money amount, long gameSessionId, long roomId,
                       int betNumber, Long tournamentId, IBuyInPostProcessor buyInPostProcessor) throws BuyInFailedException;

    IBuyInResult buyIn(int serverId, long accountId, String sessionId, Money amount, long gameSessionId, long roomId,
                       int betNumber, Long tournamentId, Long currentBalance, IBuyInPostProcessor buyInPostProcessor) throws BuyInFailedException;

    IBGUpdateRoomResult updatePlayersStatusInPrivateRoom(int serverId, IBGUpdatePrivateRoom request);

    boolean invitePlayersToPrivateRoom(int serverId, List<IBGPlayer> players, String privateRoomId);

    List<Friend> getFriends(int serverId, Friend friend);

    List<OnlinePlayer> getOnlineStatus(int serverId, Collection<Friend> onlinePlayers);

    boolean pushOnlineRoomsPlayers(List<IRMSRoom> trmsRooms);

    boolean finishGameSessionAndMakeSitOut(int serverId, String sid, String privateRoomId);

    boolean closeGameSession(int serverId, String sessionId, long accountId, long gameSessionId, long roomId, long gameId, long bankId, long buyIn);

    boolean refundBuyIn(int serverId, String sessionId, long cents, long accountId, long gameSessionId, long roomId, int betNumber);

    Mono<IBuyInResult> buyInParallel(int serverId, long accountId, String sessionId, Money amount, long gameSessionId, long roomId,
                       int betNumber, Long tournamentId);

    IStartNewRoundResult startNewRound(int serverId, long accountId, String sessionId, long gameSessionId,
                                       long roomId, long roomRoundId, long roundStartDate,
                                       boolean battlegroundRoom, long stakeOrBuyInAmount) throws Exception;

    <T extends IStartNewRoundResult> List<T> startNewRoundForManyPlayers(List<ISeat> seats,
                                       long roomId, long roomRoundId, long roundStartDate,
                                       boolean battlegroundRoom, long stakeOrBuyInAmount) throws Exception;

    Mono<Boolean> leaveMultiPlayerLobby(int serverId, String sessionId);

    IFrbCloseResult closeFRBonusAndSession(int serverId, long accountId, String sessionId,
                                           long gameSessionId, long gameId, long bonusId, long winSum) throws Exception;

    void sendMQDataSync(int serverId, ISeat seat, IActiveFrbSession frbSession, IPlayerProfile profile, long gameId,
                        Set<IQuest> quests, Map<Long, Map<Integer, Integer>> weapons) throws Exception;

    Mono<Long> getBalance(int serverId, String sessionId, String mode);

    long getBalanceSync(int serverId, String sessionId, String mode) throws Exception;

    CurrencyRate getCurrencyRatesSync(CurrencyRate unknownRate) throws Exception;

    Map<Long, String> getExternalAccountIds(List<Long> accountIds);

    Boolean savePlayerBetForFRB(int serverId, String sessionId, long gameSessionId, long roundId, long accountId,
                                      IPlayerBet playerBet);

    IActiveCashBonusSession saveCashBonusRoundResult(long gameId, ISeat seat, IActiveCashBonusSession bonus,
                                                     IPlayerProfile profile, Set<IQuest> allQuests,
                                                     Map<Long, Map<Integer, Integer>> weapons, IPlayerBet playerBet, long roundId)
            throws CommonException;

    ISitOutCashBonusSessionResult sitOutCashBonusSession(long accountId, String nickName, String sessionId, long gameSessionId, long gameId,
                                                                double experience, IActiveCashBonusSession bonus, IPlayerStats playerStats,
                                                                IPlayerProfile profile, Set<IQuest> allQuests,
                                                                Map<Long, Map<Integer, Integer>> weapons,
                                                                IPlayerBet playerBet, long roundId)
            throws CommonException;

    void addMQReservedNicknames(String region, long owner, Set<String> nicknames) throws CommonException;

    void removeMQReservedNicknames(String region, long owner, Set<String> nicknames) throws CommonException;

    ITournamentSession saveTournamentRoundResult(long gameId, ISeat seat, ITournamentSession tournament,
                                   IPlayerProfile profile, Set<IQuest> allQuests,
                                   Map<Long, Map<Integer, Integer>> weapons, IPlayerBet playerBet,
                                   long roundId) throws CommonException;

    ISitOutTournamentSessionResult sitOutTournamentSession(long accountId, String nickName, String sessionId, long gameSessionId,long gameId,
                                                           double experience, ITournamentSession tournament, IPlayerStats playerStats,
                                                           IPlayerProfile profile, Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons,
                                                           IPlayerBet playerBet, long roundId)
            throws CommonException;

    Set<ICrashGameSetting> getCrashGameSetting(Set<Long> bankIds, int gameId) throws Exception;

    void roomWasDeactivated(String privateRoomId, String reason, long bankId) throws Exception;
}
