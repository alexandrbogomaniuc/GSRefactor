var Game = new Array();
var featuredLaunchersCount = 8;
var commonLaunchersCount = 12;

// Slot Games //

Game['treasureroom'] = {id:'158', name:'Treasure Room'};
Game['threewishes'] = {id:'177', name:'Three Wishes'};
Game['slotfather'] = {id:'179', name:'The Slot Father'};
Game['madscientist'] = {id:'159', name:'Mad Scientist'};
Game['heist'] = {id:'180', name:'Heist'};
Game['glamlife'] = {id:'173', name:'The GlamLife'};
Game['gladiator'] = {id:'178', name:'Gladiator'};
Game['aztectreasure3'] = {id:'190', name:'Aztec Treasure 3'};
Game['diamondjackpot'] = {id:'32', name:'Diamond Jackpot'};
Game['diamonddreams'] = {id:'21', name:'Diamond Dreams'};
Game['captaincash'] = {id:'18', name:'Captain Cash'};
Game['dashforcash'] = {id:'73', name:'Dash for Cash'};
Game['headsortails'] = {id:'61', name:'Heads or Tails'};
Game['mermaidspearl'] = {id:'20', name:'Mermaids Pearl'};
Game['lucky7'] = {id:'2', name:'Lucky 7'};
Game['triplecrown'] = {id:'3', name:'Triple Crown'};
Game['chasethecheese'] = {id:'88', name:'Chase The Cheese'};
Game['hiddenloot'] = {id:'80', name:'Hidden Loot'};
Game['ghoulsgold'] = {id:'52', name:'Ghouls Gold'};
Game['thebighit'] = {id:'76', name:'The Big Hit'};
Game['jackpotjamba'] = {id:'41', name:'Jackpot Jamba'};
Game['outofthisworld'] = {id:'47', name:'Out Of This World'};
Game['monkeymoney'] = {id:'33', name:'Monkey Money'};
Game['backintime'] = {id:'104', name:'Back In Time'};
Game['reeloutlaws'] = {id:'133', name:'Reel Outlaws'};
Game['pharaohking'] = {id:'134', name:'Pharaoh King'};
Game['wizardscastle'] = {id:'136', name:'Wizards Castle'};
Game['aztectreasure'] = {id:'148', name:'Aztec Treasure'};
Game['invaders'] = {id:'155', name:'Invaders'};
Game['theghouls'] = {id:'156', name:'The Ghouls'};

// Video Poker //

Game['allamerican'] = {id:'46', name:'All American'};
Game['deucesdoubleup'] = {id:'29', name:'Deuces Wild'};
Game['fivedrawpoker'] = {id:'23', name:'Five Hand Poker'};
Game['jacksdoubleup'] = {id:'31', name:'Jacks or Better'};
Game['jokersdoubleup'] = {id:'30', name:'Joker Poker'};
Game['multihandpoker'] = {id:'99', name:'Multihand Poker'};
Game['pyramidpoker'] = {id:'68', name:'Pyramid Poker'};

// Other, TABLE //

Game['baccarat'] = {id:'13', name:'Baccarat'};
Game['burnblackjack'] = {id:'64', name:'21 Burn Blackjack'};
Game['caribeanpoker'] = {id:'12', name:'Caribbean Poker'};
Game['casinowar'] = {id:'48', name:'Casino War'};
Game['craps'] = {id:'10', name:'Craps'};
Game['drawhilo'] = {id:'74', name:'Draw High Low'};
Game['hamburgroulette'] = {id:'79', name:'European Roulette'};
Game['modifiedbj'] = {id:'63', name:'S-Deck Blackjack 1-100'};
Game['modifiedbjhilimit'] = {id:'124', name:'S-Deck Blackjack 25-300'};
Game['multihandblackjack'] = {id:'44', name:'Super 7 Blackjack 1-100'};
Game['multihandblackjackhilimit'] = {id:'125', name:'Super 7 Blackjack 50-500'};
Game['multihandblackjackmiddlelimit'] = {id:'132', name:'Super 7 Blackjack Middle'};
Game['paigow'] = {id:'9', name:'Pai Gow Poker'};
Game['pirate21'] = {id:'82', name:'Pirate21'};
Game['pontoon'] = {id:'81', name:'Pontoon'};
Game['reddog'] = {id:'94', name:'Red Dog'};
Game['ridethempoker'] = {id:'43', name:'Ridem Poker'};
Game['roulette'] = {id:'5', name:'American Roulette'};
Game['threecardpoker'] = {id:'105', name:'Three Card Poker'};

// Other, SOFT GAMES //

Game['onaroll'] = {id:'75', name:'On a Roll'};
Game['ontheball'] = {id:'84', name:'On the Ball'};
Game['predictor'] = {id:'91', name:'Predictor'};
Game['scratcherz'] = {id:'143', name:'Skratcherz'};

// Other, KENO //
Game['instantkeno'] = {id:'16', name:'Instant Keno'};
Game['keno'] = {id:'8	', name:'Traditional Keno'};
Game['krazykeno'] = {id:'15', name:'Krazy Keno'};

// Other, RACEBOOK //
Game['virtualracebook'] = {id:'135', name:'Virtual Racebook'};

var gameMenu = new Array();
/*
gameMenu = {
	featured:[	'aztectreasure2','gladiator','glamlife','heist',
			'madscientist','slotfather','threewishes','treasureroom'
	],
	videoslots:[
		'treasureroom','threewishes','slotfather','madscientist','heist','glamlife', 
		'gladiator','aztectreasure2',
		'diamondjackpot','diamonddreams','captaincash','dashforcash','headsortails',
		'mermaidspearl','lucky7','triplecrown','chasethecheese','hiddenloot','ghoulsgold',
		'thebighit','jackpotjamba','outofthisworld','monkeymoney','backintime',
		'reeloutlaws','pharaohking','wizardscastle','aztectreasure','invaders','theghouls'
	],
	videopoker:[
		'allamerican','deucesdoubleup','jacksdoubleup','jokersdoubleup','fivedrawpoker','multihandpoker','pyramidpoker'
	],
	other:[	
		'baccarat','burnblackjack','caribeanpoker','casinowar','craps','drawhilo','hamburgroulette',
		'modifiedbj','modifiedbjhilimit','multihandblackjack','multihandblackjackhilimit',
		'multihandblackjackmiddlelimit','paigow','pirate21','pontoon','reddog','ridethempoker','roulette',
		'threecardpoker',
		'virtualracebook',
		'onaroll','ontheball','predictor','scratcherz',
		'krazykeno','instantkeno','keno'
	]
};
*/

gameMenu = {
	featured:[
		'gladiator','threewishes','heist','aztectreasure3','glamlife','madscientist','treasureroom'
	],
	videoslots:[
	            'gladiator','threewishes','heist','aztectreasure3','glamlife',
	            'madscientist','treasureroom',
	            'outofthisworld','monkeymoney','ghoulsgold',
	            'diamonddreams','captaincash'
	],
	videopoker:[
		'deucesdoubleup','jacksdoubleup','jokersdoubleup',
		'multihandpoker','pyramidpoker'
	],
	other:[
        	'baccarat','burnblackjack','caribeanpoker','casinowar','craps',
	        'drawhilo','hamburgroulette','modifiedbj','multihandblackjack',       
	        'paigow','pirate21','pontoon','reddog','ridethempoker','roulette',
	        'threecardpoker'
	]
};

function buildFeaturedGameLaunchers(node, gameIds) {
	$("#" + node).empty();

	for (var i = 0; i < gameIds.length; i++) {
	
	if (i > featuredLaunchersCount - 1) break;

		var gameName = [{
			tagName: "div",
			className : "featured_game_name",
			id : "featured_game_name_" + i,
			innerHTML : Game[gameIds[i]]["name"].toUpperCase()
		}];

		var gameIcon = [{
			tagName: "div",
			className : "featured_game_icon",
			id : "featured_game_icon_" + i,
			style : "background : url(images/game_icons/featured/" + gameIds[i] +".jpg) left no-repeat;",
			innerHTML: "<a id='featured_game_link" + i + "' class='featured_game_link'"
			+ " href='#' onClick=\"launchGame('" + gameIds[i] + "')\"></a>"
		}];

		var playNow = [{
			tagName: "div",
			innerHTML: "<a id='featured_game_playnow" + i + "' class='featured_game_playnow'" 
			+ " onMouseDown='playNowPressed(" + i +")' onMouseUp='playNowUnpressed(" + i +")' href='#'"
			+ " onClick=\"launchGame('" + gameIds[i] + "')\"></a>"
		}];

		var container = [{
			tagName : "div",
			className : "featured_container",
			id : "featured_container_" + i
		}];

		$("#" + node).appendDom(container);

		$("#featured_container_" + i).appendDom(gameIcon);
		$("#featured_container_" + i).appendDom(gameName);
		$("#featured_container_" + i).appendDom(playNow);

		$("#featured_game_icon_" + i).css("background", "url(images/game_icons/featured/" + gameIds[i] +".jpg) left no-repeat;");
      	}
}

function buildCommonGameLaunchers(node, gameIds, page) {
	var startIndex = (page - 1) * commonLaunchersCount;
	var endIndex = startIndex + commonLaunchersCount - 1;

	if (endIndex > (gameIds.length - 1)) {
		endIndex = gameIds.length - 1;
	}

	$("#" + node).empty();

	for (var i = startIndex; i <= endIndex; i++) {

		var gameName = [{
			tagName: "div",
			className : "common_game_name",
			id : "common_game_name_" + i,
			innerHTML : Game[gameIds[i]]["name"].toUpperCase()
		}];

		var gameIcon = [{
			tagName: "div",
			className : "common_game_icon",
			id : "common_game_icon_" + i,
			style : "background : url(images/game_icons/common/" + gameIds[i] +".jpg) left no-repeat;",
			innerHTML : "<a id='common_game_link" + i + "' class='common_game_link'"
			+ " href='#' onClick=\"launchGame('" + gameIds[i] + "')\"></a>"
		}];

		var container = [{
			tagName : "div",
			className : "common_container",
			childNodes : [],
			id : "common_container_" + i
		}];

		$("#" + node).appendDom(container);

		$("#common_container_" + i).appendDom(gameIcon);
		$("#common_container_" + i).appendDom(gameName);

		$("#common_game_icon_" + i).css("background", "url(images/game_icons/common/" + gameIds[i] +".jpg) left no-repeat;");
      	}
}
