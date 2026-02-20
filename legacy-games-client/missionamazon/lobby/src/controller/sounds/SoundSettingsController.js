import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import SoundMetrics from '../../../../../common/PIXI/src/dgphoenix/unified/model/sounds/SoundMetrics';
import SoundSettingsInfo from '../../model/sounds/SoundSettingsInfo';
import SettingsScreenController from '../uis/custom/secondary/settings/SettingsScreenController';
import LobbySoundButtonController from '../uis/custom/secondary/LobbySoundButtonController';
import LobbyPreloaderSoundButtonController from '../uis/custom/preloader/LobbyPreloaderSoundButtonController';

import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyApp from '../../LobbyAPP';
import BrowserSupportController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/preloading/BrowserSupportController';

class SoundSettingsController extends SimpleController
{
	static get EVENT_ON_SOUND_VOLUME_CHANGED() 		{return "onSoundSettingsVolumeChanged";}

	//IL CONSTRUCTION...
	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi ? aOptInfo_ussi : new SoundSettingsInfo());
		
		this._initUSoundSettingsController();
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	setSoundsOn(aSoundsOn_obj)
	{
		this._setSoundsOn(aSoundsOn_obj);
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSoundSettingsController()
	{
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.secondaryScreenController.settingsScreenController.on(SettingsScreenController.EVENT_ON_SOUND_VOLUME_CHANGED, this._onSoundVolumeChanged, this);
		APP.secondaryScreenController.settingsScreenController.on(SettingsScreenController.EVENT_ON_MUSIC_VOLUME_CHANGED, this._onMusicVolumeChanged, this);
		APP.secondaryScreenController.settingsScreenController.on(SettingsScreenController.EVENT_SCREEN_ACTIVATED, this._onSettingsScreenActivated, this);

		APP.commonPanelController.soundButtonController.on(LobbySoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.commonPanelController.soundButtonController.on(LobbySoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.dialogsController.soundButtonController.on(LobbySoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.dialogsController.soundButtonController.on(LobbySoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.loaderUI.preloaderSoundButtonController.on(LobbyPreloaderSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.loaderUI.preloaderSoundButtonController.on(LobbyPreloaderSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.layout.on("hidewindow", this._onHideWindow, this);
		APP.layout.on("showwindow", this._onShowWindow, this);

		APP.once(LobbyApp.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);

		let lInfo_ussi = this.__getInfo();
		lInfo_ussi.soundsLoadingAvailable = APP.browserSupportController.info.isSoundEnabled;
		lInfo_ussi.soundsAllowedToPlay = APP.browserSupportController.info.isSoundEnabled;

		let browserSupportController = APP.browserSupportController;
		if (browserSupportController.info.isAudioContextSuspended)
		{
			browserSupportController.once(BrowserSupportController.EVENT_AUDIO_CONTEXT_STATE_CHANGED, this._onAudioContextUnlocked, this);
		}
		else
		{
			this._autoTurnAPPSoundsOnIfPossible();
		}
		
	}
	//...ILI INIT

	//ALL SOUNDS...
	_setSoundsOn(aSoundsOn_obj)
	{
		if (aSoundsOn_obj && aSoundsOn_obj.soundsOn !== undefined)
		{
			if (aSoundsOn_obj.soundsOn)
			{
				this._onSoundOffButtonClicked();
			}
			else
			{
				this._onSoundOnButtonClicked();
			}
		}
	}

	_onSoundOnButtonClicked(event)
	{
		this.__getInfo().soundsMuted = true;
		this._switchOffAllSounds();
	}

	_onSoundOffButtonClicked(event)
	{
		this.__getInfo().soundsMuted = false;
		this._switchOnAllSounds();
	}

	_onHideWindow(event)
	{
		this._switchOffAllSounds();
	}

	_onShowWindow(event)
	{
		this._switchOnAllSounds();
	}

	_onLobbyStarted(event)
	{
	}

	_onAudioContextUnlocked(event)
	{
		if (this.__getInfo().soundsMuted)
		{
			this._autoTurnAPPSoundsOnIfPossible();
		}
	}

	_autoTurnAPPSoundsOnIfPossible()
	{
		// if (APP.isMobile)
		// {
		// 	return; //for mobile devices turn sounds on only by user event (sound on/off button click)
		// }

		this.setSoundsOn({soundsOn: true});
	}

	_switchOffAllSounds()
	{
		if (this.__getInfo().isSoundsVolumeOn)
		{
			this.__getInfo().i_updateSoundsVolumeBefore();
		}
		this.__setBothSoundsVolume(0, 0);
	}

	_switchOnAllSounds()
	{
		if (!this.__getInfo().soundsMuted)
		{
			this.__setBothSoundsVolume(this.__getInfo().i_getSoundsVolumeBefore(SoundMetrics.i_SOUND_FX), this.__getInfo().i_getSoundsVolumeBefore(SoundMetrics.i_SOUND_MUSIC));
		}
	}

	_onSettingsScreenActivated(event)
	{
		if (this.__getInfo().isSoundsVolumeOn)
		{
			this.__getInfo().i_updateSoundsVolumeBefore();
		}
	}

	_onSoundVolumeChanged(event)
	{
		if (this.__setSoundsVolume(event.value, SoundMetrics.i_SOUND_FX))
		{
			this._valideteMuted();
			this.emit(SoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED);
		}
	}

	_onMusicVolumeChanged(event)
	{
		if (this.__setSoundsVolume(event.value, SoundMetrics.i_SOUND_MUSIC))
		{
			this._valideteMuted();
			this.emit(SoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED);
		}
	}

	_valideteMuted()
	{
		let lInfo_ssi = this.__getInfo();
		if(lInfo_ssi)
		{
			let lFxSoundsVolume_num = lInfo_ssi.i_getFxSoundsVolume();
			let lBgSoundsVolume_num = lInfo_ssi.i_getBgSoundsVolume();

			if(lFxSoundsVolume_num == 0 && lBgSoundsVolume_num == 0)
			{
				lInfo_ssi.soundsMuted = true;
			}
			else
			{
				lInfo_ssi.soundsMuted = false;
			}
		}
	}

	__setBothSoundsVolume(aFxSoundsVolume_num, aBgSoundsVolume_num)
	{
		let lFxChanged_bl = this.__setSoundsVolume(aFxSoundsVolume_num, SoundMetrics.i_SOUND_FX);
		let lBgChanged_bl = this.__setSoundsVolume(aBgSoundsVolume_num, SoundMetrics.i_SOUND_MUSIC);
		if (lFxChanged_bl || lBgChanged_bl)
		{
			this.emit(SoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED);
		}
	}

	__setSoundsVolume(aVolume_num, aType_str)
	{
		if (Number(aVolume_num) == null)
		{
			return false;
		}
		aVolume_num = Math.max(0, Math.min(1, aVolume_num));

		let lInfo_ussi = this.__getInfo();
		if (lInfo_ussi.i_getSoundsVolume(aType_str) === aVolume_num)
		{
			return false;
		}
		
		lInfo_ussi.i_setSoundsVolume(aVolume_num, aType_str);
		return true;
	}
	//...ALL SOUNDS
}

export default SoundSettingsController;