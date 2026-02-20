package com.dgphoenix.casino.actions.api.frbonus;


import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.dgphoenix.casino.common.web.bonus.BonusErrors;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class AwardFRBLiteAction extends AbstractBonusAction<AwardFRBLiteForm> {

    private static final Logger LOG = LogManager.getLogger(AwardFRBLiteAction.class);

    public AwardFRBLiteAction() {
    }

    @Override
    protected ActionForward process(ActionMapping mapping, AwardFRBLiteForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {

        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
        try {
            inParams.put(BANK_ID_PARAM, String.valueOf(form.getBankId()));
            inParams.put(CBonus.PARAM_USERID, form.getUserId());
            inParams.put(CBonus.PARAM_ROUNDS, form.getRounds());
            inParams.put(CBonus.PARAM_GAMES, form.getGames());
            inParams.put(CBonus.PARAM_START_DATE, form.getStartTime());
            inParams.put(CBonus.PARAM_EXP_DATE, form.getExpirationTime());
            inParams.put(CBonus.PARAM_DURATION, form.getDuration());
            inParams.put(CBonus.PARAM_COMMENT, form.getComment());
            inParams.put(CBonus.PARAM_DESCRIPTION, form.getDescription());
            inParams.put(CBonus.PARAM_EXTBONUSID, form.getExtBonusId());
            inParams.put(CBonus.PARAM_HASH, form.getHash());
            inParams.put(CBonus.PARAM_TIMEZONE, form.getTimeZone());
            short subCasinoId = form.getSubCasinoId();
            String extUserId = form.getUserId();

            if (StringUtils.isTrimmedEmpty(extUserId)) {
                throw new BonusException(BonusErrors.USER_NOT_FOUND);
            }
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            form.setSendDetailsOnFrbInfo(bankInfo.isSendDetailsOnFrbInfo());
            AccountInfo accountInfo = AccountManager.getInstance().getByCompositeKey(subCasinoId, form.getBankId(),
                    extUserId);
            if (accountInfo == null) {
                try {
                    accountInfo = AccountManager.getInstance().createAccount((long) subCasinoId, bankInfo, extUserId,
                            BonusSystemType.FRB_SYSTEM);
                } catch (BonusException e) {
                    throw new BonusException(BonusErrors.USER_NOT_FOUND);
                }
            }

            List<Long> gameList;
            String gameId = form.getGames();
            if (StringUtils.isTrimmedEmpty(gameId)) {
                throw new BonusException(BonusErrors.INVALID_GAMES_ID);
            } else {
                boolean correctGameIdsValue = true;
                String[] gameIds = gameId.trim().split("\\|");
                gameList = BonusManager.getInstance().getInternalListGamesIds(gameIds, form.getBankId());
                if (!correctGameIdsValue || !isPermittedGames(gameList, form.getBankId())) {
                    throw new BonusException(BonusErrors.INVALID_GAMES_ID);
                }
                // gameList is real game list
                String comment = form.getComment();
                String description = form.getDescription();

                String extBonusId = form.getExtBonusId();
                if (StringUtils.isTrimmedEmpty(extBonusId)) {
                    throw new BonusException(BonusErrors.INVALID_BONUS_ID);
                }
                long rounds;
                try {
                    rounds = Long.parseLong(form.getRounds());
                    if (rounds < 1) {
                        LOG.error("Incorrect rounds! rounds:" + rounds);
                        throw new BonusException(BonusErrors.INVALID_ROUNDS);
                    }
                } catch (NumberFormatException e) {
                    LOG.error(e.getMessage(), e);
                    throw new BonusException(BonusErrors.INVALID_ROUNDS);
                }

                long frbTableRoundChips;
                try {
                    if (form.getFrbTableRoundChips() == null || form.getFrbTableRoundChips().equals("null")) {
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
                Long duration = null;
                try {
                    if (form.getDuration() != null && !StringUtils.isTrimmedEmpty(form.getDuration())) {
                        duration = Long.parseLong(form.getDuration());
                        if (duration < 1) {
                            LOG.error("Incorrect Duration! duration:" + duration);
                            throw new BonusException(BonusErrors.INVALID_DURATION);
                        }
                    }
                } catch (NumberFormatException e) {
                    LOG.error(e.getMessage(), e);
                    throw new BonusException(BonusErrors.INVALID_DURATION);
                }

                Long startTime = parseStartDateTime(form.getStartTime(), null);
                Long expTime = parseExpDateTime(form.getExpirationTime(), request.getParameter(CBonus.PARAM_TIMEZONE),
                        BonusErrors.INVALID_EXP_TIME);

                if (startTime != null && expTime != null) {
                    long deltaSelectedTimes = expTime - startTime;
                    if (deltaSelectedTimes <= 0) {
                        LOG.error("Incorrect Dates combination!");
                        throw new BonusException(BonusErrors.INVALID_DATES_COMBINATION);
                    }
                }
                String hash = form.getHash();
                boolean isHashValueEnabled = BankInfoCache.getInstance().
                        getBankInfo(accountInfo.getBankId()).isHashValueEnable();
                if (isHashValueEnabled) {
                    List<String> paramList = new ArrayList();
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
                    paramList.add(extBonusId);
                    if (!hash.equals(getHashValue(paramList, form.getBankId()))) {
                        throw new BonusException(BonusErrors.INVALID_HASH);
                    }
                }
                FRBonus bonus = FRBonusManager.getInstance().get(form.getBankId(), extBonusId);
                if (bonus != null) {
                    long bonusId = bonus.getId();
                    outParams.put(CBonus.BONUSID, bonusId);
                    throw new BonusException(BonusErrors.OPERATION_ALREADY_EXIST);
                }

                String profile = form.getProfile();
                try {
                    bonus = FRBonusManager.getInstance().awardBonus(accountInfo, rounds, extBonusId, gameList,
                            description, comment, System.currentTimeMillis(), false, startTime, expTime, duration,
                            frbTableRoundChips, null, null);
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.PARAM_BONUSID, bonus.getId());
                } catch (BonusException e) {
                    LOG.error(e.getMessage(), e);
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
                    outParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
                    outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
                }
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
        buildResponseXML(xw, inParams, outParams, form);
        response.getWriter().flush();
        return null;
    }

    private boolean isPermittedGames(List<Long> gameIds, long bankId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        Set<Long> frbGames = BankInfoCache.getInstance().getFrbGames(bankInfo);

        for (long gameId : gameIds) {
            if (!frbGames.contains(Long.valueOf(gameId))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
