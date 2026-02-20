<link rel="stylesheet" href="/gossip/css/jquery.fancybox.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="/gossip/css/gossip.css">
<script src="/gossip/js/html5shiv.js"></script>
<script src="/gossip/js/jquery-1.10.1.min.js"></script>
<script src="/gossip/js/plugins.js"></script>

<script>

    function showPopup($, window, document) {
        (function ($, window, document, undefined) {
            if ($('.game-popup').length > 0) {
                $.fancybox.open([
                    {
                        href: ".game-popup",
                        type: 'inline',
                        width: 500,
                        height: 170,
                        maxWidth: 500,
                        maxHeight: 170,
                        autoSize: false,
                        'beforeClose': function () {
                            initGossipPopup();
                        }
                    }
                ], {
                    padding: 0
                });
            }
        })(jQuery, window, document);
    }

    function showGossipPopup() {
        showPopup(jQuery, window, document);
    }

    function initGossipPopup() {
        setTimeout(showGossipPopup, 60000);
    }

    function JoinNowGossip() {
        var newLocation = 'https://www.gossipslots.eu/secure_signup.asp';
        if (self == top) {
            window.location = newLocation;
            window.name = 'rake_join_now';
            window.moveTo(0, 0);
            window.resizeTo(screen.availWidth, screen.availHeight);
            window.focus();
        } else {
            var newWindow = window.open(newLocation, "rake_join_now");
            newWindow.moveTo(0, 0);
            newWindow.resizeTo(screen.availWidth, screen.availHeight);
            newWindow.focus();
        }
        return false;
    }

    initGossipPopup();
    <%if (request.getHeader("User-Agent").contains("MSIE")) {%>$('body').height($(document).height() - 5);
    <%}%>

</script>

<div id="gamepopupmarkup" class="game-popup">
    <a title="Close" class="fancybox-close" href="javascript:void(0);" onclick="javascript:$.fancybox.close();">&nbsp;</a>
    <img class="popup-logo" src="/gossip/img/gs-logo.png" width="320" alt="Gossip Slots"/>
    <div class="popup-game-actions">
        <a class="btn-popup-action" href="javascript:void(0);" onclick="javascript:$.fancybox.close();"><span>Play for Fun</span></a>
        <span class="popup-game-actions-or">or</span>
        <a class="btn-popup-action" href="javascript:void(0);"
           onclick="javascript:JoinNowGossip();"><span>Play for Real</span></a>
    </div>
</div>
