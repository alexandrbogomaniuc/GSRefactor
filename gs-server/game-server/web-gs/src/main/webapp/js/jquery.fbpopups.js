(function ($) {
    var popups = {
        friends_invite:{
            id:'#w1',
            initial_delay:1000,
            start_delay_min:5000,
            start_delay_max:5000
        },
        like:{
            id:'#w2',
            initial_delay:1000,
            start_delay_min:5000,
            start_delay_max:5000
        },
        specialDeposit:{
            id:'#w2',
            initial_delay:1000,
            start_delay_min:5000,
            start_delay_max:5000
        }
    };
    var timeouts = {};
    var missed_popups = [];


    function innerShowWindow(key) {
        $(wins[key].id).show();
    }

    $.extend({
        popups:{
            show:function (key) {
                if (true) {
                    innerShowWindow(key);
                } else {
                    missed_popups.push(key);
                }
            },
            hide:function (key) {
                var cP = popups[key];
                $(cP.id).hide();
                var f = function () {
                    innerShowWindow(key)
                };
                setTimeout(f, Math.floor(Math.random() * (cP.start_delay_max - cP.start_delay_min) + cP.start_delay_min));
            }
        }
    });
})(jQuery);
