package com.abs.casino.common.promo.messages.server.notifications.tournament;

import com.abs.casino.common.promo.messages.server.notifications.PromoNotification;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladislav on 2/16/17.
 */
public class LeaderBoard extends PromoNotification {
    private static final String LEADER_BOARD_NOTIFICATION_PREFIX = "leader_board";

    private final List<Leader> leaders = new ArrayList<Leader>();
    private transient int size = 0;

    public LeaderBoard(long promoId) {
        String id = composeId(LEADER_BOARD_NOTIFICATION_PREFIX, promoId, System.currentTimeMillis());
        super.setId(id);
        super.setPromoId(promoId);
    }

    public void addLeader(String place, String nickname, long score) {
        leaders.add(new Leader(place, nickname, score));
        size++;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String httpFormat() {
        StringBuilder messageBuilder = new StringBuilder(super.httpFormat());

        List<String> leadersAsStrings = new ArrayList<String>(leaders.size());
        for (Leader leader : leaders) {
            leadersAsStrings.add(leader.httpFormat());
        }
        String leadersAsOneString = ServerMessage.ELEMENTS_JOINER.join(leadersAsStrings);
        messageBuilder
                .append(ServerMessage.FIELDS_DELIMITER)
                .append(StringUtils.encodeUriComponent(leadersAsOneString));

        return messageBuilder.toString();
    }

    @Override
    public String toString() {
        return "LeaderBoard{" +
                super.toString() +
                ", leaders=" + leaders +
                "}";
    }

    private static class Leader {
        private String place;
        private String nickname;
        private long score;

        private Leader(String place, String nickname, long score) {
            this.place = place;
            this.nickname = nickname;
            this.score = score;
        }

        private String httpFormat() {
            return ServerMessage.FIELDS_JOINER.join(place, StringUtils.encodeUriComponent(nickname), score);
        }

        @Override
        public String toString() {
            return "Leader{" +
                    "place='" + place +
                    ", nickname='" + nickname +
                    ", score=" + score +
                    '}';
        }
    }
}
