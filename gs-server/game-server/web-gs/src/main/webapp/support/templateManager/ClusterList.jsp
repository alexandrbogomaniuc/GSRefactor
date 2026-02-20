<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%
    Map<String, String> localClusterMap = new HashMap<>();
    Map<String, String> copyClusterMap = new HashMap<>();
    Map<String, String> liveClusterMap = new HashMap<>();

    localClusterMap.put("SB LOCAL", "http://gs1.sb.dgphoenix.com");
    localClusterMap.put("GP3 LOCAL", "http://gs1-gp3.dgphoenix.com:8080");
    localClusterMap.put("STRESS LOCAL", "http://gs1-stress.dgphoenix.com");
    localClusterMap.put("STRESS LOCAL 2", "http://gs1-stc2.dgphoenix.com");

    copyClusterMap.put("GP3 COPY", "https://gs1-gp3.discreetgaming.com");
    copyClusterMap.put("SB COPY", "https://gs1-sb.discreetgaming.com");
    copyClusterMap.put("GP3 BETA", "https://gs1-beta.discreetgaming.com");
    copyClusterMap.put("AAMS COPY", "https://gs1-aams.discreetgaming.com");
    copyClusterMap.put("NG COPY", "https://gs1-ng-copy.nucleusgaming.com:1443");
    localClusterMap.put("STRESS COPY", "http://gs1-stc.dgphoenix.com");

    liveClusterMap.put("SB LIVE", "http://gs1-sb.betsoftgaming.com");
    liveClusterMap.put("GP3 LIVE", "http://gs1-gp3.betsoftgaming.com");
    liveClusterMap.put("C2SS LIVE", "http://gs1-c2ss.betsoftgaming.com");
    liveClusterMap.put("188BET LIVE", "http://gs1.188bet.betsoftgaming.com");
    liveClusterMap.put("C2188BET LIVE", "https://gs1-c2188bet.betsoftgaming.com:1443");
    liveClusterMap.put("GSN LIVE", "https://gs1.gsn.betsoftgaming.com");
    liveClusterMap.put("LGA LIVE", "https://gs1-lga.betsoftgaming.com");
    liveClusterMap.put("C2LGA LIVE", "https://gs2-c2lga.betsoftgaming.com:1443");
    liveClusterMap.put("C2LGA2 LIVE", "https://gs1-c2lga2.betsoftgaming.com:1443/");
    liveClusterMap.put("AAMS LIVE", "https://gs1-aams.betsoftgaming.com:1443");
    liveClusterMap.put("DEMO CLUSTER LIVE", "http://gs1-democluster.betsoftgaming.com");
    liveClusterMap.put("LAPTOP LIVE", "http://gs1-laptop.betsoftgaming.com");
    liveClusterMap.put("NG LIVE", "https://gs1-ng.nucleusgaming.com:1443");

    liveClusterMap.put("MQ LIVE", "http://gs1-gp3.maxquest.com");
%>


