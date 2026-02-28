package com.abs.casino.common.util.string;

import com.abs.casino.common.util.string.CommonArrayUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;

/**
 * Operations in the class works tested only for rectangular matrixes
 */
public class MatrixUtils {
    public static int[][] subMatrix(int[][] arr, int posX, int lengthX, int posY, int lengthY) {
        if (posX + lengthX > arr.length && posY + lengthY > arr[0].length) {
            throw new IllegalArgumentException("Matrix doesn't fit to position");
        }
        int[][] res = new int[lengthX][];
        for (int i=0; i<lengthX; i++) {
            res[i] = CommonArrayUtils.subArray(arr[posX + i], posY, lengthY);
        }
        return res;
    }

    public static String joinMatrix(int[][] matrix, String separator, String lineSeparator) {
        return joinMatrix(matrix, separator,lineSeparator, false);
    }

    public static String joinMatrix(int[][] matrix, String separator, String lineSeparator, boolean transpose) {
        StringBuilder result = new StringBuilder();
        int xSize = transpose ? matrix[0].length : matrix.length;
        int ySize = transpose ? matrix.length : matrix[0].length;
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                int val = !transpose ? matrix[i][j] : matrix[j][i];
                result.append(val);
                if (j + 1 != ySize) {
                    result.append(separator);
                }
            }
            if (i + 1 != xSize) {
                result.append(lineSeparator);
            }
        }
        return result.toString();
    }

    public static int[][] transposeMatrix(int[][] matrix) {
        int[][] res = new int[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                res[j][i] = matrix[i][j];
            }
        }
        return res;
    }

    public static boolean equals(int[][] matrix1, int[][] matrix2) {
        if (matrix1 == matrix2)
            return true;
        if (matrix1 == null || matrix2 == null)
            return false;

        int length = matrix1.length;
        if (matrix2.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            int[] o1 = matrix1[i];
            int[] o2 = matrix2[i];
            if (!(o1 == null ? o2 == null : Arrays.equals(o1, o2)))
                return false;
        }

        return true;
    }

    public static int[][] clone(int[][] matrix) {
        int[][] res = new int[matrix.length][];
        for (int i = 0; i < res.length; i++) {
            res[i] = ArrayUtils.clone(matrix[i]);
        }
        return res;
    }

    public static int[][] parseIntMatrix(String input, String separator, String lineSeparator) {
        String[] lines = input.split(lineSeparator);
        int[][] result = new int[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            result[i] = CommonArrayUtils.parseIntArray(lines[i], separator);
        }
        return result;
    }

    private MatrixUtils() {

    }
}
