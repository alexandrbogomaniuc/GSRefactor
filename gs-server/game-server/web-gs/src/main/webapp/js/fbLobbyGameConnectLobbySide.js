var gameFrameId;
var gameFlashId;
function initLS(aGameFrameId, aGameFlashId) {
    this.gameFrameId = aGameFrameId;
    this.gameFlashId = aGameFlashId;
}
function isRoundCompleted() {
    try {
        var f = getFlash($("#" + gameFrameId)[0], gameFlashId);
        if (f) { return f.getRoundCompletedFromJS(); }
    } catch (e) { }
    return true;
}
function updateBalance(balance) {
    try {
        var f = getFlash($("#" + gameFrameId)[0].contentWindow, gameFlashId);
        if (f) { f.refreshBalanceFromJS(balance); }
    } catch (e) { }
}