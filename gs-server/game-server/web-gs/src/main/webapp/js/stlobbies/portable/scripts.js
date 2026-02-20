"use strict";

$(window).load(function () {
    if ($(window).width() < 480) {
        document.getElementById('viewport').setAttribute('content', 'width=480');
    }
});

$(document).ready(function () {

    var modalOpened = false;
    var scrollTip = $('#scroll-down');
    var wrapper = $('.wrapper');

    function checkTip() {
        if ($(window).scrollTop() + $(window).height() + 20 > $(document).innerHeight()) {
            scrollTip.hide();
        } else {
            scrollTip.show();
        }
    }

    function resizeModal() {
        $('.modalDialog').css('height', function() {
            return $(window).height();
        });
    }

    checkTip();
    $(window).on('scroll', checkTip)
        .resize(checkTip)
        .resize(resizeModal);

    $('.menu-toggle').on('click', function () {
        $('body').toggleClass('menu-expanded');
    });

    var menu = $('#menu');
    menu.find('.item').on('click', function () {
        menu.find('.selected').removeClass('selected');
        $(this).parent().addClass('selected');

        $('body').removeClass('menu-expanded');

        var containerId = $(this).data('games');
        $('.games').removeClass('active');
        $(containerId).addClass('active');

        checkTip();
    });

    function closeModal(e) {
        if (modalOpened && !e.isDefaultPrevented()) {
            modalOpened = false;

            var modal = $('#modal');
            modal.fadeTo("slow", 0, function() {
                modal.hide();
            });
            $('html').removeClass('modal-opened');

            disableControls();

            $("#tp_success").fadeOut("slow");
            $("#tp_fail").fadeOut("slow");
        }
    }

    $('#transfer').on('click', function () {
        if (!modalOpened) {
            modalOpened = true;

            $('html').addClass('modal-opened');
            enableControls();
            $('#modal').show().fadeTo("slow", 1)
                .on('click', closeModal)
                .on('click', 'div', function(e) {
                    e.preventDefault();
                });
            $('#close').on('click', closeModal);
        }
    });

});
