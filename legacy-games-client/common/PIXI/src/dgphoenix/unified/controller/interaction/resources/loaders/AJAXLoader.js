import QueueItem from './QueueItem';
import { GET } from '../../server/ajax';

let typeDetector = document.createElement('a');
function _autodetectType(url) { // XXX: helper

	typeDetector.href = url;
	let ext = (typeDetector.pathname || '').split('/').pop();
	ext = ext.indexOf('.') < 0 ? '' : ext.split('.').pop().toLowerCase();
	typeDetector.href = null;

	if (ext == 'xml') return 'xml';
	if (ext == 'json') return 'json';
	if (ext == 'html') return 'html';
	return 'text';
}

/**
 * @class
 * @inheritDoc
 * @classdesc Async HTTP loader
 */
class AJAXLoader extends QueueItem {

	/**
	 * @constructor
	 * @param {String} url
	 * @param {String|Function} [dataType="text"] - data type or parser function
	 */
	constructor(...params) {
		let [url, dataType=_autodetectType(url)] = params;
		super(url);
		this.dataType = dataType;
	}

	prepareData(src) {
		return null;
	}

	prepareKey(src) {
		return src;
	}

	load(cache = true) {
		super.load(cache);
		if (!this.complete) {
			let self = this;
			GET(
					this.generateAbsoluteURL(this.key), 
					null, 
					function (data, status, xhr, errorMessage) 
					{
						self._status = status;
						if (status == 'error') {
							self._statusMessage = errorMessage || (xhr && xhr.statusText) || 'Uknown AJAX error';
						}
						self.data = data;
						self.completeLoad(cache);
					}, 
					this.dataType);
		}
	}
}

export default AJAXLoader;