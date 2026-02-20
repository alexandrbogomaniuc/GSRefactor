package com.dgphoenix.casino.actions.api.frbonus;


import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.dgphoenix.casino.common.web.bonus.BonusErrors;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class AwardFRBAction extends AbstractBonusAction<AwardFRBForm> {

    private static final Logger LOG = LogManager.getLogger(AwardFRBAction.class);

    public AwardFRBAction() {
    }

    @Override
    protected ActionForward process(ActionMapping mapping, AwardFRBForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        long time = System.currentTimeMillis();
        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
        LOG.debug("request parameters: " + request.getParameterMap());

        inParams.put(BANK_ID_PARAM, String.valueOf(form.getBankId()));
        inParams.put(CBonus.PARAM_USERID, form.getUserId());
        inParams.put(CBonus.PARAM_ROUNDS, form.getRounds());
        inParams.put(CBonus.PARAM_GAMES, form.getGames());
        inParams.put(CBonus.PARAM_START_DATE, form.getStartTime());
        inParams.put(CBonus.PARAM_EXP_DATE, form.getExpirationTime());
        inParams.put(CBonus.PARAM_DURATION, form.getDuration());
        inParams.put(CBonus.PARAM_EXPIRATION_HOURS, form.getExpirationHours());
        inParams.put(CBonus.PARAM_COMMENT, form.getComment());
        inParams.put(CBonus.PARAM_DESCRIPTION, form.getDescription());
        inParams.put(CBonus.PARAM_EXTBONUSID, form.getExtBonusId());
        inParams.put("frbTableRoundChips", form.getFrbTableRoundChips());
        inParams.put(CBonus.PARAM_HASH, form.getHash());
        inParams.put(CBonus.PARAM_TIMEZONE, form.getTimeZone());
        inParams.put(CBonus.PARAM_MAX_WIN_LIMIT, form.getMaxWinLimit());

        try {
            long subCasinoId = form.getSubCasinoId();
            String extUserId = form.getUserId();

            if (StringUtils.isTrimmedEmpty(extUserId)) {
                throw new BonusException(BonusErrors.USER_NOT_FOUND);
            }
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            form.setSendDetailsOnFrbInfo(bankInfo.isSendDetailsOnFrbInfo());
            List<Long> gameList = getGameList(form.getGames(), form.getBankId(), bankInfo);

            // gameList is real game list
            String comment = form.getComment();
            String description = form.getDescription();

            String extBonusId = form.getExtBonusId();
            if (StringUtils.isTrimmedEmpty(extBonusId)) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }

            long frbTableRoundChips;
            try {
                if (StringUtils.isTrimmedEmpty(form.getFrbTableRoundChips()) || form.getFrbTableRoundChips().equals("null")) {
                    frbTableRoundChips = 0;
                } else {
                    frbTableRoundChips = Long.parseLong(form.getFrbTableRoundChips());
                }

            } catch (NumberFormatException e) {
                throw new BonusException(BonusErrors.INVALID_TABLE_CHIPS);
            }
            if (frbTableRoundChips < 0) {
                throw new BonusException(BonusErrors.INVALID_TABLE_CHIPS);
            }

            if (frbTableRoundChips == 0) {
                String frbDefChipsInCache = isActionGames(gameList) ? bankInfo.getMQFrbDefChips() : bankInfo.getFrbDefChips();
                if (frbDefChipsInCache != null) {
                    frbTableRoundChips = Long.valueOf(frbDefChipsInCache);
                }
            }

            long rounds = parseRounds(form.getRounds());
            Long duration = getDuration(form.getDuration());
            Long startTime = parseStartDateTime(form.getStartTime(), null);
            Long expTime = getExpTime(form.getExpirationHours(), request, startTime, form.getExpirationTime());
            validateStartAndExpTime(startTime, expTime);

            Long maxWinLimit = parseMaxWinLimit(form.getMaxWinLimit());

            String hash = form.getHash();

            boolean isHashValueEnabled = bankInfo.isHashValueEnable();

            if (isHashValueEnabled) {
                if (hash == null) {
                    LOG.error("Hash is null");
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
                List<String> paramList = new ArrayList<>();
                paramList.add(extUserId);
                paramList.add(String.valueOf(bankInfo.getExternalBankId()));
                paramList.add(form.getRounds());
                paramList.add(form.getGames());
                if (!StringUtils.isTrimmedEmpty(comment)) {
                    paramList.add(comment);
                }
                if (!StringUtils.isTrimmedEmpty(description)) {
                    paramList.add(description);
                }
                if (!StringUtils.isTrimmedEmpty(form.getMaxWinLimit())) {
                    paramList.add(form.getMaxWinLimit());
                }
                paramList.add(extBonusId);
                if (!hash.equals(getHashValue(paramList, form.getBankId()))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ":validate time", System.currentTimeMillis() - time);

            long statisticStartTime = System.currentTimeMillis();
            SessionHelper.getInstance().lock(form.getBankId(), form.getUserId());
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ":lock time", System.currentTimeMillis() - statisticStartTime);
            try {
                SessionHelper.getInstance().openSession();

                AccountInfo accountInfo = getAccountInfo(subCasinoId, bankInfo, extUserId);
                FRBonus bonus = FRBonusManager.getInstance().get(form.getBankId(), extBonusId);
                if (bonus != null) {
                    long bonusId = bonus.getId();
                    outParams.put(CBonus.BONUSID, bonusId);
                    throw new BonusException(BonusErrors.OPERATION_ALREADY_EXIST);
                }


                try {
                    LOG.debug("Start bonus awarding");
                    statisticStartTime = System.currentTimeMillis();
                    bonus = FRBonusManager.getInstance().awardBonus(accountInfo, rounds, extBonusId, gameList,
                            description, comment, System.currentTimeMillis(), false, startTime, expTime, duration,
                            frbTableRoundChips, null, maxWinLimit);
                    StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ":awardBonus time", System.currentTimeMillis() - statisticStartTime);
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.PARAM_BONUSID, bonus.getId());
                    LOG.debug("End bonus awarding, bonusId = {}", bonus.getId());
                } catch (BonusException e) {
                    throw new BonusException(e, BonusErrors.INTERNAL_ERROR);
                }
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (BonusException e) {
            LOG.error(e.getMessage(), e);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            BonusError bonusError = e.getBonusError();
            outParams.put(CBonus.CODE_TAG, (bonusError != null) ? bonusError.getCode() : BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG, (bonusError != null) ? bonusError.getDescription() : BonusErrors.INTERNAL_ERROR.getDescription());
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
        }

        LOG.debug("Send frb award response");
        buildResponseXML(xw, inParams, outParams, form);
        response.getWriter().flush();
        LOG.debug("Frb award is done");
        return null;
    }

    protected AccountInfo getAccountInfo(long subCasinoId, BankInfo bankInfo, String extUserId) throws CommonException {
        AccountInfo accountInfo = AccountManager.getInstance().getByCompositeKey(subCasinoId, bankInfo, extUserId);
        if (accountInfo == null) {
            try {
                long statisticStartTime = System.currentTimeMillis();
                accountInfo = AccountManager.getInstance().createAccount(subCasinoId, bankInfo,
                        extUserId, BonusSystemType.FRB_SYSTEM);
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ":create account time", System.currentTimeMillis() - statisticStartTime);
            } catch (BonusException e) {
                getLogger().debug("createAccount error", e);
                throw new BonusException(BonusErrors.USER_NOT_FOUND);
            }
        }
        return accountInfo;
    }

    protected long parseRounds(String roundsStr) throws BonusException {
        long rounds;
        try {
            rounds = Long.parseLong(roundsStr);
            if (rounds < 1) {
                LOG.error("Incorrect rounds! rounds:" + rounds);
                throw new BonusException(BonusErrors.INVALID_ROUNDS);
            }
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            throw new BonusException(BonusErrors.INVALID_ROUNDS);
        }
        return rounds;
    }

    protected List<Long> getGameList(String gameId, long bankId, BankInfo bankInfo) throws BonusException {
        if (StringUtils.isTrimmedEmpty(gameId)) {
            throw new BonusException(BonusErrors.INVALID_GAMES_ID);
        }

        String[] gameIds = gameId.trim().split("\\|");
        List<Long> gameList = BonusManager.getInstance().getInternalListGamesIds(gameIds, bankId);
        Set<Long> actionGames = BaseGameInfoTemplateCache.getInstance().getMultiplayerGames();
        boolean isContainsActionGames = gameList.stream().anyMatch(actionGames::contains);
        boolean isContainsNonActionGames = gameList.stream().anyMatch(game -> !actionGames.contains(game));
        boolean incorrectGameIdsValue = isContainsActionGames && isContainsNonActionGames;
        if (incorrectGameIdsValue || !isPermittedGames(gameList, bankInfo)) {
            throw new BonusException(BonusErrors.INVALID_GAMES_ID);
        }
        return gameList;
    }

    protected Long getExpTime(String sExpirationHours, HttpServletRequest request, Long startTime, String expirationTime) throws BonusException {
        Long expTime;
        if (!StringUtils.isTrimmedEmpty(sExpirationHours)) {
            long expirationHours;
            try {
                expirationHours = Long.parseLong(sExpirationHours);
            } catch (Exception e) {
                throw new BonusException(BonusErrors.INVALID_EXP_HOURS);
            }
            if (expirationHours < 1) {
                throw new BonusException(BonusErrors.INVALID_EXP_HOURS);
            }
            expTime = (startTime != null ? startTime : System.currentTimeMillis()) +
                    expirationHours * 60L * 60L * 1000L;
        } else {
            expTime = parseExpDateTime(expirationTime, request.getParameter(CBonus.PARAM_TIMEZONE),
                    BonusErrors.INVALID_EXP_TIME);
        }
        return expTime;
    }

    protected void validateStartAndExpTime(Long startTime, Long expTime) throws BonusException {
        if (startTime != null && expTime != null && expTime <= startTime) {
            LOG.error("Incorrect Dates combination!");
            throw new BonusException(BonusErrors.INVALID_DATES_COMBINATION);
        }
    }

    protected Long getDuration(String sDuration) throws BonusException {
        Long duration = null;
        try {
            if (sDuration != null && !StringUtils.isTrimmedEmpty(sDuration)) {
                duration = Long.parseLong(sDuration);
                if (duration < 1) {
                    LOG.error("Incorrect Duration! duration:" + duration);
                    throw new BonusException(BonusErrors.INVALID_DURATION);
                }
            }
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            throw new BonusException(BonusErrors.INVALID_DURATION);
        }
        return duration;
    }

    private boolean isPermittedGames(List<Long> gameIds, BankInfo bankInfo) {
        checkNotNull(gameIds, "::isPermittedGames error, gameIds list is null");
        checkNotNull(bankInfo, "::isPermittedGames error, bankInfo is null");

        LOG.debug("Validating input frb games set");
        Set<Long> frbGames = BankInfoCache.getInstance().getFrbGames(bankInfo);

        return frbGames.containsAll(gameIds);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
