package com.abs.casino.promo.wins.handlers;
import com.abs.casino.common.promo.*;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: flsh
 * Date: 10.12.16.
 */
public class PrizeWonHandlersFactory implements IPrizeWonHandlersFactory {
    private final Map<Class<?>, IPrizeWonHandler<?>> handlers = new ConcurrentHashMap<>();

    public PrizeWonHandlersFactory() {
        addHandler(TicketPrize.class, new TicketWonHandler());
        addHandler(FRBonusPrize.class, new FRBonusWonHandler());
        addHandler(CacheBonusPrize.class, new CacheBonusWonHandler());
        addHandler(InstantMoneyPrize.class, new InstantMoneyWonHandler());
    }

    private <T extends IPrize> void addHandler(Class<T> clazz, IPrizeWonHandler<T> handler) {
        handlers.put(clazz, handler);
    }

    @Override
    public <T extends IPrize> IPrizeWonHandler<T> getHandler(T prize) throws CommonException {
        @SuppressWarnings("unchecked")
        IPrizeWonHandler<T> handler = (IPrizeWonHandler<T>) handlers.get(prize.getClass());
        if (handler == null) {
            throw new CommonException("Handler not found for class: " + prize.getClass());
        }
        return handler;
    }
}
