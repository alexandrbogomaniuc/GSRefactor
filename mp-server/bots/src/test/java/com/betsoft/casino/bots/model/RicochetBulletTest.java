package com.betsoft.casino.bots.model;

import com.betsoft.casino.mp.model.movement.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

@Ignore
public class RicochetBulletTest {
    private static final Logger LOG = LogManager.getLogger(RicochetBulletTest.class);

    @Test
    public void flyRicochetBullet_AlongY_Test() throws Exception {

        //Assign
        String bulletId = "0_0";
        TurretPositions turretPositions = TurretPositions.getTurretPositionBySeatID(0);
        Point startPoint = new Point(turretPositions.getCoordinateX(), turretPositions.getCoordinateY() - 30, System.currentTimeMillis());
        Point endPoint = new Point(turretPositions.getCoordinateX(), turretPositions.getCoordinateY() - 40, System.currentTimeMillis());
        int weaponId = -1;
        boolean changeDirAllowed = true;
        int bulletType = 1;

        RicochetBullet ricochetBullet = new RicochetBullet(bulletId, startPoint, endPoint, weaponId, false, 1, changeDirAllowed, bulletType, true);

        //Action
        long currTimeMs = System.currentTimeMillis();
        long stopTimeMs = currTimeMs + 5000;

        while(currTimeMs < stopTimeMs) {

            Thread.sleep(16);
            long newTimeMs = System.currentTimeMillis();
            long deltaTimeMs = newTimeMs - currTimeMs;
            currTimeMs = newTimeMs;

            ricochetBullet.tick(deltaTimeMs);
            LOG.info("flyRicochetBullet_Start_Test: currTimeMs={}, stopTimeMs={}, deltaTimeMs={} StartPoint={}, CurrPoint={}, EndPoint={}",
                    toHumanReadableFormat(currTimeMs), toHumanReadableFormat(stopTimeMs), deltaTimeMs, ricochetBullet.getStartPoint(), ricochetBullet.getCurrPoint(), ricochetBullet.getEndPoint());
        }

        //Assert


    }
}
