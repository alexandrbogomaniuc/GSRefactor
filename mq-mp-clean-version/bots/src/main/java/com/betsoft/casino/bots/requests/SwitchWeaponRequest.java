package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.bots.strategies.IRoomNaturalBotStrategy;
import com.betsoft.casino.mp.transport.SwitchWeapon;
import com.betsoft.casino.mp.transport.WeaponSwitched;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class SwitchWeaponRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final int weaponId;
    public static String METRIC = "SwitchWeaponRequestResponse";

    public SwitchWeaponRequest(RoomBot bot, ISocketClient client, int weaponId) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.weaponId = weaponId;
    }

    @Override
    public void send(int rid) {

        if(bot.getStrategy() instanceof IRoomNaturalBotStrategy) {
            long timeMillis = System.currentTimeMillis();
            getLogger().debug("send: SwitchWeapon request was made by bot: {}, requestResponseMetric: {}, timeMillis: {}",
                    bot.getId(), METRIC, toHumanReadableFormat(timeMillis));
            ((IRoomNaturalBotStrategy) bot.getStrategy()).updateOtherRequestTimeMetric(METRIC, timeMillis);
        }

        client.sendMessage(new SwitchWeapon(System.currentTimeMillis(), rid, weaponId));
    }

    @Override
    public void handle(ITransportObject response) {
        bot.setLastReceivedServerTime(response.getDate());

        if(bot.getStrategy() instanceof IRoomNaturalBotStrategy) {
            long timeMillis = System.currentTimeMillis();
            getLogger().debug("handle: SwitchWeapon response was received by bot: {}, requestResponseMetric: {}, timeMillis: {}",
                    bot.getId(), METRIC, toHumanReadableFormat(timeMillis));
            ((IRoomNaturalBotStrategy) bot.getStrategy()).updateOtherResponseTimeMetric(METRIC, timeMillis);
        }

        switch (response.getClassName()) {
            case "WeaponSwitched": {
                WeaponSwitched weaponSwitched = (WeaponSwitched) response;
                weaponSwitched.getWeapons().forEach(weapon -> bot.updateWeapon(weapon.getId(), weapon.getShots()));
                bot.activateWeapon(weaponSwitched.getWeaponId());
                break;
            }
            case "Error":
                bot.activateWeapon(-1);
                break;
        }
        bot.setState(BotState.PLAYING, "Weapon switched");
        bot.doActionWithSleep(bot.getWaitTimeAfterSwitchWeapon(), "SwitchWeaponRequest[" + response.getClassName() + "]");
    }
}
