package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.movement.Point;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

/**
 * User: flsh
 * Date: 06.02.19.
 */
public interface IGameMapShape {
    void updateSpawnPoints();
    short getWidth();
    short getHeight();
    boolean isPassable(int x, int y);
    boolean isAvailableAndPassable(Point point);
    boolean isSpawnPoint(int x, int y);
    boolean isWall(int x, int y);
    boolean isBorder(int x, int y);
    boolean isNotMarked(int x, int y);
    boolean isBossPath(int x, int y);
    boolean isWallForLargeEnemies(int x, int y);
    boolean isValid(int x, int y);
    List<PointI> getSpawnPoints();
    void addWall(int x, int y);
    void removeWall(int x, int y);
    MoveDirection getMoveDirection(int x, int y);
    void setMoveDirection(int x, int y, MoveDirection direction);
    boolean isPassableForLargeEnemies(int x, int y);
    boolean isMasked(int x, int y, byte mask);
    PointD getCenter();
    List<PointD> getPoints(String key);
}
