package com.betsoft.casino.mp.model.gameconfig;

import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GameTools {

    public static int getIndexFromDoubleProb(double[] prob) {
        double seed = RNG.rand();
        double sumOfWeights = 0;
        for (double v : prob) {
            sumOfWeights += v;
        }

        double v = new BigDecimal(sumOfWeights, MathContext.DECIMAL32).doubleValue();
        if (v != 1)
            return -1;

        int index = 0;
        do {
            seed -= prob[index];
            if (seed < 0) {
                break;
            }
            ++index;
        } while (seed > 0);

        return index;
    }

    public static <T, V> Pair<T, V> getRandomPair(List<Triple<T, V, Double>> lisTriples) {
        double[] weights = lisTriples.stream().mapToDouble(Triple::third).toArray();
        double sum = Arrays.stream(weights).sum();
        if (sum != 1)
            weights = Arrays.stream(weights).map(v -> v / sum).toArray();

        int indexFromDoubleProb = getIndexFromDoubleProb(weights);
        Triple<T, V, Double> triple = lisTriples.get(indexFromDoubleProb);
        return new Pair<>(triple.first(), triple.second());
    }

    public static <T> T getRandomNumberKeyFromMap(Map<T, Double> prob){
        T res = null;
        double seed = RNG.rand();
        for (Map.Entry<T, Double> doubleDoubleEntry : prob.entrySet()) {
            seed -= doubleDoubleEntry.getValue();
            if (seed < 0) {
                res = doubleDoubleEntry.getKey();
                break;
            }
        }
        return res;
    }

    public static <T> T getRandomNumberKeyFromMapWithNorm(Map<T, Double> prob){
        T res = null;
        double sum = prob.values().stream().mapToDouble(Double::doubleValue).sum();
        double seed = RNG.rand();
        for (Map.Entry<T, Double> doubleDoubleEntry : prob.entrySet()) {
            seed -= doubleDoubleEntry.getValue()/sum;
            if (seed < 0) {
                res = doubleDoubleEntry.getKey();
                break;
            }
        }
        return res;
    }

}
