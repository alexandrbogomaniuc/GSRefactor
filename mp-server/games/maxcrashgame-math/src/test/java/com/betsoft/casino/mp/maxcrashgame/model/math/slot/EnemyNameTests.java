package com.betsoft.casino.mp.maxcrashgame.model.math.slot;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EnemyNameTests {
    public static void main(String[] args) {
        long currentTime = System.currentTimeMillis();
        long startTTime = System.currentTimeMillis();

        double crashMult = 3.1;
        double currentMult = 1.0;

        for (int i = 0; i < 1000; i++) {
            currentTime += 100;
            double diffTime = currentTime - startTTime;
            BigDecimal bd = BigDecimal.valueOf(0.01 + Math.exp((diffTime * 0.0331) / 1000));
            double newValue = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
            if(newValue > crashMult){
                currentMult = crashMult;
                break;
            }else{
                currentMult = newValue;
            }
            System.out.println("i: " + i + ", currentMult: " + currentMult);
        }
    }
}
