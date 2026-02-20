package com.betsoft.casino.bots.utils;

import com.betsoft.casino.mp.model.movement.Point;
import com.dgphoenix.casino.common.util.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

import static com.betsoft.casino.bots.utils.GeometryUtils.cosABC;
import static com.betsoft.casino.bots.utils.GeometryUtils.getDistance;

public class BezierCurve {

    private static final Logger LOG = LogManager.getLogger(BezierCurve.class);

    public static double THREE_PI = 3 * Math.PI; //3 - is the step sensitivity

    //Implementation for De Casteljau's Algorithm
    //Originally implemented in SectorX Front End Application

    // Cache for precomputed functions
    private static final Map<Integer, BiFunction<Point[], Double, Point>> cache = new HashMap<>();

    // Interpolate between two points
    public static Point interpolate(Point p1, Point p2, double t) {
        double ut = 1 - t;
        return new Point(p1.getX() * ut + p2.getX() * t, p1.getY() * ut + p2.getY() * t, (long)(p1.getTime() * ut + p2.getTime() * t));
    }

    // Computes a Bézier curve point using De Casteljau’s Algorithm
    public static Point getCurve(Point[] points, double t) {
        int n = points.length;
        Point[] temp = new Point[n];

        // Copy control points
        for (int i = 0; i < n; i++) {
            temp[i] = new Point(points[i].getX(), points[i].getY(), points[i].getTime());
        }

        // De Casteljau’s algorithm
        for (int k = n - 1; k > 0; k--) {
            for (int j = 0; j < k; j++) {
                temp[j] = interpolate(temp[j], temp[j + 1], t);
            }
        }

        return temp[0]; // Final computed Bézier curve point
    }

    static Triple<Point, Double, Double>[] approximatePoints(Triple<Point, Double, Double> aFirstPoint_pt,
                                  Triple<Point, Double, Double> aSecondPoint_pt,
                                  Point[] aPoints, double aAccuracy_bl) {

        List<Triple<Point, Double, Double>> lFinishTrajectoryPoint_arr = new ArrayList<>();

        boolean laccuracyAchieved = accuracyAchieved(aFirstPoint_pt.first(), aSecondPoint_pt.first(), aAccuracy_bl);
        if (laccuracyAchieved) {

            lFinishTrajectoryPoint_arr.add(aSecondPoint_pt);

            return lFinishTrajectoryPoint_arr.toArray(new Triple[0]);
        }

        double lHalfPercent_num = (aFirstPoint_pt.second() + aSecondPoint_pt.second()) / 2;
        Point lPosition = getCurve(aPoints, lHalfPercent_num);

        Triple<Point, Double, Double> lMiddlePoint_pt = new Triple<>(
                new Point(lPosition.getX(), lPosition.getY(), lPosition.getTime()),
                lHalfPercent_num,
                lHalfPercent_num);

        double lCosABC_num = cosABC(aFirstPoint_pt.first(), lMiddlePoint_pt.first(), aSecondPoint_pt.first());
        double lAccuracy_num = Math.ceil(THREE_PI / (Math.PI - Math.acos(lCosABC_num)));

        Triple<Point, Double, Double>[] lNewApproximatePointsPart1 = approximatePoints(aFirstPoint_pt, lMiddlePoint_pt, aPoints, lAccuracy_num);
        Triple<Point, Double, Double>[] lNewApproximatePointsPart2 = approximatePoints(lMiddlePoint_pt, aSecondPoint_pt, aPoints, lAccuracy_num);

        lFinishTrajectoryPoint_arr.addAll(Arrays.asList(lNewApproximatePointsPart1));
        lFinishTrajectoryPoint_arr.addAll(Arrays.asList(lNewApproximatePointsPart2));

        return lFinishTrajectoryPoint_arr.toArray(new Triple[0]);
    }

    public static Triple<Point, Double, Double>[] approximateTrajectory(Point[] points) {
        if( points == null || points.length <= 1) {
            throw new Error("BezierCurve. Incorrect trajectory points for approximate.");
        }

        //position, percent, correctPercent
        Triple<Point, Double, Double>[] lValidatedTrajectoryPoints_arr = new Triple[points.length];
        List<Triple<Point, Double, Double>> lFinishTrajectoryPoint_lst = new ArrayList<>();

        long lFirstTime_num = points[0].getTime();
        long lLastTime_num = points[points.length-1].getTime();
        long lFullTime_num = lLastTime_num - lFirstTime_num;
        boolean lPointTimeCorrect_bl = true;

        for (int i = 0; i < points.length; i++) {//convert time to percent
            double lPercent_num = (points[i].getTime() - lFirstTime_num) / (double)lFullTime_num;
            if (lPercent_num > 1)
            {
                lPointTimeCorrect_bl = false;
                lPercent_num = 1;
            }

            Point lPosition = BezierCurve.getCurve(points, lPercent_num);

            lValidatedTrajectoryPoints_arr[i] = new Triple<>(lPosition, lPercent_num, lPercent_num);
        }

        if (!lPointTimeCorrect_bl) {
            LOG.error("BezierCurve. Incorrect trajectory time for the enemy. The intermediate point time is greater than the last point time.");
        }

        lFinishTrajectoryPoint_lst.add(lValidatedTrajectoryPoints_arr[0]);

        for (int i = 1; i < lValidatedTrajectoryPoints_arr.length; i++) {//approximate points
            Triple<Point, Double, Double> lFirstPoint_pnt = new Triple<>(
                    lValidatedTrajectoryPoints_arr[i-1].first(),
                    lValidatedTrajectoryPoints_arr[i-1].second(),
                    lValidatedTrajectoryPoints_arr[i-1].third());

            Triple<Point, Double, Double> lSecondPoint_pnt = new Triple<>(
                    lValidatedTrajectoryPoints_arr[i].first(),
                    lValidatedTrajectoryPoints_arr[i].second(),
                    lValidatedTrajectoryPoints_arr[i].third());

            Triple<Point, Double, Double>[] newApproximatePoints = approximatePoints(lFirstPoint_pnt, lSecondPoint_pnt, points, getAccuracyPointDefault());
            lFinishTrajectoryPoint_lst.addAll(Arrays.asList(newApproximatePoints));
        }


        Triple<Point, Double, Double>[] lFinishTrajectoryPoint_arr = lFinishTrajectoryPoint_lst.toArray(new Triple[0]);
        double[] segmentLengths = new double[lFinishTrajectoryPoint_arr.length];

        double lFullTrajectoryLength_num = 0;

        for (int i = 1; i < lFinishTrajectoryPoint_arr.length; i++) {//search for the full length of the trajectory
            Triple<Point, Double, Double> lFirstPoint_pnt = new Triple<>(
                    lFinishTrajectoryPoint_arr[i-1].first(),
                    lFinishTrajectoryPoint_arr[i-1].second(),
                    lFinishTrajectoryPoint_arr[i-1].third());

            Triple<Point, Double, Double> lSecondPoint_pnt = new Triple<>(
                    lFinishTrajectoryPoint_arr[i].first(),
                    lFinishTrajectoryPoint_arr[i].second(),
                    lFinishTrajectoryPoint_arr[i].third());

            double segmentLength = getSegmentLength(lFirstPoint_pnt.first(), lSecondPoint_pnt.first());
            segmentLengths[i] = segmentLength;
            lFullTrajectoryLength_num += segmentLength;
        }

        double lCurrentTrajectoryLength_num = 0;

        for (int i = 1; i < lFinishTrajectoryPoint_arr.length; i++) //percent correct
        {
            lCurrentTrajectoryLength_num += segmentLengths[i];
            double lPercentTrajectoryLength = lCurrentTrajectoryLength_num / lFullTrajectoryLength_num;

            Triple<Point, Double, Double> lFinishTrajectoryPoint_pt = new Triple<>(
                    lFinishTrajectoryPoint_arr[i].first(),
                    lFinishTrajectoryPoint_arr[i].second(),
                    lPercentTrajectoryLength
            );

            lFinishTrajectoryPoint_arr[i] = lFinishTrajectoryPoint_pt;
        }

        return lFinishTrajectoryPoint_arr;
    }

    static long getAccuracyPointDefault()
    {
        return 20;
    }

    static boolean accuracyAchieved(Point aFirstPoint_pt_pnt, Point aSecondPoint_pt_pnt, double aAccuracy_bl)
    {
        if (getSegmentLength(aFirstPoint_pt_pnt, aSecondPoint_pt_pnt) > aAccuracy_bl)
        {
            return false;
        }

        return true;
    }

    static double getSegmentLength(Point aFirstPoint_pt_pnt, Point aSecondPoint_pt_pnt)
    {
        return getDistance(aFirstPoint_pt_pnt, aSecondPoint_pt_pnt);
    }
}
