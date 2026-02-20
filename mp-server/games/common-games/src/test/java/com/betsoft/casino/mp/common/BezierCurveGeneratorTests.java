package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.movement.generators.BezierCurveGenerator;

import java.util.List;

public class BezierCurveGeneratorTests {
    public static void main(String[] args) {
        BezierCurveGenerator bezierCurveGenerator = new BezierCurveGenerator(960, 540, 50, 30);

//        for (int i = 0; i < 100000; i++) {
//            Trajectory trajectory = bezierCurveGenerator.generateQuadratic();
//            bezierCurveGenerator
//            System.out.println(trajectory.getPoints());
//        }
    }
}
