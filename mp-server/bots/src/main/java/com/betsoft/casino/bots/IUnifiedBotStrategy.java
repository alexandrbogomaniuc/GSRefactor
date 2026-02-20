package com.betsoft.casino.bots;

import com.betsoft.casino.bots.strategies.IRoomBotStrategy;

public interface IUnifiedBotStrategy extends IRoomBotStrategy {

    AstronautBetData generateMultiplierForFirst(String nickname);

    AstronautBetData generateMultiplierForSecond(String nickname);

    AstronautBetData generateMultiplierForThird(String nickname);

    int getNumberRoundBeforeRestart();

    default double generateMultiplier(){
        return 1.1;
    };
}
