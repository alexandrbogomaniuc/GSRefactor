import * as FEATURES from '../../../view/layout/features';
import {default as parse} from '../../../model/interaction/server/parse';

function createHTTPQuery(params) {
	let query = params || '';
	if (query && ('string' != typeof query)) {
		query = [];
		for (let key of Object.keys(params)) {
			query.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`);
		}
		query = query.join('&');
	}
	return query;
}

function isSameDomain(url1, url2) {
	let a = document.createElement('a'), domain = '';
	a.src = url1;
	domain = a.hostname;
	a.src = url2;
	return a.hostname == domain;
}

function isXDomain(url) {
	return (FEATURES.IE && window.XDomainRequest && !document.addEventListener && !isSameDomain(url, window.location.href))
}

function createXMLHTTPRequest(url, callback, dataType) {

	let xDomain = isXDomain(url);
	let xmlhttp = (xDomain && new XDomainRequest())
		|| (window.XMLHttpRequest && new XMLHttpRequest())
		|| (window.ActiveXObject && new ActiveXObject("Microsoft.XMLHTTP"))
		|| null;

	if (xmlhttp && (callback instanceof Function)) {

		if (xDomain) {
			xmlhttp.onload = function (...args) {
				let status = 'success', errorMessage = null;
				try {
					let data = parse(xmlhttp.responseText, dataType);
				}
				catch (e) {
					status = 'error';
					errorMessage = `${url}\n\tParse error: ${e.message}`;
				}
				callback(data, status, xmlhttp, errorMessage);
			};
			xmlhttp.onerror = function () {
				callback(null, 'error', xmlhttp, `${url}\n\tHTTP Error ${xmlhttp.status}: ${xmlhttp.statusText}`);
			};
			xmlhttp.ontimeout = function () {
				console.dir(args);
				callback(null, 'error', xmlhttp, `${url}\n\tRequest timeout`);
			}
		}
		else {
			// TODO: handle request timeouts (setTimeout is bad variant!)
			xmlhttp.onreadystatechange = function () {
				if (xmlhttp.readyState == 4) {
					let data = null, status = 'success', errorMessage = null;
					// not error
					if (xmlhttp.status == 200) {
						if (dataType == 'arraybuffer') {
							data = xmlhttp.response; // special case
						}
						if (dataType == 'xml') {
							data = xmlhttp.responseXML; // try to use browser native parsing
						}
						if (!data && xmlhttp.responseText) {
							data = parse(xmlhttp.responseText, dataType);
						}
					}
					else {
						status = 'error';
						errorMessage = `${url}\n\tHTTP Error ${xmlhttp.status}: ${xmlhttp.statusText}`;
					}
					callback(data, status, xmlhttp, errorMessage);
				}
			};
		}

	}
	return xmlhttp;
}

/**
 * @module ajax
 */

/**
 * AJAX-request
 * @param {String} url
 * @param {String} [method="GET"] HTTP method
 * @param {Object|String} [params=""] request params (kew-value)
 * @param {Function} [callback=null]
 * @param {String|Function} [type="text"] expected dfata type (text, xml or json)
 */
function send(url, method = "GET", params = "", callback = null, dataType = "text") {

	let xmlhttp = createXMLHTTPRequest(url, callback, dataType);
	if (xmlhttp) {
		let query = createHTTPQuery(params);

		if(method == "GET" && query) url += "?" + query;

		xmlhttp.open(method, url, true);

		if (dataType == 'arraybuffer') {
			xmlhttp.responseType = 'arraybuffer';
		}

		if (method == "POST" && !isXDomain(url)) {
			xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		}
		xmlhttp.send(method == "GET" ? null : query);
	}
	else {
		if (callback) {
			callback(null, 'error', null, 'Cannot create XHR instance');
		}
	}
}

/**
 * GET-request
 * @param {String} url
 * @param {Object|String} [params]
 * @param {Function} [callback]
 * @param {String|Function} [dataType]
 */
function GET(url, params = "", callback = null, dataType = "text") {
	return send(url, "GET", params, callback, dataType);
}

/**
 * POST-request
 * @param {String} url
 * @param {Object|String} [params]
 * @param {Function} [callback]
 * @param {String|Function} [dataType]
 */
function POST(url, params = "", callback = null, dataType = "text") {
	return send(url, "POST", params, callback, dataType);
}

export {GET, POST};
export default send;