<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ page import="org.apache.commons.lang.LocaleUtils" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String lang = (String) session.getAttribute("lang");

    if (lang != null) {
        try {
            Locale locale = LocaleUtils.toLocale(lang);
            session.setAttribute(Globals.LOCALE_KEY, locale);
        } catch (IllegalArgumentException e) {
        }
    }
    String incompleteRoundUrl = decodeUrl(request.getParameter("incompleteRoundUrl"));
%>
<%!
    String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- Global site tag (gtag.js) - Google Analytics -->
    <script async src="https://www.googletagmanager.com/gtag/js?id=UA-100935592-2"></script>
    <script>
        window.dataLayer = window.dataLayer || [];

        function gtag() {
            dataLayer.push(arguments);
        }

        gtag('js', new Date());

        gtag('config', 'UA-100935592-2');
    </script>

    <meta charset="utf-8"/>
    <meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible"/>
    <title>Thank You</title>
    <meta content="The new era in gaming!" name="description"/>
    <meta content="width=device-width" name="viewport"/>
    <link href="favicon.ico" rel="shortcut icon"/>

    <link href="https://fonts.googleapis.com/css2?family=Barlow:wght@400;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans+Condensed:300" rel="stylesheet">

    <style type="text/css">
        html, body {
            width: 100%;
            height: 100%;
            min-height: 100%;
            margin: 0;
            padding: 0;
        }

        body {
            background-color: black;
            font-size: 16px;
        }

        .container {
            position: relative;
            width: 100%;
            height: 100%;
            background-image: url(/images/error-pages/mq/sectorx_bg.jpg);
            background-size: cover;
            background-position: center top;
            background-repeat: no-repeat;
        }

        .content {
            position: absolute;
            height: 70%;
            width: 100%;
            left: 0;
            bottom: 0;
        }

        .brand {
            text-align: center;
        }

        .brand img {
            display: block;
            margin-left: auto;
            margin-right: auto;
            max-width: 700px;
            width: 70%;
        }

        .message {
            text-align: center;
            padding: 20px;
            width: 100%;
            display: block;
        }

        .message_fatal {
            max-width: 800px;
            background-color: rgba(0, 0, 0, 0.5);
            margin: 30px auto;
        }

        .message_fatal h1 {
            font-family: "Open Sans", arial, sans-serif;
            color: #ffffff;
            margin-top: 20px;
            font-size: 2em;
        }

        .message_incomplete {
            max-width: 900px;
            background-color: rgba(0, 0, 0, 0.8);
            border-radius: 20px;
            margin: 60px auto;
        }

        .message_incomplete h1 {
            font-family: 'Barlow', arial, sans-serif;
            font-weight: 400;
            color: #ffffff;
            font-size: 1.8em;
            margin: 0 0 25px 0;
        }

        .button {
            background-color: #72b619;
            border: none;
            color: white;
            padding: 15px 20px;

            font-family: 'Barlow', arial, sans-serif;
            font-weight: 700;
            text-align: center;
            text-decoration: none;
            text-transform: uppercase;
            text-shadow: rgba(255, 255, 255, 0.5) 0px 2px;
            letter-spacing: 0.06em;
            font-size: 1.8em;

            border-radius: 5px;
            display: inline-block;
            margin: 4px 2px;
            cursor: pointer;
        }
    </style>
</head>

<body>
<div class="container">
    <div class="content">

        <div class="brand">
            <img alt="Max Quest" src="/images/error-pages/mq/sectorx-logo.png"/>
        </div>

        <% if (StringUtils.isTrimmedEmpty(incompleteRoundUrl)) { %>
        <div class="message message_fatal">
            <h1>Thank you for playing. Please try again later.</h1>
        </div>
        <% } %>

        <% if (!StringUtils.isTrimmedEmpty(incompleteRoundUrl)) { %>
        <div class="message message_incomplete">
            <h1>You already have an open game session which needs to be completed.<br>Please resume the game</h1>
            <a href="<%=incompleteRoundUrl%>" class="button">Resume Game</a>
        </div>
        <% } %>

    </div>
</div>
</body>
</html>
