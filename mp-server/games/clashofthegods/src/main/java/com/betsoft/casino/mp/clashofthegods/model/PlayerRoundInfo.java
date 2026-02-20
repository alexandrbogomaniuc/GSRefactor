package com.betsoft.casino.mp.clashofthegods.model;

import com.betsoft.casino.mp.clashofthegods.model.math.EnemyRange;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
import com.betsoft.casino.mp.clashofthegods.model.math.MathData;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.dgphoenix.casino.common.util.Pair;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("rawtypes")
public class PlayerRoundInfo extends AbstractActionGamePlayerRoundInfo<EnemyType, EnemyStat, PlayerRoundInfo> {
    public PlayerRoundInfo() {
        super();
    }

    public PlayerRoundInfo(long roomId) {
        this.gameId = (int) GameType.CLASH_OF_THE_GODS.getGameId();
        this.roomId = roomId;
        this.statBoss = new BossStat(gameId);
        Set<Integer> possibleBetLevels = MathData.getPossibleBetLevels();
        for (IEnemyType enemyType : getEnemyTypes()) {
            for (Integer betLevel : possibleBetLevels) {
                String enemyNameKey = enemyType.getId() + "_" + enemyType.getName() + "_" + betLevel;
                this.statByEnemies.put(enemyNameKey, new EnemyStat(enemyType.isHVenemy(), gameId, betLevel));
            }
        }
        questWeapons = new HashMap<>();
        realShotsCount = new HashMap<>();
        additionalWins = new HashMap<>();
        weaponStatBySources = new HashMap<>();
        moneyWheelCompleted = 0;
        moneyWheelPayouts = 0;
        totalDamage = 0;
    }

    @Override
    public EnemyType[] getEnemyTypes() {
        return EnemyType.values();
    }

    @Override
    public EnemyType getEnemyTypeById(int id) {
        return EnemyType.getById(id);
    }

    @Override
    public List<EnemyType> getBaseEnemies() {
        ArrayList<EnemyType> enemyTypes = new ArrayList<>(EnemyRange.BaseEnemies.getEnemies());
        return enemyTypes;
    }

    public static PlayerRoundInfo getPlayerRoundInfoFromVBAData(String data, double bets, double wins) {
        PlayerRoundInfo res = new PlayerRoundInfo(0);
        res.fillPlayerRoundInfo(res, data, bets, wins);
        return res;
    }

    @Override
    protected String prepareVBAData(int returnedBet) {
        StringBuilder sb = new StringBuilder();
        sb.append("timeOfRoundEnd").append(DELIMETER_VALUE_PARAM).append(timeOfRoundEnd).append(DELIMETER_FIELDS);
        sb.append("shotsCount").append(DELIMETER_VALUE_PARAM).append(shotsCount).append(DELIMETER_FIELDS);
        sb.append("xpearned").append(DELIMETER_VALUE_PARAM).append(xpearned == null ? 0 : xpearned.getLongAmount()).append(DELIMETER_FIELDS);
        sb.append("playerRoundId").append(DELIMETER_VALUE_PARAM).append(playerRoundId).append(DELIMETER_FIELDS);
        sb.append("roomRoundId").append(DELIMETER_VALUE_PARAM).append(roomRoundId).append(DELIMETER_FIELDS);
        sb.append("roomId").append(DELIMETER_VALUE_PARAM).append(roomId).append(DELIMETER_FIELDS);
        sb.append("ammoAmountBuyIn").append(DELIMETER_VALUE_PARAM).append(ammoAmountBuyIn).append(DELIMETER_FIELDS);
        sb.append("ammoAmountReturned").append(DELIMETER_VALUE_PARAM).append(returnedBet != -1 ? returnedBet : ammoAmountReturned).append(DELIMETER_FIELDS);
        sb.append("roomStake").append(DELIMETER_VALUE_PARAM).append(roomStake).append(DELIMETER_FIELDS);
        sb.append("totalBetsSpecialWeapons").append(DELIMETER_VALUE_PARAM).append(totalBetsSpecialWeapons.toDoubleCents()).append(DELIMETER_FIELDS);
        LOGGER.debug("prepareVBAData statByEnemies.size()" + statByEnemies.size());

        for (EnemyType enemyType : getEnemyTypes()) {
            Set<Integer> possibleBetLevels = MathData.getPossibleBetLevels();
            for (Integer betLevel : possibleBetLevels) {
                String nameKey = enemyType.getId() + "_" + enemyType.getName() + "_" + betLevel;
                EnemyStat enemyStat = statByEnemies.get(nameKey);
                if (enemyStat != null && enemyStat.getCntTotalHitsToEnemy() > 0) {
                    sb.append("name").append(DELIMETER_VALUE_PARAM).append(nameKey).append(DELIMETER_PARAM_FIELD);
                    sb.append("cntShotsToEnemy").append(DELIMETER_VALUE_PARAM).append(enemyStat.getCntShotsToEnemy()).append(DELIMETER_PARAM_FIELD);
                    sb.append("payouts").append(DELIMETER_VALUE_PARAM).append(enemyStat.getPayouts().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
                    sb.append("cntKills").append(DELIMETER_VALUE_PARAM).append(enemyStat.getCntKills()).append(DELIMETER_PARAM_FIELD);
                    sb.append("hvBets").append(DELIMETER_VALUE_PARAM).append(enemyStat.getHvBets().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
                    sb.append("isHV").append(DELIMETER_VALUE_PARAM).append(enemyStat.isHighValueEnemy()).append(DELIMETER_PARAM_FIELD);
                    sb.append("mainBets").append(DELIMETER_VALUE_PARAM).append(enemyStat.getMainBets().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
                    addSpecialWeapons(sb, enemyStat.getSpecialWeaponsStats());
                    sb.append(DELIMETER_FIELDS);
                }
            }
        }
        sb.append("weaponSurplusVBA").append(DELIMETER_VALUE_PARAM).append(weaponSurplusVBA == null ? "" : weaponSurplusVBA).append(DELIMETER_FIELDS);
        sb.append("weaponSurplusMoney").append(DELIMETER_VALUE_PARAM).append(weaponSurplusMoney == null ? 0 :
                weaponSurplusMoney.toDoubleCents()).append(DELIMETER_FIELDS);

        sb.append(DELIMETER_FIELDS);
        sb.append("additionalData").append(DELIMETER_VALUE_PARAM).append(additionalData);
        sb.append(DELIMETER_FIELDS);
        sb.append("maxShotTotalWin").append(DELIMETER_VALUE_PARAM).append(maxShotTotalWin);
        sb.append(DELIMETER_FIELDS);

        Pair<Double, String> realShotData = getRealShotData();
        sb.append("realWeapon").append(DELIMETER_VALUE_PARAM).append(realShotData.getKey())
                .append(DELIMETER_PARAM_FIELD).append(realShotData.getValue());
        sb.append(DELIMETER_FIELDS);


        Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins = getAdditionalWins();
        if (!additionalWins.isEmpty()) {
            StringBuilder sbAdditionalWins = new StringBuilder();
            additionalWins.forEach((s, pair) -> sbAdditionalWins.append(s).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(pair.getKey()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(pair.getValue()).append(DELIMETER_PARAM_FIELD));

            sb.append("additionalWins").append(DELIMETER_VALUE_PARAM).append(sbAdditionalWins);
        } else {
            sb.append("additionalWins").append(DELIMETER_VALUE_PARAM).append("no additional wins");
        }
        sb.append(DELIMETER_FIELDS);

        StringBuilder hitMissStat = new StringBuilder();
        getHitMissStatByWeapons().forEach((weaponId, data) ->
                hitMissStat.append(weaponId).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfRealShots()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfMiss()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfKilledMiss()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfCompensateHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfMathHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(DELIMETER_PARAM_FIELD));
        sb.append("hitMiss").append(DELIMETER_VALUE_PARAM).append(hitMissStat).append(DELIMETER_FIELDS);


        StringBuilder wStatBySources = new StringBuilder();
        getWeaponStatBySources().forEach((s, weaponSourceIntegerMap) -> {
            wStatBySources.append(s).append(DELIMETER_VALUE_PARAM_SECOND);
            weaponSourceIntegerMap.forEach((source, integer) ->
                    wStatBySources.append(source.getTitle())
                            .append(DELIMETER_VALUE_PARAM)
                            .append(integer)
                            .append(DELIMETER_VALUE_PARAM_SECOND));
            wStatBySources.append(DELIMETER_PARAM_FIELD);
        });
        sb.append("wStatBySources").append(DELIMETER_VALUE_PARAM).append(wStatBySources).append(DELIMETER_FIELDS);
        sb.append("moneyWheelCompleted").append(DELIMETER_VALUE_PARAM).append(moneyWheelCompleted).append(DELIMETER_FIELDS);
        sb.append("moneyWheelPayouts").append(DELIMETER_VALUE_PARAM).append(moneyWheelPayouts).append(DELIMETER_FIELDS);

        double vbaPlayerBets = totalBets.toDoubleCents() + totalBetsSpecialWeapons.toDoubleCents();

        Double betsByStat = realShotData.getKey();

        double diff = betsByStat - vbaPlayerBets;
        if (diff != 0) {
            LOGGER.debug("vbaPlayerBets: " + vbaPlayerBets);
            LOGGER.debug("betsByStat: " + betsByStat);
            LOGGER.debug("diff : " + diff);
        }
        return sb.toString();
    }


    @Override
    protected void fillPlayerRoundInfo(AbstractPlayerRoundInfo resA, String data, double bets, double wins) {
        String[] params = data.split(DELIMETER_FIELDS);
        int idx = 0;
        if (data.isEmpty()) {
            return;
        }
        AbstractActionGamePlayerRoundInfo res = (AbstractActionGamePlayerRoundInfo) resA;
        res.setTimeOfRoundEnd(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setShotsCount(Integer.parseInt(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setXpearned(new Experience(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1])));
        res.setPlayerRoundId(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setRoomRoundId(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setRoomId(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setAmmoAmountBuyIn(Integer.parseInt(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setAmmoAmountReturned(Integer.parseInt(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setRoomStake(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        Money betsSpecialWeapons = getMoney(Double.parseDouble(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setTotalBetsSpecialWeapons(betsSpecialWeapons);
        res.setTotalBets(getMoney(bets).subtract(betsSpecialWeapons));
        Map<String, com.betsoft.casino.mp.common.EnemyStat> statByEnemies = new HashMap<>();


        for (String param : params) {
            if (param.contains("name=")) {
                String[] enemyParams = params[idx++].split(DELIMETER_PARAM_FIELD);
                String name = enemyParams[0].split(DELIMETER_VALUE_PARAM)[1];
                boolean isHV = enemyParams[5].split(DELIMETER_VALUE_PARAM)[1].equals("true");
                com.betsoft.casino.mp.common.EnemyStat enemyStat = new com.betsoft.casino.mp.common.EnemyStat(isHV,
                        (int) GameType.CLASH_OF_THE_GODS.getGameId());
                enemyStat.setCntShotsToEnemy(Integer.parseInt(enemyParams[1].split(DELIMETER_VALUE_PARAM)[1]));
                enemyStat.setPayouts(getMoney(Double.parseDouble(enemyParams[2].split(DELIMETER_VALUE_PARAM)[1])));
                enemyStat.setCntKills(Integer.parseInt(enemyParams[3].split(DELIMETER_VALUE_PARAM)[1]));
                enemyStat.setHvBets(getMoney(Double.parseDouble(enemyParams[4].split(DELIMETER_VALUE_PARAM)[1])));
                enemyStat.setMainBets(getMoney(Double.parseDouble(enemyParams[6].split(DELIMETER_VALUE_PARAM)[1])));
                enemyStat.setSpecialWeaponsStats(parseSpecialWeapons(enemyParams, 7));
                statByEnemies.put(name, enemyStat);
            } else if (param.contains("additionalWins")) {
                String[] additionalWins = param.split("=")[1].split(DELIMETER_PARAM_FIELD);
                Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins_ = new HashMap<>();
                for (String additionalWin : additionalWins) {
                    String[] split = additionalWin.split(DELIMETER_VALUE_PARAM_SECOND);
                    String description = split[0];
                    if (split.length >= 2) {
                        AtomicInteger number = new AtomicInteger(Integer.parseInt(split[1]));
                        AtomicDouble win = new AtomicDouble(Double.parseDouble(split[2]));
                        additionalWins_.put(description, new Pair<>(number, win));
                    }
                }
                res.setAdditionalWins(additionalWins_);

            }
        }
        res.setStatByEnemies(statByEnemies);


        res.setWeaponSurplusVBA(params[idx++]);
        Money weaponSurplusMoney = getMoney(Double.parseDouble(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setWeaponSurplusMoney(weaponSurplusMoney);
        res.setTotalPayouts(getMoney(wins).subtract(weaponSurplusMoney));
        addLootBoxAndHitMiss(params);
        addAdditionalStatsBySources(params);

    }

    @Override
    protected void addSpecialWeapons(StringBuilder sb, Map<String, WeaponStat> weaponsStats) {
        for (Map.Entry<String, WeaponStat> weaponStatEntry : weaponsStats.entrySet()) {
            String nameWeapon = weaponStatEntry.getKey();
            WeaponStat wStats = weaponStatEntry.getValue();
            boolean emptyWeapon = wStats.getCnt() == 0
                    && wStats.getCntHits() == 0
                    && wStats.getPayBets().equals(Money.ZERO)
                    && wStats.getPayouts().equals(Money.ZERO);
            if (emptyWeapon)
                continue;

            sb.append("w_").append(nameWeapon).append(DELIMETER_VALUE_PARAM)
                    .append(wStats.getPayouts().toDoubleCents()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(wStats.getCntHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(wStats.getCnt()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(wStats.getPayBets().toDoubleCents()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(DELIMETER_PARAM_FIELD);
        }
    }

    public String getRTPStatData(int gameId) {
        StringBuilder sb = new StringBuilder();
        double totalMainEnemiesBet = 0;
        double totalMainEnemiesWin = 0;
        double totalSpecialWin = 0;
        double totalBetsSpecialWeapons = this.getTotalBetsSpecialWeapons().toDoubleCents();

        // Boss
        double totalSpecialBossWins = 0;
        double totalMainBossWin = 0;
        double totalBossBet;

        Map<String, SWEntry> weaponStats = new HashMap<>();
        getWeaponTitles(gameId).forEach(title -> weaponStats.put(title, new SWEntry()));

        Map<String, EnemyStat> statByEnemies = getStatByEnemies();
        for (Map.Entry<String, EnemyStat> statEntry : statByEnemies.entrySet()) {
            com.betsoft.casino.mp.common.EnemyStat enemyStat = statEntry.getValue();
            totalMainEnemiesBet += enemyStat.getMainBets().toDoubleCents();
        }

        int cntShotsFromSpecialWeapons = 0;

        sb.append("\n------------------enemies-----------------------\n");
        for (Map.Entry<String, EnemyStat> statEntry : statByEnemies.entrySet()) {
            EnemyStat value = statEntry.getValue();
            long countSWeapon = value.getSpecialWeaponsStats().values().stream()
                    .filter(weaponStat -> (weaponStat.getCnt() > 0 || weaponStat.getPayBets().greaterThan(Money.ZERO))).count();
            if(value.getPayouts().greaterThan(Money.ZERO) || value.getMainBets().greaterThan(Money.ZERO)
             || value.getCntShotsToEnemy() > 0 || countSWeapon > 0) {
                sb.append(String.format("%30s", statEntry.getKey()));
                com.betsoft.casino.mp.common.EnemyStat enemyStat = statEntry.getValue();
                totalMainEnemiesWin += enemyStat.getPayouts().toDoubleCents();
                sb.append("\tMain RTP: ").append(enemyStat.getPayouts().toDoubleCents() / totalMainEnemiesBet).append("\t");
                double[] res = update(enemyStat.getSpecialWeaponsStats(), weaponStats);
                totalSpecialWin += res[0];
                cntShotsFromSpecialWeapons += res[1];
                sb.append("\tSpecial RTP: ").append(res[0] / totalMainEnemiesBet).append("\t");
                sb.append("\n");
            }
        }

        double[] res = update(getStatBoss().getSpecialWeaponsStats(), weaponStats);
        totalSpecialBossWins += res[0];
        cntShotsFromSpecialWeapons += res[1];

        sb.append("\n------------------weapons-----------------------");
        for (Map.Entry<String, SWEntry> entryEntry : weaponStats.entrySet()) {
            sb.append("\n");
            SWEntry value = entryEntry.getValue();
            sb.append(String.format("%30s", entryEntry.getKey())).append("\t").append(" payout: " + value.getPayout()).
                    append(" cnt: ").append(value.getCnt()).append("  avg: " + value.getPayout() / value.getCnt()).append("\t");
        }

        sb.append("\n------------------common stats-----------------------\n");
        sb.append("totalBetsSpecialWeapons: " + totalBetsSpecialWeapons).append("\n");
        sb.append("totalMainEnemiesBet: " + totalMainEnemiesBet).append("\n");

        Money weaponSurplusMoney = getWeaponSurplusMoney();
        double tBet = totalMainEnemiesBet + totalBetsSpecialWeapons;


        Pair<AtomicInteger, AtomicDouble> multiplier = getAdditionalWins().get("Multiplier");
        Pair<AtomicInteger, AtomicDouble> killAwardWin = getAdditionalWins().get("KillAwardWin");

        double mult = multiplier == null ? 0 : multiplier.getValue().get();
        double killBonus = killAwardWin == null ? 0 : killAwardWin.getValue().get();

        long moneyWheelPayouts = getMoneyWheelPayouts();
        double tWin = totalMainEnemiesWin + totalSpecialWin + totalMainBossWin + totalSpecialBossWins
                 + weaponSurplusMoney.toDoubleCents() + killBonus + mult;

        double tWin2 = totalMainEnemiesWin + totalSpecialWin + totalMainBossWin + totalSpecialBossWins
                 + killBonus + mult;

        double tWin3 = totalMainEnemiesWin
                + totalSpecialWin
                + totalMainBossWin
                + totalSpecialBossWins
                + weaponSurplusMoney.toDoubleCents()
                + mult
                + killBonus
                + moneyWheelPayouts;

        sb.append("tBet: ").append(tBet).append("\n");
        sb.append("tWin: ").append(tWin).append("\n");
        sb.append("tWin2: ").append(tWin2).append("\n");
        sb.append("tWin3 (with multiplier and money wheel): ").append(tWin3).append("\n");

        if (mult != 0) {
            sb.append("  multiplier cnt: ").append(multiplier.getKey()).append(", win:  ").append(multiplier.getValue()).append("\n");
            sb.append("  multiplier RTP: " + mult / tBet).append("\n");
        }

        if (killBonus > 0) {
            sb.append("  KillAwardWin cnt: ").append(killAwardWin.getKey()).append(", win:  ").append(killAwardWin.getValue()).append("\n");
            sb.append("  KillAwardWin RTP: " + killBonus / tBet).append("\n");
        }

        sb.append("-------------------------Main Enemy-------------------------").append("\n");
        sb.append("totalMainEnemiesWin: " + totalMainEnemiesWin).append("\n");
        sb.append("Main enemies RTP: " + totalMainEnemiesWin / tBet).append("\n");

        sb.append("-------------------------Special weapons-------------------------").append("\n");
        sb.append("totalMainEnemiesWin: " + totalMainEnemiesWin).append("\n");
        sb.append("Main enemies RTP: " + totalMainEnemiesWin / tBet).append("\n");

        sb.append("-------------------------Money wheel-------------------------").append("\n");
        sb.append("Wheel win: " + moneyWheelPayouts).append("\n");
        sb.append("Wheel RTP: " + moneyWheelPayouts / tBet).append("\n");


        sb.append("-------------------------All RTP with wheel-------------------------").append("\n");
        sb.append("Win: " + tWin3).append("\n");
        sb.append("RTP: " + tWin3 / tBet).append("\n");

        sb.append("Total RTP: ").append(tWin / tBet).append("\n");
        sb.append("Total RTP: (with multiplier, Explore) ").append(tWin3 / tBet).append("\n");
        sb.append("Total RTP (without compensation): ").append(tWin2 / tBet).append("\n");


        return sb.toString();
    }

    private void addAdditionalStatsBySources(String[] params) {
        //wStatBySources=Cryogun,lootbox=1,enemy=2&
        Map<String, Map<WeaponSource, Integer>> weaponStatBySources = getWeaponStatBySources();
        for (String param : params) {
            if (param.contains("wStatBySources")) {
                String[] wStat = param.substring(15).split("&");
                for (String w : wStat) {
                    String[] split = w.split(",");
                    String weaponTitle = split[0];
                    Map<WeaponSource, Integer> stat = weaponStatBySources.getOrDefault(weaponTitle, new HashMap<>());
                    for (int i = 1; i < split.length; i++) {
                        String[] p = split[i].split("=");
                        String source = p[0];
                        int shots = Integer.parseInt(p[1]);
                        WeaponSource key = WeaponSource.getSourceByTitle(source);
                        Integer old = stat.getOrDefault(key, 0);
                        stat.put(key, old + shots);
                    }
                    weaponStatBySources.putIfAbsent(weaponTitle, stat);
                }
            }

            if (param.contains("freeShotsWon")) {
                addFreeShotsWon(Integer.parseInt(param.split(DELIMETER_VALUE_PARAM)[1]));
            }

            if (param.contains("moneyWheelCompleted")) {
                addMoneyWheelCompleted(Integer.parseInt(param.split(DELIMETER_VALUE_PARAM)[1]));
            }

            if (param.contains("moneyWheelPayouts")) {
                addMoneyWheelPayouts(Integer.parseInt(param.split(DELIMETER_VALUE_PARAM)[1]));
            }

        }
    }

    private void addLootBoxAndHitMiss(String[] params) {
        //lootBox=150&w_Artillery Strike,530,3180,&;hitMiss=7,3180,1096,37064,0,&;
        for (String param : params) {
            if (param.contains("hitMiss")) {
                String[] split = param.substring("hitMiss=".length()).split("&");
                for (String w : split) {
                    String[] wParam = w.split(",");
                    if (wParam.length >= 6) {
                        int weaponId = Integer.parseInt(wParam[0]);
                        int realShots = Integer.parseInt(wParam[1]);
                        int hits = Integer.parseInt(wParam[2]);
                        int miss = Integer.parseInt(wParam[3]);
                        int killMissed = Integer.parseInt(wParam[4]);
                        int returnedHits = Integer.parseInt(wParam[5]);
                        int numberOfMathHits = Integer.parseInt(wParam[6]);
                        AdditionalWeaponStat stat = getHitMissStatByWeapons().getOrDefault(weaponId, new AdditionalWeaponStat());
                        stat.addValues(realShots, hits, miss, killMissed, returnedHits, numberOfMathHits);
                        getHitMissStatByWeapons().put(weaponId, stat);
                    }
                }
            }
        }
    }


    @Override
    public void addRoundInfo(AbstractPlayerRoundInfo<EnemyType, EnemyStat, PlayerRoundInfo> newRoundInfo) {
        super.addRoundInfo(newRoundInfo);
        newRoundInfo.getHitMissStatByWeapons().forEach((weaponId, aws) -> {
            Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons = this.getHitMissStatByWeapons();
            AdditionalWeaponStat weaponStat = hitMissStatByWeapons.getOrDefault(weaponId, new AdditionalWeaponStat());
            weaponStat.addValues(aws.getNumberOfRealShots(), aws.getNumberOfHits(), aws.getNumberOfMiss(),
                    aws.getNumberOfKilledMiss(), aws.getNumberOfCompensateHits(), aws.getNumberOfMathHits());
            hitMissStatByWeapons.put(weaponId, weaponStat);
        });
    }

    @Override
    public void checkPay(Money checkPay) {
    }

    @Override
    protected PlayerRoundInfo getDeserialized() {
        return this;
    }
}
