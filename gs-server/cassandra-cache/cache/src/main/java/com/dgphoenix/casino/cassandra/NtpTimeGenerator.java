package com.abs.casino.cassandra;

import com.dgphoenix.casino.common.util.NtpTimeProvider;

/**
 * Adapter NtpTimeProvider for cassandra driver
 * User: van0ss
 * Date: 09.02.2017
 */
class NtpTimeGenerator implements com.datastax.driver.core.TimestampGenerator {

    private NtpTimeProvider timeProvider;

    public NtpTimeGenerator(NtpTimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public long next() {
        return timeProvider.getTimeMicroseconds();
    }
}
