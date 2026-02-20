function Texture(texture_path) {
    let that = this;
    this.texture_path = texture_path;
    this.imageObj = null;

    this.load = function (splash, callback) {
        that.imageObj = new Image();
        that.imageObj.src = texture_path + '?rand=' + Math.random();

        debugln("Load image: " + texture_path);

        $(that.imageObj).load(function () {
            debugln("Image \"" + texture_path + "\" loaded!");
            callback();
        });

        $(that.imageObj).error(function () {
            debugln("ERROR! Image \"" + texture_path + "\" not loaded!");
            splash.setLabelActionText(engine.translate("ERROR") + "! " + texture_path);
        });
    };

    this.getWidth = function () {
        return that.imageObj.width;
    };
    this.getHeight = function () {
        return that.imageObj.height;
    };
    this.getTexturePath = function () {
        return that.texture_path;
    };
    this.getImage = function () {
        return that.imageObj;
    };

}


