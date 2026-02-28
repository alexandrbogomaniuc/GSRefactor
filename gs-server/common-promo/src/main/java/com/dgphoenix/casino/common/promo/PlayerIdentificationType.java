package com.dgphoenix.casino.common.promo;

import com.abs.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;

/**
 * User: flsh
 * Date: 14.04.17.
 */
public enum PlayerIdentificationType {
    EXTERNAL_ID(false) {
        @Override
        public String getName(IAccountInfo accountInfo) {
            return accountInfo.getExternalId();
        }
    },
    FULL_NAME(false) {
        @Override
        public String getName(IAccountInfo accountInfo) {
            if(StringUtils.isTrimmedEmpty(accountInfo.getFirstName()) ||
                    StringUtils.isTrimmedEmpty(accountInfo.getLastName())) {
                return accountInfo.getExternalId();
            }
            return accountInfo.getFirstName().trim() + " " + accountInfo.getLastName().trim();
        }

    },
    EMAIL_NAME(false) {
        @Override
        public String getName(IAccountInfo accountInfo) {
            String email = accountInfo.getEmail();
            return StringUtils.isTrimmedEmpty(email) ? accountInfo.getExternalId() : email;
        }

    },
    NICK_NAME_OR_FULL_OR_EMAIL(false) {
        @Override
        public String getName(IAccountInfo accountInfo) {
            String nickName = accountInfo.getNickName();
            if(!StringUtils.isTrimmedEmpty(nickName)) {
                return nickName;
            }
            if(!StringUtils.isTrimmedEmpty(accountInfo.getFirstName())) {
                return accountInfo.getFirstName().trim();
            }
            String email = accountInfo.getEmail();
            return StringUtils.isTrimmedEmpty(email) ? accountInfo.getExternalId() : email;
        }

    },
    NICK_NAME(false) {
        @Override
        public String getName(IAccountInfo accountInfo) {
            String nickName = accountInfo.getNickName();
            return StringUtils.isTrimmedEmpty(nickName) ? accountInfo.getExternalId() : nickName;
        }

    },
    MQ_NICK_NAME(false) {
        @Override
        public String getName(IAccountInfo accountInfo) {
            String nickName = accountInfo.getNickName();
            return StringUtils.isTrimmedEmpty(nickName) ? accountInfo.getExternalId() : nickName;
        }
    },
    INTEGER_ID_GENERATOR(true) {
        @Override
        public String getName(IAccountInfo accountInfo) {
            throw new UnsupportedOperationException("Must be implemented externally");
        }
    };

    public abstract String getName(IAccountInfo accountInfo);
    private final boolean uniqueForEachPromo;

    public boolean isUniqueForEachPromo() {
        return uniqueForEachPromo;
    }

    PlayerIdentificationType(boolean uniqueForEachPromo) {
        this.uniqueForEachPromo = uniqueForEachPromo;
    }
}
