package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.INewEnemy;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class NewEnemy extends TObject implements INewEnemy<RoomEnemy> {
    private RoomEnemy newEnemy;

    public NewEnemy(long date, IRoomEnemy newEnemy) {
        super(date, SERVER_RID);
        this.newEnemy = newEnemy instanceof RoomEnemy ? (RoomEnemy) newEnemy : new RoomEnemy(
                newEnemy.getId(),
                newEnemy.getTypeId(),
                newEnemy.isBoss(),
                newEnemy.getSpeed(),
                newEnemy.getAwardedPrizes(),
                newEnemy.getAwardedSum(),
                newEnemy.getEnergy(),
                newEnemy.getSkin(),
                newEnemy.getTrajectory(),
                newEnemy.getParentEnemyId(),
                newEnemy.getFullEnergy(),
                newEnemy.getMembers(),
                newEnemy.getSwarmId(),
                newEnemy.getSwarmType(),
                newEnemy.getParentEnemyTypeId()
        );
    }

    @Override
    public RoomEnemy getNewEnemy() {
        return newEnemy;
    }

    @Override
    public void setNewEnemy(RoomEnemy newEnemy) {
        this.newEnemy = newEnemy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NewEnemy newEnemy1 = (NewEnemy) o;

        return newEnemy.equals(newEnemy1.newEnemy);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + newEnemy.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NewEnemy[" +
                "newEnemy=" + newEnemy +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
