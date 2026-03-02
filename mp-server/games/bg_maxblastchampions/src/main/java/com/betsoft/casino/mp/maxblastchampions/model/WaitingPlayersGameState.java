package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.common.AbstractMultiNodeWaitingPlayerGameState;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WaitingPlayersGameState extends AbstractMultiNodeWaitingPlayerGameState<BattleAbstractCrashGameRoom, Seat, GameMap, WaitingPlayersGameState> {
    public static final long START_NEW_ROUND_PAUSE = 15000;

    public WaitingPlayersGameState() {
        super();
    }

    public WaitingPlayersGameState(BattleAbstractCrashGameRoom gameRoom) {
        super(gameRoom);
    }

    @Override
    public void init() throws CommonException {

        long roomId = getRoomId();
        gameRoom.lock();
        long oldRoundId = getRoomInfo().getRoundId();

        try {
            getRoom().reloadRoomInfo();

            ISharedGameStateService sharedGameStateService = getGameStateService();

            SharedCrashGameState sharedCrashGameState = sharedGameStateService.get(roomId, SharedCrashGameState.class);

            if (sharedCrashGameState == null) {

                IRoomInfo roomInfoUpdated = getRoomInfo();

                sharedCrashGameState =
                        new SharedCrashGameState(
                                roomInfoUpdated.getState(),
                                roomId, roomInfoUpdated.getRoundId(),
                                0,
                                0,
                                null);

                sharedCrashGameState.setCalculationFinished(false);
                sharedCrashGameState.setRoundResultProcessingStarted(false);

                sharedGameStateService.put(sharedCrashGameState);
                getLog().debug("WaitingPlayersGameState (maxblastchampions) init: sharedCrashGameState={}", sharedCrashGameState);
            }

            if (sharedCrashGameState.getState() != RoomState.WAIT) {

                RoomState roomState = getRoomState();// it is RoomState.WAIT
                sharedCrashGameState.setState(roomState);

                long roundStartTime = System.currentTimeMillis() + getStartNewRoundPause();
                sharedCrashGameState.setRoundStartTime(roundStartTime);

                sharedCrashGameState.setMaxCrashData(null);
                sharedCrashGameState.setCalculationFinished(false);
                double kilometerMult = getRandomKMMultiplier();
                sharedCrashGameState.setKilometerMult(kilometerMult);
                sharedCrashGameState.setRoundResultProcessingStarted(false);
                sharedCrashGameState.setNeedStartNewRound(false);

                sharedGameStateService.put(sharedCrashGameState);

                super.init();

                sharedCrashGameState = sharedGameStateService.get(roomId, SharedCrashGameState.class);
                sharedCrashGameState.setRoundId(getRoomInfo().getRoundId());

                sharedGameStateService.put(sharedCrashGameState);
                getLog().debug("WaitingPlayersGameState (maxblastchampions) init: sharedCrashGameState={}", sharedCrashGameState);

            } else {

                innerInit();

            }

            getLog().debug("WaitingPlayersGameState (maxblastchampions) init: oldRoundId={}, newRoundId={}", oldRoundId, getRoomInfo().getRoundId());

        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public void processSitOut(Seat seat) throws CommonException {

        getLog().debug("WaitingPlayersGameState (maxblastchampions) processSitOut: seat={}", seat);

        if (seat != null) {
            seat.setWantSitOut(true);
        }
        if (seat != null && !gameRoom.needToSitOutByNoActivity(seat)) {
            getLog().debug("WaitingPlayersGameState (maxblastchampions) processSitOut: call firePlayersCountChanged for seat={}", seat);
            firePlayersCountChanged();
        }
    }

    private double getRandomKMMultiplier() {
        return (double) RNG.nextInt(80, 110) / 100;
    }

    @Override
    public void firePlayersCountChanged() throws CommonException {
        boolean locked = false;

        try {
            locked = gameRoom.tryLock(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            getLog().warn("firePlayersCountChanged: (maxblastchampions): cannot lock: {}", e.getMessage());
        }

        if (!locked) {
            getLog().error("firePlayersCountChanged: (maxblastchampions): cannot obtain room lock, just exit");
            return;
        }

        getLog().debug("firePlayersCountChanged: (maxblastchampions): Room State={}", gameRoom.getState());

        try {

            if (gameRoom.getState().equals(RoomState.WAIT)) {

                boolean needStartNewRound = isNeedStartNewRound();
                short seatsCount = getSeatsCountRequiredForStart();

                getLog().debug("firePlayersCountChanged: (maxblastchampions): seatsCount={}, minSeats={}, needStartNewRound={}",
                        seatsCount, getRoom().getMinSeats(), needStartNewRound);

                if (seatsCount >= getRoom().getMinSeats()) {

                    if (!getRoom().isTimerStopped()) {
                        getLog().debug("firePlayersCountChanged: (maxblastchampions): stopTimer");
                        getRoom().stopTimer();
                    }

                    if (!needStartNewRound) {
                        setNeedStartNewRound(true);
                    }

                    boolean isPrivateRoom = isPrivateRoom();

                    if(!isPrivateRoom) {
                        //set timer for new round on players count change
                        //for non-private room only
                        getLog().debug("firePlayersCountChanged: (maxblastchampions): roomId={}, room is not Private, " +
                                "startTimer, call setTimerStartNewRoundTime()", getRoomId());
                        setTimerStartNewRoundTime();
                    }
                }
            } else {
                getLog().debug("firePlayersCountChanged: (maxblastchampions): game room is in PLAY state, not need any action");
            }
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public void onTimer(boolean needClearEnemies) throws CommonException {
        gameRoom.lock();

        try {

            short realSeatsCount = gameRoom.getRealSeatsCount();
            List<Long> realSeatsAccountIds = gameRoom.getRealSeatsAccountId();
            boolean needStartNewRound = this.isNeedStartNewRound();

            //remove after debug
            getLog().debug("Crash BTG onTimer: realSeatsAccountIds={}, realSeatsCount={}, seatsCount={}, " +
                            "needStartNewRound={}", realSeatsAccountIds, realSeatsCount, getRoom().getSeatsCount(),
                    needStartNewRound);

            if (realSeatsCount >= gameRoom.getMinSeats() && needStartNewRound) {

                setPlayGameState();

            } else {
                getRoom().stopTimer();
                setTimerStartNewRoundTime();
            }
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    protected short getSeatsCountRequiredForStart() {
        return getRoom().getSeatsCount();
    }

    @Override
    protected long getStartNewRoundPause() {
        return START_NEW_ROUND_PAUSE;
    }

    @Override
    protected void setTimerStartNewRoundTime() throws CommonException {

        long startNewRoundPause = getStartNewRoundPause();
        setTimerStartNewRoundTime(startNewRoundPause);
    }

    public void setTimerStartNewRoundTime(long startNewRoundPause) throws CommonException {

        long roomId = getRoomId();
        IGameStateChanged gameStateChanged = null;
        short seatsCount = 0;
        Map<Long, Pair<ITransportObject, ITransportObject>> seatMessages = new HashMap<>();

        getLog().debug("setTimerStartNewRoundTime (maxblastchampions): roomId={}, startNewRoundPause={}",
                roomId, startNewRoundPause);

        try {

            if (!getRoom().isTimerStopped()) {
                //need always stopTime before setTimerTime(), also note that stopTimer() called from setPlayGameState()->gameRoom.setGameState
                getRoom().stopTimer();
            }

            SharedCrashGameState sharedCrashGameState =
                    getGameStateService().get(roomId, SharedCrashGameState.class);

            long roundStartTime = sharedCrashGameState.getRoundStartTime();
            long currentTime = System.currentTimeMillis();

            getLog().debug("setTimerStartNewRoundTime (maxblastchampions): roomId={}, state={}, roundStartTime={}, " +
                    "roundStart-currentTime={}", roomId, sharedCrashGameState.getState(), roundStartTime, roundStartTime - currentTime);

            if (roundStartTime > currentTime) {

                getRoom().setTimerTime(roundStartTime - currentTime);
                getLog().debug("setTimerStartNewRoundTime (maxblastchampions): roomId={}, timerTime={}",
                        roomId, roundStartTime - currentTime);

            } else {

                if (sharedCrashGameState.getState() == RoomState.PLAY) {

                    setNeedStartNewRound(true);
                    setPlayGameState();

                } else if (sharedCrashGameState.getState() != RoomState.WAIT) {

                    getRoom().setTimerTime(1);

                } else { //sharedCrashGameState.getState() == RoomState.WAIT && roundStartTime <= currentTime

                    boolean isRoomOnInit = (roundStartTime == 0); // room has just started and it is initialized;
                    boolean isPrivateRoom = isPrivateRoom();

                    seatMessages = this.processSitOutAndCancelCrashMultiplierAndGetSeatMessages(isRoomOnInit);

                    currentTime = System.currentTimeMillis();
                    getRoom().setTimerTime(startNewRoundPause);

                    roundStartTime = currentTime + startNewRoundPause;
                    sharedCrashGameState.setRoundStartTime(roundStartTime);
                    sharedCrashGameState.setCalculationFinished(false);
                    sharedCrashGameState.setRoundResultProcessingStarted(false);

                    getGameStateService().put(sharedCrashGameState);

                    if(isRoomOnInit && isPrivateRoom) {
                        gameStateChanged = null;

                        getLog().debug("setTimerStartNewRoundTime (maxblastchampions): roomId={} is on Start " +
                                        "Init and is Private, skip to send gameStateChanged message with RoomState.WAIT " +
                                        "to existing players", getRoomId());
                    } else {
                        gameStateChanged = gameRoom.getTOFactoryService().createGameStateChanged(
                                currentTime,
                                RoomState.WAIT,
                                0,
                                sharedCrashGameState.getRoundId(),
                                sharedCrashGameState.getRoundStartTime()
                        );

                        getLog().debug("setTimerStartNewRoundTime (maxblastchampions): roomId={} prepare gameStateChanged" +
                                "to send it to existing players, gameStateChanged={}", getRoomId(), gameStateChanged);
                    }

                    seatsCount = gameRoom.getSeatsCount();
                }
            }
        } finally {
            //timer may be started in PlayGameState.init()
            if (getRoom().isTimerStopped()) {
                getRoom().startTimer();
                getLog().debug("setTimerStartNewRoundTime (maxblastchampions):  roomId={}, timer started", roomId);
            } else {
                getLog().warn("setTimerStartNewRoundTime (maxblastchampions):  roomId={}, timer already started", roomId);
            }
        }

        this.submitMessages(seatMessages, gameStateChanged, seatsCount);

    }

    private void submitMessages(Map<Long, Pair<ITransportObject, ITransportObject>> seatMessages, IGameStateChanged gameStateChanged, short seatsCount) {
        if (seatMessages != null && !seatMessages.isEmpty()) {

            for (Map.Entry<Long, Pair<ITransportObject, ITransportObject>> entry : seatMessages.entrySet()) {

                Long accountId = entry.getKey();
                Pair<ITransportObject, ITransportObject> pair = entry.getValue();

                ITransportObject seatResponse = pair.getKey();
                ITransportObject allSeatsResponse = pair.getValue();

                gameRoom.sendChanges(allSeatsResponse, seatResponse, accountId, null);

                gameRoom.executeOnAllMembers(gameRoom.createSendSeatMessageTask(accountId, seatResponse));

                gameRoom.executeOnAllMembers(gameRoom.createSendSeatsMessageTask(accountId, true, -1,
                        allSeatsResponse, true));
            }
        }

        if (gameStateChanged != null && seatsCount > 0) {

            getLog().debug("setTimerStartNewRoundTime (maxblastchampions): gameStateChanged={}, seatsCount: {}",
                    gameStateChanged, seatsCount);

            gameRoom.sendChanges(gameStateChanged);
            gameRoom.executeOnAllMembers(gameRoom.createSendSeatsMessageTask(null, false,
                    -1, gameStateChanged, true));
        }
    }

    @Override
    protected void setPlayGameState() throws CommonException {
        gameRoom.setGameState(new PlayGameState(gameRoom));
    }

    @Override
    public boolean isBuyInAllowed(Seat seat) {
        return true;
    }

    @Override
    public long getTimeToNextState() {
        return gameRoom.isTimerStopped() ?
                getStartNewRoundPause() :
                gameRoom.getTimerRemaining();
    }

    @Override
    public int processCancelCrashMultiplier(long accountId, String crashBetId, Integer senderRequestId, boolean placeNewBet,
                                            TInboundObject inboundMessage) {

        Seat seat = gameRoom.getSeatByAccountId(accountId);
        ICrashBetInfo crashBetInfo = seat.getCrashBet(crashBetId);

        if (crashBetInfo != null) {

            Money refund = Money.fromCents(crashBetInfo.getCrashBetAmount());
            getLog().debug("processCancelCrashMultiplier: crashBetInfo: {}, refund: {}", crashBetInfo, refund.toCents());

            ITransportObject allSeatsResponse = gameRoom.getTOFactoryService().createCrashCancelBetResponse(
                    System.currentTimeMillis(), -1, 0, getSeatNumber(seat),
                    refund.toCents(), crashBetId, seat.getNickname());

            seat.cancelCrashBet(crashBetId);
            gameRoom.saveSeat(0, seat);

            int relatedRequestId = senderRequestId == null ? -1 : senderRequestId;

            ITransportObject seatResponse = gameRoom.getTOFactoryService().createCrashCancelBetResponse(
                    System.currentTimeMillis(), relatedRequestId, 0, getSeatNumber(seat),
                    refund.toCents(), crashBetId, seat.getNickname());

            gameRoom.sendChanges(allSeatsResponse, seatResponse, accountId, inboundMessage);

            Runnable sendSeatMessageTask = gameRoom.createSendSeatMessageTask(accountId, seatResponse);
            gameRoom.executeOnAllMembers(sendSeatMessageTask);

            Runnable sendSeatsMessageTask = gameRoom.createSendSeatsMessageTask(accountId, true,
                    relatedRequestId, allSeatsResponse, true);
            gameRoom.executeOnAllMembers(sendSeatsMessageTask);

            return ErrorCodes.OK;
        } else {
            return ErrorCodes.BAD_STAKE;
        }
    }

    private void sitOutByNoActivity(Seat seat) throws CommonException {
        getLog().debug("sitOutByNoActivity: roomId={}, seat.isWantSitOut()={}, seat={}",
                getRoomId(), seat.isWantSitOut(), seat);

        gameRoom.processSitOut(
                seat.getSocketClient(),
                null,
                getSeatNumber(seat),
                seat.getAccountId(),
                false
        );
    }

    private boolean isPrivateRoom() {
        return gameRoom.getRoomInfo() != null ? gameRoom.getRoomInfo().isPrivateRoom() : false;
    }

    private Map<Long, Pair<ITransportObject, ITransportObject>> processSitOutAndCancelCrashMultiplierAndGetSeatMessages(boolean isRoomOnInit) throws CommonException {

        Map<Long, Pair<ITransportObject, ITransportObject>> messagesForSeats = new HashMap<>();
        List<Seat> seats = gameRoom.getSeats();

        boolean isPrivateRoom = isPrivateRoom();

        getLog().debug("processCancelCrashMultiplierAndGetSeatMessages: roomId={}, isRoomOnInit={}, isPrivateRoom={}",
                getRoomId(), isRoomOnInit, isPrivateRoom);

        for (Seat seat : seats) {

            getLog().debug("processCancelCrashMultiplierAndGetSeatMessages: roomId={}, seat={}", getRoomId(), seat);

            if (seat != null) {

                if (gameRoom.needToSitOutByNoActivity(seat)) {
                    sitOutByNoActivity(seat);
                    continue;
                }

                if(isRoomOnInit && isPrivateRoom) {
                    getLog().debug("processCancelCrashMultiplierAndGetSeatMessages: roomId={} is on Start Init and is Private" +
                                    ", skip processCancelCrashMultiplierAndGetMessage for accountId={}",
                            getRoomId(), seat.getAccountId());
                } else {

                    Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();

                    getLog().debug("processCancelCrashMultiplierAndGetSeatMessages: roomId={}, accountId={} has crashBets={} ",
                            getRoomId(), seat.getAccountId(), crashBets);

                    for (String crashBetId : crashBets.keySet()) {

                        Pair<ITransportObject, ITransportObject> pair =
                                processCancelCrashMultiplierAndGetMessage(seat.getAccountId(), crashBetId);

                        if (pair != null) {
                            messagesForSeats.put(seat.getAccountId(), pair);
                        }
                    }

                    if (crashBets.size() > 0) {
                        this.updatePlayersStatus(seat.getAccountId(), seat.getNickname(), Status.WAITING);
                    }
                }
            }
        }

        return messagesForSeats;
    }

    public Pair<ITransportObject, ITransportObject> processCancelCrashMultiplierAndGetMessage(long accountId, String crashBetId) {

        getLog().debug("processCancelCrashMultiplierAndGetMessage: roomId={}, accountId: {}, crashBetId: {}",
                getRoomId(), accountId, crashBetId);

        Seat seat = gameRoom.getSeatByAccountId(accountId);
        if (seat == null) {
            getLog().error("processCancelCrashMultiplierAndGetMessage:  roomId={}, seat is null for accountId: {}," +
                            " crashBetId: {}", getRoomId(), accountId, crashBetId);
            return null;
        }

        ICrashBetInfo crashBetInfo = seat.getCrashBet(crashBetId);

        if (crashBetInfo == null) {
            getLog().error("processCancelCrashMultiplierAndGetMessage: roomId={}, crashBetInfo is null for accountId: {}, " +
                    "crashBetId: {}", getRoomId(), accountId, crashBetId);
            return null;
        }

        long refund = Money.fromCents(crashBetInfo.getCrashBetAmount()).toCents();
        getLog().debug("processCancelCrashMultiplierAndGetMessage: roomId={}, crashBetInfo: {}, refund: {} for accountId: {}",
                getRoomId(), crashBetInfo, refund, accountId);

        ITransportObject allSeatsResponse = gameRoom.getTOFactoryService().createCrashCancelBetResponse(
                System.currentTimeMillis(), -1, 0, getSeatNumber(seat),
                refund, crashBetId, seat.getNickname());

        seat.cancelCrashBet(crashBetId);

        getLog().debug("processCancelCrashMultiplierAndGetMessage: roomId={}, accountId: {}, allSeatsResponse: {}",
                getRoomId(), accountId, allSeatsResponse);

        gameRoom.saveSeat(0, seat);

        ITransportObject seatResponse = gameRoom.getTOFactoryService().createCrashCancelBetResponse(
                System.currentTimeMillis(), -1, 0, getSeatNumber(seat),
                refund, crashBetId, seat.getNickname());

        getLog().debug("processCancelCrashMultiplierAndGetMessage: roomId={}, accountId: {}, seatResponse: {}",
                getRoomId(), accountId, seatResponse);

        return new Pair<>(seatResponse, allSeatsResponse);

    }

    protected void updatePlayersStatus(long accountId, String nickname, Status status) {

        getLog().debug("updatePlayersStatus: accountId={}, nickname={}, status={}", accountId, nickname, status);

        boolean isPrivateRoom = isPrivateRoom();

        if(isPrivateRoom) {
            try {
                gameRoom.updatePlayersStatusNicknamesOnly(Arrays.asList(nickname),
                        status, false, true);
            } catch (Exception e) {
                getLog().error("updatePlayersStatus: Exception to updatePlayersStatusNicknamesOnly, " +
                        "{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
    }

    @Override
    public void read(Kryo kryo, Input input) {
    }

    @Override
    protected WaitingPlayersGameState getDeserializer() {
        return this;
    }
}
