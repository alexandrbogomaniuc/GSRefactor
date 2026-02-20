<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%
    String rounds = request.getParameter("rounds");
    String lang = request.getParameter("lang");
    String mini = request.getParameter("mini");
    boolean isMini = (!StringUtils.isTrimmedEmpty(mini) && mini.equals("yes"));

    String headMessage = "You are now playing with your free round bonus, enjoy and good luck!";
    String remainMessage = "XXX Free Rounds Remain";
    String closeMessage = "Close";
    if (!StringUtils.isTrimmedEmpty(lang)) {

        switch (lang) {
            case "no":
                headMessage = "Du spiller n\u00e5 med din gratis-runde bonus, ha det morsomt og lykke til!";
                remainMessage = "XXX gratis runder/runde gjenst\u00e5r";
                break;
            case "cz":
                headMessage = "Nyn\u00ed hrajete s bonusem kolo zdarma, hodn\u011b \u0161t\u011bst\u00ed!";
                remainMessage = "Zb\u00fdv\u00e1 XXX kolo/kola/kol zdarma";
                break;
            case "dk":
                headMessage = "Du er nu i gang med din gratis bonusrunde. Held og lykke!";
                remainMessage = "XXX gratisrunde(r) tilbage";
                break;
            case "de":
                headMessage = "Sie spielen jetzt mit Ihrem Freispielbonus, viel Spa\u00df und viel Gl\u00fcck!";
                remainMessage = "XXX Freirunden/Runden verbleibend";
                break;
            case "el":
                headMessage = "\u03a4\u03ce\u03c1\u03b1 \u03c0\u03b1\u03af\u03b6\u03b5\u03c4\u03b5 \u03bc\u03b5 \u03c4\u03bf\u03bd \u03b4\u03c9\u03c1\u03b5\u03ac\u03bd \u03b3\u03cd\u03c1\u03bf \u03bc\u03c0\u03cc\u03bd\u03bf\u03c5\u03c2 \u03c3\u03b1\u03c2, \u03b1\u03c0\u03bf\u03bb\u03b1\u03cd\u03c3\u03c4\u03b5 \u03ba\u03b1\u03b9 \u03ba\u03b1\u03bb\u03ae \u03c4\u03cd\u03c7\u03b7!";
                remainMessage = "XXX \u0394\u03c9\u03c1\u03b5\u03ac\u03bd \u0393\u03cd\u03c1\u03bf\u03b9/\u0393\u03cd\u03c1\u03bf\u03c2 \u0391\u03c0\u03bf\u03bc\u03ad\u03bd\u03bf\u03c5\u03bd/\u0391\u03c0\u03bf\u03bc\u03ad\u03bd\u03b5\u03b9";
                break;
            case "hu":
                headMessage = "\u00d6n most az ingyenes b\u00f3nuszk\u00f6r\u00e9vel j\u00e1tszik, j\u00f3 sz\u00f3rakoz\u00e1st \u00e9s sok szerencs\u00e9t!";
                remainMessage = "XXX ingyenes k\u00f6r van h\u00e1tra";
                break;
            case "it":
                headMessage = "Ora stai giocando con il tuo Giro Bonus Gratis, divertiti e in bocca al lupo!";
                remainMessage = "Hai ancora XXX Giri/Giro Gratis";
                closeMessage = "Chiudi";
                break;
            case "bg":
                headMessage = "\u0412 \u043c\u043e\u043c\u0435\u043d\u0442\u0430 \u0438\u0433\u0440\u0430\u0435\u0442\u0435 \u0441 \u0431\u043e\u043d\u0443\u0441 \u0431\u0435\u0437\u043f\u043b\u0430\u0442\u0435\u043d \u043a\u0440\u044a\u0433, \u0443\u0441\u043f\u0435\u0445!";
                remainMessage = "\u041e\u0441\u0442\u0430\u0432\u0430\u0442 XXX \u0411\u0435\u0437\u043f\u043b\u0430\u0442\u043d\u0438 \u041a\u0440\u044a\u0433\u0430/\u041a\u0440\u044a\u0433";
                break;
            case "pl":
                headMessage = "Grasz teraz w bezp\u0142atnej rundzie bonusowej, baw si\u0119 dobrze, powodzenia!";
                remainMessage = "Pozosta\u0142o bezp\u0142atnych rund: XXX";
                break;
            case "pt":
                headMessage = "Est\u00e1 a jogar o seu B\u00f3nus de Ronda Gr\u00e1tis, por isso, desfrute e boa sorte!";
                remainMessage = "Restam XXX Rondas Gr\u00e1tis";
                break;
            case "ro":
                headMessage = "Jucati utilizand rundele bonus gratuite, distrati-va si sa aveti mult noroc!";
                remainMessage = "XXX Runde Gratuite Ramase/Runda Gratuita Ramasa";
                break;
            case "es":
                headMessage = "Est\u00e1 jugando con su ronda de 'Bonus' gratuita, divi\u00e9rtase y \u00a1buena suerte!";
                remainMessage = "XXX Rondas gratis restantes/Ronda gratis restante";
                break;
            case "sk":
                headMessage = "Teraz hr\u00e1te so svojim bonusom Kolo zadarmo, u\u017eite si to, ve\u013ea \u0161\u0165astia!";
                remainMessage = "Zost\u00e1va XXX k\u00f4l/kolo zadarmo";
                break;
            case "se":
                headMessage = "Du spelar nu med din bonus f\u00f6r gratisomg\u00e5ngar, ha s\u00e5 skoj och lycka till!";
                remainMessage = "XXX gratisomg\u00e5ngar \u00e5terst\u00e5r";
                break;
            case "zh-cn":
                headMessage = "\u60a8\u6b63\u5728\u8fdb\u884c\u514d\u8d39\u5956\u91d1\u6e38\u620f\u3002\u5e0c\u671b\u60a8\u73a9\u7684\u5c3d\u5174\u5e76\u8d62\u5f97\u5927\u5956\uff01";
                remainMessage = "XXX\u8f6e\u514d\u8d39\u6e38\u620f\u5269\u4f59\u3002";
                break;
            case "zh":
                headMessage = "\u60a8\u6b63\u5728\u9032\u884c\u514d\u8cbb\u734e\u91d1\u904a\u6232\u3002\u5e0c\u671b\u60a8\u73a9\u7684\u76e1\u8208\u4e26\u8d0f\u5f97\u5927\u734e\uff01";
                remainMessage = "XXX\u8f2a\u514d\u8cbb\u904a\u6232\u5269\u4f59\u3002";
                break;
            case "nl":
                headMessage = "U speelt nu met uw Gratis ronde bonus. Veel succes en plezier!";
                remainMessage = "XXX Gratis Ronde/Rondes over";
                break;
            case "fi":
                headMessage = "Pelaat nyt ilmaisia bonuskierroksiasi, nauti ja onnea peliin!";
                remainMessage = "XXX Ilmaiskierrosta/Ilmaiskierros j\u00e4ljell\u00e4";
                break;
            case "fr":
                headMessage = "Vous jouez maintenant avec le bonus tours gratuits, profitez en bien et bonne chance!";
                remainMessage = "XXX Tours Gratuits Restants";
                break;
            case "ru":
                headMessage = "\u0412\u044b \u0438\u0433\u0440\u0430\u0435\u0442\u0435 \u0432 \u0431\u0435\u0441\u043f\u043b\u0430\u0442\u043d\u044b\u0435 \u0440\u0430\u0443\u043d\u0434\u044b, \u043f\u043e\u043b\u0443\u0447\u0430\u0439\u0442\u0435 \u0443\u0434\u043e\u0432\u043e\u043b\u044c\u0441\u0442\u0432\u0438\u0435. \u0423\u0434\u0430\u0447\u0438!";
                remainMessage = "\u0411\u0435\u0441\u043f\u043b\u0430\u0442\u043d\u044b\u0445 \u0440\u0430\u0443\u043d\u0434\u043e\u0432: XXX";
                break;
            case "tr":
                headMessage = "\u015eimdi \u00fccretsiz tur bonusunuzu oynuyorsunuz, keyfini \u00e7\u0131kar\u0131n ve iyi \u015fanslar!";
                remainMessage = "XXX \u00dccretsiz Tur kald\u0131.";
                closeMessage = "Tamam";
                break;
            case "vi":
                headMessage = "B\u00e2y gi\u1edd b\u1ea1n \u0111ang ch\u01a1i v\u1edbi v\u00f2ng th\u01b0\u1edfng mi\u1ec5n ph\u00ed c\u1ee7a b\u1ea1n, xin h\u00e3y t\u1eadn h\u01b0\u1edfng v\u00e0 ch\u00fac may m\u1eafn!";
                remainMessage = "XXX V\u00f2ng Mi\u1ec5n Ph\u00ed C\u00f2n L\u1ea1i.";
                break;
            case "jp":
                headMessage = "\u73fe\u5728\u30d5\u30ea\u30fc\u30e9\u30a6\u30f3\u30c9\u30dc\u30fc\u30ca\u30b9\u306e\u30d7\u30ec\u30fc\u4e2d\u3067\u3059\u3002\u697d\u3057\u3093\u3067\u5e78\u904b\u3092\u3064\u304b\u3093\u3067\u304f\u3060\u3055\u3044\uff01";
                remainMessage = "\u30d5\u30ea\u30fc\u30e9\u30a6\u30f3\u30c9\u30dc\u30fc\u30ca\u30b9\u6b8b\u308a XXX";
                break;
            case "ko":
                headMessage = "\uc9c0\uae08 \ubb34\ub8cc\ub77c\uc6b4\ub4dc \ubcf4\ub108\uc2a4\ub85c \ud50c\ub808\uc774\ud558\uace0\uacc4\uc2ed\ub2c8\ub2e4, \ud589\uc6b4\uc744 \ube55\ub2c8\ub2e4!";
                remainMessage = "XXX \ubb34\ub8cc\ub77c\uc6b4\ub4dc \ub0a8\uc74c.";
                break;
            case "th":
                headMessage = "\u0e15\u0e2d\u0e19\u0e19\u0e35\u0e49\u0e04\u0e38\u0e13\u0e01\u0e33\u0e25\u0e31\u0e07\u0e40\u0e25\u0e48\u0e19\u0e01\u0e31\u0e1a\u0e23\u0e2d\u0e1a\u0e42\u0e1a\u0e19\u0e31\u0e2a\u0e02\u0e2d\u0e07\u0e04\u0e38\u0e13\u0e1f\u0e23\u0e35 \u0e02\u0e2d\u0e43\u0e2b\u0e49\u0e2a\u0e19\u0e38\u0e01\u0e41\u0e25\u0e30\u0e42\u0e0a\u0e04\u0e14\u0e35";
                remainMessage = "XXX \u0e23\u0e2d\u0e1a\u0e1f\u0e23\u0e35\u0e22\u0e31\u0e07\u0e40\u0e2b\u0e25\u0e37\u0e2d\u0e2d\u0e22\u0e39\u0e48";
                break;
            case "id":
                headMessage = "Anda sedang bermain dengan ronde bonus gratis anda, selamat menikmati dan semoga sukses!";
                remainMessage = "XXX Ronde Gratis Tersisa.";
                break;
        }
    }


%>
<style>
    #frbInner {
        background: url("/frb/images/frbPopupBack.png") no-repeat;
        height: 226px;
        margin: 0 auto;
        width: 457px;
        text-align: left;
        font-family: Arial, Geneva, Helvetica, sans-serif;
    }

    .close-frbPopup {
        height: 23px;
        margin-top: 33px;
        position: relative;
        width: 100%;
        text-decoration: none;
        outline: none;
        text-align: center;
        color: white;
        font-size: 13px;
        cursor: pointer;
    }

    .frbRounds {
        color: #666666;
        font-size: 14px;
        height: 25px;
        margin-left: 99px;
        margin-top: 90px;
        position: relative;
        width: 258px;
        text-align: center;
    }

    .heading {
        color: white;
        font-size: 18px;
        height: 50px;
        left: 40px;
        position: relative;
        top: 45px;
        width: 370px;
        text-align: center;
    }

    #frbContainer {
        position: fixed;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
    }

    .frbWrapper {
        position: absolute;
        top: 50%;
        margin-top: -113px;
        width: 100%;
        z-index: 99;
        text-align: center;

    <%if (isMini) {%> margin-left: -54px;
        -ms-transform: scale(0.5, 0.5); /* IE 9 */
        -webkit-transform: scale(0.5, 0.5); /* Safari */
        transform: scale(0.5, 0.5); /* Standard syntax */
    <%}%>

    }

</style>

<%
    String finalRemainMessage;
    finalRemainMessage = remainMessage.replaceFirst("XXX", rounds);
%>

<script>
    function closeFrbPopup() {
        document.getElementById("frbContainer").style.display = "none";
    }
    function showFRBDialog(frc) {
        document.getElementById("frbRoundsMessage").innerHTML = "<%=remainMessage%>".replace("XXX", frc);
        document.getElementById("frbContainer").style.display = "block";
    }
</script>

<div id="frbContainer">
    <div class="frbWrapper">
        <div id="frbInner">
            <div class="heading"><%=headMessage%>
            </div>
            <div class="frbRounds" id="frbRoundsMessage"><%=finalRemainMessage%>
            </div>
            <div class="close-frbPopup" onclick="closeFrbPopup()"><%=closeMessage%>
            </div>
        </div>
    </div>
</div>

