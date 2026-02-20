package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.AstronautBetData;

import static com.betsoft.casino.bots.utils.CrashBetKeyUtil.getCrashBetKeyFromNickname;

public class CrashGameBotStrategy extends AbstractCrashGameBotStrategy  {

    public CrashGameBotStrategy(int numberRoundsBeforeRestart) {
        super(numberRoundsBeforeRestart);
    }

    @Override
    public AstronautBetData generateMultiplierForFirst(String nickname) {
        String crashBetKey = getCrashBetKeyFromNickname(nickname);
        double multiplier = getPlayerRandomMultForRange(2, 300);
        return new AstronautBetData(crashBetKey,  multiplier, true);
    }

    @Override
    public AstronautBetData generateMultiplierForSecond(String nickname) {
        return new AstronautBetData(System.currentTimeMillis() + "_1_" + nickname, getPlayerRandomMultForRange(10, 300), false);
    }

    @Override
    public AstronautBetData generateMultiplierForThird(String nickname) {
        return new AstronautBetData(System.currentTimeMillis() + "_2_" + nickname, getPlayerRandomMultForRange(50, 1000), false);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CrashGameBotStrategy{");
        sb.append("numberRoundsBeforeRestart=").append(numberRoundsBeforeRestart);
        sb.append('}');
        return sb.toString();
    }
}
