function Text() {
    let that = this;

    let label = null;

    this.x = 0;
    this.y = 0;

    this.canvas = null;
    this.context = null;

    this.text = "";

    this.fontName = FONT_NAME_ARIAL;
    this.fontSize = 20;

    this.color = COLOR_BLACK;
    this.fontString = that.fontSize + "px " + that.fontName;

    this.border_size = 0;
    this.border_color = COLOR_NULL;

    this.isCenter = false;

    this.callback_event_click = null;

    this.setPosition = function (x, y) {
        that.x = Math.round(x);
        that.y = Math.round(y);
    };


    this.getTop = function () {
        return that.y - that.fontSize;
    };
    this.getLeft = function () {
        return that.x - that.getTextWidth() / 2;
    };
    this.getRight = function () {
        return that.x + that.getTextWidth() / 2;
    };
    this.getBottom = function () {
        return that.y;
    };

    this.isContains = function (x, y) {
        if (that.callback_event_click != null) {
            if (x >= that.getLeft() && y >= that.getTop() && x <= that.getRight() && y <= that.getBottom()) {
                document.body.style.cursor = "pointer";
                return true;
            }
            else document.body.style.cursor = "";
        }

        return false;
    };

    this.setClickEvent = function (callback) {
        that.callback_event_click = callback;
    };

    this.onClick = function () {
        if (that.callback_event_click != null) that.callback_event_click();
    };

    this.setFont = function (isBold, name, size, color) {
        this.fontSize = size;
        that.fontString = "";
        if (isBold) that.fontString = "bold ";
        that.fontString += size + "pt " + "'" + name + "'";

        that.setColor(color);
    };

    this.setColor = function (color) {
        that.color = color;
    };

    this.setCenter = function (isCenter) {
        that.isCenter = isCenter;
    };

    this.setBorder = function (border_size, border_color) {
        that.border_size = border_size;
        that.border_color = border_color;
    };

    this.setText = function (text) {
        that.text = text;
    };

    this.getTextWidth = function () {
        return that.context.measureText(that.text).width;
    };

    this.draw = function () {
        if (that.isCenter) that.context.textAlign = "center";
        else that.context.textAlign = "left";

        that.context.font = that.fontString;

        if (that.color != COLOR_NULL) {
            that.context.fillStyle = that.color;
            that.context.fillText(that.text, that.x, that.y);
        }

        if (that.border_size > 0 && that.border_color != COLOR_NULL) {
            that.context.strokeStyle = that.border_color;
            that.context.lineWidth = that.border_size;
            that.context.strokeText(that.text, that.x, that.y);
        }
    }
}