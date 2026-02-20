package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.sectorx.model.math.BossType;
import com.betsoft.casino.mp.sectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.sectorx.model.math.MathData;
import com.betsoft.casino.mp.sectorx.model.math.config.*;
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
    public static final double HUGE_ENEMY_RTP_START = 0.92;
    public static final double HUGE_ENEMY_RTP_END = 0.98;
    public static final double SPECIAL_ITEM_RTP_START = 0.82;
    public static final double SPECIAL_ITEM_RTP_END = 0.92;
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

    protected ShootResult shootBaseEnemy(Seat seat, Weapon weapon, Money stake, Enemy enemy, boolean isBossRound, int expectedPayoutFromFreezeBossAndEnemies, Integer numberOfPlayers) throws CommonException {
        GameConfig config = getConfig(seat);
        long accountId = seat.getAccountId();

        logMessage(accountId, "shootBaseEnemy: started, weapon: {}", weapon);

        // Bet-level is the multiplier of the cost per shot
        int betLevel = getBetLevelWithCheck(seat);
        IShot actualShot = getShotWithCheck(seat);

        boolean paidSpecialShot = actualShot.isPaidSpecialShot();
        logMessage(accountId, "shootBaseEnemy: paidSpecialShot: {} seat.getBetLevel(): {}", paidSpecialShot, betLevel);

        String sessionId = seat.getPlayerInfo().getSessionId();
        SectorXTestStand testStand = getTestStand(accountId, sessionId);

        boolean isSpecialWeapon = (weapon != null);

        boolean isPaidShot = !isSpecialWeapon;

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        List<Integer> shotGems = initializeShotGems();
        Money totalGemPayout = Money.ZERO;

        Money payout = Money.ZERO;
        int chMult = 1;
        boolean isKilled = false;
        boolean isHit;

        EnemyType enemyType = enemy.getEnemyType();
        boolean isRegularEnemy = EnemyRange.BASE_ENEMIES.contains(enemyType) && !EnemyRange.HUGE_PAY_ENEMIES.contains(enemyType);
        boolean isHugeEnemy = EnemyRange.HUGE_PAY_ENEMIES.contains(enemyType);
        boolean isBoss = enemy.isBoss();

        logMessage(accountId, "shootBaseEnemy: isRegularEnemy: {}, isHugeEnemy: {}, isBoss: {}",
                isRegularEnemy, isHugeEnemy, isBoss);

        Money killAwardWin = Money.ZERO;

        if (isBoss) {

            double rtp;
            double percentRangeMode = 10;
            double rageThreshold = (enemy.getFullEnergy() * percentRangeMode) / 100;
            boolean isFixedPay = enemy.getEnergy() <= rageThreshold;
            boolean isRageMode = isFixedPay;

            BossType bossType = BossType.getBySkinId(enemy.getSkin());
            BossParams bossParams = config.getBosses().get(bossType.getSkinId());

            double pay;
            if (testStand.isNeedKill()) {
                pay = (int) enemy.getFullEnergy();
            } else if (isRageMode) {
                pay = bossParams.getFixedPay();
            } else {
                pay = bossParams.getMinPay()
                        + (bossParams.getMaxPay()
                        - bossParams.getMinPay()) * Math.pow(RNG.randUniform(), 3);
            }

            if (isRageMode) {
                rtp = RNG.randExponential(bossParams.getMinRTPFixedPay(), bossParams.getMaxRTPFixedPay());
            } else if (pay <= bossParams.getLowPayThreshold()) {
                rtp = RNG.randExponential(bossParams.getMinRTPSmallPay(), bossParams.getMaxRTPSmallPay());
            } else if (pay > bossParams.getLowPayThreshold()) {
                rtp = RNG.randExponential(bossParams.getMinRTPBigPay(), bossParams.getMaxRTPBigPay());
            } else {
                throw new CommonException("RTP for Boss can not be calculated: " + bossType);
            }
            double hitProbabilityForBoss = testStand.isNeedKill() ? 1 : rtp / pay;

            boolean needKill = false;
            if (!testStand.isNotNeedAnyWin()) {
                if (RNG.rand() < hitProbabilityForBoss) {
                    payout = stake.getWithMultiplier(pay * betLevel);
                    seat.addTotalBossPayout(payout.toCents());

                    double damage = pay / bossParams.getDamageDivider() / numberOfPlayers;
                    double newEnergy = enemy.getEnergy() - damage;
                    if (newEnergy < 0 && !isFixedPay) {
                        newEnergy = 0;
                    }
                    enemy.setEnergy(newEnergy);
                    needKill = isFixedPay;
                    logMessage(accountId, "shootBaseEnemy: needKill: {}, partialPay: {}, betLevel: {}, newEnergy: {}, isFixedPay: {}",
                            needKill, pay, betLevel, newEnergy, isFixedPay);
                }

                isKilled = needKill || testStand.isNeedKill();
            }

            if (payout.greaterThan(Money.ZERO)) {
                logMessage(accountId, "shootBaseEnemy: Payout Room {} | EnemyId: {}, EnemyType: {}, Hit: {}, HitProbability: {}, RTP: {}, Stake: {} , Pay: {},expectedPayoutFromFreezeBossAndEnemies: {}, Payout: {}, BetLevel: {} Killed: {}, Energy: {}, IsRageMode: {}, numberOfPlayers: {}, DamageDivider: {} ",
                        seat.getPlayerInfo().getRoomId(), enemy.getId(), enemyType.getName(), payout.greaterThan(Money.ZERO), hitProbabilityForBoss, rtp, stake.toCents(), pay, expectedPayoutFromFreezeBossAndEnemies, payout, betLevel, isKilled, enemy.getEnergy(), isRageMode, numberOfPlayers, bossParams.getDamageDivider());

            }

        } else {

            int idxData = 0;
            logMessage(accountId, "shootBaseEnemy: idxData for enemy " + idxData);

            double hitProbability;
            int pay;
            double rtp;

            if (EnemyRange.SPECIAL_ITEMS.contains(enemyType)) {

                EnemySpecialItem enemySpecialItem = (EnemySpecialItem) enemy;

                logMessage(accountId, "shootBaseEnemy: enemyType is SPECIAL_ITEM: {}", enemySpecialItem);

                pay = enemySpecialItem.getTotalPayout();
                rtp = RNG.randExponential(SPECIAL_ITEM_RTP_START, SPECIAL_ITEM_RTP_END);
                hitProbability = rtp / (pay + expectedPayoutFromFreezeBossAndEnemies) / enemySpecialItem.getCurrentMultiplier();

            } else if (isRegularEnemy) {

                logMessage(accountId, "shootBaseEnemy: enemyType is isRegularEnemy: {}", enemy);

                chMult = getCriticalMultiplier(accountId, enemyType, config);
                pay = MathData.getEnemyPayout(config, enemyType, idxData) * chMult;
                rtp = config.getGameRTP() / 100;

                hitProbability = rtp / (pay + expectedPayoutFromFreezeBossAndEnemies);

            } else if (isHugeEnemy) {

                logMessage(accountId, "shootBaseEnemy: enemyType is isHugeEnemy: {}", enemy);

                pay = RNG.nextInt(
                        MathData.getMinEnemyPayout(config, enemyType, idxData),
                        MathData.getMaxEnemyPayout(config, enemyType, idxData) + 1
                );

                rtp = RNG.randExponential(HUGE_ENEMY_RTP_START, HUGE_ENEMY_RTP_END);
                hitProbability = rtp / (pay + expectedPayoutFromFreezeBossAndEnemies);

            } else {
                throw new CommonException("Unknown enemy type: " + enemyType);
            }

            logMessage(accountId, "shootBaseEnemy: pay:{}, rtp:{}, hitProbability:{}", pay, rtp, hitProbability);

            isHit = isHit(hitProbability, testStand, accountId, weaponTypeId);

            logMessage(accountId, "shootBaseEnemy: isHit:{}", isHit);

            if (isHit) {

                isKilled = true;
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                enemy.setEnergy(0);

                payout = stake.getWithMultiplier((double) pay * betLevel);

                if (hitProbability > 1) {
                    double diff = hitProbability - 1;
                    killAwardWin = stake.getWithMultiplier(pay * diff * betLevel);
                }

                if (payout.greaterThan(Money.ZERO) || expectedPayoutFromFreezeBossAndEnemies > 0) {

                    logMessage(accountId, "shootBaseEnemy: Payout RoomId {} | EnemyId: {}, EnemyType: {}, Hit: {}, HitProbability: {}, RTP: {}, Stake: {} Pay: {}, expectedPayoutFromFreezeBossAndEnemies: {}, Payout: {}, BetLevel: {} Killed: {}, Energy: {}, IsRageMode: {}, CriticalMultiplier: {}, SpecialItemCurrentMultiplier: {}, totalPayoutFromEspecialEnemy: {}, additionalKillAwardFromSpecialEnemyWithMultiplier: {}, additionalKillAwardFromSpecialEnemy:{}",
                            seat.getPlayerInfo().getRoomId(), enemy.getId(), enemyType.getName(), payout.greaterThan(Money.ZERO), hitProbability, rtp, stake.toDoubleCents(), pay, expectedPayoutFromFreezeBossAndEnemies, payout, betLevel, isKilled, enemy.getEnergy(), false, chMult);
                }
            }
        }

        boolean bossShouldBeAppeared = !isBossRound && testStand.isNeedSpawnBoss();

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

        logMessage(accountId, "shootBaseEnemy: shootResult: {}", shootResult);

        return shootResult;
    }

    private int getCriticalMultiplier(long accountId, EnemyType enemyType, GameConfig config) {
        int chMult;
        CriticalMultiplierType criticalMultiplierType;

        if (EnemyRange.LOW_PAY_ENEMIES.contains(enemyType)) {
            criticalMultiplierType = CriticalMultiplierType.LP;
        } else if (EnemyRange.MID_PAY_ENEMIES.contains(enemyType)) {
            criticalMultiplierType = CriticalMultiplierType.MP;
        } else {
            criticalMultiplierType = CriticalMultiplierType.HP;
        }

        List<Double> multipliers = config.getCriticalMultiplier().get(criticalMultiplierType);

        chMult = GameTools.getIndexFromDoubleProb(multipliers.stream().mapToDouble(Double::doubleValue).toArray()) + 1;

        logMessage(accountId, "getCriticalMultiplier: chMult:{}, multipliers:{}, criticalMultiplierType:{}, enemyType:{}",
                chMult, multipliers, criticalMultiplierType, enemyType);


        return chMult;
    }

    private boolean isHit(double hitProbability, SectorXTestStand testStand, long accountId, int weaponTypeId) {
        boolean isHit;
        isHit = RNG.rand() < hitProbability;

        if (testStand.isNeedHit()) {
            logMessage(accountId, "Hit from TestStand");
            isHit = true;
        }

        if (testStand.isNeedKill()) {
            isHit = true;
            logMessage(accountId, "testStand need kill");
        }

        if (testStand.isNeedCriticalHit() && weaponTypeId != -1) {
            isHit = true;
            logMessage(accountId, "testStand need CH");
        }

        if (testStand.isNotNeedAnyWin()) {
            isHit = false;
            logMessage(accountId, "testStand isNotNeedAnyWin isHit is false");
        }
        return isHit;
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

    private SectorXTestStand getTestStand(long accountId, String sessionId) {
        if (sessionId != null) {
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(accountId, " testStand feature: {}", featureBySid);
            if (featureBySid != null) {
                return new SectorXTestStand(featureBySid);
            }
        }
        return new SectorXTestStand();
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
                               ITransportObjectsFactoryService toService, boolean isMainShot, int expectedPayoutFromFreezeBossAndEnemies, Integer numberOfPlayers) throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        if (debug) {
            logMessage(accountId, "doShoot: sessionId: " + sessionId
                    + " seat.getCurrentWeaponId(): " + seat.getCurrentWeaponId());
        }

        Weapon weapon = seat.getCurrentWeapon();
        logMessage(accountId, "doShoot:  weapon: " + weapon);

        ShootResult shootResult =
                shootBaseEnemy(seat, weapon, stake, enemy, isBossRound, expectedPayoutFromFreezeBossAndEnemies, numberOfPlayers);

        if (shootResult.isDestroyed()) {
            addKill(seat);
        }

        if (debug) {
            logMessage(accountId, "doShoot: shootResult: " + shootResult);
        }

        return shootResult;
    }

    public GameConfig getConfig(Seat seat) {
        return (GameConfig) gameConfigProvider
                .getConfig(GameType.SECTOR_X.getGameId(), seat.getPlayerInfo().getRoomId());
    }

    public GameConfig getConfig(long roomId) {
        return (GameConfig) gameConfigProvider.getConfig(GameType.SECTOR_X.getGameId(), roomId);
    }

    @Override
    public IGameConfig getGameConfig(long roomId) {
        return getConfig(roomId);
    }

    @Override
    public SpawnConfig getSpawnConfig(long roomId) {
        return (SpawnConfig) spawnConfigProvider.getConfig(GameType.SECTOR_X.getGameId(), roomId);
    }
}
