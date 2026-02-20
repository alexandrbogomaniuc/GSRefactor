package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.AstronautBetData;
import com.dgphoenix.casino.common.util.Triple;
import java.util.ArrayList;
import java.util.List;

import static com.betsoft.casino.bots.utils.CrashBetKeyUtil.getCrashBetKeyFromNickname;

public class CrashGameBotStrategyCustom extends AbstractCrashGameBotStrategy {

    private List<Triple<Integer, Integer, Boolean>> astroParams;

    public CrashGameBotStrategyCustom(int numberRoundsBeforeRestart, String astroParamsString) {
        super(numberRoundsBeforeRestart);
        astroParams = new ArrayList<>();
        String[] split = astroParamsString.split("\\|");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (!s.isEmpty()) {
                String[] aParams = s.split(",");
                if (aParams.length == 3) {
                    int min = Integer.parseInt(aParams[0]);
                    int max = Integer.parseInt(aParams[1]);
                    boolean needEject = Boolean.parseBoolean(aParams[2]);
                    astroParams.add(new Triple<>(min, max, needEject));
                }
            }
        }
        if(astroParams.isEmpty()){
            astroParams.add(new Triple<>(2,10, false));
            astroParams.add(new Triple<>(10,50, false));
            astroParams.add(new Triple<>(50,100, false));
        }

    }

    @Override
    public AstronautBetData generateMultiplierForFirst(String nickname) {
        Triple<Integer, Integer, Boolean> param = astroParams.get(0);
        String crashBetKey = getCrashBetKeyFromNickname(nickname);
        double multiplier = getPlayerRandomMultForRange(param.first(),param.second());

        return new AstronautBetData(crashBetKey, multiplier, param.third());
    }

    @Override
    public AstronautBetData generateMultiplierForSecond(String nickname) {
        Triple<Integer, Integer, Boolean> param = astroParams.get(1);
        return new AstronautBetData(System.currentTimeMillis() + "_1_" + nickname,
                getPlayerRandomMultForRange(param.first(),param.second()),param.third());
    }

    @Override
    public AstronautBetData generateMultiplierForThird(String nickname) {
        Triple<Integer, Integer, Boolean> param = astroParams.get(2);
        return new AstronautBetData(System.currentTimeMillis() + "_2_" + nickname,
                getPlayerRandomMultForRange(param.first(),param.second()),param.third());
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CrashGameBotStrategyCustom{");
        sb.append("astroParams=").append(astroParams);
        sb.append(", numberRoundsBeforeRestart=").append(numberRoundsBeforeRestart);
        sb.append('}');
        return sb.toString();
    }
}
