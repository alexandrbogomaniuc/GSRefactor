package com.abs.casino.common.geoip;

public enum RestrictionType {
    PROMO {
        @Override
        public int getCassandraTtl() {
            return 15552000; // 180 days
        }
    };

    public abstract int getCassandraTtl();
}
