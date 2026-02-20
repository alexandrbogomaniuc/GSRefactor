import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import Game from '../../Game';
import ASSETS from '../../config/assets.json';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SyncQueue from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/SyncQueue';
import GameExternalCommunicator from '../../external/GameExternalCommunicator';
import {LOBBY_MESSAGES} from '../../external/GameExternalCommunicator';
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

		APP.on(Game.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED, this._onTournamentModeServerStateChanged, this);

	}
	//...ILI INIT

	_onSoundSettingsChanged(event)
	{
		if (event.muted !== undefined && !event.muted)
		{
			APP.off(Game.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

			this._startSoundsBackgroundLoading();
		}
	}

	_startSoundsBackgroundLoading()
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			return;
		}
		
		let queue = this._soundsQueue = new SyncQueue();
		queue.concurrency = 1;

		let lobbySoundsQueue = APP.soundsController.createLoaderQueue(ASSETS.sounds);
		lobbySoundsQueue.concurrency = 1;

		queue.add(lobbySoundsQueue);

		queue.once('error', this._onSoundsBgLoadingError, this);
		queue.load();
	}

	_onSoundsBgLoadingError(e)
	{
		this._soundsQueue.stopLoading();
		APP.handleAssetsLoadingError(e.key, e.message);
	}

	_onLobbyMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case LOBBY_MESSAGES.LOBBY_LOADING_ERROR:
			case LOBBY_MESSAGES.WEBGL_CONTEXT_LOST:			
				this._soundsQueue.stopLoading();
				break;
		}
	}

	//TOURNAMENT...
	_onTournamentModeServerStateChanged(event)
	{
		let l_cpv = this.view;
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;
		
		if (lTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			this._soundsQueue && this._soundsQueue.stopLoading();
		}		
	}
	//...TOURNAMENT
}

export default SoundsBackgoundLoadingController;