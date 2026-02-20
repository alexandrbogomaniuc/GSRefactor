import GUSSubloadingController from './GUSSubloadingController';
import GUSLobbySubloadingInfo from '../../model/subloading/GUSLobbySubloadingInfo';
import GUSLobbySubloadingView from '../../view/subloading/GUSLobbySubloadingView';
import Queue from '../../../unified/controller/interaction/resources/loaders/Queue';
import { APP } from '../../../unified/controller/main/globals';
import I18 from '../../../unified/controller/translations/I18';
import { APP_TYPES, TRANSLATIONS_TYPES } from '../../../unified/controller/interaction/resources/loaders/TranslationsQueue';
import { SUBLOADING_ASSETS_TYPES } from '../../model/subloading/GUSubloadingInfo';
import GUSLobbyApplication from '../main/GUSLobbyApplication';

class GUSLobbySubloadingController extends GUSSubloadingController
{
	constructor(aOptInfo_suii, aOptView_suiv)
	{
		super(aOptInfo_suii || new GUSLobbySubloadingInfo(), aOptView_suiv || new GUSLobbySubloadingView());
	}

	init()
	{
		super.init();

		APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyAPPStarted, this);
	}

	_onLobbyAPPStarted(event)
	{
		this._loadAssets();
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
					APP.library.createLoaderQueue(this.__paytableImageAssets)
				);
				break;
			case SUBLOADING_ASSETS_TYPES.profile:
				lLoader_q.add(
					APP.library.createLoaderQueue(this.__profileImageAssets)
				);
				break;
			default:
				break;
		}

		return lLoader_q;
	}

	get __paytableImageAssets()
	{
		// must be overridden
		return null;
	}

	get __profileImageAssets()
	{
		// must be overridden
		return null;
	}

	destroy()
	{
		super.destroy();
	}
}

export default GUSLobbySubloadingController;