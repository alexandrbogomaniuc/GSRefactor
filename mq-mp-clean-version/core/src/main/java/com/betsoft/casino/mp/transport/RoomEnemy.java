package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.FormationType;
import com.betsoft.casino.mp.model.IMember;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.Member;
import com.betsoft.casino.mp.model.movement.*;
import org.kynosarges.tektosyne.geometry.PointD;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class RoomEnemy implements IRoomEnemy<Member>, Serializable {
    private long id;
    private long typeId;
    private  boolean isBoss;
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
    private List<Member> members;
    private long swarmId;
    private long swarmType;
    private List<PointExt> detailPoints;

    public RoomEnemy(long id, long typeId, boolean isBoss, double speed, String awardedPrizes,
                     double awardedSum, double energy, int skin, Trajectory trajectory, long parentEnemyId,
                     double fullEnergy, List<IMember> members, long swarmId, long swarmType, long parentEnemyTypeId) {
        this.id = id;
        this.typeId = typeId;
        this.isBoss = isBoss;
        this.speed = speed;
        this.awardedPrizes = awardedPrizes;
        this.awardedSum = awardedSum;
        this.energy = energy;
        this.skin = skin;
        this.trajectory = trajectory;
        this.parentEnemyId = parentEnemyId;
        this.fullEnergy = fullEnergy;
        if (members != null) {
            this.members = Member.convert(members);
        }
        this.swarmId = swarmId;
        this.swarmType = swarmType;
        this.parentEnemyTypeId = parentEnemyTypeId;
        this.detailPoints = new ArrayList<>();
        if (trajectory != null && trajectory.getPoints() != null) {
            List<Point> points = trajectory.getPoints();
            points.forEach(point -> {
                boolean isInvulnerable = false;
                boolean isTeleport = false;
                boolean isFreeze = false;
                if (point instanceof InvulnerablePoint) {
                    isInvulnerable = true;
                } else if (point instanceof TeleportPoint) {
                    TeleportPoint teleportPoint = (TeleportPoint) point;
                    isTeleport = teleportPoint.isTeleport();
                    isInvulnerable = teleportPoint.isInvulnerable();
                } else if (point instanceof FreezePoint) {
                    isFreeze = true;
                }
                detailPoints.add(new PointExt(point.getX(), point.getY(), point.getTime(), isInvulnerable, isTeleport, isFreeze));
            });
        }
    }

    public static List<RoomEnemy> convert(List<IRoomEnemy> roomEnemies) {
        List<RoomEnemy> result = new ArrayList<>(roomEnemies.size());
        for (IRoomEnemy roomEnemy : roomEnemies) {
            if (roomEnemy instanceof RoomEnemy) {
                result.add((RoomEnemy) roomEnemy);
            } else {
                result.add(new RoomEnemy(
                        roomEnemy.getId(),
                        roomEnemy.getTypeId(),
                        roomEnemy.isBoss(),
                        roomEnemy.getSpeed(),
                        roomEnemy.getAwardedPrizes(),
                        roomEnemy.getAwardedSum(),
                        roomEnemy.getEnergy(),
                        roomEnemy.getSkin(),
                        roomEnemy.getTrajectory(),
                        roomEnemy.getParentEnemyId(),
                        roomEnemy.getFullEnergy(),
                        roomEnemy.getMembers(),
                        roomEnemy.getSwarmId(),
                        roomEnemy.getSwarmType(),
                        roomEnemy.getParentEnemyTypeId()
                ));
            }
        }
        return result;
    }

    public static RoomEnemy convert(IRoomEnemy roomEnemy) {
        return new RoomEnemy(
                roomEnemy.getId(),
                roomEnemy.getTypeId(),
                roomEnemy.isBoss(),
                roomEnemy.getSpeed(),
                roomEnemy.getAwardedPrizes(),
                roomEnemy.getAwardedSum(),
                roomEnemy.getEnergy(),
                roomEnemy.getSkin(),
                roomEnemy.getTrajectory(),
                roomEnemy.getParentEnemyId(),
                roomEnemy.getFullEnergy(),
                roomEnemy.getMembers(),
                roomEnemy.getSwarmId(),
                roomEnemy.getSwarmType(),
                roomEnemy.getParentEnemyTypeId()
        );
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
    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
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

    public List<PointExt> getDetailPoints() {
        return detailPoints;
    }

    @Override
    public boolean isBoss() {
        return isBoss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomEnemy that = (RoomEnemy) o;

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
                ", detailPoints=" + detailPoints +
                ']';
    }
}
