package com.betsoft.casino.mp.bgmissionamazon.model;

import com.betsoft.casino.mp.bgmissionamazon.model.math.*;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.GemDrop;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.WeaponDrop;
import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.common.AbstractEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
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

import static com.betsoft.casino.mp.common.AbstractActionSeat.ADD_COUNTER_POWER_UP_MULT;

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
                                         ITransportObjectsFactoryService toService, boolean isMainShot, int scoreIdx, boolean isBossTarget) throws CommonException {
        GameConfig config = getConfig(seat);

        long accountId = seat.getAccountId();
        logMessage(accountId, "shootBaseEnemy started, weapon: {}", weapon);

        int betLevel = getBetLevelWithCheck(seat);

        ArrayList<Integer> possibleBetLevels = new ArrayList<>(MathData.getPossibleBetLevels());
        int idxBetLevel = possibleBetLevels.indexOf(betLevel);
        SpecialWeaponType weaponType = weapon == null ? null : weapon.getType();
        boolean isSpecialWeapon = weapon != null;

        SpecialWeaponType baseSpecialWeaponType = weaponType;
        if (isSpecialWeapon)
            baseSpecialWeaponType = MathData.getBaseSpecialWeaponType(weaponType);

        logMessage(accountId, "idxBetLevel: {} seat.getBetLevel(): {}, weaponType: {}, baseSpecialWeaponType: {}",
                idxBetLevel, betLevel, weaponType, baseSpecialWeaponType);

        String sessionId = seat.getPlayerInfo().getSessionId();
        BGMissionAmazonTestStand testStand = getTestStand(accountId, sessionId);

        boolean isBoss = enemy.isBoss();
        boolean isPaidShot = !isSpecialWeapon;

        List<ITransportWeapon> awardedWeapons = new ArrayList<>();

        List<Integer> shotGems = initializeShotGems();
        Money totalGemPayout = Money.ZERO;

        Money payout = Money.ZERO;
        boolean isKilled = false;
        boolean isHit = false;

        EnemyType enemyType = enemy.getEnemyType();

        int chMultiplier = 1;

        int additionalPowerUpMult = 1;
        if (isSpecialWeapon && weapon.getType().isPowerUp()) {
            int powerMult = seat.getAdditionalTempCounters(ADD_COUNTER_POWER_UP_MULT);
            if (powerMult > 1) {
                additionalPowerUpMult = powerMult;
            }
        }
        Money killAwardWin = Money.ZERO;

        if (isBoss) {
            BossType bossType = BossType.getBySkinId(enemy.getSkin());
            int randomBossPay = testStand.isNeedKill()
                    ? config.getBossParams().getBossHP().get(bossType.getSkinId())
                    : MathData.getRandomBossSmallPay(config, weaponType);
            double hitProbabilityForBoss = testStand.isNeedKill() ? 1
                    : MathData.getHitSmallProbabilityForBoss(config, weaponType, randomBossPay, isBossTarget);

            int winFromTestStand = testStand.getWin();
            if (winFromTestStand != 0) {
                randomBossPay = winFromTestStand;
                hitProbabilityForBoss = 1;
                logMessage(accountId, "testStand, big/huge/mega win: {}", randomBossPay);
            }

            long currentTotalWin = 0;
            boolean needKill = false;
            if (!testStand.isNotNeedAnyWin()) {
                if (RNG.rand() < hitProbabilityForBoss) {
                    payout = stake.getWithMultiplier(randomBossPay * betLevel * additionalPowerUpMult);
                    seat.addTotalBossPayout(payout.toCents());
                    double newEnergy = enemy.getEnergy() - randomBossPay;
                    if (newEnergy < 0) {
                        newEnergy = 0;
                    }
                    enemy.setEnergy(newEnergy);
                    needKill = newEnergy == 0;
                    logMessage(accountId, "needKill: {}, randomBossPay: {}, betLevel: {}, newEnergy: {}",
                            needKill, randomBossPay, betLevel, newEnergy);
                }

                if (needKill || testStand.isNeedKill()) {
                    currentTotalWin = seat.getTotalBossPayout();
                    int defeatBossMultiplierFromConfig = MathData.getRandomDefeatBossMultiplier(config);
                    int actualBossMultiplier = defeatBossMultiplierFromConfig <= 1 ? 0 : defeatBossMultiplierFromConfig - 1;
                    killAwardWin = Money.fromCents(currentTotalWin * actualBossMultiplier);
                    isKilled = true;
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

            double hitProbability = MathData.getHitProbability(config, weaponType, enemyType, idxData, isBossTarget);
            isHit = RNG.rand() < hitProbability;

            if (testStand.isNeedHit()) {
                logMessage(accountId, "Hit from TestStand");
                isHit = true;
            }

            if (testStand.isNeedKill()) {
                isHit = true;
                logMessage(accountId, "teststand need kill");
            }

            if (testStand.isNotNeedAnyWin()) {
                isHit = false;
                logMessage(accountId, "teststand isNotNeedAnyWin isHit is false");
            }

            if (testStand.isNeedCriticalHit() && weaponType != null && !SpecialWeaponType.LevelUp.equals(weaponType)) {
                isHit = true;
                logMessage(accountId, "teststand need CH");
            }

            if (testStand.isNeedNewWeaponsWithKill()) {
                isHit = true;
                logMessage(accountId, "isNeedNewWeaponsWithKill isHit is true");
            }

            if (testStand.isNeedNewWeaponsWithoutKill()) {
                isHit = false;
                logMessage(accountId, "isNeedNewWeaponsWithoutKill isHit is false");
            }

            logMessage(accountId, "weaponType: {}, hitProbability: {}, isHit: {}, additionalPowerUpMult: {}"
                    , weaponType, hitProbability, isHit, additionalPowerUpMult);

            if (isHit) {
                isKilled = true;
                seat.getCurrentPlayerRoundInfo().addMathHitCounter(weaponType == null ? -1 : weaponType.getId(), 1);
                chMultiplier = testStand.isNeedCriticalHit() ? RNG.nextInt(2, 4) : MathData.getRandomMultForWeapon(config, weaponType, isBossTarget);
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
                payout = stake.getWithMultiplier(enemyPayout * chMultiplier * betLevel * additionalPowerUpMult);
                if (hitProbability > 1) {
                    double diff = hitProbability - 1;
                    killAwardWin = stake.getWithMultiplier(enemyPayout * diff * chMultiplier * betLevel * additionalPowerUpMult);
                    logMessage(accountId, "hit prob more 1, compensate to killAward, diff: {}, killAwardWin: {}, enemyPayout: {}",
                            diff, killAwardWin, enemyPayout);
                }
            }
        }

        double probGem = MathData.getGemDropProbability(config, -1, isBossTarget);
        boolean needGem = !isSpecialWeapon && (RNG.rand() < probGem || testStand.isNeedGem());
        logMessage(accountId, "needGem: {}, probGem: {}", needGem, probGem);

        if (needGem) {
            Map<Integer, Integer> seatGemsMap = initializeSeatGems(seat);
            GemDrop gemDrop = MathData.getRandomGemDrop(config);
            seat.addGem(gemDrop.getType(), 1);
            addShotGem(shotGems, Gem.getById(gemDrop.getType()));
            totalGemPayout = getGemPayout(seatGemsMap, config, stake, gemDrop, 3);
        }

        double probWeapon;
        boolean isMaxLevel = seat.getBetLevel() == Collections.max(MathData.getPossibleBetLevels());

        if (!isSpecialWeapon) {
            probWeapon = isBoss ? 0.007061183941407 : config.getEnemyData(enemyType, 0).getBaseTurretSWDrop();
        } else {
            Map<SpecialWeaponType, Double> data;
            if (isBossTarget) {
                data = isMaxLevel ? config.getPSWDropsReTriggerTwoTargetBoss() :
                        config.getPSWDropsReTriggerOneTargetBoss();
            } else {
                data = isMaxLevel ? config.getPSWDropsReTriggerTwoTargetEn() :
                        config.getPSWDropsReTriggerOneTargetEn();
            }

            probWeapon = data.containsKey(baseSpecialWeaponType) ? data.get(baseSpecialWeaponType) : 0;
        }

        boolean needWeapon = isMainShot && (RNG.rand() < probWeapon || testStand.isNeedNewWeaponsWithKill() || testStand.isNeedNewWeaponsWithoutKill());
        int weaponTypeId = isSpecialWeapon ? weapon.getType().getId() : -1;
        boolean needLevelUp = testStand.isNeedLevelUp() && (weaponTypeId == -1 || weaponTypeId == SpecialWeaponType.LevelUp.getId());
        logMessage(accountId, "needWeapon: {}, testStand.isNeedNewWeapons(): {}, probWeapon: {}, isMainShot: {} " +
                "averageDamageForWeapon: {}", needWeapon, testStand.isNeedNewWeaponsWithKill(), probWeapon, isMainShot);


        if (testStand.isNotNeedAnyWin()) {
            needWeapon = false;
            logMessage(accountId, "teststand isNotNeedAnyWin needWeapon is false");
        }

        if (needWeapon || needLevelUp) {
            WeaponDrop drop = MathData.getRandomWeaponDrop(config, weaponType == null ? -1 : baseSpecialWeaponType.getId(), isBossTarget);
            int weaponId = drop.getType().getId();
            int newSpecialWeaponShots = drop.getAmount();
            if (needLevelUp) {
                weaponId = SpecialWeaponType.LevelUp.getId();
                newSpecialWeaponShots = 20;
            } else if (testStand.isNeedNewWeaponsWithKill() || testStand.isNeedNewWeaponsWithoutKill()) {
                weaponId = testStand.getSpecialWeaponType().getId();
                newSpecialWeaponShots = 10;
            }
            awardedWeapons.add(toService.createWeapon(weaponId, newSpecialWeaponShots));
        }

        if (EnemyRange.WEAPON_CARRIERS.contains(enemyType) && isKilled) {
            WeaponDrop weaponCarrierDrop = MathData.getWeaponCarrierDrop(config, enemyType, isBossTarget);
            seat.resetAdditionalTempCounter(ADD_COUNTER_POWER_UP_MULT);
            Map<Integer, Map<Integer, Double>> powerUpMultipliers = config.getPowerUpMultipliers();
            Map<Integer, Double> multipliersForCurrentScore = powerUpMultipliers.get(scoreIdx);
            Integer currentPowerMult = GameTools.getRandomNumberKeyFromMapWithNorm(multipliersForCurrentScore);
            seat.addAdditionalTempCounter(ADD_COUNTER_POWER_UP_MULT, currentPowerMult);

            logMessage(accountId, "multipliersForCurrentScore: {}, currentPowerMult: {} ", multipliersForCurrentScore, currentPowerMult);
            awardedWeapons.add(toService.createWeapon(weaponCarrierDrop.getType().getId(), weaponCarrierDrop.getAmount()));
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

    private Money getGemPayout(Map<Integer, Integer> seatGemsMap, GameConfig config, Money stake, GemDrop gemDrop, int betLevel) {
        for (Map.Entry<Integer, Integer> gem : seatGemsMap.entrySet()) {
            if (gem.getValue() >= config.getQuestParams().getCollectToWin()) {
                gem.setValue(0);
                return stake.getWithMultiplier((double) gemDrop.getPrize() * betLevel);
            }
        }
        return Money.ZERO;
    }

    private BGMissionAmazonTestStand getTestStand(long accountId, String sessionId) {
        if (sessionId != null) {
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(accountId, " testStand feature: {}", featureBySid);
            if (featureBySid != null) {
                return new BGMissionAmazonTestStand(featureBySid);
            }
        }
        return new BGMissionAmazonTestStand();
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
                               ITransportObjectsFactoryService toService, boolean isMainShot, int scoreIdx, boolean isBossTarget) throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        if (debug) {
            logMessage(seat.getAccountId(), "doShoot, sessionId: " + sessionId
                    + " seat.getCurrentWeaponId(): " + seat.getCurrentWeaponId()
            );
        }

        Weapon weapon = seat.getCurrentWeapon();
        logMessage(accountId, "doShoot,  weapon: " + weapon);

        ShootResult res = shootBaseEnemy(seat, weapon, stake, enemy, isBossRound, toService, isMainShot, scoreIdx, isBossTarget);

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
                .getConfig(GameType.BG_MISSION_AMAZON.getGameId(), seat.getPlayerInfo().getRoomId());
    }

    public GameConfig getConfig(long roomId) {
        return (GameConfig) gameConfigProvider.getConfig(GameType.BG_MISSION_AMAZON.getGameId(), roomId);
    }

    @Override
    public IGameConfig getGameConfig(long roomId) {
        return getConfig(roomId);
    }

    @Override
    public SpawnConfig getSpawnConfig(long roomId) {
        return (SpawnConfig) spawnConfigProvider.getConfig(GameType.BG_MISSION_AMAZON.getGameId(), roomId);
    }
}
