function VabEngine() {
    let that = this;
    const TOUCH_EVENTS = ['touchstart', 'touchmove', 'touchend', 'touchcancel'];
    const MOUSE_EVENTS = ['mousedown', 'mouseup', 'mousemove', 'mouseover', 'mouseout', 'mouseenter', 'mouseleave'];

    this.canvas = null;
    this.context = null;

    this.array_text = new Array(Text);
    this.array_sprite = new Array(Sprite);
    this.array_textures = new Array(Texture);
    this.array_sprite_draw = new Array(Sprite);

    that.array_text.length = 0;
    that.array_sprite.length = 0;
    that.array_textures.length = 0;
    that.array_sprite_draw.length = 0;

    this.loaded_lines = 0;
    this.bar_delta = 0;

    this.total_records = 0;
    this.current_records = 0;
    this.max_records_in_get = 250;
    this.line_id = 0;

    this.previous_session_id = 0;
    this.current_session_id = 0;
    this.next_session_id = 0;

    this.round_id = 0;

    this.center_x = 137;
    this.text_start = 210;
    this.text_step = 21;

    this.array_row = null;

    this.scheme = "";
    this.folderName = "";
    this.gameName = "";
    this.serverName = "";
    this.title = "";
    this.brandName = "";
    this.belgiumMode = false;

    this.callback_keydown = null;
    this.callback_keyup = null;
    this.callback_mousedown = null;
    this.callback_mouseup = null;
    this.callback_mousemove = null;
    this.callback_row_parse = null;
    this.callback_row_create = null;
    this.callback_row_click = null;

    this.callback_draw_line_info = null;

    this.selected_row = null;
    this.callback_draw = null;

    this.linked_text = null;
    this.linked_sprite_array = null;

    this.cursor_pos = {x: 0, y: 0};

    this.lines_info_state = 0;
    this.WIN_LINES = null;
    this.COLORS = null;

    this.width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
    this.height = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

    this.splash = new Splash();

    this.strings = {};

    this.initTranslation = function (common_strings, game_strings) {
        $.extend(this.strings, common_strings, game_strings);
    };

    this.translate = function (word) {
        let result;
        if (!word) {
            return word;
        }
        if (!this.strings[word]) {
            result = word;
            if (this.lang) {
                console.log("Missing translation: '" + word + "'");
            }
        } else {
            result = this.strings[word];
        }

        for (let i = 1; i < arguments.length; i++) {
            result = result.replaceFirst('{}', arguments[i]);
        }

        return result;
    };

    this.loadTexture = function (filename) {
        let texture_path = "";
        if (filename.indexOf(that.folderName + "/") !== -1) texture_path = filename;
        else texture_path = that.folderName + "/" + filename;

        let texture = getTexture(texture_path);

        if (texture == null) {
            texture = new Texture(texture_path);
            that.array_textures.push(texture);
        }

        return texture;
    };

    function getTexture(texture_path) {
        for (let i = 0; i < that.array_textures.length; i++) {
            if (that.array_textures[i].getTexturePath() === texture_path)
                return that.array_textures[i];
        }
        return null;
    }

    this.createSprite = function (filename) {
        let texture = that.loadTexture(filename);

        let sprite = new Sprite();
        sprite.setTexture(texture);
        if (texture.imageObj != null)
            sprite.init(that, that.canvas, that.context);

        that.array_sprite.push(sprite);

        return sprite;
    };

    this.addSprite = function (sprite) {
        that.array_sprite_draw.push(sprite);
        that.linked_sprite_array = getSpritesUnderCursor();
    };

    this.createText = function (string) {
        let text = new Text();
        text.setText(string);
        text.canvas = that.canvas;
        text.context = that.context;
        that.array_text.push(text);

        return text;
    };

    this.clearCanvas = function () {
        that.array_text.length = 0;
        that.array_sprite_draw.length = 0;
        that.context.clearRect(0, 0, that.canvas.width, that.canvas.height);
        document.body.style.cursor = "";

        if (that.linked_sprite_array != null) {
            that.linked_sprite_array.length = 0;
        }
    };

    function resizeWindow() {
        let canvas_window = $('#canvas_window');

        that.canvas = document.getElementById("myCanvas");
        that.canvas.width = 274;
        that.canvas.height = canvas_window.height() === 0 ? 426 : canvas_window.height();
    }

    this.setFolder = function (folderName) {
        that.folderName = folderName;
    };

    this.init = function (onComplete, scheme, serverName, gameName, viewSessionID, viewRoundID, title, hideBalance, showExtId) {
        that.scheme = scheme;
        that.gameName = gameName;
        that.serverName = serverName;
        that.round_id = viewRoundID;
        that.title = title;
        that.hideBalance = hideBalance;
        that.showExtId = showExtId;

        document.title = that.title;

        resizeWindow();

        setTimeout(function () {
            that.splash.create(that.title, initEngine);
        }, 50);

        function initEngine() {
            let loaded = 0;

            if (that.array_textures.length > 0) {
                that.splash.setLabelActionText(engine.translate("Loading images"));
                for (let i = 0; i < that.array_textures.length; i++) {
                    that.array_textures[i].load(that.splash, onLoad);
                }
            }
            else {
                doneLoadingTextures(onComplete, viewSessionID, viewRoundID);
            }

            function onLoad() {
                loaded++;

                let percentage = 10 / that.array_textures.length;
                that.splash.addToLoadingBar(percentage);

                if (loaded === that.array_textures.length) {
                    doneLoadingTextures(onComplete, viewSessionID, viewRoundID);
                }
            }
        }
    };

    function doneLoadingTextures(callback, viewSessionID, viewRoundID) {
        if (typeof (G_vmlCanvasManager) !== 'undefined') that.canvas = G_vmlCanvasManager.initElement(that.canvas);

        that.context = that.canvas.getContext("2d");

        that.context.imageSmoothingEnabled = true;

        for (let i = 0; i < that.array_sprite.length; i++) {
            let sprite = that.array_sprite[i];
            sprite.init(that, that.canvas, that.context);
        }

        that.splash.setLabelActionText(engine.translate("Connecting to server..."));

        $(document).delay(20, function () {
            callback();
            startEngine(viewSessionID, viewRoundID);
        });
    }

    function pointerEventToXY(e) {
        let out = {x: 0, y: 0};
        if (TOUCH_EVENTS.indexOf(e.type) !== -1) {
            let touch = e.originalEvent.touches[0] || e.originalEvent.changedTouches[0];
            out.x = touch.pageX;
            out.y = touch.pageY;
        }
        else if (MOUSE_EVENTS.indexOf(e.type) !== -1) {
            out.x = e.pageX;
            out.y = e.pageY;
        }
        return out;
    }

    function getCursorPosition(ev) {
        let rect = that.canvas.getBoundingClientRect();
        let cursor = pointerEventToXY(ev);
        return {
            x: Math.round(cursor.x - rect.left - window.scrollX),
            y: Math.round(cursor.y - rect.top - window.scrollY)
        };
    }

    function canvasMouseMoveEvent(ev) {
        if (!document.body.style.cursor) {
            document.body.style.cursor = "pointer";
            document.body.style.cursor = "";
        }

        that.cursor_pos = getCursorPosition(ev);
        that.linked_text = getTextUnderCursor();

        that.linked_sprite_array = getSpritesUnderCursor();
        if (that.linked_sprite_array != null) {
            for (let i = 0; i < that.linked_sprite_array.length; i++)
                that.linked_sprite_array[i].onMove(that.cursor_pos.x, that.cursor_pos.y);
        }

        if (that.callback_mousemove != null) that.callback_mousemove(that.cursor_pos);
    }

    function canvasMouseDownEvent(ev) {
        that.cursor_pos = getCursorPosition(ev);
        that.linked_text = getTextUnderCursor();

        if (that.linked_text != null) that.linked_text.onClick();

        if (that.linked_sprite_array != null) {
            for (let i = 0; i < that.linked_sprite_array.length; i++)
                that.linked_sprite_array[i].onClick();
        }

        let button = ev.which || window.event.keyCode || ev.button;
        if (that.callback_mousedown != null) that.callback_mousedown(button, that.cursor_pos);
    }

    function canvasMouseUpEvent(ev) {
        that.cursor_pos = getCursorPosition(ev);
        let button = ev.which || window.event.keyCode || ev.button;
        if (that.callback_mouseup != null) that.callback_mouseup(button, that.cursor_pos);
    }

    function getTextUnderCursor() {
        for (let i = 0; i < that.array_text.length; i++) {
            if (that.array_text[i].isContains(that.cursor_pos.x, that.cursor_pos.y)) {
                return that.array_text[i];
            }
        }

        return null;
    }

    function getSpritesUnderCursor() {
        let result_sprite_array = null;

        for (let i = 0; i < that.array_sprite_draw.length; i++) {
            if (that.array_sprite_draw[i].isContains(that.cursor_pos.x, that.cursor_pos.y)) {
                if (that.array_sprite_draw[i].state == 0) that.array_sprite_draw[i].onEnter(that.cursor_pos.x, that.cursor_pos.y);
                else if (that.array_sprite_draw[i].state == 1) that.array_sprite_draw[i].onMove(that.cursor_pos.x, that.cursor_pos.y);

                if (result_sprite_array == null) result_sprite_array = new Array(0);
                result_sprite_array.push(that.array_sprite_draw[i]);
            }
            else {
                if (that.array_sprite_draw[i].state == 1) that.array_sprite_draw[i].onLeave(that.cursor_pos.x, that.cursor_pos.y);
            }
        }

        return result_sprite_array;
    }

    function startEngine(viewSessionID, viewRoundID) {
        onMouseMove(that.canvas, canvasMouseMoveEvent);
        onMouseDown(that.canvas, canvasMouseDownEvent);
        onMouseUp(that.canvas, canvasMouseUpEvent);
        onMouseDown(document, mouseDown);
        onMouseUp(document, mouseUp);
        onKeyboardDown(document, keyDown);
        onKeyboardUp(document, keyUp);

        loadGameSession(viewSessionID, viewRoundID);
        setFilterButtonsCallback();
    }

    function setFilterButtonsCallback() {
        let button_ok = document.getElementById("button_ok");
        button_ok.onclick = function () {
            let RID_min = document.getElementById("RID_min");
            let RID_max = document.getElementById("RID_max");

            setFilter(RID_min.value, RID_max.value);
        };


        let button_clear = document.getElementById("button_clear");
        button_clear.onclick = function () {
            let RID_min = document.getElementById("RID_min");
            let RID_max = document.getElementById("RID_max");

            RID_min.value = "";
            RID_max.value = "";

            setFilter(RID_min.value, RID_max.value);
        };

        setHoverColor(button_ok, "#340071", "#3A3A3A");
        setHoverColor(button_clear, "#340071", "#3A3A3A");
    }

    function setFilter(min, max) {
        let min_value = 0, max_value = 999999999999;
        if (min) min_value = tryParseInt(min);
        if (max) max_value = tryParseInt(max);

        if (min_value == null) debugln("FILTER ERROR: Not correct min value");
        if (max_value == null) debugln("FILTER ERROR: Not correct max value");

        if ((min_value == null) || (max_value == null)) return;

        for (let i = 0; i < that.array_row.length; i++) {
            that.array_row[i].setFilter(min_value, max_value);
        }

        setScroll();
    }

    function loadGameSession(gameSessionId, viewRoundID) {
        that.current_session_id = gameSessionId;
        that.splash.setLabelActionText(engine.translate("Connecting to server..."));
        that.splash.setLoadingBar(10);
        that.splash.setVisible(true, continueLoad);
        disableButtons();

        that.clearCanvas();

        that.loaded_lines = 0;

        function continueLoad() {
            that.array_row = new Array(TRow);
            that.array_row.length = 0;

            clearInfo();

            that.total_records = that.max_records_in_get;
            if (that.total_records == 0) return;

            let i = 0;
            readBlockLines();

            function readBlockLines() {
                window.setTimeout(function () {
                    if (i >= that.total_records) {
                        that.splash.setVisible(false);

                        setButton(document.getElementById("button_prev"), that.previous_session_id);
                        setButton(document.getElementById("button_next"), that.next_session_id);

                        document.getElementById("currency_code").innerHTML = that.currency;
                        document.getElementById("account_id").innerHTML = that.accountId;

                        return;
                    }

                    let start_date = $.getUrlVar('STARTDATE');
                    if (!start_date) start_date = getDate(0, 3, 0);

                    let end_date = $.getUrlVar('ENDDATE');
                    if (!end_date) end_date = getDate(0, 0, 0);

                    if (getTimeZoneError()) {
                        debugln("TIMEZONE_ERROR! Incorrect TimeZone, RESULT=" + getTimeZoneError());
                        that.splash.setLoadingBar(100.0);
                        that.splash.splash_label_percent.innerHTML = engine.translate(engine.translate("ERROR"));
                        that.splash.setLabelActionText(engine.translate("Incorrect TimeZone"));
                        return 0;
                    }

                    // let request = "http://gs1.sb.discreetgaming.com/gsproxy/VisualArchivingBetsV2.servlet?CMD=GETINFO&GAMENAME=LIVEBLACKJACK&VIEWSESSID=219607236&STARTRECORD=0&RECORDS=250&STARTDATE=2014-10-8+00:00:00&ENDDATE=2014-10-9+23:59:59";
                    let request = buildRequest(that.scheme, that.serverName, that.gameName, gameSessionId, viewRoundID, i, that.max_records_in_get, start_date, end_date);
                    httpGetBlock(request, parseBlock);

                    function parseBlock(pageData) {
                        parseData(pageData, buildTable);
                    }
                }, 1);

                function buildTable() {
                    that.splash.setVisible(false, null);
                    createTable(i, readBlockLines);

                    if (i < that.max_records_in_get) {
                        onRowClick(that.array_row[0]);
                    }

                    if (that.callback_row_click != null) that.setRowClickEvent(that.callback_row_click);
                    i += that.max_records_in_get;
                }
            }
        }
    }

    function httpGetBlock(theUrl, callback_parseBlock) {
        $.ajaxSetup({timeout: 30000});
        $.post(theUrl, function (data) {
            callback_parseBlock(data)
        })
            .fail(function (jqXHR, textStatus) {
                debugln("ERROR: " + textStatus);
                that.splash.setVisible(false, null);
            });
    }

    function readJsonInfo(lines) {
        if (lines.result === "ERROR") {
            debugln("ERROR! Information not found, RESULT=ERROR...");
            that.splash.setLoadingBar(100.0);
            that.splash.splash_label_percent.innerHTML = engine.translate("ERROR");
            that.splash.setLabelActionText(engine.translate("Information not found"));
            return 0;
        }

        that.total_records = lines.totalRecords;
        that.current_records = lines.currentRecord;

        that.previous_session_id = lines.previousSessionId;
        that.next_session_id = lines.nextSessionId;
        that.currency = lines.currency;
        that.accountId = lines.accountId;
        that.brandName = lines.brandName;

        if (that.loaded_lines == 0) that.bar_delta = (100.0 - that.splash.getProgressBarPersent()) / (that.total_records);
        if (that.total_records == 0) {
            that.splash.setLoadingBar(100.0);
            that.splash.setVisible(false, null);
            return 0;
        }

        return 1;
    }

    function parseData(data, callback_buildTable) {
        let json_object = JSON.parse(data);
        if (!readJsonInfo(json_object)) return;
        that.splash.setLabelActionText(engine.translate("Loading base..."));

        let json_lines = json_object.playerBets;

        window.setTimeout(function () {
            loadJsonLine(json_lines.length);
        }, 1);

        function loadJsonLine(lines_left) {
            if (lines_left == 0) {
                callback_buildTable();
            }
            else {
                parseJsonLine(json_lines[(that.loaded_lines) % that.max_records_in_get]);
                that.splash.addToLoadingBar(that.bar_delta);
                that.loaded_lines += 1;
                window.setTimeout(function () {
                    loadJsonLine(lines_left - 1);
                }, 1);
            }
        }
    }

    function parseJsonLine(line) {
        if (that.callback_row_parse != null) {
            line.betData = that.callback_row_parse(line.betData);
            line.servletData = that.callback_row_parse(line.servletData);
        }

        let row = new TRow();
        row.parseJson(line);
        that.array_row.push(row);
    }

    let lastRow = null;

    function createRow(row, index, classname, color) {
        that.array_row[index].table_element = row;

        row.id = index;
        row.className = classname;
        row.style.backgroundColor = color;

        $(row).click(function () {
            onRowClick(that.array_row[row.id]);
        });
    }

    function onRowClick(row) {
        if (lastRow != null) setRowColor(lastRow, "#C7C7C7", "#B0B0B0");
        lastRow = row.table_element;

        row.table_element.style.backgroundColor = "#FFD297";

        if (that.callback_row_click != null) that.callback_row_click(row);

        if (that.selected_row !== row) {
            that.selected_row = row;
            if (that.callback_draw != null) that.callback_draw(row);
        }
    }

    function setRowColor(row, color_1, color_2) {
        if (row.id % 2 === 0) row.style.backgroundColor = color_1;
        else row.style.backgroundColor = color_2;
    }

    function createCell(cell, id, width, name, isBold) {
        cell.id = id;
        if (name != null) cell.innerHTML = name;
        cell.style.width = width;
        if (isBold) cell.style.fontWeight = "bold";
    }

    function setButton(button, sessionId) {
        if (sessionId == -1) {
            button.disabled = true;
            button.style.backgroundColor = "#B0B0B0";
        }
        else {
            button.disabled = false;
            button.style.backgroundColor = "#3A3A3A";
            button.onclick = function () {
                loadGameSession(sessionId);
            };

            setHoverColor(button, "#340071", "#3A3A3A");
        }
    }

    function disableButtons() {
        setButton(document.getElementById("button_prev"), -1);
        setButton(document.getElementById("button_next"), -1);
    }

    function setHoverColor(item, color_inside, color_outside) {
        $(item).mouseenter(function () {
            if (!this.disabled) $(this).css({"backgroundColor": color_inside});
        });
        $(item).mousemove(function () {
            if (!this.disabled) $(this).css({"backgroundColor": color_inside});
        });
        $(item).mouseleave(function () {
            if (!this.disabled) $(this).css({"backgroundColor": color_outside});
        });
    }

    function clearInfo() {
        let table = document.getElementById("infotable");
        while (table.rows.length > 0) {
            table.deleteRow(0);
        }
    }

    function getBrowserName() {
        let ua = navigator.userAgent;

        if (ua.search(/Chrome/) > 0) return 'Google Chrome';
        if (ua.search(/Firefox/) > 0) return 'Firefox';
        if (ua.search(/Opera/) > 0) return 'Opera';
        if (ua.search(/Safari/) > 0) return 'Safari';
        if (ua.search(/MSIE/) > 0) return 'Internet Explorer';

        return 'Не определен';
    }

    function createTable(start_index, callback_readNextBlock) {
        let column_number_width = document.getElementById("column_number").style.width;
        let column_date_width = document.getElementById("column_date").style.width;
        let column_roundID_width = document.getElementById("column_roundID").style.width;
        let column_bet_width = document.getElementById("column_bet").style.width;
        let column_state_width = document.getElementById("column_state").style.width;
        let column_payout_width = document.getElementById("column_payout").style.width;
        let column_balance_width = document.getElementById("column_balance").style.width;
        let extBetIdColumn = document.getElementById("column_extBetId");
        let column_extBetId_width = extBetIdColumn ? extBetIdColumn.style.width : 0;

        column_balance_width = column_balance_width.replace("px", "");
        column_balance_width -= 10;
        if (getBrowserName().indexOf("Internet Explorer") !== -1) column_balance_width -= 6;

        let table = document.getElementById("infotable");

        for (let i = start_index; i < that.array_row.length; i++) {
            let index = parseInt(i, 10);

            let back_color = "#C7C7C7";
            if (index % 2 === 1) back_color = "#B0B0B0";

            let row = table.insertRow(-1);
            createRow(row, index, "row", back_color);

            createCell(row.insertCell(-1), "id", column_number_width, index + 1, true);
            createCell(row.insertCell(-1), "date", column_date_width, that.array_row[index].getDate(), false);
            createCell(row.insertCell(-1), "roundId", column_roundID_width, that.array_row[index].getRoundID(), false);
            if (engine.showExtId) {
                createCell(row.insertCell(-1), "extBetId", column_extBetId_width, that.array_row[index].getExtBetId(), false);
            }
            createCell(row.insertCell(-1), "bet", column_bet_width, that.array_row[index].getBet(), false);
            createCell(row.insertCell(-1), "state", column_state_width, engine.translate(that.array_row[index].getStateText()), true);
            createCell(row.insertCell(-1), "payout", column_payout_width, that.array_row[index].getPayout(), false);
            if (!engine.hideBalance) {
                createCell(row.insertCell(-1), "balance", column_balance_width, that.array_row[index].getBalance(), false);
            }

            let currentRow = that.array_row[index];
            let oldText = currentRow.getStateText();

            if (that.callback_row_create != null) that.callback_row_create(that.array_row[index]);

            if (currentRow === that.getLastRow() &&
                (!currentRow.getStateText() || currentRow.getStateText() === "Game End")) { // TODO: delete aften fix game
                debug(oldText);
                currentRow.setStateText(oldText);

                setScroll();
            }
        }

        let title = document.getElementById("id_title");
        title.innerHTML = that.title;

        callback_readNextBlock();
        setScroll();
    }

    function setScroll() {
        let scrollbar_table = $("#boxscroll");

        if (scrollbar_table.getNiceScroll) {
            if (scrollbar_table.getNiceScroll().length !== 0) {
                scrollbar_table.getNiceScroll().resize();
            } else {
                scrollbar_table.niceScroll({
                    touchbehavior: false,
                    cursoropacitymax: 1.0,
                    cursorwidth: 8,
                    cursorborder: "1px solid #000000",
                    cursorborderradius: "1px",
                    grabcursorenabled: true,
                    background: "gray",
                    autohidemode: false
                });
            }

            let scrollbar_canvas = $("#canvas_scroll");
            scrollbar_canvas.getNiceScroll().remove();

            let canvas_window = $('#canvas_window');
            if (that.canvas.width > canvas_window.width() + 10 || that.canvas.height > canvas_window.height() + 10) {
                scrollbar_canvas.niceScroll({
                    touchbehavior: false, cursorcolor: "#000", cursoropacitymin: 0.5, cursoropacitymax: 0.75,
                    cursorwidth: 8, cursorborder: "1px solid #aa0000", cursorborderradius: "1px",
                    grabcursorenabled: true, background: "gray", autohidemode: true
                });
            }
        }
    }

    this.getPreviousRow = function () {
        let current_index = that.array_row.indexOf(that.selected_row);
        if (current_index > 0) return that.array_row[current_index - 1];
        return null;
    };

    this.getNextRow = function () {
        let current_index = that.array_row.indexOf(that.selected_row);
        if (current_index < that.array_row.length - 1) return that.array_row[current_index + 1];
        return null;
    };

    this.getLastRow = function () {
        if (that.array_row.length == that.total_records) {
            let currentLastRow = that.array_row[that.array_row.length - 1];
            if (currentLastRow.state == STATE_GAME_END_ID || currentLastRow.state == STATE_GAME_NOT_FINISHED_ID)
                return that.array_row[that.array_row.length - 1];
        }
        else return null;
    };

    this.setKeyDownEvent = function (callback) {
        that.callback_keydown = callback;
    };
    this.setKeyUpEvent = function (callback) {
        that.callback_keyup = callback;
    };
    this.setRowParseEvent = function (callback) {
        that.callback_row_parse = callback;
    };
    this.setRowCreateEvent = function (callback) {
        that.callback_row_create = callback;
    };
    this.setMouseDownEvent = function (callback) {
        that.callback_mousedown = callback;
    };
    this.setMouseUpEvent = function (callback) {
        that.callback_mouseup = callback;
    };
    this.setMouseMoveEvent = function (callback) {
        that.callback_mousemove = callback;
    };

    this.setRowClickEvent = function (callback) {
        that.callback_row_click = callback;
        //callback(that.array_row[0]);
    };

    this.setDrawLineInfoCallback = function (callback) {
        that.callback_draw_line_info = callback;
    };

    this.setDrawCallback = function (callback) {
        that.callback_draw = callback;
    };
    this.setWinLines = function (WIN_LINES) {
        that.WIN_LINES = WIN_LINES;
    };
    this.setLineColors = function (COLORS) {
        that.COLORS = COLORS;
    };
    this.setLineThickness = function (thickness) {
        that.line_thickness = thickness;
    };

    this.getWinLines = function () {
        return that.WIN_LINES;
    };
    this.getLineColors = function () {
        return that.COLORS;
    };
    this.getLineThickness = function () {
        return that.line_thickness;
    };

    this.drawText = function (string, x, y, isBold, size, color, isCenter, fontname, callback) {
        let text = that.createText(string);
        text.setPosition(x, y);

        let text_isBold = (isBold != undefined) ? isBold : false;
        let text_size = (size != undefined) ? size : 13;
        let text_color = (color != undefined) ? color : COLOR_BLACK;
        let text_isCenter = (isCenter != undefined) ? isCenter : true;
        let text_fontName = (fontname != undefined) ? fontname : FONT_NAME_TIMES_NEW_ROMAN;
        let text_callback = (callback != undefined) ? callback : null;

        text.setFont(text_isBold, text_fontName, text_size, text_color);
        text.setCenter(text_isCenter);
        if (callback != null) text.setClickEvent(text_callback);

        text.draw();

        that.linked_text = getTextUnderCursor();
    };

    this.drawTextShadow = function (string, x, y, offset_x, offset_y, shadow_color, isBold, size, color, isCenter, fontname, callback) {
        let text_isBold = (isBold != undefined) ? isBold : false;
        let text_size = (size != undefined) ? size : 13;
        let text_color = (color != undefined) ? color : COLOR_BLACK;
        let text_isCenter = (isCenter != undefined) ? isCenter : true;
        let text_fontName = (fontname != undefined) ? fontname : FONT_NAME_TIMES_NEW_ROMAN;
        let text_callback = (callback != undefined) ? callback : null;

        that.drawText(string, x + offset_x, y + offset_y, text_isBold, text_size, shadow_color, text_isCenter, text_fontName, null);
        that.drawText(string, x, y, text_isBold, text_size, text_color, text_isCenter, text_fontName, text_callback);
    };

    this.drawTextStroke = function (string, x, y, stroke_size, stroke_color, isBold, size, color, isCenter, fontname, callback) {
        let text_isBold = (isBold != undefined) ? isBold : false;
        let text_size = (size != undefined) ? size : 13;
        let text_color = (color != undefined) ? color : COLOR_BLACK;
        let text_stroke_size = (stroke_size != undefined) ? stroke_size : 0;
        let text_stroke_color = (stroke_color != undefined) ? stroke_color : COLOR_NULL;
        let text_isCenter = (isCenter != undefined) ? isCenter : true;
        let text_fontName = (fontname != undefined) ? fontname : FONT_NAME_TIMES_NEW_ROMAN;
        let text_callback = (callback != undefined) ? callback : null;

        for (let i = 1; i <= text_stroke_size; i++) {
            that.drawText(string, x - i, y, text_isBold, text_size, text_stroke_color, text_isCenter, text_fontName, null);
            that.drawText(string, x + i, y, text_isBold, text_size, text_stroke_color, text_isCenter, text_fontName, null);
            that.drawText(string, x, y + i, text_isBold, text_size, text_stroke_color, text_isCenter, text_fontName, null);
            that.drawText(string, x, y - i, text_isBold, text_size, text_stroke_color, text_isCenter, text_fontName, null);
        }

        that.drawText(string, x, y, text_isBold, text_size, text_color, text_isCenter, text_fontName, text_callback);
    };

    this.drawLine = function (x1, y1, x2, y2, size, color) {
        let text_size = (size) ? size : 2;
        let text_color = (color) ? color : COLOR_BLACK;

        let delta = 0;
        if (text_size % 2 === 1) delta = 0.5;
        that.context.beginPath();
        that.context.lineWidth = text_size;
        that.context.strokeStyle = text_color;
        that.context.fillStyle = text_color;
        that.context.moveTo(x1, y1 + delta);
        that.context.lineTo(x2, y2 + delta);
        that.context.stroke();
        that.context.closePath();
    };

    this.drawPolygon = function (array_position_x, array_position_y, color, border_size, border_color) {
        let param_color = (color != undefined) ? color : COLOR_NULL;
        let param_border_size = (border_size != undefined) ? border_size : 2;
        let param_border_color = (border_color != undefined) ? border_color : COLOR_BLACK;

        let delta = 0;
        if (param_border_size % 2 === 1) delta = 0.5;

        that.context.beginPath();
        that.context.moveTo(array_position_x[0], array_position_y[0] + delta);
        for (let i = 1; i < array_position_x.length; i++)
            that.context.lineTo(array_position_x[i], array_position_y[i] + delta);

        if (param_color !== COLOR_NULL) {
            that.context.fillStyle = param_color;
            that.context.fill();
        }

        if (param_border_size > 0 && param_border_color !== COLOR_NULL) {
            that.context.lineWidth = param_border_size;
            that.context.strokeStyle = param_border_color;
            that.context.stroke();
        }

        that.context.closePath();
    };

    this.drawRectangle = function (x, y, width, height, color, border_size, border_color) {
        let param_color = (color != undefined) ? color : COLOR_NULL;
        let param_border_size = (border_size != undefined) ? border_size : 2;
        let param_border_color = (border_color != undefined) ? border_color : COLOR_BLACK;

        let delta = 0;
        if (param_border_size % 2 === 1) delta = 0.5;

        if (param_color !== COLOR_NULL) {
            that.context.fillStyle = param_color;
            that.context.fillRect(Math.round(x) + delta, Math.round(y) + delta, Math.round(width), Math.round(height));
        }

        if (param_border_size > 0 && param_border_color !== COLOR_NULL) {
            that.context.beginPath();
            that.context.lineWidth = param_border_size;
            that.context.strokeStyle = param_border_color;
            that.context.rect(Math.round(x) + delta, Math.round(y) + delta, Math.round(width), Math.round(height));
            that.context.stroke();
            that.context.closePath();
        }
    };

    this.draw3DRectangle = function (x, y, width, height) {
        that.context.beginPath();
        that.context.fillStyle = COLOR_GRAY_10;
        that.context.fillRect(x, y, width, height);
        that.context.closePath();

        that.drawLine(x, y, x + width, y, 1, COLOR_WHITE);
        that.drawLine(x, y, x, y + height, 1, COLOR_WHITE);

        that.drawLine(x, y + height, x + width, y + height, 1, COLOR_BLACK);
        that.drawLine(x + width, y, x + width, y + height, 1, COLOR_BLACK);
    };

    this.drawCircle = function (center_x, center_y, radius, color, border_size, border_color) {
        let param_color        = (color        != undefined) ? color       : COLOR_NULL;
        let param_border_size  = (border_size  != undefined) ? border_size : 2;
        let param_border_color = (border_color != undefined) ? border_color: COLOR_BLACK;

        that.context.beginPath();
        that.context.arc(Math.round(center_x), Math.round(center_y), Math.round(radius), 0, 2 * Math.PI, false);

        if (param_color !== COLOR_NULL) {
            that.context.fillStyle = param_color;
            that.context.fill();
        }

        if (param_border_size > 0 && border_color !== COLOR_NULL) {
            that.context.lineWidth = param_border_size;
            that.context.strokeStyle = param_border_color;
            that.context.stroke();

        }
        that.context.closePath();
    };

    this.calcArraySum = function (array) {
        if (array == null) return 0;

        let result = 0;
        for (let i = 0; i < array.length; i++) {
            if (!array[i]) continue;
            result += Math.abs(parseInt(array[i], 10));
        }
        return result;
    };

    this.isCanDrawLinesInfo = function () {
        return that.lines_info_state != 2;
    };
    this.endDrawLinesInfo = function () {
        that.lines_info_state = 1;
    };

    this.drawLinesInfo = function (array_payout, COLORS, test_start_p) {
        let info_array_payout = (array_payout != undefined) ? array_payout : that.array_payout;
        let info_COLORS = (COLORS != undefined) ? COLORS : that.COLORS;

        let center_x = 137;
        let text_start = (test_start_p) ? test_start_p : 240;
        let text_step = 21;


        if (that.isCanDrawLinesInfo()) {
            let win_count = 0;

            for (let z = 0; z < info_array_payout.length; z++) {
                let value = that.getSpecialInfoValue(info_array_payout[z]);
                if (parseInt(info_array_payout[z]) > 0 || value != null) win_count += 1;
            }

            let font_size = 13;
            if (win_count > 0) {
                if (win_count >= 20 && win_count <= 23) {
                    font_size = 12;
                    text_step = 17;
                }
                if (win_count >= 24 && win_count <= 27) {
                    font_size = 11;
                    text_step = 15;
                }
                if (win_count >= 28 && win_count <= 30) {
                    font_size = 10;
                    text_step = 13;
                }

                let table_y = that.cursor_pos.y - win_count * text_step / 2;
                if (table_y < 0) table_y = 0;
                that.draw3DRectangle(center_x - 125, table_y, 250, win_count * text_step + 8);

                let index = 0;
                for (let i = 0; i < info_array_payout.length; i++) {
                    if (info_array_payout[i] != 0) {
                        let x = center_x - 50;
                        let y = table_y + (index + 1) * text_step;
                        // Need to look all items

                        let value = that.getSpecialInfoValue(info_array_payout[i]);

                        if (parseInt(info_array_payout[i]) > 0 || value != null) {
                            let drawText = (value != null) ? value : engine.translate("{} credits", info_array_payout[i]);

                            that.drawLine(x - 70, Math.round(y - text_step / 3), x - 45, Math.round(y - text_step / 3), 8, info_COLORS[i % info_COLORS.length]);

                            that.drawTextShadow(engine.translate("Line {}: {}", parseInt(i) + 1, drawText), x - 40, y, 1, 1, COLOR_GRAY_30,
                                true, font_size, COLOR_BLACK, false, FONT_NAME_ARIAL, null);

                            index += 1;
                        }
                    }
                }

                that.endDrawLinesInfo();
            }
        }
    };

    this.getSpecialInfoValue = function (item) {
        if (that.callback_draw_line_info != null)
            return that.callback_draw_line_info(that.getSelectedRow(), item);

        return null;
    };

    this.setPayoutArray = function (payout_array) {
        that.array_payout = payout_array;
    };
    this.getPayoutArray = function () {
        return that.array_payout;
    };

    this.getCanvasWidth = function () {
        return that.canvas.width;
    };
    this.getCanvasHeight = function () {
        return that.canvas.height;
    };

    this.setCanvasSize = function (width, height) {
        that.canvas.width = width;
        that.canvas.height = height;

        setScroll();
    };

    this.getCanvasSize = function () {
        return {width: that.getCanvasWidth(), height: that.getCanvasHeight()};
    };

    this.getCanvasWindowSize = function () {
        let canvas_window = $('#canvas_window');
        return {width: canvas_window.width(), height: canvas_window.height()};
    };

    this.getCurrentGameSessionID = function () {
        return that.current_session_id;
    };

    this.getSelectedRow = function () {
        return that.selected_row;
    };

    this.getCustomerBrandName = function () {
        return that.brandName;
    }

    this.setCustomerBrandName = function(brandName) {
        that.brandName = brandName;
    }
    this.isBelgiumMode = function () {
        return that.belgiumMode;
    }
    this.setBelgiumMode = function(belgiumMode) {
        that.belgiumMode = belgiumMode;
    }

    function keyDown(event) {
        let key = event.which || window.event.keyCode;
        if (that.callback_keydown != null) that.callback_keydown(key);
    }

    function keyUp(event) {
        let key = event.which || window.event.keyCode;
        if (that.callback_keyup != null) that.callback_keyup(key);
    }

    function mouseDown(event) {
        that.cursor_pos = getCursorPosition(event);

        let button = event.which || window.event.keyCode || event.button;

        if ((that.lines_info_state == 2) && (button === MOUSE_LEFT)) {
            that.lines_info_state = 0;
            that.callback_draw(that.selected_row);
        }
    }


    function mouseUp(event) {
        let button = event.which || window.event.keyCode || event.button;

        if ((that.lines_info_state == 1) && (button === MOUSE_LEFT)) that.lines_info_state = 2;
    }
}


function debug(text) {
    let result = ((typeof text === 'string') ? text : JSON.stringify(text));

    let textBox = document.getElementById("debugText");
    if (textBox != null) {
        textBox.value += result;
        textBox.scrollTop = textBox.scrollHeight;
    }

    if (window.console) console.log(result);
}

function getDate(offset_year, offset_month, offset_day) {
    if (offset_year != undefined) offset_year = 0;
    if (offset_month != undefined) offset_month = 0;
    if (offset_day != undefined) offset_day = 0;

    let now = new Date();
    now.setFullYear(now.getFullYear() - offset_year, now.getMonth() - offset_month, now.getDate() - offset_day);
    return now.getFullYear() + '-' + ('0' + (now.getMonth() + 1)).slice(-2) + '-' + ('0' + now.getDate()).slice(-2) + "+" +
        now.getHours() + ':' + ('0' + (now.getMinutes() + 1)).slice(-2) + ':' + ('0' + now.getSeconds()).slice(-2);
}

function debugln(text) {
    let result = ((typeof text === 'string') ? text : JSON.stringify(text));

    debug(result + "\n");
}

$.extend({
    getUrlVars: function () {
        let vars = [], hash;
        let hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for (let i = 0; i < hashes.length; i++) {
            hash = hashes[i].split('=');
            vars.push(hash[0]);
            vars[hash[0]] = hash[1];
        }
        return vars;
    },
    getUrlVar: function (name) {
        return $.getUrlVars()[name];
    }
});

function onKeyboardDown(element, func) {
    $(element).keydown(func);
}

function onKeyboardPress(element, func) {
    $(element).keypress(func);
}

function onKeyboardUp(element, func) {
    $(element).keyup(func);
}

function onMouseDown(element, func) {
    $(element).mousedown(func);
}

function onMouseUp(element, func) {
    $(element).mouseup(func);
}

function onMouseMove(element, func) {
    $(element).mousemove(func)
}

function onTouchDown(element, func) {
    element.addEventListener('touchstart', func);
}

function onTouchUp(element, func) {
    element.addEventListener('touchend', func);
}

function onTouchMove(element, func) {
    element.addEventListener('touchmove', func);
}