var sprite_icons  = engine.createSprite("empty.gif");
var sprite_coins = sprite_icons;
var sprite_buttons = sprite_icons;

var sprite_backgroundMain = engine.createSprite('background-main.png');

var enemies = {
    0: engine.createSprite('S1.png'),
    1: engine.createSprite('S2.png'),
    2: engine.createSprite('S3.png'),
    3: engine.createSprite('S4.png'),
    4: engine.createSprite('S5.png'),
    5: engine.createSprite('S6.png'),
    6: engine.createSprite('S7.png'),
    7: engine.createSprite('S8.png'),
    8: engine.createSprite('S9.png'),
    9: engine.createSprite('S10.png'),
    10: engine.createSprite('S11.png'),
    11: engine.createSprite('S12.png'),
    12: engine.createSprite('S13.png'),
    13: engine.createSprite('S14.png'),
    14: engine.createSprite('S15.png'),
    15: engine.createSprite('S16.png'),
    16: engine.createSprite('S17.png'),
    17: engine.createSprite('S18.png'),
    18: engine.createSprite('S19.png'),
    19: engine.createSprite('S20.png'),
    20: engine.createSprite('S21.png'),
    21: engine.createSprite('S22.png'),
    22: engine.createSprite('S23.png'),
    23: engine.createSprite('S24.png'),
    24: engine.createSprite('S25.png'),
    25: engine.createSprite('S26.png'),
    26: engine.createSprite('S27.png'),
    27: engine.createSprite('S28.png'),
    28: engine.createSprite('S29.png'),
    29: engine.createSprite('S30.png'),
    30: engine.createSprite('S31.png'),
    51: engine.createSprite('F1.png'),
    52: engine.createSprite('F2.png'),
    53: engine.createSprite('F3.png'),
    54: engine.createSprite('F4.png'),
    55: engine.createSprite('F5.png'),
    56: engine.createSprite('F6.png'),
    57: engine.createSprite('F7.png'),
    71: engine.createSprite('B3.png'),
    72: engine.createSprite('B2.png'),
    73: engine.createSprite('B1.png'),
    100: engine.createSprite('Boss.png'),

};

var weaponNames = {
    3: 'Laser',
    4: 'Plasma Rifle',
    7: 'Artillery Strike',
    9: 'Flamethrower',
    10: 'Cryogun'
};

var specialItemsNames = ['Money Wheel', 'Flash Blizzard',
 'Enemy Seeker',
 'Multiplier Bomb',
 'Chain Reaction Shot',
'Arc Lighthing',
'Laser Net']

var screenIndex = 0;
var idx = 0;
var idxLevel = 0;
var pagesRowData;

engine.setRowClickEvent(rowClick);
engine.setRowCreateEvent(createRowEvent);
engine.setDrawCallback(draw);

engine.setLineColors(COLOR_ARRAY_10);
engine.setLineThickness(2);

function rowClick() {
    screenIndex = 0;
    idx = 0;
    pagesRowData = [[]];
}

function createRowEvent(row) {
    var rowData = row.publicText.split(';');
    if (rowData[3] != null) {
        var playerRoundId = rowData[2].split('=')[1];
        row.setRoundID(playerRoundId)
    }
    if (row !== engine.getLastRow()) {
        row.setStateText('');
    } else {
        row.setStateText('Game End');
    }
}

var center_x = null;

function start() {
    center_x = engine.getCanvasWidth() / 2;
    sprite_icons.splitFrames(16, 4, 4);
    sprite_icons.setAsReels(5, 3, 0, 18, -3, -3);
    sprite_coins.splitFrames(2, 1, 2);
    sprite_buttons.splitFrames(4, 1, 4);
}

function initData(row) {
    var publicData = row.publicText.split(';');
    var length = publicData.length;
    var data = '';
    for (var idx = 0; idx < 9; idx++) {
        data = data + publicData[idx] + ';';
    }
    pagesRowData[0] = data;
    var cnt = 1;

    for (idx = 9; idx < length; idx++) {
        if (publicData[idx].includes('hitMiss')) {
            pagesRowData[cnt] = publicData[idx];
            cnt++;
        }

        if (publicData[idx].includes('wStatBySources')) {
            pagesRowData[cnt] = publicData[idx];
            cnt++;
        }
    }


    for (var idEnemyType = 0; idEnemyType < 101; idEnemyType++) {
        var array = [];
        for (idx = 9; idx < length; idx++) {
            var name = 'name=' + idEnemyType + '_';
            if (publicData[idx].includes(name)) {
                array.push(publicData[idx]);
            }
        }
        if (array.length > 0) {
            pagesRowData[cnt] = array;
            cnt++;
        }
    }
}

function draw(row) {
    var font_size = 13;
    var text_start = 190;
    var text_step = 15;
    var text_step_weapon_stats = 20;

    engine.clearCanvas();

    if (pagesRowData.length === 1) {
        initData(row)
    }

    var rowData = row.publicText.split(';');

    if (row !== engine.getLastRow()) {
        console.log(row);
        console.log(engine.getLastRow());
        engine.drawRectangle(0, sprite_backgroundMain.size_y, sprite_backgroundMain.size_x, 20, setColor(0, 0, 0, 0.6), 1, COLOR_BLACK);

        engine.drawText('>>', center_x + 120, text_start - 10, true, font_size, COLOR_GOLD, true, FONT_NAME_ARIAL,
                function () {
                    idx = idx + 1;
                    if (idx === 2) idx = 3;
                    idxLevel = 0;
                    if (idx > pagesRowData.length - 1) {
                        idx = 0
                    }
                    draw(engine.getSelectedRow())
                });

        engine.drawText('<<', center_x - 120, text_start - 10, true, font_size, COLOR_GOLD,
                true, FONT_NAME_ARIAL, function () {
                    idx = idx - 1;
                    if (idx === 2) idx = 1;
                    idxLevel = 0;
                    if (idx < 0) {
                        idx = pagesRowData.length - 1;
                        if (idx === 2) idx = 1;
                    }
                    draw(engine.getSelectedRow())
                });


        if (pagesRowData[idx].length > 1 && idx >= 3) {
            engine.drawText('>>', center_x + 80, text_start - 10, true, font_size - 1, COLOR_GREEN, true, FONT_NAME_ARIAL,
                    function () {
                        idxLevel = idxLevel + 1;
                        if (idxLevel > pagesRowData[idx].length - 1) {
                            idxLevel = 0
                        }
                        // if (idx < 3) idxLevel = 0;
                        draw(engine.getSelectedRow())
                    });

            engine.drawText('<<', center_x - 80, text_start - 10, true, font_size - 1, COLOR_GREEN,
                    true, FONT_NAME_ARIAL, function () {
                        idxLevel = idxLevel - 1;
                        if (idxLevel < 0) {
                            idxLevel = pagesRowData[idx].length - 1;
                        }
                        // if (idx < 3) idxLevel = 0;
                        draw(engine.getSelectedRow())
                    });
        }
        processScreenData(rowData, text_start, text_step, text_step_weapon_stats, idx, idxLevel);
    }
}

function showWeapons(text_start, idx, text_step, data, fromIdx) {
    var fsize1 = 11;
    var mult1 = 0.9;
    var fsize2 = 9;
    var mult2 = 0.6;

    engine.drawText('Weapon', center_x - 130, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    engine.drawText('Payout/Bets', center_x - 30, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    engine.drawText('Shots/Kills', center_x + 55, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    idx = idx + 3;
    var len = 13;
    for (var i = fromIdx; i < fromIdx + len; i++) {
        if (i < data.length) {
            var split = data[i].split('=');
            var name = split[0].substr(2);
            var weapon = split[1];
            if (weapon != null) {
                var params_w = weapon.split(',');
                var payout_w = (params_w[0] / 100).toFixed(2);
                var kills_w = params_w[1];
                var shots = params_w[2];
                var payBets = 0;
                if (params_w.length > 3) {
                    payBets = (params_w[3] / 100).toFixed(2);
                }
                engine.drawText(name, center_x - 130, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
                engine.drawText(payout_w + '/' + payBets, center_x - 20, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
                engine.drawText(' ' + shots + '/' + kills_w, center_x + 70, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
                idx++;
            }
        }
    }
}

function toMoney(param) {
    return (param / 100).toFixed(2);
}

function processScreenData(rowData, text_start, text_step, text_step_weapon_stats, screenIndex, idxBetLevel) {
    var idx;

    if (screenIndex === 0) {
        sprite_backgroundMain.draw();
        idx = 1;
        var roomStake = toMoney(rowData[7].split('=')[1]);
        engine.drawText('Base stats', center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        engine.drawText('Room Id: ' + rowData[4].split('=')[1] + ' stake: ' + roomStake, center_x, text_start + idx * text_step, false, 12);
        idx++;
        var kk = 0;
        var res = '';
        var weaponSurplusMoney = '';
        var moneyWheelCompleted = '';
        var moneyWheelPayouts = '0';

        for (var ii = 2; ii < rowData.length; ii++) {
            if (rowData[ii].includes('weaponSurplusMoney')) {
                weaponSurplusMoney = rowData[ii];
            }

            if (rowData[ii].includes('moneyWheelCompleted')) {
                moneyWheelCompleted = rowData[ii].split('=')[1];
            }

            if (rowData[ii].includes('moneyWheelPayouts')) {
                moneyWheelPayouts = rowData[ii].split('=')[1];
            }

            if (rowData[ii].includes('additionalWins') && !rowData[ii].includes('no additional wins')) {
                var wins_ = rowData[ii].substring(15).split('&');
                for (kk = 0; kk < wins_.length; kk++) {
                    var win = wins_[kk];
                    var w = win.split(',');
                    if (win.length > 0) {
                        if (w[0].includes('totalGemsPayout')) {
                            res = 'Total gem pays cnt: ' + w[1] + '    payouts: ' + toMoney(w[2]);
                            engine.drawText(res, center_x, text_start + idx * text_step, false, 9);
                            idx++;
                        }
                    }
                }
            }
        }
        idx++;

    } else if (screenIndex === 1) {
        sprite_backgroundMain.draw();
        engine.drawText('Weapon stats', center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        idx = 1;

        engine.drawText('Weapon: ', center_x - 120, text_start + idx * text_step_weapon_stats, false, 12, COLOR_BLACK, false);
        let yPosForWeapon = text_start + idx * text_step_weapon_stats;
        idx++;
        engine.drawText('Real shots: ', center_x - 120, text_start + idx * text_step_weapon_stats, false, 12, COLOR_BLACK, false);
        let yPosForRealShots = text_start + idx * text_step_weapon_stats
        idx++;
        engine.drawText('Hits: ', center_x - 120, text_start + idx * text_step_weapon_stats, false, 12, COLOR_BLACK, false);
        let yPosForHits = text_start + idx * text_step_weapon_stats
        idx++;
        engine.drawText('Miss: ', center_x - 120, text_start + idx * text_step_weapon_stats, false, 12, COLOR_BLACK, false);
        let yPosForMiss = text_start + idx * text_step_weapon_stats
        idx++;
//        idx++;
//        idx++;

        for (ii = 2; ii < rowData.length; ii++) {
            if (rowData[ii].includes('hitMiss')) {
                var weaponsData = rowData[ii].split('=')[1];
                var wParams = weaponsData.split('&');

                for (var ll = 0; ll < wParams.length; ll++) {
                    var weapon = wParams[ll];
                    var wParam = weapon.split(',');
                    var wName = weapon.includes('-1') ? 'Turret' : weaponNames[wParam[0]];
                    if (typeof wName !== 'undefined') {
                        var y = text_start + idx * text_step * 0.6;
                        engine.drawText(addSpace(wName, 20), center_x, yPosForWeapon, false,
                                10, COLOR_BLACK, false);
                        engine.drawText(parseInt(wParam[1]) - parseInt(wParam[4]), center_x, yPosForRealShots, false,
                                10, COLOR_BLACK, false);
                        engine.drawText(parseInt(wParam[2]), center_x, yPosForHits, false,
                                10, COLOR_BLACK, false);
                        engine.drawText(parseInt(wParam[3]), center_x, yPosForMiss, false,
                                10, COLOR_BLACK, false);
                        idx++;
                    }
                }
            }
        }
    } else if (screenIndex === 2) {
        /*sprite_backgroundMain.draw();
        engine.drawText('Weapon sources', center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        idx = 1;
        var resTitle = addSpace('Weapon', 12)
                + addSpace('Shots', 8);
        engine.drawText(resTitle, center_x - 120, text_start + idx * text_step,
                false, 12, COLOR_BLACK, false);
        idx++;
        idx++;

        //wStatBySources=Railgun,Enemy=5,&Flamethrower,Enemy=15,&Mine Launcher,LootBox=57,&Machine Gun,LootBox=66,&Laser,Enemy=27,&Plasma Rifle,LootBox=110,Quest=30,&Artillery Strike,LootBox=9,&Shotgun,Quest=40,&Rocket Launcher,LootBox=66,Enemy=25,&Cryogun,LootBox=9,&
        for (ii = 2; ii < rowData.length; ii++) {
            if (rowData[ii].includes('wStatBySources')) {
                wParams = rowData[ii].substring(15).split('&')
                for (ll = 0; ll < wParams.length; ll++) {
                    weapon = wParams[ll];
                    wParam = weapon.split(',');
                    wName = wParam[0];
                    if (wName.length > 0) {
                        y = text_start + idx * text_step * 0.6;
                        engine.drawText(addSpace(wName, 20), center_x - 120, y, false, 9, COLOR_BLACK, false);
                        var enemiesNumber = 0;
                        var questsNumber = 0;
                        var lootboxNumber = 0;

                        for (var jj = 1; jj < wParam.length; jj++) {
                            var ws = wParam[jj].split('=');
                            var source = ws[0];
                            var shots = ws[1];
                            if (source.includes('Enemy')) {
                                enemiesNumber = parseInt(enemiesNumber + shots);
                            } else if (source.includes('Quest')) {
                                questsNumber = parseInt(questsNumber + shots);
                            }
                            if (source.includes('LootBox')) {
                                lootboxNumber = parseInt(lootboxNumber + shots);
                            }
                        }
                        var s1 = addSpace(enemiesNumber, 10);
                        engine.drawText(s1, center_x - 15, y, false, 9, COLOR_BLACK, false);
                        idx++;
                    }
                }
            }
        }*/

    } else if (pagesRowData[screenIndex][0].includes('name=')) {
        var enemyData = pagesRowData[screenIndex][idxBetLevel];
        var data = enemyData.split('&');
        var nameParam = data[0].split('=')[1];
        var split = nameParam.split('_');
        var enemyName = split[1];
        var enemyTypeId = split[0];
        var betLevel = split[2];

        enemies[enemyTypeId].draw();
        engine.drawTextStroke(enemyName, center_x, text_start - 30, 2, COLOR_BLACK, true, 13, COLOR_WHITE);

        engine.drawText('Bet level: ' + betLevel,
                center_x, text_start - 10, true, 13, COLOR_WHITE, true);

        var cntShots = data[1].split('=')[1];
        var payout = toMoney(data[2].split('=')[1]);
        var kills = data[3].split('=')[1];
        var turretBets = toMoney(data[4].split('=')[1]);
        //chMultipliers={1=3, 2=2, 3=0}
        var chMults1 = data[5].split('chMultipliers');
        var ch1 = chMults1[1].split(',')[0].split('{')[1].split('=')[1];
        var ch2 = chMults1[1].split(',')[1].split('=')[1];
        var ch3 = chMults1[1].split(',')[2].split('=')[1];
        var ch4 = chMults1[1].split(',')[3].split('}')[0].split('=')[1];

        var pays = data[6].split('payoutsFromItems={')[1].split('}')[0];
        var F1 = pays.split('Money Wheel=')[1].split(',')[0];
        var F2 = pays.split('Flash Blizzard=')[1].split(',')[0];
        var F3 = pays.split('Enemy Seeker=')[1].split(',')[0];
        var F4 = pays.split('Multiplier Bomb=')[1].split(',')[0];
        var F5 = pays.split('Chain Reaction Shot=')[1].split(',')[0];
        var F6 = pays.split('Arc Lighthing=')[1].split(',')[0];
        var F7 = pays.split('Laser Net=')[1].split(',')[0];

        idx = 1;
        engine.drawText('Bets: ' + turretBets, center_x, text_start + text_step, false, 11);
        idx++;
        engine.drawText('Payouts: ' + payout, center_x, text_start + idx * text_step, false, 11);
        idx++;
        engine.drawText('Shots: ' + cntShots + ' Kills: ' + kills, center_x, text_start + idx * text_step, false, 11);
        idx++;
        if (specialItemsNames.includes(enemyName) && enemyName === "Multiplier Bomb") {
            engine.drawText('Bomb multipliers:', center_x, text_start + idx * text_step, true, 11);
            idx++;
            engine.drawText('1: ' + ch1 + '    ' + '   2: ' + ch2 + '    ' + ' 3: ' + ch3 + '  ' + ' 4: ' + ch4 + '  ', center_x, text_start + idx * text_step, false, 11);
            idx++;
        } else if (!specialItemsNames.includes(enemyName)){
            engine.drawText('Critical hits (multipliers):', center_x, text_start + idx * text_step, true, 11);
            idx++;
            engine.drawText('1: ' + ch1 + ' kills   ' + '   2: ' + ch2 + ' kills   ' + ' 3: ' + ch3 + ' kills ' + ' 4: ' + ch4 + ' kills ', center_x, text_start + idx * text_step, false, 11);
            idx++;
            engine.drawText('Payouts from special items:', center_x, text_start + idx * text_step, true, 11);
            idx++;
            engine.drawText('Flash Blizzard: ' + toMoney(F2), center_x, text_start + idx * text_step, false, 11);
            idx++;
            engine.drawText('Enemy Seeker: ' + toMoney(F3), center_x, text_start + idx * text_step, false, 11);
            idx++;
            engine.drawText('Multiplier Bomb: ' + toMoney(F4), center_x, text_start + idx * text_step, false, 11);
            idx++;
            engine.drawText('Chain Reaction Shot: ' + toMoney(F5), center_x, text_start + idx * text_step, false, 11);
            idx++;
            engine.drawText('Arc Lighthing: ' + toMoney(F6), center_x, text_start + idx * text_step, false, 11);
            idx++;
            engine.drawText('Laser Net: ' + toMoney(F7), center_x, text_start + idx * text_step, false, 11);
            idx++;
            engine.drawText("      ", center_x, text_start + idx * text_step - 45, false, 11);

        }

        /*engine.drawText('F3: ' + toMoney(F3), center_x, text_start + idx * text_step, false, 11);
        idx++;*/

        /*engine.drawText('F4: ' + toMoney(F4), center_x, text_start + idx * text_step, false, 11);
        idx++;
        engine.drawText('F5: ' + toMoney(F5), center_x, text_start + idx * text_step, false, 11);
        idx++;*/

        /*engine.drawText('F6: ' + toMoney(F6), center_x, text_start + idx * text_step, false, 11);
        idx++;
        engine.drawText('F7: ' + toMoney(F7), center_x, text_start + idx * text_step, false, 11);
        idx++;*/

        //showWeapons(text_start, idx, text_step, data, 5);
    }
}

function addSpace(str, len) {
    var string = str.toString();
    return string.length < len ? string.concat(' '.repeat(len - string.length)) : string;
}
