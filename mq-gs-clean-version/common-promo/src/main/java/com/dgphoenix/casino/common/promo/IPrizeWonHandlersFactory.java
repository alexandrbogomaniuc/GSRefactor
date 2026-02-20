package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 10.12.16.
 */
public interface IPrizeWonHandlersFactory {
    <T extends IPrize> IPrizeWonHandler<T> getHandler(T prize) throws CommonException;
}
