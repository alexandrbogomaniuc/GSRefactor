var sprite_icons = engine.createSprite("empty.gif");
var sprite_coins = sprite_icons;
var sprite_buttons = sprite_icons;
var idx = 0;
var center_x = null;
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
    idx = 0;
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

    if (row !== engine.getLastRow()) {
        processScreenData(row.publicText, text_start, text_step);
    }
}

function toMoney(param) {
    return (param / 100).toFixed(2);
}

function processScreenData(publicText, text_start, text_step) {

    var rowData = publicText.split(';');

    engine.drawText('Room RoundId: ' + rowData[2].split('=')[1], center_x, text_start + idx * text_step, false, 12);
    idx++;

    engine.drawText('Crash KM: ' + rowData[10].split('=')[1], center_x, text_start + idx * text_step, false, 12);
    idx++;

    engine.drawText('Bet: ' + toMoney(rowData[6].split('=')[1]), center_x, text_start + idx * text_step, false, 12);
    idx++;

    engine.drawText('Total Pot: ' + toMoney(rowData[7].split('=')[1]), center_x, text_start + idx * text_step, false, 12);
    idx++;

    engine.drawText('Rake (%): ' + Number(rowData[8].split('=')[1]).toFixed(2), center_x, text_start + idx * text_step, false, 12);
    idx++;

    engine.drawText('Payout: ' + toMoney(rowData[9].split('=')[1]), center_x, text_start + idx * text_step, false, 12);
    idx++;
}


