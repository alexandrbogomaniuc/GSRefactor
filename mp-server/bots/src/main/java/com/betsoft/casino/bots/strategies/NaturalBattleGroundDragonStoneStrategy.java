package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.bots.model.EnemySize;
import com.betsoft.casino.bots.requests.SwitchWeaponRequest;
import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;
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

public class NaturalBattleGroundDragonStoneStrategy extends BattleGroundDragonStoneStrategy implements IRoomNaturalBotStrategy {

    private static final Logger LOG = LogManager.getLogger(NaturalBattleGroundDragonStoneStrategy.class);
    public static final int ENEMY_DRAGON_TYPE_ID = EnemyType.DRAGON.getId();
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

    public NaturalBattleGroundDragonStoneStrategy(int stakesLimit, int requestedBetLevel, long requestedByInAmount) {
        super(stakesLimit, requestedBetLevel, true, requestedByInAmount);
        this.stakesLimit = stakesLimit;
        this.requestedBetLevel = requestedBetLevel;
        this.debug = true;

        weapons.put(DEFAULT_WEAPON_ID, 0);
        for (SpecialWeaponType type : SpecialWeaponType.values()) {
            if (type.getAvailableGameIds().contains((int) GameType.BG_DRAGONSTONE.getGameId())
                    && !type.isInternalServerShot())
                weapons.put(type.getId(), 0);
        }

        timesMinResponseByType = new HashMap<>();
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
            if (value.getAvailableGameIds().contains((int) GameType.BG_DRAGONSTONE.getGameId())) {
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

        ENEMY_SIZES_MAP.put(0L, new EnemySize(0, EnemyType.BROWN_SPIDER.getName(), 55, 30, 1, 33, 33, 33 , 33));//"Brown Spider"
        ENEMY_SIZES_MAP.put(1L, new EnemySize(1, EnemyType.BLACK_SPIDER.getName(), 50, 33, 1, 33, 33, 33 , 33));//"Black Spider"
        ENEMY_SIZES_MAP.put(2L, new EnemySize(2, EnemyType.BROWN_RAT.getName(), 30, 20, 1, 33, 33, 33 , 33));//"Brown Rat"
        ENEMY_SIZES_MAP.put(3L, new EnemySize(3, EnemyType.BLACK_RAT.getName(), 30, 20, 1, 33, 33, 33 , 33));//"Black Rat"
        ENEMY_SIZES_MAP.put(4L, new EnemySize(4, EnemyType.BAT.getName(), 45, 30, 1, 33, 33, 33 , 33));//"Bat"
        ENEMY_SIZES_MAP.put(5L, new EnemySize(5, EnemyType.RAVEN.getName(), 60, 35, 1, 33, 33, 33 , 33));//"Raven"
        ENEMY_SIZES_MAP.put(6L, new EnemySize(6, EnemyType.SKELETON_1.getName(), 35, 95, 1, 33, 33, 33 , 33));//"Skeleton"
        ENEMY_SIZES_MAP.put(7L, new EnemySize(7, EnemyType.IMP_1.getName(), 60, 90, 1, 33, 33, 33 , 33));//"Gluttonous Imp"
        ENEMY_SIZES_MAP.put(8L, new EnemySize(8, EnemyType.IMP_2.getName(), 60, 90, 1, 33, 33, 33 , 33));//"Plaqued Imp"
        ENEMY_SIZES_MAP.put(9L, new EnemySize(9, EnemyType.SKELETON_SHIELD.getName(),  35, 95, 1, 33, 33, 33 , 33));//"Skeletal Commander"
        ENEMY_SIZES_MAP.put(10L, new EnemySize(10, EnemyType.GOBLIN.getName(), 30, 70,  1, 33, 33, 33 , 33));//"Goblin"
        ENEMY_SIZES_MAP.put(11L, new EnemySize(11, EnemyType.HOBGOBLIN.getName(),  30, 70, 1, 33, 33, 33 , 33));//"Hobgoblin"
        ENEMY_SIZES_MAP.put(12L, new EnemySize(12, EnemyType.DUP_GOBLIN.getName(),  30, 70, 1, 33, 33, 33 , 33));//"Spectral Goblin"
        ENEMY_SIZES_MAP.put(13L, new EnemySize(13, EnemyType.GARGOYLE.getName(),  180, 130, 1, 33, 33, 33 , 33));//"Gargoyle"
        ENEMY_SIZES_MAP.put(14L, new EnemySize(14, EnemyType.ORC.getName(), 60, 130, 1, 33, 33, 33 , 33));//"Orc"
        ENEMY_SIZES_MAP.put(15L, new EnemySize(15, EnemyType.EMPTY_ARMOR_1.getName(), 35, 100, 1, 33, 33, 33 , 33));//"Knight's Armor"
        ENEMY_SIZES_MAP.put(16L, new EnemySize(16, EnemyType.EMPTY_ARMOR_2.getName(), 35, 100, 1, 33, 33, 33 , 33));//"Tarnished Armor"
        ENEMY_SIZES_MAP.put(17L, new EnemySize(17, EnemyType.EMPTY_ARMOR_3.getName(), 35, 100, 1, 33, 33, 33 , 33));//"Champion's Armor"
        ENEMY_SIZES_MAP.put(18L, new EnemySize(18, EnemyType.RED_WIZARD.getName(),  30, 100, 1, 33, 33, 33 , 33));//"Red Wizard"
        ENEMY_SIZES_MAP.put(19L, new EnemySize(19, EnemyType.BLUE_WIZARD.getName(),  30, 100, 1, 33, 33, 33 , 33));//"Blue Wizard"
        ENEMY_SIZES_MAP.put(20L, new EnemySize(20, EnemyType.PURPLE_WIZARD.getName(),  30, 100, 1, 33, 33, 33 , 33));//"Purple Wizard"
        ENEMY_SIZES_MAP.put(21L, new EnemySize(21, EnemyType.OGRE.getName(), 110, 200, 1, 33, 33, 33 , 33));//"Ogre"
        ENEMY_SIZES_MAP.put(22L, new EnemySize(22, EnemyType.DARK_KNIGHT.getName(), 65, 160, 1, 33, 33, 33 , 33));//"Dark Knight"
        ENEMY_SIZES_MAP.put(23L, new EnemySize(23, EnemyType.CERBERUS.getName(),  160, 130, 1, 33, 33, 33 , 33));//"Cerberus"

        ENEMY_SIZES_MAP.put(24L, new EnemySize(24, EnemyType.SPIRIT_SPECTER.getName(), 90, 150, 1, 33, 33, 33 , 33));//"Spirit Specter"
        ENEMY_SIZES_MAP.put(25L, new EnemySize(25, EnemyType.FIRE_SPECTER.getName(), 90, 150, 1, 33, 33, 33 , 33));//"Fire Specter"
        ENEMY_SIZES_MAP.put(26L, new EnemySize(26, EnemyType.LIGHTNING_SPECTER.getName(), 90, 150, 1, 33, 33, 33 , 33));//"Lightning Specter"

        ENEMY_SIZES_MAP.put(27L, new EnemySize(27, EnemyType.DRAGON.getName(),  500, 500, 1, 33, 33, 33 , 33));//"Dragon"

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
    public boolean isSpecialCaseForEnemy(IRoomEnemy randomFirstEnemy) {
        return randomFirstEnemy != null && randomFirstEnemy.getTypeId() == ENEMY_DRAGON_TYPE_ID;
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
    public String toString() {
        return "NaturalBattleGroundDragonStoneStrategy{" + "LOG=" + LOG +
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