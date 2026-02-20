package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.common.math.SWPaidCosts;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;

public interface IUpdateWeaponPaidMultiplierResponse extends ITransportObject {
    List<SWPaidCosts> getWeaponPaidMultiplier();
}
