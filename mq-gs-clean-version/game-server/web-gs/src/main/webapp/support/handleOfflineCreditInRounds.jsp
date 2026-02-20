<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraCommonGameWalletPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.session.GameSession" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.common.exception.WalletException" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.LasthandPersister" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.LasthandInfo" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister" %>
<%@ page import="static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.GameMode" %>
<%@ page import="com.dgphoenix.casino.common.util.IdGenerator" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.IWallet" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.WalletOperationType" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.common.web.statistics.StatisticsManager" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.gs.singlegames.tools.util.LasthandHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String bankIdParam = request.getParameter("bankId");
    long bankId = Long.parseLong(bankIdParam);

    String extUserId = request.getParameter("extUserId");

    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraCommonGameWalletPersister walletPersister = persistenceManager.getPersister(CassandraCommonGameWalletPersister.class);
    CassandraGameSessionPersister gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);

    List<String> extIds = new ArrayList<>();
    if (!isTrimmedEmpty(extUserId)) {
        extIds = Arrays.asList(extUserId);
    } else {
        //extIds = Arrays.asList("15154", "497705", "278564", "230534", "568812", "301018", "445752", "327344", "616035", "311720", "550827", "585925", "195532", "308308", "299054", "308308", "481104", "359403", "486559", "132351", "261031", "452431", "275015", "327828", "264397", "37286", "550628", "65819", "239678", "630822", "563776", "463939", "236158", "112754", "613627", "255162", "519368", "311603", "378656", "314533", "342184", "113003", "267447", "330453", "370458", "586137", "629996", "630020", "299652", "182651", "262913", "511686", "523087", "511681", "511681", "424108", "291345", "424027", "450448", "379668", "379668", "379668", "136982", "330308", "153705", "152367", "337316", "492937", "413285", "413285", "413285", "625108", "331376", "491558", "282456", "549477", "613108", "611524", "621668", "567946", "350253", "313673", "329918", "168169", "278564", "291886", "546157", "29277", "14912", "441187", "288534", "629136", "397336", "628369", "337476", "482092", "193774", "404671", "519842", "584821", "631763", "236791", "378600", "591387", "355329", "507113", "615261", "615261", "459728", "589543", "501043", "360666", "222246", "139511", "41076", "442218", "298399", "631471", "624482", "574872", "311744", "581632", "314906", "311653", "577697", "345461", "628302", "289188", "631538", "289188", "626144", "374476", "468794", "482095", "614276", "631471", "624382", "631121", "631455", "482096", "478912", "444624", "497609", "203268", "628047", "626016", "491260", "458455", "631139", "422926", "631121", "550569", "36679", "94012", "299186", "563399", "493183", "631307", "145976", "330335", "186055", "124634", "62485", "141781", "571264", "498429", "401012", "630784", "440420", "428635", "562546", "517292", "441738", "623294", "604939", "428836", "480036", "491558", "552869", "418569", "626550", "234346", "587238", "17978", "23751", "630784", "584818", "624748", "277433", "591878", "342189", "374476", "577556", "310211", "411576", "577556", "590050", "41664", "525569", "438717", "430681", "452525", "491041", "449135", "422926", "174414", "174414", "383323", "407984", "197170", "588884");
    }

    for (String extId : extIds) {
        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(bankInfo.getSubCasinoId(), bankId, extId);
        if (accountInfo == null) {
            response.getWriter().println("Cannot find account extId = " + extId);
            response.getWriter().println("<br>");
            continue;
        }
        //BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
        SessionHelper.getInstance().lock(accountInfo.getId());
        try {
            SessionHelper.getInstance().openSession();
            ITransactionData data = SessionHelper.getInstance().getTransactionData();
            //CommonWallet wallet = (CommonWallet) walletPersister.getWallet(accountInfo.getId());
            CommonWallet wallet = (CommonWallet) data.getWallet();
            if (wallet == null) {
                wallet = (CommonWallet) walletPersister.getWallet(accountInfo.getId());
                data.setWallet(wallet);
            }
            if (wallet == null) {
                response.getWriter().println("Wallet is null");
                response.getWriter().println("<br>");
            } else {
                response.getWriter().println(wallet);
                response.getWriter().println("<br>");
                response.getWriter().println("<br>");
                Map<Integer, Long> unfinishedRounds = wallet.getUnfinishedGames();
                for (Integer gameId : unfinishedRounds.keySet()) {
                    response.getWriter().println("Finishing roundId = " + unfinishedRounds.get(gameId) + ", gameId = " + gameId);
                    response.getWriter().println("<br>");
                    CommonWalletOperation operation = wallet.getCurrentWalletOperation(gameId);
                    if (operation != null) {
                        response.getWriter().println("previous operation is not completed, gameId = " + gameId + ", accountId = " + extId);
                        continue;
                    }
                    if (data.getGameSession() != null && data.getGameSession().getGameId() == gameId) {
                        response.getWriter().println("Game is online, skipping, roundId = " + unfinishedRounds.get(gameId) + ", gameId = " + gameId);
                        response.getWriter().println("<br>");
                    } else {
                        LasthandInfo lasthandInfo = LasthandPersister.getInstance().get(accountInfo.getId(), gameId);
                        if (!isTrimmedEmpty(lasthandInfo.getLasthandData()) && lasthandInfo.getLasthandData().contains("DUPP")) {
                            GameSession gameSession = gameSessionPersister.get(wallet.getGameWalletGameSessionId(gameId));
                            if (gameSession == null) {
                                response.getWriter().println("GameSession is null, gameId = " + gameId);
                                response.getWriter().println("<br>");
                                continue;
                            }
                            handleOfflineCredit(wallet.getGameWalletWinAmount(gameId), bankInfo, true, gameId, accountInfo, gameSession, wallet);
                        }
                    }
                }
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Throwable t) {
            response.getWriter().println("Error on process accountId=" + extId);
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }
%>

<%!
    // Partially copied from WinActionProcessor.
    // No need to update PlayerBet, GameSession. It is done as usual, just CW transaction need to be sent.
    public void handleOfflineCredit(long winAmount, BankInfo bankInfo, boolean isRoundFinished, Integer gameId,
                                    AccountInfo accountInfo, GameSession gameSession, CommonWallet cWallet)
            throws CommonException {
        WalletProtocolFactory.getInstance().interceptCreateWallet(accountInfo, bankInfo.getId(), gameSession.getId(),
                gameId, GameMode.REAL, gameSession.getClientType());
        CommonWalletOperation operation = cWallet.getCurrentWalletOperation(gameId);
        if (operation != null) {
            throw new WalletException("previous operation is not completed");
        }

        cWallet.setGameWalletRoundFinished(gameId, isRoundFinished);

        Long roundId = cWallet.getGameWalletRoundId(gameId);
        if (roundId == null) {
            roundId = IdGenerator.getInstance().getNext(IWallet.class);
            cWallet.setGameWalletRoundId(gameId, roundId);
        }
        operation = createCommonWalletOperation(accountInfo.getId(), gameSession.getId(), roundId, winAmount,
                WalletOperationType.CREDIT, null, cWallet, gameId,
                gameSession.getExternalSessionId());
        IWalletProtocolManager protocolManager = WalletProtocolFactory.getInstance().getWalletProtocolManager(
                accountInfo.getBankId());
        protocolManager.credit(accountInfo, (long) gameId, winAmount, isRoundFinished, cWallet, operation, gameId != 209, roundId);
        protocolManager.completeOperation(accountInfo, gameId, WalletOperationStatus.COMPLETED, cWallet.getGameWallet(gameId),
                operation, null);
        if (isRoundFinished) {
            cWallet.updateGameWallet(gameId, 0L, 0L, null);
        } else {
            cWallet.updateGameWallet(gameId, 0L, 0L);
        }
    }

    protected CommonWalletOperation createCommonWalletOperation(long accountId, long gameSessionId,
                                                                long roundId, long amount,
                                                                WalletOperationType type, String description,
                                                                CommonWallet cWallet, long gameId,
                                                                String externalSessionId)
            throws WalletException {
        long now = System.currentTimeMillis();
        long id = IdGenerator.getInstance().getNext(CommonWalletOperation.class);
        CommonWalletOperation commonWalletOperation = cWallet.createCommonWalletOperation(id, accountId, gameSessionId,
                roundId, amount, type, description, WalletOperationStatus.STARTED,
                WalletOperationStatus.STARTED, (int) gameId, 0, externalSessionId);
        StatisticsManager.getInstance().updateRequestStatistics("ChangeBalanceActionProcessor: " +
                "createCommonWalletOperation", System.currentTimeMillis() - now);
        return commonWalletOperation;
    }

    public void removeDuppFromLastHand(LasthandInfo lasthandInfo) throws CommonException {
        Map<String, String> publicLastHand = new HashMap<>();
        Map<String, String> privateLastHand = new HashMap<>();
        String lasthandData = lasthandInfo.getLasthandData();
        if (lasthandData != null && !lasthandData.isEmpty()) {
            List<Map<String, String>> data = LasthandHelper.unpack(lasthandData);
            if (data != null && data.get(0) != null) {
                publicLastHand.putAll(data.get(0));
                if (data.get(1) != null) {
                    privateLastHand.putAll(data.get(1));
                }
                privateLastHand.remove("DUPP");
                publicLastHand.remove("DUPP");
                privateLastHand.remove("GAMBLE");
                publicLastHand.remove("GAMBLE");
                String lasthandDataNew = LasthandHelper.pack(publicLastHand, privateLastHand,
                        null, null);
                lasthandInfo.setLasthandData(lasthandDataNew);
            }
        }
    }
%>