package com.abs.casino.web.bonus;

import com.abs.casino.account.AccountManager;
import com.abs.casino.actions.api.bonus.AbstractBonusAction;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.BaseGameCache;
import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.cache.data.bonus.BonusGameMode;
import com.abs.casino.common.cache.data.bonus.BonusType;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.exception.BonusException;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import com.abs.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.abs.casino.gs.managers.payment.bonus.BonusManager;
import com.abs.casino.gs.managers.payment.bonus.IBonusClient;
import com.abs.casino.gs.managers.payment.bonus.IBonusManager;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;
import com.abs.casino.services.bonus.ForbiddenGamesForBonusProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: ktd
 * Date: 31.03.11
 */
public class AwardBonusServlet extends AbstractBonusServlet {
    private static final Logger LOG = LogManager.getLogger(AwardBonusServlet.class);

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        XmlWriter xw = new XmlWriter(response.getWriter());
        HashMap inParams = new HashMap();
        HashMap outParams = new HashMap();
        Long bankId;
        Long subCasinoId;

        try {
            inParams.put(BANK_ID_PARAM, request.getParameter(BANK_ID_PARAM));
            inParams.put(CBonus.PARAM_USERID, request.getParameter(CBonus.PARAM_USERID));
            inParams.put(CBonus.PARAM_TYPE, request.getParameter(CBonus.PARAM_TYPE));
            inParams.put(CBonus.PARAM_AMOUNT, request.getParameter(CBonus.PARAM_AMOUNT));
            inParams.put(CBonus.PARAM_MULTIPLIER, request.getParameter(CBonus.PARAM_MULTIPLIER));
            inParams.put(CBonus.PARAM_GAMES, request.getParameter(CBonus.PARAM_GAMES));
            inParams.put(CBonus.PARAM_GAMEIDS, request.getParameter(CBonus.PARAM_GAMEIDS));
            inParams.put(CBonus.PARAM_EXPDATE, request.getParameter(CBonus.PARAM_EXPDATE));
            inParams.put(CBonus.PARAM_COMMENT, request.getParameter(CBonus.PARAM_COMMENT));
            inParams.put(CBonus.PARAM_DESCRIPTION, request.getParameter(CBonus.PARAM_DESCRIPTION));
            inParams.put(CBonus.PARAM_EXTBONUSID, request.getParameter(CBonus.PARAM_EXTBONUSID));
            inParams.put(CBonus.PARAM_HASH, request.getParameter(CBonus.PARAM_HASH));
            inParams.put(CBonus.PARAM_TIMEZONE, request.getParameter(CBonus.PARAM_TIMEZONE));
            String autoReleaseParam = request.getParameter(CBonus.PARAM_AUTO_RELEASE);
            inParams.put(CBonus.PARAM_AUTO_RELEASE, autoReleaseParam);
            inParams.put(CBonus.PARAM_START_DATE, request.getParameter(CBonus.PARAM_START_DATE));
            inParams.put(CBonus.PARAM_MAX_WIN_LIMIT, request.getParameter(CBonus.PARAM_MAX_WIN_LIMIT));

            String sBankId = request.getParameter(BANK_ID_PARAM);
            if (StringUtils.isTrimmedEmpty(sBankId)) {
                throw new BonusException(BonusErrors.INVALID_PARAMETERS);
            }

            try {
                bankId = Long.parseLong(sBankId);
            } catch (NumberFormatException e) {
                throw new BonusException(e, BonusErrors.INVALID_PARAMETERS);
            }

            String sSubCasino = request.getParameter(SUBCASINO_ID_PARAM);
            subCasinoId = Long.parseLong(sSubCasino);

            String extUserId = request.getParameter(CBonus.PARAM_USERID);
            if (StringUtils.isTrimmedEmpty(extUserId)) {
                throw new BonusException(BonusErrors.USER_NOT_FOUND);
            }
            String type = request.getParameter(CBonus.PARAM_TYPE);
            if (StringUtils.isTrimmedEmpty(type)) {
                throw new BonusException(BonusErrors.INVALID_BONUS_TYPE);
            }

            boolean typeRecognized = false;
            for (BonusType knownType : BonusType.values()) {
                if (type.equalsIgnoreCase(knownType.name())) {
                    typeRecognized = true;
                    break;
                }
            }
            if (!typeRecognized) {
                throw new BonusException(BonusErrors.INVALID_BONUS_TYPE);
            }

            String sAmount = request.getParameter(CBonus.PARAM_AMOUNT);
            if (StringUtils.isTrimmedEmpty(sAmount)) {
                throw new BonusException(BonusErrors.INVALID_OR_EMPTY_AMOUNT);
            }
            int amount;
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId.intValue());
            checkArgument(!bankInfo.isCheckAccountOnOldSystem(), "Migration from old system in progress");
            try {
                amount = Integer.parseInt(sAmount);
                if (amount < bankInfo.getBonusThresholdMinKey()) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw new BonusException(e, BonusErrors.INVALID_OR_EMPTY_AMOUNT);
            }

            String sMultiplier = request.getParameter(CBonus.PARAM_MULTIPLIER);
            String sMultiplierAmount = "-1";
            if (!StringUtils.isTrimmedEmpty(request.getParameter(CBonus.PARAM_ROLLOVER_AMOUNT)) && !"null".equals(
                    request.getParameter(CBonus.PARAM_ROLLOVER_AMOUNT))) {
                sMultiplierAmount = request.getParameter(CBonus.PARAM_ROLLOVER_AMOUNT);
                if (StringUtils.isTrimmedEmpty(sMultiplierAmount)) {
                    throw new BonusException(BonusErrors.INVALID_ROLLOVER_AMOUNT);
                }
            } else {
                if (StringUtils.isTrimmedEmpty(sMultiplier)) {
                    throw new BonusException(BonusErrors.INVALID_MULTIPLIER);
                }
            }

            double multiplier;
            double multiplierAmount;
            if (!StringUtils.isTrimmedEmpty(request.getParameter(CBonus.PARAM_ROLLOVER_AMOUNT)) && !"null".equals(
                    request.getParameter(CBonus.PARAM_ROLLOVER_AMOUNT))) {
                try {
                    multiplierAmount = Double.parseDouble(sMultiplierAmount);

                    if (multiplierAmount < 1) {
                        throw new NumberFormatException();
                    }
                    multiplier = multiplierAmount / amount;
                    if (multiplier < 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new BonusException(e, BonusErrors.INVALID_ROLLOVER_AMOUNT);
                }
            } else {
                try {
                    multiplier = Double.parseDouble(sMultiplier);

                    if (multiplier < 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new BonusException(e, BonusErrors.INVALID_MULTIPLIER);
                }
            }

            String games = request.getParameter(CBonus.PARAM_GAMES);
            if (StringUtils.isTrimmedEmpty(games)) {
                throw new BonusException(BonusErrors.INVALID_GAMES_MODE);
            }

            boolean modeRecognized = false;
            for (BonusGameMode knownMode : BonusGameMode.values()) {
                if (games.equalsIgnoreCase(knownMode.name())) {
                    modeRecognized = true;
                    break;
                }
            }
            if (!modeRecognized) {
                throw new BonusException(BonusErrors.INVALID_GAMES_MODE);
            }

            List<Long> gameList = null;
            String gameId = request.getParameter(CBonus.PARAM_GAMEIDS);
            if (!BonusGameMode.valueOf(games.toUpperCase()).equals(BonusGameMode.ALL)) {
                if (StringUtils.isTrimmedEmpty(gameId)) {
                    throw new BonusException(BonusErrors.INVALID_GAMES_ID);
                } else {
                    String[] gameIds = gameId.trim().split("\\|");
                    for (String gid : gameIds) {
                        LOG.debug(gid);
                    }
                    try {
                        gameList = BonusManager.getInstance().getInternalListGamesIds(gameIds, bankId);
                    } catch (Exception e) {
                        throw new BonusException(e, BonusErrors.INVALID_GAMES_ID);
                    }
                    LOG.debug("after internal parse:" + gameList);
                    ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider = ApplicationContextHelper.getApplicationContext()
                            .getBean(ForbiddenGamesForBonusProvider.class);
                    Set<Long> forbiddenGamesForBonus = forbiddenGamesForBonusProvider.getGames(bankId);
                    gameList.removeAll(forbiddenGamesForBonus);
                    Set<Long> allGameIds = BaseGameCache.getInstance().getAllGamesSet(bankId,
                            bankInfo.getDefaultCurrency());
                    Collection<Long> realGameList = getValidGameIds(allGameIds,
                            BonusGameMode.valueOf(games.toUpperCase()), gameList);
                    if (realGameList == null || realGameList.isEmpty()) {
                        LOG.error("realGameList is empty: bankInfo=" + bankInfo +
                                ", allGameIds.size=" + allGameIds + ", GameTemplates.size=" +
                                BaseGameInfoTemplateCache.getInstance().printDebug() +
                                ", realGameList=" + realGameList + ", bankId=" + bankId);
                        throw new BonusException(BonusErrors.INVALID_GAMES_ID);
                    }
                }
            }
            Long startTime = AbstractBonusAction.parseStartDateTime(request.getParameter(CBonus.PARAM_START_DATE), null);

            String sExpDate = request.getParameter(CBonus.PARAM_EXPDATE);
            if (StringUtils.isTrimmedEmpty(sExpDate)) {
                throw new BonusException(BonusErrors.INVALID_EXP_DATE);
            }
            Long expTime = AbstractBonusAction.parseExpDateTime(sExpDate, request.getParameter(CBonus.PARAM_TIMEZONE),
                    BonusErrors.INVALID_EXP_DATE);
            if (startTime != null && expTime != null && expTime <= startTime) {
                LOG.error("Incorrect Dates combination!");
                throw new BonusException(BonusErrors.INVALID_DATES_COMBINATION);
            }

            String comment = request.getParameter(CBonus.PARAM_COMMENT);
            String description = request.getParameter(CBonus.PARAM_DESCRIPTION);

            String extBonusId = request.getParameter(CBonus.PARAM_EXTBONUSID);
            if (StringUtils.isTrimmedEmpty(extBonusId)) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }

            String hash = request.getParameter(CBonus.PARAM_HASH);
            if (StringUtils.isTrimmedEmpty(hash)) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }

            boolean autoRelease = true;
            if (!StringUtils.isTrimmedEmpty(autoReleaseParam)) {
                if (!autoReleaseParam.equalsIgnoreCase(Boolean.TRUE.toString())
                        && !autoReleaseParam.equalsIgnoreCase(Boolean.FALSE.toString())) {
                    throw new BonusException(BonusErrors.INVALID_AUTO_RELEASE_PARAM);
                }
                autoRelease = Boolean.parseBoolean(autoReleaseParam);
            }

            String sMaxWinMultiplier = request.getParameter(CBonus.PARAM_MAX_WIN_MULTIPLIER);
            Double maxWinMultiplier = sMaxWinMultiplier == null ? null : Double.parseDouble(sMaxWinMultiplier);
            boolean isHashValueEnabled = bankInfo.isHashValueEnable();

            if (isHashValueEnabled) {

                /**
                 * usedId, bankId, type, amount, multiplier, games, gameIds, extDate, comment, description, extBonusId
                 */

                List<String> paramList = new ArrayList<String>();
                paramList.add(extUserId);
                paramList.add(sBankId);
                paramList.add(type);
                paramList.add(sAmount);
                paramList.add(sMultiplier);
                paramList.add(games);

                if (!StringUtils.isTrimmedEmpty(gameId)) paramList.add(gameId);

                paramList.add(sExpDate);

                if (!StringUtils.isTrimmedEmpty(comment)) paramList.add(comment);
                if (!StringUtils.isTrimmedEmpty(description)) paramList.add(description);

                paramList.add(extBonusId);

                if (!StringUtils.isTrimmedEmpty(autoReleaseParam)) {
                    paramList.add(autoReleaseParam);
                }

                if (!hash.equals(getHashValue(paramList, bankId))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            Bonus bonus = BonusManager.getInstance().get(bankId, extBonusId);
            if (bonus != null) {
                long bonusId = bonus.getId();
                outParams.put(CBonus.BONUSID, bonusId);
                throw new BonusException(BonusErrors.OPERATION_ALREADY_EXIST);
            }
            SessionHelper.getInstance().lock(bankId.intValue(), extUserId);
            try {
                SessionHelper.getInstance().openSession();
                AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(subCasinoId.shortValue(),
                        bankId.intValue(), extUserId);
                if (accountInfo == null) {
                    accountInfo = createAccount(bankInfo, extUserId);
                }
                bonus = BonusManager.getInstance().awardBonus(accountInfo, BonusType.valueOf(type.toUpperCase()),
                        amount, multiplier, extBonusId, gameList, BonusGameMode.valueOf(games.toUpperCase()),
                        description, comment, expTime + 1000 * 60 * 60 * 24 - 1, System.currentTimeMillis(),
                        false, false, autoRelease, startTime, maxWinMultiplier);
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                outParams.put(CBonus.PARAM_BONUSID, bonus.getId());
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (BonusException e) {
            LOG.error(e.getMessage(), e);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            BonusError bonusError = e.getBonusError();
            outParams.put(CBonus.CODE_TAG,
                    (bonusError != null) ? bonusError.getCode() : BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG,
                    (bonusError != null) ? bonusError.getDescription() : BonusErrors.INTERNAL_ERROR.getDescription());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
        }
        buildResponseXML(xw, inParams, outParams);
    }

    private AccountInfo createAccount(BankInfo bankInfo, String accountExtId) throws BonusException {
        try {
            long bankId = bankInfo.getId();
            IBonusManager bonusManager = BonusManager.getInstance();
            IBonusClient client = bonusManager.getClient(bankId);
            BonusAccountInfoResult result = client.getAccountInfo(accountExtId);

            return AccountManager.getInstance().saveAccountWithCurrencyUpdate(null, accountExtId, bankInfo,
                    result.getUserName(), false, false, result.getEmail(), ClientType.FLASH, result.getFirstName(),
                    result.getLastName(), result.getCurrency(), result.getCountryCode(), true);
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    public Collection<Long> getValidGameIds(Collection<Long> fullList, BonusGameMode mode, Collection<Long> gameIds) {
        if (mode.equals(BonusGameMode.ALL)) {
            return fullList;
        }
        List<Long> validGameIds = new ArrayList();
        for (Long gameId : fullList) {
            if ((mode.equals(BonusGameMode.ONLY) && gameIds != null && gameIds.contains(gameId)) ||
                    (mode.equals(BonusGameMode.EXCEPT) && gameIds != null && !gameIds.contains(gameId)))
                validGameIds.add(gameId);
        }
        return validGameIds;
    }
}
