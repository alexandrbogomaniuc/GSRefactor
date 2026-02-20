import SimpleUIController from '../../../unified/controller/uis/base/SimpleUIController';
import Queue from '../../../unified/controller/interaction/resources/loaders/Queue';
import { APP } from '../../../unified/controller/main/globals';
import GUSubloadingInfo, { SUBLOADING_ASSETS_TYPES } from '../../model/subloading/GUSubloadingInfo';
import GUSubloadingView from '../../view/subloading/GUSubloadingView';

class GUSSubloadingController extends SimpleUIController
{
	static get EVENT_ON_LOADING_COMPLETED() { return "eventOnLoadingCompleted"; }
	static get EVENT_ON_LOADING_ERROR() { return "eventOnLoadingError"; }

	constructor(aOptInfo_suii, aOptView_suiv)
	{
		super(aOptInfo_suii || new GUSubloadingInfo(), aOptView_suiv || new GUSubloadingView());

		this._fLoaders_qs = {};

		for (let assetsType in SUBLOADING_ASSETS_TYPES)
		{
			this._fLoaders_qs[assetsType] = null;
		}

		this.init();
	}

	i_showLoadingScreen()
	{
		this.view.i_showLoadingScreen();
	}

	i_hideLoadingScreen()
	{
		this.view.i_hideLoadingScreen();
	}

	i_isLoaded(assetsType)
	{
		return this.i_getInfo().i_isLoaded(assetsType);
	}

	init()
	{
		super.init();
	}

	_loadAssets()
	{
		for (var assetsType in SUBLOADING_ASSETS_TYPES)
		{
			this.i_getInfo().i_setLoadingInProgress(assetsType);

			let lLoader_q = this._createLoader(assetsType);
			lLoader_q.once('error', this._onLoadingError, this);
			lLoader_q.once('complete', this._onLoadingComplete.bind(this, assetsType), this);
			lLoader_q.load();

			this._fLoaders_qs[assetsType] = lLoader_q;
		}
	}

	_createLoader(assetsType)
	{
		//must be overridden
		return new Queue();
	}

	_onLoadingError(aEvent_ue)
	{
		this.emit(GUSSubloadingController.EVENT_ON_LOADING_ERROR);
		APP.handleAssetsLoadingError(aEvent_ue.key, aEvent_ue.message);
	}

	_onLoadingComplete(assetsType)
	{
		this.i_getInfo().i_setLoaded(assetsType);
		this.emit(GUSSubloadingController.EVENT_ON_LOADING_COMPLETED, { assetsType: assetsType });
	}

	destroy()
	{
		for (var loader in this._fLoaders_qs)
		{
			loader.off('error', this._onLoadingError, this, true);
			loader.off('complete', this._onLoadingComplete, this, true);
			loader = null;
		}

		this._fLoaders_qs = null;

		super.destroy();
	}
}

export default GUSSubloadingController;