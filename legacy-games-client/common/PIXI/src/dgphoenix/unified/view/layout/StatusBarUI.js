import EventDispatcher from '../../controller/events/EventDispatcher';

/**
 * @class
 * @inheritDoc
 * @extends EventDispatcher
 * @classdesc Base class for status bar view.
 */
class StatusBarUI extends EventDispatcher {

	/**
	 * @constructor
	 * @param {DOMLayout} layout
	 */
	constructor(layout) {
		super();

		this.layout = layout;
		this.container = layout.getLayer('status-bar');

		this._dispatchComplete = () => {
			/**
			 * @event StatusBarUI#complete
			 * @type {Object}
			 */
			this.emit('complete');
		}
	}

	destructor() {
		this.removeAllEventListeners();
		this.layout = null;
		this.container = null;
	}

	removeAllEventListeners(){
		super.removeAllEventListeners();
		this.removeListeners();
	}

	removeListeners() {
		this.layout.off('fitlayout', this.fitLayout);
	}

	addListeners() {
		this.removeListeners();
		// this.layout.on('fitlayout', this.fitLayout, this);
	}

	dispatchComplete() {
		this.emit('complete');
	}

	updateStatusBar(refreshView, fps)
	{
	}
}

export default StatusBarUI;