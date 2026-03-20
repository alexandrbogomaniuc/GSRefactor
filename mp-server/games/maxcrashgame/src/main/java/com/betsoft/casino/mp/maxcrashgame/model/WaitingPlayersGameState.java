package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractMultiNodeWaitingPlayerGameState;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.concurrent.TimeUnit;

/**
 * The state of the room is RoomState.WAIT. During this state, players can place new bets for the round.
 */
public class WaitingPlayersGameState extends AbstractMultiNodeWaitingPlayerGameState<AbstractCrashGameRoom, Seat, GameMap, WaitingPlayersGameState> {
    public static final long START_NEW_ROUND_PAUSE_FIFTEEN = 15000;
    public static final long START_NEW_ROUND_PAUSE_TEN = 10000;
    public static final long START_NEW_ROUND_PAUSE_LUNAR_CASH = 8000;
    public static final int IS_NOT_ALLOWED_CANCEL_BET_PERIOD = 200;

    public WaitingPlayersGameState() {
        super();
    }

    public WaitingPlayersGameState(AbstractCrashGameRoom gameRoom) {
        super(gameRoom);
    }

    /**
     * Inits wait game state. Changes state in shared game state and allow make new bets.
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void init() throws CommonException {
        long roomId = getRoomId();
        gameRoom.lock();
        long oldRoundId = getRoomInfo().getRoundId();
        try {
            getRoom().reloadRoomInfo();
            SharedCrashGameState crashGameState = getGameStateService().get(roomId, SharedCrashGameState.class);
            if (crashGameState == null) {
                IRoomInfo roomInfo = getRoomInfo();
                crashGameState = new SharedCrashGameState(roomInfo.getState(), roomId, roomInfo.getRoundId(), 0, 0, null);
                crashGameState.setCalculationFinished(false);
                crashGameState.setRoundResultProcessingStarted(false);
                getGameStateService().put(crashGameState);
            }
            if (crashGameState.getState() != RoomState.WAIT) {
                crashGameState.setState(getRoomState());
                crashGameState.setRoundStartTime(System.currentTimeMillis() + getStartNewRoundPause());
                crashGameState.setMaxCrashData(null);
                crashGameState.setCalculationFinished(false);
                crashGameState.setRoundResultProcessingStarted(false);
                crashGameState.setNeedStartNewRound(false);
                getGameStateService().put(crashGameState);
                super.init();
                crashGameState = getGameStateService().get(roomId, SharedCrashGameState.class);
                crashGameState.setRoundId(getRoomInfo().getRoundId());
            } else {
                innerInit();
            }
            getLog().debug("init: oldRoundId={}, newRoundId={}", oldRoundId, getRoomInfo().getRoundId());
            getLog().debug("init: crashGameState={}", crashGameState);
            getGameStateService().put(crashGameState);
        } finally {
            gameRoom.unlock();
        }
    }

    /**
     * Called when player make sitIn, sitOut, or reBuy.
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void firePlayersCountChanged() throws CommonException {
        boolean locked = false;
        try {
            locked = gameRoom.tryLock(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            getLog().warn("firePlayersCountChanged: (maxcrashgame): cannot lock: {}", e.getMessage());
        }
        if (!locked) {
            getLog().error("firePlayersCountChanged: (maxcrashgame): cannot obtain room lock, just exit");
            return;
        }

        getLog().debug("firePlayersCountChanged: (maxcrashgame): Room State={}", gameRoom.getState());

        try {
            if (gameRoom.getState().equals(RoomState.WAIT)) {
                short seatsCount = getSeatsCountRequiredForStart();
                getLog().debug("firePlayersCountChanged: (maxcrashgame): seatsCount={}, minSeats={}, needStartNewRound={}",
                        seatsCount, getRoom().getMinSeats(), isNeedStartNewRound());
                if (seatsCount >= getRoom().getMinSeats()) {
                    if (!getRoom().isTimerStopped()) {
                        getLog().debug("firePlayersCountChanged: (maxcrashgame): stopTimer");
                        getRoom().stopTimer();
                    }
                    setNeedStartNewRound(true);
                    setTimerStartNewRoundTime();
                    getLog().debug("firePlayersCountChanged: (maxcrashgame): startTimer");
                }
            } else {
                getLog().debug("firePlayersCountChanged: (maxcrashgame): game room is in PLAY state, not need any action");
            }
        } finally {
            gameRoom.unlock();
        }
    }

    /**
     * Called when the timer fires. Starts a round or restarts the timer.
     * @param needClearEnemies true if you need clear enemies on map. (for action games)
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void onTimer(boolean needClearEnemies) throws CommonException {
        short seatsCount = getSeatsCountRequiredForStart();
        boolean needStartNewRound = isNeedStartNewRound();
        getLog().debug("onTimer: seatsCount={}, needStartNewRound={}", seatsCount, needStartNewRound);
        if (seatsCount >= getRoom().getMinSeats() && needStartNewRound) {
            if (!gameRoom.isSendRealBetWin()) {
                setBuyInState();
            } else {
                if (gameRoom.getPlayerInfoService().hasPlayersWithPendingOperation(gameRoom.getId())) {
                    getLog().error("onTimer: has player pending transaction");
                }
                setPlayGameState();
            }
        } else {
            getRoom().stopTimer();
            setTimerStartNewRoundTime();
        }
    }

    @Override
    protected short getSeatsCountRequiredForStart() {
        return getRoom().getSeatsCount();
    }

    /**
     * Launch timer for next checks
     * @throws CommonException  if any unexpected error occur
     */
    @Override
    protected void setTimerStartNewRoundTime() throws CommonException {
        long roomId = getRoomId();
        gameRoom.lock();
        try {
            if (!getRoom().isTimerStopped()) {
                //need always stopTime before setTimerTime(), also note that stopTimer() called from setPlayGameState()->gameRoom.setGameState
                getRoom().stopTimer();
            }
            SharedCrashGameState crashGameState = getGameStateService().get(roomId, SharedCrashGameState.class);
            long roundStartTime = crashGameState.getRoundStartTime();
            long currentTime = System.currentTimeMillis();
            if (roundStartTime > currentTime) {
                getRoom().setTimerTime(roundStartTime - currentTime);
                getLog().debug("setTimerStartNewRoundTime: timerTime={}", roundStartTime - currentTime);
            } else {
                crashGameState = getGameStateService().get(roomId, SharedCrashGameState.class);
                roundStartTime = crashGameState.getRoundStartTime();
                currentTime = System.currentTimeMillis();
                getLog().debug("setTimerStartNewRoundTime: state={}, roundStartTime={}, roundStart-currentTime={}", crashGameState.getState(),
                        roundStartTime, roundStartTime - currentTime);
                if (crashGameState.getState() == RoomState.PLAY) {
                    setNeedStartNewRound(true);
                    setPlayGameState();
                } else if (crashGameState.getState() == RoomState.BUY_IN) {
                    setBuyInState();
                } else if (crashGameState.getState() != RoomState.WAIT) {
                    getRoom().setTimerTime(1);
                } else if (roundStartTime > currentTime) {
                    getRoom().setTimerTime(roundStartTime - currentTime);
                } else {
                    getRoom().setTimerTime(getStartNewRoundPause());
                    crashGameState.setRoundStartTime(System.currentTimeMillis() + getStartNewRoundPause());
                    crashGameState.setCalculationFinished(false);
                    crashGameState.setRoundResultProcessingStarted(false);
                    getGameStateService().put(crashGameState);
                }
            }
        } finally {
            gameRoom.unlock();
            //timer may be started in PlayGameState.init() or BuyInState.init()
            if (getRoom().isTimerStopped()) {
                getRoom().startTimer();
                getLog().debug("WaitingPlayersGameState: setTimerStartNewRoundTime timer started");
            } else {
                getLog().warn("WaitingPlayersGameState: setTimerStartNewRoundTime timer already started");
            }
        }
    }

    @Override
    protected void setPlayGameState() throws CommonException {
        if (!gameRoom.isSendRealBetWin()) {
            gameRoom.clearReservedBetsAllSeats();
        }
        gameRoom.setGameState(new PlayGameState(gameRoom));
    }

    protected void setBuyInState() throws CommonException {
        gameRoom.setGameState(new BuyInState(gameRoom));
    }

    @Override
    protected long getStartNewRoundPause() {
        boolean isLunarCrash = GameType.LUNARCASH.equals(gameRoom.getGameType());
        boolean isSendRealBetWin = gameRoom.isSendRealBetWin();
        if (isLunarCrash) {
            return START_NEW_ROUND_PAUSE_LUNAR_CASH;
        }
        return isSendRealBetWin ? START_NEW_ROUND_PAUSE_FIFTEEN : START_NEW_ROUND_PAUSE_TEN;
    }

    @Override
    public boolean isBuyInAllowed(Seat seat) {
        long remainTime = getTimeToNextState();
        return remainTime > IS_NOT_ALLOWED_CANCEL_BET_PERIOD;
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
        long remainTime = getTimeToNextState();
        if (remainTime < IS_NOT_ALLOWED_CANCEL_BET_PERIOD) {
            getLog().warn("WaitingPlayersGameState.processCancelCrashMultiplier isn't allowed cancel, remain time={}", remainTime);
            return ErrorCodes.CANCEL_BET_NOT_ALLOWED;
        }
        Seat seatByAccountId = gameRoom.getSeatByAccountId(accountId);
        ICrashBetInfo crashBetInfo = seatByAccountId.getCrashBet(crashBetId);
        if (crashBetInfo != null) {
            Money refund = Money.fromCents(crashBetInfo.getCrashBetAmount());
            ITransportObject allSeatsResponse = gameRoom.getTOFactoryService().createCrashCancelBetResponse(
                    System.currentTimeMillis(), -1, 0, getSeatNumber(seatByAccountId),
                    refund.toCents(), crashBetId, seatByAccountId.getNickname());
            seatByAccountId.cancelCrashBet(crashBetId);
            gameRoom.saveSeat(0, seatByAccountId);
            if (senderRequestId == null) {
                gameRoom.sendChanges(allSeatsResponse);
            } else {
                ITransportObject seatResponse = gameRoom.getTOFactoryService().createCrashCancelBetResponse(
                        System.currentTimeMillis(), senderRequestId, 0, getSeatNumber(seatByAccountId),
                        refund.toCents(), crashBetId, seatByAccountId.getNickname());
                gameRoom.sendChanges(allSeatsResponse, seatResponse, accountId, inboundMessage);
            }
            gameRoom.executeOnAllMembers(gameRoom.createSendSeatsMessageTask(accountId, true,
                    senderRequestId == null ? -1 : senderRequestId, allSeatsResponse, true));
            return ErrorCodes.OK;
        } else {
            return ErrorCodes.BAD_STAKE;
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
