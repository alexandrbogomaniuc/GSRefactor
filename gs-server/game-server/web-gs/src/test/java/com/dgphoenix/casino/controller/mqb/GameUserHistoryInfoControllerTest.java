package com.abs.casino.controller.mqb;

import com.abs.casino.gs.socket.mq.GameUserHistoryService;
import com.abs.casino.mqb.GameUserHistoryInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.mqb.GameUserHistory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GameUserHistoryInfoControllerTest {

    @Mock
    private GameUserHistoryService gameUserHistoryService;
    @Mock
    private HttpServletRequest request;

    private GameUserHistoryInfoController controller;

    @Before
    public void setUp() {
        controller = new GameUserHistoryInfoController(gameUserHistoryService);
    }

    @Test
    public void getCorrectUserHistoryResponse() throws CommonException {
        GameUserHistoryInfo returnedGamesHistory = new GameUserHistoryInfo(
                Collections.emptyList(),
                Collections.singletonList(new GameUserHistory(123L, 856L, "Amazon", 100L, 85L, "MQC", 1639477250L))
        );
        when(gameUserHistoryService.getUserGameHistoryInfo(271L, "user1", 272L, "user2", 1637556259L, 1637556259L, ClientType.FLASH)).thenReturn(returnedGamesHistory);
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(returnedGamesHistory, HttpStatus.OK);

        ResponseEntity<Object> actualResponse = controller.getPlayerGameHistory(271L, "user1", 272L, "user2", 1637556259L, 1637556259L, request);

        Assert.assertEquals(expectedResponse, actualResponse);

    }
}
