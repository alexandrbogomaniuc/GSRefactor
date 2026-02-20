package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.common.AbstractBattlegroundGameRoom;
import com.betsoft.casino.mp.model.SingleNodeRoomInfo;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.dgphoenix.casino.common.util.Pair;
import com.hazelcast.core.HazelcastInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BGPrivateRoomPlayersStatusServiceTest {

    /*
    @Mock
    private AbstractBattlegroundGameRoom room;
    @Mock
    private IGameSocketClient gameSocketClient;
    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private SingleNodeRoomInfo singleNodeRoomInfo;
    private BGPrivateRoomPlayersStatusService bgPrivateRoomPlayersStatusService;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        AsyncExecutorService asyncExecutorService = new AsyncExecutorService(1,1,1, TimeUnit.MINUTES,10,new ThreadPoolExecutor.CallerRunsPolicy());
        bgPrivateRoomPlayersStatusService = new BGPrivateRoomPlayersStatusService(hazelcastInstance, asyncExecutorService);
    }

    @Test
    public void testCancelKickPlayerFound() {
        String nickname = "testPlayer";
        String externalId = "testExternalId";
        long accountId = 123L;
        when(room.getObservers()).thenReturn(Collections.singletonList(gameSocketClient));
        when(gameSocketClient.getNickname()).thenReturn(nickname);

        Pair<IGameSocketClient, Status> result = bgPrivateRoomPlayersStatusService.cancelKick(room, nickname, externalId, accountId);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertEquals(Status.WAITING, result.getValue());
        verify(room, times(1)).cancelKick(any(IGameSocketClient.class), eq(externalId));
    }

    @Test
    public void testCancelKickPlayerNotFound() {
        String nickname = "testPlayer";
        String externalId = "testExternalId";
        long accountId = 123L;
        when(room.getObservers()).thenReturn(Collections.emptyList());

        Pair<IGameSocketClient, Status> result = bgPrivateRoomPlayersStatusService.cancelKick(room, nickname, externalId, accountId);

        assertNotNull(result);
        assertNull(result.getKey());
        assertNull(result.getValue());
        verify(room, never()).cancelKick(any(IGameSocketClient.class), anyString());
    }

    @Test
    public void testCancelKick_SendPlayerStatusInPrivateRoomToCanex_IsCalled() {
        // Given
        int serverId = 1;
        String privateRoomId = "room123", nickname = "player1", externalId = "ext123";
        long accountId = 123L, bankId = 100;
        when(room.getObservers()).thenReturn(Collections.emptyList());
        when(room.getRoomInfo()).thenReturn(singleNodeRoomInfo);
        when(singleNodeRoomInfo.getGameServerId()).thenReturn(serverId);
        when(singleNodeRoomInfo.getBankId()).thenReturn(bankId);
        when(singleNodeRoomInfo.getPrivateRoomId()).thenReturn(privateRoomId);

        BGPrivateRoomPlayersStatusService spyService = Mockito.spy(bgPrivateRoomPlayersStatusService);

        // When
        spyService.cancelKick(room, nickname, externalId, accountId);

        // Then
        verify(spyService, timeout(1000)).sendPlayerStatusInPrivateRoomToCanex(serverId, (int) bankId, privateRoomId, nickname, externalId, accountId, TBGStatus.accepted);
    }
*/
}