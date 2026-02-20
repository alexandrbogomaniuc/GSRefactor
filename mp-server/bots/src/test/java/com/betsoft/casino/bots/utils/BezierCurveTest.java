package com.betsoft.casino.bots.utils;

import com.betsoft.casino.mp.model.movement.Point;
import org.junit.Test;

import static com.betsoft.casino.bots.utils.BezierCurve.getCurve;
import static org.junit.Assert.assertEquals;

public class BezierCurveTest {

    @Test
    public void getCurve_5_Control_Points_Test() throws Exception {

        Point[] points = {
                new Point(0.0, 0.0, 0),
                new Point(1.0, 2.0, 5),
                new Point(2.0, 3.0, 10),
                new Point(4.0, 2.0, 15),
                new Point(5.0, 0.0, 20)
        };
        double t = 0.5;

        // Compute Bézier curve point at t
        Point result = getCurve(points, t);

        assertEquals(2.3125d, result.getX(), 0);
        assertEquals(2.125, result.getY(), 0);
        assertEquals(8, result.getTime(), 0);
    }

    @Test
    public void getCurve_40_Control_Points_Test() throws Exception {

        Point[] points = {
                new Point(1320.0, 420.0, 1740589702881L),
                new Point(1130.0, 390.0, 1740589704256L),
                new Point(985.0, 370.0, 1740589705631L),
                new Point(840.0, 260.0, 1740589707006L),
                new Point(670.0, 450.0, 1740589708381L),
                new Point(310.0, 245.0, 1740589709756L),
                new Point(230.0, 470.0, 1740589711131L),
                new Point(150.0, 220.0, 1740589712506L),
                new Point(60.0, 410.0, 1740589713881L),
                new Point(0.0, 270.0, 1740589715256L),
                new Point(130.0, 440.0, 1740589716631L),
                new Point(290.0, 210.0, 1740589718006L),
                new Point(330.0, 400.0, 1740589719381L),
                new Point(470.0, 200.0, 1740589720756L),
                new Point(510.0, 490.0, 1740589722131L),
                new Point(650.0, 290.0, 1740589723506L),
                new Point(740.0, 480.0, 1740589724881L),
                new Point(830.0, 260.0, 1740589726256L),
                new Point(990.0, 430.0, 1740589727631L),
                new Point(840.0, 320.0, 1740589729006L),
                new Point(630.0, 230.0, 1740589730381L),
                new Point(410.0, 180.0, 1740589731756L),
                new Point(290.0, 70.0, 1740589733131L),
                new Point(90.0, 40.0, 1740589734506L),
                new Point(-40.0, 90.0, 1740589735881L),
                new Point(70.0, 160.0, 1740589737256L),
                new Point(120.0, 200.0, 1740589738631L),
                new Point(140.0, 240.0, 1740589740006L),
                new Point(180.0, 310.0, 1740589741381L),
                new Point(190.0, 350.0, 1740589742756L),
                new Point(200.0, 390.0, 1740589744131L),
                new Point(280.0, 440.0, 1740589745506L),
                new Point(335.0, 370.0, 1740589746881L),
                new Point(530.0, 280.0, 1740589748256L),
                new Point(250.0, 140.0, 1740589749631L),
                new Point(730.0, 20.0, 1740589751006L),
                new Point(300.0, 130.0, 1740589752381L),
                new Point(900.0, 270.0, 1740589753756L),
                new Point(440.0, 420.0, 1740589755131L),
                new Point(1160.0, 790.0, 1740589756506L)
        };
        double t = 0.88;

        // Compute Bézier curve point at t
        Point result = getCurve(points, t);

        assertEquals(472.9179682134724d, result.getX(), 0);
        assertEquals(206.9757709139601, result.getY(), 0);
        assertEquals(1740589750071L, result.getTime(), 0);
    }
}
