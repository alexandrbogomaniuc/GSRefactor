package com.dgphoenix.casino.gs.managers.game.event;

import com.abs.casino.gs.managers.dblink.IGameDBLink;

/**
 * User: Grien
 * Date: 27.08.2014 16:21
 */
public interface IGameEventProcessor {
    void process(IGameEvent gameEvent, IGameDBLink dbLink);
}
