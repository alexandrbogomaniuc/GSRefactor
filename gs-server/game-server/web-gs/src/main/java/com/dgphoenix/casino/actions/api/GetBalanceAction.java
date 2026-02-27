package com.abs.casino.actions.api;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.ClientTypeFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.abs.casino.gs.managers.payment.wallet.v3.ICommonWalletClient;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: flsh
 * Date: 3/1/12
 */
public class GetBalanceAction extends BaseAction<GetBalanceForm> {
    private static final Logger LOG = LogManager.getLogger(GetBalanceAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, GetBalanceForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        String sessionId = form.getSid();
        boolean hasChanges = false;
        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();
            final SessionInfo sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
            if (sessionInfo == null) {
                LOG.warn("SessionInfo not found, SID=" + sessionId);
                response.getWriter().println("ERROR=" + getMessage("error.deposit.FAIL_LOGIN_INVALID"));
            } else {
                long balance = 0;
                AccountInfo account = AccountManager.getInstance().getAccountInfo(sessionInfo.getAccountId());
                if (account.isGuest()) {
                    balance = account.getFreeBalance();
                } else {
                    if (!StringUtils.isTrimmedEmpty(form.getRefresh())) {
                        if (WalletProtocolFactory.getInstance().isWalletBank(account.getBankId())) {
                            IWalletProtocolManager ocwm = WalletProtocolFactory.getInstance().
                                    getWalletProtocolManager(account.getBankId());
                            ICommonWalletClient client = (ICommonWalletClient) ocwm.getClient();
                            final CommonWalletAuthResult auth = client.auth(sessionInfo.getExternalSessionId(),
                                    ClientTypeFactory.getByHttpRequest(request));
                            if (auth != null) {
                                final long authBalance = DigitFormatter.getCentsFromCurrency(auth.getBalance());
                                LOG.info("Refresh, account=" + account + ", authBalance=" + authBalance);
                                account.setBalance(authBalance);
                                hasChanges = true;
                            } else {
                                throw new CommonException("Cannot refresh balance auth is null");
                            }
                        }
                    }
                    balance = account.getBalance();
                }
                if (hasChanges) {
                    SessionHelper.getInstance().commitTransaction();
                }
                response.getWriter().println(String.valueOf(balance));
            }
            SessionHelper.getInstance().markTransactionCompleted();
        } catch (Throwable t) {
            LOG.error("Cannot get balance, SID=" + sessionId, t);
            response.getWriter().println("ERROR=" + getMessage("error.login.internalError"));
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
        response.getWriter().flush();
        return null;
    }
}
