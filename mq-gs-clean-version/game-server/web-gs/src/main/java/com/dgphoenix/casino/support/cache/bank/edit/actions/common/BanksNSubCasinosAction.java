package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.IDistributedCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.support.cache.bank.edit.forms.common.BanksNSubCasinosForm;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User: vik
 * Date: 17.01.13
 */
public class BanksNSubCasinosAction extends AbstractCRUDAction<BanksNSubCasinosForm> {
    public static final String REMOVE_COMMAND = "remove";
    public static final String REMOVE_SUBCASINO = "Remove SubCasino";
    public static final String REMOVE_BANK = "Remove Bank";

    private int oneTwo;

    @Override
    public void read(BanksNSubCasinosForm form) throws Exception {
        form.setSubCasinoList(fulfillCollection(SubCasinoCache.getInstance()));
        form.setBankIdList(fulfillCollection(BankInfoCache.getInstance()));
    }

    private <C extends IDistributedCache, I extends IDistributedConfigEntry> List<LabelValueBean> fulfillCollection(C cache) {
        Map<Long, I> infoMap = cache.getAllObjects();
        Set<Long> infoKeys = infoMap.keySet();
        List<Long> infoIdList = new ArrayList<>(infoKeys);
        Collections.sort(infoIdList);

        List<LabelValueBean> infoList = new ArrayList<>();
        for (long infoId : infoIdList) {
            I info = infoMap.get(infoId);
            infoList.add(new LabelValueBean(getLabel(info) + "=" + infoId, String.valueOf(infoId)));
        }
        return infoList;
    }

    private <I extends IDistributedConfigEntry> String getLabel(I info) {
        if (info instanceof BankInfo) {
            return ((BankInfo) info).getExternalBankIdDescription();
        }
        if (info instanceof SubCasino) {
            return ((SubCasino) info).getName();
        }
        return null;
    }

    @Override
    public void remove(BanksNSubCasinosForm form) throws Exception {
        if (oneTwo == 1) {
            long subCasinoId = form.getSubCasinoId();
            SubCasinoCache.getInstance().remove(subCasinoId);
            form.setSubCasinoId(0L);
            List<LabelValueBean> subCasinoList = form.getSubCasinoList();
            subCasinoList.remove(lvbToRemove(subCasinoList, subCasinoId));
        }
        if (oneTwo == 2) {
            long bankId = form.getBankId();
            Long subCasinoId = BankInfoCache.getInstance().getSubCasinoId(bankId);
            if (subCasinoId != null) {
                SubCasino subCasino = SubCasinoCache.getInstance().get(subCasinoId);
                if (subCasino != null) {
                    List<Long> bankIds = subCasino.getBankIds();
                    if (bankIds.size() == 1 && bankIds.contains(bankId)) {
                        SubCasinoCache.getInstance().remove(subCasinoId);
                        logger.debug("after from cache =" + SubCasinoCache.getInstance().get(subCasinoId));
                        List<LabelValueBean> subCasinoList = form.getSubCasinoList();
                        subCasinoList.remove(lvbToRemove(subCasinoList, subCasinoId));
                    } else {
                        logger.debug("bankIds.size()={} bankIds.contains(bankId)={}", bankIds.size(), bankIds.contains(bankId));
                        SubCasinoCache.getInstance().remove(subCasinoId, bankId);
                    }
                } else {
                    logger.debug("subCasino is null");
                }
            } else {
                logger.debug("Free bank removed or subCasinoId is null");
            }
            BankInfoCache.getInstance().remove(bankId);
            form.setBankId(0L);
            List<LabelValueBean> bankIdList = form.getBankIdList();
            bankIdList.remove(lvbToRemove(bankIdList, bankId));
        }
    }

    private LabelValueBean lvbToRemove(List<LabelValueBean> lvbList, Long removeId) {
        for (LabelValueBean lvb : lvbList) {
            if (Long.parseLong(lvb.getValue()) == removeId) {
                return lvb;
            }
        }
        return null;
    }

    @Override
    public String getReadCommand(HttpServletRequest request) {
        return REMOVE_COMMAND;
    }

    @Override
    public String getRemoveCommand(HttpServletRequest request) {
        String buttonParam = request.getParameter(BUTTON);
        if (buttonParam.equals(REMOVE_SUBCASINO)) {
            oneTwo = 1;
            return REMOVE_SUBCASINO;
        }
        if (buttonParam.equals(REMOVE_BANK)) {
            oneTwo = 2;
            return REMOVE_BANK;
        }
        return "";
    }
}
