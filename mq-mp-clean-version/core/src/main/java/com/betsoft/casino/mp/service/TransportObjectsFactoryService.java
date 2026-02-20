package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.common.SeatBullet;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.quests.EnemyProgress;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.TreasureProgress;
import com.betsoft.casino.mp.model.room.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
@Service
public class TransportObjectsFactoryService implements ITransportObjectsFactoryService {
    @Override
    public ISitInResponse createSitInResponse(long date, int number, String nickname, long enterDate, long ammoAmount, long balance,
                                              IAvatar avatar, List<IWeapon> weapons, List<Double> weaponLootBoxPrices,
                                              boolean showRefreshBalanceButton, int level, boolean frbMode, long frbBalance, String mode, double rake) {
        return new SitInResponse(date, number, nickname, enterDate, 0, 0, avatar, weapons, weaponLootBoxPrices,
                showRefreshBalanceButton, level, frbMode, frbBalance, mode, rake);
    }

    public Weapons createWeapons(long date, int rid, int ammoAmount, boolean freeShots, List<ITransportWeapon> weapons) {
        return new Weapons(date, rid, ammoAmount, freeShots, weapons);
    }

    @Override
    public Weapon createWeapon(int id, int shots) {
        return new Weapon(id, shots);
    }

    @Override
    public Weapon createWeapon(int id, int shots, int sourceId) {
        return new Weapon(id, shots, sourceId);
    }

    @Override
    public ShotResponse createShotResponse(long date, int rid, int seatId, int weaponId, int remainingSWShots) {
        return new ShotResponse(date, rid, seatId, weaponId, remainingSWShots);
    }

    @Override
    public EnemyDestroyed createEnemyDestroyed(long date, int rid, long enemyId, int reason) {
        return new EnemyDestroyed(date, rid, enemyId, reason);
    }

    @Override
    public LevelUp createLevelUp(long date, int rid, int seatId, int level) {
        return new LevelUp(date, rid, seatId, level);
    }

    @Override
    public Experience createExperience(double amount) {
        return new Experience(amount);
    }

    public ChangeMap createChangeMap(long date, int mapId, String subround) {
        return new ChangeMap(date, mapId, subround);
    }

    @Override
    public UpdateTrajectories createUpdateTrajectories(long date, int rid, Map<Long, Trajectory> trajectories,
                                                       int freezeTime, int animationId) {
        return new UpdateTrajectories(date, rid, trajectories, freezeTime, animationId);
    }

    @Override
    public UpdateTrajectories createUpdateTrajectories(long date, int rid, Map<Long, Trajectory> trajectories) {
        return new UpdateTrajectories(date, rid, trajectories, 0, EnemyAnimation.NO_ANIMATION.getAnimationId());
    }

    @Override
    public Award createAward(int seatId, double score, float qualifyWin) {
        return new Award(seatId, score, qualifyWin);
    }

    @Override
    public Awards createAwards(long date, long enemyId) {
        return new Awards(date, enemyId);
    }

    @Override
    public Error createError(int code, String msg, long date, int rid) {
        return new Error(code, msg, date, rid);
    }

    @Override
    public EnemyProgress createEnemyProgress(int typeId, int skin, int kills, int goal) {
        return new EnemyProgress(typeId, skin, kills, goal);
    }

    @Override
    public TreasureProgress createTreasureProgress(int treasureId, int collect, int goal) {
        return new TreasureProgress(treasureId, collect, goal);
    }

    @Override
    public Enemy createEnemy(long id, int width, int height, float speed, int prizes, double sumAward, int skins,
                             boolean boss) {
        return new Enemy(id, width, height, speed, prizes, sumAward, skins, boss);
    }

    @Override
    public RoundFinishSoon createRoundFinishSoon(long date) {
        return new RoundFinishSoon(date);
    }

    @Override
    public RoomEnemy createRoomEnemy(long id,
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
                                     long swarmType) {
        return new RoomEnemy(id, typeId, isBoss, speed, awardedPrizes,  awardedSum, energy, skin, trajectory, parentEnemyId, fullEnergy, members, swarmId, swarmType, -1);
    }

    public RoomEnemy createRoomEnemy(long id,
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
                                     long parentEnemyTypeId) {
        return new RoomEnemy(id, typeId, isBoss, speed, awardedPrizes, awardedSum, energy, skin, trajectory, parentEnemyId, fullEnergy, members, swarmId, swarmType, parentEnemyTypeId);
    }

    @Override
    public Seat createSeat(int id, String nickname, long enterDate, double totalScore, double currentScore,
                           IAvatar avatar, int specialWeaponId, int level, int unplayedFreeShots, double totalDamage, long roundWin) {
        return new Seat(id, nickname, enterDate, totalScore, currentScore,
                avatar, specialWeaponId, level, unplayedFreeShots, totalDamage, roundWin);
    }

    @Override
    public Observer createObserver(String nickname, boolean isKicked, Status status, Boolean isOwner) {
        return new Observer(nickname, isKicked, status, isOwner);
    }

    @Override
    public Seat createSeat(int id, String nickname, long enterDate, double totalScore, double currentScore,
                           IAvatar avatar, int specialWeaponId, int level, int unplayedFreeShots, double totalDamage, int betLevel, long roundWin) {
        Seat seat = new Seat(id, nickname, enterDate, totalScore, currentScore,
                avatar, specialWeaponId, level, unplayedFreeShots, totalDamage, roundWin);
        seat.setBetLevel(betLevel);
        return seat;
    }

    @Override
    public WeaponLootBox createWeaponLootBox(long date, int rid, int weaponId, int shots, long balance,
                                             float currentWin, int usedAmmoAmount) {
        return new WeaponLootBox(date, rid, weaponId, shots, balance, currentWin, usedAmmoAmount);
    }

    @Override
    public GameStateChanged createGameStateChanged(long date, RoomState state, long ttnx, long roundId, Long roundStartTime) {
        return new GameStateChanged(date, state, ttnx, roundId, roundStartTime);
    }

    @Override
    public PlayerStats createPlayerStats() {
        return new PlayerStats();
    }

    @Override
    public SitOutResponse createSitOutResponse(long date, int rid, int id, String nickname, long outDate,
                                               long compensateSpecialWeapons, long surplusHvBonus,
                                               long totalReturnedSpecialWeapons, long nextRoomId, boolean hasNextFrb) {
        return new SitOutResponse(date, rid, id, nickname, outDate, compensateSpecialWeapons, surplusHvBonus,
                totalReturnedSpecialWeapons, nextRoomId, hasNextFrb);
    }

    @Override
    public NewEnemies createNewEnemies(long date, List<IRoomEnemy> enemies) {
        return new NewEnemies(date, enemies);
    }

    @Override
    public NewEnemy createNewEnemy(long date, IRoomEnemy newEnemy) {
        return new NewEnemy(date, newEnemy);
    }

    @Override
    public RoundResult createRoundResult(long date, int rid, double winAmount, double winRebuyAmount, long balance,
                                         long currentScore, long totalScore, int hitCount, int missCount, int nextMapId,
                                         List<ITransportSeat> seats, int enemiesKilledCount, long winAmountInCredits,
                                         long unusedBulletsCount, double unusedBulletsMoney, double totalBuyInMoney,
                                         long xpPrev, List<IWeaponSurplus> weaponSurplus, long totalKillsXP,
                                         int totalTreasuresCount, long totalTreasuresXP, ILevelInfo beforeRound,
                                         ILevelInfo afterRound, long surplusHvBonus,
                                         int questsCompletedCount, long questsPayouts, long roundId,
                                         List<IWeaponSurplus> weaponsReturned, int bulletsFired, double realWinAmount,
                                         int freeShotsWon, int moneyWheelCompleted, long moneyWheelPayouts,
                                         double totalDamage, List<IBattlegroundRoundResult> battlegroundRoundResult) {
        return new RoundResult(date, rid, winAmount, winRebuyAmount, balance, currentScore, totalScore, hitCount,
                missCount, nextMapId, seats, enemiesKilledCount, winAmountInCredits, unusedBulletsCount,
                unusedBulletsMoney, totalBuyInMoney, xpPrev, weaponSurplus, totalKillsXP, totalTreasuresCount,
                totalTreasuresXP, beforeRound, afterRound, surplusHvBonus, questsCompletedCount,
                questsPayouts, roundId, weaponsReturned, bulletsFired, realWinAmount, freeShotsWon, moneyWheelCompleted,
                moneyWheelPayouts, totalDamage, battlegroundRoundResult);
    }

    @Override
    public LevelInfo createLevelInfo(int level, long score, long minScore, long maxScore) {
        return new LevelInfo(level, score, minScore, maxScore);
    }

    @Override
    public IBattlegroundRoundResult createBattlegroundRoundResult(long id, long score, long rank, long pot, String nickName) {
        return new BattlegroundRoundResult(id, score, rank, pot, nickName);
    }

    @Override
    public TInboundObject createBetLevelResponse(long date, int rid, int betLevel, int seatId) {
        return new BetLevelResponse(date, rid, betLevel, seatId);
    }

    @Override
    public ICancelBattlegroundRound createCancelBattlegroundRound(long refundedAmount, String reason) {
        return new CancelBattlegroundRound(System.currentTimeMillis(), -1, refundedAmount, reason);
    }

    @Override
    public IKingOfHillChanged createKingOfHillChanged(long date, int rid, List<Integer> newKings) {
        return new KingOfHillChanged(date, rid, newKings);
    }

    @Override
    public BalanceUpdated createBalanceUpdated(long date, long balance, int serverAmmo) {
        return new BalanceUpdated(date, balance, serverAmmo);
    }

    @Override
    public IFRBEnded createFRBEnded(long date, long winSum, String closeReason, boolean hasNextFrb, long realWinSum) {
        return new FRBEnded(date, winSum, closeReason, hasNextFrb, realWinSum);
    }

    @Override
    public ITransportObject getOkResponse(long date, int rid) {
        return new Ok(date, rid);
    }

    @Override
    public GetRoomInfoResponse createGetRoomInfoResponse(long date, long roomId, int rid, String name, short maxSeats,
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
                                                         IRoomBattlegroundInfo battlegroundInfo,
                                                         Map<Integer, List<Integer>> reels, Map<Integer, Double> gemPrizes) {
        return new GetRoomInfoResponse(date, roomId, rid, name, maxSeats, minBuyIn, stake, playerStake, state, seats,
                ttnx, width, height, enemies, roomEnemies, alreadySitInNumber, alreadySitInAmmoCount,
                alreadySitInBalance, alreadySitInWin, mapId, subround, ammoValues, mines, freezeTime, immortalBoss,
                roundId, seatGems, activeCashBonusSession, tournamentSession, betLevel, enemiesModes, allBullets, battlegroundInfo,
                reels, gemPrizes);
    }

    @Override
    public IRoomBattlegroundInfo createRoomBattlegroundInfo(long buyIn, boolean buyInConfirmed, int timeToStart,
                                                            List<Integer> kingsOfHill, long score, long rank, long pot, double potTaxPercent,
                                                            String joinUrl, List<Integer> confirmedSeatsId) {
        return new RoomBattlegroundInfo(buyIn, buyInConfirmed, timeToStart, kingsOfHill, score, rank, pot, potTaxPercent, joinUrl, confirmedSeatsId);
    }

    @Override
    public GetFullGameInfo createGetFullGameInfo(long date, int rid) {
        return new GetFullGameInfo(date, rid);
    }

    public FullGameInfo createFullGameInfo(long date, int rid, int mapId, String subround, long startTime, RoomState state,
                                           List<IRoomEnemy> roomEnemies, List<ITransportSeat> seats, List<IMinePlace> mines,
                                           Map<Long, Integer> freezeTime, boolean immortalBoss, long roundId,
                                           Map<Integer, Integer> seatGems, int betLevel, Map<Long, Integer> enemiesModes,
                                           Set<SeatBullet> allBullets, long timeToStart, Map<Integer, List<Integer>> reels,
                                           int currentPowerUpMultiplier, Map<Integer, Double> gemPrizes) {
        return new FullGameInfo(date, rid, mapId, subround, startTime, state, roomEnemies, seats, mines, freezeTime,
                immortalBoss, roundId, seatGems, betLevel, enemiesModes, allBullets, timeToStart, reels, currentPowerUpMultiplier, gemPrizes);
    }


    @Override
    public WeaponSurplus createWeaponSurplus(int id, int shots, long winBonus) {
        return new WeaponSurplus(id, shots, winBonus);
    }

    @Override
    public IShot createShot(long date, int rid, int weaponId, long enemyId, float x, float y, boolean isPaidSpecialShot) {
        return new Shot(date, rid, weaponId, enemyId, x, y, isPaidSpecialShot, "");
    }

    @Override
    public Miss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                           int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                           int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable) {
        return new Miss(date, rid, seatId, killedMiss, awardedWeaponId, enemyId, usedSpecialWeapon, remainingSWShots,
                score, lastResult, x, y, awardedWeaponShots, mineId, shotEnemyId, invulnerable);
    }

    @Override
    public Miss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                           int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                           int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable, int betLevel,
                           Integer fragmentId, List<String> effects, String bulletId) {
        Miss miss = new Miss(date, rid, seatId, killedMiss, awardedWeaponId, enemyId, usedSpecialWeapon, remainingSWShots,
                score, lastResult, x, y, awardedWeaponShots, mineId, shotEnemyId, invulnerable);
        miss.setBetLevel(betLevel);
        miss.setFragmentId(fragmentId);
        miss.setBulletId(bulletId);
        miss.setEffects(effects);
        return miss;
    }

    @Override
    public Miss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                           int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                           int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable,
                           int betLevel, String bulletId) {
        Miss miss = new Miss(date, rid, seatId, killedMiss, awardedWeaponId, enemyId, usedSpecialWeapon, remainingSWShots,
                score, lastResult, x, y, awardedWeaponShots, mineId, shotEnemyId, invulnerable);
        miss.setBetLevel(betLevel);
        miss.setBulletId(bulletId);
        return miss;
    }

    @Override
    public Hit createHit(long date, int rid, int seatId, double damage, double win, int awardedWeaponId,
                         int usedSpecialWeapon, int remainingSWShots, double score, IRoomEnemy enemy, boolean lastResult,
                         double currentWin, long hvEnemyId, float x, float y, int awardedWeaponShots, boolean killed,
                         String mineId, int newFreeShots, int newFreeShotsSeatId, boolean instanceKill, int chMult,
                         long enemyId, long shotEnemyId) {
        return new Hit(date, rid, seatId, damage, win, awardedWeaponId, usedSpecialWeapon, remainingSWShots, score,
                enemy, lastResult, currentWin, hvEnemyId, x, y, awardedWeaponShots, killed, mineId, newFreeShots,
                newFreeShotsSeatId, instanceKill, chMult, enemyId, shotEnemyId);
    }

    @Override
    public Hit createHit(long date, int rid, int seatId, double damage, double win, int awardedWeaponId,
                         int usedSpecialWeapon, int remainingSWShots, double score, IRoomEnemy enemy, boolean lastResult,
                         double currentWin, long hvEnemyId, float x, float y, int awardedWeaponShots, boolean killed,
                         String mineId, int newFreeShots, int newFreeShotsSeatId, boolean instanceKill, int chMult,
                         long enemyId, long shotEnemyId, String bulletId) {
        Hit hit = new Hit(date, rid, seatId, damage, win, awardedWeaponId, usedSpecialWeapon, remainingSWShots, score,
                enemy, lastResult, currentWin, hvEnemyId, x, y, awardedWeaponShots, killed, mineId, newFreeShots,
                newFreeShotsSeatId, instanceKill, chMult, enemyId, shotEnemyId);
        hit.setBulletId(bulletId);
        return hit;
    }

    @Override
    public MinePlace createMinePlace(long date, int rid, int seatId, float x, float y, String mineId) {
        return new MinePlace(date, rid, seatId, x, y, mineId);
    }

    @Override
    public WinPrize createWinPrize(int id, String value) {
        return new WinPrize(id, value);
    }

    @Override
    public UpdateQuest createUpdateQuest(long date, IQuest quest, long lastEnemyId) {
        return new UpdateQuest(date, quest, lastEnemyId);
    }

    @Override
    public NewTreasure createNewTreasure(long date, int rid, long id, long enemyId, int completedQuestId, long questId) {
        return new NewTreasure(date, rid, id, enemyId, completedQuestId, questId);
    }

    @Override
    public SeatWinForQuest createSeatWinForQuest(long date, int rid, long seatId, long enemyId, long winAmount,
                                                 int awardedWeaponId) {
        return new SeatWinForQuest(date, rid, seatId, enemyId, winAmount, awardedWeaponId);
    }

    @Override
    public Class<?> getClassForRoundId() {
        return RoundInfo.class;
    }

    @Override
    public ITransportObject createBonusStatusChangedMessage(long bonusId, String oldStatus, String newStatus,
                                                            String reason, String type) {
        return new BonusStatusChanged(System.currentTimeMillis(), -1, bonusId, oldStatus, newStatus, reason, type);
    }

    @Override
    public ITransportObject createTournamentStateChangedMessage(long tournamentId, String oldState, String newState, String reason) {
        return new TournamentStateChanged(System.currentTimeMillis(), -1, tournamentId, oldState, newState, reason);
    }

    @Override
    public ISpin createSpin(List<Integer> reels, double win) {
        return new Spin(reels, win);
    }

    @Override
    public IChangeEnemyMode createChangeEnemyModeMessage(long enemyId, EnemyMode enemyMode) {
        return new ChangeEnemyMode(System.currentTimeMillis(), enemyId, enemyMode);
    }

    @Override
    public IDamage createRageDamage(long enemyId, int damage) {
        return new RageDamage(enemyId, damage);
    }

    public IEnemyMode createGameEnemyMode(long enemyId, int enemyMode) {
        return new GameEnemyMode(enemyId, enemyMode);
    }

    @Override
    public IUpdateWeaponPaidMultiplierResponse createUpdateWeaponPaidMultiplierResponse(long date, int rid,
                                                                                        Map<Integer, Integer> weaponPaidMultiplier) {
        return new UpdateWeaponPaidMultiplierResponse(date, rid, weaponPaidMultiplier);
    }

    @Override
    public IBattlegroundScoreBoard createBattlegroundScoreBoard(long startTime, long endTime, List<IBattleScoreInfo> score, Map<Integer, Long> scoreBoss, int rid) {
        return new BattlegroundScoreBoard(System.currentTimeMillis(), rid, startTime, endTime, score, scoreBoss);
    }

    @Override
    public IBattleScoreInfo createBattleScoreInfo(int seatId, long betAmount, long winAmount, boolean isKing) {
        return new BattleScoreInfo(seatId, winAmount, betAmount, isKing);
    }

    @Override
    public IBulletClearResponse createBulletClearResponse(long time, int rid, int seatId) {
        return new BulletClearResponse(time, rid, seatId);
    }

    @Override
    public ITransportAsteroid createAsteroid(int type, Double speed, Double spawnX, Double spawnY, Double slow){
        return new Asteroid(type, speed, spawnX, spawnY, slow);
    }

    @Override
    public ICrashStateInfo createCrashStateInfo(long date, double currentMult, double timeSpeedMult) {
        return new CrashStateInfo(date, currentMult, timeSpeedMult);
    }

    @Override
    public ICrashCancelBet createCrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seaWin, String crashBetId, String name) {
        return new CrashCancelBetResponse(date, rid, currentMult, seatId, seaWin, crashBetId, name);
    }

    @Override
    public ICrashCancelBet createCrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seaWin, String crashBetId, String name, long balance) {
        return new CrashCancelBetResponse(date, rid, currentMult, seatId, seaWin, crashBetId, name, balance);
    }

    @Override
    public ICrashAllBetsRejected createCrashAllBetsRejectedResponse(long date, int rid, int seatId, String name) {
        return new CrashAllBetsRejectedResponse(date, rid, seatId, name);
    }

    @Override
    public ICrashAllBetsRejected createCrashAllBetsRejectedDetailedResponse(long date, int rid, int seatId, String name, int errorCode, String errorMessage) {
        return new CrashAllBetsRejectedDetailedResponse(date, rid, seatId, name, errorCode, errorMessage);
    }

    @Override
    public ICrashAllBets createCrashAllBetsResponse(long date, int rid, int seatId, String name, long balance, long amount) {
        return new CrashAllBetsResponse(date, rid, seatId, name, balance, amount);
    }


    @Override
    public ICrashGameInfo createCrashGameInfo(long date, int rid, long roomId, int mapId, long startTime, RoomState state, List<ITransportSeat> seats,
                                              long roundId, List<ICrashRoundInfo> multHistory, int nextMapId, long ttnx, String function, boolean isBattleGame, double kilometerMult, Double rakePercent) {
        return new CrashGameInfo(date, rid, roomId, mapId, startTime, state, seats, roundId, multHistory, nextMapId, ttnx, function, isBattleGame, kilometerMult, rakePercent);
    }

    @Override
    public IRoomManagerChanged createRoomManagerChanged(long date, int rid, int newRoomManager) {
        return new RoomManagerChanged(date, rid, newRoomManager);
    }

    @Override
    public IBuyInConfirmedSeats createBuyInConfirmedSeats(long date, int rid, List<Integer> confirmedSeatsId) {
        return new BuyInConfirmedSeats(date, rid, confirmedSeatsId);
    }

    @Override
    public IAddWinRequest createAddWinRequest(String sessionId, long gameSessionId, long winAmount, long returnedBet, long accountId,
                                              IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo, long gsRoundId, boolean isSitOut) {
        return new AddWinRequest(sessionId, gameSessionId, winAmount, returnedBet, accountId, playerBet, bgRoundInfo, gsRoundId, isSitOut);
    }

    @Override
    public IKickResponse createKickResponse(long date, int rid) {
        return new KickResponse(date, rid);
    }

    @Override
    public ICancelKickResponse createCancelKickResponse(long date, int rid) {
        return new CancelKickResponse(date, rid);
    }

    @Override
    public IPrivateRoomInviteResponse createPrivateRoomInviteResponse(long date, int rid, boolean successful) {
        return new PrivateRoomInviteResponse(date, rid, successful);
    }

    @Override
    public IFinishGameSessionResponse createFinishGameSessionResponse(long date, int rid, boolean successful) {
        return new FinishGameSessionResponse(date, rid, successful);
    }


    public IRoomWasOpened createRoomWasOpened(long date, int rid, String nickname, boolean isKicked) {
        return new RoomWasOpened(date, rid, nickname, isKicked);
    }

    public IObserverRemoved createObserverRemoved(long date, int rid, String nickname) {
        return new ObserverRemoved(date, rid, nickname);
    }

    @Override
    public ILatencyResponse createLatencyResponse(long date, int rid, int step, long serverTs, long serverAckTs, long clientTs, long clientAckTs) {
        return  Latency.Builder.newBuilder(date, rid, step)
                .withServerTs(serverTs)
                .withServerAckTs(serverAckTs)
                .withClientTs(clientTs)
                .withClientAckTs(clientAckTs)
                .build();
    }
}
