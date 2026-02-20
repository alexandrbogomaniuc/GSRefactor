package com.betsoft.casino.mp.dragonstone;

import com.betsoft.casino.mp.model.GameType;
import com.dgphoenix.casino.common.exception.CommonException;

import java.util.*;

public class TestRTP {
    static Long gameId = GameType.DRAGONSTONE.getGameId();
    static int DEFAULT_WEAPON_ID = -1;
    private static final List<Integer> WEAPON_LOOT_BOX_PRICES = Arrays.asList(150, 300, 450);

    public static void main(String[] args) throws CommonException {
        List<TestParam> configs = new ArrayList<>();
//        configs.add(new TestParam(100000, false, true, EnemyType.ANT));
//        configs.add(new TestParam(100000, false, false, EnemyType.ANT));
//        configs.add(new TestParam(200000, true, false, EnemyType.ANT));
//        configs.add(new TestParam(200000, true, true, EnemyType.ANT));
//        configs.add(new TestParam(200000, true, false, EnemyType.ANT, 0.1));
//        configs.add(new TestParam(200000, true, true, EnemyType.ANT, 0.1));

        for (TestParam config : configs) {
            //new TestRTP().doTestEnemy(config);
        }

    }

/*    private void doTestEnemy(TestParam testParam) throws CommonException {
        System.out.println("--------------------------------------Test params: " + testParam + " -----------------------");
        double roomStake_ = 1.;
        Money stake = Money.fromCents((long) (roomStake_ * 100));
        RoomPlayerInfo roomPlayerInfo = new RoomPlayerInfo(1, 271,
                2,
                1,
                "sid_123123", 11, 1000000,
                "testUser_" + 1,
                new Avatar(0, 1, 2),
                System.currentTimeMillis(),
                new Currency("USD", "$"),
                new PlayerStats(), false,
                null, null,
                null, (long) (roomStake_ * 100),
                10,
                0.02, MaxQuestWeaponMode.LOOT_BOX, false);

        System.out.println("");
        GameSocketClient client = new GameSocketClient(1l, 271L, "", getConnection(),
                new GsonMessageSerializer(new Gson()), GameType.AMAZON);
        Seat seat = new Seat(roomPlayerInfo, client, 1);

        EnemyType enemyType = testParam.getEnemyType();
        EnemyClass enemyClass = new EnemyClass(0, (short) 1, (short) 1, "", 20, 12, new ArrayList<>(), enemyType);
        MathEnemy mathEnemy = createMathEnemy(enemyType, 0);

        double totalBaseBet = 0;
        double totalBetBuySpecialWeapons = 0;
        long numberOfBuyInSpecialWeapons = 0;
        double totalWin = 0;
        double totalWinKillBonusAward = 0;
        int lastPercent = 0;
        double totalGemWins = 0;
        long cntInstantKill = 0;
        long cntKilledEnemy = 0;
        seat.setWeapon(-1);

        Map<Integer, AtomicDouble> winsWeapons = new HashMap<>();
        Map<Integer, AtomicDouble> winsKillBonus = new HashMap<>();
        Map<Integer, AtomicLong> shotsWeapons = new HashMap<>();

        for (SpecialWeaponType value : SpecialWeaponType.values()) {
            if (value.getAvailableGameIds().contains(gameId.intValue())) {
                winsWeapons.put(value.getId(), new AtomicDouble(0));
                winsKillBonus.put(value.getId(), new AtomicDouble(0));
                shotsWeapons.put(value.getId(), new AtomicLong(0));
            }
        }

        winsWeapons.put(DEFAULT_WEAPON_ID, new AtomicDouble(0));
        winsKillBonus.put(DEFAULT_WEAPON_ID, new AtomicDouble(0));
        shotsWeapons.put(DEFAULT_WEAPON_ID, new AtomicLong(0));

        long cntTests = testParam.getCnt();

        for (int i = 0; i < cntTests; i++) {

            EnemyGame enemyGame = new EnemyGame(new EmptyLogger(), null);
            Enemy enemy = new Enemy(0, enemyClass, 1, null, null, -1, new ArrayList<>());
            enemy.setEnergy(mathEnemy.getFullEnergy());

            while (true) {
                int currentWeaponId = seat.getCurrentWeaponId();
                boolean isSpecialWeapon = currentWeaponId != DEFAULT_WEAPON_ID;
                int randomDamageForWeapon = isSpecialWeapon ?
                        MathData.getRandomDamageForWeapon(seat.getCurrentWeaponId()) : 1;

                ShootResult shootResult = enemyGame.doShoot(enemy, seat, stake, false, false, 1);

                if(!isSpecialWeapon)
                    totalBaseBet += shootResult.getBet().toDoubleCents();

                totalGemWins += shootResult.getTotalGemsPayout().toDoubleCents();

                if (isSpecialWeapon)
                    seat.consumeSpecialWeapon(currentWeaponId);

                addWeaponToSeat(seat, shootResult);

                double baseWin = shootResult.getWin().toDoubleCents() * randomDamageForWeapon;
                double killWin = shootResult.getKillAwardWin().toDoubleCents() * randomDamageForWeapon;
                totalWin+= (baseWin + killWin + totalGemWins);

                winsWeapons.get(currentWeaponId).addAndGet(baseWin);
                winsKillBonus.get(currentWeaponId).addAndGet(killWin);
                shotsWeapons.get(currentWeaponId).incrementAndGet();

                seat.setWeapon(DEFAULT_WEAPON_ID);
                choiceWeapon(seat);

                if((seat.getCurrentWeaponId() == -1 && testParam.isCanBuyWeapon()) ||
                        RNG.rand() < testParam.getProbBuySpecialWeapon()){
                    Money weaponLootBoxPrice = getWeaponLootBoxPrice(seat, 2);
                    totalBetBuySpecialWeapons += weaponLootBoxPrice.toDoubleCents();
                    generateWeaponLootBox(seat,-1, 2 , 0, weaponLootBoxPrice);
                    choiceWeapon(seat);
                    numberOfBuyInSpecialWeapons++;
                }

                if(!testParam.isCanShotFromSpecialWeapon())
                    seat.setWeapon(DEFAULT_WEAPON_ID);

                if (shootResult.isInstanceKill()) cntInstantKill++;

                int percent = (int) (100 - ((double) (cntTests - i) / cntTests) * 100);
                if (percent != lastPercent && percent % 25 == 0) {
                    double rtp = totalWin / (totalBaseBet + totalBetBuySpecialWeapons);
                    System.out.println("processing " + percent + "%"
                            + " RTP: " + rtp
                            + " totalWin: " + totalWin
                            + " totalGemWins: " + totalGemWins
                            + " totalBetBuySpecialWeapons: " + totalBetBuySpecialWeapons
                            + " numberOfBuyInSpecialWeapons: " + numberOfBuyInSpecialWeapons
                    );
                    lastPercent = percent;
                }
                if (shootResult.isDestroyed()) {
                    cntKilledEnemy++;
                    break;
                }
            }
        }


        double winByWeapons = 0;
        for (Map.Entry<SpecialWeaponType, Weapon> weapon : seat.getWeapons().entrySet()) {
            int weaponId = weapon.getValue().getType().getId();
            Double rtpForWeapon = MathData.getRtpForWeapon(weaponId) / 100;
            double multiplier = rtpForWeapon * weapon.getValue().getShots()
                    * MathData.getAverageDamageForWeapon(weaponId);
            winByWeapons += stake.getWithMultiplier(multiplier).toDoubleCents();
        }



        System.out.println("cntKilledEnemy: " + cntKilledEnemy);
        System.out.println("averagePayout: " + totalWin / cntKilledEnemy);

        System.out.println("totalBetBuySpecialWeapons: " + totalBetBuySpecialWeapons);
        System.out.println("seat.getWeapons(): " + seat.getWeapons());
        System.out.println("totalBaseBet: " + totalBaseBet);
        System.out.println("totalWin: " + totalWin);
        System.out.println("totalWinKillBonusAward: " + totalWinKillBonusAward);
        System.out.println("winByWeapons: " + winByWeapons);
        System.out.println("cntInstantKill: " + cntInstantKill);

        double totalBet = totalBaseBet + totalBetBuySpecialWeapons;
        System.out.println("RTP base: " + totalWin / totalBet);
        System.out.println("RTP weapons: " + winByWeapons / totalBet);
        System.out.println("RTP kill bonus: " + totalWinKillBonusAward / totalBet);
        System.out.println("RTP gems bonus: " + totalGemWins / totalBet);

        System.out.println("RTP all: " + (totalWin + totalWinKillBonusAward + winByWeapons + totalGemWins) / totalBet);

        System.out.println("winByWeapons:  " + winByWeapons);
        System.out.println("winsKillBonus:  " + winsKillBonus);
        System.out.println("shotsWeapons:  " + shotsWeapons);

        System.out.println("totalBetBuySpecialWeapons: " + totalBetBuySpecialWeapons);
        System.out.println("numberOfBuyInSpecialWeapons: " + numberOfBuyInSpecialWeapons);

    }

    private void choiceWeapon(Seat seat) {
        seat.getWeapons().forEach((specialWeaponType, weapon) -> {
            if (weapon.getShots() > 0) seat.setWeapon(weapon.getType().getId());
        });
    }


    static MathEnemy createMathEnemy(EnemyType enemyType, int level) {
        if (enemyType.isBoss()) {
            return new MathEnemy(0, "", 0, MathData.getBossParams().get(level).second());
        } else {
            int[] levels = MathData.getEnemyData(enemyType.getId()).getLevels();
            int healthForFirstLevel = levels[level == -1 ? RNG.nextInt(levels.length) : level];
            return new MathEnemy(0, "", 0, healthForFirstLevel);
        }
    }


    private void addWeaponToSeat(Seat seat, ShootResult result) {
        result.getAwardedWeapons().forEach(weapon -> {
            com.betsoft.casino.mp.common.Weapon newWeapon = new com.betsoft.casino.mp.common.Weapon(weapon.getShots(), SpecialWeaponType.values()[weapon.getId()]);
            seat.addWeapon(newWeapon);
        });
    }


    public WeaponLootBox generateWeaponLootBox(Seat seat, int rid, int size, int usedAmmoAmount, Money usedMoney) {
        AtomicInteger sum = new AtomicInteger(0);
        List<WeaponLootBoxProb.WeaponEntry> weaponEntries = WeaponLootBoxProb.getTables200().get(0);
        weaponEntries.forEach(weaponEntry -> sum.addAndGet(weaponEntry.getWeight()));

        double[] prob = new double[weaponEntries.size()];
        for (int i = 0; i < prob.length; i++) {
            prob[i] = (double) weaponEntries.get(i).getWeight() / sum.get();
        }
        int indexFromDoubleProb = GameTools.getIndexFromDoubleProb(prob);
        Weapon weapon = getSpecialWeapon(weaponEntries.get(indexFromDoubleProb), size);
        return addWeaponLootBoxToSeat(seat, weapon, rid, size, usedAmmoAmount);
    }

    protected static List<Integer> getWeaponLootBoxPrices() {
        return WEAPON_LOOT_BOX_PRICES;
    }

    protected WeaponLootBox addWeaponLootBoxToSeat(Seat seat, Weapon weapon, int rid, int size, int usedAmmoAmount) {
        seat.addWeapon(weapon);
        return new WeaponLootBox(System.currentTimeMillis(), rid, weapon.getType().getId(), weapon.getShots(),
                seat.getBalance(), seat.getRoundWin().toFloatCents(), usedAmmoAmount);
    }


    private Weapon getSpecialWeapon(WeaponLootBoxProb.WeaponEntry weapon, int size) {
        SpecialWeaponType specialWeaponType = values()[weapon.getType()];
        int shots = weapon.getShots();
        if (size == 1)
            shots = 2 * shots;
        else if (size == 2) {
            shots = 4 * shots;
        }
        return new Weapon(shots, specialWeaponType);
    }

    public Money getWeaponLootBoxPrice(Seat seat, int size) {
        return seat.getStake().multiply(getWeaponLootBoxPrices().get(size));
    }*/


}
