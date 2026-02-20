 var commonNavigationPageNumber = 1;
 var commonNavigationPagesCount = 0;
 var navigationGameCategory = "";
 var loggedInAccountId = "";
 var loggedInLanguage = "";
 var isReal = true;
 
 function setClicked(element, image) {
	var obj = document.getElementById(element);
	obj.style["background"]="url(images/" + image + ") left no-repeat";
 }   

 function setSelectionUnclicked() {
	hideElement('btn_featured_pressed');
	hideElement('btn_videoslots_pressed');
	hideElement('btn_videopoker_pressed');
	hideElement('btn_other_pressed');
 }   

 function showGameCategory(category) {
	navigationGameCategory = category;
	setSelectionUnclicked();
        showElement("btn_" + category + "_pressed")

	if (category == "featured") {
		hideElement("common_layer");
		showElement("featured_layer");
		buildFeaturedLaunchers();
	} else {
		hideElement("featured_layer");
		showElement("common_layer");
		showCommonGameCategory(category);
	}
 }

 function showCommonGameCategory(category) {
	commonNavigationPageNumber = 1;
	calculateCommonNavigationPagesCount();
	moreUnpressed();
	lessUnpressed();
	buildCommonLaunchers();
 }

 function hideCommonPageNavigation() {
	hideElement('btn_less_disabled');
	hideElement('btn_more_disabled');
 }   

 function hideElement(element) {
	var obj = document.getElementById(element);
	obj.style["display"]="none";
 }

 function showElement(element) {
	var obj = document.getElementById(element);
	obj.style["display"]="block";
 }
 
 function calculateCommonNavigationPagesCount() {
	if (gameMenu[navigationGameCategory].length % commonLaunchersCount != 0) {
		commonNavigationPagesCount = ((gameMenu[navigationGameCategory].length 
		- gameMenu[navigationGameCategory].length % commonLaunchersCount) 
		/ commonLaunchersCount) + 1;
	} else {
		commonNavigationPagesCount = gameMenu[navigationGameCategory].length / commonLaunchersCount;
	}
 }

 function rebuildCommonNavigation() {
	if (navigationGameCategory != "") {
		hideCommonPageNavigation();

		if (commonNavigationPageNumber == 1) {
			showElement("btn_less_disabled");
		} 
		if (commonNavigationPageNumber == commonNavigationPagesCount) {
			showElement("btn_more_disabled");
		} 

	$("#pager_digit1").html(commonNavigationPageNumber);
	$("#pager_digit2").html(commonNavigationPagesCount);
	}
 }

 function nextPage() {
	$("#btn_more_rollover").removeClass("more_rollover").addClass("more_rollover_active");
 }

 function moreUnpressed() {
	$("#btn_more_rollover").removeClass("more_rollover_active").addClass("more_rollover");
	commonNavigationPageNumber++;
	rebuildCommonNavigation();
	buildCommonLaunchers();
 }

 function prevPage() {
	$("#btn_less_rollover").removeClass("less_rollover").addClass("less_rollover_active");
 }

 function lessUnpressed() {
	$("#btn_less_rollover").removeClass("less_rollover_active").addClass("less_rollover");
	commonNavigationPageNumber--;
	rebuildCommonNavigation();
	buildCommonLaunchers();
 }

 function buildCommonLaunchers() {
	rebuildCommonNavigation();
	buildCommonGameLaunchers("common_luncher", gameMenu[navigationGameCategory], commonNavigationPageNumber);
 }

 function buildFeaturedLaunchers() {
	buildFeaturedGameLaunchers("featured_layer", gameMenu[navigationGameCategory]);
 }

 function playNowPressed(number) {
	$("#featured_game_playnow" + number).removeClass("featured_game_playnow").addClass("featured_game_playnow_active");
 }

 function playNowUnpressed(number) {
	$("#featured_game_playnow" + number).removeClass("featured_game_playnow_active").addClass("featured_game_playnow");
 }

 function launchGame(gamename) {
	var gamelink = "/entercasino.do?";
	gamelink += "accountId=" + loggedInAccountId;
	gamelink += "&gameId=" + Game[gamename]["id"];
	gamelink += "&mode=" + (isReal ? "REAL" : "FREE");
	gamelink += "&lang=" + loggedInLanguage;
	openWnd(gamelink, "800", "600", "Game", "yes");
 }

 function setupPageElements() {

    var h;
    var w;

	if(window.innerHeight) {
		h=window.innerHeight;
		w=window.innerWidth;
	} else { 
		h=document.documentElement.clientHeight;
		w=document.documentElement.clientWidth;
	}

		$("#header_background").css("width", w + "px");
		$("#footer_background").css("width", w + "px");

		var footer_background_top = h - 52;
		if (footer_background_top < 548) footer_background_top = 548;
		$("#footer_background").css("top", footer_background_top + "px");

		var global_left = (w - 800) / 2;
		if (global_left < 0) global_left = 0;

		$("#header").css("left", global_left + "px");
		$("#login_form").css("left", global_left + "px");

		var common_layer_top = (h - 465) / 2;
		if (common_layer_top < 80) common_layer_top = 80;
		$("#common_layer").css("top", common_layer_top + "px");

		$("#common_layer").css("left", global_left + "px");

		var featured_layer_top = (h - 450) / 2;
		if (featured_layer_top < 85) featured_layer_top = 85;
		$("#featured_layer").css("top", featured_layer_top + "px");

		var featured_layer_left = (w - 796) / 2;
		if (featured_layer_left < 0) featured_layer_left = 0;
		$("#featured_layer").css("left", featured_layer_left + "px");
		$("#footer").css("top", footer_background_top + "px");
		$("#footer").css("left", global_left + "px");

	window.onresize=setupPageElements;
 }

function startSessionExpireMonitor(accountId, lobbyUrl){	
	var servletUrl = "/AJAXSessionMonitor.servlet?aid=" + accountId;
	
	var method = "POST";
	var agent = navigator.userAgent;	
	if (agent.indexOf("Firefox") != -1) method = "GET";
	
	$.ajax({
        type: method,
        url: servletUrl,
        async:true,
        success: function(msg) {						
            looper(accountId, lobbyUrl, msg);
        },
        error:function(msg) {
        	refreshPage();
        }
    });
}

function looper(accountId, lobbyUrl, msg){
	var delay = parseInt(msg);
	if (delay < 0){
		refreshPage();
	} else{
		var stub = function(){ startSessionExpireMonitor(accountId, lobbyUrl); };		
		setTimeout(stub, delay);				
	}
}

function refreshPage(){
	window.location.href = window.location.href;
	//location.reload(true);
}

function getDiv(name){
	return document.getElementById(name);
}

function writeToDiv(div, message, isAppend){
	if (isAppend == "true"){
		div.appendChild(document.createTextNode(message));
		return;
	}
	div.innerHTML = message;
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}