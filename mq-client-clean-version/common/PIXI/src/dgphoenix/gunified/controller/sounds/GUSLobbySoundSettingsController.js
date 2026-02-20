import SimpleController from '../../../unified/controller/base/SimpleController';
import GUSLobbySoundSettingsInfo from '../../model/sounds/GUSLobbySoundSettingsInfo';
import { APP } from '../../../unified/controller/main/globals';
import GUSettingsScreenController from '../uis/custom/secondary/settings/GUSettingsScreenController';
import GUPreloaderSoundButtonController from '../uis/custom/preloader/GUPreloaderSoundButtonController';
import BrowserSupportController from '../../../unified/controller/preloading/BrowserSupportController';
import GUSLobbySoundButtonController from '../uis/custom/GUSLobbySoundButtonController';
import SoundMetrics from '../../../unified/model/sounds/SoundMetrics';

class GUSLobbySoundSettingsController extends SimpleController
{
	static get EVENT_ON_SOUND_VOLUME_CHANGED() { return "onSoundSettingsVolumeChanged"; }

	//IL INTERFACE...
	setSoundsOn(aSoundsOn_obj)
	{
		this._setSoundsOn(aSoundsOn_obj);
	}
	//...IL INTERFACE

	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi ? aOptInfo_ussi : new GUSLobbySoundSettingsInfo());
	}

	//ILI INIT...
	__initControlLevel()
	{
		super.__initControlLevel();

		APP.secondaryScreenController.settingsScreenController.on(GUSettingsScreenController.EVENT_ON_SOUND_VOLUME_CHANGED, this._onSoundVolumeChanged, this);
		APP.secondaryScreenController.settingsScreenController.on(GUSettingsScreenController.EVENT_ON_MUSIC_VOLUME_CHANGED, this._onMusicVolumeChanged, this);
		APP.secondaryScreenController.settingsScreenController.on(GUSettingsScreenController.EVENT_SCREEN_ACTIVATED, this._onSettingsScreenActivated, this);

		APP.commonPanelController.soundButtonController.on(GUSLobbySoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.commonPanelController.soundButtonController.on(GUSLobbySoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.dialogsController.soundButtonController.on(GUSLobbySoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.dialogsController.soundButtonController.on(GUSLobbySoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.loaderUI.preloaderSoundButtonController.on(GUPreloaderSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		APP.loaderUI.preloaderSoundButtonController.on(GUPreloaderSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		APP.layout.on("hidewindow", this._onHideWindow, this);
		APP.layout.on("showwindow", this._onShowWindow, this);

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

	_onSoundOnButtonClicked()
	{
		this.__getInfo().soundsMuted = true;
		this._switchOffAllSounds();
	}

	_onSoundOffButtonClicked()
	{
		this.__getInfo().soundsMuted = false;
		this._switchOnAllSounds();
	}

	_onHideWindow()
	{
		this._switchOffAllSounds();
	}

	_onShowWindow()
	{
		this._switchOnAllSounds();
	}

	_onAudioContextUnlocked()
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

		this.setSoundsOn({ soundsOn: true });
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

	_onSettingsScreenActivated()
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
			this.emit(GUSLobbySoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED);
		}
	}

	_onMusicVolumeChanged(event)
	{
		if (this.__setSoundsVolume(event.value, SoundMetrics.i_SOUND_MUSIC))
		{
			this._valideteMuted();
			this.emit(GUSLobbySoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED);
		}
	}

	_valideteMuted()
	{
		let lInfo_ssi = this.__getInfo();
		if (lInfo_ssi)
		{
			let lFxSoundsVolume_num = lInfo_ssi.i_getFxSoundsVolume();
			let lBgSoundsVolume_num = lInfo_ssi.i_getBgSoundsVolume();

			if (lFxSoundsVolume_num == 0 && lBgSoundsVolume_num == 0)
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
			this.emit(GUSLobbySoundSettingsController.EVENT_ON_SOUND_VOLUME_CHANGED);
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

export default GUSLobbySoundSettingsController;