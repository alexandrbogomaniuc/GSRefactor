package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractPlayGameState;
import com.betsoft.casino.mp.common.MaxCrashData;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyRange;
import com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfig;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.utils.TInboundObject;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.RNG;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import reactor.core.publisher.Mono;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The state of the room is RoomState.PLAY. During this state, players plays in room and can make to eject from flying rocket. SitIn is not allowed.
 */
@SuppressWarnings({"Duplicates", "unchecked"})
public class PlayGameState extends AbstractPlayGameState<AbstractCrashGameRoom, Seat, GameMap, PlayGameState> {
    private static final double Z = Math.pow(2, 52);
    private transient volatile double lastSendedMultiplier = 0;

    public PlayGameState() {
        super();
    }

    public PlayGameState(AbstractCrashGameRoom gameRoom) {
        super(gameRoom, null);
    }

    @Override
    public boolean isSitInAllowed() {
        return false;
    }

    /**
     * Inits play game state. Changes state in shared game state and prepares  {@link  MaxCrashData} for round.
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void init() throws CommonException {
        long roomId = getRoomId();
        gameRoom.lock();
        try {
            SharedCrashGameState crashGameState = getGameStateService().get(roomId, SharedCrashGameState.class);
            getLog().debug("PlayGameState.init: crashGameState={}", crashGameState);
            GameConfig gameConfig = getRoom().getGame().getConfig(getRoom().getId());
            if (crashGameState.getState() != RoomState.PLAY) {
                getLog().debug("Max Crash PlayGameState init start");
                initMaxCrashData(crashGameState, gameConfig);
                crashGameState.setState(RoomState.PLAY);
                crashGameState.setRoundStartTime(getCurrentTime());
                getGameStateService().put(crashGameState);
                super.init();
            } else {
                //may null after dirty shutdown
                if (crashGameState.getMaxCrashData() == null) {
                    initMaxCrashData(crashGameState, gameConfig);
                    getGameStateService().put(crashGameState);
                }
                roundWasFinished = false;
                gameRoom.clearSeatDataFromPreviousRound();
                pauseTime = 3000;
                needWaitingWhenEnemiesLeave = false;
                needForceFinishRound = false;
                initSeats();
            }
        } finally {
            gameRoom.unlock();
        }
    }

    /**
     * Inits base round data {@code MaxCrashData} for crash game.
     * @param crashGameState {@code SharedCrashGameState} game state for crash game
     * @param gameConfig {@code GameConfig} game config data
     */
    private void initMaxCrashData(SharedCrashGameState crashGameState, GameConfig gameConfig) {
        MaxCrashData maxCrashData = new MaxCrashData();

        maxCrashData.setStartTime(System.currentTimeMillis());

        maxCrashData.setSalt(RandomStringUtils.randomAscii(32));

        maxCrashData.setRoundId(getRoomInfo().getRoundId());

        long offsetStartTime = gameConfig.getInitialTime() > 0 ?
                gameConfig.getInitialTime() : 0L;

        maxCrashData.setOffsetStartTime(offsetStartTime);

        maxCrashData.setFunction(gameConfig.getFunction());

        double initMult = calcCrashTime(maxCrashData);

        double randomMultiplier = getRandomMultiplier(gameConfig);
        maxCrashData.setNaturalMultiplier(randomMultiplier);

        double maxAllowedMultiplier = getMaxAllowedCrashMultiplier();

        double crashMult = Math.min(randomMultiplier, maxAllowedMultiplier);
        double crashTime = calcCrashTime(crashMult);

        getLog().debug("initMaxCrashData: initMult={}, randomMultiplier={}, maxAllowedMultiplier={}, " +
                "crashMult={}, crashTime={}", initMult, randomMultiplier, maxAllowedMultiplier, crashMult, crashTime);


        if (RNG.rand() < 1 / gameConfig.getH()) {

            getLog().debug("initMaxCrashData: need crash instantly");

            maxCrashData.setCrashMult(1.0);

        } else {

            double currentMult = maxCrashData.getOffsetStartTime() == 0 ?
                    1.0 : initMult;

            getLog().debug("initMaxCrashData: currentMult={}, maxCrashData.getOffsetStartTime()={}, initMult={}",
                    currentMult, maxCrashData.getOffsetStartTime(), initMult);

            currentMult = Math.min(crashMult, currentMult);

            getLog().debug("initMaxCrashData: currentMult={}, crashMult={}", currentMult, crashMult);

            maxCrashData.setCrashMult(crashMult);
            maxCrashData.setCrashTime(crashTime);
            maxCrashData.setCurrentMult(currentMult);
        }

        /*
         * `double crashMult = Math.min(randomMultiplier, maxAllowedMultiplier);`
         *       -> if randomMultiplier > maxAllowedMultiplier --> crashMult = maxAllowedMultiplier
         * `if (RNG.rand() < 1 / gameConfig.getH())`
         *      -> if true --> crashMult = 1.0 --> crashMult != maxAllowedMultiplier
         */
        maxCrashData.setReachedMultiplierLimit(crashMult >= maxAllowedMultiplier);

        maxCrashData.setToken(generateToken(maxCrashData));

        lastSendedMultiplier = 0;

        getLog().debug("initMaxCrashData: maxCrashData={} ", maxCrashData);

        crashGameState.setMaxCrashData(maxCrashData);
    }

    /**
     * Calculates possible crash time (for internal tasks only)
     * @param crashMult crash multiplier
     * @return crash time (ms)
     */
    private double calcCrashTime(double crashMult) {
        try {
            return calc("Math.log(t) * 1000 / 0.06012", crashMult);
        } catch (Exception e) {
            getLog().error("Unable calculate crashTime, error: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Calculates max allowed crash multiplier for bank from ICrashGameSetting
     * @return max multiplier
     */
    private double getMaxAllowedCrashMultiplier() {
        ICrashGameSetting crashGameSetting = gameRoom.getCrashGameSetting();
        getLog().debug("getMaxAllowedCrashMultiplier: crashGameSetting={}", crashGameSetting);

        double settingsMaxMultiplier = getSettingsMaxMultiplier(crashGameSetting);

        try {
            double playersMultiplierLimit = getPlayersMultiplierLimit(crashGameSetting);

            double minMultiplierLimit = Math.min(settingsMaxMultiplier, playersMultiplierLimit);

            getLog().debug("getMaxAllowedCrashMultiplier: minMultiplierLimit={}, playersMultiplierLimit={}, " +
                            "settingsMaxMultiplier={}", minMultiplierLimit, playersMultiplierLimit, settingsMaxMultiplier);

            return minMultiplierLimit;

        } catch (Exception e) {
            getLog().error("getMaxAllowedCrashMultiplier: cannot calculate max allowed multiplier", e);
        }

        getLog().debug("getMaxAllowedCrashMultiplier: settingsMaxMultiplier={}", settingsMaxMultiplier);

        return settingsMaxMultiplier;
    }

    /**
     * get random multiplier by formula from math. team.
     * @param gameConfig {@code GameConfig} game config for set test crash multiplier.
     * @return random crash multiplier or crash multiplier from config.
     */
    private double getRandomMultiplier(GameConfig gameConfig) {
        double maxAllowedCrashMultiplier = getMaxAllowedCrashMultiplier();
        double randomMultiplier;
        long R;
        do {
            R = (long) (Z * RNG.rand());
            randomMultiplier = gameConfig.getCrashMultiplier() >= 1 ?
                    gameConfig.getCrashMultiplier() :
                    Math.floor((100.0 * Z - R) / (Z - R)) / 100.0;

        } while (randomMultiplier > maxAllowedCrashMultiplier);

        getLog().debug("getRandomMultiplier: Z={}, R={}, gameConfig.getCrashMultiplier()={}, randomMultiplier={}",
                Z, R, gameConfig.getCrashMultiplier(), randomMultiplier);

        return randomMultiplier;
    }

    private double getSettingsMaxMultiplier(ICrashGameSetting crashGameSetting) {

        double maxMultiplier = crashGameSetting.getMaxMultiplier();

        getLog().debug("getSettingsMaxMultiplier: maxMultiplier={}", maxMultiplier);

        return BigDecimal.valueOf(maxMultiplier).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double getPlayersMultiplierLimit(ICrashGameSetting crashGameSetting) {
        long totalBetsSum = 0;
        long maxPlayerBets = 0;

        for (Seat seat : gameRoom.getAllSeats()) {

            long seatBetsSum = 0;

            for (Map.Entry<String, ICrashBetInfo> betInfo : seat.getCrashBets().entrySet()) {
                ICrashBetInfo crashBetInfo = betInfo.getValue();
                seatBetsSum += crashBetInfo.getCrashBetAmount();
            }

            totalBetsSum += seatBetsSum;
            maxPlayerBets = Math.max(maxPlayerBets, seatBetsSum);
        }

        getLog().debug("getPlayersMultiplierLimit: totalBetsSum={}, maxPlayerBets={}", totalBetsSum, maxPlayerBets);

        if (totalBetsSum == 0) {
            return getSettingsMaxMultiplier(crashGameSetting);
        }

        double playerBetsMultiplier = maxPlayerBets == 0 ?
                crashGameSetting.getMaxPlayerProfitInRound() :
                (double) crashGameSetting.getMaxPlayerProfitInRound() / maxPlayerBets;

        double totalBetsMultiplier = (double) crashGameSetting.getTotalPlayersProfitInRound() / totalBetsSum;

        double minMultiplier = Math.min(playerBetsMultiplier, totalBetsMultiplier);

        getLog().debug("getPlayersMultiplierLimit: playerBetsMultiplier={}, totalBetsMultiplier={}, " +
                        "minMultiplier={}, crashGameSetting.getMaxPlayerProfitInRound()={}",
                playerBetsMultiplier, totalBetsMultiplier, minMultiplier, crashGameSetting.getMaxPlayerProfitInRound());

        if (minMultiplier < 1.01) {
            getLog().error("getPlayersMultiplierLimit: error, possible overflow, set limit to minMultiplier allowed value 1.01");
            minMultiplier = 1.01;
        }
        return BigDecimal.valueOf(minMultiplier).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public long getStartRoundTime() {
        return getSharedGameState().getRoundStartTime();
    }

    public MaxCrashData getMaxCrashData() {
        return getSharedGameState().getMaxCrashData();
    }

    /**
     * Gets shared game state. Game state is common for all mq servers for each room.
     * @return {@code SharedCrashGameState} shared game state
     */
    private SharedCrashGameState getSharedGameState() {
        SharedCrashGameState crashGameState = getGameStateService().get(getRoomId(), SharedCrashGameState.class);
        if (crashGameState == null) {
            getLog().error("getSharedGameState: crashGameState not found");
            throw new IllegalStateException("crashGameState not found for roomId=" + getRoomId());
        }
        return crashGameState;
    }

    private ISharedGameStateService getGameStateService() {
        return gameRoom.getSharedGameStateService();
    }

    private String generateToken(MaxCrashData data) {
        String input = "" + data.getRoundId() + data.getStartTime() + data.getCrashMult() + data.getSalt();
        SHA3.DigestSHA3 digest = new SHA3.Digest512();
        return Hex.toHexString(digest.digest(input.getBytes(StandardCharsets.US_ASCII)));
    }

    private void saveHistory(double mult, MaxCrashData data) {
        CrashRoundInfo crashRoundInfo = new CrashRoundInfo(mult, startRoundTime, data.getRoundId(), countBets(), data.getSalt(), data.getToken());
        getMap().addCrashHistory(crashRoundInfo);
        gameRoom.executeOnAllMembers(gameRoom.createUpdateCrashHistoryTask(crashRoundInfo));
    }

    /**
     * Gets total number of bets in room.
     * @return total number of bets
     */
    private int countBets() {
        int count = 0;
        for (Seat seat : getRoom().getSeats()) {
            ICrashGameRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            if (playerInfo != null) {
                count += seat.getCrashBetsCount();
            } else {
                getLog().error("Found seat without playerInfo: {}", seat);
            }
        }
        return count;
    }

    @Override
    protected void setWaitingGameState() throws CommonException {
        gameRoom.setGameState(new WaitingPlayersGameState(gameRoom));
    }

    @Override
    protected void setQualifyGameState() throws CommonException {
        gameRoom.setGameState(new QualifyGameState(gameRoom, getCurrentMapId(), pauseTime, startRoundTime, endRoundTime));
    }

    @Override
    protected void setPossibleEnemies() {
        gameRoom.setPossibleEnemies(EnemyRange.BASE_ENEMIES);
    }

    @Override
    public void update() throws CommonException, InterruptedException {
        updateWithLock();
    }

    /**
     * Every 100 ms updates the round data and processes the players who managed to jump out of the rocket. At the end of the round, the calculation
     * procedure will be launched, the round will end and the transition to the Qualify state will be started.
     */
    @Override
    protected void updateWithLock() {
        SharedCrashGameState sharedGameState = getSharedGameState();
        MaxCrashData maxCrashData = sharedGameState.getMaxCrashData();
        //need double-check for optimization
        if (maxCrashData == null) {
            getLog().error("PlayGameState:: updateWithLock, maxCrashData is null, may end round started, sharedGameState={}", sharedGameState);
            return;
        }
        if (maxCrashData.getCrashMult() == 0) {
            return;
        }
        double currentMult = maxCrashData.getCurrentMult();
        if (lastSendedMultiplier < currentMult && !maxCrashData.isNeedCrashInstantly()) {
            getLog().debug("PlayGameState:: updateWithLock, multiplier already increased on other server, just send message. " +
                    "lastSendedMultiplier={}, currentMult={}", lastSendedMultiplier, currentMult);
            if (lastSendedMultiplier < 1.0) {
                //need always send start multiplier
                sendCrashStateInfo(1.0, maxCrashData.getCrashMult());
                lastSendedMultiplier = 1.0;
            } else {
                sendCrashStateInfo(currentMult, maxCrashData.getCrashMult());
                lastSendedMultiplier = maxCrashData.getCurrentMult();
            }
            return;
        }
        gameRoom.lock();
        boolean needSent;
        try {
            sharedGameState = getSharedGameState();
            maxCrashData = sharedGameState.getMaxCrashData();
            if (maxCrashData == null) {
                getLog().error("PlayGameState:: updateWithLock, maxCrashData is null, may end round started, sharedGameState={}", sharedGameState);
                return;
            }
            if (maxCrashData.getCrashMult() == 0) {
                return;
            }
            currentMult = maxCrashData.getCurrentMult();
            if (lastSendedMultiplier < currentMult) {
                getLog().debug("PlayGameState:: updateWithLock, multiplier already increased on other server, just send message. " +
                        "lastSendedMultiplier={}, currentMult={}", lastSendedMultiplier, currentMult);
                needSent = true;
            } else {
                double newPossibleMult = calcCrashTime(maxCrashData);
                needSent = newPossibleMult > currentMult;
                double crashMult = maxCrashData.getCrashMult();
                if (newPossibleMult > crashMult || maxCrashData.isNeedCrashInstantly()) {
                    sharedGameState.setRoundResultProcessingStarted(true);
                    getGameStateService().put(sharedGameState);
                    performAutoEjectParallel(crashMult - 0.01, maxCrashData.getNaturalMultiplier(), maxCrashData.isReachedMultiplierLimit());
                    maxCrashData.setCurrentMult(crashMult);
                    maxCrashData.setCrashMult(crashMult);
                    getLog().debug("PlayGameState:: end round processing, sharedGameState  {}", sharedGameState);
                    if (!sharedGameState.isCalculationFinished()) {
                        saveHistory(crashMult, maxCrashData);
                        sharedGameState.setCalculationFinished(true);
                        getLog().debug("PlayGameState:: updateWithLock need finish: maxCrashData={}", maxCrashData);
                        double crashDataCurrentMult = maxCrashData.getCurrentMult();
                        Executors.newSingleThreadExecutor().execute(() -> {
                            ICrashStateInfo crashStateInfo = createCrashStateInfo(System.currentTimeMillis(), crashDataCurrentMult, 1.0, crashMult);
                            gameRoom.executeOnAllMembers(gameRoom.createSendSeatsMessageTask(null, false, -1, crashStateInfo, true));
                        });
                    }
                    sendCrashStateInfo(maxCrashData.getCurrentMult(), maxCrashData.getCrashMult());
                    getGameStateService().put(sharedGameState);
                    needSent = false;
                    nextSubRound();
                } else {
                    maxCrashData.setCurrentMult(newPossibleMult);
                    //if rocket crashed, all autoEject bets must be lost
                    if (Double.compare(newPossibleMult, crashMult) != 0) {
                        performAutoEjectParallel(newPossibleMult, maxCrashData.getNaturalMultiplier(), false);
                    } else {
                        getLog().debug("Skip call performAutoEjectParallel, rocket crashed, newPossibleMult={}", newPossibleMult);
                    }
                    getGameStateService().put(sharedGameState);
                }
            }
        } finally {
            gameRoom.unlock();
            if (maxCrashData != null) {
                lastSendedMultiplier = maxCrashData.getCurrentMult();
            }
        }
        if (needSent) {
            sendCrashStateInfo(maxCrashData.getCurrentMult(), maxCrashData.getCrashMult());
        }
    }

    private void sendCrashStateInfo(double currentMultiplier, double crashMult) {
        gameRoom.sendChanges(createCrashStateInfo(System.currentTimeMillis(), currentMultiplier, 1.0, crashMult));
    }

    private ICrashStateInfo createCrashStateInfo(long date, double currentMult, double timeSpeedMult, double crashMult) {
        ICrashStateInfo crashStateInfo = getTOFactoryService()
                .createCrashStateInfo(date, currentMult, timeSpeedMult);
        if (currentMult >= crashMult) {
            crashStateInfo.setCrash(true);
        }
        return crashStateInfo;
    }

    private double calcCrashTime(MaxCrashData maxCrashData) {

        double diffTime = System.currentTimeMillis() - maxCrashData.getStartTime() + maxCrashData.getOffsetStartTime();

        BigDecimal bd;

        try {
            double nextCrashTimeFromConfig = calc(maxCrashData.getFunction(), diffTime);

            if (nextCrashTimeFromConfig < 1) {
                getLog().error("invalid nextCrashTime {}, function: {}, time: {}",
                        nextCrashTimeFromConfig, maxCrashData.getFunction(), diffTime);
            }

            bd = BigDecimal.valueOf(nextCrashTimeFromConfig);

        } catch (ScriptException | NumberFormatException | NullPointerException e) {
            getLog().error("Unexpected incorrect function: {}, was replaced to: {}, time: {}",
                    getMaxCrashData().getFunction(), "Math.exp((diffTime * 0.0.06012) / 1000)", diffTime);
            bd = BigDecimal.valueOf(Math.exp((diffTime * 0.06012) / 1000));
        }

        return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double calc(String func, double time) throws ScriptException {
        ScriptEngine scriptEngine = getRoom().getScriptEngine();
        scriptEngine.put("t", time);
        return (double) scriptEngine.eval(func);
    }

    /**
     * Processing all players who auto-ejected from rocket
     * @param mult current multiplier
     * @param naturalMultiplier naturalMultiplier without limit
     * @param crashRocketAndLimitReached whether the multiplier limit has been reached
     */
    private void performAutoEjectParallel(double mult, double naturalMultiplier, boolean crashRocketAndLimitReached) {
        long now = System.currentTimeMillis();
        try {
            List<Seat> seats = gameRoom.getSeats();
            CountDownLatch latch = new CountDownLatch(seats.size());
            getLog().debug("performAutoEjectParallel: latch.count={}", latch.getCount());
            for (Seat dirtySeat : seats) {
                //optimization for prevent locking
                if (dirtySeat == null || dirtySeat.isAllCrashBetsEjected()) {
                    latch.countDown();
                    getLog().debug("performAutoEjectParallel: skip auto eject, all bets ejected latch.count={}", latch.getCount());
                    continue;
                }
                long accountId = dirtySeat.getAccountId();
                int autoPlayBetsCount = dirtySeat.getNotEjectedAutoPlayBetsCount();
                if (autoPlayBetsCount == 0 && !crashRocketAndLimitReached) {
                    latch.countDown();
                    getLog().debug("performAutoEjectParallel: skip auto eject, no autoPlay and not crashRocketAndLimitReached " +
                            "latch.count={}, accountId={}, autoPlayBetsCount={} ", latch.getCount(), accountId, autoPlayBetsCount);
                    continue;
                }
                performAutoEjectForSeat(mult, naturalMultiplier, dirtySeat, crashRocketAndLimitReached)
                        .doOnSuccess(result -> {
                            latch.countDown();
                            getLog().debug("performAutoEjectParallel: doOnSuccess accountId={}, latch.count={}", accountId, latch.getCount());
                        })
                        .doOnError(error -> {
                            latch.countDown();
                            getLog().error("performAutoEjectParallel: doOnError accountId={}, latch.count={}", accountId, latch.getCount(), error);
                        })
                        .doOnCancel(() -> {
                            latch.countDown();
                            getLog().error("performAutoEjectParallel: doOnCancel accountId={}, latch.count={}", accountId, latch.getCount());
                        })
                        .subscribeOn(gameRoom.getCrashScheduler())
                        .subscribe();
            }
            //critical section for performAutoEjectForSeat() must be completed after 1 second
            boolean success = latch.await(200, TimeUnit.MILLISECONDS);
            if (!success) {
                getLog().error("performAutoEjectParallel: first latch timeout, latch.count={}", latch.getCount());
                if (latch.getCount() > 2) {
                    success = latch.await(200, TimeUnit.MILLISECONDS);
                    if (!success) {
                        getLog().error("performAutoEjectParallel: second latch timeout, latch.count={}", latch.getCount());
                    }
                }
            } else {
                getLog().debug("performAutoEjectParallel: latch.await() success");
            }
        } catch (Exception e) {
            getLog().error("performAutoEjectParallel error", e);
        }
        getLog().debug("performAutoEjectParallel, time={}", System.currentTimeMillis() - now);
    }

    /**
     * Processing a player who auto-ejected from rocket
     * @param mult current multiplier
     * @param naturalMultiplier aturalMultiplier without limit
     * @param dirtySeat seat of player
     * @param crashRocketAndLimitReached whether the multiplier limit has been reached
     * @return {@code Mono<Void>}
     */
    private Mono<Void> performAutoEjectForSeat(double mult, double naturalMultiplier, Seat dirtySeat, boolean crashRocketAndLimitReached) {
        return Mono.create(sink -> {
            try {
                getLog().debug("performAutoEjectForSeat: started for accountId={}", dirtySeat.getAccountId());
                Map<ICrashCancelBet, Long> messages = new HashMap<>(3);
                boolean seatLocked = gameRoom.tryLockSeat(dirtySeat.getAccountId(), 1, TimeUnit.SECONDS);
                if (!seatLocked) {
                    getLog().error("performAutoEjectForSeat: cannot lockSeat, timeout, seat.accountId={}", dirtySeat.getAccountId());
                } else {
                    try {
                        Seat seat = gameRoom.getSeatByAccountId(dirtySeat.getAccountId());
                        if (seat == null) {
                            getLog().error("performAutoEjectForSeat: seat not found, dirtySeat={}", dirtySeat);
                        } else {
                            boolean needSaveSeat = false;
                            for (Map.Entry<String, ICrashBetInfo> betInfo : seat.getCrashBets().entrySet()) {
                                ICrashBetInfo crashBetInfo = betInfo.getValue();
                                if (crashBetInfo.isEjected()) {
                                    continue;
                                }
                                ICrashCancelBet cancelBet = null;
                                if (crashRocketAndLimitReached && crashBetInfo.isAutoPlay()) {
                                    getLog().debug("performAutoEjectForSeat: found autoEject by maxlimit, naturalMultiplier={}, crashBetInfo={}",
                                            naturalMultiplier, crashBetInfo);
                                    cancelBet = ejectBet(mult, crashBetInfo, betInfo.getKey(), seat, true);
                                } else if (crashBetInfo.isAutoPlay() && crashBetInfo.getMultiplier() <= mult) {
                                    cancelBet = ejectBet(crashBetInfo.getMultiplier(), crashBetInfo, betInfo.getKey(), seat, true);
                                }
                                if (cancelBet != null) {
                                    messages.put(cancelBet, seat.getAccountId());
                                    needSaveSeat = true;
                                }
                            }
                            if (needSaveSeat) {
                                gameRoom.saveSeat(0, seat);
                            }
                        }
                    } finally {
                        gameRoom.unlockSeat(dirtySeat.getAccountId());
                    }
                }
                for (Map.Entry<ICrashCancelBet, Long> entry : messages.entrySet()) {
                    Long accountId = entry.getValue();
                    ICrashCancelBet cancelBet = entry.getKey();
                    gameRoom.sendChanges(cancelBet);
                    gameRoom.executeOnAllMembers(gameRoom.createSendSeatsMessageTask(accountId, false, -1, cancelBet, true));
                }
                getLog().debug("performAutoEjectForSeat: completed for accountId={}", dirtySeat.getAccountId());
                sink.success();
            } catch (Exception e) {
                getLog().error("performAutoEjectForSeat: processing error, seat={}", dirtySeat, e);
                sink.error(e);
            }
        });
    }

    /**
     * Updates ICrashBetInfo of player after eject.
     * @param mult eject multiplier of player
     * @param crashBetInfo {@code ICrashBetInfo} of player
     * @param crashBetId crashBetId of player
     * @param seat seat of player
     * @param autoEject true if was auto-eject.
     * @return {@code ICrashCancelBet} message for players.
     */
    private ICrashCancelBet ejectBet(double mult, ICrashBetInfo crashBetInfo, String crashBetId, Seat seat, boolean autoEject) {
        Money currentBetWin = calculateWin(mult, crashBetInfo.getCrashBetAmount());
        seat.incrementRoundWin(currentBetWin);
        seat.getCurrentPlayerRoundInfo().addCrashBetInfo(seat.getAccountId(), crashBetInfo.getCrashBetAmount(), currentBetWin.toCents(), mult, crashBetInfo.getAutoPlayMultiplier());
        crashBetInfo.setEjected(true);
        crashBetInfo.setMultiplier(mult);
        crashBetInfo.setAutoPlay(autoEject);
        crashBetInfo.setEjectTime(System.currentTimeMillis());
        return getTOFactoryService().createCrashCancelBetResponse(
                System.currentTimeMillis(), -1, mult,
                getSeatNumber(seat), currentBetWin.toCents(), crashBetId, seat.getNickname());
    }

    protected Money calculateWin(double mult, long crashBetAmount) {
        double roundDownWin = BigDecimal.valueOf(mult * crashBetAmount).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        return Money.fromCents(1).multiply(roundDownWin);
    }

    /**
     * Processing a player who manually ejected from rocket
     * @param accountId accountId of player
     * @param crashBetId crashBetId
     * @param senderRequestId serverId
     * @param placeNewBet placeNewBet
     * @param inboundMessage message from client
     * @return
     */
    @Override
    public int processCancelCrashMultiplier(long accountId, String crashBetId, Integer senderRequestId, boolean placeNewBet,
                                            TInboundObject inboundMessage) {
        if (placeNewBet) {
            return ErrorCodes.BAD_STAKE;
        }
        Seat seatByAccountId = gameRoom.getSeatByAccountId(accountId);
        MaxCrashData maxCrashData = getMaxCrashData();
        if (maxCrashData == null) {
            getLog().error("processCancelCrashMultiplier: failed, maxCrashData is null");
            return ErrorCodes.BAD_STAKE;
        }
        double currentMult = maxCrashData.getCurrentMult();
        boolean locked = false;
        try {
            boolean endRoundSoon = (System.currentTimeMillis() - maxCrashData.getStartTime()) + 1500 > maxCrashData.getCrashTime();
            if (endRoundSoon) {
                locked = gameRoom.tryLock(1000, TimeUnit.MILLISECONDS);
                if (!locked) {
                    getLog().debug("processCancelCrashMultiplier, cannot obtain gameRoom lock");
                    return ErrorCodes.BET_NOT_FOUND;
                }
            }
        } catch (InterruptedException e) {
            getLog().error("processCancelCrashMultiplier, cannot obtain gameRoom lock, error: {}", e.getMessage());
            return ErrorCodes.BAD_STAKE;
        }
        try {
            if (getSharedGameState().isRoundResultProcessingStarted()) {
                return ErrorCodes.BET_NOT_FOUND;
            }
            ICrashBetInfo crashBetInfo = seatByAccountId.getCrashBet(crashBetId);
            if (crashBetInfo != null) {
                if (crashBetInfo.isEjected()) {
                    getLog().debug("processCancelCrashMultiplier: already ejected, just return CrashCancelBet, crashBetInfo={}", crashBetInfo);
                    double mult = crashBetInfo.getMultiplier();
                    Money currentBetWin = calculateWin(mult, crashBetInfo.getCrashBetAmount());
                    ICrashCancelBet seatResponse = getTOFactoryService().createCrashCancelBetResponse(
                            System.currentTimeMillis(), senderRequestId, mult,
                            getSeatNumber(seatByAccountId), currentBetWin.toCents(), crashBetId, seatByAccountId.getNickname());
                    gameRoom.sendChanges(null, seatResponse, accountId, inboundMessage);
                } else if (!gameRoom.isSendRealBetWin() && crashBetInfo.isReserved()) {
                    getLog().error("accountId={} Impossible situation. Check why reserved bets weren't sent reservedBet={}", accountId, crashBetInfo);
                    return ErrorCodes.BAD_STAKE;
                } else {
                    ICrashCancelBet allSeatsResponse = ejectBet(currentMult, crashBetInfo, crashBetId, seatByAccountId, false);
                    gameRoom.saveSeat(0, seatByAccountId);
                    if (senderRequestId == null) {
                        gameRoom.sendChanges(allSeatsResponse);
                    } else {
                        ICrashCancelBet seatResponse = getTOFactoryService().createCrashCancelBetResponse(
                                System.currentTimeMillis(), senderRequestId, allSeatsResponse.getCurrentMult(),
                                allSeatsResponse.getSeatId(), allSeatsResponse.getSeatWin(),
                                allSeatsResponse.getCrashBetId(), allSeatsResponse.getName());
                        gameRoom.sendChanges(allSeatsResponse, seatResponse, accountId, inboundMessage);
                    }
                    gameRoom.executeOnAllMembers(gameRoom.createSendSeatsMessageTask(accountId, true,
                            senderRequestId == null ? -1 : senderRequestId, allSeatsResponse, true));
                }
                return ErrorCodes.OK;
            }
            return ErrorCodes.BAD_STAKE;
        } finally {
            if (locked) {
                getLog().debug("processCancelCrashMultiplier gameRoom unlock");
                gameRoom.unlock();
            }
        }
    }

    protected void onTimerWithLock(boolean needClearEnemy) {
        getLog().debug("PlayGameState:: onTimer: current={}", this);
        getLog().debug("End round, aliveMummies: {} needWaitingWhenEnemiesLeave: {} remainingNumberOfBoss: {}",
                getMap().getItemsSize(), needWaitingWhenEnemiesLeave, 0
        );
        if (!needWaitingWhenEnemiesLeave) {
            needWaitingWhenEnemiesLeave = true;
            getMap().clearInactivityLiveItems();
            if (needClearEnemy) {
                getMap().removeAllEnemies();
            }
            gameRoom.sendChanges(getTOFactoryService().createRoundFinishSoon(System.currentTimeMillis()));
        }
    }

    @Override
    public void firePlaySubroundFinished(boolean endGame) throws CommonException {
        if (endGame) {
            getLog().debug("firePlaySubroundFinished: end game, move to QualifyGameState; pause={}", pauseTime);
            getLog().debug("firePlaySubroundFinished: stopTimer");
            gameRoom.stopUpdateTimer();
            setQualifyGameState();
        } else {
            getLog().error("firePlaySubroundFinished: firePlaySubroundFinished called with endGame=false");
        }
    }

    @Override
    public void nextSubRound() {
        doFinishWithLock();
    }

    @Override
    public void doFinishWithLock() {
        gameRoom.lock();
        try {
            getLog().debug("doFinishWithLock: timer: {}", gameRoom.getTimerTime());
            finish();
        } catch (CommonException e) {
            getLog().error("Unexpected error", e);
        } finally {
            gameRoom.unlock();
        }
    }

    /**
     * Finish of processing of all seats.
     * @param seats list of seats
     */
    @Override
    protected void finishSeats(List<Seat> seats) {
        long startTime = System.currentTimeMillis();
        boolean hasPendingOperations = gameRoom.getPlayerInfoService().hasPlayersWithPendingOperation(gameRoom.getId());
        getLog().debug("finishSeats start time: {}, hasPendingOperations={}", startTime, hasPendingOperations);
        for (Seat seat : seats) {
            gameRoom.lockSeat(seat.getAccountId());
            try {
                Seat actualSeat = gameRoom.getSeatByAccountId(seat.getAccountId());
                if (actualSeat == null) {
                    getLog().debug("finishSeats: actualSeat is null, seat={}", seat);
                } else if (actualSeat.getCrashBetsCount() == 0) {
                    getLog().debug("finishSeats: actualSeat has no bets, seat={}", seat);
                } else if (seat.getPlayerInfo().isPendingOperation()) {
                    getLog().debug("finishSeats: actualSeat has pending operation, seat={}", seat);
                } else {
                    SharedCrashGameState sharedGameState = getSharedGameState();
                    MaxCrashData maxCrashData = sharedGameState.getMaxCrashData();
                    getLog().debug("maxCrashData: actualSeat seatId={}, maxCrashData: {}", seat.getAccountId(), maxCrashData);
                    if (maxCrashData != null) {
                        PlayerRoundInfo currentPlayerRoundInfo = actualSeat.getCurrentPlayerRoundInfo();

                        if (currentPlayerRoundInfo.getSalt().isEmpty()) {
                            currentPlayerRoundInfo.setCrashMult(maxCrashData.getCrashMult());
                            currentPlayerRoundInfo.setTimeOfRoundStart(maxCrashData.getStartTime());
                            currentPlayerRoundInfo.setSalt(maxCrashData.getSalt());
                            currentPlayerRoundInfo.setPlayerRoundId(seat.getPlayerInfo().getExternalRoundId());
                            currentPlayerRoundInfo.setRoomRoundId(sharedGameState.getRoundId());
                        } else {
                            getLog().debug("Find currentPlayerRoundInfo with old data for accountId={}, currentPlayerRoundInfo={}, " +
                                            "seat.getPlayerInfo()={}, skip update currentPlayerRoundInfo, hasPendingOperations: {}",
                                    actualSeat.getAccountId(), currentPlayerRoundInfo, seat.getPlayerInfo(), hasPendingOperations);
                        }

                        if (!(hasPendingOperations && seat.getPlayerInfo().isPendingOperation())) {
                            actualSeat.getCrashBets().forEach((s, iCrashBetInfo) -> {
                                if (!iCrashBetInfo.isEjected()) {
                                    currentPlayerRoundInfo.addCrashBetInfo(actualSeat.getAccountId(),
                                            iCrashBetInfo.getCrashBetAmount(), 0, maxCrashData.getCrashMult(), iCrashBetInfo.getAutoPlayMultiplier());
                                }
                            });
                        } else {
                            getLog().debug("Find pending operation for accountId={}, currentPlayerRoundInfo={}, seat.getPlayerInfo()={}," +
                                    " skip update currentPlayerRoundInfo", actualSeat.getAccountId(), currentPlayerRoundInfo, seat.getPlayerInfo());
                        }
                    }
                    getLog().debug("finishSeats: clearCrashBets for seat.accountId={}, seat.crashBets={}, seat.getCurrentPlayerRoundInfo(): {}",
                            actualSeat.getAccountId(), actualSeat.getCrashBets(), seat.getCurrentPlayerRoundInfo());
                    actualSeat.clearCrashBets();
                    actualSeat.transferRoundWin();
                    gameRoom.saveSeat(0, actualSeat);
                }
            } finally {
                gameRoom.unlockSeat(seat.getAccountId());
            }
        }
        long endTime = System.currentTimeMillis();
        getLog().debug("finishSeats end time: {}", endTime);
        if (hasPendingOperations) {
            getLog().warn("finishSeats: method was working for {} mills", endTime - startTime);
        }
    }

    @Override
    public Map<Long, Integer> getFreezeTimeRemaining() {
        return getMap().getAllFreezeTimeRemaining(FREEZE_TIME_MAX);
    }

    @Override
    public boolean isBuyInAllowed(Seat seat) {
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayGameState [");
        sb.append(super.toString());
        sb.append(", maxCrashData=").append(getMaxCrashData());
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
    }

    @Override
    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        
    }

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}

