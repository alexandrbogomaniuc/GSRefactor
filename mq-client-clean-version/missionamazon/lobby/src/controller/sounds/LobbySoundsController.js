import SoundsController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/sounds/SoundsController';
import LobbySoundsInfo from '../../model/sounds/LobbySoundsInfo';
import ASSETS from '../../config/assets.json';
import PRELOADER_ASSETS from '../../config/preloader_assets.json';
import LobbyAPP from '../../LobbyAPP';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SoundMetrics from '../../../../../common/PIXI/src/dgphoenix/unified/model/sounds/SoundMetrics';
import LobbyExternalCommunicator from '../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../external/LobbyExternalCommunicator';
import LobbyBGSoundsController from './LobbyBGSoundsController';
import SettingsScreenController from '../uis/custom/secondary/settings/SettingsScreenController';
import LobbyScreen from '../../main/LobbyScreen';
import LobbyButtonsController from '../uis/custom/lobby_buttons/LobbyButtonsController';
import Button from '../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';

class LobbySoundsController extends SoundsController
{
	//IL CONSTRUCTION...
	constructor()
	{
		super(new LobbySoundsInfo());

		this._initGSoundsController();
	}
	//...IL CONSTRUCTION

	//IL INTERFACE...
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

	//...IL INTERFACE

	//IL INIT...
	_initGSoundsController()
	{
		this._initSoundsMetrics();
	}

	_initBackgroundController()
	{
		this._fBGSoundsController_bgsc = new LobbyBGSoundsController();
		this._fBGSoundsController_bgsc.init();

		this._fBGSoundsController_bgsc.on(LobbyBGSoundsController.EVENT_ON_SET_VOLUME_SMOOTHLY, this._setBGVolumeSmoothly, this);
		this._fBGSoundsController_bgsc.on(LobbyBGSoundsController.EVENT_ON_SET_VOLUME, this._setBGVolume, this);
		this._fBGSoundsController_bgsc.on(LobbyBGSoundsController.EVENT_ON_SET_MULTIPLY_SOUND_VOLUME, this._multiplyBGSoundVolume, this);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(LobbyAPP.EVENT_ON_SOUND_SETTINGS_CHANGED, this.__updateSoundSettings, this);
		APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this.onGameMessageReceived, this);

		APP.secondaryScreenController.settingsScreenController.on(SettingsScreenController.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE, this._onSettingsScreenSliderMove, this);
		APP.secondaryScreenController.settingsScreenController.on(SettingsScreenController.EVENT_ON_TUTORIAL_TOGGLE_BTN_CLICK, this._onTutorialToggleBtnClick, this);

		APP.lobbyButtonsConstroller.on(LobbyButtonsController.EVENT_ON_SOME_LOBBY_BTN_PLAY_SOUND, this._onSomeLobbyBtnPlaySound, this);
	}

	_onLobbyAppStarted()
	{
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_LAUNCH_GAME_CLICKED, this._onGameLaunchClicked, this);
	}

	playPreloaderSounds()
	{
		this.play("mq_mus_lobby_bg");
		this.setFadeMultiplier("mq_mus_lobby_bg", 0);

		if (this.info.soundsMuted)
		{
			this._fBGSoundsController_bgsc.needSetPreloaderSoundsVolume();
			this._fBGSoundsController_bgsc.once(LobbyBGSoundsController.EVENT_ON_PLAY_PRELOADER_SOUNDS, this._onSetPreloaderSoundsVolume, this);
		}
		else
		{
			this._onSetPreloaderSoundsVolume();
		}
	}

	_onSetPreloaderSoundsVolume()
	{
		this.setVolumeSmoothly("mq_mus_lobby_bg", 1, 5000);
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
		this.__getInfo().i_initSoundsMetrics(PRELOADER_ASSETS.sounds);
		this.__getInfo().i_initSoundsMetrics(ASSETS.sounds);
	}
	//...IL INIT

	__applySoundsVolumeToSound(aSound_ussc, aVolume_num)
	{
		if (aSound_ussc.i_getInfo().i_getSoundName() === "mq_gui_launch")
		{
			aSound_ussc.i_setVolume(this.__getInfo().i_getSoundsVolume(SoundMetrics.i_SOUND_FX, true));
		}
		else
		{
			super.__applySoundsVolumeToSound.call(this, aSound_ussc, aVolume_num);
		}
	}

	_onSettingsScreenSliderMove()
	{
		this.play('mq_gui_slider');
	}

	_onTutorialToggleBtnClick()
	{
		this.play('mq_gui_button_generic_ui');
	}

	_onGameLaunchClicked()
	{
		this.play('mq_gui_launch');
	}

	_onSomeLobbyBtnPlaySound(aEvent_obj)
	{
		let soundName = "";
		switch (aEvent_obj.buttonType)
		{
			case Button.BUTTON_TYPE_ACCEPT:
				soundName = "mq_gui_button_generic_ui";
				break;
			case Button.BUTTON_TYPE_CANCEL:
				soundName = "mq_gui_button_generic_ui";
				break;
			case Button.BUTTON_TYPE_COMMON:
			default:
				soundName = "mq_gui_button_generic_ui";
				break;
		}
		this.play(soundName);
	}

	destroy()
	{
		super.destroy();

		this._fBGSoundsController_bgsc = null;
	}
}

export default LobbySoundsController;