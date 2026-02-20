package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.battleground.IBattleScoreInfo;
import java.util.ArrayList;
import java.util.List;

public class BattleScoreInfo implements IBattleScoreInfo {
    int seatId;
    long winAmount;
    long betAmount;
    boolean isKing;

    public BattleScoreInfo(int seatId, long winAmount, long betAmount, boolean isKing) {
        this.seatId = seatId;
        this.winAmount = winAmount;
        this.betAmount = betAmount;
        this.isKing = isKing;
    }


    public static List<BattleScoreInfo> convert(List<IBattleScoreInfo> scoreBySeatId) {
        List<BattleScoreInfo> result = new ArrayList<>();
        if (scoreBySeatId == null) {
            return result;
        }
        for (IBattleScoreInfo scoreBySeat : scoreBySeatId) {
            if (scoreBySeat instanceof BattleScoreInfo) {
                result.add((BattleScoreInfo) scoreBySeat);
            } else {
                result.add(new BattleScoreInfo(scoreBySeat.getSeatId(), scoreBySeat.getWinAmount(), scoreBySeat.getBetAmount(),scoreBySeat.isKing()));
            }
        }
        return result;
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    @Override
    public long getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    @Override
    public long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(long betAmount) {
        this.betAmount = betAmount;
    }

    @Override
    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }
}
