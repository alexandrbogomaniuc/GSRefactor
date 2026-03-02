package com.abs.casino.websocket.tournaments;

import com.abs.casino.common.transport.TObject;
import com.abs.casino.promo.tournaments.ErrorCodes;
import com.abs.casino.promo.tournaments.messages.Error;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public interface IMessageHandler<MESSAGE extends TObject> {
    void handle(MESSAGE message, ISocketClient client);

    default long getCurrentTime() {
        return System.currentTimeMillis();
    }

    default Error createErrorMessage(int code, String msg, int rid) {
        return new Error(code, msg, getCurrentTime(), rid);
    }

    default Error createErrorMessage(int code, String msg, int rid, Consumer<? super Error> errorSaver) {
        Error error = createErrorMessage(code, msg, rid);
        errorSaver.accept(error);
        return error;
    }

    default void sendErrorMessage(ISocketClient client, int code, String msg, int rid, Consumer<? super Error> errorSaver) {
        Error errorMessage = createErrorMessage(code, msg, rid, errorSaver);
        getLog().error("error, message={}, client={}", errorMessage, client);
        client.sendMessage(errorMessage);
    }

    default void processUnexpectedError(ISocketClient client, MESSAGE message, Exception e, Consumer<? super Error> errorSaver) {
        Error errorMessage = createErrorMessage(ErrorCodes.INTERNAL_ERROR, "Internal error: " + e.getMessage(),
                message.getRid(), errorSaver);
        getLog().error("Unexpected error, message={}, client={}", errorMessage, client);
        getLog().error("Stacktrace: ", e);
        client.sendMessage(errorMessage);
    }

    Logger getLog();
}
