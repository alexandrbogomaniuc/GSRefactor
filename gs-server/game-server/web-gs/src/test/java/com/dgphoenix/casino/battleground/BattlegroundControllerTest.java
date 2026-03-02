package com.abs.casino.battleground;

import com.abs.casino.battleground.messages.BattlegroundInfo;
import com.abs.casino.battleground.messages.BattlegroundRoundHistory;
import com.abs.casino.battleground.messages.BattlegroundRoundHistoryInfo;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.controller.battleground.BattlegroundController;
import com.abs.casino.gs.socket.mq.BattlegroundService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BattlegroundControllerTest {

    @Mock
    private BattlegroundService battlegroundService;
    @Mock
    private HttpServletRequest request;

    private BattlegroundController battlegroundController;


    @Before
    public void setUp() {
        battlegroundController = new BattlegroundController(battlegroundService);
    }

    @Test
    public void getCorrectInfoResponse() {
        List<BattlegroundInfo> returnedGames = Collections.singletonList(new BattlegroundInfo(1L, "Dragonstone", Arrays.asList(10L, 20L, 30L)));
        when(battlegroundService.getGamesByBankId(1L)).thenReturn(returnedGames);
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(returnedGames, HttpStatus.OK);

        ResponseEntity<Object> actualResponse = battlegroundController.getGameConfigs(1L);

        Assert.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getCorrectBattlegroundRoundInfo() throws CommonException {
        BattlegroundRoundHistoryInfo returnedRounds = new BattlegroundRoundHistoryInfo(
                Collections.emptyList(),
                Collections.singletonList((new BattlegroundRoundHistory(856, "BG_Dragonstone", 50L, 140L, "MQC", 3, 1637556259L, 130L, 131L))));
        when(battlegroundService.getPlayerBattlegroundHistory(271L, "user1", 272L, "user2", 1637556259L, 1637556259L, ClientType.FLASH)).thenReturn(returnedRounds);
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(returnedRounds, HttpStatus.OK);

        ResponseEntity<Object> actualResponse = battlegroundController.getPlayerRoundHistory(271L, "user1", 272L, "user2", 1637556259L, 1637556259L, request);

        Assert.assertEquals(expectedResponse, actualResponse);
    }

}
