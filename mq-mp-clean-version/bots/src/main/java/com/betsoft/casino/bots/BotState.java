package com.betsoft.casino.bots;

public enum BotState {
    IDLE,
    OBSERVING,
    WAITING_FOR_RESPONSE,
    PLAYING,
    WAIT_BATTLE_PLAYERS,
    NEED_SIT_OUT;

    public static BotState fromTBotState(com.betsoft.casino.mp.model.bots.dto.BotState tBotState) {
        switch (tBotState) {
            case need_sit_out:
                return BotState.NEED_SIT_OUT;
            case observing:
                return BotState.OBSERVING;
            case waiting_for_response:
                return BotState.WAITING_FOR_RESPONSE;
            case playing:
                return BotState.PLAYING;
            case wait_battle_players:
                return BotState.WAIT_BATTLE_PLAYERS;
            default:
                return BotState.IDLE;
        }
    }

    public com.betsoft.casino.mp.model.bots.dto.BotState toTBotState() {
        switch (this) {
            case NEED_SIT_OUT:
                return com.betsoft.casino.mp.model.bots.dto.BotState.need_sit_out;
            case OBSERVING:
                return com.betsoft.casino.mp.model.bots.dto.BotState.observing;
            case WAITING_FOR_RESPONSE:
                return com.betsoft.casino.mp.model.bots.dto.BotState.waiting_for_response;
            case PLAYING:
                return com.betsoft.casino.mp.model.bots.dto.BotState.playing;
            case WAIT_BATTLE_PLAYERS:
                return com.betsoft.casino.mp.model.bots.dto.BotState.wait_battle_players;
            default:
                return com.betsoft.casino.mp.model.bots.dto.BotState.idle;
        }
    }
}
