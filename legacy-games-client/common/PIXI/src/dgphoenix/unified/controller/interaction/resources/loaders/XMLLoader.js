import AJAXLoader from './AJAXLoader';

/**
 * @class
 * @inheritDoc
 * @extends AJAXLoader
 * @classdesc XML loader
 */
class XMLLoader extends AJAXLoader {
	constructor(src) {
		super(src, 'xml');
	}
}

export default XMLLoader;