package com.betsoft.casino.bots.utils;

import com.betsoft.casino.mp.model.movement.Point;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtils {
    public static Point getIntersectionBetweenTwoInfiniteLines(Point p1, Point p2, Point p3, Point p4) {

        double d1 = (p1.getX() - p2.getX()) * (p3.getY() - p4.getY());
        double d2 = (p1.getY() - p2.getY()) * (p3.getX() - p4.getX());
        double d  = (d1) - (d2);  //denominator

        if (d == 0) { //lines are parallel or identical
            return null;
        }

        double u1 = (p1.getX() * p2.getY() - p1.getY() * p2.getX());
        double u4 = (p3.getX() * p4.getY() - p3.getY() * p4.getX());

        double u2x = p3.getX() - p4.getX();
        double u3x = p1.getX() - p2.getX();

        double u2y = p3.getY() - p4.getY();
        double u3y = p1.getY() - p2.getY();

        double px = (u1 * u2x - u3x * u4) / d;
        double py = (u1 * u2y - u3y * u4) / d;

        return new Point(px, py, System.currentTimeMillis());
    }

    public static boolean isPointOnSegment(Point p, Point p1, Point p2) {
        return (
                Math.min(p1.getX(), p2.getX()) <= p.getX() && p.getX() <= Math.max(p1.getX(), p2.getX()) &&
                        Math.min(p1.getY(), p2.getY()) <= p.getY() && p.getY() <= Math.max(p1.getY(), p2.getY())
        );
    }

    public static Point getIntersectionBetweenTwoLineSegments(Point p1, Point p2, Point p3, Point p4) {

        Point p = getIntersectionBetweenTwoInfiniteLines(p1, p2, p3, p4);

        if(p == null) {
            return null;
        }

        // Check if intersection point is within the bounds of both line segments
        if (isPointOnSegment(p, p1, p2) && isPointOnSegment(p, p3, p4)) {
            return p; // Intersection lies within both segments
        }

        return null; // Intersection is outside the segment bounds
    }

    public static Double getDistance(Point p1, Point p2) {

        if (p1 == null || p2 == null) {
            return null;
        }

        double xPow2 = Math.pow((p1.getX() - p2.getX()), 2);
        double yPow2 = Math.pow((p1.getY() - p2.getY()), 2);

        return Math.sqrt(xPow2 + yPow2);
    }

    public static Double getDistance(double x1, double y1, double x2, double y2) {

        Point p1 = new Point(x1, y1, 0);
        Point p2 = new Point(x2, y2, 0);

        return getDistance(p1, p2);
    }

    public static List<Point> divideSegment(double x1, double y1, double x2, double y2, int k) {
        Point p1 = new Point(x1, y1, 0);
        Point p2 = new Point(x2, y2, 0);

        return divideSegment(p1, p2, k);
    }

    public static List<Point> divideSegment(Point point1, Point point2, int k) {
        List<Point> points = new ArrayList<>();

        if(k > 0) {
            double xOffset = (point2.getX() - point1.getX()) / k;
            double yOffset = (point2.getY() - point1.getY()) / k;
            long timeOffset = (point2.getTime() - point1.getTime()) / k;

            for (int i = 1; i < k; i++) {//excluding end points
                double x = point1.getX() + i * xOffset;
                double y = point1.getY() + i * yOffset;
                long time = point1.getTime() + i * timeOffset;

                Point point = new Point(x, y, time);

                points.add(point);
            }
        }

        return points;
    }

    public static boolean isPointInsideOfRectangle(Point point, double leftTopCornerX, double leftTopCornerY, double width, double height) {
        if(point == null) {
            return false;
        }

        return     point.getX() >= leftTopCornerX && point.getX() <= leftTopCornerX + width
                && point.getY() >= leftTopCornerY && point.getY() <= leftTopCornerY + height;
    }

    public static boolean isPointInsideOfRectangle(Point point, Point leftTopCorner, double width, double height) {
        if(point == null || leftTopCorner == null) {
            return false;
        }

        return isPointInsideOfRectangle(point, leftTopCorner.getX(), leftTopCorner.getY(), width, height);
    }

    public static boolean isLineSegmentIntersectingRectangle(Point segmentPoint1, Point segmentPoint2, Point leftTopCorner, double width, double height) {

        if(leftTopCorner == null) {
            return false;
        }

        if(segmentPoint1 == null) {
            return isPointInsideOfRectangle(segmentPoint2, leftTopCorner, width, height);
        }

        if(segmentPoint2 == null) {
            return isPointInsideOfRectangle(segmentPoint1, leftTopCorner, width, height);
        }

        if(isPointInsideOfRectangle(segmentPoint1, leftTopCorner, width, height)) {
            return true;
        }

        if(isPointInsideOfRectangle(segmentPoint2, leftTopCorner, width, height)) {
            return true;
        }

        Point rightTopCorner = new Point(leftTopCorner.getX() + width, leftTopCorner.getY(), leftTopCorner.getTime());
        if(getIntersectionBetweenTwoLineSegments(segmentPoint1, segmentPoint2, leftTopCorner, rightTopCorner) != null) {
            return true;
        }

        Point rightBottomCorner = new Point(leftTopCorner.getX() + width, leftTopCorner.getY()+ height, leftTopCorner.getTime());
        if(getIntersectionBetweenTwoLineSegments(segmentPoint1, segmentPoint2, rightTopCorner, rightBottomCorner) != null) {
            return true;
        }

        Point leftBottomCorner = new Point(leftTopCorner.getX(), leftTopCorner.getY()+ height, leftTopCorner.getTime());
        if(getIntersectionBetweenTwoLineSegments(segmentPoint1, segmentPoint2, rightBottomCorner, leftBottomCorner) != null) {
            return true;
        }

        return getIntersectionBetweenTwoLineSegments(segmentPoint1, segmentPoint2, leftBottomCorner, leftTopCorner) != null;
    }

    public static boolean isPointInsideOfTriangle(Point p, Point a, Point b, Point c) {

        double px = p.getX(), py = p.getY();
        double ax = a.getX(), ay = a.getY();
        double bx = b.getX(), by = b.getY();
        double cx = c.getX(), cy = c.getY();

        double areaOrig = Math.abs((ax * (by - cy) + bx * (cy - ay) + cx * (ay - by)) / 2.0);
        double area1 = Math.abs((px * (by - cy) + bx * (cy - py) + cx * (py - by)) / 2.0);
        double area2 = Math.abs((ax * (py - cy) + px * (cy - ay) + cx * (ay - py)) / 2.0);
        double area3 = Math.abs((ax * (by - py) + bx * (py - ay) + px * (ay - by)) / 2.0);
        return Math.abs(areaOrig - (area1 + area2 + area3)) < 1e-9;
    }

    public static boolean isPointInsideOfQuadrilateral(Point p, Point p1, Point p2, Point p3, Point p4) {
        return isPointInsideOfTriangle(p, p1, p2, p3) || isPointInsideOfTriangle(p, p1, p3, p4);
    }

    public static boolean isQuadrilateralIntersectingRectangle(Point p1, Point p2, Point p3, Point p4, Point leftTopCorner, double width, double height) {

        if(isLineSegmentIntersectingRectangle(p1, p2, leftTopCorner, width, height)) {
            return true;
        }

        if(isLineSegmentIntersectingRectangle(p2, p3, leftTopCorner, width, height)) {
            return true;
        }

        if(isLineSegmentIntersectingRectangle(p3, p4, leftTopCorner, width, height)) {
            return true;
        }

        if(isLineSegmentIntersectingRectangle(p4, p1, leftTopCorner, width, height)) {
            return true;
        }

        return isPointInsideOfQuadrilateral(leftTopCorner, p1, p2, p3 ,p4);
    }

    public static boolean isPointInsideOfCircle(Point point, Point circleCenter, double circleRadius) {
        //Point is Inside the Circle if it satisfies equation: (x−cx)^2+(y−cy)^2 ≤r^2
        double dx = point.getX() - circleCenter.getX();
        double dy = point.getY() - circleCenter.getY();
        return (dx * dx + dy * dy) <= (circleRadius * circleRadius);
    }

    public static boolean isPointInsideOfEllipse(Point point, Point ellipseCenter, double width, double height) {
        //Point is Inside the Ellipse if it satisfies equation: ((x-ex)^2 / width^2 + (y-ey)^2 / height^2) <= 1;

        if(point == null || ellipseCenter == null || width == 0 || height == 0) {
            return false;
        }
        double x = point.getX();
        double y = point.getY();
        double ex = ellipseCenter.getX();
        double ey = ellipseCenter.getY();

        double xPow2 = Math.pow((x - ex), 2);
        double yPow2 = Math.pow((y - ey), 2);
        double wPow2 = Math.pow(width, 2);
        double hPow2 = Math.pow(height, 2);

        return (xPow2 / wPow2 + yPow2 / hPow2) <= 1;
    }

    public static boolean isLineSegmentFullyInsideCircle(Point segmentPoint1, Point segmentPoint2, Point circleCenter, double circleRadius) {
        return isPointInsideOfCircle(segmentPoint1, circleCenter, circleRadius)
                && isPointInsideOfCircle(segmentPoint2, circleCenter, circleRadius);
    }

    public static boolean isLineSegmentPartiallyInsideCircle(Point segmentPoint1, Point segmentPoint2, Point circleCenter, double circleRadius) {
        return isPointInsideOfCircle(segmentPoint1, circleCenter, circleRadius)
                || isPointInsideOfCircle(segmentPoint2, circleCenter, circleRadius);
    }

    public static Point[] getIntersectionBetweenLineSegmentAndCircle(Point segmentPoint1, Point segmentPoint2, Point circleCenter, double circleRadius) {

        List<Point> intersections = new ArrayList<>();
        double x1 = segmentPoint1.getX(), y1 = segmentPoint1.getY();
        double x2 = segmentPoint2.getX(), y2 = segmentPoint2.getY();
        double cx = circleCenter.getX(), cy = circleCenter.getY();

        // Line segment direction
        double dx = x2 - x1;
        double dy = y2 - y1;

        // Quadratic coefficients
        double a = dx * dx + dy * dy;
        double b = 2 * (dx * (x1 - cx) + dy * (y1 - cy));
        double c = (x1 - cx) * (x1 - cx) + (y1 - cy) * (y1 - cy) - circleRadius * circleRadius;

        // Solve quadratic equation a*t^2 + b*t + c = 0
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            // No real solutions, no intersection
            return new Point[0];
        }

        // Compute possible values of t
        double sqrtD = Math.sqrt(discriminant);
        double t1 = (-b - sqrtD) / (2 * a);
        double t2 = (-b + sqrtD) / (2 * a);

        // Check if intersections are within the segment
        if (t1 >= 0 && t1 <= 1) {
            intersections.add(new Point(x1 + t1 * dx, y1 + t1 * dy, System.currentTimeMillis()));
        }
        if (t2 >= 0 && t2 <= 1 && discriminant > 0) {
            intersections.add(new Point(x1 + t2 * dx, y1 + t2 * dy, System.currentTimeMillis()));
        }

        // Convert List to Array
        return intersections.toArray(new Point[0]);
    }

    static double cosABC(Point pointA, Point pointB, Point pointC)
    {
        Point vectorAB = new Point(pointB.getX()-pointA.getX(), pointB.getY()-pointA.getY(), 0);
        Point vectorCB = new Point(pointB.getX()-pointC.getX(), pointB.getY()-pointC.getY(), 0);

        double abc = vectorAB.getX() * vectorCB.getX() + vectorAB.getY() * vectorCB.getY();
        double ab = Math.sqrt(vectorAB.getX() * vectorAB.getX() + vectorAB.getY() * vectorAB.getY());
        double cb = Math.sqrt(vectorCB.getX() * vectorCB.getX() + vectorCB.getY() * vectorCB.getY());

        return abc /( ab * cb);
    }
}
