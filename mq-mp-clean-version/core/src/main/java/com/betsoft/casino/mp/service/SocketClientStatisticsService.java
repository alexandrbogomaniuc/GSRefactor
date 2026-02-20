package com.betsoft.casino.mp.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.betsoft.casino.mp.model.onlineplayer.SocketClientsStats;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientsStats.SocketClientsStat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
public class SocketClientStatisticsService {
    private static final Logger LOG = LogManager.getLogger(SocketClientStatisticsService.class);

    private RoomPlayersMonitorService roomPlayersMonitorService;

    @Autowired
    public SocketClientStatisticsService(RoomPlayersMonitorService roomPlayersMonitorService) {
        this.roomPlayersMonitorService = roomPlayersMonitorService;
    }

    public String getStatsJson() {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        try {
            return jsonMapper.writeValueAsString(getStats());
        } catch (JsonProcessingException e) {
            LOG.error("Error generating json: ", e);
            return "{}";
        }
    }

    public SocketClientStatsDto getStats() {
        NavigableMap<Long, SocketClientsStats> statsMap = roomPlayersMonitorService.getSocketClientsStats();

        Optional<Map<Integer, SocketClientsStat>> latest = Optional.ofNullable(statsMap.lastEntry())
                .map(e -> e.getValue()).map(SocketClientsStats::getStats);
        Optional<Map<Integer, SocketClientsStat>> min15 = Optional.ofNullable(averageSocketStats(statsMap, 15))
                .map(SocketClientsStats::getStats);
        Optional<Map<Integer, SocketClientsStat>> min30 = Optional.ofNullable(averageSocketStats(statsMap, 30))
                .map(SocketClientsStats::getStats);

        SocketClientStatsDto stats = new SocketClientStatsDto();
        Map<String, String> sidmap = new HashMap<>();
        Utilization utilization = new Utilization();

        if (!latest.isPresent()) {
            return stats;
        }

        Map<String, Long> nowU = new HashMap<String, Long>();
        for (Entry<Integer, SocketClientsStat> latestE : latest.get().entrySet()) {
            sidmap.put(latestE.getKey().toString(), latestE.getValue().getServerIP());
            nowU.put(latestE.getKey().toString(), latestE.getValue().getClientsCount());
        }
        utilization.setNow(nowU);
        stats.setSidmap(sidmap);
        stats.setUtilization(utilization);

        if (!min15.isPresent()) {
            return stats;
        }
        Map<String, Long> min15U = new HashMap<String, Long>();
        for (Entry<Integer, SocketClientsStat> min15E : min15.get().entrySet()) {
            min15U.put(min15E.getKey().toString(), min15E.getValue().getClientsCount());
        }
        utilization.setMin15(min15U);
        stats.setUtilization(utilization);

        if (!min30.isPresent()) {
            return stats;
        }
        Map<String, Long> min30U = new HashMap<String, Long>();
        for (Entry<Integer, SocketClientsStat> min30E : min30.get().entrySet()) {
            min30U.put(min30E.getKey().toString(), min30E.getValue().getClientsCount());
        }
        utilization.setMin30(min30U);
        stats.setUtilization(utilization);

        return stats;
    }

    private SocketClientsStats averageSocketStats(NavigableMap<Long, SocketClientsStats> history, int minutes) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        long to = now.toEpochMilli();
        long from = now.minus(minutes, ChronoUnit.MINUTES).toEpochMilli();
        NavigableMap<Long, SocketClientsStats> window = history.subMap(from, true, to, true);

        // Aggregation map: serverId → [sum, count]
        Map<Integer, int[]> aggregation = new HashMap<>();
        Map<Integer, String> serverIps = new HashMap<>();

        for (SocketClientsStats snapshot : window.values()) {
            for (Map.Entry<Integer, SocketClientsStat> entry : snapshot.getStats().entrySet()) {
                int serverId = entry.getKey();
                SocketClientsStat stat = entry.getValue();

                aggregation.computeIfAbsent(serverId, id -> new int[2]); // [sum, count]
                aggregation.get(serverId)[0] += stat.getClientsCount();
                aggregation.get(serverId)[1] += 1;

                serverIps.putIfAbsent(serverId, stat.getServerIP()); // assuming IP doesn't change
            }
        }

        // Construct result
        Map<Integer, SocketClientsStat> stats = new HashMap<Integer, SocketClientsStat>();
        for (Map.Entry<Integer, int[]> entry : aggregation.entrySet()) {
            int serverId = entry.getKey();
            int[] sumAndCount = entry.getValue();
            double avg = sumAndCount[1] > 0 ? ((double)sumAndCount[0]) / sumAndCount[1] : 0;

            stats.put(serverId, new SocketClientsStat(serverIps.get(serverId), (long)avg));
        }

        return new SocketClientsStats(stats);
    }

    public static class SocketClientStatsDto {
        private Map<String, String> sidmap;
        private Utilization utilization;

        public Map<String, String> getSidmap() {
            return sidmap;
        }

        public void setSidmap(Map<String, String> sidmap) {
            this.sidmap = sidmap;
        }

        public Utilization getUtilization() {
            return utilization;
        }

        public void setUtilization(Utilization utilization) {
            this.utilization = utilization;
        }
    }

    public static class Utilization {
        private Map<String, Long> now;
        private Map<String, Long> min15;
        private Map<String, Long> min30;

        public Map<String, Long> getNow() {
            return now;
        }

        public void setNow(Map<String, Long> now) {
            this.now = now;
        }

        public Map<String, Long> getMin15() {
            return min15;
        }

        public void setMin15(Map<String, Long> min15) {
            this.min15 = min15;
        }

        public Map<String, Long> getMin30() {
            return min30;
        }

        public void setMin30(Map<String, Long> min30) {
            this.min30 = min30;
        }
    }
}
