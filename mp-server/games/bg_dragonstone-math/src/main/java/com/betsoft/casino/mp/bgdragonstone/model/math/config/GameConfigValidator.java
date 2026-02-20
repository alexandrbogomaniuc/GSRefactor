package com.betsoft.casino.mp.bgdragonstone.model.math.config;

import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyData;
import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyRange;
import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;

public class GameConfigValidator {

    private static final double EPS = 0.0000001;
    private int[] weapons = { -1, 9, 10, 4, 6, 7 , 16};

    public String validate(GameConfig config) {

        StringBuilder res = new StringBuilder();

        if (config.getWeaponTargets() == null) {
            return "Missed weaponTargets";
        }

        if (config.getCriticalHitMultipliers() == null) {
            return "Missed criticalHitMultipliers";
        }

        for (int weaponId : weapons) {
            if (config.getCriticalHitMultipliers().get(weaponId) == null) {
                return "Missed criticalHitMultipliers for weapon " + weaponId;
            }
        }

        for (EnemyType enemyType : EnemyRange.BASE_ENEMIES.getEnemies()) {
            EnemyData data = config.getEnemyData(enemyType, 0);
            if (data == null) {
                return "Missed data for enemy " + enemyType.name();
            }
            if (data.getPayout() == 0) {
                return "Missed payout for enemy " + enemyType.name();
            }
            if (data.getPSlotDrop() < EPS) {
                return "Missed getPSlotDrop for enemy " + enemyType.name();
            }
            if (data.getPSWDrop() < EPS) {
                return "Missed getPSWDrop for enemy " + enemyType.name();
            }
            if (data.getKillProbabilities() == null) {
                return "Missed kill probabilities for enemy " + enemyType.name();
            }
        }

        if (config.getBoss() == null) {
            return "Missed boss params";
        }


         if(config.getSpiritMax() == 0){
             return "spiritMax is not defined";
         }

        if(config.getSpiritMin() == 0){
            return "spiritMin is not defined";
        }

        if(config.getRageMax() == 0){
            return "RageMax is not defined";
        }

        if(config.getRageMin() == 0){
            return "RageMin is not defined";
        }


        if(config.getBoss().getDefeatThreshold() == 0){
            return "Boss DefeatThreshold is not defined";
        }

        if(config.getBoss().getPartialPayProb() == null || config.getBoss().getPartialPayProb().isEmpty()){
            return "Boss partialPayProb is not defined";
        }


        if(config.getBoss().getPartialPays() == null || config.getBoss().getPartialPays().isEmpty()){
            return "Boss partialPays is not defined";
        }


        if (config.getSlot() == null) {
            return "Missed slot config";
        }
        if (config.getSlot().getPays() == null) {
            return "Missed slot pays";
        }
        if (config.getSlot().getSpins() == 0) {
            return "Missed slot spins";
        }

        if (config.getSlot().getReels() == null) {
            return "Missed slot reels";
        }


        if(res.length() > 0){
            return res.toString();
        }


        if (config.getFragments() == null) {
            return "Missed Dragonstone fragments config";
        }

        if(config.getFragments().getHitFrequency() == null || config.getFragments().getHitFrequency().isEmpty()){
            return "Fragments hitFrequency is not defined";
        }

        if (config.getFragments().getCollectToSpawn() == 0) {
            return "Missed Dragonstone fragments collectToSpawn";
        }

        return "";
    }
}
