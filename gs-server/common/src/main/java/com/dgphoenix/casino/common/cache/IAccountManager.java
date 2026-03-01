package com.abs.casino.common.cache;

import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;

public interface IAccountManager {
    AccountInfo getByAccountId(long accountId) throws CommonException;

    Pair<Integer, String> getBankIdExternalIdByAccountId(long accountId) throws CommonException;
}
