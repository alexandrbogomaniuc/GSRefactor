package com.betsoft.casino.mp.model.bots.dto;

public enum BotState {
    idle(0),
    observing(1),
    waiting_for_response(2),
    playing(3),
    wait_battle_players(4),
    need_sit_out(5);

    private final int value;

    private BotState(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
}
