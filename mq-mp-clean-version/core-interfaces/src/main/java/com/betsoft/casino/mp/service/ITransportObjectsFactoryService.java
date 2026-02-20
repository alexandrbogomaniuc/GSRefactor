package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.common.SeatBullet;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.quests.INewTreasure;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.ITreasureProgress;
import com.betsoft.casino.mp.model.quests.IUpdateQuest;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.*;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface ITransportObjectsFactoryService {
    ISitInResponse createSitInResponse(long date, int number, String nickname, long enterDate, long ammoAmount, long balance,
                                         IAvatar avatar, List<IWeapon> weapons, List<Double> weaponLootBoxPrices,
                                         boolean showRefreshBalanceButton, int level, boolean frbMode, long frbBalance, String mode, double rake);

    IWeapons createWeapons(long date, int rid, int ammoAmount, boolean freeShots, List<ITransportWeapon> weapons);

    ITransportWeapon createWeapon(int id, int shots);

    ITransportWeapon createWeapon(int id, int shots, int sourceId);

    IShotResponse createShotResponse(long date, int rid, int seatId, int weaponId, int remainingSWShots);

    IEnemyDestroyed createEnemyDestroyed(long date, int rid, long enemyId, int reason);

    ILevelUp createLevelUp(long date, int rid, int seatId, int level);

    IExperience createExperience(double amount);

    IChangeMap createChangeMap(long date, int mapId, String subround);

    IUpdateTrajectories createUpdateTrajectories(long date, int rid, Map<Long, Trajectory> trajectories,
                                                 int freezeTime, int animationId);

    IUpdateTrajectories createUpdateTrajectories(long date, int rid, Map<Long, Trajectory> trajectories);

    IAward createAward(int seatId, double score, float qualifyWin);

    IAwards createAwards(long date, long enemyId);

    IError createError(int code, String msg, long date, int rid);

    IEnemyProgress createEnemyProgress(int typeId, int skin, int kills, int goal);

    ITreasureProgress createTreasureProgress(int treasureId, int collect, int goal);

    ITransportEnemy createEnemy(long id, int width, int height, float speed, int prizes, double sumAward, int skins,
                                boolean boss);

    IRoundFinishSoon createRoundFinishSoon(long date);

    IRoomEnemy createRoomEnemy(long id,
                               long typeId,
                               boolean isBoss,
                               double speed,
                               String awardedPrizes,
                               double awardedSum,
                               double energy,
                               int skin,
                               Trajectory trajectory,
                               long parentEnemyId,
                               double fullEnergy,
                               List<IMember> members,
                               long swarmId,
                               long swarmType);

    IRoomEnemy createRoomEnemy(long id,
                               long typeId,
                               boolean isBoss,
                               double speed,
                               String awardedPrizes,
                               double awardedSum,
                               double energy,
                               int skin,
                               Trajectory trajectory,
                               long parentEnemyId,
                               double fullEnergy,
                               List<IMember> members,
                               long swarmId,
                               long swarmType,
                               long parentEnemyTypeId);

    ITransportSeat createSeat(int id, String nickname, long enterDate, double totalScore, double currentScore,
                              IAvatar avatar, int specialWeaponId, int level, int unplayedFreeShots, double totalDamage, long roundWin);

    ITransportSeat createSeat(int id, String nickname, long enterDate, double totalScore, double currentScore,
                              IAvatar avatar, int specialWeaponId, int level, int unplayedFreeShots, double totalDamage, int betLevel, long roundWin);

    ITransportObserver createObserver(String nickname, boolean isKicked, Status status, Boolean isOwner);

    IWeaponLootBox createWeaponLootBox(long date, int rid, int weaponId, int shots, long balance,
                                       float currentWin, int usedAmmoAmount);

    IGameStateChanged createGameStateChanged(long date, RoomState state, long ttnx, long roundId, Long roundStartTime);

    IPlayerStats createPlayerStats();

    ISitOutResponse createSitOutResponse(long date, int rid, int id, String nickname, long outDate,
                                         long compensateSpecialWeapons, long surplusHvBonus,
                                         long totalReturnedSpecialWeapons, long nextRoomId, boolean hasNextFrb);

    INewEnemies createNewEnemies(long date, List<IRoomEnemy> enemies);

    INewEnemy createNewEnemy(long date, IRoomEnemy newEnemy);

    IRoundResult createRoundResult(long date, int rid, double winAmount, double winRebuyAmount, long balance,
                                   long currentScore, long totalScore, int hitCount, int missCount, int nextMapId,
                                   List<ITransportSeat> seats, int enemiesKilledCount, long winAmountInCredits,
                                   long unusedBulletsCount, double unusedBulletsMoney, double totalBuyInMoney,
                                   long xpPrev, List<IWeaponSurplus> weaponSurplus, long totalKillsXP,
                                   int totalTreasuresCount, long totalTreasuresXP, ILevelInfo beforeRound,
                                   ILevelInfo afterRound, long surplusHvBonus,
                                   int questsCompletedCount, long questsPayouts, long roundId,
                                   List<IWeaponSurplus> weaponsReturned, int bulletsFired, double realWinAmount,
                                   int freeShotsWon, int moneyWheelCompleted, long moneyWheelPayouts,
                                   double totalDamage, List<IBattlegroundRoundResult> battlegroundRoundResult
    );

    IBattlegroundRoundResult createBattlegroundRoundResult(long id, long score, long rank, long pot, String nickName);

    TInboundObject createBetLevelResponse(long date, int rid, int betLevel, int seatId);

    ICancelBattlegroundRound createCancelBattlegroundRound(long refundedAmount, String reason);

    IKingOfHillChanged createKingOfHillChanged(long date, int rid, List<Integer> newKings);

    ILevelInfo createLevelInfo(int level, long score, long minScore, long maxScore);

    IBalanceUpdated createBalanceUpdated(long date, long balance, int serverAmmo);

    IFRBEnded createFRBEnded(long date, long winSum, String closeReason, boolean hasNextFrb, long realWinSum);

    ITransportObject getOkResponse(long date, int rid);

    IGetRoomInfoResponse createGetRoomInfoResponse(long date, long roomId, int rid, String name, short maxSeats,
                                                   double minBuyIn, double stake, double playerStake,
                                                   RoomState state, List<ITransportSeat> seats,
                                                   int ttnx, int width, int height, List<ITransportEnemy> enemies,
                                                   List<IRoomEnemy> roomEnemies, int alreadySitInNumber,
                                                   long alreadySitInAmmoCount, long alreadySitInBalance,
                                                   double alreadySitInWin, int mapId, String subround,
                                                   List<Integer> ammoValues, List<IMinePlace> mines,
                                                   Map<Long, Integer> freezeTime, boolean immortalBoss,
                                                   long roundId, Map<Integer, Integer> seatGems,
                                                   IActiveCashBonusSession activeCashBonusSession,
                                                   ITournamentSession tournamentSession, int betLevel,
                                                   Map<Long, Integer> enemiesModes, Set<SeatBullet> allBullets,
                                                   IRoomBattlegroundInfo battlegroundInfo, Map<Integer, List<Integer>> reels,
                                                   Map<Integer, Double> gemPrizes);

    IRoomBattlegroundInfo createRoomBattlegroundInfo(long buyIn, boolean buyInConfirmed,
                                                     int timeToStart,
                                                     List<Integer> kingsOfHill, long score, long rank, long pot, double potTaxPercent,
                                                     String joinUrl, List<Integer> confirmedSeatsId);

    IGetFullGameInfo createGetFullGameInfo(long date, int rid);

    IFullGameInfo createFullGameInfo(long date, int rid, int mapId, String subround, long startTime, RoomState state,
                                     List<IRoomEnemy> roomEnemies, List<ITransportSeat> seats, List<IMinePlace> mines,
                                     Map<Long, Integer> freezeTime, boolean immortalBoss, long roundId,
                                     Map<Integer, Integer> seatGems, int betLevel, Map<Long, Integer> enemiesModes,
                                     Set<SeatBullet> allBullets, long timeToStart, Map<Integer, List<Integer>> reels,
                                     int currentPowerUpMultiplier, Map<Integer, Double> gemPrizes);

    IWeaponSurplus createWeaponSurplus(int id, int shots, long winBonus);

    IShot createShot(long date, int rid, int weaponId, long enemyId, float x, float y, boolean isPaidSpecialShot);

    IMiss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                     int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                     int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable);

    IMiss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                     int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                     int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable, int betLevel,
                     Integer fragmentId, List<String> effects, String bulletId);

    IMiss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                     int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                     int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable, int betLevel,
                     String bulletId);


    IHit createHit(long date, int rid, int seatId, double damage, double win, int awardedWeaponId,
                   int usedSpecialWeapon, int remainingSWShots, double score, IRoomEnemy enemy, boolean lastResult,
                   double currentWin, long hvEnemyId, float x, float y, int awardedWeaponShots, boolean killed,
                   String mineId, int newFreeShots, int newFreeShotsSeatId, boolean instanceKill, int chMult,
                   long enemyId, long shotEnemyId);

    IHit createHit(long date, int rid, int seatId, double damage, double win, int awardedWeaponId,
                   int usedSpecialWeapon, int remainingSWShots, double score, IRoomEnemy enemy, boolean lastResult,
                   double currentWin, long hvEnemyId, float x, float y, int awardedWeaponShots, boolean killed,
                   String mineId, int newFreeShots, int newFreeShotsSeatId, boolean instanceKill, int chMult,
                   long enemyId, long shotEnemyId, String bulletId);

    IMinePlace createMinePlace(long date, int rid, int seatId, float x, float y, String mineId);

    IWinPrize createWinPrize(int id, String value);

    IUpdateQuest createUpdateQuest(long date, IQuest quest, long lastEnemyId);

    INewTreasure createNewTreasure(long date, int rid, long id, long enemyId, int completedQuestId, long questId);

    ISeatWinForQuest createSeatWinForQuest(long date, int rid, long seatId, long enemyId, long winAmount,
                                           int awardedWeaponId);

    Class getClassForRoundId();

    ITransportObject createBonusStatusChangedMessage(long bonusId, String oldStatus, String newStatus, String reason, String type);

    ITransportObject createTournamentStateChangedMessage(long tournamentId, String oldState, String newState, String reason);

    ISpin createSpin(List<Integer> reels, double win);

    IChangeEnemyMode createChangeEnemyModeMessage(long enemyId, EnemyMode enemyMode);

    IDamage createRageDamage(long enemyId, int damage);

    IEnemyMode createGameEnemyMode(long enemyId, int enemyMode);

    IUpdateWeaponPaidMultiplierResponse createUpdateWeaponPaidMultiplierResponse(long date, int rid,
                                                                                 Map<Integer, Integer> weaponPaidMultiplier);

    IBattlegroundScoreBoard createBattlegroundScoreBoard(long startTime, long endTime, List<IBattleScoreInfo> score, Map<Integer, Long> scoreBoss, int rid);

    IBattleScoreInfo createBattleScoreInfo(int seatId, long betAmount, long winAmount, boolean isKing);

    IBulletClearResponse createBulletClearResponse(long time, int rid, int seatId);

    ITransportAsteroid createAsteroid(int type, Double speed, Double spawnX, Double spawnY, Double slow);

    ICrashStateInfo createCrashStateInfo(long date, double currentMult, double timeSpeedMult);

    ICrashCancelBet createCrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seatWin, String crashBetId, String name);

    ICrashCancelBet createCrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seatWin, String crashBetId, String name, long balance);

    ICrashAllBetsRejected createCrashAllBetsRejectedResponse(long date, int rid, int seatId, String name);

    ICrashAllBetsRejected createCrashAllBetsRejectedDetailedResponse(long date, int rid, int seatId, String name, int errorCode, String errorMessage);

    ICrashAllBets createCrashAllBetsResponse(long date, int rid, int seatId, String name, long balance, long amount);

    ICrashGameInfo createCrashGameInfo(long date, int rid, long roomId, int mapId, long startTime, RoomState state, List<ITransportSeat> seats,
                                       long roundId, List<ICrashRoundInfo> multHistory, int nextMapId, long ttnx, String function, boolean isBattleGame, double kilometerMult, Double rakePercent);

    IRoomManagerChanged createRoomManagerChanged(long date, int rid, int newRoomManager);

    IBuyInConfirmedSeats createBuyInConfirmedSeats(long date, int rid, List<Integer> confirmedSeatsId);

    IAddWinRequest createAddWinRequest(String sessionId, long gameSessionId, long winAmount, long returnedBet, long accountId, IPlayerBet playerBet,
                                       IBattlegroundRoundInfo bgRoundInfo, long gsRoundId, boolean isSitOut);

    IKickResponse createKickResponse(long date, int rid);

    ICancelKickResponse createCancelKickResponse(long date, int rid);

    IPrivateRoomInviteResponse createPrivateRoomInviteResponse(long date, int rid, boolean successful);

    IFinishGameSessionResponse createFinishGameSessionResponse(long date, int rid, boolean successful);

    IRoomWasOpened createRoomWasOpened(long date, int rid, String nickname, boolean isKicked);

    IObserverRemoved createObserverRemoved(long date, int rid, String nickname);

    ILatencyResponse createLatencyResponse(long date, int rid, int step, long serverTs, long serverAckTs, long clientTs, long clientAckTs);

}
