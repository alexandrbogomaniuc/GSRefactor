package com.abs.casino.common.games;

import com.abs.casino.common.games.IDelegatedStartGameHelper;
import com.abs.casino.common.games.IStartGameHelper;

/**
 * User: flsh
 * Date: 24.07.13
 */
public interface IHelperCreator {
    IStartGameHelper create(boolean old, long gameId, String servletName, String title, String swfLocation,
                            String additionalParams, IDelegatedStartGameHelper delegatedHelper);
}
