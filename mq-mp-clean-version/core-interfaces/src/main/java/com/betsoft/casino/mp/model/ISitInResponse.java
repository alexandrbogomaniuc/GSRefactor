package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 02.06.2022.
 */
public interface ISitInResponse extends ITransportObject {
    void setMaxMultiplier(Double maxMultiplier);

    void setMaxPlayerProfitInRound(Long maxPlayerProfitInRound);

    void setTotalPlayersProfitInRound(Long totalPlayersProfitInRound);
}
