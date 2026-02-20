var cf = new ContentFlow('contentFlow',{scrollWheelSpeed:0});
$(function() {
	var currentGames = $("#gamesWrap ul.selected").attr("id");
	$("#gamesNav a").click(function(){
		var l = $(this).attr("href").substr(1);
		if(!l)
			return false;
		
		$("#gamesNav li").removeClass("selected");
		$(this).parents("li").addClass("selected");
		
		$("#"+currentGames).hide().removeClass("selected");
		/*$("#gamesWrap").css('height',$("#"+l).height()+"px");*/
		$("#"+l).show(300).addClass("selected");
		currentGames=l;
		return false;
	});
});