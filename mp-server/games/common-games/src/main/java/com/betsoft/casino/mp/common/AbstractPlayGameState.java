package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.InboundObject;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ConcurrentHashSet;
import com.abs.casino.common.util.RNG;
import com.abs.casino.common.web.statistics.StatisticsManager;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.betsoft.casino.mp.model.EnemyDestroyReason.REMOVED_ON_SERVER;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

/**
 * User: flsh
 * Date: 14.02.19.
 */
public abstract class AbstractPlayGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        extends AbstractGameState<GAMEROOM, SEAT, MAP, GS> {
    private static final byte VERSION = 1;

    protected static final int FREEZE_TIME_MAX = 3000;
    protected boolean roundWasFinished = false;
    protected volatile boolean needWaitingWhenEnemiesLeave = false;
    protected volatile boolean needForceFinishRound = false;
    /** time of pause to next state (ms) */
    protected int pauseTime;
    private Set<MathEnemy> liveSimpleEnemiesOfRound = new ConcurrentHashSet<>();
    protected transient AbstractQuestManager questManager;
    protected transient boolean optimalStrategy = false;
    protected transient String currentModel = "";
    private transient boolean needMinimalEnemies;
    /** manualGenerationEnemies is used for local tests of action games for generation of required enemies */
    private transient boolean manualGenerationEnemies = false;
    /** lock in room by shots */
    protected transient ReentrantLock lockShots = new ReentrantLock();
    /** start time (ms) */
    protected long startRoundTime;
    /** end time (ms) */
    protected long endRoundTime;

    public AbstractPlayGameState() {
        super();
    }

    public AbstractPlayGameState(GAMEROOM gameRoom, AbstractQuestManager questManager) {
        super(gameRoom);
        this.questManager = questManager;
    }

    protected abstract void setWaitingGameState() throws CommonException;

    protected abstract void setQualifyGameState() throws CommonException;

    protected abstract void setPossibleEnemies();


    @Override
    public RoomState getRoomState() {
        return RoomState.PLAY;
    }

    @Override
    public void init() throws CommonException {
        gameRoom.lock();
        try {
            super.init();
            roundWasFinished = false;
            short seatsCount = gameRoom.getSeatsCount();
            gameRoom.clearSeatDataFromPreviousRound();
            pauseTime = 3000;
            needWaitingWhenEnemiesLeave = false;
            needForceFinishRound = false;
            if (seatsCount >= gameRoom.getMinSeats()) {
                initSeats();
            } else {
                setWaitingGameState();
            }
            needMinimalEnemies = false;
            TestStandLocal.getInstance().removeMinimalEnemiesModeForRoom(gameRoom.getId());
        } finally {
            gameRoom.unlock();
        }
    }

    protected void initSeats() throws CommonException {
        gameRoom.sendStartNewRoundToAllPlayers(gameRoom.getSeats());
        setPossibleEnemies();
        long roundDuration = isFRB() ? 40000 : gameRoom.getRoundDuration() * 1000L;
        startRoundTime = System.currentTimeMillis();
        endRoundTime = startRoundTime + roundDuration;
        gameRoom.startUpdateTimer();
        gameRoom.setTimerTime(roundDuration);
        gameRoom.startTimer();
        getLog().debug("init PlayGameState completed");
    }

    @Override
    public void restoreGameRoom(GAMEROOM gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);
        gameRoom.lock();
        try {
            gameRoom.setTimerTime(isFRB() ? 40000 : gameRoom.getRoundDuration() * 1000L);
            if (isRoundWasFinished()) {
                getLog().warn("isRoundWasFinished, reset to false for correct finish");
                setRoundWasFinished(false);
            }
            gameRoom.startUpdateTimer();
            gameRoom.startTimer();
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public void update() throws CommonException, InterruptedException {
        long now = System.currentTimeMillis();
        String gameName = gameRoom.getRoomInfo().getGameType().name();
        boolean locked = gameRoom.tryLock(10, TimeUnit.MILLISECONDS);
        if (locked) {
            getLog().debug("Updating room");
            try {
                StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayGameState::get lock " +
                                gameName,
                        System.currentTimeMillis() - now, "" + getRoomInfo().getId() + ":" + getRoomInfo().getRoundId());
                now = System.currentTimeMillis();
                updateWithLock();
            } catch (Exception e) {
                getLog().debug(" update log error ", e);
            } finally {
                long time = System.currentTimeMillis() - now;
                if (time > 5000) {
                    getLog().warn("update, long time delaying time: {}", time);
                }
                gameRoom.unlock();
                StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayGameState::update time "
                        + gameName, time, "" + getRoomInfo().getId() + ":" + getRoomInfo().getRoundId());
            }
        } else {
            getLog().debug("Skipping room update");
        }
    }

    protected abstract void updateWithLock() throws CommonException;

    public void checkTestStandFeatures() {
        if (!needMinimalEnemies && TestStandLocal.getInstance().needMinimalEnemiesModeForRoom(gameRoom.getId())) {
            needMinimalEnemies = true;
            getLog().debug("setMinimalNumberOfEnemies applied");
        }
    }

    protected ITransportObjectsFactoryService getTOFactoryService() {
        return gameRoom.getTOFactoryService();
    }

    protected void checkLevelUp(SEAT seat, ShotMessages messages) {
        if (seat.isLevelUp()) {
            messages.addAllMessage(getTOFactoryService().createLevelUp(System.currentTimeMillis(), SERVER_RID,
                    getSeatNumber(seat), seat.getLevel()));
        }
    }

    protected IExperience collectScore(IShootResult result, SEAT seat) {
        int score = 0;

        if (result.getWin().greaterThan(Money.ZERO)) {
            boolean isHVEnemy = result.getEnemy().getEnemyClass().getEnemyType().isHVenemy();
            if (result.isDestroyed()) {
                if (isHVEnemy) {
                    score += RNG.nextInt(100, 500);
                } else {
                    score += result.getEnemy().getEnemyClass().getEnemyType().getReward();
                }
            } else {
                if (isHVEnemy) {
                    score += RNG.nextInt(2, 50);
                } else {
                    score += 1;
                }
            }
        }
        return getTOFactoryService().createExperience(score * gameRoom.getExpScale(seat));
    }

    public abstract void doFinishWithLock();

    protected void finish() throws CommonException {
        getLog().debug("finish from game logic, calculate qualify wins for all seats, isRoundWasFinished() {}",
                isRoundWasFinished());
        if (isRoundWasFinished()) {
            return;
        }
        setRoundWasFinished(true);

        try {
            //noinspection unchecked
            finishSeats(gameRoom.getSeats());
            firePlaySubroundFinished(true);
        } catch (Exception e) {
            getLog().error("Failed finish, reset roundWasFinished", e);
            setRoundWasFinished(false);
        }
    }

    protected void finishSeats(List<SEAT> seats) {
        for (SEAT seat : seats) {
            finishSeat(seat);
        }
    }

    @SuppressWarnings("unchecked")
    protected void finishSeat(SEAT seat) {
        seat.transferRoundWin();
    }

    @Override
    public boolean isSitInAllowed() {
        return true;
    }

    @Override
    public void processSitIn(SEAT seat) {
        seat.setWantSitOut(false);
    }

    public void onTimer(boolean needClearEnemy) {
        boolean locked = false;
        try {
            if (gameRoom.tryLock(5, TimeUnit.SECONDS)) {
                getLog().debug("Finishing round with locked update");
                locked = true;
            } else {
                getLog().debug("Finishing round without locked update");
            }
            onTimerWithLock(needClearEnemy);
        } catch (Exception e) {
            getLog().warn("onTimer error log, ", e);
        } finally {
            if (locked) {
                gameRoom.unlock();
            }
        }
    }

    protected abstract void onTimerWithLock(boolean needClearEnemy);

    protected void sendLeaveTrajectories() {
        getLog().debug("sendLeaveTrajectories");
        try {
            Map<Long, Trajectory> trajectories = new HashMap<>();
            Map<Long, Trajectory> shortLeaveTrajectories = gameRoom.getMap().generateShortLeaveTrajectories();
            shortLeaveTrajectories.forEach((id, trajectory) ->
                    trajectories.put(id, gameRoom.convertFullTrajectory(trajectory)));
            gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(System.currentTimeMillis(), SERVER_RID, trajectories, 0,
                    EnemyAnimation.NO_ANIMATION.getAnimationId()));
        } catch (Exception e) {
            getLog().warn("sendLeaveTrajectories error log, ", e);
        }
    }

    protected void sendUpdateTrajectories(boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        Map<Long, Trajectory> updateTrajectories = gameRoom.getMap().generateUpdateTrajectories(needFinalSteps);
        if (!updateTrajectories.isEmpty()) {
            getLog().debug("updateTrajectories: {}", updateTrajectories);
            updateTrajectories.forEach((id, trajectory) ->
                    trajectories.put(id, gameRoom.convertTrajectory(trajectory, getCurrentTime())));
            gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(System.currentTimeMillis(), SERVER_RID,
                    trajectories, 0, EnemyAnimation.NO_ANIMATION.getAnimationId()));
        }
    }

    protected void sendCustomUpdateTrajectories(IEnemyRange range, EnemyAnimation enemyAnimation, boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        Map<Long, Trajectory> updateTrajectories =
                gameRoom.getMap().generateCustomUpdateTrajectories(range, enemyAnimation.getDuration(), needFinalSteps);
        getLog().debug("sendCustomUpdateTrajectories: {} range: {} enemyAnimation: {}",
                updateTrajectories, range, enemyAnimation);
        if (!updateTrajectories.isEmpty()) {
            updateTrajectories.forEach((id, trajectory) ->
                    trajectories.put(id, gameRoom.convertTrajectory(trajectory, getCurrentTime())));
            gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(System.currentTimeMillis(), SERVER_RID,
                    trajectories, 0, enemyAnimation.getAnimationId()));
        }
    }


    protected void sendFreezeTrajectories(long time, double x, double y, int d) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        Map<Long, Trajectory> freezeTrajectories = gameRoom.getMap().generateFreezeTrajectories(time, FREEZE_TIME_MAX, x, y, d);
        freezeTrajectories.forEach((id, trajectory) ->
                trajectories.put(id, gameRoom.convertTrajectory(trajectory, time)));
        gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(time, SERVER_RID,
                trajectories, FREEZE_TIME_MAX, EnemyAnimation.NO_ANIMATION.getAnimationId()));
    }

    protected void removeEnemies() {
        List<Long> enemyIds = getMap().removeAllEnemiesAndGetIds();
        for (Long enemyIdForRemoved : enemyIds) {
            gameRoom.sendChanges(
                    getTOFactoryService().createEnemyDestroyed(getCurrentTime(), SERVER_RID, enemyIdForRemoved,
                            REMOVED_ON_SERVER.ordinal()));
        }
    }

    @Override
    public int getCurrentMapId() {
        return gameRoom.getMapId();
    }

    @Override
    public boolean isBuyInAllowed(SEAT seat) {
        return true;
    }

    protected void sendError(SEAT seat, ITransportObject message, int code, String description,
                             InboundObject inboundObject) {
        seat.sendMessage(getTOFactoryService().createError(code, description, System.currentTimeMillis(),
                message.getRid()), inboundObject);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeBoolean(needWaitingWhenEnemiesLeave);
        output.writeInt(pauseTime, true);
        output.writeBoolean(roundWasFinished);
        kryo.writeClassAndObject(output, new HashSet(liveSimpleEnemiesOfRound));
        output.writeLong(startRoundTime, true);
        output.writeLong(endRoundTime, true);
        output.writeBoolean(needForceFinishRound);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        needWaitingWhenEnemiesLeave = input.readBoolean();
        pauseTime = input.readInt(true);
        roundWasFinished = input.readBoolean();
        HashSet<MathEnemy> wrappedSet = (HashSet<MathEnemy>) kryo.readClassAndObject(input);
        this.liveSimpleEnemiesOfRound = wrappedSet == null
                ? new ConcurrentHashSet<>()
                : new ConcurrentHashSet<>(wrappedSet);
        startRoundTime = input.readLong(true);
        endRoundTime = input.readLong(true);
        needForceFinishRound = input.readBoolean();
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeBooleanField("needWaitingWhenEnemiesLeave", needWaitingWhenEnemiesLeave);
        gen.writeNumberField("pauseTime", pauseTime);
        gen.writeBooleanField("roundWasFinished", roundWasFinished);
        serializeSetField(gen, "liveSimpleEnemiesOfRound", new HashSet<MathEnemy>(liveSimpleEnemiesOfRound), new TypeReference<Set<MathEnemy>>() {});
        gen.writeNumberField("startRoundTime", startRoundTime);
        gen.writeNumberField("endRoundTime", endRoundTime);
        gen.writeBooleanField("needForceFinishRound", needForceFinishRound);

        serializeAdditional(gen, serializers);


    }

    @Override
    public GS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = p.getCodec().readTree(p);

        needWaitingWhenEnemiesLeave = node.get("needWaitingWhenEnemiesLeave").booleanValue();
        pauseTime = node.get("pauseTime").intValue();
        roundWasFinished = node.get("roundWasFinished").booleanValue();
        HashSet<MathEnemy> wrappedSet = om.convertValue(node.get("liveSimpleEnemiesOfRound"), new TypeReference<HashSet<MathEnemy>>() {});
        this.liveSimpleEnemiesOfRound = wrappedSet == null
                ? new ConcurrentHashSet<>()
                : new ConcurrentHashSet<>(wrappedSet);
        startRoundTime = node.get("startRoundTime").longValue();
        endRoundTime = node.get("endRoundTime").longValue();
        needForceFinishRound = node.get("needForceFinishRound").booleanValue();

        deserializeAdditional(p, node, ctxt);

        return getDeserializer();
    }

    protected abstract void serializeAdditional(JsonGenerator gen, SerializerProvider serializers) throws IOException;

    protected abstract void deserializeAdditional(JsonParser p,
                                                  JsonNode node,
                                                  DeserializationContext ctxt) throws IOException;

    protected abstract GS getDeserializer();

    public boolean isRoundWasFinished() {
        return roundWasFinished;
    }

    public void setRoundWasFinished(boolean roundWasFinished) {
        this.roundWasFinished = roundWasFinished;
    }

    public MathEnemy getNextMathEnemy(String enemyType) {
        Optional<MathEnemy> first = liveSimpleEnemiesOfRound.stream()
                .filter(mathEnemy -> mathEnemy.getTypeName().equals(enemyType)).findFirst();

        if (first.isPresent()) {
            MathEnemy mathEnemy = first.get();
            liveSimpleEnemiesOfRound.remove(mathEnemy);
            getLog().debug(" math for new enemy: {} remaining enemies: {}", mathEnemy, liveSimpleEnemiesOfRound.size());
            return mathEnemy;
        }
        return null;
    }

    public boolean enemyIsPossibleForGeneration(String enemyType) {
        return liveSimpleEnemiesOfRound.stream().anyMatch(mathEnemy -> mathEnemy.getTypeName().equals(enemyType));
    }


    public int getCountRemainingEnemiesByModel() {
        return liveSimpleEnemiesOfRound.size();
    }

    public Set<MathEnemy> getLiveSimpleEnemiesOfRound() {
        return liveSimpleEnemiesOfRound;
    }

    public boolean isOptimalStrategy() {
        return optimalStrategy;
    }

    public void setOptimalStrategy(boolean optimalStrategy) {
        getLog().debug("set optimal strategy (tests only): {}", optimalStrategy);
        this.optimalStrategy = optimalStrategy;
    }

    public boolean isNeedWaitingWhenEnemiesLeave() {
        return needWaitingWhenEnemiesLeave;
    }

    public void setNeedForceFinishRound(boolean needForceFinishRound) {
        this.needForceFinishRound = needForceFinishRound;
    }

    public boolean isNeedMinimalEnemies() {
        return needMinimalEnemies;
    }

    public void setNeedMinimalEnemies(boolean needMinimalEnemies) {
        this.needMinimalEnemies = needMinimalEnemies;
    }

    public boolean isManualGenerationEnemies() {
        return manualGenerationEnemies;
    }

    public void setManualGenerationEnemies(boolean manualGenerationEnemies) {
        this.manualGenerationEnemies = manualGenerationEnemies;
    }

    public int getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(int pauseTime) {
        this.pauseTime = pauseTime;
    }

    public ReentrantLock getLockShots() {
        return lockShots;
    }

    @Override
    public long getStartRoundTime() {
        return startRoundTime;
    }

    @Override
    public long getEndRoundTime() {
        return endRoundTime;
    }

    public Coords getCoords() {
        return getMap().getCoords();
    }

    @Override
    public boolean isAllowedRemoving() {
        return false;
    }

    @Override
    public boolean isAllowedKick() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayGameState [");
        sb.append(", needWaitingWhenEnemiesLeave=").append(needWaitingWhenEnemiesLeave);
        sb.append(", pauseTime=").append(pauseTime);
        sb.append(", roundWasFinished=").append(roundWasFinished);
        sb.append(", startRoundTime=").append(startRoundTime);
        sb.append(", endRoundTime=").append(endRoundTime);
        sb.append(", lockShots.isLocked()=").append(lockShots != null && lockShots.isLocked());
        sb.append(", needForceFinishRound=").append(needForceFinishRound);
        sb.append(']');
        return sb.toString();
    }
}
