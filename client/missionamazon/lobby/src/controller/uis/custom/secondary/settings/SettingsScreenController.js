import SimpleUIController from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import SettingsScreenView from '../../../../../view/uis/custom/secondary/settings/SettingsScreenView';
import LobbySoundButtonController from '../LobbySoundButtonController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import {LOBBY_MESSAGES} from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import DialogsInfo from '../../../../../model/uis/custom/dialogs/DialogsInfo';

class SettingsScreenController extends SimpleUIController
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()					{ return SettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED; }
	static get EVENT_ON_SOUND_VOLUME_CHANGED()				{ return SettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED; }
	static get EVENT_ON_MUSIC_VOLUME_CHANGED()				{ return SettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED; }
	static get EVENT_ON_OK_BUTTON_CLICKED()					{ return SettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED; }
	static get EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED()	{ return SettingsScreenView.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED; }
	static get EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE()		{ return SettingsScreenView.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE; }
	static get EVENT_ON_TUTORIAL_TOGGLE_BTN_CLICK()			{ return SettingsScreenView.EVENT_ON_TUTORIAL_TOGGLE_BTN_CLICK; }
	static get EVENT_SCREEN_ACTIVATED()						{ return "onSettingsScreenActivated"; }
	static get EVENT_SCREEN_DEACTIVATED()					{ return "onSettingsScreenDeactivated"; }

	showScreen()
	{
		this._showScreen();
	}

	hideScreen()
	{
		this._hideScreen();
	}

	cancelChanges()
	{
		this._restoreVolumeSettings();
	}

	getVolumeSettings()
	{
		return this._fVolumeSettings_obj;
	}

	//INIT...
	__init()
	{
		this._fVolumeSettings_obj = {};
		this._fTutorialRememberState_bln = null;
		this._fIsToggleStateChanged_bln = null;

		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.commonPanelController.soundButtonController.on(LobbySoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._updateMuteSettings, this);
		APP.commonPanelController.soundButtonController.on(LobbySoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._updateUnMuteSettings, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;
		lView_ssv.on(SettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED, this._onClose, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED, this._onOkClicked, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, this._onSoundVolumeChanged, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, this._onMusicVolumeChanged, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, this._onTutorialStateChanged, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE, this.emit, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_TUTORIAL_TOGGLE_BTN_CLICK, this.emit, this);
	}
	//...INIT

	_showScreen()
	{
		if(APP.battlegroundController.isSecondaryScreenTimerRequired())
		{
			clearInterval(this.interval);

			this.view.updateTimeIndicator(APP.battlegroundController.info.getFormattedTimeToStart(false));
			this.interval = setInterval(this._tick.bind(this), 100);

			if(!APP.dialogsController.info.hasActiveDialogWithId(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS))
			{
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_HIDE);
			}
		}

		let lView_ssv = this.view;
		lView_ssv.visible = true;

		this._fTutorialRememberState_bln = APP.playerController.info.toolTipEnabled;
		lView_ssv.updateTutorialToggleState(this._fTutorialRememberState_bln);

		this._updateVolumeSettings();

		this.emit(SettingsScreenController.EVENT_SCREEN_ACTIVATED);
	}

	_hideScreen()
	{
		let lView_ssv = this.view;
		if (lView_ssv)
		{
			lView_ssv.visible = false;
			this.emit(SettingsScreenController.EVENT_SCREEN_DEACTIVATED, {tutorialShowAgainRequired: !!this._fIsToggleStateChanged_bln});

			clearInterval(this.interval);
			this.view.hideTimeIndicator();

			if( APP.battlegroundController.isSecondaryScreenTimerRequired()
				&& !APP.dialogsController.info.hasActiveDialogWithId(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS)
				)
			{
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_SHOW);
			}
		}
	}

	_tick()
	{
		this.view.updateTimeIndicator(APP.battlegroundController.getFormattedTimeToStart(false));

		if (APP.battlegroundController.info.getTimeToStartInMillis() <= 0)
		{
			clearInterval(this.interval);
			this._hideScreen();
		}
	}

	_onClose()
	{
		this._fIsToggleStateChanged_bln = false;
		this._fNewTutorialState_bln = null;
		this._validateTutorialState();
		this.emit(SettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED);
	}

	_onOkClicked()
	{
		this._validateTutorialState();
		this.emit(SettingsScreenController.EVENT_ON_OK_BUTTON_CLICKED);
		this._fIsToggleStateChanged_bln = false;
		this._saveVolumeSettings();
	}

	_onSoundVolumeChanged(aData_obj)
	{
		let lVal_num = aData_obj.value;
		this.emit(SettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, {value: lVal_num});
	}

	_onMusicVolumeChanged(aData_obj)
	{
		let lVal_num = aData_obj.value;
		this.emit(SettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, {value: lVal_num});
	}

	_validateTutorialState()
	{
		if (this._fNewTutorialState_bln !== null && this._fNewTutorialState_bln !== undefined)
		{
			this.view.updateTutorialToggleState(this._fTutorialRememberState_bln);
			this.emit(SettingsScreenController.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, {value: this._fNewTutorialState_bln});
		}

		this._fTutorialRememberState_bln = null;
		this._fNewTutorialState_bln = null;
	}

	_onTutorialStateChanged(aData_obj)
	{
		this._fNewTutorialState_bln = aData_obj.value;
		this._fIsToggleStateChanged_bln = true;
	}

	_saveVolumeSettings(musicVolume, fxVolume)
	{
		let lSoundSettingsInfo_ssi = APP.soundSettingsController.info;
		this._fVolumeSettings_obj.musicVolume = !!musicVolume||musicVolume===0?musicVolume:lSoundSettingsInfo_ssi.i_getBgSoundsVolume();
		this._fVolumeSettings_obj.fxVolume = !!fxVolume||fxVolume===0?fxVolume:lSoundSettingsInfo_ssi.i_getFxSoundsVolume();
		APP.secondaryScreenController.secondaryScreenView.settingsScreenView.soundVolumeValue = this._fVolumeSettings_obj.fxVolume;
		APP.secondaryScreenController.secondaryScreenView.settingsScreenView.musicVolumeValue = this._fVolumeSettings_obj.musicVolume;	
	}

	_updateUnMuteSettings()
	{
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			let lSoundSettingsInfo_ssi = APP.soundSettingsController.info;

			let muteVolumeSettings = {musicVolume:lSoundSettingsInfo_ssi.i_getBgSoundsVolume(), fxVolume:lSoundSettingsInfo_ssi.i_getFxSoundsVolume()}
			lView_ssv.setSettings({ soundVolumes: muteVolumeSettings });
		}
	}

	_updateMuteSettings()
	{
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			let muteVolumeSettings = {musicVolume:0, fxVolume:0}
			lView_ssv.setSettings({ soundVolumes: muteVolumeSettings });
		}
	}
	_updateVolumeSettings()
	{
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			this._saveVolumeSettings();
			lView_ssv.setSettings({ soundVolumes: this._fVolumeSettings_obj });
		}
	}

	_restoreVolumeSettings()
	{
		this._saveVolumeSettings(this._fVolumeSettings_obj.musicVolume, this._fVolumeSettings_obj.fxVolume);
		APP.secondaryScreenController.secondaryScreenView.settingsScreenView.updateSlidersPosition();
	}

	destroy()
	{
		let lView_ssv = this.view;

		if (lView_ssv)
		{
			lView_ssv.off(SettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED, this.emit, this);
			lView_ssv.off(SettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED, this.emit, this);
			lView_ssv.off(SettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, this.emit, this);
			lView_ssv.off(SettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, this.emit, this);
		}

		super.destroy();

		this._fVolumeSettings_obj = null;

		this._fTutorialRememberState_bln = null;
		this._fIsToggleStateChanged_bln = null;
	}
}

export default SettingsScreenController