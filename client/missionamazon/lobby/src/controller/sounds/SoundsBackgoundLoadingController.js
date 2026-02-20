import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';

import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyApp from '../../LobbyAPP';
import SyncQueue from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/SyncQueue';

import ASSETS from '../../config/assets.json';
import PRELOADER_ASSETS from '../../config/preloader_assets.json';
import TournamentModeController from '../custom/tournament/TournamentModeController';

class SoundsBackgoundLoadingController extends SimpleController
{
	//IL CONSTRUCTION...
	constructor()
	{
		super();
		
		this._soundsQueue = null;
		this._fTournamentModeInfo_tmi = null;
		
		this._initUSoundsBackgoundLoadingController();
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	//...IL INTERFACE

	//ILI INIT...
	_initUSoundsBackgoundLoadingController()
	{
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(LobbyApp.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);
	}
	//...ILI INIT

	_onSoundSettingsChanged(event)
	{
		if (event.muted !== undefined && !event.muted)
		{
			APP.off(LobbyApp.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

			this._startSoundsBackgroundLoading();
		}
	}

	_startSoundsBackgroundLoading()
	{
		APP.soundsLoadingInitiated = true;
		
		let queue = this._soundsQueue = new SyncQueue();
		queue.concurrency = 1;

		let preloaderSoundsQueue = APP.soundsController.createLoaderQueue(PRELOADER_ASSETS.sounds);
		preloaderSoundsQueue.concurrency = 1;
		preloaderSoundsQueue.once('error', this._onPreloaderSoundsAssetsLoadingError, this);
		preloaderSoundsQueue.once('complete', this._onPreloaderSoundsAssetsLoaded, this);

		let lobbySoundsQueue = APP.soundsController.createLoaderQueue(ASSETS.sounds);
		lobbySoundsQueue.concurrency = 1;

		queue.add (
					preloaderSoundsQueue,
					lobbySoundsQueue
				);

		queue.once('error', this._onSoundsBgLoadingError, this);
		queue.load();
	}

	_onPreloaderSoundsAssetsLoadingError(e)
	{
		APP.handleAssetsLoadingError(e.key, e.message);
	}

	_onPreloaderSoundsAssetsLoaded(e)
	{
		if (APP.isPreloaderActive)
		{
			APP.playPreloaderSounds();
		}
	}

	_onSoundsBgLoadingError(e)
	{
		this._soundsQueue.stopLoading();
		APP.handleAssetsLoadingError(e.key, e.message);
	}

	//TOURNAMENT...
	_onTournamentModeStateChanged(event)
	{
		let l_cpv = this.view;
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;
		
		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			this._soundsQueue && this._soundsQueue.stopLoading();
		}		
	}
	//...TOURNAMENT
}

export default SoundsBackgoundLoadingController;