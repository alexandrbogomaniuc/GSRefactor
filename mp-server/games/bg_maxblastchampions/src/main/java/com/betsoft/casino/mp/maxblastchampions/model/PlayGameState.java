package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.common.AbstractPlayGameState;
import com.betsoft.casino.mp.common.MaxCrashAsteroid;
import com.betsoft.casino.mp.common.MaxCrashData;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyRange;
import com.betsoft.casino.mp.maxblastchampions.model.math.config.GameConfig;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.BattlegroundModeStatus;
import com.betsoft.casino.mp.model.battleground.IBgPlace;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.utils.TInboundObject;
import com.dgphoenix.casino.common.exception.CommonException;
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

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.betsoft.casino.mp.common.MaxCrashData.getNumberWithScale;

@SuppressWarnings({"Duplicates", "unchecked"})
public class PlayGameState extends AbstractPlayGameState<BattleAbstractCrashGameRoom, Seat, GameMap, PlayGameState> {
    private static final double Z = Math.pow(2, 52);
    private static final long SPEED_UP_AFTER_ALL_EJECT_DELAY = 2000;
    public static final int MIN_CRASH_MILLISECONDS = 28000;
    public static final int MAX_CRASH_MILLISECONDS = 32000;
    private transient double lastSentMultiplier = 0;
    private transient final long delayBeforeStart = 5284;
    private transient final long saveTimePeriodInMillis = 2000;
    private static final long BASE_FINAL_ASTEROID_OFFSET_BEFORE_CRASH_MS = 900;//offset before crash 0.9 sec

    public PlayGameState() {
        super();
    }

    public PlayGameState(BattleAbstractCrashGameRoom gameRoom) {
        super(gameRoom, null);
    }

    @Override
    public boolean isSitInAllowed() {
        return false;
    }

    @Override
    public void init() throws CommonException {
        long roomId = getRoomId();
        SharedCrashGameState crashGameState = getGameStateService().get(roomId, SharedCrashGameState.class);
        getLog().debug("PlayGameState.init: crashGameState={}", crashGameState);
        GameConfig gameConfig = getRoom().getGame().getConfig(getRoom().getId());
        if (crashGameState.getState() != RoomState.PLAY) {
            long startTime = getCurrentTime() + delayBeforeStart;
            getLog().debug("Max Crash PlayGameState init start");
            initMaxCrashData(crashGameState, gameConfig, startTime);
            crashGameState.setState(RoomState.PLAY);
            crashGameState.setRoundStartTime(startTime);
            crashGameState.setQualifyRoundResultFinished(false);
            getGameStateService().put(crashGameState);
            super.init();
        } else {
            //may null after dirty shutdown
            if (crashGameState.getMaxCrashData() == null) {
                long startTime = getCurrentTime() + delayBeforeStart;
                initMaxCrashData(crashGameState, gameConfig, startTime);
                getGameStateService().put(crashGameState);
            }
            roundWasFinished = false;
            gameRoom.clearSeatDataFromPreviousRound();
            pauseTime = 3000;
            needWaitingWhenEnemiesLeave = false;
            needForceFinishRound = false;
            initSeats();
        }
    }

    private void initMaxCrashData(SharedCrashGameState crashGameState, GameConfig gameConfig, long startTime) {

        MaxCrashData maxCrashData = new MaxCrashData();
        maxCrashData.setStartTime(startTime);
        maxCrashData.setSalt(RandomStringUtils.randomAscii(32));
        maxCrashData.setRoundId(getRoomInfo().getRoundId());
        maxCrashData.setOffsetStartTime(gameConfig.getInitialTime() > 0 ? gameConfig.getInitialTime() : 0L);
        maxCrashData.setFunction(gameConfig.getFunction());
        maxCrashData.setTimeSpeedMult(1.0);
        maxCrashData.setReachedMultiplierLimit(false);

        double initMult = this.getNowMult(maxCrashData, maxCrashData.getTimeSpeedMult());
        double crashMult = this.getRandomCrashMultiplier(gameConfig);

        maxCrashData.setCrashMult(crashMult);
        double crashTime = this.convertToCrashTime(crashMult);
        maxCrashData.setCrashTime(crashTime);

        //speed Coefficient vary From 0.5 to 1.0 when speed is increased
        double speedCoefficientFrom = 0.5;
        double speedCoefficientTill = 1.0;

        if(RNG.nextBoolean()) {// or speed Coefficient vary From 1.0 to 2.0 when speed is decreased
            speedCoefficientFrom = 1.0;
            speedCoefficientTill = 2.0;
        }

        double speedCoefficient = speedCoefficientFrom + (speedCoefficientTill - speedCoefficientFrom) * RNG.rand();
        speedCoefficient = BigDecimal.valueOf(speedCoefficient)
                .setScale(5, RoundingMode.HALF_UP).doubleValue();

        double adjustedFinalAsteroidOffsetBeforeCrashMs = BASE_FINAL_ASTEROID_OFFSET_BEFORE_CRASH_MS * speedCoefficient;
        adjustedFinalAsteroidOffsetBeforeCrashMs = BigDecimal.valueOf(adjustedFinalAsteroidOffsetBeforeCrashMs)
                .setScale(0, RoundingMode.HALF_UP).doubleValue();

        double crashTimeWithOffset = crashTime - adjustedFinalAsteroidOffsetBeforeCrashMs;
        double crashMultWithOffset = convertToCrashMult(maxCrashData.getFunction(), crashTimeWithOffset);

        getLog().debug("initMaxCrashData: speedCoefficientFrom={}, speedCoefficientTill={}, speedCoefficient={}, " +
                        "adjustedFinalAsteroidOffsetBeforeCrashMs={}, crashTime={}, crashTimeWithOffset={}, crashMultWithOffset={}",
                speedCoefficientFrom, speedCoefficientTill, speedCoefficient, adjustedFinalAsteroidOffsetBeforeCrashMs,
                crashTime, crashTimeWithOffset, crashMultWithOffset);

        maxCrashData.initAsteroidMults(crashMultWithOffset, adjustedFinalAsteroidOffsetBeforeCrashMs, speedCoefficient);

        double currentMult = maxCrashData.getOffsetStartTime() == 0 ? 1.0 : initMult;
        currentMult = Math.min(crashMult, currentMult);
        maxCrashData.setCurrentMult(currentMult);

        maxCrashData.setToken(generateToken(maxCrashData));
        getLog().debug("initMaxCrashData: crashMult={}, maxCrashData={} ", crashMult, maxCrashData);

        lastSentMultiplier = maxCrashData.getCurrentMult();
        crashGameState.setMaxCrashData(maxCrashData);
    }

    private double convertToCrashTime(double crashMult) {
        try {
            return calc("Math.log(t) * 1000 / 0.06012", crashMult);
        } catch (Exception e) {
            getLog().error("Unable calculate crashTime, error: {}", e.getMessage());
            return -1;
        }
    }

    private double convertToCrashMult(String function, double crashTime) {
        try {
            return calc(function, crashTime);
        } catch (Exception e) {
            getLog().error("Unable calculate crashMult, error: {}", e.getMessage());
            return -1;
        }
    }

    private double getRandomCrashMultiplier(GameConfig gameConfig) {

        long X = (long) (Z * RNG.rand());
        double resultMult;
        boolean usualCalculation = RNG.rand() < gameConfig.getAlpha();

        if (usualCalculation) {

            resultMult = gameConfig.getCrashMultiplier() >= 1 ?
                    gameConfig.getCrashMultiplier() :
                    Math.floor((100.0 * Z - X) / (Z - X)) / 100.0;

            //ensure minimum 2.01x
            resultMult += (resultMult >= 2 ? 0.01 : 2.01);

        } else {

            int R = RNG.nextInt(gameConfig.getA(), gameConfig.getB());
            resultMult = Math.floor(2 + (double) R / (1 - X / Z));

        }

        double gameConfigMcm;
        try {

            double crashTimeMs = RNG.nextInt(MIN_CRASH_MILLISECONDS, MAX_CRASH_MILLISECONDS);
            gameConfigMcm = this.calc(gameConfig.getFunction(), crashTimeMs);

        } catch (ScriptException | NumberFormatException | NullPointerException e) {
            getLog().error("Unexpected incorrect function for gameConfigMcm: {}, set gameConfigMcm to default: {}", e.getMessage(), gameConfig.getMcm());
            gameConfigMcm = gameConfig.getMcm();
        }

        getLog().debug("usualCalculation: {}, X: {}, temp resultMult: {}, gameConfigMcm: {}, ", usualCalculation, X, resultMult, gameConfigMcm);

        double minimalCrashMultiplier = this.getMinimalCrashMultiplier(gameConfig, resultMult);

        //ensure minimalCrashMultiplier is at least 2.01x
        minimalCrashMultiplier = Math.max(2.01, minimalCrashMultiplier);
        double numberWithScale = getNumberWithScale(gameConfigMcm, 2, RoundingMode.HALF_UP);

        return Math.max(2.01, Math.min(minimalCrashMultiplier, numberWithScale));
    }

    private double getMinimalCrashMultiplier(GameConfig gameConfig, double calculatedCrashMult) {
        BigDecimal bd;
        try {
            double nextCrashTimeFromConfig = calc(gameConfig.getFunction(), saveTimePeriodInMillis);
            if (nextCrashTimeFromConfig < 1) {
                getLog().error("invalid minimalCrashMultiplier {}, function: {}, time: {}",
                        nextCrashTimeFromConfig, gameConfig.getFunction(), saveTimePeriodInMillis);
            }
            bd = BigDecimal.valueOf(nextCrashTimeFromConfig);
        } catch (ScriptException | NumberFormatException | NullPointerException e) {
            getLog().error("getMinimalCrashMultiplier: Unexpected incorrect function: {}, time: {}",
                    getMaxCrashData().getFunction(), saveTimePeriodInMillis);
            bd = BigDecimal.valueOf(0);
        }
        return Math.max(bd.setScale(2, RoundingMode.HALF_UP).doubleValue(), getNumberWithScale(calculatedCrashMult, 2, RoundingMode.HALF_UP));
    }

    private double getSettingsMaxMultiplier(ICrashGameSetting crashGameSetting) {
        return BigDecimal.valueOf(crashGameSetting.getMaxMultiplier()).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public long getStartRoundTime() {
        return getSharedGameState().getRoundStartTime();
    }

    public MaxCrashData getMaxCrashData() {
        return getSharedGameState().getMaxCrashData();
    }

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

    private void saveHistory(double mult, MaxCrashData data, Map<String, Double> winners, double kilometerMult) {
        CrashRoundInfo crashRoundInfo = new CrashRoundInfo(mult, startRoundTime, data.getRoundId(), countBets(), data.getSalt(), data.getToken(), winners, kilometerMult);
        getMap().addCrashHistory(crashRoundInfo);
        gameRoom.executeOnAllMembers(gameRoom.createUpdateCrashHistoryTask(crashRoundInfo));
    }

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
        IGameState waitingPlayersGameState = gameRoom.getWaitingPlayersGameState();
        gameRoom.setGameState(waitingPlayersGameState);
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
    protected void updateWithLock() {
        SharedCrashGameState sharedGameState = getSharedGameState();
        MaxCrashData maxCrashData = sharedGameState.getMaxCrashData();

        //need double-check for optimization
        if (maxCrashData == null) {
            getLog().error("updateWithLock: maxCrashData is null, may end round started, sharedGameState={}", sharedGameState);
            return;
        }

        if (maxCrashData.getCrashMult() == 0) {
            return;
        }

        double currentMult = maxCrashData.getCurrentMult();

        getLog().debug("updateWithLock: lastSentMultiplier={}, currentMult={}, isNeedCrashInstantly={}",
                lastSentMultiplier, currentMult, maxCrashData.isNeedCrashInstantly());

        if (lastSentMultiplier < currentMult && !maxCrashData.isNeedCrashInstantly()) {

            getLog().debug("updateWithLock: multiplier already increased on other server, just send message and exit. " +
                    "lastSendedMultiplier={}, currentMult={}", lastSentMultiplier, currentMult);

            if (lastSentMultiplier < 1.0) {
                //need always send start multiplier
                lastSentMultiplier = 1.0;
                sendCrashStateInfo(
                        lastSentMultiplier,
                        maxCrashData.getCrashMult(),
                        maxCrashData.getTimeSpeedMult(),
                        maxCrashData.getLastEjectTime(),
                        maxCrashData.getAsteroidMults());

            } else {
                sendCrashStateInfo(
                        currentMult,
                        maxCrashData.getCrashMult(),
                        maxCrashData.getTimeSpeedMult(),
                        maxCrashData.getLastEjectTime(),
                        maxCrashData.getAsteroidMults());
                lastSentMultiplier = currentMult;
            }

            return;
        }

        gameRoom.lock();
        boolean needSent;

        try {

            if (lastSentMultiplier < currentMult) {

                getLog().debug("updateWithLock: multiplier already increased on other server, set needSent=true. " +
                        "lastSendedMultiplier={}, currentMult={}", lastSentMultiplier, currentMult);
                needSent = true;

            } else {

                if (maxCrashData.getLastEjectTime() == 0 && isAllBetsEjected()) {
                    maxCrashData.setLastEjectTime(System.currentTimeMillis());
                }

                double timeSpeedMult = calcExtraMultiplierAfterAllBetsEjected(maxCrashData);
                double newPossibleMult = this.getNowMult(maxCrashData, timeSpeedMult);
                needSent = newPossibleMult > currentMult;
                double crashMult = maxCrashData.getCrashMult();

                getLog().debug("updateWithLock: timeSpeedMult={}, newPossibleMult={}, crashMult={}, currentMult={}, needSent={}",
                        timeSpeedMult, newPossibleMult, crashMult, currentMult, needSent);

                if (newPossibleMult <= crashMult) {

                    maxCrashData.setCurrentMult(newPossibleMult);
                    maxCrashData.setTimeSpeedMult(timeSpeedMult);
                    getGameStateService().put(sharedGameState);

                } else {

                    sharedGameState.setRoundResultProcessingStarted(true);
                    getGameStateService().put(sharedGameState);

                    maxCrashData.setCurrentMult(crashMult);
                    maxCrashData.setTimeSpeedMult(timeSpeedMult);
                    maxCrashData.setCrashMult(crashMult);

                    getLog().debug("updateWithLock: sharedGameState={}", sharedGameState);

                    if(!sharedGameState.isCalculationFinished()) {

                        double kilometerMult = sharedGameState.getKilometerMult();
                        Map<String, Double> finishWinners = calculateFinishRoundData(crashMult, kilometerMult);
                        saveHistory(crashMult, maxCrashData, finishWinners, kilometerMult);

                        getLog().debug("updateWithLock: need finish: finishWinners={}, crashMult={}, kilometerMult={}, maxCrashData={}",
                                finishWinners, crashMult, kilometerMult, maxCrashData);

                        buildBGRoundInfo(maxCrashData, finishWinners);

                        sharedGameState.setCalculationFinished(true);

                        double crashDataCurrentMult = maxCrashData.getCurrentMult();
                        double crashDataTimeSpeedMult = maxCrashData.getTimeSpeedMult();
                        long lastEjectTime = maxCrashData.getLastEjectTime();
                        Map<Double, MaxCrashAsteroid> asteroidMults = maxCrashData.getAsteroidMults();

                        Executors.newSingleThreadExecutor().execute(() -> {

                            getLog().debug("updateWithLock: SingleThreadExecutor executes sendCrashStateInfo for " +
                                            "crashDataCurrentMult={}, crashMult={}, crashDataTimeSpeedMult={}, lastEjectTime={}, asteroidMults={}",
                                    crashDataCurrentMult, crashMult, crashDataTimeSpeedMult, lastEjectTime, asteroidMults);

                            ICrashStateInfo crashStateInfo = sendCrashStateInfo(
                                    crashDataCurrentMult,
                                    crashMult,
                                    crashDataTimeSpeedMult,
                                    lastEjectTime,
                                    asteroidMults);

                            if(crashStateInfo != null) {

                                getLog().debug("updateWithLock: executeOnAllMembers task to send  rin=-1, " +
                                        "crashStateInfo={}", crashStateInfo);

                                gameRoom.executeOnAllMembers(
                                        gameRoom.createSendSeatsMessageTask(
                                                null,
                                                false,
                                                -1,
                                                crashStateInfo,
                                                true
                                        )
                                );
                            }
                        });

                    }

                    getLog().debug("updateWithLock: to send sendCrashStateInfo for " +
                                    " currentMultiplier={}, crashMult={}, timeSpeedMult={}, allEjectedTime={}, asteroidMults={}",
                            maxCrashData.getCurrentMult(), maxCrashData.getCrashMult(), maxCrashData.getTimeSpeedMult(),
                            maxCrashData.getLastEjectTime(), maxCrashData.getAsteroidMults());

                    sendCrashStateInfo(
                            maxCrashData.getCurrentMult(),
                            maxCrashData.getCrashMult(),
                            maxCrashData.getTimeSpeedMult(),
                            maxCrashData.getLastEjectTime(),
                            maxCrashData.getAsteroidMults());

                    getGameStateService().put(sharedGameState);

                    needSent = false;

                    nextSubRound();
                }
            }
        } finally {
            gameRoom.unlock();
        }

        getLog().debug("updateWithLock: needSent={} currentMultiplier={}, crashMult={}, timeSpeedMult={}, " +
                        "allEjectedTime={}, asteroidMults={}", needSent, maxCrashData.getCurrentMult(),
                maxCrashData.getCrashMult(), maxCrashData.getTimeSpeedMult(), maxCrashData.getLastEjectTime(), maxCrashData.getAsteroidMults());

        if (needSent) {
            sendCrashStateInfo(
                    maxCrashData.getCurrentMult(),
                    maxCrashData.getCrashMult(),
                    maxCrashData.getTimeSpeedMult(),
                    maxCrashData.getLastEjectTime(),
                    maxCrashData.getAsteroidMults());
        }

        if (maxCrashData != null) {
            getLog().debug("updateWithLock: lastSentMultiplier={} changed to {}", lastSentMultiplier, maxCrashData.getCurrentMult());
            lastSentMultiplier = maxCrashData.getCurrentMult();
        }
    }

    private ICrashStateInfo sendCrashStateInfo(double currentMultiplier, double crashMult, double timeSpeedMult,
                                               long allEjectedTime, Map<Double, MaxCrashAsteroid> asteroidMults) {
        if (currentMultiplier > 1.0) {

            ICrashStateInfo crashStateInfo = createCrashStateInfo(
                    System.currentTimeMillis(),
                    currentMultiplier,
                    crashMult,
                    timeSpeedMult,
                    allEjectedTime,
                    asteroidMults);

            gameRoom.sendChanges(crashStateInfo);
            return crashStateInfo;

        } else {
            getLog().warn("sendCrashStateInfo: skip sending currentMultiplier={}", currentMultiplier);
        }
        return null;
    }

    private ICrashStateInfo createCrashStateInfo(long date, double currentMult, double crashMult, double timeSpeedMult, long allEjectedTime, Map<Double, MaxCrashAsteroid> maxCrashAsteroids) {

        ICrashStateInfo crashStateInfo = getTOFactoryService()
                .createCrashStateInfo(date, currentMult, timeSpeedMult);

        if(maxCrashAsteroids != null && !maxCrashAsteroids.isEmpty()) {

            for(Double asteroidMult : maxCrashAsteroids.keySet()) {
                if(lastSentMultiplier < asteroidMult && asteroidMult <= currentMult) {

                    MaxCrashAsteroid  maxCrashAsteroid = maxCrashAsteroids.get(asteroidMult);

                    if(maxCrashAsteroid != null) {
                        int type = maxCrashAsteroid.getType();
                        Double speedCoefficient = maxCrashAsteroid.getSpeedCoefficient();
                        Double xSpawnPercent = maxCrashAsteroid.getXPercent();
                        Double ySpawnPercent = maxCrashAsteroid.getYPercent();
                        Double slowDistancePercent = maxCrashAsteroid.getSlowDistancePercent();

                        ITransportAsteroid asteroid = getTOFactoryService()
                                .createAsteroid(type, speedCoefficient, xSpawnPercent, ySpawnPercent, slowDistancePercent);

                        crashStateInfo.setAsteroid(asteroid);
                        break;
                    }
                }
            }
        }

        if (currentMult >= crashMult) {
            crashStateInfo.setCrash(true);
        }

        if (allEjectedTime > 0) {
            crashStateInfo.setAllEjectedTime(allEjectedTime);
        }

        return crashStateInfo;
    }

    private void buildBGRoundInfo(MaxCrashData maxCrashData, Map<String, Double> finishWinners) {
        try {
            String winnerName = buildWinnerName(finishWinners);
            List<IBgPlace> bgPlaces = new ArrayList<>();
            for (Seat dirtySeat : gameRoom.getSeats()) {
                boolean seatLocked;
                try {
                    seatLocked = gameRoom.tryLockSeat(dirtySeat.getAccountId(), 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    getLog().error("buildBGRoundInfo: cannot lockSeat, interrupted, just exit, reason={}", e.getMessage());
                    continue;
                }
                if (!seatLocked) {
                    getLog().error("buildBGRoundInfo: cannot lockSeat, timeout, seat.accountId={}", dirtySeat.getAccountId());
                    continue;
                }
                try {
                    Seat seat = gameRoom.getSeatByAccountId(dirtySeat.getAccountId());
                    if (seat != null && !seat.getCrashBets().isEmpty())  {
                        IBattlegroundRoomPlayerInfo playerInfo = (IBattlegroundRoomPlayerInfo) seat.getPlayerInfo();
                        bgPlaces.add(playerInfo.createBattlegroundRoundInfo(seat.getCurrentPlayerRoundInfo().getTotalBets().toCents(), seat.getCurrentPlayerRoundInfo().getTotalPayouts().toCents(), 0, 0,
                                BattlegroundModeStatus.COMPLETED.name(), 0, winnerName, dirtySeat.getAccountId(), 1, playerInfo.getGameSessionId(), seat.getTotalScore().getLongAmount(),
                                getRoomInfo().getRoundId(), maxCrashData.getStartTime(), calculateEjectPoint(seat.getCrashBets().values().iterator().next()), getRoomInfo().getPrivateRoomId()));
                        gameRoom.saveSeat(0, seat);
                    }
                } finally {
                    gameRoom.unlockSeat(dirtySeat.getAccountId());
                }
            }
            updateBgPlaces(bgPlaces, finishWinners.isEmpty());
        } catch (Exception e) {
            getLog().warn("buildBGRoundInfo error", e);
        }
    }

    private String buildWinnerName(Map<String, Double> finishWinners) {
        if (finishWinners.isEmpty()) {
            return "";
        }
        StringJoiner stringJoiner = new StringJoiner(",");
        double max = Collections.max(finishWinners.values());
        finishWinners.forEach((name, ejectPoint) -> {
            if (max == ejectPoint) {
                stringJoiner.add(name);
            }
        });
        return stringJoiner.toString();
    }

    private double calculateEjectPoint(ICrashBetInfo bet) {
        if (bet.isEjected()) {
            double crashTime = convertToCrashTime(bet.getMultiplier()) / 1000;
            return getNumberWithScale(crashTime, 1, RoundingMode.DOWN);
        }
        return 0.0;
    }

    private void updateBgPlaces(List<IBgPlace> places, boolean refunded) {
        if (places.isEmpty()) {
            getLog().warn("Something went wrong, places is empty, roundId:{}", gameRoom.getRoomInfo().getRoundId());
            return;
        }
        places.sort(Comparator.comparingLong(IBgPlace::getWin)
                .reversed());
        long win = places.get(0).getWin();

        int rateIndex = 1;
        for (IBgPlace place : places) {
            if (refunded) {
                place.setRank(1);
            } else {
             place.setRank(win == place.getWin() ? 1 : ++rateIndex);
            }
        }

        for (IBgPlace place : places) {
            boolean seatLocked;
            try {
                seatLocked = gameRoom.tryLockSeat(place.getAccountId(), 1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                getLog().error("updateBgPlaces: cannot lockSeat, interrupted, just exit, reason={}", e.getMessage());
                continue;
            }
            if (!seatLocked) {
                getLog().error("updateBgPlaces: cannot lockSeat, timeout, place.getAccountId={}", place.getAccountId());
                continue;
            }
            try {
                Seat seat = gameRoom.getSeatByAccountId(place.getAccountId());
                if (seat != null && !seat.getCrashBets().isEmpty())  {
                    IBattlegroundRoomPlayerInfo playerInfo = (IBattlegroundRoomPlayerInfo) seat.getPlayerInfo();
                    playerInfo.getBattlegroundRoundInfo().setPlaces(places);
                    gameRoom.saveSeat(0, seat);
                }
            } finally {
                gameRoom.unlockSeat(place.getAccountId());
            }
        }
    }

    private boolean isAllBetsEjected() {
        List<Seat> seats = gameRoom.getSeats();
        for (Seat seat : seats) {
            if (seat != null) {
                Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();
                long notEjectedCount = crashBets.values().stream()
                        .filter(bet -> !bet.isEjected())
                        .count();
                if (notEjectedCount > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private double getNowMult(MaxCrashData maxCrashData, double timeSpeedMult) {

        long now = System.currentTimeMillis();
        long startTime = maxCrashData.getStartTime();
        long offsetStartTime = maxCrashData.getOffsetStartTime();
        long lastEjectTime = maxCrashData.getLastEjectTime();
        long lastEjectTimeWithOffset = lastEjectTime + SPEED_UP_AFTER_ALL_EJECT_DELAY;

        double diffTime = timeSpeedMult > 1 ?
                (lastEjectTimeWithOffset - startTime) + ((now - lastEjectTimeWithOffset) * timeSpeedMult) :
                (now + offsetStartTime - startTime);

        double nextCrashMult;

        getLog().debug("getNowMult: timeSpeedMult={}, now={}, startTime={}, offsetStartTime={}, " +
                        "lastEjectTime={}, lastEjectTimeWithOffset={}",
                timeSpeedMult, now, startTime, offsetStartTime, lastEjectTime, lastEjectTimeWithOffset);

        try {

            nextCrashMult = calc(maxCrashData.getFunction(), diffTime);

            if (nextCrashMult < 1) {
                getLog().error("getNowMult: invalid nextCrashTime {}, function: {}, time: {}",
                        nextCrashMult, maxCrashData.getFunction(), diffTime);
            }

        } catch (ScriptException | NumberFormatException | NullPointerException e) {
            getLog().error("getNowMult: Unexpected incorrect function='{}', was replaced to='{}', time={}",
                    getMaxCrashData().getFunction(), "Math.exp((diffTime * 0.0.06012) / 1000)", diffTime);
            nextCrashMult = Math.exp((diffTime * 0.06012) / 1000);
        }

        return getNumberWithScale(nextCrashMult, 2, RoundingMode.HALF_UP);
    }

    private double calcExtraMultiplierAfterAllBetsEjected(MaxCrashData maxCrashData) {

        long now = System.currentTimeMillis();
        double lastEjectTime = maxCrashData.getLastEjectTime();
        double timeSpeedMult = 1;

        try {

            if(lastEjectTime != 0 && now - SPEED_UP_AFTER_ALL_EJECT_DELAY > lastEjectTime) {
                timeSpeedMult = calc("(Math.exp((t * 0.347) / 1000)) - 1", now - lastEjectTime);
            }

            if (timeSpeedMult < 1) {
                timeSpeedMult = 1;
            }

        } catch (ScriptException | NumberFormatException | NullPointerException e) {
            getLog().error("calcExtraMultiplierAfterAllBetsEjected: Unexpected incorrect function for " +
                    "timeSpeedMult: {}", e.getMessage());
        }

        return Math.min(timeSpeedMult, 4.0);
    }

    private double calc(String func, double time) throws ScriptException {
        ScriptEngine scriptEngine = getRoom().getScriptEngine();
        scriptEngine.put("t", time);
        return (double) scriptEngine.eval(func);
    }

    private Map<String, Double> calculateFinishRoundData(double crashMult, double kilometerMult) {
        //value is accountId
        Map<String, Double> winnersForHistory = new HashMap<>();
        Set<Long> seatsWithMaxMult = new HashSet<>();
        double maxMult = 0;
        long totalPot = 0;
        double defaultRakePercent = 5.0;
        double rakePercent = 0;

        boolean wasAnyEjected = false;
        try {
            for (Seat dirtySeat : gameRoom.getAllSeats()) {
                boolean seatLocked;

                try {
                    seatLocked = gameRoom.tryLockSeat(dirtySeat.getAccountId(), 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    getLog().error("calculateFinishRoundData: cannot lockSeat, interrupted, just exit, reason={}", e.getMessage());
                    continue;
                }

                if (!seatLocked) {
                    getLog().error("calculateFinishRoundData: cannot lockSeat, timeout, seat.accountId={}", dirtySeat.getAccountId());
                    continue;
                }

                try {
                    Seat seat = gameRoom.getSeatByAccountId(dirtySeat.getAccountId());
                    if (seat == null) {
                        getLog().error("calculateFinishRoundData: seat not found, dirtySeat={}", dirtySeat);
                    } else {

                        IBattlegroundRoomPlayerInfo playerInfo = (IBattlegroundRoomPlayerInfo) seat.getPlayerInfo();
                        if(rakePercent == 0 && playerInfo.getBattlegroundRake() > 0) {
                            rakePercent = playerInfo.getBattlegroundRake();
                        }

                        for (Map.Entry<String, ICrashBetInfo> betInfo : seat.getCrashBets().entrySet()) {

                            totalPot += gameRoom.getRoomInfo().getStake().toCents();

                            ICrashBetInfo crashBetInfo = betInfo.getValue();
                            if (crashBetInfo.isEjected()) {

                                wasAnyEjected = true;
                                double multiplier = crashBetInfo.getMultiplier();

                                if (maxMult == 0 && multiplier > 0) {

                                    maxMult = multiplier;
                                    seatsWithMaxMult.add(seat.getAccountId());

                                } else if (maxMult > 0 && multiplier == maxMult) {

                                    seatsWithMaxMult.add(seat.getAccountId());

                                } else if (multiplier > maxMult) {

                                    maxMult = multiplier;
                                    seatsWithMaxMult.clear();
                                    seatsWithMaxMult.add(seat.getAccountId());
                                }
                            }
                        }
                    }
                } finally {
                    gameRoom.unlockSeat(dirtySeat.getAccountId());
                }
            }

        } catch (Exception e) {
            getLog().warn("calculateFinishRoundData: exception", e);
        }

        if(rakePercent == 0){
            rakePercent = defaultRakePercent;
        }

        if (!wasAnyEjected) {
            rakePercent = 0; //do not take any rake value when none player ejected.
            processRefundForAllSeats(crashMult, rakePercent, totalPot, kilometerMult);
        } else {
            long totalPotRake = getBattlegroundWin(totalPot, rakePercent);
            long totalPotForSeat = totalPotRake / seatsWithMaxMult.size();
            getLog().debug("calculateFinishRoundData: totalPotRake: {}. totalPotForSeat: {}, totalPot: {}, " +
                            "seatsWithMaxMult: {}, rakePercent: {}",
                    totalPotRake, totalPotForSeat, totalPot, seatsWithMaxMult, rakePercent);

            updateWinsForSeat(seatsWithMaxMult, totalPotForSeat, totalPotRake, crashMult, winnersForHistory, rakePercent, totalPot, kilometerMult);
        }

        return winnersForHistory;
    }

    private void updateWinsForSeat(Set<Long> seatWinnersAccountIds, Long totalPotForSeat, long totalPotRake,
                                   double crashMult, Map<String, Double> winnersForHistory, double rake, long totalPotWithoutRake, double kilometerMult) {
        try {
            for (Seat dirtySeat : gameRoom.getAllSeats()) {
                boolean seatLocked;
                try {
                    seatLocked = gameRoom.tryLockSeat(dirtySeat.getAccountId(), 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    getLog().error("updateWinsForSeat: cannot lockSeat, interrupted, just exit, reason={}", e.getMessage());
                    continue;
                }
                if (!seatLocked) {
                    getLog().error("updateWinsForSeat: cannot lockSeat, timeout, seat.accountId={}", dirtySeat.getAccountId());
                    continue;
                }
                try {
                    Seat seat = gameRoom.getSeatByAccountId(dirtySeat.getAccountId());
                    if (seat == null) {
                        getLog().error("updateWinsForSeat: seat not found, dirtySeat={}", dirtySeat);
                    } else if(!seat.getCrashBets().isEmpty()) {
                        boolean isWinner = seatWinnersAccountIds.contains(seat.getAccountId());
                        PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
                        Money totalPayouts = isWinner ? Money.fromCents(totalPotForSeat) : Money.ZERO;
                        seat.incrementRoundWin(totalPayouts);
                        currentPlayerRoundInfo.setTotalPayouts(totalPayouts);
                        currentPlayerRoundInfo.setTotalPot(totalPotRake);
                        currentPlayerRoundInfo.setCrashMult(crashMult);
                        currentPlayerRoundInfo.setTotalPotWithoutRake(totalPotWithoutRake);
                        currentPlayerRoundInfo.setRake(rake);
                        currentPlayerRoundInfo.setKilometerMult(kilometerMult);
                        currentPlayerRoundInfo.updateStatOnEndRound(0, seat.getCurrentScore(), 0);
                        Optional<ICrashBetInfo> crashBetInfo = seat.getCrashBets().values().stream().findFirst();
                        crashBetInfo.ifPresent(iCrashBetInfo -> winnersForHistory.put(seat.getNickname(), iCrashBetInfo.getMultiplier()));
                        gameRoom.saveSeat(0, seat);
                    }
                } finally {
                    gameRoom.unlockSeat(dirtySeat.getAccountId());
                }
            }

        } catch (Exception e) {
            getLog().warn("updateWinsForSeat: exception", e);
        }
    }

    private void processRefundForAllSeats(double crashMult, double rakePercent, long totalPotWithoutRake, double kilometerMult) {
        getLog().debug("processRefundForAllSeats");
        try {
            for (Seat dirtySeat : gameRoom.getAllSeats()) {
                boolean seatLocked;
                try {
                    seatLocked = gameRoom.tryLockSeat(dirtySeat.getAccountId(), 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    getLog().error("processRefundForAllSeats: cannot lockSeat, interrupted, just exit, reason={}", e.getMessage());
                    continue;
                }
                if (!seatLocked) {
                    getLog().error("processRefundForAllSeats: cannot lockSeat, timeout, seat.accountId={}", dirtySeat.getAccountId());
                    continue;
                }
                try {
                    Seat seat = gameRoom.getSeatByAccountId(dirtySeat.getAccountId());
                    if (seat == null) {
                        getLog().error("processRefundForAllSeats: seat not found, dirtySeat={}", dirtySeat);
                    } else if(!seat.getCrashBets().isEmpty()) {

                        PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();

                        long totalBetsInCents = currentPlayerRoundInfo.getTotalBets().toCents();
                        long refundWinInCents = getBattlegroundWin(totalBetsInCents, rakePercent);
                        Money refundWin = Money.fromCents(refundWinInCents);

                        seat.incrementRoundWin(refundWin);

                        currentPlayerRoundInfo.setTotalPayouts(refundWin);
                        currentPlayerRoundInfo.setRefundAmount(refundWinInCents);
                        currentPlayerRoundInfo.setCrashMult(crashMult);
                        currentPlayerRoundInfo.setTotalPotWithoutRake(totalPotWithoutRake);
                        currentPlayerRoundInfo.setRake(rakePercent);
                        currentPlayerRoundInfo.setKilometerMult(kilometerMult);
                        currentPlayerRoundInfo.updateStatOnEndRound(0, seat.getCurrentScore(), 0);

                        gameRoom.saveSeat(0, seat);
                    }
                } finally {
                    gameRoom.unlockSeat(dirtySeat.getAccountId());
                }
            }

        } catch (Exception e) {
            getLog().warn("processRefundForAllSeats: exception", e);
        }
    }

    private long getBattlegroundWin(long winBySeatsCount, double rakePercentage) {
        long buyIn = (long) getRoomInfo().getStake().toDoubleCents();
        getLog().debug("getBattlegroundWin: buyIn={}, rakePercentage={}, winBySeatsCount={}",
                buyIn, rakePercentage, winBySeatsCount);
        if (winBySeatsCount <= 0) {
            return 0;
        }
        BigDecimal winDecimal = BigDecimal.valueOf(winBySeatsCount);
        BigDecimal rake = BigDecimal.valueOf(rakePercentage).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128).
                multiply(winDecimal, MathContext.DECIMAL128);
        BigDecimal winWithoutRake = winDecimal.subtract(rake, MathContext.DECIMAL128);
        long resultWin = winWithoutRake.longValue();
        if (resultWin <= 0) {
            return winBySeatsCount == 1 ? 1 : winBySeatsCount - 1; //get minimal rake, one cent
        }
        return resultWin;
    }

    private ICrashCancelBet ejectBet(double mult, ICrashBetInfo crashBetInfo, String crashBetId, Seat seat, boolean autoEject) {

        crashBetInfo.setEjected(true);
        crashBetInfo.setMultiplier(mult);
        crashBetInfo.setAutoPlay(autoEject);
        crashBetInfo.setEjectTime(System.currentTimeMillis());

        ICrashCancelBet crashCancelBet = getTOFactoryService().createCrashCancelBetResponse(
                System.currentTimeMillis(),
                -1,
                mult,
                getSeatNumber(seat),
                Money.ZERO.toCents(),
                crashBetId,
                seat.getNickname());

        return crashCancelBet;
    }

    @Override
    public int processCancelCrashMultiplier(long accountId, String crashBetId, Integer senderRequestId, boolean placeNewBet,
                                            TInboundObject inboundMessage) {
        if (placeNewBet) {
            return ErrorCodes.BAD_STAKE;
        }
        Seat seat = gameRoom.getSeatByAccountId(accountId);
        MaxCrashData maxCrashData = getMaxCrashData();
        if (maxCrashData == null) {
            getLog().error("processCancelCrashMultiplier: failed, maxCrashData is null");
            return ErrorCodes.BAD_STAKE;
        }

        double currentMult = maxCrashData.getCurrentMult();
        boolean locked = false;

        try {

            long timePassedFromStart = System.currentTimeMillis() - maxCrashData.getStartTime();
            boolean endRoundSoon = timePassedFromStart > maxCrashData.getCrashTime() - 1500;

            if (endRoundSoon) {
                locked = gameRoom.tryLock(300, TimeUnit.MILLISECONDS);
                if (!locked) {
                    getLog().debug("processCancelCrashMultiplier: cannot obtain gameRoom lock");
                    return ErrorCodes.BET_NOT_FOUND;
                }
            }

        } catch (InterruptedException e) {
            getLog().error("processCancelCrashMultiplier: cannot obtain gameRoom lock, error: {}", e.getMessage());
            return ErrorCodes.BAD_STAKE;
        }

        try {
            if (getSharedGameState().isRoundResultProcessingStarted()) {
                return ErrorCodes.BET_NOT_FOUND;
            }

            ICrashBetInfo crashBetInfo = seat.getCrashBet(crashBetId);

            if (crashBetInfo != null) {

                if (crashBetInfo.isEjected()) {

                    getLog().debug("processCancelCrashMultiplier: already ejected, just return CrashCancelBet, crashBetInfo={}", crashBetInfo);
                    double mult = crashBetInfo.getMultiplier();

                    ICrashCancelBet seatResponse = getTOFactoryService().createCrashCancelBetResponse(
                            System.currentTimeMillis(),
                            senderRequestId,
                            mult,
                            getSeatNumber(seat),
                            0,
                            crashBetId,
                            seat.getNickname());

                    gameRoom.sendChanges(null, seatResponse, accountId, inboundMessage);

                } else {

                    ICrashCancelBet allSeatsResponse = ejectBet(
                            currentMult,
                            crashBetInfo,
                            crashBetId,
                            seat,
                            false);

                    gameRoom.saveSeat(0, seat);

                    if (senderRequestId == null) {

                        gameRoom.sendChanges(allSeatsResponse);

                    } else {

                        ICrashCancelBet seatResponse = getTOFactoryService().createCrashCancelBetResponse(
                                System.currentTimeMillis(),
                                senderRequestId,
                                allSeatsResponse.getCurrentMult(),
                                allSeatsResponse.getSeatId(),
                                allSeatsResponse.getSeatWin(),
                                allSeatsResponse.getCrashBetId(),
                                allSeatsResponse.getName());

                        gameRoom.sendChanges(allSeatsResponse, seatResponse, accountId, inboundMessage);
                    }

                    Runnable sendSeatsMessageTask = gameRoom.createSendSeatsMessageTask(
                            accountId,
                            true,
                            senderRequestId == null ? -1 : senderRequestId,
                            allSeatsResponse,
                            true);

                    gameRoom.executeOnAllMembers(sendSeatsMessageTask);
                }
                return ErrorCodes.OK;
            }

            return ErrorCodes.BAD_STAKE;

        } catch (Exception e) {
            getLog().error("processCancelCrashMultiplier: error: {}", e.getMessage());
            return ErrorCodes.BAD_STAKE;
        } finally {
            if (locked) {
                getLog().debug("processCancelCrashMultiplier: gameRoom unlock 2");
                gameRoom.unlock();
            }
        }
    }

    protected void onTimerWithLock(boolean needClearEnemy) {
        getLog().debug("onTimerWithLock:: onTimer: current={}", this);
        getLog().debug("onTimerWithLock: End round, aliveMummies: {} needWaitingWhenEnemiesLeave: {} remainingNumberOfBoss: {}",
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
            getLog().error("doFinishWithLock: Unexpected error", e);
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    protected void finishSeats(List<Seat> seats) {
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
                        currentPlayerRoundInfo.setCrashMult(maxCrashData.getCrashMult());
                        currentPlayerRoundInfo.setTimeOfRoundStart(maxCrashData.getStartTime());
                        currentPlayerRoundInfo.setSalt(maxCrashData.getSalt());
                        currentPlayerRoundInfo.setPlayerRoundId(seat.getPlayerInfo().getExternalRoundId());
                        currentPlayerRoundInfo.setRoomRoundId(sharedGameState.getRoundId());
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

