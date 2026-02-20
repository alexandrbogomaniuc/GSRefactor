package com.dgphoenix.casino.common.games;

/**
 * User: flsh
 * Date: 24.07.13
 */
public interface IHelperCreator {
    IStartGameHelper create(boolean old, long gameId, String servletName, String title, String swfLocation,
                            String additionalParams, IDelegatedStartGameHelper delegatedHelper);
}
