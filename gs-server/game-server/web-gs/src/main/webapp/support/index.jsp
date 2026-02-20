<%--
  Created by IntelliJ IDEA.
  User: nick
  Date: 28.01.16
  Time: 10:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>GP3 branch support tools</title>
    <link rel="stylesheet" href="/support/css/bootstrap.min.css"/>
    <script type="text/javascript" src="/support/js/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="/support/js/bootstrap.min.js"></script>
    <script>
        function toggleDescription(elem) {
            var descr = $(elem).parents('div:first').children('.descr');
            if (descr.is(':visible')) {
                descr.fadeOut('slow');
            } else {
                descr.fadeIn('slow');
            }
        }
    </script>
    <style>
        .descr {
            display: none;
        }
        .descr-toggle {
            cursor: pointer;
        }
    </style>
</head>
<body>
<div class="container col-xs-8 col-xs-offset-2">
    <h1 style="text-align: center">GP3 BRANCH SUPPORT TOOLS</h1>
    <%
        String serverName = request.getServerName();
        String cmServerName = "report" + serverName.substring(3);
        String scheme = request.getScheme();
    %>
    <ul class="list-group">

        <li class="list-group-item nav">
            <h3>Casino Manager</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://report-[host]</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Casino Manager Description</div>
                    <div class="panel-body descr"> Example link: <a href="<%=scheme%>://<%=cmServerName%>"><%=scheme%>://<%=cmServerName%>
                    </a></div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Cluster Hosts Config</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/clusterHosts.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Cluster Hosts Config Description</div>
                    <div class="panel-body descr">
                        Centralized host/port values used by refactor deployment.
                        Example link:
                        <a href="/support/clusterHosts.jsp"><%=scheme%>://<%=serverName%>/support/clusterHosts.jsp</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>GS Configuration Portal</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/configPortal.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">GS Configuration Portal Description</div>
                    <div class="panel-body descr">
                        Unified operator portal for configuration levels:
                        cluster hosts, bank settings catalog, and effective bank values.
                        Example link:
                        <a href="/support/configPortal.jsp"><%=scheme%>://<%=serverName%>/support/configPortal.jsp</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Modernization Progress Dashboard</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/modernizationProgress.html</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Modernization Progress Dashboard Description</div>
                    <div class="panel-body descr">
                        Temporary visual tracker for legacy coverage, milestones, and phase progress.
                        It includes checkboxes and section/overall progress bars.
                        Example link:
                        <a href="/support/modernizationProgress.html"><%=scheme%>://<%=serverName%>/support/modernizationProgress.html</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Modernization Docs Index</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/modernizationDocs.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Modernization Docs Index Description</div>
                    <div class="panel-body descr">
                        Quick index for roadmap docs, phase deliverables, and runtime evidence locations.
                        Example link:
                        <a href="/support/modernizationDocs.jsp"><%=scheme%>://<%=serverName%>/support/modernizationDocs.jsp</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Modernization Runbook</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/modernizationRunbook.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Modernization Runbook Description</div>
                    <div class="panel-body descr">
                        Operational command sequence for Phase 4/5 readiness checks and evidence-pack generation.
                        Example link:
                        <a href="/support/modernizationRunbook.jsp"><%=scheme%>://<%=serverName%>/support/modernizationRunbook.jsp</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Cache Viewer</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/cacheviewer.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Cacheviewer Description</div>
                    <div class="panel-body descr">
                        It allows you to view objects in selected cache. Example link:
                        <a href="/support/cacheviewer.do"><%=scheme%>://<%=serverName%>/support/cacheviewer.do</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Server statistics</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/stat</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Server statistics Description</div>
                    <div class="panel-body descr">
                        <p>
                            Shows summary statistics of queries on server. Example link:
                            <a href="stat">
                                <%=scheme%>://<%=serverName%>/support/stat</a><br/>
                            <a href="stat?getter=true"><%=scheme%>://<%=serverName%>/support/stat?getter=true</a> (Can be slow).
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>System Diagnosis</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/systemdiagnosis.servlet</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">System Diagnosis Description</div>
                    <div class="panel-body descr">
                        <p>
                            Makes diagnosis of game server. Example link: <a href="/systemdiagnosis.servlet">
                            <%=scheme%>://<%=serverName%>/systemdiagnosis.servlet</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>System parameters monitoring</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/metrics/</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">System Parameters Monitoring Description</div>
                    <div class="panel-body descr">
                        <p>
                            Cбор различных характеристик системы для gp3 и их отображение в графическом виде. Новые значения конкретной метрики
                            сохраняются через определенный
                            интервал(по умолчанию 5 сек.). Если интервал для отображения больше 12 часов, то отображаются средние за час значения, а
                            также минимальное и максимальное значение за этот час.
                        </p>
                        <p>Страница для просмотра параметров системы здесь /support/metrics/<br/>
                            <a href="/support/metrics/"><%=scheme%>://<%=serverName%>/support/metrics/</a><br/>
                            <a href="/support/metrics/"><%=scheme%>://<%=serverName%>/support/metrics/</a><br/>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Send Missing Game Sessions Alerts</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/sendMissingGameSessionsAlerts.jsp?startDate=[start date]&endDate=[end date]&filePath=[file
                            path]
                        </td>
                    </tr>
                </table>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Log Viewer</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/logviewer.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Log Viewer Description</div>
                    <div class="panel-body descr">
                        A tool for viewing failed http requests/responses.
                        Example link: <a href="/support/logviewer.do"><%=scheme%>://<%=serverName%>/support/logviewer.do</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>View pending/online wallets</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/viewWallets.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">View pending/online wallets Description</div>
                    <div class="panel-body descr">
                        <p>
                            It allows to view pending/online wallets and matching transactionData. Also it can finish operation tracking if operation
                            is already PENDING. Example link:
                            <a href="/support/viewWallets.jsp"><%=scheme%>://<%=serverName%>/support/viewWallets.jsp</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>ShowAPIIssues</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/showAPIIssues.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">ShowAPIIssues Description</div>
                    <div class="panel-body descr">
                        Ссылка: <a href="/support/showAPIIssues.do"><%=scheme%>://<%=serverName%>/support/showAPIIssues.do</a>
                        <p>Нужна для суппорта/тестирования/отладки.Показывает статистику для api вызовов(success, failed count, failed percent,
                            lastFailTime). Добавлены фильтры и сортировка.
                            Макимальная длина периода, за который можно посмотреть статистику - 30 дней.
                        <p>Последний столбец расчитывается как failed percent = failed count / ( failed count + success count) * 100. Строки, для
                            которых failed percent > 10%, подсвечиваются красным.</p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Test Common Wallet EC API</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/tools/test/api/commonWallet.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Test Common Wallet EC API Description</div>
                    <div class="panel-body descr">
                        <p>Как пользоваться:</p>
                        <p>
                            Ввести параметры. Сделать это можно двумя способами: через форму по адресу
                            <a href="/tools/test/api/commonWallet.do"><%=scheme%>://<%=serverName%>/tools/test/api/commonWallet.do</a> или как и
                            раньше задав параметры в URL, например,
                            /support/test/api/commonWallet.do?bankId=123&gameId=100&token=1V94z3awBJ978Z1&scenario=Complete.
                            Добавился один новый параметр - scenario (тестовый сценарий). Этот параметр является обязательным. Тестовый сценарий -
                            набор проверок, который будет выполнен при тестировании API. На данный момент заведено три сценария:</p>
                        <ul>
                            <li>BonusAndStandard - содержит полный набор проверок;</li>
                            <li>Standard - содержит все проверки, кроме проверок связанных с cash bonus и FRB;</li>
                            <li>Bonus - содержит проверки API cash bonus, FRB и проверку аутентификации.</li>
                            <li>Minimal - тестируется всё самое базовое(auth, balance, bet + win, refund), это сценарий отрабатывает быстрее чем
                                Standard.
                            </li>
                        </ul>
                        <p>
                            Запустить тест, нажав на кнопку "Run API Test" формы или перейдя про URL'у с параметрами. После завершения теста откроется
                            форма с результатами. Детали каждой проверки можно посмотреть кликнув по названию.
                            Просмотреть заключение, нажав на кнопку "Show conclusion". Откроется форма с Description:м причин провала отдельных
                            проверок, если такие были, и полным логом запросов к EC API.
                        </p>
                        <p>
                            Добавлена проверка на соответствие баланса возвращаемого после авторизации и GetBalance. Также добавлены аналогичные
                            проверки баланса в GetBalance и после нулевого бета и нулевого вина.
                            Исправлено падение всех тестов после провала авторизации. Теперь при невозможности получить userId используется значение
                            по умолчанию unknown и при невозможности получения accountId - значение по умолчанию -1. Остальные тесты пытаются
                            выполниться с этими значениями.
                        </p>
                        <p>
                            Добавлена валидация незавершённых игр, повисших операций, неправильного значения бета(миминальное, максимальное).
                            Валидацию можно отключить галочкой disable validation.
                            Можно выбрать режим Many games, в gameId можно указывать игры через +|, или если оставить поле пустым, то будут
                            проверяться все игры с gamelist.
                            Если все кейсы для игры прошли успешно, то выводится строка с result=Ok, если один из кейсов завалился, то выводится
                            result=Error, и лог только с завалившемися операциями.
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Cluster management tool</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>gp3-local</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td>http://[host]/support/clustermanagement.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Cluster management tool Description</div>
                    <div class="panel-body descr">
                        <p>Позволяет отслеживать состояние gs-ов на различных кластерах, управлять их состояниями (старт, стоп, ребут)</p>
                        <p>Просмотр в режиме выбора кластера: <a href="http://gs1-gp3.dgphoenix.com:8180/support/clustermanagement.do">http://gs1-gp3.dgphoenix.com/support/clustermanagement.do</a>
                        </p>
                        <p>Просмотр сразу всех кластеров и gs-ов: <a href="http://gs1-gp3.dgphoenix.com:8180/support/clustermanagement.do?full=true">http://gs1-gp3.dgphoenix.com/support/clustermanagement.do?full=true</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Response Emulation</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/ext-support/emulateresponse.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Response Emulation Description</div>
                    <div class="panel-body descr">
                        <p>Пример обращения :<a href="/ext-support/emulateresponse.do"><%=scheme%>://<%=serverName%>
                            /ext-support/emulateresponse.do</a></p>
                        <p>Можно создавать emulated response (эмулированный ответ) трех типов:</p>
                        <ol>
                            <li>HTTP_ERROR — Ответ с заданным кодом ошибки (Status Code)</li>
                            <li>PLAIN_FILE — содержимое ответа вычитывается из файла</li>
                            <li>JSP - содержимое response формируется jsp на которую производится форвард (для случаев когда необходимо реализовать
                                более сложную логику) .
                            </li>
                        </ol>
                        <p>Для того, чтобы создать emulated response, необходимо выбрать в выпадающем списке подходящий вариант (HTTP_ERROR,
                            PLAIN_FILE, JSP) и указать параметры Session ID и Command. Эти параметры являются обязательными для всех типов Emulated
                            Response (собственно, фильтр, который процессит запросы к игровым сервлетам (*.game, *.servlet), проверяет наличие
                            emulated response по ключу, составленному из этих двух параметров, и если такого нет, то управление передается дальше
                            игровому сервлету).
                            Затем необходимо указать данные для создания emulated response в соответствии с выбранным типом:</p>
                        <ul>
                            <li>Для HTTP_ERROR это код статуса HTTP</li>
                            <li>Для PLAIN_FILE это файл с диска</li>
                            <li>Для JSP это путь к jsp, которая формирует response, например /empty.jsp</li>
                        </ul>
                        <p>
                            После этого, нажать кнопку Submit. Вверху страницы появится таблица с emulated response, если ее до этого не было, иначе в
                            существующую добавится новая строка.
                            В таблице отображаются: ключ, с которым связан emulated response (SID, CMD), Description: response, связанного с ключом, и
                            кнопка для удаления emulated response.
                            После перезагрузки сервера все emulated response удаляются.
                            Если на сервере не включен Emulation Filter, то вверху странички будет предупреждающая надпись: «Emulation Filter is not
                            available for this server!» и, следовательно, управление всегда будет передаваться игровому сервлету.
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Bank/Subcasino Configuration</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/cache/bank/common/subcasinoSelect.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Bank/Subcasino Configuration Description</div>
                    <div class="panel-body descr">
                        Allows editing properties of games, banks, etc. Example link:
                        <a href="/support/cache/bank/common/subcasinoSelect.jsp"><%=scheme%>://<%=serverName%>
                            /support/cache/bank/common/subcasinoSelect.jsp</a>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Show Cassandra Account</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/showcassandra_account.jsp?accountId=[account ID]</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Show Cassandra Account Description</div>
                    <div class="panel-body descr">
                        <p>
                            View Account Info from AccountsCache which has no active player/game sessions. Example link:
                            <a href="/support/showcassandra_account.jsp?accountId=333732692"><%=scheme%>://<%=serverName%>
                                /support/showcassandra_account.jsp?accountId=333732692</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>View FRB Coin</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/viewFRBCoin.jsp?bankId=[bankId1|...]&ampcurrencyCode=[currencyCode1|...]</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">View FRB Coin Description</div>
                    <div class="panel-body descr">
                        <p>Use: getting FRB settings information. Example
                            link:<a href="/support/viewFRBCoin.jsp?bankId=123&currencyCode=all"><%=scheme%>://<%=serverName%>
                                /support/viewFRBCoin.jsp?bankId=123&ampcurrencyCode=all</a></p>
                        <p>Parameters:</p>
                        <ul>
                            <li>bankId - inner bank id, it can contain one or more values splitted with "|" token</li>
                            <li>currencyCode - currency id in our system, can be passed "all" value and in this case all bank currencies info will be
                                get.
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Show Games Details Prop 2</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]support/showGamesDetailsProp2.jsp?bankId=[bank ID|...]&gameId=[game ID|...]&ampcurrencyCode=[currency
                            code|...]&prop=[property|...]
                        </td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Show Games Details Prop 2 Description</div>
                    <div class="panel-body descr">
                        <p>
                            Script for viewing coin, limits, defcoin, etc. settings. Example link :
                            <a href="/support/showGamesDetailsProp2.jsp?bankId=271&gameId=210%7C209&currencyCode=EUR"><%=scheme%>://<%=serverName%>
                                /support/showGamesDetailsProp2.jsp?bankId=271&gameId=210%7C209&ampcurrencyCode=EUR</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>View JP Details</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/showJackpotDetails.jsp?bankId=[bank ID]&gameId=[game ID]&ampcurrencyCode=[currency
                            code]&highLightErrors&printCoins
                        </td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">View JP Details Description</div>
                    <div class="panel-body descr">
                        <p>Скрипт призван помочь быстро просматривать JP настройки в играх, по банку/валюте и тп. Пример обращения:
                            <a href="https://<%=serverName%>/support/showJackpotDetails.jsp?bankId=92&gameId=210&currencyCode=EUR&highLightErrors&printCoins">https://<%=serverName%>
                                /support/showJackpotDetails.jsp?bankId=92&gameId=210&currencyCode=EUR&highLightErrors&printCoins</a>
                        </p>
                        <ul>
                            <li>bankId - индификатор банка, по которому будет выполнен запрос. Возможные варианты: указать один или несколько bankId
                                разделенных | . Например: 92|613.
                            </li>
                            <li>gameId - индификатор игры, по которой будет выполнен запрос. Параметр опциональный, если его нету, то выводится по
                                всем JACKPOT играм банка. Возможные варианты: указать один или несколько gameId разделенных | . Например: 210|177
                            </li>
                            <li>subCasinoId - индификатор субказино. Опциональный параметр, если он передан, то скрипт выполнится для всех банков
                                этого сабказино.
                            </li>
                            <li>CurrencyCode - валюта, по которой выполняется запрос. Возможные варианты: all - по всем валютам на банке; либо
                                передать один или больше кодов, в формате EUR|USD|JPY. Опциональный параметр, если не указан явяно, то считается
                                currecnyCode=all.
                            </li>
                            <li>highLightErrors - подсветка запроса, который невозможно выполнить по каким-либо причинам. Обычно явно указывает на
                                ошибку.
                            </li>
                            <li>printCoins - детальное значение накопленной части (PCR Bank) по каждой монете, а так же текущее значение джекпота
                                (действительное, которое видит пользователь) по этой монете.
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Get Banks With Game2</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/getBanksWithGame2.jsp?gid=[game ID]</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Get Banks With Game2 Description</div>
                    <div class="panel-body descr">
                        <p>Use: shows list of all subcasino/banks where game is configured. Examle
                            link:<a href="/support/getBanksWithGame2.jsp?gid=210"><%=scheme%>://<%=serverName%>
                                /support/getBanksWithGame2.jsp?gid=210</a></p>
                        <p>Parameters: gid - game id.</p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Jackpot Default Game Params</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/jackpotDefaultGameParams.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Jackpot Default Game Params Description</div>
                    <div class="panel-body descr">
                        <p>Usage: shows JackPot Multiplier and Default PCR percent information for all games. Example link:
                            <a href="/support/jackpotDefaultGameParams.jsp"><%=scheme%>://<%=serverName%>/support/jackpotDefaultGameParams.jsp</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Show Banks Details Prop</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/showBanksDetailsProp.jsp?bankId=[bankId|...]&prop=[property]</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Show Banks Details Prop Description</div>
                    <div class="panel-body descr">
                        <p>Use: viewing one or more bank properties. Example link:
                            <a href="/support/showBanksDetailsProp.jsp?bankId=135%7C171&prop=COMMON_WALLET_WAGER_URL"><%=scheme%>://<%=serverName%>
                                /support/showBanksDetailsProp.jsp?bankId=135%7C171&prop=COMMON_WALLET_WAGER_URL</a>
                        </p>
                        <p>Parameters</p>
                        <ul>
                            <li>bankId - inner bank id, it can contain one or more values splitted with "|" token</li>
                            <li>prop - property name from bankInfo (not all properties will be shown, only not boolean values)</li>
                        </ul>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Get Games Config By Banks</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]support/GameConfig/getGamesConfigByBanks.jsp?banks=[bankID]&editmode=[edit mode]&mode=[mode1, ....]
                        </td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Get Games Config By Banks Description</div>
                    <div class="panel-body descr">
                        <p>Form for view/edit coin property, defcoin/frbcoin, bank limits. Example link:
                            <a href="/support/GameConfig/getGamesConfigByBanks.jsp?banks=271&editmode=true&mode=coins,frb,limit,defcoin&"><%=scheme%>
                                ://<%=serverName%>
                                /support/GameConfig/getGamesConfigByBanks.jsp?banks=271&editmode=true&mode=coins,frb,limit,defcoin&</a>
                        </p>
                        <p>Parameters:</p>
                        <ul>
                            <li>banks - inner bank ID.</li>
                            <li>editmode - if true, it adds possibility to edit properties</li>
                            <li>mode - filter for viewing parameters</li>
                        </ul>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Banks compare</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/compare/banks.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Banks compare Description</div>
                    <div class="panel-body descr">
                        <p>Пример вызова: <a href="/support/compare/banks.jsp"><%=scheme%>://<%=serverName%>/support/compare/banks.jsp</a></p>
                        <p>
                            Сначала пользователь выбирает ID банков для сравнения, а также кластер для каждого банка.
                            Далее идут режимы программы, можно выбрать несколько, если ни один не отмечен то используются все сразу:
                        </p>
                        <p>
                            Bank properties — вывести отличающиеся параметры у банка (BankInfo).
                            В первой колонке идут названия параметров (полей), жирным идут основные параметры (поля класса) — назовём их статические.
                            Обычным шрифтом В_ВЕРХНЕМ_РЕГИСТРЕ С_ПОДЧЁРКИВАНИЯМИ идут дополнительные параметры (в поле вида Map< String,String >
                            properties) — назовём их динамические.
                            Во второй колонке тип параметра (type в java, либо аннотации для динамических параметров), 3я и 4я — значения параметров
                            (отображаются через toString(), в редких случаях приводятся к более читабельному виду, например currencies и coins).
                            Если динамическое поле является обязательным (mandatory) то фон его названия будет зелёным. Также для динамических полей
                            реализована проверка данных, например, если тип поля будет Boolean, а значение будет не true/false то оно будет
                            подчёркнуто красным.
                            (Для всех параметров (статические и динамические) в виде объекта String, значение null приравнивается к "". Все
                            динамические поля проходят trim(), для Boolean: регистр приводится в нижний, пустое поле = false)
                        </p>
                        <p>
                            Games list — вывести отличающиеся игры у банков. У банка, который содержит игру, будет значение Delivered, у другого
                            пустое поле. Для этого режима учитывается валюта, которая задаётся в полях - Currency for Bank. Если поле валюты пустое,
                            то берётся валюта по умолчанию. (Название игры берётся из BaseGameTemplate из поля title, если нет то из name, если опять
                            нету то пишется ERROR_NAME ID=x.).
                            Для получения отчёта в отдельном окне надо нажать кнопку Show missing games, тогда выведется список игр, которые
                            отсутствуют у этого банка.
                            Missing languages in games — вывести только отсутствующие игры и языки, в данном банке. Валюта учитывается.
                        </p>
                        <p>
                            Games with properties — вывести параметры игр, если игра есть у двух банков. По умолчанию выводятся отличающиеся
                            параметры, если поставить галочку Show all properties то будут выведены все параметры, а отличающиеся выделены красным.
                            Также можно указать какие конкретно игры сравнивать через поле Games Ids, можно указывать несколько игр через запятую или
                            другие разделители |, +, и др. Поле jackPotId не сравнивается. Валюта берётся из Currency for Bank.
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Game template compare</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/compare/gameTemplates.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Game template compare Description</div>
                    <div class="panel-body descr">
                        <p>Пример вызова: <a href="/support/compare/gameTemplates.jsp"><%=scheme%>://<%=serverName%>
                            /support/compare/gameTemplates.jsp</a></p>
                        <p>Сравниваются общие параметры шаблона (BaseGameTemplate) и его шаблон по умолчанию (defaultGameInfo).</p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Server Configuration</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/serverConfiguration.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Server Configuration Description</div>
                    <div class="panel-body descr">
                        <p>Allows add new and modify existing game server configuration properties
                            <a href="/support/serverConfiguration.do"><%=scheme%>://<%=serverName%>/support/serverConfiguration.do</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Language Tools</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/langTools.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Language Tools Description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/support/langTools.jsp"><%=scheme%>://<%=serverName%>/support/langTools.jsp</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>New bank and subcasino</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/newBankNSubCasino.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">New bank and subcasino Description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/support/newBankNSubCasino.do"><%=scheme%>://<%=serverName%>/support/newBankNSubCasino.do</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Bank and subcasino control</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/BankNSubCasinoControl.do</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Bank and subcasino control Description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/support/BankNSubCasinoControl.do"><%=scheme%>://<%=serverName%>/support/BankNSubCasinoControl.do</a>
                        </p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Wallet info</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/walletInfo.jsp</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Wallet info Description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/support/walletInfo.jsp"><%=scheme%>://<%=serverName%>/support/walletInfo.jsp</a>
                        </p>
                        <p>
                            Если юзер онлайн, нет никаких кнопок, надпись "Player is online! No actions are allowed.", иначе
                        <ol>
                            <li>
                                Для wallet
                                <div>
                                    -если операция трекается, то отображаются delete и suspend
                                </div>
                                <div>
                                    -если операция подвисла(PENDING_SEND_ALERT статус), то отображается resolve
                                </div>
                            </li>
                            <li>
                                Для lastHand
                                <div>-отображается delete</div>
                            </li>
                        </ol>
                        <p> resolve: работает аналогично нажатию Process в CM Wallet Operation Alerts. При этом в CM отправляется алерт со статусом
                            RESOLVED. Операция начинает трекаться и, если опять закончится неуспешно(через 5 мин), то она преходит в статус
                            PENDING_SEND_ALERT, и на CM отправится алерт со статусом UNRESOLVED.</p>
                        <p> suspend: переводит операцию из traking в pending(PENDING_SEND_ALERT). Отправляется алерт со статусом UNRESOLVED.</p>
                        <p> delete: Для операции - удялает и отправляет алерт со статусом DELETED. Для lastHand - просто удаляет lastHand.</p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Game history from support</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/support/gamehistory.do?accountId=[accountId]</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Game history from support Description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/support/gamehistory.do?accountId=587426150"><%=scheme%>://<%=serverName%>
                                /support/gamehistory.do?accountId=587426150</a>
                        </p>
                        <p> Утилита показывает историю игрока, с ссылками на VAB'ы. Тоже самое что handhistory.jsp/gamehistory.do/cwstarthistory.do,
                            только не требует открытой сессии.
                        <p> Обязательный параметр accountId= (внутренний ID), также можно задать itemsPerPage=, по дефолту 40. Остальные параметры
                            можно задать на странице, а именно:
                        <p> gameId= (если -1, значит все игры), mode= (0 - все, 1 - Real, 2 - Bonus, 3 - FRB), и параметры вренного отрезка:
                            startYear=, startMonth=, startDay=, endYear=, и др.
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Wallet manager</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td><%=scheme%>://[host]/tools/walletsManager.jsp?accountId=[account ID]&bankId=[bank ID]&extUserId=[external user ID]</td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Wallet manager Description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/tools/walletsManager.jsp"><%=scheme%>://<%=serverName%>/tools/walletsManager.jsp</a>
                        </p>
                        <p>Allows to manage wallet\frbonus\last hand operations.</p>
                    </div>
                </div>
            </div>
        </li>

        <li class="list-group-item nav">
            <h3>Edit BaseGameInfoTemplate property</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td>
                            <%=scheme%>://[host]/support/editTemplateProperty.jsp<br/>
                        </td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Property editor description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/support/EditTemplateProperty.jsp">https://<%=serverName%>/support/EditTemplateProperty.jsp</a><br/>
                        </p>
                        <p>Allows to create/edit/delete properties of BaseGameInfo.</p>
                    </div>
                </div>
            </div>
        </li>
        <li class="list-group-item nav">
            <h3>Show Jackpot contributions</h3>
            <div class="container col-xs-12">
                <table class="table table-bordered">
                    <tr>
                        <td>Deployed:</td>
                        <td>copy, live</td>
                    </tr>
                    <tr>
                        <td>URL:</td>
                        <td>
                            <%=scheme%>://[host]/support/showJackpotContributions.jsp<br/>
                        </td>
                    </tr>
                </table>
                <div class="panel panel-info">
                    <div class="panel-heading descr-toggle" onclick="toggleDescription(this)">Property editor description</div>
                    <div class="panel-body descr">
                        <p>Example link:
                            <a href="/support/showJackpotContributions.jsp">https://<%=serverName%>/support/showJackpotContributions.jsp</a><br/>
                        </p>
                        <p>Allows to show jackpot contributions of all JP generations in unified format.</p>
                    </div>
                </div>
            </div>
        </li>
    </ul>
</div>
</body>
</html>
