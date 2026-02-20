function easeIn(t, b, c, d) {
	return c * (t /= d) * t + b;
}

function easeOut(t, b, c, d) {
	return -c * (t /= d) * (t - 2) + b;
}

function easeInOut(t, b, c, d) {
	if ((t /= d / 2) < 1) return c / 2 * t * t + b;
	return -c / 2 * ((--t) * (t - 2) - 1) + b;
}

function easeOutIn(t, b, c, d) {
	var ts=(t/=d)*t;
	var tc=ts*t;
	return b+c*(-4.5475*tc*ts + 6.2475*ts*ts + 2.4*tc + -5.8*ts + 2.7*t);
}

export {easeIn, easeOut, easeInOut, easeOutIn};