/**
 * Helper utilities and cross-browser polyfills
 * @module helpers
 */

/**
 * Add event handler to a DOM element
 * @param {HTMLElement} el DOM element
 * @param {string} eventName Event name (without 'on' prefix)
 * @param {function} eventHandler Event handler
 */
function bindEvent(el, eventName, eventHandler) {
	if (el.addEventListener) {
		el.addEventListener(eventName, eventHandler, false);
	}
	else if (el.attachEvent) {
		el.attachEvent('on' + eventName.toLowerCase(), eventHandler);
	}
}

/**
 * Remove event handler for DOM-element
 * @param {HTMLElement} el DOM element
 * @param {string} eventName Event name (without 'on' prefix)
 * @param {function} eventHandler Event handler
 */
function unbindEvent(el, eventName, eventHandler) {
	if (el.removeEventListener) {
		el.removeEventListener(eventName, eventHandler, false);
	}
	else if (el.detachEvent) {
		el.detachEvent('on' + eventName, eventHandler);
	}
}

/**
 * @description Prevent any browser event
 * @param {Event} e Event
 */
function preventEvent(e) {
	e.preventDefault();
	e.stopPropagation();
	e.cancelBubble = true;
	e.returnValue = false;
	return false;
}

/**
 * Get absolute left-top position of DOM-element
 * @param {HTMLElement} element DOM-element
 * @returns {{x:0, y:0}} position coordinates
 */
function getElementPosition(element) {
	let el = element, result = {x: 0, y: 0};
	while (el) {
		result.x += el.offsetLeft || 0;
		result.y += el.offsetTop || 0;
		el = el.offsetParent;
	}
	return result;
}

/**
 * Get mouse coordinates relative to an object
 * @param {Event} event Browser event
 * @param {HTMLElement} [element] Target DOM-element
 * @returns {{x:number, y:number}} coordinates
 */
function getPointerCoordinates(event, element) {
	let e = event || window.event,
		ret = getElementPosition(element);

	e = (e && e.touches && e.touches[0]) || e;
	if (e) {

		let x = 0, y = 0;
		if (('pageX' in e) && ('pageY' in e)) {
			x = e.pageX;
			y = e.pageY;
		}
		else if (('clientX' in e) && ('clientY' in e)) {
			x = e.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft) - document.documentElement.clientLeft;
			y = e.clientY + (document.documentElement.scrollTop || document.body.scrollTop) - document.documentElement.clientTop;
		}
		ret.x = x - ret.x;
		ret.y = y - ret.y;
	}
	return ret;

}

export { bindEvent, unbindEvent, preventEvent, getElementPosition, getPointerCoordinates }