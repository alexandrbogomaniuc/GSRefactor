/**
 * @typedef {Object} Point
 * @property {number} x - The X Coordinate
 * @property {number} y - The Y Coordinate
 */

/**
 * @class
 * @classdesc Utility to define Bezier curves
 * @abstract
 */
class Bezier {

	/** @ignore */
	static getBasis(i, n, t) {
		function f(n) {
			return (n <= 1) ? 1 : n * f(n - 1);
		}
		return (f(n) / (f(i) * f(n - i))) * Math.pow(t, i) * Math.pow(1 - t, n - i);
	}

	/**
	 * Calculate Bezier curve
	 * @param {Point[]} points - array of points {x:number, y:number}
	 * @param {Number} [step=0.1] - step size (from >0 to 1). The smaller the step, the more often the points on the curve are calculated.
	 * @returns {Point[]} array of point on the curve {x:number, y:number}
	 */
	static getCurve(points, step = 0.1) {
		var res = [];
		step = step / points.length;
		for (var t = 0.0; t < 1 + step; t += step) {
			if (t > 1) t = 1;
			var ind = res.length;
			res[ind] = {x: 0, y: 0};
			for (var i = 0; i < points.length; i++) {
				var b = Bezier.getBasis(i, points.length - 1, t);
				res[ind].x += points[i].x * b;
				res[ind].y += points[i].y * b;
			}
		}
		return res;
	}

	/**
	 * Calculates the length of Bezier curve.
	 * @param {Point[]} curvePoints 
	 * @returns {number}
	 */
	static getCurveLen(curvePoints) {
		var len = 0;
		for (var i = 1, n = curvePoints.length; i < n; i++) {
			len += Math.sqrt(
				Math.pow(curvePoints[i].x - curvePoints[i-1].x, 2)
				+
				Math.pow(curvePoints[i].y - curvePoints[i-1].y, 2)
			);
		}
		return len;
	}

	/**
	 * Get point on a curve according to 
	 * @param {Point[]} curvePoints 
	 * @param {number} pathPartLength 
	 * @returns {Point}
	 */
	static getCurvePoint(curvePoints, pathPartLength)
	{
		var w, h, len=0, pathLen=0, ok=true, i=0, cx=curvePoints[0].x, cy=curvePoints[0].y;

		while(ok) {
			i++;

			if(i >= curvePoints.length) {
				return {x: curvePoints[curvePoints.length-1].x, y: curvePoints[curvePoints.length-1].y};
			}
			else {
				//add the length of the next step
				w = cx - curvePoints[i].x;
				h = cy - curvePoints[i].y;
				len = Math.sqrt(w*w+h*h);

				//out of pathPartLength
				if(pathLen + len >= pathPartLength) {
					//do step back
					var angle = Math.atan2(curvePoints[i].y - cy, curvePoints[i].x - cx);
					len = pathPartLength - pathLen;
					cx += Math.cos(angle) * len;
					cy += Math.sin(angle) * len;
					return {x: cx, y: cy};
				}
				//move to the next point
				else {
					pathLen += len;
					cx = curvePoints[i].x;
					cy = curvePoints[i].y;
				}
			}
		}
	}
}

export { Bezier };