$(function() {

    var s = $('#list').jScrollPane({showArrows: true});
    var api = s.data('jsp');

    //Game Tabs
    $("#listCategories li").click(function() {
        var curID = $(this).attr("id");

        $("#listCategories li").removeClass("active");
        $(this).addClass("active");

        $("#list div").removeClass("active");
        $("#" + curID + "_list").addClass("active");

        api.reinitialise();
        return false;
    });
});
