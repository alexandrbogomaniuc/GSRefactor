<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    private Set<String> validJars = new HashSet<String>(Arrays.asList(
            "agcc-afternightfalls-2.0.jar",
            "agcc-americanblackjack-2.0.jar",
            "agcc-anightinparis-2.0.jar",
            "agcc-anightinparisjp-2.0.jar",
            "agcc-arrival-2.0.jar",
            "agcc-atthecopa-2.0.jar",
            "agcc-barbarycoast-2.0.jar",
            "agcc-caveman-2.0.jar",
            "agcc-enchanted-2.0.jar",
            "agcc-enchantedjp-2.0.jar",
            "agcc-gemscapades-2.0.jar",
            "agcc-gladiator-2.0.jar",
            "agcc-golddiggers-2.0.jar",
            "agcc-goodbadgirl-2.0.jar",
            "agcc-greedygoblins-2.0.jar",
            "agcc-hamburgroulette-2.0.jar",
            "agcc-heist-2.0.jar",
            "agcc-houseoffun-2.0.jar",
            "agcc-lost-2.0.jar",
            "agcc-madderscientist-2.0.jar",
            "agcc-mamamia-2.0.jar",
            "agcc-moregolddiggin-2.0.jar",
            "agcc-mrvegas-2.0.jar",
            "agcc-onceuponatime-2.0.jar",
            "agcc-paco-2.0.jar",
            "agcc-rockstar-2.0.jar",
            "agcc-rooksrevenge-2.0.jar",
            "agcc-safarisam-2.0.jar",
            "agcc-sheriff-2.0.jar",
            "agcc-slotfather-2.0.jar",
            "agcc-slotfatherjp-2.0.jar",
            "agcc-slotsangels-2.0.jar",
            "agcc-sugarpop-2.0.jar",
            "agcc-threewishes1x2-2.0.jar",
            "agcc-trueillusions-2.0.jar",
            "agcc-tycoons-2.0.jar",
            "agcc-underthebed-2.0.jar",
            "agcc-vikingage-2.0.jar",
            "afternightfalls-2.0.jar",
            "allamerican-2.0.jar",
            "americanblackjack-2.0.jar",
            "americanblackjackgt-2.0.jar",
            "anightinparis-2.0.jar",
            "anightinparis7red-2.0.jar",
            "anightinparisjp-2.0.jar",
            "arrival-2.0.jar",
            "atthecopa-2.0.jar",
            "atthemovies-2.0.jar",
            "aztectreasure-2.0.jar",
            "aztectreasure2-2.0.jar",
            "aztectreasureold-2.0.jar",
            "baccarat-2.0.jar",
            "backintime-2.0.jar",
            "barbarycoast-2.0.jar",
            "betuppoker-2.0.jar",
            "blackgold-2.0.jar",
            "bonusdeuces-2.0.jar",
            "bonuspoker-2.0.jar",
            "boombucks-2.0.jar",
            "burnblackjack-2.0.jar",
            "captaincash-2.0.jar",
            "carpoker-2.0.jar",
            "casinowarv2-2.0.jar",
            "caveman-2.0.jar",
            "chasethecheese-2.0.jar",
            "craps-2.0.jar",
            "crazyjackpot6000-2.0.jar",
            "curiousmachine-2.0.jar",
            "curiousmachineplus-2.0.jar",
            "dashforcash-2.0.jar",
            "deucedblup-2.0.jar",
            "deucesandjokers-2.0.jar",
            "diamonddreams-2.0.jar",
            "diamondps-2.0.jar",
            "doublebonus-2.0.jar",
            "doublejoker-2.0.jar",
            "doublesixteen-2.0.jar",
            "drawhilo-2.0.jar",
            "drjekyllandmrhyde-2.0.jar",
            "enchanted-2.0.jar",
            "enchantedjp-2.0.jar",
            "europeanblackjack-2.0.jar",
            "exterminator-2.0.jar",
            "fireworks-2.0.jar",
            "fivedrawpoker-2.0.jar",
            "froghunter-2.0.jar",
            "fruitzen-2.0.jar",
            "gemscapades-2.0.jar",
            "ghouls-2.0.jar",
            "ghoulsgold-2.0.jar",
            "gladiator-2.0.jar",
            "glamlife-2.0.jar",
            "glamlifegb-2.0.jar",
            "golddiggers-2.0.jar",
            "goodbadgirl-2.0.jar",
            "greedygoblins-2.0.jar",
            "gypsyrose-2.0.jar",
            "hamburgroulette-2.0.jar",
            "hamilton-2.0.jar",
            "heist-2.0.jar",
            "hellraiser-2.0.jar",
            "hiddenloot-2.0.jar",
            "holdemfoldem-2.0.jar",
            "holdthempoker-2.0.jar",
            "hotshot-2.0.jar",
            "houseoffun-2.0.jar",
            "instantkeno-2.0.jar",
            "invaders-2.0.jar",
            "itcamefromvenus-2.0.jar",
            "itcamefromvenusjp-2.0.jar",
            "itcamefromvenusjpplus-2.0.jar",
            "jackdblup-2.0.jar",
            "jackpot2000-2.0.jar",
            "jackpot2000mobile-2.0.jar",
            "jackpot2000norg-2.0.jar",
            "jackpot2000norgVIP-2.0.jar",
            "jackpot2000v9-2.0.jar",
            "jackpotgagnant-2.0.jar",
            "jamba-2.0.jar",
            "jokerdblup-2.0.jar",
            "keno-2.0.jar",
            "klubkeno-2.0.jar",
            "krazykeno-2.0.jar",
            "lost-2.0.jar",
            "lostlv-2.0.jar",
            "lucky7-2.0.jar",
            "madderscientist-2.0.jar",
            "madscientist-2.0.jar",
            "magiclines-2.0.jar",
            "mamamia-2.0.jar",
            "megajackpot-2.0.jar",
            "mermaidspearl-2.0.jar",
            "modifiedblackjackacs-2.0.jar",
            "modifiedblackjackacseu-2.0.jar",
            "monkeymoney-2.0.jar",
            "moregolddiggin-2.0.jar",
            "mrvegas-2.0.jar",
            "mrvegasmini-2.0.jar",
            "multihandbjacs-2.0.jar",
            "multihandpocker-2.0.jar",
            "nedandtherats-2.0.jar",
            "oasispoker-2.0.jar",
            "oldtimer-2.0.jar",
            "onceuponatime-2.0.jar",
            "outworld-2.0.jar",
            "paco-2.0.jar",
            "paigow-2.0.jar",
            "pharaohking20-2.0.jar",
            "pinocchio-2.0.jar",
            "pirate21-2.0.jar",
            "plumbo-2.0.jar",
            "pontoonv2-2.0.jar",
            "predictor-2.0.jar",
            "puppylove-2.0.jar",
            "puppyloveplus-2.0.jar",
            "pyramidpoker-2.0.jar",
            "randomrunner-2.0.jar",
            "reddog-2.0.jar",
            "redhot7s-2.0.jar",
            "reeloutlaws-2.0.jar",
            "revolution-2.0.jar",
            "ridempokerv2-2.0.jar",
            "rockstar-2.0.jar",
            "rollintrolls-2.0.jar",
            "rooksrevenge-2.0.jar",
            "roulette-2.0.jar",
            "royalreels-2.0.jar",
            "safarisam-2.0.jar",
            "scratcherz-2.0.jar",
            "scratcherzv2-2.0.jar",
            "sevenheaven-2.0.jar",
            "sheriff-2.0.jar",
            "simplywild-2.0.jar",
            "singledeckblackjack-2.0.jar",
            "slotfather-2.0.jar",
            "slotfatherjp-2.0.jar",
            "slotsangels-2.0.jar",
            "splitwayroyal-2.0.jar",
            "sugarpop-2.0.jar",
            "sugarpopplus-2.0.jar",
            "superjoker-2.0.jar",
            "superjokervip-2.0.jar",
            "supermoneywheel-2.0.jar",
            "sushibar-2.0.jar",
            "sushibarv2-2.0.jar",
            "sweettreats-2.0.jar",
            "tensbetter-2.0.jar",
            "thebees-2.0.jar",
            "thebeessb-2.0.jar",
            "threecardpoker-2.0.jar",
            "threecardrummy-2.0.jar",
            "threewishes1x2-2.0.jar",
            "treasureroom-2.0.jar",
            "triplecrown-2.0.jar",
            "trueillusions-2.0.jar",
            "tycoons-2.0.jar",
            "underthebed-2.0.jar",
            "underthesea-2.0.jar",
            "vikingage-2.0.jar",
            "vrb3d-2.0.jar",
            "whospunit-2.0.jar",
            "whospunitplus-2.0.jar",
            "whospunitv2-2.0.jar",
            "whospunitv3-2.0.jar",
            "wizardscastle-2.0.jar"
    ));

    private void processRepositoryFiles(long gameId, BaseGameInfo info, String gameControllerClass, PrintWriter writer) {
        if (info == null) {
            writer.write("Empty defaultGame for " + gameId + "<br/>");
            return;
        }
        String prefix;
        int ind;
        if (gameControllerClass.startsWith("com.casino.singlegames.")) {
            prefix = "agcc-";
            ind = 3;
        } else if (gameControllerClass.startsWith("com.dgphoenix.casino.singlegames.")) {
            prefix = "";
            ind = 4;
        } else {
            writer.write("Incorrect gameController=" + gameControllerClass + " for gameId=" + gameId + "<br/>");
            return;
        }
        String[] split = gameControllerClass.split(Pattern.quote("."));
        if (split.length < (ind + 1)) {
            writer.write("Incorrect gameController=" + gameControllerClass + " for gameId=" + gameId + "<br/>");
            return;
        }
        String jarName = "agcc-" + split[ind] + "-2.0.jar";
        if (!validJars.contains(jarName)) {
            writer.write(
                    "Incorrect gameController=" + gameControllerClass + " for gameId=" + gameId + " invalide jar name=" + jarName + "<br/>");
            return;
        }
        writer.write(gameControllerClass + "         " + jarName + "<br/>");
        //info.setProperty(BaseGameConstants.KEY_REPOSITORY_FILE, jarName);
    }
%>
<%
    Collection<BaseGameInfoTemplate> templates = BaseGameInfoTemplateCache.getInstance().getAllObjects().values();
    PrintWriter writer = response.getWriter();
    for (BaseGameInfoTemplate template : templates) {
        processRepositoryFiles(template.getGameId(), template.getDefaultGameInfo(), template.getGameControllerClass(), writer);
    }
%>