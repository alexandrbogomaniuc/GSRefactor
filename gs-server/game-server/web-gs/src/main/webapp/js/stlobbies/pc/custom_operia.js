var topGamesDelay = 5000;
$(function() {
    //Vertical Scroll
    var s = $('#list').jScrollPane({showArrows: true});
    var api = s.data('jsp');

    //Horizontal Scroll
    $("#headerIn").width($("#headerIn a").size() * 751);
    var scrollPane = $("#headerSliderWrap"),
        scrollContent = $("#headerIn");

    var scrollbar = $("#headerSlider").slider({
        slide: function(event, ui) {
            if (scrollContent.width() > scrollPane.width()) {
                scrollContent.css("margin-left", "-" + ui.value + "px");
                /*
                 alert('hide');
                 */
            } else {
                scrollContent.css("margin-left", 0);
                /*
                 alert('show');
                 */
            }
            if (ui.value < 100) {
//                $("#headerNewBadge").show();
            } else {
                $("#headerNewBadge").hide();
            }
        },
        step: 751,
        min: 1,
        max: ($("#headerIn a").size() - 1) * 751
    });

    setTimeout(scrollTopGames, topGamesDelay);

    //Game Tabs
    $("#listCategories li a").click(function() {
        var curID = $(this).parent().attr("id");

        if (curID != 'catLD') {
            $("#listCategories li").removeClass("active");
            $(this).parent().addClass("active");

            $("#list ul").removeClass("active");
            $("#list ul#" + curID + "_list").addClass("active");

            api.reinitialise();
        }
        return false;
    });

    $(window).resize(function() {
        api.reinitialise();
    });
});

function scrollTopGames() {
    var headerSlider = $("#headerSlider");
    var slideValue = headerSlider.slider("value");
    var scrollContent = $("#headerIn");
    if (slideValue < ($("#headerIn a").size() - 1) * 751 - 1) {
        slideValue = slideValue + 751;
        headerSlider.slider("value", slideValue);
        scrollContent.css("margin-left", "-" + slideValue + "px");
        $("#headerNewBadge").hide();
    } else {
        slideValue = 0;
        headerSlider.slider("value", slideValue);
        scrollContent.css("margin-left", 0);
//        $("#headerNewBadge").show();
    }
    setTimeout(scrollTopGames, topGamesDelay);
}