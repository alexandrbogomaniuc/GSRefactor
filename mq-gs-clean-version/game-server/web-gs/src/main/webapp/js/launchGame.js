function launchGame(gameId, mode, localization, startGameServer, SID) {
    var width = 800, height = 600;
    var xOffset = 0, yOffset = 0;
    var gameLaunchUrl = "http://" + startGameServer + "/";
    if (mode == "free") {
        gameLaunchUrl += "cwguestlogin.do?&gameId=" + gameId + "&lang=" + localization;
    } else if (mode == "real") {
        gameLaunchUrl += "cwstartgame.do?&gameId=" + gameId + "&lang=" + localization
            + "&sessionId=" + SID + "&mode=real";
    }
    var winPop = window.open(
        gameLaunchUrl,
        'wnd',
        'location=no,menubar=no,resizable=yes,scrollbars=no,toolbar=no,width=' + width + ',height=' + height + ',screenX=' + xOffset + ',screenY=' + yOffset + ',top=' + yOffset + ', left=' + xOffset, true);
    if (winPop == null || typeof(winPop) == "undefined") {
        alert("Sorry, the game window has been blocked. Please disable your pop-up blocker.");
    } else {
        winPop.focus();
    }
    return false;
}