package com.betsoft.casino.bots.model;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.movement.Point;

import java.util.HashMap;
import java.util.Map;

public class Turret {

    public static final int DEFAULT_WEAPON_ID = -1;

    private static final Map<Integer, int[]> MUZZLE_TIP_OFFSETS = new HashMap<Integer, int[]>() {{
       put(DEFAULT_WEAPON_ID, new int[]{ 48, 55, 60, 62, 70 }); //id=-1
       put(SpecialWeaponType.Plasma.getId(), new int[]{ 0 });//id=4 (Instakill)
       put(SpecialWeaponType.Railgun.getId(), new int[]{ 0 });//id=6
       put(SpecialWeaponType.ArtilleryStrike.getId(), new int[]{ 0 });//id=7
       put(SpecialWeaponType.Flamethrower.getId(), new int[]{ 61 });//id=9
       put(SpecialWeaponType.Cryogun.getId(), new int[]{ 0 });//id=10
       put(SpecialWeaponType.LevelUp.getId(), new int[]{ 48, 55, 60, 62, 70 });//id=16
    }};

    public static int getMuzzleTipOffset(int weaponId, int weaponLevel) { //default level = 1

        if(weaponLevel < 1) {
            weaponLevel = 1;
        }

        int[] muzzleTipOffset = MUZZLE_TIP_OFFSETS.get(weaponId);

        if(muzzleTipOffset != null && muzzleTipOffset.length >= weaponLevel) {
            return muzzleTipOffset[weaponLevel - 1];
        }

        return 0;
    }

    public static Point getMuzzleTipPoint(int seatId, Point endPoint, int weaponId, int weaponLevel) {

        TurretPositions turretPositions = TurretPositions.getTurretPositionBySeatID(seatId);

        double muzzleAngle = Math.atan2(endPoint.getY() - turretPositions.getCentreCoordinateY(), endPoint.getX() - turretPositions.getCentreCoordinateX());
        double angleCos = Math.cos(muzzleAngle);
        double angleSin = Math.sin(muzzleAngle);

        //double sign = turretPositions.getDirect();

        int muzzleTipOffset = getMuzzleTipOffset(weaponId, weaponLevel);

        double muzzleX = turretPositions.getCentreCoordinateX() + /*sign **/ angleCos * muzzleTipOffset;
        double muzzleY = turretPositions.getCentreCoordinateY() + /*sign **/ angleSin * muzzleTipOffset;

        return new Point(muzzleX, muzzleY, System.currentTimeMillis());
    }
}
