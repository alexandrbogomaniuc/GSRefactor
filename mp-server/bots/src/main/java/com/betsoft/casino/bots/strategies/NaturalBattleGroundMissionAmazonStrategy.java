package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.bots.model.EnemySize;
import com.betsoft.casino.bots.requests.SwitchWeaponRequest;
import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyType;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.transport.PointExt;
import com.betsoft.casino.mp.transport.RoomEnemy;
import com.betsoft.casino.utils.TObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.betsoft.casino.bots.model.Turret.DEFAULT_WEAPON_ID;

public class NaturalBattleGroundMissionAmazonStrategy extends BattleGroundMissionAmazonStrategy implements IRoomNaturalBotStrategy {

    private static final Logger LOG = LogManager.getLogger(NaturalBattleGroundMissionAmazonStrategy.class);
    private int shots = 0;
    private int activeWeaponId = DEFAULT_WEAPON_ID;
    private final int stakesLimit;
    private final Map<Integer, Integer> weapons = new HashMap<>();
    private int requestedBetLevel = 1;
    private boolean debug;
    private final HashMap<String, Long> timesMinResponseByType;
    private final HashMap<String, Long> timesLastShootResponseByType;
    private final HashMap<String, Long> timesLastShootRequestByType;
    private final HashMap<String, Long> timesLastBulletResponseByType;
    private final HashMap<String, Long> timesLastBulletRequestByType;
    private final HashMap<String, Long> timesLastOtherResponseByType;
    private final HashMap<String, Long> timesLastOtherRequestByType;
    private ILobbyBot lobbyBot;

    private static final Map<Long, EnemySize> ENEMY_SIZES_MAP = new HashMap();

    public NaturalBattleGroundMissionAmazonStrategy(int stakesLimit, int requestedBetLevel, long requestedByInAmount) {
        super(stakesLimit, requestedBetLevel, true, requestedByInAmount);
        this.stakesLimit = stakesLimit;
        this.requestedBetLevel = requestedBetLevel;
        this.debug = true;

        weapons.put(DEFAULT_WEAPON_ID, 0);
        for (SpecialWeaponType type : SpecialWeaponType.values()) {
            if (type.getAvailableGameIds().contains((int) GameType.BG_MISSION_AMAZON.getGameId())
                    && !type.isInternalServerShot())
                weapons.put(type.getId(), 0);
        }

        timesMinResponseByType =  new HashMap<>();
        timesLastShootResponseByType = new HashMap<>();
        timesLastShootRequestByType = new HashMap<>();
        timesLastBulletResponseByType = new HashMap<>();
        timesLastBulletRequestByType = new HashMap<>();
        timesLastOtherResponseByType = new HashMap<>();
        timesLastOtherRequestByType = new HashMap<>();

        addMetric(BaseMetricTimeKey.PLAY_STARTED.name(), 4700L);

        addMetric(SwitchWeaponRequest.METRIC, (long) TObject.FREQUENCY_LIMIT);

        addMetric("PISTOL", 150L);
        addMetric(SpecialWeaponType.LevelUp.name(), 150L);

        for (SpecialWeaponType value : SpecialWeaponType.values()) {
            if (value.getAvailableGameIds().contains((int) GameType.BG_MISSION_AMAZON.getGameId())) {
                switch (value) {
                    case Cryogun:
                    case PowerUp_Cryogun:
                        addMetric(value.name(), 978L);
                        break;
                    case Flamethrower:
                    case Ricochet:
                    case PowerUp_Flamethrower:
                    case PowerUp_Laser:
                    case Plasma:
                    case PowerUp_Plasma:
                        addMetric(value.name(), 1100L);
                        break;
                    case ArtilleryStrike:
                    case PowerUp_ArtilleryStrike:
                        addMetric(value.name(), 1700L);
                        break;
                    case Railgun:
                        addMetric(value.name(), 500L);
                        break;
                    default:
                        break;
                }
            }
        }

        ENEMY_SIZES_MAP.put(0L, new EnemySize(0, EnemyType.SKULL_BREAKER.getName(), 312.772103445871, 410.971539633615, 0.48, 265.001098632813, 402.34546661377, 354.297424316406 , 426.54167175293));//"Cerulean Skullbreaker"
        ENEMY_SIZES_MAP.put(1L, new EnemySize(1, EnemyType.WITCH.getName(), 190.117630004883, 410.021183013916, 0.44, 190.117630004883, 410.021183013916, 190.117630004883, 410.021183013916));//"Vine Witch"
        ENEMY_SIZES_MAP.put(2L, new EnemySize(2, EnemyType.GUARDIAN.getName(), 184.042891446282, 306.562862321442, 0.49, 142.052276611328, 290.298669815063, 231.587757110596 , 381.403995513916));//"Crazed Guardian"
        ENEMY_SIZES_MAP.put(3L, new EnemySize(3, EnemyType.RUNNER.getName(), 187.126907941115, 281.85761940905, 0.46, 107.033889770508, 234.640312671661, 234.395057678223, 348.151935577393));//"Jungle Runner"
        ENEMY_SIZES_MAP.put(4L, new EnemySize(4, EnemyType.JAGUAR.getName(), 370.534499969482, 281.717177276611, 0.44, 275.048812866211, 244.625366210938, 460.247009277344, 332.191024780273));//"Stalking Jaguar"
        ENEMY_SIZES_MAP.put(5L, new EnemySize(5, EnemyType.SERPENT.getName(), 233.743186576694, 149.302722656649, 0.44, 219.506816864014, 120.086486816406, 239.025615692139, 173.737236022949));//"Slithering Serpent"
        ENEMY_SIZES_MAP.put(6L, new EnemySize(6, EnemyType.ANT.getName(), 777.746398501414, 526.666136311504, 0.08, 633.304412841797, 498.413970947266, 1198.86184692383, 628.418426513672));//"Carnivorous Ant"
        ENEMY_SIZES_MAP.put(7L, new EnemySize(7, EnemyType.WASP.getName(), 639.78597388738, 485.488146572647, 0.14, 465.257476806641, 374.990379333496, 713.423217773438, 550.347381591797));//"Venomous Wasp"
        ENEMY_SIZES_MAP.put(8L, new EnemySize(8, EnemyType.ARMED_WARRIOR.getName(), 267.473430887858, 381.567364332411, 0.48, 251.57933807373, 369.320377349854, 305.949417114258, 405.184417724609));//"Armed Warrior"
        ENEMY_SIZES_MAP.put(9L, new EnemySize(9, EnemyType.EXPLODING_TOAD.getName(), 1216.09813605415, 702.011603284765, 0.13, 919.723602294922, 423.101608276367, 1529.37634277344, 1188.59436035156));//"Exploding Toad"
        ENEMY_SIZES_MAP.put(10L, new EnemySize(10, EnemyType.SCORPION.getName(), 486.114157104492, 373.812675170898, 0.26, 465.024780273438, 357.377975463867, 524.207595825195, 383.337753295898));//"Spirit Scorpion"
        ENEMY_SIZES_MAP.put(11L, new EnemySize(11, EnemyType.TINY_TOAD.getName(), 97.01, 56.28, 1, 97.01, 56.28, 97.01, 56.28));//"Tiny Toad"
        ENEMY_SIZES_MAP.put(12L, new EnemySize(12, EnemyType.FLOWERS_1.getName(), 550.826023101807, 527.671812057495, 0.22, 547.995559692383, 522.233642578125, 553.551849365234, 532.769931793213));//"Noxious Growth"
        ENEMY_SIZES_MAP.put(13L, new EnemySize(13, EnemyType.FLOWERS_2.getName(), 523.710077700408, 528.232848291812, 0.22, 97.4283294677734, 123.199199676514, 591.983673095703, 658.542205810547));//"Invasive poisoner"
        ENEMY_SIZES_MAP.put(14L, new EnemySize(14, EnemyType.PLANT_1.getName(), 659.368144485186, 939.794950665168, 0.172, 631.715850830078, 862.680694580078, 680.130798339844, 977.532104492188));//"Crimson Chomper"
        ENEMY_SIZES_MAP.put(15L, new EnemySize(15, EnemyType.PLANT_2.getName(), 661.839660644531, 942.448820260855, 0.172, 635.497161865234, 873.522369384766, 680.403717041016, 977.522155761719));//"Emerald Maneater"
        ENEMY_SIZES_MAP.put(16L, new EnemySize(16, EnemyType.WEAPON_CARRIER_1.getName(), 130, 130, 1, 130, 130, 130, 130));//"Weapon Carrier Artillery Strike"
        ENEMY_SIZES_MAP.put(17L, new EnemySize(17, EnemyType.WEAPON_CARRIER_2.getName(), 130, 130, 1, 130, 130, 130, 130));//"Weapon Carrier Flamethrower"
        ENEMY_SIZES_MAP.put(18L, new EnemySize(18, EnemyType.WEAPON_CARRIER_3.getName(), 130, 130, 1, 130, 130, 130, 130));//"Weapon Carrier Cryogun"
        ENEMY_SIZES_MAP.put(19L, new EnemySize(19, EnemyType.WEAPON_CARRIER_4.getName(), 130, 130, 1, 130, 130, 130, 130));//"Weapon Carrier Laser"
        ENEMY_SIZES_MAP.put(20L, new EnemySize(20, EnemyType.WEAPON_CARRIER_5.getName(), 130, 130, 1, 130, 130, 130, 130));//"Weapon Carrier Plasma Gun"

        ENEMY_SIZES_MAP.put(21L, new EnemySize(21, EnemyType.BOSS.getName(), 359.29, 172.46, 1, 359.29, 172.46, 359.29, 172.46));//"Boss"
    }

    private void addMetric(String metric, Long minValue) {
        LOG.debug("addMetric: metric:{}, minValue:{}", metric, minValue);

        timesMinResponseByType.put(metric, minValue);
        timesLastShootResponseByType.put(metric, 0L);
        timesLastShootRequestByType.put(metric, 0L);
        timesLastBulletResponseByType.put(metric, 0L);
        timesLastBulletRequestByType.put(metric, 0L);
        timesLastOtherResponseByType.put(metric, 0L);
        timesLastOtherRequestByType.put(metric, 0L);
    }

    @Override
    public void updateShootResponseTimeMetric(String metric, Long value){
        LOG.debug("updateShootResponseTimeMetric: metric:{}, value:{}", metric, value);
        timesLastShootResponseByType.put(metric, value);
    }

    @Override
    public void updateShootRequestTimeMetric(String metric, Long value){
        LOG.debug("updateShootRequestTimeMetric: metric:{}, value:{}", metric, value);
        timesLastShootRequestByType.put(metric, value);
    }

    @Override
    public void updateBulletResponseTimeMetric(String metric, Long value){
        LOG.debug("updateBulletResponseTimeMetric: metric:{}, value:{}", metric, value);
        timesLastBulletResponseByType.put(metric, value);
    }

    @Override
    public void updateBulletRequestTimeMetric(String metric, Long value){
        LOG.debug("updateBulletRequestTimeMetric: metric:{}, value:{}", metric, value);
        timesLastBulletRequestByType.put(metric, value);
    }

    @Override
    public void updateOtherResponseTimeMetric(String metric, Long value){
        LOG.debug("updateOtherResponseTimeMetric: metric:{}, value:{}", metric, value);
        timesLastOtherResponseByType.put(metric, value);
    }

    @Override
    public void updateOtherRequestTimeMetric(String metric, Long value){
        LOG.debug("updateOtherRequestTimeMetric: metric:{}, value:{}", metric, value);
        timesLastOtherRequestByType.put(metric, value);
    }

    @Override
    public EnemySize getEnemySize(long typeId) {
        EnemySize enemySize = ENEMY_SIZES_MAP.get(typeId);
        if(enemySize == null) {
            return new EnemySize(typeId, "NotFound", 100, 100, 1, 100, 100, 100, 100);
        }
        return enemySize;
    }

    @Override
    public void setLobbyBot(ILobbyBot lobbyBot) {
        this.lobbyBot = lobbyBot;
    }

    @Override
    public ILobbyBot getLobbyBot() {
        return lobbyBot;
    }

    @Override
    public int getShots() {
        return shots;
    }

    @Override
    public void resetShots() {
        shots = 0;
    }

    @Override
    public void activateWeapon(int weaponId) {
        activeWeaponId = weaponId;
    }

    @Override
    public void resetWeapons() {
        activeWeaponId = DEFAULT_WEAPON_ID;
        for (Map.Entry<Integer, Integer> entry : weapons.entrySet()) {
            entry.setValue(0);
        }
    }

    @Override
    public boolean shouldSendBullet(String botId) {
        String key = activeWeaponId == DEFAULT_WEAPON_ID ? "PISTOL" : SpecialWeaponType.values()[activeWeaponId].name();
        long bulletWaitTime = getWaitTime(botId, key, timesLastBulletRequestByType, timesLastBulletResponseByType,
                timesMinResponseByType, debug, LOG);

        try {

            LOG.debug("shouldSendBullet: botId: {}, Thread.sleep(Math.abs(waitForSleep)), waitForSleep={}", botId, bulletWaitTime);
            Thread.sleep(Math.abs(bulletWaitTime));

        } catch (InterruptedException e) {
            LOG.error(e);
            return false;
        }

        return true;
    }

    @Override
    public boolean shouldShoot(String botId) {
        String key = activeWeaponId == DEFAULT_WEAPON_ID ? "PISTOL" : SpecialWeaponType.values()[activeWeaponId].name();
        long shootWaitTime = getWaitTime(botId, key, timesLastShootRequestByType, timesLastShootResponseByType,
                timesMinResponseByType, debug, LOG);

        try {

            LOG.debug("shouldShoot: botId: {}, Thread.sleep(Math.abs(waitForSleep)), waitForSleep={}", botId, shootWaitTime);
            Thread.sleep(Math.abs(shootWaitTime));

        } catch (InterruptedException e) {
            LOG.error(e);
            return false;
        }

        return true;
    }

    @Override
    public boolean isBulletTime(String botId) {
        String key = activeWeaponId == DEFAULT_WEAPON_ID ? "PISTOL" : SpecialWeaponType.values()[activeWeaponId].name();
        long bulletWaitTime = getWaitTime(botId, key, timesLastBulletRequestByType, timesLastBulletResponseByType,
                timesMinResponseByType, debug, LOG);
        long shootWaitTime = getWaitTime(botId, key, timesLastShootRequestByType, timesLastShootResponseByType,
                timesMinResponseByType, debug, LOG);

        return bulletWaitTime <= shootWaitTime;
    }

    @Override
    public boolean isRicochetWeapon() {
        return activeWeaponId == DEFAULT_WEAPON_ID || activeWeaponId == SpecialWeaponType.LevelUp.getId();
    }

    @Override
    public long getWaitTimeForSwitchWeapon(String botId) {
        return getWaitTime(botId, SwitchWeaponRequest.METRIC, timesLastOtherRequestByType, timesLastOtherResponseByType,
                timesMinResponseByType, debug, LOG);
    }


    @Override
    public IRoomEnemy getEnemyToShoot(List<RoomEnemy> enemies) {
        String botId = "unknown";
        ILobbyBot iLobbyBot = getLobbyBot();
        if(iLobbyBot != null) {
            botId = iLobbyBot.getId();
        }

        if (enemies != null) {
            LOG.warn("getEnemyToShoot:  botId={}, enemies enemies.size()={}", botId, enemies.size());
        } else {
            LOG.warn("getEnemyToShoot:  botId={}, enemies list in null", botId);
        }
        return null;
    }

    @Override
    public long getWaitTime() {
        return 10;
    }

    @Override
    public int getBuyInAmmoAmount(long balance, float stake, int minAmmo) {
        int maxAmmo = (int) Math.floor(balance / stake);
        if (debug) {
            LOG.debug(" getBuyInAmmoAmount: balance={}, stake={}, minAmmo={}, maxAmmo={}", balance, stake, minAmmo, maxAmmo);
        }
        if (maxAmmo < minAmmo) {
            return 0;
        } else {
            return Math.min(maxAmmo, 1000);
        }
    }

    @Override
    public boolean shouldPurchaseWeaponLootBox() {
        return false;
    }

    @Override
    public boolean botHasSpecialWeapons() {
        boolean res = false;
        for (Map.Entry<Integer, Integer> sw : weapons.entrySet()) {
            Integer id = sw.getKey();
            Integer shotsFromSW = sw.getValue();
            if (id >= 0 && shotsFromSW > 0) {
                res = true;
                break;
            }
        }
        return res;
    }

    @Override
    public int getShotsForWeapon(int weaponId) {
        return weapons.get(weaponId);
    }

    @Override
    public void addWeapon(int id, int shots) {
        weapons.put(id, weapons.get(id) + shots);
    }

    @Override
    public int getWeaponId() {
        return activeWeaponId;
    }

    @Override
    public void consumeAmmo(int weaponId) {
        if(weaponId != DEFAULT_WEAPON_ID && weapons.get(weaponId) > 0) {
            weapons.put(weaponId, weapons.get(weaponId) - 1);
        }
    }

    @Override
    public void updateWeapon(int weaponId, int shots) {
        weapons.put(weaponId, shots);
    }

    @Override
    public boolean shouldSwitchWeapon() {
        boolean hasSpecialWeapons = botHasSpecialWeapons();
        boolean needUpdateFreeWeapon = hasSpecialWeapons && (activeWeaponId == DEFAULT_WEAPON_ID || weapons.get(activeWeaponId) == 0);
        if (needUpdateFreeWeapon) {
            LOG.debug("needUpdateFreeWeapon shouldSwitchWeapon before activeWeaponId {}", activeWeaponId);
            Optional<Map.Entry<Integer, Integer>> firstNotEmptyWeapon = weapons.entrySet().stream()
                    .filter(weapon -> (weapon.getValue() > 0 && weapon.getKey() != DEFAULT_WEAPON_ID))
                    .findFirst();
            firstNotEmptyWeapon.ifPresent(
                    integerIntegerEntry -> activeWeaponId = integerIntegerEntry.getKey());
            LOG.debug("shouldSwitchWeapon found free weapons need switch, activeWeaponId: {}, firstNotEmptyWeapon: {}",
                    activeWeaponId, firstNotEmptyWeapon);
            return true;
        } else if (activeWeaponId != DEFAULT_WEAPON_ID && weapons.get(activeWeaponId) == 0) {
            activeWeaponId = DEFAULT_WEAPON_ID;
            LOG.debug("shouldSwitchWeapon needUpdate weapon to pistol activeWeaponId {}, hasSpecialWeapons: {}",
                    activeWeaponId, hasSpecialWeapons);
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return false;
    }

    @Override
    public int requestedBetLevel() {
        return requestedBetLevel;
    }

    @Override
    public void addLastShootResponseTime(String key, Long time) {
        timesLastShootResponseByType.put(key, time);
        if (debug) {
            LOG.debug("addLastShootResponseTime: key: {}, time: {}", key, time);
        }
    }

    @Override
    public Long getTimesShootLastResponseByType(String key) {
        return timesLastShootResponseByType.getOrDefault(key, 0L);
    }

    @Override
    public Long getTimesBulletLastResponseByType(String key) {
        return timesLastBulletResponseByType.getOrDefault(key, 0L);
    }

    @Override
    public Long getTimesMinResponseByType(String key) {
        return timesMinResponseByType.getOrDefault(key, 0L);
    }

    @Override
    public Point getLocationOnScreen(RoomEnemy roomEnemy, long serverTime) {

        //LOG.debug("getLocationOnScreen: serverTime={}, roomEnemy:{}", toHumanReadableFormat(serverTime), roomEnemy);

        Trajectory trajectory = roomEnemy.getTrajectory();
        List<Point> points = trajectory.getPoints();

        if (points.get(0).getTime() > serverTime) {
            return new Point(points.get(0).getX(), points.get(0).getY(), serverTime);
        }

        Point lastPoint = points.get(points.size() - 1);
        if (serverTime >= lastPoint.getTime()) {
            return new Point(lastPoint.getX(), lastPoint.getY(), serverTime);
        }

        int i = 1;
        while (i < points.size() && serverTime > points.get(i).getTime()) {
            i++;
        }

        Point a = points.get(i - 1);
        Point b = points.get(i);
        double percent = ((double) (serverTime - a.getTime())) / (b.getTime() - a.getTime());

        return new Point(a.getX() + (b.getX() - a.getX()) * percent, a.getY() + (b.getY() - a.getY()) * percent, serverTime);
    }

    @Override
    public boolean isLocationOnMapAllowedForShot(List<PointExt> points, long serverTime, int currentMapId, int enemyType,
                                                 Point serverLocationByEnemy, GameMapShape map) {
        if (points.size() >= 2) {
            if(map != null && serverLocationByEnemy != null) {
                boolean availableAndPassable = map.isAvailableAndPassable(serverLocationByEnemy);
                if (!availableAndPassable) {
                    LOG.debug("isLocationOnMapAllowedForShot: availableAndPassable false for serverLocationByEnemy: {}", serverLocationByEnemy);
                    return false;
                }
                LOG.debug("isLocationOnMapAllowedForShot: availableAndPassable true for serverLocationByEnemy: {}", serverLocationByEnemy);
            }

            PointExt startPoint = points.get(0);
            PointExt lastPoint = points.get(points.size() - 1);
            PointExt currentPrev;
            if (startPoint.getTime() > serverTime) {
                currentPrev = startPoint;
            } else if (serverTime >= lastPoint.getTime()) {
                currentPrev = lastPoint;
            } else {
                int i = 1;
                while (i < points.size() && serverTime > points.get(i).getTime()) {
                    i++;
                }
                currentPrev = points.get(i - 1);
            }

            boolean invulnerable = currentPrev.isInvulnerable();
            boolean teleport = currentPrev.isTeleport();
            boolean result = !(invulnerable || teleport);
            if (result) {
                LOG.debug("isLocationOnMapAllowedForShot: currentPrev: {}, serverTime: {}, lastPoint.getTime(): {} ",
                        currentPrev, serverTime, lastPoint.getTime());
            }
            return result;

        } else {
            return true;
        }
    }

    @Override
    public boolean allowShotAfterRoundFinishSoon() {
        return false;
    }

    @Override
    public String toString() {
        return "NaturalBattleGroundMissionAmazonStrategy{" + "LOG=" + LOG +
                ", shots=" + shots +
                ", activeWeaponId=" + activeWeaponId +
                ", stakesLimit=" + stakesLimit +
                ", weapons=" + weapons +
                ", requestedBetLevel=" + requestedBetLevel +
                ", timesMinResponseByType=" + timesMinResponseByType +
                ", timesLastShootResponseByType=" + timesLastShootResponseByType +
                ", timesLastShootRequestByType=" + timesLastShootRequestByType +
                ", timesLastBulletResponseByType=" + timesLastBulletResponseByType +
                ", timesLastBulletRequestByType=" + timesLastBulletRequestByType +
                ", timesLastOtherResponseByType=" + timesLastOtherResponseByType +
                ", timesLastOtherRequestByType=" + timesLastOtherRequestByType +
                '}';
    }
}