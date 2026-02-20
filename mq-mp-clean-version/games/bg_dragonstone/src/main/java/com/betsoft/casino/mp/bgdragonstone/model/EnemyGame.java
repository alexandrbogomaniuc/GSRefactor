package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.bgdragonstone.model.math.*;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.common.AbstractEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.WeaponDrop;
import com.betsoft.casino.mp.bgdragonstone.model.math.slot.MiniSlot;
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
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyRange.SPECTERS;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType.*;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {
    public static final String RAGE_EFFECT = "RAGE";
    public static final String WEAPON_DROPPED_COUNTER = "WEAPON_DROPPED";

    private final IGameConfigProvider gameConfigProvider;
    private final ISpawnConfigProvider spawnConfigProvider;

    public EnemyGame(Logger logger, IGameConfigService<? extends IGameConfigService<?>> gameConfigService,
                     IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(logger, GameType.BG_DRAGONSTONE, gameConfigService);
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
                                         ITransportObjectsFactoryService toService, int seatsNumber,
                                         boolean isMainShot) throws CommonException {

        GameConfig config = getConfig(seat);

        long accountId = seat.getAccountId();
        logMessage(accountId, "shootBaseEnemy started, weapon: {}, isMainShot: {}, mathVersion: {}  ", weapon,
                isMainShot, config.getMathVersion());

        int betLevel = getBetLevelWithCheck(seat);
        IShot actualShot = getShotWithCheck(seat);

        boolean paidSpecialShot = actualShot.isPaidSpecialShot();
        logMessage(accountId, "paidSpecialShot: {} seat.getBetLevel(): {}", paidSpecialShot, betLevel);

        String sessionId = seat.getPlayerInfo().getSessionId();
        BGDragonStoneTestStand testStand = getTestStand(accountId, sessionId);

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
        int idxForEnemy = 0;

        if (SPECTERS.contains(enemy.getEnemyType())) {
            double probability = MathData.getHitProbability(config, weaponTypeId, enemyType, 0);
            isKilled = RNG.rand() < probability;
            if (testStand.isNeedKill() || testStand.isDropTwoWeapons()) {
                isKilled = true;
                logMessage(accountId, "specter killed from testStand");
            }
            logMessage(accountId, "specter kill probability: {}, killed: {}", probability, isKilled);

            if (testStand.isNotNeedAnyWin()) {
                isKilled = false;
                logMessage(accountId, "teststand isNotNeedAnyWin isKilled is false");
            }

            if (isKilled) {
                enemy.setEnergy(0);
                isRage = true;
                testStand.countRage();
                int spiritPayout = 0;
                int size = 1;
                if (enemyType.equals(SPIRIT_SPECTER)) {
                    size = 2;
                    spiritPayout = MathData.getRandomSpiritPayouts(config);
                } else {
                    spiritPayout = MathData.getEnemyPayout(config, enemyType, weaponTypeId, 0);
                    size = enemyType.equals(LIGHTNING_SPECTER) ? 7 : 5;
                }

                ragePayouts = getRagePayoutsParts(spiritPayout, size);
                Integer realSum = ragePayouts.stream().reduce(0, Integer::sum);
                if (ragePayouts.isEmpty()) {
                    logMessage(accountId, "error spirit sum: " + realSum
                            + ", spiritPayout: " + spiritPayout + ", size: " + size);
                }
                logMessage(accountId, "specter kill awardedWeapons: {}, payout: {}, randomSpiritPayouts: {}",
                        awardedWeapons, payout, ragePayouts);
            }
        } else if (isBoss) {
            int randomBossPay = testStand.isNeedKill() ? config.getBoss().getDefeatThreshold()
                    : MathData.getRandomBossSmallPay(config);
            double hitProbabilityForBoss = testStand.isNeedKill() ? 1
                    : MathData.getHitSmallProbabilityForBoss(config, weaponTypeId, randomBossPay);
            long currentTotalWinOfEnemyX2 = 0;
            boolean needKill = false;
            if (!testStand.isNotNeedAnyWin()) {
                if (RNG.rand() < hitProbabilityForBoss) {
                    payout = stake.getWithMultiplier(randomBossPay * betLevel);
                    Map<Long, Double> damageToEnemies = seat.getDamageToEnemies();
                    Double oldDamage = damageToEnemies.get(enemy.getId());
                    double totalDamage = oldDamage == null ? randomBossPay : oldDamage + (randomBossPay);
                    damageToEnemies.put(enemy.getId(), totalDamage);
                    seat.addTotalBossPayout(payout.toCents());
                    needKill = totalDamage > config.getBoss().getDefeatThreshold();
                    logMessage(accountId, "damageToEnemies: {}, randomBossPay: {}, betLevel: {}, totalDamage: {}",
                            damageToEnemies, randomBossPay, betLevel, totalDamage);
                }

                if (needKill || testStand.isNeedKill()) {
                    currentTotalWinOfEnemyX2 = seat.getTotalBossPayout();
                    killAwardWin = Money.fromCents(currentTotalWinOfEnemyX2);
                    isKilled = true;
                }
            }

            if(payout.greaterThan(Money.ZERO)) {
                logMessage(accountId, "boss hitProbabilityForBoss: {}, randomBossPay: {}, " +
                                "payout: {}, killAwardWin: {}, currentTotalWinOfEnemyX2: {}," +
                                " seat.getTotalBossPayout(): {}, needKill: {}",
                        hitProbabilityForBoss,
                        randomBossPay,
                        payout.toDoubleCents(),
                        killAwardWin.toDoubleCents(),
                        currentTotalWinOfEnemyX2, seat.getTotalBossPayout(), needKill);
            }
        } else if (OGRE.equals(enemyType)) {
            int idxForOgre = enemy.getLives() == 1 ? 0 : 1;
            idxForEnemy = idxForOgre;
            boolean isRageMode = enemy.getLives() == 0;

            double hitBaseRageProbability = MathData.getHitProbability(config, weaponTypeId, enemyType, idxForOgre);

            isHit = RNG.rand() < hitBaseRageProbability;

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

            logMessage(accountId, "wpId: {}, hitProbability: {}, isHit: {}", weaponTypeId, hitBaseRageProbability, isHit);

            int enemyPayout = 0;
            if (isHit) {
                enemyPayout = isRageMode ? MathData.getRandomRagePayouts(config) :
                        MathData.getEnemyPayout(config, enemyType, weaponTypeId, idxForOgre);
            }

            chMult = MathData.getRandomMultForWeapon(config, weaponTypeId);

            logMessage(accountId, "ogre enemyPayout: {}, ragePayouts:{}, " +
                    "chMult(only base payout for ogre): {}, isRageMode: {} ", enemyPayout, ragePayouts, chMult, isRageMode);

            if (isHit) {
                // pay base payout without rage and ogr will be killed
                enemy.setEnergy(0);
                payout = stake.getWithMultiplier(enemyPayout * chMult * betLevel);
                isKilled = true;
            }
        } else {
            int idxData = enemyType.equals(CERBERUS) ? enemy.getLives() : 0;
            idxForEnemy = idxData;

            logMessage(accountId, "idxData for enemy " + idxData);

            double hitProbability = MathData.getHitProbability(config, weaponTypeId, enemyType, 0);
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


        if (!isBoss && isMainShot) {
            EnemyData enemyData = config.getEnemyData(enemyType, idxForEnemy);
            double probWeapon = 0;
            boolean wasRealReTrigger = seat.getAdditionalTempCounters(WEAPON_DROPPED_COUNTER) > 0;

            if (isSpecialWeapon) {
                probWeapon = wasRealReTrigger ? config.getPSWDropsReTriggerTwo().get(weaponTypeId) :
                        config.getPSWDropsReTriggerOne().get(weaponTypeId);
            } else {
                probWeapon = enemyData.getPSWDrop();
            }

            boolean needWeapon = RNG.rand() < probWeapon || testStand.isNeedNewWeapons();
            boolean needLevelUp = testStand.isNeedLevelUp() && (weaponTypeId == -1 || weaponTypeId == SpecialWeaponType.LevelUp.getId());

            logMessage(accountId, "needWeapon: {}, probWeapon: {},  needLevelUp: {}, idxForEnemy: {}, wasRealReTrigger: {}",
                    needWeapon, probWeapon, needLevelUp, idxForEnemy, wasRealReTrigger);

            if(testStand.isNotNeedAnyWin()){
                needWeapon = false;
                logMessage(accountId, "teststand isNotNeedAnyWin needWeapon is false");
            }

            if (needWeapon || needLevelUp) {
                WeaponDrop drop = getRandomWeaponDrop(config.getWeaponDrops(weaponTypeId));
                int weaponId = drop.getType();
                int newSpecialWeaponShots = drop.getAmount();
                if(needLevelUp){
                    weaponId = SpecialWeaponType.LevelUp.getId();
                    newSpecialWeaponShots = 20;
                }
                awardedWeapons.add(toService.createWeapon(weaponId, newSpecialWeaponShots));
                if(isSpecialWeapon && !wasRealReTrigger){
                    seat.addAdditionalTempCounter(WEAPON_DROPPED_COUNTER, 1);
                    logMessage(accountId, "appeared first weapon re-trigger, counters: {} ",
                            seat.getAdditionalTempCounters());
                }
            }
        }

        boolean stoneDropped = false;
        if (!isBoss) {
            double stoneProbability = DragonStone.getDropProbability(config, seatsNumber);
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

        if (!isBoss && !testStand.isNotNeedAnyWin() && isMainShot && weaponTypeId == -1) {
            EnemyData enemyData = config.getEnemyData(enemyType, idxForEnemy);
            MiniSlot slot = new MiniSlot(config.getSlot());
            double slotProbability = enemyData.getPSlotDrop();
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

    private BGDragonStoneTestStand getTestStand(long accountId, String sessionId) {
        if (sessionId != null) {
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(accountId, " testStand feature: {}", featureBySid);
            if (featureBySid != null) {
                return new BGDragonStoneTestStand(featureBySid);
            }
        }
        return new BGDragonStoneTestStand();
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
                               ITransportObjectsFactoryService toService, int seatsNumber, boolean isMainShot) throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        if (debug) {
            logMessage(seat.getAccountId(), "doShoot, sessionId: " + sessionId
                    + " seat.getCurrentWeaponId(): " + seat.getCurrentWeaponId()
            );
        }

        Weapon weapon = seat.getCurrentWeapon();
        logMessage(accountId, "doShoot,  weapon: " + weapon);

        ShootResult res = shootBaseEnemy(seat, weapon, stake, enemy, toService, seatsNumber, isMainShot);

        if (res.isDestroyed()) {
            addKill(seat);
        }

        if (debug) {
            logMessage(accountId, "doShoot shootResult: " + res);
        }
        return res;
    }

    private List<ISpinResult> doSpins(BGDragonStoneTestStand testStand, MiniSlot slot) {
        int testStandCombination = testStand.getSlotCombination();
        if (testStandCombination > 0) {
            return slot.doSpinsWithTestStand(testStandCombination);
        } else {
            return slot.doSpins();
        }
    }

    public GameConfig getConfig(Seat seat) {
        return (GameConfig) gameConfigProvider
                .getConfig(GameType.BG_DRAGONSTONE.getGameId(), seat.getPlayerInfo().getRoomId());
    }

    public GameConfig getConfig(long roomId) {
        return (GameConfig) gameConfigProvider.getConfig(GameType.BG_DRAGONSTONE.getGameId(), roomId);
    }

    @Override
    public IGameConfig getGameConfig(long roomId) {
        return getConfig(roomId);
    }

    public SpawnConfig getSpawnConfig(long roomId) {
        return (SpawnConfig) spawnConfigProvider.getConfig(GameType.BG_DRAGONSTONE.getGameId(), roomId);
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
