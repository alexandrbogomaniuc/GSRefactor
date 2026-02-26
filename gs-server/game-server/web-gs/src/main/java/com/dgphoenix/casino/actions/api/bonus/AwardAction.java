package com.dgphoenix.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.AwardForm;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusGameMode;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.bonus.BonusType;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.dgphoenix.casino.common.web.bonus.BonusErrors;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.services.bonus.ForbiddenGamesForBonusProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class AwardAction extends AbstractBonusAction<AwardForm> {
    private static final Logger LOG = LogManager.getLogger(AwardAction.class);
    private static final long MAX_AMOUNT = 99_999_999_999_999L;

    private final ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider;

    public AwardAction() {
        forbiddenGamesForBonusProvider = ApplicationContextHelper.getApplicationContext()
                .getBean(ForbiddenGamesForBonusProvider.class);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, AwardForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
        try {
            Integer bankId = form.getBankId();
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            checkArgument(!bankInfo.isCheckAccountOnOldSystem(), "Migration from old system in progress");

            inParams.put(BANK_ID_PARAM, String.valueOf(bankInfo.getExternalBankId()));
            inParams.put(CBonus.PARAM_USERID, form.getUserId());
            inParams.put(CBonus.PARAM_TYPE, form.getType());
            inParams.put(CBonus.PARAM_AMOUNT, form.getAmount());
            inParams.put(CBonus.PARAM_MULTIPLIER, form.getMultiplier());
            inParams.put(CBonus.PARAM_GAMES, form.getGames());
            inParams.put(CBonus.PARAM_GAMEIDS, form.getGameIds());
            inParams.put(CBonus.PARAM_EXPDATE, form.getExpDate());
            inParams.put(CBonus.PARAM_COMMENT, form.getComment());
            inParams.put(CBonus.PARAM_DESCRIPTION, form.getDescription());
            inParams.put(CBonus.PARAM_EXTBONUSID, form.getExtBonusId());
            inParams.put(CBonus.PARAM_HASH, form.getHash());
            inParams.put(CBonus.PARAM_TIMEZONE, form.getTimeZone());
            String autoReleaseParam = form.isAutoRelease();
            inParams.put(CBonus.PARAM_AUTO_RELEASE, autoReleaseParam);
            inParams.put(CBonus.PARAM_START_DATE, form.getStartTime());
            String sMaxWinMultiplier = form.getMaxWinMultiplier();
            inParams.put(CBonus.PARAM_MAX_WIN_MULTIPLIER, sMaxWinMultiplier);

            short subCasinoId = form.getSubCasinoId();

            String extUserId = form.getUserId();
            if (StringUtils.isTrimmedEmpty(extUserId)) {
                throw new BonusException(BonusErrors.USER_NOT_FOUND);
            }
            String type = form.getType();
            if (StringUtils.isTrimmedEmpty(type)) {
                throw new BonusException(BonusErrors.INVALID_BONUS_TYPE);
            }
            String sAmount = form.getAmount();
            if (StringUtils.isTrimmedEmpty(sAmount)) {
                throw new BonusException(BonusErrors.INVALID_OR_EMPTY_AMOUNT);
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

            long amount;
            try {
                amount = Long.parseLong(sAmount);
                if (amount < bankInfo.getBonusThresholdMinKey() || amount > MAX_AMOUNT) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw new BonusException(e, BonusErrors.INVALID_OR_EMPTY_AMOUNT);
            }

            String sMultiplier = form.getMultiplier();
            if (StringUtils.isTrimmedEmpty(sMultiplier)) {
                throw new BonusException(BonusErrors.INVALID_MULTIPLIER);
            }

            double multiplier;
            try {
                multiplier = Double.parseDouble(sMultiplier);
                double necessaryAmount = MAX_AMOUNT / multiplier;

                if (multiplier < 1 || necessaryAmount < amount) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw new BonusException(BonusErrors.INVALID_MULTIPLIER);
            }

            String games = form.getGames();
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
            String gameId = form.getGameIds();

            if (!BonusGameMode.valueOf(games.toUpperCase()).equals(BonusGameMode.ALL) && StringUtils.isTrimmedEmpty(gameId)) {
                throw new BonusException(BonusErrors.INVALID_GAMES_ID);
            }

            Long startTime = parseStartDateTime(form.getStartTime(), null);

            String sExpDate = form.getExpDate();
            if (StringUtils.isTrimmedEmpty(sExpDate)) {
                throw new BonusException(BonusErrors.INVALID_EXP_DATE);
            }
            Long expTime = parseExpDateTime(sExpDate, request.getParameter(CBonus.PARAM_TIMEZONE),
                    BonusErrors.INVALID_EXP_DATE);
            if (startTime != null && expTime != null && expTime <= startTime) {
                LOG.error("Incorrect Dates combination!");
                throw new BonusException(BonusErrors.INVALID_DATES_COMBINATION);
            }

            String comment = form.getComment();
            String description = form.getDescription();

            String extBonusId = form.getExtBonusId();
            if (StringUtils.isTrimmedEmpty(extBonusId)) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }

            String hash = form.getHash();
            if (StringUtils.isTrimmedEmpty(hash)) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }

            Double maxWinMultiplier = parseMaxWinMultiplier(sMaxWinMultiplier);
            if (maxWinMultiplier != null && MAX_AMOUNT / maxWinMultiplier < amount) {
                throw new BonusException(BonusErrors.INVALID_MAX_WIN_MULTIPLIER);
            }
            boolean autoRelease = true;
            if (!StringUtils.isTrimmedEmpty(autoReleaseParam)) {
                if (!autoReleaseParam.equalsIgnoreCase(Boolean.TRUE.toString())
                        && !autoReleaseParam.equalsIgnoreCase(Boolean.FALSE.toString())) {
                    throw new BonusException(BonusErrors.INVALID_AUTO_RELEASE_PARAM);
                }
                autoRelease = Boolean.parseBoolean(autoReleaseParam);
            }

            boolean isHashValueEnabled = bankInfo.isHashValueEnable();
            if (isHashValueEnabled) {
                /*
                 * usedId, bankId, type, amount, multiplier, maxWinMultiplier, games, gameIds, extDate, comment,
                 * description, extBonusId
                 */
                List<String> paramList = new ArrayList<String>();
                paramList.add(extUserId);
                paramList.add(String.valueOf(bankInfo.getExternalBankId()));
                paramList.add(type);
                paramList.add(sAmount);
                paramList.add(sMultiplier);

                if (!StringUtils.isTrimmedEmpty(sMaxWinMultiplier)) paramList.add(sMaxWinMultiplier);

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

            SessionHelper.getInstance().lock(bankId, extUserId);
            try {
                SessionHelper.getInstance().openSession();
                AccountInfo accountInfo = AccountManager.getInstance().getByCompositeKey(subCasinoId, bankInfo, extUserId);
                if (accountInfo == null) {
                    accountInfo = createAccount(bankInfo, extUserId, BonusSystemType.ORDINARY_SYSTEM);
                }

                if (!BonusGameMode.valueOf(games.toUpperCase()).equals(BonusGameMode.ALL)) {
                    String[] gameIds = gameId.trim().split("\\|");
                    for (String gid : gameIds) {
                        LOG.debug(gid);
                    }
                    gameList = BonusManager.getInstance().getInternalListGamesIds(gameIds, bankId);
                    LOG.debug("after internal parse:{}", gameList);
                    boolean incorrectGameIdsValue = false;
                    if (!gameList.isEmpty()) {
                        Set<Long> actionGames = BaseGameInfoTemplateCache.getInstance().getMultiplayerGames();
                        boolean isContainsActionGames = gameList.stream().anyMatch(actionGames::contains);
                        boolean isContainsNonActionGames = gameList.stream().anyMatch(game -> !actionGames.contains(game));
                        incorrectGameIdsValue = isContainsActionGames && isContainsNonActionGames;
                    }
                    ArrayList<Long> allGameIds = new ArrayList<>(BaseGameCache.getInstance().getAllGamesSet(bankId, null));
                    List<Long> validBankGames = removeDisableAndDeniedGames(allGameIds, bankId);
                    Set<Long> forbiddenGamesForBonus = forbiddenGamesForBonusProvider.getGames(accountInfo.getBankId());
                    Collection<Long> realGameList = getValidGameIds(
                            validBankGames,
                            BonusGameMode.valueOf(games.toUpperCase()),
                            gameList);
                    if (realGameList == null || realGameList.isEmpty() ||
                            CollectionUtils.containsAny(realGameList, forbiddenGamesForBonus) || incorrectGameIdsValue) {
                        LOG.debug("invalid game ids: realGameList={}, forbiddenGamesForBonus={}", realGameList, forbiddenGamesForBonus);
                        throw new BonusException(BonusErrors.INVALID_GAMES_ID);
                    }
                }

                Bonus bonus = BonusManager.getInstance().get(bankId, extBonusId);
                if (bonus != null) {
                    long bonusId = bonus.getId();
                    outParams.put(CBonus.BONUSID, bonusId);
                    throw new BonusException(BonusErrors.OPERATION_ALREADY_EXIST);
                }
                try {
                    bonus = BonusManager.getInstance().awardBonus(accountInfo, BonusType.valueOf(type.toUpperCase()),
                            amount, multiplier, extBonusId, gameList, BonusGameMode.valueOf(games.toUpperCase()),
                            description, comment, expTime + 1000 * 60 * 60 * 24 - 1, System.currentTimeMillis(),
                            false, false, autoRelease, startTime, maxWinMultiplier);
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.PARAM_BONUSID, bonus.getId());
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

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private List<Long> removeDisableAndDeniedGames(List<Long> games, long bankId) {
        Currency currency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
        return games.stream().filter(gameId -> isGameEnabled(bankId, gameId, currency))
                .filter(gameId -> !forbiddenGamesForBonusProvider.getGames(bankId).contains(gameId))
                .collect(Collectors.toList());
    }
}
