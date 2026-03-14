package com.abs.casino;

import com.abs.casino.common.cache.data.game.BaseGameInfo;
import com.abs.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.abs.casino.common.cache.data.game.GameGroup;
import com.abs.casino.common.cache.data.game.GameType;
import com.abs.casino.common.cache.data.game.GameVariableType;
import com.abs.casino.common.util.JsonHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonHelperGameTemplateTest {

    @Test
    public void shouldPreserveDefaultGameInfoMetadataAcrossJsonRoundTrip() {
        JsonHelper helper = new JsonHelper("com.abs.casino", "com.dgphoenix.casino");
        BaseGameInfo defaultGameInfo = new BaseGameInfo();
        defaultGameInfo.setId(838L);
        defaultGameInfo.setName("DRAGONSTONE");
        defaultGameInfo.setGameType(GameType.MP);
        defaultGameInfo.setGroup(GameGroup.ACTION_GAMES);
        defaultGameInfo.setVariableType(GameVariableType.COIN);

        BaseGameInfoTemplate template = new BaseGameInfoTemplate(838L, "DRAGONSTONE", null, defaultGameInfo, false, "/MQ_Dragonstone.game");

        String json = helper.serializeToJson(template);
        BaseGameInfoTemplate restored = helper.deserializeFromJson(json, BaseGameInfoTemplate.class);

        assertEquals(GameType.MP, restored.getDefaultGameInfo().getGameType());
        assertEquals(GameGroup.ACTION_GAMES, restored.getDefaultGameInfo().getGroup());
        assertEquals(GameVariableType.COIN, restored.getDefaultGameInfo().getVariableType());
    }
}
