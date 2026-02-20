package com.dgphoenix.casino.common.promo;

public enum SupportedPlatform implements ISupportedPlatform {
    ALL(null) {
        @Override
        public boolean isPlatformSupported(Long clientTypeId) {
            return true;
        }
    },
    VAULT(8L);

    private final Long clientTypeId;

    SupportedPlatform(Long clientTypeId) {
        this.clientTypeId = clientTypeId;
    }

    @Override
    public boolean isPlatformSupported(Long clientTypeId) {
        return clientTypeId != null && clientTypeId.equals(this.clientTypeId);
    }
}
