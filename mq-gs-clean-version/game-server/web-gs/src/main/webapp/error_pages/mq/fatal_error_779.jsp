<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ page import="org.apache.commons.lang.LocaleUtils" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String lang = (String) session.getAttribute("lang");

    if (lang != null) {
        try {
            Locale locale = LocaleUtils.toLocale(lang);
            session.setAttribute(Globals.LOCALE_KEY, locale);
        } catch (IllegalArgumentException e) {
        }
    }

    GameServerConfiguration conf = ApplicationContextHelper.getApplicationContext()
            .getBean(GameServerConfiguration.class);
%>
<DOCTYPE html>
    <!--[if IE 8 ]>
    <html lang="en" class="ie8"> <![endif]-->
    <!--[if IE 9 ]>
    <html lang="en" class="ie9"> <![endif]-->
    <!--[if (gt IE 9)|!(IE)]><!-->
    <html lang="en"> <!--<![endif]-->
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
            body {
                width: 100%;
                min-height: 100%;
                margin: 0;
                padding: 0;
                background-image: url(/images/error-pages/mq/leaderboard-bg-min.jpg);
                background-size: cover;
                background-repeat: no-repeat;
                font-size: 16px;
            }
            .container {
                position: relative;
                width: 100%;
                height: 100%;
                background-image: url(/images/error-pages/mq/leaderboard-mq-eyes5-min.png);
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
                max-width: 600px;
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

            footer {
                clear: both;
                position: absolute;
                bottom: 0;
                width: 100%;
                text-align: center;
                padding: 20px 0px;
            }

            footer img {
                width: 140px;
                height: 54px;
                display: inline-block;
            }
        </style>
    </head>

    <body>

    <div class="wrapper">
        <div class="container">
            <div class="row1">

                <div class="span1">
                    <div class="brand">
                        <img alt="Max Quest" src="/images/error-pages/mq/mq-logo.png"/>
                    </div>
                </div>
                <div class="span2">
                    <h1><bean:message key="error.common.thanksMessage"/></h1>
                </div>

            </div>
            <footer>
                <a href="https://<%=conf.getBrandNameLowCase()%>.com" target="_blank">
                    <img src="/images/error-pages/mq/<%=conf.getBrandApiRootShortTagName()%>-logo.png" alt="<%=conf.getBrandName()%> logo">
                </a>
            </footer>
        </div>
    </div>
    </body>
    </html>