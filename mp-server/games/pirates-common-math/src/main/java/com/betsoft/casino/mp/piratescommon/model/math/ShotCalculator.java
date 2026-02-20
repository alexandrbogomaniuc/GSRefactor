package com.betsoft.casino.mp.piratescommon.model.math;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

import static com.betsoft.casino.mp.model.SpecialWeaponType.values;

public class ShotCalculator {
    public static ShootResult shootBaseEnemy(AbstractActionSeat seat, Weapon weapon, Money stake, AbstractEnemy enemy,
                                             boolean isBot, boolean isBossRound, boolean isNearLandMine,
                                             double totalDamageMultiplier, ITransportObjectsFactoryService toService,
                                             GameConfig currentGameConfig, Logger logger)
            throws CommonException {

        long accountId = seat.getAccountId();
        logMessage(logger, accountId, " shootBaseEnemy started, weapon:  " + weapon);

        int betLevel = seat.getBetLevel();
        IShot actualShot = seat.getActualShot();

        if (!MathData.getPossibleBetLevels().contains(betLevel)) {
            throw new CommonException("error bet level");
        }

        if (actualShot == null) {
            throw new CommonException("actual shot is null");
        }

        boolean paidSpecialShot = actualShot.isPaidSpecialShot();
        logMessage(logger, accountId, " paidSpecialShot:  " + paidSpecialShot + " seat.getBetLevel(): " + betLevel);

        String sessionId = seat.getPlayerInfo().getSessionId();
        TestStandFeature featureBySid = null;
        if (sessionId != null) {
            featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(logger, seat.getAccountId(), " teststand feature : " + featureBySid);
        }

        boolean isSpecialWeapon = (weapon != null);
        boolean isHVenemy = enemy.getEnemyClass().getEnemyType().isHVenemy();
        boolean isBoss = enemy.isBoss();
        boolean isPaidShot = !isSpecialWeapon;

        boolean needKillEnemyAndTryGetHVEnemy = featureBySid != null && featureBySid.getId() == 5;
        boolean needKillEnemyAndTryBoss = featureBySid != null && featureBySid.getId() == 9;
        boolean teststandNeedKillEnemy = featureBySid != null && (featureBySid.getId() == 6 || featureBySid.getId() == 7);
        boolean teststandNeedKill = teststandNeedKillEnemy || needKillEnemyAndTryBoss || needKillEnemyAndTryGetHVEnemy;
        boolean teststandNeedNewWeapons = featureBySid != null && featureBySid.getId() >= 30 && featureBySid.getId() <= 41;
        boolean needKillEnemyAndGetBoss = featureBySid != null && (featureBySid.getId() == 9);
        boolean teststandNeedBronzeKey = featureBySid != null && (featureBySid.getId() == 48) && isPaidShot;

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        Money payout = Money.ZERO;
        Money additionalPayout = Money.ZERO;
        boolean bossShouldBeAppeared;
        boolean isKilled;
        String weaponType = null;
        int shots = 0;
        int newFreeShots = 0;
        boolean isHit;
        long damage = 0;
        IEnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        int wpId = isSpecialWeapon ? weapon.getType().getId() : -1;
        EnemyData enemyData = MathData.getEnemyData(enemyType.getId());
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

        WeaponData weaponData = enemyData.getWeaponDataMap(weaponTypeId, levelId);
        logMessage(logger, accountId, " currentEnergy: " + currentEnergy + " fullEnergy: " + fullEnergy
                + " levelId: " + levelId);

        boolean isIK = false;
        int chMult = 1;

        Money killAwardWin = Money.ZERO;
        boolean isWeaponCarrier = EnemyType.WEAPON_CARRIER.getId() == enemyType.getId();
        Money withMultiplier = Money.ZERO;

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

            double hitProbabilityForBoss = MathData.getHitProbabilityForBoss(weaponTypeId, enemy.getSkin(), prob);

            isHit = RNG.rand() < hitProbabilityForBoss;

            logMessage(logger, accountId, "boss hitProbabilityForBoss: " + hitProbabilityForBoss + " isHit: " + isHit);
            double win = 0;
            double realWin = 0;
            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);

                Long hits = GameTools.getRandomNumberKeyFromMap(prob);
                chMult = weaponData.getRandomCriticalHit();
                logMessage(logger, accountId, "boss chMult: " + chMult + " used prob : " + prob + " limits: " + limits);

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
            logMessage(logger, accountId, "boss newEnergy : " + enemy.getEnergy() + " payout: " + payout
                    + " chMult: " + chMult + " win: " + win + " damage: " + damage + " realWin: " + realWin);

        } else {

            boolean canTakeLowMap = currentEnergy <= weaponData.getLimitForHitPointMap()
                    && weaponData.getEnemyLowHitPointMap() != null
                    && !weaponData.getEnemyLowHitPointMap().isEmpty();
            Map<Long, Double> hitPointsMap = canTakeLowMap ? weaponData.getEnemyLowHitPointMap() : weaponData.getEnemyHiHitPointMap();

            double rtpForWeapon = MathData.getRtpForWeapon(wpId);
            Double avgPayout = enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT, hitPointsMap);
            double hitProbability = rtpForWeapon / avgPayout;
            isHit = RNG.rand() < hitProbability;

            boolean needKillFromTeststand = teststandNeedKill || teststandNeedNewWeapons || needKillEnemyAndGetBoss;

            if (needKillFromTeststand) {
                isHit = true;
                logMessage(logger, accountId, "teststand need kill");
            }


            boolean enemyWithoutIK = isWeaponCarrier;

            logMessage(logger, accountId, " wpId : " + wpId + " rtpForWeapon: " + rtpForWeapon
                    + " avgPayout: " + avgPayout
                    + " hitProbability: "
                    + hitProbability
                    + " isHit: " + isHit
                    + " enemyWithoutIK: " + enemyWithoutIK
            );


            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                double instantKillEV = weaponData.getInstantKillEV() / 100;
                double prob = instantKillEV / ((currentEnergy) * MathData.PAY_HIT_PERCENT);
                if (prob > 1) {
                    logMessage(logger, accountId, "error : " + prob + " currentEnergy: " + currentEnergy);
                }

                isIK = !enemyWithoutIK && RNG.rand() < prob;

                if (needKillFromTeststand && !enemyWithoutIK)
                    isIK = true;

                boolean isTableWithWeapon = currentEnergy <= weaponData.getLimitForHitPointMap();
                logMessage(logger, accountId, "IK prob: " + prob + " isIK: " + isIK
                        + " isTableWithWeapon: " + isTableWithWeapon);

                if (isIK) {
                    isTableWithWeapon = RNG.nextInt(100) > 25;
                }

                Map<Long, Double> enemyLowHitPointMap = weaponData.getEnemyLowHitPointMap();
                if (enemyLowHitPointMap == null || enemyLowHitPointMap.isEmpty())
                    isTableWithWeapon = false;

                Long hits = GameTools.getRandomNumberKeyFromMap(isTableWithWeapon ?
                        weaponData.getEnemyLowHitPointMap() : weaponData.getEnemyHiHitPointMap());


                chMult = weaponData.getRandomCriticalHit();

                boolean isCriticalHit = chMult != 1;

                logMessage(logger, accountId, " isCriticalHit: " + isCriticalHit
                        + " isTableWithWeapon: " + isTableWithWeapon + " chMult: " + chMult);

                if (hits == null) {
                    logMessage(logger, accountId, "error");
                }


                double newEnergy = currentEnergy - hits;

                Pair<Double, Double> weaponDropData = MathData.getWeaponDropData(enemyType.getId(), weaponTypeId);
                Double avgWeaponAward = weaponDropData == null ? 0. : weaponDropData.getKey();

                damage = hits;
                if (isIK) {
                    damage = (long) currentEnergy;
                    isKilled = true;
                    enemy.setEnergy(0);
                    payout = stake.getWithMultiplier(currentEnergy * MathData.PAY_HIT_PERCENT * betLevel); // base payout
                    double multiplier = isTableWithWeapon ? (hits - avgWeaponAward) : (hits);
                    if(multiplier >= 0){
                        multiplier = isTableWithWeapon ? (hits - avgWeaponAward) : (hits);
                    }else{
                        isTableWithWeapon = false;
                        multiplier = hits;
                    }
                    logMessage(logger, accountId, "multiplier: " + multiplier + " isTableWithWeapon: " + isTableWithWeapon);
                    killAwardWin = stake.getWithMultiplier(multiplier * chMult * MathData.PAY_HIT_PERCENT * betLevel);
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
                        double killAwardMult;
                        // additional killed payout
                        if (!isWeaponCarrier) {
                            if (actual - multFromWeapon >= 0) {
                                killAwardMult = (actual - multFromWeapon) * chMult * MathData.PAY_HIT_PERCENT;
                            } else {
                                isTableWithWeapon = false;
                                killAwardMult = actual * chMult * MathData.PAY_HIT_PERCENT;
                            }
                            killAwardWin = stake.getWithMultiplier(killAwardMult * betLevel); // additional killed payout
                        } else {
                            if (chMult == 2) {
                                killAwardMult = (actual - multFromWeapon) * chMult * MathData.PAY_HIT_PERCENT;
                                killAwardWin = stake.getWithMultiplier(killAwardMult * betLevel); // additional killed payout
                                double awardMultAdditional = multFromWeapon * MathData.PAY_HIT_PERCENT;
                                withMultiplier = withMultiplier.add(stake.getWithMultiplier(awardMultAdditional * betLevel));
                                killAwardWin = killAwardWin.add(withMultiplier);
                                logMessage(logger, accountId, "weapon carrier awardMultAdditional: " + awardMultAdditional);
                            } else {
                                killAwardMult = (actual - multFromWeapon) * chMult * MathData.PAY_HIT_PERCENT;
                                killAwardWin = stake.getWithMultiplier(killAwardMult * betLevel); // additional killed payout
                            }
                        }

                        logMessage(logger, accountId, "killAwardMult: " + killAwardMult + " isTableWithWeapon: " + isTableWithWeapon);
                    }
                    logMessage(logger, accountId, "multBase: " + multBase);
                }

                if (isWeaponCarrier && isKilled) {
                    if (payout.smallerThan(Money.ZERO))
                        payout = Money.ZERO;
                    if (killAwardWin.smallerThan(Money.ZERO))
                        killAwardWin = Money.ZERO;
                }

                logMessage(logger, accountId, "isWeaponCarrier: " + isWeaponCarrier);

                if ((isTableWithWeapon || isWeaponCarrier) && isKilled && hits > 0) {
                    double probFromWeaponData = weaponDropData.getKey() / weaponDropData.getValue();
                    double probWeapon = probFromWeaponData;
                    probWeapon = isWeaponCarrier ? probWeapon : probWeapon * chMult;
                    boolean needWeaponForKilling = (RNG.rand() < probWeapon) || isWeaponCarrier;
                    logMessage(logger, accountId,
                            " needWeaponForKilling: " + needWeaponForKilling
                                    + " weaponDropData: " + weaponDropData
                                    + " probFromWeaponData: " + probFromWeaponData
                                    + " probWeapon: " + probWeapon
                                    + " chMult: " + chMult
                                    + " isWeaponCarrier: " + isWeaponCarrier
                    );

                    Money newCompensation = Money.ZERO;
                    if (probWeapon > 1) {
                        double compensateProb = probWeapon - 1;
                        double avgWinCompProb = compensateProb * (weaponDropData.getKey() * MathData.PAY_HIT_PERCENT);
                        newCompensation = newCompensation.add(seat.getStake().getWithMultiplier(avgWinCompProb * betLevel));
                        List<IWeaponSurplus> weaponSurplus = seat.getWeaponSurplus();
                        logMessage(logger, accountId, "compensateProb: " + compensateProb
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
                            awardedWeapons.add(toService.createWeapon(weaponId, newSpecialWeaponShots));
                        }
                    }
                }
                logMessage(logger, accountId, " newEnergy : " + newEnergy
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

        logMessage(logger, accountId, " win :  " + payout + " bossShouldBeAppeared: " + bossShouldBeAppeared);
        logMessage(logger, accountId, " weaponType:  " + weaponType + " shots: " + shots + " payout: " + payout
                + " newFreeShots: " + newFreeShots);

        ShootResult shootResult = new ShootResult(isPaidShot ? stake : Money.ZERO, payout, bossShouldBeAppeared, isKilled, enemy);
        shootResult.setInstanceKill(isIK);
        shootResult.setChMult(chMult);

        if (isHit)
            shootResult.setDamage(damage);

        int bossSkinId = -1;
        if (bossShouldBeAppeared) {
            Pair<Integer, Integer> bossParam = GameTools.getRandomPair(MathData.getBossParams());
            bossSkinId = bossParam.getKey();
            shootResult.setBossSkinId(bossSkinId);
            logMessage(logger, accountId, "need generate boss: " + bossParam + " bossSkinId: " + bossSkinId);
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

        Pair<Integer, Integer> weaponPair;
        Double damageForWeapon = MathData.getAverageDamageForWeapon(weaponTypeId);
        double probWeapon = isSpecialWeapon ? MathData.getDroppedWeaponSWFreq() : MathData.getDroppedWeaponPistolFreq();
        double finalProbWeapon = probWeapon / damageForWeapon;
        boolean needWeaponOnShot = RNG.rand() < finalProbWeapon;
        logMessage(logger, accountId, "main probWeapon: + " + probWeapon + " needWeaponOnShot: " + needWeaponOnShot
                + " finalProbWeapon: " + finalProbWeapon);

        if (needWeaponOnShot) {
            weaponPair = GameTools.getRandomPair(MathData.getDroppedWeaponsTable());
            int weaponId = weaponPair.getKey();
            int newSpecialWeaponShots = weaponPair.getValue();
            ITransportWeapon dropWeapon = toService.createWeapon(weaponId, newSpecialWeaponShots, WeaponSource.DROP_ON_SHOOT.ordinal());
            awardedWeapons.add(dropWeapon);
            logMessage(logger, accountId, "main awardedWeapons: + " + awardedWeapons + " newSpecialWeaponShots: "
                    + newSpecialWeaponShots + " dropWeapon: " + dropWeapon);
        }

        if (teststandNeedNewWeapons) {
            List<Triple<Integer, Integer, Double>> killedTable = MathData.getWeaponKilledTable(enemyType.getId(),
                    weaponTypeId);
            if (!killedTable.isEmpty()) {
                weaponType = SpecialWeaponType.values()[featureBySid.getId() - 30].getMathTitle();
                logMessage(logger, accountId, "teststand feature: " + featureBySid + " weaponType: " + weaponType);
                String finalWeaponType = weaponType;
                Optional<SpecialWeaponType> wtype = Arrays.stream(values())
                        .filter(specialWeaponType -> specialWeaponType.getMathTitle().equals(finalWeaponType)).findFirst();
                SpecialWeaponType specialWeaponType = wtype.get();
                awardedWeapons.add(toService.createWeapon(specialWeaponType.getId(),
                        RNG.nextInt(20) + 5));
            }
        }

        if (featureBySid != null)
            TestStandLocal.getInstance().removeFeatureBySid(sessionId);


        if (isKilled) {
            if (killAwardWin.smallerThan(Money.ZERO)) {
                killAwardWin = Money.ZERO;
            }
            shootResult.setKillAwardWin(killAwardWin);
        }

        shootResult.setAwardedWeapons(awardedWeapons);

        logMessage(logger, accountId, "shootResult: " + shootResult);

        return shootResult;
    }

    public static void updateWeaponSurplus(int wpId, Money newCompensation, List<IWeaponSurplus> weaponSurplus,
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

    protected static void logMessage(Logger logger, long aid, String message) {
        if(logger!=null)
            logger.debug("aid: {}: {}", aid, message);
    }

    public static void compensateSpecialWeapons(AbstractActionSeat seat, ITransportObjectsFactoryService toService, Logger logger) {
        try {
            logger.debug("compensateSpecialWeapons starting, getAccountId: {}", seat.getAccountId());
            StringBuilder vbaData = new StringBuilder();
            int totalCountShots = 0;
            Money totalCompensation = Money.ZERO;
            Money stake = seat.getStake();

            List<IWeaponSurplus> weaponSurpluses = seat.getWeaponSurplus();
            logger.debug("compensateSpecialWeapons, account: {}, weaponSurpluses before: {}",
                    seat.getAccountId(), weaponSurpluses);

            for (Object entry : seat.getWeapons().entrySet()) {
                @SuppressWarnings("unchecked")
                Map.Entry<SpecialWeaponType, IWeapon> weaponTypeWeaponEntry =
                        (Map.Entry<SpecialWeaponType, IWeapon>) entry;
                Weapon weapon = (Weapon) weaponTypeWeaponEntry.getValue();
                SpecialWeaponType key = weaponTypeWeaponEntry.getKey();
                int weaponId = weapon.getType().getId();
                Double rtpForWeapon = MathData.getFullRtpForWeapon(weaponId) / 100;

                int shots = weapon.getShots();
                Money newCompensation = Money.ZERO;

                double multiplier = new BigDecimal(shots, MathContext.DECIMAL32)
                        .multiply(new BigDecimal(rtpForWeapon, MathContext.DECIMAL32))
                        .multiply(new BigDecimal(MathData.getAverageDamageForWeapon(weaponId), MathContext.DECIMAL32))
                        .multiply(new BigDecimal(seat.getBetLevel(), MathContext.DECIMAL32))
                        .doubleValue();
                newCompensation = newCompensation.add(stake.getWithMultiplier(multiplier));


                boolean weaponWasFound = false;
                if (weaponSurpluses.size() > 0) {
                    for (IWeaponSurplus weaponSurplus : weaponSurpluses) {
                        if (weaponSurplus.getId() == key.getId()) {
                            weaponWasFound = true;
                            int shotsOld = weaponSurplus.getShots();
                            long winBonusNew = weaponSurplus.getWinBonus();
                            int newShots = shotsOld + weapon.getShots();
                            weaponSurplus.setShots(newShots);
                            newCompensation = Money.fromCents(winBonusNew + newCompensation.toCents());
                            weaponSurplus.setWinBonus(newCompensation.toCents());
                        }
                    }
                }
                if (!weaponWasFound && newCompensation.greaterThan(Money.ZERO)) {
                    weaponSurpluses.add(toService.createWeaponSurplus(key.getId(), weapon.getShots(),
                            newCompensation.toCents()));
                }
                if (newCompensation.greaterThan(Money.ZERO)) {
                    vbaData.append(key.getTitle()).append(",")
                            .append(weapon.getShots())
                            .append(",")
                            .append(newCompensation);
                    vbaData.append("&");
                    totalCompensation = totalCompensation.add(newCompensation);
                }
            }

            if (totalCompensation.greaterThan(Money.ZERO)) {
                seat.setWeaponSurplus((ArrayList<IWeaponSurplus>) weaponSurpluses);
                seat.incrementRoundWin(totalCompensation);
                seat.setCompensateSpecialWeapons(new Money(totalCompensation.getValue()));
                IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
                currentPlayerRoundInfo.addWeaponSurplusMoney(totalCompensation);
                currentPlayerRoundInfo.addTotalPayouts(totalCompensation);
                currentPlayerRoundInfo.addWeaponSurplusVBA(vbaData.toString());
            }
            logger.debug("compensateSpecialWeapons end, totalCompensation: {}," +
                            " totalCountShots: {} , getAccountId: {}, weaponSurpluses: {}",
                    totalCompensation, totalCountShots, seat.getAccountId(), weaponSurpluses);

        } finally {
            seat.resetWeapons();
        }
    }

    public  static String getQuestKey(AbstractActionSeat seat){
        boolean isSpecialWeapon = seat.getActualShot().getWeaponId() != -1;
        String sessionId = seat.getPlayerInfo().getSessionId();
        TestStandFeature featureBySid = null;
        if (sessionId != null) {
            featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
        }
        boolean teststandNeedBronzeKey = featureBySid != null && (featureBySid.getId() == 48);
        boolean isKeyAppeared = MathQuestData.isKeyAppeared(seat.getBetLevel(), isSpecialWeapon);
        return isKeyAppeared || teststandNeedBronzeKey ? "key" : "";
    }
}
