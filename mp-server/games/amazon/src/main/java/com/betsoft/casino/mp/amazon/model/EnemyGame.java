package com.betsoft.casino.mp.amazon.model;

import com.betsoft.casino.mp.amazon.model.math.*;
import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
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
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.abs.casino.common.util.Triple;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.betsoft.casino.mp.model.SpecialWeaponType.*;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {

    public EnemyGame(Logger logger, IGameConfigService gameConfigService) {
        super(logger, GameType.AMAZON, gameConfigService);
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
        boolean needKillEnemyAndGetFreeShots = featureBySid != null && (featureBySid.getId() == 43) && !isSpecialWeapon;
        boolean teststandNeedBronzeKey = featureBySid != null && (featureBySid.getId() == 48) && isPaidShot;
        boolean teststandNeedSilverKey = featureBySid != null && (featureBySid.getId() == 49) && isPaidShot;
        boolean teststandNeedGoldKey = featureBySid != null && (featureBySid.getId() == 50) && isPaidShot;
        boolean teststandNeedMaxDamageBoss = featureBySid != null && (featureBySid.getId() == 10);

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        Money payout = Money.ZERO;
        Money additionalPayout = Money.ZERO;
        boolean bossShouldBeAppeared = false;
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

        WeaponData weaponData = enemyData.getWeaponDataMap(weaponTypeId, levelId);
        logMessage(accountId, " currentEnergy: " + currentEnergy + " fullEnergy: " + fullEnergy
                + " levelId: " + levelId);

        boolean isIK = false;
        int chMult = 1;

        List<Integer> shotGems = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            shotGems.add(i, 0);
        }

        Map<Integer, Integer> seatGemsMap = seat.getSeatGems();

        if (seatGemsMap.isEmpty()) {
            for (int i = 0; i < shotGems.size(); i++) {
                seatGemsMap.put(i, 0);
            }
        }

        Money killAwardWin = Money.ZERO;
        Integer multiplierPay = 1;
        logMessage(accountId, "seatGemsMap old: " + seatGemsMap);
        boolean isWeaponCarrier = EnemyType.WEAPON_CARRIER.getId() == enemyType.getId();
        boolean isMultiplier = EnemyType.MULTIPLIER.getId() == enemyType.getId();
        boolean isExploder = EnemyType.EXPLODER.getId() == enemyType.getId();


        Money totalGemPayout = Money.ZERO;
        if (isBoss) {
            Map<Integer, List<Pair<Integer, Double>>> gemPayoutsByBoss = MathData.getGemPayoutsByBoss(weaponTypeId);
            int skinId = enemy.getSkin() - 1;
            for (int i = 0; i < shotGems.size(); i++) {
                List<Pair<Integer, Double>> gemData = gemPayoutsByBoss.get(i);
                logMessage(accountId, "gemData: " + gemData + " skinId: " + skinId);
                Pair<Integer, Double> bossData = gemData.get(skinId);
                double prob = bossData.getValue() / bossData.getKey() / 100;
                if (prob > 1) {
                    logMessage(accountId, "wrong gem prob: " + prob
                            + " weaponTypeId: " + weaponTypeId + " bossData: " + bossData);
                }
                if (RNG.rand() < prob) {
                    Integer mult = bossData.getKey();
                    logMessage(accountId, "gem appeared id:  " + i + " mult : " + mult);
                    shotGems.set(i, shotGems.get(i) + 1);
                    totalGemPayout = totalGemPayout.add(seat.getStake().getWithMultiplier(mult));
                    Integer old = seatGemsMap.get(i);
                    int newGemCount = old == null ? 1 : old + 1;
                    seatGemsMap.put(i, newGemCount);
                }
            }

            double hitProbabilityForBoss = MathData.getHitProbabilityForBoss(weaponTypeId, enemy.getSkin());
            isHit = RNG.rand() < hitProbabilityForBoss;

            logMessage(accountId, "boss hitProbabilityForBoss: " + hitProbabilityForBoss + " isHit: " + isHit);
            double win = 0;
            double realWin = 0;
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
                payout = stake.getWithMultiplier(win * chMult);

                double newEnergy = currentEnergy - hits;
                if (newEnergy <= 0) {
                    realWin = currentEnergy * MathData.PAY_HIT_PERCENT * chMult;
                    payout = stake.getWithMultiplier(realWin);
                    newEnergy = 0;
                    killAwardWin = stake.getWithMultiplier((hits - currentEnergy) * chMult * MathData.PAY_HIT_PERCENT);
                }
                enemy.setEnergy(newEnergy);
            }
            logMessage(accountId, "boss newEnergy : " + enemy.getEnergy() + " payout: " + payout
                    + " chMult: " + chMult + " win: " + win + " damage: " + damage + " realWin: " + realWin);

        } else {

            double rtpForWeapon = MathData.getRtpForWeapon(wpId);
            Double avgPayout = enemyData.getSwAvgPayouts(wpId, levelId, MathData.PAY_HIT_PERCENT);
            double hitProbability = rtpForWeapon / avgPayout;
            isHit = RNG.rand() < hitProbability;

            boolean needKillFromTeststand = teststandNeedKill || teststandNeedNewWeapons || needKillEnemyAndGetBoss;

            if (needKillFromTeststand) {
                isHit = true;
                logMessage(accountId, "teststand need kill");
            }


            boolean enemyWithoutIK = isWeaponCarrier || isMultiplier || isExploder;

            logMessage(accountId, " wpId : " + wpId + " rtpForWeapon: " + rtpForWeapon
                    + " avgPayout: " + avgPayout
                    + " hitProbability: "
                    + hitProbability
                    + " isHit: " + isHit
                    + " isMultiplier: " + isMultiplier
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
                logMessage(accountId, "IK prob: "  + prob + " isIK: " + isIK
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

                logMessage(accountId, " isCriticalHit: " + isCriticalHit
                        + " isTableWithWeapon: " + isTableWithWeapon + " chMult: " + chMult);

                if(hits == null){
                    logMessage(accountId, "error");
                }


                if(isMultiplier && isTableWithWeapon && hits > 0)
                    hits = (long) weaponData.getLimitForHitPointMap();

                double newEnergy = currentEnergy - hits;

                Pair<Double, Double> weaponDropData = MathData.getWeaponDropData(enemyType.getId(), weaponTypeId);
                Double avgWeaponAward = weaponDropData == null ? 0. : weaponDropData.getKey();

                if (isMultiplier && isTableWithWeapon) {
                    Map<Integer, Double> randomMultiplierForMultiplier = MathData.getRandomMultiplierForMultiplier();
                    multiplierPay = GameTools.getRandomNumberKeyFromMap(randomMultiplierForMultiplier);
                    logMessage(accountId, "multiplierPay updated  for Multiplier: " + multiplierPay);
                }


                damage = hits;

                if (isIK) {
                    damage = (long) currentEnergy;
                    isKilled = true;
                    enemy.setEnergy(0);
                    payout = stake.getWithMultiplier(currentEnergy * MathData.PAY_HIT_PERCENT); // base payout
                    double multiplier = isTableWithWeapon ? (hits - avgWeaponAward) : (hits);
                    logMessage(accountId, "multiplier: " + multiplier);
                    killAwardWin = stake.getWithMultiplier(multiplier * chMult * MathData.PAY_HIT_PERCENT  * multiplierPay);
                } else {
                    enemy.setEnergy(newEnergy);
                    isKilled = newEnergy <= 0;

                    double multBase = hits > currentEnergy ? currentEnergy : hits;
                    payout = stake.getWithMultiplier((multBase * MathData.PAY_HIT_PERCENT * chMult  * multiplierPay));

                    if (isKilled) {
                        newEnergy = 0;
                        enemy.setEnergy(newEnergy);
                        double multFromWeapon = isTableWithWeapon ? avgWeaponAward : 0;
                        double weaponPart = multFromWeapon * chMult;
                        double actual = hits - currentEnergy;
                        if(isExploder){
                            actual -= MathData.getAvgHpWinExploder();
                        }

                        logMessage(accountId, "multFromWeapon: " + multFromWeapon + " chMult: "
                                + chMult + " actual: " + actual);
                        double killAwardMult = (actual * chMult  * multiplierPay * MathData.PAY_HIT_PERCENT) -
                                (weaponPart * MathData.PAY_HIT_PERCENT);
                        killAwardWin = stake.getWithMultiplier(killAwardMult); // additional killed payout
                        logMessage(accountId, "killAwardMult: " + killAwardMult);
                    }
                    logMessage(accountId, "multBase: " + multBase);
                }

                if(isWeaponCarrier && isKilled){
                    if(payout.smallerThan(Money.ZERO))
                        payout = Money.ZERO;
                    if(killAwardWin.smallerThan(Money.ZERO))
                        killAwardWin = Money.ZERO;
                }

                boolean isEnemyWithoutWeapon = isExploder || isMultiplier;

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

        if (isHit)
            shootResult.setDamage(damage);

        int bossSkinId = -1;
        if (bossShouldBeAppeared) {
            Pair<Integer, Integer> bossParam = GameTools.getRandomPair(MathData.getBossParams());
            bossSkinId = bossParam.getKey();
            shootResult.setBossSkinId(bossSkinId);
            logMessage(accountId, "need generate boss: " + bossParam + " bossSkinId: " + bossSkinId);
        }


        if (isPaidShot && !isBoss) {
            boolean needTreasure = RNG.rand() < MathData.getProbTreasureForQuest();
            //      needTreasure = false;
            if (needTreasure) {
                Treasure randomTreasure = MathData.getRandomTreasure();
                shootResult.setPrize(randomTreasure.name());
                logMessage(accountId, "appeared usual treasure: " + randomTreasure);
            }

            if (featureBySid != null && featureBySid.getId() >= 200 && featureBySid.getId() <= 215) {
                Treasure treasure = Treasure.values()[featureBySid.getId() - 200];
                shootResult.setPrize(treasure.name());
                logMessage(accountId, " treasure from teststand: " + treasure + " featureBySid: " + featureBySid);
            }
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

        if (teststandNeedNewWeapons && !isExploder && !isBoss && !isMultiplier) {
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

            if (enemyType.equals(EnemyType.MULTIPLIER) && multiplierPay > 1) {
                shootResult.setMultiplierPay(multiplierPay);
            }
        }

        shootResult.setTotalGemsPayout(totalGemPayout);
        shootResult.setGems(shotGems);
        logMessage(accountId, "totalGemPayout: " + totalGemPayout + " shotGems: " + shotGems);

        if (isBoss && isKilled) {
            for (int i = 0; i < shotGems.size(); i++) {
                seatGemsMap.put(i, 0);
            }
        }

        logMessage(accountId, "seatGemsMap new: " + seatGemsMap);


        if (isKilled && enemyType.equals(EnemyType.EXPLODER)) {
            Map<Integer, Double> totalHPdamageForExplorer = MathData.getTotalHPdamageForExplorer();
            Integer randomNumberKeyFromMap = GameTools.getRandomNumberKeyFromMap(totalHPdamageForExplorer);
            if (randomNumberKeyFromMap != null) {
                shootResult.setNeedExplodeHP(randomNumberKeyFromMap);
                shootResult.setNeedExplode(true);
            } else {
                logMessage(accountId, "randomNumberKeyFromMap for Exploder is null, totalHPdamageForExplorer: " + totalHPdamageForExplorer);
            }
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
            compensateWin = seat.getStake().multiply((hits - currentEnergy) * MathData.PAY_HIT_PERCENT);
        }

        double win = MathData.PAY_HIT_PERCENT * realHits;

        enemy.setEnergy(newEnergy);
        payout = seat.getStake().getWithMultiplier(win);
        logMessage(accountId, "doShootWithExplode newEnergy : " + enemy.getEnergy()
                + " payout: " + payout + " compensateWin: " + compensateWin + " realHits: " + realHits + " hits: " + hits);

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
