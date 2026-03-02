package com.betsoft.casino.mp.clashofthegods.model;

import com.betsoft.casino.mp.clashofthegods.model.math.*;
import com.betsoft.casino.mp.clashofthegods.model.math.enemies.IEnemyData;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.abs.casino.common.util.Triple;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static com.betsoft.casino.mp.model.SpecialWeaponType.*;
import static com.betsoft.casino.teststand.TeststandConst.*;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {

    public EnemyGame(Logger logger, IGameConfigService gameConfigService) {
        super(logger, GameType.CLASH_OF_THE_GODS, gameConfigService);
    }


    @Override
    public List<EnemyType> getBaseEnemyTypes() {
        return EnemyRange.BaseEnemies.getEnemies();
    }

    @Override
    protected ShootResult shootBaseEnemy(Seat seat, Weapon weapon, Money stake, Enemy enemy,
                                         boolean isBot, boolean isBossRound, boolean isNearLandMine,
                                         double totalDamageMultiplier, ITransportObjectsFactoryService toService)
            throws CommonException {

        long accountId = seat.getAccountId();
        logMessage(accountId, " shootBaseEnemy started, weapon:  " + weapon);
        int betLevel = seat.getBetLevel();
        IShot actualShot = seat.getActualShot();

        if (!MathData.getPossibleBetLevels().contains(betLevel)) {
            throw new CommonException("error bet level");
        }

        if (actualShot == null) {
            throw new CommonException("actual shot is null");
        }

        boolean paidSpecialShot = actualShot.isPaidSpecialShot();
        logMessage(accountId, " paidSpecialShot:  " + paidSpecialShot + " seat.getBetLevel(): " + betLevel);

        String sessionId = seat.getPlayerInfo().getSessionId();
        TestStandFeature featureBySid = null;
        if (sessionId != null) {
            featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(seat.getAccountId(), " teststand feature : " + featureBySid);
        }

        boolean isSpecialWeapon = (weapon != null);
        boolean isHVenemy = enemy.getEnemyClass().getEnemyType().isHVenemy();
        boolean isBoss = enemy.isBoss();
        boolean isPaidShot = !isSpecialWeapon;

        boolean needKillEnemyAndTryGetHVEnemy = featureBySid != null && featureBySid.getId() == 5;
        boolean needKillEnemyAndTryBoss = featureBySid != null && featureBySid.getId() == 9;
        boolean teststandNeedKillEnemy = featureBySid != null && (featureBySid.getId() == 6 || featureBySid.getId() == 7);
        boolean teststandNeedKill = teststandNeedKillEnemy || needKillEnemyAndTryBoss || needKillEnemyAndTryGetHVEnemy;

        boolean needKillEnemyAndGetBoss = featureBySid != null && (featureBySid.getId() == 9);

        boolean teststandDoubleSW = featureBySid != null && (featureBySid.getId() == 53);
        boolean teststandPistolNoWin = featureBySid != null && (featureBySid.getId() == 55);
        boolean isGuaranteedHit = featureBySid != null && (featureBySid.getId() == 11);

        boolean teststandNeedSWLaser = featureBySid != null && (featureBySid.getId() == 33);
        boolean teststandNeedSWArtilleryStrike = featureBySid != null && (featureBySid.getId() == 37);
        boolean teststandNeedSWLightning = featureBySid != null && (featureBySid.getId() == 58);
        boolean teststandNeedSWNapalm = featureBySid != null && (featureBySid.getId() == 59);
        boolean teststandNeedSWNuke = featureBySid != null && (featureBySid.getId() == 60);
        boolean teststandNeedSW_MW = featureBySid != null && (featureBySid.getId() == 61);
        boolean teststandNeed2SW_MW = featureBySid != null && (featureBySid.getId() == 62);
        boolean teststandNeedRandomWeapon= featureBySid != null && (featureBySid.getId() == FEATURE_RANDOM_WEAPON);
        boolean teststandNeedCH = featureBySid != null && (featureBySid.getId() == FEATURE_NEED_CH);
        boolean needMoneyWheel = featureBySid != null && (featureBySid.getId() == 52 || teststandNeedSW_MW || teststandNeed2SW_MW);

        boolean teststandNeedNewWeapons = featureBySid != null && (
                featureBySid.getId() == 33
                        || featureBySid.getId() == 37
                        || (featureBySid.getId() >= 58 && featureBySid.getId() <= 62)
                        || teststandNeedRandomWeapon
        );

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        Money payout = Money.ZERO;
        boolean bossShouldBeAppeared;
        boolean isKilled;
        String weaponType = null;
        int shots = 0;
        int newFreeShots = 0;
        boolean isHit;
        long damage = 0;
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        int wpId = isSpecialWeapon ? weapon.getType().getId() : -1;
        IEnemyData enemyData = MathData.getEnemyData(enemyType.getId());
        double currentEnergy = enemy.getEnergy();
        int fullEnergy = Double.valueOf(enemy.getFullEnergy()).intValue();

        int[] levels = enemyData.getLevels();
        int levelId = 0;
        if (!isBoss && levels != null) {
            for (int i = 0; i < levels.length; i++) {
                if (levels[i] == fullEnergy) {
                    levelId = i;
                    break;
                }
            }
        }

        if (isBoss)
            levelId = enemy.getSkin() - 1;

        WeaponData weaponData = enemyData.getWeaponDataMap(weaponTypeId, levelId, paidSpecialShot);
        logMessage(accountId, " currentEnergy: " + currentEnergy + " fullEnergy: " + fullEnergy
                + " levelId: " + levelId);

        boolean isIK = false;
        int chMult = 1;

        Money killAwardWin = Money.ZERO;

        boolean isBomber = EnemyRange.BOMB_ENEMY.getEnemies().contains(enemyType);
        logMessage(accountId, "isBomber: " + isBomber);

        if (isBoss) {

            Pair<Integer, Integer> limits = MathData.getBossLimits().get(enemy.getSkin());
            double hiLimit = limits.getKey();
            double middleLimit = limits.getValue();

            Map<Long, Double> prob;
            if (currentEnergy > hiLimit)
                prob = weaponData.getEnemyHiHitPointMap();
            else if (currentEnergy > middleLimit) {
                prob = weaponData.getEnemyMiddleHitPointMap();
            } else {
                prob = weaponData.getEnemyLowHitPointMap();
            }

            double hitProbabilityForBoss = MathData.getHitProbabilityForBoss(weaponTypeId, enemy.getSkin(),
                    paidSpecialShot, prob);
            isHit = RNG.rand() < hitProbabilityForBoss || isGuaranteedHit;

            logMessage(accountId, "boss hitProbabilityForBoss: " + hitProbabilityForBoss + " isHit: " + isHit);
            double win = 0;
            double realWin = 0;

            if (teststandPistolNoWin)
                isHit = false;

            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);

                Long hits = GameTools.getRandomNumberKeyFromMap(prob);
                chMult = weaponData.getRandomCriticalHit();
                logMessage(accountId, "boss chMult: " + chMult + " used prob : " + prob + " limits: " + limits);

                win = MathData.PAY_HIT_PERCENT * (double) hits;
                damage = hits;
                realWin = win;
                payout = stake.getWithMultiplier(win * chMult * betLevel);

                double newEnergy = currentEnergy - hits;
                if (newEnergy <= 0) {
                    realWin = currentEnergy * MathData.PAY_HIT_PERCENT * chMult;
                    payout = stake.getWithMultiplier(realWin * betLevel);
                    newEnergy = 0;
                    killAwardWin = stake.getWithMultiplier((hits - currentEnergy) * chMult * MathData.PAY_HIT_PERCENT * betLevel);
                }
                enemy.setEnergy(newEnergy);
            }
            logMessage(accountId, "boss newEnergy : " + enemy.getEnergy() + " payout: " + payout
                    + " chMult: " + chMult + " win: " + win + " damage: " + damage + " realWin: " + realWin);

        } else {


            boolean canTakeLowMap = currentEnergy <= weaponData.getLimitForHitPointMap()
                    && weaponData.getEnemyLowHitPointMap() != null
                    && !weaponData.getEnemyLowHitPointMap().isEmpty();
            Map<Long, Double> hitPointsMap = canTakeLowMap ? weaponData.getEnemyLowHitPointMap() : weaponData.getEnemyHiHitPointMap();

            double rtpForWeapon = MathData.getRtpForWeapon(wpId);
            Double avgPayout = enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT, paidSpecialShot, hitPointsMap);
            double hitProbability = rtpForWeapon / avgPayout;

            logMessage(accountId, "base hitProbability + " + hitProbability);
            boolean isX2Mode = enemy.getEnemyMode().equals(EnemyMode.X_2);
            if (isX2Mode) {
                hitProbability /= 2;
            }
            logMessage(accountId, "final hitProbability + " + hitProbability + " isX2Mode: " + isX2Mode);

            isHit = RNG.rand() < hitProbability || isGuaranteedHit;

            boolean needKillFromTeststand = teststandNeedKill || teststandNeedNewWeapons
                    || needKillEnemyAndGetBoss || teststandDoubleSW;

            if (needKillFromTeststand || teststandNeedCH || teststandNeed2SW_MW || teststandNeedSW_MW) {
                isHit = true;
                logMessage(accountId, "teststand need hit");
            }


            if (teststandPistolNoWin)
                isHit = false;

            logMessage(accountId, " wpId : " + wpId + " rtpForWeapon: " + rtpForWeapon
                    + " avgPayout: " + avgPayout
                    + " hitProbability: "
                    + hitProbability
                    + " isHit: " + isHit
            );


            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                double instanceKillEV = weaponData.getInstanceKillEV() / 100;
                double prob = instanceKillEV / ((currentEnergy) * MathData.PAY_HIT_PERCENT);
                if (prob > 1) {
                    logMessage(accountId, "error : " + prob + " currentEnergy: " + currentEnergy);
                }

                isIK = RNG.rand() < prob;

                if (needKillFromTeststand)
                    isIK = true;

                if(isBomber) {
                    isIK = false;
                }

                boolean isTableWithWeapon = currentEnergy <= weaponData.getLimitForHitPointMap();
                logMessage(accountId, "IK prob: " + prob + " isIK: " + isIK + " isTableWithWeapon: " + isTableWithWeapon);

                if (isIK) {
                    isTableWithWeapon = RNG.nextInt(100) > 25;
                }

                if (teststandDoubleSW)
                    isTableWithWeapon = true;

                if(teststandNeedNewWeapons)
                    isTableWithWeapon = false;

                Map<Long, Double> enemyLowHitPointMap = weaponData.getEnemyLowHitPointMap();
                if (enemyLowHitPointMap == null || enemyLowHitPointMap.isEmpty())
                    isTableWithWeapon = false;

                Long hits = GameTools.getRandomNumberKeyFromMap(isTableWithWeapon ?
                        weaponData.getEnemyLowHitPointMap() : weaponData.getEnemyHiHitPointMap());

                logMessage(accountId, "base hits + " + hits);


                if (isX2Mode) {
                    hits *= 2;
                }
                logMessage(accountId, "final  hits + " + hits);

                if (teststandDoubleSW && hits == 0) {
                    int cnt = 1000;
                    while (cnt-- > 0) {
                        hits = GameTools.getRandomNumberKeyFromMap(isTableWithWeapon ?
                                weaponData.getEnemyLowHitPointMap() : weaponData.getEnemyHiHitPointMap());
                        if (hits > 0)
                            break;
                    }
                }

                if (isBomber && needKillFromTeststand) {
                    hits = (long) currentEnergy;
                    logMessage(accountId, "needKillFromTeststand for bomber,  hits = currentEnergy , " +
                            "isTableWithWeapon set true "  + currentEnergy);
                    isTableWithWeapon = true;
                }


                chMult = weaponData.getRandomCriticalHit();

                if (teststandNeedCH) {
                    Map<Integer, Double> criticalHitProb = weaponData.getCriticalHitProb();
                    logMessage(accountId, " criticalHitProb from teststand: " + criticalHitProb);
                    if (criticalHitProb != null && !criticalHitProb.isEmpty()) {
                        Set<Integer> integers = criticalHitProb.keySet();
                        chMult = integers.stream().skip(RNG.nextInt(integers.size())).findFirst().get();
                        logMessage(accountId, " chMult from teststand: " + chMult);
                        Map<Integer, AtomicInteger> featuresAppeared = featureBySid.getFeaturesAppeared();
                        featuresAppeared.get(FEATURE_NEED_CH).incrementAndGet();
                    }
                }
                boolean isCriticalHit = chMult != 1;

                logMessage(accountId, " isCriticalHit: " + isCriticalHit
                        + " isTableWithWeapon: " + isTableWithWeapon + " chMult: " + chMult);

                if (hits == null) {
                    logMessage(accountId, "error");
                }

                double newEnergy = currentEnergy - hits;

                Pair<Double, Double> weaponDropData = MathData.getWeaponDropData(enemyType.getId(), weaponTypeId);
                Double avgWeaponAward = weaponDropData == null ? 0. : weaponDropData.getKey();
                if (isX2Mode)
                    avgWeaponAward *= 2;

                damage = hits;

                if (isIK) {
                    damage = (long) currentEnergy;
                    isKilled = true;
                    enemy.setEnergy(0);
                    payout = stake.getWithMultiplier(currentEnergy * MathData.PAY_HIT_PERCENT * betLevel * (isX2Mode ? 2 : 1)); // base payout
                    double multiplier = isTableWithWeapon ? (hits - avgWeaponAward) : (hits);
                    if(multiplier >= 0){
                        multiplier = isTableWithWeapon ? (hits - avgWeaponAward) : (hits);
                    }else{
                        isTableWithWeapon = false;
                        multiplier = hits;
                    }
                    logMessage(accountId, "multiplier: " + multiplier + " isTableWithWeapon: " + isTableWithWeapon);
                    killAwardWin = stake.getWithMultiplier(multiplier * chMult * MathData.PAY_HIT_PERCENT *  betLevel);
                } else {
                    enemy.setEnergy(newEnergy);
                    isKilled = newEnergy <= 0;

                    double multBase = hits > currentEnergy ? currentEnergy : hits;
                    payout = stake.getWithMultiplier((multBase * MathData.PAY_HIT_PERCENT * chMult * betLevel));

                    if (isKilled) {
                        newEnergy = 0;
                        enemy.setEnergy(newEnergy);
                        double multFromWeapon = isTableWithWeapon ? avgWeaponAward : 0;
                        double actual = hits - currentEnergy;

                        if (isBomber) {
                            logMessage(accountId, "actual before: {}, MathData.getAvgHpWinExploder(): {}",
                                    actual, MathData.getAvgHpWinExploder());
                            actual -= MathData.getAvgHpWinExploder();
                        }

                        logMessage(accountId, "multFromWeapon: " + multFromWeapon + " chMult: "
                                + chMult + " actual: " + actual);

                        double killAwardMult;

                        if(actual - multFromWeapon >= 0) {
                            killAwardMult = (actual - multFromWeapon) * chMult * MathData.PAY_HIT_PERCENT;
                        }
                        else {
                            isTableWithWeapon = false;
                            killAwardMult = actual * chMult * MathData.PAY_HIT_PERCENT;
                        }

                        killAwardWin = stake.getWithMultiplier(killAwardMult * betLevel); // additional killed payout
                        logMessage(accountId, "killAwardMult: " + killAwardMult + " isTableWithWeapon: " +  isTableWithWeapon);
                    }
                    logMessage(accountId, "multBase: " + multBase);
                }


                boolean isEnemyWithoutWeapon = weaponDropData == null;

                logMessage(accountId, " isEnemyWithoutWeaponTable: " + isEnemyWithoutWeapon);

                if (isTableWithWeapon && isKilled && !isEnemyWithoutWeapon && hits > 0) {
                    double probFromWeaponData = weaponDropData.getKey() / weaponDropData.getValue();
                    double probWeapon = probFromWeaponData;
                    probWeapon = probWeapon * chMult * (isX2Mode ? 2 : 1);
                    boolean needWeaponForKilling = (RNG.rand() < probWeapon);
                    logMessage(accountId,
                            " needWeaponForKilling: " + needWeaponForKilling
                                    + " weaponDropData: " + weaponDropData
                                    + " probFromWeaponData: " + probFromWeaponData
                                    + " probWeapon: " + probWeapon
                                    + " chMult: " + chMult
                                    + " isX2Mode: " + isX2Mode
                    );


                    if (probWeapon > 1) {
                        double compensateProb = probWeapon - 1;
                        Money newCompensation = Money.ZERO;
                        double avgWinCompProb = compensateProb * (weaponDropData.getKey() * MathData.PAY_HIT_PERCENT);
                        newCompensation = newCompensation.add(seat.getStake().multiply(avgWinCompProb * seat.getBetLevel()));
                        List<IWeaponSurplus> weaponSurplus = seat.getWeaponSurplus();
                        logMessage(accountId, "compensateProb: " + compensateProb
                                + " avgWinCompProb: " + avgWinCompProb
                                + " avgWinCompProb amount: " + newCompensation
                        );
                        updateWeaponSurplus(wpId, newCompensation, weaponSurplus, toService);
                    }

                    if (needWeaponForKilling) {
                        Pair<Integer, Integer> weaponPair;
                        List<Triple<Integer, Integer, Double>> killedTable = MathData.getWeaponKilledTable(enemyType.getId(),
                                weaponTypeId);
                        if (!killedTable.isEmpty()) {
                            weaponPair = GameTools.getRandomPair(killedTable);
                            int weaponId = weaponPair.getKey();
                            int newSpecialWeaponShots = weaponPair.getValue();
                            awardedWeapons.add(toService.createWeapon(weaponId, newSpecialWeaponShots,
                                    WeaponSource.KILL_AWARD.ordinal()));
                        }
                    }
                }
                logMessage(accountId, " newEnergy : " + newEnergy
                        + " payout: " + payout
                        + ",  killAwardWin: " + killAwardWin
                        + " isCriticalHit: " + isCriticalHit
                        + " isIK: " + isIK
                        + " hits: " + hits
                        + " avgWeaponAward: " + avgWeaponAward
                        + " damage: " + damage
                );

            }
        }


        isKilled = enemy.getEnergy() <= 0;

        bossShouldBeAppeared = RNG.rand() < MathData.getBosMainProb();
        if (needKillEnemyAndGetBoss)
            bossShouldBeAppeared = true;

        logMessage(accountId, " win :  " + payout + " bossShouldBeAppeared: " + bossShouldBeAppeared);
        logMessage(accountId, " weaponType:  " + weaponType + " shots: " + shots + " payout: " + payout
                + " newFreeShots: " + newFreeShots);

        ShootResult shootResult = new ShootResult(isPaidShot ? stake : Money.ZERO, payout, bossShouldBeAppeared, isKilled, enemy);
        shootResult.setInstanceKill(isIK);
        shootResult.setChMult(chMult);
        Double damageForWeapon = MathData.getAverageDamageForWeapon(weaponTypeId);

        if (!teststandPistolNoWin) {
            double prob = isSpecialWeapon ? MathData.getWheelHitSpecialWeapons() : MathData.getWheelHitPistol();
            double finalProb = prob / damageForWeapon;

            boolean needFeature = false;
            if(needMoneyWheel) {
                Map<Integer, AtomicInteger> featuresAppeared = featureBySid.getFeaturesAppeared();
                if (!featuresAppeared.isEmpty()) {
                    if (featuresAppeared.containsKey(FEATURE_GET_WHEEL)) {
                        AtomicInteger cnt = featuresAppeared.get(FEATURE_GET_WHEEL);
                        if ((teststandNeed2SW_MW && cnt.get() < 2)
                                || (!teststandNeed2SW_MW && cnt.get() < 1)
                        ) {
                            needFeature = true;
                            cnt.incrementAndGet();
                        }
                    }
                }
            }
            boolean realGame = RNG.rand() < finalProb;
            boolean needFromTeststand = needMoneyWheel && needFeature;

            if (realGame || needFromTeststand) {
                int randomPayoutForWheel = MathData.getRandomPayoutForWheel();
                Money wheelPayout = stake.getWithMultiplier(randomPayoutForWheel * betLevel);
                logMessage(accountId, "wheel appeared: prob: " + prob
                        + " randomPayoutForWheel: " + randomPayoutForWheel + " wheelPayout: " + wheelPayout
                        + " needMoneyWheel: " + needMoneyWheel
                        + " damageForWeapon: " + damageForWeapon
                        + "finalProb" + finalProb
                );
                shootResult.setMoneyWheelWin(wheelPayout);
            }

            Pair<Integer, Integer> weaponPair;
            double probWeapon = isSpecialWeapon ? MathData.getDroppedWeaponSWFreq() : MathData.getDroppedWeaponPistolFreq();
            double finalProbWeapon = probWeapon / damageForWeapon;
            boolean needWeaponOnShot = RNG.rand() < finalProbWeapon;
            logMessage(accountId, "main probWeapon: + " + probWeapon + " needWeaponOnShot: " + needWeaponOnShot
                    + " finalProbWeapon: " + finalProbWeapon + " teststandDoubleSW: " + teststandDoubleSW);

            if (needWeaponOnShot || teststandDoubleSW) {
                weaponPair = GameTools.getRandomPair(MathData.getDroppedWeaponsTable());
                int weaponId = weaponPair.getKey();
                int newSpecialWeaponShots = weaponPair.getValue();
                ITransportWeapon dropWeapon = toService.createWeapon(weaponId, newSpecialWeaponShots, WeaponSource.DROP_ON_SHOOT.ordinal());
                awardedWeapons.add(dropWeapon);
                logMessage(accountId, "main awardedWeapons: + " + awardedWeapons + " newSpecialWeaponShots: "
                        + newSpecialWeaponShots + " dropWeapon: " + dropWeapon);
            }
        }

        if (isHit)
            shootResult.setDamage(damage);

        int bossSkinId = -1;
        if (bossShouldBeAppeared) {
            Pair<Integer, Integer> bossParam = GameTools.getRandomPair(MathData.getBossParams());
            bossSkinId = bossParam.getKey();
            shootResult.setBossSkinId(bossSkinId);
            logMessage(accountId, "need generate boss: " + bossParam + " bossSkinId: " + bossSkinId);
        }

        if (isKilled) {
            if (!isHVenemy && !isBoss) {
                if (needKillEnemyAndTryGetHVEnemy)
                    shootResult.setNeedGenerateHVEnemy(true);
                else {
                    shootResult.setNeedGenerateHVEnemy(RNG.nextInt(30) == 0);
                }
            }
        }


        if (isKilled && isBomber) {
            Map<Integer, Double> totalHPdamageForExplorer = MathData.getTotalHPdamageForExplorer();
            Integer randomNumberKeyFromMap = GameTools.getRandomNumberKeyFromMap(totalHPdamageForExplorer);
            if (randomNumberKeyFromMap != null) {
                shootResult.setNeedExplodeHP(randomNumberKeyFromMap * chMult);
                logMessage(accountId, "get random explode from table: " + randomNumberKeyFromMap
                        + ", shootResult.getNeedExplodeHP(): " + shootResult.getNeedExplodeHP() + ",  chMult: " + chMult);
            } else {
                logMessage(accountId, "randomNumberKeyFromMap for Exploder is null, " +
                        "totalHPdamageForExplorer: " + totalHPdamageForExplorer);
            }
        }

        if (teststandNeedNewWeapons && !isBoss) {
            List<Triple<Integer, Integer, Double>> killedTable = MathData.getWeaponKilledTable(enemyType.getId(),
                    weaponTypeId);
            if (killedTable != null && !killedTable.isEmpty()) {
                SpecialWeaponType specialWeaponType;
                if (teststandNeedSWLaser) {
                    specialWeaponType = Ricochet;
                } else if (teststandNeedSWArtilleryStrike) {
                    specialWeaponType = ArtilleryStrike;
                } else if (teststandNeedSWNapalm) {
                    specialWeaponType = Napalm;
                } else if (teststandNeedSWNuke) {
                    specialWeaponType = Nuke;
                } else if (teststandNeedSWLightning) {
                    specialWeaponType = Lightning;
                } else {
                    List<SpecialWeaponType> weapons = Arrays.stream(values()).filter(sw ->
                            sw.getAvailableGameIds().contains((int) GameType.CLASH_OF_THE_GODS.getGameId()))
                            .collect(Collectors.toList());
                    specialWeaponType = weapons.get(RNG.nextInt(weapons.size()));
                }
                logMessage(accountId, "teststandNeedNewWeapons teststand feature: "
                        + featureBySid + " weaponType: " + weaponType);
                List<Triple<Integer, Integer, Double>> weaponKilledTable =
                        MathData.getWeaponKilledTable(enemyType.getId(), weaponTypeId);
                if (weaponKilledTable.stream().anyMatch(triple -> triple.first() == specialWeaponType.getId())) {
                    awardedWeapons.add(toService.createWeapon(specialWeaponType.getId(), 1, WeaponSource.KILL_AWARD.ordinal()));
                    if(teststandNeed2SW_MW || teststandNeedSW_MW){
                        Map<Integer, AtomicInteger> featuresAppeared = featureBySid.getFeaturesAppeared();
                        featuresAppeared.get(FEATURE_RANDOM_WEAPON).incrementAndGet();
                    }
                }
            }
        }

        if (featureBySid != null) {
            logMessage(accountId, "end teststand feature: " + featureBySid);
            if (teststandNeedSW_MW || teststandNeed2SW_MW || teststandNeedCH) {
                Map<Integer, AtomicInteger> featuresAppeared = featureBySid.getFeaturesAppeared();
                boolean b1 = teststandNeedSW_MW && featuresAppeared.get(FEATURE_GET_WHEEL).get() >= 1
                        && featuresAppeared.get(FEATURE_RANDOM_WEAPON).get() >= 1;
                boolean b2 = teststandNeed2SW_MW && featuresAppeared.get(FEATURE_GET_WHEEL).get() >= 2
                        && featuresAppeared.get(FEATURE_RANDOM_WEAPON).get() >= 2;
                boolean b3 = teststandNeedCH && featuresAppeared.get(FEATURE_NEED_CH).get() >= 1;
                if(b1 || b2 || b3)
                    TestStandLocal.getInstance().removeFeatureBySid(sessionId);

            } else {
                TestStandLocal.getInstance().removeFeatureBySid(sessionId);
            }
        }


        if (isKilled) {
            if (killAwardWin.smallerThan(Money.ZERO)) {
                killAwardWin = Money.ZERO;
            }
            shootResult.setKillAwardWin(killAwardWin);
        }

        shootResult.setAwardedWeapons(awardedWeapons);

        logMessage(accountId, "shootResult: " + shootResult);

        return shootResult;
    }

    static void updateWeaponSurplus(int wpId, Money newCompensation, List<IWeaponSurplus> weaponSurplus,
                                    ITransportObjectsFactoryService toService) {
        boolean weaponsWasFound = false;
        if (weaponSurplus.size() > 0) {
            for (IWeaponSurplus surplus : weaponSurplus) {
                if (surplus.getId() == wpId) {
                    int shotsOld = surplus.getShots();
                    long oldCompensation = surplus.getWinBonus();
                    surplus.setShots(shotsOld);
                    surplus.setWinBonus(oldCompensation + newCompensation.toCents());
                    weaponsWasFound = true;
                }
            }
        }
        if (!weaponsWasFound) {
            weaponSurplus.add(toService.createWeaponSurplus(wpId, 0, newCompensation.toCents()));
        }

    }

    @Override
    protected boolean isRealBet(Weapon weapon) {
        return true;
    }


    @Override
    public IShootResult doShootWithExplode(Enemy enemy, Seat seat, int explodeDamage,
                                           ITransportObjectsFactoryService toService) throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        if (debug) {
            logMessage(seat.getAccountId(), "doShootWithExplode, sessionId: " + sessionId
                    + " explodeDamage: " + explodeDamage);
        }

        Weapon weapon = seat.getCurrentWeapon();
        logMessage(accountId, "doShootWithExplode started, weapon:  " + weapon);

        Money payout;
        long damage;

        double currentEnergy = enemy.getEnergy();
        double fullEnergy = enemy.getFullEnergy();
        logMessage(accountId, "doShootWithExplode currentEnergy: " + currentEnergy + " fullEnergy: " + fullEnergy);


        long hits = explodeDamage;

        damage = hits;

        double newEnergy = currentEnergy - hits;
        Money compensateWin = Money.ZERO;

        double realHits = hits;
        if (newEnergy < 0) {
            newEnergy = 0;
            realHits = currentEnergy;
            compensateWin = seat.getStake().getWithMultiplier((hits - currentEnergy) * MathData.PAY_HIT_PERCENT * seat.getBetLevel());
        }

        double win = MathData.PAY_HIT_PERCENT * realHits;

        enemy.setEnergy(newEnergy);
        payout = seat.getStake().getWithMultiplier(win * seat.getBetLevel());
        logMessage(accountId, "doShootWithExplode newEnergy : " + enemy.getEnergy()
                + " payout: " + payout + " compensateWin: " + compensateWin
                + " realHits: " + realHits
                + " hits: " + hits
                + " seat.getBetLevel()" + seat.getBetLevel()
        );

        boolean isKilled = enemy.getEnergy() <= 0;

        logMessage(accountId, "doShootWithExplode enemy is killed, win :  " + payout);

        ShootResult shootResult = new ShootResult(Money.ZERO, payout, false, isKilled, enemy);
        shootResult.setDamage(damage);
        shootResult.setKillAwardWin(compensateWin);

        if (shootResult.isDestroyed()) {
            seat.incCountEnemiesKilled();
            IPlayerStats roundStats = seat.getPlayerInfo().getRoundStats();
            Map<Integer, Long> roundStatsKills = roundStats.getKills();
            roundStatsKills.put(0, roundStatsKills.isEmpty() ? 1 : roundStatsKills.get(0) + 1);
        }

        if (debug) {
            logMessage(accountId, "doShootWithExplode shootResult: " + shootResult);
        }
        return shootResult;
    }

    public GameConfig getCurrentGameConfig(IGameConfigService gameConfigService) {
        return null;
    }

}
