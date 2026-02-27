package com.abs.casino.gs.socket.mq;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.abs.casino.mqb.GameUserHistory;
import com.abs.casino.mqb.GameUserHistoryInfo;
import com.abs.casino.services.LoginService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GameUserHistoryServiceTest {

    private com.abs.casino.gs.socket.mq.GameUserHistoryService gameUserHistoryService;

    @Mock
    private CassandraPersistenceManager cpm;
    @Mock
    private LoginService loginService;
    @Mock
    private CassandraGameSessionPersister gameSessionPersister;
    @Mock
    private AccountManager accountManager;
    @Mock
    private BaseGameInfoTemplateCache gameInfoManager;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        when(cpm.getPersister(CassandraGameSessionPersister.class)).thenReturn(gameSessionPersister);
        gameUserHistoryService = new com.abs.casino.gs.socket.mq.GameUserHistoryService(loginService, cpm, accountManager, gameInfoManager);
    }

    @Test
    public void getCorrectGameHistory() throws CommonException {
        GameUserHistoryInfo expected = new GameUserHistoryInfo(
                Collections.singletonList(new GameUserHistory(1L, 856L, "Amazon", 150L, 230L, "MMC", 1639477866L)),
                Collections.singletonList(new GameUserHistory(2L, 838L, "Dragonstone", 85L, 20L, "MQC", 1639477866L))
        );
        CommonWalletAuthResult mmcWalletAuth = new CommonWalletAuthResult(true);
        mmcWalletAuth.setUserId("user1");
        when(loginService.getUserCWInfo("user1", 271L, ClientType.FLASH)).thenReturn(mmcWalletAuth);
        AccountInfo mmcAccount = new AccountInfo();
        mmcAccount.setId(1L);
        mmcAccount.setCurrency(new Currency("MCC", "!"));
        when(accountManager.getByCompositeKey(271L, mmcWalletAuth.getUserId())).thenReturn(mmcAccount);
        CommonWalletAuthResult mqcWalletAuth = new CommonWalletAuthResult(true);
        mqcWalletAuth.setUserId("user2");
        when(loginService.getUserCWInfo("user2", 272L, ClientType.FLASH)).thenReturn(mqcWalletAuth);
        AccountInfo mqcAccount = new AccountInfo();
        mqcAccount.setId(2L);
        mqcAccount.setCurrency(new Currency("MQC", "!"));
        when(accountManager.getByCompositeKey(272L, mqcWalletAuth.getUserId())).thenReturn(mqcAccount);

        List<GameSession> mmcSession = Collections.singletonList(
                new GameSession(123L, 1L, 271L, 856L, 1639477866L, 150L, 230L,
                        0, 0, false, false, new Currency("MMC", "MMC"), null, null, false, null)
        );
        when(gameSessionPersister.getAccountGameSessionList(1L, new Date(1637556259L), new Date(1647516259L))).thenReturn(mmcSession);
        BaseGameInfoTemplate template = new BaseGameInfoTemplate();
        template.setTitle("Amazon");
        when(gameInfoManager.getBaseGameInfoTemplateById(856L)).thenReturn(template);

        List<GameSession> mqcSession = Collections.singletonList(
                new GameSession(124L, 2L, 271L, 838L, 1639477866L, 85L, 20L,
                        0, 0, false, false, new Currency("MQC", "MQC"), null, null, false, null)
        );
        when(gameSessionPersister.getAccountGameSessionList(2L, new Date(1637556259L), new Date(1647516259L))).thenReturn(mqcSession);
        BaseGameInfoTemplate dragonStoneTemplate = new BaseGameInfoTemplate();
        dragonStoneTemplate.setTitle("Dragonstone");
        when(gameInfoManager.getBaseGameInfoTemplateById(838L)).thenReturn(dragonStoneTemplate);

        GameUserHistoryInfo actual = gameUserHistoryService.getUserGameHistoryInfo(271L, "user1", 272L, "user2", 1637556259L, 1647516259L, ClientType.FLASH);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getGameSessionWhenBankIdIncorrect() throws CommonException {
        when(loginService.getUserCWInfo("user1", 1L, ClientType.FLASH)).thenReturn(null);
        expectedEx.expect(CommonException.class);
        expectedEx.expectMessage("User not found with token: user1, bankId: 1, clientType: FLASH");

        gameUserHistoryService.getUserGameHistoryInfo(1L, "user1", 272L, "user2", 1637556259L, 1647516259L, ClientType.FLASH);
    }

    @Test
    public void getGameSessionWhenAllAccountDoesntExist() throws CommonException {
        GameUserHistoryInfo expected = new GameUserHistoryInfo(Collections.emptyList(), Collections.emptyList());
        CommonWalletAuthResult mmcWalletAuth = new CommonWalletAuthResult(true);
        mmcWalletAuth.setUserId("user1");
        when(loginService.getUserCWInfo("user1", 271L, ClientType.FLASH)).thenReturn(mmcWalletAuth);
        CommonWalletAuthResult mqcWalletAuth = new CommonWalletAuthResult(true);
        mqcWalletAuth.setUserId("user2");
        when(loginService.getUserCWInfo("user2", 272L, ClientType.FLASH)).thenReturn(mqcWalletAuth);
        when(accountManager.getByCompositeKey(271L, mmcWalletAuth.getUserId())).thenReturn(null);
        when(accountManager.getByCompositeKey(272L, mqcWalletAuth.getUserId())).thenReturn(null);

        GameUserHistoryInfo actual = gameUserHistoryService.getUserGameHistoryInfo(271L, "user1", 272L, "user2", 1637556259L, 1647516259L, ClientType.FLASH);

        Assert.assertEquals(expected, actual);
    }

}
