package com.betsoft.casino.mp.dragonstone.model;

import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.common.AbstractEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.dragonstone.model.math.*;
import com.betsoft.casino.mp.dragonstone.model.math.config.BossParams;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.WeaponDrop;
import com.betsoft.casino.mp.dragonstone.model.math.slot.MiniSlot;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.service.IGameConfigProvider;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ISpawnConfigProvider;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.betsoft.casino.mp.dragonstone.model.math.EnemyRange.SPECTERS;
import static com.betsoft.casino.mp.dragonstone.model.math.EnemyType.*;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {
    public static final String RAGE_EFFECT = "RAGE";

    private final IGameConfigProvider gameConfigProvider;
    private final ISpawnConfigProvider spawnConfigProvider;

    public EnemyGame(Logger logger, IGameConfigService<? extends IGameConfigService<?>> gameConfigService,
                     IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(logger, GameType.DRAGONSTONE, gameConfigService);
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

    protected ShootResult shootBaseEnemy(Seat seat, Weapon weapon, Money stake, Enemy enemy,
                                         ITransportObjectsFactoryService toService, Integer numberOfPlayers) throws CommonException {

        GameConfig config = getConfig(seat);

        long accountId = seat.getAccountId();
        logMessage(accountId, "shootBaseEnemy started, weapon: {}", weapon);

        int betLevel = getBetLevelWithCheck(seat);
        IShot actualShot = getShotWithCheck(seat);

        boolean paidSpecialShot = actualShot.isPaidSpecialShot();
        logMessage(accountId, "paidSpecialShot: {} seat.getBetLevel(): {}", paidSpecialShot, betLevel);

        String sessionId = seat.getPlayerInfo().getSessionId();
        DragonStoneTestStand testStand = getTestStand(accountId, sessionId);

        boolean isSpecialWeapon = (weapon != null);
        boolean isBoss = enemy.isBoss();
        boolean isPaidShot = !isSpecialWeapon;

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        Money payout = Money.ZERO;
        boolean isKilled = false;
        boolean isHit = false;
        boolean isRage = false;

        EnemyType enemyType = enemy.getEnemyType();

        int chMult = 1;
        Money killAwardWin = Money.ZERO;
        List<Integer> ragePayouts = new ArrayList<>();

        if (SPECTERS.contains(enemy.getEnemyType())) {
            int size;
            EnemyData enemyData = config.getEnemies().get(enemyType).get(0);
            int spiritPayout = RNG.nextInt(enemyData.getMinPay(), enemyData.getMaxPay() + 1);

            if (enemyType.equals(SPIRIT_SPECTER)) {
                size = 2;
            } else {
                size = enemyType.equals(LIGHTNING_SPECTER) ? 7 : 5;
            }

            List<Integer> ragePayoutsInternal = getRagePayoutsParts(spiritPayout, size);
            Integer pay = ragePayoutsInternal.stream().reduce(0, Integer::sum);
            double rtp = RNG.randExponential(enemyData.getMinRtp(), enemyData.getMaxRtp());
            double weaponProbabilityMultiplier = enemyData.getWeaponProbabilityMultiplier(weaponTypeId);
            double hitProbability = (rtp / pay) * weaponProbabilityMultiplier;

            isKilled = RNG.rand() < hitProbability;

            if (testStand.isNeedKill() || testStand.isDropTwoWeapons()) {
                isKilled = true;
            }

            if (testStand.isNotNeedAnyWin()) {
                isKilled = false;
            }

            if (isKilled) {
                enemy.setEnergy(0);
                isRage = true;
                testStand.countRage();

                ragePayouts = ragePayoutsInternal;
            }

            logMessage(accountId, "EnemyGame enemyType: {}, hit: {}, rtp: {}, weaponId: {}, weaponProbabilityMultiplier:{}, hitProbability: {}, pay: {}, ragePayouts: {}",
                    enemyType.getName(), isHit, rtp, weaponTypeId, weaponProbabilityMultiplier, hitProbability, pay, ragePayouts.stream().toString());
        } else if (isBoss) {
            double hitSmallProbabilityForBoss = MathData.getHitSmallProbabilityForBoss(config, weaponTypeId);
            double hitKillProbabilityForBoss = MathData.getHitKillProbabilityForBoss(config, weaponTypeId);
            int randomBossSmallPay = 0;
            if (!testStand.isNotNeedAnyWin()) {
                if (RNG.rand() < hitSmallProbabilityForBoss) {
                    randomBossSmallPay = MathData.getRandomBossSmallPay(config);
                    payout = stake.getWithMultiplier(randomBossSmallPay * betLevel);
                }

                if (RNG.rand() < hitKillProbabilityForBoss || testStand.isNeedKill()) {
                    killAwardWin = stake.getWithMultiplier(config.getBoss().getKilledPay() * betLevel);
                    isKilled = true;
                }
            }
            logMessage(accountId, "boss hitSmallProbabilityForBoss: {}, hitKillProbabilityForBoss: {}, " +
                            "payout: {}, killAwardWin: {}, randomBossSmallPay",
                    hitSmallProbabilityForBoss, hitKillProbabilityForBoss, payout.toDoubleCents(),
                    killAwardWin.toDoubleCents(), randomBossSmallPay);
        } else if (OGRE.equals(enemyType)) {
            int idxForOgre = enemy.getLives() == 1 ? 0 : 1;
            boolean isRageMode = enemy.getLives() == 0;

            EnemyData enemyData = config.getEnemies().get(enemyType).get(idxForOgre);
            int pay = isRageMode ? MathData.getRandomRagePayouts(config) :
                    RNG.nextInt(enemyData.getMinPay(), enemyData.getMaxPay() + 1);

            double rtp = RNG.randExponential(enemyData.getMinRtp(), enemyData.getMaxRtp());
            double weaponProbabilityMultiplier = enemyData.getWeaponProbabilityMultiplier(weaponTypeId);
            double hitProbability = (rtp / pay) * weaponProbabilityMultiplier;

            isHit = isHit(hitProbability, testStand, accountId);
            if (isHit) {
                chMult = MathData.getRandomMultForWeapon(config, weaponTypeId);

                // pay base payout without rage and ogr will be killed
                enemy.setEnergy(0);
                payout = stake.getWithMultiplier(pay * chMult * betLevel);
                isKilled = true;
            }

            logMessage(accountId, "EnemyGame enemyType: {}, hit: {}, rtp: {}, weaponId: {}, weaponProbabilityMultiplier: {}, hitProbability: {}, pay: {}", enemyType.getName(), isHit, rtp, weaponTypeId, weaponProbabilityMultiplier, hitProbability, pay);

        } else if (CERBERUS.equals(enemyType) || Arrays.asList(DARK_KNIGHT, PURPLE_WIZARD, BLUE_WIZARD, RED_WIZARD).contains(enemyType)) {
            int idxData = CERBERUS.equals(enemyType) ? enemy.getLives() : 0;

            EnemyData enemyData = config.getEnemies().get(enemyType).get(idxData);

            int pay = RNG.nextInt(
                    enemyData.getMinPay(),
                    enemyData.getMaxPay() + 1);

            double rtp = RNG.randExponential(enemyData.getMinRtp(), enemyData.getMaxRtp());
            double weaponProbabilityMultiplier = enemyData.getWeaponProbabilityMultiplier(weaponTypeId);
            double hitProbability = (rtp / pay) * weaponProbabilityMultiplier;

            isHit = isHit(hitProbability, testStand, accountId);

            if (isHit) {
                isKilled = true;
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                chMult = MathData.getRandomMultForWeapon(config, weaponTypeId);
                boolean isCriticalHit = chMult != 1;
                logMessage(accountId, "isCriticalHit: {} chMult: {}", isCriticalHit, chMult);

                enemy.setEnergy(0);
                payout = stake.getWithMultiplier(pay * chMult * betLevel);
                if (hitProbability > 1) {
                    double diff = hitProbability - 1;
                    killAwardWin = stake.getWithMultiplier(pay * diff * chMult * betLevel);
                    logMessage(accountId, "hit prob more 1, compensate to killAward, diff: {}, killAwardWin: {}, enemyPayout: {}",
                            diff, killAwardWin, pay);
                }
            }

            logMessage(accountId, "EnemyGame enemyType: {}, hit: {}, rtp: {}, weaponId: {}, weaponProbabilityMultiplier: {}, hitProbability: {}, pay: {}", enemyType.getName(), isHit, rtp, weaponTypeId, weaponProbabilityMultiplier, hitProbability, pay);
        } else {
            int idxData = 0;
            logMessage(accountId, "idxData for enemy " + idxData);

            double hitProbability = MathData.getHitProbability(config, weaponTypeId, enemyType, 0);
            isHit = isHit(hitProbability, testStand, accountId);

            logMessage(accountId, "wpId: {}, hitProbability: {}, isHit: {}", weaponTypeId, hitProbability, isHit);

            if (isHit) {
                isKilled = true;
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                chMult = MathData.getRandomMultForWeapon(config, weaponTypeId);
                boolean isCriticalHit = chMult != 1;
                logMessage(accountId, "isCriticalHit: {} chMult: {}", isCriticalHit, chMult);
                int enemyPayout = MathData.getEnemyPayout(config, enemyType, weaponTypeId, 0);
                enemy.setEnergy(0);
                payout = stake.getWithMultiplier(enemyPayout * chMult * betLevel);
                if (hitProbability > 1) {
                    double diff = hitProbability - 1;
                    killAwardWin = stake.getWithMultiplier(enemyPayout * diff * chMult * betLevel);
                    logMessage(accountId, "hit prob more 1, compensate to killAward, diff: {}, killAwardWin: {}, enemyPayout: {}",
                            diff, killAwardWin, enemyPayout);
                }
            }
        }

        if (!isBoss) {
            double avgDropPrice = MathData.calculateAverageDropPrice(config);
            Double rtpForWeapon = MathData.getRtpForDropWeapon(config, enemyType, weaponTypeId, 0);
            Double averageDamageForWeapon = MathData.getAverageDamageForWeapon(config, weaponTypeId);
            double probWeapon = rtpForWeapon / avgDropPrice / averageDamageForWeapon;
            boolean needWeapon = RNG.rand() < probWeapon || testStand.isNeedNewWeapons();
            logMessage(accountId,
                    "needWeapon: {}, avgDropPrice: {}, rtpForWeapon: {}, probWeapon: {}, " +
                            "averageDamageForWeapon: {}",
                    needWeapon, avgDropPrice, rtpForWeapon, probWeapon, averageDamageForWeapon);

            if (testStand.isNotNeedAnyWin()) {
                needWeapon = false;
                logMessage(accountId, "teststand isNotNeedAnyWin needWeapon is false");
            }

            if (needWeapon) {
                WeaponDrop drop = getRandomWeaponDrop(config.getWeaponDrops());
                int weaponId = drop.getType();
                int newSpecialWeaponShots = drop.getAmount();
                awardedWeapons.add(toService.createWeapon(weaponId, newSpecialWeaponShots));
            }
        }

        boolean stoneDropped = false;
        if (!isBoss) {
            double stoneProbability = DragonStone.getDropProbability(config, weaponTypeId, betLevel);
            stoneDropped = RNG.rand() < stoneProbability || testStand.isNeedDragonStoneFragmentDrop();
            logMessage(accountId, "DragonStone drop chance: {}, dropped: {}", stoneProbability, stoneDropped);
            testStand.countFragment();
        }

        logMessage(accountId, "win: {}", payout);

        ShootResult shootResult = new ShootResult(isPaidShot ? stake : Money.ZERO, payout, stoneDropped, isKilled, enemy);
        shootResult.setChMult(chMult);
        shootResult.setRage(isRage);

        if (isRage) {
            shootResult.setGems(ragePayouts);
            shootResult.addEffect(RAGE_EFFECT);
        }

        if (!isBoss && !testStand.isNotNeedAnyWin()) {
            MiniSlot slot = new MiniSlot(config.getSlot());
            double slotProbability = slot.getTriggerProbability(weaponTypeId)
                    / MathData.getAverageDamageForWeapon(config, weaponTypeId);
            logMessage(accountId, "slot trigger probability: {}", slotProbability);
            if (RNG.rand() < slotProbability || testStand.isNeedTriggerSlot()) {
                shootResult.setSpinResults(doSpins(testStand, slot));
                testStand.countSlot();
            }
        }

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
        logMessage(accountId, "shootResult: {}", shootResult);

        return shootResult;
    }

    private boolean isHit(double hitProbability, DragonStoneTestStand testStand, long accountId) {
        boolean isHit;
        isHit = RNG.rand() < hitProbability;

        if (testStand.isNeedHit()) {
            logMessage(accountId, "Hit from TestStand");
            isHit = true;
        }

        if (testStand.isNeedKill()) {
            isHit = true;
            logMessage(accountId, "teststand need kill");
        }

        if (testStand.isRage()) {
            isHit = true;
            logMessage(accountId, "teststand rage");
        }

        if (testStand.isNotNeedAnyWin()) {
            isHit = false;
            logMessage(accountId, "teststand isNotNeedAnyWin isHit is false");
        }
        return isHit;
    }

    private WeaponDrop getRandomWeaponDrop(List<WeaponDrop> weaponDrops) {
        double[] weights = weaponDrops.stream().mapToDouble(WeaponDrop::getWeight).toArray();
        double sum = Arrays.stream(weights).sum();
        if (sum != 1) {
            weights = Arrays.stream(weights).map(v -> v / sum).toArray();
        }
        return weaponDrops.get(GameTools.getIndexFromDoubleProb(weights));
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

    private DragonStoneTestStand getTestStand(long accountId, String sessionId) {
        if (sessionId != null) {
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(accountId, " testStand feature: {}", featureBySid);
            if (featureBySid != null) {
                return new DragonStoneTestStand(featureBySid);
            }
        }
        return new DragonStoneTestStand();
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

    public ShootResult doRageDamage(Enemy enemy, Seat seat, int damage, Money stake, int betLevel) {
        long accountId = seat.getAccountId();
        logMessage(accountId, "enemy {} damaged by rage, hp: {}, damage: {}", enemy.getId(), damage);
        ShootResult result;
        Money payout = stake.getWithMultiplier(damage * betLevel);
        result = new ShootResult(Money.ZERO, payout, false, true, enemy);
        // result.setDamage(enemy.getEnergy());
        enemy.setEnergy(0);
        addKill(seat);
        result.addEffect(RAGE_EFFECT);
        logMessage(accountId, "rageShootResult: {}", result);
        return result;
    }

    private void addKill(Seat seat) {
        seat.incCountEnemiesKilled();
        IPlayerStats roundStats = seat.getPlayerInfo().getRoundStats();
        Map<Integer, Long> roundStatsKills = roundStats.getKills();
        roundStatsKills.put(0, roundStatsKills.isEmpty() ? 1 : roundStatsKills.get(0) + 1);
    }

    public ShootResult doShoot(Enemy enemy, Seat seat, Money stake,
                               ITransportObjectsFactoryService toService, Integer numberOfPlayers) throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        if (debug) {
            logMessage(seat.getAccountId(), "doShoot, sessionId: " + sessionId
                    + " seat.getCurrentWeaponId(): " + seat.getCurrentWeaponId()
            );
        }

        Weapon weapon = seat.getCurrentWeapon();
        logMessage(accountId, "doShoot,  weapon: " + weapon);

        ShootResult res = shootBaseEnemy(seat, weapon, stake, enemy, toService, numberOfPlayers);

        if (res.isDestroyed()) {
            addKill(seat);
        }

        if (debug) {
            logMessage(accountId, "doShoot shootResult: " + res);
        }
        return res;
    }

    private List<ISpinResult> doSpins(DragonStoneTestStand testStand, MiniSlot slot) {
        int testStandCombination = testStand.getSlotCombination();
        if (testStandCombination > 0) {
            return slot.doSpinsWithTestStand(testStandCombination);
        } else {
            return slot.doSpins();
        }
    }

    public GameConfig getConfig(Seat seat) {
        return (GameConfig) gameConfigProvider
                .getConfig(GameType.DRAGONSTONE.getGameId(), seat.getPlayerInfo().getRoomId());
    }

    public GameConfig getConfig(long roomId) {
        return (GameConfig) gameConfigProvider.getConfig(GameType.DRAGONSTONE.getGameId(), roomId);
    }

    @Override
    public IGameConfig getGameConfig(long roomId) {
        return getConfig(roomId);
    }

    public SpawnConfig getSpawnConfig(long roomId) {
        return (SpawnConfig) spawnConfigProvider.getConfig(GameType.DRAGONSTONE.getGameId(), roomId);
    }

    public static List<Integer> getRagePayoutsParts(int payout, int size) {
        int sum = 0;
        int part = payout / size;
        int cnt = 1000;
        List<Integer> ragePayouts = new ArrayList<>();

        while (cnt-- > 0) {
            int realPart = RNG.nextInt(part - 1, part + 1);
            boolean needAddToPrev = false;
            if (sum + realPart > payout) {
                realPart = payout - sum;
                needAddToPrev = true;
            }
            sum += realPart;
            if (needAddToPrev) {
                int lastIdx = ragePayouts.size() - 1;
                Integer old = ragePayouts.get(lastIdx);
                ragePayouts.set(lastIdx, old + realPart);
            } else {
                ragePayouts.add(realPart);
            }
            if (payout == sum) {
                break;
            }
        }

        Integer realSum = ragePayouts.stream().reduce(0, Integer::sum);
        if (realSum != payout) {
            ragePayouts.clear();
        }
        return ragePayouts;
    }

}


