package com.abs.casino.common.promo.messages.server.responses;

import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import org.apache.commons.lang.StringUtils;

/**
 * Created by vladislav on 12/19/16.
 */
public class NotificationsShownResponse extends ServerResponse {
    @Override
    public String httpFormat() {
        return StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        return "NotificationsShownResponse{" + super.toString() + '}';
    }
}
