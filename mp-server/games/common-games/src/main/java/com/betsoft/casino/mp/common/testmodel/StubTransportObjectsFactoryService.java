package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.common.SeatBullet;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.quests.INewTreasure;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.IUpdateQuest;
import com.betsoft.casino.mp.model.room.*;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StubTransportObjectsFactoryService implements ITransportObjectsFactoryService {
    @Override
    public ISitInResponse createSitInResponse(long date, int number, String nickname, long enterDate, long ammoAmount, long balance,
                                              IAvatar avatar, List<IWeapon> weapons, List<Double> weaponLootBoxPrices,
                                              boolean showRefreshBalanceButton, int level, boolean frbMode, long frbBalance, String mode, double rake) {
        return null;
    }

    public IWeapons createWeapons(long date, int rid, int ammoAmount, boolean freeShots, List<ITransportWeapon> weapons) {
        return null;
    }

    @Override
    public ITransportWeapon createWeapon(int id, int shots) {
        return new StubWeapon(id, shots);
    }

    @Override
    public ITransportWeapon createWeapon(int id, int shots, int sourceId) {
        return new StubWeapon(id, shots, sourceId);
    }

    @Override
    public IShotResponse createShotResponse(long date, int rid, int seatId, int weaponId, int remainingSWShots) {
        return null;
    }

    @Override
    public IEnemyDestroyed createEnemyDestroyed(long date, int rid, long enemyId, int reason) {
        return null;
    }

    @Override
    public ILevelUp createLevelUp(long date, int rid, int seatId, int level) {
        return null;
    }

    @Override
    public Experience createExperience(double amount) {
        return new Experience(amount);
    }

    public IChangeMap createChangeMap(long date, int mapId, String subround) {
        return new StubChangeMap(date, mapId, subround);
    }

    @Override
    public IUpdateTrajectories createUpdateTrajectories(long date, int rid, Map<Long, Trajectory> trajectories,
                                                        int freezeTime, int animationId) {
        return null;
    }

    @Override
    public IUpdateTrajectories createUpdateTrajectories(long date, int rid, Map<Long, Trajectory> trajectories) {
        return null;
    }

    @Override
    public IAward createAward(int seatId, double score, float qualifyWin) {
        return null;
    }

    @Override
    public IAwards createAwards(long date, long enemyId) {
        return null;
    }

    @Override
    public IError createError(int code, String msg, long date, int rid) {
        return null;
    }

    @Override
    public IEnemyProgress createEnemyProgress(int typeId, int skin, int kills, int goal) {
        return null;
    }

    @Override
    public StubTreasureProgress createTreasureProgress(int treasureId, int collect, int goal) {
        return new StubTreasureProgress(treasureId, collect, goal);
    }

    @Override
    public ITransportEnemy createEnemy(long id, int width, int height, float speed, int prizes, double sumAward, int skins,
                                       boolean boss) {
        return null;
    }

    @Override
    public IRoundFinishSoon createRoundFinishSoon(long date) {
        return null;
    }

    @Override
    public IRoomEnemy createRoomEnemy(long id,
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
        return new StubRoomEnemy(id, typeId, speed, awardedPrizes, awardedSum, energy, skin, trajectory, parentEnemyId, fullEnergy, members, swarmId, swarmType);
    }

    @Override
    public IRoomEnemy createRoomEnemy(long id,
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
        return new StubRoomEnemy(id, typeId, speed, awardedPrizes, awardedSum, energy, skin, trajectory, parentEnemyId, fullEnergy, members, swarmId, swarmType, parentEnemyTypeId);
    }

    @Override
    public ITransportSeat createSeat(int id, String nickname, long enterDate, double totalScore, double currentScore,
                                     IAvatar avatar, int specialWeaponId, int level, int unplayedFreeShots, double totalDamage, long roundWin) {
        return null;
    }

    @Override
    public ITransportSeat createSeat(int id, String nickname, long enterDate, double totalScore, double currentScore, IAvatar avatar,
                                     int specialWeaponId, int level, int unplayedFreeShots, double totalDamage, int betLevel, long roundWin) {
        return null;
    }

    @Override
    public ITransportObserver createObserver(String nickname, boolean isKicked, Status status, Boolean isOwner) {
        return null;
    }

    @Override
    public IWeaponLootBox createWeaponLootBox(long date, int rid, int weaponId, int shots, long balance,
                                              float currentWin, int usedAmmoAmount) {
        return new StubWeaponLootBox(date, rid, weaponId, shots, balance, currentWin, usedAmmoAmount);
    }

    @Override
    public IGameStateChanged createGameStateChanged(long date, RoomState state, long ttnx, long roundId, Long roundStartTime) {
        return new StubGameStateChanged(date, state, ttnx, roundId);
    }

    @Override
    public IPlayerStats createPlayerStats() {
        return null;
    }

    @Override
    public ISitOutResponse createSitOutResponse(long date, int rid, int id, String nickname, long outDate,
                                                long compensateSpecialWeapons, long surplusHvBonus,
                                                long totalReturnedSpecialWeapons, long nextRoomId, boolean hasNextFrb) {
        return null;
    }

    @Override
    public INewEnemies createNewEnemies(long date, List<IRoomEnemy> enemies) {
        return null;
    }

    @Override
    public INewEnemy createNewEnemy(long date, IRoomEnemy newEnemy) {
        return null;
    }

    @Override
    public IRoundResult createRoundResult(long date, int rid, double winAmount, double winRebuyAmount, long balance,
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
        return null;
    }

    @Override
    public ILevelInfo createLevelInfo(int level, long score, long minScore, long maxScore) {
        return null;
    }

    @Override
    public IBattlegroundRoundResult createBattlegroundRoundResult(long id, long score, long rank, long pot, String nickName) {
        return null;
    }

    @Override
    public TInboundObject createBetLevelResponse(long date, int rid, int betLevel, int seatId) {
        return null;
    }

    @Override
    public ICancelBattlegroundRound createCancelBattlegroundRound(long refundedAmount, String reason) {
        return null;
    }

    @Override
    public IKingOfHillChanged createKingOfHillChanged(long date, int rid, List<Integer> newKings) {
        return null;
    }

    @Override
    public IBalanceUpdated createBalanceUpdated(long date, long balance, int serverAmmo) {
        return null;
    }

    @Override
    public IFRBEnded createFRBEnded(long date, long winSum, String closeReason, boolean hasNextFrb, long realWinSum) {
        return null;
    }

    @Override
    public ITransportObject getOkResponse(long date, int rid) {
        return null;
    }

    @Override
    public IGetRoomInfoResponse createGetRoomInfoResponse(long date, long roomId, int rid, String name, short maxSeats,
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
        return null;
    }

    @Override
    public IRoomBattlegroundInfo createRoomBattlegroundInfo(long buyIn, boolean buyInConfirmed,
                                                            int timeToStart, List<Integer> kingsOfHill, long score, long rank, long pot, double potTaxPercent,
                                                            String joinUrl, List<Integer> confirmedSeatsId) {
        return null;
    }

    @Override
    public IGetFullGameInfo createGetFullGameInfo(long date, int rid) {
        return null;
    }

    @Override
    public IFullGameInfo createFullGameInfo(long date, int rid, int mapId, String subround,
                                            long startTime, RoomState state, List<IRoomEnemy> roomEnemies,
                                            List<ITransportSeat> seats, List<IMinePlace> mines,
                                            Map<Long, Integer> freezeTime, boolean immortalBoss, long roundId,
                                            Map<Integer, Integer> seatGems, int betLevel,
                                            Map<Long, Integer> enemiesModes, Set<SeatBullet> allBullets, long timeToStart,
                                            Map<Integer, List<Integer>> reels, int currentPowerUpMultiplier, Map<Integer, Double> gemPrizes) {
        return null;
    }

    @Override
    public IWeaponSurplus createWeaponSurplus(int id, int shots, long winBonus) {
        return new StubWeaponSurplus(id, shots, winBonus);
    }

    @Override
    public IShot createShot(long date, int rid, int weaponId, long enemyId, float x, float y, boolean isPaidSpecialShot) {
        return new StubShot(date, rid, weaponId, enemyId, x, y, isPaidSpecialShot);
    }

    @Override
    public IMiss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                            int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                            int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable) {
        return null;
    }

    @Override
    public IMiss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId, long enemyId,
                            int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult, float x, float y,
                            int awardedWeaponShots, String mineId, long shotEnemyId, boolean invulnerable, int betLevel,
                            Integer fragmentId, List<String> effects, String bulletId) {
        return null;
    }

    @Override
    public IMiss createMiss(long date, int rid, int seatId, boolean killedMiss, int awardedWeaponId,
                            long enemyId, int usedSpecialWeapon, int remainingSWShots, double score, boolean lastResult,
                            float x, float y, int awardedWeaponShots, String mineId, long shotEnemyId,
                            boolean invulnerable, int betLevel, String bulletId) {
        return null;
    }

    public IHit createHit(long date, int rid, int seatId, double damage, double win, int awardedWeaponId,
                          int usedSpecialWeapon, int remainingSWShots, double score, IRoomEnemy enemy, boolean lastResult,
                          double currentWin, long hvEnemyId, float x, float y, int awardedWeaponShots, boolean killed,
                          String mineId, int newFreeShots, int newFreeShotsSeatId, boolean instanceKill, int chMult,
                          long enemyId, long shotEnemyId) {
        return new StubHit(date, rid, seatId, damage, win, awardedWeaponId, usedSpecialWeapon, remainingSWShots, score,
                enemy, lastResult, currentWin, hvEnemyId, x, y, awardedWeaponShots, killed, mineId, newFreeShots,
                newFreeShotsSeatId, instanceKill, chMult, enemyId, shotEnemyId);
    }

    public IHit createHit(long date, int rid, int seatId, double damage, double win, int awardedWeaponId,
                          int usedSpecialWeapon, int remainingSWShots, double score, IRoomEnemy enemy, boolean lastResult,
                          double currentWin, long hvEnemyId, float x, float y, int awardedWeaponShots, boolean killed,
                          String mineId, int newFreeShots, int newFreeShotsSeatId, boolean instanceKill, int chMult,
                          long enemyId, long shotEnemyId, String bulletId) {
        StubHit stubHit = new StubHit(date, rid, seatId, damage, win, awardedWeaponId, usedSpecialWeapon, remainingSWShots, score,
                enemy, lastResult, currentWin, hvEnemyId, x, y, awardedWeaponShots, killed, mineId, newFreeShots,
                newFreeShotsSeatId, instanceKill, chMult, enemyId, shotEnemyId);
        stubHit.setBulletId(bulletId);
        return stubHit;
    }

    @Override
    public IMinePlace createMinePlace(long date, int rid, int seatId, float x, float y, String mineId) {
        return null;
    }

    @Override
    public IWinPrize createWinPrize(int id, String value) {
        return null;
    }

    @Override
    public IUpdateQuest createUpdateQuest(long date, IQuest quest, long lastEnemyId) {
        return null;
    }

    @Override
    public INewTreasure createNewTreasure(long date, int rid, long id, long enemyId, int completedQuestId, long questId) {
        return null;
    }

    @Override
    public ISeatWinForQuest createSeatWinForQuest(long date, int rid, long seatId, long enemyId, long winAmount,
                                                  int awardedWeaponId) {
        return null;
    }

    @Override
    public Class getClassForRoundId() {
        return this.getClass();
    }

    @Override
    public ITransportObject createBonusStatusChangedMessage(long bonusId, String oldStatus, String newStatus,
                                                            String reason, String type) {
        return null;
    }

    @Override
    public ITransportObject createTournamentStateChangedMessage(long tournamentId, String oldStatus, String newStatus, String reason) {
        return null;
    }

    @Override
    public ISpin createSpin(List<Integer> reels, double win) {
        return null;
    }

    @Override
    public IChangeEnemyMode createChangeEnemyModeMessage(long enemyId, EnemyMode enemyMode) {
        return null;
    }

    @Override
    public IDamage createRageDamage(long enemyId, int damage) {
        return null;
    }

    @Override
    public IEnemyMode createGameEnemyMode(long enemyId, int enemyMode) {
        return null;
    }

    @Override
    public IUpdateWeaponPaidMultiplierResponse createUpdateWeaponPaidMultiplierResponse(long date, int rid, Map<Integer, Integer> weaponPaidMultiplier) {
        return null;
    }

    @Override
    public IBattlegroundScoreBoard createBattlegroundScoreBoard(long startTime, long endTime, List<IBattleScoreInfo> score,
                                                                Map<Integer, Long> scoreBoss, int rid) {
        return null;
    }

    @Override
    public IBattleScoreInfo createBattleScoreInfo(int seatId, long betAmount, long winAmount, boolean isKing) {
        return null;
    }

    @Override
    public IBulletClearResponse createBulletClearResponse(long time, int rid, int seatId) {
        return null;
    }

    @Override
    public ITransportAsteroid createAsteroid(int type, Double speed, Double spawnX, Double spawnY, Double slow) {
        return null;
    }

    @Override
    public ICrashStateInfo createCrashStateInfo(long date, double currentMult, double timeSpeedMult) {
        return null;
    }

    @Override
    public ICrashCancelBet createCrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seaWin, String crashBetId, String name) {
        return null;
    }

    @Override
    public ICrashCancelBet createCrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seatWin, String crashBetId, String name, long balance) {
        return null;
    }

    @Override
    public ICrashAllBetsRejected createCrashAllBetsRejectedResponse(long date, int rid, int seatId, String name) {
        return null;
    }

    @Override
    public ICrashAllBetsRejected createCrashAllBetsRejectedDetailedResponse(long date, int rid, int seatId, String name, int errorCode, String errorMessage) {
        return null;
    }

    @Override
    public ICrashAllBets createCrashAllBetsResponse(long date, int rid, int seatId, String name, long balance, long amount) {
        return null;
    }

    @Override
    public ICrashGameInfo createCrashGameInfo(long date, int rid, long roomId, int mapId, long startTime, RoomState state, List<ITransportSeat> seats, long roundId,
                                              List<ICrashRoundInfo> multHistory, int nextMapId, long ttnx, String function, boolean isBattleGame, double kilometerMult, Double rakePercent) {
        return null;
    }

    @Override
    public IRoomManagerChanged createRoomManagerChanged(long date, int rid, int newRoomManager) {
        return null;
    }

    @Override
    public IBuyInConfirmedSeats createBuyInConfirmedSeats(long date, int rid, List<Integer> confirmedSeatsId) {
        return null;
    }

    @Override
    public IAddWinRequest createAddWinRequest(String sessionId, long gameSessionId, long winAmount, long returnedBet, long accountId,
                                              IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo, long gsRoundId, boolean isSitOut) {
        return null;
    }

    @Override
    public IKickResponse createKickResponse(long date, int rid) {
        return null;
    }

    @Override
    public ICancelKickResponse createCancelKickResponse(long date, int rid) {
        return null;
    }

    @Override
    public IPrivateRoomInviteResponse createPrivateRoomInviteResponse(long date, int rid, boolean successful) {
        return null;
    }

    @Override
    public IFinishGameSessionResponse createFinishGameSessionResponse(long date, int rid, boolean successful) {
        return null;
    }

    @Override
    public IRoomWasOpened createRoomWasOpened(long date, int rid, String nickname, boolean isKicked) {
        return null;
    }

    public IObserverRemoved createObserverRemoved(long date, int rid, String nickname) {
        return null;
    }

    @Override
    public ILatencyResponse createLatencyResponse(long date, int rid, int step, long serverTs, long serverAckTs, long clientTs, long clientAckTs) {
        return null;
    }
}
