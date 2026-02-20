function showPopup(popup){
	$('#flashblock').show();
	$('#popupwrapper').show();
	popup.show();
}

var achievements = ['', 'Rock Star Big Win!', 'High Roller Big Win!', 'Top Dog Big Win!', 'Total Wins Achievement!', 'Gross Earnings Achievement!', 'Number of Rounds Achievement!', 'Friend Invite Achievement!', 'Time in App Achievement!', 'Screenshot Post Achievement!'];
var tbl =[[], ['', '5 Big Wins of 1000 Coins or More', '15 Big Wins of 1000 Coins or More', '30 Big Wins of 1000 Coins or More', '50 Big Wins of 1000 Coins or More', '100 Big Wins of 1000 Coins or More'], ['', '5 Big Wins of 2500 Coins or More', '15 Big Wins of 2500 Coins or More', '30 Big Wins of 2500 Coins or More', '50 Big Wins of 2500 Coins or More', '100 Big Wins of 2500 Coins or More'], ['', '5 Big Wins of 5000 Coins or More', '15 Big Wins of 5000 Coins or More', '30 Big Wins of 5000 Coins or More', '50 Big Wins of 5000 Coins or More', '100 Big Wins of 5000 Coins or More'], ['', '50 Total Wins', '150 Total Wins', '300 Total Wins', '500 Total Wins', '1000 Total Wins', '10000 Total Wins'], ['', '10000 Gross Earnings', '50000 Gross Earnings', '100000 Gross Earnings', '500000 Gross Earnings', '1000000 Gross Earnings', '5000000 Gross Earnings'], ['', '1000 Number Of Rounds', '10000 Number Of Rounds', '50000 Number Of Rounds', '100000 Number Of Rounds', '500000 Number Of Rounds', '1000000 Number Of Rounds'], ['', '5 Friend Invite', '15 Friend Invite', '25 Friend Invite', '40 Friend Invite'], ['', '1 Hours Spent', '8 Hours Spent', '24 Hours Spent', '48 Hours Spent', '72 Hours Spent', '96 Hours Spent']];

function openAchievementMessage(achievementID, level, bonus){
    var achievementImg = ['', '/images/facebook/achievements/1.png', '/images/facebook/achievements/2.png', '/images/facebook/achievements/3.png', '/images/facebook/achievements/4.png', '/images/facebook/achievements/5.png', '/images/facebook/achievements/6.png', '/images/facebook/achievements/7.png', '/images/facebook/achievements/8.png', '/images/facebook/achievements/9.png'];
    var levels = ['', '/images/facebook/levels/1.png', '/images/facebook/levels/2.png', '/images/facebook/levels/3.png', '/images/facebook/levels/4.png', '/images/facebook/levels/5.png', '/images/facebook/levels/6.png'];
    var titles = ['', '/images/facebook/achievements/1-title.png', '/images/facebook/achievements/2-title.png', '/images/facebook/achievements/3-title.png', '/images/facebook/achievements/4-title.png', '/images/facebook/achievements/5-title.png', '/images/facebook/achievements/6-title.png', '/images/facebook/achievements/7-title.png', '/images/facebook/achievements/8-title.png', '/images/facebook/achievements/9-title.png'];
    var popup = $('#achievements');
    $('#achievement_image').css('background-image', 'url(' + achievementImg[achievementID] + ')');
    $('#level_image').css('background-image', 'url(' + levels[level] + ')');
    $('#bonus_msg').html('<span>' + bonus + ' Coins</span> <br>(Achievement Unlocked!)');
    $('#achievement_title').attr('src', titles[achievementID]).attr('alt', achievements[achievementID]);
    $('#achievement_description').text(tbl[achievementID][level]);
    $('.brag', popup).data({'type':'achievement', 'achievementID':achievementID, 'level':level, 'bonus':bonus});
	showPopup(popup);
}

function openLevelMessage(level, unlocked_game_url, unlocked_video_url, bonus){
    var popup = $('#levelup');
   	var game = $('#new_game');
   	var video = $('#new_cinematic');
    var new_level = $('#new_level');
    var pos;
    $('#levelup_description').html('You have Reached Level ' + level + ((unlocked_game_url || unlocked_video_url) ? '! You&rsquo;ve Unlocked:' : '!'));
    if (unlocked_game_url) {
        if (unlocked_video_url) {
            pos = (popup.width() - game.width() - video.width() - 40) / 2;
        } else {
            pos = (popup.width() - game.width()) / 2;
        }
        game.css('left', pos + 'px');
        unlocked_game_url = '/flash/fb/' + unlocked_game_url;
        game.empty().append($('<img >').attr('src', unlocked_game_url)).show();
    } else {
        game.hide();
    }
    if (unlocked_video_url) {
        if (unlocked_game_url) {
            pos = (popup.width() - game.width() - video.width() - 40) / 2;
        } else {
            pos = (popup.width() - video.width()) / 2;
        }
        video.css('right', pos + 'px');
        unlocked_video_url = '/flash/fb/' + unlocked_video_url;
        video.empty().append($('<img >').attr('src', unlocked_video_url)).show();
    } else {
        video.hide();
    }
    if (!unlocked_game_url && !unlocked_video_url) {
        pos = (popup.width() - new_level.width()) / 2;
        new_level.css('left', pos + 'px');
        new_level.empty().append($('<img >').attr('src', '/images/facebook/brag/level/' + level + '.png')).show();
    } else {
        new_level.hide();
    }
    $('#levelup_bonus_msg').html(bonus + ' Coins<br />(Level Up Bonus!)')
    $('.brag', popup).data({'type':'level', 'bonus':bonus, 'level':level, 'gameUrl':unlocked_game_url, 'videoUrl':unlocked_video_url});
    showPopup(popup);
}

function openBonusMessage(bonus, newBalance) {
    var popup = $('#bonus');
    $('#prev_balance').text(newBalance - bonus);
    $('#bonus_amount').text(bonus);
    $('#new_balance').text(newBalance);
    $('.brag', popup).data({'type':'bonus', 'bonus':bonus, 'balance':newBalance});
    showPopup(popup);
}

var posImages = ['', '/images/facebook/leaderboard_places/1.png', '/images/facebook/leaderboard_places/2.png', '/images/facebook/leaderboard_places/3.png'];
var posNames = ['','1st', '2nd', '3rd'];

function openAnnouncementMessage(lbid, mode, position) {
    var popup = $('#leaderboard');
    var dict = {'BALANCE':'Balance', 'ROUNDS':'Rounds', 'WAGER':'Wager', 'ACHIEVEMENTS':'Achievements', 'OVERALL':'Overall', 'FRIENDS':'Friends'};
    $('#leaderboard_position').attr('src', posImages[position]);
    $('#leaderboard_description').html('You have achieved ' + posNames[position] + ' Place on the<br />' + dict[mode] + ' Leader Board for ' + dict[lbid]);
    $('.brag', popup).data({'type':'leaderboard', 'lbid':lbid, 'mode':mode, 'position':position});
    showPopup(popup);
}

function isPopupVisible(){
    return $('#flashblock').is(":visible");
}

function openInviteMessage(users){
    var i;
    var item;
    $('#frend_list').empty();
    for (i = 0; i < users.length; i++) {
        if (users[i] && (users[i] != null)) {
            item = $('<div class="friend_item"/>').append($('<div class="username"/>').text(users[i].name)).data(users[i]);
            if (users[i].picture) {
                item.append($('<img />').attr('src', users[i].picture))
            }
            item.on('click', function () {
                $(this).toggleClass('selected')
            });
            $('#frend_list').append(item);
        }
    }
    showPopup($('#friends'));
}

function prepareBragMessage(object) {
    if (!object) {
        return null;
    }
    var dict = {'BALANCE':'Balance', 'ROUNDS':'Rounds', 'WAGER':'Wager', 'ACHIEVEMENTS':'Achievements', 'OVERALL':'Overall', 'FRIENDS':'Friends'};
    var type = object['type'];
    var picture;
    var name;
    var caption;
    var description;
    if (type == 'achievement') {
        var achievementId = object['achievementID'];
        var achievementLevel = object['level'];
        var achievementBonus = object['bonus'];
        name = achievements[achievementId];
        if (achievementId == 2 || achievementId == 3) {
            picture = '/images/facebook/brag/achievement/1_' + achievementLevel + '.png';
        } else {
            picture = '/images/facebook/brag/achievement/' + achievementId + '_' + achievementLevel + '.png';
        }
        caption = tbl[achievementId][achievementLevel]+'.';
        description = 'Awarded ' + achievementBonus + ' Coins.';
    } else if (type == 'level') {
        var levelBonus = object['bonus'];
        var levelLevel = object['level'];
        var levelGameUrl = object['gameUrl'];
        var levelVideoUrl = object['videoUrl'];
        name = 'Reached Level ' + levelLevel;
        picture = '/images/facebook/brag/level/' + levelLevel + '.png';
        caption = (levelGameUrl ? ' New Game' : '') + (levelVideoUrl ? ' New Video' : '');
        caption = 'Congratulations! You have Reached Level ' + levelLevel + '! ' + ((caption == '') ? '!' : ('! You&rsquo;ve Unlocked ' + caption + '!'));
        description = 'Awarded ' + levelBonus + ' Coins.';
    } else if (type == 'bonus') {
        var bonusBonus = object['bonus'];
        var bonusBalance = object['balance'];
        name = 'Bonus Awarded';
        //picture = '';
        picture = '/images/facebook/brag/time_bonus.png';
        caption = 'Congratulations! Bonus Awarded.';
        description = 'Awarded ' + bonusBonus + ' Coins.';
    } else if (type == 'leaderboard') {
        var lbLbid = object['lbid'];
        var lbMode = object['mode'];
        var lbPosition = object['position'];
        name = 'Announcement';
        picture = '/images/facebook/brag/leaderboard/' + lbPosition + '.png';
        caption = 'Leader Board Update.';
        description = 'You have achieved ' + posNames[lbPosition] + ' Place on the ' + dict[lbMode] + ' Leader Board for ' + dict[lbLbid];
    } else {
        return null;
    }
    return {'picture':picture, 'name':name, 'caption':caption, 'description':description};
}