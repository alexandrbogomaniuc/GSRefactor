package com.dgphoenix.casino.promo.events.process;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.IPrizeWonHelper;
import com.dgphoenix.casino.common.promo.IPromoTemplate;
import com.dgphoenix.casino.gs.managers.dblink.IGameDBLink;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 6/9/22
 */
public class PrizeWonBalanceChanger implements IPrizeWonHelper {
    private final IGameDBLink dbLink;
    private final AccountInfo account;
    private final IPromoTemplate<?,?> template;
    private final boolean walletMode;

    public PrizeWonBalanceChanger(IGameDBLink dbLink, AccountInfo account, IPromoTemplate<?,?> template) {
        this.dbLink = dbLink;
        this.account = account;
        this.template = template;
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(account.getBankId());
        this.walletMode = bankInfo.getWPMClass() != null;
    }

    @Override
    public void changePlayerBalance(long amount) {
        if (walletMode) {
            dbLink.setWinAmount(dbLink.getWinAmount() + amount);
        } else {
            try {
                account.incrementBalance(amount, true);
            } catch (CommonException e) {
                //nop, used silent, exception is impossible
            }
        }
    }
}
