<%@ page import="com.dgphoenix.casino.common.util.IdGenerator" %>
<%@ page import="com.dgphoenix.casino.common.util.ISequencer" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    final ISequencer sequencer = IdGenerator.getInstance().getSequencer("com.dgphoenix.casino.gs.biz.DBWalletOperation");
    if (sequencer != null) {
        //1152921504606846976L in binary is 001000000000000000000000000000000000000000000000000000000000000, pm-real startValue
        //sequencer.setValue(1152921504606846976L); //see AccountIdGeneratorTest.testTmp
        //2305843009213693952L in binary is 010000000000000000000000000000000000000000000000000000000000000, pm1 startValue
        sequencer.setValue(2305843009213693952L); //see AccountIdGeneratorTest.testTmp
    } else {
        response.getWriter().println("sequencer is null. ");
    }
    response.getWriter().println("OK\n <br/>");
    response.getWriter().flush();
%>
