package com.dgphoenix.casino.gs.singlegames.tools.cbservtools;

import com.dgphoenix.casino.gs.managers.dblink.IGameDBLink;

/**
 * User: flsh
 * Date: 17.01.14
 */
public interface IBaseServlet {
    IGameDBLink getDbLink();
}