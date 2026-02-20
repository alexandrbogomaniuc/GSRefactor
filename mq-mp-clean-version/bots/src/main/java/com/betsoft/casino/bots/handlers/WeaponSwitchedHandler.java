package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.WeaponSwitched;
import com.betsoft.casino.mp.transport.Weapons;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class WeaponSwitchedHandler implements IServerMessageHandler<WeaponSwitched> {
    private final IRoomBot bot;

    public WeaponSwitchedHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(WeaponSwitched response) {

    }
}
