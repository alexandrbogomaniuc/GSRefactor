package com.betsoft.casino.bots.utils;

import com.betsoft.casino.mp.model.movement.Point;

import static com.betsoft.casino.bots.utils.GeometryUtils.divideSegment;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class GeometryUtilsTest {

    @Test
    public void getIntersectionBetweenTwoInfiniteLines_LinesAreParallel_Test() throws Exception {

        Point line1p1 = new Point(10, 10, 0);
        Point line1p2 = new Point(20, 20, 0);

        Point line2p1 = new Point(15, 15, 0);
        Point line2p2 = new Point(25, 25, 0);

        Point intersection = GeometryUtils.getIntersectionBetweenTwoInfiniteLines(line1p1, line1p2, line2p1, line2p2);

        assertNull(intersection);
    }

    @Test
    public void getIntersectionBetweenTwoInfiniteLines_IntersectionExists1_Test() throws Exception {

        Point line1p1 = new Point(10, 10, 0);
        Point line1p2 = new Point(20, 20, 0);

        Point line2p1 = new Point(10, 20, 0);
        Point line2p2 = new Point(20, 10, 0);

        Point intersection = GeometryUtils.getIntersectionBetweenTwoInfiniteLines(line1p1, line1p2, line2p1, line2p2);

        assertNotNull(intersection);
        assertEquals(15d, intersection.getX(), 0);
        assertEquals(15d, intersection.getY(), 0);
    }

    @Test
    public void getIntersectionBetweenTwoInfiniteLines_IntersectionExists2_Test() throws Exception {

        Point line1p1 = new Point(10, 10, 0);
        Point line1p2 = new Point(20, 20, 0);

        Point line2p1 = new Point(10, 20, 0);
        Point line2p2 = new Point(14, 16, 0);

        Point intersection = GeometryUtils.getIntersectionBetweenTwoInfiniteLines(line1p1, line1p2, line2p1, line2p2);

        assertNotNull(intersection);
        assertEquals(15d, intersection.getX(), 0);
        assertEquals(15d, intersection.getY(), 0);
    }

    @Test
    public void getIntersectionBetweenTwoLineSegments_LinesAreParallel_Test() throws Exception {

        Point line1p1 = new Point(10, 10, 0);
        Point line1p2 = new Point(20, 20, 0);

        Point line2p1 = new Point(15, 15, 0);
        Point line2p2 = new Point(25, 25, 0);

        Point intersection = GeometryUtils.getIntersectionBetweenTwoLineSegments(line1p1, line1p2, line2p1, line2p2);

        assertNull(intersection);
    }

    @Test
    public void getIntersectionBetweenTwoLineSegments_IntersectionExists_Test() throws Exception {

        Point line1p1 = new Point(10, 10, 0);
        Point line1p2 = new Point(20, 20, 0);

        Point line2p1 = new Point(10, 20, 0);
        Point line2p2 = new Point(20, 10, 0);

        Point intersection = GeometryUtils.getIntersectionBetweenTwoLineSegments(line1p1, line1p2, line2p1, line2p2);

        assertNotNull(intersection);
        assertEquals(15d, intersection.getX(), 0);
        assertEquals(15d, intersection.getY(), 0);
    }

    @Test
    public void getIntersectionBetweenTwoLineSegments_IntersectionDoesNotExist_Test() throws Exception {

        Point line1p1 = new Point(10, 10, 0);
        Point line1p2 = new Point(20, 20, 0);

        Point line2p1 = new Point(10, 20, 0);
        Point line2p2 = new Point(14, 16, 0);

        Point intersection = GeometryUtils.getIntersectionBetweenTwoLineSegments(line1p1, line1p2, line2p1, line2p2);

        assertNull(intersection);
    }

    @Test
    public void getDistance_Test() throws Exception {

        Point p1 = new Point(10, 10, 0);
        Point p2 = new Point(10, 30, 0);
        Point p3 = new Point(30, 10, 0);
        Point p4 = new Point(30, 30, 0);

        double distance1 = GeometryUtils.getDistance(p1, p2);
        double distance2 = GeometryUtils.getDistance(p1, p3);
        double distance3 = GeometryUtils.getDistance(p1, p4);

        assertEquals(20d, distance1, 0);
        assertEquals(20d, distance2, 0);
        assertEquals(28.284271247461902d, distance3, 0);
    }

    @Test
    public void divideSegment_Test() {
        Point p1 = new Point(10, 10, 0);
        Point p2 = new Point(10, 30, 0);
        Point p4 = new Point(30, 30, 0);

        List<Point> points1 = divideSegment(p1, p2, 4);
        List<Point> points2 = divideSegment(p4, p2, 6);


        assertEquals(3, points1.size());
        assertEquals(5, points2.size());
    }

    @Test
    public void isPointInsideOfRectangle_CheckTrueAndFalseTest() throws Exception {

        Point p1 = new Point(9, 9, 0);
        Point p2 = new Point(12, 13, 0);
        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point leftTopCorner = new Point(9, 9, 0);
        double width = 3;
        double height = 4;

        boolean isPointInsideRectangle1 = GeometryUtils.isPointInsideOfRectangle(p1, leftTopCorner, width, height);
        boolean isPointInsideRectangle2 = GeometryUtils.isPointInsideOfRectangle(p2, leftTopCorner, width, height);
        boolean isPointInsideRectangle3 = GeometryUtils.isPointInsideOfRectangle(segmentP1, leftTopCorner, width, height);
        boolean isPointInsideRectangle4 = GeometryUtils.isPointInsideOfRectangle(segmentP2, leftTopCorner, width, height);

        assertTrue(isPointInsideRectangle1);
        assertTrue(isPointInsideRectangle2);
        assertTrue(isPointInsideRectangle3);
        assertFalse(isPointInsideRectangle4);
    }

    @Test
    public void isLineSegmentIntersectingRectangle_CheckTrueAndFalseTest() throws Exception {

        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point segmentP3 = new Point(8, 8, 0);
        Point segmentP4 = new Point(8, 20, 0);
        Point leftTopCorner = new Point(9, 9, 0);
        double width = 3;
        double height = 4;

        boolean isLineSegmentIntersectingRectangle1 = GeometryUtils.isLineSegmentIntersectingRectangle(segmentP1, segmentP2, leftTopCorner, width, height);
        boolean isLineSegmentIntersectingRectangle2 = GeometryUtils.isLineSegmentIntersectingRectangle(segmentP3, segmentP4, leftTopCorner, width, height);

        assertTrue(isLineSegmentIntersectingRectangle1);
        assertFalse(isLineSegmentIntersectingRectangle2);
    }

    @Test
    public void isPointInsideOfTriangle_CheckTrueAndFalseTest() throws Exception {

        Point p1 = new Point(10, 10, 0);
        Point p2 = new Point(3, 10, 0);
        Point a = new Point(5, 6, 0);
        Point b = new Point(10, 11, 0);
        Point c = new Point(11, 9, 0);

        boolean isPointInsideTriangle1 = GeometryUtils.isPointInsideOfTriangle(p1, a, b, c);
        boolean isPointInsideTriangle2 = GeometryUtils.isPointInsideOfTriangle(p2, a, b, c);

        assertTrue(isPointInsideTriangle1);
        assertFalse(isPointInsideTriangle2);
    }

    @Test
    public void isPointInsideOfQuadrilateral_CheckTrueAndFalseTest() throws Exception {

        Point p1 = new Point(10, 10, 0);
        Point p2 = new Point(3, 10, 0);
        Point a = new Point(5, 6, 0);
        Point b = new Point(10, 11, 0);
        Point c = new Point(11, 9, 0);
        Point d = new Point(10, 1, 0);

        boolean isPointInsideQuadrilateral1 = GeometryUtils.isPointInsideOfQuadrilateral(p1, a, b, c, d);
        boolean isPointInsideQuadrilateral2 = GeometryUtils.isPointInsideOfQuadrilateral(p2, a, b, c, d);

        assertTrue(isPointInsideQuadrilateral1);
        assertFalse(isPointInsideQuadrilateral2);
    }

    @Test
    public void isQuadrilateralIntersectingRectangle_CheckTrueAndFalseTest() throws Exception {

        //QuadrilateralIntersectingRectangle
        Point q1p1 = new Point(10, 11, 0);
        Point q1p2 = new Point(14, 25, 0);
        Point q1p3 = new Point(20, 20, 0);
        Point q1p4 = new Point(5, 16, 0);

        //QuadrilateralInsideOfRectangle
        Point q2p1 = new Point(11, 15, 0);
        Point q2p2 = new Point(12, 17, 0);
        Point q2p3 = new Point(14, 12, 0);
        Point q2p4 = new Point(12, 11, 0);

        //QuadrilateralOutsideSurroundingRectangle
        Point q3p1 = new Point(0, 10, 0);
        Point q3p2 = new Point(12, 120, 0);
        Point q3p3 = new Point(20, 11, 0);
        Point q3p4 = new Point(13, 0, 0);

        //QuadrilateralOutsideOfRectangle
        Point q4p1 = new Point(20, 15, 0);
        Point q4p2 = new Point(32, 120, 0);
        Point q4p3 = new Point(40, 16, 0);
        Point q4p4 = new Point(33, -100, 0);

        Point leftTopCorner = new Point(10, 10, 0);
        double width = 5;
        double height = 10;

        boolean isQuadrilateralIntersectingRectangle1 = GeometryUtils.isQuadrilateralIntersectingRectangle(q1p1, q1p2, q1p3, q1p4, leftTopCorner, width, height);
        boolean isQuadrilateralIntersectingRectangle2 = GeometryUtils.isQuadrilateralIntersectingRectangle(q2p1, q2p2, q2p3, q2p4, leftTopCorner, width, height);
        boolean isQuadrilateralIntersectingRectangle3 = GeometryUtils.isQuadrilateralIntersectingRectangle(q3p1, q3p2, q3p3, q3p4, leftTopCorner, width, height);
        boolean isQuadrilateralIntersectingRectangle4 = GeometryUtils.isQuadrilateralIntersectingRectangle(q4p1, q4p2, q4p3, q4p4, leftTopCorner, width, height);

        assertTrue(isQuadrilateralIntersectingRectangle1);
        assertTrue(isQuadrilateralIntersectingRectangle2);
        assertTrue(isQuadrilateralIntersectingRectangle3);
        assertFalse(isQuadrilateralIntersectingRectangle4);
    }

    @Test
    public void isPointInsideOfCircle_CheckTrueAndFalseTest() throws Exception {

        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point circleCentre = new Point(10, 11, 0);
        double circeRadius = 1;

        boolean isPointInsideCircle1 = GeometryUtils.isPointInsideOfCircle(segmentP1, circleCentre, circeRadius);
        boolean isPointInsideCircle2 = GeometryUtils.isPointInsideOfCircle(segmentP2, circleCentre, circeRadius);

        assertTrue(isPointInsideCircle1);
        assertFalse(isPointInsideCircle2);
    }

    @Test
    public void isPointInsideOfEllipse_CheckTrueAndFalseTest() throws Exception {

        Point p1 = new Point(10, 10, 0);
        Point p2 = new Point(10, 30, 0);
        Point ellipseCentre = new Point(10, 11, 0);
        double width = 1;
        double height = 3;

        boolean isPointInsideEllipse1 = GeometryUtils.isPointInsideOfEllipse(p1, ellipseCentre, width, height);
        boolean isPointInsideEllipse2 = GeometryUtils.isPointInsideOfEllipse(p2, ellipseCentre, width, height);

        assertTrue(isPointInsideEllipse1);
        assertFalse(isPointInsideEllipse2);
    }

    @Test
    public void isLineSegmentPartiallyInsideCircle_CheckTrueAndFalseTest() throws Exception {

        Point segment0P1 = new Point(10, 10, 0);
        Point segment0P2 = new Point(10, 12, 0);
        Point segment1P1 = new Point(10, 10, 0);
        Point segment1P2 = new Point(10, 30, 0);
        Point segment2P1 = new Point(20, 10, 0);
        Point segment2P2 = new Point(20, 30, 0);
        Point circleCentre = new Point(10, 11, 0);
        double circeRadius = 1;

        boolean isLineSegmentPartiallyInsideCircle0 = GeometryUtils.isLineSegmentPartiallyInsideCircle(segment0P1, segment0P2, circleCentre, circeRadius);
        boolean isLineSegmentPartiallyInsideCircle1 = GeometryUtils.isLineSegmentPartiallyInsideCircle(segment1P1, segment1P2, circleCentre, circeRadius);
        boolean isLineSegmentPartiallyInsideCircle2 = GeometryUtils.isLineSegmentPartiallyInsideCircle(segment2P1, segment2P2, circleCentre, circeRadius);

        assertTrue(isLineSegmentPartiallyInsideCircle0);
        assertTrue(isLineSegmentPartiallyInsideCircle1);
        assertFalse(isLineSegmentPartiallyInsideCircle2);
    }

    @Test
    public void isLineSegmentFullyInsideCircle_CheckTrueAndFalseTest() throws Exception {

        Point segment0P1 = new Point(10, 10, 0);
        Point segment0P2 = new Point(10, 12, 0);
        Point segment1P1 = new Point(10, 10, 0);
        Point segment1P2 = new Point(10, 30, 0);
        Point segment2P1 = new Point(20, 10, 0);
        Point segment2P2 = new Point(20, 30, 0);
        Point circleCentre = new Point(10, 11, 0);
        double circeRadius = 1;

        boolean isLineSegmentPartiallyInsideCircle0 = GeometryUtils.isLineSegmentFullyInsideCircle(segment0P1, segment0P2, circleCentre, circeRadius);
        boolean isLineSegmentPartiallyInsideCircle1 = GeometryUtils.isLineSegmentFullyInsideCircle(segment1P1, segment1P2, circleCentre, circeRadius);
        boolean isLineSegmentPartiallyInsideCircle2 = GeometryUtils.isLineSegmentFullyInsideCircle(segment2P1, segment2P2, circleCentre, circeRadius);

        assertTrue(isLineSegmentPartiallyInsideCircle0);
        assertFalse(isLineSegmentPartiallyInsideCircle1);
        assertFalse(isLineSegmentPartiallyInsideCircle2);
    }

    @Test
    public void getIntersectionBetweenLineSegmentAndCircle_NoIntersection_SegmentIsOutsideOfCircle_Test() throws Exception {

        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point circleCentre = new Point(50, 50, 0);
        double circeRadius = 1;

        Point[] intersections = GeometryUtils.getIntersectionBetweenLineSegmentAndCircle(segmentP1, segmentP2, circleCentre, circeRadius);

        assertNotNull(intersections);
        assertEquals(0, intersections.length);
    }

    @Test
    public void getIntersectionBetweenLineSegmentAndCircle_NoIntersection_SegmentIsInsideOfCircle_Test() throws Exception {

        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point circleCentre = new Point(50, 50, 0);
        double circeRadius = 100;

        Point[] intersections = GeometryUtils.getIntersectionBetweenLineSegmentAndCircle(segmentP1, segmentP2, circleCentre, circeRadius);

        assertNotNull(intersections);
        assertEquals(0, intersections.length);
    }

    @Test
    public void getIntersectionBetweenLineSegmentAndCircle_OneIntersection_SegmentIsTangentOfCircle_Test() throws Exception {

        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point circleCentre = new Point(15, 15, 0);
        double circeRadius = 5;

        Point[] intersections = GeometryUtils.getIntersectionBetweenLineSegmentAndCircle(segmentP1, segmentP2, circleCentre, circeRadius);

        assertNotNull(intersections);
        assertEquals(1, intersections.length);
        assertEquals(10d, intersections[0].getX(), 0);
        assertEquals(15d, intersections[0].getY(), 0);
    }

    @Test
    public void getIntersectionBetweenLineSegmentAndCircle_OneIntersection_SegmentP1InsideAndIsSegmentP2OutsideOfCircle_Test() throws Exception {

        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point circleCentre = new Point(10, 10, 0);
        double circeRadius = 10;

        Point[] intersections = GeometryUtils.getIntersectionBetweenLineSegmentAndCircle(segmentP1, segmentP2, circleCentre, circeRadius);

        assertNotNull(intersections);
        assertEquals(1, intersections.length);
        assertEquals(10d, intersections[0].getX(), 0);
        assertEquals(20d, intersections[0].getY(), 0);
    }

    @Test
    public void getIntersectionBetweenLineSegmentAndCircle_TwoIntersections_SegmentPointsOutsideOfCircle_Test() throws Exception {

        Point segmentP1 = new Point(10, 10, 0);
        Point segmentP2 = new Point(10, 30, 0);
        Point circleCentre = new Point(10, 20, 0);
        double circeRadius = 1;

        Point[] intersections = GeometryUtils.getIntersectionBetweenLineSegmentAndCircle(segmentP1, segmentP2, circleCentre, circeRadius);

        assertNotNull(intersections);
        assertEquals(2, intersections.length);
        assertEquals(10d, intersections[0].getX(), 0);
        assertEquals(19d, intersections[0].getY(), 0);
        assertEquals(10d, intersections[1].getX(), 0);
        assertEquals(21d, intersections[1].getY(), 0);
    }
}
