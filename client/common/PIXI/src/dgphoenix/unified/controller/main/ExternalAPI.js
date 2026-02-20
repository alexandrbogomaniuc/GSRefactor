import LoaderUI from '../../view/layout/LoaderUI';
import PreLoaderUI from '../../view/layout/PreLoaderUI';
import StatusBarUI from '../../view/layout/StatusBarUI';

class ExternalAPI {

	/**
	 * Application preloader view
	 */
	get LoaderUI() {
		// override this if needed
		return LoaderUI;
	}

	/**
	 * Application view for preloader assets loading
	 */
	get PreLoaderUI() {
		return PreLoaderUI;
	}

	/**
	 * Application status bar that contains app name, version and fps meter
	 */
	get StatusBarUI() {
		return StatusBarUI;
	}

	constructor(config) {
		this.config = config || null;
		this.ready = false;
		this.exec = this.exec.bind(this);
	}

	exec(method, ...args) {
		// override this if needed
		if (this[method] instanceof Function){
			return this[method](...args);
		}
	}

	init(callback){
		if (callback instanceof Function) {
			callback(this);
		}

		this.ready = true;
	}
}

export default ExternalAPI;