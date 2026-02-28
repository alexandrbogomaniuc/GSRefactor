package com.abs.casino.actions.api;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.exception.AccountException;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.abs.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.wallet.CCommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User: van0ss
 * Date: 22.12.2016
 * Refresh balance in Account, difference with GetBalanceAction with 'refresh' parameter is calling method
 * client.getBalance instead of client.auth and without sessionId
 * At this moment 17.02.2016 no one client uses this API
 */
public class RefreshBalanceAction extends BaseAction<RefreshBalanceForm> {
    private static final Logger LOG = Logger.getLogger(RefreshBalanceAction.class);
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected ActionForward process(ActionMapping mapping, RefreshBalanceForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        LOG.debug(actionForm);
        BonusError error = null;
        long balance = 0;

        int bankId = actionForm.getBankId();
        String extUserId = actionForm.getExtUserId();
        if (StringUtils.isTrimmedEmpty(extUserId)) {
            throw new IllegalArgumentException("Empty extUserId");
        }
        SessionHelper.getInstance().lock(bankId, extUserId);
        try {
            SessionHelper.getInstance().openSession();
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            AccountInfo account = AccountManager.getInstance().getByCompositeKey(actionForm.getSubCasinoId(),
                    actionForm.getBankId(), actionForm.getExtUserId());
            if (account == null) {
                throw new AccountException("User not found");
            }
            if (WalletProtocolFactory.getInstance().isWalletBank(bankId)) {
                IWalletProtocolManager manager = WalletProtocolFactory.getInstance().getWalletProtocolManager(bankId);
                ICommonWalletClient client = manager.getClient();
                double dBalance = client.getBalance(account.getId(), account.getExternalId(), bankId, account.getCurrency());
                if (bankInfo.isParseLong()) {
                    balance = (long) dBalance;
                } else {
                    balance = DigitFormatter.getCentsFromCurrency(dBalance);
                }
            } else {
                balance = account.getBalance();
            }
            LOG.info("RefreshBalance, account=" + account + ", got balance=" + balance);
            account.setBalance(balance);
            SessionHelper.getInstance().commitTransaction();
            SessionHelper.getInstance().markTransactionCompleted();
        } catch (IllegalArgumentException e) {
            LOG.error(e);
            error = BonusErrors.INVALID_PARAMETERS;
        } catch (AccountException e) {
            LOG.error(e);
            error = BonusErrors.USER_NOT_FOUND;
        } catch (Exception e) {
            LOG.error(e);
            error = BonusErrors.INTERNAL_ERROR;
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");
        XmlWriter xw = new XmlWriter(response.getWriter(), "UTF-8");
        xw.startDocument(GameServerConfiguration.getInstance().getBrandApiRootTagName());
        xw.startNode(CCommonWallet.REQUEST_TAG);
        xw.node("EXTUSERID", actionForm.getExtUserId());
        xw.node("BANKID", String.valueOf(bankId));
        xw.endNode(CCommonWallet.REQUEST_TAG);
        xw.node(CCommonWallet.TIME_TAG, DF.format(LocalDateTime.now()));
        xw.startNode(CCommonWallet.RESPONSE_TAG);
        String result = CCommonWallet.RESULT_OK;
        if (error != null && error.getCode() > 0) {
            result = CCommonWallet.RESULT_FAILED;
        }
        xw.node(CCommonWallet.RESULT_TAG, result);
        if (error != null && error.getCode() > 0) {
            xw.node(CCommonWallet.CODE_ATTRIBUTE, String.valueOf(error.getCode()));
            xw.node(CBonus.DESCRIPTION_TAG, String.valueOf(error.getDescription()));
        } else {
            xw.node(CCommonWallet.BALANCE_TAG, String.valueOf(balance));
        }
        xw.endNode(CCommonWallet.RESPONSE_TAG);
        xw.endDocument(GameServerConfiguration.getInstance().getBrandApiRootTagName());
        return null;
    }
}
