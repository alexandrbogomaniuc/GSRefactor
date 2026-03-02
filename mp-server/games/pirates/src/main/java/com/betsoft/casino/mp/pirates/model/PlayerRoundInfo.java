package com.betsoft.casino.mp.pirates.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.Experience;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.pirates.model.math.EnemyRange;
import com.betsoft.casino.mp.pirates.model.math.EnemyType;
import com.betsoft.casino.mp.pirates.model.math.MathData;
import com.abs.casino.common.util.Pair;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("rawtypes")
public class PlayerRoundInfo extends AbstractActionGamePlayerRoundInfo<EnemyType, EnemyStat, PlayerRoundInfo> {

    public PlayerRoundInfo() {
        super();
    }

    public PlayerRoundInfo(long roomId) {
        super(roomId, (int) GameType.PIRATES.getGameId());
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
        return new ArrayList<>(EnemyRange.BaseEnemies.getEnemies());
    }

    public static PlayerRoundInfo getPlayerRoundInfoFromVBAData(String data, double bets, double wins) {
        PlayerRoundInfo res = new PlayerRoundInfo(0);
        res.fillPlayerRoundInfo(res, data, bets, wins);
        return res;
    }


    @Override
    protected void fillPlayerRoundInfo(AbstractPlayerRoundInfo resA, String data, double bets, double wins) {
        String[] params = data.split(DELIMETER_FIELDS);
        int idx = 0;
        if(data.isEmpty()) {
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

        for (int i = 0; i < res.getBaseEnemies().size(); i++) {
            String[] enemyParams = params[idx++].split(DELIMETER_PARAM_FIELD);
            String name = enemyParams[0].split(DELIMETER_VALUE_PARAM)[1];
            boolean isHV = enemyParams[5].split(DELIMETER_VALUE_PARAM)[1].equals("true");
            com.betsoft.casino.mp.common.EnemyStat enemyStat = new com.betsoft.casino.mp.common.EnemyStat(isHV, 808);
            enemyStat.setCntShotsToEnemy(Integer.parseInt(enemyParams[1].split(DELIMETER_VALUE_PARAM)[1]));
            enemyStat.setPayouts(getMoney(Double.parseDouble(enemyParams[2].split(DELIMETER_VALUE_PARAM)[1])));
            enemyStat.setCntKills(Integer.parseInt(enemyParams[3].split(DELIMETER_VALUE_PARAM)[1]));
            enemyStat.setHvBets(getMoney(Double.parseDouble(enemyParams[4].split(DELIMETER_VALUE_PARAM)[1])));
            enemyStat.setMainBets(getMoney(Double.parseDouble(enemyParams[6].split(DELIMETER_VALUE_PARAM)[1])));
            enemyStat.setSpecialWeaponsStats(parseSpecialWeapons(enemyParams, 7));
            statByEnemies.put(name, enemyStat);
        }
        res.setStatByEnemies(statByEnemies);

        String bossStats = params[idx++];
        String[] bossParams = bossStats.split(DELIMETER_PARAM_FIELD);
        int bossCnt = Integer.parseInt(bossParams[0].split(DELIMETER_VALUE_PARAM)[1]);
        int bossCntKilled = Integer.parseInt(bossParams[1].split(DELIMETER_VALUE_PARAM)[1]);
        Money bossBetsExtra = getMoney(Double.parseDouble(bossParams[2].split(DELIMETER_VALUE_PARAM)[1]));
        Money bossPayoutsExtra = getMoney(Double.parseDouble(bossParams[3].split(DELIMETER_VALUE_PARAM)[1]));
        Money bossPayoutsMain = getMoney(Double.parseDouble(bossParams[4].split(DELIMETER_VALUE_PARAM)[1]));
        res.setStatBoss(new BossStat(bossBetsExtra, bossPayoutsExtra, bossPayoutsMain, bossCnt, bossCntKilled,
                parseSpecialWeapons(bossParams, 5)));

        res.setWeaponSurplusVBA(params[idx++]);
        Money weaponSurplusMoney = getMoney(Double.parseDouble(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setWeaponSurplusMoney(weaponSurplusMoney);
        res.setTotalPayouts(getMoney(wins).subtract(bossPayoutsMain).subtract(weaponSurplusMoney));
        idx++; // skip old field
        idx++; // skip leaderbords
        if (params.length > idx) {
            String[] quests = params[idx++].split(DELIMETER_VALUE_PARAM)[1].split("&");
            res.questCompletedCount = Integer.parseInt(quests[0]);
            res.questPayouts =  getMoney(Double.parseDouble(quests[1]));
        }

        if (params.length >= 40) {
            String paramWins = params[40].split(DELIMETER_VALUE_PARAM)[1];
            if (!paramWins.contains("no additional wins")) {
                String[] additionalWins = paramWins.split(DELIMETER_PARAM_FIELD);
                Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins_ = new HashMap<>();
                for (String additionalWin : additionalWins) {
                    String[] split = additionalWin.split(DELIMETER_VALUE_PARAM_SECOND);
                    String description = split[0];
                    AtomicInteger number = new AtomicInteger(Integer.parseInt(split[1]));
                    AtomicDouble win = new AtomicDouble(Double.parseDouble(split[2]));
                    additionalWins_.put(description, new Pair<>(number, win));
                }
                res.setAdditionalWins(additionalWins_);
            }
        }

        if (params.length > 36) {
            String[] quests = params[36].split(DELIMETER_VALUE_PARAM)[1].split("&");
            res.questCompletedCount = Integer.parseInt(quests[0]);
            res.questPayouts = getMoney(Double.parseDouble(quests[1]));
        }

        addLootBoxAndHitMiss(params);
        addWeaponStatsBySources(params);
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
            com.betsoft.casino.mp.common.EnemyStat enemyStat =  statEntry.getValue();
            totalMainEnemiesBet += enemyStat.getMainBets().toDoubleCents();
        }

        int cntShotsFromSpecialWeapons = 0;

        sb.append("\n------------------enemies-----------------------\n");
        for (Map.Entry<String, EnemyStat> statEntry : statByEnemies.entrySet()) {
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
        BossStat statBoss = getStatBoss();
        totalMainBossWin += statBoss.getPayoutsMain().toDoubleCents();
        totalBossBet  = statBoss.getBetsExtra().toDoubleCents();
        int cntKillsOfBoss = statBoss.getCntKillsOfBoss();

        sb.append("totalBossBet: " + totalBossBet).append("\n");
        sb.append("totalSpecialBossWins: " + totalSpecialBossWins).append("\n");
        sb.append("totalBetsSpecialWeapons: " + totalBetsSpecialWeapons).append("\n");
        sb.append("totalMainEnemiesBet: " + totalMainEnemiesBet).append("\n");

        Money questPayouts = getQuestPayouts();
        Money weaponSurplusMoney = getWeaponSurplusMoney();

        double tBet = totalMainEnemiesBet + totalBetsSpecialWeapons + totalBossBet;


        Pair<AtomicInteger, AtomicDouble> multiplier = getAdditionalWins().get("Multiplier");
        Pair<AtomicInteger, AtomicDouble> killAwardWin = getAdditionalWins().get("KillAwardWin");
        Pair<AtomicInteger, AtomicDouble> totalGemsPayout = getAdditionalWins().get("totalGemsPayout");

        double mult = multiplier == null ? 0 : multiplier.getValue().get();
        double killBonus = killAwardWin == null ? 0 :  killAwardWin.getValue().get();
        double gemsPayout = totalGemsPayout == null ? 0 :  totalGemsPayout.getValue().get();


        double tWin = totalMainEnemiesWin + totalSpecialWin + totalMainBossWin + totalSpecialBossWins
                + questPayouts.toDoubleCents() + weaponSurplusMoney.toDoubleCents() + killBonus + mult + gemsPayout;

        double tWin2 = totalMainEnemiesWin + totalSpecialWin + totalMainBossWin + totalSpecialBossWins
                + questPayouts.toDoubleCents() + killBonus + mult + gemsPayout;

        double tWin3 = totalMainEnemiesWin
                + totalSpecialWin
                + totalMainBossWin
                + totalSpecialBossWins
                + questPayouts.toDoubleCents()
                + weaponSurplusMoney.toDoubleCents()
                + mult
                + killBonus
                + gemsPayout;

        sb.append("tBet: ").append(tBet).append("\n");
        sb.append("tWin: ").append(tWin).append("\n");
        sb.append("tWin2: ").append(tWin2).append("\n");
        sb.append("tWin3 (with multiplier): ").append(tWin3).append("\n");

        if (mult != 0) {
            sb.append("  multiplier cnt: ").append(multiplier.getKey()).append(", win:  ").append(multiplier.getValue()).append("\n");
            sb.append("  multiplier RTP: " + mult / tBet).append("\n");
        }

        if(killBonus > 0) {
            sb.append("  KillAwardWin cnt: ").append(killAwardWin.getKey()).append(", win:  ").append(killAwardWin.getValue()).append("\n");
            sb.append("  KillAwardWin RTP: " + killBonus / tBet).append("\n");
        }

        sb.append("-------------------------Main Enemy-------------------------").append("\n");
        sb.append("totalMainEnemiesWin: " + totalMainEnemiesWin).append("\n");
        sb.append("Main enemies RTP: " + totalMainEnemiesWin/tBet).append("\n");

        sb.append("-------------------------Special weapons-------------------------").append("\n");
        sb.append("totalMainEnemiesWin: " + totalMainEnemiesWin).append("\n");
        sb.append("Main enemies RTP: " + totalMainEnemiesWin/tBet).append("\n");


        sb.append("-------------------------Quest-------------------------").append("\n");
        sb.append("questCompletedCount: "  + getQuestCompletedCount()).append("\n");
        sb.append("questPayouts: "  + questPayouts).append("\n");
        sb.append("quest RTP: " + questPayouts.toDoubleCents()/tBet).append("\n");

        sb.append("-------------------------Boss-------------------------").append("\n");;
        sb.append("cntKillsOfBoss: " + cntKillsOfBoss).append("\n");
        sb.append("totalMainBossWin: " + totalMainBossWin).append("\n");
        sb.append("average win of Boss: " + totalMainBossWin/cntKillsOfBoss).append("\n");
        sb.append("Boss main RTP: " + totalMainBossWin/tBet).append("\n");
        sb.append("Boss gemsPayout RTP: " + gemsPayout/tBet).append("\n");

        sb.append("Total RTP: ").append(tWin / tBet).append("\n");
        sb.append("Total RTP: (with multiplier, Explore) ").append(tWin3 / tBet).append("\n");
        sb.append("Total RTP (without compensation): ").append(tWin2 / tBet).append("\n");


        return sb.toString();
    }

    private void addWeaponStatsBySources(String[] params) {
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
        }
    }

    private void addLootBoxAndHitMiss(String[] params) {
        //lootBox=150&w_Artillery Strike,530,3180,&;hitMiss=7,3180,1096,37064,0,&;
        for (String param : params) {
            if (param.contains("lootBox")) {
                String[] split = param.split("&");
                int cost = Integer.parseInt(split[0].split("=")[1]);
                for (int i = 1; i < split.length; i++) {
                    String weapon = split[i];
                    String[] wParams = weapon.split(",");
                    String name = wParams[0].substring(2);
                    int numberBuyIns = Integer.parseInt(wParams[1]);
                    int shots = Integer.parseInt(wParams[2]);
                    int index = MathData.WEAPON_LOOT_BOX_PRICES.indexOf(cost);
                    LootBoxStat stat = getLootBoxStat().getOrDefault(index, new LootBoxStat(cost));
                    stat.addWeaponShots(name, shots, numberBuyIns);
                    getLootBoxStat().put(index, stat);
                }
            }

            if (param.contains("hitMiss")) {
                String[] split = param.substring("hitMiss=".length()).split("&");
                for (String w : split) {
                    String[] wParam = w.split(",");
                    if(wParam.length >= 6) {
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
            weaponStat.addValues(aws.getNumberOfRealShots(),aws.getNumberOfHits(), aws.getNumberOfMiss(),
                    aws.getNumberOfKilledMiss(), aws.getNumberOfCompensateHits(), aws.getNumberOfMathHits());
            hitMissStatByWeapons.put(weaponId, weaponStat);
        });

        newRoundInfo.getLootBoxStat().forEach((idx, lootBoxStat) -> {
            Map<Integer, LootBoxStat> boxStat = this.getLootBoxStat();
            LootBoxStat stat = boxStat.getOrDefault(idx, new LootBoxStat(lootBoxStat.getCost()));
            lootBoxStat.getWeaponShots().forEach((s, pair) -> stat.addWeaponShots(s, pair.getValue(), pair.getKey()));
            boxStat.put(idx, stat);
        });

    }

    @Override
    protected PlayerRoundInfo getDeserialized() {
        return this;
    }
}
