package com.dgphoenix.casino.services;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.ICommonWalletClient;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final BankInfoCache bankInfoCache;
    private final WalletProtocolFactory walletProtocolFactory;

    public LoginService(BankInfoCache bankInfoCache, WalletProtocolFactory walletProtocolFactory) {
        this.bankInfoCache = bankInfoCache;
        this.walletProtocolFactory = walletProtocolFactory;
    }

    public CommonWalletAuthResult getUserCWInfo(String token, Long bankId, ClientType clientType) throws CommonException {
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        if (bankInfo == null) {
            throw new CommonException("Incorrect bankId=" + bankId);
        }
        IWalletProtocolManager wallet = walletProtocolFactory.getWalletProtocolManager(bankInfo.getId());
        ICommonWalletClient client = (ICommonWalletClient) wallet.getClient();
        return client.auth(token, clientType);
    }
}
