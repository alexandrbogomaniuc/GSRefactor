import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { BACKGROUND_SOUNDS_FADING_TIME } from '../../../../shared/src/CommonConstants';
import SecondaryScreenController from '../../controller/uis/custom/secondary/SecondaryScreenController';
import LobbyStateController from '../state/LobbyStateController';
import LobbyExternalCommunicator from '../../external/LobbyExternalCommunicator';
import {GAME_MESSAGES} from '../../external/LobbyExternalCommunicator';
import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import LobbyApp from './../../LobbyAPP';

const LOBBY_DEFAULT_BACKGROUND_SOUND_NAME = "mq_mus_lobby_bg";

class LobbyBGSoundsController extends SimpleController
{
	static get EVENT_ON_SET_VOLUME_SMOOTHLY() 						{return "eventOnSetVolumeSmoothly";}
	static get EVENT_ON_SET_VOLUME() 								{return "eventOnSetVolume";}
	static get EVENT_ON_SET_MULTIPLY_SOUND_VOLUME() 				{return "eventOnSetMultiplySoundVolume";}
	static get EVENT_ON_PLAY_PRELOADER_SOUNDS()						{return "EVENT_ON_PLAY_PRELOADER_SOUNDS";}

	constructor()
	{
		super();

		this._fSecondaryScreenVisible_bln = false;
		this._fLobbyScreenVisible_bln = false;
		this._isGameWasActive_bln = false;
		this._fIsMuted = false;
		this._fGameStartClicked_bl = false;
		this._fNeedSetPreloaderSoundsVolume_bl = false;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.lobbyStateController.on(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);
		APP.lobbyStateController.on(LobbyStateController.EVENT_ON_DIALOGS_VISIBILITY_CHANGED, this._onDialogsViewChanged, this);

		APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_ACTIVATED, this._onSecondaryScreenActivated, this);
		APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this.onGameMessageReceived, this);

		APP.on(LobbyApp.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);
		APP.on(LobbyApp.EVENT_ON_LOBBY_UI_SHOWN, this._onLobbyUIShown, this);
	}

	needSetPreloaderSoundsVolume()
	{
		this._fNeedSetPreloaderSoundsVolume_bl = true;
	}

	_onSoundSettingsChanged(event)
	{
		if (!this._fGameStartClicked_bl && this._fNeedSetPreloaderSoundsVolume_bl)
		{
			this.emit(LobbyBGSoundsController.EVENT_ON_PLAY_PRELOADER_SOUNDS);
			this._fNeedSetPreloaderSoundsVolume_bl = false;
		}

		if (this._fGameStartClicked_bl && !this._isGameWasActive_bln)
		{
			return;
		}

		if (event.muted || event.musicVolume == 0)
		{
			this._fIsMuted = true;
		}
		else
		{
			this._fIsMuted = false;
		}
		this._validateSounds(true);
	}

	_onLobbyVisibilityChanged(aEvent_obj)
	{
		this._fLobbyScreenVisible_bln = aEvent_obj.visible;

		if (this._isGameWasActive_bln)
		{
			this._validateSounds();
		}
	}

	_onLobbyUIShown(aEvent_obj)
	{
		if (this._isGameWasActive_bln)
		{
			this._validateSounds();
		}
	}

	_onSecondaryScreenActivated()
	{
		this._fSecondaryScreenVisible_bln = APP.lobbyStateController.info.secondaryScreenVisible;
		if (!APP.lobbyStateController.info.lobbyScreenVisible)
		{
			let lConfig_obj = {
				soundId: LOBBY_DEFAULT_BACKGROUND_SOUND_NAME,
				endVolume: 1,
				delay: BACKGROUND_SOUNDS_FADING_TIME
			}
			this.emit(LobbyBGSoundsController.EVENT_ON_SET_VOLUME_SMOOTHLY, lConfig_obj);
		}
	}

	_onSecondaryScreenDeactivated()
	{
		this._fSecondaryScreenVisible_bln = APP.lobbyStateController.info.secondaryScreenVisible;
		if (!APP.lobbyStateController.info.lobbyScreenVisible)
		{
			let lConfig_obj = {
				soundId: LOBBY_DEFAULT_BACKGROUND_SOUND_NAME,
				endVolume: 0,
				delay: BACKGROUND_SOUNDS_FADING_TIME
			}
			this.emit(LobbyBGSoundsController.EVENT_ON_SET_VOLUME_SMOOTHLY, lConfig_obj);
		}
	}

	_onDialogsViewChanged()
	{
		if (this._isGameWasActive_bln)
		{
			this._validateSounds();
		}
	}

	onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;
		let lConfig_obj;

		switch (msgType)
		{
			case GAME_MESSAGES.GAME_STARTED:
				if (!this._isGameWasActive_bln)
				{
					this._isGameWasActive_bln = true;
				}

				lConfig_obj = {
					soundId: LOBBY_DEFAULT_BACKGROUND_SOUND_NAME,
					volume: 0
				}
				this.emit(LobbyBGSoundsController.EVENT_ON_SET_VOLUME, lConfig_obj);
				this.emit(LobbyBGSoundsController.EVENT_ON_SET_MULTIPLY_SOUND_VOLUME, lConfig_obj);
				break;
			case GAME_MESSAGES.GAME_START_CLICKED:
				lConfig_obj = {
					soundId: LOBBY_DEFAULT_BACKGROUND_SOUND_NAME,
					volume: 0
				}
				this.emit(LobbyBGSoundsController.EVENT_ON_SET_VOLUME, lConfig_obj);
				this.emit(LobbyBGSoundsController.EVENT_ON_SET_MULTIPLY_SOUND_VOLUME, lConfig_obj);
				this._fGameStartClicked_bl = true;
				break;
		}
	}

	_validateSounds(aUseSecondary_bln = false)
	{
		let lLobbyVisible_bln = APP.lobbyStateController.info.lobbyScreenVisible;
		let lPreloaderVisible_bln = APP.lobbyStateController.info.preloaderVisible;
		let lShouldPlay_bln = !!lLobbyVisible_bln
								|| !!(APP.layout.isLobbyLayoutVisible && APP.lobbyScreen.visible) /*when returned to lobby but lobby view is still hidden (rejoin room dialog required)*/
								|| !!lPreloaderVisible_bln;

		if (aUseSecondary_bln)
		{
			let lSecondaryScreenVisible_bln = APP.lobbyStateController.info.secondaryScreenVisible;
			lShouldPlay_bln = lShouldPlay_bln || lSecondaryScreenVisible_bln;
		}

		if(lShouldPlay_bln && !this._fIsMuted)
		{
			let lConfig_obj = {
				soundId: LOBBY_DEFAULT_BACKGROUND_SOUND_NAME,
				volume: 1
			}

			this.emit(LobbyBGSoundsController.EVENT_ON_SET_VOLUME, lConfig_obj);
			this.emit(LobbyBGSoundsController.EVENT_ON_SET_MULTIPLY_SOUND_VOLUME, lConfig_obj);
		}
		else if(!APP.isBattlegroundGame || this._fIsMuted)
		{
			let lConfig_obj = {
				soundId: LOBBY_DEFAULT_BACKGROUND_SOUND_NAME,
				volume: 0
			}
			this.emit(LobbyBGSoundsController.EVENT_ON_SET_VOLUME, lConfig_obj);
			this.emit(LobbyBGSoundsController.EVENT_ON_SET_MULTIPLY_SOUND_VOLUME, lConfig_obj);
		}
	}

	destroy()
	{
		super.destroy();

		this._fLobbyScreenVisible_bln = undefined;
		this._fSecondaryScreenVisible_bln = undefined;
		this._isGameWasActive_bln = undefined;
	}
}

export default LobbyBGSoundsController;