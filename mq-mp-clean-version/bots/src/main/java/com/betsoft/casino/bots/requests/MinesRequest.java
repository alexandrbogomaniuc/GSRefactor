package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.MineCoordinates;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.util.RNG;

public class MinesRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    boolean isPaid;

    public MinesRequest(IRoomBot bot, ISocketClient client, boolean isPaid) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.isPaid = isPaid;
    }

    @Override
    public void send(int rid) {
        MineCoordinates outbound = new MineCoordinates(System.currentTimeMillis(), rid,
                (float) ((RNG.rand() * 200) + 250),
                (float) ((RNG.rand() * 200) + 150),
                isPaid);
        getLogger().debug("MinesRequest: bot: {},  mine:  {}, isPaid: {}", bot.getId(), outbound, isPaid);
        if (!isPaid) {
            bot.getStrategy().consumeAmmo(SpecialWeaponType.Landmines.getId());
            getLogger().debug("MinesRequest: decrease mine: " + bot.getStrategy().
                    getShotsForWeapon(SpecialWeaponType.Landmines.getId()));
        }
        client.sendMessage(outbound);
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "MinePlace":
                break;
            case "Error":
                bot.count(Stats.ERRORS);
                //bot.getStrategy().addWeapon(SpecialWeaponType.Landmines.getId(), 1);
                bot.activateWeapon(-1);
                handleError((Error) response);
                break;
            default:
                getLogger().error("MinesRequest: unexpected response type: {}", response);
                break;
        }
    }

    private void handleError(Error error) {
        getLogger().debug("MinesRequest: mine place error={}", error);
        switch (error.getCode()) {
            case ErrorCodes.INTERNAL_ERROR:
                break;
            case ErrorCodes.WRONG_WEAPON:
//                bot.setWeapon(!special, 1);
                break;
            default:
                getLogger().error("MinesRequest: Unhandled mine place error: {}", error);
                break;
        }
    }
}
