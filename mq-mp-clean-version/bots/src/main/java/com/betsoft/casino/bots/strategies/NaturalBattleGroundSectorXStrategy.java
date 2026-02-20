package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.LobbyBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.bots.model.EnemySize;
import com.betsoft.casino.bots.requests.SwitchWeaponRequest;
import com.betsoft.casino.mp.bgsectorx.model.math.EnemyType;
import com.betsoft.casino.mp.bgsectorx.model.EnemyCircularRadius;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.transport.PointExt;
import com.betsoft.casino.mp.transport.RoomEnemy;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.betsoft.casino.bots.model.Turret.DEFAULT_WEAPON_ID;
import static com.betsoft.casino.bots.utils.BezierCurve.approximateTrajectory;
import static com.betsoft.casino.bots.utils.BezierCurve.getCurve;

public class NaturalBattleGroundSectorXStrategy extends BattleGroundSectorXStrategy implements IRoomNaturalBotStrategy {

    private static final Logger LOG = LogManager.getLogger(NaturalBattleGroundSectorXStrategy.class);
    public static final int ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS = EnemyType.B1.getId();
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
    private final Long[] requestedEnemyTypeIds;
    private ILobbyBot lobbyBot;

    private static final Map<Long, EnemySize> ENEMY_SIZES_MAP = new HashMap();
    private final HashMap<Long, Integer> enemyCurrentApproximatePositionIndexes = new HashMap<>();//enemyId,CurrentApproximatePositionIndexes

    public NaturalBattleGroundSectorXStrategy(int stakesLimit, int requestedBetLevel, long requestedByInAmount, String requestedEnemyTypeIds) {
        super(stakesLimit, requestedBetLevel, true, requestedByInAmount);
        this.stakesLimit = stakesLimit;
        this.requestedBetLevel = requestedBetLevel;
        this.debug = true;

        weapons.put(DEFAULT_WEAPON_ID, 0);
        for (SpecialWeaponType type : SpecialWeaponType.values()) {
            if (type.getAvailableGameIds().contains((int) GameType.BG_SECTOR_X.getGameId())
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

        addMetric(SwitchWeaponRequest.METRIC, (long)TObject.FREQUENCY_LIMIT);

        addMetric("PISTOL", 150L);
        addMetric(SpecialWeaponType.LevelUp.name(), 150L);
        this.requestedEnemyTypeIds = IRoomBotStrategy.convertIds(requestedEnemyTypeIds);

        ENEMY_SIZES_MAP.put(0L, new EnemySize(0, EnemyType.S1.getName(), 44.1207211489762, 42.5308189748817, 1, 30.037353515625, 30.037353515625, 72.1676330566406, 72.1339111328125));// "Malachite Eye Flyer"
        ENEMY_SIZES_MAP.put(1L, new EnemySize(1, EnemyType.S2.getName(), 41.7588068138057, 44.7902088777383, 1, 30.1268005371094, 30.1293334960938, 72.183349609375, 72.1039276123047));//"Amber Eye Flyer"
        ENEMY_SIZES_MAP.put(2L, new EnemySize(2, EnemyType.S3.getName(), 47.2125046421105, 39.4728910715927, 1, 37.7485961914063, 30.1959228515625, 71.9585571289063, 55.2077178955078));//"Amethyst Eye Flyer"
        ENEMY_SIZES_MAP.put(3L, new EnemySize(3, EnemyType.S4.getName(), 43.6907371984486, 42.8442143051583, 1, 30.0397644042969, 30.0678100585938, 72.2004547119141, 72.8781814575195));//"Citrine Eye Flyer"
        ENEMY_SIZES_MAP.put(4L, new EnemySize(4, EnemyType.S5.getName(), 60.1907278708518, 61.4955226492751, 1, 31.1547622680664, 33.178218126297, 92.7041091918945, 92.6667175292969));//"Jellied Skyswimmer"
        ENEMY_SIZES_MAP.put(5L, new EnemySize(5, EnemyType.S6.getName(), 68.6910352105418, 72.5362737625174, 1, 48.7977905273438, 48.688232421875, 99.4560241699219, 96.6043853759766));//"Malignant Ray"
        ENEMY_SIZES_MAP.put(6L, new EnemySize(6, EnemyType.S7.getName(), 76.2511324941299, 75.3298790554398, 1, 39.8853149414063, 39.7435665130615, 114.925342559814, 113.924850463867));//"Emerald Jumper"
        ENEMY_SIZES_MAP.put(7L, new EnemySize(7, EnemyType.S8.getName(), 75.5590887236444, 74.4779123927819, 1, 49.1829833984375, 39.9505767822266, 112.430877685547, 114.16911315918));//"Sapphire Jumper"
        ENEMY_SIZES_MAP.put(8L, new EnemySize(8, EnemyType.S9.getName(), 77.4226967150741, 75.1610785272557, 1, 39.7191772460938, 39.7791748046875, 113.810256958008, 112.703338623047));//"Albino Jumper"
        ENEMY_SIZES_MAP.put(9L, new EnemySize(9, EnemyType.S10.getName(), 72.4346016267306, 65.6323306107185, 1, 45.3765029907227, 44.7177734375, 100.8212890625, 95.5546875));//"Grotesque Slug"
        ENEMY_SIZES_MAP.put(10L, new EnemySize(10, EnemyType.S11.getName(), 60.2554759642868, 57.5884847287617, 1, 46.6015625, 46.5023736953735, 72.6401062011719, 73.4033527374268));//"Invader Trooper"
        ENEMY_SIZES_MAP.put(11L, new EnemySize(11, EnemyType.S12.getName(), 86.6780779325045, 87.7898760575515, 1, 79.81298828125, 82.1744995117188, 91.386474609375, 93.3350830078125));//"Spiked Triclops"
        ENEMY_SIZES_MAP.put(12L, new EnemySize(12, EnemyType.S13.getName(), 84.5507697459682, 90.8037469986439, 1, 46.5032348632813, 46.54044008255, 122.371032714844, 124.455581665039));//"Buzzing Watcher"
        ENEMY_SIZES_MAP.put(13L, new EnemySize(13, EnemyType.S14.getName(), 95.0783313302619, 101.824497418147, 1, 81.1466674804688, 81.0156478881836, 123.443237304688, 123.443222045898));//"Ocular Terror"
        ENEMY_SIZES_MAP.put(14L, new EnemySize(14, EnemyType.S15.getName(), 102.228651384718, 101.890186255685, 1, 53.8482666015625, 53.9337158203125, 142.335571289063, 143.463397860527));//"Nimble Jumper"
        ENEMY_SIZES_MAP.put(15L, new EnemySize(15, EnemyType.S16.getName(), 87.4986938476563, 76.4202837262835, 1, 71.5294799804688, 65.3599243164063, 95.342529296875, 88.9912414550781));//"Scarlet Glider"
        ENEMY_SIZES_MAP.put(16L, new EnemySize(16, EnemyType.S17.getName(), 100.989729376017, 93.5546441217202, 1, 72.2905883789063, 74.3096923828125, 128.319946289063, 127.733093261719));//"Azure Devourer"
        ENEMY_SIZES_MAP.put(17L, new EnemySize(17, EnemyType.S18.getName(), 94.2623019488887, 98.924099756516, 1, 71.4153747558594, 74.2551507949829, 125.324157714844, 129.02725982666));//"Crimson Devourer"
        ENEMY_SIZES_MAP.put(18L, new EnemySize(18, EnemyType.S19.getName(), 95.5171006723063, 96.6642069436333, 1, 75.2518310546875, 71.8775634765625, 130.018672943115, 124.672622680664));//"Albino Devourer"
        ENEMY_SIZES_MAP.put(19L, new EnemySize(19, EnemyType.S20.getName(), 90.438285546875, 104.140624651337, 1, 75.6993408203125, 72.4794921875, 120.478759765625, 125.140838623047));//"Yellow Devourer"
        ENEMY_SIZES_MAP.put(20L, new EnemySize(20, EnemyType.S21.getName(), 85.7640392130072, 82.5816817933863, 1, 66.2042846679688, 61.0073165893555, 118.205017089844, 122.378677368164));//"Fluttering Screecher"
        ENEMY_SIZES_MAP.put(21L, new EnemySize(21, EnemyType.S22.getName(), 96.1451460213765, 91.4679898047165, 1, 60.7704238891602, 72.9188842773438, 107.912506103516, 102.339141845703));//"Invader Commander"
        ENEMY_SIZES_MAP.put(22L, new EnemySize(22, EnemyType.S23.getName(), 137.69930665508, 138.409700891455, 1, 93.4121398925781, 90.9630584716797, 176.072082519531, 176.063461303711));//"Cyborg Raider"
        ENEMY_SIZES_MAP.put(23L, new EnemySize(23, EnemyType.S24.getName(), 108.51377819178, 108.169698422301, 1, 64.7565307617188, 64.6396713256836, 149.046081542969, 148.640441894531));//"Trinocular Leaper"
        ENEMY_SIZES_MAP.put(24L, new EnemySize(24, EnemyType.S25.getName(), 108.982205694434, 107.671878901915, 1, 81.1347198486328, 80.7526550292969, 131.100891113281, 130.098651409149));//"Crawling Hellmouth"
        ENEMY_SIZES_MAP.put(25L, new EnemySize(25, EnemyType.S26.getName(), 133.671129513658, 129.787732301769, 1, 82.3995361328125, 80.967041015625, 196.630065917969, 192.366607666016));//"Razortooth"
        ENEMY_SIZES_MAP.put(26L, new EnemySize(26, EnemyType.S27.getName(), 134.29219549688, 145.636962436048, 1, 111.127593994141, 111.119903564453, 164.958908081055, 161.641362667084));//"Spiked Beholder"
        ENEMY_SIZES_MAP.put(27L, new EnemySize(27, EnemyType.S28.getName(), 107.154946579678, 104.826349853618, 1, 77.9829711914063, 76.8072814941406, 144.087036132813, 142.308959960938));//"Darkwing Mutant"
        ENEMY_SIZES_MAP.put(28L, new EnemySize(28, EnemyType.S29.getName(), 121.417352040609, 124.873844288014, 1, 100.633422851563, 103.786773681641, 141.318664550781, 140.192504882813));//"Hivemind Overlord"
        ENEMY_SIZES_MAP.put(29L, new EnemySize(29, EnemyType.S30.getName(), 141.659875737404, 143.617592760981, 1, 134.545776367188, 133.889038085938, 162.472290039063, 151.963226318359));//"Living Magma"
        ENEMY_SIZES_MAP.put(30L, new EnemySize(30, EnemyType.S31.getName(), 183.424202997067, 160.717584264747, 1, 111.5244140625, 111.288146972656, 205.073669433594, 199.315093994141));//"Rampaging Behemoth"

        ENEMY_SIZES_MAP.put(71L, new EnemySize(71, EnemyType.B1.getName(), 200, 170, 1, 200, 170, 200, 170));//"Concealed Coins"
        ENEMY_SIZES_MAP.put(72L, new EnemySize(72, EnemyType.B2.getName(), 230.859200919701, 168.193522674358, 1, 199.884202003479, 140.998291015625, 264.076470375061, 195.078796386719));//"Mega Cyborg Raider"
        ENEMY_SIZES_MAP.put(73L, new EnemySize(73, EnemyType.B3.getName(), 200, 170, 1, 200, 170, 200, 17));//"Mega Rampaging Behemoth"

        ENEMY_SIZES_MAP.put(51L, new EnemySize(51, EnemyType.F1.getName(), 159.983926588489, 192.293549445368, 1, 150.707336425781, 183.452392578125, 172.14208984375, 207.344497680664));//"Money Wheel"
        ENEMY_SIZES_MAP.put(52L, new EnemySize(52, EnemyType.F2.getName(), 158.404583642259, 202.690269052982, 1, 153.141052246094, 183.467712402344, 171.733066558838, 218.70662689209));//"Flash Blizzard"
        ENEMY_SIZES_MAP.put(53L, new EnemySize(53, EnemyType.F3.getName(), 157.823357900369, 196.577057730842, 1, 150.696228027344, 183.428451538086, 175.954162597656, 211.137102127075));//"Enemy Seeker"
        ENEMY_SIZES_MAP.put(54L, new EnemySize(54, EnemyType.F4.getName(), 157.931654640774, 195.662545512175, 1, 150.722015380859, 183.415802001953, 172.08837890625, 207.35432434082));//""Multiplier Bomb"
        ENEMY_SIZES_MAP.put(55L, new EnemySize(55, EnemyType.F5.getName(), 157.409108316622, 197.034341719407, 1, 150.702453613281, 183.4250831604, 172.097351074219, 207.330589294434));//"Chain Reaction Shot"
        ENEMY_SIZES_MAP.put(56L, new EnemySize(56, EnemyType.F6.getName(), 160, 195, 1, 160, 195, 160, 195));//"Arc Lighthing"
        ENEMY_SIZES_MAP.put(57L, new EnemySize(57, EnemyType.F7.getName(), 156.802682155104, 197.762004425277, 1, 150.712245464325, 183.449981689453, 172.158538818359, 207.356979370117));//"Laser Net"

        ENEMY_SIZES_MAP.put(100L, new EnemySize(100, EnemyType.BOSS.getName(), 302.757677350725, 319.278552100772, 1, 297.905776977539, 311.39387512207, 305.600494384766, 332.836868286133));//"Boss"
    }

    private void addMetric(String metric, Long minValue){
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
            List<RoomEnemy> requestedEnemies = enemies.stream()
                    .filter(enemy -> IRoomBotStrategy.contains(requestedEnemyTypeIds, enemy.getTypeId()))
                    .collect(Collectors.toList());
            LOG.debug("getEnemyToShoot: botId={} there are {} enemies and {} requestedEnemies",
                    botId, enemies.size(), requestedEnemies.size());

            if (requestedEnemies.size() > 0) {
                LOG.debug("getEnemyToShoot: botId={}, existing requestedEnemies are:[{}]", botId,
                        requestedEnemies.stream()
                                .map(enemy -> "{\"id\":" + enemy.getId() + ", \"typeId\": " + enemy.getTypeId() + "}")
                                .collect(Collectors.joining(", ")));

                RoomEnemy requestedEnemy = requestedEnemies.get(RNG.nextInt(requestedEnemies.size()));
                if (requestedEnemy != null) {
                    LOG.debug("getEnemyToShoot: botId={}, random selected requestedEnemy is: {\"id\":{}, \"typeId\":{}}",
                            botId, requestedEnemy.getId(), requestedEnemy.getTypeId());

                    if (requestedEnemy.getTypeId() == ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS) {
                        IRoomEnemy concealedEnemy = tryFindConcealedEnemy(enemies, requestedEnemy.getId());
                        IRoomEnemy enemyToReturn = concealedEnemy == null ? requestedEnemy : concealedEnemy;
                        if (enemyToReturn != null) {
                            LOG.debug("getEnemyToShoot: botId={}, enemyToReturn is: {\"id\":{}, \"typeId\":{}}",
                                    botId, enemyToReturn.getId(), enemyToReturn.getTypeId());
                        } else {
                            LOG.warn("getEnemyToShoot: botId={}, enemyToReturn is null", botId);
                        }
                        return enemyToReturn;
                    }
                } else {
                    LOG.warn("getEnemyToShoot: botId={}, requestedEnemy is null", botId);
                }
            } else {
                LOG.debug("getEnemyToShoot: botId={}, existing requestedEnemies list is empty, return null", botId);
                /*LOG.debug("getEnemyToShoot: botId={}, existing requestedEnemies list is empty, try to get Random Enemy from roomBot", botId);
                LobbyBot lobbyBot = iLobbyBot instanceof LobbyBot ? (LobbyBot) iLobbyBot : null;
                if(lobbyBot != null ) {
                    IRoomBot iRoomBot = lobbyBot.getRoomBot();
                    RoomBot roomBot = iRoomBot instanceof RoomBot ? (RoomBot) iRoomBot : null;
                    if (roomBot != null) {
                        IRoomEnemy randomEnemy = roomBot.getRandomEnemy();
                        LOG.debug("getEnemyToShoot: botId={}, random selected by roomBot enemy is: {\"id\":{}, \"typeId\":{}}",
                                botId, randomEnemy.getId(), randomEnemy.getTypeId());
                        return randomEnemy;
                    } else {
                        LOG.warn("getEnemyToShoot: botId={}, roomBot is null", botId);
                    }
                } else {
                    LOG.warn("getEnemyToShoot: botId={}, lobbyBot is null", botId);
                }
                */
            }
        } else {
            LOG.warn("getEnemyToShoot:  botId={}, enemies list in null", botId);
        }
        return null;
    }

    private IRoomEnemy tryFindConcealedEnemy(List<RoomEnemy> enemies, Long parentEnemyId) {
        String botId = "unknown";
        ILobbyBot iLobbyBot = getLobbyBot();
        if(iLobbyBot != null) {
            botId = iLobbyBot.getId();
        }

        if(enemies != null) {
            List<RoomEnemy> concealedEnemies = enemies.stream()
                    .filter(cEnemy -> cEnemy.getParentEnemyId() == parentEnemyId
                            && cEnemy.getParentEnemyTypeId() == ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS)
                    .collect(Collectors.toList());
            LOG.debug("tryFindConcealedEnemy: botId={}, there are {} concealedEnemies for parentId {}",
                    botId, concealedEnemies.size(), parentEnemyId);

            if(concealedEnemies.size() > 0) {
                LOG.debug("tryFindConcealedEnemy: botId={}, concealedEnemies are:[{}]", botId,
                        concealedEnemies.stream()
                                .map(enemy -> "{\"id\":" + enemy.getId() + ", \"typeId\": " + enemy.getTypeId() +"}")
                                .collect(Collectors.joining(", ")));
                IRoomEnemy concealedEnemy = concealedEnemies.get(RNG.nextInt(concealedEnemies.size()));

                if(concealedEnemy != null) {
                    LOG.debug("getEnemyToShoot: botId={}, random selected concealedEnemy is: {\"id\":{}, \"typeId\":{}}",
                            botId, concealedEnemy.getId(), concealedEnemy.getTypeId());
                }

                return concealedEnemy;
            }
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
        return timesMinResponseByType.getOrDefault(key, 0L) ;
    }

    //get position of the enemy moving on orbit of the main enemy
    private Point getCircularPoint(int parentTypeId, Point location, long firstPointTime, double speed, int circularAngle, long serverTime) {

        long passTime = (long)((serverTime - firstPointTime) * speed / 10000);

        if (circularAngle != -1) {
            passTime += Math.toRadians(circularAngle);
        }

        int radius = 0;
        Optional<EnemyCircularRadius> circularRadius = EnemyCircularRadius.getRadiusByID(parentTypeId);
        if(circularRadius.isPresent()) {
            radius = circularRadius.get().getRadius();
        }

        double positionPtX = location.getX() + radius * Math.sin(passTime);
        double positionPtY = location.getY() + radius * Math.cos(passTime);
        return new Point(positionPtX, positionPtY, serverTime);
    }

    private Point determCircularPosition(RoomEnemy enemy, long aCurrentTime_num)
    {
        Point lPosition_pt = new Point(0,0,0);
        Trajectory trajectory = enemy == null ? null : enemy.getTrajectory();
        List<Point> points = trajectory == null ? null : trajectory.getPoints();
        Point[] lSeparetedPoints_arr = points.toArray(new Point[0]);

        if (trajectory == null || trajectory.isCircularTrajectory() == null || !trajectory.isCircularTrajectory()) {
            return lPosition_pt;
        }

        double lEnemySpeed_num = enemy.getSpeed();
        RoomEnemy lParentEnemy_obj = null;
        long parentEnemyTypeId = enemy.getTypeId();
        if(this.getLobbyBot() instanceof LobbyBot) {
            IRoomBot roomBot = ((LobbyBot)this.getLobbyBot()).getRoomBot();
            if(roomBot instanceof RoomBot) {
                lParentEnemy_obj = ((RoomBot)roomBot).getEnemy(parentEnemyTypeId);
            }
        }

        if(lEnemySpeed_num == 0 && lParentEnemy_obj != null)
        {
            lEnemySpeed_num = lParentEnemy_obj.getSpeed();
        }

        long lFirstTrajectoryPointTime_num = lSeparetedPoints_arr[0].getTime();
        double lPassTime = (aCurrentTime_num - lFirstTrajectoryPointTime_num) * lEnemySpeed_num / 10000; /*enemy.getCircularSpeedCoefficient()*/
        int circularAngle = trajectory.getCircularAngle();
        if (circularAngle != -1)
        {
            lPassTime += Math.toRadians(circularAngle);
        }

        int radius = 0;
        Optional<EnemyCircularRadius> circularRadius = EnemyCircularRadius.getRadiusByID((int)enemy.getId());
        if(circularRadius.isPresent()) {
            radius = circularRadius.get().getRadius();
        }

        double positionPtX = lPosition_pt.getX() + radius * Math.sin(lPassTime);
        double positionPtY = lPosition_pt.getY() + radius * Math.cos(lPassTime);
        lPosition_pt = new Point(positionPtX, positionPtY, aCurrentTime_num);

        return lPosition_pt;
    }

    private Point determCircularStaticPosition(RoomEnemy enemy)
    {
        Point lPosition_pt = new Point(0,0,0);
        Trajectory trajectory = enemy == null ? null : enemy.getTrajectory();
        List<Point> points = trajectory == null ? null : trajectory.getPoints();
        Point[] lSeparetedPoints_arr = points.toArray(new Point[0]);

        if (trajectory == null || trajectory.isCircularTrajectory() == null || !trajectory.isCircularTrajectory()) {
            return lPosition_pt;
        }

        int circularAngle = trajectory.getCircularAngle();
        if (circularAngle != -1)
        {
            double lFirstTrajectoryPointTime_num = Math.toRadians(circularAngle);

            int radius = 0;
            Optional<EnemyCircularRadius> circularRadius = EnemyCircularRadius.getRadiusByID((int)enemy.getId());
            if(circularRadius.isPresent()) {
                radius = circularRadius.get().getRadius();
            }

            double positionPtX = lPosition_pt.getX() + radius * Math.sin(lFirstTrajectoryPointTime_num);
            double positionPtY = lPosition_pt.getY() + radius * Math.cos(lFirstTrajectoryPointTime_num);
            lPosition_pt = new Point(positionPtX, positionPtY, lPosition_pt.getTime());
        }

        return lPosition_pt;
    }

    private Point getCircularPosition(RoomEnemy enemy, long lCurrentTime_num)
    {
        Point lCircularPosition_pt = new Point(0,0,0);
        Trajectory trajectory = enemy == null ? null : enemy.getTrajectory();
        if (trajectory != null && trajectory.isCircularTrajectory() != null && trajectory.isCircularTrajectory())
        {
            /*if (trajectory.isCircularStatic())
            {
                if (trajectory.getDetermCircularStaticPosition() != null)
                {
                    lCircularPosition_pt = trajectory.getDetermCircularStaticPosition();
                }
                else
                {
                    lCircularPosition_pt = this.determCircularStaticPosition(enemy);
                    trajectory.setDetermCircularStaticPosition(lCircularPosition_pt); //cache
                }
            }
            else
            {*/
                lCircularPosition_pt = this.determCircularPosition(enemy, lCurrentTime_num);
            //}
        }

        return lCircularPosition_pt;
    }

    private Point getApproximateBezierPosition(RoomEnemy enemy, double aCurrentPercentageOfTrajectoryMoved_num)
    {
        List<Point> points = enemy == null ?
                null : enemy.getTrajectory() == null ?
                null : enemy.getTrajectory().getPoints();

        Long enemyId = enemy == null ? null : enemy.getId();

        Point[] lSeparetedPoints_arr = points.toArray(new Point[0]);
        Triple<Point, Double, Double>[] lApproximateTrajectory_arr = approximateTrajectory(lSeparetedPoints_arr);

        double lCorrectPercent_num = aCurrentPercentageOfTrajectoryMoved_num;

        int lStartPosition_num = 1;
        Integer enemyCurrentApproximatePositionIndex = enemyCurrentApproximatePositionIndexes.get(enemyId);
        if (enemyCurrentApproximatePositionIndex != null
                && lApproximateTrajectory_arr[enemyCurrentApproximatePositionIndex] != null
                && lApproximateTrajectory_arr[enemyCurrentApproximatePositionIndex].third() <= aCurrentPercentageOfTrajectoryMoved_num) {
            //if the past coordinates and path segment have already been determined, i.e. not the first drawing,
            // then we start the search from this position
            lStartPosition_num = enemyCurrentApproximatePositionIndex;
        }

        for (int i = lStartPosition_num; i < lApproximateTrajectory_arr.length; i++) {

            double percent = lApproximateTrajectory_arr[i].second();
            double correctPercent = lApproximateTrajectory_arr[i].third();

            if (correctPercent == aCurrentPercentageOfTrajectoryMoved_num) {//

                lCorrectPercent_num = percent;
                enemyCurrentApproximatePositionIndexes.put(enemyId, i);
                break;

            } else if (correctPercent > aCurrentPercentageOfTrajectoryMoved_num) {

                double percentPrevElement = lApproximateTrajectory_arr[i-1].second();
                double correctPercentPrevElement = lApproximateTrajectory_arr[i-1].third();

                double lOffsetSegmentPercent_num = aCurrentPercentageOfTrajectoryMoved_num - correctPercentPrevElement;
                double lFullSegmentPercent_num = correctPercent - correctPercentPrevElement;
                double lRelativeOffset_num = lOffsetSegmentPercent_num / lFullSegmentPercent_num;

                double lFullSegmentCorrectPercent_num = percent - percentPrevElement;
                lCorrectPercent_num = lFullSegmentCorrectPercent_num * lRelativeOffset_num +  percentPrevElement ;
                enemyCurrentApproximatePositionIndexes.put(enemyId, i - 1);
                break;
            }
        }

        return getCurve(lSeparetedPoints_arr, lCorrectPercent_num);
    }

    public Point getGenericBezierEnemyPosition(RoomEnemy enemy, long serverTime) {

        long lCurrentTime_num = serverTime;

        List<Point> points = enemy == null ?
                null : enemy.getTrajectory() == null ?
                null : enemy.getTrajectory().getPoints();

        Point[] lTrajectoryPoints_arr = points.toArray(new Point[0]);

        long lFirstTrajectoryPointTime_num = lTrajectoryPoints_arr[0].getTime();
        long lLastTrajectoryPointTime_num = lTrajectoryPoints_arr[lTrajectoryPoints_arr.length - 1].getTime();

        long lTotalEnemyMovementTime_num = lLastTrajectoryPointTime_num - lFirstTrajectoryPointTime_num;
        double lCurrentPercentageOfTrajectoryMoved_num =
                Math.max((serverTime - lFirstTrajectoryPointTime_num) / (double)lTotalEnemyMovementTime_num, 0);

        Point lEnemyPosition = new Point(0,0,0);

        Point lEnemyPositionPoint_pnt = getApproximateBezierPosition(enemy, lCurrentPercentageOfTrajectoryMoved_num);
        lEnemyPosition.setX(lEnemyPositionPoint_pnt.getX());
        lEnemyPosition.setY(lEnemyPositionPoint_pnt.getY());
        lEnemyPosition.setTime(lEnemyPositionPoint_pnt.getTime());

        return lEnemyPosition;
    }

    public Point getQuadraticBezierEnemyPosition(RoomEnemy roomEnemy, long serverTime) {
        if(roomEnemy == null || roomEnemy.getTrajectory() == null || roomEnemy.getTrajectory().getPoints() == null) {
            return null;
        }

        Trajectory trajectory =  roomEnemy.getTrajectory();
        List<Point> trajectoryPoints = trajectory.getPoints();

        if(trajectoryPoints.size() == 0) {
            return null;
        }

        Point point = null;

        if (trajectoryPoints.size() == 3) {
            //quadraticBezier
            Point a = trajectoryPoints.get(0);
            Point b = trajectoryPoints.get(2);
            double t = ((double) (serverTime - a.getTime())) / (b.getTime() - a.getTime());
            double oneMinusT = (double) 1.0F - t;

            double x = oneMinusT * oneMinusT * ((Point) trajectoryPoints.get(0)).getX()
                    + (double) 2.0F * t * oneMinusT * ((Point) trajectoryPoints.get(1)).getX()
                    + t * t * ((Point) trajectoryPoints.get(2)).getX();

            double y = oneMinusT * oneMinusT * ((Point) trajectoryPoints.get(0)).getY()
                    + (double) 2.0F * t * oneMinusT * ((Point) trajectoryPoints.get(1)).getY()
                    + t * t * ((Point) trajectoryPoints.get(2)).getY();

            point = new Point(x, y, serverTime);

        }

        return point;
    }

    public Point getCubicBezierEnemyPosition(RoomEnemy roomEnemy, long serverTime) {
        if(roomEnemy == null || roomEnemy.getTrajectory() == null || roomEnemy.getTrajectory().getPoints() == null) {
            return null;
        }

        Trajectory trajectory =  roomEnemy.getTrajectory();
        List<Point> trajectoryPoints = trajectory.getPoints();

        if(trajectoryPoints.size() == 0) {
            return null;
        }

        Point point = null;

        if (trajectoryPoints.size() == 4) {
            //cubicBezier
            Point a = trajectoryPoints.get(0);
            Point b = trajectoryPoints.get(3);
            double t = ((double) (serverTime - a.getTime())) / (b.getTime() - a.getTime());

            double x = Math.pow((double) 1.0F - t, (double) 3.0F) * ((Point) trajectoryPoints.get(0)).getX()
                    + (double) 3.0F * t * Math.pow((double) 1.0F - t, (double) 2.0F) * ((Point) trajectoryPoints.get(1)).getX()
                    + (double) 3.0F * t * t * ((double) 1.0F - t) * ((Point) trajectoryPoints.get(2)).getX() + Math.pow(t, (double) 3.0F) * ((Point) trajectoryPoints.get(3)).getX();

            double y = Math.pow((double) 1.0F - t, (double) 3.0F) * ((Point) trajectoryPoints.get(0)).getY()
                    + (double) 3.0F * t * Math.pow((double) 1.0F - t, (double) 2.0F) * ((Point) trajectoryPoints.get(1)).getY()
                    + (double) 3.0F * t * t * ((double) 1.0F - t) * ((Point) trajectoryPoints.get(2)).getY() + Math.pow(t, (double) 3.0F) * ((Point) trajectoryPoints.get(3)).getY();

            point = new Point(x, y, serverTime);
        }

        return point;
    }

    private Point getLinerEnemyPosition(RoomEnemy enemy, long serverTime) {
        List<Point> points = enemy == null ?
                null : enemy.getTrajectory() == null ?
                null : enemy.getTrajectory().getPoints();
        Point[] trajectoryPoints = points.toArray(new Point[0]);

        if (trajectoryPoints.length == 0) {
            return null;
        }

        //multiple linear
        if (trajectoryPoints[0].getTime() > serverTime) {
            return new Point(trajectoryPoints[0].getX(), trajectoryPoints[0].getY(), serverTime);
        }

        Point lastPoint = trajectoryPoints[trajectoryPoints.length - 1];
        if (serverTime >= lastPoint.getTime()) {
            return new Point(lastPoint.getX(), lastPoint.getY(), serverTime);
        }

        int i = 1;
        while (i < trajectoryPoints.length && serverTime > trajectoryPoints[i].getTime()) {
            i++;
        }

        Point a = trajectoryPoints[i - 1];
        Point b = trajectoryPoints[i];
        double percent = ((double) (serverTime - a.getTime())) / (b.getTime() - a.getTime());

        return new Point(a.getX() + (b.getX() - a.getX()) * percent, a.getY() + (b.getY() - a.getY()) * percent, serverTime);
    }

    @Override
    public Point getLocationOnScreen(RoomEnemy roomEnemy, long serverTime) {

        //LOG.debug("getLocationOnScreen: serverTime={}, roomEnemy:{}", toHumanReadableFormat(serverTime), roomEnemy);

        if(roomEnemy == null || roomEnemy.getTrajectory() == null || roomEnemy.getTrajectory().getPoints() == null) {
            return null;
        }

        Trajectory trajectory =  roomEnemy.getTrajectory();
        List<Point> trajectoryPoints = trajectory.getPoints();

        if(trajectoryPoints.size() == 0) {
            return null;
        }

        boolean isBezierTrajectory = trajectory.isBezierTrajectory() != null && trajectory.isBezierTrajectory();
        boolean isCircularTrajectory = trajectory.isCircularTrajectory() != null && trajectory.isCircularTrajectory();
        boolean isHybridTrajectory = trajectory.isHybridTrajectory() != null && trajectory.isHybridTrajectory();

        int parentEnemyTypeId = (int)roomEnemy.getParentEnemyTypeId();

        Point point = null;

        try {
            if (trajectoryPoints.size() == 1) {
                point = trajectoryPoints.get(0);
            } else {

                if (isBezierTrajectory) {
                  /* if (trajectoryPoints.size() == 3) {
                    point = getQuadraticBezierEnemyPosition(roomEnemy, serverTime);
                  } else if (trajectoryPoints.size() == 4) {
                    point = getQuadraticBezierEnemyPosition(roomEnemy, serverTime);
                  } else {
                    point = getBezierEnemyPosition(roomEnemy, serverTime);
                  }
                }*/
                    point = getGenericBezierEnemyPosition(roomEnemy, serverTime);
                } else {
                    point = getLinerEnemyPosition(roomEnemy, serverTime);
                }


                if (isCircularTrajectory && point != null) {
                    Point lCircularPosition_pt = this.getCircularPosition(roomEnemy, serverTime);
                    point.setX(point.getX() + lCircularPosition_pt.getX());
                    point.setY(point.getY() + lCircularPosition_pt.getY());
                }
            }

            //LOG.debug("getLocationOnScreen: roomEnemyId:{}, point:{}", roomEnemy.getId(), point);

            /*if (isCircularTrajectory && parentEnemyTypeId != -1) {
                int circularAngle = trajectory.getCircularAngle();
                point = getCircularPoint(parentEnemyTypeId, point, trajectoryPoints.get(0).getTime(), roomEnemy.getSpeed(), circularAngle, serverTime);
            }*/
        } catch (Exception e) {
            LOG.error("getLocationOnScreen: Exception for roomEnemy:{}", roomEnemy, e);
        }

        return point;
    }

    @Override
    public boolean isLocationOnMapAllowedForShot(List<PointExt> points, long serverTime, int currentMapId, int enemyType,
                                                 Point serverLocationByEnemy, GameMapShape map) {

        if (points.size() >= 2) {
            if(map != null && serverLocationByEnemy != null) {
                int x = (int)serverLocationByEnemy.getX();
                int y = (int)serverLocationByEnemy.getY();
                boolean isValid = map.isValidWithAssumption(x, y, -5);
                if(!isValid) {
                    return false;
                }
                if(!map.isPassable(x, y)) {
                    return false;
                }
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
            boolean result = !invulnerable && !teleport;
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
    public Long[] getRequestedEnemiesIds() {
        return requestedEnemyTypeIds;
    }

    @Override
    public boolean allowShotAfterRoundFinishSoon() {
        return false;
    }

    @Override
    public String toString() {
        return "NaturalBattleGroundSectorXStrategy{" + "LOG=" + LOG +
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
