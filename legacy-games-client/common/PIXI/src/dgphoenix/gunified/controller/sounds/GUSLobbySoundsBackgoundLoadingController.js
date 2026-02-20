import SimpleController from '../../../unified/controller/base/SimpleController';
import GUSLobbyApplication from '../main/GUSLobbyApplication';
import { APP } from '../../../unified/controller/main/globals';
import GUSLobbyTournamentModeController from '../custom/tournament/GUSLobbyTournamentModeController';
import SyncQueue from '../../../unified/controller/interaction/resources/loaders/SyncQueue';

class GUSLobbySoundsBackgoundLoadingController extends SimpleController
{
	constructor()
	{
		super();

		this._soundsQueue = null;
		this._fTournamentModeInfo_tmi = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(GUSLobbyApplication.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(GUSLobbyTournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);
	}

	_onSoundSettingsChanged(event)
	{
		if (event.muted !== undefined && !event.muted)
		{
			APP.off(GUSLobbyApplication.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

			this._startSoundsBackgroundLoading();
		}
	}

	_startSoundsBackgroundLoading()
	{
		APP.soundsLoadingInitiated = true;

		let queue = this._soundsQueue = new SyncQueue();
		queue.concurrency = 1;

		let preloaderSoundsQueue = APP.soundsController.createLoaderQueue(this.__preloaderSoundsAssets);
		preloaderSoundsQueue.concurrency = 1;
		preloaderSoundsQueue.once('error', this._onPreloaderSoundsAssetsLoadingError, this);
		preloaderSoundsQueue.once('complete', this._onPreloaderSoundsAssetsLoaded, this);

		let lobbySoundsQueue = APP.soundsController.createLoaderQueue(this.__lobbySoundsAssets);
		lobbySoundsQueue.concurrency = 1;

		queue.add(
			preloaderSoundsQueue,
			lobbySoundsQueue
		);

		queue.once('error', this._onSoundsBgLoadingError, this);
		queue.load();
	}

	get __preloaderSoundsAssets()
	{
		//must be overridden
		return undefined;
	}

	get __lobbySoundsAssets()
	{
		//must be overridden
		return undefined;
	}

	_onPreloaderSoundsAssetsLoadingError(e)
	{
		APP.handleAssetsLoadingError(e.key, e.message);
	}

	_onPreloaderSoundsAssetsLoaded()
	{
		if (APP.isPreloaderActive || APP.dialogsController.returnToGameDialogController.info.visible)
		{
			APP.playPreloaderSounds();
		}

		if(APP.lobbyStateController.info.lobbyScreenVisible)
		{
			APP.playBackgroundSoundName()
		}
	}

	_onSoundsBgLoadingError(e)
	{
		this._soundsQueue.stopLoading();
		APP.handleAssetsLoadingError(e.key, e.message);
	}

	//TOURNAMENT...
	_onTournamentModeStateChanged()
	{
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;

		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			this._soundsQueue && this._soundsQueue.stopLoading();
		}
	}
	//...TOURNAMENT
}

export default GUSLobbySoundsBackgoundLoadingController;