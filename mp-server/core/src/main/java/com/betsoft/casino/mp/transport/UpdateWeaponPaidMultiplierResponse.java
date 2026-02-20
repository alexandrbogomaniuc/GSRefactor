package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.common.math.SWPaidCosts;
import com.betsoft.casino.mp.model.IUpdateWeaponPaidMultiplierResponse;
import com.betsoft.casino.utils.TObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpdateWeaponPaidMultiplierResponse extends TObject implements IUpdateWeaponPaidMultiplierResponse {
    private List<SWPaidCosts> weaponPaidMultiplier;

    public UpdateWeaponPaidMultiplierResponse(long date, int rid, Map<Integer, Integer> weaponPaidMultiplier) {
        super(date, rid);
        this.weaponPaidMultiplier = new ArrayList<>();
        weaponPaidMultiplier.forEach((weapon, price) -> this.weaponPaidMultiplier.add(new SWPaidCosts(weapon, price)));
    }

    @Override
    public List<SWPaidCosts> getWeaponPaidMultiplier() {
        return weaponPaidMultiplier;
    }

    public void setWeaponPaidMultiplier(List<SWPaidCosts> weaponPaidMultiplier) {
        this.weaponPaidMultiplier = weaponPaidMultiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UpdateWeaponPaidMultiplierResponse that = (UpdateWeaponPaidMultiplierResponse) o;

        return Objects.equals(weaponPaidMultiplier, that.weaponPaidMultiplier);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (weaponPaidMultiplier != null ? weaponPaidMultiplier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateWeaponPaidMultiplierResponse" + "[" +
                "date=" + date +
                ", rid=" + rid +
                ", weaponPaidMultiplier=" + weaponPaidMultiplier +
                ']';
    }
}
