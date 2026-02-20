package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IAward;
import com.betsoft.casino.mp.model.IAwards;
import com.betsoft.casino.utils.TObject;

import java.util.ArrayList;
import java.util.List;

public class Awards extends TObject implements IAwards {
    private long enemyId;
    private List<IAward> awards = new ArrayList<>();

    public Awards(long date, long enemyId) {
        super(date, SERVER_RID);
        this.enemyId = enemyId;
    }

    @Override
    public void addAward(IAward award) {
        this.awards.add(award);
    }

    public long getEnemyId() {
        return enemyId;
    }

    public List<IAward> getAwards() {
        return awards;
    }

    @Override
    public String toString() {
        return "Awards[" +
                "awards=" + awards +
                ", enemyId=" + enemyId +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
