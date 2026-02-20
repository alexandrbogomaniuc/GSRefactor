/**
 * User: plastical
 * Date: 18.03.2010
 */
package com.dgphoenix.casino.gs.maintenance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerDataRemover {
    private static final Logger LOG = LogManager.getLogger(PlayerDataRemover.class);
    private static final PlayerDataRemover instance = new PlayerDataRemover();

    public static PlayerDataRemover getInstance() {
        return instance;
    }

    private PlayerDataRemover() {
    }

}
