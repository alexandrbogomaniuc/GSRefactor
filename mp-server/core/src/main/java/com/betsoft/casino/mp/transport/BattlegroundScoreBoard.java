package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.battleground.IBattleScoreInfo;
import com.betsoft.casino.mp.model.battleground.IBattlegroundScoreBoard;
import com.betsoft.casino.utils.TInboundObject;

import java.util.List;
import java.util.Map;

public class BattlegroundScoreBoard extends TInboundObject implements IBattlegroundScoreBoard {
    long endTime;
    List<BattleScoreInfo> scoreBySeatId;
    Map<Integer, Long> scoreBossBySeatId;
    long startTime;

    public BattlegroundScoreBoard(long date, int rid, long startTime, long endTime, List<IBattleScoreInfo> scoreBySeatId,
                                  Map<Integer, Long> scoreBossBySeatId) {
        super(date, rid);
        this.endTime = endTime;
        this.scoreBySeatId = BattleScoreInfo.convert(scoreBySeatId);
        this.scoreBossBySeatId = scoreBossBySeatId;
        this.startTime = startTime;
    }

   public long getEndTime() {
        return endTime;
    }

    public List<BattleScoreInfo> getScoreBySeatId() {
        return scoreBySeatId;
    }

    public Map<Integer, Long> getScoreBossBySeatId() {
        return scoreBossBySeatId;
    }

    public long getStartTime() {
        return startTime;
    }
}
