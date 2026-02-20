package com.betsoft.casino.mp.bgsectorx.model;

import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.BossType;
import com.betsoft.casino.mp.bgsectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.bgsectorx.model.math.EnemyType;
import com.betsoft.casino.mp.bgsectorx.model.math.MathData;
import com.betsoft.casino.mp.bgsectorx.model.math.config.*;
import com.betsoft.casino.mp.service.IGameConfigProvider;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ISpawnConfigProvider;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {
    private final IGameConfigProvider gameConfigProvider;
    private final ISpawnConfigProvider spawnConfigProvider;

    public EnemyGame(Logger logger, IGameConfigService<? extends IGameConfigService<?>> gameConfigService,
                     IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(logger, GameType.SECTOR_X, gameConfigService);
        this.gameConfigProvider = gameConfigProvider;
        this.spawnConfigProvider = spawnConfigProvider;
    }

    @Override
    public List<EnemyType> getBaseEnemyTypes() {
        return EnemyRange.BASE_ENEMIES.getEnemies();
    }

    @Override
    protected ShootResult shootBaseEnemy(Seat seat, Weapon weapon, Money stake, Enemy enemy, boolean isBot,
                                         boolean isBossRound, boolean isNearLandMine, double damageMultiplier,
                                         ITransportObjectsFactoryService toService) {
        throw new UnsupportedOperationException("Shot without room mode is not allowed");
    }

    protected ShootResult shootBaseEnemy(Seat seat, Weapon weapon, Money stake, Enemy enemy, boolean isBossRound,
                                         ITransportObjectsFactoryService toService, int countPlayers, long lastTimeBossShot, long timeInterval) throws CommonException {
        GameConfig config = getConfig(seat);

        double gameRTP = config.getGameRTP();

        long accountId = seat.getAccountId();
        logMessage(accountId, "shootBaseEnemy started, weapon: {}", weapon);

        int betLevel = getBetLevelWithCheck(seat);
        ArrayList<Integer> possibleBetLevels = new ArrayList<>(MathData.getPossibleBetLevels());
        int idxBetLevel = possibleBetLevels.indexOf(betLevel);
        SpecialWeaponType weaponType = weapon == null ? null : weapon.getType();

        //boolean paidSpecialShot = actualShot.isPaidSpecialShot();
        //logMessage(accountId, "paidSpecialShot: {} seat.getBetLevel(): {}", paidSpecialShot, betLevel);

        String sessionId = seat.getPlayerInfo().getSessionId();
        BGSectorXTestStand testStand = getTestStand(accountId, sessionId);

        boolean isSpecialWeapon = (weapon != null);
        boolean isBoss = enemy.isBoss();
        boolean isPaidShot = !isSpecialWeapon;

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        List<Integer> shotGems = initializeShotGems();
        Money totalGemPayout = Money.ZERO;

        Money payout = Money.ZERO;
        int chMult = 1;
        boolean isKilled = false;
        boolean isHit = false;

        EnemyType enemyType = enemy.getEnemyType();

        Money killAwardWin = Money.ZERO;
        if (isBoss) {
            double percentRangeMode = 10;
            double rageThreshold = (enemy.getFullEnergy() * percentRangeMode) / 100;
            boolean isFixedPay = enemy.getEnergy() <= rageThreshold;
            BossType bossType = BossType.getBySkinId(enemy.getSkin());
            double hitProbabilityForBoss;
            int randomBossPay = testStand.isNeedKill()
                    ? (int) enemy.getFullEnergy()
                    : MathData.getRandomBossSmallPay(config, bossType, countPlayers);
            hitProbabilityForBoss = testStand.isNeedKill() ? 1
                    : MathData.getHitSmallProbabilityForBoss(config, bossType, randomBossPay, countPlayers);
            if (isFixedPay) {
                randomBossPay = MathData.getFixedBossPay(config, bossType, countPlayers);
                hitProbabilityForBoss = testStand.isNeedKill() ? 1
                        : config.getGameRTP() / 100 / randomBossPay;
            }
            double bossPkillDiscount = config.getBossPkillDiscount().get(countPlayers);
            hitProbabilityForBoss *= bossPkillDiscount;
            long currentTotalWin = 0;
            boolean needKill = false;
            if (!testStand.isNotNeedAnyWin()) {
                if (RNG.rand() < hitProbabilityForBoss) {
                    if ((System.currentTimeMillis() - lastTimeBossShot) > timeInterval) {
                        payout = stake.getWithMultiplier(randomBossPay * betLevel);
                        seat.addTotalBossPayout(payout.toCents());
                        double newEnergy = enemy.getEnergy() - randomBossPay;
                        if (newEnergy < 0 && !isFixedPay) {
                            newEnergy = 0;
                        }
                        enemy.setEnergy(newEnergy);
                        needKill = isFixedPay;
                        logMessage(accountId, "needKill: {}, randomBossPay: {}, betLevel: {}, newEnergy: {}, isFixedPay: {}",
                                needKill, randomBossPay, betLevel, newEnergy, isFixedPay);
                    }
                }

                if (needKill || testStand.isNeedKill()) {
                    currentTotalWin = seat.getTotalBossPayout();
                    int bossKillMultiplierFromConfig = config.getBossKillMultiplier();
                    int bossKillMultiplier = bossKillMultiplierFromConfig <= 1 ? 0 : bossKillMultiplierFromConfig - 1;
                    killAwardWin = Money.fromCents(currentTotalWin * bossKillMultiplier);
                    isKilled = true;
                    chMult = bossKillMultiplier + 1;
                }
            }

            if (payout.greaterThan(Money.ZERO)) {
                logMessage(accountId, "boss hitProbabilityForBoss: {}, randomBossPay: {}, " +
                                "payout: {}, killAwardWin: {}, currentTotalWin: {}," +
                                " seat.getTotalBossPayout(): {}, needKill: {}",
                        hitProbabilityForBoss,
                        randomBossPay,
                        payout.toDoubleCents(),
                        killAwardWin.toDoubleCents(),
                        currentTotalWin, seat.getTotalBossPayout(), needKill);
            }
        } else {
            int idxData = 0;
            logMessage(accountId, "idxData for enemy " + idxData);


            double hitProbability;
            if(EnemyRange.SPECIAL_ITEMS.contains(enemyType)){
                EnemySpecialItem enemySpecialItem = (EnemySpecialItem) enemy;
                hitProbability = gameRTP / 100 / enemySpecialItem.getTotalPayout() / enemySpecialItem.getCurrentMultiplier();
                logMessage(accountId, "enemySpecialItem.getTotalPayout(): " + enemySpecialItem.getTotalPayout());
            } else {
                if (EnemyRange.MID_PAY_ENEMIES.contains(enemyType)) {
                    hitProbability = MathData.getHitProbability(config, enemyType);
                } else if (EnemyRange.HIGH_PAY_ENEMIES.contains(enemyType)) {
                    hitProbability = MathData.getHitProbability(config, enemyType);
                } else if (EnemyType.B3.equals(enemyType)){
                    /*double averagePay = (MathData.getMinEnemyPayout(config, enemyType, idxData) +
                            MathData.getMaxEnemyPayout(config, enemyType, idxData)) / 2;*/
                    hitProbability = MathData.getHitProbability(config, enemyType);
                } else {
                    hitProbability = MathData.getHitProbability(config, enemyType);
                }
            }


            hitProbability *= config.getTurretLevelRatio().get(betLevel);
            logMessage(accountId, "ratio: {}, enemyType: {}, hitProbability: {} ", config.getTurretLevelRatio().get(betLevel),
                    enemyType, hitProbability);

            isHit = RNG.rand() < hitProbability;

            if (testStand.isNeedHit()) {
                logMessage(accountId, "Hit from TestStand");
                isHit = true;
            }

            if (testStand.isNeedKill()) {
                isHit = true;
                logMessage(accountId, "teststand need kill");
            }

            if (testStand.isNeedCriticalHit() && weaponTypeId != -1) {
                isHit = true;
                logMessage(accountId, "teststand need CH");
            }

            if (testStand.isNotNeedAnyWin()) {
                isHit = false;
                logMessage(accountId, "teststand isNotNeedAnyWin isHit is false");
            }

            logMessage(accountId, "wpId: {}, hitProbability: {}, isHit: {}", weaponTypeId, hitProbability, isHit);

            if (isHit) {
                isKilled = true;
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                enemy.setEnergy(0);
                int enemyPayout = 0;

                if (EnemyRange.BASE_ENEMIES.contains(enemyType) && !EnemyRange.HUGE_PAY_ENEMIES.contains(enemyType)) {
                    enemyPayout = MathData.getEnemyPayout(config, enemyType, idxData);
                    CriticalMultiplierType criticalMultiplierType;
                    if (EnemyRange.LOW_PAY_ENEMIES.contains(enemyType)) {
                        criticalMultiplierType = CriticalMultiplierType.LP;
                    } else if (EnemyRange.MID_PAY_ENEMIES.contains(enemyType)) {
                        criticalMultiplierType = CriticalMultiplierType.MP;
                    } else {
                        criticalMultiplierType = CriticalMultiplierType.HP;
                    }
                    List<Double> multipliers = config.getCriticalMultiplier().get(criticalMultiplierType);
                    int idMult = GameTools.getIndexFromDoubleProb(multipliers.stream().mapToDouble(Double::doubleValue).toArray()) + 1;
                    chMult = idMult;
                    enemyPayout = enemyPayout * chMult;
                } else if (EnemyRange.HUGE_PAY_ENEMIES.contains(enemyType)) {
                    if (enemyType.getId() == EnemyType.B3.getId()) {
                        enemyPayout = RNG.nextInt(MathData.getMinEnemyPayout(config, enemyType, idxData),
                                MathData.getMaxEnemyPayout(config, enemyType, idxData) + 1);
                    } else {
                        enemyPayout = MathData.getEnemyPayout(config, enemyType, idxData);
                    }
                    List<Double> multipliers = config.getCriticalMultiplier().get(CriticalMultiplierType.SP);
                    int idMult = GameTools.getIndexFromDoubleProb(multipliers.stream().mapToDouble(Double::doubleValue).toArray()) + 1;
                    chMult = idMult;
                    enemyPayout = enemyPayout * chMult;
                } else if (EnemyRange.SPECIAL_ITEMS.contains(enemyType)) {
                    SpecialItem specialItem = config.getItems().get(enemyType).get(0);
                    if (EnemyType.F1.equals(enemyType)) {
                        EnemySpecialItem enemySpecialItem = (EnemySpecialItem) enemy;
                        enemyPayout = enemySpecialItem.getTotalPayout();
                        logMessage(accountId, "Special item F1 is killed, prize: {} ", enemyPayout);
                    }
                }

                payout = stake.getWithMultiplier(enemyPayout * betLevel);
                if (hitProbability > 1) {
                    double diff = hitProbability - 1;
                    killAwardWin = stake.getWithMultiplier(enemyPayout * diff * betLevel);
                    logMessage(accountId, "hit prob more 1, compensate to killAward, diff: {}, killAwardWin: {}, enemyPayout: {}",
                            diff, killAwardWin, enemyPayout);
                }
            }
        }

        double probToTriggerLevelUp = seat.getBetLevel() == 1 ? config.getTurretLevelUp().getTriggerProb() :
                config.getTurretLevelUp().getReTriggerProb();
        boolean isLevelUpDropped = RNG.rand() < probToTriggerLevelUp;
        boolean needLevelUp = isLevelUpDropped || testStand.isNeedLevelUp();
        logMessage(accountId, "testStand.isNeedLevelUp(): {}, isLevelUpDropped: {}", testStand.isNeedLevelUp(), isLevelUpDropped);

        if (needLevelUp) {
            int weaponId = SpecialWeaponType.LevelUp.getId();
            int newSpecialWeaponShots = config.getTurretLevelUp().getNumShotsRewardPerTrigger();
            awardedWeapons.add(toService.createWeapon(weaponId, newSpecialWeaponShots));
        }

        boolean bossShouldBeAppeared = !isBossRound && testStand.isNeedSpawnBoss();

        logMessage(accountId, "win: {}", payout);

        ShootResult shootResult = new ShootResult(isPaidShot ? stake : Money.ZERO, payout, bossShouldBeAppeared,
                isKilled, enemy);
        shootResult.setChMult(chMult);

        if (testStand.isFeatureCompleted()) {
            TestStandLocal.getInstance().removeFeatureBySid(sessionId);
        }

        if (isKilled) {
            if (killAwardWin.smallerThan(Money.ZERO)) {
                killAwardWin = Money.ZERO;
            }
            shootResult.setKillAwardWin(killAwardWin);
        }

        shootResult.setAwardedWeapons(awardedWeapons);
        shootResult.setTotalGemsPayout(totalGemPayout);
        shootResult.setGems(shotGems);

        logMessage(accountId, "shootResult: {}", shootResult);

        return shootResult;
    }

    private int getBetLevelWithCheck(Seat seat) throws CommonException {
        int betLevel = seat.getBetLevel();
        if (!MathData.getPossibleBetLevels().contains(betLevel)) {
            throw new CommonException("Invalid bet level");
        }
        return betLevel;
    }

    private IShot getShotWithCheck(Seat seat) throws CommonException {
        IShot actualShot = seat.getActualShot();
        if (actualShot == null) {
            throw new CommonException("actual shot is null");
        }
        return actualShot;
    }

    private List<Integer> initializeShotGems() {
        List<Integer> shotGems = new ArrayList<>();
        for (int i = 0; i < Gem.values().length; i++) {
            shotGems.add(0);
        }
        return shotGems;
    }

    private BGSectorXTestStand getTestStand(long accountId, String sessionId) {
        if (sessionId != null) {
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(accountId, " testStand feature: {}", featureBySid);
            if (featureBySid != null) {
                return new BGSectorXTestStand(featureBySid);
            }
        }
        return new BGSectorXTestStand();
    }

    @Override
    protected boolean isRealBet(Weapon weapon) {
        return true;
    }

    @Override
    public IShootResult doShootWithExplode(Enemy enemy, Seat seat, int explodeDamage,
                                           ITransportObjectsFactoryService toService) {
        throw new UnsupportedOperationException();
    }

    private void addKill(Seat seat) {
        seat.incCountEnemiesKilled();
        IPlayerStats roundStats = seat.getPlayerInfo().getRoundStats();
        Map<Integer, Long> roundStatsKills = roundStats.getKills();
        roundStatsKills.put(0, roundStatsKills.isEmpty() ? 1 : roundStatsKills.get(0) + 1);
    }

    public ShootResult doShoot(Enemy enemy, Seat seat, Money stake, boolean isBossRound,
                               ITransportObjectsFactoryService toService, int countPlayers, long lastTimeBossShot, long timeInterval) throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        if (debug) {
            logMessage(seat.getAccountId(), "doShoot, sessionId: " + sessionId
                    + " seat.getCurrentWeaponId(): " + seat.getCurrentWeaponId()
            );
        }

        Weapon weapon = seat.getCurrentWeapon();
        logMessage(accountId, "doShoot,  weapon: " + weapon);

        ShootResult res = shootBaseEnemy(seat, weapon, stake, enemy, isBossRound, toService, countPlayers, lastTimeBossShot, timeInterval);

        if (res.isDestroyed()) {
            addKill(seat);
        }


        if (debug) {
            logMessage(accountId, "doShoot shootResult: " + res);
        }
        return res;
    }

    public GameConfig getConfig(Seat seat) {
        return (GameConfig) gameConfigProvider
                .getConfig(GameType.BG_SECTOR_X.getGameId(), seat.getPlayerInfo().getRoomId());
    }


    public GameConfig getConfig(long roomId) {
        return (GameConfig) gameConfigProvider.getConfig(GameType.BG_SECTOR_X.getGameId(), roomId);
    }

    @Override
    public IGameConfig getGameConfig(long roomId) {
        return getConfig(roomId);
    }

    @Override
    public SpawnConfig getSpawnConfig(long roomId) {
        return (SpawnConfig) spawnConfigProvider.getConfig(GameType.BG_SECTOR_X.getGameId(), roomId);
    }
}
