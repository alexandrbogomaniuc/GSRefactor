package com.betsoft.casino.mp.common.math;

import java.util.HashMap;
import java.util.Map;

public class TimeMeasure {
    private static TimeMeasure ourInstance = new TimeMeasure();

    private Map<String, MeasureValue> measureValueMap;

    public static TimeMeasure getInstance() {
        return ourInstance;
    }

    private TimeMeasure() {
        measureValueMap = new HashMap<>();
    }

    public void addMeasure(String key, long time) {
        MeasureValue measureValue = measureValueMap.get(key) == null ? new MeasureValue() : measureValueMap.get(key);
        measureValue.cnt++;
        measureValue.totalAmount += time;
        measureValueMap.put(key, measureValue);
    }

    public void printData() {
        for (Map.Entry<String, MeasureValue> entry : measureValueMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue().getAverage() + " cnt: " + entry.getValue().cnt);
        }
    }

    private class MeasureValue {
        private long cnt;
        private long totalAmount;

        public double getAverage() {
            return (double)totalAmount / cnt;
        }
    }

}
