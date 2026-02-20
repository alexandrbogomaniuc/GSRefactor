function easeIn(t, b, c, d) {
	var mt = (t / d) / 2;
	return (2 * (mt * mt * (3 - 2 * mt))) * c + b;
}

function easeOut(t, b, c, d) {
	var mt = ((t / d) + 1) / 2;
	return ((2 * (mt * mt * (3 - 2 * mt))) - 1) * c + b;
}

function easeInOut(t, b, c, d) {
	var mt = (t / d);
	return (mt * mt * (3 - 2 * mt)) * c + b;
}

export {easeIn, easeOut, easeInOut};