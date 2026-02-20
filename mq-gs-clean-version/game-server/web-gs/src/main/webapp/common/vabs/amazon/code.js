var sprite_icons = engine.createSprite("icons.png");
var sprite_backgroundMain = engine.createSprite("backgroundMain.png");
var sprite_coins = engine.createSprite("coins.png");
var sprite_buttons = engine.createSprite("buttons.png");

var enemies = [
    engine.createSprite("1.png"),
    engine.createSprite("2.png"),
    engine.createSprite("3.png"),
    engine.createSprite("4.png"),
    engine.createSprite("5.png"),
    engine.createSprite("6.png"),
    engine.createSprite("7.png"),
    engine.createSprite("8.png"),
    engine.createSprite("9.png"),
    engine.createSprite("10.png"),
    engine.createSprite("11.png")
];

var enemyIndexes = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]

var weaponNames = [
    "Shotgun", "Grenade", "Machine Gun", "Laser", "Plasma Rifle", "Mine Launcher", "Rail Gun", "Artillery Strike",
    "Rocket Launcher", "Flamethrower", "Cryogun", "Rapid Fire Pistol", "Free Bonus Strike"
];

var sprite_boss = engine.createSprite("boss.png");
var screenIndex = 0;
var idx = 0;
var IDX_QUESTS = 26;

engine.setRowClickEvent(rowClick);
engine.setRowCreateEvent(createRowEvent);
engine.setDrawCallback(draw);

engine.setWinLines(WIN_LINES);
engine.setLineColors(COLOR_ARRAY_10);
engine.setLineThickness(2);

function rowClick() {
    screenIndex = 0;
    idx = 0;
}

function createRowEvent(row) {
    var publicData = row.publicText;
    var params = publicData.split(";");
    if (params[3] != null) {
        var playerRoundId = params[3].split("=")[1];
        row.setRoundID(playerRoundId)
    }

    if (row != engine.getLastRow()) {
        row.setStateText("");
    } else {
        row.setStateText("Game End");
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

function draw(row) {
    var font_size = 13;
    var text_start = 190;
    var text_step = 20;

    engine.clearCanvas();

    if (row != engine.getLastRow()) {
        engine.drawRectangle(0, sprite_backgroundMain.size_y, sprite_backgroundMain.size_x, 20, setColor(0, 0, 0, 0.6), 1, COLOR_BLACK);
        engine.drawText(">>", center_x + 120, text_start - 10, true, font_size, COLOR_GOLD, true, FONT_NAME_ARIAL,
            function () {
                idx = idx + 1;
                if (idx > 14) {
                    idx = 0
                }
                screenIndex = enemyIndexes[idx];
                draw(engine.getSelectedRow())
            });

        engine.drawText("<<", center_x - 120, text_start - 10, true, font_size, COLOR_GOLD,
            true, FONT_NAME_ARIAL, function () {
                idx = idx - 1;
                if (idx < 0) {
                    idx = 14;
                }
                screenIndex = enemyIndexes[idx];
                draw(engine.getSelectedRow())
            });

        processScreenData(row.publicText, screenIndex, text_start, text_step, idx);
    }
}

function showWeapons(text_start, idx, text_step, data, fromIdx) {
    var fsize1 = 11;
    var mult1 = 0.9;
    var fsize2 = 10;
    var mult2 = 0.6;

    engine.drawText("Weapon", center_x - 130, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    engine.drawText("Payout", center_x - 30, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    engine.drawText("Hits/Kills", center_x + 50, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    idx = idx + 3;
    var len =  13;
    for (var i = fromIdx; i < fromIdx + len; i++) {
        if (i < data.length) {
            var split = data[i].split("=");
            var name = split[0].substr(2);
            var weapon = split[1];
            if (weapon != null) {
                var params_w = weapon.split(",");
                var payout_w = (params_w[0] / 100).toFixed(2);
                var kills_w = params_w[1];
                var shots = params_w[2];
                engine.drawText(name, center_x - 130, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
                engine.drawText(payout_w, center_x - 20, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
                engine.drawText(shots + "/" + kills_w, center_x + 70, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
                idx++;
            }
        }
    }
}

function toMoney(param) {
    return (param / 100).toFixed(2);

}

function processScreenData(data, screenIndex, text_start, text_step, sidx) {
    var idx;
    var params = data.split(";");
    if (screenIndex === 0) {
        sprite_backgroundMain.draw();
        idx = 1;
        var roomStake = toMoney(params[8].split("=")[1]);
        engine.drawText("Base stats", center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        engine.drawText("Room Id: " + params[5].split("=")[1] + " stake: " + roomStake, center_x, text_start + idx * text_step, false, 12);
        idx++;
        engine.drawText("Weapons buyIn: " + toMoney(params[9].split("=")[1]), center_x, text_start + idx * text_step, false, 12);
        idx++;
        var kk = 0;
        var res = "";

        for (var ii = 2; ii < params.length; ii++) {
            if(params[ii].includes("lootBox")){
                var numberOfLootBoxBuy = 0;
                var strings = params[ii].split("&");
                var cost = strings[0].split("=")[1] * roomStake;
                for (kk = 0; kk < strings.length; kk++){
                    var ps = strings[kk];
                    if(ps.includes("w_")){
                        var count = ps.split(",")[1];
                        numberOfLootBoxBuy = parseInt(numberOfLootBoxBuy) + parseInt(count);
                    }
                }
                res = parseInt(numberOfLootBoxBuy);
                var cost_res = cost.toFixed(2);
                engine.drawText("LootBox, cost: " + cost_res +  " cnt: "+ res, center_x, text_start + idx * text_step, false, 9);
                idx++;
            } if(params[ii].includes("additionalWins") && !params[ii].includes("no additional wins")){
                var wins_ = params[ii].substring(15).split("&");
                for (kk = 0; kk < wins_.length; kk++){
                    var win = wins_[kk];
                    var w = win.split(",");
                    if(win.length > 0) {
                        if(w[0].includes("KillAwardWin")){
                            res = "Over kill cnt: " + w[1] + "    payouts: " + toMoney(w[2]);
                        }else{
                            res = "Boss gems cnt: " + w[1] + "    payouts: " + toMoney(w[2]);
                        }
                        engine.drawText(res, center_x, text_start + idx * text_step, false, 9);
                        idx++;
                    }
                }
            }
        }

        engine.drawText("Weapons returned: " + toMoney(params[23].split("=")[1]), center_x,
            text_start + idx * text_step, false, 12);
        idx++;

        var quests = params[IDX_QUESTS].split("&")
        var questsCollected = quests[0];
        var questsPayouts = toMoney(quests[1]);

        engine.drawText("Quests collected: " + questsCollected.split("=")[1] + "     Payouts: " + questsPayouts,
            center_x - 130, text_start + idx * text_step, true, 12, COLOR_BLACK, false);

        idx++;
    } else if (screenIndex === 1){
        sprite_backgroundMain.draw();
        engine.drawText("Weapon stats", center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        idx = 1;

        engine.drawText( "Weapon       Real shots      Hits     Miss", center_x - 120, text_start + idx * text_step,
            false, 12, COLOR_BLACK, false);
        idx++;
        idx++;

        for (ii = 2; ii < params.length; ii++) {
            if (params[ii].includes("hitMiss")) {
                var weaponsData = params[ii].split("=")[1];
                var wParams = weaponsData.split("&");

                for(var ll = 0; ll < wParams.length; ll++){
                    var weapon = wParams[ll];
                    var wParam = weapon.split(",");
                    var wName = weapon.includes("-1") ? "Pistol" : weaponNames[wParam[0]];
                    if (typeof  wName !== 'undefined') {
                        var y = text_start + idx * text_step * 0.6;
                        engine.drawText(addSpace(wName, 20),center_x - 120, y, false,
                            9, COLOR_BLACK, false);
                        engine.drawText(parseInt(wParam[1]) - parseInt(wParam[4]),center_x - 10, y, false,
                            9, COLOR_BLACK, false);
                        engine.drawText(parseInt(wParam[2]),center_x + 60, y, false,
                            9, COLOR_BLACK, false);
                        engine.drawText(parseInt(wParam[3]),center_x + 110, y, false,
                            9, COLOR_BLACK, false);
                        idx++;
                    }
                }
            }
        }

    } else if (screenIndex === 2){
        sprite_backgroundMain.draw();
        engine.drawText("Weapon sources", center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        idx = 1;
        var resTitle = addSpace("Weapon", 12)
            + addSpace("Enemy", 16)
            + addSpace("LootBox", 8);
        engine.drawText( resTitle, center_x - 120, text_start + idx * text_step,
            false, 12, COLOR_BLACK, false);
        idx++;
        idx++;

        //wStatBySources=Railgun,Enemy=5,&Flamethrower,Enemy=15,&Mine Launcher,LootBox=57,&Machine Gun,LootBox=66,&Laser,Enemy=27,&Plasma Rifle,LootBox=110,Quest=30,&Artillery Strike,LootBox=9,&Shotgun,Quest=40,&Rocket Launcher,LootBox=66,Enemy=25,&Cryogun,LootBox=9,&
        for (ii = 2; ii < params.length; ii++) {
            if (params[ii].includes("wStatBySources")) {
                wParams = params[ii].substring(15).split("&")
                for(ll = 0; ll < wParams.length; ll++){
                    weapon = wParams[ll];
                    wParam = weapon.split(",");
                    wName = wParam[0];
                    if (wName.length > 0) {
                        y = text_start + idx * text_step * 0.6;
                        engine.drawText(addSpace(wName, 20),center_x - 120, y, false,9, COLOR_BLACK, false);
                        var enemiesNumber = 0;
                        var lootboxNumber = 0;

                        for(var jj = 1; jj < wParam.length; jj++){
                            var ws = wParam[jj].split("=");
                            var source = ws[0];
                            var shots = ws[1];
                            if(source.includes("Enemy")) {
                                enemiesNumber = parseInt(enemiesNumber + shots);
                            }if(source.includes("LootBox")) {
                                lootboxNumber = parseInt(lootboxNumber + shots);
                            }
                        }
                        var s1 = addSpace(enemiesNumber, 16);
                        var s3 = addSpace(lootboxNumber, 10);
                        engine.drawText(s1, center_x - 15, y, false, 9, COLOR_BLACK, false);
                        engine.drawText(s3, center_x + 80, y, false, 9, COLOR_BLACK, false);
                        idx++;
                    }
                }
            }
        }

    } else if ((screenIndex >= 2 && screenIndex <= 13)) {
        // var newIdx = screenIndex > 12 ? screenIndex + 8 : screenIndex + 9;
        var enemyData = params[screenIndex + 7];
        // alert("screenIndex: " +  screenIndex  + "enemyData: " + enemyData)
        data = enemyData.split("&");
        enemies[screenIndex - 3].draw();
        engine.drawText(data[0].split("=")[1], center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        var cntShots = data[1].split("=")[1];
        var payout = toMoney(data[2].split("=")[1]);
        var kills = data[3].split("=")[1];
        var mainBets = toMoney(data[6].split("=")[1]);

        idx = 1;
        engine.drawText("Bets: " + mainBets + " Payouts: " + payout, center_x, text_start + text_step, false, 11);
        idx++;
        engine.drawText("Shots: " + cntShots + " Kills: " + kills, center_x, text_start + idx * text_step, false, 11);
        idx++;
        showWeapons(text_start, idx, text_step, data, 7);
    } else if (screenIndex === 14) {
        sprite_boss.draw();
        engine.drawText("Boss", center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        const bossData = params[enemyIndexes[screenIndex] + 7];
        // alert("screenIndex: " +  screenIndex +  "  boss data: " + bossData)
        params = bossData.split("&");
        params = bossData.split("&");
        const shots_b = params[0].split("=")[1];
        const kills_b = params[1].split("=")[1];
        const betExtra = toMoney(params[2].split("=")[1]);
        const sharedPrize = toMoney(params[4].split("=")[1]);
        idx = 1;
        engine.drawText("Bets: " + betExtra + " Payouts: " + sharedPrize, center_x,
            text_start + idx * text_step, false, 12);
        idx++;
        engine.drawText("Total Hits/Miss: " + shots_b + " Kills: " + kills_b, center_x,
            text_start + idx * text_step, false, 12);
        idx++;
        showWeapons(text_start, idx, text_step, params, 5);

    }
    
    function addSpace(str, len) {
        var string = str.toString();
        return string.length < len ? string.concat(" ".repeat(len - string.length)) : string;
    }
}
