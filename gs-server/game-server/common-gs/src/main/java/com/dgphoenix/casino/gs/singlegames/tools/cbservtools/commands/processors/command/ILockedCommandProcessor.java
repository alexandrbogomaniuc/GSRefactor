package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command;

import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.ILockedProcessor;

/**
 * Created by vladislav on 2/14/17.
 */
public interface ILockedCommandProcessor extends ILockedProcessor, ICommandRelated {

    @Override
    default boolean canProcessCommand(String command, boolean isNewRoundBet) {
        return getCommand().equals(command);
    }
}
