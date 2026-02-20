package com.betsoft.casino.mp.common.maps;

import com.betsoft.casino.mp.common.GameMapShape;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

public class BirdsGameMapShape extends AbstractGameMapShapeWrapper {

    public BirdsGameMapShape(GameMapShape map) {
        super(map);
    }

    /**
     * As we have only 8 bits for flags, some of them are reused for different purpose
     * In the future it would be good to use int instead of byte to extend amount of flags to 32
     */
    @Override
    public boolean isWall(int x, int y) {
        return map.isMasked(x, y, GameMapShape.LARGE_ENEMIES_WALL_MASK);
    }

    @Override
    public boolean isPassable(int x, int y) {
        return map.isValid(x, y) && !isWall(x, y);
    }

    @Override
    public boolean isSpawnPoint(int x, int y) {
        return map.isValid(x, y) && super.isBossPath(x, y);
    }

    @Override
    public List<PointI> getSpawnPoints() {
        List<PointI> spawnPoints = new ArrayList<>();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (isSpawnPoint(x, y)) {
                    spawnPoints.add(new PointI(x, y));
                }
            }
        }
        return spawnPoints;
    }
}
