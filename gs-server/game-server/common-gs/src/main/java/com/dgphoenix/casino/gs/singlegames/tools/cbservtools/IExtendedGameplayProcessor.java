package com.abs.casino.gs.singlegames.tools.cbservtools;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.dblink.IDBLink;


/**
 * User: Grien
 * Date: 02.06.2014 17:58
 */
public interface IExtendedGameplayProcessor {
    Iterable<String> process(IDBLink dbLink, boolean roundFinished, boolean isEnter) throws CommonException;

    StringBuilder createBetParameter(long accountId, short gamePosition, long gameId) throws CommonException;
}
