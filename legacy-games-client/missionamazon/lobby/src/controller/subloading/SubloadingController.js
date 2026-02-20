import SimpleUIController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import SubloadingInfo from '../../model/subloading/SubloadingInfo';
import SubloadingView from '../../view/subloading/SubloadingView';
import Queue from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/Queue';
import { createLoader } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PAYTABLE_ASSETS from '../../config/paytable_assets.json';
import PROFILE_ASSETS from '../../config/profile_assets.json';
import VueApplicationController from '../../vue/VueApplicationController';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { TRANSLATIONS_TYPES, APP_TYPES } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/TranslationsQueue';
import { SUBLOADING_ASSETS_TYPES } from '../../config/Constants';
import LobbyAPP from '../../LobbyAPP';

class SubloadingController extends SimpleUIController
{
	static get EVENT_ON_LOADING_COMPLETED() 	{ return "eventOnLoadingCompleted" };
	static get EVENT_ON_LOADING_ERROR() 	 	{ return "eventOnLoadingError" };

	constructor()
	{
		super(new SubloadingInfo(), new SubloadingView());
		
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

 		APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._loadAssets, this);
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
		let lLoader_q = new Queue();

		switch (assetsType)
		{
			case SUBLOADING_ASSETS_TYPES.paytable:
				let translationsQueue = I18.createLoaderQueue
				(
					{
						type: TRANSLATIONS_TYPES.PAYTABLE, 
						appType: APP_TYPES.LOBBY, 
						isSupportDesktopPaytable: false
					}
				);

				translationsQueue.once('complete', () => {
					lLoader_q.add
					(
						I18.createAssetsLoaderQueue(translationsQueue)
					)
				});

				lLoader_q.add
				(
					translationsQueue,
					APP.library.createLoaderQueue(PAYTABLE_ASSETS.images)
				);
				break;
			case SUBLOADING_ASSETS_TYPES.profile:
				lLoader_q.add
				(
					APP.library.createLoaderQueue(PROFILE_ASSETS.images)
				);
				break;
			default:
				break;
		}

		return lLoader_q;
	}

	_onLoadingError(aEvent_ue)
	{
		this.emit(SubloadingController.EVENT_ON_LOADING_ERROR);
		APP.handleAssetsLoadingError(aEvent_ue.key, aEvent_ue.message);
	}

	_onLoadingComplete(assetsType)
	{
		this.i_getInfo().i_setLoaded(assetsType);
		this.emit(SubloadingController.EVENT_ON_LOADING_COMPLETED, {assetsType: assetsType});
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

export default SubloadingController;