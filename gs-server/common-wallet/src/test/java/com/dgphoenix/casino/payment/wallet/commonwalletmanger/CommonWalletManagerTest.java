package com.dgphoenix.casino.payment.wallet.commonwalletmanger;

import com.dgphoenix.casino.common.DomainSessionFactory;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.config.CommonContextConfiguration;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionDataPersister;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import com.dgphoenix.casino.gs.managers.payment.wallet.v4.CWMType;
import com.dgphoenix.casino.payment.wallet.CommonWalletManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CommonContextConfiguration.class, CommonWalletManagerTest.class})
public class CommonWalletManagerTest {
    private static final String EXPECTED_BALANCE_PARAM = "EXPECTED_BALANCE_PARAM";
    private static final String EXPECTED_ACCUMULATED_WIN_PARAM = "EXPECTED_ACCUMULATED_WIN_PARAM";
    private static final long BANK_ID = 271L;
    private static final int GAME_ID = 590;

    private CommonWalletManager cwManager;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Spy
    private BankInfo bankInfo;
    @Mock
    private SessionInfo sessionInfo;
    @Mock
    private IWalletDBLink dbLink;
    private long dbLinkWinAmount;
    private AccountInfo accountInfo = new AccountInfo(1000, "testAcc", (int) BANK_ID, (short) 1, 0,
            false, false, new Currency("USD", "$"), null);
    private CommonWallet cWallet = new CommonWallet(accountInfo.getId());
    private CommonGameWallet gameWallet = new CommonGameWallet(GAME_ID, 0);

    @Mock
    private IdGenerator transactionIdGenerator;
    private static final AtomicLong transactionIdSequence = new AtomicLong(1);

    private RemoteClientStubHelper.ExtAccountInfoStub remoteAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(accountInfo.getExternalId());
    private Map<String, Long> expectedParams = new HashMap<>();

    @Bean
    DomainSessionFactory domainSessionFactory() {
        return new DomainSessionFactory(null, mock(ITransactionDataPersister.class), null, null);
    }

    @Before
    public void setUp() throws CommonException {
        when(bankInfo.getExternalTransactionHandlerClassName()).thenReturn(null);
        bankInfo.setId(BANK_ID);
        bankInfo.setProperty(BankInfo.KEY_PARSE_LONG, String.valueOf(true));
        bankInfo.setProperty(BankInfo.KEY_CW_REQUEST_CLIENT_CLASS, RESTCWClientMock.class.getName());
        BankInfoCache.getInstance().getAllObjects().clear();
        BankInfoCache.getInstance().put(bankInfo);

        when(dbLink.getMode()).thenReturn(GameMode.REAL);
        when(dbLink.getWallet()).thenReturn(cWallet);
        when(dbLink.getGameId()).thenReturn((long) GAME_ID);
        when(dbLink.getAccount()).thenReturn(accountInfo);
        when(dbLink.getWinAmount()).thenAnswer(invocationOnMock -> dbLinkWinAmount);
        doAnswer(invocationOnMock -> dbLinkWinAmount = invocationOnMock.getArgument(0))
                .when(dbLink).setWinAmount(anyLong());

        when(transactionIdGenerator.getNext(CommonWalletOperation.class)).
                thenAnswer(invocationOnMock -> transactionIdSequence.getAndIncrement());

        cWallet.addGameWallet(gameWallet);
        expectedParams.put(EXPECTED_BALANCE_PARAM, 500L);
        expectedParams.put(EXPECTED_ACCUMULATED_WIN_PARAM, 0L);
        accountInfo.setBalance(expectedParams.get(EXPECTED_BALANCE_PARAM));
        remoteAccountInfo.setBalance(expectedParams.get(EXPECTED_BALANCE_PARAM));
    }

    @Test
    public void testSimpleBets() throws CommonException {
        bankInfo.setProperty(BankInfo.KEY_CWM_TYPE, CWMType.SEND_WIN_ONLY.toString());
        cwManager = new CommonWalletManager(bankInfo, transactionIdGenerator);
        cwManager.init(mock(IWalletHelper.class));
        processGameLogic(100, 50, true, expectedParams);
        processGameLogic(25, 50, true, expectedParams);
        processGameLogic(25, 50, true, expectedParams);
    }

    @Test
    public void testSimpleBetsWinAccumulated() throws CommonException {
        bankInfo.setProperty(BankInfo.KEY_CWM_TYPE, CWMType.SEND_WIN_ACCUMULATED_AND_ISROUNDFINISHED.toString());
        cwManager = new CommonWalletManager(bankInfo, transactionIdGenerator);
        cwManager.init(mock(IWalletHelper.class));
        processGameLogic(100, 50, true, expectedParams);
        processGameLogic(25, 50, true, expectedParams);
        processGameLogic(25, 50, true, expectedParams);
    }

    @Test
    public void testSendWholeOrZeroBetsWinAccumulated() throws CommonException {
        bankInfo.setProperty(BankInfo.KEY_CWM_TYPE, CWMType.SEND_WIN_ACCUMULATED_AND_ISROUNDFINISHED.toString());
        cwManager = new CommonWalletManager(bankInfo, transactionIdGenerator);
        cwManager.init(mock(IWalletHelper.class));
        // Round 1.
        // Current accumulated win == 0, sending bet unchanged.
        processGameLogic(100, 50, false, expectedParams);
        // All bets are less then current accumulated win,
        // so bet amount is cleared and it should not be sent at all.
        processGameLogic(25, 0, false, expectedParams);
        processGameLogic(10, 80, false, expectedParams);
        processGameLogic(60, 50, true, expectedParams);

        // Round 2.
        processGameLogic(100, 120, false, expectedParams);
        processGameLogic(25, 0, false, expectedParams);
        processGameLogic(40, 50, true, expectedParams);
    }

    @Test
    public void testSendBetPartialWinAccumulated() throws CommonException {
        bankInfo.setProperty(BankInfo.KEY_CWM_TYPE, CWMType.SEND_WIN_ACCUMULATED_AND_ISROUNDFINISHED.toString());
        cwManager = new CommonWalletManager(bankInfo, transactionIdGenerator);
        cwManager.init(mock(IWalletHelper.class));
        // Current accumulated win == 0, sending bet unchanged.
        processGameLogic(100, 50, false, expectedParams);
        // Here bet > current accumulated win > 0,
        // so bet amount will be sent partially.
        processGameLogic(100, 0, false, expectedParams);
        processGameLogic(25, 50, true, expectedParams);
    }

    @Test
    public void testIsCreditConditionAppliedToWinAccumulated() throws CommonException {
        bankInfo.setProperty(BankInfo.KEY_CWM_TYPE, CWMType.SEND_WIN_ACCUMULATED.toString());
        cwManager = new CommonWalletManager(bankInfo, transactionIdGenerator);
        cwManager.init(mock(IWalletHelper.class));
        processGameLogic(100, 50, false, expectedParams);
        processGameLogic(25, 0, true, expectedParams);
    }

    private void processGameLogic(long bet, long win, boolean isRoundFinished, Map<String, Long> expectedParams)
            throws CommonException {
        long oldExpectedRemoteBalance = expectedParams.get(EXPECTED_BALANCE_PARAM);
        long oldExpectedAccumulatedWin = expectedParams.get(EXPECTED_ACCUMULATED_WIN_PARAM);

        checkBalance(oldExpectedRemoteBalance, oldExpectedRemoteBalance, oldExpectedAccumulatedWin);

        long newExpectedRemoteBalance = oldExpectedRemoteBalance;
        long newExpectedAccumulatedWin = oldExpectedAccumulatedWin;

        { // handle debit
            cwManager.handleDebit(accountInfo.getId(), bet, dbLink, sessionInfo, null, dbLink.getRoundId() );
            cwManager.handleDebitCompleted(accountInfo.getId(), true, dbLink, null);
            // Do manually what handleDebit should do
            newExpectedRemoteBalance -= bet;
            if (CWMType.getCWMTypeByString(bankInfo.getCWMType()).isWinAccumulated()) {
                if (bet > newExpectedAccumulatedWin) {
                    newExpectedAccumulatedWin = 0;
                } else {
                    newExpectedAccumulatedWin -= bet;
                }
            }
            // AccountInfo balance is updated in dbLink.sendBet, here should be unchanged
            checkBalance(oldExpectedRemoteBalance, newExpectedRemoteBalance, newExpectedAccumulatedWin);
        }

        { // processCommands/sendBet
            //dbLink.sendBet do this
            accountInfo.incrementBalance(-bet, win, false);
            //((IDBLink) dbLink).saveWinAmount(win);
            dbLink.setWinAmount(dbLink.getWinAmount() + win);
            checkBalance(newExpectedRemoteBalance + win, newExpectedRemoteBalance, newExpectedAccumulatedWin);
        }

        { // handle credit
            cwManager.handleCredit(accountInfo.getId(), isRoundFinished, dbLink, sessionInfo, null);
            cwManager.handleCreditCompleted(accountInfo.getId(), isRoundFinished, dbLink, null, dbLink.getRoundId());
            // Do manually what handleCredit should do
            newExpectedRemoteBalance += win;
            if (CWMType.getCWMTypeByString(bankInfo.getCWMType()).
                    isCreditCondition(newExpectedAccumulatedWin + win, 0, isRoundFinished)) {
                newExpectedAccumulatedWin = 0;
            } else {
                newExpectedAccumulatedWin += win;
            }
            checkBalance(newExpectedRemoteBalance, newExpectedRemoteBalance, newExpectedAccumulatedWin);
        }

        expectedParams.put(EXPECTED_BALANCE_PARAM, newExpectedRemoteBalance);
        expectedParams.put(EXPECTED_ACCUMULATED_WIN_PARAM, newExpectedAccumulatedWin);
    }

    private void checkBalance(long localExpectedBalance, long remoteExpectedBalance, long expectedAccumulatedWin) {
        assertEquals("Unexpected accountInfo balance", localExpectedBalance, accountInfo.getBalance());
        assertEquals("Unexpected accumulated win", expectedAccumulatedWin, gameWallet.getWinAmount());
        assertEquals("Unexpected remote balance", remoteExpectedBalance - expectedAccumulatedWin,
                remoteAccountInfo.getBalance());
    }
}