package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.BalanceUpdated;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class BalanceUpdatedHandler implements IServerMessageHandler<BalanceUpdated> {

    private static final Logger LOG = LogManager.getLogger(BalanceUpdatedHandler.class);

    private final IRoomBot bot;

    public BalanceUpdatedHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(BalanceUpdated response) {
        LOG.debug("BalanceUpdatedHandler::handle: set balance={}, rid={}", response.getBalance(), response.getRid());
        bot.setBalance(response.getBalance());
    }
}
