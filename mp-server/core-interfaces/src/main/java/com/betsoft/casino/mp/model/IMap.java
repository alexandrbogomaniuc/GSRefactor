package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.common.Coords;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;
import org.kynosarges.tektosyne.graph.GraphAgent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public interface IMap<MAP_ITEM extends IMapItem, MAPSHAPE extends IGameMapShape> extends GraphAgent<PointI> {
    int getId();

    short getWidth();

    short getHeight();

    List<MAP_ITEM> getItems();

    ReentrantLock getLockEnemy();

    int getItemsSize();

    List<Integer> getItemsTypeIds();

    IEnemy getItemById(Long enemyId);

    Long getNearestEnemy(PointD point, boolean excludeBaseEnemy, Long baseEnemyId, Long distance);

    Long getAllNearestEnemy(long time, PointD point, boolean excludeBaseEnemy, Long baseEnemyId, Long distance);

    Map<Long, Double> getNNearestEnemies(long time, PointD point, Long baseEnemyId, int numberEnemies);

    Map<Long, Double> getNNearestEnemies(long time, PointD point, Long baseEnemyId, int numberEnemies, IEnemyRange allowedRange);

    Map<Long, Double> getNNearestEnemies(long time, PointD point, Long baseEnemyId, int numberEnemies, IEnemyRange allowedRange,
                                         long timeBeforeLastPoint);

    Map<Long, Double> getNNearestEnemiesWithoutBase(long time, PointD point, Long baseEnemyId, int numberEnemies);

    void removeAllEnemies();

    List<Long> removeAllEnemiesAndGetIds();

    IEnemy addRandomEnemyFromPossibleList(short numberSeats, int skinId, boolean needStandOnPlace, IMathEnemy mathEnemy,
                                          long parentEnemyId, boolean needFinalSteps);

    boolean isWall(double nextX, double y);

    int getMapId();

    Map<Long, Trajectory> generateShortLeaveTrajectories();

    Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps);

    Map<Long, Trajectory> generateCustomUpdateTrajectories(IEnemyRange range, long duration, boolean needFinalSteps);

    Map<Long, Trajectory> generateFreezeTrajectories(long time, int freezeTime, double x, double y, int d);

    default Set<Long> getEnemiesForNewEnemyUpdating(boolean needFinalSteps, boolean needReturnAllEnemies){
        return Collections.emptySet();
    }

    void setMapShape(MAPSHAPE shape);

    MAPSHAPE getMapShape();

    void checkFreezeTimeEnemies(int maxFreezeTime);

    Long getAnyBossId();

    void updateEnemyMode(Long enemyId, EnemyMode enemyMode);

    Map<Long, Integer> getAdditionalEnemyModes();

    Coords getCoords();

    void setLogger(Logger logger);
}
