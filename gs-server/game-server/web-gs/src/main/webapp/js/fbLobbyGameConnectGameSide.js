var lobbyFlashId;
function initGS(eLobbyFlashId) {
    lobbyFlashId = eLobbyFlashId;
}
function updateBalance(balance) {
    try {
        var f = getFlash(window.parent, lobbyFlashId);
        if (f) { f.refreshBalanceFromJS(balance); }
    } catch (e) { }
}
function onRoundCompleted() {
    try {
        var f = getFlash(window.parent, lobbyFlashId);
        if (f) { f.onRoundCompletedJS(); }
    } catch (e) { }
}