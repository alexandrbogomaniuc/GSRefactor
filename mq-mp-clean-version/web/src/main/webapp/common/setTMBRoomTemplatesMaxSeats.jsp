<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="java.io.IOException" %>
<%@ page import="com.betsoft.casino.mp.model.RoomTemplate" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
    static final GameType gameType = GameType.TRIPLE_MAX_BLAST;
    static final short maxSeats = gameType.getMaxSeats();

    private void updateMaxSeats(Collection<RoomTemplate> roomTemplates, RoomTemplateService roomTemplateService,
                                javax.servlet. http. HttpServletResponse response) throws IOException {

        for (RoomTemplate roomTemplate : roomTemplates) {
            if(roomTemplate.getGameType() == gameType) {
                LOG.debug("setTMBRoomTemplatesMaxSeats.jsp: Start update roomTemplate:{} maxSeats={} to {}", roomTemplate.getId(), roomTemplate.getMaxSeats(), maxSeats);
                response.getWriter().println("Start update roomTemplate:" + roomTemplate.getId() + " maxSeats=" + roomTemplate.getMaxSeats() +" to " + maxSeats);

                roomTemplate.setMaxSeats(maxSeats);

                try {
                    roomTemplateService.put(roomTemplate);
                } catch (Exception exception){
                    LOG.error("setTMBRoomTemplatesMaxSeats.jsp: error to update maxSeats={} for the RoomTemplate={}", maxSeats, roomTemplate, exception);
                    response.getWriter().println("error to update maxSeats=" + maxSeats + " for the RoomTemplate=" + roomTemplate + " exception: " + exception.getMessage());
                }

                LOG.debug("setTMBRoomTemplatesMaxSeats.jsp: Finish update roomTemplate:{} maxSeats={} to {}", roomTemplate.getId(), roomTemplate.getMaxSeats(), maxSeats);
                response.getWriter().println("Finish update roomTemplate:" + roomTemplate.getId() + " maxSeats=" + roomTemplate.getMaxSeats() +" to " + maxSeats);
            }
        }
    }
%>

<%

    LOG.debug("setTMBRoomTemplatesMaxSeats.jsp: query={}", request.getQueryString());

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();

    RoomTemplateService roomTemplateService = appContext.getBean(RoomTemplateService.class);

    Collection<RoomTemplate>  roomTemplates = roomTemplateService.getAll();

    if (roomTemplates != null && !roomTemplates.isEmpty()) {
        roomTemplates = roomTemplates.stream()
                .sorted(Comparator.comparingLong(RoomTemplate::getId))
                .collect(Collectors.toList());
    }

    if (roomTemplates == null) {
        response.getWriter().println("roomTemplates is null");
    } else {
        updateMaxSeats(roomTemplates, roomTemplateService, response);
    }
%>