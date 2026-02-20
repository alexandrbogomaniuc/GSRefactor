import SimpleUIController from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import SettingsScreenView from '../../../../../view/uis/custom/secondary/settings/SettingsScreenView';
import GameSoundButtonController from '../GameSoundButtonController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const HP_BAR_DEFAULT_VALUE = "false";

class SettingsScreenController extends SimpleUIController
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()					{ return SettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED; }
	static get EVENT_ON_SOUND_VOLUME_CHANGED()				{ return SettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED; }
	static get EVENT_ON_MUSIC_VOLUME_CHANGED()				{ return SettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED; }
	static get EVENT_ON_OK_BUTTON_CLICKED()					{ return SettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED; }
	static get EVENT_SCREEN_ACTIVATED()						{ return "onSettingsScreenActivated"; }
	static get EVENT_SCREEN_DEACTIVATED()					{ return "onSettingsScreenDeactivated"; }

	showScreen()
	{
		this._showScreen();
	}

	hideScreen()
	{
		this._hideSceeen();
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

		super.__init();
	}

	__initControlLevel()
	{
		console.log("init controller level changes")
		super.__initControlLevel();

		APP.dialogsController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._updateMuteSettings, this);
   		APP.dialogsController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._updateUnMuteSettings, this);

		APP.gameController.bottomPanelController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._updateMuteSettings, this);
		APP.gameController.bottomPanelController.soundButtonController.on(GameSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._updateUnMuteSettings, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;
		lView_ssv.on(SettingsScreenView.EVENT_ON_CLOSE_BTN_CLICKED, this._onClose, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_OK_BUTTON_CLICKED, this._onOkClicked, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_SOUND_VOLUME_CHANGED, this.emit, this);
		lView_ssv.on(SettingsScreenView.EVENT_ON_MUSIC_VOLUME_CHANGED, this.emit, this);
	}
	//...INIT

	_showScreen()
	{
		let lView_ssv = this.view;
		lView_ssv.visible = true;

		this._updateVolumeSettings();

		this.emit(SettingsScreenController.EVENT_SCREEN_ACTIVATED);
	}

	_hideSceeen()
	{
		let lView_ssv = this.view;
		if (lView_ssv)
		{
			lView_ssv.visible = false;
			this.emit(SettingsScreenController.EVENT_SCREEN_DEACTIVATED);
		}
	}

	_onClose()
	{
		this.emit(SettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED);
	}

	_onOkClicked()
	{
		this.emit(SettingsScreenController.EVENT_ON_OK_BUTTON_CLICKED);
	}

	_saveVolumeSettings()
	{
		let lSoundSettingsInfo_ssi = APP.soundSettingsController.info;

		this._fVolumeSettings_obj.fxVolume = lSoundSettingsInfo_ssi.i_getFxSoundsVolume();
		this._fVolumeSettings_obj.musicVolume = lSoundSettingsInfo_ssi.i_getBgSoundsVolume();
	}

	_updateVolumeSettings()
	{
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			this._saveVolumeSettings();
			lView_ssv.setSettings({soundVolumes: this._fVolumeSettings_obj});
		}
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

	_restoreVolumeSettings()
	{
		this.emit(SettingsScreenController.EVENT_ON_SOUND_VOLUME_CHANGED, {value: this._fVolumeSettings_obj.fxVolume});
		this.emit(SettingsScreenController.EVENT_ON_MUSIC_VOLUME_CHANGED, {value: this._fVolumeSettings_obj.musicVolume});
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

		if (APP.gameController.bottomPanelController && APP.gameController.bottomPanelController.soundButtonController)
		{
			APP.gameController.bottomPanelController.soundButtonController.off(GameSoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._updateVolumeSettings, this);
			APP.gameController.bottomPanelController.soundButtonController.off(GameSoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._updateVolumeSettings, this);
		}

		super.destroy();

		this._fVolumeSettings_obj = null;
	}
}

export default SettingsScreenController