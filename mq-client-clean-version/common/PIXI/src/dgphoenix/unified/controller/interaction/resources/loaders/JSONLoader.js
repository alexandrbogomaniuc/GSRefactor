import AJAXLoader from './AJAXLoader';

/**
 * @class
 * @inheritDoc
 * @classdesc JSON loader
 */
class JSONLoader extends AJAXLoader {
	constructor(src) {
		super(src, 'json');
	}
}

export default JSONLoader;