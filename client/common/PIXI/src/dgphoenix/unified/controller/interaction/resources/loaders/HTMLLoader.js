import AJAXLoader from './AJAXLoader';

/**
 * @class
 * @inheritDoc
 * @classdesc HTML loader
 */
class HTMLLoader extends AJAXLoader {
	constructor(src) {
		super(src, 'html');
	}
}

export default HTMLLoader;