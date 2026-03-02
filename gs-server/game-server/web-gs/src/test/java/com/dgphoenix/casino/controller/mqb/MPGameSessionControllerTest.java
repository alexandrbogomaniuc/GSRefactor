package com.abs.casino.controller.mqb;

import com.abs.casino.battleground.messages.MPGameSessionFinishResponse;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.abs.casino.gs.socket.mq.BattlegroundService;
import com.abs.casino.services.mp.MPGameSessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MPGameSessionControllerTest {

    @Mock
    private MPGameSessionService mpGameSessionService;
    @Mock
    private BattlegroundService battlegroundService;

    private MPGameSessionController mpGameSessionController;

    @Before
    public void setUp() {
        mpGameSessionController = new MPGameSessionController(mpGameSessionService, battlegroundService);
    }


    @Test
    public void successRequest() throws CommonException {
        ResponseEntity<?> expected = ResponseEntity.ok(new MPGameSessionFinishResponse(false, Collections.emptySet()));
        String SID = "123456789";
        when(mpGameSessionService.finishGameSessionAndMakeSitOut(SID, null)).thenReturn(new Pair<>(null, false));

        ResponseEntity<?> actualResponse = mpGameSessionController.finishGameSession(SID, null);

        assertEquals(expected, actualResponse);
    }
}
