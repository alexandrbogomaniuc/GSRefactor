package com.dgphoenix.casino.gs.singlegames.tools.cbservtools;

import com.dgphoenix.casino.gs.managers.dblink.IGameDBLink;

public interface ICoinGame {
    String getDefaultBetPerLine(IGameDBLink dbLink);

    String getDefaultNumLines(IGameDBLink dbLink);
}
