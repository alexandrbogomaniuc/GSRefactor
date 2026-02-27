package com.abs.casino.gs.singlegames.tools.cbservtools.commands.responses;

import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

import java.util.ArrayList;
import java.util.List;

public class CompositeResponse extends ServerResponse {
    private List<ServerResponse> responses = new ArrayList<>();

    public void add(ServerResponse response) {
        responses.add(response);
    }

    @Override
    public String httpFormat() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < responses.size(); ++i) {
            sb.append(responses.get(i).httpFormat());
            if (i < responses.size() - 1) {
                sb.append(PARAMS_DELIMITER);
            }
        }
        return sb.toString();
    }

    public boolean isEmpty() {
        return responses.isEmpty();
    }
}
