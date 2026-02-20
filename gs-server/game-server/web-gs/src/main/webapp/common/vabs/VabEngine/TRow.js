function TRow() {
    let that = this;

    this.text = "";

    this.table_element = null;  // Stupid IE8 don't supports "document.getElementsByClassName"; It's link to row in table;
    this.date = "";
    this.roundID = 5856972;
    this.state = 0;
    this.state_text = "";
    this.bet = 0.00;
    this.payout = 0.00;
    this.balance = 0.00;
    this.extBetId = "";

    this.coin = 0.00;

    this.publicText = "";
    this.privateText = "";

    this.publicData = new HashMap();
    this.privateData = new HashMap();

    function addParameter(hashMap, param) {
        if (!param || !param.trim()) return;

        let param_value = splitWithTail(param, "=", 1);
        hashMap.set(param_value[0], param_value[1]);
    }

    this.getValue = function (param) {
        let value = that.publicData.get(param);
        if (value == null) value = that.privateData.get(param);

        if (value != null) return value;

        return null;
    };

    this.getPrivateValue = function (param) {
        return that.privateData.get(param);
    };
    this.getPublicValue = function (param) {
        return that.publicData.get(param);
    };

    this.getValueArray = function (param) {
        return toArray(that.getValue(param), arguments);
    };
    this.getPrivateValueArray = function (param) {
        return toArray(that.privateData.get(param), arguments);
    };
    this.getPublicValueArray = function (param) {
        return toArray(that.publicData.get(param), arguments);
    };

    function splitWithTail(str, delim, count) {
        let parts = str.split(delim);
        let tail = parts.slice(count).join(delim);
        let result = parts.slice(0, count);
        result.push(tail);
        return result;
    }

    function toArray(value, args) {
        if (value == null) {
            return null;
        }
        if (args.length > 1) {
            let temp_delimiter = "@#@";
            for (let i = 1; i < args.length; i++) {
                value = value.replace(args[i], temp_delimiter);
            }
            return value.split(temp_delimiter);
        } else {
            return value.split("|");
        }
    }

    this.parse = function (line) {
        that.text = line;

        let params = splitWithTail(line, "+", 7);

        that.date = params[0].split("=")[1];
        that.state = parseInt(params[1], 10);
        that.state_text = params[2];
        that.bet = parseFloat(params[3]);
        that.payout = parseFloat(params[4]);
        that.balance = parseFloat(params[5]);

        that.endParse(params[6], params[7]);
    };

    this.parseJson = function (line) {
        that.text = line;

        that.date = line.time;
        that.state = line.stateId;
        that.state_text = line.stateName;
        that.extBetId = line.extBetId;
        that.bet = line.bet;
        that.payout = line.win;
        that.balance = line.balance;

        that.endParse(line.betData, line.servletData);
    };

    this.endParse = function (publicText, privateText) {
        that.publicText = publicText;
        that.privateText = privateText;

        let publicParams = that.publicText.split("~");
        let privateParams = that.privateText.split("~");

        that.publicData.clear();
        that.privateData.clear();

        for (let i = 0; i < publicParams.length; i++) addParameter(that.publicData, publicParams[i]);
        for (let i = 0; i < privateParams.length; i++) addParameter(that.privateData, privateParams[i]);

        that.roundID = that.getValue("ROUND_ID");
        if (tryParseInt(that.roundID) == null) that.roundID = "";
    };

    this.getText = function () {
        return that.text;
    };
    this.getDate = function () {
        return that.date;
    };
    this.getState = function () {
        return that.state;
    };
    this.getRoundID = function () {
        return that.roundID;
    };
    this.getExtBetId = function () {
        return that.extBetId;
    };
    this.getBet = function () {
        return that.bet.toFixed(2);
    };
    this.getStateText = function () {
        return that.state_text;
    };
    this.getPayout = function () {
        return that.payout.toFixed(2);
    };
    this.getBalance = function () {
        return that.balance.toFixed(2);
    };

    this.getCoin = function () {
        let bets = that.getValueArray("COINSEQ");
        let coinID = that.getValue("BETID");

        if (bets == null || coinID == null) return null;
        that.coin = parseFloat(bets[coinID]);

        return that.coin.toFixed(2);
    };

    this.getPublicText = function () {
        return that.publicText;
    };
    this.getPrivateText = function () {
        return that.privateText;
    };

    this.setDate = function (text) {
        that.date = text;
        that.table_element.getElementsByTagName("td").namedItem("date").innerHTML = text;
    };
    this.setRoundID = function (text) {
        that.roundID = text;
        that.table_element.getElementsByTagName("td").namedItem("roundId").innerHTML = text;
    };
    this.setBet = function (text) {
        that.bet = text;
        that.table_element.getElementsByTagName("td").namedItem("bet").innerHTML = text.toFixed(2);
    };
    this.setStateText = function (text) {
        that.state_text = text;
        that.table_element.getElementsByTagName("td").namedItem("state").innerHTML = engine.translate(text);
    };
    this.setPayout = function (text) {
        that.payout = text;
        that.table_element.getElementsByTagName("td").namedItem("payout").innerHTML = text.toFixed(2);
    };
    this.setBalance = function (text) {
        that.balance = text;
        that.table_element.getElementsByTagName("td").namedItem("balance").innerHTML = text.toFixed(2);
    };

    this.getStateIDasText = function () {
        switch (that.state) {
            case STATE_ROUND_COMPLETE_ID:
                return "Place Bet";
            case STATE_GAME_END_ID:
                return "Game End";
            case STATE_GAME_NOT_FINISHED_ID:
                return "Not Finished";
            case STATE_GAME_CONTINUATION_ID:
                return "Continuation";
            case STATE_DOUBLE_UP_ID:
                return "Double Up";
            default:
                return "";
        }
    };

    this.setFilter = function (min, max) {
        let RID = 999999999999;
        if (that.roundID) RID = parseInt(that.roundID);

        if (RID < min || RID > max) {
            that.table_element.style.display = "none";
        }
        else {
            that.table_element.style.display = "";
        }
    };
}