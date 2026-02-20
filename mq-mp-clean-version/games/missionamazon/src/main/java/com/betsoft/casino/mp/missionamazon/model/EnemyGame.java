package com.betsoft.casino.mp.missionamazon.model;

import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.common.AbstractEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.missionamazon.model.math.BossType;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyRange;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyType;
import com.betsoft.casino.mp.missionamazon.model.math.MathData;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.WeaponDrop;
import com.betsoft.casino.mp.model.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {
    private final IGameConfigProvider gameConfigProvider;
    private final ISpawnConfigProvider spawnConfigProvider;

    public EnemyGame(Logger logger, IGameConfigService<? extends IGameConfigService<?>> gameConfigService,
                     IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(logger, GameType.MISSION_AMAZON, gameConfigService);
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
                                         ITransportObjectsFactoryService toService, boolean isMainShot, boolean isBossTarget) throws CommonException {
        GameConfig config = getConfig(seat);

        long accountId = seat.getAccountId();
        logMessage(accountId, "shootBaseEnemy started, weapon: {}", weapon);

        int betLevel = getBetLevelWithCheck(seat);
        IShot actualShot = getShotWithCheck(seat);

        boolean paidSpecialShot = actualShot.isPaidSpecialShot();
        logMessage(accountId, "paidSpecialShot: {} seat.getBetLevel(): {}", paidSpecialShot, betLevel);

        String sessionId = seat.getPlayerInfo().getSessionId();
        MissionAmazonTestStand testStand = getTestStand(accountId, sessionId);

        boolean isSpecialWeapon = (weapon != null);
        boolean isBoss = enemy.isBoss();
        boolean isPaidShot = !isSpecialWeapon;

        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        List<Integer> shotGems = initializeShotGems();
        Money totalGemPayout = Money.ZERO;

        Money payout = Money.ZERO;
        boolean isKilled = false;
        boolean isHit = false;

        EnemyType enemyType = enemy.getEnemyType();

        int chMultiplier = 1;
        Money killAwardWin = Money.ZERO;

        if (isBoss) {
            BossType bossType = BossType.getBySkinId(enemy.getSkin());
            int randomBossPay = testStand.isNeedKill()
                    ? config.getBossParams().getBossHP().get(bossType.getSkinId())
                    : MathData.getRandomBossSmallPay(config, bossType, weaponTypeId);
            double hitProbabilityForBoss = testStand.isNeedKill() ? 1
                    : MathData.getHitSmallProbabilityForBoss(config, bossType, weaponTypeId, randomBossPay, isBossTarget);

            int winFromTestStand = testStand.getWin();
            if (winFromTestStand != 0) {
                randomBossPay = winFromTestStand;
                hitProbabilityForBoss = 1;
                logMessage(accountId, "testStand, big/huge/mega win: {}", randomBossPay);
            }

            boolean needKill = false;
            if (!testStand.isNotNeedAnyWin()) {
                if (RNG.rand() < hitProbabilityForBoss) {
                    double newEnergy = enemy.getEnergy() - randomBossPay;
                    if (newEnergy < 0) {
                        newEnergy = 0;
                    }
                    enemy.setEnergy(newEnergy);
                    needKill = newEnergy == 0;
                    payout = stake.getWithMultiplier(randomBossPay * betLevel);
                    seat.addTotalBossPayout(stake.getWithMultiplier(randomBossPay * betLevel).toCents());

                    logMessage(accountId, "needKill: {}, randomBossPay: {}, betLevel: {}, newEnergy: {}",
                            needKill, randomBossPay, betLevel, newEnergy);
                }
                isKilled = needKill || testStand.isNeedKill();
            }

            if (payout.greaterThan(Money.ZERO) || needKill) {
                logMessage(accountId, "boss hitProbabilityForBoss: {}, randomBossPay: {}, " +
                                "payout: {}, killAwardWin: {}, " +
                                " seat.getTotalBossPayout(): {}, needKill: {}",
                        hitProbabilityForBoss,
                        randomBossPay,
                        payout.toDoubleCents(),
                        killAwardWin.toDoubleCents(), seat.getTotalBossPayout(), needKill);
            }
        } else {
            int idxData = 0;
            logMessage(accountId, "idxData for enemy " + idxData);

            double hitProbability = MathData.getHitProbability(config, weaponTypeId, enemyType, idxData, isBossTarget);
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

            if (testStand.isNeedNewWeaponsWithKill()) {
                isHit = true;
                logMessage(accountId, "teststand isNeedNewWeaponsWithKill isHit is true");
            }

            if (testStand.isNeedNewWeaponsWithoutKill()) {
                isHit = false;
                logMessage(accountId, "isNeedNewWeaponsWithoutKill isHit is false");
            }

            logMessage(accountId, "wpId: {}, hitProbability: {}, isHit: {}", weaponTypeId, hitProbability, isHit);

            if (isHit) {
                isKilled = true;
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponTypeId, 1);
                chMultiplier = testStand.isNeedCriticalHit() ? RNG.nextInt(2, 4) : MathData.getRandomMultForWeapon(config, weaponTypeId, isBossTarget);
                int enemyPayout = MathData.getEnemyPayout(config, enemyType, idxData);

                int winFromTestStand = testStand.getWin();
                if (winFromTestStand != 0) {
                    chMultiplier = 1;
                    enemyPayout = winFromTestStand;
                    logMessage(accountId, "testStand big/huge/mega win: {}", enemyPayout);
                }

                boolean isCriticalHit = chMultiplier != 1;
                logMessage(accountId, "isCriticalHit: {} chMultiplier: {}", isCriticalHit, chMultiplier);
                enemy.setEnergy(0);
                payout = stake.getWithMultiplier(enemyPayout * chMultiplier * betLevel);
                if (hitProbability > 1) {
                    double diff = hitProbability - 1;
                    killAwardWin = stake.getWithMultiplier(enemyPayout * diff * chMultiplier * betLevel);
                    logMessage(accountId, "hit prob more 1, compensate to killAward, diff: {}, killAwardWin: {}, enemyPayout: {}",
                            diff, killAwardWin, enemyPayout);
                }
            }
        }

        double probGem = MathData.getGemDropProbability(config, weaponTypeId, isBossTarget);
        boolean needGem = isMainShot && (((weaponTypeId == MathData.TURRET_WEAPON_ID || paidSpecialShot)
                && RNG.rand() < probGem) || testStand.isNeedGem());
        logMessage(accountId, "needGem: {}, probGem: {}", needGem, probGem);

        if (needGem) {
            Map<Integer, Integer> seatGemsMap = initializeSeatGems(seat);
            Gem gemDrop = MathData.getGemByBetLevel(betLevel);
            seat.addGem(gemDrop.getId(), 1);
            addShotGem(shotGems, gemDrop);
            totalGemPayout = getGemPayout(seatGemsMap, config, stake, betLevel);
        }

        double probWeapon;
        Double rtpForWeapon;
        double avgDropPrice = MathData.calculateAverageDropPrice(config, isBossTarget);
        Double averageDamageForWeapon = MathData.getAverageDamageForWeapon(config, weaponTypeId, isBossTarget);
        if (isBoss && weaponTypeId == -1) {
            rtpForWeapon = 3. / 100;
            probWeapon = (rtpForWeapon / 60.437175);
        } else {
            rtpForWeapon = MathData.getRtpForDropWeapon(config, enemyType, weaponTypeId, 0, isBossTarget);
            probWeapon = rtpForWeapon / avgDropPrice / averageDamageForWeapon;
        }

            boolean shootFromWCDrop = seat.containsWeapon(weaponTypeId);
            boolean needWeapon = !shootFromWCDrop && RNG.rand() < probWeapon || testStand.isNeedNewWeaponsWithKill() || testStand.isNeedNewWeaponsWithoutKill();
            logMessage(accountId, "needWeapon: {}, avgDropPrice: {}, rtpForWeapon: {}, probWeapon: {}, " +
                    "averageDamageForWeapon: {}, shootFromWCDrop: {}", needWeapon, avgDropPrice, rtpForWeapon, probWeapon, averageDamageForWeapon, shootFromWCDrop);

            if (testStand.isNotNeedAnyWin()) {
                needWeapon = false;
                logMessage(accountId, "teststand isNotNeedAnyWin needWeapon is false");
            }

            //needWeapon = false;
            if (testStand.isNeedNewWeaponsWithKill() || testStand.isNeedNewWeaponsWithoutKill()) {
                awardedWeapons.add(toService.createWeapon(testStand.getSpecialWeaponType().getId(), 10));
            } else if (needWeapon) {
                WeaponDrop drop = MathData.getRandomWeaponDrop(config, isBossTarget);
                awardedWeapons.add(toService.createWeapon(drop.getType(), drop.getAmount()));
            }


        if (EnemyRange.WEAPON_CARRIERS.contains(enemyType) && isKilled) {
            WeaponDrop weaponCarrierDrop = MathData.getWeaponCarrierDrop(config, enemyType, isBossTarget);
            awardedWeapons.add(toService.createWeapon(weaponCarrierDrop.getType(), weaponCarrierDrop.getAmount()));
            seat.addWC(weaponCarrierDrop.getType(), weaponCarrierDrop.getAmount());
        }


        boolean bossShouldBeAppeared = !isBossRound && testStand.isNeedSpawnBoss();

        logMessage(accountId, "win: {}", payout);

        ShootResult shootResult = new ShootResult(isPaidShot ? stake : Money.ZERO, payout, bossShouldBeAppeared,
                isKilled, enemy);
        shootResult.setChMult(chMultiplier);

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

    private Map<Integer, Integer> initializeSeatGems(Seat seat) {
        Map<Integer, Integer> gemsMap = seat.getSeatGems();
        if (gemsMap.isEmpty()) {
            Arrays.stream(Gem.values()).forEach(gem -> gemsMap.put(gem.getId(), 0));
        }
        return gemsMap;
    }

    private List<Integer> initializeShotGems() {
        List<Integer> shotGems = new ArrayList<>();
        for (int i = 0; i < Gem.values().length; i++) {
            shotGems.add(0);
        }
        return shotGems;
    }

    private void addShotGem(List<Integer> shotGems, Gem gem) {
        shotGems.set(gem.getId(), shotGems.get(gem.getId()) + 1);
    }

    private Money getGemPayout(Map<Integer, Integer> seatGemsMap, GameConfig config, Money stake, int betLevel) {
        for (Map.Entry<Integer, Integer> gem : seatGemsMap.entrySet()) {
            if (gem.getValue() >= config.getQuestParams().getCollectToWin()) {
                gem.setValue(0);
                return stake.getWithMultiplier(MathData.getGemPrize(config) * betLevel);
            }
        }
        return Money.ZERO;
    }

    private MissionAmazonTestStand getTestStand(long accountId, String sessionId) {
        if (sessionId != null) {
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(accountId, " testStand feature: {}", featureBySid);
            if (featureBySid != null) {
                return new MissionAmazonTestStand(featureBySid);
            }
        }
        return new MissionAmazonTestStand();
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
                               ITransportObjectsFactoryService toService, boolean isMainShot, boolean isBossTarget) throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        if (debug) {
            logMessage(seat.getAccountId(), "doShoot, sessionId: " + sessionId
                    + " seat.getCurrentWeaponId(): " + seat.getCurrentWeaponId()
            );
        }

        Weapon weapon = seat.getCurrentWeapon();
        logMessage(accountId, "doShoot,  weapon: " + weapon);

        ShootResult res = shootBaseEnemy(seat, weapon, stake, enemy, isBossRound, toService, isMainShot, isBossTarget);

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
                .getConfig(GameType.MISSION_AMAZON.getGameId(), seat.getPlayerInfo().getRoomId());
    }

    public GameConfig getConfig(long roomId) {
        return (GameConfig) gameConfigProvider.getConfig(GameType.MISSION_AMAZON.getGameId(), roomId);
    }

    @Override
    public IGameConfig getGameConfig(long roomId) {
        return getConfig(roomId);
    }

    @Override
    public SpawnConfig getSpawnConfig(long roomId) {
        return (SpawnConfig) spawnConfigProvider.getConfig(GameType.MISSION_AMAZON.getGameId(), roomId);
    }
}
