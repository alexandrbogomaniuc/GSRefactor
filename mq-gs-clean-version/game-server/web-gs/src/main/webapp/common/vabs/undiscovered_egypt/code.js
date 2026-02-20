var sprite_icons = engine.createSprite("icons.png");
var sprite_backgroundMain = engine.createSprite("backgroundMain.png");
var sprite_coins = engine.createSprite("coins.png");
var sprite_buttons = engine.createSprite("buttons.png");

var enemies;
var enemies1 = [
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
    engine.createSprite("11.png"),
];

var enemies2 = [
    engine.createSprite("2_1.png"),
    engine.createSprite("2_2.png"),
    engine.createSprite("2_3.png"),
    engine.createSprite("2_4.png"),
    engine.createSprite("2_5.png"),
    engine.createSprite("2_6.png"),
    engine.createSprite("2_7.png"),
    engine.createSprite("2_8.png"),
    engine.createSprite("2_9.png"),
    engine.createSprite("2_10.png"),
    engine.createSprite("2_11.png"),
];

var weaponNames = [
    "Shotgun", "Grenade","Machine Gun", "Laser", "Plasma Gun",  "Mine Launcher", "Rail Gun", "Artillery Strike",
    "Rocket Launcher", "Flamethrower", "Cryogun", "Double Strength"
];

var sprite_boss = engine.createSprite("boss.png");
var screenIndex = 0;

var IDX_WEAPON_SYRPLUS_VBA = 22;
var IDX_WEAPON_SYRPLUS_AMOUNT = 23;
var IDX_BOSS_DATA = 23;
var IDX_QUESTS = 26;

engine.setRowClickEvent(rowClick);
engine.setRowCreateEvent(createRowEvent);
engine.setDrawCallback(draw);

engine.setWinLines(WIN_LINES);
engine.setLineColors(COLOR_ARRAY_10);
engine.setLineThickness(2);

function rowClick() {
    screenIndex = 0;
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

var version = 1;

function draw(row) {
    var font_size = 13;
    var text_start = 190;
    var text_step = 20;

    if(row.publicText.includes("quests")){
        version = 2;
        enemies = enemies2;
    }else {
        version = 1;
        enemies = enemies1;
    }

    engine.clearCanvas();

    if (row != engine.getLastRow()) {

        var screenIndexCurrent = screenIndex;

        engine.drawRectangle(0, sprite_backgroundMain.size_y, sprite_backgroundMain.size_x, 20, setColor(0, 0, 0, 0.6), 1, COLOR_BLACK);

        engine.drawText(">>", center_x + 120, text_start - 10, true, font_size, COLOR_GOLD, true, FONT_NAME_ARIAL,
            function () {
                screenIndex = screenIndex + 1;
                if (screenIndex > 12) screenIndex = 0;
                draw(engine.getSelectedRow())
            });

        engine.drawText("<<", center_x - 120, text_start - 10, true, font_size, COLOR_GOLD,
            true, FONT_NAME_ARIAL, function () {
                screenIndex = screenIndex - 1;
                if (screenIndex < 0) screenIndex = 12;
                draw(engine.getSelectedRow())
            });

        processScreenData(row.publicText, screenIndexCurrent, text_start, text_step);
    }
}

function showWeapons(text_start, idx, text_step, data, fromIdx) {
    var fsize1 = version === 1 ? 12 : 11;
    var mult1 = version === 1 ? 1 : 0.9;

    var fsize2 = version === 1 ? 12 : 10;
    var mult2 = version === 1 ? 1 : 0.6;


    engine.drawText("Weapon", center_x - 130, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    engine.drawText("Payout", center_x - 30, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    engine.drawText("Shots/Kills", center_x + 50, text_start + idx * text_step * mult1, true, fsize1, COLOR_BLACK, false);
    idx++;
    if(version === 2) idx = idx + 2;
    var len = version === 1 ? 5 : 12;
    for (var i = fromIdx; i < fromIdx + len; i++) {
        var split = data[i].split("=");
        var name = split[0].substr(2);
        var weapon = split[1];
        var params_w = weapon.split(",");
        var payout_w = (params_w[0] / 100).toFixed(2);
        var kills_w = params_w[1];
        var shots = params_w[2];
        var payBets = 0;
        if(params_w.length > 3){
            payBets = (params_w[3] / 100).toFixed(2);
        }
        engine.drawText(name, center_x - 130, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
        engine.drawText(payBets > 0 ? (payout_w + "/" + payBets) : payout_w, center_x - 20, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
        engine.drawText(shots + "/" + kills_w, center_x + 70, text_start + idx * text_step * mult2, false, fsize2, COLOR_BLACK, false);
        idx++;
    }
}

function toMoney(param) {
    return (param / 100).toFixed(2);

}

function processScreenData(data, screenIndex, text_start, text_step) {
    if(data.includes("quests")){
        version = 2;
        enemies = enemies2;
    }else {
        version = 1;
        enemies = enemies1;
    }

    var params = data.split(";");
    if (screenIndex === 0) {
        sprite_backgroundMain.draw();
        var idx = 1;
        var roomStake = toMoney(params[8].split("=")[1]);
        engine.drawText("Base stats", center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        engine.drawText("Room Id: " + params[5].split("=")[1] + " stake: " + roomStake, center_x, text_start + idx * text_step, false, 12);
        idx++;
        // engine.drawText("Room Stake: " + roomStake, center_x, text_start + idx * text_step, false, 12);
        // idx++;
        engine.drawText("XP: " + params[2].split("=")[1], center_x, text_start + idx * text_step, false, 12);
        // idx++;
        // idx++;
        // idx++;
        // engine.drawText("Weapons buyIn: " + toMoney(params[9].split("=")[1]), center_x, text_start + idx * text_step, false, 12);

        if(version === 1) {
            idx++;
            engine.drawText("Total shots with weapons: " + params[1].split("=")[1], center_x, text_start + idx * text_step, false, 12);

            idx++;
            engine.drawText("Weapons Surplus amount: " + toMoney(params[IDX_WEAPON_SYRPLUS_AMOUNT].split("=")[1])
                , center_x, text_start + idx * text_step, false, 12);

            //weaponSurplusVBA=Shotgun,24,117&Grenade,3,172&Laser,25,494&;weaponSurplusMoney=783.0;
            var weapons = params[IDX_WEAPON_SYRPLUS_VBA].split("=")[1].split("&");
            idx++;
            engine.drawText("Weapon/Shots/Surplus", center_x - 130, text_start + idx * text_step, true, 9, COLOR_BLACK, false);
            engine.drawText("Weapon/Shots/Surplus", center_x + 10, text_start + idx * text_step, true, 9, COLOR_BLACK, false);
            idx++;
            var idx_new;
            var startIdx = text_start + idx * text_step;
            for (var i = 0; i < weapons.length; i++) {
                var weapon = weapons[i];
                if (weapon !== "") {
                    var left = i < 3;
                    var paramsWeapon = weapon.split(",");
                    idx_new = startIdx + (i % 3) * 15;
                    var x = center_x - 130;
                    if (!left) x = center_x + 10;
                    var s = paramsWeapon[0] + "/" + paramsWeapon[1] + "/" + toMoney(paramsWeapon[2]);
                    engine.drawText(s, x, idx_new, false, 11, COLOR_BLACK, false);
                }
            }
        }else{
            idx++;
            engine.drawText("Weapons buyIn: " + toMoney(params[9].split("=")[1]), center_x, text_start + idx * text_step, false, 12);
            idx++;
            engine.drawText("Total shots with weapons: " + params[1].split("=")[1], center_x, text_start + idx * text_step, false, 12);

            idx++;
            //quests=2&31250.0&0,0&1,0&2,0&3,0&4,0&5,0&6,0&7,0&8,0&9,0&10,5&11,0&
            var quests = params[IDX_QUESTS].split("&")
            var questsCollected = quests[0];
            var questsPayouts = toMoney(quests[1]);

            engine.drawText("Quests collected: " + questsCollected.split("=")[1] + "     Payouts: " + questsPayouts,
                center_x - 130, text_start + idx * text_step, true, 12, COLOR_BLACK, false);

            idx++;

            var idx_new;
            var startIdx = text_start + idx * text_step;
            var cnt = 0;

            for (var i = 2; i < quests.length; i++) {
                var weapon = quests[i];
                var weaponParam = weapon.split(",");
                var weaponId = weaponParam[0];
                var count = weaponParam[1];
                if (count > 0) {
                    var col = cnt % 2;
                    var row = Math.floor(cnt/2);
                    var x = col === 0 ? center_x - 130 : center_x + 10;
                    idx_new = startIdx + row * 15;
                    var s = weaponNames[weaponId] + " : " + count;
                    engine.drawText(s, x, idx_new, false, 11, COLOR_BLACK, false);
                    cnt++;
                }
            }
        }


        //;weaponSurplusVBA=Grenade=2,38&Chain Gun=15,125&;weaponSurplusMoney=163.0;
    } else if (screenIndex >= 1 && screenIndex < 12) {
        var enemyData = params[screenIndex + 9];
        var data = enemyData.split("&");
        enemies[screenIndex - 1].draw();
        engine.drawText(data[0].split("=")[1], center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        var cntShots = data[1].split("=")[1];
        var payout = toMoney(data[2].split("=")[1]);
        var kills = data[3].split("=")[1];
        var hvBets = toMoney(data[4].split("=")[1]);
        var mainBets = toMoney(data[6].split("=")[1]);
        var isHV = data[5].split("=")[1] === "true";

        var idx = 1;
        if(version === 1) {
            engine.drawText("Bets: " + (isHV ? hvBets : mainBets), center_x, text_start + text_step, false, 12);
            idx++;
            engine.drawText("Payouts: " + payout, center_x, text_start + idx * text_step, false, 12);
            idx++;
            engine.drawText("Shots: " + cntShots, center_x, text_start + idx * text_step, false, 12);
            idx++;
            engine.drawText("Kills: " + kills, center_x, text_start + idx * text_step, false, 12);
            idx++;
        }else{
            engine.drawText("Bets: " + mainBets + " Payouts: " + payout, center_x, text_start + text_step, false, 11);
            idx++;
            engine.drawText("Shots: " + cntShots + " Kills: " + kills, center_x, text_start + idx * text_step, false, 11);
            idx++;
        }
        showWeapons(text_start, idx, text_step, data, 7);
    } else if (screenIndex === 12) {
        sprite_boss.draw();
        engine.drawText("Boss", center_x, text_start - 10, true, 13, COLOR_WHITE, true);
        var bossData = params[screenIndex + 9];
        var params = bossData.split("&");
        var shots_b = params[0].split("=")[1];
        var kills_b = params[1].split("=")[1];
        var betExtra = toMoney(params[2].split("=")[1]);
        if(version === 1) {
            var payoutExtra = toMoney(params[3].split("=")[1]);
            var sharedPrize = toMoney(params[4].split("=")[1]);

            var idx = 1;
            engine.drawText("Extra Bet: " + betExtra + " Payouts: " + payoutExtra, center_x,
                text_start + idx * text_step, false, 12);
            idx++;
            engine.drawText("Shared prize: " + sharedPrize, center_x, text_start + idx * text_step, false, 12);
            idx++;
            engine.drawText("shots: " + shots_b, center_x, text_start + idx * text_step, false, 12);
            idx++;
            engine.drawText("Kills: " + kills_b, center_x, text_start + idx * text_step, false, 12);
            idx++;
        }else{
            var params = bossData.split("&");
            var shots_b = params[0].split("=")[1];
            var kills_b = params[1].split("=")[1];
            var betExtra = toMoney(params[2].split("=")[1]);
            var sharedPrize = toMoney(params[4].split("=")[1]);
            var idx = 1;
            engine.drawText("Bets: " + betExtra + " Payouts: " + sharedPrize, center_x,
                text_start + idx * text_step, false, 12);
            idx++;
            engine.drawText("shots: " + shots_b + " Kills: " + kills_b, center_x,
                text_start + idx * text_step, false, 12);
            if(version == 2) idx++;
        }
        showWeapons(text_start, idx, text_step, params, 5);

    }
}
