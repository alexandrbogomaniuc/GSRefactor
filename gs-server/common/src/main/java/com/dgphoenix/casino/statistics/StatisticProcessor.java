package com.abs.casino.statistics;

import com.abs.casino.common.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StatisticProcessor {
    private static final String COMMON_BOT_MACHINE_ID = "";
    private static StatisticProcessor instance = new StatisticProcessor();

    private Map<String, Integer> botMachineOnlinePlayers = new ConcurrentHashMap<String, Integer>();
    private Map<String, Integer> botMachineOfflinePlayers = new ConcurrentHashMap<String, Integer>();
    private String logFile;

    private ConcurrentHashMap<String, TimesHolder> botMachineTimesMap
            = new ConcurrentHashMap<String, TimesHolder>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock changeStateLock = lock.readLock();
    private final Lock clearStateLock = lock.writeLock();
    private Date clearDate;

    private TimesHolder commonTimesHolder = new TimesHolder();


    private StatisticProcessor() {
    }

    public static StatisticProcessor getInstance() {
        return instance;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getLogFile() {
        return logFile;
    }

    public void processStatistic(String botMachineId, String playersNum, String droppedPlayersNum,
                                 String loginData, String betsData, String closeGameData,
                                 String historyData, String logoutData, String openGameData)
            throws Exception {
        changeStateLock.lock();
        try {
//such ordering for String array is obligatory
            String[] data = {loginData, openGameData, betsData, historyData, closeGameData, logoutData};

            botMachineOnlinePlayers.put(botMachineId, Integer.parseInt(playersNum));
            botMachineOfflinePlayers.put(botMachineId, Integer.parseInt(droppedPlayersNum));

            //process local stat
            TimesHolder localTimesHolder = botMachineTimesMap.get(botMachineId);
            if (localTimesHolder == null) {
                botMachineTimesMap.putIfAbsent(botMachineId, new TimesHolder());
                localTimesHolder = botMachineTimesMap.get(botMachineId);
            }
            localTimesHolder.collectStatistic(loginData, betsData, closeGameData,
                    historyData, logoutData, openGameData);
            print(botMachineId, localTimesHolder);

            //process global stat
            commonTimesHolder.collectStatistic(loginData, betsData, closeGameData,
                    historyData, logoutData, openGameData);
            print(COMMON_BOT_MACHINE_ID, commonTimesHolder);
        } finally {
            changeStateLock.unlock();
        }
    }

    public void processStatistic(String botMachineId, String playersNum, String droppedPlayersNum,
                                 String loginData, String betsData, String closeGameData,
                                 String historyData, String logoutData, String openGameData, List<Pair<String, String>> addition)
            throws Exception {
        changeStateLock.lock();
        try {
//such ordering for String array is obligatory
            String[] data = {loginData, openGameData, betsData, historyData, closeGameData, logoutData};

            botMachineOnlinePlayers.put(botMachineId, Integer.parseInt(playersNum));
            botMachineOfflinePlayers.put(botMachineId, Integer.parseInt(droppedPlayersNum));

            //process local stat
            TimesHolder localTimesHolder = botMachineTimesMap.get(botMachineId);
            if (localTimesHolder == null) {
                botMachineTimesMap.putIfAbsent(botMachineId, new TimesHolder());
                localTimesHolder = botMachineTimesMap.get(botMachineId);
            }
            localTimesHolder.collectStatistic(loginData, betsData, closeGameData,
                    historyData, logoutData, openGameData, addition);
            print(botMachineId, localTimesHolder);

            //process global stat
            commonTimesHolder.collectStatistic(loginData, betsData, closeGameData,
                    historyData, logoutData, openGameData, addition);
            print(COMMON_BOT_MACHINE_ID, commonTimesHolder);
        } finally {
            changeStateLock.unlock();
        }
    }

    private void processCollectStatistic(List<TimeContainer> timesList,
                                         String[] data) throws Exception {
        if (timesList.size() != data.length) {
            throw new Exception("Bad time data");
        }
        Iterator<TimeContainer> iter = timesList.iterator();
        for (String aData : data) {
            iter.next().addData(aData);
        }
    }

    public void clearLogFile() throws IOException {
        clearStateLock.lock();
        try {
            commonTimesHolder = new TimesHolder();
            for (String botMachineId : botMachineOnlinePlayers.keySet()) {
                try (BufferedWriter out = new BufferedWriter(new FileWriter(logFile + botMachineId, false))) {
                    out.write("");
                }
                botMachineOnlinePlayers.remove(botMachineId);
                botMachineOfflinePlayers.remove(botMachineId);
                botMachineTimesMap.remove(botMachineId);
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(logFile + COMMON_BOT_MACHINE_ID, false))) {
                out.write("");
            }
            clearDate = new Date();
        } finally {
            clearStateLock.unlock();
        }
    }

    private void print(String botMachineId, TimesHolder timesHolder) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(logFile + botMachineId, true))) {
            out.write("*********************************************************\n");
            out.write(new Date().toString() + "\n");
            String clrDate = clearDate == null ? "never." : clearDate.toString();
            out.write("Statistics last cleared at: " + clrDate + "\n");
            if (COMMON_BOT_MACHINE_ID.equals(botMachineId)) {
                for (String serverId : botMachineOnlinePlayers.keySet()) {
                    printPlayersCount(serverId, out);
                }
                out.write("Total online:" + countTotalPlayers(botMachineOnlinePlayers) + "\n");
                out.write("Total offline:" + countTotalPlayers(botMachineOfflinePlayers) + "\n\n");
            } else {
                printPlayersCount(botMachineId, out);
            }
            out.write("Statistics:\n");
            for (TimeContainer timeContainer : timesHolder.getTimeList()) {
                out.write(timeContainer.getName() + ": " + timeContainer.toString() + "\n\n");
            }
        }
    }

    private void printPlayersCount(String botMachineId, BufferedWriter out) throws IOException {
        out.write("botMachineId:" + botMachineId + " onlinePlayersCount:"
                + botMachineOnlinePlayers.get(botMachineId) + " offlinePlayersCount:"
                + botMachineOfflinePlayers.get(botMachineId) + "\n");
    }

    private int countTotalPlayers(Map<String, Integer> serverToPlayers) {
        int summ = 0;
        for (Integer playersCount : serverToPlayers.values()) {
            summ += playersCount;
        }
        return summ;
    }

}