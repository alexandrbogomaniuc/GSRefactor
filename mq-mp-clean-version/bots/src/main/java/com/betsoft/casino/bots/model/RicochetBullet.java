package com.betsoft.casino.bots.model;

import com.betsoft.casino.bots.strategies.*;
import com.betsoft.casino.bots.utils.GeometryUtils;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.transport.RoomEnemy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.betsoft.casino.bots.utils.GeometryUtils.*;
import static com.dgphoenix.casino.common.util.string.DateTimeUtils.toHumanReadableFormat;

public class RicochetBullet {

    private static final Logger LOG = LogManager.getLogger(RicochetBullet.class);

    static double SIZE_LEFT = 0;
    static double SIZE_TOP = 0 + 16;
    static double SIZE_RIGHT = 960;
    static double SIZE_BOTTOM = 540 - 11;

    static double FLY_SPEED = 1.25;

    private double bulletAngle;
    private double angleCos;
    private double angleSin;
    private String bulletId;
    private Point startPoint;
    private Point currPoint;
    private Point endPoint;
    private int weaponId;
    private boolean changeDirAllowed;
    private int bulletType;
    private boolean enabled;
    private int rid = 0;
    private boolean isPaidShot;
    private int weaponPrice;

    private Point prevBulletLocation = null;

    public RicochetBullet(String bulletId, Point startPoint, Point endPoint, int weaponId, boolean isPaidShot, int weaponPrice) {
        this(bulletId, startPoint, endPoint, weaponId, isPaidShot, weaponPrice, true, 1, true);
    }

    public RicochetBullet(String bulletId, Point startPoint, Point endPoint, int weaponId, boolean isPaidShot, int weaponPrice, boolean changeDirAllowed, int bulletType, boolean enabled) {

        this.bulletId = bulletId;
        this.weaponId = weaponId;
        this.isPaidShot = isPaidShot;
        this.weaponPrice = weaponPrice;
        this.changeDirAllowed = changeDirAllowed;
        this.bulletType = bulletType;
        this.enabled = enabled;

        bulletFly(startPoint, endPoint);
        Point currPoint = startPoint != null ? new Point(startPoint.getX(), startPoint.getY(), startPoint.getTime()) : null;
        setCurrPoint(currPoint);

        //LOG.info("RicochetBullet: RicochetBullet={}", this);
    }

    private void recalculateAngle(Point startPoint, Point endPoint) {

        //LOG.info("recalculateAngle: startPoint={}, endPoint={}", startPoint, endPoint);

        if(this.getStartPoint() != null && this.getEndPoint() != null) {
            double bulletAngle = Math.atan2(endPoint.getY() - startPoint.getY(), endPoint.getX() - startPoint.getX());
            setBulletAngle(bulletAngle);
            setAngleCos(Math.cos(bulletAngle));
            setAngleSin(Math.sin(bulletAngle));
        }
    }

    public Point getWallsCollisionPoint() {

        Point wallsCollisionPoint;
        switch(this.bulletType)
        {
            case 5:
                wallsCollisionPoint = new Point(0, -40, 0);
            case 4:
                wallsCollisionPoint = new Point(0, -20, 0);
            case 3:
            case 2:
                wallsCollisionPoint = new Point(0, -18, 0);
            case 1:
            default:
                wallsCollisionPoint = new Point(0, -15, 0);
        }

        //LOG.info("getWallsCollisionPoint: wallsCollisionPoint={}", wallsCollisionPoint);

        return wallsCollisionPoint;
    }

    private Point[] moveOutOfBoundsIfRequired(Point[] points) {

        //LOG.info("moveOutOfBoundsIfRequired: before adjustment points={}", (Object) points);

        if(points == null || points.length < 1) {
            return null;
        }

        Point newPoint = points[0];

        if(newPoint == null) {
            return null;
        }

        if(newPoint.getX() >= SIZE_RIGHT) {
            newPoint.setX(newPoint.getX() - 1);
        }

        if(newPoint.getX() <= SIZE_LEFT) {
            newPoint.setX(newPoint.getX() + 1);
        }

        if(newPoint.getY() <= SIZE_TOP) {
            newPoint.setX(newPoint.getX() + 0.56);
        }

        if(newPoint.getY() <= SIZE_BOTTOM) {
            newPoint.setX(newPoint.getX() - 0.56);
        }

        //LOG.info("moveOutOfBoundsIfRequired: after adjustment points={}", (Object) points);

        return points;
    }

    private Point[] getNewPointToMoveFromCollision(Point startPoint, Point currPoint, Point[] hitSide) {

        //LOG.info("getNewPointToMoveFromCollision: startPoint={}, currPoint={}, hitSide={}", startPoint, currPoint, hitSide);

        if(currPoint == null || startPoint == null || hitSide == null || hitSide.length < 2) {
            return null;
        }

        Point hitSidePoint1 = hitSide[0];
        Point hitSidePoint2 = hitSide[1];

        if(hitSidePoint1 == null || hitSidePoint2 == null) {
            return null;
        }

        Point intersectPoint = GeometryUtils.getIntersectionBetweenTwoInfiniteLines(hitSidePoint1, hitSidePoint2, startPoint, currPoint);

        if(intersectPoint == null) {
            intersectPoint = new Point(0d, 0d, System.currentTimeMillis());// Need for shifting point from current position outside of bounds
        }

        double a1 = Math.atan2(hitSidePoint2.getY() - hitSidePoint1.getY(), hitSidePoint2.getX() - hitSidePoint1.getX());
        double a2 = Math.atan2(intersectPoint.getY() - startPoint.getY(), intersectPoint.getX() - startPoint.getX());
        double a = a1 * 2 - a2; //angle between bullet trajectory and crossed line

        //LOG.info("getNewPointToMoveFromCollision: intersectPoint={}, a1={}, a2={}, a={}", intersectPoint, a1, a2, a);

        double twoPI = 2 * Math.PI;

        while ( a < 0) {
            a += twoPI;
        }

        while ( a >= twoPI) {
            a -= twoPI;
        }

        double distance = GeometryUtils.getDistance(startPoint, intersectPoint);

        double newX = intersectPoint.getX() + Math.cos(a) * distance;
        double newY = intersectPoint.getY() + Math.sin(a) * distance;
        Point newPoint =  new Point(newX, newY, System.currentTimeMillis());

        //LOG.info("getNewPointToMoveFromCollision: a={}, distance={}, intersectPoint={}, newPoint={}", a, distance, intersectPoint, newPoint);

        Point[] returnPoints = new Point[] {newPoint, intersectPoint};

        return moveOutOfBoundsIfRequired(returnPoints);
    }

    private Point[] checkForCollision(Point currPoint, Point startPoint, boolean changeDirAllowed) {

        //LOG.info("checkForCollision: currPoint={}, startPoint={}, changeDirAllowed={}", currPoint, startPoint, changeDirAllowed);

        Point[] newPointToMove = null;
        Point[] hitSide = null;

        if(currPoint == null || startPoint == null) {
            return null;
        }

        if(currPoint.getX() > SIZE_RIGHT) {
            hitSide = new Point[] {new Point(SIZE_RIGHT, SIZE_BOTTOM, 0), new Point(SIZE_RIGHT, SIZE_TOP, 0)};
        } else if(currPoint.getX() < SIZE_LEFT) {
            hitSide = new Point[] {new Point(SIZE_LEFT, SIZE_TOP, 0), new Point(SIZE_LEFT, SIZE_BOTTOM, 0)};
        } else if(currPoint.getY() < SIZE_TOP) {
            hitSide = new Point[] {new Point(SIZE_RIGHT, SIZE_TOP, 0), new Point(SIZE_LEFT, SIZE_TOP, 0)};
        } else if(currPoint.getY() > SIZE_BOTTOM) {
            hitSide = new Point[] {new Point(SIZE_LEFT, SIZE_BOTTOM, 0), new Point(SIZE_RIGHT, SIZE_BOTTOM, 0)};
        }

        //LOG.info("checkForCollision: hitSide={}", (Object) hitSide);

        if(hitSide != null && changeDirAllowed) {
                newPointToMove = getNewPointToMoveFromCollision(startPoint, currPoint, hitSide);
        }

        return newPointToMove;
    }

    public RoomEnemy collide(List<RoomEnemy> enemies, IRoomBotStrategy strategy, long serverTime) {

        if (!(strategy instanceof IRoomNaturalBotStrategy)) {
            return null;
        }

        IRoomNaturalBotStrategy roomNaturalBotStrategy = (IRoomNaturalBotStrategy)strategy;
        Point bulletLocation = getCurrPoint();

        for(RoomEnemy roomEnemy: enemies) {
            if(collide(roomNaturalBotStrategy, roomEnemy, bulletLocation, prevBulletLocation, serverTime)) {
                return roomEnemy;
            }
        }

        prevBulletLocation = bulletLocation;

        LOG.info("collide: severTime={}, bulletId={}, collide=false, bulletLocation={}",
                toHumanReadableFormat(serverTime), getBulletId(), bulletLocation);

        return null;
    }


    public boolean collide(IRoomNaturalBotStrategy roomNaturalBotStrategy, RoomEnemy roomEnemy, Point bulletLocation, Point prevBulletLocation, long serverTime) {

        Point enemyLocation = roomNaturalBotStrategy.getLocationOnScreen(roomEnemy, serverTime);
        if (enemyLocation == null) {
            return false;
        }

        EnemySize enemySize = roomNaturalBotStrategy.getEnemySize(roomEnemy.getTypeId());
        boolean collide = isPointInsideOfEllipse(bulletLocation, enemyLocation, enemySize.getWidth(), enemySize.getHeight());

        if(collide) {
            LOG.info("collide: severTime={}, bulletId={}, enemyId={}, collide=true, enemyLocation={}, bulletLocation={}",
                    toHumanReadableFormat(serverTime), getBulletId(), roomEnemy.getId(), enemyLocation, bulletLocation);
        }

        return collide;
    }

    public void tick(long deltaTime) {

        if(this.getEndPoint() == null) {
            return;
        }

        Point currPoint = this.getCurrPoint();
        //LOG.info("tick: bulletId={}, deltaTime={}, currPoint={}", getBulletId(), deltaTime, currPoint);


        if(currPoint != null) {

            double step = deltaTime * FLY_SPEED;
            double x = currPoint.getX();
            double y = currPoint.getY();
            long currentPointTime = currPoint.getTime();

            x += this.getAngleCos() * step;
            y += this.getAngleSin() * step;
            currentPointTime += deltaTime;

            long timeDiff = System.currentTimeMillis() - currentPointTime - deltaTime;

            if (timeDiff > 0) {
                long halfDeltaTime = deltaTime / 2;

                //if(lastHand) {
                //    halfDeltaTime += 10; //Speed up x10 times
                //}

                if (timeDiff < halfDeltaTime) {
                    halfDeltaTime = timeDiff;
                }

                //LOG.info("tick: timeDiff={}, halfDeltaTime={}", timeDiff, halfDeltaTime);

                step = halfDeltaTime * FLY_SPEED;
                x += this.getAngleCos() * step;
                y += this.getAngleSin() * step;
                currentPointTime += halfDeltaTime;
            }

            currPoint.setX(x);
            currPoint.setY(y);
            currPoint.setTime(currentPointTime);

            //LOG.info("tick: currPoint={}", currPoint);
        }

        onMove();
    }

    public void bulletFly(Point startPoint, Point endPoint) {

        //LOG.info("bulletFly: 1. startPoint={}, endPoint={}, bulletAngle={}, angleSin={}, angleCos={}", startPoint, endPoint, bulletAngle, angleSin, angleCos);

        if(startPoint == null || endPoint == null) {
            return;
        }

        if(startPoint.getX() == endPoint.getX()) {
            endPoint.setX(endPoint.getX() + 1);
        }

        if(startPoint.getY() == endPoint.getY()) {
            endPoint.setY(endPoint.getY() + 1);
        }

        setStartPoint(startPoint);
        setEndPoint(endPoint);
        recalculateAngle(startPoint, endPoint);

        //LOG.info("bulletFly: 2. startPoint={}, endPoint={}, bulletAngle={}, angleSin={}, angleCos={}", startPoint, endPoint, bulletAngle, angleSin, angleCos);
    }

    private void changeBulletDirection(Point[] collisionPoints) {

        Point currPoint = getCurrPoint();

        //LOG.info("changeBulletDirection: collisionPoints={}, currPoint={}", (Object) collisionPoints, currPoint);

        if(collisionPoints != null && collisionPoints.length > 0) {

            Point newPointToMove = collisionPoints[0];

            if(collisionPoints.length > 1) {
                Point intersectPoint = collisionPoints[1];
                if(intersectPoint != null) {
                    currPoint.setX(intersectPoint.getX());
                    currPoint.setY(intersectPoint.getY());
                }
            }

            //LOG.info("changeBulletDirection: currPoint={}", currPoint);

            Point startPoint = new Point(currPoint.getX(), currPoint.getY(), currPoint.getTime());
            Point endPoint = new Point(newPointToMove.getX(), newPointToMove.getY(), newPointToMove.getTime());
            bulletFly(startPoint, endPoint);
        }
    }

    public void onMove() {

        Point offsetPoint = getWallsCollisionPoint();
        Point startPoint = this.getStartPoint();
        Point currPoint = this.getCurrPoint();

        //LOG.info("onMove: offsetPoint={}, startPoint={}, currPoint={}", offsetPoint, startPoint, currPoint);

        if (startPoint != null && currPoint != null) {

            double startPointX = startPoint.getX() + offsetPoint.getX();
            double startPointY = startPoint.getY() + offsetPoint.getY();
            Point startPointWithOffset = new Point(startPointX, startPointY, startPoint.getTime());

            double currPointX = currPoint.getX() + offsetPoint.getX();
            double currPointY = currPoint.getY() + offsetPoint.getY();
            Point currPointWithOffset = new Point(currPointX, currPointY, currPoint.getTime());

            boolean isChangeDirAllowed = isChangeDirAllowed();

            //LOG.info("onMove: startPointWithOffset={}, currPointWithOffset={}, isChangeDirAllowed={}", startPointWithOffset, currPointWithOffset, isChangeDirAllowed);

            Point[] collisionPoints = this.checkForCollision(currPointWithOffset, startPointWithOffset, isChangeDirAllowed);

            //LOG.info("onMove: collisionPoints={}", (Object) collisionPoints);

            if (collisionPoints != null && collisionPoints.length > 0) {

                Point newPointToMove = collisionPoints[0];
                if (newPointToMove != null) {
                    if (isChangeDirAllowed) {
                        double newPointToMoveX = newPointToMove.getX() - offsetPoint.getX();
                        double newPointToMoveY = newPointToMove.getY() - offsetPoint.getY();
                        newPointToMove.setX(newPointToMoveX);
                        newPointToMove.setY(newPointToMoveY);

                        //LOG.info("onMove: offset removed newPointToMove={}", newPointToMove);

                        if (collisionPoints.length > 1) {
                            Point intersectPoint = collisionPoints[1];
                            if (intersectPoint != null) {
                                double intersectPointX = intersectPoint.getX() - offsetPoint.getX();
                                double intersectPointY = intersectPoint.getY() - offsetPoint.getY();
                                intersectPoint.setX(intersectPointX);
                                intersectPoint.setY(intersectPointY);
                            }

                            //LOG.info("onMove: offset removed intersectPoint={}", intersectPoint);
                        }

                        changeBulletDirection(collisionPoints);

                    } else {
                        // Destroy bullet if new direction not needed and it run out of bounds
                    }
                }
            }
        }
    }

    public double getBulletAngle() {
        return bulletAngle;
    }

    public void setBulletAngle(double bulletAngle) {
        this.bulletAngle = bulletAngle;
    }

    public double getAngleCos() {
        return angleCos;
    }

    public void setAngleCos(double angleCos) {
        this.angleCos = angleCos;
    }

    public double getAngleSin() {
        return angleSin;
    }

    public void setAngleSin(double angleSin) {
        this.angleSin = angleSin;
    }

    public String getBulletId() {
        return bulletId;
    }

    public void setBulletId(String bulletId) {
        this.bulletId = bulletId;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getCurrPoint() {
        return currPoint;
    }

    public void setCurrPoint(Point currPoint) {
        this.currPoint = currPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    public int getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(int weaponId) {
        this.weaponId = weaponId;
    }

    public boolean isChangeDirAllowed() {
        return changeDirAllowed;
    }

    public void setChangeDirAllowed(boolean changeDirAllowed) {
        this.changeDirAllowed = changeDirAllowed;
    }

    public int getBulletType() {
        return bulletType;
    }

    public void setBulletType(int bulletType) {
        this.bulletType = bulletType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public boolean isPaidShot() {
        return isPaidShot;
    }

    public void setPaidShot(boolean paidShot) {
        isPaidShot = paidShot;
    }

    public int getWeaponPrice() {
        return weaponPrice;
    }

    public void setWeaponPrice(int weaponPrice) {
        this.weaponPrice = weaponPrice;
    }

    @Override
    public String toString() {
        return "RicochetBullet{" +
                "bulletAngle=" + bulletAngle +
                ", angleCos=" + angleCos +
                ", angleSin=" + angleSin +
                ", bulletId='" + bulletId + '\'' +
                ", startPoint=" + startPoint +
                ", currPoint=" + currPoint +
                ", endPoint=" + endPoint +
                ", weaponId=" + weaponId +
                ", changeDirAllowed=" + changeDirAllowed +
                ", bulletType=" + bulletType +
                ", enabled=" + enabled +
                ", rid=" + rid +
                ", isPaidShot=" + isPaidShot +
                ", weaponPrice=" + weaponPrice +
                ", prevBulletLocation=" + prevBulletLocation +
                '}';
    }
}
