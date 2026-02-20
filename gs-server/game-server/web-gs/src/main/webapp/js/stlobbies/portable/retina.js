/*!
 * Retina.js v1.3.0
 *
 * Copyright 2016 Imulus, LLC
 * Released under the MIT license
 *
 * Retina.js is an open source script that makes it easy to serve
 * high-resolution images to devices with retina displays.
 */

(function () {
    "use strict";

    function isRetina() {
        var mediaQuery = "(-webkit-min-device-pixel-ratio: 1.5), (min--moz-device-pixel-ratio: 1.5), (-o-min-device-pixel-ratio: 3/2), (min-resolution: 1.5dppx)";

        if (window.devicePixelRatio > 1) {
            return true;
        }

        if (window.matchMedia && window.matchMedia(mediaQuery).matches) {
            return true;
        }

        return false;
    }

    function swap(element) {
        var path = element.getAttribute("data-at-2x");

        function load() {
            if (!element.complete) {
                setTimeout(load, 5);
            } else {
                element.setAttribute("src", path);
            }
        }

        load();
    }

    function init() {
        window.addEventListener("load", function () {
            var images = document.getElementsByTagName("img"), imagesLength = images.length, i, image;
            for (i = 0; i < imagesLength; i++) {
                image = images[i];

                if (image.getAttributeNode("data-at-2x")) {
                    swap(image);
                }
            }
        });
    }

    if (isRetina()) {
        init();
    }
})();
