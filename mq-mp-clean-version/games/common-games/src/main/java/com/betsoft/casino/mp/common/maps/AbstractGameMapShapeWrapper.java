package com.betsoft.casino.mp.common.maps;

import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.MoveDirection;
import com.betsoft.casino.mp.model.movement.Point;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

public class AbstractGameMapShapeWrapper implements IGameMapShape {
    protected final GameMapShape map;

    public AbstractGameMapShapeWrapper(GameMapShape map) {
        this.map = map;
    }

    @Override
    public void updateSpawnPoints() {
        map.updateSpawnPoints();
    }

    @Override
    public short getWidth() {
        return map.getWidth();
    }

    @Override
    public short getHeight() {
        return map.getHeight();
    }

    @Override
    public boolean isPassable(int x, int y) {
        return map.isPassable(x, y);
    }

    @Override
    public boolean isAvailableAndPassable(Point point) {
        return map.isAvailableAndPassable(point);
    }

    @Override
    public boolean isSpawnPoint(int x, int y) {
        return map.isSpawnPoint(x, y);
    }

    @Override
    public boolean isWall(int x, int y) {
        return map.isWall(x, y);
    }

    @Override
    public boolean isBorder(int x, int y) {
        return map.isBorder(x, y);
    }

    @Override
    public boolean isNotMarked(int x, int y) {
        return map.isNotMarked(x, y);
    }

    @Override
    public boolean isBossPath(int x, int y) {
        return map.isBossPath(x, y);
    }

    @Override
    public boolean isWallForLargeEnemies(int x, int y) {
        return map.isWallForLargeEnemies(x, y);
    }

    @Override
    public List<PointI> getSpawnPoints() {
        return map.getSpawnPoints();
    }

    @Override
    public void addWall(int x, int y) {
        map.addWall(x, y);
    }

    @Override
    public void removeWall(int x, int y) {
        map.removeWall(x, y);
    }

    @Override
    public boolean isValid(int x, int y) {
        return map.isValid(x, y);
    }

    @Override
    public MoveDirection getMoveDirection(int x, int y) {
        return map.getMoveDirection(x, y);
    }

    @Override
    public void setMoveDirection(int x, int y, MoveDirection direction) {
        map.setMoveDirection(x, y, direction);
    }

    @Override
    public boolean isPassableForLargeEnemies(int x, int y) {
        return map.isPassableForLargeEnemies(x, y);
    }

    @Override
    public boolean isMasked(int x, int y, byte mask) {
        return map.isMasked(x, y, mask);
    }

    @Override
    public PointD getCenter() {
        return map.getCenter();
    }

    @Override
    public List<PointD> getPoints(String key) {
        return map.getPoints(key);
    }
}
