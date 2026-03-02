package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IBattlegroundSeat;
import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.abs.casino.common.exception.CommonException;

import java.util.Collections;
import java.util.List;

import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("rawtypes,unchecked")
public abstract class AbstractBattlegroundPlayGameState<GAMEROOM extends AbstractBattlegroundGameRoom, SEAT extends IBattlegroundSeat, MAP extends IMap, GS extends IGameState>
        extends AbstractActionPlayGameState<GAMEROOM, SEAT, MAP, GS> {
    public AbstractBattlegroundPlayGameState() {}

    public AbstractBattlegroundPlayGameState(GAMEROOM gameRoom, AbstractQuestManager questManager) {
        super(gameRoom, questManager);
    }

    protected void initSeats() throws CommonException {
        List<SEAT> seats = gameRoom.getSeats();
        for (SEAT seat : seats) {
            if (seat != null) {
                if (gameRoom.isBattlegroundMode()) {
                    seat.setAmmoAmount(gameRoom.getRoomInfo().getBattlegroundAmmoAmount());
                }
            }
        }
        gameRoom.sendStartNewRoundToAllPlayers(seats);
        setPossibleEnemies();
        allowSpawn = true;
        allowSpawnHW = true;
        long roundDuration = gameRoom.getRoundDuration() * 1000L;
        startRoundTime = System.currentTimeMillis();
        endRoundTime = startRoundTime + roundDuration;

        gameRoom.sentBattlegroundMessageToPlayers(Collections.singletonList(-1), SERVER_RID);

        gameRoom.startUpdateTimer();
        gameRoom.setTimerTime(roundDuration);
        gameRoom.startTimer();
        getLog().debug("init PlayGameState completed");
    }

    protected void finishSeats(List<SEAT> seats) {
        for (SEAT seat : seats) {
            gameRoom.saveMinesWithLock(seat);
            seat.resetWeapons();
        }
    }

    @Override
    public boolean isBuyInAllowed(SEAT seat) {
        return false;
    }

    @Override
    public boolean isSitInAllowed() {
        return false;
    }
}
