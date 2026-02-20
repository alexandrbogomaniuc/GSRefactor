/**
 * @module math
 */

/**
 * Specify the sign of the value - obsolete
 * @param {number} val - value
 * @returns {number} 0, 1, -1
 */
function sign(val) {
	if (val == 0) return 0;
	return val > 0 ? 1 : -1;
}

/**
 * Convert degrees to radians - obsolete
 * @param {number} val - value
 * @returns {number}
 */
function grad2radian(val) {
	return val / (180 / Math.PI);
}

/**
 * Convert radians to degrees - obsolete
 * @param {number} val - value
 * @returns {number}
 */
function radian2grad(val) {
	return val * (180 / Math.PI);
}

export { sign, grad2radian, radian2grad }