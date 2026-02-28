package com.abs.casino.common.util.string;

import org.junit.Assert;
import org.junit.Test;

public class MatrixUtilsTest {

    @Test
    public void testEquals() {
        int[][] matrix = {{0, 1}, {2, 3}, {4, 5}};
        int[][] matrix1 = {{0, 1}, {2, 3}, {4, 5}};

        Assert.assertTrue(MatrixUtils.equals(matrix, matrix1));

        int[][] matrix3 = {{0, 1}, {2, 3}, {2, 5}};

        Assert.assertFalse(MatrixUtils.equals(matrix, matrix3));

        boolean equalsNull = MatrixUtils.equals(matrix, null);

        Assert.assertFalse(equalsNull);

        boolean equalsItself = MatrixUtils.equals(matrix1, matrix1);

        Assert.assertTrue(equalsItself);
    }

    @Test
    public void cloneTest() {
        int[][] ints = {{0, 1}, {2, 3}, {4, 5}};

        int[][] clone = MatrixUtils.clone(ints);
        boolean equals = MatrixUtils.equals(ints, clone);

        Assert.assertTrue(equals);
    }
    @Test
    public void testJoinMatrix() {
        int[][] ints = {{0, 1}, {2, 3}, {4, 5}};

        String st = MatrixUtils.joinMatrix(ints, ",", "|");

        int[][] parsed = MatrixUtils.parseIntMatrix(st, ",", "\\|");
        Assert.assertTrue(MatrixUtils.equals(parsed, ints));
    }

    @Test
    public void testJoinMatrixTransposed() {
        int[][] matrix = {{0, 1}, {2, 3}, {4, 5}};

        int[][] transposedMatrix = MatrixUtils.transposeMatrix(matrix);
        String st = MatrixUtils.joinMatrix(matrix, ",", "|", true);
        int[][] parsed = MatrixUtils.parseIntMatrix(st, ",", "\\|");

        Assert.assertTrue(MatrixUtils.equals(transposedMatrix, parsed));
    }

    @Test
    public void testSubMatrix() {
        int[][] matrix = { { 0, 1, 2}, {3, 4, 5} };

        int[][] subMatrix = MatrixUtils.subMatrix(matrix, 1, 1, 1, 2);

        Assert.assertEquals(1, subMatrix.length);
        Assert.assertEquals(2, subMatrix[0].length);
        Assert.assertEquals(4, subMatrix[0][0]);
        Assert.assertEquals(5, subMatrix[0][1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectBoundariesSubMatrix() {
        int[][] matrix = { { 0 }, { 1 } };

        int[][] subMatrix = MatrixUtils.subMatrix(matrix, 0, 1, 0, 2);
    }
}
