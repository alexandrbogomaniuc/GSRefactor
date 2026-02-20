import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import SoundMetrics from '../../../../../common/PIXI/src/dgphoenix/unified/model/sounds/SoundMetrics';
import SoundSettingsInfo from '../../model/sounds/SoundSettingsInfo';
import SettingsScreenController from '../uis/custom/secondary/settings/SettingsScreenController';
import GameSoundButtonController from '../uis/custom/secondary/GameSoundButtonController';
import GamePreloaderSoundButtonController from '../uis/custom/preloader/GamePreloaderSoundButtonController';

import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameApp from '../../CrashAPP';
import BrowserSupportController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/preloading/BrowserSupportController';

class SoundSettingsController extends SimpleController
{
	static get EVENT_ON_SOUND_VOLUME_CHANGED() 		{return "onSoundSettingsVolumeChanged";}
	static get EVENT_ON_SOUND_SETTINGS_CHANGED() 	{return "onSoundSettingsChanged";}

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

		APP.gameController.bottomPanelController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.gameController.bottomPanelController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.dialogsController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.dialogsController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.loaderUI.preloaderSoundButtonController.on(GamePreloaderSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.loaderUI.preloaderSoundButtonController.on(GamePreloaderSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.layout.on("hidewindow", this._onHideWindow, this);
		APP.layout.on("showwindow", this._onShowWindow, this);

		APP.once(GameApp.EVENT_ON_GAME_STARTED, this._onGameStarted, this);

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

	_onGameStarted(event)
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
		//return; // https://jira.dgphoenix.com/browse/CRG-74 - 0019358: CRSP-74 MAKE GAME AUDIO OFF BY DEFAULT
		

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
			this._notifyVolumeChanged();
		}
	}

	_onMusicVolumeChanged(event)
	{
		if (this.__setSoundsVolume(event.value, SoundMetrics.i_SOUND_MUSIC))
		{
			this._valideteMuted();
			this._notifyVolumeChanged();
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
			this._notifyVolumeChanged();
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

	_notifyVolumeChanged()
	{
		this.emit(SoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED);

		this._notifySoundSettingsChanged();
	}

	_notifySoundSettingsChanged()
	{
		let lInfo_ussi = this.__getInfo();
		let lSoundSettings_obj = {
									"fxVolume": 	lInfo_ussi.i_getFxSoundsVolume(),
									"musicVolume": 	lInfo_ussi.i_getBgSoundsVolume(),
									"muted":		lInfo_ussi.soundsMuted
								};

		this.emit(SoundSettingsController.EVENT_ON_SOUND_SETTINGS_CHANGED, lSoundSettings_obj);
	}
}

export default SoundSettingsController;