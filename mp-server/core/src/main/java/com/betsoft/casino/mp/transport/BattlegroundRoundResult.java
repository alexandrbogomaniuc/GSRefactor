package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IBattlegroundRoundResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BattlegroundRoundResult implements Serializable, IBattlegroundRoundResult {
    private long id;
    private long score;
    private long rank;
    private long pot;
    private String nickName;

    public BattlegroundRoundResult(long id, long score, long rank, long pot, String nickName) {
        this.id = id;
        this.score = score;
        this.rank = rank;
        this.pot = pot;
        this.nickName = nickName;
    }

    public static List<BattlegroundRoundResult> convert(List<IBattlegroundRoundResult> battlegroundRoundResults) {
        List<BattlegroundRoundResult> result = new ArrayList<>();
        if (battlegroundRoundResults == null) {
            return result;
        }
        for (IBattlegroundRoundResult battlegroundRoundResult : battlegroundRoundResults) {
            if (battlegroundRoundResult instanceof BattlegroundRoundResult) {
                result.add((BattlegroundRoundResult) battlegroundRoundResult);
            } else {
                result.add(new BattlegroundRoundResult(battlegroundRoundResult.getId(), battlegroundRoundResult.getScore(),
                        battlegroundRoundResult.getRank(), battlegroundRoundResult.getPot(), battlegroundRoundResult.getNickName()));
            }
        }
        return result;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public long getPot() {
        return pot;
    }

    public void setPot(long pot) {
        this.pot = pot;
    }

    @Override
    public String toString() {
        return "BattlegroundRoundResult{" +
                "id=" + id +
                ", score=" + score +
                ", rank=" + rank +
                ", pot=" + pot +
                ", nickName=" + nickName +
                '}';
    }
}
