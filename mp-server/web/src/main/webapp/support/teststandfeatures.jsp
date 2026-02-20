<%@ page import="com.betsoft.casino.teststand.TestStandLocal" %>
<%@ page import="com.betsoft.casino.teststand.TestStandFeature" %>
<%

    String action = request.getParameter("action");
    String sid = request.getParameter("sid");

    if (action != null) {
        TestStandLocal testStandLocal = TestStandLocal.getInstance();
        if (action.equals("add") && request.getParameter("featureId") != null) {
            String featureId = request.getParameter("featureId");
            TestStandFeature featureById = testStandLocal.getPossibleFeatureById(Integer.parseInt(featureId)).copy();
            testStandLocal.addFeature(sid, featureById);
        } else if (action.equals("remove")) {
            testStandLocal.removeFeatureBySid(sid);
        } else if (action.equals("addBoss")) {
            Long roomId = Long.parseLong(request.getParameter("roomId"));
            Integer bossId = Integer.valueOf(request.getParameter("bossId"));
            testStandLocal.addBossForRoom(roomId, bossId);
        } else if (action.equals("removeBoss")) {
            Long roomId = Long.parseLong(request.getParameter("roomId"));
            testStandLocal.removeBossForRoom(roomId);
        } else if (action.equals("addMinEnemiesModeForRoom")) {
            Long roomId = Long.parseLong(request.getParameter("roomId"));
            testStandLocal.addMinimalEnemiesModeForRoom(roomId);
        } else if (action.equals("addEnemyId")) {
            Long roomId = Long.parseLong(request.getParameter("roomId"));
            Integer enemyId = Integer.valueOf(request.getParameter("enemyId"));
            testStandLocal.addEnemyIdForRoom(roomId, enemyId);
        } else if (action.equals("removeEnemy")) {
            Long roomId = Long.parseLong(request.getParameter("roomId"));
            testStandLocal.removeEnemyIdForRoom(roomId);
        }

    }
%>