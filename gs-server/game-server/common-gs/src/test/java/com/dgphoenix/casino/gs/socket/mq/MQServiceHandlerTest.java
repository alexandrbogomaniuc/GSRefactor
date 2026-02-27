package com.dgphoenix.casino.gs.socket.mq;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.abs.casino.promo.PromoCampaignManager;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MQServiceHandlerTest {
    @Mock
    private CassandraPersistenceManager persistenceManager;
    @Mock
    private ICurrencyRateManager currencyRateManager;
    @Mock
    private ErrorPersisterHelper errorPersisterHelper;
    @Mock
    private TournamentBuyInHelper tournamentBuyInHelper;
    @Mock
    private PromoCampaignManager promoCampaignManager;
    @Mock
    private PlayerBetPersistenceManager betPersistenceManager;
    @Mock
    private CommonExecutorService executorService;
    @Mock
    private AccountManager accountManager;
    private MQServiceHandler mqServiceHandler;

    @Before
    public void setUp() throws CommonException {
        mqServiceHandler = new MQServiceHandler(persistenceManager, promoCampaignManager, tournamentBuyInHelper, currencyRateManager, errorPersisterHelper, betPersistenceManager, accountManager, executorService);
    }

    @Test
    public void testFetchingTotalBetsSpecialWeaponsWithCorrectData() {
        String data = "roomStake=5;totalBetsSpecialWeapons=20.0;name=Skullbreaker&cntShotsToEnemy=0&payouts=0.0";

        long totalBetsSpecialWeapons = mqServiceHandler.fetchTotalBetsSpecialWeapons(data);

        assertEquals(20L, totalBetsSpecialWeapons);
    }

    @Test
    public void testFetchingTotalBetsSpecialWeaponsWithNotNumber() {
        String data = "roomStake=5;totalBetsSpecialWeapons=million;name=Skullbreaker&cntShotsToEnemy=0&payouts=0.0";

        long totalBetsSpecialWeapons = mqServiceHandler.fetchTotalBetsSpecialWeapons(data);

        assertEquals(0L, totalBetsSpecialWeapons);
    }

    @Test
    public void testFetchingTotalBetsSpecialWeaponsWithoutSearchingParam() {
        String data = "roomStake=5;name=Skullbreaker&cntShotsToEnemy=0&payouts=0.0";

        long totalBetsSpecialWeapons = mqServiceHandler.fetchTotalBetsSpecialWeapons(data);

        assertEquals(0L, totalBetsSpecialWeapons);
    }

    @Test
    public void testFetchingTotalBetsSpecialWeaponsWithNullData() {
        long totalBetsSpecialWeapons = mqServiceHandler.fetchTotalBetsSpecialWeapons(null);

        assertEquals(0L, totalBetsSpecialWeapons);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testFetchingTotalBetsSpecialWeaponsWithNonFloatNumber() {
        String data = "roomStake=5;totalBetsSpecialWeapons=20;name=Skullbreaker&cntShotsToEnemy=0";

        mqServiceHandler.fetchTotalBetsSpecialWeapons(data);
    }
}