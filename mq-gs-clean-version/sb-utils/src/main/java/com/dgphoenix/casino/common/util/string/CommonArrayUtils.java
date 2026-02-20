package com.dgphoenix.casino.common.util.string;

import com.dgphoenix.casino.common.util.Pair;
import java.util.*;

public class CommonArrayUtils {
    private CommonArrayUtils() {
    }

    public static int equalCount(boolean[] arr, boolean val) {
        int res = 0;
        for (boolean b : arr) {
            res += b == val ? 1 : 0;
        }
        return res;
    }

    public static <T> int equalCount(T[] arr, T val) {
        int res = 0;
        for (T b : arr) {
            res += val.equals(b) ? 1 : 0;
        }
        return res;
    }

    public static int[][] subMatrix(int[][] arr, int posX, int lengthX, int posY, int lengthY) {
        if (posX + lengthX > arr.length && posY + lengthY > arr[0].length) {
            throw new IllegalArgumentException("Matrix doesn't fit to position");
        }
        int[][] res = new int[lengthX][];
        for (int i=0; i<lengthX; i++) {
            res[i] = subArray(arr[posX + i], posY, lengthY);
        }
        return res;
    }

    public static int[] subArray(int[] arr, int pos, int length) {
        if (pos + length > arr.length) {
            throw new IllegalArgumentException("Array doesn't fit to position");
        }
        int[] res = new int[length];
        for (int i=0; i<length; i++) {
            res[i] = arr[pos + i];
        }
        return res;
    }

    public static <T> void insertInto(T[] source, T[] dest, int pos) {
        if (pos + dest.length > source.length) {
            throw new IllegalArgumentException("Array doesn't fit to position");
        }
        for (int i = 0; i < dest.length; i++) {
            source[pos + i] = dest[i];
        }
    }

    public static double[] toDoubleArray(Collection<Double> collection) {
        double[] arr = new double[collection.size()];
        Iterator<Double> iterator = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            arr[i] = iterator.next();
        }
        return arr;
    }

    public static int[] toIntArray(Collection<Integer> collection) {
        int[] arr = new int[collection.size()];
        Iterator<Integer> iterator = collection.iterator();
        for (int i = 0; i < collection.size(); i++) {
            arr[i] = iterator.next();
        }
        return arr;
    }

    public static void addAll(Collection<Double> collection, double[] arr) {
        for (double item : arr) {
            collection.add(item);
        }
    }

    public static <T> T[] setAllTo(T[] arr, T val) {
        for (int i=0; i < arr.length; i++) {
            arr[i] = val;
        }
        return arr;
    }

    public static boolean equals(int[] arr1, int[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int[] range(int begin, int end) {
        int length = end - begin;
        int[] res = new int[length];
        for (int i = 0; i < length; i++) {
            res[i] = begin + i;
        }
        return res;
    }

    public static int nonzeroCount(int[] arr) {
        int count = 0;
        for (int el : arr) {
            if (el != 0) {
                count++;
            }
        }
        return count;
    }

    public static int indexOf(int[] arr, int val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == val) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(double[] arr, double val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == val) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int indexOf(T[] arr, T val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfIgnoreCase(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equalsIgnoreCase(val)) {
                return i;
            }
        }
        return -1;
    }

    public static int countOf(int[] arr, int val) {
        int count = 0;
        for (int el : arr) {
            if (el == val) {
                count++;
            }
        }
        return count;
    }

    public static boolean areAllEquals(int[] array, int val) {
        for (int el : array) {
            if (el != val) {
                return false;
            }
        }
        return true;
    }

    public static boolean areAllEquals(boolean[] array, boolean val) {
        for (boolean el : array) {
            if (el != val) {
                return false;
            }
        }
        return true;
    }

    public static int[] getZeroArray(int size) {
        return getArrayOf(size, 0);
    }

    public static int[] getArrayOf(int size, int val) {
        int[] result = new int[size];
        Arrays.fill(result, val);
        return result;
    }

    public static boolean[] getArrayOf(int size, boolean val) {
        boolean[] result = new boolean[size];
        Arrays.fill(result, val);
        return result;
    }

    public static int sum(int[] array) {
        int result = 0;
        for (int val : array) {
            result += val;
        }
        return result;
    }

    public static boolean[] sum(boolean[] arr1, boolean[] arr2) {
        boolean[] result = getArrayOf(arr1.length, false);
        for (int i = 0; i < arr1.length; i++) {
            result[i] = (arr1[i] || arr2[i]);
        }
        return result;
    }

    public static int product(int[] array, boolean excludeZero) {
        int result = 1;
        for (int val : array) {
            if (!excludeZero || val != 0) {
                result *= val;
            }
        }
        return result;
    }

    public static double sum(double[] array) {
        double result = 0;
        for (double val : array) {
            result += val;
        }
        return result;
    }

    public static <T> String join(Collection<T> list, String separator) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    @Deprecated
    public static <T> String joinMatrix(T[][] stopReel, String separator, String lineSeparator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < stopReel[0].length; i++) {
            for (int j = 0; j < stopReel.length; j++) {
                result.append(stopReel[j][i]).append(separator);
            }
            result.delete(result.length() - separator.length(),result.length()).append(lineSeparator);
        }
        result.delete(result.length() - lineSeparator.length(),result.length());
        return result.toString();
    }

    @Deprecated
    public static String joinMatrix(int[][] matrix, String separator, String lineSeparator) {
        return joinMatrix(matrix, separator,lineSeparator, false);
    }

    @Deprecated
    public static String joinMatrix(int[][] matrix, String separator, String lineSeparator, boolean transpose) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int val;
                if (!transpose) {
                    val = matrix[i][j];
                } else {
                    val = matrix[j][i];
                }
                result.append(val);
                if (j + 1 != matrix[i].length) {
                    result.append(separator);
                }
            }
            if (i + 1 != matrix.length) {
                result.append(lineSeparator);
            }
        }
        return result.toString();
    }

    public static String joinArray(double[] array, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            result.append(array[i]);
            if (i + 1 != array.length) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    public static String joinArray(boolean[] array, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            result.append(array[i]);
            if (i + 1 != array.length) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    public static String joinArray(int[] array, String separator) {
        return joinArray(array, separator, 0, array.length);
    }

    public static String joinArray(int[] array, String separator, int begin, int end) {
        StringBuilder result = new StringBuilder();
        if ((end - begin) <= 0) {
            return result.toString();
        }
        for (int i = begin; i < end; i++) {
            result.append(array[i]);
            if (i + 1 != end) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    public static String joinArray(int[] array, String separator, int begin) {
        return joinArray(array, separator, begin, array.length);
    }

    public static <T> List<T> toList(T[] arr) {
        List<T> res = new ArrayList<T>(arr.length);
        for (T t : arr) {
            res.add(t);
        }
        return res;
    }

    public static int[][] parseIntMatrix(String input, String separator, String lineSeparator) {
        String[] lines = input.split(lineSeparator);
        int[][] result = new int[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            result[i] = parseIntArray(lines[i], separator);
        }
        return result;
    }

    public static int[] parseIntArray(String input, String separator) {
        String[] lines = input.split(separator);
        int[] result = new int[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = Integer.parseInt(lines[i]);
        }
        return result;
    }

    public static boolean[] parseBoolArray(String input, String separator) {
        StringTokenizer tokenizer = new StringTokenizer(input, separator);
        int len = tokenizer.countTokens();
        boolean[] result = new boolean[len];
        int index = 0;
        while (tokenizer.hasMoreElements()) {
            if (Integer.parseInt(tokenizer.nextToken()) == 0) {
                result[index++] = false;
            } else {
                result[index++] = true;
            }
        }
        return result;
    }

    @Deprecated
    public static <K, V> String stringifyMap(Map<K, V> map, String separator) {
        return stringifyMap(map, separator, "=");
    }

    @Deprecated
    public static <K, V> String stringifyMap(Map<K, V> map, String separator, String valueSeparator) {
        if (map.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            result.append(entry.getKey()).append(valueSeparator).append(entry.getValue());
            if (it.hasNext()) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    public static Map<Integer, String> parseIntegerStringMap(String raw, String separator) {
        Map<Integer, String> result = new HashMap<Integer, String>();
        for (String el : raw.split(separator)) {
            Pair<Integer, String> pair = parseIntegerStringPair(el);
            result.put(pair.getKey(), pair.getValue());
        }
        return result;
    }

    public static Pair<Integer, String> parseIntegerStringPair(String raw) {
        Pair<Integer, String> result = new Pair<Integer, String>(-1, null);
        String[] parts = raw.split("=");
        if (parts.length == 2) {
            Integer left = Integer.parseInt(parts[0]);
            String right = parts[1];
            result.setKey(left);
            result.setValue(right);
        }
        return result;
    }
}