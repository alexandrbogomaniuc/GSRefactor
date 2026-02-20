function getFlash(aWind, flashId) {
    if (navigator.userAgent.indexOf("Firefox") != -1) { return aWind.document[flashId]; }
    if (aWind.document.embeds && aWind.document.embeds[flashId]) { return aWind.document.embeds[flashId]; }
    return aWind.document.getElementById(flashId);
}