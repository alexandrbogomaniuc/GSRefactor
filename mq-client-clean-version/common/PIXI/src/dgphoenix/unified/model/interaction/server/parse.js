/**
 * @module parse
 */

/**
 * JSON string parser
 */
function json(data) {
	return JSON.parse(data);
}

/**
 * XML string parser
 */
function xml(data) {
	let xml = null;

	if (typeof window.DOMParser != "undefined") {
		xml = (new window.DOMParser()).parseFromString(data, "text/xml");
	}
	else if (typeof window.ActiveXObject != "undefined" && new window.ActiveXObject("Microsoft.XMLDOM")) {
		xml = new window.ActiveXObject("Microsoft.XMLDOM");
		xml.async = "false";
		xml.loadXML(data);
	}
	else {
		throw new Error("No XML parser found");
	}

	return xml;
}

/**
 * Autodetect parser if type is defined
 */
function parse(data, type) {

	if (type instanceof Function) {
		return type(data);
	}

	if (type == 'json') {
		return json(data);
	}

	if (type == 'xml') {
		return xml(data);
	}

	return data;
}

export {json, xml};
export default parse;