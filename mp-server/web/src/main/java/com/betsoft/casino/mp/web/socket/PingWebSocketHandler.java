//package com.betsoft.casino.mp.web.socket;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
///**
// * User: flsh
// * Date: 08.09.17.
// */
//public class PingWebSocketHandler extends TextWebSocketHandler {
//    private static final Logger LOG = LogManager.getLogger(PingWebSocketHandler.class);
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        LOG.debug("handleTextMessage: session=" + session + ", message=" + message);
//        session.sendMessage(new TextMessage("pong"));
//    }
//}
