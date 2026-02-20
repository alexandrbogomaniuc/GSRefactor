package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.TObject;

import java.util.List;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class EnemiesMoved extends TObject implements IServerMessage {
    private static final byte VERSION = 0;

    private List<EnemyMove> enemyMoves;

    public EnemiesMoved(long date, List<EnemyMove> enemyMoves) {
        super(date, SERVER_RID);
        this.enemyMoves = enemyMoves;
    }

    public List<EnemyMove> getEnemyMoves() {
        return enemyMoves;
    }

    public void setEnemyMoves(List<EnemyMove> enemyMoves) {
        this.enemyMoves = enemyMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EnemiesMoved that = (EnemiesMoved) o;

        return enemyMoves.equals(that.enemyMoves);

    }

    @Override
    public String toString() {
        return "EnemiesMoved[" +
                "enemyMoves=" + enemyMoves +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
