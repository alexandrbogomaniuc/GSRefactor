package com.betsoft.casino.mp.maxcrashgame.model.math.config;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class GameConfigValidator {
    private static final double MAX_MULTIPLIER = 4458563631096791.05;

    public String validate(GameConfig config) {
        if (config.getInitialTime() < 0) {
            return "Incorrect 'initial time' [integer (>=0), in millisecond]";
        }

        if (config.getCrashMultiplier() < 1 && config.getCrashMultiplier() != -1) {
            return "Incorrect 'crash multiplier' [float (>=1), 2 decimal-place precision] [-1 unable parameter]";
        }

        if (config.getCrashMultiplier() > MAX_MULTIPLIER) {
            return "Too big 'crash multiplier', max allowed=" + MAX_MULTIPLIER;
        }

        if (config.getH() <= 0) {
            return "Incorrect 'H' [integer>0]";
        }

        if (config.getFunction() == null || config.getFunction().isEmpty()) {
            return "Empty function";
        }

        try {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
            long startTime = System.currentTimeMillis();
            scriptEngine.put("t", 0);
            double prevRes = (double) scriptEngine.eval(config.getFunction());
            if (prevRes < 1.0) {
                return "Incorrect function, expected f(0) >= 1.0, actual f(0) = " + prevRes;
            }
            for (int nextTime = 5000; nextTime < 100000; nextTime += 5000) {
                long time = System.currentTimeMillis() + nextTime - startTime;
                scriptEngine.put("t", time);
                double res = (double) scriptEngine.eval(config.getFunction());
                if (res < 0) {
                    return "Incorrect function: return negative result for t ="  + time;
                }
                if (prevRes > res) {
                    return "Error: The function is not monotonic";
                }
                prevRes = res;
            }
        } catch (Exception e) {
            return "Incorrect function: " + e.getMessage();
        }

        return "";
    }
}
