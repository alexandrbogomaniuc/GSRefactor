export * from 'pixi-filters';

/**
 * Canvas filters utils.
 */
export class Filters {

	//https://www.html5rocks.com/en/tutorials/canvas/imagefilters/
	/**
	 * Get pixel data of canvas.
	 * @param {canvas} canvas 
	 * @returns {ImageData}
	 * @static
	 */
	static getPixelsFromCanvas(canvas)
	{
		let ctx = canvas.getContext('2d');
		return ctx.getImageData(0, 0, canvas.width, canvas.height);
	}

	/**
	 * Get pixel data of image.
	 * @param {*} img 
	 * @returns {ImageData}
	 * @static
	 */
	static getPixels(img) {
		var c = this.getCanvas(img.width, img.height);
		var ctx = c.getContext('2d');
		ctx.drawImage(img, 0, 0, c.width, c.height);
		return ctx.getImageData(0,0,c.width,c.height);
	};

	/**
	 * Create new canvas element.
	 * @param {width} w 
	 * @param {height} h 
	 * @returns {canvas}
	 * @static
	 */
	static getCanvas(w,h) {
		var c = document.createElement('canvas');
		c.width = w;
		c.height = h;
		return c;
	};

	/**
	 * Apply filters to image.
	 * @param {*} filter 
	 * @param {*} image 
	 * @param {*} var_args 
	 * @returns {*}
	 * @static 
	 */
	static filterImage(filter, image, var_args) {
		var args = [this.getPixels(image)];
		for (var i=2; i<arguments.length; i++) {
		args.push(arguments[i]);
		}
		return filter.apply(null, args);
	};

	/**
	 * Add grayscale filter.
	 * @param {ImageData} pixels 
	 * @param {*} args 
	 * @returns {ImageData}
	 * @static
	 */
	static grayscale(pixels, args) {
		var d = pixels.data;
		for (var i=0; i<d.length; i+=4) {
		var r = d[i];
		var g = d[i+1];
		var b = d[i+2];
		// CIE luminance for the RGB
		// The human eye is bad at seeing red and blue, so we de-emphasize them.
		var v = 0.2126*r + 0.7152*g + 0.0722*b;
		d[i] = d[i+1] = d[i+2] = v
		}
		return pixels;
	};

}
