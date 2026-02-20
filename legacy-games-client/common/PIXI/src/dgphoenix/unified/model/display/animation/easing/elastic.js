function easeIn(t, b, c, d) {
	if (c == 0) return b;
	var s = 1.70158, p = 0, a = c * 1;
	if (t == 0) return b;
	if ((t /= d) == 1) return b + c;
	if (!p) p = d * .3;
	if (a < Math.abs(c)) {
		a = c * 1;
		s = p / 4;
	}
	else s = p / (2 * Math.PI) * Math.asin(c / a);
	return -(a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
}

function easeOut(t, b, c, d) {
	if (c == 0) return b;
	var s = 1.70158, p = 0, a = c * 1;
	if (t == 0) return b;
	if ((t /= d) == 1) return b + c;
	if (!p) p = d * .3;
	if (a < Math.abs(c)) {
		a = c * 1;
		s = p / 4;
	}
	else s = p / (2 * Math.PI) * Math.asin(c / a);
	return a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
}

function easeInOut(t, b, c, d) {
	if (c == 0) return b;
	var s = 1.70158, p = 0, a = c * 1;
	if (t == 0) return b;
	if ((t /= d / 2) == 2) return b + c;
	if (!p) p = d * (.3 * 1.5);
	if (a < Math.abs(c)) {
		a = c * 1;
		s = p / 4;
	}
	else s = p / (2 * Math.PI) * Math.asin(c / a);
	return (t < 1)
		? -.5 * (a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b
		: a * Math.pow(2, -10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p) * .5 + c + b
		;
}

export {easeIn, easeOut, easeInOut};