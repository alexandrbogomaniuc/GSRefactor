package com.abs.casino.common.promo.messages.server.responses;

import com.abs.casino.common.promo.PromoType;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vladislav on 12/6/16.
 */
public class PromoEnterResponse extends ServerResponse {
    private static final String ACTIVE_PROMOS = "ACTIVE_PROMOS";
    private static final String NEW_ACTIVE_PROMOS = "ACTIVE_PROMOS2";

    private final Set<ActivePromo> activePromos = new HashSet<ActivePromo>();
    private final transient Set<Long> activePromosIds = new HashSet<Long>();

    public void addActivePromo(long promoId, String title, PromoType type, long endTimeUTC) {
        ActivePromo activePromo = new ActivePromo(promoId, title, type, endTimeUTC);
        activePromos.add(activePromo);
        activePromosIds.add(activePromo.promoId);
    }

    public Set<ActivePromo> getActivePromos() {
        return activePromos;
    }

    public Set<Long> getActivePromosIds() {
        return activePromosIds;
    }

    @Override
    public String httpFormat() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(ACTIVE_PROMOS).append(ServerMessage.VALUE_DELIMITER);

        List<String> activePromosAsStrings = new ArrayList<String>(activePromos.size());
        for (ActivePromo activePromo : activePromos) {
            activePromosAsStrings.add(activePromo.oldHttpFormat());
        }
        String activePromosAsOneString = ServerMessage.ELEMENTS_JOINER.join(activePromosAsStrings);
        responseBuilder.append(StringUtils.encodeUriComponent(activePromosAsOneString));
        responseBuilder.append(ServerMessage.PARAMS_DELIMITER);


        //new format
        responseBuilder.append(NEW_ACTIVE_PROMOS).append(ServerMessage.VALUE_DELIMITER);

        activePromosAsStrings = new ArrayList<String>(activePromos.size());
        for (ActivePromo activePromo : activePromos) {
            activePromosAsStrings.add(activePromo.httpFormat());
        }
        activePromosAsOneString = ServerMessage.ELEMENTS_JOINER.join(activePromosAsStrings);
        responseBuilder.append(StringUtils.encodeUriComponent(activePromosAsOneString));

        return responseBuilder.toString();
    }

    @Override
    public String toString() {
        return "PromoEnterResponse{" +
                super.toString() +
                ", activePromos=" + activePromos +
                '}';
    }

    private static class ActivePromo {
        private long promoId;
        private String title;
        private String type;
        private long endTime;

        public ActivePromo(long promoId, String title, PromoType type, long endTimeUTC) {
            this.promoId = promoId;
            this.title = title;
            this.type = type.getStringRepresentation();
            this.endTime = endTimeUTC;
        }

        public String oldHttpFormat() {
            return ServerMessage.FIELDS_JOINER.join(promoId, title);
        }

        public String httpFormat() {
            return ServerMessage.FIELDS_JOINER.join(promoId, title, type, endTime);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ActivePromo that = (ActivePromo) o;

            return promoId == that.promoId;
        }

        @Override
        public int hashCode() {
            return (int) (promoId ^ (promoId >>> 32));
        }

        @Override
        public String toString() {
            return "ActivePromo{" +
                    "promoId=" + promoId +
                    ", title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    ", endTime=" + endTime +
                    '}';
        }
    }
}
