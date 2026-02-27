package com.abs.casino.common.currency;

import com.dgphoenix.casino.common.util.Pair;

/**
 * User: flsh
 * Date: 25.04.15.
 */
public interface ICurrencyRateChangedListener {
    void notify(Pair<String, String> ratePair);
}
