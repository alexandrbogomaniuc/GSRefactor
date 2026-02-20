<%@ page import="com.dgphoenix.casino.common.cache.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.GameGroup" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%!
    long[] array_fullSuite = {
            224, 617, 616, 618, 64, 637, 639, 638, 640, 229, 543, 542, 544, 619, 621, 620, 622, 351, 392, 391, 393, 295, 307, 306, 364, 553, 605, 604, 606, 195,
            272, 271, 366, 5, 226, 659, 658, 660, 300, 315, 312, 361, 266, 608, 607, 609, 299, 314, 311, 360, 298, 313, 310, 359, 190, 13, 194, 628,
            627, 629, 654, 656, 655, 657, 256, 602, 601, 603, 763, 765, 764, 766, 145, 137, 323, 325, 324, 369, 12, 446, 445, 447, 691, 672, 669,
            675, 690, 671, 668, 674, 689, 670, 667, 673, 209, 10, 147, 29, 540, 539, 541, 139, 144, 500, 502, 501, 503, 796, 74, 350, 532, 531, 533, 191, 79, 276,
            275, 371, 548, 550, 549, 551, 692, 694, 693, 695, 755, 757, 756, 758, 23, 647, 649, 648, 650, 512, 514, 513, 515, 177, 292, 291, 356, 759, 761, 760, 762,
            178, 407, 406, 408, 173, 611, 610, 612, 222, 426, 430, 428, 432, 425, 429, 427, 431, 700, 702, 701, 703, 341, 343, 342, 353, 478, 480, 479, 481, 180, 469, 468,
            470, 221, 645, 644, 646, 504, 506, 505, 507, 31, 30, 767, 769, 768, 770, 727, 729, 728, 730, 775, 777, 776, 778, 248, 423, 422, 424, 2, 268, 263,
            355, 159, 247, 413, 412, 414, 751, 753, 752, 754, 238, 585, 584, 586, 534, 536, 535, 537, 554, 588, 587, 589, 444, 452, 451, 453, 210, 270, 269, 362, 99,
            101, 100, 97, 102, 103, 96, 98, 243, 203, 793, 193, 525, 524, 526, 225, 652, 651, 653, 9, 520, 522, 521, 523, 82, 249, 81, 482, 484, 483, 485, 68,
            70, 69, 66, 71, 72, 65, 67, 94, 792, 43, 220, 277, 319, 318, 368, 223, 280, 304, 303, 373, 143, 719, 721, 720, 722, 63, 449, 448, 450, 704, 706,
            705, 707, 344, 346, 345, 376, 258, 546, 545, 547, 142, 771, 773, 772, 774, 402, 784, 786, 785, 787, 495, 494, 496, 44, 288, 466, 465, 467, 146, 747, 749,
            748, 750, 461, 463, 462, 464, 262, 642, 641, 643, 794, 597, 599, 598, 600, 384, 386, 385, 387, 244, 788, 790, 789, 791, 48, 158, 382, 381, 383,
            105, 236, 397, 396, 398, 471, 473, 472, 474, 308, 321, 320, 365, 259, 456, 455, 457, 228, 624, 623, 625, 198, 197, 199, 590, 592, 591, 593, 490, 278
    };


    ArrayList<Long> fullSuiteGames = new ArrayList<Long>();
%>

<%
    for (long gameId : array_fullSuite)
        fullSuiteGames.add(gameId);
%>

<SCRIPT>

    function isDigit(event, input, allow_point) {
        var key, ctrl;

        if (window.event) {
            key = window.event.keyCode;
            ctrl = window.event.ctrlKey;
        } else {
            key = event.which;
            ctrl = event.ctrlKey;
        }

        if ((key == 46) || (key == 8) || (key == 9)) return true; // delete, backspace, TAB
        if ((key >= 48) && (key <= 57)) return true;    // 0..9
        if (((key == 86) || (key == 67) || (key == 88)) && ctrl) return true; // X C V
        if (key == 190 && allow_point) {
            var points = input.value.split('.');
            if (points.length <= 1 && points[0] != '') return true;
        }


        return false;
    }


    function submitButton(btn) {
        var $result = document.getElementById('id_subcasino').value;

        if ($result == "") {
            alert("ОШИБКА! Необходимо ввести ID субказино");
            return false;
        }

        setNewPropertyIDS();
        return true;
    }

    <%
        String strBanks = "";
        String serverName = request.getServerName();
    %>
    function reloadPage(source_bank, dest_bank) {
        if (source_bank == null) return;
        if (dest_bank == null) return;

        location.href = "/support/gameBankConfig/compareBanksGames.jsp?source_bank=" + source_bank + "&dest_bank=" + dest_bank;
    }
</SCRIPT>


<HTML>
<HEAD>
    <TITLE> Compare Games in Banks</TITLE>
</HEAD>
<BODY>


<%
    class Game {
        PrintWriter writer = null;
        String title = "";
        String name = "";
        String descr = "";
        Long id = -1L;

        public Game(PrintWriter writer, String title, String name, String descr, Long id) {
            this.writer = writer;
            this.title = title;
            this.name = name;
            this.descr = descr;
            this.id = id;
        }
    }

    class CustomerComparator implements Comparator<Game> {
        public int compare(Game a, Game b) {
            return a.title.compareTo(b.title);
        }
    }


    String strSourceBank = request.getParameter("source_bank");
    String strDestBank = request.getParameter("dest_bank");

    String strSourceCurr = request.getParameter("source_curr");
    String strDestCurr = request.getParameter("dest_curr");

    String strFilter = request.getParameter("filter");
    if (strFilter == null) strFilter = "ALL";

    if (strSourceBank == null) strSourceBank = "833";  // Full Suite
    if (strDestBank == null) strDestBank = "271";  // Default

    if (strSourceCurr == null) strSourceCurr = "";
    if (strDestCurr == null) strDestCurr = "";

    Long prev_bank = null, next_bank = null;

    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(Integer.parseInt(strDestBank));

    if (bankInfo != null) {
        SubCasino subCasino = SubCasinoCache.getInstance().get(bankInfo.getSubCasinoId());

        if (subCasino != null) {
            List<Long> banks = subCasino.getBankIds();

            long bankId = Long.parseLong(strDestBank);

            int bank_index = banks.indexOf(bankId);

            prev_bank = (bank_index > 0 ? banks.get(banks.indexOf(bankId) - 1) : bankId);
            next_bank = (bank_index < banks.size() - 1 ? banks.get(banks.indexOf(bankId) + 1) : bankId);

            strBanks = Arrays.toString(banks.toArray()).replace("[", "").replace("]", "").replaceAll(", ", "\\|");
            response.getWriter().write("SubCasino has banks: " + strBanks + "<br>");
        } else
            response.getWriter().write("DEST SUBCASINO is NULL<br>");
    } else
        response.getWriter().write("DEST BANK IS NULL<br>");
%>

<%!
    public String getPCName(String gameName) {
        return gameName.replace("MOBILE", "").replace("ANDROID", "").replace("WINDOWSPHONE", "");
    }

    boolean isPCGameName(String gameName) {
        if (gameName.contains("MOBILE") || gameName.contains("ANDROID") || gameName.contains("WINDOWSPHONE")) return false;
        return true;
    }
%>

<FORM ACTION="compareBanksGames.jsp" METHOD="GET">
    Сравнить список игр из банка:
    <INPUT style="text-align: left;  width: 80px" type="text" id="id_source_bank" name="source_bank" onKeyDown="return isDigit(event, this, false);" value="<%=strSourceBank%>"/>
    Валюта: <INPUT style="text-align: left;  width: 60px" type="text" id="id_source_curr" name="source_curr" value="<%=strSourceCurr%>"/>

    и из банка:
    <INPUT type="button" name="prevbank" value="<" title="Предыдущий банк" onclick="reloadPage(<%=strSourceBank%>, <%=prev_bank%>)">
    <INPUT style="text-align: left;  width: 80px" type="text" id="id_dest_bank" name="dest_bank" onKeyDown="return isDigit(event, this, false);" value="<%=strDestBank%>"/>
    <INPUT type="button" name="nextbank" value=">" title="Следующий банк" onclick="reloadPage(<%=strSourceBank%>, <%=next_bank%>)">
    Валюта: <INPUT style="text-align: left;  width: 60px" type="text" id="id_dest_curr" name="dest_curr" value="<%=strDestCurr%>"/>


    &nbsp&nbsp
    Фильтр: <SELECT style="width: 80px" name="filter">
    <OPTION value="ALL"           <% if (strFilter.equals("ALL")) { %> selected="selected" <%}%>>ALL</OPTION>
    <OPTION value="SLOTS"         <% if (strFilter.equals("SLOTS")) { %> selected="selected" <%}%>>SLOTS</OPTION>
    <OPTION value="TABLE"         <% if (strFilter.equals("TABLE")) { %> selected="selected" <%}%>>TABLE</OPTION>
    <OPTION value="ONLY PC"       <% if (strFilter.equals("ONLY PC")) { %> selected="selected" <%}%>>ONLY PC</OPTION>
    <OPTION value="ONLY TOGO"     <% if (strFilter.equals("ONLY TOGO")) { %> selected="selected" <%}%>>ONLY TOGO</OPTION>
    <OPTION value="FULL SUITE"    <% if (strFilter.equals("FULL SUITE")) { %> selected="selected" <%}%>>FULL SUITE</OPTION>
    <OPTION value="LGA_APPROVED"  <% if (strFilter.equals("LGA_APPROVED")) { %> selected="selected" <%}%>>LGA_APPROVED</OPTION>
</SELECT>&nbsp&nbsp

    <INPUT style="width: 80px" type="submit" value="ОК"/>
</FORM>

<%
    if (((request.getParameter("source_bank") != null) && (request.getParameter("dest_bank") != null)) && // If form submited
            ((strSourceBank.equals("") || strDestBank.equals("")))) {// If parameters not correct
%>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Необходимо ввести ID банков</DIV>
<%
} else {
    long sourceBankID = Long.parseLong(strSourceBank);
    long destBankID = Long.parseLong(strDestBank);

    BankInfo sourceBankInfo = BankInfoCache.getInstance().getBankInfo(sourceBankID);
    BankInfo destBankInfo = BankInfoCache.getInstance().getBankInfo(destBankID);

    if (sourceBankInfo == null) { %>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Банк <%=sourceBankID%> не существует</DIV>
<% } else if (destBankInfo == null) { %>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Банк <%=destBankID%> не существует</DIV>
<% } else {
    Currency sourceCurrency = sourceBankInfo.getDefaultCurrency();
    Currency destCurrency = destBankInfo.getDefaultCurrency();

    if (!strSourceCurr.isEmpty()) sourceCurrency = CurrencyCache.getInstance().get(strSourceCurr);
    if (!strDestCurr.isEmpty()) destCurrency = CurrencyCache.getInstance().get(strDestCurr);

    Set<Long> sourceGameIds = BaseGameCache.getInstance().getAllGamesSet(sourceBankID, sourceCurrency);

    ArrayList<Game> missedGames = new ArrayList<Game>();

    for (Long gameId : sourceGameIds) {
        String descr = "(Отсутствует)";

        IBaseGameInfo sourceGame = BaseGameCache.getInstance().getGameInfoShared(sourceBankID, gameId, sourceCurrency);
        IBaseGameInfo destGame = BaseGameCache.getInstance().getGameInfoShared(destBankID, gameId, destCurrency);

        // Apply FILTER ////////////////////////////////////////////////////////////////////////////////////
        if (strFilter.equals("ALL")) {
        }
        if (strFilter.equals("SLOTS") && (sourceGame.getGroup() != GameGroup.SLOTS)) continue;
        if (strFilter.equals("TABLE") && (sourceGame.getGroup() != GameGroup.TABLE)) continue;

        if (strFilter.equals("ONLY PC")) {
            if (!isPCGameName(sourceGame.getName())) continue;
        }

        if (strFilter.equals("ONLY TOGO")) {
            if (isPCGameName(sourceGame.getName())) continue;
        }

        if (strFilter.equals("FULL SUITE")) {
            if (!fullSuiteGames.contains(gameId)) continue;
        }
        if (strFilter.equals("LGA_APPROVED")) {
            BaseGameInfoTemplate sourceTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
            String strPC_ID = String.valueOf(gameId);
            if (sourceTemplate != null) {
                strPC_ID = sourceTemplate.getDefaultGameInfo().getProperty("PC");
                if (strPC_ID != null) {
                    BaseGameInfoTemplate pcTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(Integer.parseInt(strPC_ID));
                    if (pcTemplate != null) sourceTemplate = pcTemplate;
                    else
                        response.getWriter().write("<br>ERROR! PC Template for \"" + strPC_ID + "\" = null<br>");
                }
                String strLGA_APPROVED = sourceTemplate.getDefaultGameInfo().getProperty("LGA_APPROVED");
                if (strLGA_APPROVED == null || !strLGA_APPROVED.toUpperCase().equals("TRUE")) continue;
            } else {
                response.getWriter().write("<br>ERROR! TEMPLATE \"" + strPC_ID + "\" = null<br>");
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        if (destGame == null || (destGame.getProperty("ISENABLED") != null && !destGame.getProperty("ISENABLED").toUpperCase().
                equals(sourceGame.getProperty("ISENABLED").toUpperCase()))) {
            if (destGame != null)
                descr = "(Отключена)";

            IBaseGameInfo missedGameInfo = BaseGameCache.getInstance().getGameInfoShared(sourceBankID, gameId, sourceCurrency);
            BaseGameInfoTemplate missedTemplateGame = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);

            if (missedTemplateGame != null) {
                String title = missedTemplateGame.getTitle();
                String name = missedTemplateGame.getGameName();
                if (title == null) title = name;

                Game game = new Game(response.getWriter(), title, name, descr, gameId);
                missedGames.add(game);
            }
        }
    }

    Collections.sort(missedGames, new CustomerComparator());

    Long destSubcasino = BankInfoCache.getInstance().getSubCasinoId(destBankID);

    try {
        for (Game game : missedGames) {
            long readSourceBankID = sourceBankID;
            long readGameID = game.id;

            Long gameId = BaseGameInfoTemplateCache.getInstance().getGameIdByName(getPCName(game.name));

            if (gameId == null) {
                gameId = BaseGameInfoTemplateCache.getInstance().getGameIdByName(game.name);
            }

            if (gameId != null) {
                IBaseGameInfo destGame = BaseGameCache.getInstance().getGameInfoShared(destBankID, gameId, destCurrency);

                if (destGame != null) {
                    readSourceBankID = destGame.getBankId();
                    readGameID = destGame.getId();
                }
%>
<%=game.id%>:&ensp;<b><%=game.title%>
</b>&ensp;<%=game.descr%>
<a href="<%=request.getScheme()%>://<%=serverName%>/support/gameBankConfig/editGameForm.jsp?gamecopy=<%=readGameID%>&bankcopy=<%=readSourceBankID%>&currency=<%=strSourceCurr%>&to_subcasino=<%=destSubcasino%>">
    ВЫДАТЬ </a>
<br>
<%
                        //game.addToBank(destBankID, destBankInfo.getDefaultCurrency());
                    }
                }
            } catch (Exception ex) {
                if (ex != null && response != null) {
                    response.getWriter().write("EXCEPTION: " + ex.toString());
                    ex.printStackTrace(response.getWriter());
                }
            }
        }
    }
%>
<HR>
<a href="<%=request.getScheme()%>://<%=serverName%>/support/showGamesDetailsProp2.jsp?bankId=<%=strBanks%>"> Обновить локальные кеши </a>

</BODY>
</HTML>