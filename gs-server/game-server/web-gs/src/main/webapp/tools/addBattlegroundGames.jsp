<%@ page import="com.abs.casino.promo.persisters.CassandraBattlegroundConfigPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.promo.battleground.BattlegroundConfig" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    CassandraPersistenceManager cpm = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraBattlegroundConfigPersister persister = cpm.getPersister(CassandraBattlegroundConfigPersister.class);

    Set<BattlegroundConfig> bgConfigs = persister.getConfigs(6274L);
    for(BattlegroundConfig bgConfig : bgConfigs) {
        persister.delete(6274L, bgConfig.getGameId());
    }

    bgConfigs = persister.getConfigs(6275L);
    for(BattlegroundConfig bgConfig : bgConfigs) {
        persister.delete(6275L, bgConfig.getGameId());
    }

    //add games buyIn configurations for Dragonstone, Mission Amazon, SectorX
    long[] bgGameIds = { 856, 862, 867 };

    Map<Long, List<Long>> bgBuyIns = new HashMap<Long, List<Long>>();

    bgBuyIns.put(6274L, new ArrayList<>(Arrays.asList(1_00L, 2_00L, 5_00L, 10_00L, 20_00L, 50_00L, 100_00L, 500_00L, 1000_00L)));
    bgBuyIns.put(6275L, new ArrayList<>(Arrays.asList(1_00L, 2_00L, 5_00L, 10_00L, 20_00L, 50_00L, 100_00L, 500_00L, 1000_00L)));

    for (Map.Entry<Long, List<Long>> entry : bgBuyIns.entrySet()) {
        long bank = entry.getKey();
        String icon = "";
        String rulesLink = "";
        Map<String, List<Long>> buyInsByCurrencyMap = new HashMap<>();
        for (long gameId : bgGameIds) {
            BattlegroundConfig bgConfig = new BattlegroundConfig(gameId, icon, rulesLink, entry.getValue(),
                    buyInsByCurrencyMap, entry.getValue(), true, 10.0, 6);
            persister.save(bank, bgConfig);
        }
    }

    //add games buyIn configurations for Max Blast Champs
    long[] crashGameIds = { 864 };

    Map<Long, List<Long>> crashBuyIns = new HashMap<Long, List<Long>>();

    crashBuyIns.put(6274L, new ArrayList<>(Arrays.asList(1_00L, 2_00L, 5_00L, 10_00L, 20_00L, 50_00L, 100_00L, 500_00L, 1000_00L)));
    crashBuyIns.put(6275L, new ArrayList<>(Arrays.asList(1_00L, 2_00L, 5_00L, 10_00L, 20_00L, 50_00L, 100_00L, 500_00L, 1000_00L)));

    for (Map.Entry<Long, List<Long>> entry : crashBuyIns.entrySet()) {
        long bank = entry.getKey();
        String icon = "";
        String rulesLink = "";
        Map<String, List<Long>> buyInsByCurrencyMap = new HashMap<>();
        for (long gameId : crashGameIds) {
            BattlegroundConfig battlegroundConfig = new BattlegroundConfig(gameId, icon, rulesLink, entry.getValue(),
                    buyInsByCurrencyMap, entry.getValue(), true, 10.0, 30);
            persister.save(bank, battlegroundConfig);
        }
    }

    response.getWriter().write("Done!");
%>
