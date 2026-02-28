package com.abs.casino.bgm;

import com.abs.casino.common.cache.data.game.BaseGameConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * User: flsh Date: 10.04.2009
 */
public class DataBaseInitializer implements ServletContextListener {
    private static final Logger LOG = LogManager.getLogger(DataBaseInitializer.class);


    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // nothing to implement here
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOG.info("DataBaseInitializer started");

        try {
            LOG.info("Initing db");
            //        createOrUpdateGames();
            LOG.info("Initing db completed");
        } catch (Throwable e) {
            LOG.error("DataBaseInitializer::contextInitialized error:", e);
            throw new RuntimeException("error initializing DataBaseInitializer", e);
        }

        LOG.info("DataBaseInitializer ended");
    }
      /*

    private void createOrUpdateGames() throws Exception {
        ThreadLog.info("DataBaseInitializer: Start creating games...");

        BaseGameCache bgc = BaseGameCache.getInstance();
        List<Coin> jackpotSlotCoins = new ArrayList<Coin>();
        jackpotSlotCoins.add(new Coin(1, 10));
        jackpotSlotCoins.add(new Coin(2, 25));
        jackpotSlotCoins.add(new Coin(3, 50));
        jackpotSlotCoins.add(new Coin(4, 100));

        List<Coin> slotCoins = new ArrayList<Coin>();
        slotCoins.add(new Coin(1, 10));
        slotCoins.add(new Coin(2, 25));
        slotCoins.add(new Coin(3, 50));
        slotCoins.add(new Coin(4, 100));
        slotCoins.add(new Coin(11, 2));
        slotCoins.add(new Coin(12, 5));

        List<Coin> kenoCoins = new ArrayList<Coin>();
        kenoCoins.add(new Coin(1, 25));
        kenoCoins.add(new Coin(2, 100));
        kenoCoins.add(new Coin(3, 500));

        Long jackPotId = null;
        Map<String, String> properties = null;
        String spGameProcessor = "com.abs.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(3L, "TRIPLECROWN", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(122L, "THEBEES", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId, properties,
                true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(193l, "ONCEUPONATIME", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(190l, "AZTECTREASURE3", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(180l, "HEIST", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId, properties,
                true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(179L, "SLOTFATHER", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(178L, "GLADIATOR", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(177L, "THREEWISHES", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(175L, "AZTECTREASURE2", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = createJackPotIfNeeded(148L, 0.01, 0.01, 750, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(148L, "AZTECTREASURE", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = createJackPotIfNeeded(173L, 0.01, 0.01, 25000, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(173L, "GLAMLIFE", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId, properties,
                true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(159L, "MADSCIENTIST", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = createJackPotIfNeeded(158L, 0.01, 0.01, 25000, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(158L, "TREASUREROOM", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = createJackPotIfNeeded(156L, 0.01, 0.01, 5000, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(156L, "THEGHOULS", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(155L, "INVADERS", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId, properties,
                true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(143L, "SCRATCHERZ", GameType.SP, GameGroup.SOFT_GAMES, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(136L, "WIZARDSCASTLE", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = createJackPotIfNeeded(134L, 0.01, 0.01, 5000, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(134L, "PHARAOHKING", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(133L, "REELOUTLAWS", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(106L, "PAIRPLUS", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(105L, "THREECARDPOKER", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(104L, "BACKINTIME", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(103L, "MP_DOUBLEJACKPOT", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(102L, "MP_DOUBLEBONUS", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(101L, "MP_BONUSDELUX", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(100L, "MP_BONUSPOKER", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(99L, "MP_ALLAMERICAN", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(98L, "MP_JOKERPOKER", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(97L, "MP_DEUCESWILD", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(96L, "MP_JACKSORBETTER", GameType.SP, GameGroup.MULTIHAND_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(94L, "REDDOG", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(91L, "PREDICTOR", GameType.SP, GameGroup.SOFT_GAMES, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = createJackPotIfNeeded(88L, 0.01, 0.01, 9375, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(88L, "CHASETHECHEESE", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(87L, "SWEET16", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(86L, "SINGLEDECKSIDE", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(85L, "MATCHDEALER", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(82L, "PIRATE21", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(81L, "PONTOON", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(80L, "HIDDENLOOT", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_MAX_BET_1, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_2, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_3, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_4, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_6, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_12, "50000");
        properties.put(BaseGameConstants.KEY_MAX_BET_18, "50000");
        bgc.createAndSave(79L, "HAMBURGROULETTE", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties, true, 500, 30000, Collections.EMPTY_LIST);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(78L, "ATLANTICROULETTE", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(74L, "DRAWHILO", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(73L, "DASHFORCASH", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(72L, "P_DOUBLEJACKPOT", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(71L, "P_DOUBLEBONUS", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(70L, "P_BONUSDELUX", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(69L, "P_BONUSPOKER", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(68L, "P_ACESANDFACES", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(67L, "P_JOKERPOKER", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(66L, "P_DEUCESWILD", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(65L, "P_JACKSORBETTER", GameType.SP, GameGroup.PYRAMID_POKER, null, spGameProcessor,
                jackPotId, properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(64L, "BURNBLACKJACK", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(63L, "MODIFIEDBJ", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(191L, "MODIFIEDBJEU", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(195L, "AMERICANBLACKJACK", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = createJackPotIfNeeded(52L, 0.01, 0.01, 2000, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(52L, "GHOULSGOLD", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(49L, "SUPER7", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(48L, "CASINOWAR", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(47L, "OUTOFTHISWORLD", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        // MULTIHANDBLACKJACK, SUPER7 is subgame, subgame must be
        // isenabled=false
        bgc.createAndSave(44L, "MULTIHANDBLACKJACK", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(43L, "RIDETHEMPOKER", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties, true, 300, 9900, Collections.EMPTY_LIST);

        jackPotId = createJackPotIfNeeded(41L, 0.01, 0.01, 25000, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(41L, "JACKPOTJAMBA", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(33L, "MONKEYMONEY", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = createJackPotIfNeeded(32L, 0.01, 0.01, 3000, Coin.copyCoins(slotCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(32L, "DIAMONDPROGRESSIVE1L", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(jackpotSlotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(31L, "JACKSDOUBLEUP", GameType.SP, GameGroup.VIDEOPOKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(30L, "JOKERSDOUBLEUP", GameType.SP, GameGroup.VIDEOPOKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(29L, "DEUCESDOUBLEUP", GameType.SP, GameGroup.VIDEOPOKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(23L, "FIVEDRAWPOKER", GameType.SP, GameGroup.VIDEOPOKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(137L, "BONUSPOKER", GameType.SP, GameGroup.VIDEOPOKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(139L, "DOUBLEBONUS", GameType.SP, GameGroup.VIDEOPOKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        List<Coin> allAmericanCoins = new ArrayList<Coin>();
        allAmericanCoins.add(new Coin(2, 25));
        allAmericanCoins.add(new Coin(3, 50));
        allAmericanCoins.add(new Coin(4, 100));
        allAmericanCoins.add(new Coin(6, 500));
        jackPotId = createJackPotIfNeeded(46L, 0.01, 0.01, 1250, Coin.copyCoins(allAmericanCoins));
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.96");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "0");
        bgc.createAndSave(46L, "ALLAMERICAN", GameType.SP, GameGroup.VIDEOPOKER, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(allAmericanCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(21L, "DIAMONDDREAMS5L", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(20L, "MERMAIDSPEARL", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(18L, "CAPTAINCASH", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(194L, "BARBARYCOAST", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(207L, "ENCHANTED", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId,
                properties, true, 100, 10000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(199L, "VIRTUALRACEBOOK3D", GameType.SP, GameGroup.SOFT_GAMES, null, spGameProcessor,
                jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "FALSE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(8L, "KENO", GameType.SP, GameGroup.KENO, null, spGameProcessor, jackPotId, properties, true,
                100, 1000, Coin.copyCoins(slotCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(16L, "INSTANTKENO", GameType.SP, GameGroup.KENO, null, spGameProcessor, jackPotId, properties,
                true,
                100, 1000, Coin.copyCoins(kenoCoins));

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(107L, "KLUBKENO", GameType.SP, GameGroup.KENO, null, spGameProcessor, jackPotId, properties,
                true,
                100, 1000, Coin.copyCoins(slotCoins));


        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(15L, "KRAZYKENO", GameType.SP, GameGroup.KENO, null, spGameProcessor, jackPotId, properties,
                true,
                100, 1000, Coin.copyCoins(kenoCoins));


        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(13L, "BACCARAT", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(12L, "CARIBEANPOKER", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(203L, "OASISPOKER", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId,
                properties);


        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(10L, "CRAPS", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        bgc.createAndSave(9L, "PAIGOW", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_ACS_DISABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_MAX_BET_1, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_2, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_3, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_4, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_5, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_6, "3000");
        properties.put(BaseGameConstants.KEY_MAX_BET_12, "50000");
        properties.put(BaseGameConstants.KEY_MAX_BET_18, "50000");
        bgc.createAndSave(5L, "ROULETTE", GameType.SP, GameGroup.TABLE, null, spGameProcessor, jackPotId, properties,
                true, 500, 30000, Collections.EMPTY_LIST);

        jackPotId = null;
        properties = new ConcurrentStringMap<String>();
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, "0.97");
        properties.put(BaseGameConstants.KEY_ISENABLED, "TRUE");
        properties.put(BaseGameConstants.KEY_MAX_PLAYER_COUNT, "500");
        properties.put(BaseGameConstants.KEY_MAX_BET_TIME, "10000");
        properties.put(BaseGameConstants.KEY_DEFAULT_COIN, "2");
        bgc.createAndSave(2L, "LUCKY7", GameType.SP, GameGroup.SLOTS, null, spGameProcessor, jackPotId, properties,
                true, 100, 10000, Coin.copyCoins(slotCoins));

        ThreadLog.info("DataBaseInitializer: createing games completed");
    }


    private Long createJackPotIfNeeded(long gameId, Double pcrp, Double bcrp, int baseBankMultiplier,
                                       List<Coin> gameCoins) throws Exception {
        try {
            ThreadLog.info("DataBaseInitializer::initializing jackpot for game:" + gameId);
            SPGJackPot jackpot = JackPotCache.getInstance().getJackPot(gameId, null);
            if (jackpot == null) {
                ThreadLog.info("DataBaseInitializer::jackpot for game:" + gameId + " not found, creating...");
                jackpot =
                        ProgressiveJackPotManager.getInstance().createJackpot(gameId, null, "MAIN", 0, pcrp, bcrp, 0,
                                baseBankMultiplier, gameCoins);
                JackPotCache.getInstance().put(jackpot);
            } else {
                JackPotCache.getInstance().flush();
            }
            ThreadLog.info("DataBaseInitializer::jackpot for game:" + gameId + " initialized");
            return jackpot.getId();
        } catch (Throwable e) {
            ThreadLog.error("DataBaseInitializer::error:", e);
            throw new Exception(e);
        }
    }
    */

}
