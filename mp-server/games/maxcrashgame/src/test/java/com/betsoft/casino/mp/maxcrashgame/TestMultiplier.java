package com.betsoft.casino.mp.maxcrashgame;

import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.google.common.util.concurrent.AtomicDouble;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class AstronautDataParams {
    private final double bet;
    private final int minMult;
    private final int maxMult;

    public AstronautDataParams(double bet, int minMult, int maxMult) {
        this.bet = bet;
        this.minMult = minMult;
        this.maxMult = maxMult;
    }

    public double getBet() {
        return bet;
    }

    public int getMinMult() {
        return minMult;
    }

    public int getMaxMult() {
        return maxMult;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AstronautDataParams{");
        sb.append("bet=").append(bet);
        sb.append(", minMult=").append(minMult);
        sb.append(", maxMult=").append(maxMult);
        sb.append('}');
        return sb.toString();
    }
}

public class TestMultiplier {

    private static final double Z = Math.pow(2, 52);

    private static final double H = 25.0;

    public static double getRandomMultiplier() {
        double maxAllowedCrashMultiplier = 100.00;
        double randomMultiplier;
        long R;
        do {
            R = (long) (Z * RNG.rand());
            randomMultiplier = Math.floor((100.0 * Z - R) / (Z - R)) / 100.0;

        } while (randomMultiplier > maxAllowedCrashMultiplier);
        return randomMultiplier;
    }

    public static double getPlayerRandomMultiplierForRange(int min, int max) {
        double temMult = RNG.nextInt(min, max) + RNG.rand();
        return BigDecimal.valueOf(temMult).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    int cntRounds;
    int multiplierLimit;
    List<AstronautDataParams> astronautsData;
    boolean needApplyNaturalRule;

    public TestMultiplier(int cntRounds, int multiplierLimit, List<AstronautDataParams> astronautsData, boolean needApplyNaturalRule) {
        this.cntRounds = cntRounds;
        this.multiplierLimit = multiplierLimit;
        this.astronautsData = astronautsData;
        this.needApplyNaturalRule = needApplyNaturalRule;
    }

    public static void main(String[] args) {
        List<AstronautDataParams> astronautsData = new ArrayList<>();
        int cntTests = 1;
        int cntRounds = 10000000;
        int multiplierLimit = 22;
        boolean needApplyNaturalRule = false;
        astronautsData.add(new AstronautDataParams(100., 3,4));
        astronautsData.add(new AstronautDataParams(100., 200,300));
       // astronautsData.add(new AstronautDataParams(100., 70,200));

        for (String arg : args) {
            String[] param_ = arg.split("=");
            switch (param_[0]) {
                case "astronautsData":
                    astronautsData.clear();
                    String[] params = param_[1].split("\\|");
                    for (int i = 0; i < params.length; i++) {
                        String param = params[i];
                        String[] paramAstronaut = param.split(",");
                        astronautsData.add(new AstronautDataParams(
                                Double.parseDouble(paramAstronaut[0]),
                                Integer.parseInt(paramAstronaut[1]),
                                Integer.parseInt(paramAstronaut[2])));
                        ;
                    }
                    break;
                case "cntTests":
                    cntTests = Integer.parseInt(param_[1]);
                    break;
                case "cntRounds":
                    cntRounds = Integer.parseInt(param_[1]);
                    break;
                case "multiplierLimit":
                    multiplierLimit = Integer.parseInt(param_[1]);
                    break;
                case "needApplyNaturalRule":
                    needApplyNaturalRule = param_[1].equals("true");
                    break;
                default:
                    break;
            }
        }

        System.out.println("cntTests: " + cntTests);
        System.out.println("cntRounds: " + cntRounds);
        System.out.println("multiplierLimit: " + multiplierLimit);
        System.out.println("needApplyNaturalRule: " + needApplyNaturalRule);
        System.out.println("astronautsData: " + astronautsData);

        TestMultiplier testMultiplier = new TestMultiplier(cntRounds, multiplierLimit, astronautsData, needApplyNaturalRule);

        for (int i = 0; i < cntTests; i++) {
            testMultiplier.doTest();
        }
    }

    void doTest() {
        AtomicDouble totalBets = new AtomicDouble(0);
        AtomicDouble totalWins = new AtomicDouble(0);
        List<Pair<Double, Double>> bets = new ArrayList<>();
        AtomicLong cntWins = new AtomicLong(0);
        long totalCountTests = cntRounds;

        while (totalCountTests-- > 0) {
            bets.clear();
            for (AstronautDataParams astronautsDatum : astronautsData) {
                double playerRandomMultiplier =
                        TestMultiplier.getPlayerRandomMultiplierForRange(astronautsDatum.getMinMult(), astronautsDatum.getMaxMult());
                Pair<Double, Double> bet = new Pair<>(astronautsDatum.getBet(), playerRandomMultiplier);
                bets.add(bet);
            }
            bets.forEach(pair -> totalBets.addAndGet(pair.getKey()));

            if (RNG.rand() < 1 / H) {
                // lose
            } else {
                double randomMultiplierNatural = TestMultiplier.getRandomMultiplier();
                AtomicBoolean limitAppeared = new AtomicBoolean(randomMultiplierNatural > multiplierLimit);
                double randomMultiplier = Math.min(randomMultiplierNatural, multiplierLimit);
                AtomicBoolean wasWin = new AtomicBoolean(false);

                for(Pair<Double, Double> bet : bets) {

                    Double playerMult = bet.getValue();

                    if (needApplyNaturalRule) {
                        if (limitAppeared.get() && playerMult > randomMultiplierNatural) {
                            limitAppeared.set(false);
                        }
                    }

                    if (playerMult <= randomMultiplier || limitAppeared.get()) {
                        Double playerBet = bet.getKey();
                        wasWin.set(true);
                        double realWin = playerMult < multiplierLimit ? playerMult : multiplierLimit;
                        totalWins.addAndGet(playerBet * realWin);
                    }
                }

                if (wasWin.get()) {
                    cntWins.incrementAndGet();
                }
            }
        }

        double rtp = totalWins.doubleValue() / totalBets.doubleValue();
        System.out.println("totalBets: " + totalBets.doubleValue() + ", totalWins: " + totalWins.doubleValue()
                + ", RTP: " + rtp + ", cntWins: " + cntWins.longValue() + ", cntRounds: " + cntRounds);
    }
}
