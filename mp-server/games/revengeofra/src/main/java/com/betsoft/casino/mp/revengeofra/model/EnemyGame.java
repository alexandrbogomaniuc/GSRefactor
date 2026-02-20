package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.revengeofra.model.math.*;
import com.betsoft.casino.mp.common.AbstractEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.betsoft.casino.mp.model.SpecialWeaponType.*;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {

    public EnemyGame(Logger logger, IGameConfigService gameConfigService) {
        super(logger, GameType.REVENGE_OF_RA, gameConfigService);
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
        boolean teststandNeedNewWeapons = featureBySid != null && featureBySid.getId() >= 30 && featureBySid.getId() <= 41;
        boolean needKillEnemyAndGetBoss = featureBySid != null && (featureBySid.getId() == 9);
        boolean needMoneyWheel = featureBySid != null && (featureBySid.getId() == 52);
        boolean teststandDoubleSW = featureBySid != null && (featureBySid.getId() == 53);
        boolean teststandPistolNoWin = featureBySid != null && (featureBySid.getId() == 55);
        boolean isGuaranteedHit = featureBySid != null && (featureBySid.getId() == 11);

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        Money payout = Money.ZERO;
        boolean bossShouldBeAppeared;
        boolean isKilled = false;
        String weaponType = null;
        int shots = 0;
        String prize = "";
        int newFreeShots = 0;
        boolean isHit = false;
        long damage = 0;
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
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

        WeaponData weaponData = enemyData.getWeaponDataMap(weaponTypeId, levelId, paidSpecialShot);
        logMessage(accountId, " currentEnergy: " + currentEnergy + " fullEnergy: " + fullEnergy
                + " levelId: " + levelId);

        boolean isIK = false;
        int chMult = 1;

        List<Integer> shotGems = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            shotGems.add(i, 0);
        }

        Money killAwardWin = Money.ZERO;
        Integer multiplierPay = 1;
        boolean isWeaponCarrier = EnemyType.WEAPON_CARRIER.getId() == enemyType.getId();
        boolean isBomberWithoutHP = enemyType.equals(EnemyType.ENEMY_14);
        logMessage(accountId, "isWeaponCarrier: " + isWeaponCarrier + " isBomberWithoutHP: " + isBomberWithoutHP);
        double bomberPossibleWin = MathData.getBomberHPWin() * MathData.PAY_HIT_PERCENT;

        if (isBoss) {

            double hitProbabilityForBoss = MathData.getHitProbabilityForBoss(weaponTypeId, enemy.getSkin(), paidSpecialShot);
            isHit = RNG.rand() < hitProbabilityForBoss || isGuaranteedHit;

            logMessage(accountId, "boss hitProbabilityForBoss: " + hitProbabilityForBoss + " isHit: " + isHit);
            double win = 0;
            double realWin = 0;

            if(teststandPistolNoWin)
                isHit = false;

            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
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

        } else if (isBomberWithoutHP) {
            Double rtpForWeapon = MathData.getRtpForWeapon(weaponTypeId, paidSpecialShot);
            double probOfKilling = rtpForWeapon / bomberPossibleWin / 100;
            isKilled = RNG.rand() < probOfKilling;

            if(teststandPistolNoWin)
                isKilled = false;

            if (teststandNeedKill) {
                isKilled = true;
                logMessage(accountId, "teststand need kill");
            }
            logMessage(accountId, "isBomberWithoutHP, rtpForWeapon:  " + rtpForWeapon + " betLevel: " + betLevel
                    + " probOfKilling: " + probOfKilling + " isKilled: " + isKilled + " bomberPossibleWin: " + bomberPossibleWin);
        } else {

            double rtpForWeapon = MathData.getRtpForWeapon(wpId, paidSpecialShot);
            Double avgPayout = enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT, paidSpecialShot);
            double hitProbability = rtpForWeapon / avgPayout;
            isHit = RNG.rand() < hitProbability || isGuaranteedHit;

            boolean needKillFromTeststand = teststandNeedKill || teststandNeedNewWeapons
                    || needKillEnemyAndGetBoss || teststandDoubleSW;

            if (needKillFromTeststand) {
                isHit = true;
                logMessage(accountId, "teststand need kill");
            }


            boolean enemyWithoutIK = isWeaponCarrier;

            if(teststandPistolNoWin)
                isHit = false;

            logMessage(accountId, " wpId : " + wpId + " rtpForWeapon: " + rtpForWeapon
                    + " avgPayout: " + avgPayout
                    + " hitProbability: "
                    + hitProbability
                    + " isHit: " + isHit
                    + " enemyWithoutIK: " + enemyWithoutIK
            );


            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                double instanceKillEV = weaponData.getInstanceKillEV() / 100;
                double prob = instanceKillEV / ((currentEnergy) * MathData.PAY_HIT_PERCENT);
                if (prob > 1) {
                    logMessage(accountId, "error : " + prob + " currentEnergy: " + currentEnergy);
                }

                isIK = !enemyWithoutIK && RNG.rand() < prob;

                if (needKillFromTeststand && !enemyWithoutIK)
                    isIK = true;

                boolean isTableWithWeapon = currentEnergy <= weaponData.getLimitForHitPointMap();
                logMessage(accountId, "IK prob: " + prob + " isIK: " + isIK
                        + " isTableWithWeapon: " + isTableWithWeapon);

                if (isIK) {
                    isTableWithWeapon = RNG.nextInt(100) > 25;
                }

                if(teststandDoubleSW)
                    isTableWithWeapon = true;

                Map<Long, Double> enemyLowHitPointMap = weaponData.getEnemyLowHitPointMap();
                if (enemyLowHitPointMap == null || enemyLowHitPointMap.isEmpty())
                    isTableWithWeapon = false;

                Long hits = GameTools.getRandomNumberKeyFromMap(isTableWithWeapon ?
                        weaponData.getEnemyLowHitPointMap() : weaponData.getEnemyHiHitPointMap());

                if(teststandDoubleSW && hits == 0){
                    int cnt = 1000;
                    while (cnt-- > 0){
                        hits = GameTools.getRandomNumberKeyFromMap(isTableWithWeapon ?
                                weaponData.getEnemyLowHitPointMap() : weaponData.getEnemyHiHitPointMap());
                        if(hits > 0)
                            break;
                    }
                }

                chMult = weaponData.getRandomCriticalHit();

                boolean isCriticalHit = chMult != 1;

                logMessage(accountId, " isCriticalHit: " + isCriticalHit
                        + " isTableWithWeapon: " + isTableWithWeapon + " chMult: " + chMult);

                if (hits == null) {
                    logMessage(accountId, "error");
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
                    logMessage(accountId, "multiplier: " + multiplier);
                    killAwardWin = stake.getWithMultiplier(multiplier * chMult * MathData.PAY_HIT_PERCENT * multiplierPay * betLevel);
                } else {
                    enemy.setEnergy(newEnergy);
                    isKilled = newEnergy <= 0;

                    double multBase = hits > currentEnergy ? currentEnergy : hits;
                    payout = stake.getWithMultiplier((multBase * MathData.PAY_HIT_PERCENT * chMult * multiplierPay * betLevel));

                    if (isKilled) {
                        newEnergy = 0;
                        enemy.setEnergy(newEnergy);
                        double multFromWeapon = isTableWithWeapon ? avgWeaponAward : 0;
                        double weaponPart = multFromWeapon * chMult;
                        double actual = hits - currentEnergy;
                        logMessage(accountId, "multFromWeapon: " + multFromWeapon + " chMult: "
                                + chMult + " actual: " + actual);
                        double killAwardMult = (actual * chMult * multiplierPay * MathData.PAY_HIT_PERCENT) -
                                (weaponPart * MathData.PAY_HIT_PERCENT);
                        killAwardWin = stake.getWithMultiplier(killAwardMult * betLevel); // additional killed payout
                        logMessage(accountId, "killAwardMult: " + killAwardMult);
                    }
                    logMessage(accountId, "multBase: " + multBase);
                }

                if (isWeaponCarrier && isKilled) {
                    if (payout.smallerThan(Money.ZERO))
                        payout = Money.ZERO;
                    if (killAwardWin.smallerThan(Money.ZERO))
                        killAwardWin = Money.ZERO;
                }

                boolean isEnemyWithoutWeapon = weaponDropData == null;

                logMessage(accountId, "isWeaponCarrier: " + isWeaponCarrier
                        + " isEnemyWithoutWeaponTable: " + isEnemyWithoutWeapon);

                if ((isTableWithWeapon || isWeaponCarrier) && isKilled && !isEnemyWithoutWeapon && hits > 0) {
                    double probFromWeaponData = weaponDropData.getKey() / weaponDropData.getValue();
                    double probWeapon = probFromWeaponData;
                    probWeapon = probWeapon * chMult;
                    boolean needWeaponForKilling = (RNG.rand() < probWeapon) || isWeaponCarrier;
                    logMessage(accountId,
                            " needWeaponForKilling: " + needWeaponForKilling
                                    + " weaponDropData: " + weaponDropData
                                    + " probFromWeaponData: " + probFromWeaponData
                                    + " probWeapon: " + probWeapon
                                    + " chMult: " + chMult
                                    + " isWeaponCarrier: " + isWeaponCarrier
                    );

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

        if (!isBomberWithoutHP)
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

        if (!isBoss && !teststandPistolNoWin) {
            double prob = isSpecialWeapon ? MathData.getWheelHitSpecialWeapons() : MathData.getWheelHitPistol();
            double finalProb = prob / damageForWeapon;
            if (RNG.rand() < finalProb || needMoneyWheel) {
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
                ITransportWeapon dropWeapon = toService.createWeapon(weaponId, newSpecialWeaponShots);
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

        if (teststandNeedNewWeapons && !isBoss) {
            List<Triple<Integer, Integer, Double>> killedTable = MathData.getWeaponKilledTable(enemyType.getId(),
                    weaponTypeId);
            if (!killedTable.isEmpty()) {
                weaponType = SpecialWeaponType.values()[featureBySid.getId() - 30].getMathTitle();
                logMessage(accountId, "teststand feature: " + featureBySid + " weaponType: " + weaponType);
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

        if (isKilled && isBomberWithoutHP) {
            int totalHPWin = MathData.getBomberHPWin();
            shootResult.setNeedExplodeHP(totalHPWin);
        }
        shootResult.setAwardedWeapons(awardedWeapons);

        logMessage(accountId, "shootResult: " + shootResult);

        return shootResult;
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

        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        EnemyData enemyData = MathData.getEnemyData(enemyType.getId());

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
            compensateWin = seat.getStake().multiply((hits - currentEnergy) * MathData.PAY_HIT_PERCENT * seat.getBetLevel());
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
