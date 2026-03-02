<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ include file="language.jsp" %>

<script type="text/javascript" src="/js/jquery-1.3.2.min.js"></script>
<style type="text/css">

    body {
        font-family: "Trebuchet MS", "Lucida Sans Unicode", "Lucida Grande", " Lucida Sans", Arial, sans-serif;
        line-height: 1.4;
    }

    a, a:visited {
        text-decoration: none;
        color: #fff;
        font-weight: bold;
    }

    #game-enter-popup {
        top: 0px;
        text-align: center;
        position: absolute;
        margin: auto;
        bottom: 0px;
        height: 191px;
        width: 100%;
        z-index: 10;
        display: none;
    }

    #game-enter-popup-content {
        background-color: rgba(255, 255, 255, 0.4);
        border: 10px solid rgb(0, 0, 0);
        border-radius: 15px;
        display: inline-block;
        margin: auto;
        padding: 30px;
        position: relative;
    }

    .popup-game-actions {
        background: linear-gradient(to bottom, #8b9ead, #485367) repeat scroll 0 0 rgba(0, 0, 0, 0);
        border-radius: 5px;
        height: 30px;
        left: 0;
        margin: 15px auto 0;
        padding: 5px;
        position: relative;
        right: 0;
        width: 120px;
        cursor: pointer;
    }

    #game-enter-popup-content span {
        color: #fff;
        font-size: 14pt;
        font-weight: normal;
        line-height: 1.5;
    }

    #background-popup {
        background-color: rgba(0, 0, 0, 0.4);
        bottom: 0;
        height: 100%;
        left: 0;
        position: absolute;
        right: 0;
        top: 0;
        width: 100%;
        z-index: 5;
        display: none;
    }

    #game-top-message {
        background-color: rgba(0, 0, 0, 0.8);
        border-radius: 10px;
        color: #fff;
        display: block;
        font-size: 12pt;
        margin: auto;
        max-width: 800px;
        min-width: 500px;
        padding: 7px;
        position: relative;
        text-align: center;
    }

    #game-top-popup {
        position: absolute;
        top: 64px;
        width: 100%;
        display: none;
        z-index: 10;
    }

</style>

<%
    String lang = request.getParameter("lang");
%>

<div id="game-enter-popup">
    <div id="game-enter-popup-content">
        <span>
            <%= getMessage(lang, "<br>") %>
        </span>
        <div class="popup-game-actions">
            <a class="btn-popup-action" href="javascript:void(0);"><span>OK</span></a>
        </div>
    </div>
</div>
<div id="background-popup"></div>

<div id="game-top-popup">
    <div id="game-top-message">
        <span>
            <%= getMessage(lang, " ") %>
        </span>
    </div>
</div>

<script type="text/javascript">

    $("#game-enter-popup .popup-game-actions").click(function () {
        closeEnterPopup();
    });

    $("#game-top-popup").hover(
            function () {
                $(this).fadeOut(300);
                window.setTimeout(showTopPopup, 5000);
            });

    function showTopPopup() {
        $("#game-top-popup").fadeIn(300);
    };

    function showEnterPopup() {
        $('#game-enter-popup').show();
        $('#background-popup').show();
    }

    function showAlertEnterPopup() {
        alert("<%= getMessage(lang, "\\r\\n") %>");
    }

    function closeEnterPopup() {
        $('#game-enter-popup').hide();
        $('#background-popup').hide();
        showTopPopup();
    }

    function showTopPopup() {
        $('#game-top-popup').show();
    }

    function closeTopPopup() {
        $('#game-top-popup').hide();
    }

    function initPopup() {
        if ($("#game-enter-popup").parent().find("object embed").attr('wmode') == 'opaque' || $("#game-enter-popup").parent().find("object embed").attr('wmode') == 'Opaque') {
            setTimeout(showEnterPopup, 5000);
        } else {
            setTimeout(showAlertEnterPopup, 5000);
        }
    }

    initPopup();
</script>