package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IEnemy;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.IMovementStrategy;
import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 08.02.19.
 */
public abstract class AbstractMovementStrategy<ENEMY extends IEnemy, MS extends AbstractMovementStrategy> implements IMovementStrategy<ENEMY>,
        KryoSerializable, JsonSelfSerializable<MS> {
    protected transient ENEMY self;
    protected transient IMap<ENEMY, GameMapShape> map;

    public AbstractMovementStrategy() {
    }

    public AbstractMovementStrategy(ENEMY self, IMap<ENEMY, GameMapShape> map) {
        this.self = self;
        this.map = map;
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public void setSelf(ENEMY self) {
        this.self = self;
    }

    @Override
    public void setMap(IMap map) {
        //noinspection unchecked
        this.map = map;
    }

}
