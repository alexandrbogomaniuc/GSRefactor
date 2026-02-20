var EventEmitter = require('eventemitter3');

/**
 * @class
 * @classdesc Event management class
 */
class EventDispatcher extends EventEmitter {

	/**
	 * @constructor
	 */
	constructor() {
		super();
	}

	destructor(){
		this.removeAllListeners();
	}

	emit(type, params) 
	{
		if (typeof type === "string")
		{
			if(!params) params = {};
			if(!params.type) params.type = type;
			if(!params.target) params.target = this;

			super.emit(type, params);
		}
		else if (typeof type === "object")
		{
			super.emit(type.type, type);
		}
	}

	hasListeners(type) {
		return this.listeners(type).length > 0;
	}
}

export default EventDispatcher;