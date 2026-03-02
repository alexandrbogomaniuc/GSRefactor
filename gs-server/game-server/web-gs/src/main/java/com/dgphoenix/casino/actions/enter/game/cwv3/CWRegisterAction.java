package com.abs.casino.actions.enter.game.cwv3;

import com.abs.casino.account.AccountManager;
import com.abs.casino.actions.enter.game.BaseStartGameAction;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.exception.*;
import com.abs.casino.common.exception.MaintenanceModeException;
import com.abs.casino.common.exception.StartParameters;
import com.abs.casino.common.exception.CurrencyMismatchException;
import com.abs.casino.common.exception.InvalidCurrencyRateException;
import com.abs.casino.common.exception.UnknownCurrencyException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.common.util.DigitFormatter;
import com.abs.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.payment.currency.CurrencyManager;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.abs.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Struts action for launch game.
 * /CWRegisterv2.do?gameId=&mode=real&token=userToken&bankId=
 */
public class CWRegisterAction extends BaseStartGameAction<CWRegisterForm> {
    private static final Logger LOG = LogManager.getLogger(CWRegisterAction.class.getName() + "_v3");

    @Override
    protected Logger getLog() {
        return LOG;
    }

    /**
     * Performs checks, make login player to the server and redirects to the game launch page or errors.
     * @param mapping struts action mapping
     * @param actionForm {@code CWRegisterForm} action form.
     * @param request http request
     * @param response http response
     * @return {@code ActionForward} action redirect result
     * @throws Exception if any unexpected error occur
     */
    @Override
    protected ActionForward process(ActionMapping mapping, CWRegisterForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {

        try {
            long now = System.currentTimeMillis();

            LOG.debug("process: now={}, actionForm={}", now, actionForm);

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(actionForm.getBankId());
            StatisticsManager.getInstance().updateRequestStatistics("CWRegisterAction process 1",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();
            StatisticsManager.getInstance().updateRequestStatistics("CWRegisterAction process 2",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();

            LOG.debug("process: make an authentication request to external side actionForm.getToken()={}", actionForm.getToken());

            //Makes authentication request to external side.
            final CommonWalletAuthResult authResult = getAuthInfo(actionForm, actionForm.getToken(), bankInfo,
                    request.getRemoteHost(), request);

            LOG.debug("process: authResult={}", authResult);

            SessionHelper.getInstance().lock(actionForm.getBankId(), authResult.getUserId());
            boolean isLocked = true;

            try {

                //validates password from game server config for allow launch game.
                // (is not for production, usual used for  demo with password)
                validateMpPass(request, actionForm.getBankId());

                String authCurrency = CurrencyManager.getInstance().getCurrencyCodeByAlias(authResult.getCurrency(), bankInfo);
                final String externalId = authResult.getUserId();

                SessionHelper.getInstance().openSession();

                AccountInfo accountInfo = AccountManager.getInstance().getByCompositeKey(actionForm.getSubCasinoId(),
                        bankInfo, externalId);

                LOG.debug("process: accountInfo={}", accountInfo);

                //  If user is not found (for new players), creates new account info
                if (accountInfo == null) {

                    accountInfo = saveAccount(
                            null,
                            externalId,
                            authCurrency,
                            authResult.getUserName(),
                            authResult.getFirstName(),
                            authResult.getLastName(),
                            authResult.getEmail(),
                            authResult.getCountryCode(),
                            actionForm,
                            true);

                    LOG.debug("process: created new accountInfo={}", accountInfo);

                } else {

                    accountInfo = saveAccount(
                            accountInfo.getId(),
                            externalId,
                            authCurrency,
                            authResult.getUserName(),
                            authResult.getFirstName(),
                            authResult.getLastName(),
                            authResult.getEmail(),
                            authResult.getCountryCode(),
                            actionForm,
                            false);

                    LOG.debug("process: updated accountInfo={}", accountInfo);
                }

                StatisticsManager.getInstance().updateRequestStatistics("CWRegisterAction process 3",
                        System.currentTimeMillis() - now);

                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();

                return mapping.findForward(SUCCESS_FORWARD);

            } finally {
                if (isLocked) {
                    SessionHelper.getInstance().clearWithUnlock();
                } else {
                    getLog().debug("process: isLocked={}, skip SessionHelper.getInstance().clearWithUnlock()", isLocked);
                }
            }

        } catch (MaintenanceModeException e) {
            getLog().warn("process: {}", e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(GameServerConfiguration.getInstance().getMaintenancePage());
        } catch (UnknownCurrencyException e) {
            getLog().error("process: {}", e.getMessage());
            addError(request, "error.login.currencyCodeValidationError");
            return mapping.findForward(ERROR_FORWARD);
        } catch (InvalidCurrencyRateException e) {
            getLog().error("process: {}", e.getMessage());
            addError(request, "error.login.invalidCurrencyRate");
            return mapping.findForward(ERROR_FORWARD);
        } catch (CurrencyMismatchException e) {
            getLog().error("process: {}", e.getMessage());
            return mapping.findForward(ERROR_FORWARD);
        } catch (Exception e) {
            getLog().error("process: process error", e);
            if (e instanceof WalletException && (actionForm.getBankId() == 121 || actionForm.getBankId() == 221
                    || actionForm.getBankId() == 226)) {
                addError(request, "error.login.walletError121", String.valueOf(((WalletException) e).getAccountId()));
            } else {
                addErrorWithPersistence(request, "error.login.internalError", e, System.currentTimeMillis());
            }

            return mapping.findForward(ERROR_FORWARD);
        }
    }

    @Override
    protected void processCommonWalletAuthResult(CWRegisterForm form, CommonWalletAuthResult result, BankInfo bankInfo)
            throws CommonException {
        super.processCommonWalletAuthResult(form, result, bankInfo);
        long balance;
        if (bankInfo.isParseLong()) {
            balance = (long) result.getBalance();
        } else {
            balance = DigitFormatter.getCentsFromCurrency(result.getBalance());
        }
        form.setBalance(balance);
    }
}
