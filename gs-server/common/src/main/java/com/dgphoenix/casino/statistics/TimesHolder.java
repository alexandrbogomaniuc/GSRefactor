package com.abs.casino.statistics;

import com.abs.casino.common.util.Pair;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * User: dartilla
 * Date: Sep 13, 2010
 * Time: 2:32:23 PM
 */
public class TimesHolder {
    private final ListOrderedMap times = (ListOrderedMap) ListOrderedMap.decorate(new HashMap<String, TimeContainer>());
    private final TimeContainer login = new TimeContainer("Login");
    private final TimeContainer openGame = new TimeContainer("OpenGame");
    private final TimeContainer bets = new TimeContainer("Bets");
    private final TimeContainer history = new TimeContainer("History");
    private final TimeContainer closeGame = new TimeContainer("CloseGame");
    private final TimeContainer logout = new TimeContainer("Logout");

    public TimesHolder() {
        addTimeContainer(login);
        addTimeContainer(openGame);
        addTimeContainer(bets);
        addTimeContainer(history);
        addTimeContainer(closeGame);
        addTimeContainer(logout);
    }

    protected void addTimeContainer(TimeContainer container) {
        times.put(container.getName(), container);
    }

    public void collectStatistic(String loginData, String betsData, String closeGameData,
                                 String historyData, String logoutData, String openGameData) {
        login.addData(loginData);
        openGame.addData(openGameData);
        bets.addData(betsData);
        history.addData(historyData);
        closeGame.addData(closeGameData);
        logout.addData(logoutData);
    }

    public void collectStatistic(String loginData, String betsData, String closeGameData,
                                 String historyData, String logoutData, String openGameData, List<Pair<String, String>> addition) {
        login.addData(loginData);
        openGame.addData(openGameData);
        bets.addData(betsData);
        history.addData(historyData);
        closeGame.addData(closeGameData);
        logout.addData(logoutData);
        for (Pair<String, String> entry : addition) {
            TimeContainer container = (TimeContainer) times.get(entry.getKey());
            if (container == null) {
                synchronized (times) {
                    container = (TimeContainer) times.get(entry.getKey());
                    if (container == null) {
                        times.put(entry.getKey(), container = new TimeContainer(entry.getKey()));
                    }
                }
            }
            container.addData(entry.getValue());
        }
    }

    public Collection<TimeContainer> getTimeList() {
        return times.values();
    }
}