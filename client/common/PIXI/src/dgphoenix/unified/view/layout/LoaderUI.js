import EventDispatcher from '../../controller/events/EventDispatcher';

/**
 * @class
 * @inheritDoc
 * @extends EventDispatcher
 * @classdesc Base class for preloader view
 */
class LoaderUI extends EventDispatcher {

	/**
	 * @constructor
	 * @param {DOMLayout} layout
	 * @param {Loader} loader
	 */
	constructor(layout, loader) {
		super();

		this.layout = layout;
		this.container = layout.getLayer('preloader');
		this.loader = loader;

		this._dispatchComplete = () => {
			/**
			 * @event LoaderUI#complete
			 * @type {Event}
			 */
			this.emit('complete');
		}
	}

	destructor() {
		this.removeAllEventListeners();
		this.layout = null;
		this.container = null;
		this.loader = null;

		super.destructor();
	}

	removeAllEventListeners(){
		this.removeListeners();
	}

	removeListeners() {
		this.layout.off('fitlayout', this.fitLayout);
		this.layout.off('orientationchange', this.onOrientationChanged, this);
		this.loader.off('progress', this.handleProgress);
		this.loader.off('complete', this.handleComplete);
	}

	addListeners() {
		this.removeListeners();
		this.layout.on('fitlayout', this.fitLayout, this);
		this.layout.on('orientationchange', this.onOrientationChanged, this);
		this.loader.on('progress', this.handleProgress, this);
		this.loader.once('complete', this.handleComplete, this);
	}

	handleProgress() {
		let progressInfo = this.loader.progressInfo;

		let prc = (progressInfo === undefined) ? 0 : Math.floor(progressInfo.progress * 100);
		this.showProgress(prc);
	}

	handleComplete() {
		this.showProgress(100);
		this.showComplete();
	}

	/**
	 * @protected
	 */
	dispatchComplete() {
		this.emit('complete');
	}

	/**
	 * @protected
	 */
	createLayout()
	{
	}

	/**
	 * @protected
	 */
	fitLayout() 
	{
	}

	/**
	 * @protected
	 */
	onOrientationChanged(e)
	{
	}

	/**
	 * @protected
	 */
	showProgress()
	{
	}

	/**
	 * @protected
	 */
	showComplete()
	{
	}
}

export default LoaderUI;