package com.betsoft.casino.mp.model;


public interface IMovementStrategy<ENEMY extends IEnemy> {

    /**
     * Updates enemy position and changes movement direction if necessary
     *
     * @return true if enemy should be removed
     */
    boolean update();

    void setSelf(ENEMY self);

    void setMap(IMap map);
}
