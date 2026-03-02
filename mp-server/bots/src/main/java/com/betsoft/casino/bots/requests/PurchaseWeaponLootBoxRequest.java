package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.PurchaseWeaponLootBox;
import com.betsoft.casino.mp.transport.WeaponLootBox;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PurchaseWeaponLootBoxRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;

    public PurchaseWeaponLootBoxRequest(IRoomBot bot, ISocketClient client) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new PurchaseWeaponLootBox(System.currentTimeMillis(), rid, RNG.nextInt(1, 3)));
    }

    @Override
    public void handle(ITransportObject response) {
        bot.setState(BotState.PLAYING, "After LootBox purchase");
        switch (response.getClassName()) {
            case "WeaponLootBox":
                WeaponLootBox box = (WeaponLootBox) response;
                bot.addWeapon(box.getWeaponId(), box.getShots());
                break;
            case "Error":
                getLogger().error("PurchaseWeaponLootBoxRequest: failed to purchase LootBox={}", response);
                bot.count(Stats.ERRORS);
                Error errorResponse = (Error) response;
                int code = errorResponse.getCode();
                if(code == ErrorCodes.NOT_SEATER) {
                    bot.stop();
                    bot.start();
                }
                break;
            default:
                getLogger().error("PurchaseWeaponLootBoxRequest: unexpected response type");
                break;
        }
        bot.doActionWithSleep(1000, "PurchaseWeaponLootBoxRequest[" + response.getClassName() + "]");
    }
}
