package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.betsoft.casino.mp.model.EnemyDestroyReason.REMOVED_ON_SERVER;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

/**
 * User: flsh
 * Date: 27.01.2022.
 */
/**
 * The main state of round. Players play in this state.
 */
public abstract class AbstractActionPlayGameState<GAMEROOM extends AbstractActionGameRoom, SEAT extends IActionGameSeat, MAP extends IMap, GS extends IGameState>
        extends AbstractPlayGameState<GAMEROOM, SEAT, MAP, GS> {
    private static final byte VERSION = 2;
    public static final int REGULAR_WEAPON = -1;
    private static final int MAX_ALIVE_ENEMIES = 12;
    private static final int MAX_SCARABS = 20;
    protected PlaySubround subround = PlaySubround.BASE;
    protected volatile boolean mainBossIsAvailable = false;
    protected long totalCountMainBossAppeared = 0;
    protected int remainingNumberOfBoss = 0;
    protected volatile long lastShotTime;
    protected volatile long timeOfStartBossRound;
    protected int spawnProbability = 30;
    /**  allow spawn mobs on map  */
    protected volatile boolean allowSpawn = false;
    protected volatile boolean allowSpawnHW = false;
    protected transient int currentKilledEnemies = 0;
    protected List<Long> bossesSpawnTime;
    protected int spawnedBossesCounter;
    protected long prevBossSpawnTime;
    /**  additional counters in play game state  */
    protected Map<String, Integer> additionalActionCounters;
    protected Map<String, List<Long>> additionalActionSpawnTimes;

    protected AbstractActionPlayGameState() {
        super();
    }

    @SuppressWarnings("rawtypes")
    protected AbstractActionPlayGameState(GAMEROOM gameRoom, AbstractQuestManager questManager) {
        super(gameRoom, questManager);
    }

    @Override
    public void init() throws CommonException {
        super.init();
        subround = PlaySubround.BASE;
        totalCountMainBossAppeared = 0;
        remainingNumberOfBoss = 0;
        lastShotTime = System.currentTimeMillis();
        currentKilledEnemies = 0;
        spawnedBossesCounter = 0;
        prevBossSpawnTime = 0;
    }

    @Override
    protected void initSeats() throws CommonException {
        List<SEAT> seats = gameRoom.getSeats();
        for (SEAT seat : seats) {
            if (seat != null) {
                if (makeBuyInForCashBonusOrTournament(seat)) {
                    sendWeaponsInfo(seat);
                }
            }
        }
        super.initSeats();
        allowSpawn = true;
        allowSpawnHW = true;
    }

    @Override
    public void restoreGameRoom(GAMEROOM gameRoom) throws CommonException {
        this.gameRoom = gameRoom;
        gameRoom.lock();
        try {
            long durationRound = isFRB() ? 40000 : gameRoom.getRoundDuration() * 1000L;
            gameRoom.setTimerTime(durationRound);
            endRoundTime = System.currentTimeMillis() + durationRound;
            if (isRoundWasFinished()) {
                getLog().warn("isRoundWasFinished, reset to false for correct finish");
                setRoundWasFinished(false);
            }
            gameRoom.startUpdateTimer();
            List<SEAT> seats = gameRoom.getSeats();
            for (SEAT seat : seats) {
                if (seat != null) {
                    if (makeBuyInForCashBonusOrTournament(seat)) {
                        sendWeaponsInfo(seat);
                    }
                }
            }
            gameRoom.startTimer();
        } finally {
            gameRoom.unlock();
        }
    }

    protected boolean makeBuyInForCashBonusOrTournament(SEAT seat) {
        boolean sendWeapons = true;
        if (seat.getPlayerInfo() != null) {
            IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            if (playerInfo.getActiveCashBonusSession() != null) {
                gameRoom.makeBuyInForCashBonus(seat);
            } else if (playerInfo.getTournamentSession() != null) {
                if (seat.getAmmoAmount() > 0) {
                    sendWeapons = false;
                } else {
                    gameRoom.makeBuyInForTournament(seat);
                }
            }
        }
        return sendWeapons;
    }

    protected abstract List<ShootResult> shootWithSpecialWeaponAndUpdateState(long time, SEAT seat, IShot shot, int weaponId,
                                                                              ShotMessages messages) throws CommonException;

    protected abstract void spawnMummy();

    public abstract void spawnEnemyFromTeststand(int typeId, int skinId, Trajectory trajectory, long parentEnemyId);

    protected abstract void generateHVEnemy(ShootResult result, ShotMessages messages, String sessionId);

    protected abstract ShootResult shootToOneEnemy(long time, SEAT seat, Long itemIdForShot, int weaponId, boolean isNearLandMine,
                                                   double damageMultiplier) throws CommonException;

    public abstract void processShot(SEAT seat, IShot shot, boolean isInternalShot) throws CommonException;

    protected int getMaxAliveEnemies() {
        return MAX_ALIVE_ENEMIES;
    }

    protected int getMaxAliveCritters() {
        return MAX_SCARABS;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void finishSeat(SEAT seat) {
        gameRoom.saveMinesWithLock(seat);
        gameRoom.compensateSpecialWeaponsWithLock(seat);
        seat.transferRoundWin();
    }

    /**
     * Shot from main pistol weapon.
     * @param time time
     * @param seat seat of player
     * @param shot shot message from client
     * @throws CommonException if any unexpected error occur
     */
    protected void shootWithRegularWeapon(long time, SEAT seat, IShot shot) throws CommonException {
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), getSeatNumber(seat),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), getSeatNumber(seat),
                        shot.getWeaponId(), 0));

        ShootResult result = shootWithRegularWeaponAndUpdateState(time, seat, shot.getEnemyId(), messages);
        int awardedWeaponId = !result.isNewWeapon() ? -1 : result.getWeapon().getType().getId();
        if (!result.isKilledMiss() && !result.isInvulnerable()) {
            seat.decrementAmmoAmount();
        }
        processShootResult(seat, shot, result, messages, awardedWeaponId, true);
        List<SEAT> seats = gameRoom.getSeats();
        for (SEAT seatCurrent : seats) {
            if (!isFRB()) {
                seatCurrent.transferWinToAmmo();
            }
        }
        messages.send(seat.getSpecialWeaponRemaining(), shot);
    }

    /**
     * Calculation of shot in enemy game.
     * @param time current time
     * @param seat seat of player
     * @param itemIdForShot id of enemy
     * @param messages shot messages
     * @return {@code ShootResult} result of shot
     * @throws CommonException if any unexpected error occur
     */
    protected ShootResult shootWithRegularWeaponAndUpdateState(long time, SEAT seat, Long itemIdForShot, ShotMessages messages)
            throws CommonException {
        ShootResult result = shootToOneEnemy(time, seat, itemIdForShot, REGULAR_WEAPON, false, 1);

        if (result.isNeedGenerateHVEnemy() && allowSpawnHW) {
            generateHVEnemy(result, messages, seat.getPlayerInfo().getSessionId());
        }
        getLog().debug("allowSpawnHW: {}, shootResult: {}", allowSpawnHW, result);

        return result;
    }

    /**
     * Shot from special weapons
     * @param time current time
     * @param seat seat of player
     * @param shot shot message from client
     * @throws CommonException if any unexpected error occur
     */
    protected void shootWithSpecialWeapon(long time, SEAT seat, IShot shot) throws CommonException {
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), getSeatNumber(seat),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), getSeatNumber(seat),
                        shot.getWeaponId(), 0));
        boolean isMine = shot.getWeaponId() == SpecialWeaponType.Landmines.getId();
        String mineId = isMine ? getSeatNumber(seat) + "_" + shot.getDate() : "";
        MinePoint mine = null;

        if (isMine) {
            for (Object seatMine : seat.getSeatMines()) {
                MinePoint point = (MinePoint) seatMine;
                if (point.getTimePlace() == shot.getDate()) {
                    mine = point;
                }
            }
            getLog().debug("current mine: {}", mine);
            if (mine == null) {
                return;
            }
        }

        getLog().debug("processing shot from special weapon: {}, seat.getSpecialWeaponId(): {}, mineId: {}",
                shot.getWeaponId(), seat.getSpecialWeaponId(), mineId);

        List<ShootResult> shootResults = shootWithSpecialWeaponAndUpdateState(time, seat, shot, shot.getWeaponId(), messages);

        int size = shootResults.size();
        for (ShootResult result : shootResults) {
            if (isMine) {
                result.setMineId(mineId);
            }
            size--;
            int awardedWeaponId = !result.isNewWeapon() ? -1 : result.getWeapon().getType().getId();
            processShootResult(seat, shot, result, messages, awardedWeaponId, size == 0);
        }
        if (isMine) {
            seat.getSeatMines().remove(mine);
            getLog().debug("remove mine, {}, new mines of accountId: {} is {}", mine,
                    seat.getAccountId(), seat.getSeatMines());
        }
        List<SEAT> seats = gameRoom.getSeats();
        for (SEAT seatCurrent : seats) {
            if (!isFRB()) {
                seatCurrent.transferWinToAmmo();
            }
        }
        messages.send(shot);
    }

    /**
     * Start of finishing current round.
     * @param endGame true if was finish before
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void firePlaySubroundFinished(boolean endGame) throws CommonException {
        gameRoom.lock();
        try {
            if (endGame) {
                getLog().debug("firePlaySubroundFinished: end game, move to QualifyGameState; subRound={}, " +
                        "pause={}", subround, pauseTime);
                getLog().debug("PlayGameState: stopTimer");
                gameRoom.stopUpdateTimer();
                if (lockShots != null && lockShots.isLocked() && !lockShots.isHeldByCurrentThread()) {
                    getLog().debug("PlayGameState: lockShots is locked, try lock/unlock");
                    lockShots.lock();
                    lockShots.unlock();
                    getLog().debug("PlayGameState: lockShots is locked, success lock/unlock");
                }
                allowSpawn = false;
                allowSpawnHW = false;
                if (getMap().getItemsSize() > 0) {
                    getLog().debug("firePlaySubroundFinished: found enemies in playGameState, will be cleared");
                    getMap().removeAllEnemies();
                }
                setQualifyGameState();
            } else {
                PlaySubround newSubRound = subround.getNext();
                getLog().debug("firePlaySubroundFinished: old subround={}, new subround={}", subround, newSubRound);
                subround = newSubRound;
                gameRoom.sendChanges(getTOFactoryService().createChangeMap(getCurrentTime(), gameRoom.getMapId(),
                        newSubRound.name()));
            }
        } finally {
            gameRoom.unlock();
        }
    }

    /**
     * Processing shot result from enemy game and send to client results of shot.
     * @param seat seat of player
     * @param shot shot message from client
     * @param result shot result from enemy game
     * @param messages shot messages
     * @param awardedWeaponId new awarded weaponId
     * @param isLastResult true if result of shot is last. For special weapons can a lot of result of shots for one real shot from client.
     */
    protected void processShootResult(SEAT seat, IShot shot, IShootResult result, ShotMessages messages,
                                      int awardedWeaponId, boolean isLastResult) {
    }

    public void setRemainingNumberOfBoss(int remainingNumberOfBoss) {
        this.remainingNumberOfBoss = remainingNumberOfBoss;
    }

    public long getTimeFromLastShotTime() {
        return (System.currentTimeMillis() - lastShotTime) / 1000;
    }

    @Override
    public long getStartTime() {
        return subround == PlaySubround.BOSS ? timeOfStartBossRound : 0;
    }

    public void setSpawnProbability(int spawnProbability) {
        this.spawnProbability = spawnProbability;
    }

    public boolean isAllowSpawn() {
        return allowSpawn;
    }

    @Override
    public PlaySubround getSubround() {
        return subround;
    }

    public void incCurrentKilledEnemies() {
        currentKilledEnemies++;
    }

    public void setBossesSpawnTime(List<Long> bossesSpawnTime) {
        this.bossesSpawnTime = bossesSpawnTime;
    }

    private void sendWeaponsInfo(SEAT seat) {
        IWeapons weapons = getTOFactoryService().createWeapons(System.currentTimeMillis(), SERVER_RID,
                seat.getAmmoAmount(), isFRB(), getSeatWeapons(seat));
        seat.sendMessage(weapons);
    }

    private List<ITransportWeapon> getSeatWeapons(SEAT seat) {
        List<ITransportWeapon> weapons = new ArrayList<>();
        Map<SpecialWeaponType, IWeapon> seatWeapons = seat.getWeapons();
        seatWeapons.forEach((specialWeaponType, weapon) -> {
            weapons.add(getTOFactoryService().createWeapon(specialWeaponType.getId(), weapon.getShots()));
        });
        return weapons;
    }

    public Map<String, List<Long>> getAdditionalActionSpawnTimes() {
        if(additionalActionSpawnTimes == null){
            additionalActionSpawnTimes = new HashMap<>();
        }
        return additionalActionSpawnTimes;
    }

    public void addAdditionalActionSpawnTimes(String key, List<Long> times) {
        getAdditionalActionSpawnTimes().put(key, times);
    }

    public void clearAdditionalActionCounters(){
        getAdditionalActionCounters().clear();
    }

    public void clearAdditionalActionSpawnTimes(){
        getAdditionalActionSpawnTimes().clear();
    }


    public Map<String, Integer> getAdditionalActionCounters() {
        if(additionalActionCounters == null){
            additionalActionCounters = new HashMap<>();
        }
        return additionalActionCounters;
    }

    public void addAdditionalActionCounter(String key, Integer addValue) {
        if(additionalActionCounters == null){
            additionalActionCounters = new HashMap<>();
        }
        additionalActionCounters.put(key, addValue + additionalActionCounters.getOrDefault(key, 0));
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        output.writeInt(subround.ordinal(), true);
        output.writeBoolean(mainBossIsAvailable);
        output.writeLong(totalCountMainBossAppeared, true);
        output.writeInt(remainingNumberOfBoss, true);
        output.writeLong(lastShotTime, true);
        output.writeLong(timeOfStartBossRound, true);
        output.writeInt(spawnProbability, true);
        output.writeBoolean(allowSpawn);
        output.writeBoolean(allowSpawnHW);
        output.writeInt(currentKilledEnemies, true);
        kryo.writeClassAndObject(output, bossesSpawnTime);
        output.writeInt(spawnedBossesCounter, true);
        output.writeLong(prevBossSpawnTime, true);
        kryo.writeClassAndObject(output, getAdditionalActionCounters());
        kryo.writeClassAndObject(output, getAdditionalActionSpawnTimes());
    }

    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        subround = PlaySubround.values()[input.readInt(true)];
        mainBossIsAvailable = input.readBoolean();
        totalCountMainBossAppeared = input.readLong(true);
        remainingNumberOfBoss = input.readInt(true);
        lastShotTime = input.readLong(true);
        timeOfStartBossRound = input.readLong(true);
        spawnProbability = input.readInt(true);
        allowSpawn = input.readBoolean();
        allowSpawnHW = input.readBoolean();
        currentKilledEnemies = input.readInt(true);
        bossesSpawnTime = (List<Long>) kryo.readClassAndObject(input);
        spawnedBossesCounter = input.readInt(true);
        prevBossSpawnTime = input.readLong(true);
        if(version >= 2){
            additionalActionCounters = (Map<String, Integer>) kryo.readClassAndObject(input);
            additionalActionSpawnTimes = (Map<String, List<Long>>) kryo.readClassAndObject(input);
        }
    }

    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("subround", subround.ordinal());
        gen.writeBooleanField("mainBossIsAvailable", mainBossIsAvailable);
        gen.writeNumberField("totalCountMainBossAppeared", totalCountMainBossAppeared);
        gen.writeNumberField("remainingNumberOfBoss", remainingNumberOfBoss);
        gen.writeNumberField("lastShotTime", lastShotTime);
        gen.writeNumberField("timeOfStartBossRound", timeOfStartBossRound);
        gen.writeNumberField("spawnProbability", spawnProbability);
        gen.writeBooleanField("allowSpawn", allowSpawn);
        gen.writeBooleanField("allowSpawnHW", allowSpawnHW);
        gen.writeNumberField("currentKilledEnemies", currentKilledEnemies);
        serializeListField(gen, "bossesSpawnTime", bossesSpawnTime, new TypeReference<List<Long>>() {});
        gen.writeNumberField("spawnedBossesCounter", spawnedBossesCounter);
        gen.writeNumberField("prevBossSpawnTime", prevBossSpawnTime);
        serializeMapField(gen, "additionalActionCounters", getAdditionalActionCounters(), new TypeReference<Map<String,Integer>>() {});
        serializeMapField(gen, "additionalActionSpawnTimes", getAdditionalActionSpawnTimes(), new TypeReference<Map<String,List<Long>>>() {});
    }

    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();

        subround = PlaySubround.values()[node.get("subround").intValue()];
        mainBossIsAvailable = node.get("mainBossIsAvailable").booleanValue();
        totalCountMainBossAppeared = node.get("totalCountMainBossAppeared").longValue();
        remainingNumberOfBoss = node.get("remainingNumberOfBoss").intValue();
        lastShotTime = node.get("lastShotTime").longValue();
        timeOfStartBossRound = node.get("timeOfStartBossRound").longValue();
        spawnProbability = node.get("spawnProbability").intValue();
        allowSpawn = node.get("allowSpawn").booleanValue();
        allowSpawnHW = node.get("allowSpawnHW").booleanValue();
        currentKilledEnemies = node.get("currentKilledEnemies").intValue();
        bossesSpawnTime = om.convertValue(node.get("bossesSpawnTime"), new TypeReference<List<Long>>() {});
        spawnedBossesCounter = node.get("spawnedBossesCounter").intValue();
        prevBossSpawnTime = node.get("prevBossSpawnTime").longValue();
        additionalActionCounters = om.convertValue(node.get("additionalActionCounters"), new TypeReference<Map<String, Integer>>() {});
        additionalActionSpawnTimes = om.convertValue(node.get("additionalActionSpawnTimes"), new TypeReference<Map<String, List<Long>>>() {});
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractActionPlayGameState [");
        sb.append("subround=").append(subround);
        sb.append(", mainBossIsAvailable=").append(mainBossIsAvailable);
        sb.append(", totalCountMainBossAppeared=").append(totalCountMainBossAppeared);
        sb.append(", remainingNumberOfBoss=").append(remainingNumberOfBoss);
        sb.append(", lastShotTime=").append(lastShotTime);
        sb.append(", timeOfStartBossRound=").append(timeOfStartBossRound);
        sb.append(", spawnProbability=").append(spawnProbability);
        sb.append(", allowSpawn=").append(allowSpawn);
        sb.append(", allowSpawnHW=").append(allowSpawnHW);
        sb.append(", currentKilledEnemies=").append(currentKilledEnemies);
        sb.append(", bossesSpawnTime=").append(bossesSpawnTime);
        sb.append(", additionalActionCounters=").append(getAdditionalActionCounters());
        sb.append(", additionalActionSpawnTimes=").append(getAdditionalActionSpawnTimes());
        sb.append(super.toString());
        sb.append(']');
        return sb.toString();
    }
}
