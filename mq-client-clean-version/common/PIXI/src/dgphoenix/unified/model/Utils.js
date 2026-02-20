/**
 * Copy properties from one object to another.
 * @param {Object} objFrom - Source object.
 * @param {Object} objTo - Target object.
 */
function copyObjectProps(objFrom, objTo) {
	for(var i in objFrom) {
		if(!objFrom.hasOwnProperty(i)) continue;

		if(Array.isArray(objFrom[i])) {
			objTo[i] = [];
			for(var n=0; n<objFrom[i].length; n++) {
				if(typeof objFrom[i][n] == "object" && objFrom[i][n] !== null) {
					objTo[i][n] = cloneEmptyObject(objFrom[i][n]);
					copyObjectProps(objFrom[i][n], objTo[i][n]);
				}
				else objTo[i][n] = objFrom[i][n];
			}
			continue;
		}

		if(isPlainObject(objFrom[i])) {
			objTo[i] = {};
			copyObjectProps(objFrom[i], objTo[i]);
			continue;
		}

		objTo[i] = objFrom[i];
	}
}

/**
 * Clone object without copying its properties.
 * @param {*} obj - Source object.
 * @returns {Object} - Cloned empty Object.
 */
function cloneEmptyObject(obj) {
	if(obj.constructor) return new obj.constructor();
	return {};
}

/**
 * Checks whether object's type is Object or not.
 * @param {*} obj 
 * @returns {boolean}
 */
function isPlainObject(obj) {
	if(!obj || !obj.constructor) return false;
	return obj.constructor === Object;
}

/**
 * Set of utility methods
 */
export class Utils {

	/**
	 * @description Parse GET-params of the page
	 * @param {string} [aOptLocation_str=undefined] - Location url to parse. If omitted - window.location will be used.
	 * @returns {Object} Object of GET-params (key-value)
	 * @static
	 */
	static parseGet(aOptLocation_str = undefined) {

		var get = {};
		var s;

		if (aOptLocation_str == undefined)
		{
			s = window.location.search.length && window.location.search.substr(1).split('&');
		}
		else
		{
			let p = aOptLocation_str.indexOf("?");
			if (p >= 0)
			{
				s = aOptLocation_str.substr(p + 1, aOptLocation_str.length).split('&');
			}
			
		}

		s = s || [];

		for (let param of s) {
			let [key, ...value] = param.split('=');
			get[key] = value.join('=');
		}
		return get;
	}

	/**
	 * @description Binding a function to a context.
	 * @param {Function} fn
	 * @param {Object} context
	 * @param {...*} proxyArgs
	 * @static
	 */
	static proxy(fn, context, ...proxyArgs) {
		return function (...args) {
			return fn.apply(context || this, args.concat(proxyArgs));
		};
	}

	/**
	 * Clone object
	 * @param {mixed} obj - Source object to clone
	 * @static
	 */
	static clone(obj) {
		if(!obj || (typeof obj != "object")) return obj;

		var clone = cloneEmptyObject(obj);
		copyObjectProps(obj, clone);

		return clone;
	}

	/**
	 * Clone array.
	 * @param {*} arr - Source array.
	 * @returns {Array}
	 * @static
	 */
	static cloneArray(arr)
	{
		let result = [];
		for (let item of arr)
		{
			result.push(Utils.clone(item));
		}
		return result;
	}

	/**
	 * Convert points array to string.
	 * @param {Object[]} aPoints_arr - Array of points.
	 * @param {number} aCountNumber_int - Amount of points to convert to string.
	 * @returns {string}
	 * @static
	 */
	static pointsArrayToString(aPoints_arr, aCountNumber_int = -1) {
		let s = '\n';
		let n = aPoints_arr.length - 1;
		if (aCountNumber_int > 0)
		{
			n = Math.min(aCountNumber_int, n);
		}
		for (let i=0; i<=n; i++)
		{
			let lPoint_pt = aPoints_arr[i];
			s += Utils.pointToString(lPoint_pt);
			s += ",\n";
		}
		return s;
	}

	/**
	 * Convert point to string.
	 * @param {Object} aPoint_pt - Source point.
	 * @returns {string}
	 * @static
	 */
	static pointToString(aPoint_pt)
	{
		let lPoint_pt = aPoint_pt;
		let s = "{\t";
		for (let prop in lPoint_pt)
		{
			s += prop + ":" + lPoint_pt[prop] + ",\t\t\t\t\t";
		}
		s += " }";
		return s;
	}

	/**
	 * Convert bounds object to string.
	 * @param {Object} aBounds_obj - Source bounds.
	 * @returns {string}
	 * @static
	 */
	static boundsToString(aBounds_obj)
	{
		let s = "{ ";
		for (let prop in aBounds_obj)
		{
			if (typeof aBounds_obj[prop] != "object" && typeof aBounds_obj[prop] != "function")
				s += prop + ": " + aBounds_obj[prop] + ", ";
		}
		s += " }";
		return s;
	}
	
	/**
	 * Shuffle array elements in random order.
	 * @param {Array} arr
	 * @returns {Array}
	 * @static
	 */
	static shuffleArray(arr) {
		let cnt = arr.length;

		while (cnt > 0) {
			let ix = Math.floor(Math.random() * cnt);
			cnt--;
			let temp = arr[cnt];
			arr[cnt] = arr[ix];
			arr[ix] = temp;
		}

		return arr;
	}

	/**
	 * Stop event propagation.
	 * @static
	 */
	static preventInteractiveEvent(e) {
		if(e && e.stopPropagation) e.stopPropagation();
		return false;
	}

	/**
	 * Is integer value.
	 * @static
	 */
	static isInt(aValue_obj)
	{
		return (aValue_obj === aValue_obj - 0)  //peformance optimized atom number check
				&&
				(
					aValue_obj === (aValue_obj | 0) //int32 range optimized algo
					|| (aValue_obj === Math.floor(aValue_obj)) //out of int32 range algo
				);
	}

	/**
	 * Is number value.
	 * @static
 	 */
	static isNumber(aValue_obj)
	{
		return (aValue_obj === aValue_obj - 0);
	}

	/**
	 * Is string value. 
	 * @static
	 */
	static isString(aValue_obj)
	{
		return (typeof aValue_obj === 'string');
	}

	/**
	 * Remove not allowed chars from the string.
	 * @param {string} source_str - Source string.
	 * @param {string} restrictedGlyphs - Array of allowed chars.
	 * @param {boolean} caseSensitive_bl - Is case sensitive filter or not.
	 * @returns {string} - A new string containing only allowed chars.
	 * @static
	 */
	static filterGlyphs(source_str, restrictedGlyphs, caseSensitive_bl = false)
	{
		if (!restrictedGlyphs)
		{
			return source_str;
		}

		caseSensitive_bl = !!caseSensitive_bl;

		let filtered_str = "";
		for (let i = 0; i < source_str.length; i++)
		{
			let char = source_str[i];
			
			let isCharAcceptable = !!~restrictedGlyphs.indexOf(char);
			if (!caseSensitive_bl)
			{
				isCharAcceptable = !!~restrictedGlyphs.indexOf(char.toUpperCase());
				isCharAcceptable = isCharAcceptable || !!~restrictedGlyphs.indexOf(char.toLowerCase());
			}

			if (isCharAcceptable)
			{
				filtered_str += char;
			}
		}

		return filtered_str;
	}

	/**
	 * Is boolean value or not.
	 * @param {*} aValue_obj 
	 * @returns {boolean}
	 * @static
	 */
	static isBoolean(aValue_obj)
	{
		return typeof (aValue_obj) === "boolean";
	}

	/**
	 * Convert degree to radian.
	 * @param {number} grad
	 * @returns {number}
	 * @static
	 */
	static gradToRad(grad)
	{
		return grad * Math.PI / 180;
	}

	/**
	 * Convert radian to degree.
	 * @param {number} rad 
	 * @returns {number}
	 * @static
	 */
	static radToGrad(rad)
	{
		return rad * 180 / Math.PI;
	}

	/**
	 * Get cosine of angle between 3 points.
	 * @param {*} pointA 
	 * @param {*} pointB 
	 * @param {*} pointC 
	 * @returns {number}
	 * @static
	 */
	static cosABC(pointA, pointB, pointC)
	{
		let vectorAB = {x: (pointB.x-pointA.x), y: (pointB.y-pointA.y)};
		let vectorCB = {x: (pointB.x-pointC.x), y: (pointB.y-pointC.y)};
		
		let cosABC = (vectorAB.x*vectorCB.x + vectorAB.y*vectorCB.y)/(Math.sqrt(vectorAB.x*vectorAB.x+vectorAB.y*vectorAB.y) * Math.sqrt(vectorCB.x*vectorCB.x+vectorCB.y*vectorCB.y));
		
		return cosABC;
	}

	/**
	 * Get distance between 2 points.
	 * @param {*} pt1 
	 * @param {*} pt2 
	 * @returns {number}
	 * @static
	 */
	static getDistance(pt1, pt2) {
		if (!pt1 || !pt2)
		{
			return undefined;
		}
		return Math.sqrt(Math.pow((pt1.x - pt2.x), 2) + Math.pow((pt1.y - pt2.y), 2));
	}

	/**
	 * Get distance between 2 points.
	 * @param {*} x1 
	 * @param {*} y1 
	 * @param {*} x2 
	 * @param {*} y2 
	 * @returns {number}
	 * @static
	 */
	static getDistance2(x1, y1, x2, y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	/**
	 * Returns the angle of line (in radians).
	 * @param {*} startPos 
	 * @param {*} endPos 
	 * @returns {number}
	 * @static
	 */
	static getAngle(startPos, endPos) {
		return Math.atan2(endPos.x - startPos.x, endPos.y - startPos.y);
	}

	/**
	 * Returns normalized angle (in radians). Value is between 0 and Math.PI*2.
	 * @param {*} startPos 
	 * @param {*} endPos 
	 * @returns {number}
	 * @static
	 */
	static getNormalizedAngle(startPos, endPos)
	{
		let angle = Utils.getAngle(startPos, endPos);
		let limit = Math.PI * 2;
		while(angle < 0) angle += limit;
		while(angle > limit) angle -= limit;
		return angle;
	}

	/**
	 * Get random value between n and m.
	 * @param {number} n 
	 * @param {number} m 
	 * @param {boolean} noRound - If false - result value will be rounded.
	 * @returns {number}
	 * @static
	 */
	static random(n, m, noRound) {
		if (n && (n instanceof Array)) {
			var idx = Math.round(random(0, n.length-1));
			return n.length ? n[idx] : undefined;
		}

		var rnd = Math.random();
		if (n === undefined) return rnd;
		if (m === undefined) m = 0;
		rnd *= Math.abs(n-m);
		rnd = Math.min(n, m) + rnd;

		return noRound ? rnd : Math.round(rnd);
	};

	/**
	 * @deprecated
	 */
	static parseAngle(direction){
		var angle;
		if(direction == 90) angle = Math.PI / 6.8;
		if(direction == 0) angle = Math.PI - Math.PI / 6.8;
		if(direction == 270) angle = Math.PI + Math.PI / 6.8;
		if(direction == 180) angle = Math.PI*2 - Math.PI / 6.8;

		return angle;
	};

	/**
	 * Checks whether provided points are equal or not.
	 * @param  {...any} points 
	 * @returns {boolean}
	 * @static
	 */
	static isEqualPoints(...points)
	{
		for (let i=0; i<points.length - 1; i++)
		{
			if (!points[i] || !points[i+1])
				return (points[i] === points[i+1]);
			if (points[i].x != points[i+1].x
				|| points[i].y != points[i+1].y)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether provided trajectory points are equal or not.
	 * Equal trajectory points have equal coordinate and time.
	 * @param  {...any} points Trajectory points.
	 * @returns {boolean}
	 * @static
	 */
	static isEqualTrajectoryPoints(...points)
	{
		for (let i=0; i<points.length - 1; i++)
		{
			if (!points[i] || !points[i+1])
				return (points[i] === points[i+1]);
			if (points[i].x != points[i+1].x
				|| points[i].y != points[i+1].y)
			{
				return false;
			}
			if (points[i].time !== points[i+1].time)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether point is inside polygon or not.
	 * @param {*} point 
	 * @param {*} polygon 
	 * @returns {boolean}
	 * @static
	 */
	static isPointInsidePolygon(point, polygon)
	{
		return polygon.contains(point.x, point.y);
		// ray-casting algorithm based on
		// http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html

		/*var x = point.x, y = point.x;

		var inside = false;
		for (var i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
			var xi = polygon[i].x, yi = polygon[i].y;
			var xj = polygon[j].x, yj = polygon[j].y;

			var intersect = ((yi > y) != (yj > y))
				&& (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
			if (intersect) inside = !inside;
		}

		return inside;*/
	}

	/**
	 * Checks whether a point is inside a rectangle or not.
	 * Rectangle is set by coordinate of left top corner and its width/height.
	 * @param {*} rect 
	 * @param {*} point 
	 * @returns {boolean} 
	 * @static
	 */
	static isPointInsideRect(rect, point)
	{
	    if (
	            point.x < rect.x
	            || point.x > rect.x+rect.width
	            || point.y < rect.y
	            || point.y > rect.y+rect.height
	        )
	    {
	        return false;
	    }

	    return true;
	}

	/**
	 * Get random value within the maximum available offset from the base value.
	 * @param {number} baseValue 
	 * @param {number} maxStepValue 
	 * @returns {number}
	 * @static
	 */
	static getRandomWiggledValue(baseValue, maxStepValue)
	{
		let stepDirection = Math.random() > 0.5 ? 1 : -1;
		let randomStep = Math.random()*maxStepValue;

		return baseValue + stepDirection*randomStep;
	}

	/**
	 * Checks if object has no properties.
	 * @param {*} obj 
	 * @returns {boolean}
	 * @static
	 */
	static isEmptyObject(obj)
	{
		return (Object.keys(obj).length === 0 && obj.constructor === Object);
	}

	/**
	 * Get point of intersection of two lines.
	 * @param {Point} p1 
	 * @param {Point} p2 
	 * @param {Point} p3 
	 * @param {Point} p4 
	 * @returns {Point}
	 * @static
	 */
	static getIntersectionBetweenTwoLines(p1, p2, p3, p4)
	{
		var d1 = (p1.x - p2.x) * (p3.y - p4.y);
		var d2 = (p1.y - p2.y) * (p3.x - p4.x);
		var d  = (d1) - (d2);

		if (d == 0) return null;

		var u1 = (p1.x * p2.y - p1.y * p2.x);
		var u4 = (p3.x * p4.y - p3.y * p4.x);
		var u2x = p3.x - p4.x;
		var u3x = p1.x - p2.x;
		var u2y = p3.y - p4.y;
		var u3y = p1.y - p2.y;
		var px = (u1 * u2x - u3x * u4) / d;
		var py = (u1 * u2y - u3y * u4) / d;
		var p = { x: px, y: py };

		return p;
	}

	/**
	 * Get texture by name.
	 * @param {PIXI.Texture[]} textures 
	 * @param {string} name 
	 * @returns {PIXI.Texture}
	 * @static
	 */
	static getTexture(textures, name)
	{
		if (!textures || !textures.length) return null;

		for (let i = 0; i < textures.length; i++)
		{
			if (textures[i]._atlasName == name)
			{
				return textures[i];
			}
		}

		return null;
	}

	static getDecimalRGBColorsArray(aColor, aOptBase_num=1)
	{
		if (Utils.isNumber(aColor))
		{
			aColor = StringUtils.DecToHexString(aColor);
		}

		aColor = aColor.replace('0x', '')
		return aColor.match(/\w\w/g).map((a_num) => parseInt(a_num, 16)/256*aOptBase_num);
	}
}

/**
 * Set of utility methods for string.
 */
export class StringUtils {

	/**
	 * Replace all string inclusions. Source string does not modify.
	 * @param {string} sourceString 
	 * @param {string} expr 
	 * @param {string} newExpr 
	 * @returns {string}
	 * @static
	 */
	static replaceAll(sourceString, expr, newExpr)
	{
		let resultString = sourceString.slice(0);
		while (resultString.indexOf(expr) > -1)
		{
			resultString = resultString.replace(expr, newExpr);
		}

		return resultString;
	}

	/**
	 * Converts number from decimal to hex.
	 * @param {string|number} a_num 
	 * @returns {string}
	 * @static 
	 */
	static DecToHexString(a_num)
	{
		if (Utils.isNumber(a_num))
		{
			return a_num.toString(16);
		}
		else if (Utils.isString(a_num))
		{
			return parseInt(a_num, 10).toString(16);
		}
	}

	/**
	 * Copy string to clipboard.
	 * @param {string} aText_str 
	 * @static
	 */
	static copyToClipBoard(aText_str)
	{
		var el = document.createElement('textarea');
		el.value = aText_str;
		document.body.appendChild(el);

		if (navigator.userAgent.match(/ipad|ipod|iphone/i)) 
		{
			var editable = el.contentEditable;
			var readOnly = el.readOnly;

			// convert to editable with readonly to stop iOS keyboard opening
			el.contentEditable = true;
			el.readOnly = true;

			// create a selectable range
			var range = document.createRange();
			range.selectNodeContents(el);

			// select the range
			var selection = window.getSelection();
			selection.removeAllRanges();
			selection.addRange(range);
			el.setSelectionRange(0, 999999);

			// restore contentEditable/readOnly to original state
			el.contentEditable = editable;
			el.readOnly = readOnly;
		}
		else
		{
			el.select();
		}
		document.execCommand('copy');
		document.body.removeChild(el);
	}
}

