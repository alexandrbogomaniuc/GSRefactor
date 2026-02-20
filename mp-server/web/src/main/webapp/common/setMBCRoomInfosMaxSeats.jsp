<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="com.betsoft.casino.mp.model.AbstractRoomInfo" %>
<%@ page import="java.io.IOException" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
    static final GameType gameType = GameType.BG_MAXCRASHGAME;
    static final short maxSeats = gameType.getMaxSeats();

    private void updateMaxSeats(Collection<IRoomInfo> roomInfos, AbstractRoomInfoService roomInfoService,
                                javax.servlet. http. HttpServletResponse response) throws IOException {

        for (IRoomInfo roomInfo : roomInfos) {
            if(roomInfo.getGameType() == gameType && roomInfo instanceof AbstractRoomInfo) {
                LOG.debug("setMBCRoomInfosMaxSeats.jsp: Start update roomInfo:{} maxSeats={} to {}", roomInfo.getId(), roomInfo.getMaxSeats(), maxSeats);
                response.getWriter().println("Start update roomInfo:" + roomInfo.getId() + " maxSeats=" + roomInfo.getMaxSeats() +" to " + maxSeats);

                ((AbstractRoomInfo)roomInfo).setMaxSeats(maxSeats);

                try {
                    roomInfoService.lock(roomInfo.getId());
                    roomInfoService.update(roomInfo);
                } catch (Exception exception){
                    LOG.error("setMBCRoomInfosMaxSeats.jsp: error to update maxSeats={} for the RoomInfo={}", maxSeats, roomInfo, exception);
                    response.getWriter().println("error to update maxSeats=" + maxSeats + " for the RoomInfo=" + roomInfo + " exception: " + exception.getMessage());
                } finally {
                    roomInfoService.unlock(roomInfo.getId());
                }

                LOG.debug("setMBCRoomInfosMaxSeats.jsp: Finish update roomInfo:{} maxSeats={} to {}", roomInfo.getId(), roomInfo.getMaxSeats(), maxSeats);
                response.getWriter().println("Finish update roomInfo:" + roomInfo.getId() + " maxSeats=" + roomInfo.getMaxSeats() +" to " + maxSeats);
            }
        }
    }
%>

<%

    LOG.debug("setMBCRoomInfosMaxSeats.jsp: query={}", request.getQueryString());

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();

    AbstractRoomInfoService multiNodeRoomInfoService = appContext.getBean(MultiNodeRoomInfoService.class);
    AbstractRoomInfoService multiNodePrivateRoomInfoService =  appContext.getBean(MultiNodePrivateRoomInfoService.class);

    Collection<IRoomInfo>  multiNodeRoomInfos = multiNodeRoomInfoService.getAllRooms();
    Collection<IRoomInfo>  multiNodePrivateRoomInfos = multiNodePrivateRoomInfoService.getAllRooms();

    if (multiNodeRoomInfos != null && !multiNodeRoomInfos.isEmpty()) {
        multiNodeRoomInfos = multiNodeRoomInfos.stream()
                .sorted(Comparator.comparingLong(IRoomInfo::getId))
                .collect(Collectors.toList());
    }

    if (multiNodePrivateRoomInfos != null && !multiNodePrivateRoomInfos.isEmpty()) {
        multiNodePrivateRoomInfos = multiNodePrivateRoomInfos.stream()
                .sorted(Comparator.comparingLong(IRoomInfo::getId))
                .collect(Collectors.toList());
    }

    if (multiNodeRoomInfos == null) {
        response.getWriter().println("multiNodeRoomInfos is null");
    } else {
        updateMaxSeats(multiNodeRoomInfos, multiNodeRoomInfoService, response);
    }

    if (multiNodePrivateRoomInfos == null) {
        response.getWriter().println("multiNodePrivateRoomInfos is null");
    } else {
        updateMaxSeats(multiNodePrivateRoomInfos, multiNodePrivateRoomInfoService, response);
    }
%>