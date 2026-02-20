package com.betsoft.casino.bots;

import com.betsoft.casino.bots.requests.IBotRequest;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

public interface IBot {
    void start();

    void stop();

    void restart();

    default void openNewRoom() {

    }

    String getId();

    void setStats(Stats stats);

    Stats getStats();

    void count(int key);

    void count(int key, int delta);

    Logger getLogger();

    int send(IBotRequest request);

    Mono<Long> sleep(int min, int max);

    Mono<Long> sleep(long time);

    String getUrl();

    void setUrl(String url);

    boolean isWssUrl();

    int getServerId();

    void setServerId(int serverId);

    int getBankId();

    void setBankId(int bankId);
}
