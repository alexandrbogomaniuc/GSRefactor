package com.abs.casino.common.promo;
import com.dgphoenix.casino.common.promo.*;

import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 23.01.17.
 */
public interface IPlayerBonusQualifier extends KryoSerializable {
    boolean qualifyBonus(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize, PlayerBonusEvent event) throws CommonException;
}
