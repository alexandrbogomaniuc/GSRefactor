package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IMember;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.kynosarges.tektosyne.geometry.PointD;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StubRoomEnemy implements IRoomEnemy<StubMember>, Serializable {
    private long id;
    private long typeId;
    private double speed; // tiles per second
    private String awardedPrizes;
    private double awardedSum;
    //if energy<0 enemy is dead, remember that energy=0 for regular enemies
    private double energy;
    private double fullEnergy;
    private int skin;
    private Trajectory trajectory;
    private long parentEnemyId;
    private long parentEnemyTypeId;
    private List<StubMember> members;
    private long swarmId;
    private long swarmType;

    public StubRoomEnemy(long id, long typeId, double speed, String awardedPrizes,
                         double awardedSum, double energy, int skin, Trajectory trajectory, long parentEnemyId,
                         double fullEnergy, List<IMember> members, long swarmId, long swarmType) {
        this.id = id;
        this.typeId = typeId;
        this.speed = speed;
        this.awardedPrizes = awardedPrizes;
        this.awardedSum = awardedSum;
        this.energy = energy;
        this.skin = skin;
        this.trajectory = trajectory;
        this.parentEnemyId = parentEnemyId;
        this.fullEnergy = fullEnergy;
        if (members != null) {
            this.members = StubMember.convert(members);
        }
        this.swarmId = swarmId;
        this.swarmType = swarmType;
    }

    public StubRoomEnemy(long id, long typeId, double speed, String awardedPrizes,
                         double awardedSum, double energy, int skin, Trajectory trajectory, long parentEnemyId,
                         double fullEnergy, List<IMember> members, long swarmId, long swarmType, long parentEnemyTypeId) {
        this.id = id;
        this.typeId = typeId;
        this.speed = speed;
        this.awardedPrizes = awardedPrizes;
        this.awardedSum = awardedSum;
        this.energy = energy;
        this.skin = skin;
        this.trajectory = trajectory;
        this.parentEnemyId = parentEnemyId;
        this.fullEnergy = fullEnergy;
        if (members != null) {
            this.members = StubMember.convert(members);
        }
        this.swarmId = swarmId;
        this.swarmType = swarmType;
        this.parentEnemyTypeId = parentEnemyTypeId;
    }

    public static List<StubRoomEnemy> convert(List<IRoomEnemy> roomEnemies) {
        List<StubRoomEnemy> result = new ArrayList<>();
        for (IRoomEnemy roomEnemy : roomEnemies) {
            if (roomEnemy instanceof StubRoomEnemy) {
                result.add((StubRoomEnemy) roomEnemy);
            } else {
                result.add(new StubRoomEnemy(roomEnemy.getId(), roomEnemy.getTypeId(), roomEnemy.getSpeed(),
                        roomEnemy.getAwardedPrizes(), roomEnemy.getAwardedSum(), roomEnemy.getEnergy(),
                        roomEnemy.getSkin(), roomEnemy.getTrajectory(), roomEnemy.getParentEnemyId(),
                        roomEnemy.getFullEnergy(), roomEnemy.getMembers(), roomEnemy.getSwarmId(),
                        roomEnemy.getSwarmType()));
            }
        }
        return result;
    }

    public static StubRoomEnemy convert(IRoomEnemy roomEnemy) {
        return new StubRoomEnemy(roomEnemy.getId(), roomEnemy.getTypeId(), roomEnemy.getSpeed(),
                roomEnemy.getAwardedPrizes(), roomEnemy.getAwardedSum(), roomEnemy.getEnergy(),
                roomEnemy.getSkin(), roomEnemy.getTrajectory(), roomEnemy.getParentEnemyId(),
                roomEnemy.getFullEnergy(), roomEnemy.getMembers(), roomEnemy.getSwarmId(),
                roomEnemy.getSwarmType());
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getTypeId() {
        return typeId;
    }

    @Override
    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public String getAwardedPrizes() {
        return awardedPrizes;
    }

    @Override
    public void setAwardedPrizes(String awardedPrizes) {
        this.awardedPrizes = awardedPrizes;
    }

    @Override
    public double getAwardedSum() {
        return awardedSum;
    }

    @Override
    public void setAwardedSum(double awardedSum) {
        this.awardedSum = awardedSum;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    @Override
    public int getSkin() {
        return skin;
    }

    @Override
    public void setSkin(int skin) {
        this.skin = skin;
    }

    @Override
    public Trajectory getTrajectory() {
        return trajectory;
    }

    @Override
    public void setTrajectory(Trajectory trajectory) {
        this.trajectory = trajectory;
    }

    @Override
    public double getFullEnergy() {
        return fullEnergy;
    }

    @Override
    public void setFullEnergy(double fullEnergy) {
        this.fullEnergy = fullEnergy;
    }

    @Override
    public List<StubMember> getMembers() {
        return members;
    }

    public void setMembers(List<StubMember> members) {
        this.members = members;
    }

    @Override
    public long getSwarmId() {
        return swarmId;
    }

    @Override
    public void setSwarmId(long swarmId) {
        this.swarmId = swarmId;
    }

    @Override
    public long getSwarmType() {
        return swarmType;
    }

    @Override
    public void setSwarmTypeId(long swarmType) {
        this.swarmType = swarmType;
    }

    @Override
    public long getParentEnemyId() {
        return parentEnemyId;
    }

    @Override
    public long getParentEnemyTypeId() {
        return parentEnemyTypeId;
    }

    @Override
    public void setParentEnemyTypeId(long typeId) {
        this.parentEnemyTypeId = typeId;
    }

    @Override
    public boolean isBoss() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StubRoomEnemy that = (StubRoomEnemy) o;

        if (id != that.id) return false;
        if (typeId != that.typeId) return false;
        if (Double.compare(that.speed, speed) != 0) return false;
        return energy == that.energy;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (typeId ^ (typeId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "RoomEnemy[" +
                "id=" + id +
                ", typeId=" + typeId +
                ", speed=" + speed +
                ", awardedPrizes='" + awardedPrizes +
                "', awardedSum=" + awardedSum +
                ", energy=" + energy +
                ", skin=" + skin +
                ", trajectory=" + trajectory +
                ", parentEnemyId=" + parentEnemyId +
                ", parentEnemyTypeId=" + parentEnemyTypeId +
                ", fullEnergy=" + fullEnergy +
                ", members=" + members +
                ", swarmId=" + swarmId +
                ", swarmType=" + swarmType +
                ']';
    }
}
