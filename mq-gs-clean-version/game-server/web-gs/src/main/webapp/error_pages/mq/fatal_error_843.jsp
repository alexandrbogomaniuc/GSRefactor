<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ page import="org.apache.commons.lang.LocaleUtils" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.util.Locale" %>
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
            background-image: url(/images/error-pages/mq/cog-bg.jpg);
            background-size: cover;
            background-position: center top;
            background-repeat: no-repeat;
        }

        .row1 {
            position: absolute;
            height: 50%;
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
            max-width: 200px;
            width: 70%;
        }

        .span2 {
            text-align: center;
            background-color: rgba(0, 0, 0, 0.5);
            padding: 20px;
            width: 100%;
            max-width: 800px;
            display: block;
            margin: 0 auto;
        }

        .span2 h1 {
            font-family: "Open Sans", arial, sans-serif;
            color: #ffffff;
            margin-top: 20px;
            font-size: 2em;
        }
    </style>
</head>

<body>
<div class="container">
    <div class="row1">

        <div class="span1">
            <div class="brand">
                <img alt="Max Quest" src="/images/error-pages/mq/cog-logo.png"/>
            </div>
        </div>
        <div class="span2">
            <h1><bean:message key="error.common.thanksMessage"/></h1>
        </div>

    </div>
</div>
</body>
</html>
