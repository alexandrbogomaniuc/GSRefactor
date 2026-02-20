

/* --------------------------------------------------------------------------------- */
/* ---------------------------------------CENTER ITEMS------------------------------------------ */


var timer;
var isAutoSwitch = true;
$(document).ready(function () {

    var no = 1;
    autoSwitchCenterItem();

    $('.pic').hover(
        function () {
            isAutoSwitch = false;
            clearTimeout(timer);
        },
          function () {
              isAutoSwitch = true;
              autoSwitchCenterItem();
          }

        );

    $('.btnPic').click(function () {

        clearTimeout(timer);
        isAutoSwitch = false;
        SelectCenterItem($(this), isAutoSwitch);


    });

    function autoSwitchCenterItem() {

        var count = $('#center_items_rb').find('li');
        if (isAutoSwitch) {
            var li = $('ul#center_items_rb li#liBtn_' + no);
            timer = window.setTimeout(function () {
                SelectCenterItem(li, isAutoSwitch)
            }, 2000);

            if (no == count.length) {
                no = 1;
            }
            else {
                no++;
            }
        }
    }

    /*Canged by Yuval Pinkas 25/01/2011*/
    function SelectCenterItem(obj, auto) {

        var rb = $('#center_items_rb').find('li');
        for (i = 0; i < rb.length; i++) {

            if (rb[i] == obj[0]) {
                $(rb[i]).attr('class', 'checked');
                $('#center_item_' + (i + 1)).fadeIn("fast", function () {
                    for (j = 0; j < rb.length; j++) {
                        if (rb[j] != obj[0]) {
                            $(rb[j]).attr('class', 'not_checked');
                            $('#center_item_' + (j + 1)).fadeOut("fast");
                        }
                    }
                });
            }
            else {

            }
        }
        if (isAutoSwitch) {
            timer = window.setTimeout(autoSwitchCenterItem, 3000);
        }
    }

});

/* --------------------------------------------------------------------------------- */
/* ---------------------------------------END CENTER ITEMS------------------------------------------ */

/* --------------------------------------------------------------------------------- */
/* ---------------------------------------BANNER ITEMS------------------------------------------ */

function SelectBannerItem(obj, id) {
    var rb = $('#banner_items_rb').find('li');
    for (i = 0; i < rb.length; i++) {
        if (rb[i] == obj) {
            $(rb[i]).attr('class', 'checked');
            $('#banner_item_' + (i + 1)).fadeIn("slow");
        }
        else {
            $(rb[i]).attr('class', 'not_checked');
            $('#banner_item_' + (i + 1)).hide();
        }
    }
}



/* --------------------------------------------------------------------------------- */
/* ---------------------------------------END CENTER ITEMS------------------------------------------ */
