<%@ page import="com.dgphoenix.casino.common.util.IdGenerator" %>
<%@ page import="com.abs.casino.common.util.ISequencer" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    final String[] sequencerKeys = {
            "com.abs.casino.gs.biz.DBWalletOperation",
            "com.dgphoenix.casino.gs.biz.DBWalletOperation"
    };
    ISequencer sequencer = null;
    String activeSequencerKey = null;
    for (String sequencerKey : sequencerKeys) {
        sequencer = IdGenerator.getInstance().getSequencer(sequencerKey);
        if (sequencer != null) {
            activeSequencerKey = sequencerKey;
            break;
        }
    }
    if (sequencer != null) {
        //1152921504606846976L in binary is 001000000000000000000000000000000000000000000000000000000000000, pm-real startValue
        //sequencer.setValue(1152921504606846976L); //see AccountIdGeneratorTest.testTmp
        //2305843009213693952L in binary is 010000000000000000000000000000000000000000000000000000000000000, pm1 startValue
        sequencer.setValue(2305843009213693952L); //see AccountIdGeneratorTest.testTmp
        response.getWriter().println("sequencer key = " + activeSequencerKey + "<br/>");
    } else {
        response.getWriter().println("sequencer is null for keys: com.abs... and com.dgphoenix... ");
    }
    response.getWriter().println("OK\n <br/>");
    response.getWriter().flush();
%>
