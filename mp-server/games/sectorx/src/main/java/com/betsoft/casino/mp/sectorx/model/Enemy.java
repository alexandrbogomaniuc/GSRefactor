package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.common.AbstractEnemy;
import com.betsoft.casino.mp.model.FormationType;
import com.betsoft.casino.mp.model.IEnemyPrize;
import com.betsoft.casino.mp.model.IMathEnemy;
import com.betsoft.casino.mp.model.IMember;
import com.betsoft.casino.mp.model.movement.BezierTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Enemy extends AbstractEnemy<EnemyClass, Enemy> {

    private static final Logger LOG = LogManager.getLogger(Enemy.class);

    private EnemyClass enemyClass;
    private List<IEnemyPrize> awardedPrizes = new ArrayList<>();

    public Enemy(long id, EnemyClass enemyClass, int skin, Trajectory trajectory, IMathEnemy mathEnemy, long parentEnemyId,
                 List<IMember> members) {
        super(id, skin, trajectory, mathEnemy, parentEnemyId, members);
        this.enemyClass = enemyClass;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public EnemyClass getEnemyClass() {
        return enemyClass;
    }

    @Override
    public String getAwardedPrizesAsString() {
        return StringUtils.toString(awardedPrizes, ",", enemyPrize -> String.valueOf(enemyPrize.getOrdinalValue()));
    }

    @Override
    public List<IEnemyPrize> getAwardedPrizes() {
        return awardedPrizes;
    }

    @Override
    public void setAwardedPrizes(List<IEnemyPrize> awardedPrizes) {
        this.awardedPrizes = awardedPrizes;
    }

    public PointD getEnemyLocation(long time) {
        Trajectory trajectory = getTrajectory();
        if (trajectory.getPoints().size() > 4) {
            return getLocation(time);
        } else {
            return getPointByTime(trajectory, time);
        }
    }

    public PointD getPointByTime(Trajectory trajectory, long timeForLocation) {
        Point location;
        List<Point> points = trajectory.getPoints();
        if (points.get(0).getTime() > timeForLocation) {
            return new PointD(points.get(0).getX(), points.get(0).getY());
        }

        Point lastPoint = points.get(points.size() - 1);
        if (timeForLocation >= lastPoint.getTime()) {
            return new PointD(lastPoint.getX(), lastPoint.getY());
        }

        if (this.swarmId == FormationType.HYBRID.getTypeId()) {
            long startTime = points.get(0).getTime();
            if (points.size() == 3) {
                double dt = 0.01;
                for (double t = 0; t < 1; t += dt) {
                    PointD pointD = getNextPoint(points, t);
                    startTime = startTime + 200;
                    location = new Point(pointD.x, pointD.y, startTime);
                    PointD resultPoint = getCircularPoint(location, points.get(0).getTime());
                    if (timeForLocation < startTime + 200) {
                        return resultPoint;
                    }
                }
            }
            if (points.size() == 4) {
                double dt = 0.01;
                for (double t = 0; t < 1; t += dt) {
                    PointD pointD = getNextPoint(points, t);
                    startTime = startTime + 200;
                    location = new Point(pointD.x, pointD.y, startTime);
                    PointD resultPoint = getCircularPoint(location, points.get(0).getTime());
                    if (timeForLocation < startTime + 200) {
                        return resultPoint;
                    }
                }
            }
            if (points.size() == 2) {
                double dt = 0.01;
                for (double t = 0; t < 1; t += dt) {
                    PointD pointD = getNextPoint(points, t);
                    startTime = startTime + 200;
                    location = new Point(pointD.x, pointD.y, startTime);
                    PointD resultPoint = getCircularPoint(location, points.get(0).getTime());
                    if (timeForLocation < startTime + 200) {
                        return resultPoint;
                    }
                }
            }

        }

        long startTime = points.get(0).getTime();
        if (points.size() == 3) {
            double dt = 0.01;
            for (double t = 0; t < 1; t += dt) {
                PointD pointD = getNextPoint(points, t);
                startTime = startTime + 200;
                location = new Point(pointD.x, pointD.y, startTime);
                if (timeForLocation < startTime + 200) {
                    return new PointD(location.getX(), location.getY());
                }
            }
        } else if (points.size() == 4){
            double dt = 0.01;
            for (double t = 0; t < 1; t += dt) {
                PointD pointD = getNextPoint(points, t);
                startTime = startTime + 200;
                location = new Point(pointD.x, pointD.y, startTime);
                if (timeForLocation < startTime + 200) {
                    return new PointD(location.getX(), location.getY());
                }
            }
        } else {
            double dt = 0.01;
            for (double t = 0; t < 1; t += dt) {
                PointD pointD = getNextPoint(points, t);
                startTime = startTime + 200;
                location = new Point(pointD.x, pointD.y, startTime);
                if (timeForLocation < startTime + 200) {
                    return new PointD(location.getX(), location.getY());
                }
            }
        }
        return null;
    }

    private PointD getCircularPoint (Point location, long firstTrajectoryPointTime) {
        long passTime = (long)((System.currentTimeMillis() - firstTrajectoryPointTime) * this.getSpeed() / 10000);
        BezierTrajectory bezierTrajectory = (BezierTrajectory) getTrajectory();
        if (bezierTrajectory.isCircularTrajectory() && bezierTrajectory.getCircularAngle() != -1) {
            passTime += Math.toRadians(bezierTrajectory.getCircularAngle());
        }
        double positionPtX = location.getX() + getCircularRadius(getEnemyType().getId()) * Math.sin(passTime);
        double positionPtY = location.getY() + getCircularRadius(getEnemyType().getId()) * Math.cos(passTime);
        return new PointD(positionPtX, positionPtY);
    }

    public PointD getNextPoint (List<Point> points, double t) {
        if (points.size() == 2) {
            double Xt1 = (1 - t) * points.get(0).getX() + t * points.get(1).getX();
            double Yt1 = (1 - t) * points.get(0).getY() + t * points.get(1).getY();
            return new PointD(Xt1, Yt1);
        }
        if (points.size() == 3) {
            double Xt1 = ((1 - t) * (1 - t) * points.get(0).getX() + 2 * t * (1 - t) * points.get(1).getX()
                    + t * t * points.get(2).getX());
            double Yt1 = ((1 - t) * (1 - t) * points.get(0).getY() + 2 * t * (1 - t) * points.get(1).getY()
                    + t * t * points.get(2).getY());
            return new PointD(Xt1, Yt1);
        } else {
            double Xt1 = ((1 - t) * (1 - t) * (1 - t) * points.get(0).getX() + 3 * t * (1 - t) * (1 - t) * points.get(1).getX()
                    + 3 * t * t * (1-t) * points.get(2).getX() + t * t * t * points.get(3).getX());
            double Yt1 = ((1 - t) * (1 - t) * (1 - t) * points.get(0).getY() + 3 * t * (1 - t) * (1 - t) * points.get(1).getY()
                    + 3 * t * t * (1 - t) * points.get(2).getY() + t * t * t * points.get(3).getY());
            return new PointD(Xt1, Yt1);
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Enemy [");
        sb.append(super.toString());
        sb.append(", enemyClass=").append(enemyClass);
        sb.append(", awardedPrizes=").append(awardedPrizes);
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected void writeEnemyClass(Kryo kryo, Output output) {
        kryo.writeObject(output, enemyClass);
    }

    @Override
    protected void readEnemyClass(Kryo kryo, Input input) {
        enemyClass = kryo.readObject(input, EnemyClass.class);
    }

    @Override
    protected void writeAwardedPrizes(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, awardedPrizes);
    }

    @Override
    protected void readAwardedPrizes(Kryo kryo, Input input) {
        awardedPrizes = (List<IEnemyPrize>) kryo.readClassAndObject(input);
    }

    @Override
    protected void serializeInheritorFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
    }

    @Override
    protected void serializeAwardedPrizes(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializeListField(gen, "awardedPrizes", awardedPrizes, new TypeReference<List<IEnemyPrize>>() {});
    }

    @Override
    protected void serializeEnemyClass(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("enemyClass", enemyClass);
    }

    @Override
    protected Enemy getDeserialized() {
        return this;
    }

    @Override
    protected void deserializeInheritorFields(JsonParser p,
                                              JsonNode node,
                                              DeserializationContext ctxt) {
    }

    @Override
    protected void deserializeAwardedPrizes(JsonParser p,
                                            JsonNode node,
                                            DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        awardedPrizes = om.convertValue(node.get("awardedPrizes"), new TypeReference<List<IEnemyPrize>>() {});
    }

    @Override
    protected void deserializeEnemyClass(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        enemyClass = om.convertValue("enemyClass", EnemyClass.class);
    }

    @Override
    public boolean isLocationNearEnd(long time) {
        return super.isLocationNearEnd(time);
    }

    public EnemyType getEnemyType() {
        return enemyClass.getEnemyType();
    }

    public double getFullEnergy() {
        return fullEnergy;
    }

    @Override
    public boolean isInvulnerable(long time) {
        return false;
    }

    //necessary for hybrid and spatial formations
    public int getCircularRadius(int typeId) {
        int radius = 0;
        EnemyType enemyType = EnemyType.getById(typeId);
        switch (enemyType) {
            case S1:
                radius = 60;
                break;
            case S2:
                radius = 60;
                break;
            case S3:
                radius = 60;
                break;
            case S4:
                radius = 60;
                break;
            case S5:
                radius = 74;
                break;
            case S6:
                radius = 72;
                break;
            case S7:
                radius = 56;
                break;
            case S8:
                radius = 56;
                break;
            case S9:
                radius = 56;
                break;
            case S10:
                radius = 80;
            case S11:
                radius = 82;
                break;
            case S12:
                radius = 80;
                break;
            case S13:
                radius = 74;
                break;
            case S14:
                radius = 86;
                break;
            case S15:
                radius = 86;
            case S16:
                radius = 88;
                break;
            case S17:
                radius = 95;
                break;
            case S18:
                radius = 95;
                break;
            case S19:
                radius = 95;
                break;
            case S20:
                radius = 95;
                break;
            case S21:
                radius = 94;
                break;
            case S22:
                radius = 64;
                break;
            case S23:
                radius = 94;
                break;
            case S24:
                radius = 94;
                break;
            case S25:
                radius = 90;
                break;
            case S26:
                radius = 76;
                break;
            case S28:
                radius = 66;
                break;
            case S29:
                radius = 76;
                break;
            case S30:
                radius = 82;
                break;
            case S31:
                radius = 84;
                break;
            case BOSS:
                radius = 200;
        }
        return radius;
    }
}
