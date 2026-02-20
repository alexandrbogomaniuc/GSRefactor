var sprite_icons = engine.createSprite("empty.gif");
var sprite_coins = sprite_icons;
var sprite_buttons = sprite_icons;
var sprite_backgroundMain_win = engine.createSprite('crash_vab_win.jpg');
var sprite_backgroundMain_lost = engine.createSprite('crash_vab_lost.jpg');
var idx = 0;
var center_x = null;
var pagesRowData = [];
var screenIndex = 0;
var templateSaltBegin = "saltDataRound=";
var templateSaltEnd = ";kpiInfo=";
var templateBetBegin = "crashBetData=";
var templateBetEnd = "&saltDataRound";


engine.setRowClickEvent(rowClick);
engine.setRowCreateEvent(createRowEvent);
engine.setDrawCallback(draw);
engine.setLineColors(COLOR_ARRAY_10);
engine.setLineThickness(2);

function rowClick() {
    screenIndex = 0;
    idx = 0;
    pagesRowData = [];
}

function createRowEvent(row) {
    var rowData = row.publicText.split(';');
    if (rowData[3] != null) {
        var playerRoundId = rowData[1].split('=')[1];
        row.setRoundID(playerRoundId)
    }
    if (row !== engine.getLastRow()) {
        row.setStateText('');
    } else {
        row.setStateText('Game End');
    }
}


function start() {
    center_x = engine.getCanvasWidth() / 2;
    sprite_icons.splitFrames(16, 4, 4);
    sprite_icons.setAsReels(5, 3, 0, 18, -3, -3);
    sprite_coins.splitFrames(2, 1, 2);
    sprite_buttons.splitFrames(4, 1, 4);
}

function draw(row) {
    var text_start = 20;
    var text_step = 25;

    engine.clearCanvas();

    if (pagesRowData.length === 0) {
        initData(row)
    }

    if (row !== engine.getLastRow()) {
        processScreenData(row.publicText, text_start, text_step);
    }
}

function initData(row) {
    var publicData = row.publicText;
    var beginBetData = publicData.indexOf(templateBetBegin);
    var endBetData = publicData.indexOf(templateBetEnd);

    var crashBetDataRow = publicData.substring(beginBetData + templateBetBegin.length, endBetData);
    var crashBetDataArray = crashBetDataRow.split('&');

    if (crashBetDataArray.length > 0) {
        for (let i = 0; i < crashBetDataArray.length; i++) {
            pagesRowData[i] = crashBetDataArray[i];
        }
    }
}

function toMoney(param) {
    return (param / 100).toFixed(2);
}

function processScreenData(publicText, text_start, text_step) {
    var betParams = pagesRowData[screenIndex].split('|');
    var font_size = 13;
    var rowData = publicText.split(';');

    const isWin = betParams[1] > 0;
    if (isWin) {
        sprite_backgroundMain_win.draw(1, 50);
    } else {
        sprite_backgroundMain_lost.draw(1, 50);
    }

    const multColor = betParams[1] > 0 ? COLOR_GREEN : COLOR_RED;
    engine.drawText(betParams[2] + "X", center_x, 130, true, 20, multColor, true, FONT_NAME_ARIAL);

    engine.drawTextStroke('BET ' + (screenIndex + 1) + " OF " + pagesRowData.length, center_x, 175, 2, COLOR_GRAY_60, true, 13, COLOR_WHITE);

    engine.drawText('>>', center_x + 120, 175, true, font_size, COLOR_GOLD, true, FONT_NAME_ARIAL,
            function () {
                screenIndex = screenIndex + 1;
                idx = 0;
                if (screenIndex > pagesRowData.length - 1) {
                    screenIndex = 0
                }
                draw(engine.getSelectedRow())
            });

    engine.drawText('<<', center_x - 120, 175, true, font_size, COLOR_GOLD,
            true, FONT_NAME_ARIAL, function () {
                screenIndex = screenIndex - 1;
                idx = 0;
                if (screenIndex < 0) {
                    screenIndex = pagesRowData.length - 1;
                }
                draw(engine.getSelectedRow())
            });

    engine.drawText('Room RoundId: ' + rowData[2].split('=')[1], center_x, text_start + idx * text_step, false, 12);
    idx++;

    engine.drawText('Crash Multiplier: ' + rowData[5].split('=')[1], center_x, text_start + idx * text_step, false, 12);
    idx++;

    idx += 5;

    if (betParams.length > 3) {
        roundWithAutoEject(betParams, text_start + 5, text_step);
    } else {
        roundWithoutAutoEject(betParams, text_start + 5, text_step)
    }

    idx += 1;

    engine.drawText('Data for verify', center_x, text_start + idx * text_step, true, 12, COLOR_BLACK, true);
    idx++;


    var startTimeMs = rowData[4].split('=')[1];
    engine.drawText('Time of start: ' + startTimeMs, center_x, text_start + idx * text_step, false,
            12, COLOR_BLACK, true, FONT_NAME_TIMES_NEW_ROMAN
    );

    idx++;

    engine.drawRectangle(center_x - 60, (text_start + idx * text_step) - 15, 120, 20, COLOR_GRAY_30, 2, COLOR_BLACK);
    engine.drawText('Copy to clipboard', center_x, text_start + idx * text_step, false,
            12, COLOR_BLACK, true, FONT_NAME_TIMES_NEW_ROMAN,
            function () {
                navigator.clipboard.writeText(startTimeMs);
            }
    );


    idx += 1;

    var beginSalt = publicText.indexOf(templateSaltBegin);
    var endSalt = publicText.indexOf(templateSaltEnd);
    var salt = publicText.substring(beginSalt + templateSaltBegin.length, endSalt);

    engine.drawText('Salt: ' + salt.substring(0, 16), center_x, text_start + idx * text_step, false,
            12, COLOR_BLACK, true, FONT_NAME_TIMES_NEW_ROMAN
    );
    idx++;
    engine.drawText(salt.substring(16, 32), center_x, text_start + idx * text_step, false, 12, COLOR_BLACK, true);
    idx++;

    engine.drawRectangle(center_x - 60, (text_start + idx * text_step) - 15, 120, 20, COLOR_GRAY_30, 2, COLOR_BLACK);
    engine.drawText('Copy to clipboard', center_x, text_start + idx * text_step, false,
            12, COLOR_BLACK, true, FONT_NAME_TIMES_NEW_ROMAN,
            function () {
                navigator.clipboard.writeText(salt);
            }
    );
    idx++;
}

function roundWithAutoEject(betParams, text_start, text_step) {
    engine.drawText('Bet: ' + toMoney(betParams[0]) + " Win: " + toMoney(betParams[1]),
            center_x, text_start + idx * text_step, false, 12);
    idx++;
    engine.drawText('Multiplier: ' + betParams[2] + " (auto-eject: " + printEject(betParams[3]) + ")",
            center_x, text_start + idx * text_step, false, 12);
    idx++;
}

function roundWithoutAutoEject(betParams, text_start, text_step) {
    engine.drawText('Bet: ' + toMoney(betParams[0]) + " Win: " + toMoney(betParams[1]) + ", Mult: " + betParams[2],
            center_x, text_start + idx * text_step, false, 12);
    idx++;
}

function printEject(param) {
    return (param === null || param === "0") ? "-" : param;
}

