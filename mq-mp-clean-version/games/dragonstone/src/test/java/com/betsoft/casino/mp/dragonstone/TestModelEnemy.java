package com.betsoft.casino.mp.dragonstone;

import com.betsoft.casino.mp.dragonstone.model.Enemy;
import com.betsoft.casino.mp.dragonstone.model.EnemyClass;
import com.betsoft.casino.mp.dragonstone.model.EnemyGame;
import com.betsoft.casino.mp.dragonstone.model.Seat;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.dragonstone.model.math.MathData;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.testmodel.EmptyLogger;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.betsoft.casino.mp.common.testmodel.StubSocketService.getConnection;

public class TestModelEnemy {

    public static void main(String[] args) throws CommonException {
//            new TestModelEnemy().doTestEnemy(EnemyType.SHAMAN, -1, 0);
//        new TestModelEnemy().doTestEnemy(EnemyType.Boss, -1, 0);
//        new TestModelEnemy().doTestEnemy(EnemyType.MULTIPLIER, -1, 0);
        //new TestModelEnemy().doTestEnemy(EnemyType.EXPLODER, SpecialWeaponType.ArtilleryStrike.getId(), 0);
       // new TestModelEnemy().doTestEnemy(EnemyType.EXPLODER, -1, 0);
    }

/*    private void doTestEnemy(EnemyType enemyType, int specialWeaponTypeId, int level) throws CommonException {
        int cnt = 10000;
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

        GameSocketClient client = new GameSocketClient(1l, 271L, "", getConnection(),
                new GsonMessageSerializer(new Gson()), GameType.AMAZON);
        Seat seat = new Seat(roomPlayerInfo, client, 1);

        EnemyClass enemyClass = new EnemyClass(0, (short) 1, (short) 1, "", 20, 12, new ArrayList<>(), enemyType);
        MathEnemy mathEnemy = createMathEnemy(enemyType, 0);

        double totalBet = 0;
        double totalWin = 0;
        double totalWinKillBonusAward = 0;
        int lastPercent = 0;
        double totalGemWins = 0;
        long cntInstantKill = 0;
        Map<Integer, Long> playerWeapons = new HashMap<>();
        for (SpecialWeaponType value : SpecialWeaponType.values()) {
            if (value.getAvailableGameIds().contains(821)) {
                playerWeapons.put(value.getId(), 0L);
            }
        }

        boolean needTestSpecialWeapon = specialWeaponTypeId != MathData.PISTOL_DEFAULT_WEAPON_ID;
        long cntKilledEnemy = 0;
        for (int i = 0; i < cnt; i++) {

            EnemyGame enemyGame = new EnemyGame(new EmptyLogger(), null);
            Enemy enemy = new Enemy(0, enemyClass, 1, null, null, -1, new ArrayList<>());
            enemy.setEnergy(mathEnemy.getFullEnergy());
            seat.setWeapon(-1);

            while (true) {
                Integer randomDamageForWeapon = MathData.getRandomDamageForWeapon(specialWeaponTypeId);
                if (needTestSpecialWeapon) {
                    seat.addWeapon(specialWeaponTypeId, 1);
                    seat.setWeapon(specialWeaponTypeId);
                }
                ShootResult shootResult = enemyGame.doShoot(enemy, seat, stake, false, false, 1);
                totalBet += shootResult.getBet().toDoubleCents();
                totalWin += shootResult.getWin().toDoubleCents() * randomDamageForWeapon;
                totalWinKillBonusAward += shootResult.getKillAwardWin().toDoubleCents() * randomDamageForWeapon;
                totalGemWins += shootResult.getTotalGemsPayout().toDoubleCents();

                if(shootResult.isInstanceKill()) cntInstantKill++;
                List<Weapon> awardedWeapons = shootResult.getAwardedWeapons();
                for (Weapon awardedWeapon : awardedWeapons) {
                    Long old = playerWeapons.get(awardedWeapon.getId());
                    playerWeapons.put(awardedWeapon.getId(), old + awardedWeapon.getShots());
                }

                int percent = (int) (100 - ((double) (cnt - i) / cnt) * 100);
                if (percent != lastPercent && percent % 2 == 0) {
                    double bet = totalBet>0 ?  totalBet  : i;
                    double rtp = (totalWin + totalWinKillBonusAward) / bet;
                    System.out.println("processing " + percent + "%"
                            + " RTP: " + rtp
                            + " totalWin: " + totalWin
                            + " totalWinKillBonusAward: " + totalWinKillBonusAward
                            + " totalGemWins: " + totalGemWins

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
        for (Map.Entry<Integer, Long> weapon : playerWeapons.entrySet()) {
            Double rtpForWeapon = MathData.getRtpForWeapon(weapon.getKey()) / 100;
            double multiplier = rtpForWeapon * weapon.getValue() * MathData.getAverageDamageForWeapon(weapon.getKey());
            winByWeapons += stake.getWithMultiplier(multiplier).toDoubleCents();
        }

        if (totalBet == 0)
            totalBet = cnt;


        System.out.println("cntKilledEnemy: " + cntKilledEnemy);
        System.out.println("averagePayout: " + totalWin/cntKilledEnemy);


        System.out.println("playerWeapons: " + playerWeapons);
        System.out.println("totalBet: " + totalBet);
        System.out.println("totalWin: " + totalWin);
        System.out.println("totalWinKillBonusAward: " + totalWinKillBonusAward);
        System.out.println("winByWeapons: " + winByWeapons);
        System.out.println("cntInstantKill: " + cntInstantKill);
        System.out.println("RTP base: " + totalWin / totalBet);
        System.out.println("RTP weapons: " + winByWeapons / totalBet);
        System.out.println("RTP kill bonus: " + totalWinKillBonusAward / totalBet);
        System.out.println("RTP gems bonus: " + totalGemWins / totalBet);

        System.out.println("RTP all: " + (totalWin + totalWinKillBonusAward + winByWeapons + totalGemWins) / totalBet);
    }


    static MathEnemy createMathEnemy(EnemyType enemyType, int level) {
        if (enemyType.isBoss()) {
            return new MathEnemy(0, "", 0, MathData.getBossParams().get(level).second());
        } else {
            int[] levels = MathData.getEnemyData(enemyType.getId()).getLevels();
            int healthForFirstLevel = levels[level == -1 ? RNG.nextInt(levels.length) : level];
            return new MathEnemy(0, "", 0, healthForFirstLevel);
        }
    }*/
}
