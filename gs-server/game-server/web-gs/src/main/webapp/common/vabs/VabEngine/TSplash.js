function Splash() {
    let that = this;

    this.splash = null;
    this.splash_label_title = null;

    this.splash_loading_bar = null;
    this.splash_loading_bar_current_length = 0;
    this.splash_loading_bar_length = 574;

    this.splash_label_percent = null;
    this.splash_label_percent_child = null;

    this.splash_label_mini_percent = null;
    this.splash_label_mini_percent_child = null;

    this.splash_label_action = null;

    function updateLabelPersent() {
        that.splash_label_percent.innerHTML = that.getProgressBarPersent() + "%";
        that.splash_label_percent.style.marginLeft = -that.splash_label_percent_child.offsetWidth / 2;
        $('#mini_progress_bar').val(that.getProgressBarPersent());

        let mini_progress_window = $('#mini_progress_window');
        let mini_progress = $('#mini_progress_persent');

        mini_progress.text(that.getProgressBarPersent() + "%");

        let mini_progress_element = document.getElementById("mini_progress_persent");
        mini_progress_element.style.marginLeft = (mini_progress_window.width() - mini_progress.width()) / 2;
    }

    this.addToLoadingBar = function (percent) {
        that.splash_loading_bar_current_length += (percent / 100.0) * that.splash_loading_bar_length;
        that.splash_loading_bar.style.width = that.splash_loading_bar_current_length + "px";
        updateLabelPersent();
    };

    this.addToLoadingBarValue = function (value) {
        that.splash_loading_bar_current_length += value;
        that.splash_loading_bar.style.width = that.splash_loading_bar_current_length + "px";
        updateLabelPersent();
    };

    this.setLoadingBar = function (percent) {
        that.splash_loading_bar_current_length = (percent / 100.0) * that.splash_loading_bar_length;
        that.splash_loading_bar.style.width = that.splash_loading_bar_current_length + "px";
        updateLabelPersent();
    };

    this.setLabelActionText = function (text) {
        that.splash_label_action.innerHTML = text;
    };

    this.getProgressBarPersent = function () {
        return Math.round(that.splash_loading_bar_current_length / that.splash_loading_bar_length * 100.0);
    };

    this.create = function (title, callback) {
        let x = 50;
        let y = 50;

        // Splash Image //--------------------------------------------------------------------------------------------//
        that.splash = document.getElementById("splash");

        that.splash.style.position = "absolute";
        that.splash.style.width = "600px";
        that.splash.style.height = "200px";
        that.splash.style.left = x + "%";
        that.splash.style.top = y + "%";
        that.splash.style.marginLeft = -300;
        that.splash.style.marginTop = -100;

        // Label Loading //-------------------------------------------------------------------------------------------//
        that.splash_label_loading = document.createElement("label");
        that.splash_label_loading.style.position = "absolute";
        that.splash_label_loading.style.left = 0;
        that.splash_label_loading.style.top = y + "%";
        that.splash_label_loading.style.width = "100%";
        that.splash_label_loading.style.marginTop = "-100px";
        that.splash_label_loading.style.fontSize = "64px";
        that.splash_label_loading.style.fontFamily = "Times New Roman,serif";
        that.splash_label_loading.style.fontWeight = "bold";
        that.splash_label_loading.style.textAlign = "center";
        that.splash_label_loading.style.color = "white";
        that.splash_label_loading.style.textShadow = "2px 1px 1px #808080";
        that.splash_label_loading.style.zIndex = 10;
        that.splash_label_loading.innerText = engine.translate("Loading").toUpperCase();
        document.body.appendChild(that.splash_label_loading);

        // Label Title //---------------------------------------------------------------------------------------------//
        that.splash_label_title = document.createElement("label");
        that.splash_label_title.style.position = "absolute";
        that.splash_label_title.style.left = 0;
        that.splash_label_title.style.top = y + "%";
        that.splash_label_title.style.width = "100%";
        that.splash_label_title.style.marginTop = "-26px";
        that.splash_label_title.style.fontSize = "22pt";
        that.splash_label_title.style.fontFamily = "Arial";
        that.splash_label_title.style.fontWeight = "bold";
        that.splash_label_title.style.textAlign = "center";
        that.splash_label_title.style.color = "FFD800";
        that.splash_label_title.style.zIndex = 10;
        that.splash_label_title.appendChild(document.createTextNode(title));
        document.body.appendChild(that.splash_label_title);

        // Loading bar //---------------------------------------------------------------------------------------------//
        that.splash_loading_bar = document.getElementById("loading_bar");
        that.splash_loading_bar.style.position = "absolute";
        that.splash_loading_bar.style.left = x + "%";
        that.splash_loading_bar.style.top = y + "%";
        that.splash_loading_bar.style.width = 0;
        that.splash_loading_bar.style.height = 44;
        that.splash_loading_bar.style.marginLeft = -that.splash_loading_bar_length / 2 - 2;
        that.splash_loading_bar.style.marginTop = 10;


        // Label Percent //-------------------------------------------------------------------------------------------//
        that.splash_label_percent = document.createElement("label");
        that.splash_label_percent.style.position = "absolute";
        that.splash_label_percent.style.left = x + "%";
        that.splash_label_percent.style.top = y + "%";
        that.splash_label_percent.style.fontSize = "36pt";
        that.splash_label_percent.style.fontFamily = "Times New Roman";
        that.splash_label_percent.style.fontWeight = "bold";
        that.splash_label_percent.textAlign = "center";
        that.splash_label_percent.style.color = "FFFFFF";
        that.splash_label_percent.style.zIndex = 20;
        that.splash_label_percent.appendChild(document.createTextNode(that.getProgressBarPersent() + "%"));

        that.splash_label_percent_child = document.body.appendChild(that.splash_label_percent);
        that.splash_label_percent_child.style.marginLeft = -that.splash_label_percent_child.offsetWidth / 2;
        that.splash_label_percent_child.style.marginTop = 5;

        // Label Action //--------------------------------------------------------------------------------------------//
        that.splash_label_action = document.createElement("label");
        that.splash_label_action.style.position = "absolute";
        that.splash_label_action.style.left = x + "%";
        that.splash_label_action.style.top = y + "%";
        that.splash_label_action.style.fontSize = "22pt";
        that.splash_label_action.style.fontFamily = "Georgia";
        that.splash_label_action.textAlign = "center";
        that.splash_label_action.style.color = "FFFFFF";
        that.splash_label_action.style.zIndex = 10;
        that.splash_label_action.appendChild(document.createTextNode("Action"));

        let child_action = document.body.appendChild(that.splash_label_action);
        child_action.style.marginTop = 64;
        child_action.style.marginLeft = -280;

        // Wait load splash image /////////////////////////////////////////
        let image = new Image();
        image.onload = function () {
            callback();
        };
        image.onerror = function () {
            callback();
        };
        image.src = '/common/vabs/VabEngine/data/splash.png';
    };

    this.setVisible = function (isVisible, callback) {
        let value = isVisible ? "block" : "none";

        that.splash.style.display = value;
        that.splash_label_loading.style.display = value;
        that.splash_label_title.style.display = value;
        that.splash_loading_bar.style.display = value;
        that.splash_label_percent.style.display = value;
        that.splash_label_action.style.display = value;

        if (callback != null) setTimeout(callback, 10);
    }
}