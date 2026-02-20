import SoundsController from '../../../unified/controller/sounds/SoundsController';
import GUSLobbySoundsInfo from '../../model/sounds/GUSLobbySoundsInfo';
import { APP } from '../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../external/GUSLobbyExternalCommunicator';
import GUSLobbyButtonsController from '../uis/custom/lobby_buttons/GUSLobbyButtonsController';
import Button from '../../../unified/view/ui/Button';
import SoundMetrics from '../../../unified/model/sounds/SoundMetrics';
import GUSLobbyApplication from '../main/GUSLobbyApplication';
import GUSLobbyBGSoundsController from './GUSLobbyBGSoundsController';
import GUSLobbyScreen from '../../view/main/GUSLobbyScreen';

class GUSLobbySoundsController extends SoundsController
{
	//IL CONSTRUCTION...
	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi || new GUSLobbySoundsInfo());

		this._initGUSoundsController();
	}

	_initGUSoundsController()
	{
		this._initSoundsMetrics();
	}

	init(aIsSoundsStereoMode_bl = true, aIsSoundsLoadingAvailable_bl = true, muted = true, fxVolume = 0, musicVolume = 0)
	{
		super.init(aIsSoundsStereoMode_bl, aIsSoundsLoadingAvailable_bl);

		this._fBGSoundsController_bgsc = null;

		let lSoundSettings_obj = {
			"fxVolume": fxVolume,
			"musicVolume": musicVolume,
			"muted": muted
		};
		this.__updateSoundSettings(lSoundSettings_obj);

		this._initBackgroundController();
	}


	__updateSoundSettings(event)
	{
		super.__updateSoundSettings(event);

		for (let lSoundName_str of this.__getDisableExceptionList())
		{
			let lSoundType_str = this.__getSoundDescription(lSoundName_str).i_getType();
			let lVolume_num = undefined;
			let lSound_ussc = undefined;

			if (lSoundType_str == SoundMetrics.i_SOUND_FX)
			{
				lSound_ussc = this._fFxSounds_ussc_obj[lSoundName_str];
				lVolume_num = APP.soundSettingsController.info.i_getFxSoundsVolume();
			}
			else
			{
				lSound_ussc = this._fMusicSounds_usslc_obj[lSoundName_str];
				lVolume_num = APP.soundSettingsController.info.i_getBgSoundsVolume();
			}

			if (lSound_ussc && (lSound_ussc !== undefined))
			{
				lSound_ussc.i_setVolume(lVolume_num);
			}
		}
	}

	_initBackgroundController()
	{
		this._fBGSoundsController_bgsc = this.__provideLobbyBGSoundsControllerInstance();
		this._fBGSoundsController_bgsc.init();

		this._fBGSoundsController_bgsc.on(GUSLobbyBGSoundsController.EVENT_ON_SET_VOLUME_SMOOTHLY, this._setBGVolumeSmoothly, this);
		this._fBGSoundsController_bgsc.on(GUSLobbyBGSoundsController.EVENT_ON_SET_VOLUME, this._setBGVolume, this);
		this._fBGSoundsController_bgsc.on(GUSLobbyBGSoundsController.EVENT_ON_SET_MULTIPLY_SOUND_VOLUME, this._multiplyBGSoundVolume, this);
	}

	__provideLobbyBGSoundsControllerInstance()
	{
		return new GUSLobbyBGSoundsController();
	}

	get bgSoundsController()
	{
		return this._fBGSoundsController_bgsc;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);
		APP.on(GUSLobbyApplication.EVENT_ON_SOUND_SETTINGS_CHANGED, this.__updateSoundSettings, this);

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this.onGameMessageReceived, this);

		APP.lobbyButtonsConstroller.on(GUSLobbyButtonsController.EVENT_ON_SOME_LOBBY_BTN_PLAY_SOUND, this._onSomeLobbyBtnPlaySound, this);
	}

	_onLobbyAppStarted()
	{
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_LAUNCH_GAME_CLICKED, this._onGameLaunchClicked, this);
	}

	_onGameLaunchClicked()
	{
		if (this.__launchGameSoundName !== undefined)
		{
			this.play(this.__launchGameSoundName);
		}
	}

	_onSomeLobbyBtnPlaySound(aEvent_obj)
	{
		let soundName = "";
		switch (aEvent_obj.buttonType)
		{
			case Button.BUTTON_TYPE_ACCEPT:
				soundName = this.__acceptBtnClickSoundName;
				break;
			case Button.BUTTON_TYPE_CANCEL:
				soundName = this.__cancelBtnClickSoundName;
				break;
			case Button.BUTTON_TYPE_COMMON:
			default:
				soundName = this.__commonBtnClickSoundName;
				break;
		}
		this.play(soundName);
	}

	get __acceptBtnClickSoundName()
	{
		return undefined;
	}

	get __cancelBtnClickSoundName()
	{
		return undefined;
	}

	get __commonBtnClickSoundName()
	{
		return undefined;
	}

	get __preloaderMusicSoundName()
	{
		return undefined;
	}

	playPreloaderSounds()
	{
		this.play(this.__preloaderMusicSoundName);
		this.setFadeMultiplier(this.__preloaderMusicSoundName, 0);

		if (this.info.soundsMuted)
		{
			this._fBGSoundsController_bgsc.needSetPreloaderSoundsVolume();
			this._fBGSoundsController_bgsc.once(GUSLobbyBGSoundsController.EVENT_ON_PLAY_PRELOADER_SOUNDS, this._onSetPreloaderSoundsVolume, this);
		}
		else
		{
			this._onSetPreloaderSoundsVolume();
		}
	}

	_onSetPreloaderSoundsVolume()
	{
		this.setVolumeSmoothly(this.__preloaderMusicSoundName, 1, 5000);
	}


	onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.BACK_TO_LOBBY:
			case GAME_MESSAGES.GAME_START_CLICKED:
				this.resetAllSoundsVolume();
				break;
		}
	}

	_setBGVolumeSmoothly(event)
	{
		this.setVolumeSmoothly(event.soundId, event.endVolume, event.delay);
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BACKGROUND_FADE_START, { endVolume: event.endVolume });
	}

	_setBGVolume(event)
	{
		this.i_setVolume(event.soundId, event.volume);
	}

	_multiplyBGSoundVolume(event)
	{
		this.setFadeMultiplier(event.soundId, event.volume);
	}

	_initSoundsMetrics()
	{
		this.__getInfo().i_initSoundsMetrics(this.__preloaderSoundsAssets);
		this.__getInfo().i_initSoundsMetrics(this.__lobbySoundsAssets);
	}

	get __preloaderSoundsAssets()
	{
		return undefined;
	}

	get __lobbySoundsAssets()
	{
		return undefined;
	}
	//...IL INIT

	__applySoundsVolumeToSound(aSound_ussc, aVolume_num)
	{
		if (aSound_ussc.i_getInfo().i_getSoundName() === this.__launchGameSoundName)
		{
			aSound_ussc.i_setVolume(this.__getInfo().i_getSoundsVolume(SoundMetrics.i_SOUND_FX, true));
		}
		else
		{
			super.__applySoundsVolumeToSound.call(this, aSound_ussc, aVolume_num);
		}
	}

	get __launchGameSoundName()
	{
		return undefined;
	}

	destroy()
	{
		super.destroy();

		this._fBGSoundsController_bgsc = null;
	}
}

export default GUSLobbySoundsController